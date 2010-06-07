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

package org.deegree.coverage.raster.data.info;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SampleModel;

import org.deegree.coverage.raster.data.RasterData;

/**
 * The <code>BandType</code> defines band information of a rasterdata object
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public enum BandType {

    /**
     * 
     */
    BAND_0( "The first band of this raster image which has no meta 'information' associated with it." ),
    /**
     * 
     */
    BAND_1( "The second band of this raster image which has no meta 'information' associated with it." ),
    /**
     * 
     */
    BAND_2( "The third band of this raster image which has no meta 'information' associated with it." ),
    /**
     * 
     */
    BAND_3( "The fourth band of this raster image which has no meta 'information' associated with it." ),
    /**
     * 
     */
    BAND_4( "The fifth band of this raster image which has no meta 'information' associated with it." ),
    /**
     * 
     */
    BAND_5( "The sixth band of this raster image which has no meta 'information' associated with it." ),
    /**
     * 
     */
    BAND_6( "The seventh band of this raster image which has no meta 'information' associated with it." ),
    /**
     * 
     */
    BAND_7( "The eighth band of this raster image which has no meta 'information' associated with it." ),
    /**
     * 
     */
    BAND_8( "The ninth band of this raster image which has no meta 'information' associated with it." ),
    /**
     * 
     */
    BAND_9( "The tenth band of this raster image which has no meta 'information' associated with it." ),
    /**
     * 
     */
    RED( "The band of this raster image which is associated with the color red." ),
    /**
     * 
     */
    GREEN( "The band of this raster image which is associated with the color green." ),
    /**
     * 
     */
    BLUE( "The band of this raster image which is associated with the color blue." ),
    /**
     * 
     */
    ALPHA( "The band of this raster image which is associated with an alpha value." ),
    /**
     * 
     */
    UNDEFINED( "No information about the band of this raster image." );

    /**
     * A standard RGB band type array
     */
    public final static BandType[] RGB = new BandType[] { RED, GREEN, BLUE };

    /**
     * A standard RGBA band type array
     */
    public final static BandType[] RGBA = new BandType[] { RED, GREEN, BLUE, ALPHA };

    private final String info;

    BandType( String info ) {
        this.info = info;
    }

    /**
     * @return a short description of the band.
     */
    public final String getInfo() {
        return info;
    }

    /**
     * Convert from {@link BufferedImage}-Types to {@link BandType}s. The size of the types are irrelevant.
     * (INT_ARGB==3BYTE_ARGB)
     * 
     * @param type
     *            The {@link DataBuffer}-Type (eg. TYPE_BYTE, etc.)
     * @param expectedSize
     *            if the type is unknown an array of expectedSize with {@link BandType#BAND_0}[9] will be returned.
     * @param sampleModel
     * @return The according BandType
     */
    public static BandType[] fromBufferedImageType( int type, int expectedSize, SampleModel sampleModel ) {
        if ( sampleModel instanceof PixelInterleavedSampleModel ) {
            PixelInterleavedSampleModel sm = (PixelInterleavedSampleModel) sampleModel;
            int[] offsets = sm.getBandOffsets();
            switch ( type ) {
            case BufferedImage.TYPE_3BYTE_BGR:
                if ( offsets.length == 3 ) {
                    BandType[] bands = new BandType[] { BLUE, GREEN, RED };
                    BandType[] bands2 = new BandType[] { BLUE, GREEN, RED };
                    for ( int i = 0; i < 3; ++i ) {
                        bands[i] = bands2[offsets[i]];
                    }
                    return bands;
                }
                break;
            }
        }
        switch ( type ) {
        case BufferedImage.TYPE_INT_ARGB:
        case BufferedImage.TYPE_INT_ARGB_PRE:
            return new BandType[] { ALPHA, RED, GREEN, BLUE };
        case BufferedImage.TYPE_4BYTE_ABGR:
        case BufferedImage.TYPE_4BYTE_ABGR_PRE:
            return new BandType[] { ALPHA, BLUE, GREEN, RED };
        case BufferedImage.TYPE_INT_RGB:
        case BufferedImage.TYPE_USHORT_555_RGB:
        case BufferedImage.TYPE_USHORT_565_RGB:
            return new BandType[] { RED, GREEN, BLUE };
        case BufferedImage.TYPE_INT_BGR:
        case BufferedImage.TYPE_3BYTE_BGR:
            return new BandType[] { BLUE, GREEN, RED };
        case RasterData.TYPE_BYTE_RGBA:
            return new BandType[] { RED, GREEN, BLUE, ALPHA };
        case RasterData.TYPE_BYTE_RGB:
            return new BandType[] { RED, GREEN, BLUE };
        default:
            BandType[] result = new BandType[expectedSize];
            for ( int i = 0; i < expectedSize; ++i ) {
                result[i] = BandType.values()[i];
            }
            return result;
        }
    }

    /**
     * Get Bandtype for the given string. This method is case insensitive, words can be separated with a whitespace,
     * minus or underscore.
     * 
     * @param band
     * @return the given BandType or <code>null</code> if the band type was not known.
     */
    public static BandType fromString( String band ) {
        String key = band.toUpperCase();
        key = key.replaceAll( "-", "_" );
        key = key.replaceAll( "\\s", "_" );
        BandType bt = null;
        try {
            bt = BandType.valueOf( key );
        } catch ( Exception e ) {
            // just let null be our guest
        }
        return bt;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
