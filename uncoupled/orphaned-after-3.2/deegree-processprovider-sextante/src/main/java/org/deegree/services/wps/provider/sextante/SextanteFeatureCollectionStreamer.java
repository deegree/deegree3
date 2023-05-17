/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.services.wps.provider.sextante;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.feature.StreamFeatureCollection;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.output.ComplexOutput;
import org.deegree.services.wps.provider.sextante.GMLSchema.GMLType;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.WrongParameterTypeException;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.Parameter;

/**
 * This class allows to steam a {@link FeatureCollection} for execute a process.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * 
 */
public class SextanteFeatureCollectionStreamer {

    public static final boolean ENABLED = false;

    // algorithm and input data
    // ---------------------------------------------------------------------------------------------------
    private final GeoAlgorithm alg;

    private final ProcessletInputs in;

    private final ProcessletOutputs out;

    // indexes of input and output parameters
    // ---------------------------------------------------------------------------------------------------
    private List<Integer> vectorLayerIndexesInput = new LinkedList<Integer>();

    private List<Integer> vectorLayerIndexesOutput = new LinkedList<Integer>();

    private List<Integer> featureCollectionIndexesInput = new LinkedList<Integer>();

    private List<Integer> featureCollectionIndexesOutput = new LinkedList<Integer>();

    private List<Integer> paramIndexesWithoutFeatureCollectionInput = new LinkedList<Integer>();

    private List<Integer> paramIndexesWithoutFeatureCollectionOutput = new LinkedList<Integer>();

    private boolean containFeatureCollectionInput;

    private boolean containFeatureCollectionOutput;

    public SextanteFeatureCollectionStreamer( GeoAlgorithm alg, ProcessletInputs in, ProcessletOutputs out ) {

        // notice algorithm and input/output data
        this.alg = alg;
        this.in = in;
        this.out = out;

        // determine indexes of vector layer input parameters
        ParametersSet params = alg.getParameters();
        for ( int i = 0; i < params.getNumberOfParameters(); i++ ) {
            Parameter param = params.getParameter( i );
            if ( param.getParameterTypeName().equals( SextanteWPSProcess.VECTOR_LAYER_INPUT ) )
                vectorLayerIndexesInput.add( i );
            else
                paramIndexesWithoutFeatureCollectionInput.add( i );
        }

        // determine indexes of feature collection inputs
        for ( Integer i : vectorLayerIndexesInput ) {
            // parameter
            Parameter param = params.getParameter( i );

            // determine GMLType of input parameter
            ComplexInput gmlInput = (ComplexInput) in.getParameter( param.getParameterName() );
            GMLType gmlType = FormatHelper.determineGMLType( gmlInput );

            // notice feature collection parameter
            if ( gmlType.equals( GMLType.FEATURE_COLLECTION ) ) {
                featureCollectionIndexesInput.add( i );
                containFeatureCollectionInput = true;
            } else {
                paramIndexesWithoutFeatureCollectionInput.add( i );
            }
        }

        // determine indexes of vector layer output parameters
        OutputObjectsSet outputs = alg.getOutputObjects();
        for ( int i = 0; i < outputs.getOutputObjectsCount(); i++ ) {
            Output output = outputs.getOutput( i );

            if ( output.getTypeDescription().equals( SextanteWPSProcess.VECTOR_LAYER_OUTPUT ) ) {
                vectorLayerIndexesOutput.add( i );
            } else {
                paramIndexesWithoutFeatureCollectionOutput.add( i );
            }
        }

        // determine indexes of feature collection outputs
        for ( Integer i : vectorLayerIndexesOutput ) {
            Output output = outputs.getOutput( i );

            ComplexOutput gmlOutput = (ComplexOutput) out.getParameter( output.getName() );
            GMLType gmlType = FormatHelper.determineGMLType( gmlOutput );

            if ( gmlType.equals( GMLType.FEATURE_COLLECTION ) ) {
                featureCollectionIndexesOutput.add( i );
                containFeatureCollectionOutput = true;
            }
        }

    }

    /**
     * Returns, if the input data contains a {@link FeatureCollection}.
     * 
     * @return true = {@link FeatureCollection}, false = no {@link FeatureCollection}.
     */
    public boolean containFeatureCollectionInput() {
        return containFeatureCollectionInput;
    }

