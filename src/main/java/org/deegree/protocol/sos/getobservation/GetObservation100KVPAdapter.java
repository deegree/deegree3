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
package org.deegree.protocol.sos.getobservation;

import static org.deegree.commons.utils.kvp.KVPUtils.getRequired;
import static org.deegree.commons.utils.kvp.KVPUtils.splitAll;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deegree.commons.utils.time.DateUtils;
import org.deegree.protocol.sos.filter.DurationFilter;
import org.deegree.protocol.sos.filter.ProcedureFilter;
import org.deegree.protocol.sos.filter.PropertyFilter;
import org.deegree.protocol.sos.filter.TimeFilter;
import org.deegree.protocol.sos.filter.TimeInstantFilter;

/**
 * This is an kvp adapter for SOS 1.0.0 GetObservation requests.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GetObservation100KVPAdapter {

    /**
     * @param kvp
     * @return the parsed GetObservation request
     * @throws ParseException
     *             if the event time couldn't be parsed.
     */
    public static GetObservation parse( Map<String, String> kvp )
                            throws ParseException {
        return new GetObservation( getOffering( kvp ), getProcedures( kvp ), getObservedProperties( kvp ),
                                   getEventTime( kvp ), null, null, getResultModel( kvp ), getResponseFormat( kvp ),
                                   getResponseMode( kvp ), getSRSName( kvp ) );
    }

    private static List<TimeFilter> getEventTime( Map<String, String> kvp )
                            throws ParseException {
        List<TimeFilter> result = new LinkedList<TimeFilter>();
        String time = kvp.get( "TIME" );
        if ( time == null ) { // try eventtime
            time = kvp.get( "EVENTTIME" );
        }
        if ( time != null ) {
            if ( time.contains( "/" ) ) {
                String[] timeParts = time.split( "/" );

                Date begin = DateUtils.parseISO8601Date( timeParts[0] );
                Date end = DateUtils.parseISO8601Date( timeParts[1] );
                result.add( new DurationFilter( begin, true, end, false ) );
            } else {
                Date instant = DateUtils.parseISO8601Date( time );
                result.add( new TimeInstantFilter( instant ) );
            }
        }
        return result;
    }

    private static List<PropertyFilter> getObservedProperties( Map<String, String> kvp ) {
        getRequired( kvp, "OBSERVEDPROPERTY" ); // check required key
        List<PropertyFilter> result = new LinkedList<PropertyFilter>();
        for ( String prop : splitAll( kvp, "OBSERVEDPROPERTY" ) ) {
            result.add( new PropertyFilter( prop ) );
        }
        return result;
    }

    private static String getOffering( Map<String, String> kvp ) {
        return getRequired( kvp, "OFFERING" );
    }

    private static List<ProcedureFilter> getProcedures( Map<String, String> kvp ) {
        List<ProcedureFilter> result = new LinkedList<ProcedureFilter>();
        for ( String proc : splitAll( kvp, "PROCEDURE" ) ) {
            result.add( new ProcedureFilter( proc ) );
        }
        return result;
    }

    private static String getResponseFormat( Map<String, String> kvp ) {
        return kvp.get( "RESPONSEFORMAT" );
    }

    private static String getResponseMode( Map<String, String> kvp ) {
        return kvp.get( "RESPONSEMODE" );
    }

    private static String getResultModel( Map<String, String> kvp ) {
        String model = kvp.get( "RESULTMODEL" );
        if ( model != null && model.startsWith( "om:" ) ) {
            return model.substring( 3 );
        }
        return model;

    }

    private static String getSRSName( Map<String, String> kvp ) {
        // TODO make required
        return kvp.get( "SRSNAME" );
    }

}
