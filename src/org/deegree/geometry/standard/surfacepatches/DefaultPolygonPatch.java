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
package org.deegree.geometry.standard.surfacepatches;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.patches.PolygonPatch;

/**
 * Default implementation of {@link PolygonPatch}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultPolygonPatch implements PolygonPatch {

    private Ring exteriorRing;

    private List<Ring> interiorRings;

    private List<Ring> allBoundaries;

    /**
     * Creates a new {@link DefaultPolygonPatch} instance from the given parameters.
     * 
     * @param exteriorRing
     *            ring that defines the outer boundary, may be null (see section 9.2.2.5 of GML spec)
     * @param interiorRings
     *            list of rings that define the inner boundaries, may be empty or null
     */
    public DefaultPolygonPatch( Ring exteriorRing, List<Ring> interiorRings ) {
        this.exteriorRing = exteriorRing;
        this.interiorRings = interiorRings;
        if ( interiorRings == null ) {
            this.interiorRings = Collections.emptyList();
        }
        this.allBoundaries = new LinkedList<Ring>();
        if ( exteriorRing != null ) {
            allBoundaries.add( exteriorRing );
        }
        if ( interiorRings != null ) {
            allBoundaries.addAll( interiorRings );
        }
    }

    @Override
    public int getCoordinateDimension() {
        return exteriorRing.getCoordinateDimension();
    }

    @Override
    public Measure getArea( Unit requestedBaseUnit ) {
        // TODO
        throw new UnsupportedOperationException();
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
    public List<Ring> getBoundaryRings() {
        return allBoundaries;
    }

    @Override
    public SurfacePatchType getSurfacePatchType() {
        return SurfacePatchType.POLYGON_PATCH;
    }

    @Override
    public PolygonPatchType getPolygonPatchType() {
        return PolygonPatchType.POLYGON_PATCH;
    }
}
