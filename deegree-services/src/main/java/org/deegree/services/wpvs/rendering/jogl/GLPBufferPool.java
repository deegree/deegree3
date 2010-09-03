//$HeadURL$
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

package org.deegree.services.wpvs.rendering.jogl;

import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLPbuffer;

import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.opengl.JOGLChecker;
import org.deegree.rendering.r3d.opengl.JOGLUtils;

/**
 * The <code>GLPBufferPool</code> supplies methods for the creation of pbuffers. In the future it will be able to pool
 * buffers as well.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class GLPBufferPool {
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( GLPBufferPool.class );

    private final int maxTextureSize;

    private final int maxWidth;

    private final int maxHeight;

    private final Map<PBufferKey, GLPbuffer> pool;

    private final PBufferKey defaultKey;

    /**
     * Create a new pool of GLPBuffers, with the given max width and max height. If the max width and or height is
     * larger as the maximum PBuffer size, the maximum PBuffer size will be used in the requested aspect.
     * 
     * @param maxNumberOfBuffers
     * @param maxWidth
     * @param maxHeight
     */
    public GLPBufferPool( int maxNumberOfBuffers, int maxWidth, int maxHeight ) {
        JOGLChecker.check();
        pool = new HashMap<PBufferKey, GLPbuffer>( maxNumberOfBuffers );
        maxTextureSize = determineMaxPBufferSize();
        maxWidth = Math.max( 2, maxWidth );
        maxHeight = Math.max( 2, maxHeight );
        if ( maxWidth > maxTextureSize || maxHeight > maxTextureSize ) {

            double scale = ( (double) maxTextureSize ) / ( ( maxWidth > maxHeight ) ? maxWidth : maxHeight );
            LOG.warn( "Configured maximum request Width/Height are larger than your graphicsboard allows, rescaling configured width: "
                      + maxWidth
                      + " to "
                      + Math.floor( maxWidth * scale )
                      + " | height: "
                      + maxHeight
                      + " to "
                      + Math.floor( maxHeight * scale ) );
            maxWidth = (int) Math.floor( maxWidth * scale );
            maxHeight = (int) Math.floor( maxHeight * scale );
        }
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.defaultKey = new PBufferKey( this.maxWidth, this.maxHeight );
    }

    /**
     * @return
     */
    private int determineMaxPBufferSize() {
        GLPbuffer buffer = createBuffer( 2, 2 );
        if ( buffer.getWidth() == 0 || buffer.getHeight() == 0 ) {
            LOG.warn( "No PBuffer could be create for size 2 implying that your Graphicsboard does not support PBuffers, you might not be able to use the WPVS.");
        }
        PBufferSizeTest test = new PBufferSizeTest();
        buffer.addGLEventListener( test );

        try {
            buffer.display();
        } catch ( Throwable t ) {
            throw new UnsupportedOperationException(
                                                     "Error while determining maximum buffer size, this implies, that your Graphicsboard does not support PBuffers, you are not able to use the WPVS. The original message was: "
                                                                             + t.getLocalizedMessage(), t );
        }
        try {
            buffer.destroy();
        } catch ( Throwable t ) {
            LOG.error( "Could not destroy test pbuffer, this might indicate a problem, the original message was: "
                       + t.getLocalizedMessage(), t );
        }

        int result = test.glTextureSize;
        if ( result == 0 ) {
            throw new UnsupportedOperationException(
                                                     "The maximum PBuffer size = 0, implying that your graphicsboard does not support PBuffers, you are not able to use the WPVS." );
        }
        return result;
    }

    /**
     * creates and returns a GLPbuffer for offscreen rendering the configured maximum width and height will be used.
     * 
     * @param viewParams
     *            to get the buffer for.
     * 
     * @return an offscreen {@link GLPbuffer} with the maximum configured width and height.
     */
    public synchronized GLPbuffer getOffscreenBuffer( ViewParams viewParams ) {
        GLPbuffer buffer = pool.get( defaultKey );
        if ( buffer == null ) {
            buffer = createBuffer( maxWidth, maxHeight );
        }
        return buffer;
    }

    private synchronized GLPbuffer createBuffer( int width, int height ) {
        GLDrawableFactory glFactory = GLDrawableFactory.getFactory();
        // Create the offscreen drawable (pBuffer). Note that the width
        // and height must be a power of two if it is to be used as a
        // texture.
        // GLCanvas d = new GLCanvas();
        if ( glFactory.canCreateGLPbuffer() ) {
            GLCapabilities caps = new GLCapabilities();
            GLPbuffer buf = glFactory.createGLPbuffer( caps, null, width, height, null );
            return buf;
            // buffer.addGLEventListener(new PBufferGLEventListener(pBufferTexID,128,128,glu));
        }
        throw new UnsupportedOperationException(
                                                 "Your graphic hardware does not support GLPbuffers, you can not run the WPVS." );
    }

    /**
     * @return the glTextureSize
     */
    public final int getMaxTextureSize() {
        return maxTextureSize;
    }

    /**
     * @return the maxWidth
     */
    public final int getMaxWidth() {
        return maxWidth;
    }

    /**
     * @return the maxHeight
     */
    public final int getMaxHeight() {
        return maxHeight;
    }

    /**
     * 
     * The <code>PBufferSizeTest</code> class queries a very small glContext for the max texture size.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author$
     * @version $Revision$, $Date$
     * 
     */
    private class PBufferSizeTest implements GLEventListener {
        PBufferSizeTest() {
            // empty constructor.
        }

        int glTextureSize;

        @Override
        public void display( GLAutoDrawable glpBuffer ) {
            // no rendering
        }

        @Override
        public void displayChanged( GLAutoDrawable glpBuffer, boolean arg1, boolean arg2 ) {
            // no rendering
        }

        @Override
        public void init( GLAutoDrawable glpBuffer ) {
            GL context = glpBuffer.getGL();
            glTextureSize = JOGLUtils.getMaxTextureSize( context );
        }

        @Override
        public void reshape( GLAutoDrawable glpBuffer, int x, int y, int width, int height ) {
            // no rendering
        }
    }

    /**
     * 
     * The <code>PBufferKey</code> class is a gathering of information which can be used as a key for the pool.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author$
     * @version $Revision$, $Date$
     * 
     */
    private class PBufferKey {

        private final int width;

        private final int height;

        PBufferKey( int width, int height ) {
            this.width = width;
            this.height = height;
        }

        PBufferKey( ViewParams params ) {
            this.width = params.getScreenPixelsX();
            this.height = params.getScreenPixelsY();
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
            long tmp = width;
            code = code * 37 + (int) ( tmp );
            tmp = height;
            code = code * 37 + (int) ( tmp );
            return (int) ( code >>> 32 ) ^ (int) code;
        }

        @Override
        public boolean equals( Object other ) {
            if ( other != null && other instanceof PBufferKey ) {
                final PBufferKey that = (PBufferKey) other;
                return this.width == that.width && this.height == that.height;
            }
            return false;
        }

    }

}
