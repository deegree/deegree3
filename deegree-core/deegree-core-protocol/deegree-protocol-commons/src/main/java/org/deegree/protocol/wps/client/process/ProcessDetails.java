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
package org.deegree.protocol.wps.client.process;

import static org.deegree.protocol.wps.WPSConstants.WPS_100_NS;
import static org.deegree.protocol.wps.WPSConstants.WPS_PREFIX;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.domain.Range;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.ows.capabilities.OWSCommon110CapabilitiesAdapter;
import org.deegree.protocol.wps.client.input.type.BBoxInputType;
import org.deegree.protocol.wps.client.input.type.ComplexInputType;
import org.deegree.protocol.wps.client.input.type.InputType;
import org.deegree.protocol.wps.client.input.type.LiteralInputType;
import org.deegree.protocol.wps.client.output.type.BBoxOutputType;
import org.deegree.protocol.wps.client.output.type.ComplexOutputType;
import org.deegree.protocol.wps.client.output.type.LiteralOutputType;
import org.deegree.protocol.wps.client.output.type.OutputType;
import org.deegree.protocol.wps.client.param.ComplexFormat;
import org.deegree.protocol.wps.client.param.ValueWithRef;

/**
 * Encapsulates the information returned by a <code>DescribeProcess</code> request.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ProcessDetails {

    private static final String owsPrefix = "ows";

    private static final String owsNS = "http://www.opengis.net/ows/1.1";

    private static final String xmlNS = "http://www.w3.org/XML/1998/namespace";

    private static NamespaceBindings nsContext;

    private final XMLAdapter omResponse;

    private final List<InputType> inputs;

    private final List<OutputType> outputs;

    private final boolean storeSupported;

    private final boolean statusSupported;

    static {
        nsContext = new NamespaceBindings();
        nsContext.addNamespace( WPS_PREFIX, WPS_100_NS );
        nsContext.addNamespace( owsPrefix, owsNS );
    }

    /**
     * Creates a new {@link ProcessDetails} instance.
     * 
     * @param describeResponse
     *            wps:ProcessDescriptions document containing a single ProcessDescription, must not be <code>null</code>
     */
    public ProcessDetails( XMLAdapter describeResponse ) {
        this.omResponse = describeResponse;
        this.inputs = parseInputs();
        this.outputs = parseOutputs();

        XPath xpath = new XPath( "/wps:ProcessDescriptions/ProcessDescription/@storeSupported", nsContext );
        storeSupported = describeResponse.getNodeAsBoolean( describeResponse.getRootElement(), xpath, false );

        xpath = new XPath( "/wps:ProcessDescriptions/ProcessDescription/@statusSupported", nsContext );
        statusSupported = describeResponse.getNodeAsBoolean( describeResponse.getRootElement(), xpath, false );
    }

    /**
     * Returns the input parameter descriptions for the process.
     * 
     * @return the input parameter descriptions, never <code>null</code>
     */
    public List<InputType> getInputs() {
        return inputs;
    }

    /**
     * Returns the output parameter descriptions for the process.
     * 
     * @return the output parameter descriptions, never <code>null</code>
     */
    public List<OutputType> getOutputs() {
        return outputs;
    }

    private List<InputType> parseInputs() {
        XPath xpath = new XPath( "/wps:ProcessDescriptions/ProcessDescription/DataInputs/Input", nsContext );
        List<OMElement> omInputs = omResponse.getElements( omResponse.getRootElement(), xpath );
        List<InputType> inputs = new ArrayList<InputType>( omInputs.size() );
        for ( OMElement input : omInputs ) {
            String minOccurs = input.getAttribute( new QName( null, "minOccurs" ) ).getAttributeValue();
            String maxOccurs = input.getAttribute( new QName( null, "maxOccurs" ) ).getAttributeValue();

            CodeType id = parseId( input );
            LanguageString inputTitle = parseLanguageString( input, "Title" );
            LanguageString inputAbstract = parseLanguageString( input, "Abstract" );
            InputType inputDesc = parseData( input, id, inputTitle, inputAbstract, minOccurs, maxOccurs );
            inputs.add( inputDesc );
        }
        return inputs;
    }

    private List<OutputType> parseOutputs() {
        XPath xpath = new XPath( "/wps:ProcessDescriptions/ProcessDescription/ProcessOutputs/Output", nsContext );
        List<OMElement> omOutputs = omResponse.getElements( omResponse.getRootElement(), xpath );
        List<OutputType> outputs = new ArrayList<OutputType>( omOutputs.size() );
        for ( OMElement output : omOutputs ) {
            CodeType id = parseId( output );
            LanguageString outputTitle = parseLanguageString( output, "Title" );
            LanguageString outputAbstract = parseLanguageString( output, "Abstract" );
            OutputType outputDesc = parseOutputData( output, id, outputTitle, outputAbstract );
            outputs.add( outputDesc );
        }
        return outputs;
    }

    private OutputType parseOutputData( OMElement output, CodeType id, LanguageString outputTitle,
                                        LanguageString outputAbstract ) {
        OutputType outputData = null;
        OMElement complexData = output.getFirstChildWithName( new QName( null, "ComplexOutput" ) );
        if ( complexData != null ) {
            outputData = parseComplexOutput( complexData, id, outputTitle, outputAbstract );
        }

        OMElement literalData = output.getFirstChildWithName( new QName( null, "LiteralOutput" ) );
        if ( literalData != null ) {
            outputData = parseLiteralOutput( literalData, id, outputTitle, outputAbstract );
        }

        OMElement bboxData = output.getFirstChildWithName( new QName( null, "BoundingBoxOutput" ) );
        if ( bboxData != null ) {
            outputData = parseBBoxOutput( bboxData, id, outputTitle, outputAbstract );
        }

        return outputData;
    }

    private BBoxOutputType parseBBoxOutput( OMElement bboxData, CodeType id, LanguageString outputTitle,
                                            LanguageString outputAbstract ) {
        XPath xpath = new XPath( "Default/CRS", nsContext );
        String defaultCrs = omResponse.getElement( bboxData, xpath ).getText();

        xpath = new XPath( "Supported/CRS", nsContext );
        List<OMElement> omSupported = omResponse.getElements( bboxData, xpath );
        String[] supportedCrs = new String[omSupported.size()];
        for ( int i = 0; i < omSupported.size(); i++ ) {
            supportedCrs[i] = omSupported.get( i ).getText();
        }

        return new BBoxOutputType( id, outputTitle, outputAbstract, defaultCrs, supportedCrs );
    }

    private LiteralOutputType parseLiteralOutput( OMElement omLiteral, CodeType id, LanguageString outputTitle,
                                                  LanguageString outputAbstract ) {
        OMElement omDataType = omLiteral.getFirstChildWithName( new QName( owsNS, "DataType" ) );
        ValueWithRef dataType = null;
        if ( omDataType != null ) {
            String dataTypeStr = omDataType.getText();
            String dataTypeRefStr = omDataType.getAttributeValue( new QName( owsNS, "reference" ) );
            dataType = new ValueWithRef( dataTypeStr, dataTypeRefStr );
        }

        XPath xpath = new XPath( "UOMs/Default/ows:UOM", nsContext );
        OMElement omDefault = omResponse.getElement( omLiteral, xpath );
        ValueWithRef defaultUom = null;
        if ( omDefault != null ) {
            String defaultUomStr = omDefault.getText();
            String defaultUomRefStr = omDefault.getAttributeValue( new QName( owsNS, "reference" ) );
            defaultUom = new ValueWithRef( defaultUomStr, defaultUomRefStr );
        }
        xpath = new XPath( "UOMs/Supported/ows:UOM", nsContext );
        List<OMElement> omSupported = omResponse.getElements( omLiteral, xpath );
        ValueWithRef[] supportedUoms = null;
        if ( omSupported != null ) {
            supportedUoms = new ValueWithRef[omSupported.size()];
            for ( int i = 0; i < omSupported.size(); i++ ) {
                OMElement omSupp = omSupported.get( i );
                String supportedRefStr = omSupp.getAttributeValue( new QName( owsNS, "reference" ) );
                supportedUoms[i] = new ValueWithRef( omSupp.getText(), supportedRefStr );
            }
        }
        return new LiteralOutputType( id, outputTitle, outputAbstract, dataType, defaultUom, supportedUoms );
    }

    private ComplexOutputType parseComplexOutput( OMElement omComplex, CodeType id, LanguageString outputTitle,
                                                  LanguageString outputAbstract ) {
        XPath xpath = new XPath( "Default/Format", nsContext );
        OMElement omDefault = omResponse.getElement( omComplex, xpath );
        String mimeType = omDefault.getFirstChildWithName( new QName( null, "MimeType" ) ).getText();
        OMElement omEncoding = omDefault.getFirstChildWithName( new QName( null, "Encoding" ) );
        String encoding = null;
        if ( omEncoding != null ) {
            encoding = omEncoding.getText();
        }
        OMElement omSchema = omDefault.getFirstChildWithName( new QName( null, "Schema" ) );
        String schema = null;
        if ( omSchema != null ) {
            schema = omSchema.getText();
        }

        ComplexFormat defaultFormat = new ComplexFormat( mimeType, encoding, schema );

        xpath = new XPath( "Supported/Format", nsContext );
        List<OMElement> omSupported = omResponse.getElements( omComplex, xpath );
        ComplexFormat[] supportedFormats = new ComplexFormat[omSupported.size()];
        for ( int i = 0; i < omSupported.size(); i++ ) {
            OMElement omSupp = omSupported.get( i );
            mimeType = omSupp.getFirstChildWithName( new QName( null, "MimeType" ) ).getText();
            omEncoding = omSupp.getFirstChildWithName( new QName( null, "Encoding" ) );
            encoding = null;
            if ( omEncoding != null ) {
                encoding = omEncoding.getText();
            }
            omSchema = omSupp.getFirstChildWithName( new QName( null, "Schema" ) );
            schema = null;
            if ( omSchema != null ) {
                schema = omSchema.getText();
            }
            supportedFormats[i] = new ComplexFormat( mimeType, encoding, schema );
        }
        return new ComplexOutputType( id, outputTitle, outputAbstract, defaultFormat, supportedFormats );
    }

    private InputType parseData( OMElement input, CodeType id, LanguageString inputTitle, LanguageString inputAbstract,
                                 String minOccurs, String maxOccurs ) {
        InputType inputData = null;

        OMElement complexData = input.getFirstChildWithName( new QName( null, "ComplexData" ) );
        if ( complexData != null ) {
            inputData = parseComplexData( complexData, id, inputTitle, inputAbstract, minOccurs, maxOccurs );
        }

        OMElement literalData = input.getFirstChildWithName( new QName( null, "LiteralData" ) );
        if ( literalData != null ) {
            inputData = parseLiteralData( literalData, id, inputTitle, inputAbstract, minOccurs, maxOccurs );
        }

        OMElement bboxData = input.getFirstChildWithName( new QName( null, "BoundingBoxData" ) );
        if ( bboxData != null ) {
            inputData = parseBBoxData( bboxData, id, inputTitle, inputAbstract, minOccurs, maxOccurs );
        }

        return inputData;
    }

    private BBoxInputType parseBBoxData( OMElement input, CodeType id, LanguageString inputTitle,
                                         LanguageString inputAbstract, String minOccurs, String maxOccurs ) {
        XPath xpath = new XPath( "Default/CRS", nsContext );
        String defaultCRS = omResponse.getElement( input, xpath ).getText();
        xpath = new XPath( "Supported/CRS", nsContext );
        List<OMElement> omSupported = omResponse.getElements( input, xpath );
        String[] supportedCRSs = new String[omSupported.size()];
        for ( int i = 0; i < omSupported.size(); i++ ) {
            supportedCRSs[i] = omSupported.get( i ).getText();
        }

        return new BBoxInputType( id, inputTitle, inputAbstract, minOccurs, maxOccurs, defaultCRS, supportedCRSs );
    }

    private LiteralInputType parseLiteralData( OMElement input, CodeType id, LanguageString inputTitle,
                                               LanguageString inputAbstract, String minOccurs, String maxOccurs ) {
        ValueWithRef dataType = null;
        OMElement omDataType = input.getFirstChildWithName( new QName( owsNS, "DataType" ) );
        if(omDataType != null) {
            String dataTypeStr = omDataType.getText();
            String dataTypeRefStr = omDataType.getAttributeValue( new QName( owsNS, "reference" ) );
            dataType = new ValueWithRef( dataTypeStr, dataTypeRefStr );
        }

        XPath xpath = new XPath( "UOMs/Default/ows:UOM", nsContext );
        OMElement omDefaultUom = omResponse.getElement( input, xpath );
        ValueWithRef defaultUom = null;
        if ( omDefaultUom != null ) {
            String defaultUomRefStr = omDefaultUom.getAttributeValue( new QName( owsNS, "reference" ) );
            defaultUom = new ValueWithRef( omDefaultUom.getText(), defaultUomRefStr );
        }

        xpath = new XPath( "UOMs/Supported/ows:UOM", nsContext );
        List<OMElement> omSupported = omResponse.getElements( input, xpath );
        ValueWithRef[] supportedUom = null;
        if ( omSupported != null ) {
            supportedUom = new ValueWithRef[omSupported.size()];
            for ( int i = 0; i < omSupported.size(); i++ ) {
                OMElement omSupport = omSupported.get( i );
                String supported = omSupport.getText();
                String supportedRefStr = omSupport.getAttributeValue( new QName( owsNS, "reference" ) );
                supportedUom[i] = new ValueWithRef( supported, supportedRefStr );
            }
        }

        OMElement omAnyValue = input.getFirstChildWithName( new QName( owsNS, "AnyValue" ) );
        boolean anyValue = ( omAnyValue != null );

        OMElement omAllowedValues = input.getFirstChildWithName( new QName( owsNS, "AllowedValues" ) );
        List<String> values = null;
        List<Range> rangeList = null;
        if ( omAllowedValues != null ) {
            QName valueQName = new QName( owsNS, "Value" );
            // safe cast
            @SuppressWarnings({ "cast", "unchecked" })
            Iterator<OMElement> iterator = (Iterator<OMElement>) omAllowedValues.getChildrenWithName( valueQName );
            values = new ArrayList<String>();
            for ( ; iterator.hasNext(); ) {
                values.add( iterator.next().getText() );
            }

            QName rangeQName = new QName( owsNS, "Range" );
            // safe cast
            @SuppressWarnings({ "cast", "unchecked" })
            Iterator<OMElement> iterator2 = (Iterator<OMElement>) omAllowedValues.getChildrenWithName( rangeQName );
            rangeList = new ArrayList<Range>();
            OWSCommon110CapabilitiesAdapter owsCommonsAdapter = new OWSCommon110CapabilitiesAdapter();
            for ( ; iterator2.hasNext(); ) {
                OMElement omRange = iterator2.next();
                Range range = owsCommonsAdapter.parseRange( omRange );
                rangeList.add( range );
            }
        }

        OMElement omValuesReference = input.getFirstChildWithName( new QName( owsNS, "ValuesReference" ) );
        ValueWithRef valuesRef = null;
        if ( omValuesReference != null ) {
            String valueRefStr = omValuesReference.getAttributeValue( new QName( owsNS, "reference" ) );
            String valueFormStr = omValuesReference.getAttributeValue( new QName( null, "valuesForm" ) );

            valuesRef = new ValueWithRef( valueRefStr, valueFormStr );
        }

        String[] valuesArray = null;
        if ( values != null ) {
            valuesArray = values.toArray( new String[values.size()] );
        }
        Range[] rangeArray = null;
        if ( rangeList != null ) {
            rangeArray = rangeList.toArray( new Range[rangeList.size()] );
        }
        return new LiteralInputType( id, inputTitle, inputAbstract, minOccurs, maxOccurs, dataType, defaultUom,
                                     supportedUom, valuesArray, rangeArray, anyValue, valuesRef );
    }

    private InputType parseComplexData( OMElement input, CodeType id, LanguageString inputTitle,
                                        LanguageString inputAbstract, String minOccurs, String maxOccurs ) {
        XPath xpath = new XPath( "Default/Format", nsContext );
        OMElement omDefaultFormat = omResponse.getElement( input, xpath );
        String mimeType = omDefaultFormat.getFirstChildWithName( new QName( null, "MimeType" ) ).getText();

        OMElement omEncoding = omDefaultFormat.getFirstChildWithName( new QName( null, "Encoding" ) );
        String encoding = null;
        if ( omEncoding != null ) {
            encoding = omEncoding.getText();
        }
        OMElement omSchema = omDefaultFormat.getFirstChildWithName( new QName( null, "Schema" ) );
        String schema = null;
        if ( omSchema != null ) {
            schema = omSchema.getText();
        }
        ComplexFormat defaultFormat = new ComplexFormat( mimeType, encoding, schema );

        xpath = new XPath( "Supported/Format", nsContext );
        List<OMElement> omFormats = omResponse.getElements( input, xpath );
        ComplexFormat[] supported = new ComplexFormat[omFormats.size()];
        for ( int i = 0; i < omFormats.size(); i++ ) {
            OMElement omFormat = omFormats.get( i );
            mimeType = omFormat.getFirstChildWithName( new QName( null, "MimeType" ) ).getText();
            omEncoding = omFormat.getFirstChildWithName( new QName( null, "Encoding" ) );
            encoding = null;
            if ( omEncoding != null ) {
                encoding = omEncoding.getText();
            }
            omSchema = omFormat.getFirstChildWithName( new QName( null, "Schema" ) );
            schema = null;
            if ( omSchema != null ) {
                schema = omSchema.getText();
            }
            supported[i] = new ComplexFormat( mimeType, encoding, schema );
        }
        return new ComplexInputType( id, inputTitle, inputAbstract, minOccurs, maxOccurs, defaultFormat, supported );
    }

    private LanguageString parseLanguageString( OMElement omElement, String name ) {
        OMElement omElem = omElement.getFirstChildWithName( new QName( owsNS, name ) );
        if ( omElem != null ) {
            String lang = omElem.getAttributeValue( new QName( xmlNS, "lang" ) );
            return new LanguageString( omElem.getText(), lang );
        }
        return null;
    }

    private CodeType parseId( OMElement omElement ) {
        OMElement omId = omElement.getFirstChildWithName( new QName( owsNS, "Identifier" ) );
        String codeSpace = omId.getAttributeValue( new QName( null, "codeSpace" ) );
        if ( codeSpace != null ) {
            return new CodeType( omId.getText(), codeSpace );
        }
        return new CodeType( omId.getText() );
    }

    /**
     * Returns whether the process supports storing the response document.
     * 
     * @return true, if storing is supported, false otherwise
     */
    boolean getStoreSupported() {
        return storeSupported;
    }

    /**
     * Returns whether the process supports status information during asynchronous operation.
     * 
     * @return true, if status information is supported, false otherwise
     */
    boolean getStatusSupported() {
        return statusSupported;
    }
}