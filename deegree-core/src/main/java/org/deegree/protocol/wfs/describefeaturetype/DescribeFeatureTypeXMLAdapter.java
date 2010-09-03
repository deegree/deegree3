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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.AbstractWFSRequestXMLAdapter;
import org.deegree.protocol.wfs.WFSConstants;

/**
 * Adapter between XML <code>DescribeFeatureType</code> requests and {@link DescribeFeatureType} objects.
 * <p>
 * TODO code for exporting to XML
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class DescribeFeatureTypeXMLAdapter extends AbstractWFSRequestXMLAdapter {

    /**
     * Parses an WFS <code>DescribeFeatureType</code> document into a {@link DescribeFeatureType} request.
     * <p>
     * Supported versions:
     * <ul>
     * <li>WFS 1.0.0</li>
     * <li>WFS 1.1.0</li>
     * <li>WFS 2.0.0 (tentative)</li>
     * </ul>
     * 
     * @param version
     *            version of the request, may be <code>null</code> (in that case, a version attribute must be present in
     *            the root element)
     * @return parsed {@link DescribeFeatureType} request
     * @throws XMLParsingException
     *             if a syntax error occurs in the XML
     * @throws MissingParameterException
     *             if the request version is unsupported
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public DescribeFeatureType parse( Version version ) {

        if ( version == null ) {
            version = Version.parseVersion( getRequiredNodeAsString( rootElement, new XPath( "@version", nsContext ) ) );
        }

        DescribeFeatureType result = null;
        if ( VERSION_100.equals( version ) ) {
            result = parse100();
        } else if ( VERSION_110.equals( version ) ) {
            result = parse110();
        } else if ( VERSION_200.equals( version ) ) {
            result = parse200();
        } else {
            String msg = Messages.get( "UNSUPPORTED_VERSION", version, Version.getVersionsString( VERSION_100,
                                                                                                  VERSION_110,
                                                                                                  VERSION_200 ) );
            throw new InvalidParameterValueException( msg );
        }
        return result;
    }

    /**
     * Parses a WFS 1.0.0 <code>DescribeFeatureType</code> document into a {@link DescribeFeatureType} request.
     * 
     * @return parsed {@link DescribeFeatureType} request
     * @throws XMLParsingException
     *             if a syntax error occurs in the XML
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public DescribeFeatureType parse100()
                            throws XMLParsingException, InvalidParameterValueException {

        // optional: '@outputFormat'
        String outputFormat = rootElement.getAttributeValue( new QName( "outputFormat" ) );

        // 'wfs:TypeName' elements (minOccurs=0, maxOccurs=unbounded)
        QName[] typeNames = getNodesAsQNames( rootElement, new XPath( "wfs:TypeName", nsContext ) );
        String[] typeNames2 = getNodesAsStrings( rootElement, new XPath( "wfs:TypeName", nsContext ) );
        // TODO remove null namespace hack
        for ( int i = 0; i < typeNames.length; i++ ) {
            if ( typeNames[i] == null ) {                
                typeNames[i] = mangleTypeName ( typeNames2[i] );
            } else if ( WFSConstants.WFS_NS.equals( typeNames[i].getNamespaceURI() ) ) {
                typeNames[i] = new QName( typeNames[i].getLocalPart() );
            }
        }

        return new DescribeFeatureType( VERSION_100, null, outputFormat, typeNames, null );
    }

    private QName mangleTypeName( String s ) {
        String localPart = s;
        String prefix = XMLConstants.DEFAULT_NS_PREFIX;
        String namespace = XMLConstants.NULL_NS_URI;

        int colonIdx = s.indexOf( ':' );
        if (colonIdx >= 0 ){
            prefix = s.substring( 0, colonIdx );
            localPart = s.substring( colonIdx + 1);
        }

        return new QName (namespace, localPart, prefix);
    }

    /**
     * Parses a WFS 1.1.0 <code>DescribeFeatureType</code> document into a {@link DescribeFeatureType} request.
     * 
     * @return parsed {@link DescribeFeatureType} request
     * @throws XMLParsingException
     *             if a syntax error occurs in the XML
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public DescribeFeatureType parse110()
                            throws XMLParsingException, InvalidParameterValueException {

        // optional: '@handle'
        String handle = rootElement.getAttributeValue( new QName( "handle" ) );

        // optional: '@outputFormat'
        String outputFormat = rootElement.getAttributeValue( new QName( "outputFormat" ) );

        // 'wfs:TypeName' elements (minOccurs=0, maxOccurs=unbounded)
        QName[] typeNames = getNodesAsQNames( rootElement, new XPath( "wfs:TypeName", nsContext ) );
        String[] typeNames2 = getNodesAsStrings( rootElement, new XPath( "wfs:TypeName", nsContext ) );
        // TODO remove null namespace hack
        for ( int i = 0; i < typeNames.length; i++ ) {
            if ( typeNames[i] == null ) {
                typeNames[i] = mangleTypeName ( typeNames2[i] );
            } else if ( WFSConstants.WFS_NS.equals( typeNames[i].getNamespaceURI() ) ) {
                typeNames[i] = new QName( typeNames[i].getLocalPart() );
            }
        }

        return new DescribeFeatureType( VERSION_110, handle, outputFormat, typeNames, null );
    }

    /**
     * Parses a WFS 2.0.0 <code>DescribeFeatureType</code> document into a {@link DescribeFeatureType} request.
     * 
     * @return parsed {@link DescribeFeatureType} request
     * @throws XMLParsingException
     *             if a syntax error occurs in the XML
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public DescribeFeatureType parse200()
                            throws XMLParsingException, InvalidParameterValueException {

        // optional: '@handle'
        String handle = rootElement.getAttributeValue( new QName( "handle" ) );

        // optional: '@outputFormat'
        String outputFormat = rootElement.getAttributeValue( new QName( "outputFormat" ) );

        // 'wfs:TypeName' elements (minOccurs=0, maxOccurs=unbounded)
        QName[] typeNames = getNodesAsQNames( rootElement, new XPath( "wfs200:TypeName", nsContext ) );
        // TODO remove null namespace hack
        for ( int i = 0; i < typeNames.length; i++ ) {
            if ( WFSConstants.WFS_NS.equals( typeNames[i].getNamespaceURI() ) ) {
                typeNames[i] = new QName( typeNames[i].getLocalPart() );
            }
        }

        return new DescribeFeatureType( VERSION_110, handle, outputFormat, typeNames, null );
    }
}
