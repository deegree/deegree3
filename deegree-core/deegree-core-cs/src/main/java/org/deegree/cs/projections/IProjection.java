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
package org.deegree.cs.projections;

import javax.vecmath.Point2d;

import org.deegree.cs.CRSResource;
import org.deegree.cs.components.IEllipsoid;
import org.deegree.cs.components.IPrimeMeridian;
import org.deegree.cs.components.IUnit;
import org.deegree.cs.coordinatesystems.IGeographicCRS;
import org.deegree.cs.exceptions.ProjectionException;

/**
 * Interface describing a general projection
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public interface IProjection extends CRSResource {

	/**
	 * The actual transform method doing a projection from geographic coordinates to map
	 * coordinates.
	 * @param lambda the longitude
	 * @param phi the latitude
	 * @return the projected Point or Point(Double.NAN, Double.NAN) if an error occurred.
	 * @throws ProjectionException if the given lamba and phi coordinates could not be
	 * projected to x and y.
	 */
	Point2d doProjection(IGeographicCRS geographicCRS, double lambda, double phi) throws ProjectionException;

	/**
	 * Do an inverse projection from projected (map) coordinates to geographic
	 * coordinates.
	 * @param x coordinate on the map
	 * @param y coordinate on the map
	 * @return the projected Point with x = lambda and y = phi;
	 * @throws ProjectionException if the given x and y coordinates could not be inverted
	 * to lambda and phi.
	 */
	Point2d doInverseProjection(IGeographicCRS geographicCRS, double x, double y) throws ProjectionException;

	/**
	 * @return A deegree specific name which will be used for the export of a projection.
	 */
	String getImplementationName();

	/**
	 * @return true if the projection projects conformal.
	 */
	boolean isConformal();

	/**
	 * @return true if the projection is projects equal Area.
	 */
	boolean isEqualArea();

	/**
	 * @return the scale.
	 */
	double getScale();

	/**
	 * Sets the old scale to the given scale, also adjusts the scaleFactor.
	 * @param scale the new scale
	 */
	void setScale(double scale);

	/**
	 * @return the scale*semimajor-axis, often revered to as R*k_0 in Snyder.
	 */
	double getScaleFactor(IGeographicCRS geographicCRS);

	/**
	 * @return the falseEasting.
	 */
	double getFalseEasting();

	/**
	 * sets the false easting to given value. (Used in for example transverse mercator,
	 * while setting the utm zone).
	 * @param newFalseEasting the new false easting parameter.
	 */
	void setFalseEasting(double newFalseEasting);

	/**
	 * @return the falseNorthing.
	 */
	double getFalseNorthing();

	/**
	 * @return the naturalOrigin.
	 */
	Point2d getNaturalOrigin();

	/**
	 * @return the units.
	 */
	IUnit getUnits();

	/**
	 * @return the primeMeridian of the datum.
	 */
	IPrimeMeridian getPrimeMeridian(IGeographicCRS geographicCRS);

	/**
	 * @return the ellipsoid of the datum.
	 */
	IEllipsoid getEllipsoid(IGeographicCRS geographicCRS);

	/**
	 * @return the eccentricity of the ellipsoid of the datum.
	 */
	double getEccentricity(IGeographicCRS geographicCRS);

	/**
	 * @return the eccentricity of the ellipsoid of the datum.
	 */
	double getSquaredEccentricity(IGeographicCRS geographicCRS);

	/**
	 * @return the semiMajorAxis (a) of the ellipsoid of the datum.
	 */
	double getSemiMajorAxis(IGeographicCRS geographicCRS);

	/**
	 * @return the semiMinorAxis (a) of the ellipsoid of the datum.
	 */
	double getSemiMinorAxis(IGeographicCRS geographicCRS);

	/**
	 * @return true if the ellipsoid of the datum is a sphere and not an ellipse.
	 */
	boolean isSpherical(IGeographicCRS geographicCRS);

	/**
	 * @return the projectionLatitude also known as central-latitude or
	 * latitude-of-origin, in Snyder referenced as phi_1 for azimuthal, phi_0 for other
	 * projections.
	 */
	double getProjectionLatitude();

	/**
	 * @return the projectionLongitude also known as projection-meridian or
	 * central-meridian, in Snyder referenced as lambda_0
	 */
	double getProjectionLongitude();

	/**
	 * @return the sinphi0, the sine of the projection latitude
	 */
	double getSinphi0();

	/**
	 * @return the cosphi0, the cosine of the projection latitude
	 */
	double getCosphi0();

}