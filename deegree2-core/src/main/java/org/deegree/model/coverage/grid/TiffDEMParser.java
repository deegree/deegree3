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
package org.deegree.model.coverage.grid;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;

import org.apache.batik.ext.awt.image.codec.FileCacheSeekableStream;
import org.apache.batik.ext.awt.image.codec.tiff.TIFFDecodeParam;
import org.apache.batik.ext.awt.image.codec.tiff.TIFFImage;

/**
 * Parses 4 channel (32Bit) tiff images as DEM and returns a float matrix containing the DEM heights
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class TiffDEMParser {

    private DataBuffer db;

    private float scale = 0;

    private float offset = 0;

    private int width = 0;

    private int height = 0;

    /**
     *
     * @param dataFile
     *            image containing DEM data instead color information
     */
    public TiffDEMParser( File dataFile ) {
        this( dataFile, 1, 0 );
    }

    /**
     *
     * @param dataFile
     *            image containing DEM data instead color information
     * @param scale
     *            scale factor; newHeight[i][j] = height[i][j] * scale
     */
    public TiffDEMParser( File dataFile, float scale ) {
        this( dataFile, scale, 0 );
    }

    /**
     *
     * @param dataFile
     *            image containing DEM data instead color information
     * @param scale
     *            scale factor; newHeight[i][j] = height[i][j] * scale
     * @param offset
     *            height offset; newHeight[i][j] = height[i][j] + offset
     */
    public TiffDEMParser( File dataFile, float scale, float offset ) {
        try {
            FileCacheSeekableStream fss = new FileCacheSeekableStream( dataFile.toURL().openStream() );
            TIFFImage tiff = new TIFFImage( fss, new TIFFDecodeParam(), 0 );
            db = tiff.getData().getDataBuffer();
            width = tiff.getWidth();
            height = tiff.getHeight();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        this.scale = scale;
        this.offset = offset;
    }

    /**
     *
     * @param data
     *            image containing DEM data instead color information
     */
    public TiffDEMParser( BufferedImage data ) {
        this( data, 1, 0 );
    }

    /**
     *
     * @param data
     *            image containing DEM data instead color information
     * @param scale
     *            scale factor; newHeight[i][j] = height[i][j] * scale
     */
    public TiffDEMParser( BufferedImage data, float scale ) {
        this( data, scale, 0 );
    }

    /**
     *
     * @param data
     * @param scale
     *            scale factor; newHeight[i][j] = height[i][j] * scale
     * @param offset
     *            height offset; newHeight[i][j] = height[i][j] + offset
     */
    public TiffDEMParser( BufferedImage data, float scale, float offset ) {
        this.db = data.getRaster().getDataBuffer();
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
    public float[][] parse() {

        float[][] terrain = new float[height][width];

        for ( int j = 0; j < height; j++ ) {
            for ( int i = 0; i < width; i++ ) {
                terrain[j][i] = db.getElemFloat( width * j + i ) * scale + offset;
            }
        }

        return terrain;

    }

}
