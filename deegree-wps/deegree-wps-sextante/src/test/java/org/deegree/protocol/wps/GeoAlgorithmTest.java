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
import org.deegree.protocol.wps.GeometryExampleData.GeometryType;
import org.deegree.protocol.wps.client.WPSClient;
import org.deegree.protocol.wps.client.output.ExecutionOutput;
import org.deegree.protocol.wps.client.process.ProcessExecution;
import org.deegree.protocol.wps.client.process.Process;
import org.deegree.protocol.wps.client.process.execute.ExecutionOutputs;
import org.deegree.services.wps.provider.sextante.SextanteProcessProvider;
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
     * Returns a list of all supported {@link GeoAlgorithm} as {@link GeoAlgorithmWithData} for testing.
     * 
     * @return List of all supported {@link GeoAlgorithm} as {@link GeoAlgorithmWithData}.
     */
    @SuppressWarnings("unchecked")
    private LinkedList<GeoAlgorithmWithData> getAlgorithms() {
        LinkedList<GeoAlgorithmWithData> algs = new LinkedList<GeoAlgorithmWithData>();

        // test all algorithms?
        boolean testAll = false;

        if ( !testAll ) {
            // test only one algorithm
            Sextante.initialize();
            HashMap<String, GeoAlgorithm> sextanteAlgs = Sextante.getAlgorithms();
            GeoAlgorithm geoAlg = sextanteAlgs.get( "centroids" );
            GeoAlgorithmWithData testAlg = new GeoAlgorithmWithData( geoAlg );
            testAlg.addAllInputData( getInputData( geoAlg ) );
            algs.add( testAlg );

        } else {

            // test all supported algorithms
            GeoAlgorithm[] geoalgs = SextanteProcessProvider.getVectorLayerAlgorithms();
            for ( int i = 0; i < geoalgs.length; i++ ) {
                GeoAlgorithm geoAlg = geoalgs[i];

                if ( !geoAlg.getCommandLineName().equals( "no algorithm" ) ) {
                    GeoAlgorithmWithData testAlg = new GeoAlgorithmWithData( geoAlg );
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
     * @return A list of a list of test data. If the algorithm need only one input parameter, returns only one list of
     *         test data in the list. If the algorithm need more than one input parameter, returns for every input
     *         parameter a list of test data in the list.
     */
    private LinkedList<LinkedList<ExampleData>> getInputData( GeoAlgorithm alg ) {

        // all input data
        LinkedList<LinkedList<ExampleData>> allData = new LinkedList<LinkedList<ExampleData>>();

        // example data
        LinkedList<ExampleData> vectorLayers = getVectorLayerInput( alg );
        LinkedList<ExampleData> nummericalValues = getVectorLayerInput( alg );

        // example data iterators
        Iterator<ExampleData> vectorLayersIterator = vectorLayers.iterator();
        Iterator<ExampleData> numericalValuesIterator = nummericalValues.iterator();

        // traverse input parameter of one execution
        ParametersSet paramSet = alg.getParameters();

        // traverse all example data
        boolean addMoreData = true;
        while ( addMoreData ) {

            // input data of one execution
            LinkedList<ExampleData> dataList = new LinkedList<ExampleData>();

            for ( int j = 0; j < paramSet.getNumberOfParameters(); j++ ) {
                Parameter param = paramSet.getParameter( j );

                // vector layers
                if ( param.getParameterTypeName().equals( SextanteWPSProcess.VECTOR_LAYER_INPUT ) )

                    if ( vectorLayersIterator.hasNext() ) {
                        dataList.add( vectorLayersIterator.next() );
                    } else {
                        addMoreData = false;
                        break;
                    }
                else

                // numerical values
                if ( param.getParameterTypeName().equals( SextanteWPSProcess.NUMERICAL_VALUE_INPUT ) ) {

                    if ( numericalValuesIterator.hasNext() ) {
                        dataList.add( numericalValuesIterator.next() );
                    } else {
                        addMoreData = false;
                        break;
                    }
                }

            }
            allData.add( dataList );
        }

        return allData;
    }

    /**
     * Returns a list of all test data for a vector layer of a SEXTANTE {@link GeoAlgorithm}.
     * 
     * @param alg
     *            SEXTANTE {@link GeoAlgorithm}.
     * 
     * @return List of all test data for a layer of a SEXTANTE {@link GeoAlgorithm}.
     */
    private LinkedList<ExampleData> getVectorLayerInput( GeoAlgorithm alg ) {
        LinkedList<ExampleData> layers = new LinkedList<ExampleData>();

        // cleanpointslayer algorithm
        if ( alg.getCommandLineName().equals( "cleanpointslayer" ) )
            layers.addAll( GeometryExampleData.getData( GeometryType.POINT ) );
        else
        // polylinestopolygons algorithm
        if ( alg.getCommandLineName().equals( "polylinestopolygons" ) )
            layers.addAll( GeometryExampleData.getData( GeometryType.LINE ) );
        else
        // pointcoordinates algorithm
        if ( alg.getCommandLineName().equals( "pointcoordinates" ) )
            layers.addAll( GeometryExampleData.getData( GeometryType.POINT ) );
        else
        // removeholes algorithm
        if ( alg.getCommandLineName().equals( "removeholes" ) )
            layers.addAll( GeometryExampleData.getData( GeometryType.POLYGON ) );
        else
        // exportvector algorithm
        if ( alg.getCommandLineName().equals( "exportvector" ) )
            // TODO
            LOG.warn( "No test data for '" + alg.getCommandLineName() + "' available." );
        else
        // polygonize algorithm
        if ( alg.getCommandLineName().equals( "polygonize" ) ) {
            layers.add( GeometryExampleData.GML_31_MULTILINESTRING );
            layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTILINESTRINGS );
        } else
        // difference and intersection algorithm
        if ( alg.getCommandLineName().equals( "difference" ) || alg.getCommandLineName().equals( "intersection" ) ) {
            layers.addAll( GeometryExampleData.getData( GeometryType.POLYGON ) );
            layers.addAll( GeometryExampleData.getData( GeometryType.LINE ) );
            layers.addAll( GeometryExampleData.getData( GeometryType.POINT ) );
        } else
        // union algorithm
        if ( alg.getCommandLineName().equals( "union" ) ) {

            // TODO
            layers.add( GeometryExampleData.GML_31_POLYGON );
            layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTIPOLYGONS );

            LOG.warn( "No test data for '" + alg.getCommandLineName() + "' available." );
        } else
        // clip algorithm
        if ( alg.getCommandLineName().equals( "clip" ) ) {
            layers.add( GeometryExampleData.GML_31_POINT );
            layers.add( GeometryExampleData.GML_31_MULTILPOLYGON );

            layers.add( GeometryExampleData.GML_31_LINESTRING );
            layers.add( GeometryExampleData.GML_31_MULTILINESTRING );

            layers.add( GeometryExampleData.GML_31_MULTILPOLYGON );
            layers.add( GeometryExampleData.GML_31_LINESTRING );

            layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTIPOINTS );
            layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POINTS );

            layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_LINESTRINGS );
            layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTILINESTRINGS );

            layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTIPOLYGONS );
            layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POLYGONS );
        } else
        // symdifference algorithm
        if ( alg.getCommandLineName().equals( "symdifference" ) ) {
            layers.addAll( GeometryExampleData.getData( GeometryType.POLYGON ) );
        } else
        // countpoints algorithm
        if ( alg.getCommandLineName().equals( "countpoints" ) ) {

            layers.add( GeometryExampleData.GML_31_POINT );
            layers.add( GeometryExampleData.GML_31_POLYGON );

            // TODO why the algorithm returns a IVectorLayer and not a ITable?

        } else
        // vectormean, changelinedirection, polylinestosinglesegments, splitpolylinesatnodes algorithm
        if ( alg.getCommandLineName().equals( "vectormean" ) || alg.getCommandLineName().equals( "changelinedirection" )
             || alg.getCommandLineName().equals( "polylinestosinglesegments" )
             || alg.getCommandLineName().equals( "splitpolylinesatnodes" )
             || alg.getCommandLineName().equals( "geometricpropertieslines" ) ) {

            // tested changelinedirection
            layers.addAll( GeometryExampleData.getData( GeometryType.LINE ) );

        } else

        // delaunay algorithm
        if ( alg.getCommandLineName().equals( "delaunay" ) ) {
            layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTIPOINTS );
            layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_POINTS );
        } else

        // removerepeatedgeometries algorithm
        if ( alg.getCommandLineName().equals( "removerepeatedgeometries" ) ) {
            layers.add( GeometryExampleData.GML_31_FEATURE_COLLECTION_MULTIPOLYGONS );

        } else

            // all algorithms
            layers.addAll( GeometryExampleData.getAllData() );

        return layers;
    }

    private LinkedList<ExampleData> getNumericalValuesInput( GeoAlgorithm alg ) {
        LinkedList<ExampleData> numbers = new LinkedList<ExampleData>();

        numbers.add( LiteralExampleData.NUMERICAL_VALUE_1 );
        numbers.add( LiteralExampleData.NUMERICAL_VALUE_2 );
        numbers.add( LiteralExampleData.NUMERICAL_VALUE_3 );
        numbers.add( LiteralExampleData.NUMERICAL_VALUE_4 );
        numbers.add( LiteralExampleData.NUMERICAL_VALUE_1 );
        numbers.add( LiteralExampleData.NUMERICAL_VALUE_2 );

        return numbers;
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
                LinkedList<GeoAlgorithmWithData> algs = getAlgorithms();

                // traverse all algorithms
                for ( GeoAlgorithmWithData testAlg : algs ) {

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
                                              + currentData.toString() );

                                    // Geometries
                                    if ( currentData instanceof GeometryExampleData ) {
                                        GeometryExampleData currentGeometryData = (GeometryExampleData) currentData;

                                        execution.addXMLInput( inputIndentifier, null, currentGeometryData.getURL(),
                                                               false, currentGeometryData.getMimeType(),
                                                               currentGeometryData.getEncoding(),
                                                               currentGeometryData.getSchemaURL() );
                                    } else {

                                        // Literals
                                        if ( currentData instanceof LiteralExampleData ) {
                                            LiteralExampleData currentLiteralData = (LiteralExampleData) currentData;

                                            execution.addLiteralInput( currentLiteralData.getId(),
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
