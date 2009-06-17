//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.protocol.wfs.describefeaturetype;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.deegree.commons.types.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.AbstractWFSRequestKVPAdapter;
import org.deegree.protocol.wfs.WFSConstants;

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

    /**
     * Exports the given {@link DescribeFeatureType} request as a KVP-encoded string (with encoded values).
     *
     * @param request
     *            request to be exported
     * @param version
     *            protocol version of the generated KVP
     * @return KVP encoded request
     */
    public static String export( DescribeFeatureType request, Version version ) {

        StringBuffer sb = new StringBuffer();
        appendFirstKVP( sb, "VERSION", version.toString() );
        appendKVP( sb, "REQUEST", "DescribeFeatureType" );
        if ( request.getOutputFormat() != null ) {
            appendKVP( sb, "OUTPUTFORMAT", request.getOutputFormat() );
        }

        QName[] ftNames = request.getTypeNames();
        if ( ftNames != null && ftNames.length > 0 ) {

            Map<String, String> nsBindings = collectNsBinding( ftNames );
            augmentNsBindings( nsBindings, ftNames );

            StringBuffer typeNameList = new StringBuffer();
            for ( QName name : ftNames ) {
                String typeName = name.getLocalPart();
                if ( name.getNamespaceURI() != XMLConstants.NULL_NS_URI ) {
                    String prefix = nsBindings.get( name.getNamespaceURI() );
                    typeName = prefix + ":" + name.getLocalPart();
                }
                if ( typeNameList.length() > 0 ) {
                    typeNameList.append( ',' );
                }
                typeNameList.append( typeName );
            }
            appendKVP( sb, "TYPENAME", typeNameList.toString() );

            // only versions 1.1.0+ support the NAMESPACE parameter for proper qualifying of namespaces
            if ( !version.equals( VERSION_100 ) && nsBindings.size() > 0 ) {
                StringBuffer namespaceList = new StringBuffer();
                for ( String namespace : nsBindings.keySet() ) {
                    String prefix = nsBindings.get( namespace );
                    if ( namespaceList.length() > 0 ) {
                        namespaceList.append( ',' );
                    }
                    namespaceList.append( "xmlns(" );
                    namespaceList.append( prefix );
                    namespaceList.append( '=' );
                    namespaceList.append( namespace );
                    namespaceList.append( ')' );
                }
                appendKVP( sb, "NAMESPACE", namespaceList.toString() );
            }
        }
        return sb.toString();
    }

    /**
     * Augment the given map of prefix to namespace bindings with generated namespace prefices, so that every qualified
     * feature type name has a proper namespace prefix.
     *
     * @param nsBindings
     * @param ftNames
     */
    private static void augmentNsBindings( Map<String, String> nsBindings, QName[] ftNames ) {
        for ( QName name : ftNames ) {
            if ( name.getNamespaceURI() != XMLConstants.NULL_NS_URI ) {
                if ( nsBindings.get( name.getNamespaceURI() ) == null ) {
                    String prefix = getUniquePrefix( nsBindings.keySet() );
                    nsBindings.put( name.getNamespaceURI(), prefix);
                }
            }
        }
    }

    private static String getUniquePrefix( Set<String> existingPrefices ) {
        int i = 0;
        String prefix = null;

        do {
            prefix = "ns" + ++i;
        } while ( existingPrefices.contains( prefix ) );

        return prefix;
    }

    private static Map<String, String> collectNsBinding( QName[] ftNames ) {
        Map<String, String> nsBindings = new HashMap<String, String>();
        for ( QName name : ftNames ) {
            if ( name.getNamespaceURI() != XMLConstants.NULL_NS_URI ) {
                String currentPrefix = nsBindings.get( name.getNamespaceURI() );
                if ( currentPrefix == null && !XMLConstants.NULL_NS_URI.equals( name.getPrefix() ) ) {
                    nsBindings.put( name.getNamespaceURI(), name.getPrefix() );
                }
            }
        }
        return nsBindings;
    }
}
