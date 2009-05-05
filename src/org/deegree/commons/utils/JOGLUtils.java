//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.commons.utils;

import javax.media.opengl.GL;

import org.deegree.commons.i18n.Messages;

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
     * Returns the number of texture image units that can be used in fragment shaders.
     * <p>
     * NOTE: This method evaluates the value of <code>GL_MAX_TEXTURE_IMAGE_UNITS</code>. The number is usually not
     * identical to <code>GL_MAX_TEXTURE_UNITS</code> which denotes the maximum number of texture units in fixed
     * shader functions.
     * </p>
     * 
     * @param gl
     * @return
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
     * output the current modelview matrix of the given context.
     * 
     * @param gl
     */
    public static void outputMV( GL gl ) {
        float[] mv = new float[16];
        gl.glGetFloatv( GL.GL_MODELVIEW_MATRIX, mv, 0 );
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
        System.out.println( sb.toString() );

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
}
