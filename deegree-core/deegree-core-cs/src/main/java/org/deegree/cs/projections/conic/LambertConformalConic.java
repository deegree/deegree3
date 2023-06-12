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

package org.deegree.cs.projections.conic;

import static org.deegree.cs.utilities.ProjectionUtils.EPS10;
import static org.deegree.cs.utilities.ProjectionUtils.EPS11;
import static org.deegree.cs.utilities.ProjectionUtils.HALFPI;
import static org.deegree.cs.utilities.ProjectionUtils.QUARTERPI;
import static org.deegree.cs.utilities.ProjectionUtils.calcMFromSnyder;
import static org.deegree.cs.utilities.ProjectionUtils.calcPhiFromConformalLatitude;
import static org.deegree.cs.utilities.ProjectionUtils.length;
import static org.deegree.cs.utilities.ProjectionUtils.preCalcedThetaSeries;
import static org.deegree.cs.utilities.ProjectionUtils.tanHalfCoLatitude;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point2d;

import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.CRSResource;
import org.deegree.cs.EPSGCode;
import org.deegree.cs.components.IUnit;
import org.deegree.cs.coordinatesystems.IGeographicCRS;

/**
 * The <code>LambertConformalConic</code> projection has following properties
 * <q>(Snyder p. 104)</q>
 * <ul>
 * <li>Conic</li>
 * <li>Conformal</li>
 * <li>Parallels are unequally spaced arcs of concentric circles, more closely spaced near
 * the center of the map</li>
 * <li>Meridians are equally spaced radii of the same circles, thereby cutting paralles at
 * right angles.</li>
 * <li>Scale is true along two standard parallels, normally or along just one.</li>
 * <li>The Pole in the same hemisphere as the standard parallels is a point; other pole is
 * at infinity</li>
 * <li>Used for maps of countries and regions with predominant east-west expanse.</li>
 * <li>Presented by Lambert in 1772.</li>
 * </ul>
 * <p>
 * <q>from: http://lists.maptools.org/pipermail/proj/2003-January/000592.html</q> For
 * east-west regions, the Lambert Conformal Conic is slightly better than the Transverse
 * Mercator because of the ability to go farther in an east-west direction and still be
 * able to have "round-trip" transformation accuracy. Geodetically speaking, it is NOT as
 * good as the transverse Mercator.
 * </p>
 * <p>
 * It is known to be used by following epsg transformations:
 * <ul>
 * <li>EPSG:3034</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */

public class LambertConformalConic extends ConicProjection implements ILambertConformalConic {

	/**
	 * @param firstParallelLatitude the latitude (in radians) of the first parallel.
	 * (Snyder phi_1).
	 * @param secondParallelLatitude the latitude (in radians) of the second parallel.
	 * (Snyder phi_2).
	 * @param geographicCRS
	 * @param falseNorthing
	 * @param falseEasting
	 * @param naturalOrigin
	 * @param units
	 * @param scale
	 * @param id an identifiable instance containing information about this projection
	 */
	public LambertConformalConic(double firstParallelLatitude, double secondParallelLatitude, double falseNorthing,
			double falseEasting, Point2d naturalOrigin, IUnit units, double scale, CRSResource id) {
		super(firstParallelLatitude, secondParallelLatitude, falseNorthing, falseEasting, naturalOrigin, units, scale,
				true/* conformal */, false /* not equalArea */, id);
	}

	/**
	 * @param firstParallelLatitude the latitude (in radians) of the first parallel.
	 * (Snyder phi_1).
	 * @param secondParallelLatitude the latitude (in radians) of the second parallel.
	 * (Snyder phi_2).
	 * @param geographicCRS
	 * @param falseNorthing
	 * @param falseEasting
	 * @param naturalOrigin
	 * @param units
	 * @param scale
	 */
	public LambertConformalConic(double firstParallelLatitude, double secondParallelLatitude, double falseNorthing,
			double falseEasting, Point2d naturalOrigin, IUnit units, double scale) {
		this(firstParallelLatitude, secondParallelLatitude, falseNorthing, falseEasting, naturalOrigin, units, scale,
				new CRSIdentifiable(new EPSGCode(9802)));
	}

