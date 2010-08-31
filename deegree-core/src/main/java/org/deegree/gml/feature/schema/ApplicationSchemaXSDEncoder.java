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
import static org.apache.xerces.xs.XSConstants.ELEMENT_DECLARATION;
import static org.apache.xerces.xs.XSConstants.SCOPE_GLOBAL;
import static org.apache.xerces.xs.XSConstants.TYPE_DEFINITION;
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
import static org.deegree.commons.xml.CommonNamespaces.XMLNS;
import static org.deegree.commons.xml.CommonNamespaces.XSNS;
import static org.deegree.commons.xml.CommonNamespaces.XS_PREFIX;
import static org.deegree.gml.GMLVersion.GML_2;

import java.util.HashMap;
import java.util.HashSet;
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
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSNamespaceItemList;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSWildcard;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureCollectionType;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLSchemaAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stream-based writer for exporting {@link FeatureType} instances as GML application schemas.
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

    private final String targetNs;

    private String gmlNsURI;

    private final Map<String, String> importURLs;

    private final Map<String, String> prefixesToNs = new HashMap<String, String>();

    private final Map<String, String> nsToPrefix = new HashMap<String, String>();

    // set to "gml:_Feature" (GML 2 and 3.1) or "gml:AbstractFeatureType" (GML 3.2)
    private String abstractGMLFeatureElement;

    // set to "gml:_FeatureCollection" (GML 2 and 3.1) or null (GML 3.2)
    private String abstractGMLFeatureCollectionElement;

    // set to "gml:FeatureAssociationType" (GML 2) or "gml:FeaturePropertyType" (GML 3.1 / GML 3.2)
    private String featurePropertyType;

    // keeps track of already exported (global) xs:elements declarations
    private Set<String> exportedElements = new HashSet<String>();

    // keeps track of already exported (global) xs:simpleType/xs:complexType definitions
    private Set<String> exportedTypes = new HashSet<String>();

    /**
     * Creates a new {@link ApplicationSchemaXSDEncoder} for the given GML version and optional import URL.
     * 
     * @param version
     *            gml version that exported schemas will comply to, must not be <code>null</code>
     * @param targetNamespace
     *            target namespace for the schema document, may be <code>null/<code>
     * @param importURLs
     *            to be imported in the generated schema document, this may also contain a URL for the gml namespace,
     *            may be <code>null</code>
     * @param prefixToNs
     *            keys: namespace prefixes, values: namespaces, may be <code>null</code>
     */
    public ApplicationSchemaXSDEncoder( GMLVersion version, String targetNamespace, Map<String, String> importURLs,
                                        Map<String, String> prefixToNs ) {

        this.version = version;
        this.targetNs = targetNamespace;
        if ( importURLs == null ) {
            this.importURLs = new HashMap<String, String>();
        } else {
            this.importURLs = new HashMap<String, String>( importURLs );
        }
        switch ( version ) {
        case GML_2:
            gmlNsURI = GMLNS;
            abstractGMLFeatureElement = "gml:_Feature";
            abstractGMLFeatureCollectionElement = "gml:_FeatureCollection";
            featurePropertyType = "gml:FeatureAssociationType";
            if ( !this.importURLs.containsKey( gmlNsURI ) ) {
                this.importURLs.put( gmlNsURI, GML_2_DEFAULT_INCLUDE );
            }
            break;
        case GML_30:
            gmlNsURI = GMLNS;
            abstractGMLFeatureElement = "gml:_Feature";
            abstractGMLFeatureCollectionElement = "gml:_FeatureCollection";
            featurePropertyType = "gml:FeaturePropertyType";
            if ( !this.importURLs.containsKey( gmlNsURI ) ) {
                this.importURLs.put( gmlNsURI, GML_30_DEFAULT_INCLUDE );
            }
            break;
        case GML_31:
            gmlNsURI = GMLNS;
            abstractGMLFeatureElement = "gml:_Feature";
            abstractGMLFeatureCollectionElement = "gml:_FeatureCollection";
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

        if ( prefixToNs != null ) {
            for ( String prefix : prefixToNs.keySet() ) {
                String ns = prefixToNs.get( prefix );
                addNsBinding( prefix, ns );
            }
        }
        addNsBinding( "gml", gmlNsURI );
        addNsBinding( "xs", XSNS );
        addNsBinding( "xml", XMLNS );

        // special treatment needed for GML namespaces
        nsToPrefix.put( GMLNS, "gml" );
        nsToPrefix.put( GML3_2_NS, "gml" );
        

        // TODO get rid of these CITE hacks
        exportedElements.add( "SimpleFeatureCollection" );
        exportedTypes.add( "SimpleFeatureCollectionType" );
    }

    private void addNsBinding( String prefix, String ns ) {
        this.prefixesToNs.put( prefix, ns );
        this.nsToPrefix.put( ns, prefix );
    }

    public void export( XMLStreamWriter writer, ApplicationSchema schema )
                            throws XMLStreamException {
        export( writer, schema.getFeatureTypes( null, true, true ) );
    }

    /**
     * Exports the given list of {@link FeatureType} instances as an XML schema document.
     * <p>
     * NOTE: The given writer must be configured to repair namespaces.
     * </p>
     * 
     * @param writer
     * @param fts
     * @throws XMLStreamException
     */
    public void export( XMLStreamWriter writer, List<FeatureType> fts )
                            throws XMLStreamException {

        for ( String prefix : prefixesToNs.keySet() ) {
            String ns = prefixesToNs.get( prefix );
            writer.setPrefix( prefix, ns );
        }

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

        // if ( targetNs == null || targetNs.isEmpty() ) {
        // writer.writeStartElement( XSNS, "element" );
        // writer.writeAttribute( "name", "FeatureCollection" );
        // writer.writeAttribute( "substitutionGroup", "gml:_FeatureCollection" );
        // writer.writeAttribute( "type", "FeatureCollectionType" );
        // writer.writeEndElement();
        // writer.writeStartElement( XSNS, "complexType" );
        // writer.writeAttribute( "name", "FeatureCollectionType" );
        // writer.writeStartElement( XSNS, "complexContent" );
        // writer.writeStartElement( XSNS, "extension" );
        // writer.writeAttribute( "base", "gml:AbstractFeatureCollectionType" );
        // writer.writeStartElement( XSNS, "sequence" );
        // writer.writeEndElement();
        // writer.writeEndElement();
        // writer.writeEndElement();
        // writer.writeEndElement();
        // }

        Set<ApplicationSchema> schemas = new HashSet<ApplicationSchema>();

        // export feature type declarations (in the target namespace)
        for ( FeatureType ft : fts ) {
            schemas.add( ft.getSchema() );
            if ( ft.getName().getNamespaceURI().equals( targetNs ) ) {
                export( writer, ft );
            }
        }

        // export element declarations and type declarations (in the target namespace) that are not feature type related
        try {
            for ( ApplicationSchema schema : schemas ) {
                GMLSchemaAnalyzer analyzer = schema.getXSModel();
                if ( analyzer != null ) {
                    XSModel xsModel = analyzer.getXSModel();
                    XSNamespaceItemList nsItems = xsModel.getNamespaceItems();
                    for ( int i = 0; i < nsItems.getLength(); i++ ) {
                        XSNamespaceItem nsItem = nsItems.item( i );
                        String ns = nsItem.getSchemaNamespace();
                        if ( ns.equals( targetNs ) ) {

                            XSNamedMap elements = nsItem.getComponents( ELEMENT_DECLARATION );
                            for ( int j = 0; j < elements.getLength(); j++ ) {
                                XSElementDeclaration xsElement = (XSElementDeclaration) elements.item( j );
                                if ( !exportedElements.contains( xsElement.getName() ) ) {
                                    exportElement( writer, xsElement, 1, 1, false );
                                }
                            }

                            XSNamedMap types = nsItem.getComponents( TYPE_DEFINITION );
                            for ( int j = 0; j < types.getLength(); j++ ) {
                                XSTypeDefinition xsType = (XSTypeDefinition) types.item( j );
                                // TODO remove hacky way
                                String localName = xsType.getName();
                                if ( !exportedTypes.contains( localName ) ) {
                                    if ( !exportedTypes.contains( xsType.getName() ) ) {
                                        exportType( writer, xsType );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch ( Throwable t ) {
            t.printStackTrace();
        }

        // end 'xs:schema'
        writer.writeEndElement();
    }

    private void export( XMLStreamWriter writer, FeatureType ft )
                            throws XMLStreamException {

        if ( exportedElements.contains( ft.getName().getLocalPart() ) ) {
            return;
        }

        exportedElements.add( ft.getName().getLocalPart() );

        // TODO find a better way to do prevent re-exporting of the type
        exportedTypes.add( ft.getName().getLocalPart() + "Type" );

        // export parent feature types
        boolean hasSubTypes = false;
        ApplicationSchema schema = ft.getSchema();
        FeatureType parentFt = null;
        if ( schema != null ) {
            hasSubTypes = schema.getDirectSubtypes( ft ).length > 0;
            parentFt = schema.getParentFt( ft );
            if ( parentFt != null ) {
                export( writer, parentFt );
            }
        }

        LOG.debug( "Exporting feature type declaration: " + ft.getName() );
        writer.writeStartElement( XSNS, "element" );
        writer.writeAttribute( "name", ft.getName().getLocalPart() );

        // export type name
        QName typeName = getTypeName( ft, hasSubTypes );
        if ( typeName != null ) {
            String prefix = getPrefix( targetNs );
            writer.writeAttribute( "type", prefix + ":" + ft.getName().getLocalPart() + "Type" );
        }

        if ( ft.isAbstract() ) {
            writer.writeAttribute( "abstract", "true" );
        }

        if ( parentFt != null ) {
            String prefix = getPrefix( parentFt.getName().getNamespaceURI() );
            writer.writeAttribute( "substitutionGroup", prefix + ":" + parentFt.getName().getLocalPart() );
        } else {
            if ( ft instanceof FeatureCollectionType && abstractGMLFeatureCollectionElement != null ) {
                writer.writeAttribute( "substitutionGroup", abstractGMLFeatureCollectionElement );
            } else {
                writer.writeAttribute( "substitutionGroup", abstractGMLFeatureElement );
            }
        }

        // end 'xs:element' here if type will be exported separately
        if ( typeName != null ) {
            writer.writeEndElement();
        }

        // ai: not everytime one should write the complexType definition; need to check if the type extends another type
        // from the same namespace
        if ( typeName == null || typeName.getNamespaceURI().equals( targetNs ) ) {
            writer.writeStartElement( XSNS, "complexType" );
            if ( typeName != null ) {
                writer.writeAttribute( "name", typeName.getLocalPart() );
            }
            if ( ft.isAbstract() ) {
                writer.writeAttribute( "abstract", "true" );
            }

            writer.writeStartElement( XSNS, "complexContent" );
            writer.writeStartElement( XSNS, "extension" );

            if ( parentFt != null ) {
                String prefix = getPrefix( parentFt.getName().getNamespaceURI() );
                writer.writeAttribute( "base", prefix + ":" + parentFt.getName().getLocalPart() + "Type" );
            } else {
                if ( ft instanceof FeatureCollectionType && abstractGMLFeatureCollectionElement != null ) {
                    writer.writeAttribute( "base", "gml:AbstractFeatureCollectionType" );
                } else {
                    writer.writeAttribute( "base", "gml:AbstractFeatureType" );
                }
            }

            writer.writeStartElement( XSNS, "sequence" );

            // TODO check for GML 2-only properties (gml:pointProperty, ...) and export as "app:gml2PointProperty" for
            // GML 3

            // export property definitions (only for non-GML ones)
            if ( schema != null ) {
                for ( PropertyType pt : schema.getNewPropertyDeclarations( ft ) ) {
                    if ( pt == null ) {
                        LOG.warn( "Property type null inside " + ft.getName() );
                        continue;
                    }
                    LOG.debug( "Exporting property type " + pt );
                    export( writer, pt );
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
        }

        if ( typeName == null ) {
            // end 'xs:element'
            writer.writeEndElement();
        }
    }

    private QName getTypeName( FeatureType ft, boolean hasSubTypes ) {
        QName elName = ft.getName();
        QName typeName = null;
        GMLSchemaAnalyzer analyzer = ft.getSchema().getXSModel();
        if ( analyzer == null ) {
            if ( hasSubTypes ) {
                typeName = new QName( elName.getNamespaceURI(), elName.getLocalPart() + "Type" );
            }
        } else {
            XSElementDeclaration elDecl = analyzer.getXSModel().getElementDeclaration( elName.getLocalPart(),
                                                                                       elName.getNamespaceURI() );
            XSTypeDefinition typeDef = elDecl.getTypeDefinition();
            if ( !typeDef.getAnonymous() ) {
                typeName = new QName( typeDef.getNamespace(), typeDef.getName() );
            }
        }
        return typeName;
    }

    private void export( XMLStreamWriter writer, PropertyType pt )
                            throws XMLStreamException {

        LOG.debug( "Exporting property type " + pt.getName() );

        // TODO is the more to this decision?
        boolean byRef = !pt.getName().getNamespaceURI().equals( targetNs );

        if ( byRef ) {
            writer.writeEmptyElement( XSNS, "element" );
            String prefix = getPrefix( pt.getName().getNamespaceURI() );
            writer.writeAttribute( "ref", prefix + ":" + pt.getName().getLocalPart() );
        } else {
            writer.writeStartElement( XSNS, "element" );
            writer.writeAttribute( "name", pt.getName().getLocalPart() );
        }

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

        if ( pt.isNillable() ) {
            // TODO activate when parser is ready
            // writer.writeAttribute( "nillable", "true" );
        }

        if ( !byRef ) {
            if ( pt instanceof SimplePropertyType ) {
                export( writer, (SimplePropertyType) pt );
            } else if ( pt instanceof GeometryPropertyType ) {
                export( writer, (GeometryPropertyType) pt );
            } else if ( pt instanceof FeaturePropertyType ) {
                export( writer, (FeaturePropertyType) pt );
            } else if ( pt instanceof CodePropertyType ) {
                export( writer, (CodePropertyType) pt );
            } else if ( pt instanceof MeasurePropertyType ) {
                export( writer, (MeasurePropertyType) pt );
            } else if ( pt instanceof CustomPropertyType ) {
                export( writer, (CustomPropertyType) pt );
            } else {
                throw new RuntimeException( "Unhandled property type '" + pt.getClass() + "'" );
            }
            writer.writeEndElement(); // end 'xs:element'
        }
    }

    private void export( XMLStreamWriter writer, SimplePropertyType pt )
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
                    String prefix = getPrefix( targetNs );
                    writer.writeAttribute( "type", prefix + ":" + name );
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

    private void export( XMLStreamWriter writer, GeometryPropertyType pt )
                            throws XMLStreamException {
        writer.writeAttribute( "type", "gml:GeometryPropertyType" );
        writer.writeComment( "TODO: Export geometry type restriction" );
    }

    private void export( XMLStreamWriter writer, FeaturePropertyType pt )
                            throws XMLStreamException {

        QName containedFt = pt.getFTName();
        if ( containedFt != null ) {
            writer.writeStartElement( XSNS, "complexType" );

            // TODO what about schemas that disallow nesting of feature properties?
            writer.writeStartElement( XSNS, "sequence" );
            writer.writeEmptyElement( XSNS, "element" );
            String prefix = getPrefix( containedFt.getNamespaceURI() );
            writer.writeAttribute( "ref", prefix + ":" + containedFt.getLocalPart() );
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
    }

    private void export( XMLStreamWriter writer, CustomPropertyType pt )
                            throws XMLStreamException {

        XSComplexTypeDefinition xsTypeDef = pt.getXSDValueType();

        if ( xsTypeDef == null ) {
            LOG.warn( "Type definition null inside " + pt.getName() + " property type." );
        } else {
            if ( !xsTypeDef.getAnonymous() ) {
                String prefix = getPrefix( xsTypeDef.getNamespace() );
                writer.writeAttribute( "type", prefix + ":" + xsTypeDef.getName() );
            } else {
                LOG.debug( "Exporting anonymous type " + xsTypeDef );
                exportType( writer, xsTypeDef );
            }
        }
    }

    private void export( XMLStreamWriter writer, CodePropertyType pt )
                            throws XMLStreamException {
        if ( version.equals( GML_2 ) ) {
            LOG.warn( "Exporting CodePropertyType as GML2 schema: narrowing down to xs:string." );
            writer.writeAttribute( "type", "xs:string" );
        } else {
            writer.writeAttribute( "type", "gml:CodeType" );
        }
    }

    private void export( XMLStreamWriter writer, MeasurePropertyType pt )
                            throws XMLStreamException {
        if ( version.equals( GML_2 ) ) {
            LOG.warn( "Exporting MeasurePropertyType as GML2 schema: narrowing to xs:string." );
            writer.writeAttribute( "type", "xs:string" );
        } else {
            writer.writeAttribute( "type", "gml:MeasureType" );
        }
    }

    private void exportType( XMLStreamWriter writer, XSTypeDefinition type )
                            throws XMLStreamException {
        short typeCat = type.getTypeCategory();
        if ( typeCat == SIMPLE_TYPE ) {
            exportSimpleType( writer, (XSSimpleTypeDefinition) type );
        } else if ( typeCat == COMPLEX_TYPE ) {
            exportComplexType( writer, (XSComplexTypeDefinition) type );
        }
    }

    private void exportSimpleType( XMLStreamWriter writer, XSSimpleTypeDefinition simple )
                            throws XMLStreamException {

        writer.writeStartElement( XSNS, "simpleType" );
        if ( !simple.getAnonymous() ) {
            writer.writeAttribute( "name", simple.getName() );
        }

        // TODO how can one find the derivation type? getFinal() is wrong!
        LOG.debug( "Exporting a simple type is done always by restriction. Other derivations may be possible?!" );
        writer.writeStartElement( "xs", "restriction", XSNS );

        String prefix = getPrefix( simple.getBaseType().getNamespace() );
        writer.writeAttribute( "base", prefix + ":" + simple.getBaseType().getName() );
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

    private void exportComplexType( XMLStreamWriter writer, XSComplexTypeDefinition complex )
                            throws XMLStreamException {

        LOG.debug( "Exporting complex type, name: " + complex.getName() );

        writer.writeStartElement( XSNS, "complexType" );
        if ( !complex.getAnonymous() ) {
            writer.writeAttribute( "name", complex.getName() );
        }

        boolean contentTypeBegin = false;
        short contentType = complex.getContentType();

        short derivation = complex.getDerivationMethod();
        XSTypeDefinition base = complex.getBaseType();

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
            // TODO check if non-redundant restriction / extension is performed (in that case, complexContent
            // container element is required)
            if ( base != null && !base.getName().equals( "anyType" ) ) {
                writer.writeStartElement( "xs", "complexContent", XSNS );
                contentTypeBegin = true;
            }
            break;
        }

        boolean derivationBegin = false;
        String prefix = getPrefix( base.getNamespace() );
        switch ( derivation ) {
        case DERIVATION_EXTENSION:
            if ( !contentTypeBegin ) {
                writer.writeStartElement( "xs", "complexContent", XSNS );
            }
            contentTypeBegin = true;
            writer.writeStartElement( "xs", "extension", XSNS );
            writer.writeAttribute( "base", prefix + ":" + base.getName() );
            derivationBegin = true;
            break;
        case DERIVATION_LIST:
            LOG.warn( "Exporting derivation by list is not implemented. Occured for complex element "
                      + complex.getName() );
            break;
        case DERIVATION_NONE:
            // nothing to do, handled above
            break;
        case DERIVATION_RESTRICTION:
            if ( !base.getName().equals( "anyType" ) ) {
                if ( !contentTypeBegin ) {
                    writer.writeStartElement( "xs", "complexContent", XSNS );
                }
                contentTypeBegin = true;
                writer.writeStartElement( "xs", "restriction", XSNS );
                writer.writeAttribute( "base", prefix + base.getName() );
                derivationBegin = true;
            }
            break;
        case DERIVATION_SUBSTITUTION:
            LOG.warn( "Exporting derivation by substitution is not implemented. Occured for complex element "
                      + complex.getName() );
            break;
        case DERIVATION_UNION:
            LOG.warn( "Exporting derivation by union is not implemented. Occured for complex element "
                      + complex.getName() );
            break;
        }

        XSParticle particle = complex.getParticle();
        if ( particle != null ) {
            exportTerm( writer, particle.getTerm(), particle.getMinOccurs(), particle.getMaxOccurs(),
                        particle.getMaxOccursUnbounded() );
        }

        // TODO only export attribute uses that are different from super types
        XSObjectList attributes = complex.getAttributeUses();
        for ( int i = 0; i < attributes.getLength(); i++ ) {
            XSAttributeUse attribute = ( (XSAttributeUse) attributes.item( i ) );
            writer.writeEmptyElement( "xs", "attribute", XSNS );
            writer.writeAttribute( "name", attribute.getAttrDeclaration().getName() );
            XSTypeDefinition type = attribute.getAttrDeclaration().getTypeDefinition();

            String xsTypeName = type.getName();
            if ( xsTypeName == null ) {
                LOG.warn( "Exporting of anonymous attribute types not implemented -- defaulting to xs:string." );
                xsTypeName = "string";
            }

            writer.writeAttribute( "type", "xs:" + xsTypeName );
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

    private void exportTerm( XMLStreamWriter writer, XSTerm term, int minOccurs, int maxOccurs, boolean maxUnbounded )
                            throws XMLStreamException {

        if ( term instanceof XSModelGroup ) {
            XSModelGroup modelGroup = (XSModelGroup) term;
            if ( modelGroup.getCompositor() == COMPOSITOR_SEQUENCE ) {
                writer.writeStartElement( "xs", "sequence", XSNS );
            } else if ( modelGroup.getCompositor() == COMPOSITOR_CHOICE ) {
                writer.writeStartElement( "xs", "choice", XSNS );
            } else if ( modelGroup.getCompositor() == COMPOSITOR_ALL ) {
                writer.writeStartElement( "xs", "all", XSNS );
            }

            if ( minOccurs != 1 ) {
                writer.writeAttribute( "minOccurs", String.valueOf( minOccurs ) );
            }
            if ( maxUnbounded ) {
                writer.writeAttribute( "maxOccurs", "unbounded" );
            } else if ( maxOccurs != 1 ) {
                writer.writeAttribute( "maxOccurs", String.valueOf( maxOccurs ) );
            }

            XSObjectList particles = modelGroup.getParticles();
            for ( int i = 0; i < particles.getLength(); i++ ) {
                XSParticle particle = (XSParticle) particles.item( i );
                exportTerm( writer, particle.getTerm(), particle.getMinOccurs(), particle.getMaxOccurs(),
                            particle.getMaxOccursUnbounded() );
            }
            writer.writeEndElement();

        } else if ( term instanceof XSElementDeclaration ) {
            XSElementDeclaration elem = (XSElementDeclaration) term;
            if ( elem.getScope() == SCOPE_GLOBAL ) {
                writer.writeEmptyElement( "xs", "element", XSNS );
                String prefix = getPrefix( elem.getNamespace() );
                writer.writeAttribute( "ref", prefix + ":" + elem.getName() );
                if ( minOccurs != 1 ) {
                    writer.writeAttribute( "minOccurs", String.valueOf( minOccurs ) );
                }
                if ( maxUnbounded ) {
                    writer.writeAttribute( "maxOccurs", "unbounded" );
                } else if ( maxOccurs != 1 ) {
                    writer.writeAttribute( "maxOccurs", String.valueOf( maxOccurs ) );
                }
            } else {
                exportElement( writer, (XSElementDeclaration) term, minOccurs, maxOccurs, maxUnbounded );
            }
        } else if ( term instanceof XSWildcard ) {
            XSWildcard wildcard = (XSWildcard) term;
            LOG.warn( "Exporting of wildcards not implemented yet." );
            // TODO
        }
    }

    private void exportElement( XMLStreamWriter writer, XSElementDeclaration element, int minOccurs, int maxOccurs,
                                boolean maxUnbounded )
                            throws XMLStreamException {

        LOG.debug( "Exporting generic element " + element.getNamespace() + "/" + element.getName() );

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
        if ( !type.getAnonymous() ) {
            String prefix = getPrefix( type.getNamespace() );
            writer.writeAttribute( "type", prefix + ":" + type.getName() );
        } else {
            exportType( writer, type );
        }
        writer.writeEndElement(); // xs:element
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
        default: {
            LOG.warn( "Schema type for simple type not found. Defaulting to string." );
            typeFound = "string";
        }
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
        } else {
            String prefix = getPrefix( baseTypeNs );
            writer.writeAttribute( "base", prefix + ":" + baseTypeName );
        }
        StringList enumValues = xsSimpleType.getLexicalEnumeration();
        for ( int i = 0; i < enumValues.getLength(); i++ ) {
            writer.writeEmptyElement( XSNS, "enumeration" );
            writer.writeAttribute( "value", enumValues.item( i ) );
        }
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private String getPrefix( String namespace ) {

        String prefix = nsToPrefix.get( namespace );
        if ( prefix == null ) {
            LOG.warn( "No prefix for namespace '" + namespace + "' defined." );
            return "app";
        }
        return prefix;
    }
}
