//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/ogcwebservices/wmps/WMPServiceTest.java $
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

package org.deegree.ogcwebservices.wmps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ConvenienceFileFilter;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.DOMPrinter;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.ogcwebservices.wmps.operation.PrintMap;
import org.deegree.ogcwebservices.wmps.operation.PrintMapResponseDocument;
import org.deegree.ogcwebservices.wmps.operation.WMPSGetCapabilities;
import org.deegree.ogcwebservices.wmps.operation.WMPSGetCapabilitiesResult;

import alltests.AllTests;
import alltests.Configuration;

/**
 * Test class for WMPS Service
 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh</a>
 * @author last edited by: $Author: mschneider $
 *
 * @version 2.0, $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 */

public class WMPServiceTest extends TestCase {
    private static ILogger LOG = LoggerFactory.getLogger( WMPServiceTest.class );
    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp()
                            throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown()
                            throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for GetCoverageTest
     *
     * @param arg0
     */
    public WMPServiceTest( String arg0 ) {
        super( arg0 );
    }

    private WMPService createWMPS()
                            throws Exception {
        URL url = Configuration.getWMPSConfigurationURL();
        WMPServiceFactory.setConfiguration( url );
        WMPService service = WMPServiceFactory.getService();
        return service;
    }

    /**
     * reads a deegree WCS configuration file and performs a GetCapbilities request Steps:
     * <ul>
     * <li>read configuration file
     * <li>read a GetCapabilites request object
     * <li>perform the request
     * </ul>
     *
     */
    public void _testGetCapabilities() {
        try {
            WMPService service = createWMPS();

            Map<String, String> map = new HashMap<String, String>();
            map.put( "REQUEST", "GetCapabilities" );
            map.put( "VERSION", "1.0.0" );
            map.put( "SERVICE", "WMPS" );

            // StringBuffer sb = new StringBuffer();
            // sb.append( "http://127.0.0.1/deegreewmps/wmps?service=WMPS&" );
            // sb.append( "request=GetCapabilities&version=1.0.0" );

            WMPSGetCapabilities getCapa = WMPSGetCapabilities.create( map );
            Object o = service.doService( getCapa );
            LOG.logInfo( "------------------------" );
            if ( o instanceof WMPSGetCapabilitiesResult ) {
                WMPSGetCapabilitiesResult result = (WMPSGetCapabilitiesResult) o;
                LOG.logInfo( result.toString() );
            }
            LOG.logInfo( "------------------------" );
        } catch ( Exception e ) {
            LOG.logInfo( StringTools.stackTraceToString( e ) );
            fail( StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * reads a deegree WPS configuration file and performs a PrintMap request Steps:
     * <ul>
     * <li>read configuration file
     * <li>read a PrintMap request object
     * <li>perform the request
     * </ul>
     *
     * @throws Exception
     *
     */
    public void _testPrintMap()
                            throws Exception {

        WMPService service = createWMPS();
        URL directoryURL = new URL( Configuration.getWMPSBaseDir(), "example/deegree/requests/printmap/post" );
        URL[] fileURLs = scanDirectory( directoryURL );
        // HTTP-POST
        for ( int i = 0; i < fileURLs.length; i++ ) {
            LOG.logInfo( "Reading PrintMap example '" + fileURLs[i] + "'." );
            XMLFragment frag = new XMLFragment( fileURLs[i] );
            PrintMap printMap = PrintMap.create( frag.getRootElement() );
            Timestamp time = new Timestamp( System.currentTimeMillis() );
            LOG.logInfo( time.toString() );
            Object o = service.doService( printMap );
            assertNotNull( o );
            if ( o instanceof PrintMapResponseDocument ) {
                PrintMapResponseDocument document = (PrintMapResponseDocument) o;
                DOMPrinter.printNode( document.getRootElement(), "" );
            }
            time = new Timestamp( System.currentTimeMillis() );
            LOG.logInfo( time.toString() );
        }
        // HTTP-GET
        directoryURL = new URL( Configuration.getWMPSBaseDir(), "example/deegree/requests/printmap/get" );
        fileURLs = scanDirectory( directoryURL );
        for ( int i = 0; i < fileURLs.length; i++ ) {
            LOG.logInfo( "Reading PrintMap example '" + fileURLs[i] + "'." );
            String fileName = fileURLs[i].getFile();
            int indxOf = fileName.indexOf( "." );
            String ext = fileName.substring( indxOf + 1, fileName.length() );
            if ( ext.equalsIgnoreCase( "kvp" ) ) {
                Map<String, String> kvpMap = createKVPMap( new File( fileName ) );
                Timestamp time = new Timestamp( System.currentTimeMillis() );
                LOG.logInfo( time.toString() );
                PrintMap printMap = PrintMap.create( kvpMap );
                Object o = service.doService( printMap );
                assertNotNull( o );
                if ( o instanceof PrintMapResponseDocument ) {
                    PrintMapResponseDocument document = (PrintMapResponseDocument) o;
                    DOMPrinter.printNode( document.getRootElement(), "" );
                }
                time = new Timestamp( System.currentTimeMillis() );
                LOG.logInfo( time.toString() );
            }
        }
    }

    private Map<String, String> createKVPMap( File file )
                            throws IOException {
        Map<String, String> kvpMap = new HashMap<String, String>();
        BufferedReader reader = new BufferedReader( new FileReader( file ) );
        // skip first line
        String line = null;
        while ( ( line = reader.readLine() ) != null ) {
            return toMap( line );
        }
        return kvpMap;
    }

    private Map<String, String> toMap( String kvp ) {

        StringTokenizer st = new StringTokenizer( kvp.trim(), "&?" );
        HashMap<String, String> map = new HashMap<String, String>();

        while ( st.hasMoreTokens() ) {
            String s = st.nextToken();
            if ( s != null ) {
                int pos = s.indexOf( '=' );

                if ( pos > -1 ) {
                    String s1 = s.substring( 0, pos );
                    String s2 = s.substring( pos + 1, s.length() );
                    map.put( s1.toUpperCase(), s2 );
                }
            }
        }

        return map;

    }

    private URL[] scanDirectory( URL directoryURL )
                            throws MalformedURLException {
        File directory = new File( directoryURL.getFile() );
        URL[] fileURLs = new URL[0];
        String[] fileNames = directory.list( new ConvenienceFileFilter( false, "xml,kvp" ) );

        if ( fileNames == null ) {
            LOG.logDebug( "Specified directory '" + directory.toString() + "' does not exist." );
            fileNames = new String[0];
        } else {
            fileURLs = new URL[fileNames.length];
            for ( int i = 0; i < fileNames.length; i++ ) {
                fileURLs[i] = new URL( directoryURL.toString() + "/" + fileNames[i] );
            }
        }
        return fileURLs;
    }

    public void testDummy() {

    }

}
