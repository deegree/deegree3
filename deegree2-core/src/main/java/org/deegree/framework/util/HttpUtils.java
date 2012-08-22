//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.framework.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.deegree.enterprise.WebUtils;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;

/**
 * utility class for performing HTTP requests
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public class HttpUtils {

    private static final ILogger LOG = LoggerFactory.getLogger( HttpUtils.class );

    /**
     * validates passed URL. If it is not a valid URL or a client can not connect to it an exception will be thrown
     * 
     * @param url
     * @throws IOException
     */
    public static int validateURL( String url )
                            throws IOException {
        return validateURL( url, null, null );
    }

    /**
     * validates passed URL. If it is not a valid URL or a client can not connect to it an exception will be thrown
     * 
     * @param url
     * @param user
     * @param password
     * @throws IOException
     */
    public static int validateURL( String url, String user, String password )
                            throws IOException {
        if ( url.startsWith( "http:" ) ) {
            URL tmp = new URL( url );
            HeadMethod hm = new HeadMethod( url );
            setHTTPCredentials( hm, user, password );
            InetAddress.getByName( tmp.getHost() );
            HttpClient client = new HttpClient();
            client.executeMethod( hm );
            if ( hm.getStatusCode() != HttpURLConnection.HTTP_OK ) {
                if ( hm.getStatusCode() != HttpURLConnection.HTTP_UNAUTHORIZED && hm.getStatusCode() != 401 ) {
                    // this method just evaluates if a URL/host is valid; it does not takes care
                    // if authorization is available/valid
                    throw new IOException( "Host " + tmp.getHost() + " of URL + " + url + " does not exists" );
                }
            }
            return hm.getStatusCode();
        } else if ( url.startsWith( "file:" ) ) {
            URL tmp = new URL( url );
            InputStream is = tmp.openStream();
            is.close();
            return 200;
        }
        return HttpURLConnection.HTTP_UNAVAILABLE;
    }

    /**
     * 
     * @param url
     * @param content
     * @param timeout
     *            timeout in milliseconds
     * @param user
     *            (can be <code>null</code>)
     * @param password
     *            (can be <code>null</code>)
     * @param contentType
     *            request content mime type (can be <code>null</code>)
     * @param encoding
     *            request encoding (can be <code>null</code>)
     * @param header
     * 
     * @return result of http post request
     * @throws HttpException
     * @throws IOException
     */
    public static HttpMethod performHttpPost( String url, InputStream content, int timeout, String user,
                                              String password, String contentType, String encoding,
                                              Map<String, String> header )
                            throws HttpException, IOException {
        HttpClient client = new HttpClient();
        URL tmp = new URL( url );
        WebUtils.enableProxyUsage( client, tmp );
        url = tmp.toExternalForm();
        client.getHttpConnectionManager().getParams().setSoTimeout( timeout );
        client.getHttpConnectionManager().getParams().setConnectionTimeout( timeout );

        PostMethod pm = new PostMethod( url );
        String ct = contentType;
        if ( ct != null && encoding != null ) {
            ct += ( "; " + encoding );
        }
        if ( ct != null ) {
            pm.setRequestEntity( new InputStreamRequestEntity( content, ct ) );
        } else {
            pm.setRequestEntity( new InputStreamRequestEntity( content ) );
        }
        if ( header != null ) {
            Iterator<String> iter = header.keySet().iterator();
            while ( iter.hasNext() ) {
                String key = (String) iter.next();
                if ( !"content-length".equalsIgnoreCase( key ) ) {
                    pm.addRequestHeader( new Header( key, header.get( key ) ) );
                }
            }
        }

        setHTTPCredentials( pm, user, password );
        client.executeMethod( pm );
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( pm.getResponseBodyAsString() );
        }
        if ( pm.getStatusCode() != 200 ) {
            throw new HttpException( "status code: " + pm.getStatusCode() );
        }
        return pm;
    }

    /**
     * 
     * @param url
     * @param content
     * @param timeout
     *            timeout in milliseconds
     * @param user
     *            (can <code>null</code>)
     * @param password
     *            (can <code>null</code>)
     * @param contentType
     *            request content mime type (can be <code>null</code>)
     * @param encoding
     *            request encoding (can be <code>null</code>)
     * @param header
     * @return result of http post request
     * @throws HttpException
     * @throws IOException
     */
    public static HttpMethod performHttpPost( String url, String content, int timeout, String user, String password,
                                              String contentType, String encoding, Map<String, String> header )
                            throws HttpException, IOException {
        HttpClient client = new HttpClient();
        URL tmp = new URL( url );
        WebUtils.enableProxyUsage( client, tmp );
        url = tmp.toExternalForm();
        client.getHttpConnectionManager().getParams().setSoTimeout( timeout );
        client.getHttpConnectionManager().getParams().setConnectionTimeout( timeout );
        PostMethod pm = new PostMethod( url );

        pm.setRequestEntity( new StringRequestEntity( content, contentType, encoding ) );

        if ( header != null ) {
            Iterator<String> iter = header.keySet().iterator();
            while ( iter.hasNext() ) {
                String key = (String) iter.next();
                if ( !"content-length".equalsIgnoreCase( key ) ) {
                    pm.addRequestHeader( new Header( key, header.get( key ) ) );
                }
            }
        }
        pm.addRequestHeader( new Header( "content-length", Integer.toString( content.getBytes().length ) ) );

        setHTTPCredentials( pm, user, password );
        client.executeMethod( pm );
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( pm.getResponseBodyAsString() );
        }
        if ( pm.getStatusCode() != 200 ) {
            throw new HttpException( "status code: " + pm.getStatusCode() );
        }
        return pm;
    }

    /**
     * 
     * @param url
     * @param content
     * @param timeout
     *            timeout in milliseconds
     * @param user
     *            (can <code>null</code>)
     * @param password
     *            (can <code>null</code>)
     * @param header
     * @return result of http post request
     * @throws HttpException
     * @throws IOException
     */
    public static HttpMethod performHttpPost( String url, XMLFragment content, int timeout, String user,
                                              String password, Map<String, String> header )
                            throws HttpException, IOException {
        
        HttpClient client = new HttpClient();
        URL tmp = new URL( url );
        WebUtils.enableProxyUsage( client, tmp );
        url = tmp.toExternalForm();
        client.getHttpConnectionManager().getParams().setSoTimeout( timeout );
        client.getHttpConnectionManager().getParams().setConnectionTimeout( timeout );
        PostMethod pm = new PostMethod( url );        

        ByteArrayOutputStream bos = new ByteArrayOutputStream( 1000000 );
        Properties props = new Properties();
        props.put( OutputKeys.ENCODING, "UTF-8" );
        content.write( bos, props );        

        pm.setRequestEntity( new ByteArrayRequestEntity( bos.toByteArray(), "text/xml" ) );

        if ( header != null ) {
            Iterator<String> iter = header.keySet().iterator();
            while ( iter.hasNext() ) {
                String key = (String) iter.next();
                if ( !"content-length".equalsIgnoreCase( key ) ) {
                    pm.addRequestHeader( new Header( key, header.get( key ) ) );
                }
            }
        }
        pm.addRequestHeader( new Header( "content-length", Integer.toString( bos.toByteArray().length ) ) );
        bos.close();

        setHTTPCredentials( pm, user, password );
        client.executeMethod( pm );
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( pm.getResponseBodyAsString() );
        }
        if ( pm.getStatusCode() != 200 ) {
            throw new HttpException( "status code: " + pm.getStatusCode() );
        }
        return pm;
    }

    /**
     * 
     * @param url
     *            e.g. http://localhost:8080/deegree/services
     * @param request
     *            e.g. service=WMS&request=GetCapabilities
     * @param timeout
     *            timeout in milliseconds
     * @param user
     *            (can be <code>null</code>)
     * @param password
     *            (can be <code>null</code>)
     * @param header
     * @return result of http get request
     * @throws HttpException
     * @throws IOException
     */
    public static HttpMethod performHttpGet( String url, String request, int timeout, String user, String password,
                                             Map<String, String> header )
                            throws HttpException, IOException {
        if ( request != null && request.startsWith( "&" ) ) {
            request = request.substring( 1 );
        }
        if ( url != null && url.endsWith( "?" ) ) {
            url = url.substring( 0, url.length() - 1 );
        }
        LOG.logDebug( "HTTP GET URL: ", url );
        LOG.logDebug( "HTTP GET request: ", request );
        GetMethod gm = null;
        if ( url.indexOf( '?' ) > -1 && request != null ) {
            gm = new GetMethod( StringTools.concat( 500, url, '&', request ) );
        } else if ( request != null && !request.startsWith( "http://" ) ) {
            gm = new GetMethod( StringTools.concat( 500, url, '?', request ) );
        } else if ( request != null && request.startsWith( "http://" ) ) {
            gm = new GetMethod( request );
        } else {
            gm = new GetMethod( url );
        }

        if ( header != null ) {
            Iterator<String> iter = header.keySet().iterator();
            while ( iter.hasNext() ) {
                String key = (String) iter.next();
                if ( !"content-length".equalsIgnoreCase( key ) ) {
                    gm.addRequestHeader( new Header( key, header.get( key ) ) );
                }

            }
        }

        setHTTPCredentials( gm, user, password );

        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setSoTimeout( timeout );
        client.getHttpConnectionManager().getParams().setConnectionTimeout( timeout );
        WebUtils.enableProxyUsage( client, new URL( url ) );
        client.executeMethod( gm );
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( gm.getResponseBodyAsString() );
        }
        if ( gm.getStatusCode() != 200 ) {
            throw new HttpException( "status code: " + gm.getStatusCode() );
        }
        return gm;
    }

    /**
     * 
     * @param url
     * @param user
     * @param password
     * @param sessionID
     * @return URL with attached authentication information (if not null)
     */
    public static String addAuthenticationForKVP( String url, String user, String password, String sessionID ) {
        if ( sessionID != null && sessionID.trim().length() > 0 ) {
            url = url + "&sessionID=" + sessionID;
        } else if ( user != null ) {
            url = url + "&user=" + user;
            if ( password == null ) {
                password = "";
            }
            url = url + "&password=" + password;
        }
        return url;
    }

    /**
     * 
     * @param xml
     * @param user
     * @param password
     * @param sessionID
     * @return XML document with authentication information (if not null) as attributes of the root element
     */
    public static XMLFragment addAuthenticationForXML( XMLFragment xml, String user, String password, String sessionID ) {
        if ( sessionID != null ) {
            xml.getRootElement().setAttribute( "sessionID", sessionID );
        } else if ( user != null ) {
            xml.getRootElement().setAttribute( "user", user );
            if ( password != null ) {
                xml.getRootElement().setAttribute( "password", password );
            }
        }
        return xml;
    }

    /**
     * 
     * @param url
     * @return URL as String with protocol and path but without request params
     * @throws URISyntaxException
     */
    public static String normalizeURL( URL url ) {
        String pr = url.getProtocol();
        String ho = url.getHost();
        int po = url.getPort();
        String pa = url.getPath();
        String s = pr + "://" + ho + ':' + po;
        if ( pa != null && pa.length() > 0 ) {
            s += pa;
        }
        return s;
    }

    /**
     * 
     * @param url
     * @return URL as String with protocol and path but without request params
     * @throws URISyntaxException
     */
    public static String normalizeURL( String url ) {
        try {
            return normalizeURL( new URL( url ) );
        } catch ( MalformedURLException e ) {
            LOG.logWarning( e.getMessage(), e );
        }
        return url;
    }

    private static void setHTTPCredentials( HttpMethod m, String user, String password ) {
        if ( user != null ) {
            if ( password == null ) {
                password = "";
            }
            String s = new String( Base64.encodeBase64( ( user + ":" + password ).getBytes() ) );
            m.setRequestHeader( "authorization", "Basic " + s );
        }
    }

}
