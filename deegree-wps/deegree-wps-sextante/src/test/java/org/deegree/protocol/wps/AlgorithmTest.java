package org.deegree.protocol.wps;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.stream.XMLStreamException;

import org.deegree.protocol.wps.process.Process;
import org.deegree.protocol.wps.process.ProcessExecution;
import org.deegree.protocol.wps.process.execute.ExecutionOutputs;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.wps.provider.SextanteProcessProvider;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.Parameter;

public class AlgorithmTest {

    private static final boolean ENABLED = false;

    private static Logger LOG = LoggerFactory.getLogger( AlgorithmTest.class );

    @Test
    public void testVectorLayerAlgorithms()
                            throws OWSException, IOException, XMLStreamException {
        if ( ENABLED ) {
            try {
                URL wpsURL = new URL(
                                      "http://localhost:8080/deegree-wps-demo/services?service=WPS&version=1.0.0&request=GetCapabilities" );

                WPSClient client = new WPSClient( wpsURL );

                Assert.assertNotNull( client );

                File gmlPoints = new File( WPSClientTest.class.getResource( "GML31_MultiPoint.xml" ).getPath() );
                File gmlLines = new File( WPSClientTest.class.getResource( "GML31_MultiLineString.xml" ).getPath() );
                File gmlPolygons = new File( WPSClientTest.class.getResource( "GML31_MultiPolygon.xml" ).getPath() );

                // Process process = client.getProcess( "centroids", null );
                // ProcessExecution execution = process.prepareExecution();
                // execution.addXMLInput( "LAYER", null, gmlGeometry.toURI().toURL(), false, "text/xml", "UTF-8",
                // "http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd" );
                //
                // execution.addOutput( "RESULT", null, null, false, "text/xml", "UTF-8",
                // "http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd" );
                //
                // ExecutionOutputs response = execution.execute();
                // Assert.assertNotNull( response );

                GeoAlgorithm[] algs = SextanteProcessProvider.getVectorLayerInAndOutAlgorithms();

                // process all vector algorithms
                for ( int i = 0; i < algs.length; i++ ) {
                    GeoAlgorithm alg = algs[i];

                    Process process = client.getProcess( alg.getCommandLineName(), null );
                    ProcessExecution execution = process.prepareExecution();

                    // add all inputs
                    ParametersSet paramSet = alg.getParameters();
                    for ( int j = 0; j < paramSet.getNumberOfParameters(); j++ ) {
                        Parameter param = paramSet.getParameter( j );

                        String inputIndentifier = param.getParameterName();

                        File geom = null;

                        if ( inputIndentifier.equals( "POINTS" ) )
                            geom = gmlPoints;
                        else if ( inputIndentifier.equals( "LINES" ) )
                            geom = gmlLines;
                        else
                            geom = gmlLines;

                        execution.addXMLInput( inputIndentifier, null, geom.toURI().toURL(), false, "text/xml",
                                               "UTF-8",
                                               "http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd" );
                    }

                    // set all output parameters
                    OutputObjectsSet outputSet = alg.getOutputObjects();
                    for ( int j = 0; j < outputSet.getOutputObjectsCount(); j++ ) {
                        Output outp = outputSet.getOutput( j );

                        String outputIdentifier = outp.getName();

                        execution.addOutput( outputIdentifier, null, null, false, "text/xml", "UTF-8",
                                             "http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd" );

                    }

                    ExecutionOutputs outputs = execution.execute();

                }

                // execution
                // .addXMLInput("GMLInput", null, gmlPoints.toURI().toURL(),
                // false, "text/xml", null,
                // "http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd");

                // execution.addOutput("CentroidSextante", null, null, true, null, null,
                // null);

                // ExecutionOutputs outputs = execution.execute();

                // access individual output values
                // ComplexOutput outputGeometry = (ComplexOutput) outputs.get(
                // "CentroidSextante", null);

                // LiteralOutput out = outputs.getLiteral("Data", null);

                // XMLStreamReader xmlStream = outputGeometry.getAsXMLStream();

                // System.out.println(out.getValue());
            } catch ( Throwable t ) {
                LOG.error( t.getMessage(), t );
            }
        }
    }
}
