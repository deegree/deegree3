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
package org.deegree.ogcwebservices.wps.execute;

/**
 * StatusType.java
 *
 * Created on 09.03.2006. 23:19:43h
 *
 * Description of the status of process execution.
 *
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @author last edited by: $Author:wanhoff$
 *
 * @version $Revision$, $Date:20.03.2007$
 */
public class Status {

    /**
     * Indicates that this process has been accepted by the server, but is in a queue and has not
     * yet started to execute. The contents of this human-readable text string is left open to
     * definition by each server implementation, but is expected to include any messages the server
     * may wish to let the clients know. Such information could include how long the queue is, or
     * any warning conditions that may have been encountered. The client may display this text to a
     * human user.
     */

    private String processAccepted;

    /**
     * Indicates that this process has been has been accepted by the server, and processing has
     * begun.
     */
    private ProcessStarted processStarted;

    /**
     * Indicates that this process has successfully completed execution. The contents of this
     * human-readable text string is left open to definition by each server, but is expected to
     * include any messages the server may wish to let the clients know, such as how long the
     * process took to execute, or any warning conditions that may have been encountered. The client
     * may display this text string to a human user. The client should make use of the presence of
     * this element to trigger automated or manual access to the results of the process. If manual
     * access is intended, the client should use the presence of this element to present the results
     * as downloadable links to the user.
     */

    private String processSucceeded;

    /**
     * Indicates that execution of this process has failed, and includes error information.
     */
    private ProcessFailed processFailed;

    /**
     * @return Returns the processAccepted.
     */
    public String getProcessAccepted() {
        return processAccepted;
    }

    /**
     * @param value
     *            The processAccepted to set.
     */
    public void setProcessAccepted( String value ) {
        this.processAccepted = value;
    }

    /**
     * @return Returns the processStarted.
     */
    public ProcessStarted getProcessStarted() {
        return processStarted;
    }

    /**
     * @param value
     *            The processStarted to set.
     */
    public void setProcessStarted( ProcessStarted value ) {
        this.processStarted = value;
    }

    /**
     * @return Returns the processSucceeded.
     */
    public String getProcessSucceeded() {
        return processSucceeded;
    }

    /**
     * @param value
     *            The processSucceeded to set.
     */
    public void setProcessSucceeded( String value ) {
        this.processSucceeded = value;
    }

    /**
     * @return Returns the processFailed.
     */
    public ProcessFailed getProcessFailed() {
        return processFailed;
    }

    /**
     * @param value
     *            The processFailed to set.
     */
    public void setProcessFailed( ProcessFailed value ) {
        this.processFailed = value;
    }

}
