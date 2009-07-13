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

package org.deegree.rendering.r3d.opengl.rendering.model.prototype;

import javax.media.opengl.GL;

import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.DirectGeometryBuffer;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableGeometry;

/**
 * The <code>BOXGeometry</code> a simple box, which might be used as a prototype reference.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class BOXGeometry extends RenderableGeometry {

    /**
     * 
     */
    private static final long serialVersionUID = 1698575727921120208L;

    private final float testObjectSize = 1f;

    private final float cubeHalf = testObjectSize * 0.5f;

    private final float[][] cubeData = { { -cubeHalf, -cubeHalf, testObjectSize },
                                        { cubeHalf, -cubeHalf, testObjectSize },
                                        { cubeHalf, cubeHalf, testObjectSize },
                                        { -cubeHalf, cubeHalf, testObjectSize }, { -cubeHalf, -cubeHalf, 0 },
                                        { cubeHalf, -cubeHalf, 0 }, { cubeHalf, cubeHalf, 0 },
                                        { -cubeHalf, cubeHalf, 0 } };

    private final float[] color = { 0.8f, 0.8f, 0.8f };

    @Override
    public void render( RenderContext glRenderContext ) {
        drawCube( glRenderContext.getContext() );
    }

    @Override
    protected void enableArrays( RenderContext glRenderContext, DirectGeometryBuffer geomBuffer ) {
        super.disableArrays( glRenderContext );
    }

    /**
     * An empty consturctor.
     */
    public BOXGeometry() {
        super( new float[] { 0, 0, 0 }, GL.GL_QUADS, null, true );
    }

    /**
     * draws the cube
     * 
     * @param gl
     */
    public void drawCube( GL gl ) {
        gl.glPushAttrib( GL.GL_CURRENT_BIT | GL.GL_LIGHTING_BIT );
        gl.glMaterialfv( GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, color, 0 );

        gl.glBegin( GL.GL_QUADS );
        // Front face
        gl.glNormal3f( 0, 0, 1 );
        gl.glVertex3fv( cubeData[0], 0 );
        gl.glVertex3fv( cubeData[1], 0 );
        gl.glVertex3fv( cubeData[2], 0 );
        gl.glVertex3fv( cubeData[3], 0 );
        gl.glEnd();

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
        gl.glPopAttrib();
    }
}
