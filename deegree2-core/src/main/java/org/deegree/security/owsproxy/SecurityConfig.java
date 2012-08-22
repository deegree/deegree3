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
package org.deegree.security.owsproxy;

/**
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SecurityConfig {

    private int readWriteTimeout = 0;

    private String registryClass = null;

    private RegistryConfig registryConfig = null;

    private AuthentificationSettings authsettings = null;

    private String proxiedUrl;

    /**
     * @param readWriteTimeout
     * @param registryClass
     * @param registryConfig
     * @param authSet
     */
    public SecurityConfig( String registryClass, int readWriteTimeout, RegistryConfig registryConfig,
                           AuthentificationSettings authSet ) {
        this.readWriteTimeout = readWriteTimeout;
        this.registryClass = registryClass;
        this.registryConfig = registryConfig;
        this.authsettings = authSet;
    }

    /**
     *
     * @return readWriteTimeout
     */
    public int getReadWriteTimeout() {
        return readWriteTimeout;
    }

    /**
     * @param readWriteTimeout
     *
     */
    public void setReadWriteTimeout( int readWriteTimeout ) {
        this.readWriteTimeout = readWriteTimeout;
    }

    /**
     *
     * @return registryClass
     */
    public String getRegistryClass() {
        return registryClass;
    }

    /**
     * @param registryClass
     *
     */
    public void setRegistryClass( String registryClass ) {
        this.registryClass = registryClass;
    }

    /**
     *
     * @return registryConfig
     */
    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

    /**
     * @param registryConfig
     *
     */
    public void setRegistryConfig( RegistryConfig registryConfig ) {
        this.registryConfig = registryConfig;
    }

    /**
     *
     * @return authsettings
     */
    public AuthentificationSettings getAuthsettings() {
        return authsettings;
    }

    /**
     *
     * @param authsettings
     */
    public void setAuthsettings( AuthentificationSettings authsettings ) {
        this.authsettings = authsettings;
    }

    /**
     * @param proxiedUrl
     */
    public void setProxiedUrl( String proxiedUrl ) {
        this.proxiedUrl = proxiedUrl;
    }

    /**
     * @return the URL of the secured service, or null, if not set
     */
    public String getProxiedUrl() {
        return proxiedUrl;
    }

}
