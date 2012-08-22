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
package org.deegree.framework.util;

import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.deegree.framework.util.DateUtil.parseISO8601Date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.deegree.framework.log.ILogger;

/**
 * The <code>TimeTools2</code> class can be used to format Strings to timecodes and get Calenadars of a given Timecode.
 * This variant can be used to properly handle time zones.
 *
 * <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class TimeTools2 {

    private static final ILogger LOG = getLogger( TimeTools2.class );

    /**
     * @return the current timestamp in ISO format
     */
    public static String getISOFormattedTime() {
        return getISOFormattedTime( new Date( System.currentTimeMillis() ) );
    }

    /**
     * returns the date calendar in ISO format
     *
     * @param date
     * @return the date calendar in ISO format
     */
    public static String getISOFormattedTime( Date date ) {
        // SimpleDateFormat is not threadsafe, so create a new for each call
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'" );
        sdf.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
        return sdf.format( date );
    }

    /**
     *
     * @param cal
     *            a Calendar to get the timevalues of
     * @return the passed calendar in ISO format
     */
    public static String getISOFormattedTime( Calendar cal ) {
        return getISOFormattedTime( cal.getTime() );
    }

    /**
     *
     * @param isoDate
     *            an ISO timestamp-> year-mon-dayThours:min:sec
     * @return an instance of a <code>GregorianCalendar</tt> from an ISO timestamp
     * @throws NumberFormatException
     *             if the parsted values of the given String are no proper numbers.
     */
    public static Calendar createCalendar( String isoDate )
                            throws NumberFormatException {
        try {
            return parseISO8601Date( isoDate );
        } catch ( ParseException e ) {
            LOG.logError( "A date value could not be parsed, probably not in ISO8601 format", e );
        }
        return null;
    }

}
