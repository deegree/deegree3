//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/geometry/primitive/CurveSegment.java $
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
package org.deegree.geometry.primitive.curvesegments;

import org.deegree.commons.types.Length;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;

/**
 * A {@link CurveSegment} that is defined by a base {@link Curve} and an offset.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface OffsetCurve extends CurveSegment {

    /**
     * Returns the {@link Curve} from which this curve segment is defined as using an offset.
     * 
     * @return the <code>Curve</code> used as the base geometry
     */
    public Curve getBaseCurve();

    /**
     * Returns the distance from the base curve.
     * 
     * @return the distance
     */
    public Length getDistance();

    /**
     * Returns the direction of the offset.
     * 
     * @return the direction of the offset
     */
    public Point getDirection();
}
