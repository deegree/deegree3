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

import static com.vividsolutions.jts.algorithm.CGAlgorithms.isCCW;
import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.CAP_SQUARE;
import static java.awt.BasicStroke.JOIN_BEVEL;
import static java.awt.BasicStroke.JOIN_MITER;
import static java.awt.BasicStroke.JOIN_ROUND;
import static java.awt.geom.Path2D.WIND_EVEN_ODD;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import static javax.media.jai.JAI.create;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_HEIGHT;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_WIDTH;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.deegree.commons.utils.math.MathUtils.isZero;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.cs.components.Unit.METRE;
import static org.deegree.geometry.primitive.GeometricPrimitive.PrimitiveType.Surface;
import static org.deegree.geometry.primitive.Ring.RingType.LinearRing;
import static org.deegree.geometry.primitive.Surface.SurfaceType.Polygon;
import static org.deegree.geometry.utils.GeometryUtils.envelopeToPolygon;
import static org.deegree.geometry.validation.GeometryFixer.invertOrientation;
import static org.deegree.rendering.r2d.RenderHelper.renderMark;
import static org.deegree.style.utils.ShapeHelper.getShapeFromMark;
import static org.deegree.style.utils.ShapeHelper.getShapeFromSvg;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D.Double;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.media.jai.RenderedOp;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.utils.ComparablePair;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.CRSUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometries;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.linearization.GeometryLinearizer;
import org.deegree.geometry.linearization.NumPointsCriterion;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.refs.GeometryReference;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.multi.DefaultMultiGeometry;
import org.deegree.geometry.standard.multi.DefaultMultiPolygon;
import org.deegree.geometry.standard.multi.DefaultMultiSurface;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.geometry.standard.primitive.DefaultPolygon;
import org.deegree.rendering.r2d.strokes.OffsetStroke;
import org.deegree.rendering.r2d.strokes.ShapeStroke;
import org.deegree.style.styling.LineStyling;
import org.deegree.style.styling.PointStyling;
import org.deegree.style.styling.PolygonStyling;
import org.deegree.style.styling.Styling;
import org.deegree.style.styling.components.Fill;
import org.deegree.style.styling.components.Graphic;
import org.deegree.style.styling.components.PerpendicularOffsetType;
import org.deegree.style.styling.components.Stroke;
import org.deegree.style.styling.components.UOM;
import org.slf4j.Logger;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * <code>Java2DRenderer</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(warn = "log info about problems with the renderer setup, or broken geometries coming in, or problematic usage of the renderer", debug = "log what's funny about rendering, eg. when null geometries are rendered, general info about the renderer, also log stack traces, use for debugging/improving your styles")
public class Java2DRenderer implements Renderer {

    private static final Logger LOG = getLogger( Java2DRenderer.class );

    Graphics2D graphics;

    AffineTransform worldToScreen = new AffineTransform();

    private GeometryTransformer transformer;

    private double pixelSize = 0.28;

    private double res;

    private Polygon clippingArea;

    private int width;

    private static final GeometryLinearizer linearizer = new GeometryLinearizer();

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
        this.width = width;

