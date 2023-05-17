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
package org.deegree.cs.refs.projections;

import java.io.Serializable;

import javax.vecmath.Point2d;

import org.deegree.commons.tom.ReferenceResolver;
import org.deegree.cs.components.IEllipsoid;
import org.deegree.cs.components.IPrimeMeridian;
import org.deegree.cs.components.IUnit;
import org.deegree.cs.coordinatesystems.IGeographicCRS;
import org.deegree.cs.exceptions.ProjectionException;
import org.deegree.cs.projections.IProjection;
import org.deegree.cs.projections.Projection;
import org.deegree.cs.refs.CRSResourceRef;

/**
 * General {@link CRSResourceRef} referncing a {@link Projection}
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public class ProjectionRef extends CRSResourceRef<IProjection> implements Serializable, IProjection {

	private static final long serialVersionUID = -5633208582212339185L;

	/**
	 * @param resolver
	 * @param uri
	 * @param baseURL
	 * @param geoCRS
	 */
	public ProjectionRef(ReferenceResolver resolver, String uri, String baseURL) {
		super(resolver, uri, baseURL);
	}

	public Point2d doProjection(IGeographicCRS geographicCRS, double lambda, double phi) throws ProjectionException {
		return getReferencedObject().doProjection(geographicCRS, lambda, phi);
	}

	public Point2d doInverseProjection(IGeographicCRS geographicCRS, double x, double y) throws ProjectionException {
		return getReferencedObject().doInverseProjection(geographicCRS, x, y);
	}

	public String getImplementationName() {
		return getReferencedObject().getImplementationName();
	}

	public boolean isConformal() {
		return getReferencedObject().isConformal();
	}

	public boolean isEqualArea() {
		return getReferencedObject().isEqualArea();
	}

	public double getScale() {
		return getReferencedObject().getScale();
	}

	public void setScale(double scale) {
		getReferencedObject().setScale(scale);
	}

	public double getScaleFactor(IGeographicCRS geographicCRS) {
		return getReferencedObject().getScaleFactor(geographicCRS);
	}

	public double getFalseEasting() {
		return getReferencedObject().getFalseEasting();
	}

	public void setFalseEasting(double newFalseEasting) {
		getReferencedObject().setFalseEasting(newFalseEasting);
	}

	public double getFalseNorthing() {
		return getReferencedObject().getFalseNorthing();
	}

	public Point2d getNaturalOrigin() {
		return getReferencedObject().getNaturalOrigin();
	}

	public IUnit getUnits() {
		return getReferencedObject().getUnits();
	}

	public IPrimeMeridian getPrimeMeridian(IGeographicCRS geographicCRS) {
		return getReferencedObject().getPrimeMeridian(geographicCRS);
	}

	public IEllipsoid getEllipsoid(IGeographicCRS geographicCRS) {
		return getReferencedObject().getEllipsoid(geographicCRS);
	}

	public double getEccentricity(IGeographicCRS geographicCRS) {
		return getReferencedObject().getEccentricity(geographicCRS);
	}

	public double getSquaredEccentricity(IGeographicCRS geographicCRS) {
		return getReferencedObject().getSquaredEccentricity(geographicCRS);
	}

	public double getSemiMajorAxis(IGeographicCRS geographicCRS) {
		return getReferencedObject().getSemiMajorAxis(geographicCRS);
	}

	public double getSemiMinorAxis(IGeographicCRS geographicCRS) {
		return getReferencedObject().getSemiMinorAxis(geographicCRS);
	}

	public boolean isSpherical(IGeographicCRS geographicCRS) {
		return getReferencedObject().isSpherical(geographicCRS);
	}

	public double getProjectionLatitude() {
		return getReferencedObject().getProjectionLatitude();
	}

	public double getProjectionLongitude() {
		return getReferencedObject().getProjectionLongitude();
	}

	public double getSinphi0() {
		return getReferencedObject().getSinphi0();
	}

	public double getCosphi0() {
		return getReferencedObject().getCosphi0();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) ? true : getReferencedObject().equals(obj);
	}

}
