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
package org.deegree.datatypes.time;

import java.io.Serializable;
import java.net.URI;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */

public class TimePosition implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private TimeIndeterminateValue indeterminatePosition = null;

    private String calendarEraName = null;

    private URI frame = null;

    private Calendar time = null;

    /**
     * defaults are:
     * <ul>
     * <li>indeterminatePosition = now</li>
     * <li>calendarEraName = AC</li>
     * <li>frame = #ISO-8601</li>
     * <li>time = new GregorianCalendar()</li>
     * </ul>
     */
    public TimePosition() {
        indeterminatePosition = new TimeIndeterminateValue();
        calendarEraName = "AC";
        try {
            frame = new URI( "#ISO-8601" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        time = new GregorianCalendar();
    }

    /**
     * defaults are:
     * <ul>
     * <li>indeterminatePosition = now</li>
     * <li>calendarEraName = AC</li>
     * <li>frame = #ISO-8601</li>
     * </ul>
     *
     * @param time
     */
    public TimePosition( Calendar time ) {
        this.time = time;
        indeterminatePosition = new TimeIndeterminateValue();
        calendarEraName = "AC";
        try {
            frame = new URI( "#ISO-8601" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * @param indeterminatePosition
     * @param calendarEraName
     * @param frame
     * @param time
     */
    public TimePosition( TimeIndeterminateValue indeterminatePosition, String calendarEraName, URI frame, Calendar time ) {
        this.indeterminatePosition = indeterminatePosition;
        this.calendarEraName = calendarEraName;
        this.frame = frame;
        this.time = time;
    }

    /**
     * @return Returns the calendarEraName.
     *
     */
    public String getCalendarEraName() {
        return calendarEraName;
    }

    /**
     * @param calendarEraName
     *            The calendarEraName to set.
     *
     */
    public void setCalendarEraName( String calendarEraName ) {
        this.calendarEraName = calendarEraName;
    }

    /**
     * @return Returns the frame.
     *
     */
    public URI getFrame() {
        return frame;
    }

    /**
     * @param frame
     *            The frame to set.
     *
     */
    public void setFrame( URI frame ) {
        this.frame = frame;
    }

    /**
     * @return Returns the indeterminatePosition.
     *
     */
    public TimeIndeterminateValue getIndeterminatePosition() {
        return indeterminatePosition;
    }

    /**
     * @param indeterminatePosition
     *            The indeterminatePosition to set.
     *
     */
    public void setIndeterminatePosition( TimeIndeterminateValue indeterminatePosition ) {
        this.indeterminatePosition = indeterminatePosition;
    }

    /**
     * @return Returns the time.
     *
     */
    public Calendar getTime() {
        return time;
    }

    /**
     * @param time
     *            The time to set.
     *
     */
    public void setTime( Calendar time ) {
        this.time = time;
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        return new TimePosition( indeterminatePosition, calendarEraName, frame, time );
    }

}
