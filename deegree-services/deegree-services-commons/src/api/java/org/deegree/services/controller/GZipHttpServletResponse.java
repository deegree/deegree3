/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.services.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HttpServletResponse} that copes with gzipping the output.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * 
 */
public class GZipHttpServletResponse implements HttpServletResponse {

    private static Logger LOG = LoggerFactory.getLogger( GZipHttpServletResponse.class );

    private final HttpServletResponse response;

    private GZipServletOutputStream gos;

    /**
     * 
     * @param response
     */
    public GZipHttpServletResponse( HttpServletResponse response ) {
        this.response = response;
    }

    /**
     * @param cookie
     * @see jakarta.servlet.http.HttpServletResponse#addCookie(jakarta.servlet.http.Cookie)
     */
    public void addCookie( Cookie cookie ) {
        response.addCookie( cookie );
    }

    /**
     * @param name
     * @return
     * @see jakarta.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
     */
    public boolean containsHeader( String name ) {
        return response.containsHeader( name );
    }

    /**
     * @param url
     * @return
     * @see jakarta.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
     */
    public String encodeURL( String url ) {
        return response.encodeURL( url );
    }

    /**
     * @return
     * @see jakarta.servlet.ServletResponse#getCharacterEncoding()
     */
    public String getCharacterEncoding() {
        return response.getCharacterEncoding();
    }

    /**
     * @param url
     * @return
     * @see jakarta.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
     */
    public String encodeRedirectURL( String url ) {
        return response.encodeRedirectURL( url );
    }

    /**
     * @return
     * @see jakarta.servlet.ServletResponse#getContentType()
     */
    public String getContentType() {
        return response.getContentType();
    }

    /**
     * @param url
     * @return
     * @deprecated
     * @see jakarta.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
     */
    public String encodeUrl( String url ) {
        return response.encodeURL( url );
    }

    /**
     * @param url
     * @return
     * @deprecated
     * @see jakarta.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
     */
    public String encodeRedirectUrl( String url ) {
        return response.encodeRedirectURL( url );
    }

    /**
     * @return
     * @throws IOException
     * @see jakarta.servlet.ServletResponse#getOutputStream()
     */
    public ServletOutputStream getOutputStream()
                            throws IOException {
        if ( gos == null ) {
            gos = new GZipServletOutputStream( this, response.getOutputStream() );
        }
        return gos;
    }

    /**
     * @param sc
     * @param msg
     * @throws IOException
     * @see jakarta.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
     */
    public void sendError( int sc, String msg )
                            throws IOException {
        response.sendError( sc, msg );
    }

    /**
     * @return
     * @throws IOException
     * @see jakarta.servlet.ServletResponse#getWriter()
     */
    public PrintWriter getWriter()
                            throws IOException {
        return response.getWriter();
    }

    /**
     * @param sc
     * @throws IOException
     * @see jakarta.servlet.http.HttpServletResponse#sendError(int)
     */
    public void sendError( int sc )
                            throws IOException {
        response.sendError( sc );
    }

    /**
     * @param location
     * @throws IOException
     * @see jakarta.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
     */
    public void sendRedirect( String location )
                            throws IOException {
        response.sendRedirect( location );
    }

    /**
     * @param charset
     * @see jakarta.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
     */
    public void setCharacterEncoding( String charset ) {
        response.setCharacterEncoding( charset );
    }

    /**
     * @param name
     * @param date
     * @see jakarta.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
     */
    public void setDateHeader( String name, long date ) {
        response.setDateHeader( name, date );
    }

    /**
     * @param name
     * @param date
     * @see jakarta.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
     */
    public void addDateHeader( String name, long date ) {
        response.addDateHeader( name, date );
    }

    /**
     * @param name
     * @param value
     * @see jakarta.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
     */
    public void setHeader( String name, String value ) {
        response.setHeader( name, value );
    }

    /**
     * @param len
     * @see jakarta.servlet.ServletResponse#setContentLength(int)
     */
    public void setContentLength( int len ) {
        LOG.warn( "setContentLength() is not supported for gzipped responses" );
    }

    @Override
    public void setContentLengthLong(long l) {

    }

    /**
     * @param type
     * @see jakarta.servlet.ServletResponse#setContentType(java.lang.String)
     */
    public void setContentType( String type ) {
        response.setContentType( type );
    }

    /**
     * @param name
     * @param value
     * @see jakarta.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
     */
    public void addHeader( String name, String value ) {
        response.addHeader( name, value );
    }

    /**
     * @param name
     * @param value
     * @see jakarta.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
     */
    public void setIntHeader( String name, int value ) {
        response.setIntHeader( name, value );
    }

    /**
     * @param name
     * @param value
     * @see jakarta.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
     */
    public void addIntHeader( String name, int value ) {
        response.addIntHeader( name, value );
    }

    /**
     * @param size
     * @see jakarta.servlet.ServletResponse#setBufferSize(int)
     */
    public void setBufferSize( int size ) {
        response.setBufferSize( size );
    }

    /**
     * @param sc
     * @see jakarta.servlet.http.HttpServletResponse#setStatus(int)
     */
    public void setStatus( int sc ) {
        response.setStatus( sc );
    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getHeader(String s) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String s) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }

    /**
     * @return
     * @see jakarta.servlet.ServletResponse#getBufferSize()
     */
    public int getBufferSize() {
        return response.getBufferSize();
    }

    /**
     * @throws IOException
     * @see jakarta.servlet.ServletResponse#flushBuffer()
     */
    public void flushBuffer()
                            throws IOException {
        if ( gos != null ) {
            if ( !gos.isClosed() ) {
                gos.close();
            }
        }
        response.flushBuffer();
    }

    /**
     * 
     * @see jakarta.servlet.ServletResponse#resetBuffer()
     */
    public void resetBuffer() {
        response.resetBuffer();
    }

    /**
     * @return
     * @see jakarta.servlet.ServletResponse#isCommitted()
     */
    public boolean isCommitted() {
        return response.isCommitted();
    }

    /**
     * 
     * @see jakarta.servlet.ServletResponse#reset()
     */
    public void reset() {
        response.reset();
    }

    /**
     * @param loc
     * @see jakarta.servlet.ServletResponse#setLocale(java.util.Locale)
     */
    public void setLocale( Locale loc ) {
        response.setLocale( loc );
    }

    /**
     * @return
     * @see jakarta.servlet.ServletResponse#getLocale()
     */
    public Locale getLocale() {
        return response.getLocale();
    }
}