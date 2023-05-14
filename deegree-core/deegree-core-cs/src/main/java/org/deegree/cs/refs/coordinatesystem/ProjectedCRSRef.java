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
package org.deegree.cs.refs.coordinatesystem;

import javax.vecmath.Point2d;

import org.deegree.commons.tom.ReferenceResolver;
import org.deegree.cs.coordinatesystems.IGeographicCRS;
import org.deegree.cs.coordinatesystems.IProjectedCRS;
import org.deegree.cs.coordinatesystems.ProjectedCRS;
import org.deegree.cs.exceptions.ProjectionException;
import org.deegree.cs.projections.IProjection;

/**
 * {@link CRSRef} to a {@link ProjectedCRS}
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public class ProjectedCRSRef extends CRSRef implements IProjectedCRS {

	private static final long serialVersionUID = -2737283787204180484L;

	/**
	 * Creates a reference to a {@link ProjectedCRS}
	 * @param resolver used for resolving the reference, must not be <code>null</code>
	 * @param uri the object's uri, must not be <code>null</code>
	 * @param baseURL base URL for resolving the uri, may be <code>null</code> (no
	 * resolving of relative URLs)
	 */
	public ProjectedCRSRef(ReferenceResolver resolver, String uri, String baseURL) {
		super(resolver, uri, baseURL);
	}

	@Override
	public ProjectedCRS getReferencedObject() {
		return (ProjectedCRS) super.getReferencedObject();
	}

	public IGeographicCRS getGeographicCRS() {
		return getReferencedObject().getGeographicCRS();
	}

	public IProjection getProjection() {
		return getReferencedObject().getProjection();
	}

	public Point2d doProjection(double lambda, double phi) throws ProjectionException {
		return getReferencedObject().doProjection(lambda, phi);
	}

	public Point2d doInverseProjection(double x, double y) throws ProjectionException {
		return getReferencedObject().doInverseProjection(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) ? true : getReferencedObject().equals(obj);
	}

}