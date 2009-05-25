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

package org.deegree.rendering.r3d.opengl.rendering.model.manager;

import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL;
import javax.vecmath.Point3d;

import org.deegree.commons.utils.math.Vectors3f;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.opengl.rendering.JOGLRenderable;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.BillBoard;
import org.deegree.rendering.r3d.opengl.rendering.model.texture.TexturePool;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.texture.Texture;

/**
 * The <code>TreeManager</code> will hold the bill board references depending on their texture id.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class TreeRenderer extends RenderableManager<BillBoard> implements JOGLRenderable {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( TreeRenderer.class );

    private static final FloatBuffer tncBuffer;
    static {
        float[] buffer = new float[] { 0, 1, 0, -1, 0, -.5f, 0, 0, // ll
                                      1, 1, 0, -1, 0, .5f, 0, 0,// lr
                                      1, 0, 0, -1, 0, .5f, 0, 1, // ur
                                      0, 0, 0, -1, 0, -.5f, 0, 1 // ul

        };
        tncBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( buffer ) );
        buffer = null;
    }

    private int[] bufferID = null;

    /**
     * @param validDomain
     * @param numberOfObjectsInLeaf
     * @param maxPixelError
     */
    public TreeRenderer( Envelope validDomain, int numberOfObjectsInLeaf, double maxPixelError ) {
        super( validDomain, numberOfObjectsInLeaf, maxPixelError );
    }

    /**
     * 
     * @param params
     * @return an ordered List of trees.
     */
    public List<BillBoard> getTreesForViewParameters( ViewParams params ) {
        TreeComparator a = new TreeComparator( params.getViewFrustum().getEyePos() );
        return getObjects( params, a );
    }

    @Override
    public void render( RenderContext glRenderContext ) {
        ViewParams params = glRenderContext.getViewParams();
        GL context = glRenderContext.getContext();
        long begin = System.currentTimeMillis();
        Point3d eye = params.getViewFrustum().getEyePos();
        float[] eye2 = new float[] { (float) eye.x, (float) eye.y, (float) eye.z };
        List<BillBoard> allBillBoards = getTreesForViewParameters( params );
        if ( !allBillBoards.isEmpty() ) {
            // back to front
            Collections.sort( allBillBoards, new DistComparator( eye ) );
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "Number of trees from viewparams: " + allBillBoards.size() );
                LOG.debug( "Total number of trees : " + size() );
            }
            context.glPushAttrib( GL.GL_CURRENT_BIT | GL.GL_LIGHTING_BIT | GL.GL_ENABLE_BIT );
            context.glMaterialfv( GL.GL_FRONT, GL.GL_DIFFUSE, new float[] { 1, 1, 1, 1 }, 0 );
            context.glMaterialfv( GL.GL_FRONT, GL.GL_SPECULAR, new float[] { 0.3f, 0.3f, 0.3f, 1 }, 0 );

            context.glEnable( GL.GL_TEXTURE_2D );

            context.glEnable( GL.GL_ALPHA_TEST );
            context.glAlphaFunc( GL.GL_GREATER, 0.4f );

            if ( bufferID == null ) {
                bufferID = new int[1];
                context.glGenBuffersARB( 1, bufferID, 0 );
                // bind vertex buffer object (vertices)
                context.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, bufferID[0] );
                context.glBufferDataARB( GL.GL_ARRAY_BUFFER_ARB, tncBuffer.capacity() * 4, tncBuffer,
                                         GL.GL_STATIC_DRAW_ARB );

            }
            context.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, bufferID[0] );
            context.glInterleavedArrays( GL.GL_T2F_N3F_V3F, 0, 0 );

            Iterator<BillBoard> it = allBillBoards.iterator();
            Texture currentTexture = null;
            while ( it.hasNext() ) {
                BillBoard b = it.next();
                context.glPushMatrix();
                Texture t = TexturePool.getTexture( glRenderContext, b.getTextureID() );
                if ( t != null ) {
                    if ( currentTexture == null || t.getTextureObject() != currentTexture.getTextureObject() ) {
                        t.bind();
                        currentTexture = t;
                    }
                    b.renderPrepared( context, eye2 );
                }
                context.glPopMatrix();

            }
            context.glDisable( GL.GL_TEXTURE_2D );
            context.glDisableClientState( GL.GL_TEXTURE_COORD_ARRAY );
            context.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, 0 );

            context.glPopAttrib();
            LOG.debug( "Rendering of " + allBillBoards.size() + " trees took: " + ( System.currentTimeMillis() - begin )
                       + " ms" );
        } else {
            LOG.debug( "Not rendering any trees." );
        }

    }

    private class TreeComparator implements Comparator<BillBoard> {
        private float[] eye;

        /**
         * @param eye
         *            to compare this billboard to.
         * 
         */
        public TreeComparator( Point3d eye ) {
            this.eye = new float[] { (float) eye.x, (float) eye.y, (float) eye.z };
        }

        @Override
        public int compare( BillBoard o1, BillBoard o2 ) {
            int res = o1.getTextureID().compareTo( o2.getTextureID() );
            if ( res == 0 ) {
                float distA = Vectors3f.distance( eye, o1.getPosition() );
                float distB = Vectors3f.distance( eye, o2.getPosition() );
                res = -Float.compare( distA, distB );
            }
            return res;
        }

    }

    private class DistComparator implements Comparator<BillBoard> {
        private float[] eye;

        /**
         * @param eye
         *            to compare this billboard to.
         * 
         */
        public DistComparator( Point3d eye ) {
            this.eye = new float[] { (float) eye.x, (float) eye.y, (float) eye.z };
        }

        @Override
        public int compare( BillBoard o1, BillBoard o2 ) {
            float distA = Vectors3f.distance( eye, o1.getPosition() );
            float distB = Vectors3f.distance( eye, o2.getPosition() );
            // /**
            // * Trees that are near to each other might have the same texture.
            // */
            // if ( Math.abs( distA - distB ) < 35 ) {
            // int res = o1.getTextureID().compareTo( o2.getTextureID() );
            // if ( res == 0 ) {
            // res = -Float.compare( distA, distB );
            // }
            // return res;
            // }
            return -Float.compare( distA, distB );
        }

    }

}
