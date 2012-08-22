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

import static java.net.URLDecoder.decode;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.StringTools;

/**
 * TODO describe function and usage of the class here.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: mays$
 * 
 * @version $Revision$, $Date: 23.05.2007 18:09:52$
 */
public class ServletRequestWrapper extends HttpServletRequestWrapper {

    private static ILogger LOG = LoggerFactory.getLogger( ServletRequestWrapper.class );

    private static final String BUNDLE_NAME = "org.deegree.enterprise.servlet.ServletRequestWrapper";

    /**
     * The resource to load the users from.
     */
    static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

    private HttpServletRequest origReq = null;

    private byte[] bytes = null;

    private Map<String, String[]> paramMap;

    private String queryString;

    private String method;

    /**
     * @param request
     */
    public ServletRequestWrapper( HttpServletRequest request ) {
        super( request );

        this.origReq = request;
        this.method = request.getMethod();

        ByteArrayOutputStream bos = new ByteArrayOutputStream( 10000 );
        try {
            InputStream is = origReq.getInputStream();
            int c = 0;
            while ( ( c = is.read() ) > -1 ) {
                bos.write( c );
            }
            bytes = bos.toByteArray();
            LOG.logDebug( "The constructor created a new bytearray in the HttpServletRequestWrapper" );
        } catch ( IOException ioe ) {
            LOG.logError( "An error occured while creating a byte-buffered inputstream from the HttpServletRequest "
                          + "inputstream because: " + ioe.getMessage(), ioe );
            bytes = null;
        }
        queryString = request.getQueryString();
        // init parameter map
        getParameterMap();
    }

    @SuppressWarnings("unchecked")
    public void reinitParameterMap() {
        paramMap = new HashMap<String, String[]>();

        // encoding heuristics for URL encoding
        // if %c3 is found (a sign of UTF-8 encoding) parse it manually, setting the encoding right
        if ( queryString != null && queryString.toLowerCase().indexOf( "%c3" ) != -1 ) {
            try {
                for ( String kv : queryString.split( "&" ) ) {
                    String[] pair = kv.split( "=", 2 );
                    if ( pair.length == 2 ) {
                        paramMap.put( decode( pair[0], "UTF-8" ), decode( pair[1], "UTF-8" ).split( "," ) );
                    }
                }
            } catch ( UnsupportedEncodingException e ) {
                LOG.logError( "Unknown error", e );
            }
        } else {
            paramMap = super.getParameterMap();
        }
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if ( paramMap == null ) {
            reinitParameterMap();
        }
        return paramMap;
    }

    @Override
    public String getParameter( String key ) {
        if ( paramMap == null ) {
            paramMap = getParameterMap();
        }
        String[] o = paramMap.get( key );
        String tmp = null;
        if ( o != null ) {
            tmp = StringTools.arrayToString( o, ',' );
        }
        return tmp;
    }

    @Override
    public String[] getParameterValues( String arg0 ) {
        if ( paramMap == null ) {
            paramMap = getParameterMap();
        }
        return paramMap.get( arg0 );
    }

    /**
     * 
     * @param param
     */
    public void setParameter( Map<String, String> param ) {
        this.paramMap = new HashMap<String, String[]>( param.size() );

        Iterator<String> iter = param.keySet().iterator();
        StringBuffer sb = new StringBuffer( 500 );
        while ( iter.hasNext() ) {
            String key = iter.next();
            String value = param.get( key );
            sb.append( key ).append( '=' ).append( value );
            if ( iter.hasNext() ) {
                sb.append( '&' );
            }
            this.paramMap.put( key, StringTools.toArray( value, ",", false ) );
        }
        this.queryString = sb.toString();
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    /**
     * marks an instance of a {@link #ServletRequestWrapper(HttpServletRequest)} as using HTTP POST. This method just
     * should be invoked if a request body as an @see {@link InputStream} (byte array) is available
     */
    public void markAsPostRequest() {
        method = "POST";
        if ( bytes == null || bytes.length == 0 ) {
            LOG.logWarning( "no request body as an InputStream (byte array) is available" );
        }
        // a post request shall not have a parameter string
        this.queryString = null;
        this.paramMap = new HashMap<String, String[]>();
    }

    @Override
    public String getMethod() {
        return method;
    }

    /**
     * sets the content of the @see {@link InputStream} returned by the
     * 
     * @see #getReader() and the
     * @see #getInputStream() method as a byte array. Calling this method will override the content that may has been
     *      read from the <code>HttpServletRequest</code> that has been passed to the constructor
     * 
     * @param b
     */
    public void setInputStreamAsByteArray( byte[] b ) {
        LOG.logDebug( "ServletRequestWrapper: setting inputstream#byteArray to given byte array" );
        this.bytes = b;
        markAsPostRequest();
    }

    @Override
    public BufferedReader getReader()
                            throws IOException {
        return new BufferedReader( new InputStreamReader( getInputStream(), CharsetUtils.getSystemCharset() ) );
    }

    /**
     * @see javax.servlet.ServletRequest#getInputStream()
     */
    @Override
    public ServletInputStream getInputStream()
                            throws IOException {
        if ( bytes == null ) {
            LOG.logDebug( "Creating new bytearray in the HttpServletRequestWrapper#getInputStream" );
            ByteArrayOutputStream bos = new ByteArrayOutputStream( 10000 );
            InputStream is = origReq.getInputStream();
            int c = 0;
            while ( ( c = is.read() ) > -1 ) {
                bos.write( c );
            }
            bytes = bos.toByteArray();
        }

        return new ProxyServletInputStream( new ByteArrayInputStream( bytes ), bytes.length );
    }

    @Override
    public Principal getUserPrincipal() {
        if ( origReq.getUserPrincipal() != null ) {
            return origReq.getUserPrincipal();
        }
        return new Principal() {
            public String getName() {
                return RESOURCE_BUNDLE.getString( "defaultuser" );
            }
        };

    }

    // ///////////////////////////////////////////////////////////////////////
    // inner classes //
    // ///////////////////////////////////////////////////////////////////////

    /**
     * @author Administrator
     * 
     *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code
     *         Style - Code Templates
     */
    private class ProxyServletInputStream extends ServletInputStream {

        private BufferedInputStream buffered;

        /**
         * @param in
         *            the InputStream which will be buffered.
         * @param length
         */
        public ProxyServletInputStream( InputStream in, int length ) {
            if ( length > 0 )
                buffered = new BufferedInputStream( in, length );
            else
                buffered = new BufferedInputStream( in );
        }

        @Override
        public synchronized int read()
                                throws IOException {
            return buffered.read();
        }

        @Override
        public synchronized int read( byte b[], int off, int len )
                                throws IOException {
            return buffered.read( b, off, len );
        }

        @Override
        public synchronized long skip( long n )
                                throws IOException {
            return buffered.skip( n );
        }

        @Override
        public synchronized int available()
                                throws IOException {
            return buffered.available();
        }

        @Override
        public synchronized void mark( int readlimit ) {
            buffered.mark( readlimit );
        }

        @Override
        public synchronized void reset()
                                throws IOException {
            buffered.reset();
        }

        @Override
        public boolean markSupported() {
            return buffered.markSupported();
        }

        @Override
        public void close()
                                throws IOException {
            buffered.close();
        }
    }

}
