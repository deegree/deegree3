//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.io.datastore.sql.wherebuilder;

import static java.sql.Types.DOUBLE;
import static java.sql.Types.INTEGER;
import static java.sql.Types.SMALLINT;
import static org.deegree.model.filterencoding.ExpressionDefines.ADD;
import static org.deegree.model.filterencoding.ExpressionDefines.DIV;
import static org.deegree.model.filterencoding.ExpressionDefines.MUL;
import static org.deegree.model.filterencoding.ExpressionDefines.SUB;
import static org.deegree.model.filterencoding.OperationDefines.BBOX;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.io.datastore.schema.MappedFeaturePropertyType;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGeometryPropertyType;
import org.deegree.io.datastore.schema.MappedPropertyType;
import org.deegree.io.datastore.schema.MappedSimplePropertyType;
import org.deegree.io.datastore.schema.TableRelation;
import org.deegree.io.datastore.schema.content.ConstantContent;
import org.deegree.io.datastore.schema.content.MappingField;
import org.deegree.io.datastore.schema.content.SQLFunctionCall;
import org.deegree.io.datastore.schema.content.SimpleContent;
import org.deegree.io.datastore.sql.StatementBuffer;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.datastore.sql.VirtualContentProvider;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.filterencoding.ArithmeticExpression;
import org.deegree.model.filterencoding.ComparisonOperation;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.DBFunction;
import org.deegree.model.filterencoding.Expression;
import org.deegree.model.filterencoding.ExpressionDefines;
import org.deegree.model.filterencoding.FeatureFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterEvaluationException;
import org.deegree.model.filterencoding.Function;
import org.deegree.model.filterencoding.Literal;
import org.deegree.model.filterencoding.LogicalOperation;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyIsBetweenOperation;
import org.deegree.model.filterencoding.PropertyIsCOMPOperation;
import org.deegree.model.filterencoding.PropertyIsLikeOperation;
import org.deegree.model.filterencoding.PropertyIsNullOperation;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.SortProperty;

