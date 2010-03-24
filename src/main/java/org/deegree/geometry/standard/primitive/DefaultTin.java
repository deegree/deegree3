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

import org.deegree.commons.uom.Length;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.crs.CRS;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Tin;
import org.deegree.geometry.primitive.patches.Triangle;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.standard.AbstractDefaultGeometry;

/**
 * Default implementation of {@link Tin}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultTin extends AbstractDefaultGeometry implements Tin {

    private List<Triangle> patches;

    private List<List<LineStringSegment>> stopLines;

    private List<List<LineStringSegment>> breakLines;

    private Length maxLength;

    private Points controlPoints;

    /**
     * Creates a new {@link DefaultTin} instance from the given parameters.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param pm
     *            precision model, may be null
     * @param patches
     *            the triangle that constitute the result of the triangulation
     */
    public DefaultTin( String id, CRS crs, PrecisionModel pm, List<Triangle> patches ) {
        super( id, crs, pm );
        this.patches = patches;
    }

    /**
     * Creates a new {@link DefaultTin} instance from the given parameters.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param pm
     *            precision model, may be null
     * @param stopLines
     * @param breakLines
     * @param maxLength
     * @param controlPoints
     * @param patches
     *            the triangle that constitute the result of the triangulation
     */
    public DefaultTin( String id, CRS crs, PrecisionModel pm, List<List<LineStringSegment>> stopLines,
                       List<List<LineStringSegment>> breakLines, Length maxLength, Points controlPoints,
                       List<Triangle> patches ) {
        super( id, crs, pm );
        this.stopLines = stopLines;
        this.breakLines = breakLines;
        this.maxLength = maxLength;
        this.controlPoints = controlPoints;
        this.patches = patches;
    }

    @Override
    public int getCoordinateDimension() {
        return patches.get( 0 ).getCoordinateDimension();
    }

    @Override
    public Measure getArea( Unit requestedBaseUnit ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point getCentroid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Triangle> getPatches() {
        if ( patches == null )
            throw new UnsupportedOperationException();
        return patches;
    }

    @Override
    public Measure getPerimeter( Unit requestedUnit ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrimitiveType getPrimitiveType() {
        return PrimitiveType.Surface;
    }

    @Override
    public SurfaceType getSurfaceType() {
        return SurfaceType.Tin;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.PRIMITIVE_GEOMETRY;
    }

    @Override
    public List<List<LineStringSegment>> getStopLines() {
        return stopLines;
    }

    @Override
    public List<List<LineStringSegment>> getBreakLines() {
        return breakLines;
    }

    @Override
    public Measure getMaxLength(Unit requestedUnit) {
        return maxLength;
    }

    @Override
    public Points getControlPoints() {
        return controlPoints;
    }

    @Override
    public Points getExteriorRingCoordinates() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Points> getInteriorRingsCoordinates() {
        throw new UnsupportedOperationException();
    }
}
