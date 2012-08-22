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
package org.deegree.ogcwebservices.wps.describeprocess;

import static org.deegree.ogcbase.CommonNamespaces.OWSNS;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.deegree.datatypes.Code;
import org.deegree.datatypes.values.Closure;
import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.datatypes.values.ValueRange;
import org.deegree.datatypes.xlink.SimpleLink;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcwebservices.MetadataType;
import org.deegree.ogcwebservices.wps.describeprocess.ProcessDescription.DataInputs;
import org.deegree.ogcwebservices.wps.describeprocess.ProcessDescription.ProcessOutputs;
import org.deegree.owscommon.OWSMetadata;
import org.deegree.owscommon.com110.OWSAllowedValues;
import org.w3c.dom.Element;

/**
 * ProcessDescriptionDocument.java
 *
 * Created on 10.03.2006. 15:18:02h
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 *
 * @version 1.0.
 *
 * @since 2.0
 */

public class ProcessDescriptionDocument extends XMLFragment {

    /**
     *
     */
    private static final long serialVersionUID = 7104894360064209850L;

    private static ILogger LOG = LoggerFactory.getLogger( ProcessDescriptionDocument.class );

    /**
     *
     * @return the bean created from the root element.
     * @throws XMLParsingException
     */
    public ProcessDescription parseProcessDescription()
                            throws XMLParsingException {

        Element root = getRootElement();

        Element processDescriptionNode = (Element) XMLTools.getRequiredNode( root, "wps:ProcessDescription", nsContext );

        String responsibleClass = getResponsibleClass( processDescriptionNode );

        Code identifier = getIdentifier( processDescriptionNode );

        String title = getTitle( processDescriptionNode );

        String _abstract = getAbstract( processDescriptionNode );

        List<MetadataType> metadataTypeList = getMetadata( processDescriptionNode );

        DataInputs dataInputs = null;

        ProcessOutputs processOutputs = null;

        // Get optional attribute "processVersion" from <wps:ProcessDescription>
        // node
        String processVersion = null;
        String versionAttribute = processDescriptionNode.getAttribute( "processVersion" );
        if ( null != versionAttribute && !"".equals( versionAttribute ) ) {
            processVersion = versionAttribute;
        }

        // Get optional attribute "storeSupported" from <wps:ProcessDescription>
        // node
        Boolean storeSupported = new Boolean( false );
        String storeSupportedAttribute = processDescriptionNode.getAttribute( "storeSupported" );
        if ( null != storeSupportedAttribute && !"".equals( storeSupportedAttribute ) ) {
            storeSupported = Boolean.valueOf( storeSupportedAttribute );
        }

        // Get optional attribute "statusSupported" from
        // <wps:ProcessDescription> node
        Boolean statusSupported = new Boolean( false );
        String statusSupportedAttribute = processDescriptionNode.getAttribute( "statusSupported" );
        if ( null != statusSupportedAttribute && !"".equals( statusSupportedAttribute ) ) {
            statusSupported = Boolean.valueOf( statusSupportedAttribute );
        }

        // Get optional node <wps:DataInputs> from <wps:ProcessDescription> node

        Element dataInputsNode = XMLTools.getRequiredElement( processDescriptionNode, "wps:DataInputs", nsContext );

        if ( null != dataInputsNode ) {

            List<Element> inputNodesList = XMLTools.getElements( dataInputsNode, "wps:Input", nsContext );

            if ( null != inputNodesList && 0 != inputNodesList.size() ) {

                int size = inputNodesList.size();
                dataInputs = new DataInputs();
                List<InputDescription> inputDescriptions = new ArrayList<InputDescription>( size );
                for ( int i = 0; i < size; i++ ) {
                    if ( inputNodesList.get( i ) != null ) {
                        inputDescriptions.add( i, getInputDescription( inputNodesList.get( i ) ) );
                    }
                }
                dataInputs.setInputDescriptions( inputDescriptions );
            }
        }

        // Get mandatory node <wps:ProcessOutputs> from <wps:ProcessDescription>
        // node.

        Element processOutputsNode = (Element) XMLTools.getRequiredNode( processDescriptionNode, "wps:ProcessOutputs",
                                                                         nsContext );

        List<Element> outputNodesList = XMLTools.getRequiredElements( processOutputsNode, "wps:Output", nsContext );
        int size = outputNodesList.size();
        processOutputs = new ProcessOutputs();
        processOutputs.output = new ArrayList<OutputDescription>( size );
        for ( int i = 0; i < size; i++ ) {
            processOutputs.output.add( i, getOutputDescription( outputNodesList.get( i ) ) );
        }

        return new ProcessDescription( responsibleClass, identifier, title, _abstract, processVersion,
                                       metadataTypeList, dataInputs, processOutputs, statusSupported, storeSupported );

    }

