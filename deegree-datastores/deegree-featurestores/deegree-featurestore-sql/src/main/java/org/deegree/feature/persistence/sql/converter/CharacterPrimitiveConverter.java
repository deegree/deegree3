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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import org.apache.commons.io.IOUtils;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.slf4j.Logger;

/**
 * Converts large character type database columns from/to primitive strings
 * <p>
 * Note that the maximum length of allowed data is limited to prevent Denial of Service
 * Attacks. The allowed maximum length can be set through the max-length parameter in
 * bytes (see {@link AbstractStringPrimitiveConverter#init(Mapping, SQLFeatureStore)}).
 * </p>
 *
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public class CharacterPrimitiveConverter extends AbstractStringPrimitiveConverter {

	private static final Logger LOG = getLogger(CharacterPrimitiveConverter.class);

	public CharacterPrimitiveConverter() {
		super(Types.CLOB);
	}

	@Override
	public PrimitiveValue toParticle(ResultSet rs, int colIndex) throws SQLException {
		try (Reader rdr = rs.getCharacterStream(colIndex)) {
			if (rdr == null) {
				return null;
			}
			return new PrimitiveValue(IOUtils.toString(rdr), pt);
		}
		catch (IOException ioe) {
			LOG.trace("Exception", ioe);
			throw new SQLException("Failed to read CLOB: " + ioe.getMessage());
		}
	}

	@Override
	public void setParticle(PreparedStatement stmt, PrimitiveValue particle, int paramIndex) throws SQLException {
		final String val;
		if (particle.getValue() != null) {
			val = particle.getValue().toString();
		}
		else {
			val = null;
		}
		if (val == null) {
			try {
				stmt.setNull(paramIndex, sqlType);
			}
			catch (SQLFeatureNotSupportedException ignored) {
				stmt.setString(paramIndex, null);
			}
		}
		else if (val.length() > maxLen) {
			throw new SQLException("Maximum length of " + maxLen + " bytes exceeded.");
		}
		else {
			stmt.setCharacterStream(paramIndex, new StringReader(val), val.length());
		}
	}

}
