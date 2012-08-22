//$HeadURL$
// $Id$
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

import static java.lang.Character.isDigit;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static org.deegree.enterprise.servlet.ServiceLookup.getInstance;
import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.deegree.framework.util.StringTools.arrayToString;

import java.beans.Introspector;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.spi.IIORegistry;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.deegree.crs.configuration.CRSConfiguration;
import org.deegree.enterprise.AbstractOGCServlet;
import org.deegree.enterprise.ServiceException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.KVP2Map;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.util.WebappResourceResolver;
import org.deegree.framework.version.Version;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.ogcwebservices.ExceptionReport;
import org.deegree.ogcwebservices.OGCRequestFactory;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wmps.configuration.WMPSConfigurationDocument;
import org.deegree.ogcwebservices.wms.configuration.WMSConfigurationDocument;
import org.deegree.ogcwebservices.wms.configuration.WMSConfigurationDocument_1_3_0;
import org.deegree.owscommon.XMLFactory;
import org.xml.sax.SAXException;

/**
 * An <code>OGCServletController</code> handles all incoming requests. The controller for all OGC service requests.
 * Dispatcher to specific handler for WMS, WFS and other.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date: 22.04.2008 16:23:34$
 * @see <a href="http://java.sun.com/blueprints/corej2eepatterns/Patterns/FrontController.html">Front controller </a>
 */
public class OGCServletController extends AbstractOGCServlet {

    /**
     * address is the url of the client which requests.
     */
    public static String address = null;

    private static final long serialVersionUID = -4461759017823581221L;

    private static ILogger LOG;

    private static final String SERVICE = "services";

    private static final String HANDLER_CLASS = ".handler";

    private static final String HANDLER_CONF = ".config";

    private static final Map<Class<?>, String> SERVICE_FACTORIES_MAPPINGS = new HashMap<Class<?>, String>();

    private static final String ERR_MSG = "Can't set configuration for {0}";

    /**
     *
     *
     * @param request
     * @param response
     * @TODO refactor and optimize code for initializing handler
     */
    public void doService( HttpServletRequest request, HttpServletResponse response ) {
        if ( response.isCommitted() ) {
            LOG.logWarning( "The response object is already committed!" );
        }

        long startTime = System.currentTimeMillis();
        address = request.getRequestURL().toString();

        String service = null;
        try {
            OGCWebServiceRequest ogcRequest = OGCRequestFactory.create( request );

            LOG.logInfo( StringTools.concat( 500, "Handling request '", ogcRequest.getId(), "' from '",
                                             request.getRemoteAddr(), "' to service: '", ogcRequest.getServiceName(),
                                             "'" ) );

            // get service from request
            service = ogcRequest.getServiceName().toUpperCase();

            // get handler instance
            ServiceDispatcher handler = ServiceLookup.getInstance().getHandler( service, request.getRemoteAddr() );
            // dispatch request to specific handler
            handler.perform( ogcRequest, response );
        } catch ( OGCWebServiceException e ) {
            LOG.logError( e.getMessage(), e );
            sendException( response, e, request, service );
        } catch ( ServiceException e ) {
            if ( e.getNestedException() instanceof OGCWebServiceException ) {
                sendException( response, (OGCWebServiceException) e.getNestedException(), request, service );
            } else {
                sendException( response, new OGCWebServiceException( this.getClass().getName(), e.getMessage() ),
                               request, service );
            }
            LOG.logError( e.getMessage(), e );
        } catch ( Exception e ) {
            sendException( response, new OGCWebServiceException( this.getClass().getName(), e.getMessage() ), request,
                           service );
            LOG.logError( e.getMessage(), e );
        }
        if ( LOG.isDebug() ) {
            LOG.logDebug( "OGCServletController: request performed in "
                          + Long.toString( System.currentTimeMillis() - startTime ) + " milliseconds." );
        }
    }

