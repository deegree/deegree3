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
     * @param width
     *            width of new raster
     * @param height
     *            height of new raster
     * @param bands
     *            number of bands
     * @param type
     *            DataType of raster samples
     */
    public BandInterleavedRasterData( int width, int height, int bands, DataType type ) {
        super( width, height, bands, type );
    }
    
    private BandInterleavedRasterData( int width, int height, int bands, DataType type, boolean init ) {
        super( width, height, bands, type, init );
    }

    @Override
    public BandInterleavedRasterData createCompatibleRasterData( int width, int height, int bands ) {
        return new BandInterleavedRasterData( width, height, bands, this.dataType, true );
    }

    @Override
    protected ByteBufferRasterData createCompatibleEmptyRasterData() {
        return new BandInterleavedRasterData( width, height, bands, this.dataType, false );
    }

    @Override
    public final int getBandStride() {
        return width * getPixelStride() * height;
    }

    @Override
    public final int getLineStride() {
        return width * getPixelStride();
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
