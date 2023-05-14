/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.services.encoding;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@link SupportedEncodings} implementation with limited encodings.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class LimitedSupportedEncodings<E extends Enum> implements SupportedEncodings<E> {

	private final Map<E, Set<String>> enabledEncodingsPerRequestType = new HashMap<E, Set<String>>();

	/**
	 * Instantiate a new {@link LimitedSupportedEncodings} instance, by default all
	 * encodings are disabled! Add enabled encodings by
	 * {@link LimitedSupportedEncodings#addEnabledEncodings(E, Set)}.
	 */
	public LimitedSupportedEncodings() {
	}

	/**
	 * Add enabled encodings for a request type.
	 * @param requestType the type of the request to add enabled requests for, never
	 * <code>null</code>
	 * @param enabledEncodingsPerRequestType a list of encodings enabled for the request
	 * type. May be empty (all encodings are disabled), but never <code>null</code>.
	 */
	public void addEnabledEncodings(E requestType, Set<String> enabledEncodingsPerRequestType) {
		this.getEnabledEncodingsPerRequestType().put(requestType, enabledEncodingsPerRequestType);
	}

	@Override
	public boolean isEncodingSupported(E requestType, String encoding) {
		if (getEnabledEncodingsPerRequestType().containsKey(requestType)) {
			Set<String> enabledEncodings = getEnabledEncodingsPerRequestType().get(requestType);
			for (String enabledEncoding : enabledEncodings) {
				if (enabledEncoding.equalsIgnoreCase(encoding))
					return true;
			}
		}
		return false;
	}

	/**
	 * @return the enabled encodings, never <code>null</code>
	 */
	public Map<E, Set<String>> getEnabledEncodingsPerRequestType() {
		return enabledEncodingsPerRequestType;
	}

}