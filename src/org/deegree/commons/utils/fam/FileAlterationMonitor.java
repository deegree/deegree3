//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.commons.utils.fam;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.deegree.commons.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File alteration monitor for monitoring changes in a directory. Supports recursive scanning and filters.
 *
 * @see FileAlterationListener
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class FileAlterationMonitor {

    private static final Logger LOG = LoggerFactory.getLogger( FileAlterationMonitor.class );

    private final File dir;

    private final long interval;

    private final boolean recurse;

    private final FileFilter filter;

    private Map<File, Long> lastFiles;

    private final List<FileAlterationListener> listeners = new ArrayList<FileAlterationListener>();

    private Timer timer;

    /**
     * Creates a new {@link FileAlterationMonitor}.
     *
     * @param dir
     *            directory to be monitored
     * @param interval
     *            number of milliseconds to wait between checks
     * @param recurse
     *            if true, subdirectories (and their subdirectories, ...) are monitored as well
     * @param filter
     *            optional filter that allows to specify for which files events should be generated, may be null (report
     *            changes on all files)
     */
    public FileAlterationMonitor( File dir, long interval, boolean recurse, FileFilter filter ) {
        this.dir = dir;
        this.interval = interval;
        this.recurse = recurse;
        this.filter = filter;
    }

    /**
     * Registers a {@link FileAlterationListener} which is notified when changes are detected.
     *
     * @param listener
     *            listener to be registered
     */
    public void registerListener( FileAlterationListener listener ) {
        listeners.add( listener );
    }

    /**
     * Removes a {@link FileAlterationListener}.
     *
     * @param listener
     *            listener to be removed
     */
    public void removeListener( FileAlterationListener listener ) {
        listeners.remove( listener );
    }

    /**
     * Starts the file alteration monitor worker thread.
     *
     * @throws RuntimeException
     *             if the worker thread is already running
     */
    @SuppressWarnings("synthetic-access")
    public void start() {
        synchronized ( this ) {
            if ( timer != null ) {
                throw new RuntimeException( Messages.getMessage( "UTILS_FAM_ALREADY_STARTED" ) );
            }
            LOG.debug( "Starting worker thread." );
            timer = new Timer();
            timer.schedule( new Worker(), 0, interval );
        }
    }

    /**
     * Stops the file alteration monitor worker thread.
     *
     * @throws RuntimeException
     *             if the worker thread wasn't running
     */
    public void stop() {
        synchronized ( this ) {
            if ( timer == null ) {
                throw new RuntimeException( Messages.getMessage( "UTILS_FAM_NOT_STARTED" ) );
            }
            LOG.debug( "Stopping worker thread." );
            timer.cancel();
        }
    }

    private class Worker extends TimerTask {

        @SuppressWarnings("synthetic-access")
        @Override
        public void run() {
            Map<File, Long> newFiles = new HashMap<File, Long>();
            try {
                scanDir( dir, recurse, filter, newFiles, new HashSet<File>() );
            } catch ( IOException e ) {
                LOG.error( e.getMessage(), e );
            }
            if ( lastFiles != null ) {
                // check for new / changed files
                for ( File file : newFiles.keySet() ) {
                    long modificationTime = newFiles.get( file );
                    if ( !lastFiles.containsKey( file ) ) {
                        newFile( file );
                    } else {
                        long oldModificationTime = lastFiles.get( file );
                        if ( modificationTime != oldModificationTime ) {
                            fileChanged( file );
                        }
                    }
                }
                // check for deleted files
                for ( File file : lastFiles.keySet() ) {
                    if ( !newFiles.containsKey( file ) ) {
                        fileDeleted( file );
                    }
                }
            } else {
                for ( File file : newFiles.keySet() ) {
                    newFile( file );
                }
            }
            lastFiles = newFiles;
        }

        @SuppressWarnings("synthetic-access")
        private void fileChanged( File file ) {
            LOG.debug( "Detected file change: '" + file + "'." );
            for ( FileAlterationListener listener : listeners ) {
                listener.fileChanged( file );
            }
        }

        @SuppressWarnings("synthetic-access")
        private void newFile( File file ) {
            LOG.debug( "Detected new file: '" + file + "'." );
            for ( FileAlterationListener listener : listeners ) {
                listener.newFile( file );
            }
        }

        @SuppressWarnings("synthetic-access")
        private void fileDeleted( File file ) {
            LOG.debug( "Detected file deletion: '" + file + "'." );
            for ( FileAlterationListener listener : listeners ) {
                listener.fileDeleted( file );
            }
        }

        /**
         * Scans the given directory and collects information on all files and their modification times.
         *
         * @param dir
         *            directory to be scanned
         * @param recurse
         *            if true, subdirectories (and their subdirectories, ...) are scanned as well
         * @param filter
         *            optional filter that allows to specify for which files events should be generated, may be null
         *            (report changes on all files)
         * @param foundFiles
         *            stores all accepted files and their modification time
         * @param scannedDirs
         *            scanned directories (used to avoid cyclic caused by filesystem links)
         * @throws IOException
         */
        @SuppressWarnings("synthetic-access")
        private void scanDir( File dir, boolean recurse, FileFilter filter, Map<File, Long> foundFiles,
                              Set<File> scannedDirs )
                                throws IOException {
            LOG.debug( "Scanning directory: '" + dir + "'" );
            File[] files = dir.listFiles( filter );
            for ( File file : files ) {
                if ( filter == null || filter.accept( file ) ) {
                    foundFiles.put( file, file.lastModified() );
                }
            }
            if ( recurse ) {
                scannedDirs.add( dir.getCanonicalFile() );
                for ( File file : dir.listFiles() ) {
                    if ( file.isDirectory() && !scannedDirs.contains( file.getCanonicalFile() ) ) {
                        scanDir( file, recurse, filter, foundFiles, scannedDirs );
                    }
                }
            }
        }
    }
}
