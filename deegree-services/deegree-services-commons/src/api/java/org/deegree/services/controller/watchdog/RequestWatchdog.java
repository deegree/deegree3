package org.deegree.services.controller.watchdog;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;

public class RequestWatchdog implements Runnable {

    private static final Logger LOG = getLogger( RequestWatchdog.class );

    private static final long CHECK_INTERVAL_MILLIS = 500;

    private List<WatchedRequest> watchedRequests = Collections.synchronizedList( new ArrayList<WatchedRequest>() );

    @Override
    public void run() {
        LOG.debug( "RequestWatchDog starting" );
        while ( true ) {
            interruptAndRemoveTimedOutRequests();
            try {
                Thread.sleep( CHECK_INTERVAL_MILLIS );
            } catch ( InterruptedException e ) {
                LOG.debug( "RequestWatchDog terminating" );
                return;
            }
        }
    }

    public void watch( final WatchedRequest request ) {
        watchedRequests.add( request );
    }

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
