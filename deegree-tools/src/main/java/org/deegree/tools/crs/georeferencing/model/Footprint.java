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

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;

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

    private Polygon polygon;

    private List<Polygon> worldCoordinatePolygonList;

    private double[] worldCoordinates;

    private FootprintPoint[] worldCoordinatePoints;

    private List<Point3d> worldCoordinatePointsList;

    /**
     * Creates a new <Code>Footprint</Code> instance.
     */
    public Footprint() {
    }

    /**
     * Creates a default instance of a <Code>Polygon</Code> from native Java AWT package.
     * 
     * <dl>
     * <dt>first point</dt>
     * <dd>50,50</dd>
     * <dt>second point</dt>
     * <dd>50,250</dd>
     * <dt>third point</dt>
     * <dd>200,200</dd>
     * <dt>fourth point</dt>
     * <dd>200,80</dd>
     * </dl>
     */
    public void setDefaultPolygon() {
        Point2d[] pointsWorldCoordinate = new Point2d[4];
        pointsWorldCoordinate[0] = new Point2d( 50, 50 );
        pointsWorldCoordinate[1] = new Point2d( 50, 250 );
        pointsWorldCoordinate[2] = new Point2d( 200, 200 );
        pointsWorldCoordinate[3] = new Point2d( 200, 80 );

        this.polygon = new Polygon( new int[] { (int) pointsWorldCoordinate[0].x, (int) pointsWorldCoordinate[1].x,
                                               (int) pointsWorldCoordinate[2].x, (int) pointsWorldCoordinate[3].x },
                                    new int[] { (int) pointsWorldCoordinate[0].y, (int) pointsWorldCoordinate[1].y,
                                               (int) pointsWorldCoordinate[2].y, (int) pointsWorldCoordinate[3].y },
                                    pointsWorldCoordinate.length );

    }

    /**
     * This should be a coverage later.
     * 
     * @return an AWT <Code>Polygon</Code>
     */
    public Polygon getPolygon() {
        return polygon;
    }

    /**
     * Generates the polygons in world- and pixel-coordinates.
     * 
     * @param footprintPointsList
     *            the points from the <Code>WorldRenderableObject</Code>
     */
    public void generateFootprints( List<float[]> footprintPointsList ) {
        worldCoordinatePolygonList = new ArrayList<Polygon>();
        int size = 0;
        for ( float[] f : footprintPointsList ) {
            size += f.length / 3;
        }

        worldCoordinates = new double[size * 2];
        worldCoordinatePoints = new FootprintPoint[size];
        worldCoordinatePointsList = new ArrayList<Point3d>();
        int countWorldCoords = 0;
        int countWorldCoordsPoint = 0;
        for ( float[] f : footprintPointsList ) {
            int polygonSize = f.length / 3;

            int[] x = new int[polygonSize];
            int[] y = new int[polygonSize];
            int count = 0;

            // get all points in 2D, so z-axis is omitted
            for ( int i = 0; i < f.length; i += 3 ) {
                x[count] = (int) f[i];
                y[count] = (int) f[i + 1];
                worldCoordinates[countWorldCoords] = f[i];
                worldCoordinates[++countWorldCoords] = f[i + 1];
                worldCoordinatePoints[countWorldCoordsPoint] = new FootprintPoint( f[i], f[i + 1] );
                worldCoordinatePointsList.add( new Point3d( f[i], f[i + 1], 0 ) );
                countWorldCoords++;
                count++;
                countWorldCoordsPoint++;
            }
            Polygon p = new Polygon( x, y, polygonSize );
            worldCoordinatePolygonList.add( p );
        }

    }

    /**
     * 
     * @return the list of polygons in world-coordinates
     */
    public List<Polygon> getWorldCoordinatePolygonList() {
        return worldCoordinatePolygonList;
    }

    public double[] getWorldCoordinates() {
        return worldCoordinates;
    }

    public FootprintPoint[] getWorldCoordinatePoints() {
        return worldCoordinatePoints;
    }

    public List<Point3d> getWorldCoordinatePointsList() {
        return worldCoordinatePointsList;
    }

}
