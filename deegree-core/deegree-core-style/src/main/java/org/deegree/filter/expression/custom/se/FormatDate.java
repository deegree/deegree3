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
package org.deegree.filter.expression.custom.se;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.SENS;
import static org.slf4j.LoggerFactory.getLogger;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.datetime.ISO8601Converter;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.Feature;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.custom.AbstractCustomExpression;
import org.deegree.style.se.parser.SymbologyParser;
import org.deegree.style.se.unevaluated.Continuation;
import org.deegree.style.se.unevaluated.Continuation.Updater;
import org.slf4j.Logger;

/**
 * <code>FormatDate</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class FormatDate extends AbstractCustomExpression {

	private static final QName ELEMENT_NAME = new QName(SENS, "FormatDate");

	private static final Logger LOG = getLogger(FormatDate.class);

	private StringBuffer dateValue;

	private Continuation<StringBuffer> dateValueContn;

	private SimpleDateFormat formatter;

	/***/
	public FormatDate() {
		// just used for SPI
	}

	private FormatDate(StringBuffer dateValue, Continuation<StringBuffer> dateValueContn, SimpleDateFormat formatter) {
		this.dateValue = dateValue;
		this.dateValueContn = dateValueContn;
		this.formatter = formatter;
	}

	@Override
	public QName getElementName() {
		return ELEMENT_NAME;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> TypedObjectNode[] evaluate(T obj, XPathEvaluator<T> xpathEvaluator) throws FilterEvaluationException {
		StringBuffer sb = new StringBuffer(dateValue.toString().trim());
		if (dateValueContn != null) {
			dateValueContn.evaluate(sb, (Feature) obj, (XPathEvaluator<Feature>) xpathEvaluator);
		}
		try {
			DateTime value = ISO8601Converter.parseDateTime(sb.toString().trim());
			return new TypedObjectNode[] { new PrimitiveValue(formatter.format(value)) };
		}
		catch (IllegalArgumentException e) {
			LOG.warn("Evaluated value could not be parsed as a date (in an argument to FormatDate).");
		}

		return new TypedObjectNode[] { new PrimitiveValue(sb.toString().trim()) };
	}

	@Override
	public FormatDate parse(XMLStreamReader in) throws XMLStreamException {

		StringBuffer dateValue = null;
		Continuation<StringBuffer> dateValueContn = null;
		SimpleDateFormat formatter = null;

		in.require(START_ELEMENT, null, "FormatDate");

		while (!(in.isEndElement() && in.getLocalName().equals("FormatDate"))) {
			in.nextTag();

			if (in.getLocalName().equals("DateValue")) {
				dateValue = new StringBuffer();
				dateValueContn = SymbologyParser.INSTANCE.updateOrContinue(in, "DateValue", dateValue,
						new Updater<StringBuffer>() {
							public void update(StringBuffer obj, String val) {
								obj.append(val);
							}
						}, null).second;
			}

			if (in.getLocalName().equals("Pattern")) {
				String pat = in.getElementText();
				Locale locale = Locale.getDefault();
				if (pat.contains("MMM")) {
					List<String> langs = Arrays.asList(Locale.getISOLanguages());
					String code = pat.substring(pat.lastIndexOf("MMM") + 3, pat.lastIndexOf("MMM") + 5);
					if (langs.contains(code)) {
						pat = pat.replace("MMM" + code, "MMM");
						locale = new Locale(code);
					}
				}
				pat = pat.replace('D', 'd');
				while (pat.indexOf("\\") != -1) {
					String q = "" + pat.charAt(pat.indexOf("\\") + 1);
					if (q.equals("'")) {
						pat = pat.replace("\\'", "''");
					}
					else {
						pat = pat.replace("\\" + q, "'" + q + "'");
					}
				}

				formatter = new SimpleDateFormat(pat, locale);
			}
		}

		in.require(END_ELEMENT, null, "FormatDate");
		return new FormatDate(dateValue, dateValueContn, formatter);
	}

}