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

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;

/**
 * The <code>FloatColorModel</code> maps java float numbers to integer values. The returned integer values are converted
 * with the {@link ByteBuffer} conversions methods.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class FloatColorModel extends ColorModel {

    private int noData;

    // will hold temporary result
    byte[] buf = new byte[4];

    private ByteBuffer converter;

    /**
     * Constructs a float colormodel with the given no data value.
     * 
     * @param noData
     */
    public FloatColorModel( byte[] noData ) {
        super( 32, new int[] { 8, 8, 8, 8 }, ColorSpace.getInstance( ColorSpace.CS_sRGB ), true, false,
               Transparency.TRANSLUCENT, DataBuffer.TYPE_FLOAT );

        // get the int representation of the given data object.
        this.noData = ByteBuffer.wrap( noData ).getInt();
        converter = ByteBuffer.wrap( buf );
    }

    @Override
    public int getAlpha( int pixel ) {
        throw new UnsupportedOperationException( "Getting alpha for float values is not supported." );
    }

    @Override
    public int getRed( int pixel ) {
        throw new UnsupportedOperationException( "Getting red part for float values is not supported." );
    }

    @Override
    public int getGreen( int pixel ) {
        throw new UnsupportedOperationException( "Getting green part for float values is not supported." );
    }

    @Override
    public int getBlue( int pixel ) {
        throw new UnsupportedOperationException( "Getting blue part for float values is not supported." );
    }

    @Override
    public int getRGB( int pixel ) {
        return pixel;
    }

    @Override
    public int getAlpha( Object inData ) {
        return getAlpha( getRGB( inData ) );
    }

    @Override
    public int getRed( Object inData ) {
        return getRed( getRGB( inData ) );
    }

    @Override
    public int getGreen( Object inData ) {
        return getGreen( getRGB( inData ) );
    }

    @Override
    public int getBlue( Object inData ) {
        return getBlue( getRGB( inData ) );
    }

    @Override
    public int getRGB( Object inData ) {
        float val = ( (float[]) inData )[0];
        if ( val == noData ) {
            return noData;
        }
        converter.putFloat( 0, val );
        return converter.getInt( 0 );
    }

    @Override
    public ColorModel coerceData( WritableRaster raster, boolean isAlphaPremultiplied ) {
        if ( !isAlphaPremultiplied ) {
            return this;
        }
        return super.coerceData( raster, isAlphaPremultiplied );
    }

    @Override
    public boolean isCompatibleRaster( Raster raster ) {
        return isCompatibleSampleModel( raster.getSampleModel() );
    }

    @Override
    public boolean isCompatibleSampleModel( SampleModel sm ) {
        return sm.getDataType() == DataBuffer.TYPE_FLOAT;
    }

}
