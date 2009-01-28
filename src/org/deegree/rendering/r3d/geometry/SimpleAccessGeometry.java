//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/wpvs/model/data3d/coordinates/SimpleAccessGeometry.java $
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

package org.deegree.rendering.r3d.geometry;

import java.io.IOException;
import java.io.Serializable;

import org.deegree.commons.utils.math.Vectors3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>SimpleAccessGeometry</code> class, defines geometry by a coordinate array with or without innerrings and
 * a set of colors.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author: rbezema $
 * 
 * @version $Revision: 15598 $, $Date: 2009-01-12 15:03:49 +0100 (Mo, 12 Jan 2009) $
 * 
 */
public class SimpleAccessGeometry implements Serializable {

    private final static Logger LOG = LoggerFactory.getLogger( SimpleAccessGeometry.class );

    /**
     * 
     */
    private static final long serialVersionUID = -3704718653663592552L;

    /**
     * The coordinates of this geometry may be null
     */
    protected transient float[] coordinates = null;

    // indizes of the startpositions of the innerrings
    private transient int[] innerRings = null;

    // length 32 bit only lowest 24 are used rgb
    private transient int specularColor;

    // length 32 bit only lowest 24 are used rgb
    private transient int ambientColor;

    // length 32 bit only lowest 24 are used rgb
    private transient int diffuseColor;

    // length 32 bit only lowest 24 are used rgb
    private transient int emmisiveColor;

    // a single value
    private transient float shininess;

    private transient int vertexCount;

    /**
     * @param coordinates
     * @param innerRings
     * @param specularColor
     * @param ambientColor
     * @param diffuseColor
     * @param emmisiveColor
     * @param shininess
     */
    public SimpleAccessGeometry( float[] coordinates, int[] innerRings, int specularColor, int ambientColor,
                                 int diffuseColor, int emmisiveColor, float shininess ) {
        this.coordinates = coordinates;
        this.innerRings = innerRings;
        this.specularColor = specularColor;
        this.ambientColor = ambientColor;
        this.diffuseColor = diffuseColor;
        this.emmisiveColor = emmisiveColor;
        this.shininess = shininess;
        vertexCount = ( coordinates == null ) ? 0 : ( coordinates.length / 3 );
    }

    /**
     * @param coordinates
     * @param specularColor
     * @param ambientColor
     * @param diffuseColor
     * @param emmisiveColor
     * @param shininess
     */
    public SimpleAccessGeometry( float[] coordinates, int specularColor, int ambientColor, int diffuseColor,
                                 int emmisiveColor, float shininess ) {
        this( coordinates, null, specularColor, ambientColor, diffuseColor, emmisiveColor, shininess );
    }

    /**
     * @param coordinates
     * @param innerRings
     *            containing indizes to the vertex, not the array offset.
     */
    public SimpleAccessGeometry( float[] coordinates, int[] innerRings ) {
        this( coordinates, innerRings, 0x00FFFFFF, 0x00FFFFFF, 0x00FFFFFF, 0x00FFFFFF, 1 );
    }

    /**
     * @param coordinates
     */
    public SimpleAccessGeometry( float[] coordinates ) {
        this( coordinates, null, 0x00FFFFFF, 0x00FFFFFF, 0x00FFFFFF, 0x00FFFFFF, 1 );
    }

    /**
     * @return the coordinates
     */
    public final float[] getGeometry() {
        return coordinates;
    }

    /**
     * @param coordinates
     *            the originalGeometry to set
     */
    public final void setGeometry( float[] coordinates ) {
        this.coordinates = coordinates;
    }

    /**
     * @return the specularColor
     */
    public final int getSpecularColor() {
        return specularColor;
    }

    /**
     * @param specularColor
     *            the specularColor to set
     */
    public final void setSpecularColor( int specularColor ) {
        this.specularColor = specularColor;
    }

    /**
     * @return the ambientColor
     */
    public final int getAmbientColor() {
        return ambientColor;
    }

    /**
     * @param ambientColor
     *            the ambientColor to set
     */
    public final void setAmbientColor( int ambientColor ) {
        this.ambientColor = ambientColor;
    }

    /**
     * @return the diffuseColor
     */
    public final int getDiffuseColor() {
        return diffuseColor;
    }

    /**
     * @param diffuseColor
     *            the diffuseColor to set
     */
    public final void setDiffuseColor( int diffuseColor ) {
        this.diffuseColor = diffuseColor;
    }

    /**
     * @return the emmisiveColor
     */
    public final int getEmmisiveColor() {
        return emmisiveColor;
    }

    /**
     * @param emmisiveColor
     *            the emmisiveColor to set
     */
    public final void setEmmisiveColor( int emmisiveColor ) {
        this.emmisiveColor = emmisiveColor;
    }

