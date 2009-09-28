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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses 4 channel (32Bit) images as a Digital Elevation Model and returns a
 * float matrix containing the DEM heights.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: aaiordachioaie $
 *
 * @version $Revision: 18195 $, $Date: 2009-09-28 20:00:39 +0200 (28 Sep 2009) $
 */
public class DEMImage2RawData implements Image2RawData {

    private final static Logger LOG = LoggerFactory.getLogger(DEMImage2RawData.class);

    private BufferedImage image;

    private DataBuffer db;

    private float scale = 0;

    private float offset = 0;

    private int width = 0;

    private int height = 0;

    /**
     *
     * @param data
     *            image containing raw data instead color information
     */
    public DEMImage2RawData( BufferedImage data ) {
        this( data, 1, 0 );
    }

    /**
     *
     * @param data
     *            image containing raw data instead color information
     * @param scale
     *            scale factor; newHeight[i][j] = height[i][j] * scale
     */
    public DEMImage2RawData( BufferedImage data, float scale ) {
        this( data, scale, 0 );
    }

    /**
     *
     * @param data
     *            image containing raw data instead color information
     * @param scale
     *            scale factor; newHeight[i][j] = height[i][j] * scale
     * @param offset
     *            height offset; newHeight[i][j] = height[i][j] + offset
     */
    public DEMImage2RawData( BufferedImage data, float scale, float offset ) {
        image = data;
        db = image.getData().getDataBuffer();
        this.scale = scale;
        this.offset = offset;
        width = data.getWidth();
        height = data.getHeight();
    }

    /**
     * returns the DEM heights as float matrix
     *
     * @return the DEM heights as float matrix
     */
    @Override
    public Float[][] parse() {
        if ( db == null )
            db = image.getData().getDataBuffer();
        Float[][] terrain = new Float[height][width];

        int ps = image.getColorModel().getPixelSize();
        LOG.debug("Parsing Image data. Each pixel has {} bytes.", ps);
        switch (ps)
        {
            case 8:
                for ( int j = 0; j < height; j++ ) {
                    for ( int i = 0; i < width; i++ ) {
                        terrain[j][i] = db.getElem( width * j + i ) * scale + offset;
                    }
                }
                break;
            case 16:
                for ( int j = 0; j < height; j++ ) {
                    for ( int i = 0; i < width; i++ ) {
                        terrain[j][i] = db.getElemFloat( width * j + i ) * scale + offset;
                    }
                }
                break;
            case 24:
            case 32:
                for ( int j = 0; j < height; j++ ) {
                    for ( int i = 0; i < width; i++ ) {
                        int v = image.getRGB( i, j );
                        terrain[j][i] = Float.intBitsToFloat( v ) * scale + offset;
                    }
                }
                break;
            default:
                LOG.warn("Unknown data format, with {} bits per pixel", ps);
                terrain = null;
        }

        return terrain;
    }

    /**
     * @param x
     *            index
     * @param y
     *            index
     * @return the appropriate value, or null if image has more than 32 bpp
     */
    @Override
    public Float get( int x, int y ) {

        Float ret = null;
        int ps = image.getColorModel().getPixelSize();
        if ( db == null )
            db = image.getData().getDataBuffer();

        switch (ps)
        {
            case 8:
                ret = db.getElem( width * y + x ) * scale + offset;
                break;
            case 16:
                ret = db.getElemFloat( width * y + x ) * scale + offset;
                break;
            case 24:
            case 32:
                ret = Float.intBitsToFloat( image.getRGB( x, y ) ) * scale + offset;
                break;
            default:
                ret = null;
        }

        return ret;

    }

}
