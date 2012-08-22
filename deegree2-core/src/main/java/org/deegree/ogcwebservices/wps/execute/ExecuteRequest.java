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

package org.deegree.ogcwebservices.wps.execute;

import static org.deegree.ogcbase.CommonNamespaces.OWSNS;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.Code;
import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OperationNotSupportedException;
import org.deegree.ogcwebservices.wps.WPSRequestBaseType;
import org.deegree.ogcwebservices.wps.execute.IOValue.ComplexValueReference;
import org.w3c.dom.Element;

/**
 * ExecuteRequest.java
 *
 * Created on 09.03.2006. 23:16:00h
 *
 * WPS Execute operation request, to execute one identified Process. If a process is to be run multiple times, each run
 * shall be submitted as a separate Execute request.
 *
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ExecuteRequest extends WPSRequestBaseType {

    /**
     *
     */
    private static final long serialVersionUID = -2943128923230930885L;

    private static ILogger LOG = LoggerFactory.getLogger( ExecuteRequest.class );

    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    /**
     * @param version
     * @param id
     * @param vendorSpecificParameter
     * @param inputs
     * @param identifier
     * @param definitions
     * @param status
     * @param store
     */
    public ExecuteRequest( String version, String id, Map<String, String> vendorSpecificParameter,
                           ExecuteDataInputs inputs, Code identifier, OutputDefinitions definitions, boolean status,
                           boolean store ) {
        super( version, id, vendorSpecificParameter );

        dataInputs = inputs;
        this.identifier = identifier;
        outputDefinitions = definitions;
        this.status = status;
        this.store = store;
    }

    /**
     * Identifier of the Process to be executed. This Process identifier shall be as listed in the ProcessOfferings
     * section of the WPS Capabilities document.
     */
    protected Code identifier;

    /**
     * List of input (or parameter) values provided to the process, including each of the Inputs needed to execute the
     * process. It is possible to have no inputs provided only when all the inputs are predetermined fixed resources. In
     * all other cases, at least one input is required.
     */
    protected ExecuteDataInputs dataInputs;

    /**
     * List of definitions of the outputs (or parameters) requested from the process. These outputs are not normally
     * identified, unless the client is specifically requesting a limited subset of outputs, and/or is requesting output
     * formats and/or schemas and/or encodings different from the defaults and selected from the alternatives identified
     * in the process description, or wishes to customize the descriptive information about the output.
     */
    protected OutputDefinitions outputDefinitions;

    /**
     * Specifies if the Execute operation response shall be returned quickly with status information, or not returned
     * until process execution is complete. This parameter shall not be included unless the corresponding
     * "statusSupported" parameter is included and is "true" in the ProcessDescription for this process.
     */
    protected boolean status;

    /**
     * Specifies if the complex valued output(s) of this process should be stored by the process as web-accessible
     * resources. If store is "true", the server shall store all the complex valued output(s) of the process so that the
     * client can retrieve them as required. If store is "false", all the complex valued output(s) shall be encoded in
     * the Execute operation response. This parameter shall not be included unless the corresponding "storeSupported"
     * parameter is included and is "true" in the ProcessDescription for this process.
     */
    protected boolean store;

    /**
     * @return Returns the identifier.
     */
    public Code getIdentifier() {
        return identifier;
    }

    /**
     * @return Returns the dataInputs.
     */
    public ExecuteDataInputs getDataInputs() {
        return dataInputs;
    }

    /**
     * @return Returns the outputDefinitions.
     */
    public OutputDefinitions getOutputDefinitions() {
        return outputDefinitions;
    }

    /**
     * @return true if status is set.
     */
    public boolean isStatus() {
        return status;
    }

    /**
     * @return true if can store
     */
    public boolean isStore() {
        return store;
    }

    /**
     *
     * KVP-encoded transfer of the execute operation request is not supported.
     *
     * @see "OGC 05-007r4 Subclause 10.2.2"
     * @param map
     * @return the Execute request created from the map.
     * @throws OGCWebServiceException
     *
     */
    public static ExecuteRequest create( Map<String, String> map )
                            throws OGCWebServiceException {
        String msg = "KVP-encoded transfer of the execute operation request is not supported.";
        LOG.logError( msg );
        throw new OperationNotSupportedException( msg );
    }

    /**
     *
     * @param id
     * @param executeNode
     * @return the ExcuteRequest wrapper for the xml encoded request.
     * @throws OGCWebServiceException
     * @throws MissingParameterValueException
     * @throws InvalidParameterValueException
     */
    public static ExecuteRequest create( String id, Element executeNode )
                            throws OGCWebServiceException, MissingParameterValueException,
                            InvalidParameterValueException {

        // get attribute version from <wps:Execute> Node
        String version = getVersion( executeNode );

        // get attribute status from <wps:Execute> Node
        boolean status = getStatus( executeNode );

        // get attribute store from <wps:Execute> Node
        boolean store = getStore( executeNode );

        Map<String, String> vendorSpecificParameters = null;

        // get <ows:Identifier> from <wps:Execute> Node
        Code identifier = getIdentifier( executeNode );

        // get <wps:DataInputs> from <wps:Execute> Node
        ExecuteDataInputs dataInputs = getDataInputs( executeNode );

        // get <wps:OutputDefinitions> from <wps:Execute> Node
        OutputDefinitions outputDefinitions = getOutputDefinitions( executeNode );

        return new ExecuteRequest( version, id, vendorSpecificParameters, dataInputs, identifier, outputDefinitions,
                                   status, store );
    }

    /**
     *
     * @param executeNode
     * @return version
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    private static final String getVersion( Element executeNode )
                            throws InvalidParameterValueException, MissingParameterValueException {
        String version = null;
        try {
            version = XMLTools.getRequiredAttrValue( "version", null, executeNode );
        } catch ( XMLParsingException xmlex ) {
            String msg = "Operation Request does not include parameter value and this server did not declare a default value for that parameter.";
            LOG.logDebug( msg );
            throw new MissingParameterValueException( "Version", msg );
        }
        if ( "".equals( version ) ) {
            String msg = "Operation Request contains an invalid parameter value";
            LOG.logDebug( msg );
            throw new InvalidParameterValueException( "Version", msg );
        }
        if ( !supportedVersion.equals( version ) ) {
            String msg = "Operation Request contains an invalid parameter value";
            LOG.logDebug( msg );
            throw new InvalidParameterValueException( "Version", msg );
        }
        return version;
    }

    /**
     *
     * @param executeNode
     * @return status
     * @throws InvalidParameterValueException
     */
    private static final boolean getStatus( Element executeNode )
                            throws InvalidParameterValueException {
        boolean status = false;
        String statusString = XMLTools.getAttrValue( executeNode, null, "status", null );
        if ( null != statusString ) {
            if ( "true".equalsIgnoreCase( statusString ) || "false".equalsIgnoreCase( statusString ) ) {
                status = new Boolean( statusString );
            } else {
                String msg = "Operation Request contains an invalid parameter value";
                LOG.logDebug( msg );
                throw new InvalidParameterValueException( "status", msg );
            }
        }
        return status;
    }

    /**
     *
     * @param executeNode
     * @return store
     * @throws InvalidParameterValueException
     */
    private static final boolean getStore( Element executeNode )
                            throws InvalidParameterValueException {
        boolean store = false;
        String storeString = XMLTools.getAttrValue( executeNode, null, "store", null );
        if ( null != storeString ) {
            if ( "true".equalsIgnoreCase( storeString ) || "false".equalsIgnoreCase( storeString ) ) {
                store = new Boolean( storeString );
            } else {
                String msg = "Operation Request contains an invalid parameter value";
                LOG.logDebug( msg );
                throw new InvalidParameterValueException( "store", msg );
            }
        }
        return store;
    }

    /**
     *
     * @param e
     * @return identifier
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    private static Code getIdentifier( Element e )
                            throws InvalidParameterValueException, MissingParameterValueException {
        // Get required node <ows:Identifier>
        String identifierAsString = null;

        try {
            identifierAsString = XMLTools.getRequiredNodeAsString( e, "ows:Identifier/text()", nsContext );
        } catch ( XMLParsingException ex ) {
            String msg = "Operation Request does not include parameter value and this server did not declare a default value for that parameter.";
            LOG.logDebug( msg );
            throw new MissingParameterValueException( "Identifier", msg );
        }

        if ( "".equals( identifierAsString ) ) {
            String msg = "Operation Request contains an invalid parameter value";
            LOG.logDebug( msg );
            throw new InvalidParameterValueException( "Identifier", msg );
        }
        return new Code( identifierAsString, null );
    }

    /**
     *
     * @param executeNode
     * @return dataInputs
     * @throws MissingParameterValueException
     * @throws InvalidParameterValueException
     */
    @SuppressWarnings("unchecked")
    private static ExecuteDataInputs getDataInputs( Element executeNode )
                            throws MissingParameterValueException, InvalidParameterValueException {
        // Get optional node <DataInputs>
        ExecuteDataInputs dataInputs = null;
        try {
            Element dataInputsNode = (Element) XMLTools.getNode( executeNode, "wps:DataInputs", nsContext );

            // dataInputsNode may be null, if not null, at least one <Input> has
            // to be defined
            if ( null != dataInputsNode ) {
                LOG.logInfo( "DataInputs: " + dataInputsNode );
                List dataInputNodeList = XMLTools.getNodes( dataInputsNode, "wps:Input", nsContext );

                if ( null != dataInputNodeList && 0 != dataInputNodeList.size() ) {
                    dataInputs = new ExecuteDataInputs();
                    int size = dataInputNodeList.size();
                    HashMap inputs = new HashMap<String, IOValue>( size );
                    for ( int i = 0; i < size; i++ ) {
                        IOValue ioValue = getIOValue( (Element) dataInputNodeList.get( i ) );

                        inputs.put( ioValue.getIdentifier().getCode(), ioValue );
                    }
                    dataInputs.setInputs( inputs );
                } else {
                    throw new MissingParameterValueException( "Input",
                                                              "If DataInputs node provided, at least one input node has to be defined." );
                }
            }
        } catch ( XMLParsingException ex ) {
            String msg = "Optional node DataInputs not declared";
            LOG.logDebug( msg );
        }

        return dataInputs;
    }

    /**
     *
     * @param inputNode
     * @return IOValue
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    private static IOValue getIOValue( Element inputNode )
                            throws InvalidParameterValueException, MissingParameterValueException {
        Code identifier = getIdentifier( inputNode );
        String title = getTitle( inputNode );
        String _abstract = getAbstract( inputNode );
        Envelope boundingBox = getBoundingBox( inputNode );
        ComplexValue complexValue = getComplexValue( inputNode );
        ComplexValueReference complexValueReference = getValueReference( inputNode );
        TypedLiteral literalValue = getLiteralValue( inputNode );

        return new IOValue( identifier, title, _abstract, boundingBox, complexValue, complexValueReference,
                            literalValue );
    }

    /**
     *
     * @param e
     * @return title
     * @throws MissingParameterValueException
     * @throws InvalidParameterValueException
     */
    private static String getTitle( Element e )
                            throws MissingParameterValueException, InvalidParameterValueException {
        String title;
        try {
            title = XMLTools.getRequiredNodeAsString( e, "ows:Title/text()", nsContext );
        } catch ( XMLParsingException ex ) {
            String msg = "Operation Request does not include parameter value and this server did not declare a default value for that parameter.";
            throw new MissingParameterValueException( "Title", msg );
        }

        if ( "".equals( title ) ) {
            throw new InvalidParameterValueException( "Title", "Operation Request contains an invalid parameter Value" );
        }
        return title;
    }

    /**
     *
     * @param e
     * @return abstact
     */
    private static String getAbstract( Element e ) {
        String _abstract = null;
        try {
            _abstract = XMLTools.getNodeAsString( e, "ows:Abstract/text()", nsContext, null );
        } catch ( XMLParsingException ex ) {
            // optional Node
        }
        return _abstract;
    }

    /**
     *
     * @param e
     * @return boundingBox
     * @throws MissingParameterValueException
     * @throws InvalidParameterValueException
     */
    private static Envelope getBoundingBox( Element e )
                            throws MissingParameterValueException, InvalidParameterValueException {
        Envelope boundingBox = null;

        try {
            Element boundingBoxValueNode = (Element) XMLTools.getNode( e, "wps:BoundingBoxValue", nsContext );

            if ( null != boundingBoxValueNode ) {
                double minX = 0;
                double minY = 0;
                double maxX = 0;
                double maxY = 0;
                String crsName = null;
                String crs = null;
                try {
                    crs = XMLTools.getRequiredNodeAsString( boundingBoxValueNode, "@crs", nsContext );
                    String lowerCornerValue = XMLTools.getRequiredNodeAsString( boundingBoxValueNode,
                                                                                "ows:LowerCorner/text()", nsContext );
                    String[] lowerCornerValues = lowerCornerValue.split( " " );
                    if ( lowerCornerValues.length != 2 ) {
                        throw new InvalidParameterValueException( "lowerCornerNode", "Two parameters are mandatory." );
                    }
                    minX = Double.parseDouble( lowerCornerValues[0] );
                    minY = Double.parseDouble( lowerCornerValues[1] );
                } catch ( XMLParsingException ex ) {
                    throw new MissingParameterValueException(
                                                              "LowerCornerNode",
                                                              "Operation Request does not include "
                                                                                      + "parameter value and this server did "
                                                                                      + "not declare a default value for that "
                                                                                      + "parameter." );
                }
                try {
                    String upperCornerValue = XMLTools.getRequiredNodeAsString( boundingBoxValueNode,
                                                                                "ows:UpperCorner/text()", nsContext );
                    String[] upperCornerValues = upperCornerValue.split( " " );
                    if ( upperCornerValues.length != 2 ) {
                        throw new InvalidParameterValueException( "upperCornerNode", "Two parameters are mandatory." );
                    }
                    maxX = Double.parseDouble( upperCornerValues[0] );
                    maxY = Double.parseDouble( upperCornerValues[1] );
                } catch ( XMLParsingException ex ) {
                    throw new MissingParameterValueException(
                                                              "LowerCornerNode",
                                                              "Operation Request does not include parameter value and this server did not declare a default value for that parameter." );
                }

                crsName = crs.substring( 16 );

                CoordinateSystem cs;
                try {
                    cs = CRSFactory.create( crsName );
                } catch ( UnknownCRSException e1 ) {
                    throw new InvalidParameterValueException( ExecuteRequest.class.getName(), e1.getMessage() );
                }

                boundingBox = GeometryFactory.createEnvelope( minX, minY, maxX, maxY, cs );
            }
        } catch ( XMLParsingException ex ) {
            // optionalNode
        }
        return boundingBox;
    }

    /**
     *
     * @param e
     * @return complexValue
     * @throws InvalidParameterValueException
     */
    private static ComplexValue getComplexValue( Element e )
                            throws InvalidParameterValueException {
        ComplexValue complexValue = null;

        try {
            Element complexValueNode = (Element) XMLTools.getNode( e, "wps:ComplexValue", nsContext );
            if ( null != complexValueNode ) {
                String format = null;
                URI encoding = null;
                URL schema = null;
                Object value = null;
                try {

                    format = complexValueNode.getAttribute( "format" );
                    if ( null != format ) {
                        if ( "".equals( format ) ) {
                            throw new InvalidParameterValueException( "ComplexValue",
                                                                      "Attribute format must not be empty if provided." );

                        }
                    }
                    String enc = complexValueNode.getAttribute( "encoding" );
                    if ( null != enc ) {
                        if ( "".equals( enc ) ) {
                            throw new InvalidParameterValueException( "ComplexValue",
                                                                      "Attribute encoding must not be empty if provided." );
                        }
                        encoding = new URI( enc );
                    }

                    String scheme = complexValueNode.getAttribute( "schema" );
                    if ( null != scheme ) {
                        if ( "".equals( scheme ) ) {
                            throw new InvalidParameterValueException( "ComplexValue",
                                                                      "Attribute schema must not be empty if provided." );
                        }
                        schema = new URL( scheme );
                    }

                    /**
                     * FIXME complexValue may contain any type of data specified by the attributes format, encoding and
                     * schema dynamically extract the content of this node according to specified format, encoding ,
                     * schema
                     *
                     * @see OGC05-007r4 Table 35. At the moment only a GML FeatureCollection is supported.
                     *
                     * does using xmlfragment make more sense??
                     */
                    Element complexValueContent = XMLTools.getFirstChildElement( complexValueNode );
                    if ( null != complexValueContent ) {
                        try {

                            GMLFeatureCollectionDocument gmlFeatureCollectionDoc = new GMLFeatureCollectionDocument();
                            gmlFeatureCollectionDoc.setRootElement( complexValueContent );
                            value = gmlFeatureCollectionDoc.parse();

                        } catch ( XMLParsingException ex1 ) {
                            LOG.logInfo( "Provided content cannot be parsed as featurecollection" );
                        }
                    }

                } catch ( URISyntaxException uriEx ) {
                    throw new InvalidParameterValueException( "ComplexValue",
                                                              "Operation Request contains an invalid parameter Value" );
                } catch ( MalformedURLException mue ) {
                    throw new InvalidParameterValueException( "ComplexValue",
                                                              "Operation Request contains an invalid parameter Value" );
                }

                complexValue = new ComplexValue( format, encoding, schema, value );
            }

        } catch ( XMLParsingException ex ) {
            // optionalNode
        }

        return complexValue;

    }

    /**
     *
     * @param e
     * @return complexValueReference
     * @throws InvalidParameterValueException
     */
    private static ComplexValueReference getValueReference( Element e )
                            throws InvalidParameterValueException {
        ComplexValueReference complexValueReference = null;

        Element complexValueReferenceNode;
        try {
            complexValueReferenceNode = (Element) XMLTools.getNode( e, "wps:ComplexValueReference", nsContext );

            if ( null != complexValueReferenceNode ) {
                String format = null;
                URI encoding = null;
                URL schema = null;
                URL reference = null;

                format = complexValueReferenceNode.getAttribute( "format" );
                if ( null != format ) {
                    if ( "".equals( format ) ) {
                        throw new InvalidParameterValueException( "ComplexValueReference",
                                                                  "Attribute format must not be empty if provided." );
                    }
                }

                String enc = complexValueReferenceNode.getAttribute( "encoding" );
                if ( null != enc ) {
                    if ( "".equals( enc ) ) {
                        throw new InvalidParameterValueException( "ComplexValueReference",
                                                                  "Attribute encoding must not be empty if provided." );
                    }
                    try {
                        encoding = new URI( enc );
                    } catch ( URISyntaxException e1 ) {
                        throw new InvalidParameterValueException( "ComplexValueReference",
                                                                  "Provided content of attribute encoding could not be parsed as URI." );
                    }
                }

                String scheme = complexValueReferenceNode.getAttribute( "schema" );
                if ( null != scheme ) {
                    if ( "".equals( scheme ) ) {
                        throw new InvalidParameterValueException( "ComplexValueReference",
                                                                  "Attribute schema must not be empty if provided." );
                    }
                    try {
                        schema = new URL( scheme );
                    } catch ( MalformedURLException e1 ) {
                        throw new InvalidParameterValueException( "ComplexValueReference",
                                                                  "Provided content of attribute schema could not be parsed as URL." );
                    }
                }

                String referenceString = complexValueReferenceNode.getAttributeNS( OWSNS.toString(), "reference" );
                if ( "".equals( referenceString ) ) {
                    throw new InvalidParameterValueException( "ComplexValueReference",
                                                              "Mandatory attibute reference must not be empty." );
                }
                try {
                    reference = new URL( referenceString );
                } catch ( MalformedURLException e1 ) {
                    throw new InvalidParameterValueException( "ComplexValueReference",
                                                              "Provided content of attribute reference could not be parsed as URL." );

                }

                complexValueReference = new IOValue.ComplexValueReference( format, encoding, schema, reference );
            }
        } catch ( XMLParsingException e1 ) {
            // optional element
        }

        return complexValueReference;
    }

    /**
     *
     * @param e
     * @return literalValue
     * @throws InvalidParameterValueException
     */
    private static TypedLiteral getLiteralValue( Element e )
                            throws InvalidParameterValueException {
        TypedLiteral literalValue = null;

        Element literalValueNode;
        try {
            literalValueNode = (Element) XMLTools.getNode( e, "wps:LiteralValue", nsContext );
            if ( null != literalValueNode ) {
                String value = null;
                URI dataType = null;
                URI uom = null;
                String dataTypeString = literalValueNode.getAttribute( "dataType" );
                if ( null != dataTypeString ) {
                    if ( "".equals( dataTypeString ) ) {
                        throw new InvalidParameterValueException( "LiteralValue",
                                                                  "Attribute data type must not be empty if provided." );
                    }
                    try {
                        dataType = new URI( dataTypeString );
                    } catch ( URISyntaxException e1 ) {
                        throw new InvalidParameterValueException( "LiteralValue",
                                                                  "Provided content of attribute data type could not be parsed as URI." );
                    }
                }

                String uomString = literalValueNode.getAttribute( "uom" );
                if ( null != uomString ) {
                    if ( "".equals( uomString ) ) {
                        throw new InvalidParameterValueException( "LiteralValue",
                                                                  "Attribute uom must not be empty if provided." );
                    }
                }
                try {
                    uom = new URI( uomString );
                } catch ( URISyntaxException e1 ) {
                    throw new InvalidParameterValueException( "LiteralValue",
                                                              "Provided content of attribute uom could not be parsed as URI." );
                }
                value = XMLTools.getNodeAsString( literalValueNode, "/text()", nsContext, null );

                literalValue = new TypedLiteral( value, dataType, uom );
            }

        } catch ( XMLParsingException e1 ) {
            // optional Element
        }

        return literalValue;
    }

    /**
     *
     * @param executeNode
     * @return outputDefinitions
     * @throws MissingParameterValueException
     * @throws InvalidParameterValueException
     */
    @SuppressWarnings("unchecked")
    private static OutputDefinitions getOutputDefinitions( Element executeNode )
                            throws MissingParameterValueException, InvalidParameterValueException {
        OutputDefinitions outputDefinitions = null;

        try {
            Element outputDefinitionsNode = (Element) XMLTools.getNode( executeNode, "wps:OutputDefinitions", nsContext );

            if ( null != outputDefinitionsNode ) {

                // outputDefinitionsNode may be null, if not null, at least one <Input> has to be defined
                LOG.logInfo( "outputDefinitionsNode: " + outputDefinitionsNode );
                List outputNodeList = XMLTools.getNodes( outputDefinitionsNode, "wps:Output", nsContext );

                if ( null != outputNodeList && 0 != outputNodeList.size() ) {
                    outputDefinitions = new OutputDefinitions();
                    int size = outputNodeList.size();
                    List outputs = new ArrayList<OutputDefinition>( size );
                    for ( int i = 0; i < size; i++ ) {
                        outputs.add( i, getOutputDefinition( (Element) outputNodeList.get( i ) ) );
                    }

                    outputDefinitions.setOutputDefinitions( outputs );
                } else {
                    throw new MissingParameterValueException( "Output",
                                                              "If OutputDefinitions node provided, at least one output node has to be defined." );
                }
            }

        } catch ( XMLParsingException ex ) {
            // Optional element
        }

        return outputDefinitions;
    }

    /**
     *
     * @param outputNode
     * @return outputDefinitions
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    private static OutputDefinition getOutputDefinition( Element outputNode )
                            throws InvalidParameterValueException, MissingParameterValueException {

        Code identifier = getIdentifier( outputNode );
        String title = getTitle( outputNode );
        String _abstract = getAbstract( outputNode );
        String format = null;
        URI encoding = null;
        URL schema = null;
        URI uom = null;

        format = outputNode.getAttribute( "format" );
        if ( null != format ) {
            if ( "".equals( format ) ) {
                throw new InvalidParameterValueException( "Output", "Attribute format must not be empty if provided." );
            }
        }

        String enc = outputNode.getAttribute( "encoding" );
        if ( null != enc ) {
            if ( "".equals( enc ) ) {
                throw new InvalidParameterValueException( "Output", "Attribute encoding must not be empty if provided." );
            }
            try {
                encoding = new URI( enc );
            } catch ( URISyntaxException e1 ) {
                throw new InvalidParameterValueException( "Output",
                                                          "Provided content of attribute encoding could not be parsed as URI." );
            }
        }

        String scheme = outputNode.getAttribute( "schema" );
        if ( null != scheme ) {
            if ( "".equals( scheme ) ) {
                throw new InvalidParameterValueException( "Output", "Attribute schema must not be empty if provided." );
            }
            try {
                schema = new URL( scheme );
            } catch ( MalformedURLException e1 ) {
                throw new InvalidParameterValueException( "Output",
                                                          "Provided content of attribute schema could not be parsed as URL." );
            }
        }

        String uomString = outputNode.getAttribute( "uom" );
        if ( null != uomString ) {
            if ( "".equals( uomString ) ) {
                throw new InvalidParameterValueException( "Output", "Attribute uom must not be empty if provided." );
            }
        }
        try {
            uom = new URI( uomString );
        } catch ( URISyntaxException e1 ) {
            throw new InvalidParameterValueException( "Output",
                                                      "Provided content of attribute uom could not be parsed as URI." );
        }

        return new OutputDefinition( identifier, title, _abstract, encoding, format, schema, uom );
    }

}
