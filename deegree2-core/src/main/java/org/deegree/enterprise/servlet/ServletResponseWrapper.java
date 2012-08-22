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
package org.deegree.enterprise.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * The <code>ServletResponse</code> class is a wrapper for an HttpServletResponse object. It allows to repeatedly
 * access the stream, without emptying it.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class ServletResponseWrapper extends HttpServletResponseWrapper {

    /**
     * The output as a stream
     */
    protected ServletOutputStream stream = null;

    /**
     * The output as a writer
     */
    protected PrintWriter writer = null;

    /**
     * the original response
     */
    protected HttpServletResponse origResponse = null;

    private String contentType = null;

    /**
     *
     * @param response
     */
    public ServletResponseWrapper( HttpServletResponse response ) {
        super( response );
        origResponse = response;
    }

    /**
     * It is possible to re-send the response of an already handled request (by a servlet) as a new request object. The
     * new response will then be added (at the end) of the first response, if -for some reason- the (new) response alone
     * should be send to the client. In this case the response stream must be reset before it is sent to the client
     * anew. This is what this method is for.
     */
    @Override
    public void reset() {
        createOutputStream();
    }

    /**
     * set the local variable to new stream.
     */
    private void createOutputStream() {
        stream = new ProxyServletOutputStream( 10000 );
    }

    @Override
    public ServletOutputStream getOutputStream()
                            throws IOException {
        if ( stream == null ) {
            createOutputStream();
        }
        return stream;
    }

    @Override
    public PrintWriter getWriter()
                            throws IOException {
        if ( writer != null ) {
            return writer;
        }
        createOutputStream();
        writer = new PrintWriter( stream );
        return writer;
    }

    @Override
    public void setContentType( String contentType ) {
        this.contentType = contentType;
        if ( contentType != null ) {
            super.setContentType( contentType );
        }
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    // ///////////////////////////////////////////////////////////////////////
    // inner classes //
    // ///////////////////////////////////////////////////////////////////////

    /**
     * The <code>ProxyServletOutputStream</code> class is a wrapper for OutputStream object thus allowing repeated
     * access to the stream.
     *
     * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
     *
     * @author last edited by: $Author$
     *
     * @version $Revision$, $Date$
     *
     */
    public class ProxyServletOutputStream extends ServletOutputStream {

        private ByteArrayOutputStream bos = null;

        /**
         * @param length
         *            of the buffer
         */
        public ProxyServletOutputStream( int length ) {
            if ( length > 0 )
                bos = new ByteArrayOutputStream( length );
            else
                bos = new ByteArrayOutputStream( 10000 );
        }

        @Override
        public void close()
                                throws IOException {
            bos.close();
        }

        @Override
        public void flush()
                                throws IOException {
            bos.flush();
        }

        @Override
        public void write( byte[] b, int off, int len )
                                throws IOException {
            bos.write( b, off, len );
        }

        @Override
        public void write( byte[] b )
                                throws IOException {
            bos.write( b );
        }

        @Override
        public void write( int v )
                                throws IOException {
            bos.write( v );
        }

        /**
         * @return the actual bytes of the stream.
         */
        public byte[] toByteArray() {
            return bos.toByteArray();
        }

        /**
         * @param enc
         *            encoding to which the bytes must encoded.
         * @return a string representation of the byte array with the given encoding.
         * @throws UnsupportedEncodingException
         *             if the encoding is not supported
         */
        public String toString( String enc )
                                throws UnsupportedEncodingException {
            return bos.toString( enc );
        }
    }
}
