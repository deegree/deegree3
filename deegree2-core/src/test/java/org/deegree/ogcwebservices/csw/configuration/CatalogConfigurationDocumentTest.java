//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/ogcwebservices/csw/configuration/CatalogConfigurationDocumentTest.java $
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
package org.deegree.ogcwebservices.csw.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deegree.datatypes.xlink.SimpleLink;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities;
import org.deegree.model.filterencoding.capabilities.FilterCapabilitiesTest;
import org.deegree.model.metadata.iso19115.Linkage;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.csw.XMLFactory_2_0_0;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueContents;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueOperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.Contents;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentificationTest;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.getcapabilities.ServiceProviderTest;
import org.deegree.owscommon.OWSDomainType;
import org.w3c.dom.Node;

import alltests.AllTests;
import alltests.Configuration;

/**
 * Test case to validate the structure and creation of a CWS configuration document. Includes
 * creation of an empty document, saving a document and reading a document. Tests the modifying
 * (getter/setter) of the different components of the document.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author last edited by: $Author: mschneider $
 *
 * @version $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 */
public class CatalogConfigurationDocumentTest extends TestCase {

    private static ILogger LOG = LoggerFactory.getLogger( CatalogConfigurationDocumentTest.class );

    private CatalogueConfigurationDocument doc;

    private File generatedCapabilities;

    public static Test suite() {
        return new TestSuite( CatalogConfigurationDocumentTest.class );
    }

    @Override
    protected void setUp()
                            throws Exception {
        super.setUp();
        this.generatedCapabilities = new File(
                                               Configuration.getGeneratedCSWCapabilitiesURL().getFile() );
        this.doc = new CatalogueConfigurationDocument();
        this.writeConfigDoc();
    }

    @Override
    protected void tearDown()
                            throws Exception {
        super.tearDown();
        File generatedFile = generatedCapabilities;
        if ( generatedFile.exists() && generatedFile.canWrite() ) {
            // generatedFile.delete();
            // LOG.logDebug("Configuration document "
            // + generatedFile.getCanonicalPath() + " deleted");
        }
        this.doc = null;
    }

    /**
     * Constructor for CatalogConfigurationDocumentTest.
     *
     * @param arg0
     */
    public CatalogConfigurationDocumentTest( String arg0 ) {
        super( arg0 );
    }

    /**
     * Test deegreeParams section
     *
     */
    public void testGetDeegreeParams() {
        try {
            assertNotNull( doc.getDeegreeParams() );
            assertEquals(
                          createConfiguration().getDeegreeParams().getDefaultOnlineResource().getLinkage().getHref(),
                          doc.getDeegreeParams().getDefaultOnlineResource().getLinkage().getHref() );
            assertEquals( doc.getDeegreeParams().getRequestTimeLimit(), 60 );
        } catch ( Exception e ) {
            LOG.logError( "Unit test failed", e );
            fail( "Error: " + e.getMessage() );
        }

    }

    /**
     * Create an empty configuration document with default settings.
     *
     */
    public void testCreateEmptyDocument() {
        try {
            doc.createEmptyDocument();
            Node rootNode = doc.getRootElement();
            assertNotNull( rootNode );
            assertEquals( "Capabilities", rootNode.getLocalName() );
            URI namespace = new URI( rootNode.getNamespaceURI() );
            assertEquals( CommonNamespaces.CSWNS, namespace );
        } catch ( Exception e ) {
            LOG.logError( "Unit test failed", e );
            fail( "Error: " + e.getMessage() );
        }
    }

    /**
     * Test the document root element
     *
     */
    public void testDocumentRootNode() {
        assertNotNull( doc.parseVersion() );
        assertEquals( "2.0.0", doc.parseVersion() );
        assertNotNull( doc.parseUpdateSequence() );
        assertEquals( "0", doc.parseUpdateSequence() );
    }