    /**
     * @return the shininess
     */
    public final float getShininess() {
        return shininess;
    }

    /**
     * @param shininess
     *            the shininess to set
     */
    public final void setShininess( float shininess ) {
        this.shininess = shininess;
    }

    /**
     * @return the innerRings
     */
    public final int[] getInnerRings() {
        return innerRings;
    }

    /**
     * @param innerRings
     *            the innerRings to set
     */
    public final void setInnerRings( int[] innerRings ) {
        this.innerRings = innerRings;
    }

    /**
     * @param coordinateLocation
     *            of the x ordinate of the requested coordinate.
     * @return a copy of the coordinate at the given location (location, location +1,location +2)
     * @throws IndexOutOfBoundsException
     *             is outside the coordinate array
     */
    public float[] getCoordinate( int coordinateLocation ) {
        if ( coordinates == null || coordinateLocation < 0 || coordinateLocation + 2 > coordinates.length ) {
            throw new IndexOutOfBoundsException( "Location is out of the range" );
        }
        return new float[] { coordinates[coordinateLocation], coordinates[coordinateLocation + 1],
                            coordinates[coordinateLocation + 2] };
    }

    /**
     * @param vertex
     *            the vertex index(starting at 0), e.g. if you would like to get the coordinates of the second vertex
     *            the vertex index would be 1
     * @return a copy of the coordinates for the vertex at given index
     * @throws IndexOutOfBoundsException
     *             is outside the coordinate array
     */
    public float[] getCoordinateForVertex( int vertex ) {
        if ( coordinates == null || vertex < 0 || ( vertex * 3 ) + 2 > coordinates.length ) {
            throw new IndexOutOfBoundsException( "No such vertex, the given index is out of range" );
        }
        return new float[] { coordinates[vertex * 3], coordinates[( vertex * 3 ) + 1], coordinates[( vertex * 3 ) + 2] };
    }

    /**
     * @return the vertexCount
     */
    public final int getVertexCount() {
        return vertexCount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if ( coordinates != null && coordinates.length > 0 ) {
            sb.append( "VertexCount: " ).append( vertexCount );
            sb.append( "\nCoordinates:\n" );
            for ( int i = 0; i < vertexCount; ++i ) {
                sb.append( i ).append( ":" ).append( Vectors3f.asString( getCoordinateForVertex( i ) ) ).append( "\n" );
            }
            if ( innerRings != null && innerRings.length > 0 ) {
                sb.append( "\nInnerRings at vertices: " );
                for ( int i = 0; i < innerRings.length; ++i ) {
                    sb.append( innerRings[i] );
                    if ( ( i + 1 ) < innerRings.length ) {
                        sb.append( ", " );
                    }
                }
            } else {
                sb.append( "No Inner rings defined." );
            }
        } else {
            sb.append( "No geometry coordinates defined." );
        }
        sb.append( "\nspecularColor: " ).append( specularColor );
        sb.append( "\nambientColor: " ).append( ambientColor );
        sb.append( "\ndiffuseColor: " ).append( diffuseColor );
        sb.append( "\nemmisiveColor: " ).append( emmisiveColor );
        sb.append( "\nshininess: " ).append( shininess );
        return sb.toString();
    }

    /**
     * Method called while serializing this object
     * 
     * @param out
     *            to write to.
     * @throws IOException
     */
    private void writeObject( java.io.ObjectOutputStream out )
                            throws IOException {
        LOG.trace( "Serializing to object stream" );

        out.writeObject( coordinates );
        out.writeObject( innerRings );
        out.writeInt( specularColor );
        out.writeInt( ambientColor );
        out.writeInt( diffuseColor );
        out.writeInt( emmisiveColor );
        out.writeFloat( shininess );
        out.writeInt( vertexCount );

    }

    /**
     * Method called while de-serializing (instancing) this object.
     * 
     * @param in
     *            to create the methods from.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject( java.io.ObjectInputStream in )
                            throws IOException, ClassNotFoundException {
        LOG.trace( "Deserializing from object stream" );
        // The coordinates of this geometry may be null
        coordinates = (float[]) in.readObject();
        // indizes of the startpositions of the innerrings
        innerRings = (int[]) in.readObject();
        // length 32 bit only lowest 24 are used rgb
        specularColor = in.readInt();
        // length 32 bit only lowest 24 are used rgb
        ambientColor = in.readInt();
        // length 32 bit only lowest 24 are used rgb
        diffuseColor = in.readInt();
        // length 32 bit only lowest 24 are used rgb
        emmisiveColor = in.readInt();
        // a single value
        shininess = in.readFloat();
        vertexCount = in.readInt();
    }
}
