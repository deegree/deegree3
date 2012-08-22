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

package org.deegree.io.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Replaces inner class Reloader in AbstractOGCServlet.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </A>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @see org.deegree.enterprise.AbstractOGCServlet
 * @see java.io.File#lastModified
 * @see <a href="http://www.javaworld.com/javaworld/javatips/jw-javatip125.html">Java Tip 125 </a>
 *
 * @since 2.0
 */

public class FileMonitor {

    private static final FileMonitor instance = new FileMonitor();

    private Timer timer;

    private Hashtable<String, FileMonitorTask> timerEntries;

    /**
     * Factory method to get singleton instance.
     *
     * @return the instance
     *
     */
    public static FileMonitor getInstance() {
        return instance;
    }

    protected FileMonitor() {
        // Create timer, run timer thread as daemon.
        timer = new Timer( true );
        timerEntries = new Hashtable<String, FileMonitorTask>();
    }

    /**
     * Add a monitored file with a FileChangeListener.
     *
     * @param listener
     *            listener to notify when the file changed.
     * @param fileName
     *            name of the file to monitor.
     * @param period
     *            polling period in milliseconds.
     * @throws FileNotFoundException
     */
    public void addFileChangeListener( FileChangeListener listener, String fileName, long period )
                            throws FileNotFoundException {
        removeFileChangeListener( listener, fileName );
        FileMonitorTask task = new FileMonitorTask( listener, fileName );
        timerEntries.put( fileName + listener.hashCode(), task );
        timer.schedule( task, period, period );
    }

    /**
     * Remove the listener from the notification list.
     *
     * @param listener
     *            the listener to be removed.
     * @param fileName
     */
    public void removeFileChangeListener( FileChangeListener listener, String fileName ) {
        FileMonitorTask task = timerEntries.remove( fileName + listener.hashCode() );
        if ( task != null ) {
            task.cancel();
        }
    }

    protected void fireFileChangeEvent( FileChangeListener listener, String fileName ) {
        listener.fileChanged( fileName );
    }

    class FileMonitorTask extends TimerTask {

        FileChangeListener listener;

        String fileName;

        File monitoredFile;

        long lastModified;

        /**
         * @param listener
         * @param fileName
         * @throws FileNotFoundException
         */
        public FileMonitorTask( FileChangeListener listener, String fileName ) throws FileNotFoundException {
            this.listener = listener;
            this.fileName = fileName;
            this.lastModified = 0;

            monitoredFile = new File( fileName );
            if ( !monitoredFile.exists() ) { // but is it on CLASSPATH?
                URL fileURL = listener.getClass().getClassLoader().getResource( fileName );
                if ( fileURL != null ) {
                    monitoredFile = new File( fileURL.getFile() );
                } else {
                    throw new FileNotFoundException( "File Not Found: " + fileName );
                }
            }
            this.lastModified = monitoredFile.lastModified();
        }

        @Override
        public void run() {
            long lastModified = monitoredFile.lastModified();
            if ( lastModified != this.lastModified ) {
                this.lastModified = lastModified;
                fireFileChangeEvent( this.listener, this.fileName );
            }
        }
    }
}
