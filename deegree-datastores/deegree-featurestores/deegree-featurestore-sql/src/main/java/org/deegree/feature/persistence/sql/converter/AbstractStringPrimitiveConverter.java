/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2024 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 https://www.grit.de/

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
package org.deegree.feature.persistence.sql.converter;

import static org.slf4j.LoggerFactory.getLogger;

import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.jaxb.CustomConverterJAXB;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.slf4j.Logger;

/**
 * Base for building a custom converter on top of primitive mappings of strings
 *
 * @see BinaryBase64PrimitiveConverter
 * @see BinaryDataUrlPrimitiveConverter
 * @see CharacterPrimitiveConverter
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public abstract class AbstractStringPrimitiveConverter implements CustomParticleConverter<PrimitiveValue> {

	private static final Logger LOG = getLogger(AbstractStringPrimitiveConverter.class);

	protected final PrimitiveType pt = new PrimitiveType(BaseType.STRING);

	private String column = null;

	protected int maxLen = 256 * 1024 * 1024; // Default limit of 256 MiB

	protected int sqlType;

	protected AbstractStringPrimitiveConverter(int defaultSqlType) {
		this.sqlType = defaultSqlType;
	}

	@Override
	public String getSelectSnippet(String tableAlias) {
		if (tableAlias != null) {
			if (column.startsWith("'") || column.contains(" ")) {
				return column.replace("$0", tableAlias);
			}
			return tableAlias + "." + column;
		}
		return column;
	}

	@Override
	public String getSetSnippet(PrimitiveValue particle) {
		return "?";
	}

	@Override
	public void init(Mapping mapping, SQLFeatureStore fs) {
		if (mapping.getConverter() == null) {
			return;
		}
		for (CustomConverterJAXB.Param p : mapping.getConverter().getParam()) {
			if ("max-length".equalsIgnoreCase(p.getName())) {
				maxLen = Math.max(1, Integer.parseInt(p.getValue()));
			}
			if ("sql-type".equalsIgnoreCase(p.getName())) {
				sqlType = Integer.parseInt(p.getValue());
			}
		}
		if (mapping instanceof PrimitiveMapping) {
			column = ((PrimitiveMapping) mapping).getMapping().toString();
		}
		else {
			LOG.error("Converter cannot be used for mapping path {}", mapping.getPath());
		}
	}

}