/**
 * Creates SQL-WHERE clauses from OGC filter expressions (to restrict SQL statements to all stored features that match a
 * given filter).
 * <p>
 * Also handles the creation of ORDER-BY clauses.
 * 
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WhereBuilder {

    private static final ILogger LOG = LoggerFactory.getLogger( WhereBuilder.class );

    // database specific SRS code for unspecified SRS
    protected static final int SRS_UNDEFINED = -1;

    /** Targeted feature types. */
    protected MappedFeatureType[] rootFts;

    /** {@link Filter} for which the corresponding WHERE-clause will be generated. */
    protected Filter filter;

    protected SortProperty[] sortProperties;

    protected VirtualContentProvider vcProvider;

    protected QueryTableTree queryTableTree;

    protected List<PropertyPath> filterPropertyPaths = new ArrayList<PropertyPath>();

    protected List<PropertyPath> sortPropertyPaths = new ArrayList<PropertyPath>();

    private Hashtable<String, String> functionMap = new Hashtable<String, String>();

    /**
     * Creates a new <code>WhereBuilder</code> instance.
     * 
     * @param rootFts
     *            selected feature types, more than one type means that the types are joined
     * @param aliases
     *            aliases for the feature types, may be null (must have same length as rootFts otherwise)
     * @param filter
     * @param sortProperties
     * @param aliasGenerator
     *            aliasGenerator to be used to generate table aliases, may be null
     * @param vcProvider
     * @throws DatastoreException
     */
    public WhereBuilder( MappedFeatureType[] rootFts, String[] aliases, Filter filter, SortProperty[] sortProperties,
                         TableAliasGenerator aliasGenerator, VirtualContentProvider vcProvider )
                            throws DatastoreException {

        this.rootFts = rootFts;
        this.queryTableTree = new QueryTableTree( rootFts, aliases, aliasGenerator );

        // add filter properties to the QueryTableTree
        this.filter = filter;
        if ( filter != null ) {
            assert filter instanceof ComplexFilter || filter instanceof FeatureFilter;
            buildFilterPropertyNameMap();
            for ( PropertyPath property : this.filterPropertyPaths ) {
                this.queryTableTree.addFilterProperty( property );
            }
            fillFunctionNameMap();
        }

        // add sort properties to the QueryTableTree
        this.sortProperties = sortProperties;
        if ( sortProperties != null ) {
            for ( SortProperty property : sortProperties ) {
                this.sortPropertyPaths.add( property.getSortProperty() );
                this.queryTableTree.addSortProperty( property.getSortProperty() );
            }
        }

        this.vcProvider = vcProvider;

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "QueryTableTree:\n" + this.queryTableTree );
        }
    }

    /**
     * Returns the table alias used for the specified root feature type.
     * 
     * @param i
     *            index of the requested root feature type
     * @return the alias used for the root table
     */
    public String getRootTableAlias( int i ) {
        return this.queryTableTree.getRootNodes()[i].getTableAlias();
    }

    /**
     * Returns the associated <code>Filter</code> instance.
     * 
     * @return the associated <code>Filter</code> instance
     */
    public Filter getFilter() {
        return this.filter;
    }

    protected MappedGeometryPropertyType getGeometryProperty( PropertyName propName ) {
        PropertyPath propertyPath = propName.getValue();
        PropertyNode propertyNode = this.queryTableTree.getPropertyNode( propertyPath );
        assert propertyNode != null;
        assert propertyNode instanceof GeometryPropertyNode;
        LOG.logDebug( "Found geometry property with path: " + propertyPath + " and propName: "
                      + propertyNode.getProperty().getName() );
        return (MappedGeometryPropertyType) propertyNode.getProperty();
    }

    // /**
    // * Returns the SRS of the {@link MappedGeometryPropertyType} that is identified by the given
    // * {@link PropertyPath}.
    // *
    // * @param propertyPath
    // * @return the default SRS of the geometry property type
    // */
    // protected String getSrs( PropertyPath propertyPath ) {
    // PropertyNode propertyNode = this.queryTableTree.getPropertyNode( propertyPath );
    // assert propertyNode != null;
    // assert propertyNode instanceof GeometryPropertyNode;
    // MappedGeometryPropertyType geoProp = (MappedGeometryPropertyType) propertyNode.getProperty();
    // return geoProp.getSRS().toString();
    // }
    //
    // /**
    // * Returns the internal Srs of the {@link MappedGeometryPropertyType} that is identified by
    // the
    // * given {@link PropertyPath}.
    // *
    // * @param propertyPath
    // * @return the default SRS of the geometry property type
    // */
    // protected int getInternalSrsCode( PropertyPath propertyPath ) {
    // PropertyNode propertyNode = this.queryTableTree.getPropertyNode( propertyPath );
    // assert propertyNode != null;
    // assert propertyNode instanceof GeometryPropertyNode;
    // MappedGeometryPropertyType geoProp = (MappedGeometryPropertyType) propertyNode.getProperty();
    // return geoProp.getMappingField().getSRS();
    // }

    protected int getPropertyNameSQLType( PropertyName propertyName ) {

        PropertyPath propertyPath = propertyName.getValue();
        PropertyNode propertyNode = this.queryTableTree.getPropertyNode( propertyPath );
        assert propertyNode != null;

        // can be useful if the assertions are not enabled
        if ( propertyNode == null ) {
            LOG.logDebug( "Null propertyNode for propertyName: " + propertyName + " with queryTable: "
                          + this.queryTableTree );
        }

        MappedPropertyType propertyType = propertyNode.getProperty();
        if ( !( propertyType instanceof MappedSimplePropertyType ) ) {
            String msg = "Error in WhereBuilder: cannot compare against properties of type '" + propertyType.getClass()
                         + "'.";
            LOG.logError( msg );
            throw new RuntimeException( msg );
        }

        SimpleContent content = ( (MappedSimplePropertyType) propertyType ).getContent();
        if ( !( content instanceof MappingField ) ) {
            String msg = "Virtual properties are currently ignored in WhereBuilder#getPropertyNameSQLType(PropertyName).";
            LOG.logWarning( msg );
            return Types.VARCHAR;
        }

        int targetSqlType = ( (MappingField) content ).getType();
        return targetSqlType;
    }

    protected void buildFilterPropertyNameMap()
                            throws PropertyPathResolvingException {
        if ( this.filter instanceof ComplexFilter ) {
            buildPropertyNameMapFromOperation( ( (ComplexFilter) this.filter ).getOperation() );
        } else if ( this.filter instanceof FeatureFilter ) {
            // TODO
            // throw new PropertyPathResolvingException( "FeatureFilter not implemented yet." );
        }
    }

    private void buildPropertyNameMapFromOperation( Operation operation )
                            throws PropertyPathResolvingException {
        switch ( OperationDefines.getTypeById( operation.getOperatorId() ) ) {
        case OperationDefines.TYPE_SPATIAL: {
            PropertyName name = ( (SpatialOperation) operation ).getPropertyName();

            // possible in case of BBOX
            if ( name == null && operation.getOperatorId() == BBOX ) {
                // use first ft and first geom property
                FeatureType ft = queryTableTree.getRootNodes()[0].getFeatureType();
                name = new PropertyName( ft.getGeometryProperties()[0].getName() );
                // modify the operation, since the property name is used later on from the datastores' where builders
                ( (SpatialOperation) operation ).setPropertyName( name );
            }
            registerPropertyName( name );
            break;
        }
        case OperationDefines.TYPE_COMPARISON: {
            buildPropertyNameMap( (ComparisonOperation) operation );
            break;
        }
        case OperationDefines.TYPE_LOGICAL: {
            buildPropertyNameMap( (LogicalOperation) operation );
            break;
        }
        default: {
            break;
        }
        }
    }

    private void buildPropertyNameMap( ComparisonOperation operation )
                            throws PropertyPathResolvingException {
        switch ( operation.getOperatorId() ) {
        case OperationDefines.PROPERTYISEQUALTO:
        case OperationDefines.PROPERTYISNOTEQUALTO:
        case OperationDefines.PROPERTYISLESSTHAN:
        case OperationDefines.PROPERTYISGREATERTHAN:
        case OperationDefines.PROPERTYISLESSTHANOREQUALTO:
        case OperationDefines.PROPERTYISGREATERTHANOREQUALTO: {
            buildPropertyNameMap( ( (PropertyIsCOMPOperation) operation ).getFirstExpression() );
            buildPropertyNameMap( ( (PropertyIsCOMPOperation) operation ).getSecondExpression() );
            break;
        }
        case OperationDefines.PROPERTYISLIKE: {
            registerPropertyName( ( (PropertyIsLikeOperation) operation ).getPropertyName() );
            break;
        }
        case OperationDefines.PROPERTYISNULL: {
            buildPropertyNameMap( ( (PropertyIsNullOperation) operation ).getPropertyName() );
            break;
        }
        case OperationDefines.PROPERTYISBETWEEN: {
            buildPropertyNameMap( ( (PropertyIsBetweenOperation) operation ).getLowerBoundary() );
            buildPropertyNameMap( ( (PropertyIsBetweenOperation) operation ).getUpperBoundary() );
            registerPropertyName( ( (PropertyIsBetweenOperation) operation ).getPropertyName() );
            break;
        }
        default: {
            break;
        }
        }
    }

    private void buildPropertyNameMap( LogicalOperation operation )
                            throws PropertyPathResolvingException {
        List<?> operationList = operation.getArguments();
        Iterator<?> it = operationList.iterator();
        while ( it.hasNext() ) {
            buildPropertyNameMapFromOperation( (Operation) it.next() );
        }
    }

    private void buildPropertyNameMap( Expression expression )
                            throws PropertyPathResolvingException {
        switch ( expression.getExpressionId() ) {
        case ExpressionDefines.PROPERTYNAME: {
            registerPropertyName( (PropertyName) expression );
            break;
        }
        case ExpressionDefines.ADD:
        case ExpressionDefines.SUB:
        case ExpressionDefines.MUL:
        case ExpressionDefines.DIV: {
            buildPropertyNameMap( ( (ArithmeticExpression) expression ).getFirstExpression() );
            buildPropertyNameMap( ( (ArithmeticExpression) expression ).getSecondExpression() );
            break;
        }
        case ExpressionDefines.FUNCTION: {
            // TODO: What about PropertyNames used here?
            break;
        }
        case ExpressionDefines.EXPRESSION:
        case ExpressionDefines.LITERAL: {
            break;
        }
        }
    }

    private void registerPropertyName( PropertyName propertyName ) {
        this.filterPropertyPaths.add( propertyName.getValue() );
    }

    /**
     * Appends the alias-qualified, comma separated list of all tables to be joined in order to represent the associated
     * filter expression (and possibly feature type joins).
     * <p>
     * The list consist of left outer joins ("x LEFT OUTER JOIN y") and cross-product joins ("x,y"):
     * <ul>
     * <li>left outer joins are generated for each join that is necessary, because of filter expressions that target
     * properties stored in related tables (condition joins)
     * <li>cross-product joins are generated for all feature type root tables (feature type joins) that have not joined
     * by filter expression joins before</li>
     * </ul>
     * 
     * @param query
     *            the list is appended to this <code>SQLStatement</code>
     */
    public void appendJoinTableList( StatementBuffer query ) {

        FeatureTypeNode[] rootNodes = this.queryTableTree.getRootNodes();
        appendOuterJoinTableList( query, rootNodes[0] );
        for ( int i = 1; i < rootNodes.length; i++ ) {
            query.append( ',' );
            appendOuterJoinTableList( query, rootNodes[i] );
        }
    }

    /**
     * Appends the alias-qualified, comma separated list of tables to be joined (for one root feature type node). This
     * includes the join conditions (necessary for the filter conditions), which are generated in ANSI-SQL left outer
     * join style.
     * 
     * @param query
     *            the list is appended to this <code>SQLStatement</code>
     * @param rootNode
     *            one root feature type node in the <code>QueryTableTree</code>
     */
    private void appendOuterJoinTableList( StatementBuffer query, FeatureTypeNode rootNode ) {

        query.append( rootNode.getTable() );
        query.append( ' ' );
        query.append( rootNode.getTableAlias() );
        Stack<PropertyNode> propertyNodeStack = new Stack<PropertyNode>();
        PropertyNode[] propertyNodes = rootNode.getPropertyNodes();
        for ( int i = 0; i < propertyNodes.length; i++ ) {
            propertyNodeStack.push( propertyNodes[i] );
        }

        while ( !propertyNodeStack.isEmpty() ) {
            PropertyNode currentNode = propertyNodeStack.pop();
            String fromAlias = currentNode.getParent().getTableAlias();
            TableRelation[] tableRelations = currentNode.getPathFromParent();
            if ( tableRelations != null && tableRelations.length != 0 ) {
                String[] toAliases = currentNode.getTableAliases();
                appendOuterJoins( tableRelations, fromAlias, toAliases, query );

            }
            if ( currentNode instanceof FeaturePropertyNode ) {
                FeaturePropertyNode featurePropertyNode = (FeaturePropertyNode) currentNode;
                FeatureTypeNode[] childNodes = ( (FeaturePropertyNode) currentNode ).getFeatureTypeNodes();
                for ( int i = 0; i < childNodes.length; i++ ) {
                    // TODO is this way of skipping root tables o.k.?
                    if ( childNodes[i].getFtAlias() != null ) {
                        continue;
                    }
                    String toTable = childNodes[i].getTable();
                    String toAlias = childNodes[i].getTableAlias();
                    String[] pathAliases = featurePropertyNode.getTableAliases();
                    if ( pathAliases.length == 0 ) {
                        fromAlias = featurePropertyNode.getParent().getTableAlias();
                    } else {
                        fromAlias = pathAliases[pathAliases.length - 1];
                    }
                    MappedFeaturePropertyType content = (MappedFeaturePropertyType) featurePropertyNode.getProperty();
                    TableRelation[] relations = content.getTableRelations();
                    TableRelation relation = relations[relations.length - 1];
                    appendOuterJoin( relation, fromAlias, toAlias, toTable, query );
                    propertyNodes = childNodes[i].getPropertyNodes();
                    for ( int j = 0; j < propertyNodes.length; j++ ) {
                        propertyNodeStack.push( propertyNodes[j] );
                    }
                }
            }
        }
    }

    private void appendOuterJoins( TableRelation[] tableRelation, String fromAlias, String[] toAliases,
                                   StatementBuffer query ) {
        for ( int i = 0; i < toAliases.length; i++ ) {
            String toAlias = toAliases[i];
            appendOuterJoin( tableRelation[i], fromAlias, toAlias, query );
            fromAlias = toAlias;
        }
    }

    private void appendOuterJoin( TableRelation tableRelation, String fromAlias, String toAlias, StatementBuffer query ) {

        query.append( " LEFT OUTER JOIN " );
        query.append( tableRelation.getToTable() );
        query.append( " " );
        query.append( toAlias );
        query.append( " ON " );

        MappingField[] fromFields = tableRelation.getFromFields();
        MappingField[] toFields = tableRelation.getToFields();
        for ( int i = 0; i < fromFields.length; i++ ) {
            if ( toAlias.equals( "" ) ) {
                toAlias = tableRelation.getToTable();
            }
            query.append( toAlias );
            query.append( "." );
            query.append( toFields[i].getField() );
            query.append( "=" );
            if ( fromAlias.equals( "" ) ) {
                fromAlias = tableRelation.getFromTable();
            }
            query.append( fromAlias );
            query.append( "." );
            query.append( fromFields[i].getField() );
            if ( i != fromFields.length - 1 ) {
                query.append( " AND " );
            }
        }
    }

    private void appendOuterJoin( TableRelation tableRelation, String fromAlias, String toAlias, String toTable,
                                  StatementBuffer query ) {

        query.append( " LEFT OUTER JOIN " );
        query.append( toTable );
        query.append( " " );
        query.append( toAlias );
        query.append( " ON " );

        MappingField[] fromFields = tableRelation.getFromFields();
        MappingField[] toFields = tableRelation.getToFields();
        for ( int i = 0; i < fromFields.length; i++ ) {
            if ( toAlias.equals( "" ) ) {
                toAlias = toTable;
            }
            query.append( toAlias );
            query.append( "." );
            query.append( toFields[i].getField() );
            query.append( "=" );
            if ( fromAlias.equals( "" ) ) {
                fromAlias = tableRelation.getFromTable();
            }
            query.append( fromAlias );
            query.append( "." );
            query.append( fromFields[i].getField() );
            if ( i != fromFields.length - 1 ) {
                query.append( " AND " );
            }
        }
    }

    /**
     * Appends an SQL WHERE-condition corresponding to the <code>Filter</code> to the given SQL statement.
     * 
     * @param query
     * @throws DatastoreException
     */
    public final void appendWhereCondition( StatementBuffer query )
                            throws DatastoreException {
        if ( filter instanceof ComplexFilter ) {
            query.append( " WHERE " );
            appendComplexFilterAsSQL( query, (ComplexFilter) filter );
        } else if ( filter instanceof FeatureFilter ) {
            FeatureFilter featureFilter = (FeatureFilter) filter;
            if ( featureFilter.getFeatureIds().size() > 0 ) {
                query.append( " WHERE " );
                appendFeatureFilterAsSQL( query, featureFilter );
            }
        } else if ( filter != null ) {
            assert false : "Unexpected filter type: " + filter.getClass();
        }
    }

    /**
     * Appends an SQL "ORDER BY"-condition that corresponds to the sort properties of the query to the given SQL
     * statement.
     * 
     * @param query
     * @throws DatastoreException
     */
    public void appendOrderByCondition( StatementBuffer query )
                            throws DatastoreException {

        // ignore properties that are unsuitable as sort criteria (like constant properties)
        List<SortProperty> sortProps = new ArrayList<SortProperty>();

        if ( this.sortProperties != null && this.sortProperties.length != 0 ) {
            for ( int i = 0; i < this.sortProperties.length; i++ ) {
                SortProperty sortProperty = this.sortProperties[i];
                PropertyPath path = sortProperty.getSortProperty();
                PropertyNode propertyNode = this.queryTableTree.getPropertyNode( path );
                MappedPropertyType pt = propertyNode.getProperty();
                if ( !( pt instanceof MappedSimplePropertyType ) ) {
                    String msg = Messages.getMessage( "DATASTORE_INVALID_SORT_PROPERTY", pt.getName() );
                    throw new DatastoreException( msg );
                }
                SimpleContent content = ( (MappedSimplePropertyType) pt ).getContent();
                if ( content.isSortable() ) {
                    sortProps.add( sortProperty );
                } else {
                    String msg = "Ignoring sort criterion - property '" + path.getAsString()
                                 + "' is not suitable for sorting.";
                    LOG.logDebug( msg );
                }
            }
        }

        if ( sortProps.size() > 0 ) {
            query.append( " ORDER BY " );
        }

        for ( int i = 0; i < sortProps.size(); i++ ) {
            SortProperty sortProperty = sortProps.get( i );
            PropertyPath path = sortProperty.getSortProperty();
            appendPropertyPathAsSQL( query, path );
            if ( !sortProperty.getSortOrder() ) {
                query.append( " DESC" );
            }
            if ( i != sortProps.size() - 1 ) {
                query.append( ',' );
            }
        }
    }

    /**
     * Appends an SQL fragment for the given object.
     * 
     * @param query
     * @param filter
     * @throws DatastoreException
     */
    protected void appendComplexFilterAsSQL( StatementBuffer query, ComplexFilter filter )
                            throws DatastoreException {
        appendOperationAsSQL( query, filter.getOperation() );
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param operation
     * @throws DatastoreException
     */
    protected void appendOperationAsSQL( StatementBuffer query, Operation operation )
                            throws DatastoreException {

        switch ( OperationDefines.getTypeById( operation.getOperatorId() ) ) {
        case OperationDefines.TYPE_SPATIAL: {
            appendSpatialOperationAsSQL( query, (SpatialOperation) operation );
            break;
        }
        case OperationDefines.TYPE_COMPARISON: {
            try {
                appendComparisonOperationAsSQL( query, (ComparisonOperation) operation );
            } catch ( FilterEvaluationException e ) {
                LOG.logDebug( "Stack trace of eaten exception: ", e );
                // TODO unknown what will break if this exception is actually thrown
                // new DatastoreException( e.getMessage(), e );
            }
            break;
        }
        case OperationDefines.TYPE_LOGICAL: {
            appendLogicalOperationAsSQL( query, (LogicalOperation) operation );
            break;
        }
        default: {
            break;
        }
        }
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param operation
     * @throws FilterEvaluationException
     */
    protected void appendComparisonOperationAsSQL( StatementBuffer query, ComparisonOperation operation )
                            throws FilterEvaluationException {
        switch ( operation.getOperatorId() ) {
        case OperationDefines.PROPERTYISEQUALTO:
        case OperationDefines.PROPERTYISNOTEQUALTO:
        case OperationDefines.PROPERTYISLESSTHAN:
        case OperationDefines.PROPERTYISGREATERTHAN:
        case OperationDefines.PROPERTYISLESSTHANOREQUALTO:
        case OperationDefines.PROPERTYISGREATERTHANOREQUALTO: {
            appendPropertyIsCOMPOperationAsSQL( query, (PropertyIsCOMPOperation) operation );
            break;
        }
        case OperationDefines.PROPERTYISLIKE: {
            appendPropertyIsLikeOperationAsSQL( query, (PropertyIsLikeOperation) operation );
            break;
        }
        case OperationDefines.PROPERTYISNULL: {
            appendPropertyIsNullOperationAsSQL( query, (PropertyIsNullOperation) operation );
            break;
        }
        case OperationDefines.PROPERTYISBETWEEN: {
            appendPropertyIsBetweenOperationAsSQL( query, (PropertyIsBetweenOperation) operation );
            break;
        }
        }
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param operation
     * @throws FilterEvaluationException
     */
    protected void appendPropertyIsCOMPOperationAsSQL( StatementBuffer query, PropertyIsCOMPOperation operation )
                            throws FilterEvaluationException {
        Expression firstExpr = operation.getFirstExpression();
        if ( !( firstExpr instanceof PropertyName ) ) {
            throw new IllegalArgumentException( "First expression in a comparison must "
                                                + "always be a 'PropertyName' element." );
        }
        int targetSqlType = getPropertyNameSQLType( (PropertyName) firstExpr );
        if ( operation.isMatchCase() ) {
            appendExpressionAsSQL( query, firstExpr, targetSqlType );
        } else {
            List<Expression> list = new ArrayList<Expression>();
            list.add( firstExpr );
            Function func = new DBFunction( getFunctionName( "LOWER" ), list );
            appendFunctionAsSQL( query, func, targetSqlType );
        }
        switch ( operation.getOperatorId() ) {
        case OperationDefines.PROPERTYISEQUALTO: {
            query.append( " = " );
            break;
        }
        case OperationDefines.PROPERTYISNOTEQUALTO: {
            query.append( " <> " );
            break;
        }
        case OperationDefines.PROPERTYISLESSTHAN: {
            query.append( " < " );
            break;
        }
        case OperationDefines.PROPERTYISGREATERTHAN: {
            query.append( " > " );
            break;
        }
        case OperationDefines.PROPERTYISLESSTHANOREQUALTO: {
            query.append( " <= " );
            break;
        }
        case OperationDefines.PROPERTYISGREATERTHANOREQUALTO: {
            query.append( " >= " );
            break;
        }
        }
        if ( operation.isMatchCase() ) {
            appendExpressionAsSQL( query, operation.getSecondExpression(), targetSqlType );
        } else {
            List<Expression> list = new ArrayList<Expression>();
            list.add( operation.getSecondExpression() );
            Function func = new DBFunction( getFunctionName( "LOWER" ), list );
            appendFunctionAsSQL( query, func, targetSqlType );
        }
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param operation
     * @throws FilterEvaluationException
     */
    protected void appendPropertyIsLikeOperationAsSQL( StatementBuffer query, PropertyIsLikeOperation operation )
                            throws FilterEvaluationException {

        String literal = operation.getLiteral().getValue();
        String escape = "" + operation.getEscapeChar();
        String wildCard = "" + operation.getWildCard();
        String singleChar = "" + operation.getSingleChar();

        SpecialCharString specialString = new SpecialCharString( literal, wildCard, singleChar, escape );
        String sqlEncoded = specialString.toSQLStyle( !operation.isMatchCase() );

        int targetSqlType = getPropertyNameSQLType( operation.getPropertyName() );

        // if isMatchCase == false surround first argument with LOWER (...) and convert characters
        // in second argument to lower case
        if ( operation.isMatchCase() ) {
            appendPropertyNameAsSQL( query, operation.getPropertyName() );
        } else {
            List<Expression> list = new ArrayList<Expression>();
            list.add( operation.getPropertyName() );
            Function func = new DBFunction( getFunctionName( "LOWER" ), list );
            appendFunctionAsSQL( query, func, targetSqlType );
        }

        query.append( " LIKE ? ESCAPE ?" );
        query.addArgument( sqlEncoded, Types.VARCHAR );
        query.addArgument( "\\", Types.VARCHAR );
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param operation
     */
    protected void appendPropertyIsNullOperationAsSQL( StatementBuffer query, PropertyIsNullOperation operation ) {
        appendPropertyNameAsSQL( query, operation.getPropertyName() );
        query.append( " IS NULL" );
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param operation
     * @throws FilterEvaluationException
     */
    protected void appendPropertyIsBetweenOperationAsSQL( StatementBuffer query, PropertyIsBetweenOperation operation )
                            throws FilterEvaluationException {

        PropertyName propertyName = operation.getPropertyName();
        int targetSqlType = getPropertyNameSQLType( propertyName );
        appendExpressionAsSQL( query, operation.getLowerBoundary(), targetSqlType );
        query.append( " <= " );
        appendPropertyNameAsSQL( query, propertyName );
        query.append( " AND " );
        appendPropertyNameAsSQL( query, propertyName );
        query.append( " <= " );
        appendExpressionAsSQL( query, operation.getUpperBoundary(), targetSqlType );
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param expression
     * @param targetSqlType
     *            sql type code to be used for literals at the bottom of the expression tree
     * @throws FilterEvaluationException
     */
    protected void appendExpressionAsSQL( StatementBuffer query, Expression expression, int targetSqlType )
                            throws FilterEvaluationException {
        switch ( expression.getExpressionId() ) {
        case ExpressionDefines.PROPERTYNAME: {
            appendPropertyNameAsSQL( query, (PropertyName) expression );
            break;
        }
        case ExpressionDefines.LITERAL: {
            appendLiteralAsSQL( query, (Literal) expression, targetSqlType );
            break;
        }
        case ExpressionDefines.FUNCTION: {
            Function function = (Function) expression;
            appendFunctionAsSQL( query, function, targetSqlType );
            break;
        }
        case ExpressionDefines.ADD:
        case ExpressionDefines.SUB:
        case ExpressionDefines.MUL:
        case ExpressionDefines.DIV: {
            appendArithmeticExpressionAsSQL( query, (ArithmeticExpression) expression, targetSqlType );
            break;
        }
        case ExpressionDefines.EXPRESSION:
        default: {
            throw new IllegalArgumentException( "Unexpected expression type: " + expression.getExpressionName() );
        }
        }
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param literal
     * @param targetSqlType
     */
    protected void appendLiteralAsSQL( StatementBuffer query, Literal literal, int targetSqlType ) {
        query.append( '?' );
        query.addArgument( literal.getValue(), targetSqlType );
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param propertyName
     */
    protected void appendPropertyNameAsSQL( StatementBuffer query, PropertyName propertyName ) {

        PropertyPath propertyPath = propertyName.getValue();
        appendPropertyPathAsSQL( query, propertyPath );
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param propertyPath
     */
    protected void appendPropertyPathAsSQL( StatementBuffer query, PropertyPath propertyPath ) {

        LOG.logDebug( "Looking up '" + propertyPath + "' in the query table tree." );
        MappingField mappingField = null;
        PropertyNode propertyNode = this.queryTableTree.getPropertyNode( propertyPath );
        assert ( propertyNode != null );
        if ( propertyNode instanceof SimplePropertyNode ) {
            SimpleContent content = ( (MappedSimplePropertyType) ( propertyNode.getProperty() ) ).getContent();
            if ( !( content instanceof MappingField ) ) {
                if ( content instanceof ConstantContent ) {
                    query.append( "'" + ( (ConstantContent) content ).getValue() + "'" );
                    return;
                } else if ( content instanceof SQLFunctionCall ) {
                    SQLFunctionCall call = (SQLFunctionCall) content;
                    String tableAlias = null;
                    String[] tableAliases = propertyNode.getTableAliases();
                    if ( tableAliases == null || tableAliases.length == 0 ) {
                        tableAlias = propertyNode.getParent().getTableAlias();
                    } else {
                        tableAlias = tableAliases[tableAliases.length - 1];
                    }
                    this.vcProvider.appendSQLFunctionCall( query, tableAlias, call );
                    return;
                }
                String msg = "Virtual properties are currently ignored in WhereBuilder#appendPropertyPathAsSQL(StatementBuffer,PropertyPath).";
                LOG.logWarning( msg );
                assert false;
            }
            mappingField = (MappingField) content;
        } else if ( propertyNode instanceof GeometryPropertyNode ) {
            mappingField = ( (MappedGeometryPropertyType) propertyNode.getProperty() ).getMappingField();
        } else {
            String msg = "Internal error in WhereBuilder: unhandled PropertyNode type: '"
                         + propertyNode.getClass().getName() + "'.";
            LOG.logError( msg );
            throw new RuntimeException( msg );
        }
        String tableAlias = null;
        String[] tableAliases = propertyNode.getTableAliases();
        if ( tableAliases == null || tableAliases.length == 0 ) {
            tableAlias = propertyNode.getParent().getTableAlias();
        } else {
            tableAlias = tableAliases[tableAliases.length - 1];
        }
        if ( tableAlias != "" ) {
            query.append( tableAlias );
            query.append( '.' );
        } else {
            query.append( mappingField.getTable() );
            query.append( '.' );
        }
        query.append( mappingField.getField() );
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param expression
     * @param targetSqlType
     * @throws FilterEvaluationException
     */
    protected void appendArithmeticExpressionAsSQL( StatementBuffer query, ArithmeticExpression expression,
                                                    int targetSqlType )
                            throws FilterEvaluationException {

        // quirk to enable SQL-level type conversions of numbers. We really should use numbers as literals in the SQL
        // string.
        switch ( targetSqlType ) {
        case INTEGER:
        case SMALLINT: {
            targetSqlType = DOUBLE;
        }
        }

        query.append( '(' );
        appendExpressionAsSQL( query, expression.getFirstExpression(), targetSqlType );
        switch ( expression.getExpressionId() ) {
        case ADD: {
            query.append( '+' );
            break;
        }
        case SUB: {
            query.append( '-' );
            break;
        }
        case MUL: {
            query.append( '*' );
            break;
        }
        case DIV: {
            query.append( '/' );
            break;
        }
        }
        appendExpressionAsSQL( query, expression.getSecondExpression(), targetSqlType );
        query.append( ')' );
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param function
     * @param targetSqlType
     * @throws FilterEvaluationException
     */
    protected void appendFunctionAsSQL( StatementBuffer query, Function function, int targetSqlType )
                            throws FilterEvaluationException {
        if ( function instanceof DBFunction ) {
            query.append( function.getName() );
            query.append( " (" );
            List<?> list = function.getArguments();
            for ( int i = 0; i < list.size(); i++ ) {
                Expression expression = (Expression) list.get( i );
                appendExpressionAsSQL( query, expression, targetSqlType );
                if ( i != list.size() - 1 )
                    query.append( ", " );
            }
            query.append( ")" );
        } else {
            Object o = function.evaluate( null );
            if ( o != null ) {
                Literal literal = new Literal( o.toString() );
                appendExpressionAsSQL( query, literal, targetSqlType );
            }
        }
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param operation
     * @throws DatastoreException
     */
    protected void appendLogicalOperationAsSQL( StatementBuffer query, LogicalOperation operation )
                            throws DatastoreException {
        List<?> argumentList = operation.getArguments();
        switch ( operation.getOperatorId() ) {
        case OperationDefines.AND: {
            for ( int i = 0; i < argumentList.size(); i++ ) {
                Operation argument = (Operation) argumentList.get( i );
                query.append( '(' );
                appendOperationAsSQL( query, argument );
                query.append( ')' );
                if ( i != argumentList.size() - 1 )
                    query.append( " AND " );
            }
            break;
        }
        case OperationDefines.OR: {
            for ( int i = 0; i < argumentList.size(); i++ ) {
                Operation argument = (Operation) argumentList.get( i );
                query.append( '(' );
                appendOperationAsSQL( query, argument );
                query.append( ')' );
                if ( i != argumentList.size() - 1 )
                    query.append( " OR " );
            }
            break;
        }
        case OperationDefines.NOT: {
            Operation argument = (Operation) argumentList.get( 0 );
            query.append( "NOT (" );
            appendOperationAsSQL( query, argument );
            query.append( ')' );
            break;
        }
        }
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * TODO Handle compound primary keys correctly.
     * 
     * @param query
     * @param filter
     * @throws DatastoreException
     */
    protected void appendFeatureFilterAsSQL( StatementBuffer query, FeatureFilter filter )
                            throws DatastoreException {

        // List list = filter.getFeatureIds();
        // Iterator it = list.iterator();
        // while (it.hasNext()) {
        // FeatureId fid = (FeatureId) it.next();
        // MappingField mapping = null;
        // DatastoreMapping mapping = featureType.getFidDefinition().getFidFields()[0];
        // query.append( ' ' );
        // query.append( this.joinTableTree.getAlias() );
        // query.append( "." );
        // query.append( mapping.getField() );
        // query.append( "=?" );
        // query.addArgument( fid.getValue() );
        // if ( it.hasNext() ) {
        // query.append( " OR" );
        // }
        // }

        if ( this.rootFts.length > 1 ) {
            String msg = Messages.getMessage( "DATASTORE_FEATURE_QUERY_MORE_THAN_FEATURE_TYPE" );
            throw new DatastoreException( msg );
        }

        MappedFeatureType rootFt = this.rootFts[0];
        MappingField[] idFields = rootFt.getGMLId().getIdFields();
        MappingField idField = null;
        String tbl = getRootTableAlias( 0 );
        ArrayList<?> list = filter.getFeatureIds();
        try {
            for ( int i = 0; i < idFields.length; i++ ) {
                idField = idFields[i];
                if ( i == 0 ) {
                    query.append( ' ' );
                } else {
                    query.append( " AND " );
                }
                if ( null != tbl && 0 < tbl.length() ) {
                    query.append( tbl );
                    query.append( "." );
                }
                query.append( idField.getField() );
                for ( int j = 0; j < list.size(); j++ ) {
                    if ( j == 0 ) {
                        query.append( " IN (?" );
                    } else {
                        query.append( ",?" );
                    }
                    if ( j == list.size() - 1 ) {
                        query.append( ")" );
                    }
                    String fid = ( (org.deegree.model.filterencoding.FeatureId) list.get( j ) ).getValue();
                    Object fidValue = org.deegree.io.datastore.FeatureId.removeFIDPrefix( fid, rootFt.getGMLId() );
                    if ( idFields.length > 1 ) { // Equal to: if ( fidValue instanceof Object[] ) {
                        fidValue = ( (Object[]) fidValue )[i];
                    }
                    query.addArgument( fidValue, idField.getType() );
                }
            }
        } catch ( Exception e ) {
            LOG.logError( "Error converting feature id", e );
        }
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement. As this depends on the handling of
     * geometry data by the concrete database in use, this method must be overwritten by any datastore implementation
     * that has spatial capabilities.
     * 
     * @param query
     * @param operation
     * @throws DatastoreException
     */
    protected void appendSpatialOperationAsSQL( @SuppressWarnings("unused") StatementBuffer query,
                                                @SuppressWarnings("unused") SpatialOperation operation )
                            throws DatastoreException {
        String msg = "Spatial operations are not supported by the WhereBuilder implementation in use: '" + getClass()
                     + "'";
        throw new DatastoreException( msg );
    }

    /**
     * Prepares the function map for functions with implementation specific names, e.g. upper case conversion in ORACLE
     * = UPPER(string); POSTGRES = UPPER(string), and MS Access = UCase(string). Default SQL-function name map function
     * 'UPPER' is 'UPPER'. If this function shall be used with user databases e.g. SQLServer a specialized WhereBuilder
     * must override this method.
     */
    protected void fillFunctionNameMap() {
        functionMap.clear();
        functionMap.put( "LOWER", "LOWER" );
    }

    /**
     * Get the function with the specified name.
     * 
     * @param name
     *            the function name
     * @return the mapped function name
     */
    protected String getFunctionName( String name ) {
        String f = functionMap.get( name );
        if ( null == f )
            f = name;
        return f;
    }
}
