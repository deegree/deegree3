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

import java.awt.Color;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.BandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to convert a multi-band raster to a matrix of float values, that can be used in lookup operations (for
 * example in Categorize or Interpolate). This class can read 1,3 and 4-band rasters, and combines band values into a
 * single pixel value.
 * 
 * @author <a href="mailto:a.aiordachioaie@jacobs-university.de">Andrei Aiordachioaie</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Raster2RawData {

    private final static Logger LOG = LoggerFactory.getLogger( Raster2RawData.class );

    private RasterData data;

    private int width = 0;

    private int height = 0;

    private int bands = 0;

    /**
     * 
     * @param raster
     *            abstract raster data source
     */
    public Raster2RawData( AbstractRaster raster ) {
        this( raster.getAsSimpleRaster().getRasterData() );
    }

    /**
     * 
     * @param data
     *            raster data source
     */
    public Raster2RawData( RasterData d ) {
        this.data = d;
        width = data.getWidth();
        height = data.getHeight();
        bands = data.getBands();
    }

    /**
     * 
     * @return the image pixels as a matrix of Floats
     */
    public float[][] parse() {

        float[][] values = null;
        int row = -1, col = -1;

        LOG.debug( "Parsing raster data. Raster has {} bands and datatype {}.", bands, data.getDataType() );
        BandType[] allb = data.getDataInfo().bandInfo;
        String b = allb[0].toString();
        for ( int i = 1; i < bands; i++ )
            b += ", " + allb[i].toString();
        b = "[" + b + "]";
        LOG.debug( "Raster bands are: {}", b );
        LOG.debug( "Raster has size {} x {} ", height, width );

        try {
            values = new float[height][width];

            switch ( data.getDataType() ) {
            case BYTE:
                byte[] bpixel = new byte[bands];
                for ( col = 0; col < width; col++ ) {
                    for ( row = 0; row < height; row++ ) {
                        Float f = combineBytes( data.getBytePixel( col, row, bpixel ) );
                        // Compensate for the fact that byte is a signed datatype. This is used in grayscale images, range 0-255
                        values[row][col] = ( ( f >= 0 ) ? ( f ) : ( f + 256 ) );
                    }
                }
                break;
            case SHORT:
            case USHORT:
                short[] spixel = new short[bands];
                for ( col = 0; col < width; col++ ) {
                    for ( row = 0; row < height; row++ ) {
                        values[row][col] = combineShorts( data.getShortPixel( col, row, spixel ) );
                    }
                }
                break;
            case INT:
                int[] ipixel = new int[bands];
                for ( col = 0; col < width; col++ ) {
                    for ( row = 0; row < height; row++ ) {
                        values[row][col] = combineInts( data.getIntPixel( col, row, ipixel ) );
                    }
                }
                break;
            case FLOAT:

                float[] fpixel = new float[bands];
                for ( col = 0; col < width; col++ ) {
                    for ( row = 0; row < height; row++ ) {
                        values[row][col] = combineFloats( data.getFloatPixel( col, row, fpixel ) );
                    }
                }
            default:
                throw new IllegalArgumentException( "Cannot parse datatype " + data.getDataType().toString() );
            }
        } catch ( NullPointerException e ) {
            LOG.error( "Error while parsing raster data, @ row={}, col={}", row, col );
            // e.printStackTrace();
        }

        return values;
    }

    private float combineFloats( float[] pixel ) {
        try {
            switch ( bands ) {
            case 1: /* Gray-scale */
                return (float) pixel[0];
            case 3: /* RGB bands */
                Color c = new Color( pixel[0], pixel[1], pixel[2] );
                return (float) c.getRGB();
            default:
                return 0;
            }
        } catch ( Exception e ) {
            return 0;
        }
    }

    private float combineShorts( short[] pixel ) {
        try {
            switch ( bands ) {
            case 1: /* Gray-scale */
                return (float) pixel[0] ;
            case 3: /* RGB bands */
                Color c = new Color( pixel[0], pixel[1], pixel[2] );
                return (float) c.getRGB();
            default:
                return 0;
            }
        } catch ( Exception e ) {
            return 0;
        }
    }

    private float combineInts( int[] pixel ) {
        try {
            switch ( bands ) {
            case 1: /* Gray-scale */
                return (float) pixel[0];
            case 3: /* RGB bands */
                Color c = new Color( pixel[0], pixel[1], pixel[2] );
                return (float) c.getRGB() ;
            default:
                return 0;
            }
        } catch ( Exception e ) {
            return 0;
        }
    }

    private float combineBytes( byte[] pixel ) {
        try {
            switch ( bands ) {
            case 1: /* Gray-scale */
                return (float) pixel[0];
            case 3: /* RGB bands */
                Color c = new Color( pixel[0], pixel[1], pixel[2] );
                return (float) c.getRGB();
            default:
                return 0;
            }
        } catch ( Exception e ) {
            return  0;
        }
    }

    /**
     * @param col
     *            column index
     * @param row
     *            row index
     * @return the appropriate value, as an Integer, or a Float object
     */
    public float get( int col, int row ) {
        float ret = (float) 0.0;

        switch ( data.getDataType() ) {
        case BYTE:
            ret = combineBytes( data.getBytePixel( col, row, null ) );
            // Compensate for the fact that byte is a signed datatype. This is used in grayscale images, range 0-255
            if ( ret < 0 )
                ret += -2 * Byte.MIN_VALUE;
            break;
        case SHORT:
        case USHORT:
            ret = combineShorts( data.getShortPixel( col, row, null ) );
            break;
        case INT:
            ret = combineInts( data.getIntPixel( col, row, null ) );
        case FLOAT:
            ret = combineFloats( data.getFloatPixel( col, row, null ) );
            break;
        default:
            LOG.error( "Cannot parse datatype '{}'", data.getDataType().toString() );
            throw new UnsupportedOperationException( "Cannot parse datatype: " + data.getDataType() );
        }

        return ret;

    }

}
