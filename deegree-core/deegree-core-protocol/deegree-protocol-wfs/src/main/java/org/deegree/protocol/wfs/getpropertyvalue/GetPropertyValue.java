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
package org.deegree.protocol.wfs.getpropertyvalue;

import org.deegree.commons.tom.ResolveParams;
import org.deegree.commons.tom.ows.Version;
import org.deegree.filter.expression.ValueReference;
import org.deegree.protocol.wfs.AbstractWFSRequest;
import org.deegree.protocol.wfs.query.Query;
import org.deegree.protocol.wfs.query.StandardPresentationParams;

/**
 * Represents a <code>GetPropertyValue</code> request to a WFS.
 * <p>
 * Supported versions:
 * <ul>
 * <li>WFS 2.0.0</li>
 * </ul>
 * </p>
 *
 * @see Query
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class GetPropertyValue extends AbstractWFSRequest {

	private final StandardPresentationParams presentationParams;

	private final ResolveParams resolveParams;

	private final ValueReference valueReference;

	private final ValueReference resolvePath;

	private final Query query;

	/**
	 * Creates a new {@link GetPropertyValue} instance.
	 * @param version protocol version, must not be <code>null</code>
	 * @param handle client-generated identifier, may be <code>null</code>
	 * @param presentationParams parameters for controlling the presentation of the result
	 * set, must not be <code>null</code>
	 * @param resolveParams parameters for controlling the resolution of references of the
	 * result set, must not be <code>null</code>
	 * @param valueReference selects the nodes or child nodes of queried features to be
	 * returned, must not be <code>null</code>
	 * @param resolvePath path along which resource resolution shall be performed, may be
	 * <code>null</code> (global resource resolution mode)
	 * @param query query to be executed, must not be <code>null</code>
	 */
	public GetPropertyValue(Version version, String handle, StandardPresentationParams presentationParams,
			ResolveParams resolveParams, ValueReference valueReference, ValueReference resolvePath, Query query) {
		super(version, handle);
		this.presentationParams = presentationParams;
		this.resolveParams = resolveParams;
		this.valueReference = valueReference;
		this.resolvePath = resolvePath;
		this.query = query;
	}

	/**
	 * Returns the parameters that control the presentation of the result set.
	 * @return presentation control parameters, never <code>null</code>
	 */
	public StandardPresentationParams getPresentationParams() {
		return presentationParams;
	}

	/**
	 * Returns the parameters that control the resolution of references of the result set.
	 * @return reference resolution control parameters, never <code>null</code>
	 */
	public ResolveParams getResolveParams() {
		return resolveParams;
	}

	/**
	 * Returns the expression for selecting the returned nodes or child nodes of the
	 * queried features.
	 * @return expression for selecting the returned nodes or child nodes, never
	 * <code>null</code>
	 */
	public ValueReference getValueReference() {
		return valueReference;
	}

	/**
	 * Returns the path along which resource resolution shall be performed.
	 * @return path along which resource resolution shall be performed, or
	 * <code>null</code> (global resource resolution mode)
	 */
	public ValueReference getResolvePath() {
		return resolvePath;
	}

	/**
	 * Returns the query to be executed (determines the feature instances for the property
	 * value extraction).
	 * @return query to be executed, never <code>null</code>
	 */
	public Query getQuery() {
		return query;
	}

}
