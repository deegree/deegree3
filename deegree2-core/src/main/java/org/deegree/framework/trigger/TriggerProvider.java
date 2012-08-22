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
package org.deegree.framework.trigger;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.trigger.TriggerCapabilities.TRIGGER_TYPE;
import org.deegree.framework.util.BootLogger;
import org.deegree.i18n.Messages;

/**
 *
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class TriggerProvider {

    private static Map<String, TriggerProvider> providerMap = new HashMap<String, TriggerProvider>();

    private static TriggerCapabilities triggerCapabilities;

    private static final ILogger LOG = LoggerFactory.getLogger( TriggerProvider.class );

    static {
        try {
            URL url = TriggerProvider.class.getResource( "triggerConfiguration.xml" );
            TriggerConfigurationDocument doc = new TriggerConfigurationDocument();
            doc.load( url );
            triggerCapabilities = doc.parseTriggerCapabilities();
            // try reading trigger definitions from root that may overrides
            // default trigger
            url = TriggerProvider.class.getResource( "/triggerConfiguration.xml" );
            try {

                if ( url != null ) {
                    LOG.logDebug( "Trying to create trigger from local configuration: " + url );
                    doc = new TriggerConfigurationDocument();
                    doc.load( url );
                    TriggerCapabilities temp = doc.parseTriggerCapabilities();
                    triggerCapabilities.merge( temp );
                } else {
                    LOG.logDebug( "No local configuration found." );
                }

            } catch ( Exception e ) {
                e.printStackTrace();
                BootLogger.log( "!!! BOOTLOG: No valid trigger configuration available from root." );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private String className = null;

    private TriggerProvider( String className ) {
        this.className = className;
    }

    /**
     *
     * @param clss
     */
    public static TriggerProvider create( Class clss ) {
        String s = clss.getName();
        if ( providerMap.get( s ) == null ) {
            providerMap.put( s, new TriggerProvider( s ) );
        }
        return providerMap.get( s );
    }

    /**
     * @return all pre triggers assigend to the calling method
     */
    public List<Trigger> getPreTrigger()
                            throws TriggerException {

        List<Trigger> trigger = new ArrayList<Trigger>();

        StackTraceElement[] st = Thread.currentThread().getStackTrace();

        String mn = null;
        for ( int i = 0; i < st.length; i++ ) {
            if ( st[i].getClassName().equals( className ) ) {
                mn = st[i].getMethodName();
                break;
            }
        }
        if ( mn != null ) {
            TriggerCapability tc = triggerCapabilities.getTriggerCapability( className, mn, TRIGGER_TYPE.PRE );
            if ( tc != null ) {
                appendTrigger( trigger, tc );

                List<TriggerCapability> tCaps = tc.getTrigger();
                for ( int i = 0; i < tCaps.size(); i++ ) {
                    appendTrigger( trigger, tCaps.get( i ) );
                }
            }
        }

        return trigger;
    }

    /**
     * returns all post triggers assigend to the calling method
     *
     * @return all post triggers assigend to the calling method
     */
    public List<Trigger> getPostTrigger()
                            throws TriggerException {
        List<Trigger> trigger = new ArrayList<Trigger>();

        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        String mn = null;
        for ( int i = 0; i < st.length; i++ ) {
            if ( st[i].getClassName().equals( className ) ) {
                mn = st[i].getMethodName();
                break;
            }
        }

        if ( mn != null ) {
            TriggerCapability tc = triggerCapabilities.getTriggerCapability( className, mn, TRIGGER_TYPE.POST );

            if ( tc != null ) {
                appendTrigger( trigger, tc );

                List<TriggerCapability> tCaps = tc.getTrigger();
                for ( int i = 0; i < tCaps.size(); i++ ) {
                    appendTrigger( trigger, tCaps.get( i ) );
                }
            }
        }
        return trigger;
    }

    /**
     * creates a Trigger instance from the passed TriggerCapability and add it to the passed list.
     * If the TriggerCapability contains futher TriggerCapability entries they will also be added
     * within a recursion
     *
     * @param trigger
     * @param tc
     * @return extended list
     * @throws TriggerException
     */
    private List<Trigger> appendTrigger( List<Trigger> trigger, TriggerCapability tc )
                            throws TriggerException {
        Class clss = tc.getPerformingClass();
        List<String> paramNames = tc.getInitParameterNames();
        Class[] initClasses = new Class[paramNames.size()];
        Object[] paramVals = new Object[paramNames.size()];
        for ( int i = 0; i < initClasses.length; i++ ) {
            paramVals[i] = tc.getInitParameterValue( paramNames.get( i ) );
            initClasses[i] = paramVals[i].getClass();
        }
        try {
            Constructor cstrtr = clss.getConstructor( initClasses );
            LOG.logDebug( "Trying to instantiate new class" );
            trigger.add( (Trigger) cstrtr.newInstance( paramVals ) );
            LOG.logDebug( "Succesfully instantiated configured trigger class." );
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new TriggerException( Messages.getMessage( "FRAMEWORK_ERROR_INITIALIZING_TRIGGER", clss ) );
        }
        return trigger;
    }

    /**
     * performs pre triggers assigend to the calling method
     *
     * @param caller
     * @param obj
     * @return changed object passed to the trigger(s)
     * @throws TriggerException
     */
    public Object[] doPreTrigger( Object caller, Object... obj )
                            throws TriggerException {
        List<Trigger> list = getPreTrigger();
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "list of appliable pretriggers: " + list );
        }
        for ( int i = 0; i < list.size(); i++ ) {
            obj = list.get( i ).doTrigger( caller, obj );
        }
        return obj;
    }

    /**
     * performs post triggers assigend to the calling method
     *
     * @param caller
     * @param obj
     * @return changed object passed to the trigger(s)
     */
    public Object[] doPostTrigger( Object caller, Object... obj ) {
        List<Trigger> list = getPostTrigger();
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "list of appliable posttriggers: " + list );
        }
        for ( int i = 0; i < list.size(); i++ ) {
            obj = list.get( i ).doTrigger( caller, obj );
        }
        return obj;
    }

    /**
     * returns the root capabilities
     *
     * @return the root capabilities
     */
    public TriggerCapabilities getCapabilities() {
        return triggerCapabilities;
    }

}
