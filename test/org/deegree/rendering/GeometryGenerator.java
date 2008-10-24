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

import static java.util.Arrays.asList;
import static org.deegree.model.geometry.GeometryFactoryCreator.getInstance;

import java.util.Random;

import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Polygon;

/**
 * <code>GeometryGenerator</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeometryGenerator {

    private static final Random rnd = new Random();

    private static final GeometryFactory fac = getInstance().getGeometryFactory();

    /**
     * @param max
     *            generate points between offx/offy and max + offx/offy
     * @param offx
     * @param offy
     * @return a random triangle polygon
     */
    public static Polygon randomTriangle( int max, double offx, double offy ) {
        double x = rnd.nextDouble() * max + offx;
        double y = rnd.nextDouble() * max + offy;
        Point[] ps = {
                      fac.createPoint( null, new double[] { x, y }, null ),
                      fac.createPoint( null, new double[] { rnd.nextDouble() * max + offx,
                                                           rnd.nextDouble() * max + offy }, null ),
                      fac.createPoint( null, new double[] { rnd.nextDouble() * max + offx,
                                                           rnd.nextDouble() * max + offy }, null ),
                      fac.createPoint( null, new double[] { x, y }, null ) };
        return fac.createPolygon( null, null, fac.createLinearRing( null, null, asList( ps ) ), null );
    }

    /**
     * @param max
     * @param offx
     * @param offy
     * @return a random point
     */
    public static Point randomPoint( int max, double offx, double offy ) {
        double x = rnd.nextDouble() * max + offx;
        double y = rnd.nextDouble() * max + offy;
        return fac.createPoint( null, new double[] { x, y }, null );
    }

    /**
     * @param max
     * @param offx
     * @param offy
     * @return a random polygon with an edge in each quadrant
     */
    public static Polygon randomQuad( int max, double offx, double offy ) {
        double half = max / 2;
        double x = rnd.nextDouble() * half + offx;
        double y = rnd.nextDouble() * half + offy;
        Point[] ps = {
                      fac.createPoint( null, new double[] { x, y }, null ),
                      fac.createPoint( null, new double[] { rnd.nextDouble() * half + half + offx,
                                                           rnd.nextDouble() * half + offy }, null ),
                      fac.createPoint( null, new double[] { rnd.nextDouble() * half + half + offx,
                                                           rnd.nextDouble() * half + half + offy }, null ),
                      fac.createPoint( null, new double[] { rnd.nextDouble() * half + offx,
                                                           rnd.nextDouble() * half + half + offy }, null ),
                      fac.createPoint( null, new double[] { x, y }, null ) };
        return fac.createPolygon( null, null, fac.createLinearRing( null, null, asList( ps ) ), null );
    }

    /**
     * @param max
     * @param offx
     * @param offy
     * @return a curve similar to the points of #randomQuad (but without the last)
     */
    public static Curve randomCurve( int max, double offx, double offy ) {
        double half = max / 2;
        double x = rnd.nextDouble() * half + offx;
        double y = rnd.nextDouble() * half + offy;
        Point[] ps = {
                      fac.createPoint( null, new double[] { x, y }, null ),
                      fac.createPoint( null, new double[] { rnd.nextDouble() * half + half + offx,
                                                           rnd.nextDouble() * half + offy }, null ),
                      fac.createPoint( null, new double[] { rnd.nextDouble() * half + half + offx,
                                                           rnd.nextDouble() * half + half + offy }, null ),
                      fac.createPoint( null, new double[] { rnd.nextDouble() * half + offx,
                                                           rnd.nextDouble() * half + half + offy }, null ) };
        return fac.createLineString( null, null, asList( ps ) );
    }

}
