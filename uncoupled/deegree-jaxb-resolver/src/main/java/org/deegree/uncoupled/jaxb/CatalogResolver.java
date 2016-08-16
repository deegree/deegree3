/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2014 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit GmbH -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.uncoupled.jaxb;

import java.net.URL;

/**
 * JAXB Catalog Resolver
 * 
 * The Resolver is build to allow JAXB to find deegree bundled schemas inside the build classpath instead of resolving
 * schema files online.
 * 
 * <blockquote> <b>Note:</b> Eclipse only builds this class if the following setting is changed, because of internal
 * API:
 * 
 * <pre>
 * Window > Preferences > Java > Compiler > Errors/Warnings
 *  - Deprecated and restricted API
 *    Set "Forbidden reference (access rules)" to "Warning"
 * </pre>
 * 
 * </blockquote>
 * 
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public class CatalogResolver extends com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver {

    private static final String REWRITE_FROM = "http://schemas.deegree.org/";

    private static final String REWRITE_TO = "META-INF/schemas/";

    private boolean debug = false;

    public CatalogResolver() {
        String dbg = System.getProperty( "deeegree.jaxb.debug" );
        if ( dbg != null && !dbg.isEmpty() ) {
            debug = true;
        }
    }

    @Override
    public String getResolvedEntity( String publicId, String systemId ) {

        if ( systemId != null && systemId.toLowerCase().startsWith( REWRITE_FROM ) ) {
            if ( debug ) {
                System.err.println( "*** deegree JAXB CatalogResolver: publicId: " + publicId + " systemId: "
                                    + systemId );
            }

            String newid = REWRITE_TO + systemId.substring( REWRITE_FROM.length() );

            try {
                URL resource = Thread.currentThread().getContextClassLoader().getResource( newid );
                if ( resource == null ) {
                    resource = getClass().getResource( "/" + newid );
                }
                if ( resource != null ) {
                    if ( debug ) {
                        System.err.println( "Resolved localy to: " + resource );
                    }
                    return resource.toString();
                }
            } catch ( Exception ex ) {
                System.err.println( "Error: " + ex.getMessage() );
            }
        }

        // Use default lookup
        return super.getResolvedEntity( publicId, systemId );
    }
}