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

package org.deegree.geometry.standard;

import org.deegree.commons.utils.Pair;

import org.locationtech.jts.geom.Geometry;

/**
 * Stores a pair of JTS geometries.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class JTSGeometryPair extends Pair<Geometry, Geometry> {

    private JTSGeometryPair( Geometry first, Geometry second ) {
        super( first, second );
    }

    /**
     * Creates a "compatible" pair of JTS geometries from an {@link AbstractDefaultGeometry} and a second
     * {@link Geometry} instance.
     * 
     * @param geom1
     * @param geom2
     * @return
     */
    public static JTSGeometryPair createCompatiblePair( AbstractDefaultGeometry geom1,
                                                        org.deegree.geometry.Geometry geom2 ) {
        return new JTSGeometryPair( geom1.getJTSGeometry(),
                                    AbstractDefaultGeometry.getAsDefaultGeometry( geom2 ).getJTSGeometry() );
    }

}
