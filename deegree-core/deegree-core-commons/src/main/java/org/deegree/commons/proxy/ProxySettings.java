/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

package org.deegree.commons.proxy;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.proxy.jaxb.ProxyConfiguration;
import org.deegree.workspace.Initializable;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.slf4j.Logger;

/**
 * Utility class for accessing and modifying the VM's proxy configuration and for opening URL connections that respect
 * proxy configurations which require authentication.
 * <p>
 * Please note that Java's proxy configuration is VM-global: there is a set of system properties (proxyHost, proxyPort,
 * etc.) that determines the behaviour of network-related classes (e.g. in <code>java.net</code>). This makes sense, as
 * the proxy configuration is usually defined by the network environment of the physical machine.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public final class ProxySettings implements Initializable {

    private static final Logger LOG = getLogger( ProxySettings.class );

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.commons.proxy.jaxb";

    private static final URL CONFIG_SCHEMA = ProxySettings.class.getResource( "/META-INF/schemas/proxy/3.4.0/proxy.xsd" );

    private static final String PROXY_HOST = "proxyHost";

    private static final String HTTP_PROXY_HOST = "http.proxyHost";

    private static final String FTP_PROXY_HOST = "ftp.proxyHost";

    private static final String PROXY_PORT = "proxyPort";

    private static final String HTTP_PROXY_PORT = "http.proxyPort";

    private static final String FTP_PROXY_PORT = "ftp.proxyPort";

    private static final String PROXY_USER = "proxyUser";

    private static final String HTTP_PROXY_USER = "http.proxyUser";

    private static final String FTP_PROXY_USER = "ftp.proxyUser";

    private static final String PROXY_PASSWORD = "proxyPassword";

    private static final String HTTP_PROXY_PASSWORD = "http.proxyPassword";

    private static final String FTP_PROXY_PASSWORD = "ftp.proxyPassword";

    private static final String NON_PROXY_HOSTS = "nonProxyHosts";

    private static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    private static final String FTP_NON_PROXY_HOSTS = "ftp.nonProxyHosts";

    private static final String PROXY_SET = "proxySet";

    /**
     * Sets/augments the VM's proxy configuration.
     *
     * @param workspace proxy.xml located in the workspaces is used
     *                  in case the workspace root has no proxy.xml, never <code>null</code>
     */
    public void init( Workspace workspace ) {

        File globalProxy = new File( DeegreeWorkspace.getWorkspaceRoot(), "proxy.xml" );

        File proxyConfigFile = new File( ( (DefaultWorkspace) workspace ).getLocation(), "proxy.xml" );
        if ( proxyConfigFile.exists() ) {
            LOG.info( "Using 'proxy.xml' from workspace." );
        } else if ( globalProxy.exists() ) {
            LOG.info( "Using global 'proxy.xml'." );
            proxyConfigFile = globalProxy;
        } else {
            setupAuthenticator();
            LOG.info( "No 'proxy.xml' file -- skipping set up of proxy configuration." );
            return;
        }

        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Proxy configuration." );
        LOG.info( "--------------------------------------------------------------------------------" );
        try {
            ProxyConfiguration proxyConfig = (ProxyConfiguration) unmarshall( CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA,
                                                                              new FileInputStream( proxyConfigFile ),
                                                                              workspace );
            if ( proxyConfig != null ) {
                setupProxyParameters( proxyConfig );
            }
        } catch ( Exception e ) {
            String msg = "Could not unmarshall proxy configuration: " + e.getMessage();
            throw new ResourceInitException( msg, e );
        }
        setupAuthenticator();
        logProxyConfiguration( LOG );
        LOG.info( "" );
    }

    /**
     * Sets/augments the VM's proxy configuration.
     * 
     * @param config
     */
    public synchronized static void setupProxyParameters( ProxyConfiguration config ) {

        String proxyHost = config.getProxyHost();
        String httpProxyHost = config.getHttpProxyHost();
        String ftpProxyHost = config.getFtpProxyHost();
        int proxyPort = config.getProxyPort() != null ? config.getProxyPort().intValue() : -1;
        int httpProxyPort = config.getHttpProxyPort() != null ? config.getHttpProxyPort().intValue() : -1;
        int ftpProxyPort = config.getFtpProxyPort() != null ? config.getFtpProxyPort().intValue() : -1;
        String proxyUser = config.getProxyUser();
        String httpProxyUser = config.getHttpProxyUser();
        String ftpProxyUser = config.getFtpProxyUser();
        String proxyPassword = config.getProxyPassword();
        String httpProxyPassword = config.getHttpProxyPassword();
        String ftpProxyPassword = config.getFtpProxyPassword();
        String nonProxyHosts = config.getNonProxyHosts();
        String httpNonProxyHosts = config.getHttpNonProxyHosts();
        String ftpNonProxyHosts = config.getFtpNonProxyHosts();

        setupProxyParameters( proxyHost, httpProxyHost, ftpProxyHost, proxyPort, httpProxyPort, ftpProxyPort,
                              proxyUser, httpProxyUser, ftpProxyUser, proxyPassword, httpProxyPassword,
                              ftpProxyPassword, nonProxyHosts, httpNonProxyHosts, ftpNonProxyHosts,
                              config.isOverrideSystemSettings() );
    }

    /**
     * Sets/augments the VM's proxy configuration.
     * 
     * @param proxyHost
     * @param httpProxyHost
     * @param ftpProxyHost
     * @param proxyPort
     * @param httpProxyPort
     * @param ftpProxyPort
     * @param proxyUser
     * @param httpProxyUser
     * @param ftpProxyUser
     * @param proxyPassword
     * @param httpProxyPassword
     * @param ftpProxyPassword
     * @param nonProxyHosts
     * @param httpNonProxyHosts
     * @param ftpNonProxyHosts
     * @param override
     */
    public synchronized static void setupProxyParameters( String proxyHost, String httpProxyHost, String ftpProxyHost,
                                                          int proxyPort, int httpProxyPort, int ftpProxyPort,
                                                          String proxyUser, String httpProxyUser, String ftpProxyUser,
                                                          String proxyPassword, String httpProxyPassword,
                                                          String ftpProxyPassword, String nonProxyHosts,
                                                          String httpNonProxyHosts, String ftpNonProxyHosts,
                                                          boolean override ) {

        Properties props = System.getProperties();
        if ( override || props.get( PROXY_HOST ) == null ) {
            setProperty( PROXY_HOST, proxyHost );
        }
        if ( override || props.get( HTTP_PROXY_HOST ) == null ) {
            setProperty( HTTP_PROXY_HOST, httpProxyHost );
        }
        if ( override || props.get( FTP_PROXY_HOST ) == null ) {
            setProperty( FTP_PROXY_HOST, ftpProxyHost );
        }
        if ( override || props.get( PROXY_PORT ) == null ) {
            if ( proxyPort != -1 ) {
                setProperty( PROXY_PORT, "" + proxyPort );
            } else {
                setProperty( PROXY_PORT, null );
            }
        }
        if ( override || props.get( HTTP_PROXY_PORT ) == null ) {
            if ( httpProxyPort != -1 ) {
                setProperty( HTTP_PROXY_PORT, "" + httpProxyPort );
            } else {
                setProperty( HTTP_PROXY_PORT, null );
            }
        }
        if ( override || props.get( FTP_PROXY_PORT ) == null ) {
            if ( ftpProxyPort != -1 ) {
                setProperty( FTP_PROXY_PORT, "" + ftpProxyPort );
            } else {
                setProperty( FTP_PROXY_PORT, null );
            }
        }

        if ( override || props.get( PROXY_USER ) == null ) {
            setProperty( PROXY_USER, proxyUser );
        }
        if ( override || props.get( HTTP_PROXY_USER ) == null ) {
            setProperty( HTTP_PROXY_USER, httpProxyUser );
        }
        if ( override || props.get( FTP_PROXY_USER ) == null ) {
            setProperty( FTP_PROXY_USER, ftpProxyUser );
        }

        if ( override || props.get( PROXY_PASSWORD ) == null ) {
            setProperty( PROXY_PASSWORD, proxyPassword );
        }
        if ( override || props.get( HTTP_PROXY_PASSWORD ) == null ) {
            setProperty( HTTP_PROXY_PASSWORD, httpProxyPassword );
        }
        if ( override || props.get( FTP_PROXY_PASSWORD ) == null ) {
            setProperty( FTP_PROXY_PASSWORD, ftpProxyPassword );
        }

        if ( override || props.get( NON_PROXY_HOSTS ) == null ) {
            setProperty( NON_PROXY_HOSTS, nonProxyHosts );
        }
        if ( override || props.get( HTTP_NON_PROXY_HOSTS ) == null ) {
            setProperty( HTTP_NON_PROXY_HOSTS, httpNonProxyHosts );
        }
        if ( override || props.get( FTP_NON_PROXY_HOSTS ) == null ) {
            setProperty( FTP_NON_PROXY_HOSTS, ftpNonProxyHosts );
        }
        if ( override || props.get( PROXY_SET ) == null ) {
            setProperty( PROXY_SET, "true" );
        }
    }

    private static void setProperty( String key, String value ) {
        if ( value != null ) {
            System.setProperty( key, value );
        } else {
            System.clearProperty( key );
        }
    }

    /**
     * This method should be used everywhere instead of <code>URL.openConnection()</code>, as it copes with proxies that
     * require user authentication. This method will retrieve the configured password and user name.
     * 
     * @param url
     * @return connection
     * @throws IOException
     */
    public static URLConnection openURLConnection( URL url )
                            throws IOException {
        return openURLConnection( url, getProxyUser(), getProxyPassword() );
    }

    /**
     * This method should be used everywhere instead of <code>URL.openConnection()</code>, as it copes with proxies that
     * require user authentication and http basic authentication.
     * 
     * @param url
     * @return connection
     * @throws IOException
     */
    public static URLConnection openURLConnection( URL url, String proxyUser, String proxyPass, String httpUser,
                                                   String httpPass )
                            throws IOException {
        URLConnection conn = url.openConnection();
        if ( proxyUser != null ) {
            // TODO evaluate java.net.Authenticator
            String userAndPass = Base64.encodeBase64String( ( proxyUser + ":" + proxyPass ).getBytes() );
            conn.setRequestProperty( "Proxy-Authorization", "Basic " + userAndPass );
        }
        if ( httpUser != null ) {
            // TODO evaluate java.net.Authenticator
            String userAndPass = Base64.encodeBase64String( ( httpUser + ":" + httpPass ).getBytes() );
            conn.setRequestProperty( "Authorization", "Basic " + userAndPass );
        }
        // TODO should this be a method parameter?
        conn.setConnectTimeout( 5000 );
        return conn;
    }

    /**
     * This method should be used everywhere instead of <code>URL.openConnection()</code>, as it copes with proxies that
     * require user authentication.
     * 
     * @param url
     * @param user
     * @param pass
     * @return connection
     * @throws IOException
     */
    public static URLConnection openURLConnection( URL url, String user, String pass )
                            throws IOException {
        URLConnection conn = url.openConnection();
        if ( user != null ) {
            // TODO evaluate java.net.Authenticator
            String userAndPass = Base64.encodeBase64String( ( user + ":" + pass ).getBytes() );
            conn.setRequestProperty( "Proxy-Authorization", "Basic " + userAndPass );
        }
        // TODO should this be a method parameter?
        conn.setConnectTimeout( 5000 );
        return conn;
    }

    public static String getProxyHost() {
        return System.getProperty( PROXY_HOST );
    }

    public static String getHttpProxyHost( boolean considerBaseConfig ) {
        String result = System.getProperty( HTTP_PROXY_HOST );
        if ( considerBaseConfig && result == null ) {
            result = getProxyHost();
        }
        return result;
    }

    public static String getFtpProxyHost( boolean considerBaseConfig ) {
        String result = System.getProperty( FTP_PROXY_HOST );
        if ( considerBaseConfig && result == null ) {
            result = getProxyHost();
        }
        return result;
    }

    public static String getProxyPort() {
        return System.getProperty( PROXY_PORT );
    }

    public static String getHttpProxyPort( boolean considerBaseConfig ) {
        String result = System.getProperty( HTTP_PROXY_PORT );
        if ( considerBaseConfig && result == null ) {
            result = getProxyPort();
        }
        return result;
    }

    public static String getFtpProxyPort( boolean considerBaseConfig ) {
        String result = System.getProperty( FTP_PROXY_PORT );
        if ( considerBaseConfig && result == null ) {
            result = getProxyPort();
        }
        return result;
    }

    public static String getProxyUser() {
        return System.getProperty( PROXY_USER );
    }

    public static String getHttpProxyUser( boolean considerBaseConfig ) {
        String result = System.getProperty( HTTP_PROXY_USER );
        if ( considerBaseConfig && result == null ) {
            result = getProxyUser();
        }
        return result;
    }

    public static String getFtpProxyUser( boolean considerBaseConfig ) {
        String result = System.getProperty( FTP_PROXY_USER );
        if ( considerBaseConfig && result == null ) {
            result = getProxyUser();
        }
        return result;
    }

    public static String getProxyPassword() {
        return System.getProperty( PROXY_PASSWORD );
    }

    public static String getHttpProxyPassword( boolean considerBaseConfig ) {
        String result = System.getProperty( HTTP_PROXY_PASSWORD );
        if ( considerBaseConfig && result == null ) {
            result = getProxyPassword();
        }
        return result;
    }

    public static String getFtpProxyPassword( boolean considerBaseConfig ) {
        String result = System.getProperty( FTP_PROXY_PASSWORD );
        if ( considerBaseConfig && result == null ) {
            result = getProxyPassword();
        }
        return result;
    }

    public static String getNonProxyHosts() {
        return System.getProperty( NON_PROXY_HOSTS );
    }

    public static String getHttpNonProxyHosts( boolean considerBaseConfig ) {
        String result = System.getProperty( HTTP_NON_PROXY_HOSTS );
        if ( considerBaseConfig && result == null ) {
            result = getNonProxyHosts();
        }
        return result;
    }

    public static String getFtpNonProxyHosts( boolean considerBaseConfig ) {
        String result = System.getProperty( FTP_NON_PROXY_HOSTS );
        if ( considerBaseConfig && result == null ) {
            result = getNonProxyHosts();
        }
        return result;
    }

    public static void logProxyConfiguration( Logger log ) {
        log.info( "- proxyHost=" + getProxyHost() + ", http.proxyHost=" + getHttpProxyHost( false )
                  + ", ftp.proxyHost=" + getFtpProxyHost( false ) );
        log.info( "- proxyPort=" + getProxyPort() + ", http.proxyPort=" + getHttpProxyPort( false )
                  + ", ftp.proxyPort=" + getFtpProxyPort( false ) );
        log.info( "- proxyUser=" + getProxyUser() + ", http.proxyUser=" + getHttpProxyUser( false )
                  + ", ftp.proxyUser=" + getFtpProxyUser( false ) );
        log.info( "- proxyPassword=" + getProxyPassword() + ", http.proxyPassword=" + getHttpProxyPassword( false )
                  + ", ftp.proxyPassword=" + getFtpProxyPassword( false ) );
        log.info( "- nonProxyHosts=" + getNonProxyHosts() + ", http.nonProxyHosts=" + getHttpNonProxyHosts( false )
                  + ", ftp.nonProxyHosts=" + getFtpNonProxyHosts( false ) );
    }

    private void setupAuthenticator() {
        String httpProxyUser = getHttpProxyUser( true );
        String httpProxyPassword = getHttpProxyPassword( true );
        if ( httpProxyUser != null && httpProxyPassword != null )
            Authenticator.setDefault( createAuthenticator( httpProxyUser, httpProxyPassword ) );
    }

    private Authenticator createAuthenticator( String httpProxyUser, String httpProxyPassword ) {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication( httpProxyUser, httpProxyPassword.toCharArray() );
            }
        };
    }

}
