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
package org.deegree.geometry.primitive.surfacepatches;

import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.geometry.primitive.Surface;

/**
 * A {@link SurfacePatch} describes a continuous portion of a {@link Surface}.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version. $Revision$, $Date$
 */
public interface SurfacePatch {

    public enum SurfacePatchType {
        //TODO no class for it currently
        GRIDDED_SURFACE_PATCH,

        POLYGON_PATCH,

        RECTANGLE,

        TRIANGLE
    }

    /**
     * valid surface patch interpolations.
     */
    public enum Interpolation {
        /**
         * No interpolation of the surfaces
         */
        none,
        /**
         * A planar interpolation of the surfaces.
         */
        planar,
        /**
         * A spherical interpolation of the surfaces.
         */
        spherical,
        /**
         * A elliptical interpolation of the surfaces.
         */
        elliptical,
        /**
         * A conic interpolation of the surfaces.
         */
        conic,
        /**
         * A tin interpolation of the surfaces.
         */
        tin,
        /**
         * A bilinear interpolation of the surfaces.
         */
        bilinear,
        /**
         * A biquadratic interpolation of the surfaces.
         */
        biquadratic,
        /**
         * A bicubic interpolation of the surfaces.
         */
        bicubic,
        /**
         * A polynomialSpline interpolation of the surfaces.
         */
        polynomialSpline,
        /**
         * A rationalSpline interpolation of the surfaces.
         */
        rationalSpline,
        /**
         * A triangulatedSpline interpolation of the surfaces.
         */
        triangulatedSpline
    }

    /**
     *
     * @return area of a surface patch measured in units of the assigned {@link CoordinateSystem}
     */
    public double getArea();

    /**
     * Returns the coordinate dimension, i.e. the dimension of the space that the patch is embedded in.
     * 
     * @return the coordinate dimension
     */
    public int getCoordinateDimension();

    /**
     * @return the kind of SurfacePatch the object represents, an element of {@link SurfacePatchType}
     */
    public SurfacePatchType getSurfacePatchType();

}