    /**
     * Sends the passed <code>OGCWebServiceException</code> to the calling client.
     *
     * @param response
     * @param e
     * @param request
     * @param service
     *            the service name, if known
     */
    private static void sendException( HttpServletResponse response, OGCWebServiceException e,
                                       HttpServletRequest request, String service ) {
        LOG.logInfo( "Sending OGCWebServiceException to client." );

        Map<?, ?> pmap = request.getParameterMap();
        Map<String, String> map = new HashMap<String, String>( pmap.size() );
        for ( Object o : pmap.keySet() ) {
            String[] tmp = (String[]) pmap.get( o );
            for ( int i = 0; i < tmp.length; i++ ) {
                tmp[i] = tmp[i].trim();
            }
            map.put( ( (String) o ).toLowerCase(), arrayToString( tmp, ',' ) );
        }

        boolean isWMS130 = false, isCSW = false, isWCTS = false, isWFS = false, isWFS100 = false;

        if ( service == null ) {
            service = map.get( "service" );
        }

        String version = map.get( "version" );

        if ( service != null ) {
            if ( "wms".equalsIgnoreCase( service ) ) {
                isWMS130 = version != null && version.equals( "1.3.0" );
            }
            if ( "wfs".equalsIgnoreCase( service ) ) {
                isWFS = true;
                isWFS100 = version != null && version.equals( "1.0.0" );
            }

            isCSW = "csw".equalsIgnoreCase( service );
            isWCTS = "wcts".equalsIgnoreCase( service );
            isWFS = "wfs".equalsIgnoreCase( service );
        } else {
            try {
                XMLFragment doc = new XMLFragment( request.getReader(), XMLFragment.DEFAULT_URL );
                service = OGCRequestFactory.getTargetService( "", "", doc.getRootElement().getOwnerDocument() );
                isCSW = "csw".equalsIgnoreCase( service );
                isWCTS = "wcts".equalsIgnoreCase( service );
                isWFS = "wfs".equalsIgnoreCase( service );
                isWFS100 = isWFS && doc.getRootElement().getAttribute( "version" ) != null
                           && doc.getRootElement().getAttribute( "version" ).equals( "1.0.0" );
            } catch ( SAXException e1 ) {
                // ignore
            } catch ( IOException e1 ) {
                // ignore
            } catch ( IllegalStateException e1 ) {
                // ignore, that happens in some tomcats
            }
        }

        try {
            XMLFragment doc;
            String contentType = "text/xml";

            if ( !( isWMS130 || isCSW || isWCTS || isWFS ) ) {
                // apply the simplest of heuristics...
                String req = request.getRequestURI().toLowerCase();
                if ( req.indexOf( "csw" ) != -1 ) {
                    isCSW = true;
                } else if ( req.indexOf( "wcts" ) != -1 ) {
                    isWCTS = true;
                } else if ( req.indexOf( "wfs" ) != -1 ) {
                    isWFS = true;
                }

                if ( isWFS ) {
                    isWFS100 = req.indexOf( "1.0.0" ) != -1;
                }

                if ( !( isWMS130 || isCSW || isWCTS || isWFS || isWFS100 ) ) {
                    isWMS130 = version != null && version.equals( "1.3.0" );
                }
            }

            // send exception format INIMAGE etc. for WMS
            if ( service != null && service.equalsIgnoreCase( "wms" ) ) {
                ServiceDispatcher handler = getInstance().getHandler( service, request.getRemoteAddr() );
                if ( handler instanceof WMSHandler ) {
                    WMSHandler h = (WMSHandler) handler;
                    String format = map.get( "format" );
                    String eFormat = map.get( "exceptions" );
                    try {
                        h.determineExceptionFormat( eFormat, format, version, response );
                        h.writeServiceExceptionReport( e );
                        return;
                    } catch ( Exception ex ) {
                        LOG.logDebug( "Error while sending the exception in special format."
                                      + " Continuing in default mode.", ex );
                    }
                }
            }

            if ( isWMS130 || "wcs".equalsIgnoreCase( e.getLocator() ) ) {
                doc = XMLFactory.exportNS( new ExceptionReport( new OGCWebServiceException[] { e } ) );
            } else if ( isCSW ) {
                doc = XMLFactory.exportExceptionReport( new ExceptionReport( new OGCWebServiceException[] { e } ) );
            } else if ( isWCTS ) {
                doc = org.deegree.owscommon_1_1_0.XMLFactory.exportException( e );
            } else if ( isWFS100 ) {
                doc = XMLFactory.exportExceptionReportWFS100( e );
            } else if ( isWFS ) {
                doc = XMLFactory.exportExceptionReportWFS( e );
            } else {
                contentType = "application/vnd.ogc.se_xml";
                doc = XMLFactory.export( new ExceptionReport( new OGCWebServiceException[] { e } ) );
            }

            response.setContentType( contentType );
            OutputStream os = response.getOutputStream();
            doc.write( os );
            os.close();
        } catch ( Exception ex ) {
            LOG.logError( "ERROR: " + ex.getMessage(), ex );
        }
    }

