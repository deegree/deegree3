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

import javax.vecmath.Point3d;

import org.deegree.commons.utils.Triple;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.standard.points.PointsList;
import org.deegree.tools.crs.georeferencing.application.Scene2DValues;
import org.deegree.tools.crs.georeferencing.application.TransformationPoints;
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
public class AffineTransformation extends AbstractTransformation {

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

    private double[] dY;

    private double[] dX;

    private double m;

    private double a11;

    private double a12;

    private double a21;

    private double a22;

    private double[] passPointsE_one;

    private double[] passPointsN_one;

    public AffineTransformation( TransformationPoints points, Footprint footPrint, Scene2DValues sceneValues,
                                 ICRS sourceCRS, ICRS targetCRS, final int order ) {
        super( points, footPrint, sceneValues, sourceCRS, targetCRS, order );

        if ( !points.isEmpty() ) {
            this.passPointsSrcE = new double[points.getNumPoints()];
            this.passPointsSrcN = new double[points.getNumPoints()];
            this.passPointsDstX = new double[points.getNumPoints()];
            this.passPointsDstY = new double[points.getNumPoints()];
            double cumulatedPointsE = 0;
            double cumulatedPointsDstX = 0;
            double cumulatedPointsN = 0;
            double cumulatedPointsDstY = 0;
            int counterSrc = 0;
            int counterDst = 0;

            for ( Triple<Point4Values, Point4Values, PointResidual> p : points.getMappedPoints() ) {
                if ( p.first == null || p.second == null ) {
                    continue;
                }
                double x = p.first.getWorldCoords().x;
                double y = p.first.getWorldCoords().y;
                cumulatedPointsDstX += x;
                this.passPointsDstX[counterDst] = x;
                cumulatedPointsDstY += y;
                this.passPointsDstY[counterDst] = y;
                counterDst++;
                Point4Values pValue = p.second;

                // this is strange why is this perverted?? 1/2 later is the second one...
                y = pValue.getWorldCoords().y;
                x = pValue.getWorldCoords().x;

                cumulatedPointsE += y;
                this.passPointsSrcE[counterSrc] = y;
                cumulatedPointsN += x;
                this.passPointsSrcN[counterSrc] = x;
                counterSrc++;

            }

            /*
             * BalancePointCoordinates
             */
            this.balancedPointE = cumulatedPointsE / points.getNumPoints();
            this.balancedPointDstX = cumulatedPointsDstX / points.getNumPoints();
            this.balancedPointN = cumulatedPointsN / points.getNumPoints();
            this.balancedPointDstY = cumulatedPointsDstY / points.getNumPoints();
            System.out.println( "[AffineTransformation] BalancedCoords -->  \nE: " + this.balancedPointE + " \nN: "
                                + this.balancedPointN + " \nY: " + this.balancedPointDstY + " \nX: "
                                + this.balancedPointDstX );

            /*
             * Coordinates related to balancedPoints
             */
            this.passPointsE_two = new double[points.getNumPoints()];
            this.passPointsN_two = new double[points.getNumPoints()];
            this.passPointsDstX_two = new double[points.getNumPoints()];
            this.passPointsDstY_two = new double[points.getNumPoints()];
            this.dY = new double[points.getNumPoints()];
            this.dX = new double[points.getNumPoints()];

            int counter = 0;
            for ( double point : this.passPointsSrcN ) {
                this.passPointsN_two[counter] = point - this.balancedPointN;
                System.out.println( "[AffineTransformation] related BalancedCoords -->  \nN\'\': "
                                    + this.passPointsN_two[counter] );
                counter++;
            }
            counter = 0;
            for ( double point : this.passPointsSrcE ) {
                this.passPointsE_two[counter] = point - this.balancedPointE;
                System.out.println( "[AffineTransformation] related BalancedCoords -->  \nE\'\': "
                                    + this.passPointsE_two[counter] );
                counter++;
            }
            counter = 0;
            for ( double point : this.passPointsDstY ) {
                this.passPointsDstY_two[counter] = point - this.balancedPointDstY;
                this.dY[counter] = this.passPointsE_two[counter] - this.passPointsDstY_two[counter];
                System.out.println( "[AffineTransformation] related BalancedCoords -->   \nY\'\': "
                                    + this.passPointsDstY_two[counter] + " dY: " + this.dY[counter] );
                counter++;
            }
            counter = 0;
            for ( double point : this.passPointsDstX ) {
                this.passPointsDstX_two[counter] = point - this.balancedPointDstX;
                this.dX[counter] = this.passPointsN_two[counter] - this.passPointsDstX_two[counter];
                System.out.println( "[AffineTransformation] related BalancedCoords -->   \nX\'\': "
                                    + this.passPointsDstX_two[counter] + " dX: " + this.dX[counter] );

                counter++;
            }
            this.calculateTransformationConstants();
            this.transformCoordinates();
        }
    }

    public double[] getMatrix() {
        return new double[] { this.a21, this.a22, this.a11, this.a12, this.balancedPointE, this.balancedPointN };
    }

