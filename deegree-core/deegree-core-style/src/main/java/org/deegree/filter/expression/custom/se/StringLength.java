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
import static org.deegree.style.se.unevaluated.Continuation.SBUPDATER;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.Feature;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.custom.AbstractCustomExpression;
import org.deegree.style.se.parser.SymbologyParser;
import org.deegree.style.se.unevaluated.Continuation;

/**
 * <code>StringLength</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class StringLength extends AbstractCustomExpression {

	private static final QName ELEMENT_NAME = new QName(SENS, "StringLength");

	private StringBuffer value;

	private Continuation<StringBuffer> contn;

	/**
	 *
	 */
	public StringLength() {
		// just used for SPI
	}

	private StringLength(StringBuffer value, Continuation<StringBuffer> contn) {
		this.value = value;
		this.contn = contn;
	}

	@Override
	public QName getElementName() {
		return ELEMENT_NAME;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> TypedObjectNode[] evaluate(T obj, XPathEvaluator<T> xpathEvaluator) throws FilterEvaluationException {
		StringBuffer sb = new StringBuffer(value.toString().trim());
		if (contn != null) {
			contn.evaluate(sb, (Feature) obj, (XPathEvaluator<Feature>) xpathEvaluator);
		}
		return new TypedObjectNode[] { new PrimitiveValue(sb.length() + "") };
	}

	@Override
	public StringLength parse(XMLStreamReader in) throws XMLStreamException {

		StringBuffer value = null;
		Continuation<StringBuffer> contn = null;

		in.require(START_ELEMENT, null, "StringLength");
		while (!(in.isEndElement() && in.getLocalName().equals("StringLength"))) {
			in.nextTag();

			if (in.getLocalName().equals("StringValue")) {
				value = new StringBuffer();
				contn = SymbologyParser.INSTANCE.updateOrContinue(in, "StringValue", value, SBUPDATER, null).second;
			}
		}
		in.require(END_ELEMENT, null, "StringLength");
		return new StringLength(value, contn);
	}

}