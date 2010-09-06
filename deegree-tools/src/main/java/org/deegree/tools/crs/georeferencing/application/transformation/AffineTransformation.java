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

import org.deegree.commons.utils.Triple;
import org.deegree.cs.CRS;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.standard.points.PointsList;
import org.deegree.tools.crs.georeferencing.application.Scene2DValues;
import org.deegree.tools.crs.georeferencing.model.Footprint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;
import org.deegree.tools.crs.georeferencing.model.points.PointResidual;

/**
 * Implementation of the <b>affine transformation</b> with 6 parameters.
 * <p>
 * 
 * <li>Build an array of balanced points calculated from the passpoints.</li>
 * <li>Calculate the coodinates to the balancedPoints.</li>
 * <li>Calculate the coodinates to the balancedPoints(the points for E, N, X, Y, dX, dY).</li>
 * <li>Calculate helpers for the caluculation of the needed transformation constants(SumY''X'', mSubtrahend, X''²sum,
 * Y''²sum).</li>
 * <li>Calculate the transformation constants applied to the helpers.</li>
 * <li>Calculate the residuals for each coordiante.</li>
 * <li>Finally caluculate the coordinates of the footprint polygons.</li>
 * <p>
 * 
 * <table border="1">
 * <tr>
 * <th></th>
 * <th>GeoRefPointsX-Dimension</th>
 * <th>GeoRefPointsY-Dimension</th>
 * <th>FootprintPointsX-Dimension</th>
 * <th>FootprintPointsY-Dimension</th>
 * </tr>
 * <tr>
 * <td>terminology</td>
 * <td>N</td>
 * <td>E</td>
 * <td>X</td>
 * <td>Y</td>
 * </tr>
 * <tr>
 * <td>passPoints</td>
 * <td>passPointsSrcN</td>
 * <td>passPointsSrcE</td>
 * <td>passPointsDstX</td>
 * <td>passPointsDstY</td>
 * </tr>
 * <tr>
 * <td>balancedPoints</td>
 * <td>balancedPointN</td>
 * <td>balancedPointE</td>
 * <td>balancedPointDstX</td>
 * <td>balancedPointDstY</td>
 * </tr>
 * <tr>
 * <td>resultingPoints</td>
 * <td>passPointsN_one</td>
 * <td>passPointsE_one</td>
 * <td>calculatedN_one</td>
 * <td>calculatedE_one</td>
 * </tr>
 * </table>
 * 
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class AffineTransformation extends AbstractTransformation implements TransformationMethod {

    public AffineTransformation( List<Triple<Point4Values, Point4Values, PointResidual>> mappedPoints,
                                 Footprint footPrint, Scene2DValues sceneValues, CRS sourceCRS, CRS targetCRS,
                                 final int order ) {
        super( mappedPoints, footPrint, sceneValues, sourceCRS, targetCRS, order );

    }

    @Override
    public List<Ring> computeRingList() {
        int arraySize = mappedPoints.size();
        List<Ring> transformedRingList = null;

        if ( arraySize > 0 ) {

            double[] passPointsSrcE = new double[arraySize];
            double[] passPointsSrcN = new double[arraySize];
            double[] passPointsDstX = new double[arraySize];
            double[] passPointsDstY = new double[arraySize];
            double cumulatedPointsE = 0;
            double cumulatedPointsDstX = 0;
            double cumulatedPointsN = 0;
            double cumulatedPointsDstY = 0;
            int counterSrc = 0;
            int counterDst = 0;

            for ( Triple<Point4Values, Point4Values, PointResidual> p : mappedPoints ) {
                double x = p.first.getWorldCoords().getX();
                double y = p.first.getWorldCoords().getY();
                cumulatedPointsDstX += x;
                passPointsDstX[counterDst] = x;
                cumulatedPointsDstY += y;
                passPointsDstY[counterDst] = y;
                counterDst++;
                Point4Values pValue = p.second;

                // this is strange why is this perverted?? 1/2 later is the second one...
                y = pValue.getWorldCoords().getY();
                x = pValue.getWorldCoords().getX();

                cumulatedPointsE += y;
                passPointsSrcE[counterSrc] = y;
                cumulatedPointsN += x;
                passPointsSrcN[counterSrc] = x;
                counterSrc++;

            }

            /*
             * BalancePointCoordinates
             */
            double balancedPointE = cumulatedPointsE / arraySize;
            double balancedPointDstX = cumulatedPointsDstX / arraySize;
            double balancedPointN = cumulatedPointsN / arraySize;
            double balancedPointDstY = cumulatedPointsDstY / arraySize;
            System.out.println( "[AffineTransformation] BalancedCoords -->  \nE: " + balancedPointE + " \nN: "
                                + balancedPointN + " \nY: " + balancedPointDstY + " \nX: " + balancedPointDstX );

            /*
             * Coordinates related to balancedPoints
             */
            double[] passPointsE_two = new double[arraySize];
            double[] passPointsN_two = new double[arraySize];
            double[] passPointsDstX_two = new double[arraySize];
            double[] passPointsDstY_two = new double[arraySize];
            double[] dY = new double[arraySize];
            double[] dX = new double[arraySize];

            int counter = 0;
            for ( double point : passPointsSrcN ) {
                passPointsN_two[counter] = point - balancedPointN;
                System.out.println( "[AffineTransformation] related BalancedCoords -->  \nN\'\': "
                                    + passPointsN_two[counter] );
                counter++;
            }
            counter = 0;
            for ( double point : passPointsSrcE ) {
                passPointsE_two[counter] = point - balancedPointE;
                System.out.println( "[AffineTransformation] related BalancedCoords -->  \nE\'\': "
                                    + passPointsE_two[counter] );
                counter++;
            }
            counter = 0;
            for ( double point : passPointsDstY ) {
                passPointsDstY_two[counter] = point - balancedPointDstY;
                dY[counter] = passPointsE_two[counter] - passPointsDstY_two[counter];
                System.out.println( "[AffineTransformation] related BalancedCoords -->   \nY\'\': "
                                    + passPointsDstY_two[counter] + " dY: " + dY[counter] );
                counter++;
            }
            counter = 0;
            for ( double point : passPointsDstX ) {
                passPointsDstX_two[counter] = point - balancedPointDstX;
                dX[counter] = passPointsN_two[counter] - passPointsDstX_two[counter];
                System.out.println( "[AffineTransformation] related BalancedCoords -->   \nX\'\': "
                                    + passPointsDstX_two[counter] + " dX: " + dX[counter] );

                counter++;
            }

            /*
             * calculate the helpers
             */
            double sumY_two_X_two = 0;
            double mSubtrahend;
            double sumX_two_square = 0;
            double sumY_two_square = 0;

            double a11_minuend = 0;
            double a11_subtrahend;
            double a12_minuend = 0;
            double a12_subtrahend;
            double a21_minuend = 0;
            double a21_subtrahend;
            double a22_minuend = 0;
            double a22_subtrahend;

            for ( int i = 0; i < passPointsDstX.length; i++ ) {
                sumY_two_X_two += passPointsDstY_two[i] * passPointsDstX_two[i];
                sumX_two_square += passPointsDstX_two[i] * passPointsDstX_two[i];
                sumY_two_square += passPointsDstY_two[i] * passPointsDstY_two[i];
                a11_minuend += passPointsDstX_two[i] * dX[i];
                a12_minuend += passPointsDstY_two[i] * dX[i];
                a21_minuend += passPointsDstX_two[i] * dY[i];
                a22_minuend += passPointsDstY_two[i] * dY[i];

                System.out.println( "[AffineTransformation] a12test --> " + a12_minuend + " = " + passPointsDstY_two[i]
                                    + " * " + dX[i] );

            }

            mSubtrahend = sumY_two_X_two * sumY_two_X_two;
            a11_subtrahend = a12_minuend * sumY_two_X_two;
            a12_subtrahend = a11_minuend * sumY_two_X_two;
            a21_subtrahend = a22_minuend * sumY_two_X_two;
            a22_subtrahend = a21_minuend * sumY_two_X_two;
            System.out.println( "[AffineTransformation] helperFirst -->\n SumX\'\'_Y\'\': " + sumY_two_X_two
                                + " mSub: " + mSubtrahend + " X\'\'_Square: " + sumX_two_square + " Y\'\'_Square: "
                                + sumY_two_square + " a12_minuend: " + a12_minuend + " a11_subtrahend: "
                                + a11_subtrahend );
            a11_minuend = a11_minuend * sumY_two_square;
            a12_minuend = a12_minuend * sumX_two_square;
            a21_minuend = a21_minuend * sumY_two_square;
            a22_minuend = a22_minuend * sumX_two_square;

            /*
             * calculate the transformationconstants
             */
            double m = ( sumX_two_square * sumY_two_square ) - mSubtrahend;
            double a11 = 1 + 1 / m * ( a11_minuend - a11_subtrahend );
            double a12 = 1 / m * ( a12_minuend - a12_subtrahend );
            double a21 = 1 / m * ( a21_minuend - a21_subtrahend );
            double a22 = 1 + 1 / m * ( a22_minuend - a22_subtrahend );

            System.out.println( "[AffineTransformation] Transformationconstants -->\n M: " + m + " a11: " + a11
                                + " a12: " + a12 + " a21: " + a21 + " a22: " + a22 );

            transformedRingList = new ArrayList<Ring>();
            List<Point> pointList;
            GeometryFactory geom = new GeometryFactory();

            double[] passPointsE_one = new double[arraySize];
            double[] passPointsN_one = new double[arraySize];

            for ( int i = 0; i < arraySize; i++ ) {
                passPointsN_one[i] = balancedPointN + ( a11 * passPointsDstX_two[i] ) + ( a12 * passPointsDstY_two[i] );
                passPointsE_one[i] = balancedPointE + ( a21 * passPointsDstX_two[i] ) + ( a22 * passPointsDstY_two[i] );
                System.out.println( "[AffineTransformation] Transformed Coords -->  \nN\': " + passPointsN_one[i] );
                System.out.println( "[AffineTransformation] Transformed Coords -->  \nE\': " + passPointsE_one[i] );

            }

            /*
             * Caluculate the residuals TODO
             */

            /*
             * calculate the new coordinates in the target coordinate system
             */
            for ( Ring ring : footPrint.getWorldCoordinateRingList() ) {
                pointList = new ArrayList<Point>();
                for ( int i = 0; i < ring.getControlPoints().size(); i++ ) {
                    double x = ring.getControlPoints().getX( i );
                    double y = ring.getControlPoints().getY( i );

                    double newX_two = x - balancedPointDstX;
                    double newY_two = y - balancedPointDstY;

                    double calculatedE_one = balancedPointE + ( a21 * newX_two ) + ( a22 * newY_two );
                    double calculatedN_one = balancedPointN + ( a11 * newX_two ) + ( a12 * newY_two );
                    // and pervert it back... 2/2
                    pointList.add( geom.createPoint( "point", calculatedN_one, calculatedE_one, null ) );

                }
                Points points = new PointsList( pointList );
                transformedRingList.add( geom.createLinearRing( "ring", null, points ) );

            }

        }

        return transformedRingList;
    }

    @Override
    public TransformationType getType() {
        return TransformationType.Affine;
    }

    @Override
    public PointResidual[] getResiduals() {
        // TODO Auto-generated method stub
        return null;
    }

}
