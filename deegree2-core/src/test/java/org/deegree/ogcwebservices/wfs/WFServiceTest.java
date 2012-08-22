//$Header: /deegreerepository/deegree/test/org/deegree/ogcwebservices/wfs/WFServiceTest.java,v 1.54 2007/01/06 23:13:50 mschneider Exp $
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
package org.deegree.ogcwebservices.wfs;

import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.deegree.framework.concurrent.ExecutionFinishedEvent;
import org.deegree.framework.concurrent.ExecutionFinishedListener;
import org.deegree.framework.concurrent.Executor;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.io.datastore.DatastoreTransaction;
import org.deegree.io.datastore.sql.AbstractSQLDatastore;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.GMLFeatureAdapter;
import org.deegree.ogcwebservices.AbstractServiceTest;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfiguration;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfigurationDocument;
import org.deegree.ogcwebservices.wfs.operation.DescribeFeatureType;
import org.deegree.ogcwebservices.wfs.operation.DescribeFeatureTypeDocument;
import org.deegree.ogcwebservices.wfs.operation.FeatureResult;
import org.deegree.ogcwebservices.wfs.operation.FeatureTypeDescription;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.GetFeatureDocument;
import org.deegree.ogcwebservices.wfs.operation.WFSGetCapabilities;
import org.deegree.ogcwebservices.wfs.operation.WFSGetCapabilitiesDocument;

import alltests.Configuration;

/**
 * Sets up an example WFS instance (Philosopher) and performs the example requests from the
 * "resources/wfs/example/deegree/requests" directory.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: rbezema $
 * 
 * @version $Revision: 19734 $, $Date: 2009-09-23 17:36:35 +0200 (Mi, 23 Sep 2009) $
 */
public class WFServiceTest extends AbstractServiceTest {

    private static ILogger LOG = LoggerFactory.getLogger( WFServiceTest.class );

    private WFService wfs;

    private URL requestDir = new URL( Configuration.getWFSBaseDir(), "demo/requests/philosopher/" );

    private URL outputDir = new URL( Configuration.getWFSBaseDir(), Configuration.GENERATED_DIR + "/" );

