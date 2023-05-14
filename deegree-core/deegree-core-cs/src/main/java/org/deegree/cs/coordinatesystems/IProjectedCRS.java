/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.cs.coordinatesystems;

import javax.vecmath.Point2d;

import org.deegree.cs.exceptions.ProjectionException;
import org.deegree.cs.projections.IProjection;

/**
 * Interface describing a ProjectedCRS
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public interface IProjectedCRS extends ICRS {

	/**
	 * @return the underlyingCRS.
	 */
	IGeographicCRS getGeographicCRS();

	/**
	 * @return the projection.
	 */
	IProjection getProjection();

	/**
	 * The actual transform method doing a projection from geographic coordinates to map
	 * coordinates.
	 * @param lambda the longitude
	 * @param phi the latitude
	 * @return the projected Point or Point(Double.NAN, Double.NAN) if an error occurred.
	 * @throws ProjectionException if the given lamba and phi coordinates could not be
	 * projected to x and y.
	 */
	public Point2d doProjection(double lambda, double phi) throws ProjectionException;

	/**
	 * Do an inverse projection from projected (map) coordinates to geographic
	 * coordinates.
	 * @param x coordinate on the map
	 * @param y coordinate on the map
	 * @return the projected Point with x = lambda and y = phi;
	 * @throws ProjectionException if the given x and y coordinates could not be inverted
	 * to lambda and phi.
	 */
	public Point2d doInverseProjection(double x, double y) throws ProjectionException;

}