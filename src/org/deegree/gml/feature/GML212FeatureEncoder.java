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

package org.deegree.gml.feature;

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.types.ows.CodeType;
import org.deegree.commons.uom.Length;
import org.deegree.commons.uom.Measure;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Property;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.EnvelopePropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.LengthPropertyType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.gml.GML21GeometryEncoder;
import org.deegree.geometry.gml.GMLGeometryEncoder;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exporter class for Features and properties that delegates exporting tasks to the <code>GML311GeometryExporter</code>
 * Please note that this class is just a copy of the GMLFeatureExporter, and it conforms to the appropriate schemas just
 * by accident!
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GML212FeatureEncoder implements GMLFeatureEncoder {

    private static final Logger LOG = LoggerFactory.getLogger( GML212FeatureEncoder.class );

    private Set<String> exportedIds = new HashSet<String>();

    private XMLStreamWriter writer;

    private GMLGeometryEncoder geometryExporter;

    /**
     * @param writer
     * @param outputCRS
     *            crs used for exported geometries, may be <code>null</code> (in that case, the crs of the geometries is
     *            used)
     */
    public GML212FeatureEncoder( XMLStreamWriter writer, CRS outputCRS ) {
        this.writer = writer;
        geometryExporter = new GML21GeometryEncoder( writer, exportedIds );
    }

    // public void export( FeatureCollection featureCol ) throws XMLStreamException {
    // QName fcName = featureCol.getName();
    // LOG.debug( "Exporting FeatureCollection " + fcName );
    // writeStartElementWithNS( fcName.getNamespaceURI(), fcName.getLocalPart() );
    // if ( firstElement ) {
    // writer.writeAttribute( schemaAttributeName, schemaAttributeValue ); //set schema
    // firstElement = false;
    // }
    // Iterator<Feature> iterator = featureCol.iterator();
    // while ( iterator.hasNext() ) {
    // Feature f = iterator.next();
    // writer.writeStartElement( GMLNS, "featureMember" );
    // export( ( Property<?> ) f );
    // writer.writeEndElement();
    // }
    // writer.writeEndElement();
    // }

    /**
     * @param feature
     * @throws XMLStreamException
     * @throws TransformationException
     * @throws UnknownCRSException
     */
    @Override
    public void export( Feature feature )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        QName featureName = feature.getName();
        LOG.debug( "Exporting Feature {} with ID {}", featureName, feature.getId() );
        writeStartElementWithNS( featureName.getNamespaceURI(), featureName.getLocalPart() );
        if ( feature.getId() != null ) {
            writer.writeAttribute( "fid", feature.getId() );
        }
        for ( Property<?> prop : feature.getProperties( GMLVersion.GML_2 ) ) {
            export( prop );
        }
        writer.writeEndElement();
    }

    /**
     * @param col
     * @param schemaLocation
     *            may be null
     * @throws XMLStreamException
     * @throws TransformationException
     * @throws UnknownCRSException
     */
    public void export( FeatureCollection col, String schemaLocation )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        LOG.debug( "Exporting generic feature collection." );
        writer.setPrefix( "gml", GMLNS );
        writer.writeStartElement( "FeatureCollection" );
        if ( schemaLocation != null ) {
            writer.setPrefix( "xsi", XSINS );
            writer.writeAttribute( XSINS, "noNamespaceSchemaLocation", schemaLocation );
        }

        writer.writeStartElement( GMLNS, "boundedBy" );
        Envelope fcEnv = col.getEnvelope();
        if ( fcEnv != null ) {
            geometryExporter.exportEnvelope( col.getEnvelope() );
        } else {
            writer.writeEmptyElement( GMLNS, "null" );
        }
        writer.writeEndElement();
        for ( Feature f : col ) {
            writer.writeStartElement( "http://www.opengis.net/gml", "featureMember" );
            export( f );
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void export( Property<?> property )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        QName propName = property.getName();
        PropertyType<?> propertyType = property.getType();
        Object value = property.getValue();
        if ( propertyType instanceof FeaturePropertyType ) {
            Feature fValue = (Feature) value;
            if ( fValue.getId() != null && exportedIds.contains( fValue.getId() ) ) {
                writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                writer.writeAttribute( XLNNS, "href", "#" + fValue.getId() );
            } else {
                exportedIds.add( fValue.getId() );
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                export( fValue );
                writer.writeEndElement();
            }
        } else if ( propertyType instanceof SimplePropertyType<?> ) {
            if ( value != null ) {
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                writer.writeCharacters( value.toString() );
                writer.writeEndElement();
            }

        } else if ( propertyType instanceof GeometryPropertyType ) {
            Geometry gValue = (Geometry) value;
            if ( gValue.getId() != null && exportedIds.contains( gValue.getId() ) ) {
                writeEmptyElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                writer.writeAttribute( XLNNS, "href", "#" + gValue.getId() );
            } else {
                exportedIds.add( gValue.getId() );
                writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
                geometryExporter.export( (Geometry) value );
                writer.writeEndElement();
            }

        } else if ( propertyType instanceof CodePropertyType ) {
            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
            CodeType codeType = (CodeType) value;
            if ( codeType.getCodeSpace() != null && codeType.getCodeSpace().length() > 0 )
                writer.writeAttribute( "codeSpace", codeType.getCodeSpace() );
            writer.writeCharacters( codeType.getCode() );
            writer.writeEndElement();

        } else if ( propertyType instanceof EnvelopePropertyType ) {
            geometryExporter.exportEnvelope( (Envelope) value );

        } else if ( propertyType instanceof LengthPropertyType ) {
            Length length = (Length) value;
            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
            writer.writeAttribute( "uom", length.getUomUri() );
            writer.writeCharacters( String.valueOf( length.getValue() ) );
            writer.writeEndElement();

        } else if ( propertyType instanceof MeasurePropertyType ) {
            Measure measure = (Measure) value;
            writeStartElementWithNS( propName.getNamespaceURI(), propName.getLocalPart() );
            writer.writeAttribute( "uom", measure.getUomUri() );
            writer.writeCharacters( String.valueOf( measure.getValue() ) );
            writer.writeEndElement();
        }
    }

    /**
     * @param namespaceURI
     * @param localname
     * @throws XMLStreamException
     */
    void writeStartElementWithNS( String namespaceURI, String localname )
                            throws XMLStreamException {
        if ( namespaceURI == null || namespaceURI.length() == 0 )
            writer.writeStartElement( localname );
        else
            writer.writeStartElement( namespaceURI, localname );
    }

    /**
     * @param namespaceURI
     * @param localname
     * @throws XMLStreamException
     */
    void writeEmptyElementWithNS( String namespaceURI, String localname )
                            throws XMLStreamException {
        if ( namespaceURI == null || namespaceURI.length() == 0 )
            writer.writeEmptyElement( localname );
        else
            writer.writeEmptyElement( namespaceURI, localname );
    }

    @Override
    public boolean isExported( String memberFid ) {
        return exportedIds.contains( memberFid );
    }
}
