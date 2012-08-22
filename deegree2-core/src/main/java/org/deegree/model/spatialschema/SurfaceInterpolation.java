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

package org.deegree.model.spatialschema;

/**
 *
 * Defining the different kind of surface interpolation known by the iso geometry modell.
 *
 * <p>
 * -----------------------------------------------------
 * </p>
 *
 * @author Andreas Poth
 * @version $Revision$ $Date$
 *          <p>
 */

public interface SurfaceInterpolation {

    /**
     * Defining no interpolation.
     */
    public static final int NONE = 0;

    /**
     * Defining a planar interpolation.
     */
    public static final int PLANAR = 1;

    /**
     * Defining a spherical interpolation.
     */
    public static final int SPHERICAL = 2;

    /**
     * Defining a elliptical interpolation.
     */
    public static final int ELLIPTICAL = 3;

    /**
     * Defining a conical interpolation.
     */
    public static final int CONIC = 4;

    /**
     * Defining a tin interpolation.
     */
    public static final int TIN = 5;

    /**
     * Defining a bilinear interpolation.
     */
    public static final int BILINEAR = 6;

    /**
     * Defining a biquadratic interpolation.
     */
    public static final int BIQUADRATIC = 7;

    /**
     * Defining a bicubic interpolation.
     */
    public static final int BICUBIC = 8;

    /**
     * Defining a polynomial-spline interpolation.
     */
    public static final int POLYNOMIALSPLINE = 9;

    /**
     * Defining a rotational-spline interpolation.
     */
    public static final int RATIONALSPLINE = 10;

    /**
     * Defining a triangulated-so-line interpolation.
     */
    public static final int TRIANGULATEDSOLINE = 11;

    /**
     * @return one of the defined values of the interpolation.
     */
    int getValue();

}
