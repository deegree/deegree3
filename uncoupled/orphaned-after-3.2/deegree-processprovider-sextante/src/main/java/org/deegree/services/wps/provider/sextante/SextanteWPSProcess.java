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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.deegree.process.jaxb.java.CodeType;
import org.deegree.process.jaxb.java.ComplexFormatType;
import org.deegree.process.jaxb.java.ComplexInputDefinition;
import org.deegree.process.jaxb.java.ComplexOutputDefinition;
import org.deegree.process.jaxb.java.LanguageStringType;
import org.deegree.process.jaxb.java.LiteralInputDefinition;
import org.deegree.process.jaxb.java.ProcessDefinition;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.process.jaxb.java.ProcessletOutputDefinition;
import org.deegree.process.jaxb.java.LiteralInputDefinition.DataType;
import org.deegree.process.jaxb.java.ProcessDefinition.InputParameters;
import org.deegree.process.jaxb.java.ProcessDefinition.OutputParameters;
import org.deegree.services.wps.ExceptionCustomizer;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.WPSProcess;
import org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.Parameter;

/**
 * This class presents a {@link WPSProcess} with a {@link SextanteProcesslet} and creates the {@link ProcessDefinition}
 * on the basis of the SEXTANTE {@link GeoAlgorithm}. Therefore this class must differentiates all input and output
 * parameters of a SEXTANTE {@link GeoAlgorithm}.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * 
 */
public class SextanteWPSProcess implements WPSProcess {

    private static final Logger LOG = LoggerFactory.getLogger( SextanteWPSProcess.class );

    // input parameter types
    public static final String VECTOR_LAYER_INPUT = "Vector Layer";

    public static final String NUMERICAL_VALUE_INPUT = "Numerical Value";

    public static final String SELECTION_INPUT = "Selection";

    public static final String FILEPATH_INPUT = "Filepath";

    public static final String BOOLEAN_INPUT = "Boolean";

    public static final String STRING_INPUT = "String";

    public static final String MULTIPLE_INPUT_INPUT = "Multiple Input";

    public static final String RASTER_LAYER_INPUT = "Raster Layer";

    public static final String TABLE_FIELD_INPUT = "Table Field";

    public static final String POINT_INPUT = "Point";

    public static final String BAND_INPUT = "Band";

    public static final String TABLE_INPUT = "Table";

    public static final String FIXED_TABLE_INPUT = "Fixed Table";

    // output parameter types
    public static final String VECTOR_LAYER_OUTPUT = "vector";

    public static final String RASTER_LAYER_OUTPUT = "raster";

    public static final String TABLE_OUTPUT = "table";

    public static final String TEXT_OUTPUT = "text";

    public static final String CHART_OUTPUT = "chart";

    // prefix for process identifier
    private static final String PREFIX = "st_";

    // suffix for process identifier
    private static final String SUFFIX = "";

    /**
     * This method creates the command line name from a identifier of a {@link SextanteProcesses}. It removes prefix and
     * suffix.
     * 
     * @param indentifier
     *            Identifier of a {@link SextanteProcesses}.
     * @return Command line name of a SEXTANTE {@link GeoAlgorithm}.
     */
    public static String createCommandLineName( String indentifier ) {
        String s = indentifier.substring( PREFIX.length(), ( indentifier.length() - SUFFIX.length() ) );
        return s;
    }

    /**
     * This method creates the identifier for a {@link SextanteWPSProcess} with its prefix and suffix.
     * 
     * @param alg
     *            SEXTANTE {@link GeoAlgorithm}.
     * @return Identifier for a {@link SextanteWPSProcess}.
     */
    public static String createIdentifier( GeoAlgorithm alg ) {
        return PREFIX + alg.getCommandLineName() + SUFFIX;
    }

    // processlet
    private final Processlet processlet;

    // process description
    private final ProcessDefinition description;

    // complex format types
    private LinkedList<ComplexFormatType> gmInputFormats = FormatHelper.getInputFormatsWithoutDefault();