    private String getResponsibleClass( Element processDescriptionNode )
                            throws XMLParsingException {

        // Get resonsible class for process execution from deegreeParams section
        String responsibleClass = null;
        Element deegreeParamsNode = (Element) XMLTools.getRequiredNode( processDescriptionNode,
                                                                        "deegreewps:deegreeParams", nsContext );
        responsibleClass = XMLTools.getRequiredNodeAsString( deegreeParamsNode, "deegreewps:responsibleClass/text()",
                                                             nsContext );

        return responsibleClass;
    }

    /**
     * @param e
     *            processDescriptionNode
     * @throws XMLParsingException
     */
    private List<MetadataType> getMetadata( Element e )
                            throws XMLParsingException {
        List<MetadataType> metadataTypeList = null;

        // Get optional nodes <ows:Metadata>
        List<Element> metadataTypeNodes = XMLTools.getElements( e, "ows:Metadata", nsContext );
        if ( null != metadataTypeNodes && 0 != metadataTypeNodes.size() ) {
            int size = metadataTypeNodes.size();
            metadataTypeList = new ArrayList<MetadataType>( size );
            for ( int i = 0; i < size; i++ ) {
                metadataTypeList.add( i, getMetadataType( metadataTypeNodes.get( i ) ) );
            }
        }
        return metadataTypeList;
    }

    /**
     * @param e
     *            processDescriptionNode
     * @throws XMLParsingException
     */
    private String getAbstract( Element e )
                            throws XMLParsingException {
        String _abstract = null;

        // Get optional node <ows:Abstract>
        String owsAbstract = XMLTools.getNodeAsString( e, "ows:Abstract/text()", nsContext, null );
        if ( null != owsAbstract && !"".equals( owsAbstract ) ) {
            _abstract = owsAbstract;
        }
        return _abstract;
    }

    /**
     * @param e
     *            processDescriptionNode
     * @throws XMLParsingException
     */
    private String getTitle( Element e )
                            throws XMLParsingException {
        // Get required node <ows:Title>
        return XMLTools.getRequiredNodeAsString( e, "ows:Title/text()", nsContext );
    }

    /**
     * @param e
     *            processDescriptionNode
     * @throws XMLParsingException
     */
    private Code getIdentifier( Element e )
                            throws XMLParsingException {
        // Get required node <ows:Identifier>
        String identifierAsString = XMLTools.getRequiredNodeAsString( e, "ows:Identifier/text()", nsContext );
        return new Code( identifierAsString, null );
    }

    /**
     * Creates an object representation of a <code>ows:Metadata</code> section.
     *
     * @param metadataTypeNode
     * @return object representation of the <code>ows:Metadata</code> section
     */
    private MetadataType getMetadataType( Element metadataTypeNode ) {

        // FIXME MetadataType contained in Deegree does not correspond with
        // current OWS MetadataType definition
        MetadataType metadataType = null;

        // Only attribute xlink:title supported by now, e.g. <ows:Metadata
        // xlink:title="buffer"/>
        String title = metadataTypeNode.getAttributeNS( XLNNS.toString(), "title" );

        if ( null != title && !"".equals( title ) ) {
            metadataType = new MetadataType();
            metadataType.value = title;
        }

        return metadataType;
    }

