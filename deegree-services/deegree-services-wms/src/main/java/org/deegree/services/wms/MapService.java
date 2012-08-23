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

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_OFF;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
import static java.awt.RenderingHints.VALUE_RENDER_DEFAULT;
import static java.awt.RenderingHints.VALUE_RENDER_QUALITY;
import static java.awt.RenderingHints.VALUE_RENDER_SPEED;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.deegree.commons.utils.CollectionUtils.AND;
import static org.deegree.commons.utils.CollectionUtils.addAllUncontained;
import static org.deegree.commons.utils.CollectionUtils.map;
import static org.deegree.commons.utils.CollectionUtils.reduce;
import static org.deegree.commons.utils.CollectionUtils.removeDuplicates;
import static org.deegree.commons.utils.MapUtils.DEFAULT_PIXEL_SIZE;
import static org.deegree.rendering.r2d.RenderHelper.calcScaleWMS130;
import static org.deegree.rendering.r2d.context.MapOptions.Antialias.BOTH;
import static org.deegree.rendering.r2d.context.MapOptions.Interpolation.NEARESTNEIGHBOR;
import static org.deegree.rendering.r2d.context.MapOptions.Quality.NORMAL;
import static org.deegree.services.wms.model.layers.Layer.render;
import static org.deegree.style.utils.ImageUtils.postprocessPng8bit;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.CollectionUtils;
import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Features;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.stream.ThreadedFeatureInputStream;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.xpath.GMLObjectXPathEvaluator;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.XPathEvaluator;
import org.deegree.layer.LayerData;
import org.deegree.layer.LayerQuery;
import org.deegree.layer.LayerRef;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.wms.WMSException.InvalidDimensionValue;
import org.deegree.protocol.wms.WMSException.MissingDimensionValue;
import org.deegree.protocol.wms.ops.GetFeatureInfoSchema;
import org.deegree.protocol.wms.ops.GetLegendGraphic;
import org.deegree.protocol.wms.ops.RequestBase;
import org.deegree.rendering.r2d.Java2DRenderer;
import org.deegree.rendering.r2d.Java2DTextRenderer;
import org.deegree.rendering.r2d.context.MapOptions;
import org.deegree.rendering.r2d.context.MapOptions.Antialias;
import org.deegree.rendering.r2d.context.MapOptions.Interpolation;
import org.deegree.rendering.r2d.context.MapOptions.Quality;
import org.deegree.rendering.r2d.context.MapOptionsMaps;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.rendering.r2d.legends.Legends;
import org.deegree.services.jaxb.wms.AbstractLayerType;
import org.deegree.services.jaxb.wms.BaseAbstractLayerType;
import org.deegree.services.jaxb.wms.DynamicLayer;
import org.deegree.services.jaxb.wms.LayerOptionsType;
import org.deegree.services.jaxb.wms.ServiceConfigurationType;
import org.deegree.services.jaxb.wms.StatisticsLayer;
import org.deegree.services.wms.controller.ops.GetFeatureInfo;
import org.deegree.services.wms.controller.ops.GetMap;
import org.deegree.services.wms.dynamic.LayerUpdater;
import org.deegree.services.wms.dynamic.PostGISUpdater;
import org.deegree.services.wms.dynamic.ShapeUpdater;
import org.deegree.services.wms.model.layers.EmptyLayer;
import org.deegree.services.wms.model.layers.FeatureLayer;
import org.deegree.services.wms.model.layers.Layer;
import org.deegree.services.wms.model.layers.RasterLayer;
import org.deegree.services.wms.model.layers.RemoteWMSLayer;
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

    private HashMap<Style, Pair<Integer, Integer>> legendSizes = new HashMap<Style, Pair<Integer, Integer>>();

    private HashMap<Style, HashMap<String, BufferedImage>> legends = new HashMap<Style, HashMap<String, BufferedImage>>();

    private MapOptionsMaps layerOptions = new MapOptionsMaps();

    private MapOptions defaultLayerOptions;

    private static int stylesCounter = 0;

    private LinkedList<LayerUpdater> dynamics = new LinkedList<LayerUpdater>();

    /**
     * The current update sequence.
     */
    public int updateSequence = 0; // TODO how to restore this after restart?

    private Timer styleUpdateTimer;

    private DeegreeWorkspace workspace;

    private List<Theme> themes;

    private HashMap<String, org.deegree.layer.Layer> newLayers;

    private HashMap<String, Theme> themeMap;

    /**
     * @param conf
     * @param adapter
     * @throws MalformedURLException
     */
    public MapService( ServiceConfigurationType conf, XMLAdapter adapter, DeegreeWorkspace workspace )
                            throws MalformedURLException {
        this.workspace = workspace;
        this.registry = new StyleRegistry( workspace );
        layers = new HashMap<String, Layer>();

        Antialias alias = Antialias.BOTH;
        Quality quali = Quality.HIGH;
        Interpolation interpol = Interpolation.NEARESTNEIGHBOUR;
        int maxFeatures = 10000;
        int featureInfoRadius = 1;
        if ( conf != null ) {
            LayerOptionsType sf = conf.getDefaultLayerOptions();
            alias = handleDefaultValue( sf == null ? null : sf.getAntiAliasing(), Antialias.class, BOTH );
            quali = handleDefaultValue( sf == null ? null : sf.getRenderingQuality(), Quality.class, NORMAL );
            interpol = handleDefaultValue( sf == null ? null : sf.getInterpolation(), Interpolation.class,
                                           NEARESTNEIGHBOR );
            if ( sf != null && sf.getMaxFeatures() != null ) {
                maxFeatures = sf.getMaxFeatures();
                LOG.debug( "Using global max features setting of {}.", maxFeatures );
            } else {
                LOG.debug( "Using default global max features setting of {}, set it to -1 if you don't want a limit.",
                           maxFeatures );
            }
            if ( sf != null && sf.getFeatureInfoRadius() != null ) {
                featureInfoRadius = sf.getFeatureInfoRadius();
                LOG.debug( "Using global feature info radius setting of {}.", featureInfoRadius );
            } else {
                LOG.debug( "Using default feature info radius of {}.", featureInfoRadius );
            }
            defaultLayerOptions = new MapOptions( quali, interpol, alias, maxFeatures, featureInfoRadius );
        }

        if ( conf != null && conf.getAbstractLayer() != null ) {
            root = parseLayer( conf.getAbstractLayer().getValue(), null, adapter, alias, interpol, quali );
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

                    for ( org.deegree.layer.Layer l : Themes.getAllLayers( thm ) ) {
                        newLayers.put( l.getMetadata().getName(), l );
                    }
                    for ( Theme theme : Themes.getAllThemes( thm ) ) {
                        themeMap.put( theme.getMetadata().getName(), theme );
                    }
                }
            }
        }
    }

    /**
     * Empty map service with an empty root layer.
     */
    public MapService( DeegreeWorkspace workspace ) {
        this.registry = new StyleRegistry( workspace );
        this.defaultLayerOptions = new MapOptions( Quality.NORMAL, Interpolation.NEARESTNEIGHBOR, Antialias.BOTH, -1, 3 );
        layers = new HashMap<String, Layer>();
        root = new EmptyLayer( this, null, "Root Layer", null );
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

    private static <T extends Enum<T>> T handleDefaultValue( String val, Class<T> enumType, T defaultValue ) {
        if ( val == null ) {
            return defaultValue;
        }
        try {
            return Enum.valueOf( enumType, val.toUpperCase() );
        } catch ( IllegalArgumentException e ) {
            LOG.warn( "'{}' is not a valid value for '{}'. Using default value '{}' instead.",
                      new Object[] { val, enumType.getSimpleName(), defaultValue } );
            return defaultValue;
        }
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

    private void addChildren( Layer parent, List<JAXBElement<? extends BaseAbstractLayerType>> layers,
                              XMLAdapter adapter, Antialias alias, Interpolation interpol, Quality quality )
                            throws MalformedURLException {
        for ( JAXBElement<? extends BaseAbstractLayerType> el : layers ) {
            BaseAbstractLayerType l = el.getValue();
            Layer parsed = parseLayer( l, parent, adapter, alias, interpol, quality );
            if ( parsed != null ) {
                parent.getChildren().add( parsed );
            }
        }

        // second run for scale hints
        double last = NEGATIVE_INFINITY;
        Iterator<Layer> iter = parent.getChildren().iterator();
        for ( JAXBElement<? extends BaseAbstractLayerType> el : layers ) {
            BaseAbstractLayerType lay = el.getValue();
            if ( !( lay instanceof AbstractLayerType ) ) {
                continue;
            }

            if ( !iter.hasNext() ) {
                LOG.warn( "The parsed layer does not have a child, but a layer was configured, bailing out." );
                break;
            }
            Layer mappedChild = iter.next();
            if ( mappedChild == null ) {
                LOG.warn( "The parsed layer does not have a child, but a layer was configured, bailing out." );
                break;
            }

            AbstractLayerType l = (AbstractLayerType) lay;
            double min = Double.NaN;
            double max = Double.NaN;
            if ( l.getScaleDenominators() != null ) {
                min = l.getScaleDenominators().getMin();
                max = l.getScaleDenominators().getMax();
                last = max;
            }
            if ( l.getScaleUntil() != null ) {
                min = last;
                max = l.getScaleUntil();
                last = max;
            }
            if ( l.getScaleAbove() != null ) {
                min = l.getScaleAbove();
                max = POSITIVE_INFINITY;
            }
            if ( !Double.isNaN( min ) && !Double.isNaN( max ) ) {
                if ( min > max ) {
                    LOG.warn( "Configured min and max scale conflict (min > max) swapping min and max." );
                    double d = max;
                    max = min;
                    min = d;
                }
                mappedChild.setScaleHint( new DoublePair( min, max ) );
            }
        }
    }

    private Layer parseLayer( BaseAbstractLayerType layer, final Layer parent, XMLAdapter adapter, Antialias alias,
                              Interpolation interpol, Quality quality )
                            throws MalformedURLException {
        Layer res = null;
        if ( layer instanceof AbstractLayerType ) {
            AbstractLayerType aLayer = (AbstractLayerType) layer;

            if ( aLayer.getFeatureStoreId() != null ) {
                try {
                    res = new FeatureLayer( this, aLayer, parent, workspace );
                } catch ( Throwable e ) {
                    LOG.warn( "Layer '{}' could not be loaded: '{}'", aLayer.getName() == null ? aLayer.getTitle()
                                                                                              : aLayer.getName(),
                              e.getLocalizedMessage() );
                    LOG.trace( "Stack trace", e );
                    return null;
                }
            } else if ( aLayer.getCoverageStoreId() != null ) {
                res = new RasterLayer( this, aLayer, parent );
            } else if ( aLayer.getRemoteWMSStoreId() != null ) {
                res = new RemoteWMSLayer( this, aLayer, parent );
            } else {
                res = new EmptyLayer( this, aLayer, parent );
            }

            if ( res.getName() != null ) {
                if ( aLayer.getDirectStyle() != null ) {
                    registry.load( res.getName(), aLayer.getDirectStyle(), adapter );
                }
                if ( aLayer.getSLDStyle() != null ) {
                    registry.load( res.getName(), adapter, aLayer.getSLDStyle() );
                }
                synchronized ( layers ) {
                    layers.put( res.getName(), res );
                }
            } else {
                String name = "NamelessLayer_" + ++stylesCounter;
                if ( aLayer.getDirectStyle() != null ) {
                    registry.load( name, aLayer.getDirectStyle(), adapter );
                }
                if ( aLayer.getSLDStyle() != null ) {
                    registry.load( name, adapter, aLayer.getSLDStyle() );
                }
                res.setInternalName( name );
            }

            // this is necessary as well as the run in addChildren (where the children can contain a mix between
            // scaledenominators and scaleabove/until)
            if ( aLayer.getScaleDenominators() != null ) {
                res.setScaleHint( new DoublePair( aLayer.getScaleDenominators().getMin(),
                                                  aLayer.getScaleDenominators().getMax() ) );
            }

            LayerOptionsType sf = aLayer.getLayerOptions();
            if ( sf != null ) {
                alias = handleDefaultValue( sf.getAntiAliasing(), Antialias.class, alias );
                quality = handleDefaultValue( sf.getRenderingQuality(), Quality.class, quality );
                interpol = handleDefaultValue( sf.getInterpolation(), Interpolation.class, interpol );
                if ( sf.getMaxFeatures() != null ) {
                    layerOptions.setMaxFeatures( res.getName(), sf.getMaxFeatures() );
                }
                if ( sf.getFeatureInfoRadius() != null ) {
                    layerOptions.setFeatureInfoRadius( res.getName(), sf.getFeatureInfoRadius() );
                }
            }
            layerOptions.setAntialias( res.getName(), alias );
            layerOptions.setQuality( res.getName(), quality );
            layerOptions.setInterpolation( res.getName(), interpol );

            addChildren( res, aLayer.getAbstractLayer(), adapter, alias, interpol, quality );
        } else if ( layer instanceof StatisticsLayer ) {
            res = new org.deegree.services.wms.model.layers.StatisticsLayer( this, parent );
            synchronized ( layers ) {
                layers.put( res.getName(), res );
            }
        } else {
            DynamicLayer dyn = (DynamicLayer) layer;
            if ( dyn.getShapefileDirectory() != null ) {
                try {
                    File shapeDir = new File( adapter.resolve( dyn.getShapefileDirectory() ).toURI() );
                    dynamics.add( new ShapeUpdater( shapeDir, parent, this ) );
                } catch ( URISyntaxException e ) {
                    LOG.error( "Dynamic shape file directory '{}' could not be resolved.", dyn.getShapefileDirectory() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( dyn.getPostGIS() != null ) {
                dynamics.add( new PostGISUpdater( dyn.getPostGIS().getValue(), dyn.getPostGIS().getSchema(), parent,
                                                  this, adapter.getSystemId(), workspace ) );
            }
        }

        return res;
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

    protected LinkedList<String> paintLayer( Layer l, Style s, Graphics2D g, GetMap gm )
                            throws MissingDimensionValue, InvalidDimensionValue {
        LinkedList<String> warnings = new LinkedList<String>();
        double scale = gm.getScale();
        DoublePair scales = l.getScaleHint();
        LOG.debug( "Scale settings are: {}, current scale is {}.", scales, scale );
        if ( scales.first > scale || scales.second < scale ) {
            LOG.debug( "Not showing layer '{}' because of its scale constraint.", l.getName() == null ? l.getTitle()
                                                                                                     : l.getName() );
            return warnings;
        }
        warnings.addAll( l.paintMap( g, gm, s ) );

        for ( Layer child : l.getChildren() ) {
            warnings.addAll( paintLayer( child, registry.get( child.getInternalName(), null ), g, gm ) );
        }
        return warnings;
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

    private static void applyHints( final String l, final Graphics2D g, MapOptionsMaps options, MapOptions defaults ) {
        Quality q = options.getQuality( l );
        if ( q == null ) {
            q = defaults.getQuality();
        }
        switch ( q ) {
        case HIGH:
            g.setRenderingHint( KEY_RENDERING, VALUE_RENDER_QUALITY );
            break;
        case LOW:
            g.setRenderingHint( KEY_RENDERING, VALUE_RENDER_SPEED );
            break;
        case NORMAL:
            g.setRenderingHint( KEY_RENDERING, VALUE_RENDER_DEFAULT );
            break;
        }
        Interpolation i = options.getInterpolation( l );
        if ( i == null ) {
            i = defaults.getInterpolation();
        }
        switch ( i ) {
        case BICUBIC:
            g.setRenderingHint( KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC );
            break;
        case BILINEAR:
            g.setRenderingHint( KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR );
            break;
        case NEARESTNEIGHBOR:
        case NEARESTNEIGHBOUR:
            g.setRenderingHint( KEY_INTERPOLATION, VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
            break;
        }
        Antialias a = options.getAntialias( l );
        if ( a == null ) {
            a = defaults.getAntialias();
        }
        switch ( a ) {
        case IMAGE:
            g.setRenderingHint( KEY_ANTIALIASING, VALUE_ANTIALIAS_ON );
            g.setRenderingHint( KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_OFF );
            break;
        case TEXT:
            g.setRenderingHint( KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF );
            g.setRenderingHint( KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON );
            break;
        case BOTH:
            g.setRenderingHint( KEY_ANTIALIASING, VALUE_ANTIALIAS_ON );
            g.setRenderingHint( KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON );
            break;
        case NONE:
            g.setRenderingHint( KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF );
            g.setRenderingHint( KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_OFF );
            break;
        }
    }

    private static Mapper<Boolean, Layer> getFeatureLayerCollector( final LinkedList<FeatureLayer> list ) {
        return new Mapper<Boolean, Layer>() {
            @Override
            public Boolean apply( Layer u ) {
                return collectFeatureLayers( u, list );
            }
        };
    }

    /**
     * @param l
     * @param list
     * @return true, if all sub layers were feature layers and its style had a distinct feature type name
     */
    static boolean collectFeatureLayers( Layer l, final LinkedList<FeatureLayer> list ) {
        if ( l instanceof FeatureLayer ) {
            list.add( (FeatureLayer) l );
            return reduce( true, map( l.getChildren(), getFeatureLayerCollector( list ) ), AND );
        }
        return false;
    }

    // must ensure that subtree consists of feature layers only
    // returns null if not all styles contain distinct feature type names
    private LinkedList<String> collectFeatureQueries( Map<FeatureStore, LinkedList<Query>> queries, FeatureLayer l,
                                                      Style style, GetMap gm, HashMap<QName, FeatureLayer> ftToLayer,
                                                      HashMap<QName, Style> ftToStyle )
                            throws MissingDimensionValue, InvalidDimensionValue {
        if ( !l.getClass().equals( FeatureLayer.class ) ) {
            return null;
        }

        LinkedList<String> warns = new LinkedList<String>();

        LinkedList<Query> list = queries.get( l.getDataStore() );
        if ( list == null ) {
            list = new LinkedList<Query>();
            queries.put( l.getDataStore(), list );
        }
        warns.addAll( l.collectQueries( style, gm, list ) );

        QName name = style == null ? null : style.getFeatureType();
        if ( name == null || ftToLayer.containsKey( name ) ) {
            return null;
        }
        ftToLayer.put( name, l );
        ftToStyle.put( name, style );

        for ( Layer child : l.getChildren() ) {
            LinkedList<String> otherWarns = collectFeatureQueries( queries, (FeatureLayer) child,
                                                                   registry.get( child.getInternalName(), null ), gm,
                                                                   ftToLayer, ftToStyle );
            if ( otherWarns == null ) {
                return null;
            }
            warns.addAll( otherWarns );
        }

        return warns;
    }

    /**
     * Paints the map on a graphics object.
     * 
     * @param g
     * @param gm
     * @param warnings
     * @throws InvalidDimensionValue
     * @throws MissingDimensionValue
     */
    public void paintMap( Graphics2D g, GetMap gm, LinkedList<String> warnings )
                            throws MissingDimensionValue, InvalidDimensionValue {
        Iterator<Layer> layers = gm.getLayers().iterator();
        Iterator<Style> styles = gm.getStyles().iterator();

        if ( reduce( true, map( gm.getLayers(), CollectionUtils.<Layer> getInstanceofMapper( FeatureLayer.class ) ),
                     AND ) ) {
            LinkedList<FeatureLayer> fls = new LinkedList<FeatureLayer>();
            Map<FeatureStore, LinkedList<Query>> queries = new HashMap<FeatureStore, LinkedList<Query>>();
            HashMap<QName, FeatureLayer> ftToLayer = new HashMap<QName, FeatureLayer>();
            HashMap<QName, Style> ftToStyle = new HashMap<QName, Style>();

            double scale = gm.getScale();
            if ( reduce( true, map( gm.getLayers(), getFeatureLayerCollector( fls ) ), AND ) ) {
                while ( layers.hasNext() ) {
                    Layer l = layers.next();
                    Style s = styles.next();
                    DoublePair scales = l.getScaleHint();
                    LOG.debug( "Scale settings are: {}, current scale is {}.", scales, scale );
                    if ( scales.first > scale || scales.second < scale ) {
                        LOG.debug( "Not showing layer '{}' because of its scale constraint.",
                                   l.getName() == null ? l.getTitle() : l.getName() );
                        continue;
                    }
                    LinkedList<String> otherWarns = collectFeatureQueries( queries, (FeatureLayer) l, s, gm, ftToLayer,
                                                                           ftToStyle );
                    if ( otherWarns == null ) {
                        queries.clear();
                        break;
                    }
                    warnings.addAll( otherWarns );
                }
            }

            if ( queries.size() == 1 ) {
                LOG.debug( "Using collected queries for better performance." );

                Java2DRenderer renderer = new Java2DRenderer( g, gm.getWidth(), gm.getHeight(), gm.getBoundingBox(),
                                                              gm.getPixelSize() );
                Java2DTextRenderer textRenderer = new Java2DTextRenderer( renderer );

                // TODO
                XPathEvaluator<?> evaluator = new GMLObjectXPathEvaluator();

                Collection<LinkedList<Query>> qs = queries.values();
                FeatureInputStream rs = null;
                try {
                    FeatureStore store = queries.keySet().iterator().next();
                    LinkedList<Query> queriesList = qs.iterator().next();
                    if ( !queriesList.isEmpty() ) {
                        rs = store.query( queriesList.toArray( new Query[queriesList.size()] ) );
                        // TODO Should this always be done on this level? What about min and maxFill values?
                        rs = new ThreadedFeatureInputStream( rs, 100, 20 );
                        for ( Feature f : rs ) {
                            QName name = f.getType().getName();
                            FeatureLayer l = ftToLayer.get( name );

                            applyHints( l.getName(), g, layerOptions, defaultLayerOptions );
                            render( f, (XPathEvaluator<Feature>) evaluator, ftToStyle.get( name ), renderer,
                                    textRenderer, gm.getScale(), gm.getResolution() );
                        }
                    } else {
                        LOG.warn( "No queries were found for the requested layers." );
                    }
                } catch ( FilterEvaluationException e ) {
                    LOG.error( "A filter could not be evaluated. The error was '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                } catch ( FeatureStoreException e ) {
                    LOG.error( "Data could not be fetched from the feature store. The error was '{}'.",
                               e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                } finally {
                    if ( rs != null ) {
                        rs.close();
                    }
                }
                return;
            }

            LOG.debug( "Not using collected queries." );

            layers = gm.getLayers().iterator();
            styles = gm.getStyles().iterator();
        }

        while ( layers.hasNext() ) {
            Layer l = layers.next();
            Style s = styles.next();

            applyHints( l.getName(), g, layerOptions, defaultLayerOptions );

            warnings.addAll( paintLayer( l, s, g, gm ) );
        }
    }

    public boolean hasTheme( String name ) {
        return themeMap.get( name ) != null;
    }

    private HashMap<String, OperatorFilter> extractFilters( RequestBase gm ) {
        HashMap<String, OperatorFilter> filters = new HashMap<String, OperatorFilter>();
        Map<String, OperatorFilter> reqFilters = gm.getFilters();
        for ( LayerRef lr : gm.getLayers() ) {
            OperatorFilter f = reqFilters.get( lr.getName() );
            if ( f != null ) {
                for ( org.deegree.layer.Layer l : Themes.getAllLayers( themeMap.get( lr.getName() ) ) ) {
                    filters.put( l.getMetadata().getName(), f );
                }
            }
        }
        return filters;
    }

    public void getMap( org.deegree.protocol.wms.ops.GetMap gm, List<String> headers, RenderContext ctx )
                            throws OWSException {
        Map<String, StyleRef> styles = new HashMap<String, StyleRef>();
        Iterator<StyleRef> iter = gm.getStyles().iterator();
        MapOptionsMaps options = gm.getRenderingOptions();
        List<MapOptions> mapOptions = new ArrayList<MapOptions>();
        for ( LayerRef lr : gm.getLayers() ) {
            StyleRef style = iter.next();
            for ( org.deegree.layer.Layer l : Themes.getAllLayers( themeMap.get( lr.getName() ) ) ) {
                insertMissingOptions( l.getMetadata().getName(), options, l.getMetadata().getMapOptions(),
                                      defaultLayerOptions );
                mapOptions.add( options.get( l.getMetadata().getName() ) );
                StyleRef ref;
                if ( style.getStyle() != null ) {
                    ref = new StyleRef( style.getStyle() );
                } else {
                    ref = new StyleRef( style.getName() );
                }
                styles.put( l.getMetadata().getName(), ref );
            }
        }

        List<LayerData> list = new ArrayList<LayerData>();

        double scale = gm.getScale();

        LayerQuery query = new LayerQuery( gm.getBoundingBox(), gm.getWidth(), gm.getHeight(), styles,
                                           extractFilters( gm ), gm.getParameterMap(), gm.getDimensions(),
                                           gm.getPixelSize(), options, gm.getQueryBox() );
        for ( LayerRef lr : gm.getLayers() ) {
            for ( org.deegree.layer.Layer l : Themes.getAllLayers( themeMap.get( lr.getName() ) ) ) {
                if ( l.getMetadata().getScaleDenominators().first > scale
                     || l.getMetadata().getScaleDenominators().second < scale ) {
                    continue;
                }
                list.add( l.mapQuery( query, headers ) );
            }
        }
        Iterator<MapOptions> optIter = mapOptions.iterator();
        for ( LayerData d : list ) {
            ctx.applyOptions( optIter.next() );
            d.render( ctx );
        }
    }

    private static void insertMissingOptions( String layer, MapOptionsMaps options, MapOptions layerDefaults,
                                              MapOptions globalDefaults ) {
        if ( options.getAntialias( layer ) == null ) {
            if ( layerDefaults != null ) {
                options.setAntialias( layer, layerDefaults.getAntialias() );
            }
            if ( options.getAntialias( layer ) == null ) {
                options.setAntialias( layer, globalDefaults.getAntialias() );
            }
        }
        if ( options.getQuality( layer ) == null ) {
            if ( layerDefaults != null ) {
                options.setQuality( layer, layerDefaults.getQuality() );
            }
            if ( options.getQuality( layer ) == null ) {
                options.setQuality( layer, globalDefaults.getQuality() );
            }
        }
        if ( options.getInterpolation( layer ) == null ) {
            if ( layerDefaults != null ) {
                options.setInterpolation( layer, layerDefaults.getInterpolation() );
            }
            if ( options.getInterpolation( layer ) == null ) {
                options.setInterpolation( layer, globalDefaults.getInterpolation() );
            }
        }
        if ( options.getMaxFeatures( layer ) == -1 ) {
            if ( layerDefaults != null ) {
                options.setMaxFeatures( layer, layerDefaults.getMaxFeatures() );
            }
            if ( options.getMaxFeatures( layer ) == -1 ) {
                options.setMaxFeatures( layer, globalDefaults.getMaxFeatures() );
            }
        }
        if ( options.getFeatureInfoRadius( layer ) == -1 ) {
            if ( layerDefaults != null ) {
                options.setFeatureInfoRadius( layer, layerDefaults.getFeatureInfoRadius() );
            }
            if ( options.getFeatureInfoRadius( layer ) == -1 ) {
                options.setFeatureInfoRadius( layer, globalDefaults.getFeatureInfoRadius() );
            }
        }
    }

    public FeatureCollection getFeatures( org.deegree.protocol.wms.ops.GetFeatureInfo gfi, List<String> headers )
                            throws OWSException {
        Map<String, StyleRef> styles = new HashMap<String, StyleRef>();
        Iterator<StyleRef> iter = gfi.getStyles().iterator();
        for ( LayerRef lr : gfi.getQueryLayers() ) {
            StyleRef style = iter.next();
            for ( org.deegree.layer.Layer l : Themes.getAllLayers( themeMap.get( lr.getName() ) ) ) {
                styles.put( l.getMetadata().getName(), style );
            }
        }
        List<LayerData> list = new ArrayList<LayerData>();
        LayerQuery query = new LayerQuery( gfi.getEnvelope(), gfi.getWidth(), gfi.getHeight(), gfi.getX(), gfi.getY(),
                                           gfi.getFeatureCount(), extractFilters( gfi ), styles, gfi.getParameterMap(),
                                           gfi.getDimensions(), new MapOptionsMaps(), gfi.getEnvelope() );

        double scale = calcScaleWMS130( gfi.getWidth(), gfi.getHeight(), gfi.getEnvelope(), gfi.getCoordinateSystem(),
                                        DEFAULT_PIXEL_SIZE );

        for ( LayerRef n : gfi.getQueryLayers() ) {
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
            feats.addAll( col );
        }

        feats = Features.clearDuplicates( feats );

        if ( feats.size() > gfi.getFeatureCount() ) {
            feats = feats.subList( 0, gfi.getFeatureCount() );
        }
        GenericFeatureCollection col = new GenericFeatureCollection();
        col.addAll( feats );
        return col;
    }

    /**
     * @param gm
     * @return a rendered image, containing the requested maps
     * @throws InvalidDimensionValue
     * @throws MissingDimensionValue
     */
    public Pair<BufferedImage, LinkedList<String>> getMapImage( GetMap gm )
                            throws MissingDimensionValue, InvalidDimensionValue {
        LinkedList<String> warnings = new LinkedList<String>();

        BufferedImage img = prepareImage( gm );
        Graphics2D g = img.createGraphics();
        paintMap( g, gm, warnings );
        g.dispose();

        // 8 bit png color map support copied from deegree 2, to be optimized
        if ( gm.getFormat().equals( "image/png; mode=8bit" ) || gm.getFormat().equals( "image/png; subtype=8bit" )
             || gm.getFormat().equals( "image/gif" ) ) {
            img = postprocessPng8bit( img );
        }
        return new Pair<BufferedImage, LinkedList<String>>( img, warnings );
    }

    private LinkedList<String> getFeatures( Collection<Feature> feats, Layer l, GetFeatureInfo fi, Style s )
                            throws MissingDimensionValue, InvalidDimensionValue {
        LinkedList<String> warnings = new LinkedList<String>();

        if ( l.isQueryable() ) {
            Pair<FeatureCollection, LinkedList<String>> pair = l.getFeatures( fi, s );
            if ( pair != null ) {
                if ( pair.first != null ) {
                    addAllUncontained( feats, pair.first );
                }
                warnings.addAll( pair.second );
            }
        }
        double scale = calcScaleWMS130( fi.getWidth(), fi.getHeight(), fi.getEnvelope(), fi.getCoordinateSystem(),
                                        DEFAULT_PIXEL_SIZE );
        for ( Layer c : l.getChildren() ) {
            DoublePair scales = c.getScaleHint();
            LOG.debug( "Scale settings are: {}, current scale is {}.", scales, scale );
            if ( scales.first > scale || scales.second < scale ) {
                LOG.debug( "Not showing layer '{}' because of its scale constraint.",
                           c.getName() == null ? c.getTitle() : c.getName() );
                continue;
            }
            if ( c.getName() != null ) {
                s = registry.get( c.getName(), null );
            }
            warnings.addAll( getFeatures( feats, c, fi, s ) );
        }
        return warnings;
    }

    /**
     * @param fi
     * @return a collection of feature values for the selected area, and warning headers
     * @throws InvalidDimensionValue
     * @throws MissingDimensionValue
     */
    public Pair<FeatureCollection, LinkedList<String>> getFeatures( GetFeatureInfo fi )
                            throws MissingDimensionValue, InvalidDimensionValue {
        List<Feature> list = new LinkedList<Feature>();
        LinkedList<String> warnings = new LinkedList<String>();
        Iterator<Style> styles = fi.getStyles().iterator();
        double scale = calcScaleWMS130( fi.getWidth(), fi.getHeight(), fi.getEnvelope(), fi.getCoordinateSystem(),
                                        DEFAULT_PIXEL_SIZE );
        for ( Layer layer : fi.getQueryLayers() ) {
            DoublePair scales = layer.getScaleHint();
            LOG.debug( "Scale settings are: {}, current scale is {}.", scales, scale );
            if ( scales.first > scale || scales.second < scale ) {
                LOG.debug( "Not showing layer '{}' because of its scale constraint.",
                           layer.getName() == null ? layer.getTitle() : layer.getName() );
                continue;
            }
            warnings.addAll( getFeatures( list, layer, fi, styles.next() ) );
        }

        list = Features.clearDuplicates( list );

        if ( list.size() > fi.getFeatureCount() ) {
            list = list.subList( 0, fi.getFeatureCount() );
        }

        GenericFeatureCollection col = new GenericFeatureCollection();
        col.addAll( list );
        return new Pair<FeatureCollection, LinkedList<String>>( col, warnings );
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
        Pair<Integer, Integer> res = legendSizes.get( style );
        if ( res != null ) {
            return res;
        }

        legendSizes.put( style, res = new Legends().getLegendSize( style ) );
        return res;
    }

    public BufferedImage getLegend( GetLegendGraphic req ) {
        Legends renderer = new Legends( req.getLegendOptions() );

        LayerRef layer = req.getLayer();
        StyleRef styleRef = req.getStyle();
        Style style;

        if ( isNewStyle() ) {
            style = themeMap.get( layer.getName() ).getMetadata().getLegendStyles().get( styleRef.getName() );
            if ( style == null ) {
                style = themeMap.get( layer.getName() ).getMetadata().getStyles().get( styleRef.getName() );
            }
        } else {
            style = registry.getLegendStyle( layer.getName(), styleRef.getName() );
        }

        Pair<Integer, Integer> size;
        if ( renderer.getLegendOptions().isDefault() ) {
            size = getLegendSize( style );
        } else {
            size = renderer.getLegendSize( style );
        }

        if ( req.getWidth() == -1 ) {
            req.setWidth( size.first );
        }
        if ( req.getHeight() == -1 ) {
            req.setHeight( size.second );
        }

        boolean originalSize = req.getWidth() == size.first && req.getHeight() == size.second
                               && renderer.getLegendOptions().isDefault();

        HashMap<String, BufferedImage> legendMap = legends.get( style );
        if ( originalSize && legendMap != null && legendMap.get( req.getFormat() ) != null ) {
            return legendMap.get( req.getFormat() );
        }
        if ( legendMap == null ) {
            legendMap = new HashMap<String, BufferedImage>();
            legends.put( style, legendMap );
        }

        BufferedImage img = prepareImage( req );
        Graphics2D g = img.createGraphics();
        g.setRenderingHint( KEY_ANTIALIASING, VALUE_ANTIALIAS_ON );
        g.setRenderingHint( KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON );

        renderer.paintLegend( style, req.getWidth(), req.getHeight(), g );

        g.dispose();

        if ( req.getFormat().equals( "image/png; mode=8bit" ) || req.getFormat().equals( "image/png; subtype=8bit" )
             || req.getFormat().equals( "image/gif" ) ) {
            img = postprocessPng8bit( img );
        }

        if ( originalSize ) {
            legendMap.put( req.getFormat(), img );
        }

        return img;
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
