//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.commons.utils.nio;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>DirectByteBufferPool</code> pools a number of direct 'native' bytebuffers so they can be reused.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class DirectByteBufferPool {

    private static Logger LOG = LoggerFactory.getLogger( DirectByteBufferPool.class );

    private final int MAX_MEMORY_CAPACITY;

    private final int MAX_NUM_OF_BUFFERS;

    private long totalCapacity;

    private int numBuffers;

    private final Set<PooledByteBuffer> allBuffers = new HashSet<PooledByteBuffer>();

    private final Map<Integer, Set<PooledByteBuffer>> freeBuffers = new HashMap<Integer, Set<PooledByteBuffer>>();

    /**
     * Construct a direct byte buffer which may allocate buffers with given capacity
     * 
     * @param capacityLimit
     *            total capacity of this pool.
     * @param bufferLimit
     *            the number of buffers
     */
    public DirectByteBufferPool( int capacityLimit, int bufferLimit ) {
        this.MAX_MEMORY_CAPACITY = capacityLimit;
        this.MAX_NUM_OF_BUFFERS = bufferLimit;
    }

    /**
     * @param capacity
     * @return the requested byte buffer
     * 
     * @throws OutOfMemoryError
     *             if no PooledByteBuffer with the given capacity is available and allocating it from the system would
     *             exceed the assigned resources
     */
    public synchronized PooledByteBuffer allocate( int capacity )
                            throws OutOfMemoryError {

        PooledByteBuffer buffer;

        Set<PooledByteBuffer> freeBuffers = this.freeBuffers.get( capacity );
        if ( freeBuffers != null && freeBuffers.size() > 0 ) {
            buffer = freeBuffers.iterator().next();
            buffer.getBuffer().rewind();
            freeBuffers.remove( buffer );
        } else {
            if ( numBuffers >= MAX_NUM_OF_BUFFERS ) {
                String msg = "Maximum number of direct buffers (=" + MAX_NUM_OF_BUFFERS + ") exceeded";
                throw new OutOfMemoryError( msg );
            }
            if ( totalCapacity + capacity > MAX_MEMORY_CAPACITY ) {
                String msg = "Maximum memory size for direct buffers (=" + MAX_MEMORY_CAPACITY + ") exceeded";
                throw new OutOfMemoryError( msg );
            }
            buffer = new PooledByteBuffer( capacity, this );

            numBuffers++;
            totalCapacity += capacity;
            allBuffers.add( buffer );
            LOG.debug( "New buffer: " + buffer );
        }
        return buffer;
    }

    /**
     * Notifies the pool the given buffer is free for use.
     * 
     * @param buffer
     *            to be freed.
     */
    public synchronized void deallocate( PooledByteBuffer buffer ) {
        if ( !allBuffers.contains( buffer ) ) {
            String msg = "Buffer to be deallocated (" + buffer + ") has not been allocated using the pool.";
            throw new IllegalArgumentException( msg );
        }
        int capacity = buffer.getBuffer().capacity();
        Set<PooledByteBuffer> buffers = freeBuffers.get( capacity );
        if ( buffers == null ) {
            buffers = new HashSet<PooledByteBuffer>();
            freeBuffers.put( capacity, buffers );
        }
        buffers.add( buffer );
    }

    @Override
    public String toString() {
        return "Number of buffers: " + numBuffers + ", total capacity: " + totalCapacity;
    }
}
