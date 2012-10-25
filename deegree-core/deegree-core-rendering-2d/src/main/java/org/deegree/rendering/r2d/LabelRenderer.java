//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.rendering.r2d;

import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.JOIN_ROUND;
import static java.awt.geom.AffineTransform.getTranslateInstance;
import static java.lang.Math.toRadians;
import static org.deegree.commons.utils.math.MathUtils.isZero;
import static org.deegree.commons.utils.math.MathUtils.round;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Path2D.Double;

import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.rendering.r2d.strokes.OffsetStroke;
import org.deegree.rendering.r2d.strokes.TextStroke;
import org.deegree.style.styling.TextStyling;

/**
 * Responsible for rendering a single label.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class LabelRenderer {

    private Java2DRenderer renderer;

    LabelRenderer( Java2DRenderer renderer ) {
        this.renderer = renderer;
    }

    void render( TextStyling styling, Font font, String text, Point p ) {
        Point2D.Double pt = (Point2D.Double) renderer.worldToScreen.transform( new Point2D.Double( p.get0(), p.get1() ),
                                                                               null );
        double x = pt.x + renderer.considerUOM( styling.displacementX, styling.uom );
        double y = pt.y - renderer.considerUOM( styling.displacementY, styling.uom );
        renderer.graphics.setFont( font );
        AffineTransform transform = renderer.graphics.getTransform();
        renderer.graphics.rotate( toRadians( styling.rotation ), x, y );
        TextLayout layout;
        synchronized ( FontRenderContext.class ) {
            // apparently getting the font render context is not threadsafe (despite having different graphics here)
            // so do this globally synchronized to fix:
            // http://tracker.deegree.org/deegree-core/ticket/200
            FontRenderContext frc = renderer.graphics.getFontRenderContext();
            layout = new TextLayout( text, font, frc );
        }
        double width = layout.getBounds().getWidth();
        double height = layout.getBounds().getHeight();
        double px = x - styling.anchorPointX * width;
        double py = y + styling.anchorPointY * height;

        if ( styling.halo != null ) {
            renderer.getFillRenderer().applyFill( styling.halo.fill, styling.uom );

            BasicStroke stroke = new BasicStroke(
                                                  round( 2 * renderer.considerUOM( styling.halo.radius, styling.uom ) ),
                                                  CAP_BUTT, JOIN_ROUND );
            renderer.graphics.setStroke( stroke );
            renderer.graphics.draw( layout.getOutline( getTranslateInstance( px, py ) ) );
        }

        renderer.graphics.setStroke( new BasicStroke() );

        renderer.getFillRenderer().applyFill( styling.fill, styling.uom );
        layout.draw( renderer.graphics, (float) px, (float) py );

        renderer.graphics.setTransform( transform );
    }

     void render( TextStyling styling, Font font, String text, Curve c ) {
        renderer.getFillRenderer().applyFill( styling.fill, styling.uom );
        java.awt.Stroke stroke = new TextStroke( text, font, styling.linePlacement );
        if ( isZero( ( (TextStroke) stroke ).getLineHeight() ) ) {
            return;
        }
        if ( !isZero( styling.linePlacement.perpendicularOffset ) ) {
            stroke = new OffsetStroke( styling.linePlacement.perpendicularOffset, stroke,
                                       styling.linePlacement.perpendicularOffsetType );
        }

        renderer.graphics.setStroke( stroke );
        Double line = renderer.geomHelper.fromCurve( c, false );

        renderer.graphics.draw( line );
    }

}
