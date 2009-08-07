//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.geometry.standard.surfacepatches;

import java.util.List;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.patches.Cylinder;

/**
 * TODO add class documentation here.
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author: ionita $
 *
 * @version $Revision: $, $Date: $
 */
public class DefaultCylinder implements Cylinder {

    private List<Points> grid;

    private int rows;

    private int columns;

    public DefaultCylinder( List<Points> grid ) {
        this.grid = grid;
        this.rows = getNumRows();
        this.columns = getNumColumns();
    }

    public DefaultCylinder( List<Points> grid, int rows, int columns ) {
        this.grid = grid;
        this.rows = rows;
        this.columns = columns;
    }

    @Override
    public GriddedSurfaceType getGriddedSurfaceType() {
        return GriddedSurfaceType.CYLINDER;
    }

    @Override
    public int getNumColumns() {
        return grid.get( 0 ).size();
    }

    @Override
    public int getNumRows() {
        return grid.size();
    }

    @Override
    public Points getRow( int rownum ) {
        return grid.get( rownum );
    }

    @Override
    public List<Points> getRows() {
        return grid;
    }

    @Override
    public Measure getArea( Unit requestedBaseUnit ) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCoordinateDimension() {
        return grid.get( 0 ).getDimension();
    }

    @Override
    public SurfacePatchType getSurfacePatchType() {
        return SurfacePatchType.GRIDDED_SURFACE_PATCH;
    }

}
