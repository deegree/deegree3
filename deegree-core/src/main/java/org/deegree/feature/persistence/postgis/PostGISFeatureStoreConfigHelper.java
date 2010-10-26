//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
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
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
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

    public PostGISFeatureStoreConfigHelper( ApplicationSchema schema ) {
        this.schema = schema;
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

        writer.writeStartElement( CONFIG_NS, "FeatureType" );
        writer.writeAttribute( "name", getName( ft.getName() ) );

        FeatureType parentFt = schema.getParentFt( ft );
        if ( parentFt != null ) {
            writer.writeAttribute( "parent", getName( parentFt.getName() ) );
        }

        writer.writeAttribute( "mapping", getColumn( ft.getName() ) );

        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            PropertyType[] substitutions = pt.getSubstitutions();
            for ( PropertyType substitution : substitutions ) {
                if ( !substitution.isAbstract() ) {
                    writePropertyMapping( writer, substitution );
                }
            }
        }

        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, PropertyType pt )
                            throws XMLStreamException {
        if ( pt instanceof CodePropertyType ) {
            writePropertyMapping( writer, (CodePropertyType) pt );
        } else if ( pt instanceof CustomPropertyType ) {
            writePropertyMapping( writer, (CustomPropertyType) pt );
        } else if ( pt instanceof FeaturePropertyType ) {
            writePropertyMapping( writer, (FeaturePropertyType) pt );
        } else if ( pt instanceof GeometryPropertyType ) {
            writePropertyMapping( writer, (GeometryPropertyType) pt );
        } else if ( pt instanceof MeasurePropertyType ) {
            writePropertyMapping( writer, (MeasurePropertyType) pt );
        } else if ( pt instanceof SimplePropertyType ) {
            writePropertyMapping( writer, (SimplePropertyType) pt );
        } else {
            System.out.println( "Unhandled property type '" + pt.getClass() + "'" );
        }
    }

    private void writePropertyMapping( XMLStreamWriter writer, CodePropertyType pt )
                            throws XMLStreamException {
        writer.writeStartElement( CONFIG_NS, "CodeProperty" );
        writeCommonAttrs( writer, pt );
        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, FeaturePropertyType pt )
                            throws XMLStreamException {
        writer.writeEmptyElement( CONFIG_NS, "FeatureProperty" );
        writeCommonAttrs( writer, pt );
        if ( pt.getFTName() != null ) {
            writer.writeAttribute( "type", getName( pt.getFTName() ) );
        }
    }

    private void writePropertyMapping( XMLStreamWriter writer, GeometryPropertyType pt )
                            throws XMLStreamException {
        writer.writeStartElement( CONFIG_NS, "GeometryProperty" );
        writeCommonAttrs( writer, pt );
        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, MeasurePropertyType pt )
                            throws XMLStreamException {
        writer.writeStartElement( CONFIG_NS, "MeasureProperty" );
        writeCommonAttrs( writer, pt );
        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, SimplePropertyType pt )
                            throws XMLStreamException {
        writer.writeEmptyElement( CONFIG_NS, "SimpleProperty" );
        writeCommonAttrs( writer, pt );
        writer.writeAttribute( "type", pt.getPrimitiveType().getXSTypeName() );
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

        if ( pt.getMaxOccurs() == 1 ) {
            writer.writeAttribute( "mapping", getColumn( pt.getName() ) );
        } else {
            writer.writeAttribute( "mapping", "id->" + getColumn( pt.getName() ) + ".id->value" );
        }

    }

    private String getName( QName name ) {
        if ( name.getNamespaceURI() != null && !name.getNamespaceURI().equals( NULL_NS_URI ) ) {
            String prefix = schema.getXSModel().getNamespacePrefixes().get( name.getNamespaceURI() );
            return prefix + ":" + name.getLocalPart();
        }
        return name.getLocalPart();
    }

    private String getColumn( QName name ) {
        if ( name.getNamespaceURI() != null && !name.getNamespaceURI().equals( NULL_NS_URI ) ) {
            String prefix = schema.getXSModel().getNamespacePrefixes().get( name.getNamespaceURI() );
            if ( prefix == null ) {
                LOG.warn( "Prefix null!?" );
                prefix = "app";
            }
            return prefix.toLowerCase() + "_" + name.getLocalPart().toLowerCase();
        }
        return name.getLocalPart().toLowerCase();
    }

    private void writePropertyMapping( XMLStreamWriter writer, CustomPropertyType pt )
                            throws XMLStreamException {

        writer.writeStartElement( CONFIG_NS, "CustomProperty" );
        writeCommonAttrs( writer, pt );
        createMapping( writer, pt.getXSDValueType() );
        writer.writeEndElement();
    }

    private void createMapping( XMLStreamWriter writer, XSComplexTypeDefinition typeDef )
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
            writer.writeAttribute( "mapping", "attr_" + getColumn( attrName ) );
            writer.writeAttribute( "type", getPrimitiveTypeName( attrDecl.getTypeDefinition() ) );
        }

        // text node
        if ( typeDef.getContentType() != CONTENTTYPE_EMPTY && typeDef.getContentType() != CONTENTTYPE_ELEMENT ) {
            writer.writeEmptyElement( CONFIG_NS, "PrimitiveMapping" );
            writer.writeAttribute( "path", "text()" );
            writer.writeAttribute( "mapping", "value" );
            writer.writeAttribute( "type", getPrimitiveTypeName( typeDef.getSimpleType() ) );
        }

        // child elements
        XSParticle particle = typeDef.getParticle();
        if ( particle != null ) {
            createMapping( writer, particle, 1 );
        }
    }

    private void createMapping( XMLStreamWriter writer, XSParticle particle, int maxOccurs )
                            throws XMLStreamException {
        if ( particle.getMaxOccursUnbounded() ) {
            createMapping( writer, particle.getTerm(), -1 );
        } else {
            for ( int i = 1; i <= particle.getMaxOccurs(); i++ ) {
                createMapping( writer, particle.getTerm(), i );
            }
        }
    }

    private void createMapping( XMLStreamWriter writer, XSTerm term, int occurence )
                            throws XMLStreamException {
        if ( term instanceof XSElementDeclaration ) {
            createMapping( writer, (XSElementDeclaration) term, occurence );
        } else if ( term instanceof XSModelGroup ) {
            createMapping( writer, (XSModelGroup) term, occurence );
        } else {
            createMapping( writer, (XSWildcard) term, occurence );
        }
    }

    private void createMapping( XMLStreamWriter writer, XSElementDeclaration elDecl, int occurence )
                            throws XMLStreamException {

        // consider every concrete element substitution
        List<XSElementDeclaration> substitutions = schema.getXSModel().getSubstitutions( elDecl, null, true, true );

        for ( XSElementDeclaration substitution : substitutions ) {

            QName elName = new QName( substitution.getName() );
            if ( substitution.getNamespace() != null ) {
                elName = new QName( substitution.getNamespace(), substitution.getName() );
            }

            if ( schema.getFeatureType( elName ) != null ) {
                writer.writeEmptyElement( CONFIG_NS, "FeatureMapping" );
                writer.writeAttribute( "path", getName( elName ) );
                writer.writeAttribute( "mapping", getColumn( elName ) );
            } else if ( schema.getXSModel().getGeometryElement( elName ) != null ) {
                writer.writeEmptyElement( CONFIG_NS, "GeometryMapping" );
                writer.writeAttribute( "path", getName( elName ) );
                writer.writeAttribute( "mapping", getColumn( elName ) );
            } else if ( schema.getXSModel().isGMLNamespace( elName.getNamespaceURI() ) ) {
                LOG.warn( "Skipping element '" + elName + "'" );
            } else {
                writer.writeStartElement( CONFIG_NS, "CompoundMapping" );
                writer.writeAttribute( "path", getName( elName ) );
                writer.writeAttribute( "mapping", getColumn( elName ) );
                XSTypeDefinition typeDef = elDecl.getTypeDefinition();
                if ( typeDef instanceof XSComplexTypeDefinition ) {
                    createMapping( writer, (XSComplexTypeDefinition) typeDef );
                } else {
                    writer.writeEmptyElement( CONFIG_NS, "PrimitiveMapping" );
                    writer.writeAttribute( "path", "text()" );
                    writer.writeAttribute( "type", getPrimitiveTypeName( (XSSimpleTypeDefinition) typeDef ) );
                }
                writer.writeEndElement();
            }
        }
    }

    private void createMapping( XMLStreamWriter writer, XSModelGroup modelGroup, int occurrence )
                            throws XMLStreamException {
        // base += "compositor_" + modelGroup.getCompositor();
        XSObjectList particles = modelGroup.getParticles();
        for ( int i = 0; i < particles.getLength(); i++ ) {
            XSParticle particle = (XSParticle) particles.item( i );
            createMapping( writer, particle, occurrence );
        }
    }

    private void createMapping( XMLStreamWriter writer, XSWildcard wildCard, int occurrence ) {
        // TODO
    }

    private String getPrimitiveTypeName( XSSimpleTypeDefinition typeDef ) {
        if ( typeDef == null ) {
            return "string";
        }
        return XMLValueMangler.getPrimitiveType( typeDef ).getXSTypeName();
    }

    // public static void main( String[] args )
    // throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
    // ClassNotFoundException, InstantiationException, IllegalAccessException {
    //
    // String schemaURL =
    // "file:/home/markus/Programmieren/Java/workspace/deegree-inspire-node/src/main/webapp/WEB-INF/workspace/schemas/inspire/annex1/Addresses.xsd";
    // if ( schemaURL == null ) {
    // return;
    // }
    //
    // ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_32, null, schemaURL );
    // ApplicationSchema schema = adapter.extractFeatureTypeSchema();
    //
    // PostGISFeatureStoreConfigHelper helper = new PostGISFeatureStoreConfigHelper( schema );
    //
    // OutputStream os = new FileOutputStream( "/tmp/config.xml" );
    // XMLStreamWriter xmlStream = XMLOutputFactory.newInstance().createXMLStreamWriter( os );
    // xmlStream = new IndentingXMLStreamWriter( xmlStream );
    // helper.writeConfig( xmlStream, "EPSG:4258", null, "inspire", null );
    // xmlStream.close();
    // os.close();
    // }
}