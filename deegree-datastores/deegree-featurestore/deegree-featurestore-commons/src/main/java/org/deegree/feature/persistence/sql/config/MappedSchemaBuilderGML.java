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

import org.deegree.commons.jdbc.QTableName;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.postgis.jaxb.AbstractParticleJAXB;
import org.deegree.feature.persistence.postgis.jaxb.ComplexParticleJAXB;
import org.deegree.feature.persistence.postgis.jaxb.FIDMappingJAXB;
import org.deegree.feature.persistence.postgis.jaxb.FIDMappingJAXB.Column;
import org.deegree.feature.persistence.postgis.jaxb.FeatureParticleJAXB;
import org.deegree.feature.persistence.postgis.jaxb.FeatureTypeMappingJAXB;
import org.deegree.feature.persistence.postgis.jaxb.GeometryParticleJAXB;
import org.deegree.feature.persistence.postgis.jaxb.PostGISFeatureStoreJAXB.BLOBMapping;
import org.deegree.feature.persistence.postgis.jaxb.PostGISFeatureStoreJAXB.NamespaceHint;
import org.deegree.feature.persistence.postgis.jaxb.PrimitiveParticleJAXB;
import org.deegree.feature.persistence.sql.BBoxTableMapping;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.MappedApplicationSchema;
import org.deegree.feature.persistence.sql.blob.BlobCodec;
import org.deegree.feature.persistence.sql.blob.BlobMapping;
import org.deegree.feature.persistence.sql.expressions.JoinChain;
import org.deegree.feature.persistence.sql.id.AutoIDGenerator;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.id.IDGenerator;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.FeatureMapping;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
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

    public MappedSchemaBuilderGML( String configURL, List<String> gmlSchemas, List<NamespaceHint> nsHints,
                                   BLOBMapping blobConf, List<FeatureTypeMappingJAXB> ftMappingConfs )
                            throws FeatureStoreException {

        gmlSchema = buildGMLSchema( configURL, gmlSchemas );
        nsBindings = buildNSBindings( gmlSchema.getNamespaceBindings(), nsHints );
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
                                            blobMapping );
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
        for ( NamespaceHint userHint : userHints ) {
            nsBindings.addNamespace( userHint.getPrefix(), userHint.getNamespaceURI() );
        }
        return nsBindings;
    }

    private Pair<BlobMapping, BBoxTableMapping> buildBlobMapping( BLOBMapping blobMappingConf, GMLVersion gmlVersion ) {
        ICRS storageCRS = CRSManager.getCRSRef( blobMappingConf.getStorageCRS(), false );
        String ftTable = blobMappingConf.getFeatureTypeTable() == null ? FEATURE_TYPE_TABLE
                                                                      : blobMappingConf.getFeatureTypeTable();
        BBoxTableMapping bboxMapping = new BBoxTableMapping( ftTable, storageCRS );
        String blobTable = blobMappingConf.getBlobTable() == null ? GML_OBJECTS_TABLE : blobMappingConf.getBlobTable();
        BlobMapping blobMapping = new BlobMapping( blobTable, storageCRS, new BlobCodec( gmlVersion, NONE ) );
        return new Pair<BlobMapping, BBoxTableMapping>( blobMapping, bboxMapping );
    }

    private FeatureTypeMapping buildFtMapping( FeatureTypeMappingJAXB ftMappingConf )
                            throws FeatureStoreException {

        QName ftName = ftMappingConf.getName();
        QTableName ftTable = new QTableName( ftMappingConf.getTable() );
        FIDMapping fidMapping = buildFIDMapping( ftTable, ftName, ftMappingConf.getFIDMapping() );
        List<Mapping> particleMappings = new ArrayList<Mapping>();
        for ( JAXBElement<? extends AbstractParticleJAXB> particle : ftMappingConf.getAbstractParticle() ) {
            particleMappings.add( buildMapping( ftTable, particle.getValue() ) );
        }
        return new FeatureTypeMapping( ftName, ftTable, fidMapping, particleMappings );
    }

    private FIDMapping buildFIDMapping( QTableName table, QName ftName, FIDMappingJAXB config )
                            throws FeatureStoreException {

        String prefix = ftName.getPrefix().toUpperCase() + "_" + ftName.getLocalPart().toUpperCase() + "_";
        Column column = null;
        if ( config != null ) {
            column = config.getColumn();
        }

        String columnName = null;
        IDGenerator generator = buildGenerator( config );
        if ( generator instanceof AutoIDGenerator ) {
            if ( column != null && column.getName() != null ) {
                columnName = column.getName();
            }
        } else {
            if ( column == null || column.getName() == null ) {
                throw new FeatureStoreException( "No FIDMapping column for table '" + table
                                                 + "' specified. This is only possible for AutoIDGenerator." );
            }
            columnName = column.getName();
        }

        PrimitiveType pt = null;
        if ( config != null && config.getColumn().getType() != null ) {
            pt = getPrimitiveType( config.getColumn().getType() );
            columnName = config.getColumn().getName();
        }
        return new FIDMapping( prefix, columnName, pt, generator );
    }

    private Mapping buildMapping( QTableName currentTable, AbstractParticleJAXB value ) {
        if ( value instanceof PrimitiveParticleJAXB ) {
            return buildMapping( currentTable, (PrimitiveParticleJAXB) value );
        }
        if ( value instanceof GeometryParticleJAXB ) {
            return buildMapping( currentTable, (GeometryParticleJAXB) value );
        }
        if ( value instanceof FeatureParticleJAXB ) {
            return buildMapping( currentTable, (FeatureParticleJAXB) value );
        }
        if ( value instanceof ComplexParticleJAXB ) {
            return buildMapping( currentTable, (ComplexParticleJAXB) value );
        }
        throw new RuntimeException( "Internal error. Unhandled particle mapping JAXB bean '"
                                    + value.getClass().getName() + "'." );
    }

    private PrimitiveMapping buildMapping( QTableName currentTable, PrimitiveParticleJAXB config ) {
        PropertyName path = new PropertyName( config.getPath(), nsBindings );
        PrimitiveType pt = getPrimitiveType( config.getType() );
        MappingExpression me = parseMappingExpression( config.getMapping() );
        DBField nilMapping = buildNilMapping( config.getNilMapping() );
        JoinChain joinedTable = buildJoinTable( currentTable, config.getJoinedTable() );
        return new PrimitiveMapping( path, me, pt, joinedTable, nilMapping );
    }

    private GeometryMapping buildMapping( QTableName currentTable, GeometryParticleJAXB config ) {
        PropertyName path = new PropertyName( config.getPath(), nsBindings );
        MappingExpression me = parseMappingExpression( config.getMapping() );
        LOG.warn( "Build mappings from geometry particle configs is incomplete." );
        GeometryType type = getGeometryType( config.getType().name() );
        CoordinateDimension dim = CoordinateDimension.DIM_2;
        ICRS crs = CRSManager.getCRSRef( config.getCrs() );
        String srid = config.getSrid() + "";
        JoinChain joinedTable = buildJoinTable( currentTable, config.getJoinedTable() );
        DBField nilMapping = buildNilMapping( config.getNilMapping() );
        return new GeometryMapping( path, me, type, dim, crs, srid, joinedTable, nilMapping );
    }

    private FeatureMapping buildMapping( QTableName currentTable, FeatureParticleJAXB config ) {
        LOG.warn( "Unhandled feature particle mapping" );
        return null;
    }

    private CompoundMapping buildMapping( QTableName currentTable, ComplexParticleJAXB config ) {
        PropertyName path = new PropertyName( config.getPath(), nsBindings );
        List<JAXBElement<? extends AbstractParticleJAXB>> children = config.getAbstractParticle();
        List<Mapping> particles = new ArrayList<Mapping>( children.size() );
        for ( JAXBElement<? extends AbstractParticleJAXB> child : children ) {
            Mapping particle = buildMapping( currentTable, child.getValue() );
            if ( particle != null ) {
                particles.add( particle );
            }
        }
        JoinChain joinedTable = buildJoinTable( currentTable, config.getJoinedTable() );
        DBField nilMapping = buildNilMapping( config.getNilMapping() );
        return new CompoundMapping( path, particles, joinedTable, nilMapping );
    }

    private DBField buildNilMapping( String nilMapping ) {
        if ( nilMapping != null ) {
            return new DBField( nilMapping );
        }
        return null;
    }
}