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
package org.deegree.commons.tom;

/**
 * Implementations provide the functionality to retrieve {@link Object} instances via URIs
 * (which may be document local or remote).
 * <p>
 * A local reference is always constructed as <code># + id</code>.
 * </p>
 *
 * @see Reference
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public interface ReferenceResolver {

	/**
	 * Returns the {@link Object} that is referenced by the given URI.
	 * @param uri URI that identifies the object, must not be <code>null</code>
	 * @param baseURL optional baseURL for resolving URIs that are relative URLs, may be
	 * <code>null</code>
	 * @return the referenced object or <code>null</code> if no such object exists
	 */
	public Object getObject(String uri, String baseURL);

}
