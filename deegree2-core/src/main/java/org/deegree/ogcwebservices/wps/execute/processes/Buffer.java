//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

package org.deegree.ogcwebservices.wps.execute.processes;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.Code;
import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.JTSAdapter;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wps.describeprocess.InputDescription;
import org.deegree.ogcwebservices.wps.describeprocess.ProcessDescription;
import org.deegree.ogcwebservices.wps.execute.ComplexValue;
import org.deegree.ogcwebservices.wps.execute.ExecuteResponse;
import org.deegree.ogcwebservices.wps.execute.IOValue;
import org.deegree.ogcwebservices.wps.execute.OutputDefinition;
import org.deegree.ogcwebservices.wps.execute.OutputDefinitions;
import org.deegree.ogcwebservices.wps.execute.Process;
import org.deegree.ogcwebservices.wps.execute.ExecuteResponse.ProcessOutputs;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.buffer.BufferOp;

/**
 * Buffer.java
 *
 * Created on 09.03.2006. 23:42:39h
 *
 * This class describes an exemplary Process Implementation. The corresponding configuration document is '<root>\WEB-INF\conf\wps\processConfigs.xml'.
 * Process configuration is described further inside the configuration document.
 *
 * The process implementor has to ensure, that the process implemented extends the abstract super class Process.
 *
 * This example process IS NOT intended to describe a best practice approach. In some cases simplifying assumptions have
 * been made for sake of simplicity.
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 *
 * @version 1.0.
 *
 * @since 2.0
 */

public class Buffer extends Process {

    static int id = 0;

    /**
     *
     * @param processDescription
     */
    public Buffer( ProcessDescription processDescription ) {
        super( processDescription );
    }

    // define an ILogger for this class
    private static final ILogger LOG = LoggerFactory.getLogger( Buffer.class );

    /**
     * The provided buffer implementation relies on the Java Topology Suite
     * <link>http://www.vividsolutions.com/jts/JTSHome.htm</link>. Buffering is an operation which in GIS is used to
     * compute the area containing all points within a given distance of a Geometry. A buffer takes at least two inputs:
     *
     * 1.) The Geometry to buffer (point, linestring, polygon, etc.) 2.) The distance of the buffer.
     *
     * The provided Buffer defines two optional elements as well:
     *
     * 3.) The end cap style, which determines how the linework for the buffer polygon is constructed at the ends of
     * linestrings. Possible styles are: a) CAP_ROUND The usual round end caps (integer value of 1) b) CAP_BUTT End caps
     * are truncated flat at the line ends (integer value of 2) c) CAP_SQUARE End caps are squared off at the buffer
     * distance beyond the line ends (integer value of 2)
     *
     * 4.) Since the exact buffer outline of a Geometry usually contains circular sections, the buffer must be
     * approximated by the linear Geometry. The degree of approximation may be controlled by the user. This is done by
     * specifying the number of quadrant segments used to approximate a quarter-circle. Specifying a larger number of
     * segments results in a better approximation to the actual area, but also results in a larger number of line
     * segments in the computed polygon. The default value is 8.
     *
     */

    /*******************************************************************************************************************
     * <wps:DataInputs>
     ******************************************************************************************************************/

    /**
     * The input section defines four elements: 1) BufferDistance (mandatory), wich will be mapped to
     *
     * <code>private int bufferDistance</code> 2) CapStyle (optional, when ommited, default value 1 will be used),
     * which will be mapped to <code>private int capStyle</code> 3) ApproximationQuantization (optional, when ommited,
     * default value 8 will be used), which will be mapped to <code>private int approximationQuantization</code> 4)
     * InputGeometry (mandatory), which will be mapped to <code>private Object content</code>
     *
     * To illustrate the use, the first and fourth input Elements are included below.
     *
     */

    // "BufferDistance", "CapStyle", and "ApproximationQuantization" refer to
    // the corresponding <ows:Identifier/> elements.
    private static final String BUFFER_DISTANCE = "BufferDistance";

    private static final String CAP_STYLE = "CapStyle";

    private static final String APPROXIMATION_QUANTIZATION = "ApproximationQuantization";

