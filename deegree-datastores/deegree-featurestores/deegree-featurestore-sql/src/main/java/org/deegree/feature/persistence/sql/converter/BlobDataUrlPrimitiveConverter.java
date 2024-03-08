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

import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Hex;
import org.deegree.commons.utils.StringUtils;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.jaxb.CustomConverterJAXB;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.slf4j.Logger;

public class BlobDataUrlPrimitiveConverter extends BlobBase64PrimitiveConverter {

	private static final Logger LOG = getLogger(BlobDataUrlPrimitiveConverter.class);

	private static final Pattern PAT_NON_HEX = Pattern.compile("[^0-9a-fA-F]+");

	private final Map<String, String> magicBytesToMimeType = new HashMap<>();

	private void addImageMapping(String hexInput, String mimeType) {
		try {
			String hex = PAT_NON_HEX.matcher(hexInput).replaceAll("");
			byte[] magic = Hex.decodeHex(hex);
			String result = encoder.encodeToString(magic);
			// remove padding
			String base64 = StringUtils.trim(result, "=");
			magicBytesToMimeType.put(base64, mimeType);
		}
		catch (Exception ex) {
			LOG.error("Failed to add image magic '{}' for mime-type '{}' to lookup map .", hexInput, mimeType);
			LOG.trace("Exception", ex);
		}
	}

	public BlobDataUrlPrimitiveConverter() {
		super(Base64.getUrlEncoder(), Base64.getUrlDecoder());

		// https://en.wikipedia.org/wiki/List_of_file_signatures
		LOG.trace("Adding default mappings for gif, jpg and png");
		addImageMapping("47 49 46 38 37 61", "image/gif");
		addImageMapping("47 49 46 38 39 61", "image/gif");
		addImageMapping("FF D8 FF DB", "image/jpg");
		addImageMapping("FF D8 FF E0", "image/jpg");
		addImageMapping("FF D8 FF E1", "image/jpg");
		addImageMapping("FF D8 FF EE", "image/jpg");
		addImageMapping("89 50 4E 47 0D 0A 1A 0A", "image/png");
	}

	@Override
	String formatInput(String value) throws SQLException {
		String search = value.substring(0, Math.min(200, value.length()));
		int pos = search.indexOf(";base64,");
		if (search.startsWith("data:") || pos == -1) {
			throw new SQLException("Input data is not a data URL encoded in base64!");
		}
		return super.formatInput(value.substring(pos + 8));
	}

	@Override
	String formatOutput(String value) {
		String mime = magicBytesToMimeType.entrySet()
			.stream()
			.filter(kv -> value.startsWith(kv.getKey()))
			.map(Map.Entry::getValue)
			.findFirst()
			.orElse("application/octet-stream");
		return "data:" + mime + ";base64," + value;
	}

	@Override
	public void init(Mapping mapping, SQLFeatureStore fs) {
		super.init(mapping, fs);
		if (mapping.getConverter() == null) {
			return;
		}
		for (CustomConverterJAXB.Param p : mapping.getConverter().getParam()) {
			if (p.getName() != null && p.getName().toLowerCase().startsWith("magic-") && p.getName().length() > 7) {
				addImageMapping(p.getName().substring(7), p.getValue());
			}
		}
	}

}
