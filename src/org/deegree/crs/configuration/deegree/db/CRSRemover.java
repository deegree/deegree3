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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.crs.projections.Projection;
import org.deegree.crs.transformations.helmert.Helmert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>CRSRemover</code> class deletes a CRS from the database. The provided CRS if first identified with the
 * one in the database via its code (every CRS needs to have a code). Identifiable objects other than CRSs (axes,
 * datums, ellipsoids, etc.) happen to not have codes all the time and thus will not be identified ( hence removed) in
 * these cases. Before an object is removed, a check that no other object references it is done.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class CRSRemover {

    private Logger LOG = LoggerFactory.getLogger( CRSRemover.class );

    Connection connection;

    /**
     * Determines the database internal ID of the provided {@link CRSIdentifiable} object based on the
     * {@link CRSCodeType} that it has
     * 
     * @param identifiable
     *            the {@link CRSIdentifiable} object
     * @return the database internal ID
     * @throws SQLException
     *             if an {@link SQLException} occurs
     */
    int getInternalID( CRSIdentifiable identifiable )
    throws SQLException {
        PreparedStatement ps = connection.prepareStatement( "SELECT ref_id FROM code WHERE code.code ='"
                                                            + identifiable.getCode().getCode()
                                                            + "' AND code.codespace ='"
                                                            + identifiable.getCode().getCodeSpace() + "'" );
        ResultSet rs = ps.executeQuery();
        rs.next();
        return rs.getInt( 1 );
    }

    /**
     * Deletes the database table attributes: code, name, version, description, area.
     * 
     * @param internalID
     *            the internal database ID
     * @throws SQLException
     *             if an {@link SQLException} occurs
     */
    void removeIdentifiableAttributes( int internalID )
    throws SQLException {
        connection.prepareStatement( "DELETE FROM code WHERE ref_id = " + internalID ).execute();
        connection.prepareStatement( "DELETE FROM name WHERE ref_id = " + internalID ).execute();
        connection.prepareStatement( "DELETE FROM version WHERE ref_id = " + internalID ).execute();
        connection.prepareStatement( "DELETE FROM description WHERE ref_id = " + internalID ).execute();
        connection.prepareStatement( "DELETE FROM area WHERE ref_id = " + internalID ).execute();
    }

    /**
     * Deletes the provided ellipsoid object from the database.
     * 
     * @param ellipsoid
     *            the {@link Ellipsoid} object
     * @throws SQLException
     *             if an {@link SQLException} occurs
     */
    void removeEllipsoid( Ellipsoid ellipsoid )
    throws SQLException {
        LOG.info( "Removing Ellipsoid element..." );
        if ( ellipsoid.getCode().getCode().equals( "NOT PROVIDED" ) ) {
            LOG.info( "A referenced Ellipsoid does not have a code. It will not be removed." );
            return;
        }

        int internalID = getInternalID( ellipsoid );

        ResultSet datumRef = connection.prepareStatement(
                                                         "SELECT * FROM geodetic_datum WHERE ellipsoid_id = "
                                                         + internalID ).executeQuery();
        if ( datumRef.next() ) {
            LOG.info( "An Ellipsoid is refereced by GeodeticDatum(s) that are still in the database " );
            return;
        }

        connection.prepareStatement( "DELETE FROM ellipsoid WHERE id = " + internalID ).execute();
        removeIdentifiableAttributes( internalID );
    }

    /**
     * Deletes the provided prime meridian object from the database.
     * 
     * @param pm
     *            the {@link PrimeMeridian} object
     * @throws SQLException
     *             if an {@link SQLException} occurs
     */
    void removePrimeMeridian( PrimeMeridian pm )
    throws SQLException {
        LOG.info( "Removing Prime Meridian element..." );
        if ( pm.getCode().getCode().equals( "NOT PROVIDED" ) ) {
            LOG.info( "A referenced Prime Meridian does not have a code. It will not be removed." );
            return;
        }

        int internalID = getInternalID( pm );

        ResultSet datumRef = connection.prepareStatement(
                                                         "SELECT * FROM geodetic_datum WHERE prime_meridian_id = "
                                                         + internalID ).executeQuery();
        if ( datumRef.next() ) {
            LOG.info( "A Prime Meridian is referenced by Geodetic Datum(s) that are still in the " + "database" );
            return;
        }

        connection.prepareStatement( "DELETE FROM prime_meridian WHERE id = " + internalID ).execute();
        removeIdentifiableAttributes( internalID );
    }

    /**
     * Deletes the provided Helmert transformation object from the database.
     * 
     * @param helmert
     *            the {@link Helmert} transformation object
     * @throws SQLException
     *             if an {@link SQLException} occurs
     */
    void removeHelmert( Helmert helmert )
    throws SQLException {
        LOG.info( "Removing Helmert element..." );
        if ( helmert.getCode().getCode().equals( "NOT PROVIDED" ) ) {
            LOG.info( "A referenced WGS84 Transformation does not have a code. It will not be removed." );
            return;
        }

        int internalID = getInternalID( helmert );

        ResultSet datumRef = connection.prepareStatement(
                                                         "SELECT * FROM geodetic_datum WHERE helmert_id = "
                                                         + internalID ).executeQuery();
        if ( datumRef.next() ) {
            LOG.info( "A WGS84 transformation is referenced by GeodeticaDatum(s) that are still" + "in the database" );
            return;
        }
        connection.prepareStatement( "DELETE FROM helmert_transformation WHERE id = " + internalID ).execute();
        connection.prepareStatement( "DELETE FROM transformation_lookup WHERE id = " + internalID ).execute();
        removeIdentifiableAttributes( internalID );
    }

    /**
     * Removes from the database the geodetic datum provided
     * 
     * @param datum
     *            the {@link GeodeticDatum} object
     * @throws SQLException
     *             if an {@link SQLException} exception occurs
     */
    void removeGeodeticDatum( GeodeticDatum datum )
    throws SQLException {
        LOG.info( "Removing Geodetic Datum element..." );
        if ( datum.getCode().getCode().equals( "NOT PROVIDED" ) ) {
            LOG.info( "A referenced Geodetic Datum referenced does not have a code. It will not be removed." );
            return;
        }

        int internalID = getInternalID( datum );

        ResultSet geographicRef = connection.prepareStatement(
                                                              "SELECT * FROM geographic_crs WHERE datum_id = "
                                                              + internalID ).executeQuery();
        if ( geographicRef.next() ) {
            LOG.info( "A geodeticDatum is referenced by geographicCRS(s) and/or geocentricCRS(s) "
                      + "that are still used in the database" );
            return;
        }
        ResultSet geocentricRef = connection.prepareStatement(
                                                              "SELECT * FROM geocentric_crs WHERE datum_id = "
                                                              + internalID ).executeQuery();
        if ( geocentricRef.next() ) {
            LOG.info( "A geodeticDatum is referenced by geographicCRS(s) and/or geocentricCRS(s) "
                      + "that are still used in the database" );
            return;
        }

        connection.prepareStatement( "DELETE FROM geodetic_datum WHERE id = " + internalID ).execute();
        removeEllipsoid( datum.getEllipsoid() );
        removePrimeMeridian( datum.getPrimeMeridian() );
        removeHelmert( datum.getWGS84Conversion() );
        removeIdentifiableAttributes( internalID );
    }

    /**
     * Removes from the database the vertical datum provided
     * 
     * @param verticalDatum
     *            the {@link VerticalDatum} object
     * @throws SQLException
     *             if an {@link SQLException} occurs
     */
    void removeVerticalDatum( VerticalDatum verticalDatum )
    throws SQLException {
        LOG.info( "Removing Vertical Datum..." );
        if ( verticalDatum.getCode().getCode().equals( "NOT PROVIDED" ) ) {
            LOG.info( "A referenced Vertical Datum does not have a code. It will not be removed." );
            return;
        }

        int internalID = getInternalID( verticalDatum );

        ResultSet verticalCRSRef = connection.prepareStatement(
                                                               "SELECT * FROM vertical_crs "
                                                               + "WHERE vertical_datum_id = "
                                                               + internalID ).executeQuery();
        if ( verticalCRSRef.next() ) {
            LOG.info( "A verticalDatum is referenced by verticalCRS(s) that are still in the" + "database" );
            return;
        }

        connection.prepareStatement( "DELETE FROM vertical_datum WHERE id = " + internalID ).execute();
        removeIdentifiableAttributes( internalID );
    }

    /**
     * Removes from the database the provided projection
     * 
     * @param projection
     *            the {@link Projection} object
     * @throws SQLException
     *             if an {@link SQLException} occurs
     */
    void removeProjection( Projection projection )
    throws SQLException {
        if ( projection.getCode().getCode().equals( "NOT PROVIDED" ) ) {
            LOG.info( "A referenced " + projection.getImplementationName()
                      + " projection does not have a code. It will not be removed." );
            return;
        }

        int internalID = getInternalID( projection );

        ResultSet projectedCRSRef = connection.prepareStatement(
                                                                "SELECT * FROM projected_crs WHERE"
                                                                + " projection_id = "
                                                                + internalID ).executeQuery();
        if ( projectedCRSRef.next() )
            LOG.info( "A projection is referenced a ProjectedCRS(s) still in the database. " );
        else {
            ResultSet rs = connection.prepareStatement(
                                                       "SELECT table_name FROM projection_lookup WHERE id = "
                                                       + internalID ).executeQuery();
            rs.next();
            connection.prepareStatement( "DELETE FROM " + rs.getString( 1 ) + " WHERE id = " + internalID ).execute();
            connection.prepareStatement( "DELETE FROM projection_lookup WHERE id = " + internalID ).execute();
            removeCRS( projection.getGeographicCRS() );
            removeIdentifiableAttributes( internalID );
        }
    }

    /**
     * Removes from the database the provided axis object
     * 
     * @param axis
     *            the {@link Axis} object
     * @throws SQLException
     *             if an {@link SQLException} exception occurs
     */
    void removeAxis( Axis axis )
    throws SQLException {
        LOG.info( "Removing Axis element... " );
        if ( axis.getCode().getCode().equals( "NOT PROVIDED" ) ) {
            LOG.info( "Axis element does not have a code. It will not be removed." );
            return;
        }

        int internalID = getInternalID( axis );

        ResultSet projectedCRSRef = connection.prepareStatement(
                                                                "SELECT * FROM projected_crs WHERE axis1_id = "
                                                                + internalID
                                                                + " OR axis2_id = "
                                                                + internalID ).executeQuery();
        if ( projectedCRSRef.next() ) {
            LOG.info( "An axis is referenced by projectedCRS(s) that are still used in the " + "database. " );
            return;
        }
        ResultSet geographicCRSRef = connection.prepareStatement(
                                                                 "SELECT * FROM geographic_crs WHERE axis1_id = "
                                                                 + internalID
                                                                 + " OR axis2_id = "
                                                                 + internalID ).executeQuery();
        if ( geographicCRSRef.next() ) {
            LOG.info( "An axis is referenced by projectedCRS(s) that are still used in the " + "database. " );
            return;
        }
        ResultSet geocentricCRSRef = connection.prepareStatement(
                                                                 "SELECT * FROM geocentric_crs WHERE axis1_id = "
                                                                 + internalID
                                                                 + " OR axis2_id = "
                                                                 + internalID
                                                                 + " OR axis3_id = "
                                                                 + internalID ).executeQuery();
        if ( geocentricCRSRef.next() ) {
            LOG.info( "An axis is referenced by projectedCRS(s) that are still used in the " + "database. " );
            return;
        }
        ResultSet verticalCRSRef = connection.prepareStatement(
                                                               "SELECT * FROM vertical_crs WHERE axis_id = "
                                                               + internalID ).executeQuery();
        if ( verticalCRSRef.next() ) {
            LOG.info( "An axis is referenced by projectedCRS(s) that are still used in the " + "database. " );
            return;
        }
        ResultSet compoundCRSRef = connection.prepareStatement(
                                                               "SELECT * FROM compound_crs WHERE height_axis_id = "
                                                               + internalID ).executeQuery();
        if ( compoundCRSRef.next() ) {
            LOG.info( "An axis is referenced by projectedCRS(s) that are still used in the " + "database. " );
            return;
        }

        removeIdentifiableAttributes( internalID );
        connection.prepareStatement( "DELETE FROM axis WHERE id = " + internalID ).execute();

    }

    /**
     * Removes a CRS from the database. Before deleting a crs, it is checked if any identifiables are referencing it
     * (with the exception of the referencing object that is given as parameter).
     * 
     * @param crs
     *            to be removed
     * @throws SQLException
     *             if an {@link SQLException} occurs
     */
    void removeCRS( CoordinateSystem crs )
    throws SQLException {
        if ( crs == null ) {
            LOG.warn( "A null CRS." );
            return;
        }

        if ( crs.getCode().getCode().equals( "NOT PROVIDED" ) ) {
            LOG.warn( "The CRS " + crs + " does not have a code. It will not be removed." );
            return;
        }

        if ( crs.getType() == CoordinateSystem.PROJECTED_CRS ) {
            ProjectedCRS projected = (ProjectedCRS) crs;

            int internalID = getInternalID( projected );

            ResultSet compoundRef = connection.prepareStatement(
                                                                "SELECT * FROM compound_crs " + "WHERE base_crs = "
                                                                + internalID ).executeQuery();
            if ( compoundRef.next() ) {
                LOG.info( "A projected CRS is referenced by compoundCrs(s) still in the database" );
                return;
            }
            LOG.info( "Removing projectedCRS " + projected.getCode() );
            connection.prepareStatement( "DELETE FROM projected_crs WHERE id = " + internalID ).execute();
            connection.prepareStatement( "DELETE FROM crs_lookup WHERE id = " + internalID ).execute();
            removeAxis( projected.getAxis()[0] );
            removeAxis( projected.getAxis()[1] );
            removeProjection( projected.getProjection() );
            removeIdentifiableAttributes( internalID );

        } else if ( crs.getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {
            GeographicCRS geographic = (GeographicCRS) crs;

            int internalID = getInternalID( geographic );

            ResultSet compoundRef = connection.prepareStatement(
                                                                "SELECT * FROM compound_crs " + "WHERE base_crs = "
                                                                + internalID ).executeQuery();
            if ( compoundRef.next() ) {
                LOG.info( "A geographicCRS is referenced by compoundCRS(s) that " + "are still in the database." );
                return;
            }

            ResultSet tmRef = connection.prepareStatement(
                                                          "SELECT * FROM transverse_mercator "
                                                          + "WHERE geographic_crs_id = "
                                                          + internalID ).executeQuery();
            if ( tmRef.next() ) {
                LOG.info( "A geographicCRS is referenced by projection(s) that " + "are still in the database." );
                return;
            }

            ResultSet lccRef = connection.prepareStatement(
                                                           "SELECT * FROM lambert_conformal_conic "
                                                           + "WHERE geographic_crs_id = "
                                                           + internalID ).executeQuery();
            if ( lccRef.next() ) {
                LOG.info( "A geographicCRS is referenced by projection(s) that " + "are still in the database." );
                return;
            }

            ResultSet laeaRef = connection.prepareStatement(
                                                            "SELECT * FROM lambert_azimuthal_equal_area "
                                                            + "WHERE geographic_crs_id = "
                                                            + internalID ).executeQuery();
            if ( laeaRef.next() ) {
                LOG.info( "A geographicCRS is referenced by projection(s) that " + "are still in the database." );
                return;
            }

            ResultSet salRef = connection.prepareStatement(
                                                           "SELECT * FROM stereographic_alternative "
                                                           + "WHERE geographic_crs_id = "
                                                           + internalID ).executeQuery();
            if ( salRef.next() ) {
                LOG.info( "A geographicCRS is referenced by projection(s) that " + "are still in the database." );
                return;
            }

            ResultSet sazRef = connection.prepareStatement(
                                                           "SELECT * FROM stereographic_azimuthal "
                                                           + "WHERE geographic_crs_id = "
                                                           + internalID ).executeQuery();
            if ( sazRef.next() ) {
                LOG.info( "A geographicCRS is referenced by projection(s) that " + "are still in the database." );
                return;
            }

            LOG.info( "Removing the Geographic CRS: " + geographic.getCode() );
            connection.prepareStatement( "DELETE FROM geographic_crs WHERE id = " + internalID ).execute();
            connection.prepareStatement( "DELETE FROM crs_lookup WHERE id = " + internalID ).execute();
            removeGeodeticDatum( geographic.getGeodeticDatum() );
            removeAxis( geographic.getAxis()[0] );
            removeAxis( geographic.getAxis()[1] );
            removeIdentifiableAttributes( internalID );

        } else if ( crs.getType() == CoordinateSystem.GEOCENTRIC_CRS ) {
            GeocentricCRS geocentric = (GeocentricCRS) crs;

            int internalID = getInternalID( geocentric );

            ResultSet compoundRef = connection.prepareStatement(
                                                                "SELECT * FROM compound_crs " + "WHERE base_crs = "
                                                                + internalID ).executeQuery();
            if ( compoundRef.next() ) {
                LOG.info( "A geocentricCRS is referenced by compoundCRS(s) that " + "are still in the database." );
                return;
            }

            LOG.info( "Removing the Geocentric CRS: " + geocentric.getCode() );
            connection.prepareStatement( "DELETE FROM geocentric_crs WHERE id = " + internalID ).execute();
            connection.prepareStatement( "DELETE FROM crs_lookup WHERE id = " + internalID ).execute();
            removeGeodeticDatum( geocentric.getGeodeticDatum() );
            removeAxis( geocentric.getAxis()[0] );
            removeAxis( geocentric.getAxis()[1] );
            removeAxis( geocentric.getAxis()[2] );
            removeIdentifiableAttributes( internalID );

        } else if ( crs.getType() == CoordinateSystem.VERTICAL_CRS ) {
            VerticalCRS vertical = (VerticalCRS) crs;

            int internalID = getInternalID( vertical );

            LOG.info( "Removing the Vertical CRS: " + vertical.getCode() );
            connection.prepareStatement( "DELETE FROM vertical_crs WHERE id = " + internalID ).execute();
            connection.prepareStatement( "DELETE FROM crs_lookup WHERE id = " + internalID ).execute();
            removeAxis( vertical.getVerticalAxis() );
            removeVerticalDatum( vertical.getVerticalDatum() );
            removeIdentifiableAttributes( internalID );

        } else if ( crs.getType() == CoordinateSystem.COMPOUND_CRS ) {
            CompoundCRS compound = (CompoundCRS) crs;

            int internalID = getInternalID( compound );

            LOG.info( "Removing Compound CRS " + compound.getCode() );
            connection.prepareStatement( "DELETE FROM compound_crs WHERE id = " + internalID ).execute();
            connection.prepareStatement( "DELETE FROM crs_lookup WHERE id = " + internalID ).execute();
            removeCRS( compound.getUnderlyingCRS() );
            removeAxis( compound.getHeightAxis() );
            removeIdentifiableAttributes( internalID );
        }
    }

    /**
     * Removes all the crs's provided as the arguments list
     * 
     * @param crsList
     *            the list of {@CoordinateSystem}s that will be removed from db
     * @throws SQLException
     *             if an {@link SQLException} occurs
     */
    public void removeCRSList( List<CoordinateSystem> crsList )
    throws SQLException {
        if ( crsList == null || crsList.size() == 0 ) {
            System.out.println( "No CRSs to export." );
            return;
        }
        for ( CoordinateSystem crs : crsList ) {
            removeCRS( crs );
        }
    }

    /**
     * Set the connection locally.
     * 
     * @param conn
     *            the database {@link Connection}
     */
    public void setConnection( Connection conn ) {
        this.connection = conn;
    }

    /**
     * Close the database connection
     * 
     * @throws SQLException
     */
    public void closeConnection()
    throws SQLException {
        connection.close();
    }

    /**
     * {@link CRSRemover} constructor
     */
    public CRSRemover() {
        // nothing necessary yet.
    }

    /**
     * Command-line tool for removing a CRS from the CRS db. It can be a given in WKT form (in a file),
     * or as a reference to the xml crs file.
     * 
     * Hence the argument list can be either
     * <ul>
     * <li>wkt your-wkt-file.txt</li>
     * <li>xml id-of-the-crs</li>
     * </ul>
     * 
     * To run this you have to set the environment variable CRS_DB_URL to 
     * jdbc:derby:${your-workspace-path}/d3_core/src/META-INF/deegreeCRS, otherwise 
     * the database will be accessed read-only, hence no changes will be possible.
     * 
     * @param args
     * @throws Exception
     * @throws Exception
     */
    public static void main( String[] args )
    throws Exception {

        if ( args.length != 2 ) {
            throw new Exception(
            "Exactly 2 arguments have to be provided ( 'xml|wkt  crs-code-type|wkt-filename' )" );
        }

        try {
            CoordinateSystem crs = null;
            if ( args[0].equals( "wkt" ) ) {
                // get the instantiated CRS from WKT format
                WKTParser parser = new WKTParser( args[1] );
                crs = parser.parseCoordinateSystem();
            } else if ( args[0].equals( "xml" ) ) {
                crs = CRSRegistry.lookup( args[1] );

            } else {
                throw new Exception( "First argument must be either 'xml' or 'wkt'" );
            }

            // preparing the crs to remove
            List<CoordinateSystem> crsList = new ArrayList<CoordinateSystem>();
            crsList.add( crs );

            DatabaseCRSProvider dbProvider = (DatabaseCRSProvider) CRSConfiguration.getCRSConfiguration(
                                                                                                        "org.deegree.crs.configuration.deegree.db.DatabaseCRSProvider" ).getProvider();
            dbProvider.remove( crsList );
        } catch ( UnknownCRSException e ) {
            System.err.println( "The crs code-type provided as the second argument is unknown. " +
                                "Original exception message: '" + e.getMessage() + "'" );
        } catch ( SQLException e ) {
            System.err.println( e.getMessage() );
        } catch ( IOException e ) {
            System.err.println( e.getMessage() );
        }
    }
}
