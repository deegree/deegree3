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
package org.deegree.ogcwebservices.wass.was.operation;

import java.io.IOException;

import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.security.drm.model.User;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * <code>DescribeUserResponse</code> is an XML document class used as response object
 * for the DescribeUser operation.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class DescribeUserResponse extends XMLFragment {

    private static final long serialVersionUID = -4883638653028678703L;

    protected static final String XML_TEMPLATE = "describeusertemplate.xml";

    /**
     * Constructs a new response document.
     *
     * @param user the user object to extract the response values from.
     * @param sessionID the user's session ID
     * @throws IOException
     * @throws SAXException
     * @throws XMLParsingException
     */
    public DescribeUserResponse( User user, String sessionID ) throws IOException, SAXException,
                                                  XMLParsingException {
        super( DescribeUserResponse.class.getResource( XML_TEMPLATE ) );

        NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();
        Element root = getRootElement();
        root.setAttribute( "id", sessionID );

        Element userName = (Element) XMLTools.getRequiredNode( root, "UserName", nsContext );
        Element firstName = (Element) XMLTools.getRequiredNode( root, "FirstName", nsContext );
        Element lastName = (Element) XMLTools.getRequiredNode( root, "LastName", nsContext );
        Element password = (Element) XMLTools.getRequiredNode( root, "Password", nsContext );
        Element email = (Element) XMLTools.getRequiredNode( root, "EMailAddress", nsContext );

        userName.setTextContent( user.getName() );
        firstName.setTextContent( user.getFirstName() );
        lastName.setTextContent( user.getLastName() );
        password.setTextContent( user.getPassword() );
        email.setTextContent( user.getEmailAddress() );
    }

}

