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
package org.deegree.model.crs;

import javax.vecmath.Point2d;

import org.deegree.crs.Identifiable;
import org.deegree.crs.components.Axis;
import org.deegree.crs.components.Ellipsoid;
import org.deegree.crs.components.GeodeticDatum;
import org.deegree.crs.components.Unit;
import org.deegree.crs.configuration.CRSConfiguration;
import org.deegree.crs.configuration.CRSProvider;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.crs.exceptions.CRSConfigurationException;
import org.deegree.crs.projections.cylindric.TransverseMercator;
import org.deegree.crs.transformations.Transformation;
import org.deegree.crs.transformations.helmert.Helmert;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.BootLogger;

/**
 * 
 * The <code>CRSFactory</code> class wraps the access to the CRSProvider in the org.deegree.crs package by supplying a
 * static create method, thus encapsulating the access to the CoordinateSystems.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class CRSFactory {

    private static ILogger LOG = LoggerFactory.getLogger( CRSFactory.class );

    public static CoordinateSystem EPSG_4326;
    static {
        if ( EPSG_4326 == null ) {
            try {
                EPSG_4326 = CRSFactory.create( "EPSG:4326" );
            } catch ( UnknownCRSException e ) {
                BootLogger.logError( e.getMessage(), e );
            }
        }
    }

    private synchronized static CRSProvider getProvider( String providerName ) {
        CRSConfiguration crsConfig = CRSConfiguration.getCRSConfiguration( providerName );
        return crsConfig.getProvider();
    }

    /**
     * Creates a CRS from the given name using the given provider, if no CRS was found an UnkownCRSException will be
     * thrown.
     * 
     * @param providerName
     *            to be used for the creation of the crs.
     * @param name
     *            of the crs, e.g. EPSG:31466
     * @return a CoordinateSystem corresponding to the given name
     * @throws UnknownCRSException
     *             if the crs-name is not known
     */
    public synchronized static CoordinateSystem create( String providerName, String name )
                            throws UnknownCRSException {
        CRSProvider crsProvider = getProvider( providerName );
        org.deegree.crs.coordinatesystems.CoordinateSystem realCRS = null;
        try {
            realCRS = crsProvider.getCRSByID( name );
        } catch ( CRSConfigurationException e ) {
            LOG.logError( e.getMessage(), e );
        }
        if ( realCRS == null ) {
            throw new UnknownCRSException( name );
        }
        LOG.logDebug( "Successfully created the crs with id: " + name );
        return new CoordinateSystem( realCRS, name );
    }

    /**
     * Get a {@link Transformation} with given id, or <code>null</code> if it does not exist.
     * 
     * @param providerName
     *            to use.
     * @param id
     *            of the Transformation.
     * @return the identified transformation or <code>null<code> if no such transformation is found.
     */
    public synchronized static Transformation getTransformation( String providerName, String id ) {
        CRSProvider crsProvider = getProvider( providerName );
        Identifiable t = crsProvider.getIdentifiable( id );
        if ( t instanceof Transformation ) {
            return (Transformation) t;
        }
        LOG.logDebug( "The given id: " + id + " is not of type transformation return null." );
        return null;
    }

    /**
     * Retrieve a {@link Transformation} (chain) which transforms coordinates from the given source into the given
     * target crs. If no such {@link Transformation} could be found or the implementation does not support inverse
     * lookup of transformations <code>null<code> will be returned.
     * 
     * @param providerName
     *            to use.
     * @param sourceCRS
     *            start of the transformation (chain)
     * @param targetCRS
     *            end point of the transformation (chain).
     * @return the given {@link Transformation} or <code>null<code> if no such transformation was found.
     */
    public synchronized static Transformation getTransformation( String providerName, CoordinateSystem sourceCRS,
                                                                 CoordinateSystem targetCRS ) {
        CRSProvider crsProvider = getProvider( providerName );
        return crsProvider.getTransformation( sourceCRS.getCRS(), targetCRS.getCRS() );
    }

    /**
     * Creates a CRS from the given name, if no CRS was found an UnkownCRSException will be thrown.
     * 
     * @param name
     *            of the crs, e.g. EPSG:4326
     * @return a CoordinateSystem corresponding to the given name, using the configured provider.
     * @throws UnknownCRSException
     *             if the crs-name is not known
     */
    public synchronized static CoordinateSystem create( String name )
                            throws UnknownCRSException {
        return create( null, name );
    }

    /**
     * Wrapper for the private constructor of the org.deegree.model.crs.CoordinateSystem class.
     * 
     * @param realCRS
     *            to wrap
     * 
     * @return a CoordinateSystem corresponding to the given crs.
     */
    public static CoordinateSystem create( org.deegree.crs.coordinatesystems.CoordinateSystem realCRS ) {
        return new CoordinateSystem( realCRS, realCRS.getIdentifier() );
    }

    /**
     * Wrapper for the private constructor to create a dummy projected crs with no projection parameters set, the
     * standard wgs84 datum and the given optional name as the identifier. X-Y axis are in metres.
     * 
     * @param name
     *            optional identifier, if missing, the word 'dummy' will be used.
     * 
     * @return a dummy CoordinateSystem having filled out all the essential values.
     */
    public static CoordinateSystem createDummyCRS( String name ) {
        if ( name == null || "".equals( name.trim() ) ) {
            name = "dummy";
        }
        /**
         * Standard axis of a geographic crs
         */
        final Axis[] axis_degree = new Axis[] { new Axis( Unit.DEGREE, "lon", Axis.AO_EAST ),
                                               new Axis( Unit.DEGREE, "lat", Axis.AO_NORTH ) };
        final Axis[] axis_projection = new Axis[] { new Axis( "x", Axis.AO_EAST ), new Axis( "y", Axis.AO_NORTH ) };

        final Helmert wgs_info = new Helmert( GeographicCRS.WGS84, GeographicCRS.WGS84, name + "_wgs" );
        final GeodeticDatum datum = new GeodeticDatum( Ellipsoid.WGS84, wgs_info, new String[] { name + "_datum" } );
        final GeographicCRS geographicCRS = new GeographicCRS( datum, axis_degree, new String[] { name
                                                                                                  + "geographic_crs" } );
        final TransverseMercator projection = new TransverseMercator( true, geographicCRS, 0, 0, new Point2d( 0, 0 ),
                                                                      Unit.METRE, 1 );

        return new CoordinateSystem(
                                     new ProjectedCRS( projection, axis_projection, new String[] { name
                                                                                                   + "projected_crs" } ),
                                     name );

    }
}
