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
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.CoordinateTransformer;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.transformations.helmert.Helmert;
import org.deegree.tools.crs.georeferencing.model.Footprint;
import org.deegree.tools.crs.georeferencing.model.Scene2DValues;
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
public class Polynomial implements Transformation {

    private List<Pair<Point4Values, Point4Values>> mappedPoints;

    private Footprint footPrint;

    private Scene2DValues sceneValues;

    private CRS sourceCRS;

    private CRS targetCRS;

    public Polynomial( List<Pair<Point4Values, Point4Values>> mappedPoints, Footprint footPrint,
                       Scene2DValues sceneValues, CRS sourceCRS, CRS targetCRS ) {
        this.mappedPoints = mappedPoints;
        this.footPrint = footPrint;
        this.sceneValues = sceneValues;
        this.sourceCRS = sourceCRS;
        this.targetCRS = targetCRS;
    }

    @Override
    public List<Polygon> comuptePolygonList() {
        int arraySize = mappedPoints.size() * 2;
        if ( arraySize > 0 ) {

            CRSCodeType[] s = null;
            CRSCodeType[] t = null;
            try {
                s = sourceCRS.getWrappedCRS().getCodes();
                t = targetCRS.getWrappedCRS().getCodes();
            } catch ( UnknownCRSException e1 ) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            int size = s.length + t.length;
            int countT = 0;
            CRSCodeType[] codeTypes = new CRSCodeType[size];
            for ( int i = 0; i < s.length; i++ ) {
                codeTypes[i] = s[i];
            }
            for ( int i = s.length; i < size; i++ ) {
                codeTypes[i] = t[countT];
                countT++;
            }
            CRSIdentifiable identifiable = new CRSIdentifiable( codeTypes );

            // final Helmert wgs_info = new Helmert( sourceCRS, targetCRS, codeTypes );

            int arrSize = footPrint.getWorldCoordinates().length;
            // double[] ordinatesSrc = new double[arraySize];
            // double[] ordinatesDst = new double[arraySize];
            float[] ordinatesSrc = new float[arraySize];
            float[] ordinatesDst = new float[arraySize];
            int counterSrc = 0;
            int counterDst = 0;
            List<double[]> coordinateList = new LinkedList<double[]>();
            CoordinateTransformer ct;
            try {
                ct = new CoordinateTransformer( targetCRS.getWrappedCRS() );

                for ( Pair<Point4Values, Point4Values> p : mappedPoints ) {

                    // double[] from = new double[3];
                    double x = p.first.getWorldCoords().getX();
                    double y = p.first.getWorldCoords().getY();

                    // from[0] = x;
                    // from[1] = y;
                    // from[2] = 0;
                    // coordinateList.add( from );
                    // System.out.println( "Before transform: " + x + " " + y );

                    ordinatesDst[counterSrc] = (float) x;
                    ordinatesDst[++counterSrc] = (float) y;
                    // ordinatesSrc[counterSrc] = x;
                    // ordinatesSrc[++counterSrc] = y;
                    counterSrc++;
                    Point4Values pValue = p.second;
                    x = pValue.getWorldCoords().getX();
                    y = pValue.getWorldCoords().getY();
                    ordinatesSrc[counterDst] = (float) x;
                    ordinatesSrc[++counterDst] = (float) y;
                    // ordinatesDst[counterDst] = x;
                    // ordinatesDst[++counterDst] = y;
                    counterDst++;

                }
                double x;
                double y;
                for ( double[] c : coordinateList ) {
                    try {
                        double[] out = ct.transform( sourceCRS.getWrappedCRS(), c, new double[3] );

                        // for ( FootprintPoint d : footPrint.getWorldCoordinatePoints() ) {
                        // System.out.println( "newX: " + d.getX() * out[0] / c[0] );
                        // System.out.println( "newY: " + d.getY() * out[1] / c[1] );
                        // }
                        // double newX = 25.0 * out[0] / c[0];
                        // System.out.println( "After transform: " + out[0] + " " + out[1] );

                    } catch ( IllegalArgumentException e1 ) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch ( TransformationException e1 ) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch ( UnknownCRSException e1 ) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            } catch ( IllegalArgumentException e2 ) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            } catch ( UnknownCRSException e2 ) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }

            Helmert wgs_info = null;
            try {
                wgs_info = new Helmert( sourceCRS.getWrappedCRS(), targetCRS.getWrappedCRS(), codeTypes );
            } catch ( UnknownCRSException e1 ) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            System.out.println( "\n\n coordinates" );
            for ( int i = 0; i < ordinatesDst.length; i += 2 ) {
                System.out.println( ordinatesSrc[i] + "/" + ordinatesSrc[i + 1] + " -- " + ordinatesDst[i] + "/"
                                    + ordinatesDst[i + 1] );
            }
            WarpPolynomial warp = WarpPolynomial.createWarp( ordinatesSrc, 0, ordinatesDst, 0, ordinatesSrc.length, 1f,
                                                             1f, 1f, 1f, 1 );
            // for ( float p : warp.getXCoeffs() ) {
            // System.out.println( "warp: " + p );
            // }
            System.out.println( "coeff:" );
            float[] x = warp.getXCoeffs();
            float[] y = warp.getYCoeffs();
            for ( int i = 0; i < y.length; i++ ) {
                System.out.println( i + " " + x[i] + " " + y[i] );
            }

            List<Point3d> result = new ArrayList<Point3d>();
            System.out.println();
            System.out.println( "resid" );
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
                    int[] value = sceneValues.getPixelCoordinate( convertPoint );
                    x2[i] = value[0];
                    y2[i] = value[1];

                }

                Polygon p = new Polygon( x2, y2, po.npoints );
                transformedPolygonList.add( p );
            }
            // for ( int i = 0; i < ordinatesDst.length; i += 2 ) {
            // Point2D p = warp.mapDestPoint( new Point2D.Float( ordinatesDst[i], ordinatesDst[i + 1] ) );
            // // System.out.println( "p: " + p + " : " + p.getX() + " - " + ordinatesSrc[i] );
            // rx += ( p.getX() - ordinatesSrc[i] );
            // ry += ( p.getY() - ordinatesSrc[i + 1] );
            // System.out.println( ( i / 2 ) + " -> " + ( p.getX() - ordinatesSrc[i] ) + "/"
            // + ( p.getY() - ordinatesSrc[i + 1] ) );
            // result.add( new Point3d( p.getX(), p.getY(), 0 ) );
            //
            // }
            System.out.println();
            System.out.println( "mean resid" );
            rx /= ( ordinatesSrc.length / 2 );
            ry /= ( ordinatesSrc.length / 2 );
            System.out.println( rx + " " + ry );

            for ( Point3d p : result ) {
                System.out.println( p.getX() + " " + p.getY() );
            }
            // Matrix4d m = wgs_info.getAsAffineTransform();
            // try {
            // wgs_info.doTransform( ordinatesSrc, 0, ordinatesDst, 0, ordinatesSrc.length );
            // m = wgs_info.getAsAffineTransform();
            // } catch ( TransformationException e1 ) {
            // e1.printStackTrace();
            // }

            // for ( double d : ordinatesDst ) {
            // System.out.println( "WGS_INFO: " + m );
            // }

            return transformedPolygonList;
        } else {
            try {
                throw new Exception( "You must specify coordinates to transform" );
            } catch ( Exception e1 ) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public TransformationType getType() {

        return TransformationType.Polynomial;
    }

}
