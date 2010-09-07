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
 * Implementation of the <b>helmert transformation</b> with 4 parameters.
 * <p>
 * 
 * <li>Build an array of balanced points calculated from the passpoints.</li>
 * <li>Calculate the coodinates to the balancedPoints.</li>
 * <li>Calculate the coodinates to the balancedPoints(the points for E, N, X, Y).</li>
 * <li>Calculate helpers for the caluculation of the needed transformation constants(oMinuend, aMinuend, oSubtrahend,
 * aSubtrahend, divisor).</li>
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
public class Helmert4Transform extends AbstractTransformation implements TransformationMethod {

    private final int arraySize;

    private double[] passPointsSrcE;

    private double[] passPointsSrcN;

    private double[] passPointsDstX;

    private double[] passPointsDstY;

    private double[] passPointsE_two;

    private double[] passPointsN_two;

    private double[] passPointsDstX_two;

    private double[] passPointsDstY_two;

    private double balancedPointE;

    private double balancedPointDstX;

    private double balancedPointN;

    private double balancedPointDstY;

    private double o;

    private double a;

    private double m;

    private double o_one;

    private double a_one;

    private double[] passPointsE_one;

    private double[] passPointsN_one;

    public Helmert4Transform( List<Triple<Point4Values, Point4Values, PointResidual>> mappedPoints,
                              Footprint footPrint, Scene2DValues sceneValues, CRS sourceCRS, CRS targetCRS,
                              final int order ) {
        super( mappedPoints, footPrint, sceneValues, targetCRS, targetCRS, order );

        arraySize = this.getArraySize();

        if ( arraySize > 0 ) {

            passPointsSrcE = new double[arraySize];
            passPointsSrcN = new double[arraySize];
            passPointsDstX = new double[arraySize];
            passPointsDstY = new double[arraySize];
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
            balancedPointE = cumulatedPointsE / arraySize;
            balancedPointDstX = cumulatedPointsDstX / arraySize;
            balancedPointN = cumulatedPointsN / arraySize;
            balancedPointDstY = cumulatedPointsDstY / arraySize;
            System.out.println( "[Helmert4] BalancedCoords -->  \nE: " + balancedPointE + " \nN: " + balancedPointN
                                + " \nY: " + balancedPointDstY + " \nX: " + balancedPointDstX );

            /*
             * Coordinates related to balancedPoints
             */
            passPointsE_two = new double[arraySize];
            passPointsN_two = new double[arraySize];
            passPointsDstX_two = new double[arraySize];
            passPointsDstY_two = new double[arraySize];

            int counter = 0;
            for ( double point : passPointsSrcN ) {
                passPointsN_two[counter] = point - balancedPointN;
                System.out.println( "[Helmert4] related BalancedCoords -->  \nN\'\': " + passPointsN_two[counter] );
                counter++;
            }
            counter = 0;
            for ( double point : passPointsSrcE ) {
                passPointsE_two[counter] = point - balancedPointE;
                System.out.println( "[Helmert4] related BalancedCoords -->  \nE\'\': " + passPointsE_two[counter] );
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
            calculateTransformationConstants();
            transformCoordinates();
        }
    }

    private void calculateTransformationConstants() {
        /*
         * calculate helpers
         */
        double minuendO = 0;
        double minuendA = 0;
        double subtrahendO = 0;
        double subtrahendA = 0;
        double divisor = 0;
        for ( int i = 0; i < arraySize; i++ ) {
            minuendO += passPointsE_two[i] * passPointsDstX_two[i];
            minuendA += passPointsE_two[i] * passPointsDstY_two[i];
            subtrahendO += passPointsN_two[i] * passPointsDstY_two[i];
            subtrahendA += passPointsN_two[i] * passPointsDstX_two[i];
            divisor += ( ( passPointsDstX_two[i] * passPointsDstX_two[i] ) + ( passPointsDstY_two[i] * passPointsDstY_two[i] ) );
        }
        System.out.println( "[Helmert4] helpers -->  \nminuend O: " + minuendO + " \nsubtrahend O: " + subtrahendO
                            + " \nminuend A: " + minuendA + " \nsubtrahend A: " + subtrahendA + " \ndivisor: "
                            + divisor );

        /*
         * calculate the transformationconstants
         */
        o = ( minuendO - subtrahendO ) / divisor;
        a = ( minuendA + subtrahendA ) / divisor;
        m = Math.sqrt( a * a + o * o );
        o_one = o / m;
        a_one = a / m;
        System.out.println( "[Helmert4] o: " + o + " a: " + a + " m: " + m + " o\': " + o_one + " a\': " + a_one );

        System.out.println( "[Helmert4] EPSILON -->  ArcSin: " + Math.asin( o_one ) + " ArcCos: " + Math.acos( a_one ) );

    }

    private void transformCoordinates() {
        passPointsE_one = new double[arraySize];
        passPointsN_one = new double[arraySize];

        for ( int i = 0; i < arraySize; i++ ) {
            passPointsN_one[i] = balancedPointN + ( a * passPointsDstX_two[i] ) - ( o * passPointsDstY_two[i] );
            passPointsE_one[i] = balancedPointE + ( a * passPointsDstY_two[i] ) + ( o * passPointsDstX_two[i] );
            System.out.println( "[Helmert4] Transformed Coords -->  \nN\': " + passPointsN_one[i] );
            System.out.println( "[Helmert4] Transformed Coords -->  \nE\': " + passPointsE_one[i] );

        }
    }

    @Override
    public PointResidual[] calculateResiduals() {
        /*
         * Caluculate the residuals
         */
        double[] residualE = new double[arraySize];
        double[] residualN = new double[arraySize];
        // residuals = new PointResidual[arraySize];

        for ( int i = 0; i < arraySize; i++ ) {
            residualE[i] = passPointsSrcE[i] - passPointsE_one[i];
            System.out.println( "[Helmert4] residualE -->  \nv(E): " + residualE[i] );
            residualN[i] = passPointsSrcN[i] - passPointsN_one[i];
            System.out.println( "[Helmert4] residualN -->  \nv(N): " + residualN[i] );
            getResiduals()[i] = new PointResidual( residualE[i], residualN[i] );

        }
        return getResiduals();
    }

    @Override
    public List<Ring> computeRingList() {

        List<Ring> transformedRingList = new ArrayList<Ring>();
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

                double newX_two = x - balancedPointDstX;
                double newY_two = y - balancedPointDstY;
                //
                // double calculatedX_one = balancedPointE + ( a * newY_two ) + ( o * newX_two );
                // double calculatedY_one = balancedPointN + ( a * newX_two ) - ( o * newY_two );
                // and pervert it back... 2/2

                double calculatedE_one = balancedPointE + ( a * newY_two ) + ( o * newX_two );
                double calculatedN_one = balancedPointN + ( a * newX_two ) - ( o * newY_two );
                pointList.add( geom.createPoint( "point", calculatedN_one, calculatedE_one, null ) );

            }
            Points points = new PointsList( pointList );
            transformedRingList.add( geom.createLinearRing( "ring", null, points ) );

        }

        return transformedRingList;

    }

    @Override
    public TransformationType getType() {

        return TransformationType.Helmert_4;
    }

}
