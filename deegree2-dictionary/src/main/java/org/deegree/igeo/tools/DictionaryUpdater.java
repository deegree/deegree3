//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.igeo.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Pair;
import org.deegree.framework.util.StringTools;
import org.deegree.igeo.enterprise.dictionary.DefinitionType;
import org.deegree.igeo.enterprise.dictionary.DictionaryResourceType;
import org.deegree.igeo.enterprise.dictionary.ObjectFactory;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.DBPoolException;
import org.deegree.io.JDBCConnection;
import org.deegree.io.datastore.schema.MappedGMLSchema;
import org.deegree.io.datastore.schema.MappedGMLSchemaDocument;
import org.deegree.io.datastore.sql.SQLDatastoreConfiguration;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfigurationDocument;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DictionaryUpdater {

    private static final long serialVersionUID = 7984333284606734499L;

    private static final ILogger LOG = LoggerFactory.getLogger( DictionaryUpdater.class );

    private String wfsConfigFile;

    private String targetFile;

    private String dbURL;

    private String dbDriver;

    private String dbUser;

    private String dbPassword;

    private Map<String, DBFeatureProperty> dbFeatureProps;

    private Map<String, DBConnection> dbConnections;

    /**
     * 
     * @param wfsConfigFile
     * @param targetFile
     * @param dbDriver
     * @param dbURL
     * @param dbUser
     * @param dbPassword
     */
    public DictionaryUpdater( String wfsConfigFile, String targetFile, String dbDriver, String dbURL, String dbUser,
                              String dbPassword ) {
        this.wfsConfigFile = wfsConfigFile;
        this.targetFile = targetFile;
        this.dbDriver = dbDriver;
        this.dbURL = dbURL;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        dbFeatureProps = new HashMap<String, DBFeatureProperty>( 1000 );
        dbConnections = new HashMap<String, DBConnection>( 1000 );
    }

    /**
     * execute dictionary update
     * 
     * @throws Exception
     */
    public void execute()
                            throws Exception {
        readDBConnectionsForFeatureTypes();
        readFeaturePropertiesFromDB();
        removePropertiesNotServed();
        updateProperties();
        updateConfiguration();
    }

    private void readDBConnectionsForFeatureTypes()
                            throws Exception {
        LOG.logInfo( "read featureType-database association from WFS configuration ..." );
        WFSConfigurationDocument doc = new WFSConfigurationDocument();
        doc.load( new File( wfsConfigFile ).toURI().toURL() );
        String[] directories = doc.getDeegreeParams().getDataDirectories();
        for ( String directory : directories ) {
            File dir = new File( directory );
            File[] files = dir.listFiles( new FilenameFilter() {
                public boolean accept( File dir, String name ) {
                    if ( name == null ) {
                        return false;
                    }
                    return name.toLowerCase().endsWith( ".xsd" );
                }
            } );
            extractMappedFeatureTypes( files );
        }
    }

    private void extractMappedFeatureTypes( File[] fileNames )
                            throws Exception {

        for ( int i = 0; i < fileNames.length; i++ ) {
            URL fileURL = fileNames[i].toURI().toURL();
            LOG.logInfo( "Reading annotated GML application schema from URL '" + fileURL + "'." );
            MappedGMLSchemaDocument doc = new MappedGMLSchemaDocument();
            doc.load( fileURL );
            MappedGMLSchema gmlSchema = doc.parseMappedGMLSchema();
            SQLDatastoreConfiguration sqlDBConf = (SQLDatastoreConfiguration) gmlSchema.getDatastore().getConfiguration();

            FeatureType[] fTs = gmlSchema.getFeatureTypes();
            for ( FeatureType featureType : fTs ) {
                DBConnection dbConn = new DBConnection( featureType, sqlDBConf.getJDBCConnection() );
                String key = featureType.getName().getNamespace().toASCIIString() + ':'
                             + featureType.getName().getLocalName();
                dbConnections.put( key, dbConn );
            }

        }
    }

    private void readFeaturePropertiesFromDB()
                            throws Exception {
        LOG.logInfo( "read featureType/properties from database ..." );
        DBConnectionPool pool = DBConnectionPool.getInstance();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = pool.acquireConnection( dbDriver, dbURL, dbUser, dbPassword );
            stmt = conn.createStatement();
            rs = stmt.executeQuery( "select * from igeo_lookupdefinition" );
            while ( rs.next() ) {
                int id = ( (Number) rs.getObject( 1 ) ).intValue();
                String featureType = rs.getString( 2 );
                String property = rs.getString( 3 );
                String namespace = rs.getString( 4 );
                String dictionaryTable = rs.getString( 5 );
                String s = rs.getString( 6 );
                if ( s == null ) {
                    s = "n";
                }
                char updateMode = s.charAt( 0 );
                DBFeatureProperty fp = new DBFeatureProperty( id, featureType, property, namespace, dictionaryTable,
                                                              updateMode );
                dbFeatureProps.put( namespace + ':' + featureType + '.' + property, fp );
            }
        } catch ( Exception e ) {
            LOG.logError( toString(), e );
            throw e;
        } finally {
            if ( rs != null ) {
                rs.close();
            }
            if ( stmt != null ) {
                stmt.close();
            }
            if ( conn != null ) {
                pool.releaseConnection( conn, dbDriver, dbURL, dbUser, dbPassword );
            }
        }
    }

    /**
     * remove properties that are listed within igeo_lookupdefinition table but not served by WFS anymore
     * 
     * @throws Exception
     */
    private void removePropertiesNotServed()
                            throws Exception {

        LOG.logInfo( "remove properties that are listed within igeo_lookupdefinition table but not served by WFS anymore ..." );

        // get properties that are listed at igeo_lookupdefinition but not served by the WFS anymore
        List<DBFeatureProperty> toBeRemoved = findPropertiesNotServed();

        DBConnectionPool pool = DBConnectionPool.getInstance();
        Connection conn = null;
        PreparedStatement stmt1 = null;
        Statement stmt2 = null;
        LOG.logInfo( "delete property from lookup table not served by WFS " );
        try {
            conn = pool.acquireConnection( dbDriver, dbURL, dbUser, dbPassword );
            stmt1 = conn.prepareStatement( "delete from igeo_lookupdefinition where id = ?" );
            stmt2 = conn.createStatement();
            // remove not served properties from map and database
            for ( DBFeatureProperty dbFeatureProperty : toBeRemoved ) {
                String key = dbFeatureProperty.namespace + ':' + dbFeatureProperty.featureType + '.'
                             + dbFeatureProperty.property;
                LOG.logInfo( "delete property: " + key );
                dbFeatureProps.remove( key );
                stmt1.setInt( 1, dbFeatureProperty.id );
                stmt1.execute();
                stmt2.execute( "drop table " + dbFeatureProperty.dictionaryTable + " CASCADE CONSTRAINTS PURGE" );
            }
            conn.commit();
        } catch ( Exception e ) {
            LOG.logError( toString(), e );
            throw e;
        } finally {
            if ( stmt1 != null ) {
                stmt1.close();
            }
            if ( conn != null ) {
                pool.releaseConnection( conn, dbDriver, dbURL, dbUser, dbPassword );
            }
        }
    }

    private List<DBFeatureProperty> findPropertiesNotServed() {
        List<DBFeatureProperty> toBeRemoved = new ArrayList<DBFeatureProperty>();
        Iterator<DBFeatureProperty> iterator = dbFeatureProps.values().iterator();
        while ( iterator.hasNext() ) {
            DBFeatureProperty featureProperty = (DBFeatureProperty) iterator.next();
            String ft = featureProperty.namespace + ':' + featureProperty.featureType;
            if ( dbConnections.containsKey( ft ) ) {
                DBConnection dbConn = dbConnections.get( ft );
                PropertyType[] pt = dbConn.featureType.getProperties();
                boolean match = false;
                for ( PropertyType propertyType : pt ) {
                    if ( propertyType.getName().getLocalName().equals( featureProperty.property ) ) {
                        match = true;
                        break;
                    }
                }
                if ( !match ) {
                    toBeRemoved.add( featureProperty );
                }
            } else {
                toBeRemoved.add( featureProperty );
            }
        }
        return toBeRemoved;
    }

    private void updateProperties()
                            throws Exception {

        LOG.logInfo( "update dictonary tables ..." );

        Iterator<DBFeatureProperty> iterator = dbFeatureProps.values().iterator();
        while ( iterator.hasNext() ) {
            DBFeatureProperty featureProperty = (DBFeatureProperty) iterator.next();            
            if ( featureProperty.updateMode != 'l' ) {
                updateDictTable( featureProperty );
            }
        }

    }

    /**
     * @param dictionaryTable
     * @return
     * @throws Exception
     */
    private boolean doesDictTableExist( String dictionaryTable )
                            throws Exception {
        if ( dictionaryTable == null ) {
            return false;
        }
        DBConnectionPool pool = DBConnectionPool.getInstance();
        Connection conn = pool.acquireConnection( dbDriver, dbURL, dbUser, dbPassword );
        Statement stmt = conn.createStatement();
        try {
            stmt.executeQuery( "select * from " + dictionaryTable + " where 1 = 2" );
        } catch ( Exception e ) {
            e.printStackTrace();
            return false;
        } finally {
            if ( stmt != null ) {
                stmt.close();
            }
            if ( conn != null ) {
                pool.releaseConnection( conn, dbDriver, dbURL, dbUser, dbPassword );
            }
        }
        return true;
    }

    /**
     * @param dictionaryTable
     * @throws Exception
     */
    private String createNewDictTable( DBFeatureProperty featureProperty, Object exampleData )
                            throws Exception {

        DBConnectionPool pool = DBConnectionPool.getInstance();
        Connection conn = pool.acquireConnection( dbDriver, dbURL, dbUser, dbPassword );
        Statement stmt = conn.createStatement();
        String dictionaryTable = featureProperty.dictionaryTable;
        try {
            if ( dictionaryTable == null ) {
                dictionaryTable = "d_" + Math.random();
                dictionaryTable = StringTools.replace( dictionaryTable, ".", "", true );
            }
            LOG.logInfo( "create table: " + dictionaryTable );
            String s = "CREATE TABLE " + dictionaryTable + " ( ";
            if ( exampleData instanceof Number ) {
                s = s + " code number(10) ";
            } else {
                s = s + " code varchar2(200) ";
            }
            s = s + ", de varchar2(200) )";
            stmt.execute( s );
            stmt.execute( "UPDATE igeo_lookupdefinition set dictionaryTable = '" + dictionaryTable + "' where id = "
                          + featureProperty.id );
            conn.commit();
        } catch ( Exception e ) {
            LOG.logError( toString(), e );
            throw e;
        } finally {
            if ( stmt != null ) {
                stmt.close();
            }
            if ( conn != null ) {
                pool.releaseConnection( conn, dbDriver, dbURL, dbUser, dbPassword );
            }
        }
        return dictionaryTable;

    }

    /**
     * @param featureProperty
     * @throws Exception
     */
    private void updateDictTable( DBFeatureProperty featureProperty )
                            throws Exception {
        String key = featureProperty.namespace + ':' + featureProperty.featureType;
        LOG.logInfo( "update dictionary table for: " + key );
        DBConnection dbConn = dbConnections.get( key );
        List<Object> valueList = getValueList( featureProperty, dbConn );
        
        if ( !doesDictTableExist( featureProperty.dictionaryTable ) ) {
            featureProperty.dictionaryTable = createNewDictTable( featureProperty, valueList.get(0) );
        }
        
        if ( featureProperty.updateMode == 'n' ) {
            clearTable( featureProperty.dictionaryTable );
        }
        appendValues( featureProperty.dictionaryTable, valueList );

    }

    /**
     * @param dictionaryTable
     * @param valueList
     * @throws Exception
     */
    private void appendValues( String dictionaryTable, List<Object> valueList )
                            throws Exception {
        List<Pair<Object, String>> dictTable = getDictTableValues( dictionaryTable );
        for ( Pair<Object, String> pair : dictTable ) {
            if ( valueList.contains( pair.second ) ) {
                valueList.remove( pair.second );
            }
        }
        DBConnectionPool pool = DBConnectionPool.getInstance();
        Connection conn = pool.acquireConnection( dbDriver, dbURL, dbUser, dbPassword );
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement( "INSERT INTO " + dictionaryTable + "(code, de) VALUES (?,?)" );
            for ( Object value : valueList ) {
                stmt.setObject( 1, value );
                stmt.setString( 2, value.toString() );
                stmt.execute();
            }
            conn.commit();
        } catch ( Exception e ) {
            LOG.logError( toString(), e );
            throw e;
        } finally {
            if ( stmt != null ) {
                stmt.close();
            }
            if ( conn != null ) {
                pool.releaseConnection( conn, dbDriver, dbURL, dbUser, dbPassword );
            }
        }
    }

    /**
     * @param dictionaryTable
     * @return
     * @throws Exception
     */
    private List<Pair<Object, String>> getDictTableValues( String dictionaryTable )
                            throws Exception {
        DBConnectionPool pool = DBConnectionPool.getInstance();
        Connection conn = pool.acquireConnection( dbDriver, dbURL, dbUser, dbPassword );
        Statement stmt = null;
        ResultSet rs = null;
        List<Pair<Object, String>> list = new ArrayList<Pair<Object, String>>( 1000 );
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery( "select * from " + dictionaryTable );
            while ( rs.next() ) {
                Object code = rs.getObject(  1 );
                // german language value
                String de = rs.getString( 2 );
                Pair<Object, String> pair = new Pair<Object, String>( code, de );
                list.add( pair );
            }
        } catch ( Exception e ) {
            LOG.logError( toString(), e );
            throw e;
        } finally {
            if ( rs != null ) {
                rs.close();
            }
            if ( stmt != null ) {
                stmt.close();
            }
            if ( conn != null ) {
                pool.releaseConnection( conn, dbDriver, dbURL, dbUser, dbPassword );
            }
        }
        return list;
    }

    /**
     * @param dictionaryTable
     * @throws Exception
     */
    private void clearTable( String dictionaryTable )
                            throws Exception {
        DBConnectionPool pool = DBConnectionPool.getInstance();
        Connection conn = pool.acquireConnection( dbDriver, dbURL, dbUser, dbPassword );
        Statement stmt = conn.createStatement();
        LOG.logInfo( "delete from  " + dictionaryTable );
        try {
            stmt.execute( "delete from " + dictionaryTable );
        } catch ( Exception e ) {
            LOG.logError( toString(), e );
            throw e;
        } finally {
            if ( stmt != null ) {
                stmt.close();
            }
            if ( conn != null ) {
                pool.releaseConnection( conn, dbDriver, dbURL, dbUser, dbPassword );
            }
        }
    }

    private List<Object> getValueList( DBFeatureProperty featureProperty, DBConnection dbConn )
                            throws DBPoolException, SQLException {
        DBConnectionPool pool = DBConnectionPool.getInstance();
        Connection wfsConn = pool.acquireConnection( dbConn.jdbc.getDriver(), dbConn.jdbc.getURL(),
                                                     dbConn.jdbc.getUser(), dbConn.jdbc.getPassword() );
        Statement wfsStmt = null;
        ResultSet wfsRs = null;
        List<Object> list = new ArrayList<Object>( 1000 );
        try {
            wfsStmt = wfsConn.createStatement();
            LOG.logInfo( "select distinct(" + featureProperty.property + ") from " + featureProperty.featureType );
            wfsRs = wfsStmt.executeQuery( "select distinct(" + featureProperty.property + ") from "
                                          + featureProperty.featureType );
            while ( wfsRs.next() ) {
                Object o = wfsRs.getObject( 1 );
                if ( o != null ) {
                    list.add( o );
                }
            }
        } catch ( Exception e ) {
            LOG.logError( toString(), e );
            if ( wfsRs != null ) {
                wfsRs.close();
            }
            if ( wfsStmt != null ) {
                wfsStmt.close();
            }
            if ( wfsConn != null ) {
                pool.releaseConnection( wfsConn, dbConn.jdbc.getDriver(), dbConn.jdbc.getURL(), dbConn.jdbc.getUser(),
                                        dbConn.jdbc.getPassword() );
            }
        }
        return list;
    }

    private void updateConfiguration()
                            throws Exception {
        DictionaryResourceType dictRes = null;
        JAXBContext jc = JAXBContext.newInstance( "org.deegree.igeo.enterprise.dictionary" );
        try {            
            Unmarshaller u = jc.createUnmarshaller();
            JAXBElement<?> o = (JAXBElement<?>) u.unmarshal( DictionaryUpdater.class.getResource( "configuration_template.xml" ) );
            dictRes = (DictionaryResourceType) o.getValue();
        } catch ( Exception e ) {
            LOG.logError( toString(), e );
            throw new Exception( e );
        }
        dictRes.getConnection().setDriver( dbDriver );
        dictRes.getConnection().setUrl( dbURL );
        dictRes.getConnection().setUser( dbUser );
        dictRes.getConnection().setPassword( dbPassword );

        DBConnectionPool pool = DBConnectionPool.getInstance();
        Connection conn = pool.acquireConnection( dbDriver, dbURL, dbUser, dbPassword );
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery( "select featuretype, property, namespace, dictionaryTable from igeo_lookupdefinition" );
            while ( rs.next() ) {
                DefinitionType def = new DefinitionType();
                def.setName( rs.getString( 1 ) + '/' + rs.getString( 2 ) );
                def.setCodeSpace( rs.getString( 3 ) );
                def.setTable( rs.getString( 4 ) );
                dictRes.getDefinition().add( def );
            }
        } catch ( Exception e ) {
            LOG.logError( toString(), e );
            if ( rs != null ) {
                rs.close();
            }
            if ( stmt != null ) {
                stmt.close();
            }
            if ( conn != null ) {
                pool.releaseConnection( conn, dbDriver, dbURL, dbUser, dbPassword );
            }
        }
        
        try {
            FileOutputStream fos = new FileOutputStream( targetFile );
            jc = JAXBContext.newInstance( "org.deegree.igeo.enterprise.dictionary" );
            Marshaller m = jc.createMarshaller();
            ObjectFactory of = new ObjectFactory();
            m.marshal( of.createDictionaryResource( dictRes ), fos );
            fos.flush();
            fos.close();
        } catch ( Exception e ) {
            LOG.logError( toString(), e );
            throw e;
        }
                
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "driver: " ).append( dbDriver ).append( "\n" );
        sb.append( "url: " ).append( dbURL ).append( "\n" );
        sb.append( "user: " ).append( dbUser ).append( "\n" );
        sb.append( "password: " ).append( dbPassword ).append( "\n" );
        sb.append( "wfsConfigFile: " ).append( wfsConfigFile ).append( "\n" );
        return sb.toString();
    }

    /**
     * 
     * @param args
     * @throws Exception
     */
    public static void main( String[] args )
                            throws Exception {
        try {
            Properties map = new Properties();
            for ( int i = 0; i < args.length; i += 2 ) {
                if ( args[i].equals( "-h" ) || args[i].equals( "-?" ) ) {
                    printHelp();
                    System.exit( 0 );
                }
                map.put( args[i], args[i + 1] );
            }
            if ( !validate( map ) ) {
                System.exit( 1 );
            }
            String wfsConfigFile = map.getProperty( "-wfsConfigFile" );
            String targetFile = map.getProperty( "-targetFile" );
            String dbDriver = map.getProperty( "-driver" );
            String dbURL = map.getProperty( "-url" );
            String dbUser = map.getProperty( "-user" );
            String dbPassword = map.getProperty( "-password" );
            DictionaryUpdater du = new DictionaryUpdater( wfsConfigFile, targetFile, dbDriver, dbURL, dbUser,
                                                          dbPassword );
            du.execute();
        } catch ( Exception e ) {
            System.exit( 1 );
        }
        System.exit( 0 );
    }

    private static boolean validate( Properties map ) {
        if ( map.getProperty( "-driver" ) == null ) {
            System.out.println( "-driver must be defined" );
            return false;
        }
        if ( map.getProperty( "-url" ) == null ) {
            System.out.println( "-url must be defined" );
            return false;
        }
        if ( map.getProperty( "-user" ) == null ) {
            System.out.println( "-user must be defined" );
            return false;
        }
        if ( map.getProperty( "-password" ) == null ) {
            System.out.println( "-password must be defined" );
            return false;
        }
        if ( map.getProperty( "-wfsConfigFile" ) == null ) {
            System.out.println( "-wfsConfigFile must be defined" );
            return false;
        }
        if ( map.getProperty( "-targetFile" ) == null ) {
            System.out.println( "-targetFile must be defined" );
            return false;
        }
        return true;
    }

    /**
     * 
     */
    private static void printHelp() {
        // TODO Auto-generated method stub

    }

    // //////////////////////////////////////////////////////////////////////////////////
    // inner classes
    // //////////////////////////////////////////////////////////////////////////////////

    private class DBFeatureProperty {
        private int id;

        private String featureType;

        private String property;

        private String namespace;

        private String dictionaryTable;

        private char updateMode;

        /**
         * @param id
         * @param featureType
         * @param property
         * @param namespace
         * @param dictionaryTable
         * @param updateMode
         */
        public DBFeatureProperty( int id, String featureType, String property, String namespace,
                                  String dictionaryTable, char updateMode ) {
            this.id = id;
            this.featureType = featureType;
            this.property = property;
            this.namespace = namespace;
            this.dictionaryTable = dictionaryTable;
            this.updateMode = updateMode;
        }

    }

    private class DBConnection {
        private FeatureType featureType;

        private JDBCConnection jdbc;

        /**
         * 
         * @param featureType
         * @param jdbc
         */
        public DBConnection( FeatureType featureType, JDBCConnection jdbc ) {
            this.featureType = featureType;
            this.jdbc = jdbc;
        }

    }

}
