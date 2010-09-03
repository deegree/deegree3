//$HeadURL$
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

package org.deegree.rendering.r2d;

import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.CAP_SQUARE;
import static java.awt.BasicStroke.JOIN_BEVEL;
import static java.awt.BasicStroke.JOIN_MITER;
import static java.awt.BasicStroke.JOIN_ROUND;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.deegree.commons.utils.math.MathUtils.isZero;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.cs.CRS.EPSG_4326;
import static org.deegree.cs.components.Unit.METRE;
import static org.deegree.geometry.utils.GeometryUtils.envelopeToPolygon;
import static org.deegree.rendering.r2d.RenderHelper.getShapeFromMark;
import static org.deegree.rendering.r2d.RenderHelper.getShapeFromSvg;
import static org.deegree.rendering.r2d.RenderHelper.renderMark;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Path2D.Double;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.linearization.CurveLinearizer;
import org.deegree.geometry.linearization.NumPointsCriterion;
import org.deegree.geometry.linearization.SurfaceLinearizer;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.gml.geometry.refs.GeometryReference;
import org.deegree.rendering.r2d.strokes.OffsetStroke;
import org.deegree.rendering.r2d.strokes.ShapeStroke;
import org.deegree.rendering.r2d.styling.LineStyling;
import org.deegree.rendering.r2d.styling.PointStyling;
import org.deegree.rendering.r2d.styling.PolygonStyling;
import org.deegree.rendering.r2d.styling.Styling;
import org.deegree.rendering.r2d.styling.components.Fill;
import org.deegree.rendering.r2d.styling.components.Graphic;
import org.deegree.rendering.r2d.styling.components.PerpendicularOffsetType;
import org.deegree.rendering.r2d.styling.components.Stroke;
import org.deegree.rendering.r2d.styling.components.UOM;
import org.slf4j.Logger;

