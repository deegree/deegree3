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
