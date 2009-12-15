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

package org.deegree.rendering.r2d.strokes;

import static java.awt.geom.PathIterator.SEG_CLOSE;
import static java.awt.geom.PathIterator.SEG_CUBICTO;
import static java.awt.geom.PathIterator.SEG_LINETO;
import static java.awt.geom.PathIterator.SEG_MOVETO;
import static java.awt.geom.PathIterator.SEG_QUADTO;
import static java.lang.Math.sqrt;
import static org.deegree.commons.utils.math.MathUtils.isZero;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.LinkedList;

import org.deegree.commons.utils.Pair;
import org.deegree.rendering.r2d.styling.components.PerpendicularOffsetType;
import org.slf4j.Logger;

/**
 * <code>OffsetStroke</code>
 * 
 * Idea: it would be good to combine the offset line and the line width line. In that case the offset and line width
 * could be adjusted dynamically when angles are too small.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OffsetStroke implements Stroke {

    private static final Logger LOG = getLogger( OffsetStroke.class );

    private final double offset;

    private final Stroke stroke;

    private PerpendicularOffsetType type;

    /**
     * @param offset
     * @param stroke
     * @param type
     *            may be null, default is Standard
     */
    public OffsetStroke( double offset, Stroke stroke, PerpendicularOffsetType type ) {
        if ( type == null ) {
            type = new PerpendicularOffsetType();
        }
        this.offset = offset;
        this.stroke = stroke;
        this.type = type;
    }

    private static final double[] calcNormal( final double x1, final double y1, final double x2, final double y2,
                                              final double[] last ) {
        final double nx = x2 - x1;
        final double ny = y2 - y1;
        if ( isZero( nx ) && isZero( ny ) ) {
            if ( last != null ) {
                LOG.debug( "Two subsequent points in a curve have been the same. Using the last normal..." );
                LOG.debug( "Please implement proper generalization so this won't happen..." );
                return last;
            }
            LOG.warn( "Two subsequent points in a curve have been the same. Using the first of the two points instead of the normal..." );
            return new double[] { x1, y1 };
        }
        final double len = sqrt( nx * nx + ny * ny );
        return new double[] { -ny / len, nx / len };
    }

    private final double[] calcIntersection( final double px, final double py, final double[] n1, final double[] n2 ) {
        double nx = px + offset * n1[0];
        double ny = py + offset * n1[1];
        if ( n2 == null ) {
            return new double[] { nx, ny };
        }

        // calc intersection point of the two lines that are parallel to the original geometry lines
        double ox = px + offset * n2[0];
        double oy = py + offset * n2[1];
        double lam = ( n1[1] * ( oy - ny ) + n1[0] * ( ox - nx ) ) / ( n2[0] * n1[1] - n1[0] * n2[1] );
        return new double[] { ox + lam * n2[1], oy - lam * n2[0] };
    }

    public Shape createStrokedShape( final Shape p ) {
        LinkedList<Pair<Integer, double[]>> list = new LinkedList<Pair<Integer, double[]>>();
        LinkedList<double[]> normals = new LinkedList<double[]>();

        PathIterator i = p.getPathIterator( null );

        while ( !i.isDone() ) {
            double[] ps = new double[6];
            list.add( new Pair<Integer, double[]>( i.currentSegment( ps ), ps ) );
            i.next();
        }

        double firstx = list.peek().second[0], firsty = list.peek().second[1];

        // calc normals
        double lastx = 0, lasty = 0;

        double[] last = null;

        for ( Pair<Integer, double[]> pair : list ) {
            switch ( pair.first ) {
            case SEG_CLOSE:
                normals.add( last = calcNormal( lastx, lasty, firstx, firsty, last ) );
                break;
            case SEG_CUBICTO:
                normals.add( last = calcNormal( lastx, lasty, pair.second[0], pair.second[1], last ) );
                normals.add( last = calcNormal( pair.second[0], pair.second[1], pair.second[2], pair.second[3], last ) );
                normals.add( last = calcNormal( pair.second[2], pair.second[3], pair.second[4], pair.second[5], last ) );
                lastx = pair.second[4];
                lasty = pair.second[5];
                break;
            case SEG_LINETO:
                normals.add( last = calcNormal( lastx, lasty, pair.second[0], pair.second[1], last ) );
                lastx = pair.second[0];
                lasty = pair.second[1];
                break;
            case SEG_MOVETO:
                lastx = pair.second[0];
                lasty = pair.second[1];
                break;
            case SEG_QUADTO:
                normals.add( last = calcNormal( lastx, lasty, pair.second[0], pair.second[1], last ) );
                normals.add( last = calcNormal( pair.second[0], pair.second[1], pair.second[2], pair.second[3], last ) );
                lastx = pair.second[2];
                lasty = pair.second[3];
                break;
            }
        }

        // calc new path
        // ATTENTION: at least for cubic to this does not work! VM crash...
        double[] firstNormal = normals.peek();
        if ( last == null ) {
            last = normals.peekLast();
        }

        boolean firstMove = true;

        Path2D.Double path = new Path2D.Double();
        for ( Pair<Integer, double[]> pair : list ) {
            switch ( pair.first ) {
            case SEG_CLOSE:
                switch ( type.type ) {
                case Edged:
                    double[] n = new double[] { last[0] + firstNormal[0], last[1] + firstNormal[1] };
                    double len = sqrt( n[0] * n[0] + n[1] * n[1] );
                    n[0] /= len;
                    n[1] /= len;
                    double[] n1 = calcIntersection( firstx, firsty, last, n );
                    maybeLineTo( path, n1[0], n1[1] );
                    n1 = calcIntersection( firstx, firsty, n, firstNormal );
                    maybeLineTo( path, n1[0], n1[1] );
                    path.closePath();
                    break;
                case Round:
                    double[] p1 = new double[] { firstx + last[0] * offset, firsty + last[1] * offset };
                    maybeLineTo( path, p1[0], p1[1] );
                    double[] p2 = new double[] { firstx + firstNormal[0] * offset, firsty + firstNormal[1] * offset };
                    double[] midp = new double[] { p1[0] + firstNormal[0] * offset, p1[1] + firstNormal[1] * offset };
                    path.quadTo( midp[0], midp[1], p2[0], p2[1] );
                    path.closePath();
                    break;
                case Standard:
                    double[] pt = calcIntersection( firstx, firsty, last, firstNormal );
                    maybeLineTo( path, pt[0], pt[1] );
                    path.closePath();
                    break;
                }
                break;
            case SEG_CUBICTO:
                double[] n1 = normals.poll();
                double[] n2 = normals.poll();
                double[] n3 = normals.poll();
                n1 = calcIntersection( pair.second[0], pair.second[1], n1, n2 );
                n2 = calcIntersection( pair.second[2], pair.second[3], n2, n3 );
                n3 = calcIntersection( pair.second[4], pair.second[5], n3, normals.peek() );
                path.curveTo( n1[0], n1[1], n2[0], n2[1], n3[0], n3[1] );
                break;
            case SEG_LINETO:
                n1 = normals.poll();
                n2 = normals.peek();
                if ( n1 == n2 ) {
                    continue;
                }
                switch ( type.type ) {
                case Edged:
                    if ( n2 == null ) {
                        continue;
                    }
                    double[] n = new double[] { n1[0] + n2[0], n1[1] + n2[1] };
                    double len = sqrt( n[0] * n[0] + n[1] * n[1] );
                    n[0] /= len;
                    n[1] /= len;
                    n1 = calcIntersection( pair.second[0], pair.second[1], n1, n );
                    maybeLineTo( path, n1[0], n1[1] );
                    n1 = calcIntersection( pair.second[0], pair.second[1], n, n2 );
                    maybeLineTo( path, n1[0], n1[1] );
                    continue;
                case Round:
                    double[] p1 = new double[] { pair.second[0] + n1[0] * offset, pair.second[1] + n1[1] * offset };
                    maybeLineTo( path, p1[0], p1[1] );
                    if ( n2 == null ) {
                        continue;
                    }
                    double[] p2 = new double[] { pair.second[0] + n2[0] * offset, pair.second[1] + n2[1] * offset };
                    double[] midp = new double[] { p1[0] + n2[0] * offset, p1[1] + n2[1] * offset };
                    path.quadTo( midp[0], midp[1], p2[0], p2[1] );
                    continue;
                case Standard:
                    n1 = calcIntersection( pair.second[0], pair.second[1], n1, n2 );
                    if ( firstMove ) {
                        path.moveTo( n1[0], n1[1] );
                        firstMove = false;
                    } else {
                        maybeLineTo( path, n1[0], n1[1] );
                    }
                    continue;
                }
                continue;
            case SEG_MOVETO:
                n1 = normals.peek();
                path.moveTo( pair.second[0] + n1[0] * offset, pair.second[1] + n1[1] * offset );
                break;
            case SEG_QUADTO:
                n1 = normals.poll();
                n2 = normals.poll();
                n1 = calcIntersection( pair.second[0], pair.second[1], n1, n2 );
                n2 = calcIntersection( pair.second[2], pair.second[3], n2, normals.peek() );
                path.quadTo( n1[0], n1[1], n2[0], n2[1] );
                break;
            }
        }

        Shape res = stroke == null ? path : stroke.createStrokedShape( path );

        switch ( type.substraction ) {
        case None:
            break;
        case NegativeOffset:
            Shape substractMe = new OffsetStroke( -offset, null, null ).createStrokedShape( p );
            substractMe = stroke == null ? substractMe : stroke.createStrokedShape( substractMe );
            Area a = new Area( res );
            a.subtract( new Area( substractMe ) );
            res = a;
            break;
        }

        return res;
    }

    private static void maybeLineTo( Path2D path, double x, double y ) {
        if ( Double.isNaN( x ) || Double.isNaN( y ) ) {
            LOG.debug( "NaN detected!" );
            return;
        }
        path.lineTo( x, y );
    }

}
