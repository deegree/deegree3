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

package org.deegree.rendering.r3d.opengl.tesselation;

/**
 * The <code>TexturedVertex</code> a textured vertex.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class TexturedVertex extends Vertex {

    float tex_u;

    float tex_v;

    /**
     * Construct a textured vertex and calculate it's values from the given vertices
     * 
     * @param coordinates
     *            of the vertex may not be <code>null</code> and must have a length of 3
     */
    TexturedVertex( double[] coordinates, TexturedVertex[] otherVertices, float[] weights ) {
        super( coordinates, otherVertices, weights );
        // checked in super class:
        // otherVertices != null && weights != null && otherVertices.length == weights.length ) {
        calcTexCoords( otherVertices, weights );
    }

    /**
     * Use the weights to calculate the u/v coordinates of this vertex
     * 
     * @param otherVertices
     *            used to create this vertex
     * @param weights
     *            adding up to 1
     */
    private void calcTexCoords( TexturedVertex[] otherVertices, float[] weights ) {
        // aufaddieren mit gewichtung
        tex_u = 0;
        tex_v = 0;
        for ( int i = 0; i < otherVertices.length; ++i ) {
            TexturedVertex v = otherVertices[i];
            if ( v != null ) {
                float w = weights[i];
                tex_u += v.tex_u * w;
                tex_v += v.tex_v * w;
            }
        }
    }

    /**
     * Construct a textured vertex with a white color and a 1,0,0 normal.
     * 
     * @param coordinates
     *            of the vertex may not be <code>null</code> and must have a length of 3
     * @param textureCoordinates
     *            of the vertex may not be <code>null</code> and must have a length of 2
     */
    TexturedVertex( float[] coordinates, float[] textureCoordinates ) {
        this( coordinates, new float[] { 1, 0, 0 }, 0x00FFFFFF, textureCoordinates );
    }

    /**
     * Construct a textured vertex with given color and normal.
     * 
     * @param coordinates
     *            of the vertex may not be <code>null</code> and must have a length of 3
     * @param normal
     *            if <code>null</code> 1,0,0 will be used.
     * @param vertexColor
     *            ARGB e.g. 0x00 FF FF FF
     * @param textureCoordinates
     *            of the vertex may not be <code>null</code> and must have a length of 2
     */
    public TexturedVertex( float[] coordinates, float[] normal, int vertexColor, float[] textureCoordinates ) {
        super( coordinates, normal, vertexColor );
        if ( textureCoordinates == null || textureCoordinates.length != 2 ) {
            throw new IllegalArgumentException( "Only 2d texture coordinates are supported." );
        }
        tex_u = textureCoordinates[0];
        tex_v = textureCoordinates[1];
    }

    /**
     * 
     * @return the u and v (x,y) texture coordinates of this vertex.
     */
    public float[] getTextureCoords() {
        return new float[] { tex_u, tex_v };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( super.toString() );
        sb.append( "\ntex_coords:\t" ).append( tex_u ).append( "," ).append( tex_v );
        return sb.toString();
    }
}
