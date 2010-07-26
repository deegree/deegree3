//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.services.controller.security;

import static org.deegree.services.controller.OGCFrontController.resolveFileLocation;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.services.controller.CredentialsProvider;
import org.deegree.services.controller.CredentialsProviderManager;
import org.deegree.services.controller.OGCFrontController;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SecurityConfiguration {

    private static final Logger LOG = getLogger( SecurityConfiguration.class );

    private static final String DEFAULT_SECURITY_PATH = OGCFrontController.DEFAULT_CONFIG_PATH
                                                        + "/services/security/security.xml";

    private org.deegree.services.jaxb.security.SecurityConfiguration config;

    private CredentialsProvider providers;

    /**
     * @param context
     */
    public SecurityConfiguration( ServletContext context ) {
        URL securityURL = null;
        try {
            securityURL = resolveFileLocation( DEFAULT_SECURITY_PATH, context );
        } catch ( MalformedURLException e ) {
            LOG.debug( "Could not resolve the location of security.xml: '{}'", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
        if ( securityURL == null ) {
            LOG.debug( "No security.xml found." );
        } else {
            String contextName = "org.deegree.services.jaxb.security";
            try {
                JAXBContext jc = JAXBContext.newInstance( contextName );
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                config = (org.deegree.services.jaxb.security.SecurityConfiguration) unmarshaller.unmarshal( securityURL );
                if ( config.getCredentialsProvider() != null ) {
                    providers = CredentialsProviderManager.create( config.getCredentialsProvider() );
                }
            } catch ( JAXBException e ) {
                LOG.debug( "Could not load security.xml: '{}'", e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            }
        }
    }

    /**
     * @return the credentials provider or null, if none was defined
     */
    public CredentialsProvider getCredentialsProvider() {
        return providers;
    }

}