    private void calculateTransformationConstants() {
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

        for ( int i = 0; i < this.passPointsDstX.length; i++ ) {
            sumY_two_X_two += this.passPointsDstY_two[i] * this.passPointsDstX_two[i];
            sumX_two_square += this.passPointsDstX_two[i] * this.passPointsDstX_two[i];
            sumY_two_square += this.passPointsDstY_two[i] * this.passPointsDstY_two[i];
            a11_minuend += this.passPointsDstX_two[i] * this.dX[i];
            a12_minuend += this.passPointsDstY_two[i] * this.dX[i];
            a21_minuend += this.passPointsDstX_two[i] * this.dY[i];
            a22_minuend += this.passPointsDstY_two[i] * this.dY[i];

            System.out.println( "[AffineTransformation] a12test --> " + a12_minuend + " = "
                                + this.passPointsDstY_two[i] + " * " + this.dX[i] );

        }

        mSubtrahend = sumY_two_X_two * sumY_two_X_two;
        a11_subtrahend = a12_minuend * sumY_two_X_two;
        a12_subtrahend = a11_minuend * sumY_two_X_two;
        a21_subtrahend = a22_minuend * sumY_two_X_two;
        a22_subtrahend = a21_minuend * sumY_two_X_two;
        System.out.println( "[AffineTransformation] helperFirst -->\n SumX\'\'_Y\'\': " + sumY_two_X_two + " mSub: "
                            + mSubtrahend + " X\'\'_Square: " + sumX_two_square + " Y\'\'_Square: " + sumY_two_square
                            + " a12_minuend: " + a12_minuend + " a11_subtrahend: " + a11_subtrahend );
        a11_minuend = a11_minuend * sumY_two_square;
        a12_minuend = a12_minuend * sumX_two_square;
        a21_minuend = a21_minuend * sumY_two_square;
        a22_minuend = a22_minuend * sumX_two_square;

        /*
         * calculate the transformationconstants
         */
        this.m = ( sumX_two_square * sumY_two_square ) - mSubtrahend;
        this.a11 = 1 + 1 / this.m * ( a11_minuend - a11_subtrahend );
        this.a12 = 1 / this.m * ( a12_minuend - a12_subtrahend );
        this.a21 = 1 / this.m * ( a21_minuend - a21_subtrahend );
        this.a22 = 1 + 1 / this.m * ( a22_minuend - a22_subtrahend );

        System.out.println( "[AffineTransformation] Transformationconstants -->\n M: " + this.m + " a11: " + this.a11
                            + " a12: " + this.a12 + " a21: " + this.a21 + " a22: " + this.a22 );

    }

    private void transformCoordinates() {
        this.passPointsE_one = new double[points.getNumPoints()];
        this.passPointsN_one = new double[points.getNumPoints()];

        for ( int i = 0; i < points.getNumPoints(); i++ ) {
            this.passPointsN_one[i] = this.balancedPointN + ( this.a11 * this.passPointsDstX_two[i] )
                                      + ( this.a12 * this.passPointsDstY_two[i] );
            this.passPointsE_one[i] = this.balancedPointE + ( this.a21 * this.passPointsDstX_two[i] )
                                      + ( this.a22 * this.passPointsDstY_two[i] );
            // System.out.println( "[AffineTransformation] Transformed Coords -->  \nN\': " + this.passPointsN_one[i] );
            // System.out.println( "[AffineTransformation] Transformed Coords -->  \nE\': " + this.passPointsE_one[i] );
        }

    }

    @Override
    public List<Ring> computeRingList() {

        List<Ring> transformedRingList = new ArrayList<Ring>();
        List<Point> pointList;
        GeometryFactory geom = new GeometryFactory();

        /*
         * calculate the new coordinates in the target coordinate system
         */
        for ( Ring ring : this.footPrint.getWorldCoordinateRingList() ) {
            pointList = new ArrayList<Point>();
            Point first = null;
            for ( int i = 0; i < ring.getControlPoints().size(); i++ ) {
                double x = ring.getControlPoints().getX( i );
                double y = ring.getControlPoints().getY( i );

                double newX_two = x - this.balancedPointDstX;
                double newY_two = y - this.balancedPointDstY;

                double calculatedE_one = this.balancedPointE + ( this.a21 * newX_two ) + ( this.a22 * newY_two );
                double calculatedN_one = this.balancedPointN + ( this.a11 * newX_two ) + ( this.a12 * newY_two );
                // and pervert it back... 2/2
                pointList.add( geom.createPoint( "point", calculatedN_one, calculatedE_one, null ) );
                if ( first == null ) {
                    first = pointList.get( pointList.size() - 1 );
                }
            }
            pointList.add( first );
            Points points = new PointsList( pointList );
            transformedRingList.add( geom.createLinearRing( "ring", null, points ) );

        }

        return transformedRingList;
    }

    @Override
    public AbstractTransformation.TransformationType getType() {
        return AbstractTransformation.TransformationType.Affine;
    }

    @Override
    public PointResidual[] calculateResiduals() {
        /*
         * Caluculate the residuals
         */
        double[] residualE = new double[points.getNumPoints()];
        double[] residualN = new double[points.getNumPoints()];
        // residuals = new PointResidual[arraySize];

        PointResidual[] residuals = new PointResidual[points.getNumPoints()];

        for ( int i = 0; i < points.getNumPoints(); i++ ) {
            residualE[i] = this.passPointsSrcE[i] - this.passPointsE_one[i];
            // System.out.println( "[AffineTransformation] residualE -->  \nv(E): " + residualE[i] );
            residualN[i] = this.passPointsSrcN[i] - this.passPointsN_one[i];
            // System.out.println( "[AffineTransformation] residualN -->  \nv(N): " + residualN[i] );
            residuals[i] = new PointResidual( residualE[i], residualN[i] );

        }
        return residuals;
    }

    @Override
    public List<Point3d> doTransform( List<Point3d> srcPts )
                            throws TransformationException {
        List<Point3d> list = new ArrayList<Point3d>( srcPts.size() );
        for ( Point3d src : srcPts ) {
            Point3d dest = new Point3d();
            dest.y = this.balancedPointN + this.a11 * src.x + this.a12 * src.y;
            dest.x = this.balancedPointE + this.a21 * src.x + this.a22 * src.y;
        }
        return list;
    }

    @Override
    public String getImplementationName() {
        return "st1affine";
    }

    @Override
    public boolean isIdentity() {
        return false;
    }

}
