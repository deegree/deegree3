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

package org.deegree.geometry.validation;

import java.util.List;

import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.patches.PolygonPatch;

/**
 * Implementations of this interface are passed to {@link GeometryValidator} on construction.
 * 
 * @see GeometryValidator
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface GeometryValidationEventHandler {

    /**
     * Called when a sequence of two identical control points in a {@link Curve} is detected.
     * 
     * @param curve
     *            invalid {@link Curve} geometry, never <code>null</code>
     * @param point
     *            the duplicated point, never <code>null</code>
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of), never <code>null</code>
     * @return <code>true</code>, if the event should indicate that the geometry is invalid, otherwise
     *         <code>false</code>
     */
    public boolean curvePointDuplication( Curve curve, Point point, List<Object> affectedGeometryParticles );

    /**
     * Called when a discontinuity in a {@link Curve} is detected, i.e. the end point of segment does not coincide with
     * the start point of the next.
     * 
     * @param curve
     *            invalid {@link Curve} geometry, never <code>null</code>
     * @param segmentIdx
     *            the index of the segment with the discontinuity (starting at 0)
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of), never <code>null</code>
     * @return <code>true</code>, if the event indicates that the geometry is invalid, otherwise <code>false</code>
     */
    public boolean curveDiscontinuity( Curve curve, int segmentIdx, List<Object> affectedGeometryParticles );

    /**
     * Called when a self-intersection of a {@link Curve} is detected.
     * 
     * @param curve
     *            invalid {@link Curve} geometry, never <code>null</code>
     * @param location
     *            the (approximative) location of the self-intersection, may be <code>null</code>
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of), never <code>null</code>
     * @return <code>true</code>, if the event should indicate that the geometry is invalid, otherwise
     *         <code>false</code>
     */
    public boolean curveSelfIntersection( Curve curve, Point location, List<Object> affectedGeometryParticles );

    /**
     * Called when a {@link Ring} is detected that is not closed.
     * 
     * @param ring
     *            invalid {@link Ring} geometry, never <code>null</code>
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of), never <code>null</code>
     * @return <code>true</code>, if the event should indicate that the geometry is invalid, otherwise
     *         <code>false</code>
     */
    public boolean ringNotClosed( Ring ring, List<Object> affectedGeometryParticles );

    /**
     * Called when a self-intersection of a {@link Ring} is detected.
     * 
     * @param ring
     *            invalid {@link Ring} geometry, never <code>null</code>
     * @param location
     *            (approximate) location of the self-intersection, may be <code>null</code>
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of), never <code>null</code>
     * @return <code>true</code>, if the event should indicate that the geometry is invalid, otherwise
     *         <code>false</code>
     */
    public boolean ringSelfIntersection( Ring ring, Point location, List<Object> affectedGeometryParticles );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has an exterior ring with a wrong orientation, i.e.
     * clockwise.
     * 
     * @param patch
     *            patch with CW exterior ring, never <code>null</code>
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the patch is a part of), never <code>null</code>
     * @return <code>true</code>, if the event should indicate that the geometry is invalid, otherwise
     *         <code>false</code>
     */
    public boolean exteriorRingCW( PolygonPatch patch, List<Object> affectedGeometryParticles );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has an interior ring with a wrong orientation, i.e.
     * counter-clockwise.
     * 
     * @param patch
     *            patch with CCW exterior ring, never <code>null</code>
     * @param ringIdx
     *            index of the offending inner ring (starting at 0)
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the patch is a part of), never <code>null</code>
     * @return <code>true</code>, if the event should indicate that the geometry is invalid, otherwise
     *         <code>false</code>
     */
    public boolean interiorRingCCW( PolygonPatch patch, int ringIdx, List<Object> affectedGeometryParticles );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has two holes (interior rings) that touch.
     * 
     * @param patch
     *            offending patch, never <code>null</code>
     * @param ring1Idx
     *            index of the first interior ring involved (starting at 0)
     * @param ring2Idx
     *            index of the second interior ring involved (starting at 0)
     * @param location
     *            (approximate) location of the touching, may be <code>null</code>
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @return <code>true</code>, if the event should indicate that the geometry is invalid, otherwise
     *         <code>false</code>
     */
    public boolean interiorRingsTouch( PolygonPatch patch, int ring1Idx, int ring2Idx, Point location,
                                       List<Object> affectedGeometryParticles );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has two holes (interior rings) that intersect.
     * 
     * @param patch
     *            offending patch, never <code>null</code>
     * @param ring1Idx
     *            index of the first interior ring involved (starting at 0)
     * @param ring2Idx
     *            index of the second interior ring involved (starting at 0)
     * @param location
     *            (approximate) location of the intersection, may be <code>null</code>
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @return <code>true</code>, if the event should indicate that the geometry is invalid, otherwise
     *         <code>false</code>
     */
    public boolean interiorRingsIntersect( PolygonPatch patch, int ring1Idx, int ring2Idx, Point location,
                                           List<Object> affectedGeometryParticles );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has two holes (interior rings) that are nested, i.e.
     * one ring is completely inside the other.
     * 
     * @param patch
     *            offending patch, never <code>null</code>
     * @param ring1Idx
     *            index of the first interior ring involved (the 'outer' one)
     * @param ring2Idx
     *            index of the second interior ring involved (the 'inner' one)
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @return <code>true</code>, if the event should indicate that the geometry is invalid, otherwise
     *         <code>false</code>
     */
    public boolean interiorRingsWithin( PolygonPatch patch, int ring1Idx, int ring2Idx,
                                        List<Object> affectedGeometryParticles );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has a hole (interior ring) that touches it's shell
     * (exterior ring).
     * 
     * @param patch
     *            offending patch, never <code>null</code>
     * @param ringIdx
     *            index of the offending inner ring
     * @param location
     *            (approximate) location of the intersection, may be <code>null</code>
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @return <code>true</code>, if the event should indicate that the geometry is invalid, otherwise
     *         <code>false</code>
     */
    public boolean interiorRingTouchesExterior( PolygonPatch patch, int ringIdx, Point location,
                                                List<Object> affectedGeometryParticles );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has a hole (interior ring) that intersects it's shell
     * (exterior ring).
     * 
     * @param patch
     *            offending patch, never <code>null</code>
     * @param ringIdx
     *            index of the offending inner ring
     * @param location
     *            (approximate) location of the intersection, may be <code>null</code>
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @return <code>true</code>, if the event should indicate that the geometry is invalid, otherwise
     *         <code>false</code>
     */
    public boolean interiorRingIntersectsExterior( PolygonPatch patch, int ringIdx, Point location,
                                                   List<Object> affectedGeometryParticles );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has a hole (interior ring) that is completely located
     * outside it's shell (exterior ring).
     * 
     * @param patch
     *            offending patch, never <code>null</code>
     * @param ringIdx
     *            index of the offending inner ring
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @return <code>true</code>, if the event should indicate that the geometry is invalid, otherwise
     *         <code>false</code>
     */
    public boolean interiorRingOutsideExterior( PolygonPatch patch, int ringIdx, List<Object> affectedGeometryParticles );
}
