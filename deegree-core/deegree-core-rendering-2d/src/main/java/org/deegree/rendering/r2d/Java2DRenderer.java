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

import static org.deegree.geometry.utils.GeometryUtils.envelopeToPolygon;
import static org.deegree.rendering.r2d.RenderHelper.calculateResolution;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D.Double;
import java.util.Collection;

import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.refs.GeometryReference;
import org.deegree.style.styling.LineStyling;
import org.deegree.style.styling.PointStyling;
import org.deegree.style.styling.PolygonStyling;
import org.deegree.style.styling.Styling;
import org.slf4j.Logger;

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

    RendererContext rendererContext;

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
        initRenderers( bbox );
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
            res = calculateResolution( bbox, width );

            LOG.debug( "For coordinate transformations, scaling by x = {} and y = {}", scalex, -scaley );
            LOG.trace( "Final transformation was {}", worldToScreen );
        } else {
            LOG.warn( "No envelope given, proceeding with a scale of 1." );
        }
        initRenderers( bbox );
    }

    /**
     * @param graphics
     */
    public Java2DRenderer( Graphics2D graphics ) {
        this.graphics = graphics;
        res = 1;
        initRenderers( null );
    }

    private void initRenderers( Envelope bbox ) {
        rendererContext = new RendererContext( pixelSize, res, graphics, this, bbox, width, worldToScreen );
    }

    @Override
    public void render( PointStyling styling, Geometry geom ) {
        if ( geom == null ) {
            LOG.debug( "Trying to render null geometry." );
            return;
        }

        if ( geom instanceof Point ) {
            geom = rendererContext.geomHelper.transform( geom );
            rendererContext.pointRenderer.render( styling, ( (Point) geom ).get0(), ( (Point) geom ).get1() );
            return;
        }
        geom = rendererContext.clipper.clipGeometry( geom );
        // TODO properly convert'em
        if ( geom instanceof Surface ) {
            rendererContext.polygonRenderer.render( styling, (Surface) geom );
        } else if ( geom instanceof Curve ) {
            rendererContext.curveRenderer.render( styling, (Curve) geom );
        } else if ( geom instanceof MultiGeometry<?> ) {
            // LOG.trace( "Breaking open multi geometry." );
            MultiGeometry<?> mc = (MultiGeometry<?>) geom;
            for ( Geometry g : mc ) {
                render( styling, g );
            }
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
        if ( styling.stroke != null && styling.stroke.stroke != null ) {
            geom = rendererContext.clipper.clipGeometry( geom );
        }
        if ( geom instanceof Curve ) {
            Double line = rendererContext.geomHelper.fromCurve( (Curve) geom, false );
            rendererContext.strokeRenderer.applyStroke( styling.stroke, styling.uom, line, styling.perpendicularOffset,
                                                        styling.perpendicularOffsetType );
        } else if ( geom instanceof Surface ) {
            rendererContext.polygonRenderer.render( styling, (Surface) geom );
        } else if ( geom instanceof MultiGeometry<?> ) {
            // LOG.trace( "Breaking open multi geometry." );
            MultiGeometry<?> mc = (MultiGeometry<?>) geom;
            for ( Geometry g : mc ) {
                render( styling, g );
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
        if ( styling.stroke != null && styling.stroke.stroke != null ) {
            geom = rendererContext.clipper.clipGeometry( geom );
        }
        if ( geom instanceof Envelope ) {
            geom = envelopeToPolygon( (Envelope) geom );
        }
        if ( geom instanceof Surface ) {
            // LOG.trace( "Drawing {} with {}", geom, styling );
            rendererContext.polygonRenderer.render( styling, (Surface) geom );
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

}
