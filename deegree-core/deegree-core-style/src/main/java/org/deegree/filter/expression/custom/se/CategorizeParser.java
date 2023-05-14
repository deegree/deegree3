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
package org.deegree.filter.expression.custom.se;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.utils.ColorUtils.decodeWithAlpha;
import static org.deegree.style.se.unevaluated.Continuation.SBUPDATER;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.utils.Pair;
import org.deegree.style.se.parser.SymbologyParser;
import org.deegree.style.se.unevaluated.Continuation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses categorize expressions.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class CategorizeParser {

	private static final Logger LOG = LoggerFactory.getLogger(CategorizeParser.class);

	static Categorize parse(XMLStreamReader in) throws XMLStreamException {
		StringBuffer value = null;
		Continuation<StringBuffer> contn = null;
		boolean precedingBelongs = false;
		List<StringBuffer> values = new ArrayList<StringBuffer>();
		List<StringBuffer> thresholds = new ArrayList<StringBuffer>();
		LinkedList<Continuation<StringBuffer>> valueContns = new LinkedList<Continuation<StringBuffer>>();
		LinkedList<Continuation<StringBuffer>> thresholdContns = new LinkedList<Continuation<StringBuffer>>();

		in.require(START_ELEMENT, null, "Categorize");

		String belong = in.getAttributeValue(null, "thresholdsBelongTo");
		if (belong == null) {
			belong = in.getAttributeValue(null, "threshholdsBelongTo");
		}
		if (belong != null) {
			precedingBelongs = belong.equals("preceding");
		}

		while (!(in.isEndElement() && in.getLocalName().equals("Categorize"))) {
			in.nextTag();

			if (in.getLocalName().equals("LookupValue")) {
				value = new StringBuffer();
				contn = SymbologyParser.INSTANCE.updateOrContinue(in, "LookupValue", value, SBUPDATER, null).second;
			}

			if (in.getLocalName().equals("Threshold")) {
				StringBuffer sb = new StringBuffer();
				thresholdContns
					.add(SymbologyParser.INSTANCE.updateOrContinue(in, "Threshold", sb, SBUPDATER, null).second);
				thresholds.add(sb);
			}

			if (in.getLocalName().equals("Value")) {
				StringBuffer sb = new StringBuffer();
				valueContns.add(SymbologyParser.INSTANCE.updateOrContinue(in, "Value", sb, SBUPDATER, null).second);
				values.add(sb);
			}

		}
		in.require(END_ELEMENT, null, "Categorize");
		Pair<Color[], Float[]> lookup = buildLookupArrays(values, thresholds);
		Color[] valuesArray = lookup.first;
		Float[] thresholdsArray = lookup.second;
		return new Categorize(value, contn, precedingBelongs, values, valuesArray, thresholds, thresholdsArray,
				valueContns, thresholdContns);
	}

	/** Create the sorted lookup arrays from the StringBuffer lists */
	private static Pair<Color[], Float[]> buildLookupArrays(List<StringBuffer> values, List<StringBuffer> thresholds) {
		LOG.debug("Building look-up arrays, for binary search... ");
		Color[] valuesArray = null;
		Float[] thresholdsArray = null;

		{
			valuesArray = new Color[values.size()];
			List<Color> list = new ArrayList<Color>(values.size());
			Iterator<StringBuffer> i = values.iterator();
			while (i.hasNext()) {
				list.add(decodeWithAlpha(i.next().toString()));
			}
			valuesArray = list.toArray(valuesArray);
		}

		{
			thresholdsArray = new Float[thresholds.size()];
			List<Float> list = new ArrayList<Float>(thresholds.size());
			Iterator<StringBuffer> i = thresholds.iterator();
			while (i.hasNext()) {
				list.add(Float.parseFloat(i.next().toString()));
			}
			thresholdsArray = list.toArray(thresholdsArray);
		}
		return new Pair<Color[], Float[]>(valuesArray, thresholdsArray);
	}

}
