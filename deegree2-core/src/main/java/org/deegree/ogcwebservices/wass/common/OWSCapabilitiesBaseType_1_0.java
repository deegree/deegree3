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

package org.deegree.ogcwebservices.wass.common;

import java.util.ArrayList;

import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;

/**
 * Encapsulates: OWS capabilities according to V1.0
 *
 * Namespace: http://www.opengis.net/ows
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class OWSCapabilitiesBaseType_1_0 extends OGCCapabilities {

    private static final long serialVersionUID = -7316008493729217865L;

    private ServiceIdentification serviceIdentification = null;

    private ServiceProvider serviceProvider = null;

    private OperationsMetadata_1_0 operationsMetadata = null;

    private ArrayList<SupportedAuthenticationMethod> authenticationMethods = null;

    private boolean passwordAuthenticationSupported = false;

    private boolean sessionAuthenticationSupported = false;

    private boolean wasAuthenticationSupported = false;

    private boolean anonymousAuthenticationSupported = false;

    /**
     * Creates new instance from the given data.
     *
     * @param version
     * @param updateSequence
     * @param serviceIdentification
     * @param serviceProvider
     * @param operationsMetadata
     * @param authenticationMethods
     */
    public OWSCapabilitiesBaseType_1_0(
                                       String version,
                                       String updateSequence,
                                       ServiceIdentification serviceIdentification,
                                       ServiceProvider serviceProvider,
                                       OperationsMetadata_1_0 operationsMetadata,
                                       ArrayList<SupportedAuthenticationMethod> authenticationMethods ) {
        super( version, updateSequence );
        this.serviceIdentification = serviceIdentification;
        this.serviceProvider = serviceProvider;
        this.operationsMetadata = operationsMetadata;
        this.authenticationMethods = authenticationMethods;

        for( SupportedAuthenticationMethod method: this.authenticationMethods ){
            if (method.getMethod().isWellformedGDINRW() && method.getMethod().getAuthenticationMethod().equals( "password" ) )
                passwordAuthenticationSupported = true;
            if (method.getMethod().isWellformedGDINRW() && method.getMethod().getAuthenticationMethod().equals( "session" ) )
                sessionAuthenticationSupported = true;
            if (method.getMethod().isWellformedGDINRW() && method.getMethod().getAuthenticationMethod().equals( "was" ) )
                wasAuthenticationSupported = true;
            if (method.getMethod().isWellformedGDINRW() && method.getMethod().getAuthenticationMethod().equals( "anonymous" ) )
                anonymousAuthenticationSupported = true;
        }


    }

    /**
     * @return the OperationsMetadata
     */
    public OperationsMetadata_1_0 getOperationsMetadata() {
        return operationsMetadata;
    }

    /**
     * @return the ServiceIdentification
     */
    public ServiceIdentification getServiceIdentification() {
        return serviceIdentification;
    }

    /**
     * @return the ServiceProvider
     */
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    /**
     * @return Returns the SupportedAuthenticationMethods.
     */
    public ArrayList<SupportedAuthenticationMethod> getAuthenticationMethods() {
        return authenticationMethods;
    }

    /**
     * @param authMethod the method to check for.
     * @return true if the method is supported
     */
    public boolean isAuthenticationMethodSupported( String authMethod ){
        for ( SupportedAuthenticationMethod method : authenticationMethods ){
            if( method.getMethod().getAuthenticationMethod().equals( authMethod ) )
                return true;
        }
        return false;
    }

    /**
     * @return Returns true if anonymousAuthentication is Supported.
     */
    public boolean isAnonymousAuthenticationSupported() {
        return anonymousAuthenticationSupported;
    }

    /**
     * @return Returns true if passwordAuthentication is Supported.
     */
    public boolean isPasswordAuthenticationSupported() {
        return passwordAuthenticationSupported;
    }

    /**
     * @return Returns true if sessionAuthentication is Supported.
     */
    public boolean isSessionAuthenticationSupported() {
        return sessionAuthenticationSupported;
    }

    /**
     * @return Returns true if wasAuthentication (SAML) is Supported.
     */
    public boolean isWasAuthenticationSupported() {
        return wasAuthenticationSupported;
    }

}
