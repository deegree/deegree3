//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.rendering;

import static org.deegree.commons.utils.MathUtils.isZero;
import static org.deegree.commons.utils.MathUtils.round;
import static org.deegree.rendering.RenderHelper.renderMark;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Envelope;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.SurfacePatch;
import org.deegree.model.styling.LineStyling;
import org.deegree.model.styling.PointStyling;
import org.deegree.model.styling.PolygonStyling;
import org.deegree.model.styling.TextStyling;
import org.deegree.model.styling.components.Fill;
import org.deegree.model.styling.components.Graphic;
import org.deegree.model.styling.components.Stroke;
import org.slf4j.Logger;

/**
 * <code>Java2DRenderer</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Java2DRenderer implements Renderer {

    private static final Logger LOG = getLogger( Java2DRenderer.class );

    private Graphics2D graphics;

    private double scale;

    /**
     * @param graphics
     * @param width
     * @param height
     * @param bbox
     */
    public Java2DRenderer( Graphics2D graphics, int width, int height, Envelope bbox ) {
        this.graphics = graphics;

        if ( bbox != null ) {
            double scalex = width / bbox.getWidth();
            double scaley = height / bbox.getHeight();
            // always use the bigger scale to determine line width etc.
            scale = 1 / ( scalex > scaley ? scalex : scaley );

            AffineTransform worldToScreen = graphics.getTransform();
            worldToScreen.translate( -bbox.getMin().getX() * scalex, -bbox.getMin().getY() * scaley );
            worldToScreen.scale( scalex, scaley );
            graphics.setTransform( worldToScreen );
        } else {
            scale = 1;
        }
    }

    void applyGraphicFill( Graphic graphic ) {
        double width = graphic.size;
        double height = graphic.size;

        if ( width < 0 ) {
            if ( graphic.image == null ) {
                width = 6;
                height = 6;
            } else {
                width = graphic.image.getWidth();
                height = graphic.image.getHeight();
            }
        }

        double x0 = width * graphic.anchorPointX + graphic.displacementX;
        double y0 = height * graphic.anchorPointY + graphic.displacementY;

        BufferedImage img;

        if ( graphic.image == null ) {
            img = renderMark( graphic.mark, graphic.size < 0 ? 6 : round( graphic.size ) );
        } else {
            img = graphic.image;
        }

        graphics.setPaint( new TexturePaint( img, new Rectangle2D.Double( x0 * scale, y0 * scale, width * scale,
                                                                          height * scale ) ) );
    }

    void applyFill( Fill fill ) {
        if ( fill == null ) {
            graphics.setPaint( new Color( 0, 0, 0, 0 ) );
            return;
        }

        if ( fill.graphic == null ) {
            graphics.setPaint( fill.color );
        } else {
            applyGraphicFill( fill.graphic );
        }
    }

    void applyStroke( Stroke stroke ) {
        if ( stroke == null || isZero( stroke.width ) ) {
            graphics.setPaint( new Color( 0, 0, 0, 0 ) );
            return;
        }
        if ( stroke.stroke == null && stroke.fill == null ) {
            graphics.setPaint( stroke.color );
        }
        graphics.setStroke( new BasicStroke( (float) ( stroke.width * scale ) ) );
    }

    public void render( TextStyling styling, String text, Geometry geom ) {
        // TODO Auto-generated method stub

    }

    private void render( PointStyling styling, double x, double y ) {
        Graphic g = styling.graphic;

        BufferedImage img;

        if ( g.image == null ) {
            img = renderMark( g.mark, g.size < 0 ? 6 : round( g.size ) );
        } else {
            img = g.image;
        }

        x += g.displacementX * scale;
        y += g.displacementY * scale;
        x -= g.anchorPointX * scale * img.getWidth();
        y -= g.anchorPointY * scale * img.getHeight();

        graphics.drawImage( img, round( x ), round( y ), round( img.getWidth() * scale ), round( img.getHeight()
                                                                                                 * scale ), null );
    }

    public void render( PointStyling styling, Geometry geom ) {
        if ( geom instanceof Point ) {
            render( styling, ( (Point) geom ).getX(), ( (Point) geom ).getY() );
        }
        if ( geom instanceof Surface ) {
            Surface surface = (Surface) geom;
            for ( SurfacePatch patch : surface.getPatches() ) {
                for ( Curve curve : patch.getBoundary() ) {
                    render( styling, curve );
                }
            }
        }
        if ( geom instanceof Curve ) {
            Curve curve = (Curve) geom;
            double[] xs = curve.getX();
            double[] ys = curve.getY();
            for ( int i = 0; i < xs.length; ++i ) {
                render( styling, xs[i], ys[i] );
            }
        }
    }

    private static Path2D.Double fromCurve( Curve curve ) {
        Path2D.Double line = new Path2D.Double();
        double[] xs = curve.getX();
        double[] ys = curve.getY();
        line.moveTo( xs[0], ys[0] );
        for ( int i = 1; i < xs.length; ++i ) {
            line.lineTo( xs[i], ys[i] );
        }
        return line;
    }

    public void render( LineStyling styling, Geometry geom ) {
        if ( geom instanceof Point ) {
            LOG.warn( "Trying to render point with line styling." );
        }
        if ( geom instanceof Curve ) {
            Path2D.Double line = fromCurve( (Curve) geom );
            applyStroke( styling.stroke );
            graphics.draw( line );
        }
        if ( geom instanceof Surface ) {
            Surface surface = (Surface) geom;
            for ( SurfacePatch patch : surface.getPatches() ) {
                for ( Curve curve : patch.getBoundary() ) {
                    render( styling, curve );
                }
            }
        }
    }

    private void render( PolygonStyling styling, Surface surface ) {
        for ( SurfacePatch patch : surface.getPatches() ) {
            Area polygon = null;
            for ( Curve curve : patch.getBoundary() ) {
                if ( polygon == null ) {
                    polygon = new Area( fromCurve( curve ) );
                } else {
                    polygon.subtract( new Area( fromCurve( curve ) ) );
                }
            }

            applyFill( styling.fill );
            graphics.fill( polygon );
            applyStroke( styling.stroke );
            graphics.draw( polygon );
        }
    }

    public void render( PolygonStyling styling, Geometry geom ) {
        if ( geom instanceof Point ) {
            LOG.warn( "Trying to render point with polygon styling." );
        }
        if ( geom instanceof Curve ) {
            LOG.warn( "Trying to render line with polygon styling." );
        }
        if ( geom instanceof Surface ) {
            render( styling, (Surface) geom );
        }
    }

}
