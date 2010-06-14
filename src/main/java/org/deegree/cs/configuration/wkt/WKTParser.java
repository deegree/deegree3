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

package org.deegree.cs.configuration.wkt;

import static org.deegree.cs.projections.SupportedProjectionParameters.FALSE_EASTING;
import static org.deegree.cs.projections.SupportedProjectionParameters.FALSE_NORTHING;
import static org.deegree.cs.projections.SupportedProjectionParameters.FIRST_PARALLEL_LATITUDE;
import static org.deegree.cs.projections.SupportedProjectionParameters.LATITUDE_OF_NATURAL_ORIGIN;
import static org.deegree.cs.projections.SupportedProjectionParameters.LONGITUDE_OF_NATURAL_ORIGIN;
import static org.deegree.cs.projections.SupportedProjectionParameters.SCALE_AT_NATURAL_ORIGIN;
import static org.deegree.cs.projections.SupportedProjectionParameters.SECOND_PARALLEL_LATITUDE;
import static org.deegree.cs.utilities.ProjectionUtils.DTR;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.Ellipsoid;
import org.deegree.cs.components.GeodeticDatum;
import org.deegree.cs.components.PrimeMeridian;
import org.deegree.cs.components.Unit;
import org.deegree.cs.components.VerticalDatum;
import org.deegree.cs.coordinatesystems.CompoundCRS;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.coordinatesystems.GeocentricCRS;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ProjectedCRS;
import org.deegree.cs.coordinatesystems.VerticalCRS;
import org.deegree.cs.exceptions.UnknownUnitException;
import org.deegree.cs.exceptions.WKTParsingException;
import org.deegree.cs.projections.SupportedProjectionParameters;
import org.deegree.cs.projections.azimuthal.StereographicAlternative;
import org.deegree.cs.projections.azimuthal.StereographicAzimuthal;
import org.deegree.cs.projections.conic.LambertConformalConic;
import org.deegree.cs.projections.cylindric.TransverseMercator;
import org.deegree.cs.transformations.helmert.Helmert;
import org.slf4j.Logger;

