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

package org.deegree.cs.configuration.resources;

import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.helmert.Helmert;

/**
 * The <code>GMLResource</code> class defines the access to a resource containing GML crs definitions, often a
 * dictionary file or a database.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 * @param <T>
 *            of the resource
 *
 */
public interface CRSResource<T> extends XLinkResolver<T> {

    /**
     * Implementations should find the helmert transformation which transforms coordinates of the geodetic datum into
     * coordinates of the WGS84 datum. If no such transformation could be found, the implementation should return
     * <code>null</code>
     *
     * @param sourceCRS
     *            to retrieve the transformation for.
     * @return the {@link Helmert} transformation or <code>null</code> if no such transformation was defined.
     */
    public abstract Helmert getWGS84Transformation( GeographicCRS sourceCRS );

    /**
     * Implementations should find a given transformation (chain) which transforms coordinates of the given coordinate
     * system into coordinates of the target crs. If no such transformation could be found, the implementation should
     * return <code>null</code>
     *
     * @param sourceCRS
     *            start point of the transformation.
     * @param targetCRS
     *            end point of the transformations
     * @return the {@link Transformation} or <code>null</code> if no such transformation was defined.
     */
    public abstract Transformation getTransformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS );

}
