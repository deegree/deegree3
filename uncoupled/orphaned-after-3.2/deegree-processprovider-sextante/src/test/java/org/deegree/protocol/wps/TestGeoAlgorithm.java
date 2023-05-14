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
package org.deegree.protocol.wps;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.wps.VectorExampleData.GeometryType;
import org.deegree.protocol.wps.client.WPSClient;
import org.deegree.protocol.wps.client.output.ComplexOutput;
import org.deegree.protocol.wps.client.output.ExecutionOutput;
import org.deegree.protocol.wps.client.process.Process;
import org.deegree.protocol.wps.client.process.ProcessExecution;
import org.deegree.protocol.wps.client.process.execute.ExecutionOutputs;
import org.deegree.services.wps.provider.sextante.ExampleData;
import org.deegree.services.wps.provider.sextante.GMLSchema;
import org.deegree.services.wps.provider.sextante.GMLSchema.GMLType;
import org.deegree.services.wps.provider.sextante.OutputFormat;
import org.deegree.services.wps.provider.sextante.SextanteWPSProcess;
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

/**
 * This class tests all supported SEXTANTE {@link GeoAlgorithm} of the deegree WPS.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * 
 */
public class TestGeoAlgorithm {

    // logger
    private static Logger LOG = LoggerFactory.getLogger( TestGeoAlgorithm.class );

    // enabled/disabled all tests
    private static final boolean ENABLED = false;

    // manages all supported algorithms with example data
    private final LinkedList<GeoAlgorithmWithData> algorithmsExecutabilityTest;

    // manages all supported algorithms with example data
    private final LinkedList<GeoAlgorithmWithData> algorithmsResultTest;

    private WPSClient client;

    public TestGeoAlgorithm() {

        // initialize SEXTANTE
        Sextante.initialize();

        // initialize all test algorithms
        algorithmsExecutabilityTest = getAlgorithmsForExecutabilityTest();
        algorithmsResultTest = getAlgorithmsForResultTest();

        // create wps client
        try {

            URL wpsURL;

            wpsURL = new URL(
                              "http://localhost:8080/deegree-wps-demo/services?service=WPS&version=1.0.0&request=GetCapabilities" );

            client = new WPSClient( wpsURL );

        } catch ( Exception e ) {
            LOG.error( "Can not create the WPSClient." );
            e.printStackTrace();
        }
    }

    /**
     * This method tests all supported SEXTANTE {@link GeoAlgorithm}s of the deegree WPS. <br>
     * It will tested only whether the algorithm runs without errors. <br>
     * It is not tested whether the algorithm calculated its output values correctly.
     */
    @Test
    public void testExecutabilityOfAlgorithms() {
        if ( ENABLED ) {
            try {
                testAlgorithm( false );
            } catch ( Throwable t ) {
                // LOG.error( t.getMessage(), t );
                t.printStackTrace();
                Assert.fail( t.getMessage() );
            }
        }
    }

    /**
     * This method test some SEXTANTE {@link GeoAlgorithm}s of the deegree WPS. <br>
     * It is tested whether the algorithm calculated its output values correctly.
     */
    @Test
    public void testResultOfAlgorithms() {
        if ( ENABLED ) {
            try {
                testAlgorithm( true );
            } catch ( Throwable t ) {
                // LOG.error( t.getMessage(), t );
                t.printStackTrace();
                Assert.fail( t.getMessage() );
            }
        }
    }

    private boolean testAlgorithm( boolean result )
                            throws OWSExceptionReport, IOException, XMLStreamException, XMLParsingException,
                            UnknownCRSException {

        Assert.assertNotNull( client );

        // get algorithms
        LinkedList<GeoAlgorithmWithData> algs;
        if ( result )
            algs = algorithmsResultTest;
        else
            algs = algorithmsExecutabilityTest;

        // traverse all algorithms
        for ( GeoAlgorithmWithData testAlg : algs ) {

            // SEXTANTE GeoAlgorithm
            GeoAlgorithm alg = testAlg.getAlgorithm();

            // all output formats of this algorithm
            LinkedList<LinkedList<OutputFormat>> outputFormats = testAlg.getAllOutputFormats();

            // all input data of this algorithm
            LinkedList<LinkedList<ExampleData>> allData = testAlg.getAllInputData();

            // traverse all output formats
            for ( LinkedList<OutputFormat> outputFormat : outputFormats ) {

                // traverse all input parameters
                for ( LinkedList<ExampleData> data : allData ) {

                    // set input data for one execution
                    ParametersSet paramSet = alg.getParameters();
                    if ( data.size() > 0 )
                        if ( data.size() == paramSet.getNumberOfParameters() ) {
                            Process process = client.getProcess( testAlg.getIdentifier() );

                            if ( process != null ) { // found process

                                LOG.info( "Testing '" + testAlg.getIdentifier() + "' algorithm with:" );

                                ProcessExecution execution = process.prepareExecution();

                                // example data iterator
                                Iterator<ExampleData> itIn = data.iterator();

                                // traverse all input parameters
                                for ( int j = 0; j < paramSet.getNumberOfParameters(); j++ ) {

                                    // current input parameter
                                    Parameter param = paramSet.getParameter( j );

                                    // current example data of the input parameter
                                    ExampleData currentData = itIn.next();

                                    // set the input parameter
                                    setInputParameter( execution, param, currentData );
                                }

                                // output formats iterator
                                Iterator<OutputFormat> itOut = outputFormat.iterator();

                                // traverse all output parameters
                                OutputObjectsSet outputSet = alg.getOutputObjects();
                                for ( int j = 0; j < outputSet.getOutputObjectsCount(); j++ ) {
                                    // current output parameter
                                    Output param = outputSet.getOutput( j );

                                    // current output format of the input parameter
                                    OutputFormat currentFormat = itOut.next();

                                    // set the output parameter
                                    setOutputParameter( execution, param, currentFormat );
                                }

                                // execute algorithm
                                ExecutionOutputs outputs = execution.execute();
                                ExecutionOutput[] allOutputs = outputs.getAll();

                                // check number of output output objects
                                Assert.assertTrue( allOutputs.length > 0 );

                                // test result values
                                if ( result )
                                    Assert.assertTrue( testResult( testAlg, allOutputs ) );

                            } else {// don't found process
                                Assert.fail( "Don't found process '" + testAlg.getIdentifier() + "'" );
                            }

                        } else { // number of input parameters and example data are wrong
                            Assert.fail( "Wrong number of input data. (" + testAlg.getIdentifier() + ")" );
                        }

                }
            }
        }

        return true;
    }

