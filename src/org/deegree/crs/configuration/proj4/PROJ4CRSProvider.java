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

package org.deegree.crs.configuration.proj4;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.vecmath.Point2d;

import org.deegree.crs.CRSCodeType;
import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.EPSGCode;
import org.deegree.crs.components.Axis;
import org.deegree.crs.components.Ellipsoid;
import org.deegree.crs.components.GeodeticDatum;
import org.deegree.crs.components.PrimeMeridian;
import org.deegree.crs.components.Unit;
import org.deegree.crs.configuration.AbstractCRSProvider;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.crs.exceptions.CRSConfigurationException;
import org.deegree.crs.projections.Projection;
import org.deegree.crs.projections.azimuthal.LambertAzimuthalEqualArea;
import org.deegree.crs.projections.azimuthal.StereographicAlternative;
import org.deegree.crs.projections.azimuthal.StereographicAzimuthal;
import org.deegree.crs.projections.conic.LambertConformalConic;
import org.deegree.crs.projections.cylindric.TransverseMercator;
import org.deegree.crs.transformations.Transformation;
import org.deegree.crs.transformations.helmert.Helmert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.deegree.crs.i18n.Messages;

/**
 * The <code>PROJ4CRSProvider</code> class is capable of parsing the nad/epsg file and use it as a backend for crs's.
 * This class also adds following identifiers to the coordinatesystems.
 * <ul>
 * <li>http://www.opengis.net/gml/srs/epsg.xml#4326</li>
 * <li>URN:OPENGIS:DEF:CRS:EPSG::</li>
 * <li>URN:OGC:DEF:CRS:EPSG::</li>
 * </ul>
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */

public class PROJ4CRSProvider extends AbstractCRSProvider<Map<String, String>> {

    private static Logger LOG = LoggerFactory.getLogger( PROJ4CRSProvider.class );

    private static int ellipsCount = 0;

    private static int datumCount = 0;

    private static int geographicCRSCount = 0;

    private static int primeMeridianCount = 0;

    private final static String EPSG_PRE = "EPSG:";

    private final static String OPENGIS_URL = "HTTP://WWW.OPENGIS.NET/GML/SRS/EPSG.XML#";

    private final static String OPENGIS_URN = "URN:OPENGIS:DEF:CRS:EPSG::";

    private static final String OGC_URN = "URN:OGC:DEF:CRS:EPSG::";

    private Map<CRSCodeType, CoordinateSystem> coordinateSystems = new HashMap<CRSCodeType, CoordinateSystem>( 10000 );

    private String version = null;

    private String[] versions = null;

    private String areaOfUse = "Unknown";

    private String[] areasOfUse = new String[] { "Unknown" };

    /**
     * Export constructor, sets the version to current date..
     */
    public PROJ4CRSProvider() {
        super( new Properties(), null, null );
        // dummy constructor for exporting only.
        GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
        version = cal.get( Calendar.YEAR ) + "-" + ( cal.get( Calendar.MONTH ) + 1 ) + "-"
                  + cal.get( Calendar.DAY_OF_MONTH ) + "T" + cal.get( Calendar.HOUR_OF_DAY ) + ":"
                  + cal.get( Calendar.MINUTE );
        versions = new String[] { version };
    }

    /**
     * Opens a reader on the file and parses all parameters with id, without instantiating any CoordinateSystems.
     *
     * @param properties
     *            containing a crs.configuration property referencing a file location.
     */
    public PROJ4CRSProvider( Properties properties ) {
        super( properties, ProjFileResource.class, null );
        if ( getResolver() == null ) {
            setResolver( new ProjFileResource( this, properties ) );
        }
    }

    @Override
    protected ProjFileResource getResolver() {
        return (ProjFileResource) super.getResolver();
    }

