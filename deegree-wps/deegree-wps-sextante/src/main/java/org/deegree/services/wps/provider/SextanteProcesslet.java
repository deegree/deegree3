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
package org.deegree.services.wps.provider;

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import javax.xml.stream.XMLStreamReader;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.cs.CRS;
import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
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
import org.deegree.services.wps.output.ComplexOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.IteratorException;
import es.unex.sextante.exceptions.WrongOutputIDException;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.StreamOutputChannel;
import es.unex.sextante.parameters.Parameter;

/**
 * Presents a processlet based on a SEXTANTE-Algorithm.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class SextanteProcesslet implements Processlet {

    // logger
    private static final Logger LOG = LoggerFactory.getLogger( SextanteProcesslet.class );

    // SEXTANTE-Algorithm
    private final GeoAlgorithm alg;

    SextanteProcesslet( GeoAlgorithm alg ) {
        this.alg = alg;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init() {
    }

    @Override
    public void process( ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info )
                            throws ProcessletException {
        try {

            // sets all input values
            setInputValues( alg, in );

            // sets output parameters (here only the output channel)
            setOutputParameters( alg );

            // execute the algorithm
            alg.execute( null, new OutputFactoryImpl() );

            // write all output values
            writeResult( alg, out );

        } catch ( WrongOutputIDException e ) {
            e.printStackTrace();
            throw new ProcessletException( e.getLocalizedMessage() );
        } catch ( GeoAlgorithmExecutionException e ) {
            e.printStackTrace();
            throw new ProcessletException( e.getLocalizedMessage() );
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
            throw new ProcessletException( e.getLocalizedMessage() );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

    /**
     * Detects the GMLVersion on the base of the schema URL.
     * 
     * It should be used for input and output data.
     * 
     * @param schema
     *            - schema URL
     * @return
     */
    private GMLVersion getGMLVersion( String schema ) {
        GMLVersion gmlVersion = null;

        // TODO CORRECT THE REDUNDANZ
        if ( schema.equals( "http://schemas.opengis.net/gml/2.1.2/geometry.xsd" ) )
            gmlVersion = GMLVersion.GML_2;
        else if ( schema.equals( "http://schemas.opengis.net/gml/3.0.1/base/geometryComplexes.xsd" ) )
            gmlVersion = GMLVersion.GML_30;
        else if ( schema.equals( "http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd" ) )
            gmlVersion = GMLVersion.GML_31;
        else if ( schema.equals( "http://schemas.opengis.net/gml/3.2.1/geometryComplexes.xsd" ) )
            gmlVersion = GMLVersion.GML_32;
        else if ( schema.equals( "http://schemas.opengis.net/gml/3.1.1/base/feature.xsd" ) )
            gmlVersion = GMLVersion.GML_31;
        return gmlVersion;
    }

    /**
     * Passes the input data (all supported types) to the algorithm parameter.
     * 
     * @param alg
     *            - algorithm
     * @param in
     *            - input data
     * @throws ProcessletException
     * @throws ClassNotFoundException
     */
    private void setInputValues( GeoAlgorithm alg, ProcessletInputs in )
                            throws ProcessletException, ClassNotFoundException {

        SextanteProcessProvider.logAlgorithm( alg );

        // input parameters
        LOG.info( "SET INTPUT PARAMETERS" );
        ParametersSet paramSet = alg.getParameters();

        // traverses the input parameters
        for ( int i = 0; i < paramSet.getNumberOfParameters(); i++ ) {
            Parameter param = paramSet.getParameter( i );

            // TODO CORRECT THE REDUNDANZ
            // set the correct input parameter value
            if ( param.getParameterTypeName().equals( "Vector Layer" ) )
                setVectorLayerInputValue( in, param );
            else if ( param.getParameterTypeName().equals( "Numerical Value" ) )
                setNumericalValueInputValue( in, param );
            else if ( param.getParameterTypeName().equals( "Selection" ) )
                setSelectionInputValue( in, param );
            else if ( param.getParameterTypeName().equals( "Filepath" ) )
                setFilepathInputValue( in, param );
            else if ( param.getParameterTypeName().equals( "Boolean" ) )
                setBooleanInputValue( in, param );
            else if ( param.getParameterTypeName().equals( "String" ) )
                setStringInputValue( in, param );
            else if ( param.getParameterTypeName().equals( "Multiple Input" ) )
                setMultipleInputInputValue( in, param, alg.getParameters() );
            else if ( param.getParameterTypeName().equals( "Raster Layer" ) )
                setRasterLayerInputValue( in, param );
            else if ( param.getParameterTypeName().equals( "Table Field" ) )
                setTableFieldInputValue( in, param );
            else if ( param.getParameterTypeName().equals( "Point" ) )
                setPointInputValue( in, param );
            else if ( param.getParameterTypeName().equals( "Band" ) )
                setBandInputValue( in, param );
            else if ( param.getParameterTypeName().equals( "Table" ) )
                setTableInputValue( in, param );
            else if ( param.getParameterTypeName().equals( "Fixed Table" ) )
                setFixedTableInputValue( in, param );
            else
                LOG.warn( "InputParameters: \"" + param.getParameterTypeName() + "\" is a unknown input type." );
        }
    }

    /**
     * Passes the vector layer input data to the algorithm parameter.
     * 
     * @param in
     *            - input data
     * @param param
     *            - algorithm parameter
     * @throws ClassNotFoundException
     */
    private void setVectorLayerInputValue( ProcessletInputs in, Parameter param )
                            throws ProcessletException, ClassNotFoundException {
        // input object
        ComplexInput gmlInput = (ComplexInput) in.getParameter( param.getParameterName() );

        // create vector layer
        IVectorLayer layer;

        // input
        if ( gmlInput.getSchema().equals( "http://schemas.opengis.net/gml/3.1.1/base/feature.xsd" ) ) {
            FeatureCollection coll = readFeatureCollection( gmlInput );
            layer = IVectorLayerAdapter.createVectorLayer( coll );
        } else {
            Geometry geometry = readGeometry( gmlInput );
            layer = IVectorLayerAdapter.createVectorLayer( geometry );
        }

        // set vector layer
        param.setParameterValue( layer );

    }

    /**
     * Passes the numerical input data to the algorithm parameter.
     * 
     * @param in
     *            - input data
     * @param param
     *            - algorithm parameter
     */
    private void setNumericalValueInputValue( ProcessletInputs in, Parameter param ) {
        LOG.warn( "Using numerical value input data is not supported." );
    }

    /**
     * Passes the selection input data to the algorithm parameter.
     * 
     * @param in
     *            - input data
     * @param param
     *            - algorithm parameter
     */
    private void setSelectionInputValue( ProcessletInputs in, Parameter param ) {
        LOG.warn( "Using selection input data is not supported." );
    }

    /**
     * Passes the filepath input data to the algorithm parameter.
     * 
     * @param in
     *            - input data
     * @param param
     *            - algorithm parameter
     */
    private void setFilepathInputValue( ProcessletInputs in, Parameter param ) {
        LOG.warn( "Using filepath input data is not supported." );
    }

    /**
     * Passes the boolean input data to the algorithm parameter.
     * 
     * @param in
     *            - input data
     * @param param
     *            - algorithm parameter
     */
    private void setBooleanInputValue( ProcessletInputs in, Parameter param ) {
        LOG.warn( "Using boolean input data is not supported." );
    }

    /**
     * Passes the string input data to the algorithm parameter.
     * 
     * @param in
     *            - input data
     * @param param
     *            - algorithm parameter
     */
    private void setStringInputValue( ProcessletInputs in, Parameter param ) {
        LOG.warn( "Using string input data is not supported." );
    }

    /**
     * Passes the multiple input data to the algorithm parameter.
     * 
     * @param in
     *            - input data
     * @param param
     *            - algorithm parameter
     *@param paramSet
     *            - all algorithm parameters
     */
    private void setMultipleInputInputValue( ProcessletInputs in, Parameter param, ParametersSet paramSet ) {
        LOG.warn( "Using multiple input input data is not supported." );
    }

    /**
     * Passes the raster layer input data to the algorithm parameter.
     * 
     * @param in
     *            - input data
     * @param param
     *            - algorithm parameter
     */
    private void setRasterLayerInputValue( ProcessletInputs in, Parameter param ) {
        LOG.warn( "Using raster layer input data is not supported." );
    }

    /**
     * Passes the table field input data to the algorithm parameter.
     * 
     * @param in
     *            - input data
     * @param param
     *            - algorithm parameter
     */
    private void setTableFieldInputValue( ProcessletInputs in, Parameter param ) {
        LOG.warn( "Using table field input data is not supported." );
    }

    /**
     * Passes the point input data to the algorithm parameter.
     * 
     * @param in
     *            - input data
     * @param param
     *            - algorithm parameter
     */
    private void setPointInputValue( ProcessletInputs in, Parameter param ) {
        LOG.warn( "Using point input data is not supported." );
    }

    /**
     * Passes the band input data to the algorithm parameter.
     * 
     * @param in
     *            - input data
     * @param param
     *            - algorithm parameter
     */
    private void setBandInputValue( ProcessletInputs in, Parameter param ) {
        LOG.warn( "Using band input data is not supported." );
    }

    /**
     * Passes the table input data to the algorithm parameter.
     * 
     * @param in
     *            - input data
     * @param param
     *            - algorithm parameter
     */
    private void setTableInputValue( ProcessletInputs in, Parameter param ) {
        LOG.warn( "Using table input data is not supported." );
    }

    /**
     * Passes the fixed table input data to the algorithm parameter.
     * 
     * @param in
     *            - input data
     * @param param
     *            - algorithm parameter
     */
    private void setFixedTableInputValue( ProcessletInputs in, Parameter param ) {
        LOG.warn( "Using fixed table input data is not supported." );
    }

    private void setOutputParameters( GeoAlgorithm alg )
                            throws WrongOutputIDException {

        LOG.info( "SET OUTPUT PARAMETERS" );

        // output parameter of algorithm
        OutputObjectsSet outputs = alg.getOutputObjects();

        StreamOutputChannel outCh = new StreamOutputChannel();
        for ( int i = 0; i < outputs.getOutputObjectsCount(); i++ ) {
            outputs.getOutput( i ).setOutputChannel( outCh );
        }

    }

    /**
     * Reads the geometry from input (GML).
     * 
     * @param gmlInput
     *            - input data
     * @return org.deegree.geometry.Geometry
     * @throws ProcessletException
     */
    private Geometry readGeometry( ComplexInput gmlInput )
                            throws ProcessletException {
        try {
            String gmlSchema = gmlInput.getSchema();

            XMLStreamReader xmlReader = gmlInput.getValueAsXMLStream();
            GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( getGMLVersion( gmlSchema ), xmlReader );

            return gmlReader.readGeometry();
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new ProcessletException( "Error parsing parameter " + gmlInput.getIdentifier() + ": "
                                           + e.getMessage() );
        }
    }

    /**
     * Reads the feature collection from input (GML).
     * 
     * @param gmlInput
     *            - input data
     * @return feature collection
     * @throws ProcessletException
     */
    private FeatureCollection readFeatureCollection( ComplexInput gmlInput )
                            throws ProcessletException {
        try {
            String gmlSchema = gmlInput.getSchema();

            XMLStreamReader xmlReader = gmlInput.getValueAsXMLStream();
            GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( getGMLVersion( gmlSchema ), xmlReader );

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
     *            - SEXTANTE-Algorithm
     * @param out
     *            - output channel
     * @throws WrongOutputIDException
     * @throws ProcessletException
     * @throws IteratorException
     */
    private void writeResult( GeoAlgorithm alg, ProcessletOutputs out )
                            throws WrongOutputIDException, ProcessletException, IteratorException {

        OutputObjectsSet outputs = alg.getOutputObjects();

        // traverses the output parameters
        for ( int i = 0; i < outputs.getOutputObjectsCount(); i++ ) {

            // output parameter
            Output result = outputs.getOutput( i );

            // TODO CORRECT THE REDUNDANZ
            // write the correct output type
            if ( result.getTypeDescription().equals( "vector" ) )
                writeVectorLayer( result, out );
            else if ( result.getTypeDescription().equals( "raster" ) )
                writeRasterLayer( result, out );
            else if ( result.getTypeDescription().equals( "table" ) )
                writeTable( result, out );
            else if ( result.getTypeDescription().equals( "text" ) )
                writeText( result, out );
            else if ( result.getTypeDescription().equals( "chart" ) )
                writeChart( result, out );
            else
                LOG.warn( "OutputParameters: \"" + result.getTypeDescription() + "\" is a unknown output type." );
        }

    }

    /**
     * Writes a vector layer.
     * 
     * @param obj
     *            - vector layer
     * @param out
     *            - output channel
     * @throws IteratorException
     * @throws ProcessletException
     */
    private void writeVectorLayer( Output obj, ProcessletOutputs out )
                            throws IteratorException, ProcessletException {
        // output object
        VectorLayerImpl result = (VectorLayerImpl) obj.getOutputObject();

        // write geometry
        ComplexOutput centroidOutput = (ComplexOutput) out.getParameter( obj.getName() );
        Geometry g = IVectorLayerAdapter.createGeometry( result );
        writeGeometry( centroidOutput, g );
    }

    /**
     * Writes a raster layer.
     * 
     * @param obj
     *            - raster layer
     * @param out
     *            - output channel
     */
    private void writeRasterLayer( Output obj, ProcessletOutputs out ) {
        LOG.warn( "Writing of raster layer is not supported." );
    }

    /**
     * Writes a table.
     * 
     * @param obj
     *            - table
     * @param out
     *            - output channel
     */
    private void writeTable( Output obj, ProcessletOutputs out ) {
        LOG.warn( "Writing of table is not supported." );
    }

    /**
     * Writes a text.
     * 
     * @param obj
     *            - text
     * @param out
     *            - output channel
     */
    private void writeText( Output obj, ProcessletOutputs out ) {
        LOG.warn( "Writing of text is not supported." );
    }

    /**
     * Writes a chart.
     * 
     * @param obj
     *            - chart
     * @param out
     *            - output channel
     */
    private void writeChart( Output obj, ProcessletOutputs out ) {
        LOG.warn( "Writing of chart is not supported." );
    }

    /**
     * Writes a org.deegree.geometry.Geometry.
     * 
     * @param gmlOutput
     *            - output channel
     * @param geometry
     *            - geometry
     * @throws ProcessletException
     */
    private void writeGeometry( ComplexOutput gmlOutput, Geometry geometry )
                            throws ProcessletException {
        try {

            String gmlSchema = gmlOutput.getRequestedSchema();
            XMLStreamWriterWrapper sw = new XMLStreamWriterWrapper( gmlOutput.getXMLStreamWriter(),
                                                                    "http://www.opengis.net/gml " + gmlSchema );
            sw.setPrefix( "gml", GMLNS );
            GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter( getGMLVersion( gmlSchema ), sw );
            gmlWriter.write( geometry );
        } catch ( Exception e ) {
            throw new ProcessletException( "Error exporting geometry: " + e.getMessage() );
        }
    }

    /**
     * Writes a feature collection.
     * 
     * @param gmlOutput
     *            - output channel
     * @param coll
     *            - feature collection
     * @throws ProcessletException
     */
    private void writeFeatureCollection( ComplexOutput gmlOutput, FeatureCollection coll )
                            throws ProcessletException {
        try {

            String gmlSchema = gmlOutput.getRequestedSchema();
            XMLStreamWriterWrapper sw = new XMLStreamWriterWrapper( gmlOutput.getXMLStreamWriter(),
                                                                    "http://www.opengis.net/gml " + gmlSchema );
            sw.setPrefix( "gml", GMLNS );
            GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter( getGMLVersion( gmlSchema ), sw );
            gmlWriter.write( coll );

        } catch ( Exception e ) {
            throw new ProcessletException( "Error exporting geometry: " + e.getMessage() );
        }
    }
}
