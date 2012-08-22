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
package org.deegree.ogcwebservices.csw.discovery;

import static org.deegree.ogcbase.CommonNamespaces.CSW202NS;
import static org.deegree.ogcbase.CommonNamespaces.CSWNS;

import java.util.List;

import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class GetRecordByIdResultDocument extends XMLFragment {

    private static final long serialVersionUID = 2796229558893029054L;

    /**
     * parses a GetRecordById response XML document and maps it to its corresponding java/deegree class
     * 
     * @param request
     * @return a new result object
     * @throws XMLParsingException
     */
    public GetRecordByIdResult parseGetRecordByIdResponse( GetRecordById request )
                            throws XMLParsingException {

        NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
        Element root = getRootElement();
        List<Node> records = XMLTools.getNodes( root, "./child::*", nsc );
        
        return new GetRecordByIdResult( request, records );
    }

    /**
     * creates an empty GetRecordByIdResponse document
     * 
     * @param version
     *            the desired version of the response document
     * 
     */
    public void createEmptyDocument( String version ) {
        Document doc = XMLTools.create();
        Element root = doc.createElementNS( version.equals( "2.0.2" ) ? CSW202NS.toASCIIString()
                                                                     : CSWNS.toASCIIString(),
                                            "csw:GetRecordByIdResponse" );
        setRootElement( root );
    }

}
