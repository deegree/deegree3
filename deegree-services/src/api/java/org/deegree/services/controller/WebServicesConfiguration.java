//$HeadURL$
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

import static java.lang.Class.forName;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.services.controller.utils.StandardRequestLogger;
import org.deegree.services.csw.CSWController;
import org.deegree.services.jaxb.main.AllowedServices;
import org.deegree.services.jaxb.main.ConfiguredServicesType;
import org.deegree.services.jaxb.main.DeegreeServiceControllerType;
import org.deegree.services.jaxb.main.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.main.FrontControllerOptionsType;
import org.deegree.services.jaxb.main.ServiceType;
import org.deegree.services.jaxb.main.FrontControllerOptionsType.RequestLogging;
import org.deegree.services.sos.SOSController;
import org.deegree.services.wcs.WCSController;
import org.deegree.services.wfs.WFSController;
import org.deegree.services.wms.controller.WMSController;
import org.deegree.services.wps.WPService;
import org.deegree.services.wpvs.controller.WPVSController;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WebServicesConfiguration {

    private static final Logger LOG = getLogger( WebServicesConfiguration.class );

    private static final Version SUPPORTED_CONFIG_VERSION = Version.parseVersion( "0.6.0" );

    // maps service names (e.g. 'WMS', 'WFS', ...) to responsible subcontrollers
    private final Map<AllowedServices, AbstractOGCServiceController> serviceNameToController = new HashMap<AllowedServices, AbstractOGCServiceController>();

    // maps service namespaces (e.g. 'http://www.opengis.net/wms', 'http://www.opengis.net/wfs', ...) to the
    // responsible subcontrollers
    private final Map<String, AbstractOGCServiceController> serviceNSToController = new HashMap<String, AbstractOGCServiceController>();

    // maps request names (e.g. 'GetMap', 'DescribeFeatureType') to the responsible subcontrollers
    private final Map<String, AbstractOGCServiceController> requestNameToController = new HashMap<String, AbstractOGCServiceController>();

    private DeegreeWorkspace workspace;

    private DeegreeServicesMetadataType metadataConfig;

    private DeegreeServiceControllerType mainConfig;

    private RequestLogger requestLogger;

    private boolean logOnlySuccessful;

    /**
     * @param workspace
     */
    public WebServicesConfiguration( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    /**
     * @throws ServletException
     */
    public void init()
                            throws ServletException {
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Starting webservices." );
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "" );

        File metadata = new File( workspace.getLocation(), "services" + File.separator + "metadata.xml" );
        File main = new File( workspace.getLocation(), "services" + File.separator + "main.xml" );

        if ( !metadata.exists() ) {
            String msg = "No 'services/metadata.xml' file, aborting startup!";
            LOG.error( msg );
            throw new ServletException( msg );
        }
        if ( !metadata.exists() ) {
            LOG.debug( "No 'services/main.xml' file, assuming defaults." );
        }

        try {
            String contextName = "org.deegree.services.jaxb.main";
            JAXBContext jc = JAXBContext.newInstance( contextName );
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            metadataConfig = (DeegreeServicesMetadataType) ( (JAXBElement<?>) unmarshaller.unmarshal( metadata ) ).getValue();
            try {
                mainConfig = (DeegreeServiceControllerType) ( (JAXBElement<?>) unmarshaller.unmarshal( main ) ).getValue();
            } catch ( JAXBException e ) {
                mainConfig = new DeegreeServiceControllerType();
                LOG.info( "main.xml could not be loaded. Proceeding with defaults." );
                LOG.debug( "Error was: '{}'.", e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            }
        } catch ( JAXBException e ) {
            String msg = "Could not unmarshall frontcontroller configuration: " + e.getMessage();
            LOG.error( msg );
            throw new ServletException( msg, e );
        }

        Version configVersion = Version.parseVersion( metadataConfig.getConfigVersion() );
        if ( !configVersion.equals( SUPPORTED_CONFIG_VERSION ) ) {
            String msg = "The service metadata file '" + metadata + " uses configuration format version "
                         + metadataConfig.getConfigVersion() + ", but this deegree version only supports version "
                         + SUPPORTED_CONFIG_VERSION + ". Information on resolving this issue can be found at "
                         + "'http://wiki.deegree.org/deegreeWiki/deegree3/ConfigurationVersions'. ";
            LOG.debug( "********************************************************************************" );
            LOG.error( msg );
            LOG.debug( "********************************************************************************" );
            throw new ServletException( msg );
        }

        initRequestLogger();

        ConfiguredServicesType servicesConfigured = mainConfig.getConfiguredServices();
        List<ServiceType> services = null;
        if ( servicesConfigured != null ) {
            services = servicesConfigured.getService();
            if ( services != null && services.size() > 0 ) {
                LOG.info( "The file: " + main );
                LOG.info( "Provided following services:" );
                for ( ServiceType s : services ) {
                    URL configLocation = null;
                    try {
                        configLocation = new URL( main.toURI().toURL(), s.getConfigurationLocation() );
                    } catch ( MalformedURLException e ) {
                        LOG.error( e.getMessage(), e );
                        return;
                    }
                    s.setConfigurationLocation( configLocation.toExternalForm() );

                    LOG.info( " - " + s.getServiceName() );
                }
                LOG.info( "ATTENTION - Skipping the loading of all services in conf/ which are not listed above." );
            }
        }
        if ( services == null || services.size() == 0 ) {
            LOG.info( "No service elements were supplied in the file: '" + main
                      + "' -- trying to use the default loading mechanism." );
            try {
                services = loadServicesFromDefaultLocation();
            } catch ( MalformedURLException e ) {
                throw new ServletException( "Error loading service configurations: " + e.getMessage() );
            }
        }
        if ( services.size() == 0 ) {
            LOG.info( "No deegree web services have been loaded." );
        }

        for ( ServiceType configuredService : services ) {
            AbstractOGCServiceController serviceController = instantiateServiceController( configuredService );
            if ( serviceController != null ) {
                registerSubController( configuredService, serviceController );
            }
        }
        LOG.info( "" );
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Webservices started." );
        LOG.info( "--------------------------------------------------------------------------------" );
    }

    /**
     * Iterates over all directories in the conf/ directory and returns the service/configuration mappings as a list.
     * This default service loading mechanism implies the following directory structure:
     * <ul>
     * <li>conf/</li>
     * <li>conf/[SERVICE_NAME]/ (upper-case abbreviation of a deegree web service, please take a look at
     * {@link AllowedServices})</li>
     * <li>conf/[SERVICE_NAME]/[SERVICE_NAME]_configuration.xml</li>
     * </ul>
     * If all conditions are met the service type is added to resulting list. If none of the underlying directories meet
     * above criteria, an empty list will be returned.
     * 
     * @return the list of services found in the conf directory. Or an empty list if the above conditions are not met
     *         for any directory in the conf directory.
     * @throws MalformedURLException
     */
    private List<ServiceType> loadServicesFromDefaultLocation()
                            throws MalformedURLException {
        File serviceConfigDir = new File( workspace.getLocation(), "services" );

        List<ServiceType> loadedServices = new ArrayList<ServiceType>();
        if ( !serviceConfigDir.isDirectory() ) {
            LOG.error( "Could not read from the default service configuration directory (" + serviceConfigDir
                       + ") because it is not a directory." );
            return loadedServices;

        }
        LOG.info( "Using default directory: " + serviceConfigDir.getAbsolutePath()
                  + " to scan for webservice configurations." );
        File[] files = serviceConfigDir.listFiles();
        if ( files == null || files.length == 0 ) {
            LOG.error( "No files found in default configuration directory, hence no services to load." );
            return loadedServices;
        }
        for ( File f : files ) {
            if ( !f.isDirectory() ) {
                String fileName = f.getName();
                if ( fileName != null && !"".equals( fileName.trim() ) ) {
                    String serviceName = fileName.trim().toUpperCase();
                    // to avoid the ugly warning we can afford this extra s(hack)
                    if ( serviceName.equals( ".SVN" ) || !serviceName.endsWith( ".XML" )
                         || serviceName.equals( "METADATA.XML" ) || serviceName.equals( "MAIN.XML" ) ) {
                        continue;
                    }
                    serviceName = serviceName.substring( 0, fileName.length() - 4 );

                    AllowedServices as;
                    try {
                        as = AllowedServices.fromValue( serviceName );
                    } catch ( IllegalArgumentException ex ) {
                        LOG.warn( "File '" + fileName + "' in the configuration directory "
                                  + "is not a valid deegree webservice, so skipping it." );
                        continue;
                    }
                    LOG.debug( "Trying to create a frontcontroller for service: " + fileName
                               + " found in the configuration directory." );
                    ServiceType configuredService = new ServiceType();
                    configuredService.setConfigurationLocation( f.toURI().toURL().toString() );
                    configuredService.setServiceName( as );
                    loadedServices.add( configuredService );
                }
            }

        }

        return loadedServices;
    }

    /**
     * Creates an instance of a sub controller which is valid for the given configured Service, by applying following
     * conventions:
     * <ul>
     * <li>The sub controller must extend {@link AbstractOGCServiceController}</li>
     * <li>The package of the controller is the package of this class.[SERVICE_ABBREV_lower_case]</li>
     * <li>The name of the controller must be [SERVICE_NAME_ABBREV]Controller</li>
     * <li>The controller must have a constructor with a String parameter</li>
     * </ul>
     * If all above conditions are met, the instantiated controller will be returned else <code>null</code>
     * 
     * @param configuredService
     * @return the instantiated secured sub controller or <code>null</code> if an error occurred.
     */
    @SuppressWarnings("unchecked")
    private AbstractOGCServiceController instantiateServiceController( ServiceType configuredService ) {
        AbstractOGCServiceController subController = null;
        if ( configuredService == null ) {
            return subController;
        }

        final String serviceName = configuredService.getServiceName().name();

        // TODO outfactor this (maybe use a Map or proper SPI for plugging service implementations?)
        String controller = null;
        if ( "CSW".equals( serviceName ) ) {
            controller = CSWController.class.getName();
        } else if ( "SOS".equals( serviceName ) ) {
            controller = SOSController.class.getName();
        } else if ( "WCS".equals( serviceName ) ) {
            controller = WCSController.class.getName();
        } else if ( "WFS".equals( serviceName ) ) {
            controller = WFSController.class.getName();
        } else if ( "WMS".equals( serviceName ) ) {
            controller = WMSController.class.getName();
        } else if ( "WPS".equals( serviceName ) ) {
            controller = WPService.class.getName();
        } else if ( "WPVS".equals( serviceName ) ) {
            controller = WPVSController.class.getName();
        }

        LOG.info( "" );
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Starting " + serviceName + "." );
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Configuration file: '" + configuredService.getConfigurationLocation() + "'" );
        if ( configuredService.getControllerClass() != null ) {
            LOG.info( "Using custom controller class '{}'.", configuredService.getControllerClass() );
            controller = configuredService.getControllerClass();
        }
        try {
            long time = System.currentTimeMillis();
            Class<AbstractOGCServiceController> subControllerClass = (Class<AbstractOGCServiceController>) Class.forName(
                                                                                                                          controller,
                                                                                                                          false,
                                                                                                                          OGCFrontController.class.getClassLoader() );
            subController = subControllerClass.newInstance();
            XMLAdapter controllerConf = new XMLAdapter( new URL( configuredService.getConfigurationLocation() ) );
            subController.init( controllerConf, metadataConfig, mainConfig );
            LOG.info( "" );
            // round to exactly two decimals, I think their should be a java method for this though
            double startupTime = Math.round( ( ( System.currentTimeMillis() - time ) * 0.1 ) ) * 0.01;
            LOG.info( serviceName + " startup successful (took: " + startupTime + " seconds)" );
        } catch ( Exception e ) {
            LOG.error( "Initializing {} failed: {}", serviceName, e.getMessage() );
            LOG.error( "Set the log level to TRACE to get the stack trace." );
            LOG.trace( "Stack trace:", e );
            LOG.info( "" );
            LOG.info( serviceName + " startup failed." );
            subController = null;
        }
        return subController;
    }

    private void registerSubController( ServiceType configuredService, AbstractOGCServiceController serviceController ) {

        // associate service name (abbreviation) with controller instance
        LOG.debug( "Service name '" + configuredService.getServiceName() + "' -> '"
                   + serviceController.getClass().getSimpleName() + "'" );
        serviceNameToController.put( configuredService.getServiceName(), serviceController );

        // associate request types with controller instance
        for ( String request : serviceController.getHandledRequests() ) {
            // skip GetCapabilities requests
            if ( !( "GetCapabilities".equals( request ) ) ) {
                LOG.debug( "Request type '" + request + "' -> '" + serviceController.getClass().getSimpleName() + "'" );
                requestNameToController.put( request, serviceController );
            }
        }

        // associate namespaces with controller instance
        for ( String ns : serviceController.getHandledNamespaces() ) {
            LOG.debug( "Namespace '" + ns + "' -> '" + serviceController.getClass().getSimpleName() + "'" );
            serviceNSToController.put( ns, serviceController );
        }
    }

    /**
     * Determines the {@link AbstractOGCServiceController} that is responsible for handling requests to a certain
     * service type, e.g. WMS, WFS.
     * 
     * @param serviceType
     *            service type code, e.g. "WMS" or "WFS"
     * @return responsible <code>SecuredSubController</code> or null, if no responsible controller was found
     */
    public AbstractOGCServiceController determineResponsibleControllerByServiceName( String serviceType ) {
        AllowedServices service = AllowedServices.fromValue( serviceType );
        if ( service == null ) {
            return null;
        }
        return serviceNameToController.get( service );
    }

    /**
     * Determines the {@link AbstractOGCServiceController} that is responsible for handling requests with a certain
     * name, e.g. GetMap, GetFeature.
     * 
     * @param requestName
     *            request name, e.g. "GetMap" or "GetFeature"
     * @return responsible <code>SecuredSubController</code> or null, if no responsible controller was found
     */
    public AbstractOGCServiceController determineResponsibleControllerByRequestName( String requestName ) {
        return requestNameToController.get( requestName );
    }

    /**
     * Determines the {@link AbstractOGCServiceController} that is responsible for handling requests to a certain
     * service type, e.g. WMS, WFS.
     * 
     * @param ns
     *            service type code, e.g. "WMS" or "WFS"
     * @return responsible <code>SecuredSubController</code> or null, if no responsible controller was found
     */
    public AbstractOGCServiceController determineResponsibleControllerByNS( String ns ) {
        return serviceNSToController.get( ns );
    }

    /**
     * Return all active service controllers.
     * 
     * @return the instance of the requested service used by OGCFrontController, or null if the service is not
     *         registered.
     */
    public Map<String, AbstractOGCServiceController> getServiceControllers() {
        Map<String, AbstractOGCServiceController> nameToController = new HashMap<String, AbstractOGCServiceController>();
        for ( AllowedServices serviceName : serviceNameToController.keySet() ) {
            nameToController.put( serviceName.value(), serviceNameToController.get( serviceName ) );
        }
        return nameToController;
    }

    /**
     * Returns the service controller instance based on the class of the service controller.
     * 
     * @param <T>
     * 
     * @param c
     *            class of the requested service controller, e.g. <code>WPSController.getClass()</code>
     * @return the instance of the requested service used by OGCFrontController, or null if no such service controller
     *         is active
     */
    public <T extends AbstractOGCServiceController> T getServiceController( Class<T> c ) {
        for ( AbstractOGCServiceController it : serviceNSToController.values() ) {
            if ( c == it.getClass() ) {
                // somehow just annotating the return expression does not work
                // even annotations to suppress sucking generics suck
                @SuppressWarnings(value = "unchecked")
                T result = (T) it;
                return result;
            }
        }
        return null;
    }

    /**
     * 
     */
    public void destroy() {
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Shutting down deegree web services in context..." );
        for ( AllowedServices serviceName : serviceNameToController.keySet() ) {
            AbstractOGCServiceController subcontroller = serviceNameToController.get( serviceName );
            LOG.info( "Shutting down '" + serviceName + "'." );
            try {
                subcontroller.destroy();
            } catch ( Exception e ) {
                String msg = "Error destroying subcontroller '" + subcontroller.getClass().getName() + "': "
                             + e.getMessage();
                LOG.error( msg, e );
            }
        }
        LOG.info( "deegree OGC webservices shut down." );
        LOG.info( "--------------------------------------------------------------------------------" );
    }

    /**
     * @return the JAXB main configuration
     */
    public DeegreeServiceControllerType getMainConfiguration() {
        return mainConfig;
    }

    private void initRequestLogger() {
        FrontControllerOptionsType opts = mainConfig.getFrontControllerOptions();
        RequestLogging requestLogging = opts == null ? null : opts.getRequestLogging();
        if ( requestLogging != null ) {
            org.deegree.services.jaxb.main.FrontControllerOptionsType.RequestLogging.RequestLogger logger = requestLogging.getRequestLogger();
            requestLogger = instantiateRequestLogger( logger );
            this.logOnlySuccessful = requestLogging.isOnlySuccessful() != null && requestLogging.isOnlySuccessful();
        }
    }

    private static RequestLogger instantiateRequestLogger(
                                                           org.deegree.services.jaxb.main.FrontControllerOptionsType.RequestLogging.RequestLogger conf ) {
        if ( conf != null ) {
            String cls = conf.getClazz();
            try {
                Object o = conf.getConfiguration();
                if ( o == null ) {
                    return (RequestLogger) forName( cls ).newInstance();
                }
                return (RequestLogger) forName( cls ).getDeclaredConstructor( Object.class ).newInstance( o );
            } catch ( ClassNotFoundException e ) {
                LOG.info( "The request logger class '{}' could not be found on the classpath.", cls );
                LOG.trace( "Stack trace:", e );
            } catch ( ClassCastException e ) {
                LOG.info( "The request logger class '{}' does not implement the RequestLogger interface.", cls );
                LOG.trace( "Stack trace:", e );
            } catch ( InstantiationException e ) {
                LOG.info( "The request logger class '{}' could not be instantiated"
                          + " (needs a default constructor without arguments if no configuration is given).", cls );
                LOG.trace( "Stack trace:", e );
            } catch ( IllegalAccessException e ) {
                LOG.info( "The request logger class '{}' could not be instantiated"
                          + " (default constructor needs to be accessible if no configuration is given).", cls );
                LOG.trace( "Stack trace:", e );
            } catch ( IllegalArgumentException e ) {
                LOG.info( "The request logger class '{}' could not be instantiated"
                          + " (constructor needs to take an object argument if configuration is given).", cls );
                LOG.trace( "Stack trace:", e );
            } catch ( java.lang.SecurityException e ) {
                LOG.info( "The request logger class '{}' could not be instantiated"
                          + " (JVM does have insufficient rights to instantiate the class).", cls );
                LOG.trace( "Stack trace:", e );
            } catch ( InvocationTargetException e ) {
                LOG.info( "The request logger class '{}' could not be instantiated"
                          + " (constructor call threw an exception).", cls );
                LOG.trace( "Stack trace:", e );
            } catch ( NoSuchMethodException e ) {
                LOG.info( "The request logger class '{}' could not be instantiated"
                          + " (constructor needs to take an object argument if configuration is given).", cls );
                LOG.trace( "Stack trace:", e );
            }
        }
        return new StandardRequestLogger();
    }

    /**
     * @return null, if none was configured
     */
    public RequestLogger getRequestLogger() {
        return requestLogger;
    }

    /**
     * @return true, if the option was set in the logging configuration
     */
    public boolean logOnlySuccessful() {
        return logOnlySuccessful;
    }

}
