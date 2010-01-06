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
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterDataFactory;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
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

    /** A key which can be given to the JVM to define the amount of memory used for caching. */
    public static final String DEF_RASTER_CACHE = "deegree.raster.cache";

    /**
     * Standard name for a deegree cache file.
     */
    public static final String FILE_EXTENSION = ".d3rcache";

    private static final String MEM_LOCK = "l";

    private final static Map<String, RasterCache> currentCaches = new ConcurrentHashMap<String, RasterCache>();

    private final File cacheDir;

    private static final long maxCacheMem;
    static {
        String cacheSize = System.getProperty( DEF_RASTER_CACHE );
        long mm = 0;
        if ( cacheSize != null ) {
            int byteConvert = 1;
            // split on no numbers.
            String[] split = cacheSize.split( "\\D" );
            // only the first split is of importance
            String bytes = split[0];
            if ( bytes.length() != cacheSize.length() ) {
                // some characters were used after the split.
                String unit = cacheSize.substring( bytes.length(), bytes.length() + 1 );
                if ( unit.equalsIgnoreCase( "k" ) ) {
                    byteConvert = 1024;
                } else if ( unit.equalsIgnoreCase( "m" ) ) {
                    byteConvert = 1024 * 1024;
                } else if ( unit.equalsIgnoreCase( "g" ) ) {
                    byteConvert = 1024 * 1024 * 1024;
                }
            }
            try {
                mm = Long.parseLong( bytes ) * byteConvert;
                LOG.info( "Using {} of memory for raster caching (because it was set with the {} property).",
                          ( mm * 1024 * 1024 ) + "Mb", DEF_RASTER_CACHE );
            } catch ( NumberFormatException e ) {
                LOG.warn(
                          "Ignoring supplied property: {} because it could not be parsed: {}. Using 0.5 of the total memory for raster caching.",
                          DEF_RASTER_CACHE, e.getLocalizedMessage() );
            }

        }
        if ( mm == 0 ) {
            if ( mm == Long.MAX_VALUE ) {
                mm = Math.round( Runtime.getRuntime().totalMemory() * 0.5 );
            } else {
                mm *= 0.5;
            }
        }
        maxCacheMem = mm;
    }

    private static long currentlyUsedMemory = 0;

    private final static ConcurrentSkipListSet<CacheRasterReader> cache = new ConcurrentSkipListSet<CacheRasterReader>(
                                                                                                                        new CacheComparator() );

    // private final static TreeSet<CacheRasterReader> cache = new TreeSet<CacheRasterReader>( new CacheComparator() );

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
                    cacheFile = createCacheFile( reader.getDataLocationId() );
                }
                result = new CacheRasterReader( reader, cacheFile, this );
            }
            if ( !cache.contains( result ) ) {
                synchronized ( MEM_LOCK ) {
                    currentlyUsedMemory += result.currentApproxMemory();
                }
                addReader( result );
            } else {
                LOG.debug( "Not adding reader ({}) to cache because it is already in the cache.", reader );
            }
        } else {
            LOG.debug( "Not adding reader to cache, because it is was null." );
        }
        if ( LOG.isDebugEnabled() && result != null && reader != null ) {
            LOG.debug( "Adding reader to cache {} with id: {}.", reader, reader.getDataLocationId() );
        }
        // System.out.println( "size: " + cache.size() );
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
            Iterator<CacheRasterReader> it = cache.iterator();
            while ( it != null && it.hasNext() ) {
                CacheRasterReader next = it.next();
                if ( next != null ) {
                    synchronized ( MEM_LOCK ) {
                        currentlyUsedMemory -= next.dispose( true );
                    }
                }
            }
        }
        LOG.debug( "After removing all readers used cache memory is calculated to be: {}", currentlyUsedMemory );
    }

    /**
     * Gets an instance of a data cache which uses the given directory as the cache directory.
     * 
     * @param directory
     * @param create
     *            true if the directory should be created if missing.
     * @return a raster cache for the given directory.
     */
    public synchronized static RasterCache getInstance( File directory, boolean create ) {
        File cacheDir = DEFAULT_CACHE_DIR;
        if ( directory != null ) {
            if ( !directory.exists() ) {
                if ( create ) {
                    LOG.warn( "Given cache directory: {} did not exist creating as requested.",
                              directory.getAbsolutePath() );
                    boolean creation = directory.mkdir();
                    if ( !creation ) {
                        LOG.warn(
                                  "Creation of cache directory: {} was not succesfull using default cache directory instead: {}.",
                                  directory.getAbsolutePath(), DEFAULT_CACHE_DIR.getAbsoluteFile() );
                    } else {
                        cacheDir = directory;
                    }
                } else {
                    LOG.warn(
                              "Given cache directory: {} does not exist and creation was not requested, using default cache dir: {}",
                              directory.getAbsolutePath(), DEFAULT_CACHE_DIR.getAbsolutePath() );
                }
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
    public synchronized static RasterCache getInstance( RasterIOOptions options ) {
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
    public synchronized static RasterCache getInstance() {
        return getInstance( DEFAULT_CACHE_DIR, false );
    }

    /**
     * Iterates over all known readers and (re) calculates their in memory data.
     */
    public static void updateCurrentlyUsedMemory() {
        LOG.debug( "Updating estimation of in-memory cache." );
        long cum = 0;
        Iterator<CacheRasterReader> it = cache.iterator();
        while ( it != null && it.hasNext() ) {
            CacheRasterReader next = it.next();
            if ( next != null ) {
                cum += next.currentApproxMemory();
            }
        }
        synchronized ( MEM_LOCK ) {
            LOG.debug( "Resetting currently used memory from:{} to:{}", ( currentlyUsedMemory / ( 1024 * 1024d ) ),
                       ( cum / ( 1024 * 1024d ) ) );
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
        // LOG.info( "Currently used cache memory:{} MB, totalCacheMemory:{} MB", currentlyUsedMemory / ( 1024d * 1024
        // ),
        // maxCacheMem / ( 1024d * 1024 ) );
        if ( currentlyUsedMemory + requiredMemory > maxCacheMem ) {
            disposeMemory( requiredMemory );
        }

        return currentlyUsedMemory;
    }

    private static void disposeMemory( long requiredMemory ) {
        synchronized ( MEM_LOCK ) {
            if ( currentlyUsedMemory + requiredMemory > maxCacheMem ) {
                Iterator<CacheRasterReader> it = cache.iterator();
                while ( it != null && it.hasNext() ) {
                    CacheRasterReader next = it.next();
                    if ( next != null ) {
                        currentlyUsedMemory -= next.dispose( false );
                    }
                    if ( currentlyUsedMemory + requiredMemory < maxCacheMem ) {
                        break;
                    }
                }
            }
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
     * Writes all current caches to their cache files, but leaves the in memory cached rasters alone.
     */
    public static void flush() {
        synchronized ( cache ) {
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
     * Tries to find the file with given id from the current cache directory and instantiates a cachedraster for it.
     * 
     * @param reader
     *            to be used for reading the original data, if parts of the cachefile are incoherent
     * @param rasterId
     *            the id of the raster if <code>null<code> no raster cache file will be created.
     * @return the raster created from the cache or <code>null</code> if no cache file with given id was found.
     */
    public SimpleRaster createFromCache( RasterReader reader, String rasterId ) {
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

                    result = new SimpleRaster( rasterData, data.getEnvelope(), data.getGeoReference() );
                }
            }
        }
        return result;
    }

    /**
     * @return the maximum allowed in memory cache size in bytes.
     */
    public static long getMaximumCacheMemory() {
        return maxCacheMem;
    }

    /**
     * Iterates over all current cache directories and calls dispose on their cache files.
     */
    public static void dispose() {
        // System.out.println( cache.size() + ") Cache: " + cache );
        // Set<CacheRasterReader> c = cache.descendingSet();
        // System.out.println( "Cache: " + c + ", size: " + c.size() );
        Iterator<CacheRasterReader> it = cache.iterator();
        long allocatedMem = 0;
        int i = 1;
        while ( it != null && it.hasNext() ) {
            CacheRasterReader next = it.next();
            if ( next != null ) {
                synchronized ( MEM_LOCK ) {
                    LOG.debug( "{}: Disposing for file: {}", i++, next.file() );
                    // currentlyUsedMemory -= next.dispose( false );
                    allocatedMem += next.dispose( false );
                }
            }
        }
        LOG.debug( "Disposing allocated {} MB on the heap.",
                   ( Math.round( ( allocatedMem / ( 1024 * 1024d ) ) * 100d ) / 100d ) );

    }

    /**
     * The <code>CacheComparator</code> class compares two raster readers for their last read access time.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author$
     * @version $Revision$, $Date$
     * 
     */
    static class CacheComparator implements Comparator<CacheRasterReader> {

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

}
