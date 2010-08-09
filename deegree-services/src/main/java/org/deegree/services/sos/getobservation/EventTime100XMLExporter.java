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
package org.deegree.services.sos.getobservation;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.sos.time.IndeterminateTime;
import org.deegree.protocol.sos.time.SamplingTime;
import org.deegree.protocol.sos.time.TimeInstant;
import org.deegree.protocol.sos.time.TimePeriod;

/**
 * The <code>EventTime100XMLExporter</code> class TODO
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class EventTime100XMLExporter extends XMLAdapter {

    private static final String GML_NS = "http://www.opengis.net/gml";

    private static final String OM_NS = "http://www.opengis.net/om/1.0";

    /**
     * Create a new EventTime adapter for SOS spec 1.0.0.
     * 
     * @param rootElement
     *            the EventTime element
     * @param systemId
     *            to resolve all relative files from.
     */
    public EventTime100XMLExporter( OMElement rootElement, String systemId ) {
        super( rootElement, systemId );
    }

    /**
     * Export indeterminate time as gml:TimeInstant/gml:timePosition.
     * 
     * @param writer
     * @param time
     * @throws XMLStreamException
     */
    public static void exportIndeterminateTime( XMLStreamWriter writer, IndeterminateTime time )
                            throws XMLStreamException {
        writer.writeStartElement( GML_NS, "TimeInstant" );
        writer.writeStartElement( GML_NS, "timePosition" );
        writer.writeAttribute( "indeterminatePosition", time.getType().name().toLowerCase() );
        writer.writeEndElement();
        writer.writeEndElement();
    }

    /**
     * Export {@link SamplingTime} as O&M samplingTime with gml:TimeInstant or gml:TimePeriod.
     * 
     * @param writer
     * @param time
     * @throws XMLStreamException
     */
    public static void exportOMSamplingTime( XMLStreamWriter writer, SamplingTime time )
                            throws XMLStreamException {
        writer.writeStartElement( OM_NS, "samplingTime" );
        exportSamplingTime( writer, time );
        writer.writeEndElement();
    }

    /**
     * Export {@link SamplingTime} as gml:TimeInstant or gml:TimePeriod.
     * 
     * @param writer
     * @param time
     * @throws XMLStreamException
     */
    public static void exportSamplingTime( XMLStreamWriter writer, SamplingTime time )
                            throws XMLStreamException {
        if ( time instanceof TimeInstant ) {
            exportTimeInstant( writer, (TimeInstant) time );
        } else if ( time instanceof TimePeriod ) {
            exportTimePeriod( writer, (TimePeriod) time );
        } else if ( time instanceof IndeterminateTime ) {
            exportIndeterminateTime( writer, (IndeterminateTime) time );
        }
    }

    /**
     * Export gml:TimeInstant.
     * 
     * @param writer
     * @param timeInstant
     * @throws XMLStreamException
     */
    public static void exportTimeInstant( XMLStreamWriter writer, TimeInstant timeInstant )
                            throws XMLStreamException {
        writer.writeStartElement( GML_NS, "TimeInstant" );
        writer.writeStartElement( GML_NS, "timePosition" );
        writer.writeCharacters( DateUtils.formatISO8601Date( timeInstant.getTime() ) );
        writer.writeEndElement();
        writer.writeEndElement();
    }

    /**
     * Export gml:TimePeriod.
     * 
     * @param writer
     * @param timePeriod
     * @throws XMLStreamException
     */
    public static void exportTimePeriod( XMLStreamWriter writer, TimePeriod timePeriod )
                            throws XMLStreamException {
        writer.writeStartElement( GML_NS, "TimePeriod" );
        writer.writeStartElement( GML_NS, "beginPosition" );
        writer.writeCharacters( DateUtils.formatISO8601Date( timePeriod.getBegin() ) );
        writer.writeEndElement();
        writer.writeStartElement( GML_NS, "endPosition" );
        writer.writeCharacters( DateUtils.formatISO8601Date( timePeriod.getEnd() ) );
        writer.writeEndElement();
        writer.writeEndElement();
    }

}
