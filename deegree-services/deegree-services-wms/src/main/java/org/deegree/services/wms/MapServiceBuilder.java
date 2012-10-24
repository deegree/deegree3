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

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.deegree.rendering.r2d.context.MapOptions.Antialias.BOTH;
import static org.deegree.rendering.r2d.context.MapOptions.Interpolation.NEARESTNEIGHBOR;
import static org.deegree.rendering.r2d.context.MapOptions.Quality.NORMAL;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.rendering.r2d.context.MapOptions;
import org.deegree.rendering.r2d.context.MapOptions.Antialias;
import org.deegree.rendering.r2d.context.MapOptions.Interpolation;
import org.deegree.rendering.r2d.context.MapOptions.Quality;
import org.deegree.rendering.r2d.context.MapOptionsMaps;
import org.deegree.services.jaxb.wms.AbstractLayerType;
import org.deegree.services.jaxb.wms.BaseAbstractLayerType;
import org.deegree.services.jaxb.wms.DynamicLayer;
import org.deegree.services.jaxb.wms.LayerOptionsType;
import org.deegree.services.jaxb.wms.ServiceConfigurationType;
import org.deegree.services.jaxb.wms.StatisticsLayer;
import org.deegree.services.wms.dynamic.LayerUpdater;
import org.deegree.services.wms.dynamic.PostGISUpdater;
import org.deegree.services.wms.dynamic.ShapeUpdater;
import org.deegree.services.wms.model.layers.EmptyLayer;
import org.deegree.services.wms.model.layers.FeatureLayer;
import org.deegree.services.wms.model.layers.Layer;
import org.deegree.services.wms.model.layers.RasterLayer;
import org.deegree.services.wms.model.layers.RemoteWMSLayer;
import org.slf4j.Logger;

/**
 * Builds map service components from jaxb config beans.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class MapServiceBuilder {

    private static final Logger LOG = getLogger( MapServiceBuilder.class );

    private static int stylesCounter = 0;

    private ServiceConfigurationType conf;

    private XMLAdapter adapter;

    private MapOptionsMaps layerOptions;

    private MapService service;

    private Antialias alias;

    private Quality quali;

    private Interpolation interpol;

    private DeegreeWorkspace workspace;

    private List<LayerUpdater> dynamics;

    MapServiceBuilder( ServiceConfigurationType conf, XMLAdapter adapter, MapOptionsMaps layerOptions,
                       MapService service, DeegreeWorkspace workspace, List<LayerUpdater> dynamics ) {
        this.conf = conf;
        this.adapter = adapter;
        this.layerOptions = layerOptions;
        this.service = service;
        this.workspace = workspace;
        this.dynamics = dynamics;
        alias = Antialias.BOTH;
        quali = Quality.HIGH;
        interpol = Interpolation.NEARESTNEIGHBOUR;
    }

    MapOptions buildMapOptions() {
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
            return new MapOptions( quali, interpol, alias, maxFeatures, featureInfoRadius );
        }
        return null;
    }

    Layer parseLayers()
                            throws MalformedURLException {
        return parseLayer( conf.getAbstractLayer().getValue(), null, adapter, alias, interpol, quali );
    }

    private Layer parseLayer( BaseAbstractLayerType layer, final Layer parent, XMLAdapter adapter, Antialias alias,
                              Interpolation interpol, Quality quality )
                            throws MalformedURLException {
        Layer res = null;
        if ( layer instanceof AbstractLayerType ) {
            AbstractLayerType aLayer = (AbstractLayerType) layer;

            if ( aLayer.getFeatureStoreId() != null ) {
                try {
                    res = new FeatureLayer( service, aLayer, parent, workspace );
                } catch ( Throwable e ) {
                    LOG.warn( "Layer '{}' could not be loaded: '{}'", aLayer.getName() == null ? aLayer.getTitle()
                                                                                              : aLayer.getName(),
                              e.getLocalizedMessage() );
                    LOG.trace( "Stack trace", e );
                    return null;
                }
            } else if ( aLayer.getCoverageStoreId() != null ) {
                res = new RasterLayer( service, aLayer, parent );
            } else if ( aLayer.getRemoteWMSStoreId() != null ) {
                res = new RemoteWMSLayer( service, aLayer, parent );
            } else {
                res = new EmptyLayer( service, aLayer, parent );
            }

            if ( res.getName() != null ) {
                if ( aLayer.getDirectStyle() != null ) {
                    service.registry.load( res.getName(), aLayer.getDirectStyle(), adapter );
                }
                if ( aLayer.getSLDStyle() != null ) {
                    service.registry.load( res.getName(), adapter, aLayer.getSLDStyle() );
                }
                synchronized ( service.layers ) {
                    service.layers.put( res.getName(), res );
                }
            } else {
                String name = "NamelessLayer_" + ++stylesCounter;
                if ( aLayer.getDirectStyle() != null ) {
                    service.registry.load( name, aLayer.getDirectStyle(), adapter );
                }
                if ( aLayer.getSLDStyle() != null ) {
                    service.registry.load( name, adapter, aLayer.getSLDStyle() );
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
            res = new org.deegree.services.wms.model.layers.StatisticsLayer( service, parent );
            synchronized ( service.layers ) {
                service.layers.put( res.getName(), res );
            }
        } else {
            DynamicLayer dyn = (DynamicLayer) layer;
            if ( dyn.getShapefileDirectory() != null ) {
                try {
                    File shapeDir = new File( adapter.resolve( dyn.getShapefileDirectory() ).toURI() );
                    dynamics.add( new ShapeUpdater( shapeDir, parent, service ) );
                } catch ( URISyntaxException e ) {
                    LOG.error( "Dynamic shape file directory '{}' could not be resolved.", dyn.getShapefileDirectory() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( dyn.getPostGIS() != null ) {
                dynamics.add( new PostGISUpdater( dyn.getPostGIS().getValue(), dyn.getPostGIS().getSchema(), parent,
                                                  service, adapter.getSystemId(), workspace ) );
            }
        }

        return res;
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

}
