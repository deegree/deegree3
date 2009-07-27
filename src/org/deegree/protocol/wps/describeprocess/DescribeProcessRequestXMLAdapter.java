//$Header: /deegreerepository/deegree/resources/eclipse/svn_classfile_header_template.xml,v 1.2 2007/03/06 09:44:09 bezema Exp $
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

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.types.ows.CodeType;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.wps.WPSConstants;

/**
 * Parser for WPS <code>DescribeProcess</code> XML requests.
 *
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: padberg$
 *
 * @version $Revision$, $Date: 09.05.2008 11:44:19$
 */
public class DescribeProcessRequestXMLAdapter extends XMLAdapter {

    private static final String OWS_PREFIX = "ows";

    private static final String OWS_NS = "http://www.opengis.net/ows/1.1";

    private static NamespaceContext nsContext;

    static {
        nsContext = new NamespaceContext( XMLAdapter.nsContext );
        nsContext.addNamespace( OWS_PREFIX, OWS_NS );
        nsContext.addNamespace( WPSConstants.WPS_PREFIX, WPSConstants.WPS_100_NS );
    }

    /**
     * Parses the encapsulated WPS 1.0.0 &lt;<code>DescribeProcess</code>&gt; element.
     * <p>
     * Prerequisites (not checked by this method):
     * <ul>
     * <li>The name of the encapsulated element is a &lt;<code>wps:DescribeProcess</code>&gt;
     * (wps="http://www.opengis.net/wps/1.0.0").</li>
     * <li>The <code>version</code> attribute of the element must have the value <code>1.0.0</code>.</li>
     * </p>
     *
     * @return corresponding <code>DescribeProcessRequest</code> object
     * @throws XMLParsingException
     *             if a syntactical or semantical error has been encountered in the request document
     */
    public DescribeProcessRequest parse100()
                            throws XMLParsingException {

        // language attribute (optional)
        String language = getNodeAsString( rootElement, new XPath( "@language", nsContext ), null );

        // ows:Identifier elements (minOccurs="1", maxOccurs="unbounded")
        List<CodeType> identifiers = new ArrayList<CodeType>();
        List<OMElement> identifierElements = getRequiredElements( rootElement, new XPath( "ows:Identifier", nsContext ) );
        for ( OMElement identifierElement : identifierElements ) {
            identifiers.add( parseCodeType( identifierElement ) );
        }
        return new DescribeProcessRequest( WPSConstants.VERSION_100, language, identifiers );
    }

    private CodeType parseCodeType( OMElement codeTypeElement ) {

        // codeSpace attribute (optional)
        String codeSpace = codeTypeElement.getAttributeValue( new QName( "codeSpace" ) );

        // text value
        String value = codeTypeElement.getText();

        return new CodeType( value, codeSpace );
    }
}
