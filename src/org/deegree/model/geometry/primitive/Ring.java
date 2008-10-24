//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/geometry/primitive/Primitive.java $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
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
package org.deegree.model.geometry.primitive;

import java.util.List;

/**
 * A <code>Ring</code> is a composition of {@link Curve}s that forms a closed loop.
 * <p>
 * Please note that it extends {@link Curve} as well, because it has an inherent curve semantic.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface Ring extends Curve {

    /**
     * All ring variants.
     */
    public enum RingType {

        /** Just one curve member (with a single segment) with linear interpolation. **/
        LinearRing,

        /** Generic ring: arbitrary number of members with arbitrary interpolation methods. **/
        Ring
    }

    /**
     * Returns the type of ring.
     * 
     * @return the type of ring
     */
    public RingType getRingType();

    /**
     * Must always return {@link GeometricPrimitive.PrimitiveType#Curve}.
     * 
     * @return {@link GeometricPrimitive.PrimitiveType#Curve}
     */
    @Override
    public PrimitiveType getPrimitiveType();    
    
    /**
     * Returns the {@link Curve}s that constitute this {@link Ring}.
     * 
     * @return the constituting curves
     */
    public List<Curve> getMembers ();
}
