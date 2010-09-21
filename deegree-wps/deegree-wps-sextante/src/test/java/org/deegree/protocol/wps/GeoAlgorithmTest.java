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

    public GeoAlgorithmWithData getAlgorithm( String commandLineName ) {

        // determine algorithm
        for ( GeoAlgorithmWithData alg : algorithms ) {
            if ( alg.getCommandLineName().equals( commandLineName ) )
                return alg;
        }

        return null;
    }

    private LinkedList<GeoAlgorithmWithData> getAllSupportedAlgorithms() {

        Sextante.initialize();
        LinkedList<GeoAlgorithmWithData> allAlgs = new LinkedList<GeoAlgorithmWithData>();

        boolean getAll = false;

        if ( !getAll ) {// return only one algorithm

            // ---------------------------------------------------------------------------------------------------------------------------
            // groupnearfeatures algorithm
            String groupnearfeaturesName = "groupnearfeatures";
            GeoAlgorithmWithData groupnearfeaturesAlg = new GeoAlgorithmWithData(
                                                                                  Sextante.getAlgorithmFromCommandLineName( groupnearfeaturesName ) );
            // add test data
            LinkedList<ExampleData> groupnearfeaturesData1 = new LinkedList<ExampleData>();
            groupnearfeaturesData1.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POLYGONS );
            groupnearfeaturesData1.add( LiteralExampleData.NUMERICAL_VALUE_0 );
            groupnearfeaturesAlg.addInputData( groupnearfeaturesData1 );
            allAlgs.add( groupnearfeaturesAlg );

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
            LinkedList<? extends ExampleData> clipLayerData = GeometryExampleData.getAllData();
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
        }

        LOG.info( "FOUND DATA FOR " + allAlgs.size() + " ALGORITHMS" );

        return allAlgs;
    }

}

