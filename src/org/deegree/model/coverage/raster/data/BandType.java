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

package org.deegree.model.coverage.raster.data;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;

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
     * @return The according BandType
     */
    public static BandType[] fromBufferedImageType( int type, int expectedSize ) {
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
}
