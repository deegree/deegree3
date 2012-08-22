//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/ogcwebservices/wfs/capabilities/WFSCapabilitiesDocumentTest.java $
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
package org.deegree.ogcwebservices.wfs.capabilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities;
import org.deegree.model.filterencoding.capabilities.FilterCapabilitiesTest;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.getcapabilities.Contents;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.Protocol;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentificationTest;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.getcapabilities.ServiceProviderTest;
import org.deegree.ogcwebservices.wfs.XMLFactory;
import org.deegree.owscommon.OWSDomainType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import alltests.AllTests;
import alltests.Configuration;

/**
 * Test case to validate the structure and creation of a WFS configuration document. <BR>
 * Includes creation of an empty document, saving a document and reading a document. <BR>
 * Tests the modifying (getter/setter) of the different components of the document.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author last edited by: $Author: mschneider $
 *
 * @version $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 */
public class WFSCapabilitiesDocumentTest extends TestCase {

    private static ILogger LOG = LoggerFactory.getLogger( WFSCapabilitiesDocumentTest.class );
    public static Test suite() {
        return new TestSuite( WFSCapabilitiesDocumentTest.class );
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp()
                            throws Exception {
        super.setUp();
        LOG.logInfo( "wfs2ConfFile: " + Configuration.getWFSConfigurationURL() );
        LOG.logInfo( "base dir: " + Configuration.getBaseDir() );
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown()
                            throws Exception {
        super.tearDown();
    }

    public void testCreateEmptyDocument() {
        try {
            WFSCapabilitiesDocument doc = new WFSCapabilitiesDocument();
            doc.createEmptyDocument();
            Node rootNode = doc.getRootElement();
            assertNotNull( rootNode );
            assertEquals( "WFS_Capabilities", rootNode.getNodeName() );
            String namespaceUri = rootNode.getNamespaceURI();
            assertEquals( CommonNamespaces.WFSNS.toString(), namespaceUri );
        } catch ( Exception e ) {
            LOG.logError( "Unit test failed", e );
            fail( "Error: " + e.getMessage() );
        }
    }

    public void testGetVersion()
                            throws IOException, SAXException {
        WFSCapabilitiesDocument wfsConfDoc = new WFSCapabilitiesDocument();
        wfsConfDoc.createEmptyDocument();
        assertEquals( "1.1.0", wfsConfDoc.parseVersion() );
        assertEquals( "0", wfsConfDoc.parseUpdateSequence() );
        assertEquals( CommonNamespaces.WFSNS.toString(),
                      wfsConfDoc.getRootElement().getNamespaceURI() );
    }

    public void testValidateDocument()
                            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware( true );
        domFactory.setValidating( true );
        domFactory.setIgnoringElementContentWhitespace( true );
        domFactory.setExpandEntityReferences( true );
        DocumentBuilder domBuilder = domFactory.newDocumentBuilder();

        domBuilder.setErrorHandler( new ErrorHandler() {
            public void error( SAXParseException exception )
                                    throws SAXException {
                LOG.logWarning( exception.getMessage() );
                // fail(exception.getMessage());
            }

            public void fatalError( SAXParseException exception )
                                    throws SAXException {
                LOG.logError( exception.getMessage() );
                fail( exception.getMessage() );
            }

            public void warning( SAXParseException exception )
                                    throws SAXException {
                LOG.logWarning( exception.getMessage() );
            }
        } );
        Document doc = domBuilder.parse( Configuration.getWFSConfigurationURL().openStream() );
        assertNotNull( doc );
        assertEquals( CommonNamespaces.WFSNS.toString(), doc.getDocumentElement().getNamespaceURI() );
    }

    /**
     * Class under test for void load(URI)
     */
    public void testLoad()
                            throws SAXException, IOException {

        LOG.logInfo( "loading " + Configuration.getWFSConfigurationURL().toString() );
        WFSCapabilitiesDocument wfsConfDoc = new WFSCapabilitiesDocument();
        wfsConfDoc.load( Configuration.getWFSConfigurationURL() );
        assertNotNull( wfsConfDoc.getRootElement() );
        LOG.logDebug( wfsConfDoc.getRootElement().getNodeName() );
        assertEquals( "1.1.0", wfsConfDoc.parseVersion() );
        assertEquals( "10", wfsConfDoc.parseUpdateSequence() );
        assertEquals( CommonNamespaces.WFSNS.toString(),
                      wfsConfDoc.getRootElement().getNamespaceURI() );

    }

    /**
     * Create a JavaBean based configuration, than creating a new <code>XmlDocument</code> by
     * transforming the configuration to a XML capabilities file. This file is stored to disc and
     * parsed into a DOM representation to validate the output against the JavaBean based
     * configuration.
     *
     * @throws XMLParsingException
     *
     * @see #createConfiguration()
     * @see #testXML2JavaAndBack()
     */
    public void testSave()
                            throws URISyntaxException, IOException, SAXException,
                            XMLParsingException {
        WFSCapabilities capabilities = createConfiguration();
        WFSCapabilitiesDocument doc = XMLFactory.export( capabilities );
        assertEquals( capabilities.getVersion(), doc.parseVersion() );
        File outFile = new File(
                                 new URI( Configuration.getGeneratedWFSCapabilitiesURL().toString() ) );
        if ( outFile.exists() ) {
            outFile.delete();
        }
        assertTrue( outFile.createNewFile() );
        LOG.logInfo( "writing file:" + outFile.getAbsolutePath() );
        FileOutputStream outStream = new FileOutputStream( outFile );
        doc.write( outStream );
        outStream.close();

        assertTrue( outFile.exists() );
        assertTrue( outFile.canRead() );
        assertTrue( outFile.isFile() );
        assertTrue( outFile.length() > 0 );

        WFSCapabilitiesDocument wfsConfDoc = new WFSCapabilitiesDocument();
        wfsConfDoc.load( outFile.toURL() );
        assertEquals( CommonNamespaces.WFSNS.toString(),
                      wfsConfDoc.getRootElement().getNamespaceURI() );
        assertEquals( capabilities.getVersion(), wfsConfDoc.parseVersion() );
        assertEquals( capabilities.getUpdateSequence(), wfsConfDoc.parseUpdateSequence() );
        assertEquals( capabilities.getServiceIdentification().getServiceType().getCode(),
                      wfsConfDoc.getServiceIdentification().getServiceType().getCode() );
        assertEquals( capabilities.getServiceIdentification().getServiceTypeVersions()[0],
                      wfsConfDoc.getServiceIdentification().getServiceTypeVersions()[0] );
        assertTrue( outFile.delete() );
    }

    public static WFSCapabilities createConfiguration()
                            throws MalformedURLException, URISyntaxException {
        String version = "1.1.0";
        String updateSeq = "5";
        ServiceIdentification srvId = ServiceIdentificationTest.getTestInstance( "WFS", version );
        ServiceProvider provider = ServiceProviderTest.getTestInstance();
        URL[] onlineResources = new URL[] { Configuration.getWFSURL() };
        Protocol protocol = new HTTP( onlineResources, onlineResources );
        DCPType[] dcpTypes = new DCPType[2];
        dcpTypes[0] = new DCPType( protocol );
        dcpTypes[1] = new DCPType( protocol );
        Operation getCapabilitiesOperation = new Operation( "GetCapabilities", dcpTypes );
        Operation describeFeatureType = new Operation( "DescribeFeatureType", dcpTypes );
        Operation getFeature = new Operation( "GetFeature", dcpTypes );
        Operation getFeatureWithLock = new Operation( "GetFeatureWithLock", dcpTypes );
        Operation getGMLObject = new Operation( "GetGMLObject", dcpTypes );
        Operation lockFeature = new Operation( "LockFeature", dcpTypes );
        Operation transaction = new Operation( "Transaction", dcpTypes );

        OWSDomainType[] parameters = null;
        OWSDomainType[] constraints = null;
        OperationsMetadata operations = new WFSOperationsMetadata( getCapabilitiesOperation,
                                                                   describeFeatureType, getFeature,
                                                                   getFeatureWithLock,
                                                                   getGMLObject, lockFeature,
                                                                   transaction, parameters,
                                                                   constraints );

        FeatureTypeList featureTypeList = null;
        GMLObject[] servesGMLObjectTypeList = null;
        GMLObject[] supportsGMLObjectTypeList = null;
        Contents contents = new WFSContents();
        FilterCapabilities filter = FilterCapabilitiesTest.getTestInstance();
        return new WFSCapabilities( version, updateSeq, srvId, provider, operations,
                                    featureTypeList, servesGMLObjectTypeList,
                                    supportsGMLObjectTypeList, contents, filter );
    }

}
