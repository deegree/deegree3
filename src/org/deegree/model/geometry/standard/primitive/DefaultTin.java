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
package org.deegree.model.geometry.standard.primitive;

import java.util.List;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Tin;
import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;
import org.deegree.model.geometry.primitive.surfacepatches.SurfacePatch;
import org.deegree.model.geometry.standard.AbstractDefaultGeometry;
import org.deegree.model.gml.Length;

/**
 * Default implementation of {@link Tin}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultTin extends AbstractDefaultGeometry implements Tin {

    private List<List<LineStringSegment>> stopLines;

    private List<List<LineStringSegment>> breakLines;

    private Length maxLength;

    private List<Point> controlPoints;

    /**
     * Creates a new {@link DefaultTin} instance from the given parameters.
     * 
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param stopLines 
     * @param breakLines 
     * @param maxLength 
     * @param controlPoints 
     */
    public DefaultTin( String id, CoordinateSystem crs, List<List<LineStringSegment>> stopLines, List<List<LineStringSegment>> breakLines,
                       Length maxLength, List<Point> controlPoints ) {
        super( id, crs );
        this.stopLines = stopLines;
        this.breakLines = breakLines;
        this.maxLength = maxLength;
        this.controlPoints = controlPoints;
    }

    @Override
    public double getArea() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point getCentroid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<SurfacePatch> getPatches() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getPerimeter() {
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
    public Length getMaxLength() {
        return maxLength;
    }

    @Override
    public List<Point> getControlPoints() {
        return controlPoints;
    }
}
