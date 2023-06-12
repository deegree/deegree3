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

package org.deegree.style.se.parser;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Double.parseDouble;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.SENS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getElementTextAsBoolean;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getElementTextAsRelaxedQName;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;
import static org.deegree.filter.xml.Filter110XMLDecoder.parseExpression;
import static org.deegree.style.se.parser.SymbologyParsingHelper.parseCommon;
import static org.deegree.style.styling.components.UOM.Foot;
import static org.deegree.style.styling.components.UOM.Metre;
import static org.deegree.style.styling.components.UOM.Pixel;
import static org.deegree.style.styling.components.UOM.mm;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.feature.Feature;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.custom.se.Categorize;
import org.deegree.filter.expression.custom.se.Interpolate;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.filter.xml.Filter110XMLEncoder;
import org.deegree.style.persistence.StyleStore;
import org.deegree.style.se.parser.SymbologyParsingHelper.Common;
import org.deegree.style.se.unevaluated.Continuation;
import org.deegree.style.se.unevaluated.Continuation.Updater;
import org.deegree.style.se.unevaluated.Symbolizer;
import org.deegree.style.styling.LineStyling;
import org.deegree.style.styling.PointStyling;
import org.deegree.style.styling.PolygonStyling;
import org.deegree.style.styling.RasterChannelSelection;
import org.deegree.style.styling.RasterStyling;
import org.deegree.style.styling.RasterStyling.ContrastEnhancement;
import org.deegree.style.styling.RasterStyling.Overlap;
import org.deegree.style.styling.RasterStyling.ShadedRelief;
import org.deegree.style.styling.TextStyling;
import org.deegree.style.styling.components.Fill;
import org.deegree.style.styling.components.Font;
import org.deegree.style.styling.components.Font.Style;
import org.deegree.style.styling.components.Graphic;
import org.deegree.style.styling.components.Halo;
import org.deegree.style.styling.components.LinePlacement;
import org.deegree.style.styling.components.PerpendicularOffsetType;
import org.deegree.style.styling.components.PerpendicularOffsetType.Substraction;
import org.deegree.style.styling.components.PerpendicularOffsetType.Type;
import org.deegree.style.styling.components.Stroke;
import org.deegree.style.styling.components.UOM;
import org.deegree.workspace.ResourceLocation;
import org.slf4j.Logger;

