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
package org.deegree.ogcwebservices.wms;

import static java.awt.image.BufferedImage.TYPE_BYTE_INDEXED;
import static java.util.Arrays.asList;
import static java.util.regex.Pattern.compile;
import static javax.media.jai.operator.ColorQuantizerDescriptor.MEDIANCUT;
import static org.deegree.crs.coordinatesystems.GeographicCRS.WGS84;
import static org.deegree.framework.util.CollectionUtils.find;
import static org.deegree.i18n.Messages.get;
import static org.deegree.ogcwebservices.wms.operation.WMSProtocolFactory.createGetMapResponse;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.regex.Pattern;

import javax.media.jai.RenderedOp;
import javax.media.jai.operator.BandSelectDescriptor;
import javax.media.jai.operator.ColorQuantizerDescriptor;

import org.apache.batik.svggen.SVGGraphics2D;
import org.deegree.framework.concurrent.ExecutionFinishedEvent;
import org.deegree.framework.concurrent.Executor;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.MapUtils;
import org.deegree.framework.util.MimeTypeMapper;
import org.deegree.framework.util.CollectionUtils.Predicate;
import org.deegree.graphics.MapFactory;
import org.deegree.graphics.Theme;
import org.deegree.graphics.optimizers.LabelOptimizer;
import org.deegree.graphics.sld.AbstractLayer;
import org.deegree.graphics.sld.AbstractStyle;
import org.deegree.graphics.sld.NamedLayer;
import org.deegree.graphics.sld.NamedStyle;
import org.deegree.graphics.sld.StyledLayerDescriptor;
import org.deegree.graphics.sld.UserLayer;
import org.deegree.graphics.sld.UserStyle;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcbase.InvalidSRSException;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceResponse;
import org.deegree.ogcwebservices.wms.GetMapServiceInvokerForNL.WMSExceptionFromWCS;
import org.deegree.ogcwebservices.wms.capabilities.ScaleHint;
import org.deegree.ogcwebservices.wms.configuration.AbstractDataSource;
import org.deegree.ogcwebservices.wms.configuration.DatabaseDataSource;
import org.deegree.ogcwebservices.wms.configuration.WMSConfigurationType;
import org.deegree.ogcwebservices.wms.configuration.WMSConfiguration_1_3_0;
import org.deegree.ogcwebservices.wms.configuration.WMSDeegreeParams;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.ogcwebservices.wms.operation.GetMapResult;
import org.deegree.ogcwebservices.wms.operation.WMSProtocolFactory;
import org.deegree.ogcwebservices.wms.operation.GetMap.Layer;
import org.w3c.dom.Element;

