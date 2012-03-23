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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.cache;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import net.sf.ehcache.CacheManager;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreManager;
import org.deegree.tile.persistence.TileStoreProvider;

/**
 * The <code>GeoTIFFTileStoreProvider</code> provides a <code>TileMatrixSet</code> out of a GeoTIFF file (tiled
 * BIGTIFF).
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class CachingTileStoreProvider implements TileStoreProvider {

    private static final URL SCHEMA = CachingTileStoreProvider.class.getResource( "/META-INF/schemas/datasource/tile/cache/3.2.0/cache.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @Override
    public CachingTileStore create( URL configUrl )
                            throws ResourceInitException {
        try {
            org.deegree.tile.persistence.cache.jaxb.CachingTileStore cfg;
            cfg = (org.deegree.tile.persistence.cache.jaxb.CachingTileStore) unmarshall( "org.deegree.tile.persistence.cache.jaxb",
                                                                                         SCHEMA, configUrl, workspace );

            TileStoreManager mgr = workspace.getSubsystemManager( TileStoreManager.class );
            TileStore ts = mgr.get( cfg.getTileStoreId() );
            if ( ts == null ) {
                throw new ResourceInitException( "The tile store with id " + cfg.getTileStoreId()
                                                 + " is not available." );
            }

            String cache = cfg.getCacheConfiguration();
            File f = new File( cache );
            if ( !f.isAbsolute() ) {
                f = new File( new File( configUrl.toURI() ), cache );
            }
            CacheManager cmgr = new CacheManager( f.toURI().toURL() );

            return new CachingTileStore( ts, cmgr, cfg.getCacheName() );
        } catch ( Throwable e ) {
            throw new ResourceInitException( "Unable to create tile store.", e );
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
    }

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/datasource/tile/cache";
    }

    @Override
    public URL getConfigSchema() {
        return SCHEMA;
    }

    @Override
    public List<File> getTileStoreDependencies( File config ) {
        try {
            org.deegree.tile.persistence.cache.jaxb.CachingTileStore p;
            p = (org.deegree.tile.persistence.cache.jaxb.CachingTileStore) unmarshall( "org.deegree.tile.persistence.cache.jaxb",
                                                                                       SCHEMA, config.toURI().toURL(),
                                                                                       workspace );
            return Collections.<File> singletonList( new File( config.getParentFile(), p.getTileStoreId() + ".xml" ) );
        } catch ( Throwable e ) {
            // ignore here, will be parsed again anyway
        }
        return Collections.<File> emptyList();
    }

}
