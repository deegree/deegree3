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

package org.deegree.cs.configuration.deegree.xml.exporters;

import static java.lang.Math.toDegrees;
import static org.deegree.commons.xml.CommonNamespaces.CRSNS;
import static org.deegree.cs.coordinatesystems.CoordinateSystem.CRSType.COMPOUND;
import static org.deegree.cs.coordinatesystems.CoordinateSystem.CRSType.PROJECTED;
import static org.deegree.cs.utilities.ProjectionUtils.EPS11;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.EPSGCode;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.Ellipsoid;
import org.deegree.cs.components.GeodeticDatum;
import org.deegree.cs.components.PrimeMeridian;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.CompoundCRS;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.coordinatesystems.GeocentricCRS;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ProjectedCRS;
import org.deegree.cs.coordinatesystems.CoordinateSystem.CRSType;
import org.deegree.cs.projections.Projection;
import org.deegree.cs.projections.azimuthal.LambertAzimuthalEqualArea;
import org.deegree.cs.projections.azimuthal.StereographicAlternative;
import org.deegree.cs.projections.azimuthal.StereographicAzimuthal;
import org.deegree.cs.projections.conic.LambertConformalConic;
import org.deegree.cs.projections.cylindric.Mercator;
import org.deegree.cs.projections.cylindric.TransverseMercator;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.helmert.Helmert;
import org.deegree.cs.transformations.ntv2.NTv2Transformation;
import org.deegree.cs.transformations.polynomial.LeastSquareApproximation;
import org.slf4j.Logger;