    private static final String INPUT_GEOMETRY = "InputGeometry";

    /*******************************************************************************************************************
     * <wps:Input> <ows:Identifier>BufferDistance</ows:Identifier> <ows:Title>BufferDistance</ows:Title>
     * <ows:Abstract>Width of Buffer</ows:Abstract> <wps:LiteralValue dataType="urn:ogc:def:dataType:OGC:0.0:Double"
     * uom="urn:ogc:def:dataType:OGC:1.0:metre"> 50</wps:LiteralValue> </wps:Input>
     ******************************************************************************************************************/

    // the required attributes for calculating a spatial buffer, initialized
    // with default-values.
    private int bufferDistance = 0;

    private int capStyle = 1;

    private int approximationQuantization = 8;

    /**
     * the content represents the <wps:ComplexValue/> Element in the ProcessInput section. This sample process is feeded
     * with a feature collection, resulting in <wps:ComplexValue format="text/xml" encoding="UTF-8"
     * schema="http://schemas.opengis.net/gml/3.0.0/base/gml.xsd"> <wfs:FeatureCollection
     * xmlns:gml="http://www.opengis.net/gml" xmlns:wfs="http://www.opengis.net/wfs"
     * xmlns:app="http://www.deegree.org/app" xmlns:xlink="http://www.w3.org/1999/xlink"> <gml:boundedBy> <gml:Envelope>
     * <gml:pos>2581829.334 5660821.982</gml:pos> <gml:pos>2582051.078 5661086.442</gml:pos> </gml:Envelope>
     * </gml:boundedBy> <gml:featureMember> <app:flurstuecke gml:id="ID_10208"> <app:gid></app:gid> <app:id></app:id>
     * <app:rechtswert>2581969.20000000020</app:rechtswert> <app:hochwert>5660957.50000000000</app:hochwert>
     * <app:datum></app:datum> <app:folie></app:folie> <app:objart></app:objart> <app:aliasfolie>Flurstuecke</app:aliasfolie>
     * <app:aliasart>Flurstueck</app:aliasart> <app:alknr></app:alknr> <app:gemarkung></app:gemarkung> <app:flur></app:flur>
     * <app:zaehler></app:zaehler> <app:nenner></app:nenner> <app:beschrift></app:beschrift> <app:the_geom>
     * <gml:MultiPolygon srsName="EPSG:31466"> <gml:polygonMember> <gml:Polygon srsName="EPSG:31466">
     * <gml:outerBoundaryIs> <gml:LinearRing> <gml:coordinates cs="," decimal="." ts=" ">2581856.436,5660874.757
     * 2581947.164,5660938.093 2581940.797,5660952.002 2581936.158,5660962.135 2581971.597,5660982.717
     * 2581971.83,5660982.852 2581969.62,5660994.184 2581967.616,5661004.464 2581959.465,5661016.584
     * 2581958.555,5661017.679 2581967.415,5661024.833 2581974.177,5661032.529 2582021.543,5661086.442
     * 2582051.078,5661001.919 2582002.624,5660957.782 2581960.501,5660919.412 2581956.98,5660916.972
     * 2581904.676,5660880.734 2581878.263,5660853.196 2581868.096,5660842.595 2581848.325,5660821.982
     * 2581829.334,5660840.172 2581837.725,5660850.881 2581856.436,5660874.757</gml:coordinates> </gml:LinearRing>
     * </gml:outerBoundaryIs> </gml:Polygon> </gml:polygonMember> </gml:MultiPolygon> </app:the_geom> </app:flurstuecke>
     * </gml:featureMember> </wfs:FeatureCollection> </wps:ComplexValue>
     *
     */

    private Object content = null;

    // Values for ProcessOutput, will be filled dynamically.

    private Code identifier = null;

    private String title = null;

    private String _abstract = null;

    private URL schema = null;

    @SuppressWarnings("unused")
    private URI uom = null;

    private String format = null;

    private URI encoding = null;

    private ComplexValue complexValue = null;

    private TypedLiteral literalValue = null;

