//$HeadURL: http://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
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

import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import org.deegree.protocol.wps.GeometryExampleData.GeometryType;
import org.deegree.protocol.wps.client.WPSClient;
import org.deegree.protocol.wps.client.output.ExecutionOutput;
import org.deegree.protocol.wps.client.process.ProcessExecution;
import org.deegree.protocol.wps.client.process.Process;
import org.deegree.protocol.wps.client.process.execute.ExecutionOutputs;
import org.deegree.services.wps.provider.sextante.GMLSchema;
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
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class GeoAlgorithmTest {

    // logger
    private static Logger LOG = LoggerFactory.getLogger( GeoAlgorithmTest.class );

    // enabled/disabled all tests
    private static final boolean ENABLED = false;

    // manages all supported algorithms with example data
    private final LinkedList<GeoAlgorithmWithData> algorithms;

    public GeoAlgorithmTest() {
        // initialize SEXTANTE
        Sextante.initialize();

        // initialize all test algorithms
        algorithms = getAllSupportedAlgorithms();
    }

    /**
     * This method tests all supported SEXTANTE {@link GeoAlgorithm} of the deegree WPS. <br>
     * It will tested only whether the algorithm runs without errors. <br>
     * It is not tested whether the algorithm calculated its output values correctly.
     */
    @Test
    public void testExecutabilityOfAlgorithms() {
        if ( ENABLED ) {
            try {

                // client
                URL wpsURL = new URL(
                                      "http://localhost:8080/deegree-wps-demo/services?service=WPS&version=1.0.0&request=GetCapabilities" );
                WPSClient client = new WPSClient( wpsURL );
                Assert.assertNotNull( client );

                // traverse all algorithms
                for ( GeoAlgorithmWithData testAlg : algorithms ) {

                    // geoalgorithm
                    GeoAlgorithm alg = testAlg.getAlgorithm();

                    // input data
                    LinkedList<LinkedList<ExampleData>> allData = testAlg.getAllInputData();
                    for ( LinkedList<ExampleData> data : allData ) {

                        // set input data for one execution
                        ParametersSet paramSet = alg.getParameters();
                        if ( data.size() > 0 )
                            if ( data.size() == paramSet.getNumberOfParameters() ) {
                                Process process = client.getProcess( testAlg.getIdentifier() );

                                if ( process != null ) { // found process

                                    ProcessExecution execution = process.prepareExecution();

                                    Iterator<ExampleData> it = data.iterator();

                                    for ( int j = 0; j < paramSet.getNumberOfParameters(); j++ ) {
                                        ExampleData currentData = it.next();

                                        Parameter param = paramSet.getParameter( j );
                                        String inputIndentifier = param.getParameterName();
                                        LOG.info( "Testing '" + testAlg.getIdentifier() + "' algorithm with "
                                                  + currentData.toString() );

                                        // Geometries
                                        if ( currentData instanceof GeometryExampleData ) {
                                            GeometryExampleData currentGeometryData = (GeometryExampleData) currentData;

                                            execution.addXMLInput( inputIndentifier, null,
                                                                   currentGeometryData.getURL(), false,
                                                                   currentGeometryData.getMimeType(),
                                                                   currentGeometryData.getEncoding(),
                                                                   currentGeometryData.getSchemaURL() );
                                        } else {

                                            // Literals
                                            if ( currentData instanceof LiteralExampleData ) {
                                                LiteralExampleData currentLiteralData = (LiteralExampleData) currentData;

                                                execution.addLiteralInput( inputIndentifier,
                                                                           currentLiteralData.getIdCodeSpace(),
                                                                           currentLiteralData.getValue(),
                                                                           currentLiteralData.getType(),
                                                                           currentLiteralData.getUOM() );

                                            }
                                        }

                                    }

                                    // set all output parameters
                                    OutputObjectsSet outputSet = alg.getOutputObjects();
                                    for ( int j = 0; j < outputSet.getOutputObjectsCount(); j++ ) {
                                        Output outp = outputSet.getOutput( j );

                                        String outputIdentifier = outp.getName();

                                        // execution.addOutput( outputIdentifier, null, null, false, "text/xml",
                                        // "UTF-8",
                                        // GMLSchema.GML_31_GEOMETRY_SCHEMA.getSchemaURL() );

                                        execution.addOutput( outputIdentifier, null, null, false, "text/xml", "UTF-8",
                                                             GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA.getSchemaURL() );
                                    }

                                    // execute algorithm
                                    ExecutionOutputs outputs = execution.execute();
                                    ExecutionOutput[] allOutputs = outputs.getAll();

                                    // check number of output output objects
                                    Assert.assertTrue( allOutputs.length > 0 );

                                } else {// don't found process
                                    // LOG.error( "Don't found process '" + testAlg.getIdentifier() + "'" );
                                    Assert.fail( "Don't found process '" + testAlg.getIdentifier() + "'" );
                                }

                            } else { // number of input parameters and example data are wrong
                                String msg = "Wrong number of input data. (" + testAlg.getIdentifier() + ")";
                                // LOG.error( msg );
                                Assert.fail( msg );
                            }

                    }
                }

            } catch ( Throwable t ) {
                LOG.error( t.getMessage(), t );
                Assert.fail( t.getLocalizedMessage() );
            }
        }
    }

    @Test
    public void testResultOfAlgorithms() {
        if ( ENABLED ) {
            try {

            } catch ( Throwable t ) {
                LOG.error( t.getMessage(), t );
                Assert.fail( t.getLocalizedMessage() );
            }
        }
    }

    /**
     * This method determines a SEXTANTE {@link GeoAlgorithm} by commmand line name.
     * 
     * @param commandLineName
     *            Command line name of a SEXTANTE {@link GeoAlgorithm}.
     * 
     * @return SEXTANTE {@link GeoAlgorithm} or null if the algorithm was not found.
     */
    public GeoAlgorithmWithData getAlgorithm( String commandLineName ) {

        // determine algorithm
        for ( GeoAlgorithmWithData alg : algorithms ) {
            if ( alg.getCommandLineName().equals( commandLineName ) )
                return alg;
        }

        LOG.error( "SEXTANTE GeoAlgorithm '" + commandLineName + "' was not found." );

        return null;
    }

    /**
     * This method returns all supported SEXTANTE {@link GeoAlgorithm} of the deegree WPS with example data.
     * 
     * @return All supported SEXTANTE {@link GeoAlgorithm} with example data.
     */
    private LinkedList<GeoAlgorithmWithData> getAllSupportedAlgorithms() {

        LinkedList<GeoAlgorithmWithData> allAlgs = new LinkedList<GeoAlgorithmWithData>();

        boolean getAll = true;

        if ( !getAll ) {// return only one algorithm

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
            LinkedList<? extends ExampleData> boundingboxData = GeometryExampleData.getAllData();
            for ( ExampleData data : boundingboxData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                boundingboxAlg.addInputData( list );
            }
            allAlgs.add( boundingboxAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // centroids algorithm
            String centroidsName = "centroids";
            GeoAlgorithmWithData centroidsAlg = new GeoAlgorithmWithData(
                                                                          Sextante.getAlgorithmFromCommandLineName( centroidsName ) );
            // add all test data
            LinkedList<? extends ExampleData> centroidsData = GeometryExampleData.getAllData();
            for ( ExampleData data : centroidsData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                centroidsAlg.addInputData( list );
            }
            allAlgs.add( centroidsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // changelinedirection algorithm
            String changelinedirectionName = "changelinedirection";
            GeoAlgorithmWithData changelinedirectionAlg = new GeoAlgorithmWithData(
                                                                                    Sextante.getAlgorithmFromCommandLineName( changelinedirectionName ) );
            // add all test data
            LinkedList<? extends ExampleData> changelinedirectionData = GeometryExampleData.getData( GeometryType.LINE );
            for ( ExampleData data : changelinedirectionData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                changelinedirectionAlg.addInputData( list );
            }
            allAlgs.add( changelinedirectionAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // cleanpointslayer algorithm
            String cleanpointslayerName = "cleanpointslayer";
            GeoAlgorithmWithData cleanpointslayerAlg = new GeoAlgorithmWithData(
                                                                                 Sextante.getAlgorithmFromCommandLineName( cleanpointslayerName ) );
            // add all test data
            LinkedList<? extends ExampleData> cleanpointslayerData = GeometryExampleData.getData( GeometryType.POINT );
            for ( ExampleData data : cleanpointslayerData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                cleanpointslayerAlg.addInputData( list );
            }
            allAlgs.add( cleanpointslayerAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // cleanvectorlayer algorithm
            String cleanvectorlayerName = "cleanvectorlayer";
            GeoAlgorithmWithData cleanvectorlayerAlg = new GeoAlgorithmWithData(
                                                                                 Sextante.getAlgorithmFromCommandLineName( cleanvectorlayerName ) );
            // add all test data
            LinkedList<? extends ExampleData> cleanvectorlayerData = GeometryExampleData.getAllData();
            for ( ExampleData data : cleanvectorlayerData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                cleanvectorlayerAlg.addInputData( list );
            }
            allAlgs.add( cleanvectorlayerAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // clip algorithm
            String clipName = "clip";
            GeoAlgorithmWithData clipAlg = new GeoAlgorithmWithData(
                                                                     Sextante.getAlgorithmFromCommandLineName( clipName ) );

            // add all test data
            LinkedList<ExampleData> clipLayerData = new LinkedList<ExampleData>();
            clipLayerData.add( GeometryExampleData.GML_31_POINT );
            clipLayerData.add( GeometryExampleData.GML_31_LINESTRING );
            clipLayerData.add( GeometryExampleData.GML_31_POLYGON_2 );
            clipLayerData.add( GeometryExampleData.GML_31_MULTIPOINT );
            clipLayerData.add( GeometryExampleData.GML_31_MULTILINESTRING );
            clipLayerData.add( GeometryExampleData.GML_31_MULTILPOLYGON );

            for ( ExampleData data : clipLayerData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data ); // LAYER: all geometries
                list.add( GeometryExampleData.GML_31_POLYGON ); // CLIPLAYER: only polygon
                clipAlg.addInputData( list );
            }
            allAlgs.add( clipAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // countpoints algorithm
            String countpointsName = "countpoints";
            GeoAlgorithmWithData countpointsAlg = new GeoAlgorithmWithData(
                                                                            Sextante.getAlgorithmFromCommandLineName( countpointsName ) );
            // add test data
            LinkedList<ExampleData> countpointsData1 = new LinkedList<ExampleData>();
            countpointsData1.add( GeometryExampleData.GML_31_POINT );
            countpointsData1.add( GeometryExampleData.GML_31_POLYGON );
            countpointsAlg.addInputData( countpointsData1 );
            LinkedList<ExampleData> countpointsData2 = new LinkedList<ExampleData>();
            countpointsData2.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POINTS );
            countpointsData2.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POLYGONS );
            countpointsAlg.addInputData( countpointsData2 );
            allAlgs.add( countpointsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // delaunay algorithm
            String delaunayName = "delaunay";
            GeoAlgorithmWithData delaunayAlg = new GeoAlgorithmWithData(
                                                                         Sextante.getAlgorithmFromCommandLineName( delaunayName ) );
            // add test data
            LinkedList<ExampleData> delaunayData1 = new LinkedList<ExampleData>();
            delaunayData1.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POINTS );
            delaunayAlg.addInputData( delaunayData1 );
            LinkedList<ExampleData> delaunayData2 = new LinkedList<ExampleData>();
            delaunayData2.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTIPOINTS );
            delaunayAlg.addInputData( delaunayData2 );
            allAlgs.add( delaunayAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // difference algorithm
            String differenceName = "difference";
            GeoAlgorithmWithData differenceAlg = new GeoAlgorithmWithData(
                                                                           Sextante.getAlgorithmFromCommandLineName( differenceName ) );
            // add test data
            LinkedList<ExampleData> differenceData1 = new LinkedList<ExampleData>();
            differenceData1.add( GeometryExampleData.GML_31_POLYGON_2 );
            differenceData1.add( GeometryExampleData.GML_31_POLYGON );
            differenceAlg.addInputData( differenceData1 );
            allAlgs.add( differenceAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // extractendpointsoflines algorithm
            String extractendpointsoflinesName = "extractendpointsoflines";
            GeoAlgorithmWithData extractendpointsoflinesAlg = new GeoAlgorithmWithData(
                                                                                        Sextante.getAlgorithmFromCommandLineName( extractendpointsoflinesName ) );
            // add all test data
            LinkedList<? extends ExampleData> extractendpointsoflinesData = GeometryExampleData.getData( GeometryType.LINE );
            for ( ExampleData data : extractendpointsoflinesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                extractendpointsoflinesAlg.addInputData( list );
            }
            allAlgs.add( extractendpointsoflinesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // extractnodes algorithm
            String extractnodesName = "extractnodes";
            GeoAlgorithmWithData extractnodesAlg = new GeoAlgorithmWithData(
                                                                             Sextante.getAlgorithmFromCommandLineName( extractnodesName ) );
            // add all test data
            LinkedList<? extends ExampleData> extractnodesData = GeometryExampleData.getData( GeometryType.LINE );

            for ( ExampleData data : extractnodesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                extractnodesAlg.addInputData( list );
            }
            allAlgs.add( extractnodesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // geometricproperties algorithm
            String geometricpropertiesName = "geometricproperties";
            GeoAlgorithmWithData geometricpropertiesAlg = new GeoAlgorithmWithData(
                                                                                    Sextante.getAlgorithmFromCommandLineName( geometricpropertiesName ) );
            // add all test data
            LinkedList<? extends ExampleData> geometricpropertiesData = GeometryExampleData.getData( GeometryType.POLYGON );
            for ( ExampleData data : geometricpropertiesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                geometricpropertiesAlg.addInputData( list );
            }
            allAlgs.add( geometricpropertiesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // geometricpropertieslines algorithm
            String geometricpropertieslinesName = "geometricpropertieslines";
            GeoAlgorithmWithData geometricpropertieslinesAlg = new GeoAlgorithmWithData(
                                                                                         Sextante.getAlgorithmFromCommandLineName( geometricpropertieslinesName ) );
            // add all test data
            LinkedList<? extends ExampleData> geometricpropertieslinesData = GeometryExampleData.getData( GeometryType.LINE );
            for ( ExampleData data : geometricpropertieslinesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                geometricpropertieslinesAlg.addInputData( list );
            }
            allAlgs.add( geometricpropertieslinesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // geometriestopoints algorithm
            String geometriestopointsName = "geometriestopoints";
            GeoAlgorithmWithData geometriestopointsAlg = new GeoAlgorithmWithData(
                                                                                   Sextante.getAlgorithmFromCommandLineName( geometriestopointsName ) );
            // add all test data
            LinkedList<? extends ExampleData> geometriestopointsData = GeometryExampleData.getAllData();
            for ( ExampleData data : geometriestopointsData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                geometriestopointsAlg.addInputData( list );
            }
            allAlgs.add( geometriestopointsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // intersection algorithm
            String intersectionName = "intersection";
            GeoAlgorithmWithData intersectionAlg = new GeoAlgorithmWithData(
                                                                             Sextante.getAlgorithmFromCommandLineName( intersectionName ) );
            // add test data
            LinkedList<ExampleData> intersectionData1 = new LinkedList<ExampleData>();
            intersectionData1.add( GeometryExampleData.GML_31_POLYGON );
            intersectionData1.add( GeometryExampleData.GML_31_POLYGON_2 );
            intersectionAlg.addInputData( intersectionData1 );
            allAlgs.add( intersectionAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // nodelines algorithm
            String nodelinesName = "nodelines";
            GeoAlgorithmWithData nodelinesAlg = new GeoAlgorithmWithData(
                                                                          Sextante.getAlgorithmFromCommandLineName( nodelinesName ) );
            // add all test data
            LinkedList<? extends ExampleData> nodelinesData = GeometryExampleData.getData( GeometryType.LINE );
            for ( ExampleData data : nodelinesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                nodelinesAlg.addInputData( list );
            }
            allAlgs.add( nodelinesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // pointcoordinates algorithm
            String pointcoordinatesName = "pointcoordinates";
            GeoAlgorithmWithData pointcoordinatesAlg = new GeoAlgorithmWithData(
                                                                                 Sextante.getAlgorithmFromCommandLineName( pointcoordinatesName ) );
            // add all test data
            LinkedList<? extends ExampleData> pointcoordinatesData = GeometryExampleData.getData( GeometryType.POINT );
            for ( ExampleData data : pointcoordinatesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                pointcoordinatesAlg.addInputData( list );
            }
            allAlgs.add( pointcoordinatesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // polygonize algorithm
            String polygonizeName = "polygonize";
            GeoAlgorithmWithData polygonizeAlg = new GeoAlgorithmWithData(
                                                                           Sextante.getAlgorithmFromCommandLineName( polygonizeName ) );
            // add all test data
            LinkedList<ExampleData> polygonizeData = new LinkedList<ExampleData>();
            polygonizeData.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTILINESTRINGS );
            polygonizeData.add( GeometryExampleData.GML_31_MULTILINESTRING );
            for ( ExampleData data : polygonizeData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                polygonizeAlg.addInputData( list );
            }
            allAlgs.add( polygonizeAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // polygonstopolylines algorithm
            String polygonstopolylinesName = "polygonstopolylines";
            GeoAlgorithmWithData polygonstopolylinesAlg = new GeoAlgorithmWithData(
                                                                                    Sextante.getAlgorithmFromCommandLineName( polygonstopolylinesName ) );
            // add all test data
            LinkedList<? extends ExampleData> polygonstopolylinesData = GeometryExampleData.getAllData();
            for ( ExampleData data : polygonstopolylinesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                polygonstopolylinesAlg.addInputData( list );
            }
            allAlgs.add( polygonstopolylinesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // polylinestopolygons algorithm
            String polylinestopolygonsName = "polylinestopolygons";
            GeoAlgorithmWithData polylinestopolygonsAlg = new GeoAlgorithmWithData(
                                                                                    Sextante.getAlgorithmFromCommandLineName( polylinestopolygonsName ) );
            // add all test data
            LinkedList<? extends ExampleData> polylinestopolygonsData = GeometryExampleData.getData( GeometryType.LINE );
            for ( ExampleData data : polylinestopolygonsData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                polylinestopolygonsAlg.addInputData( list );
            }
            allAlgs.add( polylinestopolygonsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // polylinestosinglesegments algorithm
            String polylinestosinglesegmentsName = "polylinestosinglesegments";
            GeoAlgorithmWithData polylinestosinglesegmentsAlg = new GeoAlgorithmWithData(
                                                                                          Sextante.getAlgorithmFromCommandLineName( polylinestosinglesegmentsName ) );
            // add all test data
            LinkedList<? extends ExampleData> polylinestosinglesegmentsData = GeometryExampleData.getData( GeometryType.LINE );
            for ( ExampleData data : polylinestosinglesegmentsData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                polylinestosinglesegmentsAlg.addInputData( list );
            }
            allAlgs.add( polylinestosinglesegmentsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // removeholes algorithm
            String removeholesName = "removeholes";
            GeoAlgorithmWithData removeholesAlg = new GeoAlgorithmWithData(
                                                                            Sextante.getAlgorithmFromCommandLineName( removeholesName ) );
            // add all test data
            LinkedList<? extends ExampleData> removeholesData = GeometryExampleData.getData( GeometryType.POLYGON );
            for ( ExampleData data : removeholesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                removeholesAlg.addInputData( list );
            }
            allAlgs.add( removeholesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // removerepeatedgeometries algorithm
            String removerepeatedgeometriesName = "removerepeatedgeometries";
            GeoAlgorithmWithData removerepeatedgeometriesAlg = new GeoAlgorithmWithData(
                                                                                         Sextante.getAlgorithmFromCommandLineName( removerepeatedgeometriesName ) );
            // add all test data
            LinkedList<? extends ExampleData> removerepeatedgeometriesData = GeometryExampleData.getAllData();
            for ( ExampleData data : removerepeatedgeometriesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                removerepeatedgeometriesAlg.addInputData( list );
            }
            allAlgs.add( removerepeatedgeometriesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // splitmultipart algorithm
            String splitmultipartName = "splitmultipart";
            GeoAlgorithmWithData splitmultipartAlg = new GeoAlgorithmWithData(
                                                                               Sextante.getAlgorithmFromCommandLineName( splitmultipartName ) );
            // add all test data
            LinkedList<? extends ExampleData> splitmultipartData = GeometryExampleData.getAllData();
            for ( ExampleData data : splitmultipartData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                splitmultipartAlg.addInputData( list );
            }
            allAlgs.add( splitmultipartAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // splitpolylinesatnodes algorithm
            String splitpolylinesatnodesName = "splitpolylinesatnodes";
            GeoAlgorithmWithData splitpolylinesatnodesAlg = new GeoAlgorithmWithData(
                                                                                      Sextante.getAlgorithmFromCommandLineName( splitpolylinesatnodesName ) );
            // add all test data
            LinkedList<? extends ExampleData> splitpolylinesatnodesData = GeometryExampleData.getData( GeometryType.LINE );
            for ( ExampleData data : splitpolylinesatnodesData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                splitpolylinesatnodesAlg.addInputData( list );
            }
            allAlgs.add( splitpolylinesatnodesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // symdifference algorithm
            String symdifferenceName = "symdifference";
            GeoAlgorithmWithData symdifferenceAlg = new GeoAlgorithmWithData(
                                                                              Sextante.getAlgorithmFromCommandLineName( symdifferenceName ) );
            // add test data
            LinkedList<ExampleData> symdifferenceData1 = new LinkedList<ExampleData>();
            symdifferenceData1.add( GeometryExampleData.GML_31_POLYGON_2 );
            symdifferenceData1.add( GeometryExampleData.GML_31_POLYGON );
            symdifferenceAlg.addInputData( symdifferenceData1 );
            allAlgs.add( symdifferenceAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // union algorithm
            String unionName = "union";
            GeoAlgorithmWithData unionAlg = new GeoAlgorithmWithData(
                                                                      Sextante.getAlgorithmFromCommandLineName( unionName ) );
            // add test data
            LinkedList<ExampleData> unionData1 = new LinkedList<ExampleData>();
            unionData1.add( GeometryExampleData.GML_31_POLYGON_2 );
            unionData1.add( GeometryExampleData.GML_31_POLYGON );
            unionAlg.addInputData( unionData1 );
            allAlgs.add( unionAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // vectormean algorithm
            String vectormeanName = "vectormean";
            GeoAlgorithmWithData vectormeanAlg = new GeoAlgorithmWithData(
                                                                           Sextante.getAlgorithmFromCommandLineName( vectormeanName ) );
            // add all test data
            LinkedList<? extends ExampleData> vectormeanData = GeometryExampleData.getData( GeometryType.LINE );
            for ( ExampleData data : vectormeanData ) {
                LinkedList<ExampleData> list = new LinkedList<ExampleData>();
                list.add( data );
                vectormeanAlg.addInputData( list );
            }
            allAlgs.add( vectormeanAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // clipbyrectangle algorithm
            String clipbyrectangleName = "clipbyrectangle";
            GeoAlgorithmWithData clipbyrectangleAlg = new GeoAlgorithmWithData(
                                                                                Sextante.getAlgorithmFromCommandLineName( clipbyrectangleName ) );
            // add test data
            LinkedList<ExampleData> clipbyrectangleData1 = new LinkedList<ExampleData>();
            clipbyrectangleData1.add( GeometryExampleData.GML_31_POLYGON_2 );
            clipbyrectangleData1.add( LiteralExampleData.NUMERICAL_VALUE_0 );
            clipbyrectangleData1.add( LiteralExampleData.NUMERICAL_VALUE_100 );
            clipbyrectangleData1.add( LiteralExampleData.NUMERICAL_VALUE_0 );
            clipbyrectangleData1.add( LiteralExampleData.NUMERICAL_VALUE_100 );
            clipbyrectangleAlg.addInputData( clipbyrectangleData1 );
            allAlgs.add( clipbyrectangleAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // groupnearfeatures algorithm
            String groupnearfeaturesName = "groupnearfeatures";
            GeoAlgorithmWithData groupnearfeaturesAlg = new GeoAlgorithmWithData(
                                                                                  Sextante.getAlgorithmFromCommandLineName( groupnearfeaturesName ) );
            // add test data
            LinkedList<ExampleData> groupnearfeaturesData1 = new LinkedList<ExampleData>();
            groupnearfeaturesData1.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POLYGONS );
            groupnearfeaturesData1.add( LiteralExampleData.NUMERICAL_VALUE_1 );
            groupnearfeaturesAlg.addInputData( groupnearfeaturesData1 );
            allAlgs.add( groupnearfeaturesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // joinadjacentlines algorithm
            String joinadjacentlinesName = "joinadjacentlines";
            GeoAlgorithmWithData joinadjacentlinesAlg = new GeoAlgorithmWithData(
                                                                                  Sextante.getAlgorithmFromCommandLineName( joinadjacentlinesName ) );
            // add test data
            LinkedList<ExampleData> joinadjacentlinesData1 = new LinkedList<ExampleData>();
            joinadjacentlinesData1.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_LINESTRINGS );
            joinadjacentlinesData1.add( LiteralExampleData.NUMERICAL_VALUE_100 );
            joinadjacentlinesAlg.addInputData( joinadjacentlinesData1 );
            allAlgs.add( joinadjacentlinesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // linestoequispacedpoints algorithm
            String linestoequispacedpointsName = "linestoequispacedpoints";
            GeoAlgorithmWithData linestoequispacedpointsAlg = new GeoAlgorithmWithData(
                                                                                        Sextante.getAlgorithmFromCommandLineName( linestoequispacedpointsName ) );
            // add test data
            LinkedList<ExampleData> linestoequispacedpointsData1 = new LinkedList<ExampleData>();
            linestoequispacedpointsData1.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_LINESTRINGS );
            linestoequispacedpointsData1.add( LiteralExampleData.NUMERICAL_VALUE_100 );
            linestoequispacedpointsAlg.addInputData( linestoequispacedpointsData1 );
            allAlgs.add( linestoequispacedpointsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // perturbatepointslayer algorithm
            String perturbatepointslayerName = "perturbatepointslayer";
            GeoAlgorithmWithData perturbatepointslayerAlg = new GeoAlgorithmWithData(
                                                                                      Sextante.getAlgorithmFromCommandLineName( perturbatepointslayerName ) );
            // add test data
            LinkedList<ExampleData> perturbatepointslayerData1 = new LinkedList<ExampleData>();
            perturbatepointslayerData1.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POINTS );
            perturbatepointslayerData1.add( LiteralExampleData.NUMERICAL_VALUE_100 );
            perturbatepointslayerData1.add( LiteralExampleData.NUMERICAL_VALUE_200 );
            perturbatepointslayerAlg.addInputData( perturbatepointslayerData1 );
            allAlgs.add( perturbatepointslayerAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // snappoints algorithm
            String snappointsName = "snappoints";
            GeoAlgorithmWithData snappointsAlg = new GeoAlgorithmWithData(
                                                                           Sextante.getAlgorithmFromCommandLineName( snappointsName ) );
            // add test data
            LinkedList<ExampleData> snappointsData1 = new LinkedList<ExampleData>();
            snappointsData1.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POINTS );
            snappointsData1.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_LINESTRINGS );
            snappointsData1.add( LiteralExampleData.NUMERICAL_VALUE_100 );
            snappointsAlg.addInputData( snappointsData1 );
            allAlgs.add( snappointsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // transform algorithm
            String transformName = "transform";
            GeoAlgorithmWithData transformAlg = new GeoAlgorithmWithData(
                                                                          Sextante.getAlgorithmFromCommandLineName( transformName ) );
            // add test data
            LinkedList<ExampleData> transformData1 = new LinkedList<ExampleData>();
            transformData1.add( GeometryExampleData.GML_31_POINT );
            transformData1.add( LiteralExampleData.NUMERICAL_VALUE_100 );
            transformData1.add( LiteralExampleData.NUMERICAL_VALUE_200 );
            transformData1.add( LiteralExampleData.NUMERICAL_VALUE_0 );
            transformData1.add( LiteralExampleData.NUMERICAL_VALUE_1 );
            transformData1.add( LiteralExampleData.NUMERICAL_VALUE_1 );
            transformData1.add( LiteralExampleData.NUMERICAL_VALUE_0 );
            transformData1.add( LiteralExampleData.NUMERICAL_VALUE_0 );
            transformAlg.addInputData( transformData1 );
            allAlgs.add( transformAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // vectorspatialcluster algorithm
            String vectorspatialclusterName = "vectorspatialcluster";
            GeoAlgorithmWithData vectorspatialclusterAlg = new GeoAlgorithmWithData(
                                                                                     Sextante.getAlgorithmFromCommandLineName( vectorspatialclusterName ) );
            // add test data
            LinkedList<ExampleData> vectorspatialclusterData1 = new LinkedList<ExampleData>();
            vectorspatialclusterData1.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POINTS );
            vectorspatialclusterData1.add( LiteralExampleData.NUMERICAL_VALUE_100 );
            vectorspatialclusterAlg.addInputData( vectorspatialclusterData1 );
            allAlgs.add( vectorspatialclusterAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // fixeddistancebuffer algorithm
            String fixeddistancebufferName = "fixeddistancebuffer";
            GeoAlgorithmWithData fixeddistancebufferAlg = new GeoAlgorithmWithData(
                                                                                    Sextante.getAlgorithmFromCommandLineName( fixeddistancebufferName ) );

            // add test data
            LinkedList<ExampleData> fixeddistancebufferData1 = new LinkedList<ExampleData>();
            fixeddistancebufferData1.add( GeometryExampleData.GML_31_POLYGON );
            fixeddistancebufferData1.add( LiteralExampleData.NUMERICAL_VALUE_1 ); // DISTANCE: 1
            fixeddistancebufferData1.add( LiteralExampleData.SELECTION_0 ); // TYPE: BUFFER_OUTSIDE_POLY
            fixeddistancebufferData1.add( LiteralExampleData.SELECTION_0 ); // RINGS: 0
            fixeddistancebufferData1.add( LiteralExampleData.BOOLEAN_FALSE ); // NOTROUNDED: false
            fixeddistancebufferAlg.addInputData( fixeddistancebufferData1 );
            LinkedList<ExampleData> fixeddistancebufferData2 = new LinkedList<ExampleData>();
            fixeddistancebufferData2.add( GeometryExampleData.GML_31_POLYGON );
            fixeddistancebufferData2.add( LiteralExampleData.NUMERICAL_VALUE_1 ); // DISTANCE: 1
            fixeddistancebufferData2.add( LiteralExampleData.SELECTION_1 ); // TYPE: BUFFER_INSIDE_POLY
            fixeddistancebufferData2.add( LiteralExampleData.SELECTION_0 ); // RINGS: 0
            fixeddistancebufferData2.add( LiteralExampleData.BOOLEAN_FALSE ); // NOTROUNDED: false
            fixeddistancebufferAlg.addInputData( fixeddistancebufferData2 );
            LinkedList<ExampleData> fixeddistancebufferData3 = new LinkedList<ExampleData>();
            fixeddistancebufferData3.add( GeometryExampleData.GML_31_POLYGON );
            fixeddistancebufferData3.add( LiteralExampleData.NUMERICAL_VALUE_1 ); // DISTANCE: 1
            fixeddistancebufferData3.add( LiteralExampleData.SELECTION_2 ); // TYPE: BUFFER_INSIDE_OUTSIDE_POLY
            fixeddistancebufferData3.add( LiteralExampleData.SELECTION_0 ); // RINGS: 0
            fixeddistancebufferData3.add( LiteralExampleData.BOOLEAN_FALSE ); // NOTROUNDED: true
            fixeddistancebufferAlg.addInputData( fixeddistancebufferData3 );
            LinkedList<ExampleData> fixeddistancebufferData4 = new LinkedList<ExampleData>();
            fixeddistancebufferData4.add( GeometryExampleData.GML_31_POINT );
            fixeddistancebufferData4.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // DISTANCE: 10
            fixeddistancebufferData4.add( LiteralExampleData.SELECTION_0 ); // TYPE: BUFFER_OUTSIDE_POLY
            fixeddistancebufferData4.add( LiteralExampleData.SELECTION_2 ); // RINGS: 2
            fixeddistancebufferData4.add( LiteralExampleData.BOOLEAN_TRUE ); // NOTROUNDED: true
            fixeddistancebufferAlg.addInputData( fixeddistancebufferData4 );
            LinkedList<ExampleData> fixeddistancebufferData5 = new LinkedList<ExampleData>();
            fixeddistancebufferData5.add( GeometryExampleData.GML_31_POINT );
            fixeddistancebufferData5.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // DISTANCE: 10
            fixeddistancebufferData5.add( LiteralExampleData.SELECTION_0 ); // TYPE: BUFFER_OUTSIDE_POLY
            fixeddistancebufferData5.add( LiteralExampleData.SELECTION_1 ); // RINGS: 1
            fixeddistancebufferData5.add( LiteralExampleData.BOOLEAN_TRUE ); // NOTROUNDED: true
            fixeddistancebufferAlg.addInputData( fixeddistancebufferData5 );
            LinkedList<ExampleData> fixeddistancebufferData6 = new LinkedList<ExampleData>();
            fixeddistancebufferData6.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTIPOLYGONS );
            fixeddistancebufferData6.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // DISTANCE: 10
            fixeddistancebufferData6.add( LiteralExampleData.SELECTION_0 ); // TYPE: BUFFER_OUTSIDE_POLY
            fixeddistancebufferData6.add( LiteralExampleData.SELECTION_1 ); // RINGS: 1
            fixeddistancebufferData6.add( LiteralExampleData.BOOLEAN_FALSE ); // NOTROUNDED: false
            fixeddistancebufferAlg.addInputData( fixeddistancebufferData6 );
            allAlgs.add( fixeddistancebufferAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // generateroutes algorithm
            String generateroutesName = "generateroutes";
            GeoAlgorithmWithData generateroutesAlg = new GeoAlgorithmWithData(
                                                                               Sextante.getAlgorithmFromCommandLineName( generateroutesName ) );
            // add test data
            LinkedList<ExampleData> generateroutesData1 = new LinkedList<ExampleData>();
            generateroutesData1.add( GeometryExampleData.GML_31_LINESTRING ); // ROUTE
            generateroutesData1.add( LiteralExampleData.NUMERICAL_VALUE_1 ); // NROUTES: 1
            generateroutesData1.add( LiteralExampleData.SELECTION_1 ); // METHOD: CREATION_METHOD_RECOMBINE
            generateroutesData1.add( LiteralExampleData.NUMERICAL_VALUE_1 ); // SINUOSITY: 1
            generateroutesData1.add( LiteralExampleData.BOOLEAN_TRUE ); // USESINUOSITY: true
            generateroutesAlg.addInputData( generateroutesData1 );
            LinkedList<ExampleData> generateroutesData2 = new LinkedList<ExampleData>();
            generateroutesData2.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTILINESTRINGS ); // ROUTE
            generateroutesData2.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // NROUTES: 10
            generateroutesData2.add( LiteralExampleData.SELECTION_1 ); // METHOD: CREATION_METHOD_RECOMBINE
            generateroutesData2.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // SINUOSITY: 10
            generateroutesData2.add( LiteralExampleData.BOOLEAN_FALSE ); // USESINUOSITY: false
            generateroutesAlg.addInputData( generateroutesData2 );
            allAlgs.add( generateroutesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // simplifylines algorithm
            String simplifylinesName = "simplifylines";
            GeoAlgorithmWithData simplifylinesAlg = new GeoAlgorithmWithData(
                                                                              Sextante.getAlgorithmFromCommandLineName( simplifylinesName ) );
            // add test data
            LinkedList<ExampleData> simplifylinesData1 = new LinkedList<ExampleData>();
            simplifylinesData1.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_LINESTRINGS );
            simplifylinesData1.add( LiteralExampleData.NUMERICAL_VALUE_100 ); // TOLERANCE: 100
            simplifylinesData1.add( LiteralExampleData.BOOLEAN_FALSE ); // PRESERVE: false
            simplifylinesAlg.addInputData( simplifylinesData1 );
            LinkedList<ExampleData> simplifylinesData2 = new LinkedList<ExampleData>();
            simplifylinesData2.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTILINESTRINGS );
            simplifylinesData2.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // TOLERANCE: 10
            simplifylinesData2.add( LiteralExampleData.BOOLEAN_TRUE ); // PRESERVE: true
            simplifylinesAlg.addInputData( simplifylinesData2 );
            allAlgs.add( simplifylinesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // simplifypolygons algorithm
            String simplifypolygonsName = "simplifypolygons";
            GeoAlgorithmWithData simplifypolygonsAlg = new GeoAlgorithmWithData(
                                                                                 Sextante.getAlgorithmFromCommandLineName( simplifypolygonsName ) );
            // add test data
            LinkedList<ExampleData> simplifypolygonsData1 = new LinkedList<ExampleData>();
            simplifypolygonsData1.add( GeometryExampleData.GML_31_POLYGON );
            simplifypolygonsData1.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // TOLERANCE: 10
            simplifypolygonsData1.add( LiteralExampleData.BOOLEAN_FALSE ); // PRESERVE: false
            simplifypolygonsAlg.addInputData( simplifypolygonsData1 );
            LinkedList<ExampleData> simplifypolygonsData2 = new LinkedList<ExampleData>();
            simplifypolygonsData2.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTIPOLYGONS );
            simplifypolygonsData2.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // TOLERANCE: 10
            simplifypolygonsData2.add( LiteralExampleData.BOOLEAN_TRUE ); // PRESERVE: true
            simplifypolygonsAlg.addInputData( simplifypolygonsData2 );
            allAlgs.add( simplifypolygonsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // smoothlines algorithm
            String smoothlinesName = "smoothlines";
            GeoAlgorithmWithData smoothlinesAlg = new GeoAlgorithmWithData(
                                                                            Sextante.getAlgorithmFromCommandLineName( smoothlinesName ) );
            // add test data
            LinkedList<ExampleData> smoothlinesData1 = new LinkedList<ExampleData>();
            smoothlinesData1.add( GeometryExampleData.GML_31_LINESTRING );
            smoothlinesData1.add( LiteralExampleData.SELECTION_3 ); // INTERMEDIATE_POINTS: 3
            smoothlinesData1.add( LiteralExampleData.SELECTION_0 ); // CURVE_TYPE: NATURAL_CUBIC_SPLINES
            smoothlinesAlg.addInputData( smoothlinesData1 );
            LinkedList<ExampleData> smoothlinesData2 = new LinkedList<ExampleData>();
            smoothlinesData2.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_LINESTRINGS );
            smoothlinesData2.add( LiteralExampleData.SELECTION_5 ); // INTERMEDIATE_POINTS: 5
            smoothlinesData2.add( LiteralExampleData.SELECTION_1 ); // CURVE_TYPE: BEZIER_CURVES
            smoothlinesAlg.addInputData( smoothlinesData2 );
            LinkedList<ExampleData> smoothlinesData3 = new LinkedList<ExampleData>();
            smoothlinesData3.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTILINESTRINGS );
            smoothlinesData3.add( LiteralExampleData.SELECTION_7 ); // INTERMEDIATE_POINTS: 7
            smoothlinesData3.add( LiteralExampleData.SELECTION_2 ); // CURVE_TYPE: BSPLINES
            smoothlinesAlg.addInputData( smoothlinesData3 );
            LinkedList<ExampleData> smoothlinesData4 = new LinkedList<ExampleData>();
            smoothlinesData4.add( GeometryExampleData.GML_31_MULTILINESTRING );
            smoothlinesData4.add( LiteralExampleData.SELECTION_6 ); // INTERMEDIATE_POINTS: 6
            smoothlinesData4.add( LiteralExampleData.SELECTION_1 ); // CURVE_TYPE: NATURAL_CUBIC_SPLINES
            smoothlinesAlg.addInputData( smoothlinesData4 );
            allAlgs.add( smoothlinesAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // splitlineswithpoints algorithm
            String splitlineswithpointsName = "splitlineswithpoints";
            GeoAlgorithmWithData splitlineswithpointsAlg = new GeoAlgorithmWithData(
                                                                                     Sextante.getAlgorithmFromCommandLineName( splitlineswithpointsName ) );
            // add test data
            LinkedList<ExampleData> splitlineswithpointsData1 = new LinkedList<ExampleData>();
            splitlineswithpointsData1.add( GeometryExampleData.GML_31_LINESTRING ); // LINES
            splitlineswithpointsData1.add( GeometryExampleData.GML_31_POINT ); // POINTS
            splitlineswithpointsData1.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // TOLERANCE: 10
            splitlineswithpointsData1.add( LiteralExampleData.SELECTION_0 ); // METHOD: 10
            splitlineswithpointsAlg.addInputData( splitlineswithpointsData1 );
            LinkedList<ExampleData> splitlineswithpointsData2 = new LinkedList<ExampleData>();
            splitlineswithpointsData2.add( GeometryExampleData.GML_31_MULTILINESTRING ); // LINES
            splitlineswithpointsData2.add( GeometryExampleData.GML_31_MULTIPOINT ); // POINTS
            splitlineswithpointsData2.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // TOLERANCE: 10
            splitlineswithpointsData2.add( LiteralExampleData.SELECTION_1 ); // METHOD: 10
            splitlineswithpointsAlg.addInputData( splitlineswithpointsData2 );
            LinkedList<ExampleData> splitlineswithpointsData3 = new LinkedList<ExampleData>();
            splitlineswithpointsData3.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_LINESTRINGS ); // LINES
            splitlineswithpointsData3.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POINTS ); // POINTS
            splitlineswithpointsData3.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // TOLERANCE: 10
            splitlineswithpointsData3.add( LiteralExampleData.SELECTION_0 ); // METHOD: 10
            splitlineswithpointsAlg.addInputData( splitlineswithpointsData3 );
            LinkedList<ExampleData> splitlineswithpointsData4 = new LinkedList<ExampleData>();
            splitlineswithpointsData4.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTILINESTRINGS ); // LINES
            splitlineswithpointsData4.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTIPOINTS ); // POINTS
            splitlineswithpointsData4.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // TOLERANCE: 10
            splitlineswithpointsData4.add( LiteralExampleData.SELECTION_1 ); // METHOD: 10
            splitlineswithpointsAlg.addInputData( splitlineswithpointsData4 );
            allAlgs.add( splitlineswithpointsAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // vectoraddfield algorithm
            String vectoraddfieldName = "vectoraddfield";
            GeoAlgorithmWithData vectoraddfieldAlg = new GeoAlgorithmWithData(
                                                                               Sextante.getAlgorithmFromCommandLineName( vectoraddfieldName ) );
            // add test data
            LinkedList<ExampleData> vectoraddfieldData1 = new LinkedList<ExampleData>();
            vectoraddfieldData1.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POINTS ); // INPUT
            vectoraddfieldData1.add( LiteralExampleData.STRING_VIEW ); // FIELD_NAME: VIEWS
            vectoraddfieldData1.add( LiteralExampleData.SELECTION_0 ); // FIELD_TYPE: INTEGER
            vectoraddfieldData1.add( LiteralExampleData.NUMERICAL_VALUE_1 ); // FIELD_LENGTH: 1
            vectoraddfieldData1.add( LiteralExampleData.NUMERICAL_VALUE_10 ); // FIELD_PRECISION: 10
            vectoraddfieldData1.add( LiteralExampleData.STRING_0 ); // DEFAULT_VALUE: 0
            vectoraddfieldAlg.addInputData( vectoraddfieldData1 );
            allAlgs.add( vectoraddfieldAlg );

            // ---------------------------------------------------------------------------------------------------------------------------
            // vectorcluster algorithm
            String vectorclusterName = "vectorcluster";
            GeoAlgorithmWithData vectorclusterAlg = new GeoAlgorithmWithData(
                                                                              Sextante.getAlgorithmFromCommandLineName( vectorclusterName ) );
            // add test data
            LinkedList<ExampleData> vectorclusterData1 = new LinkedList<ExampleData>();
            vectorclusterData1.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POLYGONS ); // LAYER
            vectorclusterData1.add( LiteralExampleData.STRING_NAME_UPPERNAME_DATAORIGIN_AREA_QUERYBBOXOVERLAP ); // FIELDS
            vectorclusterData1.add( LiteralExampleData.NUMERICAL_VALUE_5 ); // NUMCLASS: 5
            vectorclusterAlg.addInputData( vectorclusterData1 );
            allAlgs.add( vectorclusterAlg );

        }

        LOG.info( "FOUND DATA FOR " + allAlgs.size() + " ALGORITHMS" );

        return allAlgs;
    }
}
