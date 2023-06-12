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
package org.deegree.metadata.ebrim;

import static org.deegree.commons.utils.StringUtils.split;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * {@link RIMType} with (query) alias.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class AliasedRIMType {

	private final RIMType type;

	private final String alias;

	private AliasedRIMType(String name, String alias) {
		type = RIMType.valueOf(name);
		this.alias = alias;
	}

	public RIMType getType() {
		return type;
	}

	public String getAlias() {
		return alias;
	}

	/**
	 * Returns {@link AliasedRIMType}s for the given qualified name.
	 * @param name qualified name of a {@link RIMType} with optional aliases, separated by
	 * underscores
	 * @return aliased registry object types, never <code>null</code> and contains at
	 * least a single entry
	 * @throws IllegalArgumentException if the input name does not refer to a known
	 * {@link RIMType}
	 */
	public static List<AliasedRIMType> valueOf(QName name) throws IllegalArgumentException {
		List<AliasedRIMType> values = null;
		String[] tokens = split(name.getLocalPart(), "_");
		if (tokens.length > 1) {
			String unaliasedName = tokens[0];
			values = new ArrayList<AliasedRIMType>(tokens.length - 1);
			for (int i = 1; i < tokens.length; i++) {
				String alias = tokens[i];
				values.add(new AliasedRIMType(unaliasedName, alias));
			}
		}
		else {
			String unaliasedName = name.getLocalPart();
			values = Collections.singletonList(new AliasedRIMType(unaliasedName, unaliasedName));
		}
		return values;
	}

	@Override
	public String toString() {
		return "{type=" + type + ", alias=" + alias + "}";
	}

}