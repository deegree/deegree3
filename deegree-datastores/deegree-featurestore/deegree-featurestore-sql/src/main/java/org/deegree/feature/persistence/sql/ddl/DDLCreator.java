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
package org.deegree.feature.persistence.sql.ddl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.QTableName;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.MappedApplicationSchema;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.id.AutoIDGenerator;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.FeatureMapping;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.sqldialect.SQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates DDL (DataDefinitionLanguage) scripts from {@link MappedApplicationSchema} instances.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class DDLCreator {

    private static Logger LOG = LoggerFactory.getLogger( DDLCreator.class );

    protected final MappedApplicationSchema schema;

    private final boolean hasBlobTable;

    private final SQLDialect dialect;

    protected QTableName currentFtTable;

    /**
     * Creates a new {@link DDLCreator} instance for the given {@link MappedApplicationSchema}.
     * 
     * @param schema
     *            mapped application schema, must not be <code>null</code>
     * @param dialect
     *            SQL dialect, must not be <code>null</code>
     */
    protected DDLCreator( MappedApplicationSchema schema, SQLDialect dialect ) {
        this.schema = schema;
        this.dialect = dialect;
        hasBlobTable = schema.getBlobMapping() != null;
    }

    /**
     * Returns the DDL statements for creating the relational schema required by the {@link MappedApplicationSchema}.
     * 
     * @return the DDL statements, never <code>null</code>
     */
    public String[] getDDL() {
        List<String> ddl = new ArrayList<String>();
        if ( hasBlobTable ) {
            ddl.addAll( getBLOBCreates() );
        }
        for ( StringBuffer sb : getRelationalCreates() ) {
            ddl.add( sb.toString() );
        }

        return ddl.toArray( new String[ddl.size()] );
    }

    protected abstract List<String> getBLOBCreates();

    private List<StringBuffer> getRelationalCreates() {
        List<StringBuffer> ddl = new ArrayList<StringBuffer>();

        for ( short ftId = 0; ftId < schema.getFts(); ftId++ ) {
            QName ftName = schema.getFtName( ftId );
            FeatureTypeMapping ftMapping = schema.getFtMapping( ftName );
            if ( ftMapping != null ) {
                ddl.addAll( process( ftMapping ) );
            }
        }
        return ddl;
    }

    private List<StringBuffer> process( FeatureTypeMapping ftMapping ) {
        List<StringBuffer> ddls = new ArrayList<StringBuffer>();

        currentFtTable = ftMapping.getFtTable();

        StringBuffer sql = new StringBuffer( "CREATE TABLE " );
        ddls.add( sql );
        sql.append( currentFtTable );
        sql.append( " (" );
        List<String> pkColumns = new ArrayList<String>();
        if ( hasBlobTable ) {
            sql.append( "\n    id " ).append( getDBType( BaseType.INTEGER ) ).append( " REFERENCES gml_objects" );
            pkColumns.add( "id" );
        } else {
            FIDMapping fidMapping = ftMapping.getFidMapping();
            if ( fidMapping.getIdGenerator() instanceof AutoIDGenerator ) {
                for ( Pair<String, BaseType> fidColumn : fidMapping.getColumns() ) {
                    sql.append( "\n    " );
                    dialect.createAutoColumn( sql, ddls, fidColumn.first, currentFtTable.toString() );
                    pkColumns.add( fidColumn.first );
                }
            } else {
                for ( Pair<String, BaseType> fidColumn : fidMapping.getColumns() ) {
                    sql.append( "\n    " );
                    sql.append( fidColumn.first );
                    sql.append( " " );
                    sql.append( getDBType( fidColumn.second ) );
                    pkColumns.add( fidColumn.first );
                }
            }
        }
        for ( Mapping mapping : ftMapping.getMappings() ) {
            ddls.addAll( process( sql, ftMapping.getFtTable(), mapping ) );
        }
        sql.append( ",\n    CONSTRAINT " );
        sql.append( ftMapping.getFtTable() );
        sql.append( "_pkey PRIMARY KEY (" );
        boolean first = true;
        for ( String pkColumn : pkColumns ) {
            if ( !first ) {
                sql.append( "," );
            }
            sql.append( pkColumn );
            first = false;
        }
        sql.append( ")\n)" );
        return ddls;
    }

    protected abstract void primitiveMappingSnippet( StringBuffer sql, PrimitiveMapping mapping );

    protected abstract void geometryMappingSnippet( StringBuffer sql, GeometryMapping mapping, List<StringBuffer> ddls,
                                                    QTableName table );

    protected abstract void featureMappingSnippet( StringBuffer sql, FeatureMapping mapping );

    protected abstract StringBuffer createJoinedTable( QTableName fromTable, TableJoin jc, List<StringBuffer> ddls );

    private List<StringBuffer> process( StringBuffer sql, QTableName table, Mapping mapping ) {
        List<StringBuffer> ddls = new ArrayList<StringBuffer>();

        if ( !( mapping instanceof FeatureMapping ) && mapping.getJoinedTable() != null ) {
            List<TableJoin> jc = mapping.getJoinedTable();
            sql = createJoinedTable( table, jc.get( 0 ), ddls );
            table = jc.get( 0 ).getToTable();
            if ( !ddls.contains( sql ) ) {
                ddls.add( sql );
            }
        }

        if ( mapping instanceof PrimitiveMapping ) {
            primitiveMappingSnippet( sql, (PrimitiveMapping) mapping );
        } else if ( mapping instanceof GeometryMapping ) {
            geometryMappingSnippet( sql, (GeometryMapping) mapping, ddls, table );
        } else if ( mapping instanceof FeatureMapping ) {
            featureMappingSnippet( sql, (FeatureMapping) mapping );
        } else if ( mapping instanceof CompoundMapping ) {
            CompoundMapping compoundMapping = (CompoundMapping) mapping;
            for ( Mapping childMapping : compoundMapping.getParticles() ) {
                ddls.addAll( process( sql, table, childMapping ) );
            }
        } else {
            throw new RuntimeException( "Internal error. Unhandled mapping type '" + mapping.getClass() + "'" );
        }

        if ( !( mapping instanceof FeatureMapping ) && mapping.getJoinedTable() != null ) {
            sql.append( "\n)" );
        }
        return ddls;
    }

    protected abstract String getDBType( BaseType type );

    // TODO get rid of this (DDLCreator should be the only needed implementation)
    public static DDLCreator newInstance( MappedApplicationSchema appSchema, SQLDialect dialect ) {
        switch ( dialect.getDBType() ) {
        case PostgreSQL: {
            return new PostGISDDLCreator( appSchema, dialect );
        }
        case MSSQL: {
            return new MSSQLDDLCreator( appSchema, dialect );
        }
        case Oracle: {
            return new OracleDDLCreator( appSchema, dialect );
        }
        }
        throw new IllegalArgumentException( "Nod DDLCreator for DB type '" + dialect.getDBType() + "' available." );
    }
}