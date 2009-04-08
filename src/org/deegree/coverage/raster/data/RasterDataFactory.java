//$HeadURL:svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/coverage/raster/data/RasterDataFactory.java $
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
package org.deegree.coverage.raster.data;

import org.deegree.coverage.raster.data.nio.BandInterleavedRasterData;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.data.nio.LineInterleavedRasterData;
import org.deegree.coverage.raster.data.nio.PixelInterleavedRasterData;
import org.deegree.coverage.raster.geom.RasterRect;

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

        ByteBufferRasterData result;

        if ( interleaveType == InterleaveType.PIXEL ) {
            result = new PixelInterleavedRasterData( new RasterRect( 0, 0, width, height ), width, height, bands,
                                                     dataType );
        } else if ( interleaveType == InterleaveType.LINE ) {
            result = new LineInterleavedRasterData( new RasterRect( 0, 0, width, height ), width, height, bands,
                                                    dataType );
        } else if ( interleaveType == InterleaveType.BAND ) {
            result = new BandInterleavedRasterData( new RasterRect( 0, 0, width, height ), width, height, bands,
                                                    dataType );
        } else {
            throw new UnsupportedOperationException( "Interleaving type " + interleaveType + " not supported!" );
        }
        return result;
    }
}
