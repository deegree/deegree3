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

package org.deegree.ogcwebservices.wmps.configuration;

/**
 * This class is a container for the database used to cache the asynchronous request.
 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh</a>
 *
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class CacheDatabase {

    private String driver;

    private String url;

    private String user;

    private String password;

    /**
     * Create a new CacheDatabase instance.
     *
     * @param driver
     * @param url
     * @param user
     * @param password
     */
    public CacheDatabase( String driver, String url, String user, String password ) {

        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * @return Returns the driver.
     */
    public String getDriver() {
        return this.driver;
    }

    /**
     * @param driver
     *            The driver to set.
     */
    public void setDriver( String driver ) {
        this.driver = driver;
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * @param password
     *            The password to set.
     */
    public void setPassword( String password ) {
        this.password = password;
    }

    /**
     * @return Returns the url.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * @param url
     *            The url to set.
     */
    public void setUrl( String url ) {
        this.url = url;
    }

    /**
     * @return Returns the user.
     */
    public String getUser() {
        return this.user;
    }

    /**
     * @param user
     *            The user to set.
     */
    public void setUser( String user ) {
        this.user = user;
    }
}

