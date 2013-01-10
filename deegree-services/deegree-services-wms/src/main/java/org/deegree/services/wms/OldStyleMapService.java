//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wms;

import static org.deegree.commons.utils.CollectionUtils.AND;
import static org.deegree.commons.utils.CollectionUtils.addAllUncontained;
import static org.deegree.commons.utils.CollectionUtils.map;
import static org.deegree.commons.utils.CollectionUtils.reduce;
import static org.deegree.commons.utils.MapUtils.DEFAULT_PIXEL_SIZE;
import static org.deegree.rendering.r2d.RenderHelper.calcScaleWMS130;
import static org.deegree.rendering.r2d.context.Java2DHelper.applyHints;
import static org.deegree.services.wms.model.layers.Layer.render;
import static org.deegree.style.utils.ImageUtils.postprocessPng8bit;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.CollectionUtils;
import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Features;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.stream.ThreadedFeatureInputStream;
import org.deegree.feature.xpath.TypedObjectNodeXPathEvaluator;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.protocol.wms.WMSException.InvalidDimensionValue;
import org.deegree.protocol.wms.WMSException.MissingDimensionValue;
import org.deegree.protocol.wms.filter.ScaleFunction;
import org.deegree.rendering.r2d.Java2DRenderer;
import org.deegree.rendering.r2d.Java2DTextRenderer;
import org.deegree.services.wms.controller.ops.GetFeatureInfo;
import org.deegree.services.wms.controller.ops.GetMap;
import org.deegree.services.wms.model.layers.FeatureLayer;
import org.deegree.services.wms.model.layers.Layer;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;

/**
 * Factored out old style map service methods (using old configuration, old architecture).
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class OldStyleMapService {

    private static final Logger LOG = getLogger( OldStyleMapService.class );

    private MapService service;

    OldStyleMapService( MapService service ) {
        this.service = service;
    }

    /**
     * @param gm
     * @return a rendered image, containing the requested maps
     * @throws InvalidDimensionValue
     * @throws MissingDimensionValue
     */
    Pair<BufferedImage, LinkedList<String>> getMapImage( GetMap gm )
                            throws MissingDimensionValue, InvalidDimensionValue {
        LinkedList<String> warnings = new LinkedList<String>();
        ScaleFunction.getCurrentScaleValue().set( gm.getScale() );

        BufferedImage img = MapService.prepareImage( gm );
        Graphics2D g = img.createGraphics();
        paintMap( g, gm, warnings );
        g.dispose();

        // 8 bit png color map support copied from deegree 2, to be optimized
        if ( gm.getFormat().equals( "image/png; mode=8bit" ) || gm.getFormat().equals( "image/png; subtype=8bit" )
             || gm.getFormat().equals( "image/gif" ) ) {
            img = postprocessPng8bit( img );
        }
        ScaleFunction.getCurrentScaleValue().remove();
        return new Pair<BufferedImage, LinkedList<String>>( img, warnings );
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
    private void paintMap( Graphics2D g, GetMap gm, LinkedList<String> warnings )
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

                    s = s.filter( scale );

                    DoublePair scales = l.getScaleHint();
                    LOG.debug( "Scale settings are: {}, current scale is {}.", scales, scale );
                    if ( scales.first > scale || scales.second < scale || ( !s.isDefault() && s.getRules().isEmpty() ) ) {
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
                handleCollectedQueries( queries, ftToLayer, ftToStyle, gm, g );
                return;
            }

            if ( queries.isEmpty() ) {
                LOG.debug( "No queries found when collecting, probably due to scale constraints in the layers/styles." );
                return;
            }

            LOG.debug( "Not using collected queries." );

            layers = gm.getLayers().iterator();
            styles = gm.getStyles().iterator();
        }

        while ( layers.hasNext() ) {
            Layer l = layers.next();
            Style s = styles.next();

            applyHints( l.getName(), g, service.layerOptions, service.defaultLayerOptions );

            warnings.addAll( paintLayer( l, s, g, gm ) );
        }
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
            LinkedList<String> otherWarns = collectFeatureQueries( queries,
                                                                   (FeatureLayer) child,
                                                                   service.registry.get( child.getInternalName(), null ),
                                                                   gm, ftToLayer, ftToStyle );
            if ( otherWarns == null ) {
                return null;
            }
            warns.addAll( otherWarns );
        }

        return warns;
    }

    private void handleCollectedQueries( Map<FeatureStore, LinkedList<Query>> queries,
                                         HashMap<QName, FeatureLayer> ftToLayer, HashMap<QName, Style> ftToStyle,
                                         GetMap gm, Graphics2D g ) {
        LOG.debug( "Using collected queries for better performance." );

        Java2DRenderer renderer = new Java2DRenderer( g, gm.getWidth(), gm.getHeight(), gm.getBoundingBox(),
                                                      gm.getPixelSize() );
        Java2DTextRenderer textRenderer = new Java2DTextRenderer( renderer );

        // TODO
        XPathEvaluator<?> evaluator = new TypedObjectNodeXPathEvaluator();

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

                    applyHints( l.getName(), g, service.layerOptions, service.defaultLayerOptions );
                    render( f, (XPathEvaluator<Feature>) evaluator, ftToStyle.get( name ), renderer, textRenderer,
                            gm.getScale(), gm.getResolution() );
                }
            } else {
                LOG.warn( "No queries were found for the requested layers." );
            }
        } catch ( FilterEvaluationException e ) {
            LOG.error( "A filter could not be evaluated. The error was '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( FeatureStoreException e ) {
            LOG.error( "Data could not be fetched from the feature store. The error was '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } finally {
            if ( rs != null ) {
                rs.close();
            }
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
            warnings.addAll( paintLayer( child, service.registry.get( child.getInternalName(), null ), g, gm ) );
        }
        return warnings;
    }

    /**
     * @param fi
     * @return a collection of feature values for the selected area, and warning headers
     * @throws InvalidDimensionValue
     * @throws MissingDimensionValue
     */
    Pair<FeatureCollection, LinkedList<String>> getFeatures( GetFeatureInfo fi )
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
                s = service.registry.get( c.getName(), null );
            }
            warnings.addAll( getFeatures( feats, c, fi, s ) );
        }
        return warnings;
    }

}
