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

import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_ELEMENT;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_EMPTY;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_MIXED;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_SIMPLE;
import static org.apache.xerces.xs.XSConstants.DERIVATION_EXTENSION;
import static org.apache.xerces.xs.XSConstants.DERIVATION_LIST;
import static org.apache.xerces.xs.XSConstants.DERIVATION_NONE;
import static org.apache.xerces.xs.XSConstants.DERIVATION_RESTRICTION;
import static org.apache.xerces.xs.XSConstants.DERIVATION_SUBSTITUTION;
import static org.apache.xerces.xs.XSConstants.DERIVATION_UNION;
import static org.apache.xerces.xs.XSModelGroup.COMPOSITOR_ALL;
import static org.apache.xerces.xs.XSModelGroup.COMPOSITOR_CHOICE;
import static org.apache.xerces.xs.XSModelGroup.COMPOSITOR_SEQUENCE;
import static org.apache.xerces.xs.XSSimpleTypeDefinition.FACET_ENUMERATION;
import static org.apache.xerces.xs.XSSimpleTypeDefinition.FACET_FRACTIONDIGITS;
import static org.apache.xerces.xs.XSSimpleTypeDefinition.FACET_LENGTH;
import static org.apache.xerces.xs.XSSimpleTypeDefinition.FACET_MAXEXCLUSIVE;
import static org.apache.xerces.xs.XSSimpleTypeDefinition.FACET_MAXINCLUSIVE;
import static org.apache.xerces.xs.XSSimpleTypeDefinition.FACET_MAXLENGTH;
import static org.apache.xerces.xs.XSSimpleTypeDefinition.FACET_MINEXCLUSIVE;
import static org.apache.xerces.xs.XSSimpleTypeDefinition.FACET_MININCLUSIVE;
import static org.apache.xerces.xs.XSSimpleTypeDefinition.FACET_MINLENGTH;
import static org.apache.xerces.xs.XSSimpleTypeDefinition.FACET_NONE;
import static org.apache.xerces.xs.XSSimpleTypeDefinition.FACET_PATTERN;
import static org.apache.xerces.xs.XSSimpleTypeDefinition.FACET_TOTALDIGITS;
import static org.apache.xerces.xs.XSSimpleTypeDefinition.FACET_WHITESPACE;
import static org.apache.xerces.xs.XSTypeDefinition.COMPLEX_TYPE;
import static org.apache.xerces.xs.XSTypeDefinition.SIMPLE_TYPE;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.GML_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XSNS;
import static org.deegree.commons.xml.CommonNamespaces.XS_PREFIX;
import static org.deegree.gml.GMLVersion.GML_2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSFacet;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSWildcard;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stream-based writer for GML application schemas.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ApplicationSchemaXSDEncoder {

    private static final Logger LOG = LoggerFactory.getLogger( ApplicationSchemaXSDEncoder.class );

    public static final String GML_2_DEFAULT_INCLUDE = "http://schemas.opengis.net/gml/2.1.2/feature.xsd";

    public static final String GML_30_DEFAULT_INCLUDE = "http://schemas.opengis.net/gml/3.0.1/base/gml.xsd";

    public static final String GML_31_DEFAULT_INCLUDE = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";

    public static final String GML_32_DEFAULT_INCLUDE = "http://schemas.opengis.net/gml/3.2.1/gml.xsd";

    private final GMLVersion version;

    private String gmlNsURI;

    private final Map<String, String> importURLs;

    // set to "gml:_Feature" (GML 2 and 3.1) or "gml:AbstractFeatureType" (GML 3.2)
    private String abstractGMLFeatureElement;

    // set to "gml:FeatureAssociationType" (GML 2) or "gml:FeaturePropertyType" (GML 3.1 / GML 3.2)
    private String featurePropertyType;

    // stores all custom simple types that occured as types of simple properties
    private Map<QName, XSSimpleTypeDefinition> customSimpleTypes = new LinkedHashMap<QName, XSSimpleTypeDefinition>();

    private List<XSTypeDefinition> separateTypes = new ArrayList<XSTypeDefinition>();

    private Set<QName> exportedFts = new HashSet<QName>();

    /**
     * Creates a new {@link ApplicationSchemaXSDEncoder} for the given GML version and optional import URL.
     * 
     * @param version
     *            gml version that exported schemas will comply to, must not be <code>null</code>
     * @param importURLs
     *            to be imported in the generated schema document, this may also contain a URL for the gml namespace,
     *            may be <code>null</code>
     */
    public ApplicationSchemaXSDEncoder( GMLVersion version, Map<String, String> importURLs ) {

        this.version = version;
        if ( importURLs == null ) {
            this.importURLs = new HashMap<String, String>();
        } else {
            this.importURLs = new HashMap<String, String>( importURLs );
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

        // export custom simple type declarations
        for ( XSSimpleTypeDefinition xsSimpleType : customSimpleTypes.values() ) {
            export( writer, xsSimpleType );
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

        // TODO better prefix handling
        final String targetNs = fts.get( 0 ).getName().getNamespaceURI();
        final String targetPrefix = fts.get( 0 ).getName().getPrefix();
        if ( targetNs != null && !targetNs.isEmpty() ) {
            writer.setPrefix( "app", targetNs );
        }

        writer.setPrefix( XS_PREFIX, XSNS );
        writer.setPrefix( GML_PREFIX, gmlNsURI );

        writer.writeStartElement( XSNS, "schema" );
        writer.writeNamespace( XS_PREFIX, XSNS );
        writer.writeNamespace( GML_PREFIX, gmlNsURI );
        if ( targetNs != null && !targetNs.isEmpty() ) {
            writer.writeAttribute( "targetNamespace", targetNs );
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

        if ( targetNs == null || targetNs.isEmpty() ) {
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
            LOG.debug( "Exporting ft " + ft.getName() );
            export( writer, ft );
        }

        // export custom simple type declarations
        for ( XSSimpleTypeDefinition xsSimpleType : customSimpleTypes.values() ) {
            export( writer, xsSimpleType );
        }

        int index = 0;
        while ( index < separateTypes.size() ) {
            XSTypeDefinition xsTypeDef = separateTypes.get( index );
            LOG.debug( "Exporting type " + xsTypeDef );
            exportType( writer, xsTypeDef, targetPrefix, targetNs );
            index++;
        }

        // end 'xs:schema'
        writer.writeEndElement();
    }

    /**
     * @param writer
     * @param type
     * @param targetPrefix
     * @param targetNs
     * @throws XMLStreamException
     */
    private void exportComplexType( XMLStreamWriter writer, XSComplexTypeDefinition complex, String targetPrefix,
                                    String targetNs )
                            throws XMLStreamException {
        if ( complex.getNamespace().equals( targetNs ) ) {
            writer.writeStartElement( XSNS, "complexType" );
            if ( !complex.getAnonymous() ) {
                writer.writeAttribute( "name", complex.getName() );
            }

            boolean contentTypeBegin = false;
            short contentType = complex.getContentType();
            switch ( contentType ) {
            case CONTENTTYPE_SIMPLE:
                writer.writeStartElement( "xs", "simpleContent", XSNS );
                contentTypeBegin = true;
                break;
            case CONTENTTYPE_MIXED:
                writer.writeStartElement( "xs", "complexContent", XSNS );
                contentTypeBegin = true;
                break;
            case CONTENTTYPE_EMPTY:
                break;
            case CONTENTTYPE_ELEMENT:
                writer.writeStartElement( "xs", "complexContent", XSNS );
                contentTypeBegin = true;
                break;
            }

            boolean derivationBegin = false;
            short derivation = complex.getDerivationMethod();
            XSTypeDefinition base = complex.getBaseType();
            String prefix = determinePrefix( base.getNamespace(), targetNs, targetPrefix );
            switch ( derivation ) {
            case DERIVATION_EXTENSION:
                writer.writeStartElement( "xs", "extension", XSNS );
                writer.writeAttribute( "base", prefix + base.getName() );
                derivationBegin = true;
                break;
            case DERIVATION_LIST:
                // TODO?
                break;
            case DERIVATION_NONE:
                // TODO?
                break;
            case DERIVATION_RESTRICTION:
                writer.writeStartElement( "xs", "restriction", XSNS );
                writer.writeAttribute( "base", prefix + base.getName() );
                derivationBegin = true;
                break;
            case DERIVATION_SUBSTITUTION:
                // TODO?
                break;
            case DERIVATION_UNION:
                // TODO?
                break;
            }

            XSParticle particle = complex.getParticle();
            if ( particle != null ) {
                exportTerm( writer, particle.getTerm(), particle.getMinOccurs(), particle.getMaxOccurs(),
                            particle.getMaxOccursUnbounded(), targetPrefix, targetNs );
            }

            XSObjectList attributes = complex.getAttributeUses();
            for ( int i = 0; i < attributes.getLength(); i++ ) {
                XSAttributeUse attribute = ( (XSAttributeUse) attributes.item( i ) );
                writer.writeEmptyElement( "xs", "attribute", XSNS );
                writer.writeAttribute( "name", attribute.getAttrDeclaration().getName() );
                XSTypeDefinition type = attribute.getAttrDeclaration().getTypeDefinition();
                writer.writeAttribute( "type", "xs:" + type.getName() );
                writer.writeAttribute( "use", attribute.getRequired() ? "required" : "optional" );
            }

            if ( derivationBegin ) {
                writer.writeEndElement(); // extension, etc.
            }
            if ( contentTypeBegin ) {
                writer.writeEndElement(); // simpleContent or complexContent
            }
            writer.writeEndElement(); // complexType
        }
    }

    /**
     * @throws XMLStreamException
     * 
     */
    private void exportSimpleType( XMLStreamWriter writer, XSSimpleTypeDefinition simple, String targetPrefix,
                                   String targetNs )
                            throws XMLStreamException {
        if ( simple.getNamespace().equals( targetNs ) ) {

            writer.writeStartElement( XSNS, "simpleType" );
            writer.writeAttribute( "name", simple.getName() );

            // TODO how can one find the derivation type? getFinal() is wrong!
            writer.writeStartElement( "xs", "restriction", XSNS );

            String simpleNs = simple.getBaseType().getNamespace();
            String prefix = determinePrefix( simpleNs, targetNs, targetPrefix );

            writer.writeAttribute( "base", prefix + simple.getBaseType().getName() );
            StringList members = simple.getLexicalEnumeration();
            if ( members != null && members.getLength() > 0 ) {
                for ( int i = 0; i < members.getLength(); i++ ) {
                    writer.writeEmptyElement( "xs", "enumeration", XSNS );
                    writer.writeAttribute( "value", members.item( i ) );
                }
            }

            writer.writeEndElement(); // derivation (restriction, extension, etc.)
            writer.writeEndElement(); // simpleType
        }
    }

    private String determinePrefix( String ns, String targetNs, String targetPrefix ) {
        String prefix = ns == targetNs ? targetPrefix + ":" : "";
        prefix = ns == GMLNS ? GML_PREFIX + ":" : prefix;
        prefix = ns == XSNS ? XS_PREFIX + ":" : prefix;
        return prefix;
    }

    private void exportTerm( XMLStreamWriter writer, XSTerm term, int minOccurs, int maxOccurs, boolean maxUnbounded,
                             String targetPrefix, String targetNs )
                            throws XMLStreamException {
        if ( term instanceof XSModelGroup ) {
            XSModelGroup modelGroup = (XSModelGroup) term;
            if ( modelGroup.getCompositor() == COMPOSITOR_SEQUENCE ) {
                writer.writeStartElement( "xs", "sequence", XSNS );
            }
            if ( modelGroup.getCompositor() == COMPOSITOR_CHOICE ) {
                writer.writeStartElement( "xs", "choice", XSNS );
            }
            if ( modelGroup.getCompositor() == COMPOSITOR_ALL ) {
                writer.writeStartElement( "xs", "all", XSNS );
            }

            XSObjectList particles = modelGroup.getParticles();
            for ( int i = 0; i < particles.getLength(); i++ ) {
                XSParticle particle = (XSParticle) particles.item( i );
                exportTerm( writer, particle.getTerm(), particle.getMinOccurs(), particle.getMaxOccurs(),
                            particle.getMaxOccursUnbounded(), targetPrefix, targetNs );
            }
            writer.writeEndElement();

        } else if ( term instanceof XSElementDeclaration ) {
            exportElement( writer, (XSElementDeclaration) term, minOccurs, maxOccurs, maxUnbounded, targetPrefix,
                           targetNs );
        } else if ( term instanceof XSWildcard ) {
            XSWildcard wildcard = (XSWildcard) term;
            // TODO
        }

    }

    private void exportElement( XMLStreamWriter writer, XSElementDeclaration element, int minOccurs, int maxOccurs,
                                boolean maxUnbounded, String targetPrefix, String targetNs )
                            throws XMLStreamException {
        LOG.debug( "Exporting Element " + element.getNamespace() + "/" + element.getName() );
        writer.writeStartElement( "xs", "element", XSNS );
        writer.writeAttribute( "name", element.getName() );
        if ( minOccurs != 1 ) {
            writer.writeAttribute( "minOccurs", String.valueOf( minOccurs ) );
        }
        if ( maxUnbounded ) {
            writer.writeAttribute( "maxOccurs", "unbounded" );
        } else if ( maxOccurs != 1 ) {
            writer.writeAttribute( "maxOccurs", String.valueOf( maxOccurs ) );
        }

        XSTypeDefinition type = element.getTypeDefinition();
        if ( type.getNamespace().equals( targetNs ) ) {
            if ( !type.getAnonymous() ) {
                writer.writeAttribute( "type", type.getName() );
                if ( !separateTypes.contains( type ) ) {
                    separateTypes.add( type );
                }
            } else {
                exportType( writer, type, targetPrefix, targetNs );
            }
        } else {
            String prefix = determinePrefix( type.getNamespace(), targetNs, targetPrefix );
            writer.writeAttribute( "type", prefix + type.getName() );
        }

        writer.writeEndElement(); // xs:element
    }

    /**
     * @param typeDefinition
     * @throws XMLStreamException
     */
    private void exportType( XMLStreamWriter writer, XSTypeDefinition type, String targetPrefix, String targetNs )
                            throws XMLStreamException {
        short typeCat = type.getTypeCategory();
        if ( typeCat == SIMPLE_TYPE ) {
            exportSimpleType( writer, (XSSimpleTypeDefinition) type, targetPrefix, targetNs );

        } else if ( typeCat == COMPLEX_TYPE ) {
            exportComplexType( writer, (XSComplexTypeDefinition) type, targetPrefix, targetNs );
        }

    }

    private void export( XMLStreamWriter writer, FeatureType ft )
                            throws XMLStreamException {

        if ( exportedFts.contains( ft.getName() ) ) {
            return;
        }

        ApplicationSchema schema = ft.getSchema();
        FeatureType parentFt = null;
        boolean hasSubTypes = false;
        if ( schema != null ) {
            parentFt = schema.getParentFt( ft );
            if ( parentFt != null ) {
                export( writer, parentFt );
            }
        }

        if ( schema != null ) {
            hasSubTypes = schema.getDirectSubtypes( ft ).length > 0;
        }

        writer.writeStartElement( XSNS, "element" );
        // TODO (what about features in other namespaces???)
        writer.writeAttribute( "name", ft.getName().getLocalPart() );

        if ( hasSubTypes ) {
            writer.writeAttribute( "type", "app:" + ft.getName().getLocalPart() + "Type" );
        }

        if ( ft.isAbstract() ) {
            writer.writeAttribute( "abstract", "true" );
        }
        if ( parentFt != null ) {
            writer.writeAttribute( "substitutionGroup", "app:" + parentFt.getName().getLocalPart() );
        } else {
            writer.writeAttribute( "substitutionGroup", abstractGMLFeatureElement );
        }
        // end 'xs:element'
        if ( hasSubTypes ) {
            writer.writeEndElement();
        }

        writer.writeStartElement( XSNS, "complexType" );
        if ( hasSubTypes ) {
            writer.writeAttribute( "name", ft.getName().getLocalPart() + "Type" );
        }
        if ( ft.isAbstract() && hasSubTypes ) {
            writer.writeAttribute( "abstract", "true" );
        }
        writer.writeStartElement( XSNS, "complexContent" );
        writer.writeStartElement( XSNS, "extension" );

        if ( parentFt != null ) {
            writer.writeAttribute( "base", "app:" + parentFt.getName().getLocalPart() + "Type" );
        } else {
            writer.writeAttribute( "base", "gml:AbstractFeatureType" );
        }

        writer.writeStartElement( XSNS, "sequence" );

        // TODO check for GML 2 properties (gml:pointProperty, ...) and export as "app:gml2PointProperty" for GML 3

        // export property definitions (only for non-GML ones)
        if ( schema != null ) {
            for ( PropertyType pt : schema.getNewPropertyDeclarations( ft ) ) {
                if ( pt == null ) {
                    LOG.warn( "Property type null inside " + ft.getName() );
                    continue;
                }
                LOG.debug( "Exporting property type " + pt );
                export( writer, pt, version );
            }
        }

        // end 'xs:sequence'
        writer.writeEndElement();
        // end 'xs:extension'
        writer.writeEndElement();
        // end 'xs:complexContent'
        writer.writeEndElement();
        // end 'xs:complexType'
        writer.writeEndElement();

        if ( !hasSubTypes ) {
            // end 'xs:element'
            writer.writeEndElement();
        }

        exportedFts.add( ft.getName() );
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
            export( writer, (SimplePropertyType) pt, version );
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
                if ( containedFt.getPrefix() != null ) {
                    writer.setPrefix( containedFt.getPrefix(), containedFt.getNamespaceURI() );
                    writer.writeAttribute( "ref", containedFt.getPrefix() + ":" + containedFt.getLocalPart() );
                } else {
                    writer.writeAttribute( "ref", "app:" + containedFt.getLocalPart() );
                }
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
            if ( version.equals( GML_2 ) ) {
                LOG.warn( "Exporting CodePropertyType as GML2 schema: narrowing down to xs:string." );
                writer.writeAttribute( "type", "xs:string" );
            } else {
                writer.writeAttribute( "type", "gml:CodeType" );
            }
        } else if ( pt instanceof MeasurePropertyType ) {
            if ( version.equals( GML_2 ) ) {
                LOG.warn( "Exporting MeasurePropertyType as GML2 schema: narrowing to xs:string." );
                writer.writeAttribute( "type", "xs:string" );
            } else {
                writer.writeAttribute( "type", "gml:MeasureType" );
            }
        } else if ( pt instanceof CustomPropertyType ) {
            XSTypeDefinition xsTypeDef = ( (CustomPropertyType) pt ).getXSDValueType();
            if ( xsTypeDef == null ) {
                LOG.warn( "Type definition null inside " + pt.getName() + " property type." );
            } else {

                if ( !xsTypeDef.getAnonymous() ) {
                    if ( !separateTypes.contains( xsTypeDef ) ) {
                        separateTypes.add( xsTypeDef );
                    }
                    writer.writeAttribute( "type", xsTypeDef.getName() );
                } else {
                    LOG.debug( "Exporting anonymous Type " + xsTypeDef );
                    exportType( writer, xsTypeDef, pt.getName().getPrefix(), pt.getName().getNamespaceURI() );
                }
            }
        }

        writer.writeEndElement(); // end 'xs:element'
    }

    private void export( XMLStreamWriter writer, SimplePropertyType pt, GMLVersion version )
                            throws XMLStreamException {

        XSSimpleTypeDefinition xsdType = pt.getXSDType();
        if ( xsdType == null ) {
            // export without XML schema information
            PrimitiveType type = pt.getPrimitiveType();
            writer.writeAttribute( "type", "xs:" + getSimpleType( type ) );
        } else {
            // reconstruct XML schema type definition
            String name = xsdType.getName();
            String ns = xsdType.getNamespace();
            if ( xsdType.getName() != null ) {
                if ( XSNS.equals( ns ) ) {
                    writer.writeAttribute( "type", "xs:" + name );
                } else {
                    // TODO handle other namespaces
                    writer.writeAttribute( "type", "app:" + name );
                    while ( xsdType != null && !XSNS.equals( xsdType.getNamespace() ) ) {
                        name = xsdType.getName();
                        ns = xsdType.getNamespace();
                        QName qName = new QName( ns, name );
                        if ( !customSimpleTypes.containsKey( qName ) ) {
                            customSimpleTypes.put( qName, xsdType );
                        }
                        xsdType = (XSSimpleTypeDefinition) xsdType.getBaseType();
                    }
                }
            } else {
                // unnamed simple property
                writer.writeStartElement( "xs", "simpleType", XSNS );
                writer.writeStartElement( "xs", "restriction", XSNS );
                writer.writeAttribute( "base", "xs:" + getSimpleType( pt.getPrimitiveType() ) );

                XSObjectList facets = pt.getXSDType().getFacets();
                for ( int i = 0; i < facets.getLength(); i++ ) {
                    XSFacet facet = (XSFacet) facets.item( i );
                    writer.writeEmptyElement( "xs", getFacetName( facet.getFacetKind() ), XSNS );
                    writer.writeAttribute( "value", facet.getLexicalFacetValue() );
                }

                writer.writeEndElement();
                writer.writeEndElement();
            }
        }
    }

    private String getFacetName( short facetKind ) {
        String facetName = null;
        switch ( facetKind ) {
        case FACET_ENUMERATION:
            facetName = "enumeration";
            break;
        case FACET_FRACTIONDIGITS:
            facetName = "fractionDigits";
            break;
        case FACET_LENGTH:
            facetName = "length";
            break;
        case FACET_MAXEXCLUSIVE:
            facetName = "maxExclusive";
            break;
        case FACET_MAXINCLUSIVE:
            facetName = "maxInclusive";
            break;
        case FACET_MAXLENGTH:
            facetName = "maxLength";
            break;
        case FACET_MINEXCLUSIVE:
            facetName = "minExclusive";
            break;
        case FACET_MININCLUSIVE:
            facetName = "minInclusive";
            break;
        case FACET_MINLENGTH:
            facetName = "minLength";
            break;
        case FACET_NONE:
            facetName = "none";
            break;
        case FACET_PATTERN:
            facetName = "pattern";
            break;
        case FACET_TOTALDIGITS:
            facetName = "totalDigits";
            break;
        case FACET_WHITESPACE:
            facetName = "whiteSpace";
            break;
        }
        return facetName;
    }

    private String getSimpleType( PrimitiveType type ) {
        String typeFound = null;
        switch ( type ) {
        case BOOLEAN:
            typeFound = "boolean";
            break;
        case DATE:
            typeFound = "date";
            break;
        case DATE_TIME:
            typeFound = "dateTime";
            break;
        case DECIMAL:
            typeFound = "decimal";
            break;
        case DOUBLE:
            typeFound = "double";
            break;
        case INTEGER:
            typeFound = "integer";
            break;
        case STRING:
            typeFound = "string";
            break;
        case TIME:
            typeFound = "time";
            break;
        }
        return typeFound;
    }

    private void export( XMLStreamWriter writer, XSSimpleTypeDefinition xsSimpleType )
                            throws XMLStreamException {

        writer.writeStartElement( XSNS, "simpleType" );
        writer.writeAttribute( "name", xsSimpleType.getName() );
        writer.writeStartElement( XSNS, "restriction" );

        String baseTypeName = xsSimpleType.getBaseType().getName();
        String baseTypeNs = xsSimpleType.getBaseType().getNamespace();
        if ( baseTypeName == null || xsSimpleType.getLexicalEnumeration().getLength() == 0 ) {
            LOG.warn( "Custom simple type '{" + xsSimpleType.getNamespace() + "}" + xsSimpleType.getName()
                      + "' is based on unnamed type. Defaulting to xs:string." );
            writer.writeAttribute( "base", "xs:string" );
        } else if ( XSNS.equals( baseTypeNs ) ) {
            writer.writeAttribute( "base", "xs:" + baseTypeName );
        } else {
            writer.writeAttribute( "base", "app:" + baseTypeName );
        }
        StringList enumValues = xsSimpleType.getLexicalEnumeration();
        for ( int i = 0; i < enumValues.getLength(); i++ ) {
            writer.writeEmptyElement( XSNS, "enumeration" );
            writer.writeAttribute( "value", enumValues.item( i ) );
        }
        writer.writeEndElement();
        writer.writeEndElement();
    }
}