    private InputDescription getInputDescription( Element inputDescriptionNode )
                            throws XMLParsingException {

        Code identifier = getIdentifier( inputDescriptionNode );

        String title = getTitle( inputDescriptionNode );

        String _abstract = getAbstract( inputDescriptionNode );

        ComplexData complexData = null;

        LiteralInput literalData = null;

        SupportedCRSs boundingBoxData = null;

        Element boundingBoxDataNode = (Element) XMLTools.getNode( inputDescriptionNode, "wps:BoundingBoxData",
                                                                  nsContext );

        Element complexDataNode = (Element) XMLTools.getNode( inputDescriptionNode, "wps:ComplexData", nsContext );

        Element literalDataNode = (Element) XMLTools.getNode( inputDescriptionNode, "wps:LiteralData", nsContext );

        if ( null == boundingBoxDataNode && null == complexDataNode && null == literalDataNode ) {
            throw new XMLParsingException(
                                           "A required data type is missing, one of wps:ComplexData or wps:LiteralData is missing from inputDescriptionNode with localname: "
                                                                   + inputDescriptionNode.getLocalName() );
        }

        if ( null != boundingBoxDataNode && null == complexDataNode && null == literalDataNode ) {
            boundingBoxData = getSupportedCRSsType( boundingBoxDataNode );
        }
        if ( null == boundingBoxDataNode && null != complexDataNode && null == literalDataNode ) {
            complexData = getComplexDataType( complexDataNode );
        }
        if ( null == boundingBoxDataNode && null == complexDataNode && null != literalDataNode ) {
            literalData = getLiteralInputType( literalDataNode );

        }
        int occurs = XMLTools.getNodeAsInt( inputDescriptionNode, "wps:MinimumOccurs/text()", nsContext, 1 );

        return new InputDescription( identifier, title, _abstract, boundingBoxData, complexData, literalData, occurs );
    }

    @SuppressWarnings("unchecked")
    private SupportedCRSs getSupportedCRSsType( Element boundingBoxDataNode )
                            throws XMLParsingException {

        List<URI> crsList = null;

        // Get required nodes <wps:CRS>
        List<Element> crsNodes = XMLTools.getRequiredElements( boundingBoxDataNode, "wps:CRS", nsContext );
        if ( null != crsNodes && 0 != crsNodes.size() ) {
            int size = crsNodes.size();
            crsList = new ArrayList<URI>( size );
            for ( int i = 0; i < size; i++ ) {

                String crs = XMLTools.getNodeAsString( crsNodes.get( i ), "/text()", nsContext, null );

                crsList.add( i, buildURIFromString( crs ) );
            }
        }

        // Get required attribute "defaultCRS" from node <wps:BoundingBoxData>
        URI defaultCRS = buildURIFromString( boundingBoxDataNode.getAttribute( "defaultCRS" ) );

        return new SupportedCRSs( crsList, defaultCRS );

    }

    /**
     * @param complexDataNode
     * @return the complex data bean created from the node.
     * @throws XMLParsingException
     */
    private ComplexData getComplexDataType( Element complexDataNode )
                            throws XMLParsingException {
        String defaultEncoding = null;
        String defaultFormat = null;
        String defaultSchema = null;
        List<SupportedComplexData> supportedComplexDataList = null;

        // Get optional attribute "defaultFormat" from <wps:ComplexData> node
        String defaultFormatAttribute = complexDataNode.getAttribute( "defaultFormat" );
        if ( null != defaultFormatAttribute && !"".equals( defaultFormatAttribute ) ) {
            defaultFormat = defaultFormatAttribute;
        }

        // Get optional attribute "defaultEncoding" from <wps:ComplexData> node
        String defaultEncodingAttribute = complexDataNode.getAttribute( "defaultEncoding" );
        if ( null != defaultEncodingAttribute && !"".equals( defaultEncodingAttribute ) ) {
            defaultEncoding = defaultEncodingAttribute;
        }

        // Get optional attribute "defaultSchema" from <wps:ComplexData> node
        String defaultSchemaAttribute = complexDataNode.getAttribute( "defaultSchema" );
        if ( null != defaultSchemaAttribute && !"".equals( defaultSchemaAttribute ) ) {
            defaultSchema = defaultSchemaAttribute;
        }

        List<Element> supportedComplexDataNodes = XMLTools.getElements( complexDataNode, "wps:SupportedComplexData",
                                                                        nsContext );
        if ( null != supportedComplexDataNodes && 0 != supportedComplexDataNodes.size() ) {
            int size = supportedComplexDataNodes.size();
            supportedComplexDataList = new ArrayList<SupportedComplexData>( size );
            for ( int i = 0; i < size; i++ ) {
                supportedComplexDataList.add( i, getSupportedComplexData( supportedComplexDataNodes.get( i ) ) );
            }
        }

        return new ComplexData( defaultEncoding, defaultFormat, defaultSchema, supportedComplexDataList );
    }

