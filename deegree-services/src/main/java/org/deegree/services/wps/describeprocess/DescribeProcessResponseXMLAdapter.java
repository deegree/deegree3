//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/svn_classfile_header_template.xml $
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

import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.services.jaxb.wps.BoundingBoxInputDefinition;
import org.deegree.services.jaxb.wps.BoundingBoxOutputDefinition;
import org.deegree.services.jaxb.wps.ComplexFormatType;
import org.deegree.services.jaxb.wps.ComplexInputDefinition;
import org.deegree.services.jaxb.wps.ComplexOutputDefinition;
import org.deegree.services.jaxb.wps.LiteralInputDefinition;
import org.deegree.services.jaxb.wps.LiteralOutputDefinition;
import org.deegree.services.jaxb.wps.ProcessDefinition;
import org.deegree.services.jaxb.wps.ProcessletInputDefinition;
import org.deegree.services.jaxb.wps.ProcessletOutputDefinition;
import org.deegree.services.jaxb.wps.Range;
import org.deegree.services.jaxb.wps.ValidValueReference;
import org.deegree.services.jaxb.wps.LiteralOutputDefinition.DataType;
import org.deegree.services.jaxb.wps.LiteralOutputDefinition.DefaultUOM;
import org.deegree.services.jaxb.wps.LiteralOutputDefinition.OtherUOM;
import org.deegree.services.jaxb.wps.ProcessDefinition.Metadata;
import org.deegree.services.wps.WPSProcess;
import org.deegree.services.wps.annotations.ProcessDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for the generation of WPS ProcessDescription documents (responses to describe process requests).
 * 
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: $
 * 
 * @version $Revision: $, $Date: $
 */
public class DescribeProcessResponseXMLAdapter extends XMLAdapter {

    private static Logger LOG = LoggerFactory.getLogger( DescribeProcessResponseXMLAdapter.class );

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
     * @param processes
     * @param processDefToWSDLUrl
     * @param processAnnotations
     * @throws XMLStreamException
     */
    public static void export100( XMLStreamWriter writer, List<WPSProcess> processes,
                                  Map<ProcessDefinition, String> processDefToWSDLUrl,
                                  List<ProcessDescription> processAnnotations )
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

        if ( processes != null && !processes.isEmpty() ) {
            for ( WPSProcess process : processes ) {
                exportDescription100( writer, process, processDefToWSDLUrl.get( process ) );
            }
        }
        if ( processAnnotations != null && !processAnnotations.isEmpty() ) {
            for ( ProcessDescription annotation : processAnnotations ) {
                DescribeProcessFromAnnotation.exportDescription100( writer, annotation );
            }
        }

