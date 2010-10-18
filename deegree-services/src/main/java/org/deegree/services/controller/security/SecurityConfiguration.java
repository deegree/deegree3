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

import static java.io.File.separator;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ServiceLoader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.services.controller.Credentials;
import org.deegree.services.controller.CredentialsProvider;
import org.deegree.services.controller.CredentialsProviderManager;
import org.deegree.services.controller.security.authorities.AuthenticationAuthority;
import org.deegree.services.controller.security.authorities.AuthenticationAuthorityProvider;
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

    private static HashMap<String, AuthenticationAuthorityProvider> authenticationAuthorityProviders = new HashMap<String, AuthenticationAuthorityProvider>();

    private CredentialsProvider providers;

    private ArrayList<AuthenticationAuthority> authorities = new ArrayList<AuthenticationAuthority>();

    private DeegreeWorkspace workspace;

    static {
        ServiceLoader<AuthenticationAuthorityProvider> loader = ServiceLoader.load( AuthenticationAuthorityProvider.class );
        for ( AuthenticationAuthorityProvider auth : loader ) {
            authenticationAuthorityProviders.put( auth.getConfigNamespace(), auth );
        }
    }

    /**
     * @param workspace
     */
    public SecurityConfiguration( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    /**
     * 
     */
    public void init() {
        File securityFile = new File( workspace.getLocation(), "services" + separator + "security" + separator
                                                               + "security.xml" );
        if ( !securityFile.exists() ) {
            LOG.info( "No security.xml found." );
            return;
        }

        String contextName = "org.deegree.services.jaxb.security";
        try {
            JAXBContext jc = JAXBContext.newInstance( contextName );
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            org.deegree.services.jaxb.security.SecurityConfiguration config;
            config = (org.deegree.services.jaxb.security.SecurityConfiguration) unmarshaller.unmarshal( securityFile );
            if ( config.getCredentialsProvider() != null ) {
                providers = CredentialsProviderManager.create( config.getCredentialsProvider() );
            }
        } catch ( JAXBException e ) {
            LOG.warn( "Could not load security.xml: '{}'", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }

        File authorities = new File( workspace.getLocation(), "services" + separator + "security" + separator
                                                              + "authorities" );
        XMLInputFactory fac = XMLInputFactory.newInstance();
        if ( authorities.exists() && authorities.isDirectory() ) {
            for ( File f : authorities.listFiles( new FileFilter() {
                public boolean accept( File f ) {
                    return f.toString().toLowerCase().endsWith( ".xml" );
                }
            } ) ) {
                try {
                    XMLStreamReader in = fac.createXMLStreamReader( new FileInputStream( f ) );
                    in.next();
                    String ns = in.getNamespaceURI();
                    if ( ns == null ) {
                        LOG.info( "The namespace in '{}' was not set, skipping file.", f );
                        continue;
                    }
                    AuthenticationAuthorityProvider prov = authenticationAuthorityProviders.get( ns );
                    if ( prov == null ) {
                        LOG.info( "No authentication authority provider for"
                                  + " namepace '{}', in file '{}', skipping.", ns, f );
                        continue;
                    }
                    this.authorities.add( prov.getAuthenticationAuthority( f.toURI().toURL() ) );
                } catch ( FileNotFoundException e ) {
                    LOG.debug( "File '{}' could not be found?!?", f );
                    LOG.trace( "Stack trace:", e );
                } catch ( XMLStreamException e ) {
                    LOG.debug( "File '{}' could not be parsed as XML, skipping.", f );
                    LOG.trace( "Stack trace:", e );
                } catch ( MalformedURLException e ) {
                    LOG.debug( "File '{}' could not be found?!?", f );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }

    /**
     * @return the credentials provider or null, if none was defined
     */
    public CredentialsProvider getCredentialsProvider() {
        return providers;
    }

    /**
     * @param creds
     * @return true, if an authentication authority does
     */
    public boolean checkCredentials( Credentials creds ) {
        for ( AuthenticationAuthority auth : authorities ) {
            if ( auth.isAuthorized( creds ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param creds
     * @param address
     * @return true, if an authentication authority does
     */
    public boolean verifyAddress( Credentials creds, String address ) {
        for ( AuthenticationAuthority auth : authorities ) {
            if ( auth.verifyAddress( creds, address ) ) {
                return true;
            }
        }
        return false;
    }

}
