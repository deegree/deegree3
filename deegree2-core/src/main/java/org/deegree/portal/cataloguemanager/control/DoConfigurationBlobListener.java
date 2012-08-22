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
package org.deegree.portal.cataloguemanager.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.FileUtils;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.io.DBConnectionPool;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DoConfigurationBlobListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( DoConfigurationListener.class );

    private static final String[] sqlOracle = new String[] { "oracle/create_blob_searchTables.sql" };

    private static final String[] sqlPostgis = new String[] { "postgis/drop_blob_searchTables.sql",
                                                             "postgis/create_blob_searchTables.sql" };

    private XMLFragment xml = null;

    private static NamespaceContext nsc = null;
    static {
        if ( nsc == null ) {
            nsc = CommonNamespaces.getNamespaceContext();
            nsc.addNamespace( "md", URI.create( "http://www.deegree.org/cataloguemanager" ) );
        }
    }

    private WebEvent event;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    public void actionPerformed( WebEvent event, ResponseHandler resp )
                            throws IOException {
        this.event = event;
        String cswurl = (String) event.getParameter().get( "cswurl" );
        String url = (String) event.getParameter().get( "url" );
        String db = (String) event.getParameter().get( "db" );
        String user = (String) event.getParameter().get( "user" );
        String password = (String) event.getParameter().get( "pw" );
        String sid = (String) event.getParameter().get( "sid" );
        String driver = null;
        String database = null;
        String[] sql = null;
        if ( db.equalsIgnoreCase( "postgres" ) ) {
            driver = "org.postgresql.Driver";
            database = "jdbc:postgresql://" + url + '/' + sid;
            sql = sqlPostgis;
        } else if ( db.equalsIgnoreCase( "oracle" ) ) {
            driver = "oracle.jdbc.OracleDriver";
            database = "jdbc:oracle:thin:@" + url + ':' + sid;
            sql = sqlOracle;
        } else {
            resp.writeAndClose( "ERROR: not supported database type" );
            return;
        }
        boolean newTables = (Boolean) event.getParameter().get( "newTables" );
        boolean transactions = (Boolean) event.getParameter().get( "transactions" );
        boolean searchClient = (Boolean) event.getParameter().get( "searchClient" );
        boolean editor = (Boolean) event.getParameter().get( "editor" );
        if ( newTables ) {
            try {
                createTables( driver, database, user, password, sql );
            } catch ( Exception e ) {
                resp.writeAndClose( "ERROR: can not create database: " + e.getMessage() );
                return;
            }
        }

        xml = new XMLFragment();
        try {
            xml.load( new File( event.getAbsolutePath( "./WEB-INF/web.xml" ) ).toURI().toURL() );
        } catch ( SAXException e ) {
            // should never happen
            e.printStackTrace();
        }

        try {
            doCSWConfiguration( driver, database, user, password, cswurl, db, transactions );
        } catch ( Exception e ) {
            resp.writeAndClose( "ERROR: can not do CSW configuration: " + e.getMessage() );
            return;
        }

        doSearchClientConfiguration( searchClient, cswurl );

        doMetadataEditorConfiguration( cswurl );

        markAsConfigured( searchClient, editor );

        resp.writeAndClose( "success" );
    }

    /**
     * 
     * @param searchClient
     * @param editor
     * @throws IOException
     */
    private void markAsConfigured( boolean searchClient, boolean editor )
                            throws IOException {
        String s = event.getAbsolutePath( "WEB-INF/conf/setup/catalogueManager_config.properties" );
        Properties p = new Properties();
        FileInputStream fis = new FileInputStream( s );
        p.load( fis );
        fis.close();
        p.put( "configured", "true" );
        p.put( "searchClient", "" + searchClient );
        p.put( "editor", "" + editor );
        FileOutputStream fos = new FileOutputStream( s );
        p.store( fos, null );
        fos.close();

    }

    /**
     * @param searchClient
     * @param cswurl
     */
    private void doSearchClientConfiguration( boolean searchClient, String cswurl ) {
        // TODO Auto-generated method stub

    }

    /**
     * @param cswurl
     * @throws IOException
     */
    private void doMetadataEditorConfiguration( String cswurl )
                            throws IOException {
        String s = event.getAbsolutePath( "WEB-INF/conf/cataloguemanager/cataloguemanager.xml" );
        XMLFragment cm = new XMLFragment();
        try {
            cm.load( new File( s ).toURI().toURL() );
            String xpath = "md:CatalogueService/md:onlineResource";
            Element elem = XMLTools.getRequiredElement( cm.getRootElement(), xpath, nsc );
            elem.setAttribute( "xlink:href", cswurl );
            //XMLTools.setNodeValue( elem, cswurl );
            FileUtils.writeToFile( s, cm.getAsPrettyString() );
        } catch ( Exception e ) {
            // should never happen
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
    }

    /**
     * 
     * @param driver
     * @param url
     * @param user
     * @param password
     * @param cswurl
     * @param db
     * @param transactions
     * @throws XMLParsingException
     */
    private void doCSWConfiguration( String driver, String url, String user, String password, String cswurl, String db,
                                     boolean transactions )
                            throws Exception {
        // update CSW capabilities/configuration with desired URL
        String xpath = "./servlet[servlet-name ='owservice']/init-param[param-name ='csw.config']/param-value";
        String configFile = XMLTools.getRequiredNodeAsString( xml.getRootElement(), xpath, nsc );
        configFile = event.getAbsolutePath( configFile );
        XMLFragment csw = new XMLFragment( new File( configFile ).toURI().toURL() );
        xpath = "ows:OperationsMetadata/ows:Operation/ows:DCP/ows:HTTP/ows:Get";
        List<Element> elements = XMLTools.getElements( csw.getRootElement(), xpath, nsc );
        for ( Element element : elements ) {
            element.setAttributeNS( CommonNamespaces.XLNNS.toASCIIString(), "xlink:href", cswurl );
        }
        xpath = "ows:OperationsMetadata/ows:Operation/ows:DCP/ows:HTTP/ows:Post";
        elements = XMLTools.getElements( csw.getRootElement(), xpath, nsc );
        for ( Element element : elements ) {
            element.setAttributeNS( CommonNamespaces.XLNNS.toASCIIString(), "xlink:href", cswurl );
        }

        // delete already existing feature type configurations
        FileUtils.writeToFile( configFile, csw.getAsPrettyString(), Charset.defaultCharset().displayName() );
        String ftFile = "WEB-INF/conf/csw/featuretypes/csw_blob_postgres.xsd";
        ftFile = event.getAbsolutePath( ftFile );
        File tmp = new File( ftFile );
        if ( tmp.exists() ) {
            tmp.delete();
        }
        ftFile = "WEB-INF/conf/csw/featuretypes/csw_blob_oracle.xsd";
        ftFile = event.getAbsolutePath( ftFile );
        tmp = new File( ftFile );
        if ( tmp.exists() ) {
            tmp.delete();
        }

        // set database connection informations and feature type configurations
        if ( db.equalsIgnoreCase( "postgres" ) ) {
            ftFile = "WEB-INF/conf/csw/featuretypes/csw_blob_postgres.xsd.ignore";
        } else if ( db.equalsIgnoreCase( "oracle" ) ) {
            ftFile = "WEB-INF/conf/csw/featuretypes/csw_blob_oracle.xsd.ignore";
        }

        ftFile = event.getAbsolutePath( ftFile );
        XMLFragment ftXML = new XMLFragment( new File( ftFile ).toURI().toURL() );
        xpath = "xs:annotation/xs:appinfo/dgjdbc:JDBCConnection/dgjdbc:Driver";
        Element element = (Element) XMLTools.getRequiredNode( ftXML.getRootElement(), xpath, nsc );
        XMLTools.setNodeValue( element, driver );
        xpath = "xs:annotation/xs:appinfo/dgjdbc:JDBCConnection/dgjdbc:Url";
        element = (Element) XMLTools.getRequiredNode( ftXML.getRootElement(), xpath, nsc );
        XMLTools.setNodeValue( element, url );
        xpath = "xs:annotation/xs:appinfo/dgjdbc:JDBCConnection/dgjdbc:User";
        element = (Element) XMLTools.getRequiredNode( ftXML.getRootElement(), xpath, nsc );
        XMLTools.setNodeValue( element, user );
        xpath = "xs:annotation/xs:appinfo/dgjdbc:JDBCConnection/dgjdbc:Password";
        element = (Element) XMLTools.getRequiredNode( ftXML.getRootElement(), xpath, nsc );
        XMLTools.setNodeValue( element, password );
        FileUtils.writeToFile( ftFile.substring( 0, ftFile.length() - 7 ), ftXML.getAsPrettyString(),
                               Charset.defaultCharset().displayName() );

        adaptProperties( driver, url, user, password, cswurl );

    }

    /**
     * @param driver
     * @param url
     * @param user
     * @param password
     * @param cswurl
     * @throws IOException
     */
    private void adaptProperties( String driver, String url, String user, String password, String cswurl )
                            throws IOException {
        File file = new File(
                              event.getAbsolutePath( "./WEB-INF/classes/org/deegree/ogcwebservices/csw/csw202.properties" ) );
        try {
            FileInputStream fis = new FileInputStream( file );
            Properties prop = new Properties();
            prop.load( fis );
            fis.close();

            prop.put( "db.driver", driver );
            prop.put( "db.url", url );
            prop.put( "db.user", user );
            prop.put( "db.password", password );
            prop.put( "csw.url", cswurl );

            FileOutputStream fos = new FileOutputStream( file );
            prop.store( fos, null );
            fos.close();
        } catch ( Exception e ) {
            LOG.logWarning( file + ": " + e.getMessage() );
        }
        file = new File( event.getAbsolutePath( "./WEB-INF/classes/harvestrepository.properties" ) );
        try {
            FileInputStream fis = new FileInputStream( file );
            Properties prop = new Properties();
            prop.load( fis );
            fis.close();

            prop.put( "harvester.Driver", driver );
            prop.put( "harvester.Url", url );
            prop.put( "harvester.User", user );
            prop.put( "harvester.Password", password );

            FileOutputStream fos = new FileOutputStream( file );
            prop.store( fos, null );
            fos.close();
        } catch ( Exception e ) {
            LOG.logWarning( file + ": " + e.getMessage() );
        }

    }

    private void createTables( String driver, String url, String user, String password, String[] sql )
                            throws Exception {

        for ( String script : sql ) {
            BufferedReader br = null;
            try {
                String s = event.getAbsolutePath( "WEB-INF/scripts/sql/" + script );
                LOG.logDebug( "load ", s );
                br = new BufferedReader( new FileReader( s ) );
            } catch ( Exception e ) {
                throw new Exception( "can not open database creation script: " + script );
            }
            String thisLine, sqlQuery;
            Statement stmt = null;
            DBConnectionPool pool = DBConnectionPool.getInstance();
            Connection conn = null;
            try {
                conn = pool.acquireConnection( driver, url, user, password );
                stmt = conn.createStatement();
                sqlQuery = "";
                while ( ( thisLine = br.readLine() ) != null ) {

                    // Skip comments and empty lines
                    if ( thisLine.length() > 0 && thisLine.charAt( 0 ) == '-' || thisLine.length() == 0 ) {
                        continue;
                    }
                    sqlQuery = sqlQuery + " " + thisLine.trim();
                    // If one command complete
                    if ( sqlQuery.charAt( sqlQuery.length() - 1 ) == ';' ) {
                        sqlQuery = sqlQuery.replace( ';', ' ' ); // Remove the ; since jdbc complains
                        LOG.logDebug( "execute ", sqlQuery );
                        try {
                            stmt.execute( sqlQuery );
                        } catch ( Exception ex ) {
                            LOG.logInfo( "executed: ", sqlQuery );
                            LOG.logError( ex.getMessage() );
                        }
                        sqlQuery = "";
                    }
                }
            } catch ( Exception ex ) {
                br.close();
                throw ex;
            } finally {
                try {
                    if ( conn != null ) {
                        pool.releaseConnection( conn, driver, url, user, password );
                    }
                } catch ( Exception e ) {
                    // do nothing
                }
            }
            br.close();
        }

    }

}
