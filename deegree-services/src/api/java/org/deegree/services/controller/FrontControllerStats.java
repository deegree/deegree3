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
package org.deegree.services.controller;

/**
 * Keeps track of request and rumtime statistics for the {@link OGCFrontController}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FrontControllerStats {

    private static long numDispatched;

    private static long numFinished;

    private static double averageResponseTime;

    private static double maxResponseTime;

    /**
     * Called by the {@link OGCFrontController} to indicate that a new request has just been dispatched to an
     * {@link AbstractOGCServiceController}.
     * 
     * @return current time
     */
    synchronized static long requestDispatched() {
        numDispatched++;
        return System.currentTimeMillis();
    }

    /**
     * Called by the {@link OGCFrontController} to indicate that a dispatched request has been finished.
     * 
     * @param dispatchTime
     *            time when the request has been dispatched
     */
    synchronized static void requestFinished( long dispatchTime ) {
        long duration = System.currentTimeMillis() - dispatchTime;
        if ( duration > maxResponseTime ) {
            maxResponseTime = duration;
        }
        averageResponseTime = ( averageResponseTime * numFinished + duration ) / ( numFinished + 1 );
        numFinished++;
    }

    /**
     * Returns the number of requests that the {@link OGCFrontController} dispatched to service controllers.
     * 
     * @return number of dispatched requests
     */
    public static long getDispatchedRequests() {
        return numDispatched;
    }

    /**
     * Returns the number of currently active requests, i.e. requests that the {@link OGCFrontController} dispatched to
     * service controllers, but that didn't return yet.
     * 
     * @return number of active requests
     */
    public static long getActiveRequests() {
        return numDispatched - numFinished;
    }

    /**
     * Returns the average response time for all finished requests.
     * 
     * @return the average response time
     */
    public static long getAverageResponseTime() {
        return (long) averageResponseTime;
    }

    /**
     * Returns the maximum response time of all finished requests.
     * 
     * @return the maximum response time
     */    
    public static long getMaximumResponseTime() {
        return (long) maxResponseTime;
    }
}
