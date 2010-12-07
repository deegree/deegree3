package org.deegree.services.wps.provider.sextante;

import java.util.LinkedList;
import java.util.List;

import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.output.ComplexOutput;
import org.deegree.services.wps.provider.sextante.GMLSchema.GMLType;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.Parameter;

public class SextanteFeatureCollectionStreamer {

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

    public boolean containFeatureCollectionInput() {
        return containFeatureCollectionInput;
    }

    public boolean containFeatureCollectionOutput() {
        return containFeatureCollectionOutput;
    }

    public void execute() {

    }
}
