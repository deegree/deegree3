/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.cs.persistence.deegree.d3;

import org.deegree.commons.tom.Object;
import org.deegree.commons.tom.ReferenceResolver;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.cs.persistence.AbstractCRSStore.RESOURCETYPE;

/**
 * The <code>CRSParser</code> holds the instances to the StAX based crs components
 * parsers.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 */
public class DeegreeReferenceResolver implements ReferenceResolver {

	private final DeegreeCRSStore store;

	private RESOURCETYPE resourceType;

	public DeegreeReferenceResolver(DeegreeCRSStore store, RESOURCETYPE resourceType) {
		this.store = store;
		this.resourceType = resourceType;
	}

	// TODO: encode type?
	@Override
	public Object getObject(String uri, String baseURL) {
		if (uri.startsWith("#")) {
			// OBJEKT-> keine REFERENZ!
			// CACHE
			return store.getCRSResource(uri.substring(1), resourceType);
		}
		throw new ReferenceResolvingException("Deegree CRS Resolver does not support remote references.");
	}

}
