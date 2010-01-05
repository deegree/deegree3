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

package org.deegree.rendering.r3d.opengl.rendering.dem.texturing;

import java.nio.ByteBuffer;

import javax.media.opengl.GL;

/**
 * 
 * The <code>TextureTile</code> define the parameters necessary to create, request and render a texture to the DEM.
 * Texture are created and rendered with following options:
 * <ul>
 * <li>gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP );</li>
 * <li>gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP );</li>
 * <li>gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR );</li>
 * <li>gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );</li>
 * </ul>
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: rbezema $
 * @version $Revision: $, $Date: $
 * 
 */
public class TextureTile {

    private int[] textureID;

    private float minX, minY, maxX, maxY;

    private float metersPerPixel;

    private int pixelsX, pixelsY;

    private ByteBuffer imageData;

    private static int idsInUse = 0;

    // counts the number of references, if 0, data can be unloaded
    private int numReferences;

    private boolean hasAlpha;

    /**
     * Construct a new texture tile.
     * 
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @param pixelsX
     * @param pixelsY
     * @param imageData
     * @param hasAlpha
     */
    public TextureTile( float minX, float minY, float maxX, float maxY, int pixelsX, int pixelsY, ByteBuffer imageData,
                        boolean hasAlpha ) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;

        // rb: what to do with lines?
        if ( maxX != minX ) {
            metersPerPixel = ( maxX - minX ) / pixelsX;
        } else {
            metersPerPixel = ( maxY - minY ) / pixelsY;
        }

        this.pixelsX = pixelsX;
        this.pixelsY = pixelsY;
        this.imageData = imageData;
        this.hasAlpha = hasAlpha;
    }

    /**
     * @return the actual data.
     */
    public ByteBuffer getImageData() {
        return imageData;
    }

    /**
     * @return the min x position in world coordinates.
     */
    public float getMinX() {
        return minX;
    }

    /**
     * @return the min y position in world coordinates.
     */
    public float getMinY() {
        return minY;
    }

    /**
     * @return the max x position in world coordinates.
     */
    public float getMaxX() {
        return maxX;
    }

    /**
     * @return the max Y position in world coordinates.
     */
    public float getMaxY() {
        return maxY;
    }

    /**
     * @return the number of meters this texture projects onto one pixel.
     */
    public float getMetersPerPixel() {
        return metersPerPixel;
    }

    /**
     * 
     * @return the width of the image.
     */
    public int getPixelsX() {
        return pixelsX;
    }

    /**
     * 
     * @return the height of the image.
     */
    public int getPixelsY() {
        return pixelsY;
    }

    /**
     * @param gl
     *            context to load this texture to.
     * @return the opengl texture id.
     */
    public int enable( GL gl ) {
        numReferences++;
        loadToGPU( gl );
        return textureID[0];
    }

    /**
     * Remove the texture from the context.
     * 
     * @param gl
     */
    public void disable( GL gl ) {
        numReferences--;
        if ( numReferences < 0 ) {
            throw new RuntimeException();
        }
        if ( numReferences == 0 ) {
            gl.glDeleteTextures( 1, textureID, 0 );
            textureID = null;
            idsInUse--;
        }
    }

    /**
     * load this texture to the gpu, using Texture Objects.
     * 
     * @param gl
     */
    private void loadToGPU( GL gl ) {
        if ( textureID == null ) {
            textureID = new int[1];
            idsInUse++;
            gl.glGenTextures( 1, textureID, 0 );
            gl.glBindTexture( GL.GL_TEXTURE_2D, textureID[0] );
            gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP );
            gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP );
            gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR );
            gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );
            imageData.rewind();

            if ( hasAlpha ) {
                gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, pixelsX, pixelsY, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE,
                                 imageData );
            } else {
                gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, GL.GL_RGB, pixelsX, pixelsY, 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE,
                                 imageData );
            }
        }
    }

    @Override
    public String toString() {
        String s = "{minX=" + minX + ",minY=" + minY + ",maxX=" + maxX + ",maxY=" + maxY + ",meters/pixel="
                   + metersPerPixel + "}";
        return s;
    }

    /**
     * 
     * @return the GL-texture object id of this texture tile.
     */
    public int getId() {
        return textureID[0];
    }
}
