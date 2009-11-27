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

package org.deegree.rendering.r2d.strokes;

import static java.awt.geom.PathIterator.SEG_CLOSE;
import static java.awt.geom.PathIterator.SEG_LINETO;
import static java.awt.geom.PathIterator.SEG_MOVETO;
import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;
import static java.util.Arrays.asList;
import static org.deegree.commons.utils.GeometryUtils.measurePathLengths;
import static org.deegree.commons.utils.math.MathUtils.isZero;
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
import java.util.ListIterator;

import org.deegree.commons.utils.Pair;
import org.deegree.rendering.r2d.styling.components.LinePlacement;
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

    private FontRenderContext frc;

    private double lineHeight;

    private static final float FLATNESS = 1;

    private final LinePlacement linePlacement;

    /**
     * @param text
     * @param font
     * @param linePlacement
     */
    public TextStroke( String text, Font font, LinePlacement linePlacement ) {
        this.text = text;
        this.font = font;
        this.linePlacement = linePlacement;
        frc = new FontRenderContext( null, false, false );
        lineHeight = font.getLineMetrics( text, frc ).getHeight();
    }

    // the code for this function may benefit from a good refactoring idea
    // I had none that was practical.
    // the method returnes (true, path) if rendering word wise is possible
    // and (false, null) if not
    private Pair<Boolean, GeneralPath> tryWordWise( Shape shape ) {
        // two steps: first a list is prepared that describes what to render where
        // second the list is rendered

        // first step: iterates over the segment lengths and the words to render
        Pair<Boolean, GeneralPath> pair = new Pair<Boolean, GeneralPath>();

        LinkedList<String> words = new LinkedList<String>( asList( text.split( "\\s" ) ) );
        ListIterator<String> list = words.listIterator();
        while ( list.hasNext() ) {
            if ( list.next().trim().equals( "" ) ) {
                list.remove();
            }
        }
        LinkedList<String> wordsCopy = new LinkedList<String>();
        wordsCopy.addAll( words );
        LinkedList<StringOrGap> wordsToRender = new LinkedList<StringOrGap>();

        LinkedList<Double> lengths = measurePathLengths( shape );

        // things to consider: initial gaps, gaps, whether to repeat the string
        // algorithm sketch:
        // try to match as many words on each line segment as possible (taking into account gap length)
        // if a segment is encountered that is smaller than the current word length, return false
        // (even if maybe the next segment would be fitting, the empty segment would just not look nice, so better abort
        // here)
        // the list will contain StringOrGap objects (a choice between a String, a gap or a end of line tag)
        // So if some gap[1]/word[1-*] combination fits the segment, all of them are added to the list, as well as an
        // end of line tag. Then, the next segment is considered. If repeat is on, the words list will never be empty
        // and the loop will run until the segment lengths list is empty.
        // TODO: Optimization: do not add Strings, add the GlyphVectors
        double currentGap = isZero( linePlacement.initialGap ) ? 0 : linePlacement.initialGap;
        if ( !isZero( currentGap ) ) {
            StringOrGap sog = new StringOrGap();
            sog.gap = currentGap;
            wordsToRender.add( sog );
        }

        while ( words.size() > 0 && lengths.size() > 0 ) {
            String word = words.poll();
            GlyphVector vec = font.createGlyphVector( frc, word );
            double vecLength = currentGap + vec.getOutline().getBounds2D().getWidth();
            double segLength = lengths.poll() - font.getSize2D(); // at least it works for line angles < 90°

            if ( vecLength > segLength ) {
                pair.first = false;
                return pair;
            }

            currentGap = 0;

            String newWord = word;
            words.addFirst( word );

            double totalLength = 0;
            do { // do/while because at least one word has to be added
                words.poll();

                boolean justInserted = false;

                if ( words.isEmpty() && linePlacement.repeat ) {
                    // in this case, the words list has to be filled again. Also the gap has to be considered.
                    words.addAll( wordsCopy );

                    StringOrGap sog = new StringOrGap();
                    sog.string = newWord;
                    wordsToRender.add( sog );
                    justInserted = true; // set this flag so the adding at the end of the loop won't happen again with
                    // the same string.

                    totalLength += font.createGlyphVector( frc, word ).getOutline().getBounds2D().getWidth();
                    newWord = "";
                    if ( !isZero( linePlacement.gap ) ) {
                        sog = new StringOrGap();
                        sog.gap = linePlacement.gap;
                        totalLength += sog.gap;
                        wordsToRender.add( sog );
                    }
                } else if ( words.isEmpty() ) {
                    // in this case, the last word can be rendered, and the loop terminates
                    StringOrGap sog = new StringOrGap();
                    sog.string = newWord;
                    wordsToRender.add( sog );
                    break;
                }

                newWord = ( newWord.equals( "" ) ? "" : newWord + " " ) + words.peek();

                vecLength = font.createGlyphVector( frc, newWord ).getOutline().getBounds2D().getWidth();
                if ( !justInserted && ( totalLength + vecLength >= segLength ) ) {
                    // the normal case, one or more words have been fit, and the current word does overflow the length
                    // -> add the word
                    StringOrGap sog = new StringOrGap();
                    sog.string = word;
                    wordsToRender.add( sog );
                }

                word = newWord;

            } while ( totalLength + vecLength < segLength );

            StringOrGap sog = new StringOrGap();
            sog.newLine = true;

            // the next segment will be considered, so add a end of line tag. If the last StringOrGap was a gap, insert
            // the end of line BEFORE the gap.
            if ( wordsToRender.getLast().string == null ) {
                wordsToRender.add( wordsToRender.size() - 1, sog );
            } else {
                wordsToRender.add( sog );
            }

        }

        LOG.trace( "Rendering the word/gap list: " + wordsToRender );

        // prepare for second run - now actually doing something
        // this just iterates over the path and creates the final shape to render.
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

                double length = 0;

                StringOrGap sog = wordsToRender.poll();

                // render all pieces until the next EOL
                while ( !sog.newLine ) {
                    double gap = 0;
                    // add all gaps in the list (will normally only be one)
                    while ( sog.string == null && !sog.newLine ) {
                        gap += sog.gap;
                        sog = wordsToRender.poll();
                    }

                    GlyphVector vec = font.createGlyphVector( frc, sog.string );
                    Shape text = vec.getOutline();

                    // straightforward: move to the beginning of the segment
                    // rotate so text direction fits the line
                    // move to current position of part
                    // the lineHeight / 4 centers the text center line on the line
                    AffineTransform t = new AffineTransform();
                    t.setToTranslation( lastx, lasty );
                    t.rotate( angle );
                    t.translate( gap + length, lineHeight / 4 );

                    pair.second.append( t.createTransformedShape( text ), false );

                    length += gap + text.getBounds2D().getWidth();

                    sog = wordsToRender.poll();

                }

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
                        result.append( t.createTransformedShape( glyph ), false );
                        next += ( advance + nextAdvance );
                        currentChar++;
                        if ( linePlacement.repeat )
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

    class StringOrGap {
        String string;

        double gap;

        boolean newLine;

        @Override
        public String toString() {
            return newLine ? "[eol]" : ( string == null ? "" + gap : string );
        }
    }

}
