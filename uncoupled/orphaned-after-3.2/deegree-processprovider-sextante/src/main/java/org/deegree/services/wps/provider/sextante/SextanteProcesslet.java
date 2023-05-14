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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.xml.stax.SchemaLocationXMLStreamWriter;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
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
import es.unex.sextante.exceptions.WrongParameterTypeException;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.Parameter;

/**
 * This class presents a {@link Processlet} with a SEXTANTE {@link GeoAlgorithm} Therefore this class must
 * differentiates all input and output parameters of a SEXTANTE {@link GeoAlgorithm}.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * 
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

            SextanteFeatureCollectionStreamer sfcs = new SextanteFeatureCollectionStreamer( alg, in, out );

            if ( sfcs.containFeatureCollectionInput() && SextanteFeatureCollectionStreamer.ENABLED ) { // feature
                                                                                                       // collection =
                                                                                                       // streaming
                LOG.info( "STREAMING" );
                sfcs.execute();
            } else { // no feature collection = no streaming

                LOG.info( "NO STREAMING" );

                // sets all input values
                setInputValues( alg, in );

                // LOG.info( "GET OUTPUT PARAMETERS" );

                // execute the algorithm
                alg.execute( null, new OutputFactoryExt() );

                // write all output values
                writeResult( alg, out );
            }
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
            throw new ProcessletException( e.getMessage() );
        }

    }

    /**
     * This method determines all namespaces of a {@link Feature}. If the {@link Feature} is a {@link FeatureCollection}
     * , the first {@link Feature} of the {@link FeatureCollection} is used to determine the namespaces.
     * 
     * @param f
     *            {@link Feature}.
     * 
     * @return {@link HashMap} of namespaces. The key is the prefix and the value the namespace URI.
     */
    static HashMap<String, String> determinePropertyNamespaces( Feature f ) {

        Feature propertyTypeFeature = f;

        if ( f instanceof FeatureCollection ) {
            FeatureCollection fc = (FeatureCollection) f;
            Iterator<Feature> it = fc.iterator();
            if ( it.hasNext() ) {
                propertyTypeFeature = it.next();
            }
        }

        HashMap<String, String> namespaces = new HashMap<String, String>();

        List<Property> props = propertyTypeFeature.getProperties();
        for ( int i = 0; i < props.size(); i++ ) {
            QName name = props.get( i ).getName();
            namespaces.put( name.getNamespaceURI(), name.getPrefix() );
        }

        return namespaces;
    }

    /**
     * Commits the input data (all supported types) to the {@link GeoAlgorithm} input parameter.
     * 
     * @param alg
     *            SEXTANTE {@link GeoAlgorithm}.
     * @param in
     *            Input data as {@link ProcessletInputs}.
     * @throws ProcessletException
     * @throws ClassNotFoundException
     * @throws NullParameterValueException
     * @throws WrongParameterTypeException
     */
    static void setInputValues( GeoAlgorithm alg, ProcessletInputs in )
                            throws WrongParameterTypeException, NullParameterValueException, ClassNotFoundException,
                            ProcessletException {
        setInputValues( alg, in, null );
    }

    /**
     * Commits the input data (all supported types) to the {@link GeoAlgorithm} input parameter.
     * 
     * @param alg
     *            SEXTANTE {@link GeoAlgorithm}.
     * @param in
     *            Input data as {@link ProcessletInputs}.
     * @param paramIndexes
     *            Indexes of input parameters for commit, can be null.
     * 
     * @throws ClassNotFoundException
     * @throws NullParameterValueException
     * @throws WrongParameterTypeException
     * @throws ProcessletException
     */
    static void setInputValues( GeoAlgorithm alg, ProcessletInputs in, List<Integer> paramIndexes )
                            throws ClassNotFoundException, WrongParameterTypeException, NullParameterValueException,
                            ProcessletException {

        // input parameters
        ParametersSet paramSet = alg.getParameters();

        // set all parameters
        if ( paramIndexes == null ) {
            paramIndexes = new LinkedList<Integer>();
            for ( int i = 0; i < paramSet.getNumberOfParameters(); i++ ) {
                paramIndexes.add( i );
            }
        }

        // traverses the input parameters
        for ( Integer i : paramIndexes ) {

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
                setStringInputValue( in, param, alg );
            else if ( paramTypeName.equals( SextanteWPSProcess.MULTIPLE_INPUT_INPUT ) )
                setMultipleInputInputValue( in, param, alg );
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
                String message = "'" + paramTypeName + "' is a not supported input parameter type.";
                throw new IllegalArgumentException( message );
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
     * @throws UnknownParameterException
     * @throws ProcessletException
     */
    private static void setVectorLayerInputValue( ProcessletInputs in, Parameter param )
                            throws ClassNotFoundException, ProcessletException {
        // input object
        ComplexInput gmlInput = (ComplexInput) in.getParameter( param.getParameterName() );

        // create vector layer
        IVectorLayer layer = null;

        GMLType gmlType = FormatHelper.determineGMLType( gmlInput );

        // feature collection input
        if ( gmlType.equals( GMLType.FEATURE_COLLECTION ) ) {
            FeatureCollection coll = readFeatureCollection( gmlInput );
            layer = VectorLayerAdapter.createVectorLayer( coll );
        } else {

            // geometry input
            if ( gmlType.equals( GMLType.GEOMETRY ) ) {
                Geometry geometry = readGeometry( gmlInput );
                layer = VectorLayerAdapter.createVectorLayer( geometry );

            } else {// unknown GMLType
                String message = "The GMLType '" + gmlType.name() + "' of schema '" + gmlInput.getSchema()
                                 + "' is unknown.";
                throw new IllegalArgumentException( message );
            }
        }

        if ( layer != null ) {
            // set vector layer
            param.setParameterValue( layer );
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
    private static void setNumericalValueInputValue( ProcessletInputs in, Parameter param ) {
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
    private static void setSelectionInputValue( ProcessletInputs in, Parameter param, GeoAlgorithm alg ) {
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
    private static void setFilepathInputValue( ProcessletInputs in, Parameter param ) {
        LOG.error( "'" + param.getParameterTypeName()
                   + "' a is not supported input parameter type (but is in implementation)" );
        // TODO implement this input parameter type
    }

    /**
     * Commits the boolean input data to the {@link GeoAlgorithm} input parameter.
     * 
     * @param in
     *            Input data as {@link ProcessletInputs}.
     * @param param
     *            Input parameter of {@link GeoAlgorithm}
     * @throws ProcessletException
     */
    private static void setBooleanInputValue( ProcessletInputs in, Parameter param )
                            throws ProcessletException {
        LiteralInput literalInput = (LiteralInput) in.getParameter( param.getParameterName() );

        String value = literalInput.getValue();

        if ( value.equals( "true" ) || value.equals( "1" ) )
            param.setParameterValue( true );
        else if ( value.equals( "false" ) || value.equals( "0" ) )
            param.setParameterValue( false );
        else {
            String message = "Can't identify the value '" + value + "' to true or false.";
            throw new ProcessletException( message );
        }

    }

    /**
     * Commits the string input data to the {@link GeoAlgorithm} input parameter.
     * 
     * @param in
     *            Input data as {@link ProcessletInputs}.
     * @param param
     *            Input parameter of {@link GeoAlgorithm}.
     * @throws NullParameterValueException
     * @throws WrongParameterTypeException
     */
    private static void setStringInputValue( ProcessletInputs in, Parameter param, GeoAlgorithm alg )
                            throws WrongParameterTypeException, NullParameterValueException {
        LiteralInput literalInput = (LiteralInput) in.getParameter( param.getParameterName() );

        String stringInput = literalInput.getValue();

        // special case of vectorcluster algorithm
        if ( alg.getCommandLineName().equals( "vectorcluster" ) ) {

            // determine input vector layer if available
            IVectorLayer layer = null;
            ParametersSet params = alg.getParameters();
            for ( int i = 0; i < params.getNumberOfParameters(); i++ ) {
                Parameter p = params.getParameter( i );
                if ( p.getParameterTypeName().equals( SextanteWPSProcess.VECTOR_LAYER_INPUT ) ) {
                    layer = p.getParameterValueAsVectorLayer();
                    break;
                }
            }

            // determine input strings in format of getNameWithNamespaceAndPrefix() method
            String[] fNamesInput = stringInput.split( "," );
            LinkedList<String> newNames = new LinkedList<String>();
            if ( layer != null ) {
                if ( layer instanceof VectorLayerImpl ) {
                    Field[] fields = ( (VectorLayerImpl) layer ).getFields();
                    for ( int i = 0; i < fields.length; i++ ) {
                        String fNameLayer = fields[i].getQName().getLocalPart();
                        for ( int j = 0; j < fNamesInput.length; j++ ) {
                            if ( fNameLayer.equals( fNamesInput[i] ) ) {
                                newNames.add( fields[i].getNameWithNamespaceAndPrefix() );
                                break;
                            }
                        }
                    }
                }
            }

            // modify input string
            stringInput = "";
            Iterator<String> itNewNames = newNames.iterator();
            if ( itNewNames.hasNext() ) {
                stringInput += itNewNames.next();
            }
            while ( itNewNames.hasNext() ) {
                stringInput += "," + itNewNames.next();
            }
        }

        param.setParameterValue( stringInput );
    }

    /**
     * Commits the multiple input data to the {@link GeoAlgorithm} input parameter.
     * 
     * @param in
     *            Input data as {@link ProcessletInputs}.
     * @param param
     *            Input parameter of {@link GeoAlgorithm}.
     * @param paramSet
     *            Input parameter set of {@link GeoAlgorithm}.
     */
    private static void setMultipleInputInputValue( ProcessletInputs in, Parameter param, GeoAlgorithm paramSet ) {
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
    private static void setRasterLayerInputValue( ProcessletInputs in, Parameter param ) {
        LOG.error( "'" + param.getParameterTypeName()
                   + "' a is not supported input parameter type (but is in implementation)" );
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
    private static void setTableFieldInputValue( ProcessletInputs in, Parameter param ) {
        LOG.error( "'" + param.getParameterTypeName()
                   + "' a is not supported input parameter type (but is in implementation)" );
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
    private static void setPointInputValue( ProcessletInputs in, Parameter param ) {
        LOG.error( "'" + param.getParameterTypeName()
                   + "' a is not supported input parameter type (but is in implementation)" );
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
    private static void setBandInputValue( ProcessletInputs in, Parameter param ) {
        LOG.error( "'" + param.getParameterTypeName()
                   + "' a is not supported input parameter type (but is in implementation)" );
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
    private static void setTableInputValue( ProcessletInputs in, Parameter param ) {
        LOG.error( "'" + param.getParameterTypeName()
                   + "' a is not supported input parameter type (but is in implementation)" );
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
    private static void setFixedTableInputValue( ProcessletInputs in, Parameter param ) {
        LOG.error( "'" + param.getParameterTypeName()
                   + "' a is not supported input parameter type (but is in implementation)" );
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
    private static Geometry readGeometry( ComplexInput gmlInput )
                            throws ProcessletException {
        try {

            XMLStreamReader xmlReader = gmlInput.getValueAsXMLStream();
            GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( FormatHelper.determineGMLVersion( gmlInput ),
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
    private static FeatureCollection readFeatureCollection( ComplexInput gmlInput )
                            throws ProcessletException {
        try {

            XMLStreamReader xmlReader = gmlInput.getValueAsXMLStream();

            GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( FormatHelper.determineGMLVersion( gmlInput ),
                                                                               xmlReader );

            FeatureCollection fc = gmlReader.readFeatureCollection();

            return fc;
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new ProcessletException( "Error parsing parameter " + gmlInput.getIdentifier() + ": "
                                           + e.getMessage() );
        }
    }

    static void writeResult( GeoAlgorithm alg, ProcessletOutputs out )
                            throws WrongOutputIDException, IteratorException, IllegalArgumentException,
                            ProcessletException, InstantiationException, IllegalAccessException {
        writeResult( alg, out, null );
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
     */
    static void writeResult( GeoAlgorithm alg, ProcessletOutputs out, List<Integer> paramIndexes )
                            throws WrongOutputIDException, ProcessletException, IteratorException,
                            IllegalArgumentException, InstantiationException, IllegalAccessException {

        OutputObjectsSet outputs = alg.getOutputObjects();

        // write all parameters
        if ( paramIndexes == null ) {
            paramIndexes = new LinkedList<Integer>();
            for ( int i = 0; i < outputs.getOutputObjectsCount(); i++ ) {
                paramIndexes.add( i );
            }
        }

        // traverses the output parameters
        for ( Integer i : paramIndexes ) {

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
                String message = "'" + paramTypeDesc + "' is a not supported output parameter type.";
                throw new IllegalArgumentException( message );
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
    private static void writeVectorLayer( Output obj, ProcessletOutputs out )
                            throws IteratorException, ProcessletException, IllegalArgumentException,
                            InstantiationException, IllegalAccessException {
        // output object
        IVectorLayer result = (IVectorLayer) obj.getOutputObject();

        ComplexOutput gmlOutput = (ComplexOutput) out.getParameter( obj.getName() );

        GMLType gmlType = FormatHelper.determineGMLType( gmlOutput );

        // feature collection output
        if ( gmlType.equals( GMLType.FEATURE_COLLECTION ) ) {
            FeatureCollection fc = VectorLayerAdapter.createFeatureCollection( result );
            writeFeatureCollection( gmlOutput, fc );
        } else {

            // geometry output
            if ( gmlType.equals( GMLType.GEOMETRY ) ) {
                Geometry g = VectorLayerAdapter.createGeometry( result );

                if ( g != null ) {
                    writeGeometry( gmlOutput, g );
                } else {
                    LOG.warn( "The " + gmlOutput.getIdentifier().getCode() + " is an empty collection." );
                }

            } else {// unknown GML type
                String message = "The GMLType '" + gmlType.name() + "' of schema '" + gmlOutput.getRequestedSchema()
                                 + "' is unknown.";
                throw new ProcessletException( message );
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
    private static void writeRasterLayer( Output obj, ProcessletOutputs out ) {
        LOG.error( "Writing of '" + obj.getTypeDescription() + "' is not supported (but is in implementation)" );
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
    private static void writeTable( Output obj, ProcessletOutputs out ) {
        LOG.error( "Writing of '" + obj.getTypeDescription() + "' is not supported (but is in implementation)" );
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
    private static void writeText( Output obj, ProcessletOutputs out ) {
        LOG.error( "Writing of '" + obj.getTypeDescription() + "' is not supported (but is in implementation)" );
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
    private static void writeChart( Output obj, ProcessletOutputs out ) {
        LOG.error( "Writing of '" + obj.getTypeDescription() + "' is not supported (but is in implementation)" );
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
    private static void writeGeometry( ComplexOutput gmlOutput, Geometry geometry )
                            throws ProcessletException {
        try {

            String gmlSchema = gmlOutput.getRequestedSchema();

            GMLVersion gmlVersion = FormatHelper.determineGMLVersion( gmlOutput );

            String schemaPrefix;
            if ( gmlVersion.equals( GMLVersion.GML_32 ) )
                schemaPrefix = "http://www.opengis.net/gml/3.2 ";
            else
                schemaPrefix = "http://www.opengis.net/gml ";

            SchemaLocationXMLStreamWriter sw = new SchemaLocationXMLStreamWriter( gmlOutput.getXMLStreamWriter(),
                                                                                  schemaPrefix + gmlSchema );
            sw.setPrefix( "gml", GMLNS );
            GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter( gmlVersion, sw );

            // gmlWriter.setOutputCRS(new CRS( "EPSG:4326" ));

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
    public static void writeFeatureCollection( ComplexOutput gmlOutput, Feature coll )
                            throws ProcessletException {
        try {

            XMLStreamWriter sw = gmlOutput.getXMLStreamWriter();

            // determine and set namespaces
            HashMap<String, String> namespaces = determinePropertyNamespaces( coll );
            Set<String> namespaceURIs = namespaces.keySet();
            for ( String uri : namespaceURIs ) {
                sw.setPrefix( namespaces.get( uri ), uri );
            }

            // sw.setPrefix( VectorLayerAdapter.APP_PREFIX, VectorLayerAdapter.APP_NS );
            // sw.setPrefix( "gml", CommonNamespaces.GML3_2_NS );
            GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter( FormatHelper.determineGMLVersion( gmlOutput ),
                                                                                sw );
            // gmlWriter.setOutputCRS(new CRS( "EPSG:4326" ));
            gmlWriter.write( coll );

        } catch ( Exception e ) {
            throw new ProcessletException( "Error exporting geometry: " + e.getMessage() );
        }
    }
}
