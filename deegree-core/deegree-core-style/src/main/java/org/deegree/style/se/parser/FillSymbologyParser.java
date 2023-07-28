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
package org.deegree.style.se.parser;

import static java.lang.Float.parseFloat;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.utils.ColorUtils.decodeWithAlpha;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.utils.Pair;
import org.deegree.feature.Feature;
import org.deegree.filter.XPathEvaluator;
import org.deegree.style.se.unevaluated.Continuation;
import org.deegree.style.se.unevaluated.Continuation.Updater;
import org.deegree.style.styling.components.Fill;
import org.deegree.style.styling.components.Graphic;
import org.slf4j.Logger;

/**
 * Responsible for parsing SE Fill elements.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class FillSymbologyParser {

	static final Logger LOG = getLogger(FillSymbologyParser.class);

	private SymbologyParserContext context;

	FillSymbologyParser(SymbologyParserContext context) {
		this.context = context;
	}

	Pair<Fill, Continuation<Fill>> parseFill(XMLStreamReader in) throws XMLStreamException {
		in.require(START_ELEMENT, null, "Fill");

		Fill base = new Fill();
		Continuation<Fill> contn = null;

		while (!(in.isEndElement() && in.getLocalName().equals("Fill"))) {
			in.nextTag();

			if (in.getLocalName().equals("GraphicFill")) {
				in.nextTag();
				final Pair<Graphic, Continuation<Graphic>> pair = context.graphicParser.parseGraphic(in);
				if (pair != null) {
					base.graphic = pair.first;
					if (pair.second != null) {
						contn = new Continuation<Fill>(contn) {
							@Override
							public void updateStep(Fill base, Feature f, XPathEvaluator<Feature> evaluator) {
								pair.second.evaluate(base.graphic, f, evaluator);
							}
						};
					}
				}
				in.nextTag();
			}
			else if (in.getLocalName().endsWith("Parameter")) {
				String cssName = in.getAttributeValue(null, "name");
				if (cssName.equals("fill")) {
					contn = context.parser.updateOrContinue(in, "Parameter", base, new Updater<Fill>() {
						@Override
						public void update(Fill obj, String val) {
							// keep alpha value
							int alpha = obj.color.getAlpha();
							obj.color = decodeWithAlpha(val);
							obj.color = new Color(obj.color.getRed(), obj.color.getGreen(), obj.color.getBlue(), alpha);
						}
					}, contn).second;
				}

				if (cssName.equals("fill-opacity")) {
					contn = context.parser.updateOrContinue(in, "Parameter", base, new Updater<Fill>() {
						@Override
						public void update(Fill obj, String val) {
							// keep original color
							float alpha = max(0, min(1, parseFloat(val)));
							float[] cols = obj.color.getRGBColorComponents(null);
							obj.color = new Color(cols[0], cols[1], cols[2], alpha);
						}
					}, contn).second;
				}
			}
			else if (in.isStartElement()) {
				Location loc = in.getLocation();
				LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
						new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() });
				skipElement(in);
			}

		}

		in.require(END_ELEMENT, null, "Fill");

		return new Pair<Fill, Continuation<Fill>>(base, contn);
	}

}
