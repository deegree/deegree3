//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/ogcwebservices/wfs/operation/WFSOperationTest.java $
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
package org.deegree.ogcwebservices.wfs.operation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ConvenienceFileFilter;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.io.datastore.TransactionException;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCRequestFactory;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.operation.transaction.Transaction;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import alltests.Configuration;

/**
 * Tests the request-examples from the
 * <code>Web Feature Service Implementation Specification 1.1.0</code> (OGC 04-094).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author: mschneider $
 *
 * @version $Revision: 18522 $, $Date: 2009-07-17 17:45:49 +0200 (Fr, 17 Jul 2009) $
 *
 */
public class WFSOperationTest extends TestCase {

    private static ILogger LOG = LoggerFactory.getLogger( WFSOperationTest.class );

    public static final String REQUEST_DIR = "example/ogc/requests/";

    public static final String DEEGREE_REQUEST_DIR = "example/philosopher/requests/";

    /*
     * @see TestCase#setUp()
     */
    protected void setUp()
                            throws Exception {
        super.setUp();

    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown()
                            throws Exception {
        super.tearDown();
    }

    private Map<String, String> createKVPMap( File file )
                            throws IOException {
        Map<String, String> kvpMap = new HashMap<String, String>();
        BufferedReader reader = new BufferedReader( new FileReader( file ) );
        // skip first line
        String line = reader.readLine();
        while ( ( line = reader.readLine() ) != null ) {
            int equalsPos = line.indexOf( '=' );
            String key = line.substring( 0, equalsPos );
            String value = line.substring( equalsPos + 1 );
            if ( value.endsWith( "&" ) ) {
                value = value.substring( 0, value.length() - 1 );
            }
            kvpMap.put( key, value );
        }
        return kvpMap;
    }

    public void testKVPDescribeFeatureTypeExamples()
                            throws FileNotFoundException, IOException, InvalidParameterValueException,
                            OGCWebServiceException {
        File filterbase = new File(
                                    new URL( Configuration.getWFSBaseDir(), REQUEST_DIR + "DescribeFeatureType/kvp" ).getFile() );
        ConvenienceFileFilter xmlFilter = new ConvenienceFileFilter( false, "kvp" );
        File[] fileList = filterbase.listFiles( xmlFilter );
        for ( int i = 0; i < fileList.length; i++ ) {
            LOG.logInfo( "Parsing KVP encoded example DescribeFeatureType request: " + fileList[i] );
            Map<String, String> kvpMap = createKVPMap( fileList[i] );
            DescribeFeatureType request = (DescribeFeatureType) OGCRequestFactory.createFromKVP( kvpMap );
            assertNotNull( request );
        }
    }

    public void testXMLDescribeFeatureTypeExamples()
                            throws FileNotFoundException, IOException, SAXException, MissingParameterValueException,
                            InvalidParameterValueException, OGCWebServiceException {
        File filterbase = new File(
                                    new URL( Configuration.getWFSBaseDir(), REQUEST_DIR + "DescribeFeatureType/xml" ).getFile() );
        ConvenienceFileFilter xmlFilter = new ConvenienceFileFilter( false, "xml" );
        File[] fileList = filterbase.listFiles( xmlFilter );
        for ( int i = 0; i < fileList.length; i++ ) {
            LOG.logInfo( "Parsing XML encoded example DescribeFeatureType request: " + fileList[i] );
            Document doc = XMLTools.parse( new FileReader( fileList[i] ) );
            DescribeFeatureType request = DescribeFeatureType.create( "" + i, doc.getDocumentElement() );
            assertNotNull( request );
        }
    }

    public void testKVPGetCapabilitiesExamples()
                            throws FileNotFoundException, IOException, MissingParameterValueException,
                            InvalidParameterValueException, OGCWebServiceException {
        File filterbase = new File(
                                    new URL( Configuration.getWFSBaseDir(), REQUEST_DIR + "GetCapabilities/kvp" ).getFile() );
        ConvenienceFileFilter xmlFilter = new ConvenienceFileFilter( false, "kvp" );
        File[] fileList = filterbase.listFiles( xmlFilter );
        for ( int i = 0; i < fileList.length; i++ ) {
            LOG.logInfo( "Parsing KVP encoded example GetCapabilities request: " + fileList[i] );
            Map<String, String> kvpMap = createKVPMap( fileList[i] );
            WFSGetCapabilities request = (WFSGetCapabilities) OGCRequestFactory.createFromKVP( kvpMap );
            assertNotNull( request );
        }
    }

    public void testXMLGetCapabilitiesExamples()
                            throws FileNotFoundException, IOException, SAXException, MissingParameterValueException,
                            InvalidParameterValueException, OGCWebServiceException {
        File filterbase = new File(
                                    new URL( Configuration.getWFSBaseDir(), REQUEST_DIR + "GetCapabilities/xml" ).getFile() );
        ConvenienceFileFilter xmlFilter = new ConvenienceFileFilter( false, "xml" );
        File[] fileList = filterbase.listFiles( xmlFilter );
        for ( int i = 0; i < fileList.length; i++ ) {
            LOG.logInfo( "Parsing XML encoded example GetCapabilities request: " + fileList[i] );
            Document doc = XMLTools.parse( new FileReader( fileList[i] ) );
            WFSGetCapabilities request = WFSGetCapabilities.create( "" + i, doc.getDocumentElement() );
            assertNotNull( request );
        }
    }

    public void testKVPGetFeatureExamples()
                            throws FileNotFoundException, IOException, InvalidParameterValueException,
                            OGCWebServiceException {
        File filterbase = new File( new URL( Configuration.getWFSBaseDir(), REQUEST_DIR + "GetFeature/kvp" ).getFile() );
        ConvenienceFileFilter xmlFilter = new ConvenienceFileFilter( false, "kvp" );
        File[] fileList = filterbase.listFiles( xmlFilter );
        for ( int i = 0; i < fileList.length; i++ ) {
            LOG.logInfo( "Parsing KVP encoded example GetFeature request: " + fileList[i] );
            Map<String, String> kvpMap = createKVPMap( fileList[i] );
            GetFeature request = (GetFeature) OGCRequestFactory.createFromKVP( kvpMap );
            assertNotNull( request );
        }
    }

    public void testXMLGetFeatureExamples()
                            throws FileNotFoundException, IOException, SAXException, MissingParameterValueException,
                            InvalidParameterValueException, OGCWebServiceException {
        File filterbase = new File( new URL( Configuration.getWFSBaseDir(), REQUEST_DIR + "GetFeature/xml" ).getFile() );
        ConvenienceFileFilter xmlFilter = new ConvenienceFileFilter( false, "xml" );
        File[] fileList = filterbase.listFiles( xmlFilter );
        for ( int i = 0; i < fileList.length; i++ ) {
            LOG.logInfo( "Parsing XML encoded example GetFeature request: " + fileList[i] );
            Document doc = XMLTools.parse( new FileReader( fileList[i] ) );
            GetFeature request = GetFeature.create( "" + i, doc.getDocumentElement() );
            assertNotNull( request );
        }
    }

    // public void testXMLGetGmlObjectExamples()
    // throws FileNotFoundException,
    // IOException,
    // SAXException,
    // MissingParameterValueException,
    // InvalidParameterValueException,
    // OGCWebServiceException {
    // File filterbase = new File( new URL( Configuration.getWFSBaseDir(), REQUEST_DIR
    // + "GetGmlObject" ).getFile() );
    // FileFilter xmlFilter = new ExtensionFilter( "xml" );
    // File[] fileList = filterbase.listFiles( xmlFilter );
    // for (int i = 0; i < fileList.length; i++) {
    // LOG.logInfo( "Parsing XML encoded example GetGmlObject request: "
    // + fileList[i] );
    // Document doc = XMLTools.parse( new FileReader( fileList[i] ) );
    // GetGmlObject request = GetGmlObject.create( ""
    // + i, doc.getDocumentElement() );
    // }
    // }

    // public void testXMLLockFeatureExamples()
    // throws FileNotFoundException,
    // IOException,
    // SAXException,
    // MissingParameterValueException,
    // InvalidParameterValueException,
    // OGCWebServiceException {
    // File filterbase = new File( new URL( Configuration.getWFSBaseDir(), REQUEST_DIR
    // + "LockFeature" ).getFile() );
    // FileFilter xmlFilter = new ExtensionFilter( "xml" );
    // File[] fileList = filterbase.listFiles( xmlFilter );
    // for (int i = 0; i < fileList.length; i++) {
    // LOG.logInfo( "Parsing XML encoded example LockFeature request: "
    // + fileList[i] );
    // Document doc = XMLTools.parse( new FileReader( fileList[i] ) );
    // LockFeature request = LockFeature.create( ""
    // + i, doc.getDocumentElement() );
    // }
    // }

    public void testKVPTransactionExamples()
                            throws FileNotFoundException, IOException, InvalidParameterValueException,
                            OGCWebServiceException {
        File filterbase = new File( new URL( Configuration.getWFSBaseDir(), REQUEST_DIR + "Transaction/kvp" ).getFile() );
        ConvenienceFileFilter xmlFilter = new ConvenienceFileFilter( false, "kvp" );
        File[] fileList = filterbase.listFiles( xmlFilter );
        for ( int i = 0; i < fileList.length; i++ ) {
            LOG.logInfo( "Parsing KVP encoded example Transaction request: " + fileList[i] );
            Map<String, String> kvpMap = createKVPMap( fileList[i] );
            Transaction request = (Transaction) OGCRequestFactory.createFromKVP( kvpMap );
            assertNotNull( request );
        }
    }

//    public void testXMLTransactionExamples()
//                            throws FileNotFoundException, IOException, SAXException, MissingParameterValueException,
//                            InvalidParameterValueException, OGCWebServiceException, XMLParsingException,
//                            TransactionException {
//        File filterbase = new File( new URL( Configuration.getWFSBaseDir(), REQUEST_DIR + "Transaction/xml" ).getFile() );
//        ConvenienceFileFilter xmlFilter = new ConvenienceFileFilter( false, "xml" );
//        File[] fileList = filterbase.listFiles( xmlFilter );
//        for ( int i = 0; i < fileList.length; i++ ) {
//            LOG.logInfo( "Parsing XML encoded example Transaction request: " + fileList[i] );
//            Document doc = XMLTools.parse( new FileReader( fileList[i] ) );
//            Transaction request = Transaction.create( "" + i, doc.getDocumentElement() );
//            assertNotNull( request );
//            assertEquals( "WFS", request.getServiceName() );
//        }
//    }

    public void testKVPLockFeatureExamples()
                            throws FileNotFoundException, IOException, InvalidParameterValueException,
                            OGCWebServiceException {
        File filterbase = new File( new URL( Configuration.getWFSBaseDir(), REQUEST_DIR + "LockFeature/kvp" ).getFile() );
        ConvenienceFileFilter xmlFilter = new ConvenienceFileFilter( false, "kvp" );
        File[] fileList = filterbase.listFiles( xmlFilter );
        for ( int i = 0; i < fileList.length; i++ ) {
            LOG.logInfo( "Parsing KVP encoded example LockFeature request: " + fileList[i] );
            Map<String, String> kvpMap = createKVPMap( fileList[i] );
            LockFeature request = (LockFeature) OGCRequestFactory.createFromKVP( kvpMap );
            assertNotNull( request );
        }
    }

    public void testXMLLockFeatureExamples()
                            throws FileNotFoundException, IOException, SAXException, MissingParameterValueException,
                            InvalidParameterValueException, OGCWebServiceException {
        File filterbase = new File( new URL( Configuration.getWFSBaseDir(), REQUEST_DIR + "LockFeature/xml" ).getFile() );
        ConvenienceFileFilter xmlFilter = new ConvenienceFileFilter( false, "xml" );
        File[] fileList = filterbase.listFiles( xmlFilter );
        for ( int i = 0; i < fileList.length; i++ ) {
            LOG.logInfo( "Parsing XML encoded example LockFeature request: " + fileList[i] );
            Document doc = XMLTools.parse( new FileReader( fileList[i] ) );
            LockFeature request = LockFeature.create( "" + i, doc.getDocumentElement() );
            assertNotNull( request );
        }
    }

}
