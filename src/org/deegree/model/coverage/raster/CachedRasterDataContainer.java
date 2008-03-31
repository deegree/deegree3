//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/
package org.deegree.model.coverage.raster;

import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.model.coverage.raster.data.RasterData;


/**
 * This class implements a cached RasterDataContainer.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class CachedRasterDataContainer implements RasterDataContainer {

    private RasterReader reader;

    private String identifier;

    private static Log log = LogFactory.getLog( CachedRasterDataContainer.class );

    private static Cache cache;

    private final static String CACHENAME = "SimpleRasterCache";

    static {
        CacheManager manager = CacheManager.create();
        // see ehcachel.xml for SimpleRasterCache configuration
        cache = manager.getCache( CACHENAME );
    }

    /**
     * Creates a RasterDataContainer that loads the data on first access.
     * 
     * @param reader
     *            RasterReader for the raster source
     */
    public CachedRasterDataContainer( RasterReader reader ) {
        this.reader = reader;
        this.identifier = UUID.randomUUID().toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.raster.RasterDataContainer#getRasterData()
     */
    public RasterData getRasterData() {
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
}
