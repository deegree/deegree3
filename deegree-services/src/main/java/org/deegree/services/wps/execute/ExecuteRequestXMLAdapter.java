//$Header: /deegreerepository/deegree/resources/eclipse/svn_classfile_header_template.xml,v 1.2 2007/03/06 09:44:09 bezema Exp $
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

package org.deegree.services.wps.execute;

import static org.deegree.protocol.wps.WPSConstants.WPS_100_NS;
import static org.deegree.protocol.wps.WPSConstants.WPS_PREFIX;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.ows.OWSCommonXMLAdapter;
import org.deegree.protocol.wps.WPSConstants;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.jaxb.wps.BoundingBoxInputDefinition;
import org.deegree.services.jaxb.wps.ComplexFormatType;
import org.deegree.services.jaxb.wps.ComplexInputDefinition;
import org.deegree.services.jaxb.wps.ComplexOutputDefinition;
import org.deegree.services.jaxb.wps.LiteralInputDefinition;
import org.deegree.services.jaxb.wps.ProcessDefinition;
import org.deegree.services.jaxb.wps.ProcessletInputDefinition;
import org.deegree.services.jaxb.wps.ProcessletOutputDefinition;
import org.deegree.services.jaxb.wps.LiteralInputDefinition.OtherUOM;
import org.deegree.services.jaxb.wps.ProcessDefinition.InputParameters;
import org.deegree.services.jaxb.wps.ProcessDefinition.OutputParameters;
import org.deegree.services.wps.DefaultExceptionCustomizer;
import org.deegree.services.wps.ExceptionCustomizer;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.WPSProcess;
import org.deegree.services.wps.input.BoundingBoxInput;
import org.deegree.services.wps.input.BoundingBoxInputImpl;
import org.deegree.services.wps.input.EmbeddedComplexInput;
import org.deegree.services.wps.input.InputReference;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.input.LiteralInputImpl;
import org.deegree.services.wps.input.ProcessletInput;
import org.deegree.services.wps.input.ReferencedComplexInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser and validator for incoming WPS <code>Execute</code> XML requests.
 * <p>
 * Besides the general syntax, the following aspects are validated during parsing:
 * <ul>
 * <li>Process identifier: must refer to a known process <code>p</code></li>
 * <li>Input parameters: each present input parameter must be defined in the definition of <code>p</code></li>
 * <li>Output parameters: TBD</li>
 * </ul>
 * In case of a detected error, an appropriate {@link OWSException} is thrown.
 * </p>
 * 
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: padberg$
 * 
 * @version $Revision$, $Date: 08.05.2008 13:53:13$
 */
