/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2023 by:
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
 http://www.grit.de/

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
package org.deegree.services.wfs.format.csv;

import java.nio.charset.Charset;
import java.util.Optional;

public class CsvFormatConfig {

	public enum ColumnHeaders {

		AUTO, SHORT, PREFIXED, LONG;

	}

	private final Boolean exportGeometry;

	private final Charset encoding;

	private final Character quoteCharacter;

	private final Character escape;

	private final Character delimiter;

	private final String instanceSeparator;

	private final String recordSeparator;

	private final String columnIdentifier;

	private final String columnCRS;

	private final ColumnHeaders columnHeaders;

	CsvFormatConfig(Boolean exportGeometry, Charset encoding, Character quoteCharacter, Character escape,
			Character delimiter, String instanceSeparator, String recordSeparator, ColumnHeaders columnHeaders,
			String columnGMLIdentifier, String columnCRS) {
		this.exportGeometry = exportGeometry;
		this.encoding = encoding;
		this.quoteCharacter = quoteCharacter;
		this.escape = escape;
		this.delimiter = delimiter;
		this.instanceSeparator = instanceSeparator;
		this.recordSeparator = recordSeparator;
		this.columnIdentifier = columnGMLIdentifier;
		this.columnCRS = columnCRS;
		this.columnHeaders = columnHeaders;
	}

	public Optional<Boolean> getExportGeometry() {
		return Optional.ofNullable(exportGeometry);
	}

	public Optional<Charset> getEncoding() {
		return Optional.ofNullable(encoding);
	}

	public Optional<Character> getQuoteCharacter() {
		return Optional.ofNullable(quoteCharacter);
	}

	public Optional<Character> getEscape() {
		return Optional.ofNullable(escape);
	}

	public Optional<Character> getDelimiter() {
		return Optional.ofNullable(delimiter);
	}

	public Optional<String> getInstanceSeparator() {
		return Optional.ofNullable(instanceSeparator);
	}

	public Optional<String> getRecordSeparator() {
		return Optional.ofNullable(recordSeparator);
	}

	public Optional<String> getColumnIdentifier() {
		return Optional.ofNullable(columnIdentifier);
	}

	public Optional<String> getColumnCRS() {
		return Optional.ofNullable(columnCRS);
	}

	public Optional<ColumnHeaders> getColumnHeaders() {
		return Optional.ofNullable(columnHeaders);
	}

	@SuppressWarnings("unused")
	public static class Builder {

		private Boolean exportGeometry;

		private Charset encoding;

		private Character quoteCharacter;

		private Character escape;

		private Character delimiter;

		private String instanceSeparator;

		private String recordSeparator;

		private String columnIdentifier;

		private String columnCRS;

		private ColumnHeaders columnHeaders;

		public Builder setExportGeometry(Boolean exportGeometry) {
			this.exportGeometry = exportGeometry;
			return this;
		}

		public Builder setEncoding(Charset encoding) {
			this.encoding = encoding;
			return this;
		}

		public Builder setQuoteCharacter(String quoteCharacter) {
			this.quoteCharacter = firstChar(quoteCharacter);
			return this;
		}

		public Builder setEscape(String escape) {
			this.escape = firstChar(escape);
			return this;
		}

		public Builder setDelimiter(String delimiter) {
			this.delimiter = firstChar(delimiter);
			return this;
		}

		public Builder setRecordSeparator(String recordSeparator) {
			this.recordSeparator = recordSeparator;
			return this;
		}

		public Builder setColumnIdentifier(String columnIdentifier) {
			this.columnIdentifier = columnIdentifier;
			return this;
		}

		public Builder setColumnCRS(String columnCRS) {
			this.columnCRS = columnCRS;
			return this;
		}

		public Builder setColumnHeaders(ColumnHeaders columnHeaders) {
			this.columnHeaders = columnHeaders;
			return this;
		}

		public Builder setInstanceSeparator(String instanceSeparator) {
			this.instanceSeparator = instanceSeparator;
			return this;
		}

		public CsvFormatConfig build() {
			return new CsvFormatConfig(exportGeometry, encoding, quoteCharacter, escape, delimiter, instanceSeparator,
					recordSeparator, columnHeaders, columnIdentifier, columnCRS);
		}

		private Character firstChar(String text) {
			if (text != null && !text.isEmpty()) {
				return text.charAt(0);
			}
			else {
				return null;
			}
		}

	}

}
