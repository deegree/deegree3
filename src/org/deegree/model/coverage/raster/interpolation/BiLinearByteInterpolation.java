//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

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
package org.deegree.model.coverage.raster.interpolation;

import java.nio.BufferUnderflowException;

import org.deegree.model.coverage.raster.data.RasterData;

/**
 * This class implements a bilinear interpolation for byte raster.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: $
 *
 * @version $Revision: $, $Date: $
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

