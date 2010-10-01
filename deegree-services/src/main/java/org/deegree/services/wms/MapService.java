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

import static java.awt.Font.PLAIN;
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
import static java.awt.image.BufferedImage.TYPE_BYTE_INDEXED;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.awt.image.DataBuffer.TYPE_BYTE;
import static java.awt.image.Raster.createBandedRaster;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static javax.media.jai.operator.ColorQuantizerDescriptor.MEDIANCUT;
import static org.deegree.commons.utils.CollectionUtils.AND;
import static org.deegree.commons.utils.CollectionUtils.addAllUncontained;
import static org.deegree.commons.utils.CollectionUtils.map;
import static org.deegree.commons.utils.CollectionUtils.reduce;
import static org.deegree.commons.utils.CollectionUtils.removeDuplicates;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.rendering.r2d.styling.components.UOM.Metre;
import static org.deegree.services.wms.controller.ops.GetMap.Antialias.BOTH;
import static org.deegree.services.wms.controller.ops.GetMap.Interpolation.NEARESTNEIGHBOR;
import static org.deegree.services.wms.controller.ops.GetMap.Quality.NORMAL;
import static org.deegree.services.wms.model.layers.Layer.render;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.BandSelectDescriptor;
import javax.media.jai.operator.ColorQuantizerDescriptor;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.deegree.commons.utils.CollectionUtils;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.CRS;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.query.ThreadedResultSet;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.xpath.FeatureXPathEvaluator;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.rendering.r2d.Java2DRenderer;
import org.deegree.rendering.r2d.Java2DTextRenderer;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.rendering.r2d.styling.LineStyling;
import org.deegree.rendering.r2d.styling.PointStyling;
import org.deegree.rendering.r2d.styling.Styling;
import org.deegree.rendering.r2d.styling.TextStyling;
import org.deegree.services.jaxb.wms.AbstractLayerType;
import org.deegree.services.jaxb.wms.BaseAbstractLayerType;
import org.deegree.services.jaxb.wms.DynamicLayer;
import org.deegree.services.jaxb.wms.ServiceConfiguration;
import org.deegree.services.jaxb.wms.StatisticsLayer;
import org.deegree.services.jaxb.wms.SupportedFeaturesType;
import org.deegree.services.wms.WMSException.InvalidDimensionValue;
import org.deegree.services.wms.WMSException.MissingDimensionValue;
import org.deegree.services.wms.controller.ops.GetFeatureInfo;
import org.deegree.services.wms.controller.ops.GetFeatureInfoSchema;
import org.deegree.services.wms.controller.ops.GetLegendGraphic;
import org.deegree.services.wms.controller.ops.GetMap;
import org.deegree.services.wms.controller.ops.GetMap.Antialias;
import org.deegree.services.wms.controller.ops.GetMap.Interpolation;
import org.deegree.services.wms.controller.ops.GetMap.Quality;
import org.deegree.services.wms.dynamic.LayerUpdater;
import org.deegree.services.wms.dynamic.PostGISUpdater;
import org.deegree.services.wms.dynamic.ShapeUpdater;
import org.deegree.services.wms.model.layers.EmptyLayer;
import org.deegree.services.wms.model.layers.FeatureLayer;
import org.deegree.services.wms.model.layers.Layer;
import org.deegree.services.wms.model.layers.RasterLayer;
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

    private static final GeometryFactory geofac = new GeometryFactory();

    /**
     * 
     */
    public HashMap<String, Layer> layers;

    private Layer root;

    /**
     * 
     */
    public StyleRegistry registry = new StyleRegistry();

    private HashMap<Style, Pair<Integer, Integer>> legendSizes = new HashMap<Style, Pair<Integer, Integer>>();

    private HashMap<Style, HashMap<String, BufferedImage>> legends = new HashMap<Style, HashMap<String, BufferedImage>>();

    private HashMap<Layer, Antialias> defaultAntialiases = new HashMap<Layer, Antialias>();

    private HashMap<Layer, Quality> defaultQualities = new HashMap<Layer, Quality>();

    private HashMap<Layer, Interpolation> defaultInterpolations = new HashMap<Layer, Interpolation>();

    private HashMap<Layer, Integer> defaultMaxFeatures = new HashMap<Layer, Integer>();

    private int globalDefaultMaxFeatures = 10000;

    private int defaultFeatureInfoRadius = 1;

    private static int stylesCounter = 0;

    private LinkedList<LayerUpdater> dynamics = new LinkedList<LayerUpdater>();

    /**
     * The current update sequence.
     */
    public int updateSequence = 0; // TODO how to restore this after restart?

    /**
     * @param conf
     * @param adapter
     * @throws MalformedURLException
     */
    public MapService( ServiceConfiguration conf, XMLAdapter adapter ) throws MalformedURLException {
        layers = new HashMap<String, Layer>();
        if ( conf != null && conf.getAbstractLayer() != null ) {
            SupportedFeaturesType sf = conf.getSupportedFeatures();
            Antialias alias = handleDefaultValue( sf == null ? null : sf.getAntiAliasing(), Antialias.class, BOTH );
            Quality quali = handleDefaultValue( sf == null ? null : sf.getRenderingQuality(), Quality.class, NORMAL );
            Interpolation interpol = handleDefaultValue( sf == null ? null : sf.getInterpolation(),
                                                         Interpolation.class, NEARESTNEIGHBOR );
            if ( sf != null && sf.getMaxFeatures() != null ) {
                globalDefaultMaxFeatures = sf.getMaxFeatures();
                LOG.debug( "Using global max features setting of {}.", globalDefaultMaxFeatures );
            } else {
                LOG.debug( "Using default global max features setting of {}, set it to -1 if you don't want a limit.",
                           globalDefaultMaxFeatures );
            }
            root = parseLayer( conf.getAbstractLayer().getValue(), null, adapter, alias, interpol, quali );
            defaultFeatureInfoRadius = conf.getFeatureInfoRadius() == null ? 1 : parseInt( conf.getFeatureInfoRadius() );
            fillInheritedInformation( root, new LinkedList<CRS>( root.getSrs() ) );
            // update the dynamic layers once on startup to avoid having a disappointingly long initial GetCapabilities
            // request...
            update();
            new Timer().schedule( registry, 0, 1000 );
        }
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
    public static void fillInheritedInformation( Layer layer, List<CRS> srs ) {
        if ( layer.getParent() == null ) {
            if ( layer.getScaleHint() == null ) {
                layer.setScaleHint( new DoublePair( NEGATIVE_INFINITY, POSITIVE_INFINITY ) );
            }
        }

        for ( Layer l : layer.getChildren() ) {
            List<CRS> curSrs = new LinkedList<CRS>( srs );
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
                    res = new FeatureLayer( aLayer, parent, adapter );
                } catch ( FileNotFoundException e ) {
                    LOG.warn( "Layer '{}' could not be loaded: '{}'", aLayer.getName() == null ? aLayer.getTitle()
                                                                                              : aLayer.getName(),
                              e.getLocalizedMessage() );
                    LOG.trace( "Stack trace", e );
                    return null;
                } catch ( IOException e ) {
                    LOG.warn( "Layer '{}' could not be loaded: '{}'", aLayer.getName() == null ? aLayer.getTitle()
                                                                                              : aLayer.getName(),
                              e.getLocalizedMessage() );
                    LOG.trace( "Stack trace", e );
                    return null;
                }
            } else if ( aLayer.getCoverageStoreId() != null ) {
                res = new RasterLayer( aLayer, parent );
                // }else if(aLayer.getWMSStoreId() != null){
            } else {
                res = new EmptyLayer( aLayer, parent );
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

            SupportedFeaturesType sf = aLayer.getSupportedFeatures();
            if ( sf != null ) {
                alias = handleDefaultValue( sf.getAntiAliasing(), Antialias.class, alias );
                quality = handleDefaultValue( sf.getRenderingQuality(), Quality.class, quality );
                interpol = handleDefaultValue( sf.getInterpolation(), Interpolation.class, interpol );
                if ( sf.getMaxFeatures() != null ) {
                    defaultMaxFeatures.put( res, sf.getMaxFeatures() );
                }
            }
            defaultAntialiases.put( res, alias );
            defaultQualities.put( res, quality );
            defaultInterpolations.put( res, interpol );

            addChildren( res, aLayer.getAbstractLayer(), adapter, alias, interpol, quality );
        } else if ( layer instanceof StatisticsLayer ) {
            res = new org.deegree.services.wms.model.layers.StatisticsLayer( parent );
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
                dynamics.add( new PostGISUpdater( dyn.getPostGIS(), parent, this ) );
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
            warnings.addAll( paintLayer( child, registry.getDefault( child.getInternalName() ), g, gm ) );
        }
        return warnings;
    }

    private static int getType( boolean transparent, String format ) {
        int type = transparent ? TYPE_INT_ARGB : TYPE_INT_RGB;
        if ( format.equals( "image/x-ms-bmp" ) ) {
            type = TYPE_INT_RGB;
        }
        return type;
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
        if ( format.equals( "image/png; mode=8bit" ) || format.equals( "image/png; subtype=8bit" )
             || format.equals( "image/gif" ) ) {
            ColorModel cm = PlanarImage.getDefaultColorModel( TYPE_BYTE, 4 );
            return new BufferedImage( cm, createBandedRaster( TYPE_BYTE, width, height, 4, null ), false, null );
        }
        return prepareImage( width, height, bgcolor,
                             transparent && ( format.indexOf( "png" ) != -1 || format.indexOf( "gif" ) != -1 ), format );
    }

    /**
     * @param width
     * @param height
     * @param color
     * @param transparent
     * @param format
     * @return an empty image conforming to the parameters
     */
    public static BufferedImage prepareImage( int width, int height, Color color, boolean transparent, String format ) {
        BufferedImage img = new BufferedImage( width, height, getType( transparent, format ) );
        if ( !transparent ) {
            Graphics2D g = img.createGraphics();
            g.setBackground( color );
            g.clearRect( 0, 0, width, height );
            g.dispose();
        }
        return img;
    }

    protected static void applyHints( final Layer l, final Map<Layer, Quality> qualities,
                                      final Map<Layer, Interpolation> interpolations,
                                      final Map<Layer, Antialias> antialiases, final Graphics2D g ) {
        switch ( qualities.get( l ) ) {
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
        switch ( interpolations.get( l ) ) {
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
        switch ( antialiases.get( l ) ) {
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

    protected static final BufferedImage postprocessPng8bit( final BufferedImage img ) {
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

        return res;
    }

    private static Mapper<Boolean, Layer> getFeatureLayerCollector( final LinkedList<FeatureLayer> list ) {
        return new Mapper<Boolean, Layer>() {
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
                                                                   registry.getDefault( child.getInternalName() ), gm,
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
        Map<Layer, Quality> qualities = gm.getQuality();
        Map<Layer, Interpolation> interpolations = gm.getInterpolation();
        Map<Layer, Antialias> antialiases = gm.getAntialias();

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
                FeatureXPathEvaluator evaluator = new FeatureXPathEvaluator( GML_31 );

                Collection<LinkedList<Query>> qs = queries.values();
                FeatureResultSet rs = null;
                try {
                    FeatureStore store = queries.keySet().iterator().next();
                    LinkedList<Query> queriesList = qs.iterator().next();
                    if ( !queriesList.isEmpty() ) {
                        rs = store.query( queriesList.toArray( new Query[queriesList.size()] ) );
                        // TODO Should this always be done on this level? What about min and maxFill values?
                        rs = new ThreadedResultSet( rs, 100, 20 );
                        for ( Feature f : rs ) {
                            QName name = f.getType().getName();
                            FeatureLayer l = ftToLayer.get( name );

                            applyHints( l, qualities, interpolations, antialiases, g );
                            render( f, evaluator, ftToStyle.get( name ), renderer, textRenderer, gm.getScale(),
                                    gm.getResolution() );
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

            applyHints( l, qualities, interpolations, antialiases, g );

            warnings.addAll( paintLayer( l, s, g, gm ) );
        }
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
        for ( Layer c : l.getChildren() ) {
            if ( c.getName() != null ) {
                s = registry.getDefault( c.getName() );
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
    public Pair<GenericFeatureCollection, LinkedList<String>> getFeatures( GetFeatureInfo fi )
                            throws MissingDimensionValue, InvalidDimensionValue {
        List<Feature> list = new LinkedList<Feature>();
        LinkedList<String> warnings = new LinkedList<String>();
        Iterator<Style> styles = fi.getStyles().iterator();
        for ( Layer layer : fi.getQueryLayers() ) {
            warnings.addAll( getFeatures( list, layer, fi, styles.next() ) );
        }

        if ( list.size() > fi.getFeatureCount() ) {
            list = list.subList( 0, fi.getFeatureCount() );
        }

        GenericFeatureCollection col = new GenericFeatureCollection();
        col.addAll( list );
        return new Pair<GenericFeatureCollection, LinkedList<String>>( col, warnings );
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

    /**
     * @param fis
     * @return an application schema object
     */
    public List<FeatureType> getSchema( GetFeatureInfoSchema fis ) {
        List<FeatureType> list = new LinkedList<FeatureType>();
        for ( String l : fis.getLayers() ) {
            getFeatureTypes( list, layers.get( l ) );
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
        close( root );
    }

    /**
     * @return the style registry
     */
    public StyleRegistry getStyles() {
        return registry;
    }

    protected static Polygon getLegendRect( int xpos, int ypos, int xsize, int ysize ) {
        Point p1 = geofac.createPoint( null, xpos, ypos, null );
        Point p2 = geofac.createPoint( null, xpos + xsize, ypos, null );
        Point p3 = geofac.createPoint( null, xpos + xsize, ypos + ysize, null );
        Point p4 = geofac.createPoint( null, xpos, ypos + ysize, null );
        List<Point> ps = new ArrayList<Point>( 5 );
        ps.add( p1 );
        ps.add( p2 );
        ps.add( p3 );
        ps.add( p4 );
        ps.add( p1 );

        return geofac.createPolygon( null, null, geofac.createLinearRing( null, null, geofac.createPoints( ps ) ), null );
    }

    protected static LineString getLegendLine( int xpos, int ypos, int xsz, int ysz ) {
        Point p1 = geofac.createPoint( null, xpos, ypos, null );
        Point p2 = geofac.createPoint( null, xpos + xsz / 3, ypos + ysz / 3 * 2, null );
        Point p3 = geofac.createPoint( null, xpos + xsz / 3 * 2, ypos + ysz / 3, null );
        Point p4 = geofac.createPoint( null, xpos + xsz, ypos + ysz, null );
        List<Point> ps = new ArrayList<Point>( 4 );
        ps.add( p1 );
        ps.add( p2 );
        ps.add( p3 );
        ps.add( p4 );
        return geofac.createLineString( null, null, geofac.createPoints( ps ) );
    }

    /**
     * @param req
     * @return the legend
     */
    public BufferedImage getLegend( GetLegendGraphic req ) {
        Style style = req.getStyle();
        Pair<Integer, Integer> size = getLegendSize( style );

        boolean originalSize = req.getWidth() == size.first && req.getHeight() == size.second;

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

        Java2DRenderer renderer = new Java2DRenderer( g, req.getWidth(), req.getHeight(),
                                                      new DefaultEnvelope( geofac.createPoint( null, 0, 0, null ),
                                                                           geofac.createPoint( null, size.first,
                                                                                               size.second, null ) ) );
        Java2DTextRenderer textRenderer = new Java2DTextRenderer( renderer );

        int ypos = 6;
        int xpos = 6;

        Iterator<Class<?>> types = style.getRuleTypes().iterator();
        TextStyling textStyling = new TextStyling();
        textStyling.font = new org.deegree.rendering.r2d.styling.components.Font();
        textStyling.font.fontFamily.add( 0, "Arial" );
        textStyling.font.fontSize = 14;
        textStyling.anchorPointX = 0;
        textStyling.anchorPointY = 0.5;
        textStyling.uom = Metre;

        Mapper<Boolean, Styling> pointStylingMapper = CollectionUtils.<Styling> getInstanceofMapper( PointStyling.class );
        Mapper<Boolean, Styling> lineStylingMapper = CollectionUtils.<Styling> getInstanceofMapper( LineStyling.class );
        // Mapper<Boolean, Styling> polygonStylingMapper = CollectionUtils.<Styling> getInstanceofMapper(
        // PolygonStyling.class );
        Iterator<String> titles = style.getRuleTitles().iterator();
        for ( LinkedList<Styling> styles : style.getBases() ) {
            String title = titles.next();
            Class<?> c = types.next();
            boolean isPoint = c.equals( Point.class ) || reduce( true, map( styles, pointStylingMapper ), AND );
            boolean isLine = c.equals( LineString.class ) || reduce( true, map( styles, lineStylingMapper ), AND );
            // boolean isPolygon = c.equals( Polygon.class ) || reduce( true, map( styles, polygonStylingMapper ), AND
            // );

            Geometry geom;
            if ( isPoint ) {
                geom = geofac.createPoint( null, xpos + 10, ypos + 10, null );
            } else if ( isLine ) {
                geom = getLegendLine( xpos, ypos, 20, 20 );
                // } else if ( isPolygon ) {
                // geom = getLegendRect( xpos, ypos, 20, 20 );
            } else {
                // something better?
                geom = getLegendRect( xpos, ypos, 20, 20 );
            }
            if ( title != null && title.length() > 0 ) {
                textRenderer.render( textStyling, title, geofac.createPoint( null, 35, ypos + 10, null ) );
            }
            ypos += 32;

            double maxSize = 0;
            if ( isPoint ) {
                for ( Styling s : styles ) {
                    if ( s instanceof PointStyling ) {
                        maxSize = max( ( (PointStyling) s ).graphic.size, maxSize );
                    }
                }
            }

            for ( Styling styling : styles ) {
                // normalize point symbols to 20 pixels
                if ( styling instanceof PointStyling && isPoint ) {
                    PointStyling s = ( (PointStyling) styling ).copy();
                    s.uom = Metre;
                    s.graphic.size = s.graphic.size / maxSize * 20;
                    styling = s;
                }
                renderer.render( styling, geom );
            }
        }
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
     * @param style
     * @return the legend width/height given a base size of 32x32
     */
    public Pair<Integer, Integer> getLegendSize( Style style ) {
        Pair<Integer, Integer> res = legendSizes.get( style );
        if ( res != null ) {
            return res;
        }

        res = new Pair<Integer, Integer>( 0, 0 );

        res.second = 32 * style.getBases().size();
        res.first = 32;

        Font font = new Font( "Arial", PLAIN, 14 );

        for ( String s : style.getRuleTitles() ) {
            if ( s != null && s.length() > 0 ) {
                TextLayout layout = new TextLayout( s, font, new FontRenderContext( new AffineTransform(), true, false ) );
                res.first = (int) max( layout.getBounds().getWidth() + 40, res.first );
            }
        }

        LOG.trace( "Calculated a legend size of '{}'.", res );

        legendSizes.put( style, res );

        return res;
    }

    /**
     * @return the map w/ default settings
     */
    public HashMap<Layer, Antialias> getDefaultAntialiases() {
        return defaultAntialiases;
    }

    /**
     * @return the map w/ default settings
     */
    public HashMap<Layer, Interpolation> getDefaultInterpolations() {
        return defaultInterpolations;
    }

    /**
     * @return the map w/ default settings
     */
    public HashMap<Layer, Quality> getDefaultQualities() {
        return defaultQualities;
    }

    /**
     * @return the map w/ default settings
     */
    public HashMap<Layer, Integer> getDefaultMaxFeatures() {
        return defaultMaxFeatures;
    }

    /**
     * @return the default feature info radius
     */
    public int getFeatureInfoRadius() {
        return defaultFeatureInfoRadius;
    }

    /**
     * @return the global max features setting
     */
    public int getGlobalMaxFeatures() {
        return globalDefaultMaxFeatures;
    }

}