    private LinkedList<ComplexFormatType> gmlOutputFormats = FormatHelper.getOutputFormatsWithoutDefault();

    SextanteWPSProcess( GeoAlgorithm alg, SextanteProcesses config ) {
        processlet = new SextanteProcesslet( alg );
        description = createDescription( alg, config );
    }

    @Override
    public Processlet getProcesslet() {
        return processlet;
    }

    @Override
    public ProcessDefinition getDescription() {
        return description;
    }

    @Override
    public ExceptionCustomizer getExceptionCustomizer() {
        return null;
    }

    /**
     * This method determines a process object of the configuration file.
     * 
     * @param alg
     *            SEXTANTE {@link GeoAlgorithm}.
     * @param config
     *            SEXTANTE configuration file.
     * 
     * @return process object of the configuration file.
     */
    private org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process determineProcess(
                                                                                                        GeoAlgorithm alg,
                                                                                                        SextanteProcesses config ) {

        org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process process = null;
        List<org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process> processes = config.getProcess();

        // determine process
        for ( org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process p : processes ) {
            if ( p.getId().equals( alg.getCommandLineName() ) ) {
                process = p;
                break;
            }
        }

        return process;
    }

    /**
     * This method determines a input parameter of a process in the SEXTANTE configuration file.
     * 
     * @param p
     *            Process of the SEXTANTE configuration file.
     * @param name
     *            Name of the input parameter.
     * @return Input parameter of a process in the configuration file.
     */
    private org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process.InputParameters.Parameter determineInputParameter(
                                                                                                                                         org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process p,

                                                                                                                                         String name ) {
        org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process.InputParameters.Parameter parameter = null;
        List<org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process.InputParameters.Parameter> params = p.getInputParameters().getParameter();

        // determine input parameter
        for ( org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process.InputParameters.Parameter param : params ) {
            if ( param.getId().equals( name ) ) {
                parameter = param;
                break;
            }
        }
        return parameter;
    }

    /**
     * This method determines a output parameter of a process in the SEXTANTE configuration file.
     * 
     * @param p
     *            Process of the SEXTANTE configuration file.
     * @param name
     *            Name of the output parameter.
     * 
     * @return Output parameter of a process in the configuration file.
     */
    private org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process.OutputParameters.Parameter determineOutputParameter(
                                                                                                                                           org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process p,
                                                                                                                                           String name ) {
        org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process.OutputParameters.Parameter parameter = null;
        List<org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process.OutputParameters.Parameter> params = p.getOutputParameters().getParameter();

        // determine output parameter
        for ( org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process.OutputParameters.Parameter param : params ) {
            if ( param.getId().equals( name ) ) {
                parameter = param;
                break;
            }
        }
        return parameter;
    }

