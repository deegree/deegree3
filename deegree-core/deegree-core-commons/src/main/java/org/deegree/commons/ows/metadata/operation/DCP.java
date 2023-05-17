/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.commons.ows.metadata.operation;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.ows.metadata.domain.Domain;
import org.deegree.commons.utils.Pair;

/**
 * The <code>DCP</code> bean encapsulates the corresponding GetCapabilities response
 * metadata element.
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class DCP {

	private List<Pair<URL, List<Domain>>> getEndpoints;

	private List<Pair<URL, List<Domain>>> postEndpoints;

	/**
	 * Creates a new {@link DCP} instance.
	 * @param getUrl endpoint for HTTP-GET requests, can be <code>null</code>
	 * @param postUrl endpoint for HTTP-POST requests, can be <code>null</code>
	 */
	public DCP(URL getUrl, URL postUrl) {
		getEndpoints = new ArrayList<Pair<URL, List<Domain>>>();
		if (getUrl != null) {
			getEndpoints.add(new Pair<URL, List<Domain>>(getUrl, new ArrayList<Domain>()));
		}
		postEndpoints = new ArrayList<Pair<URL, List<Domain>>>();
		if (postUrl != null) {
			postEndpoints.add(new Pair<URL, List<Domain>>(postUrl, new ArrayList<Domain>()));
		}
	}

	public DCP(List<Pair<URL, List<Domain>>> getEndpoints, List<Pair<URL, List<Domain>>> postEndpoints) {
		if (getEndpoints != null) {
			this.getEndpoints = getEndpoints;
		}
		else {
			this.getEndpoints = new ArrayList<Pair<URL, List<Domain>>>();
		}
		if (postEndpoints != null) {
			this.postEndpoints = postEndpoints;
		}
		else {
			this.postEndpoints = new ArrayList<Pair<URL, List<Domain>>>();
		}
	}

	/**
	 * @return getURLs, never <code>null</code>
	 */
	public List<Pair<URL, List<Domain>>> getGetEndpoints() {
		return getEndpoints;
	}

	/**
	 * @return getPostURLs, never <code>null</code>
	 */
	public List<Pair<URL, List<Domain>>> getPostEndpoints() {
		return postEndpoints;
	}

}
