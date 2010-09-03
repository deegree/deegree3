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

package org.deegree.coverage.raster.io.grid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Set;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.data.container.BufferResult;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;

/**
 * 
 * A simple wrapper class needed to mark the offset for a given tile in the total grid.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class TileOffsetReader implements RasterReader {

    private final GridReader originalReader;

    private final RasterRect tileRectInGrid;

    /**
     * @param original
     *            mapped gridreader
     * @param tileRectInGrid
     *            the rectangle inside the grid.
     * 
     */
    public TileOffsetReader( GridReader original, RasterRect tileRectInGrid ) {
        this.originalReader = original;
        this.tileRectInGrid = tileRectInGrid;
    }

    @Override
    public boolean canLoad( File filename ) {
        return originalReader.canLoad( filename );
    }

    @Override
    public File file() {
        return originalReader.file();
    }

    @Override
    public RasterGeoReference getGeoReference() {
        return originalReader.getGeoReference();
    }

    @Override
    public int getHeight() {
        return originalReader.getHeight();
    }

    @Override
    public Set<String> getSupportedFormats() {
        return originalReader.getSupportedFormats();
    }

    @Override
    public int getWidth() {
        return originalReader.getWidth();
    }

    @Override
    public AbstractRaster load( File filename, RasterIOOptions options )
                            throws IOException {
        return originalReader.load( filename, options );
    }

    @Override
    public AbstractRaster load( InputStream stream, RasterIOOptions options )
                            throws IOException {
        return originalReader.load( stream, options );
    }

    @Override
    public BufferResult read( RasterRect rect, ByteBuffer buffer )
                            throws IOException {
        RasterRect tmpRect = new RasterRect( rect.x + tileRectInGrid.x, rect.y + tileRectInGrid.y, rect.width,
                                             rect.height );
        BufferResult bufferResult = originalReader.read( tmpRect, buffer );
        bufferResult.getResult().clear();
        // PixelInterleavedRasterData rd = new PixelInterleavedRasterData( bufferResult.getRect(),
        // bufferResult.getRect().width,
        // bufferResult.getRect().height,
        // new RasterDataInfo( BandType.RGB,
        // DataType.BYTE,
        // InterleaveType.PIXEL ) );
        // rd.setByteBuffer( bufferResult.getResult() );
        // BufferedImage image = RasterFactory.rasterDataToImage( rd );
        // ImageIO.write( image, "png", new File( "/tmp/" + tmpRect.toString() + ".png" ) );
        // bufferResult.getRect().x -= tileRectInGrid.x;
        // bufferResult.getRect().y -= tileRectInGrid.y;
        return bufferResult;
    }

    @Override
    public boolean shouldCreateCacheFile() {
        return originalReader.shouldCreateCacheFile();
    }

    @Override
    public RasterDataInfo getRasterDataInfo() {
        return originalReader.getRasterDataInfo();
    }

    @Override
    public boolean canReadTiles() {
        return originalReader.canReadTiles();
    }

    @Override
    public String getDataLocationId() {
        return originalReader.getDataLocationId();
    }

    @Override
    public void dispose() {
        originalReader.dispose();
    }
}