    /**
     * Creates a {@link ProcessDefinition} for a SEXTANTE {@link GeoAlgorithm}.
     * 
     * @param alg
     *            SEXTANTE {@link GeoAlgorithm}
     * @return
     */
    private ProcessDefinition createDescription( GeoAlgorithm alg, SextanteProcesses config ) {

        // process definition
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setConfigVersion( "0.5.0" );
        processDefinition.setProcessVersion( "1.0.0" );
        processDefinition.setStatusSupported( false );
        processDefinition.setStoreSupported( true );

        // identifier
        String identifierStr = createIdentifier( alg );
        CodeType identifier = new CodeType();
        identifier.setValue( identifierStr );
        processDefinition.setIdentifier( identifier );

        // title
        LanguageStringType title = new LanguageStringType();
        title.setValue( alg.getName() );
        processDefinition.setTitle( title );

        // abstract
        String abstrStr = "No abstract available.";
        org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process process = determineProcess( alg,
                                                                                                              config );
        if ( process != null ) {
            abstrStr = process.getAbstract();
        }
        LanguageStringType abstr = new LanguageStringType();
        abstr.setValue( abstrStr );
        processDefinition.setAbstract( abstr );

        // define input parameters
        InputParameters input = new InputParameters();
        List<JAXBElement<? extends ProcessletInputDefinition>> listInput = input.getProcessInput();

        // traverses the input parameters
        for ( int i = 0; i < alg.getParameters().getNumberOfParameters(); i++ ) {

            // input parameter
            Parameter param = alg.getParameters().getParameter( i );

            // select the correct input parameter type
            String paramTypeName = param.getParameterTypeName();
            if ( param.getParameterTypeName().equals( VECTOR_LAYER_INPUT ) )
                listInput.add( createVectorLayerInputParameter( param ) );
            else if ( paramTypeName.equals( NUMERICAL_VALUE_INPUT ) )
                listInput.add( createNumericalValueInputParameter( param ) );
            else if ( paramTypeName.equals( SELECTION_INPUT ) )
                listInput.add( createSelectionInputParameter( param ) );
            else if ( paramTypeName.equals( FILEPATH_INPUT ) )
                listInput.add( createFilepathInputParameter( param ) );
            else if ( paramTypeName.equals( BOOLEAN_INPUT ) )
                listInput.add( createBooleanInputParameter( param ) );
            else if ( paramTypeName.equals( STRING_INPUT ) )
                listInput.add( createStringInputParameter( param ) );
            else if ( paramTypeName.equals( MULTIPLE_INPUT_INPUT ) )
                listInput.add( createMultipleInputInputParameter( param, alg.getParameters() ) );
            else if ( paramTypeName.equals( RASTER_LAYER_INPUT ) )
                listInput.add( createRasterLayerInputParameter( param ) );
            else if ( paramTypeName.equals( TABLE_FIELD_INPUT ) )
                listInput.add( createTableFieldInputParameter( param ) );
            else if ( paramTypeName.equals( POINT_INPUT ) )
                listInput.add( createPointInputParameter( param ) );
            else if ( paramTypeName.equals( BAND_INPUT ) )
                listInput.add( createBandInputParameter( param ) );
            else if ( paramTypeName.equals( TABLE_INPUT ) )
                listInput.add( createTableInputParameter( param ) );
            else if ( paramTypeName.equals( FIXED_TABLE_INPUT ) )
                listInput.add( createFixedTableInputParameter( param ) );
            else {
                LOG.error( "'" + paramTypeName + "' is a not supported input parameter type." );
                // TODO throw exception
            }

            // set parameter abstract
            org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process.InputParameters.Parameter configParam = determineInputParameter(
                                                                                                                                                       process,
                                                                                                                                                       param.getParameterName() );
            JAXBElement<? extends ProcessletInputDefinition> descParam = listInput.get( listInput.size() - 1 );
            if ( descParam != null ) {
                if ( configParam != null ) {

                    // abstract of a input parameter
                    String paramAbstract = configParam.getAbstract();

                    // if input value is a selection, extends abstract
                    List<org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process.InputParameters.Parameter.SelectionValue> selValues = configParam.getSelectionValue();
                    if ( selValues.size() > 0 ) {

                        // add dot
                        if ( !paramAbstract.valueOf( paramAbstract.length() ).equals( "." ) ) {
                            paramAbstract += ". ";
                        }

                        String selValuesStr = " You can use the following values: ";

                        Iterator<org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process.InputParameters.Parameter.SelectionValue> selIt = selValues.iterator();
                        org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process.InputParameters.Parameter.SelectionValue firstValue = selIt.next();
                        selValuesStr += firstValue.getId() + ": " + firstValue.getValue();

                        while ( selIt.hasNext() ) {
                            org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process.InputParameters.Parameter.SelectionValue selValue = selIt.next();
                            selValuesStr += ", " + selValue.getId() + ": " + selValue.getValue();
                        }

                        paramAbstract += selValuesStr;
                    }

                    LanguageStringType value = new LanguageStringType();
                    value.setValue( paramAbstract );
                    descParam.getValue().setAbstract( value );
                }
            }
        }

        // set input parameters
        processDefinition.setInputParameters( input );

        // define output parameters
        OutputParameters output = new OutputParameters();
        List<JAXBElement<? extends ProcessletOutputDefinition>> listOutput = output.getProcessOutput();
        OutputObjectsSet outputObjects = alg.getOutputObjects();

        // traverses the output parameters
        for ( int i = 0; i < outputObjects.getOutputObjectsCount(); i++ ) {

            // output parameter
            Output param = outputObjects.getOutput( i );

            // select the correct output parameter type
            String paramTypeDesc = param.getTypeDescription();
            if ( paramTypeDesc.equals( VECTOR_LAYER_OUTPUT ) )
                listOutput.add( createVectorLayerOutputParameter( param ) );
            else if ( paramTypeDesc.equals( RASTER_LAYER_OUTPUT ) )
                listOutput.add( createRasterLayerOutputParameter( param ) );
            else if ( paramTypeDesc.equals( TABLE_OUTPUT ) )
                listOutput.add( createTableOutputParameter( param ) );
            else if ( paramTypeDesc.equals( TEXT_OUTPUT ) )
                listOutput.add( createTextOutputParameter( param ) );
            else if ( paramTypeDesc.equals( CHART_OUTPUT ) )
                listOutput.add( createChartOutputParameter( param ) );
            else {
                LOG.error( "'" + paramTypeDesc + "' is a not supported output parameter type." );
                // TODO throw exception
            }

            // set parameter abstract
            org.deegree.services.wps.provider.sextante.jaxb.SextanteProcesses.Process.OutputParameters.Parameter configParam = determineOutputParameter(
                                                                                                                                                         process,
                                                                                                                                                         param.getName() );

            JAXBElement<? extends ProcessletOutputDefinition> descParam = listOutput.get( listOutput.size() - 1 );
            if ( descParam != null ) {
                if ( configParam != null ) {
                    // abstract of a output parameter
                    String paramAbstract = configParam.getAbstract();
                    LanguageStringType value = new LanguageStringType();
                    value.setValue( paramAbstract );
                    descParam.getValue().setAbstract( value );
                }
            }

        }

        // set output parameters
        processDefinition.setOutputParameters( output );

        return processDefinition;
    }

