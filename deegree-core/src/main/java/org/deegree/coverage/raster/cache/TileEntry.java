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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.coverage.raster.cache;

import java.nio.ByteBuffer;

import org.deegree.coverage.raster.geom.RasterRect;

/**
 * A tile entry is one tile of a cached Raster. The cached raster is gridified, each grid is a tile, represented by this
 * class.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class TileEntry {
    // the memory buffer containing the data of this buffer.
    private ByteBuffer memoryTile;

    // time this tile was written to cache.
    private long writtenToCache;

    // time this tile was read from the original raster
    private long readFromOriginal;

    private RasterRect rasterRect;

    /**
     * A new tile entry valid for the given raster rect.
     * 
     * @param rect
     *            of this tile.
     */
    public TileEntry( RasterRect rect ) {
        this.memoryTile = null;
        this.writtenToCache = 0;
        this.readFromOriginal = 0;
        this.rasterRect = rect;
    }

    /**
     * @param buffer
     *            containing data of this tile.
     * @return the new memory size of this tile, if the given buffer was <code>null</code> the returned size will be 0,
     *         buffer.capacity otherwise.
     */
    public long setBuffer( ByteBuffer buffer ) {
        long result = 0;
        if ( buffer != null ) {
            if ( this.memoryTile == null ) {
                result = buffer.capacity();
            }
        } else {
            if ( this.memoryTile != null ) {
                result = -this.memoryTile.capacity();
            }
        }
        this.memoryTile = buffer;
        readFromOriginal = this.memoryTile == null ? 0 : System.currentTimeMillis();
        return result;
    }

    /**
     * @return the byte buffer of this tile or <code>null</code> if no such memory buffer is available.
     */
    public ByteBuffer getBuffer() {
        return memoryTile == null ? null : memoryTile.asReadOnlyBuffer();
    }

    /**
     * Delete the memory buffer of this tile, implicit reset of {@link #getReadTime}.
     * 
     * @return the size of the freed memory
     */
    public long deteleBuffer() {
        if ( this.memoryTile == null ) {
            return 0;
        }
        long result = memoryTile.capacity();
        this.readFromOriginal = 0;
        memoryTile = null;
        return result;
    }

    /**
     * @return the time the memory buffer was set (was read from the cached file), or 0 if no information about access
     *         time is available.
     */
    public final long getReadTime() {
        return readFromOriginal;
    }

    /**
     * @return true if this tile is marked as written to the cache.
     */
    public boolean isOnFile() {
        return writtenToCache > 0;
    }

    /**
     * @param isOnFile
     */
    public void setTileOnFile( boolean isOnFile ) {
        writtenToCache = isOnFile ? System.currentTimeMillis() : 0;
    }

    /**
     * Mark this tile as cleared, e.g. no memory buffer, no last access time, no tile on file.
     * 
     * @param clearFileTime
     *            if true, this tile is marked as not on file (tile write time is set to 0).
     * @return the memory freed up by this clear call.
     * 
     */
    public long clear( boolean clearFileTime ) {
        long result = memoryTile == null ? 0 : memoryTile.capacity();
        memoryTile = null;
        readFromOriginal = 0;
        writtenToCache = clearFileTime ? 0 : writtenToCache;
        return result;
    }

    /**
     * @return true if this
     */
    public boolean isInMemory() {
        return this.memoryTile != null;
    }

    /**
     * @return the rasterRect
     */
    public final RasterRect getRasterRect() {
        return rasterRect;
    }
}