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

package org.deegree.services.wms;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.deegree.commons.utils.CollectionUtils.removeDuplicates;
import static org.deegree.commons.utils.MapUtils.DEFAULT_PIXEL_SIZE;
import static org.deegree.rendering.r2d.RenderHelper.calcScaleWMS130;
import static org.deegree.rendering.r2d.context.MapOptionsHelper.insertMissingOptions;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;

import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Features;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.OperatorFilter;
import org.deegree.layer.LayerData;
import org.deegree.layer.LayerQuery;
import org.deegree.layer.LayerRef;
import org.deegree.protocol.wms.WMSException.InvalidDimensionValue;
import org.deegree.protocol.wms.WMSException.MissingDimensionValue;
import org.deegree.protocol.wms.filter.ScaleFunction;
import org.deegree.protocol.wms.ops.GetFeatureInfoSchema;
import org.deegree.protocol.wms.ops.GetLegendGraphic;
import org.deegree.rendering.r2d.context.MapOptions;
import org.deegree.rendering.r2d.context.MapOptions.Antialias;
import org.deegree.rendering.r2d.context.MapOptions.Interpolation;
import org.deegree.rendering.r2d.context.MapOptions.Quality;
import org.deegree.rendering.r2d.context.MapOptionsMaps;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.services.jaxb.wms.ServiceConfigurationType;
import org.deegree.services.wms.controller.ops.GetFeatureInfo;
import org.deegree.services.wms.controller.ops.GetMap;
import org.deegree.services.wms.dynamic.LayerUpdater;
import org.deegree.services.wms.model.layers.EmptyLayer;
import org.deegree.services.wms.model.layers.Layer;
import org.deegree.style.StyleRef;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.style.utils.ImageUtils;
import org.deegree.theme.Theme;
import org.deegree.theme.Themes;
import org.deegree.theme.persistence.ThemeManager;
import org.slf4j.Logger;

