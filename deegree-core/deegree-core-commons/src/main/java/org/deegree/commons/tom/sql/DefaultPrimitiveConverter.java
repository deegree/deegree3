/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.commons.tom.sql;

import static org.deegree.commons.tom.datetime.ISO8601Converter.parseDate;
import static org.deegree.commons.tom.datetime.ISO8601Converter.parseDateTime;
import static org.deegree.commons.tom.datetime.ISO8601Converter.parseTime;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.datetime.Temporal;
import org.deegree.commons.tom.datetime.Time;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;

/**
 * {@link PrimitiveParticleConverter} for canonical conversion between SQL types and
 * {@link PrimitiveValue}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class DefaultPrimitiveConverter implements PrimitiveParticleConverter {

	protected final PrimitiveType pt;

	protected BaseType bt;

	protected final String column;

	private final boolean isConcatenated;

	public DefaultPrimitiveConverter(PrimitiveType pt, String column) {
		this.pt = pt;
		this.bt = pt.getBaseType();
		this.column = column;
		this.isConcatenated = false;
	}

	public DefaultPrimitiveConverter(PrimitiveType pt, String column, boolean isConcatenated) {
		this.pt = pt;
		this.bt = pt.getBaseType();
		this.column = column;
		this.isConcatenated = isConcatenated;
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
	public PrimitiveValue toParticle(ResultSet rs, int colIndex) throws SQLException {
		Object sqlValue = rs.getObject(colIndex);
		if (sqlValue == null) {
			return null;
		}
		switch (bt) {
			case BOOLEAN:
				return toBooleanParticle(sqlValue);
			case DATE:
				return toDateParticle(sqlValue);
			case DATE_TIME:
				return toDateTimeParticle(sqlValue);
			case DECIMAL:
				return toDecimalParticle(sqlValue);
			case DOUBLE:
				return toDoubleParticle(sqlValue);
			case INTEGER:
				return toIntegerParticle(sqlValue);
			case STRING:
				return toStringParticle(sqlValue);
			case TIME:
				return toTimeParticle(sqlValue);
		}
		throw new UnsupportedOperationException();
	}

	public PrimitiveValue toParticle(Object sqlValue) throws SQLException {
		if (sqlValue == null) {
			return null;
		}
		switch (bt) {
			case BOOLEAN:
				return toBooleanParticle(sqlValue);
			case DATE:
				return toDateParticle(sqlValue);
			case DATE_TIME:
				return toDateTimeParticle(sqlValue);
			case DECIMAL:
				return toDecimalParticle(sqlValue);
			case DOUBLE:
				return toDoubleParticle(sqlValue);
			case INTEGER:
				return toIntegerParticle(sqlValue);
			case STRING:
				return toStringParticle(sqlValue);
			case TIME:
				return toTimeParticle(sqlValue);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public PrimitiveType getType() {
		return pt;
	}

	protected PrimitiveValue toBooleanParticle(Object sqlValue) {
		Boolean value = null;
		if (sqlValue instanceof Boolean) {
			value = (Boolean) sqlValue;
		}
		else {
			String s = "" + sqlValue;
			if ("1".equals(s) || "true".equalsIgnoreCase(s)) {
				value = Boolean.TRUE;
			}
			else if ("0".equals(s) || "false".equalsIgnoreCase(s)) {
				value = Boolean.FALSE;
			}
			else {
				throw new IllegalArgumentException("Unable to convert SQL result value of type '" + sqlValue.getClass()
						+ "' to Boolean particle.");
			}
		}
		return new PrimitiveValue(value, pt);
	}

	protected PrimitiveValue toDateParticle(Object sqlValue) {
		Temporal value = null;
		if (sqlValue instanceof java.util.Date) {
			Calendar cal = Calendar.getInstance();
			cal.setTime((java.util.Date) sqlValue);
			value = new Date(cal, true);
		}
		else if (sqlValue != null) {
			throw new IllegalArgumentException(
					"Unable to convert SQL result value of type '" + sqlValue.getClass() + "' to Date particle.");
		}
		return new PrimitiveValue(value, pt);
	}

	protected PrimitiveValue toDateTimeParticle(Object sqlValue) {
		Temporal value = null;
		if (sqlValue instanceof java.util.Date) {
			Calendar cal = Calendar.getInstance();
			cal.setTime((java.util.Date) sqlValue);
			value = new DateTime(cal, true);
		}
		else if (sqlValue != null) {
			throw new IllegalArgumentException(
					"Unable to convert SQL result value of type '" + sqlValue.getClass() + "' to DateTime particle.");
		}
		return new PrimitiveValue(value, pt);
	}

	protected PrimitiveValue toTimeParticle(Object sqlValue) {
		Temporal value = null;
		if (sqlValue instanceof java.util.Date) {
			Calendar cal = Calendar.getInstance();
			cal.setTime((java.util.Date) sqlValue);
			value = new Time(cal, true);
		}
		else if (sqlValue != null) {
			throw new IllegalArgumentException(
					"Unable to convert SQL result value of type '" + sqlValue.getClass() + "' to Time particle.");
		}
		return new PrimitiveValue(value, pt);
	}

	protected PrimitiveValue toDecimalParticle(Object sqlValue) throws NumberFormatException {
		BigDecimal value = null;
		if (sqlValue instanceof BigDecimal) {
			value = (BigDecimal) sqlValue;
		}
		else {
			value = new BigDecimal(sqlValue.toString());
		}
		return new PrimitiveValue(value, pt);
	}

	protected PrimitiveValue toDoubleParticle(Object sqlValue) throws NumberFormatException {
		Double value = null;
		if (sqlValue instanceof Double) {
			value = (Double) sqlValue;
		}
		else {
			value = new Double(sqlValue.toString());
		}
		return new PrimitiveValue(value, pt);
	}

	protected PrimitiveValue toIntegerParticle(Object sqlValue) throws NumberFormatException {
		BigInteger value = null;
		if (sqlValue instanceof BigInteger) {
			value = (BigInteger) sqlValue;
		}
		else {
			value = new BigInteger(sqlValue.toString());
		}
		return new PrimitiveValue(value, pt);
	}

	protected PrimitiveValue toStringParticle(Object sqlValue) {
		return new PrimitiveValue("" + sqlValue, pt);
	}

	@Override
	public void setParticle(PreparedStatement stmt, PrimitiveValue particle, int colIndex) throws SQLException {
		Object value = particle.getValue();
		if (value != null) {
			value = toSqlValue(value);
		}
		stmt.setObject(colIndex, value);
	}

	public Object toSqlValue(Object input) {
		switch (bt) {
			case BOOLEAN:
				return toBoolean(input);
			case DATE:
				return toSqlDate(input);
			case DATE_TIME:
				return toSqlTimestamp(input);
			case DECIMAL:
				return toDecimal(input);
			case DOUBLE:
				return toDouble(input);
			case INTEGER:
				return toInteger(input);
			case STRING:
				return toString(input);
			case TIME:
				return toSqlTime(input);
		}
		throw new UnsupportedOperationException();
	}

	protected Boolean toBoolean(Object input) {
		if (input instanceof Boolean) {
			return (Boolean) input;
		}
		Boolean value = null;
		String s = "" + input;
		if ("1".equals(s) || "true".equalsIgnoreCase(s)) {
			value = Boolean.TRUE;
		}
		else if ("0".equals(s) || "false".equalsIgnoreCase(s)) {
			value = Boolean.FALSE;
		}
		else {
			String msg = "Unable to convert primitive value ('" + s + "'), type '" + input.getClass()
					+ "' to Boolean object.";
			throw new IllegalArgumentException(msg);
		}
		return value;
	}

	/**
	 * TODO handling of SQL timezone
	 * @param input
	 * @return
	 */
	protected java.sql.Date toSqlDate(Object input) {
		if (input instanceof java.sql.Date) {
			return (java.sql.Date) input;
		}
		java.sql.Date value = null;
		if (input instanceof java.util.Date) {
			java.util.Date date = (java.util.Date) input;
			value = new java.sql.Date(date.getTime());
		}
		else if (input instanceof Temporal) {
			Temporal timeInstant = (Temporal) input;
			value = new java.sql.Date(timeInstant.getTimeInMilliseconds());
		}
		else {
			String s = input.toString();
			Date timeInstant = parseDate(s);
			value = toSqlDate(timeInstant);
		}
		return value;
	}

	/**
	 * TODO handling of SQL timezone
	 * @param input
	 * @return
	 */
	protected Timestamp toSqlTimestamp(Object input) {
		if (input instanceof Timestamp) {
			return (Timestamp) input;
		}
		Timestamp value = null;
		if (input instanceof java.util.Date) {
			java.util.Date date = (java.util.Date) input;
			value = new Timestamp(date.getTime());
		}
		else if (input instanceof Temporal) {
			Temporal timeInstant = (Temporal) input;
			value = new Timestamp(timeInstant.getTimeInMilliseconds());
		}
		else {
			String s = input.toString();
			if (s.isEmpty()) {
				return null;
			}
			DateTime timeInstant = parseDateTime(s);
			value = toSqlTimestamp(timeInstant);
		}
		return value;
	}

	private Object toDecimal(Object input) {
		// TODO is this the correct SQL representation
		return toDouble(input);
	}

	private Double toDouble(Object input) {
		if (input instanceof Double) {
			return (Double) input;
		}
		if (input instanceof Number) {
			return ((Number) input).doubleValue();
		}
		return Double.parseDouble(input.toString());
	}

	private Object toInteger(Object input) {
		if (input instanceof Integer) {
			return (Integer) input;
		}
		if (input instanceof Number) {
			return ((Number) input).intValue();
		}
		try {
			return Integer.parseInt(input.toString());
		}
		catch (NumberFormatException e) {
			// let the DB try to convert it
		}
		return input.toString();
	}

	private String toString(Object input) {
		return input.toString();
	}

	/**
	 * TODO handling of SQL timezone
	 * @param input
	 * @return
	 */
	private java.sql.Time toSqlTime(Object input) {
		if (input instanceof java.sql.Time) {
			return (java.sql.Time) input;
		}
		java.sql.Time value = null;
		if (input instanceof java.util.Date) {
			java.util.Date date = (java.util.Date) input;
			value = new java.sql.Time(date.getTime());
		}
		else if (input instanceof Temporal) {
			Temporal timeInstant = (Temporal) input;
			value = new java.sql.Time(timeInstant.getTimeInMilliseconds());
		}
		else {
			String s = input.toString();
			Time timeInstant = parseTime(s);
			value = toSqlTime(timeInstant);
		}
		return value;
	}

	@Override
	public boolean isConcatenated() {
		return isConcatenated;
	}

}
