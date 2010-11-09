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
import org.deegree.rendering.r2d.styling.RasterChannelSelection;
import org.deegree.rendering.r2d.styling.RasterChannelSelection.ChannelSelectionMode;
import org.deegree.rendering.r2d.styling.RasterStyling.ContrastEnhancement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to interpret raster data in a consistent way. It should be used in raster lookup operations (for
 * example in Categorize or Interpolate). This class can read 1, 2, 3 and 4-band rasters, and combines band values into
 * a single pixel value. RGB bands are combined and their average value is returned (yielding the overall gray-pixel
 * intensity of a pixel), and alpha bands are ignored.
 * 
 * This class can perform contrast enhancement on-the-fly.
 * 
 * If channel mappings are provided, this class can process rasters with any number of bands.
 * 
 * @author <a href="mailto:a.aiordachioaie@jacobs-university.de">Andrei Aiordachioaie</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RasterDataUtility {

    private final static Logger LOG = LoggerFactory.getLogger( RasterDataUtility.class );

    private RasterData data;

    private int width = 0;

    private int height = 0;

    private int bands = 0;

    /* Index of alpha band, if exists */
    private int alphaIndex = -1;

    /* Channel mappings */
    private boolean channelMappings = false;

    /* Overall Contrast Enhancement */
    private ContrastEnhancement contrast = new ContrastEnhancement();

    /* Contrast Enhancement: Gamma */
    private double gamma;

    private int[] gammaTable;

    /*
     * Contrast Enhancement: Normalize. lower and upper bounds of raster data in scope, and lower and upper bounds of
     * datatype.
     */
    private int rastermin, rastermax, datamin, datamax;

    private int[] normalizeTable = new int[256];

    /*
     * Contrast Enhancement: Histogram normalization.
     */
    private int[] cdf = new int[256];

    private int[] histogramTable = new int[256];

    private int[] indexes;

    /**
     * 
     * @param raster
     *            abstract raster data source
     */
    public RasterDataUtility( AbstractRaster raster ) {
        this( raster.getAsSimpleRaster().getRasterData() );
    }

    /**
     * Create a RasterDataUtility object, where channels are mapped according to the RasterStyle options
     * 
     * @param raster
     * @param style
     */
    public RasterDataUtility( AbstractRaster raster, RasterChannelSelection style ) {
        this( raster.getAsSimpleRaster().getRasterData() );
        if ( style != null ) {
            style.evaluate( raster.getRasterDataInfo().bandInfo );
            if ( style.getMode() == ChannelSelectionMode.RGB || style.getMode() == ChannelSelectionMode.GRAY ) {
                channelMappings = true;
                indexes = style.evaluate( raster.getRasterDataInfo().bandInfo );
            }
        }
    }

    /**
     * 
     * @param d
     *            raster data
     */
    public RasterDataUtility( RasterData d ) {
        this.data = d;
        width = data.getColumns();
        height = data.getRows();
        bands = data.getBands();
        BandType[] blist = d.getDataInfo().getBandInfo();
        for ( int i = 0; i < blist.length; i++ )
            if ( blist[i] == BandType.ALPHA )
                alphaIndex = i;
        setGamma( 1.0 );
    }

    /**
     * Update the contrast enhancement. Method getEnhanced(row, col, band) will apply this contrast enhancement before
     * returning values.
     * 
     * @param ce
     */
    public void setContrastEnhancement( ContrastEnhancement ce ) {
        contrast = ce;
        if ( contrast != null )
            setGamma( contrast.gamma );
        else
            setGamma( 1.0 );
    }

    private float combineFloats( float[] pixel ) {
        try {
            if ( channelMappings == true ) {
                if ( indexes[3] != -1 ) {
                    return pixel[indexes[3]];
                }

                return ( pixel[indexes[0]] + pixel[indexes[1]] + pixel[indexes[2]] ) / 3;
            }
            switch ( bands ) {
            case 1: /* Gray-scale */
                return pixel[0];
            case 2: /* Gray-scale + alpha: ignore alpha */
                return pixel[bands - alphaIndex - 1];
            case 3: /* RGB bands: use gray-pixel intensity */
                return ( pixel[0] + pixel[1] + pixel[2] ) / 3;
            case 4: /* RGBA bands: use gray-pixel intensity, ignore alpha */
                return ( pixel[0] + pixel[1] + pixel[2] + pixel[3] - pixel[alphaIndex] ) / 3;
            default:
                return 0;
            }
        } catch ( Exception e ) {
            return 0;
        }
    }

    private float combineShorts( short[] pixel ) {
        try {
            if ( channelMappings == true ) {
                if ( indexes[3] != -1 ) {
                    return pixel[indexes[3]];
                }

                return ( pixel[indexes[0]] + pixel[indexes[1]] + pixel[indexes[2]] ) / 3;
            }
            switch ( bands ) {
            case 1: /* Gray-scale */
                return 0xffff & pixel[0];
            case 2: /* Gray-scale + alpha: ignore alpha */
                return pixel[bands - alphaIndex - 1];
            case 3: /* RGB bands: use gray-pixel intensity */
                return (float) ( pixel[0] + pixel[1] + pixel[2] ) / 3;
            case 4: /* RGBA bands: use gray-pixel intensity, ignore alpha */
                return (float) ( pixel[0] + pixel[1] + pixel[2] + pixel[3] - pixel[alphaIndex] ) / 3;
            default:
                return 0;
            }
        } catch ( Exception e ) {
            return 0;
        }
    }

    private float combineInts( int[] pixel ) {
        try {
            if ( channelMappings == true ) {
                if ( indexes[3] != -1 ) {
                    return pixel[indexes[3]];
                }

                return ( pixel[indexes[0]] + pixel[indexes[1]] + pixel[indexes[2]] ) / 3f;
            }
            switch ( bands ) {
            case 1: /* Gray-scale */
                return pixel[0];
            case 2: /* Gray-scale + alpha: ignore alpha */
                return pixel[bands - alphaIndex - 1];
            case 3: /* RGB bands: use gray-pixel intensity */
                return (float) ( pixel[0] + pixel[1] + pixel[2] ) / 3;
            case 4: /* RGBA bands: use gray-pixel intensity, ignore alpha */
                return (float) ( pixel[0] + pixel[1] + pixel[2] + pixel[3] - pixel[alphaIndex] ) / 3;
            default:
                return 0;
            }
        } catch ( Exception e ) {
            return 0;
        }
    }

    private float combineBytes( byte[] pixel ) {
        try {
            if ( channelMappings == true ) {
                if ( indexes[3] != -1 )
                    return pixel[indexes[3]];
                return ( pixel[indexes[0]] + pixel[indexes[1]] + pixel[indexes[2]] ) / 3;
            }
            switch ( bands ) {
            case 1: /* Gray-scale */
                return pixel[0];
            case 2: /* Gray-scale + alpha: ignore alpha */
                return pixel[bands - alphaIndex - 1];
            case 3: /* RGB bands: use gray-pixel intensity */
                return (float) ( pixel[0] + pixel[1] + pixel[2] ) / 3;
            case 4: /* RGBA bands: use as float (d2 rtb does this often) */
                return Float.intBitsToFloat( ( ( 0xff & pixel[3] ) << 24 ) + ( ( 0xff & pixel[0] ) << 16 )
                                             + ( ( 0xff & pixel[1] ) << 8 ) + ( 0xff & pixel[2] ) );
            default:
                return 0;
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
     * @return the appropriate value, as a float value
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
            break;
        case FLOAT:
            ret = combineFloats( data.getFloatPixel( col, row, null ) );
            break;
        default:
            LOG.error( "Cannot parse datatype '{}'", data.getDataType().toString() );
            throw new UnsupportedOperationException( "Cannot parse datatype: " + data.getDataType() );
        }

        return ret;

    }

    /**
     * @param col
     *            column index
     * @param row
     *            row index
     * @param band
     *            band index
     * @return the appropriate value, as a float value
     */
    public float get( int col, int row, int band ) {
        float ret = (float) 0.0;

        switch ( data.getDataType() ) {
        case BYTE:
            ret = data.getByteSample( col, row, band );
            // Compensate for byte being a signed datatype. This is commonly used in grayscale images, range 0-255
            if ( ret < 0 )
                ret += -2 * Byte.MIN_VALUE;
            break;
        case SHORT:
        case USHORT:
            ret = data.getShortSample( col, row, band );
            break;
        case INT:
            ret = data.getIntSample( col, row, band );
            break;
        case FLOAT:
            ret = data.getFloatSample( col, row, band );
            break;
        default:
            LOG.error( "Cannot parse datatype '{}'", data.getDataType().toString() );
            throw new UnsupportedOperationException( "Cannot parse datatype: " + data.getDataType() );
        }

        return ret;
    }

    /**
     * Return a pixel value in a particular band, after performing contrastEnhancements.
     * 
     * NOTE: The user is responsible for calling <code>setGamma(double)</code> or
     * <code>setContrastEnhancements(...)</code> before calling this function.
     * 
     * @param col
     *            column index
     * @param row
     *            row index
     * @param band
     *            band index
     * @return the appropriate value, as a float value, after gamma correction
     */
    public float getEnhanced( int col, int row, int band ) {
        float ret = (float) 0.0;
        int i = -1;
        double size;

        switch ( data.getDataType() ) {
        case BYTE:
            i = data.getByteSample( col, row, band );
            // Compensate for byte being a signed datatype (-128...127). This is commonly used in grayscale images,
            // range 0-255
            if ( i < 0 )
                i += -2 * Byte.MIN_VALUE;
            if ( contrast != null ) {
                if ( contrast.histogram )
                    i = histogramTable[i];
                else if ( contrast.normalize )
                    i = normalizeTable[i];
            }
            ret = gammaTable[i];
            break;
        case SHORT:
        case USHORT:
            // Compute gamma correction on-the-fly for larger datatypes.
            i = data.getShortSample( col, row, band );
            size = Short.MAX_VALUE - Short.MIN_VALUE + 1;
            ret = (float) ( Math.pow( i / size, 1.0 / gamma ) * size );
            break;
        case INT:
            i = data.getIntSample( col, row, band );
            size = Integer.MAX_VALUE - Integer.MIN_VALUE + 1;
            ret = (float) ( Math.pow( i / size, 1.0 / gamma ) * size );
            break;
        case FLOAT:
            ret = data.getFloatSample( col, row, band );
            size = Float.MAX_VALUE - Float.MIN_VALUE + 1;
            ret = (float) ( Math.pow( ret / size, 1.0 / gamma ) * size );
            break;
        default:
            LOG.error( "Datatype '{}' is not suitable for gamma correction.", data.getDataType().toString() );
            throw new UnsupportedOperationException( "Datatype '" + data.getDataType()
                                                     + "' is not suitable for gamma correction." );
        }

        return ret;
    }

    /**
     * Update the gamma value
     * 
     * @param g
     *            new gamma value
     */
    public void setGamma( double g ) {
        if ( gamma != g )
            gammaTable = createGammaTable( g );
        gamma = g;
    }

    /* Create a look-up table for gamma-altered values */
    private int[] createGammaTable( double gamma ) {
        int[] table = new int[256];
        for ( int i = 0; i < 256; i++ ) {
            int v = (int) ( ( 255.0 * Math.pow( i / 255.0, 1.0 / gamma ) ) + 0.5 );
            if ( v > 255 )
                v = 255;
            table[i] = v;
        }
        return table;
    }

    /**
     * Precomputes weight factors for applying contrast enhancements to the current raster. Can apply enhancements to
     * all channels (if <code>index</code> is less than 0) or to a particular channel (if <code>index</code> is
     * non-negative). This function is relevant for normalize or histogram enhancements, where the operation is
     * dependent on the input raster data. All subsequent calls to <code>getEnhanced(col,row)</code> will use this
     * contrast enhancement.
     * 
     * NOTE: This function overrides the effects of <code>setGamma()</code>.
     * 
     * @param index
     *            target channel number.
     * @param enhancement
     *            desired contrast enhancement
     */
    public void precomputeContrastEnhancements( int index, ContrastEnhancement enhancement ) {
        LOG.trace( "Precomputing contrast tables ..." );
        long start = System.nanoTime();
        // Gamma
        if ( enhancement == null )
            return;
        setGamma( enhancement.gamma );

        // Normalize
        int col, row, i;
        if ( enhancement.normalize ) {
            switch ( this.data.getDataType() ) {
            case BYTE:
                datamin = Byte.MIN_VALUE;
                datamax = Byte.MAX_VALUE;
                break;
            default:
                LOG.error( "Datatype '{}' is not suitable for histogram contrast enhancement.",
                           data.getDataType().toString() );
                throw new UnsupportedOperationException( "Datatype '" + data.getDataType()
                                                         + "' is not suitable for histogram contrast enhancement." );
            }
            int val, min = datamax, max = datamin;
            if ( index < 0 ) {
                // Find minimum and maximum values for average pixel intensity
                for ( col = 0; col < this.width; col++ )
                    for ( row = 0; row < this.height; row++ ) {
                        val = (int) get( col, row );
                        min = ( val < min ? val : min );
                        max = ( val > max ? val : max );
                    }
            } else {
                // Find minimum and maximum values for pixel values in band "index"
                for ( col = 0; col < this.width; col++ )
                    for ( row = 0; row < this.height; row++ ) {
                        val = (int) get( col, row, index );
                        min = ( val < min ? val : min );
                        max = ( val > max ? val : max );
                    }
            }
            rastermin = min;
            rastermax = max;
            // Precompute a lookup table (only for BYTE data)
            min = ( 0 < rastermin ? 0 : rastermin );
            max = ( rastermax < 255 ? rastermax : 255 );
            for ( i = min; i <= max; i++ )
                normalizeTable[i] = 255 * i / ( rastermax - rastermin + 1 );
        }

        // Histogram
        if ( enhancement.histogram ) {
            switch ( data.getDataType() ) {
            case BYTE:
                // Count brightness values
                for ( i = 0; i < 256; i++ )
                    cdf[i] = 0;
                if ( index < 0 )
                    // Use average pixel intensity
                    for ( col = 0; col < this.width; col++ )
                        for ( row = 0; row < this.height; row++ )
                            cdf[(int) get( col, row )]++;
                else
                    // Use pixel value from band "index"
                    for ( col = 0; col < this.width; col++ )
                        for ( row = 0; row < this.height; row++ )
                            cdf[(int) get( col, row, index )]++;
                // Use the cumulative brightness values
                for ( i = 1; i < 256; i++ )
                    cdf[i] += cdf[i - 1];
                int nonnegpixels = width * height - cdf[0];
                // And precompute the histogram normalization lookup table (only for BYTE data)
                for ( i = 0; i < 256; i++ )
                    histogramTable[i] = (int) Math.floor( 255.0 * ( cdf[i] - cdf[0] ) / nonnegpixels );
                break;
            default:
                LOG.error( "Datatype '{}' is not suitable for histogram contrast enhancement.",
                           data.getDataType().toString() );
                throw new UnsupportedOperationException( "Datatype '" + data.getDataType()
                                                         + "' is not suitable for histogram contrast enhancement." );
            }
        }
        long end = System.nanoTime();
        LOG.trace( "Done precomputing contrast tables ({} ms).", ( end - start ) / 1000000 );
    }

}
