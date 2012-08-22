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
package org.deegree.ogcwebservices.wps;

import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.OGCWebServiceException;

/**
 * ServerBusyException.java
 *
 * Created on 29.03.2006. 09:57:59h
 *
 * The server is too busy to accept and queue the request at this time. Locator: None, omit
 * �locator� parameter
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 *
 * @version 1.0.
 *
 * @since 2.0
 */
public class ServerBusyException extends OGCWebServiceException {

    /**
     *
     */
    private static final long serialVersionUID = -6524656143880607709L;

    /**
     * @param message
     */
    public ServerBusyException( String message ) {
        super( message );
    }

    /**
     *
     * @param locator
     * @param message
     */
    public ServerBusyException( String locator, String message ) {
        super( locator, message );
    }

    /**
     *
     * @param locator
     * @param message
     * @param code
     */
    public ServerBusyException( String locator, String message, ExceptionCode code ) {
        super( locator, message, code );
    }
}
