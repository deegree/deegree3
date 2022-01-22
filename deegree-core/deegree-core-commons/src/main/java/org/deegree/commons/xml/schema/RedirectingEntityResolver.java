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
package org.deegree.commons.xml.schema;

import java.io.IOException;
import java.net.URL;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Xerces entity resolver that performs redirection of requests for OpenGIS core schemas (e.g. GML) to a local copy on
 * the classpath.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class RedirectingEntityResolver implements XMLEntityResolver {

    private static final Logger LOG = LoggerFactory.getLogger( RedirectingEntityResolver.class );

    private static final String SCHEMAS_OPENGIS_NET_URL = "http://schemas.opengis.net/";

    public static final String INSPIRE_SCHEMAS_URL = "http://inspire.ec.europa.eu/schemas";

    private static final String ROOT = "/META-INF/SCHEMAS_OPENGIS_NET/";

    private static final URL baseURL;

    static {
        baseURL = RedirectingEntityResolver.class.getResource( ROOT );
        if ( baseURL == null ) {
            LOG.warn( "'"
                      + ROOT
                      + "' could not be found on the classpath. Schema references to 'http://schemas.opengis.net' will not be redirected, but fetched from their original location.  " );
        }
    }

    /**
     * Redirects the given entity URL, returning a local URL if available.
     * 
     * @param systemId
     *            entity URL, must not be <code>null</code>
     * @return redirected URL, identical to input if it cannot be redirected, never <code>null</code>
     */
    public String redirect( String systemId ) {
        if ( systemId.startsWith( SCHEMAS_OPENGIS_NET_URL ) ) {
            String localPart = systemId.substring( SCHEMAS_OPENGIS_NET_URL.length() );
            URL u = RedirectingEntityResolver.class.getResource( ROOT + localPart );
            if ( u != null ) {
                LOG.debug( "Local hit: " + systemId );
                return u.toString();
            }
        } else if ( systemId.startsWith( INSPIRE_SCHEMAS_URL ) ) {
            return systemId.replaceFirst( "http://", "https://" );
        } else if ( systemId.equals( "http://www.w3.org/2001/xml.xsd" ) ) {
            // workaround for schemas that include the xml base schema...
            return RedirectingEntityResolver.class.getResource( "/w3c/xml.xsd" ).toString();
        } else if ( systemId.equals( "http://www.w3.org/1999/xlink.xsd" ) ) {
            // workaround for schemas that include the xlink schema...
            return RedirectingEntityResolver.class.getResource( "/w3c/xlink.xsd" ).toString();
        }
        return systemId;
    }

    @Override
    public XMLInputSource resolveEntity( XMLResourceIdentifier identifier )
                            throws XNIException, IOException {

        String systemId = identifier.getExpandedSystemId();
        String redirectedSystemId = systemId != null ? redirect( systemId ) : null;
        LOG.debug( "'" + systemId + "' -> '" + redirectedSystemId + "'" );
        return new XMLInputSource( null, redirectedSystemId, null );
    }

}
