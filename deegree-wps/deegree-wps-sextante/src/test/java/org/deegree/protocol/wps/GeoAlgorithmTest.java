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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.deegree.protocol.wps.ExampleData.GeometryType;
import org.deegree.protocol.wps.client.WPSClient;
import org.deegree.protocol.wps.client.output.ExecutionOutput;
import org.deegree.protocol.wps.client.process.ProcessExecution;
import org.deegree.protocol.wps.client.process.Process;
import org.deegree.protocol.wps.client.process.execute.ExecutionOutputs;
import org.deegree.services.wps.provider.sextante.SextanteProcessProvider;
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

    /**
     * Returns a list of all supported {@link GeoAlgorithm} as {@link TestAlgorithm} for testing.
     * 
     * @return List of all supported {@link GeoAlgorithm} as {@link TestAlgorithm}.
     */
    @SuppressWarnings("unchecked")
    private LinkedList<TestAlgorithm> getAlgorithms() {
        LinkedList<TestAlgorithm> algs = new LinkedList<TestAlgorithm>();

        // test all algorithms?
        boolean testAll = true;

        if ( !testAll ) {
            // test only one algorithm
            Sextante.initialize();
            HashMap<String, GeoAlgorithm> sextanteAlgs = Sextante.getAlgorithms();
            GeoAlgorithm geoAlg = sextanteAlgs.get( "countpoints" );
            TestAlgorithm testAlg = new TestAlgorithm( geoAlg );
            testAlg.addAllInputData( getInputData( geoAlg ) );
            algs.add( testAlg );

        } else {

            // test all supported algorithms
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

    /**
     * This method determine test data for a SEXTANTE {@link GeoAlgorithm}.
     * 
     * @param alg
     *            SEXTANTE {@link GeoAlgorithm}.
     * @return - A list of a list of test data. If the algorithm need only one input parameter, returns only one list of
     *         test data in the list. If the algorithm need more than one input parameter, returns for every input
     *         parameter a list of test data in the list.
     */
    private LinkedList<LinkedList<ExampleData>> getInputData( GeoAlgorithm alg ) {

        // all input data
        LinkedList<LinkedList<ExampleData>> allData = new LinkedList<LinkedList<ExampleData>>();

        // example data in geometry types
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

    /**
     * Returns a list of all test data for a layer of a SEXTANTE {@link GeoAlgorithm}.
     * 
     * @param alg
     *            - SEXTANTE {@link GeoAlgorithm}.
     * 
     * @return List of all test data for a layer of a SEXTANTE {@link GeoAlgorithm}.
     */
    private LinkedList<ExampleData> getLayerInput( GeoAlgorithm alg ) {
        LinkedList<ExampleData> layers = new LinkedList<ExampleData>();

        // cleanpointslayer algorithm
        if ( alg.getCommandLineName().equals( "cleanpointslayer" ) )
            layers.addAll( ExampleData.getData( GeometryType.POINT ) );
        else
        // polylinestopolygons algorithm
        if ( alg.getCommandLineName().equals( "polylinestopolygons" ) )
            layers.addAll( ExampleData.getData( GeometryType.LINE ) );
        else
        // pointcoordinates algorithm
        if ( alg.getCommandLineName().equals( "pointcoordinates" ) )
            layers.addAll( ExampleData.getData( GeometryType.POINT ) );
        else
        // removeholes algorithm
        if ( alg.getCommandLineName().equals( "removeholes" ) )
            layers.addAll( ExampleData.getData( GeometryType.POLYGON ) );
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
            layers.addAll( ExampleData.getData( GeometryType.POLYGON ) );
            layers.addAll( ExampleData.getData( GeometryType.LINE ) );
            layers.addAll( ExampleData.getData( GeometryType.POINT ) );
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
            layers.addAll( ExampleData.getData( GeometryType.POLYGON ) );
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

    /**
     * Returns a list of all test data for a polygon layer of a SEXTANTE {@link GeoAlgorithm}.
     * 
     * @param alg
     *            - SEXTANTE {@link GeoAlgorithm}.
     * 
     * @return List of all test data for a polygon layer of a SEXTANTE {@link GeoAlgorithm}.
     */
    private LinkedList<ExampleData> getPolygonsInput() {
        LinkedList<ExampleData> polygons = new LinkedList<ExampleData>();
        polygons.addAll( ExampleData.getData( GeometryType.POLYGON ) );
        return polygons;
    }

    /**
     * Returns a list of all test data for a lines layer of a SEXTANTE {@link GeoAlgorithm}.
     * 
     * @param alg
     *            - SEXTANTE {@link GeoAlgorithm}.
     * 
     * @return List of all test data for a lines layer of a SEXTANTE {@link GeoAlgorithm}.
     */
    private LinkedList<ExampleData> getLinesInput() {
        LinkedList<ExampleData> lines = new LinkedList<ExampleData>();
        lines.addAll( ExampleData.getData( GeometryType.LINE ) );
        lines.addAll( ExampleData.getData( GeometryType.POLYGON ) );
        return lines;
    }

    /**
     * Returns a list of all test data for a point layer of a SEXTANTE {@link GeoAlgorithm}.
     * 
     * @param alg
     *            - SEXTANTE {@link GeoAlgorithm}.
     * 
     * @return List of all test data for a point layer of a SEXTANTE {@link GeoAlgorithm}.
     */
    private LinkedList<ExampleData> getPointsInput() {
        LinkedList<ExampleData> points = new LinkedList<ExampleData>();
        points.add( ExampleData.GML_31_FEATURE_COLLECTION_POINTS );
        return points;
    }

    /**
     * This method tests all supported SEXTANTE {@link GeoAlgorithm} of the deegree WPS. <br>
     * It will tested only whether the algorithm runs without errors. <br>
     * It is not tested whether the algorithm calculated his output values correctly.
     */
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
                for ( TestAlgorithm testAlg : algs ) {

                    // geoalgorithm
                    GeoAlgorithm alg = testAlg.getAlgorithm();

                    // input data
                    LinkedList<LinkedList<ExampleData>> allData = testAlg.getAllInputData();
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
                                                           currentData.getSchemaURL() );
                                }

                                // set all output parameters
                                OutputObjectsSet outputSet = alg.getOutputObjects();
                                for ( int j = 0; j < outputSet.getOutputObjectsCount(); j++ ) {
                                    Output outp = outputSet.getOutput( j );

                                    String outputIdentifier = outp.getName();

                                    execution.addOutput( outputIdentifier, null, null, false, "text/xml", "UTF-8",
                                                         "http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd" );
                                }

                                // execute algorithm
                                ExecutionOutputs outputs = execution.execute();
                                ExecutionOutput[] allOutputs = outputs.getAll();

                                // check number of output output objects
                                Assert.assertTrue( allOutputs.length > 0 );

                            } else {
                                String msg = "Wrong number of input data.";
                                LOG.error( msg );
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
}
