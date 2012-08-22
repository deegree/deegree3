//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/enterprise/servlet/OGCServletControllerTest.java $
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
package org.deegree.enterprise.servlet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import junit.framework.TestCase;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import alltests.Configuration;

/**
 * OGCServletControllerTest requires a running Servlet Container. The server name and port is specified in the
 * <code>Configuration</code>.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 *
 * @author last edited by: $Author: mschneider $
 *
 * @version 2.0, $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 *
 * @since 2.0
 *
 * @see alltests.Configuration#PROTOCOL
 * @see alltests.Configuration#HOST
 * @see alltests.Configuration#PORT
 */
public class OGCServletControllerTest extends TestCase {
    private static ILogger LOG = LoggerFactory.getLogger( OGCServletControllerTest.class );

    private final static String cswCapabilitiesRequest = "?REQUEST=GetCapabilities&service=CSW&version=2.0.0&acceptversion=2.0.0&outputFormat=text/xml";

    private final static String wfsCapabilitiesRequest = "?REQUEST=GetCapabilities&service=WFS&version=1.1.0";

    /**
     * Send WFS getCapabilities request to service.
     *
     * @throws Exception
     *
     */
    public void testWFSGetCapabilities()
                            throws Exception {
        // fail( "Not testing Get-Method because of localhost -- fixme" );
        URL wfsUrl = new URL( Configuration.getWFSURL() + wfsCapabilitiesRequest );
        LOG.logInfo( "GET: " + wfsUrl.toString() );
        WFSCapabilitiesDocument capaDoc = new WFSCapabilitiesDocument();
        capaDoc.load( wfsUrl );
        WFSCapabilities wfsCapabilities = (WFSCapabilities) capaDoc.parseCapabilities();
        assertNotNull( wfsCapabilities );
        assertNotNull( wfsCapabilities.getVersion() );
        assertEquals( "Capabilities Version is wrong", "1.1.0", wfsCapabilities.getVersion() );
        // assertNotNull( "Service name is null", wfsCapabilities.getServiceIdentification().getName() );

        StringWriter sw = new StringWriter();
        capaDoc.prettyPrint( sw );
        assertTrue( sw.getBuffer().length() > 0 );

        LOG.logDebug( "XML : " + sw.getBuffer() );

        Document capabilitiesDoc = XMLTools.parse( new StringReader( sw.getBuffer().toString() ) );

        assertNotNull( "Document is null", capabilitiesDoc );

        assertEquals( "Root element is wrong", "WFS_Capabilities", capabilitiesDoc.getDocumentElement().getLocalName() );

        assertEquals( "Root element is wrong", "1.1.0", capabilitiesDoc.getDocumentElement().getAttribute( "version" ) );
    }

    /**
     * Send CSW getCapabilities request to server.
     *
     * @throws Exception
     *
     */
    public void testCSWGetCapabilities()
                            throws Exception {
        URL cswUrl = new URL( Configuration.getCSWURL() + cswCapabilitiesRequest );
        LOG.logInfo( "GET: " + cswUrl.toString() );
        CatalogueCapabilities cswCapabilities = (CatalogueCapabilities) CatalogueCapabilities.createCapabilities( cswUrl );
        assertNotNull( cswCapabilities );
        LOG.logDebug( cswCapabilities.toString() );
        assertNotNull( cswCapabilities.getVersion() );
        assertEquals( "2.0.0", cswCapabilities.getVersion() );
        assertNotNull( cswCapabilities.getServiceIdentification().getServiceType().getCode() );
        assertEquals( "CSW", cswCapabilities.getServiceIdentification().getServiceType().getCode() );
    }

