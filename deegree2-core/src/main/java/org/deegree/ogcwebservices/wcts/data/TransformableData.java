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

package org.deegree.ogcwebservices.wcts.data;

import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.crs.transformations.Transformation;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcwebservices.OGCWebServiceException;

/**
 * <code>TransformableData</code> defines the interface for different kinds of Data, which can be transformed.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 * @param <T>
 *            the type of data.
 *
 */
public abstract class TransformableData<T> {

    /**
     * This function should implement the transforming of the underlying data.
     *
     * @param sourceCRS
     *            of the incoming points
     * @param targetCRS
     *            of the outgoing points
     *
     * @param enableLogging
     *            if true the implementing class should log all transformations.
     * @throws OGCWebServiceException
     *             if an exception occurs while transforming.
     */
    public abstract void doTransform( CoordinateSystem sourceCRS, CoordinateSystem targetCRS, boolean enableLogging )
                            throws OGCWebServiceException;

    /**
     * This function should implement the transforming of the underlying data.
     *
     * @param transformation
     *            to use for the transform.
     *
     * @param enableLogging
     *            if true the implementing class should log all transformations.
     * @throws OGCWebServiceException
     *             if an exception occurs while transforming.
     */
    public abstract void doTransform( Transformation transformation, boolean enableLogging )
                            throws OGCWebServiceException;

    /**
     * @param targetCRS
     *            to get the {@link GeoTransformer} for.
     * @return a new GeoTransformer instance initialized with the targetCRS.
     */
    public final GeoTransformer getGeotransformer( CoordinateSystem targetCRS ) {
        return new GeoTransformer( targetCRS );
    }

    /**
     * @param transformation
     *            to get the {@link GeoTransformer} for.
     * @return a new GeoTransformer instance initialized with the targetCRS.
     */
    public final GeoTransformer getGeotransformer( Transformation transformation ) {
        return new GeoTransformer( transformation );
    }

    /**
     * @return the transformed data as a list of the overriding class implementation, for example {@link Geometry}
     *         (GeometyData), or {@link Point3d} (SimpleData).
     */
    public abstract List<T> getTransformedData();

}
