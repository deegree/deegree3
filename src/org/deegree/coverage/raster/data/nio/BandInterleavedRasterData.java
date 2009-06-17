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

import org.deegree.coverage.raster.data.BandType;
import org.deegree.coverage.raster.data.DataType;
import org.deegree.coverage.raster.data.InterleaveType;
import org.deegree.coverage.raster.data.RasterData;
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
     * @param bands
     *            number of bands
     * @param type
     *            DataType of raster samples
     */
    public BandInterleavedRasterData( RasterRect sampleDomain, int rasterWidth, int rasterHeight, BandType[] bands,
                                      DataType type ) {
        super( sampleDomain, rasterWidth, rasterHeight, bands, type );
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
     * @param bands
     *            number of bands
     * @param type
     *            DataType of raster samples
     */
    private BandInterleavedRasterData( RasterRect sampleDomain, int rasterWidth, int rasterHeight, BandType[] bands,
                                       DataType type, boolean init ) {
        super( sampleDomain, rasterWidth, rasterHeight, bands, type, init );
    }

    @Override
    public BandInterleavedRasterData createCompatibleRasterData( RasterRect sampleDomain, BandType[] bands ) {
        return new BandInterleavedRasterData( sampleDomain, rasterWidth, rasterHeight, bands, this.dataType, false );
    }

    @Override
    public RasterData createCompatibleWritableRasterData( RasterRect sampleDomain, BandType[] bands ) {
        return new BandInterleavedRasterData( sampleDomain, sampleDomain.width, sampleDomain.height, bands,
                                              this.dataType, true );
    }

    @Override
    protected ByteBufferRasterData createCompatibleEmptyRasterData() {
        return new BandInterleavedRasterData( view, rasterWidth, rasterHeight, bandsTypes, this.dataType, false );
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
        return dataType.getSize();
    }

    @Override
    public final InterleaveType getInterleaveType() {
        return InterleaveType.BAND;
    }

}
