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

import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.codec.binary.Base64;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.io.StreamBufferStore;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.protocol.ows.OWSExceptionReader;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.wps.WPSConstants.ExecutionState;
import org.deegree.protocol.wps.client.output.BBoxOutput;
import org.deegree.protocol.wps.client.output.ComplexOutput;
import org.deegree.protocol.wps.client.output.ExecutionOutput;
import org.deegree.protocol.wps.client.output.LiteralOutput;
import org.deegree.protocol.wps.client.param.ComplexFormat;
import org.deegree.protocol.wps.client.process.execute.ExecutionResponse;
import org.deegree.protocol.wps.client.process.execute.ExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parser for WPS 1.0.0 execute response documents.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ExecuteResponse100Reader {

    private static Logger LOG = LoggerFactory.getLogger( ExecuteResponse100Reader.class );

    private XMLStreamReader reader;

    /**
     * Creates an {@link ExecuteResponse100Reader} instance.
     * 
     * @param reader
     *            an {@link XMLStreamReader} instance, never <code>null</code>
     */
    public ExecuteResponse100Reader( XMLStreamReader reader ) {
        this.reader = reader;
    }

    /**
     * Parses an execute response document. The response shall not be an ExceptionReport.
     * 
     * @return an {@link ExecutionResponse} object
     * @throws MalformedURLException
     * @throws XMLStreamException
     */
    public ExecutionResponse parse100()
                            throws MalformedURLException, XMLStreamException {

        ExecutionStatus status = null;
        List<ExecutionOutput> outputs = null;

        String statusLocationXMLEncoded = reader.getAttributeValue( null, "statusLocation" );
        LOG.debug( "Status location: " + statusLocationXMLEncoded );
        URL statusLocation = null;
        if ( statusLocationXMLEncoded != null ) {
            statusLocation = new URL( statusLocationXMLEncoded );
        }

        int state = reader.getEventType();

        while ( state != XMLStreamConstants.START_ELEMENT || !reader.getName().getLocalPart().equals( "Status" ) ) {
            state = reader.next();
        }
        status = parseStatus();

        while ( state != END_DOCUMENT
                && ( state != START_ELEMENT || !reader.getName().getLocalPart().equals( "ProcessOutputs" ) ) ) {
            state = reader.next();
        }
        if ( state == XMLStreamConstants.START_ELEMENT ) {
            outputs = parseOutputs();
        }

        ExecutionOutput[] outputsArray = null;
        if ( outputs != null ) {
            outputsArray = outputs.toArray( new ExecutionOutput[outputs.size()] );
        } else {
            outputsArray = new ExecutionOutput[0];
        }

        return new ExecutionResponse( statusLocation, status, outputsArray );
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;wps:ProcessOutputs&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event
     * (&lt;/wps:ProcessOutputs&gt;)</li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     */
    private List<ExecutionOutput> parseOutputs()
                            throws XMLStreamException {

        List<ExecutionOutput> outputs = new ArrayList<ExecutionOutput>();
        try {
            StAXParsingHelper.nextElement( reader );
            while ( START_ELEMENT == reader.getEventType() && "Output".equals( reader.getName().getLocalPart() ) ) {
                ExecutionOutput output = null;
                StAXParsingHelper.nextElement( reader );
                CodeType id = parseIdentifier();

                int eventType;
                String localName = null;
                do {
                    eventType = reader.next();
                    if ( eventType == START_ELEMENT || eventType == END_ELEMENT ) {
                        localName = reader.getName().getLocalPart();
                    }
                } while ( eventType != START_ELEMENT
                          || ( !localName.equals( "Reference" ) && !localName.equals( "Data" ) ) );

                if ( reader.getName().getLocalPart().equals( "Reference" ) ) {
                    String href = reader.getAttributeValue( null, "href" );
                    ComplexFormat attribs = parseComplexAttributes();
                    String mimeType = attribs.getMimeType();
                    output = new ComplexOutput( id, new URL( href ), mimeType, attribs.getEncoding(),
                                                attribs.getSchema() );
                    StAXParsingHelper.nextElement( reader );
                }
                if ( reader.getName().getLocalPart().equals( "Data" ) ) {
                    output = parseOutput( id );
                    StAXParsingHelper.nextElement( reader );
                }

                outputs.add( output );
                StAXParsingHelper.nextElement( reader ); // </Output>
                StAXParsingHelper.nextElement( reader );
            }
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
        }

        return outputs;
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event of either (&lt;wps:ComplexData&gt;),
     * (&lt;wps:LiteralData&gt;) or (&lt;wps:BoundingBoxData&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event of either
     * (&lt;/wps:ComplexData&gt;) , (&lt;/wps:LiteralData&gt;) or (&lt;/wps:BoundingBoxData&gt;)</li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     */
    private ExecutionOutput parseOutput( CodeType id )
                            throws XMLStreamException {
        ExecutionOutput dataType = null;
        StAXParsingHelper.nextElement( reader );
        String localName = reader.getName().getLocalPart();
        if ( "ComplexData".equals( localName ) ) {
            dataType = parseComplexOutput( id );
        } else if ( "LiteralData".equals( localName ) ) {
            dataType = parseLiteralOutput( id );
        } else if ( "BoundingBoxData".equals( localName ) ) {
            dataType = parseBBoxOutput( id );
        }
        return dataType;
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;wps:LiteralData&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/wps:LiteralData&gt;)</li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     */
    private LiteralOutput parseLiteralOutput( CodeType id )
                            throws XMLStreamException {
        String dataType = reader.getAttributeValue( null, "dataType" );
        String uom = reader.getAttributeValue( null, "uom" );
        String value = reader.getElementText();
        return new LiteralOutput( id, value, dataType, uom );
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;wps:BoundingBoxData&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event
     * (&lt;/wps:BoundingBoxData&gt;)</li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     */
    private BBoxOutput parseBBoxOutput( CodeType id )
                            throws XMLStreamException {

        String crs = reader.getAttributeValue( null, "crs" );

        StAXParsingHelper.nextElement( reader ); // <LowerCorner>
        String[] coordStr = reader.getElementText().split( "\\s" );
        double[] lower = new double[coordStr.length];
        for ( int i = 0; i < lower.length; i++ ) {
            lower[i] = Double.parseDouble( coordStr[i] );
        }

        StAXParsingHelper.nextElement( reader ); // <UpperCorner>
        coordStr = reader.getElementText().split( "\\s" );
        double[] upper = new double[coordStr.length];
        for ( int i = 0; i < upper.length; i++ ) {
            upper[i] = Double.parseDouble( coordStr[i] );
        }
        StAXParsingHelper.nextElement( reader );
        return new BBoxOutput( id, lower, upper, crs );
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;wps:ComplexData&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/wps:ComplexData&gt;)</li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     * @throws IOException
     */
    private ExecutionOutput parseComplexOutput( CodeType id )
                            throws XMLStreamException {

        ComplexFormat attribs = parseComplexAttributes();

        StreamBufferStore tmpSink = new StreamBufferStore();
        try {
            if ( attribs.getMimeType().startsWith( "text/xml" ) || attribs.getMimeType().startsWith( "application/xml" ) ) {
                XMLOutputFactory fac = XMLOutputFactory.newInstance();
                fac.setProperty( XMLOutputFactory.IS_REPAIRING_NAMESPACES, true );
                XMLStreamWriter xmlWriter = fac.createXMLStreamWriter( tmpSink, "UTF-8" );

                StAXParsingHelper.nextElement( reader );

                xmlWriter.writeStartDocument( "UTF-8", "1.0" );
                if ( reader.getEventType() == START_ELEMENT ) {
                    XMLAdapter.writeElement( xmlWriter, reader );
                    StAXParsingHelper.nextElement( reader );
                } else {
                    LOG.debug( "Response document contains empty complex data output '" + id + "'" );
                }
                xmlWriter.writeEndDocument();
                xmlWriter.close();

            } else {
                if ( "base64".equals( attribs.getEncoding() ) ) {
                    String base64String = reader.getElementText();
                    byte[] bytes = Base64.decodeBase64( base64String );
                    tmpSink.write( bytes );
                } else {
                    LOG.warn( "The encoding of binary data (found at response location "
                              + reader.getLocation()
                              + ") is not base64. Currently only for this format the decoding can be performed. Skipping the data." );
                }
            }
            tmpSink.close();
        } catch ( IOException e ) {
            LOG.error( e.getMessage() );
        }
        return new ComplexOutput( id, tmpSink, attribs.getMimeType(), attribs.getEncoding(), attribs.getEncoding() );
    }

    /**
     * @return
     */
    private ComplexFormat parseComplexAttributes() {
        String mimeType = reader.getAttributeValue( null, "mimeType" );
        String encoding = reader.getAttributeValue( null, "encoding" );
        String schema = reader.getAttributeValue( null, "schema" );
        return new ComplexFormat( mimeType, encoding, schema );
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;wps:Identifier&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/wps:Identifier&gt;)</li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     */
    private CodeType parseIdentifier()
                            throws XMLStreamException {
        String codeSpace = reader.getAttributeValue( null, "codeSpace" );
        String code = reader.getElementText();
        return new CodeType( code, codeSpace );
    }

    /**
     * <ul>
     * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (&lt;wps:Status&gt;)</li>
     * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code> event (&lt;/wps:Status&gt;)</li>
     * </ul>
     * 
     * @return
     * @throws XMLStreamException
     */
    private ExecutionStatus parseStatus()
                            throws XMLStreamException {

        ExecutionState state = null;
        String statusMsg = null;
        Integer percent = null;
        String creationTime = null;
        OWSException exceptionReport = null;

        String attribute = reader.getAttributeValue( null, "creationTime" );
        if ( attribute != null ) {
            creationTime = attribute;
        }

        StAXParsingHelper.nextElement( reader );
        String localName = reader.getName().getLocalPart();
        if ( "ProcessAccepted".equals( localName ) ) {
            state = ExecutionState.ACCEPTED;
            statusMsg = reader.getElementText();
        } else if ( "ProcessSucceeded".equals( localName ) ) {
            state = ExecutionState.SUCCEEDED;
            statusMsg = reader.getElementText();
        } else if ( "ProcessStarted".equals( localName ) ) {
            state = ExecutionState.STARTED;
            String percentStr = reader.getAttributeValue( null, "percentCompleted" );
            if ( percentStr != null ) {
                percent = Integer.parseInt( percentStr );
            }
            statusMsg = reader.getElementText();
            StAXParsingHelper.nextElement( reader );
        } else if ( "ProcessPaused".equals( localName ) ) {
            state = ExecutionState.PAUSED;
            String percentStr = reader.getAttributeValue( null, "percentCompleted" );
            if ( percentStr != null ) {
                percent = Integer.parseInt( percentStr );
            }
            statusMsg = reader.getElementText();
            StAXParsingHelper.nextElement( reader );
        } else if ( "ProcessFailed".equals( localName ) ) {
            state = ExecutionState.FAILED;
            StAXParsingHelper.nextElement( reader ); // ProcessFailed
            exceptionReport = OWSExceptionReader.parseException( reader );
        }
        StAXParsingHelper.nextElement( reader ); // </Status>
        return new ExecutionStatus( state, statusMsg, percent, creationTime, exceptionReport );
    }
}