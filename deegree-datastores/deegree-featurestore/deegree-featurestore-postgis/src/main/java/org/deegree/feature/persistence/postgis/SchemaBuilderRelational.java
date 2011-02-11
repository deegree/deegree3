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
import static org.deegree.commons.tom.primitive.PrimitiveType.determinePrimitiveType;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_3;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.GEOMETRY;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.LINE_STRING;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.MULTI_GEOMETRY;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.MULTI_LINE_STRING;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.MULTI_POINT;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.MULTI_POLYGON;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.POINT;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.POLYGON;
import static org.deegree.feature.types.property.ValueRepresentation.INLINE;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.QTableName;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.mapping.FeatureTypeMapping;
import org.deegree.feature.persistence.mapping.JoinChain;
import org.deegree.feature.persistence.mapping.MappedApplicationSchema;
import org.deegree.feature.persistence.mapping.antlr.FMLLexer;
import org.deegree.feature.persistence.mapping.antlr.FMLParser;
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
import org.deegree.feature.persistence.postgis.jaxb.AbstractIDGeneratorType;
import org.deegree.feature.persistence.postgis.jaxb.AbstractPropertyDecl;
import org.deegree.feature.persistence.postgis.jaxb.CustomMapping;
import org.deegree.feature.persistence.postgis.jaxb.FIDMapping.Column;
import org.deegree.feature.persistence.postgis.jaxb.FeatureTypeDecl;
import org.deegree.feature.persistence.postgis.jaxb.GeometryPropertyDecl;
import org.deegree.feature.persistence.postgis.jaxb.SimplePropertyDecl;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.MappingExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates {@link MappedApplicationSchema} instances from feature type declarations / relational mapping information.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class SchemaBuilderRelational {

    private static final Logger LOG = LoggerFactory.getLogger( SchemaBuilderRelational.class );

    private final String connId;

    private Map<QName, FeatureType> ftNameToFt = new HashMap<QName, FeatureType>();

    private Map<QName, FeatureTypeMapping> ftNameToMapping = new HashMap<QName, FeatureTypeMapping>();

    private NamespaceBindings nsContext = null;

    private Connection conn;

    private DatabaseMetaData md;

    // caches the column information
    private Map<String, LinkedHashMap<String, ColumnMetadata>> tableNameToColumns = new HashMap<String, LinkedHashMap<String, ColumnMetadata>>();

    /**
     * Creates a new {@link SchemaBuilderRelational} instance.
     * 
     * @param jdbcConnId
     *            identifier of JDBC connection, must not be <code>null</code>
     * @param ftDecls
     *            JAXB feature type declarations, must not be <code>null</code>
     * @throws SQLException
     * @throws FeatureStoreException
     */
    SchemaBuilderRelational( String jdbcConnId, List<FeatureTypeDecl> ftDecls ) throws SQLException,
                            FeatureStoreException {

        this.connId = jdbcConnId;
        try {
            for ( FeatureTypeDecl ftDecl : ftDecls ) {
                process( ftDecl );
            }
        } finally {
            JDBCUtils.close( conn );
        }
    }

    /**
     * Returns the {@link MappedApplicationSchema} derived from configuration / tables.
     * 
     * @return mapped application schema, never <code>null</code>
     */
    MappedApplicationSchema getMappedSchema() {
        FeatureType[] fts = ftNameToFt.values().toArray( new FeatureType[ftNameToFt.size()] );
        FeatureTypeMapping[] ftMappings = ftNameToMapping.values().toArray( new FeatureTypeMapping[ftNameToMapping.size()] );
        return new MappedApplicationSchema( fts, null, null, null, ftMappings, null, null, null );
    }

    private void process( FeatureTypeDecl ftDecl )
                            throws FeatureStoreException, SQLException {

        if ( ftDecl.getTable() == null || ftDecl.getTable().isEmpty() ) {
            String msg = "Feature type element without or with empty table attribute.";
            throw new FeatureStoreException( msg );
        }

        QTableName table = new QTableName( ftDecl.getTable() );

        LOG.debug( "Processing feature type mapping for table '" + table + "'." );
        QName ftName = ftDecl.getName();
        if ( ftName == null ) {
            LOG.debug( "Using table name for feature type." );
            ftName = new QName( table.getTable() );
        }
        ftName = makeFullyQualified( ftName, "app", "http://www.deegree.org/app" );
        LOG.debug( "Feature type name: '" + ftName + "'." );

        FIDMapping fidMapping = buildFIDMapping( table, ftName, ftDecl.getFIDMapping() );

        List<JAXBElement<? extends AbstractPropertyDecl>> propDecls = ftDecl.getAbstractProperty();
        if ( propDecls != null && !propDecls.isEmpty() ) {
            process( table, ftName, fidMapping, propDecls );
        } else {
            process( table, ftName, fidMapping );
        }
    }

    private void process( QTableName table, QName ftName, FIDMapping fidMapping )
                            throws SQLException {

        LOG.debug( "Deriving properties and mapping for feature type '" + ftName + "' from table '" + table + "'" );

        List<PropertyType> pts = new ArrayList<PropertyType>();
        Map<QName, Mapping> propToColumn = new HashMap<QName, Mapping>();

        for ( ColumnMetadata md : getColumns( table ).values() ) {
            if ( md.column.equalsIgnoreCase( fidMapping.getColumn() ) ) {
                LOG.debug( "Omitting column '" + md.column + "' from properties. Used in FIDMapping." );
                continue;
            }

            DBField dbField = new DBField( md.column );
            QName ptName = makeFullyQualified( new QName( md.column ), ftName.getPrefix(), ftName.getNamespaceURI() );
            if ( md.geomType == null ) {
                try {
                    PrimitiveType type = PrimitiveType.determinePrimitiveType( md.sqlType );
                    PropertyType pt = new SimplePropertyType( ptName, 0, 1, type, false, false, null );
                    pts.add( pt );
                    PropertyName path = new PropertyName( ptName );
                    PrimitiveMapping mapping = new PrimitiveMapping( path, dbField, type, null );
                    propToColumn.put( ptName, mapping );
                } catch ( IllegalArgumentException e ) {
                    LOG.warn( "Skipping column with type code '" + md.sqlType + "' from list of properties:"
                              + e.getMessage() );
                }
            } else {
                PropertyType pt = new GeometryPropertyType( ptName, 0, 1, false, false, null, md.geomType, md.dim,
                                                            INLINE );
                pts.add( pt );
                PropertyName path = new PropertyName( ptName );
                GeometryMapping mapping = new GeometryMapping( path, dbField, md.geomType, md.dim, md.crs, md.srid,
                                                               null );
                propToColumn.put( ptName, mapping );
            }
        }

        FeatureType ft = new GenericFeatureType( ftName, pts, false );
        ftNameToFt.put( ftName, ft );

        FeatureTypeMapping ftMapping = new FeatureTypeMapping( ftName, table, fidMapping, propToColumn );
        ftNameToMapping.put( ftName, ftMapping );
    }

    private void process( QTableName table, QName ftName, FIDMapping fidMapping,
                          List<JAXBElement<? extends AbstractPropertyDecl>> propDecls )
                            throws FeatureStoreException, SQLException {

        List<PropertyType> pts = new ArrayList<PropertyType>();
        Map<QName, Mapping> propToColumn = new HashMap<QName, Mapping>();

        for ( JAXBElement<? extends AbstractPropertyDecl> propDeclEl : propDecls ) {
            AbstractPropertyDecl propDecl = propDeclEl.getValue();
            Pair<PropertyType, Mapping> pt = process( table, propDecl );
            pts.add( pt.first );
            propToColumn.put( pt.first.getName(), pt.second );
        }

        FeatureType ft = new GenericFeatureType( ftName, pts, false );
        ftNameToFt.put( ftName, ft );

        FeatureTypeMapping ftMapping = new FeatureTypeMapping( ftName, table, fidMapping, propToColumn );
        ftNameToMapping.put( ftName, ftMapping );
    }

    private Pair<PropertyType, Mapping> process( QTableName table, AbstractPropertyDecl propDecl )
                            throws FeatureStoreException, SQLException {

        MappingExpression mapping = parseMappingExpression( propDecl.getMapping() );
        if ( !( mapping instanceof DBField ) ) {
            throw new FeatureStoreException( "Unhandled mapping type '" + mapping.getClass()
                                             + "'. Currently, only DBFields are supported." );
        }

        String columnName = ( (DBField) mapping ).getColumn();
        QName propName = propDecl.getName();
        if ( propName == null ) {
            LOG.debug( "Using column name for feature type." );
            propName = new QName( columnName );
        }
        propName = makeFullyQualified( propName, "app", "http://www.deegree.org/app" );
        PropertyName path = new PropertyName( propName );
        ColumnMetadata md = getColumn( table, columnName.toLowerCase() );
        int minOccurs = md.isNullable ? 0 : 1;

        PropertyType pt = null;
        Mapping m = null;
        if ( propDecl instanceof SimplePropertyDecl ) {
            SimplePropertyDecl simpleDecl = (SimplePropertyDecl) propDecl;
            PrimitiveType primType = null;
            if ( simpleDecl.getType() != null ) {
                primType = getPrimitiveType( simpleDecl.getType() );
            } else {
                primType = determinePrimitiveType( md.sqlType );
            }
            pt = new SimplePropertyType( propName, minOccurs, 1, primType, false, false, null );
            m = new PrimitiveMapping( path, mapping, primType, null );
        } else if ( propDecl instanceof GeometryPropertyDecl ) {
            GeometryPropertyDecl geomDecl = (GeometryPropertyDecl) propDecl;
            GeometryType type = null;
            if ( geomDecl.getType() != null ) {
                type = GeometryType.fromGMLTypeName( geomDecl.getType().name() );
            } else {
                type = md.geomType;
            }
            ICRS crs = null;
            if ( geomDecl.getCrs() != null ) {
                crs = CRSManager.getCRSRef( geomDecl.getCrs() );
            } else {
                crs = md.crs;
            }
            String srid = null;
            if ( geomDecl.getSrid() != null ) {
                srid = geomDecl.getSrid().toString();
            } else {
                srid = md.srid;
            }
            CoordinateDimension dim = null;
            if ( geomDecl.getDim() != null ) {
                // TODO why does JAXB return a list here?
                dim = DIM_2;
            } else {
                dim = md.dim;
            }
            pt = new GeometryPropertyType( propName, minOccurs, 1, false, false, null, type, dim, INLINE );
            m = new GeometryMapping( path, mapping, type, dim, crs, srid, null );
        } else {
            throw new FeatureStoreException( "Unhandled property declaration '" + propDecl.getClass()
                                             + "'. Currently, only simple / geometry properties are supported." );
        }
        return new Pair<PropertyType, Mapping>( pt, m );
    }

    private FIDMapping buildFIDMapping( QTableName table, QName ftName,
                                        org.deegree.feature.persistence.postgis.jaxb.FIDMapping config )
                            throws FeatureStoreException, SQLException {

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
            } else {
                // determine autoincrement column automatically
                for ( ColumnMetadata md : getColumns( table ).values() ) {
                    if ( md.isAutoincrement ) {
                        columnName = md.column;
                        break;
                    }
                }
                if ( columnName == null ) {
                    throw new FeatureStoreException( "No autoincrement column in table '" + table
                                                     + "' found. Please specify in FIDMapping." );
                }
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
        } else {
            ColumnMetadata md = getColumn( table, columnName.toLowerCase() );
            pt = PrimitiveType.determinePrimitiveType( md.sqlType );
        }
        return new FIDMapping( prefix, columnName, pt, generator );
    }

    private IDGenerator buildGenerator( org.deegree.feature.persistence.postgis.jaxb.FIDMapping fidMappingConfig ) {

        AbstractIDGeneratorType config = null;
        if ( fidMappingConfig != null ) {
            config = fidMappingConfig.getAbstractIDGenerator().getValue();
        }

        if ( config == null || config instanceof org.deegree.feature.persistence.postgis.jaxb.AutoIdGenerator ) {
            return new AutoIDGenerator();
        } else if ( config instanceof org.deegree.feature.persistence.postgis.jaxb.SequenceIDGenerator ) {
            String sequence = ( (org.deegree.feature.persistence.postgis.jaxb.SequenceIDGenerator) config ).getSequence();
            return new SequenceIDGenerator( sequence );
        } else if ( config instanceof org.deegree.feature.persistence.postgis.jaxb.UUIDGenerator ) {
            return new UUIDGenerator();
        }
        throw new RuntimeException( "Internal error. Unhandled JAXB config bean: " + config.getClass() );
    }

    private List<Mapping> process( List<JAXBElement<? extends CustomMapping>> customMappings ) {
        List<Mapping> mappings = new ArrayList<Mapping>( customMappings.size() );
        for ( JAXBElement<? extends CustomMapping> customMappingEl : customMappings ) {
            CustomMapping customMapping = customMappingEl.getValue();

            String path = customMapping.getPath();
            MappingExpression mapping = null;
            if ( customMapping.getMapping() != null ) {
                ANTLRStringStream in = new ANTLRStringStream( customMapping.getMapping() );
                FMLLexer lexer = new FMLLexer( in );
                CommonTokenStream tokens = new CommonTokenStream( lexer );
                FMLParser parser = new FMLParser( tokens );
                try {
                    mapping = parser.mappingExpr().value;
                } catch ( RecognitionException e ) {
                    LOG.warn( "Unable to parse mapping expression '" + customMapping.getMapping() + "'" );
                }
            }

            PropertyName propName = new PropertyName( path, nsContext );
            if ( customMapping instanceof org.deegree.feature.persistence.postgis.jaxb.PrimitiveMapping ) {
                org.deegree.feature.persistence.postgis.jaxb.PrimitiveMapping primitiveMapping = (org.deegree.feature.persistence.postgis.jaxb.PrimitiveMapping) customMapping;
                mappings.add( new PrimitiveMapping( propName, mapping, getPrimitiveType( primitiveMapping.getType() ),
                                                    null ) );
            } else if ( customMapping instanceof org.deegree.feature.persistence.postgis.jaxb.GeometryMapping ) {
                org.deegree.feature.persistence.postgis.jaxb.GeometryMapping geometryMapping = (org.deegree.feature.persistence.postgis.jaxb.GeometryMapping) customMapping;
                mappings.add( new GeometryMapping( propName, mapping, GEOMETRY, DIM_2,
                                                   CRSManager.getCRSRef( "EPSG:4326", true ), "-1", null ) );
            } else if ( customMapping instanceof org.deegree.feature.persistence.postgis.jaxb.FeatureMapping ) {
                org.deegree.feature.persistence.postgis.jaxb.FeatureMapping featureMapping = (org.deegree.feature.persistence.postgis.jaxb.FeatureMapping) customMapping;
                mappings.add( new FeatureMapping( propName, mapping, featureMapping.getType(), null ) );
            } else if ( customMapping instanceof org.deegree.feature.persistence.postgis.jaxb.CustomMapping ) {
                org.deegree.feature.persistence.postgis.jaxb.ComplexMapping compoundMapping = (org.deegree.feature.persistence.postgis.jaxb.ComplexMapping) customMapping;
                List<Mapping> particles = process( compoundMapping.getAbstractCustomMapping() );
                JoinChain joinedTable = null;
                if ( compoundMapping.getJoinedTable() != null ) {

                    joinedTable = (JoinChain) parseMappingExpression( compoundMapping.getJoinedTable().getValue() );
                }
                mappings.add( new CompoundMapping( propName, mapping, particles, joinedTable ) );
            } else {
                throw new RuntimeException( "Internal error. Unexpected JAXB type '" + customMapping.getClass() + "'." );
            }
        }
        return mappings;
    }

    private PrimitiveType getPrimitiveType( org.deegree.feature.persistence.postgis.jaxb.PrimitiveType type ) {
        switch ( type ) {
        case BOOLEAN:
            return PrimitiveType.BOOLEAN;
        case DATE:
            return PrimitiveType.DATE;
        case DATE_TIME:
            return PrimitiveType.DATE_TIME;
        case DECIMAL:
            return PrimitiveType.DECIMAL;
        case DOUBLE:
            return PrimitiveType.DOUBLE;
        case INTEGER:
            return PrimitiveType.INTEGER;
        case STRING:
            return PrimitiveType.STRING;
        case TIME:
            return PrimitiveType.TIME;
        }
        throw new RuntimeException( "Internal error: Unhandled JAXB primitive type: " + type );
    }

    private GeometryType getGeometryType( String pgType ) {
        if ( "GEOMETRY".equals( pgType ) ) {
            return GEOMETRY;
        } else if ( "POINT".equals( pgType ) ) {
            return POINT;
        } else if ( "LINESTRING".equals( pgType ) ) {
            return LINE_STRING;
        } else if ( "POLYGON".equals( pgType ) ) {
            return POLYGON;
        } else if ( "MULTIPOINT".equals( pgType ) ) {
            return MULTI_POINT;
        } else if ( "MULTILINESTRING".equals( pgType ) ) {
            return MULTI_LINE_STRING;
        } else if ( "MULTIPOLYGON".equals( pgType ) ) {
            return MULTI_POLYGON;
        } else if ( "GEOMETRYCOLLECTION".equals( pgType ) ) {
            return MULTI_GEOMETRY;
        }
        LOG.warn( "Unknown PostGIS geometry type '" + pgType + "'. Interpreting as generic geometry." );
        return GEOMETRY;
    }

    private MappingExpression parseMappingExpression( String s ) {
        MappingExpression mapping = null;
        if ( s != null ) {
            ANTLRStringStream in = new ANTLRStringStream( s );
            FMLLexer lexer = new FMLLexer( in );
            CommonTokenStream tokens = new CommonTokenStream( lexer );
            FMLParser parser = new FMLParser( tokens );
            try {
                mapping = parser.mappingExpr().value;
            } catch ( RecognitionException e ) {
                LOG.warn( "Unable to parse mapping expression '" + s + "': " + e.getMessage() );
            }
        }
        return mapping;
    }

    private QName makeFullyQualified( QName qName, String defaultPrefix, String defaultNamespace ) {
        String prefix = qName.getPrefix();
        String namespace = qName.getNamespaceURI();
        String localPart = qName.getLocalPart();
        if ( DEFAULT_NS_PREFIX.equals( prefix ) ) {
            prefix = defaultPrefix;
            namespace = defaultNamespace;
        }
        if ( NULL_NS_URI.equals( namespace ) ) {
            namespace = defaultNamespace;
        }
        return new QName( namespace, localPart, prefix );
    }

    private Connection getConnection()
                            throws SQLException {
        if ( conn == null ) {
            conn = ConnectionManager.getConnection( connId );
        }
        return conn;
    }

    private DatabaseMetaData getDBMetadata()
                            throws SQLException {
        if ( md == null ) {
            Connection conn = getConnection();
            md = conn.getMetaData();
        }
        return md;
    }

    private ColumnMetadata getColumn( QTableName qTable, String columnName )
                            throws SQLException, FeatureStoreException {
        ColumnMetadata md = getColumns( qTable ).get( columnName.toLowerCase() );
        if ( md == null ) {
            throw new FeatureStoreException( "Table '" + qTable + "' does not have a column with name '" + columnName
                                             + "'" );
        }
        return md;
    }

    private LinkedHashMap<String, ColumnMetadata> getColumns( QTableName qTable )
                            throws SQLException {

        LinkedHashMap<String, ColumnMetadata> columnNameToMD = tableNameToColumns.get( qTable.toString().toLowerCase() );

        if ( columnNameToMD == null ) {
            DatabaseMetaData md = getDBMetadata();
            columnNameToMD = new LinkedHashMap<String, ColumnMetadata>();
            ResultSet rs = null;
            try {
                LOG.debug( "Analyzing metadata for table {}", qTable );
                String dbSchema = qTable.getSchema() != null ? qTable.getSchema() : "public";
                String table = qTable.getTable();
                rs = md.getColumns( null, dbSchema, table.toLowerCase(), "%" );
                while ( rs.next() ) {
                    String column = rs.getString( 4 );
                    int sqlType = rs.getInt( 5 );
                    String sqlTypeName = rs.getString( 6 );
                    boolean isNullable = "YES".equals( rs.getString( 18 ) );
                    boolean isAutoincrement = "YES".equals( rs.getString( 23 ) );
                    LOG.debug( "Found column '" + column + "', typeName: '" + sqlTypeName + "', typeCode: '" + sqlType
                               + "', isNullable: '" + isNullable + "', isAutoincrement:' " + isAutoincrement + "'" );

                    if ( sqlTypeName.equals( "geometry" ) ) {
                        String srid = "-1";
                        ICRS crs = CRSManager.getCRSRef( "EPSG:4326", true );
                        CoordinateDimension dim = DIM_2;
                        GeometryPropertyType.GeometryType geomType = GeometryType.GEOMETRY;
                        Connection conn = getConnection();
                        Statement stmt = null;
                        ResultSet rs2 = null;
                        try {
                            stmt = conn.createStatement();
                            String sql = "SELECT coord_dimension,srid,type FROM public.geometry_columns WHERE f_table_schema='"
                                         + dbSchema.toLowerCase()
                                         + "' AND f_table_name='"
                                         + table.toLowerCase()
                                         + "' AND f_geometry_column='" + column.toLowerCase() + "'";
                            rs2 = stmt.executeQuery( sql );
                            rs2.next();
                            if ( rs2.getInt( 2 ) != -1 ) {
                                crs = CRSManager.lookup( "EPSG:" + rs2.getInt( 2 ), true );
                            }
                            if ( rs2.getInt( 1 ) == 3 ) {
                                dim = DIM_3;
                            }
                            srid = "" + rs2.getInt( 2 );
                            geomType = getGeometryType( rs2.getString( 3 ) );
                            LOG.debug( "Derived geometry type: " + geomType + ", crs: " + crs + ", srid: " + srid
                                       + ", dim: " + dim + "" );
                        } catch ( Exception e ) {
                            LOG.warn( "Unable to determine geometry column details: " + e.getMessage()
                                      + ". Using defaults." );
                        } finally {
                            JDBCUtils.close( rs2, stmt, null, LOG );
                        }
                        ColumnMetadata columnMd = new ColumnMetadata( column, sqlType, sqlTypeName, isNullable,
                                                                      geomType, dim, crs, srid );
                        columnNameToMD.put( column.toLowerCase(), columnMd );
                    } else {
                        ColumnMetadata columnMd = new ColumnMetadata( column, sqlType, sqlTypeName, isNullable,
                                                                      isAutoincrement );
                        columnNameToMD.put( column.toLowerCase(), columnMd );
                    }
                }
                tableNameToColumns.put( qTable.toString().toLowerCase(), columnNameToMD );
            } finally {
                JDBCUtils.close( rs );
            }
        }
        return columnNameToMD;
    }
}

class ColumnMetadata {

    String column;

    int sqlType;

    String sqlTypeName;

    boolean isNullable;

    boolean isAutoincrement;

    GeometryType geomType;

    CoordinateDimension dim;

    ICRS crs;

    String srid;

    ColumnMetadata( String column, int sqlType, String sqlTypeName, boolean isNullable, boolean isAutoincrement ) {
        this.column = column;
        this.sqlType = sqlType;
        this.sqlTypeName = sqlTypeName;
        this.isNullable = isNullable;
        this.isAutoincrement = isAutoincrement;
    }

    public ColumnMetadata( String column, int sqlType, String sqlTypeName, boolean isNullable, GeometryType geomType,
                           CoordinateDimension dim, ICRS crs, String srid ) {
        this.column = column;
        this.sqlType = sqlType;
        this.sqlTypeName = sqlTypeName;
        this.isNullable = isNullable;
        this.geomType = geomType;
        this.dim = dim;
        this.crs = crs;
        this.srid = srid;
    }
}