    /**
     *
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {

        LOG.logDebug( "query string ", request.getQueryString() );
        if ( request.getParameter( "RELOADDEEGREE" ) != null ) {
            reloadServices( request, response );
        } else {
            this.doService( request, response );
        }
    }

    /**
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private void reloadServices( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {
        Map<?, ?> map = KVP2Map.toMap( request );
        String user = (String) map.get( "USER" );
        String password = (String) map.get( "PASSWORD" );
        String message = null;
        if ( getInitParameter( "USER" ) != null && getInitParameter( "PASSWORD" ) != null
             && getInitParameter( "USER" ).equals( user ) && getInitParameter( "PASSWORD" ).equals( password ) ) {
            initServices( getServletContext() );
            ctDestroyed();
            message = Messages.getString( "OGCServletController.reloadsuccess" );
        } else {
            message = Messages.getString( "OGCServletController.reloadfailed" );
        }
        PrintWriter pw = response.getWriter();
        pw.print( message );
        pw.flush();
        pw.close();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
                            throws ServletException, IOException {
        this.doService( request, response );
    }

    private static String spaces( int i ) {
        if ( i <= 0 ) {
            return "";
        }

        StringBuffer sb = new StringBuffer( i );
        for ( int j = 0; j < i; ++j ) {
            sb.append( " " );
        }
        return sb.toString();
    }

    private static void logIfThere( String param ) {
        String val = getProperty( param );
        if ( val != null ) {
            LOG.logInfo( "- " + param + spaces( 15 - param.length() ) + ": " + val );
        }
    }

    /**
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init()
                            throws ServletException {

        synchronized ( OGCServletController.class ) {
            if ( LOG == null ) {
                // hack to figure out and set the context path name
                // for a laugh, see http://marc.info/?l=tomcat-user&m=109215904113904&w=2 and the related thread
                // http://marc.info/?t=109215871400004&r=1&w=2
                String path = getServletContext().getRealPath( "" );
                String[] ps = path.split( "[/\\\\]" );
                path = ps[ps.length - 1];
                // heuristics are always a charm (and work best for tomcat in this case)
                if ( isDigit( path.charAt( 0 ) ) && path.indexOf( "-" ) != -1 ) {
                    path = path.split( "-", 2 )[1];
                }
                // note that setting this changes it on a JVM GLOBAL BASIS, so it WILL GET OVERWRITTEN in subsequent
                // deegree startups! (However, since the log4j.properties will only be read on startup, this hack is
                // useful anyway)
                setProperty( "context.name", path );

                LOG = getLogger( OGCServletController.class );
            }
        }

        super.init();
        LOG.logDebug( "Logger for " + this.getClass().getName() + " initialized." );

        SERVICE_FACTORIES_MAPPINGS.put( CSWHandler.class, "org.deegree.ogcwebservices.csw.CSWFactory" );
        SERVICE_FACTORIES_MAPPINGS.put( WFSHandler.class, "org.deegree.ogcwebservices.wfs.WFServiceFactory" );
        SERVICE_FACTORIES_MAPPINGS.put( WCSHandler.class, "org.deegree.ogcwebservices.wcs.WCServiceFactory" );
        SERVICE_FACTORIES_MAPPINGS.put( WMSHandler.class, "org.deegree.ogcwebservices.wms.WMServiceFactory" );
        SERVICE_FACTORIES_MAPPINGS.put( WPVSHandler.class, "org.deegree.ogcwebservices.wpvs.WPVServiceFactory" );
        SERVICE_FACTORIES_MAPPINGS.put( WMPSHandler.class, "org.deegree.ogcwebservices.wmps.WMPServiceFactory" );
        SERVICE_FACTORIES_MAPPINGS.put( WPSHandler.class, "org.deegree.ogcwebservices.wps.WPServiceFactory" );
        SERVICE_FACTORIES_MAPPINGS.put( WASSHandler.class, "org.deegree.ogcwebservices.wass.common.WASServiceFactory" );
        SERVICE_FACTORIES_MAPPINGS.put( WCTSHandler.class, "org.deegree.ogcwebservices.wcts.WCTServiceFactory" );

        LOG.logInfo( "-------------------------------------------------------------------------------" );
        LOG.logInfo( "Starting deegree version " + Version.getVersion() );
        LOG.logInfo( "- context        : " + this.getServletContext().getServletContextName() );
        LOG.logInfo( "- real path      : " + this.getServletContext().getRealPath( "/" ) );
        LOG.logInfo( "- java version   : " + System.getProperty( "java.version" ) + "" );
        LOG.logInfo( "- dom builder    : " + DocumentBuilderFactory.newInstance().getClass().getName() + "" );
        LOG.logInfo( "- xslt builder   : " + TransformerFactory.newInstance().getClass().getName() + "" );
        LOG.logInfo( "- system charset : " + CharsetUtils.getSystemCharset() );
        LOG.logInfo( "- default charset: " + Charset.defaultCharset() );
        LOG.logInfo( "- server info    : " + this.getServletContext().getServerInfo() );
        logIfThere( "proxyHost" );
        logIfThere( "proxyPort" );
        logIfThere( "noProxyHosts" );
        logIfThere( "nonProxyHosts" );
        logIfThere( "http.proxyHost" );
        logIfThere( "http.proxyPort" );
        logIfThere( "http.noProxyHosts" );
        logIfThere( "http.nonProxyHosts" );
        logIfThere( "ftp.proxyHost" );
        logIfThere( "ftp.proxyPort" );
        logIfThere( "ftp.noProxyHosts" );
        logIfThere( "ftp.nonProxyHosts" );
        logIfThere( "https.proxyHost" );
        logIfThere( "https.proxyPort" );
        logIfThere( "https.noProxyHosts" );
        logIfThere( "https.nonProxyHosts" );
        try {
            LOG.logInfo( "- ip             : " + InetAddress.getLocalHost().getHostAddress() );
            LOG.logInfo( "- host name      : " + InetAddress.getLocalHost().getHostName() );
            LOG.logInfo( "- domain name    : " + InetAddress.getLocalHost().getCanonicalHostName() );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }
        LOG.logInfo( "-------------------------------------------------------------------------------" );
        this.initServices( getServletContext() );
        checkServerCompatibility();
        LOG.logInfo( "-------------------------------------------------------------------------------" );
        String tmpServiceList = this.getServiceList();
        if ( tmpServiceList != null && !( "".equals( tmpServiceList.trim() ) ) ) {
            LOG.logInfo( "Initialized successfully (context '" + this.getServletContext().getServletContextName()
                         + "'):" );
            String[] tmpServices = tmpServiceList.split( "," );
            for ( String service : tmpServices ) {
                // Added a check for the alternative service name, because it should not be outputed twice for the csw.
                if ( !OGCRequestFactory.CSW_SERVICE_NAME_EBRIM.toUpperCase().equals( service ) ) {
                    LOG.logInfo( "- " + service );
                }
            }
        } else {
            LOG.logError( "An Error occured while initializing context '"
                          + this.getServletContext().getServletContextName() + "', no services are available." );
        }

        LOG.logInfo( "-------------------------------------------------------------------------------" );
        // Sets the attributes for tomcat -> application.getAttribute(); in jsp sites
        this.getServletContext().setAttribute( "deegree_ogc_services", this.getServiceList() );
    }

    private void checkServerCompatibility() {
        String serverInfo = getServletContext().getServerInfo();
        if ( "Apache Tomcat/5.5.26".equals( serverInfo ) || "Apache Tomcat/6.0.16".equals( serverInfo ) ) {
            LOG.logWarning( "*******************************************************************************" );
            LOG.logWarning( "YOU ARE RUNNING DEEGREE ON A TOMCAT RELEASE (" + serverInfo
                            + ") THAT IS KNOWN TO HAVE A SERIOUS ISSUE WITH LARGE POST REQUESTS." );
            LOG.logWarning( "PLEASE CONSIDER THE CORRESPONDING DEEGREE WIKI PAGE AT  https://wiki.deegree.org/deegreeWiki/ApacheTomcat "
                            + "FOR DETAILS AND SWITCH TO A DIFFERENT TOMCAT VERSION." );
            LOG.logWarning( "*******************************************************************************" );
        }
    }

    private void initServices( ServletContext context )
                            throws ServletException {

        // get list of OGC services
        String serviceList = this.getRequiredInitParameter( SERVICE );

        String[] serviceNames = StringTools.toArray( serviceList, ",", false );

        ServiceLookup lookup = ServiceLookup.getInstance();
        for ( int i = 0; i < serviceNames.length; i++ ) {
            LOG.logInfo( StringTools.concat( 100, "---- Initializing ", serviceNames[i].toUpperCase(), " ----" ) );
            try {
                String className = this.getRequiredInitParameter( serviceNames[i] + HANDLER_CLASS );
                Class<?> handlerClzz = Class.forName( className );

                // initialize each service factory
                String s = this.getRequiredInitParameter( serviceNames[i] + HANDLER_CONF );
                URL serviceConfigurationURL = WebappResourceResolver.resolveFileLocation( s, context, LOG );

                // set configuration
                LOG.logInfo( StringTools.concat( 300, "Reading configuration for ", serviceNames[i].toUpperCase(),
                                                 " from URL: '", serviceConfigurationURL, "'." ) );

                String factoryClassName = SERVICE_FACTORIES_MAPPINGS.get( handlerClzz );

                Class<?> factory = Class.forName( factoryClassName );
                Method method = factory.getMethod( "setConfiguration", new Class[] { URL.class } );
                method.invoke( factory, new Object[] { serviceConfigurationURL } );

                // The csw-ebrim profile adds an alternative service name, it too is registred with the CSW handler.
                if ( "CSW".equals( serviceNames[i].toUpperCase() ) ) {
                    lookup.addService( OGCRequestFactory.CSW_SERVICE_NAME_EBRIM.toUpperCase(), handlerClzz );
                }
                // put handler to available service list
                lookup.addService( serviceNames[i].toUpperCase(), handlerClzz );

                LOG.logInfo( StringTools.concat( 300, serviceNames[i].toUpperCase(), " successfully initialized." ) );
            } catch ( ServletException e ) {
                LOG.logError( e.getMessage(), e );
            } catch ( InvocationTargetException e ) {
                e.getTargetException().printStackTrace();
                LOG.logError( this.produceMessage( ERR_MSG, new Object[] { serviceNames[i] } ), e );
            } catch ( Exception e ) {
                LOG.logError( "Can't initialize OGC service:" + serviceNames[i], e );
            }
        }
    }

    private String getRequiredInitParameter( String name )
                            throws ServletException {
        String paramValue = getInitParameter( name );
        if ( paramValue == null ) {

            String msg = "Required init parameter '" + name + "' missing in web.xml";
            LOG.logError( msg );
            throw new ServletException( msg );
        }
        return paramValue;
    }

    /**
     * @return the services, separated by ","
     */
    private String getServiceList() {

        StringBuffer buf = new StringBuffer();
        ServiceLookup lookup = ServiceLookup.getInstance();
        for ( Iterator<?> iter = lookup.getIterator(); iter.hasNext(); ) {
            String serviceName = (String) iter.next();
            buf.append( serviceName );
            if ( iter.hasNext() ) {
                buf.append( ',' );
            }
        }
        return buf.toString();
    }

