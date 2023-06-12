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
package org.deegree.commons.ows.metadata.domain;

/**
 * A range of values of a numeric parameter.
 * <p>
 * Data model has been designed to capture the expressiveness of all OWS specifications
 * and versions and was verified against the following specifications:
 * <ul>
 * <li>OWS Common 2.0</li>
 * </ul>
 * </p>
 * <p>
 * From OWS Common 2.0: <cite>A range of values of a numeric parameter. This range can be
 * continuous or discrete, defined by a fixed spacing between adjacent valid values. If
 * the MinimumValue or MaximumValue is not included, there is no value limit in that
 * direction. Inclusion of the specified minimum and maximum values in the range shall be
 * defined by the rangeClosure.</cite>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class Range implements Values {

	private String min;

	private String max;

	private String spacing;

	private RangeClosure closure;

	/**
	 * Creates a new {@link Range} instance.
	 * @param min minimum value, may be <code>null</code> (no lower limit)
	 * @param max maximum value, may be <code>null</code> (no upper limit)
	 * @param spacing spacing, may be <code>null</code> (continuous range)
	 * @param closure boundary value mode, may be <code>null</code> (implies
	 * {@link RangeClosure#CLOSED})
	 */
	public Range(String min, String max, String spacing, RangeClosure closure) {
		this.min = min;
		this.max = max;
		this.spacing = spacing;
		this.closure = closure;
	}

	/**
	 * Returns the minimum value.
	 * <p>
	 * From OWS Common 2.0: <cite>Minimum value of this numeric parameter.</cite>
	 * </p>
	 * @return minimum value, may be <code>null</code> (no lower limit)
	 */
	public String getMin() {
		return min;
	}

	/**
	 * Sets the minimum value.
	 * <p>
	 * From OWS Common 2.0: <cite>Minimum value of this numeric parameter.</cite>
	 * </p>
	 * @param min minimum value, may be <code>null</code> (no lower limit)
	 */
	public void setMin(String min) {
		this.min = min;
	}

	/**
	 * Returns the maximum value.
	 * <p>
	 * From OWS Common 2.0: <cite>Maximum value of this numeric parameter.</cite>
	 * </p>
	 * @return maximum value, may be <code>null</code> (no upper limit)
	 */
	public String getMax() {
		return max;
	}

	/**
	 * Sets the maximum value.
	 * <p>
	 * From OWS Common 2.0: <cite>Maximum value of this numeric parameter.</cite>
	 * </p>
	 * @param max maximum value, may be <code>null</code> (no lower limit)
	 */
	public void setMax(String max) {
		this.max = max;
	}

	/**
	 * Returns the spacing between values in the range.
	 * <p>
	 * From OWS Common 2.0: <cite>Shall be included when the allowed values are NOT
	 * continuous in this range. Shall not be included when the allowed values are
	 * continuous in this range.</cite>
	 * </p>
	 * @return spacing between values in the range, may be <code>null</code> (continuous
	 * range)
	 */
	public String getSpacing() {
		return spacing;
	}

	/**
	 * Sets the spacing between values in the range.
	 * <p>
	 * From OWS Common 2.0: <cite>Shall be included when the allowed values are NOT
	 * continuous in this range. Shall not be included when the allowed values are
	 * continuous in this range.</cite>
	 * </p>
	 * @param spacing spacing between values in the range, may be <code>null</code>
	 * (continuous range)
	 */
	public void setSpacing(String spacing) {
		this.spacing = spacing;
	}

	/**
	 * Returns the closure between values in the range.
	 * <p>
	 * From OWS Common 2.0: <cite>Shall be included unless the default value applies
	 * (closed).</cite>
	 * </p>
	 * @return closure, may be <code>null</code> (implies {@link RangeClosure#CLOSED})
	 */
	public RangeClosure getClosure() {
		return closure;
	}

	/**
	 * Sets the closure between values in the range.
	 * <p>
	 * From OWS Common 2.0: <cite>Shall be included unless the default value applies
	 * (closed).</cite>
	 * </p>
	 * @return closure, may be <code>null</code> (implies {@link RangeClosure#CLOSED})
	 */
	public void setClosure(RangeClosure closure) {
		this.closure = closure;
	}

}
