/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.cs.persistence;

import java.util.List;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSResource;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.TransformationFactory.DSTransform;

/**
 * Base interface for all
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public interface CRSStore {

	/**
	 * Called by the manager to indicate that this {@link CRSStore} instance is being
	 * registered.
	 */
	public void init();

	/**
	 * This method is should retrieve a transformation which transforms coordinates from
	 * the given source into the given target crs. If no such transformation could be
	 * found or the implementation does not support inverse lookup of transformations
	 * <code>null<code> should be returned.
	 *
	 * &#64;param sourceCRS
	 *            start of the transformation (chain)
	 * &#64;param targetCRS
	 *            end point of the transformation (chain).
	 *
	 *
	@return the {@link Transformation} Object or <code>null</code> if no such Object was
	 * found.
	 * @throws CRSConfigurationException if the implementation was confronted by an
	 * exception and could not deliver the requested Object. This exception should not be
	 * thrown no Transformation was found, in this case <code>null</code> should be
	 * returned.
	 */
	public Transformation getDirectTransformation(ICRS sourceCRS, ICRS targetCRS) throws CRSConfigurationException;

	/**
	 * This method should retrieve a transformation with the given id. If a transformation
	 * with the given id could not be found <code>null<code> should be returned.
	 *
	 * &#64;param id
	 *            the id of the transformation
	 *
	 *
	@return the {@link Transformation} Object or <code>null</code> if no such Object was
	 * found.
	 * @throws CRSConfigurationException if the implementation was confronted by an
	 * exception and could not deliver the requested Object. This exception should not be
	 * thrown no Transformation was found, in this case <code>null</code> should be
	 * returned.
	 */
	public Transformation getDirectTransformation(String id) throws CRSConfigurationException;

	/**
	 * This method is more general than the {@link #getCRSByCode(CRSCodeType)}, because it
	 * represents a possibility to return an arbitrary {@link CRSResource} Object from the
	 * providers backend.
	 * @param id string representation of the resource to retrieve
	 * @return the {@link CRSResource} Object or <code>null</code> if no such Object was
	 * found.
	 * @throws CRSConfigurationException if the implementation was confronted by an
	 * exception and could not deliver the requested Object. This exception should not be
	 * thrown if the given id wasn't found, in this case <code>null</code> should be
	 * returned.
	 */
	public CRSResource getCRSResource(CRSCodeType id) throws CRSConfigurationException;

	/**
	 * @param id string representation of the CoordinateSystem
	 * @return the identified CRS or <code>null</code> if no such CRS was found.
	 * @throws CRSConfigurationException if the implementation was confronted by an
	 * exception and could not deliver the requested crs. This exception should not be
	 * thrown if the given id wasn't found, in this case <code>null</code> should be
	 * returned.
	 */
	public ICRS getCRSByCode(CRSCodeType id) throws CRSConfigurationException;

	/**
	 * @param id string representation of the CoordinateSystem
	 * @param forceXY true if the axis order of the coordinate system should be x/y
	 * (EAST/NORTH; WEST/SOUTH); false if the defined axis order should be used
	 * @return the identified CRS or <code>null</code> if no such CRS was found.
	 * @throws CRSConfigurationException if the implementation was confronted by an
	 * exception and could not deliver the requested crs. This exception should not be
	 * thrown if the given id wasn't found, in this case <code>null</code> should be
	 * returned.
	 */
	public ICRS getCRSByCode(CRSCodeType id, boolean forceXY) throws CRSConfigurationException;

	/**
	 * This method should be called to see if the provider is able to create all defined
	 * crs's, thus verifying the correctness of the configuration.
	 * @return all configured CRSs.
	 * @throws CRSConfigurationException if the implementation was confronted by an
	 * exception and could not deliver the requested crs. This exception should not be
	 * thrown if no CoordinateSystems were found, in the latter case an empty List ( a
	 * list with size == 0 ) should be returned.
	 */
	public List<ICRS> getAvailableCRSs() throws CRSConfigurationException;

	/**
	 * This method should be called if one is only interested in the available identifiers
	 * and not in the coordinatesystems themselves.
	 * @return the identifiers of all configured CRSs.
	 * @throws CRSConfigurationException if the implementation was confronted by an
	 * exception and could not deliver the requested crs. This exception should not be
	 * thrown if no CoordinateSystems were found, in the latter case an empty List ( a
	 * list with size == 0 ) should be returned.
	 */
	public List<CRSCodeType[]> getAvailableCRSCodes() throws CRSConfigurationException;

	/**
	 * @return the prefered transformation type for this {@link CRSStore}
	 */
	public DSTransform getPreferedTransformationType();

}
