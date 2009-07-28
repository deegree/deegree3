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

package org.deegree.protocol.wfs.getgmlobject;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;

import javax.xml.namespace.QName;

import org.deegree.commons.types.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.AbstractWFSRequestXMLAdapter;

/**
 * Adapter between XML encoded <code>GetGmlObject</code> requests and {@link GetGmlObject} objects.
 * <p>
 * TODO code for exporting to XML
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetGmlObjectXMLAdapter extends AbstractWFSRequestXMLAdapter {

    /**
     * Parses a WFS <code>GetGmlObject</code> document into a {@link GetGmlObject} request.
     * <p>
     * Supported versions:
     * <ul>
     * <li>WFS 1.1.0</li>
     * </ul>
     * 
     * @return parsed {@link GetGmlObject} request
     * @throws XMLParsingException
     *             if a syntax error occurs in the XML
     * @throws MissingParameterException
     *             if the request version is unsupported
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public GetGmlObject parse() {
        Version version = Version.parseVersion( getRequiredNodeAsString( rootElement, new XPath( "@version", nsContext ) ) );

        GetGmlObject result = null;
        if ( VERSION_110.equals( version ) ) {
            result = parse110();
        } else {
            String msg = Messages.get( "UNSUPPORTED_VERSION", version, Version.getVersionsString( VERSION_110 ) );
            throw new InvalidParameterValueException( msg );
        }
        return result;
    }

    /**
     * Parses a WFS 1.1.0 <code>GetGmlObject</code> document into a {@link GetGmlObject} request.
     * 
     * @return parsed {@link GetGmlObject} request
     * @throws XMLParsingException
     *             if a syntax error occurs in the XML
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public GetGmlObject parse110()
                            throws XMLParsingException, InvalidParameterValueException {

        // optional: '@handle'
        String handle = rootElement.getAttributeValue( new QName( "handle" ) );

        // optional: '@outputFormat'
        String outputFormat = rootElement.getAttributeValue( new QName( "outputFormat" ) );

        // required: '@traverseXlinkDepth'
        String traverseXlinkDepth = getRequiredNodeAsString( rootElement, new XPath( "@traverseXlinkDepth", nsContext ) );

        // optional: '@traverseXlinkExpiry'
        Integer traverseXlinkExpiry = getNodeAsInt( rootElement, new XPath( "@traverseXlinkExpiry", nsContext ), -1 );
        if ( traverseXlinkExpiry < 0 ) {
            traverseXlinkExpiry = null;
        }

        // required: 'ogc:GmlObjectId/@gml:id'
        String requestedId = getRequiredNodeAsString( rootElement, new XPath( "ogc:GmlObjectId/@gml:id", nsContext ) );

        return new GetGmlObject( VERSION_110, handle, requestedId, outputFormat, traverseXlinkDepth, traverseXlinkExpiry );
    }
}
