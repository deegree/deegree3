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

package org.deegree.coverage.raster.utils;

import java.awt.image.DataBuffer;
import java.nio.ByteBuffer;

import org.deegree.coverage.raster.data.DataView;
import org.deegree.coverage.raster.geom.RasterRect;

/**
 * The <code>RawDataBufferFloat</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class RawDataBufferFloat extends DataBuffer {

    private ByteBuffer floatBuffer;

    private int noData;

    private final int SIZE = Float.SIZE / 8;

    private RasterRect bufferDomain;

    private DataView view;

    private RasterRect maxViewData;

    private int toNullPoint;

    private int lineStride;

    /**
     * @param floatBuffer
     */
    public RawDataBufferFloat( ByteBuffer floatBuffer, float noData, RasterRect bufferDomain, DataView view ) {
        super( DataBuffer.TYPE_FLOAT, 1 );
        this.floatBuffer = floatBuffer;
        this.noData = Float.floatToIntBits( noData );
        this.bufferDomain = bufferDomain;
        this.view = view;
        this.maxViewData = RasterRect.intersection( bufferDomain, view );
        toNullPoint = ( ( bufferDomain.width * maxViewData.y ) + maxViewData.x );
        lineStride = bufferDomain.width;
    }

    private int calculatePosition( int index ) {
        int yPos = index / maxViewData.width;
        int xPos = index - ( maxViewData.width * yPos );
        return ( toNullPoint + ( ( yPos * lineStride ) + xPos ) ) * SIZE;
    }

    @Override
    public int getElem( int i ) {
        int index = calculatePosition( i );
        if ( index >= floatBuffer.capacity() || index >= floatBuffer.limit() ) {
            return noData;
        }
        // float v = floatBuffer.getFloat( index );
        int val = floatBuffer.getInt( index );
        // System.out.println( "Getting: " + v + " as int: " + val );
        return val;
    }

    @Override
    public float getElemFloat( int i ) {
        int index = calculatePosition( i );
        if ( index >= floatBuffer.capacity() || index >= floatBuffer.limit() ) {
            return noData;
        }
        // float v = floatBuffer.getFloat( index );
        float val = floatBuffer.getFloat( index );
        // System.out.println( "Getting elem float: " + val );
        return val;

    }

    @Override
    public float getElemFloat( int bank, int i ) {
        if ( bank > 1 ) {
            throw new IndexOutOfBoundsException( "Only one bank (buffer array) is supported." );
        }
        return getElemFloat( i );
    }

    @Override
    public void setElem( int i, int val ) {
        int index = calculatePosition( i );
        if ( index < floatBuffer.capacity() && index < floatBuffer.limit() ) {
            System.out.println( "SETTING?" );
            floatBuffer.putInt( index, val );
        }
        throw new IndexOutOfBoundsException( "The given index is outside the bank." );

    }

    @Override
    public int getElem( int bank, int i ) {
        if ( bank > 1 ) {
            throw new IndexOutOfBoundsException( "Only one bank (buffer array) is supported." );
        }
        return getElem( i );

    }

    @Override
    public void setElem( int bank, int i, int val ) {
        if ( bank > 1 ) {
            throw new IndexOutOfBoundsException( "Only one bank (buffer array) is supported." );
        }
        setElem( i, val );
    }

}
