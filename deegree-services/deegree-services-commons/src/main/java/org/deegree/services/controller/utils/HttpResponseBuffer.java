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

package org.deegree.services.controller.utils;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.utils.io.StreamBufferStore;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.schema.SchemaValidationEvent;
import org.deegree.commons.xml.schema.SchemaValidator;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.slf4j.Logger;

/**
 * Custom {@link HttpServletResponseWrapper} that buffers all written data internally and will only send the result when
 * {@link #flushBuffer()} is called.
 * <p>
 * This allows for two things:
 * <ul>
 * <li>The header can be changed after the response is generated.</li>
 * <li>The whole response and the header can be discarded.</li>
 * </ul>
 * </p>
 * With the first the service is able to set the Content-length. The second allows to discard the generated response and
 * start the response from scratch. This is useful if an exception occurred and an ExceptionReport should be returned
 * and not the partial original result.
 * <p>
 * This wrapper allows the change between {@link #getWriter()} and {@link #getOutputStream()} after {@link #reset()} was
 * called. This is unlike the original servlet API that throws an {@link IllegalStateException} when getWriter is called
 * after getOutputStream, or vice versa.
 * </p>
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class HttpResponseBuffer extends HttpServletResponseWrapper {

    private static final Logger LOG = getLogger( HttpResponseBuffer.class );

    private boolean addEncoding = true;

    // if buffer == null, buffering is disabled
    private StreamBufferStore buffer;

    /**
     * The servlet api only allows a call to either getWriter or getOutputStream. This enum will protocol the current
     * state.
     */
    private enum ReturnType {
        NOT_DEFINED_YET, PRINT_WRITER, OUTPUT_STREAM
    }

    private ReturnType returnType = ReturnType.NOT_DEFINED_YET;

    private PrintWriter printWriter;

    private ServletOutputStream outputStream;

    private XMLStreamWriter xmlWriter;

    private final HttpServletResponse wrappee;

    private final HttpServletRequest request;

    /**
     * @param response
     * @param request
     */
    public HttpResponseBuffer( HttpServletResponse response, HttpServletRequest request ) {
        super( response );
        wrappee = response;
        this.request = request;
        buffer = new StreamBufferStore();
        outputStream = new BufferedServletOutputStream( buffer );
    }

    /**
     * @return the underlying servlet response
     */
    public HttpServletResponse getWrappee() {
        return wrappee;
    }

    /**
     * Disables the buffering of the output.
     * <p>
     * This method may only be called, if neither {@link #getWriter()}, {@link #getOutputStream()} nor
     * {@link #getXMLWriter()} has been called before.
     * </p>
     */
    public void disableBuffering() {
        if ( returnType != ReturnType.NOT_DEFINED_YET ) {
            throw new IllegalStateException(
                                             "getOutputStream() / getWriter() has already been called for this response, cannot disable output buffering" );
        }
        LOG.debug( "Disabling buffering." );
        this.buffer = null;
    }

    @Override
    public PrintWriter getWriter()
                            throws IOException {
        if ( buffer == null ) {
            return super.getWriter();
        }

        if ( returnType == ReturnType.NOT_DEFINED_YET ) {
            String encoding = getCharacterEncoding();
            if ( encoding == null || "".equals( encoding ) ) {
                encoding = Charset.defaultCharset().name();
            }
            OutputStreamWriter writer = new OutputStreamWriter( outputStream, encoding );
            printWriter = new PrintWriter( writer );
            returnType = ReturnType.PRINT_WRITER;
        }
        if ( returnType == ReturnType.OUTPUT_STREAM ) {
            throw new IllegalStateException( "getOutputStream() has already been called for this response" );
        }
        return printWriter;
    }

    @Override
    public ServletOutputStream getOutputStream()
                            throws IOException {
        if ( buffer == null ) {
            return super.getOutputStream();
        }
        if ( returnType == ReturnType.NOT_DEFINED_YET ) {
            returnType = ReturnType.OUTPUT_STREAM;
        }
        if ( returnType == ReturnType.PRINT_WRITER ) {
            throw new IllegalStateException( "getWriter() has already been called for this response" );
        }
        return outputStream;
    }

    /**
     * Returns an {@link XMLStreamWriter} for writing a response with XML content.
     * <p>
     * NOTE: This method may be called more than once -- the first call will create an {@link XMLStreamWriter} object
     * and subsequent calls return the same object. This provides a convenient means to produce plain XML responses and
     * SOAP wrapped response bodies with the same code.
     * </p>
     * 
     * @return {@link XMLStreamWriter} for writing the response, with XML preamble already written
     * @throws IOException
     * @throws XMLStreamException
     */
    public synchronized XMLStreamWriter getXMLWriter()
                            throws IOException, XMLStreamException {
        if ( xmlWriter == null ) {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            String encoding = "UTF-8";
            xmlWriter = new IndentingXMLStreamWriter( factory.createXMLStreamWriter( getOutputStream(), encoding ) );
            xmlWriter.writeStartDocument( encoding, "1.0" );
            // TODO decide again if character encoding should be set (WFS CITE 1.1.0 tests don't like it, but
            // iGeoDesktop/OpenJUMP currently require it)
            if ( addEncoding ) {
                setCharacterEncoding( "UTF-8" );
            }
        }
        return xmlWriter;
    }

    /**
     * Returns an {@link XMLStreamWriter} for writing a response with XML content.
     * <p>
     * NOTE: This method may be called more than once -- the first call will create an {@link XMLStreamWriter} object
     * and subsequent calls return the same object. This provides a convenient means to produce plain XML responses and
     * SOAP wrapped response bodies with the same code.
     * </p>
     * 
     * @param setCharacterEncoding
     *            true, if the response's character encoding should be set, false otherwise
     * @return {@link XMLStreamWriter} for writing the response, with XML preamble already written
     * @throws IOException
     * @throws XMLStreamException
     */
    public XMLStreamWriter getXMLWriter( boolean setCharacterEncoding )
                            throws IOException, XMLStreamException {

        if ( xmlWriter == null ) {
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            String xmlEncoding = "UTF-8";
            xmlWriter = new IndentingXMLStreamWriter( factory.createXMLStreamWriter( getOutputStream(), xmlEncoding ) );
            xmlWriter.writeStartDocument( xmlEncoding, "1.0" );
            if ( setCharacterEncoding ) {
                setCharacterEncoding( xmlEncoding );
            }
        }
        return xmlWriter;
    }

    /**
     * Performs a schema-based validation of the response (only if the written output has been buffered and was XML).
     */
    public void validate() {

        if ( buffer != null ) {

            boolean isXML = xmlWriter != null || ( getContentType() != null && getContentType().contains( "xml" ) );

            if ( isXML && buffer != null ) {

                long begin = System.currentTimeMillis();
                List<String> messages = null;

                XMLStreamReader reader;
                try {
                    reader = XMLInputFactory.newInstance().createXMLStreamReader( buffer.getInputStream() );
                    reader.nextTag();
                    QName firstElement = reader.getName();
                    if ( new QName( CommonNamespaces.XSNS, "schema" ).equals( firstElement ) ) {
                        LOG.info( "Validating generated XML output (schema document)." );
                        messages = SchemaValidator.validateSchema( buffer.getInputStream() );
                    } else {
                        LOG.info( "Validating generated XML output (instance document)." );
                        messages = new ArrayList<String>();
                        List<SchemaValidationEvent> evts = SchemaValidator.validate( buffer.getInputStream() );
                        for ( SchemaValidationEvent evt : evts ) {
                            messages.add( evt.toString() );
                        }
                    }
                } catch ( Exception e ) {
                    messages = Collections.singletonList( e.getLocalizedMessage() );
                }

                long elapsed = System.currentTimeMillis() - begin;
                if ( messages.size() == 0 ) {
                    LOG.info( "Output is valid. Validation took " + elapsed + " ms." );
                } else {
                    LOG.error( "Output is ***invalid***: " + messages.size() + " error(s)/warning(s). Validation took "
                               + elapsed + " ms." );
                    for ( String msg : messages ) {
                        LOG.error( "***" + msg );
                    }
                }
            }
        }
    }

    @Override
    public void flushBuffer()
                            throws IOException {
        if ( request instanceof LoggingHttpRequestWrapper ) {
            ( (LoggingHttpRequestWrapper) request ).finalizeLogging();
        }
        if ( xmlWriter != null ) {
            try {
                xmlWriter.flush();
            } catch ( XMLStreamException e ) {
                LOG.debug( e.getLocalizedMessage(), e );
                throw new IOException( e );
            }
        }
        if ( buffer != null ) {
            buffer.flush();
            buffer.writeTo( super.getOutputStream() );
            buffer.reset();
        }
        super.flushBuffer();
    }

    @Override
    public void reset() {
        if ( buffer != null && !isCommitted() ) {
            buffer.reset();
            super.reset();
            returnType = ReturnType.NOT_DEFINED_YET;
            xmlWriter = null;
        } else {
            super.reset(); // throws IllegalStateException
        }
    }

    @Override
    public int getBufferSize() {
        if ( buffer != null ) {
            return buffer.size();
        }
        return super.getBufferSize();
    }

    /**
     * @return the buffer
     */
    public OutputStream getBuffer() {
        return buffer;
    }

    /**
     * This is a ServletOutputStream that uses our internal ByteArrayOutputStream to buffer all data.
     */
    private static class BufferedServletOutputStream extends ServletOutputStream {

        private final OutputStream buffer;

        public BufferedServletOutputStream( OutputStream buffer ) {
            this.buffer = buffer;
        }

        @Override
        public void write( byte[] b )
                                throws IOException {
            buffer.write( b );
        }

        @Override
        public void write( byte[] b, int off, int len )
                                throws IOException {
            buffer.write( b, off, len );
        }

        @Override
        public void write( int b )
                                throws IOException {
            buffer.write( b );
        }
    }
}
