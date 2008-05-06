//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
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

package org.deegree.model.geometry.jtswrapper;

import java.util.List;

import org.deegree.model.geometry.primitive.CurveSegment;
import org.deegree.model.geometry.primitive.Point;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public class JTSWrapperCurveSegment implements CurveSegment {

    private List<Point> points;

    private double[] coordinates;

    private int coordinateDimension = 0;

    /**
     * 
     * @param points
     *            list of {@link Point}s forming a {@link CurveSegment}
     */
    public JTSWrapperCurveSegment( List<Point> points ) {
        this.points = points;
        coordinateDimension = points.get( 0 ).getCoordinateDimension();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.CurveSegment#getAsArray()
     */
    public double[] getAsArray() {
        if ( coordinates == null ) {
            coordinates = new double[points.size()];
            int k = 0;
            for ( int i = 0; i < points.size(); i++ ) {
                Point point = points.get( i );
                coordinates[k++] = point.getX();
                coordinates[k++] = point.getY();
                if ( coordinateDimension == 3 ) {
                    coordinates[k++] = point.getZ();
                }
            }
        }
        return coordinates;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.CurveSegment#getCoordinateDimension()
     */
    public int getCoordinateDimension() {
        return coordinateDimension;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.CurveSegment#getPoints()
     */
    public List<Point> getPoints() {
        return points;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.CurveSegment#getX()
     */
    public double[] getX() {
        double[] x = new double[coordinates.length];
        int k = 0;
        for ( int i = 0; i < x.length; i++ ) {
            x[i] = coordinates[k];
            k += coordinateDimension;
        }
        return x;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.CurveSegment#getY()
     */
    public double[] getY() {
        double[] y = new double[coordinates.length];
        int k = 1;
        for ( int i = 0; i < y.length; i++ ) {
            y[i] = coordinates[k];
            k += coordinateDimension;
        }
        return y;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.CurveSegment#getZ()
     */
    public double[] getZ() {
        double[] z = new double[coordinates.length];
        if ( coordinateDimension == 3 ) {
            int k = 2;
            for ( int i = 0; i < z.length; i++ ) {
                z[i] = coordinates[k];
                k += coordinateDimension;
            }
        } else {
            for ( int i = 0; i < z.length; i++ ) {
                z[i] = Double.NaN;
            }
        }
        return z;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.CurveSegment#getInterpolation()
     */
    public INTERPOLATION getInterpolation() {
        // JTS does not support other interpolations
        return INTERPOLATION.linear;
    }

}
