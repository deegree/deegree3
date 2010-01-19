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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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

    private final SortedMap<Integer, Set<PooledByteBuffer>> freeBuffers = new TreeMap<Integer, Set<PooledByteBuffer>>();

    private String name;

    /**
     * Construct a direct byte buffer which may allocate buffers with given capacity
     * 
     * @param capacityLimit
     *            total capacity of this pool.
     * @param bufferLimit
     *            the number of buffers
     * @param name
     */
    public DirectByteBufferPool( int capacityLimit, int bufferLimit, String name ) {
        this.MAX_MEMORY_CAPACITY = capacityLimit;
        this.MAX_NUM_OF_BUFFERS = bufferLimit;
        this.name = name;
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
        StringBuilder sb = new StringBuilder( name );
        sb.append( "| numBuffers: " ).append( numBuffers );
        sb.append( "| requested: " ).append( capacity );
        sb.append( "| tailMap: " ).append( freeBuffers );
        sb.append( "| freebuffers: " ).append( this.freeBuffers.size() );
        sb.append( "| allBuffers: " ).append( allBuffers.size() );
        // System.out.println( sb.toString() );
        if ( freeBuffers != null && freeBuffers.size() > 0 ) {
            // if ( name.startsWith( "static" ) && this.numBuffers > 202 ) {
            // Thread.dumpStack();
            // System.exit( 1 );
            // }

            buffer = freeBuffers.iterator().next();
            buffer.getBuffer().rewind();
            freeBuffers.remove( buffer );
        } else {
            if ( numBuffers >= MAX_NUM_OF_BUFFERS ) {
                String msg = name + "Maximum number of direct buffers (=" + MAX_NUM_OF_BUFFERS + ") exceeded";
                throw new OutOfMemoryError( msg );
            }
            if ( totalCapacity + capacity > MAX_MEMORY_CAPACITY ) {
                String msg = name + "Maximum memory size for direct buffers (=" + MAX_MEMORY_CAPACITY + ") exceeded";
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
     * @param capacity
     * @return the requested byte buffer
     * 
     * @throws OutOfMemoryError
     *             if no PooledByteBuffer with the given capacity is available and allocating it from the system would
     *             exceed the assigned resources
     */
    public synchronized PooledByteBuffer allocate_( int capacity )
                            throws OutOfMemoryError {

        PooledByteBuffer buffer = null;

        // get all free byte buffers with at least the given capacity
        SortedMap<Integer, Set<PooledByteBuffer>> tailMap = this.freeBuffers.tailMap( capacity );

        StringBuilder sb = new StringBuilder( name );
        sb.append( "| numBuffers: " ).append( numBuffers );
        sb.append( "| requested: " ).append( capacity );
        sb.append( "| tailMap: " ).append( tailMap );
        sb.append( "| freebuffers: " ).append( freeBuffers.size() );
        sb.append( "| allBuffers: " ).append( allBuffers.size() );

        // System.out.println( sb.toString() );
        // Set<PooledByteBuffer> freeBuffers = this.freeBuffers.get( capacity );
        // if ( freeBuffers != null && freeBuffers.size() > 0 ) {
        if ( tailMap != null && !tailMap.isEmpty() ) {
            Iterator<Set<PooledByteBuffer>> tailIt = tailMap.values().iterator();
            while ( tailIt.hasNext() && buffer == null ) {
                Set<PooledByteBuffer> frBuffers = tailIt.next();
                if ( frBuffers != null && !frBuffers.isEmpty() ) {
                    Iterator<PooledByteBuffer> frBufIt = frBuffers.iterator();
                    while ( frBufIt.hasNext() && buffer == null ) {
                        buffer = frBufIt.next();
                        if ( buffer != null ) {
                            buffer.clear();
                            // set the limit
                            buffer.limit( capacity );
                        }
                    }
                    if ( buffer != null ) {
                        frBuffers.remove( buffer );
                    }
                }
            }
        }
        if ( buffer == null ) {
            if ( numBuffers >= MAX_NUM_OF_BUFFERS ) {
                String msg = name + ": Maximum number of direct buffers (=" + MAX_NUM_OF_BUFFERS + ") exceeded";
                throw new OutOfMemoryError( msg );
            }
            if ( totalCapacity + capacity > MAX_MEMORY_CAPACITY ) {
                String msg = name + ":Maximum memory size for direct buffers (=" + MAX_MEMORY_CAPACITY + ") exceeded";
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
        // System.out.println( name + ":deallocate: " + buffer.capacity() );
        if ( buffer != null ) {
            if ( !allBuffers.contains( buffer ) ) {
                String msg = name + ":Buffer to be deallocated (" + buffer + ") has not been allocated using the pool.";
                LOG.warn( msg );
                buffer.markAsFree();
                // throw new IllegalArgumentException( msg );
            } else {
                int capacity = buffer.capacity();
                Set<PooledByteBuffer> buffers = freeBuffers.get( capacity );
                if ( buffers == null ) {
                    buffers = new HashSet<PooledByteBuffer>();
                    freeBuffers.put( capacity, buffers );
                }
                buffers.add( buffer );
            }
        }
    }

    @Override
    public String toString() {
        return "Number of buffers: " + numBuffers + ", total capacity: " + totalCapacity;
    }
}
