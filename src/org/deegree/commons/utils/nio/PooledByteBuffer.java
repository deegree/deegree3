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

    /**
     * Only intantiable from the {@link DirectByteBufferPool}.
     *
     * @param capacity
     * @param pool
     */
    PooledByteBuffer( int capacity, DirectByteBufferPool pool ) {
        this.buffer = ByteBuffer.allocateDirect( capacity );
        this.buffer.order( ByteOrder.nativeOrder() );
        this.pool = pool;
    }

    /**
     * @param capacity
     *            of the bytebuffer.
     */
    public PooledByteBuffer( int capacity ) {
        this.buffer = ByteBuffer.allocateDirect( capacity );
        this.buffer.order( ByteOrder.nativeOrder() );
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

}
