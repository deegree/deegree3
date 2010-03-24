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
package org.deegree.protocol.sos.time;

import java.text.ParseException;
import java.util.Date;

import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.utils.time.Duration;

/**
 * This class represets a time period between to dates. The period can be extended.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class TimePeriod implements SamplingTime {

    private Date begin;

    private Date end;

    /**
     * Create an empty TimePeriod. getEnd and getBegin will return <code>null</code> before it is extended the frst
     * time.
     */
    public TimePeriod() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param begin
     * @param end
     */
    public TimePeriod( Date begin, Date end ) {
        this.begin = begin;
        this.end = end;
    }

    /**
     * Create a new TimePeriod. One of the values may be null or empty.
     *
     * <p>
     * If all values are given, the end must be begin+duration.
     *
     * @param isoBegin
     *            iso8601 string
     * @param isoEnd
     *            iso8601 string
     * @param isoDuration
     *            iso8601 duration string
     * @return a new time period
     * @throws IllegalArgumentException
     *             if more than one value is null or empty, or if begin, end and duration doesn't match
     */
    public static TimePeriod createTimePeriod( String isoBegin, String isoEnd, String isoDuration ) {
        Date begin = parseDateOrNull( isoBegin );
        Date end = parseDateOrNull( isoEnd );
        Duration duration = parseDurationOrNull( isoDuration );

        if ( begin != null && end == null && duration != null ) {
            // end is indetermined
            end = duration.getDateAfter( begin );
        } else if ( begin == null && end != null && duration != null ) {
            // begin is indetermined
            begin = duration.getDateBefore( end );
        } else if ( begin != null && end != null && duration != null ) {
            // all given
            Date testEnd = duration.getDateAfter( begin );
            if ( !end.equals( testEnd ) ) {
                throw new IllegalArgumentException( "Duration dosn't match to begin and end time." );
            }
        } else if ( begin != null && end != null && duration == null ) {
            // everything is fine
        } else {
            throw new IllegalArgumentException( "Illegal values/combination for/of begin, end and duration ("
                                                + isoBegin + ", " + isoEnd + ", " + isoDuration + ")" );
        }

        return new TimePeriod( begin, end );
    }

    /**
     * parse a iso date, return null on failure
     */
    private static Date parseDateOrNull( String date ) {
        if ( date != null && !date.equals( "" ) ) {
            try {
                return DateUtils.parseISO8601Date( date );
            } catch ( ParseException e ) {
                return null;
            }
        }
        return null;
    }

    /**
     * parse a iso duration, return null on failure
     */
    private static Duration parseDurationOrNull( String duration ) {
        if ( duration != null && !duration.equals( "" ) ) {
            try {
                return DateUtils.parseISO8601Duration( duration );
            } catch ( ParseException e ) {
                return null;
            }
        }
        return null;
    }


    /**
     * Extend the TimePeriod with the given date.
     *
     * @param samplingTime
     */
    public void extend( Date samplingTime ) {
        if ( begin == null || samplingTime.before( begin ) ) {
            begin = samplingTime;
        }
        if ( end == null || samplingTime.after( end ) ) {
            end = samplingTime;
        }
    }

    /**
     * Extend the TimePeriod with the given date.
     *
     * @param samplingTime
     */
    public void extend( SamplingTime samplingTime ) {
        if ( samplingTime instanceof TimeInstant ) {
            Date date = ( (TimeInstant) samplingTime ).getTime();
            extend( date );
        } else if ( samplingTime instanceof TimePeriod ) {
            Date date;
            date = ( (TimePeriod) samplingTime ).getBegin();
            extend( date );
            date = ( (TimePeriod) samplingTime ).getEnd();
            extend( date );
        }
    }

    /**
     * @return the begin date
     */
    public Date getBegin() {
        return begin;
    }

    /**
     * @return the end date
     */
    public Date getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "TimePeriod: from " + DateUtils.formatISO8601Date( begin ) + " to " + DateUtils.formatISO8601Date( end );
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash += 31 * hash * ( begin == null ? 0 : begin.getTime() );
        hash += 31 * hash * ( end == null ? 0 : end.getTime() );
        return hash;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( obj != null && obj instanceof TimePeriod ) {
            TimePeriod that = (TimePeriod)obj;
            if ( this.begin == null || that.begin == null ) { // if begin is null, end is null too
                return this.begin == that.begin;
            }
            return this.begin.getTime() == that.begin.getTime() && this.end.getTime() == that.end.getTime();
        }
        return super.equals( obj );
    }

}
