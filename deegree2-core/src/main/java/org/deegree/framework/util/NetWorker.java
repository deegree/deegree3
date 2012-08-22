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
package org.deegree.framework.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 * Performs a HTTP request using the service URL submitted to the constructor
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class NetWorker {

    private static final ILogger LOG = LoggerFactory.getLogger( NetWorker.class );

    private static final int GET = 0;

    private static final int POST = 1;

    private String contentType = null;

    private String request = null;

    private URL url = null;

    private int reqType = -1;

    private String encoding = null;

    /**
     * constructor for initializing a HTTP GET connection with with default character encoding
     *
     * @param url
     *            URL to the net resource containing the URI
     */
    public NetWorker( URL url ) {

        this.reqType = GET;
        this.url = url;
        this.encoding = CharsetUtils.getSystemCharset();

    }

    /**
     * constructor for initializing a HTTP GET connection with a user defined encoding
     *
     * @param encoding
     *            desired character encoding
     * @param url
     *            URL to the net resource containing the URI
     */
    public NetWorker( String encoding, URL url ) {
        this.reqType = GET;
        this.url = url;
        this.encoding = encoding;

    }

    /**
     * constructor for initializing a HTTP POST connection with UTF-8 as character encoding
     *
     * @param url
     *            URL to the net resource (without URI parameters)
     * @param request
     *            request that shall be posted to the net resource
     */
    public NetWorker( URL url, String request ) {

        this.reqType = POST;
        this.url = url;
        this.request = request;
        this.encoding = "UTF-8";

    }

    /**
     * constructor for initializing a HTTP POST connection with a user defined encoding
     *
     * @param encoding
     *            desired character encoding
     * @param url
     *            URL to the net resource (without URI parameters)
     * @param request
     *            request that shall be posted to the net resource
     */
    public NetWorker( String encoding, URL url, String request ) {
        this.reqType = POST;
        this.url = url;
        this.request = request;
        this.encoding = encoding;

    }

    /**
     * @return the content type of the response from the connected net resource. this method shall
     *         be called after <tt>getInputStream</tt> or <tt>getDataAsByteArr</tt> has been
     *         called.
     *
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * sends the request that have been passed to the constructor without expecting to receive a
     * response.
     *
     * @throws IOException
     */
    public void sendRequest()
                            throws IOException {

        LOG.logDebug( "Trying to send request " + request + " via POST with encoding " + encoding );

        // open connection to the requested host
        URLConnection connection = url.openConnection();

        connection.setDoInput( false );

        // sets the content type of the request
        connection.setRequestProperty( "Content-Type", "text/xml" );

        if ( ( reqType == POST ) && ( request != null ) ) {
            connection.setDoOutput( true );

            // get connection stream
            OutputStreamWriter osw = new OutputStreamWriter( connection.getOutputStream(), encoding );
            PrintWriter os = new PrintWriter( osw );

            // write post request into stream
            os.print( request );
            os.close();
        } else {
            connection.setDoOutput( false );
        }

        LOG.logDebug( "Sent request." );
    }

    /**
     * returns an <tt>InputStream</tt> from the et resource
     *
     * @return InputStream accessing the net resource
     *
     * @throws IOException
     */
    public InputStream getInputStream()
                            throws IOException {

        // open connection to the requested host
        URLConnection connection = url.openConnection();

        connection.setDoInput( true );

        // sets the content type of the request
        connection.setRequestProperty( "Content-Type", "text/xml" );

        // provide result stream
        InputStream is = null;

        if ( ( reqType == POST ) && ( request != null ) ) {
            connection.setDoOutput( true );

            // get connection stream
            OutputStreamWriter osw = new OutputStreamWriter( connection.getOutputStream(), encoding );
            PrintWriter os = new PrintWriter( osw );

            // write post request into stream
            os.print( request );
            os.close();
        } else {
            connection.setDoOutput( false );
        }

        // reads the content type of the connected net resource
        try {
            contentType = connection.getHeaderField( "Content-Type" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        // get result of the request
        try {
            is = connection.getInputStream();
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new IOException( "could not provide data: " + e );
        }

        return is;
    }

    /**
     * performs the request and returns the result as a byte array.
     *
     * @param expectedDataSize
     *            size a the data in bytes expected to be returned from the net resource. this value
     *            will be replaced if the resource is able to return the available data size.
     * @return a byte array containing the content of the net resource
     *
     * @throws IOException
     */
    public byte[] getDataAsByteArr( int expectedDataSize )
                            throws IOException {

        InputStream is = getInputStream();

        if ( expectedDataSize <= 0 ) {
            expectedDataSize = 10000;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream( expectedDataSize );

        int v = 0;

        // write result to a ByteArrayOutputStream
        while ( ( v = is.read() ) > -1 ) {
            bos.write( v );
        }

        bos.flush();
        bos.close();

        is.close();

        // return result as byte array
        return bos.toByteArray();
    }

    /**
     * Returns the original form of a <tt>URL</tt> as as <tt>String</tt>. Handles local
     * filenames correctly, C:/foo is formatted as file:///C:/foo (and not as file:/C:/foo as
     * returned by the toString () method of the <tt<URL</tt> object.
     * <p>
     *
     * @param url
     *            <tt>URL</tt> to be converted
     * @return <tt>String</tt> representation of the given <tt>URL</tt>
     */
    public static synchronized String url2String( URL url ) {
        String port = "";

        if ( url.getPort() != -1 ) {
            port = ":" + url.getPort();
        }

        String s = url.getProtocol() + "://" + url.getHost() + port + url.getPath();

        return s;
    }

    /**
     * @param url
     * @return true if a connection to the submitted <tt>URL</tt> can be opend
     */
    public static synchronized boolean existsURL( URL url ) {
        try {
            URLConnection con = url.openConnection();
            con.connect();
            con.getContentType();
        } catch ( Exception e ) {
            return false;
        }
        return true;
    }

}
