//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
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
package org.deegree.protocol.ows.capabilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.deegree.commons.types.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;

/**
 * Parser for OWS/OGC GetCapabilities requests (KVP).
 * <p>
 * Handles GetCapabilities requests that are compliant to the following specifications:
 * <ul>
 * <li>OWS Common 1.0.0</li>
 * <li>OWS Common 1.1.0</li>
 * </ul>
 * </p>
 * <p>
 * Additionally evaluates the <code>LANGUAGE</code> parameter for multilingual services according to OWS Common change
 * request OGC 08-016r2. This is used by the WPS Specification 1.0.0.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GetCapabilitiesKVPParser {

    /**
     * Parses a normalized (upper-cased keys) KVP-map as an {@link GetCapabilities} request.
     * <p>
     * NOTE: The parameters "SERVICE" and "REQUEST" are not evaluated. It is assumed that the caller already checked
     * them.
     * 
     * @param kvpParams
     *            normalized KVP-map; keys must be uppercase, each key only has one associated value
     * @return {@link GetCapabilities} request
     * @throws InvalidParameterValueException
     *             if a parameter (e.g. ACCEPTVERSIONS) contains a syntactical error
     */
    public static GetCapabilities parse( Map<String, String> kvpParams )
                            throws InvalidParameterValueException {

        // ACCEPTVERSIONS (optional)
        List<Version> acceptVersions = null;
        String acceptVersionsString = kvpParams.get( "ACCEPTVERSIONS" );
        if ( acceptVersionsString != null ) {
            acceptVersions = new ArrayList<Version>();
            String[] versionStrings = acceptVersionsString.split( "," );
            for ( String versionString : versionStrings ) {
                acceptVersions.add( Version.parseVersion( versionString ) );
            }
        }

        // SECTIONS (optional)
        List<String> sections = null;
        String sectionsString = kvpParams.get( "SECTIONS" );
        if ( sectionsString != null ) {
            sections = Arrays.asList( sectionsString.split( "," ) );
        }

        // ACCEPTFORMATS (optional)
        List<String> acceptFormats = null;
        String acceptFormatsString = kvpParams.get( "ACCEPTFORMATS" );
        if ( acceptFormatsString != null ) {
            acceptFormats = Arrays.asList( acceptFormatsString.split( "," ) );
        }

        // UPDATESEQUENCE (optional)
        String updateSequence = kvpParams.get( "UPDATESEQUENCE" );

        // LANGUAGE (optional)
        List<String> languages = null;
        String languagesString = kvpParams.get( "LANGUAGE" );
        if ( languagesString != null ) {
            languages = Arrays.asList( languagesString.split( "," ) );
        }

        return new GetCapabilities( acceptVersions, sections, acceptFormats, updateSequence, languages );
    }
}
