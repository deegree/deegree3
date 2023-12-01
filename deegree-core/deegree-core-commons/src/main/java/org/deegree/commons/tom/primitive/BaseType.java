/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.commons.tom.primitive;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;

import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.datetime.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enumeration for discriminating the different primitive types used in deegree's "Typed
 * Object Model".
 * <p>
 * Based on XML schema types, but stripped down to leave out distinctions that are not
 * needed in the type model.
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public enum BaseType {

	/** Property value is of class <code>String</code>. */
	STRING("string", String.class),
	/** Property value is of class <code>Boolean</code>. */
	BOOLEAN("boolean", Boolean.class),
	/** Property value is of class <code>BigDecimal</code>. */
	DECIMAL("decimal", BigDecimal.class),
	/**
	 * Property value is of class <code>Double</code> (needed because BigDecimal cannot
	 * express "NaN", "-INF" and "INF"), which are required by <code>xs:double</code> /
	 * <code>xs:float</code>.
	 */
	DOUBLE("double", Double.class),
	/** Property value is of class <code>BigInteger</code>. */
	INTEGER("integer", BigInteger.class),
	/** Property value is of class {@link Date}. */
	DATE("date", Date.class),
	/** Property value is of class {@link DateTime}. */
	DATE_TIME("dateTime", DateTime.class),
	/** Property value is of class {@link Time}. */
	TIME("time", Time.class);

	private static final Logger LOG = LoggerFactory.getLogger(BaseType.class);

	private final String xsTypeName;

	private final Class<?> valueClass;

	private BaseType(String xsTypeName, Class<?> valueClass) {
		this.xsTypeName = xsTypeName;
		this.valueClass = valueClass;
	}

	/**
	 * Returns the class that values of this type use.
	 * @return the corresponding class for values, never <code>null</code>
	 */
	public Class<?> getValueClass() {
		return valueClass;
	}

	/**
	 * @return
	 */
	public String getXSTypeName() {
		return xsTypeName;
	}

	/**
	 * Returns the {@link BaseType} for the given value.
	 * @param value
	 * @return corresponding {@link BaseType}, never <code>null</code>
	 * @throws IllegalArgumentException
	 */
	public static BaseType valueOf(Object value) throws IllegalArgumentException {
		Class<?> oClass = value.getClass();
		for (BaseType pt : values()) {
			if (pt.getValueClass() == oClass) {
				return pt;
			}
		}
		String msg = "Cannot determine PrimitiveType for object class: " + value.getClass();
		throw new IllegalArgumentException(msg);
	}

	/**
	 * Returns the {@link BaseType} for the given SQL type (from {@link Types}).
	 *
	 * @see Types
	 * @param sqlType
	 * @return corresponding {@link BaseType}, never <code>null</code>
	 * @throws IllegalArgumentException if the SQL type can not be mapped to a
	 * {@link BaseType}
	 */
	public static BaseType valueOf(int sqlType) {

		BaseType pt = null;

		switch (sqlType) {
			case Types.BIGINT:
			case Types.INTEGER:
			case Types.SMALLINT:
			case Types.TINYINT: {
				pt = INTEGER;
				break;
			}
			case Types.DECIMAL:
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.NUMERIC:
			case Types.REAL: {
				pt = DECIMAL;
				break;
			}
			case Types.NCHAR:
			case Types.NVARCHAR:
			case Types.CHAR:
			case Types.VARCHAR: {
				pt = STRING;
				break;
			}
			case Types.DATE: {
				pt = DATE;
				break;
			}
			case Types.TIMESTAMP: {
				pt = DATE_TIME;
				break;
			}
			case Types.TIME: {
				pt = TIME;
				break;
			}
			case Types.BIT:
			case Types.BOOLEAN: {
				pt = BOOLEAN;
				break;
			}

			case Types.ARRAY:
			case Types.BINARY:
			case Types.BLOB:
			case Types.CLOB:
			case Types.DATALINK:
			case Types.DISTINCT:
			case Types.JAVA_OBJECT:
			case Types.LONGNVARCHAR:
			case Types.LONGVARBINARY:
			case Types.LONGVARCHAR:
			case Types.NCLOB:
			case Types.NULL:
			case Types.OTHER:
			case Types.REF:
			case Types.ROWID:
			case Types.SQLXML:
			case Types.STRUCT:
			case Types.VARBINARY: {
				String msg = "Unmappable SQL type encountered: " + sqlType;
				LOG.warn(msg);
				throw new IllegalArgumentException(msg);
			}
			default: {
				String msg = "Internal error: unknown SQL type encountered: " + sqlType;
				LOG.error(msg);
				throw new IllegalArgumentException(msg);
			}
		}
		return pt;
	}

	/**
	 * Returns the {@link BaseType} for the given XSD simple type definition.
	 * @param xsdTypeDef XSD simple type definition, must not be <code>null</code>
	 * @return corresponding {@link BaseType}, never <code>null</code>
	 */
	public static BaseType valueOf(XSSimpleTypeDefinition xsdTypeDef) {

		switch (xsdTypeDef.getBuiltInKind()) {

			// date and time types
			case XSConstants.DATE_DT: {
				return BaseType.DATE;
			}
			case XSConstants.DATETIME_DT: {
				return BaseType.DATE_TIME;
			}
			case XSConstants.TIME_DT: {
				return BaseType.TIME;
			}

			// numeric types
			// -1.23, 0, 123.4, 1000.00
			case XSConstants.DECIMAL_DT:
				// -INF, -1E4, -0, 0, 12.78E-2, 12, INF, NaN (equivalent to
				// double-precision 64-bit floating point)
			case XSConstants.DOUBLE_DT:
				// -INF, -1E4, -0, 0, 12.78E-2, 12, INF, NaN (single-precision 32-bit
				// floating point)
			case XSConstants.FLOAT_DT: {
				return BaseType.DECIMAL;
			}

			// integer types

			// ...-1, 0, 1, ...
			case XSConstants.INTEGER_DT:
				// 1, 2, ...
			case XSConstants.POSITIVEINTEGER_DT:
				// ... -2, -1
			case XSConstants.NEGATIVEINTEGER_DT:
				// 0, 1, 2, ...
			case XSConstants.NONNEGATIVEINTEGER_DT:
				// ... -2, -1, 0
			case XSConstants.NONPOSITIVEINTEGER_DT:
				// -9223372036854775808, ... -1, 0, 1, ... 9223372036854775807
			case XSConstants.LONG_DT:
				// 0, 1, ... 18446744073709551615
			case XSConstants.UNSIGNEDLONG_DT:
				// -2147483648, ... -1, 0, 1, ... 2147483647
			case XSConstants.INT_DT:
				// 0, 1, ...4294967295
			case XSConstants.UNSIGNEDINT_DT:
				// -32768, ... -1, 0, 1, ... 32767
			case XSConstants.SHORT_DT:
				// 0, 1, ... 65535
			case XSConstants.UNSIGNEDSHORT_DT:
				// -128, ...-1, 0, 1, ... 127
			case XSConstants.BYTE_DT:
				// 0, 1, ... 255
			case XSConstants.UNSIGNEDBYTE_DT: {
				return BaseType.INTEGER;
			}

			// true, false
			case XSConstants.BOOLEAN_DT: {
				return BaseType.BOOLEAN;
			}

			// other types
			case XSConstants.ANYSIMPLETYPE_DT:
			case XSConstants.ANYURI_DT:
			case XSConstants.BASE64BINARY_DT:
			case XSConstants.DURATION_DT:
			case XSConstants.ENTITY_DT:
			case XSConstants.GDAY_DT:
			case XSConstants.GMONTH_DT:
			case XSConstants.GMONTHDAY_DT:
			case XSConstants.GYEAR_DT:
			case XSConstants.GYEARMONTH_DT:
			case XSConstants.HEXBINARY_DT:
			case XSConstants.ID_DT:
			case XSConstants.IDREF_DT:
			case XSConstants.LANGUAGE_DT:
			case XSConstants.LIST_DT:
			case XSConstants.LISTOFUNION_DT:
			case XSConstants.NAME_DT:
			case XSConstants.NCNAME_DT:
			case XSConstants.NMTOKEN_DT:
			case XSConstants.NORMALIZEDSTRING_DT:
			case XSConstants.NOTATION_DT:
			case XSConstants.QNAME_DT:
			case XSConstants.STRING_DT:
			case XSConstants.TOKEN_DT:
			case XSConstants.UNAVAILABLE_DT: {
				return BaseType.STRING;
			}
		}
		throw new IllegalArgumentException("Unexpected simple type: " + xsdTypeDef);
	}

}