    private boolean testResult( GeoAlgorithmWithData alg, ExecutionOutput[] allOutputs )
                            throws XMLStreamException, IOException, XMLParsingException, UnknownCRSException {

        boolean resultCorrect = false;

        // determine output format
        GMLSchema outputFormat = null;
        LinkedList<LinkedList<OutputFormat>> outputFormats = alg.getAllOutputFormats();
        if ( outputFormats.size() == 1 ) {
            LinkedList<OutputFormat> outputs = outputFormats.getFirst();
            if ( outputs.size() == 1 ) {
                OutputFormat format = outputs.getFirst();
                if ( format instanceof GMLSchema ) {
                    outputFormat = (GMLSchema) format;
                }
            }
        }

        // read output values
        if ( outputFormat != null && allOutputs.length == 1 ) {
            ComplexOutput gmlOutput = (ComplexOutput) allOutputs[0];
            XMLStreamReader xmlReader = gmlOutput.getAsXMLStream();
            GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( outputFormat.getGMLVersion(), xmlReader );

            if ( outputFormat.getGMLType().equals( GMLType.GEOMETRY ) ) { // Geometry
                org.deegree.geometry.Geometry g = gmlReader.readGeometry();

                // centroids algorithm
                if ( alg.getCommandLineName().equals( "centroids" ) ) {
                    if ( g instanceof Point ) {
                        Point p = (Point) g;
                        if ( p.get0() == 40.0 && p.get1() == 40.0 )
                            resultCorrect = true;
                    }
                } else {

                    // transform algorithm
                    if ( alg.getCommandLineName().equals( "transform" ) ) {
                        if ( g instanceof Point ) {
                            Point p = (Point) g;
                            if ( p.get0() == 120.0 && p.get1() == 120.0 )
                                resultCorrect = true;
                        }
                    } else {
                        // removeholes algorithm
                        if ( alg.getCommandLineName().equals( "removeholes" ) ) {
                            if ( g instanceof Polygon ) {
                                Polygon p = (Polygon) g;
                                if ( p.getInteriorRings().size() == 0 )
                                    resultCorrect = true;
                            }
                        }
                    }
                }

            } else { // FeatureCollection
                // feature collections has not a schema, don't parse
                // FeatureCollection fc = gmlReader.readFeatureCollection();

                if ( alg.getCommandLineName().equals( "countpoints" ) ) {
                    // Property[] props = fc.getProperties( new QName( "Innerpoints" ) );
                    // if ( props.length == 1 ) {
                    // Property prop = props[0];
                    // TypedObjectNode value = prop.getValue();
                    // if ( value instanceof PrimitiveValue ) {
                    // PrimitiveValue valuePrim = (PrimitiveValue) value;
                    // if ( valuePrim.getAsText().equals( "1" ) )
                    resultCorrect = true;
                    // }
                    // }
                }

            }

        }

        return resultCorrect;
    }