// /**
// * Returns a list of all supported {@link GeoAlgorithm} as {@link GeoAlgorithmWithData} for testing.
// *
// * @return List of all supported {@link GeoAlgorithm} as {@link GeoAlgorithmWithData}.
// */
// @SuppressWarnings("unchecked")
// private LinkedList<GeoAlgorithmWithData> getAlgorithms() {
// LinkedList<GeoAlgorithmWithData> algs = new LinkedList<GeoAlgorithmWithData>();
//
// // test all algorithms?
// boolean testAll = false;
//
// if ( !testAll ) {
// // test only one algorithm
// Sextante.initialize();
// HashMap<String, GeoAlgorithm> sextanteAlgs = Sextante.getAlgorithms();
// GeoAlgorithm geoAlg = sextanteAlgs.get( "splitmultipart" );
// GeoAlgorithmWithData testAlg = new GeoAlgorithmWithData( geoAlg );
// testAlg.addAllInputData( getInputData( geoAlg ) );
// algs.add( testAlg );
//
// } else {
//
// // get SEXTANTE configuration
// SextanteProcesses config = null;
// try {
// JAXBContext jc = JAXBContext.newInstance( "org.deegree.services.wps.provider.sextante.jaxb" );
// Unmarshaller unmarshaller = jc.createUnmarshaller();
// config = (SextanteProcesses) unmarshaller.unmarshal( new URL(
// "file:/home/pabel/workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/deegree-wps-demo/WEB-INF/workspace/services/processes/Sextante.xml"
// ) );
// } catch ( JAXBException e ) {
// throw new IllegalArgumentException( e.getMessage(), e );
// } catch ( MalformedURLException e ) {
// // ToDo ??
// e.printStackTrace();
// }
//
// // test all supported algorithms
// GeoAlgorithm[] geoalgs = SextanteProcessProvider.getSupportedAlgorithms( config );
// for ( int i = 0; i < geoalgs.length; i++ ) {
// GeoAlgorithm geoAlg = geoalgs[i];
//
// GeoAlgorithmWithData testAlg = new GeoAlgorithmWithData( geoAlg );
// testAlg.addAllInputData( getInputData( geoAlg ) );
// algs.add( testAlg );
// }
// }
//
// return algs;
// }
// /**
// * This method determine test data for a SEXTANTE {@link GeoAlgorithm}.
// *
// * @param alg
// * SEXTANTE {@link GeoAlgorithm}.
// * @return A list of a list of test data. If the algorithm need only one input parameter, returns only one list of
// * test data in the list. If the algorithm need more than one input parameter, returns for every input
// * parameter a list of test data in the list.
// */
// private LinkedList<LinkedList<ExampleData>> getInputData( GeoAlgorithm alg ) {
//
// // all input data
// LinkedList<LinkedList<ExampleData>> allData = new LinkedList<LinkedList<ExampleData>>();
//
// // example data
// LinkedList<ExampleData> vectorLayers = getVectorLayerInput( alg );
// LinkedList<ExampleData> nummericalValues = getNumericalValuesInput( alg );
//
// // example data iterators
// Iterator<ExampleData> vectorLayersIterator = vectorLayers.iterator();
// Iterator<ExampleData> numericalValuesIterator = nummericalValues.iterator();
//
// // traverse input parameter of one execution
// ParametersSet paramSet = alg.getParameters();
//
// // traverse all example data
// boolean addMoreData = true;
// while ( addMoreData ) {
//
// // input data of one execution
// LinkedList<ExampleData> dataList = new LinkedList<ExampleData>();
//
// for ( int j = 0; j < paramSet.getNumberOfParameters(); j++ ) {
// Parameter param = paramSet.getParameter( j );
//
// // vector layers
// if ( param.getParameterTypeName().equals( SextanteWPSProcess.VECTOR_LAYER_INPUT ) )
//
// if ( vectorLayersIterator.hasNext() ) {
// dataList.add( vectorLayersIterator.next() );
// } else {
// addMoreData = false;
// break;
// }
// else
//
// // numerical values
// if ( param.getParameterTypeName().equals( SextanteWPSProcess.NUMERICAL_VALUE_INPUT ) ) {
//
// if ( numericalValuesIterator.hasNext() ) {
// dataList.add( numericalValuesIterator.next() );
// } else {
// addMoreData = false;
// break;
// }
// }
//
// }
// allData.add( dataList );
// }
//
// return allData;
// }
//
// /**
// * Returns a list of all test data for a vector layer of a SEXTANTE {@link GeoAlgorithm}.
// *
// * @param alg
// * SEXTANTE {@link GeoAlgorithm}.
// *
// * @return List of all test data for a layer of a SEXTANTE {@link GeoAlgorithm}.
// */
// private LinkedList<ExampleData> getVectorLayerInput( GeoAlgorithm alg ) {
// LinkedList<ExampleData> layers = new LinkedList<ExampleData>();
//
// // cleanpointslayer algorithm
// if ( alg.getCommandLineName().equals( "cleanpointslayer" ) )
// layers.addAll( GeometryExampleData.getData( GeometryType.POINT ) );
// else
// // st_polylinestosinglesegments algorithm
// if ( alg.getCommandLineName().equals( "polylinestosinglesegments" ) )
// layers.addAll( GeometryExampleData.getData( GeometryType.LINE ) );
// else
// // pointcoordinates algorithm
// if ( alg.getCommandLineName().equals( "pointcoordinates" ) )
// layers.addAll( GeometryExampleData.getData( GeometryType.POINT ) );
// else
// // removeholes algorithm
// if ( alg.getCommandLineName().equals( "removeholes" ) )
// layers.addAll( GeometryExampleData.getData( GeometryType.POLYGON ) );
// else
// // exportvector algorithm
// if ( alg.getCommandLineName().equals( "exportvector" ) )
// // ToDo
// LOG.warn( "No test data for '" + alg.getCommandLineName() + "' available." );
// else
// // polygonize algorithm
// if ( alg.getCommandLineName().equals( "polygonize" ) ) {
// layers.add( GeometryExampleData.GML_31_MULTILINESTRING );
// layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTILINESTRINGS );
// } else
// // difference and intersection algorithm
// if ( alg.getCommandLineName().equals( "difference" ) || alg.getCommandLineName().equals( "intersection" ) ) {
// layers.addAll( GeometryExampleData.getData( GeometryType.POLYGON ) );
// layers.addAll( GeometryExampleData.getData( GeometryType.LINE ) );
// layers.addAll( GeometryExampleData.getData( GeometryType.POINT ) );
// } else
// // union algorithm
// if ( alg.getCommandLineName().equals( "union" ) ) {
//
// // ToDo
// layers.add( GeometryExampleData.GML_31_POLYGON );
// layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTIPOLYGONS );
//
// LOG.warn( "No test data for '" + alg.getCommandLineName() + "' available." );
// } else
// // clip algorithm
// if ( alg.getCommandLineName().equals( "clip" ) ) {
// layers.add( GeometryExampleData.GML_31_POINT );
// layers.add( GeometryExampleData.GML_31_POLYGON );
//
// layers.add( GeometryExampleData.GML_31_LINESTRING );
// layers.add( GeometryExampleData.GML_31_POLYGON_2 );
//
// layers.add( GeometryExampleData.GML_31_POLYGON );
// layers.add( GeometryExampleData.GML_31_POLYGON_2 );
//
// layers.add( GeometryExampleData.GML_31_MULTIPOINT );
// layers.add( GeometryExampleData.GML_31_POLYGON );
//
// layers.add( GeometryExampleData.GML_31_MULTILINESTRING );
// layers.add( GeometryExampleData.GML_31_POLYGON_2 );
//
// layers.add( GeometryExampleData.GML_31_MULTILPOLYGON );
// layers.add( GeometryExampleData.GML_31_POLYGON_2 );
//
// layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POINTS );
// layers.add( GeometryExampleData.GML_31_POLYGON );
//
// layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POLYGONS );
// layers.add( GeometryExampleData.GML_31_POLYGON_2 );
//
// layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTILINESTRINGS );
// layers.add( GeometryExampleData.GML_31_POLYGON_2 );
//
// layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTIPOINTS );
// layers.add( GeometryExampleData.GML_31_POLYGON );
//
// layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTILINESTRINGS );
// layers.add( GeometryExampleData.GML_31_POLYGON_2 );
//
// layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTIPOLYGONS );
// layers.add( GeometryExampleData.GML_31_POLYGON_2 );
//
// } else
// // symdifference algorithm
// if ( alg.getCommandLineName().equals( "symdifference" ) ) {
// layers.addAll( GeometryExampleData.getData( GeometryType.POLYGON ) );
// } else
// // countpoints algorithm
// if ( alg.getCommandLineName().equals( "countpoints" ) ) {
//
// layers.add( GeometryExampleData.GML_31_POINT );
// layers.add( GeometryExampleData.GML_31_POLYGON );
//
// // ToDo why the algorithm returns a IVectorLayer and not a ITable?
//
// } else
// // vectormean, cleanpointslayer, polylinestosinglesegments, splitpolylinesatnodes algorithm
// if ( alg.getCommandLineName().equals( "vectormean" ) || alg.getCommandLineName().equals( "cleanpointslayer" )
// || alg.getCommandLineName().equals( "polylinestosinglesegments" )
// || alg.getCommandLineName().equals( "splitpolylinesatnodes" )
// || alg.getCommandLineName().equals( "geometricpropertieslines" ) ) {
//
// // tested cleanpointslayer
// layers.addAll( GeometryExampleData.getData( GeometryType.LINE ) );
//
// } else
//
// // delaunay algorithm
// if ( alg.getCommandLineName().equals( "delaunay" ) ) {
// layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTIPOINTS );
// layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POINTS );
// } else
//
// // removerepeatedgeometries algorithm
// if ( alg.getCommandLineName().equals( "removerepeatedgeometries" ) ) {
// layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTIPOLYGONS );
//
// } else
// // splitmultipart algorithm
// if ( alg.getCommandLineName().equals( "splitmultipart" ) ) {
// layers.add( GeometryExampleData.GML_31_MULTILPOLYGON );
//
// } else
// // all algorithms
// layers.addAll( GeometryExampleData.getAllData() );
//
// return layers;
// }
//
// private LinkedList<ExampleData> getNumericalValuesInput( GeoAlgorithm alg ) {
// LinkedList<ExampleData> numbers = new LinkedList<ExampleData>();
//
// numbers.add( LiteralExampleData.NUMERICAL_VALUE_1 );
// numbers.add( LiteralExampleData.NUMERICAL_VALUE_2 );
// numbers.add( LiteralExampleData.NUMERICAL_VALUE_3 );
// numbers.add( LiteralExampleData.NUMERICAL_VALUE_4 );
// numbers.add( LiteralExampleData.NUMERICAL_VALUE_1 );
// numbers.add( LiteralExampleData.NUMERICAL_VALUE_2 );
//
// return numbers;
// }

