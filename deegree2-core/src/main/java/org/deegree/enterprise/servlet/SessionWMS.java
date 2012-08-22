/*----------------------------------------------------------------------------
 This file is part of BfS WPS WebApplication project
 
 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 ----------------------------------------------------------------------------*/
package org.deegree.enterprise.servlet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.GeometryUtils;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.KVP2Map;
import org.deegree.framework.util.StringTools;
import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.graphics.transformation.WorldToScreenTransform;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.MultiPrimitive;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.SurfacePatch;
import org.deegree.ogcwebservices.wms.operation.GetLegendGraphic;
import org.deegree.ogcwebservices.wms.operation.GetMap;

/**
 * This servlet realizes a OGC:WMS that just offers one layer ('highlight') reading a geometry form session of
 * requesting user. The geometry to be painted must be stored in a session attribute named 'TEMP_WMS_GEOMETRY'.
 * Following init parameters ars upported:
 * <ul>
 * <li>FILL: fill color for polygons (optional)
 * <li>STROKE: stroke color for lines (optional)
 * <li>PATTERN: fill pattern/image for polygon (if available FILL will be ignored) (optional)
 * <li>SYMBOL: symbol image for points (optional)
 * </ul>
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class SessionWMS extends HttpServlet {

    private static final long serialVersionUID = 7263960291096038482L;

    private static final ILogger LOG = LoggerFactory.getLogger( SessionWMS.class );

    private static Color fillColor = new Color( 1f, 1f, 1f, 0.5f );

    private static Color lineColor = Color.BLACK;
    
    private static float polyStrokeWidth = 1;
    
    private static float strokeStrokeWidth = 1;

    private static BufferedImage fillPattern;

    private static BufferedImage symbol;

    private static String capa;

    @Override
    public void init()
                            throws ServletException {
        // TODO Auto-generated method stub
        super.init();
        if ( getInitParameter( "FILL" ) != null ) {
            fillColor = Color.decode( getInitParameter( "FILL" ) );
        }
        if ( getInitParameter( "STROKE" ) != null ) {
            lineColor = Color.decode( getInitParameter( "STROKE" ) );
        }
        if ( getInitParameter( "STROKEWIDTH" ) != null ) {
            strokeStrokeWidth = Float.parseFloat(  getInitParameter( "STROKEWIDTH" ) );
        }
        if ( getInitParameter( "FILLSTROKEWIDTH" ) != null ) {
            polyStrokeWidth = Float.parseFloat(  getInitParameter( "FILLSTROKEWIDTH" ) );
        }
        if ( getInitParameter( "PATTERN" ) != null ) {
            File pattern = new File( getInitParameter( "PATTERN" ) );
            if ( !pattern.isAbsolute() ) {
                String s = getServletContext().getRealPath( getInitParameter( "PATTERN" ) );
                pattern = new File( s );
            }
            try {
                fillPattern = ImageUtils.loadImage( pattern );
            } catch ( IOException e ) {
                LOG.logError( e );
                throw new ServletException( e );
            }
        }
        if ( getInitParameter( "SYMBOL" ) != null ) {
            File pattern = new File( getInitParameter( "SYMBOL" ) );
            if ( !pattern.isAbsolute() ) {
                String s = getServletContext().getRealPath( getInitParameter( "SYMBOL" ) );
                pattern = new File( s );
            }
            try {
                symbol = ImageUtils.loadImage( pattern );
            } catch ( IOException e ) {
                LOG.logError( e );
                throw new ServletException( e );
            }
        } else {
            symbol = new BufferedImage( 9, 9, BufferedImage.TYPE_INT_ARGB );
            Graphics g = symbol.getGraphics();
            g.setColor( Color.RED );
            g.fillOval( 0, 0, 9, 9 );
            g.dispose();
        }
        Properties prop = new Properties();
        InputStream is = SessionWMS.class.getResourceAsStream( "session-wms.properties" );
        try {
            prop.load( is );
            is.close();
        } catch ( IOException e ) {
            LOG.logError( e );
            throw new ServletException( e );
        }
        capa = prop.getProperty( "capabilities" );

    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
                            throws ServletException, IOException {
        Map<String, String> param = KVP2Map.toMap( req );
        param.put( "ID", "1" );
        resp.setHeader( "Cache-Control", "no-cache, no-store" );
        resp.setHeader( "Pragma", "no-cache" );

        if ( "GetCapabilities".equals( param.get( "REQUEST" ) ) ) {
            PrintWriter pw = resp.getWriter();
            String s = StringTools.replace( capa, "§URL§", req.getRequestURL().toString(), true );
            pw.write( s );
            pw.close();
        } else if ( "GetMap".equals( param.get( "REQUEST" ) ) ) {
            getMap( req, resp, param );
        } else if ( "GetLegendGraphic".equals( param.get( "REQUEST" ) ) ) {
            getLegendGraphic( req, resp, param );
        }

    }

    private void getMap( HttpServletRequest req, HttpServletResponse resp, Map<String, String> param ) {
        try {
            GetMap getMap = GetMap.create( param );
            HttpSession session = req.getSession();
            Geometry geometry = (Geometry) session.getAttribute( "TEMP_WMS_GEOMETRY" );
            if ( geometry != null ) {
                BufferedImage bi = new BufferedImage( getMap.getWidth(), getMap.getHeight(),
                                                      BufferedImage.TYPE_INT_ARGB );
                // set background to white
                Graphics g = bi.getGraphics();
                g.setColor( new Color( 0, 0, 0, 0f ) );
                g.fillRect( 0, 0, bi.getWidth(), bi.getHeight() );

                Envelope bbox = getMap.getBoundingBox();
                GeoTransform gt = new WorldToScreenTransform( bbox.getMin().getX(), bbox.getMin().getY(),
                                                              bbox.getMax().getX(), bbox.getMax().getY(), 0, 0,
                                                              getMap.getWidth() - 1, getMap.getHeight() - 1 );
                if ( geometry instanceof MultiPrimitive ) {
                    Geometry[] geometries = ( (MultiPrimitive) geometry ).getAll();
                    for ( Geometry geom : geometries ) {
                        drawGeometry( geom, (Graphics2D) g, gt );
                    }
                } else {
                    drawGeometry( geometry, (Graphics2D) g, gt );
                }

                g.dispose();
                resp.setContentType( getMap.getFormat() );
                int idx = getMap.getFormat().lastIndexOf( "/" );
                OutputStream os = resp.getOutputStream();
                ImageUtils.saveImage( bi, resp.getOutputStream(), getMap.getFormat().substring( idx + 1 ), 0.99f );
                os.flush();
                os.close();
            }
        } catch ( Exception e ) {
            LOG.logError( e );
        }
    }

    private void drawGeometry( Geometry geometry, Graphics2D g, GeoTransform gt )
                            throws GeometryException {
        if ( geometry instanceof Point ) {
            int x = (int) Math.round( gt.getDestX( ( (Point) geometry ).getX() ) );
            int y = (int) Math.round( gt.getDestY( ( (Point) geometry ).getY() ) );
            x += symbol.getWidth() / 2 + 1;
            y += symbol.getHeight() / 2 + 1;
            g.drawImage( symbol, x, y, null );
        } else if ( geometry instanceof Curve ) {
            GeneralPath path = createPath( (Curve) geometry, gt );
            g.setColor( lineColor );
            g.setStroke( new BasicStroke( strokeStrokeWidth ) );
            g.draw( path );
        } else if ( geometry instanceof Surface ) {
            GeneralPath path = createPath( (Surface) geometry, gt );
            if ( fillColor != null && fillPattern == null ) {
                g.setColor( fillColor );                
            } else {
                Rectangle anchor = new Rectangle( 0, 0, fillPattern.getWidth(), fillPattern.getHeight() );
                ( (Graphics2D) g ).setPaint( new TexturePaint( fillPattern, anchor ) );
            }
            g.fill( path );
            if ( lineColor != null ) {
                g.setStroke( new BasicStroke( polyStrokeWidth ) );
                g.setColor( lineColor );
                g.draw( path );
            }
        }
    }

    private GeneralPath createPath( Curve curve, GeoTransform gt )
                            throws GeometryException {

        GeneralPath path = new GeneralPath();

        Position[] pos = curve.getAsLineString().getPositions();
        appendPositionsToPath( path, pos, gt );

        return path;
    }

    private GeneralPath createPath( Surface surface, GeoTransform gt ) {

        GeneralPath path = new GeneralPath();

        SurfacePatch patch = null;
        try {
            patch = surface.getSurfacePatchAt( 0 );
        } catch ( GeometryException e ) {
            LOG.logError( e.getMessage(), e );
        }
        appendPositionsToPath( path, patch.getExteriorRing(), gt );

        Position[][] inner = patch.getInteriorRings();
        if ( inner != null ) {
            for ( int i = 0; i < inner.length; i++ ) {
                appendPositionsToPath( path, inner[i], gt );
            }
        }

        return path;
    }

    private void appendPositionsToPath( GeneralPath path, Position[] pos, GeoTransform gt ) {

        Position p = gt.getDestPoint( pos[0] );
        float xx = (float) p.getX();
        float yy = (float) p.getY();
        path.moveTo( xx, yy );
        for ( int i = 1; i < pos.length; i++ ) {
            p = gt.getDestPoint( pos[i] );
            float xx_ = (float) p.getX();
            float yy_ = (float) p.getY();
            if ( GeometryUtils.distance( xx, yy, xx_, yy_ ) > 5 || i == pos.length - 1 ) {
                path.lineTo( xx_, yy_ );
                xx = xx_;
                yy = yy_;
            }
        }

    }

    /**
     * 
     * @param req
     * @param resp
     * @param param
     */
    @SuppressWarnings("unchecked")
    private void getLegendGraphic( HttpServletRequest req, HttpServletResponse resp, Map<String, String> param ) {
        try {
            GetLegendGraphic getLegendGraphic = GetLegendGraphic.create( param );
            HttpSession session = req.getSession();
            Map<String, String> imageURLs = (Map<String, String>) session.getAttribute( "legendURLs" );
            BufferedImage bi = null;
            String layer = getLegendGraphic.getLayer();
            if ( imageURLs != null && imageURLs.get( layer ) != null ) {
                String s = imageURLs.get( layer );
                bi = ImageUtils.loadImage( new URL( s ) );
                resp.setContentType( getLegendGraphic.getFormat() );
                int idx = getLegendGraphic.getFormat().lastIndexOf( "/" );
                ImageUtils.saveImage( bi, resp.getOutputStream(), getLegendGraphic.getFormat().substring( idx + 1 ),
                                      0.99f );
            } else {
                if ( "image/png".equals( getLegendGraphic.getFormat() )
                     || "image/gif".equals( getLegendGraphic.getFormat() ) ) {
                    bi = new BufferedImage( 100, 100, BufferedImage.TYPE_INT_ARGB );
                } else {
                    bi = new BufferedImage( 100, 100, BufferedImage.TYPE_INT_RGB );
                }
                // set background to white and draw error message
                Graphics g = bi.getGraphics();
                g.setColor( Color.WHITE );
                g.fillRect( 0, 0, bi.getWidth(), bi.getHeight() );
                g.setColor( Color.RED );
                g.drawString( "no legend symbol", 10, 10 );
                g.drawString( "available for layer:", 10, 40 );
                g.drawString( layer, 10, 70 );
                g.dispose();
            }
            resp.setContentType( getLegendGraphic.getFormat() );
            int idx = getLegendGraphic.getFormat().lastIndexOf( "/" );
            OutputStream os = resp.getOutputStream();
            ImageUtils.saveImage( bi, os, getLegendGraphic.getFormat().substring( idx + 1 ), 0.99f );
            os.flush();
            os.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost( HttpServletRequest req, HttpServletResponse resp )
                            throws ServletException, IOException {
        super.doGet( req, resp );
    }

}
