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
package org.deegree.feature.persistence.sql.xpath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.MappedApplicationSchema;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.ConstantMapping;
import org.deegree.feature.persistence.sql.rules.FeatureMapping;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.ConstantPropertyNameMapping;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.Join;
import org.deegree.filter.sql.MappingExpression;
import org.deegree.filter.sql.PropertyNameMapping;
import org.deegree.filter.sql.TableAliasManager;
import org.deegree.filter.sql.UnmappableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link PropertyName} that's mapped to the relational model defined by a {@link MappedApplicationSchema}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MappedXPath {

    private static final Logger LOG = LoggerFactory.getLogger( MappedXPath.class );

    private final SQLFeatureStore fs;

    private final MappedApplicationSchema schema;

    private final PropertyName propName;

    private final TableAliasManager aliasManager;

    private final List<Join> joins = new ArrayList<Join>();

    private String currentTable;

    private String currentTableAlias;

    private PropertyNameMapping propMapping;

    /**
     * @param fs
     * @param ftMapping
     * @param propName
     * @param aliasManager
     * @throws UnmappableException
     *             if the propertyName can not be matched to the relational model
     */
    public MappedXPath( SQLFeatureStore fs, FeatureTypeMapping ftMapping, PropertyName propName,
                        TableAliasManager aliasManager ) throws UnmappableException {

        this.fs = fs;
        this.schema = fs.getSchema();
        this.aliasManager = aliasManager;

        // check for empty property name
        if ( propName == null || propName.getAsText().isEmpty() ) {
            LOG.debug( "Null / empty property name (=targets default geometry property)." );
            FeatureType ft = schema.getFeatureType( ftMapping.getFeatureType() );
            GeometryPropertyType pt = ft.getDefaultGeometryPropertyDeclaration();
            if ( pt == null ) {
                String msg = "Feature type '" + ft.getName()
                             + "' does not have a geometry property and PropertyName is missing / empty.";
                throw new UnmappableException( msg );
            }
            propName = new PropertyName( pt.getName() );
        }

        this.propName = propName;

        List<MappableStep> steps = MappableStep.extractSteps( propName );

        // the first step may be the name of the feature type or the name of a property
        if ( ftMapping.getFeatureType().equals( steps.get( 0 ) ) ) {
            steps.subList( 1, steps.size() );
        }

        currentTable = ftMapping.getFtTable().getTable();
        currentTableAlias = aliasManager.getRootTableAlias();
        map( ftMapping.getMappings(), steps );
    }

    public PropertyNameMapping getPropertyNameMapping() {
        return propMapping;
    }

    private void map( Collection<Mapping> mappedParticles, List<MappableStep> steps )
                            throws UnmappableException {

        boolean matchFound = false;
        for ( Mapping mapping : mappedParticles ) {
            List<MappableStep> mapSteps = MappableNameStep.extractSteps( mapping.getPath() );
            if ( mapSteps.isEmpty() ) {
                matchFound = true;
            } else if ( !( steps.isEmpty() ) ) {
                matchFound = true;
                matchFound = mapSteps.get( 0 ).equals( steps.get( 0 ) );
            } else if ( mapSteps.get( 0 ) instanceof TextStep ) {
                matchFound = true;
            }
            if ( matchFound ) {
                if ( mapping instanceof CompoundMapping ) {
                    followJoins( mapping.getJoinedTable() );
                    map( ( (CompoundMapping) mapping ).getParticles(), steps.subList( 1, steps.size() ) );
                } else if ( mapping instanceof PrimitiveMapping ) {
                    followJoins( mapping.getJoinedTable() );
                    map( (PrimitiveMapping) mapping, steps );
                } else if ( mapping instanceof GeometryMapping ) {
                    followJoins( mapping.getJoinedTable() );
                    map( (GeometryMapping) mapping, steps );
                } else if ( mapping instanceof FeatureMapping ) {
                    // not following joins
                    map( (FeatureMapping) mapping, steps );
                } else if ( mapping instanceof ConstantMapping<?> ) {
                    followJoins( mapping.getJoinedTable() );
                    map( (ConstantMapping<?>) mapping, steps );
                } else {
                    String msg = "Handling of '" + mapping.getClass() + " not implemented yet.";
                    LOG.warn( msg );
                    throw new UnmappableException( msg );
                }
                break;
            }
        }
        if ( !matchFound ) {
            String msg = "No mapping for PropertyName '" + propName.getAsText() + "' available.";
            throw new UnmappableException( msg );
        }
    }

    private void map( ConstantMapping<?> mapping, List<MappableStep> steps ) {
        propMapping = new ConstantPropertyNameMapping( mapping.getValue() );
    }

    private void map( PrimitiveMapping mapping, List<MappableStep> remaining )
                            throws UnmappableException {

        PrimitiveMapping primMapping = mapping;
        MappingExpression me = primMapping.getMapping();
        if ( !( me instanceof DBField ) ) {
            throw new UnmappableException( "Mappings to non-DBField primitives is currently not supported." );
        }
        ParticleConverter<?> converter = fs.getConverter( primMapping );
        propMapping = new PropertyNameMapping( converter, joins, ( (DBField) me ).getColumn(), currentTableAlias );
    }

    private void map( GeometryMapping mapping, List<MappableStep> remaining )
                            throws UnmappableException {

        GeometryMapping geomMapping = mapping;
        MappingExpression me = geomMapping.getMapping();
        if ( !( me instanceof DBField ) ) {
            throw new UnmappableException( "Mappings to non-DBField geometries is currently not supported." );
        }
        ParticleConverter<?> converter = fs.getConverter( geomMapping );
        propMapping = new PropertyNameMapping( converter, joins, ( (DBField) me ).getColumn(), currentTableAlias );
    }

    private void map( FeatureMapping mapping, List<MappableStep> remaining )
                            throws UnmappableException {
//        followJoins( mapping.getJoinedTable() );
        if ( remaining.size() < 2 ) {
            throw new UnmappableException( "Not enough steps." );
        }
        MappableStep ftStep = remaining.get( 0 );
        if ( !( ftStep instanceof ElementStep ) ) {
            throw new UnmappableException( "Must provide a feature type name." );
        }
        QName ftName = ( (ElementStep) ftStep ).getNodeName();
        FeatureTypeMapping ftMapping = schema.getFtMapping( ftName );
        if ( ftMapping == null ) {
            throw new UnmappableException( "Feature type '" + ftName + " is not mapped to a table." );
        }

        String fromTable = currentTable;
        String fromTableAlias = currentTableAlias;
        // TODO
        String fromColumn = mapping.getJoinedTable().get( 0 ).getFromColumns().get( 0 );
        String toTable = ftMapping.getFtTable().getTable();
        String toTableAlias = aliasManager.generateNew();

        String toColumn = ftMapping.getFidMapping().getColumn();
        Join appliedJoin = new Join( fromTable, fromTableAlias, Collections.singletonList( fromColumn ), toTable,
                                     toTableAlias, Collections.singletonList( toColumn ) );
        joins.add( appliedJoin );
        currentTable = toTable;
        currentTableAlias = toTableAlias;

        map( ftMapping.getMappings(), remaining.subList( 1, remaining.size() ) );
    }

    private void followJoins( List<TableJoin> joinedTables ) {
        if ( joinedTables != null ) {
            for ( TableJoin joinedTable : joinedTables ) {
                String fromTable = currentTable;
                String fromTableAlias = currentTableAlias;
                String toTable = joinedTable.getToTable().getTable();
                String toTableAlias = aliasManager.generateNew();
                Join appliedJoin = new Join( fromTable, fromTableAlias, joinedTable.getFromColumns(), toTable,
                                             toTableAlias, joinedTable.getToColumns() );
                joins.add( appliedJoin );
                currentTable = toTable;
                currentTableAlias = toTableAlias;
            }
        }
    }
}