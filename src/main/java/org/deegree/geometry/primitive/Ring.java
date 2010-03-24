//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/geometry/primitive/Primitive.java $
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
package org.deegree.geometry.primitive;

import java.util.List;

/**
 * A <code>Ring</code> is a composition of {@link Curve}s that forms a closed loop.
 * <p>
 * Please note that it extends {@link Curve}, because it has an inherent curve semantic.
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
     * Must always return {@link Curve.CurveType#Ring}.
     *
     * @return {@link Curve.CurveType#Ring}
     */
    @Override
    public CurveType getCurveType();

    /**
     * Returns the type of ring.
     *
     * @return the type of ring
     */
    public RingType getRingType();

    /**
     * Returns the {@link Curve}s that constitute this {@link Ring}.
     *
     * @return the constituting curves
     */
    public List<Curve> getMembers();
}
