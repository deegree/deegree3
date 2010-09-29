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
package org.deegree.services.wps.provider.sextante;

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.output.ComplexOutput;
import org.deegree.services.wps.provider.sextante.GMLSchema.GMLType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.IteratorException;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.WrongOutputIDException;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.Parameter;

/**
 * This class presents a {@link Processlet} with a SEXTANTE {@link GeoAlgorithm} Therefore this class must
 * differentiates all input and output parameters of a SEXTANTE {@link GeoAlgorithm}.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class SextanteProcesslet implements Processlet {

    // logger
    private static final Logger LOG = LoggerFactory.getLogger( SextanteProcesslet.class );

    // SEXTANTE algorithm
    private final GeoAlgorithm alg;

    SextanteProcesslet( GeoAlgorithm alg ) {
        this.alg = alg;
    }

    @Override
    public void destroy() {
        // LOG.info( "Destroying process with id '" + alg.getCommandLineName() + "'" );
    }

    @Override
    public void init() {
        LOG.info( "Initializing process with id '" + SextanteWPSProcess.createIdentifier( alg ) + "'" );
    }

    @Override
    public void process( ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info )
                            throws ProcessletException {
        try {

            SextanteWPSProcess.logAlgorithm( alg );
            // LOG.info( "SET INPUT PARAMETERS" );

            // sets all input values
            setInputValues( alg, in );

            // LOG.info( "GET OUTPUT PARAMETERS" );

            // execute the algorithm
            alg.execute( null, new OutputFactoryExt() );

            // write all output values
            writeResult( alg, out );

        } catch ( NullParameterValueException e ) { // false input data
            e.printStackTrace();
            String message = "'" + SextanteWPSProcess.createIdentifier( alg ) + "' algorithm found false input data. ("
                             + e.getLocalizedMessage() + ")";
            throw new ProcessletException( message );

        } catch ( ArrayIndexOutOfBoundsException e ) { // false input data
            e.printStackTrace();
            String message = "'" + SextanteWPSProcess.createIdentifier( alg ) + "' algorithm found false input data. ("
                             + e.getLocalizedMessage() + ")";
            throw new ProcessletException( message );

        } catch ( NullPointerException e ) { // false input data
            e.printStackTrace();
            String message = "'" + SextanteWPSProcess.createIdentifier( alg ) + "' algorithm found false input data. ("
                             + e.getLocalizedMessage() + ")";
            throw new ProcessletException( message );

        } catch ( IndexOutOfBoundsException e ) { // false input data
            e.printStackTrace();
            String message = "'" + SextanteWPSProcess.createIdentifier( alg ) + "' algorithm found false input data. ("
                             + e.getLocalizedMessage() + ")";
            throw new ProcessletException( message );

        } catch ( Exception e ) {
            e.printStackTrace();
            throw new ProcessletException( e.getLocalizedMessage() );
        }

    }

    /**
     * Commits the input data (all supported types) to the {@link GeoAlgorithm} input parameter.
     * 
     * @param alg
     *            SEXTANTE {@link GeoAlgorithm}.
     * @param in
     *            Input data as {@link ProcessletInputs}.
     * 
     * @throws ProcessletException
     * @throws ClassNotFoundException
     */
    private void setInputValues( GeoAlgorithm alg, ProcessletInputs in )
                            throws ProcessletException, ClassNotFoundException {

        // input parameters
        ParametersSet paramSet = alg.getParameters();

        // traverses the input parameters
        for ( int i = 0; i < paramSet.getNumberOfParameters(); i++ ) {
            Parameter param = paramSet.getParameter( i );

            // set the correct input parameter value
            String paramTypeName = param.getParameterTypeName();
            if ( paramTypeName.equals( SextanteWPSProcess.VECTOR_LAYER_INPUT ) )
                setVectorLayerInputValue( in, param );
            else if ( paramTypeName.equals( SextanteWPSProcess.NUMERICAL_VALUE_INPUT ) )
                setNumericalValueInputValue( in, param );
            else if ( paramTypeName.equals( SextanteWPSProcess.SELECTION_INPUT ) )
                setSelectionInputValue( in, param, alg );
            else if ( paramTypeName.equals( SextanteWPSProcess.FILEPATH_INPUT ) )
                setFilepathInputValue( in, param );
            else if ( paramTypeName.equals( SextanteWPSProcess.BOOLEAN_INPUT ) )
                setBooleanInputValue( in, param );
            else if ( paramTypeName.equals( SextanteWPSProcess.STRING_INPUT ) )
                setStringInputValue( in, param );
            else if ( paramTypeName.equals( SextanteWPSProcess.MULTIPLE_INPUT_INPUT ) )
                setMultipleInputInputValue( in, param, alg.getParameters() );
            else if ( paramTypeName.equals( SextanteWPSProcess.RASTER_LAYER_INPUT ) )
                setRasterLayerInputValue( in, param );
            else if ( paramTypeName.equals( SextanteWPSProcess.TABLE_FIELD_INPUT ) )
                setTableFieldInputValue( in, param );
            else if ( paramTypeName.equals( SextanteWPSProcess.POINT_INPUT ) )
                setPointInputValue( in, param );
            else if ( paramTypeName.equals( SextanteWPSProcess.BAND_INPUT ) )
                setBandInputValue( in, param );
            else if ( paramTypeName.equals( SextanteWPSProcess.TABLE_INPUT ) )
                setTableInputValue( in, param );
            else if ( paramTypeName.equals( SextanteWPSProcess.FIXED_TABLE_INPUT ) )
                setFixedTableInputValue( in, param );
            else {
                LOG.error( "\"" + paramTypeName + "\" is a not supported input parameter type." );
                // TODO throw exception
            }
        }
    }

    /**
     * Commits the {@link IVectorLayer} input data to the {@link GeoAlgorithm} input parameter.
     * 
     * @param in
     *            Input data as {@link ProcessletInputs}.
     * @param param
     *            Input parameter of {@link GeoAlgorithm}
     * @throws ClassNotFoundException
     */
    private void setVectorLayerInputValue( ProcessletInputs in, Parameter param )
                            throws ProcessletException, ClassNotFoundException {
        // input object
        ComplexInput gmlInput = (ComplexInput) in.getParameter( param.getParameterName() );

        // create vector layer
        IVectorLayer layer = null;

        // feature collection input
        if ( FormatHelper.determineGMLType( gmlInput ).equals( GMLType.FEATURE_COLLECTION ) ) {
            FeatureCollection coll = readFeatureCollection( gmlInput );
            layer = VectorLayerAdapter.createVectorLayer( coll );
        } else {

            // geometry input
            if ( FormatHelper.determineGMLType( gmlInput ).equals( GMLType.GEOMETRY ) ) {
                Geometry geometry = readGeometry( gmlInput );
                layer = VectorLayerAdapter.createVectorLayer( geometry );
            }
        }

        if ( layer != null ) {
            // set vector layer
            param.setParameterValue( layer );

        } else {// unknown payload
            LOG.error( "Type (GEOMETRY OR FEATURE COLLECTION) of schema \"" + gmlInput.getSchema() + "\" is unknown." );
            // TODO throw exception
        }

    }

    /**
     * Commits the numerical input data to the {@link GeoAlgorithm} input parameter.
     * 
     * @param in
     *            Input data as {@link ProcessletInputs}.
     * @param param
     *            Input parameter of {@link GeoAlgorithm}.
     */
    private void setNumericalValueInputValue( ProcessletInputs in, Parameter param ) {
        LiteralInput literalInput = (LiteralInput) in.getParameter( param.getParameterName() );
        param.setParameterValue( Double.parseDouble( literalInput.getValue() ) );
    }

    /**
     * Commits the selection input data to the {@link GeoAlgorithm} input parameter.
     * 
     * @param in
     *            Input data as {@link ProcessletInputs}.
     * @param param
     *            Input parameter of {@link GeoAlgorithm}.
     */
    private void setSelectionInputValue( ProcessletInputs in, Parameter param, GeoAlgorithm alg ) {
        LiteralInput literalInput = (LiteralInput) in.getParameter( param.getParameterName() );

        // value as string
        String valueAsString = literalInput.getValue();

        // value as integer
        int valueAsInteger = 0;

        // string to integer
        if ( org.apache.commons.lang.math.NumberUtils.isNumber( valueAsString ) )
            valueAsInteger = Integer.parseInt( valueAsString );

        // special case of 'vectoraddfield' algorithm
        if ( alg.getCommandLineName().equals( "vectoraddfield" ) ) {

            @SuppressWarnings("unused")
            String valueAsText;

            if ( valueAsInteger == 0 )
                valueAsText = "Integer";
            else if ( valueAsInteger == 1 )
                valueAsText = "Double";
            else
                valueAsText = "String";

            param.setParameterValue( valueAsString );

        } else {// normal case
            param.setParameterValue( valueAsInteger );
        }

    }

    /**
     * Commits the filepath input data to the {@link GeoAlgorithm} input parameter.
     * 
     * @param in
     *            Input data as {@link ComplexInput}.
     * @param param
     *            Input parameter of {@link GeoAlgorithm}.
     */
    private void setFilepathInputValue( ProcessletInputs in, Parameter param ) {
        LOG.error( "\"" + param.getParameterTypeName()
                   + "\" a is not supported input parameter type (but is in implementation)" );
        // TODO implement this input parameter type
    }

    /**
     * Commits the boolean input data to the {@link GeoAlgorithm} input parameter.
     * 
     * @param in
     *            Input data as {@link ProcessletInputs}.
     * @param param
     *            Input parameter of {@link GeoAlgorithm}
     */
    private void setBooleanInputValue( ProcessletInputs in, Parameter param ) {
        LiteralInput literalInput = (LiteralInput) in.getParameter( param.getParameterName() );

        String value = literalInput.getValue();

        if ( value.equals( "true" ) || value.equals( "1" ) )
            param.setParameterValue( true );
        else if ( value.equals( "false" ) || value.equals( "0" ) )
            param.setParameterValue( false );
        else {
            // TODO throw Exception? false input value
        }

    }

    /**
     * Commits the string input data to the {@link GeoAlgorithm} input parameter.
     * 
     * @param in
     *            Input data as {@link ProcessletInputs}.
     * @param param
     *            Input parameter of {@link GeoAlgorithm}.
     */
    private void setStringInputValue( ProcessletInputs in, Parameter param ) {
        LiteralInput literalInput = (LiteralInput) in.getParameter( param.getParameterName() );
        param.setParameterValue( literalInput.getValue() );
    }

    /**
     * Commits the multiple input data to the {@link GeoAlgorithm} input parameter.
     * 
     * @param in
     *            Input data as {@link ProcessletInputs}.
     * @param param
     *            Input parameter of {@link GeoAlgorithm}.
     *@param paramSet
     *            Input parameter set of {@link GeoAlgorithm}.
     */
    private void setMultipleInputInputValue( ProcessletInputs in, Parameter param, ParametersSet paramSet ) {
        LOG.error( "Using multiple input input data is not supported." );
    }

    /**
     * Commits the raster layer input data to the {@link GeoAlgorithm} input parameter.
     * 
     * @param in
     *            - Input data as {@link ProcessletInputs}.
     * @param param
     *            - Input parameter of {@link GeoAlgorithm}.
     */
    private void setRasterLayerInputValue( ProcessletInputs in, Parameter param ) {
        LOG.error( "\"" + param.getParameterTypeName()
                   + "\" a is not supported input parameter type (but is in implementation)" );
        // TODO implement this input parameter type
    }

    /**
     * Commits the table field input data to the {@link GeoAlgorithm} input parameter.
     * 
     * @param in
     *            Input data as {@link ProcessletInputs}.
     * @param param
     *            Input parameter of {@link GeoAlgorithm}.
     */
    private void setTableFieldInputValue( ProcessletInputs in, Parameter param ) {
        LOG.error( "\"" + param.getParameterTypeName()
                   + "\" a is not supported input parameter type (but is in implementation)" );
        // TODO implement this input parameter type
    }

    /**
     * Commits the point input data to the {@link GeoAlgorithm} input parameter.
     * 
     * @param in
     *            Input data as {@link ProcessletInputs}.
     * @param param
     *            Input parameter of {@link GeoAlgorithm}.
     */
    private void setPointInputValue( ProcessletInputs in, Parameter param ) {
        LOG.error( "\"" + param.getParameterTypeName()
                   + "\" a is not supported input parameter type (but is in implementation)" );
        // TODO implement this input parameter type
    }

    /**
     * Commits the band input data to the {@link GeoAlgorithm} input parameter.
     * 
     * @param in
     *            Input data as {@link ProcessletInputs}.
     * @param param
     *            Input parameter of {@link GeoAlgorithm}.
     */
    private void setBandInputValue( ProcessletInputs in, Parameter param ) {
        LOG.error( "\"" + param.getParameterTypeName()
                   + "\" a is not supported input parameter type (but is in implementation)" );
        // TODO implement this input parameter type
    }

    /**
     * Commits the table input data to the {@link GeoAlgorithm} input parameter.
     * 
     * @param in
     *            Input data as {@link ProcessletInputs}.
     * @param param
     *            Input parameter of {@link GeoAlgorithm}.
     */
    private void setTableInputValue( ProcessletInputs in, Parameter param ) {
        LOG.error( "\"" + param.getParameterTypeName()
                   + "\" a is not supported input parameter type (but is in implementation)" );
        // TODO implement this input parameter type
    }

    /**
     * Commits the fixed table input data to the {@link GeoAlgorithm} input parameter.
     * 
     * @param in
     *            Input data as {@link ProcessletInputs}.
     * @param param
     *            Input parameter of {@link GeoAlgorithm}.
     */
    private void setFixedTableInputValue( ProcessletInputs in, Parameter param ) {
        LOG.error( "\"" + param.getParameterTypeName()
                   + "\" a is not supported input parameter type (but is in implementation)" );
        // TODO implement this input parameter type
    }

    /**
     * Reads the {@link Geometry} from {@link ComplexInput}.
     * 
     * @param gmlInput
     *            Input data as {@link ComplexInput}.
     * @return {@link Geometry}
     * @throws ProcessletException
     */
    private Geometry readGeometry( ComplexInput gmlInput )
                            throws ProcessletException {
        try {

            XMLStreamReader xmlReader = gmlInput.getValueAsXMLStream();
            GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(
                                                                               FormatHelper.determineGMLVersion( gmlInput ),
                                                                               xmlReader );

            return gmlReader.readGeometry();
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new ProcessletException( "Error parsing parameter " + gmlInput.getIdentifier() + ": "
                                           + e.getMessage() );
        }
    }

    /**
     * Reads the {@link FeatureCollection} from {@link ComplexInput}.
     * 
     * @param gmlInput
     *            {@link ComplexInput}
     * @return feature collection
     * @throws ProcessletException
     */
    private FeatureCollection readFeatureCollection( ComplexInput gmlInput )
                            throws ProcessletException {
        try {

            XMLStreamReader xmlReader = gmlInput.getValueAsXMLStream();

            GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(
                                                                               FormatHelper.determineGMLVersion( gmlInput ),
                                                                               xmlReader );

            return gmlReader.readFeatureCollection();
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new ProcessletException( "Error parsing parameter " + gmlInput.getIdentifier() + ": "
                                           + e.getMessage() );
        }
    }

    /**
     * Writes the output data (all supported types).
     * 
     * @param alg
     *            SEXTANTE {@link GeoAlgorithm}
     * @param out
     *            {@link ProcessletOutputs}
     * 
     * @throws WrongOutputIDException
     * @throws ProcessletException
     * @throws IteratorException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     */
    private void writeResult( GeoAlgorithm alg, ProcessletOutputs out )
                            throws WrongOutputIDException, ProcessletException, IteratorException,
                            IllegalArgumentException, InstantiationException, IllegalAccessException {

        OutputObjectsSet outputs = alg.getOutputObjects();

        // traverses the output parameters
        for ( int i = 0; i < outputs.getOutputObjectsCount(); i++ ) {

            // output parameter
            Output param = outputs.getOutput( i );

            // write the correct output type
            String paramTypeDesc = param.getTypeDescription();
            if ( paramTypeDesc.equals( SextanteWPSProcess.VECTOR_LAYER_OUTPUT ) )
                writeVectorLayer( param, out );
            else if ( paramTypeDesc.equals( SextanteWPSProcess.RASTER_LAYER_OUTPUT ) )
                writeRasterLayer( param, out );
            else if ( paramTypeDesc.equals( SextanteWPSProcess.TABLE_OUTPUT ) )
                writeTable( param, out );
            else if ( paramTypeDesc.equals( SextanteWPSProcess.TEXT_OUTPUT ) )
                writeText( param, out );
            else if ( paramTypeDesc.equals( SextanteWPSProcess.CHART_OUTPUT ) )
                writeChart( param, out );
            else {
                LOG.error( "\"" + paramTypeDesc + "\" is a not supported output parameter type." );
                // TODO throw exception
            }
        }

    }

    /**
     * Writes an {@link IVectorLayer}.
     * 
     * @param obj
     *            - {@link IVectorLayer} as {@link Output}
     * @param out
     *            - {@link ProcessletOutputs}
     * @throws IteratorException
     * @throws ProcessletException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     */
    private void writeVectorLayer( Output obj, ProcessletOutputs out )
                            throws IteratorException, ProcessletException, IllegalArgumentException,
                            InstantiationException, IllegalAccessException {
        // output object
        IVectorLayer result = (IVectorLayer) obj.getOutputObject();

        ComplexOutput gmlOutput = (ComplexOutput) out.getParameter( obj.getName() );

        // feature collection output
        if ( FormatHelper.determineGMLType( gmlOutput ).equals( GMLType.FEATURE_COLLECTION ) ) {
            FeatureCollection fc = VectorLayerAdapter.createFeatureCollection( result );
            writeFeatureCollection( gmlOutput, fc );
        } else {

            // geometry output
            if ( FormatHelper.determineGMLType( gmlOutput ).equals( GMLType.GEOMETRY ) ) {
                Geometry g = VectorLayerAdapter.createGeometry( result );

                if ( g != null ) {
                    writeGeometry( gmlOutput, g );
                } else {

                    LOG.warn( "The " + gmlOutput.getIdentifier().getCode() + " is an empty collection." );
                }

            } else {// unknown GML type
                LOG.error( "Type (GEOMETRY OR FEATURE COLLECTION) of schema \"" + gmlOutput.getRequestedSchema()
                           + "\" is unknown." );
                // TODO throw exception
            }
        }
    }

    /**
     * Writes an {@link IRasterLayer}.
     * 
     * @param obj
     *            - {@link IRasterLayer} as {@link Output}.
     * @param out
     *            - {@link ProcessletOutputs}
     */
    private void writeRasterLayer( Output obj, ProcessletOutputs out ) {
        LOG.error( "Writing of \"" + obj.getTypeDescription() + "\" is not supported (but is in implementation)" );
        // TODO implement this output parameter type

    }

    /**
     * Writes an {@link ITable}.
     * 
     * @param obj
     *            - {@link ITable} as {@link Output}.
     * @param out
     *            - {@link ProcessletOutputs}
     */
    private void writeTable( Output obj, ProcessletOutputs out ) {
        LOG.error( "Writing of \"" + obj.getTypeDescription() + "\" is not supported (but is in implementation)" );
        // TODO implement this output parameter type
    }

    /**
     * Writes a text.
     * 
     * @param obj
     *            - text as {@link Output}.
     * @param out
     *            - {@link ProcessletOutputs}
     */
    private void writeText( Output obj, ProcessletOutputs out ) {
        LOG.error( "Writing of \"" + obj.getTypeDescription() + "\" is not supported (but is in implementation)" );
        // TODO implement this output parameter type

    }

    /**
     * Writes a chart.
     * 
     * @param obj
     *            - chart as {@link Output}.
     * @param out
     *            - {@link ProcessletOutputs}
     */
    private void writeChart( Output obj, ProcessletOutputs out ) {
        LOG.error( "Writing of \"" + obj.getTypeDescription() + "\" is not supported (but is in implementation)" );
        // TODO implement this output parameter type
    }

    /**
     * Writes a {@link Geometry}.
     * 
     * @param gmlOutput
     *            - {@link ComplexOutput}
     * @param geometry
     *            - {@link Geometry}
     * @throws ProcessletException
     */
    private void writeGeometry( ComplexOutput gmlOutput, Geometry geometry )
                            throws ProcessletException {
        try {

            String gmlSchema = gmlOutput.getRequestedSchema();
            XMLStreamWriterWrapper sw = new XMLStreamWriterWrapper( gmlOutput.getXMLStreamWriter(),
                                                                    "http://www.opengis.net/gml " + gmlSchema );
            sw.setPrefix( "gml", GMLNS );
            GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter(
                                                                                FormatHelper.determineGMLVersion( gmlOutput ),
                                                                                sw );

            gmlWriter.write( geometry );

        } catch ( Exception e ) {
            throw new ProcessletException( "Error exporting geometry: " + e.getMessage() );
        }
    }

    /**
     * Writes a {@link FeatureCollection}.
     * 
     * @param gmlOutput
     *            - {@link ComplexOutput}
     * @param coll
     *            - {@link FeatureCollection}
     * 
     * @throws ProcessletException
     */
    private void writeFeatureCollection( ComplexOutput gmlOutput, FeatureCollection coll )
                            throws ProcessletException {
        try {

            XMLStreamWriter sw = gmlOutput.getXMLStreamWriter();
//            sw.setPrefix( "gml", CommonNamespaces.GML3_2_NS );
            GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter(
                                                                                FormatHelper.determineGMLVersion( gmlOutput ),
                                                                                sw );

            gmlWriter.write( coll );

        } catch ( Exception e ) {
            throw new ProcessletException( "Error exporting geometry: " + e.getMessage() );
        }
    }
}
