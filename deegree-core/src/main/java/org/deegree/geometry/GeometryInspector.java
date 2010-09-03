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
package org.deegree.geometry;

import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.segments.CurveSegment;

/**
 * Enables the inspection of {@link Geometry} objects created in a {@link GeometryFactory}.
 * <p>
 * Implementations can perform such tasks as topological validation or repairing of defects.
 * </p>
 * 
 * @see GeometryFactory
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface GeometryInspector {

    /**
     * Invokes the inspection of the given {@link Geometry}.
     * 
     * @param geom
     *            geometry to be inspected, never <code>null</code>
     * @return inspected geometry, may be a different (repaired) instance, but must have exactly the same subinterface
     * @throws GeometryInspectionException
     *             if the inspector rejects the {@link Geometry}
     */
    public Geometry inspect( Geometry geom )
                            throws GeometryInspectionException;

    /**
     * Invokes the inspection of the given {@link CurveSegment}.
     * 
     * @param segment
     *            segment to be inspected, never <code>null</code>
     * @return inspected segment, may be a different (repaired) instance, but must be the exact same subinterface
     * @throws GeometryInspectionException
     *             if the inspector rejects the {@link CurveSegment}
     */
    public CurveSegment inspect( CurveSegment segment )
                            throws GeometryInspectionException;

    /**
     * Invokes the inspection of the given {@link SurfacePatch}.
     * 
     * @param patch
     *            patch to be inspected, never <code>null</code>
     * @return inspected patch, may be a different (repaired) instance, but must be the exact same subinterface
     * @throws GeometryInspectionException
     *             if the inspector rejects the {@link SurfacePatch}
     */
    public SurfacePatch inspect( SurfacePatch patch )
                            throws GeometryInspectionException;

    /**
     * Invokes the inspection of the given {@link Points}.
     * 
     * @param points
     *            points to be inspected, never <code>null</code>
     * @return inspected patch, may be a different (repaired) instance
     * @throws GeometryInspectionException
     *             if the inspector rejects the {@link Points}
     */
    public Points inspect( Points points );
}
