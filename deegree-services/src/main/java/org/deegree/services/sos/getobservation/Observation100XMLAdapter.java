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
package org.deegree.services.sos.getobservation;

import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.services.sos.getobservation.EventTime100XMLExporter.exportOMSamplingTime;

import java.util.List;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.observation.model.Measurement;
import org.deegree.observation.model.MeasurementCollection;
import org.deegree.observation.model.Observation;
import org.deegree.observation.model.Property;
import org.deegree.observation.model.Result;

/**
 * This is an xml adapter for the O&M 1.0.0 spec.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class Observation100XMLAdapter extends XMLAdapter {
    // private static final String OGC_NS = "http://www.opengis.net/ogc";
    //
    // private static final String OGC_PREFIX = "ogc";

    private static final String OM_NS = "http://www.opengis.net/om/1.0";

    private static final String OM_SCHEMA = "http://schemas.opengis.net/om/1.0.0/om.xsd";

    private static final String SA_NS = "http://www.opengis.net/sampling/1.0";

    private static final String SA_SCHEMA = "http://schemas.opengis.net/sampling/1.0.0/sampling.xsd";

    private static final String OM_PREFIX = "om";

    private static final String GML_PREFIX = "gml";

    private static final String SA_PREFIX = "sa";

    private static final String GML_NS = "http://www.opengis.net/gml";

    private static final String SWE_NS = "http://www.opengis.net/swe/1.0.1";

    private enum TYPE {
        OBSERVATION, MEASUREMENT
    }

    /**
     * Export a {@link MeasurementCollection} as a O&M Observation resultModel.
     * 
     * @param writer
     * @param observation
     * 
     * @throws XMLStreamException
     */
    public static void exportOMObservation( XMLStreamWriter writer, Observation observation )
                            throws XMLStreamException {
        export( writer, observation, TYPE.OBSERVATION );
    }

    /**
     * Export a {@link MeasurementCollection} as a O&M Measurement resultModel.
     * 
     * @param writer
     * @param observation
     * 
     * @throws XMLStreamException
     */
    public static void exportOMMeasurement( XMLStreamWriter writer, Observation observation )
                            throws XMLStreamException {
        export( writer, observation, TYPE.MEASUREMENT );
    }

    private static void export( XMLStreamWriter writer, Observation observation, TYPE type )
                            throws XMLStreamException {
        writer.setPrefix( OM_PREFIX, OM_NS );
        writer.setPrefix( SA_PREFIX, SA_NS );
        writer.setPrefix( GML_PREFIX, GML_NS );
        writer.setPrefix( "swe", SWE_NS );
        writer.setPrefix( "xlink", XLN_NS );
        writer.setPrefix( "xsi", XSINS );

        writer.writeStartElement( OM_NS, "ObservationCollection" );
        writer.writeAttribute( XSINS, "schemaLocation", OM_NS + " " + OM_SCHEMA + " " + SA_NS + " " + SA_SCHEMA );

        if ( observation == null || observation.size() == 0 ) { // empty collection
            writer.writeStartElement( OM_NS, "member" );
            writer.writeAttribute( XLN_NS, "href", "urn:ogc:def:nil:OGC:inapplicable" );
            writer.writeEndElement();
            // } else if ( collection.size() == 1 ) {
            // exportObservation( writer, collection.iterator().next() );
        } else {
            for ( MeasurementCollection mc : observation ) {
                if ( type == TYPE.OBSERVATION ) {
                    Measurement m = mc.iterator().next();
                    writer.writeStartElement( OM_NS, "member" );
                    writer.writeStartElement( OM_NS, "Observation" );
                    exportOMSamplingTime( writer, observation.getSamplingTime() );
                    exportProcedure( writer, m );
                    exportProperties( writer, m.getProperties() );
                    exportFoI( writer, m );
                    writer.writeStartElement( OM_NS, "result" );
                    DataArray101XMLAdapter.export( writer, mc );
                    writer.writeEndElement(); // result
                    writer.writeEndElement(); // Observation
                    writer.writeEndElement(); // member
                } else {
                    for ( Measurement m : mc ) {
                        exportObservation( writer, m );
                    }
                }
            }
        }
        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private static void exportObservation( XMLStreamWriter writer, Measurement m )
                            throws XMLStreamException {
        // each result/observedProperty must be encoded in its own observation, sigh.
        for ( Result result : m.getResults() ) {
            writer.writeStartElement( OM_NS, "member" );
            writer.writeStartElement( OM_NS, "Observation" );
            exportOMSamplingTime( writer, m.getSamplingTime() );
            exportProcedure( writer, m );
            exportProperty( writer, result.getProperty() );
            exportFoI( writer, m );
            exportResult( writer, result );
            writer.writeEndElement(); // member
            writer.writeEndElement(); // Observation
        }
    }

    private static void exportProcedure( XMLStreamWriter writer, Measurement m )
                            throws XMLStreamException {
        writer.writeStartElement( OM_NS, "procedure" );
        writer.writeAttribute( XLN_NS, "href", m.getProcedure().getProcedureHref() );
        writer.writeEndElement();
    }

    private static void exportFoI( XMLStreamWriter writer, Measurement m )
                            throws XMLStreamException {
        writer.writeStartElement( OM_NS, "featureOfInterest" );
        // exportFeature( writer, m.getProcedure().getGeometry() ); // TODO from the spec this is a reference type
        writer.writeAttribute( XLN_NS, "href", m.getFeatureOfInterest().toString() );
        writer.writeEndElement();
    }

    // private void exportFeature( XMLStreamWriter writer, Geometry geometry )
    // throws XMLStreamException {
    // writer.writeStartElement( GML_NS, "FeatureCollection" );
    // writer.writeStartElement( GML_NS, "featureMember" );
    // if ( geometry instanceof Point ) {
    // Point pointGeom = (Point) geometry;
    // exportSamplingPoint( writer, pointGeom );
    // }
    // writer.writeEndElement(); // featureMember
    // writer.writeEndElement(); // featureCollection
    // }

    // private void exportSamplingPoint( XMLStreamWriter writer, Point pointGeom )
    // throws XMLStreamException {
    // writer.writeStartElement( SA_NS, "SamplingPoint" );
    // writeElement( writer, SA_NS, "sampledFeature", "" );
    // writer.writeStartElement( SA_NS, "position" );
    // writer.writeStartElement( GML_NS, "Point" );
    // writer.writeAttribute( "srsName",
    // ( srs == null || srs.equals( "" ) ) ? pointGeom.getCoordinateSystem().getName() : srs );
    // writeElement( writer, GML_NS, "pos", pointGeom.getX() + " " + pointGeom.getY() );
    // writer.writeEndElement(); // Point
    // writer.writeEndElement(); // position
    // writer.writeEndElement(); // SampingPoint
    // }

    private static void exportProperty( XMLStreamWriter writer, Property property )
                            throws XMLStreamException {
        writer.writeStartElement( OM_NS, "observedProperty" );
        writer.writeAttribute( XLN_NS, "href", property.getHref() );
        writer.writeEndElement();
    }

    private static void exportProperties( XMLStreamWriter writer, List<Property> properties )
                            throws XMLStreamException {
        if ( properties.size() == 1 ) {
            exportProperty( writer, properties.get( 0 ) );
        } else {
            writer.writeStartElement( OM_NS, "observedProperty" );
            writer.writeStartElement( SWE_NS, "CompositePhenomenon" );
            writer.writeAttribute( GML_NS, "id", "_" + UUID.randomUUID().toString().replace( '-', '_' ) );
            writer.writeAttribute( "dimension", Integer.toString( properties.size() ) );
            writer.writeStartElement( GML_NS, "name" );
            writer.writeCharacters( "observedProperties" );
            writer.writeEndElement();
            for ( Property property : properties ) {
                writer.writeStartElement( SWE_NS, "component" );
                writer.writeAttribute( XLN_NS, "href", property.getHref() );
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private static void exportResult( XMLStreamWriter writer, Result r )
                            throws XMLStreamException {
        writer.writeStartElement( OM_NS, "result" );
        writer.writeAttribute( XSINS, "type", GML_PREFIX + ":MeasureType" );
        writer.writeAttribute( "uom", r.getProperty().getOptionValue( "uom" ) );
        writer.writeCharacters( r.getResultAsString() );
        writer.writeEndElement();
    }
}
