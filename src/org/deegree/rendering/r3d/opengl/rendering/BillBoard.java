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

import javax.media.opengl.GL;
import javax.vecmath.Vector3f;

import org.deegree.rendering.r3d.opengl.rendering.texture.TexturePool;

/**
 * The <code>BillBoard</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class BillBoard implements Renderable {

    private static final transient Vector3f UP_VECTOR = new Vector3f( 0, 0, 1 );

    /**
     * 
     */
    private static final long serialVersionUID = -5693355972373810535L;

    private String texture;

    /**
     * Constructs a billboard data structure with the given texture id.
     * 
     * @param texture
     */
    public BillBoard( String texture ) {
        this.texture = texture;
    }

    @Override
    public void render( GL context, Vector3f eye ) {
        context.glPushMatrix();
        setTransformation( context, eye );
        TexturePool.loadTexture( context, texture );
        context.glBegin( GL.GL_QUADS );
        {
            context.glVertex3f( 0, 0, 0 );
            context.glTexCoord2f( 0, 0 );

            context.glVertex3f( 1, 0, 0 );
            context.glTexCoord2f( 1, 0 );

            context.glVertex3f( 1, 0, 1 );
            context.glTexCoord2f( 1, 1 );

            context.glVertex3f( 0, 0, 1 );
            context.glTexCoord2f( 0, 1 );
        }
        context.glEnd();
        context.glPopMatrix();
    }

    /**
     * @param context
     */
    private void setTransformation( GL context, Vector3f eye ) {
        float originalModelView[] = new float[16];
        context.glGetFloatv( GL.GL_MODELVIEW_MATRIX, originalModelView, 0 );

        // Calculate the rotationmatrix
        calcViewAllignment( originalModelView, eye );

        context.glLoadMatrixf( originalModelView, 0 );
    }

    /**
     * Calculates the rotation matrix for this billboard, by crossing the view direction with the up vector, normalizing
     * the result and than calculate the normal by calculating the normalized cross from the right and up vector, and
     * placing the result in the rotation part of the modelview matrix.<code>
     * r = u x viewdirection
     * normalize( r );
     * n = u x r
     * normalize( n )
     * 
     * modelview.rotation= (r, u, n )
     * </code>
     * 
     * @param modelview
     * @param eye
     */
    private void calcViewAllignment( float[] modelview, Vector3f eye ) {
        Vector3f r = new Vector3f();
        Vector3f viewDirection = new Vector3f( eye );
        viewDirection.negate();
        r.cross( UP_VECTOR, viewDirection );
        r.normalize();
        viewDirection.cross( r, UP_VECTOR );
        viewDirection.normalize();

        modelview[0] = r.x;
        modelview[1] = r.y;
        modelview[2] = r.z;

        modelview[4] = UP_VECTOR.x;
        modelview[5] = UP_VECTOR.y;
        modelview[6] = UP_VECTOR.z;

        modelview[8] = viewDirection.x;
        modelview[9] = viewDirection.y;
        modelview[10] = viewDirection.z;
    }
}
