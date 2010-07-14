//$HeadURL$
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

package org.deegree.cs.configuration.deegree.xml;

import java.util.List;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.Ellipsoid;
import org.deegree.cs.components.GeodeticDatum;
import org.deegree.cs.components.PrimeMeridian;
import org.deegree.cs.configuration.resources.CRSResource;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.projections.Projection;
import org.deegree.cs.transformations.Transformation;

/**
 * The <code>CRSParser</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * @param <T>
 * 
 */
public interface CRSParser<T> extends CRSResource<T> {

    /**
     * @param crsDefintion
     *            to be parsed
     * @return an instance of the given crs or <code>null</code> if the crsDefinition is <code>null</code> or could not
     *         be mapped to a valid type.
     * @throws CRSConfigurationException
     *             if something went wrong while constructing the crs.
     */
    public abstract CoordinateSystem parseCoordinateSystem( T crsDefintion )
                            throws CRSConfigurationException;

    /**
     * @return the version of the root element of the empty string if no version attribute was found in the root
     *         element.
     * @throws CRSConfigurationException
     *             if the root element is empty
     */
    public abstract String getVersion()
                            throws CRSConfigurationException;

    /**
     * Retrieves a transformation from the resource.
     * 
     * @param transformationDefinition
     * @return the parsed transformation or <code>null</code> if no transformation could be parsed.
     */
    public abstract Transformation parseTransformation( T transformationDefinition );

    /**
     * @param datumID
     * @return the
     * @throws CRSConfigurationException
     */
    public abstract GeodeticDatum getGeodeticDatumForId( String datumID )
                            throws CRSConfigurationException;

    /**
     * @param meridianID
     *            the id to search for.
     * @return the primeMeridian with given id or <code>null</code>
     * @throws CRSConfigurationException
     *             if the longitude was not set or the units could not be parsed.
     */
    public abstract PrimeMeridian getPrimeMeridianForId( String meridianID )
                            throws CRSConfigurationException;

    /**
     * Tries to find a cached ellipsoid, if not found, the config will be checked.
     * 
     * @param ellipsoidID
     * @return an ellipsoid or <code>null</code> if no ellipsoid with given id was found, or the id was
     *         <code>null</code> or empty.
     * @throws CRSConfigurationException
     *             if something went wrong.
     */
    public abstract Ellipsoid getEllipsoidForId( String ellipsoidID )
                            throws CRSConfigurationException;

    /**
     * Gets the Element for the given id and heuristically check the localname of the resulting root Element. This
     * version supports following local names (see schema): <code>
     * <ul>
     * <li>ellipsoid</li>
     * <li>geodeticDatum</li>
     * <li>projectedCRS</li>
     * <li>geographicCRS</li>
     * <li>compoundCRS</li>
     * <li>geocentricCRS</li>
     * <li>primeMeridian</li>
     * <li>wgs84Transformation</li>
     * </ul>
     * </code>
     * 
     * @param id
     *            to look for.
     * @return the instantiated {@link CRSIdentifiable} or <code>null</code> if it could not be parsed.
     */
    public abstract CRSIdentifiable parseIdentifiableObject( String id );

    /**
     * 
     * @return all available codetypes, each sole array should reference the ids of one single crs.
     * @throws CRSConfigurationException
     */
    public abstract List<CRSCodeType[]> getAvailableCRSCodes()
                            throws CRSConfigurationException;

    /**
     * @param projectionId
     *            of the projection
     * @param underlyingCRS
     *            of the projection
     * @return the projection denoted by the given id, or <code>null</code> if no such projection could be loaded.
     */
    public abstract Projection getProjectionForId( String projectionId, GeographicCRS underlyingCRS );

}