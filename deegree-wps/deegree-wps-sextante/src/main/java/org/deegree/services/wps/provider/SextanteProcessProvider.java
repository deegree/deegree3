//$HeadURL$
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
package org.deegree.services.wps.provider;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.services.exception.ServiceInitException;
import org.deegree.services.jaxb.wps.ComplexFormatType;
import org.deegree.services.jaxb.wps.ComplexInputDefinition;
import org.deegree.services.jaxb.wps.ComplexOutputDefinition;
import org.deegree.services.jaxb.wps.LanguageStringType;
import org.deegree.services.jaxb.wps.ProcessDefinition;
import org.deegree.services.jaxb.wps.ProcessletInputDefinition;
import org.deegree.services.jaxb.wps.ProcessletOutputDefinition;
import org.deegree.services.jaxb.wps.ProcessDefinition.InputParameters;
import org.deegree.services.jaxb.wps.ProcessDefinition.OutputParameters;
import org.deegree.services.wps.ExceptionAwareProcesslet;
import org.deegree.services.wps.ExceptionCustomizer;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.WPSProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.IGeoAlgorithmFilter;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.Parameter;

/**
 * {@link ProcessProvider} that provides <a
 * href="http://www.osor.eu/studies/sextante-a-geographic-information-system-for-the-spanish-region-of-extremadura"
 * >Sextante</a> processes.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SextanteProcessProvider implements ProcessProvider {

    // logger
    private static final Logger LOG = LoggerFactory.getLogger( SextanteProcessProvider.class );

    // manage SEXTANTE-WPS-Processes
    private final Map<CodeType, WPSProcess> idToProcess = new HashMap<CodeType, WPSProcess>();

    // FormatTypes
    private ComplexFormatType complexFormatTypeGML2;

    private ComplexFormatType complexFormatTypeGML30;

    private ComplexFormatType complexFormatTypeGML31;

    private ComplexFormatType complexFormatTypeGML32;

    private ComplexFormatType complexFormatTypeFeatureCollection;

    /**
     * Returns SEXTANTE-Algorithms. They need only a vector layer input and have a vector layer output.
     * 
     * @return SEXTANTE-Algorithms
     */
    public static GeoAlgorithm[] getVectorLayerInAndOutAlgorithms() {
        Sextante.initialize();

        /**
         * Filters algorithms (only with a vector layer input and output)
         */
        IGeoAlgorithmFilter algFilter = new IGeoAlgorithmFilter() {

            @Override
            public boolean accept( GeoAlgorithm alg ) {

                boolean answer = true;

                // Raster Layers inclusive Multiple Input
                if ( alg.getNumberOfRasterLayers( true ) > 0 )
                    answer = false;

                // Table Fields
                if ( alg.getNumberOfTableFieldsParameters() > 0 )
                    answer = false;

                // Bands
                if ( alg.getNumberOfBandsParameters() > 0 )
                    answer = false;

                // Tables
                if ( alg.getNumberOfTables() > 0 )
                    answer = false;

                // No Date Parameters like String, Number, Boolean, etc.
                if ( alg.getNumberOfNoDataParameters() > 0 )
                    answer = false;

                // Multiple Input - Vector Layer
                if ( alg.getNumberOfVectorLayers( true ) - alg.getNumberOfVectorLayers( false ) > 0 )
                    answer = false;

                // Vector Layer (only 1)
                if ( alg.getNumberOfVectorLayers( false ) > 1 )
                    answer = false;

                // Output Paramerter Raster and Vector Layer
                OutputObjectsSet outputObjects = alg.getOutputObjects();

                // only 1 vector layer
                if ( outputObjects.getOutputObjectsCount() == 1 )

                    for ( int i = 0; i < outputObjects.getOutputObjectsCount(); i++ ) {
                        Output out = outputObjects.getOutput( i );
                        if ( out.getTypeDescription().equals( "raster" ) )
                            answer = false;
                        else if ( out.getTypeDescription().equals( "table" ) )
                            answer = false;
                        else if ( out.getTypeDescription().equals( "text" ) )
                            answer = false;
                        else if ( out.getTypeDescription().equals( "chart" ) )
                            answer = false;
                    }

                else
                    answer = false;

                return answer;
            }
        };

        // create a array of SEXTANTE-Algorithms
        HashMap<?, ?> algorithms = Sextante.getAlgorithms( algFilter );
        Set<?> keys = algorithms.keySet();
        Iterator<?> iterator = keys.iterator();
        GeoAlgorithm[] algs = new GeoAlgorithm[keys.size()];
        for ( int j = 0; j < algs.length; j++ ) {
            GeoAlgorithm alg = (GeoAlgorithm) algorithms.get( iterator.next() );
            algs[j] = alg;
        }

        return algs;
    }

    SextanteProcessProvider() {
    }

    @Override
    public void init()
                            throws ServiceInitException {

        // initialize SEXTANTE
        Sextante.initialize();
        LOG.info( "Sextante initialized" );

        // TODO CORRECT THE REDUNDANZ
        // initialize format typs
        complexFormatTypeGML2 = new ComplexFormatType();
        complexFormatTypeGML2.setEncoding( "UTF-8" );
        complexFormatTypeGML2.setMimeType( "text/xml" );
        complexFormatTypeGML2.setSchema( "http://schemas.opengis.net/gml/2.1.2/geometry.xsd" );

        complexFormatTypeGML30 = new ComplexFormatType();
        complexFormatTypeGML30.setEncoding( "UTF-8" );
        complexFormatTypeGML30.setMimeType( "text/xml" );
        complexFormatTypeGML30.setSchema( "http://schemas.opengis.net/gml/3.0.1/base/geometryComplexes.xsd" );

        complexFormatTypeGML31 = new ComplexFormatType();
        complexFormatTypeGML31.setEncoding( "UTF-8" );
        complexFormatTypeGML31.setMimeType( "text/xml" );
        complexFormatTypeGML31.setSchema( "http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd" );

        complexFormatTypeGML32 = new ComplexFormatType();
        complexFormatTypeGML32.setEncoding( "UTF-8" );
        complexFormatTypeGML32.setMimeType( "text/xml" );
        complexFormatTypeGML32.setSchema( "http://schemas.opengis.net/gml/3.2.1/geometryComplexes.xsd" );

        complexFormatTypeFeatureCollection = new ComplexFormatType();
        complexFormatTypeFeatureCollection.setEncoding( "UTF-8" );
        complexFormatTypeFeatureCollection.setMimeType( "text/xml" );
        complexFormatTypeFeatureCollection.setSchema( "" );

        // initialize algorithms
        initializeAlgorithms();

    }

    @Override
    public void destroy() {
        for ( WPSProcess process : idToProcess.values() ) {
            process.getProcesslet().destroy();
        }
    }

    @Override
    public WPSProcess getProcess( CodeType id ) {
        return idToProcess.get( id );
    }

    @Override
    public Map<CodeType, WPSProcess> getProcesses() {
        return idToProcess;
    }

    /**
     * Initializes the processes based on Sextante.
     */
    private void initializeAlgorithms() {

        // collect SEXTANTE-Algorithms
        GeoAlgorithm[] algs = getVectorLayerInAndOutAlgorithms();

        for ( int i = 0; i < algs.length; i++ ) {

            // SEXTANTE-Algorithm
            GeoAlgorithm alg = algs[i];

            // LOGGING: initializing a process
            LOG.info( "Initializing process with id '" + alg.getCommandLineName() + "'" );

            // create processlet
            Processlet processlet = new SextanteProcesslet( alg );
            processlet.init();

            // create ExceptionCustomizer
            ExceptionCustomizer customizer = null;
            if ( processlet instanceof ExceptionAwareProcesslet ) {
                customizer = ( (ExceptionAwareProcesslet) processlet ).getExceptionCustomizer();
            }

            // create process definition
            ProcessDefinition def = createProcessDefinition( alg );

            // create process
            WPSProcess process = new WPSProcess( def, processlet, customizer );

            // create process key
            CodeType centroidCodeType = new CodeType( alg.getCommandLineName() );

            // add process
            idToProcess.put( centroidCodeType, process );
        }

    }

    /**
     * Creates a process definition for a SEXTANTE-Algorithm.
     * 
     * @param alg
     *            - SEXTANTE-Algorithm
     * @return
     */
    private ProcessDefinition createProcessDefinition( GeoAlgorithm alg ) {

        // ProcessDefinition
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setConfigVersion( "0.5.0" );
        processDefinition.setProcessVersion( "1.0.0" );
        processDefinition.setStatusSupported( false );

        // Identifier
        org.deegree.services.jaxb.wps.CodeType identifier = new org.deegree.services.jaxb.wps.CodeType();
        identifier.setValue( alg.getCommandLineName() );
        processDefinition.setIdentifier( identifier );

        // Title
        LanguageStringType title = new LanguageStringType();
        title.setValue( alg.getName() );
        processDefinition.setTitle( title );

        // Abstract
        LanguageStringType abstr = new LanguageStringType();
        String help = "";
        try {
            help = alg.getCommandLineHelp();
        } catch ( StringIndexOutOfBoundsException e ) {
        }
        abstr.setValue( help );
        processDefinition.setAbstract( abstr );

        // input parameters
        InputParameters input = new InputParameters();
        List<JAXBElement<? extends ProcessletInputDefinition>> listInput = input.getProcessInput();

        // traverses the input parameters
        for ( int i = 0; i < alg.getParameters().getNumberOfParameters(); i++ ) {

            // input parameter
            Parameter param = alg.getParameters().getParameter( i );

            // TODO CORRECT THE REDUNDANZ
            // select the correct input date type
            if ( param.getParameterTypeName().equals( "Vector Layer" ) )
                listInput.add( getVectorLayerInputDefinition( param ) );
            else if ( param.getParameterTypeName().equals( "Numerical Value" ) )
                listInput.add( getNumericalValueInputDefinition( param ) );
            else if ( param.getParameterTypeName().equals( "Selection" ) )
                listInput.add( getSelectionInputDefinition( param ) );
            else if ( param.getParameterTypeName().equals( "Filepath" ) )
                listInput.add( getFilepathInputDefinition( param ) );
            else if ( param.getParameterTypeName().equals( "Boolean" ) )
                listInput.add( getBooleanInputDefinition( param ) );
            else if ( param.getParameterTypeName().equals( "String" ) )
                listInput.add( getStringInputDefinition( param ) );
            else if ( param.getParameterTypeName().equals( "Multiple Input" ) )
                listInput.add( getMultipleInputInputDefinition( param, alg.getParameters() ) );
            else if ( param.getParameterTypeName().equals( "Raster Layer" ) )
                listInput.add( getRasterLayerInputDefinition( param ) );
            else if ( param.getParameterTypeName().equals( "Table Field" ) )
                listInput.add( getTableFieldInputDefinition( param ) );
            else if ( param.getParameterTypeName().equals( "Point" ) )
                listInput.add( getPointInputDefinition( param ) );
            else if ( param.getParameterTypeName().equals( "Band" ) )
                listInput.add( getBandInputDefinition( param ) );
            else if ( param.getParameterTypeName().equals( "Table" ) )
                listInput.add( getTableInputDefinition( param ) );
            else if ( param.getParameterTypeName().equals( "Fixed Table" ) )
                listInput.add( getFixedTableInputDefinition( param ) );
            else
                LOG.warn( "InputParameters: \"" + param.getParameterTypeName() + "\" is a unknown input type." );
        }

        // set input definition
        processDefinition.setInputParameters( input );

        // OutputParameters
        OutputParameters output = new OutputParameters();
        List<JAXBElement<? extends ProcessletOutputDefinition>> listOutput = output.getProcessOutput();
        OutputObjectsSet outputObjects = alg.getOutputObjects();

        // traverses the output parameters
        for ( int i = 0; i < outputObjects.getOutputObjectsCount(); i++ ) {

            // output parameter
            Output out = outputObjects.getOutput( i );

            // TODO CORRECT THE REDUNDANZ
            // select the correct output date type
            if ( out.getTypeDescription().equals( "vector" ) )
                listOutput.add( getVectorLayerOutputDefinition( out ) );
            else if ( out.getTypeDescription().equals( "raster" ) )
                listOutput.add( getRasterLayerOutputDefinition( out ) );
            else if ( out.getTypeDescription().equals( "table" ) )
                listOutput.add( getTableOutputDefinition( out ) );
            else if ( out.getTypeDescription().equals( "text" ) )
                listOutput.add( getTextOutputDefinition( out ) );
            else if ( out.getTypeDescription().equals( "chart" ) )
                listOutput.add( getChartOutputDefinition( out ) );
            else
                LOG.warn( "OutputParameters: \"" + out.getTypeDescription() + "\" is a unknown output type." );

        }

        // set output definition
        processDefinition.setOutputParameters( output );

        return processDefinition;
    }

    /**
     * Returns a input parameter definition for a vector layer.
     * 
     * @param param
     *            - input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> getVectorLayerInputDefinition( Parameter param ) {

        // ComplexInput
        QName complexInputName = new QName( "ComplexData" );
        ComplexInputDefinition complexInputValue = new ComplexInputDefinition();
        JAXBElement<ComplexInputDefinition> complexInput = new JAXBElement<ComplexInputDefinition>(
                                                                                                    complexInputName,
                                                                                                    ComplexInputDefinition.class,
                                                                                                    complexInputValue );
        // ComplexInput - Identifier
        org.deegree.services.jaxb.wps.CodeType complexInputIdentifier = new org.deegree.services.jaxb.wps.CodeType();
        complexInputIdentifier.setValue( param.getParameterName() );
        complexInputValue.setIdentifier( complexInputIdentifier );

        // ComplexInput - Title
        LanguageStringType complexInputTitle = new LanguageStringType();
        complexInputTitle.setValue( param.getParameterDescription() );
        complexInputValue.setTitle( complexInputTitle );

        // ComplexInput - Format
        complexInputValue.setDefaultFormat( complexFormatTypeGML2 );
        List<ComplexFormatType> inputOtherFormats = complexInputValue.getOtherFormats();
        inputOtherFormats.add( complexFormatTypeGML30 );
        inputOtherFormats.add( complexFormatTypeGML31 );
        inputOtherFormats.add( complexFormatTypeGML32 );

        return complexInput;
    }

    /**
     * Returns a input parameter definition for a numerical value.
     * 
     * @param param
     *            - input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> getNumericalValueInputDefinition( Parameter param ) {

        LOG.warn( "InputParameters: \"" + param.getParameterTypeName() + "\" is not supported." );

        // // LiteralInput
        // QName literalInputName = new QName( "LiteralData" );
        // LiteralInputDefinition literalInputValue = new LiteralInputDefinition();
        // JAXBElement<LiteralInputDefinition> literalInput = new JAXBElement<LiteralInputDefinition>(
        // literalInputName,
        // LiteralInputDefinition.class,
        // literalInputValue );
        // // LiteralInput - Identifier
        // org.deegree.services.jaxb.wps.CodeType literalInputIdentifier = new org.deegree.services.jaxb.wps.CodeType();
        // literalInputIdentifier.setValue( param.getParameterName() );
        // literalInputValue.setIdentifier( literalInputIdentifier );
        //
        // // LiteralInput - Title
        // LanguageStringType literalInputTitle = new LanguageStringType();
        // literalInputTitle.setValue( param.getParameterDescription() );
        // literalInputValue.setTitle( literalInputTitle );
        //
        // // LiteralInput - Format
        // DataType literalDataType = new DataType();
        // literalDataType.setValue( "double" );
        // literalInputValue.setDataType( literalDataType );

        return getVectorLayerInputDefinition( param );
    }

    /**
     * Returns a input parameter definition for a selection.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> getSelectionInputDefinition( Parameter param ) {
        LOG.warn( "InputParameters: \"" + param.getParameterTypeName() + "\" is not supported." );
        return getVectorLayerInputDefinition( param );
    }

    /**
     * Returns a input parameter definition for a filepath.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> getFilepathInputDefinition( Parameter param ) {
        LOG.warn( "InputParameters: \"" + param.getParameterTypeName() + "\" is not supported." );
        return getVectorLayerInputDefinition( param );
    }

    /**
     * Returns a input parameter definition for a boolean.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> getBooleanInputDefinition( Parameter param ) {
        LOG.warn( "InputParameters: \"" + param.getParameterTypeName() + "\" is not supported." );
        return getVectorLayerInputDefinition( param );
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
    private JAXBElement<? extends ProcessletInputDefinition> getMultipleInputInputDefinition( Parameter param,
                                                                                              ParametersSet paramSet ) {

        // type of vector layers
        if ( paramSet.getNumberOfVectorLayers( true ) - paramSet.getNumberOfVectorLayers( false ) > 0 ) {
            LOG.warn( "InputParameters: \"" + param.getParameterTypeName() + " - Vector Layer\" is not supported." );
        } else {

            // type of raster layers
            if ( paramSet.getNumberOfRasterLayers( true ) - paramSet.getNumberOfRasterLayers( false ) > 0 ) {
                LOG.warn( "InputParameters: \"" + param.getParameterTypeName() + " - Raster Layer\" is not supported." );
            } else {
                LOG.warn( "InputParameters: \"" + param.getParameterTypeName() + " - unknown type\" is not supported." );
            }
        }

        return getVectorLayerInputDefinition( param );
    }

    /**
     * Returns a input parameter definition for a string.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> getStringInputDefinition( Parameter param ) {
        LOG.warn( "InputParameters: \"" + param.getParameterTypeName() + "\" is not supported." );
        return getVectorLayerInputDefinition( param );
    }

    /**
     * Returns a input parameter definition for a raster layer.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> getRasterLayerInputDefinition( Parameter param ) {
        LOG.warn( "InputParameters: \"" + param.getParameterTypeName() + "\" is not supported." );
        return getVectorLayerInputDefinition( param );
    }

    /**
     * 
     * Returns a input parameter definition for a table field.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> getTableFieldInputDefinition( Parameter param ) {
        LOG.warn( "InputParameters: \"" + param.getParameterTypeName() + "\" is not supported." );
        return getVectorLayerInputDefinition( param );
    }

    /**
     * Returns a input parameter definition for a point.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> getPointInputDefinition( Parameter param ) {
        LOG.warn( "InputParameters: \"" + param.getParameterTypeName() + "\" is not supported." );
        return getVectorLayerInputDefinition( param );
    }

    /**
     * Returns a input parameter definition for a band.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> getBandInputDefinition( Parameter param ) {
        LOG.warn( "InputParameters: \"" + param.getParameterTypeName() + "\" is not supported." );
        return getVectorLayerInputDefinition( param );
    }

    /**
     * Returns a input parameter definition for a table.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> getTableInputDefinition( Parameter param ) {
        LOG.warn( "InputParameters: \"" + param.getParameterTypeName() + "\" is not supported." );
        return getVectorLayerInputDefinition( param );
    }

    /**
     * Returns a input parameter definition for a fixed table.
     * 
     * @param param
     *            input parameter
     * @return
     */
    private JAXBElement<? extends ProcessletInputDefinition> getFixedTableInputDefinition( Parameter param ) {
        LOG.warn( "InputParameters: \"" + param.getParameterTypeName() + "\" is not supported." );
        return getVectorLayerInputDefinition( param );
    }

    /**
     * Returns a output parameter definition for a vector layer.
     * 
     * @param out
     *            output parameter
     * @return
     */
    private JAXBElement<? extends ProcessletOutputDefinition> getVectorLayerOutputDefinition( Output out ) {

        // ComplexOutput
        QName complexOutputName = new QName( "ComplexOutput" );
        ComplexOutputDefinition complexOutputValue = new ComplexOutputDefinition();
        JAXBElement<ComplexOutputDefinition> complexOutput = new JAXBElement<ComplexOutputDefinition>(
                                                                                                       complexOutputName,
                                                                                                       ComplexOutputDefinition.class,
                                                                                                       complexOutputValue );
        // ComplexOutput - Identifier
        org.deegree.services.jaxb.wps.CodeType complexOutputIdentifier = new org.deegree.services.jaxb.wps.CodeType();
        complexOutputIdentifier.setValue( out.getName() );
        complexOutputValue.setIdentifier( complexOutputIdentifier );

        // ComplexOutput - Title
        LanguageStringType complexOutputTitle = new LanguageStringType();
        complexOutputTitle.setValue( out.getDescription() );
        complexOutputValue.setTitle( complexOutputTitle );

        // ComplexOutput - Format
        complexOutputValue.setDefaultFormat( complexFormatTypeGML2 );
        List<ComplexFormatType> outputOtherFormats = complexOutputValue.getOtherFormats();
        outputOtherFormats.add( complexFormatTypeGML30 );
        outputOtherFormats.add( complexFormatTypeGML31 );
        outputOtherFormats.add( complexFormatTypeGML32 );

        return complexOutput;
    }

    /**
     * Returns a output parameter definition for a raster layer.
     * 
     * @param out
     *            output parameter
     * @return
     */
    private JAXBElement<? extends ProcessletOutputDefinition> getRasterLayerOutputDefinition( Output out ) {
        LOG.warn( "OutputParameters: \"Raster Layer\" is not supported." );
        return getVectorLayerOutputDefinition( out );
    }

    /**
     * Returns a output parameter definition for a table.
     * 
     * @param out
     *            output parameter
     * @return
     */
    private JAXBElement<? extends ProcessletOutputDefinition> getTableOutputDefinition( Output out ) {
        LOG.warn( "OutputParameters: \"Table\" is not supported." );
        return getVectorLayerOutputDefinition( out );
    }

    /**
     * Returns a output parameter definition for a text.
     * 
     * @param out
     *            output parameter
     * @return
     */
    private JAXBElement<? extends ProcessletOutputDefinition> getTextOutputDefinition( Output out ) {
        LOG.warn( "OutputParameters: \"Text\" is not supported." );
        return getVectorLayerOutputDefinition( out );
    }

    /**
     * Returns a output parameter definition for a chart.
     * 
     * @param out
     *            output parameter
     * @return
     */
    private JAXBElement<? extends ProcessletOutputDefinition> getChartOutputDefinition( Output out ) {
        LOG.warn( "OutputParameters: \"Chart\" is not supported." );
        return getVectorLayerOutputDefinition( out );
    }

    /**
     * Logs a SEXTANTE-Algorithm with his input und output parameters.
     * 
     * @param alg
     *            - SEXTANTE-Algorithm
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
