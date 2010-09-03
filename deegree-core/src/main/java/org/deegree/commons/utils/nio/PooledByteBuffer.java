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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 
 * The <code>PooledByteBuffer</code> defines the interface to a direct 'native' byte buffer which can be reused again.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: rbezema $
 * @version $Revision: $, $Date: $
 * 
 */
public class PooledByteBuffer {

    private DirectByteBufferPool pool;

    private ByteBuffer buffer;

    private final long id;

    /**
     * Only intantiable from the {@link DirectByteBufferPool}.
     * 
     * @param capacity
     * @param pool
     */
    PooledByteBuffer( int capacity, DirectByteBufferPool pool, long id ) {
        this.buffer = ByteBuffer.allocateDirect( capacity );
        this.buffer.order( ByteOrder.nativeOrder() );
        this.pool = pool;
        this.id = id;
    }

    /**
     * @param capacity
     *            of the bytebuffer.
     */
    public PooledByteBuffer( int capacity ) {
        this.buffer = ByteBuffer.allocateDirect( capacity );
        this.buffer.order( ByteOrder.nativeOrder() );
        this.id = 0;
    }

    /**
     * Get the pooled direct, native buffer.
     * 
     * @return the ByteBuffer wrapped by this class.
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Mark this bytebuffer as free.
     */
    public void free() {
        if ( pool != null )
            pool.deallocate( this );
    }

    /**
     * Just set the reference to null.
     */
    public void markAsFree() {
        this.pool = null;
        this.buffer = null;
    }

    /**
     * call the {@link ByteBuffer#rewind()} method.
     */
    public void rewind() {
        if ( this.buffer != null ) {
            buffer.rewind();
        }
    }

    /**
     * call the {@link ByteBuffer#clear()} method.
     */
    public void clear() {
        if ( this.buffer != null ) {
            buffer.clear();
        }
    }

    /**
     * call the {@link ByteBuffer#limit(int)} method.
     * 
     * @param capacity
     */
    public void limit( int capacity ) {
        if ( this.buffer != null ) {
            buffer.limit( capacity );
        }
    }

    /**
     * call the {@link ByteBuffer#limit()} method.
     * 
     * @return the limit of the buffer.
     */
    public int limit() {
        if ( this.buffer != null ) {
            return buffer.limit();
        }
        return 0;
    }

    /**
     * call the {@link ByteBuffer#capacity()} method.
     * 
     * @return the limit of the buffer.
     */
    public int capacity() {
        if ( this.buffer != null ) {
            return buffer.capacity();
        }
        return 0;
    }

    /**
     * call the {@link ByteBuffer#position()} method.
     * 
     * @return the limit of the buffer.
     */
    public int position() {
        if ( this.buffer != null ) {
            return buffer.position();
        }
        return 0;
    }

    /**
     * call the {@link ByteBuffer#position()} method.
     * 
     * @param position
     *            of the buffer.
     */
    public void position( int position ) {
        if ( this.buffer != null ) {
            buffer.position( position );
        }
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof PooledByteBuffer ) {
            final PooledByteBuffer that = (PooledByteBuffer) other;
            return this.getId() == that.getId();
        }
        return false;
    }

    /**
     * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
     * distribution and is relatively fast. It is created from field <b>f</b> as follows:
     * <ul>
     * <li>boolean -- code = (f ? 0 : 1)</li>
     * <li>byte, char, short, int -- code = (int)f</li>
     * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
     * <li>float -- code = Float.floatToIntBits(f);</li>
     * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt; 32))</li>
     * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code = f.hashCode(&nbsp;)</li>
     * <li>Array -- Apply above rules to each element</li>
     * </ul>
     * <p>
     * Combining the hash code(s) computed above: result = 37 * result + code;
     * </p>
     * 
     * @return (int) ( result >>> 32 ) ^ (int) result;
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // the 2nd millionth prime, :-)
        long code = 32452843;
        long tmp = (int) ( getId() ^ ( getId() >>> 32 ) );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );
        return (int) ( code >>> 32 ) ^ (int) code;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }
}
