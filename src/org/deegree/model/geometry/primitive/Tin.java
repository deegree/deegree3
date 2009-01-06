//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/geometry/primitive/Surface.java $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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
package org.deegree.model.geometry.primitive;

import java.util.List;

import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;
import org.deegree.model.gml.Length;

/**
 * A {@link Tin} is a {@link TriangulatedSurface} that uses the Delauny algorithm or a similar algorithm complemented
 * with consideration of breaklines, stoplines, and maximum length of triangle sides. These networks satisfy the
 * Delauny's criterion away from the modifications: Foreeach triangle in the network, the circle passing through its
 * vertices does not contain, in its interior, the vertex of any other triangle.
 * <p>
 * NOTE: In GML 3.1.1, <code>gml:TinType</code> extends <code>gml:TriangulatedSurface</code>. This means that a
 * <code>gml:Tin</code> element contains both trianglePatches and controlPoint properties!? This is apparently
 * redundant, and consequently (?) GML 3.2.1 only allows the controlPoint property...
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version. $Revision: 14412 $, $Date: 2008-10-23 19:29:57 +0200 (Do, 23 Okt 2008) $
 */
public interface Tin extends Surface {

    /**
     * Must always return {@link Surface.SurfaceType#Tin}.
     * 
     * @return {@link Surface.SurfaceType#Tin}
     */
    @Override
    public SurfaceType getSurfaceType();

    public List<List<LineStringSegment>> getStopLines();

    public List<List<LineStringSegment>> getBreakLines();

    public Length getMaxLength();

    public List<Point> getControlPoints();
}
