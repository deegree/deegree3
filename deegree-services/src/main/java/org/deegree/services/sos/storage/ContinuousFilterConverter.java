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
package org.deegree.services.sos.storage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.deegree.commons.utils.ArrayUtils;
import org.deegree.protocol.sos.filter.BeginFilter;
import org.deegree.protocol.sos.filter.DurationFilter;
import org.deegree.protocol.sos.filter.EndFilter;
import org.deegree.protocol.sos.filter.TimeFilter;
import org.deegree.protocol.sos.filter.TimeInstantFilter;

/**
 * This is a filter converter for datastores that does not store the time of each observation. The observations are
 * stored in a fixed interval, the start time of the offering is known. The observations need a uniqe id that
 * incerements with each new observation.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class ContinuousFilterConverter extends GenericFilterConverter {

    private final Date begin;

    private final int firstID;

    private final long interval;

    private final String idField;

    /**
     * @param dsConf
     * @param idField
     *            the column name of the id field
     * @param begin
     *            the date of the first observation
     * @param firstID
     *            the time span between two observations in ms
     * @param interval
     *            the id of the first observation (with <code>begin</begin> date)
     */
    public ContinuousFilterConverter( DatastoreConfiguration dsConf, String idField, Date begin, long interval,
                                      int firstID ) {
        super( dsConf, null );
        this.idField = idField;
        this.begin = begin;
        this.firstID = firstID;
        this.interval = interval;
    }

    @Override
    public void buildTimeClause( QueryBuilder q, List<TimeFilter> filters ) {
        ArrayList<String> result = new ArrayList<String>( filters.size() );
        for ( TimeFilter filter : filters ) {
            if ( filter instanceof DurationFilter ) {
                DurationFilter durationFilter = (DurationFilter) filter;
                String begin = rowIDForDate( durationFilter.getBegin() );
                String end = rowIDForDate( durationFilter.getEnd() );
                result.add( "(" + beginIDFilter( begin, durationFilter.isInclusiveBegin() ) + " AND "
                            + endIDFilter( end, durationFilter.isInclusiveEnd() ) + ")" );
            } else if ( filter instanceof BeginFilter ) {
                BeginFilter beginFilter = (BeginFilter) filter;
                result.add( beginIDFilter( rowIDForDate( beginFilter.getBegin() ), beginFilter.isInclusiveBegin() ) );
            } else if ( filter instanceof EndFilter ) {
                EndFilter endFilter = (EndFilter) filter;
                result.add( endIDFilter( rowIDForDate( endFilter.getEnd() ), endFilter.isInclusiveEnd() ) );
            } else if ( filter instanceof TimeInstantFilter ) {
                TimeInstantFilter timeInstantFilter = (TimeInstantFilter) filter;
                result.add( idField + " = " + rowIDForDate( timeInstantFilter.getInstant() ) );
            }
        }

        q.add( ArrayUtils.join( " OR ", result ) );
    }

    private String beginIDFilter( String id, boolean inclusive ) {
        String beginCmp = inclusive ? " >= " : " > ";
        return idField + beginCmp + id;
    }

    private String endIDFilter( String id, boolean inclusive ) {
        String endCmp = inclusive ? " <= " : " < ";
        return idField + endCmp + id;
    }

    private String rowIDForDate( Date date ) {
        long delta = date.getTime() - begin.getTime();
        return Integer.toString( (int) Math.ceil( delta / interval ) + firstID );
    }

    /**
     * Calculates the date of the given row number.
     * 
     * @param id
     *            the row id
     * @return the date
     */
    public Date dateForRowID( int id ) {
        long delta = interval * ( id - firstID );
        // this.begin is already in GMT
        Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( "GMT" ) );
        cal.setTime( begin );
        cal.add( Calendar.SECOND, (int) ( delta / 1000.0 ) );
        return cal.getTime();
    }

}
