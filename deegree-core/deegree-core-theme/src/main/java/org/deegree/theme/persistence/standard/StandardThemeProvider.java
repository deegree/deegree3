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

 Occam Labs Schmitz & Schneider GbR
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.theme.persistence.standard;

import static java.util.Collections.singletonList;
import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.deegree.theme.Themes.aggregateSpatialMetadata;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.ows.metadata.DescriptionConverter;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.geometry.metadata.SpatialMetadataConverter;
import org.deegree.layer.Layer;
import org.deegree.layer.dims.Dimension;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.LayerStoreManager;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.theme.Theme;
import org.deegree.theme.persistence.ThemeProvider;
import org.deegree.theme.persistence.standard.jaxb.ThemeType;
import org.deegree.theme.persistence.standard.jaxb.Themes;
import org.slf4j.Logger;

/**
 * @author stranger
 * 
 */
public class StandardThemeProvider implements ThemeProvider {

    private static final Logger LOG = getLogger( StandardThemeProvider.class );

    private static final URL SCHEMA_URL = StandardThemeProvider.class.getResource( "/META-INF/schemas/themes/3.2.0/themes.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    private StandardTheme buildTheme( ThemeType current, List<ThemeType.Layer> layers, List<ThemeType> themes,
                                      Map<String, LayerStore> stores )
                            throws ResourceInitException {
        List<Layer> lays = new ArrayList<Layer>( layers.size() );

        LinkedHashMap<String, Dimension<?>> dims = new LinkedHashMap<String, Dimension<?>>();
        LinkedHashMap<String, Style> styles = new LinkedHashMap<String, Style>();
        LinkedHashMap<String, Style> legendStyles = new LinkedHashMap<String, Style>();
        List<FeatureType> types = new ArrayList<FeatureType>();

        for ( ThemeType.Layer l : layers ) {
            Layer lay = null;
            if ( l.getLayerStore() != null ) {
                LayerStore s = stores.get( l.getLayerStore() );
                if ( s != null ) {
                    lay = s.get( l.getValue() );
                }
                if ( lay == null ) {
                    LOG.warn( "Layer with identifier {} is not available from {}, trying all.", l.getValue(),
                              l.getLayerStore() );
                }
            }
            if ( lay == null ) {
                for ( LayerStore s : stores.values() ) {
                    lay = s.get( l.getValue() );
                    if ( lay != null ) {
                        break;
                    }
                }
            }
            if ( lay == null ) {
                LOG.warn( "Layer with identifier {} is not available from any layer store.", l.getValue() );
                continue;
            }
            if ( lay.getMetadata().getDimensions() != null ) {
                dims.putAll( lay.getMetadata().getDimensions() );
            }
            styles.putAll( lay.getMetadata().getStyles() );
            legendStyles.putAll( lay.getMetadata().getLegendStyles() );
            types.addAll( lay.getMetadata().getFeatureTypes() );
            lays.add( lay );
        }
        List<Theme> thms = new ArrayList<Theme>( themes.size() );
        for ( ThemeType tt : themes ) {
            thms.add( buildTheme( tt, tt.getLayer(), tt.getTheme(), stores ) );
        }

        SpatialMetadata smd = SpatialMetadataConverter.fromJaxb( current.getEnvelope(), current.getCRS() );
        Description desc = DescriptionConverter.fromJaxb( current.getTitle(), current.getAbstract(),
                                                          current.getKeywords() );

        LayerMetadata md = new LayerMetadata( current.getIdentifier(), desc, smd );
        md.setDimensions( dims );
        md.setStyles( styles );
        md.setLegendStyles( legendStyles );
        return new StandardTheme( md, thms, lays );
    }

    private static Theme buildAutoTheme( Layer layer ) {
        LayerMetadata md = new LayerMetadata( null, null, null );
        LayerMetadata lmd = layer.getMetadata();
        md.merge( lmd );
        md.setDimensions( new LinkedHashMap<String, Dimension<?>>( lmd.getDimensions() ) );
        md.setStyles( new LinkedHashMap<String, Style>( lmd.getStyles() ) );
        md.setLegendStyles( new LinkedHashMap<String, Style>( lmd.getLegendStyles() ) );
        return new StandardTheme( md, Collections.<Theme> emptyList(), singletonList( layer ) );
    }

    private static Theme buildAutoTheme( String id, LayerStore store ) {
        Description desc = new Description( id, singletonList( new LanguageString( id, null ) ), null, null );
        LayerMetadata md = new LayerMetadata( null, desc, new SpatialMetadata(null, Collections.<ICRS>emptyList()) );
        List<Theme> themes = new ArrayList<Theme>();

        for ( Layer l : store.getAll() ) {
            themes.add( buildAutoTheme( l ) );
        }

        return new StandardTheme( md, themes, new ArrayList<Layer>() );
    }

    private static Theme buildAutoTheme( Map<String, LayerStore> stores ) {
        Description desc = new Description( null, Collections.singletonList( new LanguageString( "root", null ) ),
                                            null, null );
        LayerMetadata md = new LayerMetadata( null, desc, new SpatialMetadata(null, Collections.<ICRS>emptyList()) );
        List<Theme> themes = new ArrayList<Theme>();

        for ( Entry<String, LayerStore> e : stores.entrySet() ) {
            themes.add( buildAutoTheme( e.getKey(), e.getValue() ) );
        }

        return new StandardTheme( md, themes, new ArrayList<Layer>() );
    }

    @Override
    public Theme create( URL configUrl )
                            throws ResourceInitException {
        String pkg = "org.deegree.theme.persistence.standard.jaxb";
        try {
            Themes cfg;
            cfg = (Themes) unmarshall( pkg, SCHEMA_URL, configUrl, workspace );

            List<String> storeIds = cfg.getLayerStoreId();
            Map<String, LayerStore> stores = new LinkedHashMap<String, LayerStore>( storeIds.size() );
            LayerStoreManager mgr = workspace.getSubsystemManager( LayerStoreManager.class );

            for ( String id : storeIds ) {
                LayerStore store = mgr.get( id );
                if ( store == null ) {
                    LOG.warn( "Layer store with id {} is not available.", id );
                    continue;
                }
                stores.put( id, store );
            }

            ThemeType root = cfg.getTheme();
            Theme theme;
            if ( root == null ) {
                theme = buildAutoTheme( stores );
            } else {
                theme = buildTheme( root, root.getLayer(), root.getTheme(), stores );
            }
            aggregateSpatialMetadata( theme );
            return theme;
        } catch ( Throwable e ) {
            throw new ResourceInitException( "Could not parse theme configuration file.", e );
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
    }

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/themes/standard";
    }

    @Override
    public URL getConfigSchema() {
        return SCHEMA_URL;
    }

}
