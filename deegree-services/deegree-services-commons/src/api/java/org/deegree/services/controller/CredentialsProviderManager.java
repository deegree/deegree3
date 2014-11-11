//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.services.controller;

import org.deegree.services.authentication.DeegreeAuthentication;
import org.deegree.services.authentication.EcasAndHttpBasicAuthentication;
import org.deegree.services.authentication.HttpBasicAuthentication;
import org.deegree.services.authentication.SOAPAuthentication;
import org.deegree.services.jaxb.security.CredentialsProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for creating {@link CredentialsProvider} instances from XML elements (JAXB objects).
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class CredentialsProviderManager {

    private static final Logger LOG = LoggerFactory.getLogger( CredentialsProviderManager.class );

    /**
     * Creates the authentication method that is specified in the configuration.
     * 
     * @param authentication
     *            is one of the authentication methods that are implemented.
     * @return the configured {@link CredentialsProvider}, never <code>null</code>
     */
    public static CredentialsProvider create( CredentialsProviderType authentication ) {

        CredentialsProvider credentialsProvider = null;

        if ( authentication.getHttpBasicCredentialsProvider() != null ) {
            credentialsProvider = new HttpBasicAuthentication();
            LOG.debug( "httpBasicAuth" );
        } else if ( authentication.getDeegreeCredentialsProvider() != null ) {
            credentialsProvider = new DeegreeAuthentication();
            LOG.debug( "deegreeAuth" );
        } else if ( authentication.getSOAPCredentialsProvider() != null ) {
            credentialsProvider = new SOAPAuthentication();
            LOG.debug( "SOAPAuth" );
        } else if ( authentication.getHttpDigestCredentialsProvider() != null ) {
            LOG.debug( "digestAuth not implemented yet" );
            throw new UnsupportedOperationException( "digestAuth not implemented yet" );
        } else if ( authentication.getEcasHttpBasicCredentialsProvider() != null ) {
            credentialsProvider = new EcasAndHttpBasicAuthentication();
            LOG.debug( "Basic and Ecas Auth" );
        }
        return credentialsProvider;
    }
}
