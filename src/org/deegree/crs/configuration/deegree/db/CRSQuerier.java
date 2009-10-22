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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.crs.CRSCodeType;
import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.components.Axis;
import org.deegree.crs.components.Ellipsoid;
import org.deegree.crs.components.GeodeticDatum;
import org.deegree.crs.components.PrimeMeridian;
import org.deegree.crs.components.Unit;
import org.deegree.crs.components.VerticalDatum;
import org.deegree.crs.configuration.CRSConfiguration;
import org.deegree.crs.configuration.deegree.xml.CRSExporter;
import org.deegree.crs.coordinatesystems.CompoundCRS;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.coordinatesystems.GeocentricCRS;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.crs.coordinatesystems.VerticalCRS;
import org.deegree.crs.exceptions.CRSConfigurationException;
import org.deegree.crs.i18n.Messages;
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
 * The <code>CRSQuerier</code> class instantiates all the CRSs, projections, transformations, datums, ellipsoids, prime
 * meridians - from the database backend.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class CRSQuerier {

    private static Logger LOG = LoggerFactory.getLogger( CRSQuerier.class );

    private Connection conn = null;

    /**
     * maps based on internal database IDs -- called in getCRSByCode() where queries return internal IDs
     */
    private Map<Integer, Axis> loadedAxesMap = new HashMap<Integer, Axis>();

    private Map<Integer, GeodeticDatum> loadedDatumsMap = new HashMap<Integer, GeodeticDatum>();

    private Map<Integer, Ellipsoid> loadedEllipsoidsMap = new HashMap<Integer, Ellipsoid>();

    private Map<Integer, PrimeMeridian> loadedPMeridiansMap = new HashMap<Integer, PrimeMeridian>();

    private Map<Integer, Helmert> loadedHelmertsMap = new HashMap<Integer, Helmert>();

    private Map<Integer, TransverseMercator> loadedTMercatorMap = new HashMap<Integer, TransverseMercator>();

    private Map<Integer, GeographicCRS> loadedGeographicsMap = new HashMap<Integer, GeographicCRS>();

    private Map<Integer, GeocentricCRS> loadedGeocentricsMap = new HashMap<Integer, GeocentricCRS>();

    private Map<Integer, ProjectedCRS> loadedProjectedsMap = new HashMap<Integer, ProjectedCRS>();

    private Map<Integer, CompoundCRS> loadedCompoundsMap = new HashMap<Integer, CompoundCRS>();

    /**
     * Map based on CRSCodeTypes -- used in the getCRSByCode() queries
     */
    private Map<CRSCodeType, CRSIdentifiable> cachedCRSs = new HashMap<CRSCodeType, CRSIdentifiable>();

    /**
     * Method currently used by the EPSGDatabaseSynchronizer to get the internal ID for projections.
     * 
     * @param identifiable
     *            CRS Identifiable object
     * @return the internal database ID under which the Identifiable object can be found
     */
    protected int getInternalID( CRSIdentifiable identifiable ) {
        try {
            PreparedStatement ps = conn.prepareStatement( "SELECT ref_id FROM code WHERE code = '"
                                                          + identifiable.getCode().getCode() + "' "
                                                          + "AND codespace = '" + identifiable.getCode().getCodeSpace()
                                                          + "'" );
            ResultSet rs = ps.executeQuery();
            if ( rs.next() ) {
                return rs.getInt( 1 );
            }
            LOG.warn( "CRSIdentifiable was unable to find an internal ID for the supplied identifiable object" );
            return -1;

        } catch ( SQLException e ) {
            LOG.error( e.getLocalizedMessage(), e );
            return 0;
        }

    }

    /**
     * Method currently used by the EPSGDatabaseSynchronizer to update the in-house database with the EPSG codes found
     * in the EPSG database
     * 
     * @param internalID
     *            internal database ID of the object that will get an EPSG id
     * @param code
     *            the EPSG id
     * @throws SQLException
     */
    protected void setCode( int internalID, String code )
                            throws SQLException {
        Statement stmt = conn.createStatement();
        // Execute the update
        int rows = stmt.executeUpdate( "UPDATE code SET code.code='" + code
                                       + "', code.codespace='EPSG' WHERE code.ref_id = " + internalID );
        LOG.info( rows + " Row(s) modified when Updating the internal id: " + internalID );

        stmt.close();
    }

    /**
     * Gets the Projection identified by the supplied internal database ID
     * 
     * @param projectionId
     *            the internal database ID
     * @return the Projection object
     * @throws SQLException
     */
    protected Projection getProjection( int projectionId )
                            throws SQLException {
        Projection proj = null;

        PreparedStatement prepSt = conn.prepareStatement( "SELECT table_name FROM " + "projection_lookup WHERE id = "
                                                          + projectionId );
        ResultSet rs = prepSt.executeQuery();
        rs.next();

        String tableName = rs.getString( 1 );

        if ( tableName.equals( "transverse_mercator" ) ) {
            proj = getTransverseMercator( projectionId );
        } else if ( tableName.equals( "lambert_conformal_conic" ) ) {
            proj = getLambertConformalConic( projectionId );
        } else if ( tableName.equals( "stereographic_azimuthal" ) ) {
            proj = getStereographicAzimuthal( projectionId );
        } else if ( tableName.equals( "stereographic_alternative" ) ) {
            proj = getStereographicAlternative( projectionId );
        } else if ( tableName.equals( "lambert_azimuthal_equal_area" ) ) {
            proj = getLambertAzimuthalEqualArea( projectionId );
        } else if ( "mercator".equalsIgnoreCase( tableName ) ) {
            proj = getMercator( projectionId );
        } else if ( tableName.equals( "custom_projection" ) ) {
            proj = getCustomProjection( projectionId );
        }
        return proj;
    }

    /**
     * Gets the custom Projection (that is loaded using the information in the provided classname attribute) that is
     * identified by the supplied database internal ID.
     * 
     * @param projectionID
     *            the database internal ID
     * @return the Projection object
     * @throws SQLException
     */
    protected Projection getCustomProjection( int projectionID )
                            throws SQLException {
        Projection p = null;
        try {
            PreparedStatement ps = conn.prepareStatement( "SELECT id, geographic_crs_id, "
                                                          + "natural_origin_lat, natural_origin_long, "
                                                          + "scale, false_easting, false_northing, "
                                                          + "units, class FROM custom_projection " + "WHERE id = "
                                                          + projectionID );
            ResultSet rs = ps.executeQuery();
            rs.next();

            Unit projUnit = Unit.createUnitFromString( rs.getString( 8 ) );

            // CRSIdentifiable identifiable = getIdentifiableAttributes( projectionID ); // get the identifiable object
            // for this id

            String className = rs.getString( 9 );
            Class<?> t = Class.forName( className );
            t.asSubclass( Projection.class );
            Constructor<?> constructor = t.getConstructor( GeographicCRS.class, double.class, double.class,
                                                           Point2d.class, Unit.class, double.class, List.class );

            p = (Projection) constructor.newInstance( getGeographicCRS( rs.getInt( 2 ) ), rs.getDouble( 7 ),
                                                      rs.getDouble( 6 ), new Point2d( rs.getDouble( 1 ),
                                                                                      rs.getDouble( 2 ) ), projUnit,
                                                      rs.getDouble( 5 ), null );

        } catch ( ClassNotFoundException e ) {
            LOG.error( Messages.getMessage( "CUSTOM_PROJECTION_CLASS_INSTANTIATION", projectionID, e.getMessage() ), e );
        } catch ( NoSuchMethodException e ) {
            LOG.error( Messages.getMessage( "CUSTOM_PROJECTION_CLASS_INSTANTIATION", projectionID, e.getMessage() ), e );
        } catch ( InvocationTargetException e ) {
            LOG.error( e.getCause().getMessage(), e.getCause() );
            LOG.error( Messages.getMessage( "CUSTOM_PROJECTION_CLASS_INSTANTIATION", projectionID, e.getMessage() ), e );
        } catch ( IllegalAccessException e ) {
            LOG.error( Messages.getMessage( "CUSTOM_PROJECTION_CLASS_INSTANTIATION", projectionID, e.getMessage() ), e );
        } catch ( InstantiationException e ) {
            LOG.error( Messages.getMessage( "CUSTOM_PROJECTION_CLASS_INSTANTIATION", projectionID, e.getMessage() ), e );
        }
        return p;
    }

    /**
     * Gets the Lambert Azimuthal Equal Area projection data from the database (identified by the supplied internal
     * database ID) and instantiates an LambertAzimuthalEqualArea object
     * 
     * @param projectionId
     *            the internal database ID
     * @return the LambertAzimuthalEqualArea object
     * @throws SQLException
     */
    protected LambertAzimuthalEqualArea getLambertAzimuthalEqualArea( int projectionId )
                            throws SQLException {
        LambertAzimuthalEqualArea laea = null;
        PreparedStatement ps = conn.prepareStatement( "SELECT natural_origin_lat, " + "natural_origin_long, scale"
                                                      + ", false_easting, false_northing, "
                                                      + "geographic_crs_id, units, id FROM "
                                                      + "lambert_azimuthal_equal_area WHERE " + "id = " + projectionId );
        ResultSet rs = ps.executeQuery();
        rs.next();

        Unit units = Unit.createUnitFromString( rs.getString( 7 ) );
        CRSIdentifiable identifiable = getIdentifiableAttributes( rs.getInt( 8 ) );
        laea = new LambertAzimuthalEqualArea( getGeographicCRS( rs.getInt( 6 ) ), rs.getDouble( 5 ), rs.getDouble( 4 ),
                                              new Point2d( rs.getDouble( 2 ), rs.getDouble( 1 ) ), units,
                                              rs.getDouble( 3 ), identifiable );
        return laea;
    }

    /**
     * Gets the Stereographic Alternative projection data from the database (identified by the supplied internal
     * database ID) and instantiates a {@link StereographicAlternative} object
     * 
     * @param projectionId
     *            the internal database ID
     * @return the StereographicAlternative object
     * @throws SQLException
     */

    protected StereographicAlternative getStereographicAlternative( int projectionId )
                            throws SQLException {
        StereographicAlternative sal = null;
        PreparedStatement ps = conn.prepareStatement( "SELECT natural_origin_lat, " + "natural_origin_long, scale"
                                                      + ", false_easting, false_northing, "
                                                      + "geographic_crs_id, units, id FROM "
                                                      + "stereographic_alternative WHERE " + "id = " + projectionId );
        ResultSet rs = ps.executeQuery();
        rs.next();

        Unit units = Unit.createUnitFromString( rs.getString( 7 ) );
        CRSIdentifiable identifiable = getIdentifiableAttributes( rs.getInt( 8 ) );
        sal = new StereographicAlternative( getGeographicCRS( rs.getInt( 6 ) ), rs.getDouble( 5 ), rs.getDouble( 4 ),
                                            new Point2d( rs.getDouble( 2 ), rs.getDouble( 1 ) ), units,
                                            rs.getDouble( 3 ), identifiable );
        return sal;
    }

    /**
     * Gets the Mercator projection data from the database (identified by the supplied internal database ID) and
     * instantiates a {@link Mercator} object
     * 
     * @param projectionId
     *            the internal database ID
     * @return the Mercator object
     * @throws SQLException
     */
    private Mercator getMercator( int projectionId )
                            throws SQLException {
        PreparedStatement ps = conn.prepareStatement( "SELECT natural_origin_lat, natural_origin_long,"
                                                      + "scale, false_easting, false_northing, "
                                                      + "geographic_crs_id, units, id FROM mercator " + "WHERE id = "
                                                      + projectionId );
        ResultSet rs = ps.executeQuery();
        rs.next();

        Unit units = Unit.createUnitFromString( rs.getString( 7 ) );
        CRSIdentifiable identifiable = getIdentifiableAttributes( rs.getInt( 8 ) );
        Mercator proj = new Mercator( getGeographicCRS( rs.getInt( 6 ) ), rs.getDouble( 5 ), rs.getDouble( 4 ),
                                      new Point2d( rs.getDouble( 2 ), rs.getDouble( 1 ) ), units, rs.getDouble( 3 ),
                                      identifiable );
        return proj;
    }

    /**
     * Gets the Stereographic Azimuthal projection data from the database (identified by the supplied internal database
     * ID) and instantiates a {@link StereographicAzimuthal} object
     * 
     * @param projectionId
     *            the internal database ID
     * @return the StereographicAzimuthal object
     * @throws SQLException
     */
    protected StereographicAzimuthal getStereographicAzimuthal( int projectionId )
                            throws SQLException {
        StereographicAzimuthal sa = null;
        PreparedStatement ps = conn.prepareStatement( "SELECT natural_origin_lat, " + "natural_origin_long, scale"
                                                      + ", false_easting, false_northing, " + "true_scale_latitude, "
                                                      + "geographic_crs_id, units, id "
                                                      + "FROM stereographic_azimuthal WHERE " + "id = " + projectionId );
        ResultSet rs = ps.executeQuery();
        rs.next();

        Unit units = Unit.createUnitFromString( rs.getString( 8 ) );
        CRSIdentifiable identifiable = getIdentifiableAttributes( rs.getInt( 9 ) );
        sa = new StereographicAzimuthal( rs.getInt( 6 ), getGeographicCRS( rs.getInt( 7 ) ), rs.getDouble( 5 ),
                                         rs.getDouble( 4 ), new Point2d( rs.getDouble( 2 ), rs.getDouble( 1 ) ), units,
                                         rs.getDouble( 3 ), identifiable );
        return sa;
    }

    /**
     * Gets the Lambert Conformal Conic projection data from the database (identified by the supplied internal database
     * ID) and instantiates a {@link LambertConformalConic} object
     * 
     * @param projectionId
     *            the internal database ID
     * @return the LambertConformalConic object
     * @throws SQLException
     */
    protected LambertConformalConic getLambertConformalConic( int projectionId )
                            throws SQLException {
        LambertConformalConic lcc = null;
        PreparedStatement ps = conn.prepareStatement( "SELECT natural_origin_lat, " + "natural_origin_long, scale"
                                                      + ", false_easting, false_northing, " + "firstParallelLatitude, "
                                                      + "secondParallelLatitude, " + "geographic_crs_id, units, id "
                                                      + "FROM lambert_conformal_conic WHERE " + "id = " + projectionId );
        ResultSet rs = ps.executeQuery();
        rs.next();

        Unit units = Unit.createUnitFromString( rs.getString( 9 ) );
        CRSIdentifiable identifiable = getIdentifiableAttributes( rs.getInt( 10 ) );
        double firstParallelLatitude = rs.getDouble( 6 );
        if ( rs.wasNull() )
            firstParallelLatitude = Double.NaN;
        double secondParallelLatitude = rs.getDouble( 7 );
        if ( rs.wasNull() )
            secondParallelLatitude = Double.NaN;
        lcc = new LambertConformalConic( firstParallelLatitude, secondParallelLatitude,
                                         getGeographicCRS( rs.getInt( 8 ) ), rs.getDouble( 5 ), rs.getDouble( 4 ),
                                         new Point2d( rs.getDouble( 2 ), rs.getDouble( 1 ) ), units, rs.getDouble( 3 ),
                                         identifiable );
        return lcc;
    }

    /**
     * Gets the Transverse Mercator projection data from the database (identified by the supplied internal database ID)
     * and instantiates a {@link TransverseMercator} object
     * 
     * @param projectionId
     *            the internal database ID
     * @return the TransverseMercator object
     * @throws SQLException
     */
    protected TransverseMercator getTransverseMercator( int projectionId )
                            throws SQLException {
        if ( loadedTMercatorMap.containsKey( projectionId ) ) {
            return loadedTMercatorMap.get( projectionId );
        }
        TransverseMercator tm = null;
        PreparedStatement ps = conn.prepareStatement( "SELECT natural_origin_lat, natural_origin_long, "
                                                      + "scale, false_easting, false_northing, "
                                                      + "northern_hemisphere, geographic_crs_id, units, "
                                                      + "id FROM transverse_mercator WHERE id = " + projectionId );
        ResultSet rs = ps.executeQuery();

        if ( rs.next() ) {
            Unit units = Unit.createUnitFromString( rs.getString( 8 ) );
            CRSIdentifiable identifiable = getIdentifiableAttributes( rs.getInt( 9 ) );
            boolean northernH = ( rs.getInt( 6 ) > 0 ) ? true : false;
            tm = new TransverseMercator( northernH, getGeographicCRS( rs.getInt( 7 ) ), rs.getDouble( 5 ),
                                         rs.getDouble( 4 ), new Point2d( rs.getDouble( 2 ), rs.getDouble( 1 ) ), units,
                                         rs.getDouble( 3 ), identifiable );
            loadedTMercatorMap.put( projectionId, tm );
        }
        return tm;

    }

    /**
     * Get the Geographic CRS object from the database given its internal ID
     * 
     * @param geographicId
     *            internal database ID of the Geographic CRS
     * @return the Geographic CRS object
     * @throws SQLException
     */
    protected GeographicCRS getGeographicCRS( int geographicId )
                            throws SQLException {
        if ( loadedGeographicsMap.containsKey( geographicId ) )
            return loadedGeographicsMap.get( geographicId );

        GeographicCRS geo = null;
        PreparedStatement ps = conn.prepareStatement( "SELECT axis1_id, axis2_id,"
                                                      + " datum_id, id FROM geographic_crs WHERE id = " + geographicId );
        ResultSet rs = ps.executeQuery();
        rs.next();

        CRSIdentifiable identifiable = getIdentifiableAttributes( rs.getInt( 4 ) );
        geo = new GeographicCRS( getGeodeticDatum( rs.getInt( 3 ) ), new Axis[] { getAxis( rs.getInt( 1 ) ),
                                                                                 getAxis( rs.getInt( 2 ) ) },
                                 identifiable );
        loadedGeographicsMap.put( geographicId, geo );

        return geo;
    }

    /**
     * Get the Vertical CRS object from the database given its internal ID.
     * 
     * @param verticalID
     *            internal database ID of the Vertical CRS
     * @return the {@link VerticalCRS} object
     * @throws SQLException
     */
    protected VerticalCRS getVerticalCRS( int verticalID )
                            throws SQLException {
        ResultSet verticalRows = conn.prepareStatement(
                                                        "SELECT axis_id, vertical_datum_id FROM "
                                                                                + " vertical_crs WHERE id = "
                                                                                + verticalID ).executeQuery();
        verticalRows.next();
        CRSIdentifiable identifiable = getIdentifiableAttributes( verticalID );
        return new VerticalCRS( getVerticalDatum( verticalRows.getInt( 2 ) ),
                                new Axis[] { getAxis( verticalRows.getInt( 1 ) ) }, identifiable );
    }

    /**
     * Get the Vertical Datum from the database given its internal database ID
     * 
     * @param vDatumID
     *            the internal database ID of the Vertical Datum
     * @return the {@link VerticalDatum} object
     * @throws SQLException
     */
    protected VerticalDatum getVerticalDatum( int vDatumID )
                            throws SQLException {
        CRSIdentifiable identifiable = getIdentifiableAttributes( vDatumID );
        return new VerticalDatum( identifiable );
    }

    /**
     * Get the Geodetic Datum from the database given its internal database ID
     * 
     * @param datumId
     *            the internal database ID of the Geodetic Datum
     * @return the Geodetic Datum object
     * @throws SQLException
     */
    protected GeodeticDatum getGeodeticDatum( int datumId )
                            throws SQLException {
        if ( loadedDatumsMap.containsKey( datumId ) )
            return loadedDatumsMap.get( datumId );

        GeodeticDatum gd = null;
        PreparedStatement prepSt = conn.prepareStatement( "SELECT ellipsoid_id, prime_meridian_id, "
                                                          + "helmert_id FROM geodetic_datum WHERE id = " + datumId );
        ResultSet rs = prepSt.executeQuery();
        if ( !rs.next() )
            throw new CRSConfigurationException(
                                                 Messages.getMessage( "CRS_CONFIG_NO_ELEMENT", "geodeticDatum", datumId ) );
        CRSIdentifiable identifiable = getIdentifiableAttributes( datumId );
        Ellipsoid ellipsoid = getEllipsoid( rs.getInt( 1 ) );
        if ( ellipsoid == null )
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_DATUM_HAS_NO_ELLIPSOID", datumId ) );
        gd = new GeodeticDatum( ellipsoid, getPrimeMeridian( rs.getInt( 2 ) ),
                                getHelmertTransformation( rs.getInt( 3 ) ), identifiable );
        loadedDatumsMap.put( datumId, gd );

        return gd;
    }

    /**
     * Get the Helmert Transformation object from the database given its internal database ID
     * 
     * @param helmertId
     *            the database internal ID of the Helmert Transformation object
     * @return the Helmert Transformation object
     * @throws SQLException
     */
    protected Helmert getHelmertTransformation( int helmertId )
                            throws SQLException {
        if ( loadedHelmertsMap.containsKey( helmertId ) )
            return loadedHelmertsMap.get( helmertId );

        Helmert h = null;
        PreparedStatement prepSt = conn.prepareStatement( "SELECT source_crs_id, x_axis_translation, "
                                                          + "y_axis_translation, z_axis_translation, "
                                                          + "x_axis_rotation, y_axis_rotation, "
                                                          + "z_axis_rotation, scale_difference "
                                                          + "FROM helmert_transformation " + "WHERE id = " + helmertId );
        ResultSet rs = prepSt.executeQuery();
        if ( !rs.next() )
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NO_ELEMENT", "wgs84transformation",
                                                                      helmertId ) );
        CRSIdentifiable identifiable = getIdentifiableAttributes( helmertId );
        h = new Helmert( rs.getDouble( 2 ), rs.getDouble( 3 ), rs.getDouble( 4 ), rs.getDouble( 5 ), rs.getDouble( 6 ),
                         rs.getDouble( 7 ), rs.getDouble( 8 ), null, GeographicCRS.WGS84, identifiable, false );
        loadedHelmertsMap.put( helmertId, h );
        return h;
    }

    /**
     * Gets the Prime Meridian data from the database ( given its intenal database ID ) and instantiates a
     * {@link PrimeMeridian} object
     * 
     * @param pmId
     *            the internal database ID of Prime Meridian
     * @return the PrimeMeridian object
     * @throws SQLException
     */
    protected PrimeMeridian getPrimeMeridian( int pmId )
                            throws SQLException {
        if ( loadedPMeridiansMap.containsKey( pmId ) )
            return loadedPMeridiansMap.get( pmId );

        PrimeMeridian pm = null;
        PreparedStatement prepSt = conn.prepareStatement( "SELECT unit, longitude FROM " + "prime_meridian WHERE id = "
                                                          + pmId );
        ResultSet rs = prepSt.executeQuery();
        if ( !rs.next() )
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NO_ELEMENT", "primeMeridian", pmId ) );
        Unit units = Unit.createUnitFromString( rs.getString( 1 ) );
        CRSIdentifiable identifiable = getIdentifiableAttributes( pmId );
        pm = new PrimeMeridian( units, rs.getDouble( 2 ), identifiable );
        loadedPMeridiansMap.put( pmId, pm );

        return pm;
    }

    /**
     * Returns the ellipsoid identified by its internal database ID
     * 
     * @param ellipsoidId
     *            the internal database ID of the ellipsoid
     * @return the Ellipsoid object
     * @throws SQLException
     */
    protected Ellipsoid getEllipsoid( int ellipsoidId )
                            throws SQLException {
        if ( loadedEllipsoidsMap.containsKey( ellipsoidId ) )
            return loadedEllipsoidsMap.get( ellipsoidId );

        Ellipsoid e = null;
        PreparedStatement prepSt = conn.prepareStatement( "SELECT semi_major_axis, eccentricity, "
                                                          + "inverse_flattening, semi_minor_axis, "
                                                          + "unit FROM ellipsoid WHERE id = " + ellipsoidId );
        ResultSet rs = prepSt.executeQuery();
        if ( !rs.next() )
            throw new CRSConfigurationException(
                                                 Messages.getMessage( "CRS_CONFIG_NO_ELEMENT", "ellipsoid", ellipsoidId ) );

        Unit units = Unit.createUnitFromString( rs.getString( 5 ) );

        double semiMajorAxis = rs.getDouble( 1 );
        double eccentricity = rs.getDouble( 2 );
        double inverseFlattening = rs.getDouble( 3 );
        double semiMinorAxis = rs.getDouble( 4 );

        CRSIdentifiable identifiable = getIdentifiableAttributes( ellipsoidId );
        if ( !Double.isNaN( inverseFlattening ) )
            e = new Ellipsoid( semiMajorAxis, units, inverseFlattening, identifiable );
        else if ( !Double.isNaN( eccentricity ) )
            e = new Ellipsoid( semiMajorAxis, eccentricity, units, identifiable );
        else if ( !Double.isNaN( semiMinorAxis ) )
            e = new Ellipsoid( units, semiMajorAxis, semiMinorAxis, identifiable );
        else
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_ELLIPSOID_MISSES_PARAM", ellipsoidId ) );
        loadedEllipsoidsMap.put( ellipsoidId, e );
        return e;
    }

    /**
     * Returns the Axis identified by a provided internal database ID
     * 
     * @param axisId
     *            the internal database ID for the Axis
     * @return the Axis object
     * @throws SQLException
     */
    protected Axis getAxis( int axisId )
                            throws SQLException {
        if ( loadedAxesMap.containsKey( axisId ) )
            return loadedAxesMap.get( axisId );

        Axis newAxis = null;
        PreparedStatement prepSt = conn.prepareStatement( "SELECT name, units, axis_orientation "
                                                          + "FROM axis WHERE id = " + axisId );
        ResultSet rs = prepSt.executeQuery();
        rs.next();

        Unit units = Unit.createUnitFromString( rs.getString( 2 ) );
        String axisName = rs.getString( 1 );
        String axisOrientation = rs.getString( 3 );

        newAxis = new Axis( units, axisName, axisOrientation );
        loadedAxesMap.put( axisId, newAxis );
        return newAxis;
    }

    /**
     * Method used by the EPSGDatabaseSynchronizer class for giving codes to Axis objects that have none in the in-house
     * database ( which are taken from the EPSG database)
     * 
     * @param name
     * @param orientation
     * @param uom
     * @param code
     * @throws SQLException
     */
    protected void changeAxisCode( String name, String orientation, Unit uom, CRSCodeType code )
                            throws SQLException {
        PreparedStatement ps = conn.prepareStatement( "SELECT id, name, units, axis_orientation " + "FROM axis" );
        ResultSet rs = ps.executeQuery();

        while ( rs.next() ) {
            if ( name.equals( rs.getString( 2 ) ) && orientation.equals( rs.getString( 4 ) )
                 && uom.equals( Unit.createUnitFromString( rs.getString( 3 ) ) ) ) {
                setCode( rs.getInt( 1 ), code.getCode() );
            }
        }
    }

    /**
     * 
     */
    public CRSQuerier() {
        conn = null; // initialize connection that can be checked afterwards with connectionAlreadySet()
    }

    /**
     * Returns a list of all available CoordinateSystems in the database
     * 
     * @return a list of all available CoordinateSystems in the database
     * @throws SQLException
     */
    protected List<CoordinateSystem> getAvailableCRSs()
                            throws SQLException {
        List<CoordinateSystem> CRSlist = new LinkedList<CoordinateSystem>();

        List<CRSCodeType> crsCodes = getAvailableCRSCodes();
        int nCRSCodes = crsCodes.size();
        long time1 = System.currentTimeMillis();
        System.out.println( "Loading " + nCRSCodes + " CRSs..." );
        int percent = 0;
        for ( int i = 0; i < nCRSCodes; i++ ) {
            if ( ( i * 100 ) / nCRSCodes > percent ) {
                percent = ( i * 100 ) / nCRSCodes;
                System.out.println( percent + "%" );
            }
            CRSlist.add( getCRSByCode( crsCodes.get( i ) ) );
        }
        System.out.println( "Complete! ( loading time: " + ( System.currentTimeMillis() - time1 ) + " ms )" );
        // let other methods know that the cache contains all the CRSs, so not to query them again in the database

        return CRSlist;
    }

    /**
     * Constructs the CRSIdentifiable object that stands at the core of every CRS object.
     * 
     * @param id
     *            the internal database id of the object whose attributes are filled in
     * @return the CRSIdentifiable object
     * @throws SQLException
     */
    protected CRSIdentifiable getIdentifiableAttributes( int id )
                            throws SQLException {

        List<CRSCodeType> codes = new ArrayList<CRSCodeType>();
        PreparedStatement ps = conn.prepareStatement( "SELECT original FROM code WHERE ref_id = " + id );
        ResultSet rs = ps.executeQuery();
        while ( rs.next() ) {
            codes.add( new CRSCodeType( rs.getString( 1 ) ) );
        }
        ps.close();

        List<String> names = new ArrayList<String>();
        ps = conn.prepareStatement( "SELECT name FROM name WHERE ref_id = " + id );
        rs = ps.executeQuery();
        while ( rs.next() ) {
            if ( rs.getString( 1 ) != null ) {
                names.add( rs.getString( 1 ) );
            }
        }
        ps.close();

        List<String> versions = new ArrayList<String>();
        ps = conn.prepareStatement( "SELECT version FROM version WHERE ref_id = " + id );
        rs = ps.executeQuery();
        while ( rs.next() ) {
            if ( rs.getString( 1 ) != null ) {
                versions.add( rs.getString( 1 ) );
            }
        }
        ps.close();

        List<String> descriptions = new ArrayList<String>();
        ps = conn.prepareStatement( "SELECT description FROM description WHERE ref_id = " + id );
        rs = ps.executeQuery();
        while ( rs.next() ) {
            if ( rs.getString( 1 ) != null ) {
                descriptions.add( rs.getString( 1 ) );
            }
        }
        ps.close();

        List<String> areas = new ArrayList<String>();
        ps = conn.prepareStatement( "SELECT area_of_use FROM area WHERE ref_id = " + id );
        rs = ps.executeQuery();
        while ( rs.next() ) {
            if ( rs.getString( 1 ) != null ) {
                areas.add( rs.getString( 1 ) );
            }
        }
        ps.close();

        return new CRSIdentifiable( codes.toArray( new CRSCodeType[codes.size()] ),
                                    names.toArray( new String[names.size()] ),
                                    versions.toArray( new String[versions.size()] ),
                                    descriptions.toArray( new String[descriptions.size()] ),
                                    areas.toArray( new String[areas.size()] ) );
    }

    /**
     * Gets the CompoundCRS identified by the supplied internal ID
     * 
     * @param compoundId
     *            an internal ID
     * @return the CompoundCRS object
     * @throws SQLException
     */
    protected CompoundCRS getCompoundCRS( int compoundId )
                            throws SQLException {
        if ( loadedCompoundsMap.containsKey( compoundId ) )
            return loadedCompoundsMap.get( compoundId );

        CompoundCRS compound_crs = null;
        PreparedStatement ps = conn.prepareStatement( "SELECT base_crs, height_axis_id, default_height,"
                                                      + " id FROM compound_crs WHERE id = " + compoundId );
        ResultSet rs = ps.executeQuery();
        rs.next();

        CRSIdentifiable identifiable = getIdentifiableAttributes( compoundId );
        compound_crs = new CompoundCRS( getAxis( rs.getInt( 2 ) ), getCRSByInternalID( rs.getInt( 1 ) ),
                                        rs.getDouble( 3 ), identifiable );
        loadedCompoundsMap.put( compoundId, compound_crs );
        return compound_crs;
    }

    /**
     * Method currently used by getCompoundCRS() to retrieve the underlying CRS, given an internal database ID
     * 
     * @param internalID
     *            the internal database ID
     * @return the Coordinate System object
     * @throws SQLException
     */
    protected CoordinateSystem getCRSByInternalID( int internalID )
                            throws SQLException {
        // TODO this gets called only from getCompoundCRS, which itself is rarely called - does it make sense to have a
        // map { internalID -> CoordinateSystem } ?
        CoordinateSystem result = null;

        // query the database
        PreparedStatement ps = conn.prepareStatement( "SELECT table_name FROM crs_lookup WHERE id = " + internalID );
        ResultSet rs = ps.executeQuery();
        rs.next();
        String table = rs.getString( 1 );

        if ( table.equals( "projected_crs" ) )
            result = getProjectedCRS( internalID );
        else if ( table.equals( "geographic_crs" ) )
            result = getGeographicCRS( internalID );
        else if ( table.equals( "geocentric_crs" ) )
            result = getGeocentricCRS( internalID );
        else if ( table.equals( "compound_crs" ) )
            result = getCompoundCRS( internalID );
        return result;
    }

    /**
     * Gets the GeocentricCRS identified by the supplied internal ID
     * 
     * @param geocentricId
     *            an internal ID
     * @return the GeocentricCRS object
     * @throws SQLException
     */
    protected GeocentricCRS getGeocentricCRS( int geocentricId )
                            throws SQLException {
        if ( loadedGeocentricsMap.containsKey( geocentricId ) )
            return loadedGeocentricsMap.get( geocentricId );

        GeocentricCRS geocentric_crs = null;
        PreparedStatement ps = conn.prepareStatement( "SELECT axis1_id, axis2_id, axis3_id,"
                                                      + " datum_id, id FROM geocentric_crs " + "WHERE id = "
                                                      + geocentricId );
        ResultSet rs = ps.executeQuery();
        rs.next();

        CRSIdentifiable identifiable = getIdentifiableAttributes( rs.getInt( 5 ) );
        geocentric_crs = new GeocentricCRS( getGeodeticDatum( rs.getInt( 4 ) ),
                                            new Axis[] { getAxis( rs.getInt( 1 ) ), getAxis( rs.getInt( 2 ) ),
                                                        getAxis( rs.getInt( 3 ) ) }, identifiable );
        loadedGeocentricsMap.put( geocentricId, geocentric_crs );
        return geocentric_crs;
    }

    /**
     * Gets the ProjectedCRS identified by the supplied internal ID
     * 
     * @param projectedId
     *            an internal ID
     * @return the ProjectedCRS object
     * @throws SQLException
     */
    protected ProjectedCRS getProjectedCRS( int projectedId )
                            throws SQLException {
        if ( loadedProjectedsMap.containsKey( projectedId ) )
            return loadedProjectedsMap.get( projectedId );

        ProjectedCRS proj_crs = null;
        PreparedStatement ps = conn.prepareStatement( "SELECT axis1_id, axis2_id,"
                                                      + " projection_id FROM projected_crs " + "WHERE id = "
                                                      + projectedId );
        ResultSet rs = ps.executeQuery();
        rs.next();

        CRSIdentifiable identifiable = getIdentifiableAttributes( projectedId );
        int projectionID = rs.getInt( 3 );
        Projection proj = getProjection( projectionID );
        if ( proj == null || proj.getGeographicCRS() == null
             || proj.getGeographicCRS().getType() != CoordinateSystem.GEOGRAPHIC_CRS ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJECTEDCRS_FALSE_CRSREF",
                                                                      identifiable.getCode().toString(),
                                                                      getProjection( projectionID ) ) );
        }

        proj_crs = new ProjectedCRS( getProjection( rs.getInt( 3 ) ), new Axis[] { getAxis( rs.getInt( 1 ) ),
                                                                                  getAxis( rs.getInt( 2 ) ) },
                                     identifiable );
        loadedProjectedsMap.put( projectedId, proj_crs );
        return proj_crs;
    }

    /**
     * Gets the codes of all CoordinateSystems
     * 
     * @return a list with CRSCodeType elements
     */
    protected List<CRSCodeType> getAvailableCRSCodes() {
        List<CRSCodeType> listCodes = new ArrayList<CRSCodeType>();

        try {
            PreparedStatement ps = conn.prepareStatement( "SELECT original " + "FROM crs_lookup JOIN code ON"
                                                          + " crs_lookup.id = code.ref_id" );
            ResultSet rs = ps.executeQuery();
            while ( rs.next() ) {
                listCodes.add( new CRSCodeType( rs.getString( 1 ) ) );
            }
        } catch ( SQLException e ) {
            LOG.error( e.getMessage() );
        }

        return listCodes;
    }

    /**
     * Gets the CoordinateSystem object by supplying the code as a String
     * 
     * @param crsCode
     *            the code as a String
     * @return the CoordinateSystem object
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    protected CoordinateSystem getCRSByCode( CRSCodeType crsCode )
                            throws IllegalArgumentException, SQLException {
        CRSIdentifiable result = cachedCRSs.get( crsCode );
        if ( result == null ) {
            result = getNotCachedCRS( crsCode );
            return (CoordinateSystem) result;
        }
        if ( result instanceof CoordinateSystem ) {
            return (CoordinateSystem) result;
        }

        LOG.warn( "The retrieved crs " + crsCode + " is not a CRS!" );
        return null;
    }

    /**
     * Gets the CoordinateSystem object by supplying the code as a CRSCodeType and then caches it
     * 
     * @param crsCode
     *            the code as a CRSCodeType
     * @return the CoordinateSystem object
     * @throws SQLException
     */
    protected CoordinateSystem getNotCachedCRS( CRSCodeType crsCode )
                            throws SQLException {
        CoordinateSystem result = null;

        PreparedStatement ps = conn.prepareStatement( "SELECT ref_id FROM code WHERE UPPER(original) = UPPER('"
                                                      + crsCode.getOriginal() + "')" );

        ResultSet rs = ps.executeQuery();
        if ( !rs.next() ) {
            LOG.warn( "The database does not contain a crs with the code " + crsCode.getOriginal() );
            return null;
        }

        int internalID = rs.getInt( 1 );

        ps = conn.prepareStatement( "SELECT table_name FROM crs_lookup WHERE id = " + internalID );
        rs = ps.executeQuery();
        rs.next();

        String table = rs.getString( 1 );
        if ( table.equals( "projected_crs" ) )
            result = getProjectedCRS( internalID );
        else if ( table.equals( "geographic_crs" ) )
            result = getGeographicCRS( internalID );
        else if ( table.equals( "geocentric_crs" ) )
            result = getGeocentricCRS( internalID );
        else if ( table.equals( "compound_crs" ) )
            result = getCompoundCRS( internalID );

        cachedCRSs.put( crsCode, result );
        return result;
    }

    /**
     * Gets the Identifiable object by supplying the code as a String
     * 
     * @param code
     *            the code as a String
     * @return the Identifiable object
     * @throws SQLException
     */
    protected CRSIdentifiable getIdentifiable( CRSCodeType code )
                            throws SQLException {
        if ( cachedCRSs.containsKey( code ) ) // if it is a CRS that has been cached
            return cachedCRSs.get( code );

        ResultSet crsType = conn.prepareStatement(
                                                   "SELECT table_name, id FROM identifiable_lookup, code"
                                                                           + " WHERE id = ref_id AND code = '"
                                                                           + code.getCode() + "' AND codespace = '"
                                                                           + code.getCodeSpace() + "'" ).executeQuery();
        if ( crsType.getString( 1 ).equalsIgnoreCase( "projected_crs" ) )
            return getProjectedCRS( crsType.getInt( 2 ) );
        else if ( crsType.getString( 1 ).equalsIgnoreCase( "geographic_crs" ) )
            return getGeographicCRS( crsType.getInt( 2 ) );
        else if ( crsType.getString( 1 ).equalsIgnoreCase( "geocentric_crs" ) )
            return getGeocentricCRS( crsType.getInt( 2 ) );
        else if ( crsType.getString( 1 ).equalsIgnoreCase( "compound_crs" ) )
            return getCompoundCRS( crsType.getInt( 2 ) );
        else if ( crsType.getString( 1 ).equalsIgnoreCase( "vertical_crs" ) )
            return getVerticalCRS( crsType.getInt( 2 ) );
        else if ( crsType.getString( 1 ).equalsIgnoreCase( "geodetic_datum" ) )
            return getGeodeticDatum( crsType.getInt( 2 ) );
        else if ( crsType.getString( 1 ).equalsIgnoreCase( "ellipsoid" ) )
            return getEllipsoid( crsType.getInt( 2 ) );
        else if ( crsType.getString( 1 ).equalsIgnoreCase( "prime_meridian" ) )
            return getPrimeMeridian( crsType.getInt( 2 ) );
        else if ( crsType.getString( 1 ).equalsIgnoreCase( "axis" ) )
            return getAxis( crsType.getInt( 2 ) );
        else if ( crsType.getString( 1 ).equalsIgnoreCase( "transverse_mercator" ) )
            return getTransverseMercator( crsType.getInt( 2 ) );
        else if ( crsType.getString( 1 ).equalsIgnoreCase( "lambert_conformal_conic" ) )
            return getLambertConformalConic( crsType.getInt( 2 ) );
        else if ( crsType.getString( 1 ).equalsIgnoreCase( "lambert_azimuthal_equal_area" ) )
            return getLambertAzimuthalEqualArea( crsType.getInt( 2 ) );
        else if ( crsType.getString( 1 ).equalsIgnoreCase( "stereographic_alternative" ) )
            return getStereographicAlternative( crsType.getInt( 2 ) );
        else if ( crsType.getString( 1 ).equalsIgnoreCase( "stereographic_azimuthal" ) )
            return getStereographicAzimuthal( crsType.getInt( 2 ) );
        else if ( crsType.getString( 1 ).equalsIgnoreCase( "custom_projection" ) )
            return getStereographicAlternative( crsType.getInt( 2 ) );
        else if ( crsType.getString( 1 ).equalsIgnoreCase( "helmert_transformation" ) )
            return getStereographicAlternative( crsType.getInt( 2 ) );
        else
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NO_ID", code.toString() ) );
    }

    /**
     * Gets the connection data that was establised in the DatabaseCRSProvider
     * 
     * @param connection
     *            Connection
     */
    protected void setConnection( Connection connection ) {
        conn = connection;
    }

    /**
     * Checks to see if the connection has been passed from the db provider already or not
     * 
     * @return whether the connection is already set (true or false)
     */
    protected boolean connectionAlreadySet() {
        return ( conn != null );
    }

    /**
     * Command-line tool to output a CRS from the database. Only the CRS id needs to be provided. The result will be
     * shown in XML format.
     * 
     * @param args
     */
    static public void main( String[] args ) {
        if ( args == null || args.length == 0 ) {
            LOG.info( "No CRS id provided. No CRS is shown." );
            return;
        }

        DatabaseCRSProvider dbProvider = (DatabaseCRSProvider) CRSConfiguration.getCRSConfiguration(
                                                                                                     "org.deegree.crs.configuration.deegree.db.DatabaseCRSProvider" ).getProvider();
        CoordinateSystem crs = dbProvider.getCRSByCode( new CRSCodeType( args[0] ) );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter( out );

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty( "javax.xml.stream.isRepairingNamespaces", Boolean.TRUE );

        StringBuilder sb = new StringBuilder();

        try {
            XMLStreamWriter xmlWriter = new FormattingXMLStreamWriter( factory.createXMLStreamWriter( writer ) );
            List<CoordinateSystem> crsList = new ArrayList<CoordinateSystem>();
            crsList.add( crs );

            CRSExporter exporter = new CRSExporter( null );
            exporter.export( crsList, xmlWriter );

            sb.append( out.toString( Charset.defaultCharset().displayName() ) );

        } catch ( XMLStreamException e ) {
            LOG.error( "Error while exporting the coordinates: " + e.getLocalizedMessage(), e );
        } catch ( UnsupportedEncodingException e ) {
            LOG.error( e.getLocalizedMessage(), e );
        }

        System.out.println( sb );
    }

}