/**
 * <code>Java2DRenderer</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(warn = "log info about problems with the renderer setup, or broken geometries coming in, or problematic usage of the renderer", debug = "log what's funny about rendering, eg. when null geometries are rendered, general info about the renderer, also log stack traces, use for debugging/improving your styles", trace = "log geometries and styles being rendered, only use for debugging the code")
public class Java2DRenderer implements Renderer {

    private static final Logger LOG = getLogger( Java2DRenderer.class );

    Graphics2D graphics;

    AffineTransform worldToScreen = new AffineTransform();

    private GeometryTransformer transformer;

    private double pixelSize = 0.28;

    private double res;

    private static final CurveLinearizer curveLinearizer = new CurveLinearizer( new GeometryFactory() );

    private static final SurfaceLinearizer surfaceLinearizer = new SurfaceLinearizer( new GeometryFactory() );

    /**
     * @param graphics
     * @param width
     * @param height
     * @param bbox
     * @param pixelSize
     *            in mm
     */
    public Java2DRenderer( Graphics2D graphics, int width, int height, Envelope bbox, double pixelSize ) {
        this( graphics, width, height, bbox );
        this.pixelSize = pixelSize;
    }

    /**
     * @param graphics
     * @param width
     * @param height
     * @param bbox
     */
    public Java2DRenderer( Graphics2D graphics, int width, int height, Envelope bbox ) {
        this.graphics = graphics;

        if ( bbox != null ) {
            double scalex = width / bbox.getSpan0();
            double scaley = height / bbox.getSpan1();
            try {
                if ( bbox.getCoordinateSystem() == null
                     || bbox.getCoordinateSystem().getWrappedCRS().getUnits()[0].equals( METRE ) ) {
                    res = bbox.getSpan0() / width; // use x for resolution
                } else {
                    // heuristics more or less copied from d2, TODO is use the proper UTM conversion
                    Envelope box = new GeometryTransformer( EPSG_4326.getWrappedCRS() ).transform( bbox );
                    double minx = box.getMin().get0(), miny = box.getMin().get1();
                    double maxx = minx + box.getSpan0();
                    double r = 6378.137;
                    double rad = PI / 180d;
                    double cose = sin( rad * minx ) * sin( rad * maxx ) + cos( rad * minx ) * cos( rad * maxx );
                    double dist = r * acos( cose ) * cos( rad * miny );
                    res = abs( dist * 1000 / width );
                }
            } catch ( UnknownCRSException e ) {
                LOG.warn( "Could not determine CRS of bbox, assuming it's in meter..." );
                LOG.debug( "Stack trace:", e );
                res = bbox.getSpan0() / width; // use x for resolution
            } catch ( IllegalArgumentException e ) {
                LOG.warn( "Could not transform bbox, assuming it's in meter..." );
                LOG.debug( "Stack trace:", e );
                res = bbox.getSpan0() / width; // use x for resolution
            } catch ( TransformationException e ) {
                LOG.warn( "Could not transform bbox, assuming it's in meter..." );
                LOG.debug( "Stack trace:", e );
                res = bbox.getSpan0() / width; // use x for resolution
            }

            // we have to flip horizontally, so invert y scale and add the screen height
            worldToScreen.translate( -bbox.getMin().get0() * scalex, bbox.getMin().get1() * scaley + height );
            worldToScreen.scale( scalex, -scaley );

            try {
                if ( bbox.getCoordinateSystem() != null ) {
                    transformer = new GeometryTransformer( bbox.getCoordinateSystem().getWrappedCRS() );
                }
            } catch ( IllegalArgumentException e ) {
                LOG.debug( "Stack trace:", e );
                LOG.warn( "Setting up the renderer yielded an exception when setting up internal transformer. This may lead to problems." );
            } catch ( UnknownCRSException e ) {
                LOG.debug( "Stack trace:", e );
                LOG.warn( "Setting up the renderer yielded an exception when setting up internal transformer. This may lead to problems." );
            }

            LOG.debug( "For coordinate transformations, scaling by x = {} and y = {}", scalex, -scaley );
            LOG.trace( "Final transformation was {}", worldToScreen );
        } else {
            LOG.warn( "No envelope given, proceeding with a scale of 1." );
        }
    }

    /**
     * @param graphics
     */
    public Java2DRenderer( Graphics2D graphics ) {
        this.graphics = graphics;
        res = 1;
    }

    private Rectangle2D.Double getGraphicBounds( Graphic graphic, double x, double y, UOM uom ) {
        double width = considerUOM( graphic.size, uom );
        double height = considerUOM( graphic.size, uom );

        if ( width < 0 ) {
            if ( graphic.image == null ) {
                width = 6;
                height = 6;
            } else {
                width = graphic.image.getWidth();
                height = graphic.image.getHeight();
            }
        }

        double x0 = x - width * graphic.anchorPointX + considerUOM( graphic.displacementX, uom );
        double y0 = y - height * graphic.anchorPointY + considerUOM( graphic.displacementY, uom );

        return new Rectangle2D.Double( x0, y0, width, height );
    }

    void applyGraphicFill( Graphic graphic, UOM uom ) {
        BufferedImage img;

        if ( graphic.image == null ) {
            int size = round( considerUOM( graphic.size, uom ) );
            img = new BufferedImage( size, size, TYPE_INT_ARGB );
            Graphics2D g = img.createGraphics();
            Java2DRenderer renderer = new Java2DRenderer( g );
            renderMark( graphic.mark, graphic.size < 0 ? 6 : size, uom, renderer, 0, 0, graphic.rotation );
            g.dispose();
        } else {
            img = graphic.image;
        }

        graphics.setPaint( new TexturePaint( img, getGraphicBounds( graphic, 0, 0, uom ) ) );
    }

    void applyFill( Fill fill, UOM uom ) {
        if ( fill == null ) {
            graphics.setPaint( new Color( 0, 0, 0, 0 ) );
            return;
        }

        if ( fill.graphic == null ) {
            graphics.setPaint( fill.color );
        } else {
            applyGraphicFill( fill.graphic, uom );
        }
    }

    void applyStroke( Stroke stroke, UOM uom, Shape object, double perpendicularOffset, PerpendicularOffsetType type ) {
        if ( stroke == null || isZero( stroke.width ) ) {
            graphics.setPaint( new Color( 0, 0, 0, 0 ) );
            return;
        }
        if ( stroke.fill == null ) {
            graphics.setPaint( stroke.color );
        } else {
            applyGraphicFill( stroke.fill, uom );
        }
        if ( stroke.stroke != null ) {
            if ( stroke.stroke.image == null && stroke.stroke.imageURL != null ) {
                Shape shape = getShapeFromSvg( stroke.stroke.imageURL, considerUOM( stroke.stroke.size, uom ),
                                               stroke.stroke.rotation );
                graphics.setStroke( new ShapeStroke( shape, considerUOM( stroke.strokeGap + stroke.stroke.size, uom ),
                                                     stroke.positionPercentage ) );
            } else if ( stroke.stroke.mark != null ) {
                double poff = considerUOM( perpendicularOffset, uom );
                Shape transed = object;
                if ( !isZero( poff ) ) {
                    transed = new OffsetStroke( poff, null, type ).createStrokedShape( transed );
                }
                Shape shape = getShapeFromMark( stroke.stroke.mark,
                                                stroke.stroke.size <= 0 ? 6 : considerUOM( stroke.stroke.size, uom ),
                                                stroke.stroke.rotation );
                ShapeStroke s = new ShapeStroke( shape, considerUOM( stroke.strokeGap + stroke.stroke.size, uom ),
                                                 stroke.positionPercentage );
                transed = s.createStrokedShape( transed );
                if ( stroke.stroke.mark.fill != null ) {
                    applyFill( stroke.stroke.mark.fill, uom );
                    graphics.fill( transed );
                }
                if ( stroke.stroke.mark.stroke != null ) {
                    applyStroke( stroke.stroke.mark.stroke, uom, transed, 0, null );
                    graphics.draw( transed );
                }
                return;
            } else {
                LOG.warn( "Rendering of raster images along lines is not supported yet." );
            }
        } else {
            int linecap = CAP_SQUARE;
            if ( stroke.linecap != null ) {
                switch ( stroke.linecap ) {
                case BUTT:
                    linecap = CAP_BUTT;
                    break;
                case ROUND:
                    linecap = CAP_ROUND;
                    break;
                case SQUARE:
                    linecap = CAP_SQUARE;
                    break;
                }
            }
            int linejoin = JOIN_MITER;
            float miterLimit = 10;
            if ( stroke.linejoin != null ) {
                switch ( stroke.linejoin ) {
                case BEVEL:
                    linejoin = JOIN_BEVEL;
                    break;
                case MITRE:
                    linejoin = JOIN_MITER;
                    break;
                case ROUND:
                    linejoin = JOIN_ROUND;
                    break;
                }
            }
            float dashoffset = (float) considerUOM( stroke.dashoffset, uom );
            float[] dasharray = stroke.dasharray == null ? null : new float[stroke.dasharray.length];
            if ( stroke.dasharray != null ) {
                for ( int i = 0; i < stroke.dasharray.length; ++i ) {
                    dasharray[i] = (float) considerUOM( stroke.dasharray[i], uom );
                }
            }

            BasicStroke bs = new BasicStroke( (float) considerUOM( stroke.width, uom ), linecap, linejoin, miterLimit,
                                              dasharray, dashoffset );
            double poff = considerUOM( perpendicularOffset, uom );
            if ( !isZero( poff ) ) {
                graphics.setStroke( new OffsetStroke( poff, bs, type ) );
            } else {
                graphics.setStroke( bs );
            }
        }

        graphics.draw( object );
    }

    <T extends Geometry> T transform( T g ) {
        if ( g == null ) {
            LOG.warn( "Trying to transform null geometry." );
            return null;
        }
        if ( g.getCoordinateSystem() == null ) {
            LOG.warn( "Geometry of type '{}' had null coordinate system.", g.getClass().getSimpleName() );
            return g;
        }
        if ( transformer != null ) {
            CRS crs = null;
            try {
                // TODO minimize transformations in all other cases as well
                crs = ( (Geometry) g ).getCoordinateSystem();
                if ( transformer.getWrappedTargetCRS().equals( crs ) ) {
                    return g;
                }
                T g2 = transformer.transform( g );
                if ( g2 == null ) {
                    LOG.warn( "Geometry transformer returned null for geometry of type {}, crs was {}.",
                              g.getClass().getSimpleName(), crs );
                    return g;
                }
                return g2;
            } catch ( IllegalArgumentException e ) {
                if ( g instanceof Surface ) {
                    @SuppressWarnings("unchecked")
                    T g2 = (T) transform( surfaceLinearizer.linearize( (Surface) g, new NumPointsCriterion( 100 ) ) );
                    g2.setCoordinateSystem( g.getCoordinateSystem() );
                    return g2;
                }
                if ( g instanceof Curve ) {
                    @SuppressWarnings("unchecked")
                    T g2 = (T) transform( curveLinearizer.linearize( (Curve) g, new NumPointsCriterion( 100 ) ) );
                    g2.setCoordinateSystem( g.getCoordinateSystem() );
                    return g2;
                }
                LOG.debug( "Stack trace:", e );
                LOG.warn( "Could not transform geometry of type '{}' before rendering, "
                          + "this may lead to problems. CRS was {}.", g.getClass().getSimpleName(), crs );
            } catch ( TransformationException e ) {
                LOG.debug( "Stack trace:", e );
                LOG.warn( "Could not transform geometry of type '{}' before rendering, "
                          + "this may lead to problems. CRS was {}.", g.getClass().getSimpleName(), crs );
            } catch ( UnknownCRSException e ) {
                LOG.debug( "Stack trace:", e );
                LOG.warn( "Could not transform geometry of type '{}' before rendering, "
                          + "this may lead to problems. CRS was {}.", g.getClass().getSimpleName(), crs );
            }
        }
        return g;
    }

    private void render( PointStyling styling, double x, double y ) {
        Point2D.Double p = (Point2D.Double) worldToScreen.transform( new Point2D.Double( x, y ), null );
        x = p.x;
        y = p.y;

        Graphic g = styling.graphic;
        Rectangle2D.Double rect = getGraphicBounds( g, x, y, styling.uom );

        if ( g.image == null ) {
            renderMark( g.mark, g.size < 0 ? 6 : round( considerUOM( g.size, styling.uom ) ), styling.uom, this,
                        rect.getMinX(), rect.getMinY(), g.rotation );
            return;
        }

        if ( g.image != null ) {
            graphics.drawImage( g.image, round( rect.x ), round( rect.y ), round( rect.width ), round( rect.height ),
                                null );
        }
    }

    public void render( PointStyling styling, Geometry geom ) {
        if ( geom == null ) {
            LOG.debug( "Trying to render null geometry." );
            return;
        }

        if ( LOG.isTraceEnabled() ) {
            LOG.trace( "Drawing " + geom + " with " + styling );
        }

        if ( geom instanceof Point ) {
            geom = transform( geom );
            render( styling, ( (Point) geom ).get0(), ( (Point) geom ).get1() );
        }
        // TODO properly convert'em
        if ( geom instanceof Surface ) {
            Surface surface = (Surface) geom;
            for ( SurfacePatch patch : surface.getPatches() ) {
                if ( patch instanceof PolygonPatch ) {
                    PolygonPatch polygonPatch = (PolygonPatch) patch;
                    for ( Curve curve : polygonPatch.getBoundaryRings() ) {
                        curve.setCoordinateSystem( surface.getCoordinateSystem() );
                        render( styling, curve );
                    }
                } else {
                    throw new IllegalArgumentException( "Cannot render non-planar surfaces." );
                }
            }
        }
        if ( geom instanceof Curve ) {
            Curve curve = (Curve) geom;
            if ( curve.getCurveSegments().size() != 1
                 || !( curve.getCurveSegments().get( 0 ) instanceof LineStringSegment ) ) {
                // TODO handle non-linear and multiple curve segments
                throw new IllegalArgumentException();
            }
            LineStringSegment segment = ( (LineStringSegment) curve.getCurveSegments().get( 0 ) );
            // coordinate representation is still subject to change...
            for ( Point point : segment.getControlPoints() ) {
                point.setCoordinateSystem( curve.getCoordinateSystem() );
                render( styling, point );
            }
        }
        if ( geom instanceof MultiGeometry<?> ) {
            LOG.trace( "Breaking open multi geometry." );
            MultiGeometry<?> mc = (MultiGeometry<?>) geom;
            for ( Geometry g : mc ) {
                render( styling, g );
            }
        }
    }

    Double fromCurve( Curve curve ) {
        Double line = new Double();

        // TODO use error criterion
        CRS crs = curve.getCoordinateSystem();
        curve = curveLinearizer.linearize( curve, new NumPointsCriterion( 100 ) );
        curve.setCoordinateSystem( crs );
        Points points = curve.getControlPoints();
        Iterator<Point> iter = points.iterator();
        Point p = iter.next();
        double x = p.get0(), y = p.get1();
        line.moveTo( x, y );
        while ( iter.hasNext() ) {
            p = iter.next();
            if ( iter.hasNext() ) {
                line.lineTo( p.get0(), p.get1() );
            } else {
                if ( isZero( x - p.get0() ) && isZero( y - p.get1() ) ) {
                    line.closePath();
                } else {
                    line.lineTo( p.get0(), p.get1() );
                }
            }
        }

        line.transform( worldToScreen );

        return line;
    }

    public void render( LineStyling styling, Geometry geom ) {
        if ( geom == null ) {
            LOG.debug( "Trying to render null geometry." );
            return;
        }

        LOG.trace( "Drawing {} with {}", geom, styling );

        if ( geom instanceof Point ) {
            LOG.warn( "Trying to render point with line styling." );
        }
        if ( geom instanceof Curve ) {
            geom = transform( geom );

            Double line = fromCurve( (Curve) geom );
            applyStroke( styling.stroke, styling.uom, line, styling.perpendicularOffset,
                         styling.perpendicularOffsetType );
        }
        if ( geom instanceof Surface ) {
            Surface surface = (Surface) geom;
            for ( SurfacePatch patch : surface.getPatches() ) {
                if ( patch instanceof PolygonPatch ) {
                    PolygonPatch polygonPatch = (PolygonPatch) patch;
                    for ( Curve curve : polygonPatch.getBoundaryRings() ) {
                        if ( curve.getCoordinateSystem() == null ) {
                            curve.setCoordinateSystem( surface.getCoordinateSystem() );
                        }
                        render( styling, curve );
                    }
                } else {
                    throw new IllegalArgumentException( "Cannot render non-planar surfaces." );
                }
            }
        }
        if ( geom instanceof MultiGeometry<?> ) {
            LOG.trace( "Breaking open multi geometry." );
            MultiGeometry<?> mc = (MultiGeometry<?>) geom;
            for ( Geometry g : mc ) {
                render( styling, g );
            }
        }
    }

    private void render( PolygonStyling styling, Surface surface ) {
        for ( SurfacePatch patch : surface.getPatches() ) {
            if ( patch instanceof PolygonPatch ) {
                LinkedList<Double> lines = new LinkedList<Double>();
                PolygonPatch polygonPatch = (PolygonPatch) patch;

                // just appending the holes appears to work, the Java2D rendering mechanism can determine that they lie
                // inside and thus no substraction etc. is needed. This speeds up things SIGNIFICANTLY
                GeneralPath polygon = new GeneralPath();
                for ( Curve curve : polygonPatch.getBoundaryRings() ) {
                    Double d = fromCurve( curve );
                    lines.add( d );
                    polygon.append( d, false );
                }

                applyFill( styling.fill, styling.uom );
                graphics.fill( polygon );
                for ( Double d : lines ) {
                    applyStroke( styling.stroke, styling.uom, d, styling.perpendicularOffset,
                                 styling.perpendicularOffsetType );
                }
            } else {
                throw new IllegalArgumentException( "Cannot render non-planar surfaces." );
            }
        }
    }

    public void render( PolygonStyling styling, Geometry geom ) {
        if ( geom == null ) {
            LOG.debug( "Trying to render null geometry." );
            return;
        }

        if ( geom instanceof Point ) {
            LOG.warn( "Trying to render point with polygon styling." );
        }
        if ( geom instanceof Curve ) {
            LOG.warn( "Trying to render line with polygon styling." );
        }
        if ( geom instanceof Envelope ) {
            geom = envelopeToPolygon( (Envelope) geom );
        }
        if ( geom instanceof Surface ) {
            LOG.trace( "Drawing {} with {}", geom, styling );
            geom = transform( geom );
            render( styling, (Surface) geom );
        }
        if ( geom instanceof MultiGeometry<?> ) {
            LOG.trace( "Breaking open multi geometry." );
            for ( Geometry g : (MultiGeometry<?>) geom ) {
                render( styling, g );
            }
        }
    }

    public void render( Styling styling, Geometry geom ) {
        if ( geom instanceof GeometryReference<?> ) {
            render( styling, ( (GeometryReference<?>) geom ).getReferencedObject() );
        }
        if ( styling instanceof PointStyling ) {
            render( (PointStyling) styling, geom );
        }
        if ( styling instanceof LineStyling ) {
            render( (LineStyling) styling, geom );
        }
        if ( styling instanceof PolygonStyling ) {
            render( (PolygonStyling) styling, geom );
        }
    }

    public void render( Styling styling, Collection<Geometry> geoms ) {
        for ( Geometry geom : geoms ) {
            if ( geom instanceof GeometryReference<?> ) {
                render( styling, ( (GeometryReference<?>) geom ).getReferencedObject() );
            }
            if ( styling instanceof PointStyling ) {
                render( (PointStyling) styling, geom );
            }
            if ( styling instanceof LineStyling ) {
                render( (LineStyling) styling, geom );
            }
            if ( styling instanceof PolygonStyling ) {
                render( (PolygonStyling) styling, geom );
            }
        }
    }

    final double considerUOM( final double in, final UOM uom ) {
        switch ( uom ) {
        case Pixel:
            return in;
        case Foot:
            // TODO properly convert the res to foot
            return in / res * ( 0.28 / pixelSize );
        case Metre:
            return in / res * ( 0.28 / pixelSize );
        case mm:
            return in / pixelSize;
        }
        return in;
    }

}
