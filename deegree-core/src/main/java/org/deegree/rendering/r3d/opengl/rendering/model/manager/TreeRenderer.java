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

package org.deegree.rendering.r3d.opengl.rendering.model.manager;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.media.opengl.GL;
import javax.vecmath.Point3d;

import org.deegree.commons.utils.math.Vectors3f;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.ViewParams;
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
public class TreeRenderer extends RenderableManager<BillBoard>{

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( TreeRenderer.class );

    // private static final FloatBuffer tncBuffer;
    // static {
    // float[] buffer = new float[] { 0, 1, 0, -1, 0, -.5f, 0, 0, // ll
    // 1, 1, 0, -1, 0, .5f, 0, 0,// lr
    // 1, 0, 0, -1, 0, .5f, 0, 1, // ur
    // 0, 0, 0, -1, 0, -.5f, 0, 1 // ul
    //
    // };
    // tncBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( buffer ) );
    // buffer = null;
    // }

    private static final FloatBuffer coordBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( new float[] {
                                                                                                              -.5f,
                                                                                                              0,
                                                                                                              0, // ll
                                                                                                              .5f,
                                                                                                              0,
                                                                                                              0,// lr
                                                                                                              .5f,
                                                                                                              0,
                                                                                                              1,// ur
                                                                                                              -.5f, 0,
                                                                                                              1 }// ul
    ) );

    private static final FloatBuffer normalBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( new float[] {
                                                                                                               0,
                                                                                                               -1,
                                                                                                               0, // ll
                                                                                                               0,
                                                                                                               -1,
                                                                                                               0,// lr
                                                                                                               0, -1,
                                                                                                               0,// ur
                                                                                                               0, -1, 0 }// ul
    ) );

    private static final FloatBuffer textureBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( new float[] { 0, 1,
                                                                                                                1, 1,
                                                                                                                1, 0,
                                                                                                                0, 0 } ) );

    private int[] bufferID = null;

    /**
     * @param validDomain
     * @param numberOfObjectsInLeaf
     * @param maxPixelError
     */
    public TreeRenderer( Envelope validDomain, int numberOfObjectsInLeaf, double maxPixelError ) {
        super( validDomain, numberOfObjectsInLeaf, maxPixelError );
    }

    @Override
    public void render( RenderContext glRenderContext ) {
        ViewParams params = glRenderContext.getViewParams();
        GL context = glRenderContext.getContext();
        long begin = System.currentTimeMillis();
        Point3d eye = params.getViewFrustum().getEyePos();
        float[] eye2 = new float[] { (float) eye.x, (float) eye.y, (float) eye.z };
        Set<BillBoard> boards = getObjects( params );
        // TreeComparator a = new TreeComparator( params.getViewFrustum().getEyePos() );
        // Collections.sort( allBillBoards, a );
        if ( !boards.isEmpty() ) {
            // back to front
            List<BillBoard> allBillBoards = new ArrayList<BillBoard>( boards );
            Collections.sort( allBillBoards, new DistComparator( eye ) );
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "Number of trees from viewparams: " + allBillBoards.size() );
                LOG.debug( "Total number of trees : " + size() );
            }
            context.glPushAttrib( GL.GL_CURRENT_BIT | GL.GL_LIGHTING_BIT | GL.GL_ENABLE_BIT );
            context.glMaterialfv( GL.GL_FRONT, GL.GL_DIFFUSE, new float[] { 1, 1, 1, 1 }, 0 );
            context.glMaterialfv( GL.GL_FRONT, GL.GL_SPECULAR, new float[] { 0.3f, 0.3f, 0.3f, 1 }, 0 );

            context.glEnable( GL.GL_TEXTURE_2D );
            context.glEnableClientState( GL.GL_TEXTURE_COORD_ARRAY );

            context.glEnable( GL.GL_ALPHA_TEST );
            context.glAlphaFunc( GL.GL_GREATER, 0.4f );

            if ( bufferID == null ) {
                bufferID = new int[3];
                context.glGenBuffers( 3, bufferID, 0 );

                // bind vertex buffer object (vertices)
                context.glBindBuffer( GL.GL_ARRAY_BUFFER, bufferID[0] );
                context.glBufferData( GL.GL_ARRAY_BUFFER, coordBuffer.capacity() * 4, coordBuffer, GL.GL_STATIC_DRAW );

                // bind vertex buffer object (normals)
                context.glBindBuffer( GL.GL_ARRAY_BUFFER, bufferID[1] );
                context.glBufferData( GL.GL_ARRAY_BUFFER, normalBuffer.capacity() * 4, normalBuffer, GL.GL_STATIC_DRAW );

                // bind element buffer object (triangles)
                context.glBindBuffer( GL.GL_ARRAY_BUFFER, bufferID[2] );
                context.glBufferData( GL.GL_ARRAY_BUFFER, textureBuffer.capacity() * 4, textureBuffer,
                                      GL.GL_STATIC_DRAW );

            }

            context.glBindBuffer( GL.GL_ARRAY_BUFFER, bufferID[0] );
            context.glVertexPointer( 3, GL.GL_FLOAT, 0, 0 );

            context.glBindBuffer( GL.GL_ARRAY_BUFFER, bufferID[1] );
            context.glNormalPointer( GL.GL_FLOAT, 0, 0 );

            context.glBindBuffer( GL.GL_ARRAY_BUFFER, bufferID[2] );
            context.glTexCoordPointer( 2, GL.GL_FLOAT, 0, 0 );

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
            // context.glDeleteBuffers( 3, bufferID, 0 );
            context.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );

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