    /**
     * Formats the provided string and the args array into a String using MessageFormat.
     *
     * @param pattern
     * @param args
     * @return the message to present the client.
     */
    private String produceMessage( String pattern, Object[] args ) {
        return new MessageFormat( pattern ).format( args );
    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void ctDestroyed() {
        LOG.logInfo( "Stopping context: " );

        WMSConfigurationDocument.resetCapabilitiesCache();
        WMSConfigurationDocument_1_3_0.resetCapabilitiesCache();
        WMPSConfigurationDocument.resetCapabilitiesCache();

        ServiceLookup lookup = ServiceLookup.getInstance();
        for ( Iterator<?> iter = lookup.getIterator(); iter.hasNext(); ) {
            String serviceName = (String) iter.next();
            LOG.logInfo( "Stopping service " + serviceName );

            try {
                String s = SERVICE_FACTORIES_MAPPINGS.get( lookup.getService( serviceName ) );
                Class<?> clzz = Class.forName( s );
                // TODO stop and reset all service instances
                Method[] methods = clzz.getMethods();
                for ( int j = 0; j < methods.length; j++ ) {
                    if ( methods[j].getName().equals( "reset" ) ) {
                        Object[] args = new Object[0];
                        methods[j].invoke( clzz.newInstance(), args );
                    }
                }
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        Enumeration<Driver> e = DriverManager.getDrivers();
        while ( e.hasMoreElements() ) {
            Driver driver = e.nextElement();
            try {
                if ( driver.getClass().getClassLoader() == getClass().getClassLoader() )
                    DriverManager.deregisterDriver( driver );
            } catch ( SQLException e1 ) {
                LOG.logError( "Cannot unload driver: " + driver );
            }
        }
        LogFactory.releaseAll();
        LogManager.shutdown();
        // SLF4JLogFactory.releaseAll(); // should be the same as the LogFactory.releaseAll call
        Iterator<Class<?>> i = IIORegistry.getDefaultInstance().getCategories();
        while ( i.hasNext() ) {
            Class<?> c = i.next();
            Iterator<?> k = IIORegistry.getDefaultInstance().getServiceProviders( c, false );
            while ( k.hasNext() ) {
                Object o = k.next();
                if ( o.getClass().getClassLoader() == getClass().getClassLoader() ) {
                    IIORegistry.getDefaultInstance().deregisterServiceProvider( o );
                    LOG.logDebug( "Deregistering JAI driver ", o.getClass() );
                }
            }
        }
        Introspector.flushCaches();
        // just clear the configurations for now, it does not hurt
        CRSConfiguration.DEFINED_CONFIGURATIONS.clear();
    }

}
