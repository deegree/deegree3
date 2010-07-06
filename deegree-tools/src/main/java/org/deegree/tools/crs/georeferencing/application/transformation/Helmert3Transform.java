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

import java.util.List;

import org.deegree.commons.utils.Pair;
import org.deegree.cs.CRS;
import org.deegree.geometry.primitive.Ring;
import org.deegree.tools.crs.georeferencing.application.Scene2DValues;
import org.deegree.tools.crs.georeferencing.model.Footprint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Helmert3Transform extends AbstractTransformation implements TransformationMethod {

    public Helmert3Transform( List<Pair<Point4Values, Point4Values>> mappedPoints, Footprint footPrint,
                              Scene2DValues sceneValues, CRS sourceCRS, CRS targetCRS, final int order ) {
        super( mappedPoints, footPrint, sceneValues, targetCRS, targetCRS, order );
    }

    @Override
    public List<Ring> computeRingList() {
        // int arraySize = mappedPoints.size() * 2;
        // if ( arraySize > 0 ) {
        //
        // double[] ordinatesSrc = new double[arraySize];
        // double[] ordinatesDst = new double[arraySize];
        // List<Point3d> from = new ArrayList<Point3d>();
        // List<Point3d> to = new ArrayList<Point3d>();
        //
        // // List<Double> ordinateSRCList = new ArrayList<Double>();
        // // List<Double> ordinateDSTList = new ArrayList<Double>();
        // int counterSrc = 0;
        // int counterDst = 0;
        //
        // for ( Pair<Point4Values, Point4Values> p : mappedPoints ) {
        //
        // // double[] from = new double[3];
        // double x = p.first.getWorldCoords().getX();
        // double y = p.first.getWorldCoords().getY();
        //
        // // from[0] = x;
        // // from[1] = y;
        // // from[2] = 0;
        // // coordinateList.add( from );
        // // System.out.println( "Before transform: " + x + " " + y );
        // from.add( new Point3d( x, y, 0 ) );
        // ordinatesDst[counterDst] = x;
        // ordinatesDst[++counterDst] = y;
        // // ordinateSRCList.add( x );
        // // ordinateSRCList.add( y );
        // counterDst++;
        // Point4Values pValue = p.second;
        // x = pValue.getWorldCoords().getX();
        // y = pValue.getWorldCoords().getY();
        // to.add( new Point3d( x, y, 0 ) );
        // ordinatesSrc[counterSrc] = x;
        // ordinatesSrc[++counterSrc] = y;
        // // ordinateDSTList.add( x );
        // // ordinateDSTList.add( y );
        // counterSrc++;
        //
        // }
        // // List<Double> ordinateSRCList = Arrays.asList( ordinatesSrc );
        // // List<Double> ordinateDSTList = Arrays.asList( ordinatesDst );
        // PolynomialTransformation transform = null;
        // List<Double> params = new LinkedList<Double>();
        // params.add( new Double( 1 ) );
        // try {
        // transform = new LeastSquareApproximation( params, params, sourceCRS.getWrappedCRS(),
        // targetCRS.getWrappedCRS(), 0, 0, getIdentifiable() );
        // } catch ( UnknownCRSException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        //
        // // transform.doTransform( srcPts );
        //
        // Helmert helmert = null;
        // try {
        // float[][] calculatedParams = transform.createVariables( from, to, order );
        // helmert = new Helmert( calculatedParams[0][0], calculatedParams[1][0], 0, calculatedParams[0][1],
        // calculatedParams[1][1], 0, calculatedParams[0][2], sourceCRS.getWrappedCRS(),
        // targetCRS.getWrappedCRS(), getCRSCodeType() );
        // helmert.doTransform( ordinatesSrc, 0, ordinatesDst, 0, ordinatesSrc.length );
        // for ( int i = 0; i < ordinatesSrc.length; i++ ) {
        // System.out.println( ordinatesDst[i] );
        //
        // }
        // } catch ( UnknownCRSException e1 ) {
        // e1.printStackTrace();
        // } catch ( TransformationException e ) {
        // e.printStackTrace();
        // }
        // }
        // throw new NotImplementedException( "not implemented yet" );

        return null;
    }

    @Override
    public TransformationType getType() {

        return TransformationType.Helmert_3;
    }

}
