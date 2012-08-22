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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

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
public class TriggerCapabilities {

    private static final ILogger LOG = LoggerFactory.getLogger( TriggerCapabilities.class );

    private Map<String, TargetClass> targetClasses;

    public static enum TRIGGER_TYPE {
        /** identifies pre-trigger */
        PRE,
        /** identifies post-trigger */
        POST,
        /** identifies all trigger */
        ALL
    }

    /**
     *
     */
    public TriggerCapabilities( Map<String, TargetClass> targetClasses ) {
        this.targetClasses = targetClasses;
    }

    /**
     * returns a List of capabilities of all available
     *
     * @see Trigger
     * @return a List of capabilities of all available
     * @see Trigger
     */
    public List<TargetClass> getTargetClasses() {
        List<TargetClass> list = new ArrayList<TargetClass>( targetClasses.size() );
        Iterator iter = targetClasses.values().iterator();
        while ( iter.hasNext() ) {
            list.add( (TargetClass) iter.next() );
        }
        return list;
    }

    /**
     * returns all the capabilties of all
     *
     * @see Trigger assigned to class and method. The thrid parameter determines if just
     *      pre-trigger, post-trigger or all triggers shall be returned.
     * @param clss
     * @param method
     * @param type
     * @return all the capabilties of all
     * @see Trigger assigned to class and method.
     */
    public TriggerCapability getTriggerCapability( String clss, String method, TRIGGER_TYPE type ) {

        LOG.logDebug( "get trigger capability for: ", clss + '.' + method );

        TriggerCapability tc = null;
        TargetClass tgc = targetClasses.get( clss );
        if ( tgc != null ) {
            TargetMethod tm = tgc.getMethod( method );
            if ( tm != null ) {
                if ( type.equals( TRIGGER_TYPE.PRE ) ) {
                    tc = tm.getPreTrigger();
                } else {
                    tc = tm.getPostTrigger();
                }
            }
        }
        return tc;
    }

    /**
     * returns the mapping of java class names to
     *
     * @see TargetClass instances
     * @return the mapping of java class names to
     * @see TargetClass instances
     */
    Map<String, TargetClass> getClassMappings() {
        return targetClasses;
    }

    /**
     * adds all mappings from the passed TriggerCapabilities to the current. The passed mappings
     * will override the current mapping if necessary
     *
     * @param other
     */
    void merge( TriggerCapabilities other ) {
        Iterator iter = other.getClassMappings().keySet().iterator();
        while ( iter.hasNext() ) {
            String key = (String) iter.next();
            targetClasses.put( key, other.getClassMappings().get( key ) );
        }
    }

}
