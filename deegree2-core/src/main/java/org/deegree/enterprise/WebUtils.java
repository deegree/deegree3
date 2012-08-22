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
package org.deegree.enterprise;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static org.deegree.framework.log.LoggerFactory.getLogger;

import java.net.URL;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.StringTools;

/**
 * 
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class WebUtils {

    private static final ILogger LOG = getLogger( WebUtils.class );

    /**
     * returns the root path (address - port - context) of a web application read from the passed request<br>
     * e.g. http://myserver:8080/deegree/services?request=GetCapabilites ... <br>
     * will return 'http://myserver:8080/deegree'
     * 
     * @param request
     * @return the root path (address - port - context) of a web application read from the passed request
     */
    public static String getAbsoluteContextPath( HttpServletRequest request ) {

        String s = request.getRequestURL().toString();
        String[] parts = org.deegree.framework.util.StringTools.toArray( s, "/", false );
        return parts[0] + "//" + parts[1] + request.getContextPath();

    }

    private static void handleProxies( String protocol, HttpClient client, String host ) {
        TreeSet<String> nops = new TreeSet<String>();

        String proxyHost = getProperty( ( protocol == null ? "" : protocol + "." ) + "proxyHost" );

        String proxyUser = getProperty( ( protocol == null ? "" : protocol + "." ) + "proxyUser" );
        String proxyPass = getProperty( ( protocol == null ? "" : protocol + "." ) + "proxyPassword" );

        if ( proxyHost != null ) {
            String nop = getProperty( ( protocol == null ? "" : protocol + "." ) + "noProxyHosts" );
            if ( nop != null && !nop.equals( "" ) ) {
                nops.addAll( asList( nop.split( "\\|" ) ) );
            }
            nop = getProperty( ( protocol == null ? "" : protocol + "." ) + "nonProxyHosts" );
            if ( nop != null && !nop.equals( "" ) ) {
                nops.addAll( asList( nop.split( "\\|" ) ) );
            }

            int proxyPort = parseInt( getProperty( ( protocol == null ? "" : protocol + "." ) + "proxyPort" ) );

            HostConfiguration hc = client.getHostConfiguration();

            if ( LOG.isDebug() ) {
                LOG.logDebug( "Found the following no- and nonProxyHosts", nops );
            }

            if ( proxyUser != null ) {
                Credentials creds = new UsernamePasswordCredentials( proxyUser, proxyPass );
                client.getState().setProxyCredentials( AuthScope.ANY, creds );
                client.getParams().setAuthenticationPreemptive( true );
            }

            if ( !nops.contains( host ) ) {
                if ( LOG.isDebug() ) {
                    LOG.logDebug( "Using proxy " + proxyHost + ":" + proxyPort );
                    if ( protocol == null ) {
                        LOG.logDebug( "This overrides the protocol specific settings, if there were any." );
                    }
                }
                hc.setProxy( proxyHost, proxyPort );
                client.setHostConfiguration( hc );
            } else {
                if ( LOG.isDebug() ) {
                    LOG.logDebug( "Proxy was set, but " + host + " was contained in the no-/nonProxyList!" );
                    if ( protocol == null ) {
                        LOG.logDebug( "If a protocol specific proxy has been set, it will be used anyway!" );
                    }
                }
            }
        }

        if ( protocol != null ) {
            handleProxies( null, client, host );
        }
    }

    /**
     * reads proxyHost and proxyPort from system parameters and sets them to the passed HttpClient instance
     * 
     * @see HostConfiguration of the passed
     * @see HttpClient
     * @param client
     * @param url
     * @return HttpClient with proxy configuration
     */
    public static HttpClient enableProxyUsage( HttpClient client, URL url ) {
        String host = url.getHost();
        String protocol = url.getProtocol().toLowerCase();
        handleProxies( protocol, client, host );
        return client;
    }

    /**
     * returns the charset read from the content type of the passed {@link HttpServletRequest}. If no content type or no
     * charset is defined the charset read from {@link CharsetUtils} will be returned instead. So it is gauranteed that
     * the method will not return <code>null</code>
     * 
     * @param request
     * @return charset
     */
    public static String readCharsetFromContentType( HttpServletRequest request ) {

        String contentType = request.getHeader( "Content-Type" );
        return readCharsetFromContentType( contentType );
    }

    /**
     * @see #readCharsetFromContentType(HttpServletRequest)
     * @param contentType
     * @return charset
     */
    public static String readCharsetFromContentType( String contentType ) {
        String charset = null;
        if ( contentType != null ) {
            String[] tmp = StringTools.toArray( contentType, ";", false );
            if ( tmp.length == 2 ) {
                charset = tmp[1].substring( tmp[1].indexOf( '=' ) + 1, tmp[1].length() );
            } else {
                charset = CharsetUtils.getSystemCharset();
            }
        } else {
            charset = CharsetUtils.getSystemCharset();
        }
        return charset;
    }

}
