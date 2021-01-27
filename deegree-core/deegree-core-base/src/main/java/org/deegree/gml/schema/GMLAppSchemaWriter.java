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
package org.deegree.gml.schema;

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
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.URITranslator;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureCollectionType;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
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
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSNS;
import static org.deegree.commons.xml.schema.SchemaUtils.copy;
import static org.deegree.commons.xml.schema.SchemaUtils.writeWrapperDoc;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.gml.GMLVersion.GML_30;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.gml.schema.GMLSchemaInfoSet.isGMLNamespace;

/**
 * Stream-based writer for exporting {@link AppSchema} or {@link FeatureType} instances as a GML application schema.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GMLAppSchemaWriter {

    private static final Logger LOG = LoggerFactory.getLogger( GMLAppSchemaWriter.class );

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

    // set to "gml:_Feature" (GML 2, 3.0 and 3.1) or "gml:AbstractFeature" (GML 3.2)
    private String abstractGMLFeatureElement;

    // set to "gml:_FeatureCollection" (GML 2, 3.0 and 3.1) or null (GML 3.2)
    private String abstractGMLFeatureCollectionElement;

    // set to "gml:FeatureAssociationType" (GML 2) or "gml:FeaturePropertyType" (GML 3.0, 3.1 and 3.2)
    private String featurePropertyType;

    // keeps track of already exported (global) xs:elements declarations
    private final Set<String> exportedElements = new HashSet<String>();

    // keeps track of already exported (global) xs:simpleType/xs:complexType definitions
    private final Set<String> exportedTypes = new HashSet<String>();

    /**
     * Creates a new {@link GMLAppSchemaWriter} for the given GML version and optional import URL.
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
    public GMLAppSchemaWriter( GMLVersion version, String targetNamespace, Map<String, String> importURLs,
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
        addNsBinding( DEFAULT_NS_PREFIX, XSNS );
        addNsBinding( "gml", gmlNsURI );
        addNsBinding( "xlink", XLNNS );

        // special treatment needed for GML namespaces
        nsToPrefix.put( GMLNS, "gml" );
        nsToPrefix.put( GML3_2_NS, "gml" );

        // TODO remove CITE 1.0.0 hack
        exportedTypes.add( "DataFeatureCollectionType" );
    }

    private void addNsBinding( String prefix, String ns ) {
        this.prefixesToNs.put( prefix, ns );
        this.nsToPrefix.put( ns, prefix );
    }

    /**
     * Exports a wrapper schema document for the given XML Schema Infoset.
     *
     * @param writer
     * @param xsModel
     * @param targetNs
     * @param translator
     * @throws XMLStreamException
     */
    public static void export( XMLStreamWriter writer, GMLSchemaInfoSet xsModel, String targetNs,
                               URITranslator translator )
                    throws XMLStreamException, IOException {
        Set<String> schemaLocations = new HashSet<>();
        List<Pair<String, String>> nsImports = new ArrayList<Pair<String, String>>();
        for ( String ns : xsModel.getAppNamespaces() ) {
            List<String> locations = xsModel.getComponentLocations( ns );
            for ( String location : locations ) {
                if ( !location.startsWith( "http" ) )
                    schemaLocations.add( location );
                String translated = translator.translate( location );
                nsImports.add( new Pair<String, String>( ns, translated ) );
            }
        }

        if ( schemaLocations.size() == 1 ) {
            try {
                copyOriginalSchema( writer, schemaLocations );
            } catch ( URISyntaxException e ) {
                // fallback
                writeWrapperDoc( writer, targetNs, nsImports );
            }
        } else {
            writeWrapperDoc( writer, targetNs, nsImports );
        }
    }

    private static void copyOriginalSchema( XMLStreamWriter writer, Set<String> schemaLocations )
                    throws IOException, XMLStreamException, URISyntaxException {
        XMLStreamReader reader = null;
        try {
            String next = schemaLocations.iterator().next();
            File file = new File( new URI( next ) );
            InputStream schemaLocation = new FileInputStream( file );
            reader = XMLInputFactory.newFactory().createXMLStreamReader( schemaLocation );
            XMLStreamUtils.skipStartDocument( reader );
            copy( reader, writer );
        } finally {
            if ( reader != null )
                reader.close();
        }
    }

    public void export( XMLStreamWriter writer, AppSchema schema )
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

        writer.setDefaultNamespace( XSNS );
        writer.writeStartElement( XSNS, "schema" );
        writer.writeDefaultNamespace( XSNS );
        writer.writeNamespace( GML_PREFIX, gmlNsURI );
        for ( String prefix : prefixesToNs.keySet() ) {
            String ns = prefixesToNs.get( prefix );
            // avoid double writing of the namespace (required since IS_REPAIRING_NAMESPACE=FALSE)
            if ( !prefix.equals( GML_PREFIX ) && !prefix.equals( DEFAULT_NS_PREFIX ) ) {
                writer.writeNamespace( prefix, ns );
            }
        }

        if ( targetNs != null && !targetNs.isEmpty() ) {
            writer.writeAttribute( "targetNamespace", targetNs );
            writer.writeAttribute( "elementFormDefault", "qualified" );
            writer.writeAttribute( "attributeFormDefault", "unqualified" );
        } else {
            writer.writeAttribute( "elementFormDefault", "unqualified" );
            writer.writeAttribute( "attributeFormDefault", "unqualified" );
        }

        for ( String importNamespace : importURLs.keySet() ) {
            writer.writeEmptyElement( "import" );
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

        Set<AppSchema> schemas = new HashSet<AppSchema>();

        // export feature type declarations (in the target namespace)
        for ( FeatureType ft : fts ) {
            schemas.add( ft.getSchema() );
            if ( ft.getName().getNamespaceURI().equals( targetNs ) ) {
                export( writer, ft );
            }
        }

        // export element declarations and type declarations (in the target namespace) that are not feature type related
        try {
            for ( AppSchema schema : schemas ) {
                GMLSchemaInfoSet analyzer = schema.getGMLSchema();
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

        // export parent feature types
        AppSchema schema = ft.getSchema();
        FeatureType parentFt = null;
        if ( schema != null ) {
            parentFt = schema.getParent( ft );
            if ( parentFt != null ) {
                export( writer, parentFt );
            }
        }

        LOG.debug( "Exporting feature type declaration: " + ft.getName() );
        LOG.debug( "Parent: " + ft.getSchema().getParent( ft ) );
        writer.writeStartElement( "element" );
        writer.writeAttribute( "name", ft.getName().getLocalPart() );

        // export type name
        QName typeName = getXSTypeName( ft );
        if ( typeName != null ) {
            writer.writeAttribute( "type", getPrefixedName( typeName ) );
        }

        if ( ft.isAbstract() ) {
            writer.writeAttribute( "abstract", "true" );
        }

        if ( parentFt != null ) {
            writer.writeAttribute( "substitutionGroup", getPrefixedName( parentFt.getName() ) );
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

        if ( typeName == null || targetNs.equals( typeName.getNamespaceURI() ) ) {
            exportFeatureComplexType( writer, ft, parentFt, typeName == null ? null : typeName.getLocalPart() );
        }

        if ( typeName == null ) {
            // end 'xs:element'
            writer.writeEndElement();
        }
    }

    private void exportFeatureComplexType( XMLStreamWriter writer, FeatureType ft, FeatureType parentFt, String typeName )
                            throws XMLStreamException {

        if ( typeName != null ) {
            if ( exportedTypes.contains( typeName ) ) {
                return;
            }
            exportedTypes.add( typeName );
        }

        AppSchema schema = ft.getSchema();

        writer.writeStartElement( "complexType" );
        if ( typeName != null ) {
            writer.writeAttribute( "name", typeName );
        }
        if ( ft.isAbstract() ) {
            writer.writeAttribute( "abstract", "true" );
        }

        writer.writeStartElement( "complexContent" );
        writer.writeStartElement( "extension" );

        if ( parentFt != null ) {
            QName parentFtTypeDecl = getXSTypeName( parentFt );
            writer.writeAttribute( "base", getPrefixedName( parentFtTypeDecl ) );
        } else {
            if ( ft instanceof FeatureCollectionType && abstractGMLFeatureCollectionElement != null ) {
                writer.writeAttribute( "base", "gml:AbstractFeatureCollectionType" );
            } else {
                writer.writeAttribute( "base", "gml:AbstractFeatureType" );
            }
        }

        writer.writeStartElement( "sequence" );

        // TODO check for GML 2-only properties (gml:pointProperty, ...) and export as "app:gml2PointProperty" for
        // GML 3

        // export property definitions (only for non-GML ones)
        if ( schema != null ) {
            for ( PropertyType pt : schema.getNewPropertyDecls( ft ) ) {
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

    /**
     * Returns the XML schema type name to use for exporting the complex type for the given {@link FeatureType}.
     *
     * @param ft
     *            feature type, must not be <code>null</code>
     * @return the qualified complex type name or <code>null</code> if the type should be exported anonymously
     */
    private QName getXSTypeName( FeatureType ft ) {

        // export parent feature types
        boolean hasSubTypes = false;
        AppSchema schema = ft.getSchema();
        if ( schema != null ) {
            hasSubTypes = schema.getDirectSubtypes( ft ).length > 0;
        }

        QName elName = ft.getName();
        QName typeName = null;
        GMLSchemaInfoSet analyzer = ft.getSchema().getGMLSchema();
        if ( analyzer == null ) {
            if ( hasSubTypes ) {
                typeName = new QName( elName.getNamespaceURI(), elName.getLocalPart() + "Type" );
            }
        } else {
            XSElementDeclaration elDecl = analyzer.getXSModel().getElementDeclaration( elName.getLocalPart(),
                                                                                       elName.getNamespaceURI() );
            XSTypeDefinition typeDef = elDecl.getTypeDefinition();
            if ( !typeDef.getAnonymous() ) {
                if ( isGMLNamespace( typeDef.getNamespace() )
                     && ( typeDef.getName().equals( "AbstractFeatureType" ) || typeDef.getName().equals(
                                "AbstractFeatureCollectionType" ) ) ) {
                    if ( ft instanceof FeatureCollectionType && abstractGMLFeatureCollectionElement != null ) {
                        typeName = new QName( gmlNsURI, "AbstractFeatureCollectionType" );
                    } else {
                        typeName = new QName( gmlNsURI, "AbstractFeatureType" );
                    }
                }
                typeName = new QName( typeDef.getNamespace(), typeDef.getName() );
            }
        }
        return typeName;
    }

    private void export( XMLStreamWriter writer, PropertyType pt )
                            throws XMLStreamException {

        LOG.debug( "Exporting property type " + pt.getName() );

        // TODO is there more to this decision?
        boolean byRef = !pt.getName().getNamespaceURI().equals( targetNs );

        if ( byRef ) {
            writer.writeEmptyElement( "element" );
            writer.writeAttribute( "ref", getPrefixedName( pt.getName() ) );
        } else {
            writer.writeStartElement( "element" );
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
            writer.writeAttribute( "nillable", "true" );
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

        XSSimpleTypeDefinition xsdType = pt.getPrimitiveType().getXSType();
        if ( xsdType == null ) {
            // export without XML schema information
            BaseType type = pt.getPrimitiveType().getBaseType();
            writer.writeAttribute( "type", getSimpleType( type ) );
        } else {
            // reconstruct XML schema type definition
            String name = xsdType.getName();
            String ns = xsdType.getNamespace();
            if ( xsdType.getName() != null ) {
                QName qName = new QName( ns, name );
                writer.writeAttribute( "type", getPrefixedName( qName ) );
            } else {
                // unnamed simple property
                writer.writeStartElement( "simpleType" );
                writer.writeStartElement( "restriction" );
                writer.writeAttribute( "base", getSimpleType( pt.getPrimitiveType().getBaseType() ) );

                XSObjectList facets = pt.getPrimitiveType().getXSType().getFacets();
                for ( int i = 0; i < facets.getLength(); i++ ) {
                    XSFacet facet = (XSFacet) facets.item( i );
                    writer.writeEmptyElement( getFacetName( facet.getFacetKind() ) );
                    writer.writeAttribute( "value", facet.getLexicalFacetValue() );
                }

                writer.writeEndElement();
                writer.writeEndElement();
            }
        }
    }

    private void export( XMLStreamWriter writer, GeometryPropertyType pt )
                            throws XMLStreamException {

        GeometryType type = pt.getGeometryType();
        if ( version.equals( GMLVersion.GML_2 ) ) {
            switch ( type ) {
            case POINT:
                writer.writeAttribute( "type", "gml:PointPropertyType" );
                break;
            case CURVE:
            case LINE_STRING:
            case LINEAR_RING:
            case ORIENTABLE_CURVE:
            case RING:
                writer.writeAttribute( "type", "gml:LineStringPropertyType" );
                break;
            case POLYGON:
            case SURFACE:
            case ORIENTABLE_SURFACE:
                writer.writeAttribute( "type", "gml:PolygonPropertyType" );
                break;
            case MULTI_POINT:
                writer.writeAttribute( "type", "gml:MultiPointPropertyType" );
                break;
            case MULTI_LINE_STRING:
            case MULTI_CURVE:
            case COMPOSITE_CURVE:
                writer.writeAttribute( "type", "gml:MultiLineStringPropertyType" );
                break;
            case COMPOSITE_SURFACE:
            case MULTI_POLYGON:
            case MULTI_SURFACE:
                writer.writeAttribute( "type", "gml:MultiPolygonPropertyType" );
                break;
            case COMPOSITE:
            case MULTI_GEOMETRY:
                writer.writeAttribute( "type", "gml:MultiGeometryPropertyType" );
                break;
            case COMPOSITE_SOLID:
            case GEOMETRY:
            case MULTI_SOLID:
            case POLYHEDRAL_SURFACE:
            case PRIMITIVE:
            case SOLID:
            case TIN:
            case TRIANGULATED_SURFACE:
                writer.writeAttribute( "type", "gml:GeometryPropertyType" );
                break;
            }
        } else {
            switch ( type ) {
            case POINT:
                writer.writeAttribute( "type", "gml:PointPropertyType" );
                break;
            case LINE_STRING:
                if ( version.equals( GML_30 ) || version.equals( GML_31 ) ) {
                    writer.writeAttribute( "type", "gml:LineStringPropertyType" );
                } else {
                    writer.writeAttribute( "type", "gml:CurvePropertyType" );
                }
                break;
            case CURVE:
            case ORIENTABLE_CURVE:
                writer.writeAttribute( "type", "gml:CurvePropertyType" );
                break;
            case POLYGON:
                if ( version.equals( GML_30 ) || version.equals( GML_31 ) ) {
                    writer.writeAttribute( "type", "gml:PolygonPropertyType" );
                } else {
                    writer.writeAttribute( "type", "gml:SurfacePropertyType" );
                }
                break;
            case SURFACE:
            case ORIENTABLE_SURFACE:
                writer.writeAttribute( "type", "gml:SurfacePropertyType" );
                break;
            case SOLID:
                writer.writeAttribute( "type", "gml:SolidPropertyType" );
                break;
            case MULTI_POINT:
                writer.writeAttribute( "type", "gml:MultiPointPropertyType" );
                break;
            case MULTI_LINE_STRING:
                if ( version.equals( GML_30 ) || version.equals( GML_31 ) ) {
                    writer.writeAttribute( "type", "gml:MultiLineStringPropertyType" );
                } else {
                    writer.writeAttribute( "type", "gml:MultiCurvePropertyType" );
                }
                break;
            case MULTI_CURVE:
                writer.writeAttribute( "type", "gml:MultiCurvePropertyType" );
                break;
            case MULTI_POLYGON:
                if ( version.equals( GML_30 ) || version.equals( GML_31 ) ) {
                    writer.writeAttribute( "type", "gml:MultiPolygonPropertyType" );
                } else {
                    writer.writeAttribute( "type", "gml:MultiSurfacePropertyType" );
                }
                break;
            case MULTI_SURFACE:
                writer.writeAttribute( "type", "gml:MultiSurfacePropertyType" );
                break;
            case MULTI_SOLID:
                writer.writeAttribute( "type", "gml:MultiSolidPropertyType" );
                break;
            case MULTI_GEOMETRY:
                writer.writeAttribute( "type", "gml:MultiGeometryPropertyType" );
                break;
            case COMPOSITE:
            case COMPOSITE_CURVE:
            case COMPOSITE_SURFACE:
            case COMPOSITE_SOLID:
                writer.writeAttribute( "type", "gml:GeometricComplexPropertyType" );
                break;
            case LINEAR_RING:
                writer.writeAttribute( "type", "gml:LinearRingPropertyType" );
                break;
            case RING:
                writer.writeAttribute( "type", "gml:RingPropertyType" );
                break;
            case POLYHEDRAL_SURFACE:
            case PRIMITIVE:
            case TIN:
            case TRIANGULATED_SURFACE:
            case GEOMETRY:
                writer.writeAttribute( "type", "gml:GeometryPropertyType" );
                break;
            }
        }
    }

    private void export( XMLStreamWriter writer, FeaturePropertyType pt )
                            throws XMLStreamException {

        QName containedFt = pt.getFTName();
        if ( containedFt != null ) {
            writer.writeStartElement( "complexType" );

            // TODO what about schemas that disallow nesting of feature properties?
            writer.writeStartElement( "sequence" );
            writer.writeEmptyElement( "element" );
            writer.writeAttribute( "ref", getPrefixedName( containedFt ) );
            writer.writeAttribute( "minOccurs", "0" );
            // end 'xs:sequence'
            writer.writeEndElement();

            writer.writeEmptyElement( "attributeGroup" );
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
                QName qName = new QName( xsTypeDef.getNamespace(), xsTypeDef.getName() );
                writer.writeAttribute( "type", getPrefixedName( qName ) );
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
            writer.writeAttribute( "type", "string" );
        } else {
            writer.writeAttribute( "type", "gml:CodeType" );
        }
    }

    private void export( XMLStreamWriter writer, MeasurePropertyType pt )
                            throws XMLStreamException {
        if ( version.equals( GML_2 ) ) {
            LOG.warn( "Exporting MeasurePropertyType as GML2 schema: narrowing to xs:string." );
            writer.writeAttribute( "type", "string" );
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

        writer.writeStartElement( "simpleType" );
        if ( !simple.getAnonymous() ) {
            writer.writeAttribute( "name", simple.getName() );
        }

        // TODO how can one find the derivation type? getFinal() is wrong!
        LOG.debug( "Exporting a simple type is done always by restriction. Other derivations may be possible?!" );
        writer.writeStartElement( "restriction" );

        QName qName = new QName( simple.getBaseType().getNamespace(), simple.getBaseType().getName() );
        writer.writeAttribute( "base", getPrefixedName( qName ) );
        StringList members = simple.getLexicalEnumeration();
        if ( members != null && members.getLength() > 0 ) {
            for ( int i = 0; i < members.getLength(); i++ ) {
                writer.writeEmptyElement( "enumeration" );
                writer.writeAttribute( "value", members.item( i ) );
            }
        }

        writer.writeEndElement(); // derivation (restriction, extension, etc.)
        writer.writeEndElement(); // simpleType
    }

    private void exportComplexType( XMLStreamWriter writer, XSComplexTypeDefinition complex )
                            throws XMLStreamException {

        LOG.debug( "Exporting complex type, name: " + complex.getName() );

        writer.writeStartElement( "complexType" );
        if ( !complex.getAnonymous() ) {
            writer.writeAttribute( "name", complex.getName() );
        }

        boolean contentTypeBegin = false;
        short contentType = complex.getContentType();

        short derivation = complex.getDerivationMethod();
        XSTypeDefinition base = complex.getBaseType();

        switch ( contentType ) {
        case CONTENTTYPE_SIMPLE:
            writer.writeStartElement( "simpleContent" );
            contentTypeBegin = true;
            break;
        case CONTENTTYPE_MIXED:
            writer.writeStartElement( "complexContent" );
            contentTypeBegin = true;
            break;
        case CONTENTTYPE_EMPTY:
            break;
        case CONTENTTYPE_ELEMENT:
            // TODO check if non-redundant restriction / extension is performed (in that case, complexContent
            // container element is required)
            if ( base != null && !base.getName().equals( "anyType" ) ) {
                writer.writeStartElement( "complexContent" );
                contentTypeBegin = true;
            }
            break;
        }

        boolean derivationBegin = false;
        switch ( derivation ) {
        case DERIVATION_EXTENSION:
            if ( !contentTypeBegin ) {
                writer.writeStartElement( "complexContent" );
            }
            contentTypeBegin = true;
            writer.writeStartElement( "extension" );
            writer.writeAttribute( "base", getPrefixedName( new QName( base.getNamespace(), base.getName() ) ) );
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
                    writer.writeStartElement( "complexContent" );
                }
                contentTypeBegin = true;
                writer.writeStartElement( "restriction" );
                writer.writeAttribute( "base", getPrefixedName( new QName( base.getNamespace(), base.getName() ) ) );
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
            writer.writeEmptyElement( "attribute" );
            writer.writeAttribute( "name", attribute.getAttrDeclaration().getName() );
            XSTypeDefinition type = attribute.getAttrDeclaration().getTypeDefinition();

            String xsTypeName = type.getName();
            if ( xsTypeName == null ) {
                LOG.warn( "Exporting of anonymous attribute types not implemented -- defaulting to xs:string." );
                writer.writeAttribute( "type", "string" );
            } else {
                writer.writeAttribute( "type", getPrefixedName( new QName( type.getNamespace(), type.getName() ) ) );
            }
            if ( attribute.getRequired() ) {
                writer.writeAttribute( "use", "required" );
            }
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
                writer.writeStartElement( "sequence" );
            } else if ( modelGroup.getCompositor() == COMPOSITOR_CHOICE ) {
                writer.writeStartElement( "choice" );
            } else if ( modelGroup.getCompositor() == COMPOSITOR_ALL ) {
                writer.writeStartElement( "all" );
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
                writer.writeEmptyElement( "element" );
                writer.writeAttribute( "ref", getPrefixedName( new QName( elem.getNamespace(), elem.getName() ) ) );
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
            LOG.warn( "Exporting of wildcards is not implemented yet." );
        }
    }

    private void exportElement( XMLStreamWriter writer, XSElementDeclaration element, int minOccurs, int maxOccurs,
                                boolean maxUnbounded )
                            throws XMLStreamException {

        LOG.debug( "Exporting generic element " + element.getNamespace() + "/" + element.getName() );

        writer.writeStartElement( "element" );
        writer.writeAttribute( "name", element.getName() );
        if ( minOccurs != 1 ) {
            writer.writeAttribute( "minOccurs", String.valueOf( minOccurs ) );
        }
        if ( maxUnbounded ) {
            writer.writeAttribute( "maxOccurs", "unbounded" );
        } else if ( maxOccurs != 1 ) {
            writer.writeAttribute( "maxOccurs", String.valueOf( maxOccurs ) );
        }

        XSElementDeclaration substGroup = element.getSubstitutionGroupAffiliation();
        if ( substGroup != null ) {
            writer.writeAttribute( "substitutionGroup", getPrefixedName( new QName( substGroup.getNamespace(),
                                                                                    substGroup.getName() ) ) );
        }

        XSTypeDefinition type = element.getTypeDefinition();
        if ( !type.getAnonymous() ) {
            writer.writeAttribute( "type", getPrefixedName( new QName( type.getNamespace(), type.getName() ) ) );
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

    private String getSimpleType( BaseType type ) {
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

    private String getPrefixedName( QName name ) {

        String prefix = nsToPrefix.get( name.getNamespaceURI() );
        if ( prefix == null ) {
            LOG.warn( "No prefix for namespace '" + name.getNamespaceURI() + "' defined." );
            return "app:" + name.getLocalPart();
        }
        if ( prefix.equals( DEFAULT_NS_PREFIX ) ) {
            return name.getLocalPart();
        }
        return prefix + ":" + name.getLocalPart();
    }
}