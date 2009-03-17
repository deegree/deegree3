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
package org.deegree.model.coverage.raster.data.nio;

import org.deegree.model.coverage.raster.data.BandType;
import org.deegree.model.coverage.raster.data.DataType;
import org.deegree.model.coverage.raster.data.InterleaveType;
import org.deegree.model.coverage.raster.data.RasterData;
import org.deegree.model.coverage.raster.geom.RasterRect;

/**
 * This class implements a line-interleaved, ByteBuffer-based RasterData.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LineInterleavedRasterData extends ByteBufferRasterData {

    /**
     * Creates a new LineInterleavedRasterData with given size, number of bands and data type
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
    public LineInterleavedRasterData( RasterRect sampleDomain, int rasterWidth, int rasterHeight, BandType[] bands,
                                      DataType type ) {
        super( sampleDomain, rasterWidth, rasterHeight, bands, type );
    }

    /**
     * Creates a new LineInterleavedRasterData with given size, number of bands and data type and initializes a new
     * bytebuffer if init is set to true.
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
    private LineInterleavedRasterData( RasterRect sampleDomain, int rasterWidth, int rasterHeight, BandType[] bands,
                                       DataType type, boolean init ) {
        super( sampleDomain, rasterWidth, rasterHeight, bands, type, init );
    }

    @Override
    public LineInterleavedRasterData createCompatibleRasterData( RasterRect env, BandType[] bands ) {
        return new LineInterleavedRasterData( env, rasterWidth, rasterHeight, bands, this.dataType, false );
    }

    @Override
    public RasterData createCompatibleWritableRasterData( RasterRect sampleDomain, BandType[] bands ) {
        return new LineInterleavedRasterData( sampleDomain, sampleDomain.width, sampleDomain.height, bands,
                                              this.dataType, true );
    }

    @Override
    protected ByteBufferRasterData createCompatibleEmptyRasterData() {
        return new LineInterleavedRasterData( view, rasterWidth, rasterHeight, bandsTypes, this.dataType, false );
    }

    @Override
    public final int getBandStride() {
        return rasterWidth * getPixelStride();
    }

    @Override
    public final int getLineStride() {
        return rasterWidth * getPixelStride() * bands;
    }

    @Override
    public final int getPixelStride() {
        return dataType.getSize();
    }

    @Override
    public final InterleaveType getInterleaveType() {
        return InterleaveType.LINE;
    }

}
