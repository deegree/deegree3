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
package org.deegree.gml.feature.schema;

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.GML_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XSNS;
import static org.deegree.commons.xml.CommonNamespaces.XS_PREFIX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.PrimitiveType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates GML application schemas from {@link ApplicationSchema} instances.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ApplicationSchemaXSDEncoder {

    private static final Logger LOG = LoggerFactory.getLogger( ApplicationSchemaXSDEncoder.class );

    private static final String GML_2_DEFAULT_INCLUDE = "http://schemas.opengis.net/gml/2.1.2.1/feature.xsd";

    private static final String GML_30_DEFAULT_INCLUDE = "http://schemas.opengis.net/gml/3.0.1/base/gml.xsd";

    private static final String GML_31_DEFAULT_INCLUDE = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";

    private static final String GML_32_DEFAULT_INCLUDE = "http://schemas.opengis.net/gml/3.2.1/gml.xsd";

    private final GMLVersion version;

    private String gmlNsURI;

    private final Map<String, String> importURLs;

    // set to "gml:_Feature" (GML 2 and 3.1) or "gml:AbstractFeatureType" (GML 3.2)
    private String abstractGMLFeatureElement;

    // set to "gml:FeatureAssociationType" (GML 2) or "gml:FeaturePropertyType" (GML 3.1 / GML 3.2)
    private String featurePropertyType;

    /**
     * Creates a new {@link ApplicationSchemaXSDEncoder} for the given GML version and optional import URL.
     * 
     * @param version
     *            gml version that exported schemas will comply to
     * @param importURLs
     *            to be imported in the generated schema document, this may also contain a URL for the gml namespace
     */
    public ApplicationSchemaXSDEncoder( GMLVersion version, Map<String, String> importURLs ) {

        this.version = version;
        if ( importURLs == null ) {
            this.importURLs = new HashMap<String, String>();
        } else {
            this.importURLs = importURLs;
        }
        switch ( version ) {
        case GML_2:
            gmlNsURI = GMLNS;
            abstractGMLFeatureElement = "gml:_Feature";
            featurePropertyType = "gml:FeatureAssociationType";
            if ( !this.importURLs.containsKey( gmlNsURI ) ) {
                this.importURLs.put( gmlNsURI, GML_2_DEFAULT_INCLUDE );
            }
            break;
        case GML_30:
            gmlNsURI = GMLNS;
            abstractGMLFeatureElement = "gml:_Feature";
            featurePropertyType = "gml:FeaturePropertyType";
            if ( !this.importURLs.containsKey( gmlNsURI ) ) {
                this.importURLs.put( gmlNsURI, GML_30_DEFAULT_INCLUDE );
            }
            break;
        case GML_31:
            gmlNsURI = GMLNS;
            abstractGMLFeatureElement = "gml:_Feature";
            featurePropertyType = "gml:FeaturePropertyType";
            if ( !this.importURLs.containsKey( gmlNsURI ) ) {
                this.importURLs.put( gmlNsURI, GML_31_DEFAULT_INCLUDE );
            }
            break;
        case GML_32:
            gmlNsURI = GML3_2_NS;
            abstractGMLFeatureElement = "gml:AbstractFeature";
            featurePropertyType = "gml:FeaturePropertyType";
            if ( !this.importURLs.containsKey( gmlNsURI ) ) {
                this.importURLs.put( gmlNsURI, GML_32_DEFAULT_INCLUDE );
            }
            break;
        }
    }

    /**
     * @param writer
     * @param schema
     * @throws XMLStreamException
     */
    public void export( XMLStreamWriter writer, ApplicationSchema schema )
                            throws XMLStreamException {

        // TODO prefix handling
        final String uri = schema.getFeatureTypes()[0].getName().getNamespaceURI();
        if ( uri != null && !uri.isEmpty() ) {
            writer.setPrefix( "app", uri );
        }

        writer.setPrefix( XS_PREFIX, XSNS );
        writer.setPrefix( GML_PREFIX, gmlNsURI );

        writer.writeStartElement( XSNS, "schema" );
        writer.writeNamespace( XS_PREFIX, XSNS );
        writer.writeNamespace( GML_PREFIX, gmlNsURI );
        writer.writeAttribute( "attributeFormDefault", "unqualified" );
        writer.writeAttribute( "elementFormDefault", "qualified" );

        for ( String importNamespace : importURLs.keySet() ) {
            writer.writeEmptyElement( XSNS, "import" );
            writer.writeAttribute( "namespace", importNamespace );
            writer.writeAttribute( "schemaLocation", importURLs.get( importNamespace ) );
        }

        // export feature type declarations
        for ( FeatureType ft : schema.getFeatureTypes() ) {
            export( writer, ft );
        }

        // end 'xs:schema'
        writer.writeEndElement();
    }

    /**
     * @param writer
     * @param fts
     * @throws XMLStreamException
     */
    public void export( XMLStreamWriter writer, List<FeatureType> fts )
                            throws XMLStreamException {

        for ( FeatureType ft : fts ) {
            LOG.debug( "Exporting ft " + ft.getName() );
        }

        // TODO prefix handling
        final String ns = fts.get( 0 ).getName().getNamespaceURI();
        if ( ns != null && !ns.isEmpty() ) {
            writer.setPrefix( "app", ns );
        }

        writer.setPrefix( XS_PREFIX, XSNS );
        writer.setPrefix( GML_PREFIX, gmlNsURI );

        writer.writeStartElement( XSNS, "schema" );
        writer.writeNamespace( XS_PREFIX, XSNS );
        writer.writeNamespace( GML_PREFIX, gmlNsURI );
        if ( ns != null && !ns.isEmpty() ) {
            writer.writeAttribute( "targetNamespace", ns );
            writer.writeAttribute( "elementFormDefault", "qualified" );
            writer.writeAttribute( "attributeFormDefault", "unqualified" );
        } else {
            writer.writeAttribute( "elementFormDefault", "unqualified" );
            writer.writeAttribute( "attributeFormDefault", "unqualified" );
        }

        for ( String importNamespace : importURLs.keySet() ) {
            writer.writeEmptyElement( XSNS, "import" );
            writer.writeAttribute( "namespace", importNamespace );
            writer.writeAttribute( "schemaLocation", importURLs.get( importNamespace ) );
        }

        if ( ns == null || ns.isEmpty() ) {
            writer.writeStartElement( XSNS, "element" );
            writer.writeAttribute( "name", "FeatureCollection" );
            writer.writeAttribute( "substitutionGroup", "gml:_FeatureCollection" );
            writer.writeAttribute( "type", "FeatureCollectionType" );
            writer.writeEndElement();
            writer.writeStartElement( XSNS, "complexType" );
            writer.writeAttribute( "name", "FeatureCollectionType" );
            writer.writeStartElement( XSNS, "complexContent" );
            writer.writeStartElement( XSNS, "extension" );
            writer.writeAttribute( "base", "gml:AbstractFeatureCollectionType" );
            writer.writeStartElement( XSNS, "sequence" );
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
        }

        // export feature type declarations
        for ( FeatureType ft : fts ) {
            export( writer, ft );
        }

        // end 'xs:schema'
        writer.writeEndElement();
    }

    private void export( XMLStreamWriter writer, FeatureType ft )
                            throws XMLStreamException {

        writer.writeStartElement( XSNS, "element" );
        // TODO (what about features in other namespaces???)
        writer.writeAttribute( "name", ft.getName().getLocalPart() );
        writer.writeAttribute( "substitutionGroup", abstractGMLFeatureElement );

        writer.writeStartElement( XSNS, "complexType" );
        writer.writeStartElement( XSNS, "complexContent" );
        writer.writeStartElement( XSNS, "extension" );
        // TODO handle derived feature types
        writer.writeAttribute( "base", "gml:AbstractFeatureType" );
        writer.writeStartElement( XSNS, "sequence" );

        // TODO check for GML 2 properties (gml:pointProperty, ...) and export as "app:gml2PointProperty" for GML 3

        // export property definitions (only for non-GML ones)
        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            export( writer, pt, version );
        }

        // end 'xs:sequence'
        writer.writeEndElement();
        // end 'xs:extension'
        writer.writeEndElement();
        // end 'xs:complexContent'
        writer.writeEndElement();
        // end 'xs:complexType'
        writer.writeEndElement();
        // end 'xs:element'
        writer.writeEndElement();
    }

    private void export( XMLStreamWriter writer, PropertyType pt, GMLVersion version )
                            throws XMLStreamException {

        writer.writeStartElement( XSNS, "element" );
        // TODO (what about properties in other namespaces???)
        writer.writeAttribute( "name", pt.getName().getLocalPart() );

        if ( pt.getMinOccurs() != 1 ) {
            writer.writeAttribute( "minOccurs", "" + pt.getMinOccurs() );
        }
        if ( pt.getMaxOccurs() != 1 ) {
            if ( pt.getMaxOccurs() == -1 ) {
                writer.writeAttribute( "maxOccurs", "unbounded" );
            } else {
                writer.writeAttribute( "maxOccurs", "" + pt.getMaxOccurs() );
            }
        }

        if ( pt instanceof SimplePropertyType ) {
            PrimitiveType type = ((SimplePropertyType) pt ).getPrimitiveType();          
            switch (type) {
            case BOOLEAN:
                writer.writeAttribute( "type", "xs:boolean" );
                break;
            case DATE:
                writer.writeAttribute( "type", "xs:date" );
                break;
            case DATE_TIME:
                writer.writeAttribute( "type", "xs:dateTime" );
                break;
            case DECIMAL:
                writer.writeAttribute( "type", "xs:decimal" );
                break;
            case DOUBLE:
                writer.writeAttribute( "type", "xs:double" );
                break;
            case INTEGER:
                writer.writeAttribute( "type", "xs:integer" );
                break;
            case STRING:
                writer.writeAttribute( "type", "xs:string" );
                break;
            case TIME:
                writer.writeAttribute( "type", "xs:string" );
                break;
            }
        } else if ( pt instanceof GeometryPropertyType ) {
            // TODO handle restricted types (e.g. 'gml:PointPropertyType')
            writer.writeAttribute( "type", "gml:GeometryPropertyType" );
        } else if ( pt instanceof FeaturePropertyType ) {
            QName containedFt = ( (FeaturePropertyType) pt ).getFTName();
            if ( containedFt != null ) {
                writer.writeStartElement( XSNS, "complexType" );
                writer.writeStartElement( XSNS, "sequence" );
                writer.writeEmptyElement( XSNS, "element" );
                // TODO
                writer.writeAttribute( "ref", "app:" + containedFt.getLocalPart() );
                writer.writeAttribute( "minOccurs", "0" );
                // end 'xs:sequence'
                writer.writeEndElement();
                writer.writeEmptyElement( XSNS, "attributeGroup" );
                writer.writeAttribute( "ref", "gml:AssociationAttributeGroup" );
                // end 'xs:complexType'
                writer.writeEndElement();
            } else {
                writer.writeAttribute( "type", featurePropertyType );
            }
        } else if ( pt instanceof CodePropertyType ) {
            writer.writeAttribute( "type", "gml:CodeType" );
        } else if ( pt instanceof MeasurePropertyType ) {
            writer.writeAttribute( "type", "gml:MeasureType" );
        } else if ( pt instanceof CustomPropertyType ) {
            writer.writeComment( "TODO: export custom property type information" );
            writer.writeStartElement( XSNS, "complexType" );
            writer.writeAttribute( "mixed", "true" );
            writer.writeStartElement( XSNS, "sequence" );
            writer.writeStartElement( XSNS, "any" );
            writer.writeAttribute( "minOccurs", "0" );
            writer.writeAttribute( "maxOccurs", "unbounded" );
            writer.writeAttribute( "processContents", "lax" );
            writer.writeEndElement();
            writer.writeEndElement();
            XMLAdapter.writeElement( writer, XSNS, "anyAttribute", null, "processContents", "lax" );
            writer.writeEndElement();
        }

        // end 'xs:element'
        writer.writeEndElement();
    }
}
