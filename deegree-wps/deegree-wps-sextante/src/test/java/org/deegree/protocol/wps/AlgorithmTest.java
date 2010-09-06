package org.deegree.protocol.wps;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.deegree.protocol.wps.ExampleData.ExampleDataType;
import org.deegree.protocol.wps.client.WPSClient;
import org.deegree.protocol.wps.client.output.ExecutionOutput;
import org.deegree.protocol.wps.client.process.ProcessExecution;
import org.deegree.protocol.wps.client.process.Process;
import org.deegree.protocol.wps.client.process.execute.ExecutionOutputs;
import org.deegree.services.wps.provider.SextanteProcessProvider;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.Parameter;

public class AlgorithmTest {

    private static final boolean ENABLED = true;

    private static Logger LOG = LoggerFactory.getLogger( AlgorithmTest.class );

    private LinkedList<TestAlgorithm> getAlgorithms() {
        LinkedList<TestAlgorithm> algs = new LinkedList<TestAlgorithm>();

        // test all algorithms?
        boolean all = true;

        if ( !all ) {
            // only one algorithm
            Sextante.initialize();
            HashMap<String, GeoAlgorithm> sextanteAlgs = Sextante.getAlgorithms();
            GeoAlgorithm geoAlg = sextanteAlgs.get( "countpoints" );
            TestAlgorithm testAlg = new TestAlgorithm( geoAlg );
            testAlg.addAllInputData( getInputData( geoAlg ) );
            algs.add( testAlg );

        } else {

            // all vector algorithms
            GeoAlgorithm[] geoalgs = SextanteProcessProvider.getVectorLayerAlgorithms();
            for ( int i = 0; i < geoalgs.length; i++ ) {
                GeoAlgorithm geoAlg = geoalgs[i];

                if ( !geoAlg.getCommandLineName().equals( "no algorithm" ) ) {

                    TestAlgorithm testAlg = new TestAlgorithm( geoAlg );
                    testAlg.addAllInputData( getInputData( geoAlg ) );
                    algs.add( testAlg );

                }

            }
        }

        return algs;
    }

    private LinkedList<LinkedList<ExampleData>> getInputData( GeoAlgorithm alg ) {

        // all input data
        LinkedList<LinkedList<ExampleData>> allData = new LinkedList<LinkedList<ExampleData>>();

        // example data in categories
        LinkedList<ExampleData> layers = getLayerInput( alg );
        LinkedList<ExampleData> poylgons = getPolygonsInput();
        LinkedList<ExampleData> lines = getLinesInput();
        LinkedList<ExampleData> points = getPointsInput();

        // example data iterators
        Iterator<ExampleData> layersIterator = layers.iterator();
        Iterator<ExampleData> linesIterator = lines.iterator();
        Iterator<ExampleData> poylgonsIterator = poylgons.iterator();
        Iterator<ExampleData> pointsIterator = points.iterator();

        // traverse input parameter of one execution
        ParametersSet paramSet = alg.getParameters();

        // traverse all example data
        boolean addMoreData = true;
        while ( addMoreData ) {

            // input data of one execution
            LinkedList<ExampleData> dataList = new LinkedList<ExampleData>();

            for ( int j = 0; j < paramSet.getNumberOfParameters(); j++ ) {
                Parameter param = paramSet.getParameter( j );

                // add a layer
                if ( param.getParameterName().equals( "LAYER" ) || param.getParameterName().equals( "INPUT" )
                     || param.getParameterName().equals( "CLIPLAYER" ) || param.getParameterName().equals( "LAYER2" )
                     || param.getParameterName().equals( "LAYER1" ) ) {

                    if ( layersIterator.hasNext() ) {
                        dataList.add( layersIterator.next() );
                    } else {
                        addMoreData = false;
                        break;
                    }

                } else {
                    // add lines
                    if ( param.getParameterName().equals( "LINES" ) ) {
                        if ( linesIterator.hasNext() ) {
                            dataList.add( linesIterator.next() );
                        } else {
                            addMoreData = false;
                            break;
                        }

                    } else {
                        // add points
                        if ( param.getParameterName().equals( "POINTS" ) ) {
                            if ( pointsIterator.hasNext() ) {
                                dataList.add( pointsIterator.next() );
                            } else {
                                addMoreData = false;
                                break;
                            }

                        } else {

                            // add polygons
                            if ( param.getParameterName().equals( "POLYGONS" ) ) {
                                if ( poylgonsIterator.hasNext() ) {
                                    dataList.add( poylgonsIterator.next() );
                                } else {
                                    addMoreData = false;
                                    break;
                                }

                            } else {

                                // add nothing
                                LOG.error( "Unknown input parameter \"" + param.getParameterName()
                                           + "\" of SEXTANTE algorithm." );
                            }
                        }
                    }
                }
            }

            allData.add( dataList );
        }

        return allData;
    }

