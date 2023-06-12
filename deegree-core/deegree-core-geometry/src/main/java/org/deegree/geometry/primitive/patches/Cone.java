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

/**
 * The <code>Cone</code> class represents (according to GML-3.1 spec ) a gridded surface
 * given as a family of conic sections whose control points vary linearly. A 5-point
 * ellipse with all defining positions identical is a point. Thus, a truncated elliptical
 * cone can be given as a 2x5 set of control points <<P1, P1, P1, P1, P1>, <P2, P3, P4,
 * P5, P6>>. P1 is the apex of the cone. P2, P3, P4, P5 and P6 are any five distinct
 * points around the base ellipse of the cone. If the horizontal curves are circles as
 * opposed to ellipses, the circular cone can be constructed using <<P1, P1, P1>, <P2, P3,
 * P4>>.
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public interface Cone extends GriddedSurfacePatch {

	// nothing new here, this interface is only necessary for a type-based differentiation

	/**
	 * Must always return {@link GriddedSurfacePatch.GriddedSurfaceType#CONE}.
	 * @return {@link GriddedSurfacePatch.GriddedSurfaceType#CONE}
	 */
	@Override
	public GriddedSurfaceType getGriddedSurfaceType();

}