/**
 * Exports a list of coordinate systems into the deegree CRS format.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
@LoggingNotes(debug = "Get information about the currently exported coordinate system.")
public class CRSExporter extends CRSExporterBase {

    private static final Logger LOG = getLogger( CRSExporter.class );

    private final String db_id;

    /**
     * @param properties
     */
    public CRSExporter( Properties properties ) {
        super( properties );
        String user = null;
        if ( properties != null ) {
            user = properties.getProperty( "DB_USER" );
            if ( user != null ) {
                String pass = properties.getProperty( "DB_PASSWORD" );
                String con = properties.getProperty( "DB_CONNECTION" );
                db_id = "epsg_db_id";
                ConnectionManager.addConnection( db_id, con, user, pass, 1, 10 );
            } else {
                db_id = null;
            }
        } else {
            db_id = null;
        }

    }

    /**
     * Export the given list of CoordinateSystems into the crs-definition format.
     * 
     * 
     * @param crsToExport
     * @param xmlWriter
     *            to write the definitions to.
     * @throws XMLStreamException
     *             if an error occurred while exporting
     */
    @Override
    public void export( List<CoordinateSystem> crsToExport, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( crsToExport != null ) {
            if ( crsToExport.size() != 0 ) {
                LOG.debug( "Trying to export: " + crsToExport.size() + " coordinate systems." );

                // LinkedList<String> exportedIDs = new LinkedList<String>();
                Set<Ellipsoid> ellipsoids = new TreeSet<Ellipsoid>( new IdComparer() );
                Set<GeodeticDatum> datums = new TreeSet<GeodeticDatum>( new IdComparer() );
                Set<GeocentricCRS> geocentrics = new TreeSet<GeocentricCRS>( new IdComparer() );
                Set<GeographicCRS> geographics = new TreeSet<GeographicCRS>( new IdComparer() );
                Set<ProjectedCRS> projecteds = new TreeSet<ProjectedCRS>( new IdComparer() );
                Set<CompoundCRS> compounds = new TreeSet<CompoundCRS>( new IdComparer() );
                Set<PrimeMeridian> primeMeridians = new TreeSet<PrimeMeridian>( new IdComparer() );
                Set<Transformation> transformations = new TreeSet<Transformation>( new IdComparer() );
                Set<Projection> projections = new TreeSet<Projection>( new IdComparer() );

                for ( CoordinateSystem crs : crsToExport ) {
                    if ( crs != null ) {
                        updateBoundingBox( crs );
                        GeodeticDatum d = (GeodeticDatum) crs.getDatum();
                        updateDatum( crs, d );
                        datums.add( d );
                        ellipsoids.add( d.getEllipsoid() );

                        final CRSType type = crs.getType();
                        switch ( type ) {
                        case COMPOUND:
                            compounds.add( (CompoundCRS) crs );
                            break;
                        case GEOCENTRIC:
                            geocentrics.add( (GeocentricCRS) crs );
                            break;
                        case GEOGRAPHIC:
                            geographics.add( (GeographicCRS) crs );
                            break;
                        case PROJECTED: {
                            final ProjectedCRS proj = (ProjectedCRS) crs;
                            final Projection projection = proj.getProjection();
                            String id = projection.getCode().getOriginal();
                            if ( id != null
                                 && ( id.contains( "Snyder-StereoGraphic" ) || id.contains( "9820" ) /* LaAzEq */
                                      || id.contains( "9809" ) /* StAl */|| id.contains( "9802" ) /* LaCoCo */
                                      || id.contains( "9804" ) /* Merc */|| id.contains( "9807" ) /* tmerc */) ) {
                                // rb: those values were set as 'pseudo ids' in pre 0.3 parser, add projected crs id
                                // try to get it from the epsg database :-)
                                LOG.debug( "Updating projection id: " + id + " because it was black listed." );
                                updateProjectionId( proj, projection );
                            } else {
                                LOG.debug( "Not updating projection id: " + id + " because it was not black listed." );
                            }

                            projecteds.add( proj );
                            projections.add( projection );
                        }
                            break;
                        case VERTICAL:
                            // not yet supported
                            break;
                        }

                        if ( d.getPrimeMeridian() != null ) {
                            PrimeMeridian pm = d.getPrimeMeridian();
                            updatePM( pm );
                            primeMeridians.add( pm );
                        }
                        Helmert h = d.getWGS84Conversion();
                        if ( h != null ) {
                            if ( h.getSourceCRS() == null ) {
                                CoordinateSystem src = crs;
                                if ( src.getType() == COMPOUND ) {
                                    src = ( (CompoundCRS) crs ).getUnderlyingCRS();
                                }
                                if ( src.getType() == PROJECTED ) {
                                    src = ( (ProjectedCRS) crs ).getGeographicCRS();
                                }
                                h.setSourceCRS( src );
                            }
                        }
                        transformations.add( h );
                        if ( crs.getTransformations() != null && !crs.getTransformations().isEmpty() ) {
                            for ( Transformation t : crs.getTransformations() ) {
                                if ( t != null ) {
                                    if ( t.getSourceCRS() == null ) {
                                        t.setSourceCRS( crs );
                                    }
                                    if ( t.getTargetCRS() != null ) {
                                        transformations.add( t );
                                    } else {
                                        LOG.warn( "Transformation: " + t + " has no target crs, this may not be." );
                                    }
                                }
                            }
                        }

                    }
                }

                initDocument( xmlWriter );
                exportProjections( xmlWriter, projections );
                exportTransformations( xmlWriter, transformations );
                exportPrimeMeridians( xmlWriter, primeMeridians );
                exportEllipsoids( xmlWriter, ellipsoids );
                exportDatums( xmlWriter, datums );
                exportCoordinateSystems( xmlWriter, compounds, projecteds, geographics, geocentrics );

                endDocument( xmlWriter );
            } else {
                LOG.warn( "No coordinate system were given (list.size() == 0)." );
            }
        } else {
            LOG.error( "No coordinate system were given (list == null)." );
        }
    }

    /**
     * @param crs
     * @param d
     */
    private void updateDatum( CoordinateSystem crs, GeodeticDatum d ) {
        if ( db_id != null ) {
            int epsgCode = getEPSGCode( d );
            if ( epsgCode == -1 ) {
                CoordinateSystem bCRS = crs;
                if ( crs.getType() == COMPOUND ) {
                    bCRS = ( (CompoundCRS) crs ).getUnderlyingCRS();
                }
                if ( bCRS.getType() == PROJECTED ) {
                    bCRS = ( (ProjectedCRS) bCRS ).getGeographicCRS();
                }

                int crsCode = getEPSGCode( bCRS );
                if ( crsCode != -1 ) {
                    Connection connection = null;
                    try {
                        connection = ConnectionManager.getConnection( db_id );
                        PreparedStatement ps = connection.prepareStatement( "SELECT a.datum_code,b.datum_name,b.remarks,b.revision_date,b.ellipsoid_code,c.ellipsoid_name,c.remarks,c.revision_date,c.semi_major_axis,c.inv_flattening,c.semi_minor_axis,c.uom_code from epsg_coordinatereferencesystem as a JOIN epsg_datum as b ON a.datum_code=b.datum_code JOIN epsg_ellipsoid as c ON b.ellipsoid_code=c.ellipsoid_code where coord_ref_sys_code=?" );
                        ps.setInt( 1, crsCode );
                        ResultSet rs = ps.executeQuery();
                        if ( rs != null && rs.next() ) {
                            // found values
                            int count = 1;
                            int newId = rs.getInt( count++ );
                            if ( newId != 0 ) {
                                String datumName = rs.getString( count++ );

                                byte[] remark = rs.getBytes( count++ );
                                BufferedReader reader = new BufferedReader(
                                                                            new InputStreamReader(
                                                                                                   new ByteArrayInputStream(
                                                                                                                             remark ),
                                                                                                   Charset.forName( "LATIN1" ) ) );

                                String datumRemark = null;
                                try {
                                    datumRemark = reader.readLine();
                                } catch ( IOException e ) {
                                    LOG.warn( "Could not read datum remark keep old description." );
                                } finally {
                                    try {
                                        reader.close();
                                    } catch ( IOException e ) {
                                        // just leave it open.
                                    }
                                }
                                String dVersion = rs.getString( count++ );
                                int eId = rs.getInt( count++ );
                                Ellipsoid ellips = d.getEllipsoid();
                                String ellpsName = rs.getString( count++ );

                                remark = rs.getBytes( count++ );
                                reader = new BufferedReader( new InputStreamReader( new ByteArrayInputStream( remark ),
                                                                                    Charset.forName( "LATIN1" ) ) );

                                String ellpsRemark = null;
                                try {
                                    ellpsRemark = reader.readLine();
                                } catch ( IOException e ) {
                                    LOG.warn( "Could not read ellipsoid remark keep old description." );
                                } finally {
                                    try {
                                        reader.close();
                                    } catch ( IOException e ) {
                                        // just leave it open.
                                    }
                                }
                                String eVersion = rs.getString( count++ );
                                int ellipsId = getEPSGCode( ellips );
                                if ( eId != 0 ) {
                                    d.setDefaultId( new EPSGCode( newId ), true );
                                    d.setDefaultName( datumName, true );
                                    d.setDefaultVersion( dVersion, true );
                                    d.setDefaultDescription( datumRemark, true );
                                    if ( ellipsId != -1 && eId == ellipsId ) {
                                        LOG.debug( "The ellipsoid (" + ellips.getCode() + ") of the datum ("
                                                   + d.getCodeAndName()
                                                   + ") is the same as the ellipsoid in the epsg database (" + eId
                                                   + ")." );

                                    } else {
                                        double a = ellips.getSemiMajorAxis();
                                        double f = ellips.getInverseFlattening();
                                        double b = ellips.getSemiMinorAxis();
                                        double ea = rs.getDouble( count++ );
                                        double ef = rs.getDouble( count++ );
                                        double eb = rs.getDouble( count++ );
                                        int uom = rs.getInt( count++ );
                                        if ( ea != 0 ) {
                                            if ( ef != 0 || eb != 0 ) {
                                                Unit u = Unit.createUnitFromString( "epsg:" + uom );
                                                if ( u == null ) {
                                                    LOG.warn( "Could not determine unit of measure of epsg:" + uom );
                                                } else {
                                                    ea = u.convert( ea, Unit.METRE );
                                                    eb = u.convert( eb, Unit.METRE );
                                                }
                                                boolean otherMatch = ( ef == 0 ) ? ( Math.abs( b - eb ) < 1E-6 )
                                                                                : ( Math.abs( f - ef ) < 1E-6 );
                                                if ( ( Math.abs( a - ea ) < 1E-6 ) && otherMatch ) {
                                                    LOG.info( "The ellipsoid of datum: "
                                                              + d.getCodeAndName()
                                                              + " did not have an epsg code, but the values match, updating ellipsoid epsg code as well." );
                                                    ellips.setDefaultId( new EPSGCode( eId ), true );
                                                    ellips.setDefaultName( ellpsName, true );
                                                    ellips.setDefaultVersion( eVersion, true );
                                                    ellips.setDefaultDescription( ellpsRemark, true );
                                                    d.setDefaultId( new EPSGCode( newId ), true );
                                                    d.setDefaultName( datumName, true );
                                                    d.setDefaultVersion( dVersion, true );
                                                    d.setDefaultDescription( datumRemark, true );
                                                } else {
                                                    LOG.warn( "The ellipsoid (" + ellips.getCode() + ") of the datum ("
                                                              + d.getCodeAndName() + ") is not an epsg ellipsoid." );
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    LOG.warn( "No epsg ellipsoid found for datum (" + d.getCodeAndName() + "." );
                                }
                            } else {
                                LOG.info( "Not updating datum: " + d.getCodeAndName()
                                          + " because no code was found in epsg database." );
                            }
                        } else {
                            LOG.warn( "No epsg id was found for datum: " + d.getCodeAndName() );
                        }
                    } catch ( SQLException e ) {
                        LOG.warn( "Could not update epsg code for datum : " + d.getCodeAndName() + " because: "
                                  + e.getLocalizedMessage() );
                    } finally {
                        if ( connection != null ) {
                            try {
                                connection.close();
                            } catch ( SQLException e ) {
                                LOG.warn( "Could not close stream, of prime meredian just let it open." );
                            }
                        }

                    }
                } else {
                    LOG.warn( "Could not determine epsg code for crs: " + bCRS.getCodeAndName()
                              + " not updating datum: " + d.getCodeAndName() );
                }
            } else {
                LOG.debug( "No need to determine epsg code for datum: " + d.getCodeAndName() );
            }
        }
    }

    /**
     * @param pm
     */
    private void updatePM( PrimeMeridian pm ) {
        if ( db_id != null ) {
            int epsgCode = getEPSGCode( pm );
            if ( epsgCode != -1 ) {
                Connection connection = null;
                try {
                    connection = ConnectionManager.getConnection( db_id );
                    PreparedStatement ps = connection.prepareStatement( "SELECT prime_meridian_name,greenwich_longitude from epsg_primemeridian where prime_meridian_code=?" );
                    ps.setInt( 1, epsgCode );
                    ResultSet rs = ps.executeQuery();
                    if ( rs != null && rs.next() ) {
                        // found values
                        String name = rs.getString( 1 );
                        double lon = rs.getDouble( 2 );
                        if ( Math.abs( lon - pm.getLongitude( Unit.DEGREE ) ) > 1E-4 ) {
                            if ( !pm.hasIdOrName( name, false, false ) ) {
                                pm.addName( name );
                            } else {
                                LOG.debug( "Not updating name of prime meridian, because it already has the name: "
                                           + name );
                            }
                            LOG.debug( "Updating longitude (" + pm.getLongitude( Unit.DEGREE )
                                       + "°) of prime meridian (" + pm.getCodeAndName() + ") to (" + lon
                                       + "°), because it was not consistent with the epsg database." );
                            pm.setLongitude( lon, Unit.DEGREE );
                            // result = new PrimeMeridian( Unit.DEGREE, lon, pm.getCodes(), pm.getNames(),
                            // pm.getVersions(),
                            // pm.getDescriptions(), pm.getAreasOfUse() );
                        } else {
                            LOG.debug( "Not updating pm: " + pm.getCodeAndName()
                                       + " because the longitude is consistent with the epsg database." );
                        }
                    } else {
                        LOG.warn( "No prime meridian was found for id: " + pm.getCodeAndName() );
                    }

                } catch ( SQLException e ) {
                    LOG.warn( "Could not update longitude for prime meridian: " + pm.getCodeAndName() + " because: "
                              + e.getLocalizedMessage() );
                } finally {
                    if ( connection != null ) {
                        try {
                            connection.close();
                        } catch ( SQLException e ) {
                            LOG.warn( "Could not close stream, of prime meredian just let it open." );
                        }
                    }

                }
            } else {
                LOG.warn( "Could not determine epsg code for prime meridian: " + pm.getCodeAndName()
                          + " please check if longitude: " + pm.getLongitude( Unit.DEGREE ) + "° is correct!" );
            }
        }
    }

    private int getEPSGCode( CRSIdentifiable crs ) {
        CRSCodeType[] pCodes = crs.getCodes();
        int epsgCode = -1;
        for ( int i = 0; i < pCodes.length && epsgCode == -1; ++i ) {
            CRSCodeType pCode = pCodes[i];
            if ( "EPSG".equalsIgnoreCase( pCode.getCodeSpace() ) ) {
                try {
                    epsgCode = Integer.parseInt( pCode.getCode() );
                } catch ( NumberFormatException e ) {
                    LOG.warn( "Given epsg code is not an int, ignoring it: " + pCode.getCode() );
                }
            }
        }
        return epsgCode;
    }

    private void updateBoundingBox( CoordinateSystem crs ) {
        if ( db_id != null ) {
            double[] areaOfUseBBox = crs.getAreaOfUseBBox();
            if ( Math.abs( areaOfUseBBox[0] + 180 ) < 1E-8 && Math.abs( areaOfUseBBox[1] + 90 ) < 1E-8
                 && Math.abs( areaOfUseBBox[2] - 180 ) < 1E-8 && Math.abs( areaOfUseBBox[3] - 90 ) < 1E-8 ) {
                int epsgCode = getEPSGCode( crs );
                if ( epsgCode != -1 ) {
                    Connection connection = null;
                    try {
                        connection = ConnectionManager.getConnection( db_id );
                        PreparedStatement ps = connection.prepareStatement( "SELECT b.area_of_use, b.area_west_bound_lon, b.area_south_bound_lat,b.area_east_bound_lon,b.area_north_bound_lat from epsg_coordinatereferencesystem as a JOIN epsg_area as b on a.area_of_use_code=b.area_code where a.coord_ref_sys_code=?" );
                        ps.setInt( 1, epsgCode );
                        ResultSet rs = ps.executeQuery();
                        if ( rs != null && rs.next() ) {
                            // no bbox defined

                            String areaOfUse = rs.getString( 1 );
                            crs.addAreaOfUse( areaOfUse );
                            areaOfUseBBox[0] = rs.getDouble( 2 );
                            areaOfUseBBox[1] = rs.getDouble( 3 );
                            areaOfUseBBox[2] = rs.getDouble( 4 );
                            areaOfUseBBox[3] = rs.getDouble( 5 );
                            crs.setDefaultAreaOfUse( areaOfUseBBox );

                        } else {
                            LOG.warn( "No geographic bbox was found for crs: " + crs.getCodeAndName() );
                        }

                    } catch ( SQLException e ) {
                        LOG.warn( "Could not update area of use for crs: " + crs.getCodeAndName() + " because: "
                                  + e.getLocalizedMessage() );
                    } finally {
                        if ( connection != null ) {
                            try {
                                connection.close();
                            } catch ( SQLException e ) {
                                LOG.warn( "Could not close stream, of domainOfValidity just let it open." );
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * @param proj
     * @param projection
     * @param override
     */
    private void updateProjectionId( ProjectedCRS proj, Projection projection ) {
        CRSCodeType oldCode = proj.getCode();
        CRSCodeType newCodeType = new CRSCodeType( "projection_for_" + oldCode.getOriginal() );
        if ( db_id != null ) {
            int epsgCode = getEPSGCode( proj );
            if ( epsgCode != -1 ) {
                Connection connection = null;
                try {
                    connection = ConnectionManager.getConnection( db_id );
                    PreparedStatement ps = connection.prepareStatement( "select projection_conv_code from epsg_coordinatereferencesystem where coord_ref_sys_code=?" );
                    ps.setInt( 1, epsgCode );
                    ResultSet rs = ps.executeQuery();
                    if ( rs != null && rs.next() ) {
                        int convID = rs.getInt( 1 );
                        if ( convID != 0 ) {
                            newCodeType = new EPSGCode( convID );
                        }
                    } else {
                        LOG.warn( "No conversion code was found for crs: " + proj.getCodeAndName() );
                    }

                    ps = connection.prepareStatement( "select coord_op_name from epsg_coordinatereferencesystem as a JOIN epsg_coordoperation as b ON a.projection_conv_code=b.coord_op_code where coord_ref_sys_code=?" );
                    ps.setInt( 1, epsgCode );
                    rs = ps.executeQuery();
                    if ( rs != null && rs.next() ) {
                        String name = rs.getString( 1 );
                        if ( name != null ) {
                            projection.addName( name );
                        }
                    } else {
                        LOG.warn( "No conversion code was found for crs: " + proj.getCodeAndName() );
                    }

                } catch ( SQLException e ) {
                    LOG.warn( "Could not get conversion / projection code for crs: " + proj.getCodeAndName()
                              + " because: " + e.getLocalizedMessage() );
                } finally {
                    if ( connection != null ) {
                        try {
                            connection.close();
                        } catch ( SQLException e ) {
                            LOG.warn( "Could not close stream, of projection id retrieval just let it open." );
                        }
                    }

                }
            } else {
                LOG.debug( "No epsg code found for projected crs, setting the projected crs value as the default value." );
            }
        } else {
            LOG.debug( "No epsg db configured, setting the projected crs value as the default value." );
        }
        projection.setDefaultId( newCodeType, true );

    }

    /**
     * Exports the given set of ellipsoids
     * 
     * @param xmlWriter
     * @param ellipsoids
     * @throws XMLStreamException
     */
    protected void exportEllipsoids( XMLStreamWriter xmlWriter, Set<Ellipsoid> ellipsoids )
                            throws XMLStreamException {
        writeDefinitionStart( xmlWriter, "EllipsoidDefinitions" );
        // Set<Ellipsoid> sorted = new TreeSet<Ellipsoid>( new IdComparer() );
        // sorted.addAll( ellipsoids );
        for ( Ellipsoid e : ellipsoids ) {
            export( e, xmlWriter );
        }
        xmlWriter.writeEndElement();
    }

    /**
     * Exports the given set of PrimeMeridians
     * 
     * @param xmlWriter
     * @param pms
     * @throws XMLStreamException
     */
    protected void exportPrimeMeridians( XMLStreamWriter xmlWriter, Set<PrimeMeridian> pms )
                            throws XMLStreamException {
        writeDefinitionStart( xmlWriter, "PMDefinitions" );
        for ( PrimeMeridian pm : pms ) {
            export( pm, xmlWriter );
        }
        xmlWriter.writeEndElement();
    }

    /**
     * Exports the given set of Datums
     * 
     * @param xmlWriter
     * @param datums
     * @throws XMLStreamException
     */
    protected void exportDatums( XMLStreamWriter xmlWriter, Set<GeodeticDatum> datums )
                            throws XMLStreamException {
        writeDefinitionStart( xmlWriter, "DatumDefinitions" );
        for ( GeodeticDatum d : datums ) {
            export( d, xmlWriter );
        }
        xmlWriter.writeEndElement();
    }

    /**
     * Exports the given set of transformations
     * 
     * @param xmlWriter
     * @param transformations
     * @throws XMLStreamException
     */
    protected void exportTransformations( XMLStreamWriter xmlWriter, Set<Transformation> transformations )
                            throws XMLStreamException {
        writeDefinitionStart( xmlWriter, "TransformationDefinitions" );
        Set<Helmert> helmert = new TreeSet<Helmert>( new IdComparer() );
        Set<NTv2Transformation> ntv2 = new TreeSet<NTv2Transformation>( new IdComparer() );
        Set<LeastSquareApproximation> ls = new TreeSet<LeastSquareApproximation>( new IdComparer() );
        Set<Transformation> userDefined = new TreeSet<Transformation>( new IdComparer() );
        for ( Transformation t : transformations ) {
            String name = t.getImplementationName();
            if ( "NTv2".equalsIgnoreCase( name ) ) {
                ntv2.add( (NTv2Transformation) t );
            } else if ( "Helmert".equalsIgnoreCase( name ) ) {
                helmert.add( (Helmert) t );
            } else if ( "leastsquare".equalsIgnoreCase( name ) ) {
                ls.add( (LeastSquareApproximation) t );
            } else {
                userDefined.add( t );
            }
        }

        for ( Transformation t : userDefined ) {
            export( t, xmlWriter );
        }
        for ( Transformation t : helmert ) {
            export( t, xmlWriter );
        }

        for ( Transformation t : ntv2 ) {
            export( t, xmlWriter );
        }

        for ( Transformation t : ls ) {
            export( t, xmlWriter );
        }
        xmlWriter.writeEndElement();
    }

    /**
     * Exports the given set of projections
     * 
     * @param xmlWriter
     * @param projections
     * @throws XMLStreamException
     */
    public void exportProjections( XMLStreamWriter xmlWriter, Set<Projection> projections )
                            throws XMLStreamException {
        writeDefinitionStart( xmlWriter, "ProjectionDefinitions" );
        Set<Projection> userDefined = new TreeSet<Projection>( new IdComparer() );
        Set<LambertAzimuthalEqualArea> laea = new TreeSet<LambertAzimuthalEqualArea>( new IdComparer() );
        Set<LambertConformalConic> lcc = new TreeSet<LambertConformalConic>( new IdComparer() );
        Set<StereographicAzimuthal> sa = new TreeSet<StereographicAzimuthal>( new IdComparer() );
        Set<StereographicAlternative> saa = new TreeSet<StereographicAlternative>( new IdComparer() );
        Set<TransverseMercator> tmerc = new TreeSet<TransverseMercator>( new IdComparer() );
        Set<Mercator> merc = new TreeSet<Mercator>( new IdComparer() );

        for ( Projection p : projections ) {
            String implName = p.getImplementationName();
            if ( "LambertAzimuthalEqualArea".equalsIgnoreCase( implName ) ) {
                laea.add( (LambertAzimuthalEqualArea) p );
            } else if ( "lambertConformalConic".equalsIgnoreCase( implName ) ) {
                lcc.add( (LambertConformalConic) p );
            } else if ( "StereographicAzimuthal".equalsIgnoreCase( implName ) ) {
                sa.add( (StereographicAzimuthal) p );
            } else if ( "StereographicAlternative".equalsIgnoreCase( implName ) ) {
                saa.add( (StereographicAlternative) p );
            } else if ( "TransverseMercator".equalsIgnoreCase( implName ) ) {
                tmerc.add( (TransverseMercator) p );
            } else if ( "Mercator".equalsIgnoreCase( implName ) ) {
                merc.add( (Mercator) p );
            } else {
                userDefined.add( p );
            }
        }
        for ( Projection p : userDefined ) {
            export( p, xmlWriter );
        }
        for ( Projection p : laea ) {
            export( p, xmlWriter );
        }
        for ( Projection p : lcc ) {
            export( p, xmlWriter );
        }
        for ( Projection p : sa ) {
            export( p, xmlWriter );
        }
        for ( Projection p : saa ) {
            export( p, xmlWriter );
        }
        for ( Projection p : tmerc ) {
            export( p, xmlWriter );
        }
        for ( Projection p : merc ) {
            export( p, xmlWriter );
        }
        xmlWriter.writeEndElement();
    }

    /**
     * Exports the given sets of CoordinateSystems
     * 
     * @param xmlWriter
     * @param compounds
     * @param projecteds
     * @param geographics
     * @param geocentrics
     * @throws XMLStreamException
     */
    protected void exportCoordinateSystems( XMLStreamWriter xmlWriter, Set<CompoundCRS> compounds,
                                            Set<ProjectedCRS> projecteds, Set<GeographicCRS> geographics,
                                            Set<GeocentricCRS> geocentrics )
                            throws XMLStreamException {
        writeDefinitionStart( xmlWriter, "CRSDefinitions" );
        for ( GeographicCRS geographic : geographics ) {
            export( geographic, xmlWriter );
        }
        for ( ProjectedCRS projected : projecteds ) {
            export( projected, xmlWriter );
        }
        for ( GeocentricCRS geocentric : geocentrics ) {
            export( geocentric, xmlWriter );
        }
        for ( CompoundCRS compound : compounds ) {
            export( compound, xmlWriter );
        }
        xmlWriter.writeEndElement();
    }

    private void writeDefinitionStart( XMLStreamWriter xmlWriter, String name )
                            throws XMLStreamException {
        xmlWriter.writeComment( "Following component definition should be placed in a different file." );
        xmlWriter.writeStartElement( CRSNS, name );
        xmlWriter.writeNamespace( CommonNamespaces.CRS_PREFIX, CommonNamespaces.CRSNS );
        xmlWriter.writeNamespace( CommonNamespaces.XSI_PREFIX, CommonNamespaces.XSINS );
        // xmlWriter.writeAttribute( CommonNamespaces.XSI_PREFIX, CommonNamespaces.XSINS, "schemaLocation",
        // "http://www.deegree.org/crs Y:/WORKSPACE/D3_CORE/RESOURCES/SCHEMA/CRS/0.3.0/crsdefinition.xsd" );
        xmlWriter.writeAttribute( "version", "0.5.0" );
    }

    /**
     * Open an XML document from stream for exporting
     * 
     * @param xmlWriter
     */
    @Override
    protected void initDocument( XMLStreamWriter xmlWriter ) {
        try {
            xmlWriter.writeStartDocument();
            xmlWriter.writeStartElement( CRSNS, "CRSConfiguration" );
            xmlWriter.writeNamespace( CommonNamespaces.CRS_PREFIX, CommonNamespaces.CRSNS );
            xmlWriter.writeNamespace( CommonNamespaces.XSI_PREFIX, CommonNamespaces.XSINS );
            xmlWriter.writeAttribute( CommonNamespaces.XSI_PREFIX, CommonNamespaces.XSINS, "schemaLocation",
                                      "http://www.deegree.org/crs Y:/WORKSPACE/D3_CORE/RESOURCES/SCHEMA/CRS/0.5.0/crsdefinition.xsd" );
            xmlWriter.writeAttribute( "version", "0.5.0" );

            xmlWriter.writeStartElement( CRSNS, "ProjectionsFile" );
            xmlWriter.writeCharacters( "projection-definitions.xml" );
            xmlWriter.writeEndElement();

            xmlWriter.writeStartElement( CRSNS, "TransformationsFile" );
            xmlWriter.writeCharacters( "transformation-definitions.xml" );
            xmlWriter.writeEndElement();

            xmlWriter.writeStartElement( CRSNS, "PrimeMeridiansFile" );
            xmlWriter.writeCharacters( "pm-definitions.xml" );
            xmlWriter.writeEndElement();

            xmlWriter.writeStartElement( CRSNS, "EllispoidsFile" );
            xmlWriter.writeCharacters( "ellipsoid-definitions.xml" );
            xmlWriter.writeEndElement();

            xmlWriter.writeStartElement( CRSNS, "DatumsFile" );
            xmlWriter.writeCharacters( "datum-definitions.xml" );
            xmlWriter.writeEndElement();

            xmlWriter.writeStartElement( CRSNS, "CRSsFile" );
            xmlWriter.writeCharacters( "crs-definitions.xml" );
            xmlWriter.writeEndElement();

        } catch ( XMLStreamException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Write the /crs:defintions and the end document and flush the writer.
     * 
     * @param xmlWriter
     * @throws XMLStreamException
     */
    @Override
    protected void endDocument( XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        xmlWriter.writeEndElement(); // </crs:definitions>
        xmlWriter.writeEndDocument();
        xmlWriter.flush();
    }

    /**
     * Exports the given transformation
     * 
     * @param transformation
     * @param xmlWriter
     * @throws XMLStreamException
     */
    protected void export( Transformation transformation, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( transformation instanceof Helmert ) {
            xmlWriter.writeStartElement( CRSNS, "Helmert" );
        } else if ( transformation instanceof NTv2Transformation ) {
            xmlWriter.writeStartElement( CRSNS, "NTv2" );
        } else if ( transformation instanceof LeastSquareApproximation ) {
            xmlWriter.writeStartElement( CRSNS, "LeastSquare" );
        } else {
            xmlWriter.writeStartElement( CRSNS, "UserDefined" );
            xmlWriter.writeAttribute( "class", transformation.getClass().getCanonicalName() );
        }
        exportIdentifiable( transformation, xmlWriter );
        xmlWriter.writeStartElement( CRSNS, "SourceCRS" );
        xmlWriter.writeCharacters( transformation.getSourceCRS().getCode().getOriginal().toLowerCase() );
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement( CRSNS, "TargetCRS" );
        xmlWriter.writeCharacters( transformation.getTargetCRS().getCode().getOriginal().toLowerCase() );
        xmlWriter.writeEndElement();

        if ( transformation instanceof Helmert ) {
            export( (Helmert) transformation, xmlWriter );
        } else if ( transformation instanceof NTv2Transformation ) {
            export( (NTv2Transformation) transformation, xmlWriter );
        } else if ( transformation instanceof LeastSquareApproximation ) {
            export( (LeastSquareApproximation) transformation, xmlWriter );
        }

        xmlWriter.writeEndElement();// helmert/ntv2/userdefined,leastsqauare
    }

    /**
     * Export the NTv2 to it's appropriate deegree-crs-definitions form.
     * 
     * @param ntv2
     *            to be exported
     * @param xmlWriter
     *            to export to.
     * @throws XMLStreamException
     */
    protected void export( NTv2Transformation ntv2, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( ntv2 != null ) {
            xmlWriter.writeStartElement( CRSNS, "Gridfile" );
            xmlWriter.writeCharacters( ntv2.getGridfile().toExternalForm() );
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export the confInvo to it's appropriate deegree-crs-definitions form.
     * 
     * @param wgs84
     *            to be exported
     * @param xmlWriter
     *            to export to.
     * @throws XMLStreamException
     */
    @Override
    protected void export( Helmert wgs84, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( wgs84 != null ) {
            // xAxisTranslation element
            xmlWriter.writeStartElement( CRSNS, "XAxisTranslation" );
            xmlWriter.writeCharacters( Double.toString( wgs84.dx ) );
            xmlWriter.writeEndElement();
            // yAxisTranslation element
            xmlWriter.writeStartElement( CRSNS, "YAxisTranslation" );
            xmlWriter.writeCharacters( Double.toString( wgs84.dy ) );
            xmlWriter.writeEndElement();
            // zAxisTranslation element
            xmlWriter.writeStartElement( CRSNS, "ZAxisTranslation" );
            xmlWriter.writeCharacters( Double.toString( wgs84.dz ) );
            xmlWriter.writeEndElement();
            // xAxisRotation element
            xmlWriter.writeStartElement( CRSNS, "XAxisRotation" );
            xmlWriter.writeCharacters( Double.toString( wgs84.ex ) );
            xmlWriter.writeEndElement();
            // yAxisRotation element
            xmlWriter.writeStartElement( CRSNS, "YAxisRotation" );
            xmlWriter.writeCharacters( Double.toString( wgs84.ey ) );
            xmlWriter.writeEndElement();
            // zAxisRotation element
            xmlWriter.writeStartElement( CRSNS, "ZAxisRotation" );
            xmlWriter.writeCharacters( Double.toString( wgs84.ez ) );
            xmlWriter.writeEndElement();
            // scaleDifference element
            xmlWriter.writeStartElement( CRSNS, "ScaleDifference" );
            xmlWriter.writeCharacters( Double.toString( wgs84.ppm ) );
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export the transformation to it's appropriate deegree-crs-definitions form.
     * 
     * @param ls
     *            to be exported
     * @param xmlWriter
     *            to export to.
     * @throws XMLStreamException
     */
    protected void export( LeastSquareApproximation ls, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( ls != null ) {
            // xAxisTranslation element
            xmlWriter.writeStartElement( CRSNS, "PolynomialOrder" );
            xmlWriter.writeCharacters( Integer.toString( ls.getOrder() ) );
            xmlWriter.writeEndElement();

            List<Double> params = ls.getFirstParams();
            StringBuilder sb = new StringBuilder();
            for ( int i = 0; i < params.size(); ++i ) {
                sb.append( params.get( i ) );
                if ( ( i + 1 ) < params.size() ) {
                    sb.append( " " );
                }
            }
            // xParameters
            xmlWriter.writeStartElement( CRSNS, "XParameters" );
            xmlWriter.writeCharacters( sb.toString() );
            xmlWriter.writeEndElement();

            // yParameters
            params = ls.getSecondParams();
            sb = new StringBuilder();
            for ( int i = 0; i < params.size(); ++i ) {
                sb.append( params.get( i ) );
                if ( ( i + 1 ) < params.size() ) {
                    sb.append( " " );
                }
            }
            xmlWriter.writeStartElement( CRSNS, "YParameters" );
            xmlWriter.writeCharacters( sb.toString() );
            xmlWriter.writeEndElement();

            xmlWriter.writeStartElement( CRSNS, "ScaleX" );
            xmlWriter.writeCharacters( Float.toString( ls.getScaleX() ) );
            xmlWriter.writeEndElement();

            xmlWriter.writeStartElement( CRSNS, "ScaleY" );
            xmlWriter.writeCharacters( Float.toString( ls.getScaleY() ) );
            xmlWriter.writeEndElement();

        }
    }

    /**
     * Export the PrimeMeridian to it's appropriate deegree-crs-definitions form.
     * 
     * @param pm
     *            PrimeMeridian to be exported
     * @param xmlWriter
     *            to export to.
     * @throws XMLStreamException
     */
    @Override
    protected void export( PrimeMeridian pm, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( pm != null ) {
            xmlWriter.writeStartElement( CRSNS, "PrimeMeridian" );

            exportIdentifiable( pm, xmlWriter );
            // units element
            export( pm.getAngularUnit(), xmlWriter );
            // longitude element
            xmlWriter.writeStartElement( CRSNS, "Longitude" );
            xmlWriter.writeCharacters( Double.toString( pm.getLongitude( Unit.DEGREE ) ) );
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export the compoundCRS to it's appropriate deegree-crs-definitions form.
     * 
     * @param compoundCRS
     *            to be exported
     * @param xmlWriter
     *            to export the geographic CRS to.
     * @throws XMLStreamException
     */
    @Override
    protected void export( CompoundCRS compoundCRS, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( compoundCRS != null ) {
            xmlWriter.writeStartElement( CRSNS, "CompoundCRS" );

            exportIdentifiable( compoundCRS, xmlWriter );
            CoordinateSystem underCRS = compoundCRS.getUnderlyingCRS();
            // usedCRS element
            xmlWriter.writeStartElement( CRSNS, "UsedCRS" );
            xmlWriter.writeCharacters( underCRS.getCode().getOriginal().toLowerCase() );
            xmlWriter.writeEndElement();
            // heightAxis element
            Axis heightAxis = compoundCRS.getHeightAxis();
            export( heightAxis, "HeightAxis", xmlWriter );
            // defaultHeight element
            double axisHeight = compoundCRS.getDefaultHeight();
            xmlWriter.writeStartElement( CRSNS, "DefaultHeight" );
            xmlWriter.writeCharacters( Double.toString( axisHeight ) );
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
        }

    }

    /**
     * Export the projected CRS to it's appropriate deegree-crs-definitions form.
     * 
     * @param projectedCRS
     *            to be exported
     * @param xmlWriter
     *            to export the projected CRS to.
     * @throws XMLStreamException
     */
    @Override
    protected void export( ProjectedCRS projectedCRS, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( projectedCRS != null ) {
            xmlWriter.writeStartElement( CRSNS, "ProjectedCRS" );
            exportAbstractCRS( projectedCRS, xmlWriter );

            xmlWriter.writeStartElement( CRSNS, "UsedGeographicCRS" );
            xmlWriter.writeCharacters( projectedCRS.getGeographicCRS().getCode().getOriginal().toLowerCase() );
            xmlWriter.writeEndElement();

            // projection
            xmlWriter.writeStartElement( CRSNS, "UsedProjection" );
            xmlWriter.writeCharacters( projectedCRS.getProjection().getCode().getOriginal().toLowerCase() );
            xmlWriter.writeEndElement();

            xmlWriter.writeEndElement(); // projectedcrs
        }
    }

    /**
     * Export the projection to it's appropriate deegree-crs-definitions form.
     * 
     * @param projection
     *            to be exported
     * @param xmlWriter
     *            to export the projection to.
     * @throws XMLStreamException
     */
    @Override
    protected void export( Projection projection, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( projection != null ) {

            String implName = projection.getImplementationName();
            if ( "LambertAzimuthalEqualArea".equalsIgnoreCase( implName ) ) {
                xmlWriter.writeStartElement( CRSNS, "LambertAzimuthalEqualArea" );
            } else if ( "lambertConformalConic".equalsIgnoreCase( implName ) ) {
                xmlWriter.writeStartElement( CRSNS, "LambertConformalConic" );
            } else if ( "StereographicAzimuthal".equalsIgnoreCase( implName ) ) {
                xmlWriter.writeStartElement( CRSNS, "StereographicAzimuthal" );
            } else if ( "StereographicAlternative".equalsIgnoreCase( implName ) ) {
                xmlWriter.writeStartElement( CRSNS, "StereographicAlternative" );
            } else if ( "TransverseMercator".equalsIgnoreCase( implName ) ) {
                xmlWriter.writeStartElement( CRSNS, "TransverseMercator" );
                if ( !( (TransverseMercator) projection ).getHemisphere() ) {
                    xmlWriter.writeAttribute( "northernHemisphere", "false" );
                }

            } else if ( "Mercator".equalsIgnoreCase( implName ) ) {
                xmlWriter.writeStartElement( CRSNS, "Mercator" );
            } else {
                xmlWriter.writeStartElement( CRSNS, "UserDefined" );
                xmlWriter.writeAttribute( "class", projection.getClass().getCanonicalName() );
            }

            exportIdentifiable( projection, xmlWriter );

            // latitudeOfNaturalOrigin
            xmlWriter.writeStartElement( CRSNS, "LatitudeOfNaturalOrigin" );
            // xmlWriter.writeAttribute( "inDegrees", "true" );
            xmlWriter.writeCharacters( Double.toString( toDegrees( projection.getProjectionLatitude() ) ) );
            xmlWriter.writeEndElement();
            // longitudeOfNaturalOrigin
            xmlWriter.writeStartElement( CRSNS, "LongitudeOfNaturalOrigin" );
            // xmlWriter.writeAttribute( "inDegrees", "true" );
            xmlWriter.writeCharacters( Double.toString( toDegrees( projection.getProjectionLongitude() ) ) );
            xmlWriter.writeEndElement();
            // scaleFactor element
            xmlWriter.writeStartElement( CRSNS, "ScaleFactor" );
            xmlWriter.writeCharacters( Double.toString( projection.getScale() ) );
            xmlWriter.writeEndElement();
            // falseEasting element
            xmlWriter.writeStartElement( CRSNS, "FalseEasting" );
            xmlWriter.writeCharacters( Double.toString( projection.getFalseEasting() ) );
            xmlWriter.writeEndElement();
            // falseNorthing element
            xmlWriter.writeStartElement( CRSNS, "FalseNorthing" );
            xmlWriter.writeCharacters( Double.toString( projection.getFalseNorthing() ) );
            xmlWriter.writeEndElement();
            if ( "lambertConformalConic".equalsIgnoreCase( implName ) ) {
                double paralellLatitude = ( (LambertConformalConic) projection ).getFirstParallelLatitude();
                if ( !Double.isNaN( paralellLatitude ) && Math.abs( paralellLatitude ) > EPS11 ) {
                    paralellLatitude = toDegrees( paralellLatitude );
                    xmlWriter.writeStartElement( CRSNS, "FirstParallelLatitude" );
                    // xmlWriter.writeAttribute( "inDegrees", "true" );
                    xmlWriter.writeCharacters( Double.toString( paralellLatitude ) );
                    xmlWriter.writeEndElement();
                }
                paralellLatitude = ( (LambertConformalConic) projection ).getSecondParallelLatitude();
                if ( !Double.isNaN( paralellLatitude ) && Math.abs( paralellLatitude ) > EPS11 ) {
                    paralellLatitude = toDegrees( paralellLatitude );
                    xmlWriter.writeStartElement( CRSNS, "SecondParallelLatitude" );
                    // xmlWriter.writeAttribute( "inDegrees", "true" );
                    xmlWriter.writeCharacters( Double.toString( paralellLatitude ) );
                    xmlWriter.writeEndElement();
                }
            } else if ( "stereographicAzimuthal".equalsIgnoreCase( implName ) ) {
                xmlWriter.writeStartElement( CRSNS, "TrueScaleLatitude" );
                // xmlWriter.writeAttribute( "inDegrees", "true" );
                xmlWriter.writeCharacters( Double.toString( toDegrees( ( (StereographicAzimuthal) projection ).getTrueScaleLatitude() ) ) );
                xmlWriter.writeEndElement();
            }
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export the geocentric/geographic CRS to it's appropriate deegree-crs-definitions form.
     * 
     * @param geoGraphicCRS
     *            to be exported
     * @param xmlWriter
     *            to export the geographic CRS to.
     * @throws XMLStreamException
     */
    @Override
    protected void export( GeographicCRS geoGraphicCRS, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( geoGraphicCRS != null ) {
            xmlWriter.writeStartElement( CRSNS, "GeographicCRS" );

            exportAbstractCRS( geoGraphicCRS, xmlWriter );
            xmlWriter.writeStartElement( CRSNS, "UsedDatum" );
            xmlWriter.writeCharacters( geoGraphicCRS.getDatum().getCode().getOriginal().toLowerCase() );
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export the geocentric CRS to it's appropriate deegree-crs-definitions form.
     * 
     * @param geocentricCRS
     *            to be exported
     * @param xmlWriter
     *            to export the geocentric CRS to.
     * @throws XMLStreamException
     */
    @Override
    protected void export( GeocentricCRS geocentricCRS, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( geocentricCRS != null ) {
            xmlWriter.writeStartElement( CRSNS, "GeocentricCRS" );
            exportAbstractCRS( geocentricCRS, xmlWriter );
            xmlWriter.writeStartElement( CRSNS, "UsedDatum" );
            xmlWriter.writeCharacters( geocentricCRS.getDatum().getCode().getOriginal().toLowerCase() );
            xmlWriter.writeEndElement();
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export toplevel crs features.
     * 
     * @param crs
     *            to be exported
     * @param xmlWriter
     *            to export to
     * @throws XMLStreamException
     */
    @Override
    protected void exportAbstractCRS( CoordinateSystem crs, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( crs != null ) {
            exportIdentifiable( crs, xmlWriter );

            Axis[] axes = crs.getAxis();
            for ( Axis a : axes ) {
                export( a, "Axis", xmlWriter );
            }

            // axisOrder is a goner

            // export transformations and recurse on their type
            // exportTransformations( crs.getTransformations(), xmlWriter );
        }
    }

    /**
     * Export an axis to xml in the crs-definitions schema layout.
     * 
     * @param axis
     *            to be exported.
     * @param elName
     *            the name of the element, either 'Axis' or 'heightAxis'
     * @param xmlWriter
     *            to export to.
     * @throws XMLStreamException
     */
    @Override
    protected void export( Axis axis, String elName, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( axis != null ) {
            xmlWriter.writeStartElement( CRSNS, elName );
            // axis name
            xmlWriter.writeStartElement( CRSNS, "Name" );
            xmlWriter.writeCharacters( axis.getName() );
            xmlWriter.writeEndElement();
            // axis units
            export( axis.getUnits(), xmlWriter );
            // axis orientation
            xmlWriter.writeStartElement( CRSNS, "AxisOrientation" );
            xmlWriter.writeCharacters( axis.getOrientationAsString() );
            xmlWriter.writeEndElement();

            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export a unit to xml in the crs-definitions schema layout.
     * 
     * @param units
     *            to be exported.
     * @param xmlWriter
     *            to export to.
     * @throws XMLStreamException
     */
    @Override
    protected void export( Unit units, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( units != null ) {
            xmlWriter.writeStartElement( CRSNS, "Units" );
            xmlWriter.writeCharacters( units.getName().toLowerCase() );
            xmlWriter.writeEndElement();
        }

    }

    /**
     * Export the datum to it's appropriate deegree-crs-definitions form.
     * 
     * @param datum
     *            to be exported
     * @param xmlWriter
     *            to export the datum to.
     * @throws XMLStreamException
     */
    @Override
    protected void export( GeodeticDatum datum, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( datum != null ) {
            xmlWriter.writeStartElement( CRSNS, "GeodeticDatum" );
            exportIdentifiable( datum, xmlWriter );
            // usedEllipsoid element
            xmlWriter.writeStartElement( CRSNS, "UsedEllipsoid" );
            xmlWriter.writeCharacters( datum.getEllipsoid().getCode().getOriginal().toLowerCase() );
            xmlWriter.writeEndElement();
            // usedPrimeMeridian element
            PrimeMeridian pm = datum.getPrimeMeridian();
            if ( pm != null ) {
                xmlWriter.writeStartElement( CRSNS, "UsedPrimeMeridian" );
                xmlWriter.writeCharacters( pm.getCode().getOriginal().toLowerCase() );
                xmlWriter.writeEndElement();
            }
            // // usedWGS84ConversionInfo element
            // Helmert convInfo = datum.getWGS84Conversion();
            // if ( convInfo != null ) {
            // xmlWriter.writeStartElement( CRSNS, "usedWGS84ConversionInfo" );
            // xmlWriter.writeCharacters( convInfo.getCode().toString() );
            // xmlWriter.writeEndElement();
            // }
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Export the ellipsoid to it's appropriate deegree-crs-definitions form.
     * 
     * @param ellipsoid
     *            to be exported
     * @param xmlWriter
     *            to export the ellipsoid to.
     * @throws XMLStreamException
     */
    @Override
    protected void export( Ellipsoid ellipsoid, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        if ( ellipsoid != null ) {
            xmlWriter.writeStartElement( CRSNS, "Ellipsoid" );

            // write the elements that are specific to Identifiable
            exportIdentifiable( ellipsoid, xmlWriter );

            export( ellipsoid.getUnits(), xmlWriter );

            double sMajorAxis = ellipsoid.getSemiMajorAxis();
            xmlWriter.writeStartElement( CRSNS, "SemiMajorAxis" );
            xmlWriter.writeCharacters( Double.toString( sMajorAxis ) );
            xmlWriter.writeEndElement();

            double inverseF = ellipsoid.getInverseFlattening();
            xmlWriter.writeStartElement( CRSNS, "InverseFlattening" );
            xmlWriter.writeCharacters( Double.toString( inverseF ) );
            xmlWriter.writeEndElement();

            xmlWriter.writeEndElement();
        }
    }

    /**
     * Creates the basic nodes of the identifiable object.
     * 
     * @param identifiable
     *            object to be exported.
     * @param xmlWriter
     *            to export to
     * @throws XMLStreamException
     */
    @Override
    protected void exportIdentifiable( CRSIdentifiable identifiable, XMLStreamWriter xmlWriter )
                            throws XMLStreamException {
        // ids
        CRSCodeType[] identifiers = identifiable.getCodes();
        for ( CRSCodeType id : identifiers ) {
            if ( id != null ) {
                xmlWriter.writeStartElement( CRSNS, "Id" );
                xmlWriter.writeCharacters( id.getOriginal().toLowerCase() );
                xmlWriter.writeEndElement();
            }
        }
        // names
        String[] names = identifiable.getNames();
        if ( names != null && names.length > 0 ) {
            for ( String name : names ) {
                if ( name != null ) {
                    xmlWriter.writeStartElement( CRSNS, "Name" );
                    xmlWriter.writeCharacters( name );
                    xmlWriter.writeEndElement();
                }
            }
        }
        // versions
        String[] versions = identifiable.getVersions();
        if ( versions != null && versions.length > 0 ) {
            for ( String version : versions ) {
                if ( version != null ) {
                    xmlWriter.writeStartElement( CRSNS, "Version" );
                    xmlWriter.writeCharacters( version );
                    xmlWriter.writeEndElement();
                }
            }
        }
        // descriptions
        String[] descriptions = identifiable.getDescriptions();
        if ( descriptions != null && descriptions.length > 0 ) {
            for ( String description : descriptions ) {
                if ( description != null ) {
                    xmlWriter.writeStartElement( CRSNS, "Description" );
                    xmlWriter.writeCharacters( description );
                    xmlWriter.writeEndElement();
                }
            }
        }
        // areasOfUse
        String[] areas = identifiable.getAreasOfUse();
        if ( areas != null && areas.length > 0 ) {
            for ( String area : areas ) {
                if ( area != null ) {
                    xmlWriter.writeStartElement( CRSNS, "AreaOfUse" );
                    xmlWriter.writeCharacters( area );
                    xmlWriter.writeEndElement();
                }
            }
        }

    }

    static class IdComparer implements Comparator<CRSIdentifiable> {

        @Override
        public int compare( CRSIdentifiable o1, CRSIdentifiable o2 ) {
            String first = o1.getCode().getOriginal();
            String second = o2.getCode().getOriginal();
            int result = first.compareToIgnoreCase( second );
            if ( result == 0 && !o1.equals( o2 ) ) {
                result = -1;
            }
            return result;
        }
    }

}