	/**
	 * Creates a Lambert Conformal projection with a tangential cone at the
	 * naturalOrigin.y's latitude.
	 * @param geographicCRS
	 * @param falseNorthing
	 * @param falseEasting
	 * @param naturalOrigin
	 * @param units
	 * @param scale
	 * @param id an identifiable instance containing information about this projection
	 */
	public LambertConformalConic(double falseNorthing, double falseEasting, Point2d naturalOrigin, IUnit units,
			double scale, CRSResource id) {
		this(Double.NaN, Double.NaN, falseNorthing, falseEasting, naturalOrigin, units, scale, id);
	}

	/**
	 * Creates a Lambert Conformal projection with a tangential cone at the
	 * naturalOrigin.y's latitude.
	 * @param geographicCRS
	 * @param falseNorthing
	 * @param falseEasting
	 * @param naturalOrigin
	 * @param units
	 * @param scale
	 */
	public LambertConformalConic(double falseNorthing, double falseEasting, Point2d naturalOrigin, IUnit units,
			double scale) {
		this(Double.NaN, Double.NaN, falseNorthing, falseEasting, naturalOrigin, units, scale);
	}

	/**
	 * Creates a Lambert Conformal projection with a intersecting cone at the given
	 * parallel latitudes. and a scale of 1.
	 * @param firstParallelLatitude the latitude (in radians) of the first parallel.
	 * (Snyder phi_1).
	 * @param secondParallelLatitude the latitude (in radians) of the second parallel.
	 * (Snyder phi_2).
	 * @param geographicCRS
	 * @param falseNorthing
	 * @param falseEasting
	 * @param naturalOrigin
	 * @param units
	 * @param id an identifiable instance containing information about this projection
	 */
	public LambertConformalConic(double firstParallelLatitude, double secondParallelLatitude, double falseNorthing,
			double falseEasting, Point2d naturalOrigin, IUnit units, CRSResource id) {
		this(firstParallelLatitude, secondParallelLatitude, falseNorthing, falseEasting, naturalOrigin, units, 1., id);
	}

	/**
	 * Creates a Lambert Conformal projection with a intersecting cone at the given
	 * parallel latitudes. and a scale of 1.
	 * @param firstParallelLatitude the latitude (in radians) of the first parallel.
	 * (Snyder phi_1).
	 * @param secondParallelLatitude the latitude (in radians) of the second parallel.
	 * (Snyder phi_2).
	 * @param geographicCRS
	 * @param falseNorthing
	 * @param falseEasting
	 * @param naturalOrigin
	 * @param units
	 */
	public LambertConformalConic(double firstParallelLatitude, double secondParallelLatitude, double falseNorthing,
			double falseEasting, Point2d naturalOrigin, IUnit units) {
		this(firstParallelLatitude, secondParallelLatitude, falseNorthing, falseEasting, naturalOrigin, units, 1.);
	}

	/**
	 * Creates a Lambert Conformal projection with a tangential cone at the
	 * naturalOrigin.y's latitude. And a scale of 1.
	 * @param geographicCRS
	 * @param falseNorthing
	 * @param falseEasting
	 * @param naturalOrigin
	 * @param units
	 * @param id an identifiable instance containing information about this projection
	 */
	public LambertConformalConic(double falseNorthing, double falseEasting, Point2d naturalOrigin, IUnit units,
			CRSResource id) {
		this(Double.NaN, Double.NaN, falseNorthing, falseEasting, naturalOrigin, units, 1, id);
	}

	/**
	 * Creates a Lambert Conformal projection with a tangential cone at the
	 * naturalOrigin.y's latitude. And a scale of 1.
	 * @param geographicCRS
	 * @param falseNorthing
	 * @param falseEasting
	 * @param naturalOrigin
	 * @param units
	 */
	public LambertConformalConic(double falseNorthing, double falseEasting, Point2d naturalOrigin, IUnit units) {
		this(Double.NaN, Double.NaN, falseNorthing, falseEasting, naturalOrigin, units, 1);
	}

