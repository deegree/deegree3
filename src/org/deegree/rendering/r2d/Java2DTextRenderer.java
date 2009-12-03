//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.rendering.r2d;

import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.JOIN_ROUND;
import static java.awt.Font.BOLD;
import static java.awt.Font.ITALIC;
import static java.awt.Font.PLAIN;
import static java.awt.geom.AffineTransform.getTranslateInstance;
import static org.deegree.commons.utils.math.MathUtils.isZero;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Path2D.Double;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.rendering.r2d.strokes.OffsetStroke;
import org.deegree.rendering.r2d.strokes.TextStroke;
import org.deegree.rendering.r2d.styling.TextStyling;
import org.deegree.rendering.r2d.styling.components.Font.Style;
import org.slf4j.Logger;

/**
 * <code>Java2DTextRenderer</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Java2DTextRenderer implements TextRenderer {

    private static final Logger LOG = getLogger( Java2DTextRenderer.class );

    private Java2DRenderer renderer;

    /**
     * @param renderer
     */
    public Java2DTextRenderer( Java2DRenderer renderer ) {
        this.renderer = renderer;
    }

    private void render( TextStyling styling, Font font, String text, Point p ) {
        Point2D.Double pt = (Point2D.Double) renderer.worldToScreen.transform(
                                                                               new Point2D.Double( p.get0(), p.get1() ),
                                                                               null );
        double x = pt.x + renderer.considerUOM( styling.displacementX, styling.uom );
        double y = pt.y + renderer.considerUOM( styling.displacementY, styling.uom );
        renderer.graphics.setFont( font );
        AffineTransform transform = renderer.graphics.getTransform();
        renderer.graphics.rotate( styling.rotation, x, y );
        TextLayout layout = new TextLayout( text, font, renderer.graphics.getFontRenderContext() );
        double width = layout.getBounds().getWidth();
        double height = layout.getBounds().getHeight();
        double px = x - styling.anchorPointX * width;
        double py = y + styling.anchorPointY * height;

        if ( styling.halo != null ) {
            renderer.applyFill( styling.halo.fill, styling.uom );

            BasicStroke stroke = new BasicStroke(
                                                  round( 2 * renderer.considerUOM( styling.halo.radius, styling.uom ) ),
                                                  CAP_BUTT, JOIN_ROUND );
            renderer.graphics.setStroke( stroke );
            renderer.graphics.draw( layout.getOutline( getTranslateInstance( px, py ) ) );
        }

        renderer.graphics.setStroke( new BasicStroke() );

        renderer.applyFill( styling.fill, styling.uom );
        layout.draw( renderer.graphics, (float) px, (float) py );

        renderer.graphics.setTransform( transform );
    }

    private void render( TextStyling styling, Font font, String text, Curve c ) {
        renderer.applyFill( styling.fill, styling.uom );
        java.awt.Stroke stroke = new TextStroke( text, font, styling.linePlacement );
        if ( !isZero( styling.linePlacement.perpendicularOffset ) ) {
            stroke = new OffsetStroke( styling.linePlacement.perpendicularOffset, stroke,
                                       styling.linePlacement.perpendicularOffsetType );
        }

        renderer.graphics.setStroke( stroke );
        Double line = renderer.fromCurve( c );

        renderer.graphics.draw( line );
    }

    public void render( TextStyling styling, String text, Geometry geom ) {
        if ( geom == null ) {
            LOG.debug( "Trying to render null geometry." );
            return;
        }
        if ( text == null || text.length() == 0 ) {
            LOG.debug( "Trying to render null or zero length text." );
            return;
        }

        geom = renderer.transform( geom );
        AffineTransform shear = null;

        int style = styling.font.bold ? BOLD : PLAIN;
        switch ( styling.font.fontStyle ) {
        case ITALIC:
            style += ITALIC;
            break;
        case NORMAL:
            style += PLAIN; // yes, it's zero, but the code looks nicer this way
            break;
        case OBLIQUE:
            // Shear the font horizontally to achieve obliqueness
            shear = new AffineTransform();
            shear.shear( -0.2, 0 );
            break;
        }

        // use the first matching name, or Dialog, if none was found
        int size = round( renderer.considerUOM( styling.font.fontSize, styling.uom ) );
        Font font = new Font( "", style, size );
        for ( String name : styling.font.fontFamily ) {
            font = new Font( name, style, size );
            if ( !font.getFamily().equalsIgnoreCase( "dialog" ) ) {
                break;
            }
        }

        if ( styling.font.fontStyle == Style.OBLIQUE && shear != null )
            font = font.deriveFont( shear );

        if ( geom instanceof Point ) {
            render( styling, font, text, (Point) geom );
        } else if ( geom instanceof Surface ) {
            Surface surface = (Surface) geom;
            if ( styling.linePlacement != null ) {
                for ( SurfacePatch patch : surface.getPatches() ) {
                    if ( patch instanceof PolygonPatch ) {
                        PolygonPatch polygonPatch = (PolygonPatch) patch;
                        for ( Curve curve : polygonPatch.getBoundaryRings() ) {
                            render( styling, font, text, curve );
                        }
                    } else {
                        throw new IllegalArgumentException( "Cannot render non-planar surfaces." );
                    }
                }
            } else {
                render( styling, font, text, surface.getCentroid() );
            }
        } else if ( geom instanceof Curve ) {
            if ( styling.linePlacement != null ) {
                render( styling, font, text, (Curve) geom );
            }
        } else if ( geom instanceof MultiSurface ) {
            render( styling, font, text, ( (MultiSurface) geom ).getCentroid() );
        } else {
            LOG.warn( "Trying to use unsupported geometry type '{}' for text rendering.",
                      geom.getClass().getSimpleName() );
        }

    }

}
