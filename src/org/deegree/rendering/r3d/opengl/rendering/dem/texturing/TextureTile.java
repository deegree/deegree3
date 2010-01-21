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

import static org.slf4j.LoggerFactory.getLogger;

import java.nio.ByteBuffer;

import javax.media.opengl.GL;

import org.deegree.commons.utils.nio.PooledByteBuffer;
import org.slf4j.Logger;

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

    private static final Logger LOG = getLogger( TextureTile.class );

    private int[] textureID;

    private double minX, minY, maxX, maxY;

    private double metersPerPixel;

    private int tWidth, tHeight;

    private ByteBuffer imageData;

    // counts the number of references, if 0, data can be unloaded
    private int numReferences;

    private boolean hasAlpha;

    private PooledByteBuffer pooledBuffer;

    /**
     * Construct a new texture tile.
     * 
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @param textureWidth
     * @param textureHeight
     * @param imageData
     * @param hasAlpha
     */
    public TextureTile( double minX, double minY, double maxX, double maxY, int textureWidth, int textureHeight,
                        ByteBuffer imageData, boolean hasAlpha ) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;

        // rb: what to do with lines?
        if ( maxX != minX ) {
            metersPerPixel = ( maxX - minX ) / textureWidth;
        } else {
            metersPerPixel = ( maxY - minY ) / textureWidth;
        }

        this.tWidth = textureWidth;
        this.tHeight = textureHeight;
        this.imageData = imageData;
        this.hasAlpha = hasAlpha;
    }

    /**
     * Construct a new texture tile.
     * 
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @param metersPerPixel
     *            the resolution of a pixel in meters.
     * @param textureWidth
     * @param textureHeight
     * @param pooledBuffer
     * @param hasAlpha
     */
    public TextureTile( double minX, double minY, double maxX, double maxY, float metersPerPixel, int textureWidth,
                        int textureHeight, PooledByteBuffer pooledBuffer, boolean hasAlpha ) {
        this( minX, minY, maxX, maxY, textureWidth, textureHeight, pooledBuffer.getBuffer(), hasAlpha );
        this.pooledBuffer = pooledBuffer;
        this.metersPerPixel = metersPerPixel;
    }

    /**
     * Clean up all reference to the byte buffer.
     */
    public void dispose() {
        if ( this.pooledBuffer != null ) {
            this.pooledBuffer.free();
        }
        this.imageData = null;
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
    public double getMinX() {
        return minX;
    }

    /**
     * @return the min y position in world coordinates.
     */
    public double getMinY() {
        return minY;
    }

    /**
     * @return the max x position in world coordinates.
     */
    public double getMaxX() {
        return maxX;
    }

    /**
     * @return the max Y position in world coordinates.
     */
    public double getMaxY() {
        return maxY;
    }

    /**
     * @return the number of meters this texture projects onto one pixel.
     */
    public double getMetersPerPixel() {
        return metersPerPixel;
    }

    /**
     * 
     * @return the width of the image.
     */
    public int getWidth() {
        return tWidth;
    }

    /**
     * 
     * @return the height of the image.
     */
    public int getHeight() {
        return tHeight;
    }

    /**
     * @param gl
     *            context to load this texture to.
     * @return the opengl texture id.
     */
    public int enable( GL gl ) {
        System.out.println( "Enabling textureTile: " + numReferences );
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
            LOG.debug( "disabling and freeing texture memory." );
            gl.glDeleteTextures( 1, textureID, 0 );
            textureID = null;
            if ( pooledBuffer != null ) {
                pooledBuffer.free();
            }
            if ( imageData != null ) {
                imageData = null;
            }
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
            gl.glGenTextures( 1, textureID, 0 );
            gl.glBindTexture( GL.GL_TEXTURE_2D, textureID[0] );
            gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP );
            gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP );
            gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR );
            gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );
            if ( imageData != null && imageData.capacity() > 0 ) {
                imageData.rewind();
                if ( hasAlpha ) {
                    gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, tWidth, tHeight, 0, GL.GL_RGBA,
                                     GL.GL_UNSIGNED_BYTE, imageData );
                } else {
                    gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, GL.GL_RGB, tWidth, tHeight, 0, GL.GL_RGB,
                                     GL.GL_UNSIGNED_BYTE, imageData );
                }
            } else {
                LOG.warn( "The texture tile has no data set (anymore?) might their be a cache problem?" );
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
