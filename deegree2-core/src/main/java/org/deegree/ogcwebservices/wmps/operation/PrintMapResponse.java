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

package org.deegree.ogcwebservices.wmps.operation;

import java.util.Date;

/**
 * PrintMapInitialResponse to inform the user if his request the status of his requst before
 * processing. If the request is (not) successfully recieved an appropriate message will be sent to
 * the user.
 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class PrintMapResponse {

    private String id;

    private Date timeStamp;

    private Date expectedTime;

    private String emailAddress;

    private String exception;

    private String message;

    /**
     * Create an instance of the PrintMapResponse
     *
     * @param id
     * @param emailAddress
     * @param timeStamp
     * @param expectedTime
     * @param message
     * @param exception
     */
    public PrintMapResponse( String id, String emailAddress, Date timeStamp, Date expectedTime,
                             String message, String exception ) {
        this.id = id;
        this.emailAddress = emailAddress;
        this.timeStamp = timeStamp;
        this.expectedTime = expectedTime;
        this.exception = exception;
        this.message = message;

    }

    /**
     * Get PrintMap Request Id
     *
     * @return String
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get PrintMap request Email Address
     *
     * @return String
     */
    public String getEmailAddress() {
        return this.emailAddress;

    }

    /**
     * Get PrintMap request TimeStamp
     *
     * @return Date
     */
    public Date getTimeStamp() {
        return this.timeStamp;
    }

    /**
     * Get Success/Failed Message for this PrintMap request.
     *
     * @return String
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get ExpectedTime for the service to process the PrintMap request.
     *
     * @return Date
     */
    public Date getExpectedTime() {
        return this.expectedTime;
    }

    /**
     * @return Returns the exception.
     */
    public String getException() {
        return this.exception;
    }


}
