//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/wpvs/model/data3d/coordinates/SimpleAccessGeometry.java $
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

package org.deegree.rendering.r3d.model.geometry;

import java.io.IOException;

import org.deegree.commons.utils.math.Vectors3f;
import org.deegree.commons.utils.memory.AllocatedHeapMemory;
import org.deegree.rendering.r3d.model.QualityModelPart;
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
public class SimpleAccessGeometry implements QualityModelPart {

    /**
     * 
     */
    private static final long serialVersionUID = -5069487647474073270L;

    private final static Logger LOG = LoggerFactory.getLogger( SimpleAccessGeometry.class );

    /**
     * The coordinates of this geometry may be null
     */
    protected transient float[] coordinates = null;

    // indizes of the startpositions of the innerrings
    transient int[] innerRings = null;

    transient int vertexCount;

    private transient SimpleGeometryStyle style;

    /**
     * @param coordinates
     * @param innerRings
     * @param style
     */
    public SimpleAccessGeometry( float[] coordinates, int[] innerRings, SimpleGeometryStyle style ) {
        this.style = style;
        this.coordinates = coordinates;
        this.innerRings = innerRings;
        vertexCount = ( coordinates == null ) ? 0 : ( coordinates.length / 3 );
    }

    /**
     * @param coordinates
     * @param style
     */
    public SimpleAccessGeometry( float[] coordinates, SimpleGeometryStyle style ) {
        this( coordinates, null, style );
    }

    /**
     * @param coordinates
     * @param innerRings
     *            containing indizes to the vertex, not the array offset.
     */
    public SimpleAccessGeometry( float[] coordinates, int[] innerRings ) {
        this( coordinates, innerRings, new SimpleGeometryStyle() );
    }

    /**
     * @param coordinates
     */
    public SimpleAccessGeometry( float[] coordinates ) {
        this( coordinates, null, new SimpleGeometryStyle() );
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
            throw new IndexOutOfBoundsException( "No such vertex: " + vertex + ", the given index is out of range" );
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
        vertexCount = in.readInt();
    }

    @Override
    public long sizeOf() {
        long localSize = style.sizeOf();
        localSize += AllocatedHeapMemory.sizeOfFloatArray( coordinates, true );
        localSize += AllocatedHeapMemory.sizeOfIntArray( innerRings, true );
        localSize += AllocatedHeapMemory.INT_SIZE;
        return localSize;
    }

    /**
     * @return the style information of this geometry
     */
    public SimpleGeometryStyle getStyle() {
        return style;
    }
}
