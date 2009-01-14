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

import org.deegree.rendering.r3d.geometry.SimpleAccessGeometry;

/**
 * The <code>RenderableGeometry</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class RenderableGeometry extends SimpleAccessGeometry implements Renderable {

    /**
     * 
     */
    private static final long serialVersionUID = -7188698491925826649L;

    /**
     * an array of integers which can be used to store gl pointers (vertexlists, displaylist etc).
     */
    protected transient int[] glBufferIDs;

    // have a look at GL class.
    private int openGLType;

    // 3D, same length as renderableGeometry
    private float[] vertexNormals = null;

    // 4D (RGBA)
    private int[] vertexColors;

    /**
     * @param geometry
     * @param openGLType
     * @param vertexNormals
     * @param vertexColors
     * @param specularColor
     * @param ambientColor
     * @param diffuseColor
     * @param emmisiveColor
     * @param shininess
     */
    public RenderableGeometry( float[] geometry, int openGLType, float[] vertexNormals, int[] vertexColors,
                               int specularColor, int ambientColor, int diffuseColor, int emmisiveColor, float shininess ) {
        super( geometry, specularColor, ambientColor, diffuseColor, emmisiveColor, shininess );
        this.openGLType = openGLType;
        this.vertexNormals = vertexNormals;
        this.vertexColors = vertexColors;
    }

    /**
     * @param geometry
     * @param openGLType
     */
    public RenderableGeometry( float[] geometry, int openGLType ) {
        this( geometry, openGLType, null, null, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 1 );
    }

    @Override
    public void render( GL context, Vector3f eye ) {
        // nottin yet
    }

    /**
     * @param geometry
     *            the originalGeometry to set
     * @param openGLType
     */
    public final void setGeometry( float[] geometry, int openGLType ) {
        setGeometry( geometry );
        this.openGLType = openGLType;
    }

    /**
     * @return the vertexNormals
     */
    public final float[] getVertexNormals() {
        return vertexNormals;
    }

    /**
     * @param vertexNormals
     *            the vertexNormals to set
     */
    public final void setVertexNormals( float[] vertexNormals ) {
        this.vertexNormals = vertexNormals;
    }

    /**
     * @return the vertexColors
     */
    public final int[] getVertexColors() {
        return vertexColors;
    }

    /**
     * @param vertexColors
     *            the vertexColors to set
     */
    public final void setVertexColors( int[] vertexColors ) {
        this.vertexColors = vertexColors;
    }

    /**
     * @return the openGLType
     */
    public final int getOpenGLType() {
        return openGLType;
    }
}
