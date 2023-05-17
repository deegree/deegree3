/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.protocol.wps.client.param;

import java.io.Serializable;

/**
 * Encapsulates the parameters needed for defining a complex input / output format.
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class ComplexFormat implements Serializable {

	private static final long serialVersionUID = -55302641376503085L;

	private String mimeType;

	private String encoding;

	private String schema;

	/**
	 * Creates a new {@link ComplexFormat} instance.
	 * @param mimeType mime type, may be <code>null</code>
	 * @param encoding encoding, may be <code>null</code>
	 * @param schema XML schema, may be <code>null</code>
	 */
	public ComplexFormat(String mimeType, String encoding, String schema) {
		this.mimeType = mimeType;
		this.encoding = encoding;
		this.schema = schema;
	}

	/**
	 * Returns the mime type of the format.
	 * @return mime type, may be <code>null</code> (means unspecified / default)
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Returns the encoding of the format.
	 * @return encoding, may be <code>null</code> (means unspecified / default)
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Returns the XML schema URL of the format.
	 * @return he XML schema URL, may be <code>null</code> (means unspecified / default)
	 */
	public String getSchema() {
		return schema;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 == null || !(arg0 instanceof ComplexFormat))
			return false;
		ComplexFormat other = (ComplexFormat) arg0;
		if (((this.schema != null && this.schema.equals(other.getSchema()))
				|| (this.schema == null && other.getSchema() == null))
				&& ((this.encoding != null && this.encoding.equals(other.getEncoding()))
						|| (this.encoding == null && other.getEncoding() == null))
				&& ((this.mimeType != null && this.mimeType.equals(other.getMimeType()))
						|| (this.mimeType == null && other.getMimeType() == null))) {
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int result = 0;
		if (schema != null)
			result = schema.hashCode();
		if (encoding != null)
			result = 37 * result + encoding.hashCode();
		if (mimeType != null)
			result = 37 * result + mimeType.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return schema + "; " + encoding + "; " + mimeType;
	}

}
