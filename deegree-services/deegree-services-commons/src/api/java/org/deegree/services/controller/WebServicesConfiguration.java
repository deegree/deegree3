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
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.xml.bind.JAXBElement;

import org.deegree.commons.config.AbstractResourceManager;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ExtendedResourceManager;
import org.deegree.commons.config.ExtendedResourceProvider;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceManagerMetadata;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.FileUtils;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.controller.utils.StandardRequestLogger;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType.RequestLogging;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.slf4j.Logger;

/**
 * {@link ExtendedResourceManager} for {@link OWS} (and web service configuration).
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WebServicesConfiguration extends AbstractResourceManager<OWS<?>> {

    private static final Logger LOG = getLogger( WebServicesConfiguration.class );

    private static final String CONTROLLER_JAXB_PACKAGE = "org.deegree.services.jaxb.controller";

    private static final URL CONTROLLER_CONFIG_SCHEMA = WebServicesConfiguration.class.getResource( "/META-INF/schemas/controller/3.0.0/controller.xsd" );

    private static final String METADATA_JAXB_PACKAGE = "org.deegree.services.jaxb.metadata";

    private static final URL METADATA_CONFIG_SCHEMA = WebServicesConfiguration.class.getResource( "/META-INF/schemas/metadata/3.0.0/metadata.xsd" );

    // maps service names (e.g. 'WMS', 'WFS', ...) to responsible subcontrollers
    private final Map<String, OWS<? extends Enum<?>>> serviceNameToController = new HashMap<String, OWS<? extends Enum<?>>>();

    // maps service namespaces (e.g. 'http://www.opengis.net/wms', 'http://www.opengis.net/wfs', ...) to the
    // responsible subcontrollers
    private final Map<String, OWS<? extends Enum<?>>> serviceNSToController = new HashMap<String, OWS<? extends Enum<?>>>();

    // maps request names (e.g. 'GetMap', 'DescribeFeatureType') to the responsible subcontrollers
    private final Map<String, OWS<? extends Enum<?>>> requestNameToController = new HashMap<String, OWS<? extends Enum<?>>>();

    private DeegreeServicesMetadataType metadataConfig;

    private DeegreeServiceControllerType mainConfig;

    private RequestLogger requestLogger;

    private boolean logOnlySuccessful;

    static List<ResourceProvider> providers = new ArrayList<ResourceProvider>();

    private static Class<? extends ResourceManager>[] dependencies;

    static {
        updateDependencies( null );
    }

    private static void updateDependencies( DeegreeWorkspace workspace ) {
        providers.clear();
        List<Class<? extends ResourceManager>> deps = new LinkedList<Class<? extends ResourceManager>>();

        deps.add( ProxyUtils.class );
        deps.add( ConnectionManager.class );

        @SuppressWarnings("unchecked")
        Iterator<OWSProvider> iter;
        if ( workspace != null ) {
            iter = ServiceLoader.load( OWSProvider.class, workspace.getModuleClassLoader() ).iterator();
        } else {
            iter = ServiceLoader.load( OWSProvider.class ).iterator();
        }
        while ( iter.hasNext() ) {
            OWSProvider<?> prov = iter.next();
            providers.add( prov );
            deps.addAll( asList( prov.getDependencies() ) );
        }

        dependencies = deps.toArray( new Class[deps.size()] );
    }

    @Override
    public void startup( DeegreeWorkspace workspace )
                            throws ResourceInitException {

        this.workspace = workspace;
        updateDependencies( workspace );

        File metadata = new File( workspace.getLocation(), "services" + File.separator + "metadata.xml" );
        File main = new File( workspace.getLocation(), "services" + File.separator + "main.xml" );

        try {
            URL mdurl;
            if ( !metadata.exists() ) {
                mdurl = WebServicesConfiguration.class.getResource( "/META-INF/schemas/metadata/3.0.0/example.xml" );
                String msg = "No 'services/metadata.xml' file, assuming defaults.";
                LOG.debug( msg );
            } else {
                mdurl = metadata.toURI().toURL();
            }
            metadataConfig = (DeegreeServicesMetadataType) ( (JAXBElement<?>) JAXBUtils.unmarshall( METADATA_JAXB_PACKAGE,
                                                                                                    METADATA_CONFIG_SCHEMA,
                                                                                                    mdurl, workspace ) ).getValue();
        } catch ( Exception e ) {
            String msg = "Could not unmarshall frontcontroller configuration: " + e.getMessage();
            LOG.error( msg );
            throw new ResourceInitException( msg, e );
        }
        if ( !main.exists() ) {
            LOG.debug( "No 'services/main.xml' file, assuming defaults." );
            mainConfig = new DeegreeServiceControllerType();
        } else {
            try {
                mainConfig = (DeegreeServiceControllerType) ( (JAXBElement<?>) JAXBUtils.unmarshall( CONTROLLER_JAXB_PACKAGE,
                                                                                                     CONTROLLER_CONFIG_SCHEMA,
                                                                                                     main.toURI().toURL(),
                                                                                                     workspace ) ).getValue();
            } catch ( Exception e ) {
                mainConfig = new DeegreeServiceControllerType();
                LOG.info( "main.xml could not be loaded. Proceeding with defaults." );
                LOG.debug( "Error was: '{}'.", e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            }
        }

        initRequestLogger();

        @SuppressWarnings("unchecked")
        Iterator<OWSProvider> iter = ServiceLoader.load( OWSProvider.class, workspace.getModuleClassLoader() ).iterator();
        Map<String, OWSProvider<? extends Enum<?>>> providers = new HashMap<String, OWSProvider<? extends Enum<?>>>();
        while ( iter.hasNext() ) {
            OWSProvider<?> p = iter.next();
            providers.put( p.getImplementationMetadata().getImplementedServiceName().toUpperCase(), p );
        }

        this.workspace = workspace;
        ResourceManagerMetadata<OWS<?>> md = getMetadata();
        if ( md != null ) {
            for ( ResourceProvider p : md.getResourceProviders() ) {
                ( (OWSProvider<?>) p ).init( workspace );
                nsToProvider.put( p.getConfigNamespace(), (ExtendedResourceProvider<OWS<?>>) p );
            }

            dir = new File( workspace.getLocation(), md.getPath() );
            name = md.getName();
            if ( !dir.exists() ) {
                LOG.info( "No '{}' directory -- skipping initialization of {}.", md.getPath(), name );
                return;
            }
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "Setting up {}.", name );
            LOG.info( "--------------------------------------------------------------------------------" );

            List<File> files = FileUtils.findFilesForExtensions( dir, true, "xml,ignore" );

            for ( File configFile : files ) {
                String fileName = configFile.getName();
                if ( !fileName.equals( "metadata.xml" ) && !fileName.equals( "main.xml" ) ) {
                    try {
                        ResourceState<OWS<?>> state = processResourceConfig( configFile );
                        idToState.put( state.getId(), state );
                    } catch ( Throwable t ) {
                        LOG.error( t.getMessage(), t );
                    }
                }
            }
            LOG.info( "" );
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
    public OWS<? extends Enum<?>> determineResponsibleControllerByServiceName( String serviceType ) {
        return serviceNameToController.get( serviceType );
    }

    /**
     * Determines the {@link AbstractOGCServiceController} that is responsible for handling requests with a certain
     * name, e.g. GetMap, GetFeature.
     * 
     * @param requestName
     *            request name, e.g. "GetMap" or "GetFeature"
     * @return responsible <code>SecuredSubController</code> or null, if no responsible controller was found
     */
    public OWS<? extends Enum<?>> determineResponsibleControllerByRequestName( String requestName ) {
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
    public OWS<? extends Enum<?>> determineResponsibleControllerByNS( String ns ) {
        return serviceNSToController.get( ns );
    }

    /**
     * Return all active service controllers.
     * 
     * @return the instance of the requested service used by OGCFrontController, or null if the service is not
     *         registered.
     */
    public Map<String, OWS<? extends Enum<?>>> getServiceControllers() {
        Map<String, OWS<? extends Enum<?>>> nameToController = new HashMap<String, OWS<? extends Enum<?>>>();
        for ( String serviceName : serviceNameToController.keySet() ) {
            nameToController.put( serviceName, serviceNameToController.get( serviceName ) );
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
    public <T extends Enum<T>, U extends OWS<T>> U getServiceController( Class<U> c ) {
        for ( OWS<?> it : serviceNSToController.values() ) {
            if ( c == it.getClass() ) {
                // somehow just annotating the return expression does not work
                // even annotations to suppress sucking generics suck
                @SuppressWarnings(value = "unchecked")
                U result = (U) it;
                return result;
            }
        }
        return null;
    }

    @Override
    public void shutdown() {
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Shutting down deegree web services in context..." );
        for ( String serviceName : serviceNameToController.keySet() ) {
            OWS<?> subcontroller = serviceNameToController.get( serviceName );
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

    /**
     * @return the JAXB metadata configuration
     */
    public DeegreeServicesMetadataType getMetadataConfiguration() {
        return metadataConfig;
    }

    private void initRequestLogger() {
        RequestLogging requestLogging = mainConfig.getRequestLogging();
        if ( requestLogging != null ) {
            org.deegree.services.jaxb.controller.DeegreeServiceControllerType.RequestLogging.RequestLogger logger = requestLogging.getRequestLogger();
            requestLogger = instantiateRequestLogger( logger );
            this.logOnlySuccessful = requestLogging.isOnlySuccessful() != null && requestLogging.isOnlySuccessful();
        }
    }

    private static RequestLogger instantiateRequestLogger( RequestLogging.RequestLogger conf ) {
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

    public Class<? extends ResourceManager>[] getDependencies() {
        return dependencies;
    }

    public DeegreeWorkspace getWorkspace() {
        return workspace;
    }

    static class WebServiceManagerMetadata implements ResourceManagerMetadata {
        public String getName() {
            return "web services";
        }

        public String getPath() {
            return "services";
        }

        public List<ResourceProvider> getResourceProviders() {
            return providers;
        }
    }

    public ResourceManagerMetadata getMetadata() {
        return new WebServiceManagerMetadata();
    }

    @Override
    protected void add( OWS ows ) {
        ImplementationMetadata<?> md = ows.getImplementationMetadata();
        LOG.debug( "Service name '" + md.getImplementedServiceName() + "' -> '" + ows.getClass().getSimpleName() + "'" );
        serviceNameToController.put( md.getImplementedServiceName().toUpperCase(), ows );

        // associate request types with controller instance
        for ( String request : md.getHandledRequests() ) {
            // skip GetCapabilities requests
            if ( !( "GetCapabilities".equals( request ) ) ) {
                LOG.debug( "Request type '" + request + "' -> '" + ows.getClass().getSimpleName() + "'" );
                requestNameToController.put( request, ows );
            }
        }

        // associate namespaces with controller instance
        for ( String ns : md.getHandledNamespaces() ) {
            LOG.debug( "Namespace '" + ns + "' -> '" + ows.getClass().getSimpleName() + "'" );
            serviceNSToController.put( ns, ows );
        }
    }

    @Override
    protected void remove( OWS ows ) {
        ImplementationMetadata<?> md = ows.getImplementationMetadata();
        LOG.debug( "Service name '" + md.getImplementedServiceName() + "' -> '" + ows.getClass().getSimpleName() + "'" );
        serviceNameToController.remove( md.getImplementedServiceName().toUpperCase() );

        for ( String request : md.getHandledRequests() ) {
            // skip GetCapabilities requests
            if ( !( "GetCapabilities".equals( request ) ) ) {
                LOG.debug( "Request type '" + request + "' -> '" + ows.getClass().getSimpleName() + "'" );
                requestNameToController.remove( request );
            }
        }

        for ( String ns : md.getHandledNamespaces() ) {
            LOG.debug( "Namespace '" + ns + "' -> '" + ows.getClass().getSimpleName() + "'" );
            serviceNSToController.remove( ns );
        }
    }
}