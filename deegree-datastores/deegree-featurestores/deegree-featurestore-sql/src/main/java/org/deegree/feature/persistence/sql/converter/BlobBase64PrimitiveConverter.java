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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Base64;
import org.apache.commons.io.IOUtils;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.rules.Mapping;

/**
 * Converts BLOB database columns from/to primitive strings encoded as Base64
 * <p>
 * Note that the maximum length of allowed data is limited to prevent Denial of Service
 * Attacks. The allowed maximum length can be set through the max-length parameter in
 * bytes (see {@link AbstractStringPrimitiveConverter#init(Mapping, SQLFeatureStore)}).
 * </p>
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc4648.txt">The Base16, Base32, and
 * Base64 Data Encodings</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public class BlobBase64PrimitiveConverter extends AbstractStringPrimitiveConverter {

	protected final Base64.Decoder decoder;

	protected final Base64.Encoder encoder;

	protected BlobBase64PrimitiveConverter(Base64.Encoder enc, Base64.Decoder dec) {
		this.decoder = dec;
		this.encoder = enc;
	}

	public BlobBase64PrimitiveConverter() {
		this(Base64.getEncoder(), Base64.getDecoder());
	}

	String formatInput(String value) throws SQLException {
		return value;
	}

	String formatOutput(String value) throws SQLException {
		return value;
	}

	@Override
	public PrimitiveValue toParticle(ResultSet rs, int colIndex) throws SQLException {
		Blob lob = rs.getBlob(colIndex);
		if (lob == null) {
			return null;
		}
		try (InputStream is = lob.getBinaryStream()) {
			byte[] raw = IOUtils.toByteArray(is);
			return new PrimitiveValue(formatOutput(encoder.encodeToString(raw)), pt);
		}
		catch (IOException ioe) {
			LOG.trace("Exception", ioe);
			throw new SQLException("Conversation from BLOB to BASE64 failed: " + ioe.getMessage());
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
				stmt.setNull(paramIndex, Types.BLOB);
			}
			catch (SQLFeatureNotSupportedException ignored) {
				stmt.setString(paramIndex, null);
			}
		}
		else if (val.length() > (((float) maxLen / 3) * 4 * 1.1)) {
			// NOTE encoded is 4/3 the size of the not encoded content, but 10 percent is
			// added for linebreak etc.
			throw new SQLException("Maximum length of " + maxLen + " bytes exceeded in pre check.");
		}
		else {
			byte[] raw = decoder.decode(formatInput(val));
			if (raw.length > maxLen) {
				throw new SQLException("Maximum length of " + maxLen + " bytes exceeded.");
			}

			stmt.setBlob(paramIndex, new ByteArrayInputStream(raw), raw.length);
		}
	}

}
