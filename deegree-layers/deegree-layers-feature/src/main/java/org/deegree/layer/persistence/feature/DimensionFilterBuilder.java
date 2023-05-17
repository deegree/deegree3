/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.layer.persistence.feature;

import static java.lang.System.currentTimeMillis;
import static org.deegree.commons.tom.datetime.ISO8601Converter.formatDateTime;
import static org.deegree.layer.dims.Dimension.formatDimensionValueList;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.datetime.Temporal;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsBetween;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.Or;
import org.deegree.layer.dims.Dimension;
import org.deegree.layer.dims.DimensionInterval;

/**
 * Is used to create dimension related filter parts.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class DimensionFilterBuilder {

	private Map<String, Dimension<?>> dimensions;

	DimensionFilterBuilder(Map<String, Dimension<?>> dimensions) {
		this.dimensions = dimensions;
	}

	/**
	 * @param dims
	 * @return a filter or null, if no dimensions have been requested
	 * @throws OWSException
	 * @throws MissingDimensionValue
	 * @throws InvalidDimensionValue
	 */
	OperatorFilter getDimensionFilter(Map<String, List<?>> dims, List<String> headers) throws OWSException {
		LinkedList<Operator> ops = new LinkedList<Operator>();

		Dimension<?> time = dimensions.get("time");

		if (time != null) {
			handleTime(dims, time, headers, ops);
		}

		for (String name : dimensions.keySet()) {
			if (name.equals("time")) {
				continue;
			}
			Dimension<?> dim = dimensions.get(name);

			List<?> vals = dims.get(name);

			vals = checkDefaultValue(vals, dim, headers, name);

			Operator[] os = new Operator[vals.size()];

			findFilters(vals, dim, name, os, headers);

			if (os.length > 1) {
				if (!dim.getMultipleValues()) {
					throw new OWSException("Multiple values are not allowed for ELEVATION.", "InvalidDimensionValue",
							"elevation");
				}
				ops.add(new Or(os));
			}
			else {
				ops.add(os[0]);
			}
		}

		if (ops.isEmpty()) {
			return null;
		}
		if (ops.size() > 1) {
			return new OperatorFilter(new And(ops.toArray(new Operator[ops.size()])));
		}
		return new OperatorFilter(ops.get(0));
	}

	private void findFilters(List<?> vals, Dimension<?> dim, String name, Operator[] os, List<String> headers)
			throws OWSException {
		final ValueReference property = new ValueReference(dim.getPropertyName());
		int i = 0;

		for (Object o : vals) {
			checkValidity(dim, o, name);

			if (o instanceof DimensionInterval<?, ?, ?>) {
				DimensionInterval<?, ?, ?> iv = (DimensionInterval<?, ?, ?>) o;
				final String min;
				if (iv.min instanceof Date) {
					min = formatDateTime((Date) iv.min);
				}
				else {
					min = ((Number) iv.min).toString();
				}
				final String max;
				if (iv.max instanceof Date) {
					max = formatDateTime((Date) iv.max);
				}
				else if (iv.max instanceof String) {
					max = formatDateTime(new Date());
				}
				else {
					max = ((Number) iv.max).toString();
				}
				os[i++] = new PropertyIsBetween(property, new Literal<PrimitiveValue>(min),
						new Literal<PrimitiveValue>(max), true, null);
			}
			else {
				o = checkNearestValue(o, headers, name, dim);
				os[i++] = new PropertyIsEqualTo(new ValueReference(dim.getPropertyName()),
						new Literal<PrimitiveValue>(o.toString()), true, null);
			}
		}
	}

	private void checkValidity(Dimension<?> dim, Object o, String name) throws OWSException {
		if (!dim.getNearestValue() && !dim.isValid(o)) {
			throw new OWSException("The value " + o.toString() + " was not valid for dimension " + name + ".",
					"InvalidDimensionValue", name);
		}
	}

	private List<?> checkDefaultValue(List<?> vals, Dimension<?> dim, List<String> headers, String name)
			throws OWSException {
		if (vals == null) {
			vals = dim.getDefaultValue();
			if (vals == null) {
				throw new OWSException("The dimension value for " + name + " was missing.", "MissingDimensionValue",
						name);
			}
			String units = dim.getUnits();
			if (name.equals("elevation")) {
				headers.add("99 Default value used: elevation=" + formatDimensionValueList(vals, false) + " "
						+ (units == null ? "m" : units));
			}
			else if (name.equals("time")) {
				headers.add("99 Default value used: time=" + formatDimensionValueList(vals, true) + " "
						+ (units == null ? "ISO8601" : units));
			}
			else {
				headers.add("99 Default value used: DIM_" + name + "=" + formatDimensionValueList(vals, false) + " "
						+ units);
			}
		}
		return vals;
	}

	private Object checkNearestValue(Object o, List<String> headers, String name, Dimension<?> dim) {
		if (dim.getNearestValue()) {
			Object nearest = dim.getNearestValue(o);
			if (!nearest.equals(o)) {
				o = nearest;
				if ("elevation".equals(name)) {
					headers.add("99 Nearest value used: elevation=" + o + " " + dim.getUnits());
				}
				else {
					headers.add("99 Nearest value used: DIM_" + name + "=" + o + " " + dim.getUnits());
				}
			}
		}
		return o;
	}

	private void handleTime(Map<String, List<?>> dims, Dimension<?> time, List<String> headers,
			LinkedList<Operator> ops) throws OWSException {
		List<?> vals = dims.get("time");

		vals = checkDefaultValueTime(vals, time, headers);

		Operator[] os = new Operator[vals.size()];
		findTimeFilters(time, vals, os, headers);
		if (os.length > 1) {
			if (!time.getMultipleValues()) {
				String msg = "Multiple values are not allowed for TIME.";
				throw new OWSException(msg, "InvalidDimensionValue", "time");
			}
			try {
				ops.add(new Or(os));
			}
			catch (Throwable e) {
				// will not happen, look at the if condition
			}
		}
		else {
			ops.add(os[0]);
		}
	}

	private void findTimeFilters(Dimension<?> time, List<?> vals, Operator[] os, List<String> headers)
			throws OWSException {
		final ValueReference property = new ValueReference(time.getPropertyName());

		int i = 0;
		for (Object o : vals) {
			checkTimeValidity(time, o);
			Date theVal = null;
			if (o instanceof DimensionInterval<?, ?, ?>) {
				DimensionInterval<?, ?, ?> iv = (DimensionInterval<?, ?, ?>) o;
				final String min = formatDateTime((Date) iv.min);
				final String max = formatDateTime((Date) iv.max);
				os[i++] = new PropertyIsBetween(property, new Literal<PrimitiveValue>(min),
						new Literal<PrimitiveValue>(max), true, null);
			}
			else if (o.toString().equalsIgnoreCase("current")) {
				if (!time.getCurrent()) {
					String msg = "The value 'current' for TIME was invalid.";
					throw new OWSException(msg, "InvalidDimensionValue", "time");
				}
				theVal = new Date(currentTimeMillis());
			}
			else if (o instanceof Date) {
				theVal = (Date) o;
			}
			else if (o instanceof Temporal) {
				theVal = ((Temporal) o).getDate();
			}
			else {
				throw new RuntimeException("Unexpected dimension value class: " + o.getClass());
			}
			if (theVal != null) {
				theVal = checkNearestValueTime(theVal, time, headers);
				Literal<PrimitiveValue> lit = new Literal<PrimitiveValue>(formatDateTime(theVal));
				os[i++] = new PropertyIsEqualTo(property, lit, true, null);
			}
		}
	}

	private void checkTimeValidity(Dimension<?> time, Object o) throws OWSException {
		if (!time.getNearestValue() && !time.isValid(o)) {
			String msg = "The value " + (o instanceof Date ? formatDateTime((Date) o) : o.toString())
					+ " for dimension TIME was invalid.";
			throw new OWSException(msg, "InvalidDimensionValue", "time");
		}
	}

	private List<?> checkDefaultValueTime(List<?> vals, Dimension<?> time, List<String> headers) throws OWSException {
		if (vals == null) {
			vals = time.getDefaultValue();
			if (vals == null) {
				throw new OWSException("The TIME parameter was missing.", "MissingDimensionValue", "time");
			}
			String defVal = formatDimensionValueList(vals, true);

			headers.add("99 Default value used: time=" + defVal + " ISO8601");
		}
		return vals;
	}

	private Date checkNearestValueTime(Date theVal, Dimension<?> time, List<String> headers) {
		if (time.getNearestValue()) {
			Object nearest = time.getNearestValue(theVal);
			if (!nearest.equals(theVal)) {
				theVal = (Date) nearest;
				headers.add("99 Nearest value used: time=" + formatDateTime(theVal) + " " + time.getUnits());
			}
		}
		return theVal;
	}

}
