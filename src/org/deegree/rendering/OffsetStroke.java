//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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

import static java.awt.geom.PathIterator.SEG_CLOSE;
import static java.awt.geom.PathIterator.SEG_CUBICTO;
import static java.awt.geom.PathIterator.SEG_LINETO;
import static java.awt.geom.PathIterator.SEG_MOVETO;
import static java.awt.geom.PathIterator.SEG_QUADTO;
import static java.lang.Math.sqrt;

import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.LinkedList;

import org.deegree.commons.utils.Pair;

/**
 * <code>OffsetStroke</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OffsetStroke implements Stroke {

    private final double offset;

    private final Stroke stroke;

    /**
     * @param offset
     * @param stroke
     */
    public OffsetStroke( double offset, Stroke stroke ) {
        this.offset = offset;
        this.stroke = stroke;
    }

    private static final double[] calcNormal( final double x1, final double y1, final double x2, final double y2 ) {
        final double nx = x2 - x1;
        final double ny = y2 - y1;
        final double len = sqrt( nx * nx + ny * ny );
        return new double[] { ny / len, -nx / len };
    }

    private final double[] calcNewInner( final double px, final double py, final double[] n1, final double[] n2 ) {
        double nx = px + offset * n1[0];
        double ny = py + offset * n1[1];
        if ( n2 == null ) {
            return new double[] { nx, ny };
        }

        // calc intersection point of the two lines that are parallel to the original geometry lines
        double ox = px + offset * n2[0];
        double oy = py + offset * n2[1];
        double lam = ( n1[1] * oy - ny * n1[1] + n1[0] * ox - nx * n1[0] ) / ( n2[0] * n1[1] - n1[0] * n2[1] );
        return new double[] { ox + lam * n2[1], oy - lam * n2[0] };
    }

    public Shape createStrokedShape( Shape p ) {
        LinkedList<Pair<Integer, double[]>> list = new LinkedList<Pair<Integer, double[]>>();
        LinkedList<double[]> normals = new LinkedList<double[]>();

        PathIterator i = p.getPathIterator( null );

        while ( !i.isDone() ) {
            double[] ps = new double[6];
            list.add( new Pair<Integer, double[]>( i.currentSegment( ps ), ps ) );
            i.next();
        }

        // calc normals
        double lastx = 0, lasty = 0;

        for ( Pair<Integer, double[]> pair : list ) {
            switch ( pair.first ) {
            case SEG_CLOSE:
                break;
            case SEG_CUBICTO:
                normals.add( calcNormal( lastx, lasty, pair.second[0], pair.second[1] ) );
                normals.add( calcNormal( pair.second[0], pair.second[1], pair.second[2], pair.second[3] ) );
                normals.add( calcNormal( pair.second[2], pair.second[3], pair.second[4], pair.second[5] ) );
                lastx = pair.second[4];
                lasty = pair.second[5];
                break;
            case SEG_LINETO:
                normals.add( calcNormal( lastx, lasty, pair.second[0], pair.second[1] ) );
                lastx = pair.second[0];
                lasty = pair.second[1];
                break;
            case SEG_MOVETO:
                lastx = pair.second[0];
                lasty = pair.second[1];
                break;
            case SEG_QUADTO:
                normals.add( calcNormal( lastx, lasty, pair.second[0], pair.second[1] ) );
                normals.add( calcNormal( pair.second[0], pair.second[1], pair.second[2], pair.second[3] ) );
                lastx = pair.second[2];
                lasty = pair.second[3];
                break;
            }
        }

        // calc new path
        Path2D.Double path = new Path2D.Double();
        for ( Pair<Integer, double[]> pair : list ) {
            switch ( pair.first ) {
            case SEG_CLOSE:
                path.closePath();
                break;
            case SEG_CUBICTO:
                double[] n1 = normals.poll();
                double[] n2 = normals.poll();
                double[] n3 = normals.poll();
                n1 = calcNewInner( pair.second[0], pair.second[1], n1, n2 );
                n2 = calcNewInner( pair.second[2], pair.second[3], n2, n3 );
                n3 = calcNewInner( pair.second[4], pair.second[5], n3, normals.peek() );
                path.curveTo( n1[0], n1[1], n2[0], n2[1], n3[0], n3[1] );
                break;
            case SEG_LINETO:
                n1 = calcNewInner( pair.second[0], pair.second[1], normals.poll(), normals.peek() );
                path.lineTo( n1[0], n1[1] );
                break;
            case SEG_MOVETO:
                n1 = normals.peek();
                path.moveTo( pair.second[0] + n1[0] * offset, pair.second[1] + n1[1] * offset );
                break;
            case SEG_QUADTO:
                n1 = normals.poll();
                n2 = normals.poll();
                n1 = calcNewInner( pair.second[0], pair.second[1], n1, n2 );
                n2 = calcNewInner( pair.second[2], pair.second[3], n2, normals.peek() );
                path.quadTo( n1[0], n1[1], n2[0], n2[1] );
                break;
            }
        }

        return stroke.createStrokedShape( path );
    }

}
