//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-base/src/main/java/org/deegree/protocol/wms/client/WMSClient111.java $
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
package org.deegree.protocol.wms.client;

import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.DescribeLayer;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.deegree.commons.struct.Tree;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.protocol.ows.metadata.Description;
import org.deegree.protocol.ows.metadata.OperationsMetadata;
import org.deegree.protocol.ows.metadata.ServiceIdentification;
import org.deegree.protocol.ows.metadata.ServiceProvider;
import org.deegree.protocol.ows.metadata.domain.Domain;
import org.deegree.protocol.ows.metadata.operation.DCP;
import org.deegree.protocol.ows.metadata.operation.Operation;
import org.deegree.protocol.ows.metadata.party.Address;
import org.deegree.protocol.ows.metadata.party.ContactInfo;
import org.deegree.protocol.ows.metadata.party.ResponsibleParty;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lgoltz $
 * 
 * @version $Revision: 31860 $, $Date: 2011-09-13 15:11:47 +0200 (Di, 13. Sep 2011) $
 */
public class WMS111CapabilitiesAdapterTest {

    private static final String GETMAP_URL = "http://demo.deegree.org:80/deegree-wms/services?";

    @Test(expected = IllegalArgumentException.class)
    public void testNullWMS111Capabilities() {
        new WMS111CapabilitiesAdapter( null );
    }

    @Test
    public void testWMS111CapabilitiesLayer()
                            throws XMLStreamException, UnknownCRSException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();

        List<String> namedLayers = capabilities.getNamedLayers();
        assertTrue( namedLayers.contains( "citelayers" ) );
        assertTrue( namedLayers.contains( "cite:BasicPolygons" ) );

        Tree<LayerMetadata> layerTree = capabilities.getLayerTree();
        LayerMetadata rootLayer = layerTree.value;
        assertNull( rootLayer.getName() );
        Description description = rootLayer.getDescription();
        assertEquals( "deegree demo WMS", description.getAbstract( null ).getString() );
        Envelope boundingBox = capabilities.getBoundingBox( "EPSG:26912", "citelayers" );
        Geometry bbox = ( new GeometryFactory() ).createEnvelope( 0.0, 3581352.0, 1504379.0, 5432672.0,
                                                                  CRSManager.lookup( "EPSG:26912" ) );
        assertTrue( boundingBox.equals( bbox ) );

