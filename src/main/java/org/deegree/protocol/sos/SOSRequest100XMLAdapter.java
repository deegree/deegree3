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
package org.deegree.protocol.sos;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.ows.OWSCommonXMLAdapter;

/**
 * Base class for all SOS 1.0.0 XMLAdapter. Defines the SOS XML namespace.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class SOSRequest100XMLAdapter extends OWSCommonXMLAdapter {

    private static final String SOS_PREFIX = "sos";

    private static final String SOS_NS = "http://www.opengis.net/sos/1.0";

    private static final String GML_PREFIX = "gml";

    private static final String GML_NS = "http://www.opengis.net/gml";

    private static final String OGC_PREFIX = "ogc";

    private static final String OGC_NS = "http://www.opengis.net/ogc";

    /**
     * namespace context with sos ns
     */
    protected final static NamespaceContext nsContext;

    static {
        nsContext = new NamespaceContext( XMLAdapter.nsContext );
        nsContext.addNamespace( SOS_PREFIX, SOS_NS );
        nsContext.addNamespace( GML_PREFIX, GML_NS );
        nsContext.addNamespace( OGC_PREFIX, OGC_NS );
    }

    /**
     *
     */
    protected SOSRequest100XMLAdapter() {
        // protected
        super();
    }

    /**
     * Return the required node, throws an OWS MissingParameterValue exception if the node is missing.
     * 
     * @param root
     * @param xpath
     * @return the node value
     * @throws OWSException
     */
    protected String getRequiredStringNode( OMElement root, XPath xpath )
                            throws XMLParsingException {
        try {
            return getRequiredNodeAsString( root, xpath );
        } catch ( XMLParsingException ex ) {
            String elemName = xpath.getXPath();
            String type = "element";
            int atPos = elemName.lastIndexOf( "@" );
            if ( atPos != -1 ) {
                elemName = elemName.substring( atPos + 1 );
                type = "attribute";
            } else {
                int colonPos = elemName.lastIndexOf( ":" );
                if ( colonPos != -1 ) {
                    elemName = elemName.substring( colonPos + 1 );
                }
            }
            throw new XMLParsingException( this, root, "required " + type + " " + xpath.getXPath() + " is missing: "
                                                       + elemName );
        }
    }
}
