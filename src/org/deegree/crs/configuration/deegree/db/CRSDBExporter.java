//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.crs.configuration.deegree.db;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point2d;

import org.deegree.crs.CRSCodeType;
import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.CRSRegistry;
import org.deegree.crs.components.Axis;
import org.deegree.crs.components.Ellipsoid;
import org.deegree.crs.components.GeodeticDatum;
import org.deegree.crs.components.PrimeMeridian;
import org.deegree.crs.components.VerticalDatum;
import org.deegree.crs.configuration.CRSConfiguration;
import org.deegree.crs.coordinatesystems.CompoundCRS;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.coordinatesystems.GeocentricCRS;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.crs.coordinatesystems.VerticalCRS;
import org.deegree.crs.exceptions.CRSException;
import org.deegree.crs.exceptions.CRSExportingException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.crs.projections.Projection;
import org.deegree.crs.projections.azimuthal.LambertAzimuthalEqualArea;
import org.deegree.crs.projections.azimuthal.StereographicAlternative;
import org.deegree.crs.projections.azimuthal.StereographicAzimuthal;
import org.deegree.crs.projections.conic.LambertConformalConic;
import org.deegree.crs.projections.cylindric.Mercator;
import org.deegree.crs.projections.cylindric.TransverseMercator;
import org.deegree.crs.transformations.helmert.Helmert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>CRSDBExporter</code> class inserts a CRS object in the database. In order not to introduce an object
 * twice, the codetype( sometimes - when the code is not provided - the objects's data) is checked not to exist already
 * in the database.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author: aionita $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class CRSDBExporter {

    private Logger LOG = LoggerFactory.getLogger( CRSDBExporter.class );

    /**
     * For now, the internal database ID is an incrementing variable.
     */
    private int internalID = 1;

    private Connection connection = null;

    /**
     * CRS database exporter
     */
    public CRSDBExporter() {
        // nothing necessary yet
    }

    /**
     * Set the database connection (that is usually obtained from {@link DatabaseCRSProvider})
     * 
     * @param connection
     *            the database connection
     */
    protected void setConnection( Connection connection ) {
        this.connection = connection;
    }

    /**
     * Insert into the database the core Identifiable properties of an object
     * 
     * @param crsObject
     *            any CRS object
     */
    protected void exportIdentifiableProperties( CRSIdentifiable crsObject ) {
        PreparedStatement preparedSt;

        try {
            // insert into the CODE table
            CRSCodeType[] codes = crsObject.getCodes();
            int nCodes = codes.length;
            for ( int i = 0; i < nCodes; i++ ) {
                preparedSt = connection.prepareStatement( "INSERT INTO code VALUES ( ?, ?, ?, ?, ? )" );
                preparedSt.setInt( 1, internalID );
                preparedSt.setString( 2, codes[i].getCode() );
                preparedSt.setString( 3, codes[i].getCodeSpace() );
                preparedSt.setString( 4, codes[i].getCodeVersion() );
                preparedSt.setString( 5, codes[i].getOriginal() );
                preparedSt.execute();
            }

            // insert into the VERSION table
            String[] versions = crsObject.getVersions();
            if ( versions != null ) {
                for ( String ver : versions ) {
                    if ( ver != null ) {
                        preparedSt = connection.prepareStatement( "INSERT INTO version VALUES (?, ?)" );
                        preparedSt.setInt( 1, internalID );
                        preparedSt.setString( 2, ver );
                        preparedSt.execute();
                    }
                }
            }

            // insert into the NAME table
            String[] names = crsObject.getNames();
            if ( names != null ) {
                for ( String n : names ) {
                    if ( n != null ) {
                        preparedSt = connection.prepareStatement( "INSERT INTO name VALUES ( ?, ?)" );
                        preparedSt.setInt( 1, internalID );
                        preparedSt.setString( 2, n );
                        preparedSt.execute();
                    }
                }
            }

            // insert into the DESCRIPTION table
            String[] descriptions = crsObject.getDescriptions();
            if ( descriptions != null ) {
                for ( String d : descriptions ) {
                    if ( d != null ) {
                        preparedSt = connection.prepareStatement( "INSERT INTO description VALUES ( ?, ?)" );
                        preparedSt.setInt( 1, internalID );
                        preparedSt.setString( 2, d );
                        preparedSt.execute();
                    }
                }
            }

            // insert into the AREA table
            String[] areas = crsObject.getAreasOfUse();
            if ( areas != null ) {
                for ( String a : areas ) {
                    if ( a != null ) {
                        preparedSt = connection.prepareStatement( "INSERT INTO area VALUES ( ?, ?)" );
                        preparedSt.setInt( 1, internalID );
                        preparedSt.setString( 2, a );
                        preparedSt.execute();
                    }
                }
            }
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e );
        }
    }

    /**
     * Insert into the database the Lambert Azimuthal Equal Area projection properties
     * 
     * @param lambertAzimuthal
     *            the Lambert Azimuthal Equal Area projection object
     * @return the internal database ID assigned to the supplied object
     * @throws SQLException
     *             when an SQLException occurs
     */
    protected int export( LambertAzimuthalEqualArea lambertAzimuthal )
                            throws SQLException {
        LOG.info( "Exporting Lambert Azimuthal Equal Area projection..." );

        int geographicID = export( lambertAzimuthal.getGeographicCRS() );

        try {
            exportIdentifiableProperties( lambertAzimuthal );

            // insert into projection_lookup table
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO projection_lookup VALUES (?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "lambert_azimuthal_equal_area" );
            ps.execute();

            // insert into lambert_azimuthal_equal_area table
            ps = connection.prepareStatement( "INSERT INTO lambert_azimuthal_equal_area VALUES (?, ?, ?, ?, ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );

            Point2d p2d = lambertAzimuthal.getNaturalOrigin();
            setNullDoubleIf( p2d.y, 2, ps ); // y for latitude (of natural origin)
            setNullDoubleIf( p2d.x, 3, ps ); // x for longitude (of natural origin)
            setNullDoubleIf( lambertAzimuthal.getScale(), 4, ps );
            setNullDoubleIf( lambertAzimuthal.getFalseEasting(), 5, ps );
            setNullDoubleIf( lambertAzimuthal.getFalseNorthing(), 6, ps );

            ps.setInt( 7, geographicID );
            ps.setString( 8, lambertAzimuthal.getUnits().getName() );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e );
        }

        return internalID++;
    }

    /**
     * Inserts into the database the Stereographic Alternative projection properties
     * 
     * @param stereographicAl
     *            the Stereographic Alternative projection object
     * @return the internal database ID assigned to the object
     * @throws SQLException
     *             when an SQLException occurs
     */
    protected int export( StereographicAlternative stereographicAl )
                            throws SQLException {

        int geographicID = export( stereographicAl.getGeographicCRS() );

        try {
            exportIdentifiableProperties( stereographicAl );

            // insert into projection_lookup table
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO projection_lookup VALUES (?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "stereographic_alternative" );
            ps.execute();

            // insert into stereographic_alternative table
            ps = connection.prepareStatement( "INSERT INTO stereographic_alternative VALUES (?, ?, ?, ?, ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );

            Point2d p2d = stereographicAl.getNaturalOrigin();
            setNullDoubleIf( p2d.y, 2, ps ); // y for latitude (of natural origin)
            setNullDoubleIf( p2d.x, 3, ps ); // x for longitude (of natural origin)
            setNullDoubleIf( stereographicAl.getScale(), 4, ps );
            setNullDoubleIf( stereographicAl.getFalseEasting(), 5, ps );
            setNullDoubleIf( stereographicAl.getFalseNorthing(), 6, ps );

            ps.setInt( 7, geographicID );
            ps.setString( 8, stereographicAl.getUnits().getName() );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e );
        }

        return internalID++;
    }

    /**
     * Insert into the database the Stereographic Azimuthal projection properties
     * 
     * @param stereographicAz
     *            the Stereographic Azimuthal projection object
     * @return the internal database ID assigned to the object
     * @throws SQLException
     *             when an SQLException occurs
     */
    protected int export( StereographicAzimuthal stereographicAz )
                            throws SQLException {

        int geographicID = export( stereographicAz.getGeographicCRS() );

        try {
            exportIdentifiableProperties( stereographicAz );

            // insert into projection_lookup table
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO projection_lookup VALUES (?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "stereographic_azimuthal" );
            ps.execute();

            // insert into the stereographic_azimuthal table
            ps = connection.prepareStatement( "INSERT INTO stereographic_azimuthal VALUES (?, ?, ?, ?, ?, ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );

            Point2d p2d = stereographicAz.getNaturalOrigin();
            setNullDoubleIf( p2d.y, 2, ps ); // y for latitude (of natural origin)
            setNullDoubleIf( p2d.x, 3, ps ); // x for longitude (of natural origin)
            setNullDoubleIf( stereographicAz.getScale(), 4, ps );
            setNullDoubleIf( stereographicAz.getFalseEasting(), 5, ps );
            setNullDoubleIf( stereographicAz.getFalseNorthing(), 6, ps );
            setNullDoubleIf( stereographicAz.getTrueScaleLatitude(), 7, ps );

            ps.setInt( 8, geographicID );
            ps.setString( 9, stereographicAz.getUnits().getName() );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e );
        }

        return internalID++;
    }

    /**
     * Insert into the database the Lambert Conformal Conic projection properties
     * 
     * @param lambertConformal
     *            the Lambert Conformal projection object
     * @return the internal database ID assigned to the object
     * @throws SQLException
     *             when an SQLException occurs
     */
    protected int export( LambertConformalConic lambertConformal )
                            throws SQLException {
        LOG.info( "Exporting Lambert Conformal projection... " );

        int geographicID = export( lambertConformal.getGeographicCRS() );

        try {
            exportIdentifiableProperties( lambertConformal );

            // insert into projection_lookup table
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO projection_lookup VALUES (?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "lambert_conformal_conic" );
            ps.execute();

            // insert into lambert_conformal_conic table
            ps = connection.prepareStatement( "INSERT INTO lambert_conformal_conic VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );

            Point2d p2d = lambertConformal.getNaturalOrigin();
            setNullDoubleIf( p2d.y, 2, ps ); // y for latitude (of natural origin)
            setNullDoubleIf( p2d.x, 3, ps ); // x for longitude (of natural origin)
            setNullDoubleIf( lambertConformal.getScale(), 4, ps );
            setNullDoubleIf( lambertConformal.getFalseEasting(), 5, ps );
            setNullDoubleIf( lambertConformal.getFalseNorthing(), 6, ps );
            setNullDoubleIf( lambertConformal.getFirstParallelLatitude(), 7, ps );
            setNullDoubleIf( lambertConformal.getSecondParallelLatitude(), 8, ps );

            ps.setString( 9, lambertConformal.getUnits().getName() );
            ps.setInt( 10, geographicID );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e );
        }

        return internalID++;
    }

    /**
     * Checks if the double variable to be inserted in the database is NULL, and if so, sets the database value to NULL.
     * Otherwise simply fill the insert statement with the double variable.
     * 
     * @param d
     *            the double variable from the insert statement
     * @param pos
     *            the position of the variable in the PreparedStatement from the java.sql
     * @param preparedSt
     *            the PreparedStatement
     */
    protected void setNullDoubleIf( double d, int pos, PreparedStatement preparedSt ) {
        try {
            if ( Double.isNaN( d ) )
                preparedSt.setNull( pos, java.sql.Types.DOUBLE );
            else
                preparedSt.setDouble( pos, d );
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e );
        }
    }

    /**
     * Inserts the Transverse Mercator projection into the database
     * 
     * @param transMercator
     *            the Transform Mercator projection object
     * @return the internal database ID assigned to the object
     * @throws SQLException
     *             when an SQLException occurs
     */
    protected int export( TransverseMercator transMercator )
                            throws SQLException {

        int geographicID = export( transMercator.getGeographicCRS() );

        try {
            exportIdentifiableProperties( transMercator );

            // insert into projection_lookup table
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO projection_lookup VALUES (?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "transverse_mercator" );
            ps.execute();

            // insert into transverse_mercator
            ps = connection.prepareStatement( "INSERT INTO transverse_mercator VALUES (?, ?, ?, ?, ?, ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );

            Point2d p2d = transMercator.getNaturalOrigin();
            setNullDoubleIf( p2d.y, 2, ps ); // y for latitude (of natural origin)
            setNullDoubleIf( p2d.x, 3, ps ); // x for longitude (of natural origin)
            setNullDoubleIf( transMercator.getScale(), 4, ps );
            setNullDoubleIf( transMercator.getFalseEasting(), 5, ps );
            setNullDoubleIf( transMercator.getFalseNorthing(), 6, ps );

            ps.setShort( 7, (short) ( transMercator.getHemisphere() ? 1 : 0 ) );
            ps.setInt( 8, geographicID );
            ps.setString( 9, transMercator.getUnits().getName() );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e );
        }

        return internalID++;
    }

    /**
     * Inserts the Mercator projection into the database
     * 
     * @param mercator
     *            the Mercator projection object
     * @return the internal database ID assigned to the object
     * @throws SQLException
     *             when an SQLException occurs
     */
    protected int export( Mercator mercator )
                            throws SQLException {

        int geographicID = export( mercator.getGeographicCRS() );

        try {
            exportIdentifiableProperties( mercator );

            // insert into projection_lookup table
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO projection_lookup VALUES (?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "mercator" );
            ps.execute();

            // insert into mercator
            ps = connection.prepareStatement( "INSERT INTO mercator VALUES (?, ?, ?, ?, ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );

            Point2d p2d = mercator.getNaturalOrigin();
            setNullDoubleIf( p2d.y, 2, ps ); // y for latitude (of natural origin)
            setNullDoubleIf( p2d.x, 3, ps ); // x for longitude (of natural origin)
            setNullDoubleIf( mercator.getScale(), 4, ps );
            setNullDoubleIf( mercator.getFalseEasting(), 5, ps );
            setNullDoubleIf( mercator.getFalseNorthing(), 6, ps );

            ps.setInt( 7, geographicID );
            ps.setString( 8, mercator.getUnits().getName() );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e );
        }

        return internalID++;
    }

    /**
     * Inserts the Axis properties into the database
     * 
     * @param axis
     *            the Axis object
     * @return the internal database ID assigned to the Axis object
     * @throws SQLException
     *             when an SQLException occurs
     */
    protected int export( Axis axis )
                            throws SQLException {
        LOG.info( "Exporting Axis..." );

        String statement = "SELECT id FROM axis " + "WHERE upper( name )  =  upper( '" + axis.getName()
                           + "') AND upper( axis_orientation ) = upper('" + axis.getOrientationAsString()
                           + "') AND upper( units ) = " + "upper('" + axis.getUnits().getName() + "')";
        ResultSet rs = connection.prepareStatement( statement ).executeQuery();
        if ( rs.next() ) {
            LOG.info( "...found in the database already." );
            return rs.getInt( 1 );
        }

        try {
            exportIdentifiableProperties( axis );
            // insert into axis table
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO axis VALUES ( ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, axis.getName() );
            ps.setString( 3, axis.getUnits().getName() );
            ps.setString( 4, axis.getOrientationAsString() );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e );
        }

        return internalID++;
    }

    /**
     * Export the Vertical Datum to the database
     * 
     * @param vDatum
     *            the Vertical Datum object
     * @return the internal database ID assigned to the Vertical Datum
     * @throws SQLException
     *             when an SQLException occurs
     */
    protected int export( VerticalDatum vDatum )
                            throws SQLException {
        LOG.info( "Exporting Vertical Datum..." );
        ResultSet rs = connection.prepareStatement(
                                                    "SELECT ref_id FROM code, vertical_datum WHERE original= '"
                                                                            + vDatum.getCode().getOriginal()
                                                                            + "' AND vertical_datum.id = code.ref_id" ).executeQuery();
        if ( rs.next() ) {
            LOG.info( "...found in the database already." );
            return rs.getInt( 1 );
        }

        try {
            exportIdentifiableProperties( vDatum );

            // insert into the vertical_datum table
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO vertical_datum VALUES ( ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setNull( 2, java.sql.Types.DOUBLE );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e );
        }

        return internalID++;
    }

    /**
     * Insert the Vertical CRS data into the database
     * 
     * @param vertical
     *            the Vertical CRS object
     * @return the database internal ID assigned to the Vertical CRS
     * @throws SQLException
     *             when an SQLException occurs
     */
    protected int export( VerticalCRS vertical )
                            throws SQLException {
        LOG.info( "Exporting Vertical CRS..." );
        ResultSet rs = connection.prepareStatement(
                                                    "SELECT ref_id FROM code, vertical_crs WHERE original= '"
                                                                            + vertical.getCode().getOriginal()
                                                                            + "' AND vertical_crs.id = code.ref_id" ).executeQuery();
        if ( rs.next() ) {
            LOG.info( "...found in the database already." );
            return rs.getInt( 1 );
        }

        try {
            int axisID = export( vertical.getAxis()[0] );
            int verticalDatumID = export( (VerticalDatum) vertical.getDatum() );

            exportIdentifiableProperties( vertical );

            // insert into the crs_lookup table
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO crs_lookup VALUES ( ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "vertical_crs" );
            ps.execute();

            // insert into the geocentric_crs lookup table
            ps = connection.prepareStatement( "INSERT INTO vertical_crs VALUES ( ?, ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setInt( 2, axisID );
            ps.setInt( 3, verticalDatumID );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e );
        }

        return internalID++;
    }

    /**
     * Insert the Helmert Transformation properties into the database
     * 
     * @param helmert
     *            the helmert transformation object to be exported
     * @return the internal database ID assigned to the Helmert transformation object
     * @throws SQLException
     *             when an SQLException occurs
     */
    protected int export( Helmert helmert )
                            throws SQLException {
        LOG.info( "Exporting Helmert Transformation..." );
        ResultSet rs = connection.prepareStatement(
                                                    "SELECT id FROM helmert_transformation, code WHERE x_axis_translation = "
                                                                            + helmert.dx + " AND y_axis_translation = "
                                                                            + helmert.dy + " AND z_axis_translation = "
                                                                            + helmert.dz + " AND x_axis_rotation = "
                                                                            + helmert.ex + " AND y_axis_rotation = "
                                                                            + helmert.ey + " AND z_axis_rotation = "
                                                                            + helmert.ez + " AND scale_difference = "
                                                                            + helmert.ppm + " AND ref_id = id "
                                                                            + "AND original = '" + helmert.getCode().getOriginal() + "'" ).executeQuery();
        if ( rs.next() ) {
            LOG.info( "...found in the database already." );
            return rs.getInt( 1 );
        }

        try {
            exportIdentifiableProperties( helmert );

            // insert into the transformation_lookup table
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO transformation_lookup VALUES ( ?, ?)" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "helmert_transformation" );
            ps.execute();

            // insert into the helmert table
            ps = connection.prepareStatement( "INSERT INTO helmert_transformation VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setInt( 2, /* crsDBid.get( wgs84.getSourceCRS().getIdentifier() ) */42 ); // currently
            // wgs84.getSourceCRS() is null
            ps.setInt( 3, /* crsDBid.get( wgs84.getTargetCRS().getIdentifier() ) */24 ); // currently
            // wgs84.getTargetCRS() is null
            ps.setDouble( 4, helmert.dx );
            ps.setDouble( 5, helmert.dy );
            ps.setDouble( 6, helmert.dz );
            ps.setDouble( 7, helmert.ex );
            ps.setDouble( 8, helmert.ey );
            ps.setDouble( 9, helmert.ez );
            ps.setDouble( 10, helmert.ppm );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e );
        }

        return internalID++;
    }

    /**
     * Insert the Prime Meridian properties into the database
     * 
     * @param pm
     *            the Prime Meridian object
     * @return the internal database ID assigned to the object
     * @throws SQLException
     *             when an SQLException occurs
     */
    protected int export( PrimeMeridian pm )
                            throws SQLException {
        LOG.info( "Exporting Prime Meridian..." );
        ResultSet rs = connection.prepareStatement(
                                                    "SELECT id FROM prime_meridian "
                                                                            + "WHERE prime_meridian.longitude = "
                                                                            + pm.getLongitude()
                                                                            + " AND  upper( prime_meridian.unit ) = upper('"
                                                                            + pm.getAngularUnit().getName() + "') " ).executeQuery();
        if ( rs.next() ) {
            LOG.info( "...found in the database already." );
            return rs.getInt( 1 );
        }

        try {
            exportIdentifiableProperties( pm );

            // insert into the prime_meridian table
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO prime_meridian VALUES ( ?, ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, pm.getAngularUnit().getName().toLowerCase() );
            ps.setString( 3, Double.toString( pm.getLongitude() ) );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e );
        }

        return internalID++;
    }

    /**
     * Insert the Ellipsoid object data into the database
     * 
     * @param ellipsoid
     *            the ellipsoid object
     * @return the internal database ID assigned to the ellipsoid
     * @throws SQLException
     *             when an SQLException occurs
     */
    protected int export( Ellipsoid ellipsoid )
                            throws SQLException {
        LOG.info( "Exporting Ellipsoid..." );
        ResultSet rs = connection.prepareStatement(
                                                    "SELECT ref_id FROM code, ellipsoid " + "WHERE original= '"
                                                                            + ellipsoid.getCode().getOriginal()
                                                                            + "' AND ellipsoid.id = code.ref_id" ).executeQuery();
        if ( rs.next() ) {
            LOG.info( "...found in the database already." );
            return rs.getInt( 1 );
        }

        try {
            exportIdentifiableProperties( ellipsoid );

            // insert into ellipsoid table
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO ellipsoid VALUES (?, ?, ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, Double.toString( ellipsoid.getSemiMajorAxis() ) );

            double eccentricity = ellipsoid.getEccentricity();
            double inverseFlattening = ellipsoid.getInverseFlattening();
            double semiMinorAxis = ellipsoid.getSemiMinorAxis();

            setNullDoubleIf( eccentricity, 3, ps );
            setNullDoubleIf( inverseFlattening, 4, ps );
            setNullDoubleIf( semiMinorAxis, 5, ps );
            ps.setString( 6, ellipsoid.getUnits().getName() );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e );
        }

        return internalID++;
    }

    /**
     * Insert the Geodetic Datum properties into the database
     * 
     * @param gdatum
     *            the Geodetic Datum object
     * @return the internal database ID assigned to the Geodetic Datum
     * @throws SQLException
     *             when an SQLException occurs
     */
    protected int export( GeodeticDatum gdatum )
                            throws SQLException {
        LOG.info( "Exporting Geodetic Datum ..." );
        if ( !gdatum.getCode().getCode().equalsIgnoreCase( "NOT PROVIDED" ) ) {
            ResultSet rs = connection.prepareStatement(
                                                        "SELECT ref_id FROM code, geodetic_datum "
                                                                                + "WHERE original= '"
                                                                                + gdatum.getCode().getOriginal()
                                                                                + "' AND geodetic_datum.id = code.ref_id" ).executeQuery();
            if ( rs.next() ) {
                LOG.info( "...found in the database already." );
                return rs.getInt( 1 );
            }
        } else {
            ResultSet rs1 = connection.prepareStatement(
                                                         "SELECT geodetic_datum.id FROM geodetic_datum, ellipsoid, code "
                                                                                 + "WHERE geodetic_datum.ellipsoid_id = ellipsoid.id "
                                                                                 + "AND ellipsoid.id = code.ref_id "
                                                                                 + "AND original ='"
                                                                                 + gdatum.getEllipsoid().getCode().getOriginal()
                                                                                 + "'" ).executeQuery();
            Set<Integer> ellipsoidMatches = new HashSet<Integer>();
            while ( rs1.next() )
                ellipsoidMatches.add( rs1.getInt( 1 ) );

            ResultSet rs2 = connection.prepareStatement(
                                                         "SELECT geodetic_datum.id FROM geodetic_datum, prime_meridian "
                                                                                 + "WHERE geodetic_datum.prime_meridian_id = prime_meridian.id "
                                                                                 + "AND prime_meridian.longitude = "
                                                                                 + gdatum.getPrimeMeridian().getLongitude()
                                                                                 + " AND upper( prime_meridian.unit ) = upper('"
                                                                                 + gdatum.getPrimeMeridian().getAngularUnit().getName()
                                                                                 + "') " ).executeQuery();
            Set<Integer> pmMatches = new HashSet<Integer>();
            while ( rs2.next() )
                pmMatches.add( rs2.getInt( 1 ) );

            ResultSet rs3 = connection.prepareStatement(
                                                         "SELECT geodetic_datum.id FROM geodetic_datum, helmert_transformation WHERE geodetic_datum.helmert_id = helmert_transformation.id "
                                                                                 + "AND x_axis_translation = "
                                                                                 + gdatum.getWGS84Conversion().dx
                                                                                 + " AND y_axis_translation = "
                                                                                 + gdatum.getWGS84Conversion().dy
                                                                                 + " AND z_axis_translation = "
                                                                                 + gdatum.getWGS84Conversion().dz
                                                                                 + " AND x_axis_rotation = "
                                                                                 + gdatum.getWGS84Conversion().ex
                                                                                 + " AND y_axis_rotation = "
                                                                                 + gdatum.getWGS84Conversion().ey
                                                                                 + " AND z_axis_rotation = "
                                                                                 + gdatum.getWGS84Conversion().ez
                                                                                 + " AND scale_difference = "
                                                                                 + gdatum.getWGS84Conversion().ppm ).executeQuery();
            Set<Integer> helmertMatches = new HashSet<Integer>();
            while ( rs3.next() )
                helmertMatches.add( rs3.getInt( 1 ) );

            Set<Integer> intersection = ellipsoidMatches;
            intersection.retainAll( pmMatches );
            intersection.retainAll( helmertMatches );
            if ( !intersection.isEmpty() ) {
                Iterator<Integer> it = intersection.iterator();
                LOG.info( "...found in the database already." );
                return it.next();
            }
        }

        try {
            int ellipsoidID = export( gdatum.getEllipsoid() );
            int pmID = export( gdatum.getPrimeMeridian() );
            int helmertID = export( gdatum.getWGS84Conversion() );

            exportIdentifiableProperties( gdatum );

            // insert into the geodetic_datum table
            PreparedStatement preparedSt = connection.prepareStatement( "INSERT INTO geodetic_datum VALUES ( ?, ?, ?, ? )" );
            preparedSt.setInt( 1, internalID );
            preparedSt.setInt( 2, ellipsoidID );
            preparedSt.setInt( 3, pmID );
            preparedSt.setInt( 4, helmertID );
            preparedSt.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e );
        }

        return internalID++;
    }

    /**
     * Insert the Geocentric CRS data into the database
     * 
     * @param geocentric
     *            the Geocentric CRS object
     * @return the database internal ID assigned to the Geocentric CRS
     * @throws SQLException
     *             when an SQLException occurs
     */
    protected int export( GeocentricCRS geocentric )
                            throws SQLException {
        LOG.info( "Exporting Geocentric CRS..." );
        ResultSet rs = connection.prepareStatement(
                                                    "SELECT ref_id FROM code, geocentric_crs " + "WHERE original = '"
                                                                            + geocentric.getCode().getOriginal()
                                                                            + "' AND geocentric_crs.id = code.ref_id" ).executeQuery();
        if ( rs.next() ) {
            LOG.info( "...found in the database already." );
            return rs.getInt( 1 );
        }

        try {
            int axisID1 = export( geocentric.getAxis()[0] );
            int axisID2 = export( geocentric.getAxis()[1] );
            int axisID3 = export( geocentric.getAxis()[2] );
            int gdatumID = export( geocentric.getGeodeticDatum() );

            exportIdentifiableProperties( geocentric );

            // insert into the crs_lookup table
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO crs_lookup VALUES ( ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "geocentric_crs" );
            ps.execute();

            // insert into the geocentric_crs lookup table
            ps = connection.prepareStatement( "INSERT INTO geocentric_crs VALUES ( ?, ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setInt( 2, axisID1 );
            ps.setInt( 3, axisID2 );
            ps.setInt( 4, axisID3 );
            ps.setInt( 5, gdatumID );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( e.getMessage() );
        }

        return internalID++;
    }

    /**
     * Inserts the GeographicCRS data into the database
     * 
     * @param geographic
     *            the Geographic CRS object
     * @return the internal database ID for the Geographic CRS
     * @throws SQLException
     *             when an SQLException occurs
     */
    protected int export( GeographicCRS geographic )
                            throws SQLException {
        LOG.info( "Exporting Geographic CRS..." );
        ResultSet rs = connection.prepareStatement(
                                                    "SELECT ref_id FROM code, geographic_crs " + "WHERE original= '"
                                                                            + geographic.getCode().getOriginal()
                                                                            + "' AND geographic_crs.id = code.ref_id" ).executeQuery();
        if ( rs.next() ) {
            LOG.info( "...found in the database already." );
            return rs.getInt( 1 );
        }

        try {
            int axisID1 = export( geographic.getAxis()[0] );
            int axisID2 = export( geographic.getAxis()[1] );
            int gdatumID = export( geographic.getGeodeticDatum() );

            exportIdentifiableProperties( geographic );

            // insert into the crs_lookup table
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO crs_lookup VALUES ( ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "geographic_crs" );
            ps.execute();

            // insert into geographic_crs table
            ps = connection.prepareStatement( "INSERT INTO geographic_crs VALUES ( ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setInt( 2, axisID1 );
            ps.setInt( 3, axisID2 );
            ps.setInt( 4, gdatumID );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( e.getMessage(), e );
        }

        return internalID++;
    }

    /**
     * Checks for the type of projection that is supplied and delegates the insertion to the specific methods.
     * 
     * @param projection
     *            the Projection object
     * @return the internal database ID that was assigned to the projection
     * @throws SQLException
     *             when and SQLException occurs
     */
    protected int export( Projection projection )
                            throws SQLException {
        if ( projection instanceof TransverseMercator ) {
            return export( (TransverseMercator) projection );
        }

        if ( projection instanceof LambertConformalConic ) {
            return export( (LambertConformalConic) projection );
        }

        if ( projection instanceof LambertAzimuthalEqualArea ) {
            return export( (LambertAzimuthalEqualArea) projection );
        }

        if ( projection instanceof StereographicAzimuthal ) {
            return export( (StereographicAzimuthal) projection );
        }

        if ( projection instanceof StereographicAlternative ) {
            return export( (StereographicAlternative) projection );
        }
        if ( projection instanceof Mercator ) {
            return export( (Mercator) projection );
        }

        //
        // Export a custom projection
        //
        LOG.info( "Exporting a Custom Projection( that has class attribute)..." );
        String statementStr = "SELECT ref_id FROM code, custom_projection " + "WHERE original= '"
                              + projection.getCode().getOriginal() + "'";
        ResultSet rs = connection.prepareStatement( statementStr ).executeQuery();
        if ( rs.next() ) {
            LOG.info( "...found in the database already." );
            return rs.getInt( 1 );
        }

        int geographicID = export( projection.getGeographicCRS() );

        try {
            exportIdentifiableProperties( projection );

            // insert into projection_lookup table
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO projection_lookup VALUES (?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "custom_projection" );
            ps.execute();

            // insert into custom_projection
            ps = connection.prepareStatement( "INSERT INTO custom_projection VALUES (?, ?, ?, ?, ?, ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );

            Point2d p2d = projection.getNaturalOrigin();
            setNullDoubleIf( p2d.y, 2, ps ); // y for latitude (of natural origin)
            setNullDoubleIf( p2d.x, 3, ps ); // x for longitude (of natural origin)
            setNullDoubleIf( projection.getScale(), 4, ps );
            setNullDoubleIf( projection.getFalseEasting(), 5, ps );
            setNullDoubleIf( projection.getFalseNorthing(), 6, ps );

            ps.setInt( 7, geographicID );
            ps.setString( 8, projection.getUnits().getName() );
            ps.setString( 9, projection.getClass().getCanonicalName() );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e );
        }

        return internalID++;
    }

    /**
     * Inserts the Projected CRS data into the database
     * 
     * @param projected
     *            the projected CRS object
     * @return the database internal ID assigned to the Geocentric CRS
     * @throws SQLException
     */
    protected int export( ProjectedCRS projected )
                            throws SQLException {
        LOG.info( "Exporting Projected CRS..." );
        ResultSet rs = connection.prepareStatement(
                                                    "SELECT ref_id FROM code, projected_crs WHERE original= '"
                                                                            + projected.getCode().getOriginal()
                                                                            + "' AND projected_crs.id = code.ref_id" ).executeQuery();
        if ( rs.next() ) {
            LOG.info( "...found in the database already." );
            return rs.getInt( 1 );
        }

        int projectionID = export( projected.getProjection() );
        int axisID1 = export( projected.getAxis()[0] );
        int axisID2 = export( projected.getAxis()[1] );

        exportIdentifiableProperties( projected );

        // Insert into the crs_lookup TABLE
        PreparedStatement ps = connection.prepareStatement( "INSERT INTO crs_lookup VALUES ( ?, ? )" );
        ps.setInt( 1, internalID );
        ps.setString( 2, "projected_crs" );
        ps.execute();

        // insert into projected_crs table
        ps = connection.prepareStatement( "INSERT INTO projected_crs VALUES ( ?, ?, ?, ? )" );
        ps.setInt( 1, internalID );
        ps.setInt( 2, axisID1 );
        ps.setInt( 3, axisID2 );
        ps.setInt( 4, projectionID );
        ps.execute();

        return internalID++;
    }

    /**
     * Insert into the database the supplied Compound CRS
     * 
     * @param compound
     *            the Compound CRS object
     * @return the internal database ID assigned to the Compound CRS
     * @throws SQLException
     * @throws CRSException
     */
    protected int export( CompoundCRS compound )
                            throws SQLException, CRSException {
        LOG.info( "Exporting Compound CRS..." );
        ResultSet rs = connection.prepareStatement(
                                                    "SELECT ref_id FROM code, compound_crs " + "WHERE original= '"
                                                                            + compound.getCode().getOriginal()
                                                                            + "' AND compound_crs.id = code.ref_id" ).executeQuery();
        if ( rs.next() ) {
            LOG.info( "...found in the database already." );
            return rs.getInt( 1 );
        }

        try {
            int underlyingCRSID = export( compound.getUnderlyingCRS() );
            int heightAxisID = export( compound.getHeightAxis() );
            exportIdentifiableProperties( compound );

            // Insert into the crs_lookup TABLE
            PreparedStatement ps = connection.prepareStatement( "INSERT INTO crs_lookup VALUES ( ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "compound_crs" );
            ps.execute();

            // Insert into the compound_crs TABLE
            ps = connection.prepareStatement( "INSERT INTO compound_crs VALUES ( ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setInt( 2, underlyingCRSID );
            ps.setInt( 3, heightAxisID );
            ps.setDouble( 4, compound.getDefaultHeight() );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e );
        }

        return internalID++;
    }

    /**
     * Delegate the CRS to the specific exporting method
     * 
     * @param crs
     *            the CRS object
     * @return the internal database id that was assigned to the supplied CoordinateSystem
     * @throws SQLException
     * @throws CRSException
     */
    protected int export( CoordinateSystem crs )
                            throws SQLException, CRSException {
        if ( crs.getType() == CoordinateSystem.COMPOUND_CRS )
            return export( (CompoundCRS) crs );
        if ( crs.getType() == CoordinateSystem.GEOCENTRIC_CRS )
            return export( (GeocentricCRS) crs );
        if ( crs.getType() == CoordinateSystem.GEOGRAPHIC_CRS )
            return export( (GeographicCRS) crs );
        if ( crs.getType() == CoordinateSystem.PROJECTED_CRS )
            return export( (ProjectedCRS) crs );
        if ( crs.getType() == CoordinateSystem.VERTICAL_CRS )
            return export( (VerticalCRS) crs );
        throw new CRSExportingException( "Unknown type of CRS encountered: " + crs );
    }

    /**
     * Export to database the supplied list of CoordinateSystems
     * 
     * @param crsList
     *            the CoordinateSystems as a List
     * @throws SQLException
     * @throws CRSException
     */
    public void export( List<CoordinateSystem> crsList )
                            throws SQLException, CRSException {
        if ( connection == null ) {
            throw new CRSExportingException(
                                             "The connection to the database has not been passed to CRSExporter. Try calling setConnection( Connection ) before exporting." );
        }

        if ( crsList == null || crsList.size() == 0 ) {
            LOG.info( "Available CRS list is null or empty! Nothing to export." );
            return;
        }

        PreparedStatement ps = connection.prepareStatement( "SELECT MAX(ref_id) FROM code" );
        ResultSet rs = ps.executeQuery();
        rs.next();
        internalID = rs.getInt( 1 ) + 1; // if rs.getInt(1) is NULL the value returned is 0 anyway

        for ( CoordinateSystem crs : crsList ) {
            if ( crs != null ) {
                export( crs );
            } else {
                LOG.warn( "A null CRS in the exporting CRS list." );
            }
        }
    }

    /**
     * Command-line tool for inserting a CRS that is provided in a either in WKT format (via a filename argument), or in
     * XML format (through its codetype).
     * 
     * @param args
     * @throws IOException
     * @throws UnknownCRSException
     */
    public static void main( String[] args )
                            throws IOException, UnknownCRSException {
        // get the instantiated CRS from WKT format
        if ( args != null ) {
            if ( args.length == 2 ) {
                List<CoordinateSystem> crsList = new ArrayList<CoordinateSystem>();
                if ( "wkt".equals( args[0] ) ) {
                    WKTParser parser = new WKTParser( args[1] );
                    CoordinateSystem crs = parser.parseCoordinateSystem();

                    crsList.add( crs );

                } else if ( "xml".equals( args[0] ) ) {
                    CoordinateSystem lookup = CRSRegistry.lookup(
                                                                  "org.deegree.crs.configuration.deegree.xml.DeegreeCRSProvider",
                                                                  args[1] );
                    crsList.add( lookup );
                } else {
                    throw new IllegalArgumentException(
                                                        "First parameters should be, wkt or xml, second a wktfile or an epsg code." );
                }

                CRSDBExporter exporter = new CRSDBExporter();
                exporter.exportFromOther( crsList.get( 0 ) );
            }
        }
    }

    /**
     * Method for inserting an Identifiable object into the database.
     * 
     * @param crsID
     *            the type to add to the database
     * @param className
     *            the class name of the object, so that the exporting method may be determined
     */
    private void exportFromOther( CRSIdentifiable crsID ) {

        // prepare the exporter ( and getting the database connection )
        CRSConfiguration dbConfig = CRSConfiguration.getCRSConfiguration();
        DatabaseCRSProvider dbProvider = (DatabaseCRSProvider) dbConfig.getProvider();
        Connection conn = dbProvider.getConnection();
        setConnection( conn );

        // determine the maximum id used so that the next additions can be under immediately larger ids
        try {
            PreparedStatement ps = conn.prepareStatement( "SELECT MAX(ref_id) FROM code" );
            ResultSet rs = ps.executeQuery();
            rs.next();
            internalID = rs.getInt( 1 ) + 1; // if rs.getInt(1) is NULL the value returned is 0 anyway
        } catch ( SQLException e1 ) {
            LOG.error( e1.getMessage(), e1 );
        }

        // since we do not know before runtime what crs type we are exporting
        // the accessing is done via the java reflection mechanism
        try {
            Method exportMethod = CRSDBExporter.class.getDeclaredMethod( "export", crsID.getClass() );
            exportMethod.invoke( this, crsID );

        } catch ( SecurityException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( NoSuchMethodException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( IllegalArgumentException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( IllegalAccessException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( InvocationTargetException e ) {
            LOG.error( e.getMessage(), e );
        }
    }
}
