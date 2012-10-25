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

import static java.awt.geom.Path2D.WIND_EVEN_ODD;
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
import static org.deegree.geometry.utils.GeometryUtils.envelopeToPolygon;
import static org.deegree.rendering.r2d.RenderHelper.renderMark;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Graphics2D;
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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.refs.GeometryReference;
import org.deegree.style.styling.LineStyling;
import org.deegree.style.styling.PointStyling;
import org.deegree.style.styling.PolygonStyling;
import org.deegree.style.styling.Styling;
import org.deegree.style.styling.components.Graphic;
import org.deegree.style.styling.components.UOM;
import org.deegree.style.utils.UomCalculator;
import org.slf4j.Logger;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;

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

    private double pixelSize = 0.28;

    private double res;

    private int width;

    private UomCalculator uomCalculator;

    private Java2DStrokeRenderer strokeRenderer;

    private Java2DFillRenderer fillRenderer;

    GeometryHelper geomHelper;

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
        uomCalculator = new UomCalculator( pixelSize, res );
        fillRenderer = new Java2DFillRenderer( uomCalculator, graphics );
        strokeRenderer = new Java2DStrokeRenderer( graphics, uomCalculator, fillRenderer );
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
            calculateResolution( bbox );

            geomHelper = new GeometryHelper( bbox, width, worldToScreen );

            LOG.debug( "For coordinate transformations, scaling by x = {} and y = {}", scalex, -scaley );
            LOG.trace( "Final transformation was {}", worldToScreen );
        } else {
            LOG.warn( "No envelope given, proceeding with a scale of 1." );
        }
        uomCalculator = new UomCalculator( pixelSize, res );
        fillRenderer = new Java2DFillRenderer( uomCalculator, graphics );
        strokeRenderer = new Java2DStrokeRenderer( graphics, uomCalculator, fillRenderer );
    }

    private void calculateResolution( final Envelope bbox ) {
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
    }

    /**
     * @param graphics
     */
    public Java2DRenderer( Graphics2D graphics ) {
        this.graphics = graphics;
        res = 1;
        uomCalculator = new UomCalculator( pixelSize, res );
        fillRenderer = new Java2DFillRenderer( uomCalculator, graphics );
        strokeRenderer = new Java2DStrokeRenderer( graphics, uomCalculator, fillRenderer );
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
        Rectangle2D.Double rect = fillRenderer.getGraphicBounds( g, x, y, styling.uom );

        if ( g.image == null && g.imageURL == null ) {
            renderMark( g.mark, g.size < 0 ? 6 : round( considerUOM( g.size, styling.uom ) ), styling.uom, this,
                        rect.getMinX(), rect.getMinY(), g.rotation );
            return;
        }

        BufferedImage img = g.image;

        // try if it's an svg
        if ( img == null && g.imageURL != null ) {
            img = prepareSvg( rect, g );
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

    private BufferedImage prepareSvg( Rectangle2D.Double rect, Graphic g ) {
        BufferedImage img = null;
        ComparablePair<String, Integer> cp = new ComparablePair<String, Integer>( g.imageURL, round( g.size ) );
        if ( svgCache.containsKey( cp ) ) {
            img = svgCache.get( cp );
        } else {
            PNGTranscoder t = new PNGTranscoder();

            t.addTranscodingHint( KEY_WIDTH, new Float( rect.width ) );
            t.addTranscodingHint( KEY_HEIGHT, new Float( rect.height ) );

            TranscoderInput input = new TranscoderInput( g.imageURL );

            // TODO improve performance by writing a custom transcoder output directly rendering on an image, or
            // even on the target graphics
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
        return img;
    }

    @Override
    public void render( PointStyling styling, Geometry geom ) {
        if ( geom == null ) {
            LOG.debug( "Trying to render null geometry." );
            return;
        }

        if ( geom instanceof Point ) {
            geom = geomHelper.transform( geom );
            render( styling, ( (Point) geom ).get0(), ( (Point) geom ).get1() );
            return;
        }
        geom = geomHelper.clipGeometry( geom );
        // TODO properly convert'em
        if ( geom instanceof Surface ) {
            render( styling, (Surface) geom );
        } else if ( geom instanceof Curve ) {
            render( styling, (Curve) geom );
        } else if ( geom instanceof MultiGeometry<?> ) {
            // LOG.trace( "Breaking open multi geometry." );
            MultiGeometry<?> mc = (MultiGeometry<?>) geom;
            for ( Geometry g : mc ) {
                render( styling, g );
            }
        }
    }

    private void render( PointStyling styling, Surface surface ) {
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

    private void render( PointStyling styling, Curve curve ) {
        if ( curve.getCurveSegments().size() != 1 || !( curve.getCurveSegments().get( 0 ) instanceof LineStringSegment ) ) {
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

    @Override
    public void render( LineStyling styling, Geometry geom ) {
        if ( geom == null ) {
            LOG.debug( "Trying to render null geometry." );
            return;
        }

        // LOG.trace( "Drawing {} with {}", geom, styling );

        if ( geom instanceof Point ) {
            LOG.warn( "Trying to render point with line styling." );
            return;
        }
        geom = geomHelper.clipGeometry( geom );
        if ( geom instanceof Curve ) {
            Double line = geomHelper.fromCurve( (Curve) geom, false );
            strokeRenderer.applyStroke( styling.stroke, styling.uom, line, styling.perpendicularOffset,
                                        styling.perpendicularOffsetType );
        } else if ( geom instanceof Surface ) {
            render( styling, (Surface) geom );
        } else if ( geom instanceof MultiGeometry<?> ) {
            // LOG.trace( "Breaking open multi geometry." );
            MultiGeometry<?> mc = (MultiGeometry<?>) geom;
            for ( Geometry g : mc ) {
                render( styling, g );
            }
        }
    }

    private void render( LineStyling styling, Surface surface ) {
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

    private void render( PolygonStyling styling, Surface surface ) {
        for ( SurfacePatch patch : surface.getPatches() ) {
            if ( patch instanceof PolygonPatch ) {
                LinkedList<Double> lines = new LinkedList<Double>();
                PolygonPatch polygonPatch = (PolygonPatch) patch;

                // just appending the holes appears to work, the Java2D rendering mechanism can determine that they lie
                // inside and thus no substraction etc. is needed. This speeds up things SIGNIFICANTLY
                GeneralPath polygon = new GeneralPath( WIND_EVEN_ODD );
                for ( Curve curve : polygonPatch.getBoundaryRings() ) {
                    Double d = geomHelper.fromCurve( curve, true );
                    lines.add( d );
                    polygon.append( d, false );
                }

                fillRenderer.applyFill( styling.fill, styling.uom );
                graphics.fill( polygon );
                for ( Double d : lines ) {
                    strokeRenderer.applyStroke( styling.stroke, styling.uom, d, styling.perpendicularOffset,
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
        geom = geomHelper.clipGeometry( geom );
        if ( geom instanceof Envelope ) {
            geom = envelopeToPolygon( (Envelope) geom );
        }
        if ( geom instanceof Surface ) {
            // LOG.trace( "Drawing {} with {}", geom, styling );
            render( styling, (Surface) geom );
        }
        if ( geom instanceof MultiGeometry<?> ) {
            // LOG.trace( "Breaking open multi geometry." );
            for ( Geometry g : (MultiGeometry<?>) geom ) {
                render( styling, g );
            }
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

    Java2DFillRenderer getFillRenderer() {
        return fillRenderer;
    }

    Java2DStrokeRenderer getStrokeRenderer() {
        return strokeRenderer;
    }

}
