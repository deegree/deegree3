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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.OGCDocument;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.wfs.WFService;
import org.w3c.dom.Element;

/**
 * Abstract base class for WFS request documents / parsers.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class AbstractWFSRequestDocument extends OGCDocument {

    private static final long serialVersionUID = -3826447710328793808L;

    private static String SERVICE_NAME = "WFS";

    /**
     * Checks that the "service" attribute in the root node matches the expected value (WFS).
     *
     * @throws XMLParsingException
     */
    protected void checkServiceAttribute()
                            throws InvalidParameterValueException, XMLParsingException {
        String service = XMLTools.getNodeAsString( getRootElement(), "@service", nsContext, SERVICE_NAME );
        if ( service.equals( "" ) ) {
            service = SERVICE_NAME;
        }
        if ( !SERVICE_NAME.equals( service ) ) {
            throw new InvalidParameterValueException( "service", "Service attribute must be '" + SERVICE_NAME + "'." );
        }
    }

    /**
     * Parses and checks the "version" attribute in the root node (can be "1.0.0" or "1.1.0"). If it is not present,
     * "1.1.0" is returned.
     *
     * @return version
     * @throws XMLParsingException
     */
    protected String checkVersionAttribute()
                            throws XMLParsingException, InvalidParameterValueException {
        String version = XMLTools.getNodeAsString( this.getRootElement(), "@version", nsContext, WFService.VERSION );
        if ( version.equals( "" ) ) {
            version = WFService.VERSION;
        }
        if ( !WFService.VERSION.equals( version ) && !"1.0.0".equals( version ) ) {
            String msg = Messages.getMessage( "WFS_REQUEST_UNSUPPORTED_VERSION", version, "1.0.0 and "
                                                                                          + WFService.VERSION );
            throw new InvalidParameterValueException( "version", msg );
        }
        return version;
    }

    /**
     * Transform an array of strings to an array of qualified names.
     *
     * TODO adapt style (parseXYZ)
     *
     * @param values
     * @param element
     * @return QualifiedNames
     * @throws XMLParsingException
     */
    protected QualifiedName[] transformToQualifiedNames( String[] values, Element element )
                            throws XMLParsingException {
        QualifiedName[] typeNames = new QualifiedName[values.length];
        for ( int i = 0; i < values.length; i++ ) {
            int idx = values[i].indexOf( ":" );
            if ( idx != -1 ) {
                String prefix = values[i].substring( 0, idx );
                String name = values[i].substring( idx + 1 );
                URI uri;
                try {
                    uri = XMLTools.getNamespaceForPrefix( prefix, element );
                } catch ( URISyntaxException e ) {
                    throw new XMLParsingException( e.getMessage(), e );
                }
                typeNames[i] = new QualifiedName( prefix, name, uri );
            } else {
                typeNames[i] = new QualifiedName( values[i] );
            }
        }
        return typeNames;
    }

    protected Map<String, String> parseDRMParams( Element root )
                            throws XMLParsingException {
        String user = XMLTools.getNodeAsString( root, "@user", nsContext, null );
        String password = XMLTools.getNodeAsString( root, "@password", nsContext, null );
        String sessionID = XMLTools.getNodeAsString( root, "@sessionID", nsContext, null );
        Map<String, String> vendorSpecificParam = new HashMap<String, String>();
        vendorSpecificParam.put( "USER", user );
        vendorSpecificParam.put( "PASSWORD", password );
        vendorSpecificParam.put( "SESSIONID", sessionID );
        return vendorSpecificParam;
    }
}
