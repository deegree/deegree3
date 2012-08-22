//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

package org.deegree.tools.raster;

import static java.io.File.separator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ConvenienceFileFilter;
import org.deegree.framework.util.FileUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.io.DBConnectionPool;
import org.deegree.io.DBPoolException;
import org.deegree.io.datastore.sql.postgis.PGgeometryAdapter;
import org.deegree.io.dbaseapi.DBaseException;
import org.deegree.io.shpapi.shape_new.ShapeFile;
import org.deegree.io.shpapi.shape_new.ShapeFileReader;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.xml.sax.SAXException;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ImportIndexIntoDB {

    private static ILogger LOG = LoggerFactory.getLogger( ImportIndexIntoDB.class );

    private static final String createPG = "create_wcs_table_template_postgis.sql";

    private static final String indexPG = "create_wcs_table_index_template_postgis.sql";

    private static final String createOrcl = "create_wcs_table_template_oracle.sql";

    private static final String indexOrcl = "create_wcs_index_table_template_oracle.sql";

    private static URI app = null;

    static {
        try {
            app = new URI( "http://www.deegree.org/app" );
        } catch ( URISyntaxException e ) {
            e.printStackTrace();
        }
    }

    private String table;

    private String user;

    private String pw;

    private String driver;

    private String url;

    private String rootDir;

    private File configurationFile;

    private boolean append = false;

    private boolean switchDir = false;

    /**
     * @param driver
     * @param url
     * @param user
     * @param pw
     * @param table
     * @param rootDir
     */
    public ImportIndexIntoDB( String driver, String url, String user, String pw, String table, String rootDir ) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.pw = pw;
        this.table = table;
        this.rootDir = rootDir;
        ConvenienceFileFilter ff = new ConvenienceFileFilter( false, "XML" );
        File dir = new File( this.rootDir );
        configurationFile = dir.listFiles( ff )[0];
    }

    /**
     * @param driver
     * @param url
     * @param user
     * @param pw
     * @param table
     * @param rootDir
     * @param append
     * @param switchDir
     */
    public ImportIndexIntoDB( String driver, String url, String user, String pw, String table, String rootDir,
                              boolean append, boolean switchDir ) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.pw = pw;
        this.table = table;
        this.rootDir = rootDir;
        ConvenienceFileFilter ff = new ConvenienceFileFilter( false, "XML" );
        File dir = new File( this.rootDir );
        configurationFile = dir.listFiles( ff )[0];
        this.append = append;
        this.switchDir = switchDir;
    }

    /**
     * main method to perform indexing of raster tile via a spatial DB
     *
     * @throws IOException
     * @throws DBPoolException
     * @throws SQLException
     * @throws DBaseException
     * @throws GeometryException
     * @throws SAXException
     * @throws TransformerException
     */
    public void perform()
                            throws IOException, DBPoolException, SQLException, DBaseException, GeometryException,
                            SAXException, TransformerException {
        if ( !append ) {
            createTables();
        }
        fillIndexTable();
        // index will be created after data has been added to increase performance
        if ( !append ) {
            createIndex();
        }
        adaptConfiguration();
    }

    /**
     * fill the pyramid table with informations about the assoziation between levels and scales
     *
     * @param map
     * @throws DBPoolException
     * @throws SQLException
     */
    private void fillPyramidTable( Map<File, PyramidHelper> map )
                            throws DBPoolException, SQLException {
        DBConnectionPool pool = DBConnectionPool.getInstance();
        Connection con = pool.acquireConnection( driver, url, user, pw );
        Iterator<PyramidHelper> iterator = map.values().iterator();
        while ( iterator.hasNext() ) {
            PyramidHelper helper = iterator.next();
            String sql = StringTools.concat( 200, "INSERT INTO ", table,
                                             "_pyr (level,minscale,maxscale) values (?,?,?)" );
            PreparedStatement stmt = con.prepareStatement( sql );
            stmt.setInt( 1, helper.level );
            stmt.setFloat( 2, helper.minscale );
            stmt.setFloat( 3, helper.maxscale );
            stmt.execute();
            stmt.close();
        }
        LOG.logInfo( "pyramid table filled!" );

    }

    /**
     * creates DB-indexes for pyramid and tile-index tables
     *
     * @throws IOException
     * @throws DBPoolException
     * @throws SQLException
     */
    private void createIndex()
                            throws IOException, DBPoolException, SQLException {
        String template = null;
        if ( driver.toUpperCase().indexOf( "ORACLE" ) > -1 ) {
            template = indexOrcl;
        } else if ( driver.toUpperCase().indexOf( "POSTGRES" ) > -1 ) {
            template = indexPG;
        }

        StringBuffer sb = FileUtils.readTextFile( ImportIndexIntoDB.class.getResource( template ) );
        String tmp = StringTools.replace( sb.toString(), "$TABLE$", table.toLowerCase(), true );
        String[] sql = parseSQLStatements( tmp );

        DBConnectionPool pool = DBConnectionPool.getInstance();
        Connection con = pool.acquireConnection( driver, url, user, pw );
        Statement stmt = con.createStatement();
        for ( int i = 0; i < sql.length; i++ ) {
            stmt.execute( sql[i] );
        }
        stmt.close();
        pool.releaseConnection( con, driver, url, user, pw );
        LOG.logInfo( "indexes created!" );

    }

    /**
     * fills the index table with informations about raster tiles, their spatial extent, assigened
     * level and assigned file
     *
     * @throws IOException
     * @throws DBaseException
     * @throws DBPoolException
     * @throws SQLException
     * @throws GeometryException
     * @throws SAXException
     */
    private void fillIndexTable()
                            throws IOException, DBaseException, DBPoolException, SQLException, GeometryException,
                            SAXException {
        DBConnectionPool pool = DBConnectionPool.getInstance();
        Connection con = pool.acquireConnection( driver, url, user, pw );

        ConvenienceFileFilter ff = new ConvenienceFileFilter( false, "SHP" );
        File dir = new File( rootDir );
        File[] files = dir.listFiles( ff );
        Map<File, PyramidHelper> map = createLevelMap( files );
        if ( !append ) {
            fillPyramidTable( map );
        }

        XMLFragment xml = new XMLFragment( configurationFile.toURI().toURL() );

        for ( int i = 0; i < files.length; i++ ) {
            LOG.logInfo( "import: ", files[i].getAbsolutePath() );
            String tmp = files[i].getAbsolutePath();
            tmp = tmp.substring( 0, tmp.length() - 4 );
            ShapeFileReader sfr = new ShapeFileReader( tmp );
            ShapeFile sf = sfr.read();
            FeatureCollection fc = sf.getFeatureCollection();
            for ( int j = 0; j < fc.size(); j++ ) {
                String sql = StringTools.concat( 200, "INSERT INTO ", table,
                                                 " (level, dir, file, bbox) values(?,?,?,?)" );
                PreparedStatement stmt = con.prepareStatement( sql );
                Object[] values = createInsert( fc.getFeature( j ) );
                stmt.setInt( 1, map.get( files[i] ).level );
                if ( switchDir ) {
                    String s = xml.resolve( (String) values[1] ).toExternalForm();
                    StringBuffer sb = new StringBuffer();
                    int k = 0;
                    int l = s.length() - 1;
                    while ( k != 2 ) {
                        sb.insert( 0, s.charAt( l ) );
                        if ( s.charAt( l ) == '/' ) {
                            k++;
                        }
                        l--;
                    }
                    stmt.setString( 2, sb.toString() );
                } else {
                    stmt.setString( 2, (String) values[1] );
                }
                stmt.setString( 3, (String) values[2] );
                stmt.setObject( 4, values[3] );
                stmt.execute();
                stmt.close();
            }

        }

        pool.releaseConnection( con, driver, url, user, pw );
        LOG.logInfo( "index table filled!" );

    }

    /**
     * creates pyramid level map; assigning each file to a level starting at 0
     *
     * @param files
     * @return the created pyramid level map
     */
    private Map<File, PyramidHelper> createLevelMap( File[] files ) {
        PyramidHelper[] phelper = new PyramidHelper[files.length];
        for ( int i = 0; i < files.length; i++ ) {
            String tmp = files[i].getName().substring( 2, files[i].getName().length() - 4 );
            float scale = Float.parseFloat( tmp );
            PyramidHelper helper = new PyramidHelper();
            helper.file = files[i];
            helper.scale = scale;
            phelper[i] = helper;
        }
        Arrays.sort( phelper );
        Map<File, PyramidHelper> map = new HashMap<File, PyramidHelper>();
        for ( int i = 0; i < phelper.length; i++ ) {
            phelper[i].level = i;
            if ( i == 0 ) {
                phelper[i].minscale = 0;
            } else {
                phelper[i].minscale = phelper[i - 1].scale;
            }
            if ( i == phelper.length - 1 ) {
                phelper[i].maxscale = 99999999999F;
            } else {
                phelper[i].maxscale = phelper[i].scale;
            }
            map.put( phelper[i].file, phelper[i] );
        }
        LOG.logInfo( "pyramid level assoziation:", map );
        return map;
    }

    /**
     *
     * @param feature
     * @return values to insert for a raster tile
     * @throws GeometryException
     */
    private Object[] createInsert( Feature feature )
                            throws GeometryException {
        Object[] values = new Object[4];
        values[1] = feature.getProperties( new QualifiedName( "FOLDER", app ) )[0].getValue();
        values[2] = feature.getProperties( new QualifiedName( "FILENAME", app ) )[0].getValue();
        Geometry geom = feature.getDefaultGeometryPropertyValue();
        values[3] = PGgeometryAdapter.export( geom, -1 );
        return values;
    }

    /**
     * creates data and pyramid table
     *
     * @throws IOException
     * @throws DBPoolException
     * @throws SQLException
     */
    private void createTables()
                            throws IOException, DBPoolException, SQLException {
        String template = null;
        if ( driver.toUpperCase().indexOf( "ORACLE" ) > -1 ) {
            template = createOrcl;
        } else if ( driver.toUpperCase().indexOf( "POSTGRES" ) > -1 ) {
            template = createPG;
        }
        StringBuffer sb = FileUtils.readTextFile( ImportIndexIntoDB.class.getResource( template ) );
        String tmp = StringTools.replace( sb.toString(), "$TABLE$", table.toLowerCase(), true );
        String[] sql = parseSQLStatements( tmp );

        DBConnectionPool pool = DBConnectionPool.getInstance();
        Connection con = pool.acquireConnection( driver, url, user, pw );
        Statement stmt = con.createStatement();
        for ( int i = 0; i < sql.length; i++ ) {
            try {
                stmt.execute( sql[i] );
            } catch ( Exception e ) {
                LOG.logWarning( e.getMessage() );
            }
        }
        stmt.close();
        pool.releaseConnection( con, driver, url, user, pw );
        LOG.logInfo( "tables created!" );

    }

    /**
     *
     * @param tmp
     * @return SQL statements parsed from a file/string
     */
    private String[] parseSQLStatements( String tmp ) {
        List<String> list = StringTools.toList( tmp, ";", false );
        String[] sql = new String[list.size()];
        for ( int i = 0; i < sql.length; i++ ) {
            sql[i] = list.get( i ).trim();
        }
        return sql;
    }

    /**
     * adapts the wcs configuration for a coverage to use the created db-based index instead of a
     * shapefile base one.
     *
     * @throws IOException
     * @throws SAXException
     * @throws TransformerException
     */
    private void adaptConfiguration()
                            throws IOException, SAXException, TransformerException {
        URL url = ImportIndexIntoDB.class.getResource( "wcsconfiguration.xsl" );
        XSLTDocument xslt = new XSLTDocument( url );
        XMLFragment xml = new XMLFragment( configurationFile.toURI().toURL() );
        xml = xslt.transform( xml );
        Map<String, String> params = new HashMap<String, String>();
        params.put( "TABLE", table );
        params.put( "USER", user );
        params.put( "PASSWORD", pw );
        params.put( "DRIVER", driver );
        params.put( "URL", this.url );
        xml = xslt.transform( xml, XMLFragment.DEFAULT_URL, null, params );

        String s = configurationFile.getAbsolutePath();
        if ( switchDir ) {
            s = configurationFile.getAbsolutePath();
            int kk = s.lastIndexOf( File.separator );
            s = s.substring( 0, kk );
            kk = s.lastIndexOf( File.separator );
            s = s.substring( 0, kk ) + File.separator + configurationFile.getName();
        }

        FileOutputStream fos = new FileOutputStream( s );
        xml.write( fos );
        fos.close();
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main( String[] args )
                            throws Exception {

        try {
            Properties map = new Properties();
            for ( int i = 0; i < args.length; ) {
                String first = args[i++];
                if ( "?".equals( first ) || "-h".equals( first ) || "-help".equals( first ) ) {
                    printHelp();
                    System.exit( 0 );
                }
                map.put( first, args[i++] );
            }

            // set up stderr/stdout redirection
            String redirect = map.getProperty( "-redirect" );
            if ( redirect != null && redirect.equals( "true" ) ) {
                String rootDir = map.getProperty( "-rootDir" );
                File f = new File( rootDir + separator + "dbindexer.log" );
                PrintStream out = new PrintStream( new FileOutputStream( f ) );
                System.setOut( out );
                System.setErr( out );
            }

            try {
                validate( map );
            } catch ( InvalidParameterException ipe ) {
                LOG.logError( ipe.getMessage() );
                printHelp();
                System.exit( 1 );
            }
            LOG.logDebug( "Resulting commandline arguments and their values {argument=value, ...}: " + map.toString() );

            String table = map.getProperty( "-table" );
            String user = map.getProperty( "-user" );
            String pw = map.getProperty( "-password" );
            String driver = map.getProperty( "-driver" );
            String url = map.getProperty( "-url" );
            String rootDir = map.getProperty( "-rootDir" );
            String tmp = map.getProperty( "-append" );
            boolean append = "true".equals( tmp );
            tmp = map.getProperty( "-switchDir" );
            boolean switchDir = "true".equals( tmp );

            ImportIndexIntoDB importer = new ImportIndexIntoDB( driver, url, user, pw, table, rootDir, append,
                                                                switchDir );
            importer.perform();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }
        System.out.println( "finished ...." );
        System.exit( 0 );
    }

    /**
     *
     * @param map
     * @throws InvalidParameterException
     */
    private static void validate( Properties map )
                            throws InvalidParameterException {
        if ( map.getProperty( "-table" ) == null ) {
            throw new InvalidParameterException( "-table must be set" );
        }
        if ( map.getProperty( "-user" ) == null ) {
            throw new InvalidParameterException( "-user must be set" );
        }
        if ( map.getProperty( "-password" ) == null ) {
            throw new InvalidParameterException( "-password must be set" );
        }
        if ( map.getProperty( "-driver" ) == null ) {
            throw new InvalidParameterException( "-driver must be set" );
        }
        if ( map.getProperty( "-url" ) == null ) {
            throw new InvalidParameterException( "-url must be set" );
        }
        if ( map.getProperty( "-rootDir" ) == null ) {
            throw new InvalidParameterException( "-rootDir must be set" );
        }

    }

    /**
     *
     *
     */
    private static void printHelp() {
        // TODO Auto-generated method stub
        System.out.println( "no help available at the moment" );
    }

    /**
     *
     *
     *
     * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
     * @author last edited by: $Author$
     *
     * @version $Revision$, $Date$
     */
    class PyramidHelper implements Comparable<PyramidHelper> {

        File file;

        float scale;

        float minscale;

        float maxscale;

        int level;

        public int compareTo( PyramidHelper o ) {
            if ( o.scale < scale ) {
                return 1;
            } else if ( o.scale > scale ) {
                return -1;
            }
            return 0;
        }

        @Override
        public String toString() {
            return StringTools.concat( 260, file.getName(), ' ', level, ' ', scale );
        }

    }

}
