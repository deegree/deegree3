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
import org.deegree.services.OWS;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType.RequestTimeoutMilliseconds;

import java.util.List;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * Watchdog for enforcing time-outs on {@link OWS} requests.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
public class RequestWatchdog {

    private static final Logger LOG = getLogger( RequestWatchdog.class );

    private final WatchdogDaemon daemon = new WatchdogDaemon();

    private final Thread daemonThread;

    private final WatchedRequestFactory watchedRequestFactory;

    /**
     * Creates a new {@link RequestWatchdog} for the given time-out configuration.
     * 
     * @param timeoutConfigs
     *            time-outs values per request type and service
     */
    public RequestWatchdog( final List<RequestTimeoutMilliseconds> timeoutConfigs ) {
        daemonThread = new Thread( daemon, "RequestWatchdog" );
        daemonThread.setDaemon( true );
        watchedRequestFactory = new WatchedRequestFactory( timeoutConfigs );
    }

    /**
     * Adds the current thread to the watchdog, enforcing the time-out configured for the service and request type.
     * 
     * @param serviceId
     *            service identifier (e.g. "wms-ad"), must not be <code>null</code>
     * @param requestType
     *            request (e.g. "GetMap"), must not be <code>null</code>
     */
    public void watchCurrentThread( final String serviceId, final String requestType ) {
        final WatchedRequest request = watchedRequestFactory.create( serviceId, requestType );
        if ( request != null ) {
            daemon.watch( request );
        }
    }

    /**
     * Removes the request associated with the current thread from the watchdog.
     */
    public void unwatchCurrentThread() {
        daemon.unwatch();
    }

    /**
     * Initializes a {@link WatchdogDaemon} instance.
     */
    public void init() {
        LOG.info ("Starting");
        daemonThread.start();
    }

    /**
     * Destroys the {@link WatchdogDaemon}.
     */    
    public void destroy() {
        LOG.info ("Stopping");
        daemonThread.interrupt();
    }

}
