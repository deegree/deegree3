//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.geometry.validation.event;

import java.util.List;

import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.patches.PolygonPatch;

/**
 * {@link GeometryValidationEvent} that indicates the orientation of an interior {@link Ring} of a {@link PolygonPatch}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class InteriorRingOrientation extends AbstractGeometryValidationEvent {

    private final PolygonPatch patch;

    private final int ringIdx;

    private final boolean isClockwise;

    /**
     * Creates a new {@link InteriorRingOrientation} instance.
     * 
     * @param patch
     *            patch that the ring belongs to, never <code>null</code>
     * @param ringIdx
     *            index of the affected interior ring (starting at 0)
     * @param isClockwise
     *            <code>true</code> if orientation is clockwise, <code>false</code> if counter-clockwise
     * @param geometryParticleHierarchy
     *            list of affected geometry particles (that the patch is part of), must not be <code>null</code>
     */
    public InteriorRingOrientation( PolygonPatch patch, int ringIdx, boolean isClockwise,
                                    List<Object> geometryParticleHierarchy ) {
        super( geometryParticleHierarchy );
        this.patch = patch;
        this.ringIdx = ringIdx;
        this.isClockwise = isClockwise;
    }

    /**
     * Returns the affected {@link PolygonPatch} geometry.
     * 
     * @return affected patch, never <code>null</code>
     */
    public PolygonPatch getPatch() {
        return patch;
    }

    /**
     * Returns the index of the affected interior ring (starting at 0).
     * 
     * @return index of the affected interior ring
     */
    public int getRingIdx() {
        return ringIdx;
    }

    /**
     * Returns the orientation.
     * 
     * @return <code>true</code> if orientation is clockwise, <code>false</code> if counter-clockwise
     */
    public boolean isClockwise() {
        return isClockwise;
    }

    /**
     * Returns true if the geometry is an interior boundary.
     * 
     * @return <code>true</code> if geometry is an interior boundary, <code>false</code> if it's exterior
     */
    public boolean isInterior() {
        boolean isInterior = isClockwise;

        if ( isLeftHanded( patch.getInteriorRings().get( ringIdx ).getCoordinateSystem() ) ) {
            isInterior = !isInterior;
        }

        return isInterior;
    }

}
