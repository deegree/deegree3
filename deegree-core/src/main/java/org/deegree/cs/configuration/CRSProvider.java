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

package org.deegree.cs.configuration;

import java.util.List;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.transformations.Transformation;

/**
 * The <code>CRSProvider</code> will allow the support for different crs-definitions formats within the crs package. All
 * implementation should consider the fact that the deegree-crs package will assume all incoming and outgoing
 * latitude/longitude coordinates in <u>radians</u>. Dependent on the forceXY flag it is possible to set the axis order
 * to x/y (EAST/NORTH; WEST/SOUTH). If this flag is true and the defined axis order is not x/y, the axis order will be
 * swapped. By default the defined axis order will be used.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public interface CRSProvider {

    /**
     * This method is should retrieve a transformation (chain) which transforms coordinates from the given source into
     * the given target crs. If no such transformation could be found or the implementation does not support inverse
     * lookup of transformations <code>null<code> should be returned.
     * 
     * @param sourceCRS
     *            start of the transformation (chain)
     * @param targetCRS
     *            end point of the transformation (chain).
     * 
     * @return the {@link Transformation} Object or <code>null</code> if no such Object was found.
     * @throws CRSConfigurationException
     *             if the implementation was confronted by an exception and could not deliver the requested Object. This
     *             exception should not be thrown no Transformation was found, in this case <code>null</code> should be
     *             returned.
     */
    public Transformation getTransformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS )
                            throws CRSConfigurationException;

    /**
     * This method is more general than the {@link #getCRSByCode(CRSCodeType)}, because it represents a possibility to
     * return an arbitrary {@link CRSIdentifiable} Object from the providers backend.
     * 
     * 
     * @param id
     *            string representation of the resource to retrieve
     * @return the {@link CRSIdentifiable} Object or <code>null</code> if no such Object was found.
     * @throws CRSConfigurationException
     *             if the implementation was confronted by an exception and could not deliver the requested Object. This
     *             exception should not be thrown if the given id wasn't found, in this case <code>null</code> should be
     *             returned.
     */
    public CRSIdentifiable getIdentifiable( CRSCodeType id )
                            throws CRSConfigurationException;

    /**
     * @param id
     *            string representation of the CoordinateSystem
     * @return the identified CRS or <code>null</code> if no such CRS was found.
     * @throws CRSConfigurationException
     *             if the implementation was confronted by an exception and could not deliver the requested crs. This
     *             exception should not be thrown if the given id wasn't found, in this case <code>null</code> should be
     *             returned.
     */
    public CoordinateSystem getCRSByCode( CRSCodeType id )
                            throws CRSConfigurationException;

    /**
     * @param id
     *            string representation of the CoordinateSystem
     * @param forceXY
     *            true if the axis order of the coordinate system should be x/y (EAST/NORTH; WEST/SOUTH); false if the
     *            defined axis order should be used
     * @return the identified CRS or <code>null</code> if no such CRS was found.
     * @throws CRSConfigurationException
     *             if the implementation was confronted by an exception and could not deliver the requested crs. This
     *             exception should not be thrown if the given id wasn't found, in this case <code>null</code> should be
     *             returned.
     */
    public CoordinateSystem getCRSByCode( CRSCodeType id, boolean forceXY )
                            throws CRSConfigurationException;

    /**
     * This method should be called to see if the provider is able to create all defined crs's, thus verifying the
     * correctness of the configuration.
     * 
     * @return all configured CRSs.
     * @throws CRSConfigurationException
     *             if the implementation was confronted by an exception and could not deliver the requested crs. This
     *             exception should not be thrown if no CoordinateSystems were found, in the latter case an empty List (
     *             a list with size == 0 ) should be returned.
     */
    public List<CoordinateSystem> getAvailableCRSs()
                            throws CRSConfigurationException;

    /**
     * This method should be called if one is only interested in the available identifiers and not in the
     * coordinatesystems themselves.
     * 
     * @return the identifiers of all configured CRSs.
     * @throws CRSConfigurationException
     *             if the implementation was confronted by an exception and could not deliver the requested crs. This
     *             exception should not be thrown if no CoordinateSystems were found, in the latter case an empty List (
     *             a list with size == 0 ) should be returned.
     */
    public List<CRSCodeType[]> getAvailableCRSCodes()
                            throws CRSConfigurationException;

    /**
     * Exports the crs to the implemented format. Try calling {@link #canExport()} before executing this method.
     * 
     * @param sb
     *            the StringBuilder which will contain the exported version of the given crs.
     * @param crsToExport
     *            the CoordinateSystems to export.
     * @see #canExport()
     */
    public void export( StringBuilder sb, List<CoordinateSystem> crsToExport );

    /**
     * @return true if this provider can export a given crs.
     */
    public boolean canExport();

}
