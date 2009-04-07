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

package org.deegree.rendering.r3d.opengl.rendering.prototype;

import javax.media.opengl.GL;

import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.opengl.rendering.RenderableGeometry;

/**
 * The <code>BOXGeometry</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class BOXGeometry extends RenderableGeometry {

    private final float testObjectSize = 1f;

    private final float cubeHalf = testObjectSize * 0.5f;

    private final float[][] cubeData = { { -cubeHalf, -cubeHalf, testObjectSize },
                                        { cubeHalf, -cubeHalf, testObjectSize },
                                        { cubeHalf, cubeHalf, testObjectSize },
                                        { -cubeHalf, cubeHalf, testObjectSize }, { -cubeHalf, -cubeHalf, 0 },
                                        { cubeHalf, -cubeHalf, 0 }, { cubeHalf, cubeHalf, 0 },
                                        { -cubeHalf, cubeHalf, 0 } };

    @Override
    public void render( GL context, ViewParams params ) {

        drawCube( context );
    }

    @Override
    protected void enableArrays( GL context ) {
        super.disableArrays( context );
    }

    /**
     * @param vertices
     * @param openGLType
     */
    public BOXGeometry() {
        super( new float[] { 0, 0, 0 }, GL.GL_QUADS );
    }

    /*
     * Draws a colored cube with cubeHalf length defined by the ObjectSize.
     */
    private void drawCube( GL gl ) {
        gl.glPushAttrib( GL.GL_CURRENT_BIT | GL.GL_LIGHTING_BIT );
        gl.glDisable( GL.GL_BLEND );
        float[] color = new float[] { 1, 0, 0 };
        gl.glMaterialfv( GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, color, 0 );

        gl.glBegin( GL.GL_QUADS );
        // Front face
        gl.glNormal3f( 0, 0, 1 );
        gl.glVertex3fv( cubeData[0], 0 );
        gl.glVertex3fv( cubeData[1], 0 );
        gl.glVertex3fv( cubeData[2], 0 );
        gl.glVertex3fv( cubeData[3], 0 );
        // // Back face
        // gl.glNormal3f( 0, 0, -1 );
        // gl.glVertex3fv( cubeData[5], 0 );
        // gl.glVertex3fv( cubeData[4], 0 );
        // gl.glVertex3fv( cubeData[7], 0 );
        // gl.glVertex3fv( cubeData[6], 0 );
        gl.glEnd();

        color = new float[] { 0, 1, 0 };
        gl.glMaterialfv( GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, color, 0 );

        gl.glBegin( GL.GL_QUADS );
        // Left face
        gl.glNormal3f( -1, 0, 0 );
        gl.glVertex3fv( cubeData[4], 0 );
        gl.glVertex3fv( cubeData[0], 0 );
        gl.glVertex3fv( cubeData[3], 0 );
        gl.glVertex3fv( cubeData[7], 0 );
        // Right face
        gl.glNormal3f( 1, 0, 0 );
        gl.glVertex3fv( cubeData[1], 0 );
        gl.glVertex3fv( cubeData[5], 0 );
        gl.glVertex3fv( cubeData[6], 0 );
        gl.glVertex3fv( cubeData[2], 0 );
        gl.glEnd();

        color = new float[] { 0, 0, 1 };
        gl.glMaterialfv( GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, color, 0 );
        gl.glBegin( GL.GL_QUADS );
        // Top face
        gl.glNormal3f( 0, 1, 0 );
        gl.glVertex3fv( cubeData[3], 0 );
        gl.glVertex3fv( cubeData[2], 0 );
        gl.glVertex3fv( cubeData[6], 0 );
        gl.glVertex3fv( cubeData[7], 0 );
        // Bottom face
        gl.glNormal3f( 0, -1, 0 );
        gl.glVertex3fv( cubeData[4], 0 );
        gl.glVertex3fv( cubeData[5], 0 );
        gl.glVertex3fv( cubeData[1], 0 );
        gl.glVertex3fv( cubeData[0], 0 );
        gl.glEnd();
        gl.glEnable( GL.GL_BLEND );
        gl.glPopAttrib();
    }

}
