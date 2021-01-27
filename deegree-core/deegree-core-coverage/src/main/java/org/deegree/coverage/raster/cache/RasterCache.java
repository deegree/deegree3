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

package org.deegree.coverage.raster.cache;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.deegree.commons.utils.FileUtils;
import org.deegree.commons.utils.StringUtils;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterDataFactory;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.slf4j.Logger;

/**
 * The <code>RasterCache</code> holds references {@link CacheRasterReader} which wrap other RasterReaders. This Cache
 * can have multiple directories for storing cache files. The cache is kept up-to-date because the RasterReaders
 * allocate cached memory by using {@link ByteBufferPool#allocate(int, boolean, boolean)}. This will call
 * {@link #freeMemory(long)} which in turn keeps track of reserved memory.
 * <p>
 * The RasterCache memory size and the amount of disk space can be set by adding the keys
 * {@link #DEF_RASTER_CACHE_MEM_SIZE} and {@link #DEF_RASTER_CACHE_DISK_SIZE} to the JVM.
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

    /** A key which can be given to the JVM to define the amount of memory used for caching (e.g. 1024m). */
    public static final String DEF_RASTER_CACHE_MEM_SIZE = "deegree.raster.cache.memsize";

    /** A key which can be given to the JVM to define the amount of disk memory used for caching (e.g. 1024m). */
    public static final String DEF_RASTER_CACHE_DISK_SIZE = "deegree.raster.cache.disksize";

    private static long maxCacheMem;

    private static long maxCacheDisk;

    /**
     * Standard name for a deegree cache file.
     */
    public static final String FILE_EXTENSION = ".d3rcache";

    private static final Object MEM_LOCK = new Object();

    private static final Object CURRENT_CACHE_LOCK = new Object();

    private final static Map<String, RasterCache> currentCaches = new ConcurrentHashMap<String, RasterCache>();

    private final File cacheDir;

    private static long currentlyUsedMemory = 0;

    private static long currentlyUsedDisk = 0;

    private final static ConcurrentSkipListSet<CacheRasterReader> cache = new ConcurrentSkipListSet<CacheRasterReader>(
                                                                                                                        new CacheComparator() );

    private final static Map<String, String> uniqueRasterCacheIds = new HashMap<String, String>();

    static {
        evaluateProperties();
    }

    /**
     * (Re-)Evaluate the {@link RasterCache#DEF_RASTER_CACHE_DISK_SIZE} and {@link #DEF_RASTER_CACHE_MEM_SIZE}
     * properties in the JVM.
     */
    private static void evaluateProperties() {
        synchronized ( MEM_LOCK ) {
            String cacheSize = System.getProperty( DEF_RASTER_CACHE_MEM_SIZE );
            long mm = StringUtils.parseByteSize( cacheSize );
            if ( mm == 0 ) {
                if ( StringUtils.isSet( cacheSize ) ) {
                    LOG.warn( "Ignoring supplied property: {} because it could not be parsed. Using 0.5 of the total memory for raster caching.",
                              DEF_RASTER_CACHE_MEM_SIZE );
                }
                mm = Runtime.getRuntime().maxMemory();
                if ( mm == Long.MAX_VALUE ) {
                    mm = Math.round( Runtime.getRuntime().totalMemory() * 0.5 );
                } else {
                    mm *= 0.5;
                }
            } else {
                LOG.info( "Using {} of memory for raster caching (because it was set with the {} property).",
                          ( mm / ( 1024 * 1024 ) ) + "Mb", DEF_RASTER_CACHE_MEM_SIZE );
            }
            maxCacheMem = mm;
            String t = System.getProperty( DEF_RASTER_CACHE_DISK_SIZE );
            mm = StringUtils.parseByteSize( t );
            if ( mm == 0 ) {
                if ( StringUtils.isSet( t ) ) {
                    LOG.warn( "Ignoring supplied property: {} because it could not be parsed. Using 20G of disk space for raster caching.",
                              DEF_RASTER_CACHE_MEM_SIZE );
                }
                mm = 20 * ( 1024l * 1024 * 1024 );
            } else {
                LOG.info( "Using {} of disk space for raster caching (because it was set with the {} property).",
                          ( mm / ( 1024 * 1024 ) ) + "Mb", DEF_RASTER_CACHE_DISK_SIZE );
            }
            maxCacheDisk = mm;
        }
    }

    // private final static TreeSet<CacheRasterReader> cache = new TreeSet<CacheRasterReader>( new CacheComparator() );

    private RasterCache( File cacheDir ) {
        this.cacheDir = cacheDir;
    }

    /**
     * Clear the cache of all readers (and optionally delete all cache files) and reevaluate the disk and memory size (
     * {@link #DEF_RASTER_CACHE_DISK_SIZE}, {@link #DEF_RASTER_CACHE_MEM_SIZE}) properties from the system. This method
     * is to be called with care.
     * 
     * @param deleteCachedFile
     *            true if all cached files should be deleted.
     */
    public static void reset( boolean deleteCachedFile ) {
        clear( deleteCachedFile );
        evaluateProperties();
    }

    /**
     * Gets an instance of a data cache which uses the given directory as the cache directory.
     * 
     * @param directory
     * @param create
     *            true if the directory should be created if missing.
     * @return a raster cache for the given directory.
     */
    public static RasterCache getInstance( File directory, boolean create ) {
        File cacheDir = DEFAULT_CACHE_DIR;
        if ( directory != null ) {
            if ( !directory.exists() ) {
                if ( create ) {
                    LOG.warn( "Given cache directory: {} did not exist creating as requested.",
                              directory.getAbsolutePath() );
                    boolean creation = directory.mkdir();
                    if ( !creation ) {
                        LOG.warn( "Creation of cache directory: {} was not succesfull using default cache directory instead: {}.",
                                  directory.getAbsolutePath(), DEFAULT_CACHE_DIR.getAbsoluteFile() );
                    } else {
                        cacheDir = directory;
                    }
                } else {
                    LOG.warn( "Given cache directory: {} does not exist and creation was not requested, using default cache dir: {}",
                              directory.getAbsolutePath(), DEFAULT_CACHE_DIR.getAbsolutePath() );
                }
            } else {
                cacheDir = directory;
            }
        }
        synchronized ( CURRENT_CACHE_LOCK ) {
            if ( !currentCaches.containsKey( cacheDir.getAbsolutePath() ) ) {
                currentCaches.put( cacheDir.getAbsolutePath(), new RasterCache( cacheDir ) );
            }
        }

        return currentCaches.get( cacheDir.getAbsolutePath() );
    }

    /**
     * Gets an instance of a data cache which uses the given options to instantiate a cache. Currently following
     * {@link RasterIOOptions} keys are evaluated. If the options are <code>null</code> the {@link #DEFAULT_CACHE_DIR}
     * will be used.
     * <ul>
     * <li>{@link RasterIOOptions#RASTER_CACHE_DIR}</li>
     * <li>{@link RasterIOOptions#LOCAL_RASTER_CACHE_DIR}</li>
     * <li>{@link RasterIOOptions#CREATE_RASTER_MISSING_CACHE_DIR}</li>
     * </ul>
     * 
     * @param options
     *            which can contain cache information.
     * @return a raster cache for the given directory.
     */
    public static RasterCache getInstance( RasterIOOptions options ) {
        File directory = DEFAULT_CACHE_DIR;
        boolean create = false;
        if ( options != null ) {
            String topLevelDir = options.get( RasterIOOptions.RASTER_CACHE_DIR );
            if ( topLevelDir != null ) {
                directory = new File( topLevelDir );
            }
            String dir = options.get( RasterIOOptions.LOCAL_RASTER_CACHE_DIR );
            if ( dir != null ) {
                directory = new File( directory, dir );
            }
            create = options.get( RasterIOOptions.CREATE_RASTER_MISSING_CACHE_DIR ) != null;
        }
        return getInstance( directory, create );
    }

    /**
     * Gets an instance of a data cache which uses the default directory as the cache directory.
     * 
     * @return a raster cache for the default directory.
     */
    public static RasterCache getInstance() {
        return getInstance( DEFAULT_CACHE_DIR, false );
    }

    /**
     * @return the currentlyUsedMemory
     */
    public static final long getCurrentlyUsedMemory() {
        return currentlyUsedMemory;
    }

    /**
     * @return the currentlyUsedDisk
     */
    public static final long getCurrentlyUsedDisk() {
        return currentlyUsedDisk;
    }

    /**
     * @return the number of cached readers.
     */
    public static int size() {
        return cache.size();
    }

    /**
     * Clears all Memory buffers from all known cached readers, removes the cache files (if requested) and removes all
     * readers from the cache. Note, all information on the currently rasters is lost.
     * 
     * @param deleteCacheFiles
     *            true if the currently used files should be removed from cache.
     * 
     */
    public static void clear( boolean deleteCacheFiles ) {
        synchronized ( MEM_LOCK ) {
            Iterator<CacheRasterReader> it = cache.iterator();
            while ( it != null && it.hasNext() ) {
                CacheRasterReader next = it.next();
                if ( next != null ) {
                    next.clear( deleteCacheFiles );
                }
            }
            cache.clear();
            currentlyUsedMemory = 0;
            currentlyUsedDisk = 0;
        }
        synchronized ( CURRENT_CACHE_LOCK ) {
            if ( deleteCacheFiles && currentCaches.values() != null ) {
                for ( RasterCache rc : currentCaches.values() ) {
                    File f = rc.cacheDir;
                    if ( f != null && f.exists() && !DEFAULT_CACHE_DIR.equals( f ) ) {
                        if ( f.isDirectory() ) {
                            try {
                                boolean deleted = f.delete();
                                if ( !deleted ) {
                                    LOG.warn( "Could not delete raster cache dir: " + f.getAbsolutePath()
                                              + " please delete manually." );
                                }
                            } catch ( Exception e ) {
                                LOG.warn( "Could not delete raster cache dir: " + f.getAbsolutePath()
                                          + " please delete manually." );
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Writes all current caches to their cache files, but leaves the in memory cached rasters alone.
     */
    public static void flush() {
        synchronized ( MEM_LOCK ) {
            Iterator<CacheRasterReader> it = cache.iterator();
            while ( it != null && it.hasNext() ) {
                CacheRasterReader next = it.next();
                if ( next != null ) {
                    next.flush();
                }
            }
        }
    }

    /**
     * Iterates over all current cache directories and calls dispose on their cache files.
     */
    public static void dispose() {
        synchronized ( MEM_LOCK ) {
            Iterator<CacheRasterReader> it = cache.iterator();
            long allocatedMem = 0;
            int i = 1;
            while ( it != null && it.hasNext() ) {
                CacheRasterReader next = it.next();
                if ( next != null ) {
                    LOG.debug( "{}: Disposing for file: {}", i++, next.file() );
                    // currentlyUsedMemory -= next.dispose( false );
                    allocatedMem += next.dispose( false );
                }
            }
            LOG.debug( "Disposing allocated {} MB on the heap.",
                       ( Math.round( ( allocatedMem / ( 1024 * 1024d ) ) * 100d ) / 100d ) );
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
        synchronized ( MEM_LOCK ) {
            LOG.debug( "Currently used cache memory:{} MB, totalCacheMemory:{} MB", currentlyUsedMemory
                                                                                    / ( 1024d * 1024 ),
                       maxCacheMem / ( 1024d * 1024 ) );
            if ( currentlyUsedMemory + requiredMemory > maxCacheMem ) {
                disposeMemory( requiredMemory );
            }
            currentlyUsedMemory += requiredMemory;
        }

        return currentlyUsedMemory;
    }

    private static void disposeMemory( long requiredMemory ) {
        synchronized ( MEM_LOCK ) {
            if ( currentlyUsedMemory + requiredMemory > maxCacheMem ) {
                // make a copy, to reflect the last read access.
                SortedSet<CacheRasterReader> sort = new ConcurrentSkipListSet<CacheRasterReader>( cache );
                Iterator<CacheRasterReader> it = sort.iterator();
                if ( it != null ) {
                    final double halfMem = maxCacheMem * 0.5;
                    int readersConsidered = 0;
                    while ( it.hasNext() ) {
                        CacheRasterReader next = it.next();
                        if ( next != null ) {
                            ++readersConsidered;
                            long onDisk = next.cacheFileSize();
                            if ( currentlyUsedDisk > maxCacheDisk && onDisk > 0 ) {
                                // just delete the cache file and any in memory data from the least recently used cache
                                // file.
                                currentlyUsedMemory -= next.clear( true );
                                currentlyUsedDisk -= onDisk;
                            } else {
                                long mem = next.currentApproxMemory();
                                if ( mem > 0 && next.canCreateCacheFile() ) {
                                    currentlyUsedMemory -= next.dispose( false );
                                    // add the new cache file size
                                    currentlyUsedDisk += ( next.cacheFileSize() - onDisk );
                                } else if ( mem > 0 ) {
                                    currentlyUsedMemory -= next.dispose( true );
                                }
                            }
                        }
                        if ( currentlyUsedMemory + requiredMemory < halfMem ) {
                            break;
                        }
                    }

                    if ( currentlyUsedMemory > halfMem || readersConsidered > ( cache.size() * 0.5 ) ) {
                        // disposed more than half of the readers or could not get required memory..., update the real
                        // memory, it might well be the current values are invalid.
                        updateCurrentlyUsedSpace();
                    }
                }
            }
        }
    }

    /**
     * Iterates over all known readers and (re) calculates their in memory data.
     */
    private static void updateCurrentlyUsedSpace() {
        synchronized ( MEM_LOCK ) {
            LOG.debug( "Updating estimation of in-memory cache." );
            long cum = 0;
            long onDisk = 0;
            Iterator<CacheRasterReader> it = cache.iterator();
            while ( it != null && it.hasNext() ) {
                CacheRasterReader next = it.next();
                if ( next != null ) {
                    cum += next.currentApproxMemory();
                    onDisk += next.cacheFileSize();
                }
            }
            LOG.debug( "Resetting currently used memory from: {} to: {}", ( currentlyUsedMemory / ( 1024 * 1024d ) ),
                       ( cum / ( 1024 * 1024d ) ) );

            LOG.debug( "Resetting currently used space on disk from: {} to: {}",
                       ( currentlyUsedDisk / ( 1024 * 1024d ) ), ( onDisk / ( 1024 * 1024d ) ) );
            currentlyUsedMemory = cum;
            currentlyUsedDisk = onDisk;
        }
    }

    /**
     * Adds the reader to the global map
     * 
     * @param reader
     */
    private static void addReader( CacheRasterReader reader ) {
        boolean added = false;
        synchronized ( MEM_LOCK ) {
            currentlyUsedMemory += reader.currentApproxMemory();
            currentlyUsedDisk += reader.cacheFileSize();
            added = cache.add( reader );
        }
        if ( !added ) {
            LOG.debug( "Not adding reader ({}) to cache because it is already in the cache.", reader );
        }
    }

    /**
     * Adds a raster reader to this cache, all cache files will be written to this cache directory.
     * 
     * @param reader
     *            to add to the cache.
     * @return a new CachedReader which was added to the cache.
     */
    public RasterReader addReader( RasterReader reader ) {
        CacheRasterReader result = null;
        if ( reader != null ) {
            if ( reader instanceof CacheRasterReader ) {
                result = (CacheRasterReader) reader;
            } else {
                boolean createCache = reader.shouldCreateCacheFile();
                File cacheFile = null;
                if ( createCache ) {
                    LOG.trace( "create cachefile for location {}", reader.getDataLocationId() );
                    cacheFile = createCacheFile( reader.getDataLocationId() );
                }
                result = new CacheRasterReader( reader, cacheFile, this );
            }
            addReader( result );
        } else {
            LOG.debug( "Not adding reader to cache, because it is was null." );
        }
        if ( LOG.isDebugEnabled() && result != null && reader != null ) {
            LOG.debug( "Adding reader to cache {} with id: {}.", reader, reader.getDataLocationId() );
        }
        return result;
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
        // rb: currently always use old file, don't try to create a new one.
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
            // if the id was null, delete the file on exit.
            f.deleteOnExit();
        }
        return f;
    }

    /**
     * Tries to find the file with given id from the current cache directory and instantiates a cachedraster for it.
     * 
     * @param reader
     *            to be used for reading the original data, if parts of the cachefile are incoherent
     * @param rasterId
     *            the id of the raster if <code>null<code> no raster cache file will be created.
     * @return the raster created from the cache or <code>null</code> if no cache file with given id was found.
     */
    public SimpleRaster createFromCache( RasterReader reader, String rasterId ) {
        // rb: todo what about the CRS from RasterIOOptions
        SimpleRaster result = null;
        if ( rasterId != null ) {
            File cacheFile = new File( this.cacheDir, rasterId + FILE_EXTENSION );
            if ( cacheFile.exists() ) {
                CacheRasterReader data = CacheRasterReader.createFromCache( reader, cacheFile, this );
                if ( data != null ) {
                    ByteBufferRasterData rasterData = RasterDataFactory.createRasterData( data.getWidth(),
                                                                                          data.getHeight(),
                                                                                          data.getRasterDataInfo(),
                                                                                          data, true );

                    result = new SimpleRaster( rasterData, data.getEnvelope(), data.getGeoReference(), null );
                }
            }
        }
        return result;
    }

    /**
     * The <code>CacheComparator</code> class compares two raster readers for their last read access time.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author$
     * @version $Revision$, $Date$
     * 
     */
    static class CacheComparator implements Comparator<CacheRasterReader>, Serializable {

        /**
         * just for the Serializable
         */
        private static final long serialVersionUID = 957737023397188332L;

        @Override
        public int compare( CacheRasterReader o1, CacheRasterReader o2 ) {
            // rb: returning 0 will cause the above 'add' method to discard the new raster.
            if ( o1 == null ) {
                return -1;
            }
            if ( o2 == null ) {
                return 1;
            }
            int result = o1.lastReadAccess() <= o2.lastReadAccess() ? -1 : 1;
            return result;
        }
    }

    /**
     * @return the directory used for caching.
     */
    public File getCacheDirectory() {
        return cacheDir;
    }

    public static void disableAllCaches() {
        maxCacheMem = 0;
        maxCacheDisk = 0;
    }

    /**
     * Generate unique (in the scope of RasterCache) short filename for raster reader identification
     * 
     * @param file
     *            file reference to start with
     * @return unique filename (may contains prefix to make it unique)
     */
    public static synchronized String getUniqueCacheIdentifier( File file ) {
        String fname = FileUtils.getFilename( file );
        String apath = file.getAbsolutePath();

        int idx = 0;
        String key = fname;

        while ( !apath.equals( uniqueRasterCacheIds.getOrDefault( key, apath ) ) ) {
            idx++;
            key = idx + "_" + fname;
        }
        uniqueRasterCacheIds.put( key, apath );
        return key;
    }
    
    /**
     * Helper function to check if a .no-cache or .no-cache-[level] file exists
     */
    public static boolean hasNoCacheFile( File file, int level ) {
        try {
            if ( file == null || !file.exists() )
                return false;

            File all = new File( file.getParentFile(), file.getName() + ".no-cache" );
            if ( all.exists() ) {
                return true;
            }

            File lvl = new File( file.getParentFile(), file.getName() + ".no-cache-" + level );
            return lvl.exists();
        } catch ( Exception ex ) {
            LOG.debug( "Failed to check for .no-cache files for {} level {}", file, level );
            LOG.debug( "Got exception", ex );
            return false;
        }
    }
}
