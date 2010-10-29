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
package org.deegree.services.sos.capabilities;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.geometry.Envelope;
import org.deegree.observation.model.Offering;
import org.deegree.observation.model.Procedure;
import org.deegree.observation.model.Property;
import org.deegree.observation.persistence.ObservationDatastore;
import org.deegree.observation.persistence.simple.SimpleObservationDatastore;
import org.deegree.services.sos.getobservation.EventTime100XMLExporter;

/**
 * This is an xml adapter for ObservationOffering elements after the SOS 1.0.0 spec.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Offering100XMLAdapter extends XMLAdapter {

    private static final String SOS_NS = "http://www.opengis.net/sos/1.0";

    private static final String SOS_PREFIX = "sos";

    private static final String OM_NS = "http://www.opengis.net/om/1.0";

    private static final String OM_PREFIX = "om";

    private static final String GML_PREFIX = "gml";

    private static final String GML_NS = "http://www.opengis.net/gml";

    /**
     * Export a single SOS ObservationOffering.
     * 
     * @param writer
     * @param offering
     *            the ObservationOffering
     * @throws XMLStreamException
     */
    public static void export( XMLStreamWriter writer, Offering offering, ObservationDatastore datastore )
                            throws XMLStreamException {
        writer.setPrefix( SOS_PREFIX, SOS_NS );
        writer.setPrefix( GML_PREFIX, GML_NS );
        writer.setPrefix( OM_PREFIX, OM_NS );
        writer.setPrefix( "xlink", XLN_NS );

        writer.writeStartElement( SOS_NS, "ObservationOffering" );
        writer.writeAttribute( GML_NS, "id", offering.getObservationStoreId() );

        writer.writeStartElement( GML_NS, "name" );
        writer.writeCharacters( offering.getOfferingName() );
        writer.writeEndElement();

        exportBoundedBy( writer, offering );

        writer.writeStartElement( SOS_NS, "time" );
        EventTime100XMLExporter.exportSamplingTime( writer, datastore.getSamplingTime() );
        writer.writeEndElement();

        for ( Procedure proc : offering.getProcedures() ) {
            writer.writeStartElement( SOS_NS, "procedure" );
            writer.writeAttribute( XLN_NS, "href", proc.getProcedureHref() );
            writer.writeEndElement();
        }

        for ( Property prop : ( (SimpleObservationDatastore) datastore ).getProperties() ) {
            writer.writeStartElement( SOS_NS, "observedProperty" );
            writer.writeAttribute( XLN_NS, "href", prop.getHref() );
            writer.writeEndElement();
        }

        for ( Procedure proc : offering.getProcedures() ) {
            writer.writeStartElement( SOS_NS, "featureOfInterest" );
            writer.writeAttribute( XLN_NS, "href", proc.getFeatureOfInterestHref() );
            writer.writeEndElement();
        }

        writer.writeStartElement( SOS_NS, "responseFormat" );
        writer.writeCharacters( "text/xml;subtype=\"om/1.0.0\"" );
        writer.writeEndElement();

        writer.writeStartElement( SOS_NS, "responseMode" );
        writer.writeCharacters( "inline" );
        writer.writeEndElement();

        writer.writeEndElement(); // ObservationOffering
    }

    private static void exportBoundedBy( XMLStreamWriter writer, Offering offering )
                            throws XMLStreamException {
        writer.writeStartElement( GML_NS, "boundedBy" );
        Envelope env = offering.getBBOX();
        if ( env == null ) {
            writeElement( writer, GML_NS, "Null", "" );
        } else {
            writer.writeStartElement( GML_NS, "Envelope" );
            writer.writeAttribute( "srsName", "urn:ogc:def:crs:epsg::4326" );
            writer.writeStartElement( GML_NS, "lowerCorner" );
            writer.writeCharacters( env.getMin().get0() + " " + env.getMin().get1() );
            writer.writeEndElement();
            writer.writeStartElement( GML_NS, "upperCorner" );
            writer.writeCharacters( env.getMax().get0() + " " + env.getMax().get1() );
            writer.writeEndElement();
            writer.writeEndElement(); // Envelope
        }
        writer.writeEndElement(); // boundedBy
    }
}
