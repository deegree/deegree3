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

package org.deegree.services.controller.wps.execute;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.util.Base64;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.ows.OWSCommonXMLAdapter;
import org.deegree.services.controller.ows.OWSException110XMLAdapter;
import org.deegree.services.controller.wps.ProcessletExecution;
import org.deegree.services.controller.wps.ProcessletExecution.ExecutionState;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.BoundingBoxInput;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.EmbeddedComplexInput;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.input.ProcessletInput;
import org.deegree.services.wps.input.ReferencedComplexInput;
import org.deegree.services.wps.output.BoundingBoxOutputImpl;
import org.deegree.services.wps.output.ComplexOutputImpl;
import org.deegree.services.wps.output.LiteralOutputImpl;
import org.deegree.services.wps.output.ProcessletOutput;
import org.deegree.services.wps.output.ProcessletOutputImpl;
import org.deegree.services.wps.processdefinition.ProcessDefinition;
import org.deegree.services.wps.processdefinition.ProcessletOutputDefinition;
import org.deegree.services.wps.processdefinition.ProcessDefinition.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for the generation of WPS ExecuteResponse documents from {@link ExecuteResponse} objects.
 * 
 * @see ExecuteResponse
 * 
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: $
 * 
 * @version $Revision: $, $Date: $
 */
