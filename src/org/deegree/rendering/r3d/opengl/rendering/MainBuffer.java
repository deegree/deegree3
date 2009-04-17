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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.FloatBuffer;

import sun.misc.VM;

import com.sun.opengl.util.BufferUtil;

/**
 * The <code>MainBuffer</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class MainBuffer {

    private static FloatBuffer coordBuffer;

    private static FloatBuffer normalBuffer;

    private static FloatBuffer textureBuffer;

    /**
     * @param coordinateCapacity
     * @param textureCapacity
     * @param colorCapacity
     */
    public static synchronized void initialize( int coordinateCapacity, int textureCapacity ) {
        if ( coordBuffer == null ) {
            // System.out.println( "Trying to allocate: " + "\n- coordinate|normals: "
            // + ( coordinateCapacity / ( (double) 1024 * 1024 * AllocatedHeapMemory.FLOAT_SIZE ) * 2 )
            // + "mb of direct memory." );
            System.out.println( "coordinateCap: " + coordinateCapacity );
            System.out.println( "textureCap: " + textureCapacity );
            System.out.println( "VM.maxDirectMemory(): " + VM.maxDirectMemory() );
            coordBuffer = BufferUtil.newFloatBuffer( coordinateCapacity );
            normalBuffer = BufferUtil.newFloatBuffer( coordinateCapacity );
            textureBuffer = BufferUtil.newFloatBuffer( textureCapacity );
        }

    }

    public static FloatBuffer getCoords( int position, int limit ) {
        if ( position < 0 || position > coordBuffer.capacity() ) {
            return null;
        }
        FloatBuffer fb = coordBuffer.asReadOnlyBuffer();
        fb.position( position );
        fb.limit( position + limit );
        return fb.slice();
    }

    public static FloatBuffer getNormals( int position, int limit ) {
        if ( position < 0 || position > normalBuffer.capacity() ) {
            return null;
        }
        FloatBuffer fb = normalBuffer.asReadOnlyBuffer();
        fb.position( position );
        fb.limit( position + limit );
        return fb.slice();
    }

    public static FloatBuffer getTextureCoordinates( int position, int limit ) {
        if ( position < 0 || position > textureBuffer.capacity() ) {
            return null;
        }
        FloatBuffer fb = textureBuffer.asReadOnlyBuffer();
        fb.position( position );
        fb.limit( position + limit );
        return fb.slice();
    }

    // public static synchronized void addGeometry( RenderableGeometry geom ) {
    // int coordPosition = coordBuffer.position();
    // int normPosition = -1;
    //
    // coordBuffer.put( geom.getCoordBuffer() );
    // if ( geom.getNormalBuffer() != null ) {
    // normPosition = normalBuffer.position();
    // normalBuffer.put( geom.getNormalBuffer() );
    // }
    // geom.setCoordPosition( coordPosition );
    // geom.setNormPosition( normPosition );
    // }

    public static synchronized int addCoordinates( FloatBuffer buffer ) {
        int coordPosition = coordBuffer.position();
        coordBuffer.put( buffer );
        return coordPosition;
    }

    public static synchronized int addNormals( FloatBuffer normals ) {
        int normalPosition = normalBuffer.position();
        normalBuffer.put( normals );
        return normalPosition;
    }

    public static synchronized int readCoordsFromStream( ObjectInputStream in )
                            throws IOException {
        int numberOfValues = in.readInt();
        int position = -1;
        if ( numberOfValues != -1 ) {
            position = coordBuffer.position();
            for ( int i = 0; i < numberOfValues; ++i ) {
                coordBuffer.put( in.readFloat() );
            }

        }
        return position;
    }

    public static synchronized int readNormalsFromStream( ObjectInputStream in )
                            throws IOException {
        int numberOfValues = in.readInt();
        int position = -1;
        if ( numberOfValues != -1 ) {
            position = normalBuffer.position();
            for ( int i = 0; i < numberOfValues; ++i ) {
                normalBuffer.put( in.readFloat() );
            }
        }
        return position;
    }

    public static synchronized int readTexCoordsFromStream( ObjectInputStream in )
                            throws IOException {
        int numberOfValues = in.readInt();
        int position = -1;
        if ( numberOfValues != -1 ) {
            position = textureBuffer.position();
            for ( int i = 0; i < numberOfValues; ++i ) {
                textureBuffer.put( in.readFloat() );
            }

        }
        return position;
    }

}
