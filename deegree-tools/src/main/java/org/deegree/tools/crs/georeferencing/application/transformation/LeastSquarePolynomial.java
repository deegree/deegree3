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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.commons.utils.Pair;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.transformations.polynomial.LeastSquareApproximation;
import org.deegree.cs.transformations.polynomial.PolynomialTransformation;
import org.deegree.tools.crs.georeferencing.application.Scene2DValues;
import org.deegree.tools.crs.georeferencing.model.Footprint;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LeastSquarePolynomial extends AbstractTransformation implements TransformationMethod {

    private static Logger log = LoggerFactory.getLogger( LeastSquarePolynomial.class );

    public LeastSquarePolynomial( List<Pair<Point4Values, Point4Values>> mappedPoints, Footprint footPrint,
                                  Scene2DValues sceneValues, CRS sourceCRS, CRS targetCRS, final int order ) {
        super( mappedPoints, footPrint, sceneValues, sourceCRS, targetCRS, order );

    }

    @Override
    public List<Polygon> computePolygonList() {

        List<Point3d> from = new ArrayList<Point3d>();
        List<Point3d> to = new ArrayList<Point3d>();
        List<Polygon> transformedPolygonList = new ArrayList<Polygon>();

        for ( Pair<Point4Values, Point4Values> points : mappedPoints ) {
            AbstractGRPoint firstPoint = points.first.getWorldCoords();
            AbstractGRPoint secondPoint = points.second.getWorldCoords();
            from.add( new Point3d( firstPoint.getX(), firstPoint.getY(), 0 ) );
            to.add( new Point3d( secondPoint.getX(), secondPoint.getY(), 0 ) );

        }

        if ( from.size() != to.size() ) {
            log.error( "The number of coordinates in the from file( " + from.size() + ") differ from the targetFile ("
                       + to.size() + ") , this maynot be!" );

        }
        List<Double> params = new LinkedList<Double>();
        params.add( new Double( 1 ) );
        PolynomialTransformation transform = null;
        try {
            transform = new LeastSquareApproximation( params, params, sourceCRS.getWrappedCRS(),
                                                      targetCRS.getWrappedCRS(), 1, 1, getIdentifiable() );

        } catch ( UnknownCRSException e ) {
            e.printStackTrace();
        }

        if ( transform != null ) {
            float[][] calculatedParams = transform.createVariables( from, to, order );

            for ( Polygon po : footPrint.getWorldCoordinatePolygonList() ) {

                int[] x2 = new int[po.npoints];
                int[] y2 = new int[po.npoints];
                for ( int i = 0; i < po.npoints; i++ ) {

                    Point3d point3d = null;
                    try {
                        point3d = transform.doTransform( new Point3d( po.xpoints[i], po.ypoints[i], 0 ) );
                    } catch ( TransformationException e ) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    AbstractGRPoint convertPoint = new GeoReferencedPoint( point3d.getX(), point3d.getY() );
                    int[] value = sceneValues.getPixelCoordinatePolygon( convertPoint );
                    x2[i] = value[0];
                    y2[i] = value[1];
                }

                Polygon p = new Polygon( x2, y2, po.npoints );
                transformedPolygonList.add( p );
            }

            try {
                transform.applyPolynomial( footPrint.getWorldCoordinatePointsList() );
            } catch ( TransformationException e ) {
                e.printStackTrace();
            }

            // StringBuilder sb = new StringBuilder();
            // for ( int i = 0; i < calculatedParams.length; ++i ) {
            // String t = "crs:" + ( i == 0 ? "x" : "y" ) + "Parameters>";
            // sb.append( "<" ).append( t );
            // for ( int y = 0; y < calculatedParams[i].length; ++y ) {
            // sb.append( calculatedParams[i][y] );
            // if ( ( y + 1 ) < calculatedParams[i].length ) {
            // sb.append( " " );
            // }
            // }
            // sb.append( "</" ).append( t ).append( "\n" );
            // }
            // System.out.println( "[LeastSquarePolynomial] Transformation: " + sb );
        }

        for ( Polygon p : transformedPolygonList ) {
            for ( int i = 0; i < p.npoints; i++ ) {
                System.out.println( "[LeastSquarePolynomial] TransformedPolygons: " + p.xpoints[i] + " " + p.ypoints[i] );
            }
        }

        return transformedPolygonList;
    }

    @Override
    public TransformationType getType() {

        return TransformationType.PolynomialFirstOrder;
    }

}
