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
package org.deegree.protocol.wps.client;

import static org.locationtech.jts.io.gml2.GMLConstants.GML_NAMESPACE;
import static org.locationtech.jts.io.gml2.GMLConstants.GML_PREFIX;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.ows.metadata.OperationsMetadata;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.ows.metadata.operation.Operation;
import org.deegree.commons.ows.metadata.party.ContactInfo;
import org.deegree.commons.ows.metadata.party.ResponsibleParty;
import org.deegree.commons.utils.test.TestProperties;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.wps.WPSConstants;
import org.deegree.protocol.wps.WPSConstants.ExecutionState;
import org.deegree.protocol.wps.client.input.type.BBoxInputType;
import org.deegree.protocol.wps.client.input.type.ComplexInputType;
import org.deegree.protocol.wps.client.input.type.InputType;
import org.deegree.protocol.wps.client.input.type.LiteralInputType;
import org.deegree.protocol.wps.client.output.BBoxOutput;
import org.deegree.protocol.wps.client.output.ComplexOutput;
import org.deegree.protocol.wps.client.output.LiteralOutput;
import org.deegree.protocol.wps.client.output.type.BBoxOutputType;
import org.deegree.protocol.wps.client.output.type.ComplexOutputType;
import org.deegree.protocol.wps.client.output.type.LiteralOutputType;
import org.deegree.protocol.wps.client.output.type.OutputType;
import org.deegree.protocol.wps.client.process.Process;
import org.deegree.protocol.wps.client.process.ProcessExecution;
import org.deegree.protocol.wps.client.process.RawProcessExecution;
import org.deegree.protocol.wps.client.process.execute.ExecutionOutputs;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * JUnit class tests the functionality of the client.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPSClientTest {

    private static final File CURVE_FILE = new File( WPSClientTest.class.getResource( "curve.xml" ).getPath() );

    private static final File BINARY_INPUT = new File( WPSClientTest.class.getResource( "image.png" ).getPath() );

    private static final String REMOTE_BINARY_INPUT = "http://www.deegree.org/images/logos/deegree.png";

    private static final String WPS_NS = "http://www.opengis.net/wps/1.0.0";
    private static final String OWS_NS = "http://www.opengis.net/ows/1.1";

    @Test
    public void testMetadata()
                            throws OWSExceptionReport, IOException {

        String demoWPSURL = TestProperties.getProperty( "demo_wps_url" );
        Assume.assumeNotNull( demoWPSURL );
        URL serviceUrl = new URL( demoWPSURL );

        WPSClient client = new WPSClient( serviceUrl );
        Assert.assertNotNull( client );
        ServiceIdentification serviceId = client.getMetadata().getServiceIdentification();
        Assert.assertNotNull( serviceId );
        Assert.assertEquals( 1, serviceId.getTitles().size() );
        Assert.assertNotNull( serviceId.getTitles().get( 0 ).getString() ); 
        Assert.assertEquals( 1, serviceId.getAbstracts().size() );
        Assert.assertNotNull( serviceId.getAbstracts().get( 0 ).getString() );

        Assert.assertEquals( "WPS", serviceId.getServiceType().getCode() );
        Assert.assertEquals( "1.0.0", serviceId.getServiceTypeVersion().get( 0 ).toString() );

        ServiceProvider serviceProvider = client.getMetadata().getServiceProvider();
        Assert.assertEquals( "lat/lon GmbH", serviceProvider.getProviderName() );
        Assert.assertEquals( "http://www.lat-lon.de", serviceProvider.getProviderSite() );

        ResponsibleParty serviceContact = serviceProvider.getServiceContact();
        Assert.assertNotNull( serviceContact.getIndividualName() );
        Assert.assertNotNull( serviceContact.getPositionName() );

        ContactInfo contactInfo = serviceContact.getContactInfo();
        Assert.assertEquals( "0228/18496-0", contactInfo.getPhone().getVoice().get( 0 ) );
        Assert.assertEquals( "0228/18496-29", contactInfo.getPhone().getFacsimile().get( 0 ) );
        Assert.assertEquals( "Aennchenstr. 19", contactInfo.getAddress().getDeliveryPoint().get( 0 ) );
        Assert.assertEquals( "Bonn", contactInfo.getAddress().getCity() );
        Assert.assertEquals( "NRW", contactInfo.getAddress().getAdministrativeArea() );
        Assert.assertEquals( "53177", contactInfo.getAddress().getPostalCode() );
        Assert.assertEquals( "Germany", contactInfo.getAddress().getCountry() );
        Assert.assertNotNull( contactInfo.getAddress().getElectronicMailAddress().get( 0 ) );
        Assert.assertEquals( "http://www.deegree.org", contactInfo.getOnlineResource().toExternalForm() );
        Assert.assertEquals( "24x7", contactInfo.getHoursOfService() );
        Assert.assertEquals( "Do not hesitate to call", contactInfo.getContactInstruction() );

        Assert.assertEquals( "PointOfContact", serviceContact.getRole().getCode() );

        OperationsMetadata opMetadata = client.getMetadata().getOperationsMetadata();
        Operation op; 
        
        op = opMetadata.getOperation("GetCapabilities"); 
        Assert.assertNotNull( op.getGetUrls().get( 0 ).toExternalForm() );
        Assert.assertNotNull( op.getPostUrls().get( 0 ).toExternalForm() );
        
        op = opMetadata.getOperation("DescribeProcess"); 
        Assert.assertNotNull( op.getGetUrls().get( 0 ).toExternalForm() );
        Assert.assertNotNull( op.getPostUrls().get( 0 ).toExternalForm() );
        
        op = opMetadata.getOperation("Execute"); 
        Assert.assertNotNull( op.getGetUrls().get( 0 ).toExternalForm() );
        Assert.assertNotNull( op.getPostUrls().get( 0 ).toExternalForm() );
    }

    @Test
    public void testProcessDescription_1()
                            throws OWSExceptionReport, IOException {
        String demoWPSURL = TestProperties.getProperty( "demo_wps_url" );
        Assume.assumeNotNull( demoWPSURL );
        WPSClient wpsClient = new WPSClient( new URL( demoWPSURL ) );
        org.deegree.protocol.wps.client.process.Process p1 = wpsClient.getProcess( "Buffer" );
        LiteralInputType literalInput = (LiteralInputType) p1.getInputTypes()[1];
        Assert.assertEquals( "1", literalInput.getMinOccurs() );
        Assert.assertEquals( "1", literalInput.getMaxOccurs() );
        Assert.assertEquals( "double", literalInput.getDataType().getValue() );
        Assert.assertEquals( "http://www.w3.org/TR/xmlschema-2/#double", literalInput.getDataType().getRef().toString() );
        Assert.assertEquals( "unity", literalInput.getDefaultUom().getValue() );
        Assert.assertEquals( "unity", literalInput.getSupportedUoms()[0].getValue() );
        Assert.assertEquals( true, literalInput.isAnyValue() );

        OutputType output = p1.getOutputTypes()[0];
        ComplexOutputType complexData = (ComplexOutputType) output;
        Assert.assertEquals( "UTF-8", complexData.getDefaultFormat().getEncoding() );
        Assert.assertEquals( "text/xml", complexData.getDefaultFormat().getMimeType() );
        Assert.assertTrue( complexData.getDefaultFormat().getSchema().startsWith("http://schemas.opengis.net/gml/3.1.1/base/") );
        Assert.assertEquals( "UTF-8", complexData.getSupportedFormats()[0].getEncoding() );
        Assert.assertEquals( "text/xml", complexData.getSupportedFormats()[0].getMimeType() );
        Assert.assertTrue( complexData.getSupportedFormats()[0].getSchema().startsWith("http://schemas.opengis.net/gml/3.1.1/base/") );
    }

    @Test
    public void testProcessDescription_2()
                            throws OWSExceptionReport, IOException {
        String demoWPSURL = TestProperties.getProperty( "demo_wps_url" );
        Assume.assumeNotNull( demoWPSURL );
        WPSClient wpsClient = new WPSClient( new URL( demoWPSURL ) );
        org.deegree.protocol.wps.client.process.Process p2 = wpsClient.getProcess( "Crosses", null );
        InputType secondInput = p2.getInputTypes()[1];
        Assert.assertEquals( "1", secondInput.getMinOccurs() );
        Assert.assertEquals( "1", secondInput.getMaxOccurs() );
        ComplexInputType complexData = (ComplexInputType) secondInput;
        Assert.assertEquals( "text/xml", complexData.getDefaultFormat().getMimeType() );
        Assert.assertEquals( "UTF-8", complexData.getDefaultFormat().getEncoding() );
        Assert.assertEquals( "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd",
                             complexData.getDefaultFormat().getSchema() );
        Assert.assertEquals( "text/xml", complexData.getSupportedFormats()[0].getMimeType() );
        Assert.assertEquals( "UTF-8", complexData.getSupportedFormats()[0].getEncoding() );
        Assert.assertEquals( "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd",
                             complexData.getSupportedFormats()[0].getSchema() );

        OutputType output = p2.getOutputTypes()[0];
        LiteralOutputType literalOut = (LiteralOutputType) output;
        Assert.assertEquals( "boolean", literalOut.getDataType().getValue() );
        Assert.assertEquals( "http://www.w3.org/TR/xmlschema-2/#boolean", literalOut.getDataType().getRef().toString() );
    }

    @Test
    public void testProcessDescription_3()
                            throws OWSExceptionReport, IOException {
        String demoWPSURL = TestProperties.getProperty( "demo_wps_url" );
        Assume.assumeNotNull( demoWPSURL );
        WPSClient wpsClient = new WPSClient( new URL( demoWPSURL ) );
        org.deegree.protocol.wps.client.process.Process p2 = wpsClient.getProcess( "ParameterDemoProcess", null );

        InputType firstInput = p2.getInputTypes()[0];
        LiteralInputType literalInput = (LiteralInputType) firstInput;
        Assert.assertEquals( "integer", literalInput.getDataType().getValue() );
        Assert.assertEquals( "http://www.w3.org/TR/xmlschema-2/#integer",
                             literalInput.getDataType().getRef().toString() );
        Assert.assertEquals( "seconds", literalInput.getDefaultUom().getValue() );
        Assert.assertEquals( "seconds", literalInput.getSupportedUoms()[0].getValue() );
        Assert.assertEquals( "minutes", literalInput.getSupportedUoms()[1].getValue() );

        InputType secondInput = p2.getInputTypes()[1];
        Assert.assertEquals( "1", secondInput.getMinOccurs() );
        Assert.assertEquals( "1", secondInput.getMaxOccurs() );
        BBoxInputType bboxData = (BBoxInputType) secondInput;
        Assert.assertEquals( "EPSG:4326", bboxData.getDefaultCRS() );
        Assert.assertEquals( "EPSG:4326", bboxData.getSupportedCrs()[0] );

        InputType thirdInput = p2.getInputTypes()[2];
        ComplexInputType xmlData = (ComplexInputType) thirdInput;
        Assert.assertEquals( "text/xml", xmlData.getDefaultFormat().getMimeType() );
        Assert.assertEquals( "text/xml", xmlData.getSupportedFormats()[0].getMimeType() );

        InputType fourthInput = p2.getInputTypes()[3];
        ComplexInputType binaryData = (ComplexInputType) fourthInput;
        Assert.assertEquals( "image/png", binaryData.getDefaultFormat().getMimeType() );
        Assert.assertEquals( "base64", binaryData.getDefaultFormat().getEncoding() );
        Assert.assertEquals( "image/png", binaryData.getSupportedFormats()[0].getMimeType() );
        Assert.assertEquals( "base64", binaryData.getSupportedFormats()[0].getEncoding() );

        OutputType firstOutput = p2.getOutputTypes()[0];
        Assert.assertEquals( "A literal output parameter", firstOutput.getTitle().getString() );
        LiteralOutputType literalData = (LiteralOutputType) firstOutput;
        Assert.assertEquals( "integer", literalData.getDataType().getValue() );
        Assert.assertEquals( "http://www.w3.org/TR/xmlschema-2/#integer", literalData.getDataType().getRef().toString() );
        Assert.assertEquals( "seconds", literalData.getDefaultUom().getValue() );
        Assert.assertEquals( "seconds", literalData.getSupportedUoms()[0].getValue() );

        OutputType secondOutput = p2.getOutputTypes()[1];
        BBoxOutputType bboxOutput = (BBoxOutputType) secondOutput;
        Assert.assertEquals( "EPSG:4326", bboxOutput.getDefaultCrs() );
        Assert.assertEquals( "EPSG:4326", bboxOutput.getSupportedCrs()[0] );

        OutputType thirdOutput = p2.getOutputTypes()[2];
        ComplexOutputType xmlOutput = (ComplexOutputType) thirdOutput;
        Assert.assertEquals( "text/xml", xmlOutput.getDefaultFormat().getMimeType() );
        Assert.assertEquals( "text/xml", xmlOutput.getSupportedFormats()[0].getMimeType() );

        OutputType fourthOutput = p2.getOutputTypes()[3];
        ComplexOutputType binaryOutput = (ComplexOutputType) fourthOutput;
        System.out.println( binaryOutput.getDefaultFormat() );
        Assert.assertEquals( "text/xml", xmlOutput.getDefaultFormat().getMimeType() );
        Assert.assertEquals( "text/xml", xmlOutput.getSupportedFormats()[0].getMimeType() );
    }

    // @Test
    // public void testProcessDescription_4()
    // throws OWSException, IOException {
    // URL processUrl = new URL( NORTH52_SERVICE_URL );
    // WPSClient wpsClient = new WPSClient( processUrl );
    // Process proc = wpsClient.getProcess( "buffer", null );
    // InputDescription inputLayer = proc.getInputType( "LAYER", null );
    // ComplexDataDescription layerData = (ComplexDataDescription) inputLayer.getData();
    // Assert.assertEquals( "http://geoserver.itc.nl:8080/wps/schemas/gml/2.1.2/gmlpacket.xsd",
    // layerData.getSupportedFormats()[1].getSchema() );
    //
    // InputDescription inputField = proc.getInputType( "FIELD", null );
    // LiteralDataDescription fieldData = (LiteralDataDescription) inputField.getData();
    // Assert.assertEquals( "xs:int", fieldData.getDataType().getRef().toString() );
    // Assert.assertEquals( "0", fieldData.getRanges()[0].getMinimumValue() );
    // Assert.assertEquals( "+Infinity", fieldData.getRanges()[0].getMaximumValue() );
    //
    // InputDescription inputMethod = proc.getInputType( "METHOD", null );
    // Assert.assertEquals( "Distance", inputMethod.getAbstract().getString() );
    // LiteralDataDescription methodData = (LiteralDataDescription) inputMethod.getData();
    // Assert.assertEquals( "Fixed distance", methodData.getAllowedValues()[0] );
    // Assert.assertEquals( "Distance from table field", methodData.getAllowedValues()[1] );
    // }

    @Test
    public void testGetProcess()
                            throws OWSExceptionReport, IOException {
        String demoWPSURL = TestProperties.getProperty( "demo_wps_url" );
        Assume.assumeNotNull( demoWPSURL );
        WPSClient wpsClient = new WPSClient( new URL( demoWPSURL ) );
        org.deegree.protocol.wps.client.process.Process p1 = wpsClient.getProcess( "Buffer", null );
        Assert.assertNotNull( p1 );
        org.deegree.protocol.wps.client.process.Process p2 = wpsClient.getProcess( "ParameterDemoProcess", null );
        Assert.assertNotNull( p2 );
    }

    @Test
    public void testExecute_1()
                            throws Exception {

        String demoWPSURL = TestProperties.getProperty( "demo_wps_url" );
        Assume.assumeNotNull( demoWPSURL );
        WPSClient wpsClient = new WPSClient( new URL( demoWPSURL ) );
        org.deegree.protocol.wps.client.process.Process proc = wpsClient.getProcess( "Centroid", null );
        ProcessExecution execution = proc.prepareExecution();
        execution.addXMLInput( "GMLInput", null, CURVE_FILE.toURI(), false, "text/xml", null, null );
        execution.addOutput( "Centroid", null, null, true, null, null, null );
        ExecutionOutputs response = execution.execute();
        assertEquals( ExecutionState.SUCCEEDED, execution.getState() );

        ComplexOutput output = (ComplexOutput) response.get( 0 );
        XMLStreamReader reader = output.getAsXMLStream();
        XMLAdapter searchableXML = new XMLAdapter( reader );
        NamespaceBindings nsContext = new NamespaceBindings();
        nsContext.addNamespace( "wps", WPSConstants.WPS_100_NS );
        nsContext.addNamespace( "gml", "http://www.opengis.net/gml" );
        XPath xpath = new XPath( "/gml:Point/gml:pos/text()", nsContext );
        String pos = searchableXML.getRequiredNodeAsString( searchableXML.getRootElement(), xpath );

        String[] pair = pos.split( "\\s" );
        Assert.assertNotNull( Double.parseDouble( pair[0] ) );
        Assert.assertNotNull( Double.parseDouble( pair[1] ) );
    }

    @Test
    public void testExecute_2()
                            throws Exception {

        String demoWPSURL = TestProperties.getProperty( "demo_wps_url" );
        Assume.assumeNotNull( demoWPSURL );
        WPSClient wpsClient = new WPSClient( new URL( demoWPSURL ) );
        org.deegree.protocol.wps.client.process.Process proc = wpsClient.getProcess( "Buffer", null );

        ProcessExecution execution = proc.prepareExecution();
        execution.addLiteralInput( "BufferDistance", null, "0.1", "double", "unity" );
        execution.addXMLInput( "GMLInput", null, CURVE_FILE.toURI(), false, "text/xml", null, null );
        execution.addOutput( "BufferedGeometry", null, null, false, null, null, null );
        ExecutionOutputs outputs = execution.execute();

        ComplexOutput complexOut = outputs.getComplex( "BufferedGeometry", null );
        XMLAdapter searchableXML = new XMLAdapter( complexOut.getAsXMLStream() );
        String xpathStr = "/gml:Polygon/gml:exterior/gml:LinearRing/gml:posList";
        NamespaceBindings nsContext = new NamespaceBindings();
        nsContext.addNamespace( GML_PREFIX, GML_NAMESPACE );
        XPath xpath = new XPath( xpathStr, nsContext );
        String pointList = searchableXML.getRequiredNodeAsString( searchableXML.getRootElement(), xpath );
        Assert.assertEquals( 460, pointList.split( "\\s" ).length );
    }

    @Test
    public void testExecute_3()
                            throws OWSExceptionReport, IOException, XMLStreamException {
        String demoWPSURL = TestProperties.getProperty( "demo_wps_url" );
        Assume.assumeNotNull( demoWPSURL );
        WPSClient wpsClient = new WPSClient( new URL( demoWPSURL ) );
        org.deegree.protocol.wps.client.process.Process proc = wpsClient.getProcess( "ParameterDemoProcess" );

        ProcessExecution execution = proc.prepareExecution();
        execution.addLiteralInput( "LiteralInput", null, "0", "integer", "seconds" );
        execution.addBBoxInput( "BBOXInput", null, new double[] { 0, 0 }, new double[] { 90, 180 }, "EPSG:4326" );
        execution.addXMLInput( "XMLInput", null, CURVE_FILE.toURI(), false, "text/xml", null, null );
        execution.addBinaryInput( "BinaryInput", null, BINARY_INPUT.toURI(), false, "image/png", null );
        ExecutionOutputs outputs = execution.execute();

        LiteralOutput out1 = (LiteralOutput) outputs.get( 0 );
        Assert.assertEquals( "0", out1.getValue() );
        Assert.assertEquals( "integer", out1.getDataType() );
        Assert.assertEquals( "seconds", out1.getUom() );

        BBoxOutput out2 = (BBoxOutput) outputs.get( 1 );
        Assert.assertTrue( Arrays.equals( new double[] { 0.0, 0.0 }, out2.getLower() ) );
        Assert.assertTrue( Arrays.equals( new double[] { 90.0, 180.0 }, out2.getUpper() ) );
        Assert.assertEquals( "EPSG:4326", out2.getCrs() );
        Assert.assertEquals( 2, out2.getDimension() );
    }

    @Test
    public void testExecute_4()
                            throws OWSExceptionReport, IOException, XMLStreamException {
        String demoWPSURL = TestProperties.getProperty( "demo_wps_url" );
        Assume.assumeNotNull( demoWPSURL );
        WPSClient wpsClient = new WPSClient( new URL( demoWPSURL ) );
        org.deegree.protocol.wps.client.process.Process proc = wpsClient.getProcess( "ParameterDemoProcess", null );

        ProcessExecution execution = proc.prepareExecution();
        execution.addLiteralInput( "LiteralInput", null, "0", "integer", "seconds" );
        execution.addBBoxInput( "BBOXInput", null, new double[] { 0, 0 }, new double[] { 90, 180 }, "EPSG:4326" );
        execution.addXMLInput( "XMLInput", null, CURVE_FILE.toURI(), false, "text/xml", null, null );
        execution.addBinaryInput( "BinaryInput", null, BINARY_INPUT.toURI(), false, "image/png", null );
        execution.addOutput( "BBOXOutput", null, null, false, null, null, null );
        ExecutionOutputs outputs = execution.execute();

        BBoxOutput bboxOut = outputs.getBoundingBox( "BBOXOutput", null );
        Assert.assertTrue( Arrays.equals( new double[] { 0.0, 0.0 }, bboxOut.getLower() ) );
        Assert.assertTrue( Arrays.equals( new double[] { 90.0, 180.0 }, bboxOut.getUpper() ) );
        Assert.assertEquals( "EPSG:4326", bboxOut.getCrs() );
        Assert.assertEquals( 2, bboxOut.getDimension() );

        Assert.assertNull( outputs.getComplex( "XMLOutput", null ) );
        Assert.assertNull( outputs.getComplex( "BinaryOutput", null ) );
        Assert.assertNull( outputs.getLiteral( "LiteralOutput", null ) );
    }

    @Test
    public void testExecuteRawOutput()
                            throws OWSExceptionReport, IOException, XMLStreamException {
        String demoWPSURL = TestProperties.getProperty( "demo_wps_url" );
        Assume.assumeNotNull( demoWPSURL );
        WPSClient wpsClient = new WPSClient( new URL( demoWPSURL ) );
        org.deegree.protocol.wps.client.process.Process proc = wpsClient.getProcess( "ParameterDemoProcess", null );

        RawProcessExecution execution = proc.prepareRawExecution();
        execution.addLiteralInput( "LiteralInput", null, "0", "integer", "seconds" );
        execution.addBBoxInput( "BBOXInput", null, new double[] { 0, 0 }, new double[] { 90, 180 }, "EPSG:4326" );
        execution.addXMLInput( "XMLInput", null, CURVE_FILE.toURI(), false, "text/xml", null, null );
        execution.addBinaryInput( "BinaryInput", null, BINARY_INPUT.toURI(), false, "image/png", null );
        ComplexOutput out = execution.executeComplexOutput( "BinaryOutput", null, "image/png", null, null );

        InputStream stream = out.getAsBinaryStream();
        FileOutputStream fileStream = new FileOutputStream( File.createTempFile( "wpsBinaryOut", "" ) );
        byte[] b = new byte[1024];
        int read = -1;
        while ( ( read = stream.read( b ) ) != -1 ) {
            fileStream.write( b, 0, read );
        }
        fileStream.close();
        stream.close();
    }

    @Test
    public void testExecuteInputsByRef()
                            throws OWSExceptionReport, IOException, XMLStreamException, URISyntaxException {
        String demoWPSURL = TestProperties.getProperty( "demo_wps_url" );
        Assume.assumeNotNull( demoWPSURL );
        WPSClient wpsClient = new WPSClient( new URL( demoWPSURL ) );
        org.deegree.protocol.wps.client.process.Process proc = wpsClient.getProcess( "ParameterDemoProcess", null );

        ProcessExecution execution = proc.prepareExecution();
        execution.addLiteralInput( "LiteralInput", null, "0", "integer", "seconds" );
        execution.addBBoxInput( "BBOXInput", null, new double[] { 0, 0 }, new double[] { 90, 180 }, "EPSG:4326" );
        // use the process's GetCapabilities document as XML input because we can be sure it's available
        execution.addXMLInput( "XMLInput", null, new URI( demoWPSURL ), true, "text/xml", null, null );
        execution.addBinaryInput( "BinaryInput", null, new URI( REMOTE_BINARY_INPUT ), true, "image/png", null );
        ExecutionOutputs outputs = execution.execute();

        LiteralOutput out1 = (LiteralOutput) outputs.get( 0 );
        Assert.assertEquals( "0", out1.getValue() );
        Assert.assertEquals( "integer", out1.getDataType() );
        Assert.assertEquals( "seconds", out1.getUom() );

        BBoxOutput out2 = (BBoxOutput) outputs.get( 1 );
        Assert.assertTrue( Arrays.equals( new double[] { 0.0, 0.0 }, out2.getLower() ) );
        Assert.assertTrue( Arrays.equals( new double[] { 90.0, 180.0 }, out2.getUpper() ) );
        Assert.assertEquals( "EPSG:4326", out2.getCrs() );
        Assert.assertEquals( 2, out2.getDimension() );

        ComplexOutput output = (ComplexOutput) outputs.get( 2 );
        XMLStreamReader reader = output.getAsXMLStream();
        XMLAdapter searchableXML = new XMLAdapter( reader );
        NamespaceBindings nsContext = new NamespaceBindings();
        nsContext.addNamespace( "wps", WPS_NS );
        nsContext.addNamespace( "ows", OWS_NS );
        XPath xpath = new XPath( "/wps:Capabilities/ows:ServiceIdentification/ows:ServiceType", nsContext );
        String pos = searchableXML.getRequiredNodeAsString( searchableXML.getRootElement(), xpath );
        Assert.assertEquals( pos, "WPS" );
        
        InputStream binaryStream = outputs.getComplex( "BinaryOutput", null ).getAsBinaryStream();
        Assert.assertNotNull( binaryStream );
        binaryStream.close();

        // Assert.assertTrue( compareStreams( new URL( REMOTE_BINARY_INPUT ).openStream(),
        // outputs.getComplex( "BinaryOutput", null ).getAsBinaryStream() ) );
    }

    // /**
    // * @param openStream
    // * @param complex
    // * @throws IOException
    // */
    // private boolean compareStreams( InputStream originalStream, InputStream resultingStream )
    // throws IOException {
    // boolean result = true;
    // byte[] b1 = new byte[1024];
    // byte[] b2 = new byte[1024];
    // while ( originalStream.read( b1 ) != -1 ) {
    // if ( resultingStream.read( b2 ) != -1 ) {
    // System.out.println( Arrays.toString( b1 ) );
    // System.out.println( Arrays.toString( b2 ) );
    // if ( !Arrays.equals( b1, b2 ) ) {
    // result = false;
    // break;
    // }
    // } else {
    // result = false;
    // break;
    // }
    // }
    // if ( result ) {
    // if ( originalStream.read( b1 ) != resultingStream.read( b2 ) ) {
    // result = false;
    // }
    // }
    // return result;
    // }

    @Test
    public void testExecuteAsync()
                            throws OWSExceptionReport, IOException, XMLStreamException, InterruptedException {
        String demoWPSURL = TestProperties.getProperty( "demo_wps_url" );
        Assume.assumeNotNull( demoWPSURL );
        WPSClient wpsClient = new WPSClient( new URL( demoWPSURL ) );
        org.deegree.protocol.wps.client.process.Process proc = wpsClient.getProcess( "ParameterDemoProcess", null );

        ProcessExecution execution = proc.prepareExecution();
        execution.addLiteralInput( "LiteralInput", null, "5", "integer", "seconds" );
        execution.addBBoxInput( "BBOXInput", null, new double[] { 0, 0 }, new double[] { 90, 180 }, "EPSG:4326" );
        execution.addXMLInput( "XMLInput", null, CURVE_FILE.toURI(), false, "text/xml", null, null );
        execution.addBinaryInput( "BinaryInput", null, BINARY_INPUT.toURI(), false, "image/png", null );

        execution.executeAsync();
        Assert.assertNotSame( ExecutionState.SUCCEEDED, execution.getState() );
        Assert.assertNotSame( ExecutionState.FAILED, execution.getState() );

        while ( ( execution.getState() ) != ExecutionState.SUCCEEDED ) {
            System.out.println( execution.getPercentCompleted() );
            Thread.sleep( 500 );
        }

        ExecutionOutputs outputs = execution.getOutputs();
        LiteralOutput out1 = (LiteralOutput) outputs.get( "LiteralOutput", null );
        Assert.assertEquals( "5", out1.getValue() );
        Assert.assertEquals( "integer", out1.getDataType() );
        // Assert.assertEquals( "seconds", out1.getUom() );

        BBoxOutput out2 = (BBoxOutput) outputs.get( "BBOXOutput", null );
        Assert.assertTrue( Arrays.equals( new double[] { 0.0, 0.0 }, out2.getLower() ) );
        Assert.assertTrue( Arrays.equals( new double[] { 90.0, 180.0 }, out2.getUpper() ) );
        Assert.assertEquals( "EPSG:4326", out2.getCrs() );
        Assert.assertEquals( 2, out2.getDimension() );
    }

    // @Test
    // public void testExecute_4()
    // throws OWSException, IOException, XMLStreamException {
    // URL processUrl = new URL( NORTH52_SERVICE_URL );
    // WPSClient wpsClient = new WPSClient( processUrl );
    // Process proc = wpsClient.getProcess( "sortraster", null );
    //
    // ProcessExecution execution = proc.prepareExecution();
    // execution.addBinaryInput( "INPUT", null, BINARY_INPUT_TIFF.toURI().toURL(), "image/tiff", null );
    // ExecuteResponse response = execution.start();
    //
    // BinaryDataType out1 = (BinaryDataType) response.getOutputs()[0].getDataType();
    // InputStream inStream = out1.getDataStream();
    // FileOutputStream fileStream = new FileOutputStream( File.createTempFile( "north52", ".tiff" ) );
    // byte[] ar = new byte[1024];
    // int readFlag = -1;
    // while ( ( readFlag = inStream.read( ar ) ) != -1 ) {
    // fileStream.write( ar );
    // }
    // fileStream.close();
    // inStream.close();
    // }
    //
    // @Test
    // public void testExecute_5()
    // throws OWSException, IOException, XMLStreamException {
    // URL processUrl = new URL( NORTH52_SERVICE_URL );
    // WPSClient wpsClient = new WPSClient( processUrl );
    // Process proc = wpsClient.getProcess( "ripleysk", null );
    //
    // ProcessExecution execution = proc.prepareExecution();
    // execution.addXMLInput( "POINTS", null, POINT_FILE.toURI().toURL(), "text/xml", null, null );
    // execution.setRequestedOutput( "RESULT", null, null, false, null, null, null );
    // // execution.addXMLInput( "LAYER2", null, POINT_FILE.toURI().toURL(), "text/xml", null, null );
    // ExecuteResponse response = execution.start();
    //
    // response.getOutputs()[0].getDataType();
    // }

    @Test(expected = OWSExceptionReport.class)
    public void testFailedExecute()
                            throws Exception {
        String demoWPSURL = TestProperties.getProperty( "demo_wps_url" );
        Assume.assumeNotNull( demoWPSURL );

        WPSClient wpsClient = new WPSClient( new URL( demoWPSURL ) );
        Process proc = wpsClient.getProcess( "Centroid", null );
        ProcessExecution execution = proc.prepareExecution();
        // omitting required input parameter
        execution.addOutput( "Centroid", null, null, true, null, null, null );
        execution.execute();
        Assert.assertTrue(execution.getState() != ExecutionState.SUCCEEDED); // we shouldn't arrive here
    }

    @Test(expected = OWSExceptionReport.class)
    public void testFailedExecute_2()
                            throws Exception {
        String demoWPSURL = TestProperties.getProperty( "demo_wps_url" );
        Assume.assumeNotNull( demoWPSURL );

        WPSClient wpsClient = new WPSClient( new URL( demoWPSURL ) );
        Process proc = wpsClient.getProcess( "Centroid" );
        ProcessExecution execution = proc.prepareExecution();
        // adding invalid input parameter
        execution.addLiteralInput( "ThisDoesNotExist", null, "5", "sortOfInteger", "reallyBigUnit" );
        execution.executeAsync();
        Assert.assertTrue(execution.getState() != ExecutionState.SUCCEEDED); // we shouldn't arrive here
    }

    @Test
    public void testURIInput()
                            throws MalformedURLException, OWSExceptionReport, IOException, XMLStreamException,
                            InterruptedException, URISyntaxException {

        String demoWPSURL = TestProperties.getProperty( "demo_wps_authentication_url" );
        String demoWPSProcessName = TestProperties.getProperty( "demo_wps_authentication_process_name" );
        String demoWPSInputParam = TestProperties.getProperty( "demo_wps_input_uri" );

        Assume.assumeNotNull( demoWPSURL );
        Assume.assumeNotNull( demoWPSProcessName );
        Assume.assumeNotNull( demoWPSInputParam );

        URL serviceUrl = new URL( demoWPSURL );
        URI inputUri = new URI( demoWPSInputParam );

        WPSClient wpsClient = new WPSClient( serviceUrl );

        Process proc = wpsClient.getProcess( demoWPSProcessName, null );
        ProcessExecution execution = proc.prepareExecution();

        execution.addBinaryInput( "infile", null, inputUri, true, null, null );

        execution.execute();

        while ( execution.getState() != ExecutionState.SUCCEEDED && execution.getState() != ExecutionState.FAILED ) {
            System.out.println( String.format( "%s, %d, %s", execution.getState(), execution.getPercentCompleted(),
                                               execution.getStatusLocation() ) );
            Thread.sleep( 500 );
        }

        ExecutionOutputs outputs = execution.getOutputs();
        Assert.assertTrue( outputs.getAll().length > 0 );
    }
}
