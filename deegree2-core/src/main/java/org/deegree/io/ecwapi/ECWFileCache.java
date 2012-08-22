//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2007 by:
 Planetek Italia s.r.l, Bari, Italia
 http://www.planetek.it

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 ---------------------------------------------------------------------------*/
package org.deegree.io.ecwapi;

import java.util.HashSet;
import java.util.Hashtable;

import com.ermapper.ecw.JNCSException;
import com.ermapper.ecw.JNCSFile;

/**
 * 
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:stutte@planetek.it">Jens Stutte</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class ECWFileCache {

    private static Hashtable<String, HashSet<ECWFile>> ECWNamedFileCache = new Hashtable<String, HashSet<ECWFile>>();

    private static HashSet<ECWFile> ECWAllFiles = new HashSet<ECWFile>();

    private static Hashtable<JNCSFile, ECWFile> ECWJNCSFileRef = new Hashtable<JNCSFile, ECWFile>();

    public static long EXPIRATION_PERIOD_MS = 600000;

    public static long MAX_NUM_OPEN = 10;

    // A private inner class that describes a cache entry
    private static class ECWFile {
        java.util.Date LastAccess;

        JNCSFile myFile;

        long lockingThread;

        ECWFile( String fileName ) throws JNCSException {
            myFile = new JNCSFile( fileName, false );
            LastAccess = new java.util.Date();
            ECWAllFiles.add( this );
            ECWJNCSFileRef.put( myFile, this );
            lockingThread = 0;
        }
    }

    // Retrieve the thread id.
    private static long getThreadId() {
        return Thread.currentThread().getId();
    }

    // Find the opened instances for a file name
    private static HashSet<ECWFile> findNamedFileSet( String fileName ) {
        HashSet<ECWFile> files = ECWNamedFileCache.get( fileName );
        if ( files == null ) {
            files = new HashSet<ECWFile>();
            ECWNamedFileCache.put( fileName, files );
        }
        return files;
    }

    // lock the file for the current thread, opens new instance
    // if nothing present in cache
    private static ECWFile lockFile( String fileName )
                            throws JNCSException {
        // Is there an unused instance in the cache?
        HashSet<ECWFile> files = findNamedFileSet( fileName );
        ECWFile[] fa = files.toArray( new ECWFile[files.size()] );
        for ( int i = 0; i < fa.length; i++ ) {
            if ( fa[i].lockingThread == 0 || fa[i].lockingThread == getThreadId() ) {
                fa[i].lockingThread = getThreadId();
                fa[i].LastAccess = new java.util.Date();
                return fa[i];
            }
        }
        // no: create new one
        ECWFile f = new ECWFile( fileName );
        files.add( f );
        f.lockingThread = getThreadId();

        return f;
    }

    // release file for use
    private static void unlockFile( ECWFile file ) {
        if ( file != null ) {
            file.lockingThread = 0;
        }
    }

    // close (and delete) file access instance
    private static void closeFile( ECWFile file ) {
        findNamedFileSet( file.myFile.fileName ).remove( file );
        ECWAllFiles.remove( file );
        ECWJNCSFileRef.remove( file.myFile );
        file.myFile.close( true );
    }

    // Find and close expired cache entries.
    private static void closeExpired() {
        synchronized ( ECWNamedFileCache ) {
            ECWFile oldest = null;
            int numOpen = 0;
            ECWFile[] fa = ECWAllFiles.toArray( new ECWFile[ECWAllFiles.size()] );
            java.util.Date now = new java.util.Date();
            for ( int i = 0; i < fa.length; i++ ) {
                if ( fa[i].lockingThread == 0 ) {
                    numOpen++;
                    if ( now.getTime() - fa[i].LastAccess.getTime() > EXPIRATION_PERIOD_MS ) {
                        closeFile( fa[i] );
                    } else if ( oldest == null || oldest.LastAccess.getTime() > fa[i].LastAccess.getTime() ) {
                        oldest = fa[i];
                    }
                }
            }
            if ( oldest != null && numOpen > MAX_NUM_OPEN ) {
                closeFile( oldest );
            }
        }
    }

    /**
     * Claim access to an ECW file.
     * <p>
     * If there is a non-expired instance in the cache, re-uses and locks this. Otherwise adds a new
     * one to the cache.
     * <p>
     * CAUTION: This cache DOES NOT HANDLE NESTED LOCKS/UNLOCKS ! This means, that after having
     * called claimAcces, you MUST call releaseFile before ANY OTHER OPERATION ON THE CACHE within
     * the same thread.
     * <p>
     * NOTE: There is no periodical cleanup of this cache. The expiration method is called only
     * during new calls to claimAccess/releaseFile. So server memory consumption may remain high for
     * a much longer time than the defined ExpirationPeriod.
     */
    public static JNCSFile claimAccess( String fileName )
                            throws JNCSException {
        synchronized ( ECWNamedFileCache ) {
            // close old instances
            closeExpired();
            ECWFile f = lockFile( fileName );
            return f.myFile;
        }
    }

    /**
     * Release access to an ECW file.
     * <p>
     * Unlocks the cache entry. Calls also the expiration method for cleanup of the instances that
     * exceed MaxNumOpen.
     */
    public static void releaseFile( JNCSFile myfile ) {
        synchronized ( ECWNamedFileCache ) {
            unlockFile( ECWJNCSFileRef.get( myfile ) );
            closeExpired();
        }
    }

    /**
     * Set expiration period
     * <p>
     * Set the time in milliseconds that a cache entry remains valid. Calls also the expiration
     * method for cleanup.
     * <p>
     * Default value: 600000
     */
    public static void setExpirationPeriod( long MilliSecs ) {
        EXPIRATION_PERIOD_MS = MilliSecs;
        closeExpired();
    }

    /**
     * Set maximum number of unused file instances
     * <p>
     * This parameter describes the maximum number of UNLOCKED entries in the cache (locked
     * instances are not counted). Calls also the expiration method for cleanup.
     * <p>
     * Default value: 10
     */
    public static void setMaxNumOpen( long MaxOpen ) {
        MAX_NUM_OPEN = MaxOpen;
        closeExpired();
    }
}
