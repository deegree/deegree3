//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/svn_classfile_header_template.xml $
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

package org.deegree.protocol.wps.describeprocess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.protocol.wps.WPSConstants;

/**
 * Parser for WPS <code>DescribeProcess</code> KVP requests.
 *
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: $
 *
 * @version $Revision: $, $Date: $
 */
public class DescribeProcessRequestKVPAdapter {

    /**
     * Parses the given WPS 1.0.0 <code>DescribeProcess</code> KVP request.
     * <p>
     * Prerequisites (not checked by this method):
     * <ul>
     * <li>Key 'SERVICE' has value 'WPS'</li>
     * <li>Key 'REQUEST' has value 'DescribeProcess'</li>
     * <li>Key 'VERSION' has value '1.0.0'</li>
     * </ul>
     * </p>
     *
     * @param kvpParams
     *            key-value pairs, keys must be in uppercase
     * @return corresponding {@link DescribeProcessRequest} object
     * @throws MissingParameterException if a required parameter is missing
     */
    public static DescribeProcessRequest parse100( Map<String, String> kvpParams)
                            throws MissingParameterException {

        // IDENTIFIER (mandatory)
        List<String> identifierStrings = KVPUtils.splitAll( kvpParams, "IDENTIFIER" );
        if ( identifierStrings == null || identifierStrings.isEmpty() ) {
            throw new MissingParameterException( "Required parameter 'IDENTIFIER' is missing." );
        }
        List<CodeType> identifiers = new ArrayList<CodeType>(identifierStrings.size());
        for ( String identifier : identifierStrings ) {
            identifiers.add( new CodeType(identifier) );
        }

        // LANGUAGE (optional)
        String language = kvpParams.get( "LANGUAGE" );

        return new DescribeProcessRequest( WPSConstants.VERSION_100, language, identifiers );
    }
}
