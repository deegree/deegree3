//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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

package org.deegree.model.geometry.validation;

import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Ring;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.surfacepatches.PolygonPatch;

/**
 * Implementations of this interface
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public interface ValidationEventHandler {

    /**
     * Called when a sequence of two indentical control points in a {@link Curve} is detected.
     * 
     * @param curve
     *            invalid {@link Curve} geometry
     * @param point
     *            the duplicated point
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean curvePointDuplication( Curve curve, Point point );

    /**
     * Called when a discontinuity in a {@link Curve} is detected, i.e. the end point of segment does not coincide with
     * the start point of the next.
     * 
     * @param curve
     *            invalid {@link Curve} geometry
     * @param segmentIdx
     *            the index of the segment with the discontinuity
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean curveDiscontinuity( Curve curve, int segmentIdx );

    /**
     * Called when a self-intersection of a {@link Curve} is detected.
     * 
     * @param curve
     *            invalid {@link Curve} geometry
     * @param location
     *            the (approximative) location of the self-intersection
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean curveSelfIntersection( Curve curve, Point location );    

    /**
     * Called when a {@link Ring} is detected that is not closed.
     * 
     * @param ring
     *            invalid {@link Ring} geometry
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean ringNotClosed( Ring ring );

    /**
     * Called when a self-intersection of a {@link Ring} is detected.
     * 
     * @param ring
     *            invalid {@link Ring} geometry
     * @param location
     *            the (approximative) location of the self-intersection
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean ringSelfIntersection( Ring ring, Point location );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has an exterior ring with a wrong orientation, i.e.
     * 
     * @param surface
     * @param patch
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean exteriorRingCW( Surface surface, PolygonPatch patch );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has a interior ring with a wrong orientation, i.e.
     * 
     * @param surface
     * @param patch
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean interiorRingCCW( Surface surface, PolygonPatch patch );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has two holes (interior rings) that touch.
     * 
     * @param surface
     *            invalid {@link Surface} geometry
     * @param patch
     *            offending patch
     * @param ring1Idx
     *            index of the first ring involved
     * @param ring2Idx
     *            index of the second ring involved
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean interiorRingsTouch( Surface surface, PolygonPatch patch, int ring1Idx, int ring2Idx );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has two holes (interior rings) that intersect.
     * 
     * @param surface
     *            invalid {@link Surface} geometry
     * @param patch
     *            offending patch
     * @param ring1Idx
     *            index of the first ring involved
     * @param ring2Idx
     *            index of the second ring involved
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean interiorRingsIntersect( Surface surface, PolygonPatch patch, int ring1Idx, int ring2Idx );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has two holes (interior rings) that are nested, i.e.
     * one ring is completely inside the other.
     * 
     * @param surface
     *            invalid {@link Surface} geometry
     * @param patch
     *            offending patch
     * @param ring1Idx
     *            index of the first ring involved (the 'outer' one)
     * @param ring2Idx
     *            index of the second ring involved (the 'inner' one)
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean interiorRingsWithin( Surface surface, PolygonPatch patch, int ring1Idx, int ring2Idx );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has a hole (interior ring) that touches it's shell
     * (exterior ring).
     * 
     * @param surface
     *            invalid {@link Surface} geometry
     * @param patch
     *            offending patch
     * @param ringIdx
     *            index of the offending inner ring
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean interiorRingTouchesExterior( Surface surface, PolygonPatch patch, int ringIdx );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has a hole (interior ring) that intersects it's shell
     * (exterior ring).
     * 
     * @param surface
     *            invalid {@link Surface} geometry
     * @param patch
     *            offending patch
     * @param ringIdx
     *            index of the offending inner ring
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean interiorRingIntersectsExterior( Surface surface, PolygonPatch patch, int ringIdx );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has a hole (interior ring) that is completely located
     * outside it's shell (exterior ring).
     * 
     * @param surface
     *            invalid {@link Surface} geometry
     * @param patch
     *            offending patch
     * @param ringIdx
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean interiorRingOutsideExterior( Surface surface, PolygonPatch patch, int ringIdx );
}