	/**
	 *
	 * @see org.deegree.cs.projections.Projection#doInverseProjection(double, double)
	 */
	@Override
	public synchronized Point2d doInverseProjection(IGeographicCRS geographicCRS, double x, double y) {
		Map<PARAMS, Double> params = calulateParameters(geographicCRS);
		double n = params.get(PARAMS.n);
		double rho0 = params.get(PARAMS.rho0);
		double largeF = params.get(PARAMS.largeF);

		/**
		 * used for the calculation of phi (in the inverse projection with an ellipsoid)
		 * by applying the pre calculated values to the series of Snyder (p.15 3-5), thus
		 * avoiding iteration.
		 */
		double[] preCalcedPhiSeries = preCalcedThetaSeries(getSquaredEccentricity(geographicCRS));

		Point2d out = new Point2d(0, 0);
		x -= getFalseEasting();
		y -= getFalseNorthing();
		// why divide by the scale????
		x /= getScaleFactor(geographicCRS);
		y = rho0 - (y / getScaleFactor(geographicCRS));
		double rho = length(x, y);
		if (rho > EPS11) {
			if (n < 0.0) {
				// if using the atan2 the values must be inverted.
				rho = -rho;
				x = -x;
				y = -y;
			}
			if (isSpherical(geographicCRS)) {
				// Snyder (p.107 15-5).
				out.y = (2.0 * Math.atan(Math.pow(largeF / rho, 1.0 / n))) - HALFPI;
			}
			else {
				// out.y = MapUtils.phi2( Math.pow( rho / largeF, 1.0 / n ),
				// getEccentricity() );
				double t = Math.pow(rho / largeF, 1.0 / n);
				double chi = HALFPI - (2 * Math.atan(t));
				out.y = calcPhiFromConformalLatitude(chi, preCalcedPhiSeries);
			}
			// Combine Snyder (P.107/109 14-9) with (p.107/109 14-11), please pay
			// attention to the remark of snyder on
			// the atan2 at p.107!!!
			out.x = Math.atan2(x, y) / n;
		}
		else {
			out.x = 0;
			out.y = (n > 0.0) ? HALFPI : -HALFPI;
		}
		out.x += getProjectionLongitude();
		return out;
	}

	/**
	 *
	 * @see org.deegree.cs.projections.Projection#doProjection(double, double)
	 */
	@Override
	public synchronized Point2d doProjection(IGeographicCRS geographicCRS, double lambda, double phi) {
		Map<PARAMS, Double> params = calulateParameters(geographicCRS);
		double n = params.get(PARAMS.n);
		double rho0 = params.get(PARAMS.rho0);
		double largeF = params.get(PARAMS.largeF);

		lambda -= getProjectionLongitude();
		double rho = 0;
		if (Math.abs(Math.abs(phi) - HALFPI) > EPS10) {
			// For spherical see Snyder (p.106 15-1) for ellipitical Snyder (p.108 15-7),
			// pay attention to the '-n'
			rho = largeF * (isSpherical(geographicCRS) ? Math.pow(Math.tan(QUARTERPI + (.5 * phi)), -n)
					: Math.pow(tanHalfCoLatitude(phi, Math.sin(phi), getEccentricity(geographicCRS)), n));
		}
		// calc theta Snyder (p.106/108 14-4) multiply lambda with the 'n' constant.
		double theta = lambda * n;

		Point2d out = new Point2d(0, 0);
		out.x = getScaleFactor(geographicCRS) * (rho * Math.sin(theta)) + getFalseEasting();
		out.y = getScaleFactor(geographicCRS) * (rho0 - (rho * Math.cos(theta))) + getFalseNorthing();
		return out;
	}

	@Override
	public String getImplementationName() {
		return "lambertConformalConic";
	}

	private enum PARAMS {

		n, rho0, largeF

	}

