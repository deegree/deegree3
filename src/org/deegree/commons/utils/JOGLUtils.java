//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.commons.utils;

import static javax.media.opengl.GL.GL_UNSIGNED_BYTE;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import javax.media.opengl.GL;

import org.deegree.commons.i18n.Messages;

import com.sun.opengl.util.BufferUtil;

/**
 * JOGL-related utility methods.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class JOGLUtils {

    private static final int[] glTextureUnitIds = new int[32];

    static {
        int i = 0;
        glTextureUnitIds[i++] = GL.GL_TEXTURE0;
        glTextureUnitIds[i++] = GL.GL_TEXTURE1;
        glTextureUnitIds[i++] = GL.GL_TEXTURE2;
        glTextureUnitIds[i++] = GL.GL_TEXTURE3;
        glTextureUnitIds[i++] = GL.GL_TEXTURE4;
        glTextureUnitIds[i++] = GL.GL_TEXTURE5;
        glTextureUnitIds[i++] = GL.GL_TEXTURE6;
        glTextureUnitIds[i++] = GL.GL_TEXTURE7;
        glTextureUnitIds[i++] = GL.GL_TEXTURE8;
        glTextureUnitIds[i++] = GL.GL_TEXTURE9;
        glTextureUnitIds[i++] = GL.GL_TEXTURE10;
        glTextureUnitIds[i++] = GL.GL_TEXTURE11;
        glTextureUnitIds[i++] = GL.GL_TEXTURE12;
        glTextureUnitIds[i++] = GL.GL_TEXTURE13;
        glTextureUnitIds[i++] = GL.GL_TEXTURE14;
        glTextureUnitIds[i++] = GL.GL_TEXTURE15;
        glTextureUnitIds[i++] = GL.GL_TEXTURE16;
        glTextureUnitIds[i++] = GL.GL_TEXTURE17;
        glTextureUnitIds[i++] = GL.GL_TEXTURE18;
        glTextureUnitIds[i++] = GL.GL_TEXTURE19;
        glTextureUnitIds[i++] = GL.GL_TEXTURE20;
        glTextureUnitIds[i++] = GL.GL_TEXTURE21;
        glTextureUnitIds[i++] = GL.GL_TEXTURE22;
        glTextureUnitIds[i++] = GL.GL_TEXTURE23;
        glTextureUnitIds[i++] = GL.GL_TEXTURE24;
        glTextureUnitIds[i++] = GL.GL_TEXTURE25;
        glTextureUnitIds[i++] = GL.GL_TEXTURE26;
        glTextureUnitIds[i++] = GL.GL_TEXTURE27;
        glTextureUnitIds[i++] = GL.GL_TEXTURE28;
        glTextureUnitIds[i++] = GL.GL_TEXTURE29;
        glTextureUnitIds[i++] = GL.GL_TEXTURE30;
        glTextureUnitIds[i++] = GL.GL_TEXTURE31;
    }

    /**
     * Returns maximum texture size support by the GL driver.
     * 
     * @param gl
     * @return maximum texture size (pixels)
     */
    public static int getMaxTextureSize( GL gl ) {
        int[] valueBuffer = new int[1];
        gl.glGetIntegerv( GL.GL_MAX_TEXTURE_SIZE, valueBuffer, 0 );
        return valueBuffer[0];
    }

    /**
     * Returns the number of texture image units that can be used in fragment shaders.
     * <p>
     * NOTE: This method evaluates the value of <code>GL_MAX_TEXTURE_IMAGE_UNITS</code>. The number is usually not
     * identical to <code>GL_MAX_TEXTURE_UNITS</code> which denotes the maximum number of texture units in fixed
     * shader functions.
     * </p>
     * 
     * @param gl
     * @return number of texture image units
     */
    public static int getNumTextureImageUnits( GL gl ) {
        int[] valueBuffer = new int[1];
        gl.glGetIntegerv( GL.GL_MAX_TEXTURE_IMAGE_UNITS, valueBuffer, 0 );
        return valueBuffer[0];
    }

    /**
     * Returns the value of the symbolic constant TEXTURE0...TEXTURE31 for a certain texture unit id.
     * 
     * @param textureId
     *            id of the requested texture unit (0...31)
     * @return value of the corresponding symbolic constant
     */
    public static int getTextureUnitConst( int textureId ) {
        if ( textureId >= 0 && textureId <= glTextureUnitIds.length - 1 ) {
            return glTextureUnitIds[textureId];
        }
        throw new IllegalArgumentException( Messages.getMessage( "JOGL_INVALID_TEXTURE_UNIT", textureId ) );
    }

    /**
     * Get a string representation of the current modelview matrix of the given context.
     * 
     * @param gl
     * @return the String representation of modelview matrix
     */
    public static String getMVAsString( GL gl ) {
        return outputMatrix( gl, GL.GL_MODELVIEW_MATRIX );
    }

    /**
     * Get a string representation of the current Projection matrix of the given context.
     * 
     * @param gl
     * @return the String representation of projection matrix
     */
    public static String getProjectionAsString( GL gl ) {
        return outputMatrix( gl, GL.GL_PROJECTION_MATRIX );
    }

    /**
     * Get a string representation of the given matrix type of the given context.
     * 
     * @param gl
     * @param GL_MATRIX_TYPE
     *            one of {@link GL#GL_MODELVIEW_MATRIX}, {@link GL#GL_PROJECTION_MATRIX}
     * @return the String representation of given matrix
     */
    public static String outputMatrix( GL gl, int GL_MATRIX_TYPE ) {
        float[] mv = new float[16];
        gl.glGetFloatv( GL_MATRIX_TYPE, mv, 0 );
        StringBuilder sb = new StringBuilder();
        sb.append( mv[0] ).append( ",\t" ).append( mv[4] ).append( ",\t" ).append( mv[8] ).append( ",\t" ).append(
                                                                                                                   mv[12] );
        sb.append( "\n" );
        sb.append( mv[1] ).append( ",\t" ).append( mv[5] ).append( ",\t" ).append( mv[9] ).append( ",\t" ).append(
                                                                                                                   mv[13] );
        sb.append( "\n" );
        sb.append( mv[2] ).append( ",\t" ).append( mv[6] ).append( ",\t" ).append( mv[10] ).append( ",\t" ).append(
                                                                                                                    mv[14] );
        sb.append( "\n" );
        sb.append( mv[3] ).append( ",\t\t" ).append( mv[7] ).append( ",\t\t" ).append( mv[11] ).append( ",\t\t" ).append(
                                                                                                                          mv[15] );
        sb.append( "\n" );
        return sb.toString();

    }

    /**
     * Calculate the eye position from the given modelview, note, no scale may be applied.
     * 
     * @param gl
     *            to get the modelview from.
     * @return the eye position of the modelview matrix.
     */
    public static float[] getEyeFromModelView( GL gl ) {
        float[] originalModelView = new float[16];
        gl.glGetFloatv( GL.GL_MODELVIEW_MATRIX, originalModelView, 0 );

        float[] newEye = new float[3];
        float[] t = new float[] { -originalModelView[12], -originalModelView[13], -originalModelView[14] };
        newEye[0] = originalModelView[0] * t[0] + originalModelView[1] * t[1] + originalModelView[2] * t[2];
        newEye[1] = originalModelView[4] * t[0] + originalModelView[5] * t[1] + originalModelView[6] * t[2];
        newEye[2] = originalModelView[8] * t[0] + originalModelView[9] * t[1] + originalModelView[10] * t[2];
        return newEye;
    }

    /**
     * Create an int with rgba from the given color (which returns the argb values).
     * 
     * @param color
     *            to be converted.
     * @return the color as an int holding rgba.
     */
    public static int convertColorGLColor( Color color ) {
        int oldColor = color.getRGB();
        int alpha = ( oldColor >> 24 ) & 0xFF;
        int red = ( oldColor >> 16 ) & 0xFF;
        int green = ( oldColor >> 8 ) & 0xFF;
        int blue = oldColor & 0xFF;

        int newColor = alpha;
        newColor |= ( red << 24 );
        newColor |= ( green << 16 );
        newColor |= ( blue << 8 );
        return newColor;
    }

    /**
     * Create an a float array from the given color object, which can be used for rendering with jogl.
     * 
     * @param color
     *            to be converted.
     * @return the color as an float array holding rgba.
     */
    public static float[] convertColorFloats( Color color ) {
        int c = convertColorGLColor( color );
        return convertColorIntAsFloats( c );
    }

    /**
     * The float array appropriate for opengl.
     * 
     * @param color
     *            (rgba) to be converted into a float arra.
     * @return the float array ready to be rendered.
     */
    public static float[] convertColorIntAsFloats( int color ) {
        return new float[] { ( ( color >> 24 ) & 0xFF ) / 255f, ( ( color >> 16 ) & 0xFF ) / 255f,
                            ( ( color >> 8 ) & 0xFF ) / 255f, ( color & 0xFF ) / 255f, };
    }

    /**
     * Create an int value ([a]rgb) from the given color array (rgb[a]), the result can be used for buffered images.
     * 
     * @param color
     *            to be converted may be of length 3 or 4, not <code>null</code>.
     * @return the color as an int holding argb.
     */
    public static int convertBytesToARGBInt( byte[] color ) {
        int result = convertBytesToRGBInt( color );
        if ( color.length == 4 ) {
            result = color[3] & 0x000000FF;
            result <<= 8;
        }
        return result;
    }

    /**
     * Create an int value (rgb) from the given color array (rgb), the result can be used for buffered images.
     * 
     * @param color
     *            to be converted may be only be of length 3, not <code>null</code>.
     * @return the color as an int holding rgb.
     */
    public static int convertBytesToRGBInt( byte[] color ) {
        int result = 0;
        result |= ( color[0] & 0x000000FF );
        result <<= 8;
        result |= ( color[1] & 0x000000FF );
        result <<= 8;
        result |= ( color[2] & 0x000000FF );

        return result;
    }

    /**
     * Read the framebuffer's rgb values and place them into a {@link BufferedImage}. If the resultImage was
     * <code>null</code> or it's height or width are to small a new BufferedImage is created. The resultImage type is
     * supposed to be {@link BufferedImage#TYPE_INT_RGB}.
     * 
     * @param glContext
     *            to get the image from.
     * @param imageBuffer
     *            to write the framebuffer to.
     * @param viewPortX
     *            the x location in the framebuffer.
     * @param viewPortY
     *            the y location in the framebuffer.
     * @param width
     *            of read in the framebuffer.
     * @param height
     *            of read in the framebuffer.
     * @param resultImage
     *            which will hold the result, may be <code>null</code>
     * @return the resultImage or a newly created BufferedImage holding the requested data from the framebuffer.
     */
    public static BufferedImage getFrameBufferRGB( GL glContext, ByteBuffer imageBuffer, int viewPortX, int viewPortY,
                                                   int width, int height, BufferedImage resultImage ) {
        if ( imageBuffer == null || ( imageBuffer.capacity() < ( width * height * 3 ) ) ) {
            imageBuffer = BufferUtil.newByteBuffer( width * height * 3 );
        }
        imageBuffer.rewind();
        glContext.glPixelStorei( GL.GL_PACK_ALIGNMENT, 1 );
        glContext.glReadPixels( viewPortX, viewPortY, width, height, GL.GL_RGB, GL_UNSIGNED_BYTE, imageBuffer );
        if ( resultImage == null || resultImage.getWidth() < width || resultImage.getHeight() < height ) {
            resultImage = new BufferedImage( width, height, BufferedImage.TYPE_3BYTE_BGR );
        }
        imageBuffer.rewind();
        byte[] color = new byte[3];
        for ( int y = height - 1; y >= 0; --y ) {
            for ( int x = 0; x < width; x++ ) {
                imageBuffer.get( color );
                resultImage.setRGB( x, y, convertBytesToRGBInt( color ) );
            }
        }

        return resultImage;
    }
}
