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
package org.deegree.protocol.wps.client.wps100;

import static org.deegree.commons.xml.CommonNamespaces.XLINK_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.util.Base64;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.wps.client.input.BBoxInput;
import org.deegree.protocol.wps.client.input.BinaryInput;
import org.deegree.protocol.wps.client.input.ExecutionInput;
import org.deegree.protocol.wps.client.input.LiteralInput;
import org.deegree.protocol.wps.client.input.XMLInput;
import org.deegree.protocol.wps.client.param.ComplexFormat;
import org.deegree.protocol.wps.client.process.execute.OutputFormat;
import org.deegree.protocol.wps.client.process.execute.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates WPS 1.0.0 Execute request documents.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ExecuteRequest100Writer {

    private static Logger LOG = LoggerFactory.getLogger( ExecuteRequest100Writer.class );

    private static final String wpsPrefix = "wps";

    private static final String xsiPrefix = "xsi";

    private static final String owsPrefix = "ows";

    private static final String wpsNS = "http://www.opengis.net/wps/1.0.0";

    private static final String owsNS = "http://www.opengis.net/ows/1.1";

    private static final String xsiNS = "http://www.w3.org/2001/XMLSchema-instance";

    private final XMLStreamWriter writer;

    /**
     * Creates a new {@link ExecuteRequest100Writer} instance.
     * 
     * @param writer
     *            xml stream to write to, must not be <code>null</code> and empty
     */
    public ExecuteRequest100Writer( XMLStreamWriter writer ) {
        this.writer = writer;
    }

    /**
     * @param id
     * @param inputs
     * @param responseFormat
     * @throws IOException
     * @throws XMLStreamException
     */
    public void write100( CodeType id, List<ExecutionInput> inputs, ResponseFormat responseFormat )
                            throws IOException, XMLStreamException {

        writer.writeStartDocument( "UTF-8", "1.0" );
        writer.writeStartElement( wpsPrefix, "Execute", wpsNS );
        writer.writeAttribute( "service", "WPS" );
        writer.writeAttribute( "version", "1.0.0" );
        String schemaLocation = "http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd";
        writer.writeAttribute( "xsi", "http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", schemaLocation );

        writer.writeNamespace( wpsPrefix, wpsNS );
        writer.writeNamespace( owsPrefix, owsNS );
        writer.writeNamespace( xsiPrefix, xsiNS );
        writer.writeNamespace( XLINK_PREFIX, XLNNS );

        writeHeader( id );
        writeInputs( inputs );
        writeOutputs( responseFormat );

        writer.writeEndElement();
        writer.writeEndDocument();

    }

    /**
     * @param outputFormat
     * @throws XMLStreamException
     */
    private void writeOutputs( ResponseFormat outputFormat )
                            throws XMLStreamException {
        if ( outputFormat != null ) {
            List<OutputFormat> outputs = outputFormat.getOutputDefinitions();

            if ( outputs != null && outputs.size() > 0 ) {
                writer.writeStartElement( wpsPrefix, "ResponseForm", wpsNS );

                if ( !outputFormat.returnRawOutput() ) {
                    writer.writeStartElement( wpsPrefix, "ResponseDocument", wpsNS );
                    writer.writeAttribute( "storeExecuteResponse", String.valueOf( outputFormat.storeResponse() ) );
                    writer.writeAttribute( "lineage", String.valueOf( outputFormat.includeInputs() ) );
                    writer.writeAttribute( "status", String.valueOf( outputFormat.updateStatus() ) );

                    for ( OutputFormat outputDef : outputs ) {
                        writer.writeStartElement( wpsPrefix, "Output", wpsNS );
                        if ( outputDef.isReference() ) {
                            writer.writeAttribute( "asReference", "true" );
                        }
                        if ( outputDef.getUom() != null ) {
                            writer.writeAttribute( "uom", outputDef.getUom() );
                        }
                        if ( outputDef.getComplexAttributes() != null ) {
                            writeComplexAttributes( outputDef.getComplexAttributes() );
                        }
                        writeIdentifier( outputDef.getId() );
                        writer.writeEndElement();
                    }
                    writer.writeEndElement();

                } else {
                    writer.writeStartElement( wpsPrefix, "RawDataOutput", wpsNS );

                    if ( outputFormat.getOutputDefinitions().get( 0 ).getUom() != null ) {
                        writer.writeAttribute( "uom", outputFormat.getOutputDefinitions().get( 0 ).getUom() );
                    }
                    writeComplexAttributes( outputFormat.getOutputDefinitions().get( 0 ).getComplexAttributes() );

                    writeIdentifier( outputFormat.getOutputDefinitions().get( 0 ).getId() );
                    writer.writeEndElement(); // RawDataOutput
                }
            }
        }
    }

    /**
     * @param complexAttributes
     * @throws XMLStreamException
     */
    private void writeComplexAttributes( ComplexFormat complexAttributes )
                            throws XMLStreamException {
        if ( complexAttributes.getEncoding() != null ) {
            writer.writeAttribute( "encoding", complexAttributes.getEncoding() );
        }
        if ( complexAttributes.getSchema() != null ) {
            writer.writeAttribute( "schema", complexAttributes.getSchema() );
        }
        if ( complexAttributes.getMimeType() != null ) {
            writer.writeAttribute( "mimeType", complexAttributes.getMimeType() );
        }
    }

    private void writeIdentifier( CodeType id )
                            throws XMLStreamException {
        writer.writeStartElement( "ows", "Identifier", owsNS );
        if ( id.getCodeSpace() != null ) {
            writer.writeCharacters( id.getCodeSpace() + ":" + id.getCode() );
        } else {
            writer.writeCharacters( id.getCode() );
        }
        writer.writeEndElement();
    }

    private void writeHeader( CodeType id )
                            throws XMLStreamException {
        writeIdentifier( id );
    }

    private void writeInputs( List<ExecutionInput> inputList )
                            throws XMLStreamException, IOException {
        if ( inputList != null && inputList.size() > 0 ) {
            writer.writeStartElement( wpsPrefix, "DataInputs", wpsNS );

            for ( int i = 0; i < inputList.size(); i++ ) {
                ExecutionInput dataInput = inputList.get( i );

                writer.writeStartElement( wpsPrefix, "Input", wpsNS );
                writeIdentifier( dataInput.getId() );

                if ( dataInput.getWebAccessibleURL() != null ) {
                    writer.writeStartElement( wpsPrefix, "Reference", wpsNS );
                    writer.writeAttribute( XLINK_PREFIX, XLNNS, "href",
                                           dataInput.getWebAccessibleURL().toExternalForm() );
                    writer.writeEndElement();
                } else {
                    writer.writeStartElement( wpsPrefix, "Data", wpsNS );
                    if ( dataInput instanceof XMLInput ) {
                        XMLInput complexInput = (XMLInput) dataInput;
                        writer.writeStartElement( wpsPrefix, "ComplexData", wpsNS );

                        writeComplexAttributes( complexInput.getFormat() );

                        XMLStreamReader xmldata = complexInput.getAsXMLStream();

                        XMLAdapter.writeElement( writer, xmldata );

                        writer.writeEndElement();

                    } else if ( dataInput instanceof BinaryInput ) {
                        BinaryInput binaryInput = (BinaryInput) dataInput;

                        try {
                            writer.writeStartElement( wpsPrefix, "ComplexData", wpsNS );
                            byte[] buffer = new byte[1024];
                            int read = -1;
                            InputStream is = binaryInput.getAsBinaryStream();
                            while ( ( read = is.read( buffer ) ) != -1 ) {
                                if ( !"base64".equals( binaryInput.getFormat().getEncoding() ) ) {
                                    String encoded = Base64.encode( buffer, 0, read );
                                    writer.writeCharacters( encoded );
                                } else {
                                    writer.writeCharacters( new String( buffer, "UTF-8" ) );
                                }
                            }
                            writer.writeEndElement();
                        } catch ( IOException e ) {
                            LOG.error( e.getMessage() );
                        }

                    } else if ( dataInput instanceof LiteralInput ) {
                        LiteralInput literalDataType = (LiteralInput) dataInput;
                        writer.writeStartElement( wpsPrefix, "LiteralData", wpsNS );

                        if ( literalDataType.getDataType() != null ) {
                            writer.writeAttribute( "dataType", literalDataType.getDataType() );
                        }
                        if ( literalDataType.getUom() != null ) {
                            writer.writeAttribute( "uom", literalDataType.getUom() );
                        }
                        writer.writeCharacters( literalDataType.getValue() );
                        writer.writeEndElement();

                    } else if ( dataInput instanceof BBoxInput ) {
                        BBoxInput bboxInput = (BBoxInput) dataInput;
                        writer.writeStartElement( wpsPrefix, "BoundingBoxData", wpsNS );
                        writer.writeAttribute( "dimensions", String.valueOf( bboxInput.getDimension() ) );
                        if ( bboxInput.getCrs() != null ) {
                            writer.writeAttribute( "crs", bboxInput.getCrs() );
                        }
                        writer.writeStartElement( owsPrefix, "LowerCorner", owsNS );
                        writePoint( writer, bboxInput.getLower() );
                        writer.writeEndElement();

                        writer.writeStartElement( owsPrefix, "UpperCorner", owsNS );
                        writePoint( writer, bboxInput.getUpper() );
                        writer.writeEndElement();

                        writer.writeEndElement(); // BoundingBox
                    }
                    writer.writeEndElement(); // Data
                }
                writer.writeEndElement(); // Input
            }
            writer.writeEndElement(); // DataInputs
        }
    }

    private void writePoint( XMLStreamWriter writer, double[] coords )
                            throws XMLStreamException {
        String s = "";
        for ( int i = 0; i < coords.length; i++ ) {
            s += coords[i];
            if ( i != coords.length - 1 ) {
                s += " ";
            }
        }
        writer.writeCharacters( s );
    }
}