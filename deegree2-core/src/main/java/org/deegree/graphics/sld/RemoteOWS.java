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
package org.deegree.graphics.sld;

import static org.deegree.framework.xml.XMLTools.escape;

import java.net.URL;

import org.deegree.framework.xml.Marshallable;

/**
 * Since a layer is defined as a collection of potentially mixed-type features, the UserLayer
 * element must provide the means to identify the features to be used. All features to be rendered
 * are assumed to be fetched from a Web Feature Server (WFS) or a Web Coverage CapabilitiesService
 * (WCS, in which case the term features is used loosely).
 * <p>
 * </p>
 * The remote server to be used is identified by RemoteOWS (OGC Web CapabilitiesService) element.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * @version $Revision$ $Date$
 */
public class RemoteOWS implements Marshallable {

    /**
     * WFS
     */
    final static public String WFS = "WFS";

    /**
     * WCS
     */
    final static public String WCS = "WCS";

    private String service = null;

    private URL onlineResource = null;

    /**
     * Creates a new RemoteOWS object.
     *
     * @param service
     * @param onlineResource
     */
    RemoteOWS( String service, URL onlineResource ) {
        setService( service );
        setOnlineResource( onlineResource );
    }

    /**
     * type of service that is represented by the remote ows. at the moment <tt>WFS</tt> and
     * <tt>WCS</tt> are possible values.
     *
     * @return the type of the services
     *
     */
    public String getService() {
        return service;
    }

    /**
     * @see RemoteOWS#getService()
     * @param service
     *            the type of the services
     *
     */
    public void setService( String service ) {
        this.service = service;
    }

    /**
     * address of the the ows as URL
     *
     * @return the adress of the ows as URL
     *
     */
    public URL getOnlineResource() {
        return onlineResource;
    }

    /**
     * @see RemoteOWS#getOnlineResource()
     * @param onlineResource
     *            the adress of the ows as URL
     *
     */
    public void setOnlineResource( URL onlineResource ) {
        this.onlineResource = onlineResource;
    }

    /**
     * exports the content of the RemoteOWS as XML formated String
     *
     * @return xml representation of the RemoteOWS
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 200 );
        sb.append( "<RemoteOWS>" );
        sb.append( "<Service>" ).append( escape( service ) ).append( "</Service>" );
        sb.append( "<OnlineResource xmlns:xlink='http://www.w3.org/1999/xlink' " );
        sb.append( "xlink:type='simple' xlink:href='" );
        sb.append( escape( onlineResource.toExternalForm() ) + "'/>" );
        sb.append( "</RemoteOWS>" );

        return sb.toString();
    }
}
