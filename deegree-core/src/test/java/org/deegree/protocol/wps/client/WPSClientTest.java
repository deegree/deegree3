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

import static com.vividsolutions.jts.io.gml2.GMLConstants.GML_NAMESPACE;
import static com.vividsolutions.jts.io.gml2.GMLConstants.GML_PREFIX;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.ows.metadata.ContactInfo;
import org.deegree.protocol.ows.metadata.Operation;
import org.deegree.protocol.ows.metadata.OperationsMetadata;
import org.deegree.protocol.ows.metadata.ServiceContact;
import org.deegree.protocol.ows.metadata.ServiceIdentification;
import org.deegree.protocol.ows.metadata.ServiceProvider;
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
import org.junit.Before;
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

    private static final File POLYGON_FILE = new File( WPSClientTest.class.getResource( "Polygon.gml" ).getPath() );

    private static final File POINT_FILE = new File( WPSClientTest.class.getResource( "Point_coord.gml" ).getPath() );

    private static final File BINARY_INPUT = new File( WPSClientTest.class.getResource( "image.png" ).getPath() );

    private static final File BINARY_INPUT_TIFF = new File( WPSClientTest.class.getResource( "image.tiff" ).getPath() );

    private static final String DEMO_SERVICE_URL = "http://deegree3-testing.deegree.org/deegree-wps-demo/services?service=WPS&version=1.0.0&request=GetCapabilities";

    private static final String NORTH52_SERVICE_URL = "http://giv-wps.uni-muenster.de:8080/wps/WebProcessingService?Request=GetCapabilities&Service=WPS";

    private static final String REMOTE_XML_INPUT = "http://demo.deegree.org/deegree-wfs/services?REQUEST=GetCapabilities&version=1.1.0&service=WFS";

    private static final String REMOTE_BINARY_INPUT = "http://www.deegree.org/deegree/images/deegree/logo-deegree.png";

    @Before
    public void init() {
        if ( DEMO_SERVICE_URL == null ) {
            throw new RuntimeException( "Cannot proceed: Service URL not provided." );
        }
    }

    @Test
    public void testMetadata()
                            throws OWSException, IOException {
        URL serviceUrl = new URL( DEMO_SERVICE_URL );
        WPSClient client = new WPSClient( serviceUrl );
        Assert.assertNotNull( client );
        ServiceIdentification serviceId = client.getMetadata().getServiceIdentification();
        Assert.assertNotNull( serviceId );
        Assert.assertEquals( serviceId.getDescription().getTitle().size(), 1 );
        Assert.assertEquals( serviceId.getDescription().getTitle().get( 0 ).getString(), "deegree 3 WPS" );
        Assert.assertEquals( serviceId.getDescription().getAbstract().size(), 1 );
        Assert.assertEquals( serviceId.getDescription().getAbstract().get( 0 ).getString(),
                             "deegree 3 WPS implementation" );

        Assert.assertEquals( serviceId.getServiceType().getCode(), "WPS" );
        Assert.assertEquals( serviceId.getServiceTypeVersion().get( 0 ).toString(), "1.0.0" );

        ServiceProvider serviceProvider = client.getMetadata().getServiceProvider();
        Assert.assertEquals( serviceProvider.getProviderName(), "lat-lon GmbH" );
        Assert.assertEquals( serviceProvider.getProviderSite().toExternalForm(), "http://www.lat-lon.de" );

        ServiceContact serviceContact = serviceProvider.getServiceContact();
        Assert.assertEquals( serviceContact.getIndividualName(), "Christian Kiehle" );
        Assert.assertEquals( serviceContact.getPositionName(), "Project Manager" );

        ContactInfo contactInfo = serviceContact.getContactInfo();
        Assert.assertEquals( contactInfo.getPhone().getVoice().get( 0 ), "0228/18496-0" );
        Assert.assertEquals( contactInfo.getPhone().getFacsimile().get( 0 ), "0228/18496-29" );
        Assert.assertEquals( contactInfo.getAddress().getDeliveryPoint().get( 0 ), "Aennchenstr. 19" );
        Assert.assertEquals( contactInfo.getAddress().getCity(), "Bonn" );
        Assert.assertEquals( contactInfo.getAddress().getAdministrativeArea(), "NRW" );
        Assert.assertEquals( contactInfo.getAddress().getPostalCode(), "53177" );
        Assert.assertEquals( contactInfo.getAddress().getCountry(), "Bonn" );
        Assert.assertEquals( contactInfo.getAddress().getElectronicMailAddress().get( 0 ).trim(), "kiehle@lat-lon.de" );
        Assert.assertEquals( contactInfo.getOnlineResource().toExternalForm(), "http://www.deegree.org" );
        Assert.assertEquals( contactInfo.getHoursOfService(), "24x7" );
        Assert.assertEquals( contactInfo.getContactInstruction(), "Don't hesitate to call" );

        Assert.assertEquals( serviceContact.getRole().getCode(), "PointOfContact" );

        OperationsMetadata opMetadata = client.getMetadata().getOperationsMetadata();
        Operation op = opMetadata.getOperation().get( 0 ); // GetCapabilities
        Assert.assertEquals( op.getDCP().get( 0 ).getGetURLs().get( 0 ).first.toExternalForm(),
                             "http://deegree3-testing.deegree.org/deegree-wps-demo/services?" );
        Assert.assertEquals( op.getDCP().get( 0 ).getPostURLs().get( 0 ).first.toExternalForm(),
                             "http://deegree3-testing.deegree.org/deegree-wps-demo/services" );
        op = opMetadata.getOperation().get( 1 ); // DescribeProcess
        Assert.assertEquals( op.getDCP().get( 0 ).getGetURLs().get( 0 ).first.toExternalForm(),
                             "http://deegree3-testing.deegree.org/deegree-wps-demo/services?" );
        Assert.assertEquals( op.getDCP().get( 0 ).getPostURLs().get( 0 ).first.toExternalForm(),
                             "http://deegree3-testing.deegree.org/deegree-wps-demo/services" );
        op = opMetadata.getOperation().get( 2 ); // Execute
        Assert.assertEquals( op.getDCP().get( 0 ).getGetURLs().get( 0 ).first.toExternalForm(),
                             "http://deegree3-testing.deegree.org/deegree-wps-demo/services?" );
        Assert.assertEquals( op.getDCP().get( 0 ).getPostURLs().get( 0 ).first.toExternalForm(),
                             "http://deegree3-testing.deegree.org/deegree-wps-demo/services" );
    }

    @Test
    public void testProcessDescription_1()
                            throws OWSException, IOException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
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
        Assert.assertEquals( "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd",
                             complexData.getDefaultFormat().getSchema() );
        Assert.assertEquals( "UTF-8", complexData.getSupportedFormats()[0].getEncoding() );
        Assert.assertEquals( "text/xml", complexData.getSupportedFormats()[0].getMimeType() );
        Assert.assertEquals( "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd",
                             complexData.getSupportedFormats()[0].getSchema() );
    }

    @Test
    public void testProcessDescription_2()
                            throws OWSException, IOException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
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
                            throws OWSException, IOException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
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
                            throws OWSException, IOException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        org.deegree.protocol.wps.client.process.Process p1 = wpsClient.getProcess( "Buffer", null );
        Assert.assertNotNull( p1 );
        org.deegree.protocol.wps.client.process.Process p2 = wpsClient.getProcess( "ParameterDemoProcess", null );
        Assert.assertNotNull( p2 );
    }

    @Test
    public void testExecute_1()
                            throws Exception {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );

        org.deegree.protocol.wps.client.process.Process proc = wpsClient.getProcess( "Centroid", null );
        ProcessExecution execution = proc.prepareExecution();
        execution.addXMLInput( "GMLInput", null, CURVE_FILE.toURI().toURL(), false, "text/xml", null, null );
        execution.addOutput( "Centroid", null, null, true, null, null, null );
        ExecutionOutputs response = execution.execute();
        assertEquals( ExecutionState.SUCCEEDED, execution.getState() );

        ComplexOutput output = (ComplexOutput) response.get( 0 );
        XMLStreamReader reader = output.getAsXMLStream();
        XMLAdapter searchableXML = new XMLAdapter( reader );
        NamespaceContext nsContext = new NamespaceContext();
        nsContext.addNamespace( "wps", WPSConstants.WPS_100_NS );
        nsContext.addNamespace( "gml", "http://www.opengis.net/gml" );
        XPath xpath = new XPath( "/gml:Point/gml:pos/text()", nsContext );
        String pos = searchableXML.getRequiredNodeAsString( searchableXML.getRootElement(), xpath );

        String[] pair = pos.split( "\\s" );
        Assert.assertEquals( -0.31043, Double.parseDouble( pair[0] ), 1E-5 );
        Assert.assertEquals( 0.56749, Double.parseDouble( pair[1] ), 1E-5 );
    }

    @Test
    public void testExecute_2()
                            throws Exception {

        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );

        org.deegree.protocol.wps.client.process.Process proc = wpsClient.getProcess( "Buffer", null );

        ProcessExecution execution = proc.prepareExecution();
        execution.addLiteralInput( "BufferDistance", null, "0.1", "double", "unity" );
        execution.addXMLInput( "GMLInput", null, CURVE_FILE.toURI().toURL(), false, "text/xml", null, null );
        execution.addOutput( "BufferedGeometry", null, null, false, null, null, null );
        ExecutionOutputs outputs = execution.execute();

        ComplexOutput complexOut = outputs.getComplex( "BufferedGeometry", null );
        XMLAdapter searchableXML = new XMLAdapter( complexOut.getAsXMLStream() );
        String xpathStr = "/gml:Polygon/gml:exterior/gml:LinearRing/gml:posList";
        NamespaceContext nsContext = new NamespaceContext();
        nsContext.addNamespace( GML_PREFIX, GML_NAMESPACE );
        XPath xpath = new XPath( xpathStr, nsContext );
        String pointList = searchableXML.getRequiredNodeAsString( searchableXML.getRootElement(), xpath );
        Assert.assertEquals( 670, pointList.split( "\\s" ).length );
    }

    @Test
    public void testExecute_3()
                            throws OWSException, IOException, XMLStreamException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        org.deegree.protocol.wps.client.process.Process proc = wpsClient.getProcess( "ParameterDemoProcess" );

        ProcessExecution execution = proc.prepareExecution();
        execution.addLiteralInput( "LiteralInput", null, "0", "integer", "seconds" );
        execution.addBBoxInput( "BBOXInput", null, new double[] { 0, 0 }, new double[] { 90, 180 }, "EPSG:4326" );
        execution.addXMLInput( "XMLInput", null, CURVE_FILE.toURI().toURL(), false, "text/xml", null, null );
        execution.addBinaryInput( "BinaryInput", null, BINARY_INPUT.toURI().toURL(), false, "image/png", null );
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
                            throws OWSException, IOException, XMLStreamException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        org.deegree.protocol.wps.client.process.Process proc = wpsClient.getProcess( "ParameterDemoProcess", null );

        ProcessExecution execution = proc.prepareExecution();
        execution.addLiteralInput( "LiteralInput", null, "0", "integer", "seconds" );
        execution.addBBoxInput( "BBOXInput", null, new double[] { 0, 0 }, new double[] { 90, 180 }, "EPSG:4326" );
        execution.addXMLInput( "XMLInput", null, CURVE_FILE.toURI().toURL(), false, "text/xml", null, null );
        execution.addBinaryInput( "BinaryInput", null, BINARY_INPUT.toURI().toURL(), false, "image/png", null );
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
                            throws OWSException, IOException, XMLStreamException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        org.deegree.protocol.wps.client.process.Process proc = wpsClient.getProcess( "ParameterDemoProcess", null );

        RawProcessExecution execution = proc.prepareRawExecution();
        execution.addLiteralInput( "LiteralInput", null, "0", "integer", "seconds" );
        execution.addBBoxInput( "BBOXInput", null, new double[] { 0, 0 }, new double[] { 90, 180 }, "EPSG:4326" );
        execution.addXMLInput( "XMLInput", null, CURVE_FILE.toURI().toURL(), false, "text/xml", null, null );
        execution.addBinaryInput( "BinaryInput", null, BINARY_INPUT.toURI().toURL(), false, "image/png", null );
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
                            throws OWSException, IOException, XMLStreamException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        org.deegree.protocol.wps.client.process.Process proc = wpsClient.getProcess( "ParameterDemoProcess", null );

        ProcessExecution execution = proc.prepareExecution();
        execution.addLiteralInput( "LiteralInput", null, "0", "integer", "seconds" );
        execution.addBBoxInput( "BBOXInput", null, new double[] { 0, 0 }, new double[] { 90, 180 }, "EPSG:4326" );
        execution.addXMLInput( "XMLInput", null, new URL( REMOTE_XML_INPUT ), true, "text/xml", null, null );
        execution.addBinaryInput( "BinaryInput", null, new URL( REMOTE_BINARY_INPUT ), true, "image/png", null );
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
        NamespaceContext nsContext = new NamespaceContext();
        nsContext.addNamespace( "wfs", WFS_NS );
        XPath xpath = new XPath( "/wfs:WFS_Capabilities/wfs:FeatureTypeList/wfs:FeatureType[1]/wfs:Name", nsContext );
        String pos = searchableXML.getRequiredNodeAsString( searchableXML.getRootElement(), xpath );
        Assert.assertEquals( "app:Springs", pos );

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
                            throws OWSException, IOException, XMLStreamException, InterruptedException {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        org.deegree.protocol.wps.client.process.Process proc = wpsClient.getProcess( "ParameterDemoProcess", null );

        ProcessExecution execution = proc.prepareExecution();
        execution.addLiteralInput( "LiteralInput", null, "5", "integer", "seconds" );
        execution.addBBoxInput( "BBOXInput", null, new double[] { 0, 0 }, new double[] { 90, 180 }, "EPSG:4326" );
        execution.addXMLInput( "XMLInput", null, CURVE_FILE.toURI().toURL(), false, "text/xml", null, null );
        execution.addBinaryInput( "BinaryInput", null, BINARY_INPUT.toURI().toURL(), false, "image/png", null );

        execution.executeAsync();
        Assert.assertNotSame( ExecutionState.SUCCEEDED, execution.getState() );
        Assert.assertNotSame( ExecutionState.FAILED, execution.getState() );

        ExecutionState state = null;
        while ( ( state = execution.getState() ) != ExecutionState.SUCCEEDED ) {
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

    @Test(expected = OWSException.class)
    public void testFailedExecute()
                            throws Exception {
        URL processUrl = new URL( DEMO_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );

        Process proc = wpsClient.getProcess( "Centroid", null );
        ProcessExecution execution = proc.prepareExecution();
        // omitting required input parameter
        execution.addOutput( "Centroid", null, null, true, null, null, null );
        ExecutionOutputs response = execution.execute();
        assertEquals( ExecutionState.SUCCEEDED, execution.getState() );
    }
}
