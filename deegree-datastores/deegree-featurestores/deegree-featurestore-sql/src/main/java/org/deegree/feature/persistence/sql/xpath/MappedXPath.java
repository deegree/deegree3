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

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.FeatureMapping;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.expression.ValueReference;
import org.deegree.sqldialect.filter.DBField;
import org.deegree.sqldialect.filter.Join;
import org.deegree.sqldialect.filter.MappingExpression;
import org.deegree.sqldialect.filter.PropertyNameMapping;
import org.deegree.sqldialect.filter.TableAliasManager;
import org.deegree.sqldialect.filter.UnmappableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ValueReference} that's mapped to the relational model defined by a {@link MappedAppSchema}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MappedXPath {

    private static final Logger LOG = LoggerFactory.getLogger( MappedXPath.class );

    private final SQLFeatureStore fs;

    private final MappedAppSchema schema;

    private final ValueReference propName;

    private final TableAliasManager aliasManager;

    private final List<Join> joins = new ArrayList<Join>();

    private final boolean isSpatial;

    private final boolean handleStrict;

    private final boolean useDefaultProperty;

    private String currentTable;

    private String currentTableAlias;

    private PropertyNameMapping propMapping;


    /**
     * @param fs
     * @param ftMapping
     * @param propName
     * @param aliasManager
     * @param isSpatial
     *            if <code>true</code>, a spatial property is targeted (in this case, mapped property names are
     *            automatically extended to the nearest geometry child in the mapping configuration)
     * @param handleStrict
     * @throws UnmappableException
     *             if the propertyName can not be matched to the relational model
     */
    public MappedXPath( SQLFeatureStore fs, FeatureTypeMapping ftMapping, ValueReference propName,
                        TableAliasManager aliasManager, boolean isSpatial, boolean handleStrict ) throws UnmappableException {

        this.fs = fs;
        this.schema = fs.getSchema();
        this.aliasManager = aliasManager;
        this.isSpatial = isSpatial;
        this.handleStrict = handleStrict;

        // check for empty property name
        List<MappableStep> steps = null;
        if ( propName == null || propName.getAsText().isEmpty() ) {
            LOG.debug( "Null / empty property name (=targets default geometry property)." );
            this.propName = new ValueReference( "geometry()", null );
            steps = Collections.emptyList();
            this.useDefaultProperty = true;
        } else {
            this.propName = propName;
            steps = MappableStep.extractSteps( propName );
            // the first step may be the name of the feature type or the name of a property
            if ( ftMapping.getFeatureType().equals( steps.get( 0 ) ) ) {
                steps.subList( 1, steps.size() );
            }
            this.useDefaultProperty = false;
        }

        currentTable = ftMapping.getFtTable().toString();
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
            if ( !mapSteps.isEmpty() && !steps.isEmpty() ) {
                matchFound = mapSteps.get( 0 ).equals( steps.get( 0 ) );
            } else if ( mapSteps.isEmpty() && steps.isEmpty() ) {
                matchFound = true;
            } else if ( !mapSteps.isEmpty() && mapSteps.get( 0 ) instanceof TextStep ) {
                matchFound = true;
            } else if ( mapSteps.isEmpty() && mapping instanceof FeatureMapping
                        && steps.get( 0 ) instanceof MappableNameStep ) {
                QName featureName = ( (MappableNameStep) steps.get( 0 ) ).getNodeName();
                FeatureType[] featureTypes = schema.getFeatureTypes();
                for ( FeatureType ft : featureTypes ) {
                    if ( ft.getName().equals( featureName ) ) {
                        matchFound = true;
                    }
                }
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
                } else {
                    String msg = "Handling of '" + mapping.getClass() + " not implemented yet.";
                    LOG.warn( msg );
                    throw new UnmappableException( msg );
                }
                break;
            }
        }

        if ( !matchFound && isSpatial ) {
            if ( handleStrict && !useDefaultProperty ) {
                String msg = "Cannot evaluate spatial operator. Targeted property name '" + propName.getAsText()
                             + "' is not mapped.";
                throw new InvalidParameterValueException( msg );
            }
            // determine path to nearest geometry mapping
            List<Mapping> additionalSteps = new ArrayList<Mapping>();
            if ( determineNearestGeometryMapping( mappedParticles, additionalSteps ) ) {
                matchFound = true;
                for ( int i = 0; i < additionalSteps.size() - 1; i++ ) {
                    followJoins( additionalSteps.get( i ).getJoinedTable() );
                }
                map( (GeometryMapping) additionalSteps.get( additionalSteps.size() - 1 ), steps );
            }
        }

        if ( !matchFound ) {
            if ( !steps.isEmpty() ) {
                MappableStep mappableStep = steps.get( 0 );
                String msg = "No mapping for PropertyName '" + propName.getAsText()
                             + "' available. Could not map step '" + mappableStep + "'.";
                throw new UnmappableException( msg );
            }
            String msg = "No mapping for PropertyName '" + propName.getAsText() + "' available.";
            throw new UnmappableException( msg );
        }
    }

    private boolean determineNearestGeometryMapping( Collection<Mapping> mappedParticles, List<Mapping> steps ) {
        boolean found = false;
        for ( Mapping mapping : mappedParticles ) {
            if ( mapping instanceof GeometryMapping ) {
                steps.add( mapping );
                found = true;
                break;
            } else if ( mapping instanceof CompoundMapping ) {
                steps.add( mapping );
                found = determineNearestGeometryMapping( ( (CompoundMapping) mapping ).getParticles(), steps );
                if ( found ) {
                    break;
                }
                steps.remove( steps.size() - 1 );
            }
        }
        return found;
    }

    private void map( PrimitiveMapping mapping, List<MappableStep> remaining )
                            throws UnmappableException {
        final PrimitiveMapping primMapping = mapping;
        final MappingExpression me = primMapping.getMapping();
        ParticleConverter<?> converter = null;
        if ( fs != null ) {
            converter = fs.getConverter( primMapping );
        }
        if ( !( me instanceof DBField ) ) {
            final String qualifiedExpr = me.toString().replace( "$0", currentTableAlias );
            propMapping = new PropertyNameMapping( converter, joins, qualifiedExpr, null );
            return;
        }
        propMapping = new PropertyNameMapping( converter, joins, ( (DBField) me ).getColumn(), currentTableAlias );
    }

    private void map( GeometryMapping mapping, List<MappableStep> remaining )
                            throws UnmappableException {

        GeometryMapping geomMapping = mapping;
        MappingExpression me = geomMapping.getMapping();
        if ( !( me instanceof DBField ) ) {
            throw new UnmappableException( "Mappings to non-DBField geometries is currently not supported." );
        }
        ParticleConverter<?> converter = null;
        if ( fs != null ) {
            converter = fs.getConverter( geomMapping );
        }
        propMapping = new PropertyNameMapping( converter, joins, ( (DBField) me ).getColumn(), currentTableAlias );
    }

    private void map( FeatureMapping mapping, List<MappableStep> remaining )
                            throws UnmappableException {
        // followJoins( mapping.getJoinedTable() );
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
        // TODO what do do with Href mappings here? Needs proper reference resolving code for mapping here...
        String fromColumn = mapping.getJoinedTable().get( 0 ).getFromColumns().get( 0 ).toString();
        String toTable = ftMapping.getFtTable().toString();
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
                String toTable = joinedTable.getToTable().toString();
                String toTableAlias = aliasManager.generateNew();

                List<String> fromColumns = new ArrayList<String>( joinedTable.getFromColumns().size() );
                for ( SQLIdentifier fromColumn : joinedTable.getFromColumns() ) {
                    fromColumns.add( fromColumn.toString() );
                }

                List<String> toColumns = new ArrayList<String>( joinedTable.getToColumns().size() );
                for ( SQLIdentifier toColumn : joinedTable.getToColumns() ) {
                    toColumns.add( toColumn.toString() );
                }

                Join appliedJoin = new Join( fromTable, fromTableAlias, fromColumns, toTable, toTableAlias, toColumns );
                joins.add( appliedJoin );
                currentTable = toTable;
                currentTableAlias = toTableAlias;
            }
        }
    }
}
