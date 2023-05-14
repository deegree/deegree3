/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.feature.persistence.sql;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;

/**
 * Encapsulates the storage parameters for geometries.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class GeometryStorageParams {

	private final ICRS crs;

	private final String srid;

	private final CoordinateDimension dim;

	/**
	 * Creates a new {@link GeometryStorageParams} instance.
	 * @param crs coordinate reference system used for stored geometries or
	 * <code>null</code> (unspecified)
	 * @param srid spatial reference identifier (database code), must not be
	 * <code>null</code>
	 * @param dim dimensionality of coordinates, must not be <code>null</code>
	 */
	public GeometryStorageParams(ICRS crs, String srid, CoordinateDimension dim) {
		this.crs = crs;
		this.srid = srid;
		this.dim = dim;
	}

	/**
	 * Returns the coordinate reference system for stored geometries.
	 * @return coordinate reference system, can be <code>null</code> (unspecified)
	 */
	public ICRS getCrs() {
		return crs;
	}

	/**
	 * Returns the spatial reference identifier (database code).
	 * @return spatial reference identifier, never <code>null</code>
	 */
	public String getSrid() {
		return srid;
	}

	/**
	 * Returns the coordinate dimension.
	 * @return coordinate dimension, never <code>null</code>
	 */
	public CoordinateDimension getDim() {
		return dim;
	}

}