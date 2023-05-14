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

import org.deegree.commons.tom.ReferenceResolver;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.cs.projections.cylindric.ITransverseMercator;
import org.deegree.cs.projections.cylindric.TransverseMercator;

/**
 * {@link ProjectionRef} to a {@link TransverseMercator}
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public class TransverseMercatorRef extends ProjectionRef implements Serializable, ITransverseMercator {

	private static final long serialVersionUID = 179754691716158093L;

	/**
	 * Creates a reference to {@link TransverseMercator}
	 * @param resolver used for resolving the reference, must not be <code>null</code>
	 * @param uri the object's uri, must not be <code>null</code>
	 * @param baseURL base URL for resolving the uri, may be <code>null</code> (no
	 * resolving of relative URLs)
	 */
	public TransverseMercatorRef(ReferenceResolver resolver, String uri, String baseURL) {
		super(resolver, uri, baseURL);
	}

	public int getRowFromNearestParallel(double latitude) {
		return getReferencedObject().getRowFromNearestParallel(latitude);
	}

	public int getZoneFromNearestMeridian(double longitude) {
		return getReferencedObject().getZoneFromNearestMeridian(longitude);
	}

	public boolean getHemisphere() {
		return getReferencedObject().getHemisphere();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) ? true : getReferencedObject().equals(obj);
	}

	@Override
	public TransverseMercator getReferencedObject() throws ReferenceResolvingException {
		return (TransverseMercator) super.getReferencedObject();
	}

}