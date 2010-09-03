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
package org.deegree.geometry.primitive.patches;

import java.util.List;

import org.deegree.geometry.points.Points;

/**
 * A {@link GriddedSurfacePatch} is a (usually non-planar) parametric {@link SurfacePatch} derived from a rectangular
 * grid in the parameter space. The rows from this grid are control points for horizontal surface curves; the columns
 * are control points for vertical surface curves.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public interface GriddedSurfacePatch extends SurfacePatch {

    /**
     * Discriminates the different types of gridded surface patches.
     */
    public enum GriddedSurfaceType {
        /** A presumably custom type of GriddedSurfaceType */
        GRIDDED_SURFACE_PATCH,

        /** A gridded surface given as a family of conic sections whose control points vary linearly. */
        CONE,
        /**
         * A gridded surface given as a family of circles whose positions vary along a set of parallel lines, keeping
         * the cross sectional horizontal curves of a constant shape.
         */
        CYLINDER,
        /**
         * A gridded surface given as a family of circles whose positions vary linearly along the axis of the sphere,
         * and whise radius varies in proportions to the cosine function of the central angle. The horizontal circles
         * resemble lines of constant latitude, and the vertical arcs resemble lines of constant longitude.
         */
        SPHERE
    }

    /**
     * Returns the type of gridded surface, the type determines the horizontal and vertical curve types used for
     * interpolation.
     *
     * @return the type of gridded surface
     */
    public GriddedSurfaceType getGriddedSurfaceType();

    /**
     * Returns the number of rows in the parameter grid.
     *
     * @return the number of rows
     */
    public int getNumRows();

    /**
     * Returns the number of columns in the parameter grid.
     *
     * @return the number of columns
     */
    public int getNumColumns();

    /**
     * Returns the specified row of the parameter grid.
     *
     * @param rownum
     *            row to be returned
     * @return the specified row
     */
    public Points getRow( int rownum );

    /**
     * Returns all rows of the parameter grid.
     *
     * @return all rows
     */
    public List<Points> getRows();
}
