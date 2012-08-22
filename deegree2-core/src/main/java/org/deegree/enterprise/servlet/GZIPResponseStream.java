//$HeadURL$
/*----------------------------------------------------------------------------
 This file originated as work from Jayson Falkner.

 Copyright 2003 Jayson Falkner (jayson@jspinsider.com)
 This code is from "Servlets and JavaServer pages; the J2EE Web Tier",
 http://www.jspbook.com. You may freely use the code both commercially
 and non-commercially. If you like the code, please pick up a copy of
 the book and help support the authors, development of more free code,
 and the JSP/Servlet/J2EE community.
 ----------------------------------------------------------------------------*/
package org.deegree.enterprise.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 *
 *
 * @author <a href="mailto:jayson@jspinsider.com">Jayson Falkner</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GZIPResponseStream extends ServletOutputStream {
    /**
     * the output as a byte array
     */
    protected ByteArrayOutputStream baos = null;

    /**
     * the output as a gzipped stream
     */
    protected GZIPOutputStream gzipstream = null;

    /**
     * true if the stream is closed
     */
    protected boolean closed = false;

    /**
     * the response wrapper
     */
    protected HttpServletResponse response = null;

    /**
     * the output as a stream
     */
    protected ServletOutputStream output = null;

    /**
     *
     * @param response
     * @throws IOException
     */
    public GZIPResponseStream( HttpServletResponse response ) throws IOException {
        super();
        closed = false;
        this.response = response;
        this.output = response.getOutputStream();
        baos = new ByteArrayOutputStream();
        gzipstream = new GZIPOutputStream( baos );
    }

    /**
     * @throws IOException
     */
    @Override
    public void close()
                            throws IOException {
        if ( closed ) {
            throw new IOException( "This output stream has already been closed" );
        }
        gzipstream.finish();

        byte[] bytes = baos.toByteArray();

        response.addHeader( "Content-Length", Integer.toString( bytes.length ) );
        response.addHeader( "Content-Encoding", "gzip" );
        output.write( bytes );
        output.flush();
        output.close();
        closed = true;
    }

    /**
     * @throws IOException
     */
    @Override
    public void flush()
                            throws IOException {
        if ( closed ) {
            throw new IOException( "Cannot flush a closed output stream" );
        }
        gzipstream.flush();
    }

    /**
     * @param b
     *            data to write
     * @throws IOException
     */
    @Override
    public void write( int b )
                            throws IOException {
        if ( closed ) {
            throw new IOException( "Cannot write to a closed output stream" );
        }
        gzipstream.write( (byte) b );
    }

    /**
     * @param b
     *            data array to write
     * @throws IOException
     */
    @Override
    public void write( byte b[] )
                            throws IOException {
        write( b, 0, b.length );
    }

    /**
     * @param b
     *            data array to write
     * @param off
     *            index of the for byte
     * @param len
     *            number of bytes to write
     * @throws IOException
     */
    @Override
    public void write( byte b[], int off, int len )
                            throws IOException {
        System.out.println( "writing..." );
        if ( closed ) {
            throw new IOException( "Cannot write to a closed output stream" );
        }
        gzipstream.write( b, off, len );
    }

    /**
     *
     * @return true if already has been closed
     */
    public boolean closed() {
        return ( this.closed );
    }

    /**
     *
     *
     */
    public void reset() {
        // noop
    }
}