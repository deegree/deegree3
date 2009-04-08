//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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

package org.deegree.rendering.r2d;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.Math.PI;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedList;

import org.deegree.rendering.r2d.styling.components.Mark;

/**
 * <code>RenderHelper</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RenderHelper {

    /**
     * Example: calculateStarPolygon (5, 2) connects the first, third, fifth, second, fourth and again first edge in
     * that order.
     * 
     * @param edges
     *            the number of edges
     * @param skip
     *            specifies, how to connect the edges
     * @param size
     * @return a general path that draws the star polygon by the rule
     */
    public static Path2D.Double calculateStarPolygon( final int edges, final int skip, final double size ) {
        // calculate from circle around (0, 0)
        // start with the point on the top
        // then, for subsequent points, use the addition theroem
        // after calculating the points, move the circle back from (0, 0) inside the rect

        double[] xs = new double[edges];
        double[] ys = new double[edges];
        double half = size / 2;
        xs[0] = 0;
        ys[0] = -half;
        double ang = PI * 2 / edges;
        double sinbeta = Math.sin( ang );
        double cosbeta = Math.cos( ang );

        for ( int i = 1; i < edges; ++i ) {
            xs[i] = xs[i - 1] * cosbeta - ys[i - 1] * sinbeta;
            ys[i] = xs[i - 1] * sinbeta + ys[i - 1] * cosbeta;
        }

        for ( int i = 0; i < edges; ++i ) {
            xs[i] += half;
            ys[i] += half;
        }

        // build the actual star by drawing lines between the points in the proper order
        HashSet<Integer> visited = new HashSet<Integer>( edges );
        Path2D.Double path = new Path2D.Double();
        for ( int i = 0; i < skip; ++i ) {
            int k = i;
            if ( visited.contains( k ) ) {
                continue;
            }
            path.moveTo( xs[k], ys[k] );
            visited.add( k );
            while ( k < edges ) {
                k += skip;
                if ( k < edges ) {
                    path.lineTo( xs[k], ys[k] );
                    visited.add( k );
                } else {
                    path.lineTo( xs[k - edges], ys[k - edges] );
                    if ( !visited.contains( k - edges ) ) {
                        k -= edges;
                        visited.add( k );
                    }
                }
            }
        }

        path.closePath();
        return path;
    }

    /**
     * @param mark
     * @param size
     * @return the mark rendered as buffered image in the specified size
     */
    public static BufferedImage renderMark( Mark mark, int size ) {
        if ( mark.fill == null && mark.stroke == null ) {
            return new BufferedImage( size, size, TYPE_INT_ARGB );
        }

        BufferedImage img = new BufferedImage( size, size, TYPE_INT_ARGB );
        Graphics2D g = img.createGraphics();

        Java2DRenderer renderer = new Java2DRenderer( g, size, size, null );

        double sizem1 = size - 1;

        LinkedList<Shape> shapes = new LinkedList<Shape>();
        switch ( mark.wellKnown ) {
        case CIRCLE:
            shapes.add( new Ellipse2D.Double( 0, 0, sizem1, sizem1 ) );
            break;
        case CROSS: {
            double half = sizem1 / 2;
            shapes.add( new Line2D.Double( half, 0, half, sizem1 ) );
            shapes.add( new Line2D.Double( 0, half, sizem1, half ) );
            break;
        }
        case SQUARE:
            shapes.add( new Rectangle2D.Double( 0, 0, sizem1, sizem1 ) );
            break;
        case STAR: {
            shapes.add( calculateStarPolygon( 5, 2, sizem1 ) );
            break;
        }
        case TRIANGLE:
            Path2D.Double path = new Path2D.Double();
            path.moveTo( sizem1 / 2, 0 );
            path.lineTo( 0, sizem1 );
            path.lineTo( sizem1, sizem1 );
            path.closePath();
            shapes.add( path );
            break;
        case X:
            shapes.add( new Line2D.Double( 0, 0, sizem1, sizem1 ) );
            shapes.add( new Line2D.Double( sizem1, 0, 0, sizem1 ) );
            break;
        }

        if ( mark.fill != null ) {
            renderer.applyFill( mark.fill );
            for ( Shape shape : shapes ) {
                g.fill( shape );
            }
        }
        if ( mark.stroke != null ) {
            renderer.applyStroke( mark.stroke );
            for ( Shape shape : shapes ) {
                // TODO remove this ugly hack to prevent having it look so bad
                if ( shape instanceof Path2D ) {
                    AffineTransform t = new AffineTransform();
                    t.scale( 0.99, .99 );
                    shape = ( (Path2D) shape ).createTransformedShape( t );
                }
                g.draw( shape );
            }
        }

        g.dispose();

        return img;
    }

}
