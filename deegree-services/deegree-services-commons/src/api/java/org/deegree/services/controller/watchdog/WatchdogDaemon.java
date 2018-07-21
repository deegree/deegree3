/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.services.controller.watchdog;

import org.apache.logging.log4j.Logger;

import java.util.*;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * Tracks {@link WatchedRequest}s and periodically checks whether they need to be interrupted.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
class WatchdogDaemon implements Runnable {

    private static final Logger LOG = getLogger( WatchdogDaemon.class );

    private static final long CHECK_INTERVAL_MILLIS = 500;

    private final Map<Thread, WatchedRequest> threadToRequest = Collections.synchronizedMap( new HashMap<Thread, WatchedRequest>() );

    @Override
    public void run() {
        LOG.debug( "Starting main loop" );
        while ( true ) {
            interruptTimedOutRequests();
            try {
                Thread.sleep( CHECK_INTERVAL_MILLIS );
            } catch ( InterruptedException e ) {
                LOG.debug( "Interrupted. Exiting" );
                return;
            }
        }
    }

    /**
     * Adds a request to be watched for time-outs.
     * 
     * @param request
     *            request to watch, must not be <code>null</code>
     */
    void watch( final WatchedRequest request ) {
        threadToRequest.put( request.getThread(), request );
    }

    /**
     * Removes the current thread from the watched requests.
     */
    void unwatch() {
        threadToRequest.remove( Thread.currentThread() );
    }

    private void interruptTimedOutRequests() {
        final List<WatchedRequest> activeRequests = new ArrayList<WatchedRequest>( threadToRequest.values() );
        LOG.debug( "Checking for timed-out requests. Number of active requests: " + activeRequests.size() );
        final long currentTimeMillis = System.currentTimeMillis();
        for ( final WatchedRequest request : activeRequests ) {
            if ( currentTimeMillis > request.getStopTimeInMillis() ) {
                LOG.info( "Request " + request + " timed-out. Interrupting request thread." );
                request.getThread().interrupt();
            }
        }
    }

}
