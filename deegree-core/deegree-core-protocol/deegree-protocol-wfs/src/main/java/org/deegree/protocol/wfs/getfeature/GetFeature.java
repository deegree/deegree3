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
package org.deegree.protocol.wfs.getfeature;

import java.util.List;

import org.deegree.commons.tom.ResolveParams;
import org.deegree.commons.tom.ows.Version;
import org.deegree.protocol.wfs.AbstractWFSRequest;
import org.deegree.protocol.wfs.query.Query;
import org.deegree.protocol.wfs.query.StandardPresentationParams;

/**
 * Represents a <code>GetFeature</code> request to a WFS.
 * <p>
 * Supported versions:
 * <ul>
 * <li>WFS 1.0.0</li>
 * <li>WFS 1.1.0</li>
 * <li>WFS 2.0.0</li>
 * </ul>
 * </p>
 *
 * @see Query
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class GetFeature extends AbstractWFSRequest {

	private final StandardPresentationParams presentationParams;

	private final ResolveParams resolveParams;

	private final List<Query> queries;

	/**
	 * Creates a new {@link GetFeature} request.
	 * @param version protocol version, must not be <code>null</code>
	 * @param handle client-generated identifier, may be <code>null</code>
	 * @param presentationParams parameters for controlling the presentation of the result
	 * set, may be <code>null</code>
	 * @param resolveParams parameters for controlling the resolution of references of the
	 * result set, may be <code>null</code>
	 * @param queries the queries to be performed in the request, must not be
	 * <code>null</code> and must contain at least one entry
	 */
	public GetFeature(Version version, String handle, StandardPresentationParams presentationParams,
			ResolveParams resolveParams, List<Query> queries) {
		super(version, handle);
		if (presentationParams != null) {
			this.presentationParams = presentationParams;
		}
		else {
			this.presentationParams = new StandardPresentationParams(null, null, null, null);
		}
		if (resolveParams != null) {
			this.resolveParams = resolveParams;
		}
		else {
			this.resolveParams = new ResolveParams(null, null, null);
		}
		this.queries = queries;
	}

	/**
	 * Returns the parameters that control the presentation of the result set.
	 * @return presentation control parameters, never <code>null</code>
	 */
	public StandardPresentationParams getPresentationParams() {
		return presentationParams;
	}

	/**
	 * Returns the parameters that control the resolution of references in the response.
	 * @return reference resolution control parameters, never <code>null</code>
	 */
	public ResolveParams getResolveParams() {
		return resolveParams;
	}

	/**
	 * The queries to be performed in the request.
	 * @return the queries to be performed, never <code>null</code> and must contain at
	 * least one entry
	 */
	public List<Query> getQueries() {
		return queries;
	}

}