    /**
     * To test the loading from an input stream
     */
    public void testLoadInputStream() {
        try {
            File inFile = generatedCapabilities;
            assertTrue( "File not exists", inFile.exists() );
            doc.load( inFile.toURL() );
            assertEquals( doc.getRootElement().getLocalName(), "Capabilities" );
        } catch ( Exception e ) {
            LOG.logError( "Unit test failed", e );
            fail( "Error: " + e.getMessage() );
        }
    }

    /**
     * To test to save a configuration to an output stream
     */
    public void testSaveOutputStreamNode() {
        try {
            XMLFragment doc = XMLFactory_2_0_0.export( createConfiguration() );
            FileOutputStream outStream = new FileOutputStream( generatedCapabilities );
            doc.write( outStream );
            outStream.close();
            assertTrue( generatedCapabilities.exists() );
            assertTrue( generatedCapabilities.canRead() );
            assertTrue( generatedCapabilities.length() > 0 );
        } catch ( Exception e ) {
            LOG.logError( "Unit test failed", e );
            fail( "Error: " + e.getMessage() );
        }

    }

    /**
     * Load examples configuration resources/csw
     *
     */
    public void testLoadExampleConfiguration() {
        try {
            // File -> CatalogConfigurationDocument
            LOG.logDebug( "Example capabilities = "
                                   + Configuration.getCSWConfigurationURL() );
            assertNotNull( Configuration.getCSWConfigurationURL() );
            File configurationFile = new File( Configuration.getCSWConfigurationURL().getFile() );
            assertTrue( configurationFile.exists() );
            assertTrue( configurationFile.canRead() );
            doc.load( Configuration.getCSWConfigurationURL() );
            assertNotNull( doc.getRootElement() );
            assertEquals( "2.0.0", doc.parseVersion() );
            assertEquals( "Capabilities", doc.getRootElement().getLocalName() );
            assertEquals( "CSW", doc.getServiceIdentification().getServiceType().getCode() );
            // CatalogConfigurationDocument -> CatalogConfiguration
            CatalogueConfiguration config = doc.getConfiguration();
            assertNotNull( config );

            // CatalogConfiguration -> CatalogConfigurationDocument
            CatalogueConfigurationDocument generatedDocument = XMLFactory_2_0_0.export( config );
            assertNotNull( generatedDocument.getRootElement() );
            assertEquals( "2.0.0", generatedDocument.parseVersion() );
            assertEquals( "Capabilities", generatedDocument.getRootElement().getLocalName() );
            assertEquals( "CSW",
                          generatedDocument.getServiceIdentification().getServiceType().getCode() );
        } catch ( Exception e ) {
            LOG.logError( "Unit test failed", e );
            fail( e.getMessage() );
        }
    }

    private void writeConfigDoc()
                            throws IOException, URISyntaxException {
        FileOutputStream outStream = new FileOutputStream( generatedCapabilities );
        doc = XMLFactory_2_0_0.export( createConfiguration() );
        doc.write( outStream );
        outStream.close();
    }

