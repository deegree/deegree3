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

package org.deegree.rendering.r3d.opengl.rendering;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.vecmath.Vector3f;

import org.deegree.commons.utils.math.Vectors3f;
import org.deegree.rendering.r3d.opengl.rendering.texture.TexturePool;

import com.sun.opengl.util.BufferUtil;

/**
 * The <code>BillBoard</code> class represents a billboard in object always facing the viewer, with the z-axis as it's
 * rotation axis.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class BillBoard extends RenderableQualityModel {

    /**
     * 
     */
    private static final long serialVersionUID = -5693355972373810535L;

    private String texture;

    private final static transient float[] NORMAL = new float[] { 0, -1, 0 };

    private float[] location;

    private float[] scaleXZ;

    private static final transient FloatBuffer coordBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( new float[] {
                                                                                                                        -.5f,
                                                                                                                        0,
                                                                                                                        -.5f, // ll
                                                                                                                        .5f,
                                                                                                                        0,
                                                                                                                        -.5f,// lr
                                                                                                                        .5f,
                                                                                                                        0,
                                                                                                                        .5f,// ur
                                                                                                                        -.5f,
                                                                                                                        0,
                                                                                                                        .5f }// ul
    ) );

    private static final transient FloatBuffer textureBuffer = BufferUtil.copyFloatBuffer( FloatBuffer.wrap( new float[] {
                                                                                                                          0,
                                                                                                                          1,
                                                                                                                          1,
                                                                                                                          1,
                                                                                                                          1,
                                                                                                                          0,
                                                                                                                          0,
                                                                                                                          0 } ) );

    /**
     * Constructs a billboard data structure with the given texture id.
     * 
     * @param texture
     * @param location
     *            of the billboard
     * @param scaleXZ
     *            the width and height of the billboard
     */
    public BillBoard( String texture, float[] location, float[] scaleXZ ) {
        this.texture = texture;
        this.location = location;
        this.scaleXZ = scaleXZ;
    }

    @Override
    public void render( GL context, Vector3f eye ) {
        context.glPushMatrix();
        context.glEnable( GL.GL_TEXTURE_2D );
        context.glEnableClientState( GL.GL_VERTEX_ARRAY );
        context.glEnableClientState( GL.GL_TEXTURE_COORD_ARRAY );
        // the translation
        context.glTranslatef( location[0], location[1], location[2] );
        // the rotation
        calculateAndSetRotation( context, new float[] { eye.x, eye.y, eye.z } );
        context.glScalef( scaleXZ[0], 1, scaleXZ[1] );

        TexturePool.loadTexture( texture );

        context.glVertexPointer( 3, GL.GL_FLOAT, 0, coordBuffer );

        context.glTexCoordPointer( 2, GL.GL_FLOAT, 0, textureBuffer );
        context.glDrawArrays( GL.GL_QUADS, 0, 4 );
        context.glDisableClientState( GL.GL_VERTEX_ARRAY );
        context.glDisableClientState( GL.GL_TEXTURE_COORD_ARRAY );
        context.glDisable( GL.GL_TEXTURE_2D );
        context.glPopMatrix();
    }

    /**
     * Normalize the viewVector, the inner product (dot) between billboard normal and viewVector will allow the
     * computation of the cosine of the angle. However knowing the cosine alone is not enough, since the cos(a) =
     * cos(-a). Computing the cross product as well (the new up-vector) allows us to uniquely determine the angle. The
     * cross product vector will have the same direction as the up vector if the angle is positive. For negative angles
     * the up vector's direction will opposed to the up vector, effectively reversing the rotation. from
     * http://www.lighthouse3d.com/opengl/billboarding/index.php?billCyl
     * 
     * @param context
     *            to set the translation to
     * @param eye
     *            the position of the camera in world coordinates
     */
    private void calculateAndSetRotation( GL context, float[] eye ) {

        float[] viewVector = Vectors3f.sub( eye, location );
        // projection to the xy plane.
        viewVector[2] = 0;
        Vectors3f.normalizeInPlace( viewVector );
        double angleCosine = Vectors3f.dot( viewVector, NORMAL );
        // only do a rotation the angles are between -1 and 1.
        if ( ( angleCosine < 0.99990 ) && ( angleCosine > -0.9999 ) ) {
            // negative or positive orientation?
            float[] newUp = Vectors3f.cross( NORMAL, viewVector );
            context.glRotatef( (float) Math.toDegrees( Math.acos( angleCosine ) ), newUp[0], newUp[1], newUp[2] );
        }
    }
}