    /**
     * Returns, if the output data contains a {@link FeatureCollection}.
     * 
     * @return true = {@link FeatureCollection}, false = no {@link FeatureCollection}.
     */
    public boolean containFeatureCollectionOutput() {
        return containFeatureCollectionOutput;
    }

    /**
     * Streams input and output {@link FeatureCollection} and execute the process.
     * 
     * @throws ProcessletException
     * @throws ClassNotFoundException
     * @throws NullParameterValueException
     * @throws WrongParameterTypeException
     * 
     */
    public void execute()
                            throws ProcessletException, WrongParameterTypeException, NullParameterValueException,
                            ClassNotFoundException {

        // input parameters
        // --------------------------------------------------------------------------------------------------------------------------------------------

        // set input parameter without feature collections
        SextanteProcesslet.setInputValues( alg, in, paramIndexesWithoutFeatureCollectionInput );

        List<SextanteFeatureCollectionStreamReader> featureCollectionsForAExecute = new LinkedList<SextanteFeatureCollectionStreamReader>();
        Map<Integer, SextanteFeatureCollectionStreamWriter> featuresForWrite = new HashMap<Integer, SextanteFeatureCollectionStreamWriter>();

        try {

            // determine all feature collections for one execute
            for ( Integer i : featureCollectionIndexesInput ) {

                // VectorLayer for FeatureCollection
                Parameter param = alg.getParameters().getParameter( i );

                // determine GMLType of input parameter
                ComplexInput gmlInput = (ComplexInput) in.getParameter( param.getParameterName() );

                // create feature collection input stream
                XMLStreamReader xmlReader = gmlInput.getValueAsXMLStream();
                GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( FormatHelper.determineGMLVersion( gmlInput ),
                                                                                   xmlReader );
                StreamFeatureCollection sfc = gmlReader.readFeatureCollectionStream();

                featureCollectionsForAExecute.add( new SextanteFeatureCollectionStreamReader( param, sfc ) );
            }

            // execute and output parameters
            // --------------------------------------------------------------------------------------------------------------------------------------------
            boolean first = true;
            do {
                // set feature for one execute
                for ( SextanteFeatureCollectionStreamReader fcsc : featureCollectionsForAExecute ) {
                    Parameter param = fcsc.getParameter();
                    IVectorLayer layer = fcsc.getNextFeatureAsVectorLayer();
                    param.setParameterValue( layer );
                }

                alg.execute( null, new OutputFactoryExt() );

                // write all results without feature collections
                SextanteProcesslet.writeResult( alg, out, paramIndexesWithoutFeatureCollectionOutput );

                // notice output parameters with their streams
                OutputObjectsSet outputs = alg.getOutputObjects();

                if ( first ) {
                    first = false;

                    for ( Integer i : featureCollectionIndexesOutput ) {
                        String identifier = alg.getOutputObjects().getOutput( i ).getName();
                        featuresForWrite.put( i, new SextanteFeatureCollectionStreamWriter( identifier, out ) );
                    }

                }

                // write features
                for ( Integer i : featureCollectionIndexesOutput ) {
                    // result feature
                    Output output = outputs.getOutput( i );
                    IVectorLayer layer = (IVectorLayer) output.getOutputObject();
                    Feature f = VectorLayerAdapter.createFeature( layer );

                    // write feature
                    SextanteFeatureCollectionStreamWriter con = featuresForWrite.get( i );
                    con.writeFeature( f );
                }

            } while ( SextanteFeatureCollectionStreamReader.containOneOfAllReadersFeatures() );

        } catch ( Exception e ) {
            e.printStackTrace();
            throw new ProcessletException( "SEXTANTE FeatureCollection-Streaming failed!" );
        } finally {
            try {
                SextanteFeatureCollectionStreamReader.closeAll();

                // close feature collections
                Set<Integer> keys = featuresForWrite.keySet();
                for ( Integer key : keys ) {
                    featuresForWrite.get( key ).close();
                }

            } catch ( IOException e ) {
                e.printStackTrace();
                throw new ProcessletException( "SEXTANTE FeatureCollection-Streaming failed!" );
            } catch ( XMLStreamException e ) {
                e.printStackTrace();
                throw new ProcessletException( "SEXTANTE FeatureCollection-Streaming failed!" );
            }
        }
    }
}
