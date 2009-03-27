package org.deegree.commons.utils.nio;

import java.nio.ByteBuffer;

public class PooledByteBuffer {

    private DirectByteBufferPool pool;
    
    private ByteBuffer buffer;
    
    PooledByteBuffer( int capacity, DirectByteBufferPool pool ) {
        this.buffer = ByteBuffer.allocateDirect( capacity );
        this.pool = pool;
    }

    public PooledByteBuffer( int capacity ) {
        this.buffer = ByteBuffer.allocateDirect( capacity );
    }

    public ByteBuffer getBuffer () {
        return buffer;
    }
    
    public void free () {
        if (pool != null)
        pool.deallocate( this );
    }
    
}