    /**
     * Returns a input parameter definition for a vector layer.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> createVectorLayerInputParameter( Parameter param ) {

        // ComplexInput
        QName complexInputName = new QName( "ComplexData" );
        ComplexInputDefinition complexInputValue = new ComplexInputDefinition();
        JAXBElement<ComplexInputDefinition> complexInput = new JAXBElement<ComplexInputDefinition>(
                                                                                                    complexInputName,
                                                                                                    ComplexInputDefinition.class,
                                                                                                    complexInputValue );
        // ComplexInput - Identifier
        CodeType complexInputIdentifier = new CodeType();
        complexInputIdentifier.setValue( param.getParameterName() );
        complexInputValue.setIdentifier( complexInputIdentifier );

        // ComplexInput - Title
        LanguageStringType complexInputTitle = new LanguageStringType();
        complexInputTitle.setValue( param.getParameterDescription() );
        complexInputValue.setTitle( complexInputTitle );

        // ComplexInput - Format
        complexInputValue.setDefaultFormat( FormatHelper.getDefaultInputFormat() );
        List<ComplexFormatType> inputOtherFormats = complexInputValue.getOtherFormats();
        inputOtherFormats.addAll( gmInputFormats );

        return complexInput;
    }

    /**
     * Returns a input parameter definition for a numerical value.
     * 
     * @param param
     *            - input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> createNumericalValueInputParameter( Parameter param ) {

        // LiteralInput
        QName literalInputName = new QName( "LiteralData" );
        LiteralInputDefinition literalInputValue = new LiteralInputDefinition();
        JAXBElement<LiteralInputDefinition> literalInput = new JAXBElement<LiteralInputDefinition>(
                                                                                                    literalInputName,
                                                                                                    LiteralInputDefinition.class,
                                                                                                    literalInputValue );
        // LiteralInput - Identifier
        CodeType literalInputIdentifier = new CodeType();
        literalInputIdentifier.setValue( param.getParameterName() );
        literalInputValue.setIdentifier( literalInputIdentifier );

        // LiteralInput - Title
        LanguageStringType literalInputTitle = new LanguageStringType();
        literalInputTitle.setValue( param.getParameterDescription() );
        literalInputValue.setTitle( literalInputTitle );

        // LiteralInput - Format
        DataType literalDataType = new DataType();
        literalDataType.setValue( "double" );
        literalDataType.setReference( "http://www.w3.org/TR/xmlschema-2/#double" );
        literalInputValue.setDataType( literalDataType );

        return literalInput;
    }

    /**
     * Returns a input parameter definition for a selection.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> createSelectionInputParameter( Parameter param ) {

        // LiteralInput
        QName literalInputName = new QName( "LiteralData" );
        LiteralInputDefinition literalInputValue = new LiteralInputDefinition();
        JAXBElement<LiteralInputDefinition> literalInput = new JAXBElement<LiteralInputDefinition>(
                                                                                                    literalInputName,
                                                                                                    LiteralInputDefinition.class,
                                                                                                    literalInputValue );
        // LiteralInput - Identifier
        CodeType literalInputIdentifier = new CodeType();
        literalInputIdentifier.setValue( param.getParameterName() );
        literalInputValue.setIdentifier( literalInputIdentifier );

        // LiteralInput - Title
        LanguageStringType literalInputTitle = new LanguageStringType();
        literalInputTitle.setValue( param.getParameterDescription() );
        literalInputValue.setTitle( literalInputTitle );

        // LiteralInput - Format
        DataType literalDataType = new DataType();
        literalDataType.setValue( "integer" );
        literalDataType.setReference( "http://www.w3.org/TR/xmlschema-2/#integer" );
        literalInputValue.setDataType( literalDataType );

        return literalInput;
    }

    /**
     * Returns a input parameter definition for a filepath.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> createFilepathInputParameter( Parameter param ) {
        LOG.error( "'" + param.getParameterTypeName()
                   + "' a is not supported input parameter type (but is in implementation)" );
        // TODO implement this input parameter type
        return null;
    }

    /**
     * Returns a input parameter definition for a boolean.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> createBooleanInputParameter( Parameter param ) {

        // LiteralInput
        QName literalInputName = new QName( "LiteralData" );
        LiteralInputDefinition literalInputValue = new LiteralInputDefinition();
        JAXBElement<LiteralInputDefinition> literalInput = new JAXBElement<LiteralInputDefinition>(
                                                                                                    literalInputName,
                                                                                                    LiteralInputDefinition.class,
                                                                                                    literalInputValue );
        // LiteralInput - Identifier
        CodeType literalInputIdentifier = new CodeType();
        literalInputIdentifier.setValue( param.getParameterName() );
        literalInputValue.setIdentifier( literalInputIdentifier );

        // LiteralInput - Title
        LanguageStringType literalInputTitle = new LanguageStringType();
        literalInputTitle.setValue( param.getParameterDescription() );
        literalInputValue.setTitle( literalInputTitle );

        // LiteralInput - Format
        DataType literalDataType = new DataType();
        literalDataType.setValue( "boolean" );
        literalDataType.setReference( "http://www.w3.org/TR/xmlschema-2/#boolean" );
        literalInputValue.setDataType( literalDataType );

        return literalInput;

    }

    /**
     * Returns a input parameter definition for a multiple input.
     * 
     * @param param
     *            input parameter
     * @param paramSet
     *            set of parameters
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> createMultipleInputInputParameter( Parameter param,
                                                                                                ParametersSet paramSet ) {

        // type of vector layers
        if ( paramSet.getNumberOfVectorLayers( true ) - paramSet.getNumberOfVectorLayers( false ) > 0 ) {
            LOG.error( "'" + param.getParameterTypeName()
                       + " - Vector Layer' a is not supported input parameter type (but is in implementation)" );
            // TODO implement this input parameter type
        } else {

            // type of raster layers
            if ( paramSet.getNumberOfRasterLayers( true ) - paramSet.getNumberOfRasterLayers( false ) > 0 ) {
                LOG.error( "'" + param.getParameterTypeName()
                           + " - Raster Layer' a is not supported input parameter type (but is in implementation)" );
                // TODO implement this input parameter type

            } else {
                LOG.error( "'" + param.getParameterTypeName()
                           + " - unknown type' a is not supported input parameter type." );
                // TODO throw exception
            }
        }

        return null;
    }

    /**
     * Returns a input parameter definition for a string.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> createStringInputParameter( Parameter param ) {

        // LiteralInput
        QName literalInputName = new QName( "LiteralData" );
        LiteralInputDefinition literalInputValue = new LiteralInputDefinition();
        JAXBElement<LiteralInputDefinition> literalInput = new JAXBElement<LiteralInputDefinition>(
                                                                                                    literalInputName,
                                                                                                    LiteralInputDefinition.class,
                                                                                                    literalInputValue );
        // LiteralInput - Identifier
        CodeType literalInputIdentifier = new CodeType();
        literalInputIdentifier.setValue( param.getParameterName() );
        literalInputValue.setIdentifier( literalInputIdentifier );

        // LiteralInput - Title
        LanguageStringType literalInputTitle = new LanguageStringType();
        literalInputTitle.setValue( param.getParameterDescription() );
        literalInputValue.setTitle( literalInputTitle );

        // LiteralInput - Format
        DataType literalDataType = new DataType();
        literalDataType.setValue( "string" );
        literalDataType.setReference( "http://www.w3.org/TR/xmlschema-2/#string" );
        literalInputValue.setDataType( literalDataType );

        return literalInput;
    }

    /**
     * Returns a input parameter definition for a raster layer.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> createRasterLayerInputParameter( Parameter param ) {
        LOG.error( "'" + param.getParameterTypeName()
                   + "' a is not supported input parameter type (but is in implementation)" );
        // TODO implement this input parameter type
        return null;
    }

    /**
     * 
     * Returns a input parameter definition for a table field.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> createTableFieldInputParameter( Parameter param ) {
        LOG.error( "'" + param.getParameterTypeName()
                   + "' a is not supported input parameter type (but is in implementation)" );
        // TODO implement this input parameter type
        return null;
    }

    /**
     * Returns a input parameter definition for a point.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> createPointInputParameter( Parameter param ) {
        LOG.error( "'" + param.getParameterTypeName()
                   + "' a is not supported input parameter type (but is in implementation)" );
        // TODO implement this input parameter type
        return null;
    }

    /**
     * Returns a input parameter definition for a band.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> createBandInputParameter( Parameter param ) {
        LOG.error( "'" + param.getParameterTypeName()
                   + "' a is not supported input parameter type (but is in implementation)" );
        // TODO implement this input parameter type
        return null;
    }

    /**
     * Returns a input parameter definition for a table.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> createTableInputParameter( Parameter param ) {
        LOG.error( "'" + param.getParameterTypeName()
                   + "' a is not supported input parameter type (but is in implementation)" );
        // TODO implement this input parameter type
        return null;
    }

    /**
     * Returns a input parameter definition for a fixed table.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> createFixedTableInputParameter( Parameter param ) {
        LOG.error( "'" + param.getParameterTypeName()
                   + "' a is not supported input parameter type (but is in implementation)" );
        // TODO implement this input parameter type
        return null;
    }

    /**
     * Returns a output parameter definition for a vector layer.
     * 
     * @param out
     *            output parameter
     * @return
     */
    private JAXBElement<? extends ProcessletOutputDefinition> createVectorLayerOutputParameter( Output out ) {

        // ComplexOutput
        QName complexOutputName = new QName( "ComplexOutput" );
        ComplexOutputDefinition complexOutputValue = new ComplexOutputDefinition();
        JAXBElement<ComplexOutputDefinition> complexOutput = new JAXBElement<ComplexOutputDefinition>(
                                                                                                       complexOutputName,
                                                                                                       ComplexOutputDefinition.class,
                                                                                                       complexOutputValue );
        // ComplexOutput - Identifier
        CodeType complexOutputIdentifier = new CodeType();
        complexOutputIdentifier.setValue( out.getName() );
        complexOutputValue.setIdentifier( complexOutputIdentifier );

        // ComplexOutput - Title
        LanguageStringType complexOutputTitle = new LanguageStringType();
        complexOutputTitle.setValue( out.getDescription() );
        complexOutputValue.setTitle( complexOutputTitle );

        // ComplexOutput - Format
        complexOutputValue.setDefaultFormat( FormatHelper.getDefaultOutputFormat() );
        List<ComplexFormatType> outputOtherFormats = complexOutputValue.getOtherFormats();
        outputOtherFormats.addAll( gmlOutputFormats );

        return complexOutput;
    }

