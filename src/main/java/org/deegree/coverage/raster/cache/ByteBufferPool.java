//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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

package org.deegree.coverage.raster.cache;

import static org.slf4j.LoggerFactory.getLogger;

import java.nio.ByteBuffer;

import org.slf4j.Logger;

/**
 * The <code>ByteBufferPool</code> will be the central place for buffering byte buffers used for rasters. Currently only
 * new byte buffers are created, no pooling is done.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class ByteBufferPool {
    private static final Logger LOG = getLogger( ByteBufferPool.class );

    /**
     * Frees up raster memory and than allocates the ByteBuffer.
     * 
     * @param size
     * @param direct
     * @param forCache
     * @return an allocated byte buffer of the given size.
     */
    public static ByteBuffer allocate( int size, boolean direct, boolean forCache ) {
        if ( forCache ) {
            LOG.debug( "Requested{}memory: {} MB", ( forCache ? " cache " : " " ), ( size / ( 1024 * 1024d ) ) );
            RasterCache.freeMemory( size );
        }
        if ( direct ) {
            return ByteBuffer.allocateDirect( size );
        }

        return ByteBuffer.allocate( size );
    }

    /**
     * @param size
     * @param directAllocation
     *            if the allocation should be direct
     * @return the newly created ByteBuffer.
     */
    public static ByteBuffer allocate( int size, boolean directAllocation ) {
        return allocate( size, directAllocation, false );
    }
}
