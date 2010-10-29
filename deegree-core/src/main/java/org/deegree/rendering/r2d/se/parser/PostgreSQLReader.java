//$HeadURL$
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
package org.deegree.rendering.r2d.se.parser;

import static java.awt.Color.decode;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.util.Arrays.asList;
import static org.deegree.commons.jdbc.ConnectionManager.getConnection;
import static org.deegree.commons.utils.ArrayUtils.splitAsDoubles;
import static org.deegree.rendering.i18n.Messages.get;
import static org.deegree.rendering.r2d.RenderHelper.getShapeFromSvg;
import static org.deegree.rendering.r2d.se.parser.SymbologyParser.getUOM;
import static org.deegree.rendering.r2d.styling.components.Mark.SimpleMark.SQUARE;
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
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.xerces.impl.dv.util.Base64;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.feature.Feature;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.rendering.r2d.se.unevaluated.Continuation;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.rendering.r2d.se.unevaluated.Symbolizer;
import org.deegree.rendering.r2d.styling.LineStyling;
import org.deegree.rendering.r2d.styling.PointStyling;
import org.deegree.rendering.r2d.styling.PolygonStyling;
import org.deegree.rendering.r2d.styling.TextStyling;
import org.deegree.rendering.r2d.styling.components.Fill;
import org.deegree.rendering.r2d.styling.components.Font;
import org.deegree.rendering.r2d.styling.components.Graphic;
import org.deegree.rendering.r2d.styling.components.Halo;
import org.deegree.rendering.r2d.styling.components.LinePlacement;
import org.deegree.rendering.r2d.styling.components.Stroke;
import org.deegree.rendering.r2d.styling.components.Mark.SimpleMark;
import org.deegree.rendering.r2d.styling.components.Stroke.LineCap;
import org.deegree.rendering.r2d.styling.components.Stroke.LineJoin;
import org.slf4j.Logger;

