//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/wpvs/model/data3d/geometry/TexturedGeometry.java $
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

import org.deegree.commons.utils.memory.AllocatedHeapMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>RenderableTexturedGeometry</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author: rbezema $
 *
 * @version $Revision: 15598 $, $Date: 2009-01-12 15:03:49 +0100 (Mo, 12 Jan 2009) $
 *
 */
public class TexturedGeometry extends SimpleAccessGeometry {
    /**
     *
     */
    private static final long serialVersionUID = -8283523043454581251L;

    private final static Logger LOG = LoggerFactory.getLogger( TexturedGeometry.class );

    private transient String texture;

    // 2D
    private transient float[] textureCoordinates;

    /**
     * @param geometry
     * @param style
     * @param texture
     *            to use
     * @param textureCoordinates
     *            of this data
     */
    public TexturedGeometry( float[] geometry, SimpleGeometryStyle style, String texture, float[] textureCoordinates ) {
        super( geometry, style );
        this.texture = texture;
        this.textureCoordinates = textureCoordinates;
    }

    /**
     * @param geometry
     * @param texture
     * @param textureCoordinates
     */
    public TexturedGeometry( float[] geometry, String texture, float[] textureCoordinates ) {
        super( geometry );
        this.texture = texture;
        this.textureCoordinates = textureCoordinates;
    }

    /**
     * @param geometry
     * @param innerRings
     * @param texture
     * @param textureCoordinates
     */
    public TexturedGeometry( float[] geometry, int[] innerRings, String texture, float[] textureCoordinates ) {
        super( geometry, innerRings );
        this.texture = texture;
        this.textureCoordinates = textureCoordinates;
    }

    /**
     * @return the texture
     */
    public final String getTexture() {
        return texture;
    }

    /**
     * @param texture
     *            the texture to set
     */
    public final void setTexture( String texture ) {
        this.texture = texture;
    }

    /**
     * @return the textureCoordinates
     */
    public final float[] getTextureCoordinates() {
        return textureCoordinates;
    }

    /**
     * @param textureCoordinates
     *            the textureCoordinates to set
     */
    public final void setTextureCoordinates( float[] textureCoordinates ) {
        this.textureCoordinates = textureCoordinates;
    }

    /**
     * @param location
     *            of the x ordinate of the requested texture coordinate.
     * @return a copy of the coordinate at the given location (location, location +1)
     * @throws IndexOutOfBoundsException
     *             is outside the coordinate array
     */
    public float[] getTextureCoordinate( int location ) {
        if ( location < 0 || location + 1 > textureCoordinates.length ) {
            throw new IndexOutOfBoundsException( "Location is out of the range" );
        }
        return new float[] { textureCoordinates[location], textureCoordinates[location + 1] };
    }

    /**
     * @param vertex
     *            the vertex index(starting at 0), e.g. if you would like to get the texture coordinates for the second
     *            vertex the vertex index would be 1
     * @return a copy of the texture coordinates for the given vertex.
     * @throws IndexOutOfBoundsException
     *             is outside the coordinate array
     */
    public float[] getTextureCoordinateForVertex( int vertex ) {
        if ( vertex < 0 || ( vertex * 2 ) + 1 > textureCoordinates.length ) {
            throw new IndexOutOfBoundsException( "No such vertex, the given index is out of range" );
        }
        return new float[] { textureCoordinates[vertex * 2], textureCoordinates[( vertex * 2 ) + 1] };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( super.toString() );
        sb.append( "\n textureID: " ).append( texture );
        if ( textureCoordinates != null && textureCoordinates.length > 0 ) {
            sb.append( "\n textureCoordinates:\n" );
            for ( int i = 0; ( i + 1 ) < textureCoordinates.length; i += 2 ) {
                sb.append( textureCoordinates[i] ).append( "," ).append( textureCoordinates[i + 1] ).append( "\n" );
            }
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
        out.writeUTF( texture );
        out.writeObject( textureCoordinates );
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
        texture = in.readUTF();
        textureCoordinates = (float[]) in.readObject();
    }

    /**
     * @return the bytes this geometry occupies
     */
    @Override
    public long sizeOf() {
        long localSize = super.sizeOf();
        // id
        localSize += AllocatedHeapMemory.sizeOfString( texture, true, true );
        // texture coordinates
        localSize += AllocatedHeapMemory.sizeOfFloatArray( textureCoordinates, true );
        return localSize;
    }

}
