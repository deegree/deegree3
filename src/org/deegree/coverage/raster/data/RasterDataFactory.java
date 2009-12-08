//$HeadURL:svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/coverage/raster/data/RasterDataFactory.java $
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
package org.deegree.coverage.raster.data;

import java.io.File;

import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.InterleaveType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.data.nio.BandInterleavedRasterData;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.data.nio.LineInterleavedRasterData;
import org.deegree.coverage.raster.data.nio.PixelInterleavedRasterData;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterReader;

/**
 * This class creates RasterData objects with a given interleaving type.
 * 
 * This factory creates RasterData objects based on ByteBufferRasterData.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author:rbezema $
 * 
 * @version $Revision:11404 $, $Date:2008-04-23 15:38:27 +0200 (Mi, 23 Apr 2008) $
 */
public class RasterDataFactory {

    private final static File CACHE_DIR = new File( "/tmp/" );

    /**
     * Creates a pixel-interleaved RasterData object with given size and data type
     * 
     * @param width
     *            width of the raster
     * @param height
     *            height of the raster
     * @param dataType
     *            data type for samples
     * @return new RasterData
     */
    public static ByteBufferRasterData createRasterData( int width, int height, DataType dataType ) {
        return createRasterData( width, height, new BandType[] { BandType.BAND_0 }, dataType, InterleaveType.PIXEL );
    }

    /**
     * Creates a RasterData object object with given size, number of bands, data type and interleaving
     * 
     * @param width
     *            width of the raster
     * @param height
     *            height of the raster
     * @param bands
     *            number and type of the bands
     * @param dataType
     *            data type for samples
     * @param interleaveType
     *            interleaving type for the raster
     * @return new RasterData
     */
    public static ByteBufferRasterData createRasterData( int width, int height, BandType[] bands, DataType dataType,
                                                         InterleaveType interleaveType ) {
        return createRasterData( width, height, bands, dataType, interleaveType, null );
    }

    /**
     * Creates a RasterData object object with given size, number of bands, data type and interleaving
     * 
     * @param width
     *            width of the raster
     * @param height
     *            height of the raster
     * @param bands
     *            number and type of the bands
     * @param dataType
     *            data type for samples
     * @param interleaveType
     *            interleaving type for the raster
     * @param reader
     *            to get the data from, maybe <code>null</code>
     * @return new RasterData
     */
    public static ByteBufferRasterData createRasterData( int width, int height, BandType[] bands, DataType dataType,
                                                         InterleaveType interleaveType, RasterReader reader ) {

        ByteBufferRasterData result;
        RasterDataInfo dataInfo = new RasterDataInfo( null, bands, dataType, interleaveType );
        switch ( interleaveType ) {
        case PIXEL:
            result = new PixelInterleavedRasterData( new RasterRect( 0, 0, width, height ), width, height, reader,
                                                     dataInfo );
            break;
        case LINE:
            result = new LineInterleavedRasterData( new RasterRect( 0, 0, width, height ), width, height, reader,
                                                    dataInfo );
            break;
        case BAND:
            result = new BandInterleavedRasterData( new RasterRect( 0, 0, width, height ), width, height, reader,
                                                    dataInfo );
            break;
        default:
            throw new UnsupportedOperationException( "Interleaving type " + interleaveType + " not supported!" );
        }
        return result;
    }

    /**
     * @return the cache dir
     * 
     */
    public static File getCacheLocation() {
        return CACHE_DIR;

    }
}
