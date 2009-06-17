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

package org.deegree.rendering.r3d.opengl.tesselation;

/**
 * The <code>Vertex</code> helper class for the tesselation algorithm
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class Vertex {
    float x;

    float y;

    float z;

    float[] normal;

    private float[] normalizedNormal = null;

    /**
     * Create a vertex from the given coordinates and the surrounding vertices.
     *
     * @param coordinates
     *            of new vertex
     * @param otherVertices
     *            which were used (combined) to create this vertex from.
     */
    Vertex( double[] coordinates, Vertex[] otherVertices, float[] weights ) {
        x = (float) coordinates[0];
        y = (float) coordinates[1];
        z = (float) coordinates[2];
        if ( otherVertices != null && otherVertices.length > 0 && otherVertices.length == weights.length ) {
            calcColorAndNormal( otherVertices, weights );
        } else {
            throw new IllegalArgumentException( "Weights and vertices may not be null and must have same length" );
        }
    }

    /**
     * @param otherVertices
     * @param weights
     */
    private void calcColorAndNormal( Vertex[] otherVertices, float[] weights ) {
        for ( int i = 0; i < otherVertices.length; ++i ) {
            Vertex v = otherVertices[i];
            if ( v != null ) {
                float w = weights[i];
                if ( v.hasNormal() ) {
                    // Lazy instancing
                    if ( this.normal == null ) {
                        this.normal = new float[3];
                    }
                    this.normal[0] += w * v.normal[0];
                    this.normal[1] += w * v.normal[1];
                    this.normal[2] += w * v.normal[2];
                }

            }
        }
    }

    Vertex( float[] coordinates ) {
        this( coordinates, null );
    }

    Vertex( float[] coordinates, float[] normal ) {
        if ( coordinates == null || coordinates.length != 3 ) {
            throw new IllegalArgumentException( "Only 3d coordinates are supported." );
        }

        x = coordinates[0];
        y = coordinates[1];
        z = coordinates[2];
        this.normal = normal;
    }

    /**
     * @return the x, y ,z as a double array
     */
    public double[] getCoordsAsDouble() {
        return new double[] { x, y, z };
    }

    /**
     * @return the x, y ,z as a an array
     */
    public float[] getCoords() {
        return new float[] { x, y, z };
    }

    /**
     * @return the x, y ,z as a an array
     */
    public float[] getNormal() {
        return normal;
    }

    /**
     * @return the x, y ,z as a an array
     */
    public float[] getNormalizedNormal() {
        if ( normalizedNormal == null ) {
            double length = Math.sqrt( normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2] );
            if ( Math.abs( length - 1 ) < 1E-10 ) {
                normalizedNormal = normal;
            } else {
                normalizedNormal = new float[] { (float) ( normal[0] / length ), (float) ( normal[1] / length ),
                                                (float) ( normal[2] / length ) };
            }
        }
        return normalizedNormal;
    }

    /**
     * @return true if the normal != <code>null</code>
     */
    public boolean hasNormal() {
        return normal != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "coord:\t" ).append( x ).append( "," ).append( y ).append( "," ).append( z );

        if ( normal == null ) {
            sb.append( "\nno normal" );
        } else {
            sb.append( "\nnormal:\t" );
            sb.append( normal[0] ).append( "," ).append( normal[1] ).append( "," ).append( normal[2] );
        }

        return sb.toString();
    }
}
