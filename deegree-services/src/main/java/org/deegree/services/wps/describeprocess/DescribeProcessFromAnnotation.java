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

package org.deegree.services.wps.describeprocess;

import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.services.wps.annotations.ProcessDescription.NOT_SET;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.services.jaxb.wps.ProcessDefinition;
import org.deegree.services.wps.annotations.ProcessDescription;
import org.deegree.services.wps.annotations.commons.BBox;
import org.deegree.services.wps.annotations.commons.ComplexFormat;
import org.deegree.services.wps.annotations.commons.Metadata;
import org.deegree.services.wps.annotations.commons.ReferenceType;
import org.deegree.services.wps.annotations.input.CmplxInput;
import org.deegree.services.wps.annotations.input.InputParameter;
import org.deegree.services.wps.annotations.input.LitInput;
import org.deegree.services.wps.annotations.input.Range;
import org.deegree.services.wps.annotations.input.ValueReference;
import org.deegree.services.wps.annotations.input.ValueType;
import org.deegree.services.wps.annotations.output.CmplxOutput;
import org.deegree.services.wps.annotations.output.LitOutput;
import org.deegree.services.wps.annotations.output.OutputParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for the generation of WPS ProcessDescription documents (responses to describe process requests).
 * 
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DescribeProcessFromAnnotation extends XMLAdapter {

    private static Logger LOG = LoggerFactory.getLogger( DescribeProcessFromAnnotation.class );

    private static final String OGC_NS = "http://www.opengis.net/ogc";

    private static final String OGC_PREFIX = "ogc";

    private static final String OWS_NS = "http://www.opengis.net/ows/1.1";

    private static final String OWS_PREFIX = "ows";

    private static final String WPS_NS = "http://www.opengis.net/wps/1.0.0";

    private static final String WPS_PREFIX = "wps";

    /**
     * Exports the given {@link ProcessDefinition}s as a WPS 1.0.0 compliant <code>wps:ProcessDescriptions</code>
     * element.
     * 
     * @param writer
     * @param processAnnotations
     * @throws XMLStreamException
     */
    public static void export100( XMLStreamWriter writer, List<ProcessDescription> processAnnotations )
                            throws XMLStreamException {

        writer.setPrefix( WPS_PREFIX, WPS_NS );
        writer.setPrefix( OWS_PREFIX, OWS_NS );
        writer.setPrefix( OGC_PREFIX, OGC_NS );
        writer.setPrefix( "xlink", XLN_NS );
        writer.setPrefix( "xsi", XSINS );

        writer.writeStartElement( WPS_NS, "ProcessDescriptions" );

        writer.writeAttribute( "service", "WPS" );
        writer.writeAttribute( "version", "1.0.0" );
        writer.writeAttribute( "xml:lang", "en" );
        writer.writeAttribute( XSINS, "schemaLocation",
                               "http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_response.xsd" );

        for ( ProcessDescription annotation : processAnnotations ) {
            exportDescription100( writer, annotation );
        }

        writer.writeEndElement();
    }

    /**
     * Exports a proces description from the given annotation.
     * 
     * @param writer
     * @param description
     * @throws XMLStreamException
     */
    static void exportDescription100( XMLStreamWriter writer, ProcessDescription description )
                            throws XMLStreamException {
        writer.writeStartElement( "ProcessDescription" );

        writer.writeAttribute( WPS_NS, "processVersion", description.version() );
        writer.writeAttribute( "storeSupported", Boolean.toString( description.storeSupported() ) );
        writer.writeAttribute( "statusSupported", Boolean.toString( description.statusSupported() ) );

        // "ows:Identifier" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( OWS_NS, "Identifier" );
        writer.writeCharacters( description.id() );
        writer.writeEndElement();

        // "ows:Title" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( OWS_NS, "Title" );
        writer.writeCharacters( description.title() );
        writer.writeEndElement();

        // "ows:Abstract" (minOccurs="0", maxOccurs="1")
        if ( isSet( description.abs() ) ) {
            writer.writeStartElement( OWS_NS, "Abstract" );
            writer.writeCharacters( description.abs() );
            writer.writeEndElement();
        }

        // "ows:Metadata" (minOccurs="0", maxOccurs="unbounded")
        exportMetadatas( writer, description.metadata() );

        // "wps:Profile" (minOccurs="0", maxOccurs="unbounded")
        if ( description.profile().length > 0 ) {
            for ( String profile : description.profile() ) {
                if ( isSet( profile ) ) {
                    writeElement( writer, WPS_NS, "Profile", profile );
                }
            }
        }

        // "wps:WSDL" (minOccurs="0", maxOccurs="unbounded")
        if ( isSet( description.wsdl() ) ) {
            writeElement( writer, WPS_NS, "WSDL", XLN_NS, "href", description.wsdl() );
        }

        // "DataInputs" (minOccurs="0", maxOccurs="1")
        if ( description.input().length > 0 ) {
            writer.writeStartElement( "DataInputs" );
            for ( InputParameter input : description.input() ) {
                exportInput100( writer, input );
            }
            writer.writeEndElement();
        }

        // "wps:ProcessOutputs" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( "ProcessOutputs" );
        for ( OutputParameter output : description.output() ) {
            exportOutput100( writer, output );
        }
        writer.writeEndElement();

        writer.writeEndElement(); // ProcessDescription
    }

    private static void exportInput100( XMLStreamWriter writer, InputParameter input )
                            throws XMLStreamException {

        writer.writeStartElement( "Input" );

        // "minOccurs" attribute (required)
        writer.writeAttribute( "minOccurs", Integer.toString( input.minOccurs() ) );

        // "maxOccurs" attribute (required)
        writer.writeAttribute( "maxOccurs", Integer.toString( input.maxOccurs() ) );

        // "ows:Identifier" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( OWS_NS, "Identifier" );
        // if ( input.getIdentifier().getCodeSpace() != null ) {
        // writer.writeAttribute( "codeSpace", input.getIdentifier().getCodeSpace() );
        // }
        writer.writeCharacters( input.id() );
        writer.writeEndElement();

        // "ows:Title" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( OWS_NS, "Title" );
        // if ( input.getTitle().getLang() != null ) {
        // writer.writeAttribute( "xml:lang", input.getTitle().getLang() );
        // }
        writer.writeCharacters( input.title() );
        writer.writeEndElement();

        // "ows:Abstract" (minOccurs="0", maxOccurs="1")
        if ( isSet( input.abs() ) ) {
            writer.writeStartElement( OWS_NS, "Abstract" );
            // if ( input.getAbstract().getLang() != null ) {
            // writer.writeAttribute( "xml:lang", input.getAbstract().getLang() );
            // }
            writer.writeCharacters( input.abs() );
            writer.writeEndElement();
        }

        // "ows:Metadata" (minOccurs="0", maxOccurs="unbounded")
        exportMetadatas( writer, input.metadata() );

        switch ( input.type() ) {
        case BBox:
            exportBBoxInput( writer, input.bbox() );
            break;
        case Complex:
            exportComplex( writer, input.complex() );
            break;
        case Literal:
            exportLiteral( writer, input.literal() );
            break;
        }
        writer.writeEndElement();
    }

    /**
     * @param writer
     * @param input
     * @throws XMLStreamException
     */
    private static void exportBBoxInput( XMLStreamWriter writer, BBox input )
                            throws XMLStreamException {
        // "BoundingBoxData" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( "BoundingBoxData" );

        exportCRS( writer, input.crs() );

        writer.writeEndElement();

    }

    /**
     * @param writer
     * @param input
     * @throws XMLStreamException
     */
    private static void exportComplex( XMLStreamWriter writer, CmplxInput input )
                            throws XMLStreamException {
        writer.writeStartElement( "ComplexData" );

        // "maximumMegabytes" attribute (optional)
        if ( !Double.isNaN( input.maximumMegabytes() ) ) {
            writer.writeAttribute( "maximumMegabytes", Double.toString( input.maximumMegabytes() ) );
        }

        // "Default" (minOccurs="1", maxOccurs="1")
        exportComplexFormats( writer, input.formats() );
        writer.writeEndElement(); // complexdata
    }

    /**
     * @param writer
     * @param complexFormat
     * @throws XMLStreamException
     */
    private static void exportComplexDataDescriptionType100( XMLStreamWriter writer, ComplexFormat complexFormat )
                            throws XMLStreamException {
        // "Format" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( "Format" );

        // "MimeType" (minOccurs="1", maxOccurs="1")
        writeElement( writer, "MimeType", complexFormat.mimeType() );

        // "Encoding" (minOccurs="0", maxOccurs="1")
        if ( isSet( complexFormat.encoding() ) ) {
            writeElement( writer, "Encoding", complexFormat.encoding() );
        }

        // "Schema" (minOccurs="0", maxOccurs="1")
        if ( isSet( complexFormat.schema() ) ) {
            writeElement( writer, "Schema", complexFormat.schema() );
        }

        writer.writeEndElement(); // Format
    }

    /**
     * @param writer
     * @param input
     * @throws XMLStreamException
     */
    private static void exportLiteral( XMLStreamWriter writer, LitInput literalData )
                            throws XMLStreamException {
        // "LiteralData" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( "LiteralData" );

        // "ows:DataType" (minOccurs="0", maxOccurs="1")
        if ( isSet( literalData.dataType() ) ) {
            exportDatatype( writer, literalData.dataType() );
        }

        // "UOMs" (minOccurs="0", maxOccurs="1")
        exportUOMS( writer, literalData.uoms() );

        ValueType[] allowedValues = literalData.allowedValues();
        // "wps:LiteralValuesChoice" (minOccurs="1", maxOccurs="1")
        if ( allowedValues != null && allowedValues.length > 0 ) {
            // "ows:AllowedValues" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( OWS_NS, "AllowedValues" );
            for ( ValueType vt : allowedValues ) {
                if ( isSet( vt.value() ) ) {
                    // "ows:Value" (minOccurs="1", maxOccurs="1")
                    writeElement( writer, OWS_NS, "Value", vt.value() );
                } else {
                    Range range = vt.range();
                    // "ows:Range" (minOccurs="1", maxOccurs="1")
                    writer.writeStartElement( OWS_NS, "Range" );
                    // attribute "rangeClosure"
                    LOG.warn( "Ignoring rangeClosure attribute." );
                    // writer.writeAttribute( OWS_NS, "rangeClosure", "open" );
                    // "ows:MinimumValue" (minOccurs="0", maxOccurs="1")
                    if ( isSet( range.minimum() ) ) {
                        writeOptionalElement( writer, OWS_NS, "MinimumValue", range.minimum() );
                    }
                    if ( isSet( range.maximum() ) ) {
                        // "ows:MaximumValue" (minOccurs="0", maxOccurs="1")
                        writeOptionalElement( writer, OWS_NS, "MaximumValue", range.maximum() );
                    }
                    if ( isSet( range.spacing() ) ) {
                        // "ows:Spacing" (minOccurs="0", maxOccurs="1")
                        writeOptionalElement( writer, OWS_NS, "Spacing", range.spacing() );
                    }
                    writer.writeEndElement();
                }
            }
            writer.writeEndElement();
        } else if ( isSet( literalData.validValueReference() ) ) {
            ValueReference validValueReference = literalData.validValueReference();
            // "ValuesReference" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "ValuesReference" );
            writer.writeAttribute( OWS_NS, "reference", validValueReference.reference() );
            writer.writeAttribute( "valuesForm", validValueReference.valuesForm() );
            writer.writeEndElement();
        } else {
            // "ows:AnyValue" (minOccurs="1", maxOccurs="1")
            writer.writeEmptyElement( OWS_NS, "AnyValue" );
        }

        // "DefaultValue" (minOccurs="0", maxOccurs="1")
        if ( isSet( literalData.defaultValue() ) ) {
            writer.writeStartElement( "DefaultValue" );
            writer.writeCharacters( literalData.defaultValue() );
            writer.writeEndElement();
        }

        writer.writeEndElement(); // LiteralData
    }

    private static void exportOutput100( XMLStreamWriter writer, OutputParameter output )
                            throws XMLStreamException {

        writer.writeStartElement( "Output" );

        // "ows:Identifier" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( OWS_NS, "Identifier" );
        // if ( output.getIdentifier().getCodeSpace() != null ) {
        // writer.writeAttribute( "codeSpace", output.getIdentifier().getCodeSpace() );
        // }
        writer.writeCharacters( output.id() );
        writer.writeEndElement();

        // "ows:Title" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( OWS_NS, "Title" );
        // if ( output.getTitle().getLang() != null ) {
        // writer.writeAttribute( "xml:lang", output.getTitle().getLang() );
        // }
        writer.writeCharacters( output.title() );
        writer.writeEndElement();

        // "ows:Abstract" (minOccurs="0", maxOccurs="1")
        if ( isSet( output.abs() ) ) {
            writer.writeStartElement( OWS_NS, "Abstract" );
            // if ( output.getAbstract().getLang() != null ) {
            // writer.writeAttribute( "xml:lang", output.getAbstract().getLang() );
            // }
            writer.writeCharacters( output.abs() );
            writer.writeEndElement();
        }

        // "ows:Metadata" (minOccurs="0", maxOccurs="unbounded")
        exportMetadatas( writer, output.metadata() );

        switch ( output.type() ) {
        case BBox:
            exportBBox( writer, output.bbox() );
            break;
        case Complex:
            exportComplex( writer, output.complex() );
            break;
        case Literal:
            exportLiteral( writer, output.literal() );
            break;
        }
        writer.writeEndElement(); // output

    }

    /**
     * @param writer
     * @param output
     * @throws XMLStreamException
     */
    private static void exportComplex( XMLStreamWriter writer, CmplxOutput output )
                            throws XMLStreamException {
        // "ComplexOutput" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( "ComplexOutput" );
        // "Default" (minOccurs="1", maxOccurs="1")
        exportComplexFormats( writer, output.formats() );
        writer.writeEndElement(); // ComplexOutput
    }

    /**
     * @param writer
     * @param output
     * @throws XMLStreamException
     */
    private static void exportBBox( XMLStreamWriter writer, BBox output )
                            throws XMLStreamException {

        // "BoundingBoxOutput" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( "BoundingBoxOutput" );

        exportCRS( writer, output.crs() );

        writer.writeEndElement();
    }

    /**
     * @param writer
     * @param output
     * @throws XMLStreamException
     */
    private static void exportLiteral( XMLStreamWriter writer, LitOutput literalOutput )
                            throws XMLStreamException {

        // "LiteralOutput" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( "LiteralOutput" );

        // "ows:DataType" (minOccurs="0", maxOccurs="1")
        if ( isSet( literalOutput.dataType() ) ) {
            exportDatatype( writer, literalOutput.dataType() );
        }

        // "UOMs" (minOccurs="0", maxOccurs="1")
        exportUOMS( writer, literalOutput.uoms() );
        writer.writeEndElement(); // LiteralOutput
    }

    private static void exportMetadatas( XMLStreamWriter writer, Metadata[] mds )
                            throws XMLStreamException {
        if ( mds != null && mds.length > 0 ) {
            for ( Metadata metadata : mds ) {
                writer.writeStartElement( OWS_NS, "Metadata" );
                if ( isSet( metadata.about() ) ) {
                    writer.writeAttribute( "about", metadata.about() );
                }
                if ( isSet( metadata.href() ) ) {
                    writer.writeAttribute( XLN_NS, "href", metadata.href() );
                }
                writer.writeEndElement();
            }
        }
    }

    /**
     * @param writer
     * @param uoms
     * @throws XMLStreamException
     */
    private static void exportUOMS( XMLStreamWriter writer, ReferenceType[] uoms )
                            throws XMLStreamException {
        if ( uoms != null && uoms.length > 0 && isSet( uoms[0] ) ) {
            writer.writeStartElement( "UOMs" );

            // "Default" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "Default" );
            // "ows:UOM" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( OWS_NS, "UOM" );
            if ( isSet( uoms[0].reference() ) ) {
                writer.writeAttribute( OWS_NS, "reference", uoms[0].reference() );
            }
            writer.writeCharacters( uoms[0].value() );
            writer.writeEndElement();
            writer.writeEndElement(); // Default

            // "Supported" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "Supported" );
            for ( ReferenceType uom : uoms ) {
                if ( isSet( uom ) ) {
                    // "ows:UOM" (minOccurs="1", maxOccurs="unbounded")
                    writer.writeStartElement( OWS_NS, "UOM" );
                    if ( isSet( uom.reference() ) ) {
                        writer.writeAttribute( OWS_NS, "reference", uom.reference() );
                    }
                    writer.writeCharacters( uom.value() );
                    writer.writeEndElement(); // UOM
                }
            }
            writer.writeEndElement(); // Supported
            writer.writeEndElement(); // UOMs
        }
    }

    /**
     * @param writer
     * @param crs
     * @throws XMLStreamException
     */
    private static void exportCRS( XMLStreamWriter writer, String[] crs )
                            throws XMLStreamException {
        if ( crs != null && crs.length > 0 ) {
            // "Default" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "Default" );
            // "CRS" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "CRS" );
            writer.writeCharacters( crs[0] );
            writer.writeEndElement();
            writer.writeEndElement();// default

            // "Supported" (minOccurs="1", maxOccurs="unbounded")
            writer.writeStartElement( "Supported" );
            for ( String otherCRS : crs ) {
                writer.writeStartElement( "CRS" );
                writer.writeCharacters( otherCRS );
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }

    /**
     * @param writer
     * @param formats
     * @throws XMLStreamException
     */
    private static void exportComplexFormats( XMLStreamWriter writer, ComplexFormat[] formats )
                            throws XMLStreamException {
        if ( formats != null && formats.length > 0 ) {
            writer.writeStartElement( "Default" );
            exportComplexDataDescriptionType100( writer, formats[0] );
            writer.writeEndElement(); // Default

            // "Supported" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "Supported" );
            for ( ComplexFormat cf : formats ) {
                exportComplexDataDescriptionType100( writer, cf );
            }
            writer.writeEndElement(); // Supported
        }

    }

    /**
     * @param writer
     * @param dataType
     * @throws XMLStreamException
     */
    private static void exportDatatype( XMLStreamWriter writer, ReferenceType dataType )
                            throws XMLStreamException {
        writer.writeStartElement( OWS_NS, "DataType" );
        if ( isSet( dataType.reference() ) ) {
            writer.writeAttribute( OWS_NS, "reference", dataType.reference() );
        }
        writer.writeCharacters( dataType.value() );
        writer.writeEndElement();
    }

    private static boolean isSet( String value ) {
        return value != null && !NOT_SET.equals( value );
    }

    /**
     * @param validValueReference
     * @return
     */
    private static boolean isSet( ValueReference validValueReference ) {
        return validValueReference != null && isSet( validValueReference.reference() )
               && isSet( validValueReference.valuesForm() );
    }

    /**
     * @param dataType
     * @return
     */
    private static boolean isSet( ReferenceType dataType ) {
        return dataType != null && isSet( dataType.value() );
    }
}