/**
 * <code>MapService</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(error = "logs errors when querying feature stores/evaluating filter encoding expressions", trace = "logs stack traces", warn = "logs problems when loading layers, also invalid values for vendor specific parameters such as ANTIALIAS, QUALITY etc.", debug = "logs if layers are skipped because of scale constraints, and info about feature store queries")
public class MapService {

    private static final Logger LOG = getLogger( MapService.class );

    /**
     *
     */
    public HashMap<String, Layer> layers;

    private Layer root;

    /**
     *
     */
    public StyleRegistry registry;

    MapOptionsMaps layerOptions = new MapOptionsMaps();

    MapOptions defaultLayerOptions;

    private final LinkedList<LayerUpdater> dynamics = new LinkedList<LayerUpdater>();

    /**
     * The current update sequence.
     */
    public int updateSequence = 0; // TODO how to restore this after restart?

    private Timer styleUpdateTimer;

    private List<Theme> themes;

    private HashMap<String, org.deegree.layer.Layer> newLayers;

    HashMap<String, Theme> themeMap;

    private final GetLegendHandler getLegendHandler;

    private final OldStyleMapService oldStyleMapService;

    /**
     * @param conf
     * @param adapter
     * @throws MalformedURLException
     */
    public MapService( ServiceConfigurationType conf, XMLAdapter adapter, DeegreeWorkspace workspace )
                            throws MalformedURLException {
        this.registry = new StyleRegistry( workspace );
        layers = new HashMap<String, Layer>();

        MapServiceBuilder builder = new MapServiceBuilder( conf, adapter, layerOptions, this, workspace, dynamics );

        defaultLayerOptions = builder.buildMapOptions();

        if ( conf != null && conf.getAbstractLayer() != null ) {
            root = builder.parseLayers();
            fillInheritedInformation( root, new LinkedList<ICRS>( root.getSrs() ) );
            // update the dynamic layers once on startup to avoid having a disappointingly long initial GetCapabilities
            // request...
            update();
            styleUpdateTimer = new Timer();
            styleUpdateTimer.schedule( registry, 0, 1000 );
        }
        if ( conf != null && conf.getThemeId() != null && !conf.getThemeId().isEmpty() ) {
            ThemeManager mgr = workspace.getSubsystemManager( ThemeManager.class );
            themes = new ArrayList<Theme>();
            newLayers = new HashMap<String, org.deegree.layer.Layer>();
            themeMap = new HashMap<String, Theme>();
            for ( String id : conf.getThemeId() ) {
                Theme thm = mgr.get( id );
                if ( thm == null ) {
                    LOG.warn( "Theme with id {} was not available.", id );
                } else {
                    themes.add( thm );
                    themeMap.put( thm.getMetadata().getName(), thm );

                    for ( org.deegree.layer.Layer l : Themes.getAllLayers( thm ) ) {
                        newLayers.put( l.getMetadata().getName(), l );
                    }
                    for ( Theme theme : Themes.getAllThemes( thm ) ) {
                        themeMap.put( theme.getMetadata().getName(), theme );
                    }
                }
            }
        }
        getLegendHandler = new GetLegendHandler( this );
        oldStyleMapService = new OldStyleMapService( this );
    }

    /**
     * Empty map service with an empty root layer.
     */
    public MapService( DeegreeWorkspace workspace ) {
        this.registry = new StyleRegistry( workspace );
        this.defaultLayerOptions = new MapOptions( Quality.NORMAL, Interpolation.NEARESTNEIGHBOR, Antialias.BOTH, -1, 3 );
        layers = new HashMap<String, Layer>();
        root = new EmptyLayer( this, null, "Root Layer", null );
        getLegendHandler = new GetLegendHandler( this );
        oldStyleMapService = new OldStyleMapService( this );
    }

    /**
     * @return the list of themes if configuration is based on themes, else null
     */
    public List<Theme> getThemes() {
        return themes;
    }

    /**
     * @return true, if configuration is based on themes
     */
    public boolean isNewStyle() {
        return themes != null;
    }

    /**
     * @param layer
     * @param srs
     */
    public static void fillInheritedInformation( Layer layer, List<ICRS> srs ) {
        if ( layer.getParent() == null ) {
            if ( layer.getScaleHint() == null ) {
                layer.setScaleHint( new DoublePair( NEGATIVE_INFINITY, POSITIVE_INFINITY ) );
            }
        }

        for ( Layer l : layer.getChildren() ) {
            List<ICRS> curSrs = new LinkedList<ICRS>( srs );
            curSrs.addAll( l.getSrs() );
            removeDuplicates( curSrs );
            l.setSrs( curSrs );
            if ( l.getScaleHint() == null ) {
                l.setScaleHint( layer.getScaleHint() );
            }

            fillInheritedInformation( l, curSrs );
        }
    }

    /**
     * Updates any dynamic layers.
     */
    public synchronized void update() {
        boolean changed = false;
        for ( LayerUpdater u : dynamics ) {
            changed |= u.update();
        }
        if ( changed ) {
            ++updateSequence;
        }
    }

    /**
     * @return the dynamic layer updaters for this map service
     */
    public ArrayList<LayerUpdater> getDynamics() {
        return new ArrayList<LayerUpdater>( dynamics );
    }

    /**
     * @return the root layer
     */
    public Layer getRootLayer() {
        return root;
    }

    /**
     * @param name
     * @return the named layer, or null
     */
    public Layer getLayer( String name ) {
        return layers.get( name );
    }

    /**
     * @param req
     *            should be a GetMap or GetLegendGraphic
     * @return an empty image conforming to the request parameters
     */
    public static BufferedImage prepareImage( Object req ) {
        String format = null;
        int width = 0, height = 0;
        Color bgcolor = null;
        boolean transparent = false;
        if ( req instanceof GetMap ) {
            GetMap gm = (GetMap) req;
            format = gm.getFormat();
            width = gm.getWidth();
            height = gm.getHeight();
            transparent = gm.getTransparent();
            bgcolor = gm.getBgColor();
        } else if ( req instanceof GetLegendGraphic ) {
            GetLegendGraphic glg = (GetLegendGraphic) req;
            format = glg.getFormat();
            width = glg.getWidth();
            height = glg.getHeight();
            transparent = true;
        } else {
            return null;
        }
        return ImageUtils.prepareImage( format, width, height, transparent, bgcolor );
    }

    public boolean hasTheme( String name ) {
        return themeMap.get( name ) != null;
    }

    public void getMap( org.deegree.protocol.wms.ops.GetMap gm, List<String> headers, RenderContext ctx )
                            throws OWSException {
        Iterator<StyleRef> styleItr = gm.getStyles().iterator();
        MapOptionsMaps options = gm.getRenderingOptions();
        List<MapOptions> mapOptions = new ArrayList<MapOptions>();
        List<LayerData> list = new ArrayList<LayerData>();

        double scale = gm.getScale();

        List<LayerQuery> queries = new ArrayList<LayerQuery>();

        Iterator<LayerRef> layerItr = gm.getLayers().iterator();
        List<OperatorFilter> filters = gm.getFilters();
        Iterator<OperatorFilter> filterItr = filters == null ? null : filters.iterator();
        while ( layerItr.hasNext() ) {
            LayerRef lr = layerItr.next();
            StyleRef sr = styleItr.next();
            OperatorFilter f = filterItr == null ? null : filterItr.next();

            LayerQuery query = buildQuery( sr, lr, options, mapOptions, f, gm );
            queries.add( query );
        }

        ListIterator<LayerQuery> queryIter = queries.listIterator();

        ScaleFunction.getCurrentScaleValue().set( scale );

        for ( LayerRef lr : gm.getLayers() ) {
            LayerQuery query = queryIter.next();
            for ( org.deegree.layer.Layer l : Themes.getAllLayers( themeMap.get( lr.getName() ) ) ) {
                if ( l.getMetadata().getScaleDenominators().first > scale
                     || l.getMetadata().getScaleDenominators().second < scale ) {
                    continue;
                }
                if ( l.getMetadata().getStyles().containsKey( query.getStyle().getName() ) ) {
                    list.add( l.mapQuery( query, headers ) );
                }
            }
        }
        Iterator<MapOptions> optIter = mapOptions.iterator();
        for ( LayerData d : list ) {
            ctx.applyOptions( optIter.next() );
            d.render( ctx );
        }

        ScaleFunction.getCurrentScaleValue().remove();
    }

    private LayerQuery buildQuery( StyleRef style, LayerRef lr, MapOptionsMaps options, List<MapOptions> mapOptions,
                                   OperatorFilter f, org.deegree.protocol.wms.ops.GetMap gm ) {

        for ( org.deegree.layer.Layer l : Themes.getAllLayers( themeMap.get( lr.getName() ) ) ) {
            insertMissingOptions( l.getMetadata().getName(), options, l.getMetadata().getMapOptions(),
                                  defaultLayerOptions );
            mapOptions.add( options.get( l.getMetadata().getName() ) );
        }

        LayerQuery query = new LayerQuery( gm.getBoundingBox(), gm.getWidth(), gm.getHeight(), style, f,
                                           gm.getParameterMap(), gm.getDimensions(), gm.getPixelSize(), options,
                                           gm.getQueryBox() );
        return query;
    }

    public FeatureCollection getFeatures( org.deegree.protocol.wms.ops.GetFeatureInfo gfi, List<String> headers )
                            throws OWSException {
        List<LayerQuery> queries = prepareGetFeatures( gfi );
        List<LayerData> list = new ArrayList<LayerData>();

        double scale = calcScaleWMS130( gfi.getWidth(), gfi.getHeight(), gfi.getEnvelope(), gfi.getCoordinateSystem(),
                                        DEFAULT_PIXEL_SIZE );

        ListIterator<LayerQuery> queryIter = queries.listIterator();
        for ( LayerRef n : gfi.getQueryLayers() ) {
            LayerQuery query = queryIter.next();
            for ( org.deegree.layer.Layer l : Themes.getAllLayers( themeMap.get( n.getName() ) ) ) {
                if ( l.getMetadata().getScaleDenominators().first > scale
                     || l.getMetadata().getScaleDenominators().second < scale ) {
                    continue;
                }
                list.add( l.infoQuery( query, headers ) );
            }
        }

        List<Feature> feats = new ArrayList<Feature>( gfi.getFeatureCount() );
        for ( LayerData d : list ) {
            FeatureCollection col = d.info();
            if ( col != null ) {
                feats.addAll( col );
            }
        }

        feats = Features.clearDuplicates( feats );

        if ( feats.size() > gfi.getFeatureCount() ) {
            feats = feats.subList( 0, gfi.getFeatureCount() );
        }
        GenericFeatureCollection col = new GenericFeatureCollection();
        col.addAll( feats );
        return col;
    }

    private List<LayerQuery> prepareGetFeatures( org.deegree.protocol.wms.ops.GetFeatureInfo gfi ) {
        List<LayerQuery> queries = new ArrayList<LayerQuery>();

        Iterator<LayerRef> layerItr = gfi.getQueryLayers().iterator();
        Iterator<StyleRef> styleItr = gfi.getStyles().iterator();
        List<OperatorFilter> filters = gfi.getFilters();
        Iterator<OperatorFilter> filterItr = filters == null ? null : filters.iterator();
        while ( layerItr.hasNext() ) {
            LayerRef lr = layerItr.next();
            StyleRef sr = styleItr.next();
            OperatorFilter f = filterItr == null ? null : filterItr.next();
            int layerRadius = 0;
            for ( org.deegree.layer.Layer l : Themes.getAllLayers( themeMap.get( lr.getName() ) ) ) {
                if ( l.getMetadata().getMapOptions() != null
                     && l.getMetadata().getMapOptions().getFeatureInfoRadius() != 1 ) {
                    layerRadius = l.getMetadata().getMapOptions().getFeatureInfoRadius();
                } else {
                    layerRadius = defaultLayerOptions.getFeatureInfoRadius();
                }
            }
            LayerQuery query = new LayerQuery( gfi.getEnvelope(), gfi.getWidth(), gfi.getHeight(), gfi.getX(),
                                               gfi.getY(), gfi.getFeatureCount(), f, sr, gfi.getParameterMap(),
                                               gfi.getDimensions(), new MapOptionsMaps(), gfi.getEnvelope(), layerRadius );
            queries.add( query );
        }
        return queries;
    }

    /**
     * @param gm
     * @return a rendered image, containing the requested maps
     * @throws InvalidDimensionValue
     * @throws MissingDimensionValue
     */
    public Pair<BufferedImage, LinkedList<String>> getMapImage( GetMap gm )
                            throws MissingDimensionValue, InvalidDimensionValue {
        return oldStyleMapService.getMapImage( gm );
    }

    /**
     * @param fi
     * @return a collection of feature values for the selected area, and warning headers
     * @throws InvalidDimensionValue
     * @throws MissingDimensionValue
     */
    public Pair<FeatureCollection, LinkedList<String>> getFeatures( GetFeatureInfo fi )
                            throws MissingDimensionValue, InvalidDimensionValue {
        return oldStyleMapService.getFeatures( fi );
    }

    private void getFeatureTypes( Collection<FeatureType> types, Layer l ) {
        FeatureType type = l.getFeatureType();
        if ( type != null ) {
            types.add( type );
        }
        for ( Layer c : l.getChildren() ) {
            getFeatureTypes( types, c );
        }
    }

    private void getFeatureTypes( Collection<FeatureType> types, String name ) {
        for ( org.deegree.layer.Layer l : Themes.getAllLayers( themeMap.get( name ) ) ) {
            types.addAll( l.getMetadata().getFeatureTypes() );
        }
    }

    /**
     * @param fis
     * @return an application schema object
     */
    public List<FeatureType> getSchema( GetFeatureInfoSchema fis ) {
        List<FeatureType> list = new LinkedList<FeatureType>();
        for ( String l : fis.getLayers() ) {
            if ( isNewStyle() ) {
                getFeatureTypes( list, l );
            } else {
                getFeatureTypes( list, layers.get( l ) );
            }
        }
        return list;
    }

    private void close( Layer l ) {
        l.close();
        for ( Layer c : l.getChildren() ) {
            close( c );
        }
    }

    /***/
    public void close() {
        if ( styleUpdateTimer != null ) {
            styleUpdateTimer.cancel();
        }
        if ( root != null ) {
            close( root );
        }
    }

    /**
     * @return the style registry
     */
    public StyleRegistry getStyles() {
        return registry;
    }

    /**
     * @param style
     * @return the optimal legend size
     */
    public Pair<Integer, Integer> getLegendSize( Style style ) {
        return getLegendHandler.getLegendSize( style );
    }

    public BufferedImage getLegend( GetLegendGraphic req ) {
        return getLegendHandler.getLegend( req );
    }

    /**
     * @return the extensions object with default extension parameter settings
     */
    public MapOptionsMaps getExtensions() {
        return layerOptions;
    }

    /**
     * @return the default feature info radius
     */
    public int getGlobalFeatureInfoRadius() {
        return defaultLayerOptions.getFeatureInfoRadius();
    }

    /**
     * @return the global max features setting
     */
    public int getGlobalMaxFeatures() {
        return defaultLayerOptions.getMaxFeatures();
    }

}