    public static CatalogueConfiguration createConfiguration()
                            throws MalformedURLException, URISyntaxException {
        URL cswUrl = Configuration.getCSWURL();
        URL systemId = Configuration.getCSWBaseDir();
        String version = "2.0.0";
        String updateSequence = "0";
        OnlineResource onlineRes = new OnlineResource( new Linkage( cswUrl ) );
        URL cswInternalWFSURL = new URL( Configuration.getCSWBaseDir(),
                                         Configuration.CSW_INTERNALWFS_FILE );
        SimpleLink wfsOnlineRes = new SimpleLink( new URI( cswInternalWFSURL.toString() ) );
        ServiceIdentification serviceIdentification = ServiceIdentificationTest.getTestInstance(
                                                                                                 "CSW",
                                                                                                 version );
        ServiceProvider serviceProvider = ServiceProviderTest.getTestInstance();
        DCPType[] dcpTypes = new DCPType[] { new DCPType( new HTTP( new URL[] { cswUrl },
                                                                    new URL[] { cswUrl } ) ) };
        OWSDomainType[] getRecordParameters = new OWSDomainType[] {
                                                                   new CatalogueTypeNameSchemaParameter(
                                                                                                         "TypeName",
                                                                                                         new CatalogueTypeNameSchemaValue[] { new CatalogueTypeNameSchemaValue(
                                                                                                                                                                                "csw:Record",
                                                                                                                                                                                "dublincore.xsd" ) },
                                                                                                         null ),
                                                                   new OWSDomainType(
                                                                                      "outputFormat",
                                                                                      new String[] { "text/xml" },
                                                                                      null ),
                                                                   new CatalogueOutputSchemaParameter(
                                                                                                       "outputSchema",
                                                                                                       new CatalogueOutputSchemaValue[] { new CatalogueOutputSchemaValue(
                                                                                                                                                                          "DublinCore",
                                                                                                                                                                          "inDC.xsl",
                                                                                                                                                                          "outDC.xsl" ) },
                                                                                                       null ),
                                                                   new OWSDomainType(
                                                                                      "resultType",
                                                                                      new String[] { "RESULTS" },
                                                                                      null ),
                                                                   new OWSDomainType(
                                                                                      "ElementSetName",
                                                                                      new String[] {
                                                                                                    "brief",
                                                                                                    "summary",
                                                                                                    "full" },
                                                                                      null ) };
        OWSDomainType[] describeRecordParameters = new OWSDomainType[] {
                                                                        new OWSDomainType(
                                                                                           "TypeName",
                                                                                           new String[] { "csw:Record" },
                                                                                           null ),
                                                                        new OWSDomainType(
                                                                                           "outputFormat",
                                                                                           new String[] { "text/xml" },
                                                                                           null ),
                                                                        new OWSDomainType(
                                                                                           "schemaLanguage",
                                                                                           new String[] { "XMLSCHEMA" },
                                                                                           null ) };
        Operation getCapabilitiesOperation = new Operation(
                                                            OperationsMetadata.GET_CAPABILITIES_NAME,
                                                            dcpTypes );
        Operation describeRecordOperation = new Operation(
                                                           CatalogueOperationsMetadata.DESCRIBE_RECORD_NAME,
                                                           dcpTypes, describeRecordParameters );
        Operation getDomainOperation = new Operation( CatalogueOperationsMetadata.GET_DOMAIN_NAME,
                                                      dcpTypes );
        Operation getRecordsOperation = new Operation(
                                                       CatalogueOperationsMetadata.GET_RECORDS_NAME,
                                                       dcpTypes, getRecordParameters );
        Operation getRecordByIdOperation = new Operation(
                                                          CatalogueOperationsMetadata.GET_RECORD_BY_ID_NAME,
                                                          dcpTypes );
        Operation transactionOperation = new Operation(
                                                        CatalogueOperationsMetadata.TRANSACTION_NAME,
                                                        dcpTypes );
        Operation harvestOperation = new Operation( CatalogueOperationsMetadata.HARVEST_NAME,
                                                    dcpTypes );

        OperationsMetadata operationsMetadata = new CatalogueOperationsMetadata(
                                                                                 getCapabilitiesOperation,
                                                                                 describeRecordOperation,
                                                                                 getDomainOperation,
                                                                                 getRecordsOperation,
                                                                                 getRecordByIdOperation,
                                                                                 transactionOperation,
                                                                                 harvestOperation,
                                                                                 null, null );
        Contents contents = new CatalogueContents();
        FilterCapabilities filterCapabilities = FilterCapabilitiesTest.getTestInstance();
        CatalogueDeegreeParams catalogDeegreeParams = new CatalogueDeegreeParams(
                                                                                  onlineRes,
                                                                                  1,
                                                                                  60,
                                                                                  CharsetUtils.getSystemCharset(),
                                                                                  wfsOnlineRes,
                                                                                  new OnlineResource[] { onlineRes },
                                                                                  null, null, null,
                                                                                  null );
        return new CatalogueConfiguration( version, updateSequence, serviceIdentification,
                                           serviceProvider, operationsMetadata, contents,
                                           filterCapabilities, catalogDeegreeParams, systemId );
    }
}
