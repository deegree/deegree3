//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.layer.config;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.Pair;
import org.deegree.layer.dims.Dimension;
import org.deegree.layer.persistence.base.jaxb.DimensionType;
import org.deegree.layer.persistence.base.jaxb.LayerOptionsType;
import org.deegree.layer.persistence.base.jaxb.StyleRefType;
import org.deegree.layer.persistence.base.jaxb.StyleRefType.Style.LegendGraphic;
import org.deegree.layer.persistence.base.jaxb.StyleRefType.Style.LegendStyle;
import org.deegree.rendering.r2d.context.MapOptions;
import org.deegree.rendering.r2d.context.MapOptions.Antialias;
import org.deegree.rendering.r2d.context.MapOptions.Interpolation;
import org.deegree.rendering.r2d.context.MapOptions.Quality;
import org.deegree.style.persistence.StyleStore;
import org.deegree.style.persistence.StyleStoreManager;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;

/**
 * Some methods to work with the jaxb beans from the base layer schema.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class ConfigUtils {

    private static final Logger LOG = getLogger( ConfigUtils.class );

    public static Pair<Map<String, Style>, Map<String, Style>> parseStyles( DeegreeWorkspace workspace,
                                                                            String layerName, List<StyleRefType> styles ) {
        // hail java 7 to finally be able to do some really complicated type inference
        Map<String, Style> styleMap = new LinkedHashMap<String, Style>();
        Map<String, Style> legendStyleMap = new LinkedHashMap<String, Style>();

        Style defaultStyle = null, defaultLegendStyle = null;

        StyleStoreManager mgr = workspace.getSubsystemManager( StyleStoreManager.class );
        for ( StyleRefType srt : styles ) {
            String id = srt.getStyleStoreId();
            StyleStore store = mgr.get( id );
            if ( store == null ) {
                LOG.warn( "Style store with id {} was not available, ignoring style definition.", id );
                continue;
            }
            if ( srt.getStyle() == null || srt.getStyle().isEmpty() ) {
                if ( store.getAll( layerName ) != null ) {
                    for ( Style s : store.getAll( layerName ) ) {
                        if ( defaultStyle == null ) {
                            defaultStyle = s;
                            defaultLegendStyle = s;
                        }
                        styleMap.put( s.getName(), s );
                        legendStyleMap.put( s.getName(), s );
                    }
                }
                continue;
            }
            Pair<Style, Style> p = useSelectedStyles( store, srt, id, styleMap, legendStyleMap, defaultStyle,
                                                      defaultLegendStyle );
            defaultStyle = p.first;
            defaultLegendStyle = p.second;
        }
        checkDefaultStyle( defaultStyle, styleMap, defaultLegendStyle, legendStyleMap );
        return new Pair<Map<String, Style>, Map<String, Style>>( styleMap, legendStyleMap );
    }

    private static Pair<Style, Style> useSelectedStyles( StyleStore store, StyleRefType srt, String id,
                                                         Map<String, Style> styleMap,
                                                         Map<String, Style> legendStyleMap, Style defaultStyle,
                                                         Style defaultLegendStyle ) {
        for ( org.deegree.layer.persistence.base.jaxb.StyleRefType.Style s : srt.getStyle() ) {
            String name = s.getStyleName();
            String nameRef = s.getStyleNameRef();
            String layerRef = s.getLayerNameRef();
            Style st = store.getStyle( layerRef, nameRef );
            if ( st == null ) {
                LOG.warn( "The combination of layer {} and style {} from store {} is not available.",
                          new Object[] { layerRef, nameRef, id } );
                continue;
            }
            if ( defaultStyle == null ) {
                defaultStyle = st;
            }
            st = st.copy();
            st.setName( name );
            styleMap.put( name, st );
            if ( s.getLegendGraphic() != null ) {
                LegendGraphic g = s.getLegendGraphic();
                // TODO properly handle this
                // st.setLegendFile( null )
            } else {
                LegendStyle ls = s.getLegendStyle();
                if ( ls != null ) {
                    st = store.getStyle( ls.getLayerNameRef(), ls.getStyleNameRef() );
                    st = st.copy();
                    st.setName( name );
                }
                legendStyleMap.put( name, st );
                if ( defaultLegendStyle == null ) {
                    defaultLegendStyle = st;
                }
            }
        }
        return new Pair<Style, Style>( defaultStyle, defaultLegendStyle );
    }

    private static void checkDefaultStyle( Style defaultStyle, Map<String, Style> styleMap, Style defaultLegendStyle,
                                           Map<String, Style> legendStyleMap ) {
        if ( defaultStyle != null && !styleMap.containsKey( "default" ) ) {
            styleMap.put( "default", defaultStyle );
            legendStyleMap.put( "default", defaultLegendStyle );
        }
        if ( !styleMap.containsKey( "default" ) ) {
            styleMap.put( "default", new Style() );
            legendStyleMap.put( "default", new Style() );
        }
    }

    /**
     * @param cfg
     * @return null, if cfg is null
     */
    public static MapOptions parseLayerOptions( LayerOptionsType cfg ) {
        if ( cfg == null ) {
            return null;
        }
        Antialias alias = null;
        Quality quali = null;
        Interpolation interpol = null;
        int maxFeats = -1;
        int rad = 1;
        try {
            alias = Antialias.valueOf( cfg.getAntiAliasing() );
        } catch ( Throwable e ) {
            // ignore
        }
        try {
            quali = Quality.valueOf( cfg.getRenderingQuality() );
        } catch ( Throwable e ) {
            // ignore
        }
        try {
            interpol = Interpolation.valueOf( cfg.getInterpolation() );
        } catch ( Throwable e ) {
            // ignore
        }
        if ( cfg.getMaxFeatures() != null ) {
            maxFeats = cfg.getMaxFeatures();
        }
        if ( cfg.getFeatureInfo() != null && cfg.getFeatureInfo().isEnabled() ) {
            rad = cfg.getFeatureInfo().getPixelRadius().intValue();
        } else if ( cfg.getFeatureInfoRadius() != null ) {
            rad = cfg.getFeatureInfoRadius();
        }
        return new MapOptions( quali, interpol, alias, maxFeats, rad );
    }

    public static Map<String, Dimension<?>> parseDimensions( String layerName, List<DimensionType> dimensions ) {
        return DimensionConfigBuilder.parseDimensions( layerName, dimensions );
    }

}