        if ( bbox != null ) {
            Pair<Envelope, DoublePair> p = RenderHelper.getWorldToScreenTransform( worldToScreen, bbox, width, height );
            double scalex = p.second.first;
            double scaley = p.second.second;
            bbox = p.first;
            try {
                if ( bbox.getCoordinateSystem() == null || bbox.getCoordinateSystem().getAlias().equals( "CRS:1" )
                     || bbox.getCoordinateSystem().getUnits()[0].equals( METRE ) ) {
                    res = bbox.getSpan0() / width; // use x for resolution
                } else {
                    // heuristics more or less copied from d2, TODO is use the proper UTM conversion
                    Envelope box = new GeometryTransformer( CRSUtils.EPSG_4326 ).transform( bbox );
                    double minx = box.getMin().get0(), miny = box.getMin().get1();
                    double maxx = minx + box.getSpan0();
                    double r = 6378.137;
                    double rad = PI / 180d;
                    double cose = sin( rad * minx ) * sin( rad * maxx ) + cos( rad * minx ) * cos( rad * maxx );
                    double dist = r * acos( cose ) * cos( rad * miny );
                    res = abs( dist * 1000 / width );
                }
            } catch ( ReferenceResolvingException e ) {
                LOG.warn( "Could not determine CRS of bbox, assuming it's in meter..." );
                LOG.debug( "Stack trace:", e );
                res = bbox.getSpan0() / width; // use x for resolution
            } catch ( UnknownCRSException e ) {
                LOG.warn( "Could not determine CRS of bbox, assuming it's in meter..." );
                LOG.debug( "Stack trace:", e );
                res = bbox.getSpan0() / width; // use x for resolution
            } catch ( Throwable e ) {
                LOG.warn( "Could not transform bbox, assuming it's in meter..." );
                LOG.debug( "Stack trace:", e );
                res = bbox.getSpan0() / width; // use x for resolution
            }

            try {
                if ( bbox.getCoordinateSystem() != null && ( !bbox.getCoordinateSystem().getAlias().equals( "CRS:1" ) ) ) {
                    transformer = new GeometryTransformer( bbox.getCoordinateSystem() );
                }
                this.clippingArea = calculateClippingArea( bbox );
            } catch ( Throwable e ) {
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

    private Polygon calculateClippingArea( Envelope bbox ) {
        double resolution = bbox.getSpan0() / width;
        double delta = resolution * 100;
        double[] minCords = new double[] { bbox.getMin().get0() - delta, bbox.getMin().get1() - delta };
        double[] maxCords = new double[] { bbox.getMax().get0() + delta, bbox.getMax().get1() + delta };
        Point min = new DefaultPoint( null, bbox.getCoordinateSystem(), null, minCords );
        Point max = new DefaultPoint( null, bbox.getCoordinateSystem(), null, maxCords );
        Envelope enlargedBBox = new DefaultEnvelope( min, max );
        return (Polygon) Geometries.getAsGeometry( enlargedBBox );
    }

    private Rectangle2D.Double getGraphicBounds( Graphic graphic, double x, double y, UOM uom ) {
        double width, height;
        if ( graphic.image != null ) {
            double max = Math.max( graphic.image.getWidth(), graphic.image.getHeight() );
            double fac = graphic.size / max;
            width = fac * graphic.image.getWidth();
            height = fac * graphic.image.getHeight();
        } else {
            width = graphic.size;
            height = graphic.size;
        }
        width = considerUOM( width, uom );
        height = considerUOM( height, uom );

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
        double y0 = y - height * graphic.anchorPointY - considerUOM( graphic.displacementY, uom );

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
                                                     stroke.positionPercentage, stroke.strokeInitialGap ) );
            } else if ( stroke.stroke.mark != null ) {
                double poff = considerUOM( perpendicularOffset, uom );
                Shape transed = object;
                if ( !isZero( poff ) ) {
                    transed = new OffsetStroke( poff, null, type ).createStrokedShape( transed );
                }
                double sz = stroke.stroke.size;
                Shape shape = getShapeFromMark( stroke.stroke.mark, sz <= 0 ? 6 : considerUOM( sz, uom ),
                                                stroke.stroke.rotation );
                if ( sz <= 0 ) {
                    sz = 6;
                }
                ShapeStroke s = new ShapeStroke( shape, considerUOM( stroke.strokeGap + sz, uom ),
                                                 stroke.positionPercentage, stroke.strokeInitialGap );
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
            if ( dasharray != null ) {
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
            ICRS crs = null;
            try {
                // TODO minimize transformations in all other cases as well
                crs = ( (Geometry) g ).getCoordinateSystem();
                if ( transformer.equals( crs ) ) {
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
                    T g2 = (T) transform( linearizer.linearize( (Surface) g, new NumPointsCriterion( 100 ) ) );
                    g2.setCoordinateSystem( g.getCoordinateSystem() );
                    return g2;
                }
                if ( g instanceof Curve ) {
                    @SuppressWarnings("unchecked")
                    T g2 = (T) transform( linearizer.linearize( (Curve) g, new NumPointsCriterion( 100 ) ) );
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

    final LinkedHashMap<ComparablePair<String, Integer>, BufferedImage> svgCache = new LinkedHashMap<ComparablePair<String, Integer>, BufferedImage>(
                                                                                                                                                      256 ) {
        private static final long serialVersionUID = -6847956873232942891L;

        @Override
        protected boolean removeEldestEntry( Map.Entry<ComparablePair<String, Integer>, BufferedImage> eldest ) {
            return size() > 256; // yeah, hardcoded max size... TODO
        }
    };

    private void render( PointStyling styling, double x, double y ) {
        Point2D.Double p = (Point2D.Double) worldToScreen.transform( new Point2D.Double( x, y ), null );
        x = p.x;
        y = p.y;

        Graphic g = styling.graphic;
        Rectangle2D.Double rect = getGraphicBounds( g, x, y, styling.uom );

        if ( g.image == null && g.imageURL == null ) {
            renderMark( g.mark, g.size < 0 ? 6 : round( considerUOM( g.size, styling.uom ) ), styling.uom, this,
                        rect.getMinX(), rect.getMinY(), g.rotation );
            return;
        }

        BufferedImage img = g.image;

        // try if it's an svg
        if ( img == null && g.imageURL != null ) {
            ComparablePair<String, Integer> cp = new ComparablePair<String, Integer>( g.imageURL, round( g.size ) );
            if ( svgCache.containsKey( cp ) ) {
                img = svgCache.get( cp );
            } else {
                PNGTranscoder t = new PNGTranscoder();

                t.addTranscodingHint( KEY_WIDTH, new Float( rect.width ) );
                t.addTranscodingHint( KEY_HEIGHT, new Float( rect.height ) );

                TranscoderInput input = new TranscoderInput( g.imageURL );

                // TODO improve performance by writing a custom transcoder output directly rendering on an image, or
                // even on
                // the target graphics
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                TranscoderOutput output = new TranscoderOutput( out );
                InputStream in = null;

                // TODO cache images
                try {
                    t.transcode( input, output );
                    out.flush();
                    in = new ByteArrayInputStream( out.toByteArray() );
                    MemoryCacheSeekableStream mcss = new MemoryCacheSeekableStream( in );
                    RenderedOp rop = create( "stream", mcss );
                    img = rop.getAsBufferedImage();
                    svgCache.put( cp, img );
                } catch ( TranscoderException e ) {
                    LOG.warn( "Could not rasterize svg '{}': {}", g.imageURL, e.getLocalizedMessage() );
                } catch ( IOException e ) {
                    LOG.warn( "Could not rasterize svg '{}': {}", g.imageURL, e.getLocalizedMessage() );
                } finally {
                    closeQuietly( out );
                    closeQuietly( in );
                }
            }

        }

        if ( img != null ) {
            AffineTransform t = graphics.getTransform();
            if ( !isZero( g.rotation ) ) {
                graphics.rotate( toRadians( g.rotation ), x, y );
            }
            graphics.drawImage( img, round( rect.x ), round( rect.y ), round( rect.width ), round( rect.height ), null );
            graphics.setTransform( t );
        }
    }

    @Override
    public void render( PointStyling styling, Geometry geom ) {
        if ( geom == null ) {
            LOG.debug( "Trying to render null geometry." );
            return;
        }

        // if ( LOG.isTraceEnabled() ) {
        // String s;
        // try {
        // s = geom.toString();
        // } catch ( UnsupportedOperationException e ) {
        // s = "(WKT representation of " + geom.getClass().getSimpleName() + " is not available)";
        // }
        // LOG.trace( "Drawing " + s + " with " + styling );
        // }

        if ( geom instanceof Point ) {
            geom = transform( geom );
            render( styling, ( (Point) geom ).get0(), ( (Point) geom ).get1() );
        }
        geom = clipGeometry( geom );
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
            // LOG.trace( "Breaking open multi geometry." );
            MultiGeometry<?> mc = (MultiGeometry<?>) geom;
            for ( Geometry g : mc ) {
                render( styling, g );
            }
        }
    }

    Double fromCurve( Curve curve, boolean close ) {
        Double line = new Double();

        // TODO use error criterion
        ICRS crs = curve.getCoordinateSystem();
        curve = linearizer.linearize( curve, new NumPointsCriterion( 100 ) );
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
                if ( close && isZero( x - p.get0() ) && isZero( y - p.get1() ) ) {
                    line.closePath();
                } else {
                    line.lineTo( p.get0(), p.get1() );
                }
            }
        }

        line.transform( worldToScreen );

        return line;
    }

    @Override
    public void render( LineStyling styling, Geometry geom ) {
        if ( geom == null ) {
            LOG.debug( "Trying to render null geometry." );
            return;
        }

        // LOG.trace( "Drawing {} with {}", geom, styling );

        if ( geom instanceof Point ) {
            LOG.warn( "Trying to render point with line styling." );
        }
        geom = clipGeometry( geom );
        if ( geom instanceof Curve ) {
            geom = transform( geom );

            Double line = fromCurve( (Curve) geom, false );
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
            // LOG.trace( "Breaking open multi geometry." );
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
                GeneralPath polygon = new GeneralPath( WIND_EVEN_ODD );
                for ( Curve curve : polygonPatch.getBoundaryRings() ) {
                    Double d = fromCurve( curve, true );
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

    @Override
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
        geom = clipGeometry( geom );
        if ( geom instanceof Envelope ) {
            geom = envelopeToPolygon( (Envelope) geom );
        }
        if ( geom instanceof Surface ) {
            // LOG.trace( "Drawing {} with {}", geom, styling );
            geom = transform( geom );
            render( styling, (Surface) geom );
        }
        if ( geom instanceof MultiGeometry<?> ) {
            // LOG.trace( "Breaking open multi geometry." );
            for ( Geometry g : (MultiGeometry<?>) geom ) {
                render( styling, g );
            }
        }
    }

    /**
     * Clips the passed geometry with the drawing area if the drawing area does not contain the passed geometry
     * completely.
     * 
     * @param geom
     *            the geometry to clip, must not be <code>null</code>
     * @return the clipped geometry or the original geometry if the geometry lays completely in the drawing area.
     */
    private Geometry clipGeometry( Geometry geom ) {
        geom = transform( geom );
        if ( clippingArea != null && !clippingArea.contains( geom ) ) {
            try {
                Geometry clippedGeometry = clippingArea.getIntersection( geom );
                com.vividsolutions.jts.geom.Geometry jtsOrig = ( (AbstractDefaultGeometry) geom ).getJTSGeometry();
                com.vividsolutions.jts.geom.Geometry jtsClipped = ( (AbstractDefaultGeometry) clippedGeometry ).getJTSGeometry();
                if ( jtsOrig == jtsClipped ) {
                    return geom;
                }
                geom = fixOrientation( clippedGeometry );
            } catch ( UnsupportedOperationException e ) {
                // use original geometry if intersection not supported by JTS
                return geom;
            }
        }
        return geom;
    }

    private Geometry fixOrientation( Geometry geom ) {
        switch ( geom.getGeometryType() ) {
        case PRIMITIVE_GEOMETRY:
            return fixOrientation( (GeometricPrimitive) geom );
        case MULTI_GEOMETRY:
            return fixOrientation( (MultiGeometry<?>) geom );
        default: {
            throw new UnsupportedOperationException();
        }
        }
    }

    private Geometry fixOrientation( GeometricPrimitive geom ) {
        if ( geom.getPrimitiveType() == Surface ) {
            return fixOrientation( (Surface) geom );
        }
        return geom;
    }

    private Geometry fixOrientation( Surface geom ) {
        if ( geom.getSurfaceType() == Polygon ) {
            return fixOrientation( (Polygon) geom );
        }
        throw new UnsupportedOperationException();
    }

    private Geometry fixOrientation( Polygon geom ) {
        Ring exteriorRing = fixOrientation( geom.getExteriorRing(), false );
        List<Ring> interiorRings = fixInteriorOrientation( geom.getInteriorRings() );
        return new DefaultPolygon( null, geom.getCoordinateSystem(), null, exteriorRing, interiorRings );
    }

    private List<Ring> fixInteriorOrientation( List<Ring> interiorRings ) {
        if ( interiorRings == null ) {
            return null;
        }
        List<Ring> fixedRings = new ArrayList<Ring>();
        for ( Ring interiorRing : interiorRings ) {
            fixedRings.add( fixOrientation( interiorRing, true ) );
        }
        return fixedRings;
    }

    private Ring fixOrientation( Ring ring, boolean forceClockwise ) {
        if ( ring.getRingType() != LinearRing ) {
            throw new UnsupportedOperationException();
        }
        LinearRing jtsRing = (LinearRing) ( (AbstractDefaultGeometry) ring ).getJTSGeometry();
        Coordinate[] coords = jtsRing.getCoordinates();

        // TODO check if inversions can be applied in any case (i.e. whether JTS has a guaranteed orientation of
        // intersection result polygons)

        boolean needsInversion = isCCW( coords ) == forceClockwise;
        if ( needsInversion ) {
            return invertOrientation( ring );
        }
        return ring;
    }

    @SuppressWarnings("unchecked")
    private Geometry fixOrientation( MultiGeometry<?> geom ) {

        List fixedMembers = new ArrayList<Object>( geom.size() );
        for ( Geometry member : geom ) {
            Geometry fixedMember = fixOrientation( member );
            fixedMembers.add( fixedMember );
        }

        switch ( geom.getMultiGeometryType() ) {
        case MULTI_GEOMETRY:
            return new DefaultMultiGeometry<Geometry>( null, geom.getCoordinateSystem(), null,
                                                       (List<Geometry>) fixedMembers );
        case MULTI_POLYGON:
            return new DefaultMultiPolygon( null, geom.getCoordinateSystem(), null, (List<Polygon>) fixedMembers );
        case MULTI_SURFACE:
            return new DefaultMultiSurface( null, geom.getCoordinateSystem(), null, (List<Surface>) fixedMembers );
        default:
            throw new UnsupportedOperationException();
        }
    }

    @Override
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

    @Override
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
            return in * 0.28 / pixelSize;
        case Foot:
            // TODO properly convert the res to foot
            return in / res;
        case Metre:
            return in / res;
        case mm:
            return in / pixelSize;
        }
        return in;
    }

}
