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
package org.deegree.geometry.primitive.segments;

/**
 * Defines allowed values for the knots' type. Uniform knots implies that all knots are of multiplicity 1 and they
 * differ by a positive constant from the preceding knot. Knots are quasi-uniform iff they are of multiplicity (degree +
 * 1) at the ends, of multiplicity 1 elsewhere, and they differ by a positive constant from the preceding knot.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public enum KnotType {

    /**
     *
     */
    UNSPECIFIED,

    /**
     * All knots are of multiplicity 1 and they differ by a positive constant from the preceding knot.
     */
    UNIFORM,

    /**
     * Multiplicity of the knots is (degree + 1) at the ends, 1 elsewhere, and knots differ by a positive constant from
     * the preceding knot.
     */
    QUASI_UNIFORM,

    /**
     * ???
     */
    BEZIER

}
