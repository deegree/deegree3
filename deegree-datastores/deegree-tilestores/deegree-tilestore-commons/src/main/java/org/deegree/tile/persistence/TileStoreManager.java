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
package org.deegree.tile.persistence;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import org.deegree.commons.config.AbstractResourceManager;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.DefaultResourceManagerMetadata;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceManagerMetadata;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.tile.tilematrixset.TileMatrixSetManager;
import org.slf4j.Logger;

/**
 * {@link ResourceManager} for {@link TileStore} resources.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
@SuppressWarnings("unchecked")
public class TileStoreManager extends AbstractResourceManager<TileStore> {

    private static final Logger LOG = getLogger( TileStoreManager.class );

    private TileStoreManagerMetadata metadata;

    @Override
    public void initMetadata( DeegreeWorkspace workspace ) {
        metadata = new TileStoreManagerMetadata( workspace );
    }

    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { ProxyUtils.class, CRSManager.class, TileMatrixSetManager.class };
    }

    static class TileStoreManagerMetadata extends DefaultResourceManagerMetadata<TileStore> {
        TileStoreManagerMetadata( DeegreeWorkspace workspace ) {
            super( "tile stores", "datasources/tile/", TileStoreProvider.class, workspace );
        }
    }

    @Override
    public ResourceManagerMetadata<TileStore> getMetadata() {
        return metadata;
    }

    @Override
    public List<File> getFiles() {
        List<File> files = super.getFiles();

        List<File> result = new ArrayList<File>();

        Map<File, List<File>> deps = getDependencies( files );

        // add stores with no dependencies first
        for ( Entry<File, List<File>> e : deps.entrySet() ) {
            if ( e.getValue().isEmpty() ) {
                result.add( e.getKey() );
                files.remove( e.getKey() );
            }
        }

        orderDependencies( files, deps, result );

        return result;
    }

    private void orderDependencies( List<File> files, Map<File, List<File>> deps, List<File> result ) {
        boolean changed = false;
        while ( !files.isEmpty() ) {
            changed = false;
            ListIterator<File> iter = files.listIterator();
            while ( iter.hasNext() ) {
                File f = iter.next();
                List<File> list = deps.get( f );
                if ( list == null || result.containsAll( list ) ) {
                    changed = true;
                    result.add( f );
                    iter.remove();
                }
            }
            if ( !changed ) {
                LOG.warn( "Circular or broken dependencies within tile stores, not all tile stores will be started up!" );
                break;
            }
        }
    }

    private Map<File, List<File>> getDependencies( List<File> files ) {
        Map<File, List<File>> deps = new HashMap<File, List<File>>();
        for ( File f : files ) {
            try {
                TileStoreProvider p = (TileStoreProvider) this.getProvider( f.toURI().toURL() );
                if ( p != null ) {
                    deps.put( f, p.getTileStoreDependencies( f ) );
                }
            } catch ( MalformedURLException e ) {
                // ignore
            }
        }
        return deps;
    }

}
