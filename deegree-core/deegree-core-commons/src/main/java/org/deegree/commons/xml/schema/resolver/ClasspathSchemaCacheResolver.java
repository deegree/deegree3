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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class ClasspathSchemaCacheResolver implements SchemaResolver {

	private static final Logger LOG = LoggerFactory.getLogger(ClasspathSchemaCacheResolver.class);

	@Override
	public URL resolve(URL systemId) throws IllegalArgumentException {
		// noinspection HttpUrlsUsage
		if (systemId == null || !("https".equalsIgnoreCase(systemId.getProtocol())
				|| "http".equalsIgnoreCase(systemId.getProtocol()))) {
			// do not try to check for non http resources
			return null;
		}
		String url = META_INF_SCHEMA_CACHE + "/" + systemId.getHost() + systemId.getPath();
		URL local = ClasspathSchemaCacheResolver.class.getResource(url);
		if (local != null) {
			LOG.debug("{} resolved locally", systemId);
		}
		return local;
	}

}
