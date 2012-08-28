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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wmts.controller;

import static org.apache.commons.io.IOUtils.copy;
import static org.deegree.commons.tom.ows.Version.parseVersion;
import static org.deegree.protocol.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.protocol.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.protocol.ows.exception.OWSException.OPERATION_NOT_SUPPORTED;
import static org.deegree.services.metadata.MetadataUtils.convertFromJAXB;
import static org.deegree.services.wmts.WMTSProvider.IMPLEMENTATION_METADATA;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.layer.Layer;
import org.deegree.layer.persistence.tile.TileLayer;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.ows.getcapabilities.GetCapabilitiesKVPParser;
import org.deegree.protocol.ows.metadata.ServiceIdentification;
import org.deegree.protocol.ows.metadata.ServiceProvider;
import org.deegree.protocol.wmts.WMTSConstants.WMTSRequestType;
import org.deegree.protocol.wmts.ops.GetTile;
import org.deegree.services.authentication.SecurityException;
import org.deegree.services.controller.AbstractOWS;
import org.deegree.services.controller.ImplementationMetadata;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.metadata.OWSMetadataProviderManager;
import org.deegree.services.ows.OWSException110XMLAdapter;
import org.deegree.services.wmts.controller.capabilities.WMTSCapabilitiesWriter;
import org.deegree.services.wmts.jaxb.DeegreeWMTS;
import org.deegree.theme.Theme;
import org.deegree.theme.Themes;
import org.deegree.theme.persistence.ThemeManager;
import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.slf4j.Logger;

