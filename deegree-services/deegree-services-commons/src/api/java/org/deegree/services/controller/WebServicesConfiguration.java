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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.deegree.commons.config.AbstractResourceManager;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.DefaultResourceManagerMetadata;
import org.deegree.commons.config.ExtendedResourceManager;
import org.deegree.commons.config.ExtendedResourceProvider;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceManagerMetadata;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.utils.FileUtils;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.services.OWS;
import org.deegree.services.OWSProvider;
import org.deegree.services.controller.utils.StandardRequestLogger;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType.RequestLogging;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.metadata.OWSMetadataProviderManager;
import org.slf4j.Logger;

/**
 * {@link ExtendedResourceManager} for {@link OWS} (and web service configuration).
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WebServicesConfiguration extends AbstractResourceManager<OWS> {

    private static final Logger LOG = getLogger( WebServicesConfiguration.class );

    private static final String CONTROLLER_JAXB_PACKAGE = "org.deegree.services.jaxb.controller";

    private static final URL CONTROLLER_CONFIG_SCHEMA = WebServicesConfiguration.class.getResource( "/META-INF/schemas/services/controller/3.0.0/controller.xsd" );

    private static final String METADATA_JAXB_PACKAGE = "org.deegree.services.jaxb.metadata";

    private static final URL METADATA_CONFIG_SCHEMA = WebServicesConfiguration.class.getResource( "/META-INF/schemas/services/metadata/3.2.0/metadata.xsd" );

    // maps service names (e.g. 'WMS', 'WFS', ...) to OWS instances
    private final Map<String, List<OWS>> ogcNameToService = new HashMap<String, List<OWS>>();

    // maps service namespaces (e.g. 'http://www.opengis.net/wms', 'http://www.opengis.net/wfs', ...) to OWS instances
    private final Map<String, List<OWS>> requestNsToService = new HashMap<String, List<OWS>>();

    // maps request names (e.g. 'GetMap', 'DescribeFeatureType') to OWS instances
    private final Map<String, List<OWS>> requestNameToService = new HashMap<String, List<OWS>>();

    private DeegreeServicesMetadataType metadataConfig;

    private DeegreeServiceControllerType mainConfig;

    private RequestLogger requestLogger;

    private boolean logOnlySuccessful;

    private WebServiceManagerMetadata metadata;

    @Override
    public void initMetadata( DeegreeWorkspace workspace ) {
        metadata = new WebServiceManagerMetadata( workspace );
    }

    @Override
    public void startup( DeegreeWorkspace workspace )
                            throws ResourceInitException {

        this.workspace = workspace;

        File metadata = new File( workspace.getLocation(), "services" + File.separator + "metadata.xml" );
        File main = new File( workspace.getLocation(), "services" + File.separator + "main.xml" );

        try {
            URL mdurl;
            if ( !metadata.exists() ) {
                mdurl = WebServicesConfiguration.class.getResource( "/META-INF/schemas/services/metadata/3.2.0/example.xml" );
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

        // @SuppressWarnings("unchecked")
        // Iterator<OWSProvider> iter = ServiceLoader.load( OWSProvider.class, workspace.getModuleClassLoader()
        // ).iterator();
        // Map<String, OWSProvider<? extends Enum<?>>> providers = new HashMap<String, OWSProvider<? extends
        // Enum<?>>>();
        // while ( iter.hasNext() ) {
        // OWSProvider<?> p = iter.next();
        // for ( String serviceName : p.getImplementationMetadata().getImplementedServiceName() ) {
        // providers.put( serviceName.toUpperCase(), p );
        // }
        // }

        this.workspace = workspace;
        ResourceManagerMetadata<OWS> md = getMetadata();
        if ( md != null ) {
            for ( ResourceProvider p : md.getResourceProviders() ) {
                try {
                    ( (OWSProvider) p ).init( workspace );
                    nsToProvider.put( p.getConfigNamespace(), (ExtendedResourceProvider<OWS>) p );
                } catch ( Throwable t ) {
                    LOG.error( "Initializing of service provider " + p.getClass() + " failed.", t );
                }
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
                if ( !fileName.endsWith( "_metadata.xml" ) && !fileName.equals( "metadata.xml" )
                     && !fileName.equals( "main.xml" ) ) {
                    try {
                        ResourceState<OWS> state = processResourceConfig( configFile );
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
     * Returns the {@link OWS} instance that is responsible for handling requests to a certain service type, e.g. WMS,
     * WFS.
     * 
     * @param serviceType
     *            service type code, e.g. "WMS" or "WFS"
     * @return responsible <code>OWS</code> or null, if no responsible service was found
     */
    public List<OWS> getByServiceType( String serviceType ) {
        return ogcNameToService.get( serviceType.toUpperCase() );
    }

    /**
     * Returns the {@link OWS} instance that is responsible for handling requests with a certain name, e.g. GetMap,
     * GetFeature.
     * 
     * @param requestName
     *            request name, e.g. "GetMap" or "GetFeature"
     * @return responsible <code>OWS</code> or null, if no responsible service was found
     */
    public List<OWS> getByRequestName( String requestName ) {
        return requestNameToService.get( requestName );
    }

    /**
     * Determines the {@link OWS} instance that is responsible for handling XML requests in the given namespace.
     * 
     * @param ns
     *            XML namespace
     * @return responsible <code>OWS</code> or null, if no responsible service was found
     */
    public List<OWS> getByRequestNS( String ns ) {
        return requestNsToService.get( ns );
    }

    /**
     * Return all active {@link OWS}.
     * 
     * @return the instance of the requested service used by OGCFrontController, or null if the service is not
     *         registered.
     */
    public Map<String, List<OWS>> getAll() {
        return ogcNameToService;
    }

    /**
     * Returns the service controller instance based on the class of the service controller.
     * 
     * @param c
     *            class of the requested service controller, e.g. <code>WPSController.getClass()</code>
     * @return the instance of the requested service used by OGCFrontController, or null if no such service controller
     *         is active
     */
    public List<OWS> getByOWSClass( Class<?> c ) {
        List<OWS> services = new ArrayList<OWS>();
        for ( ResourceState<OWS> state : getStates() ) {
            OWS ows = state.getResource();
            if ( ows != null ) {
                if ( c == ows.getClass() ) {
                    services.add( ows );
                }
            }
        }
        return services;
    }

    @Override
    public void shutdown() {
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Shutting down deegree web services in context..." );
        for ( ResourceState<OWS> state : getStates() ) {
            OWS ows = state.getResource();
            if ( ows != null ) {
                LOG.info( "Shutting down service: " + state.getId() + "" );
                try {
                    ows.destroy();
                } catch ( Throwable e ) {
                    String msg = "Error shutting down service '" + state.getId() + "': " + e.getMessage();
                    LOG.error( msg, e );
                }
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

    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { OWSMetadataProviderManager.class };
    }

    public DeegreeWorkspace getWorkspace() {
        return workspace;
    }

    static class WebServiceManagerMetadata extends DefaultResourceManagerMetadata<OWS> {
        WebServiceManagerMetadata( DeegreeWorkspace workspace ) {
            super( "web services", "services/", OWSProvider.class, workspace );
        }
    }

    @Override
    public ResourceManagerMetadata getMetadata() {
        return metadata;
    }

    @Override
    protected void add( OWS ows ) {
        ImplementationMetadata<?> md = ows.getImplementationMetadata();
        for ( String serviceName : md.getImplementedServiceName() ) {
            LOG.debug( "Service name '" + serviceName + "' -> '" + ows.getClass().getSimpleName() + "'" );
            put( ogcNameToService, serviceName.toUpperCase(), ows );
        }

        // associate request types with controller instance
        for ( String request : md.getHandledRequests() ) {
            // skip GetCapabilities requests
            if ( !( "GetCapabilities".equals( request ) ) ) {
                LOG.debug( "Request type '" + request + "' -> '" + ows.getClass().getSimpleName() + "'" );
                put( requestNameToService, request, ows );
            }
        }

        // associate namespaces with controller instance
        for ( String ns : md.getHandledNamespaces() ) {
            LOG.debug( "Namespace '" + ns + "' -> '" + ows.getClass().getSimpleName() + "'" );
            put( requestNsToService, ns, ows );
        }
    }

    @Override
    protected void remove( OWS ows ) {
        ImplementationMetadata<?> md = ows.getImplementationMetadata();
        for ( String serviceName : md.getImplementedServiceName() ) {
            LOG.debug( "Service name '" + serviceName + "' -> '" + ows.getClass().getSimpleName() + "'" );
            remove( ogcNameToService, serviceName.toUpperCase(), ows );
        }

        for ( String request : md.getHandledRequests() ) {
            // skip GetCapabilities requests
            if ( !( "GetCapabilities".equals( request ) ) ) {
                LOG.debug( "Request type '" + request + "' -> '" + ows.getClass().getSimpleName() + "'" );
                remove( requestNameToService, request, ows );
            }
        }

        for ( String ns : md.getHandledNamespaces() ) {
            LOG.debug( "Namespace '" + ns + "' -> '" + ows.getClass().getSimpleName() + "'" );
            remove( requestNsToService, ns, ows );
        }
    }

    private void put( Map<String, List<OWS>> map, String key, OWS value ) {
        List<OWS> values = map.get( key );
        if ( values == null ) {
            values = new ArrayList<OWS>();
            map.put( key, values );
        }
        if ( !values.contains( value ) ) {
            values.add( value );
        }
    }

    private void remove( Map<String, List<OWS>> map, String key, OWS value ) {
        List<OWS> values = map.get( key );
        if ( values != null ) {
            values.remove( value );
        }
    }
}