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
package org.deegree.tools.crs.georeferencing.model;

import java.util.ArrayList;
import java.util.List;

import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.standard.points.PointsList;
import org.deegree.tools.crs.georeferencing.application.Scene2DValues;

/**
 * 
 * Model of the footprint of a 3D building. Basis for georeferencing.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Footprint {

    public final static double EPS10 = 1e-10;

    public final static double EP10 = 1e+10;

    private List<Ring> worldCoordinateRingList;

    private GeometryFactory geom;

    private Scene2DValues values;

    /**
     * Creates a new <Code>Footprint</Code> instance.
     */
    public Footprint( Scene2DValues values, GeometryFactory geom ) {
        this.values = values;
        this.geom = geom;
    }

    /**
     * Generates the polygons in world- and pixel-coordinates.
     * 
     * @param footprintPointsList
     *            the points from the <Code>WorldRenderableObject</Code>
     */
    public void generateFootprints( List<float[]> footprintPointsList ) {
        worldCoordinateRingList = new ArrayList<Ring>();
        List<Point> pointList;
        int size = 0;
        for ( float[] f : footprintPointsList ) {
            size += f.length / 3;
        }
        double minX = EP10;
        double minY = EP10;
        double maxX = EPS10;
        double maxY = EPS10;

        for ( float[] f : footprintPointsList ) {
            pointList = new ArrayList<Point>();
            int polygonSize = f.length / 3;

            double[] x = new double[polygonSize];
            double[] y = new double[polygonSize];
            int count = 0;

            // get all points in 2D, so z-axis is omitted
            for ( int i = 0; i < f.length; i += 3 ) {
                x[count] = f[i];
                y[count] = f[i + 1];
                if ( minX > x[count] ) {
                    minX = x[count];
                }
                if ( minY > y[count] ) {
                    minY = y[count];
                }
                if ( maxX < x[count] ) {
                    maxX = x[count];
                }
                if ( maxY < y[count] ) {
                    maxY = y[count];
                }

                pointList.add( geom.createPoint( "point", x[count], y[count], null ) );

                count++;

            }
            Points points = new PointsList( pointList );
            worldCoordinateRingList.add( geom.createLinearRing( "ring", null, points ) );
        }
        System.out.println( "[Footprint] " + minX + " " + minY + " " + maxX + " " + maxY );
        this.values.setEnvelopeFootprint( geom.createEnvelope( minX, minY, maxX, maxY, null ) );

    }

    /**
     * 
     * @return the list of polygons in world-coordinates
     */
    public List<Ring> getWorldCoordinateRingList() {
        return worldCoordinateRingList;
    }

}
