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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.QTableName;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.feature.persistence.mapping.FeatureTypeMapping;
import org.deegree.feature.persistence.mapping.JoinChain;
import org.deegree.feature.persistence.mapping.MappedApplicationSchema;
import org.deegree.feature.persistence.mapping.property.CompoundMapping;
import org.deegree.feature.persistence.mapping.property.FeatureMapping;
import org.deegree.feature.persistence.mapping.property.GeometryMapping;
import org.deegree.feature.persistence.mapping.property.Mapping;
import org.deegree.feature.persistence.mapping.property.PrimitiveMapping;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.MappingExpression;

/**
 * Creates DDL (DataDefinitionLanguage) scripts for the {@link PostGISFeatureStoreProvider}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISDDLCreator {

    private final MappedApplicationSchema schema;

    /**
     * @param schema
     */
    public PostGISDDLCreator( MappedApplicationSchema schema ) {
        this.schema = schema;
    }

    /**
     * @return
     */
    public String[] getDDL() {

        List<String> ddl = getBLOBCreates();
        for ( StringBuffer sb : getRelationalCreates() ) {
            ddl.add( sb.toString() );
        }

        return ddl.toArray( new String[ddl.size()] );
    }

    public List<String> getBLOBCreates() {

        List<String> ddl = new ArrayList<String>();

        // create feature_type table
        QTableName ftTable = schema.getBBoxMapping().getTable();
        String ftTableSchema = ftTable.getSchema() == null ? "public" : ftTable.getSchema();
        ddl.add( "CREATE TABLE " + ftTable + " (id smallint PRIMARY KEY, qname text NOT NULL)" );
        ddl.add( "COMMENT ON TABLE " + ftTable + " IS 'Ids and bboxes of concrete feature types'" );
        ddl.add( "SELECT ADDGEOMETRYCOLUMN('" + ftTableSchema.toLowerCase() + "', '" + ftTable.getTable().toLowerCase()
                 + "','bbox','-1','GEOMETRY',2)" );

        // populate feature_type table
        for ( short ftId = 0; ftId < schema.getFts(); ftId++ ) {
            QName ftName = schema.getFtName( ftId );
            ddl.add( "INSERT INTO " + ftTable + "  (id,qname) VALUES (" + ftId + ",'" + ftName + "')" );
        }

        // create gml_objects table
        QTableName blobTable = schema.getBlobMapping().getTable();
        String blobTableSchema = blobTable.getSchema() == null ? "public" : blobTable.getSchema();
        ddl.add( "CREATE TABLE " + blobTable + " (id serial PRIMARY KEY, "
                 + "gml_id text UNIQUE NOT NULL, ft_type smallint REFERENCES " + ftTable + " , binary_object bytea)" );
        ddl.add( "COMMENT ON TABLE " + blobTable + " IS 'All objects (features and geometries)'" );
        ddl.add( "SELECT ADDGEOMETRYCOLUMN('" + blobTableSchema.toLowerCase() + "', '" + blobTable.getTable().toLowerCase()
                 + "','gml_bounded_by','-1','GEOMETRY',2)" );
        ddl.add( "ALTER TABLE " + blobTable + " ADD CONSTRAINT gml_objects_geochk CHECK (isvalid(gml_bounded_by))" );
        ddl.add( "CREATE INDEX gml_objects_sidx ON " + blobTable + "  USING GIST (gml_bounded_by GIST_GEOMETRY_OPS)" );
        // ddl.add( "CREATE TABLE gml_names (gml_object_id integer REFERENCES gml_objects,"
        // + "name text NOT NULL,codespace text,prop_idx smallint NOT NULL)" );
        return ddl;
    }

    public List<StringBuffer> getRelationalCreates() {

        List<StringBuffer> ddl = new ArrayList<StringBuffer>();

        for ( short ftId = 0; ftId < schema.getFts(); ftId++ ) {
            QName ftName = schema.getFtName( ftId );
            FeatureType ft = schema.getFeatureType( ftName );
            FeatureTypeMapping ftMapping = schema.getMapping( ftName );
            if ( ftMapping != null ) {
                ddl.addAll( process( ft, ftMapping ) );
            }
        }
        return ddl;
    }

    private List<StringBuffer> process( FeatureType ft, FeatureTypeMapping ftMapping ) {

        List<StringBuffer> ddls = new ArrayList<StringBuffer>();

        StringBuffer sql = new StringBuffer( "CREATE TABLE " );
        ddls.add( sql );
        sql.append( ftMapping.getFtTable() );
        sql.append( " (\n    " );
        sql.append( "id integer PRIMARY KEY REFERENCES gml_objects" );
        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            Mapping propMapping = ftMapping.getMapping( pt.getName() );
            if ( propMapping != null ) {
                ddls.addAll( process( sql, ftMapping.getFtTable(), propMapping ) );
            }
        }
        sql.append( "\n)" );
        return ddls;
    }

    private List<StringBuffer> process( StringBuffer sql, QTableName table, Mapping propMapping ) {

        List<StringBuffer> ddls = new ArrayList<StringBuffer>();

        MappingExpression me = propMapping.getMapping();

        JoinChain jc = propMapping.getJoinedTable();
        if ( jc != null ) {
            sql = createJoinedTable( table, jc );
            table = new QTableName( jc.getFields().get( 1 ).getTable() );
            ddls.add( sql );
        }

        if ( propMapping instanceof PrimitiveMapping ) {
            PrimitiveMapping primitiveMapping = (PrimitiveMapping) propMapping;
            if ( me instanceof DBField ) {
                DBField dbField = (DBField) me;
                sql.append( ",\n    " );
                sql.append( dbField.getColumn() );
                sql.append( " " );
                sql.append( getPostgreSQLType( primitiveMapping.getType() ) );
            }
        } else if ( propMapping instanceof GeometryMapping ) {
            GeometryMapping geometryMapping = (GeometryMapping) propMapping;
            if ( me instanceof DBField ) {
                DBField dbField = (DBField) me;
                sql.append( ",\n    " );
                sql.append( dbField.getColumn() );
                sql.append( " geometry" );
            }
        } else if ( propMapping instanceof FeatureMapping ) {
            if ( me instanceof DBField ) {
                sql.append( ",\n    " );
                sql.append( ( (DBField) me ).getColumn() );
                sql.append( " integer" );
            }
        } else if ( propMapping instanceof CompoundMapping ) {
            CompoundMapping compoundMapping = (CompoundMapping) propMapping;
            ddls.addAll( process( sql, table, compoundMapping ) );
        } else {
            throw new RuntimeException( "Internal error. Unhandled mapping type '" + propMapping.getClass() + "'" );
        }

        if ( jc != null ) {
            sql.append( "\n)" );
        }
        return ddls;
    }

    private List<StringBuffer> process( StringBuffer sb, QTableName table, CompoundMapping cm ) {

        List<StringBuffer> ddls = new ArrayList<StringBuffer>();
        for ( Mapping mapping : cm.getParticles() ) {
            if ( mapping instanceof PrimitiveMapping ) {
                PrimitiveMapping primitiveMapping = (PrimitiveMapping) mapping;
                MappingExpression me = primitiveMapping.getMapping();
                if ( me instanceof DBField ) {
                    DBField dbField = (DBField) me;
                    sb.append( ",\n    " );
                    sb.append( dbField.getColumn() );
                    sb.append( " " );
                    sb.append( getPostgreSQLType( primitiveMapping.getType() ) );
                }
            } else if ( mapping instanceof GeometryMapping ) {

            } else if ( mapping instanceof FeatureMapping ) {

            } else if ( mapping instanceof CompoundMapping ) {
                CompoundMapping compoundMapping = (CompoundMapping) mapping;
                JoinChain jc = compoundMapping.getJoinedTable();
                if ( jc != null ) {
                    StringBuffer newSb = createJoinedTable( table, jc );
                    ddls.add( newSb );
                    for ( Mapping particle : compoundMapping.getParticles() ) {
                        ddls.addAll( process( newSb, new QTableName( jc.getFields().get( 1 ).getTable() ), particle ) );
                    }
                } else {
                    for ( Mapping particle : compoundMapping.getParticles() ) {
                        ddls.addAll( process( sb, table, particle ) );
                    }
                }
            } else {
                throw new RuntimeException( "Internal error. Unhandled mapping type '" + mapping.getClass() + "'" );
            }
        }
        return ddls;
    }

    private StringBuffer createJoinedTable( QTableName fromTable, JoinChain jc ) {

        DBField to = jc.getFields().get( 1 );

        StringBuffer sb = new StringBuffer( "CREATE TABLE " );
        sb.append( to.getTable() );
        sb.append( " (\n    " );
        sb.append( "id serial PRIMARY KEY,\n    " );
        sb.append( to.getColumn() );
        sb.append( " integer NOT NULL REFERENCES" );
        sb.append( " " );
        sb.append( fromTable );
        return sb;
    }

    private String getPostgreSQLType( PrimitiveType type ) {
        String postgresqlType = null;
        switch ( type ) {
        case BOOLEAN:
            postgresqlType = "boolean";
            break;
        case DATE:
            postgresqlType = "date";
            break;
        case DATE_TIME:
            postgresqlType = "timestamp";
            break;
        case DECIMAL:
            postgresqlType = "numeric";
            break;
        case DOUBLE:
            postgresqlType = "float";
            break;
        case INTEGER:
            postgresqlType = "integer";
            break;
        case STRING:
            postgresqlType = "text";
            break;
        case TIME:
            postgresqlType = "time";
            break;
        default:
            throw new RuntimeException( "Internal error. Unhandled primitive type '" + type + "'." );
        }
        return postgresqlType;
    }
}