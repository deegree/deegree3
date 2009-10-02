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
package org.deegree.rendering.r2d.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.feature.GenericProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses 1, 3 or 4 band rasters assuming pixels are integers, and returns a long matrix containing the pixel values.
 * 
 * @author <a href="mailto:a.aiordachioaie@jacobs-university.de">Andrei Aiordachioaie</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Raster2RawData implements Image2RawData {

    private final static Logger LOG = LoggerFactory.getLogger( Raster2RawData.class );

    private RasterData data;

    private DataBuffer db;

    private float scale = 0;

    private float offset = 0;

    private int width = 0;

    private int height = 0;

    private int bands = 0;

    /**
     * 
     * @param data
     *            image containing raw data instead color information
     */
    public Raster2RawData( AbstractRaster raster ) {
        this( raster.getAsSimpleRaster().getRasterData(), 1, 0 );
    }

    /**
     * 
     * @param data
     *            image containing raw data instead color information
     * @param scale
     *            scale factor; newHeight[i][j] = height[i][j] * scale
     */
    public Raster2RawData( RasterData data, float scale ) {
        this( data, scale, 0 );
    }

    /**
     * 
     * @param data
     *            raster containing raw data
     * @param scale
     *            scale factor; newHeight[i][j] = height[i][j] * scale
     * @param offset
     *            height offset; newHeight[i][j] = height[i][j] + offset
     */
    public Raster2RawData( RasterData d, float scale, float offset ) {
        this.data = d;
        this.scale = scale;
        this.offset = offset;
        width = data.getWidth();
        height = data.getHeight();
        bands = data.getBands();
    }

    /**
     * returns the image pixels as a matrix of Integers or Floats
     * 
     * @return the image pixels as a matrix of Integers or Floats
     */
    @Override
    public Float[][] parse() {

        Float[][] values = null;
        int row = -1, col = -1;

        LOG.debug( "Parsing raster data. Raster has {} bands and datatype {}.", bands, data.getDataType() );
        LOG.debug( "Raster has size {} x {} ", data.getHeight(), data.getWidth() );

        try {
            switch ( data.getDataType() ) {
            case SHORT:
            case USHORT:
            case BYTE:
                values = new Float[height][width];
                short[] spixel = new short[data.getBands()];
                for ( col = 0; col < width; col++ ) {
                    for ( row = 0; row < height; row++ ) {
                        values[row][col] = combineShorts( data.getShortPixel( col, row, spixel ) ).floatValue();
                    }
                }
                break;
            case FLOAT:
                values = new Float[height][width];
                float[] fpixel = new float[data.getBands()];
                for ( col = 0; col < width; col++ ) {
                    for ( row = 0; row < height; row++ ) {
                        values[row][col] = combineFloats( data.getFloatPixel( col, row, fpixel ) );
                    }
                }
            default:
                break;
            }
        } catch ( Exception e ) {
            LOG.error( "Error while parsing raster data, @ row={}, col={}", row, col );
            e.printStackTrace();
        }

        return values;
    }

    private Float combineFloats( float[] pixel ) {
        switch ( bands ) {
        case 1: /* Gray-scale */
            return new Float( pixel[0] );
        case 3: /* RGB bands */
            return new Float( ( pixel[0] + pixel[1] + pixel[2] ) / 3 );
        default:
            return null;
        }
    }

    private Integer combineShorts( short[] pixel ) {
        switch ( bands ) {
        case 1: /* Gray-scale */
            return new Integer( pixel[0] );
        case 3: /* RGB bands */
            return new Integer( ( pixel[0] << 16 ) + ( pixel[1] << 8 ) + ( pixel[0] ) );
        default:
            return null;
        }
    }

    /**
     * @param x
     *            index
     * @param y
     *            index
     * @return the appropriate value, as an Integer, or a Float object
     */
    public Float get( int x, int y ) {
        Float ret = null;
        switch ( data.getDataType() ) {
        case SHORT:
        case USHORT:
        case BYTE:
            ret = combineShorts( data.getShortPixel( x, y, null ) ).floatValue();
            break;
        case FLOAT:
            ret = combineFloats( data.getFloatPixel( x, y, null ) );
        default:
            break;
        }

        return ret;

    }

}
