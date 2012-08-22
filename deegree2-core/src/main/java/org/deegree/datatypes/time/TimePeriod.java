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
import java.net.URISyntaxException;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */

public class TimePeriod implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private TimePosition beginPosition = null;

    private TimePosition endPosition = null;

    private TimeDuration timeResolution = null;

    private URI frame = null;

    /**
     * @param beginPosition
     * @param endPosition
     * @param timeResolution
     */
    public TimePeriod( TimePosition beginPosition, TimePosition endPosition, TimeDuration timeResolution ) {
        this.beginPosition = beginPosition;
        this.endPosition = endPosition;
        this.timeResolution = timeResolution;
    }

    /**
     * @param beginPosition
     * @param endPosition
     * @param timeResolution
     * @param frame
     */
    public TimePeriod( TimePosition beginPosition, TimePosition endPosition, TimeDuration timeResolution, URI frame ) {
        this.beginPosition = beginPosition;
        this.endPosition = endPosition;
        this.timeResolution = timeResolution;
        this.frame = frame;
    }

    /**
     * @return Returns the beginPosition.
     */
    public TimePosition getBeginPosition() {
        return beginPosition;
    }

    /**
     * @param beginPosition
     *            The beginPosition to set.
     */
    public void setBeginPosition( TimePosition beginPosition ) {
        this.beginPosition = beginPosition;
    }

    /**
     * @return Returns the endPosition.
     */
    public TimePosition getEndPosition() {
        return endPosition;
    }

    /**
     * @param endPosition
     *            The endPosition to set.
     */
    public void setEndPosition( TimePosition endPosition ) {
        this.endPosition = endPosition;
    }

    /**
     * @return Returns the frame.
     */
    public URI getFrame() {
        return frame;
    }

    /**
     * @param frame
     *            The frame to set.
     */
    public void setFrame( URI frame ) {
        this.frame = frame;
    }

    /**
     * @return Returns the timeResolution.
     */
    public TimeDuration getTimeResolution() {
        return timeResolution;
    }

    /**
     * @param timeResolution
     *            The timeResolution to set.
     */
    public void setTimeResolution( TimeDuration timeResolution ) {
        this.timeResolution = timeResolution;
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        URI fr = null;
        if ( frame != null ) {
            try {
                fr = new URI( frame.toASCIIString() );
            } catch ( URISyntaxException e ) {
                // will never happen.
            }
        }
        return new TimePeriod( (TimePosition) beginPosition.clone(), (TimePosition) endPosition.clone(),
                               (TimeDuration) timeResolution.clone(), fr );
    }

}
