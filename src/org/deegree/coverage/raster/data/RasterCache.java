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

package org.deegree.coverage.raster.data;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.io.grid.CacheRasterReader;
import org.slf4j.Logger;

/**
 * The <code>RasterCache</code> holds references {@link CacheRasterReader} which wrap other RasterReaders. This Cache
 * can have multiple directories for storing cache files. If a new Raster should be created it is recommended to call
 * the {@link #freeMemory(long)} method first, so all data which was less recently used will be written to file.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class RasterCache {

    private static final Logger LOG = getLogger( RasterCache.class );

    /**
     * Default cache dir if no directory was given.
     */
    public static final File DEFAULT_CACHE_DIR = new File( System.getProperty( "java.io.tmpdir" ) );

    /**
     * Standard name for a deegree cache file.
     */
    public static final String FILE_EXTENSION = ".d3rcache";

    private static final String MEM_LOCK = "l";

    private final static Map<String, RasterCache> currentCaches = new ConcurrentHashMap<String, RasterCache>();

    private final File cacheDir;

    private static final long maxCacheMem;
    static {
        // rb: todo get this from some system variable.
        long mm = Runtime.getRuntime().maxMemory();
        if ( mm == Long.MAX_VALUE ) {
            mm = Math.round( Runtime.getRuntime().totalMemory() * 0.5 );
        } else {
            mm *= 0.5;
        }
        maxCacheMem = mm;
    }

    private static long currentlyUsedMemory = 0;

    private final static ConcurrentSkipListSet<CacheRasterReader> cache = new ConcurrentSkipListSet<CacheRasterReader>(
                                                                                                                        new CacheComparator() );

    private RasterCache( File cacheDir ) {
        this.cacheDir = cacheDir;
    }

    /**
     * Adds a raster reader to this cache, all cache files will be written to this cache directory.
     * 
     * @param reader
     *            to add to the cache.
     * @return a new CachedReader which was added to the cache.
     */
    public CacheRasterReader addReader( RasterReader reader ) {
        CacheRasterReader result = null;
        if ( reader != null ) {
            if ( reader instanceof CacheRasterReader ) {
                result = (CacheRasterReader) reader;
            } else {
                boolean createCache = reader.shouldCreateCacheFile();
                File cacheFile = null;
                if ( createCache ) {
                    File file = reader.file();
                    if ( file == null ) {
                        cacheFile = new File( cacheDir, UUID.randomUUID().toString() + FILE_EXTENSION );
                        cacheFile.deleteOnExit();
                    } else {
                        cacheFile = new File( cacheDir, FileUtils.getBasename( file ) + FILE_EXTENSION );
                    }
                }
                result = new CacheRasterReader( reader, cacheFile, this );
            }
            if ( !cache.contains( result ) ) {
                synchronized ( MEM_LOCK ) {
                    currentlyUsedMemory += result.currentApproxMemory();
                }
                addReader( result );
            }
        }
        return result;
    }

    private static void addReader( CacheRasterReader reader ) {
        synchronized ( cache ) {
            cache.add( reader );
        }
    }

    /**
     * Writes all data the files and removes all readers from the cache.
     */
    public static void clear() {
        synchronized ( cache ) {
            Iterator<CacheRasterReader> it = cache.descendingIterator();
            while ( it != null && it.hasNext() ) {
                CacheRasterReader next = it.next();
                if ( next != null ) {
                    synchronized ( MEM_LOCK ) {
                        currentlyUsedMemory -= next.dispose( true );
                    }
                }
            }
        }
        LOG.info( "After removing all readers used cache memory is calculated to be: {}", currentlyUsedMemory );
    }

    /**
     * Gets an instance of a data cache which uses the given directory as the cache directory.
     * 
     * @param directory
     * @return a raster cache for the given directory.
     */
    public synchronized static RasterCache getInstance( File directory ) {
        File cacheDir = DEFAULT_CACHE_DIR;
        if ( directory != null ) {
            if ( !directory.exists() ) {
                LOG.warn( "Given cache directory {} does not exist, using default cache dir{}",
                          directory.getAbsolutePath(), DEFAULT_CACHE_DIR.getAbsolutePath() );
            } else {
                cacheDir = directory;
            }
        }
        synchronized ( currentCaches ) {
            if ( !currentCaches.containsKey( cacheDir.getAbsolutePath() ) ) {
                currentCaches.put( cacheDir.getAbsolutePath(), new RasterCache( cacheDir ) );
            }
        }

        return currentCaches.get( cacheDir.getAbsolutePath() );
    }

    /**
     * Iterates over all known readers and (re) calculates their in memory data.
     */
    public static void updateCurrentlyUsedMemory() {
        long cum = 0;
        Iterator<CacheRasterReader> it = cache.descendingIterator();
        while ( it != null && it.hasNext() ) {
            CacheRasterReader next = it.next();
            if ( next != null ) {
                cum += next.currentApproxMemory();
            }
        }
        synchronized ( MEM_LOCK ) {
            LOG.info( "Resetting currently used memory to:{}", cum );
            currentlyUsedMemory = cum;
        }
    }

    /**
     * Signals the cache to write as much data to cache files so that the memory occupied by rasters can be returned to
     * running process. Note this method does not actually write the cache files, it merely signals the
     * {@link CacheRasterReader}s to write their data to file if they have a file to write to. It may well be that the
     * required memory can not be freed.
     * 
     * @param requiredMemory
     *            some process may need.
     * @return the amount of currently used cache memory, which is only an approximation.
     */
    public static long freeMemory( long requiredMemory ) {
        LOG.info( "Currently used cache memory:{} MB, totalCacheMemory:{} MB", currentlyUsedMemory / ( 1024d * 1024 ),
                  maxCacheMem / ( 1024d * 1024 ) );
        if ( currentlyUsedMemory + requiredMemory > maxCacheMem ) {
            Iterator<CacheRasterReader> it = cache.descendingIterator();
            while ( it != null && it.hasNext() ) {
                CacheRasterReader next = it.next();
                if ( next != null ) {
                    synchronized ( MEM_LOCK ) {
                        currentlyUsedMemory -= next.dispose( false );
                    }
                }
                if ( currentlyUsedMemory + requiredMemory < maxCacheMem ) {
                    break;
                }

            }
        }
        return currentlyUsedMemory;
    }

    /**
     * The <code>CacheComparator</code> class TODO add class documentation here.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author$
     * @version $Revision$, $Date$
     * 
     */
    public static class CacheComparator implements Comparator<CacheRasterReader> {

        @Override
        public int compare( CacheRasterReader o1, CacheRasterReader o2 ) {
            if ( o1 == null ) {
                return o2 == null ? 0 : -1;
            }
            if ( o2 == null ) {
                return 1;
            }
            return o1.lastReadAccess() < o2.lastReadAccess() ? -1 : o1.lastReadAccess() == o2.lastReadAccess() ? 0 : 1;
        }
    }

    /**
     * Creates a unique cachefile for the given id, if the id already exists in the cache directory an index will be
     * appended. if the given id is <code>null</code> a uuid will be used, this file will be marked to be deleted on
     * exit.
     * 
     * @param id
     *            to be used for the identification for the cache file.
     * @return a unique file name based on the given id.
     */
    public final File createCacheFile( String id ) {
        String fileName = id;
        boolean createNew = false;
        if ( fileName == null ) {
            fileName = UUID.randomUUID().toString();
        }
        File f = new File( this.cacheDir, fileName + FILE_EXTENSION );
        int index = 0;
        while ( createNew && f.exists() ) {
            f = new File( this.cacheDir, id + "_" + ( index++ ) + FILE_EXTENSION );
        }
        if ( id == null ) {
            f.deleteOnExit();
        }
        return f;
    }

    /**
     * Writes all current caches to their cache files, but leaves the in memory files alone.
     */
    public static void flush() {
        synchronized ( cache ) {
            Iterator<CacheRasterReader> it = cache.descendingIterator();
            while ( it != null && it.hasNext() ) {
                CacheRasterReader next = it.next();
                if ( next != null ) {
                    next.flush();
                }
            }
        }

    }

    /**
     * @param reader
     * @param filename
     * @return
     */
    public SimpleRaster createFromCache( RasterReader reader, String filename ) {
        SimpleRaster result = null;
        if ( filename != null ) {
            File cacheFile = new File( this.cacheDir, filename + FILE_EXTENSION );
            if ( cacheFile.exists() ) {
                CacheRasterReader data = CacheRasterReader.createFromCache( reader, cacheFile, this );
                if ( data != null ) {
                    ByteBufferRasterData rasterData = RasterDataFactory.createRasterData( data.getWidth(),
                                                                                          data.getHeight(),
                                                                                          data.getRasterDataInfo(),
                                                                                          data, true );

                    result = new SimpleRaster( rasterData, data.getEnvelope(), data.getGeoReference() );
                }
            }
        }
        return result;
    }
}
