// $HeadURL$
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
package org.deegree.ogcwebservices.wms;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.graphics.legend.LegendElement;
import org.deegree.graphics.legend.LegendFactory;
import org.deegree.graphics.sld.SLDFactory;
import org.deegree.graphics.sld.StyledLayerDescriptor;
import org.deegree.graphics.sld.UserStyle;
import org.deegree.i18n.Messages;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.LegendURL;
import org.deegree.ogcwebservices.wms.configuration.AbstractDataSource;
import org.deegree.ogcwebservices.wms.configuration.ExternalDataAccessDataSource;
import org.deegree.ogcwebservices.wms.configuration.LocalWCSDataSource;
import org.deegree.ogcwebservices.wms.configuration.RemoteWCSDataSource;
import org.deegree.ogcwebservices.wms.configuration.RemoteWMSDataSource;
import org.deegree.ogcwebservices.wms.configuration.WMSConfigurationType;
import org.deegree.ogcwebservices.wms.dataaccess.ExternalDataAccess;
import org.deegree.ogcwebservices.wms.operation.GetLegendGraphic;
import org.deegree.ogcwebservices.wms.operation.GetLegendGraphicResult;
import org.deegree.ogcwebservices.wms.operation.WMSProtocolFactory;
import org.deegree.owscommon_new.DCP;
import org.deegree.owscommon_new.HTTP;
import org.deegree.owscommon_new.Operation;
import org.deegree.owscommon_new.OperationsMetadata;

/**
 * performs a GetLegendGraphic request. The capability of the deegree implementation is limited to handle requests
 * containing a named style or using the (named) styles defined in a passed or referenced SLD. featuretype and rule are
 * not supported yet.
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 1.1
 */
class GetLegendGraphicHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( GetLegendGraphicHandler.class );

    private WMSConfigurationType configuration = null;

    private StyledLayerDescriptor sld = null;

    private GetLegendGraphic request = null;

    /**
     * Creates a new GetMapHandler object.
     *
     * @param capabilities
     * @param request
     *            request to perform
     */
    public GetLegendGraphicHandler( WMSConfigurationType capabilities, GetLegendGraphic request ) {
        this.configuration = capabilities;
        this.request = request;
    }

    /**
     * performs the request and returns the result of it.
     *
     * @return the result object
     * @throws OGCWebServiceException
     */
    public GetLegendGraphicResult performGetLegendGraphic()
                            throws OGCWebServiceException {

        validate( request );
        LegendElement lege = getSymbol( request );
        BufferedImage bi = null;
        try {
            bi = lege.exportAsImage( request.getFormat() );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( getClass().getName(), e.getMessage() );
        }

        GetLegendGraphicResult res = WMSProtocolFactory.createGetLegendGraphicResponse( request, bi );

        return res;
    }

    /**
     * validates if the passed request is valid against the WMS it was sent to and the SLD maybe contained or referenced
     * in/by the request.
     *
     * @param request
     *            request to validate
     */
    private void validate( GetLegendGraphic request )
                            throws OGCWebServiceException {

        String layerName = request.getLayer();
        String style = request.getStyle();
        if ( request.getSLD() == null && request.getSLD_Body() == null ) {
            Layer layer = configuration.getLayer( layerName );
            if ( layer == null ) {
                String s = Messages.getMessage( "WMS_UNKNOWNLAYER", layerName );
                throw new LayerNotDefinedException( s );
            }
            AbstractDataSource[] ds = layer.getDataSource();
            boolean raster = true;
            for ( AbstractDataSource abstractDataSource : ds ) {
                if ( !( abstractDataSource instanceof ExternalDataAccessDataSource )
                     && !( abstractDataSource instanceof RemoteWMSDataSource )
                     && !( abstractDataSource instanceof RemoteWCSDataSource )
                     && !( abstractDataSource instanceof LocalWCSDataSource ) ) {
                    raster = false;
                    break;
                }
            }
            if ( !raster && getNamedStyle( style ) == null ) {
                String s = Messages.getMessage( "WMS_STYLENOTKNOWN", style );
                throw new StyleNotDefinedException( s );
            }
        } else {
            try {
                if ( request.getSLD() != null ) {
                    sld = SLDFactory.createSLD( request.getSLD() );
                } else {
                    sld = SLDFactory.createSLD( request.getSLD_Body() );
                }
                // check if layer and style are present
                org.deegree.graphics.sld.AbstractLayer[] sldLayers = sld.getLayers();
                boolean found = false;
                for ( int i = 0; i < sldLayers.length; i++ ) {
                    if ( layerName.equals( sldLayers[i].getName() ) ) {
                        org.deegree.graphics.sld.AbstractStyle[] sldStyles = sldLayers[i].getStyles();
                        for ( int k = 0; k < sldStyles.length; k++ ) {
                            if ( sldStyles[k].getName() != null && sldStyles[k].getName().equals( style ) ) {
                                found = true;
                                break;
                            }
                        }
                        if ( found )
                            break;
                    }
                }
                if ( !found ) {
                    String s = Messages.getMessage( "WMS_LAYERNOTKNOWN", layerName );
                    throw new OGCWebServiceException( getClass().getName(), s );
                }

            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
                String s = Messages.getMessage( "WMS_INVALIDSLDREF" );
                throw new OGCWebServiceException( getClass().getName(), s );
            }

        }

    }

    private org.deegree.ogcwebservices.wms.capabilities.Style getNamedStyle( String name ) {
        String layerName = request.getLayer();
        Layer layer = configuration.getLayer( layerName );
        org.deegree.ogcwebservices.wms.capabilities.Style[] styles = layer.getStyles();
        for ( int i = 0; i < styles.length; i++ ) {
            if ( styles[i].getName().equals( name ) ) {
                return styles[i];
            }
        }
        return null;
    }

    /**
     * @param request
     * @return the symbol
     * @throws OGCWebServiceException
     */
    private LegendElement getSymbol( GetLegendGraphic request )
                            throws OGCWebServiceException {

        LegendElement le = null;
        try {
            if ( request.getSLD() == null && request.getSLD_Body() == null ) {

                // (temporary?) workaround for remote WMS layers
                Layer layer = configuration.getLayer( request.getLayer() );
                if ( layer != null && layer.getDataSource() != null && layer.getDataSource().length > 0 ) {
                    AbstractDataSource ds = layer.getDataSource()[0];
                    if ( ds.getType() == AbstractDataSource.REMOTEWMS ) {
                        Object o = ( (RemoteWMSDataSource) ds ).getOGCWebService().doService( request );
                        if ( o instanceof GetLegendGraphicResult ) {
                            OGCWebServiceException exc = ( (GetLegendGraphicResult) o ).getException();
                            if ( exc != null ) {
                                throw exc;
                            }
                            le = new LegendFactory().createLegendElement( (BufferedImage) ( (GetLegendGraphicResult) o ).getLegendGraphic() );
                        }
                    } else if ( ds.getType() == AbstractDataSource.EXTERNALDATAACCESS ) {
                        ExternalDataAccess eda = ( (ExternalDataAccessDataSource) ds ).getExternalDataAccess();
                        le = new LegendFactory().createLegendElement( eda.perform( request ) );
                    }
                }

                if ( le == null ) {
                    le = getFromWellKnownStyle();
                }
            } else {
                le = getFromSLDStyle();
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            String s = Messages.getMessage( "WMS_LEGENDELEM" );
            throw new OGCWebServiceException( getClass().getName(), s );
        }

        return le;
    }

    /**
     * creates a LegendElement from a style known by the WMS
     */
    private LegendElement getFromWellKnownStyle()
                            throws OGCWebServiceException {

        String layerName = request.getLayer();
        String styleName = request.getStyle();
        LegendElement le = null;
        LegendFactory lf = new LegendFactory();

        try {
            // get Layer object from the WMS capabilities
            Layer layer = configuration.getLayer( layerName );
            // get the Style section from the matching the requested style
            org.deegree.ogcwebservices.wms.capabilities.Style nStyle = getNamedStyle( styleName );
            LegendURL[] lURLs = nStyle.getLegendURL();
            OperationsMetadata om = configuration.getOperationMetadata();
            Operation op = om.getOperation( new QualifiedName( "GetLegendGraphic" ) );
            URL url = null;
            if ( op != null ) {
                // TODO
                // should check if really HTTP
                List<DCP> dcpList = op.getDCP();
                HTTP http = (HTTP) dcpList.get( 0 );
                url = http.getLinks().get( 0 ).getLinkage().getHref();
                if ( lURLs[0].getOnlineResource().getHost().equals( url.getHost() )
                     && lURLs[0].getOnlineResource().toExternalForm().indexOf( "GetLegendGraphic" ) > -1 ) {
                    String s = StringTools.concat( 200, "GetLegendGraphic request ",
                                                   "to the WMS itself has been set has defined ",
                                                   "as LegendURL for layer: ", layerName );
                    LOG.logInfo( s );
                    // avoid cyclic calling of WMS
                    UserStyle style = layer.getStyle( styleName );
                    if ( style != null ) {
                        // drawing legend symbol
                        String title = configuration.getLayer( layerName ).getTitle();
                        le = lf.createLegendElement( style, request.getWidth(), request.getHeight(), title );
                    } else {
                        s = Messages.getMessage( "WMS_GENERALSTYLEERROR", styleName );
                        throw new OGCWebServiceException( getClass().getName(), s );
                    }
                } else {
                    // if a legend url is defined will be used for creating the legend
                    // symbol; otherwise it will be tried to create the legend symbol
                    // dynamicly
                    try {
                        BufferedImage bi = ImageUtils.loadImage( lURLs[0].getOnlineResource() );
                        le = lf.createLegendElement( bi );
                    } catch ( Exception e ) {
                        String s = StringTools.concat( 200, "can not open legen URL: ", lURLs[0].getOnlineResource(),
                                                       "; try to create ", "legend symbol dynamicly." );
                        LOG.logInfo( s );
                        UserStyle style = layer.getStyle( styleName );
                        if ( style != null ) {
                            // drawing legend symbol
                            String title = configuration.getLayer( layerName ).getTitle();
                            le = lf.createLegendElement( style, request.getWidth(), request.getHeight(), title );
                        } else {
                            s = Messages.getMessage( "WMS_GENERALSTYLEERROR", styleName );
                            throw new OGCWebServiceException( getClass().getName(), s );
                        }
                    }

                }
            }

        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage() );
        }
        return le;
    }

    /**
     * creates a LegendElement from a style defined in the SLD document passed/referenced by/in the request
     */
    private LegendElement getFromSLDStyle()
                            throws OGCWebServiceException {

        String layerName = request.getLayer();
        String styleName = request.getStyle();
        LegendElement le = null;
        LegendFactory lf = new LegendFactory();

        try {
            org.deegree.graphics.sld.AbstractLayer[] sldLayers = sld.getLayers();
            for ( int i = 0; i < sldLayers.length; i++ ) {
                if ( layerName.equals( sldLayers[i].getName() ) ) {
                    org.deegree.graphics.sld.AbstractStyle[] sldStyles = sldLayers[i].getStyles();
                    org.deegree.graphics.sld.AbstractStyle style = null;
                    if ( styleName == null ) {
                        style = sldStyles[0];
                    } else {
                        for ( int k = 0; k < sldStyles.length; k++ ) {
                            if ( sldStyles[k].getName().equals( styleName ) ) {
                                style = sldStyles[k];
                                break;
                            }
                        }
                    }
                    String title = configuration.getLayer( layerName ).getTitle();
                    le = lf.createLegendElement( style, request.getWidth(), request.getHeight(), title );
                }
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( StringTools.stackTraceToString( e.getStackTrace() ) );
        }

        return le;
    }

}
