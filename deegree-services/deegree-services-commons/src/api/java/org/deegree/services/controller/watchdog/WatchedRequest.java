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

/**
 * A request that is watched by the {@link WatchdogDaemon}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
class WatchedRequest {

    private final Thread thread;

    private final long stopTimeInMillis;

    private final long startTimeInMillis;

    private final String service;

    private final String request;

    /**
     * Creates a new {@link WatchedRequest} instance.
     * 
     * @param thread
     *            thread of the request to watch, must not be <code>null</code>
     * @param stopTimeInMillis
     *            time (in milliseconds) when the request will be interrupted
     */
    WatchedRequest( final Thread thread, final long stopTimeInMillis, final long startTimeInMillis,
                    final String service, final String request ) {
        this.thread = thread;
        this.stopTimeInMillis = stopTimeInMillis;
        this.startTimeInMillis = startTimeInMillis;
        this.service = service;
        this.request = request;
    }

    /**
     * Returns the thread that is executing the request.
     * 
     * @return the thread, never <code>null</code>
     */
    Thread getThread() {
        return thread;
    }

    /**
     * Returns the time when the request needs to be interrupted.
     * 
     * @return time (in milliseconds) when the request needs to be interrupted
     */
    long getStopTimeInMillis() {
        return stopTimeInMillis;
    }

    @Override
    public String toString() {
        return service + ":" + request + "@" + startTimeInMillis;
    }
}
