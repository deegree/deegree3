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

import static org.deegree.commons.tom.datetime.ISO8601Converter.formatDate;
import static org.deegree.commons.tom.datetime.ISO8601Converter.formatDateTime;
import static org.deegree.commons.tom.datetime.ISO8601Converter.formatTime;
import static org.deegree.commons.tom.datetime.ISO8601Converter.parseDate;
import static org.deegree.commons.tom.datetime.ISO8601Converter.parseDateTime;
import static org.deegree.commons.tom.datetime.ISO8601Converter.parseTime;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.datetime.Temporal;
import org.deegree.commons.tom.datetime.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts between internal object values and XML strings.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class XMLValueMangler {

	private static final Logger LOG = LoggerFactory.getLogger(XMLValueMangler.class);

	/**
	 * Returns the internal representation for the given XML string and {@link BaseType}.
	 * @param s
	 * @param pt
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static Object xmlToInternal(String s, BaseType pt) throws IllegalArgumentException {
		Object value = s;
		switch (pt) {
			case BOOLEAN: {
				if (s.equals("true") || s.equals("1")) {
					value = Boolean.TRUE;
				}
				else if (s.equals("false") || s.equals("0")) {
					value = Boolean.FALSE;
				}
				else {
					String msg = "Value ('" + s + "') is not valid with respect to the xs:boolean type. "
							+ "Valid values are 'true', 'false', '1' and '0'.";
					throw new IllegalArgumentException(msg);
				}
				break;
			}
			case DATE: {
				value = parseDate(s);
				break;
			}
			case DATE_TIME: {
				value = parseDateTime(s);
				break;
			}
			case DECIMAL: {
				value = new BigDecimal(s);
				break;
			}
			case DOUBLE: {
				value = new Double(s);
				break;
			}
			case INTEGER: {
				value = new BigInteger(s);
				break;
			}
			case STRING: {
				break;
			}
			case TIME: {
				try {
					value = parseTime(s);
				}
				catch (Exception e) {
					String msg = "Value ('" + s + "') is not valid with respect to the xs:time type.";
					throw new IllegalArgumentException(msg);
				}
				break;
			}
			default: {
				LOG.warn("Unhandled primitive type " + pt + " -- treating as string value.");
			}
		}
		return value;
	}

	static String internalToXML(Object o, BaseType pt) {
		String xml = null;
		if (o != null) {
			if (pt != null) {
				switch (pt) {
					case DATE:
						if (o instanceof Date) {
							xml = "" + formatDate((Temporal) o);
						}
						else {
							LOG.warn("Unhandled Date class " + o.getClass() + " -- converting via #toString()");
							xml = "" + o;
						}
						break;
					case DATE_TIME:
						if (o instanceof DateTime) {
							xml = "" + formatDateTime((Temporal) o);
						}
						else {
							LOG.warn("Unhandled Date class " + o.getClass() + " -- converting via #toString()");
							xml = "" + o;
						}
						break;
					case TIME: {
						if (o instanceof Time) {
							xml = "" + formatTime((Temporal) o);
						}
						else {
							LOG.warn("Unhandled Date class " + o.getClass() + " -- converting via #toString()");
							xml = "" + o;
						}
						break;
					}
					case STRING:
						xml = "" + o;
						break;
					case BOOLEAN:
						xml = "" + o;
						break;
					case DECIMAL:
						if (o instanceof BigDecimal) {
							xml = ((BigDecimal) o).toPlainString();
						}
						else {
							xml = "" + o;
						}
						break;
					case DOUBLE:
						xml = "" + o;
						break;
					case INTEGER:
						xml = "" + o;
						break;
					default: {
						LOG.warn("Unhandled primitive type " + pt + " -- treating as string value.");
						xml = "" + o;
					}
				}
			}
			else {
				xml = "" + o;
			}
		}
		return xml;
	}

}
