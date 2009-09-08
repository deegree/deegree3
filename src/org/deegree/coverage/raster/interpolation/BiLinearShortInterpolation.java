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
package org.deegree.coverage.raster.interpolation;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.DataType;

/**
 * This class implements a bilinear interpolation for short raster.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class BiLinearShortInterpolation implements Interpolation {
    private RasterData raster;

    private ByteBuffer tmp;

    private short[] window = new short[4];

    /**
     * Create a new bilinear interpolation for given short {@link RasterData}.
     *
     * @param rasterData
     */
    public BiLinearShortInterpolation( RasterData rasterData ) {
        if ( rasterData.getDataType() != DataType.SHORT ) {
            throw new IllegalArgumentException( this.getClass().getName() + " only supports short raster" );
        }
        raster = rasterData;
        tmp = ByteBuffer.allocate( DataType.SHORT.getSize() * raster.getBands() );

    }

    public final byte[] getPixel( float x, float y, byte[] result ) {
        try {
            tmp.position( 0 );
            float xfrac = Math.abs( x - (int) x ); // the fractional part
            float yfrac = Math.abs( y - (int) y );
            for ( int b = 0; b < raster.getBands(); b++ ) {
                raster.getShorts( (int) x, (int) y, 2, 2, b, window );
                float h1 = window[0] + ( window[1] - window[0] ) * xfrac;
                float h2 = window[2] + ( window[3] - window[2] ) * xfrac;
                tmp.putShort( (short) ( h1 + ( h2 - h1 ) * yfrac ) );
            }
            tmp.position( 0 );
            tmp.get( result );
        } catch ( IndexOutOfBoundsException ex ) {
            raster.getNullPixel( result );
        } catch ( IllegalArgumentException ex ) {
            raster.getNullPixel( result );
        } catch ( BufferUnderflowException ex ) {
            raster.getNullPixel( result );
        }
        return result;
    }
}
