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
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.deegree.commons.tom.primitive.XMLValueMangler;
import org.deegree.feature.persistence.mapping.DataTypeMapping;
import org.deegree.feature.persistence.mapping.FeatureTypeMapping;
import org.deegree.feature.persistence.mapping.JoinChain;
import org.deegree.feature.persistence.mapping.MappedApplicationSchema;
import org.deegree.feature.persistence.mapping.id.AutoIDGenerator;
import org.deegree.feature.persistence.mapping.id.FIDMapping;
import org.deegree.feature.persistence.mapping.id.IDGenerator;
import org.deegree.feature.persistence.mapping.id.SequenceIDGenerator;
import org.deegree.feature.persistence.mapping.id.UUIDGenerator;
import org.deegree.feature.persistence.mapping.property.CodeMapping;
import org.deegree.feature.persistence.mapping.property.CompoundMapping;
import org.deegree.feature.persistence.mapping.property.FeatureMapping;
import org.deegree.feature.persistence.mapping.property.GenericObjectMapping;
import org.deegree.feature.persistence.mapping.property.GeometryMapping;
import org.deegree.feature.persistence.mapping.property.Mapping;
import org.deegree.feature.persistence.mapping.property.PrimitiveMapping;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GenericObjectPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.MappingExpression;
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

    /**
     * Creates a new {@link PostGISFeatureStoreConfigWriter} instance.
     * 
     * @param schema
     *            the mapped application schema to export, must not be <code>null</code>
     */
    public PostGISFeatureStoreConfigWriter( MappedApplicationSchema schema ) {
        this.schema = schema;
    }

    /**
     * Exports the configuration document.
     * 
     * @param writer
     * @param connId
     * @param schemaURLs
     * @throws XMLStreamException
     */
    public void writeConfig( XMLStreamWriter writer, String connId, List<String> schemaURLs )
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

        for ( QName dtName : schema.getDtMappings().keySet() ) {
            DataTypeMapping dtMapping = schema.getDtMappings().get( dtName );
            writer.writeStartElement( CONFIG_NS, "DataType" );
            writer.writeAttribute( "name", getName( dtName ) );
            for ( Mapping particle : dtMapping.getParticles() ) {
                writeMapping( writer, particle );
            }
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private void writeFeatureTypeMapping( XMLStreamWriter writer, FeatureType ft )
                            throws XMLStreamException {

        LOG.info( "Feature type '" + ft.getName() + "'" );
        FeatureTypeMapping ftMapping = schema.getFtMapping( ft.getName() );

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
        } else if ( pt instanceof CodePropertyType ) {
            writePropertyMapping( writer, (CodePropertyType) pt, (CodeMapping) mapping );
        } else if ( pt instanceof GenericObjectPropertyType ) {
            writePropertyMapping( writer, (GenericObjectPropertyType) pt, (GenericObjectMapping) mapping );
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
        if ( pt.getMaxOccurs() != 1 ) {
            LOG.warn( "TODO: write join table" );
        }
        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, CodePropertyType pt, CodeMapping mapping )
                            throws XMLStreamException {

        writer.writeStartElement( CONFIG_NS, "CodeProperty" );
        writeCommonAttrs( writer, pt );
        writer.writeAttribute( "mapping", mapping.getMapping().toString() );
        writer.writeAttribute( "codeSpaceMapping", mapping.getCodeSpaceMapping().toString() );
        if ( pt.getMaxOccurs() != 1 ) {
            LOG.warn( "TODO: write join table" );
        }
        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, GeometryPropertyType pt, GeometryMapping mapping )
                            throws XMLStreamException {

        writer.writeStartElement( CONFIG_NS, "GeometryProperty" );
        writeCommonAttrs( writer, pt );
        writer.writeAttribute( "mapping", mapping.getMapping().toString() );

        GeometryType gt = pt.getGeometryType();
        switch ( gt ) {
        case POINT: {
            writer.writeAttribute( "type", "Point" );
            break;
        }
        case LINE_STRING:
        case LINEAR_RING:
        case CURVE: {
            writer.writeAttribute( "type", "LineString" );
            break;
        }
        case POLYGON:
        case SURFACE: {
            writer.writeAttribute( "type", "Polygon" );
            break;
        }
        case MULTI_POINT: {
            writer.writeAttribute( "type", "MultiPoint" );
            break;
        }
        case MULTI_LINE_STRING:
        case MULTI_CURVE: {
            writer.writeAttribute( "type", "MultiLineString" );
            break;
        }
        case MULTI_POLYGON:
        case MULTI_SURFACE: {
            writer.writeAttribute( "type", "MultiPolygon" );
            break;
        }
        case MULTI_GEOMETRY: {
            writer.writeAttribute( "type", "MultiGeometry" );
            break;
        }
        default: {
            writer.writeAttribute( "type", "Geometry" );
        }
        }
        writer.writeAttribute( "crs", mapping.getCRS().getAlias() );
        writer.writeAttribute( "srid", mapping.getSrid() );
        CoordinateDimension dim = pt.getCoordinateDimension();
        switch ( dim ) {
        case DIM_2: {
            writer.writeAttribute( "dim", "2D" );
            break;
        }
        case DIM_3: {
            writer.writeAttribute( "dim", "3D" );
            break;
        }
        case DIM_2_OR_3: {
            // TODO
            writer.writeAttribute( "dim", "2D" );
            break;
        }
        }

        if ( pt.getMaxOccurs() != 1 ) {
            LOG.warn( "TODO: write join table" );
        }
        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, FeaturePropertyType pt, FeatureMapping mapping )
                            throws XMLStreamException {
        writer.writeStartElement( CONFIG_NS, "FeatureProperty" );
        writeCommonAttrs( writer, pt );
        if ( pt.getFTName() != null ) {
            writer.writeAttribute( "type", getName( pt.getFTName() ) );
        }
        writer.writeAttribute( "mapping", mapping.getMapping().toString() );
        JoinChain jc = mapping.getJoinedTable();
        if ( jc != null ) {
            writeJoinedTable( writer, jc );
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

        if ( pt.getMaxOccurs() != 1 ) {
            LOG.warn( "TODO: write join table" );
        }
        writer.writeEndElement();
    }

    private void writePropertyMapping( XMLStreamWriter writer, GenericObjectPropertyType pt,
                                       GenericObjectMapping mapping )
                            throws XMLStreamException {

        writer.writeStartElement( CONFIG_NS, "GenericObjectProperty" );
        writeCommonAttrs( writer, pt );
        XSElementDeclaration elDecl = pt.getValueElementDecl();
        QName elName = new QName( elDecl.getNamespace(), elDecl.getName() );
        writer.writeAttribute( "valueElement", getName( elName ) );

        if ( pt.getMaxOccurs() != 1 ) {
            LOG.warn( "TODO: write join table" );
        }
        writer.writeEndElement();
    }

    private void writeMapping( XMLStreamWriter writer, Mapping particle )
                            throws XMLStreamException {

        if ( particle instanceof PrimitiveMapping ) {
            PrimitiveMapping pm = (PrimitiveMapping) particle;
            writer.writeStartElement( CONFIG_NS, "PrimitiveMapping" );
            writer.writeAttribute( "path", particle.getPath().getAsText() );
            writer.writeAttribute( "type", pm.getType().getXSTypeName() );
            MappingExpression mapping = pm.getMapping();
            if ( mapping instanceof DBField ) {
                writer.writeAttribute( "mapping", ( (DBField) mapping ).getColumn() );
            } else {
                writer.writeAttribute( "mapping", mapping.toString() );
            }
            // TODO
            writer.writeEndElement();
        } else if ( particle instanceof GeometryMapping ) {
            GeometryMapping gm = (GeometryMapping) particle;
            writer.writeStartElement( CONFIG_NS, "GeometryMapping" );
            writer.writeAttribute( "path", particle.getPath().getAsText() );
            writer.writeAttribute( "mapping", gm.getMapping().toString() );
            GeometryType gt = gm.getType();
            switch ( gt ) {
            case POINT: {
                writer.writeAttribute( "type", "Point" );
                break;
            }
            case LINE_STRING:
            case LINEAR_RING:
            case CURVE: {
                writer.writeAttribute( "type", "LineString" );
                break;
            }
            case POLYGON:
            case SURFACE: {
                writer.writeAttribute( "type", "Polygon" );
                break;
            }
            case MULTI_POINT: {
                writer.writeAttribute( "type", "MultiPoint" );
                break;
            }
            case MULTI_LINE_STRING:
            case MULTI_CURVE: {
                writer.writeAttribute( "type", "MultiLineString" );
                break;
            }
            case MULTI_POLYGON:
            case MULTI_SURFACE: {
                writer.writeAttribute( "type", "MultiPolygon" );
                break;
            }
            case MULTI_GEOMETRY: {
                writer.writeAttribute( "type", "MultiGeometry" );
                break;
            }
            default: {
                writer.writeAttribute( "type", "Geometry" );
            }
            }
            writer.writeAttribute( "crs", gm.getCRS().getAlias() );
            writer.writeAttribute( "srid", gm.getSrid() );
            CoordinateDimension dim = gm.getDim();
            switch ( dim ) {
            case DIM_2: {
                writer.writeAttribute( "dim", "2D" );
                break;
            }
            case DIM_3: {
                writer.writeAttribute( "dim", "3D" );
                break;
            }
            case DIM_2_OR_3: {
                // TODO
                writer.writeAttribute( "dim", "2D" );
                break;
            }
            }
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

    // private void writePropertyMapping( XMLStreamWriter writer, CodePropertyType pt, MappingContext mc )
    // throws XMLStreamException {
    // writer.writeStartElement( CONFIG_NS, "CodeProperty" );
    // writeCommonAttrs( writer, pt );
    // if ( pt.getMaxOccurs() == 1 ) {
    // MappingContext codeValueContext = mcManager.mapOneToOneElement( mc, pt.getName() );
    // writer.writeAttribute( "mapping", codeValueContext.getColumn() );
    // MappingContext codeSpaceContext = mcManager.mapOneToOneAttribute( codeValueContext, new QName( "codeSpace" ) );
    // writer.writeAttribute( "codeSpaceMapping", codeSpaceContext.getColumn() );
    // } else {
    // MappingContext codeValueContext = mcManager.mapOneToManyElements( mc, pt.getName() );
    // writer.writeAttribute( "mapping", "value" );
    // writer.writeAttribute( "codeSpaceMapping", "codespace" );
    // // writeJoinedTable( writer, codeValueContext.getTable() );
    // }
    // writer.writeEndElement();
    // }
    //
    // private void writePropertyMapping( XMLStreamWriter writer, MeasurePropertyType pt, MappingContext mc )
    // throws XMLStreamException {
    //
    // writer.writeStartElement( CONFIG_NS, "MeasureProperty" );
    // writeCommonAttrs( writer, pt );
    // if ( pt.getMaxOccurs() == 1 ) {
    // MappingContext measureValueContext = mcManager.mapOneToOneElement( mc, pt.getName() );
    // writer.writeAttribute( "mapping", measureValueContext.getColumn() );
    // MappingContext codeSpaceContext = mcManager.mapOneToOneAttribute( measureValueContext, new QName( "uom" ) );
    // writer.writeAttribute( "uomMapping", codeSpaceContext.getColumn() );
    // } else {
    // MappingContext measureContext = mcManager.mapOneToManyElements( mc, pt.getName() );
    // writer.writeAttribute( "mapping", "value" );
    // writer.writeAttribute( "uomMapping", "uom" );
    // // writeJoinedTable( writer, measureContext.getTable() );
    // }
    // writer.writeEndElement();
    // }

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

    private void writeJoinedTable( XMLStreamWriter writer, JoinChain jc )
                            throws XMLStreamException {
        writer.writeStartElement( CONFIG_NS, "JoinedTable" );
        writer.writeAttribute( "indexColumn", "idx" );
        writer.writeCharacters( "id=" + jc.getFields().get( 1 ).getTable() + ".parentfk" );
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