    /**
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.wps.execute.Process
     *
     * This is the central method for implementing a process. A <code>Map<String,IOValue></code> serves as an input
     * object. Each String represents the key (e.g. BufferDistance) which holds an IOValue as value (e.g. an object
     * representing a complete <wps:Input> element with all corresponding sub-elements). The process implementation is
     * responsible for retrieving all specified values according to the process configuration document.
     *
     * The method returns a <code>ProcessOutputs</code> object, which encapsulates the result of the process's
     * operation.
     *
     */
    @Override
    public ProcessOutputs execute( Map<String, IOValue> inputs, OutputDefinitions outputDefinitions )
                            throws OGCWebServiceException {

        LOG.logDebug( "execute Buffer invoked." );
        // delegate the read out of parameters to a private method
        readValuesFromInputDefinedValues( inputs );

        // get configured ( = supported) inputs from processDescription
        ProcessDescription.DataInputs configuredDataInputs = processDescription.getDataInputs();

        // delegate the read out of configured ( = supported ) inputs to a
        // private method
        readSupportedInputs( configuredDataInputs );

        // delegate the read out of configured outputs to a private method
        readOutputDefinitions( outputDefinitions );

        // Define a processOutputs object
        // validate, that data inputs correspond to process descritption
        ProcessOutputs processOutputs = null;
        boolean isValid = validate();
        if ( isValid ) {
            processOutputs = process();

        } else {
            LOG.logError( "Data input is invalid." );
            throw new OGCWebServiceException( "Buffer", "The configuration is invalid" );
        }

        return processOutputs;
    }

    /**
     * FIXME Assumes (simplified for the actual process) that only one output is defined. Reads the output definitions
     * into local variables.
     *
     * @param outputDefinitions
     */
    private void readOutputDefinitions( OutputDefinitions outputDefinitions ) {
        List<OutputDefinition> outputDefinitionList = outputDefinitions.getOutputDefinitions();
        Iterator<OutputDefinition> outputDefinitionListIterator = outputDefinitionList.iterator();
        while ( outputDefinitionListIterator.hasNext() ) {
            OutputDefinition outputDefinition = outputDefinitionListIterator.next();
            this._abstract = outputDefinition.getAbstract();
            this.title = outputDefinition.getTitle();
            this.identifier = outputDefinition.getIdentifier();
            this.schema = outputDefinition.getSchema();
            this.format = outputDefinition.getFormat();
            this.encoding = outputDefinition.getEncoding();
            this.uom = outputDefinition.getUom();
        }
    }

    /**
     * Private method for assigning input values to local variables
     *
     * @param inputs
     * @throws OGCWebServiceException
     */
    private void readValuesFromInputDefinedValues( Map<String, IOValue> inputs )
                            throws OGCWebServiceException {

        // check for mandatory values
        if ( null != inputs.get( BUFFER_DISTANCE ) ) {
            IOValue ioBufferDistance = inputs.get( BUFFER_DISTANCE );
            TypedLiteral literalBuffer = ioBufferDistance.getLiteralValue();
            this.bufferDistance = Integer.parseInt( literalBuffer.getValue() );
        } else {
            throw new MissingParameterValueException( getClass().getName(),
                                                      "The required Input Parameter BufferDistance is missing." );
        }

        if ( null != inputs.get( INPUT_GEOMETRY ) ) {
            IOValue ioGeometry = inputs.get( INPUT_GEOMETRY );
            ComplexValue complexGeometry = ioGeometry.getComplexValue();
            this.content = complexGeometry.getContent();
        } else {
            throw new MissingParameterValueException( getClass().getName(),
                                                      "The required Input Parameter InputGeometry is missing." );
        }

        // check for optional values
        if ( null != inputs.get( APPROXIMATION_QUANTIZATION ) ) {
            IOValue ioApproxQuant = inputs.get( APPROXIMATION_QUANTIZATION );
            TypedLiteral literalApproxQuant = ioApproxQuant.getLiteralValue();
            this.approximationQuantization = Integer.parseInt( literalApproxQuant.getValue() );
        } else {
            // okay, parameter is optional. Default value will be assigned.
        }
        if ( null != inputs.get( CAP_STYLE ) ) {
            IOValue ioCapStyle = inputs.get( CAP_STYLE );
            TypedLiteral literalCapStyle = ioCapStyle.getLiteralValue();
            this.capStyle = Integer.parseInt( literalCapStyle.getValue() );
        } else {
            // okay, parameter is optional. Default value will be assigned.
        }
    }

