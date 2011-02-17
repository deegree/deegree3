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
package org.deegree.feature.persistence.sql;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.QTableName;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.persistence.sql.rules.PrimitiveMapping;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.Join;
import org.deegree.filter.sql.MappingExpression;
import org.deegree.filter.sql.UnmappableException;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a {@link PropertyName} that's mapped to a relational model defined by a {@link MappedApplicationSchema}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MappedXPath {

    private static final Logger LOG = LoggerFactory.getLogger( MappedXPath.class );

    private final FeatureTypeMapping rootFt;

    private DBField valueField;

    private final List<Join> joins = new ArrayList<Join>();

    private String srid;

    private ICRS crs;

    /**
     * @param schema
     * @param ftMapping
     * @param propName
     * @throws UnmappableException
     *             if the propertyName can not be matched to the relational model
     */
    public MappedXPath( MappedApplicationSchema schema, FeatureTypeMapping ftMapping, PropertyName propName )
                            throws UnmappableException {

        this.rootFt = ftMapping;

        List<QName> steps = new ArrayList<QName>();

        if ( propName == null || propName.getAsText().isEmpty() ) {
            LOG.debug( "Null / empty property name (=targets default geometry property)." );
            FeatureType ft = schema.getFeatureType( ftMapping.getFeatureType() );
            GeometryPropertyType pt = ft.getDefaultGeometryPropertyDeclaration();
            if ( pt == null ) {
                String msg = "Feature type '" + ft.getName()
                             + "' does not have a geometry property and PropertyName is missing / empty.";
                throw new UnmappableException( msg );
            }
            steps.add( pt.getName() );
        } else if ( propName.getAsQName() != null ) {
            LOG.debug( "Simple property name (=QName)." );
            steps.add( propName.getAsQName() );
        } else {
            LOG.debug( "XPath property name (not just a QName)." );
            try {
                Expr xpath = propName.getAsXPath();
                if ( !( xpath instanceof LocationPath ) ) {
                    String msg = "Unable to map PropertyName '" + propName.getAsText()
                                 + "': the root expression is not a LocationPath.";
                    LOG.debug( msg );
                    throw new UnmappableException( msg );
                }
                for ( Object step : ( (LocationPath) xpath ).getSteps() ) {
                    if ( !( step instanceof NameStep ) ) {
                        String msg = "Unable to map PropertyName '" + propName.getAsText()
                                     + "': contains an expression that is not a NameStep.";
                        LOG.debug( msg );
                        throw new UnmappableException( msg );
                    }
                    NameStep namestep = (NameStep) step;
                    if ( namestep.getPredicates() != null && !namestep.getPredicates().isEmpty() ) {
                        String msg = "Unable to map PropertyName '" + propName.getAsText()
                                     + "': contains a NameStep with a predicate (needs implementation).";
                        LOG.debug( msg );
                        throw new UnmappableException( msg );
                    }
                    String prefix = namestep.getPrefix();
                    String localPart = namestep.getLocalName();
                    String namespace = propName.getNsContext().translateNamespacePrefixToUri( prefix );
                    steps.add( new QName( namespace, localPart, prefix ) );
                }

            } catch ( FilterEvaluationException e ) {
                throw new UnmappableException( e.getMessage() );
            }
        }
        map( schema, ftMapping, steps );
    }

    private void map( MappedApplicationSchema schema, FeatureTypeMapping rootFt, List<QName> steps )
                            throws UnmappableException {

        // the first step may be the name of the feature type or the name of a property
        int startIdx = 0;
        if ( rootFt.getFeatureType().equals( steps.get( 0 ) ) ) {
            startIdx = 1;
        }

        FeatureType ft = schema.getFeatureType( rootFt.getFeatureType() );
        PropertyType pt = null;

        boolean propStep = true;

        // process all but the last step
        FeatureTypeMapping ftMapping = rootFt;
        for ( int i = startIdx; i < steps.size() - 1; i++ ) {
            if ( propStep ) {
                QName propName = steps.get( i );
                pt = ft.getPropertyDeclaration( propName );
                if ( pt == null ) {
                    String msg = "Error in property name, step " + ( i + 1 ) + ": feature type '" + ft.getName()
                                 + "' does not define a property with name '" + propName + "'.";
                    throw new UnmappableException( msg );
                }
                propStep = false;
            } else {
                if ( !( pt instanceof FeaturePropertyType ) ) {
                    String msg = "Error in property name, step " + ( i + 1 ) + ": property '" + pt.getName()
                                 + "' is not a feature property type, but the path does not stop here.";
                    throw new UnmappableException( msg );
                }
                FeaturePropertyType fpt = (FeaturePropertyType) pt;
                QName ftName = steps.get( i );
                ft = schema.getFeatureType( ftName );
                if ( ft == null ) {
                    String msg = "Error in property name, step " + ( i + 1 ) + ": '" + ftName
                                 + "' is not a known feature type.";
                    throw new UnmappableException( msg );
                }
                if ( fpt.getValueFt() != null && !schema.isSubType( fpt.getValueFt(), ft ) ) {
                    String msg = "Error in property name, step " + ( i + 1 ) + ": '" + ftName
                                 + "' is not possible substitution for the value feature type (='"
                                 + fpt.getValueFt().getName() + "') of property '" + pt.getName() + "'.";
                    throw new UnmappableException( msg );
                }

                FeatureTypeMapping valueFtMapping = schema.getFtMapping( fpt.getValueFt().getName() );
                if ( valueFtMapping == null ) {
                    String msg = "Feature type '" + ft.getName() + "' is not mapped.";
                    throw new UnmappableException( msg );
                }

                Mapping propMapping = ftMapping.getMapping( pt.getName() );
                if ( propMapping == null ) {
                    String msg = "Property '" + pt.getName() + "' is not mapped.";
                    throw new UnmappableException( msg );
                }
                MappingExpression mapping = null;
                if ( propMapping instanceof PrimitiveMapping ) {
                    mapping = ( (PrimitiveMapping) propMapping ).getMapping();
                } else if ( propMapping instanceof GeometryMapping ) {
                    mapping = ( (GeometryMapping) propMapping ).getMapping();
                } else {
                    String msg = "Unhandled mapping type '" + propMapping.getClass() + "'.";
                    throw new UnmappableException( msg );
                }

                // TODO
                addJoins( ftMapping, mapping, valueFtMapping );

                ftMapping = valueFtMapping;
                propStep = true;
            }
        }

        // last step
        if ( !propStep ) {
            String msg = "Error in property name, it does not end with a property step.";
            throw new UnmappableException( msg );
        }
        QName propName = steps.get( steps.size() - 1 );
        pt = ft.getPropertyDeclaration( propName );
        if ( pt == null ) {
            String msg = "Error in property name, step " + ( steps.size() ) + ": feature type '" + ft.getName()
                         + "' does not define a property with name '" + propName + "'.";
            throw new UnmappableException( msg );
        }
        ftMapping = schema.getFtMapping( ft.getName() );
        if ( ftMapping == null ) {
            String msg = "Feature type '" + ft.getName() + "' is not mapped.";
            throw new UnmappableException( msg );
        }
        Mapping mapping = ftMapping.getMapping( propName );
        if ( mapping == null ) {
            String msg = "Property '" + propName + "' is not mapped.";
            throw new UnmappableException( msg );
        }

        if ( mapping instanceof GeometryMapping ) {
            crs = ( (GeometryMapping) mapping ).getCRS();
            srid = ( (GeometryMapping) mapping ).getSrid();
        }

        MappingExpression propMapping = null;
        if ( mapping instanceof PrimitiveMapping ) {
            propMapping = ( (PrimitiveMapping) mapping ).getMapping();
        } else if ( mapping instanceof GeometryMapping ) {
            propMapping = ( (GeometryMapping) mapping ).getMapping();
        } else {
            String msg = "Unhandled mapping type '" + propMapping.getClass() + "'.";
            throw new UnmappableException( msg );
        }
        if ( propMapping instanceof DBField ) {
            QTableName table = rootFt.getFtTable();
            if ( !joins.isEmpty() ) {
                table = new QTableName( joins.get( joins.size() - 1 ).getTo().getTable() );
            }
            valueField = new DBField( table.toString(), ( (DBField) propMapping ).getColumn() );
        } else if ( propMapping instanceof JoinChain ) {
            JoinChain chain = (JoinChain) propMapping;
            add( chain );
            QTableName table = getCurrentTable();
            valueField = new DBField( table.toString(),
                                      chain.getFields().get( chain.getFields().size() - 1 ).getColumn() );
        } else {
            throw new UnmappableException( "Unhandled mapping expression: " + propMapping.getClass() );
        }
    }

    private void addJoins( FeatureTypeMapping source, MappingExpression prop, FeatureTypeMapping target )
                            throws UnmappableException {

        if ( prop instanceof DBField ) {
            DBField dbField = (DBField) prop;
            DBField from = new DBField( source.getFtTable().toString(), dbField.getColumn() );
            // TODO
            DBField to = new DBField( target.getFtTable().toString(), target.getFidMapping().getColumn() );
            joins.add( new Join( from, to, null, -1 ) );
        } else if ( prop instanceof JoinChain ) {
            JoinChain chain = (JoinChain) prop;
            add( chain );
            QTableName table = getCurrentTable();
            DBField from = new DBField( table.toString(),
                                        chain.getFields().get( chain.getFields().size() - 1 ).getColumn() );
            // TODO
            DBField to = new DBField( target.getFtTable().toString(), target.getFidMapping().getColumn() );
            joins.add( new Join( from, to, null, -1 ) );
        } else {
            throw new UnmappableException( "Unhandled mapping expression: " + prop.getClass() );
        }
    }

    private void add( JoinChain chain ) {
        QTableName table = getCurrentTable();
        for ( int i = 0; i < chain.getFields().size() - 2; i += 2 ) {
            DBField from = new DBField( table.toString(), chain.getFields().get( i ).getColumn() );
            DBField to = new DBField( chain.getFields().get( i + 1 ).getTable(),
                                      chain.getFields().get( i + 1 ).getColumn() );
            joins.add( new Join( from, to, null, -1 ) );
            table = new QTableName( chain.getFields().get( i + 1 ).getTable() );
        }
    }

    private QTableName getCurrentTable() {
        QTableName table = rootFt.getFtTable();
        if ( !joins.isEmpty() ) {
            table = new QTableName( joins.get( joins.size() - 1 ).getTo().getTable() );
        }
        return table;
    }

    public DBField getValueField() {
        return valueField;
    }

    public ICRS getCRS() {
        return crs;
    }

    public String getSRID() {
        return srid;
    }

    /**
     * Returns the required joins.
     * 
     * @return the required joins, can be empty (no joins required), but never <code>null</code>
     */
    public List<Join> getJoins() {
        return joins;
    }

    @Override
    public String toString() {
        String s = "";
        for ( int i = 0; i < joins.size(); i++ ) {
            s += joins;
            s += ",";
        }
        s += valueField;
        return s;
    }
}