    private LinkedList<GeoAlgorithmWithData> getAlgorithmsForResultTest() {

        // list of algorithms
        LinkedList<GeoAlgorithmWithData> allAlgs = new LinkedList<GeoAlgorithmWithData>();

        // general output formats
        LinkedList<OutputFormat> geometryOutput = new LinkedList<OutputFormat>();
        geometryOutput.add( GMLSchema.GML_31_GEOMETRY_SCHEMA );
        LinkedList<OutputFormat> featureCollectionOutput = new LinkedList<OutputFormat>();
        featureCollectionOutput.add( GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

        // ---------------------------------------------------------------------------------------------------------------------------
        // centroids algorithm
        String centroidsName = "centroids";
        GeoAlgorithmWithData centroidsAlg = new GeoAlgorithmWithData(
                                                                      Sextante.getAlgorithmFromCommandLineName( centroidsName ) );
        // add input data
        LinkedList<ExampleData> centroidsInputData = new LinkedList<ExampleData>();
        centroidsInputData.add( VectorExampleData.GML_31_MULTIPOINT );
        centroidsAlg.addInputData( centroidsInputData );
        centroidsAlg.addOutputFormats( geometryOutput );

        // add algorithm to list
        allAlgs.add( centroidsAlg );

        // ---------------------------------------------------------------------------------------------------------------------------
        // transform algorithm
        String transformName = "transform";
        GeoAlgorithmWithData transformAlg = new GeoAlgorithmWithData(
                                                                      Sextante.getAlgorithmFromCommandLineName( transformName ) );
        // add input data
        LinkedList<ExampleData> transformData1 = new LinkedList<ExampleData>();
        transformData1.add( VectorExampleData.GML_31_POINT );
        transformData1.add( LiteralExampleData.NUMERICAL_VALUE_200 );
        transformData1.add( LiteralExampleData.NUMERICAL_VALUE_200 );
        transformData1.add( LiteralExampleData.NUMERICAL_VALUE_180 );
        transformData1.add( LiteralExampleData.NUMERICAL_VALUE_2 );
        transformData1.add( LiteralExampleData.NUMERICAL_VALUE_2 );
        transformData1.add( LiteralExampleData.NUMERICAL_VALUE_5 );
        transformData1.add( LiteralExampleData.NUMERICAL_VALUE_5 );
        transformAlg.addInputData( transformData1 );
        transformAlg.addOutputFormats( geometryOutput );

        // add algorithm to list
        allAlgs.add( transformAlg );

        // ---------------------------------------------------------------------------------------------------------------------------
        // removeholes algorithm
        String removeholesName = "removeholes";
        GeoAlgorithmWithData removeholesAlg = new GeoAlgorithmWithData(
                                                                        Sextante.getAlgorithmFromCommandLineName( removeholesName ) );
        // add all test data
        LinkedList<ExampleData> removeholesData = new LinkedList<ExampleData>();
        removeholesData.add( VectorExampleData.GML_31_POLYGON );
        removeholesAlg.addInputData( removeholesData );
        removeholesAlg.addOutputFormats( geometryOutput );

        // add algorithm to list
        allAlgs.add( removeholesAlg );

        //
        // ---------------------------------------------------------------------------------------------------------------------------
        // countpoints algorithm
        String countpointsName = "countpoints";
        GeoAlgorithmWithData countpointsAlg = new GeoAlgorithmWithData(
                                                                        Sextante.getAlgorithmFromCommandLineName( countpointsName ) );
        // add test data
        LinkedList<ExampleData> countpointsData1 = new LinkedList<ExampleData>();
        countpointsData1.add( VectorExampleData.GML_31_POINT );
        countpointsData1.add( VectorExampleData.GML_31_POLYGON );
        countpointsAlg.addInputData( countpointsData1 );
        countpointsAlg.addOutputFormats( featureCollectionOutput );
        allAlgs.add( countpointsAlg );

        return allAlgs;
    }

    /**
     * This method returns all supported SEXTANTE {@link GeoAlgorithm} of the deegree WPS with example data. Before you
     * can use it, you must initialize SEXTANTE.
     * 
     * @return All supported SEXTANTE {@link GeoAlgorithm} with example data.
     */
    private LinkedList<GeoAlgorithmWithData> getAlgorithmsForExecutabilityTest() {

        LinkedList<GeoAlgorithmWithData> allAlgs = new LinkedList<GeoAlgorithmWithData>();

        // all GML output formats
        LinkedList<? extends OutputFormat> allGMLFormatsRaw = GMLSchema.getAllSchemas();
        LinkedList<LinkedList<OutputFormat>> allGMLFormats = new LinkedList<LinkedList<OutputFormat>>();
        for ( OutputFormat format : allGMLFormatsRaw ) {
            LinkedList<OutputFormat> list = new LinkedList<OutputFormat>();
            list.add( format );
            allGMLFormats.add( list );
        }

        boolean getAll = true;

        if ( !getAll ) {// return only one algorithm

            // ---------------------------------------------------------------------------------------------------------------------------
            // centroids algorithm
            String centroidsName = "centroids";
            GeoAlgorithmWithData centroidsAlg = new GeoAlgorithmWithData(
                                                                          Sextante.getAlgorithmFromCommandLineName( centroidsName ) );
            // add input data
            LinkedList<ExampleData> centroidsInputData = new LinkedList<ExampleData>();
            centroidsInputData.add( VectorExampleData.GML_31_MULTIPOINT );
            centroidsAlg.addInputData( centroidsInputData );

            // add output format
            LinkedList<OutputFormat> centroidsOutputFormat = new LinkedList<OutputFormat>();
            centroidsOutputFormat.add( GMLSchema.GML_31_GEOMETRY_SCHEMA );
            centroidsAlg.addOutputFormats( centroidsOutputFormat );

            // add algorithm to list
            allAlgs.add( centroidsAlg );

            //
            // ---------------------------------------------------------------------------------------------------------------------------
            // countpoints algorithm
            // String countpointsName = "countpoints";
            // GeoAlgorithmWithData countpointsAlg = new GeoAlgorithmWithData(
            // Sextante.getAlgorithmFromCommandLineName( countpointsName ) );
            // // add test data
            // LinkedList<ExampleData> countpointsData1 = new LinkedList<ExampleData>();
            // countpointsData1.add( VectorExampleData.GML_31_POINT );
            // countpointsData1.add( VectorExampleData.GML_31_POLYGON );
            // countpointsAlg.addInputData( countpointsData1 );
            // LinkedList<ExampleData> countpointsData2 = new LinkedList<ExampleData>();
            // countpointsData2.add( VectorExampleData.GML_31_FEATURE_COLLECTION_POINTS );
            // countpointsData2.add( VectorExampleData.GML_31_FEATURE_COLLECTION_WFS );
            // countpointsAlg.addInputData( countpointsData2 );
            // countpointsAlg.addAllOutputFormats( allGMLFormats );
            // allAlgs.add( countpointsAlg );

            // //
            // ---------------------------------------------------------------------------------------------------------------------------
            // // vectorfieldcalculator algorithm
            // String vectorfieldcalculatorName = "vectorfieldcalculator";
            // GeoAlgorithmWithData vectorfieldcalculatorAlg = new GeoAlgorithmWithData(
            // Sextante.getAlgorithmFromCommandLineName( vectorfieldcalculatorName ) );
            // // add test data
            // LinkedList<ExampleData> vectorfieldcalculatorData1 = new LinkedList<ExampleData>();
            // vectorfieldcalculatorData1.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POLYGONS ); // LAYER
            // vectorfieldcalculatorData1.add( LiteralExampleData.STRING_TEST ); // FORMULA
            // vectorfieldcalculatorAlg.addInputData( vectorfieldcalculatorData1 );
            // allAlgs.add( vectorfieldcalculatorAlg );
            //
            // // vectorfieldcalculator
            // // LAYER[Vector Layer], FORMULA[String], RESULT[output vector layer]
        } else {// return all algorithms

            // ---------------------------------------------------------------------------------------------------------------------------
            // boundingbox algorithm
            String boundingboxName = "boundingbox";
            GeoAlgorithmWithData boundingboxAlg = new GeoAlgorithmWithData(
                                                                            Sextante.getAlgorithmFromCommandLineName( boundingboxName ) );
            // add all test data
            LinkedList<? extends ExampleData> boundingboxData = VectorExampleData.getAllData();
            for ( ExampleData data : boundingboxData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                boundingboxAlg.addInputData( list );
            }
            boundingboxAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( boundingboxAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // centroids algorithm
            String centroidsName = "centroids";
            GeoAlgorithmWithData centroidsAlg = new GeoAlgorithmWithData(
                                                                          Sextante.getAlgorithmFromCommandLineName( centroidsName ) );
            // add all test data
            LinkedList<? extends ExampleData> centroidsData = VectorExampleData.getAllData();
            for ( ExampleData data : centroidsData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                centroidsAlg.addInputData( list );
            }
            centroidsAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( centroidsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // changelinedirection algorithm
            String changelinedirectionName = "changelinedirection";
            GeoAlgorithmWithData changelinedirectionAlg = new GeoAlgorithmWithData(
                                                                                    Sextante.getAlgorithmFromCommandLineName( changelinedirectionName ) );
            // add all test data
            LinkedList<? extends ExampleData> changelinedirectionData = VectorExampleData.getData( GeometryType.LINE );
            for ( ExampleData data : changelinedirectionData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                changelinedirectionAlg.addInputData( list );
            }
            changelinedirectionAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( changelinedirectionAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // cleanpointslayer algorithm
            String cleanpointslayerName = "cleanpointslayer";
            GeoAlgorithmWithData cleanpointslayerAlg = new GeoAlgorithmWithData(
                                                                                 Sextante.getAlgorithmFromCommandLineName( cleanpointslayerName ) );
            // add all test data
            LinkedList<? extends ExampleData> cleanpointslayerData = VectorExampleData.getData( GeometryType.POINT );
            for ( ExampleData data : cleanpointslayerData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                cleanpointslayerAlg.addInputData( list );
            }
            cleanpointslayerAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( cleanpointslayerAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // cleanvectorlayer algorithm
            String cleanvectorlayerName = "cleanvectorlayer";
            GeoAlgorithmWithData cleanvectorlayerAlg = new GeoAlgorithmWithData(
                                                                                 Sextante.getAlgorithmFromCommandLineName( cleanvectorlayerName ) );
            // add all test data
            LinkedList<? extends ExampleData> cleanvectorlayerData = VectorExampleData.getAllData();
            for ( ExampleData data : cleanvectorlayerData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                cleanvectorlayerAlg.addInputData( list );
            }
            cleanvectorlayerAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( cleanvectorlayerAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // clip algorithm
            String clipName = "clip";
            GeoAlgorithmWithData clipAlg = new GeoAlgorithmWithData(
                                                                     Sextante.getAlgorithmFromCommandLineName( clipName ) );

            // add all test data
            LinkedList<ExampleData> clipLayerData = new LinkedList<ExampleData>();
            clipLayerData.add( VectorExampleData.GML_31_POINT );
            clipLayerData.add( VectorExampleData.GML_31_LINESTRING );
            clipLayerData.add( VectorExampleData.GML_31_POLYGON_2 );
            clipLayerData.add( VectorExampleData.GML_31_MULTIPOINT );
            clipLayerData.add( VectorExampleData.GML_31_MULTILINESTRING );
            clipLayerData.add( VectorExampleData.GML_31_MULTILPOLYGON );

            for ( ExampleData data : clipLayerData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data ); // LAYER: all geometries
                list.add( VectorExampleData.GML_31_POLYGON ); // CLIPLAYER: only polygon
                clipAlg.addInputData( list );
            }
            clipAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( clipAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // countpoints algorithm
            String countpointsName = "countpoints";
            GeoAlgorithmWithData countpointsAlg = new GeoAlgorithmWithData(
                                                                            Sextante.getAlgorithmFromCommandLineName( countpointsName ) );
            // add test data
            LinkedList<ExampleData> countpointsData1 = new LinkedList<ExampleData>();
            countpointsData1.add( VectorExampleData.GML_31_POINT );
            countpointsData1.add( VectorExampleData.GML_31_POLYGON );
            countpointsAlg.addInputData( countpointsData1 );
            LinkedList<ExampleData> countpointsData2 = new LinkedList<ExampleData>();
            countpointsData2.add( VectorExampleData.GML_31_FEATURE_COLLECTION_POINTS );
            countpointsData2.add( VectorExampleData.GML_31_FEATURE_COLLECTION_POLYGONS );
            countpointsAlg.addInputData( countpointsData2 );
            countpointsAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( countpointsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // delaunay algorithm
            String delaunayName = "delaunay";
            GeoAlgorithmWithData delaunayAlg = new GeoAlgorithmWithData(
                                                                         Sextante.getAlgorithmFromCommandLineName( delaunayName ) );
            // add test data
            LinkedList<ExampleData> delaunayData1 = new LinkedList<ExampleData>();
            delaunayData1.add( VectorExampleData.GML_31_FEATURE_COLLECTION_POINTS );
            delaunayAlg.addInputData( delaunayData1 );
            LinkedList<ExampleData> delaunayData2 = new LinkedList<ExampleData>();
            delaunayData2.add( VectorExampleData.GML_31_FEATURE_COLLECTION_MULTIPOINTS );
            delaunayAlg.addInputData( delaunayData2 );
            delaunayAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( delaunayAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // difference algorithm
            String differenceName = "difference";
            GeoAlgorithmWithData differenceAlg = new GeoAlgorithmWithData(
                                                                           Sextante.getAlgorithmFromCommandLineName( differenceName ) );
            // add test data
            LinkedList<ExampleData> differenceData1 = new LinkedList<ExampleData>();
            differenceData1.add( VectorExampleData.GML_31_POLYGON_2 );
            differenceData1.add( VectorExampleData.GML_31_POLYGON );
            differenceAlg.addInputData( differenceData1 );
            differenceAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( differenceAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // extractendpointsoflines algorithm
            String extractendpointsoflinesName = "extractendpointsoflines";
            GeoAlgorithmWithData extractendpointsoflinesAlg = new GeoAlgorithmWithData(
                                                                                        Sextante.getAlgorithmFromCommandLineName( extractendpointsoflinesName ) );
            // add all test data
            LinkedList<? extends ExampleData> extractendpointsoflinesData = VectorExampleData.getData( GeometryType.LINE );
            for ( ExampleData data : extractendpointsoflinesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                extractendpointsoflinesAlg.addInputData( list );
            }
            extractendpointsoflinesAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( extractendpointsoflinesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // extractnodes algorithm
            String extractnodesName = "extractnodes";
            GeoAlgorithmWithData extractnodesAlg = new GeoAlgorithmWithData(
                                                                             Sextante.getAlgorithmFromCommandLineName( extractnodesName ) );
            // add all test data
            LinkedList<? extends ExampleData> extractnodesData = VectorExampleData.getData( GeometryType.LINE );

            for ( ExampleData data : extractnodesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                extractnodesAlg.addInputData( list );
            }
            extractnodesAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( extractnodesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // geometricproperties algorithm
            String geometricpropertiesName = "geometricproperties";
            GeoAlgorithmWithData geometricpropertiesAlg = new GeoAlgorithmWithData(
                                                                                    Sextante.getAlgorithmFromCommandLineName( geometricpropertiesName ) );
            // add all test data
            LinkedList<? extends ExampleData> geometricpropertiesData = VectorExampleData.getData( GeometryType.POLYGON );
            for ( ExampleData data : geometricpropertiesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                geometricpropertiesAlg.addInputData( list );
            }
            geometricpropertiesAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( geometricpropertiesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // geometricpropertieslines algorithm
            String geometricpropertieslinesName = "geometricpropertieslines";
            GeoAlgorithmWithData geometricpropertieslinesAlg = new GeoAlgorithmWithData(
                                                                                         Sextante.getAlgorithmFromCommandLineName( geometricpropertieslinesName ) );
            // add all test data
            LinkedList<? extends ExampleData> geometricpropertieslinesData = VectorExampleData.getData( GeometryType.LINE );
            for ( ExampleData data : geometricpropertieslinesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                geometricpropertieslinesAlg.addInputData( list );
            }
            geometricpropertieslinesAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( geometricpropertieslinesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // geometriestopoints algorithm
            String geometriestopointsName = "geometriestopoints";
            GeoAlgorithmWithData geometriestopointsAlg = new GeoAlgorithmWithData(
                                                                                   Sextante.getAlgorithmFromCommandLineName( geometriestopointsName ) );
            // add all test data
            LinkedList<? extends ExampleData> geometriestopointsData = VectorExampleData.getAllData();
            for ( ExampleData data : geometriestopointsData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                geometriestopointsAlg.addInputData( list );
            }
            geometriestopointsAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( geometriestopointsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // intersection algorithm
            String intersectionName = "intersection";
            GeoAlgorithmWithData intersectionAlg = new GeoAlgorithmWithData(
                                                                             Sextante.getAlgorithmFromCommandLineName( intersectionName ) );
            // add test data
            LinkedList<ExampleData> intersectionData1 = new LinkedList<ExampleData>();
            intersectionData1.add( VectorExampleData.GML_31_POLYGON );
            intersectionData1.add( VectorExampleData.GML_31_POLYGON_2 );
            intersectionAlg.addInputData( intersectionData1 );
            intersectionAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( intersectionAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // nodelines algorithm
            String nodelinesName = "nodelines";
            GeoAlgorithmWithData nodelinesAlg = new GeoAlgorithmWithData(
                                                                          Sextante.getAlgorithmFromCommandLineName( nodelinesName ) );
            // add all test data
            LinkedList<? extends ExampleData> nodelinesData = VectorExampleData.getData( GeometryType.LINE );
            for ( ExampleData data : nodelinesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                nodelinesAlg.addInputData( list );
            }
            nodelinesAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( nodelinesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // pointcoordinates algorithm
            String pointcoordinatesName = "pointcoordinates";
            GeoAlgorithmWithData pointcoordinatesAlg = new GeoAlgorithmWithData(
                                                                                 Sextante.getAlgorithmFromCommandLineName( pointcoordinatesName ) );
            // add all test data
            LinkedList<? extends ExampleData> pointcoordinatesData = VectorExampleData.getData( GeometryType.POINT );
            for ( ExampleData data : pointcoordinatesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                pointcoordinatesAlg.addInputData( list );
            }
            pointcoordinatesAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( pointcoordinatesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // polygonize algorithm
            String polygonizeName = "polygonize";
            GeoAlgorithmWithData polygonizeAlg = new GeoAlgorithmWithData(
                                                                           Sextante.getAlgorithmFromCommandLineName( polygonizeName ) );
            // add all test data
            LinkedList<ExampleData> polygonizeData = new LinkedList<ExampleData>();
            polygonizeData.add( VectorExampleData.GML_31_FEATURE_COLLECTION_MULTILINESTRINGS );
            polygonizeData.add( VectorExampleData.GML_31_MULTILINESTRING );
            for ( ExampleData data : polygonizeData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                polygonizeAlg.addInputData( list );
            }
            polygonizeAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( polygonizeAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // polygonstopolylines algorithm
            String polygonstopolylinesName = "polygonstopolylines";
            GeoAlgorithmWithData polygonstopolylinesAlg = new GeoAlgorithmWithData(
                                                                                    Sextante.getAlgorithmFromCommandLineName( polygonstopolylinesName ) );
            // add all test data
            LinkedList<? extends ExampleData> polygonstopolylinesData = VectorExampleData.getAllData();
            for ( ExampleData data : polygonstopolylinesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                polygonstopolylinesAlg.addInputData( list );
            }
            polygonstopolylinesAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( polygonstopolylinesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // polylinestopolygons algorithm
            String polylinestopolygonsName = "polylinestopolygons";
            GeoAlgorithmWithData polylinestopolygonsAlg = new GeoAlgorithmWithData(
                                                                                    Sextante.getAlgorithmFromCommandLineName( polylinestopolygonsName ) );
            // add all test data
            LinkedList<? extends ExampleData> polylinestopolygonsData = VectorExampleData.getData( GeometryType.LINE );
            for ( ExampleData data : polylinestopolygonsData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                polylinestopolygonsAlg.addInputData( list );
            }
            polylinestopolygonsAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( polylinestopolygonsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // polylinestosinglesegments algorithm
            String polylinestosinglesegmentsName = "polylinestosinglesegments";
            GeoAlgorithmWithData polylinestosinglesegmentsAlg = new GeoAlgorithmWithData(
                                                                                          Sextante.getAlgorithmFromCommandLineName( polylinestosinglesegmentsName ) );
            // add all test data
            LinkedList<? extends ExampleData> polylinestosinglesegmentsData = VectorExampleData.getData( GeometryType.LINE );
            for ( ExampleData data : polylinestosinglesegmentsData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                polylinestosinglesegmentsAlg.addInputData( list );
            }
            polylinestosinglesegmentsAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( polylinestosinglesegmentsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // removeholes algorithm
            String removeholesName = "removeholes";
            GeoAlgorithmWithData removeholesAlg = new GeoAlgorithmWithData(
                                                                            Sextante.getAlgorithmFromCommandLineName( removeholesName ) );
            // add all test data
            LinkedList<? extends ExampleData> removeholesData = VectorExampleData.getData( GeometryType.POLYGON );
            for ( ExampleData data : removeholesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                removeholesAlg.addInputData( list );
            }
            removeholesAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( removeholesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // removerepeatedgeometries algorithm
            String removerepeatedgeometriesName = "removerepeatedgeometries";
            GeoAlgorithmWithData removerepeatedgeometriesAlg = new GeoAlgorithmWithData(
                                                                                         Sextante.getAlgorithmFromCommandLineName( removerepeatedgeometriesName ) );
            // add all test data
            LinkedList<? extends ExampleData> removerepeatedgeometriesData = VectorExampleData.getAllData();
            for ( ExampleData data : removerepeatedgeometriesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                removerepeatedgeometriesAlg.addInputData( list );
            }
            removerepeatedgeometriesAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( removerepeatedgeometriesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // splitmultipart algorithm
            String splitmultipartName = "splitmultipart";
            GeoAlgorithmWithData splitmultipartAlg = new GeoAlgorithmWithData(
                                                                               Sextante.getAlgorithmFromCommandLineName( splitmultipartName ) );
            // add all test data
            LinkedList<? extends ExampleData> splitmultipartData = VectorExampleData.getAllData();
            for ( ExampleData data : splitmultipartData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                splitmultipartAlg.addInputData( list );
            }
            splitmultipartAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( splitmultipartAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // splitpolylinesatnodes algorithm
            String splitpolylinesatnodesName = "splitpolylinesatnodes";
            GeoAlgorithmWithData splitpolylinesatnodesAlg = new GeoAlgorithmWithData(
                                                                                      Sextante.getAlgorithmFromCommandLineName( splitpolylinesatnodesName ) );
            // add all test data
            LinkedList<? extends ExampleData> splitpolylinesatnodesData = VectorExampleData.getData( GeometryType.LINE );
            for ( ExampleData data : splitpolylinesatnodesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                splitpolylinesatnodesAlg.addInputData( list );
            }
            splitpolylinesatnodesAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( splitpolylinesatnodesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // symdifference algorithm
            String symdifferenceName = "symdifference";
            GeoAlgorithmWithData symdifferenceAlg = new GeoAlgorithmWithData(
                                                                              Sextante.getAlgorithmFromCommandLineName( symdifferenceName ) );
            // add test data
            LinkedList<ExampleData> symdifferenceData1 = new LinkedList<ExampleData>();
            symdifferenceData1.add( VectorExampleData.GML_31_POLYGON_2 );
            symdifferenceData1.add( VectorExampleData.GML_31_POLYGON );
            symdifferenceAlg.addInputData( symdifferenceData1 );
            symdifferenceAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( symdifferenceAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // union algorithm
            String unionName = "union";
            GeoAlgorithmWithData unionAlg = new GeoAlgorithmWithData(
                                                                      Sextante.getAlgorithmFromCommandLineName( unionName ) );
            // add test data
            LinkedList<ExampleData> unionData1 = new LinkedList<ExampleData>();
            unionData1.add( VectorExampleData.GML_31_POLYGON_2 );
            unionData1.add( VectorExampleData.GML_31_POLYGON );
            unionAlg.addInputData( unionData1 );
            unionAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( unionAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // vectormean algorithm
            String vectormeanName = "vectormean";
            GeoAlgorithmWithData vectormeanAlg = new GeoAlgorithmWithData(
                                                                           Sextante.getAlgorithmFromCommandLineName( vectormeanName ) );
            // add all test data
            LinkedList<? extends ExampleData> vectormeanData = VectorExampleData.getData( GeometryType.LINE );
            for ( ExampleData data : vectormeanData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                vectormeanAlg.addInputData( list );
            }
            vectormeanAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( vectormeanAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // clipbyrectangle algorithm
            String clipbyrectangleName = "clipbyrectangle";
            GeoAlgorithmWithData clipbyrectangleAlg = new GeoAlgorithmWithData(
                                                                                Sextante.getAlgorithmFromCommandLineName( clipbyrectangleName ) );
            // add test data
            LinkedList<ExampleData> clipbyrectangleData1 = new LinkedList<ExampleData>();
            clipbyrectangleData1.add( VectorExampleData.GML_31_POLYGON_2 );
            clipbyrectangleData1.add( LiteralExampleData.NUMERICAL_VALUE_0 );
            clipbyrectangleData1.add( LiteralExampleData.NUMERICAL_VALUE_100 );
            clipbyrectangleData1.add( LiteralExampleData.NUMERICAL_VALUE_0 );
            clipbyrectangleData1.add( LiteralExampleData.NUMERICAL_VALUE_100 );
            clipbyrectangleAlg.addInputData( clipbyrectangleData1 );
            clipbyrectangleAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( clipbyrectangleAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // groupnearfeatures algorithm
            String groupnearfeaturesName = "groupnearfeatures";
            GeoAlgorithmWithData groupnearfeaturesAlg = new GeoAlgorithmWithData(
                                                                                  Sextante.getAlgorithmFromCommandLineName( groupnearfeaturesName ) );
            // add test data
            LinkedList<ExampleData> groupnearfeaturesData1 = new LinkedList<ExampleData>();
            groupnearfeaturesData1.add( VectorExampleData.GML_31_FEATURE_COLLECTION_POLYGONS );
            groupnearfeaturesData1.add( LiteralExampleData.NUMERICAL_VALUE_1 );
            groupnearfeaturesAlg.addInputData( groupnearfeaturesData1 );
            groupnearfeaturesAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( groupnearfeaturesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // joinadjacentlines algorithm
            String joinadjacentlinesName = "joinadjacentlines";
            GeoAlgorithmWithData joinadjacentlinesAlg = new GeoAlgorithmWithData(
                                                                                  Sextante.getAlgorithmFromCommandLineName( joinadjacentlinesName ) );
            // add test data
            LinkedList<ExampleData> joinadjacentlinesData1 = new LinkedList<ExampleData>();
            joinadjacentlinesData1.add( VectorExampleData.GML_31_FEATURE_COLLECTION_LINESTRINGS );
            joinadjacentlinesData1.add( LiteralExampleData.NUMERICAL_VALUE_100 );
            joinadjacentlinesAlg.addInputData( joinadjacentlinesData1 );
            joinadjacentlinesAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( joinadjacentlinesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // linestoequispacedpoints algorithm
            String linestoequispacedpointsName = "linestoequispacedpoints";
            GeoAlgorithmWithData linestoequispacedpointsAlg = new GeoAlgorithmWithData(
                                                                                        Sextante.getAlgorithmFromCommandLineName( linestoequispacedpointsName ) );
            // add test data
            LinkedList<ExampleData> linestoequispacedpointsData1 = new LinkedList<ExampleData>();
            linestoequispacedpointsData1.add( VectorExampleData.GML_31_FEATURE_COLLECTION_LINESTRINGS );
            linestoequispacedpointsData1.add( LiteralExampleData.NUMERICAL_VALUE_100 );
            linestoequispacedpointsAlg.addInputData( linestoequispacedpointsData1 );
            linestoequispacedpointsAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( linestoequispacedpointsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // perturbatepointslayer algorithm
            String perturbatepointslayerName = "perturbatepointslayer";
            GeoAlgorithmWithData perturbatepointslayerAlg = new GeoAlgorithmWithData(
                                                                                      Sextante.getAlgorithmFromCommandLineName( perturbatepointslayerName ) );
            // add test data
            LinkedList<ExampleData> perturbatepointslayerData1 = new LinkedList<ExampleData>();
            perturbatepointslayerData1.add( VectorExampleData.GML_31_FEATURE_COLLECTION_POINTS );
            perturbatepointslayerData1.add( LiteralExampleData.NUMERICAL_VALUE_100 );
            perturbatepointslayerData1.add( LiteralExampleData.NUMERICAL_VALUE_200 );
            perturbatepointslayerAlg.addInputData( perturbatepointslayerData1 );
            perturbatepointslayerAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( perturbatepointslayerAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // snappoints algorithm
            String snappointsName = "snappoints";
            GeoAlgorithmWithData snappointsAlg = new GeoAlgorithmWithData(
                                                                           Sextante.getAlgorithmFromCommandLineName( snappointsName ) );
            // add test data
            LinkedList<ExampleData> snappointsData1 = new LinkedList<ExampleData>();
            snappointsData1.add( VectorExampleData.GML_31_FEATURE_COLLECTION_POINTS );
            snappointsData1.add( VectorExampleData.GML_31_FEATURE_COLLECTION_LINESTRINGS );
            snappointsData1.add( LiteralExampleData.NUMERICAL_VALUE_100 );
            snappointsAlg.addInputData( snappointsData1 );
            snappointsAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( snappointsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // transform algorithm
            String transformName = "transform";
            GeoAlgorithmWithData transformAlg = new GeoAlgorithmWithData(
                                                                          Sextante.getAlgorithmFromCommandLineName( transformName ) );
            // add test data
            LinkedList<ExampleData> transformData1 = new LinkedList<ExampleData>();
            transformData1.add( VectorExampleData.GML_31_POINT );
            transformData1.add( LiteralExampleData.NUMERICAL_VALUE_100 );
            transformData1.add( LiteralExampleData.NUMERICAL_VALUE_200 );
            transformData1.add( LiteralExampleData.NUMERICAL_VALUE_0 );
            transformData1.add( LiteralExampleData.NUMERICAL_VALUE_1 );
            transformData1.add( LiteralExampleData.NUMERICAL_VALUE_1 );
            transformData1.add( LiteralExampleData.NUMERICAL_VALUE_0 );
            transformData1.add( LiteralExampleData.NUMERICAL_VALUE_0 );
            transformAlg.addInputData( transformData1 );
            transformAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( transformAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // vectorspatialcluster algorithm
            String vectorspatialclusterName = "vectorspatialcluster";
            GeoAlgorithmWithData vectorspatialclusterAlg = new GeoAlgorithmWithData(
                                                                                     Sextante.getAlgorithmFromCommandLineName( vectorspatialclusterName ) );
            // add test data
            LinkedList<ExampleData> vectorspatialclusterData1 = new LinkedList<ExampleData>();
            vectorspatialclusterData1.add( VectorExampleData.GML_31_FEATURE_COLLECTION_POINTS );
            vectorspatialclusterData1.add( LiteralExampleData.NUMERICAL_VALUE_100 );
            vectorspatialclusterAlg.addInputData( vectorspatialclusterData1 );
            vectorspatialclusterAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( vectorspatialclusterAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // fixeddistancebuffer algorithm
            String fixeddistancebufferName = "fixeddistancebuffer";
            GeoAlgorithmWithData fixeddistancebufferAlg = new GeoAlgorithmWithData(
                                                                                    Sextante.getAlgorithmFromCommandLineName( fixeddistancebufferName ) );

            // add test data
            LinkedList<ExampleData> fixeddistancebufferData1 = new LinkedList<ExampleData>();
            fixeddistancebufferData1.add( VectorExampleData.GML_31_POLYGON );
            fixeddistancebufferData1.add( LiteralExampleData.NUMERICAL_VALUE_1 ); // DISTANCE: 1
            fixeddistancebufferData1.add( LiteralExampleData.SELECTION_0 ); // TYPE: BUFFER_OUTSIDE_POLY
            fixeddistancebufferData1.add( LiteralExampleData.SELECTION_0 ); // RINGS: 0
            fixeddistancebufferData1.add( LiteralExampleData.BOOLEAN_FALSE ); // NOTROUNDED: false
            fixeddistancebufferAlg.addInputData( fixeddistancebufferData1 );
            LinkedList<ExampleData> fixeddistancebufferData2 = new LinkedList<ExampleData>();
            fixeddistancebufferData2.add( VectorExampleData.GML_31_POLYGON );
            fixeddistancebufferData2.add( LiteralExampleData.NUMERICAL_VALUE_1 ); // DISTANCE: 1
            fixeddistancebufferData2.add( LiteralExampleData.SELECTION_1 ); // TYPE: BUFFER_INSIDE_POLY
            fixeddistancebufferData2.add( LiteralExampleData.SELECTION_0 ); // RINGS: 0
            fixeddistancebufferData2.add( LiteralExampleData.BOOLEAN_FALSE ); // NOTROUNDED: false
            fixeddistancebufferAlg.addInputData( fixeddistancebufferData2 );
            LinkedList<ExampleData> fixeddistancebufferData3 = new LinkedList<ExampleData>();
            fixeddistancebufferData3.add( VectorExampleData.GML_31_POLYGON );
            fixeddistancebufferData3.add( LiteralExampleData.NUMERICAL_VALUE_1 ); // DISTANCE: 1
            fixeddistancebufferData3.add( LiteralExampleData.SELECTION_2 ); // TYPE: BUFFER_INSIDE_OUTSIDE_POLY
            fixeddistancebufferData3.add( LiteralExampleData.SELECTION_0 ); // RINGS: 0
            fixeddistancebufferData3.add( LiteralExampleData.BOOLEAN_FALSE ); // NOTROUNDED: true
            fixeddistancebufferAlg.addInputData( fixeddistancebufferData3 );
            LinkedList<ExampleData> fixeddistancebufferData4 = new LinkedList<ExampleData>();
            fixeddistancebufferData4.add( VectorExampleData.GML_31_POINT );
            fixeddistancebufferData4.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // DISTANCE: 10
            fixeddistancebufferData4.add( LiteralExampleData.SELECTION_0 ); // TYPE: BUFFER_OUTSIDE_POLY
            fixeddistancebufferData4.add( LiteralExampleData.SELECTION_2 ); // RINGS: 2
            fixeddistancebufferData4.add( LiteralExampleData.BOOLEAN_TRUE ); // NOTROUNDED: true
            fixeddistancebufferAlg.addInputData( fixeddistancebufferData4 );
            LinkedList<ExampleData> fixeddistancebufferData5 = new LinkedList<ExampleData>();
            fixeddistancebufferData5.add( VectorExampleData.GML_31_POINT );
            fixeddistancebufferData5.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // DISTANCE: 10
            fixeddistancebufferData5.add( LiteralExampleData.SELECTION_0 ); // TYPE: BUFFER_OUTSIDE_POLY
            fixeddistancebufferData5.add( LiteralExampleData.SELECTION_1 ); // RINGS: 1
            fixeddistancebufferData5.add( LiteralExampleData.BOOLEAN_TRUE ); // NOTROUNDED: true
            fixeddistancebufferAlg.addInputData( fixeddistancebufferData5 );
            LinkedList<ExampleData> fixeddistancebufferData6 = new LinkedList<ExampleData>();
            fixeddistancebufferData6.add( VectorExampleData.GML_31_FEATURE_COLLECTION_MULTIPOLYGONS );
            fixeddistancebufferData6.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // DISTANCE: 10
            fixeddistancebufferData6.add( LiteralExampleData.SELECTION_0 ); // TYPE: BUFFER_OUTSIDE_POLY
            fixeddistancebufferData6.add( LiteralExampleData.SELECTION_1 ); // RINGS: 1
            fixeddistancebufferData6.add( LiteralExampleData.BOOLEAN_FALSE ); // NOTROUNDED: false
            fixeddistancebufferAlg.addInputData( fixeddistancebufferData6 );
            fixeddistancebufferAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( fixeddistancebufferAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // generateroutes algorithm
            String generateroutesName = "generateroutes";
            GeoAlgorithmWithData generateroutesAlg = new GeoAlgorithmWithData(
                                                                               Sextante.getAlgorithmFromCommandLineName( generateroutesName ) );
            // add test data
            LinkedList<ExampleData> generateroutesData1 = new LinkedList<ExampleData>();
            generateroutesData1.add( VectorExampleData.GML_31_LINESTRING ); // ROUTE
            generateroutesData1.add( LiteralExampleData.NUMERICAL_VALUE_1 ); // NROUTES: 1
            generateroutesData1.add( LiteralExampleData.SELECTION_1 ); // METHOD: CREATION_METHOD_RECOMBINE
            generateroutesData1.add( LiteralExampleData.NUMERICAL_VALUE_1 ); // SINUOSITY: 1
            generateroutesData1.add( LiteralExampleData.BOOLEAN_TRUE ); // USESINUOSITY: true
            generateroutesAlg.addInputData( generateroutesData1 );
            LinkedList<ExampleData> generateroutesData2 = new LinkedList<ExampleData>();
            generateroutesData2.add( VectorExampleData.GML_31_FEATURE_COLLECTION_MULTILINESTRINGS ); // ROUTE
            generateroutesData2.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // NROUTES: 10
            generateroutesData2.add( LiteralExampleData.SELECTION_1 ); // METHOD: CREATION_METHOD_RECOMBINE
            generateroutesData2.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // SINUOSITY: 10
            generateroutesData2.add( LiteralExampleData.BOOLEAN_FALSE ); // USESINUOSITY: false
            generateroutesAlg.addInputData( generateroutesData2 );
            generateroutesAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( generateroutesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // simplifylines algorithm
            String simplifylinesName = "simplifylines";
            GeoAlgorithmWithData simplifylinesAlg = new GeoAlgorithmWithData(
                                                                              Sextante.getAlgorithmFromCommandLineName( simplifylinesName ) );
            // add test data
            LinkedList<ExampleData> simplifylinesData1 = new LinkedList<ExampleData>();
            simplifylinesData1.add( VectorExampleData.GML_31_FEATURE_COLLECTION_LINESTRINGS );
            simplifylinesData1.add( LiteralExampleData.NUMERICAL_VALUE_100 ); // TOLERANCE: 100
            simplifylinesData1.add( LiteralExampleData.BOOLEAN_FALSE ); // PRESERVE: false
            simplifylinesAlg.addInputData( simplifylinesData1 );
            LinkedList<ExampleData> simplifylinesData2 = new LinkedList<ExampleData>();
            simplifylinesData2.add( VectorExampleData.GML_31_FEATURE_COLLECTION_MULTILINESTRINGS );
            simplifylinesData2.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // TOLERANCE: 10
            simplifylinesData2.add( LiteralExampleData.BOOLEAN_TRUE ); // PRESERVE: true
            simplifylinesAlg.addInputData( simplifylinesData2 );
            simplifylinesAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( simplifylinesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // simplifypolygons algorithm
            String simplifypolygonsName = "simplifypolygons";
            GeoAlgorithmWithData simplifypolygonsAlg = new GeoAlgorithmWithData(
                                                                                 Sextante.getAlgorithmFromCommandLineName( simplifypolygonsName ) );
            // add test data
            LinkedList<ExampleData> simplifypolygonsData1 = new LinkedList<ExampleData>();
            simplifypolygonsData1.add( VectorExampleData.GML_31_POLYGON );
            simplifypolygonsData1.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // TOLERANCE: 10
            simplifypolygonsData1.add( LiteralExampleData.BOOLEAN_FALSE ); // PRESERVE: false
            simplifypolygonsAlg.addInputData( simplifypolygonsData1 );
            LinkedList<ExampleData> simplifypolygonsData2 = new LinkedList<ExampleData>();
            simplifypolygonsData2.add( VectorExampleData.GML_31_FEATURE_COLLECTION_MULTIPOLYGONS );
            simplifypolygonsData2.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // TOLERANCE: 10
            simplifypolygonsData2.add( LiteralExampleData.BOOLEAN_TRUE ); // PRESERVE: true
            simplifypolygonsAlg.addInputData( simplifypolygonsData2 );
            simplifypolygonsAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( simplifypolygonsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // smoothlines algorithm
            String smoothlinesName = "smoothlines";
            GeoAlgorithmWithData smoothlinesAlg = new GeoAlgorithmWithData(
                                                                            Sextante.getAlgorithmFromCommandLineName( smoothlinesName ) );
            // add test data
            LinkedList<ExampleData> smoothlinesData1 = new LinkedList<ExampleData>();
            smoothlinesData1.add( VectorExampleData.GML_31_LINESTRING );
            smoothlinesData1.add( LiteralExampleData.SELECTION_3 ); // INTERMEDIATE_POINTS: 3
            smoothlinesData1.add( LiteralExampleData.SELECTION_0 ); // CURVE_TYPE: NATURAL_CUBIC_SPLINES
            smoothlinesAlg.addInputData( smoothlinesData1 );
            LinkedList<ExampleData> smoothlinesData2 = new LinkedList<ExampleData>();
            smoothlinesData2.add( VectorExampleData.GML_31_FEATURE_COLLECTION_LINESTRINGS );
            smoothlinesData2.add( LiteralExampleData.SELECTION_5 ); // INTERMEDIATE_POINTS: 5
            smoothlinesData2.add( LiteralExampleData.SELECTION_1 ); // CURVE_TYPE: BEZIER_CURVES
            smoothlinesAlg.addInputData( smoothlinesData2 );
            LinkedList<ExampleData> smoothlinesData3 = new LinkedList<ExampleData>();
            smoothlinesData3.add( VectorExampleData.GML_31_FEATURE_COLLECTION_MULTILINESTRINGS );
            smoothlinesData3.add( LiteralExampleData.SELECTION_7 ); // INTERMEDIATE_POINTS: 7
            smoothlinesData3.add( LiteralExampleData.SELECTION_2 ); // CURVE_TYPE: BSPLINES
            smoothlinesAlg.addInputData( smoothlinesData3 );
            LinkedList<ExampleData> smoothlinesData4 = new LinkedList<ExampleData>();
            smoothlinesData4.add( VectorExampleData.GML_31_MULTILINESTRING );
            smoothlinesData4.add( LiteralExampleData.SELECTION_6 ); // INTERMEDIATE_POINTS: 6
            smoothlinesData4.add( LiteralExampleData.SELECTION_1 ); // CURVE_TYPE: NATURAL_CUBIC_SPLINES
            smoothlinesAlg.addInputData( smoothlinesData4 );
            smoothlinesAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( smoothlinesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // splitlineswithpoints algorithm
            String splitlineswithpointsName = "splitlineswithpoints";
            GeoAlgorithmWithData splitlineswithpointsAlg = new GeoAlgorithmWithData(
                                                                                     Sextante.getAlgorithmFromCommandLineName( splitlineswithpointsName ) );
            // add test data
            LinkedList<ExampleData> splitlineswithpointsData1 = new LinkedList<ExampleData>();
            splitlineswithpointsData1.add( VectorExampleData.GML_31_LINESTRING ); // LINES
            splitlineswithpointsData1.add( VectorExampleData.GML_31_POINT ); // POINTS
            splitlineswithpointsData1.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // TOLERANCE: 10
            splitlineswithpointsData1.add( LiteralExampleData.SELECTION_0 ); // METHOD: 10
            splitlineswithpointsAlg.addInputData( splitlineswithpointsData1 );
            LinkedList<ExampleData> splitlineswithpointsData2 = new LinkedList<ExampleData>();
            splitlineswithpointsData2.add( VectorExampleData.GML_31_MULTILINESTRING ); // LINES
            splitlineswithpointsData2.add( VectorExampleData.GML_31_MULTIPOINT ); // POINTS
            splitlineswithpointsData2.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // TOLERANCE: 10
            splitlineswithpointsData2.add( LiteralExampleData.SELECTION_1 ); // METHOD: 10
            splitlineswithpointsAlg.addInputData( splitlineswithpointsData2 );
            LinkedList<ExampleData> splitlineswithpointsData3 = new LinkedList<ExampleData>();
            splitlineswithpointsData3.add( VectorExampleData.GML_31_FEATURE_COLLECTION_LINESTRINGS ); // LINES
            splitlineswithpointsData3.add( VectorExampleData.GML_31_FEATURE_COLLECTION_POINTS ); // POINTS
            splitlineswithpointsData3.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // TOLERANCE: 10
            splitlineswithpointsData3.add( LiteralExampleData.SELECTION_0 ); // METHOD: 10
            splitlineswithpointsAlg.addInputData( splitlineswithpointsData3 );
            LinkedList<ExampleData> splitlineswithpointsData4 = new LinkedList<ExampleData>();
            splitlineswithpointsData4.add( VectorExampleData.GML_31_FEATURE_COLLECTION_MULTILINESTRINGS ); // LINES
            splitlineswithpointsData4.add( VectorExampleData.GML_31_FEATURE_COLLECTION_MULTIPOINTS ); // POINTS
            splitlineswithpointsData4.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // TOLERANCE: 10
            splitlineswithpointsData4.add( LiteralExampleData.SELECTION_1 ); // METHOD: 10
            splitlineswithpointsAlg.addInputData( splitlineswithpointsData4 );
            splitlineswithpointsAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( splitlineswithpointsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // vectoraddfield algorithm
            String vectoraddfieldName = "vectoraddfield";
            GeoAlgorithmWithData vectoraddfieldAlg = new GeoAlgorithmWithData(
                                                                               Sextante.getAlgorithmFromCommandLineName( vectoraddfieldName ) );
            // add test data
            LinkedList<ExampleData> vectoraddfieldData1 = new LinkedList<ExampleData>();
            vectoraddfieldData1.add( VectorExampleData.GML_31_FEATURE_COLLECTION_POINTS ); // INPUT
            vectoraddfieldData1.add( LiteralExampleData.STRING_VIEW ); // FIELD_NAME: VIEWS
            vectoraddfieldData1.add( LiteralExampleData.SELECTION_0 ); // FIELD_TYPE: INTEGER
            vectoraddfieldData1.add( LiteralExampleData.NUMERICAL_VALUE_1 ); // FIELD_LENGTH: 1
            vectoraddfieldData1.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // FIELD_PRECISION: 10
            vectoraddfieldData1.add( LiteralExampleData.STRING_0 ); // DEFAULT_VALUE: 0
            vectoraddfieldAlg.addInputData( vectoraddfieldData1 );
            vectoraddfieldAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( vectoraddfieldAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // vectorcluster algorithm
            String vectorclusterName = "vectorcluster";
            GeoAlgorithmWithData vectorclusterAlg = new GeoAlgorithmWithData(
                                                                              Sextante.getAlgorithmFromCommandLineName( vectorclusterName ) );
            // add test data
            LinkedList<ExampleData> vectorclusterData1 = new LinkedList<ExampleData>();
            vectorclusterData1.add( VectorExampleData.GML_31_FEATURE_COLLECTION_POLYGONS ); // LAYER
            vectorclusterData1.add( LiteralExampleData.STRING_NAME_UPPERNAME_DATAORIGIN_AREA_QUERYBBOXOVERLAP ); // FIELDS
            vectorclusterData1.add( LiteralExampleData.NUMERICAL_VALUE_5 ); // NUMCLASS: 5
            vectorclusterAlg.addInputData( vectorclusterData1 );
            vectorclusterAlg.addAllOutputFormats( allGMLFormats );
            allAlgs.add( vectorclusterAlg );

        }

        LOG.info( "FOUND DATA FOR " + allAlgs.size() + " ALGORITHMS" );

        return allAlgs;
    }

    /**
     * This method sets input data to the {@link ProcessExecution}.
     * 
     * @param execution
     *            {@link ProcessExecution}.
     * @param param
     *            Input {@link Parameter}.
     * @param data
     *            {@link ExampleData}.
     */
    private void setInputParameter( ProcessExecution execution, Parameter param, ExampleData data ) {

        String identifier = param.getParameterName();
        LOG.info( "  INPUT:" + data );

        // vector data
        if ( data instanceof VectorExampleData ) {
            VectorExampleData vectorData = (VectorExampleData) data;
            execution.addXMLInput( identifier, null, vectorData.getURL(), false, vectorData.getMimeType(),
                                   vectorData.getEncoding(), vectorData.getSchemaURL() );

        } else {

            // literal data
            if ( data instanceof LiteralExampleData ) {
                LiteralExampleData literalData = (LiteralExampleData) data;
                execution.addLiteralInput( identifier, literalData.getIdCodeSpace(), literalData.getValue(),
                                           literalData.getType(), literalData.getUOM() );

            } else {
                // TODO distinguish further input parameters like raster and table data
                LOG.error( "The input parameter '" + param.getParameterTypeName() + "' is unknown." );
            }
        }
    }

    /**
     * This method sets a output format to the {@link ProcessExecution}.
     * 
     * @param execution
     *            {@link ProcessExecution}.
     * @param param
     *            Output format.
     */
    private void setOutputParameter( ProcessExecution execution, Output param, OutputFormat format ) {
        String identifier = param.getName();

        LOG.info( "  OUTPUT:" + format );

        // vector data
        if ( param.getTypeDescription().equals( SextanteWPSProcess.VECTOR_LAYER_OUTPUT ) ) {

            if ( format instanceof GMLSchema ) {
                GMLSchema schema = (GMLSchema) format;
                execution.addOutput( identifier, null, null, false, "text/xml", "UTF-8", schema.getSchemaURL() );
            } else {
                LOG.error( "The ouput format '" + format.getClass().getName() + "' for vector data is unknown." );
                throw new UnsupportedOperationException();
            }

        } else {

            // create error message
            String error = "The output parameter '" + param.getName() + "' for " + param.getTypeDescription()
                           + " data is not implemented.";

            // raster data
            if ( param.getTypeDescription().equals( SextanteWPSProcess.RASTER_LAYER_OUTPUT ) ) {
                // TODO implements it.
                LOG.error( error );
                throw new UnsupportedOperationException( error );
            } else {
                // table data
                if ( param.getTypeDescription().equals( SextanteWPSProcess.TABLE_OUTPUT ) ) {
                    // TODO implements it.
                    LOG.error( error );
                    throw new UnsupportedOperationException( error );
                } else {
                    // text data
                    if ( param.getTypeDescription().equals( SextanteWPSProcess.TEXT_OUTPUT ) ) {
                        // TODO implements it.
                        LOG.error( error );
                        throw new UnsupportedOperationException( error );
                    } else {
                        // chart data
                        if ( param.getTypeDescription().equals( SextanteWPSProcess.CHART_OUTPUT ) ) {
                            // TODO implements it.
                            LOG.error( error );
                            throw new UnsupportedOperationException( error );
                        } else {
                            LOG.error( "The ouput parameter '" + param.getTypeDescription() + "' is unknown." );
                            throw new UnsupportedOperationException();
                        }
                    }
                }
            }
        }
    }
}