    /**
     * Read configured data inputs for validation.
     *
     * @param configuredDataInputs
     */
    private void readSupportedInputs( ProcessDescription.DataInputs configuredDataInputs ) {
        // Get list of configured/supported/mandatory??? WPSInputDescriptions
        // from configuredDataInputs
        List<InputDescription> inputDescriptions = configuredDataInputs.getInputDescriptions();

        // Get inputDescription for each configured input
        Iterator<InputDescription> inputDescriptionIterator = inputDescriptions.iterator();
        // TODO write variables for each input separately
        while ( inputDescriptionIterator.hasNext() ) {
            // Read values from inputDescription
            InputDescription inputDescription = inputDescriptionIterator.next();
            this._abstract = inputDescription.getAbstract();
            this.identifier = inputDescription.getIdentifier();
            this.title = inputDescription.getTitle();
        }
    }

    /**
     * Method for validating provided input parameters against configured input parameters. <b> Not implemented right
     * now! </b>
     *
     * @return true
     */
    private boolean validate() {
        boolean isValid = true;

        return isValid;
    }

    private ProcessOutputs process()
                            throws OGCWebServiceException {
        ProcessOutputs processOutputs = new ExecuteResponse.ProcessOutputs();
        // Create ProcessOutputs DataStructure
        Object content = bufferContent();
        this.complexValue = new ComplexValue( this.format, this.encoding, this.schema, content );
        IOValue ioValue = new IOValue( this.identifier, this._abstract, this.title, null, this.complexValue, null,
                                       this.literalValue );
        List<IOValue> processOutputsList = new ArrayList<IOValue>( 1 );
        processOutputsList.add( ioValue );
        processOutputs.setOutputs( processOutputsList );
        return processOutputs;
    }

    /**
     *
     * @return buffered result. In case result is a FeatureCollection, (result instanceof FeatureCollection) will return
     *         true.
     * @throws OGCWebServiceException
     */
    private Feature bufferContent()
                            throws OGCWebServiceException {
        org.deegree.model.spatialschema.Geometry[] buffered = null;
        Feature result = null;

        // determine if Geometry is Feature collection
        if ( content instanceof FeatureCollection && content instanceof Feature ) {
            // if content is a FeatureCollection, cast explicitly to
            // FeatureCollection
            FeatureCollection featureCollection = (FeatureCollection) this.content;
            // split FeatureCollection into an array of features
            Feature[] features = featureCollection.toArray();
            int size = features.length;
            // preinitialize a FeatureCollection for the buffered features
            FeatureCollection resultFeatureCollection = FeatureFactory.createFeatureCollection( "BufferedFeatures",
                                                                                                size );
            Feature f = null;

            // iterate over every feature of the array and perform buffer
            // operation. afterwards store result into feature collection
            for ( int i = 0; i < size; i++ ) {
                f = features[i];
                buffered = bufferGeometry( f, this.bufferDistance, this.capStyle, this.approximationQuantization );

                // generate QualifiedName for buffered Feature from original Feature
                QualifiedName oldQN = ( f.getFeatureType().getProperties() )[0].getName();
                QualifiedName newQN = new QualifiedName( oldQN.getPrefix(), "Buffer", oldQN.getNamespace() );

                // convert from Geometry to Feature
                for ( int j = 0; j < buffered.length; j++ ) {
                    resultFeatureCollection.add( convertToFeature( buffered[j], id, newQN ) );
                    id++;
                }
                // set result value
                result = resultFeatureCollection;
            }
        }

        // determine if Geometry is Feature
        if ( content instanceof Feature && !( content instanceof FeatureCollection ) ) {
            // if content is a Feature, cast explicitly to Feature
            Feature feature = (Feature) content;
            buffered = bufferGeometry( feature, this.bufferDistance, this.capStyle, this.approximationQuantization );

            // generate QualifiedName for buffered Feature from original Feature
            QualifiedName oldQN = ( feature.getFeatureType().getProperties() )[0].getName();
            QualifiedName newQN = new QualifiedName( oldQN.getPrefix(), "Buffer", oldQN.getNamespace() );

            // convert from Geometry to Feature
            result = convertToFeature( buffered[0], id, newQN );
            id++;
        }
        // return result. In case result is a FeatureCollection, an (result
        // instanceof FeatureCollection) will return true.
        return result;

    }