/**
 * <code>WMTSController</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
public class WMTSController extends AbstractOWS {

    private static final Logger LOG = getLogger( WMTSController.class );

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.services.wmts.jaxb";

    private static final String CONFIG_SCHEMA = "/META-INF/schemas/wmts/3.2.0/wmts.xsd";

    private ServiceIdentification identification;

    private ServiceProvider provider;

    private List<Theme> themes = new ArrayList<Theme>();

    private Map<String, TileLayer> layers = new HashMap<String, TileLayer>();

    private String metadataUrlTemplate;

    /**
     * @param configURL
     * @param serviceInfo
     */
    public WMTSController( URL configURL, ImplementationMetadata<?> serviceInfo ) {
        super( configURL, serviceInfo );
    }

    @Override
    public void init( DeegreeServicesMetadataType serviceMetadata, DeegreeServiceControllerType mainConfig,
                      ImplementationMetadata<?> md, XMLAdapter controllerConf )
                            throws ResourceInitException {
        super.init( serviceMetadata, mainConfig, IMPLEMENTATION_METADATA, controllerConf );

        identification = convertFromJAXB( mainMetadataConf.getServiceIdentification() );
        provider = convertFromJAXB( mainMetadataConf.getServiceProvider() );

        OWSMetadataProviderManager mmgr = workspace.getSubsystemManager( OWSMetadataProviderManager.class );
        ResourceState<OWSMetadataProvider> state = mmgr.getState( getId() );
        if ( state != null ) {
            OWSMetadataProvider metadata = state.getResource();
            if ( metadata != null ) {
                identification = metadata.getServiceIdentification();
                provider = metadata.getServiceProvider();
            }
        }

        DeegreeWMTS conf = (DeegreeWMTS) unmarshallConfig( CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA, controllerConf );
        ThemeManager mgr = workspace.getSubsystemManager( ThemeManager.class );

        this.metadataUrlTemplate = conf.getMetadataURLTemplate();

        List<String> ids = conf.getServiceConfiguration().getThemeId();
        for ( String id : ids ) {
            Theme t = mgr.get( id );
            if ( t == null ) {
                LOG.warn( "Theme with id {} was not available.", id );
                continue;
            }
            themes.add( t );

            for ( Layer l : Themes.getAllLayers( t ) ) {
                if ( l instanceof TileLayer ) {
                    layers.put( l.getMetadata().getName(), ( (TileLayer) l ) );
                }
            }

        }
    }

    @Override
    public void doKVP( Map<String, String> map, HttpServletRequest request, HttpResponseBuffer response,
                       List<FileItem> multiParts )
                            throws ServletException, IOException, SecurityException {
        String v = map.get( "VERSION" );
        Version version = v == null ? serviceInfo.getSupportedConfigVersions().iterator().next() : parseVersion( v );

        WMTSRequestType req;
        try {
            req = (WMTSRequestType) ( (ImplementationMetadata) serviceInfo ).getRequestTypeByName( map.get( "REQUEST" ) );
        } catch ( IllegalArgumentException e ) {
            sendException( new OWSException( "'" + map.get( "REQUEST" ) + "' is not a supported WMTS operation.",
                                             OWSException.OPERATION_NOT_SUPPORTED ), response );
            return;
        } catch ( NullPointerException e ) {
            sendException( new OWSException( "The REQUEST parameter is missing.", OPERATION_NOT_SUPPORTED ), response );
            return;
        }

        try {
            handleRequest( req, response, map, version );
        } catch ( OWSException e ) {
            LOG.debug( "The response is an exception with the message '{}'", e.getLocalizedMessage() );
            LOG.trace( "Stack trace of OWSException being sent", e );

            sendException( e, response );
        }
    }

    @Override
    public void doXML( XMLStreamReader xmlStream, HttpServletRequest request, HttpResponseBuffer response,
                       List<FileItem> multiParts )
                            throws ServletException, IOException, SecurityException {
        OWSException ex = new OWSException( "XML support is not implemented for WMTS.",
                                            OWSException.OPERATION_NOT_SUPPORTED );
        sendException( ex, response );
    }

    @Override
    public void destroy() {
        // anything to destroy?
    }

    private void sendException( OWSException e, HttpResponseBuffer response )
                            throws ServletException {
        sendException( null, new OWSException110XMLAdapter(), e, response );
    }

    private void handleRequest( WMTSRequestType req, HttpResponseBuffer response, Map<String, String> map,
                                Version version )
                            throws OWSException, ServletException {
        switch ( req ) {
        case GetCapabilities:
            // GetCapabilities gc =
            GetCapabilitiesKVPParser.parse( map );
            try {
                new WMTSCapabilitiesWriter( response.getXMLWriter(), identification, provider, themes,
                                            metadataUrlTemplate ).export100();
            } catch ( Throwable e ) {
                LOG.trace( "Stack trace:", e );
                throw new OWSException( e.getMessage(), NO_APPLICABLE_CODE );
            }
            break;
        case GetFeatureInfo:
            throw new OWSException( "The GetFeatureInfo operation is not supported yet.", OPERATION_NOT_SUPPORTED );
        case GetTile:
            GetTile op = new GetTile( map );
            getTile( op, response );
            break;
        }
    }

    private void getTile( GetTile op, HttpResponseBuffer response )
                            throws OWSException, ServletException {
        TileLayer layer = layers.get( op.getLayer() );
        if ( layer == null ) {
            throw new OWSException( "Unknown layer: " + op.getLayer(), INVALID_PARAMETER_VALUE );
        }

        String format = op.getFormat();
        TileDataSet tds = layer.getTileDataSet( op.getTileMatrixSet() );

        if ( tds == null ) {
            throw new OWSException( "The layer " + op.getLayer()
                                    + " has not been configured to offer the tile matrix set " + op.getTileMatrixSet()
                                    + ".", INVALID_PARAMETER_VALUE );
        }

        if ( !tds.getNativeImageFormat().equals( format ) ) {
            throw new OWSException( "Unknown format: " + format, INVALID_PARAMETER_VALUE );
        }

        TileDataLevel level = tds.getTileDataLevel( op.getTileMatrix() );
        if ( level == null ) {
            throw new OWSException( "No tile matrix with id " + op.getTileMatrix() + " in tile matrix set "
                                    + op.getTileMatrixSet() + ".", INVALID_PARAMETER_VALUE );
        }

        Tile t = level.getTile( op.getTileCol(), op.getTileRow() );
        if ( t == null ) {
            // exception or empty tile?
            throw new OWSException( "No such tile found.", INVALID_PARAMETER_VALUE );
        }

        try {
            copy( t.getAsStream(), response.getOutputStream() );
        } catch ( Throwable e ) {
            throw new ServletException( e );
        }
    }

    /**
     * @return null, if no template is configured
     */
    public String getMetadataUrlTemplate() {
        return metadataUrlTemplate;
    }
}
