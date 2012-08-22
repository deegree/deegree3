//$HeadURL$
// $Id$
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

package org.deegree.framework.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

/**
 * Interceptor to profile the application.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </A>
 *
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @see <a href="http://www.dofactory.com/patterns/PatternChain.aspx">Chain of Responsibility Design Pattern </a>
 *
 * @since 2.0
 */
public class ProfilerInterceptor extends Interceptor {

    static {
        Runtime.getRuntime().addShutdownHook( new Thread( new Runnable() {

            public void run() {
                printStats();
            }
        } ) );
    }

    /**
     * data to be used
     */
    protected static HashMap<String, ProfileEntry> data = new HashMap<String, ProfileEntry>();

    /**
     *
     * @param nextInterceptor
     */
    public ProfilerInterceptor( Interceptor nextInterceptor ) {
        this.nextInterceptor = nextInterceptor;
    }

    /**
     *
     *
     */
    protected static void printStats() {
        NumberFormat zweiDezimalen = new DecimalFormat( "### ###.##" );

        for ( String m : data.keySet() ) {
            ProfileEntry entry = data.get( m );
            LOG.logInfo( entry.invocations + " calls " + entry.time + " ms " + " avg "
                         + zweiDezimalen.format( entry.getAverage() ) + m );
        }
    }

    /**
     *
     * @param m
     * @return named ProfileEntry
     */
    protected static ProfileEntry getEntry( String m ) {
        if ( data.containsKey( m ) ) {
            return data.get( m );
        }
        ProfileEntry entry = new ProfileEntry();
        data.put( m, entry );
        return entry;
    }

    /**
     * @param method
     * @param params
     * @return -
     */
    @Override
    protected Object handleInvocation( Method method, Object[] params )
                            throws IllegalAccessException, InvocationTargetException {
        long start = System.currentTimeMillis();
        Object result = nextInterceptor.handleInvocation( method, params );
        long duration = System.currentTimeMillis() - start;
        ProfileEntry entry = getEntry( getTarget().getClass().getName() + "." + method.getName() );
        entry.time += duration;
        entry.invocations++;
        return result;
    }

    /**
     * <code>ProfileEntry</code> simple wrapper to be used ad a profile entry
     *
     * @author last edited by: $Author$
     *
     * @version $Revision$, $Date$
     */
    static class ProfileEntry {

        /**
         * of the profile entry
         */
        long time;

        /**
         * how many invocations
         */
        long invocations;

        /**
         * @return the average invocations during the last time
         */
        public double getAverage() {
            return (double) time / invocations;
        }

    }
}