        List<Tree<LayerMetadata>> children = layerTree.children;
        assertEquals( 2, children.size() );

    }

    @Test
    public void testWMS111CapabilitiesFormats()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();

        LinkedList<String> getMapFormats = capabilities.getFormats( GetMap );
        assertEquals( 8, getMapFormats.size() );
        assertTrue( getMapFormats.contains( "image/png" ) );
        assertTrue( getMapFormats.contains( "image/bmp" ) );
    }

    @Test
    public void testWMS111CapabilitiesFormatUnknownRequest()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();

        LinkedList<String> getMapFormats = capabilities.getFormats( DescribeLayer );
        assertNull( getMapFormats );
    }

    @Test
    public void testWMS111CapabilitiesAddresses()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();
        assertEquals( GETMAP_URL, capabilities.getAddress( GetMap, true ) );
        assertEquals( GETMAP_URL, capabilities.getAddress( GetMap, false ) );

    }

    @Test
    public void testWMS111CapabilitiesAddressesUnknownRequest()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();
        assertNull( capabilities.getAddress( DescribeLayer, true ) );
    }

    @Test
    public void testWMS111CapabilitiesServiceIdentification()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();
        ServiceIdentification serviceIdentification = capabilities.parseServiceIdentification();

        assertEquals( "OGC:WMS", serviceIdentification.getName() );
        assertEquals( "wms reference implementation", serviceIdentification.getAbstract( null ).getString() );
        assertEquals( "WMS", serviceIdentification.getServiceType().getCode() );
        Pair<List<LanguageString>, CodeType> keywords = serviceIdentification.getKeywords().get( 0 );
        assertEquals( 2, keywords.first.size() );
        assertEquals( "deegree wms", serviceIdentification.getTitle( null ).getString() );
        assertEquals( "none", serviceIdentification.getFees() );
        assertEquals( 1, serviceIdentification.getAccessConstraints().size() );
        assertEquals( "none", serviceIdentification.getAccessConstraints().get( 0 ) );

        assertEquals( 0, serviceIdentification.getProfiles().size() );
        assertEquals( new Version( 1, 1, 1 ), serviceIdentification.getServiceTypeVersion().get( 0 ) );
    }

    @Ignore
    @Test
    public void testWMS111CapabilitiesServiceProvider()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();
        ServiceProvider serviceProvider = capabilities.parseServiceProvider();
        ResponsibleParty serviceContact = serviceProvider.getServiceContact();
        assertEquals( "Andreas Poth", serviceContact.getIndividualName() );
        assertEquals( "lat/lon", serviceContact.getOrganizationName() );
        assertEquals( "Technical Director", serviceContact.getPositionName() );
        assertNull( serviceContact.getRole() );

        ContactInfo contactInfo = serviceContact.getContactInfo();
        assertNull( contactInfo.getContactInstruction() );
        assertNull( contactInfo.getHoursOfService() );
        assertEquals( GETMAP_URL, contactInfo.getOnlineResource().toExternalForm() );
        assertEquals( "00492281849629", contactInfo.getPhone().getFacsimile().get( 0 ) );
        assertEquals( "0049228184960", contactInfo.getPhone().getVoice().get( 0 ) );

        Address address = contactInfo.getAddress();
        assertEquals( "NRW", address.getAdministrativeArea() );
        assertEquals( "Bonn", address.getCity() );
        assertEquals( "53177", address.getPostalCode() );
        assertEquals( "Germany", address.getCountry() );
        assertEquals( "lat/lon", address.getDeliveryPoint() );
        assertEquals( "info@lat-lon.de", address.getElectronicMailAddress().get( 0 ) );
    }

    @Ignore
    @Test
    public void testWMS111CapabilitiesLanguages()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();
        List<String> languages = capabilities.parseLanguages();
        assertNull( languages );
    }

    @Test
    public void testWMS111CapabilitiesOperationsMetadata()
                            throws XMLStreamException {
        WMSCapabilitiesAdapter capabilities = createCapabilities();
        OperationsMetadata operationsMetadata = capabilities.parseOperationsMetadata();
        List<Operation> operations = operationsMetadata.getOperation();
        assertEquals( 4, operations.size() );

        Operation getMapOperation = operationsMetadata.getOperation( "GetMap" );

        List<URL> getUrls = getMapOperation.getGetUrls();
        assertEquals( 1, getUrls.size() );
        assertEquals( GETMAP_URL, getUrls.get( 0 ).toExternalForm() );

        List<URL> postUrls = getMapOperation.getPostUrls();
        assertEquals( 1, postUrls.size() );
        assertEquals( GETMAP_URL, postUrls.get( 0 ).toExternalForm() );

        List<DCP> dcps = getMapOperation.getDCPs();
        assertEquals( 1, dcps.size() );
        DCP dcp = dcps.get( 0 );

        List<Pair<URL, List<Domain>>> getEndpoints = dcp.getGetEndpoints();
        assertEquals( 1, getEndpoints.size() );
        assertEquals( GETMAP_URL, getEndpoints.get( 0 ).getFirst().toExternalForm() );

        List<Pair<URL, List<Domain>>> postEndpoints = dcp.getPostEndpoints();
        assertEquals( 1, postEndpoints.size() );
        assertEquals( GETMAP_URL, postEndpoints.get( 0 ).getFirst().toExternalForm() );
    }

    private WMSCapabilitiesAdapter createCapabilities()
                            throws XMLStreamException {
        InputStream is = WMS111CapabilitiesAdapterTest.class.getResourceAsStream( "wms111.xml" );
        StAXOMBuilder builder = new StAXOMBuilder( is );
        OMElement capabilities = builder.getDocumentElement();
        WMSCapabilitiesAdapter adapter = new WMS111CapabilitiesAdapter( capabilities );
        return adapter;
    }
}
