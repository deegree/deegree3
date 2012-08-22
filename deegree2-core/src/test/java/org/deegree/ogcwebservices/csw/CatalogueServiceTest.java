//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/ogcwebservices/csw/CatalogueServiceTest.java $
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
package org.deegree.ogcwebservices.csw;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.InvalidConfigurationException;
import org.deegree.ogcwebservices.AbstractServiceTest;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueCapabilities;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueCapabilitiesDocument;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueGetCapabilities;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueGetCapabilitiesDocument;
import org.deegree.ogcwebservices.csw.configuration.CatalogueConfiguration;
import org.deegree.ogcwebservices.csw.configuration.CatalogueConfigurationDocument;
import org.deegree.ogcwebservices.csw.discovery.DescribeRecord;
import org.deegree.ogcwebservices.csw.discovery.DescribeRecordDocument;
import org.deegree.ogcwebservices.csw.discovery.DescribeRecordResult;
import org.deegree.ogcwebservices.csw.discovery.DescribeRecordResultDocument;
import org.deegree.ogcwebservices.csw.discovery.XMLFactory;
import org.deegree.ogcwebservices.csw.manager.Transaction;
import org.deegree.ogcwebservices.csw.manager.TransactionDocument;
import org.deegree.ogcwebservices.csw.manager.TransactionResult;
import org.deegree.ogcwebservices.csw.manager.TransactionResultDocument;
import org.xml.sax.SAXException;

import alltests.Configuration;

/**
 * Sets up an example CSW instance (DublinCore) and performs the example requests from the "example/deegree/requests"
 * directory.
 * 
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 * 
 * @see org.deegree.ogcwebservices.csw.configuration.CatalogConfigurationDocumentTest
 */
public class CatalogueServiceTest extends AbstractServiceTest {
    private static ILogger LOG = LoggerFactory.getLogger( CatalogueServiceTest.class );

    private CatalogueService csw;

    private URL requestDir = new URL( Configuration.getCSWBaseDir(), "example/deegree/dublincore/requests/" );

    private URL outputDir = new URL( Configuration.getCSWBaseDir(), Configuration.GENERATED_DIR + "/" );

    public static Test suite() {
        return new TestSuite( CatalogueServiceTest.class );
    }

    /**
     * Constructor for CatalogueServiceTest.
     * 
     * @throws InvalidConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws OGCWebServiceException
     * @throws MalformedURLException
     */
    public CatalogueServiceTest() throws MalformedURLException, OGCWebServiceException, IOException, SAXException,
                            InvalidConfigurationException {
        this.csw = createCSW();
    }

    /**
     * Performs the <code>GetCapabilities</code> XML requests from the example/deegree directory.
     * 
     * @throws Exception
     */
    public void testGetCapabilitiesXMLExamples()
                            throws Exception {

        URL directoryURL = new URL( requestDir, "GetCapabilities/xml" );
        List<URL> exampleFiles = scanDirectory( directoryURL, "xml" );

        for ( URL example : exampleFiles ) {
            LOG.logInfo( "Reading GetCapabilities XML example '" + example + "'." );
            CatalogueGetCapabilitiesDocument requestDoc = new CatalogueGetCapabilitiesDocument();
            requestDoc.load( example );
            CatalogueGetCapabilities request = requestDoc.parse( "" );
            Object o = csw.doService( request );
            assertTrue( o instanceof CatalogueCapabilities );
            CatalogueCapabilitiesDocument doc = org.deegree.ogcwebservices.csw.XMLFactory_2_0_0.export( (CatalogueCapabilities) o,
                                                                                                        null );
            String outputFile = getResultFilename( example, outputDir );
            doc.write( new FileOutputStream( outputFile ) );
            LOG.logInfo( "Wrote '" + outputFile + "'." );
        }
    }

    /**
     * Performs the <code>GetCapabilities</code> KVP requests from the example/deegree directory.
     * 
     * @throws Exception
     */
    public void testGetCapabilitiesKVPExamples()
                            throws Exception {

        URL directoryURL = new URL( requestDir, "GetCapabilities/kvp" );
        List<URL> exampleFiles = scanDirectory( directoryURL, "kvp" );

        for ( URL example : exampleFiles ) {
            LOG.logInfo( "Reading GetCapabilities KVP example '" + example + "'." );
            Map<String, String> map = createKVPMap( example );
            CatalogueGetCapabilities request = CatalogueGetCapabilities.create( map );
            Object o = csw.doService( request );
            assertTrue( o instanceof CatalogueCapabilities );
            CatalogueCapabilitiesDocument doc = org.deegree.ogcwebservices.csw.XMLFactory_2_0_0.export( (CatalogueCapabilities) o,
                                                                                                        null );
            String outputFile = getResultFilename( example, outputDir );
            doc.write( new FileOutputStream( outputFile ) );
            LOG.logInfo( "Wrote '" + outputFile + "'." );
        }
    }

    /**
     * Performs the <code>DescribeRecord</code> XML requests from the example/deegree directory.
     * 
     * @throws Exception
     */
    public void testDescribeRecordXMLExamples()
                            throws Exception {

        URL directoryURL = new URL( requestDir, "DescribeRecord/xml" );
        List<URL> exampleFiles = scanDirectory( directoryURL, "xml" );

        for ( URL example : exampleFiles ) {
            LOG.logInfo( "Reading DescribeRecord XML example '" + example + "'." );
            DescribeRecordDocument requestDoc = new DescribeRecordDocument();
            requestDoc.load( example );
            DescribeRecord request = requestDoc.parse( "" );
            Object o = csw.doService( request );
            assertTrue( o instanceof DescribeRecordResult );
            DescribeRecordResult result = (DescribeRecordResult) o;
            DescribeRecordResultDocument resultDoc = XMLFactory.export( result );
            String outputFile = getResultFilename( example, outputDir );
            resultDoc.write( new FileOutputStream( outputFile ) );
            LOG.logInfo( "Wrote '" + outputFile + "'." );
        }
    }

