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
package org.deegree.coverage.raster.data.nio;

import org.deegree.coverage.raster.data.DataView;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterRect;

/**
 * This class implements a band-interleaved, ByteBuffer-based RasterData.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class BandInterleavedRasterData extends ByteBufferRasterData {

    /**
     * Creates a new BandInterleavedRasterData with given size, number of bands and data type
     * 
     * @param sampleDomain
     *            the raster rectangle defining the sample domain of this raster data.
     * @param rasterWidth
     *            width of the underlying raster data.
     * @param rasterHeight
     *            height of the underlying raster data.
     * @param dataInfo
     *            containing information about the underlying raster data.
     */
    public BandInterleavedRasterData( RasterRect sampleDomain, int rasterWidth, int rasterHeight,
                                      RasterDataInfo dataInfo ) {
        this( new DataView( sampleDomain, dataInfo ), rasterWidth, rasterHeight, dataInfo, true );
    }

    /**
     * Creates a new BandInterleavedRasterData with given size, number of bands and data type
     * 
     * @param sampleDomain
     *            the raster rectangle defining the sample domain of this raster data.
     * @param rasterWidth
     *            width of the underlying raster data.
     * @param rasterHeight
     *            height of the underlying raster data.
     * @param type
     *            DataType of raster samples
     */
    private BandInterleavedRasterData( DataView view, int rasterWidth, int rasterHeight, RasterDataInfo dataInfo,
                                       boolean init ) {
        super( view, rasterWidth, rasterHeight, dataInfo, init );
    }

    @Override
    public BandInterleavedRasterData createCompatibleRasterData( DataView view ) {
        return new BandInterleavedRasterData( view, rasterWidth, rasterHeight, dataInfo, false );
    }

    @Override
    public RasterData createCompatibleWritableRasterData( RasterRect sampleDomain, BandType[] bands ) {
        // a new raster will be created, the old information should be discarded.
        RasterDataInfo newRasterInfo = createRasterDataInfo( bands );
        return new BandInterleavedRasterData( new DataView( sampleDomain, newRasterInfo ), sampleDomain.width,
                                              sampleDomain.height, newRasterInfo, true );
    }

    @Override
    protected ByteBufferRasterData createCompatibleEmptyRasterData() {
        return new BandInterleavedRasterData( view, rasterWidth, rasterHeight, this.dataInfo, false );
    }

    @Override
    public final int getBandStride() {
        return rasterWidth * getPixelStride() * rasterHeight;
    }

    @Override
    public final int getLineStride() {
        return rasterWidth * getPixelStride();
    }

    @Override
    public final int getPixelStride() {
        return dataInfo.dataSize;
    }

}
