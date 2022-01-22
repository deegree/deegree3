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

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.OrientableCurve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.standard.AbstractDefaultGeometry;

/**
 * Default implementation of {@link OrientableCurve}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultOrientableCurve extends AbstractDefaultGeometry implements OrientableCurve {

    private String id;

    private ICRS crs;

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
    public DefaultOrientableCurve( String id, ICRS crs, Curve baseCurve, boolean isReversed ) {
        super( id, crs, null );
        this.baseCurve = baseCurve;
        this.isReversed = isReversed;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ICRS getCoordinateSystem() {
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
    public boolean crosses( Geometry geometry ) {
        return baseCurve.crosses( geometry );
    }

    @Override
    public Geometry getDifference( Geometry geometry ) {
        return baseCurve.getDifference( geometry );
    }

    @Override
    public Measure getDistance( Geometry geometry, Unit requestedUnit ) {
        return baseCurve.getDistance( geometry, requestedUnit );
    }

    @Override
    public boolean equals( Geometry geometry ) {
        return baseCurve.equals( geometry );
    }

    @Override
    public Pair<Point, Point> getBoundary() {
        return baseCurve.getBoundary();
    }

    @Override
    public Geometry getBuffer( Measure distance ) {
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
    public Measure getLength( Unit requestedUnit ) {
        return baseCurve.getLength( requestedUnit );
    }

    @Override
    public PrecisionModel getPrecision() {
        return baseCurve.getPrecision();
    }

    @Override
    public Geometry getIntersection( Geometry geometry ) {
        return baseCurve.getIntersection( geometry );
    }

    @Override
    public boolean intersects( Geometry geometry ) {
        return baseCurve.intersects( geometry );
    }

    @Override
    public boolean isDisjoint( Geometry geometry ) {
        return baseCurve.isDisjoint( geometry );
    }

    @Override
    public boolean overlaps( Geometry geometry ) {
        return baseCurve.overlaps( geometry );
    }

    @Override
    public boolean touches( Geometry geometry ) {
        return baseCurve.touches( geometry );
    }

    @Override
    public boolean isBeyond( Geometry geometry, Measure distance ) {
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
    public boolean isWithinDistance( Geometry geometry, Measure distance ) {
        return baseCurve.isWithinDistance( geometry, distance );
    }

    @Override
    public Geometry getUnion( Geometry geometry ) {
        return baseCurve.getUnion( geometry );
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
    public Points getControlPoints() {
        throw new RuntimeException( "not implemented yet" );
    }

    @Override
    public org.locationtech.jts.geom.Geometry getJTSGeometry() {
        // TODO Auto-generated method stub
        return null;
    }
}
