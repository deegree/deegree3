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
package org.deegree.feature.persistence.sql.id;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.FeatureMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides efficient access to dependencies between key columns of tables in a {@link MappedAppSchema}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
public class TableDependencies {

    private static Logger LOG = LoggerFactory.getLogger( TableDependencies.class );

    private final Map<TableName, LinkedHashSet<SQLIdentifier>> tableToGenerators = new HashMap<TableName, LinkedHashSet<SQLIdentifier>>();

    private final Map<TableName, LinkedHashSet<KeyPropagation>> tableToParents = new HashMap<TableName, LinkedHashSet<KeyPropagation>>();

    private final Map<TableName, LinkedHashSet<KeyPropagation>> tableToChildren = new HashMap<TableName, LinkedHashSet<KeyPropagation>>();

    public TableDependencies( FeatureTypeMapping[] ftMappings ) {
        for ( FeatureTypeMapping ftMapping : ftMappings ) {
            buildFIDGenerator( ftMapping );
            TableName currentTable = ftMapping.getFtTable();
            for ( Mapping particle : ftMapping.getMappings() ) {
                buildDependencies( particle, currentTable );
            }
        }
    }

    private void buildFIDGenerator( FeatureTypeMapping ftMapping ) {
        // fid auto column
        FIDMapping fidMapping = ftMapping.getFidMapping();
        SQLIdentifier fidColumn = new SQLIdentifier( fidMapping.getColumn() );
        addAutoColumn( ftMapping.getFtTable(), fidColumn );
    }

    private void buildDependencies( Mapping particle, TableName currentTable ) {

        List<TableJoin> joins = particle.getJoinedTable();
        if ( joins != null && !joins.isEmpty() ) {

            if ( particle instanceof FeatureMapping ) {
                FeatureMapping f = (FeatureMapping) particle;
                if ( joins.size() != 1 ) {
                    String msg = "Feature type joins with more than one table are not supported yet.";
                    throw new UnsupportedOperationException( msg );
                }
                TableJoin join = joins.get( 0 );
                if ( f.getValueFtName() == null || join.getToTable().getName().equals( "?" ) ) {
                    LOG.debug( "Found special key propagation (involving ambigous feature table). Needs implementation." );
                    return;
                }
            }

            for ( TableJoin join : joins ) {
                TableName joinTable = join.getToTable();

                // check for propagations from current table to joined table
                for ( int i = 0; i < join.getFromColumns().size(); i++ ) {
                    SQLIdentifier fromColumn = join.getFromColumns().get( i );
                    SQLIdentifier toColumn = join.getToColumns().get( i );
                    LinkedHashSet<SQLIdentifier> linkedHashSet = tableToGenerators.get( currentTable );
                    if ( tableToGenerators.get( currentTable ) != null
                         && tableToGenerators.get( currentTable ).contains( fromColumn ) ) {
                        KeyPropagation prop = new KeyPropagation( currentTable, fromColumn, joinTable, toColumn );
                        LOG.debug( "Found key propagation (to join table): " + prop );
                        addChild( currentTable, prop );
                        addParent( joinTable, prop );
                    }
                }

                // add generated columns and check for propagations from joined table to current table
                Map<SQLIdentifier, IDGenerator> keyColumnToGenerator = join.getKeyColumnToGenerator();
                for ( SQLIdentifier autoGenColumn : keyColumnToGenerator.keySet() ) {
                    addAutoColumn( joinTable, autoGenColumn );
                    for ( int i = 0; i < join.getToColumns().size(); i++ ) {
                        SQLIdentifier fromColumn = join.getFromColumns().get( i );
                        SQLIdentifier toColumn = join.getToColumns().get( i );
                        if ( autoGenColumn.equals( toColumn ) ) {
                            KeyPropagation prop = new KeyPropagation( joinTable, toColumn, currentTable, fromColumn );
                            LOG.debug( "Found key propagation (from join table): " + prop );
                            addChild( joinTable, prop );
                            addParent( currentTable, prop );
                        }
                    }
                }

                currentTable = joinTable;
            }
        }
        if ( particle instanceof CompoundMapping ) {
            for ( Mapping child : ( (CompoundMapping) particle ).getParticles() ) {
                buildDependencies( child, currentTable );
            }
        }
    }

    private void addAutoColumn( TableName table, SQLIdentifier autoColumn ) {
        LinkedHashSet<SQLIdentifier> autoColumns = tableToGenerators.get( table );
        if ( autoColumns == null ) {
            autoColumns = new LinkedHashSet<SQLIdentifier>();
            tableToGenerators.put( table, autoColumns );
        }
        autoColumns.add( autoColumn );
    }

    private void addParent( TableName table, KeyPropagation propagation ) {
        LinkedHashSet<KeyPropagation> parents = tableToParents.get( table );
        if ( parents == null ) {
            parents = new LinkedHashSet<KeyPropagation>();
            tableToParents.put( table, parents );
        }
        parents.add( propagation );
    }

    private void addChild( TableName table, KeyPropagation propagation ) {
        LinkedHashSet<KeyPropagation> children = tableToChildren.get( table );
        if ( children == null ) {
            children = new LinkedHashSet<KeyPropagation>();
            tableToChildren.put( table, children );
        }
        children.add( propagation );
    }

    public Set<KeyPropagation> getParents( TableName table ) {
        return tableToParents.get( table );
    }

    public Set<KeyPropagation> getChildren( TableName table ) {
        return tableToChildren.get( table );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Set<TableName> tables = new TreeSet<TableName>();
        tables.addAll( tableToGenerators.keySet() );
        tables.addAll( tableToParents.keySet() );
        tables.addAll( tableToChildren.keySet() );
        for ( TableName table : tables ) {
            sb.append( "\n\nTable: " + table );
            sb.append( "\n -Generated key columns:" );
            if ( tableToGenerators.get( table ) != null ) {
                for ( SQLIdentifier autoColumn : tableToGenerators.get( table ) ) {
                    sb.append( "\n  -" + autoColumn );
                }
            }
            sb.append( "\n -Parents:" );
            if ( tableToParents.get( table ) != null ) {
                for ( KeyPropagation parent : tableToParents.get( table ) ) {
                    sb.append( "\n  -" + parent );
                }
            }
            sb.append( "\n -Children:" );
            if ( tableToChildren.get( table ) != null ) {
                for ( KeyPropagation child : tableToChildren.get( table ) ) {
                    sb.append( "\n  -" + child );
                }
            }
        }
        return sb.toString();
    }
}