    private LinkedList<ExampleData> getLayerInput( GeoAlgorithm alg ) {
        LinkedList<ExampleData> layers = new LinkedList<ExampleData>();

        // cleanpointslayer algorithm
        if ( alg.getCommandLineName().equals( "cleanpointslayer" ) )
            layers.addAll( ExampleData.getData( ExampleDataType.POINT ) );
        else
        // polylinestopolygons algorithm
        if ( alg.getCommandLineName().equals( "polylinestopolygons" ) )
            layers.addAll( ExampleData.getData( ExampleDataType.LINE ) );
        else
        // pointcoordinates algorithm
        if ( alg.getCommandLineName().equals( "pointcoordinates" ) )
            layers.addAll( ExampleData.getData( ExampleDataType.POINT ) );
        else
        // removeholes algorithm
        if ( alg.getCommandLineName().equals( "removeholes" ) )
            layers.addAll( ExampleData.getData( ExampleDataType.POLYGON ) );
        else
        // exportvector algorithm
        if ( alg.getCommandLineName().equals( "exportvector" ) )
            LOG.warn( "No test data for '" + alg.getCommandLineName() + "' available." );
        else
        // polygonize algorithm
        if ( alg.getCommandLineName().equals( "polygonize" ) ) {
            layers.add( ExampleData.GML_31_MULTILINESTRING );
            layers.add( ExampleData.GML_31_FEATURE_COLLECTION_MULTILINESTRINGS );
        } else
        // difference and intersection algorithm
        if ( alg.getCommandLineName().equals( "difference" ) || alg.getCommandLineName().equals( "intersection" ) ) {
            layers.addAll( ExampleData.getData( ExampleDataType.POLYGON ) );
            layers.addAll( ExampleData.getData( ExampleDataType.LINE ) );
            layers.addAll( ExampleData.getData( ExampleDataType.POINT ) );
        } else
        // union algorithm
        if ( alg.getCommandLineName().equals( "union" ) ) {
            LOG.warn( "No test data for '" + alg.getCommandLineName() + "' available." );
        } else
        // clip algorithm
        if ( alg.getCommandLineName().equals( "clip" ) ) {
            layers.add( ExampleData.GML_31_POINT );
            layers.add( ExampleData.GML_31_MULTILPOLYGON );

            layers.add( ExampleData.GML_31_LINESTRING );
            layers.add( ExampleData.GML_31_MULTILINESTRING );

            layers.add( ExampleData.GML_31_MULTILPOLYGON );
            layers.add( ExampleData.GML_31_LINESTRING );

            layers.add( ExampleData.GML_31_FEATURE_COLLECTION_MULTIPOINTS );
            layers.add( ExampleData.GML_31_FEATURE_COLLECTION_POINTS );

            layers.add( ExampleData.GML_31_FEATURE_COLLECTION_LINESTRINGS );
            layers.add( ExampleData.GML_31_FEATURE_COLLECTION_MULTILINESTRINGS );

            layers.add( ExampleData.GML_31_FEATURE_COLLECTION_MULTIPOLYGONS );
            layers.add( ExampleData.GML_31_FEATURE_COLLECTION_POLYGONS );
        } else
        // symdifference algorithm
        if ( alg.getCommandLineName().equals( "symdifference" ) ) {
            layers.addAll( ExampleData.getData( ExampleDataType.POLYGON ) );
        } else
        // countpoints algorithm
        if ( alg.getCommandLineName().equals( "countpoints" ) ) {

            layers.add( ExampleData.GML_31_POLYGON );
            layers.add( ExampleData.GML_31_POINT );

            // TODO why the algorithm returns a IVectorLayer and not a ITable?

        } else
            // all algorithms
            layers.addAll( ExampleData.getAllData() );

        return layers;
    }

