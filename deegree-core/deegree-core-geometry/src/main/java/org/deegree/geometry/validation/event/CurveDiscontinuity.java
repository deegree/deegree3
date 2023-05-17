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

/**
 * {@link GeometryValidationEvent} that indicates that a {@link Curve} has a
 * discontinuity, i.e. the end point of one of the curve's segments does not coincide with
 * the start point of the next one.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public class CurveDiscontinuity extends AbstractGeometryValidationEvent {

	private final Curve curve;

	private final int segmentIdx;

	/**
	 * Creates a new {@link CurveDiscontinuity} instance.
	 * @param curve affected {@link Curve} geometry, must not be <code>null</code>
	 * @param segmentIdx the index of the segment with the end point (starting at 0)
	 * @param geometryParticleHierarchy list of affected geometry particles (that the
	 * curve is a part of), must not be <code>null</code>
	 */
	public CurveDiscontinuity(Curve curve, int segmentIdx, List<Object> geometryParticleHierarchy) {
		super(geometryParticleHierarchy);
		this.curve = curve;
		this.segmentIdx = segmentIdx;
	}

	/**
	 * Returns the affected {@link Curve} geometry.
	 * @return the affected curve, never <code>null</code>
	 */
	public Curve getCurve() {
		return curve;
	}

	/**
	 * Returns the index of the segment with the end point.
	 * @return index of the segment with the end point (starting at 0)
	 */
	public int getEndPointSegmentIndex() {
		return segmentIdx;
	}

}
