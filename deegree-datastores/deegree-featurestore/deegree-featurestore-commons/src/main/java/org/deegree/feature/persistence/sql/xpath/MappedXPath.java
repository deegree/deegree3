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
import java.util.List;

import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.MappedApplicationSchema;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.rules.CompoundMapping;
import org.deegree.feature.persistence.sql.rules.ConstantMapping;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.ConstantPropertyNameMapping;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.GeometryPropertyNameMapping;
import org.deegree.filter.sql.Join;
import org.deegree.filter.sql.MappingExpression;
import org.deegree.filter.sql.PrimitivePropertyNameMapping;
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

    private final MappedApplicationSchema schema;

    private final PropertyName propName;

    private final TableAliasManager aliasManager;

    private String currentTable;

    private final List<Join> joins = new ArrayList<Join>();

    private PropertyNameMapping propMapping;

    /**
     * @param schema
     * @param ftMapping
     * @param propName
     * @param aliasManager
     * @throws UnmappableException
     *             if the propertyName can not be matched to the relational model
     */
    public MappedXPath( MappedApplicationSchema schema, FeatureTypeMapping ftMapping, PropertyName propName,
                        TableAliasManager aliasManager ) throws UnmappableException {

        this.schema = schema;
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

        this.currentTable = aliasManager.getRootTableAlias();
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
                matchFound = mapSteps.get( 0 ).equals( steps.get( 0 ) );
            } else if ( mapSteps.get( 0 ) instanceof TextStep ) {
                matchFound = true;
            }
            if ( matchFound ) {
                followJoins( mapping.getJoinedTable() );
                if ( mapping instanceof CompoundMapping ) {
                    map( ( (CompoundMapping) mapping ).getParticles(), steps.subList( 1, steps.size() ) );
                } else if ( mapping instanceof PrimitiveMapping ) {
                    map( (PrimitiveMapping) mapping, steps );
                } else if ( mapping instanceof GeometryMapping ) {
                    map( (GeometryMapping) mapping, steps );
                } else if ( mapping instanceof ConstantMapping<?> ) {
                    map( (ConstantMapping<?>) mapping, steps );
                } else {
                    throw new UnmappableException( "Handling of '" + mapping.getClass() + " not implemented yet." );
                }
                break;
            }
        }
        if ( !matchFound ) {
            String msg = "No mapping for PropertyName '" + propName + "' available.";
            throw new UnmappableException( msg );
        }
    }

    private void map( ConstantMapping<?> mapping, List<MappableStep> steps ) {
        propMapping = new ConstantPropertyNameMapping( mapping.getValue() );
    }

    private void map( PrimitiveMapping mapping, List<MappableStep> remaining )
                            throws UnmappableException {

        PrimitiveMapping primMapping = (PrimitiveMapping) mapping;
        MappingExpression me = primMapping.getMapping();
        if ( !( me instanceof DBField ) ) {
            throw new UnmappableException( "Mappings to non-DBField primitives is currently not supported." );
        }
        DBField dbField = (DBField) me;
        DBField valueField = new DBField( currentTable, dbField.getColumn() );
        int sqlType = -1;
        PrimitiveType pt = primMapping.getType();
        propMapping = new PrimitivePropertyNameMapping( valueField, sqlType, joins, pt, false );
    }

    private void map( GeometryMapping mapping, List<MappableStep> remaining )
                            throws UnmappableException {

        GeometryMapping geomMapping = (GeometryMapping) mapping;
        MappingExpression me = geomMapping.getMapping();
        if ( !( me instanceof DBField ) ) {
            throw new UnmappableException( "Mappings to non-DBField geometries is currently not supported." );
        }
        DBField dbField = (DBField) me;
        DBField valueField = new DBField( currentTable, dbField.getColumn() );
        int sqlType = -1;
        propMapping = new GeometryPropertyNameMapping( valueField, sqlType, joins, geomMapping.getCRS(),
                                                       geomMapping.getSrid() );
    }

    private void followJoins( List<TableJoin> joinedTable ) {
    }

    // private void map( FeatureTypeMapping currentFt, List<MappableNameStep> steps )
    // throws UnmappableException {
    //
    // FeatureType ft = schema.getFeatureType( currentFt.getFeatureType() );
    // PropertyType pt = null;
    //
    // boolean propStep = true;
    //
    // // process all but the last step
    // FeatureTypeMapping ftMapping = rootFt;
    // for ( int i = startIdx; i < steps.size() - 1; i++ ) {
    // if ( propStep ) {
    // QName propName = steps.get( i );
    // pt = ft.getPropertyDeclaration( propName );
    // if ( pt == null ) {
    // String msg = "Error in property name, step " + ( i + 1 ) + ": feature type '" + ft.getName()
    // + "' does not define a property with name '" + propName + "'.";
    // throw new UnmappableException( msg );
    // }
    // propStep = false;
    // } else {
    // if ( !( pt instanceof FeaturePropertyType ) ) {
    // String msg = "Error in property name, step " + ( i + 1 ) + ": property '" + pt.getName()
    // + "' is not a feature property type, but the path does not stop here.";
    // throw new UnmappableException( msg );
    // }
    // FeaturePropertyType fpt = (FeaturePropertyType) pt;
    // QName ftName = steps.get( i );
    // ft = schema.getFeatureType( ftName );
    // if ( ft == null ) {
    // String msg = "Error in property name, step " + ( i + 1 ) + ": '" + ftName
    // + "' is not a known feature type.";
    // throw new UnmappableException( msg );
    // }
    // if ( fpt.getValueFt() != null && !schema.isSubType( fpt.getValueFt(), ft ) ) {
    // String msg = "Error in property name, step " + ( i + 1 ) + ": '" + ftName
    // + "' is not possible substitution for the value feature type (='"
    // + fpt.getValueFt().getName() + "') of property '" + pt.getName() + "'.";
    // throw new UnmappableException( msg );
    // }
    //
    // FeatureTypeMapping valueFtMapping = schema.getFtMapping( fpt.getValueFt().getName() );
    // if ( valueFtMapping == null ) {
    // String msg = "Feature type '" + ft.getName() + "' is not mapped.";
    // throw new UnmappableException( msg );
    // }
    //
    // Mapping propMapping = ftMapping.getMapping( pt.getName() );
    // if ( propMapping == null ) {
    // String msg = "Property '" + pt.getName() + "' is not mapped.";
    // throw new UnmappableException( msg );
    // }
    // MappingExpression mapping = null;
    // if ( propMapping instanceof PrimitiveMapping ) {
    // mapping = ( (PrimitiveMapping) propMapping ).getMapping();
    // } else if ( propMapping instanceof GeometryMapping ) {
    // mapping = ( (GeometryMapping) propMapping ).getMapping();
    // } else {
    // String msg = "Unhandled mapping type '" + propMapping.getClass() + "'.";
    // throw new UnmappableException( msg );
    // }
    //
    // // TODO
    // addJoins( ftMapping, mapping, valueFtMapping );
    //
    // ftMapping = valueFtMapping;
    // propStep = true;
    // }
    // }
    //
    // // last step
    // if ( !propStep ) {
    // String msg = "Error in property name, it does not end with a property step.";
    // throw new UnmappableException( msg );
    // }
    // QName propName = steps.get( steps.size() - 1 );
    // pt = ft.getPropertyDeclaration( propName );
    // if ( pt == null ) {
    // String msg = "Error in property name, step " + ( steps.size() ) + ": feature type '" + ft.getName()
    // + "' does not define a property with name '" + propName + "'.";
    // throw new UnmappableException( msg );
    // }
    // ftMapping = schema.getFtMapping( ft.getName() );
    // if ( ftMapping == null ) {
    // String msg = "Feature type '" + ft.getName() + "' is not mapped.";
    // throw new UnmappableException( msg );
    // }
    // Mapping mapping = ftMapping.getMapping( propName );
    // if ( mapping == null ) {
    // String msg = "Property '" + propName + "' is not mapped.";
    // throw new UnmappableException( msg );
    // }
    //
    // if ( mapping.getJoinedTable() != null ) {
    // // TODO multi column capability
    // TableJoin jc = mapping.getJoinedTable().get( 0 );
    // DBField from = new DBField( getCurrentTable().getTable(), jc.getFromColumns().get( 0 ) );
    // DBField to = new DBField( jc.getToTable().toString(), jc.getToColumns().get( 0 ) );
    // joins.add( new Join( from, to, null, -1 ) );
    // }
    //
    // if ( mapping instanceof GeometryMapping ) {
    // isSpatial = true;
    // crs = ( (GeometryMapping) mapping ).getCRS();
    // srid = ( (GeometryMapping) mapping ).getSrid();
    // }
    //
    // MappingExpression propMapping = null;
    // if ( mapping instanceof PrimitiveMapping ) {
    // propMapping = ( (PrimitiveMapping) mapping ).getMapping();
    // } else if ( mapping instanceof GeometryMapping ) {
    // propMapping = ( (GeometryMapping) mapping ).getMapping();
    // } else {
    // String msg = "Unhandled mapping type '" + mapping.getClass() + "'.";
    // throw new UnmappableException( msg );
    // }
    // if ( propMapping instanceof DBField ) {
    // QTableName table = getCurrentTable();
    // valueField = new DBField( table.toString(), ( (DBField) propMapping ).getColumn() );
    // } else {
    // throw new UnmappableException( "Unhandled mapping expression: " + propMapping.getClass() );
    // }
    // }

    private void addJoins( FeatureTypeMapping source, MappingExpression prop, FeatureTypeMapping target )
                            throws UnmappableException {

        if ( prop instanceof DBField ) {
            DBField dbField = (DBField) prop;
            DBField from = new DBField( source.getFtTable().toString(), dbField.getColumn() );
            // TODO
            DBField to = new DBField( target.getFtTable().toString(), target.getFidMapping().getColumn() );
            joins.add( new Join( from, to, null, -1 ) );
        } else {
            throw new UnmappableException( "Unhandled mapping expression: " + prop.getClass() );
        }
    }
}