    /**
     * @param supportedComplexDataNode
     *            element
     * @return the bean created from the element
     * @throws XMLParsingException
     */
    private SupportedComplexData getSupportedComplexData( Element supportedComplexDataNode )
                            throws XMLParsingException {
        String encoding = null;
        String format = null;
        String schema = null;

        // Get optional node <wps:Encoding>
        String wpsEncoding = XMLTools.getNodeAsString( supportedComplexDataNode, "wps:Encoding/text()", nsContext, null );
        if ( null != wpsEncoding && !"".equals( wpsEncoding ) ) {
            encoding = wpsEncoding;
        }

        // Get optional node <wps:Format>
        String wpsFormat = XMLTools.getNodeAsString( supportedComplexDataNode, "wps:Format/text()", nsContext, null );
        if ( null != wpsFormat && !"".equals( wpsFormat ) ) {
            format = wpsFormat;
        }

        // Get optional node <wps:Schema>
        String wpsSchema = XMLTools.getNodeAsString( supportedComplexDataNode, "wps:Schema/text()", nsContext, null );
        if ( null != wpsSchema && !"".equals( wpsSchema ) ) {
            schema = wpsSchema;
        }
        return new SupportedComplexData( encoding, format, schema );
    }

    /**
     * @return the bean created from the element
     */
    private LiteralInput getLiteralInputType( Element literalDataNode )
                            throws XMLParsingException {
        OWSMetadata domainMetadataType = null;
        SupportedUOMs supportedUOMsType = null;
        OWSAllowedValues allowedValues = null;
        boolean anyValueAllowed = false;
        ValueRange defaultValue = null;
        OWSMetadata valuesReference = null;

        // Get optional node <ows:DataType>
        Element dataTypeNode = (Element) XMLTools.getNode( literalDataNode, "ows:DataType", nsContext );
        if ( null != dataTypeNode ) {
            domainMetadataType = getDomainMetadataTypeFromContent( dataTypeNode );
        }

        // Get optional node <wps:SupportedUOMs>
        Element supportedUOMsNode = (Element) XMLTools.getNode( literalDataNode, "wps:SupportedUOMs", nsContext );
        if ( null != supportedUOMsNode ) {
            supportedUOMsType = getSupportedUOMs( supportedUOMsNode );
        }

        // Get optional node <wps:AllowedValues>
        Element allowedValuesNode = (Element) XMLTools.getNode( literalDataNode, "ows:AllowedValues", nsContext );
        // Get optional node <wps:AnyValue>
        Element anyValueNode = (Element) XMLTools.getNode( literalDataNode, "ows:AnyValue", nsContext );
        // Get optional node <wps:ValuesReference>
        Element valuesReferenceNode = (Element) XMLTools.getNode( literalDataNode, "ows:ValuesReference", nsContext );

        if ( null != allowedValuesNode && null == anyValueNode && null == valuesReferenceNode ) {
            allowedValues = getOWSAllowedValues( allowedValuesNode );
        } else if ( null == allowedValuesNode && null != anyValueNode && null == valuesReferenceNode ) {
            anyValueAllowed = true;
        } else if ( null == allowedValuesNode && null == anyValueNode && null != valuesReferenceNode ) {
            String reference = valuesReferenceNode.getAttributeNS( OWSNS.toString(), "reference" );
            String value = XMLTools.getNodeAsString( valuesReferenceNode, "/text()", nsContext, null );
            if ( null != value ) {
                URI referenceURI = buildURIFromString( reference );
                valuesReference = new OWSMetadata( null, new SimpleLink( referenceURI ), value );
            }
        } else {
            throw new XMLParsingException(
                                           "A required data type is missing, one of ows:AllowedValues, ows:AnyValue or ows:ValuesReference is missing from context node: "
                                                                   + literalDataNode.getLocalName() );
        }

        // Get optional node <wps:DefaultValue>
        Element defaultValueNode = (Element) XMLTools.getNode( literalDataNode, "ows:DefaultValue", nsContext );
        if ( null != defaultValueNode ) {
            defaultValue = getOwsRange( defaultValueNode );
        }

        return new LiteralInput( domainMetadataType, supportedUOMsType, allowedValues, anyValueAllowed, defaultValue,
                                 valuesReference );
    }

