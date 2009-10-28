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

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.rendering.r2d.RasterRenderingException;
import org.deegree.rendering.r2d.styling.RasterStyling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to interpret raster data in a consistent way. It should be used in raster lookup operations (for
 * example in Categorize or Interpolate). This class can read 1, 2, 3 and 4-band rasters, and combines band values into
 * a single pixel value. RGB bands are combined and their average value is returned (yielding the overall gray-pixel
 * intensity of a pixel), and alpha bands are ignored. 
 * 
 * If channel mappings are provided, this class can process rasters with any number
 * of bands.
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

    /* Index of alpha band, if exists */
    private int alphaIndex = -1;

    /* Channel mappings */
    private boolean channelMappings = false;
    /* Indexes of channels, if applicable */
    private int gray = -1, red = -1, green = -1, blue = -1;

    /**
     * 
     * @param raster
     *            abstract raster data source
     */
    public Raster2RawData( AbstractRaster raster ) {
        this( raster.getAsSimpleRaster().getRasterData() );
    }

    /**
     * Create a Raster2RawData object, where channels are mapped according to the RasterStyle options
     * 
     * @param raster
     * @param style
     */
    public Raster2RawData( AbstractRaster raster, RasterStyling style ) {
        this( raster.getAsSimpleRaster().getRasterData() );

        BandType[] bands = raster.getRasterDataInfo().getBandInfo();
        channelMappings = true;
        if ( style.grayChannel != null )
            gray = findChannelIndex( style.grayChannel, bands );
        if ( style.redChannel != null )
            red = findChannelIndex( style.redChannel, bands );
        if ( style.greenChannel != null )
            green = findChannelIndex( style.greenChannel, bands );
        if ( style.blueChannel != null )
            blue = findChannelIndex( style.blueChannel, bands );
        if ( gray == -1 && red == -1 && green == -1 && blue == -1 )
            channelMappings = false;
    }

    /**
     * Search the index of a channel in the list of bands.
     * 
     * @param cName
     *            Channel name or index, as string
     * @param bands
     *            array of band information for the current raster
     * @return index of the channel
     * @throws RasterRenderingException
     *             if the channel is not found
     */
    private int findChannelIndex( String cName, BandType[] bands )
                            throws RasterRenderingException {
        int i = -1;
        try {
            i = Integer.parseInt( cName ) - 1;
            if ( i < 0 || i >= bands.length ) {
                LOG.error( "Cannot evaluate band '{}', raster data has only {} bands", i, bands.length );
                throw new RasterRenderingException( "Cannot evaluate band " + i + ", raster data has only "
                                                    + bands.length + " bands. " );
            }
            return i;
        } catch ( NumberFormatException e ) {
            for ( i = 0; i < bands.length; i++ )
                if ( bands[i].name().equals( cName ) )
                    return i;
        }

        LOG.error( "Could not evaluate band with name '{}'", cName );
        throw new RasterRenderingException( "Could not evaluate band with name '" + cName + "'" );
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
        BandType[] blist = d.getDataInfo().getBandInfo();
        for ( int i = 0; i < blist.length; i++ )
            if ( blist[i] == BandType.ALPHA )
                alphaIndex = i;
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
                        // Compensate for the fact that byte is a signed datatype. This is used in grayscale images,
                        // range 0-255
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
            if ( channelMappings == true ) {
                if ( gray != -1 )
                    return pixel[gray];
                else
                    return ( pixel[red] + pixel[green] + pixel[blue] ) / 3;
            } else {
                switch ( bands ) {
                case 1: /* Gray-scale */
                    return (float) pixel[0];
                case 2: /* Gray-scale + alpha: ignore alpha */
                    return (float) pixel[bands - alphaIndex - 1];
                case 3: /* RGB bands: use gray-pixel intensity */
                    return (float) ( pixel[0] + pixel[1] + pixel[2] ) / 3;
                case 4: /* RGBA bands: use gray-pixel intensity, ignore alpha */
                    return (float) ( pixel[0] + pixel[1] + pixel[2] + pixel[3] - pixel[alphaIndex] ) / 3;
                default:
                    return 0;
                }
            }
        } catch ( Exception e ) {
            return 0;
        }
    }

    private float combineShorts( short[] pixel ) {
        try {
            if ( channelMappings == true ) {
                if ( gray != -1 )
                    return pixel[gray];
                else
                    return ( pixel[red] + pixel[green] + pixel[blue] ) / 3;
            } else {
                switch ( bands ) {
                case 1: /* Gray-scale */
                    return (float) pixel[0];
                case 2: /* Gray-scale + alpha: ignore alpha */
                    return (float) pixel[bands - alphaIndex - 1];
                case 3: /* RGB bands: use gray-pixel intensity */
                    return (float) ( pixel[0] + pixel[1] + pixel[2] ) / 3;
                case 4: /* RGBA bands: use gray-pixel intensity, ignore alpha */
                    return (float) ( pixel[0] + pixel[1] + pixel[2] + pixel[3] - pixel[alphaIndex] ) / 3;
                default:
                    return 0;
                }
            }
        } catch ( Exception e ) {
            return 0;
        }
    }

    private float combineInts( int[] pixel ) {
        try {
            if ( channelMappings == true ) {
                if ( gray != -1 )
                    return pixel[gray];
                else
                    return ( pixel[red] + pixel[green] + pixel[blue] ) / 3;
            } else {
                switch ( bands ) {
                case 1: /* Gray-scale */
                    return (float) pixel[0];
                case 2: /* Gray-scale + alpha: ignore alpha */
                    return (float) pixel[bands - alphaIndex - 1];
                case 3: /* RGB bands: use gray-pixel intensity */
                    return (float) ( pixel[0] + pixel[1] + pixel[2] ) / 3;
                case 4: /* RGBA bands: use gray-pixel intensity, ignore alpha */
                    return (float) ( pixel[0] + pixel[1] + pixel[2] + pixel[3] - pixel[alphaIndex] ) / 3;
                default:
                    return 0;
                }
            }
        } catch ( Exception e ) {
            return 0;
        }
    }

    private float combineBytes( byte[] pixel ) {
        try {
            if ( channelMappings == true ) {
                if ( gray != -1 )
                    return pixel[gray];
                else
                    return ( pixel[red] + pixel[green] + pixel[blue] ) / 3;
            } else {
                switch ( bands ) {
                case 1: /* Gray-scale */
                    return (float) pixel[0];
                case 2: /* Gray-scale + alpha: ignore alpha */
                    return (float) pixel[bands - alphaIndex - 1];
                case 3: /* RGB bands: use gray-pixel intensity */
                    return (float) ( pixel[0] + pixel[1] + pixel[2] ) / 3;
                case 4: /* RGBA bands: use gray-pixel intensity, ignore alpha */
                    return (float) ( pixel[0] + pixel[1] + pixel[2] + pixel[3] - pixel[alphaIndex] ) / 3;
                default:
                    return 0;
                }
            }
        } catch ( Exception e ) {
            return 0;
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
            // Compensate for byte being a signed datatype. This is commonly used in grayscale images, range 0-255
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

    public int getGrayChannelIndex() {
        return gray;
    }

    public int getRedChannelIndex() {
        return red;
    }

    public int getGreenChannelIndex() {
        return green;
    }

    public int getBlueChannelIndex() {
        return blue;
    }

}
