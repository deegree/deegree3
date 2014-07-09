package org.deegree.services.controller.watchdog;

public class WatchedRequest {
    
    private final Thread thread;
    
    private final long stopTimeInMillis;

    /**
     * @param thread
     * @param stopTimeInMillis
     */
    public WatchedRequest( final Thread thread, final long stopTimeInMillis ) {
        this.thread = thread;
        this.stopTimeInMillis = stopTimeInMillis;
    }

    /**
     * @return the thread
     */
    Thread getThread() {
        return thread;
    }

    /**
     * @return the stopTimeInMillis
     */
    long getStopTimeInMillis() {
        return stopTimeInMillis;
    }
    
}
