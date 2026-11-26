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

import org.deegree.commons.utils.TunableParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class AuditingSchemaResolver implements SchemaResolver {

	private static final Logger LOG = LoggerFactory.getLogger(AuditingSchemaResolver.class);

	private final boolean auditEnabled = TunableParameter.get("deegree.commons.xml.schema.enable-audit-logging", true);

	private final boolean blockEnabled = TunableParameter.get("deegree.commons.xml.schema.block-non-cached-schemas",
			false);

	@Override
	public URL resolve(URL systemId) throws IllegalArgumentException {
		if (blockEnabled) {
			// if block is enabled no info level messages will be logged
			LOG.error("Resolving non-cached schema files is forbidden by configuration, blocking '{}'", systemId);
			throw new IllegalArgumentException("Resolving of " + systemId + "blocked by configuration.");
		}
		else if (auditEnabled) {
			LOG.info("Resolving schema file '{}', no cached version available.", systemId);
		}
		return null;
	}

	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}

}
