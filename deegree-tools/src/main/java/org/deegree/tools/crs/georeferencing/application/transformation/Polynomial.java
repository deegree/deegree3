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
package org.deegree.tools.crs.georeferencing.application.transformation;

import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.media.jai.WarpPolynomial;

import org.deegree.commons.utils.Pair;
import org.deegree.cs.CRS;
import org.deegree.cs.CoordinateTransformer;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.standard.points.PointsList;
import org.deegree.tools.crs.georeferencing.application.Scene2DValues;
import org.deegree.tools.crs.georeferencing.model.Footprint;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Polynomial extends AbstractTransformation implements TransformationMethod {

    public Polynomial( List<Pair<Point4Values, Point4Values>> mappedPoints, Footprint footPrint,
                       Scene2DValues sceneValues, CRS sourceCRS, CRS targetCRS, int order ) {
        super( mappedPoints, footPrint, sceneValues, sourceCRS, targetCRS, order );

    }

    public List<Ring> computeRingList() {
        int arraySize = mappedPoints.size() * 2;
        if ( arraySize > 0 ) {

            float[] passPointsSrc = new float[arraySize];
            float[] passPointsDst = new float[arraySize];
            int counterSrc = 0;
            int counterDst = 0;
            List<double[]> coordinateList = new LinkedList<double[]>();
            CoordinateTransformer ct;

            for ( Pair<Point4Values, Point4Values> p : mappedPoints ) {
                double x = p.first.getWorldCoords().getX();
                double y = p.first.getWorldCoords().getY();

                passPointsDst[counterDst] = (float) x;
                passPointsDst[++counterDst] = (float) y;
                counterDst++;
                Point4Values pValue = p.second;
                x = pValue.getWorldCoords().getX();
                y = pValue.getWorldCoords().getY();
                passPointsSrc[counterSrc] = (float) x;
                passPointsSrc[++counterSrc] = (float) y;
                counterSrc++;

            }

            System.out.println( "\n\n coordinates" );
            for ( int i = 0; i < passPointsDst.length; i += 2 ) {
                System.out.println( passPointsSrc[i] + "/" + passPointsSrc[i + 1] + " -- " + passPointsDst[i] + "/"
                                    + passPointsDst[i + 1] );

            }

            WarpPolynomial warp = WarpPolynomial.createWarp( passPointsSrc, 0, passPointsDst, 0, passPointsSrc.length,
                                                             1f, 1f, 1f, 1f, order );
            System.out.println( "coeff:" );
            float[] xC = warp.getXCoeffs();
            float[] yC = warp.getYCoeffs();
            for ( int i = 0; i < yC.length; i++ ) {
                System.out.println( i + " " + xC[i] + " " + yC[i] );
            }

            System.out.println( "resid" );
            double rxLocal = 0;
            double ryLocal = 0;
            for ( int i = 0; i < passPointsDst.length; i += 2 ) {
                Point2D p = warp.mapDestPoint( new Point2D.Float( passPointsDst[i], passPointsDst[i + 1] ) );
                rxLocal += ( p.getX() - passPointsSrc[i] );
                ryLocal += ( p.getY() - passPointsSrc[i + 1] );
                System.out.println( ( i / 2 ) + " -> " + ( p.getX() - passPointsSrc[i] ) + "/"
                                    + ( p.getY() - passPointsSrc[i + 1] ) );

            }

            System.out.println( "\n mean resid" );
            rxLocal /= ( passPointsSrc.length / 2 );
            ryLocal /= ( passPointsSrc.length / 2 );
            System.out.println( rxLocal + " " + ryLocal );

            // for ( Point3d p : result ) {
            // System.out.println( p.getX() + " " + p.getY() );
            // }

            // int[] tz = sceneValues.getPixelCoordinate( new Point2D.Float( 0.03f, 6.0f ) );
            // int[] x = new int[passPointsDst.length / 2];
            // int[] y = new int[passPointsDst.length / 2];
            // int count = 0;
            List<Polygon> transformedPolygonList = new ArrayList<Polygon>();
            List<Ring> transformedRingList = new ArrayList<Ring>();
            List<Point> pointList;
            GeometryFactory geom = new GeometryFactory();

            // for ( int i = 0; i < passPointsDst.length; i += 2 ) {
            //
            // Point2D p = warp.mapDestPoint( new Point2D.Float( passPointsDst[i], passPointsDst[i + 1] ) );
            // double rx = -( p.getX() - passPointsSrc[i] );
            // double ry = -( p.getY() - passPointsSrc[i + 1] );
            // AbstractGRPoint convertPoint = new GeoReferencedPoint( p.getX() + rx, p.getY() + ry );
            // int[] value = sceneValues.getPixelCoordinatePolygon( convertPoint );
            // x[count] = value[0];
            // y[count] = value[1];
            // count++;
            //
            // }
            // Polygon p = new Polygon( x, y, (int) ( passPointsDst.length * 0.5 ) );
            // transformedPolygonList.add( p );
            for ( Polygon po : footPrint.getWorldCoordinatePolygonList() ) {
                pointList = new ArrayList<Point>();
                int[] x = new int[po.npoints];
                int[] y = new int[po.npoints];
                for ( int i = 0; i < po.npoints; i++ ) {
                    int x2 = po.xpoints[i];
                    int y2 = po.ypoints[i];

                    Point2D p = warp.mapDestPoint( new Point2D.Float( x2, y2 ) );
                    double rx = ( p.getX() - rxLocal );
                    double ry = ( p.getY() - ryLocal );

                    pointList.add( geom.createPoint( "point", rx, ry, null ) );

                    AbstractGRPoint convertPoint = new GeoReferencedPoint( rx, ry );
                    int[] value = sceneValues.getPixelCoord( convertPoint );
                    x[i] = value[0];
                    y[i] = value[1];

                }
                Points points = new PointsList( pointList );
                transformedRingList.add( geom.createLinearRing( "ring", null, points ) );

                Polygon p = new Polygon( x, y, po.npoints );
                transformedPolygonList.add( p );
            }

            for ( Polygon po : transformedPolygonList ) {
                for ( int i = 0; i < po.npoints; i++ ) {
                    System.out.println( "[Polynomial] TransformedPolygons: " + po.xpoints[i] + " " + po.ypoints[i] );
                }
            }

            return transformedRingList;
        }

        return null;
    }

    @Override
    public TransformationType getType() {

        return TransformationType.PolynomialFirstOrder;
    }

}
