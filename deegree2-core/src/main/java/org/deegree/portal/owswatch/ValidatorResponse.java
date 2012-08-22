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

import java.io.Serializable;
import java.util.Date;

/**
 * Data class that holds the response of execute test that tests a certain service.
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ValidatorResponse implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7602599272924961670L;

    private String message = null;

    private Date lastTest = null;

    private long lastLapse = -1;

    private Status status = null;

    /**
     * Constructor
     *
     * @param message
     * @param status
     */
    public ValidatorResponse( String message, Status status ) {
        this.message = message;
        this.status = status;
    }

    /**
     * @return lastTest Lapse
     */
    public long getLastLapse() {
        return lastLapse;
    }

    /**
     * @param lastLapse
     */
    public void setLastLapse( long lastLapse ) {
        this.lastLapse = lastLapse;
    }

    /**
     * @return last test Date
     */
    public Date getLastTest() {
        return lastTest;
    }

    /**
     * @param lastTest
     */
    public void setLastTest( Date lastTest ) {
        this.lastTest = lastTest;
    }

    /**
     * @return last message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     */
    public void setMessage( String message ) {
        this.message = message;
    }

    /**
     * @return status of the last test
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param status
     */
    public void setStatus( Status status ) {
        this.status = status;
    }

}
