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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GZIPResponseWrapper extends HttpServletResponseWrapper {
    /**
     * the original response
     */
    protected HttpServletResponse origResponse = null;

    /**
     * the output as a stream
     */
    protected ServletOutputStream stream = null;

    /**
     * the output as a writer
     */
    protected PrintWriter writer = null;

    /**
     *
     * @param response
     */
    public GZIPResponseWrapper( HttpServletResponse response ) {
        super( response );
        origResponse = response;
    }

    /**
     *
     * @return response stream
     * @throws IOException
     */
    public ServletOutputStream createOutputStream()
                            throws IOException {
        return ( new GZIPResponseStream( origResponse ) );
    }

    /**
     *
     *
     */
    public void finishResponse() {
        try {
            if ( writer != null ) {
                writer.close();
            } else {
                if ( stream != null ) {
                    stream.close();
                }
            }
        } catch ( IOException e ) {
            //notting todo?
        }
    }

    /**
     * @throws IOException
     */
    @Override
    public void flushBuffer()
                            throws IOException {
        stream.flush();
    }

    /**
     * @return ServletOutputStream
     * @throws IOException
     */
    @Override
    public ServletOutputStream getOutputStream()
                            throws IOException {
        if ( writer != null ) {
            throw new IllegalStateException( "getWriter() has already been called!" );
        }

        if ( stream == null )
            stream = createOutputStream();
        return ( stream );
    }

    /**
     * @return PrintWriter
     * @throws IOException
     */
    @Override
    public PrintWriter getWriter()
                            throws IOException {
        if ( writer != null ) {
            return ( writer );
        }

        if ( stream != null ) {
            throw new IllegalStateException( "getOutputStream() has already been called!" );
        }

        stream = createOutputStream();
        writer = new PrintWriter( new OutputStreamWriter( stream, "UTF-8" ) );
        return ( writer );
    }

    @Override
    public void setContentLength( int length ) {
        super.setContentLength( length );
    }
}