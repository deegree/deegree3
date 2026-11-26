/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2026 by:
 - Department of Geography, University of Bonn -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -
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

 website: https://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.commons.xml.schema.resolver;

import java.util.regex.Pattern;

public class RedirectBrokenSchemaResolver implements SchemaResolver {

	@SuppressWarnings("HttpUrlsUsage")
	private static final String OASIS_SCHEMAS_URL = "http://docs.oasis-open.org/";

	@SuppressWarnings("HttpUrlsUsage")
	public static final String INSPIRE_SCHEMAS_URL = "http://inspire.ec.europa.eu/schemas";

	private static final Pattern HTTP_PATTERN = Pattern.compile("http://");

	@Override
	public String redirect(String systemId) {
		if (systemId == null) {
			return systemId;
		}
		if (systemId.startsWith(INSPIRE_SCHEMAS_URL) || systemId.startsWith(OASIS_SCHEMAS_URL)) {
			return HTTP_PATTERN.matcher(systemId).replaceFirst("https://");
		}
		return systemId;
	}

	@Override
	public int getOrder() {
		return -10;
	}

}
