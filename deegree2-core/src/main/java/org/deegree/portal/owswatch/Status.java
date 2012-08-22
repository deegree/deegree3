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
 * Enum to hold the different states of a response to the tests made on different services
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public enum Status {

    /**
     * If the Service is available
     */
    RESULT_STATE_AVAILABLE,

    /**
     * If the Service has not yet been tested
     */
    RESULT_STATE_WAITING,

    /**
     * ************************************ The next list is for critical errors
     */

    /**
     * If the page is unavailable at all
     */
    RESULT_STATE_PAGE_UNAVAILABLE,

    /**
     * Timeout has occured while executing the request
     */
    RESULT_STATE_TIMEOUT,

    /**
     * The requested service is not implemented
     */
    RESULT_STATE_NOT_IMPLEMENTED,

    /**
     * ***************************************** The next ist is for non critical errors
     */
    /**
     * If the response stream is null or can not be read
     */
    RESULT_STATE_BAD_RESPONSE,

    /**
     * In case the page is available but the request returned no response
     */
    RESULT_STATE_SERVICE_UNAVAILABLE,

    /**
     * The returned xml document can not be parsed
     */
    RESULT_STATE_INVALID_XML,

    /**
     * A general use error
     */
    RESULT_STATE_ERROR_UNKNOWN,

    /**
     *
     */
    RESULT_STATE_UNEXPECTED_CONTENT;

    /**
     * @return true if the service is available, false otherwise
     */
    public boolean isAvailable() {

        if ( this == RESULT_STATE_AVAILABLE ) {
            return true;
        }
        return false;
    }

    /**
     * @return true if the state is waiting, false otherwise
     */
    public boolean isWaiting() {

        if ( this == RESULT_STATE_WAITING ) {
            return true;
        }
        return false;
    }

    /**
     * @return true if the error is critical, false otherwise
     */
    public boolean isCriticalError() {

        if ( this == RESULT_STATE_PAGE_UNAVAILABLE || this == RESULT_STATE_TIMEOUT
             || this == RESULT_STATE_NOT_IMPLEMENTED ) {
            return true;
        }
        return false;
    }

    /**
     * @return true if the error is nonCritical, false otherwise
     */
    public boolean isNonCriticalError() {

        if ( this == RESULT_STATE_SERVICE_UNAVAILABLE || this == RESULT_STATE_INVALID_XML
             || this == RESULT_STATE_ERROR_UNKNOWN || this == RESULT_STATE_UNEXPECTED_CONTENT
             || this == RESULT_STATE_BAD_RESPONSE ) {
            return true;
        }
        return false;
    }

    /**
     * @return String representation of the error code
     */
    public String getStatusMessage() {

        if ( isAvailable() ) {
            return "Service is available";
        } else if ( isWaiting() ) {
            return "Service has not yet been tested";
        } else if ( isCriticalError() ) {
            return translateCriticalErrorMessage();
        } else if ( isNonCriticalError() ) {
            return translateNoncriticalErrorMessage();
        }
        return "Unknown state";
    }

    private String translateNoncriticalErrorMessage() {

        if ( this == Status.RESULT_STATE_PAGE_UNAVAILABLE ) {
            return "Page is unavailable";
        } else if ( this == Status.RESULT_STATE_TIMEOUT ) {
            return "Server Timeout";
        } else if ( this == Status.RESULT_STATE_NOT_IMPLEMENTED ) {
            return "This functionality is not implemented";
        } else if ( this == Status.RESULT_STATE_BAD_RESPONSE ) {
            return "The response is null or empty";
        }
        return "Unknown non critical error";

    }

    private String translateCriticalErrorMessage() {

        if ( this == Status.RESULT_STATE_SERVICE_UNAVAILABLE ) {
            return "The service is currently unavailable";
        } else if ( this == Status.RESULT_STATE_INVALID_XML ) {
            return "The response xml could not be parsed";
        } else if ( this == Status.RESULT_STATE_ERROR_UNKNOWN ) {
            return "Unknown uncritical error";
        } else if ( this == Status.RESULT_STATE_UNEXPECTED_CONTENT ) {
            return "Content type is not what was expected";
        }
        return "Unknown critical error";

    }
}
