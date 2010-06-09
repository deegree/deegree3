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
package org.deegree.tools.crs.georeferencing.model;

import javax.vecmath.Point2d;

import org.deegree.commons.utils.Pair;

/**
 * 
 * Holds the horizontal referencing of the component. There should be a mapping from the footprint-component to the
 * georeferenced map.
 * 
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class HorizontalReferencingPoint extends Pair<Point2d, Point2d> {

    private Pair<Point2d, Point2d> pair1;

    private Pair<Point2d, Point2d> pair2;

    private Pair<Point2d, Point2d> pair3;
    
    private FootprintPoint fp;
    
    private GeoReferencedPoint gp;

    private Pair<FootprintPoint, GeoReferencedPoint>[] pairArray;

    // public HorizontalReferencingPoint( Pair<Point2d, Point2d> pair1, Pair<Point2d, Point2d> pair2,
    // Pair<Point2d, Point2d> pair3 ) {
    // this.pair1 = pair1;
    // this.pair2 = pair2;
    // this.pair3 = pair3;
    // this.pairArray = new Pair[] { pair1, pair2, pair3 };
    //
    // }
    //
    // public HorizontalReferencingPoint( Pair<Point2d, Point2d> pair1, Pair<Point2d, Point2d> pair2 ) {
    // this( pair1, pair2, null );
    // }
    //
    // public HorizontalReferencingPoint( Pair<Point2d, Point2d> pair1 ) {
    // this( pair1, null );
    // }

    public HorizontalReferencingPoint() {
        this.pairArray = new Pair[] {};
    }
    
    public void setFootprintPoint(FootprintPoint fp){
        this.fp = fp;
        if(this.pairArray != null){
            for(Pair<FootprintPoint, GeoReferencedPoint> pair : pairArray){
                if(pair.second == null || pair.first == null){
                    pair.first = fp;
                }
            }
        }
    }
    
    public void setGeoReferencedPoint(GeoReferencedPoint gp){
        this.gp = gp;
    }

    public Pair<Point2d, Point2d> getPair1() {
        return pair1;
    }

//    public void setPair1( Pair<Point2d, Point2d> pair1 ) {
//        this.pair1 = pair1;
//        this.pairArray[0] = pair1;
//    }
//
//    public Pair<Point2d, Point2d> getPair2() {
//        return pair2;
//    }
//
//    public void setPair2( Pair<Point2d, Point2d> pair2 ) {
//        this.pair2 = pair2;
//        this.pairArray[1] = pair2;
//    }
//
//    public Pair<Point2d, Point2d> getPair3() {
//        return pair3;
//    }
//
//    public void setPair3( Pair<Point2d, Point2d> pair3 ) {
//        this.pair3 = pair3;
//        this.pairArray[2] = pair3;
//    }
//
//    public Pair<Point2d, Point2d>[] getPairArray() {
//        return pairArray;
//    }

}