/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.deegree.rendering.strokes;

import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;

import java.awt.Font;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

/**
 * <code>TextStroke</code>
 * 
 * @author Jerry Huxtable
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TextStroke implements Stroke {

    private String text;

    private Font font;

    private boolean repeat = false;

    private AffineTransform t = new AffineTransform();

    private static final float FLATNESS = 1;

    /**
     * @param text
     * @param font
     * @param repeat
     */
    public TextStroke( String text, Font font, boolean repeat ) {
        this.text = text;
        this.font = font;
        this.repeat = repeat;
        if ( repeat && !text.endsWith( " " ) ) {
            this.text = text + " ";
        }
    }

    public Shape createStrokedShape( Shape shape ) {
        FontRenderContext frc = new FontRenderContext( null, false, false );
        GlyphVector glyphVector = font.createGlyphVector( frc, text );

        GeneralPath result = new GeneralPath();
        PathIterator it = new FlatteningPathIterator( shape.getPathIterator( null ), FLATNESS );
        double points[] = new double[6];
        double moveX = 0, moveY = 0;
        double lastX = 0, lastY = 0;
        double thisX = 0, thisY = 0;
        int type = 0;
        double next = 0;
        int currentChar = 0;
        int length = glyphVector.getNumGlyphs();

        if ( length == 0 )
            return result;

        double nextAdvance = 0;

        while ( currentChar < length && !it.isDone() ) {
            type = it.currentSegment( points );
            switch ( type ) {
            case PathIterator.SEG_MOVETO:
                moveX = lastX = points[0];
                moveY = lastY = points[1];
                result.moveTo( moveX, moveY );
                nextAdvance = glyphVector.getGlyphMetrics( currentChar ).getAdvance() * 0.5f;
                next = nextAdvance;
                break;

            case PathIterator.SEG_CLOSE:
                points[0] = moveX;
                points[1] = moveY;
                // Fall into....

            case PathIterator.SEG_LINETO:
                thisX = points[0];
                thisY = points[1];
                double dx = thisX - lastX;
                double dy = thisY - lastY;
                double distance = sqrt( dx * dx + dy * dy );
                if ( distance >= next ) {
                    double r = 1.0f / distance;
                    double angle = atan2( dy, dx );
                    while ( currentChar < length && distance >= next ) {
                        Shape glyph = glyphVector.getGlyphOutline( currentChar );
                        Point2D p = glyphVector.getGlyphPosition( currentChar );
                        double px = p.getX();
                        double py = p.getY();
                        double x = lastX + next * dx * r;
                        double y = lastY + next * dy * r;
                        double advance = nextAdvance;
                        nextAdvance = currentChar < length - 1 ? glyphVector.getGlyphMetrics( currentChar + 1 ).getAdvance() * 0.5f
                                                              : advance;
                        t.setToTranslation( x, y );
                        t.rotate( angle );
                        t.translate( -px - advance, -py );
                        t.translate( -glyph.getBounds2D().getWidth() / 2, glyph.getBounds().getHeight() / 2 );
                        result.append( t.createTransformedShape( glyph ), false );
                        next += ( advance + nextAdvance );
                        currentChar++;
                        if ( repeat )
                            currentChar %= length;
                    }
                }
                next -= distance;
                lastX = thisX;
                lastY = thisY;
                break;
            }
            it.next();
        }

        return result;
    }

}