	private synchronized Map<PARAMS, Double> calulateParameters(IGeographicCRS geographicCRS) {
		Map<PARAMS, Double> params = new HashMap<LambertConformalConic.PARAMS, Double>();
		/**
		 * Will contain snyder's variable 'n' from formula (15-3) for the spherical
		 * projection or (15-8) for the ellipsoidal projection.
		 */
		double n;
		/**
		 * Snyder (p.108 15-7). or 0 if the projectionlatitude is on one of the poles
		 * e.g.± pi*0.5.
		 */
		double rho0;
		/**
		 * Will contain snyder's variable 'F' from formula (15-2) for the spherical
		 * projection or (15-10) for the ellipsoidal projection.
		 */
		double largeF;

		double cosphi, sinphi;
		boolean secant;
		// If only one tangential parallel is used, the firstparallelLatitude will also
		// have the same value as the
		// projectionLatitude, in this case the constant 'n' from Snyder will have the
		// value sin(phi).
		n = sinphi = Math.sin(getFirstParallelLatitude());
		cosphi = Math.cos(getFirstParallelLatitude());
		secant = Math.abs(getFirstParallelLatitude() - getSecondParallelLatitude()) >= EPS10;
		if (isSpherical(geographicCRS)) {
			if (secant) {
				// two parallels are used, calc snyder (p.107 15-3), else n will contain
				// sin(firstParallelLatitude),
				// according to Snyder (p.107 just before 15-4).
				n = Math.log(cosphi / Math.cos(getSecondParallelLatitude()))
						/ Math.log(Math.tan(QUARTERPI + (.5 * getSecondParallelLatitude()))
								/ Math.tan(QUARTERPI + (.5 * getFirstParallelLatitude())));
			}
			// Snyder (p.107 15-2)
			largeF = (cosphi * Math.pow(Math.tan(QUARTERPI + (.5 * getFirstParallelLatitude())), n)) / n;

			// Snyder (p.106 15-1a) pay attention to the '-n' power term...
			rho0 = (Math.abs(Math.abs(getProjectionLatitude()) - HALFPI) < EPS10) ? 0.
					: largeF * Math.pow(Math.tan(QUARTERPI + (.5 * getProjectionLatitude())), -n);
		}
		else {

			// Calc
			double m1 = calcMFromSnyder(sinphi, cosphi, getSquaredEccentricity(geographicCRS));
			double t1 = tanHalfCoLatitude(getFirstParallelLatitude(), sinphi, getEccentricity(geographicCRS));
			if (secant) {
				sinphi = Math.sin(getSecondParallelLatitude());
				cosphi = Math.cos(getSecondParallelLatitude());
				// Basic math, the log ( x/ y ) = log(x) - log(y) if the base is the same.
				n = Math.log(m1 / calcMFromSnyder(sinphi, cosphi, getSquaredEccentricity(geographicCRS)));
				n /= Math
					.log(t1 / tanHalfCoLatitude(getSecondParallelLatitude(), sinphi, getEccentricity(geographicCRS)));
			}
			if (Math.abs(n) > EPS11) {
				// Snyder (p.108 15-10), n will contain sin(getFirstLatitudePhi()) if only
				// a tangential cone is used.
				largeF = (m1 * Math.pow(t1, -n)) / n;

				// Snyder (p.108 15-7). or 0 if the projectionlatitude is on one of the
				// poles e.g.± pi*0.5.
				rho0 = (Math.abs(Math.abs(getProjectionLatitude()) - HALFPI) < EPS10) ? 0. : largeF * Math
					.pow(tanHalfCoLatitude(getProjectionLatitude(), getSinphi0(), getEccentricity(geographicCRS)), n);
			}
			else {
				throw new IllegalArgumentException(
						"'n' may not  be '0', did you configure the lambert conformal conic with id: "
								+ getCode().getOriginal() + " correctly?");
			}
		}
		params.put(PARAMS.n, n);
		params.put(PARAMS.rho0, rho0);
		params.put(PARAMS.largeF, largeF);
		return params;
	}

}
