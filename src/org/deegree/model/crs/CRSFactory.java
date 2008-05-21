//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

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
package org.deegree.model.crs;

import javax.vecmath.Point2d;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.model.crs.components.Axis;
import org.deegree.model.crs.components.Ellipsoid;
import org.deegree.model.crs.components.GeodeticDatum;
import org.deegree.model.crs.components.Unit;
import org.deegree.model.crs.configuration.CRSConfiguration;
import org.deegree.model.crs.configuration.CRSProvider;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.crs.coordinatesystems.GeographicCRS;
import org.deegree.model.crs.coordinatesystems.ProjectedCRS;
import org.deegree.model.crs.exceptions.CRSConfigurationException;
import org.deegree.model.crs.exceptions.UnknownCRSException;
import org.deegree.model.crs.projections.cylindric.TransverseMercator;
import org.deegree.model.crs.transformations.helmert.WGS84ConversionInfo;

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

    private static Log LOG = LogFactory.getLog( CRSFactory.class );

    /**
     * e.g. epgs:4326
     * 
     * @param name
     * @return a CoordinateSystem corresponding to the given name never <code>null</code>
     * @throws UnknownCRSException
     *             if the crs-name is not known
     */
    public static CoordinateSystem create( String name )
                            throws UnknownCRSException {
        CRSConfiguration crsConfig = CRSConfiguration.getCRSConfiguration();
        CRSProvider crsProvider = crsConfig.getProvider();
        CoordinateSystem realCRS = null;
        try {
            realCRS = crsProvider.getCRSByID( name );
        } catch ( CRSConfigurationException e ) {
            LOG.error( e.getMessage(), e );
        }
        if ( realCRS == null ) {
            throw new UnknownCRSException( name );
        }
        LOG.debug( "Successfully created the crs with id: " + name );
        return realCRS;
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

        final WGS84ConversionInfo wgs_info = new WGS84ConversionInfo( name + "_wgs" );
        final GeodeticDatum datum = new GeodeticDatum( Ellipsoid.WGS84, wgs_info, new String[] { name + "_datum" } );
        final GeographicCRS geographicCRS = new GeographicCRS( datum, axis_degree, new String[] { name
                                                                                                  + "geographic_crs" } );
        final TransverseMercator projection = new TransverseMercator( true, geographicCRS, 0, 0, new Point2d( 0, 0 ),
                                                                      Unit.METRE, 1 );

        return new ProjectedCRS( projection, axis_projection, new String[] { name + "projected_crs" } );

    }
}