    /**
     * @throws Exception
     */
    public WFServiceTest() throws Exception {
        wfs = createWFS();
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
            WFSGetCapabilitiesDocument requestDoc = new WFSGetCapabilitiesDocument();
            requestDoc.load( example );
            WFSGetCapabilities request = requestDoc.parse( "" );
            Object o = wfs.doService( request );
            assertTrue( o instanceof WFSCapabilities );
            WFSCapabilitiesDocument document = XMLFactory.export( (WFSCapabilities) o );
            String outputFile = getResultFilename( example, outputDir );
            document.write( new FileOutputStream( outputFile ) );
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
            WFSGetCapabilities request = WFSGetCapabilities.create( map );
            Object o = wfs.doService( request );
            assertTrue( o instanceof WFSCapabilities );
            WFSCapabilitiesDocument document = XMLFactory.export( (WFSCapabilities) o );
            String outputFile = getResultFilename( example, outputDir );
            document.write( new FileOutputStream( outputFile ) );
            LOG.logInfo( "Wrote '" + outputFile + "'." );
        }
    }

    /**
     * Performs the <code>DescribeFeatureType</code> XML requests from the example/deegree directory.
     * 
     * @throws Exception
     */
    public void testDescribeFeatureTypeXMLExamples()
                            throws Exception {

        URL directoryURL = new URL( requestDir, "DescribeFeatureType/xml" );
        List<URL> exampleFiles = scanDirectory( directoryURL, "xml" );

        for ( URL example : exampleFiles ) {
            LOG.logInfo( "Reading DescribeFeatureType XML example '" + example + "'." );
            DescribeFeatureTypeDocument requestDoc = new DescribeFeatureTypeDocument();
            requestDoc.load( example );
            DescribeFeatureType request = requestDoc.parse( "" );
            Object o = wfs.doService( request );
            assertTrue( o instanceof FeatureTypeDescription );
            FeatureTypeDescription description = (FeatureTypeDescription) o;
            XMLFragment document = description.getFeatureTypeSchema();
            String outputFile = getResultFilename( example, outputDir );
            document.write( new FileOutputStream( outputFile ) );
            LOG.logInfo( "Wrote '" + outputFile + "'." );
        }
    }

    /**
     * Performs the <code>DescribeFeatureType</code> KVP requests from the example/deegree directory.
     * 
     * @throws Exception
     */
    public void testDescribeFeatureTypeKVPExamples()
                            throws Exception {

        URL directoryURL = new URL( requestDir, "DescribeFeatureType/kvp" );
        List<URL> exampleFiles = scanDirectory( directoryURL, "kvp" );

        for ( URL example : exampleFiles ) {
            LOG.logInfo( "Reading DescribeFeatureType KVP example '" + example + "'." );
            Map<String, String> map = createKVPMap( example );
            DescribeFeatureType request = DescribeFeatureType.create( map );
            Object o = wfs.doService( request );
            assertTrue( o instanceof FeatureTypeDescription );
            FeatureTypeDescription description = (FeatureTypeDescription) o;
            XMLFragment document = description.getFeatureTypeSchema();
            String outputFile = getResultFilename( example, outputDir );
            document.write( new FileOutputStream( outputFile ) );
            LOG.logInfo( "Wrote '" + outputFile + "'." );
        }
    }

    /**
     * Performs the <code>Transaction</code> XML requests from the example/deegree directory.
     * 
     * @throws Exception
     */
    // commented out, fails sometimes (must be a threading problem)
    // public void testTransactionXMLExamples()
    // throws Exception {
    //
    // URL directoryURL = new URL( requestDir, "Transaction/xml" );
    // List<URL> exampleFiles = scanDirectory( directoryURL, "xml" );
    //
    // for ( URL example : exampleFiles ) {
    // LOG.logInfo( "Reading Transaction XML example '" + example + "'." );
    // TransactionDocument requestDoc = new TransactionDocument();
    // requestDoc.load( example );
    // Transaction request = requestDoc.parse( "" );
    // Object o = wfs.doService( request );
    // assertTrue( o instanceof TransactionResponse );
    // XMLFragment document = XMLFactory.export( (TransactionResponse) o );
    // String outputFile = getResultFilename( example, outputDir );
    // document.write( new FileOutputStream( outputFile ) );
    // LOG.logInfo( "Wrote '" + outputFile + "'." );
    // }
    // }

    /**
     * Performs the <code>GetFeature</code> XML requests from the example/deegree directory.
     * 
     * @throws Exception
     */
    public void testGetFeatureXMLExamples()
                            throws Exception {

        URL directoryURL = new URL( requestDir, "GetFeature/xml" );
        List<URL> exampleFiles = scanDirectory( directoryURL, "xml" );

        for ( URL example : exampleFiles ) {
            LOG.logInfo( "Reading GetFeature XML example '" + example + "'." );
            GetFeatureDocument requestDoc = new GetFeatureDocument();
            requestDoc.load( example );
            GetFeature request = requestDoc.parse( "" );
            Object o = wfs.doService( request );
            assertTrue( o instanceof FeatureResult );
            FeatureResult result = (FeatureResult) o;
            assertTrue( result.getResponse() instanceof FeatureCollection );
            FeatureCollection fc = (FeatureCollection) result.getResponse();
            String outputFile = getResultFilename( example, outputDir );
            GMLFeatureAdapter gmlAdapter = new GMLFeatureAdapter();
            gmlAdapter.export( fc, new FileOutputStream( outputFile ) );
            LOG.logInfo( "Wrote '" + outputFile + "'." );
        }
    }

    /**
     * Performs the <code>GetFeature</code> KVP requests from the example/deegree directory.
     * 
     * @throws Exception
     */
    public void testGetFeatureKVPExamples()
                            throws Exception {

        URL directoryURL = new URL( requestDir, "GetFeature/kvp" );
        List<URL> exampleFiles = scanDirectory( directoryURL, "kvp" );

        for ( URL example : exampleFiles ) {
            LOG.logInfo( "Reading GetFeature KVP example '" + example + "'." );
            Map<String, String> map = createKVPMap( example );
            GetFeature request = GetFeature.create( map );
            Object o = wfs.doService( request );
            assertTrue( o instanceof FeatureResult );
            FeatureResult result = (FeatureResult) o;
            assertTrue( result.getResponse() instanceof FeatureCollection );
            FeatureCollection fc = (FeatureCollection) result.getResponse();
            String outputFile = getResultFilename( example, outputDir );
            GMLFeatureAdapter gmlAdapter = new GMLFeatureAdapter();
            gmlAdapter.export( fc, new FileOutputStream( outputFile ) );
            LOG.logInfo( "Wrote '" + outputFile + "'." );
        }
    }

    /**
     * Performs the <code>LockFeature</code> XML requests from the example/deegree directory.
     * 
     * @throws Exception
     */
    // probably fails due to commented tests
    // public void testLockFeatureXMLExamples()
    // throws Exception {
    //
    // URL directoryURL = new URL( requestDir, "LockFeature/xml" );
    // List<URL> exampleFiles = scanDirectory( directoryURL, "xml" );
    //
    // for ( URL example : exampleFiles ) {
    // LOG.logInfo( "Reading LockFeature XML example '" + example + "'." );
    // LockFeatureDocument requestDoc = new LockFeatureDocument();
    // requestDoc.load( example );
    // LockFeature request = requestDoc.parse( "" );
    // Object o = wfs.doService( request );
    // assertTrue( o instanceof LockFeatureResponse );
    // XMLFragment document = XMLFactory.export( (LockFeatureResponse) o );
    // String outputFile = getResultFilename( example, outputDir );
    // document.write( new FileOutputStream( outputFile ) );
    // LOG.logInfo( "Wrote '" + outputFile + "'." );
    // }
    // }

    /**
     * Tests the assigning of the {@link DatastoreTransaction} in parallel threads.
     * 
     * @throws Throwable
     */
    public void testParallelTransactionAcquisition()
                            throws Throwable {

        // get reference to datastore
        AbstractSQLDatastore ds = (AbstractSQLDatastore) this.wfs.getMappedFeatureTypes().values().iterator().next().getGMLSchema().getDatastore();

        // create 10 TransactionAcquirers that should all get the Transaction one-by-one
        List<Callable<TransactionAcquirer>> tasks = new ArrayList<Callable<TransactionAcquirer>>( 10 );
        tasks.add( new TransactionAcquirer( "1", ds, 0, 1 * 1000 ) );
        tasks.add( new TransactionAcquirer( "2", ds, 100, 1 * 1000 ) );
        tasks.add( new TransactionAcquirer( "3", ds, 200, 1 * 1000 ) );
        tasks.add( new TransactionAcquirer( "4", ds, 300, 1 * 1000 ) );
        tasks.add( new TransactionAcquirer( "5", ds, 400, 1 * 1000 ) );
        tasks.add( new TransactionAcquirer( "6", ds, 1550, 0 * 1000 ) );
        tasks.add( new TransactionAcquirer( "7", ds, 2000, 0 * 1000 ) );
        tasks.add( new TransactionAcquirer( "8", ds, 2000, 1 * 1000 ) );
        tasks.add( new TransactionAcquirer( "9", ds, 2500, 1 * 1000 ) );
        tasks.add( new TransactionAcquirer( "10", ds, 2500, 1 * 1000 ) );

        // create a TransactionAcquirer that will join the transaction waiting queue, but will be
        // zapped before it will get it
        Callable<TransactionAcquirer> task = new TransactionAcquirer( "zapped-1", ds, 2000, 0 * 1000 );
        Executor.getInstance().performAsynchronously( task, new ExecutionFinishedListener<TransactionAcquirer>() {

            public void executionFinished( ExecutionFinishedEvent<TransactionAcquirer> finishedEvent ) {
                // don't do anything
            }
        }, 500 );

        List<ExecutionFinishedEvent<TransactionAcquirer>> results = Executor.getInstance().performSynchronously( tasks,
                                                                                                                 60 * 1000 );
        assertTrue( results.size() == tasks.size() );
        for ( int i = 0; i < results.size(); i++ ) {
            ExecutionFinishedEvent<TransactionAcquirer> result = results.get( i );
            assertTrue( result.getResult() == tasks.get( i ) );
        }
    }

    private WFService createWFS()
                            throws Exception {
        WFSConfigurationDocument confDoc = new WFSConfigurationDocument();
        confDoc.load( Configuration.getWFSConfigurationURL() );
        WFSConfiguration conf = confDoc.getConfiguration();
        WFServiceFactory.setConfiguration( conf );
        return WFServiceFactory.createInstance();
    }

    private class TransactionAcquirer implements Callable<TransactionAcquirer> {

        private String name;

        private AbstractSQLDatastore ds;

        private long acquisitionDelay;

        private long holdTime;

        TransactionAcquirer( String name, AbstractSQLDatastore ds, long acquisitionDelay, long holdTime ) {
            this.name = name;
            this.ds = ds;
            this.acquisitionDelay = acquisitionDelay;
            this.holdTime = holdTime;
        }

        public TransactionAcquirer call()
                                throws Exception {

            LOG.logInfo( this + " created: sleeping for " + acquisitionDelay + " milliseconds before acquisition." );
            Thread.sleep( acquisitionDelay );
            LOG.logInfo( this + ": acquiring transaction..." );
            DatastoreTransaction ta = this.ds.acquireTransaction();
            LOG.logInfo( this + ": got transaction. Waiting for " + holdTime + " milliseconds before releasing it." );
            LOG.logInfo( this + ": releasing transaction..." );
            ds.releaseTransaction( ta );
            return this;
        }

        @Override
        public String toString() {
            return "TransactionAcquirer " + name;
        }
    }
}
