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
import java.util.Iterator;
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

    private long totalCapacity;

    // private final Set<PooledByteBuffer> allBuffers = new HashSet<PooledByteBuffer>();

    // private final SortedMap<Integer, Set<PooledByteBuffer>> freeBuffers = new TreeMap<Integer,
    // Set<PooledByteBuffer>>();

    private final Set<PooledByteBuffer> allBuffers = new HashSet<PooledByteBuffer>();

    private final Map<Integer, Set<PooledByteBuffer>> freeBuffers = new HashMap<Integer, Set<PooledByteBuffer>>();

    private String name;

    private int id = 0;

    /**
     * Construct a direct byte buffer which may allocate buffers with given capacity
     * 
     * @param capacityLimit
     *            total capacity of this pool.
     * @param name
     */
    public DirectByteBufferPool( int capacityLimit, String name ) {
        this.MAX_MEMORY_CAPACITY = capacityLimit;
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

        PooledByteBuffer buffer = null;

        Set<PooledByteBuffer> availableBuffers = this.freeBuffers.get( capacity );
        // System.out.println( sb.toString() );
        if ( availableBuffers != null && !availableBuffers.isEmpty() ) {
            if ( LOG.isDebugEnabled() ) {
                StringBuilder sb = new StringBuilder( name );
                sb.append( ":(" ).append( allBuffers.size() ).append( ")| found a freebuffer entry for capacity: " );
                sb.append( capacity ).append( ", buffer has size(): " + availableBuffers.size() );
                LOG.debug( sb.toString() );
            }
            buffer = availableBuffers.iterator().next();
            buffer.clear();
            availableBuffers.remove( buffer );
        } else {
            // test if a larger buffer is free and retrieve that.
            if ( !freeBuffers.isEmpty() ) {
                for ( Integer cap : freeBuffers.keySet() ) {
                    if ( cap != null && cap >= capacity ) {
                        availableBuffers = freeBuffers.get( cap );
                        if ( !availableBuffers.isEmpty() ) {
                            if ( LOG.isDebugEnabled() ) {
                                StringBuilder sb = new StringBuilder( name );
                                sb.append( ":(" ).append( allBuffers.size() ).append( ")| found a larger (" );
                                sb.append( cap ).append( ") freebuffer entry for capacity: " );
                                sb.append( capacity ).append( ", buffer has size(): " + availableBuffers.size() );
                                LOG.debug( sb.toString() );
                            }
                            buffer = availableBuffers.iterator().next();
                            buffer.clear();
                            buffer.limit( capacity );
                            availableBuffers.remove( buffer );
                            break;
                        }
                    }
                }
            }
            if ( buffer == null ) {
                if ( LOG.isDebugEnabled() ) {
                    StringBuilder sb = new StringBuilder( name );
                    sb.append( ":(" ).append( allBuffers.size() ).append( ")| no freebuffer entry for capacity: " );
                    sb.append( capacity ).append( ", creating new direct buffer." );
                    LOG.debug( sb.toString() );
                }
                if ( totalCapacity + capacity > MAX_MEMORY_CAPACITY ) {
                    String msg = name + ": Maximum memory size for direct buffers (=" + MAX_MEMORY_CAPACITY
                                 + ") exceeded, requested: " + capacity + ", freebuffers: " + this.freeBuffers.size()
                                 + " totalBuffers: " + allBuffers.size();
                    throw new OutOfMemoryError( msg );
                }
                buffer = new PooledByteBuffer( capacity, this, id++ );

                totalCapacity += capacity;
                allBuffers.add( buffer );
            }
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
        Map<Integer, Set<PooledByteBuffer>> tailMap = new HashMap<Integer, Set<PooledByteBuffer>>();// this.freeBuffers.tailMap(
        // capacity );
        // Map<Integer, Set<PooledByteBuffer>> tailMap = this.freeBuffers;

        // Set<PooledByteBuffer> freeBuffers = this.freeBuffers.get( capacity );
        // if ( freeBuffers != null && freeBuffers.size() > 0 ) {
        if ( /* tailMap != null && */!tailMap.isEmpty() ) {
            Iterator<Set<PooledByteBuffer>> tailIt = tailMap.values().iterator();
            while ( tailIt.hasNext() && buffer == null ) {
                Set<PooledByteBuffer> frBuffers = tailIt.next();
                if ( frBuffers != null && !frBuffers.isEmpty() ) {
                    Iterator<PooledByteBuffer> frBufIt = frBuffers.iterator();
                    while ( buffer == null && frBufIt.hasNext() ) {
                        buffer = frBufIt.next();
                        if ( buffer != null ) {
                            buffer.clear();
                            // set the limit
                            buffer.limit( capacity );
                        }
                    }
                }
                if ( buffer != null && frBuffers != null ) {
                    frBuffers.remove( buffer );
                }
            }
            if ( LOG.isDebugEnabled() ) {
                StringBuilder sb = new StringBuilder( name );
                sb.append( "| Found a free Buffer!" );
                sb.append( "| requested: " ).append( capacity );
                sb.append( "| tailMap: " ).append( tailMap );
                sb.append( "| freebuffers: " ).append( freeBuffers.size() );
                sb.append( "| allBuffers: " ).append( allBuffers.size() );
                LOG.debug( sb.toString() );
            }
        }
        if ( buffer == null ) {
            if ( LOG.isDebugEnabled() ) {
                StringBuilder sb = new StringBuilder( name );
                sb.append( "| requested: " ).append( capacity );
                sb.append( "| tailMap: " ).append( tailMap );
                sb.append( "| freebuffers: " ).append( freeBuffers.size() );
                sb.append( "| allBuffers: " ).append( allBuffers.size() );
                LOG.debug( sb.toString() );
            }
            if ( totalCapacity + capacity > MAX_MEMORY_CAPACITY ) {
                String msg = name + ":Maximum memory size for direct buffers (=" + MAX_MEMORY_CAPACITY + ") exceeded";
                throw new OutOfMemoryError( msg );
            }

            buffer = new PooledByteBuffer( capacity, this, id++ );
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
                // System.out.println( name + ":deallocate: freebuffer capas after: " + freeBuffers.size()
                // + ", number of freebuffers for capa: " + capacity + ", "
                // + freeBuffers.get( capacity ).size() );
                buffers.add( buffer );
                // System.out.println( name + ":deallocate: freebuffer capas before: " + freeBuffers.size()
                // + ", number of freebuffers for capa: " + capacity + ", "
                // + freeBuffers.get( capacity ).size() );

            }
        }
    }

    @Override
    public String toString() {
        return "Number of buffers: " + this.allBuffers.size() + ", total capacity: " + totalCapacity;
    }

    /**
     * @param capacity
     * @return true if the pool has more free space.
     */
    public synchronized boolean canAllocate( int capacity ) {
        boolean result = false;
        Set<PooledByteBuffer> capBuffers = freeBuffers.get( capacity );
        result = capBuffers != null && !capBuffers.isEmpty();
        return result || ( ( totalCapacity + capacity ) <= MAX_MEMORY_CAPACITY );
    }
}
