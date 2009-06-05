//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.protocol.wfs.describefeaturetype;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.types.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.WFSConstants;
import org.deegree.protocol.wfs.AbstractWFSRequestKVPAdapter;

/**
 * Adapter between KVP <code>DescribeFeatureType</code> requests and {@link DescribeFeatureType} objects.
 * <p>
 * TODO code for exporting to KVP form
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class DescribeFeatureTypeKVPAdapter extends AbstractWFSRequestKVPAdapter {

    /**
     * Parses a normalized KVP-map as a WFS {@link DescribeFeatureType} request.
     * <p>
     * Supported versions:
     * <ul>
     * <li>WFS 1.0.0</li>
     * <li>WFS 1.1.0</li>
     * <li>WFS 2.0.0 (tentative)</li>
     * </ul>
     * 
     * @param kvpParams
     *            normalized KVP-map; keys must be uppercase, each key only has one associated value
     * @return parsed {@link DescribeFeatureType} request
     * @throws MissingParameterException
     *             if the request version is unsupported
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public static DescribeFeatureType parse( Map<String, String> kvpParams )
                            throws MissingParameterException, InvalidParameterValueException {

        Version version = Version.parseVersion( KVPUtils.getRequired( kvpParams, "VERSION" ) );

        DescribeFeatureType result = null;
        if ( VERSION_100.equals( version ) ) {
            result = parse100( kvpParams );
        } else if ( VERSION_110.equals( version ) ) {
            result = parse110( kvpParams );
        } else if ( VERSION_200.equals( version ) ) {
            result = parse200( kvpParams );
        } else {
            String msg = Messages.get( "UNSUPPORTED_VERSION", version, Version.getVersionsString( VERSION_100,
                                                                                                  VERSION_110,
                                                                                                  VERSION_200 ) );
            throw new InvalidParameterValueException( msg );
        }
        return result;
    }

    /**
     * Parses a normalized KVP-map as a WFS 1.0.0 {@link DescribeFeatureType} request.
     * 
     * @param kvpParams
     *            normalized KVP-map; keys must be uppercase, each key only has one associated value
     * @return parsed {@link DescribeFeatureType} request
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public static DescribeFeatureType parse100( Map<String, String> kvpParams )
                            throws InvalidParameterValueException {

        // optional: 'TYPENAME'
        QName[] typeNames = extractTypeNames( kvpParams, null );

        // optional: 'OUTPUTFORMAT'
        String outputFormat = kvpParams.get( "OUTPUTFORMAT" );

        return new DescribeFeatureType( WFSConstants.VERSION_100, null, outputFormat, typeNames );
    }

    /**
     * Parses a normalized KVP-map as a WFS 1.1.0 {@link DescribeFeatureType} request.
     * 
     * @param kvpParams
     *            normalized KVP-map; keys must be uppercase, each key only has one associated value
     * @return parsed {@link DescribeFeatureType} request
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public static DescribeFeatureType parse110( Map<String, String> kvpParams )
                            throws InvalidParameterValueException {

        // optional: 'NAMESPACE'
        Map<String, String> nsBindings = extractNamespaceBindings( kvpParams );

        // optional: 'TYPENAME'
        QName[] typeNames = extractTypeNames( kvpParams, nsBindings );

        // optional: 'OUTPUTFORMAT'
        String outputFormat = kvpParams.get( "OUTPUTFORMAT" );

        return new DescribeFeatureType( WFSConstants.VERSION_110, null, outputFormat, typeNames );
    }

    /**
     * Parses a normalized KVP-map as a WFS 2.0.0 {@link DescribeFeatureType} request.
     * 
     * @param kvpParams
     *            normalized KVP-map; keys must be uppercase, each key only has one associated value
     * @return parsed {@link DescribeFeatureType} request
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public static DescribeFeatureType parse200( Map<String, String> kvpParams )
                            throws InvalidParameterValueException {

        // optional: 'NAMESPACE'
        Map<String, String> nsBindings = extractNamespaceBindings( kvpParams );

        // optional: 'TYPENAME'
        QName[] typeNames = extractTypeNames( kvpParams, nsBindings );

        // optional: 'OUTPUTFORMAT'
        String outputFormat = kvpParams.get( "OUTPUTFORMAT" );

        return new DescribeFeatureType( WFSConstants.VERSION_200, null, outputFormat, typeNames );
    }
}
