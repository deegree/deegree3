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
package org.deegree.ogcwebservices.wms.capabilities;

import java.net.URL;

/**
 * The optional Attribution element provides a way to identify the source of the map data used in a
 * Layer or collection of Layers. Attribution encloses several optional elements: <OnlineResource>
 * states the data provider's URL; <Title> is a human-readable string naming the data provider;
 * <LogoURL> is the URL of a logo image. Client applications may choose to display one or more of
 * these items. A <Format> element in LogoURL indicates the MIME type of the logo image, and the
 * attributes width and height state the size of the image in pixels.
 *
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$
 */
public class Attribution {
    private LogoURL logoURL = null;

    private String title = null;

    private URL onlineResource = null;

    /**
     * constructor initializing the class with the attributes
     *
     * @param title
     * @param onlineResource
     * @param logoURL
     */
    public Attribution( String title, URL onlineResource, LogoURL logoURL ) {
        setTitle( title );
        setOnlineResource( onlineResource );
        setLogoURL( logoURL );
    }

    /**
     * @return a human-readable string naming the data providerreturns the title of the attribution.
     */
    public String getTitle() {
        return title;
    }

    /**
     * sets the title
     *
     * @param title
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     * @return the data provider's URL
     */
    public URL getOnlineResource() {
        return onlineResource;
    }

    /**
     * sets the data provider's URL
     *
     * @param onlineResource
     */
    public void setOnlineResource( URL onlineResource ) {
        this.onlineResource = onlineResource;
    }

    /**
     * @return the URL of a logo image
     */
    public LogoURL getLogoURL() {
        return logoURL;
    }

    /**
     * sets the URL of a logo image
     *
     * @param logoURL
     */
    public void setLogoURL( LogoURL logoURL ) {
        this.logoURL = logoURL;
    }

    @Override
    public String toString() {
        String ret = null;
        ret = "title = " + title + "\n";
        ret += ( "onlineResource = " + onlineResource + "\n" );
        ret += ( "logoURL = " + logoURL + "\n" );
        return ret;
    }

}
