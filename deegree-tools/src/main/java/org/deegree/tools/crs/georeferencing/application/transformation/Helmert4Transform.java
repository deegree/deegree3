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

import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.utils.Pair;
import org.deegree.cs.CRS;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.standard.points.PointsList;
import org.deegree.tools.crs.georeferencing.application.Scene2DValues;
import org.deegree.tools.crs.georeferencing.model.Footprint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;

/**
 * Implementation of the Helmert-Transformation with 4 parameters
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Helmert4Transform extends AbstractTransformation implements TransformationMethod {

    public Helmert4Transform( List<Pair<Point4Values, Point4Values>> mappedPoints, Footprint footPrint,
                              Scene2DValues sceneValues, CRS sourceCRS, CRS targetCRS, final int order ) {
        super( mappedPoints, footPrint, sceneValues, targetCRS, targetCRS, order );
    }

    @Override
    public List<Ring> computeRingList() {

        int arraySize = mappedPoints.size();
        List<Ring> transformedRingList = null;

        if ( arraySize > 0 ) {

            double[] passPointsSrcX = new double[arraySize];
            double[] passPointsSrcY = new double[arraySize];
            double[] passPointsDstX = new double[arraySize];
            double[] passPointsDstY = new double[arraySize];
            double cumulatedPointsSrcX = 0;
            double cumulatedPointsDstX = 0;
            double cumulatedPointsSrcY = 0;
            double cumulatedPointsDstY = 0;
            int counterSrc = 0;
            int counterDst = 0;

            for ( Pair<Point4Values, Point4Values> p : mappedPoints ) {
                double x = p.first.getWorldCoords().getX();
                double y = p.first.getWorldCoords().getY();
                cumulatedPointsDstX += x;
                passPointsDstX[counterDst] = x;
                cumulatedPointsDstY += y;
                passPointsDstY[counterDst] = y;
                counterDst++;
                Point4Values pValue = p.second;
                x = pValue.getWorldCoords().getX();
                y = pValue.getWorldCoords().getY();
                cumulatedPointsSrcX += x;
                passPointsSrcX[counterSrc] = x;
                cumulatedPointsSrcY += y;
                passPointsSrcY[counterSrc] = y;
                counterSrc++;

            }

            /*
             * BalancePointCoordinates
             */
            double balancedPointSrcX = cumulatedPointsSrcX / arraySize;
            double balancedPointDstX = cumulatedPointsDstX / arraySize;
            double balancedPointSrcY = cumulatedPointsSrcY / arraySize;
            double balancedPointDstY = cumulatedPointsDstY / arraySize;
            System.out.println( "[Helmert4] BalancedCoords -->  \nE: " + balancedPointSrcY + " \nN: "
                                + balancedPointSrcX + " \nY: " + balancedPointDstY + " \nX: " + balancedPointDstX );

            /*
             * Coordinates related to balancedPoints
             */
            double[] passPointsSrcX_two = new double[arraySize];
            double[] passPointsSrcY_two = new double[arraySize];
            double[] passPointsDstX_two = new double[arraySize];
            double[] passPointsDstY_two = new double[arraySize];

            int counter = 0;
            for ( double point : passPointsSrcY ) {
                passPointsSrcY_two[counter] = point - balancedPointSrcY;
                System.out.println( "[Helmert4] related BalancedCoords -->  \nE\'\': " + passPointsSrcY_two[counter] );
                counter++;
            }
            counter = 0;
            for ( double point : passPointsSrcX ) {
                passPointsSrcX_two[counter] = point - balancedPointSrcX;
                System.out.println( "[Helmert4] related BalancedCoords -->  \nN\'\': " + passPointsSrcX_two[counter] );
                counter++;
            }
            counter = 0;
            for ( double point : passPointsDstY ) {
                passPointsDstY_two[counter] = point - balancedPointDstY;
                System.out.println( "[Helmert4] related BalancedCoords -->   \nY\'\': " + passPointsDstY_two[counter] );
                counter++;
            }
            counter = 0;
            for ( double point : passPointsDstX ) {
                passPointsDstX_two[counter] = point - balancedPointDstX;
                System.out.println( "[Helmert4] related BalancedCoords -->   \nX\'\': " + passPointsDstX_two[counter] );
                counter++;
            }

            /*
             * calculate transformationconstants with helpers
             */
            double minuendO = 0;
            double minuendA = 0;
            double subtrahendO = 0;
            double subtrahendA = 0;
            double divisor = 0;
            for ( int i = 0; i < arraySize; i++ ) {
                minuendO += passPointsSrcY_two[i] * passPointsDstX_two[i];
                minuendA += passPointsSrcY_two[i] * passPointsDstY_two[i];
                subtrahendO += passPointsSrcX_two[i] * passPointsDstY_two[i];
                subtrahendA += passPointsSrcX_two[i] * passPointsDstX_two[i];
                divisor += ( passPointsDstX_two[i] * passPointsDstX_two[i] )
                           + ( passPointsDstY_two[i] * passPointsDstY_two[i] );
            }
            System.out.println( "[Helmert4] helpers -->  \nminuend O: " + minuendO + " \nsubtrahend O: " + subtrahendO
                                + " \nminuend A: " + minuendA + " \nsubtrahend A: " + subtrahendA + " \ndivisor: "
                                + divisor );

            double o = ( minuendO - subtrahendO ) / divisor;
            double a = ( minuendA + subtrahendA ) / divisor;
            double m = Math.sqrt( a * a + o * o );
            double o_one = o / m;
            double a_one = a / m;
            System.out.println( "[Helmert4] o: " + o + " a: " + a + " m: " + m + " o\': " + o_one + " a\': " + a_one );

            System.out.println( "[Helmert4] EPSILON -->  ArcSin: " + Math.asin( o_one ) + " ArcCos: "
                                + Math.acos( a_one ) );

            transformedRingList = new ArrayList<Ring>();
            List<Point> pointList;
            GeometryFactory geom = new GeometryFactory();

            /*
             * calculate the new coordinates in the target coordinate system
             */
            for ( Ring ring : footPrint.getWorldCoordinateRingList() ) {
                pointList = new ArrayList<Point>();
                for ( int i = 0; i < ring.getControlPoints().size(); i++ ) {
                    double x = ring.getControlPoints().getX( i );
                    double y = ring.getControlPoints().getY( i );

                    double newX = x - balancedPointDstX;
                    double newY = y - balancedPointDstY;

                    double calculatedX = balancedPointSrcX + ( a * newX ) - ( o * newY );
                    double calculatedY = balancedPointSrcY + ( a * newY ) + ( o * newX );
                    pointList.add( geom.createPoint( "point", calculatedX, calculatedY, null ) );

                }
                Points points = new PointsList( pointList );
                transformedRingList.add( geom.createLinearRing( "ring", null, points ) );

            }

        }

        return transformedRingList;

    }

    @Override
    public TransformationType getType() {

        return TransformationType.Helmert_4;
    }

}
