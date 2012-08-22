// $HeadURL$
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
package org.deegree.ogcwebservices.wcs.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A <tt>Range</tt> defines the range of variable values like time or elevation for which the
 * coverages assigned to a <tt>Range</tt> are valid. The valid values are given by the
 * <tt>Axis</tt> of a <tt>Range</tt>. A <tt>Range</tt> can have as much <tt>Axis</tt> and
 * so as much filter dimensions as desired. If a <tt>Range</tt> doesn't have explicit
 * <tt>Axis</tt> they are implicit coded in the assigned <tt>Directory</tt> or </tt>File</tt>
 * name property.
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class Range {

    private String name = null;

    private List<Axis> axis = null;

    /**
     * @param name
     *            name of the <tt>Range</tt>
     */
    public Range( String name ) {
        this.name = name;
    }

    /**
     * @param name
     *            name of the <tt>Range</tt>
     * @param axis
     *            list of <tt>Axis</tt> (filter dimensions) assigned to the <tt>Range</tt>
     */
    public Range( String name, Axis[] axis ) {
        this.name = name;
        setAxis( axis );
    }

    /**
     * returns the list of <tt>Axis</tt> (filter dimensions) assigned to the <tt>Range</tt>
     *
     * @return Returns the axis.
     *
     */
    public Axis[] getAxis() {
        return axis.toArray( new Axis[axis.size()] );
    }

    /**
     * sets the list of <tt>Axis</tt> (filter dimensions) assigned to the <tt>Range</tt>
     *
     * @param axis
     *            The axis to set.
     */
    public void setAxis( Axis[] axis ) {
        this.axis = new ArrayList<Axis>( Arrays.asList( axis ) );
    }

    /**
     * adds an <tt>Axis</tt> to the Range
     *
     * @param axis
     */
    public void addAxis( Axis axis ) {
        this.axis.add( axis );
    }

    /**
     * removes an <tt>Axis</tt> from the Range
     *
     * @param axis
     */
    public void removeAxis( Axis axis ) {
        this.axis.remove( axis );
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName( String name ) {
        this.name = name;
    }

}
