//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.rendering.r2d.utils;

import static java.util.Arrays.asList;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.standard.points.PointsList;

/**
 * Creates a Feature from a Raster. The resulting feature contains the raster outline as a geometry and other
 * information about the raster as attributes.
 * 
 * @author <a href="mailto:a.aiordachioaie@jacobs-university.de">Andrei Aiordachioaie</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Raster2Feature {

    /**
     * Return the bounding geometry of a raster as a polygon (but is actually a rectangle).
     * 
     * @param raster
     * @return bounding polygon
     */
    public static Polygon createPolygonGeometry( AbstractRaster raster ) {
        CRS crs = raster.getCoordinateSystem();
        GeometryFactory fac = new GeometryFactory();
        Envelope env = raster.getEnvelope();
        env = raster.getRasterReference().relocateEnvelope( OriginLocation.OUTER, env );
        Point pmin = env.getMin();
        Point pmax = env.getMax();
        Point p1 = fac.createPoint( null, pmin.get0(), pmin.get1(), crs );
        Point p3 = fac.createPoint( null, pmax.get0(), pmax.get1(), crs );

        Point p2 = fac.createPoint( null, p1.get0(), p3.get1(), crs );
        Point p4 = fac.createPoint( null, p3.get0(), p1.get1(), crs );
        Point p5 = fac.createPoint( null, p1.get0(), p1.get1(), crs );
        Point[] points = { p1, p2, p3, p4, p5 };
        // (asList(points));
        LinearRing ring = fac.createLinearRing( null, crs, new PointsList( asList( points ) ) );
        Polygon poly = fac.createPolygon( null, crs, ring, null );
        return poly;
    }

}
