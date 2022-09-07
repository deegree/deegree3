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
import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.CAP_SQUARE;
import static java.awt.BasicStroke.JOIN_BEVEL;
import static java.awt.BasicStroke.JOIN_MITER;
import static java.awt.BasicStroke.JOIN_ROUND;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;
import static org.deegree.commons.utils.math.MathUtils.isZero;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.style.utils.ShapeHelper.getShapeFromMarkForFill;
import static org.deegree.style.utils.ShapeHelper.getShapeFromSvg;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.utils.TunableParameter;
import org.deegree.rendering.r2d.strokes.OffsetStroke;
import org.deegree.rendering.r2d.strokes.ShapeStroke;
import org.deegree.style.styling.components.Graphic;
import org.deegree.style.styling.components.PerpendicularOffsetType;
import org.deegree.style.styling.components.Stroke;
import org.deegree.style.styling.components.UOM;
import org.deegree.style.utils.UomCalculator;
import org.slf4j.Logger;

/**
 * Responsible to render stroke styles.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
class Java2DStrokeRenderer {

    private static final Logger LOG = getLogger( Java2DStrokeRenderer.class );

    private Graphics2D graphics;

    private UomCalculator uomCalculator;

    private Java2DFillRenderer fillRenderer;

    Java2DStrokeRenderer( Graphics2D graphics, UomCalculator uomCalculator, Java2DFillRenderer fillRenderer ) {
        this.graphics = graphics;
        this.uomCalculator = uomCalculator;
        this.fillRenderer = fillRenderer;
    }

    void applyStroke( Stroke stroke, UOM uom, Shape object, double perpendicularOffset, PerpendicularOffsetType type ) {
        if ( stroke == null || isZero( stroke.width ) ) {
            graphics.setPaint( new Color( 0, 0, 0, 0 ) );
            return;
        }
        if ( stroke.fill == null ) {
            graphics.setPaint( stroke.color );
        } else {
            fillRenderer.applyGraphicFill( stroke.fill, uom );
        }
        if ( stroke.stroke != null ) {
            if ( applyGraphicStroke( stroke, uom, object, perpendicularOffset, type ) ) {
                return;
            }
        } else {
            applyNormalStroke( stroke, uom, object, perpendicularOffset, type );
        }

        graphics.draw( object );
    }

    private boolean applyGraphicStroke( Stroke stroke, UOM uom, Shape object, double perpendicularOffset,
                                        PerpendicularOffsetType type ) {
        double strokeSizeUOM = stroke.stroke.size <= 0 ? 6 : uomCalculator.considerUOM( stroke.stroke.size, uom );

        if ( stroke.stroke.image == null && stroke.stroke.imageURL != null ) {
            Shape shape = getShapeFromSvg( stroke.stroke.imageURL, strokeSizeUOM, stroke.stroke.rotation );
            graphics.setStroke( new ShapeStroke( shape,
                                                 uomCalculator.considerUOM( stroke.strokeGap, uom ) + strokeSizeUOM,
                                                 stroke.positionPercentage,
                                                 uomCalculator.considerUOM( stroke.strokeInitialGap, uom ),
                                                 stroke.stroke.anchorPointX, stroke.stroke.anchorPointY,
                                                 uomCalculator.considerUOM( stroke.stroke.displacementX, uom ),
                                                 uomCalculator.considerUOM( stroke.stroke.displacementY, uom ) ) );
            // NOTE: rendering is done in calling method 
        } else if ( stroke.stroke.mark != null ) {
            double poff = uomCalculator.considerUOM( perpendicularOffset, uom );
            Shape transed = object;
            if ( !isZero( poff ) ) {
                transed = new OffsetStroke( poff, null, type ).createStrokedShape( transed );
            }
            Shape shape = getShapeFromMarkForFill( stroke.stroke.mark, strokeSizeUOM, stroke.stroke.rotation );
            if ( stroke.anchoredSymbol >= 0 ) {
                applyAnchoredSymbol( stroke, uom, shape, transed );
            } else {
                ShapeStroke s = new ShapeStroke( shape,
                                                 uomCalculator.considerUOM( stroke.strokeGap, uom ) + strokeSizeUOM,
                                                 stroke.positionPercentage,
                                                 uomCalculator.considerUOM( stroke.strokeInitialGap, uom ),
                                                 stroke.stroke.anchorPointX, stroke.stroke.anchorPointY,
                                                 uomCalculator.considerUOM( stroke.stroke.displacementX, uom ),
                                                 uomCalculator.considerUOM( stroke.stroke.displacementY, uom ) );
                transed = s.createStrokedShape( transed );
                if ( stroke.stroke.mark.fill != null ) {
                    fillRenderer.applyFill( stroke.stroke.mark.fill, uom );
                    graphics.fill( transed );
                }
                if ( stroke.stroke.mark.stroke != null ) {
                    applyStroke( stroke.stroke.mark.stroke, uom, transed, 0, null );
                    graphics.draw( transed );
                }
            }
            return true;
        } else {
            LOG.warn( "Rendering of raster images along lines is not supported yet." );
        }
        return false;
    }
    
    /**
     * Apply an symbol anchored to the LineString
     * 
     * The way the symbol is represented is determined by a four-digit sequence of numbers.
     * 
     * <pre>
     * xxx1 => Startpoint
     * xxx2 => on line with fixed 50%
     * xxx3 => Endpoint
     * xxx4 => Fallback on percentage in calculation if gap is a problem
     * xxxN => on line with positionPercentage
     * xx1x => Symbols are rotated regularly
     * xx0x => Symbol are oriented to the line (rotation can be used relative to line)
     * x1xx => apply offsets via anchorX/Y and displacementX/Y
     * x0xx => ignore anchorX/Y and displacementX/Y
     * 0xxx => fill symbol only
     * 1xxx => stroke symbol only
     * 2xxx => fill and stroke symbol
     *  
     * x is a placeholder.
     * </pre>
     * <p>
     * <b>Note:</b> Leading zeros can be omitted. For example, 0011 can also be written as 11
     * </p>
     * 
     * @param stroke
     *            Stroke to be applied to the shape
     * @param uom
     *            unit of measure
     * @param shape
     *            Shape to render at position
     * @param linestring
     *            Geometry to apply to
     */
    private void applyAnchoredSymbol( Stroke stroke, UOM uom, Shape shape, Shape linestring ) {
        double posPercent = stroke.anchoredSymbol % 10 == 2 ? 50 : stroke.positionPercentage;
        boolean posRotate = ( stroke.anchoredSymbol % 100 / 10 ) < 1;
        boolean applyOffsets = ( stroke.anchoredSymbol % 1000 / 100 ) == 1;
        int renderCode = ( stroke.anchoredSymbol % 10000 / 1000 );
        boolean applyFill = renderCode == 0 || renderCode == 2;
        boolean applyStroke = renderCode == 1 || renderCode == 2;
        double[][] pntXYR = calculateAnchorPoints( linestring, stroke.anchoredSymbol % 10 == 1,
                                                   stroke.anchoredSymbol % 10 == 3, posPercent,
                                                   uomCalculator.considerUOM( stroke.strokeInitialGap, uom ),
                                                   uomCalculator.considerUOM( stroke.strokeGap + stroke.stroke.size,
                                                                              uom ),
                                                   stroke.anchoredSymbol % 10 == 4 );

        if ( stroke.anchoredSymbol % 10 == 1 && pntXYR[0].length == 4 && pntXYR[0][3] > 0 ) {
            // render start-point
            renderGraphicOrMark( graphics, stroke.stroke, shape, pntXYR[0][0], pntXYR[0][1],
                                 ( posRotate ? pntXYR[0][2] : 0 ), uom, applyOffsets, applyFill, applyStroke );
        } else if ( stroke.anchoredSymbol % 10 == 3 && pntXYR[1].length == 4 && pntXYR[1][3] > 0 ) {
            // render end-point
            renderGraphicOrMark( graphics, stroke.stroke, shape, pntXYR[1][0], pntXYR[1][1],
                                 ( posRotate ? pntXYR[1][2] : 0 ), uom, applyOffsets, applyFill, applyStroke );
        } else if ( pntXYR.length > 2 ) {
            // render intermediate points
            for ( int i = 2, j = pntXYR.length; i < j; i++ ) {
                renderGraphicOrMark( graphics, stroke.stroke, shape, pntXYR[i][0], pntXYR[i][1],
                                     ( posRotate ? pntXYR[i][2] : 0 ), uom, applyOffsets, applyFill, applyStroke );
            }
        }
    }

    private void renderGraphicOrMark( Graphics2D graphics, Graphic stroke, Shape mark, double x, double y,
                                      double rotate, UOM uom, boolean applyOffsets, boolean applyFill,
                                      boolean applyStroke ) {
        AffineTransform t = graphics.getTransform();

        // TRICKY rotation of shape is made on load (rotated around center of shape)
        // TRICKY this rotation is used to get back regular rotation
        if ( !isZero( rotate ) ) {
            graphics.rotate( rotate, x, y );
        }

        if ( stroke.image != null ) {
            // rotate, because an image was not rotated previously
            if ( !isZero( stroke.rotation ) ) {
                graphics.rotate( toRadians(stroke.rotation), x, y );
            }
            // render image
            Rectangle2D.Double rect = fillRenderer.getImageBounds( stroke.image, stroke, x, y, uom );
            graphics.drawImage( stroke.image, round( rect.x ), round( rect.y ), round( rect.width ),
                                round( rect.height ), null );
        } else {
            // render mark
            double size = stroke.size <= 0 ? 6 : uomCalculator.considerUOM( stroke.size, uom );
            if ( applyOffsets ) {
                double x0 = x - size * stroke.anchorPointX + uomCalculator.considerUOM( stroke.displacementX, uom );
                double y0 = y - size * stroke.anchorPointY + uomCalculator.considerUOM( stroke.displacementY, uom );
                graphics.translate( x0, y0 );
            } else {
                graphics.translate( x - size / 2, y - size );
            }

            if ( stroke.mark.fill != null && applyFill ) {
                fillRenderer.applyFill( stroke.mark.fill, uom );
                graphics.fill( mark );
            }

            if ( stroke.mark.stroke != null && applyStroke ) {
                applyStroke( stroke.mark.stroke, uom, mark, 0, null );
                graphics.draw( mark );
            }

        }
        graphics.setTransform( t );
    }
    
    /**
     * Return a new two-dimensional array of point ( x, y, angle )
     * 
     * @param shape
     * @param startpnt
     * @param endpnt
     * @param positionPercentage
     * @param initialGap
     * @param advance
     * @param fallbackPercentage
     *            if inital gap is longer than line fall back to positionPercentage or 50 if positionPercentage is not
     *            set
     * 
     * @return array of point-data ( x, y, angle ) first and second element is used for start or end point, the third
     *         will be on positionPercentage
     */
    private double[][] calculateAnchorPoints( Shape shape, boolean startpnt, boolean endpnt, double positionPercentage,
                                        double initialGap, double advance, boolean fallbackPercentage ) {
        PathIterator it = new FlatteningPathIterator( shape.getPathIterator( null ), 1 );

        boolean needSecondIter = ( positionPercentage >= 0 || advance > 1 );

        // do not iterate a second time if only start or end point is required
        if ( startpnt || endpnt )
            needSecondIter = false;

        double[] startp = new double[4];
        double[] endp = new double[4];

        double totalLength = 0;
        float lastX = 0, lastY = 0, lLastX = 0, lLastY = 0;
        float points[] = new float[6];
        int type = 0;
        float moveX = 0, moveY = 0;

        if ( needSecondIter || endpnt || startpnt ) {
            int cnt = 0;

            while ( !it.isDone() ) {
                cnt++;
                type = it.currentSegment( points );
                switch ( type ) {
                case PathIterator.SEG_MOVETO:
                    lLastX = lastX;
                    lLastY = lastY;
                    lastX = moveX = points[0];
                    lastY = moveY = points[1];
                    break;

                case PathIterator.SEG_CLOSE:
                    // close element
                    points[0] = moveX;
                    points[1] = moveY;

                case PathIterator.SEG_LINETO:
                    totalLength += sqrt( ( lastX - points[0] ) * ( lastX - points[0] ) + ( lastY - points[1] )
                                         * ( lastY - points[1] ) );
                    lLastX = lastX;
                    lLastY = lastY;
                    lastX = points[0];
                    lastY = points[1];
                    break;
                }
                it.next();

                if ( cnt == 2 && startpnt ) {
                    startp[0] = lLastX;
                    startp[1] = lLastY;
                    double dx = lastX - lLastX;
                    double dy = lastY - lLastY;
                    // rotate 90 deegree to the left
                    startp[2] = atan2( dy, dx ) - PI / 2;
                    startp[3] = 1;

                    if ( !( needSecondIter || endpnt ) ) {
                        // stop iterator if only startpoint is needed
                        break;
                    }
                }
            }

            if ( cnt >= 2 && endpnt ) {
                endp[0] = lastX;
                endp[1] = lastY;
                double dx = lLastX - lastX;
                double dy = lLastY - lastY;
                // rotate 90 deegree to the left
                endp[2] = atan2( dy, dx ) - PI / 2;
                endp[3] = 1;
            }

            it = new FlatteningPathIterator( shape.getPathIterator( null ), 1 );
        }

        List<double[]> pnts = new ArrayList<double[]>();
        pnts.add( startp );
        pnts.add( endp );

        if ( needSecondIter ) {
            float thisX = 0, thisY = 0;
            float next = 0;
            float minLength = (float) abs( initialGap );
            boolean repeat = true;
            if ( positionPercentage > 100 || isZero( positionPercentage - 100.0d )) {
                // every value above 100 is treated as 100
                minLength = (float) totalLength;
                next = minLength;
                repeat = false;
            } else if ( positionPercentage >= 0 ) {
                minLength = (float) ( totalLength * ( ( positionPercentage % 100 ) / 100 ) );
                next = minLength;
                repeat = false;
            }

            if ( repeat && initialGap < 0 ) {
                double tmp = ( totalLength + initialGap ) % advance;
                if ( tmp + initialGap > 0 ) {
                    minLength += (float) ( ( tmp + initialGap ) / 2.0 );
                }
            }

            if ( fallbackPercentage && minLength > totalLength ) {
                if ( positionPercentage > 100 || isZero( positionPercentage - 100.0d )) {
                    minLength = (float)totalLength;
                } else if ( positionPercentage >= 0 ) {
                    minLength = (float) ( totalLength * ( ( positionPercentage % 100 ) / 100 ) );
                } else {
                    // positionPercentage is not set (<0) so fall back to 50%
                    minLength = (float) ( totalLength * 0.5d );
                }
                next = minLength;
                repeat = false;
            }

            while ( !it.isDone() ) {
                type = it.currentSegment( points );
                switch ( type ) {
                case PathIterator.SEG_MOVETO:
                    moveX = lastX = points[0];
                    moveY = lastY = points[1];
                    // result.moveTo( moveX, moveY );
                    next = minLength;
                    break;

                case PathIterator.SEG_CLOSE:
                    // close ring
                    points[0] = moveX;
                    points[1] = moveY;

                case PathIterator.SEG_LINETO:
                    thisX = points[0];
                    thisY = points[1];
                    float dx = thisX - lastX;
                    float dy = thisY - lastY;
                    float distance = (float) sqrt( dx * dx + dy * dy );
                    if ( distance >= next ) {
                        float r = 1.0f / distance;
                        double angle = atan2( dy, dx );
                        // while ( currentShape < length && distance >= next ) {
                        while ( distance >= next ) {
                            float x = lastX + next * dx * r;
                            float y = lastY + next * dy * r;
                            double[] elem = new double[3];
                            elem[0] = x;
                            elem[1] = y;
                            elem[2] = angle;
                            pnts.add( elem );

                            // do not loop if less than a half pixel
                            // performance !
                            if ( !repeat || advance < 0.5 )
                                break;

                            next += advance;
                        }
                    }
                    next -= distance;
                    lastX = thisX;
                    lastY = thisY;
                    break;
                }

                // no more symbols to set break
                if ( next < 0 )
                    break;

                it.next();
            }
        }

        return pnts.toArray( new double[pnts.size()][] );
    }

    private void applyNormalStroke( Stroke stroke, UOM uom, Shape object, double perpendicularOffset,
                                    PerpendicularOffsetType type ) {
        int linecap = getLinecap( stroke );
        float miterLimit = TunableParameter.get( "deegree.rendering.stroke.miterlimit", 10f );
        int linejoin = getLinejoin( stroke );
        float dashoffset = (float) uomCalculator.considerUOM( stroke.dashoffset, uom );
        float[] dasharray = stroke.dasharray == null ? null : new float[stroke.dasharray.length];
        if ( dasharray != null ) {
            for ( int i = 0; i < stroke.dasharray.length; ++i ) {
                dasharray[i] = (float) uomCalculator.considerUOM( stroke.dasharray[i], uom );
            }
        }

        BasicStroke bs = new BasicStroke( (float) uomCalculator.considerUOM( stroke.width, uom ), linecap, linejoin,
                                          miterLimit, dasharray, dashoffset );
        double poff = uomCalculator.considerUOM( perpendicularOffset, uom );
        if ( !isZero( poff ) ) {
            graphics.setStroke( new OffsetStroke( poff, bs, type ) );
        } else {
            graphics.setStroke( bs );
        }
    }

    private int getLinecap( Stroke stroke ) {
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
        return linecap;
    }

    private int getLinejoin( Stroke stroke ) {
        int linejoin = JOIN_MITER;
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
        return linejoin;
    }

}