/**
 * <code>PostgreSQLReader</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(debug = "logs when problematic styles were found in the database", info = "logs problems when accessing the DB", trace = "logs stack traces")
public class PostgreSQLReader {

    enum Type {
        POINT, LINE, POLYGON, TEXT
    }

    static final Logger LOG = getLogger( PostgreSQLReader.class );

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

    private String connid;

    /**
     * @param connid
     */
    public PostgreSQLReader( String connid ) {
        this.connid = connid;
    }

    private Graphic getGraphic( int id, Connection conn )
                            throws SQLException {
        Graphic graphic = graphics.get( id );
        if ( graphic != null ) {
            return graphic;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "select size, rotation, anchorx, anchory, displacementx, displacementy, wellknownname, svg, base64raster, fill_id, stroke_id from graphics where id = ?" );
            stmt.setInt( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                Graphic res = new Graphic();

                Double size = (Double) rs.getObject( "size" );
                if ( size != null ) {
                    res.size = size;
                }
                Double rotation = (Double) rs.getObject( "rotation" );
                if ( rotation != null ) {
                    res.rotation = rotation;
                }
                Double ax = (Double) rs.getObject( "anchorx" );
                if ( ax != null ) {
                    res.anchorPointX = ax;
                }
                Double ay = (Double) rs.getObject( "anchory" );
                if ( ay != null ) {
                    res.anchorPointY = ay;
                }
                Double dx = (Double) rs.getObject( "displacementx" );
                if ( dx != null ) {
                    res.displacementX = dx;
                }
                Double dy = (Double) rs.getObject( "displacementy" );
                if ( dy != null ) {
                    res.displacementY = dy;
                }
                String wkn = rs.getString( "wellknownname" );
                if ( wkn != null ) {
                    try {
                        res.mark.wellKnown = SimpleMark.valueOf( wkn.toUpperCase() );
                    } catch ( IllegalArgumentException e ) {
                        LOG.debug( "Found unknown 'well known name' '{}' for the symbol with "
                                   + "id '{}' in the database, using square instead.", wkn, id );
                        res.mark.wellKnown = SQUARE;
                    }
                }
                String svg = rs.getString( "svg" );
                if ( svg != null ) {
                    try {
                        res.mark.shape = getShapeFromSvg( new ByteArrayInputStream( svg.getBytes( "UTF-8" ) ), null );
                    } catch ( UnsupportedEncodingException e ) {
                        LOG.trace( "Stack trace:", e );
                    }
                }
                String base64raster = rs.getString( "base64raster" );
                if ( base64raster != null ) {
                    ByteArrayInputStream bis = new ByteArrayInputStream( Base64.decode( base64raster ) );
                    try {
                        res.image = ImageIO.read( bis );
                    } catch ( IOException e ) {
                        LOG.debug( "A base64 encoded image could not be read from the database,"
                                   + " for the symbol with id '{}', error was '{}'.", id, e.getLocalizedMessage() );
                        LOG.trace( "Stack trace:", e );
                    }
                }
                Integer fill = (Integer) rs.getObject( "fill_id" );
                if ( fill != null ) {
                    res.mark.fill = getFill( fill, conn );
                }
                Integer stroke = (Integer) rs.getObject( "stroke_id" );
                if ( stroke != null ) {
                    res.mark.stroke = getStroke( stroke, conn );
                }

                graphics.put( id, res );

                return res;
            }
            return null;
        } finally {
            if ( rs != null ) {
                rs.close();
            }
            if ( stmt != null ) {
                stmt.close();
            }
        }
    }

    private Stroke getStroke( int id, Connection conn )
                            throws SQLException {
        Stroke stroke = strokes.get( id );
        if ( stroke != null ) {
            return stroke;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "select color, width, linejoin, linecap, dasharray, dashoffset, stroke_graphic_id, fill_graphic_id, strokegap, strokeinitialgap, positionpercentage from strokes where id = ?" );
            stmt.setInt( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                Stroke res = new Stroke();

                String color = rs.getString( "color" );
                if ( color != null ) {
                    res.color = decode( color );
                }
                Double width = (Double) rs.getObject( "width" );
                if ( width != null ) {
                    res.width = width;
                }
                String linejoin = rs.getString( "linejoin" );
                if ( linejoin != null ) {
                    try {
                        res.linejoin = LineJoin.valueOf( linejoin.toUpperCase() );
                    } catch ( IllegalArgumentException e ) {
                        LOG.debug( "The linejoin value '{}' for stroke with id '{}' could not be parsed.", linejoin, id );
                    }
                }
                String linecap = rs.getString( "linecap" );
                if ( linecap != null ) {
                    try {
                        res.linecap = LineCap.valueOf( linecap.toUpperCase() );
                    } catch ( IllegalArgumentException e ) {
                        LOG.debug( "The linecap value '{}' for stroke with id '{}' could not be parsed.", linecap, id );
                    }
                }
                String dasharray = rs.getString( "dasharray" );
                if ( dasharray != null ) {
                    res.dasharray = splitAsDoubles( dasharray, " " );
                }
                Double dashoffset = (Double) rs.getObject( "dashoffset" );
                if ( dashoffset != null ) {
                    res.dashoffset = dashoffset;
                }
                Integer graphicstroke = (Integer) rs.getObject( "stroke_graphic_id" );
                if ( graphicstroke != null ) {
                    res.stroke = getGraphic( graphicstroke, conn );
                }
                Integer graphicfill = (Integer) rs.getObject( "fill_graphic_id" );
                if ( graphicfill != null ) {
                    res.fill = getGraphic( graphicfill, conn );
                }
                Double strokegap = (Double) rs.getObject( "strokegap" );
                if ( strokegap != null ) {
                    res.strokeGap = strokegap;
                }
                Double strokeinitialgap = (Double) rs.getObject( "strokeinitialgap" );
                if ( strokeinitialgap != null ) {
                    res.strokeInitialGap = strokeinitialgap;
                }
                Double positionPercentage = (Double) rs.getObject( "positionpercentage" );
                if ( positionPercentage != null ) {
                    res.positionPercentage = positionPercentage;
                }

                strokes.put( id, res );

                return res;
            }
            return null;
        } finally {
            if ( rs != null ) {
                rs.close();
            }
            if ( stmt != null ) {
                stmt.close();
            }
        }
    }

    private Fill getFill( int id, Connection conn )
                            throws SQLException {
        Fill fill = fills.get( id );
        if ( fill != null ) {
            return fill;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "select color, graphic_id from fills where id = ?" );
            stmt.setInt( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                Fill res = new Fill();

                String color = rs.getString( "color" );
                if ( color != null ) {
                    res.color = decode( color );
                }
                Integer graphic = (Integer) rs.getObject( "graphic_id" );
                if ( graphic != null ) {
                    res.graphic = getGraphic( graphic, conn );
                }

                fills.put( id, res );

                return res;
            }
            return null;
        } finally {
            if ( rs != null ) {
                rs.close();
            }
            if ( stmt != null ) {
                stmt.close();
            }
        }
    }

    private Font getFont( int id, Connection conn )
                            throws SQLException {
        Font font = fonts.get( id );
        if ( font != null ) {
            return font;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "select family, style, bold, size from fonts where id = ?" );
            stmt.setInt( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                Font res = new Font();

                String family = rs.getString( "family" );
                if ( family != null ) {
                    res.fontFamily.addAll( asList( StringUtils.split( family, "," ) ) );
                }
                String style = rs.getString( "style" );
                if ( style != null ) {
                    try {
                        res.fontStyle = Font.Style.valueOf( style.toUpperCase() );
                    } catch ( IllegalArgumentException e ) {
                        LOG.debug( "Found invalid font-style parameter '{}' for font with ID {}.", style, id );
                        LOG.trace( "Stack trace:", e );
                    }
                }
                Boolean bold = (Boolean) rs.getObject( "bold" );
                if ( bold != null ) {
                    res.bold = bold;
                }
                Integer size = (Integer) rs.getObject( "size" );
                if ( size != null ) {
                    res.fontSize = size;
                }

                fonts.put( id, res );

                return res;
            }
            return null;
        } finally {
            if ( rs != null ) {
                rs.close();
            }
            if ( stmt != null ) {
                stmt.close();
            }
        }
    }

    private LinePlacement getLinePlacement( int id, Connection conn )
                            throws SQLException {
        LinePlacement lineplacement = lineplacements.get( id );
        if ( lineplacement != null ) {
            return lineplacement;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "select perpendicularoffset, repeat, initialgap, gap, isaligned, generalizeline from lineplacements where id = ?" );
            stmt.setInt( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                LinePlacement res = new LinePlacement();

                Double perpendicularoffset = (Double) rs.getObject( "perpendicularoffset" );
                if ( perpendicularoffset != null ) {
                    res.perpendicularOffset = perpendicularoffset;
                }
                Boolean repeat = (Boolean) rs.getObject( "repeat" );
                if ( repeat != null ) {
                    res.repeat = repeat;
                }
                Double initialGap = (Double) rs.getObject( "initialgap" );
                if ( initialGap != null ) {
                    res.initialGap = initialGap;
                }
                Double gap = (Double) rs.getObject( "gap" );
                if ( gap != null ) {
                    res.gap = gap;
                }
                Boolean isaligned = (Boolean) rs.getObject( "isaligned" );
                if ( isaligned != null ) {
                    res.isAligned = isaligned;
                }
                Boolean generalizeLine = (Boolean) rs.getObject( "generalizeline" );
                if ( generalizeLine != null ) {
                    res.generalizeLine = generalizeLine;
                }

                lineplacements.put( id, res );

                return res;
            }
            return null;
        } finally {
            if ( rs != null ) {
                rs.close();
            }
            if ( stmt != null ) {
                stmt.close();
            }
        }
    }

    private Halo getHalo( int id, Connection conn )
                            throws SQLException {
        Halo halo = halos.get( id );
        if ( halo != null ) {
            return halo;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "select fill_id, radius from halos where id = ?" );
            stmt.setInt( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                Halo res = new Halo();

                Integer fill = (Integer) rs.getObject( "fill_id" );
                if ( fill != null ) {
                    res.fill = getFill( fill, conn );
                }
                Double radius = (Double) rs.getObject( "radius" );
                if ( radius != null ) {
                    res.radius = radius;
                }

                halos.put( id, res );

                return res;
            }
            return null;
        } finally {
            if ( rs != null ) {
                rs.close();
            }
            if ( stmt != null ) {
                stmt.close();
            }
        }
    }

    private PointStyling getPointStyling( int id, Connection conn )
                            throws SQLException {
        PointStyling sym = points.get( id );
        if ( sym != null ) {
            return sym;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "select uom, graphic_id from points where id = ?" );
            stmt.setInt( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                PointStyling res = new PointStyling();

                String uom = rs.getString( "uom" );
                if ( uom != null ) {
                    res.uom = getUOM( uom );
                }
                Integer graphic = (Integer) rs.getObject( "graphic_id" );
                if ( graphic != null ) {
                    res.graphic = getGraphic( graphic, conn );
                }

                return res;
            }
            return null;
        } finally {
            if ( rs != null ) {
                rs.close();
            }
            if ( stmt != null ) {
                stmt.close();
            }
        }
    }

    private LineStyling getLineStyling( int id, Connection conn )
                            throws SQLException {
        LineStyling sym = lines.get( id );
        if ( sym != null ) {
            return sym;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "select uom, stroke_id, perpendicularoffset from lines where id = ?" );
            stmt.setInt( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                LineStyling res = new LineStyling();

                String uom = rs.getString( "uom" );
                if ( uom != null ) {
                    res.uom = getUOM( uom );
                }
                Integer stroke = (Integer) rs.getObject( "stroke_id" );
                if ( stroke != null ) {
                    res.stroke = getStroke( stroke, conn );
                }
                Double off = (Double) rs.getObject( "perpendicularoffset" );
                if ( off != null ) {
                    res.perpendicularOffset = off;
                }

                return res;
            }
            return null;
        } finally {
            if ( rs != null ) {
                rs.close();
            }
            if ( stmt != null ) {
                stmt.close();
            }
        }
    }

    private PolygonStyling getPolygonStyling( int id, Connection conn )
                            throws SQLException {
        PolygonStyling sym = polygons.get( id );
        if ( sym != null ) {
            return sym;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "select uom, fill_id, stroke_id, displacementx, displacementy, perpendicularoffset from polygons where id = ?" );
            stmt.setInt( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                PolygonStyling res = new PolygonStyling();

                String uom = rs.getString( "uom" );
                if ( uom != null ) {
                    res.uom = getUOM( uom );
                }
                Integer fill = (Integer) rs.getObject( "fill_id" );
                if ( fill != null ) {
                    res.fill = getFill( fill, conn );
                }
                Integer stroke = (Integer) rs.getObject( "stroke_id" );
                if ( stroke != null ) {
                    res.stroke = getStroke( stroke, conn );
                }
                Double dx = (Double) rs.getObject( "displacementx" );
                if ( dx != null ) {
                    res.displacementX = dx;
                }
                Double dy = (Double) rs.getObject( "displacementy" );
                if ( dy != null ) {
                    res.displacementY = dy;
                }
                Double off = (Double) rs.getObject( "perpendicularoffset" );
                if ( off != null ) {
                    res.perpendicularOffset = off;
                }

                return res;
            }
            return null;
        } finally {
            if ( rs != null ) {
                rs.close();
            }
            if ( stmt != null ) {
                stmt.close();
            }
        }
    }

    private Pair<TextStyling, String> getTextStyling( int id, Connection conn )
                            throws SQLException {
        TextStyling sym = texts.get( id );
        if ( sym != null ) {
            return new Pair<TextStyling, String>( sym, null );
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "select labelexpr, uom, font_id, fill_id, rotation, displacementx, displacementy, anchorx, anchory, lineplacement_id, halo_id from texts where id = ?" );
            stmt.setInt( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                TextStyling res = new TextStyling();

                String labelexpr = rs.getString( "labelexpr" );
                String uom = rs.getString( "uom" );
                if ( uom != null ) {
                    res.uom = getUOM( uom );
                }
                Integer font = (Integer) rs.getObject( "font_id" );
                if ( font != null ) {
                    res.font = getFont( font, conn );
                }
                Integer fill = (Integer) rs.getObject( "fill_id" );
                if ( fill != null ) {
                    res.fill = getFill( fill, conn );
                }
                Double rotation = (Double) rs.getObject( "rotation" );
                if ( rotation != null ) {
                    res.rotation = rotation;
                }
                Double dx = (Double) rs.getObject( "displacementx" );
                if ( dx != null ) {
                    res.displacementX = dx;
                }
                Double dy = (Double) rs.getObject( "displacementy" );
                if ( dy != null ) {
                    res.displacementY = dy;
                }
                Double ax = (Double) rs.getObject( "anchorx" );
                if ( ax != null ) {
                    res.anchorPointX = ax;
                }
                Double ay = (Double) rs.getObject( "anchory" );
                if ( ay != null ) {
                    res.anchorPointY = ay;
                }
                Integer lineplacement = (Integer) rs.getObject( "lineplacement_id" );
                if ( lineplacement != null ) {
                    res.linePlacement = getLinePlacement( lineplacement, conn );
                }
                Integer halo = (Integer) rs.getObject( "halo_id" );
                if ( halo != null ) {
                    res.halo = getHalo( halo, conn );
                }

                return new Pair<TextStyling, String>( res, labelexpr );
            }
            return null;
        } finally {
            if ( rs != null ) {
                rs.close();
            }
            if ( stmt != null ) {
                stmt.close();
            }
        }
    }

    /**
     * @param id
     * @return the corresponding style from the database
     */
    public Style getStyle( int id ) {
        Style style = pool.get( id );
        if ( style != null ) {
            return style;
        }

        XMLInputFactory fac = XMLInputFactory.newInstance();

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = getConnection( connid );
            stmt = conn.prepareStatement( "select type, fk, minscale, maxscale, sld, name from styles where id = ?" );
            stmt.setInt( 1, id );
            LOG.debug( "Fetching styles using query '{}'.", stmt );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                String type = rs.getString( "type" );
                int key = rs.getInt( "fk" );
                String name = rs.getString( "name" );

                if ( type != null ) {
                    final Symbolizer<?> sym;
                    switch ( Type.valueOf( type.toUpperCase() ) ) {
                    case LINE:
                        sym = new Symbolizer<LineStyling>( getLineStyling( key, conn ), null, null, null, -1, -1 );
                        break;
                    case POINT:
                        sym = new Symbolizer<PointStyling>( getPointStyling( key, conn ), null, null, null, -1, -1 );
                        break;
                    case POLYGON:
                        sym = new Symbolizer<PolygonStyling>( getPolygonStyling( key, conn ), null, null, null, -1, -1 );
                        break;
                    case TEXT:
                        Pair<TextStyling, String> p = getTextStyling( key, conn );
                        XMLStreamReader reader = fac.createXMLStreamReader( new StringReader( p.second ) );
                        reader.next();
                        final Expression expr = Filter110XMLDecoder.parseExpression( reader );
                        sym = new Symbolizer<TextStyling>( p.first, null, null, null, -1, -1 );
                        labels.put( (Symbolizer) sym, new Continuation<StringBuffer>() {
                            @Override
                            public void updateStep( StringBuffer base, Feature f, XPathEvaluator<Feature> evaluator ) {
                                try {
                                    Object[] evald = expr.evaluate( f, evaluator );
                                    if ( evald.length == 0 ) {
                                        LOG.warn( get( "R2D.EXPRESSION_TO_NULL" ), expr );
                                    } else {
                                        base.append( evald[0] );
                                    }
                                } catch ( FilterEvaluationException e ) {
                                    LOG.warn( get( "R2D.ERROR_EVAL" ), e.getLocalizedMessage(), expr );
                                }
                            }
                        } );
                        break;
                    default:
                        sym = null;
                        break;
                    }

                    LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>> rules = new LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>>();

                    DoublePair scale = new DoublePair( NEGATIVE_INFINITY, POSITIVE_INFINITY );
                    Double min = (Double) rs.getObject( "minscale" );
                    if ( min != null ) {
                        scale.first = min;
                    }
                    Double max = (Double) rs.getObject( "maxscale" );
                    if ( max != null ) {
                        scale.second = max;
                    }

                    Continuation<LinkedList<Symbolizer<?>>> contn = new Continuation<LinkedList<Symbolizer<?>>>() {
                        @Override
                        public void updateStep( LinkedList<Symbolizer<?>> base, Feature f,
                                                XPathEvaluator<Feature> evaluator ) {
                            base.add( sym );
                        }
                    };
                    rules.add( new Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>( contn, scale ) );

                    return new Style( rules, labels, null, name == null ? ( "" + id ) : name, null );
                }
                String sld = rs.getString( "sld" );
                if ( sld != null ) {
                    try {
                        Style res = new SymbologyParser().parse( fac.createXMLStreamReader( new StringReader( sld ) ) );
                        if ( name != null ) {
                            res.setName( name );
                        }
                        return res;
                    } catch ( XMLStreamException e ) {
                        LOG.debug( "Could not parse SLD snippet for id '{}', error was '{}'", id,
                                   e.getLocalizedMessage() );
                        LOG.trace( "Stack trace:", e );
                    } catch ( FactoryConfigurationError e ) {
                        LOG.debug( "Could not parse SLD snippet for id '{}', error was '{}'", id,
                                   e.getLocalizedMessage() );
                        LOG.trace( "Stack trace:", e );
                    }
                }

                LOG.debug( "For style id '{}', no SLD snippet was found and no symbolizer referenced.", id );
            }
            return null;
        } catch ( SQLException e ) {
            LOG.info( "Unable to read style from DB: '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
            return null;
        } catch ( XMLStreamException e ) {
            LOG.info( "Unable to read style from DB: '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
            return null;
        } catch ( XMLParsingException e ) {
            LOG.info( "Unable to read style from DB: '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
            return null;
        } catch ( FactoryConfigurationError e ) {
            LOG.info( "Unable to read style from DB: '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
            return null;
        } finally {
            if ( rs != null ) {
                try {
                    rs.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to read style from DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to read style from DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( conn != null ) {
                try {
                    conn.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to read style from DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }
}
