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

import static java.awt.Font.TRUETYPE_FONT;
import static java.awt.Font.TYPE1_FONT;
import static java.awt.Font.createFont;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.resolve;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;
import static org.deegree.style.se.unevaluated.Continuation.SBUPDATER;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.function.Function;
import javax.imageio.ImageIO;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.codec.binary.Base64;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.feature.Feature;
import org.deegree.filter.XPathEvaluator;
import org.deegree.style.se.unevaluated.Continuation;
import org.deegree.style.se.unevaluated.Continuation.Updater;
import org.deegree.style.styling.components.Fill;
import org.deegree.style.styling.components.Graphic;
import org.deegree.style.styling.components.Mark;
import org.deegree.style.styling.components.Mark.SimpleMark;
import org.deegree.style.styling.components.Stroke;
import org.deegree.style.styling.mark.WellKnownNameManager;
import org.deegree.style.utils.ShapeHelper;
import org.slf4j.Logger;

/**
 * Responsible for parsing Graphic elements.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class GraphicSymbologyParser {

	static final Logger LOG = getLogger(GraphicSymbologyParser.class);

	private SymbologyParserContext context;

	GraphicSymbologyParser(SymbologyParserContext context) {
		this.context = context;
	}

	Pair<Graphic, Continuation<Graphic>> parseGraphic(XMLStreamReader in) throws XMLStreamException {
		in.require(START_ELEMENT, null, "Graphic");

		Graphic base = new Graphic();
		Continuation<Graphic> contn = null;

		while (!(in.isEndElement() && in.getLocalName().equals("Graphic"))) {
			in.nextTag();

			if (in.getLocalName().equals("Mark")) {
				final Pair<Mark, Continuation<Mark>> pair = parseMark(in);

				if (pair != null) {
					base.mark = pair.first;
					if (pair.second != null) {
						contn = new Continuation<Graphic>(contn) {
							@Override
							public void updateStep(Graphic base, Feature f, XPathEvaluator<Feature> evaluator) {
								pair.second.evaluate(base.mark, f, evaluator);
							}
						};
					}
				}
			}
			else if (in.getLocalName().equals("ExternalGraphic")) {
				try {
					final Triple<BufferedImage, String, Continuation<List<Pair<BufferedImage, String>>>> p = parseExternalGraphic(
							in);
					if (p.third != null) {
						contn = new Continuation<Graphic>(contn) {
							@Override
							public void updateStep(Graphic base, Feature f, XPathEvaluator<Feature> evaluator) {
								LinkedList<Pair<BufferedImage, String>> list = new LinkedList<Pair<BufferedImage, String>>();
								p.third.evaluate(list, f, evaluator);
								Pair<BufferedImage, String> image = list.poll();
								base.image = image.first;
								base.imageURL = image.second;
							}
						};
					}
					else {
						base.image = p.first;
						base.imageURL = p.second;
					}
				}
				catch (IOException e) {
					LOG.debug("Stack trace", e);
					LOG.warn("External graphic could not be loaded. Location: line '{}' column '{}' of file '{}'.",
							new Object[] { in.getLocation().getLineNumber(), in.getLocation().getColumnNumber(),
									in.getLocation().getSystemId() });
				}
			}
			else if (in.getLocalName().equals("Opacity")) {
				contn = context.parser.updateOrContinue(in, "Opacity", base, new Updater<Graphic>() {
					public void update(Graphic obj, String val) {
						obj.opacity = Double.parseDouble(val);
					}
				}, contn).second;
			}
			else if (in.getLocalName().equals("Size")) {
				contn = context.parser.updateOrContinue(in, "Size", base, new Updater<Graphic>() {
					public void update(Graphic obj, String val) {
						obj.size = Double.parseDouble(val);
					}
				}, contn).second;
			}
			else if (in.getLocalName().equals("Rotation")) {
				contn = context.parser.updateOrContinue(in, "Rotation", base, new Updater<Graphic>() {
					public void update(Graphic obj, String val) {
						obj.rotation = Double.parseDouble(val);
					}
				}, contn).second;
			}
			else if (in.getLocalName().equals("AnchorPoint")) {
				while (!(in.isEndElement() && in.getLocalName().equals("AnchorPoint"))) {
					in.nextTag();

					if (in.getLocalName().equals("AnchorPointX")) {
						contn = context.parser.updateOrContinue(in, "AnchorPointX", base, new Updater<Graphic>() {
							public void update(Graphic obj, String val) {
								obj.anchorPointX = Double.parseDouble(val);
							}
						}, contn).second;
					}
					else if (in.getLocalName().equals("AnchorPointY")) {
						contn = context.parser.updateOrContinue(in, "AnchorPointY", base, new Updater<Graphic>() {
							public void update(Graphic obj, String val) {
								obj.anchorPointY = Double.parseDouble(val);
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
			else if (in.getLocalName().equals("Displacement")) {
				while (!(in.isEndElement() && in.getLocalName().equals("Displacement"))) {
					in.nextTag();

					if (in.getLocalName().equals("DisplacementX")) {
						contn = context.parser.updateOrContinue(in, "DisplacementX", base, new Updater<Graphic>() {
							public void update(Graphic obj, String val) {
								obj.displacementX = Double.parseDouble(val);
							}
						}, contn).second;
					}
					else if (in.getLocalName().equals("DisplacementY")) {
						contn = context.parser.updateOrContinue(in, "DisplacementY", base, new Updater<Graphic>() {
							public void update(Graphic obj, String val) {
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
		in.require(END_ELEMENT, null, "Graphic");

		return new Pair<Graphic, Continuation<Graphic>>(base, contn);
	}

	private Pair<Mark, Continuation<Mark>> parseMark(XMLStreamReader in) throws XMLStreamException {
		in.require(START_ELEMENT, null, "Mark");

		Mark base = new Mark();
		Continuation<Mark> contn = null;

		in.nextTag();

		while (!(in.isEndElement() && in.getLocalName().equals("Mark"))) {
			if (in.isEndElement()) {
				in.nextTag();
			}

			if (in.getLocalName().equals("WellKnownName")) {
				String wkn = in.getElementText();
				Function<String, URL> resolver = (str) -> {
					if (context.location != null) {
						return context.location.resolveToUrl(str);
					}
					else {
						try {
							return resolve(str, in);
						}
						catch (MalformedURLException e) {
							LOG.warn("Failed to resolve external WellKnownName resource {}: {} ", str, e.getMessage());
							LOG.trace("Exception", e);
							return null;
						}
					}
				};

				if (!WellKnownNameManager.load(base, wkn, resolver)) {
					LOG.warn("Specified unsupported WellKnownName of '{}', using square instead.", wkn);
					base.wellKnown = SimpleMark.SQUARE;
				}
			}
			else
				sym: if (in.getLocalName().equals("OnlineResource") || in.getLocalName().equals("InlineContent")) {
					LOG.debug("Loading mark from external file.");
					Triple<InputStream, String, Continuation<StringBuffer>> pair = getOnlineResourceOrInlineContent(in);
					if (pair == null) {
						in.nextTag();
						break sym;
					}
					InputStream is = pair.first;
					in.nextTag();

					in.require(START_ELEMENT, null, "Format");
					String format = in.getElementText();
					in.require(END_ELEMENT, null, "Format");

					in.nextTag();
					if (in.getLocalName().equals("MarkIndex")) {
						base.markIndex = Integer.parseInt(in.getElementText());
					}

					if (is != null) {
						try {
							java.awt.Font font = null;
							if (format.equalsIgnoreCase("ttf")) {
								font = createFont(TRUETYPE_FONT, is);
							}
							if (format.equalsIgnoreCase("type1")) {
								font = createFont(TYPE1_FONT, is);
							}

							if (format.equalsIgnoreCase("svg")) {
								base.shape = ShapeHelper.getShapeFromSvg(is, pair.second);
							}

							if (font == null && base.shape == null) {
								LOG.warn("Mark was not loaded, because the format '{}' is not supported.", format);
								break sym;
							}

							if (font != null && base.markIndex >= font.getNumGlyphs()) {
								LOG.warn("The font only contains {} glyphs, but the index given was {}.",
										font.getNumGlyphs(), base.markIndex);
								break sym;
							}

							base.font = font;
						}
						catch (FontFormatException e) {
							LOG.debug("Stack trace:", e);
							LOG.warn("The file was not a valid '{}' file: '{}'", format, e.getLocalizedMessage());
						}
						catch (IOException e) {
							LOG.debug("Stack trace:", e);
							LOG.warn("The file could not be read: '{}'.", e.getLocalizedMessage());
						}
						finally {
							closeQuietly(is);
						}
					}
				}
				else if (in.getLocalName().equals("Fill")) {
					final Pair<Fill, Continuation<Fill>> fill = context.fillParser.parseFill(in);
					base.fill = fill.first;
					if (fill.second != null) {
						contn = new Continuation<Mark>(contn) {
							@Override
							public void updateStep(Mark base, Feature f, XPathEvaluator<Feature> evaluator) {
								fill.second.evaluate(base.fill, f, evaluator);
							}
						};
					}
				}
				else if (in.getLocalName().equals("Stroke")) {
					final Pair<Stroke, Continuation<Stroke>> stroke = context.strokeParser.parseStroke(in);
					base.stroke = stroke.first;
					if (stroke.second != null) {
						contn = new Continuation<Mark>(contn) {
							@Override
							public void updateStep(Mark base, Feature f, XPathEvaluator<Feature> evaluator) {
								stroke.second.evaluate(base.stroke, f, evaluator);
							}
						};
					}
				}
				else if (in.isStartElement()) {
					Location loc = in.getLocation();
					LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
							new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() });
					skipElement(in);
				}
		}

		in.require(END_ELEMENT, null, "Mark");

		return new Pair<Mark, Continuation<Mark>>(base, contn);
	}

	private Triple<BufferedImage, String, Continuation<List<Pair<BufferedImage, String>>>> parseExternalGraphic(
			final XMLStreamReader in) throws IOException, XMLStreamException {
		// TODO color replacement

		in.require(START_ELEMENT, null, "ExternalGraphic");

		String format = null;
		BufferedImage img = null;
		String url = null;
		Triple<InputStream, String, Continuation<StringBuffer>> pair = null;
		Continuation<List<Pair<BufferedImage, String>>> contn = null; // needs to be list
																		// to be
																		// updateable by
																		// reference...

		while (!(in.isEndElement() && in.getLocalName().equals("ExternalGraphic"))) {
			in.nextTag();

			if (in.getLocalName().equals("Format")) {
				format = in.getElementText();
			}
			else if (in.getLocalName().equals("OnlineResource") || in.getLocalName().equals("InlineContent")) {
				pair = getOnlineResourceOrInlineContent(in);
			}
			else if (in.isStartElement()) {
				Location loc = in.getLocation();
				LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
						new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() });
				skipElement(in);
			}
		}

		try {
			if (pair != null) {
				if (pair.first != null && format != null && (format.toLowerCase().indexOf("svg") == -1)) {
					img = ImageIO.read(pair.first);
				}
				url = pair.second;

				final Continuation<StringBuffer> sbcontn = pair.third;

				if (pair.third != null) {
					final LinkedHashMap<String, BufferedImage> cache = new LinkedHashMap<String, BufferedImage>(256) {
						private static final long serialVersionUID = -6847956873232942891L;

						@Override
						protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
							return size() > 256; // yeah, hardcoded max size... TODO
						}
					};
					contn = new Continuation<List<Pair<BufferedImage, String>>>() {
						@Override
						public void updateStep(List<Pair<BufferedImage, String>> base, Feature f,
								XPathEvaluator<Feature> evaluator) {
							StringBuffer sb = new StringBuffer();
							sbcontn.evaluate(sb, f, evaluator);
							String file = sb.toString();
							if (cache.containsKey(file)) {
								base.add(new Pair<BufferedImage, String>(cache.get(file), null));
								return;
							}
							try {
								URL resolvedImageUrl = resolveImageUrl(file);
								BufferedImage image = null;
								String imageUrl = null;
								if (resolvedImageUrl != null) {
									image = ImageIO.read(resolvedImageUrl);
									if (image != null) {
										cache.put(file, image);
									}
									else {
										imageUrl = resolvedImageUrl.toExternalForm();
									}
								}
								else {
									imageUrl = file;
								}
								base.add(new Pair<BufferedImage, String>(image, imageUrl));
							}
							catch (MalformedURLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

						private URL resolveImageUrl(String file) throws MalformedURLException {
							if (context.location != null)
								return context.location.resolveToUrl(file);
							return resolve(file, in);
						}

						private BufferedImage asImage(URL resolvedImageUrl) throws IOException {
							return ImageIO.read(resolvedImageUrl);
						}

					};
				}
			}
		}
		finally {
			if (pair != null) {
				try {
					pair.first.close();
				}
				catch (Exception e) {
					LOG.trace("Stack trace when closing input stream:", e);
				}
			}
		}

		return new Triple<BufferedImage, String, Continuation<List<Pair<BufferedImage, String>>>>(img, url, contn);
	}

	private Triple<InputStream, String, Continuation<StringBuffer>> getOnlineResourceOrInlineContent(XMLStreamReader in)
			throws XMLStreamException {
		if (in.getLocalName().equals("OnlineResource")) {
			String str = in.getAttributeValue(XLNNS, "href");

			if (str == null) {
				Continuation<StringBuffer> contn = context.parser.updateOrContinue(in, "OnlineResource",
						new StringBuffer(), SBUPDATER, null).second;
				return new Triple<InputStream, String, Continuation<StringBuffer>>(null, null, contn);
			}

			String strUrl = null;
			try {
				URL url;
				if (context.location != null) {
					url = context.location.resolveToUrl(str);
				}
				else {
					url = resolve(str, in);
				}
				strUrl = url.toExternalForm();
				LOG.debug("Loading from URL '{}'", url);
				in.nextTag();
				return new Triple<InputStream, String, Continuation<StringBuffer>>(url.openStream(), strUrl, null);
			}
			catch (IOException e) {
				LOG.debug("Stack trace:", e);
				LOG.warn("Could not retrieve content at URL '{}'.", str);
				return null;
			}
		}
		else if (in.getLocalName().equals("InlineContent")) {
			String format = in.getAttributeValue(null, "encoding");
			if (format.equalsIgnoreCase("base64")) {
				ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(in.getElementText()));
				return new Triple<InputStream, String, Continuation<StringBuffer>>(bis, null, null);
			}
			// if ( format.equalsIgnoreCase( "xml" ) ) {
			// // TODO
			// }
		}
		else if (in.isStartElement()) {
			Location loc = in.getLocation();
			LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
					new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() });
			skipElement(in);
		}

		return null;
	}

}