public class ExecuteRequestXMLAdapter extends OWSCommonXMLAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( ExecuteRequestXMLAdapter.class );

    private static NamespaceContext nsContext;

    static {
        nsContext = new NamespaceContext( XMLAdapter.nsContext );
        nsContext.addNamespace( OWS_PREFIX, OWS110_NS );
        nsContext.addNamespace( WPS_PREFIX, WPS_100_NS );
    }

    private Map<CodeType, WPSProcess> idToProcess;

    /**
     * Creates a new {@link ExecuteRequestXMLAdapter} for parsing execute requests for the submitted processes.
     * 
     * @param idToProcess
     *            key: process identifier, value: process
     */
    public ExecuteRequestXMLAdapter( Map<CodeType, WPSProcess> idToProcess ) {
        this.idToProcess = idToProcess;
    }

    /**
     * Parses the encapsulated WPS 1.0.0 &lt;<code>wps:ExecuteRequest</code>&gt; element.
     * <p>
     * Prerequisites (not checked by this method):
     * <ul>
     * <li>The name of the encapsulated element is &lt;<code>wps:ExecuteRequest</code>&gt;
     * (wps="http://www.opengis.net/wps/1.0.0").</li>
     * <li>The value of the <code>version</code> attribute of the element is <code>1.0.0</code>.</li>
     * </p>
     * 
     * @return corresponding <code>ExecuteRequest</code> object
     * @throws XMLParsingException
     *             if a syntactical or semantical error has been encountered in the request document
     * @throws OWSException
     * @throws UnknownCRSException
     */
    public ExecuteRequest parse100()
                            throws OWSException, UnknownCRSException {

        // "language" attribute (optional)
        String language = getNodeAsString( rootElement, new XPath( "@language", nsContext ), null );

        // "ows:Identifier" element (minOccurs="1", maxOccurs="1")
        CodeType identifier = parseRequiredIdentifier( rootElement, new DefaultExceptionCustomizer( null ) );
        WPSProcess process = lookupProcess( identifier );
        ProcessDefinition processDef = process.getDescription();
        ExceptionCustomizer eCustomizer = process.getExceptionCustomizer() == null ? new DefaultExceptionCustomizer(
                                                                                                                     null )
                                                                                  : process.getExceptionCustomizer();

        // "wps:DataInputs" element (minOccurs="0", maxOccurs="1")
        ProcessletInputs dataInputs = null;
        OMElement dataInputsElement = getElement( rootElement, new XPath( "wps:DataInputs", nsContext ) );
        if ( dataInputsElement != null ) {
            dataInputs = parseDataInputs( dataInputsElement, processDef, eCustomizer );
        }

        // "wps:ResponseForm" element (minOccurs="0", maxOccurs="1")
        ResponseForm responseForm = null;
        OMElement responseFormElement = getElement( rootElement, new XPath( "wps:ResponseForm", nsContext ) );
        if ( responseFormElement != null ) {
            responseForm = parseResponseForm( responseFormElement, processDef, eCustomizer );
        }

        return new ExecuteRequest( WPSConstants.VERSION_100, language, processDef, dataInputs, responseForm );
    }

    private ProcessletInputs parseDataInputs( OMElement dataInputsElement, ProcessDefinition processDef,
                                              ExceptionCustomizer eCustomizer )
                            throws OWSException {

        // key: id of input parameter, value: number of occurences
        Map<CodeType, Integer> inputIdToCount = new HashMap<CodeType, Integer>();

        // "wps:Input" elements (minOccurs="1", maxOccurs="unbounded")

        List<OMElement> inputElements = null;
        try {
            inputElements = getRequiredElements( dataInputsElement, new XPath( "wps:Input", nsContext ) );
        } catch ( XMLParsingException e ) {
            throw eCustomizer.missingParameter( new QName( WPS_100_NS, "Input" ).toString() );
        }
        List<ProcessletInput> inputs = new ArrayList<ProcessletInput>( inputElements.size() );
        for ( OMElement inputElement : inputElements ) {
            ProcessletInput input = parseInput( inputElement, processDef, eCustomizer );
            inputs.add( input );

            CodeType inputId = input.getIdentifier();
            Integer count = inputIdToCount.get( inputId );
            if ( count == null ) {
                count = 1;
            } else {
                count++;
            }
            inputIdToCount.put( inputId, count );
        }

        // validate cardinalities of present input parameters
        for ( JAXBElement<? extends ProcessletInputDefinition> el : processDef.getInputParameters().getProcessInput() ) {
            ProcessletInputDefinition inputDef = el.getValue();
            CodeType inputId = new CodeType( inputDef.getIdentifier().getValue(),
                                             inputDef.getIdentifier().getCodeSpace() );
            int minOccurs = inputDef.getMinOccurs() != null ? inputDef.getMinOccurs().intValue() : 1;
            int maxOccurs = inputDef.getMaxOccurs() != null ? inputDef.getMaxOccurs().intValue() : 1;
            int actualOccurs = inputIdToCount.get( inputId ) != null ? inputIdToCount.get( inputId ) : 0;
            if ( actualOccurs < minOccurs || actualOccurs > maxOccurs ) {
                throw eCustomizer.inputInvalidOccurence( inputId, minOccurs, maxOccurs, actualOccurs );
            }
        }
        return new ProcessletInputs( inputs );
    }

    private ProcessletInput parseInput( OMElement inputElement, ProcessDefinition processDef,
                                        ExceptionCustomizer eCustomizer )
                            throws OWSException {

        // "ows:Identifier" element (minOccurs="1", maxOccurs="1")
        CodeType inputId = parseRequiredIdentifier( inputElement, eCustomizer );
        ProcessletInputDefinition definition = lookupInputDefinition( inputId, processDef, eCustomizer );

        // "ows:Title" element (minOccurs="0", maxOccurs="1")
        LanguageString title = null;
        OMElement titleElement = inputElement.getFirstChildWithName( new QName( OWS110_NS, "Title" ) );
        if ( titleElement != null ) {
            title = parseLanguageString( titleElement );
        }

        // "ows:Abstract" element (minOccurs="0", maxOccurs="1")
        LanguageString summary = null;
        OMElement abstractElement = inputElement.getFirstChildWithName( new QName( OWS110_NS, "Abstract" ) );
        if ( abstractElement != null ) {
            summary = parseLanguageString( abstractElement );
        }

        // choice: "wps:Data" or "wps:Reference" element (minOccurs="1", maxOccurs="1")
        OMElement dataElement = inputElement.getFirstChildWithName( new QName( WPS_100_NS, "Data" ) );
        OMElement referenceElement = inputElement.getFirstChildWithName( new QName( WPS_100_NS, "Reference" ) );

        ProcessletInput input = null;
        if ( dataElement != null && referenceElement == null ) {
            OMElement childElement = dataElement.getFirstElement();
            if ( childElement == null || !WPS_100_NS.equals( childElement.getNamespace().getNamespaceURI() ) ) {
                String allowedInputParam = ( definition instanceof LiteralInputDefinition ) ? wpsElement( "LiteralData" )
                                                                                           : ( definition instanceof ComplexInputDefinition ) ? wpsElement( "ComplexData" )
                                                                                                                                             : wpsElement( "BoundingBoxData" );

                throw eCustomizer.inputMissingParameter( inputId, allowedInputParam );
            }
            String name = childElement.getLocalName();
            if ( definition instanceof LiteralInputDefinition ) {
                if ( !"LiteralData".equals( name ) ) {
                    throw eCustomizer.inputMissingParameter( inputId, wpsElement( "LiteralInput" ) + "/"
                                                                      + wpsElement( "LiteralData" ) );
                }
                input = parseLiteralInput( (LiteralInputDefinition) definition, childElement, title, summary,
                                           eCustomizer );
            } else if ( definition instanceof BoundingBoxInputDefinition ) {
                if ( !"BoundingBoxData".equals( name ) ) {
                    throw eCustomizer.inputMissingParameter( inputId, wpsElement( "BoundingBox" ) + "/"
                                                                      + wpsElement( "BoundingBoxData" ) );
                }
                input = parseBoundingBoxData( (BoundingBoxInputDefinition) definition, childElement, title, summary,
                                              eCustomizer );
            } else if ( definition instanceof ComplexInputDefinition ) {
                if ( !"ComplexData".equals( name ) ) {
                    throw eCustomizer.inputMissingParameter( inputId, wpsElement( "ComplexInput" ) + "/"
                                                                      + wpsElement( "ComplexData" ) );
                }
                input = parseComplexData( (ComplexInputDefinition) definition, childElement, title, summary,
                                          eCustomizer );
            }
        } else if ( dataElement == null && referenceElement != null ) {
            if ( definition instanceof ComplexInputDefinition ) {
                input = parseInputReference( (ComplexInputDefinition) definition, referenceElement, title, summary,
                                             eCustomizer );
            } else {
                String href = referenceElement.getAttributeValue( new QName( XLN_NS, "href" ) );
                Pair<String, String> kvp = new Pair<String, String>( new QName( WPS_100_NS, "Reference" ).toString(),
                                                                     href );
                throw eCustomizer.inputEvalutationNotSupported( inputId, kvp,
                                                                "Reference may only be used with complex data." );

            }
        } else if ( dataElement != null && referenceElement != null ) {
            throw eCustomizer.mutualExclusive( wpsElement( "Data" ), wpsElement( "Reference" ) );
        } else {
            throw eCustomizer.inputMissingParameters( inputId, wpsElement( "Data" ), wpsElement( "Reference" ) );
        }
        return input;
    }

    private String wpsElement( String elementName ) {
        return new QName( WPS_100_NS, elementName ).toString();
    }

    /**
     * Parses and validates the given <code>wps:LiteralData</code> element as a {@link LiteralInput} object.
     * <p>
     * The following "semantical" properties are validated by this method:
     * <ul>
     * <li>The specified UOM (unit-of-measure) must be supported according to the given {@link LiteralInputDefinition}.
     * If it is not specified (uom attribute is missing), the UOM from the definition is assumed.</li>
     * <li>If the dataType attribute is present, it must have the same value as specified by the
     * {@link LiteralInputDefinition}.</li>
     * </ul>
     * </p>
     * 
     * @param definition
     *            input parameter definition (from process description)
     * @param literalDataElement
     *            <code>wps:LiteralData</code> element to be parsed
     * @param title
     *            title for the input parameter (may be null)
     * @param summary
     *            abstract (narrative description) for the input parameter (may be null)
     * @param eCustomizer
     * @return corresponding {@link LiteralInput} object
     * @throws OWSException
     *             if the element is not valid
     */
    private LiteralInput parseLiteralInput( LiteralInputDefinition definition, OMElement literalDataElement,
                                            LanguageString title, LanguageString summary,
                                            ExceptionCustomizer eCustomizer )
                            throws OWSException {

        CodeType identifier = getIdentifier( definition );

        // text value
        String value = literalDataElement.getText();

        // "dataType" attribute (optional)
        String dataType = literalDataElement.getAttributeValue( new QName( "dataType" ) );
        String definedDataType = definition.getDataType() != null ? definition.getDataType().getValue() : dataType;
        if ( dataType != null && !dataType.equals( definedDataType ) ) {
            throw eCustomizer.inputInvalidDatatype( identifier, dataType, definedDataType );
        }

        // rb: Evaluating the value of the parameter should be done here (if allowed values were defined in the process
        // description!!!).
        if ( definition.getAllowedValues() != null && !definition.getAllowedValues().getValueOrRange().isEmpty() ) {
            LOG.warn( identifier + ", validating supplied value: " + value
                      + " against the allowed values is not yet implemented." );
        }

        // "uom" attribute (optional)
        String uom = literalDataElement.getAttributeValue( new QName( "uom" ) );
        if ( uom == null ) {
            // not specified -> use default UOM from parameter definition
            uom = definition.getDefaultUOM() != null ? definition.getDefaultUOM().getValue() : null;
        } else {
            // validate against parameter definition
            Set<String> supportedUOMs = new HashSet<String>();
            if ( definition.getDefaultUOM() != null ) {
                supportedUOMs.add( definition.getDefaultUOM().getValue() );
                for ( OtherUOM otherUOM : definition.getOtherUOM() ) {
                    supportedUOMs.add( otherUOM.getValue() );
                }
            }
            if ( !supportedUOMs.contains( uom ) ) {
                throw eCustomizer.inputInvalidParameter( identifier, new Pair<String, String>( "@uom", uom ) );
            }
        }

        return new LiteralInputImpl( definition, title, summary, value, uom );
    }

    /**
     * Parses and validates the given <code>wps:BoundingBoxData</code> element as a {@link BoundingBoxInput} object.
     * <p>
     * This method validates that the specified CRS name is supported according to the input parameter definition.
     * </p>
     * 
     * @param definition
     *            input parameter definition (from process description)
     * @param boundingBoxDataElement
     *            <code>wps:BoundingBoxData</code> element to be parsed
     * @param title
     *            title for the input parameter (may be null)
     * @param summary
     *            abstract (narrative description) for the input parameter (may be null)
     * @param eCustomizer
     * @return corresponding {@link BoundingBoxInput} object
     * @throws OWSException
     *             if the element is not valid
     */
    private BoundingBoxInput parseBoundingBoxData( BoundingBoxInputDefinition definition,
                                                   OMElement boundingBoxDataElement, LanguageString title,
                                                   LanguageString summary, ExceptionCustomizer eCustomizer )
                            throws OWSException {

        // 'crs' attribute (optional)
        String crs = boundingBoxDataElement.getAttributeValue( new QName( "crs" ) );
        if ( crs == null ) {
            // not specified -> use default CRS from parameter definition
            crs = definition.getDefaultCRS();
        } else {
            // validate against parameter definition
            Set<String> supportedCRS = new HashSet<String>();
            supportedCRS.add( definition.getDefaultCRS() );
            for ( String otherCRS : definition.getOtherCRS() ) {
                supportedCRS.add( otherCRS );
            }
            if ( !supportedCRS.contains( crs ) ) {
                throw eCustomizer.inputInvalidParameter( getIdentifier( definition ), new Pair<String, String>( "@crs",
                                                                                                                crs ) );
            }
        }

        Envelope bbox = parseBoundingBoxType( boundingBoxDataElement, new CRS( crs ) );
        return new BoundingBoxInputImpl( definition, title, summary, bbox );
    }

    /**
     * Parses and validates the given <code>wps:ComplexData</code> element as an {@link EmbeddedComplexInput} object.
     * <p>
     * The following "semantical" properties are validated by this method:
     * <ul>
     * <li>The specified UOM (unit-of-measure) must be supported according to the given {@link LiteralInputDefinition}.
     * If it is not specified (uom attribute is missing), the UOM from the definition is assumed.</li>
     * <li>If the dataType attribute is present, it must have the same value as specified by the
     * {@link LiteralInputDefinition}.</li>
     * </ul>
     * </p>
     * 
     * @param definition
     *            input parameter definition (from process description)
     * @param complexDataElement
     *            <code>wps:ComplexData</code> element to be parsed
     * @param title
     *            title for the input parameter (may be null)
     * @param summary
     *            abstract (narrative description) for the input parameter (may be null)
     * @param eCustomizer
     * @return corresponding {@link BoundingBoxInput} object
     * @throws OWSException
     *             if the element is not valid
     */
    private EmbeddedComplexInput parseComplexData( ComplexInputDefinition definition, OMElement complexDataElement,
                                                   LanguageString title, LanguageString summary,
                                                   ExceptionCustomizer eCustomizer )
                            throws OWSException {

        ComplexFormatType format = new ComplexFormatType();
        // "mimeType" attribute (optional)
        format.setMimeType( complexDataElement.getAttributeValue( new QName( "mimeType" ) ) );
        // "encoding" attribute (optional)
        format.setEncoding( complexDataElement.getAttributeValue( new QName( "encoding" ) ) );
        // "schema" attribute (optional)
        format.setSchema( complexDataElement.getAttributeValue( new QName( "schema" ) ) );

        format = validateAndAugmentFormat( format, definition, eCustomizer );

        return new EmbeddedComplexInput( definition, title, summary, format, complexDataElement );
    }

    private ReferencedComplexInput parseInputReference( ComplexInputDefinition definition, OMElement referenceElement,
                                                        LanguageString title, LanguageString summary,
                                                        ExceptionCustomizer eCustomizer )
                            throws OWSException {

        ComplexFormatType format = new ComplexFormatType();
        // "mimeType" attribute (optional)
        format.setMimeType( referenceElement.getAttributeValue( new QName( "mimeType" ) ) );
        // "encoding" attribute (optional)
        format.setEncoding( referenceElement.getAttributeValue( new QName( "encoding" ) ) );
        // "schema" attribute (optional)
        format.setSchema( getNodeAsString( referenceElement, new XPath( "@schema", nsContext ), null ) );

        format = validateAndAugmentFormat( format, definition, eCustomizer );

        // "xlink:href" attribute (required)
        URL href = null;
        try {
            href = getRequiredNodeAsURL( referenceElement, new XPath( "@xlink:href", nsContext ) );
        } catch ( XMLParsingException e ) {
            throw eCustomizer.inputMissingParameter( getIdentifier( definition ), wpsElement( "Reference" )
                                                                                  + "/@xlink:href" );
        }

        // "method" attribute (optional)
        boolean isPost = false;
        String method = getNodeAsString( referenceElement, new XPath( "@method", nsContext ), "GET" );
        if ( "GET".equals( method ) ) {
            isPost = false;
        } else if ( "POST".equals( method ) ) {
            isPost = true;
        } else {
            throw eCustomizer.invalidAttributedParameter( new Pair<String, String>( wpsElement( "Reference" )
                                                                                    + "/@method", method ) );
        }

        // "wps:Header" elements (minOccurs="0" maxOccurs="unbounded")
        Map<String, String> headers = new HashMap<String, String>();
        Iterator<?> headerIter = referenceElement.getChildrenWithName( new QName( "Header", WPS_100_NS ) );
        while ( headerIter.hasNext() ) {
            OMElement headerElement = (OMElement) headerIter.next();
            // "key" attribute (required)
            String key = null;
            try {
                key = getRequiredNodeAsString( headerElement, new XPath( "@key", nsContext ) );
            } catch ( XMLParsingException e ) {
                throw eCustomizer.inputMissingParameter( getIdentifier( definition ), wpsElement( "Header" ) + "/@key" );
            }
            // "value" attribute (required)
            String value = null;
            try {
                value = getRequiredNodeAsString( headerElement, new XPath( "@value", nsContext ) );
            } catch ( XMLParsingException e ) {
                throw eCustomizer.inputMissingParameter( getIdentifier( definition ), wpsElement( "Header" )
                                                                                      + "/@value" );
            }
            headers.put( key, value );
        }

        InputReference inputReference = null;
        if ( !isPost ) {
            // GET -> no further elements required
            inputReference = new InputReference( href, headers );
        } else {
            // POST -> choice: "wps:Body" or "wps:BodyReference" element (minOccurs="1", maxOccurs="1")
            OMElement bodyElement = referenceElement.getFirstChildWithName( new QName( WPS_100_NS, "Body" ) );
            OMElement bodyReferenceElement = referenceElement.getFirstChildWithName( new QName( WPS_100_NS,
                                                                                                "BodyReference" ) );
            if ( bodyElement != null && bodyReferenceElement == null ) {
                inputReference = new InputReference( href, headers, bodyElement );
            } else if ( bodyElement == null && bodyReferenceElement != null ) {
                // "xlink:href" attribute (required)
                URL bodyURL = null;
                try {
                    bodyURL = getRequiredNodeAsURL( bodyReferenceElement, new XPath( "@xlink:href", nsContext ) );
                } catch ( XMLParsingException e ) {
                    throw eCustomizer.inputMissingParameter( getIdentifier( definition ), wpsElement( "BodyReference" )
                                                                                          + "/@xlink:href" );
                }
                inputReference = new InputReference( href, headers, bodyURL );
            } else if ( bodyElement != null && bodyReferenceElement != null ) {
                throw eCustomizer.inputMutualExclusive( getIdentifier( definition ), wpsElement( "Body" ),
                                                        wpsElement( "BodyReference" ) );
            } else {
                throw eCustomizer.inputMissingParameters( getIdentifier( definition ), wpsElement( "Body" ),
                                                          wpsElement( "BodyReference" ) );
            }
        }

        return new ReferencedComplexInput( definition, title, summary, format, inputReference );
    }

    private CodeType getIdentifier( ProcessletInputDefinition definition ) {
        return new CodeType( definition.getIdentifier().getValue(), definition.getIdentifier().getCodeSpace() );
    }

    private ComplexFormatType validateAndAugmentFormat( ComplexFormatType format, ComplexInputDefinition definition,
                                                        ExceptionCustomizer eCustomizer )
                            throws OWSException {

        LOG.debug( "Looking up compatible format ('" + toString( format ) + "') in parameter definition." );
        List<ComplexFormatType> equalMimeType = null;
        if ( format.getMimeType() == null ) {
            // not specified -> assume mime type from default format
            equalMimeType = Collections.singletonList( definition.getDefaultFormat() );
        } else {
            equalMimeType = new LinkedList<ComplexFormatType>();
            if ( format.getMimeType().equals( definition.getDefaultFormat().getMimeType() ) ) {
                equalMimeType.add( definition.getDefaultFormat() );
            }
            for ( ComplexFormatType otherFormat : definition.getOtherFormats() ) {
                if ( format.getMimeType().equals( otherFormat.getMimeType() ) ) {
                    equalMimeType.add( format );
                }
            }
        }

        // no matching formats (mime type) found?
        if ( equalMimeType.isEmpty() ) {
            throw eCustomizer.inputInvalidParameter( getIdentifier( definition ),
                                                     new Pair<String, String>( "mimetype", format.getMimeType() ) );
        }

        List<ComplexFormatType> equalMimeTypeAndSchema = new LinkedList<ComplexFormatType>();
        for ( ComplexFormatType candidateFormat : equalMimeType ) {
            if ( format.getSchema() == null || format.getSchema().equals( candidateFormat.getSchema() ) ) {
                equalMimeTypeAndSchema.add( candidateFormat );
            }
        }

        // no matching formats (mime type and schema) found?
        if ( equalMimeTypeAndSchema.isEmpty() ) {
            List<Pair<String, String>> combi = new ArrayList<Pair<String, String>>();
            combi.add( new Pair<String, String>( "mimetype", format.getMimeType() ) );
            combi.add( new Pair<String, String>( "schema", format.getSchema() ) );
            throw eCustomizer.inputInvalidCombination( getIdentifier( definition ), combi );
        }

        List<ComplexFormatType> matchingFormats = new LinkedList<ComplexFormatType>();
        for ( ComplexFormatType candidateFormat : equalMimeTypeAndSchema ) {
            if ( format.getEncoding() == null || format.getEncoding().equals( candidateFormat.getEncoding() ) ) {
                matchingFormats.add( candidateFormat );
            }
        }

        // no formats with specified mime type, schema and encoding found?
        if ( matchingFormats.isEmpty() ) {
            List<Pair<String, String>> combi = new ArrayList<Pair<String, String>>();
            combi.add( new Pair<String, String>( "mimetype", format.getMimeType() ) );
            combi.add( new Pair<String, String>( "schema", format.getSchema() ) );
            combi.add( new Pair<String, String>( "encoding", format.getEncoding() ) );
            throw eCustomizer.inputInvalidCombination( getIdentifier( definition ), combi );
        }

        if ( matchingFormats.size() > 1 ) {
            String msg = "Format specification for complex input parameter '" + getIdentifier( definition )
                         + "' is not unique. Using first match: '" + matchingFormats.get( 0 ) + "'.";
            LOG.warn( msg );
        }

        ComplexFormatType matchingFormat = matchingFormats.get( 0 );
        LOG.debug( "Augmented format: '" + toString( matchingFormat ) + "'" );
        return matchingFormat;
    }

    private ComplexFormatType validateAndAugmentFormat( ComplexFormatType format, ComplexOutputDefinition definition,
                                                        ExceptionCustomizer eCustomizer )
                            throws OWSException {

        LOG.debug( "Looking up compatible format ('" + toString( format ) + "') in parameter definition." );
        List<ComplexFormatType> equalMimeType = null;
        if ( format.getMimeType() == null ) {
            // not specified -> assume mime type from default format
            equalMimeType = Collections.singletonList( definition.getDefaultFormat() );
        } else {
            equalMimeType = new LinkedList<ComplexFormatType>();
            if ( format.getMimeType().equals( definition.getDefaultFormat().getMimeType() ) ) {
                equalMimeType.add( definition.getDefaultFormat() );
            }
            for ( ComplexFormatType otherFormat : definition.getOtherFormats() ) {
                if ( format.getMimeType().equals( otherFormat.getMimeType() ) ) {
                    equalMimeType.add( format );
                }
            }
        }

        // no matching formats (mime type) found?
        if ( equalMimeType.isEmpty() ) {
            throw eCustomizer.outputInvalidParameter( getIdentifier( definition ),
                                                      new Pair<String, String>( "mimetype", format.getMimeType() ) );
        }

        List<ComplexFormatType> equalMimeTypeAndSchema = new LinkedList<ComplexFormatType>();
        for ( ComplexFormatType candidateFormat : equalMimeType ) {
            if ( format.getSchema() == null || format.getSchema().equals( candidateFormat.getSchema() ) ) {
                equalMimeTypeAndSchema.add( candidateFormat );
            }
        }

        // no matching formats (mime type and schema) found?
        if ( equalMimeTypeAndSchema.isEmpty() ) {
            List<Pair<String, String>> combi = new ArrayList<Pair<String, String>>();
            combi.add( new Pair<String, String>( "mimetype", format.getMimeType() ) );
            combi.add( new Pair<String, String>( "schema", format.getSchema() ) );
            throw eCustomizer.outputInvalidCombination( getIdentifier( definition ), combi );
        }

        List<ComplexFormatType> matchingFormats = new LinkedList<ComplexFormatType>();
        for ( ComplexFormatType candidateFormat : equalMimeTypeAndSchema ) {
            if ( format.getEncoding() == null || format.getEncoding().equals( candidateFormat.getEncoding() ) ) {
                matchingFormats.add( candidateFormat );
            }
        }

        // no formats with specified mime type, schema and encoding found?
        if ( matchingFormats.isEmpty() ) {
            List<Pair<String, String>> combi = new ArrayList<Pair<String, String>>();
            combi.add( new Pair<String, String>( "mimetype", format.getMimeType() ) );
            combi.add( new Pair<String, String>( "schema", format.getSchema() ) );
            combi.add( new Pair<String, String>( "encoding", format.getEncoding() ) );
            throw eCustomizer.outputInvalidCombination( getIdentifier( definition ), combi );
        }

        if ( matchingFormats.size() > 1 ) {
            String msg = "Format specification for complex output parameter '" + getIdentifier( definition )
                         + "' is not unique. Using first match: '" + matchingFormats.get( 0 ) + "'.";
            LOG.warn( msg );
        }

        ComplexFormatType matchingFormat = matchingFormats.get( 0 );
        LOG.debug( "Augmented format: '" + toString( matchingFormat ) + "'" );
        return matchingFormat;
    }

    private CodeType getIdentifier( ComplexOutputDefinition definition ) {
        return new CodeType( definition.getIdentifier().getValue(), definition.getIdentifier().getCodeSpace() );
    }

    private String toString( ComplexFormatType format ) {
        return "mimeType: " + format.getMimeType() + ", encoding: " + format.getEncoding() + ", schema: "
               + format.getSchema();
    }

    private ResponseForm parseResponseForm( OMElement responseFormElement, ProcessDefinition processDef,
                                            ExceptionCustomizer eCustomizer )
                            throws OWSException {

        // choice: "wps:ResponseDocument" or "wps:RawDataOutput" element (minOccurs="1", maxOccurs="1")
        OMElement responseDocumentElement = responseFormElement.getFirstChildWithName( new QName( WPS_100_NS,
                                                                                                  "ResponseDocument" ) );
        OMElement rawDataOutputElement = responseFormElement.getFirstChildWithName( new QName( WPS_100_NS,
                                                                                               "RawDataOutput" ) );

        ResponseForm responseForm = null;
        if ( responseDocumentElement != null && rawDataOutputElement == null ) {
            responseForm = parseResponseDocument( responseDocumentElement, processDef, eCustomizer );
        } else if ( responseDocumentElement == null && rawDataOutputElement != null ) {
            responseForm = parseRawDataOutput( rawDataOutputElement, processDef, eCustomizer );
        } else if ( responseDocumentElement != null && rawDataOutputElement != null ) {
            throw eCustomizer.mutualExclusive( wpsElement( "ResponseDocument" ), wpsElement( "RawDataOutput" ) );
        } else {
            throw eCustomizer.missingParameters( wpsElement( "ResponseDocument" ), wpsElement( "RawDataOutput" ) );
        }
        return responseForm;
    }

    private ResponseDocument parseResponseDocument( OMElement responseDocumentElement, ProcessDefinition processDef,
                                                    ExceptionCustomizer eCustomizer )
                            throws OWSException {

        // "storeExecuteResponse" attribute (optional)
        boolean storeExecuteResponse = getNodeAsBoolean( responseDocumentElement, new XPath( "@storeExecuteResponse",
                                                                                             nsContext ), false );

        // "lineage" attribute (optional)
        boolean lineage = getNodeAsBoolean( responseDocumentElement, new XPath( "@lineage", nsContext ), false );

        // "status" attribute (optional)
        boolean status = getNodeAsBoolean( responseDocumentElement, new XPath( "@status", nsContext ), false );

        // "wps:Output" elements (minOccurs="1", maxOccurs="unbounded")
        List<OMElement> outputElements = null;
        try {
            outputElements = getRequiredElements( responseDocumentElement, new XPath( "wps:Output", nsContext ) );
        } catch ( XMLParsingException e ) {
            throw eCustomizer.missingParameter( wpsElement( "ResponseDocument" ) + "/" + wpsElement( "Output" ) );
        }

        List<RequestedOutput> outputDefinitions = new ArrayList<RequestedOutput>( outputElements.size() );
        for ( OMElement outputElement : outputElements ) {
            outputDefinitions.add( parseOutput( outputElement, processDef, eCustomizer ) );
        }

        return new ResponseDocument( outputDefinitions, storeExecuteResponse, lineage, status );
    }

    private RequestedOutput parseOutput( OMElement outputElement, ProcessDefinition processDef,
                                         ExceptionCustomizer eCustomizer )
                            throws OWSException {

        // "ows:Identifier" element (minOccurs="1", maxOccurs="1")
        CodeType outputId = parseRequiredIdentifier( outputElement, eCustomizer );
        ProcessletOutputDefinition outputType = lookupOutputDefinition( outputId, processDef, eCustomizer );

        // "mimeType" attribute (optional)
        String mimeType = outputElement.getAttributeValue( new QName( "mimeType" ) );
        if ( mimeType == null ) {
            if ( outputType instanceof ComplexOutputDefinition ) {
                mimeType = ( (ComplexOutputDefinition) outputType ).getDefaultFormat().getMimeType();
            }
        }

        ComplexFormatType format = new ComplexFormatType();
        // "mimeType" attribute (optional)
        format.setMimeType( outputElement.getAttributeValue( new QName( "mimeType" ) ) );
        // "encoding" attribute (optional)
        format.setEncoding( outputElement.getAttributeValue( new QName( "encoding" ) ) );
        // "schema" attribute (optional)
        format.setSchema( outputElement.getAttributeValue( new QName( "schema" ) ) );
        if ( outputType instanceof ComplexOutputDefinition ) {
            format = validateAndAugmentFormat( format, (ComplexOutputDefinition) outputType, eCustomizer );
        }

        // "uom" attribute (optional)
        // TODO validate against offered uoms
        String uom = outputElement.getAttributeValue( new QName( "uom" ) );

        // "asReference" attribute (optional)
        boolean asReference = getNodeAsBoolean( outputElement, new XPath( "@asReference", nsContext ), false );
        LOG.debug( "attribute 'asReference': " + asReference );

        // "ows:Title" element (minOccurs="0", maxOccurs="1")
        LanguageString title = null;
        OMElement titleElement = outputElement.getFirstChildWithName( new QName( OWS110_NS, "Title" ) );
        if ( titleElement != null ) {
            title = parseLanguageString( titleElement );
        }

        // "ows:Abstract" element (minOccurs="0", maxOccurs="1")
        LanguageString summary = null;
        OMElement abstractElement = outputElement.getFirstChildWithName( new QName( OWS110_NS, "Abstract" ) );
        if ( abstractElement != null ) {
            summary = parseLanguageString( abstractElement );
        }

        return new RequestedOutput( outputType, asReference, format.getMimeType(), format.getEncoding(),
                                    format.getSchema(), uom, title, summary );
    }

    private RawDataOutput parseRawDataOutput( OMElement rawDataOutputElement, ProcessDefinition processDef,
                                              ExceptionCustomizer eCustomizer )
                            throws OWSException {

        // "ows:Identifier" element (minOccurs="1", maxOccurs="1")
        CodeType identifier = parseRequiredIdentifier( rawDataOutputElement, eCustomizer );
        ProcessletOutputDefinition outputType = lookupOutputDefinition( identifier, processDef, eCustomizer );

        // "mimeType" attribute (optional)
        String mimeType = rawDataOutputElement.getAttributeValue( new QName( "mimeType" ) );
        if ( mimeType == null ) {
            if ( outputType instanceof ComplexOutputDefinition ) {
                LOG.debug( "No mime type specified. Defaulting to '"
                           + ( (ComplexOutputDefinition) outputType ).getDefaultFormat().getMimeType() + "'" );
                mimeType = ( (ComplexOutputDefinition) outputType ).getDefaultFormat().getMimeType();
            }
        }

        ComplexFormatType format = new ComplexFormatType();
        // "mimeType" attribute (optional)
        format.setMimeType( rawDataOutputElement.getAttributeValue( new QName( "mimeType" ) ) );
        // "encoding" attribute (optional)
        format.setEncoding( rawDataOutputElement.getAttributeValue( new QName( "encoding" ) ) );
        // "schema" attribute (optional)
        format.setSchema( rawDataOutputElement.getAttributeValue( new QName( "schema" ) ) );
        if ( outputType instanceof ComplexOutputDefinition ) {
            format = validateAndAugmentFormat( format, (ComplexOutputDefinition) outputType, eCustomizer );
        }

        // "uom" attribute (optional)
        String uom = rawDataOutputElement.getAttributeValue( new QName( "uom" ) );

        RequestedOutput requestedOutput = new RequestedOutput( outputType, false, format.getMimeType(),
                                                               format.getEncoding(), format.getSchema(), uom, null,
                                                               null );

        return new RawDataOutput( requestedOutput );
    }

    private LanguageString parseLanguageString( OMElement languageStringElement ) {

        // "xml:lang" attribute (optional)
        String lang = languageStringElement.getAttributeValue( new QName( "http://www.w3.org/XML/1998/namespace",
                                                                          "lang" ) );
        // text value
        String value = languageStringElement.getText();

        return new LanguageString( value, lang );
    }

    private WPSProcess lookupProcess( CodeType identifier )
                            throws OWSException {
        WPSProcess process = idToProcess.get( identifier );
        if ( process == null ) {
            String msg = "No process with identifier '" + identifier + "' is known to the WPS.";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "ows:Identifier" );
        }
        return process;
    }

    private ProcessletInputDefinition lookupInputDefinition( CodeType identifier, ProcessDefinition processDef,
                                                             ExceptionCustomizer eCustomizer )
                            throws OWSException {

        LOG.trace( "Looking up input type: " + identifier );
        ProcessletInputDefinition inputType = null;
        InputParameters inputParams = processDef.getInputParameters();
        if ( inputParams != null ) {
            for ( JAXBElement<? extends ProcessletInputDefinition> el : inputParams.getProcessInput() ) {
                LOG.trace( "Defined input type: " + el.getValue().getIdentifier().getValue() );
                org.deegree.services.jaxb.wps.CodeType inputId = el.getValue().getIdentifier();
                if ( equals( identifier, inputId ) ) {
                    inputType = el.getValue();
                }
            }
        }
        if ( inputType == null ) {
            throw eCustomizer.inputNoSuchParameter( identifier );
        }
        return inputType;
    }

    private ProcessletOutputDefinition lookupOutputDefinition( CodeType identifier, ProcessDefinition processDef,
                                                               ExceptionCustomizer eCustomizer )
                            throws OWSException {

        ProcessletOutputDefinition outputType = null;
        OutputParameters outputParams = processDef.getOutputParameters();
        if ( outputParams != null ) {
            for ( JAXBElement<? extends ProcessletOutputDefinition> el : outputParams.getProcessOutput() ) {
                org.deegree.services.jaxb.wps.CodeType outputId = el.getValue().getIdentifier();
                if ( equals( identifier, outputId ) ) {
                    outputType = el.getValue();
                }
            }
        }
        if ( outputType == null ) {
            throw eCustomizer.outputNoSuchParameter( identifier );
        }
        return outputType;
    }

    private boolean equals( CodeType codeType, org.deegree.services.jaxb.wps.CodeType codeType2 ) {
        if ( codeType2.getValue().equals( codeType.getCode() ) ) {
            if ( codeType2.getCodeSpace() == null ) {
                return codeType.getCodeSpace() == null;
            }
            return codeType2.getCodeSpace().equals( codeType.getCodeSpace() );
        }
        return false;
    }

    private CodeType parseRequiredIdentifier( OMElement el, ExceptionCustomizer eCustomizer )
                            throws OWSException {

        OMElement codeTypeElement = el.getFirstChildWithName( new QName( OWS110_NS, "Identifier" ) );
        if ( codeTypeElement == null ) {
            throw eCustomizer.missingParameter( el.getLocalName() + "/" + new QName( OWS110_NS, "Identifier" ) );
        }

        // "codeSpace" attribute (optional)
        String codeSpace = codeTypeElement.getAttributeValue( new QName( "codeSpace" ) );

        // text value
        String value = codeTypeElement.getText();

        return new CodeType( value, codeSpace );
    }
}
