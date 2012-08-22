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
 * ProcessStartedType.java
 *
 * Created on 09.03.2006. 23:21:37h
 *
 * Indicates that this process has been has been accepted by the server, and processing has begun.
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @author last edited by: $Author:wanhoff$
 *
 * @version $Revision$, $Date:20.03.2007$
 */
public class ProcessStarted {

    /**
     * A human-readable text string whose contents are left open to definition by each WPS server,
     * but is expected to include any messages the server may wish to let the clients know. Such
     * information could include how much longer the process may take to execute, or any warning
     * conditions that may have been encountered to date. The client may display this text to a
     * human user.
     */
    private String value;

    /**
     * Percentage of the process that has been completed, where 0 means the process has just
     * started, and 100 means the process is complete. This attribute should be included if the
     * process is expected to execute for a long time (i.e. more than a few minutes). This
     * percentage is expected to be accurate to within ten percent
     */
    private int percentCompleted;

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            The value to set.
     */
    public void setValue( String value ) {
        this.value = value;
    }

    /**
     * @return Returns the percentCompleted.
     */
    public int getPercentCompleted() {
        return percentCompleted;
    }

    /**
     * @param value
     *            The percentCompleted to set.
     */
    public void setPercentCompleted( int value ) {
        this.percentCompleted = value;
    }

}