public class ExecuteResponseXMLAdapter extends XMLAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( ExecuteResponseXMLAdapter.class );

    private static final String OGC_NS = "http://www.opengis.net/ogc";

    private static final String OGC_PREFIX = "ogc";

    private static final String OWS_NS = "http://www.opengis.net/ows/1.1";

    private static final String OWS_PREFIX = "ows";

    private static final String WPS_NS = "http://www.opengis.net/wps/1.0.0";

    private static final String WPS_PREFIX = "wps";

    private static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

    private static NamespaceContext nsContext;

    static {
        nsContext = new NamespaceContext( XMLAdapter.nsContext );
        nsContext.addNamespace( OWS_PREFIX, OWS_NS );
        nsContext.addNamespace( WPS_PREFIX, WPS_NS );
    }

    /**
     * Exports an {@link ExecuteResponse} object as a WPS 1.0.0 ExecuteResponse document.
     * 
     * @param writer
     *            writer where the XML is written to
     * @param response
     *            object to be exported
     * @throws XMLStreamException
     */
    public static void export100( XMLStreamWriter writer, ExecuteResponse response )
                            throws XMLStreamException {

        writer.setPrefix( WPS_PREFIX, WPS_NS );
        writer.setPrefix( OWS_PREFIX, OWS_NS );
        writer.setPrefix( OGC_PREFIX, OGC_NS );
        writer.setPrefix( "xlink", XLN_NS );
        writer.setPrefix( "xsi", XSI_NS );

        // "wps:ExecuteResponse" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( WPS_NS, "ExecuteResponse" );

        writer.writeAttribute( "service", "WPS" );
        writer.writeAttribute( "version", "1.0.0" );
        writer.writeAttribute( "xml:lang", "en" );

        writer.writeAttribute( XSI_NS, "schemaLocation",
                               "http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_response.xsd" );

        // "serviceInstance" attribute (required)
        writer.writeAttribute( "serviceInstance", response.getServiceInstance().toString() );

        // "statusLocation" attribute (optional)
        if ( response.getStatusLocation() != null ) {
            writer.writeAttribute( "statusLocation", "" + response.getStatusLocation().getWebURL() );
        }

        // "wps:Process" element (minOccurs="1",maxOccurs="1")
        exportProcess( writer, response.getProcessDefinition() );

        // "wps:Status" element (minOccurs="1",maxOccurs="1")
        exportStatus( writer, response.getExecutionStatus() );

        // include inputs and output requests in the response?
        if ( response.getLineage() ) {
            // "wps:DataInputs" element (minOccurs="0",maxOccurs="1")
            exportDataInputs( writer, response );

            // "wps:OutputDefinitions" element (minOccurs="0",maxOccurs="1")
            exportOutputDefinitions( writer, response );
        }

        // "wps:ProcessOutputs" element (minOccurs="0",maxOccurs="1")
        if ( response.getExecutionStatus().getExecutionState() == ExecutionState.SUCCEEDED ) {
            // if process has finished successfully, include "wps:ProcessOutputs" element
            exportProcessOutputs( writer, response.getProcessOutputs(), response.getOutputDefinitions() );
        }

        writer.writeEndElement(); // ExecuteResponse
    }

    private static void exportDataInputs( XMLStreamWriter writer, ExecuteResponse response )
                            throws XMLStreamException {

        // "wps:DataInputs" (minOccurs="0", maxOccurs="1")
        writer.writeStartElement( WPS_NS, "DataInputs" );

        for ( ProcessletInput input : response.getDataInputs().getParameters() ) {

            // "wps:Input" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( WPS_NS, "Input" );

            // "ows:Identifier" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( OWS_NS, "Identifier" );
            if ( input.getIdentifier().getCodeSpace() != null ) {
                writer.writeAttribute( "codeSpace", input.getIdentifier().getCodeSpace() );
            }
            writer.writeCharacters( input.getIdentifier().getCode() );
            writer.writeEndElement();

            // "ows:Title" (minOccurs="0", maxOccurs="1")
            if ( input.getTitle() != null ) {
                writer.writeStartElement( OWS_NS, "Title" );
                if ( input.getTitle().getLanguage() != null ) {
                    writer.writeAttribute( "xml:lang", input.getTitle().getLanguage() );
                }
                writer.writeCharacters( input.getTitle().getString() );
                writer.writeEndElement();
            }

            // "ows:Abstract" (minOccurs="0", maxOccurs="1")
            if ( input.getAbstract() != null ) {
                writer.writeStartElement( OWS_NS, "Abstract" );
                if ( input.getAbstract().getLanguage() != null ) {
                    writer.writeAttribute( "xml:lang", input.getAbstract().getLanguage() );
                }
                writer.writeCharacters( input.getAbstract().getString() );
                writer.writeEndElement();
            }

            if ( input instanceof LiteralInput ) {
                exportLiteralInput( writer, (LiteralInput) input );
            } else if ( input instanceof BoundingBoxInput ) {
                exportBoundingBoxInput( writer, (BoundingBoxInput) input );
            } else if ( input instanceof ComplexInput ) {
                exportComplexInput( writer, (ComplexInput) input );
            }

            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private static void exportLiteralInput( XMLStreamWriter writer, LiteralInput input )
                            throws XMLStreamException {

        // "wps:Data" element
        writer.writeStartElement( WPS_NS, "Data" );

        // "wps:LiteralData" element
        writer.writeStartElement( WPS_NS, "LiteralData" );

        // "dataType" attribute (optional)
        if ( input.getDataType() != null ) {
            writer.writeAttribute( "dataType", input.getDataType() );
        }

        // "uom" attribute (optional)
        if ( input.getUOM() != null ) {
            writer.writeAttribute( "uom", input.getUOM() );
        }

        writer.writeCharacters( input.getValue() );

        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void exportBoundingBoxInput( XMLStreamWriter writer, BoundingBoxInput input )
                            throws XMLStreamException {

        // "wps:Data" element
        writer.writeStartElement( WPS_NS, "Data" );

        // "wps:BoundingBoxData" element
        writer.writeStartElement( WPS_NS, "BoundingBoxData" );
        OWSCommonXMLAdapter.exportBoundingBoxType( writer, input.getValue() );
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private static void exportComplexInput( XMLStreamWriter writer, ComplexInput input )
                            throws XMLStreamException {

        if ( input instanceof EmbeddedComplexInput ) {
            writer.writeStartElement( WPS_NS, "Data" );

            XMLStreamReader reader = ( (EmbeddedComplexInput) input ).getValueAsElement().getXMLStreamReaderWithoutCaching();

            // skip start document event
            reader.next();

            // "wps:ComplexData" element
            writeElement( writer, reader );

            writer.writeEndElement();
        } else if ( input instanceof ReferencedComplexInput ) {
            writer.writeStartElement( WPS_NS, "Reference" );
            ReferencedComplexInput referencedInput = (ReferencedComplexInput) input;

            // "mimeType" attribute (optional)
            if ( referencedInput.getMimeType() != null ) {
                writer.writeAttribute( "mimeType", referencedInput.getMimeType() );
            }

            // "encoding" attribute (optional)
            if ( referencedInput.getEncoding() != null ) {
                writer.writeAttribute( "encoding", referencedInput.getEncoding() );
            }

            // "schema" attribute (optional)
            if ( referencedInput.getSchema() != null ) {
                writer.writeAttribute( "schema", referencedInput.getSchema() );
            }

            // "xlink:href" attribute (required)
            writer.writeAttribute( XLN_NS, "href", "" + referencedInput.getURL() );

            writer.writeEndElement();
        }
    }

    private static void exportOutputDefinitions( XMLStreamWriter writer, ExecuteResponse response )
                            throws XMLStreamException {

        // "wps:OutputDefinitions" (minOccurs="0", maxOccurs="1")
        writer.writeStartElement( WPS_NS, "OutputDefinitions" );

        // request must contain a response document (otherwise lineage could not be true)
        ResponseDocument responseDoc = (ResponseDocument) response.getRequest().getResponseForm();

        for ( RequestedOutput output : responseDoc.getOutputDefinitions() ) {

            // "ows:Output" (minOccurs="1", maxOccurs="unbounded")
            writer.writeStartElement( WPS_NS, "Output" );

            // "asReference" attribute (optional)
            writer.writeAttribute( "asReference", "" + output.getAsReference() );

            // "uom" attribute (optional)
            if ( output.getUom() != null ) {
                writer.writeAttribute( "uom", output.getUom() );
            }

            // "mimeType" attribute (optional)
            if ( output.getMimeType() != null ) {
                writer.writeAttribute( "mimeType", output.getMimeType() );
            }

            // "encoding" attribute (optional)
            if ( output.getEncoding() != null ) {
                writer.writeAttribute( "encoding", output.getEncoding() );
            }

            // "schema" attribute (optional)
            if ( output.getSchemaURL() != null ) {
                writer.writeAttribute( "schema", "" + output.getSchemaURL() );
            }

            // "ows:Identifier" (minOccurs="1", maxOccurs="1")
            writer.writeStartElement( OWS_NS, "Identifier" );
            if ( output.getIdentifier().getCodeSpace() != null ) {
                writer.writeAttribute( "codeSpace", output.getIdentifier().getCodeSpace() );
            }
            writer.writeCharacters( output.getIdentifier().getCode() );
            writer.writeEndElement();

            // "ows:Title" (minOccurs="0", maxOccurs="1")
            if ( output.getTitle() != null ) {
                writer.writeStartElement( OWS_NS, "Title" );
                if ( output.getTitle().getLanguage() != null ) {
                    writer.writeAttribute( "xml:lang", output.getTitle().getLanguage() );
                }
                writer.writeCharacters( output.getTitle().getString() );
                writer.writeEndElement();
            }

            // "ows:Abstract" (minOccurs="0", maxOccurs="1")
            if ( output.getAbstract() != null ) {
                writer.writeStartElement( OWS_NS, "Abstract" );
                if ( output.getAbstract().getLanguage() != null ) {
                    writer.writeAttribute( "xml:lang", output.getAbstract().getLanguage() );
                }
                writer.writeCharacters( output.getAbstract().getString() );
                writer.writeEndElement();
            }

            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private static void exportProcess( XMLStreamWriter writer, ProcessDefinition process )
                            throws XMLStreamException {

        // "wps:Process" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( WPS_NS, "Process" );
        writer.writeAttribute( WPS_NS, "processVersion", process.getProcessVersion() );

        // "ows:Identifier" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( OWS_NS, "Identifier" );
        if ( process.getIdentifier().getCodeSpace() != null ) {
            writer.writeAttribute( "codeSpace", process.getIdentifier().getCodeSpace() );
        }
        writer.writeCharacters( process.getIdentifier().getValue() );
        writer.writeEndElement();

        // "ows:Title" (minOccurs="1", maxOccurs="1")
        if ( process.getTitle() != null ) {
            writer.writeStartElement( OWS_NS, "Title" );
            if ( process.getTitle().getLang() != null ) {
                writer.writeAttribute( "xml:lang", process.getTitle().getLang() );
            }
            writer.writeCharacters( process.getTitle().getValue() );
            writer.writeEndElement();
        }

        // "ows:Abstract" (minOccurs="0", maxOccurs="1")
        if ( process.getAbstract() != null ) {
            writer.writeStartElement( OWS_NS, "Abstract" );
            if ( process.getAbstract().getLang() != null ) {
                writer.writeAttribute( "xml:lang", process.getAbstract().getLang() );
            }
            writer.writeCharacters( process.getAbstract().getValue() );
            writer.writeEndElement();
        }

        // "ows:Metadata" (minOccurs="0", maxOccurs="unbounded")
        if ( process.getMetadata() != null ) {
            for ( Metadata metadata : process.getMetadata() ) {
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
        if ( process.getProfile() != null ) {
            for ( String profile : process.getProfile() ) {
                writeElement( writer, WPS_NS, "Profile", profile );
            }
        }

        // "wps:WSDL" (minOccurs="0", maxOccurs="unbounded")
        if ( process.getWSDL() != null ) {
            writeElement( writer, WPS_NS, "WSDL", XLN_NS, "href", process.getWSDL() );
        }

        writer.writeEndElement(); // wps:Process
    }

    private static void exportStatus( XMLStreamWriter writer, ProcessletExecution state )
                            throws XMLStreamException {

        writer.writeStartElement( WPS_NS, "Status" );

        // "creationTime" attribute (mandatory)
        long creationTime = state.getFinishTime();
        if ( creationTime == -1 ) {
            // use creation time of document if process execution has not been finished yet
            creationTime = System.currentTimeMillis();
        }
        writer.writeAttribute( "creationTime", DateUtils.formatISO8601Date( new Date( creationTime ) ) );

        switch ( state.getExecutionState() ) {
        case ACCEPTED:
            writeElement( writer, WPS_NS, "ProcessAccepted", state.getAcceptedMessage() );
            break;
        case STARTED:
            writer.writeStartElement( WPS_NS, "ProcessStarted" );
            writer.writeAttribute( "percentCompleted", "" + state.getPercentCompleted() );
            if ( state.getStartMessage() != null ) {
                writer.writeCharacters( state.getStartMessage() );
            }
            writer.writeEndElement();
            break;
        case PAUSED:
            writer.writeStartElement( WPS_NS, "ProcessPaused" );
            writer.writeAttribute( "percentCompleted", "" + state.getPercentCompleted() );
            if ( state.getPauseMessage() != null ) {
                writer.writeCharacters( state.getPauseMessage() );
            }
            writer.writeEndElement();
            break;
        case SUCCEEDED:
            writeElement( writer, WPS_NS, "ProcessSucceeded", state.getSucceededMessage() );
            break;
        case FAILED:
            writer.writeStartElement( WPS_NS, "ProcessFailed" );
            OWSException110XMLAdapter exceptionAdapter = new OWSException110XMLAdapter();
            exceptionAdapter.serializeExceptionToXML( writer, state.getFailedException() );
            writer.writeEndElement();
            break;
        }
        writer.writeEndElement();
    }

    private static void exportProcessOutputs( XMLStreamWriter writer, ProcessletOutputs outputs,
                                              List<RequestedOutput> requestedOutputs )
                            throws XMLStreamException {

        LOG.debug( "Exporting process outputs" );

        // "wps:ProcessOutputs" element
        writer.writeStartElement( WPS_NS, "ProcessOutputs" );

        for ( RequestedOutput requestedOutput : requestedOutputs ) {
            LOG.debug( "- exporting " + requestedOutput.getIdentifier() );
            ProcessletOutput output = outputs.getParameter( requestedOutput.getIdentifier() );
            exportOutput( writer, (ProcessletOutputImpl) output, requestedOutput );
        }

        writer.writeEndElement();
    }

    private static void exportOutput( XMLStreamWriter writer, ProcessletOutputImpl output,
                                      RequestedOutput requestedOutput )
                            throws XMLStreamException {

        // "wps:Output" element
        writer.writeStartElement( WPS_NS, "Output" );

        ProcessletOutputDefinition outputType = output.getDefinition();

        // "ows:Identifier" (minOccurs="1", maxOccurs="1")
        writer.writeStartElement( OWS_NS, "Identifier" );
        if ( outputType.getIdentifier().getCodeSpace() != null ) {
            writer.writeAttribute( "codeSpace", outputType.getIdentifier().getCodeSpace() );
        }
        writer.writeCharacters( outputType.getIdentifier().getValue() );
        writer.writeEndElement();

        // "ows:Title" (minOccurs="1", maxOccurs="1")
        if ( outputType.getTitle() != null ) {
            writer.writeStartElement( OWS_NS, "Title" );
            if ( outputType.getTitle().getLang() != null ) {
                writer.writeAttribute( "xml:lang", outputType.getTitle().getLang() );
            }
            writer.writeCharacters( outputType.getTitle().getValue() );
            writer.writeEndElement();
        }

        // "ows:Abstract" (minOccurs="0", maxOccurs="1")
        if ( outputType.getAbstract() != null ) {
            writer.writeStartElement( OWS_NS, "Abstract" );
            if ( outputType.getAbstract().getLang() != null ) {
                writer.writeAttribute( "xml:lang", outputType.getAbstract().getLang() );
            }
            writer.writeCharacters( outputType.getAbstract().getValue() );
            writer.writeEndElement();
        }

        // "ows:Metadata" (minOccurs="0", maxOccurs="unbounded")
        if ( outputType.getMetadata() != null ) {
            for ( ProcessletOutputDefinition.Metadata metadata : outputType.getMetadata() ) {
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

        // choice: "wps:Reference" or "wps:Data" (minOccurs="1", maxOccurs="1")
        // (ignore asReference for Literal or BoundingBoxOutput)
        if ( !requestedOutput.getAsReference() || output instanceof BoundingBoxOutputImpl
             || output instanceof LiteralOutputImpl ) {

            writer.writeStartElement( WPS_NS, "Data" );

            if ( output instanceof BoundingBoxOutputImpl ) {
                exportBoundingBoxOutput( writer, (BoundingBoxOutputImpl) output );
            } else if ( output instanceof LiteralOutputImpl ) {
                exportLiteralOutput( writer, (LiteralOutputImpl) output );
            } else if ( output instanceof ComplexOutputImpl ) {
                exportComplexOutput( writer, (ComplexOutputImpl) output );
            }

            writer.writeEndElement();
        } else {
            writer.writeStartElement( WPS_NS, "Reference" );

            String href = null;

            String mimeType = null;
            if ( output instanceof ComplexOutputImpl ) {
                ComplexOutputImpl complexOutput = (ComplexOutputImpl) output;
                href = complexOutput.getWebURL();
                mimeType = complexOutput.getRequestedMimeType();
            }

            if ( mimeType == null ) {
                LOG.warn( "No mime type info available -> text/xml" );
                mimeType = "text/xml";
            }

            writer.writeAttribute( "href", "" + href );
            writer.writeAttribute( "mimeType", mimeType );
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private static void exportBoundingBoxOutput( XMLStreamWriter writer, BoundingBoxOutputImpl output )
                            throws XMLStreamException {

        // "wps:BoundingBoxData" element
        writer.writeStartElement( WPS_NS, "BoundingBoxData" );
        OWSCommonXMLAdapter.exportBoundingBoxType( writer, output.getValue() );
        writer.writeEndElement();
    }

    private static void exportLiteralOutput( XMLStreamWriter writer, LiteralOutputImpl output )
                            throws XMLStreamException {

        // "wps:LiteralData" element
        writer.writeStartElement( WPS_NS, "LiteralData" );

        // "dataType" attribute (optional)
        if ( output.getDataType() != null ) {
            writer.writeAttribute( "dataType", output.getDataType() );
        }

        // "uom" attribute (optional)
        if ( output.getRequestedUOM() != null ) {
            writer.writeAttribute( "uom", output.getRequestedUOM() );
        }

        writer.writeCharacters( output.getValue() );

        writer.writeEndElement();
    }

    private static void exportComplexOutput( XMLStreamWriter writer, ComplexOutputImpl output )
                            throws XMLStreamException {
        if ( output.getStreamReader() != null ) {
            exportXMLOutput( writer, output );
        } else {
            exportBinaryOutput( writer, output );
        }
    }

    private static void exportXMLOutput( XMLStreamWriter writer, ComplexOutputImpl output )
                            throws XMLStreamException {

        // "wps:ComplexData" element
        writer.writeStartElement( WPS_NS, "ComplexData" );

        String mimeType = output.getRequestedMimeType();
        if ( mimeType != null ) {
            writer.writeAttribute( "mimeType", mimeType );
        }

        String schema = output.getRequestedSchema();
        if ( schema != null ) {
            writer.writeAttribute( "schema", schema );
        }

        // NOTE: Providing the encoding attribute doesn't make any sense for inline XML output (always defined by the surrounding
        // document)

        XMLStreamReader reader = output.getStreamReader();

        // skip start document event
        // apadberg: the following line was necessary when Axiom 1.2.8 is used,
        // it is commented out because of revised behavior in Axiom 1.2.9
        if ( reader.getEventType() == XMLStreamConstants.START_DOCUMENT ) {
            reader.next();
        }

        if ( reader.getEventType() == XMLStreamConstants.START_ELEMENT ) {
            XMLAdapter.writeElement( writer, reader );
        } else {
            LOG.warn( "No element in XMLOutput found, skipping it in response document." );
        }

        writer.writeEndElement();
    }

    private static void exportBinaryOutput( XMLStreamWriter writer, ComplexOutputImpl output )
                            throws XMLStreamException {

        // "wps:ComplexData" element
        writer.writeStartElement( WPS_NS, "ComplexData" );
        
        String mimeType = output.getRequestedMimeType();
        if ( mimeType != null ) {
            writer.writeAttribute( "mimeType", mimeType );
        }      
        
        LOG.warn( "TODO Handle other encodings. Using fixed encoding: base64." );
        writer.writeAttribute( "encoding", "base64" );

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream is = output.getInputStream();
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        try {
            while ( ( bytesRead = is.read( buffer ) ) != -1 ) {
                os.write( buffer, 0, bytesRead );
            }
            os.flush();
            os.close();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        byte[] binary = os.toByteArray();
        String base64 = Base64.encode( binary );
        writer.writeCharacters( base64 );

        writer.writeEndElement();
    }
}
