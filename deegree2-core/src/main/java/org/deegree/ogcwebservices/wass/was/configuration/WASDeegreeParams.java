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
package org.deegree.ogcwebservices.wass.was.configuration;

import org.deegree.enterprise.DeegreeParams;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.io.JDBCConnection;
import org.deegree.model.metadata.iso19115.OnlineResource;

/**
 * Encapsulates the deegree parameters for a WAS configuration.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class WASDeegreeParams extends DeegreeParams {

    private static final long serialVersionUID = 2700771143650528537L;

    private OnlineResource wasAddress = null;

    private int sessionLifetime = 0;

    private JDBCConnection databaseConnection;

    /**
     * @param defaultOnlineResource
     * @param cacheSize
     * @param requestTimeLimit
     * @param wasAddress
     * @param sessionLifetime
     * @param database
     */
    public WASDeegreeParams( OnlineResource defaultOnlineResource, int cacheSize,
                            int requestTimeLimit, OnlineResource wasAddress, int sessionLifetime,
                            JDBCConnection database ) {
        this( defaultOnlineResource, cacheSize, requestTimeLimit,
              CharsetUtils.getSystemCharset(), wasAddress, sessionLifetime, database );
    }

    /**
     *
     * @param defaultOnlineResource
     * @param cacheSize
     * @param requestTimeLimit
     * @param characterSet
     * @param wasAddress
     * @param sessionLifetime
     * @param database
     */
    public WASDeegreeParams( OnlineResource defaultOnlineResource, int cacheSize,
                            int requestTimeLimit, String characterSet, OnlineResource wasAddress,
                            int sessionLifetime, JDBCConnection database ) {
        super( defaultOnlineResource, cacheSize, requestTimeLimit, characterSet );
        this.wasAddress = wasAddress;
        this.sessionLifetime = sessionLifetime;
        this.databaseConnection = database;
    }

    /**
     * returns the address of the WAS to be used to authenticate users
     *
     * @return the address
     */
    public OnlineResource getWASAddress() {
        return wasAddress;
    }

    /**
     * @return the maximum session lifetime in milliseconds.
     */
    public int getSessionLifetime() {
        return sessionLifetime;
    }

    /**
     * @return an object containing database connection information
     */
    public JDBCConnection getDatabaseConnection() {
        return databaseConnection;
    }
}