    /**
     * Reads operations from xml files and perform request on WFS servlet. All request files are located int the
     * resource directory.
     *
     * @throws MalformedURLException
     *
     */
    public void testRequestList()
                            throws MalformedURLException {
        URL requestsDir = new URL( Configuration.getWFSBaseDir(), "example/philosopher/requests/GetFeature/xml/" );
        final String generatedDir = Configuration.getWFSBaseDir().getFile() + Configuration.GENERATED_DIR + "/";
        LOG.logInfo( "wfs: " + requestsDir );

        File filterbase = new File( requestsDir.getFile() );
        // FileFilter xmlFilter = new XMLRequestFilter();
        File[] filelist = filterbase.listFiles();
        if ( filelist != null ) {
            for ( File f : filelist ) {
                if ( f != null && f.getName().trim().toLowerCase().endsWith( ".xml" ) ) {
                    try {
                        // get connection
                        URLConnection connection = this.openConnection( Configuration.getWFSURL() );

                        // sends request
                        this.sendRequest( connection, this.read( new FileReader( f ) ) );

                        // reads response
                        String responsecontent = this.read( new InputStreamReader( connection.getInputStream() ) );
                        LOG.logInfo( "Receiving response:" + responsecontent );
                        assertNotNull( "Response is null", responsecontent );
                        assertTrue( "Response is empty", responsecontent.length() > 0 );

                        // write response to file
                        System.out.println( f.getName() );
                        System.out.println( generatedDir );
                        this.write( new FileWriter( new File( generatedDir, "response_" + f.getName() ) ),
                                    responsecontent );

                        // compare responses
                        Document responseWfs = XMLTools.parse( new StringReader( responsecontent.toString() ) );
                        assertNotNull( "Root node is null", responseWfs.getDocumentElement() );
                        LOG.logDebug( responseWfs.getDocumentElement().toString() );
                        // test for exceptions
                        NodeList exceptionNodes = responseWfs.getElementsByTagName( "Exception" );
                        for ( int j = 0; j < exceptionNodes.getLength(); j++ ) {
                            Node exceptionNode = exceptionNodes.item( j );
                            LOG.logInfo( exceptionNode.toString() );
                            LOG.logInfo( exceptionNode.getFirstChild().getNodeValue() );
                            fail( "Exception is thrown by service" + exceptionNode.toString() );
                        }
                        // TODO: do more asserts
                        //

                    } catch ( FileNotFoundException e ) {
                        LOG.logError( e.getMessage(), e );
                        fail( e.getMessage() );
                    } catch ( IOException e ) {
                        LOG.logError( e.getMessage(), e );
                        fail( e.getMessage() );
                    } catch ( SAXException e ) {
                        LOG.logError( e.getMessage(), e );
                        fail( e.getMessage() );
                    }
                }
            }
        }
    }

    private void sendRequest( final URLConnection connection, String request )
                            throws IOException {
        this.write( new BufferedWriter( new OutputStreamWriter( connection.getOutputStream() ) ), request );
    }

    private URLConnection openConnection( URL url )
                            throws IOException {
        URLConnection connection = url.openConnection();
        LOG.logInfo( "Connection opened to " + url.toString() );
        connection.setDoOutput( true );
        connection.setDoInput( true );
        connection.connect();
        return connection;

    }

    private String read( Reader reader )
                            throws IOException {
        StringBuffer buffer = new StringBuffer();
        BufferedReader bufferedreader = new BufferedReader( reader );
        String line;
        while ( ( line = bufferedreader.readLine() ) != null ) {
            buffer.append( line );
        }
        reader.close();
        return buffer.toString();
    }

    private void write( Writer writer, String content )
                            throws IOException {
        writer.write( content );
        writer.flush();
        writer.close();
    }

    // /**
    // *
    // * XMLRequestFilter
    // *
    // * @author last edited by: $Author: mschneider $
    // *
    // * @version 2.0, $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
    // *
    // * @since 2.0
    // */
    // class XMLRequestFilter extends javax.swing.filechooser.FileFilter implements FileFilter {
    //
    // public boolean accept( File f ) {
    // return this.accept( f, f.getName() );
    // }
    //
    // /**
    // * This is the one of the methods that is declared in the abstract class
    // */
    // public boolean accept( File f, String name ) {
    // // if it is a directory -- we want to hide it so return false.
    // if ( f.isDirectory() )
    // return false;
    //
    // // get the extension of the file
    //
    // String extension = getExtension( f );
    // // check to see if the extension is equal to "xml"
    // if ( extension.equalsIgnoreCase( "xml" ) && ( f.getName().indexOf( "request" ) > 0 ) )
    // return true;
    //
    // // default -- fall through. False is return on all
    // // occasions except:
    // // a) the file is a directory
    // // b) the file's extension is what we are looking for.
    // return false;
    // }
    //
    // /**
    // * Again, this is declared in the abstract class
    // *
    // * The description of this filter
    // */
    // public String getDescription() {
    // return "XML files";
    // }
    //
    // /**
    // * Method to get the extension of the file, in lowercase
    // */
    // private String getExtension( File f ) {
    // String s = f.getName();
    // int i = s.lastIndexOf( '.' );
    // if ( i > 0 && i < s.length() - 1 )
    // return s.substring( i + 1 ).toLowerCase();
    // return "";
    // }
    // }

}
