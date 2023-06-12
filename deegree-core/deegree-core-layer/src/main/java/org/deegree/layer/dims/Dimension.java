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

package org.deegree.layer.dims;

import static java.lang.Math.abs;
import static org.deegree.commons.tom.datetime.ISO8601Converter.formatDateTime;
import static org.deegree.commons.tom.datetime.ISO8601Converter.formatDuration;
import static org.deegree.commons.tom.datetime.ISO8601Converter.parseDateTime;
import static org.deegree.commons.tom.datetime.ISO8601Converter.parseDuration;
import static org.deegree.commons.utils.math.MathUtils.isZero;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.datetime.Duration;
import org.deegree.commons.tom.datetime.Temporal;

/**
 * <code>Dimension</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @param <T>
 */
public class Dimension<T> {

	private List<?> defaultValue;

	private boolean current;

	private boolean nearestValue;

	private boolean multipleValues;

	private String units;

	private String unitSymbol;

	private QName property;

	private List<?> extent;

	private String name;

	/**
	 * @param name
	 * @param defaultValue
	 * @param current
	 * @param nearestValue
	 * @param multipleValues
	 * @param units
	 * @param unitSymbol
	 * @param property
	 * @param extent
	 */
	public Dimension(String name, List<?> defaultValue, boolean current, boolean nearestValue, boolean multipleValues,
			String units, String unitSymbol, QName property, List<?> extent) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.current = current;
		this.nearestValue = nearestValue;
		this.multipleValues = multipleValues;
		this.units = units;
		this.unitSymbol = unitSymbol;
		this.property = property;
		this.extent = extent;
	}

	/**
	 * @return the default value, if set
	 */
	public List<?> getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @return the property name
	 */
	public QName getPropertyName() {
		return property;
	}

	/**
	 * @return whether current is allowed
	 */
	public boolean getCurrent() {
		return current;
	}

	/**
	 * @return whether multiple values are allowed
	 */
	public boolean getMultipleValues() {
		return multipleValues;
	}

	/**
	 * @return whether nearest values will be used
	 */
	public boolean getNearestValue() {
		return nearestValue;
	}

	/**
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * @return the unit symbol
	 */
	public String getUnitSymbol() {
		return unitSymbol;
	}

	/**
	 * @return the extent
	 */
	public List<?> getExtent() {
		return extent;
	}

	/**
	 * @return the extent as comma separated list as in the specs
	 */
	public String getExtentAsString() {
		return formatDimensionValueList(getExtent(), name.equals("time"));
	}

	/**
	 * @param list
	 * @param time
	 * @return a formatted string of dimension values
	 */
	public static String formatDimensionValueList(List<?> list, boolean time) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object o : list) {
			if (first) {
				first = false;
			}
			else {
				sb.append(",");
			}
			if (o instanceof DimensionInterval<?, ?, ?>) {
				DimensionInterval<?, ?, ?> iv = (DimensionInterval<?, ?, ?>) o;
				if (time) {
					sb.append(formatDateTime((Date) iv.min)).append("/");
					if (iv.max instanceof Date) {
						sb.append(formatDateTime((Date) iv.max)).append("/");
					}
					else {
						sb.append("current/");
					}
					if (iv.res instanceof Duration) {
						sb.append(formatDuration((Duration) iv.res));
					}
				}
				else {
					sb.append(iv.min).append("/").append(iv.max).append("/").append(iv.res);
				}
			}
			else {
				if (time) {
					sb.append(formatDateTime((Temporal) o));
				}
				else {
					sb.append(o);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Parses a dimension value (eg. a List of or individual values of String or
	 * DimensionInterval) into the appropriate Date/Double/Integer values.
	 * @param o
	 * @param time
	 * @return an object with individual values parsed
	 * @throws ParseException
	 */
	public static Object parseTyped(Object o, boolean time) throws ParseException {
		if (o instanceof List<?>) {
			List<Object> list = new ArrayList<Object>(((List<?>) o).size());
			for (Object o2 : (List<?>) o) {
				list.add(parseTyped(o2, time));
			}
			return list;
		}
		if (o instanceof String) {
			if (time) {
				if (((String) o).equalsIgnoreCase("current")) {
					return "current";
				}
				return parseDateTime((String) o);
			}
			try {
				return Integer.valueOf((String) o);
			}
			catch (NumberFormatException e) {
				try {
					return Double.valueOf((String) o);
				}
				catch (NumberFormatException e2) {
					return o;
				}
			}
		}
		if (o instanceof DimensionInterval<?, ?, ?>) {
			return parseTypedInterval(o, time);
		}
		return o;
	}

	private static Object parseTypedInterval(Object o, boolean time) throws ParseException {
		DimensionInterval<?, ?, ?> iv = (DimensionInterval<?, ?, ?>) o;
		if (time) {
			Date min = parseDateTime((String) iv.min).getDate();
			Date max;
			if (((String) iv.max).equalsIgnoreCase("current")) {
				max = new Date();
			}
			else {
				max = parseDateTime((String) iv.max).getDate();
			}
			if (iv.res instanceof Integer) {
				return new DimensionInterval<Date, Date, Object>(min, max, null);
			}
			return new DimensionInterval<Date, Date, Duration>(min, max, parseDuration((String) iv.res));
		}
		try {
			Integer min = Integer.valueOf((String) iv.min);
			Integer max = Integer.valueOf((String) iv.max);
			Integer res = iv.res instanceof Integer ? (Integer) iv.res : Integer.valueOf((String) iv.res);
			return new DimensionInterval<Integer, Integer, Integer>(min, max, res);
		}
		catch (NumberFormatException e) {
			try {
				Double min = Double.valueOf((String) iv.min);
				Double max = Double.valueOf((String) iv.max);
				Double res = iv.res instanceof Integer ? (Integer) iv.res : Double.valueOf((String) iv.res);
				return new DimensionInterval<Double, Double, Double>(min, max, res);
			}
			catch (NumberFormatException e2) {
				return iv;
			}
		}
	}

	private static double getAsDouble(final Object o) {
		if (o instanceof Double) {
			return (Double) o;
		}
		if (o instanceof Integer) {
			return (Integer) o;
		}
		if (o instanceof Long) {
			return (Long) o;
		}
		if (o instanceof Date) {
			return ((Date) o).getTime();
		}
		if (o instanceof Duration) {
			return ((Duration) o).getEnd(new DateTime(new Date(0), null)).getTimeInMilliseconds();
		}
		return 0; // what else?
	}

	/**
	 * @param val
	 * @return the closest value
	 */
	public Object getNearestValue(Object val) {
		NearestValueStatus status = new NearestValueStatus(getAsDouble(val), val);

		for (Object o : extent) {
			if (o instanceof DimensionInterval<?, ?, ?>) {
				DimensionInterval<?, ?, ?> iv = (DimensionInterval<?, ?, ?>) o;
				if (!getNearestValueFromInterval(status, iv)) {
					return val;
				}
			}
			else {
				double next = getAsDouble(o);
				double dist = abs(next - status.oval);
				if (dist < status.distance) {
					status.cur = o;
					status.distance = dist;
				}
			}
		}

		if (val instanceof Date && !(status.cur instanceof Date)) {
			return new Date((long) getAsDouble(status.cur));
		}

		return status.cur;
	}

	private static boolean getNearestValueFromInterval(NearestValueStatus status, DimensionInterval<?, ?, ?> iv) {
		double min = getAsDouble(iv.min);
		double max = getAsDouble(iv.max);
		double res = getAsDouble(iv.res);
		if (status.oval < min) {
			double dist = abs(status.oval - min);
			if (dist < status.distance) {
				status.cur = iv.min;
				status.distance = dist;
			}
		}
		else if (status.oval > max) {
			double dist = abs(max - status.oval);
			if (dist < status.distance) {
				status.cur = iv.max;
				status.distance = dist;
			}
		}
		if (res == 0) {
			if (min <= status.oval && status.oval <= max) {
				return false;
			}
		}
		else {
			getNearestValueFromIntervalWithRes(status, min, max, res);
		}
		return true;
	}

	private static void getNearestValueFromIntervalWithRes(NearestValueStatus status, double min, double max,
			double res) {
		if (min <= status.oval && status.oval <= max) {
			// TODO think about a better way
			if (res <= 0) {
				res = 1;
			}
			while (min <= max) {
				double dist = abs(status.oval - min);
				if (dist < status.distance) {
					status.cur = min;
					status.distance = dist;
				}
				min += res;
			}
		}
	}

	private static boolean hits(final DimensionInterval<?, ?, ?> iv, final Object o) {
		double min = getAsDouble(iv.min);
		final double max = getAsDouble(iv.max);
		final double val = getAsDouble(o);

		if (isInIntervalNoResolution(iv, min, max, val)) {
			return true;
		}

		final double res = getAsDouble(iv.res);
		if (res <= 0 && min <= val && val <= max) {
			return true;
		}
		if (res <= 0) {
			return false;
		}
		while (min < max) {
			if (isZero(min - val)) {
				return true;
			}
			min += res;
		}
		return false;
	}

	private static boolean isInIntervalNoResolution(DimensionInterval<?, ?, ?> iv, double min, double max, double val) {
		if (iv.res instanceof Integer && ((Integer) iv.res) == 0) {
			if (min <= val && val <= max) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param val
	 * @return true, if the value hits values defined in the extent
	 */
	public boolean isValid(Object val) {
		if (val instanceof DimensionInterval<?, ?, ?>) {
			DimensionInterval<?, ?, ?> iv = (DimensionInterval<?, ?, ?>) val;
			return isValidInInterval(iv);
		}
		else {
			for (Object o : extent) {
				if (o.equals(val)) {
					return true;
				}
				if (o instanceof DimensionInterval<?, ?, ?>) {
					if (hits((DimensionInterval<?, ?, ?>) o, val)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean isValidInInterval(DimensionInterval<?, ?, ?> iv) {
		for (Object o : extent) {
			if (o instanceof DimensionInterval<?, ?, ?>) {
				DimensionInterval<?, ?, ?> newIv = (DimensionInterval<?, ?, ?>) o;

				double min = getAsDouble(newIv.min);
				final double max = getAsDouble(newIv.max);
				double res = getAsDouble(newIv.res);
				if (res <= 0) {
					// TODO think about a better way
					res = 1;
				}

				while (min < max) {
					if (hits(iv, min)) {
						return true;
					}
					min += res;
				}

				if (hits(iv, newIv.min)) {
					return true;
				}
			}
			else {
				if (hits(iv, o)) {
					return true;
				}
			}
		}
		return false;
	}

	static class NearestValueStatus {

		double oval, distance = Double.MAX_VALUE;

		Object cur;

		NearestValueStatus(double oval, Object cur) {
			this.oval = oval;
			this.cur = cur;
		}

	}

}
