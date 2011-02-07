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
import static org.deegree.commons.xml.CommonNamespaces.XSINS;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.deegree.commons.tom.primitive.XMLValueMangler;
import org.deegree.feature.persistence.mapping.FeatureTypeMapping;
import org.deegree.feature.persistence.mapping.MappedApplicationSchema;
import org.deegree.feature.persistence.mapping.id.AutoIDGenerator;
import org.deegree.feature.persistence.mapping.id.FIDMapping;
import org.deegree.feature.persistence.mapping.id.IDGenerator;
import org.deegree.feature.persistence.mapping.id.SequenceIDGenerator;
import org.deegree.feature.persistence.mapping.id.UUIDGenerator;
import org.deegree.feature.persistence.mapping.property.CompoundMapping;
import org.deegree.feature.persistence.mapping.property.FeatureMapping;
import org.deegree.feature.persistence.mapping.property.GeometryMapping;
import org.deegree.feature.persistence.mapping.property.Mapping;
import org.deegree.feature.persistence.mapping.property.PrimitiveMapping;
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
 * Creates configuration documents for the {@link PostGISFeatureStore} from {@link MappedApplicationSchema} instances.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISFeatureStoreConfigWriter {

    private static Logger LOG = LoggerFactory.getLogger( PostGISFeatureStoreConfigWriter.class );

    private static final String CONFIG_NS = new PostGISFeatureStoreProvider().getConfigNamespace();

    private static final String SCHEMA_LOCATION = "http://www.deegree.org/datasource/feature/postgis http://schemas.deegree.org/datasource/feature/postgis/3.0.1/postgis.xsd";

    private final MappedApplicationSchema schema;

    private final MappingContextManager mcManager;

    public PostGISFeatureStoreConfigWriter( MappedApplicationSchema schema ) {
        this.schema = schema;
        mcManager = new MappingContextManager( schema.getXSModel().getNamespacePrefixes() );
    }

    public void writeConfig( XMLStreamWriter writer, String storageCrs, Map<String, String> namespaceHints,
                             String connId, List<String> schemaURLs )
                            throws XMLStreamException {

        writer.writeStartElement( "PostGISFeatureStore" );
        writer.writeAttribute( "configVersion", "3.0.1" );
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

        // writer.writeStartElement( CONFIG_NS, "StorageCRS" );
        // writer.writeCharacters( storageCrs );
        // writer.writeEndElement();

        // for ( Entry<String, String> ns : schema.getNamespaceBindings().entrySet() ) {
        // writer.writeEmptyElement( CONFIG_NS, "NamespaceHint" );
        // writer.writeAttribute( "prefix", ns.getKey() );
        // writer.writeAttribute( "namespaceURI", ns.getValue() );
        // }

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

        LOG.info( "Feature type '" + ft.getName() + "'" );
        FeatureTypeMapping ftMapping = schema.getMapping( ft.getName() );

        writer.writeStartElement( CONFIG_NS, "FeatureType" );
        writer.writeAttribute( "name", getName( ft.getName() ) );

        FeatureType parentFt = schema.getParentFt( ft );
        if ( parentFt != null ) {
            writer.writeAttribute( "parent", getName( parentFt.getName() ) );
        }

        writer.writeAttribute( "table", ftMapping.getFtTable().toString() );

        FIDMapping fidMapping = ftMapping.getFidMapping();
        writer.writeStartElement( CONFIG_NS, "FIDMapping" );
        writer.writeStartElement( CONFIG_NS, "Column" );
        writer.writeAttribute( "name", fidMapping.getColumn() );
        writer.writeAttribute( "type", fidMapping.getColumnType().getXSTypeName() );
        writer.writeEndElement();
        IDGenerator generator = fidMapping.getIdGenerator();
        if ( generator instanceof AutoIDGenerator ) {
            writer.writeEmptyElement( CONFIG_NS, "AutoIdGenerator" );
        } else if ( generator instanceof SequenceIDGenerator ) {
            writer.writeEmptyElement( CONFIG_NS, "SequenceIDGenerator" );
        } else if ( generator instanceof UUIDGenerator ) {
            writer.writeEmptyElement( CONFIG_NS, "UUIDGenerator" );
        }
        writer.writeEndElement();

        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            Mapping propMapping = ftMapping.getMapping( pt.getName() );
            if ( propMapping != null ) {
                PropertyType[] substitutions = pt.getSubstitutions();
                if ( substitutions.length > 1 ) {
                    LOG.info( "Property '" + pt.getName() + "' has multiple substitutions: " + substitutions.length );
                }
                for ( PropertyType substitution : substitutions ) {
                    if ( !substitution.isAbstract() ) {
                        writePropertyMapping( writer, substitution, propMapping );
                    }
                }
            } else {
                LOG.warn( "No mapping for property '" + pt.getName() + "'" );
            }
        }

        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, PropertyType pt, Mapping mapping )
                            throws XMLStreamException {
        LOG.info( "Mapping property type '" + pt.getName() + "'" );
        if ( pt instanceof SimplePropertyType ) {
            writePropertyMapping( writer, (SimplePropertyType) pt, (PrimitiveMapping) mapping );
        } else if ( pt instanceof GeometryPropertyType ) {
            writePropertyMapping( writer, (GeometryPropertyType) pt, (GeometryMapping) mapping );
        } else if ( pt instanceof FeaturePropertyType ) {
            writePropertyMapping( writer, (FeaturePropertyType) pt, (FeatureMapping) mapping );
        } else if ( pt instanceof CustomPropertyType ) {
            writePropertyMapping( writer, (CustomPropertyType) pt, (CompoundMapping) mapping );
        } else {
            LOG.warn( "Unhandled property type '" + pt.getClass() + "'" );
        }
    }

    private void writePropertyMapping( XMLStreamWriter writer, SimplePropertyType pt, PrimitiveMapping mapping )
                            throws XMLStreamException {

        writer.writeStartElement( CONFIG_NS, "SimpleProperty" );
        writeCommonAttrs( writer, pt );
        writer.writeAttribute( "type", pt.getPrimitiveType().getXSTypeName() );
        writer.writeAttribute( "mapping", mapping.getMapping().toString() );
        // if ( pt.getMaxOccurs() == 1 ) {
        // MappingContext simpleValueContext = mcManager.mapOneToOneElement( mc, pt.getName() );
        // writer.writeAttribute( "mapping", simpleValueContext.getColumn() );
        // } else {
        // MappingContext simpleValueContext = mcManager.mapOneToManyElements( mc, pt.getName() );
        // writeJoinedTable( writer, simpleValueContext.getTable() );
        // }
        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, GeometryPropertyType pt, GeometryMapping mapping )
                            throws XMLStreamException {
        writer.writeStartElement( CONFIG_NS, "GeometryProperty" );
        writeCommonAttrs( writer, pt );
        writer.writeAttribute( "mapping", mapping.getMapping().toString() );
        writer.writeAttribute( "type", pt.getGeometryType().name() );
        writer.writeAttribute( "crs", "" );
        writer.writeAttribute( "srid", "" );
        writer.writeAttribute( "dim", pt.getCoordinateDimension().name() );
        // if ( pt.getMaxOccurs() == 1 ) {
        // MappingContext geometryValueContext = mcManager.mapOneToOneElement( mc, pt.getName() );
        // writer.writeAttribute( "mapping", geometryValueContext.getColumn() );
        // } else {
        // MappingContext geometryValueContext = mcManager.mapOneToManyElements( mc, pt.getName() );
        // writeJoinedTable( writer, geometryValueContext.getTable() );
        // }
        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, FeaturePropertyType pt, FeatureMapping mapping )
                            throws XMLStreamException {
        writer.writeStartElement( CONFIG_NS, "FeatureProperty" );
        writeCommonAttrs( writer, pt );
        if ( pt.getFTName() != null ) {
            writer.writeAttribute( "type", getName( pt.getFTName() ) );
        }
        if ( pt.getMaxOccurs() == 1 ) {
            writer.writeAttribute( "mapping", mapping.getMapping().toString() );
        } else {
            // MappingContext featureValueContext = mcManager.mapOneToManyElements( mc, ( pt.getName() ) );
            // writeJoinedTable( writer, featureValueContext.getTable() );
        }
        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, CustomPropertyType pt, CompoundMapping mapping )
                            throws XMLStreamException {

        writer.writeStartElement( CONFIG_NS, "CustomProperty" );
        writeCommonAttrs( writer, pt );

        for ( Mapping particle : mapping.getParticles() ) {
            writeMapping( writer, particle );
        }

        // MappingContext customValueContext = null;
        // if ( pt.getMaxOccurs() == 1 ) {
        // customValueContext = mcManager.mapOneToOneElement( mc, pt.getName() );
        // } else {
        // customValueContext = mcManager.mapOneToManyElements( mc, pt.getName() );
        // writeJoinedTable( writer, customValueContext.getTable() );
        // }

        // Map<QName, QName> elements = new LinkedHashMap<QName, QName>();
        // elements.put( pt.getName(), getQName( pt.getXSDValueType() ) );
        //
        // createMapping( writer, pt.getXSDValueType(), customValueContext, elements );
        writer.writeEndElement();
    }

    private void writeMapping( XMLStreamWriter writer, Mapping particle )
                            throws XMLStreamException {

        if ( particle instanceof PrimitiveMapping ) {
            PrimitiveMapping pm = (PrimitiveMapping) particle;
            writer.writeStartElement( CONFIG_NS, "PrimitiveMapping" );
            writer.writeAttribute( "path", particle.getPath().getAsText() );
            writer.writeAttribute( "type", pm.getType().getXSTypeName() );
            writer.writeAttribute( "mapping", pm.getMapping().toString() );
            // TODO
            writer.writeEndElement();
        } else if ( particle instanceof GeometryMapping ) {
            GeometryMapping gm = (GeometryMapping) particle;
            writer.writeStartElement( CONFIG_NS, "GeometryMapping" );
            writer.writeAttribute( "path", particle.getPath().getAsText() );
            writer.writeAttribute( "mapping", gm.getMapping().toString() );
            // TODO
            writer.writeEndElement();
        } else if ( particle instanceof FeatureMapping ) {
            writer.writeStartElement( CONFIG_NS, "FeatureMapping" );
            writer.writeAttribute( "path", particle.getPath().getAsText() );
            // TODO
            writer.writeEndElement();
        } else if ( particle instanceof CompoundMapping ) {
            writer.writeStartElement( CONFIG_NS, "ComplexMapping" );
            writer.writeAttribute( "path", particle.getPath().getAsText() );
            CompoundMapping compound = (CompoundMapping) particle;
            for ( Mapping childMapping : compound.getParticles() ) {
                writeMapping( writer, childMapping );
            }
            writer.writeEndElement();
        }
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
}