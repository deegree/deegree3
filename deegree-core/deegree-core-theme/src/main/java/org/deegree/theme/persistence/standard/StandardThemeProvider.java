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
import static org.deegree.theme.Themes.aggregateSpatialMetadata;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.LayerStoreProvider;
import org.deegree.theme.Theme;
import org.deegree.theme.persistence.ThemeProvider;
import org.deegree.theme.persistence.standard.jaxb.ThemeType;
import org.deegree.theme.persistence.standard.jaxb.Themes;
import org.slf4j.Logger;

/**
 * Provider for standard themes.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class StandardThemeProvider implements ThemeProvider {

    private static final Logger LOG = getLogger( StandardThemeProvider.class );

    private static final URL SCHEMA_URL = StandardThemeProvider.class.getResource( "/META-INF/schemas/themes/3.2.0/themes.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
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

            for ( String id : storeIds ) {
                LayerStore store = workspace.getNewWorkspace().getResource( LayerStoreProvider.class, id );
                if ( store == null ) {
                    LOG.warn( "Layer store with id {} is not available.", id );
                    continue;
                }
                stores.put( id, store );
            }

            ThemeType root = cfg.getTheme();
            Theme theme;
            if ( root == null ) {
                theme = StandardThemeBuilder.buildAutoTheme( stores );
            } else {
                theme = StandardThemeBuilder.buildTheme( root, root.getLayer(), root.getTheme(), stores );
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
