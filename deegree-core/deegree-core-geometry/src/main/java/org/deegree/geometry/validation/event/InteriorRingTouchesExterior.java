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

import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.patches.PolygonPatch;

/**
 * {@link GeometryValidationEvent} that indicates that a planar surface patch
 * (={@link PolygonPatch}) has a hole (interior ring) that intersects it's shell (exterior
 * ring).
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class InteriorRingTouchesExterior extends AbstractGeometryValidationEvent {

	private final PolygonPatch patch;

	private final int ringIdx;

	private final Point location;

	/**
	 * Creates a new {@link InteriorRingTouchesExterior} instance.
	 * @param patch offending patch, never <code>null</code>
	 * @param ringIdx index of the offending inner ring (starting at 0)
	 * @param location location of the intersection, may be <code>null</code>
	 * @param affectedGeometryParticles list of affected geometry components (that the
	 * patch is part of)
	 */
	public InteriorRingTouchesExterior(PolygonPatch patch, int ringIdx, Point location,
			List<Object> geometryParticleHierarchy) {
		super(geometryParticleHierarchy);
		this.patch = patch;
		this.ringIdx = ringIdx;
		this.location = location;
	}

	/**
	 * Returns the affected {@link PolygonPatch} geometry.
	 * @return affected patch, never <code>null</code>
	 */
	public PolygonPatch getPatch() {
		return patch;
	}

	/**
	 * Returns the index of the affected interior ring.
	 * @return index of affected interior ring (starting at 0)
	 */
	public int getRingIdx() {
		return ringIdx;
	}

	/**
	 * Returns the location of the intersection.
	 * @return location of intersection, may be <code>null</code>
	 */
	public Point getLocation() {
		return location;
	}

}
