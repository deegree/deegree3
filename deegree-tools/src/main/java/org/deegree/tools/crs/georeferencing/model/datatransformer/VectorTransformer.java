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
package org.deegree.tools.crs.georeferencing.model.datatransformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;

import org.deegree.commons.utils.Triple;
import org.deegree.tools.crs.georeferencing.application.Scene2DValues;
import org.deegree.tools.crs.georeferencing.model.RowColumn;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;
import org.deegree.tools.crs.georeferencing.model.points.PointResidual;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class VectorTransformer {

    // private static final Vector<Vector<Double>> dataVector = new Vector<Vector<Double>>();

    private final Collection<Triple<Point4Values, Point4Values, PointResidual>> mappedPoints;

    // /**
    // * Prevents instantiation of VectorTransformer.
    // */
    // private VectorTransformer() {
    //
    // }

    // public static VectorTransformer newInstance( Vector<Vector<Double>> importedDataVector, Scene2DValues values ) {
    //
    // Point4Values pFirst;
    // Point4Values pSec;
    // PointResidual pr;
    //
    // // dataVector.addsAll( importedDataVector );
    // Enumeration<Vector<Double>> e1 = importedDataVector.elements();
    // int counterE1 = 0;
    // while ( e1.hasMoreElements() ) {
    // Vector<Double> e2 = e1.nextElement();
    // // Object[] e3 =
    // System.out.println( "[VectorTransformer] vectorsize: " + e2.size() );
    // // TODO test after size...
    // for ( int i = 0; i < e2.size(); i += 6 ) {
    // double a = e2.get( i );
    // double b = e2.get( i + 1 );
    // double c = e2.get( i + 2 );
    // double d = e2.get( i + 3 );
    // double f = e2.get( i + 4 );
    // double g = e2.get( i + 5 );
    // // double z = e2.get( i + 6 );
    // AbstractGRPoint pFootWorld = new FootprintPoint( c, d );
    // int initValueFootArray[] = values.getPixelCoord( pFootWorld );
    // AbstractGRPoint initValueFoot = new FootprintPoint( initValueFootArray[0], initValueFootArray[1] );
    //
    // AbstractGRPoint pGeoWorld = new GeoReferencedPoint( a, b );
    // int initValueGeoArray[] = values.getPixelCoord( pGeoWorld );
    // AbstractGRPoint initValueGeo = new GeoReferencedPoint( initValueGeoArray[0], initValueGeoArray[1] );
    //
    // pFirst = new Point4Values( initValueFoot, pFootWorld, new RowColumn( counterE1,
    // new Double( c ).intValue(),
    // new Double( d ).intValue() ) );
    // pSec = new Point4Values( initValueGeo, pGeoWorld, new RowColumn( counterE1, new Double( a ).intValue(),
    // new Double( b ).intValue() ) );
    // pr = new PointResidual( f, g );
    //
    // mappedPoints.add( new Triple<Point4Values, Point4Values, PointResidual>( pFirst, pSec, pr ) );
    // for ( Triple<Point4Values, Point4Values, PointResidual> s : mappedPoints ) {
    // System.out.println( "[VectorTransformer] mappedPoints " + s );
    // }
    // }
    // }
    //
    // return null;
    // }

    public VectorTransformer( Vector<Vector<Double>> importedDataVector, Scene2DValues values ) {

        mappedPoints = new ArrayList<Triple<Point4Values, Point4Values, PointResidual>>();
        Point4Values pFirst;
        Point4Values pSec;
        PointResidual pr;

        // dataVector.addsAll( importedDataVector );
        Enumeration<Vector<Double>> e1 = importedDataVector.elements();
        int counterE1 = 0;
        while ( e1.hasMoreElements() ) {
            Vector<Double> e2 = e1.nextElement();
            // Object[] e3 =
            System.out.println( "[VectorTransformer] vectorsize: " + e2.size() );
            // TODO test after size...
            for ( int i = 0; i < e2.size(); i += 6 ) {
                double a = e2.get( i );
                double b = e2.get( i + 1 );
                double c = e2.get( i + 2 );
                double d = e2.get( i + 3 );
                double f = e2.get( i + 4 );
                double g = e2.get( i + 5 );
                AbstractGRPoint pFootWorld = new FootprintPoint( c, d );
                int initValueFootArray[] = values.getPixelCoord( pFootWorld );
                AbstractGRPoint initValueFoot = new FootprintPoint( initValueFootArray[0], initValueFootArray[1] );

                AbstractGRPoint pGeoWorld = new GeoReferencedPoint( a, b );
                int initValueGeoArray[] = values.getPixelCoord( pGeoWorld );
                AbstractGRPoint initValueGeo = new GeoReferencedPoint( initValueGeoArray[0], initValueGeoArray[1] );

                pFirst = new Point4Values( initValueFoot, pFootWorld, new RowColumn( counterE1,
                                                                                     new Double( c ).intValue(),
                                                                                     new Double( d ).intValue() ) );
                pSec = new Point4Values( initValueGeo, pGeoWorld, new RowColumn( counterE1, new Double( a ).intValue(),
                                                                                 new Double( b ).intValue() ) );
                pr = new PointResidual( f, g );

                mappedPoints.add( new Triple<Point4Values, Point4Values, PointResidual>( pFirst, pSec, pr ) );
            }
        }
    }

    public Collection<Triple<Point4Values, Point4Values, PointResidual>> getMappedPoints() {
        return mappedPoints;
    }

}
