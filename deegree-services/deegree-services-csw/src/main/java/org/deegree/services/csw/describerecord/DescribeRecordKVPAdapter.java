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
package org.deegree.services.csw.describerecord;

import java.util.Collections;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.services.csw.AbstractCSWKVPAdapter;

/**
 * Encapsulates the method for parsing a {@link DescribeRecord} kvp request via Http-GET.
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class DescribeRecordKVPAdapter extends AbstractCSWKVPAdapter {

	/**
	 * Parses the {@link DescribeRecord} KVP request and decides which version has to
	 * parse because of the requested version.
	 * @param normalizedKVPParams that are requested as key to a value.
	 * @return {@link DescribeRecord}
	 */
	public static DescribeRecord parse(Map<String, String> normalizedKVPParams) {
		Version version = Version.parseVersion(KVPUtils.getRequired(normalizedKVPParams, "VERSION"));
		return parse202(version, normalizedKVPParams);
	}

	/**
	 * Parses the {@link DescribeRecord} request on the basis of CSW version 2.0.2
	 * @param version that is requested, 2.0.2
	 * @param normalizedKVPParams that are requested containing all mandatory and optional
	 * parts regarding CSW spec
	 * @return {@link DescribeRecord}
	 */
	private static DescribeRecord parse202(Version version, Map<String, String> normalizedKVPParams) {

		// NAMESPACE (optional)
		Map<String, String> nsBindings = extractNamespaceBindings(normalizedKVPParams);
		if (nsBindings == null) {
			nsBindings = Collections.emptyMap();
		}

		NamespaceBindings nsContext = new NamespaceBindings();
		if (nsBindings != null) {
			for (String key : nsBindings.keySet()) {
				nsContext.addNamespace(key, nsBindings.get(key));
			}
		}

		// typeName (optional)
		QName[] typeNames = extractTypeName(normalizedKVPParams, nsBindings);

		// outputFormat (optional)
		String outputFormat = KVPUtils.getDefault(normalizedKVPParams, "outputFormat", "application/xml");

		// schemaLanguage (optional)
		String schemaLanguage = KVPUtils.getDefault(normalizedKVPParams, "schemaLanguage",
				"http://www.w3.org/XML/Schema");

		return new DescribeRecord(version, nsContext, typeNames, outputFormat, schemaLanguage);
	}

	private static QName[] extractTypeName(Map<String, String> kvpParam, Map<String, String> nsBindings)
			throws InvalidParameterValueException {
		QName[] typeNames = null;
		String typeNameString = kvpParam.get("TYPENAME");
		if (typeNameString != null) {
			String[] typeNameStrings = typeNameString.split(",");
			typeNames = new QName[typeNameStrings.length];
			for (int i = 0; i < typeNameStrings.length; i++) {
				typeNames[i] = qualifyName(typeNameStrings[i], nsBindings);
			}
		}
		return typeNames;
	}

}
