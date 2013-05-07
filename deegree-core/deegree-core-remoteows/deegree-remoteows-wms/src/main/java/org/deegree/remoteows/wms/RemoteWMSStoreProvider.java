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
package org.deegree.remoteows.wms;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.remoteows.RemoteOWSStore;
import org.deegree.remoteows.RemoteOWSStoreProvider;
import org.deegree.remoteows.wms.RemoteWMSStore.LayerOptions;
import org.deegree.remoteows.wms.jaxb.AuthenticationType;
import org.deegree.remoteows.wms.jaxb.HTTPBasicAuthenticationType;
import org.deegree.remoteows.wms.jaxb.ParameterScopeType;
import org.deegree.remoteows.wms.jaxb.ParameterUseType;
import org.deegree.remoteows.wms.jaxb.RemoteWMSStore;
import org.deegree.remoteows.wms.jaxb.RequestOptionsType;
import org.deegree.remoteows.wms.jaxb.RequestOptionsType.Parameter;
import org.deegree.remoteows.wms.jaxb.RequestedLayerType;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@Deprecated
public class RemoteWMSStoreProvider implements RemoteOWSStoreProvider {

    private static final Logger LOG = getLogger( RemoteWMSStoreProvider.class );

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.remoteows.wms.jaxb";

    private static final URL CONFIG_SCHEMA = RemoteWMSStoreProvider.class.getResource( "/META-INF/schemas/datasource/remoteows/wms/3.1.0/remotewms.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public List<String> getCapabilitiesNamespaces() {
        return null;
    }

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/datasource/remoteows/wms";
    }

    @Override
    public String getServiceType() {
        return "WMS";
    }

    private static boolean fillLayerOptions( LayerOptions opts, RequestOptionsType ropts ) {
        if ( ropts != null ) {
            if ( ropts.getImageFormat() != null ) {
                opts.imageFormat = ropts.getImageFormat().getValue();
                opts.transparent = ropts.getImageFormat().isTransparent();
            }
            if ( ropts.getDefaultCRS() != null ) {
                opts.defaultCRS = ropts.getDefaultCRS().getValue();
                opts.alwaysUseDefaultCRS = ropts.getDefaultCRS().isUseAlways();
            }
            if ( ropts.getParameter() != null ) {
                for ( Parameter p : ropts.getParameter() ) {
                    String name = p.getName();
                    String value = p.getValue();
                    ParameterUseType use = p.getUse();
                    ParameterScopeType scope = p.getScope();
                    switch ( use ) {
                    case ALLOW_OVERRIDE:
                        switch ( scope ) {
                        case GET_MAP:
                            opts.defaultParametersGetMap.put( name, value );
                            break;
                        case GET_FEATURE_INFO:
                            opts.defaultParametersGetFeatureInfo.put( name, value );
                            break;
                        default:
                            opts.defaultParametersGetMap.put( name, value );
                            opts.defaultParametersGetFeatureInfo.put( name, value );
                            break;
                        }
                        break;
                    case FIXED:
                        switch ( scope ) {
                        case GET_MAP:
                            opts.hardParametersGetMap.put( name, value );
                            break;
                        case GET_FEATURE_INFO:
                            opts.hardParametersGetFeatureInfo.put( name, value );
                            break;
                        default:
                            opts.hardParametersGetMap.put( name, value );
                            opts.hardParametersGetFeatureInfo.put( name, value );
                            break;
                        }
                        break;
                    }
                }
            }
        }
        return ropts != null;
    }

    @Override
    public RemoteOWSStore create( URL config ) {
        LOG.warn( "The use of the old style remote WMS stores is deprecated." );
        LOG.warn( "Please switch to the use of remote WMS layer/theme stores if possible." );
        try {
            RemoteWMSStore cfg = (RemoteWMSStore) unmarshall( CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA, config, workspace );
            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( config.toString() );
            URL capas = resolver.resolve( cfg.getCapabilitiesDocumentLocation().getLocation() );

            int connTimeout = cfg.getConnectionTimeout() == null ? 5 : cfg.getConnectionTimeout();
            int reqTimeout = cfg.getRequestTimeout() == null ? 60 : cfg.getRequestTimeout();

            OldWMSClient111 client;

            AuthenticationType type = cfg.getAuthentication() == null ? null : cfg.getAuthentication().getValue();
            String user = null;
            String pass = null;
            if ( type instanceof HTTPBasicAuthenticationType ) {
                HTTPBasicAuthenticationType basic = (HTTPBasicAuthenticationType) type;
                user = basic.getUsername();
                pass = basic.getPassword();
            }
            client = new OldWMSClient111( capas, connTimeout, reqTimeout, user, pass );

            Map<String, LayerOptions> layers = new HashMap<String, LayerOptions>();
            List<String> layerOrder = new LinkedList<String>();
            RequestOptionsType def = cfg.getDefaultRequestOptions();
            boolean optionsOverridden = false;
            for ( RequestedLayerType rlt : cfg.getRequestedLayer() ) {
                layerOrder.add( rlt.getName() );
                LayerOptions opts = new LayerOptions();
                fillLayerOptions( opts, def );
                optionsOverridden = fillLayerOptions( opts, rlt.getRequestOptions() ) || optionsOverridden;
                layers.put( rlt.getName(), opts );
            }
            if ( optionsOverridden ) {
                LOG.debug( "Configured remote WMS store with extended options, this will result in one request per layer." );
                return new org.deegree.remoteows.wms.RemoteWMSStore( client, layers, layerOrder );
            }
            LOG.debug( "Configured remote WMS store with standard options, this enables an efficient request for all layers." );
            LayerOptions opts = new LayerOptions();
            fillLayerOptions( opts, def );
            return new org.deegree.remoteows.wms.RemoteWMSStore( client, layerOrder, opts );
        } catch ( JAXBException e ) {
            e.printStackTrace();
            LOG.warn( "Remote WMS store config at '{}' could not be parsed: {}", config, e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( ClassCastException e ) {
            e.printStackTrace();
            LOG.warn( "Remote WMS store config at '{}' could not be parsed: {}", config, e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
            LOG.warn( "Remote WMS store config at '{}' could not be parsed: {}", config, e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
        return null;
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
    }

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }
}