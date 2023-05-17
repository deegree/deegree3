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
package org.deegree.commons.tom.primitive;

import static org.deegree.commons.tom.datetime.ISO8601Converter.parseDate;
import static org.deegree.commons.tom.datetime.ISO8601Converter.parseDateTime;
import static org.deegree.commons.tom.datetime.ISO8601Converter.parseTime;
import static org.deegree.commons.tom.primitive.BaseType.BOOLEAN;
import static org.deegree.commons.tom.primitive.XMLValueMangler.internalToXML;
import static org.deegree.commons.tom.primitive.XMLValueMangler.xmlToInternal;

import java.math.BigDecimal;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.datetime.Time;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.utils.Pair;

/**
 * {@link TypedObjectNode} that represents a typed primitive value, e.g. an XML text node
 * or an XML attribute value with type information.
 * <p>
 * This class wraps both the normalized primitive value and a textual representation.
 * </p>
 *
 * @see BaseType
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class PrimitiveValue implements TypedObjectNode, Comparable<PrimitiveValue> {

	private final PrimitiveType type;

	private final Object value;

	private final String textValue;

	/**
	 * Creates a new {@link PrimitiveValue} instance.
	 * @param value object value, must not be <code>null</code> and correspond to type
	 * (see {@link BaseType})
	 * @param textValue textual representation of the value, can be <code>null</code>
	 * (representation will be auto-generated from value)
	 * @param type primitive type, must not be <code>null</code>
	 * @throws IllegalArgumentException
	 */
	public PrimitiveValue(Object value, String textValue, PrimitiveType type) throws IllegalArgumentException {
		this.value = value;
		if (textValue != null) {
			this.textValue = textValue;
		}
		else {
			this.textValue = internalToXML(value, type.getBaseType());
		}
		this.type = type;
	}

	/**
	 * Creates a new {@link PrimitiveValue} instance.
	 * <p>
	 * Textual representation will be derived from value.
	 * </p>
	 * @param value object value, must not be <code>null</code> and correspond to type
	 * (see {@link BaseType})
	 * @param type primitive type, must not be <code>null</code>
	 */
	public PrimitiveValue(Object value, PrimitiveType type) {
		this.textValue = internalToXML(value, type.getBaseType());
		this.type = type;
		this.value = value;
	}

	/**
	 * Creates a new {@link PrimitiveValue} instance.
	 * <p>
	 * Primitive type will be determined from value, as well the textual representation.
	 * </p>
	 * @param value object value, must not be <code>null</code> and correspond to a
	 * supported type (see {@link BaseType})
	 * @throws IllegalArgumentException
	 */
	public PrimitiveValue(Object value) throws IllegalArgumentException {
		this.type = new PrimitiveType(BaseType.valueOf(value));
		this.textValue = internalToXML(value, type.getBaseType());
		this.value = value;
	}

	/**
	 * Creates a new {@link PrimitiveValue} instance.
	 * <p>
	 * Object value be determined from textual representation.
	 * </p>
	 * @param value textual representation, must not be <code>null</code> and be valid
	 * according to the primitive type (see {@link BaseType})
	 * @throws IllegalArgumentException
	 */
	public PrimitiveValue(String value, PrimitiveType type) throws IllegalArgumentException {
		this.value = xmlToInternal(value, type.getBaseType());
		this.textValue = value;
		this.type = type;
	}

	/**
	 * Returns the canonical object representation of the value.
	 * <p>
	 * See {@link BaseType} for used Java types.
	 * </p>
	 * @return the canonical object representation of the value, never <code>null</code>
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Returns the textual representation of the value.
	 * <p>
	 * This may differ from the value returned by {@link #getValue()} (e.g. number of
	 * trailing zeros of a decimal fraction).
	 * </p>
	 * @return the textual representation of the value, never <code>null</code>
	 */
	public String getAsText() {
		return textValue;
	}

	/**
	 * Returns the type of the value.
	 * @return the type of the value, never <code>null</code>
	 */
	public PrimitiveType getType() {
		return type;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int compareTo(PrimitiveValue o) {
		Pair<Object, Object> comparables = makeComparable(value, o.value);
		return ((Comparable) comparables.first).compareTo((comparables.second));
	}

	@Override
	public boolean equals(Object o) {

		// TODO make this failproof
		Object thatValue = o;
		if (o instanceof PrimitiveValue) {
			thatValue = ((PrimitiveValue) o).value;
		}

		Pair<Object, Object> comparablePair = makeComparable(value, thatValue);

		// NOTE: don't use #equals() for BigDecimal, because new BigDecimal("155.00") is
		// not equal to
		// new BigDecimal("155")
		if (comparablePair.first instanceof BigDecimal) {
			return (((BigDecimal) comparablePair.first).compareTo((BigDecimal) comparablePair.second) == 0);
		}
		return comparablePair.first.equals(comparablePair.second);
	}

	@Override
	public int hashCode() {
		// TODO: see ticket #113
		return value.hashCode();
	}

	@Override
	public String toString() {
		return textValue;
	}

	/**
	 * @param value1
	 * @param value2
	 * @return should be a ComparablePair now that we have it...
	 * @throws IllegalArgumentException
	 */
	public static Pair<Object, Object> makeComparable(Object value1, Object value2) throws IllegalArgumentException {
		Pair<Object, Object> result = new Pair<Object, Object>(value1, value2);
		if (!(value1 instanceof String)) {
			if (value1 instanceof Number) {
				result = new Pair<Object, Object>(value1, new BigDecimal(value2.toString()));
			}
			else if (value1 instanceof Boolean) {
				result = new Pair<Object, Object>(value1, XMLValueMangler.xmlToInternal(value2.toString(), BOOLEAN));
			}
			else if (value1 instanceof Date) {
				result = new Pair<Object, Object>(value1, parseDate(value2.toString()));
			}
			else if (value1 instanceof DateTime) {
				result = new Pair<Object, Object>(value1, parseDateTime(value2.toString()));
			}
			else if (value1 instanceof Time) {
				result = new Pair<Object, Object>(value1, parseTime(value2.toString()));
			}
			else if (value1 instanceof CodeType) {
				result = new Pair<Object, Object>(value1,
						new CodeType(value2.toString(), ((CodeType) value1).getCodeSpace()));
			}
			else if (value1 instanceof Measure) {
				result = new Pair<Object, Object>(value1,
						new Measure(value2.toString(), ((Measure) value1).getUomUri()));
			}
		}
		else if (!(value2 instanceof String)) {
			if (value2 instanceof Number) {
				result = new Pair<Object, Object>(new BigDecimal(value1.toString()), value2);
			}
			else if (value1 instanceof Boolean) {
				result = new Pair<Object, Object>(XMLValueMangler.xmlToInternal(value1.toString(), BOOLEAN), value2);
			}
			else if (value2 instanceof Date) {
				result = new Pair<Object, Object>(parseDate(value1.toString()), value2);
			}
			else if (value2 instanceof DateTime) {
				result = new Pair<Object, Object>(parseDateTime(value1.toString()), value2);
			}
			else if (value2 instanceof Time) {
				result = new Pair<Object, Object>(parseTime(value1.toString()), value2);
			}
			else if (value1 instanceof CodeType) {
				result = new Pair<Object, Object>(new CodeType(value1.toString(), ((CodeType) value2).getCodeSpace()),
						value2);
			}
			else if (value1 instanceof Measure) {
				result = new Pair<Object, Object>(new Measure(value1.toString(), ((Measure) value2).getUomUri()),
						value2);
			}
		}

		// TODO create comparable numbers in a more efficient manner
		if (result.first instanceof Number && !(result.first instanceof BigDecimal)) {
			result.first = new BigDecimal(result.first.toString());
		}
		if (result.second instanceof Number && !(result.second instanceof BigDecimal)) {
			result.second = new BigDecimal(result.second.toString());
		}

		return result;
	}

}
