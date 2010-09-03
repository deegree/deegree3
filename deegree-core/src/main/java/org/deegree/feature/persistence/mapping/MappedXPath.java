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
package org.deegree.feature.persistence.mapping;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.UnmappableException;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a {@link PropertyName} that's mapped to the relational model defined by a {@link MappedApplicationSchema}
 * .
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

        if ( propName.isSimple() ) {
            LOG.debug( "Simple property name (=QName)." );
            steps.add( propName.getAsQName() );
        } else {
            LOG.debug( "XPath property name (not just a QName)." );
            try {
                Expr xpath = propName.getAsXPath();
                if ( !( xpath instanceof LocationPath ) ) {
                    String msg = "Unable to map PropertyName '" + propName.getPropertyName()
                                 + "': the root expression is not a LocationPath.";
                    LOG.debug( msg );
                    throw new UnmappableException( msg );
                }
                for ( Object step : ( (LocationPath) xpath ).getSteps() ) {
                    if ( !( step instanceof NameStep ) ) {
                        String msg = "Unable to map PropertyName '" + propName.getPropertyName()
                                     + "': contains an expression that is not a NameStep.";
                        LOG.debug( msg );
                        throw new UnmappableException( msg );
                    }
                    NameStep namestep = (NameStep) step;
                    if ( namestep.getPredicates() != null && !namestep.getPredicates().isEmpty() ) {
                        String msg = "Unable to map PropertyName '" + propName.getPropertyName()
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

                FeatureTypeMapping valueFtMapping = schema.getMapping( fpt.getValueFt().getName() );
                if ( valueFtMapping == null ) {
                    String msg = "Feature type '" + ft.getName() + "' is not mapped.";
                    throw new UnmappableException( msg );
                }

                MappingExpression propMapping = ftMapping.getMapping( pt.getName() );
                if ( propMapping == null ) {
                    String msg = "Property '" + pt.getName() + "' is not mapped.";
                    throw new UnmappableException( msg );
                }

                addJoins( ftMapping, propMapping, valueFtMapping );

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
        ftMapping = schema.getMapping( ft.getName() );
        if ( ftMapping == null ) {
            String msg = "Feature type '" + ft.getName() + "' is not mapped.";
            throw new UnmappableException( msg );
        }
        MappingExpression propMapping = ftMapping.getMapping( propName );
        if ( propMapping == null ) {
            String msg = "Property '" + propName + "' is not mapped.";
            throw new UnmappableException( msg );
        }
        if ( propMapping instanceof DBField ) {
            String table = rootFt.getFtTable();
            if ( !joins.isEmpty() ) {
                table = joins.get( joins.size() - 1 ).getTo().getTable();
            }
            valueField = new DBField( table, ( (DBField) propMapping ).getColumn() );
        } else if ( propMapping instanceof JoinChain ) {
            JoinChain chain = (JoinChain) propMapping;
            add( chain );
            String table = getCurrentTable();
            valueField = new DBField( table, chain.getFields().get( chain.getFields().size() - 1 ).getColumn() );
        } else {
            throw new UnmappableException( "Unhandled mapping expression: " + propMapping.getClass() );
        }
    }

    private void addJoins( FeatureTypeMapping source, MappingExpression prop, FeatureTypeMapping target )
                            throws UnmappableException {

        if ( prop instanceof DBField ) {
            DBField dbField = (DBField) prop;
            DBField from = new DBField( source.getFtTable(), dbField.getColumn() );
            DBField to = new DBField( target.getFtTable(), target.getFidColumn() );
            joins.add( new Join( from, to, null, -1 ) );
        } else if ( prop instanceof JoinChain ) {
            JoinChain chain = (JoinChain) prop;
            add( chain );
            String table = getCurrentTable();
            DBField from = new DBField( table, chain.getFields().get( chain.getFields().size() - 1 ).getColumn() );
            DBField to = new DBField( target.getFtTable(), target.getFidColumn() );
            joins.add( new Join( from, to, null, -1 ) );
        } else {
            throw new UnmappableException( "Unhandled mapping expression: " + prop.getClass() );
        }
    }

    private void add( JoinChain chain ) {
        String table = getCurrentTable();
        for ( int i = 0; i < chain.getFields().size() - 2; i += 2 ) {
            DBField from = new DBField( table, chain.getFields().get( i ).getColumn() );
            DBField to = new DBField( chain.getFields().get( i + 1 ).getTable(),
                                      chain.getFields().get( i + 1 ).getColumn() );
            joins.add( new Join( from, to, null, -1 ) );
            table = chain.getFields().get( i + 1 ).getTable();
        }
    }

    private String getCurrentTable() {
        String table = rootFt.getFtTable();
        if ( !joins.isEmpty() ) {
            table = joins.get( joins.size() - 1 ).getTo().getTable();
        }
        return table;
    }

    public DBField getValueField() {
        return valueField;
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
