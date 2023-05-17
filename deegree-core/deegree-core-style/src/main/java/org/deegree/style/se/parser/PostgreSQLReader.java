/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.style.se.parser;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.util.Arrays.asList;
import static org.deegree.commons.utils.ArrayUtils.splitAsDoubles;
import static org.deegree.commons.utils.ColorUtils.decodeWithAlpha;
import static org.deegree.style.se.parser.SymbologyParser.getUOM;
import static org.deegree.style.styling.components.Mark.SimpleMark.SQUARE;
import static org.deegree.style.utils.ShapeHelper.getShapeFromSvg;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.codec.binary.Base64;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.utils.Triple;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.db.ConnectionProvider;
import org.deegree.feature.Feature;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.style.se.unevaluated.Continuation;
import org.deegree.style.se.unevaluated.Continuation.Updater;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.style.se.unevaluated.Symbolizer;
import org.deegree.style.styling.LineStyling;
import org.deegree.style.styling.PointStyling;
import org.deegree.style.styling.PolygonStyling;
import org.deegree.style.styling.Styling;
import org.deegree.style.styling.TextStyling;
import org.deegree.style.styling.components.Fill;
import org.deegree.style.styling.components.Font;
import org.deegree.style.styling.components.Graphic;
import org.deegree.style.styling.components.Halo;
import org.deegree.style.styling.components.LinePlacement;
import org.deegree.style.styling.components.Mark.SimpleMark;
import org.deegree.style.styling.components.Stroke;
import org.deegree.style.styling.components.Stroke.LineCap;
import org.deegree.style.styling.components.Stroke.LineJoin;
import org.slf4j.Logger;

