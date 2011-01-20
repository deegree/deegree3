//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.feature.persistence.postgis;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_ELEMENT;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_EMPTY;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.gml.GMLVersion.GML_32;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSWildcard;
import org.deegree.commons.tom.primitive.XMLValueMangler;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GenericGMLObjectPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISFeatureStoreConfigHelper {

    private static Logger LOG = LoggerFactory.getLogger( PostGISFeatureStoreConfigHelper.class );

    private static final String CONFIG_NS = new PostGISFeatureStoreProvider().getConfigNamespace();

    private static final String SCHEMA_LOCATION = "http://www.deegree.org/datasource/feature/postgis http://schemas.deegree.org/datasource/feature/postgis/0.6.1/postgis.xsd";

    private final ApplicationSchema schema;

    private final MappingContextManager mcManager;

    public PostGISFeatureStoreConfigHelper( ApplicationSchema schema ) {
        this.schema = schema;
        mcManager = new MappingContextManager( schema.getXSModel().getNamespacePrefixes() );
    }

    public void writeConfig( XMLStreamWriter writer, String storageCrs, Map<String, String> namespaceHints,
                             String connId, List<String> schemaURLs )
                            throws XMLStreamException {

        writer.writeStartElement( "PostGISFeatureStore" );
        writer.writeAttribute( "configVersion", "0.6.1" );
        writer.writeNamespace( DEFAULT_NS_PREFIX, CONFIG_NS );
        writer.writeNamespace( "xsi", XSINS );
        writer.writeAttribute( XSINS, "schemaLocation", SCHEMA_LOCATION );
        int i = 1;
        for ( String ns : schema.getXSModel().getAppNamespaces() ) {
            String prefix = schema.getXSModel().getNamespacePrefixes().get( ns );
            if ( prefix != null && !prefix.equals( XMLConstants.DEFAULT_NS_PREFIX ) ) {
                writer.writeNamespace( prefix, ns );
            } else {
                writer.writeNamespace( "app" + ( i++ ), ns );
            }
        }

        writer.writeStartElement( CONFIG_NS, "StorageCRS" );
        writer.writeCharacters( storageCrs );
        writer.writeEndElement();

        for ( Entry<String, String> ns : schema.getNamespaceBindings().entrySet() ) {
            writer.writeEmptyElement( CONFIG_NS, "NamespaceHint" );
            writer.writeAttribute( "prefix", ns.getKey() );
            writer.writeAttribute( "namespaceURI", ns.getValue() );
        }

        writer.writeStartElement( CONFIG_NS, "JDBCConnId" );
        writer.writeCharacters( connId );
        writer.writeEndElement();

        List<FeatureType> fts = schema.getFeatureTypes( null, false, false );
        SortedSet<String> ftNames = new TreeSet<String>();
        for ( FeatureType ft : fts ) {
            ftNames.add( ft.getName().toString() );
        }

        for ( String qName : ftNames ) {
            QName ftName = QName.valueOf( qName );
            FeatureType ft = schema.getFeatureType( ftName );
            writeFeatureTypeMapping( writer, ft );
        }

        writer.writeEndElement();
    }

    private void writeFeatureTypeMapping( XMLStreamWriter writer, FeatureType ft )
                            throws XMLStreamException {

        LOG.info( "Mapping feature type '" + ft.getName() + "'" );

        writer.writeStartElement( CONFIG_NS, "FeatureType" );
        writer.writeAttribute( "name", getName( ft.getName() ) );

        FeatureType parentFt = schema.getParentFt( ft );
        if ( parentFt != null ) {
            writer.writeAttribute( "parent", getName( parentFt.getName() ) );
        }

        MappingContext mc = mcManager.newContext( ft.getName() );
        writer.writeAttribute( "table", mc.getTable() );

        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            PropertyType[] substitutions = pt.getSubstitutions();
            if ( substitutions.length > 1 ) {
                LOG.info( "Property '" + pt.getName() + "' has multiple substitutions: " + substitutions.length );
            }
            for ( PropertyType substitution : substitutions ) {
                if ( !substitution.isAbstract() ) {
                    writePropertyMapping( writer, substitution, mc );
                }
            }
        }

        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, PropertyType pt, MappingContext mc )
                            throws XMLStreamException {
        LOG.info( "Mapping property type '" + pt.getName() + "'");
        if ( pt instanceof CodePropertyType ) {
            writePropertyMapping( writer, (CodePropertyType) pt, mc );
        } else if ( pt instanceof CustomPropertyType ) {
            writePropertyMapping( writer, (CustomPropertyType) pt, mc );
        } else if ( pt instanceof FeaturePropertyType ) {
            writePropertyMapping( writer, (FeaturePropertyType) pt, mc );
        } else if ( pt instanceof GeometryPropertyType ) {
            writePropertyMapping( writer, (GeometryPropertyType) pt, mc );
        } else if ( pt instanceof GenericGMLObjectPropertyType ) {
            LOG.info ("Skipping property: " + pt.getName());
        } else if ( pt instanceof MeasurePropertyType ) {
            writePropertyMapping( writer, (MeasurePropertyType) pt, mc );
        } else if ( pt instanceof SimplePropertyType ) {
            writePropertyMapping( writer, (SimplePropertyType) pt, mc );
        } else {
            throw new RuntimeException( "Unhandled property type '" + pt.getClass() + "'" );
        }
    }

    private void writePropertyMapping( XMLStreamWriter writer, SimplePropertyType pt, MappingContext mc )
                            throws XMLStreamException {
        writer.writeStartElement( CONFIG_NS, "SimpleProperty" );
        writeCommonAttrs( writer, pt );
        writer.writeAttribute( "type", pt.getPrimitiveType().getXSTypeName() );
        if ( pt.getMaxOccurs() == 1 ) {
            MappingContext simpleValueContext = mcManager.mapOneToOneElement( mc, pt.getName() );
            writer.writeAttribute( "mapping", simpleValueContext.getColumn() );
        } else {
            MappingContext simpleValueContext = mcManager.mapOneToManyElements( mc, pt.getName() );
            writeJoinedTable( writer, simpleValueContext.getTable() );
        }
        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, GeometryPropertyType pt, MappingContext mc )
                            throws XMLStreamException {
        writer.writeStartElement( CONFIG_NS, "GeometryProperty" );
        writeCommonAttrs( writer, pt );
        if ( pt.getMaxOccurs() == 1 ) {
            MappingContext geometryValueContext = mcManager.mapOneToOneElement( mc, pt.getName() );
            writer.writeAttribute( "mapping", geometryValueContext.getColumn() );
        } else {
            MappingContext geometryValueContext = mcManager.mapOneToManyElements( mc, pt.getName() );
            writeJoinedTable( writer, geometryValueContext.getTable() );
        }
        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, FeaturePropertyType pt, MappingContext mc )
                            throws XMLStreamException {
        writer.writeStartElement( CONFIG_NS, "FeatureProperty" );
        writeCommonAttrs( writer, pt );
        if ( pt.getFTName() != null ) {
            writer.writeAttribute( "type", getName( pt.getFTName() ) );
        }
        if ( pt.getMaxOccurs() == 1 ) {
            MappingContext featureValueContext = mcManager.mapOneToOneElement( mc, pt.getName() );
            writer.writeAttribute( "mapping", featureValueContext.getColumn() );
        } else {
            MappingContext featureValueContext = mcManager.mapOneToManyElements( mc, ( pt.getName() ) );
            writeJoinedTable( writer, featureValueContext.getTable() );
        }
        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, CodePropertyType pt, MappingContext mc )
                            throws XMLStreamException {
        writer.writeStartElement( CONFIG_NS, "CodeProperty" );
        writeCommonAttrs( writer, pt );
        if ( pt.getMaxOccurs() == 1 ) {
            MappingContext codeValueContext = mcManager.mapOneToOneElement( mc, pt.getName() );
            writer.writeAttribute( "mapping", codeValueContext.getColumn() );
            MappingContext codeSpaceContext = mcManager.mapOneToOneAttribute( codeValueContext, new QName( "codeSpace" ) );
            writer.writeAttribute( "codeSpaceMapping", codeSpaceContext.getColumn() );
        } else {
            MappingContext codeValueContext = mcManager.mapOneToManyElements( mc, pt.getName() );
            writer.writeAttribute( "mapping", "value" );
            writer.writeAttribute( "codeSpaceMapping", "codespace" );
            writeJoinedTable( writer, codeValueContext.getTable() );
        }
        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, MeasurePropertyType pt, MappingContext mc )
                            throws XMLStreamException {

        writer.writeStartElement( CONFIG_NS, "MeasureProperty" );
        writeCommonAttrs( writer, pt );
        if ( pt.getMaxOccurs() == 1 ) {
            MappingContext measureValueContext = mcManager.mapOneToOneElement( mc, pt.getName() );
            writer.writeAttribute( "mapping", measureValueContext.getColumn() );
            MappingContext codeSpaceContext = mcManager.mapOneToOneAttribute( measureValueContext, new QName( "uom" ) );
            writer.writeAttribute( "uomMapping", codeSpaceContext.getColumn() );
        } else {
            MappingContext measureContext = mcManager.mapOneToManyElements( mc, pt.getName() );
            writer.writeAttribute( "mapping", "value" );
            writer.writeAttribute( "uomMapping", "uom" );
            writeJoinedTable( writer, measureContext.getTable() );
        }
        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, CustomPropertyType pt, MappingContext mc )
                            throws XMLStreamException {

        writer.writeStartElement( CONFIG_NS, "CustomProperty" );
        writeCommonAttrs( writer, pt );

        MappingContext customValueContext = null;
        if ( pt.getMaxOccurs() == 1 ) {
            customValueContext = mcManager.mapOneToOneElement( mc, pt.getName() );
        } else {
            customValueContext = mcManager.mapOneToManyElements( mc, pt.getName() );
            writeJoinedTable( writer, customValueContext.getTable() );
        }

        Map<QName, QName> elements = new LinkedHashMap<QName, QName>();
        elements.put( pt.getName(), getQName( pt.getXSDValueType() ) );

        createMapping( writer, pt.getXSDValueType(), customValueContext, elements );
        writer.writeEndElement();
    }

    private void writeCommonAttrs( XMLStreamWriter writer, PropertyType pt )
                            throws XMLStreamException {

        writer.writeAttribute( "name", getName( pt.getName() ) );

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
    }

    private void writeJoinedTable( XMLStreamWriter writer, String table )
                            throws XMLStreamException {
        writer.writeStartElement( CONFIG_NS, "JoinedTable" );
        writer.writeAttribute( "indexColumn", "idx" );
        writer.writeCharacters( "id=" + table + ".parentfk" );
        writer.writeEndElement();
    }

    private void createMapping( XMLStreamWriter writer, XSComplexTypeDefinition typeDef, MappingContext mc,
                                Map<QName, QName> elements )
                            throws XMLStreamException {

        // attributes
        XSObjectList attributeUses = typeDef.getAttributeUses();
        for ( int i = 0; i < attributeUses.getLength(); i++ ) {
            XSAttributeDeclaration attrDecl = ( (XSAttributeUse) attributeUses.item( i ) ).getAttrDeclaration();
            QName attrName = new QName( attrDecl.getName() );
            if ( attrDecl.getNamespace() != null ) {
                attrName = new QName( attrDecl.getNamespace(), attrDecl.getName() );
            }
            writer.writeEmptyElement( CONFIG_NS, "PrimitiveMapping" );
            writer.writeAttribute( "path", "@" + getName( attrName ) );
            MappingContext attrMc = mcManager.mapOneToOneAttribute( mc, attrName );
            writer.writeAttribute( "mapping", attrMc.getColumn() );
            writer.writeAttribute( "type", getPrimitiveTypeName( attrDecl.getTypeDefinition() ) );
        }

        // text node
        if ( typeDef.getContentType() != CONTENTTYPE_EMPTY && typeDef.getContentType() != CONTENTTYPE_ELEMENT ) {
            writer.writeEmptyElement( CONFIG_NS, "PrimitiveMapping" );
            writer.writeAttribute( "path", "text()" );
            MappingContext primitiveMc = mcManager.mapOneToOneElement( mc, new QName( "value" ) );
            writer.writeAttribute( "mapping", primitiveMc.getColumn() );
            writer.writeAttribute( "type", getPrimitiveTypeName( typeDef.getSimpleType() ) );
        }

        // child elements
        XSParticle particle = typeDef.getParticle();
        if ( particle != null ) {
            createMapping( writer, particle, 1, mc, elements );
        }
    }

    private void createMapping( XMLStreamWriter writer, XSParticle particle, int maxOccurs, MappingContext mc,
                                Map<QName, QName> elements )
                            throws XMLStreamException {
        if ( particle.getMaxOccursUnbounded() ) {
            createMapping( writer, particle.getTerm(), -1, mc, elements );
        } else {
            for ( int i = 1; i <= particle.getMaxOccurs(); i++ ) {
                createMapping( writer, particle.getTerm(), i, mc, elements );
            }
        }
    }

    private void createMapping( XMLStreamWriter writer, XSTerm term, int occurence, MappingContext mc,
                                Map<QName, QName> elements )
                            throws XMLStreamException {
        if ( term instanceof XSElementDeclaration ) {
            createMapping( writer, (XSElementDeclaration) term, occurence, mc, elements );
        } else if ( term instanceof XSModelGroup ) {
            createMapping( writer, (XSModelGroup) term, occurence, mc, elements );
        } else {
            createMapping( writer, (XSWildcard) term, occurence, mc, elements );
        }
    }

    private void createMapping( XMLStreamWriter writer, XSElementDeclaration elDecl, int occurence, MappingContext mc,
                                Map<QName, QName> elements )
                            throws XMLStreamException {

        // consider every concrete element substitution
        List<XSElementDeclaration> substitutions = schema.getXSModel().getSubstitutions( elDecl, null, true, true );

        for ( XSElementDeclaration substitution : substitutions ) {

            Map<QName, QName> elements2 = new LinkedHashMap<QName, QName>( elements );

            QName elName = new QName( substitution.getName() );
            if ( substitution.getNamespace() != null ) {
                elName = new QName( substitution.getNamespace(), substitution.getName() );
            }

            MappingContext elMC = null;
            if ( occurence == 1 ) {
                elMC = mcManager.mapOneToOneElement( mc, elName );
            } else {
                elMC = mcManager.mapOneToManyElements( mc, elName );
            }

            String path = getName( elName );

            if ( schema.getFeatureType( elName ) != null ) {
                writer.writeStartElement( CONFIG_NS, "FeatureMapping" );
                writer.writeAttribute( "path", path );
                // TODO
                writer.writeAttribute( "mapping", elMC.getColumn() );

                if ( occurence == -1 ) {
                    writeJoinedTable( writer, elMC.getTable() );
                }
                writer.writeEndElement();
            } else if ( schema.getXSModel().getGeometryElement( elName ) != null ) {
                writer.writeStartElement( CONFIG_NS, "GeometryMapping" );
                writer.writeAttribute( "path", getName( elName ) );
                // TODO
                writer.writeAttribute( "mapping", elMC.getColumn() );

                if ( occurence == -1 ) {
                    writeJoinedTable( writer, elMC.getTable() );
                }
                writer.writeEndElement();
            } else {

                if ( elName.equals( new QName( "http://www.isotc211.org/2005/gmd", "CI_Citation" ) ) ) {
                    LOG.warn( "Skipping CI_Citation!!!" );
                    continue;
                }

                if ( elName.equals( new QName( "http://www.isotc211.org/2005/gmd", "CI_Contact" ) ) ) {
                    LOG.warn( "Skipping CI_Contact!!!" );
                    continue;
                }

                if ( elName.equals( new QName( "http://www.isotc211.org/2005/gmd", "CI_ResponsibleParty" ) ) ) {
                    LOG.warn( "Skipping CI_ResponsibleParty!!!" );
                    continue;
                }

                if ( elName.equals( new QName( "http://www.isotc211.org/2005/gmd", "MD_Resolution" ) ) ) {
                    LOG.warn( "Skipping MD_Resolution!!!" );
                    continue;
                }

                if ( elName.equals( new QName( "http://www.isotc211.org/2005/gmd", "EX_Extent" ) ) ) {
                    LOG.warn( "Skipping EX_Extent!!!" );
                    continue;
                }

                if ( elName.equals( new QName( "http://www.isotc211.org/2005/gmd", "MD_PixelOrientationCode" ) ) ) {
                    LOG.warn( "Skipping EX_Extent!!!" );
                    continue;
                }

                if ( elName.equals( new QName( CommonNamespaces.GML3_2_NS, "TimeOrdinalEra" ) ) ) {
                    LOG.warn( "Skipping TimeOrdinalEra!!!" );
                    continue;
                }

                if ( elName.equals( new QName( CommonNamespaces.GML3_2_NS, "TimePeriod" ) ) ) {
                    LOG.warn( "Skipping TimePeriod!!!" );
                    continue;
                }

                if ( elName.getNamespaceURI().equals( CommonNamespaces.GML3_2_NS ) ) {
                    if ( elName.getLocalPart().endsWith( "CRS" ) ) {
                        LOG.warn( "Skipping " + elName.getLocalPart() + "!!!" );
                    }
                    continue;
                }

                XSTypeDefinition typeDef = substitution.getTypeDefinition();
                QName complexTypeName = getQName( typeDef );
                // TODO multiple elements with same name?
                QName complexTypeName2 = elements2.get( elName );
                if ( complexTypeName2 != null && complexTypeName2.equals( complexTypeName ) ) {
                    // during this mapping traversal, there already has been an element with this name and type
                    StringBuffer sb = new StringBuffer( "Path: " );
                    for ( QName qName : elements2.keySet() ) {
                        sb.append( qName );
                        sb.append( " -> " );
                    }
                    sb.append( elName );
                    LOG.info( "Skipping complex element '" + elName + "' -- detected recursion: " + sb );
                    continue;
                }
                elements2.put( elName, getQName( typeDef ) );

                writer.writeStartElement( CONFIG_NS, "ComplexMapping" );
                writer.writeAttribute( "path", path );

                if ( occurence == -1 ) {
                    writeJoinedTable( writer, elMC.getTable() );
                }

                if ( typeDef instanceof XSComplexTypeDefinition ) {
                    createMapping( writer, (XSComplexTypeDefinition) typeDef, elMC, elements2 );
                } else {
                    writer.writeEmptyElement( CONFIG_NS, "PrimitiveMapping" );
                    writer.writeAttribute( "path", "text()" );
                    writer.writeAttribute( "type", getPrimitiveTypeName( (XSSimpleTypeDefinition) typeDef ) );
                    writer.writeAttribute( "mapping", elMC.getColumn() );
                }
                writer.writeEndElement();
            }
        }
    }

    private void createMapping( XMLStreamWriter writer, XSModelGroup modelGroup, int occurrence, MappingContext mc,
                                Map<QName, QName> elements )
                            throws XMLStreamException {
        XSObjectList particles = modelGroup.getParticles();
        for ( int i = 0; i < particles.getLength(); i++ ) {
            XSParticle particle = (XSParticle) particles.item( i );
            createMapping( writer, particle, occurrence, mc, elements );
        }
    }

    private void createMapping( XMLStreamWriter writer, XSWildcard wildCard, int occurrence, MappingContext mc,
                                Map<QName, QName> elements ) {
        LOG.warn( "Handling of wild cards not implemented yet." );
        StringBuffer sb = new StringBuffer( "Path: " );
        for ( QName qName : elements.keySet() ) {
            sb.append( qName );
            sb.append( " -> " );
        }
        sb.append( "wildcard" );
        LOG.info( "Skipping wildcard at path: " + sb );
    }

    private String getPrimitiveTypeName( XSSimpleTypeDefinition typeDef ) {
        if ( typeDef == null ) {
            return "string";
        }
        return XMLValueMangler.getPrimitiveType( typeDef ).getXSTypeName();
    }

    private String getName( QName name ) {
        if ( name.getNamespaceURI() != null && !name.getNamespaceURI().equals( NULL_NS_URI ) ) {
            String prefix = schema.getXSModel().getNamespacePrefixes().get( name.getNamespaceURI() );
            return prefix + ":" + name.getLocalPart();
        }
        return name.getLocalPart();
    }

    private QName getQName( XSTypeDefinition xsType ) {
        QName name = null;
        if ( !xsType.getAnonymous() ) {
            name = new QName( xsType.getNamespace(), xsType.getName() );
        }
        return name;
    }

    public static void main( String[] args )
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException {

        File schemaFolder = new File(
                                      "/home/schneider/workspaces/projekte/bgrbmlwfs-trunk/modules/bgrbmlwfs-workspace/src/main/workspace/schemas/BoreholeML.xsd" );

        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GML_32, null, schemaFolder );
        ApplicationSchema schema = adapter.extractFeatureTypeSchema();

        PostGISFeatureStoreConfigHelper helper = new PostGISFeatureStoreConfigHelper( schema );

        OutputStream os = new FileOutputStream(
                                                "/tmp/out.xml" );
        XMLStreamWriter xmlStream = XMLOutputFactory.newInstance().createXMLStreamWriter( os );
        xmlStream = new IndentingXMLStreamWriter( xmlStream );

        String schemaURL = "/home/schneider/workspaces/projekte/bgrbmlwfs-trunk/modules/bgrbmlwfs-workspace/src/main/workspace/schemas/BoreholeML.xsd";

        helper.writeConfig( xmlStream, "EPSG:4258", null, "inspire", Collections.singletonList( schemaURL ) );
        xmlStream.close();
        os.close();
    }
}