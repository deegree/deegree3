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

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.sos.filter.BeginFilter;
import org.deegree.protocol.sos.filter.DurationFilter;
import org.deegree.protocol.sos.filter.EndFilter;
import org.deegree.protocol.sos.filter.TimeFilter;
import org.deegree.protocol.sos.filter.TimeInstantFilter;
import org.deegree.protocol.sos.time.TimePeriod;

/**
 * This is an xml adapter for SOS 1.0.0 EventTimes.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class EventTime100XMLAdapter extends XMLAdapter {

    private static final NamespaceContext nsContext;

    private static final String GML_PREFIX = "gml";

    private static final String GML_NS = "http://www.opengis.net/gml";

    private static final String OGC_NS = "http://www.opengis.net/ogc";

    private static final String SOS_PREFIX = "sos";

    private static final String SOS_NS = "http://www.opengis.net/sos/1.0";

    static {
        nsContext = new NamespaceContext( XMLAdapter.nsContext );
        nsContext.addNamespace( GML_PREFIX, GML_NS );
        nsContext.addNamespace( SOS_PREFIX, SOS_NS );
    }

    /**
     * Create a new EventTime adapter for SOS spec 1.0.0.
     * 
     * @param rootElement
     *            the EventTime element
     * @param systemId
     *            to resolve all relative files from.
     */
    public EventTime100XMLAdapter( OMElement rootElement, String systemId ) {
        super( rootElement, systemId );
    }

    /**
     * Parse all temporalOps from the EventTime element.
     * 
     * @return parsed time filter
     */
    public List<TimeFilter> parseTimeFilter() {
        List<TimeFilter> result = new LinkedList<TimeFilter>();
        List<OMElement> eventTimeElems = getElements( rootElement, new XPath( "sos:eventTime/child::*", nsContext ) );
        for ( OMElement eventTimeElem : eventTimeElems ) {
            if ( eventTimeElem.getQName().getNamespaceURI().equals( OGC_NS ) ) {
                TimeFilter eventFilter = parseTimeEvent( eventTimeElem.getQName().getLocalPart(), eventTimeElem );
                result.add( eventFilter );
            }
        }
        return result;
    }

    private TimeFilter parseTimeEvent( String temporalOp, OMElement eventTimeElem ) {
        if ( temporalOp.equals( "TM_During" ) ) {
            OMElement timePeriod = getElement( eventTimeElem, new XPath( "gml:TimePeriod", nsContext ) );
            return parseTimePeriod( timePeriod );
        }
        if ( temporalOp.equals( "TM_After" ) ) {
            OMElement timeInstant = getElement( eventTimeElem, new XPath( "gml:TimeInstant", nsContext ) );
            return new BeginFilter( parseTimeInstant( timeInstant ), false );
        }
        if ( temporalOp.equals( "TM_Before" ) ) {
            OMElement timeInstant = getElement( eventTimeElem, new XPath( "gml:TimeInstant", nsContext ) );
            return new EndFilter( parseTimeInstant( timeInstant ), false );
        }
        if ( temporalOp.equals( "TM_Begins" ) ) {
            OMElement timeInstant = getElement( eventTimeElem, new XPath( "gml:TimeInstant", nsContext ) );
            return new BeginFilter( parseTimeInstant( timeInstant ), true );
        }
        if ( temporalOp.equals( "TM_Ends" ) ) {
            OMElement timeInstant = getElement( eventTimeElem, new XPath( "gml:TimeInstant", nsContext ) );
            return new EndFilter( parseTimeInstant( timeInstant ), true );
        }
        if ( temporalOp.equals( "TM_Equals" ) ) {
            OMElement timeInstant = getElement( eventTimeElem, new XPath( "gml:TimeInstant", nsContext ) );
            return new TimeInstantFilter( parseTimeInstant( timeInstant ) );
        }
        throw new UnsupportedOperationException( temporalOp + " is not implemented." );
    }

    private TimeFilter parseTimePeriod( OMElement timePeriodElem ) {
        String begin = parseBeginTime( timePeriodElem );
        String end = parseEndTime( timePeriodElem );
        String duration = parseTimeLength( timePeriodElem );
        TimePeriod period = TimePeriod.createTimePeriod( begin, end, duration );
        return new DurationFilter( period.getBegin(), period.getEnd() );
    }

    private String parseTimeLength( OMElement timePeriodElem ) {
        OMElement duration = getElement( timePeriodElem, new XPath( "gml:duration", nsContext ) );
        if ( duration != null ) {
            return duration.getText();
        }
        duration = getElement( timePeriodElem, new XPath( "gml:timeInterval", nsContext ) );
        if ( duration != null ) {
            throw new UnsupportedOperationException( "gml:timeInterval is not supported" );
        }
        // not required
        return "";
    }

    // The begin time can be gml:begin or gml:beginPosition.
    // A gml:beginPosition can represent an indeterminatePosition like 'now', but it can also contain a ISO8601 date.
    // so <gml:beginPosition indeterminatePosition="before">2007-10-05</...> is just another redundant possibility to
    // express
    // <gml:beginPosition indeterminatePosition="unknown" />, etc.
    private String parseBeginTime( OMElement timePeriodElem ) {
        OMElement begin = getElement( timePeriodElem, new XPath( "gml:begin", nsContext ) );
        if ( begin != null ) {
            return begin.getText();
        }
        begin = getElement( timePeriodElem, new XPath( "gml:beginPosition", nsContext ) );
        if ( begin != null ) {
            String indeterminate = begin.getAttributeValue( new QName( "indeterminatePosition" ) );
            if ( indeterminate == null ) {
                return begin.getText();
            } else if ( indeterminate.equals( "unknown" ) ) {
                return "";
            } else if ( indeterminate.equals( "now" ) ) {
                return DateUtils.formatISO8601Date( new Date() );
            } else if ( indeterminate.equals( "after" ) ) {
                return begin.getText();
            } else if ( indeterminate.equals( "before" ) ) {
                return "";
            }
        }
        throw new XMLParsingException( this, timePeriodElem, "unable to parse begin time" );
    }

    // see comments for parseBeginTime
    private String parseEndTime( OMElement timePeriodElem ) {
        OMElement end = getElement( timePeriodElem, new XPath( "gml:end", nsContext ) );
        if ( end != null ) {
            return end.getText();
        }
        end = getElement( timePeriodElem, new XPath( "gml:endPosition", nsContext ) );
        if ( end != null ) {
            String indeterminate = end.getAttributeValue( new QName( "indeterminatePosition" ) );
            if ( indeterminate == null ) {
                return end.getText();
            } else if ( indeterminate.equals( "unknown" ) ) {
                return "";
            } else if ( indeterminate.equals( "now" ) ) {
                return DateUtils.formatISO8601Date( new Date() );
            } else if ( indeterminate.equals( "after" ) ) {
                return "";
            } else if ( indeterminate.equals( "before" ) ) {
                return end.getText();
            }
        }
        throw new XMLParsingException( this, timePeriodElem, "unable to parse end time" );
    }

    private Date parseTimeInstant( OMElement timeInstant ) {
        OMElement tpos = getElement( timeInstant, new XPath( "gml:timePosition", nsContext ) );
        if ( tpos.getText().trim().length() > 0 ) {
            try {
                return DateUtils.parseISO8601Date( tpos.getText() );
            } catch ( ParseException e ) {
                throw new EventTimeXMLParsingException( this, tpos );
            }
        }
        String indeterminate = tpos.getAttributeValue( new QName( "indeterminatePosition" ) );
        if ( indeterminate != null && indeterminate.equals( "now" ) ) {
            return new Date();
        }
        throw new EventTimeXMLParsingException( this, tpos );

    }

    /**
     * <code>EventTimeXMLParsingException</code> is a hack to get correct OWSException codes.
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static class EventTimeXMLParsingException extends XMLParsingException {
        private static final long serialVersionUID = -1778773749005062747L;

        /**
         * @param adapter
         * @param tpos
         */
        public EventTimeXMLParsingException( XMLAdapter adapter, OMElement tpos ) {
            super( adapter, tpos, "Unable to parse event time parameter." );
        }
    }

}
