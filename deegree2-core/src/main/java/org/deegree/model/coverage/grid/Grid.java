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

import org.deegree.model.spatialschema.Envelope;

/**
 * the class encapsulates a describtion of a grid containing the grids size (grid envelope) and the
 * names of the grids axis.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version 1.1, $Revision$, $Date$
 *
 * @since 1.1
 */

public class Grid {

    private Envelope gridEnvelope = null;

    private String[] axisNames = null;

    /**
     * @param gridEnvelope
     * @param axisNames
     */
    public Grid( Envelope gridEnvelope, String[] axisNames ) {
        super();
        this.gridEnvelope = gridEnvelope;
        this.axisNames = axisNames;
    }

    /**
     * returns the names of the axis of the grid. A grid must have at least two dimension (axis).
     * The number of axis is identical to the dimension of the positions of the grid envelope
     *
     * @return the names of the axis of the grid.
     *
     */
    public String[] getAxisNames() {
        return axisNames;
    }

    /**
     * sets the names of the grids axis. A grid must have at least two dimension (axis). The number
     * of axis must be identical to the dimension of the positions of the grid envelope
     *
     * @param axisNames
     *
     */
    public void setAxisNames( String[] axisNames ) {
        this.axisNames = axisNames;
    }

    /**
     * returns the envelope of the grid
     *
     * @return the envelope of the grid
     *
     */
    public Envelope getGridEnvelope() {
        return gridEnvelope;
    }

    /**
     * sets the envelope of the grid
     *
     * @param gridEnvelope
     *
     */
    public void setGridEnvelope( Envelope gridEnvelope ) {
        this.gridEnvelope = gridEnvelope;
    }

}
