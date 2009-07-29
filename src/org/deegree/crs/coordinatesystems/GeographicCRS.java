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

package org.deegree.crs.coordinatesystems;

import java.util.List;

import org.deegree.crs.CRSCodeType;
import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.EPSGCode;
import org.deegree.crs.components.Axis;
import org.deegree.crs.components.GeodeticDatum;
import org.deegree.crs.components.Unit;
import org.deegree.crs.i18n.Messages;
import org.deegree.crs.transformations.polynomial.PolynomialTransformation;

/**
 * The <code>GeographicCoordinateSystem</code> (in epsg aka Geodetic CRS) is a two dimensional crs with axis of
 * lat-lon.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public class GeographicCRS extends CoordinateSystem {

    /**
     * A geographic coordinate system using WGS84 datum. This coordinate system use <var>longitude</var>/<var>latitude</var>
     * axis with latitude values increasing north and longitude values increasing east. Angular units are degrees and
     * prime meridian is Greenwich.
     */
    public static final GeographicCRS WGS84 = new GeographicCRS(
                                                                 GeodeticDatum.WGS84,
                                                                 new Axis[] {
                                                                             new Axis( Unit.DEGREE, "lon", Axis.AO_EAST ),
                                                                             new Axis( Unit.DEGREE, "lat",
                                                                                       Axis.AO_NORTH ) },
                                                                 new EPSGCode( 4326 ), "WGS 84" );

    /**
     * A geographic coordinate system using WGS84 datum. This coordinate system use <var>longitude</var>/<var>latitude</var>
     * axis with latitude values increasing north and longitude values increasing east. Angular units are degrees and
     * prime meridian is Greenwich.
     */
    public static final GeographicCRS WGS84_YX = new GeographicCRS( GeodeticDatum.WGS84,
                                                                    new Axis[] {
                                                                                new Axis( Unit.DEGREE, "lat",
                                                                                          Axis.AO_NORTH ),
                                                                                new Axis( Unit.DEGREE, "lon",
                                                                                          Axis.AO_EAST ) },
                                                                    new EPSGCode( 4326 ), "WGS 84" );

    /**
     * @param datum
     * @param axisOrder
     * @param identity
     * @throws IllegalArgumentException
     *             if the axisOrder.length != 2.
     */
    public GeographicCRS( GeodeticDatum datum, Axis[] axisOrder, CRSIdentifiable identity )
                            throws IllegalArgumentException {
        this( null, datum, axisOrder, identity );
    }

    /**
     * @param datum
     * @param axisOrder
     * @param codes
     * @param names
     * @param versions
     * @param descriptions
     * @param areasOfUse
     * @throws IllegalArgumentException
     *             if the axisOrder.length != 2.
     */
    public GeographicCRS( GeodeticDatum datum, Axis[] axisOrder, CRSCodeType[] codes, String[] names,
                          String[] versions, String[] descriptions, String[] areasOfUse )
                            throws IllegalArgumentException {
        super( datum, axisOrder, codes, names, versions, descriptions, areasOfUse );
        if ( axisOrder.length != 2 ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_COORDINATESYSTEMS_WRONG_AXIS_DIM",
                                                                     "Geographic", "2" ) );
        }
    }

    /**
     * @param datum
     * @param axisOrder
     * @param codes
     */
    public GeographicCRS( GeodeticDatum datum, Axis[] axisOrder, CRSCodeType[] codes ) {
        this( datum, axisOrder, codes, null, null, null, null );
    }

    /**
     * @param datum
     * @param axisOrder
     * @param code
     * @param name
     * @param version
     * @param description
     * @param areaOfUse
     */
    public GeographicCRS( GeodeticDatum datum, Axis[] axisOrder, CRSCodeType code, String name, String version,
                          String description, String areaOfUse ) {
        this( datum, axisOrder, new CRSCodeType[] { code }, new String[] { name }, new String[] { version },
              new String[] { description }, new String[] { areaOfUse } );
    }

    /**
     * @param datum
     * @param axisOrder
     * @param code
     * @param name
     */
    public GeographicCRS( GeodeticDatum datum, Axis[] axisOrder, CRSCodeType code, String name ) {
        this( datum, axisOrder, new CRSCodeType[] { code }, new String[] { name }, null, null, null );
    }

    /**
     * @param transformations
     * @param usedDatum
     * @param axisOrder
     * @param id
     */
    public GeographicCRS( List<PolynomialTransformation> transformations, GeodeticDatum usedDatum, Axis[] axisOrder,
                          CRSIdentifiable id ) {
        super( transformations, usedDatum, axisOrder, id );
        if ( axisOrder.length != 2 ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_COORDINATESYSTEMS_WRONG_AXIS_DIM",
                                                                     "Geographic", "2" ) );
        }
    }

    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public int getType() {
        return GEOGRAPHIC_CRS;
    }

}
