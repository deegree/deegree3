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

import org.deegree.coverage.raster.data.RasterData;

/**
 * This class implements a bilinear interpolation for byte raster.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public class BiLinearByteInterpolation implements Interpolation {
    private RasterData raster;

    private byte[] window = new byte[4];

    /**
     * Create a new bilinear interpolation for given byte {@link RasterData}.
     * 
     * @param rasterData
     */
    public BiLinearByteInterpolation( RasterData rasterData ) {
        raster = rasterData;
    }

    public final byte[] getPixel( float x, float y, byte[] result ) {
        try {
            float xfrac = Math.abs( x - (int) x );
            float yfrac = Math.abs( y - (int) y );
            for ( int b = 0; b < raster.getBands(); b++ ) {
                raster.getBytes( (int) x, (int) y, 2, 2, b, window );
                // (x & 0xFF) converts between signed bytes to unsigned values
                float h1 = ( window[0] & 0xFF ) + ( ( window[1] & 0xFF ) - ( window[0] & 0xFF ) ) * xfrac;
                float h2 = ( window[2] & 0xFF ) + ( ( window[3] & 0xFF ) - ( window[2] & 0xFF ) ) * xfrac;
                result[b] = (byte) ( ( (char) ( h1 + ( h2 - h1 ) * yfrac ) ) & 0xFF );
            }
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
