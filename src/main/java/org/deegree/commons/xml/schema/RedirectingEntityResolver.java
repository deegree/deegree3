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

import static org.deegree.commons.i18n.Messages.getMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

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

    private static final String ROOT = "/META-INF/SCHEMAS_OPENGIS_NET/";

    private static final String LISTING = ".LISTING";

    private static final URL baseURL;

    private static Set<String> availableFiles = new HashSet<String>();

    static {
        baseURL = RedirectingEntityResolver.class.getResource( ROOT + LISTING );
        if ( baseURL == null ) {
            LOG.warn( getMessage( "XML_SCHEMAS_NO_LOCAL_COPY", ROOT + LISTING ) );
        }
        try {
            BufferedReader reader = new BufferedReader(
                                                        new InputStreamReader( new URL( baseURL, LISTING ).openStream() ) );
            String line = null;
            while ( ( line = reader.readLine() ) != null ) {
                availableFiles.add( line.trim() );
            }
        } catch ( Exception e ) {
            LOG.warn( getMessage( "XML_SCHEMAS_ERROR_READING_LISTING", ROOT + LISTING, e.getMessage() ) );
        }
    }

    private String redirect( String systemId ) {
        if ( systemId.startsWith( SCHEMAS_OPENGIS_NET_URL ) ) {
            String localPart = "./" + systemId.substring( SCHEMAS_OPENGIS_NET_URL.length() );
            if ( availableFiles.contains( localPart ) ) {
                LOG.debug( "Local hit: " + systemId );
                try {
                    return new URL( baseURL, localPart ).toString();
                } catch ( MalformedURLException e ) {
                    // should never happen
                }
            }
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
