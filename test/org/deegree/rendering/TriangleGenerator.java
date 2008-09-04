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

import static java.util.Collections.singletonList;
import static org.deegree.model.geometry.GeometryFactoryCreator.getInstance;
import static org.deegree.model.geometry.primitive.Curve.ORIENTATION.unknown;

import java.util.Random;

import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.SurfacePatch;

/**
 * <code>TriangleGenerator</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TriangleGenerator {

    private static final Random rnd = new Random();

    private static final GeometryFactory fac = getInstance().getGeometryFactory();

    /**
     * @param max
     *            generate points between 0 and max
     * @return a random triangle polygon
     */
    public static Surface randomTriangle( int max ) {
        double x = rnd.nextDouble() * max;
        double y = rnd.nextDouble() * max;
        Point[][] ps = { { fac.createPoint( new double[] { x, y }, null ),
                          fac.createPoint( new double[] { rnd.nextDouble() * max, rnd.nextDouble() * max }, null ),
                          fac.createPoint( new double[] { rnd.nextDouble() * max, rnd.nextDouble() * max }, null ),
                          fac.createPoint( new double[] { x, y }, null ) } };
        Curve curve = fac.createCurve( ps, unknown, null );
        SurfacePatch patch = fac.createSurfacePatch( singletonList( curve ) );
        return fac.createSurface( singletonList( patch ), null );
    }

}
