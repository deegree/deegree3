//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.feature.persistence;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;

import org.deegree.commons.config.AbstractResourceManager;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.DefaultResourceManagerMetadata;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceManagerMetadata;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.persistence.cache.BBoxCache;
import org.deegree.feature.persistence.cache.BBoxPropertiesCache;
import org.deegree.filter.function.FunctionManager;
import org.slf4j.Logger;

/**
 * Entry point for creating and retrieving {@link FeatureStore} providers and instances.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureStoreManager extends AbstractResourceManager<FeatureStore> {

    private static final Logger LOG = getLogger( FeatureStoreManager.class );

    private static final String BBOX_CACHE_FILE = "bbox_cache.properties";

    private BBoxPropertiesCache bboxCache;

    private FeatureStoreManagerMetadata metadata;

    @Override
    public void initMetadata( DeegreeWorkspace workspace ) {
        metadata = new FeatureStoreManagerMetadata( workspace );
    }

    @Override
    public void startup( DeegreeWorkspace workspace )
                            throws ResourceInitException {
        try {
            File dir = new File( workspace.getLocation(), metadata.getPath() );
            bboxCache = new BBoxPropertiesCache( new File( dir, BBOX_CACHE_FILE ) );
        } catch ( IOException e ) {
            LOG.error( "Unable to initialize global envelope cache: " + e.getMessage(), e );
        }

        // stores startup
        super.startup( workspace );
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { ProxyUtils.class, FunctionManager.class, CRSManager.class };
    }

    static class FeatureStoreManagerMetadata extends DefaultResourceManagerMetadata<FeatureStore> {
        FeatureStoreManagerMetadata( DeegreeWorkspace workspace ) {
            super( "feature stores", "datasources/feature/", FeatureStoreProvider.class, workspace );
        }
    }

    @Override
    public ResourceManagerMetadata<FeatureStore> getMetadata() {
        return metadata;
    }

    @Override
    public void shutdown() {
        // workspace.getSubsystemManager( ConnectionManager.class ).deactivate( "LOCK_DB" );
    }

    public BBoxCache getBBoxCache() {
        return bboxCache;
    }

}
