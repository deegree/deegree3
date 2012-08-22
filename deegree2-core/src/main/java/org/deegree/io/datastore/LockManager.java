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
package org.deegree.io.datastore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.ogcwebservices.wfs.operation.LockFeature;

/**
 * Keeps track of all persistent features that are in a locked state, i.e. a {@link LockFeature} request has been issued
 * to lock them.
 * <p>
 * Locked features cannot be updated or deleted except by transactions that specify the appropriate lock identifier.
 * <p>
 * The <code>LockManager</code> also ensures that active locks survive a restart of the VM - therefore it keeps
 * serialized and up-to-date versions of all active {@link Lock} instances in a temporary directory. The directory is
 * specified by the <code>java.io.tmpdir</code> system property. On first initialization, i.e. the first call to
 * {@link #getInstance()}, the directory is scanned for all files matching the pattern <code>deegree-lock*.tmp</code>,
 * and these are deserialized to rebuild the <code>LockManager</code>'s status.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class LockManager {

    private static final ILogger LOG = LoggerFactory.getLogger( LockManager.class );

    private static String FILE_PREFIX = "deegree-lock";

    private static String FILE_SUFFIX = ".tmp";

    private static LockManager instance;

    private static File workingDir;

    // maps expiry time to Lock, first key is the one that will timeout first
    private TreeMap<Long, Lock> expiryTimeToLock = new TreeMap<Long, Lock>();

    private Map<String, Lock> lockIdToLock = new HashMap<String, Lock>();

    private Map<String, Lock> fidToLock = new HashMap<String, Lock>();

    private Map<Lock, File> lockToFile = new HashMap<Lock, File>();

    private LockManager( File workingDir ) throws DatastoreException {
        if ( workingDir == null ) {
            String msg = "No working directory for the lock manager specified. Using default temp directory: '"
                         + System.getProperty( "java.io.tmpdir" ) + "'";
            LOG.logInfo( msg );
            workingDir = new File( System.getProperty( "java.io.tmpdir" ) );
        }

        LockManager.workingDir = workingDir;
        if ( !workingDir.isDirectory() ) {
            String msg = "Specified working directory for the lock manager '" + workingDir
                         + "' does not denote a directory.";
            throw new DatastoreException( msg );
        }
        if ( !workingDir.canWrite() ) {
            String msg = "Cannot write to the lock manager's working directory ('" + workingDir + "' ).";
            throw new DatastoreException( msg );
        }

        String msg = "Lock manager will use directory '" + workingDir + "' to persist it's locks.";
        LOG.logInfo( msg );

        restoreLocks();
        checkForExpiredLocks();
    }

    private void restoreLocks() {

        // get all persistent locks from temporary directory
        String[] fileNames = workingDir.list( new FilenameFilter() {
            @SuppressWarnings("synthetic-access")
            public boolean accept( File dir, String name ) {
                return name.startsWith( FILE_PREFIX ) && name.endsWith( FILE_SUFFIX );
            }
        } );

        if ( fileNames != null ) {
            String msg = Messages.getMessage( "DATASTORE_LOCK_RESTORING", fileNames.length, workingDir );
            LOG.logInfo( msg );
            for ( String fileName : fileNames ) {
                File file = new File( workingDir + File.separator + fileName );
                try {
                    FileInputStream fis = null;
                    ObjectInputStream ois = null;
                    try {
                        fis = new FileInputStream( file );
                        ois = new ObjectInputStream( fis );
                        Lock lock = (Lock) ois.readObject();
                        registerLock( lock, file );
                    } finally {
                        if ( ois != null ) {
                            ois.close();
                        }
                    }
                } catch ( Exception e ) {
                    msg = Messages.getMessage( "DATASTORE_LOCK_RESTORE_FAILED", fileName );
                    LOG.logError( msg, e );
                }
            }
        }
    }

    /**
     * Initializes the <code>LockManager</code>.
     *
     * @param workingDir
     *            directory where the <code>LockManager</code> will persists its locks
     * @throws DatastoreException
     */
    public static synchronized void initialize( File workingDir )
                            throws DatastoreException {
        if ( instance != null ) {
            String msg = "LockManager has already been initialized.";
            throw new DatastoreException( msg );
        }
        instance = new LockManager( workingDir );
    }

    /**
     * Returns the only instance of <code>LockManager</code>.
     *
     * @return the only instance of <code>LockManager</code>
     */
    public static synchronized LockManager getInstance() {
        if ( instance == null ) {
            String msg = "LockManager has not been initialized yet.";
            throw new RuntimeException( msg );
        }
        return instance;
    }

    /**
     * Returns whether the specified feature is locked.
     *
     * @param fid
     *            id of the feature
     * @return true, if the specified feature is locked, false otherwise
     */
    public boolean isLocked( FeatureId fid ) {
        return getLockId( fid ) != null;
    }

    /**
     * Returns the id of the lock that locks the specified feature (if it is locked).
     *
     * @param fid
     *            id of the feature
     * @return the lock id, or null if it is not locked
     */
    public String getLockId( FeatureId fid ) {

        checkForExpiredLocks();

        String lockId = null;
        synchronized ( this ) {
            Lock lock = this.fidToLock.get( fid.getAsString() );
            if ( lock != null ) {
                lockId = lock.getId();
            }
        }
        return lockId;
    }

    /**
     * Acquires a lock for the given {@link LockFeature} request. The affected feature instances and their descendant
     * features + super features have to be specified as well.
     * <p>
     * If the lockAction in the request is set to ALL and not all requested features could be locked, a
     * {@link DatastoreException} will be thrown.
     * <p>
     * If no features have been locked at all, a lock will be issued, but the lock is not registered (as requested by
     * the WFS spec.).
     *
     * @param request
     *            <code>LockFeature</code> request
     * @param fidsToLock
     *            all feature instances that are affected by the request
     * @return the acquired lock, never null
     * @throws DatastoreException
     */
    public Lock acquireLock( LockFeature request, List<FeatureId> fidsToLock )
                            throws DatastoreException {

        checkForExpiredLocks();

        Lock lock = null;

        synchronized ( this ) {
            String lockId = UUID.randomUUID().toString();

            Set<String> lockableFids = new TreeSet<String>();
            List<String> notLockableFids = new ArrayList<String>( fidsToLock.size() );

            for ( FeatureId fid : fidsToLock ) {
                String fidAsString = fid.getAsString();
                if ( this.fidToLock.get( fidAsString ) != null ) {
                    notLockableFids.add( fidAsString );
                } else {
                    lockableFids.add( fidAsString );
                }
            }

            if ( request.lockAllFeatures() && !notLockableFids.isEmpty() ) {
                StringBuffer sb = new StringBuffer();
                for ( int i = 0; i < notLockableFids.size(); i++ ) {
                    sb.append( notLockableFids.get( i ) );
                    if ( i != notLockableFids.size() - 1 ) {
                        sb.append( ", " );
                    }
                }
                String msg = Messages.getMessage( "DATASTORE_LOCK_SOME_HELD", sb );
                throw new DatastoreException( msg );
            }

            if ( !lockableFids.isEmpty() ) {
                long duration = request.getExpiry();
                long expiryTime = System.currentTimeMillis() + duration;
                lock = new Lock( lockId, lockableFids, expiryTime );
                File file = persistLock( lock );
                registerLock( lock, file );
            } else {
                lock = new Lock( lockId, lockableFids, System.currentTimeMillis() );
                String msg = Messages.getMessage( "DATASTORE_EMPTY_LOCK", lockId );
                LOG.logInfo( msg );
            }
        }
        return lock;
    }

    /**
     * Releases the specified lock completely (all associated features are unlocked) and removes it (also from the
     * temporary directory).
     *
     * @param lockId
     *            lock identifier
     * @throws DatastoreException
     */
    public void releaseLock( String lockId )
                            throws DatastoreException {
        synchronized ( this ) {
            Lock lock = this.lockIdToLock.get( lockId );
            if ( lock == null ) {
                String msg = Messages.getMessage( "DATASTORE_UNKNOWN_LOCK", lockId );
                throw new DatastoreException( msg );
            }
            releaseLock( lock );
        }
    }

    /**
     * Releases the given lock completely (all associated features are unlocked) and removes it (also from the temporary
     * directory).
     *
     * @param lock
     *            lock to be released
     */
    public void releaseLock( Lock lock ) {
        synchronized ( this ) {
            this.lockIdToLock.remove( lock.getId() );
            this.expiryTimeToLock.remove( lock.getExpiryTime() );
            Set<String> lockedFids = lock.getLockedFids();
            for ( String fid : lockedFids ) {
                this.fidToLock.remove( fid );
            }
            File file = this.lockToFile.get( lock );
            file.delete();
            this.lockToFile.remove( lock );
        }
    }

    /**
     * Releases the specified lock partly (all specified features are unlocked).
     * <p>
     * If there are no more features associated with the lock, the lock is removed.
     *
     * @param lockId
     *            lock identifier
     * @param unlockFids
     *            features to be unlocked
     * @throws DatastoreException
     */
    public void releaseLockPartly( String lockId, Set<FeatureId> unlockFids )
                            throws DatastoreException {

        synchronized ( this ) {
            Lock lock = this.lockIdToLock.get( lockId );
            if ( lock == null ) {
                String msg = Messages.getMessage( "DATASTORE_UNKNOWN_LOCK", lockId );
                throw new DatastoreException( msg );
            }

            Set<String> lockedFeatures = lock.getLockedFids();

            for ( FeatureId fid : unlockFids ) {
                String fidAsString = fid.getAsString();
                this.fidToLock.remove( fidAsString );
                lockedFeatures.remove( fidAsString );
            }

            if ( lockedFeatures.isEmpty() ) {
                String msg = Messages.getMessage( "DATASTORE_LOCK_CLEARED", lock.getId() );
                LOG.logInfo( msg );
                this.lockIdToLock.remove( lockId );
                this.expiryTimeToLock.remove( lock.getExpiryTime() );
            }
            persistLock( lock );
        }
    }

    /**
     * Checks for expired locks and releases them.
     */
    private void checkForExpiredLocks() {
        synchronized ( this ) {
            while ( !this.expiryTimeToLock.isEmpty() ) {
                long expiry = this.expiryTimeToLock.firstKey();
                if ( expiry > System.currentTimeMillis() ) {
                    break;
                }
                Lock lock = this.expiryTimeToLock.get( expiry );
                String msg = Messages.getMessage( "DATASTORE_LOCK_EXPIRED", lock.getId(),
                                                  new Date( lock.getExpiryTime() ) );
                LOG.logInfo( msg );
                releaseLock( lock );
            }
        }
    }

    /**
     * Registers the given lock in the lookup maps of the <code>LockManager</code>.
     * <p>
     * This includes:
     * <ul>
     * <li><code>fidToLock-Map</code></li>
     * <li><code>lockIdToLock-Map</code></li>
     * <li><code>expiryTimeToLock-Map</code></li>
     * <li><code>lockToFile-Map</code></li>
     * </ul>
     *
     * @param lock
     *            the lock to be registered
     * @param file
     *            file that stores the persistent representation of the lock
     */
    private void registerLock( Lock lock, File file ) {
        for ( String fid : lock.getLockedFids() ) {
            this.fidToLock.put( fid, lock );
        }
        this.expiryTimeToLock.put( lock.getExpiryTime(), lock );
        this.lockIdToLock.put( lock.getId(), lock );
        this.lockToFile.put( lock, file );
        String msg = Messages.getMessage( "DATASTORE_LOCK_TIMEOUT_INFO", lock.getId(), new Date( lock.getExpiryTime() ) );
        LOG.logInfo( msg );
    }

    /**
     * Persists the given {@link Lock} to a temporary directory.
     * <p>
     * <ul>
     * <li>If the lock is empty (it holds no features), a potentially existing file is deleted.</li>
     * <li>If the lock is not empty and it has not been stored yet, it is written to a temporary file.</li>
     * <li>If the lock is not empty and it has already been stored, the existing file is overwritten with the current
     * lock status.</li>
     * </ul>
     *
     * @param lock
     *            the <code>Lock</code> to be persisted
     * @return <code>File</code> that stores the <code>Lock</code>, may be null
     * @throws DatastoreException
     */
    private File persistLock( Lock lock )
                            throws DatastoreException {

        File file = this.lockToFile.get( lock );
        if ( !lock.getLockedFids().isEmpty() ) {
            // only store it if any features are hold by the lock

            // delete file if it already exists
            if ( file != null ) {
                file.delete();
            }

            // write lock to file
            FileOutputStream fos = null;
            ObjectOutputStream oos = null;
            try {
                try {
                    if ( file == null ) {
                        file = File.createTempFile( FILE_PREFIX, FILE_SUFFIX, workingDir );
                    }
                    String msg = Messages.getMessage( "DATASTORE_LOCK_STORE", lock.getId(), file.getAbsolutePath() );
                    LOG.logDebug( msg );
                    fos = new FileOutputStream( file );
                    oos = new ObjectOutputStream( fos );
                    oos.writeObject( lock );
                } finally {
                    if ( oos != null ) {
                        oos.flush();
                        oos.close();
                    } else if ( fos != null ) {
                        fos.close();
                    }
                }
            } catch ( Exception e ) {
                String msg = Messages.getMessage( "DATASTORE_LOCK_STORING_FAILED", lock.getId(),
                                                  file.getAbsolutePath(), e.getMessage() );
                LOG.logError( msg, e );
                throw new DatastoreException( msg );
            }
        } else if ( file != null ) {
            // else (and file exists) delete file
            LOG.logDebug( "Deleting lock '" + lock.getId() + "' in file: " + file.getAbsolutePath() );
            file.delete();
        }
        return file;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer( "LockManager status:\n" );
        sb.append( "- number of locked features: " + this.fidToLock.size() + "\n" );
        sb.append( "- active locks: " + this.lockIdToLock.size() + "\n" );
        for ( Lock lock : this.lockIdToLock.values() ) {
            sb.append( "- " );
            sb.append( lock );
            sb.append( "\n" );
        }
        return sb.toString();
    }
}
