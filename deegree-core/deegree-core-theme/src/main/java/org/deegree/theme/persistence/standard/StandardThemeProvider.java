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
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.layer.Layer;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.LayerStoreManager;
import org.deegree.protocol.ows.metadata.Description;
import org.deegree.protocol.wms.metadata.LayerMetadata;
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

    private static final URL SCHEMA_URL = StandardThemeProvider.class.getResource( "/META-INF/schemas/themes/3.1.0/themes.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    private Theme buildTheme( ThemeType current, List<String> layers, List<ThemeType> themes, List<LayerStore> stores )
                            throws ResourceInitException {
        List<Layer> lays = new ArrayList<Layer>( layers.size() );

        LayerMetadata md = new LayerMetadata();

        for ( String l : layers ) {
            Layer lay = null;
            for ( LayerStore s : stores ) {
                lay = s.get( l );
                if ( lay != null ) {
                    break;
                }
            }
            if ( lay == null ) {
                LOG.warn( "Layer with identifier {} is not available from any layer store.", l );
                continue;
            }
            md.setEnvelope( lay.getMetadata().getEnvelope() );
            md.setCoordinateSystems( lay.getMetadata().getCoordinateSystems() );
            lays.add( lay );
        }
        // TODO proper aggregation of envelope/crs and possibly other metadata
        List<Theme> thms = new ArrayList<Theme>( themes.size() );
        for ( ThemeType tt : themes ) {
            thms.add( buildTheme( tt, tt.getLayer(), tt.getTheme(), stores ) );
        }
        md.setName( current.getIdentifier() );
        Description desc = new Description();
        desc.setTitle( singletonList( new LanguageString( current.getTitle(), null ) ) );
        String crss = current.getCRS();
        if ( crss != null ) {
            List<ICRS> list = new ArrayList<ICRS>();
            String[] cs = crss.trim().split( "[\\s\n]+" );
            for ( String c : cs ) {
                if ( !c.isEmpty() ) {
                    list.add( CRSManager.getCRSRef( c ) );
                }
            }
            md.setCoordinateSystems( list );
        }
        md.setDescription( desc );
        return new StandardTheme( md, thms, lays );
    }

    @Override
    public Theme create( URL configUrl )
                            throws ResourceInitException {
        String pkg = "org.deegree.theme.persistence.standard.jaxb";
        try {
            Themes cfg;
            cfg = (Themes) unmarshall( pkg, SCHEMA_URL, configUrl, workspace );

            List<String> storeIds = cfg.getLayerStoreId();
            List<LayerStore> stores = new ArrayList<LayerStore>( storeIds.size() );
            LayerStoreManager mgr = workspace.getSubsystemManager( LayerStoreManager.class );

            for ( String id : storeIds ) {
                LayerStore store = mgr.get( id );
                if ( store == null ) {
                    LOG.warn( "Layer store with id {} is not available.", id );
                    continue;
                }
                stores.add( store );
            }

            ThemeType root = cfg.getTheme();
            return buildTheme( root, root.getLayer(), root.getTheme(), stores );
        } catch ( Throwable e ) {
            throw new ResourceInitException( "Could not parse theme configuration file.", e );
        }
    }

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
