//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/
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
     * 
     * @return whether the dimension of the surface's coordinates is 3 or 2 (2 for flat surfaces; 3 for surfaces in a 3D space)
     */
    public boolean is3D();
    
    /**
     * @return the kind of SurfacePatch the object represents, an element of {@link SurfacePatchType}
     */
    public SurfacePatchType getSurfacePatchType();
    
}