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
import javax.vecmath.Point3d;

import org.deegree.commons.utils.Pair;
import org.deegree.cs.CRS;
import org.deegree.cs.CoordinateTransformer;
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

    @Override
    public List<Polygon> computePolygonList() {
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

                passPointsDst[counterSrc] = (float) x;
                passPointsDst[++counterSrc] = (float) y;
                counterSrc++;
                Point4Values pValue = p.second;
                x = pValue.getWorldCoords().getX();
                y = pValue.getWorldCoords().getY();
                passPointsSrc[counterDst] = (float) x;
                passPointsSrc[++counterDst] = (float) y;
                counterDst++;

            }

            System.out.println( "\n\n coordinates" );
            for ( int i = 0; i < passPointsDst.length; i += 2 ) {
                System.out.println( passPointsSrc[i] + "/" + passPointsSrc[i + 1] + " -- " + passPointsDst[i] + "/"
                                    + passPointsDst[i + 1] );

            }

            WarpPolynomial warp = WarpPolynomial.createWarp( passPointsSrc, 0, passPointsDst, 0, passPointsSrc.length,
                                                             1f, 1f, 1f, 1f, order );
            System.out.println( "coeff:" );
            float[] x = warp.getXCoeffs();
            float[] y = warp.getYCoeffs();
            for ( int i = 0; i < y.length; i++ ) {
                System.out.println( i + " " + x[i] + " " + y[i] );
            }

            List<Point3d> result = new ArrayList<Point3d>();

            double rx = 0;
            double ry = 0;
            // int[] tz = sceneValues.getPixelCoordinate( new Point2D.Float( 0.03f, 6.0f ) );

            List<Polygon> transformedPolygonList = new ArrayList<Polygon>();
            for ( Polygon po : footPrint.getWorldCoordinatePolygonList() ) {

                int[] x2 = new int[po.npoints];
                int[] y2 = new int[po.npoints];
                for ( int i = 0; i < po.npoints; i++ ) {

                    Point2D p = warp.mapDestPoint( new Point2D.Float( po.xpoints[i], po.ypoints[i] ) );
                    AbstractGRPoint convertPoint = new GeoReferencedPoint( p.getX(), p.getY() );
                    int[] value = sceneValues.getPixelCoordinatePolygon( convertPoint );
                    x2[i] = value[0];
                    y2[i] = value[1];

                }

                Polygon p = new Polygon( x2, y2, po.npoints );
                transformedPolygonList.add( p );
            }

            System.out.println( "\n resid" );
            for ( int i = 0; i < passPointsDst.length; i += 2 ) {
                Point2D p = warp.mapDestPoint( new Point2D.Float( passPointsDst[i], passPointsDst[i + 1] ) );
                // System.out.println( "p: " + p + " : " + p.getX() + " - " + ordinatesSrc[i] );
                rx += ( p.getX() - passPointsSrc[i] );
                ry += ( p.getY() - passPointsSrc[i + 1] );
                System.out.println( ( i / 2 ) + " -> " + ( p.getX() - passPointsSrc[i] ) + "/"
                                    + ( p.getY() - passPointsSrc[i + 1] ) );
                result.add( new Point3d( p.getX(), p.getY(), 0 ) );

            }
            System.out.println( "\n mean resid" );
            rx /= ( passPointsSrc.length / 2 );
            ry /= ( passPointsSrc.length / 2 );
            System.out.println( rx + " " + ry );

            for ( Point3d p : result ) {
                System.out.println( p.getX() + " " + p.getY() );
            }

            for ( Polygon p : transformedPolygonList ) {
                for ( int i = 0; i < p.npoints; i++ ) {
                    System.out.println( "[Polynomial] TransformedPolygons: " + p.xpoints[i] + " " + p.ypoints[i] );
                }
            }

            return transformedPolygonList;
        }

        return null;
    }

    @Override
    public TransformationType getType() {

        return TransformationType.PolynomialFirstOrder;
    }

}
