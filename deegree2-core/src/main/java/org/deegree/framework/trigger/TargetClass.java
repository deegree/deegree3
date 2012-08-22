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
public class TargetClass {

    private String name;

    private Map<String, TargetMethod> methods;

    /**
     *
     * @param name
     * @param methods
     */
    public TargetClass( String name, Map<String, TargetMethod> methods ) {
        this.name = name;
        this.methods = methods;
    }

    /**
     * returns a list of decription of all methods of a class that may assignes one or more
     *
     * @see Trigger
     * @return a list of decription of all methods of a class that may assignes one or more
     * @see Trigger
     */
    public List<TargetMethod> getMethods() {
        List<TargetMethod> list = new ArrayList<TargetMethod>( methods.size() );
        Iterator iter = methods.values().iterator();
        while ( iter.hasNext() ) {
            list.add( (TargetMethod) iter.next() );
        }
        return list;
    }

    /**
     * returns a method of a TargetClass identified by its name
     *
     * @param name
     * @return a method of a TargetClass identified by its name
     */
    public TargetMethod getMethod( String name ) {
        return methods.get( name );
    }

    /**
     * returns the name of the class
     *
     * @return the name of the class
     */
    public String getName() {
        return name;
    }

}