    private URI buildURIFromString( String reference )
                            throws XMLParsingException {
        URI referenceURI = null;
        try {
            referenceURI = new URI( reference );
        } catch ( URISyntaxException e ) {
            String msg = "The URI syntax is malformed. " + e.getMessage();
            LOG.logError( msg );
            throw new XMLParsingException( msg, e );
        }
        return referenceURI;
    }

    private SupportedUOMs getSupportedUOMs( Element supportedUOMsNode )
                            throws XMLParsingException {
        List<Element> uomNodesList = XMLTools.getElements( supportedUOMsNode, "ows:UOM", nsContext );

        List<OWSMetadata> domainMetadataTypeList = null;
        if ( null != uomNodesList && 0 != uomNodesList.size() ) {
            int uomNodesListSize = uomNodesList.size();
            domainMetadataTypeList = new ArrayList<OWSMetadata>( uomNodesListSize );
            for ( int i = 0; i < uomNodesListSize; i++ ) {
                Element nodeListElement = uomNodesList.get( i );

                domainMetadataTypeList.add( i, getDomainMetadataTypeFromAttribute( nodeListElement ) );
            }
        }
        String defaultuom = supportedUOMsNode.getAttribute( "defaultUOM" );
        URI defaultUOMURI = buildURIFromString( defaultuom );
        OWSMetadata defaultUOMObject = new OWSMetadata( null, new SimpleLink( defaultUOMURI ), null );

        return new SupportedUOMs( defaultUOMObject, domainMetadataTypeList );
    }

    private OWSMetadata getDomainMetadataTypeFromContent( Element e )
                            throws XMLParsingException {
        String owsDataType = XMLTools.getNodeAsString( e, "/text()", nsContext, null );
        String reference = e.getAttributeNS( OWSNS.toString(), "reference" );
        URI referenceURI = buildURIFromString( reference );
        return new OWSMetadata( null, new SimpleLink( referenceURI ), owsDataType );
    }

    private OWSMetadata getDomainMetadataTypeFromAttribute( Element e )
                            throws XMLParsingException {
        String reference = e.getAttributeNS( OWSNS.toString(), "reference" );
        URI referenceURI = buildURIFromString( reference );

        return new OWSMetadata( null, new SimpleLink( referenceURI ), null );
    }

    @SuppressWarnings("unchecked")
    private OWSAllowedValues getOWSAllowedValues( Element e )
                            throws XMLParsingException {
        TypedLiteral[] owsValues = null;
        ValueRange[] valueRanges = null;

        // gets a Node list of type ows:Value
        List owsValueNodeList = XMLTools.getNodes( e, "ows:Value", nsContext );

        if ( null != owsValueNodeList && 0 != owsValueNodeList.size() ) {
            int size = owsValueNodeList.size();
            owsValues = new TypedLiteral[size];
            for ( int i = 0; i < size; i++ ) {
                owsValues[i] = ( getOwsValue( (Element) owsValueNodeList.get( i ) ) );
            }
        }

        List owsRangeNodeList = XMLTools.getNodes( e, "ows:Range", nsContext );

        if ( null != owsRangeNodeList && 0 != owsRangeNodeList.size() ) {
            int size = owsRangeNodeList.size();
            valueRanges = new ValueRange[size];
            for ( int i = 0; i < size; i++ ) {
                valueRanges[i] = ( getOwsRange( (Element) owsRangeNodeList.get( i ) ) );
            }
        }

        return new OWSAllowedValues( owsValues, valueRanges );
    }

