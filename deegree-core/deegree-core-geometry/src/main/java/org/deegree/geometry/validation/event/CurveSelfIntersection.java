/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.geometry.validation.event;

import java.util.List;

import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;

/**
 * {@link GeometryValidationEvent} that indicates that a {@link Curve} intersects itself.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class CurveSelfIntersection extends AbstractGeometryValidationEvent {

	private final Curve curve;

	private final Point location;

	/**
	 * Creates a new {@link CurveSelfIntersection} instance.
	 * @param curve affected {@link Curve} geometry, must not be <code>null</code>
	 * @param location the location of the self-intersection, may be <code>null</code>
	 * @param geometryParticleHierarchy list of affected geometry particles (that the
	 * curve is a part of), must not be <code>null</code>
	 */
	public CurveSelfIntersection(Curve curve, Point location, List<Object> geometryParticleHierarchy) {
		super(geometryParticleHierarchy);
		this.curve = curve;
		this.location = location;
	}

	/**
	 * Returns the affected {@link Curve} geometry.
	 * @return the affected curve, never <code>null</code>
	 */
	public Curve getCurve() {
		return curve;
	}

	/**
	 * Returns the location of the self-intersection.
	 * @return location of self-intersection, never <code>null</code>
	 */
	public Point getLocation() {
		return location;
	}

}
