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
package org.deegree.portal.owswatch;

/**
 * Constants class for owsWatch
 *
 * @author <a href="mailto:ncho@lat-lon.de">ncho</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public final class Constants {

    private Constants() {
        // never instantiate
    }

    /**
     * The main element name
     */
    public final static String SERVICE_MONITOR = "SERVICEMONITOR";

    /**
     *
     */
    public final static String SERVICE_NAME = "SERVICENAME";

    /**
     * WMS,WFS, etc..
     */
    public final static String SERVICE_TYPE = "SERVICE";

    /**
     * GetCapabailities, GetMap, etc...
     */
    public final static String REQUEST_TYPE = "REQUEST";

    /**
     * Refresh rate minutes
     */
    public final static String INTERVAL = "INTERVAL";

    /**
     * Timeout of the request in seconds
     */
    public final static String TIMEOUT_KEY = "TIMEOUT";

    /**
     * Server to send the requests to
     */
    public final static String ONLINE_RESOURCE = "ONLINERESOURCE";

    /**
     * Service version
     */
    public final static String VERSION = "VERSION";

    /**
     * Indicates whether this service should be refreshed each refreshrate or not
     */
    public final static String ACTIVE = "ACTIVE";

    /**
     * The path to write the protocol
     */
    public final static String PROTOCOL_PATH = "ProtocolLocation";

    /**
     * XMLRequest Element for POST requests
     */
    public final static String XML_REQUEST = "XMLREQUEST";

    /**
     * HttpMethod type GET or POST
     */
    public final static String HTTP_METHOD = "HTTPMETHOD";

    /**
     * SessionID key
     */
    public final static String SESSIONID_KEY = "SessionID";
}