    public List<CoordinateSystem> getAvailableCRSs()
                            throws CRSConfigurationException {
        Set<CRSCodeType> keys = getResolver().getAvailableCodes();
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Found following keys: " + keys );
        }
        List<CoordinateSystem> allSystems = new LinkedList<CoordinateSystem>();
        for ( CRSCodeType key : keys ) {
            try {
                CoordinateSystem result = getCRSByCode( key );
                if ( result != null ) {
                    allSystems.add( result );
                }
            } catch ( CRSConfigurationException e ) {
                LOG.info( Messages.getMessage( "CRS_CONFIG_PROJ4_NOT_ADDING_CRS", key, e.getMessage() ) );
            }
        }
        // get all already created crs's
        keys = coordinateSystems.keySet();
        for ( CRSCodeType key : keys ) {
            CoordinateSystem result = coordinateSystems.get( key );
            if ( result != null ) {
                allSystems.add( result );
            }
        }
        return allSystems;
    }

    public boolean canExport() {
        return false;
    }

    public void export( StringBuilder sb, List<CoordinateSystem> crsToExport ) {
        throw new UnsupportedOperationException( "Exporting to PROJ4 configuration is not suppored yet." );

    }

    /**
     * Creates a projected crs from given params. Because proj4 doesn't define some axisorder xy is always assumed.
     *
     * @param projectionName
     *            to give the geographic crs (+e.g. proj=tmerc) or <code>null</code>
     * @param params
     *            containing datum info.
     * @return a geographic crs
     * @throws CRSConfigurationException
     *             if an exception occurs while creating the datum.
     */
    private ProjectedCRS createProjectedCRS( String projectionName, Map<String, String> params )
                            throws CRSConfigurationException {
        String[] names = new String[] { params.remove( "comment" ) };

        String[] descriptions = null;
        String id = params.get( "identifier" );
        String[] ids = getPredefinedIDs( id );

        // convert ids to codes
        CRSCodeType[] codes = new CRSCodeType[ ids.length ];
        for ( int i = 0; i < ids.length; i++ )
            codes[i] = CRSCodeType.valueOf( ids[i] );

        String geoID = "GEO_CRS_" + geographicCRSCount;
        if ( "3068".equals( id ) || "31466".equals( id ) || "31467".equals( id ) || "31468".equals( id )
             || "31469".equals( id ) ) {
            geoID = EPSG_PRE + "4314";
        } else {
            geographicCRSCount++;
        }
        GeographicCRS underLyingCRS = createGeographicCRS( geoID, id, params );
        Projection projection = createProjection( projectionName, underLyingCRS, params );

        return new ProjectedCRS( projection, new Axis[] { new Axis( projection.getUnits(), "x", Axis.AO_EAST ),
                                                         new Axis( projection.getUnits(), "y", Axis.AO_NORTH ) }, codes,
                                 names, versions, descriptions, areasOfUse );
    }

    /**
     * Creates a geographic crs with given identifier, if the identifier is empty or <code>null</code> the
     * params.getIdentifier will be used. Because proj4 doesn't define some axisorder xy is always assumed.
     *
     * @param identifier
     *            to give the geographic crs (+proj=longlat) or <code>null</code>
     * @param params
     *            containing datum info.
     * @param projectedID
     *            of the projected crs (only if identifier is not null).
     * @return a geographic crs
     * @throws CRSConfigurationException
     *             if an exception occurs while creating the datum.
     */
    private GeographicCRS createGeographicCRS( String identifier, String projectedID, Map<String, String> params )
                            throws CRSConfigurationException {
        String name = "Proj4 defined Geographic CRS";
        String tmp = params.remove( "comment" );
        if ( tmp != null && !"".equals( tmp.trim() ) ) {
            name = tmp;
        }
        String[] names = new String[] { name };
        String description = "Handmade proj4 geographic crs definition (parsed from nad/epsg).";
        String ids[] = new String[] { identifier };
        CRSCodeType[] codes = new CRSCodeType[ ids.length ];
        String tmpIdentifier = identifier;
        String tmpProjectedID = projectedID;
        if ( tmpIdentifier == null || "".equals( tmpIdentifier.trim() ) ) {
            tmpIdentifier = params.get( "identifier" );
            ids = getPredefinedIDs( tmpIdentifier );

            // convert ids to codes
            for ( int i = 0; i < ids.length; i++ )
                codes[i] = CRSCodeType.valueOf( ids[i] );

            // if the id was not set, we create a geocrs, which means that no projectedID has been
            // set, we want to build the datum with the id though!
            tmpProjectedID = tmpIdentifier;
        } else {
            description += " Used by projected crs with id: " + projectedID;
        }
        String[] descriptions = new String[] { description };
        // projectedID will also hold the id of the geo-crs if it is a top level one.
        GeodeticDatum datum = createDatum( params, tmpProjectedID );
        GeographicCRS result = new GeographicCRS( datum,
                                                  new Axis[] { new Axis( Unit.RADIAN, "longitude", Axis.AO_EAST ),
                                                              new Axis( Unit.RADIAN, "latitude", Axis.AO_NORTH ) },
                                                  codes, names, versions, descriptions, areasOfUse );
        return result;
    }

    /**
     * Create a datum by resolving the 'datum' param or creating it from the given ellipsoid/primemeridan parameters.
     *
     * @param params
     *            to create the datum from
     * @return a datum
     * @throws CRSConfigurationException
     *             if one of the datums necessities( ellipsoid, primemeridan) could not be created.
     */
    private GeodeticDatum createDatum( Map<String, String> params, String identifier )
                            throws CRSConfigurationException {

        GeodeticDatum result = null;
        String tmpValue = params.remove( "datum" );
        if ( tmpValue != null && !"".equals( tmpValue.trim() ) ) {
            // removing the defined ellipsoid.
            result = getPredefinedDatum( tmpValue, params.remove( "ellps" ), identifier );
            if ( result == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJ4_UNKNOWN_DATUM",
                                                                          params.get( EPSG_PRE + "identifier" ),
                                                                          tmpValue ) );
            }
        } else {
            Ellipsoid ellipsoid = createEllipsoid( params );
            if ( ellipsoid == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJ4_DATUM_WITHOUT_ELLIPSOID",
                                                                          params.get( EPSG_PRE + "identifier" ) ) );
            }
            String id = "DATUM_" + datumCount++;
            String name = "Proj4 defined datum";

            String description = "Handmade proj4 datum definition (parsed from nad/epsg) used by crs with id: "
                                 + ( EPSG_PRE + identifier );
            PrimeMeridian pm = createPrimeMeridian( params );
            result = new GeodeticDatum( ellipsoid, pm, null, CRSCodeType.valueOf( id ), name, version, description, areaOfUse );
        }
        return result;
    }

    /**
     * Create a prime meridian according to predefined proj4 definitions.
     *
     * @param params
     *            to get the 'pm' parameter from.
     * @return a mapped primemeridian or the Greenwich if no pm parameter was found.
     * @throws CRSConfigurationException
     *             if the pm-parameter could not be mapped.
     */
    private PrimeMeridian createPrimeMeridian( Map<String, String> params )
                            throws CRSConfigurationException {
        String tmpValue = params.remove( "pm" );
        PrimeMeridian result = PrimeMeridian.GREENWICH;
        String id = "pm_" + primeMeridianCount++;
        String[] names = null;
        String[] meridianVersions = null;
        String[] descs = null;
        String[] aous = null;
        double longitude = Double.NaN;
        if ( tmpValue != null && !"".equals( tmpValue.trim() ) && !"greenwich".equals( tmpValue.trim() ) ) {
            if ( "athens".equals( tmpValue ) ) {
                longitude = parseAngleFormat( "23d42'58.815\"E", false );
                id = "8912";
                names = new String[] { "Athens" };
                meridianVersions = new String[] { "1995-06-02" };
                descs = new String[] { "Used in Greece for older mapping based on Hatt projection." };
            } else if ( "bern".equals( tmpValue ) ) {
                longitude = parseAngleFormat( "7d26'22.5\"E", false );
                id = "8907";
                names = new String[] { "Bern" };
                meridianVersions = new String[] { "1995-06-02" };
                descs = new String[] { "1895 value. Newer value of 7 deg 26 min 22.335 sec E determined in 1938." };
            } else if ( "bogota".equals( tmpValue ) ) {
                longitude = parseAngleFormat( "74d04'51.3\"W", false );
                id = "8904";
                names = new String[] { "Bogota" };
                meridianVersions = new String[] { "1995-06-02" };
                descs = new String[] { "Instituto Geografico 'Augustin Cadazzi' (IGAC); Bogota" };
            } else if ( "brussels".equals( tmpValue ) ) {
                longitude = parseAngleFormat( "4d22'4.71\"E", false );
                id = "8910";
                names = new String[] { "Brussel" };
                meridianVersions = new String[] { "1995-06-02" };
            } else if ( "ferro".equals( tmpValue ) ) {
                longitude = parseAngleFormat( "17d40'W", false );
                id = "8909";
                names = new String[] { "Ferro" };
                meridianVersions = new String[] { "1995-06-02" };
                descs = new String[] { "Used in Austria and former Czechoslovakia. " };
            } else if ( "jakarta".equals( tmpValue ) ) {
                longitude = parseAngleFormat( "106d48'27.79\"E", false );
                id = "8908";
                names = new String[] { "Jakarta" };
                meridianVersions = new String[] { "1995-06-02" };
            } else if ( "lisbon".equals( tmpValue ) ) {
                longitude = parseAngleFormat( "9d07'54.862\"W", false );
                id = "8902";
                names = new String[] { "lisbon" };
                meridianVersions = new String[] { "1995-06-02" };
                descs = new String[] { "Information Source: Instituto Geografico e Cadastral; Lisbon " };
            } else if ( "madrid".equals( tmpValue ) ) {
                longitude = parseAngleFormat( "3d41'16.58\"W", false );
                id = "8905";
                names = new String[] { "Madrid" };
                meridianVersions = new String[] { "1995-06-02" };
                descs = new String[] { "Value adopted by IGN (Paris) in 1936. Equivalent to 2 deg 20min 14.025sec. Preferred by EPSG to earlier value of 2deg 20min 13.95sec (2.596898 grads) used by RGS London." };
            } else if ( "oslo".equals( tmpValue ) ) {
                longitude = parseAngleFormat( "10d43'22.5\"E", false );
                id = "8913";
                names = new String[] { "Oslo" };
                meridianVersions = new String[] { "1995-06-02" };
                descs = new String[] { "ormerly known as Kristiania or Christiania." };
            } else if ( "paris".equals( tmpValue ) ) {
                longitude = parseAngleFormat( "2d20'14.025\"E", false );
                id = "8903";
                names = new String[] { "Paris" };
                meridianVersions = new String[] { "1995-06-02" };
                descs = new String[] { "Value adopted by IGN (Paris) in 1936. Equivalent to 2 deg 20min 14.025sec. Preferred by EPSG to earlier value of 2deg 20min 13.95sec (2.596898 grads) used by RGS London." };
            } else if ( "rome".equals( tmpValue ) ) {
                longitude = parseAngleFormat( "12d27'8.4\"E", false );
                id = "8906";
                names = new String[] { "Rome" };
                meridianVersions = new String[] { "1995-06-02" };
            } else if ( "stockholm".equals( tmpValue ) ) {
                longitude = parseAngleFormat( "18d3'29.8\"E", false );
                id = "8911";
                names = new String[] { "Stockholm" };
                meridianVersions = new String[] { "1995-06-02" };
            } else {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJ4_UNKNOWN_PM",
                                                                          params.get( EPSG_PRE + "identifier" ),
                                                                          tmpValue ) );
            }
        }
        if ( !Double.isNaN( longitude ) ) {
            String[] ids = new String[] { id };
            if ( !id.startsWith( "pm_" ) ) {
                ids = getPredefinedIDs( id );
            }

            CRSCodeType[] codes = new CRSCodeType[ ids.length ];
            for (int i = 0; i < ids.length; i++ )
                codes[i] = CRSCodeType.valueOf( ids[i] );
            result = new PrimeMeridian( Unit.RADIAN, longitude, codes, names, meridianVersions, descs, aous );
        }
        return result;
    }

    /**
     * Tries to create an ellips from a predefined mapping, or from axis, eccentricities etc. if defined.
     *
     * @param params
     *            to create the ellipsoid from
     * @return an (mapped) ellipsoid.
     * @throws CRSConfigurationException
     *             if no mapping was found or the semimajor axis was not defined.
     */
    private Ellipsoid createEllipsoid( Map<String, String> params )
                            throws CRSConfigurationException {

        Ellipsoid result = null;
        double semiMajorAxis = Double.NaN;
        double eccentricitySquared = Double.NaN;
        double eccentricity = Double.NaN;
        double inverseFlattening = Double.NaN;
        double semiMinorAxis = Double.NaN;

        // Get the ellipsoid
        String tmpValue = params.remove( "ellps" );
        if ( tmpValue != null && !"".equals( tmpValue.trim() ) ) {
            LOG.debug( "Creating predefined ellipsoid: " + tmpValue );
            result = getPredefinedEllipsoid( tmpValue );
        } else {
            // if no ellipsoid is defined maybe a sphere
            tmpValue = params.remove( "R" );
            if ( tmpValue != null && !"".equals( tmpValue.trim() ) ) {
                LOG.debug( "Found a Radius instead of an ellipsoid, the projection uses a sphere!" );
                semiMajorAxis = Double.parseDouble( tmpValue );
            } else {// an ellipsoid instead of a sphere.
                tmpValue = params.remove( "a" );
                if ( tmpValue != null && !"".equals( tmpValue.trim() ) ) {
                    semiMajorAxis = Double.parseDouble( tmpValue );
                }
                tmpValue = params.remove( "es" );
                if ( tmpValue != null && !"".equals( tmpValue.trim() ) ) {
                    eccentricitySquared = Double.parseDouble( tmpValue );
                } else {
                    tmpValue = params.remove( "e" );
                    if ( tmpValue != null && !"".equals( tmpValue.trim() ) ) {
                        eccentricity = Double.parseDouble( tmpValue );
                    } else {
                        tmpValue = params.remove( "rf" );
                        if ( tmpValue != null && !"".equals( tmpValue.trim() ) ) {
                            inverseFlattening = Double.parseDouble( tmpValue );
                        } else {
                            tmpValue = params.remove( "f" );
                            if ( tmpValue != null && !"".equals( tmpValue.trim() ) ) {
                                double flattening = Double.parseDouble( tmpValue );
                                if ( Math.abs( flattening ) > 0.000001 ) {
                                    inverseFlattening = 1 / flattening;
                                } else {
                                    LOG.debug( "The given flattening: " + flattening
                                                  + " can not be inverted (divide by zero) using a sphere as ellipsoid" );
                                }
                            } else {
                                tmpValue = params.remove( "b" );
                                if ( tmpValue != null && !"".equals( tmpValue.trim() ) ) {
                                    semiMinorAxis = Double.parseDouble( tmpValue );
                                }
                            }
                        }
                    }
                }
            }

            /**
             * These parameters (R_A, R_V, R_a, R_g, R_h, R_lat_a, R_lat_g) were not used in the epsg file, maybe they
             * are of future value?. <code>
             *     private final static double SIXTH = .1666666666666666667; // 1/6
             *     private final static double RA4 = .04722222222222222222; // 17/360
             *     private final static double RA6 = .02215608465608465608; // 67/3024
             *     private final static double RV4 = .06944444444444444444; // 5/72
             *     private final static double RV6 = .04243827160493827160; // 55/1296
             * </code>
             */
            // String tmpValue = params.get( "R_A" );
            // if ( tmpValue != null && Boolean.getBoolean( tmpValue ) ) {
            // semiMajorAxis *= 1. - eccentricitySquared
            // * ( SIXTH + eccentricitySquared * ( RA4 + eccentricitySquared * RA6 ) );
            // } else {
            // tmpValue = params.get( "R_V" );
            // if ( tmpValue != null && Boolean.getBoolean( tmpValue ) ) {
            // semiMajorAxis *= 1. - eccentricitySquared
            // * ( SIXTH + eccentricitySquared * ( RV4 + eccentricitySquared * RV6 ) );
            // } else {
            // tmpValue = params.get( "R_a" );
            // if ( tmpValue != null && Boolean.getBoolean( tmpValue ) ) {
            // semiMajorAxis = .5 * ( semiMajorAxis + semiMinorAxis );
            // } else {
            // tmpValue = params.get( "R_g" );
            // if ( tmpValue != null && Boolean.getBoolean( tmpValue ) ) {
            // semiMajorAxis = Math.sqrt( semiMajorAxis * semiMinorAxis );
            // } else {
            // tmpValue = params.get( "R_h" );
            // if ( tmpValue != null && Boolean.getBoolean( tmpValue ) ) {
            // semiMajorAxis = 2. * semiMajorAxis * semiMinorAxis / ( semiMajorAxis + semiMinorAxis
            // );
            // eccentricitySquared = 0.;
            // } else {
            // tmpValue = params.get( "R_lat_a" );
            // if ( tmpValue != null ) {
            // double tmp = Math.sin( parseAngle( tmpValue ) );
            // if ( Math.abs( tmp ) > HALFPI )
            // throw new CRSConfigurationException( "-11" );
            // tmp = 1. - eccentricitySquared * tmp * tmp;
            // semiMajorAxis *= .5 * ( 1. - eccentricitySquared + tmp ) / ( tmp * Math.sqrt( tmp )
            // );
            // eccentricitySquared = 0.;
            // } else {
            // tmpValue = params.get( "R_lat_g" );
            // if ( tmpValue != null ) {
            // double tmp = Math.sin( parseAngle( tmpValue ) );
            // if ( Math.abs( tmp ) > HALFPI )
            // throw new CRSConfigurationException( "-11" );
            // tmp = 1. - eccentricitySquared * tmp * tmp;
            // semiMajorAxis *= Math.sqrt( 1. - eccentricitySquared ) / tmp;
            // eccentricitySquared = 0.;
            // }
            // }
            // }
            // }
            // }
            // }
            // }
            if ( Double.isNaN( semiMajorAxis ) ) {
                throw new CRSConfigurationException(
                                                     Messages.getMessage(
                                                                          "CRS_CONFIG_PROJ4_ELLIPSOID_WITHOUT_SEMIMAJOR",
                                                                          params.get( EPSG_PRE + "identifier" ) ) );
            }
            String id = "ELLIPSOID_" + ellipsCount++;
            String description = "Handmade proj4 ellipsoid definition (parsed from nad/epsg) used by crs with id: "
                                 + params.get( "identifier" );
            String name = "Proj4 defined ellipsoid";

            if ( !Double.isNaN( eccentricitySquared ) ) {
                result = new Ellipsoid( semiMajorAxis, Math.sqrt( eccentricitySquared ), Unit.METRE, CRSCodeType.valueOf( id ), name, version,
                                        description, areaOfUse );
            } else if ( !Double.isNaN( eccentricity ) ) {
                result = new Ellipsoid( semiMajorAxis, eccentricity, Unit.METRE, CRSCodeType.valueOf( id ), name, version, description,
                                        areaOfUse );
            } else if ( !Double.isNaN( inverseFlattening ) ) {
                result = new Ellipsoid( semiMajorAxis, Unit.METRE, inverseFlattening, CRSCodeType.valueOf( id ), name, version, description,
                                        areaOfUse );
            } else if ( !Double.isNaN( semiMinorAxis ) ) {
                result = new Ellipsoid( Unit.METRE, semiMajorAxis, semiMinorAxis, CRSCodeType.valueOf( id ), name, version, description,
                                        areaOfUse );
            } else {
                LOG.debug( "Only a semimajor defined, assuming a sphere (instead of an ellipsoid) is to be created." );
                result = new Ellipsoid( Unit.METRE, semiMajorAxis, semiMajorAxis, CRSCodeType.valueOf( id ), name, version, description,
                                        areaOfUse );
            }
        }
        return result;
    }

    /**
     * @param datumName
     *            of the datum to map.
     * @param definedEllipsoid
     *            the ellipsoid to use (gotten from 'ellps' param) or <code>null</code> if a predefined ellipsoid
     *            should be used.
     * @return a Geodetic datum or <code>null</code> if the given name was null or empty.
     * @throws CRSConfigurationException
     */
    private GeodeticDatum getPredefinedDatum( String datumName, String definedEllipsoid, String crsID )
                            throws CRSConfigurationException {
        if ( datumName != null && !"".equals( datumName.trim() ) ) {
            datumName = datumName.trim();
            String[] datumIDs = null;
            CRSCodeType[] datumCodes = null;
            String[] datumNames = null;
            String[] datumDescriptions = null;
            String[] datumVersions = null;
            String[] datumAOU = null;
            Helmert confInfo = new Helmert( GeographicCRS.WGS84, GeographicCRS.WGS84, CRSCodeType.valueOf( "Created by proj4 CRSProvider" ) );
            Ellipsoid ellipsoid = null;
            if ( "GGRS87".equalsIgnoreCase( datumName ) ) {
                String[] ids = getPredefinedIDs( "1272" );
                CRSCodeType[] codes = new CRSCodeType[ ids.length ];
                for ( int i = 0; i < ids.length; i++ )
                    codes[i] = CRSCodeType.valueOf( ids[i] );
                confInfo = new Helmert( -199.87, 74.79, 246.62, 0, 0, 0, 0, GeographicCRS.WGS84, GeographicCRS.WGS84,
                                       codes , new String[] {}, null, null, null );

                datumIDs = getPredefinedIDs( "6121" );
                datumNames = new String[] { "Greek_Geodetic_Reference_System_1987" };
                if ( definedEllipsoid == null || "".equals( definedEllipsoid )
                     || "GRS80".equals( definedEllipsoid.trim() ) ) {
                    ellipsoid = getPredefinedEllipsoid( "GRS80" );
                } else {
                    ellipsoid = getPredefinedEllipsoid( definedEllipsoid );
                }
            } else if ( "NAD27".equalsIgnoreCase( datumName ) ) {
                String[] ids = getPredefinedIDs( "1173" );
                CRSCodeType[] codes = new CRSCodeType[ ids.length ];
                for ( int i = 0; i < ids.length; i++ )
                    codes[i] = CRSCodeType.valueOf( ids[i] );

                confInfo = new Helmert( -8, 160, 176, 0, 0, 0, 0, GeographicCRS.WGS84, GeographicCRS.WGS84,
                                        codes, new String[] { "North_American_Datum_1983" }, null,
                                        null, null );

                datumIDs = getPredefinedIDs( "6267" );
                datumNames = new String[] { "North_American_Datum_1927" };
                // don't no how to do this.
                // "nadgrids=@conus,@alaska,@ntv2_0.gsb,@ntv1_can.dat";
                if ( definedEllipsoid == null || "".equals( definedEllipsoid )
                     || "clrk66".equals( definedEllipsoid.trim() ) ) {
                    ellipsoid = getPredefinedEllipsoid( "clrk66" );
                } else {
                    ellipsoid = getPredefinedEllipsoid( definedEllipsoid );
                }
            } else if ( "NAD83".equalsIgnoreCase( datumName ) ) {
                String[] ids = getPredefinedIDs( "1188" );
                CRSCodeType[] codes = new CRSCodeType[ ids.length ];
                for ( int i = 0; i < ids.length; i++ )
                    codes[i] = CRSCodeType.valueOf( ids[i] );

                confInfo = new Helmert( GeographicCRS.WGS84, GeographicCRS.WGS84, codes, null,
                                        null, new String[] { "Derived at 312 stations." },
                                        new String[] { "North America - all Canada and USA subunits" } );
                datumIDs = getPredefinedIDs( "6269" );
                datumNames = new String[] { "North_American_Datum_1983" };
                if ( definedEllipsoid == null || "".equals( definedEllipsoid )
                     || "GRS80".equals( definedEllipsoid.trim() ) ) {
                    ellipsoid = getPredefinedEllipsoid( "GRS80" );
                } else {
                    ellipsoid = getPredefinedEllipsoid( definedEllipsoid );
                }
            } else if ( "OSGB36".equalsIgnoreCase( datumName ) ) {
                String[] ids = getPredefinedIDs( "1314" );
                CRSCodeType[] codes = new CRSCodeType[ ids.length ];
                for ( int i = 0; i < ids.length; i++ )
                    codes[i] = CRSCodeType.valueOf( ids[i] );

                confInfo = new Helmert(
                                        446.448,
                                        -125.157,
                                        542.060,
                                        0.1502,
                                        0.2470,
                                        0.8421,
                                        -20.4894,
                                        GeographicCRS.WGS84,
                                        GeographicCRS.WGS84,
                                        codes,
                                        null,
                                        null,
                                        new String[] { "For a more accurate transformation see OSGB 1936 / British National Grid to ETRS89 (2) (code 1039): contact the Ordnance Survey of Great Britain (http://www.gps.gov.uk/gpssurveying.asp) for details." },
                                        new String[] { "United Kingdom (UK) - Great Britain and UKCS" } );
                datumIDs = getPredefinedIDs( "6001" );
                datumNames = new String[] { "Airy 1830" };
                if ( definedEllipsoid == null || "".equals( definedEllipsoid )
                     || "airy".equals( definedEllipsoid.trim() ) ) {
                    ellipsoid = getPredefinedEllipsoid( "airy" );
                } else {
                    ellipsoid = getPredefinedEllipsoid( definedEllipsoid );
                }
            } else if ( "WGS84".equalsIgnoreCase( datumName ) ) {
                return GeodeticDatum.WGS84;
            } else if ( "carthage".equalsIgnoreCase( datumName ) ) {
                String[] ids = getPredefinedIDs( "1130" );
                CRSCodeType[] codes = new CRSCodeType[ ids.length ];
                for ( int i = 0; i < ids.length; i++ )
                    codes[i] = CRSCodeType.valueOf( ids[i] );

                confInfo = new Helmert( -263.0, 6.0, 431.0, 0, 0, 0, 0, GeographicCRS.WGS84, GeographicCRS.WGS84,
                                        codes, null, null,
                                        new String[] { "Derived at 5 stations." }, new String[] { "Tunisia" } );
                datumIDs = getPredefinedIDs( "6816" );
                datumNames = new String[] { "Carthage 1934 Tunisia" };
                if ( definedEllipsoid == null || "".equals( definedEllipsoid )
                     || "clark80".equals( definedEllipsoid.trim() ) ) {
                    ellipsoid = getPredefinedEllipsoid( "clark80" );
                } else {
                    ellipsoid = getPredefinedEllipsoid( definedEllipsoid );
                }
            } else if ( "hermannskogel".equalsIgnoreCase( datumName ) ) {
                String[] ids = new String[] { "kogel", EPSG_PRE + "1306" };
                CRSCodeType[] codes = new CRSCodeType[ ids.length ];
                for ( int i = 0; i < ids.length; i++ )
                    codes[i] = CRSCodeType.valueOf( ids[i] );

                confInfo = new Helmert( 653.0, -212.0, 449.0, 0, 0, 0, 0, GeographicCRS.WGS84, GeographicCRS.WGS84,
                                        codes, null, null,
                                        new String[] { "No epsg code was found." }, null );
                datumIDs = new String[] { "Hermannskogel" };
                datumNames = new String[] { "some undefined proj4 datum" };
                if ( definedEllipsoid == null || "".equals( definedEllipsoid )
                     || "bessel".equals( definedEllipsoid.trim() ) ) {
                    ellipsoid = getPredefinedEllipsoid( "bessel" );
                } else {
                    ellipsoid = getPredefinedEllipsoid( definedEllipsoid );
                }
            } else if ( "ire65".equalsIgnoreCase( datumName ) ) {
                String[] ids = new String[] { "ire65_conversion" };
                CRSCodeType[] codes = new CRSCodeType[ ids.length ];
                for ( int i = 0; i < ids.length; i++ )
                    codes[i] = CRSCodeType.valueOf( ids[i] );

                confInfo = new Helmert( 482.530, -130.596, 564.557, -1.042, -0.214, -0.631, 8.15, GeographicCRS.WGS84,
                                        GeographicCRS.WGS84, codes, null, null,
                                        new String[] { "no epsg code was found" }, null );
                datumIDs = new String[] { "Ireland 1965" };
                datumNames = new String[] { "no epsg code was found." };
                if ( definedEllipsoid == null || "".equals( definedEllipsoid )
                     || "mod_airy".equals( definedEllipsoid.trim() ) ) {
                    ellipsoid = getPredefinedEllipsoid( "mod_airy" );
                } else {
                    ellipsoid = getPredefinedEllipsoid( definedEllipsoid );
                }
            } else if ( "nzgd49".equalsIgnoreCase( datumName ) ) {
                String[] ids = getPredefinedIDs( "1564" );
                CRSCodeType[] codes = new CRSCodeType[ ids.length ];
                for ( int i = 0; i < ids.length; i++ )
                    codes[i] = CRSCodeType.valueOf( ids[i] );

                confInfo = new Helmert(
                                        59.47,
                                        -5.04,
                                        187.44,
                                        0.47,
                                        -0.1,
                                        1.024,
                                        -4.5993,
                                        GeographicCRS.WGS84,
                                        GeographicCRS.WGS84,
                                        codes,
                                        new String[] { "NZGD49 to WGS 84 (2)" },
                                        new String[] { "OSG-Nzl 4m" },
                                        new String[] { "hese parameter values are taken from NZGD49 to NZGD2000 (4) (code 1701) and assume that NZGD2000 and WGS 84 are coincident to within the accuracy of the transformation. For improved accuracy use NZGD49 to WGS 84 (4) (code 1670)." },
                                        new String[] { "New Zealand" } );
                datumIDs = getPredefinedIDs( "6272" );
                datumNames = new String[] { "New Zealand Geodetic Datum 1949" };
                if ( definedEllipsoid == null || "".equals( definedEllipsoid )
                     || "intl".equals( definedEllipsoid.trim() ) ) {
                    ellipsoid = getPredefinedEllipsoid( "intl" );
                } else {
                    ellipsoid = getPredefinedEllipsoid( definedEllipsoid );
                }
            } else if ( "potsdam".equalsIgnoreCase( datumName ) ) {
                if ( ( crsID != null && !"".equals( crsID ) ) && "3068".equals( crsID ) || "4314".equals( crsID )
                     || "31466".equals( crsID ) || "31467".equals( crsID ) || "31468".equals( crsID )
                     || "31469".equals( crsID ) ) {
                    String[] ids = getPredefinedIDs( "1777" );
                    CRSCodeType[] codes = new CRSCodeType[ ids.length ];
                    for ( int i = 0; i < ids.length; i++ )
                        codes[i] = CRSCodeType.valueOf( ids[i] );

                    confInfo = new Helmert(
                                            598.1,
                                            73.7,
                                            418.2,
                                            0.202,
                                            0.045,
                                            -2.455,
                                            6.7,
                                            GeographicCRS.WGS84,
                                            GeographicCRS.WGS84,
                                            codes,
                                            new String[] { "DHDN to WGS 84" },
                                            new String[] { "EPSG-Deu W 3m" },
                                            new String[] { "Parameter values from DHDN to ETRS89 (2) (code 1776) assuming that ETRS89 is equivalent to WGS 84 within the accuracy of the transformation. Replaces DHDN to WGS 84 (1) (tfm code 1673)." },
                                            new String[] { "Germany - states of former West Germany - Baden-Wurtemberg, Bayern, Hessen, Niedersachsen, Nordrhein-Westfalen, Rheinland-Pfalz, Saarland, Schleswig-Holstein." } );
                    datumIDs = getPredefinedIDs( "6314" );
                    datumNames = new String[] { "Deutsches Hauptdreiecksnetz" };
                    datumVersions = new String[] { "2006-06-12" };
                    datumDescriptions = new String[] { "Fundamental point: Rauenberg. Latitude: 52 deg 27 min 12.021 sec N; Longitude: 13 deg 22 min 04.928 sec E (of Greenwich). This station was destroyed in 1910 and the station at Potsdam substituted as the fundamental point." };
                    datumAOU = new String[] { "Germany - states of former West Germany - Baden-Wurtemberg, Bayern, Hessen, Niedersachsen, Nordrhein-Westfalen, Rheinland-Pfalz, Saarland, Schleswig-Holstein." };
                } else {
                    String[] ids = getPredefinedIDs( "15955" );
                    CRSCodeType[] codes = new CRSCodeType[ ids.length ];
                    for ( int i = 0; i < ids.length; i++ )
                        codes[i] = CRSCodeType.valueOf( ids[i] );

                    confInfo = new Helmert(
                                            606.0,
                                            23.0,
                                            413.0,
                                            0,
                                            0,
                                            0,
                                            0,
                                            GeographicCRS.WGS84,
                                            GeographicCRS.WGS84,
                                            codes,
                                            new String[] { "RD/83 to WGS 84 (1)" },
                                            new String[] { "OGP-Deu BeTA2007" },
                                            new String[] { "These parameter values are taken from DHDN to ETRS89 (8) (code 15948) as RD/83 and ETRS89 may be considered equivalent to DHDN and WGS 84 respectively within the accuracy of the transformation." },
                                            new String[] { "Germany-Sachsen" } );
                    datumIDs = getPredefinedIDs( "6746" );

                    datumNames = new String[] { "Potsdam Rauenberg 1950 DHDN" };
                }

                if ( definedEllipsoid == null || "".equals( definedEllipsoid )
                     || "bessel".equals( definedEllipsoid.trim() ) ) {
                    ellipsoid = getPredefinedEllipsoid( "bessel" );
                } else {
                    ellipsoid = getPredefinedEllipsoid( definedEllipsoid );
                }
            } else {
                return null;
            }

            // convert datumIDs to datumCodes
            datumCodes = new CRSCodeType[ datumIDs.length ];
            for ( int i = 0; i < datumIDs.length; i++ )
                datumCodes[i] = CRSCodeType.valueOf( datumIDs[i] );

            return new GeodeticDatum( ellipsoid, PrimeMeridian.GREENWICH, confInfo, datumCodes, datumNames,
                                      datumVersions, datumDescriptions, datumAOU );
        }
        return null;
    }

    private String[] getPredefinedIDs( String idNumber ) {
        return new String[] { EPSG_PRE + idNumber, OGC_URN + idNumber, OPENGIS_URL + idNumber, OPENGIS_URN + idNumber };
    }

    /**
     * This method was adopted from the com.jhlabs.map.proj.Projection Factory#initizalize method.
     *
     * @param projName
     *            name of the projection
     * @return the deegree understandable String or <code>null</code> if the projName could not be mapped.
     * @throws CRSConfigurationException
     *             if the projName could not be mapped.
     */
    private Projection createProjection( String projName, GeographicCRS geoCRS, Map<String, String> params )
                            throws CRSConfigurationException {
        Projection result = null;
        // in degrees
        double projectionLatitude = 0;
        double projectionLongitude = 0;
        double firstParallelLatitude = Double.NaN;
        double secondParallelLatitude = Double.NaN;
        double trueScaleLatitude = Double.NaN;

        // meter
        double falseNorthing = 0;
        double falseEasting = 0;
        double scale = 1;

        String s = params.remove( "lat_0" );
        if ( s != null && !"".equals( s.trim() ) ) {
            projectionLatitude = parseAngleFormat( s, false );
        }

        s = params.remove( "lon_0" );
        if ( s != null && !"".equals( s.trim() ) ) {
            projectionLongitude = parseAngleFormat( s, false );
        }
        Point2d naturalOrigin = new Point2d( projectionLongitude, projectionLatitude );

        s = params.remove( "lat_1" );
        if ( s != null && !"".equals( s.trim() ) ) {
            firstParallelLatitude = parseAngleFormat( s, false );
        }

        s = params.remove( "lat_2" );
        if ( s != null && !"".equals( s.trim() ) ) {
            secondParallelLatitude = parseAngleFormat( s, false );
        }

        s = params.remove( "lat_ts" );
        if ( s != null && !"".equals( s.trim() ) ) {
            trueScaleLatitude = parseAngleFormat( s, false );
        }

        s = params.remove( "x_0" );
        if ( s != null && !"".equals( s.trim() ) ) {
            falseEasting = Double.parseDouble( s );
        }
        s = params.remove( "y_0" );
        if ( s != null && !"".equals( s.trim() ) ) {
            falseNorthing = Double.parseDouble( s );
        }

        s = params.remove( "k_0" );
        if ( s == null ) {
            s = params.remove( "k" );
        }
        if ( s != null && !"".equals( s.trim() ) ) {
            scale = Double.parseDouble( s );
        }

        Unit units = createUnit( params );

        if ( projName != null && !"".equals( projName ) ) {
            projName = projName.trim();
            if ( "aea".equals( projName ) ) {// "Albers Equal Area"
            } else if ( "aeqd".equals( projName ) ) {// "Azimuthal Equidistant"
            } else if ( "airy".equals( projName ) ) {// "Airy"
            } else if ( "aitoff".equals( projName ) ) {// "Aitoff"
            } else if ( "alsk".equals( projName ) ) {// "Mod. Stereographics of Alaska"
            } else if ( "apian".equals( projName ) ) {// "Apian Globular I"
            } else if ( "august".equals( projName ) ) {// "August Epicycloidal"
            } else if ( "bacon".equals( projName ) ) {// "Bacon Globular"
            } else if ( "bipc".equals( projName ) ) {// "Bipolar conic of western hemisphere"
            } else if ( "boggs".equals( projName ) ) {// "Boggs Eumorphic"
            } else if ( "bonne".equals( projName ) ) {// "Bonne (Werner lat_1=90)"
            } else if ( "cass".equals( projName ) ) {// "Cassini"
            } else if ( "cc".equals( projName ) ) {// "Central Cylindrical"
            } else if ( "cea".equals( projName ) ) {// "Equal Area Cylindrical"
            } else if ( "chamb".equals( projName ) ) {// "Chamberlin Trimetric"
            } else if ( "collg".equals( projName ) ) {// "Collignon"
            } else if ( "crast".equals( projName ) ) {// "Craster Parabolic (Putnins P4)"
            } else if ( "denoy".equals( projName ) ) {// "Denoyer Semi-Elliptical"
            } else if ( "eck1".equals( projName ) ) {// "Eckert I"
            } else if ( "eck2".equals( projName ) ) {// "Eckert II"
            } else if ( "eck3".equals( projName ) ) {// "Eckert III"
            } else if ( "eck4".equals( projName ) ) {// "Eckert IV"
            } else if ( "eck5".equals( projName ) ) {// "Eckert V"
            } else if ( "eck6".equals( projName ) ) {// "Eckert VI"
            } else if ( "eqc".equals( projName ) ) {// "Equidistant Cylindrical (Plate Caree)"
            } else if ( "eqdc".equals( projName ) ) {// "Equidistant Conic"
            } else if ( "euler".equals( projName ) ) {// "Euler"
            } else if ( "fahey".equals( projName ) ) {// "Fahey"
            } else if ( "fouc".equals( projName ) ) {// "Foucaut"
            } else if ( "fouc_s".equals( projName ) ) {// "Foucaut Sinusoidal"
            } else if ( "gall".equals( projName ) ) {// "Gall (Gall Stereographic)"
            } else if ( "gins8".equals( projName ) ) {// "Ginsburg VIII (TsNIIGAiK)"
            } else if ( "gn_sinu".equals( projName ) ) {// "General Sinusoidal Series"
            } else if ( "gnom".equals( projName ) ) {// "Gnomonic"
            } else if ( "goode".equals( projName ) ) {// "Goode Homolosine"
            } else if ( "gs48".equals( projName ) ) {// "Mod. Stererographics of 48 U.S."
            } else if ( "gs50".equals( projName ) ) {// "Mod. Stererographics of 50 U.S."
            } else if ( "hammer".equals( projName ) ) {// "Hammer & Eckert-Greifendorff"
            } else if ( "hatano".equals( projName ) ) {// "Hatano Asymmetrical Equal Area"
            } else if ( "imw_p".equals( projName ) ) {// "Internation Map of the World Polyconic"
            } else if ( "kav5".equals( projName ) ) {// "Kavraisky V"
            } else if ( "kav7".equals( projName ) ) {// "Kavraisky VII"
            } else if ( "labrd".equals( projName ) ) {// "Laborde"
            } else if ( "laea".equals( projName ) ) {// "Lambert Azimuthal Equal Area"
                result = new LambertAzimuthalEqualArea( geoCRS, falseNorthing, falseEasting, naturalOrigin, units,
                                                        scale );
            } else if ( "lagrng".equals( projName ) ) {// "Lagrange"
            } else if ( "larr".equals( projName ) ) {// "Larrivee"
            } else if ( "lask".equals( projName ) ) {// "Laskowski"
            } else if ( "latlong".equals( projName ) ) {// "Lat/Long"
            } else if ( "lcc".equals( projName ) ) {// "Lambert Conformal Conic"
                result = new LambertConformalConic( firstParallelLatitude, secondParallelLatitude, geoCRS,
                                                    falseNorthing, falseEasting, naturalOrigin, units, scale );
            } else if ( "leac".equals( projName ) ) {// "Lambert Equal Area Conic"
            } else if ( "lee_os".equals( projName ) ) {// "Lee Oblated Stereographic"
            } else if ( "loxim".equals( projName ) ) {// "Loximuthal"
            } else if ( "lsat".equals( projName ) ) {// "Space oblique for LANDSAT"
            } else if ( "mbt_s".equals( projName ) ) {// "McBryde-Thomas Flat-Polar Sine"
            } else if ( "mbt_fps".equals( projName ) ) {// "McBryde-Thomas Flat-Pole Sine (No. 2)"
            } else if ( "mbtfpp".equals( projName ) ) {// "McBride-Thomas Flat-Polar Parabolic"
            } else if ( "mbtfpq".equals( projName ) ) {// "McBryde-Thomas Flat-Polar Quartic"
            } else if ( "mbtfps".equals( projName ) ) {// "McBryde-Thomas Flat-Polar Sinusoidal"
            } else if ( "merc".equals( projName ) ) {// "Mercator"
            } else if ( "mil_os".equals( projName ) ) {// "Miller Oblated Stereographic"
            } else if ( "mill".equals( projName ) ) {// "Miller Cylindrical"
            } else if ( "mpoly".equals( projName ) ) {// "Modified Polyconic"
            } else if ( "moll".equals( projName ) ) {// "Mollweide"
            } else if ( "murd1".equals( projName ) ) {// "Murdoch I"
            } else if ( "murd2".equals( projName ) ) {// "Murdoch II"
            } else if ( "murd3".equals( projName ) ) {// "Murdoch III"
            } else if ( "nell".equals( projName ) ) {// "Nell"
            } else if ( "nell_h".equals( projName ) ) {// "Nell-Hammer"
            } else if ( "nicol".equals( projName ) ) {// "Nicolosi Globular"
            } else if ( "nsper".equals( projName ) ) {// "Near-sided perspective"
            } else if ( "nzmg".equals( projName ) ) {// "New Zealand Map Grid"
            } else if ( "ob_tran".equals( projName ) ) {// "General Oblique Transformation"
            } else if ( "ocea".equals( projName ) ) {// "Oblique Cylindrical Equal Area"
            } else if ( "oea".equals( projName ) ) {// "Oblated Equal Area"
            } else if ( "omerc".equals( projName ) ) {// "Oblique Mercator"
            } else if ( "ortel".equals( projName ) ) {// "Ortelius Oval"
            } else if ( "ortho".equals( projName ) ) {// "Orthographic"
            } else if ( "pconic".equals( projName ) ) {// "Perspective Conic"
            } else if ( "poly".equals( projName ) ) {// "Polyconic (American)"
            } else if ( "putp1".equals( projName ) ) {// "Putnins P1"
            } else if ( "putp2".equals( projName ) ) {// "Putnins P2"
            } else if ( "putp3".equals( projName ) ) {// "Putnins P3"
            } else if ( "putp3p".equals( projName ) ) {// "Putnins P3'"
            } else if ( "putp4p".equals( projName ) ) {// "Putnins P4'"
            } else if ( "putp5".equals( projName ) ) {// "Putnins P5"
            } else if ( "putp5p".equals( projName ) ) {// "Putnins P5'"
            } else if ( "putp6".equals( projName ) ) {// "Putnins P6"
            } else if ( "putp6p".equals( projName ) ) {// "Putnins P6'"
            } else if ( "qua_aut".equals( projName ) ) {// "Quartic Authalic"
            } else if ( "robin".equals( projName ) ) {// "Robinson"
            } else if ( "rpoly".equals( projName ) ) {// "Rectangular Polyconic"
            } else if ( "sinu".equals( projName ) ) {// "Sinusoidal (Sanson-Flamsteed)"
            } else if ( "somerc".equals( projName ) ) {// "Swiss. Obl. Mercator"
            } else if ( "stere".equals( projName ) ) {// "Oblique Stereographic Alternative"
                result = new StereographicAzimuthal( trueScaleLatitude, geoCRS, falseNorthing, falseEasting,
                                                     naturalOrigin, units, scale );
            } else if ( "sterea".equals( projName ) ) {
                result = new StereographicAlternative( geoCRS, falseNorthing, falseEasting, naturalOrigin, units, scale );
            } else if ( "tcc".equals( projName ) ) {// "Transverse Central Cylindrical"
            } else if ( "tcea".equals( projName ) ) {// "Transverse Cylindrical Equal Area"
            } else if ( "tissot".equals( projName ) ) {// "Tissot Conic"
            } else if ( "tmerc".equals( projName ) || "utm".equals( projName ) ) {// "Transverse
                // Mercator"
                s = params.remove( "south" );
                boolean north = ( s == null || "".equals( s.trim() ) );
                s = params.remove( "zone" );
                if ( s != null && !"".equals( s.trim() ) ) {
                    int zone = Integer.parseInt( s );
                    result = new TransverseMercator( zone, north, geoCRS, units );
                } else {
                    result = new TransverseMercator( north, geoCRS, falseNorthing, falseEasting, naturalOrigin, units,
                                                     scale );
                }
            } else if ( "tpeqd".equals( projName ) ) {// "Two Point Equidistant"
            } else if ( "tpers".equals( projName ) ) {// "Tilted perspective"
            } else if ( "ups".equals( projName ) ) {// "Universal Polar Stereographic"
            } else if ( "urm5".equals( projName ) ) {// "Urmaev V"
            } else if ( "urmfps".equals( projName ) ) {// "Urmaev Flat-Polar Sinusoidal"
            } else if ( "utm".equals( projName ) ) {// "Universal Transverse Mercator (UTM)"
            } else if ( "vandg".equals( projName ) ) {// "van der Grinten (I)"
            } else if ( "vandg2".equals( projName ) ) {// "van der Grinten II"
            } else if ( "vandg3".equals( projName ) ) {// "van der Grinten III"
            } else if ( "vandg4".equals( projName ) ) {// "van der Grinten IV"
            } else if ( "vitk1".equals( projName ) ) {// "Vitkovsky I"
            } else if ( "wag1".equals( projName ) ) {// "Wagner I (Kavraisky VI)"
            } else if ( "wag2".equals( projName ) ) {// "Wagner II"
            } else if ( "wag3".equals( projName ) ) {// "Wagner III"
            } else if ( "wag4".equals( projName ) ) {// "Wagner IV"
            } else if ( "wag5".equals( projName ) ) {// "Wagner V"
            } else if ( "wag6".equals( projName ) ) {// "Wagner VI"
            } else if ( "wag7".equals( projName ) ) {// "Wagner VII"
            } else if ( "weren".equals( projName ) ) {// "Werenskiold I"
            } else if ( "wink1".equals( projName ) ) {// "Winkel I"
            } else if ( "wink2".equals( projName ) ) {// "Winkel II"
            } else if ( "wintri".equals( projName ) ) {// "Winkel Tripel"
            }
            if ( result == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJ4_UNKNOWN_PROJECTION",
                                                                          projName ) );
            }
        }
        return result;
    }

    /**
     * @param params
     *            the values to get the units or to_meter from.
     * @return a unit create from the +unit parameter or Unit.METRE if not found.
     * @throws CRSConfigurationException
     *             if the given unit parameter could not be mapped to a valid deegree-crs unit.
     */
    private Unit createUnit( Map<String, String> params )
                            throws CRSConfigurationException {
        Unit result = Unit.METRE;
        String tmpValue = params.remove( "units" );
        if ( tmpValue != null && !"".equals( tmpValue.trim() ) ) {
            result = Unit.createUnitFromString( tmpValue );
            if ( result == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJ4_UNKNOWN_UNIT",
                                                                          params.get( EPSG_PRE + "identifier" ),
                                                                          tmpValue ) );
            }
        } else {
            tmpValue = params.remove( "to_meter" );
            if ( tmpValue != null && !"".equals( tmpValue.trim() ) ) {
                result = new Unit( "Unknown", "unknown", Double.parseDouble( tmpValue ), Unit.METRE );
            }
        }
        return result;
    }

    /**
     * Maps the given proj4 name to an id (if any) and creates an ellipsoid accordingly.
     *
     * @param ellipsoidName
     *            defined in the proj lib
     * @return an ellipsoid with an id and name or <code>null</code> if the ellipsoidName was null or empty.
     * @throws CRSConfigurationException
     *             if the given name could not be mapped.
     */
    private Ellipsoid getPredefinedEllipsoid( String ellipsoidName )
                            throws CRSConfigurationException {
        if ( ellipsoidName != null && !"".equals( ellipsoidName.trim() ) ) {
            ellipsoidName = ellipsoidName.trim();
            double semiMajorAxis = 0;
            double semiMinorAxis = Double.NaN;
            double inverseFlattening = 1;
            String id = ellipsoidName;
            String name = "";
            if ( "APL4.9".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378137.0;
                inverseFlattening = 298.25;
                name = "Appl. Physics. 1965";
                // no epsg
            } else if ( "CPM".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6375738.7;
                inverseFlattening = 334.29;
                name = "Comm. des Poids et Mesures 1799";
                // no epsg
            } else if ( "GRS67".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378160.0;
                inverseFlattening = 298.2471674270;
                name = "GRS 67(IUGG 1967)";
                id = "7036";
            } else if ( "GRS80".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378137.0;
                inverseFlattening = 298.257222101;
                name = "GRS 1980(IUGG, 1980)";
                id = "7019";
            } else if ( "IAU76".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378140.0;
                inverseFlattening = 298.257;
                name = "IAU 1976";
                // probably clarke
                // no epsg
            } else if ( "MERIT".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378137.0;
                inverseFlattening = 298.257;
                name = "MERIT 1983";
                // no epsg
            } else if ( "NWL9D".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378145.0;
                inverseFlattening = 298.25;
                name = "Naval Weapons Lab., 1965";
                // no epsg
            } else if ( "SEasia".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378155.0;
                semiMinorAxis = 6356773.3205;
                name = "Southeast Asia";
                // no epsg
            } else if ( "SGS85".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378136.0;
                inverseFlattening = 298.257;
                name = "Soviet Geodetic System 85";
                // no epsg
            } else if ( "WGS60".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378165.0;
                inverseFlattening = 298.3;
                name = "WGS 60";
                // no epsg
            } else if ( "WGS66".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378145.0;
                inverseFlattening = 298.25;
                name = "WGS 66";
                // no epsg
            } else if ( "WGS72".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378135.0;
                inverseFlattening = 298.26;
                name = "WGS 72";
                id = "7043";
            } else if ( "WGS84".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378137.0;
                inverseFlattening = 298.257223563;
                name = "WGS 84";
                id = "7030";
            } else if ( "airy".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6377563.396;
                semiMinorAxis = 6356256.910;
                name = "Airy 1830";
                id = "7001";
            } else if ( "andrae".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6377104.43;
                inverseFlattening = 300.0;
                name = "Andrae 1876 (Den., Iclnd.)";
                // no epsg
            } else if ( "aust_SA".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378160.0;
                inverseFlattening = 298.25;
                name = "Australian Natl & S. Amer. 1969";
                id = "7050";
            } else if ( "bess_nam".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6377483.865;
                inverseFlattening = 299.1528128;
                name = "Bessel 1841 (Namibia)";
                id = "7046";
            } else if ( "bessel".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6377397.155;
                inverseFlattening = 299.1528128;
                name = "Bessel 1841";
                id = "7004";
            } else if ( "clrk66".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378206.4;
                semiMinorAxis = 6356583.8;
                name = "Clarke 1866";
                id = "7008";
            } else if ( "clrk80".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378249.145;
                inverseFlattening = 293.4663;
                name = "Clarke 1880 mod.";
                id = "7034";
            } else if ( "delmbr".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6376428.;
                inverseFlattening = 311.5;
                name = "Delambre 1810 (Belgium)";
                // epsg closed
            } else if ( "engelis".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378136.05;
                inverseFlattening = 298.2566;
                name = "Engelis 1985";
                // espg closed
            } else if ( "evrst30".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6377276.345;
                inverseFlattening = 300.8017;
                name = "Everest 1830";
                id = "7042";
            } else if ( "evrst48".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6377304.063;
                inverseFlattening = 300.8017;
                name = "Everest 1948";
                id = "7018";
            } else if ( "evrst56".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6377301.243;
                inverseFlattening = 300.8017;
                name = "Everest 1956";
                id = "7044";
            } else if ( "evrst69".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6377295.664;
                inverseFlattening = 300.8017;
                name = "Everest 1969";
                id = "7056";
            } else if ( "evrstSS".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6377298.556;
                inverseFlattening = 300.8017;
                name = "Everest (Sabah & Sarawak)";
                id = "7016";
            } else if ( "fschr60".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378166.;
                inverseFlattening = 298.3;
                name = "Fischer (Mercury Datum) 1960";
                // epsg closed
            } else if ( "fschr60m".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378155.;
                inverseFlattening = 298.3;
                name = "Modified Fischer 1960";
                // epsg closed
            } else if ( "fschr68".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378150.;
                inverseFlattening = 298.3;
                name = "Fischer 1968";
                // epsg closed
            } else if ( "helmert".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378200.;
                inverseFlattening = 298.3;
                name = "Helmert 1906";
                id = "7020";
            } else if ( "hough".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378270.0;
                inverseFlattening = 297.;
                name = "Hough";
                id = "7053";
            } else if ( "intl".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378388.0;
                inverseFlattening = 297.;
                name = "International 1909 (Hayford)";
                id = "7022";
            } else if ( "kaula".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378163.;
                inverseFlattening = 298.24;
                name = "Kaula 1961";
                // no epsg
            } else if ( "krass".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378245.0;
                inverseFlattening = 298.3;
                name = "Krassowsky, 1942";
                id = "7024";
            } else if ( "lerch".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378139.;
                inverseFlattening = 298.257;
                name = "Lerch 1979";
                // no epsg
            } else if ( "mod_airy".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6377340.189;
                semiMinorAxis = 6356034.446;
                name = "Modified Airy";
                id = "7002";
            } else if ( "mprts".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6397300.;
                inverseFlattening = 191.;
                name = "Maupertius 1738";
                // no epsg
            } else if ( "new_intl".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6378157.5;
                semiMinorAxis = 6356772.2;
                name = "New International 1967";
                id = "7036";
            } else if ( "plessis".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6376523.;
                semiMinorAxis = 6355863.;
                name = "Plessis 1817 (France)";
                id = "7027";
            } else if ( "sphere".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6370997.0;
                semiMinorAxis = 6370997.0;
                name = "Normal Sphere (r=6370997)";
            } else if ( "walbeck".equalsIgnoreCase( ellipsoidName ) ) {
                semiMajorAxis = 6376896.0;
                semiMinorAxis = 6355834.8467;
                name = "Walbeck";
                // epsg closed
            } else {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJ4_UNKNOWN_ELLIPSOID",
                                                                          ellipsoidName ) );
            }
