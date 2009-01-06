//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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
package org.deegree.model.geometry.standard.surfacepatches;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.deegree.model.geometry.primitive.Ring;
import org.deegree.model.geometry.primitive.surfacepatches.PolygonPatch;

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
    public DefaultPolygonPatch ( Ring exteriorRing, List<Ring> interiorRings) {
        this.exteriorRing = exteriorRing;
        this.interiorRings = interiorRings;
        if (interiorRings == null) {
            this.interiorRings = Collections.emptyList();
        }
        this.allBoundaries = new LinkedList<Ring>();
        if (exteriorRing != null) {
            allBoundaries.add( exteriorRing );
        }
        if (interiorRings != null) {
            allBoundaries.addAll( interiorRings );
        }
    }

    @Override
    public int getCoordinateDimension() {
        throw new UnsupportedOperationException();
    }    
    
    @Override
    public double getArea() {
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
}
