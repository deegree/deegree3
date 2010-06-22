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

import static java.lang.Double.doubleToLongBits;
import static java.lang.Math.round;
import static org.slf4j.LoggerFactory.getLogger;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

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

    // hascode is comparing on 6th decimal.
    private static final double HASH_CODE_FLOOR = 1000000;

    private static final double EPS = 1E-6;

    private int[] textureID;

    private final double minX, minY, maxX, maxY, dataMinX, dataMinY, dataMaxX, dataMaxY;

    private double metersPerPixel;

    private int tWidth, tHeight;

    private ByteBuffer imageData;

    // counts the number of references, if 0, data can be unloaded
    // private int numReferences;

    private boolean hasAlpha;

    private PooledByteBuffer pooledBuffer;

    private boolean enableCaching;

    private final String LOCK = "LOCK";

    private final Set<Integer> referencedRenderFragments = new HashSet<Integer>();

    private final long dataSize;

    // true if the gl context unpack value must be set to 1 (the texture width can not be divided by 4 ) :-)
    private final boolean unpack;

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
     * @param enableCaching
     */
    public TextureTile( double minX, double minY, double maxX, double maxY, int textureWidth, int textureHeight,
                        ByteBuffer imageData, boolean hasAlpha, boolean enableCaching ) {
        this( minX, minY, maxX, maxY, minX, minY, maxX, maxY, textureWidth, textureHeight, imageData, hasAlpha,
              enableCaching );

    }

    /**
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @param dataMinX
     * @param dataMinY
     * @param dataMaxX
     * @param dataMaxY
     * @param textureWidth
     * @param textureHeight
     * @param imageData
     * @param hasAlpha
     * @param enableCaching
     */
    public TextureTile( double minX, double minY, double maxX, double maxY, double dataMinX, double dataMinY,
                        double dataMaxX, double dataMaxY, int textureWidth, int textureHeight, ByteBuffer imageData,
                        boolean hasAlpha, boolean enableCaching ) {
        this.minX = round( minX * HASH_CODE_FLOOR ) / HASH_CODE_FLOOR;
        this.minY = round( minY * HASH_CODE_FLOOR ) / HASH_CODE_FLOOR;
        this.maxX = round( maxX * HASH_CODE_FLOOR ) / HASH_CODE_FLOOR;
        this.maxY = round( maxY * HASH_CODE_FLOOR ) / HASH_CODE_FLOOR;
        this.dataMinX = dataMinX;
        this.dataMinY = dataMinY;
        this.dataMaxX = dataMaxX;
        this.dataMaxY = dataMaxY;

        this.tWidth = textureWidth;
        this.tHeight = textureHeight;
        // rb: what to do with lines?
        if ( maxX != minX ) {
            metersPerPixel = ( this.dataMaxX - this.dataMinX ) / tWidth;
        } else {
            metersPerPixel = ( this.dataMaxY - this.dataMinY ) / tWidth;
        }
        this.metersPerPixel = round( metersPerPixel * HASH_CODE_FLOOR ) / HASH_CODE_FLOOR;

        double p = tWidth / 4d;
        this.unpack = p - Math.floor( p ) > EPS;
        this.imageData = imageData;
        this.hasAlpha = hasAlpha;
        this.enableCaching = enableCaching;
        dataSize = tWidth * tHeight * ( 3 + ( hasAlpha ? 1 : 0 ) );
    }

    /**
     * Clean up all reference to the byte buffer.
     */
    public void dispose() {
        if ( this.pooledBuffer != null ) {
            this.pooledBuffer.free();
        }
        LOG.debug( "Texture tile (holding image data)  disposing the data." );
        if ( !referencedRenderFragments.isEmpty() ) {
            LOG.warn( "Some references remain, while disposing a texture tile, this may not be!!!" );
            return;
        }

        this.imageData = null;
    }

    /**
     * @return true if the given texture tile should be cached.
     */
    public boolean enableCaching() {
        return enableCaching;
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
     * @return the dataMinX
     */
    public final double getDataMinX() {
        return dataMinX;
    }

    /**
     * @return the dataMinY
     */
    public final double getDataMinY() {
        return dataMinY;
    }

    /**
     * @return the dataMaxX
     */
    public final double getDataMaxX() {
        return dataMaxX;
    }

    /**
     * @return the dataMaxY
     */
    public final double getDataMaxY() {
        return dataMaxY;
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
     * @param refID
     *            of the enabling fragment.
     * @return the opengl texture id.
     */
    public int enable( GL gl, int refID ) {
        synchronized ( LOCK ) {
            if ( referencedRenderFragments.contains( refID ) ) {
                LOG.debug( "Given reference was already registered: {}", refID );
            }
            referencedRenderFragments.add( refID );
            // numReferences++;
            // System.out.println( "--------------Enabling textureTile " + toString() + " numrefs: "
            // + referencedRenderFragments.size() );
            // Thread.dumpStack();
            loadToGPU( gl );
        }
        return textureID == null ? -1 : textureID[0];
    }

    /**
     * Remove the texture from the context.
     * 
     * @param gl
     * @param refID
     *            of the fragment which needed this texture tile.
     */
    public void disable( GL gl, int refID ) {
        synchronized ( LOCK ) {
            if ( !referencedRenderFragments.contains( refID ) ) {
                LOG.warn( "Trying to remove a reference, which was not registered, this is strange: " + textureID );
                if ( textureID == null ) {
                    return;
                }
            }
            referencedRenderFragments.remove( refID );
            // numReferences--;
            // if ( numReferences < 0 ) {
            // throw new RuntimeException();
            // }
            if ( textureID != null ) {
                if ( referencedRenderFragments.isEmpty() /* || numReferences == 0 */) {
                    LOG.debug( "disabling and freeing texture memory." );
                    gl.glDeleteTextures( 1, textureID, 0 );
                    textureID = null;
                }
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
            if ( imageData != null ) {
                if ( imageData.capacity() != dataSize ) {
                    LOG.warn( "The data of the texture tile was not set correctly, excpected are: " + dataSize
                              + " bytes of " + ( hasAlpha ? "RGBA" : "RGB" ) + " values, supplied were: "
                              + imageData.capacity() + ". This texture will not be rendered." );
                } else {
                    textureID = new int[1];

                    gl.glGenTextures( 1, textureID, 0 );
                    gl.glBindTexture( GL.GL_TEXTURE_2D, textureID[0] );
                    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP );
                    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP );
                    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR );
                    gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );
                    imageData.rewind();
                    // int pos = 0;
                    // int cap = imageData.capacity();
                    // byte[] color = new byte[] { (byte) ( 255 ), 0, 0 };
                    // if ( tWidth == 808 ) {
                    // color = new byte[] { 0, -1, 0 };
                    // }
                    // if ( tHeight == 808 ) {
                    // color = new byte[] { 0, 0, -1 };
                    // }
                    //
                    // while ( pos < cap ) {
                    // pos = imageData.put( color ).position();
                    //
                    // }
                    //
                    // imageData.rewind();
                    // System.out.println( "twidth: " + tWidth );
                    // System.out.println( "theight: " + tHeight );
                    // System.out.println( "alpha: " + hasAlpha );
                    // System.out.println( "capacity: " + imageData.capacity() + " should be: " + 3 * tWidth * tHeight
                    // );
                    if ( unpack ) {
                        LOG.debug( "Setting pixel unpack allignment to 1" );
                        gl.glPixelStorei( GL.GL_UNPACK_ALIGNMENT, 1 );
                    }
                    if ( hasAlpha ) {
                        gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, tWidth, tHeight, 0, GL.GL_RGBA,
                                         GL.GL_UNSIGNED_BYTE, imageData );
                    } else {
                        gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, GL.GL_RGB, tWidth, tHeight, 0, GL.GL_RGB,
                                         GL.GL_UNSIGNED_BYTE, imageData );
                    }
                    if ( unpack ) {
                        // reset
                        gl.glPixelStorei( GL.GL_UNPACK_ALIGNMENT, 4 );
                    }
                }
            } else {
                LOG.warn( "The texture tile has no data set (anymore?) might there be a cache problem?" );
            }
        }

    }

    @Override
    public String toString() {
        return "(" + minX + "," + minY + "," + maxX + "," + maxY + "), meter/pixel: " + metersPerPixel;
    }

    /**
     * 
     * @return the GL-texture object id of this texture tile.
     */
    public int getId() {
        return textureID[0];
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
        long tmp = doubleToLongBits( this.minX );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );
        tmp = doubleToLongBits( this.minY );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );
        tmp = Double.doubleToLongBits( this.maxX );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );
        tmp = Double.doubleToLongBits( this.maxY );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );
        tmp = Double.doubleToLongBits( this.metersPerPixel );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );

        code = code * 37 + this.tWidth;
        code = code * 37 + this.tHeight;

        code = code * 37 + ( enableCaching ? 1 : 0 );

        return (int) ( code >>> 32 ) ^ (int) code;
    }

    // public static void main( String[] args ) {
    // double minX = 20097.8899980;
    // double minY = 100097.77388778;
    // double maxX = 20197.27779919;
    // double maxY = 400097.89898984730;
    // float metersPerPixel = 0.088898989899f;
    // int textureHeight = 2008;
    // int textureWidth = 2103;
    // TextureTile tt = new TextureTile( minX, minY, maxX, maxY, textureWidth, textureHeight, (ByteBuffer) null,
    // false, false );
    // System.out.println( tt.hashCode() );
    //
    // // minX = 20097.8899980;
    // minX = 20097.88999778;
    // // minY = 100097.77388778;
    // minY = 100097.773887784;
    // // maxX = 20197.27779919;
    // maxX = 20197.2777992001;
    // // maxY = 400097.89898984730;
    // maxY = 400097.898989845999;
    // // metersPerPixel = 0.088898989899f;
    // metersPerPixel = 0.08889898f;
    // tt = new TextureTile( minX, minY, maxX, maxY, textureWidth, textureHeight, null, false, false );
    // System.out.println( tt.hashCode() );
    // }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof TextureTile ) {
            final TextureTile that = (TextureTile) other;
            return Math.abs( this.minX - that.minX ) < EPS && Math.abs( this.maxX - that.maxX ) < EPS
                   && Math.abs( this.minY - that.minY ) < EPS && Math.abs( this.maxY - that.maxY ) < EPS
                   && Math.abs( this.metersPerPixel - that.metersPerPixel ) < EPS && this.tWidth == that.tWidth
                   && this.tHeight == that.tHeight && this.enableCaching == that.enableCaching;
        }
        return false;
    }

}
