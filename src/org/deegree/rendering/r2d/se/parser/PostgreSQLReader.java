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
import static org.deegree.commons.jdbc.ConnectionManager.getConnection;
import static org.deegree.commons.utils.ArrayUtils.splitAsDoubles;
import static org.deegree.rendering.r2d.RenderHelper.getShapeFromSvg;
import static org.deegree.rendering.r2d.se.parser.SymbologyParser.getUOM;
import static org.deegree.rendering.r2d.se.parser.SymbologyParser.parse;
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

import org.apache.xerces.impl.dv.util.Base64;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.filter.MatchableObject;
import org.deegree.rendering.r2d.se.unevaluated.Continuation;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.rendering.r2d.se.unevaluated.Symbolizer;
import org.deegree.rendering.r2d.styling.LineStyling;
import org.deegree.rendering.r2d.styling.PointStyling;
import org.deegree.rendering.r2d.styling.PolygonStyling;
import org.deegree.rendering.r2d.styling.components.Fill;
import org.deegree.rendering.r2d.styling.components.Graphic;
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
@LoggingNotes(debug = "logs when problematic styles were found in the database", trace = "logs stack traces")
public class PostgreSQLReader {

    enum Type {
        POINT, LINE, POLYGON
    }

    private static final Logger LOG = getLogger( PostgreSQLReader.class );

    private final HashMap<String, Style> pool = new HashMap<String, Style>();

    private final HashMap<String, Fill> fills = new HashMap<String, Fill>();

    private final HashMap<String, Stroke> strokes = new HashMap<String, Stroke>();

    private final HashMap<String, Graphic> graphics = new HashMap<String, Graphic>();

    private final HashMap<String, PointStyling> points = new HashMap<String, PointStyling>();

    private final HashMap<String, LineStyling> lines = new HashMap<String, LineStyling>();

    private final HashMap<String, PolygonStyling> polygons = new HashMap<String, PolygonStyling>();

    private String connid;

    /**
     * @param connid
     * @throws SQLException
     * 
     */
    public PostgreSQLReader( String connid ) throws SQLException {
        this.connid = connid;
    }

    private Graphic getGraphic( String id, Connection conn )
                            throws SQLException {
        Graphic graphic = graphics.get( id );
        if ( graphic != null ) {
            return graphic;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "select size, rotation, anchorx, anchory, displacementx, displacementy, wellknownname, svg, base64raster, fill_id, stroke_id from graphics where id = ?" );
            stmt.setString( 1, id );
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
                String fill = rs.getString( "fill_id" );
                if ( fill != null ) {
                    res.mark.fill = getFill( fill, conn );
                }
                String stroke = rs.getString( "stroke_id" );
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

    private Stroke getStroke( String id, Connection conn )
                            throws SQLException {
        Stroke stroke = strokes.get( id );
        if ( stroke != null ) {
            return stroke;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "select color, width, linejoin, linecap, dasharray, dashoffset, stroke_graphic_id, fill_graphic_id, strokegap, strokeinitialgap, positionpercentage from strokes where id = ?" );
            stmt.setString( 1, id );
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
                String graphicstroke = rs.getString( "stroke_graphic_id" );
                if ( graphicstroke != null ) {
                    res.stroke = getGraphic( graphicstroke, conn );
                }
                String graphicfill = rs.getString( "fill_graphic_id" );
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

    private Fill getFill( String id, Connection conn )
                            throws SQLException {
        Fill fill = fills.get( id );
        if ( fill != null ) {
            return fill;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "select color, graphic_id from fills where id = ?" );
            stmt.setString( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                Fill res = new Fill();

                String color = rs.getString( "color" );
                if ( color != null ) {
                    res.color = decode( color );
                }
                String graphic = rs.getString( "graphic_id" );
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

    private PointStyling getPointStyling( String id, Connection conn )
                            throws SQLException {
        PointStyling sym = points.get( id );
        if ( sym != null ) {
            return sym;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "select uom, graphic_id from points where id = ?" );
            stmt.setString( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                PointStyling res = new PointStyling();

                String uom = rs.getString( "uom" );
                if ( uom != null ) {
                    res.uom = getUOM( uom );
                }
                String graphic = rs.getString( "graphic_id" );
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

    private LineStyling getLineStyling( String id, Connection conn )
                            throws SQLException {
        LineStyling sym = lines.get( id );
        if ( sym != null ) {
            return sym;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "select uom, stroke_id, perpendicularoffset from lines where id = ?" );
            stmt.setString( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                LineStyling res = new LineStyling();

                String uom = rs.getString( "uom" );
                if ( uom != null ) {
                    res.uom = getUOM( uom );
                }
                String stroke = rs.getString( "stroke_id" );
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

    private PolygonStyling getPolygonStyling( String id, Connection conn )
                            throws SQLException {
        PolygonStyling sym = polygons.get( id );
        if ( sym != null ) {
            return sym;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "select uom, fill_id, stroke_id, displacementx, displacementy, perpendicularoffset from polygons where id = ?" );
            stmt.setString( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                PolygonStyling res = new PolygonStyling();

                String uom = rs.getString( "uom" );
                if ( uom != null ) {
                    res.uom = getUOM( uom );
                }
                String fill = rs.getString( "fill_id" );
                if ( fill != null ) {
                    res.fill = getFill( fill, conn );
                }
                String stroke = rs.getString( "stroke_id" );
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

    /**
     * @param id
     * @return the corresponding style from the database
     * @throws SQLException
     */
    public Style getStyle( String id )
                            throws SQLException {
        Style style = pool.get( id );
        if ( style != null ) {
            return style;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = getConnection( connid );
            stmt = conn.prepareStatement( "select type, fk, minscale, maxscale, sld from styles where id = ?" );
            stmt.setString( 1, id );
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                String type = rs.getString( "type" );
                String key = rs.getString( "fk" );
                if ( type != null && key != null ) {
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
                        public void updateStep( LinkedList<Symbolizer<?>> base, MatchableObject f ) {
                            base.add( sym );
                        }
                    };
                    rules.add( new Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>( contn, scale ) );

                    return new Style( rules, null, id, null );
                }
                String sld = rs.getString( "sld" );
                if ( sld != null ) {
                    try {
                        return parse( XMLInputFactory.newInstance().createXMLStreamReader( new StringReader( sld ) ) );
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
        } finally {
            if ( rs != null ) {
                rs.close();
            }
            if ( stmt != null ) {
                stmt.close();
            }
            if ( conn != null ) {
                conn.close();
            }
        }
    }

}
