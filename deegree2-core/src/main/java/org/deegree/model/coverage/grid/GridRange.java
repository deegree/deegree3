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
package org.deegree.model.coverage.grid;

import java.io.Serializable;

/**
 * Specifies the range of valid coordinates for each dimension of the coverage.
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class GridRange implements Serializable {

    private static final long serialVersionUID = -7292466343852424913L;

    private int[] up = null;

    private int[] lo = null;

    /**
     * @param lo
     * @param up
     */
    public GridRange( int[] lo, int[] up ) {
        this.up = up;
        this.lo = lo;
    }

    /**
     * The valid maximum exclusive grid coordinate. The sequence contains a maximum value for each
     * dimension of the grid coverage.
     *
     * @return The valid maximum exclusive grid coordinate
     *
     */
    public int[] getUpper() {
        return up;
    }

    /**
     * The valid minimum inclusive grid coordinate. The sequence contains a minimum value for each
     * dimension of the grid coverage. The lowest valid grid coordinate is zero.
     *
     * @return The valid minimum inclusive grid coordinate
     *
     */
    public int[] getLower() {
        return lo;
    }

}
