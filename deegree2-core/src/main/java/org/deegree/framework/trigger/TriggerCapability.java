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

import java.util.List;
import java.util.Map;

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
public class TriggerCapability {

    private String name;

    private Class clss;

    private List<String> paramNames;

    private Map<String, Class> paramTypes;

    private Map<String, Object> paramValues;

    private List<TriggerCapability> nestedTrigger;

    /**
     *
     * @param name
     * @param clss
     * @param paramTypes
     * @param paramValues
     * @param nestedTrigger
     * @throws TriggerException
     */
    public TriggerCapability( String name, Class clss, List<String> paramNames, Map<String, Class> paramTypes,
                              Map<String, Object> paramValues, List<TriggerCapability> nestedTrigger )
                            throws TriggerException {
        this.name = name;
        this.clss = clss;
        this.paramTypes = paramTypes;
        this.paramValues = paramValues;
        if ( paramTypes.size() != paramValues.size() ) {
            throw new TriggerException( Messages.getMessage( "FRAMEWORK_INVALID_TRIGGERCAP_PARAMS" ) );
        }
        this.paramNames = paramNames;
        this.nestedTrigger = nestedTrigger;
    }

    /**
     * returns the triggers name
     *
     * @return the triggers name
     */
    public String getName() {
        return name;
    }

    /**
     * returns a List of the names of all init parameter assigend to a
     *
     * @see Trigger
     * @return a List of the names of all init parameter assigend to a
     * @see Trigger
     */
    public List<String> getInitParameterNames() {
        return paramNames;
    }

    /**
     * returns the value of a named init parameter
     *
     * @param name
     */
    public Object getInitParameterValue( String name ) {
        return paramValues.get( name );
    }

    /**
     * returns the type of a named init parameter
     *
     * @param name
     */
    public Class getInitParameterType( String name ) {
        return paramTypes.get( name );
    }

    /**
     * returns the Class responsible for performing trigger functionality
     *
     * @return the Class responsible for performing trigger functionality
     */
    public Class getPerformingClass() {
        return clss;
    }

    /**
     * returns a list of Triggers that a nested within this Trigger
     *
     * @return a list of Triggers that a nested within this Trigger
     */
    public List<TriggerCapability> getTrigger() {
        return nestedTrigger;
    }

    public String toString() {
        return "Trigger name: " + name;
    }

}
