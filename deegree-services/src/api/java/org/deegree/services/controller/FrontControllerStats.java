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

import static org.deegree.commons.utils.ArrayUtils.splitAsDoubles;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.TreeSet;

import org.deegree.commons.utils.ComparablePair;
import org.deegree.commons.utils.ConfigManager;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.slf4j.Logger;

/**
 * Keeps track of request and runtime statistics for the {@link OGCFrontController}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FrontControllerStats {

    private static final Logger LOG = getLogger( FrontControllerStats.class );

    private static long numDispatched;

    private static long numFinished;

    private static double averageResponseTime;

    private static double maxResponseTime;

    private static Envelope bbox;

    private static final GeometryFactory fac = new GeometryFactory();

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
     * @param request
     *            the query string
     * @param timestamp
     */
    public synchronized static void incomingKVP( String request, long timestamp ) {
        try {
            OutputStream os = ConfigManager.getOutputResource( "requests.txt", true );
            PrintWriter out = new PrintWriter( new OutputStreamWriter( os, "UTF-8" ) );
            out.println( timestamp + " " + request );
            out.close();
            if ( request.toUpperCase().contains( "REQUEST=GETMAP" ) ) {
                try {
                    Map<String, String> map = KVPUtils.getNormalizedKVPMap( request, "UTF-8" );
                    if ( map.get( "LAYERS" ).equals( "statistics" ) ) {
                        return;
                    }
                    double[] ds = splitAsDoubles( map.get( "BBOX" ), "," );
                    Envelope newBox = fac.createEnvelope( ds[0], ds[1], ds[2], ds[3], new CRS( map.get( "SRS" ) ) );
                    if ( bbox != null ) {
                        bbox.merge( newBox );
                    }
                } catch ( UnsupportedEncodingException e ) {
                    LOG.trace( "Stack trace:", e );
                }
            }
        } catch ( FileNotFoundException e ) {
            LOG.debug( "Could not find the file to store requests." );
            LOG.debug( " Probably the DEEGREE_HOME directory does not exist and could not be created." );
            LOG.trace( "Stack trace:", e );
        } catch ( UnsupportedEncodingException e ) {
            LOG.trace( "Stack trace:", e );
        }
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

    /**
     * @return the incoming KVP requests
     */
    public static TreeSet<ComparablePair<Long, String>> getKVPRequests() {
        TreeSet<ComparablePair<Long, String>> requests = new TreeSet<ComparablePair<Long, String>>();
        InputStreamReader is = null;
        try {
            is = new InputStreamReader( ConfigManager.getInputResource( "requests.txt" ), "UTF-8" );
            BufferedReader in = new BufferedReader( is );
            String s = null;
            while ( ( s = in.readLine() ) != null ) {
                String[] req = s.split( " " );
                if ( req.length < 2 ) {
                    continue;
                }
                ComparablePair<Long, String> p = new ComparablePair<Long, String>( Long.valueOf( req[0] ), req[1] );
                if ( !requests.contains( p ) ) {
                    requests.add( p );
                }
            }
            in.close();
        } catch ( UnsupportedEncodingException e ) {
            LOG.trace( "Stack trace:", e );
        } catch ( FileNotFoundException e ) {
            LOG.debug( "The requests file does not exist." );
            LOG.trace( "Stack trace:", e );
        } catch ( IOException e ) {
            LOG.debug( "The requests file could not be read: '{}'", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } finally {
            if ( is != null ) {
                try {
                    is.close();
                } catch ( IOException e ) {
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
        return requests;
    }

    /**
     * @return the time of the first request
     */
    public static long getStartingTime() {
        InputStreamReader is = null;
        try {
            is = new InputStreamReader( ConfigManager.getInputResource( "requests.txt" ), "UTF-8" );
            BufferedReader in = new BufferedReader( is );
            String s = null;
            if ( ( s = in.readLine() ) != null ) {
                String[] req = s.split( " " );
                return Long.parseLong( req[0] );
            }
        } catch ( UnsupportedEncodingException e ) {
            LOG.trace( "Stack trace:", e );
        } catch ( FileNotFoundException e ) {
            LOG.debug( "The requests file does not exist." );
            LOG.trace( "Stack trace:", e );
        } catch ( IOException e ) {
            LOG.debug( "The requests file could not be read: '{}'", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } finally {
            if ( is != null ) {
                try {
                    is.close();
                } catch ( IOException e ) {
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
        return -1;
    }

    /**
     * @return an envelope of all the logged GetMap requests
     */
    public static synchronized Envelope getCombinedGetMapEnvelope() {
        if ( bbox == null ) {
            for ( Pair<Long, String> req : getKVPRequests() ) {
                if ( !req.second.toUpperCase().contains( "REQUEST=GETMAP" ) ) {
                    continue;
                }
                try {
                    Map<String, String> map = KVPUtils.getNormalizedKVPMap( req.second, "UTF-8" );
                    if ( map.get( "LAYERS" ).equals( "statistics" ) ) {
                        continue;
                    }
                    if ( map.get( "VERSION" ) == null || !map.get( "VERSION" ).equals( "1.1.1" ) ) {
                        continue;
                    }
                    double[] ds = splitAsDoubles( map.get( "BBOX" ), "," );
                    Envelope newBox = fac.createEnvelope( ds[0], ds[1], ds[2], ds[3], new CRS( map.get( "SRS" ) ) );
                    if ( bbox == null ) {
                        bbox = newBox;
                    } else {
                        bbox.merge( newBox );
                    }
                } catch ( UnsupportedEncodingException e ) {
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
        return bbox;
    }

}
