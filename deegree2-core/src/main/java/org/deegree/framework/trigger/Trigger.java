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
public interface Trigger {

    /**
     * performs the action(s) defined by a concrete trigger. The returned value must have the same
     * structure as the passed parameter. A trigger may changes the values of the passed
     * parameter(s) but do not change their type or structure
     *
     * @param caller
     * @param values
     */
    public Object[] doTrigger( Object caller, Object... values );

    /**
     * returns the name of the Trigger. The name starts with the name of the class Trigger is
     * assigend to followed by the method from where it is called followed by its 'specific' name;
     * e.g.<br>
     * org.deegree.enterprise.servlet.OGCServletController.doService.MyTrigger
     *
     * @return the name of the Trigger.
     */
    public String getName();

    /**
     * sets the name of a Trigger
     *
     * @param name
     */
    public void setName( String name );

}
