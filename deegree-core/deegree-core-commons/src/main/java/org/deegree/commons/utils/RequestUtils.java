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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.commons.utils;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility class to pass request parameters implicitly through the various layers.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class RequestUtils {

	private static transient final ThreadLocal<Map<String, String>> PARAMETERS = new ThreadLocal<Map<String, String>>();

	/**
	 * This thread local can be used to store the current thread's request parameters. Use
	 * with caution, and clean up!
	 */
	public static ThreadLocal<Map<String, String>> getCurrentThreadRequestParameters() {
		return PARAMETERS;
	}

	/**
	 * Utility method that uppercases the original parameters, adds the default parameters
	 * in the map if missing, and replaces the parameters contained in the hards map.
	 */
	public static void replaceParameters(Map<String, String> map, Map<String, String> originals,
			Map<String, String> defaults, Map<String, String> hards) {
		// handle default params
		for (String def : defaults.keySet()) {
			String key = def.toUpperCase();
			if (originals.containsKey(key)) {
				map.put(key, originals.get(key));
			}
			else {
				map.put(def, defaults.get(def));
			}
		}
		// handle preset params
		for (Entry<String, String> e : hards.entrySet()) {
			if (map.containsKey(e.getKey().toLowerCase())) {
				map.put(e.getKey().toLowerCase(), e.getValue());
			}
			else
				map.put(e.getKey(), e.getValue());
		}
	}

}
