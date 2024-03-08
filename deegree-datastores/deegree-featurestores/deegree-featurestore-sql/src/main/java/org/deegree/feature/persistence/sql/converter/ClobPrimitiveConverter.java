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

import java.io.IOException;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedReader;
import org.deegree.commons.tom.primitive.PrimitiveValue;

public class ClobPrimitiveConverter extends AbstractStringPrimitiveConverter {

	@Override
	public PrimitiveValue toParticle(ResultSet rs, int colIndex) throws SQLException {
		Clob lob = rs.getClob(colIndex);
		try (BoundedReader br = new BoundedReader(lob.getCharacterStream(), maxLen)) {
			return new PrimitiveValue(IOUtils.toString(br), pt);
		}
		catch (IOException ioe) {
			throw new SQLException("Maximum length of " + maxLen + " bytes exceeded.");
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
				stmt.setNull(paramIndex, Types.CLOB);
			}
			catch (SQLFeatureNotSupportedException ignored) {
				stmt.setString(paramIndex, null);
			}
		}
		else if (val.length() > maxLen) {
			throw new SQLException("Maximum length of " + maxLen + " bytes exceeded.");
		}
		else {
			stmt.setClob(paramIndex, new StringReader(val), val.length());
		}
	}

}