    /**
     * Returns a output parameter definition for a raster layer.
     * 
     * @param out
     *            output parameter
     * @return
     */
    private JAXBElement<? extends ProcessletOutputDefinition> createRasterLayerOutputParameter( Output out ) {
        LOG.error( "'" + out.getTypeDescription()
                   + "' a is not supported output parameter type (but is in implementation)" );
        // TODO implement this output parameter type
        return null;
    }

    /**
     * Returns a output parameter definition for a table.
     * 
     * @param out
     *            output parameter
     * @return
     */
    private JAXBElement<? extends ProcessletOutputDefinition> createTableOutputParameter( Output out ) {
        LOG.error( "'" + out.getTypeDescription()
                   + "' a is not supported output parameter type (but is in implementation)" );
        // TODO implement this output parameter type
        return null;
    }

    /**
     * Returns a output parameter definition for a text.
     * 
     * @param out
     *            output parameter
     * @return
     */
    private JAXBElement<? extends ProcessletOutputDefinition> createTextOutputParameter( Output out ) {
        LOG.error( "'" + out.getTypeDescription()
                   + "' a is not supported output parameter type (but is in implementation)" );
        // TODO implement this output parameter type
        return null;
    }

    /**
     * Returns a output parameter definition for a chart.
     * 
     * @param out
     *            output parameter
     * @return
     */
    private JAXBElement<? extends ProcessletOutputDefinition> createChartOutputParameter( Output out ) {
        LOG.error( "'" + out.getTypeDescription()
                   + "' a is not supported output parameter type (but is in implementation)" );
        // TODO implement this output parameter type
        return null;
    }

    /**
     * Logs a SEXTANTE {@link GeoAlgorithm} with his input und output parameters.
     * 
     * @param alg
     *            SEXTANTE {@link GeoAlgorithm}
     */
    public static void logAlgorithm( GeoAlgorithm alg ) {

        LOG.info( "ALGORITHM: " + alg.getCommandLineName() + " (" + alg.getName() + ")" );
        ParametersSet paramSet = alg.getParameters();
        for ( int i = 0; i < paramSet.getNumberOfParameters(); i++ ) {
            LOG.info( "InputParameter: " + paramSet.getParameter( i ).getParameterName() + " ("
                      + paramSet.getParameter( i ).getParameterTypeName() + ")" );
        }
        OutputObjectsSet outputSet = alg.getOutputObjects();
        for ( int i = 0; i < outputSet.getOutputDataObjectsCount(); i++ ) {
            LOG.info( "OutputParameter: " + outputSet.getOutput( i ).getName() + " ("
                      + outputSet.getOutput( i ).getTypeDescription() + ")" );
        }
    }

}
