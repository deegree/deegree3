//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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

package org.deegree.model.crs.configuration.deegree.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.vecmath.Point2d;

import org.deegree.model.crs.CRSCodeType;
import org.deegree.model.crs.CRSIdentifiable;
import org.deegree.model.crs.components.Axis;
import org.deegree.model.crs.components.Ellipsoid;
import org.deegree.model.crs.components.GeodeticDatum;
import org.deegree.model.crs.components.PrimeMeridian;
import org.deegree.model.crs.components.VerticalDatum;
import org.deegree.model.crs.coordinatesystems.CompoundCRS;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.crs.coordinatesystems.GeocentricCRS;
import org.deegree.model.crs.coordinatesystems.GeographicCRS;
import org.deegree.model.crs.coordinatesystems.ProjectedCRS;
import org.deegree.model.crs.coordinatesystems.VerticalCRS;
import org.deegree.model.crs.exceptions.CRSExportingException;
import org.deegree.model.crs.projections.Projection;
import org.deegree.model.crs.projections.azimuthal.LambertAzimuthalEqualArea;
import org.deegree.model.crs.projections.azimuthal.StereographicAlternative;
import org.deegree.model.crs.projections.azimuthal.StereographicAzimuthal;
import org.deegree.model.crs.projections.conic.LambertConformalConic;
import org.deegree.model.crs.projections.cylindric.TransverseMercator;
import org.deegree.model.crs.transformations.helmert.Helmert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>CRSToDBExporter</code> class fills the database with the CRS data provided. Run with 
 * the parameters:
 * <ol>
 * <li> provider class name ( default: org.deegree.model.crs.configuration.deegree.DeegreeCRSProvider ) </li>
 * <li> jdbc driver name ( default: org.apache.derby.jdbc.EmbeddedDriver ) </li>
 * <li> database path name ( default: /home/ionita/DerbyDB/CRS ) </li>
 * </ol>
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author: aionita $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class CRSExporter {

    private Logger LOG = LoggerFactory.getLogger( CRSExporter.class );

    /**
     * For now, the internal database ID is an incrementing variable. 
     */
    private int internalID = 1;

    private Connection conn = null;

    /**
     * Cache the Identifiable objects that are introduced into the database so that their assigned internal ID can be retrieved, and to not duplicate entries
     */
    private Map<CRSIdentifiable, Integer> cachedIdentifiables = null;

    // count number of CRSs of each type to display when the exporting process finishes
    private int nGeographic = 0, nGeocentric = 0, nCompound = 0, nProjected = 0, nVertical = 0;

    public CRSExporter( Properties properties ) {
        // nothing necessary yet
    }

    public CRSExporter() {
        // nothing necessary yet
    }

    protected void setConnection( Connection connection) throws ClassNotFoundException {
        conn = connection;  
    }

    /**
     * Export to database the supplied list of CoordinateSystems 
     * @param crsList
     *          the CoordinateSystems as a List
     * @throws SQLException 
     */
    public void export( List<CoordinateSystem> crsList ) {
        if ( conn == null ) {
            throw new CRSExportingException( "The connection to the database has not been passed to CRSExporter. Try calling setConnection( Connection ) before exporting." );
        }

        if ( crsList == null || crsList.size() == 0 ) {
            LOG.info( "Available CRS list is null or empty! Nothing to export." );
            return;
        }

        cachedIdentifiables = new HashMap<CRSIdentifiable, Integer>();
        for ( CoordinateSystem crs : crsList ) {
            if ( crs != null )
                export( crs );
            else
                LOG.warn( "Found a null CRS in the list. How did that get there?" );
        }
    }    

    /**
     * Export the supplied CoordinateSystem
     * @param crs
     * @return
     *          the internal database id that was assigned to the supplied CoordinateSystem
     * @throws SQLException 
     */
    protected int export( CoordinateSystem crs ) {        
        if ( crs.getType() == CoordinateSystem.COMPOUND_CRS ) {            
            nCompound++; // for statistic purposes
            if ( nCompound % 100 == 0 )
                LOG.info( "Already " +  nCompound + " Compound CRS's exported." );
            return export( (CompoundCRS) crs );
        }
        if ( crs.getType() == CoordinateSystem.GEOCENTRIC_CRS ) {                        
            nGeocentric++; // for statistic purposes
            if ( nGeocentric % 100 == 0 )
                LOG.info( "Already " + nGeocentric + " Geocentric CRS's exported." );
            return export( (GeocentricCRS) crs );
        }
        if ( crs.getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {            
            nGeographic++; // for statistic purposes
            if ( nGeographic % 100 == 0 )
                LOG.info( "Already " + nGeographic + " Geographic CRS's exported." );
            return export( (GeographicCRS) crs );
        }
        if ( crs.getType() == CoordinateSystem.PROJECTED_CRS ) {
            nProjected++; // for statistic purposes
            if ( nProjected % 100 == 0 )
                LOG.info( "Already " + nProjected + " Projected CRS's exported." );            
            return export( (ProjectedCRS) crs );
        }
        if ( crs.getType() == CoordinateSystem.VERTICAL_CRS ) {
            nVertical++; // for statistic purposes
            if ( nVertical % 100 == 0 )
                LOG.info( "Already " + nVertical + " Vertical CRS's exported." );            
            return export( (VerticalCRS) crs );
        }

        // this should not be reached, but if the caller is export( List<CoordinateSystem> ) then it is harmless, since the -1 returned is not used; 
        // if the caller is export( CompoundCRS ) then we will check there for a valid id returned.
        return -1;
    }

    /**
     * Insert into the database the core Identifiable properties of an object
     * @param crsObject
     *          any CRS object 
     * @throws SQLException
     */
    protected void exportIdentifiableProperties( CRSIdentifiable crsObject ) {
        PreparedStatement preparedSt;

        try {
            // insert into the CODE table
            CRSCodeType[] codes = crsObject.getCodes();
            int nCodes = codes.length;
            Set<String> insertedCodes = new HashSet<String>(); 
            Set<String> insertedCodeSpaces = new HashSet<String>();
            for ( int i = 0; i < nCodes; i++ ) {
                if ( !( insertedCodes.contains( codes[i].getCode() ) && 
                                        insertedCodeSpaces.contains( codes[i].getCodeSpace() ) ) ) {

                    preparedSt = conn.prepareStatement( "INSERT INTO code VALUES ( ?, ?, ?)" );
                    preparedSt.setInt( 1, internalID );
                    preparedSt.setString( 2, codes[i].getCode() );
                    preparedSt.setString( 3, codes[i].getCodeSpace() );
                    preparedSt.execute();

                    insertedCodes.add( codes[i].getCode() );
                    insertedCodeSpaces.add( codes[i].getCodeSpace() );
                }
            }

            // insert into the VERSION table
            String[] versions = crsObject.getVersions();
            if ( versions != null ) {
                for ( String ver : versions ) {
                    preparedSt = conn.prepareStatement( "INSERT INTO version VALUES (?, ?)" );
                    preparedSt.setInt( 1, internalID );
                    preparedSt.setString( 2, ver );
                    preparedSt.execute();
                }
            }

            // insert into the NAME table
            String[] names = crsObject.getNames();
            if ( names != null ) {
                for ( String n : names ) {
                    preparedSt = conn.prepareStatement( "INSERT INTO name VALUES ( ?, ?)" );
                    preparedSt.setInt( 1, internalID );
                    preparedSt.setString( 2, n );
                    preparedSt.execute();
                }
            }

            // insert into the DESCRIPTION table
            String[] descriptions = crsObject.getDescriptions();
            if ( descriptions != null ) {
                for ( String d : descriptions ) {
                    preparedSt = conn.prepareStatement( "INSERT INTO description VALUES ( ?, ?)" );
                    preparedSt.setInt( 1, internalID );
                    preparedSt.setString( 2, d );
                    preparedSt.execute();
                }
            }

            // insert into the AREA table
            String[] areas = crsObject.getAreasOfUse();
            if ( areas != null ) {
                for ( String a : areas ) {
                    preparedSt = conn.prepareStatement( "INSERT INTO area VALUES ( ?, ?)" );
                    preparedSt.setInt( 1, internalID );
                    preparedSt.setString( 2, a );
                    preparedSt.execute();
                }
            }
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e);
        }
    }

    /**
     * Insert into the database the supplied Compound CRS
     * @param compound
     * @return
     *          the internal database ID assigned to the Compound CRS
     * @throws SQLException
     */
    protected int export( CompoundCRS compound ) {
        if ( cachedIdentifiables != null && cachedIdentifiables.containsKey( compound ) ) {
            return cachedIdentifiables.get( compound );
        }

        try {
            int underlyingCRSID = export( compound.getUnderlyingCRS() );
            if ( underlyingCRSID == -1 ) { // if the crs type is not known
                LOG.error( "The underlying CRS of the following compound crs is of unknown type : " + compound );
                return -1; // this is harmless, since the no one uses the result  
            }
                
            int heightAxisID = export( compound.getHeightAxis() );

            exportIdentifiableProperties( compound );

            // insert into the crs_lookup table
            PreparedStatement ps = conn.prepareStatement( "INSERT INTO crs_lookup VALUES ( ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "compound_crs" );
            ps.execute();

            // insert into the compound_crs table
            ps = conn.prepareStatement( "INSERT INTO compound_crs VALUES ( ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setInt( 2, underlyingCRSID );
            ps.setInt( 3, heightAxisID );
            ps.setDouble( 4, compound.getDefaultHeight() );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e);
        }

        if ( cachedIdentifiables != null )
            cachedIdentifiables.put( compound, internalID );
        return internalID++;        
    }

    /**
     * Insert into the database the Lambert Azimuthal Equal Area projection properties
     * @param lambertAzimuthal
     * @return
     *          the internal database ID assigned to the supplied object
     * @throws SQLException
     */
    protected int export( LambertAzimuthalEqualArea lambertAzimuthal ) {
        if ( cachedIdentifiables != null && cachedIdentifiables.containsKey( lambertAzimuthal ) ) {
            return cachedIdentifiables.get( lambertAzimuthal );
        }

        try {
            int geographicID = export( lambertAzimuthal.getGeographicCRS() );

            exportIdentifiableProperties( lambertAzimuthal );

            // insert into projection_lookup table
            PreparedStatement ps = conn.prepareStatement( "INSERT INTO projection_lookup VALUES (?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "lambert_azimuthal_equal_area" );
            ps.execute();

            // insert into lambert_azimuthal_equal_area table
            ps = conn.prepareStatement( "INSERT INTO lambert_azimuthal_equal_area VALUES (?, ?, ?, ?, ?, ?, ?, ? )" );
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
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e);
        }

        if ( cachedIdentifiables != null )
            cachedIdentifiables.put( lambertAzimuthal, internalID );
        return internalID++;
    }

    /**
     * Inserts into the database the Stereographic Alternative projection properties
     * @param stereographicAl
     * @return
     *          the internal database ID assigned to the object
     * @throws SQLException
     */
    protected int export( StereographicAlternative stereographicAl ) {
        if ( cachedIdentifiables != null && cachedIdentifiables.containsKey( stereographicAl ) ) {
            return cachedIdentifiables.get( stereographicAl );
        }

        int geographicID = export( stereographicAl.getGeographicCRS() );

        try {
            exportIdentifiableProperties( stereographicAl );

            // insert into projection_lookup table
            PreparedStatement ps = conn.prepareStatement( "INSERT INTO projection_lookup VALUES (?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "stereographic_alternative" );
            ps.execute();

            // insert into stereographic_alternative table
            ps = conn.prepareStatement( "INSERT INTO stereographic_alternative VALUES (?, ?, ?, ?, ?, ?, ?, ? )" );
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
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e);
        }

        if ( cachedIdentifiables != null )
            cachedIdentifiables.put( stereographicAl, internalID );
        return internalID++;
    }

    /**
     * Insert into the database the Stereographic Azimuthal projection properties
     * @param stereographicAz
     * @return
     *          the internal database ID assigned to the object
     * @throws SQLException
     */
    protected int export( StereographicAzimuthal stereographicAz ) {
        if ( cachedIdentifiables != null && cachedIdentifiables.containsKey( stereographicAz ) ) {
            return cachedIdentifiables.get( stereographicAz );
        }

        int geographicID = export( stereographicAz.getGeographicCRS() );

        try {
            exportIdentifiableProperties( stereographicAz );

            // insert into projection_lookup table
            PreparedStatement ps = conn.prepareStatement( "INSERT INTO projection_lookup VALUES (?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "stereographic_azimuthal" );
            ps.execute();

            // insert into the stereographic_azimuthal table
            ps = conn.prepareStatement( "INSERT INTO stereographic_azimuthal VALUES (?, ?, ?, ?, ?, ?, ?, ?, ? )" );
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
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e);
        }

        if ( cachedIdentifiables != null )
            cachedIdentifiables.put( stereographicAz, internalID );
        return internalID++;
    }

    /**
     * Insert into the database the Lambert Conformal Conic projection properties
     * @param lambertConformal
     * @return
     *          the internal database ID assigned to the object
     * @throws SQLException
     */
    protected int export( LambertConformalConic lambertConformal ) {
        if ( cachedIdentifiables != null && cachedIdentifiables.containsKey( lambertConformal ) ) {
            return cachedIdentifiables.get( lambertConformal );
        }

        int geographicID = export( lambertConformal.getGeographicCRS() );

        try {
            exportIdentifiableProperties( lambertConformal );

            // insert into projection_lookup table
            PreparedStatement ps = conn.prepareStatement( "INSERT INTO projection_lookup VALUES (?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "lambert_conformal_conic" );
            ps.execute();

            // insert into lambert_conformal_conic table
            ps = conn.prepareStatement( "INSERT INTO lambert_conformal_conic VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ? )" );
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
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e);
        }

        if ( cachedIdentifiables != null )
            cachedIdentifiables.put( lambertConformal, internalID );
        return internalID++;
    }

    /**
     * Checks if the double variable to be inserted in the database is NULL and if so puts NULL for the 
     * java.sql.Type.DOUBLE type. Otherwise simply fill the insert statement with the double variable.  
     * 
     * @param d
     *      the double variable from the insert statement
     * @param pos
     *      the position of the variable in the PreparedStatement from the java.sql
     * @param preparedSt
     *      the PreparedStatement
     * @throws SQLException
     */
    protected void setNullDoubleIf( double d , int pos, PreparedStatement preparedSt ) {
        try {
            if ( Double.isNaN( d ) )
                preparedSt.setNull( pos, java.sql.Types.DOUBLE );
            else
                preparedSt.setDouble( pos, d );
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e);
        }
    }

    /**
     * Inserts the Transverse Mercator projection into the database
     * @param transMercator
     * @return
     *      the internal database ID assigned to the object
     * @throws SQLException
     */
    protected int export( TransverseMercator transMercator ) {
        if ( cachedIdentifiables != null && cachedIdentifiables.containsKey( transMercator ) ) {
            return cachedIdentifiables.get( transMercator );
        }

        int geographicID = export( transMercator.getGeographicCRS() );

        try {
            exportIdentifiableProperties( (CRSIdentifiable) transMercator );


            // insert into projection_lookup table
            PreparedStatement ps = conn.prepareStatement( "INSERT INTO projection_lookup VALUES (?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "transverse_mercator" );
            ps.execute();

            // insert into transverse_mercator
            ps = conn.prepareStatement( "INSERT INTO transverse_mercator VALUES (?, ?, ?, ?, ?, ?, ?, ?, ? )" );
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
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e);
        }

        if ( cachedIdentifiables != null )
            cachedIdentifiables.put( transMercator, internalID );
        return internalID++;
    }

    /**
     * Inserts ProjectedCRS data into the database
     * @param projected
     *          a ProjectedCRS object
     * @return
     *          the internal database ID assigned to the Projected CRS
     * @throws SQLException
     */
    protected int export( ProjectedCRS projected ) {
        if ( cachedIdentifiables != null && cachedIdentifiables.containsKey( projected ) ) {
            return cachedIdentifiables.get( projected );
        }

        try {
            int axisID1 = export( projected.getAxis()[0] );
            int axisID2 = export( projected.getAxis()[1] );
            int projectionID = export( projected.getProjection() );

            exportIdentifiableProperties( projected );

            // insert into the crs_lookup table
            PreparedStatement ps = conn.prepareStatement( "INSERT INTO crs_lookup VALUES ( ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "projected_crs" );
            ps.execute();

            // insert projected CRS - specific information            
            ps = conn.prepareStatement( "INSERT INTO projected_crs VALUES ( ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setInt(2, axisID1 );
            ps.setInt(3, axisID2 );
            ps.setInt( 4, projectionID );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( e.getMessage() );
        }

        if ( cachedIdentifiables != null )
            cachedIdentifiables.put( projected, internalID );
        return internalID++;
    }

    /**
     * Checks for the type of projection that is supplied and delegates the insertion to the specific methods 
     * @param projection
     *          Projection object
     * @return
     *          the internal database ID that was assigned to the projection
     * @throws SQLException
     */
    protected int export( Projection projection ) {
        if ( projection instanceof TransverseMercator )
            return export( (TransverseMercator) projection );

        if ( projection instanceof LambertConformalConic )
            return export( (LambertConformalConic) projection );

        if ( projection instanceof LambertAzimuthalEqualArea )
            return export( (LambertAzimuthalEqualArea) projection );

        if ( projection instanceof StereographicAzimuthal )
            return export( (StereographicAzimuthal) projection );

        if ( projection instanceof StereographicAlternative )
            return export( (StereographicAlternative) projection );

        //
        // Export a custom projection
        //
        if ( cachedIdentifiables != null && cachedIdentifiables.containsKey( projection ) ) {
            return cachedIdentifiables.get( projection );
        }

        int geographicID = export( projection.getGeographicCRS() );

        try {
            exportIdentifiableProperties( (CRSIdentifiable) projection );


            // insert into projection_lookup table
            PreparedStatement ps = conn.prepareStatement( "INSERT INTO projection_lookup VALUES (?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "transverse_mercator" );
            ps.execute();

            // insert into transverse_mercator
            ps = conn.prepareStatement( "INSERT INTO custom_projection VALUES (?, ?, ?, ?, ?, ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );

            Point2d p2d = projection.getNaturalOrigin();
            setNullDoubleIf( p2d.y, 2, ps ); // y for latitude (of natural origin)
            setNullDoubleIf( p2d.x, 3, ps ); // x for longitude (of natural origin)
            setNullDoubleIf( projection.getScale(), 4, ps );
            setNullDoubleIf( projection.getFalseEasting(), 5, ps );
            setNullDoubleIf( projection.getFalseNorthing(), 6, ps );

            ps.setInt( 7, geographicID );
            ps.setString( 8, projection.getUnits().getName() );
            ps.setString( 9, projection.getClassName() );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e);
        }

        if ( cachedIdentifiables != null )
            cachedIdentifiables.put( projection, internalID );
        return internalID++;        
    }

    /**
     * Inserts the GeographicCRS data into the database
     * @param geographic
     * @return
     *      the internal database ID for the Geographic CRS  
     * @throws SQLException
     */
    protected int export( GeographicCRS geographic ) {
        if ( cachedIdentifiables != null && cachedIdentifiables.containsKey( geographic ) ) {
            return cachedIdentifiables.get( geographic );
        }

        try {
            int axisID1 = export( geographic.getAxis()[0] );
            int axisID2 = export( geographic.getAxis()[1] );
            int gdatumID = export( geographic.getGeodeticDatum() ); 

            exportIdentifiableProperties( geographic );

            // insert into the crs_lookup table
            PreparedStatement ps = conn.prepareStatement( "INSERT INTO crs_lookup VALUES ( ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "geographic_crs" );
            ps.execute();

            // insert into geographic_crs table
            ps = conn.prepareStatement( "INSERT INTO geographic_crs VALUES ( ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setInt( 2, axisID1 );
            ps.setInt( 3, axisID2 );
            ps.setInt( 4, gdatumID );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( e.getMessage() );
        }

        if ( cachedIdentifiables != null )
            cachedIdentifiables.put( geographic, internalID );        
        return internalID++;
    }

    /**
     * Inserts the Axis properties into the database
     * @param axis
     * @return
     *      the internal database ID assigned to the Axis object
     * @throws SQLException
     */
    protected int export( Axis axis ) {
        if ( cachedIdentifiables != null && cachedIdentifiables.containsKey( axis ) ) {
            return cachedIdentifiables.get( axis );
        }

        try {
            exportIdentifiableProperties( (CRSIdentifiable) axis );
            // insert into axis table
            PreparedStatement ps = conn.prepareStatement( "INSERT INTO axis VALUES ( ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, axis.getName() );
            ps.setString( 3, axis.getUnits().getName().toLowerCase() );
            ps.setString( 4, axis.getOrientationAsString() );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e);
        }

        if ( cachedIdentifiables != null )
            cachedIdentifiables.put( axis, internalID );
        return internalID++;
    }

    /**
     * Insert the Geocentric CRS data into the database 
     * @param geocentric
     * @return
     *          the database internal ID assigned to the Geocentric CRS 
     * @throws SQLException
     */
    protected int export( GeocentricCRS geocentric ) {
        if ( cachedIdentifiables != null && cachedIdentifiables.containsKey( geocentric ) ) {
            return cachedIdentifiables.get( geocentric );
        }

        try {
            int axisID1 = export( geocentric.getAxis()[0] );
            int axisID2 = export( geocentric.getAxis()[1] );
            int axisID3 = export( geocentric.getAxis()[2] );
            int gdatumID = export( geocentric.getGeodeticDatum() );

            exportIdentifiableProperties( geocentric );

            // insert into the crs_lookup table
            PreparedStatement ps = conn.prepareStatement( "INSERT INTO crs_lookup VALUES ( ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "geocentric_crs" );
            ps.execute();

            // insert into the geocentric_crs lookup table
            ps = conn.prepareStatement( "INSERT INTO geocentric_crs VALUES ( ?, ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setInt( 2, axisID1 );
            ps.setInt( 3, axisID2 );
            ps.setInt( 4, axisID3 );
            ps.setInt( 5, gdatumID );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( e.getMessage() );
        }

        if ( cachedIdentifiables != null )
            cachedIdentifiables.put( geocentric, internalID );
        return internalID++;
    }

    /**
     * Insert the Vertical CRS data into the database 
     * @param vertical
     * @return
     *          the database internal ID assigned to the Vertical CRS 
     * @throws SQLException
     */
    protected int export( VerticalCRS vertical ) {
        if ( cachedIdentifiables != null && cachedIdentifiables.containsKey( vertical ) ) {
            return cachedIdentifiables.get( vertical );
        }

        try {
            int axisID = export( vertical.getAxis()[0] );
            int verticalDatumID = export( (VerticalDatum) vertical.getDatum() );

            exportIdentifiableProperties( vertical );

            // insert into the crs_lookup table
            PreparedStatement ps = conn.prepareStatement( "INSERT INTO crs_lookup VALUES ( ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "vertical_crs" );
            ps.execute();

            // insert into the geocentric_crs lookup table
            ps = conn.prepareStatement( "INSERT INTO vertical_crs VALUES ( ?, ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setInt( 2, axisID );
            ps.setInt( 3, verticalDatumID );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e);
        }

        if ( cachedIdentifiables != null )
            cachedIdentifiables.put( vertical, internalID );
        return internalID++;
    }

    /**
     * Export the Vertical Datum to the database
     * @param vDatum
     * @return The internal database ID assigned to the Vertical Datum
     * @throws SQLException
     */
    protected int export( VerticalDatum vDatum ) {
        if ( cachedIdentifiables != null && cachedIdentifiables.containsKey( vDatum ) ) {
            return cachedIdentifiables.get( vDatum );
        }

        try {
            exportIdentifiableProperties( vDatum );

            // insert into the vertical_datum table
            PreparedStatement ps = conn.prepareStatement( "INSERT INTO vertical_datum VALUES ( ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setNull( 2, java.sql.Types.DOUBLE );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e);
        }

        if ( cachedIdentifiables != null )
            cachedIdentifiables.put( vDatum, internalID );
        return internalID++;
    }

    /**
     * Insert the Helmert Transformation properties into the database
     * @param helmert
     * @return
     *          the internal database ID assigned to the Helmert transformation object
     * @throws SQLException
     */
    protected int export( Helmert helmert ) {
        if ( cachedIdentifiables != null && cachedIdentifiables.containsKey( helmert ) ) {
            return cachedIdentifiables.get( helmert );
        }

        try {
            exportIdentifiableProperties( helmert );

            // insert into the transformation_lookup table
            PreparedStatement ps = conn.prepareStatement( "INSERT INTO transformation_lookup VALUES ( ?, ?)" );
            ps.setInt( 1, internalID );
            ps.setString( 2, "helmert_transformation" );
            ps.execute();

            // insert into the helmert table
            ps = conn.prepareStatement( "INSERT INTO helmert_transformation VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setInt( 2, /*crsDBid.get( wgs84.getSourceCRS().getIdentifier() ) */ 42); // currently wgs84.getSourceCRS() is null
            ps.setInt( 3, /*crsDBid.get( wgs84.getTargetCRS().getIdentifier() ) */ 24); // currently wgs84.getTargetCRS() is null
            ps.setDouble( 4, helmert.dx );
            ps.setDouble( 5, helmert.dy );
            ps.setDouble( 6, helmert.dz );
            ps.setDouble( 7, helmert.ex );
            ps.setDouble( 8, helmert.ey );
            ps.setDouble( 9, helmert.ez );
            ps.setDouble( 10, helmert.ppm );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e);
        }

        if ( cachedIdentifiables != null )
            cachedIdentifiables.put( helmert, internalID );
        return internalID++;
    }

    /**
     * Insert the Prime Meridian properties into the database
     * @param pm
     * @return
     *      the internal database ID assigned to the object
     * @throws SQLException
     */
    protected int export( PrimeMeridian pm ) {
        if ( cachedIdentifiables != null && cachedIdentifiables.containsKey( pm ) ) {
            return cachedIdentifiables.get( pm );
        }

        try {
            exportIdentifiableProperties( (CRSIdentifiable) pm );        

            // insert into the prime_meridian table
            PreparedStatement ps = conn.prepareStatement( "INSERT INTO prime_meridian VALUES ( ?, ?, ? )" );
            ps.setInt( 1, internalID );
            ps.setString( 2, pm.getAngularUnit().getName().toLowerCase() );
            ps.setString( 3, Double.toString( pm.getLongitude() ) );
            ps.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e);
        }

        if ( cachedIdentifiables != null )
            cachedIdentifiables.put( pm, internalID );
        return internalID++;
    }

    /**
     * Insert the Ellipsoid object data into the database 
     * @param ellipsoid
     * @return
     *      the internal database ID assigned to the ellipsoid
     * @throws SQLException
     */
    protected int export( Ellipsoid ellipsoid ) {
        if ( cachedIdentifiables != null && cachedIdentifiables.containsKey( ellipsoid ) ) {
            return cachedIdentifiables.get( ellipsoid );
        }

        try {
            exportIdentifiableProperties( (CRSIdentifiable) ellipsoid );

            // insert into ellipsoid table
            PreparedStatement ps = conn.prepareStatement( "INSERT INTO ellipsoid VALUES (?, ?, ?, ?, ?, ? )" );
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
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e);
        }

        if ( cachedIdentifiables != null )
            cachedIdentifiables.put( ellipsoid, internalID );
        return internalID++;
    }


    /**
     * Insert the Geodetic Datum properties into the database
     * @param gdatum
     * @return
     *          the internal database ID assigned to the Geodetic Datum
     * @throws SQLException
     */
    protected int export( GeodeticDatum gdatum ) {
        if ( cachedIdentifiables != null && cachedIdentifiables.containsKey( gdatum ) ) {
            return cachedIdentifiables.get( gdatum );
        }

        try {
            int ellipsoidID = export( gdatum.getEllipsoid() );
            int pmID = export( gdatum.getPrimeMeridian() );
            int helmertID = export( gdatum.getWGS84Conversion() );

            exportIdentifiableProperties( gdatum );


            // insert into the geodetic_datum table
            PreparedStatement preparedSt = conn.prepareStatement( "INSERT INTO geodetic_datum VALUES ( ?, ?, ?, ? )" );
            preparedSt.setInt( 1, internalID );
            preparedSt.setInt( 2, ellipsoidID );   
            preparedSt.setInt( 3, pmID );
            preparedSt.setInt( 4, helmertID ); 
            preparedSt.execute();
        } catch ( SQLException e ) {
            throw new CRSExportingException( "Exporting failed: " + e.getMessage(), e);
        }

        if ( cachedIdentifiables != null )
            cachedIdentifiables.put( gdatum, internalID );
        return internalID++;
    }
}