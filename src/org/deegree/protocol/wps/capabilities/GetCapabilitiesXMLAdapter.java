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

package org.deegree.protocol.wps.capabilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.ows.capabilities.GetCapabilities;

/**
 * Parser for WPS GetCapabilities requests.
 * 
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author last edited by: $Author: $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetCapabilitiesXMLAdapter extends XMLAdapter {

    private static final String OWS_PREFIX = "ows";

    private static final String OWS_NS = "http://www.opengis.net/ows/1.1";

    private static final String WPS_PREFIX = "wps";

    private static final String WPS_NS = "http://www.opengis.net/wps/1.0.0";

    private static NamespaceContext nsContext;

    static {
        nsContext = new NamespaceContext( XMLAdapter.nsContext );
        nsContext.addNamespace( OWS_PREFIX, OWS_NS );
        nsContext.addNamespace( WPS_PREFIX, WPS_NS );
    }

    /**
     * Parses a WPS 1.0.0 GetCapabilities request.
     * <p>
     * NOTE: Because GetCapabilities-requests from the WPS specification 1.0.0 do not match OWS-Commons
     * GetCapabilities-requests, this individual XMLAdapter is needed for WPS GetCapabilities-requests. Although the
     * adapter has to be different, the common request bean can be used.
     * </p>
     * 
     * @return the parsed request
     * @throws InvalidParameterValueException
     */
    public GetCapabilities parse100()
                            throws InvalidParameterValueException {

        // ows:AcceptVersions (optional)
        String[] versions = getNodesAsStrings( rootElement, new XPath( "ows:AcceptVersions/ows:Version/text()",
                                                                       nsContext ) );

        // @language (optional)
        List<String> languages = null;
        String languageString = rootElement.getAttributeValue( new QName( "language" ) );
        if ( languageString != null ) {
            languages = new ArrayList<String>();
            languages.add( languageString );
        }
        return new GetCapabilities( Arrays.asList( versions ), null, null, null, languages );
    }
}
