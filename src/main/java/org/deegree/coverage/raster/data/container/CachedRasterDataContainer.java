//$HeadURL: svn+ssh://otonnhofer@svn.wald.intevation.org/deegree/deegree3/model/trunk/src/org/deegree/model/coverage/raster/CachedRasterDataContainer.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.coverage.raster.data.container;

import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.container.RasterDataContainerFactory.LoadingPolicy;
import org.deegree.coverage.raster.io.RasterDataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a cached RasterDataContainer.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: otonnhofer $
 * 
 * @version $Revision: 10847 $, $Date: 2008-03-31 15:54:40 +0200 (Mon, 31 Mar 2008) $
 */
public class CachedRasterDataContainer implements RasterDataContainer, RasterDataContainerProvider {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( CachedRasterDataContainer.class );

    private RasterDataReader reader;

    private String identifier;

    private static Logger log = LoggerFactory.getLogger( CachedRasterDataContainer.class );

    private static Cache cache;

    private final static String CACHENAME = "CachedRasterDataContainer";

    static {
        try {
            CacheManager manager = CacheManager.create();
            manager.addCache( CACHENAME );
            // TODO: make cachename configurable
            // see ehcache.xml for CachedRasterDataContainer configuration
            cache = manager.getCache( CACHENAME );
        } catch ( Throwable e ) {
            LOG.error( e.getLocalizedMessage(), e );
        }

    }

    /**
     * Creates an empty RasterDataContainer that loads the data on first access.
     */
    public CachedRasterDataContainer() {
        // empty constructor
    }

    /**
     * Creates a RasterDataContainer that loads the data on first access.
     * 
     * @param reader
     *            RasterReader for the raster source
     */
    public CachedRasterDataContainer( RasterDataReader reader ) {
        setRasterDataReader( reader );
    }

    public void setRasterDataReader( RasterDataReader reader ) {
        // reader.close();
        this.reader = reader;
        this.identifier = UUID.randomUUID().toString();
    }

    @Override
    public synchronized RasterData getRasterData() {
        // synchronized to prevent multiple reader.read()-calls when
        RasterData raster;
        if ( log.isDebugEnabled() )
            log.debug( "accessing: " + this.toString() );
        Element elem = cache.get( identifier );
        if ( elem == null ) {
            raster = reader.read();
            elem = new Element( identifier, raster );
            cache.put( elem );
            if ( log.isDebugEnabled() )
                log.debug( "cache miss: " + this.toString() + "#mem: " + cache.getMemoryStoreSize() );
        } else {
            raster = (RasterData) elem.getObjectValue();
            if ( log.isDebugEnabled() )
                log.debug( "cache hit: " + this.toString() );
        }
        return raster;
    }

    @Override
    public RasterData getReadOnlyRasterData() {
        return getRasterData().asReadOnly();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.raster.RasterDataContainer#getColumns()
     */
    public int getColumns() {
        return reader.getWidth();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.raster.RasterDataContainer#getRows()
     */
    public int getRows() {
        return reader.getWidth();
    }

    public RasterDataContainer getRasterDataContainer( LoadingPolicy type ) {
        if ( type == LoadingPolicy.CACHED && cache != null ) {
            // the service loader caches provider instances, so return a new instance
            return new CachedRasterDataContainer();
        }
        return null;
    }

}