    /**
     * This methods implements the actual buffer process.
     *
     *
     * @param feature
     *            Feature to buffer
     * @param bufferDistance
     * @param capStyle
     * @param approximationQuantization
     * @return an array of buffered deegree geometries
     * @throws OGCWebServiceException
     */
    private org.deegree.model.spatialschema.Geometry[] bufferGeometry( Feature feature, int bufferDistance,
                                                                       int capStyle, int approximationQuantization )
                            throws OGCWebServiceException {
        // Read the geometry property values from the provided feature
        org.deegree.model.spatialschema.Geometry[] geomArray = feature.getGeometryPropertyValues();
        if ( geomArray == null || geomArray.length == 0 ) {
            throw new OGCWebServiceException( "No geometries found." );
        }

        // initialize (null) Geometry (JTS) for the output of BufferProcess
        Geometry buffered = null;
        // initialize (null) Geometry (deegree)
        org.deegree.model.spatialschema.Geometry[] bufferedDeegreeGeometry = new org.deegree.model.spatialschema.Geometry[geomArray.length];
        // BufferOp allow optional values for capStyle and
        // approximationQuantization (JTS)
        BufferOp options = null;

        // iterate over Geometries
        for ( int j = 0; j < geomArray.length; j++ ) {
            try {
                // convert Geometries to JTS Geometries for buffer
                Geometry unbuffered = JTSAdapter.export( geomArray[j] );
                // set buffer options and get the result geometry
                options = new BufferOp( unbuffered );
                options.setEndCapStyle( capStyle );
                options.setQuadrantSegments( approximationQuantization );
                buffered = options.getResultGeometry( bufferDistance );
                // convert back to Geometry (deegree)
                bufferedDeegreeGeometry[j] = JTSAdapter.wrap( buffered );
                LOG.logInfo( "Buffered Geometry with a distance of " + bufferDistance + " , a capStyle of " + capStyle
                             + " , and an approximation quantization of " + approximationQuantization + "." );
            } catch ( GeometryException e ) {
                LOG.logError( e.getMessage() );
                throw new OGCWebServiceException( "Something went wrong while processing buffer operation." );
            }
        }
        return bufferedDeegreeGeometry;
    }

    /**
     *
     * Convert a Geometry (deegree) to a Feature
     *
     * @param bufferedGeometry
     * @return a Feature representation of Input bufferedGeometry
     *
     */
    private Feature convertToFeature( org.deegree.model.spatialschema.Geometry bufferedGeometry, int id,
                                      QualifiedName oQN ) {
        PropertyType[] propertyTypeArray = new PropertyType[1];
        propertyTypeArray[0] = FeatureFactory.createSimplePropertyType( new QualifiedName( oQN.getPrefix(), "GEOM",
                                                                                           oQN.getNamespace() ),
                                                                        Types.GEOMETRY, false );
        // FIXME set EPSG
        FeatureType ft = FeatureFactory.createFeatureType( oQN, false, propertyTypeArray );
        FeatureProperty[] featurePropertyArray = new FeatureProperty[1];
        featurePropertyArray[0] = FeatureFactory.createFeatureProperty( new QualifiedName( oQN.getPrefix(), "GEOM",
                                                                                           oQN.getNamespace() ),
                                                                        bufferedGeometry );
        Feature feature = FeatureFactory.createFeature( "ID_" + id, ft, featurePropertyArray );

        return feature;
    }
}
