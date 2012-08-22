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
package org.deegree.ogcwebservices.wfs.operation;

import java.util.HashMap;

import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.wfs.WFService;
import org.w3c.dom.Element;

/**
 * Parser for "wfs:GetCapabilities" requests.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFSGetCapabilitiesDocument extends AbstractWFSRequestDocument {

    private static final long serialVersionUID = -1901946322324983262L;

    /**
     * Parses the underlying document into a {@link WFSGetCapabilities} request object.
     *
     * @param id
     * @return corresponding <code>GetCapabilities</code> object
     * @throws XMLParsingException
     * @throws InvalidParameterValueException
     */
    public WFSGetCapabilities parse( String id )
                            throws XMLParsingException, InvalidParameterValueException {

        checkServiceAttribute();

        Element root = this.getRootElement();
        String version = root.getAttribute( "version" );

        if ( version == null || "".equals( version ) ) {
            version = WFService.VERSION;
        }

        String updateSeq = XMLTools.getNodeAsString( root, "@updateSequence", nsContext, null );
        String[] acceptVersions = XMLTools.getNodesAsStrings( root, "ows:AcceptVersions/ows:Version", nsContext );
        String[] sections = XMLTools.getNodesAsStrings( root, "ows:AcceptFormats/ows:Sections", nsContext );
        String[] acceptFormats = XMLTools.getNodesAsStrings( root, "ows:AcceptVersions/ows:OutputFormat", nsContext );
        return new WFSGetCapabilities( id, version, updateSeq, acceptVersions, sections, acceptFormats,
                                       new HashMap<String, String>() );
    }
}
