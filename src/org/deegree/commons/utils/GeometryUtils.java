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

package org.deegree.commons.utils;

import java.util.LinkedList;

import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.curvesegments.CurveSegment;
import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;
import org.deegree.model.geometry.primitive.surfacepatches.SurfacePatch;

/**
 * <code>GeometryUtils</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeometryUtils {

    /**
     * Moves the coordinates of a geometry.
     * 
     * @param geom
     *            use only surfaces, curves or points, and only with dim == 2
     * @param offx
     * @param offy
     * @return the moved geometry
     */
    public static Geometry move( Geometry geom, double offx, double offy ) {
        GeometryFactory fac = GeometryFactoryCreator.getInstance().getGeometryFactory();
        if ( geom instanceof Point ) {
            Point p = (Point) geom;
            return fac.createPoint( geom.getId(), new double[] { p.getX() + offx, p.getY() + offy },
                                    p.getCoordinateSystem() );
        }
        if ( geom instanceof Curve ) {
            Curve c = (Curve) geom;
            LinkedList<Point> ps = new LinkedList<Point>();
            if ( c.getCurveSegments().size() != 1 || !( c.getCurveSegments().get( 0 ) instanceof LineStringSegment ) ) {
                // TODO handle non-linear and multiple curve segments
                throw new IllegalArgumentException();
            }
            LineStringSegment segment = ( (LineStringSegment) c.getCurveSegments().get( 0 ) );
            for ( Point p : segment.getControlPoints() ) {
                ps.add( (Point) move( p, offx, offy ) );
            }
            return fac.createCurve( geom.getId(), new CurveSegment[] { fac.createLineStringSegment( ps ) },
                                    c.getCoordinateSystem() );
        }
        if ( geom instanceof Surface ) {
            Surface s = (Surface) geom;
            LinkedList<SurfacePatch> patches = new LinkedList<SurfacePatch>();
            for ( SurfacePatch patch : s.getPatches() ) {
                LinkedList<Curve> curves = new LinkedList<Curve>();
                for ( Curve c : patch.getBoundary() ) {
                    curves.add( (Curve) move( c, offx, offy ) );
                }
                patches.add( fac.createSurfacePatch( curves ) );
            }
            return fac.createSurface( geom.getId(), patches, s.getCoordinateSystem() );
        }
        return geom;
    }
}
