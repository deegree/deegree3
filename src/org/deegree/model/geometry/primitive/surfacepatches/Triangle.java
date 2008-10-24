//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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
package org.deegree.model.geometry.primitive.surfacepatches;

import java.util.List;

import org.deegree.model.geometry.primitive.LinearRing;
import org.deegree.model.geometry.primitive.Point;

/**
 * A {@link Triangle} is a {@link SurfacePatch} defined by three planar points.  
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface Triangle extends PolygonPatch {

    /**
     * Returns the first of the three control points.
     * 
     * @return the first control point
     */
    public Point getPoint1();

    /**
     * Returns the second of the three control points.
     * 
     * @return the second control point
     */
    public Point getPoint2();

    /**
     * Returns the last of the three control points.
     * 
     * @return the third control point
     */    
    public Point getPoint3();        
    
    /**
     * Returns the sequence of control points as a {@link LinearRing}.
     * 
     * @return the exterior ring
     */
    @Override
    public LinearRing getExteriorRing();

    @Override
    public List<LinearRing> getBoundaryRings();    
}
