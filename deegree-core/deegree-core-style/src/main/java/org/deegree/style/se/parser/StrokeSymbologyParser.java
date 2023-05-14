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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.utils.ArrayUtils.splitAsDoubles;
import static org.deegree.commons.utils.ColorUtils.decodeWithAlpha;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;
import static org.deegree.style.styling.components.Stroke.LineCap.BUTT;
import static org.deegree.style.styling.components.Stroke.LineJoin.ROUND;
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
import org.deegree.style.styling.components.Graphic;
import org.deegree.style.styling.components.Stroke;
import org.deegree.style.styling.components.Stroke.LineCap;
import org.deegree.style.styling.components.Stroke.LineJoin;
import org.slf4j.Logger;

/**
 * Responsible for parsing all Stroke related parts.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class StrokeSymbologyParser {

	static final Logger LOG = getLogger(StrokeSymbologyParser.class);

	private SymbologyParserContext context;

	StrokeSymbologyParser(SymbologyParserContext context) {
		this.context = context;
	}

	Pair<Stroke, Continuation<Stroke>> parseStroke(XMLStreamReader in) throws XMLStreamException {
		in.require(START_ELEMENT, null, "Stroke");

		Stroke base = new Stroke();
		Continuation<Stroke> contn = null;

		while (!(in.isEndElement() && in.getLocalName().equals("Stroke"))) {
			in.nextTag();

			if (in.getLocalName().endsWith("Parameter")) {
				contn = parseParameter(contn, in, base);
			}
			else if (in.getLocalName().equals("GraphicFill")) {
				contn = parseGraphicFill(contn, in, base);
			}
			else if (in.getLocalName().equals("GraphicStroke")) {
				contn = parseGraphicStroke(contn, in, base);
			}
			else if (in.isStartElement()) {
				LOG.error("Found unknown element '{}', skipping.", in.getLocalName());
				skipElement(in);
			}
		}

		in.require(END_ELEMENT, null, "Stroke");

		return new Pair<Stroke, Continuation<Stroke>>(base, contn);
	}

	private Continuation<Stroke> parseParameter(Continuation<Stroke> contn, XMLStreamReader in, Stroke base)
			throws XMLStreamException {
		String name = in.getAttributeValue(null, "name");

		if (name.equals("stroke")) {
			contn = context.parser.updateOrContinue(in, "Parameter", base, new Updater<Stroke>() {
				@Override
				public void update(Stroke obj, String val) {
					// keep alpha value
					int alpha = obj.color.getAlpha();
					obj.color = decodeWithAlpha(val);
					obj.color = new Color(obj.color.getRed(), obj.color.getGreen(), obj.color.getBlue(), alpha);
				}
			}, contn).second;
		}
		else if (name.equals("stroke-opacity")) {
			contn = context.parser.updateOrContinue(in, "Parameter", base, new Updater<Stroke>() {
				@Override
				public void update(Stroke obj, String val) {
					// keep original color
					float alpha = Float.parseFloat(val);
					float[] cols = obj.color.getRGBColorComponents(null);
					obj.color = new Color(cols[0], cols[1], cols[2], alpha);
				}
			}, contn).second;
		}
		else if (name.equals("stroke-width")) {
			contn = context.parser.updateOrContinue(in, "Parameter", base, new Updater<Stroke>() {
				@Override
				public void update(Stroke obj, String val) {
					obj.width = Double.parseDouble(val);
				}
			}, contn).second;
		}
		else if (name.equals("stroke-linejoin")) {
			contn = context.parser.updateOrContinue(in, "Parameter", base, new Updater<Stroke>() {
				@Override
				public void update(Stroke obj, String val) {
					try {
						obj.linejoin = LineJoin.valueOf(val.toUpperCase());
					}
					catch (IllegalArgumentException e) {
						LOG.warn("Used invalid value '{}' for line join.", val);
						obj.linejoin = ROUND;
					}
				}
			}, contn).second;
		}
		else if (name.equals("stroke-linecap")) {
			contn = context.parser.updateOrContinue(in, "Parameter", base, new Updater<Stroke>() {
				@Override
				public void update(Stroke obj, String val) {
					try {
						obj.linecap = LineCap.valueOf(val.toUpperCase());
					}
					catch (IllegalArgumentException e) {
						LOG.warn("Used invalid value '{}' for line cap.", val);
						obj.linecap = BUTT;
					}
				}
			}, contn).second;
		}
		else if (name.equals("stroke-dasharray")) {
			contn = context.parser.updateOrContinue(in, "Parameter", base, new Updater<Stroke>() {
				@Override
				public void update(Stroke obj, String val) {
					// , is not strictly allowed, but we don't lose anything by being
					// flexible
					if (val.contains(",")) {
						obj.dasharray = splitAsDoubles(val, ",");
					}
					else {
						obj.dasharray = splitAsDoubles(val, "\\s");
					}
				}
			}, contn).second;
		}
		else if (name.equals("stroke-dashoffset")) {
			contn = context.parser.updateOrContinue(in, "Parameter", base, new Updater<Stroke>() {
				@Override
				public void update(Stroke obj, String val) {
					obj.dashoffset = Double.parseDouble(val);
				}
			}, contn).second;
		}
		else if (name.equals("deegree-graphicstroke-position-percentage")) {
			// Note: this is a deegree specific parameter and replaces the Element
			// PositionPercentage
			contn = context.parser.updateOrContinue(in, "Parameter", base, new Updater<Stroke>() {
				@Override
				public void update(Stroke obj, String val) {
					obj.positionPercentage = Double.parseDouble(val);
				}
			}, contn).second;
		}
		else if (name.equals("deegree-graphicstroke-rotation")) {
			// Note: this is a deegree specific parameter
			contn = context.parser.updateOrContinue(in, "Parameter", base, new Updater<Stroke>() {
				@Override
				public void update(Stroke obj, String val) {
					obj.positionRotation = Double.parseDouble(val) > 0;
				}
			}, contn).second;
		}
		else {
			Location loc = in.getLocation();
			LOG.error("Found unknown parameter '{}' at line {}, column {}, skipping.",
					new Object[] { name, loc.getLineNumber(), loc.getColumnNumber() });
			skipElement(in);
		}

		in.require(END_ELEMENT, null, null);
		return contn;
	}

	private Continuation<Stroke> parseGraphicFill(Continuation<Stroke> contn, XMLStreamReader in, Stroke base)
			throws XMLStreamException {
		in.nextTag();
		final Pair<Graphic, Continuation<Graphic>> pair = context.graphicParser.parseGraphic(in);
		if (pair != null) {
			base.fill = pair.first;
			if (pair.second != null) {
				contn = new Continuation<Stroke>(contn) {
					@Override
					public void updateStep(Stroke base, Feature f, XPathEvaluator<Feature> evaluator) {
						pair.second.evaluate(base.fill, f, evaluator);
					}
				};
			}
		}
		in.require(END_ELEMENT, null, "Graphic");
		in.nextTag();
		in.require(END_ELEMENT, null, "GraphicFill");
		return contn;
	}

	private Continuation<Stroke> parseGraphicStroke(Continuation<Stroke> contn, XMLStreamReader in, Stroke base)
			throws XMLStreamException {
		while (!(in.isEndElement() && in.getLocalName().equals("GraphicStroke"))) {
			in.nextTag();

			if (in.getLocalName().equals("Graphic")) {
				final Pair<Graphic, Continuation<Graphic>> pair = context.graphicParser.parseGraphic(in);

				if (pair != null) {
					base.stroke = pair.first;
					if (pair.second != null) {
						contn = new Continuation<Stroke>(contn) {
							@Override
							public void updateStep(Stroke base, Feature f, XPathEvaluator<Feature> evaluator) {
								pair.second.evaluate(base.stroke, f, evaluator);
							}
						};
					}
				}

				in.require(END_ELEMENT, null, "Graphic");
			}
			else if (in.getLocalName().equals("InitialGap")) {
				contn = context.parser.updateOrContinue(in, "InitialGap", base, new Updater<Stroke>() {
					@Override
					public void update(Stroke obj, String val) {
						obj.strokeInitialGap = Double.parseDouble(val);
					}
				}, contn).second;
				in.require(END_ELEMENT, null, "InitialGap");
			}
			else if (in.getLocalName().equals("Gap")) {
				contn = context.parser.updateOrContinue(in, "Gap", base, new Updater<Stroke>() {
					@Override
					public void update(Stroke obj, String val) {
						obj.strokeGap = Double.parseDouble(val);
					}
				}, contn).second;
				in.require(END_ELEMENT, null, "Gap");
			}
			else if (in.getLocalName().equals("PositionPercentage")) {
				Location loc = in.getLocation();
				LOG.warn("The use of {} at line {}, column {} is deprecated and will be removed in the future. {}",
						in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber(),
						"Use a Svg/CssParameter with the name 'deegree-graphicstroke-position-percentage' instead.");
				contn = context.parser.updateOrContinue(in, "PositionPercentage", base, new Updater<Stroke>() {
					@Override
					public void update(Stroke obj, String val) {
						obj.positionPercentage = Double.parseDouble(val);
					}
				}, contn).second;
				in.require(END_ELEMENT, null, "PositionPercentage");
			}
			else if (in.isStartElement()) {
				Location loc = in.getLocation();
				LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
						new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() });
				skipElement(in);
			}

		}
		return contn;
	}

}
