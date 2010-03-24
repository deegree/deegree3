//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/geometry/primitive/Surface.java $
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
package org.deegree.geometry.primitive;

import java.util.List;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.segments.LineStringSegment;

/**
 * A {@link TriangulatedSurface} that uses the Delaunay algorithm or a similar algorithm complemented with consideration
 * of breaklines, stoplines, and maximum length of triangle sides. These networks satisfy the Delaunay's criterion away
 * from the modifications: For each triangle in the network, the circle passing through its vertices does not contain,
 * in its interior, the vertex of any other triangle.
 * <p>
 * One can notice that the useful information provided for the Tin element is solely the trianglePatches, since the
 * stopLines and breakLines (along with maxLength and ControlPoints) are only needed to obtain the triangulation.
 * However, GML allows to specify both, so the interface provides access to them.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public interface Tin extends TriangulatedSurface {

    /**
     * Must always return {@link Surface.SurfaceType#Tin}.
     * 
     * @return {@link Surface.SurfaceType#Tin}
     */
    public SurfaceType getSurfaceType();

    /**
     * Returns the stop lines that must be respected by the triangulation.
     * 
     * @return the stop lines
     */
    public List<List<LineStringSegment>> getStopLines();

    /**
     * Returns the break lines that must be respected by the triangulation.
     * 
     * @return the break lines
     */
    public List<List<LineStringSegment>> getBreakLines();

    /**
     * Returns the maximum length of all triangle side.
     * 
     * @param uom
     *            units-of-measure that the length shall be expressed as, or null for units of the underlying coordinate
     *            system
     * @return the length in the the requested uom
     */
    public Measure getMaxLength( Unit uom );

    /**
     * Returns the control points (vertices) of the triangles.
     * 
     * @return the control points
     */
    public Points getControlPoints();
}
