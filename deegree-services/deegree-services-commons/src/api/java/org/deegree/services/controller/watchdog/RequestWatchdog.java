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

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deegree.services.OWS;
import org.slf4j.Logger;

/**
 * Periodically checks for timed-out {@link OWS} requests and interrupts the request threads.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
public class RequestWatchdog implements Runnable {

    private static final Logger LOG = getLogger( RequestWatchdog.class );

    private static final long CHECK_INTERVAL_MILLIS = 500;

    private List<WatchedRequest> watchedRequests = Collections.synchronizedList( new ArrayList<WatchedRequest>() );

    @Override
    public void run() {
        LOG.debug( "Starting main loop" );
        while ( true ) {
            interruptAndRemoveTimedOutRequests();
            try {
                Thread.sleep( CHECK_INTERVAL_MILLIS );
            } catch ( InterruptedException e ) {
                LOG.debug( "Interrupted. Exiting" );
                return;
            }
        }
    }

    /**
     * Adds the given request to be watched for time-out.
     * 
     * @param request
     *            request to watch, must not be <code>null</code>
     */
    public void watch( final WatchedRequest request ) {
        watchedRequests.add( request );
    }

    /**
     * Removes the given request from the watched requests.
     * 
     * @param request
     *            request to remove, must not be <code>null</code>
     */
    public void unwatch( final WatchedRequest request ) {
        watchedRequests.remove( request );
    }

    private void interruptAndRemoveTimedOutRequests() {
        final List<WatchedRequest> requestSnapshot = new ArrayList<WatchedRequest>( watchedRequests );
        LOG.debug( "Checking for timed-out requests. Number of active requests: " + requestSnapshot.size() );
        final long currentTimeMillis = System.currentTimeMillis();
        for ( final WatchedRequest request : requestSnapshot ) {
            if ( currentTimeMillis > request.getStopTimeInMillis() ) {
                LOG.warn( "Time-out detected for request " + request + ". Interrupting request thread." );
                request.getThread().interrupt();
                watchedRequests.remove( request );
            }
        }
    }

}
