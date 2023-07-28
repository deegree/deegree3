/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

package org.deegree.filter.projection;

import org.deegree.commons.tom.ResolveParams;
import org.deegree.filter.expression.ValueReference;

/**
 * {@link ProjectionClause} that is based on {@link ValueReference}s.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class PropertyName implements ProjectionClause {

	private final ValueReference propertyName;

	private final ResolveParams resolveParams;

	private final ValueReference resolvePath;

	/**
	 * Creates a new {@link PropertyName} instance.
	 * @param propertyName name of the targeted property, must not be <code>null</code>
	 * @param resolveParams parameters for controlling the resolution of references of the
	 * result set, may be <code>null</code>
	 * @param resolvePath ....may be <code>null</code>
	 */
	public PropertyName(ValueReference propertyName, ResolveParams resolveParams, ValueReference resolvePath) {
		this.propertyName = propertyName;
		if (resolveParams != null) {
			this.resolveParams = resolveParams;
		}
		else {
			this.resolveParams = new ResolveParams(null, null, null);
		}
		this.resolvePath = resolvePath;
	}

	/**
	 * Returns the targeted property name.
	 * @return the targeted property name, never <code>null</code>
	 */
	public ValueReference getPropertyName() {
		return propertyName;
	}

	/**
	 * Returns the parameters that control the resolution of references in the response.
	 * @return reference resolution control parameters, never <code>null</code>
	 */
	public ResolveParams getResolveParams() {
		return resolveParams;
	}

	public ValueReference getResolvePath() {
		return resolvePath;
	}

}
