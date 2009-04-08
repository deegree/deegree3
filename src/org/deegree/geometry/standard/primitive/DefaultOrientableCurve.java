//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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

package org.deegree.geometry.standard.primitive;

import java.util.ArrayList;
import java.util.List;

import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.OrientableCurve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.curvesegments.CurveSegment;
import org.deegree.geometry.primitive.curvesegments.LineStringSegment;
import org.deegree.geometry.primitive.curvesegments.CurveSegment.CurveSegmentType;

/**
 * Default implementation of {@link OrientableCurve}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultOrientableCurve implements OrientableCurve {

    private String id;

    private CoordinateSystem crs;

    private Curve baseCurve;

    private boolean isReversed;

    /**
     * Creates a new <code>DefaultOrientableCurve</code> instance from the given parameters.
     * 
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param baseCurve
     *            base curve
     * @param isReversed
     *            set to true, if the order of the base curve shall be reversed
     */
    public DefaultOrientableCurve( String id, CoordinateSystem crs, Curve baseCurve, boolean isReversed ) {
        this.id = id;
        this.crs = crs;
        this.baseCurve = baseCurve;
        this.isReversed = isReversed;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public CoordinateSystem getCoordinateSystem() {
        return crs;
    }

    @Override
    public LineString getAsLineString() {
        return baseCurve.getAsLineString();
    }

    @Override
    public List<CurveSegment> getCurveSegments() {
        return baseCurve.getCurveSegments();
    }

    @Override
    public CurveType getCurveType() {
        return CurveType.OrientableCurve;
    }

    @Override
    public Curve getBaseCurve() {
        return baseCurve;
    }

    @Override
    public boolean isReversed() {
        return isReversed;
    }

    @Override
    public Point getEndPoint() {
        if ( isReversed ) {
            return baseCurve.getStartPoint();
        }
        return baseCurve.getEndPoint();
    }

    @Override
    public Point getStartPoint() {
        if ( isReversed ) {
            return baseCurve.getEndPoint();
        }
        return baseCurve.getStartPoint();
    }

    // -----------------------------------------------------------------------
    // Curve methods that are just delegated to the wrapped base curve
    // -----------------------------------------------------------------------

    @Override
    public boolean contains( Geometry geometry ) {
        return baseCurve.contains( geometry );
    }

    @Override
    public Geometry difference( Geometry geometry ) {
        return baseCurve.difference( geometry );
    }

    @Override
    public double distance( Geometry geometry ) {
        return baseCurve.distance( geometry );
    }

    @Override
    public boolean equals( Geometry geometry ) {
        return baseCurve.equals( geometry );
    }

    @Override
    public List<Point> getBoundary() {
        return baseCurve.getBoundary();
    }

    @Override
    public Geometry getBuffer( double distance ) {
        return baseCurve.getBuffer( distance );
    }

    @Override
    public Geometry getConvexHull() {
        return baseCurve.getConvexHull();
    }

    @Override
    public int getCoordinateDimension() {
        return baseCurve.getCoordinateDimension();
    }

    @Override
    public Envelope getEnvelope() {
        return baseCurve.getEnvelope();
    }

    @Override
    public double getLength() {
        return baseCurve.getLength();
    }

    @Override
    public double getPrecision() {
        return baseCurve.getPrecision();
    }

    @Override
    public Geometry intersection( Geometry geometry ) {
        return baseCurve.intersection( geometry );
    }

    @Override
    public boolean intersects( Geometry geometry ) {
        return baseCurve.intersects( geometry );
    }

    @Override
    public boolean isBeyond( Geometry geometry, double distance ) {
        return baseCurve.isBeyond( geometry, distance );
    }

    @Override
    public boolean isClosed() {
        return baseCurve.isClosed();
    }

    @Override
    public boolean isWithin( Geometry geometry ) {
        return baseCurve.isWithin( geometry );
    }

    @Override
    public boolean isWithinDistance( Geometry geometry, double distance ) {
        return baseCurve.isWithinDistance( geometry, distance );
    }

    @Override
    public Geometry union( Geometry geometry ) {
        return baseCurve.union( geometry );
    }

    @Override
    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.Curve;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.PRIMITIVE_GEOMETRY;
    }

    @Override
    public List<Point> getControlPoints() {
        throw new RuntimeException( "not implemented yet" );
    }
}
