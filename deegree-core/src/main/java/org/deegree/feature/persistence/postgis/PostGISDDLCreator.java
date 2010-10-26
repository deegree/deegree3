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

import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.feature.persistence.mapping.DBField;
import org.deegree.feature.persistence.mapping.FeatureTypeMapping;
import org.deegree.feature.persistence.mapping.JoinChain;
import org.deegree.feature.persistence.mapping.MappedApplicationSchema;
import org.deegree.feature.persistence.mapping.MappingExpression;
import org.deegree.feature.persistence.mapping.property.CompoundMapping;
import org.deegree.feature.persistence.mapping.property.FeatureMapping;
import org.deegree.feature.persistence.mapping.property.GeometryMapping;
import org.deegree.feature.persistence.mapping.property.Mapping;
import org.deegree.feature.persistence.mapping.property.PrimitiveMapping;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.PropertyType;

/**
 * Handles the creation of DDL (DataDefinitionLanguage) scripts for the {@link PostGISFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISDDLCreator {

    private final MappedApplicationSchema schema;

    private int maxColumnLength = 64;

    private int columnId = 0;

    private int maxTableLength = 64;

    private int tableId = 0;

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
        ddl.addAll( getRelationalCreates() );

        return ddl.toArray( new String[ddl.size()] );
    }

    public List<String> getBLOBCreates() {

        List<String> ddl = new ArrayList<String>();

        // create feature_type table
        ddl.add( "CREATE TABLE feature_types (id smallint PRIMARY KEY, qname text NOT NULL)" );
        ddl.add( "COMMENT ON TABLE feature_types IS 'Ids and bboxes of concrete feature types'" );
        ddl.add( "SELECT ADDGEOMETRYCOLUMN('public', 'feature_types','bbox','-1','GEOMETRY',2);" );

        // populate feature_type table
        for ( short ftId = 0; ftId < schema.getFts(); ftId++ ) {
            QName ftName = schema.getFtName( ftId );
            ddl.add( "INSERT INTO feature_types (id,qname) VALUES (" + ftId + ",'" + ftName + "')" );
        }

        // create gml_objects table
        ddl.add( "CREATE TABLE gml_objects (id SERIAL PRIMARY KEY, "
                 + "gml_id text UNIQUE NOT NULL, ft_type smallint REFERENCES feature_types, binary_object bytea)" );
        ddl.add( "COMMENT ON TABLE gml_objects IS 'All objects (features and geometries)'" );
        ddl.add( "SELECT ADDGEOMETRYCOLUMN('public', 'gml_objects','gml_bounded_by','-1','GEOMETRY',2)" );
        ddl.add( "ALTER TABLE gml_objects ADD CONSTRAINT gml_objects_geochk CHECK (isvalid(gml_bounded_by))" );
        ddl.add( "CREATE INDEX gml_objects_sidx ON gml_objects USING GIST (gml_bounded_by GIST_GEOMETRY_OPS)" );
        // ddl.add( "CREATE TABLE gml_names (gml_object_id integer REFERENCES gml_objects,"
        // + "name text NOT NULL,codespace text,prop_idx smallint NOT NULL)" );
        return ddl;
    }

    public List<String> getRelationalCreates() {

        List<String> ddl = new ArrayList<String>();

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

    private List<String> process( FeatureType ft, FeatureTypeMapping ftMapping ) {

        List<String> ddl = new ArrayList<String>();
        List<String> additionalDDLs = new ArrayList<String>();

        StringBuffer sb = new StringBuffer( "CREATE TABLE " );
        sb.append( ftMapping.getFtTable() );
        sb.append( " (\n    " );
        sb.append( "id integer PRIMARY KEY REFERENCES gml_objects" );
        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            Mapping propMapping = ftMapping.getMapping( pt.getName() );
            if ( propMapping != null ) {
                process( ftMapping.getFtTable(), "", sb, propMapping, additionalDDLs );
            }
        }
        sb.append( "\n)" );
        ddl.add( sb.toString() );

        ddl.addAll( additionalDDLs );

        return ddl;
    }

    private void process( String table, String columnPrefix, StringBuffer sb, Mapping propMapping,
                          List<String> additionalDDLs ) {

        MappingExpression me = propMapping.getMapping();
        if ( propMapping instanceof PrimitiveMapping ) {
            PrimitiveMapping primitiveMapping = (PrimitiveMapping) propMapping;
        } else if ( propMapping instanceof GeometryMapping ) {
            GeometryMapping geometryMapping = (GeometryMapping) propMapping;
        } else if ( propMapping instanceof FeatureMapping ) {
            if ( me instanceof DBField ) {
                sb.append( "," );
                sb.append( getPrefixedColumn( columnPrefix, (DBField) me ) );
                sb.append( " integer" );
            } else if ( me instanceof JoinChain ) {
                additionalDDLs.add( createFeatureJoinTable( table, (JoinChain) me ) );
            } else {
                throw new RuntimeException( "Mapping expressions of type '" + me.getClass()
                                            + "' are not allowed for feature properties." );
            }
        } else if ( propMapping instanceof CompoundMapping ) {
            CompoundMapping compoundMapping = (CompoundMapping) propMapping;
            if ( me instanceof DBField ) {
                process( table, sb, getPrefixedColumn( columnPrefix, (DBField) me ), compoundMapping, additionalDDLs );
            } else if ( me instanceof JoinChain ) {
                process( table, sb, (JoinChain) me, compoundMapping, additionalDDLs );
            } else {
                throw new RuntimeException( "Mapping expressions of type '" + me.getClass()
                                            + "' are not allowed for custom properties." );
            }
        } else {
            throw new RuntimeException( "Internal error. Unhandled mapping type '" + propMapping.getClass() + "'" );
        }
    }

    private void process( String table, StringBuffer sb, String columnPrefix, CompoundMapping cm,
                          List<String> additionalDDLs ) {

        for ( Mapping mapping : cm.getParticles() ) {
            MappingExpression me = mapping.getMapping();
            if ( me == null ) {
                me = new DBField( "" );
            }
            if ( mapping instanceof PrimitiveMapping ) {
                PrimitiveMapping primitiveMapping = (PrimitiveMapping) mapping;
                if ( me instanceof DBField ) {
                    DBField dbField = (DBField) me;
                    sb.append( ",\n    " );
                    sb.append( getPrefixedColumn( columnPrefix, dbField ) );
                    sb.append( " " );
                    sb.append( getPostgreSQLType( primitiveMapping.getType() ) );
                } else {
                    throw new RuntimeException( "Mapping expressions of type '" + me.getClass()
                                                + "' are currently not supported for primitive mappings." );
                }
            } else if ( mapping instanceof GeometryMapping ) {

            } else if ( mapping instanceof FeatureMapping ) {

            } else if ( mapping instanceof CompoundMapping ) {
                CompoundMapping compoundMapping = (CompoundMapping) mapping;
                if ( me instanceof DBField ) {
                    DBField dbField = (DBField) me;
                    for ( Mapping particle : compoundMapping.getParticles() ) {
                        process( "TODO", getPrefixedColumn( columnPrefix, dbField ), sb, particle, additionalDDLs );
                    }
                } else {
                    throw new RuntimeException( "Mapping expressions of type '" + me.getClass()
                                                + "' are currently not supported for primitive mappings." );
                }
            } else {
                throw new RuntimeException( "Internal error. Unhandled mapping type '" + mapping.getClass() + "'" );
            }
        }
    }

    private void process( String table, StringBuffer sb, JoinChain jc, CompoundMapping cm, List<String> additionalDDLs ) {

        sb = new StringBuffer( "CREATE TABLE " );
        sb.append( jc.getFields().get( 1 ).getTable() );
        sb.append( " (\n    " );
        sb.append( "id integer PRIMARY KEY REFERENCES " );
        sb.append( table );

        table = jc.getFields().get( 1 ).getTable();

        for ( Mapping mapping : cm.getParticles() ) {
            MappingExpression me = mapping.getMapping();
            if ( me == null ) {
                me = new DBField( "" );
            }
            if ( mapping instanceof PrimitiveMapping ) {
                PrimitiveMapping primitiveMapping = (PrimitiveMapping) mapping;
                if ( me instanceof DBField ) {
                    DBField dbField = (DBField) me;
                    sb.append( ",\n    " );
                    sb.append( getPrefixedColumn( "", dbField ) );
                    sb.append( " " );
                    sb.append( getPostgreSQLType( primitiveMapping.getType() ) );
                } else {
                    throw new RuntimeException( "Mapping expressions of type '" + me.getClass()
                                                + "' are currently not supported for primitive mappings." );
                }
            } else if ( mapping instanceof GeometryMapping ) {

            } else if ( mapping instanceof FeatureMapping ) {

            } else if ( mapping instanceof CompoundMapping ) {
                CompoundMapping compoundMapping = (CompoundMapping) mapping;
                if ( me instanceof DBField ) {
                    DBField dbField = (DBField) me;
                    for ( Mapping particle : compoundMapping.getParticles() ) {
                        process( table, getPrefixedColumn( "", dbField ), sb, particle, additionalDDLs );
                    }
                } else {
                    throw new RuntimeException( "Mapping expressions of type '" + me.getClass()
                                                + "' are currently not supported for primitive mappings." );
                }
            } else {
                throw new RuntimeException( "Internal error. Unhandled mapping type '" + mapping.getClass() + "'" );
            }
        }

        sb.append( "\n)" );
        additionalDDLs.add( sb.toString() );
    }

    private String createFeatureJoinTable( String fromTable, JoinChain jc ) {
        DBField first = jc.getFields().get( 1 );
        DBField second = jc.getFields().get( 2 );

        StringBuffer sb = new StringBuffer( "CREATE TABLE " );
        sb.append( first.getTable() );
        sb.append( " (" );
        sb.append( first.getColumn() );
        sb.append( " integer NOT NULL REFERENCES" );
        sb.append( " " );
        sb.append( fromTable );
        sb.append( "," );
        sb.append( second.getColumn() );
        sb.append( " integer NOT NULL)" );
        return sb.toString();
    }

    private String getPrefixedColumn( String prefix, DBField dbField ) {
        String prefixedName = dbField.getColumn();
        if ( prefix != null && !prefix.isEmpty() ) {
            prefixedName = prefix + "_" + dbField.getColumn();
        }
        if ( prefixedName.length() > maxColumnLength ) {
            prefixedName = "TOO_LONG_" + ( columnId++ );
        }
        return prefixedName;
    }

    private String getPrefixedTable( String prefix, DBField dbField ) {
        String prefixedName = dbField.getTable();
        if ( prefix != null && !prefix.isEmpty() ) {
            prefixedName = prefix + "_" + dbField.getTable();
        }
        if ( prefixedName.length() > maxTableLength ) {
            prefixedName = "TOO_LONG_" + ( tableId++ );
        }
        return prefixedName;
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