/**
 * <code>SymbologyParser</code> parses the SE part of 1.1.0 and the corresponding SLD
 * 1.0.0 part.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class SymbologyParser {

	private boolean collectXMLSnippets = false;

	static final Logger LOG = getLogger(SymbologyParser.class);

	/**
	 * A static elsefilter instance (think of it as a marker).
	 */
	public static final ElseFilter ELSEFILTER = new ElseFilter();

	/**
	 * A default instance.
	 */
	public static final SymbologyParser INSTANCE = new SymbologyParser();

	private SymbologyParserContext context = new SymbologyParserContext(this);

	private ResourceLocation<StyleStore> location;

	/**
	 * Constructs one which does not collect source snippets.
	 */
	public SymbologyParser() {

	}

	/**
	 * @param collectXMLSnippets if true, some source snippets are collected (which can be
	 * used for re-export)
	 */
	public SymbologyParser(boolean collectXMLSnippets) {
		this.collectXMLSnippets = collectXMLSnippets;
	}

	/**
	 * @param location used to resolve external resources
	 */
	public SymbologyParser(ResourceLocation<StyleStore> location) {
		this.location = location;
		context.location = location;
	}

	private static boolean require(XMLStreamReader in, String elementName) {
		if (!(in.getLocalName().equals(elementName) && in.isStartElement())) {
			Location loc = in.getLocation();
			LOG.error("Expected a '{}' element at line {} column {}.",
					new Object[] { elementName, loc.getLineNumber(), loc.getColumnNumber() });
			return false;
		}
		return true;
	}

	/**
	 * @param in
	 * @return the resolved href attribute
	 * @throws XMLStreamException
	 * @throws MalformedURLException
	 */
	public URL parseOnlineResource(XMLStreamReader in) throws XMLStreamException, MalformedURLException {
		if (!require(in, "OnlineResource")) {
			return null;
		}
		String url = in.getAttributeValue(XLNNS, "href");
		URL resolved;
		if (location != null) {
			resolved = location.resolveToUrl(url);
		}
		else {
			resolved = XMLStreamUtils.resolve(url, in);
		}
		in.nextTag();
		in.require(END_ELEMENT, null, "OnlineResource");
		return resolved;
	}

	/**
	 * @param in
	 * @param uom
	 * @return a new symbolizer
	 * @throws XMLStreamException
	 */
	public Symbolizer<PointStyling> parsePointSymbolizer(XMLStreamReader in, UOM uom) throws XMLStreamException {
		in.require(START_ELEMENT, null, "PointSymbolizer");

		Common common = new Common(in.getLocation());
		PointStyling baseOrEvaluated = new PointStyling();
		baseOrEvaluated.uom = uom;

		while (!(in.isEndElement() && in.getLocalName().equals("PointSymbolizer"))) {
			in.nextTag();

			parseCommon(common, in);

			if (in.getLocalName().equals("Graphic")) {
				final Pair<Graphic, Continuation<Graphic>> pair = context.graphicParser.parseGraphic(in);

				baseOrEvaluated.graphic = pair.first;

				if (pair.second != null) {
					return new Symbolizer<PointStyling>(baseOrEvaluated, new Continuation<PointStyling>() {
						@Override
						public void updateStep(PointStyling base, Feature f, XPathEvaluator<Feature> evaluator) {
							pair.second.evaluate(base.graphic, f, evaluator);
						}
					}, common.geometry, null, common.loc, common.line, common.col);
				}
			}
			else if (in.isStartElement()) {
				Location loc = in.getLocation();
				LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
						new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() });
				skipElement(in);
			}
		}

		in.require(END_ELEMENT, null, "PointSymbolizer");
		return new Symbolizer<PointStyling>(baseOrEvaluated, common.geometry, common.name, common.loc, common.line,
				common.col);
	}

	static UOM getUOM(String uom) {
		if (uom != null) {
			String u = uom.toLowerCase();
			if (u.endsWith("metre") || u.endsWith("meter")) {
				return Metre;
			}
			else if (u.endsWith("mm")) {
				return mm;
			}
			else if (u.endsWith("foot")) {
				return Foot;
			}
			else if (!u.endsWith("pixel")) {
				LOG.warn("Unknown unit of measure '{}', using pixel instead.", uom);
			}
		}

		return Pixel;
	}

	/**
	 * @param in
	 * @return the symbolizer
	 * @throws XMLStreamException
	 */
	public Triple<Symbolizer<?>, Continuation<StringBuffer>, String> parseSymbolizer(XMLStreamReader in)
			throws XMLStreamException {
		in.require(START_ELEMENT, null, null);
		if (in.getLocalName().endsWith("Symbolizer")) {
			UOM uom = getUOM(in.getAttributeValue(null, "uom"));

			if (in.getLocalName().equals("PointSymbolizer")) {
				return new Triple<Symbolizer<?>, Continuation<StringBuffer>, String>(parsePointSymbolizer(in, uom),
						null, null);
			}
			if (in.getLocalName().equals("LineSymbolizer")) {
				return new Triple<Symbolizer<?>, Continuation<StringBuffer>, String>(parseLineSymbolizer(in, uom), null,
						null);
			}
			if (in.getLocalName().equals("PolygonSymbolizer")) {
				return new Triple<Symbolizer<?>, Continuation<StringBuffer>, String>(parsePolygonSymbolizer(in, uom),
						null, null);
			}
			if (in.getLocalName().equals("RasterSymbolizer")) {
				return new Triple<Symbolizer<?>, Continuation<StringBuffer>, String>(parseRasterSymbolizer(in, uom),
						null, null);
			}
			if (in.getLocalName().equals("TextSymbolizer")) {
				return (Triple) parseTextSymbolizer(in, uom);
			}
			Location loc = in.getLocation();
			LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
					new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() });
			skipElement(in);
		}
		return null;
	}

	/**
	 * @param in
	 * @param uom
	 * @return the symbolizer
	 * @throws XMLStreamException
	 */
	public Symbolizer<RasterStyling> parseRasterSymbolizer(XMLStreamReader in, UOM uom) throws XMLStreamException {
		in.require(START_ELEMENT, null, "RasterSymbolizer");

		Common common = new Common(in.getLocation());
		RasterStyling baseOrEvaluated = new RasterStyling();
		baseOrEvaluated.uom = uom;
		Continuation<RasterStyling> contn = null;

		while (!(in.isEndElement() && in.getLocalName().equals("RasterSymbolizer"))) {
			in.nextTag();

			parseCommon(common, in);

			if (in.getLocalName().equals("Opacity")) {
				contn = updateOrContinue(in, "Opacity", baseOrEvaluated, new Updater<RasterStyling>() {
					@Override
					public void update(RasterStyling obj, String val) {
						obj.opacity = Double.parseDouble(val);
					}
				}, contn).second;
			}
			else if (in.getLocalName().equals("ChannelSelection")) {
				String red = null, green = null, blue = null, gray = null;
				HashMap<String, ContrastEnhancement> enhancements = new HashMap<String, ContrastEnhancement>(10);

				while (!(in.isEndElement() && in.getLocalName().equals("ChannelSelection"))) {
					in.nextTag();

					if (in.getLocalName().equals("RedChannel")) {
						in.nextTag();
						in.require(START_ELEMENT, null, "SourceChannelName");
						red = in.getElementText();
						in.nextTag();
						ContrastEnhancement enh = parseContrastEnhancement(in);
						if (enh != null) {
							enhancements.put("red", enh);
						}
						in.nextTag();
					}
					else if (in.getLocalName().equals("GreenChannel")) {
						in.nextTag();
						in.require(START_ELEMENT, null, "SourceChannelName");
						green = in.getElementText();
						in.nextTag();
						ContrastEnhancement enh = parseContrastEnhancement(in);
						if (enh != null) {
							enhancements.put("green", enh);
						}
						in.nextTag();
					}
					else if (in.getLocalName().equals("BlueChannel")) {
						in.nextTag();
						in.require(START_ELEMENT, null, "SourceChannelName");
						blue = in.getElementText();
						in.nextTag();
						ContrastEnhancement enh = parseContrastEnhancement(in);
						if (enh != null) {
							enhancements.put("blue", enh);
						}
						in.nextTag();
					}
					else if (in.getLocalName().equals("GrayChannel")) {
						in.nextTag();
						in.require(START_ELEMENT, null, "SourceChannelName");
						gray = in.getElementText();
						in.nextTag();
						ContrastEnhancement enh = parseContrastEnhancement(in);
						if (enh != null) {
							enhancements.put("gray", enh);
						}
						in.nextTag();
					}
					else if (in.isStartElement()) {
						Location loc = in.getLocation();
						LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
								new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() });
						skipElement(in);
					}
				}

				baseOrEvaluated.channelSelection = new RasterChannelSelection(red, green, blue, gray, enhancements);
			}
			else if (in.getLocalName().equals("OverlapBehavior")) {
				// actual difference between SLD 1.0.0/SE 1.1.0
				if (in.getNamespaceURI().equals(SENS)) {
					baseOrEvaluated.overlap = Overlap.valueOf(in.getElementText());
				}
				else {
					in.nextTag();
					baseOrEvaluated.overlap = Overlap.valueOf(in.getLocalName());
					in.nextTag();
					in.nextTag();
				}
			}
			else if (in.getLocalName().equals("ColorMap")) {
				if (in.getNamespaceURI().equals(SENS)) {
					in.nextTag();

					if (in.getLocalName().equals("Categorize")) {
						baseOrEvaluated.categorize = new Categorize().parse(in);
					}
					else if (in.getLocalName().equals("Interpolate")) {
						baseOrEvaluated.interpolate = new Interpolate().parse(in);
					}
					else if (in.isStartElement()) {
						Location loc = in.getLocation();
						LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
								new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() });
						skipElement(in);
					}

					in.nextTag();
				}
				else {
					baseOrEvaluated.interpolate = Interpolate.parseSLD100(in);
				}
			}
			else if (in.getLocalName().equals("ContrastEnhancement")) {
				baseOrEvaluated.contrastEnhancement = parseContrastEnhancement(in);
			}
			else if (in.getLocalName().equals("ShadedRelief")) {
				baseOrEvaluated.shaded = new ShadedRelief();
				while (!(in.isEndElement() && in.getLocalName().equals("ShadedRelief"))) {
					in.nextTag();

					if (in.getLocalName().equals("BrightnessOnly")) {
						baseOrEvaluated.shaded.brightnessOnly = getElementTextAsBoolean(in);
					}
					if (in.getLocalName().equals("ReliefFactor")) {
						baseOrEvaluated.shaded.reliefFactor = parseDouble(in.getElementText());
					}
					if (in.getLocalName().equals("AzimuthAngle")) {
						baseOrEvaluated.shaded.azimuthAngle = parseDouble(in.getElementText());
					}
					if (in.getLocalName().equals("IlluminationAngle")) {
						baseOrEvaluated.shaded.alt = parseDouble(in.getElementText());
					}
				}
			}
			else if (in.getLocalName().equals("ImageOutline")) {
				in.nextTag();
				if (in.getLocalName().equals("LineSymbolizer")) {
					baseOrEvaluated.imageOutline = parseLineSymbolizer(in, getUOM(in.getAttributeValue(null, "uom")));
				}
				if (in.getLocalName().equals("PolygonSymbolizer")) {
					baseOrEvaluated.imageOutline = parsePolygonSymbolizer(in,
							getUOM(in.getAttributeValue(null, "uom")));
				}
				in.nextTag();
			}
			else if (in.isStartElement()) {
				Location loc = in.getLocation();
				LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
						new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() });
				skipElement(in);
			}
		}

		in.require(END_ELEMENT, null, "RasterSymbolizer");
		return new Symbolizer<RasterStyling>(baseOrEvaluated, contn, common.geometry, common.name, common.loc,
				common.line, common.col);
	}

	private ContrastEnhancement parseContrastEnhancement(XMLStreamReader in) throws XMLStreamException {
		if (!in.getLocalName().equals("ContrastEnhancement")) {
			return null;
		}

		ContrastEnhancement base = new ContrastEnhancement();

		while (!(in.isEndElement() && in.getLocalName().equals("ContrastEnhancement"))) {
			in.nextTag();

			if (in.getLocalName().equals("Normalize")) {
				in.nextTag();
				base.normalize = true;
			}
			else if (in.getLocalName().equals("Histogram")) {
				base.histogram = true;
			}
			else if (in.getLocalName().equals("GammaValue")) {
				base.gamma = parseDouble(in.getElementText());
			}
			else if (in.isStartElement()) {
				Location loc = in.getLocation();
				LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
						new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() });
				skipElement(in);
			}
		}

		return base;
	}

	/**
	 * @param in
	 * @param uom
	 * @return the symbolizer
	 * @throws XMLStreamException
	 */
	public Symbolizer<LineStyling> parseLineSymbolizer(XMLStreamReader in, UOM uom) throws XMLStreamException {
		in.require(START_ELEMENT, null, "LineSymbolizer");

		Common common = new Common(in.getLocation());
		LineStyling baseOrEvaluated = new LineStyling();
		baseOrEvaluated.uom = uom;
		Continuation<LineStyling> contn = null;

		while (!(in.isEndElement() && in.getLocalName().equals("LineSymbolizer"))) {
			in.nextTag();

			parseCommon(common, in);

			if (in.getLocalName().equals("Stroke")) {
				final Pair<Stroke, Continuation<Stroke>> pair = context.strokeParser.parseStroke(in);

				if (pair != null) {
					baseOrEvaluated.stroke = pair.first;

					if (pair.second != null) {
						contn = new Continuation<LineStyling>(contn) {
							@Override
							public void updateStep(LineStyling base, Feature f, XPathEvaluator<Feature> evaluator) {
								pair.second.evaluate(base.stroke, f, evaluator);
							}
						};
					}
				}
			}
			else if (in.getLocalName().equals("PerpendicularOffset")) {
				baseOrEvaluated.perpendicularOffsetType = getPerpendicularOffsetType(in);
				contn = updateOrContinue(in, "PerpendicularOffset", baseOrEvaluated, new Updater<LineStyling>() {
					@Override
					public void update(LineStyling obj, String val) {
						obj.perpendicularOffset = Double.parseDouble(val);
					}
				}, contn).second;
			}
			else if (in.isStartElement()) {
				Location loc = in.getLocation();
				LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
						new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() });
				skipElement(in);
			}
		}

		if (contn == null) {
			return new Symbolizer<LineStyling>(baseOrEvaluated, common.geometry, common.name, common.loc, common.line,
					common.col);
		}

		return new Symbolizer<LineStyling>(baseOrEvaluated, contn, common.geometry, common.name, common.loc,
				common.line, common.col);
	}

	/**
	 * @param in
	 * @param uom
	 * @return the symbolizer
	 * @throws XMLStreamException
	 */
	public Symbolizer<PolygonStyling> parsePolygonSymbolizer(XMLStreamReader in, UOM uom) throws XMLStreamException {
		in.require(START_ELEMENT, null, "PolygonSymbolizer");

		Common common = new Common(in.getLocation());
		PolygonStyling baseOrEvaluated = new PolygonStyling();
		baseOrEvaluated.uom = uom;
		Continuation<PolygonStyling> contn = null;

		while (!(in.isEndElement() && in.getLocalName().equals("PolygonSymbolizer"))) {
			in.nextTag();

			parseCommon(common, in);

			if (in.getLocalName().equals("Stroke")) {
				final Pair<Stroke, Continuation<Stroke>> pair = context.strokeParser.parseStroke(in);

				if (pair != null) {
					baseOrEvaluated.stroke = pair.first;

					if (pair.second != null) {
						contn = new Continuation<PolygonStyling>(contn) {
							@Override
							public void updateStep(PolygonStyling base, Feature f, XPathEvaluator<Feature> evaluator) {
								pair.second.evaluate(base.stroke, f, evaluator);
							}
						};
					}
				}
			}
			else if (in.getLocalName().equals("Fill")) {
				final Pair<Fill, Continuation<Fill>> fillPair = context.fillParser.parseFill(in);

				if (fillPair != null) {
					baseOrEvaluated.fill = fillPair.first;

					if (fillPair.second != null) {
						contn = new Continuation<PolygonStyling>(contn) {
							@Override
							public void updateStep(PolygonStyling base, Feature f, XPathEvaluator<Feature> evaluator) {
								fillPair.second.evaluate(base.fill, f, evaluator);
							}
						};
					}
				}
			}
			else if (in.getLocalName().equals("PerpendicularOffset")) {
				baseOrEvaluated.perpendicularOffsetType = getPerpendicularOffsetType(in);
				contn = updateOrContinue(in, "PerpendicularOffset", baseOrEvaluated, new Updater<PolygonStyling>() {
					@Override
					public void update(PolygonStyling obj, String val) {
						obj.perpendicularOffset = Double.parseDouble(val);
					}
				}, contn).second;
			}
			else if (in.getLocalName().equals("Displacement")) {
				while (!(in.isEndElement() && in.getLocalName().equals("Displacement"))) {
					in.nextTag();

					if (in.getLocalName().equals("DisplacementX")) {
						contn = updateOrContinue(in, "DisplacementX", baseOrEvaluated, new Updater<PolygonStyling>() {
							@Override
							public void update(PolygonStyling obj, String val) {
								obj.displacementX = Double.parseDouble(val);
							}
						}, contn).second;
					}
					else if (in.getLocalName().equals("DisplacementY")) {
						contn = updateOrContinue(in, "DisplacementY", baseOrEvaluated, new Updater<PolygonStyling>() {
							@Override
							public void update(PolygonStyling obj, String val) {
								obj.displacementY = Double.parseDouble(val);
							}
						}, contn).second;
					}
					else if (in.isStartElement()) {
						Location loc = in.getLocation();
						LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
								new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() });
						skipElement(in);
					}
				}
			}
			else if (in.isStartElement()) {
				Location loc = in.getLocation();
				LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
						new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() });
				skipElement(in);
			}
		}

		if (contn == null) {
			return new Symbolizer<PolygonStyling>(baseOrEvaluated, common.geometry, common.name, common.loc,
					common.line, common.col);
		}

		return new Symbolizer<PolygonStyling>(baseOrEvaluated, contn, common.geometry, common.name, common.loc,
				common.line, common.col);
	}

	/**
	 * @param <T>
	 * @param in
	 * @param name
	 * @param obj
	 * @param updater
	 * @param contn
	 * @return either contn, or a new continuation which updates obj, also the XML snippet
	 * (w/ filter expressions re-exported) which was parsed (or null, if none was parsed)
	 * @throws XMLStreamException
	 */
	public <T> Pair<String, Continuation<T>> updateOrContinue(XMLStreamReader in, String name, T obj,
			final Updater<T> updater, Continuation<T> contn) throws XMLStreamException {
		StringBuilder xmlText = collectXMLSnippets ? new StringBuilder() : null;

		if (in.getLocalName().endsWith(name)) {
			final LinkedList<Pair<String, Pair<Expression, String>>> text = new LinkedList<Pair<String, Pair<Expression, String>>>(); // no
			// real 'alternative', have we?
			boolean textOnly = true;
			while (!(in.isEndElement() && in.getLocalName().endsWith(name))) {
				in.next();
				if (in.isStartElement()) {
					Expression expr = parseExpression(in);
					if (collectXMLSnippets) {
						StringWriter sw = new StringWriter();
						XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
						Filter110XMLEncoder.export(expr, out);
						xmlText.append(sw.toString());
					}
					Pair<Expression, String> second;
					second = new Pair<Expression, String>(expr,
							"Error parsing SLD/SE, file " + in.getLocation().getSystemId() + ", line "
									+ in.getLocation().getLineNumber() + ", column "
									+ in.getLocation().getColumnNumber());
					text.add(new Pair<String, Pair<Expression, String>>(null, second));
					textOnly = false;
				}
				if (in.isCharacters()) {
					if (collectXMLSnippets) {
						xmlText.append(in.getText());
					}
					if (textOnly && !text.isEmpty()) { // concat text in case of multiple
														// text nodes from
						// beginning
						String txt = text.removeLast().first;
						text.add(new Pair<String, Pair<Expression, String>>(txt + in.getText().trim(), null));
					}
					else {
						text.add(new Pair<String, Pair<Expression, String>>(in.getText().trim(), null));
					}
				}
			}
			in.require(END_ELEMENT, null, null);

			if (textOnly) {
				if (text.isEmpty()) {
					LOG.warn("Expression was empty at line {}, column {}.", in.getLocation().getLineNumber(),
							in.getLocation().getColumnNumber());
				}
				updater.update(obj, text.isEmpty() ? "" : text.getFirst().first);
			}
			else {
				contn = new Continuation<T>(contn) {
					@Override
					public void updateStep(T base, Feature f, XPathEvaluator<Feature> evaluator) {
						StringBuilder tmp = new StringBuilder();
						for (Pair<String, Pair<Expression, String>> p : text) {
							if (p.first != null) {
								tmp.append(p.first);
							}
							if (p.second != null) {
								try {
									TypedObjectNode[] evald = p.second.first.evaluate(f, evaluator);
									if (evald.length == 0) {
										LOG.warn("The following expression in a style evaluated to null:\n'{}'",
												p.second.second);
									}
									else {
										tmp.append(evald[0]);
									}
								}
								catch (FilterEvaluationException e) {
									LOG.warn("Evaluating the following expression resulted in an error '{}':\n'{}'",
											e.getLocalizedMessage(), p.second.second);
								}
							}
						}

						updater.update(base, tmp.toString());
					}
				};
			}
		}

		return new Pair<String, Continuation<T>>(collectXMLSnippets ? xmlText.toString().trim() : null, contn);
	}

	/**
	 * @param in
	 * @param uom
	 * @return the symbolizer
	 * @throws XMLStreamException
	 */
	public Triple<Symbolizer<TextStyling>, Continuation<StringBuffer>, String> parseTextSymbolizer(XMLStreamReader in,
			UOM uom) throws XMLStreamException {
		in.require(START_ELEMENT, null, "TextSymbolizer");

		Common common = new Common(in.getLocation());
		TextStyling baseOrEvaluated = new TextStyling();
		baseOrEvaluated.uom = uom;
		Continuation<TextStyling> contn = null;
		Continuation<StringBuffer> label = null;
		String xmlText = null;

		while (!(in.isEndElement() && in.getLocalName().equals("TextSymbolizer"))) {
			in.nextTag();

			parseCommon(common, in);

			if (in.getLocalName().equals("Label")) {
				Pair<String, Continuation<StringBuffer>> res = updateOrContinue(in, "Label", new StringBuffer(),
						new Updater<StringBuffer>() {
							@Override
							public void update(StringBuffer obj, String val) {
								obj.append(val);
							}
						}, null);
				xmlText = res.first;
				label = res.second;
			}
			else if (in.getLocalName().equals("LabelPlacement")) {
				while (!(in.isEndElement() && in.getLocalName().equalsIgnoreCase("LabelPlacement"))) {
					in.nextTag();

					if (in.getLocalName().equalsIgnoreCase("PointPlacement")) {
						String cssName = in.getAttributeValue(null, "auto");
						baseOrEvaluated.auto = (cssName != null && cssName.equalsIgnoreCase("true"));

						while (!(in.isEndElement() && in.getLocalName().equals("PointPlacement"))) {
							in.nextTag();
							if (in.getLocalName().equals("AnchorPoint")) {
								while (!(in.isEndElement() && in.getLocalName().equals("AnchorPoint"))) {
									in.nextTag();
									if (in.getLocalName().equals("AnchorPointX")) {
										contn = updateOrContinue(in, "AnchorPointX", baseOrEvaluated,
												new Updater<TextStyling>() {
													@Override
													public void update(TextStyling obj, String val) {
														obj.anchorPointX = Double.parseDouble(val);
													}
												}, contn).second;
									}
									else if (in.getLocalName().equals("AnchorPointY")) {
										contn = updateOrContinue(in, "AnchorPointY", baseOrEvaluated,
												new Updater<TextStyling>() {
													@Override
													public void update(TextStyling obj, String val) {
														obj.anchorPointY = Double.parseDouble(val);
													}
												}, contn).second;
									}
									else if (in.isStartElement()) {
										Location loc = in.getLocation();
										LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
												new Object[] { in.getLocalName(), loc.getLineNumber(),
														loc.getColumnNumber() });
										skipElement(in);
									}
								}
							}
							else if (in.getLocalName().equals("Displacement")) {
								while (!(in.isEndElement() && in.getLocalName().equals("Displacement"))) {
									in.nextTag();
									if (in.getLocalName().equals("DisplacementX")) {
										contn = updateOrContinue(in, "DisplacementX", baseOrEvaluated,
												new Updater<TextStyling>() {
													@Override
													public void update(TextStyling obj, String val) {
														obj.displacementX = Double.parseDouble(val);
													}
												}, contn).second;
									}
									else if (in.getLocalName().equals("DisplacementY")) {
										contn = updateOrContinue(in, "DisplacementY", baseOrEvaluated,
												new Updater<TextStyling>() {
													@Override
													public void update(TextStyling obj, String val) {
														obj.displacementY = Double.parseDouble(val);
													}
												}, contn).second;
									}
									else if (in.isStartElement()) {
										Location loc = in.getLocation();
										LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
												new Object[] { in.getLocalName(), loc.getLineNumber(),
														loc.getColumnNumber() });
										skipElement(in);
									}
								}
							}
							else if (in.getLocalName().equals("Rotation")) {
								contn = updateOrContinue(in, "Rotation", baseOrEvaluated, new Updater<TextStyling>() {
									@Override
									public void update(TextStyling obj, String val) {
										obj.rotation = Double.parseDouble(val);
									}
								}, contn).second;
							}
							else if (in.isStartElement()) {
								Location loc = in.getLocation();
								LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
										new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() });
								skipElement(in);
							}

						}
					}

					if (in.getLocalName().equals("LinePlacement")) {
						final Pair<LinePlacement, Continuation<LinePlacement>> pair = parseLinePlacement(in);
						if (pair != null) {
							baseOrEvaluated.linePlacement = pair.first;

							if (pair.second != null) {
								contn = new Continuation<TextStyling>(contn) {
									@Override
									public void updateStep(TextStyling base, Feature f,
											XPathEvaluator<Feature> evaluator) {
										pair.second.evaluate(base.linePlacement, f, evaluator);
									}
								};
							}
						}
					}
				}
			}
			else if (in.getLocalName().equals("Halo")) {
				final Pair<Halo, Continuation<Halo>> haloPair = parseHalo(in);
				if (haloPair != null) {
					baseOrEvaluated.halo = haloPair.first;

					if (haloPair.second != null) {
						contn = new Continuation<TextStyling>(contn) {
							@Override
							public void updateStep(TextStyling base, Feature f, XPathEvaluator<Feature> evaluator) {
								haloPair.second.evaluate(base.halo, f, evaluator);
							}
						};
					}
				}
			}
			else if (in.getLocalName().equals("Font")) {
				final Pair<Font, Continuation<Font>> fontPair = parseFont(in);
				if (fontPair != null) {
					baseOrEvaluated.font = fontPair.first;

					if (fontPair.second != null) {
						contn = new Continuation<TextStyling>(contn) {
							@Override
							public void updateStep(TextStyling base, Feature f, XPathEvaluator<Feature> evaluator) {
								fontPair.second.evaluate(base.font, f, evaluator);
							}
						};
					}
				}
			}
			else if (in.getLocalName().equals("Fill")) {
				final Pair<Fill, Continuation<Fill>> fillPair = context.fillParser.parseFill(in);
				if (fillPair != null) {
					baseOrEvaluated.fill = fillPair.first;

					if (fillPair.second != null) {
						contn = new Continuation<TextStyling>(contn) {
							@Override
							public void updateStep(TextStyling base, Feature f, XPathEvaluator<Feature> evaluator) {
								fillPair.second.evaluate(base.fill, f, evaluator);
							}
						};
					}
				}
			}
			else if (in.isStartElement()) {
				Location loc = in.getLocation();
				LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
						new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() });
				skipElement(in);
			}
		}

		if (contn == null) {
			Symbolizer<TextStyling> sym = new Symbolizer<TextStyling>(baseOrEvaluated, common.geometry, common.name,
					common.loc, common.line, common.col);
			return new Triple<Symbolizer<TextStyling>, Continuation<StringBuffer>, String>(sym, label, xmlText);
		}

		Symbolizer<TextStyling> sym = new Symbolizer<TextStyling>(baseOrEvaluated, contn, common.geometry, common.name,
				common.loc, common.line, common.col);
		return new Triple<Symbolizer<TextStyling>, Continuation<StringBuffer>, String>(sym, label, xmlText);
	}

	private Pair<Font, Continuation<Font>> parseFont(XMLStreamReader in) throws XMLStreamException {
		in.require(START_ELEMENT, null, "Font");

		Font baseOrEvaluated = new Font();
		Continuation<Font> contn = null;

		while (!(in.isEndElement() && in.getLocalName().equals("Font"))) {
			in.nextTag();

			if (in.getLocalName().endsWith("Parameter")) {
				String name = in.getAttributeValue(null, "name");
				if (name.equals("font-family")) {
					contn = updateOrContinue(in, "Parameter", baseOrEvaluated, new Updater<Font>() {
						@Override
						public void update(Font obj, String val) {
							obj.fontFamily.add(val);
						}
					}, contn).second;
				}
				else if (name.equals("font-style")) {
					contn = updateOrContinue(in, "Parameter", baseOrEvaluated, new Updater<Font>() {
						@Override
						public void update(Font obj, String val) {
							obj.fontStyle = Style.valueOf(val.toUpperCase());
						}
					}, contn).second;
				}
				else if (name.equals("font-weight")) {
					contn = updateOrContinue(in, "Parameter", baseOrEvaluated, new Updater<Font>() {
						@Override
						public void update(Font obj, String val) {
							obj.bold = val.equalsIgnoreCase("bold");
						}
					}, contn).second;
				}
				else if (name.equals("font-size")) {
					contn = updateOrContinue(in, "Parameter", baseOrEvaluated, new Updater<Font>() {
						@Override
						public void update(Font obj, String val) {
							obj.fontSize = Double.parseDouble(val);
						}
					}, contn).second;
				}
				else if (name.equals("font-color")) {
					skipElement(in);
					LOG.warn(
							"The non-standard font-color Svg/CssParameter is not supported any more. Use a standard Fill element instead.");
				}
				else {
					in.getElementText();
					LOG.warn("The non-standard '{}' Svg/CssParameter is not supported.", name);
				}
			}
		}

		return new Pair<Font, Continuation<Font>>(baseOrEvaluated, contn);

	}

	private Pair<Halo, Continuation<Halo>> parseHalo(XMLStreamReader in) throws XMLStreamException {
		in.require(START_ELEMENT, null, "Halo");

		Halo baseOrEvaluated = new Halo();
		Continuation<Halo> contn = null;

		while (!(in.isEndElement() && in.getLocalName().equals("Halo"))) {
			in.nextTag();

			if (in.getLocalName().equals("Radius")) {
				contn = updateOrContinue(in, "Radius", baseOrEvaluated, new Updater<Halo>() {
					@Override
					public void update(Halo obj, String val) {
						obj.radius = Double.parseDouble(val);

					}
				}, contn).second;
			}

			if (in.getLocalName().equals("Fill")) {
				final Pair<Fill, Continuation<Fill>> fillPair = context.fillParser.parseFill(in);

				if (fillPair != null) {
					baseOrEvaluated.fill = fillPair.first;

					if (fillPair.second != null) {
						contn = new Continuation<Halo>(contn) {
							@Override
							public void updateStep(Halo base, Feature f, XPathEvaluator<Feature> evaluator) {
								fillPair.second.evaluate(base.fill, f, evaluator);
							}
						};
					}
				}
			}
		}

		return new Pair<Halo, Continuation<Halo>>(baseOrEvaluated, contn);
	}

	private static PerpendicularOffsetType getPerpendicularOffsetType(XMLStreamReader in) {
		PerpendicularOffsetType tp = new PerpendicularOffsetType();
		String type = in.getAttributeValue(null, "type");
		if (type != null) {
			try {
				tp.type = Type.valueOf(type);
			}
			catch (IllegalArgumentException e) {
				LOG.debug("Stack trace:", e);
				LOG.warn("The value '{}' is not a valid type for perpendicular offsets. Valid types are: {}", type,
						Arrays.toString(Type.values()));
			}
		}
		String substraction = in.getAttributeValue(null, "substraction");
		if (substraction != null) {
			try {
				tp.substraction = Substraction.valueOf(substraction);
			}
			catch (IllegalArgumentException e) {
				LOG.debug("Stack trace:", e);
				LOG.warn("The value '{}' is not a valid substraction type for perpendicular offsets."
						+ " Valid types are: {}", substraction, Arrays.toString(Substraction.values()));
			}
		}
		return tp;
	}

	private Pair<LinePlacement, Continuation<LinePlacement>> parseLinePlacement(XMLStreamReader in)
			throws XMLStreamException {
		in.require(START_ELEMENT, null, "LinePlacement");

		LinePlacement baseOrEvaluated = new LinePlacement();
		Continuation<LinePlacement> contn = null;

		while (!(in.isEndElement() && in.getLocalName().equals("LinePlacement"))) {
			in.nextTag();

			if (in.getLocalName().equals("PerpendicularOffset")) {
				baseOrEvaluated.perpendicularOffsetType = getPerpendicularOffsetType(in);
				contn = updateOrContinue(in, "PerpendicularOffset", baseOrEvaluated, new Updater<LinePlacement>() {
					@Override
					public void update(LinePlacement obj, String val) {
						obj.perpendicularOffset = Double.parseDouble(val);

					}
				}, contn).second;
			}

			if (in.getLocalName().equals("InitialGap")) {
				contn = updateOrContinue(in, "InitialGap", baseOrEvaluated, new Updater<LinePlacement>() {
					@Override
					public void update(LinePlacement obj, String val) {
						obj.initialGap = Double.parseDouble(val);

					}
				}, contn).second;
			}

			if (in.getLocalName().equals("Gap")) {
				contn = updateOrContinue(in, "Gap", baseOrEvaluated, new Updater<LinePlacement>() {
					@Override
					public void update(LinePlacement obj, String val) {
						obj.gap = Double.parseDouble(val);

					}
				}, contn).second;
			}

			if (in.getLocalName().equals("GeneralizeLine")) {
				contn = updateOrContinue(in, "GeneralizeLine", baseOrEvaluated, new Updater<LinePlacement>() {
					@Override
					public void update(LinePlacement obj, String val) {
						obj.generalizeLine = Boolean.parseBoolean(val);

					}
				}, contn).second;
			}

			if (in.getLocalName().equals("IsAligned")) {
				contn = updateOrContinue(in, "IsAligned", baseOrEvaluated, new Updater<LinePlacement>() {
					@Override
					public void update(LinePlacement obj, String val) {
						obj.isAligned = Boolean.parseBoolean(val);

					}
				}, contn).second;
			}

			if (in.getLocalName().equals("IsRepeated")) {
				contn = updateOrContinue(in, "IsRepeated", baseOrEvaluated, new Updater<LinePlacement>() {
					@Override
					public void update(LinePlacement obj, String val) {
						obj.repeat = Boolean.parseBoolean(val);

					}
				}, contn).second;
			}
			if (in.getLocalName().equals("PreventUpsideDown")) {
				contn = updateOrContinue(in, "PreventUpsideDown", baseOrEvaluated, new Updater<LinePlacement>() {
					@Override
					public void update(LinePlacement obj, String val) {
						obj.preventUpsideDown = Boolean.parseBoolean(val);

					}
				}, contn).second;
			}
			if (in.getLocalName().equals("Center")) {
				contn = updateOrContinue(in, "Center", baseOrEvaluated, new Updater<LinePlacement>() {
					@Override
					public void update(LinePlacement obj, String val) {
						obj.center = Boolean.parseBoolean(val);

					}
				}, contn).second;
			}
			if (in.getLocalName().equals("WordWise")) {
				contn = updateOrContinue(in, "WordWise", baseOrEvaluated, new Updater<LinePlacement>() {
					@Override
					public void update(LinePlacement obj, String val) {
						obj.wordWise = Boolean.parseBoolean(val);

					}
				}, contn).second;
			}
		}

		return new Pair<LinePlacement, Continuation<LinePlacement>>(baseOrEvaluated, contn);
	}

	/**
	 * @param in
	 * @return null, if no symbolizer and no Feature type style was found
	 * @throws XMLStreamException
	 */
	public org.deegree.style.se.unevaluated.Style parse(XMLStreamReader in) throws XMLStreamException {
		if (in.getEventType() == START_DOCUMENT) {
			in.nextTag();
		}
		if (in.getLocalName().endsWith("Symbolizer")) {
			Triple<Symbolizer<?>, Continuation<StringBuffer>, String> pair = parseSymbolizer(in);
			return new org.deegree.style.se.unevaluated.Style(pair.first, pair.second, pair.first.getName(),
					pair.third);
		}
		if (in.getLocalName().equals("FeatureTypeStyle")) {
			return parseFeatureTypeOrCoverageStyle(in);
		}
		LOG.warn("Symbology file '{}' did not contain symbolizer or feature type style.",
				in.getLocation().getSystemId());
		return null;
	}

	private org.deegree.style.se.unevaluated.Style tryOnlineResource(XMLStreamReader in) throws XMLStreamException {
		if (in.getLocalName().equals("OnlineResource")) {
			try {
				URL url = parseOnlineResource(in);
				XMLStreamReader newReader = XMLInputFactory.newInstance()
					.createXMLStreamReader(url.toString(), url.openStream());
				while (!newReader.isStartElement())
					newReader.next();
				return parseFeatureTypeOrCoverageStyle(newReader);
			}
			catch (MalformedURLException e) {
				LOG.warn("An URL referencing a FeatureType or CoverageStyle could not be resolved.");
				LOG.debug("Stack trace:", e);
			}
			catch (FactoryConfigurationError e) {
				LOG.warn("An URL referencing a FeatureType or CoverageStyle could not be read.");
				LOG.debug("Stack trace:", e);
			}
			catch (IOException e) {
				LOG.warn("An URL referencing a FeatureType or CoverageStyle could not be read.");
				LOG.debug("Stack trace:", e);
			}
		}
		return null;
	}

	/**
	 * @param in
	 * @return a new style
	 * @throws XMLStreamException
	 */
	public org.deegree.style.se.unevaluated.Style parseFeatureTypeOrCoverageStyle(XMLStreamReader in)
			throws XMLStreamException {
		org.deegree.style.se.unevaluated.Style res = tryOnlineResource(in);
		if (res != null) {
			return res;
		}

		LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>> result = new LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>>();
		HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>> labels = new HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>>();
		HashMap<Symbolizer<TextStyling>, String> labelXMLTexts = collectXMLSnippets
				? new HashMap<Symbolizer<TextStyling>, String>() : null;
		Common common = new Common(in.getLocation());
		QName featureTypeName = null;

		while (!(in.isEndElement()
				&& (in.getLocalName().equals("FeatureTypeStyle") || in.getLocalName().equals("CoverageStyle")))) {
			in.nextTag();

			parseCommon(common, in);

			// TODO unused
			if (in.getLocalName().equals("SemanticTypeIdentifier")) {
				in.getElementText(); // AndThrowItAwayImmediately
			}

			if (in.getLocalName().equals("FeatureTypeName")) {
				featureTypeName = getElementTextAsRelaxedQName(in);
			}

			// TODO unused
			if (in.getLocalName().equals("CoverageName")) {
				in.getElementText(); // AndThrowItAwayImmediately
			}

			maybeParseRule(in, labels, labelXMLTexts, result);
		}

		return new org.deegree.style.se.unevaluated.Style(result, labels, labelXMLTexts, common.name, featureTypeName);
	}

	private void maybeParseRule(XMLStreamReader in, HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>> labels,
			HashMap<Symbolizer<TextStyling>, String> labelXMLTexts,
			LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>> result) throws XMLStreamException {
		if (in.getLocalName().equals("Rule") || in.getLocalName().equals("OnlineResource")) {
			XMLStreamReader localReader = prepareReaderOnlineResource(in);
			Common ruleCommon = new Common(in.getLocation());
			double minScale = NEGATIVE_INFINITY;
			double maxScale = POSITIVE_INFINITY;

			Filter filter = null;
			LinkedList<Symbolizer<?>> syms = new LinkedList<Symbolizer<?>>();

			while (!(localReader.isEndElement() && localReader.getLocalName().equals("Rule"))) {
				localReader.nextTag();

				parseCommon(ruleCommon, localReader);

				if (localReader.getLocalName().equals("Filter")) {
					filter = Filter110XMLDecoder.parse(localReader);
				}

				if (localReader.getLocalName().equals("ElseFilter")) {
					filter = ELSEFILTER;
					localReader.nextTag();
				}

				if (localReader.getLocalName().equals("MinScaleDenominator")) {
					minScale = parseDouble(localReader.getElementText());
				}
				if (localReader.getLocalName().equals("MaxScaleDenominator")) {
					maxScale = parseDouble(localReader.getElementText());
				}

				parseRuleSymbolizer(localReader, labels, labelXMLTexts, syms);
			}

			FilterContinuation contn = new FilterContinuation(filter, syms, ruleCommon);
			DoublePair scales = new DoublePair(minScale, maxScale);
			result.add(new Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>(contn, scales));
		}
	}

	private XMLStreamReader prepareReaderOnlineResource(XMLStreamReader in) throws XMLStreamException {
		XMLStreamReader localReader = in;
		if (in.getLocalName().equals("OnlineResource")) {
			try {
				URL url = parseOnlineResource(in);
				localReader = XMLInputFactory.newInstance().createXMLStreamReader(url.toString(), url.openStream());
			}
			catch (IOException e) {
				LOG.warn("Error '{}' while resolving/accessing remote Rule document.", e.getLocalizedMessage());
				LOG.debug("Stack trace:", e);
			}
		}
		return localReader;
	}

	private void parseRuleSymbolizer(XMLStreamReader localReader,
			HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>> labels,
			HashMap<Symbolizer<TextStyling>, String> labelXMLTexts, LinkedList<Symbolizer<?>> syms)
			throws XMLStreamException {
		// TODO legendgraphic
		if (localReader.isStartElement() && localReader.getLocalName().endsWith("Symbolizer")) {

			Triple<Symbolizer<?>, Continuation<StringBuffer>, String> parsedSym = parseSymbolizer(localReader);
			if (parsedSym.second != null) {
				labels.put((Symbolizer) parsedSym.first, parsedSym.second);
			}
			if (collectXMLSnippets && parsedSym.third != null) {
				labelXMLTexts.put((Symbolizer) parsedSym.first, parsedSym.third);
			}
			syms.add(parsedSym.first);
		}
	}

	static class ElseFilter implements Filter {

		@Override
		public <T> boolean evaluate(T object, XPathEvaluator<T> evaluator) throws FilterEvaluationException {
			return false; // always to false, has to be checked differently, see
							// FilterContinuation below
		}

		@Override
		public Type getType() {
			return null;
		}

	}

	/**
	 * <code>FilterContinuation</code>
	 *
	 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
	 */
	public static class FilterContinuation extends Continuation<LinkedList<Symbolizer<?>>> {

		/***/
		public Filter filter;

		private LinkedList<Symbolizer<?>> syms;

		/** Contains description and so on. */
		public Common common;

		public FilterContinuation(Filter filter, LinkedList<Symbolizer<?>> syms, Common common) {
			this.filter = filter;
			this.syms = syms;
			this.common = common;
		}

		@Override
		public void updateStep(LinkedList<Symbolizer<?>> base, Feature f, XPathEvaluator<Feature> evaluator) {
			try {
				if (filter == null || f == null || filter.evaluate(f, evaluator)
						|| (base.isEmpty() && filter == ELSEFILTER)) {
					base.addAll(syms);
				}
			}
			catch (FilterEvaluationException e) {
				LOG.warn("Evaluating the following expression resulted in an error '{}':\n'{}'",
						e.getLocalizedMessage(), filter.toString());
				LOG.debug("Stack trace:", e);
			}
		}

		/**
		 * @return the symbolizers
		 */
		public List<Symbolizer<?>> getSymbolizers() {
			return syms;
		}

	}

}
