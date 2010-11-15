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

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.INTEGER;
import static java.sql.Types.VARCHAR;
import static org.deegree.commons.jdbc.ConnectionManager.getConnection;
import static org.deegree.commons.utils.ArrayUtils.join;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Triple;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.rendering.r2d.styling.LineStyling;
import org.deegree.rendering.r2d.styling.PointStyling;
import org.deegree.rendering.r2d.styling.PolygonStyling;
import org.deegree.rendering.r2d.styling.Styling;
import org.deegree.rendering.r2d.styling.TextStyling;
import org.deegree.rendering.r2d.styling.components.Fill;
import org.deegree.rendering.r2d.styling.components.Font;
import org.deegree.rendering.r2d.styling.components.Graphic;
import org.deegree.rendering.r2d.styling.components.Halo;
import org.deegree.rendering.r2d.styling.components.LinePlacement;
import org.deegree.rendering.r2d.styling.components.Stroke;
import org.slf4j.Logger;

/**
 * <code>PostgreSQLWriter</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(trace = "logs stack traces", info = "logs connection problems with the DB")
public class PostgreSQLWriter {

    private static final Logger LOG = getLogger( PostgreSQLWriter.class );

    private final String connId;

    /**
     * @param connId
     */
    public PostgreSQLWriter( String connId ) {
        this.connId = connId;
    }

    private int write( Connection conn, Graphic graphic )
                            throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "insert into graphics (size, rotation, anchorx, anchory, displacementx, displacementy, wellknownname, svg, base64raster, fill_id, stroke_id) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) returning id" );
            stmt.setDouble( 1, graphic.size );
            stmt.setDouble( 2, graphic.rotation );
            stmt.setDouble( 3, graphic.anchorPointX );
            stmt.setDouble( 4, graphic.anchorPointY );
            stmt.setDouble( 5, graphic.displacementX );
            stmt.setDouble( 6, graphic.displacementY );

            // maybe a little harsh, but better than mangling it w/ the if/else below
            stmt.setNull( 7, VARCHAR );
            stmt.setNull( 8, VARCHAR );
            stmt.setNull( 9, VARCHAR );
            stmt.setNull( 10, INTEGER );
            stmt.setNull( 11, INTEGER );
            if ( graphic.image != null ) {
                // TODO base64PNG
            } else if ( graphic.mark != null ) {
                if ( graphic.mark.shape != null ) {
                    // TODO svg?
                } else {
                    stmt.setString( 7, graphic.mark.wellKnown.toString() );
                }
                if ( graphic.mark.fill != null ) {
                    stmt.setInt( 10, write( conn, graphic.mark.fill ) );
                }
                if ( graphic.mark.stroke != null ) {
                    stmt.setInt( 11, write( conn, graphic.mark.stroke ) );
                }
            }
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                return rs.getInt( 1 );
            }
            return -1;
        } finally {
            if ( rs != null ) {
                try {
                    rs.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }

    private int write( Connection conn, Fill fill )
                            throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "insert into fills (color, graphic_id) values (?, ?) returning id" );
            String hex = Integer.toHexString( fill.color.getRGB() & 0xffffff );
            while ( hex.length() < 6 ) {
                hex = "0" + hex;
            }
            stmt.setString( 1, "#" + hex );
            if ( fill.graphic != null ) {
                stmt.setInt( 2, write( conn, fill.graphic ) );
            } else {
                stmt.setNull( 2, INTEGER );
            }
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                return rs.getInt( 1 );
            }
            return -1;
        } finally {
            if ( rs != null ) {
                try {
                    rs.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }

    private int write( Connection conn, Stroke stroke )
                            throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "insert into strokes (color, width, linejoin, linecap, dasharray, dashoffset, stroke_graphic_id, fill_graphic_id, strokegap, strokeinitialgap, positionpercentage) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) returning id" );
            String hex = Integer.toHexString( stroke.color.getRGB() & 0xffffff );
            while ( hex.length() < 6 ) {
                hex = "0" + hex;
            }
            stmt.setString( 1, "#" + hex );
            stmt.setDouble( 2, stroke.width );
            if ( stroke.linejoin != null ) {
                stmt.setString( 3, stroke.linejoin.toString() );
            } else {
                stmt.setNull( 3, VARCHAR );
            }
            if ( stroke.linecap != null ) {
                stmt.setString( 4, stroke.linecap.toString() );
            } else {
                stmt.setNull( 4, VARCHAR );
            }
            if ( stroke.dasharray != null ) {
                stmt.setString( 5, join( " ", stroke.dasharray ) );
            } else {
                stmt.setNull( 5, VARCHAR );
            }
            stmt.setDouble( 6, stroke.dashoffset );
            if ( stroke.stroke != null ) {
                stmt.setInt( 7, write( conn, stroke.stroke ) );
            } else {
                stmt.setNull( 7, INTEGER );
            }
            if ( stroke.fill != null ) {
                stmt.setInt( 8, write( conn, stroke.fill ) );
            } else {
                stmt.setNull( 8, INTEGER );
            }
            stmt.setDouble( 9, stroke.strokeGap );
            stmt.setDouble( 10, stroke.strokeInitialGap );
            if ( stroke.positionPercentage >= 0 ) {
                stmt.setDouble( 11, stroke.positionPercentage );
            } else {
                stmt.setNull( 11, DOUBLE );
            }
            rs = stmt.executeQuery();
            if ( rs.next() ) {
                return rs.getInt( 1 );
            }
            return -1;
        } finally {
            if ( rs != null ) {
                try {
                    rs.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }

    private int write( Connection conn, Font font )
                            throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "insert into fonts (family, style, bold, size) values (?, ?, ?, ?) returning id" );

            stmt.setString( 1, join( ",", font.fontFamily ) );
            stmt.setString( 2, font.fontStyle.toString() );
            stmt.setBoolean( 3, font.bold );
            stmt.setInt( 4, font.fontSize );

            rs = stmt.executeQuery();
            if ( rs.next() ) {
                return rs.getInt( 1 );
            }
            return -1;
        } finally {
            if ( rs != null ) {
                try {
                    rs.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }

    private int write( Connection conn, LinePlacement lineplacement )
                            throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "insert into lineplacements (perpendicularoffset, repeat, initialgap, gap, isaligned, generalizeline) values (?, ?, ?, ?, ?, ?) returning id" );

            stmt.setDouble( 1, lineplacement.perpendicularOffset );
            stmt.setBoolean( 2, lineplacement.repeat );
            stmt.setDouble( 3, lineplacement.initialGap );
            stmt.setDouble( 4, lineplacement.gap );
            stmt.setBoolean( 5, lineplacement.isAligned );
            stmt.setBoolean( 6, lineplacement.generalizeLine );

            rs = stmt.executeQuery();
            if ( rs.next() ) {
                return rs.getInt( 1 );
            }
            return -1;
        } finally {
            if ( rs != null ) {
                try {
                    rs.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }

    private int write( Connection conn, Halo halo )
                            throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "insert into halos (fill_id, radius) values (?, ?) returning id" );

            if ( halo.fill == null ) {
                stmt.setNull( 1, INTEGER );
            } else {
                stmt.setInt( 1, write( conn, halo.fill ) );
            }
            stmt.setDouble( 2, halo.radius );

            rs = stmt.executeQuery();
            if ( rs.next() ) {
                return rs.getInt( 1 );
            }
            return -1;
        } finally {
            if ( rs != null ) {
                try {
                    rs.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }

    private int write( Connection conn, PointStyling styling )
                            throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "insert into points (uom, graphic_id) values (?, ?) returning id" );

            stmt.setString( 1, styling.uom.toString() );
            if ( styling.graphic != null ) {
                stmt.setInt( 2, write( conn, styling.graphic ) );
            } else {
                stmt.setNull( 2, INTEGER );
            }

            rs = stmt.executeQuery();
            if ( rs.next() ) {
                return rs.getInt( 1 );
            }
            return -1;
        } finally {
            if ( rs != null ) {
                try {
                    rs.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }

    private int write( Connection conn, LineStyling styling )
                            throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "insert into lines (uom, stroke_id, perpendicularoffset) values (?, ?, ?) returning id" );

            stmt.setString( 1, styling.uom.toString() );
            if ( styling.stroke != null ) {
                stmt.setInt( 2, write( conn, styling.stroke ) );
            } else {
                stmt.setNull( 2, INTEGER );
            }
            stmt.setDouble( 3, styling.perpendicularOffset );

            rs = stmt.executeQuery();
            if ( rs.next() ) {
                return rs.getInt( 1 );
            }
            return -1;
        } finally {
            if ( rs != null ) {
                try {
                    rs.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }

    private int write( Connection conn, PolygonStyling styling )
                            throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "insert into polygons (uom, fill_id, stroke_id, displacementx, displacementy, perpendicularoffset) values (?, ?, ?, ?, ?, ?) returning id" );

            stmt.setString( 1, styling.uom.toString() );
            if ( styling.fill != null ) {
                stmt.setInt( 2, write( conn, styling.fill ) );
            } else {
                stmt.setNull( 2, INTEGER );
            }
            if ( styling.stroke != null ) {
                stmt.setInt( 3, write( conn, styling.stroke ) );
            } else {
                stmt.setNull( 3, INTEGER );
            }
            stmt.setDouble( 4, styling.displacementX );
            stmt.setDouble( 5, styling.displacementY );
            stmt.setDouble( 6, styling.perpendicularOffset );

            rs = stmt.executeQuery();
            if ( rs.next() ) {
                return rs.getInt( 1 );
            }
            return -1;
        } finally {
            if ( rs != null ) {
                try {
                    rs.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }

    private int write( Connection conn, TextStyling styling, String labelexpr )
                            throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement( "insert into texts (labelexpr, uom, font_id, fill_id, rotation, displacementx, displacementy, anchorx, anchory, lineplacement_id, halo_id) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) returning id" );

            stmt.setString( 1, labelexpr );
            stmt.setString( 2, styling.uom.toString() );
            if ( styling.font == null ) {
                stmt.setNull( 3, INTEGER );
            } else {
                stmt.setInt( 3, write( conn, styling.font ) );
            }
            if ( styling.fill == null ) {
                stmt.setNull( 4, INTEGER );
            } else {
                stmt.setInt( 4, write( conn, styling.fill ) );
            }
            stmt.setDouble( 5, styling.rotation );
            stmt.setDouble( 6, styling.displacementX );
            stmt.setDouble( 7, styling.displacementY );
            stmt.setDouble( 8, styling.anchorPointX );
            stmt.setDouble( 9, styling.anchorPointY );
            if ( styling.linePlacement == null ) {
                stmt.setNull( 10, INTEGER );
            } else {
                stmt.setInt( 10, write( conn, styling.linePlacement ) );
            }
            if ( styling.halo == null ) {
                stmt.setNull( 11, INTEGER );
            } else {
                stmt.setInt( 11, write( conn, styling.halo ) );
            }

            rs = stmt.executeQuery();
            if ( rs.next() ) {
                return rs.getInt( 1 );
            }
            return -1;
        } finally {
            if ( rs != null ) {
                try {
                    rs.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }

    private void write( Styling styling, DoublePair scales, String name, String labelexpr ) {
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = getConnection( connId );
            conn.setAutoCommit( false );
            stmt = conn.prepareStatement( "insert into styles (type, fk, minscale, maxscale, name) values (?, ?, ?, ?, ?)" );
            if ( styling instanceof PointStyling ) {
                stmt.setString( 1, "POINT" );
                stmt.setInt( 2, write( conn, (PointStyling) styling ) );
            } else if ( styling instanceof LineStyling ) {
                stmt.setString( 1, "LINE" );
                stmt.setInt( 2, write( conn, (LineStyling) styling ) );
            } else if ( styling instanceof PolygonStyling ) {
                stmt.setString( 1, "POLYGON" );
                stmt.setInt( 2, write( conn, (PolygonStyling) styling ) );
            } else if ( styling instanceof TextStyling ) {
                stmt.setString( 1, "TEXT" );
                stmt.setInt( 2, write( conn, (TextStyling) styling, labelexpr ) );
            }

            if ( scales != null ) {
                if ( scales.first != NEGATIVE_INFINITY ) {
                    stmt.setDouble( 3, scales.first );
                } else {
                    stmt.setNull( 3, DOUBLE );
                }
                if ( scales.second != POSITIVE_INFINITY ) {
                    stmt.setDouble( 4, scales.second );
                } else {
                    stmt.setNull( 4, DOUBLE );
                }
            }

            if ( name == null ) {
                stmt.setNull( 5, VARCHAR );
            } else {
                stmt.setString( 5, name );
            }

            stmt.executeUpdate();
            conn.commit();
        } catch ( SQLException e ) {
            LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } finally {
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( conn != null ) {
                try {
                    conn.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }

    /**
     * @param style
     * @param name
     */
    public void write( Style style, String name ) {
        for ( Triple<LinkedList<Styling>, DoublePair, LinkedList<String>> p : style.getBasesWithScales() ) {
            Iterator<String> labelexprs = p.third.iterator();
            for ( Styling s : p.first ) {
                write( s, p.second, name == null ? style.getName() : name, labelexprs.hasNext() ? labelexprs.next()
                                                                                               : null );
            }
        }
    }

    /**
     * Writes a style as SLD/SE 'blob'.
     * 
     * @param in
     * @param name
     * @throws IOException
     */
    public void write( InputStream in, String name )
                            throws IOException {
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            conn = getConnection( connId );
            conn.setAutoCommit( false );
            stmt = conn.prepareStatement( "insert into styles (sld, name) values (?, ?)" );

            StringBuilder sb = new StringBuilder();
            String s = null;
            BufferedReader bin = new BufferedReader( new InputStreamReader( in, "UTF-8" ) );
            while ( ( s = bin.readLine() ) != null ) {
                sb.append( s ).append( "\n" );
            }
            in.close();
            stmt.setString( 1, sb.toString() );
            if ( name == null ) {
                stmt.setNull( 2, VARCHAR );
            } else {
                stmt.setString( 2, name );
            }

            stmt.executeUpdate();
            conn.commit();
        } catch ( SQLException e ) {
            LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } finally {
            if ( stmt != null ) {
                try {
                    stmt.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
            if ( conn != null ) {
                try {
                    conn.close();
                } catch ( SQLException e ) {
                    LOG.info( "Unable to write style to DB: '{}'.", e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }

    /**
     * Simple importer for SE files, with hardcoded 'configtool' on localhost.
     * 
     * @param args
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    public static void main( String[] args )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Style style = new SymbologyParser( true ).parse( XMLInputFactory.newInstance().createXMLStreamReader(
                                                                                                              new FileInputStream(
                                                                                                                                   args[0] ) ) );
        ConnectionManager.addConnection( "configtool", "jdbc:postgresql://localhost/configtool", "postgres", "", 5, 20 );
        if ( style.isSimple() ) {
            new PostgreSQLWriter( "configtool" ).write( style, null );
        } else {
            new PostgreSQLWriter( "configtool" ).write( new FileInputStream( args[0] ), style.getName() );
        }
    }

}