//            String[] ids = new String[] { id };
            CRSCodeType[] ids = new EPSGCode[] { new EPSGCode( Integer.parseInt( id ) ) };
            if ( !ellipsoidName.equals( id ) ) {
                ids = new CRSCodeType[] { CRSCodeType.valueOf( EPSG_PRE + id), CRSCodeType.valueOf( OGC_URN + id ), CRSCodeType.valueOf( OPENGIS_URL + id ), CRSCodeType.valueOf( OPENGIS_URN + id ) };
            }
            Ellipsoid ellips = null;
            if ( Double.isNaN( semiMinorAxis ) ) {
                ellips = new Ellipsoid( semiMajorAxis, Unit.METRE, inverseFlattening, ids, new String[] { name }, null,
                                        null, null );
            } else {// semiMinorAxis was given.
                ellips = new Ellipsoid( Unit.METRE, semiMajorAxis, semiMinorAxis, ids, new String[] { name }, null,
                                        null, null );
            }
            return ellips;
        }
        return null;
    }

    /**
     * Helper method to parse day, month second formats. With a little help from com.jhlabs.map.AngleFormat
     *
     * @param text
     *            to pe parsed into degrees (or radians).
     * @param toDegrees
     *            if the given text is in degrees
     * @return a
     */
    private double parseAngleFormat( String text, boolean toDegrees ) {
        double d = 0, m = 0, s = 0;
        double result;
        boolean negate = false;
        int length = text.length();
        if ( length > 0 ) {
            char c = Character.toUpperCase( text.charAt( length - 1 ) );
            switch ( c ) {
            case 'W':
            case 'S':
                negate = true;
            case 'E':
            case 'N':
                text = text.substring( 0, length - 1 );
                break;
            }
        }
        int i = text.indexOf( 'd' );
        if ( i == -1 )
            i = text.indexOf( '\u00b0' );
        if ( i != -1 ) {
            String dd = text.substring( 0, i );
            String mmss = text.substring( i + 1 );
            d = Double.valueOf( dd ).doubleValue();
            i = mmss.indexOf( 'm' );
            if ( i == -1 )
                i = mmss.indexOf( '\'' );
            if ( i != -1 ) {
                if ( i != 0 ) {
                    String mm = mmss.substring( 0, i );
                    m = Double.valueOf( mm ).doubleValue();
                }
                if ( mmss.endsWith( "s" ) || mmss.endsWith( "\"" ) )
                    mmss = mmss.substring( 0, mmss.length() - 1 );
                if ( i != mmss.length() - 1 ) {
                    String ss = mmss.substring( i + 1 );
                    s = Double.valueOf( ss ).doubleValue();
                }
                if ( m < 0 || m > 59 )
                    throw new NumberFormatException( "Minutes must be between 0 and 59" );
                if ( s < 0 || s >= 60 )
                    throw new NumberFormatException( "Seconds must be between 0 and 59" );
            } else if ( i != 0 ) {
                m = Double.valueOf( mmss ).doubleValue();
            }
            if ( toDegrees ) {
                result = dmsToDeg( d, m, s );
            } else {
                result = dmsToRad( d, m, s );
            }
        } else {
            result = Double.parseDouble( text );
            if ( !toDegrees )
                result = Math.toRadians( result );
        }
        if ( negate ) {// South
            result = -result;
        }
        return result;
    }

    /**
     * Converts angle information to radians. With a little help from com.jhlabs.map.MapMath. For negative angles, d
     * should be negative, m & s positive.
     *
     * @param d
     *            days
     * @param m
     *            months
     * @param s
     *            seconds.
     * @return the converted value in radians.
     */
    private double dmsToRad( double d, double m, double s ) {
        if ( d >= 0 ) {
            return ( d + m / 60 + s / 3600 ) * Math.PI / 180.0;
        }
        return ( d - m / 60 - s / 3600 ) * Math.PI / 180.0;
    }

    /**
     * Converts angle information to degrees. With a little help from com.jhlabs.map.MapMath. For negative angles, d
     * should be negative, m & s positive.
     *
     * @param d
     *            days
     * @param m
     *            minutes
     * @param s
     *            seconds.
     * @return the converted value in degrees.
     */
    private double dmsToDeg( double d, double m, double s ) {
        if ( d >= 0 ) {
            return ( d + m / 60 + s / 3600 );
        }
        return ( d - m / 60 - s / 3600 );
    }

    public List<CRSCodeType> getAvailableCRSCodes()
                            throws CRSConfigurationException {
        Set<CRSCodeType> keys = getResolver().getAvailableCodes();
        List<CRSCodeType> result = new LinkedList<CRSCodeType>();
        result.addAll( keys );
        return result;
    }

    public CRSIdentifiable getIdentifiable( CRSCodeType code )
                            throws CRSConfigurationException {
        CRSIdentifiable result = getCachedIdentifiable( code );
        if ( result == null ) {
            throw new UnsupportedOperationException(
                                                     "The retrieval of an arbitrary CRSIdentifiable Object is currently not supported by the proj 4 provider." );
        }
        return result;
    }

    @Override
    protected CoordinateSystem parseCoordinateSystem( Map<String, String> crsDefinition )
                            throws CRSConfigurationException {

        String crsType = crsDefinition.remove( "proj" );
        if ( crsType == null || "".equals( crsType.trim() ) ) {
            LOG.debug( "The given params contain: " + crsDefinition );
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJ4_NO_PROJ_PARAM",
                                                                      crsDefinition.get( EPSG_PRE + "identifier" ) ) );
        }
        crsType = crsType.trim();
        if ( "longlat".equals( crsType ) ) {
            // the geo-crs should find it's own id and has no parent projected crs (null, null).
            return createGeographicCRS( null, null, crsDefinition );
        }
        return createProjectedCRS( crsType, crsDefinition );
    }

    @Override
    public Transformation parseTransformation( Map<String, String> transformationDefinition )
                            throws CRSConfigurationException {
        throw new UnsupportedOperationException(
                                                 "Parsing of transformation parameters is not applicable for proj4 configuration files yet." );
    }

    public Transformation getTransformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS )
                            throws CRSConfigurationException {
        return getResolver().getTransformation( sourceCRS, targetCRS );
    }

}
