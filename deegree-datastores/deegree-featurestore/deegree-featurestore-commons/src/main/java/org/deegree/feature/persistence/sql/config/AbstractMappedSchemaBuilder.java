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

import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.GEOMETRY;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.LINE_STRING;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.MULTI_GEOMETRY;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.MULTI_LINE_STRING;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.MULTI_POINT;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.MULTI_POLYGON;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.POINT;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.POLYGON;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.deegree.commons.jdbc.QTableName;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.sql.SQLDialectHelper;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.mapping.antlr.FMLLexer;
import org.deegree.feature.persistence.mapping.antlr.FMLParser;
import org.deegree.feature.persistence.sql.MappedApplicationSchema;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.id.AutoIDGenerator;
import org.deegree.feature.persistence.sql.id.IDGenerator;
import org.deegree.feature.persistence.sql.id.SequenceIDGenerator;
import org.deegree.feature.persistence.sql.id.UUIDGenerator;
import org.deegree.feature.persistence.sql.jaxb.AbstractIDGeneratorType;
import org.deegree.feature.persistence.sql.jaxb.AutoIdGenerator;
import org.deegree.feature.persistence.sql.jaxb.FIDMappingJAXB;
import org.deegree.feature.persistence.sql.jaxb.FeatureTypeMappingJAXB;
import org.deegree.feature.persistence.sql.jaxb.SQLFeatureStoreJAXB;
import org.deegree.feature.persistence.sql.jaxb.SQLFeatureStoreJAXB.BLOBMapping;
import org.deegree.feature.persistence.sql.jaxb.SQLFeatureStoreJAXB.NamespaceHint;
import org.deegree.feature.persistence.sql.jaxb.SQLFeatureStoreJAXB.StorageCRS;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.filter.sql.MappingExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for builders that create {@link MappedApplicationSchema} instances from JAXB configuration objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class AbstractMappedSchemaBuilder {

    private static Logger LOG = LoggerFactory.getLogger( AbstractMappedSchemaBuilder.class );

    public static MappedApplicationSchema build( String configURL, SQLFeatureStoreJAXB config, SQLDialectHelper dialect )
                            throws SQLException, FeatureStoreException {
        if ( config.getGMLSchema() == null || config.getGMLSchema().isEmpty() ) {
            MappedSchemaBuilderTable builder = new MappedSchemaBuilderTable( config.getJDBCConnId(),
                                                                             config.getFeatureType(), dialect );
            return builder.getMappedSchema();
        }
        List<String> gmlSchemas = config.getGMLSchema();
        StorageCRS storageCRS = config.getStorageCRS();
        List<NamespaceHint> nsHints = config.getNamespaceHint();
        BLOBMapping blobConf = config.getBLOBMapping();
        List<FeatureTypeMappingJAXB> ftMappingConfs = config.getFeatureTypeMapping();
        MappedSchemaBuilderGML builder = new MappedSchemaBuilderGML( configURL, gmlSchemas, storageCRS, nsHints,
                                                                     blobConf, ftMappingConfs );
        return builder.getMappedSchema();
    }

    protected IDGenerator buildGenerator( FIDMappingJAXB fidMappingConfig ) {

        AbstractIDGeneratorType config = null;
        if ( fidMappingConfig != null && fidMappingConfig.getAbstractIDGenerator() != null) {
            config = fidMappingConfig.getAbstractIDGenerator().getValue();
        }
        if ( config == null || config instanceof AutoIdGenerator ) {
            return new AutoIDGenerator();
        } else if ( config instanceof org.deegree.feature.persistence.sql.jaxb.SequenceIDGenerator ) {
            String sequence = ( (org.deegree.feature.persistence.sql.jaxb.SequenceIDGenerator) config ).getSequence();
            return new SequenceIDGenerator( sequence );
        } else if ( config instanceof org.deegree.feature.persistence.sql.jaxb.UUIDGenerator ) {
            return new UUIDGenerator();
        }
        throw new RuntimeException( "Internal error. Unhandled JAXB config bean: " + config.getClass() );
    }

    protected BaseType getPrimitiveType( org.deegree.feature.persistence.sql.jaxb.PrimitiveType type ) {
        switch ( type ) {
        case BOOLEAN:
            return BaseType.BOOLEAN;
        case DATE:
            return BaseType.DATE;
        case DATE_TIME:
            return BaseType.DATE_TIME;
        case DECIMAL:
            return BaseType.DECIMAL;
        case DOUBLE:
            return BaseType.DOUBLE;
        case INTEGER:
            return BaseType.INTEGER;
        case STRING:
            return BaseType.STRING;
        case TIME:
            return BaseType.TIME;
        }
        throw new RuntimeException( "Internal error: Unhandled JAXB primitive type: " + type );
    }

    protected GeometryType getGeometryType( String pgType ) {
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

    protected MappingExpression parseMappingExpression( String s ) {
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

    protected List<TableJoin> buildJoinTable( QTableName from, org.deegree.feature.persistence.sql.jaxb.Join join ) {
        if ( join != null ) {
            QTableName target = new QTableName( join.getTable() );
            if ( join.getFromColumns().size() != join.getToColumns().size() ) {
                throw new UnsupportedOperationException( "Joins must use same number of from and to columns." );
            }
            if ( join.getFromColumns().isEmpty() ) {
                throw new UnsupportedOperationException( "Joins must use at least a single column." );
            }
            boolean isNumbered = join.isNumbered() == null ? false : join.isNumbered();
            TableJoin tj = new TableJoin( from, target, join.getFromColumns(), join.getToColumns(),
                                          join.getOrderColumns(), isNumbered );
            return Collections.singletonList( tj );
        }
        return null;
    }
}