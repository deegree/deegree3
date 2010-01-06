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
import java.nio.ByteBuffer;

import org.deegree.coverage.raster.cache.CacheRasterReader;
import org.deegree.coverage.raster.cache.RasterCache;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.InterleaveType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.data.nio.BandInterleavedRasterData;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.data.nio.LineInterleavedRasterData;
import org.deegree.coverage.raster.data.nio.PixelInterleavedRasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.io.grid.GridReader;

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
     * @param addToCache
     *            if the reader should be added to the cache (almost always should be true).
     * @return new RasterData
     */
    public static ByteBufferRasterData createRasterData( int width, int height, DataType dataType, boolean addToCache ) {
        return createRasterData( width, height, new BandType[] { BandType.BAND_0 }, dataType, InterleaveType.PIXEL,
                                 addToCache );
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
     * @param addToCache
     *            if the reader should be added to the cache (almost always should be true).
     * @return new RasterData
     */
    public static ByteBufferRasterData createRasterData( int width, int height, BandType[] bands, DataType dataType,
                                                         InterleaveType interleaveType, boolean addToCache ) {
        return createRasterData( width, height, bands, dataType, interleaveType, null, addToCache );
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
     * @param addToCache
     *            if the reader should be added to the cache (almost always should be true).
     * @return new RasterData
     */
    public static ByteBufferRasterData createRasterData( int width, int height, BandType[] bands, DataType dataType,
                                                         InterleaveType interleaveType, RasterReader reader,
                                                         boolean addToCache ) {

        return createRasterData( width, height, createRDI( null, bands, dataType, interleaveType ), reader, addToCache );
    }

    /**
     * Creates a RasterData object object with given size, number of bands, data type and interleaving. The view will be
     * 0, 0, width, height, the default raster cache location will be used.
     * 
     * @param width
     *            width of the raster
     * @param height
     *            height of the raster
     * @param dataInfo
     *            defining the bands, datatype and interleave type.
     * @param reader
     *            to get the data from, maybe <code>null</code>
     * @param addToCache
     *            if the reader should be added to the cache (almost always should be true).
     * @return new RasterData
     */
    public static ByteBufferRasterData createRasterData( int width, int height, RasterDataInfo dataInfo,
                                                         RasterReader reader, boolean addToCache ) {
        return createRasterData( new RasterRect( 0, 0, width, height ), dataInfo, reader, addToCache, null );
    }

    /**
     * Creates a RasterData object object with given view, number of bands, data type and interleaving.
     * 
     * @param view
     *            valid for the raster data.
     * @param dataInfo
     *            defining the bands, datatype and interleave type.
     * @param reader
     *            to get the data from, maybe <code>null</code>
     * @param addToCache
     *            if the reader should be added to the cache (almost always should be true).
     * @param options
     *            containing information about the raster cache directory.
     * @return new RasterData
     */
    public static ByteBufferRasterData createRasterData( RasterRect view, RasterDataInfo dataInfo, RasterReader reader,
                                                         boolean addToCache, RasterIOOptions options ) {
        RasterReader newReader = reader;
        if ( addToCache ) {
            RasterCache cache = RasterCache.getInstance( options );
            newReader = cache.addReader( reader );
        }
        ByteBufferRasterData result;
        switch ( dataInfo.interleaveType ) {
        case PIXEL:
            result = new PixelInterleavedRasterData( view, view.width, view.height, newReader, dataInfo );
            break;
        case LINE:
            result = new LineInterleavedRasterData( view, view.width, view.height, newReader, dataInfo );
            break;
        case BAND:
            result = new BandInterleavedRasterData( view, view.width, view.height, newReader, dataInfo );
            break;
        default:
            throw new UnsupportedOperationException( "Interleaving type " + dataInfo.interleaveType + " not supported!" );
        }
        byte[] noData = ( options == null ) ? null : options.getNoDataValue();
        if ( noData != null ) {
            result.setNoDataValue( noData );
        }
        return result;
    }

    /**
     * Creates a RasterData object object with given size, number of bands, data type and interleaving filled with the
     * given ByteArray. A cachereader will be created.
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
     * @param geoRef
     *            needed for the reader.
     * @param byteBuffer
     *            on which the memory reader will operate upon.
     * @param createCacheFile
     *            true if a cache file should back the data
     * @param cacheId
     *            the name of the cache file to use in the default {@link RasterCache}.
     * @return new RasterData
     */
    public static ByteBufferRasterData createRasterData( int width, int height, BandType[] bands, DataType dataType,
                                                         InterleaveType interleaveType, RasterGeoReference geoRef,
                                                         ByteBuffer byteBuffer, boolean createCacheFile, String cacheId ) {
        RasterDataInfo rdi = createRDI( null, bands, dataType, interleaveType );
        return createRasterData( width, height, rdi, geoRef, byteBuffer, createCacheFile, cacheId );
    }

    /**
     * Creates a RasterData object object with given size, number of bands, data type and interleaving filled with the
     * given ByteArray. A cachereader will be created.
     * 
     * @param width
     *            width of the raster
     * @param height
     *            height of the raster
     * @param rdi
     *            containing the number of bands, the data type and the interleave type.
     * @param geoRef
     *            needed for the reader.
     * @param byteBuffer
     *            on which the memory reader will operate upon.
     * @param createCacheFile
     *            true if a cache file should back the data
     * @param cacheId
     *            the name of the cache file to use in the default {@link RasterCache}.
     * @return new RasterData
     */
    public static ByteBufferRasterData createRasterData( int width, int height, RasterDataInfo rdi,
                                                         RasterGeoReference geoRef, ByteBuffer byteBuffer,
                                                         boolean createCacheFile, String cacheId ) {
        RasterCache cache = RasterCache.getInstance();
        File cacheFile = null;
        if ( createCacheFile ) {
            cacheFile = cache.createCacheFile( cacheId );
        }
        CacheRasterReader inMem = new CacheRasterReader( byteBuffer, width, height, cacheFile, createCacheFile, rdi,
                                                         geoRef, cache );
        return createRasterData( width, height, rdi, inMem, true );
    }

    /**
     * Creates a RasterData object object with given size, number of bands, data type and interleaving filled with the
     * given ByteArray. A cachereader will be created.
     * 
     * @param width
     *            width of the raster
     * @param height
     *            height of the raster
     * @param rdi
     *            containing the number of bands, the data type and the interleave type.
     * @param geoRef
     *            needed for the reader.
     * @param byteBuffer
     *            on which the memory reader will operate upon.
     * @param createCacheFile
     *            true if a cache file should back the data
     * @param cacheId
     *            the name of the cache file to use in the default {@link RasterCache}.
     * @param options
     *            which can be used to get the cache data from.
     * @return new RasterData
     */
    public static ByteBufferRasterData createRasterData( int width, int height, RasterDataInfo rdi,
                                                         RasterGeoReference geoRef, ByteBuffer byteBuffer,
                                                         boolean createCacheFile, String cacheId,
                                                         RasterIOOptions options ) {
        return createRasterData( new RasterRect( 0, 0, width, height ), rdi, geoRef, byteBuffer, createCacheFile,
                                 cacheId, options );
    }

    /**
     * Creates a RasterData object object with given size, number of bands, data type and interleaving filled with the
     * given ByteArray. A cachereader will be created.
     * 
     * @param view
     * @param rdi
     *            containing the number of bands, the data type and the interleave type.
     * @param geoRef
     *            needed for the reader.
     * @param byteBuffer
     *            on which the memory reader will operate upon.
     * @param createCacheFile
     *            true if a cache file should back the data
     * @param cacheId
     *            the name of the cache file to use in the default {@link RasterCache}.
     * @param options
     *            which can be used to get the cache data from.
     * @return new RasterData
     */
    public static ByteBufferRasterData createRasterData( RasterRect view, RasterDataInfo rdi,
                                                         RasterGeoReference geoRef, ByteBuffer byteBuffer,
                                                         boolean createCacheFile, String cacheId,
                                                         RasterIOOptions options ) {
        RasterCache cache = RasterCache.getInstance( options );
        File cacheFile = null;
        if ( createCacheFile ) {
            cacheFile = cache.createCacheFile( cacheId );
        }
        CacheRasterReader inMem = new CacheRasterReader( byteBuffer, view.width, view.height, cacheFile,
                                                         createCacheFile, rdi, geoRef, cache );
        if ( createCacheFile ) {
            inMem = cache.addReader( inMem );
        }
        // in mem was added to the cache already.
        return createRasterData( view, rdi, inMem, false, options );
    }

    /**
     * Encapsulates a Grid of Raster data as a new Raster data object. This tiled raster data object cascades the pixel
     * operations for the underlying raster data grid.
     * 
     * @param reader
     *            to be used.
     * @param options
     *            holding information on the io settings.
     * @return a new TiledRasterData.
     */
    public static TiledRasterData createTiledRasterData( GridReader reader, RasterIOOptions options ) {
        return new TiledRasterData( reader, options );
    }

    /**
     * Constructor wrapper.
     * 
     * @param noData
     * @param bands
     * @param dataType
     * @param interleaveType
     * @return
     */
    private static RasterDataInfo createRDI( byte[] noData, BandType[] bands, DataType dataType,
                                             InterleaveType interleaveType ) {
        return new RasterDataInfo( noData, bands, dataType, interleaveType );
    }

}
