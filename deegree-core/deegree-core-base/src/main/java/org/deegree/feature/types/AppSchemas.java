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

package org.deegree.feature.types;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.property.PropertyType;

/**
 * <code>AppSchemas</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */

public class AppSchemas {

	/**
	 * Collects all property names of a given feature type, or all feature types if the
	 * given name is null.
	 * @param schema
	 * @param featureType
	 * @return a set of property qnames
	 */
	public static Set<QName> collectProperyNames(AppSchema schema, QName featureType) {
		HashSet<QName> set = new HashSet<QName>();

		for (FeatureType ft : schema.getFeatureTypes()) {
			if (featureType == null || featureType.equals(ft.getName())) {
				for (PropertyType pt : ft.getPropertyDeclarations()) {
					set.add(pt.getName());
				}
			}
		}

		return set;
	}

}