/**
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class DefaultGetMapHandler implements GetMapHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( DefaultGetMapHandler.class );

    private GetMap request = null;

    // private Object[] themes = null;

    private double scale = 0;

    private CoordinateSystem reqCRS = null;

    private WMSConfigurationType configuration = null;

    private BufferedImage copyrightImg = null;

    boolean version130 = false;

    HashMap<String, String> sqls;

    private GeoTransformer transformToWGS84;

    // could be improved, I'm sure
    private static final Pattern CHECKSQL = compile( "(^insert|^update|^delete|.*[ ()]insert[() ]|.*[() ]update[() ]|.*[() ]delete[() ]).*" );

    /**
     * Creates a new GetMapHandler object.
     *
     * @param configuration
     * @param request
     *            request to perform
     */
    public DefaultGetMapHandler( WMSConfigurationType configuration, GetMap request ) {
        this.request = request;
        this.configuration = configuration;

        try {
            // get copyright image if possible
            copyrightImg = ImageUtils.loadImage( configuration.getDeegreeParams().getCopyRight() );
        } catch ( Exception e ) {
            // don't use copyright
        }

    }

    /**
     * returns the configuration used by the handler
     *
     * @return the configuration document
     */
    public WMSConfigurationType getConfiguration() {
        return configuration;
    }

    /**
     * performs a GetMap request and returns the result encapsulated within a <tt>GetMapResult</tt> object.
     * <p>
     * The method throws an WebServiceException that only shall be thrown if an fatal error occurs that makes it
     * impossible to return a result. If something went wrong performing the request (none fatal error) The exception
     * shall be encapsulated within the response object to be returned to the client as requested (GetMap-Request
     * EXCEPTION-Parameter).
     *
     * @return response to the GetMap response
     */
    public OGCWebServiceResponse performGetMap()
                            throws OGCWebServiceException {

        // get templates, check templates
        String sqltemplates = request.getVendorSpecificParameter( "SQLTEMPLATES" );
        if ( sqltemplates != null ) {
            LinkedList<String> sqls = new LinkedList<String>();
            sqls.addAll( asList( sqltemplates.split( ";" ) ) );
            if ( sqls.size() != request.getLayers().length ) {
                throw new InvalidParameterValueException( get( "WMS_INVALID_SQL_TEMPLATE_NUMBER" ) );
            }
            this.sqls = new HashMap<String, String>( sqls.size() );
            for ( int i = 0; i < request.getLayers().length; ++i ) {
                String sql = sqls.peek();
                if ( !sql.equals( "default" ) ) {
                    if ( CHECKSQL.matcher( sql.toLowerCase() ).matches() ) {
                        throw new InvalidParameterValueException(
                                                                  get( "WMS_INVALID_SQL_TEMPLATE_NO_TRANSACTION_PLEASE" ) );
                    }
                    String name = request.getLayers()[i].getName();
                    // ok iff all database data sources have custom sql allowed
                    for ( AbstractDataSource ds : configuration.getLayer( name ).getDataSource() ) {
                        if ( ds instanceof DatabaseDataSource ) {
                            if ( !( (DatabaseDataSource) ds ).isCustomSQLAllowed() ) {
                                throw new InvalidParameterValueException(
                                                                          get(
                                                                               "WMS_SQL_TEMPLATE_NOT_ALLOWED_FOR_LAYER",
                                                                               name ) );
                            }
                        }
                    }

                    this.sqls.put( name, sqls.poll() );
                }
            }
        }

        List<Callable<Object>> themes = constructThemes();

        Executor executor = Executor.getInstance();
        try {
            List<ExecutionFinishedEvent<Object>> results;
            results = executor.performSynchronously( themes, configuration.getDeegreeParams().getRequestTimeLimit() );

            GetMapResult res = renderMap( results );
            return res;
        } catch ( InterruptedException e ) {
            LOG.logError( e.getMessage(), e );
            String s = Messages.getMessage( "WMS_WAITING" );
            throw new OGCWebServiceException( getClass().getName(), s );
        }
    }

    /**
     * @return a list of callables that construct the maps to be painted
     * @throws OGCWebServiceException
     */
    public List<Callable<Object>> constructThemes()
                            throws OGCWebServiceException {
        // some initialization is done here because the constructor is called by reflection
        // and the exceptions won't be properly handled in that case
        if ( reqCRS == null ) {
            try {
                reqCRS = CRSFactory.create( request.getSrs().toLowerCase() );
            } catch ( Exception e ) {
                throw new InvalidSRSException( Messages.getMessage( "WMS_UNKNOWN_CRS", request.getSrs() ) );
            }
        }

        version130 = "1.3.0".equals( request.getVersion() );

        // exceeds the max allowed map width ?
        int maxWidth = configuration.getDeegreeParams().getMaxMapWidth();
        if ( ( maxWidth != 0 ) && ( request.getWidth() > maxWidth ) ) {
            throw new InconsistentRequestException( Messages.getMessage( "WMS_EXCEEDS_WIDTH", new Integer( maxWidth ) ) );
        }

        // exceeds the max allowed map height ?
        int maxHeight = configuration.getDeegreeParams().getMaxMapHeight();
        if ( ( maxHeight != 0 ) && ( request.getHeight() > maxHeight ) ) {
            throw new InconsistentRequestException(
                                                    Messages.getMessage( "WMS_EXCEEDS_HEIGHT", new Integer( maxHeight ) ) );
        }

        try {
            double pixelSize = 1;
            if ( version130 ) {
                // required because for WMS 1.3.0 'scale' represents the ScaleDenominator
                // and for WMS < 1.3.0 it represents the size of a pixel diagonal in meter
                pixelSize = MapUtils.DEFAULT_PIXEL_SIZE;
            }

            scale = MapUtils.calcScale( request.getWidth(), request.getHeight(), request.getBoundingBox(), reqCRS,
                                        pixelSize );

            LOG.logInfo( "OGC WMS scale: " + scale );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( Messages.getMessage( "WMS_SCALECALC" ) );
        }

        Layer[] ls = request.getLayers();

        // if 1.3.0, check for maximum allowed layers
        if ( version130 ) {
            WMSConfiguration_1_3_0 cfg = (WMSConfiguration_1_3_0) configuration;
            if ( ls.length > cfg.getLayerLimit() ) {
                String ms = Messages.getMessage( "WMS_EXCEEDS_NUMBER", new Integer( cfg.getLayerLimit() ) );
                throw new InconsistentRequestException( ms );
            }
        }

        Layer[] oldLayers = ls;
        ls = validateLayers( ls );

        LOG.logDebug( "Validated " + ls.length + " layers." );

        StyledLayerDescriptor sld = toSLD( oldLayers, request.getStyledLayerDescriptor() );

        AbstractLayer[] layers = sld.getLayers();

        LOG.logDebug( "After SLD consideration, found " + layers.length + " layers." );

        Envelope wgs84bbox = request.getBoundingBox();
        if ( !request.getSrs().equalsIgnoreCase( "EPSG:4326" ) ) {
            // transform the bounding box of the request to EPSG:4326
            transformToWGS84 = new GeoTransformer( CRSFactory.create( WGS84 ) );
            try {
                wgs84bbox = transformToWGS84.transform( wgs84bbox, reqCRS );
            } catch ( Exception e ) {
                // should never happen
                LOG.logError( "Could not validate WMS datasource area", e );
            }

        }

        List<Callable<Object>> themes = new LinkedList<Callable<Object>>();
        for ( int i = 0; i < layers.length; i++ ) {

            if ( layers[i] instanceof NamedLayer ) {
                String styleName = null;
                if ( i < request.getLayers().length ) {
                    styleName = request.getLayers()[i].getStyleName();
                }
                invokeNamedLayer( layers[i], styleName, themes, wgs84bbox );
            } else {
                double sc = scale;
                if ( !version130 ) {
                    // required because for WMS 1.3.0 'scale' represents the ScaleDenominator
                    // and for WMS < 1.3.0 it represents the size of a pixel diagonal in meter
                    sc = scale / MapUtils.DEFAULT_PIXEL_SIZE;
                }
                themes.add( new GetMapServiceInvokerForUL( this, (UserLayer) layers[i], sc ) );
            }
        }

        return themes;
    }

    /**
     * this methods validates layer in two ways:<br>
     * a) are layers available from the current WMS<br>
     * b) If a layer is selected that includes other layers determine all its sublayers having <Name>s and return them
     * instead
     *
     * @param ls
     * @return the layers
     * @throws LayerNotDefinedException
     * @throws InvalidSRSException
     */
    private Layer[] validateLayers( Layer[] ls )
                            throws LayerNotDefinedException, InvalidSRSException {

        List<Layer> layer = new ArrayList<Layer>( ls.length );
        for ( int i = 0; i < ls.length; i++ ) {
            org.deegree.ogcwebservices.wms.capabilities.Layer l = configuration.getLayer( ls[i].getName() );

            if ( l == null ) {
                throw new LayerNotDefinedException( Messages.getMessage( "WMS_UNKNOWNLAYER", ls[i].getName() ) );
            }

            validateSRS( l.getSrs(), ls[i].getName() );

            layer.add( ls[i] );
            if ( l.getLayer() != null ) {
                layer = addNestedLayers( l.getLayer(), ls[i].getStyleName(), layer );
            }
        }

        return layer.toArray( new Layer[layer.size()] );
    }

    /**
     * adds all direct and none direct sub-layers of the passed WMS capabilities layer as
     *
     * @see GetMap.Layer to the passed list.
     * @param list
     * @return all sublayers
     * @throws InvalidSRSException
     */
    private List<Layer> addNestedLayers( org.deegree.ogcwebservices.wms.capabilities.Layer[] ll, String styleName,
                                         List<Layer> list )
                            throws InvalidSRSException {

        for ( int j = 0; j < ll.length; j++ ) {
            if ( ll[j].getName() != null ) {
                String name = ll[j].getName();
                validateSRS( ll[j].getSrs(), name );
                list.add( GetMap.createLayer( name, styleName ) );
            }
            if ( ll[j].getLayer() != null ) {
                list = addNestedLayers( ll[j].getLayer(), styleName, list );
            }

        }
        return list;
    }

    /**
     * throws an exception if the requested SRS is not be supported by the passed layer (name)
     *
     * @param srs
     * @param name
     * @throws InvalidSRSException
     */
    private void validateSRS( String[] srs, String name )
                            throws InvalidSRSException {
        boolean validSRS = false;
        for ( int k = 0; k < srs.length; k++ ) {
            validSRS = srs[k].equalsIgnoreCase( reqCRS.getIdentifier() );
            if ( validSRS )
                break;
        }
        if ( !validSRS ) {
            String s = Messages.getMessage( "WMS_UNKNOWN_CRS_FOR_LAYER", reqCRS.getIdentifier(), name );
            throw new InvalidSRSException( s );
        }
    }

    private void invokeNamedLayer( AbstractLayer layer, String styleName, List<Callable<Object>> tasks,
                                   Envelope wgs84bbox )
                            throws OGCWebServiceException {
        org.deegree.ogcwebservices.wms.capabilities.Layer lay = configuration.getLayer( layer.getName() );

        LOG.logDebug( "Invoked layer " + layer.getName() );
        if ( validate( lay, layer.getName(), wgs84bbox ) ) {
            UserStyle us = getStyles( (NamedLayer) layer, styleName );
            AbstractDataSource[] ds = lay.getDataSource();

            if ( ds.length == 0 ) {
                LOG.logDebug( "No datasources for layer " + layer.getName() );
            } else {
                for ( int j = 0; j < ds.length; j++ ) {

                    LOG.logDebug( "Invoked datasource " + ds[j].getClass() + " for layer " + layer.getName() );

                    ScaleHint scaleHint = ds[j].getScaleHint();
                    if ( scale >= scaleHint.getMin() && scale < scaleHint.getMax()
                         && isValidArea( ds[j].getValidArea() ) ) {
                        double sc = scale;
                        if ( !version130 ) {
                            // required because for WMS 1.3.0 'scale' represents the
                            // ScaleDenominator
                            // and for WMS < 1.3.0 it represents the size of a pixel diagonal in
                            // meter
                            sc = scale / MapUtils.DEFAULT_PIXEL_SIZE;
                        }
                        GetMapServiceInvokerForNL si = new GetMapServiceInvokerForNL( this, (NamedLayer) layer, ds[j],
                                                                                      us, sc );
                        tasks.add( si );
                    } else {
                        LOG.logDebug( "Not showing layer " + layer.getName() + " due to scale" );
                    }
                }
            }
        } else {
            // using side effects for everything is great:
            // when layers are eg. out of the bounding box, the use of invalid styles was not checked
            // so let's do it here...
            getStyles( (NamedLayer) layer, styleName );
        }
    }

    /**
     * returns true if the requested boundingbox intersects with the valid area of a datasource
     *
     * @param validArea
     */
    private boolean isValidArea( Geometry validArea ) {

        if ( validArea != null ) {
            try {
                Envelope env = request.getBoundingBox();
                Geometry geom = GeometryFactory.createSurface( env, reqCRS );
                if ( !reqCRS.getIdentifier().equals( validArea.getCoordinateSystem().getIdentifier() ) ) {
                    // if requested CRS is not identical to the CRS of the valid area
                    // a transformation must be performed before intersection can
                    // be checked
                    GeoTransformer gt = new GeoTransformer( validArea.getCoordinateSystem() );
                    geom = gt.transform( geom );
                }
                return geom.intersects( validArea );
            } catch ( Exception e ) {
                // should never happen
                LOG.logError( "Could not validate WMS datasource area", e );
            }
        }
        return true;
    }

    /**
     * creates a StyledLayerDocument containing all requested layer, nested layers if required and assigend styles. Not
     * considered are nested layers for mixed requests (LAYERS- and SLD(_BODY)- parameter has been defined)
     *
     * @param layers
     * @param inSLD
     * @return a combined SLD object
     * @throws InvalidSRSException
     */
    private StyledLayerDescriptor toSLD( GetMap.Layer[] layers, StyledLayerDescriptor inSLD )
                            throws InvalidSRSException {
        StyledLayerDescriptor sld = null;

        if ( layers != null && layers.length > 0 && inSLD == null ) {
            // if just a list of layers has been requested

            // create a SLD from the requested LAYERS and assigned STYLES
            List<AbstractLayer> al = new ArrayList<AbstractLayer>( layers.length * 2 );
            for ( int i = 0; i < layers.length; i++ ) {
                AbstractStyle[] as = new AbstractStyle[] { new NamedStyle( layers[i].getStyleName() ) };
                al.add( new NamedLayer( layers[i].getName(), null, as ) );

                // collect all named nested layers
                org.deegree.ogcwebservices.wms.capabilities.Layer lla;
                lla = configuration.getLayer( layers[i].getName() );
                List<GetMap.Layer> list = new ArrayList<GetMap.Layer>();
                addNestedLayers( lla.getLayer(), layers[i].getStyleName(), list );

                // add nested layers to list of layers to be handled
                for ( int j = 0; j < list.size(); j++ ) {
                    GetMap.Layer nestedLayer = list.get( j );
                    as = new AbstractStyle[] { new NamedStyle( nestedLayer.getStyleName() ) };
                    al.add( new NamedLayer( nestedLayer.getName(), null, as ) );
                }
            }
            sld = new StyledLayerDescriptor( al.toArray( new AbstractLayer[al.size()] ), "1.0.0" );
        } else if ( layers != null && layers.length > 0 && inSLD != null ) {
            // if layers not null and sld is not null then SLD layers just be
            // considered if present in the layers list
            // TODO
            // layer with nested layers are not handled correctly and I think
            // it really causes a lot of problems to use them in such a way
            // because the style assigned to the mesting layer must be
            // applicable for all nested layers.
            List<String> list = new ArrayList<String>();
            for ( int i = 0; i < layers.length; i++ ) {
                list.add( layers[i].getName() );
            }

            List<AbstractLayer> newList = new ArrayList<AbstractLayer>( 20 );

            for ( final GetMap.Layer lay : layers ) {
                NamedLayer sldLay = find( inSLD.getNamedLayers(), new Predicate<NamedLayer>() {
                    public boolean eval( NamedLayer t ) {
                        return t.getName().equals( lay.getName() );
                    }
                } );

                AbstractStyle[] as;
                if ( sldLay == null ) {
                    as = new AbstractStyle[] { new NamedStyle( lay.getStyleName() ) };
                    newList.add( new NamedLayer( lay.getName(), null, as ) );
                } else {
                    newList.add( sldLay );
                }

                // finally, don't forget the user layers
                for ( UserLayer ul : inSLD.getUserLayers() ) {
                    newList.add( ul );
                }
            }

            AbstractLayer[] al = new AbstractLayer[newList.size()];
            sld = new StyledLayerDescriptor( newList.toArray( al ), inSLD.getVersion() );
        } else {
            // if no layers but a SLD is defined ...
            AbstractLayer[] as = inSLD.getLayers();
            for ( AbstractLayer l : as ) {
                addNestedLayers( l, inSLD );
            }

            sld = inSLD;
        }

        return sld;
    }

    // adds the nested layers to the sld
    private void addNestedLayers( AbstractLayer l, StyledLayerDescriptor sld ) {
        if ( !( l instanceof NamedLayer ) ) {
            return;
        }
        if ( configuration.getLayer( l.getName() ) == null ) {
            return;
        }

        org.deegree.ogcwebservices.wms.capabilities.Layer[] ls;
        ls = configuration.getLayer( l.getName() ).getLayer();
        for ( org.deegree.ogcwebservices.wms.capabilities.Layer lay : ls ) {
            NamedStyle sty = new NamedStyle( lay.getStyles()[0].getName() );
            AbstractStyle[] newSty = new AbstractStyle[] { sty };
            NamedLayer newLay = new NamedLayer( lay.getName(), null, newSty );
            sld.addLayer( newLay );
        }
    }

    /**
     * returns the <tt>UserStyle</tt>s assigned to a named layer
     *
     * @param sldLayer
     *            layer to get the styles for
     * @param styleName
     *            requested stylename (from the KVP encoding)
     */
    private UserStyle getStyles( NamedLayer sldLayer, String styleName )
                            throws OGCWebServiceException {

        AbstractStyle[] styles = sldLayer.getStyles();
        UserStyle us = null;

        // to avoid retrieving the layer again for each style
        org.deegree.ogcwebservices.wms.capabilities.Layer layer = null;
        layer = configuration.getLayer( sldLayer.getName() );
        int i = 0;
        while ( us == null && i < styles.length ) {
            if ( styles[i] instanceof NamedStyle ) {
                // styles will be taken from the WMS's style repository
                us = getPredefinedStyle( styles[i].getName(), sldLayer.getName(), layer );
            } else {
                // if the requested style fits the name of the defined style or
                // if the defined style is marked as default and the requested
                // style if 'default' the condition is true. This includes that
                // if more than one style with the same name or more than one
                // style is marked as default always the first will be choosen
                if ( styleName == null || ( styles[i].getName() != null && styles[i].getName().equals( styleName ) )
                     || ( styleName.equalsIgnoreCase( "$DEFAULT" ) && ( (UserStyle) styles[i] ).isDefault() ) ) {
                    us = (UserStyle) styles[i];
                }
            }
            i++;
        }
        if ( us == null ) {
            // this may happens if the SLD contains a named layer but not
            // a style! yes this is valid according to SLD spec 1.0.0
            us = getPredefinedStyle( styleName, sldLayer.getName(), layer );
        }
        return us;
    }

    /**
     *
     * @param styleName
     * @param layerName
     * @param layer
     * @return the style
     * @throws StyleNotDefinedException
     */
    public UserStyle getPredefinedStyle( String styleName, String layerName,
                                          org.deegree.ogcwebservices.wms.capabilities.Layer layer )
                            throws StyleNotDefinedException {
        UserStyle us = null;
        if ( "default".equals( styleName ) ) {
            us = layer.getStyle( styleName );
        }

        if ( us == null ) {
            if ( styleName == null || styleName.length() == 0 || styleName.equals( "$DEFAULT" )
                 || styleName.equals( "default" ) ) {
                styleName = "default:" + layerName;
            }
        }

        us = layer.getStyle( styleName );

        if ( us == null && !( styleName.startsWith( "default" ) ) && !( styleName.startsWith( "$DEFAULT" ) ) ) {
            String s = Messages.getMessage( "WMS_STYLENOTDEFINED", styleName, layer );
            throw new StyleNotDefinedException( s );
        }
        return us;
    }

    /**
     * validates if the requested layer matches the conditions of the request if not a <tt>WebServiceException</tt> will
     * be thrown. If the layer matches the request, but isn't able to deviever data for the requested area and/or scale
     * false will be returned. If the layer matches the request and contains data for the requested area and/or scale
     * true will be returned.
     *
     * @param layer
     *            layer as defined at the capabilities/configuration
     * @param name
     *            name of the layer (must be submitted separately because the layer parameter can be <tt>null</tt>
     * @param wgs84bbox
     *            the wgs84 bbox of the request
     */
    private boolean validate( org.deegree.ogcwebservices.wms.capabilities.Layer layer, String name, Envelope wgs84bbox )
                            throws OGCWebServiceException {

        // check if layer is available
        if ( layer == null ) {
            throw new LayerNotDefinedException( Messages.getMessage( "WMS_UNKNOWNLAYER", name ) );
        }

        // check bounding box
        try {
            Envelope layerBbox = layer.getLatLonBoundingBox();
            if ( !wgs84bbox.intersects( layerBbox ) ) {
                LOG.logDebug( "Not showing layer because the request is out of the bounding box." );
                return false;
            }

        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( Messages.getMessage( "WMS_BBOXCOMPARSION" ) );
        }

        return true;
    }

    /**
     * renders the map from the <tt>DisplayElement</tt>s
     *
     * @param results
     * @return a result object suitable for further processing
     *
     * @throws OGCWebServiceException
     */
    public GetMapResult renderMap( List<ExecutionFinishedEvent<Object>> results )
                            throws OGCWebServiceException {

        OGCWebServiceException exce = null;

        ArrayList<Object> list = new ArrayList<Object>( 50 );
        for ( ExecutionFinishedEvent<Object> evt : results ) {
            Object o = null;

            // exception handling might be handled in a better way
            try {
                o = evt.getResult();
            } catch ( CancellationException e ) {
                exce = new OGCWebServiceException( getClass().getName(), e.toString() );
            } catch ( OGCWebServiceException e ) {
                throw e;
            } catch ( Throwable e ) {
                exce = new OGCWebServiceException( getClass().getName(), e.toString() );
            }

            if ( o instanceof WMSExceptionFromWCS ) {
                if ( results.size() == 1 ) {
                    exce = ( (WMSExceptionFromWCS) o ).wrapped;
                    o = exce;
                } else {
                    o = null;
                }
            }
            if ( o instanceof Exception ) {
                exce = new OGCWebServiceException( getClass().getName(), o.toString() );
            }
            if ( o instanceof OGCWebServiceException ) {
                exce = (OGCWebServiceException) o;
                break;
            }
            if ( o != null ) {
                list.add( o );
            }
        }

        return render( list.toArray( new Theme[list.size()] ), exce );
    }

    /**
     * @param themes
     * @param exce
     * @return a result object suitable for further processing elsewhere
     * @throws InvalidSRSException
     */
    public GetMapResult render( Theme[] themes, OGCWebServiceException exce )
                            throws InvalidSRSException {
        // some initialization is done here because the constructor is called by reflection
        // and the exceptions won't be properly handled in that case
        // NOTE that it has to be repeated here in case someone wants to use this method only!
        if ( reqCRS == null ) {
            try {
                reqCRS = CRSFactory.create( request.getSrs().toLowerCase() );
            } catch ( Exception e ) {
                throw new InvalidSRSException( Messages.getMessage( "WMS_UNKNOWN_CRS", request.getSrs() ) );
            }
        }

        GetMapResult response = null;

        String mime = MimeTypeMapper.toMimeType( request.getFormat() );

        if ( configuration.getDeegreeParams().getDefaultPNGFormat() != null && mime.equalsIgnoreCase( "image/png" ) ) {
            mime = configuration.getDeegreeParams().getDefaultPNGFormat();
        }

        // get target object for rendering
        Object target = GraphicContextFactory.createGraphicTarget( mime, request.getWidth(), request.getHeight() );

        // get graphic context of the target
        Graphics g = GraphicContextFactory.createGraphicContext( mime, target );
        if ( exce == null ) {
            // only if no exception occured
            try {
                org.deegree.graphics.MapView map = null;
                if ( themes.length > 0 ) {
                    map = MapFactory.createMapView( "deegree WMS", request.getBoundingBox(), reqCRS, themes,
                                                    MapUtils.DEFAULT_PIXEL_SIZE );
                }
                g.setClip( 0, 0, request.getWidth(), request.getHeight() );

                if ( !request.getTransparency() ) {
                    if ( g instanceof Graphics2D ) {
                        // this ensures real clearing (rendering modifies the color ever so
                        // slightly)
                        ( (Graphics2D) g ).setBackground( request.getBGColor() );
                        g.clearRect( 0, 0, request.getWidth(), request.getHeight() );
                    } else {
                        g.setColor( request.getBGColor() );
                        g.fillRect( 0, 0, request.getWidth(), request.getHeight() );
                    }
                }

                if ( map != null ) {
                    Theme[] thms = map.getAllThemes();
                    map.addOptimizer( new LabelOptimizer( thms ) );
                    // antialiasing must be switched of for gif output format
                    // because the antialiasing may create more than 255 colors
                    // in the map/image, even just a few colors are defined in
                    // the styles
                    if ( !request.getFormat().equalsIgnoreCase( "image/gif" ) ) {
                        if ( configuration.getDeegreeParams().isAntiAliased() ) {
                            ( (Graphics2D) g ).setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                                                                 RenderingHints.VALUE_ANTIALIAS_ON );
                            ( (Graphics2D) g ).setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,
                                                                 RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
                        }
                    }
                    map.paint( g );
                }
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
                exce = new OGCWebServiceException( "GetMapHandler_Impl: renderMap", e.toString() );
            }
        }

        // print a copyright note at the left lower corner of the map
        printCopyright( g, request.getHeight() );

        if ( mime.equals( "image/svg+xml" ) || mime.equals( "image/svg xml" ) ) {
            Element root = ( (SVGGraphics2D) g ).getRoot();
            root.setAttribute( "xmlns:xlink", "http://www.w3.org/1999/xlink" );
            response = WMSProtocolFactory.createGetMapResponse( request, exce, root );
        } else {

            BufferedImage img = (BufferedImage) target;

            if ( mime.equals( "image/png; mode=8bit" ) ) {
                RenderedOp torgb = BandSelectDescriptor.create( img, new int[] { 0, 1, 2 }, null );

                torgb = ColorQuantizerDescriptor.create( torgb, MEDIANCUT, 254, null, null, null, null, null );

                WritableRaster data = torgb.getAsBufferedImage().getRaster();

                IndexColorModel model = (IndexColorModel) torgb.getColorModel();
                byte[] reds = new byte[256];
                byte[] greens = new byte[256];
                byte[] blues = new byte[256];
                byte[] alphas = new byte[256];
                model.getReds( reds );
                model.getGreens( greens );
                model.getBlues( blues );
                // note that this COULD BE OPTIMIZED to SUPPORT EG HALF TRANSPARENT PIXELS for PNG-8!
                // It's not true that PNG-8 does not support this! Try setting the value to eg. 128 here and see what
                // you'll get...
                for ( int i = 0; i < 254; ++i ) {
                    alphas[i] = -1;
                }
                alphas[255] = 0;
                IndexColorModel newModel = new IndexColorModel( 8, 256, reds, greens, blues, alphas );

                // yeah, double memory, but it was the only way I could find (I could be blind...)
                BufferedImage res = new BufferedImage( torgb.getWidth(), torgb.getHeight(), TYPE_BYTE_INDEXED, newModel );
                res.setData( data );

                // do it the hard way as the OR operation would destroy the channels
                for ( int y = 0; y < img.getHeight(); ++y ) {
                    for ( int x = 0; x < img.getWidth(); ++x ) {
                        if ( img.getRGB( x, y ) == 0 ) {
                            res.setRGB( x, y, 0 );
                        }
                    }
                }

                target = res;
            }

            response = createGetMapResponse( request, exce, target );
        }
        g.dispose();

        return response;
    }

    // works, but only with some bogus pixel that means "transparency"
    // private static BufferedImage makeTransparent( BufferedImage img ) {
    // IndexColorModel cm = (IndexColorModel) img.getColorModel();
    // WritableRaster raster = img.getRaster();
    // int pixel = raster.getSample( 0, 0, 0 );
    // int size = cm.getMapSize();
    // byte[] reds = new byte[size];
    // byte[] greens = new byte[size];
    // byte[] blues = new byte[size];
    // cm.getReds( reds );
    // cm.getGreens( greens );
    // cm.getBlues( blues );
    // return new BufferedImage( new IndexColorModel( 8, size, reds, greens, blues, pixel ), raster,
    // img.isAlphaPremultiplied(), null );
    // }

    /**
     * prints a copyright note at left side of the map bottom. The copyright note will be extracted from the WMS
     * capabilities/configuration
     *
     * @param g
     *            graphic context of the map
     * @param heigth
     *            height of the map in pixel
     */
    private void printCopyright( Graphics g, int heigth ) {
        WMSDeegreeParams dp = configuration.getDeegreeParams();
        String copyright = dp.getCopyRight();
        if ( copyrightImg != null ) {
            g.drawImage( copyrightImg, 8, heigth - copyrightImg.getHeight() - 5, null );
        } else {
            if ( copyright != null ) {
                g.setFont( new Font( "SANSSERIF", Font.PLAIN, 14 ) );
                g.setColor( Color.BLACK );
                g.drawString( copyright, 8, heigth - 15 );
                g.drawString( copyright, 10, heigth - 15 );
                g.drawString( copyright, 8, heigth - 13 );
                g.drawString( copyright, 10, heigth - 13 );
                g.setColor( Color.WHITE );
                g.setFont( new Font( "SANSSERIF", Font.PLAIN, 14 ) );
                g.drawString( copyright, 9, heigth - 14 );
            }
        }

    }

    /**
     * @return the request that is being handled
     */
    protected GetMap getRequest() {
        return request;
    }

    /**
     * @return the requests coordinate system
     */
    protected CoordinateSystem getRequestCRS() {
        return reqCRS;
    }

}
