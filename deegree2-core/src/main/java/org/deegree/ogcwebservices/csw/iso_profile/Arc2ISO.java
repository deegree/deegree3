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

package org.deegree.ogcwebservices.csw.iso_profile;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLTools;
import org.w3c.dom.Node;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Arc2ISO {

    private static ILogger LOG = LoggerFactory.getLogger( Arc2ISO.class );

    private static Map<String, String> roleCd = new HashMap<String, String>();

    private static Map<String, String> geomObjTypeCd = new HashMap<String, String>();

    private static Map<String, String> dateTypCd = new HashMap<String, String>();

    private static Map<String, String> topCatCd = new HashMap<String, String>();

    private static Map<String, String> charSetCd = new HashMap<String, String>();

    private static Map<String, String> securityCd = new HashMap<String, String>();

    private static Map<String, String> maintenanceCd = new HashMap<String, String>();

    private static Map<String, String> progressCd = new HashMap<String, String>();

    private static Map<String, String> mediumNameCd = new HashMap<String, String>();

    private static Map<String, String> mediumFormatCd = new HashMap<String, String>();

    static {
        // fill role code map
        roleCd.put( "001", "resourceProvider" );
        roleCd.put( "002", "custodian" );
        roleCd.put( "003", "owner" );
        roleCd.put( "004", "user" );
        roleCd.put( "005", "distributor" );
        roleCd.put( "006", "originator" );
        roleCd.put( "007", "pointOfContact" );
        roleCd.put( "008", "principalInvestigator" );
        roleCd.put( "009", "processor" );
        roleCd.put( "010", "publisher" );
        roleCd.put( "011", "author" );

        // fill GeometricObjectTypeCode map
        geomObjTypeCd.put( "Point", "point" );
        geomObjTypeCd.put( "MultiPoint", "NO MATCHING AVAILABLE" );
        geomObjTypeCd.put( "Polyline", "curve" );
        geomObjTypeCd.put( "Polygon", "surface" );
        geomObjTypeCd.put( "Arc", "NO MATCHING AVAILABLE" );
        geomObjTypeCd.put( "Node", "NO MATCHING AVAILABLE" );
        geomObjTypeCd.put( "Region", "NO MATCHING AVAILABLE" );
        geomObjTypeCd.put( "Route", "NO MATCHING AVAILABLE" );
        geomObjTypeCd.put( "Tic", "NO MATCHING AVAILABLE" );
        geomObjTypeCd.put( "Label", "NO MATCHING AVAILABLE" );
        geomObjTypeCd.put( "Annotation", "NO MATCHING AVAILABLE" );
        geomObjTypeCd.put( "Triangle", "NO MATCHING AVAILABLE" );
        geomObjTypeCd.put( "Edge", "NO MATCHING AVAILABLE" );

        // fill dateTypCd map
        dateTypCd.put( "001", "creation" );
        dateTypCd.put( "002", "publication" );
        dateTypCd.put( "003", "revision" );

        // fill topic category code map
        topCatCd.put( "001", "farming" );
        topCatCd.put( "002", "biota" );
        topCatCd.put( "003", "boundaries" );
        topCatCd.put( "004", "climatologyMeteorologyAtmosphere" );
        topCatCd.put( "005", "economy" );
        topCatCd.put( "006", "elevation" );
        topCatCd.put( "007", "environment" );
        topCatCd.put( "008", "geoscientificInformation" );
        topCatCd.put( "009", "health" );
        topCatCd.put( "010", "imageryBaseMapsEarthCover" );
        topCatCd.put( "011", "intelligenceMilitary" );
        topCatCd.put( "012", "inlandWaters" );
        topCatCd.put( "013", "location" );
        topCatCd.put( "014", "oceans" );
        topCatCd.put( "015", "planningCadastre" );
        topCatCd.put( "016", "society" );
        topCatCd.put( "017", "structure" );
        topCatCd.put( "018", "transportation" );
        topCatCd.put( "019", "utilitiesCommunication" );

        // fill MD_CharacterSetCode list
        charSetCd.put( "003", "utf7" );
        charSetCd.put( "004", "utf8" );
        charSetCd.put( "005", "utf16" );
        charSetCd.put( "006", "8859part1" );
        charSetCd.put( "007", "8859part2" );
        charSetCd.put( "025", "usAscii" );

        // fill MD_ClassificationCode list
        securityCd.put( "Top secret", "topSecret" );
        securityCd.put( "Secret", "secret" );
        securityCd.put( "Confidential", "confidential" );
        securityCd.put( "Restricted", "restricted" );
        securityCd.put( "Unclassified", "unclassified" );
        securityCd.put( "Sensitive", "unclassified" );

        // fill MD_MaintenanceFrequencyCode list
        maintenanceCd.put( "Continually", "continual" );
        maintenanceCd.put( "Daily", "daily" );
        maintenanceCd.put( "Weekly", "weekly" );
        maintenanceCd.put( "Monthly", "monthly" );
        maintenanceCd.put( "Annually", "annually" );
        maintenanceCd.put( "Unknown", "unknown" );
        maintenanceCd.put( "As needed", "asNeeded" );
        maintenanceCd.put( "Irregular", "irregular" );
        maintenanceCd.put( "None planned", "notPlanned" );

        progressCd.put( "Complete", "completed" );
        progressCd.put( "In work", "onGoing" );
        progressCd.put( "Planned", "planned" );

        mediumNameCd.put( "CD-ROM", "cdRom" );
        mediumNameCd.put( "DVD", "dvd" );
        mediumNameCd.put( "DVD-ROM", "dvdRom" );
        mediumNameCd.put( "3-1/2 inch floppy disk", "3halfInchFloppy" );
        mediumNameCd.put( "5-1/4 inch floppy disk", "5quarterInchFloppy" );
        mediumNameCd.put( "7-track tape", "7trackTape" );
        mediumNameCd.put( "9-track tape", "9trackTape" );
        mediumNameCd.put( "4 mm cartridge tape", "4mmCartridgeTape" );
        mediumNameCd.put( "8 mm cartridge tape", "8mmCartridgeTape" );
        mediumNameCd.put( "1/4-inch cartridge tape", "1quarterInchCartridgeTape" );

        mediumFormatCd.put( "cpio", "cpio" );
        mediumNameCd.put( "tar", "tar" );
        mediumNameCd.put( "High Sierra", "highSierra" );
        mediumNameCd.put( "ISO 9660", "iso9660" );
        mediumNameCd.put( "ISO 9660 with Rock Ridge extensions", "iso9660RockRidge" );
        mediumNameCd.put( "ISO 9660 with Apple HFS extensions", "iso9660AppleHFS" );
    }

    /**
     * transforms a esri arc catalog date into a ISO8601 date format
     *
     * @param node
     * @return the date string
     */
    public static String getISODate( Node node ) {
        String result = "1000-01-01";
        String s = XMLTools.getStringValue( node );
        if ( s.length() == 8 ) {
            String y = s.substring( 0, 4 );
            String m = s.substring( 4, 6 );
            String d = s.substring( 6, s.length() );
            result = y + '-' + m + '-' + d;
        } else if ( s.length() == 4 ) {
            String y = s.substring( 0, 4 );
            result = y + "-01-01";
        }
        return result;
    }

    /**
     * transforms a esri arc catalog date into a ISO8601 date format
     *
     * @param node
     * @return the date string
     */
    public static String getISODate2( Node node ) {

        String s = XMLTools.getStringValue( node );
        LOG.logDebug( "s: " + s );
        String[] arr = StringTools.toArray( s, ".", false );

        return arr[2] + '-' + arr[1] + '-' + arr[0];

    }

    /**
     * transforms a esri arc catalog date into a ISO8601 date format
     *
     * @param node
     * @return the date string
     */
    public static String getISOTime( Node node ) {
        String result = "00:00:00";
        String s = XMLTools.getStringValue( node );
        LOG.logDebug( "s: " + s );
        if ( s.length() == 6 ) {
            String std = s.substring( 0, 2 );
            String m = s.substring( 2, 4 );
            String sec = s.substring( 4, 6 );
            result = std + ':' + m + ':' + sec;
        }
        return result;
    }

    /**
     * returns the role code value matching the passed role code ID
     *
     * @param node
     * @return the code
     */
    public static String getRoleCode( Node node ) {
        return roleCd.get( XMLTools.getStringValue( node ) );
    }

    /**
     * returns the role security code value matching the passed security code ID
     *
     * @param node
     * @return the role security code value matching the passed security code ID
     */
    public static String getSecurityCode( Node node ) {
        if ( securityCd.containsKey( XMLTools.getStringValue( node ) ) ) {
            return securityCd.get( XMLTools.getStringValue( node ) );
        }
        return "unclassified";

    }

    /**
     * returns the role security code value matching the passed security code ID
     *
     * @param node
     * @return the role security code value matching the passed security code ID
     */
    public static String getMaintenanceCode( Node node ) {
        if ( maintenanceCd.containsKey( XMLTools.getStringValue( node ) ) ) {
            return maintenanceCd.get( XMLTools.getStringValue( node ) );
        }
        return "unknown";

    }

    /**
     * returns the GeometricObjectTypeCode value matching the passed ESRI efeageom value
     *
     * @param node
     * @return the GeometricObjectTypeCode value matching the passed ESRI
     */
    public static String getGeometricObjectTypeCode( Node node ) {
        return geomObjTypeCd.get( XMLTools.getStringValue( node ) );
    }

    /**
     * returns the date type code value matching the passed date type code ID
     *
     * @param node
     * @return the date type code value matching the passed date type code ID
     */
    public static String getDateTypeCode( Node node ) {
        return dateTypCd.get( XMLTools.getStringValue( node ) );
    }

    /**
     * returns the topic category code value matching the passed topic category code ID. As default
     * 'geoscientificInformation' will be returned if passed node is null.
     *
     * @param node
     * @return the topic category code value matching the passed topic category code ID. As default
     *         'geoscientificInformation' will be returned if passed node is null.
     */
    public static String getTopCatTypeCode( Node node ) {
        if ( node != null ) {
            return topCatCd.get( XMLTools.getStringValue( node ) );
        }
        return "geoscientificInformation";
    }

    /**
     * returns the CharacterSetCode value matching the passed CharacterSetCode ID
     *
     * @param node
     * @return the CharacterSetCode value matching the passed
     */
    public static String getCharacterSetCode( Node node ) {
        return charSetCd.get( XMLTools.getStringValue( node ) );
    }

    /**
     * formats a number to a String formatted: ###.##
     *
     * @param node
     * @return the formatted string
     */
    public static String formatCoord( Node node ) {
        String s = XMLTools.getStringValue( node );
        DecimalFormat dc = new DecimalFormat( "###.##" );
        return dc.format( Double.parseDouble( s ) ).replace( ',', '.' );
    }

    /**
     * returns the ProgressCode value matching the passed progressCode
     *
     * @param node
     * @return the ProgressCode value matching the passed progressCode
     */
    public static String getProgressCode( Node node ) {
        return progressCd.get( XMLTools.getStringValue( node ) );
    }

    /**
     * returns the MediumNameCode value matching the passed MediumNameCode
     *
     * @param node
     * @return the MediumNameCode value matching the passed MediumNameCode
     */
    public static String getMediumNameCode( Node node ) {
        if ( mediumNameCd.containsKey( XMLTools.getStringValue( node ) ) ) {
            return mediumNameCd.get( XMLTools.getStringValue( node ) );
        }
        return "UNKNOWN";

    }

    /**
     * returns the MediumFormatCode value matching the passed MediumFormatCode
     *
     * @param node
     * @return the MediumFormatCode value matching the passed MediumFormatCode
     */
    public static String getMediumFormatCode( Node node ) {
        if ( mediumFormatCd.containsKey( XMLTools.getStringValue( node ) ) ) {
            return mediumFormatCd.get( XMLTools.getStringValue( node ) );
        }
        return "UNKNOWN";

    }
}