    /**
     * @param element
     * @return the bean created from the element
     * @throws XMLParsingException
     */
    private ValueRange getOwsRange( Element element )
                            throws XMLParsingException {

        TypedLiteral maximum = getOwsValue( (Element) XMLTools.getNode( element, "ows:MaximumValue", nsContext ) );

        TypedLiteral minimum = getOwsValue( (Element) XMLTools.getNode( element, "ows:MinimumValue", nsContext ) );

        Closure rangeClosure = null;

        String rangeClosureAttribute = element.getAttributeNS( OWSNS.toString(), "rangeClosure" );
        if ( "closed".equalsIgnoreCase( rangeClosureAttribute ) ) {
            rangeClosure = new Closure( Closure.CLOSED );
        } else if ( "open".equalsIgnoreCase( rangeClosureAttribute ) ) {
            rangeClosure = new Closure( Closure.OPENED );
        } else if ( "closed-open".equalsIgnoreCase( rangeClosureAttribute ) ) {
            rangeClosure = new Closure( Closure.CLOSED_OPENED );
        } else if ( "open-closed".equalsIgnoreCase( rangeClosureAttribute ) ) {
            rangeClosure = new Closure( Closure.OPENED_CLOSED );
        } else {
            throw new XMLParsingException( "Attribute range closure contains invalid value." );
        }

        TypedLiteral spacing = null;

        Element spacingNode = (Element) XMLTools.getNode( element, "ows:Spacing", nsContext );
        if ( null != spacingNode ) {
            spacing = getOwsValue( spacingNode );
        }

        return new ValueRange( minimum, maximum, spacing, null, null, false, rangeClosure );
    }

    /**
     * @param element
     * @return the bean created from the element.
     * @throws XMLParsingException
     */
    private TypedLiteral getOwsValue( Element element )
                            throws XMLParsingException {
        String value = XMLTools.getNodeAsString( element, "/text()", nsContext, null );
        return new TypedLiteral( value, null );
    }

    /**
     * @param outputDescriptionNode
     *            element
     * @return the bean created from the element.
     * @throws XMLParsingException
     */
    private OutputDescription getOutputDescription( Element outputDescriptionNode )
                            throws XMLParsingException {
        Code identifier = getIdentifier( outputDescriptionNode );

        String title = getTitle( outputDescriptionNode );

        String _abstract = getAbstract( outputDescriptionNode );

        Element boundingBoxOutputNode = (Element) XMLTools.getNode( outputDescriptionNode, "wps:BoundingBoxOutput",
                                                                    nsContext );
        Element complexOutputNode = (Element) XMLTools.getNode( outputDescriptionNode, "wps:ComplexOutput", nsContext );
        Element literalOutputNode = (Element) XMLTools.getNode( outputDescriptionNode, "wps:LiteralOutput", nsContext );

        SupportedCRSs boundingBoxOutput = null;
        ComplexData complexOutput = null;
        LiteralOutput literalOutput = null;

        if ( null != boundingBoxOutputNode && null == complexOutputNode && null == literalOutputNode ) {
            boundingBoxOutput = getSupportedCRSsType( boundingBoxOutputNode );
        } else if ( null == boundingBoxOutputNode && null != complexOutputNode && null == literalOutputNode ) {
            complexOutput = getComplexDataType( complexOutputNode );
        } else if ( null == boundingBoxOutputNode && null == complexOutputNode && null != literalOutputNode ) {
            Element dataTypeNode = (Element) XMLTools.getNode( literalOutputNode, "ows:DataType", nsContext );
            OWSMetadata domainMetadataType = getDomainMetadataTypeFromContent( dataTypeNode );
            Element supportedUOMsNode = (Element) XMLTools.getNode( literalOutputNode, "wps:SupportedUOMs", nsContext );
            SupportedUOMs supportedUOMsType = null;
            if ( null != supportedUOMsNode ) {
                supportedUOMsType = getSupportedUOMs( supportedUOMsNode );
            }
            literalOutput = new LiteralOutput( domainMetadataType, supportedUOMsType );
        } else {
            throw new XMLParsingException(
                                           "A required data type is missing, one of wps:BoundingBoxOutput, wps:ComplexOutput or wps:LiteralOutput is missing from context node: "
                                                                   + outputDescriptionNode.getLocalName() );
        }

        return new OutputDescription( identifier, title, _abstract, boundingBoxOutput, complexOutput, literalOutput );
    }
}
