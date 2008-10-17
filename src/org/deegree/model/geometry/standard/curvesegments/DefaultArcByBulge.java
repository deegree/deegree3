//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.geometry.standard.curvesegments;

import java.util.ArrayList;
import java.util.List;

import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.curvesegments.ArcByBulge;
import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;

/**
 * Default implementation of {@link ArcByBulge}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultArcByBulge implements ArcByBulge {

    private List<Point> controlPoints;

    private double bulge;

    private Point normal;

    public DefaultArcByBulge( Point p1, Point p2, double bulge, Point normal ) {
        this.controlPoints = new ArrayList<Point>();
        controlPoints.add( p1 );
        controlPoints.add( p2 );
        this.bulge = bulge;
        this.normal = normal;
    }

    @Override
    public Point getPoint1() {
        return controlPoints.get( 0 );
    }

    @Override
    public Point getPoint2() {
        return controlPoints.get( 1 );
    }        
    
    @Override
    public double getBulge() {
        return bulge;
    }

    @Override
    public Point getNormal() {
        return normal;
    }
    
    @Override
    public double[] getBulges() {
        return new double[] { bulge };
    }

    @Override
    public List<Point> getNormals() {
        List<Point> normals = new ArrayList<Point>(1);
        normals.add( normal );
        return normals;
    }

    @Override
    public int getNumArcs() {
        return 1;
    }

    @Override
    public double[] getAsArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LineStringSegment getAsLineStringSegment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCoordinateDimension() {
        return 0;
    }

    @Override
    public Interpolation getInterpolation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Point> getPoints() {
        return controlPoints;
    }

    @Override
    public double[] getX() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double[] getY() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double[] getZ() {
        throw new UnsupportedOperationException();
    }
}
