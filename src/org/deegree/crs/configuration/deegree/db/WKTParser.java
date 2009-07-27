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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;

import org.deegree.crs.CRSCodeType;
import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.components.Axis;
import org.deegree.crs.components.Ellipsoid;
import org.deegree.crs.components.GeodeticDatum;
import org.deegree.crs.components.PrimeMeridian;
import org.deegree.crs.components.Unit;
import org.deegree.crs.components.VerticalDatum;
import org.deegree.crs.coordinatesystems.CompoundCRS;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.coordinatesystems.GeocentricCRS;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.crs.coordinatesystems.VerticalCRS;
import org.deegree.crs.exceptions.UnknownUnitException;
import org.deegree.crs.exceptions.WKTParsingException;
import org.deegree.crs.projections.azimuthal.StereographicAlternative;
import org.deegree.crs.projections.azimuthal.StereographicAzimuthal;
import org.deegree.crs.projections.conic.LambertConformalConic;
import org.deegree.crs.projections.cylindric.TransverseMercator;
import org.deegree.crs.transformations.helmert.Helmert;

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

    StreamTokenizer tokenizer;

    private BufferedReader buff;

    /**
     * Walk a character (comma or round/square bracket).
     * 
     * @param ch
     * @throws IOException
     *             if an I/O error occurs.
     * @throws WKTParsingException
     *             if the expected character is not present at this position.
     */
    void passOverChar( char ch )
    throws IOException {
        tokenizer.nextToken();
        if ( tokenizer.ttype != ch )
            throw new WKTParsingException( "The tokenizer expects the character " + ch + " while the current token is "
                                           + tokenizer.toString() );
    }

    /**
     * Walk an opening bracket (round or square).
     * 
     * @throws IOException
     *             if an I/O error occurs.
     * @throws WKTParsingException
     *             if the opening bracket is not present at this position.
     */
    void passOverOpeningBracket()
    throws IOException {
        tokenizer.nextToken();
        if ( tokenizer.ttype != '[' && tokenizer.ttype != '(' )
            throw new WKTParsingException(
                                          "The tokenizer expects an opening square/round bracket while the current token is "
                                          + tokenizer.toString() );
    }

    /**
     * Walk a closing bracket (round or square).
     * 
     * @throws IOException
     *             if an I/O error occurs.
     * @throws WKTParsingException
     *             if the closing bracket is not present at this position.
     */
    void passOverClosingBracket()
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
    void passOverWord( String s )
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
    CRSCodeType parseAuthority()
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
    String parseString()
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
    Axis parseAxis()
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
     * @return a linear Unit ( METRE, BRITISHYARD, USFOOT )
     * @throws IOException
     * @throws UnknownUnitException
     *             if the unit name does not match any of the predefined units in the API
     */
    Unit parseLinearUnit()
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
        if ( name.toLowerCase().contains( "metre" ) || name.toLowerCase().contains( "meter" )
                                || name.equalsIgnoreCase( "m" ) ) {
            return Unit.METRE;
        } else if ( name.toLowerCase().contains( "british" ) && name.toLowerCase().contains( "yard" )
                                || name.equalsIgnoreCase( "y" ) ) {
            return Unit.BRITISHYARD;
        } else if ( name.toLowerCase().contains( "foot" ) && name.toLowerCase().contains( "us" )
                                || name.equalsIgnoreCase( "ft" ) ) {
            return Unit.USFOOT;
        } else {
            throw new UnknownUnitException( "Cannot determine the unit meant by the name: " + name );
        }
    }

    /**
     * @return an angular Unit ( DEGREE, RADIAN, ARC_SEC, DMSH )
     * @throws IOException
     * @throws UnknownUnitException
     *             if the unit name does not match any of the predefined units in the API
     */
    Unit parseAngularUnit()
    throws IOException {
        passOverWord( "UNIT" );
        passOverOpeningBracket();
        String name = null;
        // Double conversionFactor = null; // we do not identify a unit based on the conversion factor, only on the name
        // CRSCodeType code = CRSCodeType.getUndefined(); // currently Units are not identifiable so the code parsed
        // here
        // is of no use
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
            throw new UnknownUnitException( "Unit name is null" );
        }
        if ( name.toLowerCase().contains( "degree" ) || name.toLowerCase().contains( "dmsh" ) )
            return Unit.DEGREE;
        else if ( name.toLowerCase().contains( "radian" ) || name.equalsIgnoreCase( "rad" ) )
            return Unit.RADIAN;
        else if ( name.toLowerCase().contains( "arc" ) && name.toLowerCase().contains( "sec" ) )
            return Unit.ARC_SEC;
        else
            throw new UnknownUnitException( "Cannot determine the unit meant by the name: " + name );
    }

    /**
     * @return a Prime Meridian
     * @throws IOException
     */
    PrimeMeridian parsePrimeMeridian()
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

        return new PrimeMeridian( Unit.RADIAN /* temporarily, until parsing the Unit of the wrapping CRS */, longitude,
                                  new CRSIdentifiable( new CRSCodeType[] { code }, new String[] { name }, null, null,
                                                       null ) );
    }

    Ellipsoid parseEllipsoid()
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

        return new Ellipsoid( semiMajorAxis, Unit.RADIAN /* temporarily, until parsing the Unit of the wrapping CRS */,
                              inverseFlattening, new CRSIdentifiable( new CRSCodeType[] { code },
                                                                      new String[] { name }, null, null, null ) );
    }

    Helmert parseHelmert()
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
     * @return a Geodetic Datum
     * @throws IOException
     */
    GeodeticDatum parseGeodeticDatum()
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

        return new GeodeticDatum( ellipsoid, helmert, code, name );
    }

    /**
     * @return a Vertical Datum
     * @throws IOException
     */
    VerticalDatum parseVerticalDatum()
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
        if ( name == null )
            throw new WKTParsingException(
                                          "The VERT_DATUM element must contain a name as a quoted String. Before line "
                                          + tokenizer.lineno() );

        return new VerticalDatum( code, name, null, null, null );
    }

    private CoordinateSystem realParseCoordinateSystem()
    throws IOException {
        tokenizer.nextToken();
        String crsType = tokenizer.sval; // expecting StreamTokenizer.TT_WORD

        // COMPOUND CRS
        if ( crsType.equalsIgnoreCase( "COMPD_CS" ) ) {
            return parseCompoundCRS();

            // PROJECTED CRS
        } else if ( crsType.equalsIgnoreCase( "PROJCS" ) ) {
            return parseProjectedCRS();

            // GEOGRAPHIC CRS
        } else if ( crsType.equalsIgnoreCase( "GEOGCS" ) ) {
            return parseGeographiCRS();

            // GEOCENTRIC CRS
        } else if ( crsType.equalsIgnoreCase( "GEOCCS" ) ) {
            return parseGeocentricCRS();

            // VERTICAL CRS
        } else if ( crsType.equalsIgnoreCase( "VERT_CS" ) ) {
            return parseVerticalCRS();

        } else
            throw new WKTParsingException( "Expected a CRS element but an unknown keyword was encountered: "
                                           + tokenizer.sval + " at line " + tokenizer.lineno() );
    }

    private CoordinateSystem parseVerticalCRS() throws IOException {
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
                    unit = parseLinearUnit();
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

        return new VerticalCRS( verticalDatum, new Axis[] { axis },
                                new CRSIdentifiable( new CRSCodeType[] { code }, new String[] { name }, null, null,
                                                     null ) );
    }

    private CoordinateSystem parseGeocentricCRS() throws IOException {
        passOverOpeningBracket();
        String name = null;
        GeodeticDatum datum = null;
        PrimeMeridian pm = null;
        Unit unit = null;
        Axis axis1 = new Axis( "X", Axis.AO_OTHER ); // the default values of GEOCCS axes, based on the OGC
        // specification
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
                    unit = parseLinearUnit();
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
            throw new WKTParsingException( "The GEOCCS element must contain a DATUM. Before line "
                                           + tokenizer.lineno() );
        if ( pm == null )
            throw new WKTParsingException( "The GEOCCS element must contain a PRIMEM. Before line "
                                           + tokenizer.lineno() );

        pm.setAngularUnit( unit );
        datum.setPrimeMeridian( pm );
        datum.getEllipsoid().setUnits( unit );

        return new GeocentricCRS( datum, new Axis[] { axis1, axis2, axis3 },
                                  new CRSIdentifiable( new CRSCodeType[] { code }, new String[] { name }, null,
                                                       null, null ) );
    }

    private CoordinateSystem parseGeographiCRS() throws IOException {
        passOverOpeningBracket();
        String name = null;
        GeodeticDatum datum = null;
        PrimeMeridian pm = null;
        Unit unit = null;
        Axis axis1 = new Axis( Unit.DEGREE, "Lon", Axis.AO_EAST ); // the default values for GEOGCS axes, based on the OGC
        // specification
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
                    unit = parseAngularUnit();
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
            throw new WKTParsingException(
                                          "The GEOGCS element must contain a name as a quoted String. Before line "
                                          + tokenizer.lineno() );
        if ( unit == null )
            throw new WKTParsingException( "The GEOGCS element must contain a UNIT keyword element. Before line "
                                           + tokenizer.lineno() );
        if ( datum == null )
            throw new WKTParsingException( "The GEOGCS element must contain a DATUM. Before line "
                                           + tokenizer.lineno() );
        if ( pm == null )
            throw new WKTParsingException( "The GEOGCS element must contain a PRIMEM. Before line "
                                           + tokenizer.lineno() );

        pm.setAngularUnit( unit );
        datum.setPrimeMeridian( pm );
        datum.getEllipsoid().setUnits( unit );

        return new GeographicCRS( datum, new Axis[] { axis1, axis2 },
                                  new CRSIdentifiable( new CRSCodeType[] { code }, new String[] { name }, null,
                                                       null, null ) );
    }

    private CoordinateSystem parseProjectedCRS() throws IOException {
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
                if ( tokenizer.sval.equalsIgnoreCase( "GEOGCS" ) ) {
                    tokenizer.pushBack();
                    geographicCRS = (GeographicCRS) realParseCoordinateSystem();
                } else if ( tokenizer.sval.equalsIgnoreCase( "PROJECTION" ) ) {
                    passOverOpeningBracket();
                    tokenizer.nextToken();
                    if ( tokenizer.ttype != '"' )
                        throw new WKTParsingException(
                                                      "The PROJECTION element must contain a quoted String. At line "
                                                      + tokenizer.lineno() );
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
                    if ( tokenizer.ttype != '"' )
                        throw new WKTParsingException(
                                                      "The PARAMETER element must contain a quoted String as parameter name. At line "
                                                      + tokenizer.lineno() );
                    String paramName = tokenizer.sval;
                    passOverChar( ',' );
                    tokenizer.nextToken();
                    if ( tokenizer.ttype != StreamTokenizer.TT_NUMBER )
                        throw new WKTParsingException(
                                                      "The PARAMETER element must contain a number as parameter value. At line "
                                                      + tokenizer.lineno() );
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
                    unit = parseLinearUnit();
                } else if ( tokenizer.sval.equalsIgnoreCase( "AUTHORITY" ) ) {
                    tokenizer.pushBack();
                    code = parseAuthority();
                } else
                    throw new WKTParsingException( "An unexpected keyword was encountered in the PROJCS element: "
                                                   + tokenizer.sval + ". At line: " + tokenizer.lineno() );
                break;

            default:
                throw new WKTParsingException( "The COMPD_CS contains an unknown token: " + tokenizer + " at line "
                                               + tokenizer.lineno() );
            }
            tokenizer.nextToken();
            if ( tokenizer.ttype == ']' || tokenizer.ttype == ')' )
                break;
        }
        if ( geographicCRS == null )
            throw new WKTParsingException( "The PROJCS element must contain a GEOGCS. Before line "
                                           + tokenizer.lineno() );
        if ( projectionType == null || params.size() == 0 )
            throw new WKTParsingException(
                                          "The PROJCS element must contain a PROJECTION type as a String and a series of PARAMETERS. Before line "
                                          + tokenizer.lineno() );
        if ( unit == null )
            throw new WKTParsingException( "The PROJCS element must contain a UNIT keyword element. Before line "
                                           + tokenizer.lineno() );

        // default value for parameters
        if ( !params.containsKey( "semi_major" ) )
            params.put( "semi_major", 0.0 );
        if ( !params.containsKey( "semi_minor" ) )
            params.put( "semi_minor", 0.0 );
        if ( !params.containsKey( "latitude_of_origin" ) )
            params.put( "latitude_of_origin", 0.0 );
        if ( !params.containsKey( "central_meridian" ) )
            params.put( "central_meridian", 0.0 );
        if ( !params.containsKey( "scale_factor" ) )
            params.put( "scale_factor", 1.0 );
        if ( !params.containsKey( "false_easting" ) )
            params.put( "false_easting", 0.0 );
        if ( !params.containsKey( "false_northing" ) )
            params.put( "false_northing", 0.0 );
        if ( !params.containsKey( "standard_parallel1" ) )
            params.put( "standard_parallel1", 0.0 );
        if ( !params.containsKey( "standard_parallel2" ) )
            params.put( "standard_parallel2", 0.0 );

        if ( projectionType.equalsIgnoreCase( "transverse_mercator" )
                                || projectionType.equalsIgnoreCase( "Gauss_Kruger" ) )
            return new ProjectedCRS(
                                    new TransverseMercator(
                                                           true,
                                                           geographicCRS,
                                                           params.get( "false_northing" ),
                                                           params.get( "false_easting" ),
                                                           new Point2d( params.get( "central_meridian" ),
                                                                        params.get( "latitude_of_origin" ) ),
                                                                        unit,
                                                                        params.get( "scale_factor" ),
                                                                        new CRSIdentifiable(
                                                                                            new CRSCodeType[] { projectionCode },
                                                                                            new String[] { projectionType },
                                                                                            null, null, null ) ),
                                                                                            new Axis[] { axis1, axis2 }, new CRSIdentifiable( new CRSCodeType[] { code },
                                                                                                                                              new String[] { name }, null,
                                                                                                                                              null, null ) );
        else if ( projectionType.equalsIgnoreCase( "Lambert_Conformal_Conic_1SP" ) )
            return new ProjectedCRS(
                                    new LambertConformalConic(
                                                              geographicCRS,
                                                              params.get( "false_northing" ),
                                                              params.get( "false_easting" ),
                                                              new Point2d( params.get( "central_meridian" ),
                                                                           params.get( "latitude_of_origin" ) ),
                                                                           unit,
                                                                           params.get( "scale_factor" ),
                                                                           new CRSIdentifiable(
                                                                                               new CRSCodeType[] { projectionCode },
                                                                                               new String[] { projectionType },
                                                                                               null, null, null ) ),
                                                                                               new Axis[] { axis1, axis2 }, new CRSIdentifiable( new CRSCodeType[] { code },
                                                                                                                                                 new String[] { name }, null,
                                                                                                                                                 null, null ) );
        else if ( projectionType.equalsIgnoreCase( "Lambert_Conformal_Conic_2SP" ) )
            return new ProjectedCRS(
                                    new LambertConformalConic(
                                                              params.get( "standard_parallel1" ),
                                                              params.get( "standard_parallel2" ),
                                                              geographicCRS,
                                                              params.get( "false_northing" ),
                                                              params.get( "false_easting" ),
                                                              new Point2d( params.get( "central_meridian" ),
                                                                           params.get( "latitude_of_origin" ) ),
                                                                           unit,
                                                                           params.get( "scale_factor" ),
                                                                           new CRSIdentifiable(
                                                                                               new CRSCodeType[] { projectionCode },
                                                                                               new String[] { projectionType },
                                                                                               null, null, null ) ),
                                                                                               new Axis[] { axis1, axis2 }, new CRSIdentifiable( new CRSCodeType[] { code },
                                                                                                                                                 new String[] { name }, null,
                                                                                                                                                 null, null ) );
        else if ( projectionType.equalsIgnoreCase( "Stereographic_Alternative" )
                                || projectionType.equalsIgnoreCase( "Double_Stereographic" )
                                || projectionType.equalsIgnoreCase( "Oblique_Stereographic" ) )
            return new ProjectedCRS(
                                    new StereographicAlternative(
                                                                 geographicCRS,
                                                                 params.get( "false_northing" ),
                                                                 params.get( "false_easting" ),
                                                                 new Point2d( params.get( "central_meridian" ),
                                                                              params.get( "latitude_of_origin" ) ),
                                                                              unit,
                                                                              params.get( "scale_factor" ),
                                                                              new CRSIdentifiable(
                                                                                                  new CRSCodeType[] { projectionCode },
                                                                                                  new String[] { projectionType },
                                                                                                  null, null, null ) ),
                                                                                                  new Axis[] { axis1, axis2 }, new CRSIdentifiable( new CRSCodeType[] { code },
                                                                                                                                                    new String[] { name }, null,
                                                                                                                                                    null, null ) );
        else if ( projectionType.equalsIgnoreCase( "Stereographic_Azimuthal" ) )
            return new ProjectedCRS(
                                    new StereographicAzimuthal(
                                                               // TODO true_scale_latitude parameter???
                                                               geographicCRS,
                                                               params.get( "false_northing" ),
                                                               params.get( "false_easting" ),
                                                               new Point2d( params.get( "central_meridian" ),
                                                                            params.get( "latitude_of_origin" ) ),
                                                                            unit,
                                                                            params.get( "scale_factor" ),
                                                                            new CRSIdentifiable(
                                                                                                new CRSCodeType[] { projectionCode },
                                                                                                new String[] { projectionType },
                                                                                                null, null, null ) ),
                                                                                                new Axis[] { axis1, axis2 }, new CRSIdentifiable( new CRSCodeType[] { code },
                                                                                                                                                  new String[] { name }, null,
                                                                                                                                                  null, null ) );
        else
            throw new WKTParsingException( "The projection type " + projectionType + " is not supported." );
    }

    private CompoundCRS parseCompoundCRS() throws IOException {
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
                if ( tokenizer.sval.equalsIgnoreCase( "COMPD_CS" ) || tokenizer.sval.equalsIgnoreCase( "PROJCS" )
                                        || tokenizer.sval.equalsIgnoreCase( "GEOGCS" ) || tokenizer.sval.equals( "GEOCCS" )
                                        || tokenizer.sval.equals( "VERT_CS" ) ) {
                    tokenizer.pushBack();
                    twoCRSs.add( realParseCoordinateSystem() );
                } else if ( tokenizer.sval.equalsIgnoreCase( "AUTHORITY" ) ) {
                    tokenizer.pushBack();
                    code = parseAuthority();
                } else
                    throw new WKTParsingException(
                                                  "Found a keyword different that AUTHORITY or any supported CRS inside the COMPD_CS. At line: "
                                                  + tokenizer.lineno() );
                break;

            default:
                throw new WKTParsingException( "The COMPD_CS contains an unknown token: " + tokenizer + " at line "
                                               + tokenizer.lineno() );
            }
            tokenizer.nextToken();
            if ( tokenizer.ttype == ']' || tokenizer.ttype == ')' )
                break;
        }
        if ( twoCRSs.size() != 2 )
            throw new WKTParsingException( "The COMPD_CS element has two contain exactly 2 CRSs. Before line "
                                           + tokenizer.lineno() );
        if ( name == null )
            throw new WKTParsingException(
                                          "The COMPD_CS element must contain a name as a quoted String. Before line "
                                          + tokenizer.lineno() );

        VerticalCRS verticalCRS = null;
        CoordinateSystem underlyingCRS = null;
        if ( twoCRSs.get( 0 ) instanceof VerticalCRS ) {
            verticalCRS = (VerticalCRS) twoCRSs.get( 0 );
            underlyingCRS = twoCRSs.get( 1 );
        } else if ( twoCRSs.get( 1 ) instanceof VerticalCRS ) {
            verticalCRS = (VerticalCRS) twoCRSs.get( 1 );
            underlyingCRS = twoCRSs.get( 0 );
        } else
            throw new WKTParsingException(
                                          "One of the CRSs from the COMPD_CS element must be a VERT_CS. Before line "
                                          + tokenizer.lineno() );

        return new CompoundCRS( verticalCRS.getVerticalAxis(), underlyingCRS, 0.0,
                                new CRSIdentifiable( new CRSCodeType[] { code }, new String[] { name }, null, null,
                                                     null ) );        
    }

    /**
     * @return a Coordinate System ( Compound CRS, Projected CRS, Geographic CRS, Geocentric CRS, Vertical CRS)
     * @throws IOException      if the provided WKT has a syntax error
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
     * @param fileName      the file that contains a Coordinate System definition
     * @throws IOException      if the provided WKT has a syntax error
     */
    public WKTParser( String fileName ) throws IOException {
        File file = new File( fileName );
        buff = new BufferedReader( new FileReader( file ) );
        tokenizer = new StreamTokenizer( buff );
        tokenizer.wordChars( '_', '_' );
    }

    /**
     * Use this if you want to parse WKT strings directly using the parse method.
     */
    public WKTParser() {
        // see comment
    }

    /**
     * @param wkt       the wkt code as a {@link String}   
     * @return      
     *          the parsed {@link CoordinateSystem}
     * @throws IOException      if the provided WKT has a syntax error
     */
    public CoordinateSystem parse( String wkt )
    throws IOException {
        buff = new BufferedReader( new StringReader( wkt ) );
        tokenizer = new StreamTokenizer( buff );
        tokenizer.wordChars( '_', '_' );
        return parseCoordinateSystem();
    }

    /**
     * For testing purposes.
     * 
     * @param args      the first argument is the file containing the Coordinate System.
     * @throws IOException      if the provided WKT has a syntax error      
     */
    public static void main( String[] args )
    throws IOException {
        WKTParser parser = new WKTParser( args[0] );
        System.out.println( " The CRS looks like: " + parser.parseCoordinateSystem() );
        // System.out.println( "The coordinate system introduced is " + crs );
        // System.out.println( ( (ProjectedCRS) ( (CompoundCRS) crs).getUnderlyingCRS() ).getProjection() );
    }

}
