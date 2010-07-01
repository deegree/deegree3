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
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.CRS;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.helmert.Helmert;
import org.deegree.cs.transformations.polynomial.LeastSquareApproximation;
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
public class HelmertTransform implements TransformationMethod {

    private List<Pair<Point4Values, Point4Values>> mappedPoints;

    private Footprint footPrint;

    private Scene2DValues sceneValues;

    private CRS sourceCRS;

    private CRS targetCRS;

    public HelmertTransform( List<Pair<Point4Values, Point4Values>> mappedPoints, Footprint footPrint,
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

            // double[] ordinatesSrc = new double[arraySize];
            // double[] ordinatesDst = new double[arraySize];

            List<Double> ordinateSRCList = new ArrayList<Double>();
            List<Double> ordinateDSTList = new ArrayList<Double>();
            int counterSrc = 0;
            int counterDst = 0;

            for ( Pair<Point4Values, Point4Values> p : mappedPoints ) {

                // double[] from = new double[3];
                double x = p.first.getWorldCoords().getX();
                double y = p.first.getWorldCoords().getY();

                // from[0] = x;
                // from[1] = y;
                // from[2] = 0;
                // coordinateList.add( from );
                // System.out.println( "Before transform: " + x + " " + y );

                // ordinatesSrc[counterSrc] = x;
                // ordinatesSrc[++counterSrc] = y;
                ordinateSRCList.add( x );
                ordinateSRCList.add( y );
                counterSrc++;
                Point4Values pValue = p.second;
                x = pValue.getWorldCoords().getX();
                y = pValue.getWorldCoords().getY();

                // ordinatesDst[counterDst] = x;
                // ordinatesDst[++counterDst] = y;
                ordinateDSTList.add( x );
                ordinateDSTList.add( y );
                counterDst++;

            }
            // List<double[]> ordinateSRCList = Arrays.asList( ordinatesSrc );
            // List<double[]> ordinateDSTList = Arrays.asList( ordinatesDst );
            Transformation transform = null;
            try {
                transform = new LeastSquareApproximation( ordinateSRCList, ordinateDSTList, sourceCRS.getWrappedCRS(),
                                                          targetCRS.getWrappedCRS(), 0, 0, identifiable );
            } catch ( UnknownCRSException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // transform.doTransform( srcPts );

            Helmert wgs_info = null;
            try {
                wgs_info = new Helmert( sourceCRS.getWrappedCRS(), targetCRS.getWrappedCRS(), codeTypes );
            } catch ( UnknownCRSException e1 ) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        throw new NotImplementedException( "not implemented yet" );
    }

    @Override
    public TransformationType getType() {

        return TransformationType.Helmert;
    }

}
