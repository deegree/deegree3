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
package org.deegree.framework.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 * class to manage the object pool. this is part of the combination of the object pool pattern an the singelton pattern.
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class ObjectPool extends TimerTask {

    private static final ILogger LOG = LoggerFactory.getLogger( ObjectPool.class );

    /**
     * all available objects
     */
    protected List<Object> available = null;

    /**
     * all objects in use
     */
    protected List<Object> in_use = null;

    /**
     * the start life time for each object
     */
    protected Map<Object, Long> startLifeTime = null;

    /**
     * the start usage time for each object
     */
    protected Map<Object, Long> startUsageTime = null;

    /**
     * number of existing instances
     */
    protected int existingInstances = 0;

    private int maxInstances = 50;

    // min * sec * millisec. example: 15*60*1000 = 15 minutes
    private int maxLifeTime = 15 * 60 * 1000;

    // min * sec * millisec. example: 5*60*1000 = 5 minutes
    private int maxUsageTime = 5 * 60 * 1000;

    // milliseconds
    private int updateInterval = 15000;

    /**
     * Creates a new ObjectPool object.
     */
    protected ObjectPool() {
        available = Collections.synchronizedList( new ArrayList<Object>( maxInstances ) );
        in_use = Collections.synchronizedList( new ArrayList<Object>( maxInstances ) );
        startLifeTime = Collections.synchronizedMap( new HashMap<Object, Long>( maxInstances ) );
        startUsageTime = Collections.synchronizedMap( new HashMap<Object, Long>( maxInstances ) );
        Timer timer = new Timer();
        timer.scheduleAtFixedRate( this, 10000, updateInterval );
        InputStream is = ObjectPool.class.getResourceAsStream( "pool.properties" );
        if ( is != null ) {
            Properties props = new Properties();
            try {
                props.load( is );
                is.close();
            } catch ( IOException e ) {
                LOG.logInfo( "could not load pool properties" );
                return;
            }
            maxInstances = Integer.parseInt( props.getProperty( "maxInstances" ) );
            maxLifeTime = Integer.parseInt( props.getProperty( "maxLifeTime" ) );
            maxUsageTime = Integer.parseInt( props.getProperty( "maxUsageTime" ) );
            updateInterval = Integer.parseInt( props.getProperty( "updateInterval" ) );
            LOG.logInfo( "pool configuration read from pool.properties" );
            LOG.logDebug( "pool properties", this.toString() );
        }
    }

    /**
     * dummy
     * 
     * @return null
     */
    public static ObjectPool getInstance() {
        return null;
    }

    /**
     * clears the complete pool. objects in used while the clear() method has been called won't be put back to the pool
     * if released back through the <tt>releaseObject</tt> method.
     */
    public synchronized void clear() {
        in_use.clear();
        available.clear();
        startUsageTime.clear();
        startLifeTime.clear();
    }

    /**
     * release an object back to the pool so it is available for other requests.
     * 
     * @param object
     * @throws Exception
     */
    public void releaseObject( Object object )
                            throws Exception {

        if ( in_use.contains( object ) ) {
            // remove the object from the 'in use' container
            in_use.remove( object );
            // remove the objects entry from the 'usage star time' container
            startUsageTime.remove( object );
            // push the object to the list of available objects
            available.add( object );
        }
    }

    /**
     * this method will be called when the submitted object will be removed from the pool
     * 
     * @param o
     */
    public abstract void onObjectKill( Object o );

    /**
     * @return the max life time
     */
    public int getMaxLifeTime() {
        return ( this.maxLifeTime );
    }

    /**
     * @param maxLifeTime
     */
    public void setMaxLifeTime( int maxLifeTime ) {
        this.maxLifeTime = maxLifeTime;
    }

    /**
     * @return the max usage time
     */
    public int getMaxUsageTime() {
        return ( this.maxUsageTime );
    }

    /**
     * @param maxUsageTime
     */
    public void setMaxUsageTime( int maxUsageTime ) {
        this.maxUsageTime = maxUsageTime;
    }

    /**
     * @return the update interval
     */
    public int getUpdateInterval() {
        return ( this.updateInterval );
    }

    /**
     * @param updateInterval
     */
    public void setUpdateInterval( int updateInterval ) {
        this.updateInterval = updateInterval;
    }

    /**
     * @return max instances
     */
    public int getMaxInstances() {
        return ( this.maxInstances );
    }

    /**
     * @param maxInstances
     */
    public void setMaxInstances( int maxInstances ) {
        this.maxInstances = maxInstances;
    }

    @Override
    public String toString() {
        String ret = getClass().getName() + "\n";
        ret = "startLifeTime = " + startLifeTime + "\n";
        ret += ( "startUsageTime = " + startUsageTime + "\n" );
        ret += ( "maxLifeTime = " + maxLifeTime + "\n" );
        ret += ( "maxUsageTime = " + maxUsageTime + "\n" );
        ret += ( "updateInterval = " + updateInterval + "\n" );
        ret += ( "maxInstances = " + maxInstances + "\n" );
        return ret;
    }

    @Override
    public void run() {
        cleaner();
        usage();
    }

    private synchronized void cleaner() {

        try {
            Object[] os = available.toArray();
            for ( int i = 0; i < os.length; i++ ) {
                Object o = os[i];
                Long lng = startLifeTime.get( o );
                long l = System.currentTimeMillis();
                if ( lng == null || ( l - lng.longValue() ) > maxLifeTime ) {
                    if ( o != null ) {
                        available.remove( o );
                        startLifeTime.remove( o );
                        onObjectKill( o );
                    }
                    existingInstances--;
                }
            }

        } catch ( Exception e ) {
            LOG.logError( "ObjectPool Cleaner ", e );
        }

    }

    private synchronized void usage() {
        try {
            Object[] os = in_use.toArray();
            for ( int i = 0; i < os.length; i++ ) {
                Object o = os[i];
                Long lng = startUsageTime.get( o );
                long l = System.currentTimeMillis();
                if ( lng == null || ( l - lng.longValue() ) > maxUsageTime ) {
                    if ( o != null ) {
                        in_use.remove( o );
                        startUsageTime.remove( o );
                        startLifeTime.remove( o );
                        onObjectKill( o );
                    }
                }
            }
        } catch ( Exception e ) {
            LOG.logError( "UsageChecker ", e );
        }

    }

}