    /**
     * Performs the <code>DescribeRecord</code> KVP requests from the example/deegree directory.
     * 
     * @throws Exception
     */
    public void testDescribeRecordKVPExamples()
                            throws Exception {

        URL directoryURL = new URL( requestDir, "DescribeRecord/kvp" );
        List<URL> exampleFiles = scanDirectory( directoryURL, "kvp" );

        for ( URL example : exampleFiles ) {
            LOG.logInfo( "Reading DescribeRecord KVP example '" + example + "'." );
            Map<String, String> map = createKVPMap( example );
            DescribeRecord request = DescribeRecord.create( map );
            Object o = csw.doService( request );
            assertTrue( o instanceof DescribeRecordResult );
            DescribeRecordResult result = (DescribeRecordResult) o;
            DescribeRecordResultDocument resultDoc = XMLFactory.export( result );
            String outputFile = getResultFilename( example, outputDir );
            resultDoc.write( new FileOutputStream( outputFile ) );
            LOG.logInfo( "Wrote '" + outputFile + "'." );
        }
    }

    /**
     * Performs the <code>Transaction</code> XML requests from the example/deegree directory.
     * 
     * @throws Exception
     */
    public void testTransactionXMLExamples()
                            throws Exception {

        URL directoryURL = new URL( requestDir, "Transaction/xml" );
        List<URL> exampleFiles = scanDirectory( directoryURL, "xml" );

        for ( URL example : exampleFiles ) {
            LOG.logInfo( "Reading Transaction XML example '" + example + "'." );
            TransactionDocument requestDoc = new TransactionDocument();
            requestDoc.load( example );
            Transaction request = requestDoc.parse( "" );
            Object o = csw.doService( request );
            assertTrue( o instanceof TransactionResult );
            TransactionResult result = (TransactionResult) o;
            TransactionResultDocument resultDoc = org.deegree.ogcwebservices.csw.manager.XMLFactory.export( result );
            String outputFile = getResultFilename( example, outputDir );
            resultDoc.write( new FileOutputStream( outputFile ) );
            LOG.logInfo( "Wrote '" + outputFile + "'." );
        }
    }

    /**
     * Performs the <code>GetRecords</code> XML requests from the example/deegree directory.
     * 
     * @throws Exception
     */
    // commented out broken tests
    // public void testGetRecordsXMLExamples()
    // throws Exception {
    //
    // URL directoryURL = new URL( requestDir, "GetRecords/xml" );
    // List<URL> exampleFiles = scanDirectory( directoryURL, "xml" );
    //
    // for ( URL example : exampleFiles ) {
    // if( example.toString().contains( "ebRIM" ) ){
    // LOG.logInfo( "ignoring ebrim with vars for now" );
    // } else {
    // LOG.logInfo( "Reading GetRecords XML example '" + example + "'." );
    // GetRecordsDocument requestDoc = new GetRecordsDocument();
    // requestDoc.load( example );
    // GetRecords request = requestDoc.parse( "" );
    // Object o = csw.doService( request );
    // assertTrue( o instanceof GetRecordsResult );
    // GetRecordsResult result = (GetRecordsResult) o;
    // GetRecordsResultDocument resultDoc = XMLFactory.export( result );
    // String outputFile = getResultFilename( example, outputDir );
    // resultDoc.write( new FileOutputStream( outputFile ) );
    // LOG.logInfo( "Wrote '" + outputFile + "'." );
    // }
    // }
    // }

    /**
     * Performs the <code>GetRecords</code> KVP requests from the example/deegree directory.
     * 
     * @throws Exception
     */
    // commented out broken test
    // public void testGetRecordsKVPExamples()
    // throws Exception {
    //
    // URL directoryURL = new URL( requestDir, "GetRecords/kvp" );
    // List<URL> exampleFiles = scanDirectory( directoryURL, "kvp" );
    //
    // for ( URL example : exampleFiles ) {
    // LOG.logInfo( "Reading GetRecords KVP example '" + example + "'." );
    // Map<String, String> map = createKVPMap( example );
    // GetRecords request = GetRecords.create( map );
    // Object o = csw.doService( request );
    // assertTrue( o instanceof GetRecordsResult );
    // GetRecordsResult result = (GetRecordsResult) o;
    // GetRecordsResultDocument resultDoc = XMLFactory.export( result );
    // String outputFile = getResultFilename( example, outputDir );
    // resultDoc.write( new FileOutputStream( outputFile ) );
    // LOG.logInfo( "Wrote '" + outputFile + "'." );
    // }
    // }

    private CatalogueService createCSW()
                            throws MalformedURLException, IOException, SAXException, InvalidConfigurationException,
                            OGCWebServiceException {
        CatalogueConfigurationDocument confDoc = new CatalogueConfigurationDocument();
        confDoc.load( Configuration.getCSWConfigurationURL() );
        CatalogueConfiguration conf = confDoc.getConfiguration();
        CSWFactory.setConfiguration( conf );
        return CSWFactory.getService();
    }
}
