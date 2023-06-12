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

package org.deegree.protocol.wfs.getpropertyvalue.kvp;

import static org.deegree.commons.utils.kvp.KVPUtils.getRequired;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.deegree.commons.tom.ResolveParams;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.filter.expression.ValueReference;
import org.deegree.protocol.wfs.getpropertyvalue.GetPropertyValue;
import org.deegree.protocol.wfs.query.Query;
import org.deegree.protocol.wfs.query.StandardPresentationParams;
import org.deegree.protocol.wfs.query.kvp.QueryKVPAdapter;

/**
 * Adapter between KVP <code>GetPropertyValue</code> requests and {@link GetPropertyValue}
 * objects.
 * <p>
 * Supported WFS versions:
 * <ul>
 * <li>2.0.0</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class GetPropertyValueKVPAdapter extends QueryKVPAdapter {

	/**
	 * Parses a normalized KVP-map as a WFS {@link GetPropertyValue} request.
	 * @param kvpParams normalized KVP-map; keys must be uppercase, each key only has one
	 * associated value
	 * @return parsed {@link GetPropertyValue} request
	 * @throws Exception
	 */
	public static GetPropertyValue parse(Map<String, String> kvpParams) throws Exception {

		Version version = Version.parseVersion(KVPUtils.getRequired(kvpParams, "VERSION"));

		GetPropertyValue result = null;

		if (VERSION_200.equals(version)) {
			result = parse200(kvpParams);
		}
		else {
			String msg = "Version '" + version
					+ "' is not supported for GetPropertyValue requests. The only supported version is 2.0.0.";
			throw new Exception(msg);
		}
		return result;
	}

	private static GetPropertyValue parse200(Map<String, String> kvpParams) throws Exception {

		// optional: 'NAMESPACE'
		String namespaceValue = kvpParams.get("NAMESPACE");
		if (namespaceValue == null) {
			namespaceValue = kvpParams.get("NAMESPACES");
		}

		Map<String, String> nsBindings = extractNamespaceBindings200(namespaceValue);
		if (nsBindings == null) {
			nsBindings = Collections.emptyMap();
		}

		NamespaceBindings nsContext = new NamespaceBindings();
		if (nsBindings != null) {
			for (String key : nsBindings.keySet()) {
				nsContext.addNamespace(key, nsBindings.get(key));
			}
		}

		StandardPresentationParams presentationParams = parseStandardPresentationParameters200(kvpParams);

		ResolveParams resolveParams = parseStandardResolveParameters200(kvpParams);

		// mandatory: VALUEREFERENCE
		ValueReference valueReference = new ValueReference(getRequired(kvpParams, "VALUEREFERENCE"), nsContext);

		// optional: RESOLVEPATH
		ValueReference resolvePath = null;
		String resolvePathStr = kvpParams.get("RESOLVEPATH");
		if (resolvePathStr != null) {
			resolvePath = new ValueReference(resolvePathStr, nsContext);
		}

		List<Query> queries = parseQueries200(kvpParams, resolveParams);
		if (queries.size() > 1) {
			String msg = "Multiple queries for GetPropertyValue requests are not allowed.";
			throw new Exception(msg);
		}

		return new GetPropertyValue(VERSION_200, null, presentationParams, resolveParams, valueReference, resolvePath,
				queries.get(0));
	}

}
