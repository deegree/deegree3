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

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.layer.Layer;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.LayerStoreManager;
import org.deegree.theme.Theme;
import org.deegree.theme.persistence.ThemeProvider;
import org.deegree.themes.persistence.standard.jaxb.ThemeType;
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

    private Theme buildTheme( String identifier, List<String> layers, List<ThemeType> themes )
                            throws ResourceInitException {
        List<Layer> lays = new ArrayList<Layer>( layers.size() );
        LayerStoreManager mgr = workspace.getSubsystemManager( LayerStoreManager.class );
        for ( String l : layers ) {
            LayerStore lay = mgr.get( l );
            if ( lay == null ) {
                LOG.warn( "Layer store with id {} is not available.", l );
                continue;
            }
            lays.addAll( lay.getAll() );
        }
        List<Theme> thms = new ArrayList<Theme>( themes.size() );
        for ( ThemeType tt : themes ) {
            thms.add( buildTheme( tt.getIdentifier(), tt.getLayerRef(), tt.getTheme() ) );
        }
        return new StandardTheme( identifier, thms, lays );
    }

    @Override
    public Theme create( URL configUrl )
                            throws ResourceInitException {
        String pkg = "org.deegree.theme.persistence.standard.jaxb";
        try {
            org.deegree.themes.persistence.standard.jaxb.Theme cfg;
            cfg = (org.deegree.themes.persistence.standard.jaxb.Theme) unmarshall( pkg, SCHEMA_URL, configUrl,
                                                                                   workspace );
            return buildTheme( cfg.getIdentifier(), cfg.getLayerRef(), cfg.getTheme() );
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