/**
 * The <code>WKTParser</code> class instantiates the Coordinate System given in a file, in WKT (Well Known Text) format.
 * The extendend Backus-Naur grammar of WKT as well as a detailed reference are available at the <a
 * href="http://www.opengeospatial.org/standards/ct">OGC website</a>.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class WKTParser {

    private static final Logger LOG = getLogger( WKTParser.class );

    private StreamTokenizer tokenizer;

    private Reader buff;

    /**
     * 
     * @param candidate
     * @param paramName
     * @return true if the candidate stripped of underscores equals ignore case the param name (also stripped).
     */
    protected boolean equalsParameterVariants( String candidate, String paramName ) {
        String candidateVariant = candidate.replace( "_", "" );
        String paramVariant = paramName.replaceAll( "_", "" );
        if ( candidateVariant.equalsIgnoreCase( paramVariant ) ) {
            return true;
        }
        return false;
    }

    protected String makeInvariantKey( String candidate ) {
        return candidate.replaceAll( "_", "" ).toLowerCase();
    }

    /**
     * Walk a character (comma or round/square bracket).
     * 
     * @param ch
     * @throws IOException
     *             if an I/O error occurs.
     * @throws WKTParsingException
     *             if the expected character is not present at this position.
     */
    protected void passOverChar( char ch )
                            throws IOException {
        tokenizer.nextToken();
        if ( tokenizer.ttype != ch ) {
            throw new WKTParsingException( "The tokenizer expects the character " + ch + " while the current token is "
                                           + tokenizer.toString() );
        }
    }

    /**
     * Walk an opening bracket (round or square).
     * 
     * @throws IOException
     *             if an I/O error occurs.
     * @throws WKTParsingException
     *             if the opening bracket is not present at this position.
     */
    protected void passOverOpeningBracket()
                            throws IOException {
        tokenizer.nextToken();
        if ( tokenizer.ttype != '[' && tokenizer.ttype != '(' ) {
            throw new WKTParsingException(
                                           "The tokenizer expects an opening square/round bracket while the current token is "
                                                                   + tokenizer.toString() );
        }
    }

    /**
     * Walk a closing bracket (round or square).
     * 
     * @throws IOException
     *             if an I/O error occurs.
     * @throws WKTParsingException
     *             if the closing bracket is not present at this position.
     */
    protected void passOverClosingBracket()
                            throws IOException {
        tokenizer.nextToken();
        if ( tokenizer.ttype != ']' && tokenizer.ttype != ')' )
            throw new WKTParsingException(
                                           "The tokenizer expects a closing square/round bracket while the current token is "
                                                                   + tokenizer.toString() );
    }

    /**
     * Walk a WKT keyword element (e.g. DATUM, AUTHORITY, UNIT, etc.)
     * 
     * @param s
     *            the keyword element as a String
     * @throws IOException
     *             if an I/O error occurs.
     * @throws WKTParsingException
     *             if the keyword is not present at this position.
     */
    protected void passOverWord( String s )
                            throws IOException {
        tokenizer.nextToken();
        if ( tokenizer.sval == null || !tokenizer.sval.equalsIgnoreCase( s ) )
            throw new WKTParsingException( "The tokenizer expects the word " + s + " while the current token is "
                                           + tokenizer.toString() );
    }

    /**
     * @return a CRSCodeType object based on the information in the AUTHORITY element
     * @throws IOException
     *             if an I/O error occurs
     */
    protected CRSCodeType parseAuthority()
                            throws IOException {
        passOverWord( "AUTHORITY" );
        passOverOpeningBracket();
        String codespace = parseString();
        passOverChar( ',' );
        String code = parseString();
        passOverClosingBracket();
        return new CRSCodeType( code, codespace );
    }

    /**
     * @return the string that is surrounded by double-quotes
     * @throws IOException
     *             if an I/O error occurs
     * @throws WKTParsingException
     *             if the string does not begin with have an opening double-quote.
     */
    protected String parseString()
                            throws IOException {
        tokenizer.nextToken();
        if ( tokenizer.ttype != '"' )
            throw new WKTParsingException( "The tokenizer expects the opening double quote while the current token is "
                                           + tokenizer.toString() );
        return tokenizer.sval;
    }

    /**
     * @return an Axis
     * @throws IOException
     * @throws WKTParsingException
     *             if the axis orientation is not one of the values defined in the WKT reference ( NORTH | SOUTH | WEST
     *             | EAST | UP | DOWN | OTHER )
     */
    protected Axis parseAxis()
                            throws IOException {
        passOverWord( "AXIS" );
        passOverOpeningBracket();
        String name = parseString();
        passOverChar( ',' );
        tokenizer.nextToken();
        String orientation = tokenizer.sval;
        if ( !( orientation.equalsIgnoreCase( "NORTH" ) || orientation.equalsIgnoreCase( "SOUTH" )
                || orientation.equalsIgnoreCase( "WEST" ) || orientation.equalsIgnoreCase( "EAST" )
                || orientation.equalsIgnoreCase( "UP" ) || orientation.equalsIgnoreCase( "DOWN" ) || orientation.equalsIgnoreCase( "OTHER" ) ) )
            throw new WKTParsingException(
                                           "The tokenizer expects a valid Axis Orientation: NORTH | SOUTH | WEST | EAST | UP | DOWN | OTHER. The current token is "
                                                                   + tokenizer.toString() );
        passOverClosingBracket();
        return new Axis( name, "AO_" + orientation );
    }

    /**
     * @return a Unit.
     * @throws IOException
     * @throws UnknownUnitException
     *             if the unit name does not match any of the predefined units in the API
     */
    protected Unit parseUnit()
                            throws IOException {
        passOverWord( "UNIT" );
        passOverOpeningBracket();
        String name = null;
        // Double conversionFactor = null; // we do not identify a unit based on the conversion factor, only on the name
        // CRSCodeType code = CRSCodeType.getUndefined(); // currently Units are not identifiable so the code parsed
        // here is of no use
        while ( true ) {
            tokenizer.nextToken();
            switch ( tokenizer.ttype ) {
            case '"':
                name = tokenizer.sval;
                break;
            case StreamTokenizer.TT_NUMBER:
                // conversionFactor = tokenizer.nval;
                break;
            case StreamTokenizer.TT_WORD:
                if ( tokenizer.sval.equalsIgnoreCase( "AUTHORITY" ) ) {
                    tokenizer.pushBack();
                    // code = parseAuthority();
                } else
                    throw new WKTParsingException( "Unknown word encountered in the UNIT element: " + tokenizer );
                break;
            default:
                throw new WKTParsingException( "Unknown token encountered in the UNIT element: " + tokenizer );
            }
            tokenizer.nextToken();
            if ( tokenizer.ttype == ']' || tokenizer.ttype == ')' )
                break;
        }
        if ( name == null ) {
            throw new UnknownUnitException( "Unit name is missing" );
        }
        return Unit.createUnitFromString( name );
    }

    /**
     * @return a Prime Meridian
     * @throws IOException
     */
    protected PrimeMeridian parsePrimeMeridian()
                            throws IOException {
        passOverWord( "PRIMEM" );
        passOverOpeningBracket();
        String name = null;
        Double longitude = null;
        CRSCodeType code = CRSCodeType.getUndefined();
        while ( true ) {
            tokenizer.nextToken();
            switch ( tokenizer.ttype ) {
            case '"':
                name = tokenizer.sval;
                break;
            case StreamTokenizer.TT_NUMBER:
                longitude = tokenizer.nval;
                break;
            case StreamTokenizer.TT_WORD:
                if ( tokenizer.sval.equalsIgnoreCase( "AUTHORITY" ) ) {
                    tokenizer.pushBack();
                    code = parseAuthority();
                } else
                    throw new WKTParsingException( "Unknown word encountered in the PRIMEM element: " + tokenizer );
                break;
            default:
                throw new WKTParsingException( "Unknown token encountered in the PRIMEM element: " + tokenizer );
            }
            tokenizer.nextToken();
            if ( tokenizer.ttype == ']' || tokenizer.ttype == ')' )
                break;
        }
        if ( longitude == null )
            throw new WKTParsingException( "The PRIMEM element must containt the longitude paramaeter. Before line  "
                                           + tokenizer.lineno() );

        if ( code.equals( CRSCodeType.getUndefined() ) ) {
            code = new CRSCodeType( name );
        }
        return new PrimeMeridian( Unit.RADIAN /* temporarily, until parsing the Unit of the wrapping CRS */, longitude,
                                  new CRSIdentifiable( new CRSCodeType[] { code }, new String[] { name }, null, null,
                                                       null ) );
    }

    /**
     * @return the Ellipsoid parsed from a WKT 'SPHEROID'.
     * @throws IOException
     */
    protected Ellipsoid parseEllipsoid()
                            throws IOException {
        passOverWord( "SPHEROID" );
        passOverOpeningBracket();
        String name = null;
        Double semiMajorAxis = null;
        Double inverseFlattening = null;
        CRSCodeType code = CRSCodeType.getUndefined();
        while ( true ) {
            tokenizer.nextToken();
            switch ( tokenizer.ttype ) {
            case '"':
                name = tokenizer.sval;
                break;
            case StreamTokenizer.TT_NUMBER:
                semiMajorAxis = tokenizer.nval;
                passOverChar( ',' );
                tokenizer.nextToken();
                inverseFlattening = tokenizer.nval;
                break;
            case StreamTokenizer.TT_WORD:
                if ( tokenizer.sval.equalsIgnoreCase( "AUTHORITY" ) ) {
                    tokenizer.pushBack();
                    code = parseAuthority();
                } else
                    throw new WKTParsingException( "Unknown word encountered in the SPHEROID element: " + tokenizer );
                break;
            default:
                throw new WKTParsingException( "Unknown token encountered in the SPHEROID element: " + tokenizer );
            }
            tokenizer.nextToken();
            if ( tokenizer.ttype == ']' || tokenizer.ttype == ')' )
                break;
        }
        if ( semiMajorAxis == null || inverseFlattening == null )
            throw new WKTParsingException(
                                           "Te SPHEROID element must contain the semi-major axis and inverse flattening parameters. Before line "
                                                                   + tokenizer.lineno() );

        if ( code.equals( CRSCodeType.getUndefined() ) ) {
            code = new CRSCodeType( name );
        }
        return new Ellipsoid( semiMajorAxis, Unit.METRE, inverseFlattening,
                              new CRSIdentifiable( new CRSCodeType[] { code }, new String[] { name }, null, null, null ) );
    }

    /**
     * @return the Helmert transformatio parsed from the WKT 'TOWGS84'
     * @throws IOException
     */
    protected Helmert parseHelmert()
                            throws IOException {
        passOverWord( "TOWGS84" );
        passOverOpeningBracket();
        Double dx = null;
        Double dy = null;
        Double dz = null;
        Double ex = null;
        Double ey = null;
        Double ez = null;
        Double ppm = null;
        CRSCodeType code = CRSCodeType.getUndefined();
        while ( true ) {
            tokenizer.nextToken();
            switch ( tokenizer.ttype ) {
            case StreamTokenizer.TT_NUMBER:
                dx = tokenizer.nval;
                passOverChar( ',' );
                tokenizer.nextToken();
                dy = tokenizer.nval;
                passOverChar( ',' );
                tokenizer.nextToken();
                dz = tokenizer.nval;
                passOverChar( ',' );
                tokenizer.nextToken();
                ex = tokenizer.nval;
                passOverChar( ',' );
                tokenizer.nextToken();
                ey = tokenizer.nval;
                passOverChar( ',' );
                tokenizer.nextToken();
                ez = tokenizer.nval;
                passOverChar( ',' );
                tokenizer.nextToken();
                ppm = tokenizer.nval;
                break;
            case StreamTokenizer.TT_WORD:
                if ( tokenizer.sval.equalsIgnoreCase( "AUTHORITY" ) ) {
                    tokenizer.pushBack();
                    code = parseAuthority();
                } else
                    throw new WKTParsingException( "The TOWGS84 contains an unknown keyword: " + tokenizer.sval
                                                   + " at line " + tokenizer.lineno() );
                break;
            default:
                throw new WKTParsingException( "The TOWGS84 contains an unknown keyword: " + tokenizer );
            }
            tokenizer.nextToken();
            if ( tokenizer.ttype == ']' || tokenizer.ttype == ')' )
                break;
        }
        if ( dx == null || dy == null || dz == null || ex == null || ey == null || ez == null || ppm == null )
            throw new WKTParsingException( "The TOWGS84 must contain all 7 parameters." );
        return new Helmert( dx, dy, dz, ex, ey, ez, ppm, null, GeographicCRS.WGS84, code );
    }

    /**
     * @return a Geodetic Datum parsed from WKT 'DATUM'
     * @throws IOException
     */
    protected GeodeticDatum parseGeodeticDatum()
                            throws IOException {
        passOverWord( "DATUM" );
        passOverOpeningBracket();
        String name = null;
        Ellipsoid ellipsoid = null;
        Helmert helmert = new Helmert( null, GeographicCRS.WGS84, CRSCodeType.getUndefined() ); // undefined Helmert
        // transformation
        CRSCodeType code = CRSCodeType.getUndefined();
        while ( true ) {
            tokenizer.nextToken();
            switch ( tokenizer.ttype ) {
            case '"':
                name = tokenizer.sval;
                break;
            case StreamTokenizer.TT_WORD:
                if ( tokenizer.sval.equalsIgnoreCase( "SPHEROID" ) ) {
                    tokenizer.pushBack();
                    ellipsoid = parseEllipsoid();
                } else if ( tokenizer.sval.equalsIgnoreCase( "TOWGS84" ) ) {
                    tokenizer.pushBack();
                    helmert = parseHelmert();
                } else if ( tokenizer.sval.equalsIgnoreCase( "AUTHORITY" ) ) {
                    tokenizer.pushBack();
                    code = parseAuthority();
                } else
                    throw new WKTParsingException( "The DATUM contains an unknown keyword: " + tokenizer.sval
                                                   + " at line " + tokenizer.lineno() );
                break;
            default:
                throw new WKTParsingException( "The DATUM contains an unknown keyword: " + tokenizer + " at line "
                                               + tokenizer.lineno() );
            }
            tokenizer.nextToken();
            if ( tokenizer.ttype == ']' || tokenizer.ttype == ')' )
                break;
        }
        if ( ellipsoid == null )
            throw new WKTParsingException( "The DATUM element must contain a SPHEROID. Before line "
                                           + tokenizer.lineno() );

        if ( code.equals( CRSCodeType.getUndefined() ) ) {
            code = new CRSCodeType( name );
        }
        return new GeodeticDatum( ellipsoid, helmert, code, name );
    }

    /**
     * @return a Vertical Datum parsed from WKT 'VERT_DATUM'
     * @throws IOException
     */
    protected VerticalDatum parseVerticalDatum()
                            throws IOException {
        passOverWord( "VERT_DATUM" );
        passOverOpeningBracket();
        String name = null;
        // Double datumType = null; // cannot find its use!
        CRSCodeType code = CRSCodeType.getUndefined();
        while ( true ) {
            tokenizer.nextToken();
            switch ( tokenizer.ttype ) {
            case '"':
                name = tokenizer.sval;
                break;
            case StreamTokenizer.TT_WORD:
                if ( tokenizer.sval.equalsIgnoreCase( "AUTHORITY" ) ) {
                    tokenizer.pushBack();
                    code = parseAuthority();
                } else
                    throw new WKTParsingException( "The VERT_DATUM contains an unknown keyword: " + tokenizer.sval
                                                   + " at line " + tokenizer.lineno() );
                break;
            case StreamTokenizer.TT_NUMBER:
                // datumType = tokenizer.nval;
                break;
            default:
                throw new WKTParsingException( "The VERT_DATUM contains an unknown token: " + tokenizer );
            }
            tokenizer.nextToken();
            if ( tokenizer.ttype == ']' || tokenizer.ttype == ')' )
                break;
        }
        if ( name == null ) {
            throw new WKTParsingException(
                                           "The VERT_DATUM element must contain a name as a quoted String. Before line "
                                                                   + tokenizer.lineno() );
        }
        if ( code.equals( CRSCodeType.getUndefined() ) ) {
            code = new CRSCodeType( name );
        }

        return new VerticalDatum( code, name, null, null, null );
    }

    private CoordinateSystem realParseCoordinateSystem()
                            throws IOException {
        tokenizer.nextToken();
        String crsType = tokenizer.sval; // expecting StreamTokenizer.TT_WORD

        // COMPOUND CRS
        if ( equalsParameterVariants( crsType, "COMPD_CS" ) ) {
            return parseCompoundCRS();

            // PROJECTED CRS
        } else if ( equalsParameterVariants( crsType, "PROJ_CS" ) ) {
            return parseProjectedCRS();

            // GEOGRAPHIC CRS
        } else if ( equalsParameterVariants( crsType, "GEOG_CS" ) ) {
            return parseGeographiCRS();

            // GEOCENTRIC CRS
        } else if ( equalsParameterVariants( crsType, "GEOC_CS" ) ) {
            return parseGeocentricCRS();

            // VERTICAL CRS
        } else if ( equalsParameterVariants( crsType, "VERT_CS" ) ) {
            return parseVerticalCRS();

        } else
            throw new WKTParsingException( "Expected a CRS element but an unknown keyword was encountered: "
                                           + tokenizer.sval + " at line " + tokenizer.lineno() );
    }

    /**
     * @return a {@link VerticalCRS} parsed from the current reader position.
     * @throws IOException
     */
    protected VerticalCRS parseVerticalCRS()
                            throws IOException {
        passOverOpeningBracket();
        String name = null;
        VerticalDatum verticalDatum = null;
        Unit unit = null;
        Axis axis = new Axis( "", Axis.AO_OTHER ); // in case there is no axis defined
        CRSCodeType code = CRSCodeType.getUndefined();
        while ( true ) {
            tokenizer.nextToken();
            switch ( tokenizer.ttype ) {
            case '"':
                name = tokenizer.sval;
                break;
            case StreamTokenizer.TT_WORD:
                if ( tokenizer.sval.equalsIgnoreCase( "VERT_DATUM" ) ) {
                    tokenizer.pushBack();
                    verticalDatum = parseVerticalDatum();
                } else if ( tokenizer.sval.equalsIgnoreCase( "UNIT" ) ) {
                    tokenizer.pushBack();
                    unit = parseUnit();
                } else if ( tokenizer.sval.equalsIgnoreCase( "AXIS" ) ) {
                    tokenizer.pushBack();
                    axis = parseAxis();
                } else if ( tokenizer.sval.equalsIgnoreCase( "AUTHORITY" ) ) {
                    tokenizer.pushBack();
                    code = parseAuthority();
                } else
                    throw new WKTParsingException( "An unexpected keyword was encountered in the GEOCCS element: "
                                                   + tokenizer.sval + ". At line: " + tokenizer.lineno() );
                break;

            default:
                throw new WKTParsingException( "The VERT_CS contains an unknown token: " + tokenizer + " at line "
                                               + tokenizer.lineno() );
            }
            tokenizer.nextToken();
            if ( tokenizer.ttype == ']' || tokenizer.ttype == ')' )
                break;
        }
        if ( unit == null )
            throw new WKTParsingException( "The VERT_CS element must contain a UNIT keyword element. Before line "
                                           + tokenizer.lineno() );
        if ( verticalDatum == null )
            throw new WKTParsingException( "The VERT_CS element must contain a VERT_DATUM. Before line "
                                           + tokenizer.lineno() );

        if ( code.equals( CRSCodeType.getUndefined() ) ) {
            code = new CRSCodeType( name );
        }
        return new VerticalCRS( verticalDatum, new Axis[] { axis }, new CRSIdentifiable( new CRSCodeType[] { code },
                                                                                         new String[] { name }, null,
                                                                                         null, null ) );
    }

    /**
     * @return a {@link GeocentricCRS} parsed from the current reader position.
     * @throws IOException
     */
    protected GeocentricCRS parseGeocentricCRS()
                            throws IOException {
        passOverOpeningBracket();
        String name = null;
        GeodeticDatum datum = null;
        PrimeMeridian pm = null;
        Unit unit = null;
        // the default values of GEOCCS axes, based on the OGC specification
        Axis axis1 = new Axis( "X", Axis.AO_OTHER );
        Axis axis2 = new Axis( "Y", Axis.AO_EAST );
        Axis axis3 = new Axis( "Z", Axis.AO_NORTH );
        CRSCodeType code = CRSCodeType.getUndefined();
        while ( true ) {
            tokenizer.nextToken();
            switch ( tokenizer.ttype ) {
            case '"':
                name = tokenizer.sval;
                break;
            case StreamTokenizer.TT_WORD:
                if ( tokenizer.sval.equalsIgnoreCase( "DATUM" ) ) {
                    tokenizer.pushBack();
                    datum = parseGeodeticDatum();
                } else if ( tokenizer.sval.equalsIgnoreCase( "PRIMEM" ) ) {
                    tokenizer.pushBack();
                    pm = parsePrimeMeridian();
                } else if ( tokenizer.sval.equalsIgnoreCase( "UNIT" ) ) {
                    tokenizer.pushBack();
                    unit = parseUnit();
                } else if ( tokenizer.sval.equalsIgnoreCase( "AXIS" ) ) {
                    tokenizer.pushBack();
                    axis1 = parseAxis();
                    passOverChar( ',' );
                    axis2 = parseAxis();
                    passOverChar( ',' );
                    axis3 = parseAxis();
                } else if ( tokenizer.sval.equalsIgnoreCase( "AUTHORITY" ) ) {
                    tokenizer.pushBack();
                    code = parseAuthority();
                } else
                    throw new WKTParsingException( "An unexpected keyword was encountered in the GEOCCS element: "
                                                   + tokenizer.sval + ". At line: " + tokenizer.lineno() );
                break;

            default:
                throw new WKTParsingException( "The GEOCCS contains an unknown token: " + tokenizer + " at line "
                                               + tokenizer.lineno() );
            }
            tokenizer.nextToken();
            if ( tokenizer.ttype == ']' || tokenizer.ttype == ')' )
                break;
        }
        if ( unit == null )
            throw new WKTParsingException( "The GEOCCS element must contain a UNIT keyword element. Before line "
                                           + tokenizer.lineno() );
        if ( datum == null )
            throw new WKTParsingException( "The GEOCCS element must contain a DATUM. Before line " + tokenizer.lineno() );
        if ( pm == null )
            throw new WKTParsingException( "The GEOCCS element must contain a PRIMEM. Before line "
                                           + tokenizer.lineno() );

        pm.setAngularUnit( unit );
        datum.setPrimeMeridian( pm );

        if ( code.equals( CRSCodeType.getUndefined() ) ) {
            code = new CRSCodeType( name );
        }
        return new GeocentricCRS( datum, new Axis[] { axis1, axis2, axis3 },
                                  new CRSIdentifiable( new CRSCodeType[] { code }, new String[] { name }, null, null,
                                                       null ) );
    }

    /**
     * @return a {@link GeographicCRS} parsed from the current reader position.
     * @throws IOException
     */
    protected GeographicCRS parseGeographiCRS()
                            throws IOException {
        passOverOpeningBracket();
        String name = null;
        GeodeticDatum datum = null;
        PrimeMeridian pm = null;
        Unit unit = null;
        Axis axis1 = new Axis( Unit.DEGREE, "Lon", Axis.AO_EAST );
        Axis axis2 = new Axis( Unit.DEGREE, "Lat", Axis.AO_NORTH );
        CRSCodeType code = CRSCodeType.getUndefined();
        while ( true ) {
            tokenizer.nextToken();
            switch ( tokenizer.ttype ) {
            case '"':
                name = tokenizer.sval;
                break;
            case StreamTokenizer.TT_WORD:
                if ( tokenizer.sval.equalsIgnoreCase( "DATUM" ) ) {
                    tokenizer.pushBack();
                    datum = parseGeodeticDatum();
                } else if ( tokenizer.sval.equalsIgnoreCase( "PRIMEM" ) ) {
                    tokenizer.pushBack();
                    pm = parsePrimeMeridian();
                } else if ( tokenizer.sval.equalsIgnoreCase( "UNIT" ) ) {
                    tokenizer.pushBack();
                    unit = parseUnit();
                } else if ( tokenizer.sval.equalsIgnoreCase( "AXIS" ) ) {
                    tokenizer.pushBack();
                    axis1 = parseAxis();
                    passOverChar( ',' );
                    axis2 = parseAxis();
                } else if ( tokenizer.sval.equalsIgnoreCase( "AUTHORITY" ) ) {
                    tokenizer.pushBack();
                    code = parseAuthority();
                } else
                    throw new WKTParsingException( "An unexpected keyword was encountered in the GEOGCS element: "
                                                   + tokenizer.sval + ". At line: " + tokenizer.lineno() );
                break;

            default:
                throw new WKTParsingException( "The GEOGCS contains an unknown token: " + tokenizer + " at line "
                                               + tokenizer.lineno() );
            }
            tokenizer.nextToken();
            if ( tokenizer.ttype == ']' || tokenizer.ttype == ')' )
                break;
        }
        if ( name == null )
            throw new WKTParsingException( "The GEOGCS element must contain a name as a quoted String. Before line "
                                           + tokenizer.lineno() );
        if ( unit == null )
            throw new WKTParsingException( "The GEOGCS element must contain a UNIT keyword element. Before line "
                                           + tokenizer.lineno() );
        if ( datum == null )
            throw new WKTParsingException( "The GEOGCS element must contain a DATUM. Before line " + tokenizer.lineno() );
        if ( pm == null )
            throw new WKTParsingException( "The GEOGCS element must contain a PRIMEM. Before line "
                                           + tokenizer.lineno() );

        pm.setAngularUnit( unit );
        datum.setPrimeMeridian( pm );

        if ( code.equals( CRSCodeType.getUndefined() ) ) {
            code = new CRSCodeType( name );
        }
        return new GeographicCRS( datum, new Axis[] { axis1, axis2 }, new CRSIdentifiable( new CRSCodeType[] { code },
                                                                                           new String[] { name }, null,
                                                                                           null, null ) );
    }

    /**
     * @return a {@link ProjectedCRS} parsed from the current reader position.
     * @throws IOException
     */
    protected ProjectedCRS parseProjectedCRS()
                            throws IOException {
        passOverOpeningBracket();
        String name = null;
        GeographicCRS geographicCRS = null;
        String projectionType = null;
        CRSCodeType projectionCode = CRSCodeType.getUndefined();
        Map<String, Double> params = new HashMap<String, Double>();
        Unit unit = null;
        Axis axis1 = new Axis( "X", Axis.AO_EAST ); // the default values for PROJCS axes, based on the OGC
        // specification
        Axis axis2 = new Axis( "Y", Axis.AO_NORTH );
        CRSCodeType code = CRSCodeType.getUndefined();
        while ( true ) {
            tokenizer.nextToken();
            switch ( tokenizer.ttype ) {
            case '"':
                name = tokenizer.sval;
                break;
            case StreamTokenizer.TT_WORD:
                if ( equalsParameterVariants( tokenizer.sval, "GEOG_CS" ) ) {
                    tokenizer.pushBack();
                    geographicCRS = (GeographicCRS) realParseCoordinateSystem();
                } else if ( tokenizer.sval.equalsIgnoreCase( "PROJECTION" ) ) {
                    passOverOpeningBracket();
                    tokenizer.nextToken();
                    if ( tokenizer.ttype != '"' ) {
                        throw new WKTParsingException( "The PROJECTION element must contain a quoted String. At line "
                                                       + tokenizer.lineno() );
                    }
                    projectionType = tokenizer.sval;
                    tokenizer.nextToken();
                    if ( tokenizer.ttype == ',' ) {
                        projectionCode = parseAuthority();
                        tokenizer.nextToken();
                    }
                    tokenizer.pushBack();
                    passOverClosingBracket();
                } else if ( tokenizer.sval.equalsIgnoreCase( "PARAMETER" ) ) {
                    passOverOpeningBracket();
                    tokenizer.nextToken();
                    if ( tokenizer.ttype != '"' ) {
                        String msg = "The PARAMETER element must contain a quoted String as parameter name. At line "
                                     + tokenizer.lineno();
                        throw new WKTParsingException( msg );
                    }

                    CRSCodeType crscode = new CRSCodeType( makeInvariantKey( tokenizer.sval ) );
                    String paramName = SupportedProjectionParameters.fromCodes( new CRSCodeType[] { crscode } ).toString();
                    passOverChar( ',' );
                    tokenizer.nextToken();
                    if ( tokenizer.ttype != StreamTokenizer.TT_NUMBER ) {
                        String msg = "The PARAMETER element must contain a number as parameter value. At line "
                                     + tokenizer.lineno();
                        throw new WKTParsingException( msg );
                    }
                    Double paramValue = tokenizer.nval;
                    params.put( paramName, paramValue );
                    passOverClosingBracket();
                } else if ( tokenizer.sval.equalsIgnoreCase( "AXIS" ) ) {
                    tokenizer.pushBack();
                    axis1 = parseAxis();
                    passOverChar( ',' );
                    axis2 = parseAxis();
                } else if ( tokenizer.sval.equalsIgnoreCase( "UNIT" ) ) {
                    tokenizer.pushBack();
                    unit = parseUnit();
                } else if ( tokenizer.sval.equalsIgnoreCase( "AUTHORITY" ) ) {
                    tokenizer.pushBack();
                    code = parseAuthority();
                } else {
                    throw new WKTParsingException( "An unexpected keyword was encountered in the PROJCS element: "
                                                   + tokenizer.sval + ". At line: " + tokenizer.lineno() );
                }
                break;

            default:
                throw new WKTParsingException( "The PROJ_CS contains an unknown token: " + tokenizer + " at line "
                                               + tokenizer.lineno() );
            }
            tokenizer.nextToken();
            if ( tokenizer.ttype == ']' || tokenizer.ttype == ')' ) {
                break;
            }
        }
        if ( geographicCRS == null ) {
            throw new WKTParsingException( "The PROJCS element must contain a GEOGCS. Before line "
                                           + tokenizer.lineno() );
        }

        if ( projectionType == null || params.size() == 0 ) {
            String msg = "The PROJCS element must contain a PROJECTION type as a String and a series of PARAMETERS. Before line "
                         + tokenizer.lineno();
            throw new WKTParsingException( msg );
        }

        if ( unit == null ) {
            throw new WKTParsingException( "The PROJCS element must contain a UNIT keyword element. Before line "
                                           + tokenizer.lineno() );
        }

        params = setDefaultParameterValues( params );

        if ( projectionCode.equals( CRSCodeType.getUndefined() ) ) {
            projectionCode = new CRSCodeType( projectionType );
        }

        if ( code.equals( CRSCodeType.getUndefined() ) ) {
            code = new CRSCodeType( name );
        }

        CRSIdentifiable baseCRS = new CRSIdentifiable( new CRSCodeType[] { code }, new String[] { name }, null, null,
                                                       null );
        CRSIdentifiable baseProjCRS = new CRSIdentifiable( new CRSCodeType[] { projectionCode },
                                                           new String[] { projectionType }, null, null, null );

        Point2d pointOrigin = new Point2d( DTR * determineLongitude( params ), DTR * determineLatitude( params ) );

        Axis[] axes = new Axis[] { axis1, axis2 };
        if ( projectionType.equalsIgnoreCase( "transverse_mercator" )
             || ( projectionType.equalsIgnoreCase( "transverse mercator" ) )
             || projectionType.equalsIgnoreCase( "Gauss_Kruger" ) ) {
            return new ProjectedCRS( new TransverseMercator( true, geographicCRS,
                                                             params.get( FALSE_NORTHING.toString() ),
                                                             params.get( FALSE_EASTING.toString() ), pointOrigin, unit,
                                                             params.get( SCALE_AT_NATURAL_ORIGIN.toString() ),
                                                             baseProjCRS ), axes, baseCRS );

        } else if ( projectionType.equalsIgnoreCase( "Lambert_Conformal_Conic_1SP" ) ) {
            return new ProjectedCRS( new LambertConformalConic( geographicCRS, params.get( FALSE_NORTHING.toString() ),
                                                                params.get( FALSE_EASTING.toString() ), pointOrigin,
                                                                unit, params.get( SCALE_AT_NATURAL_ORIGIN.toString() ),
                                                                baseProjCRS ), axes, baseCRS );

        } else if ( projectionType.equalsIgnoreCase( "Lambert_Conformal_Conic_2SP" )
                    || projectionType.equalsIgnoreCase( "Lambert_Conformal_Conic" ) ) {
            return new ProjectedCRS(
                                     new LambertConformalConic(
                                                                DTR * params.get( FIRST_PARALLEL_LATITUDE.toString() ),
                                                                DTR * params.get( SECOND_PARALLEL_LATITUDE.toString() ),
                                                                geographicCRS, params.get( FALSE_NORTHING.toString() ),
                                                                params.get( FALSE_EASTING.toString() ), pointOrigin,
                                                                unit, params.get( SCALE_AT_NATURAL_ORIGIN.toString() ),
                                                                baseProjCRS ), axes, baseCRS );
        } else if ( projectionType.equalsIgnoreCase( "Stereographic_Alternative" )
                    || projectionType.equalsIgnoreCase( "Double_Stereographic" )
                    || projectionType.equalsIgnoreCase( "Oblique_Stereographic" ) ) {
            return new ProjectedCRS( new StereographicAlternative( geographicCRS,
                                                                   params.get( FALSE_NORTHING.toString() ),
                                                                   params.get( FALSE_EASTING.toString() ), pointOrigin,
                                                                   unit,
                                                                   params.get( SCALE_AT_NATURAL_ORIGIN.toString() ),
                                                                   baseProjCRS ), axes, baseCRS );

        } else if ( projectionType.equalsIgnoreCase( "Stereographic_Azimuthal" ) ) {
            LOG.warn( "True scale latitude is not read from the StereoGraphic azimuthal projection yet." );
            return new ProjectedCRS( new StereographicAzimuthal( geographicCRS,
                                                                 params.get( FALSE_NORTHING.toString() ),
                                                                 params.get( FALSE_EASTING.toString() ), pointOrigin,
                                                                 unit,
                                                                 params.get( SCALE_AT_NATURAL_ORIGIN.toString() ),
                                                                 baseProjCRS ), axes, baseCRS );

        } else {
            throw new WKTParsingException( "The projection type " + projectionType + " is not supported." );
        }
    }

    /**
     * Determine the latitude of natural origin based on all the documented names under which it may appear (i.e. from
     * {@link SupportedProjectionParameters} javadoc).
     */
    private double determineLatitude( Map<String, Double> params ) {
        Double latNatOrigin = null;

        if ( params.containsKey( LATITUDE_OF_NATURAL_ORIGIN.toString() ) ) {
            latNatOrigin = params.get( LATITUDE_OF_NATURAL_ORIGIN.toString() );
        } else if ( params.containsKey( "projectionlatitude" ) ) {
            latNatOrigin = params.get( "projectionlatitude" );
        } else if ( params.containsKey( "central_latitude" ) ) {
            latNatOrigin = params.get( "central_latitude" );
        }
        return latNatOrigin;
    }

    /**
     * Determine the longitude of natural origin based on all the documented names under which it may appear (i.e. from
     * {@link SupportedProjectionParameters} javadoc).
     */
    private double determineLongitude( Map<String, Double> params ) {
        Double longNatOrigin = null;

        if ( params.containsKey( LONGITUDE_OF_NATURAL_ORIGIN.toString() ) ) {
            longNatOrigin = params.get( LONGITUDE_OF_NATURAL_ORIGIN.toString() );
        } else if ( params.containsKey( "central_meridian" ) ) {
            longNatOrigin = params.get( "central_meridian" );
        } else if ( params.containsKey( "projectionlongitude" ) ) {
            longNatOrigin = params.get( "projectionlongitude" );
        } else if ( params.containsKey( "projection_meridian" ) ) {
            longNatOrigin = params.get( "projection_meridian" );
        }
        return longNatOrigin;
    }

    private Map<String, Double> setDefaultParameterValues( Map<String, Double> params ) {
        if ( params.get( "semimajor" ) == null ) {
            params.put( "semimajor", 0.0 );
        }

        if ( params.get( "semiminor" ) == null ) {
            params.put( "semiminor", 0.0 );
        }

        if ( params.get( "latitudeoforigin" ) == null ) {
            params.put( "latitudeoforigin", 0.0 );
        } else {
            params.put( "latitudeoforigin", DTR * params.get( "latitudeoforigin" ) );
        }

        if ( params.get( "centralmeridian" ) == null ) {
            params.put( "centralmeridian", 0.0 );
        } else {
            params.put( "centralmeridian", DTR * params.get( "centralmeridian" ) );
        }

        if ( params.get( "scalefactor" ) == null ) {
            params.put( "scalefactor", 1.0 );
        }

        if ( params.get( "falseeasting" ) == null ) {
            params.put( "falseeasting", 0.0 );
        }

        if ( params.get( "falsenorthing" ) == null ) {
            params.put( "falsenorthing", 0.0 );
        }

        if ( params.get( "standardparallel1" ) == null ) {
            params.put( "standardparallel1", 0.0 );
        } else {
            params.put( "standardparallel1", DTR * params.get( "standardparallel1" ) );
        }

        if ( params.get( "standardparallel2" ) == null ) {
            params.put( "standardparallel2", 0.0 );
        } else {
            params.put( "standardparallel2", DTR * params.get( "standardparallel2" ) );
        }

        return params;
    }

    /**
     * Parses a {@link CompoundCRS} from the current WKT location.
     * 
     * @return a {@link CompoundCRS} parsed from the current reader position.
     * @throws IOException
     */
    protected CompoundCRS parseCompoundCRS()
                            throws IOException {
        passOverOpeningBracket();
        String name = null;
        List<CoordinateSystem> twoCRSs = new ArrayList<CoordinateSystem>();
        CRSCodeType code = CRSCodeType.getUndefined();
        while ( true ) {
            tokenizer.nextToken();
            switch ( tokenizer.ttype ) {
            case '"':
                name = tokenizer.sval;
                break;
            case StreamTokenizer.TT_WORD:
                if ( equalsParameterVariants( tokenizer.sval, "COMPD_CS" )
                     || equalsParameterVariants( tokenizer.sval, "PROJ_CS" )
                     || equalsParameterVariants( tokenizer.sval, "GEOG_CS" )
                     || equalsParameterVariants( tokenizer.sval, "GEOC_CS" )
                     || equalsParameterVariants( tokenizer.sval, "VERT_CS" ) ) {
                    tokenizer.pushBack();
                    twoCRSs.add( realParseCoordinateSystem() );
                } else if ( tokenizer.sval.equalsIgnoreCase( "AUTHORITY" ) ) {
                    tokenizer.pushBack();
                    code = parseAuthority();
                } else {
                    throw new WKTParsingException(
                                                   "Found a keyword different that AUTHORITY or any supported CRS inside the COMPD_CS. At line: "
                                                                           + tokenizer.lineno() );
                }
                break;

            default:
                throw new WKTParsingException( "The COMPD_CS contains an unknown token: " + tokenizer + " at line "
                                               + tokenizer.lineno() );
            }
            tokenizer.nextToken();
            if ( tokenizer.ttype == ']' || tokenizer.ttype == ')' ) {
                break;
            }
        }
        if ( twoCRSs.size() != 2 ) {
            throw new WKTParsingException( "The COMPD_CS element has two contain exactly 2 CRSs. Before line "
                                           + tokenizer.lineno() );
        }
        if ( name == null ) {
            throw new WKTParsingException( "The COMPD_CS element must contain a name as a quoted String. Before line "
                                           + tokenizer.lineno() );
        }

        VerticalCRS verticalCRS = null;
        CoordinateSystem underlyingCRS = null;
        if ( twoCRSs.get( 0 ) instanceof VerticalCRS ) {
            verticalCRS = (VerticalCRS) twoCRSs.get( 0 );
            underlyingCRS = twoCRSs.get( 1 );
        } else if ( twoCRSs.get( 1 ) instanceof VerticalCRS ) {
            verticalCRS = (VerticalCRS) twoCRSs.get( 1 );
            underlyingCRS = twoCRSs.get( 0 );
        } else {
            throw new WKTParsingException( "One of the CRSs from the COMPD_CS element must be a VERT_CS. Before line "
                                           + tokenizer.lineno() );
        }

        if ( code.equals( CRSCodeType.getUndefined() ) ) {
            code = new CRSCodeType( name );
        }
        return new CompoundCRS( verticalCRS.getVerticalAxis(), underlyingCRS, 0.0,
                                new CRSIdentifiable( new CRSCodeType[] { code }, new String[] { name }, null, null,
                                                     null ) );
    }

    /**
     * @return a Coordinate System ( Compound CRS, Projected CRS, Geographic CRS, Geocentric CRS, Vertical CRS)
     * @throws IOException
     *             if the provided WKT has a syntax error
     */
    public CoordinateSystem parseCoordinateSystem()
                            throws IOException {
        try {
            return realParseCoordinateSystem();
        } finally {
            buff.close();
        }
    }

    /**
     * Constructor
     * 
     * @param fileName
     *            the file that contains a Coordinate System definition
     * @throws IOException
     *             if the provided WKT has a syntax error
     */
    public WKTParser( String fileName ) throws IOException {
        this( new File( fileName ) );
    }

    /**
     * Create a WKTParser which reads data from the given reader.
     */
    private WKTParser( Reader reader ) {
        this.buff = reader;
        tokenizer = new StreamTokenizer( buff );
        tokenizer.wordChars( '_', '_' );
    }

    /**
     * @param fileName
     *            to read a wkt from.
     * @throws FileNotFoundException
     */
    public WKTParser( File fileName ) throws FileNotFoundException {
        this( new BufferedReader( new FileReader( fileName ) ) );
    }

    /**
     * @param wkt
     *            the wkt code as a {@link String}
     * @return the parsed {@link CoordinateSystem}
     * @throws IOException
     *             if the provided WKT has a syntax error
     */
    public static CoordinateSystem parse( String wkt )
                            throws IOException {
        WKTParser parse = new WKTParser( new BufferedReader( new StringReader( wkt ) ) );
        return parse.parseCoordinateSystem();
    }

}