    private LinkedList<ExampleData> getPolygonsInput() {
        LinkedList<ExampleData> polygons = new LinkedList<ExampleData>();
        polygons.addAll( ExampleData.getData( ExampleDataType.POLYGON ) );
        return polygons;
    }

    private LinkedList<ExampleData> getLinesInput() {
        LinkedList<ExampleData> lines = new LinkedList<ExampleData>();
        lines.addAll( ExampleData.getData( ExampleDataType.LINE ) );
        lines.addAll( ExampleData.getData( ExampleDataType.POLYGON ) );
        return lines;
    }

    private LinkedList<ExampleData> getPointsInput() {
        LinkedList<ExampleData> points = new LinkedList<ExampleData>();
        points.add( ExampleData.GML_31_FEATURE_COLLECTION_POINTS );
        return points;
    }

    @Test
    public void testAlgorithms() {
        if ( ENABLED ) {
            try {

                // client
                URL wpsURL = new URL(
                                      "http://localhost:9080/deegree-wps-demo/services?service=WPS&version=1.0.0&request=GetCapabilities" );
                WPSClient client = new WPSClient( wpsURL );
                Assert.assertNotNull( client );

                // all algorithms
                LinkedList<TestAlgorithm> algs = getAlgorithms();

                // traverse all algorithms
                for ( TestAlgorithm algTest : algs ) {

                    // geoalgorithm
                    GeoAlgorithm alg = algTest.getAlgorithm();

                    // input data
                    LinkedList<LinkedList<ExampleData>> allData = algTest.getAllInputData();
                    for ( LinkedList<ExampleData> data : allData ) {

                        // set input data for one execution
                        ParametersSet paramSet = alg.getParameters();
                        if ( data.size() > 0 )
                            if ( data.size() == paramSet.getNumberOfParameters() ) {
                                Process process = client.getProcess( alg.getCommandLineName() );
                                ProcessExecution execution = process.prepareExecution();

                                Iterator<ExampleData> it = data.iterator();

                                for ( int j = 0; j < paramSet.getNumberOfParameters(); j++ ) {
                                    ExampleData currentData = it.next();

                                    Parameter param = paramSet.getParameter( j );

                                    String inputIndentifier = param.getParameterName();

                                    LOG.info( "Testing '" + alg.getCommandLineName() + "' algorithm with "
                                              + currentData.getFilename() );

                                    execution.addXMLInput( inputIndentifier, null, currentData.getURL(), false,
                                                           currentData.getMimeType(), currentData.getEncoding(),
                                                           currentData.getSchema() );
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
                                ExecutionOutput[] allOutputs = outputs.getAll();

                                // check number of output output objects
                                Assert.assertTrue( allOutputs.length > 0 );

                                // LOG.info( " '" + alg.getCommandLineName() + "' has " + allOutputs.length
                                // + " ouput objects." );

                            } else {
                                LOG.error( "Wrong number of input data." );
                            }

                    }
                }

            } catch ( Throwable t ) {
                LOG.error( t.getMessage(), t );
                Assert.fail( t.getLocalizedMessage() );
            }
        }
    }

    //
    // public void testVectorLayerAlgorithms()
    // throws OWSException, IOException, XMLStreamException {
    // if ( ENABLED ) {
    // try {
    // URL wpsURL = new URL(
    // "http://localhost:8080/deegree-wps-demo/services?service=WPS&version=1.0.0&request=GetCapabilities" );
    //
    // WPSClient client = new WPSClient( wpsURL );
    //
    // Assert.assertNotNull( client );
    //
    // File gmlPoints = new File( AlgorithmTest.class.getResource( "GML31_MultiPoint.xml" ).getPath() );
    // File gmlLines = new File( AlgorithmTest.class.getResource( "GML31_MultiLineString.xml" ).getPath() );
    // File gmlPolygons = new File( AlgorithmTest.class.getResource( "GML31_MultiPolygon.xml" ).getPath() );
    //
    // // Process process = client.getProcess( "centroids", null );
    // // ProcessExecution execution = process.prepareExecution();
    // // execution.addXMLInput( "LAYER", null, gmlGeometry.toURI().toURL(), false, "text/xml", "UTF-8",
    // // "http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd" );
    // //
    // // execution.addOutput( "RESULT", null, null, false, "text/xml", "UTF-8",
    // // "http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd" );
    // //
    // // ExecutionOutputs response = execution.execute();
    // // Assert.assertNotNull( response );
    //
    // GeoAlgorithm[] algs = SextanteProcessProvider.getVectorLayerAlgorithms();
    //
    // // process all vector algorithms
    // for ( int i = 0; i < algs.length; i++ ) {
    // GeoAlgorithm alg = algs[i];
    //
    // Process process = client.getProcess( alg.getCommandLineName() );
    // ProcessExecution execution = process.prepareExecution();
    //
    // // add all inputs
    // ParametersSet paramSet = alg.getParameters();
    // for ( int j = 0; j < paramSet.getNumberOfParameters(); j++ ) {
    // Parameter param = paramSet.getParameter( j );
    //
    // String inputIndentifier = param.getParameterName();
    //
    // File geom = null;
    //
    // if ( inputIndentifier.equals( "POINTS" ) )
    // geom = gmlPoints;
    // else if ( inputIndentifier.equals( "LINES" ) )
    // geom = gmlLines;
    // else
    // geom = gmlLines;
    //
    // execution.addXMLInput( inputIndentifier, null, geom.toURI().toURL(), false, "text/xml",
    // "UTF-8",
    // "http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd" );
    // }
    //
    // // set all output parameters
    // OutputObjectsSet outputSet = alg.getOutputObjects();
    // for ( int j = 0; j < outputSet.getOutputObjectsCount(); j++ ) {
    // Output outp = outputSet.getOutput( j );
    //
    // String outputIdentifier = outp.getName();
    //
    // execution.addOutput( outputIdentifier, null, null, false, "text/xml", "UTF-8",
    // "http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd" );
    //
    // }
    //
    // ExecutionOutputs outputs = execution.execute();
    //
    // }
    //
    // // execution
    // // .addXMLInput("GMLInput", null, gmlPoints.toURI().toURL(),
    // // false, "text/xml", null,
    // // "http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd");
    //
    // // execution.addOutput("CentroidSextante", null, null, true, null, null,
    // // null);
    //
    // // ExecutionOutputs outputs = execution.execute();
    //
    // // access individual output values
    // // ComplexOutput outputGeometry = (ComplexOutput) outputs.get(
    // // "CentroidSextante", null);
    //
    // // LiteralOutput out = outputs.getLiteral("Data", null);
    //
    // // XMLStreamReader xmlStream = outputGeometry.getAsXMLStream();
    //
    // // System.out.println(out.getValue());
    // } catch ( Throwable t ) {
    // LOG.error( t.getMessage(), t );
    // }
    // }
    // }
}
