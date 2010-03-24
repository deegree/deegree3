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

package org.deegree.coverage.raster.data.info;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * The <code>RasterDataInfo</code> class encapsulates
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class RasterDataInfo {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( RasterDataInfo.class );

    /** The values of the bands, which are used for no data. */
    public final byte[] noDataPixel;

    /** The definitions of the bands */
    public final BandType[] bandInfo;

    /** The datatype of the data */
    public final DataType dataType;

    /** Interleave information */
    public final InterleaveType interleaveType;

    /** The number of bands of the raster data */
    public final int bands;

    /** The byte size of a single band unit */
    public final int dataSize;

    private final ByteBuffer noDataWrapper;

    /**
     * 
     * @param noDataPixel
     *            to be used if no data was found in a given subset.
     * @param bandInfo
     *            of the raster data
     * @param dataType
     *            of the raster data
     * @param interleaveType
     *            of the raster data.
     */
    public RasterDataInfo( byte[] noDataPixel, BandType[] bandInfo, DataType dataType, InterleaveType interleaveType ) {
        if ( bandInfo == null || bandInfo.length == 0 ) {
            throw new NullPointerException( "BandType may not be null" );
        }

        if ( dataType == null ) {
            throw new NullPointerException( "DataType may not be null" );
        }

        this.bandInfo = bandInfo;
        this.bands = bandInfo.length;

        this.dataType = dataType;
        this.dataSize = dataType.getSize();

        this.interleaveType = interleaveType;

        if ( noDataPixel == null || ( noDataPixel.length != ( bands * dataSize ) ) ) {
            this.noDataPixel = new byte[bands * dataSize];
        } else {
            this.noDataPixel = noDataPixel;
        }
        this.noDataWrapper = ByteBuffer.wrap( this.noDataPixel );
    }

    /**
     * create a new {@link RasterDataInfo} and initialize the no data pixel with null.
     * 
     * @param bandInfo
     *            of the raster data
     * @param dataType
     *            of the raster data
     * @param interleaveType
     *            of the raster data.
     */
    public RasterDataInfo( BandType[] bandInfo, DataType dataType, InterleaveType interleaveType ) {
        this( null, bandInfo, dataType, interleaveType );
    }

    /**
     * 
     * @return the information on the bands.
     */
    public final BandType[] getBandInfo() {
        return bandInfo;
    }

    /**
     * @return number of bands, equals {@link #getBandInfo()}.length
     */
    public final int bands() {
        return bands;
    }

    /**
     * @return the dataType
     */
    public final DataType getDataType() {
        return dataType;
    }

    /**
     * @return the interleaveType
     */
    public final InterleaveType getInterleaveType() {
        return interleaveType;
    }

    /**
     * Returns the no data values for this raster's bands
     * 
     * @param result
     *            an array to put the values into or <code>null</code>
     * @return the <code>result</code> array or a new array, if the <code>result</code> array is <code>null</code> or to
     *         small
     */
    public byte[] getNoDataPixel( byte[] result ) {
        if ( result == null || result.length < noDataPixel.length ) {
            result = new byte[noDataPixel.length];
        }
        System.arraycopy( noDataPixel, 0, result, 0, result.length );
        return result;
    }

    /**
     * Returns the no data values for this raster's bands
     * 
     * @param bands
     *            to copy the null data pixel values for.
     * @return the <code>result</code> array or a new array, if the <code>result</code> array is <code>null</code>
     */
    public byte[] getNoDataPixel( BandType[] bands ) {
        if ( bands == null || bands.length == 0 ) {
            return null;
        }
        byte[] result = new byte[bands.length * dataSize];

        int pos = 0;
        // fill the null pixelvalues from the given bands
        for ( BandType b : bands ) {
            if ( b != null ) {
                for ( int i = 0; i < this.bandInfo.length; ++i ) {
                    if ( b == this.bandInfo[i] ) {
                        System.arraycopy( noDataPixel, i * dataSize, result, pos, dataSize );
                        break;
                    }
                }
                pos++;
            }

        }
        return result;
    }

    /**
     * Returns the no data values for the given band. If the band is outside an empty array is returned. If the result
     * is <code>null</code> or is to small for the datasize, a new allocated byte array will be returned. Otherwise the
     * no data value for the requested band is copied at position 0 of the result array.
     * 
     * @param band
     *            to get the value for.
     * @param result
     *            to put the value in.
     * @return the <code>result</code> array or a new array, if the <code>result</code> array is <code>null</code>
     */
    public byte[] getNoDataSample( int band, byte[] result ) {
        if ( result == null || result.length < dataSize ) {
            result = new byte[dataSize];
        }
        if ( band > bands ) {
            return result;
        }
        System.arraycopy( noDataPixel, band * dataSize, result, 0, dataSize );
        return result;
    }

    /**
     * Returns the no data value for the given band, if the band is outside the number of bands or the given type is not
     * of {@link DataType#BYTE} 0 will be returned.
     * 
     * @param band
     *            to get the no data value for.
     * @return the no data value for the given band.
     */
    public byte getByteNoDataForBand( int band ) {
        byte result = 0;
        if ( band < this.bands ) {
            result = noDataPixel[band];
        }
        return result;
    }

    /**
     * Returns the no data value for the given band, if the band is outside the number of bands or the given type is not
     * of {@link DataType#SHORT} or {@link DataType#USHORT} 0 will be returned.
     * 
     * @param band
     *            to get the no data value for.
     * @return the no data value for the given band.
     */
    public short getShortNoDataForBand( int band ) {
        short result = 0;
        if ( band < this.bands && ( this.dataType == DataType.SHORT || this.dataType == DataType.USHORT ) ) {
            result = noDataWrapper.getShort( band * dataSize );
            if ( this.dataType == DataType.USHORT ) {
                result *= 0xFFFF;
            }
        }
        return result;
    }

    /**
     * Returns the no data value for the given band, if the band is outside the number of bands or the given type is not
     * of {@link DataType#INT} 0 will be returned.
     * 
     * @param band
     *            to get the no data value for.
     * @return the no data value for the given band.
     */
    public int getIntNoDataForBand( int band ) {
        int result = 0;
        if ( band < this.bands && this.dataType == DataType.INT ) {
            result = noDataWrapper.getInt( band * dataSize );
        }
        return result;
    }

    /**
     * Returns the no data value for the given band, if the band is outside the number of bands or the given type is not
     * of {@link DataType#FLOAT} 0 will be returned.
     * 
     * @param band
     *            to get the no data value for.
     * @return the no data value for the given band.
     */
    public float getFloatNoDataForBand( int band ) {
        float result = 0;
        if ( band < this.bands && this.dataType == DataType.FLOAT ) {
            result = noDataWrapper.getFloat( band * dataSize );
        }
        return result;
    }

    /**
     * Returns the no data value for the given band, if the band is outside the number of bands or the given type is not
     * of {@link DataType#DOUBLE} 0 will be returned.
     * 
     * @param band
     *            to get the no data value for.
     * @return the no data value for the given band.
     */
    public double getDoubleNoDataForBand( int band ) {
        double result = 0;
        if ( band < this.bands && this.dataType == DataType.DOUBLE ) {
            result = noDataWrapper.getDouble( band * dataSize );
        }
        return result;
    }

    /**
     * Sets the no data values for this raster's bands
     * 
     * @param values
     *            an array with the null values
     */
    public void setNoDataPixel( byte[] values ) {
        if ( values == null || values.length % dataSize != 0 ) {
            LOG.error( "invalid null pixel values" );
            return;
        }
        if ( values.length == noDataPixel.length ) {
            System.arraycopy( values, 0, noDataPixel, 0, noDataPixel.length );
        } else {
            for ( int b = 0; b < bands; b++ ) {
                System.arraycopy( values, 0, noDataPixel, dataSize * b, dataSize );
            }
        }
    }

    /**
     * @return the byte size of one unit of a band.
     */
    public int getDataSize() {
        return dataSize;
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof RasterDataInfo ) {
            final RasterDataInfo that = (RasterDataInfo) other;
            return this.dataSize == that.dataSize && this.dataType == that.dataType
                   && this.interleaveType == that.interleaveType && Arrays.equals( this.bandInfo, that.bandInfo )
                   && Arrays.equals( this.noDataPixel, that.noDataPixel );
        }
        return false;
    }

    @Override
    public String toString() {
        return "[Band info: " + Arrays.toString( bandInfo ) + ", data size: " + dataSize + ", interleave type: "
               + interleaveType + "]";
    }

}
