/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2026 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.protocol.wfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class KvpTestUtils {

	/**
	 * Reads a text file with KVP content into a map.
	 * <p>
	 * Example contents:
	 *
	 * <pre>
	 * SERVICE=WFS
	 * VERSION=1.1.0
	 * REQUEST=DescribeFeatureType
	 * TYPENAME=TreesA_1M
	 * </pre>
	 *
	 * What this method does:
	 * <ul>
	 * <li>Every line is split around the '=' character, the first part is used as the
	 * key, the second part as the value (if the line doesn't contain a '=', it is
	 * ignored).</li>
	 * <li>Keys are uppercased.</li>
	 * <li>Values are URL decoded.</li>
	 * </ul>
	 * @param inputStream to parse
	 * @return map with the contents of the file, keys are uppercase
	 * @throws IOException if the file cannot be loaded
	 */
	public static Map<String, String> readFileIntoMap(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		String line;
		Map<String, String> params = new HashMap<>();
		while ((line = reader.readLine()) != null) {
			if (line.contains("=")) {
				String[] parts = line.split("=");
				if (parts[0].equalsIgnoreCase("FILTER")) {
					params.put("FILTER", URLDecoder.decode(line.substring(7), "UTF-8"));
				}
				else if (parts[0].equalsIgnoreCase("NAMESPACE")) {
					params.put("NAMESPACE", line.substring(10));
				}
				else if (parts.length == 2) {
					params.put(parts[0].toUpperCase(), URLDecoder.decode(parts[1], "UTF-8"));
				}
				else {
					throw new IllegalArgumentException();
				}
			}
		}
		return params;
	}

}
