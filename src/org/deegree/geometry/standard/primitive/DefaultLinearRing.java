//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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
package org.deegree.geometry.standard.primitive;

import java.util.List;

import org.deegree.crs.CRS;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.standard.curvesegments.DefaultLineStringSegment;

/**
 * Default implementation of {@link Ring}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class DefaultLinearRing extends DefaultRing implements LinearRing {

    private List<Point> controlPoints;

    /**
     * Creates a new <code>DefaultRing</code> instance from the given parameters.
     *
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param controlPoints
     *
     */
    public DefaultLinearRing( String id, CRS crs, List<Point> controlPoints ) {
        super( id, crs, new DefaultLineStringSegment(controlPoints));
        this.controlPoints = controlPoints;
    }

    @Override
    public boolean is3D(){
        return controlPoints.get( 0 ).is3D();
    }

    @Override
    public CurveType getCurveType() {
        return CurveType.Ring;
    }

    @Override
    public RingType getRingType() {
        return RingType.LinearRing;
    }

    @Override
    public LineString getAsLineString() {
        return (LineString) members.get( 0 );
    }

    @Override
    public double[] getAsArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Point> getControlPoints() {
        return ((LineString) members.get( 0 )).getControlPoints();
    }
}
