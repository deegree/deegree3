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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Ring.RingType;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.standard.surfacepatches.DefaultPolygonPatch;

import org.locationtech.jts.geom.LinearRing;

/**
 * Default implementation of {@link Polygon}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultPolygon extends DefaultSurface implements Polygon {

    private Ring exteriorRing;

    private List<Ring> interiorRings;

    private Envelope envelope;

    /**
     * Creates a new {@link DefaultPolygon} instance from the given parameters.
     * 
     * @param id
     *            identifier, may be null
     * @param crs
     *            coordinate reference system, may be null
     * @param pm
     *            precision model, may be null
     * @param interiorRings
     *            list of rings that define the inner boundaries, may be empty or null
     */
    public DefaultPolygon( String id, ICRS crs, PrecisionModel pm, Ring exteriorRing, List<Ring> interiorRings ) {
        super( id, crs, pm, createPatchList( exteriorRing, interiorRings ) );
        this.exteriorRing = exteriorRing;
        this.interiorRings = interiorRings;
        if ( interiorRings == null ) {
            this.interiorRings = Collections.emptyList();
        }
    }

    private static List<PolygonPatch> createPatchList( Ring exteriorRing, List<Ring> interiorRings ) {
        List<PolygonPatch> patches = new ArrayList<PolygonPatch>( 1 );
        patches.add( new DefaultPolygonPatch( exteriorRing, interiorRings ) );
        return patches;
    }

    @Override
    public Ring getExteriorRing() {
        return exteriorRing;
    }

    @Override
    public List<Ring> getInteriorRings() {
        return interiorRings;
    }

    @Override
    public SurfaceType getSurfaceType() {
        return SurfaceType.Polygon;
    }

    @Override
    public List<PolygonPatch> getPatches() {
        return (List<PolygonPatch>) patches;
    }

    @Override
    public boolean isSFSCompliant() {
        if ( exteriorRing == null || exteriorRing.getRingType() != RingType.LinearRing ) {
            return false;
        }
        if ( interiorRings != null ) {
            for ( Ring interiorRing : interiorRings ) {
                if ( interiorRing.getRingType() != RingType.LinearRing ) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected org.locationtech.jts.geom.Geometry buildJTSGeometry() {
        LinearRing shell = (LinearRing) getAsDefaultGeometry( exteriorRing ).getJTSGeometry();
        LinearRing[] holes = null;
        if ( interiorRings != null ) {
            holes = new LinearRing[interiorRings.size()];
            int i = 0;
            for ( Ring ring : interiorRings ) {
                holes[i++] = (LinearRing) getAsDefaultGeometry( ring ).getJTSGeometry();
            }
        }
        return jtsFactory.createPolygon( shell, holes );
    }
}
