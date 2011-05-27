//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.feature.persistence.sql.config;

import static java.lang.Boolean.TRUE;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.feature.persistence.sql.blob.BlobCodec.Compression.NONE;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSElementDeclaration;
import org.deegree.commons.jdbc.QTableName;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.sql.BBoxTableMapping;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.GeometryStorageParams;
import org.deegree.feature.persistence.sql.MappedApplicationSchema;
import org.deegree.feature.persistence.sql.blob.BlobCodec;
import org.deegree.feature.persistence.sql.blob.BlobMapping;
import org.deegree.feature.persistence.sql.converter.CustomParticleConverter;
import org.deegree.feature.persistence.sql.expressions.StringConst;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.id.AutoIDGenerator;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.id.IDGenerator;
import org.deegree.feature.persistence.sql.jaxb.AbstractParticleJAXB;
import org.deegree.feature.persistence.sql.jaxb.ComplexParticleJAXB;
import org.deegree.feature.persistence.sql.jaxb.CustomConverterJAXB;
import org.deegree.feature.persistence.sql.jaxb.FIDMappingJAXB;
import org.deegree.feature.persistence.sql.jaxb.FIDMappingJAXB.ColumnJAXB;
import org.deegree.feature.persistence.sql.jaxb.FeatureParticleJAXB;
import org.deegree.feature.persistence.sql.jaxb.FeatureTypeMappingJAXB;
import org.deegree.feature.persistence.sql.jaxb.GeometryParticleJAXB;
import org.deegree.feature.persistence.sql.jaxb.PrimitiveParticleJAXB;
import org.deegree.feature.persistence.sql.jaxb.SQLFeatureStoreJAXB.BLOBMapping;
import org.deegree.feature.persistence.sql.jaxb.SQLFeatureStoreJAXB.NamespaceHint;
import org.deegree.feature.persistence.sql.jaxb.SQLFeatureStoreJAXB.StorageCRS;
import org.deegree.feature.persistence.sql.mapper.XPathSchemaWalker;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.ConstantMapping;
import org.deegree.feature.persistence.sql.rules.FeatureMapping;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.MappingExpression;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;
import org.deegree.gml.schema.GMLSchemaInfoSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates {@link MappedApplicationSchema} instances from JAXB {@link BLOBMapping} and JAXB {@link FeatureTypeMapping}
 * instances.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MappedSchemaBuilderGML extends AbstractMappedSchemaBuilder {

    private static Logger LOG = LoggerFactory.getLogger( MappedSchemaBuilderGML.class );

    private static final String FEATURE_TYPE_TABLE = "feature_types";

    private static final String GML_OBJECTS_TABLE = "gml_objects";

    private final ApplicationSchema gmlSchema;

    private final NamespaceBindings nsBindings;

    private BlobMapping blobMapping;

    private BBoxTableMapping bboxMapping;

    private final Map<QName, org.deegree.feature.persistence.sql.FeatureTypeMapping> ftNameToMapping = new HashMap<QName, org.deegree.feature.persistence.sql.FeatureTypeMapping>();

    private final GeometryStorageParams geometryParams;

    private final XPathSchemaWalker schemaWalker;

    public MappedSchemaBuilderGML( String configURL, List<String> gmlSchemas, StorageCRS storageCRS,
                                   List<NamespaceHint> nsHints, BLOBMapping blobConf,
                                   List<FeatureTypeMappingJAXB> ftMappingConfs ) throws FeatureStoreException {

        gmlSchema = buildGMLSchema( configURL, gmlSchemas );
        geometryParams = new GeometryStorageParams( CRSManager.getCRSRef( storageCRS.getValue() ),
                                                    storageCRS.getSrid(), CoordinateDimension.DIM_2 );
        nsBindings = buildNSBindings( gmlSchema.getNamespaceBindings(), nsHints );
        schemaWalker = new XPathSchemaWalker( gmlSchema, nsBindings );
        if ( blobConf != null ) {
            Pair<BlobMapping, BBoxTableMapping> pair = buildBlobMapping( blobConf, gmlSchema.getXSModel().getVersion() );
            blobMapping = pair.first;
            bboxMapping = pair.second;
        }
        if ( ftMappingConfs != null ) {
            for ( FeatureTypeMappingJAXB ftMappingConf : ftMappingConfs ) {
                org.deegree.feature.persistence.sql.FeatureTypeMapping ftMapping = buildFtMapping( ftMappingConf );
                ftNameToMapping.put( ftMapping.getFeatureType(), ftMapping );
            }
        }
    }

    /**
     * Returns the {@link MappedApplicationSchema} derived from GML application schemas / configuration.
     * 
     * @return mapped application schema, never <code>null</code>
     */
    public MappedApplicationSchema getMappedSchema() {
        FeatureType[] fts = gmlSchema.getFeatureTypes();
        org.deegree.feature.persistence.sql.FeatureTypeMapping[] ftMappings = ftNameToMapping.values().toArray( new org.deegree.feature.persistence.sql.FeatureTypeMapping[ftNameToMapping.size()] );
        Map<FeatureType, FeatureType> ftToSuperFt = gmlSchema.getFtToSuperFt();
        Map<String, String> prefixToNs = new HashMap<String, String>();
        Iterator<String> prefixIter = nsBindings.getPrefixes();
        while ( prefixIter.hasNext() ) {
            String prefix = prefixIter.next();
            prefixToNs.put( prefix, nsBindings.getNamespaceURI( prefix ) );
        }
        GMLSchemaInfoSet xsModel = gmlSchema.getXSModel();
        return new MappedApplicationSchema( fts, ftToSuperFt, prefixToNs, xsModel, ftMappings, null, bboxMapping,
                                            blobMapping, geometryParams );
    }

    private ApplicationSchema buildGMLSchema( String configURL, List<String> gmlSchemas )
                            throws FeatureStoreException {

        LOG.debug( "Building application schema from GML schema files." );
        ApplicationSchema appSchema = null;
        try {
            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( configURL );

            String[] schemaURLs = new String[gmlSchemas.size()];
            int i = 0;
            for ( String gmlSchema : gmlSchemas ) {
                schemaURLs[i++] = resolver.resolve( gmlSchema.trim() ).toString();
            }

            ApplicationSchemaXSDDecoder decoder = null;
            if ( schemaURLs.length == 1 && schemaURLs[0].startsWith( "file:" ) ) {
                File file = new File( new URL( schemaURLs[0] ).toURI() );
                decoder = new ApplicationSchemaXSDDecoder( null, null, file );
            } else {
                decoder = new ApplicationSchemaXSDDecoder( null, null, schemaURLs );
            }
            appSchema = decoder.extractFeatureTypeSchema();
        } catch ( Throwable t ) {
            String msg = "Error building GML application schema: " + t.getMessage();
            throw new FeatureStoreException( msg );
        }
        LOG.debug( "GML version: " + appSchema.getXSModel().getVersion() );
        return appSchema;
    }

    private NamespaceBindings buildNSBindings( Map<String, String> schemaNSBindings, List<NamespaceHint> userHints ) {
        NamespaceBindings nsBindings = new NamespaceBindings();
        for ( String prefix : schemaNSBindings.keySet() ) {
            nsBindings.addNamespace( prefix, schemaNSBindings.get( prefix ) );
        }
        nsBindings.addNamespace( "xsi", XSINS );
        for ( NamespaceHint userHint : userHints ) {
            nsBindings.addNamespace( userHint.getPrefix(), userHint.getNamespaceURI() );
        }
        return nsBindings;
    }

    private Pair<BlobMapping, BBoxTableMapping> buildBlobMapping( BLOBMapping blobMappingConf, GMLVersion gmlVersion ) {
        String ftTable = blobMappingConf.getFeatureTypeTable() == null ? FEATURE_TYPE_TABLE
                                                                      : blobMappingConf.getFeatureTypeTable();
        BBoxTableMapping bboxMapping = new BBoxTableMapping( ftTable, geometryParams.getCrs() );
        String blobTable = blobMappingConf.getBlobTable() == null ? GML_OBJECTS_TABLE : blobMappingConf.getBlobTable();
        BlobMapping blobMapping = new BlobMapping( blobTable, geometryParams.getCrs(), new BlobCodec( gmlVersion, NONE ) );
        return new Pair<BlobMapping, BBoxTableMapping>( blobMapping, bboxMapping );
    }

    private FeatureTypeMapping buildFtMapping( FeatureTypeMappingJAXB ftMappingConf )
                            throws FeatureStoreException {

        QName ftName = ftMappingConf.getName();
        QTableName ftTable = new QTableName( ftMappingConf.getTable() );
        FIDMapping fidMapping = buildFIDMapping( ftTable, ftName, ftMappingConf.getFIDMapping() );
        List<Mapping> particleMappings = new ArrayList<Mapping>();
        XSElementDeclaration elDecl = gmlSchema.getXSModel().getElementDecl( ftName );
        for ( JAXBElement<? extends AbstractParticleJAXB> particle : ftMappingConf.getAbstractParticle() ) {
            particleMappings.add( buildMapping( ftTable, new Pair<XSElementDeclaration, Boolean>( elDecl, TRUE ),
                                                particle.getValue() ) );
        }
        return new FeatureTypeMapping( ftName, ftTable, fidMapping, particleMappings );
    }

    private FIDMapping buildFIDMapping( QTableName table, QName ftName, FIDMappingJAXB config )
                            throws FeatureStoreException {

        String prefix = config != null ? config.getPrefix() : null;
        if ( prefix == null ) {
            prefix = ftName.getPrefix().toUpperCase() + "_" + ftName.getLocalPart().toUpperCase() + "_";
        }

        List<Pair<String, BaseType>> columns = new ArrayList<Pair<String, BaseType>>();
        if ( config != null && config.getColumn() != null ) {
            for ( ColumnJAXB configColumn : config.getColumn() ) {
                String column = configColumn.getName();
                BaseType pt = null;
                if ( configColumn.getType() != null ) {
                    pt = getPrimitiveType( configColumn.getType() );
                }
                columns.add( new Pair<String, BaseType>( column, pt ) );
            }
        }

        IDGenerator generator = buildGenerator( config );
        if ( !( generator instanceof AutoIDGenerator ) ) {
            if ( columns.isEmpty() ) {
                throw new FeatureStoreException( "No FIDMapping column for table '" + table
                                                 + "' specified. This is only possible for AutoIDGenerator." );
            }
        }
        return new FIDMapping( prefix, "_", columns, generator );
    }

    private Mapping buildMapping( QTableName currentTable, Pair<XSElementDeclaration, Boolean> elDecl,
                                  AbstractParticleJAXB value ) {
        LOG.debug( "Building mapping for path '{}' on element '{}'", value.getPath(), elDecl );
        if ( value instanceof PrimitiveParticleJAXB ) {
            return buildMapping( currentTable, elDecl, (PrimitiveParticleJAXB) value );
        }
        if ( value instanceof GeometryParticleJAXB ) {
            return buildMapping( currentTable, elDecl, (GeometryParticleJAXB) value );
        }
        if ( value instanceof FeatureParticleJAXB ) {
            return buildMapping( currentTable, elDecl, (FeatureParticleJAXB) value );
        }
        if ( value instanceof ComplexParticleJAXB ) {
            return buildMapping( currentTable, elDecl, (ComplexParticleJAXB) value );
        }
        throw new RuntimeException( "Internal error. Unhandled particle mapping JAXB bean '"
                                    + value.getClass().getName() + "'." );
    }

    private Mapping buildMapping( QTableName currentTable, Pair<XSElementDeclaration, Boolean> elDecl,
                                  PrimitiveParticleJAXB config ) {

        PropertyName path = new PropertyName( config.getPath(), nsBindings );
        Pair<PrimitiveType, Boolean> pt = schemaWalker.getTargetType( elDecl, path );
        MappingExpression me = parseMappingExpression( config.getMapping() );

        if ( me instanceof DBField ) {
            List<TableJoin> joinedTable = buildJoinTable( currentTable, config.getJoin() );
            LOG.debug( "Targeted primitive type: " + pt );
            CustomParticleConverter<TypedObjectNode> converter = null;
            if ( config.getCustomConverter() != null ) {
                converter = buildConverter( config.getCustomConverter() );
            }
            return new PrimitiveMapping( path, pt.second, me, pt.first, joinedTable, converter );
        } else if ( me instanceof StringConst ) {
            String s = me.toString();
            s = s.substring( 1, s.length() - 1 );
            PrimitiveValue value = new PrimitiveValue( s, pt.first );
            return new ConstantMapping<PrimitiveValue>( path, value );
        }
        throw new IllegalArgumentException( "Mapping expressions of type '" + me.getClass()
                                            + "' are not supported yet." );
    }

    private GeometryMapping buildMapping( QTableName currentTable, Pair<XSElementDeclaration, Boolean> elDecl,
                                          GeometryParticleJAXB config ) {
        PropertyName path = new PropertyName( config.getPath(), nsBindings );
        MappingExpression me = parseMappingExpression( config.getMapping() );
        elDecl = schemaWalker.getTargetElement( elDecl, path );
        LOG.warn( "Determining geometry type from element decls is not implemented." );
        GeometryType type = GeometryType.GEOMETRY;
        List<TableJoin> joinedTable = buildJoinTable( currentTable, config.getJoin() );
        return new GeometryMapping( path, elDecl.second, me, type, geometryParams, joinedTable );
    }

    private FeatureMapping buildMapping( QTableName currentTable, Pair<XSElementDeclaration, Boolean> elDecl,
                                         FeatureParticleJAXB config ) {
        PropertyName path = new PropertyName( config.getPath(), nsBindings );
        MappingExpression me = parseMappingExpression( config.getMapping() );
        MappingExpression hrefMe = null;
        if ( config.getHrefMapping() != null ) {
            hrefMe = parseMappingExpression( config.getHrefMapping() );
        }
        elDecl = schemaWalker.getTargetElement( elDecl, path );
        QName ptName = new QName( elDecl.first.getNamespace(), elDecl.first.getName() );
        // TODO rework this
        FeaturePropertyType pt = (FeaturePropertyType) gmlSchema.getXSModel().getGMLPropertyDecl( elDecl.first, ptName,
                                                                                                  0, 1, null );
        List<TableJoin> joinedTable = buildJoinTable( currentTable, config.getJoin() );
        return new FeatureMapping( path, elDecl.second, me, hrefMe, pt.getFTName(), joinedTable );
    }

    private CompoundMapping buildMapping( QTableName currentTable, Pair<XSElementDeclaration, Boolean> elDecl,
                                          ComplexParticleJAXB config ) {
        PropertyName path = new PropertyName( config.getPath(), nsBindings );
        elDecl = schemaWalker.getTargetElement( elDecl, path );
        List<JAXBElement<? extends AbstractParticleJAXB>> children = config.getAbstractParticle();
        List<Mapping> particles = new ArrayList<Mapping>( children.size() );
        for ( JAXBElement<? extends AbstractParticleJAXB> child : children ) {
            Mapping particle = buildMapping( currentTable, elDecl, child.getValue() );
            if ( particle != null ) {
                particles.add( particle );
            }
        }
        List<TableJoin> joinedTable = buildJoinTable( currentTable, config.getJoin() );
        return new CompoundMapping( path, elDecl.second, particles, joinedTable, elDecl.first );
    }

    private CustomParticleConverter<TypedObjectNode> buildConverter( CustomConverterJAXB config ) {
        String className = config.getClazz();
        LOG.info( "Instantiating configured custom particle converter (class=" + className + ")" );
        try {
            // TODO use workspace classloader
            return (CustomParticleConverter<TypedObjectNode>) Class.forName( className ).newInstance();
        } catch ( Throwable t ) {
            String msg = "Unable to instantiate custom particle converter (class=" + className + "): " + t.getMessage();
            throw new IllegalArgumentException( msg );
        }
    }
}