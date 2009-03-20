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
package org.deegree.rendering.r3d.opengl.rendering.dem;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.media.opengl.GL;

import org.deegree.model.multiresolution.MeshFragment;
import org.deegree.model.multiresolution.MeshFragmentData;
import org.deegree.model.multiresolution.MultiresolutionMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.opengl.util.BufferUtil;

/**
 * Represents a fragment of a {@link MultiresolutionMesh} that can be rendered via JOGL.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class RenderableMeshFragment {

    private static final double AREA_MIN_X = 2568000;

    private static final double AREA_MIN_Y = 5606000;

    private static final Logger LOG = LoggerFactory.getLogger( RenderableMeshFragment.class );

    private MeshFragment fragment;

    // 0: vertex (coordinates) buffer
    // 1: normal buffer
    // 2: triangle buffer
    // 3: texture coordinates buffer
    private int[] glBufferObjectIds;

    private TextureTile textureTile;

    private TextureTile delayedTextureTile;

    private FloatBuffer delayedTexCoordsBuffer;

    public RenderableMeshFragment( MeshFragment fragment ) {
        this.fragment = fragment;
    }

    public float[][] getBBox() {
        return fragment.bbox;
    }

    public void render( GL gl ) {

        loadToGPU( gl );

        gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[0] );
        gl.glVertexPointer( 3, GL.GL_FLOAT, 0, 0 );

        gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[1] );
        gl.glNormalPointer( GL.GL_FLOAT, 0, 0 );

        if ( textureTile != null ) {
            LOG.debug( "Rendering mesh fragment with texture..." );

            // texture information has been loaded and texture coordinate buffer has been prepared
            gl.glEnable( GL.GL_TEXTURE_2D );
            gl.glBindTexture( GL.GL_TEXTURE_2D, textureTile.getGLTextureId( gl ) );
            //            
            // textureTile.getTexture().enable();
            // textureTile.getTexture().bind();
            gl.glEnableClientState( GL.GL_TEXTURE_COORD_ARRAY );

            gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[3] );
            gl.glTexCoordPointer( 2, GL.GL_FLOAT, 0, 0 );
        } else {
            LOG.debug( "Rendering mesh fragment without texture..." );
            gl.glDisable( GL.GL_TEXTURE_2D );
            gl.glDisableClientState( GL.GL_TEXTURE_COORD_ARRAY );
        }

        gl.glBindBufferARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, glBufferObjectIds[2] );
        gl.glDrawElements( GL.GL_TRIANGLES, fragment.getData().getNumTriangles() * 3, GL.GL_UNSIGNED_SHORT, 0 );
    }

    public void texturize( GL gl, TextureTile textureTile ) {
        texturize( gl, textureTile, getTexCoordsBuffer( textureTile ) );
    }

    public void texturize( GL gl, TextureTile textureTile, FloatBuffer texCoordsBuffer ) {

        loadToGPU( gl );
        textureTile.loadToGPU( gl );
       
        // bind vertex buffer object (vertex coordinates)
        gl.glBindBufferARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, glBufferObjectIds[3] );
        gl.glBufferDataARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, texCoordsBuffer.capacity() * 4, texCoordsBuffer,
                            GL.GL_STATIC_DRAW_ARB );
        this.textureTile = textureTile;
    }    
    
    public void untexturize( GL gl ) {
        if ( textureTile != null ) {
            textureTile.unloadFromGPU( gl );
            textureTile = null;
        }
    }

    public void destroy( GL gl ) {
        int[] bufferObjectIds = this.glBufferObjectIds;
        this.glBufferObjectIds = null;
        gl.glDeleteBuffersARB( bufferObjectIds.length, bufferObjectIds, 0 );
        this.textureTile = null;
    }

    public void loadToGPU( GL gl ) {
        if ( glBufferObjectIds == null ) {
            createBufferObjects( gl );
        }
    }

    public void unloadFromGPU( GL gl ) {
        if ( glBufferObjectIds != null ) {
            int[] bufferObjectIds = this.glBufferObjectIds;
            this.glBufferObjectIds = null;
            gl.glDeleteBuffersARB( bufferObjectIds.length, bufferObjectIds, 0 );
        }
        untexturize( gl );
    }

    private void createBufferObjects( GL gl ) {

        glBufferObjectIds = new int[4];
        gl.glGenBuffersARB( 4, glBufferObjectIds, 0 );

        MeshFragmentData data = fragment.getData();
        FloatBuffer vertexBuffer = data.getVertices();
        ShortBuffer indexBuffer = (ShortBuffer) data.getTriangles();
        FloatBuffer normalsBuffer = data.getNormals();

        // bind vertex buffer object (vertices)
        gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[0] );
        gl.glBufferDataARB( GL.GL_ARRAY_BUFFER_ARB, vertexBuffer.capacity() * 4, vertexBuffer, GL.GL_STATIC_DRAW_ARB );

        // bind vertex buffer object (normals)
        gl.glBindBufferARB( GL.GL_ARRAY_BUFFER_ARB, glBufferObjectIds[1] );
        gl.glBufferDataARB( GL.GL_ARRAY_BUFFER_ARB, normalsBuffer.capacity() * 4, normalsBuffer, GL.GL_STATIC_DRAW_ARB );

        // bind element buffer object (triangles)
        gl.glBindBufferARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, glBufferObjectIds[2] );
        gl.glBufferDataARB( GL.GL_ELEMENT_ARRAY_BUFFER_ARB, indexBuffer.capacity() * 2, indexBuffer,
                            GL.GL_STATIC_DRAW_ARB );
    }

    public MeshFragment fragmentInfo() {
        return fragment;
    }

    public boolean isLoaded() {
        return glBufferObjectIds != null;
    }

    public float getGeometricError() {
        return fragment.error;
    }

    public float getCurrentTextureResolution() {
        if ( textureTile == null ) {
            return Float.MAX_VALUE;
        }
        return textureTile.getMetersPerPixel();
    }

    public void delayedTexturize( TextureTile tile ) {
        this.delayedTextureTile = tile;
        this.delayedTexCoordsBuffer = getTexCoordsBuffer( tile );        
    }
    
    private FloatBuffer getTexCoordsBuffer (TextureTile textureTile) {

        float patchXMin = fragment.bbox[0][0];
        float patchYMin = fragment.bbox[0][1];
        float patchXMax = fragment.bbox[1][0];
        float patchYMax = fragment.bbox[1][1];

        float tileXMin = textureTile.getMinX() - (float) AREA_MIN_X;
        float tileYMin = textureTile.getMinY() - (float) AREA_MIN_Y;
        float tileXMax = textureTile.getMaxX() - (float) AREA_MIN_X;
        float tileYMax = textureTile.getMaxY() - (float) AREA_MIN_Y;
        // System.out.println( "Boundary of texture tile: (" + tileXMin + "," + tileYMin + "," + tileXMax + "," +
        // tileYMax
        // + ")" );

        if ( tileXMin > patchXMin || tileYMin > patchYMin || tileXMax < patchXMax || tileYMax < patchYMax ) {
            String msg = "Internal error. Returned texture tile is not suitable for this RenderPatch.";
            throw new IllegalArgumentException( msg );
        }

        float tileWidth = textureTile.getMaxX() - textureTile.getMinX();
        float tileHeight = textureTile.getMaxY() - textureTile.getMinY();
        // System.out.println( "Using texture tile with width: " + tileWidth + ", height: " + tileHeight );

        // build texture coordinates buffer
        FloatBuffer vertexBuffer = fragment.getData().getVertices();
        vertexBuffer.rewind();

        FloatBuffer texCoordsBuffer = BufferUtil.newFloatBuffer( vertexBuffer.capacity() / 3 * 2 );
        for ( int i = 0; i < vertexBuffer.capacity() / 3; i++ ) {
            float x = vertexBuffer.get();
            float y = vertexBuffer.get();
            float z = vertexBuffer.get();
            texCoordsBuffer.put( ( x - tileXMin ) / tileWidth );
            texCoordsBuffer.put( 1.0f - ( y - tileYMin ) / tileHeight );
        }
        vertexBuffer.rewind();
        texCoordsBuffer.rewind();
        return texCoordsBuffer;
    }

    public void applyDelayedTexture( GL gl ) {
        if ( delayedTextureTile != null ) {
            untexturize( gl );
            texturize( gl, delayedTextureTile, delayedTexCoordsBuffer );
            this.delayedTextureTile = null;
            this.delayedTexCoordsBuffer = null;
        }
    }
}
