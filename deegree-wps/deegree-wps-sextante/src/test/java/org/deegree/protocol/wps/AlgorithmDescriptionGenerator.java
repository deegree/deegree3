//$HeadURL: http://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.protocol.wps;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import org.deegree.services.wps.provider.sextante.SextanteWPSProcess;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.IGeoAlgorithmFilter;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.Parameter;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class AlgorithmDescriptionGenerator {

    /*
     * If you use this filter, it will returned algorithms, that need only vector layers for input and ouput parameters.
     */
    private static IGeoAlgorithmFilter vectorFilter = new IGeoAlgorithmFilter() {

        @Override
        public boolean accept( GeoAlgorithm alg ) {

            boolean answer = true;

            // allowed input parameters
            ParametersSet paramSet = alg.getParameters();
            for ( int i = 0; i < paramSet.getNumberOfParameters(); i++ ) {
                Parameter param = paramSet.getParameter( i );
                if ( !param.getParameterTypeName().equals( SextanteWPSProcess.VECTOR_LAYER_INPUT ) ) {
                    answer = false;
                    break;
                }
            }

            // allowed output parameters
            OutputObjectsSet outputSet = alg.getOutputObjects();
            if ( outputSet.getOutputObjectsCount() > 0 )
                for ( int i = 0; i < outputSet.getOutputObjectsCount(); i++ ) {
                    Output output = outputSet.getOutput( i );
                    if ( !output.getTypeDescription().equals( SextanteWPSProcess.VECTOR_LAYER_OUTPUT ) ) {
                        answer = false;
                        break;
                    }
                }
            else
                answer = false;

            return answer;
        }
    };

    /*
     * If you use this filter, it will returned algorithms, that need only vector layers and numerical values for input
     * parameters and only a vector layer for ouput parameters.
     */
    private static IGeoAlgorithmFilter vectorNumericalFilter = new IGeoAlgorithmFilter() {

        @Override
        public boolean accept( GeoAlgorithm alg ) {

            boolean answer = false;
            boolean numerical = false;
            boolean vectorlayer = false;
            boolean others = false;

            // allowed input parameters
            ParametersSet paramSet = alg.getParameters();
            for ( int i = 0; i < paramSet.getNumberOfParameters(); i++ ) {
                Parameter param = paramSet.getParameter( i );

                if ( param.getParameterTypeName().equals( SextanteWPSProcess.VECTOR_LAYER_INPUT ) )
                    vectorlayer = true;
                else if ( param.getParameterTypeName().equals( SextanteWPSProcess.NUMERICAL_VALUE_INPUT ) )
                    numerical = true;
                else
                    others = true;
            }
            if ( !others && vectorlayer && numerical )
                answer = true;

            // allowed output parameters
            OutputObjectsSet outputSet = alg.getOutputObjects();

            if ( outputSet.getOutputObjectsCount() > 0 )
                for ( int i = 0; i < outputSet.getOutputObjectsCount(); i++ ) {
                    Output output = outputSet.getOutput( i );
                    if ( !output.getTypeDescription().equals( SextanteWPSProcess.VECTOR_LAYER_OUTPUT ) ) {
                        answer = false;
                        break;
                    }
                }
            else
                answer = false;

            return answer;
        }
    };

    private static IGeoAlgorithmFilter vectorOthersFilter = new IGeoAlgorithmFilter() {

        @Override
        public boolean accept( GeoAlgorithm alg ) {

            boolean answer = false;
            boolean nummericalValue = false;
            boolean vectorlayer = false;
            boolean others = false;

            boolean table = false;
            boolean tableField = false;
            boolean rasterLayer = false;
            boolean multipleInput = false;
            boolean fixedTable = false;
            boolean band = false;

            // allowed input parameters
            ParametersSet paramSet = alg.getParameters();
            for ( int i = 0; i < paramSet.getNumberOfParameters(); i++ ) {
                Parameter param = paramSet.getParameter( i );

                if ( param.getParameterTypeName().equals( SextanteWPSProcess.VECTOR_LAYER_INPUT ) )
                    vectorlayer = true;
                else if ( param.getParameterTypeName().equals( SextanteWPSProcess.NUMERICAL_VALUE_INPUT ) )
                    nummericalValue = true;
                else if ( param.getParameterTypeName().equals( SextanteWPSProcess.TABLE_FIELD_INPUT ) )
                    tableField = true;
                else if ( param.getParameterTypeName().equals( SextanteWPSProcess.TABLE_INPUT ) )
                    table = true;
                else if ( param.getParameterTypeName().equals( SextanteWPSProcess.RASTER_LAYER_INPUT ) )
                    rasterLayer = true;
                else if ( param.getParameterTypeName().equals( SextanteWPSProcess.MULTIPLE_INPUT_INPUT ) )
                    multipleInput = true;
                else if ( param.getParameterTypeName().equals( SextanteWPSProcess.FIXED_TABLE_INPUT ) )
                    fixedTable = true;
                else if ( param.getParameterTypeName().equals( SextanteWPSProcess.BAND_INPUT ) )
                    band = true;
                else
                    others = true;

            }

            // allow algorithm
            if ( others && vectorlayer && nummericalValue )
                answer = true;
            else if ( others && vectorlayer && !nummericalValue )
                answer = true;

            // ban algorithm
            if ( table || tableField || rasterLayer || multipleInput || fixedTable || band )
                answer = false;

            // allowed output parameters
            OutputObjectsSet outputSet = alg.getOutputObjects();
            if ( outputSet.getOutputObjectsCount() == 1 )
                for ( int i = 0; i < outputSet.getOutputObjectsCount(); i++ ) {
                    Output output = outputSet.getOutput( i );
                    if ( !output.getTypeDescription().equals( SextanteWPSProcess.VECTOR_LAYER_OUTPUT ) ) {
                        answer = false;
                        break;
                    }
                }
            else
                answer = false;

            return answer;
        }
    };

    private static IGeoAlgorithmFilter allOthersFilter = new IGeoAlgorithmFilter() {

        @Override
        public boolean accept( GeoAlgorithm alg ) {

            HashMap<String, GeoAlgorithm> allAlgs = new HashMap<String, GeoAlgorithm>();
            allAlgs.putAll( Sextante.getAlgorithms() );

            // delete false algorithms
            LinkedList<String> keys = new LinkedList<String>();
            HashMap<String, GeoAlgorithm> vectorAlgs = Sextante.getAlgorithms( vectorFilter );
            keys.addAll( vectorAlgs.keySet() );
            HashMap<String, GeoAlgorithm> vectorNumericalAlgs = Sextante.getAlgorithms( vectorNumericalFilter );
            keys.addAll( vectorNumericalAlgs.keySet() );
            HashMap<String, GeoAlgorithm> vectorOthersAlgs = Sextante.getAlgorithms( vectorOthersFilter );
            keys.addAll( vectorOthersAlgs.keySet() );
            for ( String key : keys ) {
                allAlgs.remove( key );
            }

            if ( allAlgs.get( alg.getCommandLineName() ) != null )
                return true;
            else
                return false;
        }

    };

    private static boolean[] getSupportedInputTypesAsArray( GeoAlgorithm alg ) {
        boolean[] inputTypes = new boolean[13];

        ParametersSet paramSet = alg.getParameters();
        for ( int i = 0; i < paramSet.getNumberOfParameters(); i++ ) {
            Parameter param = paramSet.getParameter( i );

            // set the correct input parameter value
            String paramTypeName = param.getParameterTypeName();
            if ( paramTypeName.equals( SextanteWPSProcess.VECTOR_LAYER_INPUT ) )
                inputTypes[0] = true;
            else if ( paramTypeName.equals( SextanteWPSProcess.NUMERICAL_VALUE_INPUT ) )
                inputTypes[7] = true;
            else if ( paramTypeName.equals( SextanteWPSProcess.SELECTION_INPUT ) )
                inputTypes[4] = true;
            else if ( paramTypeName.equals( SextanteWPSProcess.FILEPATH_INPUT ) )
                inputTypes[10] = true;
            else if ( paramTypeName.equals( SextanteWPSProcess.BOOLEAN_INPUT ) )
                inputTypes[11] = true;
            else if ( paramTypeName.equals( SextanteWPSProcess.STRING_INPUT ) )
                inputTypes[3] = true;
            else if ( paramTypeName.equals( SextanteWPSProcess.MULTIPLE_INPUT_INPUT ) )
                inputTypes[8] = true;
            else if ( paramTypeName.equals( SextanteWPSProcess.RASTER_LAYER_INPUT ) )
                inputTypes[5] = true;
            else if ( paramTypeName.equals( SextanteWPSProcess.TABLE_FIELD_INPUT ) )
                inputTypes[1] = true;
            else if ( paramTypeName.equals( SextanteWPSProcess.POINT_INPUT ) )
                inputTypes[6] = true;
            else if ( paramTypeName.equals( SextanteWPSProcess.BAND_INPUT ) )
                inputTypes[12] = true;
            else if ( paramTypeName.equals( SextanteWPSProcess.TABLE_INPUT ) )
                inputTypes[2] = true;
            else if ( paramTypeName.equals( SextanteWPSProcess.FIXED_TABLE_INPUT ) )
                inputTypes[9] = true;
            else {
                System.err.println( "\nUNKNOWN INPUT PARAMETER: '" + paramTypeName + "'\n" );
            }

        }

        return inputTypes;
    }

    private static boolean[] getSupportedOutputTypesAsArray( GeoAlgorithm alg ) {
        boolean[] outputTypes = new boolean[5];

        OutputObjectsSet outputSet = alg.getOutputObjects();
        for ( int i = 0; i < outputSet.getOutputObjectsCount(); i++ ) {
            Output output = outputSet.getOutput( i );

            // write the correct output type
            String paramTypeDesc = output.getTypeDescription();

            if ( paramTypeDesc.equals( SextanteWPSProcess.VECTOR_LAYER_OUTPUT ) )
                outputTypes[0] = true;
            else if ( paramTypeDesc.equals( SextanteWPSProcess.RASTER_LAYER_OUTPUT ) )
                outputTypes[3] = true;
            else if ( paramTypeDesc.equals( SextanteWPSProcess.TABLE_OUTPUT ) )
                outputTypes[2] = true;
            else if ( paramTypeDesc.equals( SextanteWPSProcess.TEXT_OUTPUT ) )
                outputTypes[1] = true;
            else if ( paramTypeDesc.equals( SextanteWPSProcess.CHART_OUTPUT ) )
                outputTypes[4] = true;
            else {
                System.err.println( "\nUNKNOWN OUTPUT PARAMETER: '" + paramTypeDesc + "'\n" );
            }
        }

        return outputTypes;
    }

    private static void printAlgorithms( IGeoAlgorithmFilter filter ) {

        System.out.println( "ALGORITHMS" );

        HashMap<String, String> inputs = new HashMap<String, String>();
        HashMap<String, String> outputs = new HashMap<String, String>();

        HashMap<String, GeoAlgorithm> algs = Sextante.getAlgorithms( filter );
        Set<String> keySet = algs.keySet();
        for ( String key : keySet ) {

            // algorithm
            GeoAlgorithm alg = algs.get( key );

            String commandlineHelp = "";

            // get help description
            boolean error = false;
            try {
                commandlineHelp += alg.getCommandLineHelp();

            } catch ( Exception e ) {
                commandlineHelp += "No information. Error in SEXTANTE library.";
                error = true;
            }

            // format help description
            String help;
            if ( !error ) {

                String[] helpArrayTemp = commandlineHelp.replace( "\n", "" ).replace( "[", "~" ).replace( "]", "~" ).split(
                                                                                                                            "~" );
                String helpTemp = "";
                for ( int i = 0; i < helpArrayTemp.length; i++ ) {
                    String content = "";
                    if ( i % 2 == 1 ) {
                        content += helpArrayTemp[i];
                        content += "]";
                    } else {
                        content += helpArrayTemp[i].replace( " ", "" );
                        content += "[";
                    }
                    helpTemp += content;
                }
                String[] helpArray = helpTemp.split( "," );
                help = helpArray[1];
                for ( int i = 2; i < helpArray.length - 1; i++ ) {
                    help += ", " + helpArray[i];
                }
            } else {
                help = commandlineHelp;
            }

            // format algorithm description for OpenOfficeCalc
            String algDesc = "";
            algDesc += alg.getCommandLineName() + "~";
            algDesc += alg.getName() + "~";
            algDesc += help + "~";
            algDesc += "-~";// dummy description
            algDesc += "-~";// dummy work

            // determinte input parameter types
            boolean[] inputTypes = getSupportedInputTypesAsArray( alg );
            for ( int i = 0; i < inputTypes.length; i++ ) {
                if ( inputTypes[i] )
                    algDesc += "X~";
                else
                    algDesc += "-~";
            }

            // determine output parameter types
            boolean[] outputTypes = getSupportedOutputTypesAsArray( alg );
            for ( int i = 0; i < outputTypes.length; i++ ) {
                if ( outputTypes[i] )
                    algDesc += "X~";
                else
                    algDesc += "-~";
            }

            System.out.println( algDesc );

            // input parameters
            ParametersSet paramSet = alg.getParameters();
            for ( int i = 0; i < paramSet.getNumberOfParameters(); i++ ) {
                Parameter param = paramSet.getParameter( i );
                String keyParam = param.getParameterTypeName();
                String valueParam = param.getParameterName();
                inputs.put( keyParam, valueParam );
            }

            // output parameters
            OutputObjectsSet outputSet = alg.getOutputObjects();
            for ( int i = 0; i < outputSet.getOutputObjectsCount(); i++ ) {
                Output output = outputSet.getOutput( i );
                String keyOutput = output.getTypeDescription();
                String valueOutput = output.getName();
                outputs.put( keyOutput, valueOutput );
            }

        }

        System.out.println( "USED INPUT PARAMETERS" );
        Set<String> inputKeys = inputs.keySet();
        for ( String key : inputKeys ) {
            System.out.println( "   " + key );
        }

        System.out.println( "USED OUTPUT PARAMETERS" );
        Set<String> outputKeys = outputs.keySet();
        for ( String key : outputKeys ) {
            System.out.println( "   " + key );
        }

        // System.out.println( Sextante.getAlgorithms().size() );

    }

    public static void main( String[] args ) {
        // /SextanteProcessProvider.getVectorLayerAlgorithms();

        Sextante.initialize();

        // printAlgorithms( vectorFilter );
        // printAlgorithms( vectorNumericalFilter );
        printAlgorithms( vectorOthersFilter );
        // printAlgorithms( allOthersFilter );

    }
}