/**
 * <code>PostgreSQLReader</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class PostgreSQLReader {

	enum Type {

		POINT, LINE, POLYGON, TEXT

	}

	static final Logger LOG = getLogger(PostgreSQLReader.class);

	private final HashMap<Integer, Style> pool = new HashMap<Integer, Style>();

	private final HashMap<Integer, Fill> fills = new HashMap<Integer, Fill>();

	private final HashMap<Integer, Stroke> strokes = new HashMap<Integer, Stroke>();

	private final HashMap<Integer, Graphic> graphics = new HashMap<Integer, Graphic>();

	private final HashMap<Integer, Font> fonts = new HashMap<Integer, Font>();

	private final HashMap<Integer, LinePlacement> lineplacements = new HashMap<Integer, LinePlacement>();

	private final HashMap<Integer, Halo> halos = new HashMap<Integer, Halo>();

	private final HashMap<Integer, PointStyling> points = new HashMap<Integer, PointStyling>();

	private final HashMap<Integer, LineStyling> lines = new HashMap<Integer, LineStyling>();

	private final HashMap<Integer, PolygonStyling> polygons = new HashMap<Integer, PolygonStyling>();

	private final HashMap<Integer, TextStyling> texts = new HashMap<Integer, TextStyling>();

	private final HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>> labels = new HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>>();

	private final HashMap<Styling<?>, Continuation<Styling<?>>> continuations = new HashMap<Styling<?>, Continuation<Styling<?>>>();

	private final String baseSystemId;

	private final String schema;

	private ConnectionProvider connProvider;

	/**
	 * @param connid
	 * @param baseSystemId to resolve relative references in sld files
	 */
	public PostgreSQLReader(ConnectionProvider connProvider, String schema, String baseSystemId) {
		this.connProvider = connProvider;
		this.schema = schema;
		this.baseSystemId = baseSystemId;
	}

	private Pair<Graphic, Continuation<Styling<?>>> getGraphic(int id, Connection conn, Continuation<Styling<?>> contn)
			throws SQLException {
		Graphic graphic = graphics.get(id);
		if (graphic != null) {
			return new Pair<Graphic, Continuation<Styling<?>>>(graphic, contn);
		}

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(
					"select size, sizeexpr, rotation, rotationexpr, anchorx, anchory, displacementx, displacementy, wellknownname, svg, base64raster, fill_id, stroke_id from "
							+ schema + ".graphics where id = ?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				Graphic res = new Graphic();

				Double size = (Double) rs.getObject("size");
				if (size != null) {
					res.size = size;
				}
				String sizeExpr = (String) rs.getObject("sizeexpr");
				if (sizeExpr != null) {
					contn = getContn(sizeExpr, contn, new Updater<Styling<?>>() {
						@Override
						public void update(Styling<?> obj, String val) {
							((PointStyling) obj).graphic.size = Double.parseDouble(val);
						}
					});
				}
				Double rotation = (Double) rs.getObject("rotation");
				if (rotation != null) {
					res.rotation = rotation;
				}
				String rotationExpr = (String) rs.getObject("rotationexpr");
				if (rotationExpr != null) {
					contn = getContn(rotationExpr, contn, new Updater<Styling<?>>() {
						@Override
						public void update(Styling<?> obj, String val) {
							((PointStyling) obj).graphic.rotation = Double.parseDouble(val);
						}
					});
				}
				Double ax = (Double) rs.getObject("anchorx");
				if (ax != null) {
					res.anchorPointX = ax;
				}
				Double ay = (Double) rs.getObject("anchory");
				if (ay != null) {
					res.anchorPointY = ay;
				}
				Double dx = (Double) rs.getObject("displacementx");
				if (dx != null) {
					res.displacementX = dx;
				}
				Double dy = (Double) rs.getObject("displacementy");
				if (dy != null) {
					res.displacementY = dy;
				}
				String wkn = rs.getString("wellknownname");
				if (wkn != null) {
					try {
						res.mark.wellKnown = SimpleMark.valueOf(wkn.toUpperCase());
					}
					catch (IllegalArgumentException e) {
						LOG.debug("Found unknown 'well known name' '{}' for the symbol with "
								+ "id '{}' in the database, using square instead.", wkn, id);
						res.mark.wellKnown = SQUARE;
					}
				}
				String svg = rs.getString("svg");
				if (svg != null) {
					try {
						res.mark.shape = getShapeFromSvg(new ByteArrayInputStream(svg.getBytes("UTF-8")), null);
					}
					catch (UnsupportedEncodingException e) {
						LOG.trace("Stack trace:", e);
					}
				}
				String base64raster = rs.getString("base64raster");
				if (base64raster != null) {
					ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(base64raster));
					try {
						res.image = ImageIO.read(bis);
					}
					catch (IOException e) {
						LOG.debug("A base64 encoded image could not be read from the database,"
								+ " for the symbol with id '{}', error was '{}'.", id, e.getLocalizedMessage());
						LOG.trace("Stack trace:", e);
					}
				}
				Integer fill = (Integer) rs.getObject("fill_id");
				if (fill != null) {
					res.mark.fill = getFill(fill, conn);
				}
				Integer stroke = (Integer) rs.getObject("stroke_id");
				if (stroke != null) {
					Pair<Stroke, Continuation<Styling<?>>> p = getStroke(stroke, conn, contn);
					res.mark.stroke = p.first;
					contn = p.second;
				}

				graphics.put(id, res);

				return new Pair<Graphic, Continuation<Styling<?>>>(res, contn);
			}
			return null;
		}
		finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	private Pair<Stroke, Continuation<Styling<?>>> getStroke(int id, Connection conn, Continuation<Styling<?>> contn)
			throws SQLException {
		Stroke stroke = strokes.get(id);
		if (stroke != null) {
			return new Pair<Stroke, Continuation<Styling<?>>>(stroke, contn);
		}

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(
					"select color, width, widthexpr, linejoin, linecap, dasharray, dashoffset, stroke_graphic_id, fill_graphic_id, strokegap, strokeinitialgap, positionpercentage from "
							+ schema + ".strokes where id = ?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				Stroke res = new Stroke();

				String color = rs.getString("color");
				if (color != null) {
					res.color = decodeWithAlpha(color);
				}
				Double width = (Double) rs.getObject("width");
				if (width != null) {
					res.width = width;
				}
				final String widthExpr = (String) rs.getObject("widthexpr");
				if (widthExpr != null) {
					res.width = -1;
					contn = getContn(widthExpr, contn, new Updater<Styling<?>>() {
						@Override
						public void update(Styling<?> obj, String val) {
							if (obj instanceof LineStyling) {
								((LineStyling) obj).stroke.width = Double.parseDouble(val);
							}
							if (obj instanceof PolygonStyling) {
								((PolygonStyling) obj).stroke.width = Double.parseDouble(val);
							}
						}
					});
				}
				String linejoin = rs.getString("linejoin");
				if (linejoin != null) {
					try {
						res.linejoin = LineJoin.valueOf(linejoin.toUpperCase());
					}
					catch (IllegalArgumentException e) {
						LOG.debug("The linejoin value '{}' for stroke with id '{}' could not be parsed.", linejoin, id);
					}
				}
				String linecap = rs.getString("linecap");
				if (linecap != null) {
					try {
						res.linecap = LineCap.valueOf(linecap.toUpperCase());
					}
					catch (IllegalArgumentException e) {
						LOG.debug("The linecap value '{}' for stroke with id '{}' could not be parsed.", linecap, id);
					}
				}
				String dasharray = rs.getString("dasharray");
				if (dasharray != null) {
					res.dasharray = splitAsDoubles(dasharray, " ");
				}
				Double dashoffset = (Double) rs.getObject("dashoffset");
				if (dashoffset != null) {
					res.dashoffset = dashoffset;
				}
				Integer graphicstroke = (Integer) rs.getObject("stroke_graphic_id");
				if (graphicstroke != null) {
					res.stroke = getGraphic(graphicstroke, conn, null).first;
				}
				Integer graphicfill = (Integer) rs.getObject("fill_graphic_id");
				if (graphicfill != null) {
					res.fill = getGraphic(graphicfill, conn, null).first;
				}
				Double strokegap = (Double) rs.getObject("strokegap");
				if (strokegap != null) {
					res.strokeGap = strokegap;
				}
				Double strokeinitialgap = (Double) rs.getObject("strokeinitialgap");
				if (strokeinitialgap != null) {
					res.strokeInitialGap = strokeinitialgap;
				}
				Double positionPercentage = (Double) rs.getObject("positionpercentage");
				if (positionPercentage != null) {
					res.positionPercentage = positionPercentage;
				}

				strokes.put(id, res);

				return new Pair<Stroke, Continuation<Styling<?>>>(res, contn);
			}
			return null;
		}
		finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	private Fill getFill(int id, Connection conn) throws SQLException {
		Fill fill = fills.get(id);
		if (fill != null) {
			return fill;
		}

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement("select color, graphic_id from " + schema + ".fills where id = ?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				Fill res = new Fill();

				String color = rs.getString("color");
				if (color != null) {
					res.color = decodeWithAlpha(color);
				}
				Integer graphic = (Integer) rs.getObject("graphic_id");
				if (graphic != null) {
					res.graphic = getGraphic(graphic, conn, null).first;
				}

				fills.put(id, res);

				return res;
			}
			return null;
		}
		finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	private Font getFont(int id, Connection conn) throws SQLException {
		Font font = fonts.get(id);
		if (font != null) {
			return font;
		}

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement("select family, style, bold, size from " + schema + ".fonts where id = ?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				Font res = new Font();

				String family = rs.getString("family");
				if (family != null) {
					res.fontFamily.addAll(asList(StringUtils.split(family, ",")));
				}
				String style = rs.getString("style");
				if (style != null) {
					try {
						res.fontStyle = Font.Style.valueOf(style.toUpperCase());
					}
					catch (IllegalArgumentException e) {
						LOG.debug("Found invalid font-style parameter '{}' for font with ID {}.", style, id);
						LOG.trace("Stack trace:", e);
					}
				}
				Boolean bold = (Boolean) rs.getObject("bold");
				if (bold != null) {
					res.bold = bold;
				}
				Double size = (Double) rs.getObject("size");
				if (size != null) {
					res.fontSize = size;
				}

				fonts.put(id, res);

				return res;
			}
			return null;
		}
		finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	private LinePlacement getLinePlacement(int id, Connection conn) throws SQLException {
		LinePlacement lineplacement = lineplacements.get(id);
		if (lineplacement != null) {
			return lineplacement;
		}

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn
				.prepareStatement("select perpendicularoffset, repeat, initialgap, gap, isaligned, generalizeline from "
						+ schema + ".lineplacements where id = ?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				LinePlacement res = new LinePlacement();

				Double perpendicularoffset = (Double) rs.getObject("perpendicularoffset");
				if (perpendicularoffset != null) {
					res.perpendicularOffset = perpendicularoffset;
				}
				Boolean repeat = (Boolean) rs.getObject("repeat");
				if (repeat != null) {
					res.repeat = repeat;
				}
				Double initialGap = (Double) rs.getObject("initialgap");
				if (initialGap != null) {
					res.initialGap = initialGap;
				}
				Double gap = (Double) rs.getObject("gap");
				if (gap != null) {
					res.gap = gap;
				}
				Boolean isaligned = (Boolean) rs.getObject("isaligned");
				if (isaligned != null) {
					res.isAligned = isaligned;
				}
				Boolean generalizeLine = (Boolean) rs.getObject("generalizeline");
				if (generalizeLine != null) {
					res.generalizeLine = generalizeLine;
				}

				lineplacements.put(id, res);

				return res;
			}
			return null;
		}
		finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	private Halo getHalo(int id, Connection conn) throws SQLException {
		Halo halo = halos.get(id);
		if (halo != null) {
			return halo;
		}

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement("select fill_id, radius from " + schema + ".halos where id = ?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				Halo res = new Halo();

				Integer fill = (Integer) rs.getObject("fill_id");
				if (fill != null) {
					res.fill = getFill(fill, conn);
				}
				Double radius = (Double) rs.getObject("radius");
				if (radius != null) {
					res.radius = radius;
				}

				halos.put(id, res);

				return res;
			}
			return null;
		}
		finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	private Pair<PointStyling, Continuation<PointStyling>> getPointStyling(int id, Connection conn)
			throws SQLException {
		PointStyling sym = points.get(id);
		Continuation<Styling<?>> contn = continuations.get(sym);
		if (sym != null) {
			return new Pair<PointStyling, Continuation<PointStyling>>(sym, (Continuation) contn);
		}

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement("select uom, graphic_id from " + schema + ".points where id = ?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				PointStyling res = new PointStyling();

				String uom = rs.getString("uom");
				if (uom != null) {
					res.uom = getUOM(uom);
				}
				Integer graphic = (Integer) rs.getObject("graphic_id");
				if (graphic != null) {
					Pair<Graphic, Continuation<Styling<?>>> p = getGraphic(graphic, conn, contn);
					res.graphic = p.first;
					contn = p.second;
				}

				return new Pair<PointStyling, Continuation<PointStyling>>(res, (Continuation) contn);
			}
			return null;
		}
		finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	private Pair<LineStyling, Continuation<LineStyling>> getLineStyling(int id, Connection conn) throws SQLException {
		LineStyling sym = lines.get(id);
		Continuation<Styling<?>> contn = continuations.get(sym);
		if (sym != null) {
			return new Pair<LineStyling, Continuation<LineStyling>>(sym, (Continuation) contn);
		}

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn
				.prepareStatement("select uom, stroke_id, perpendicularoffset from " + schema + ".lines where id = ?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				LineStyling res = new LineStyling();

				String uom = rs.getString("uom");
				if (uom != null) {
					res.uom = getUOM(uom);
				}
				Integer stroke = (Integer) rs.getObject("stroke_id");
				if (stroke != null) {
					Pair<Stroke, Continuation<Styling<?>>> p = getStroke(stroke, conn, contn);
					res.stroke = p.first;
					contn = p.second;
				}
				Double off = (Double) rs.getObject("perpendicularoffset");
				if (off != null) {
					res.perpendicularOffset = off;
				}

				return new Pair<LineStyling, Continuation<LineStyling>>(res, (Continuation) contn);
			}
			return null;
		}
		finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	private Pair<PolygonStyling, Continuation<PolygonStyling>> getPolygonStyling(int id, Connection conn)
			throws SQLException {
		PolygonStyling sym = polygons.get(id);
		Continuation<Styling<?>> contn = continuations.get(sym);
		if (sym != null) {
			return new Pair<PolygonStyling, Continuation<PolygonStyling>>(sym, (Continuation) contn);
		}

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(
					"select uom, fill_id, stroke_id, displacementx, displacementy, perpendicularoffset from " + schema
							+ ".polygons where id = ?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				PolygonStyling res = new PolygonStyling();

				String uom = rs.getString("uom");
				if (uom != null) {
					res.uom = getUOM(uom);
				}
				Integer fill = (Integer) rs.getObject("fill_id");
				if (fill != null) {
					res.fill = getFill(fill, conn);
				}
				Integer stroke = (Integer) rs.getObject("stroke_id");
				if (stroke != null) {
					Pair<Stroke, Continuation<Styling<?>>> p = getStroke(stroke, conn, contn);
					res.stroke = p.first;
					contn = p.second;
				}
				Double dx = (Double) rs.getObject("displacementx");
				if (dx != null) {
					res.displacementX = dx;
				}
				Double dy = (Double) rs.getObject("displacementy");
				if (dy != null) {
					res.displacementY = dy;
				}
				Double off = (Double) rs.getObject("perpendicularoffset");
				if (off != null) {
					res.perpendicularOffset = off;
				}

				return new Pair<PolygonStyling, Continuation<PolygonStyling>>(res, (Continuation) contn);
			}
			return null;
		}
		finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	private static Continuation<Styling<?>> getContn(String text, Continuation<Styling<?>> contn,
			final Updater<Styling<?>> updater) {
		XMLInputFactory fac = XMLInputFactory.newInstance();
		Expression expr;
		try {
			XMLStreamReader reader = fac.createXMLStreamReader(new StringReader(text));
			reader.next();
			expr = Filter110XMLDecoder.parseExpression(reader);
		}
		catch (XMLParsingException e) {
			String[] ss = text.split("}");
			expr = new ValueReference(new QName(ss[0].substring(1), ss[1]));
		}
		catch (XMLStreamException e) {
			String[] ss = text.split("}");
			expr = new ValueReference(new QName(ss[0].substring(1), ss[1]));
		}
		final Expression expr2 = expr;
		return new Continuation<Styling<?>>(contn) {
			@Override
			public void updateStep(Styling<?> base, Feature obj, XPathEvaluator<Feature> evaluator) {
				try {
					Object[] evald = expr2.evaluate(obj, evaluator);
					if (evald.length == 0) {
						LOG.warn("The following expression in a style evaluated to null:\n{}", expr2);
					}
					else {
						updater.update(base, evald[0].toString());
					}
				}
				catch (FilterEvaluationException e) {
					LOG.warn("Evaluating the following expression resulted in an error '{}':\n{}",
							e.getLocalizedMessage(), expr2);
				}
			}
		};
	}

	private Triple<TextStyling, Continuation<TextStyling>, String> getTextStyling(int id, Connection conn)
			throws SQLException {
		TextStyling sym = texts.get(id);
		Continuation<Styling<?>> contn = continuations.get(sym);
		if (sym != null) {
			return new Triple<TextStyling, Continuation<TextStyling>, String>(sym, (Continuation) contn, null);
		}

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(
					"select labelexpr, uom, font_id, fill_id, rotation, rotationexpr, displacementx, displacementy, anchorx, anchory, lineplacement_id, halo_id from "
							+ schema + ".texts where id = ?");
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				TextStyling res = new TextStyling();

				String labelexpr = rs.getString("labelexpr");
				String uom = rs.getString("uom");
				if (uom != null) {
					res.uom = getUOM(uom);
				}
				Integer font = (Integer) rs.getObject("font_id");
				if (font != null) {
					res.font = getFont(font, conn);
				}
				Integer fill = (Integer) rs.getObject("fill_id");
				if (fill != null) {
					res.fill = getFill(fill, conn);
				}
				Double rotation = (Double) rs.getObject("rotation");
				if (rotation != null) {
					res.rotation = rotation;
				}
				String rotationExpr = (String) rs.getObject("rotationexpr");
				if (rotationExpr != null) {
					contn = getContn(rotationExpr, contn, new Updater<Styling<?>>() {
						@Override
						public void update(Styling<?> obj, String val) {
							((TextStyling) obj).rotation = Double.parseDouble(val);
						}
					});
				}
				Double dx = (Double) rs.getObject("displacementx");
				if (dx != null) {
					res.displacementX = dx;
				}
				Double dy = (Double) rs.getObject("displacementy");
				if (dy != null) {
					res.displacementY = dy;
				}
				Double ax = (Double) rs.getObject("anchorx");
				if (ax != null) {
					res.anchorPointX = ax;
				}
				Double ay = (Double) rs.getObject("anchory");
				if (ay != null) {
					res.anchorPointY = ay;
				}
				Integer lineplacement = (Integer) rs.getObject("lineplacement_id");
				if (lineplacement != null) {
					res.linePlacement = getLinePlacement(lineplacement, conn);
				}
				Integer halo = (Integer) rs.getObject("halo_id");
				if (halo != null) {
					res.halo = getHalo(halo, conn);
				}

				return new Triple<TextStyling, Continuation<TextStyling>, String>(res, (Continuation) contn, labelexpr);
			}
			return null;
		}
		finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * @param id
	 * @return the corresponding style from the database
	 */
	public synchronized Style getStyle(int id) {
		Style style = pool.get(id);
		if (style != null) {
			return style;
		}

		XMLInputFactory fac = XMLInputFactory.newInstance();

		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = connProvider.getConnection();
			stmt = conn.prepareStatement(
					"select type, fk, minscale, maxscale, sld, name from " + schema + ".styles where id = ?");
			stmt.setInt(1, id);
			LOG.debug("Fetching styles using query '{}'.", stmt);
			rs = stmt.executeQuery();
			if (rs.next()) {
				String type = rs.getString("type");
				int key = rs.getInt("fk");
				String name = rs.getString("name");

				if (type != null) {
					final Symbolizer<?> sym;
					switch (Type.valueOf(type.toUpperCase())) {
						case LINE:
							Pair<LineStyling, Continuation<LineStyling>> lpair = getLineStyling(key, conn);
							if (lpair.second != null) {
								sym = new Symbolizer<LineStyling>(lpair.first, lpair.second, null, null, null, -1, -1);
							}
							else {
								sym = new Symbolizer<LineStyling>(lpair.first, null, null, null, -1, -1);
							}
							break;
						case POINT:
							Pair<PointStyling, Continuation<PointStyling>> pair = getPointStyling(key, conn);
							if (pair.second != null) {
								sym = new Symbolizer<PointStyling>(pair.first, pair.second, null, null, null, -1, -1);
							}
							else {
								sym = new Symbolizer<PointStyling>(pair.first, null, null, null, -1, -1);
							}
							break;
						case POLYGON:
							Pair<PolygonStyling, Continuation<PolygonStyling>> ppair = getPolygonStyling(key, conn);
							if (ppair.second != null) {
								sym = new Symbolizer<PolygonStyling>(ppair.first, ppair.second, null, null, null, -1,
										-1);
							}
							else {
								sym = new Symbolizer<PolygonStyling>(ppair.first, null, null, null, -1, -1);
							}
							break;
						case TEXT:
							Triple<TextStyling, Continuation<TextStyling>, String> p = getTextStyling(key, conn);
							XMLStreamReader reader = fac.createXMLStreamReader(new StringReader(p.third));
							reader.next();
							final Expression expr = Filter110XMLDecoder.parseExpression(reader);
							if (p.second != null) {
								sym = new Symbolizer<TextStyling>(p.first, p.second, null, null, null, -1, -1);
							}
							else {
								sym = new Symbolizer<TextStyling>(p.first, null, null, null, -1, -1);
							}
							labels.put((Symbolizer) sym, new Continuation<StringBuffer>() {
								@Override
								public void updateStep(StringBuffer base, Feature f,
										XPathEvaluator<Feature> evaluator) {
									try {
										Object[] evald = expr.evaluate(f, evaluator);
										if (evald.length == 0) {
											LOG.warn("The following expression in a style evaluated to null:\n{}",
													expr);
										}
										else {
											base.append(evald[0]);
										}
									}
									catch (FilterEvaluationException e) {
										LOG.warn("Evaluating the following expression resulted in an error '{}':\n{}",
												e.getLocalizedMessage(), expr);
									}
								}
							});
							break;
						default:
							sym = null;
							break;
					}

					LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>> rules = new LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>>();

					DoublePair scale = new DoublePair(NEGATIVE_INFINITY, POSITIVE_INFINITY);
					Double min = (Double) rs.getObject("minscale");
					if (min != null) {
						scale.first = min;
					}
					Double max = (Double) rs.getObject("maxscale");
					if (max != null) {
						scale.second = max;
					}

					Continuation<LinkedList<Symbolizer<?>>> contn = new Continuation<LinkedList<Symbolizer<?>>>() {
						@Override
						public void updateStep(LinkedList<Symbolizer<?>> base, Feature f,
								XPathEvaluator<Feature> evaluator) {
							base.add(sym);
						}
					};
					rules.add(new Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>(contn, scale));

					Style result = new Style(rules, labels, null, name == null ? ("" + id) : name, null);
					pool.put(id, result);
					return result;
				}
				String sld = rs.getString("sld");
				if (sld != null) {
					try {
						Style res = new SymbologyParser()
							.parse(fac.createXMLStreamReader(baseSystemId, new StringReader(sld)));
						if (name != null) {
							res.setName(name);
						}
						pool.put(id, res);
						return res;
					}
					catch (XMLStreamException e) {
						LOG.debug("Could not parse SLD snippet for id '{}', error was '{}'", id,
								e.getLocalizedMessage());
						LOG.trace("Stack trace:", e);
					}
					catch (FactoryConfigurationError e) {
						LOG.debug("Could not parse SLD snippet for id '{}', error was '{}'", id,
								e.getLocalizedMessage());
						LOG.trace("Stack trace:", e);
					}
				}

				LOG.debug("For style id '{}', no SLD snippet was found and no symbolizer referenced.", id);
			}
			return null;
		}
		catch (Throwable e) {
			LOG.info("Unable to read style from DB: '{}'.", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
			return null;
		}
		finally {
			JDBCUtils.close(rs, stmt, conn, LOG);
		}
	}

}
