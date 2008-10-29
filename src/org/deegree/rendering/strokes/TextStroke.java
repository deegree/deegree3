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

import static java.awt.geom.PathIterator.SEG_CLOSE;
import static java.awt.geom.PathIterator.SEG_LINETO;
import static java.awt.geom.PathIterator.SEG_MOVETO;
import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;
import static java.util.Arrays.asList;
import static org.deegree.commons.utils.GeometryUtils.measurePathLengths;
import static org.slf4j.LoggerFactory.getLogger;

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
import java.util.LinkedList;

import org.deegree.commons.utils.Pair;
import org.slf4j.Logger;

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

    private static final Logger LOG = getLogger( TextStroke.class );

    private String text;

    private Font font;

    private boolean repeat = false;

    private FontRenderContext frc;

    private double lineHeight;

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
        frc = new FontRenderContext( null, false, false );
        lineHeight = font.getLineMetrics( text, frc ).getHeight();
    }

    private Pair<Boolean, GeneralPath> tryWordWise( Shape shape ) {
        Pair<Boolean, GeneralPath> pair = new Pair<Boolean, GeneralPath>();

        LinkedList<String> words = new LinkedList<String>( asList( text.split( "\\s" ) ) );
        LinkedList<String> wordsCopy = new LinkedList<String>();
        wordsCopy.addAll( words );
        LinkedList<String> wordsToRender = new LinkedList<String>();

        LinkedList<Double> lengths = measurePathLengths( shape );

        if ( words.size() > lengths.size() ) {
            pair.first = false;
            return pair;
        }

        while ( words.size() > 0 && lengths.size() > 0 ) {
            String word = words.poll();
            GlyphVector vec = font.createGlyphVector( frc, word );
            double vecLength = vec.getOutline().getBounds2D().getWidth();
            double segLength = lengths.poll() - font.getSize2D();

            if ( vecLength > segLength ) {
                pair.first = false;
                return pair;
            }

            String newWord = word;
            words.addFirst( word );
            do {
                word = newWord;
                words.poll();

                if ( words.isEmpty() && repeat ) {
                    words.addAll( wordsCopy );
                }

                if ( words.isEmpty() ) {
                    break;
                }

                newWord += " " + words.peek();

                vec = font.createGlyphVector( frc, newWord );
                vecLength = vec.getOutline().getBounds2D().getWidth();
            } while ( vecLength < segLength );

            if ( words.isEmpty() && repeat ) {
                words.addAll( wordsCopy );
            }

            wordsToRender.add( word );
        }

        pair.first = true;
        pair.second = new GeneralPath();

        PathIterator it = new FlatteningPathIterator( shape.getPathIterator( null ), FLATNESS );
        double points[] = new double[6];

        double movex = 0, movey = 0, lastx = 0, lasty = 0;

        while ( !it.isDone() ) {
            int type = it.currentSegment( points );
            switch ( type ) {
            case SEG_MOVETO:
                movex = lastx = points[0];
                movey = lasty = points[1];
                pair.second.moveTo( movex, movey );
                break;

            case SEG_CLOSE:
                points[0] = movex;
                points[1] = movey;
                // Fall into....

            case SEG_LINETO:
                if ( wordsToRender.isEmpty() ) {
                    break;
                }

                double px = points[0];
                double py = points[1];
                double dx = px - lastx;
                double dy = py - lasty;
                double angle = atan2( dy, dx );

                GlyphVector vec = font.createGlyphVector( frc, wordsToRender.poll() );
                Shape text = vec.getOutline();

                AffineTransform t = new AffineTransform();
                t.setToTranslation( lastx, lasty );
                t.rotate( angle );
                t.translate( 0, lineHeight / 4 );

                pair.second.append( t.createTransformedShape( text ), false );

                lastx = px;
                lasty = py;
                break;
            }
            it.next();
        }

        return pair;
    }

    public Shape createStrokedShape( Shape shape ) {
        Pair<Boolean, GeneralPath> pair = tryWordWise( shape );
        if ( pair.first ) {
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "Rendered text '" + text + "' word wise." );
            }
            return pair.second;
        }

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
                        AffineTransform t = new AffineTransform();
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

        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Rendered text '" + text + "' character wise." );
        }

        return result;
    }

}
