//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.feature.gml.schema;

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.GML_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XSNS;
import static org.deegree.commons.xml.CommonNamespaces.XS_PREFIX;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CustomComplexPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;

/**
 * Generates GML application schemas from {@link ApplicationSchema} instances.
 * <p>
 * The following GML flavours are supported:
 * <ul>
 * <li>GML 2 (2.1.2)</li>
 * <li>GML 3 (3.1.1)</li>
 * <li>GML 3.2 (3.2.1)</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class ApplicationSchemaXSDExporter {

    private static final String GML_212_DEFAULT_INCLUDE = "http://schemas.opengis.net/gml/2.1.2/feature.xsd";

    private static final String GML_311_DEFAULT_INCLUDE = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";

    private static final String GML_321_DEFAULT_INCLUDE = "http://schemas.opengis.net/gml/3.2.1/gml.xsd";

    private final GMLVersion version;

    private String gmlNsURI;

    private Map<String, String> importURLs;

    // set to "gml:_Feature" (GML 2 and 3.1) or "gml:AbstractFeatureType" (GML 3.2)
    private String abstractGMLFeatureElement;

    /**
     * Creates a new {@link ApplicationSchemaXSDExporter} for the given GML version and optional import URL.
     * 
     * @param version
     *            gml version that exported schemas will comply to
     * @param importURLs
     *            to be imported in the generated schema document, this may also contain a URL for the gml namespace
     */
    public ApplicationSchemaXSDExporter( GMLVersion version, Map<String, String> importURLs ) {

        this.version = version;
        this.importURLs = importURLs;
        switch ( version ) {
        case GML_2:
            gmlNsURI = GMLNS;
            abstractGMLFeatureElement = "gml:_Feature";
            if ( !importURLs.containsKey( gmlNsURI ) ) {
                importURLs.put( gmlNsURI, GML_212_DEFAULT_INCLUDE );
            }
            break;
        case GML_31:
            gmlNsURI = GMLNS;
            abstractGMLFeatureElement = "gml:_Feature";
            if ( !importURLs.containsKey( gmlNsURI ) ) {
                importURLs.put( gmlNsURI, GML_311_DEFAULT_INCLUDE );
            }
            break;
        case GML_32:
            gmlNsURI = GML3_2_NS;
            abstractGMLFeatureElement = "gml:AbstractFeature";
            if ( !importURLs.containsKey( gmlNsURI ) ) {
                importURLs.put( gmlNsURI, GML_321_DEFAULT_INCLUDE );
            }
            break;
        }
    }

    public void export( XMLStreamWriter writer, ApplicationSchema schema )
                            throws XMLStreamException {

        writer.setPrefix( XS_PREFIX, XSNS );
        writer.setPrefix( GML_PREFIX, gmlNsURI );

        writer.writeStartElement( XSNS, "schema" );
        writer.writeNamespace( XS_PREFIX, XSNS );
        writer.writeNamespace( GML_PREFIX, gmlNsURI );

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

    public void export( XMLStreamWriter writer, List<FeatureType> fts )
                            throws XMLStreamException {

        writer.setPrefix( XS_PREFIX, XSNS );
        writer.setPrefix( GML_PREFIX, gmlNsURI );

        writer.writeStartElement( XSNS, "schema" );
        writer.writeNamespace( XS_PREFIX, XSNS );
        writer.writeNamespace( GML_PREFIX, gmlNsURI );
        writer.writeAttribute( "targetNamespace", fts.get( 0 ).getName().getNamespaceURI() );
        writer.writeAttribute( "elementFormDefault", "qualified" );
        writer.writeAttribute( "attributeFormDefault", "unqualified" );

        for ( String importNamespace : importURLs.keySet() ) {
            writer.writeEmptyElement( XSNS, "import" );
            writer.writeAttribute( "namespace", importNamespace );
            writer.writeAttribute( "schemaLocation", importURLs.get( importNamespace ) );
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

    private static void export( XMLStreamWriter writer, PropertyType pt, GMLVersion version )
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

        } else if ( pt instanceof GeometryPropertyType ) {

        } else if ( pt instanceof FeaturePropertyType ) {
            QName containedFt = ( (FeaturePropertyType) pt ).getFTName();
            if ( containedFt != null ) {
                writer.writeStartElement( XSNS, "complexType" );
                writer.writeStartElement( XSNS, "sequence" );
                writer.writeEmptyElement( XSNS, "element" );
                writer.writeAttribute( "ref", containedFt.getPrefix() + ":" + containedFt.getLocalPart() );
                writer.writeAttribute( "minOccurs", "0" );
                writer.writeEmptyElement( XSNS, "attributeGroup" );
                writer.writeAttribute( "ref", "gml:AssociationAttributeGroup" );
                // end 'xs:sequence'
                writer.writeEndElement();
                // end 'xs:complexType'
                writer.writeEndElement();
            } else {
                writer.writeAttribute( "type", "gml:FeaturePropertyType" );
            }
        } else if ( pt instanceof CustomComplexPropertyType ) {

        }

        // end 'xs:element'
        writer.writeEndElement();
    }
}