        writer.writeEndElement();
    }

    private static void exportDescription100( XMLStreamWriter writer, WPSProcess process, String wsdlURL )
                            throws XMLStreamException {

        ProcessDefinition processDef = process.getDescription();
        
        writer.writeStartElement( "ProcessDescription" );     
        writer.writeAttribute( WPS_NS, "processVersion", processDef.getProcessVersion() );
        writer.writeAttribute( "storeSupported", Boolean.toString( processDef.isStoreSupported() ) );
        writer.writeAttribute( "statusSupported", Boolean.toString( processDef.isStatusSupported() ) );

        // "ows:Identifier" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( OWS_NS, "Identifier" );
        if ( processDef.getIdentifier().getCodeSpace() != null ) {
            writer.writeAttribute( "codeSpace", processDef.getIdentifier().getCodeSpace() );
        }
        writer.writeCharacters( processDef.getIdentifier().getValue() );
        writer.writeEndElement();

        // "ows:Title" (minOccurs="1", maxOccurs="1")
        if ( processDef.getTitle() != null ) {
            writer.writeStartElement( OWS_NS, "Title" );
            if ( processDef.getTitle().getLang() != null ) {
                writer.writeAttribute( "xml:lang", processDef.getTitle().getLang() );
            }
            writer.writeCharacters( processDef.getTitle().getValue() );
            writer.writeEndElement();
        }

        // "ows:Abstract" (minOccurs="0", maxOccurs="1")
        if ( processDef.getAbstract() != null ) {
            writer.writeStartElement( OWS_NS, "Abstract" );
            if ( processDef.getAbstract().getLang() != null ) {
                writer.writeAttribute( "xml:lang", processDef.getAbstract().getLang() );
            }
            writer.writeCharacters( processDef.getAbstract().getValue() );
            writer.writeEndElement();
        }

        // "ows:Metadata" (minOccurs="0", maxOccurs="unbounded")
        if ( processDef.getMetadata() != null ) {
            for ( Metadata metadata : processDef.getMetadata() ) {
                writer.writeStartElement( OWS_NS, "Metadata" );
                if ( metadata.getAbout() != null ) {
                    writer.writeAttribute( "about", metadata.getAbout() );
                }
                if ( metadata.getHref() != null ) {
                    writer.writeAttribute( XLN_NS, "href", metadata.getHref() );
                }
                writer.writeEndElement();
            }
        }

        // "wps:Profile" (minOccurs="0", maxOccurs="unbounded")
        if ( processDef.getProfile() != null ) {
            for ( String profile : processDef.getProfile() ) {
                writeElement( writer, WPS_NS, "Profile", profile );
            }
        }

        // "wps:WSDL" (minOccurs="0", maxOccurs="unbounded")
        if ( wsdlURL != null ) {
            writeElement( writer, WPS_NS, "WSDL", XLN_NS, "href", wsdlURL );
        }

        // "DataInputs" (minOccurs="0", maxOccurs="1")
        if ( processDef.getInputParameters() != null ) {
            writer.writeStartElement( "DataInputs" );
            for ( JAXBElement<? extends ProcessletInputDefinition> input : processDef.getInputParameters().getProcessInput() ) {
                exportInput100( writer, input.getValue() );
            }
            writer.writeEndElement();
        }

        // "wps:ProcessOutputs" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( "ProcessOutputs" );
        for ( JAXBElement<? extends ProcessletOutputDefinition> output : processDef.getOutputParameters().getProcessOutput() ) {
            exportOutput100( writer, output.getValue() );
        }
        writer.writeEndElement();

        writer.writeEndElement(); // ProcessDescription
    }

    private static void exportInput100( XMLStreamWriter writer, ProcessletInputDefinition input )
                            throws XMLStreamException {

        writer.writeStartElement( "Input" );

        // "minOccurs" attribute (required)
        if ( input.getMinOccurs() != null ) {
            writer.writeAttribute( "minOccurs", input.getMinOccurs().toString() );
        } else {
            writer.writeAttribute( "minOccurs", "1" );
        }

        // "maxOccurs" attribute (required)
        if ( input.getMaxOccurs() != null ) {
            writer.writeAttribute( "maxOccurs", input.getMaxOccurs().toString() );
        } else {
            writer.writeAttribute( "maxOccurs", "1" );
        }

        // "ows:Identifier" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( OWS_NS, "Identifier" );
        if ( input.getIdentifier().getCodeSpace() != null ) {
            writer.writeAttribute( "codeSpace", input.getIdentifier().getCodeSpace() );
        }
        writer.writeCharacters( input.getIdentifier().getValue() );
        writer.writeEndElement();

        // "ows:Title" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( OWS_NS, "Title" );
        if ( input.getTitle().getLang() != null ) {
            writer.writeAttribute( "xml:lang", input.getTitle().getLang() );
        }
        writer.writeCharacters( input.getTitle().getValue() );
        writer.writeEndElement();

        // "ows:Abstract" (minOccurs="0", maxOccurs="1")
        if ( input.getAbstract() != null ) {
            writer.writeStartElement( OWS_NS, "Abstract" );
            if ( input.getAbstract().getLang() != null ) {
                writer.writeAttribute( "xml:lang", input.getAbstract().getLang() );
            }
            writer.writeCharacters( input.getAbstract().getValue() );
            writer.writeEndElement();
        }

        // "ows:Metadata" (minOccurs="0", maxOccurs="unbounded")
        if ( input.getMetadata() != null ) {
            for ( ProcessletInputDefinition.Metadata metadata : input.getMetadata() ) {
                writer.writeStartElement( OWS_NS, "Metadata" );
                if ( metadata.getAbout() != null ) {
                    writer.writeAttribute( "about", metadata.getAbout() );
                }
                if ( metadata.getHref() != null ) {
                    writer.writeAttribute( XLN_NS, "href", metadata.getHref() );
                }
                writer.writeEndElement();
            }
        }

        if ( input instanceof LiteralInputDefinition ) {
            LiteralInputDefinition literalData = (LiteralInputDefinition) input;

            // "LiteralData" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "LiteralData" );

            // "ows:DataType" (minOccurs="0", maxOccurs="1")
            if ( literalData.getDataType() != null ) {
                org.deegree.services.jaxb.wps.LiteralInputDefinition.DataType dataType = literalData.getDataType();
                writer.writeStartElement( OWS_NS, "DataType" );
                if ( dataType.getReference() != null ) {
                    writer.writeAttribute( OWS_NS, "reference", dataType.getReference() );
                }
                writer.writeCharacters( dataType.getValue() );
                writer.writeEndElement();
            }

            // "UOMs" (minOccurs="0", maxOccurs="1")
            if ( literalData.getDefaultUOM() != null ) {
                org.deegree.services.jaxb.wps.LiteralInputDefinition.DefaultUOM defaultUOM = literalData.getDefaultUOM();
                writer.writeStartElement( "UOMs" );

                // "Default" (minOccurs="1", maxOccurs="1")
                writer.writeStartElement( "Default" );
                // "ows:UOM" (minOccurs="1", maxOccurs="1")
                writer.writeStartElement( OWS_NS, "UOM" );
                if ( defaultUOM.getReference() != null ) {
                    writer.writeAttribute( OWS_NS, "reference", defaultUOM.getReference() );
                }
                writer.writeCharacters( defaultUOM.getValue() );
                writer.writeEndElement();
                writer.writeEndElement(); // Default

                // "Supported" (minOccurs="1", maxOccurs="1")
                writer.writeStartElement( "Supported" );
                // "ows:UOM" (minOccurs="1", maxOccurs="unbounded")
                writer.writeStartElement( OWS_NS, "UOM" );
                if ( defaultUOM.getReference() != null ) {
                    writer.writeAttribute( OWS_NS, "reference", defaultUOM.getReference() );
                }
                writer.writeCharacters( defaultUOM.getValue() );
                writer.writeEndElement(); // UOM
                if ( literalData.getOtherUOM() != null ) {
                    for ( LiteralInputDefinition.OtherUOM uom : literalData.getOtherUOM() ) {
                        writer.writeStartElement( OWS_NS, "UOM" );
                        if ( uom.getReference() != null ) {
                            writer.writeAttribute( OWS_NS, "reference", uom.getReference() );
                        }
                        writer.writeCharacters( uom.getValue() );
                        writer.writeEndElement(); // UOM
                    }
                }
                writer.writeEndElement(); // Supported
                writer.writeEndElement(); // UOMs
            }

            // "wps:LiteralValuesChoice" (minOccurs="1", maxOccurs="1")
            if ( literalData.getAllowedValues() != null ) {
                // "ows:AllowedValues" (minOccurs="1", maxOccurs="1")
                writer.writeStartElement( OWS_NS, "AllowedValues" );
                for ( Object valueOrRange : literalData.getAllowedValues().getValueOrRange() ) {
                    if ( valueOrRange instanceof String ) {
                        // "ows:Value" (minOccurs="1", maxOccurs="1")
                        writeElement( writer, OWS_NS, "Value", (String) valueOrRange );
                    } else {
                        Range range = (Range) valueOrRange;
                        // "ows:Range" (minOccurs="1", maxOccurs="1")
                        writer.writeStartElement( OWS_NS, "Range" );
                        // attribute "rangeClosure"
                        LOG.warn( "Ignoring rangeClosure attribute." );
                        // writer.writeAttribute( OWS_NS, "rangeClosure", "open" );
                        // "ows:MinimumValue" (minOccurs="0", maxOccurs="1")
                        writeOptionalElement( writer, OWS_NS, "MinimumValue", range.getMinimumValue() );
                        // "ows:MaximumValue" (minOccurs="0", maxOccurs="1")
                        writeOptionalElement( writer, OWS_NS, "MaximumValue", range.getMaximumValue() );
                        // "ows:Spacing" (minOccurs="0", maxOccurs="1")
                        writeOptionalElement( writer, OWS_NS, "Spacing", range.getMaximumValue() );
                        writer.writeEndElement();
                    }
                }
                writer.writeEndElement();
            } else if ( literalData.getValidValueReference() != null ) {
                ValidValueReference validValueReference = literalData.getValidValueReference();
                // "ValuesReference" (minOccurs="1", maxOccurs="1")
                writer.writeStartElement( "ValuesReference" );
                if ( StringUtils.isSet( validValueReference.getReference() ) ) {
                    writer.writeAttribute( OWS_NS, "reference", validValueReference.getReference() );
                }
                if ( StringUtils.isSet( validValueReference.getValuesForm() ) ) {
                    writer.writeAttribute( "valuesForm", validValueReference.getValuesForm() );
                }
                writer.writeEndElement();
            } else {
                // "ows:AnyValue" (minOccurs="1", maxOccurs="1")
                writer.writeEmptyElement( OWS_NS, "AnyValue" );
            }

            // "DefaultValue" (minOccurs="0", maxOccurs="1")
            if ( literalData.getDefaultValue() != null ) {
                writer.writeStartElement( "DefaultValue" );
                writer.writeCharacters( literalData.getDefaultValue() );
                writer.writeEndElement();
            }

            writer.writeEndElement(); // LiteralData
        } else if ( input instanceof BoundingBoxInputDefinition ) {
            BoundingBoxInputDefinition bboxInput = (BoundingBoxInputDefinition) input;

            // "BoundingBoxData" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "BoundingBoxData" );

            // "Default" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "Default" );

            // "CRS" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "CRS" );
            writer.writeCharacters( bboxInput.getDefaultCRS() );
            writer.writeEndElement();

            writer.writeEndElement();

            // "Supported" (minOccurs="1", maxOccurs="unbounded")
            writer.writeStartElement( "Supported" );
            writer.writeStartElement( "CRS" );
            writer.writeCharacters( bboxInput.getDefaultCRS() );
            writer.writeEndElement();
            for ( String otherCRS : bboxInput.getOtherCRS() ) {
                writer.writeStartElement( "CRS" );
                writer.writeCharacters( otherCRS );
                writer.writeEndElement();
            }
            writer.writeEndElement();

            writer.writeEndElement();
        } else if ( input instanceof ComplexInputDefinition ) {
            ComplexInputDefinition complexInput = (ComplexInputDefinition) input;
            writer.writeStartElement( "ComplexData" );

            // "maximumMegabytes" attribute (optional)
            if ( complexInput.getMaximumMegabytes() != null ) {
                writer.writeAttribute( "maximumMegabytes", complexInput.getMaximumMegabytes().toString() );
            }

            // "Default" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "Default" );
            exportComplexDataDescriptionType100( writer, complexInput.getDefaultFormat() );
            writer.writeEndElement(); // Default

            // "Supported" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "Supported" );
            exportComplexDataDescriptionType100( writer, complexInput.getDefaultFormat() );
            if ( complexInput.getOtherFormats() != null )
                for ( ComplexFormatType formatType : complexInput.getOtherFormats() ) {
                    exportComplexDataDescriptionType100( writer, formatType );
                }
            writer.writeEndElement(); // Supported

            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private static void exportOutput100( XMLStreamWriter writer, ProcessletOutputDefinition output )
                            throws XMLStreamException {

        writer.writeStartElement( "Output" );

        // "ows:Identifier" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( OWS_NS, "Identifier" );
        if ( output.getIdentifier().getCodeSpace() != null ) {
            writer.writeAttribute( "codeSpace", output.getIdentifier().getCodeSpace() );
        }
        writer.writeCharacters( output.getIdentifier().getValue() );
        writer.writeEndElement();

        // "ows:Title" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( OWS_NS, "Title" );
        if ( output.getTitle().getLang() != null ) {
            writer.writeAttribute( "xml:lang", output.getTitle().getLang() );
        }
        writer.writeCharacters( output.getTitle().getValue() );
        writer.writeEndElement();

        // "ows:Abstract" (minOccurs="0", maxOccurs="1")
        if ( output.getAbstract() != null ) {
            writer.writeStartElement( OWS_NS, "Abstract" );
            if ( output.getAbstract().getLang() != null ) {
                writer.writeAttribute( "xml:lang", output.getAbstract().getLang() );
            }
            writer.writeCharacters( output.getAbstract().getValue() );
            writer.writeEndElement();
        }

        // "ows:Metadata" (minOccurs="0", maxOccurs="unbounded")
        if ( output.getMetadata() != null ) {
            for ( ProcessletOutputDefinition.Metadata metadata : output.getMetadata() ) {
                writer.writeStartElement( OWS_NS, "Metadata" );
                if ( metadata.getAbout() != null ) {
                    writer.writeAttribute( "about", metadata.getAbout() );
                }
                if ( metadata.getHref() != null ) {
                    writer.writeAttribute( XLN_NS, "href", metadata.getHref() );
                }
                writer.writeEndElement();
            }
        }

        if ( output instanceof LiteralOutputDefinition ) {
            LiteralOutputDefinition literalOutput = (LiteralOutputDefinition) output;

            // "LiteralOutput" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "LiteralOutput" );

            // "ows:DataType" (minOccurs="0", maxOccurs="1")
            if ( literalOutput.getDataType() != null ) {
                DataType dataType = literalOutput.getDataType();
                writer.writeStartElement( OWS_NS, "DataType" );
                if ( dataType.getReference() != null ) {
                    writer.writeAttribute( OWS_NS, "reference", dataType.getReference() );
                }
                writer.writeCharacters( dataType.getValue() );
                writer.writeEndElement();
            }

            // "UOMs" (minOccurs="0", maxOccurs="1")
            if ( literalOutput.getDefaultUOM() != null ) {
                DefaultUOM defaultUOM = literalOutput.getDefaultUOM();
                writer.writeStartElement( "UOMs" );

                // "Default" (minOccurs="1", maxOccurs="1")
                writer.writeStartElement( "Default" );
                // "ows:UOM" (minOccurs="1", maxOccurs="1")
                writer.writeStartElement( OWS_NS, "UOM" );
                if ( defaultUOM.getReference() != null ) {
                    writer.writeAttribute( OWS_NS, "reference", defaultUOM.getReference() );
                }
                writer.writeCharacters( defaultUOM.getValue() );
                writer.writeEndElement();
                writer.writeEndElement(); // Default

                // "Supported" (minOccurs="1", maxOccurs="1")
                writer.writeStartElement( "Supported" );
                // "ows:UOM" (minOccurs="1", maxOccurs="unbounded")
                writer.writeStartElement( OWS_NS, "UOM" );
                if ( defaultUOM.getReference() != null ) {
                    writer.writeAttribute( OWS_NS, "reference", defaultUOM.getReference() );
                }
                writer.writeCharacters( defaultUOM.getValue() );
                writer.writeEndElement(); // UOM
                for ( OtherUOM uom : literalOutput.getOtherUOM() ) {
                    writer.writeStartElement( OWS_NS, "UOM" );
                    if ( uom.getReference() != null ) {
                        writer.writeAttribute( OWS_NS, "reference", uom.getReference() );
                    }
                    writer.writeCharacters( uom.getValue() );
                    writer.writeEndElement(); // UOM
                }
                writer.writeEndElement(); // Supported
                writer.writeEndElement(); // UOMs
            }

            writer.writeEndElement(); // LiteralOutput
        } else if ( output instanceof BoundingBoxOutputDefinition ) {
            BoundingBoxOutputDefinition bboxOutput = (BoundingBoxOutputDefinition) output;

            // "BoundingBoxOutput" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "BoundingBoxOutput" );

            // "Default" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "Default" );

            // "CRS" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "CRS" );
            writer.writeCharacters( bboxOutput.getDefaultCRS() );
            writer.writeEndElement();

            writer.writeEndElement();

            // "Supported" (minOccurs="1", maxOccurs="unbounded")
            writer.writeStartElement( "Supported" );
            writer.writeStartElement( "CRS" );
            writer.writeCharacters( bboxOutput.getDefaultCRS() );
            writer.writeEndElement();
            for ( String otherCRS : bboxOutput.getOtherCRS() ) {
                writer.writeStartElement( "CRS" );
                writer.writeCharacters( otherCRS );
                writer.writeEndElement();
            }
            writer.writeEndElement();

            writer.writeEndElement();
        } else if ( output instanceof ComplexOutputDefinition ) {
            ComplexOutputDefinition complexOutput = (ComplexOutputDefinition) output;

            // "ComplexOutput" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "ComplexOutput" );

            // "Default" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "Default" );
            exportComplexDataDescriptionType100( writer, complexOutput.getDefaultFormat() );
            writer.writeEndElement(); // Default

            // "Supported" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( "Supported" );
            exportComplexDataDescriptionType100( writer, complexOutput.getDefaultFormat() );
            if ( complexOutput.getOtherFormats() != null )
                for ( ComplexFormatType formatType : complexOutput.getOtherFormats() ) {
                    exportComplexDataDescriptionType100( writer, formatType );
                }
            writer.writeEndElement(); // Supported

            writer.writeEndElement(); // ComplexOutput
        }

        writer.writeEndElement();
    }

    private static void exportComplexDataDescriptionType100( XMLStreamWriter writer, ComplexFormatType formatType )
                            throws XMLStreamException {

        // "Format" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( "Format" );

        // "MimeType" (minOccurs="1", maxOccurs="1")
        writeElement( writer, "MimeType", formatType.getMimeType() );

        // "Encoding" (minOccurs="0", maxOccurs="1")
        if ( formatType.getEncoding() != null ) {
            writeElement( writer, "Encoding", formatType.getEncoding() );
        }

        // "Schema" (minOccurs="0", maxOccurs="1")
        if ( formatType.getSchema() != null ) {
            writeElement( writer, "Schema", formatType.getSchema() );
        }

        writer.writeEndElement(); // Format
    }
}
