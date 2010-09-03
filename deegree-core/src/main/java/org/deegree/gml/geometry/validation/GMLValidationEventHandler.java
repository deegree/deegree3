//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.gml.geometry.validation;

import java.util.List;

import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.patches.PolygonPatch;

/**
 * The <code></code> class TODO add class documentation here.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public interface GMLValidationEventHandler {

    public void geometryParsingError (GMLElementIdentifier geometryElement, Exception e);

    /**
     * Called when a sequence of two identical control points in a {@link Curve} is detected.
     *
     * @param curve
     *            invalid {@link Curve} geometry
     * @param point
     *            the duplicated point
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @param affectedElements 
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean curvePointDuplication( Curve curve, Point point, List<Object> affectedGeometryParticles, List<GMLElementIdentifier> affectedElements );

    /**
     * Called when a discontinuity in a {@link Curve} is detected, i.e. the end point of segment does not coincide with
     * the start point of the next.
     *
     * @param curve
     *            invalid {@link Curve} geometry
     * @param segmentIdx
     *            the index of the segment with the discontinuity
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @param affectedElements 
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean curveDiscontinuity( Curve curve, int segmentIdx, List<Object> affectedGeometryParticles, List<GMLElementIdentifier> affectedElements  );

    /**
     * Called when a self-intersection of a {@link Curve} is detected.
     *
     * @param curve
     *            invalid {@link Curve} geometry
     * @param location
     *            the (approximative) location of the self-intersection
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @param affectedElements 
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean curveSelfIntersection( Curve curve, Point location, List<Object> affectedGeometryParticles, List<GMLElementIdentifier> affectedElements  );

    /**
     * Called when a {@link Ring} is detected that is not closed.
     *
     * @param ring
     *            invalid {@link Ring} geometry
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @param affectedElements 
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean ringNotClosed( Ring ring, List<Object> affectedGeometryParticles, List<GMLElementIdentifier> affectedElements  );

    /**
     * Called when a self-intersection of a {@link Ring} is detected.
     *
     * @param ring
     *            invalid {@link Ring} geometry
     * @param location
     *            the (approximative) location of the self-intersection
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @param affectedElements 
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean ringSelfIntersection( Ring ring, Point location, List<Object> affectedGeometryParticles, List<GMLElementIdentifier> affectedElements  );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has an exterior ring with a wrong orientation, i.e.
     *
     * @param patch
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @param affectedElements 
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean exteriorRingCW( PolygonPatch patch, List<Object> affectedGeometryParticles, List<GMLElementIdentifier> affectedElements  );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has an interior ring with a wrong orientation, i.e.
     *
     * @param patch
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @param affectedElements 
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean interiorRingCCW( PolygonPatch patch, List<Object> affectedGeometryParticles, List<GMLElementIdentifier> affectedElements );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has two holes (interior rings) that touch.
     *
     * @param patch
     *            offending patch
     * @param ring1Idx
     *            index of the first ring involved
     * @param ring2Idx
     *            index of the second ring involved
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @param affectedElements 
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean interiorRingsTouch( PolygonPatch patch, int ring1Idx, int ring2Idx, List<Object> affectedGeometryParticles, List<GMLElementIdentifier> affectedElements );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has two holes (interior rings) that intersect.
     *
     * @param patch
     *            offending patch
     * @param ring1Idx
     *            index of the first ring involved
     * @param ring2Idx
     *            index of the second ring involved
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @param affectedElements 
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean interiorRingsIntersect( PolygonPatch patch, int ring1Idx, int ring2Idx, List<Object> affectedGeometryParticles, List<GMLElementIdentifier> affectedElements );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has two holes (interior rings) that are nested, i.e.
     * one ring is completely inside the other.
     *
     * @param patch
     *            offending patch
     * @param ring1Idx
     *            index of the first ring involved (the 'outer' one)
     * @param ring2Idx
     *            index of the second ring involved (the 'inner' one)
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @param affectedElements 
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean interiorRingsWithin( PolygonPatch patch, int ring1Idx, int ring2Idx, List<Object> affectedGeometryParticles, List<GMLElementIdentifier> affectedElements );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has a hole (interior ring) that touches it's shell
     * (exterior ring).
     *
     * @param patch
     *            offending patch
     * @param ringIdx
     *            index of the offending inner ring
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @param affectedElements 
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean interiorRingTouchesExterior( PolygonPatch patch, int ringIdx, List<Object> affectedGeometryParticles, List<GMLElementIdentifier> affectedElements );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has a hole (interior ring) that intersects it's shell
     * (exterior ring).
     *
     * @param patch
     *            offending patch
     * @param ringIdx
     *            index of the offending inner ring
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @param affectedElements 
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean interiorRingIntersectsExterior( PolygonPatch patch, int ringIdx, List<Object> affectedGeometryParticles, List<GMLElementIdentifier> affectedElements );

    /**
     * Called when a planar surface patch (={@link PolygonPatch}) has a hole (interior ring) that is completely located
     * outside it's shell (exterior ring).
     *
     * @param patch
     *            offending patch
     * @param ringIdx
     * @param affectedGeometryParticles
     *            list of affected geometry components (that the curve is a part of)
     * @param affectedElements 
     * @return true, if the event indicates that the geometry is invalid, otherwise false
     */
    public boolean interiorRingOutsideExterior( PolygonPatch patch, int ringIdx, List<Object> affectedGeometryParticles, List<GMLElementIdentifier> affectedElements );

}
