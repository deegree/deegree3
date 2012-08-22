//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2006 by: M.O.S.S. Computer Grafik Systeme GmbH
 Hohenbrunner Weg 13
 D-82024 Taufkirchen
 http://www.moss.de/

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 ---------------------------------------------------------------------------*/
package org.deegree.io.datastore.sde;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGeometryPropertyType;
import org.deegree.io.datastore.schema.MappedPropertyType;
import org.deegree.io.datastore.schema.MappedSimplePropertyType;
import org.deegree.io.datastore.schema.content.MappingField;
import org.deegree.io.datastore.schema.content.MappingGeometryField;
import org.deegree.io.datastore.schema.content.SimpleContent;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.datastore.sql.wherebuilder.GeometryPropertyNode;
import org.deegree.io.datastore.sql.wherebuilder.PropertyNode;
import org.deegree.io.datastore.sql.wherebuilder.QueryTableTree;
import org.deegree.io.datastore.sql.wherebuilder.SimplePropertyNode;
import org.deegree.io.sdeapi.SDEAdapter;
import org.deegree.model.filterencoding.ArithmeticExpression;
import org.deegree.model.filterencoding.ComparisonOperation;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.DBFunction;
import org.deegree.model.filterencoding.Expression;
import org.deegree.model.filterencoding.ExpressionDefines;
import org.deegree.model.filterencoding.FeatureFilter;
import org.deegree.model.filterencoding.FeatureId;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterTools;
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

import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeFilter;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeShapeFilter;

/**
 * <code>WhereBuilder</code> implementation for ArcSDE.
 * 
 * @author <a href="mailto:cpollmann@moss.de">Christoph Pollmann</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SDEWhereBuilder {

    private static final ILogger LOG = LoggerFactory.getLogger( SDEWhereBuilder.class );

    /**
     * the root feature type
     */
    protected MappedFeatureType rootFeatureType;

    /**
     * The filter to apply
     */
    protected Filter filter;
    

    /**
     * The query
     */
    protected QueryTableTree queryTableTree;

    /**
     * The filter property paths
     */
    protected List<PropertyPath> filterPropertyPaths = new ArrayList<PropertyPath>();

    /**
     * Creates a new instance of <code>SDEWhereBuilder</code> for the given parameters.
     * 
     * @param rootFts
     *            selected feature types, more than one type means that the types are joined
     * @param aliases
     *            aliases for the feature types, may be null (must have same length as rootFts
     *            otherwise)
     * @param filter
     * @param aliasGenerator
     * @throws DatastoreException
     */
    public SDEWhereBuilder( MappedFeatureType[] rootFts, String[] aliases, Filter filter,
                            TableAliasGenerator aliasGenerator ) throws DatastoreException {

        this.rootFeatureType = rootFts[0];
        this.queryTableTree = new QueryTableTree( rootFts, aliases, aliasGenerator );
        this.filter = filter;
        if ( filter != null ) {
            if ( !( filter instanceof ComplexFilter || filter instanceof FeatureFilter ) ) {
                throw new DatastoreException( "Invalid filter type: '" + filter.getClass()
                                              + "'. Filter must be a ComplexFilter or a FeatureFilter." );
            }
            buildFilterPropertyNameMap();
            for ( PropertyPath property : this.filterPropertyPaths ) {
                this.queryTableTree.addFilterProperty( property );
            }
        }
    }

    /**
     * @return the alias
     */
    public String getRootTableAlias() {
        return this.queryTableTree.getRootAlias();
    }

    /**
     * @return filter
     */
    public Filter getFilter() {
        return this.filter;
    }

    /**
     * Returns the internal (database specific) SRS code used in the geometry field of the given
     * <code>SpatialOperation</code>.
     * 
     * @param operation
     *            <code>SpatialOperation</code> for which the internal SRS is needed
     * @return the internal (database specific) SRS code.
     */
    protected int getInternalSRS( SpatialOperation operation ) {
        PropertyPath propertyPath = operation.getPropertyName().getValue();
        PropertyNode propertyNode = this.queryTableTree.getPropertyNode( propertyPath );
        if ( propertyNode == null ) {
            String msg = "Internal error in WhereBuilder: no PropertyNode for path '" + propertyPath
                         + "' in QueryTableTree.";
            LOG.logError( msg );
            throw new RuntimeException( msg );
        } else if ( !( propertyNode instanceof GeometryPropertyNode ) ) {
            String msg = "Internal error in WhereBuilder: unexpected PropertyNode type: '"
                         + propertyNode.getClass().getName() + "'. Must be a GeometryPropertyNode.";
            LOG.logError( msg );
            throw new RuntimeException( msg );
        }
        MappedGeometryPropertyType gpc = (MappedGeometryPropertyType) propertyNode.getProperty();
        MappingGeometryField field = gpc.getMappingField();
        return field.getSRS();
    }

    /**
     * @param propertyName
     * @return the type of the property name as an int
     */
    protected int getPropertyNameSQLType( PropertyName propertyName ) {
        PropertyPath propertyPath = propertyName.getValue();
        PropertyNode propertyNode = this.queryTableTree.getPropertyNode( propertyPath );
        if ( propertyNode == null ) {
            String msg = "Internal error in WhereBuilder: no PropertyNode for path '" + propertyPath
                         + "' in QueryTableTree.";
            LOG.logError( msg );
            throw new RuntimeException( msg );
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
            String msg = "Virtual properties are currently ignored in SDEWhereBuilder#getPropertyNameSQLType(PropertyName).";
            LOG.logError( msg );
            return Types.VARCHAR;
        }

        int targetSqlType = ( (MappingField) content ).getType();
        return targetSqlType;
    }

    /**
     * @throws PropertyPathResolvingException
     */
    protected void buildFilterPropertyNameMap()
                            throws PropertyPathResolvingException {
        if ( this.filter instanceof ComplexFilter ) {
            buildPropertyNameMapFromOperation( ( (ComplexFilter) this.filter ).getOperation() );
        } else if ( this.filter instanceof FeatureFilter ) {
            // FeatureFilter doesn't have real properties, so we don't have to add them here
            // maybe for join tables and table aliases we need some auxiliary constructions???
            // throw new PropertyPathResolvingException( "FeatureFilter not implemented yet." );
        }
    }

    private void buildPropertyNameMapFromOperation( Operation operation )
                            throws PropertyPathResolvingException {
        switch ( OperationDefines.getTypeById( operation.getOperatorId() ) ) {
        case OperationDefines.TYPE_SPATIAL: {
            registerPropertyName( ( (SpatialOperation) operation ).getPropertyName() );
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
        List<Operation> operationList = operation.getArguments();
        Iterator<Operation> it = operationList.iterator();
        while ( it.hasNext() ) {
            buildPropertyNameMapFromOperation( it.next() );
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

    /*
     * appendJoinTableList => String[] der Tabellennamen (mehrfach vorkommende Namen nicht erlaubt)
     * appendOuterJoins => mit SDE bei versionierten Tabellen realisierbar???
     */

    /**
     * Appends the SQL condition from the <code>Filter</code> to the given sql statement.
     * 
     * @param whereCondition
     */
    public final void appendWhereCondition( StringBuffer whereCondition ) {
        if ( filter instanceof ComplexFilter ) {
            appendComplexFilterAsSQL( whereCondition, (ComplexFilter) filter );
        } else if ( filter instanceof FeatureFilter ) {
            FeatureFilter featureFilter = (FeatureFilter) filter;
            if ( featureFilter.getFeatureIds().size() > 0 ) {
                appendFeatureFilterAsSQL( whereCondition, featureFilter );
            }
        } else {
            // assert false : "Unexpected filter type.";
        }
    }

    /**
     * Appends an SQL fragment for the given object.
     * 
     * @param query
     * @param filter
     */
    protected void appendComplexFilterAsSQL( StringBuffer query, ComplexFilter filter ) {
        appendOperationAsSQL( query, filter.getOperation() );
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param operation
     */
    protected void appendOperationAsSQL( StringBuffer query, Operation operation ) {

        switch ( OperationDefines.getTypeById( operation.getOperatorId() ) ) {
        case OperationDefines.TYPE_SPATIAL: {
            // handled seperately with buildSpatialFilter()
            break;
        }
        case OperationDefines.TYPE_COMPARISON: {
            appendComparisonOperationAsSQL( query, (ComparisonOperation) operation );
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
     */
    protected void appendComparisonOperationAsSQL( StringBuffer query, ComparisonOperation operation ) {
        switch ( operation.getOperatorId() ) {
        case OperationDefines.PROPERTYISEQUALTO:
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
     */
    protected void appendPropertyIsCOMPOperationAsSQL( StringBuffer query, PropertyIsCOMPOperation operation ) {
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
            Function func = new DBFunction( "LOWER", list );
            appendFunctionAsSQL( query, func, targetSqlType );
        }
        switch ( operation.getOperatorId() ) {
        case OperationDefines.PROPERTYISEQUALTO: {
            query.append( " = " );
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
            Function func = new DBFunction( "LOWER", list );
            appendFunctionAsSQL( query, func, targetSqlType );
        }
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement. Replacing and escape
     * handling is based on a finite automaton with 2 states:
     * <p>
     * (escapeMode)
     * <ul>
     * <li>' is appended as \', \ is appended as \\</li>
     * <li>every character (including the escapeChar) is simply appended</li>
     * <li>- unset escapeMode</li>
     * (escapeMode is false)
     * </ul>
     * <ul>
     * <li>' is appended as \', \ is appended as \\</li>
     * <li>escapeChar means: skip char, set escapeMode</li>
     * <li>wildCard means: append %</li>
     * <li>singleChar means: append ?</li>
     * </ul>
     * </p>
     * 
     * NOTE: Currently, the method uses a quirk and appends the generated argument inline, i.e. not
     * using query.addArgument(). This is because of a problem that occurred for example in
     * Postgresql; the execution of the inline version is *much* faster (at least with version 8.0).
     * 
     * @param query
     * @param operation
     */
    protected void appendPropertyIsLikeOperationAsSQL( StringBuffer query, PropertyIsLikeOperation operation ) {

        String literal = operation.getLiteral().getValue();
        char escapeChar = operation.getEscapeChar();
        char wildCard = operation.getWildCard();
        char singleChar = operation.getSingleChar();
        boolean escapeMode = false;
        int length = literal.length();
        int targetSqlType = getPropertyNameSQLType( operation.getPropertyName() );
        if ( operation.isMatchCase() ) {
            appendPropertyNameAsSQL( query, operation.getPropertyName() );
        } else {
            List<PropertyName> list = new ArrayList<PropertyName>();
            list.add( operation.getPropertyName() );
            Function func = new DBFunction( "LOWER", list );
            appendFunctionAsSQL( query, func, targetSqlType );
        }
        query.append( " LIKE '" );
        StringBuffer parameter = new StringBuffer();
        for ( int i = 0; i < length; i++ ) {
            char c = literal.charAt( i );
            if ( escapeMode ) {
                // ' must (even in escapeMode) be converted to \'
                if ( c == '\'' )
                    parameter.append( "\'" );
                // \ must (even in escapeMode) be converted to \\
                else if ( c == '\\' )
                    parameter.append( "\\\\" );
                else
                    parameter.append( c );
                escapeMode = false;
            } else {
                // escapeChar means: switch to escapeMode
                if ( c == escapeChar )
                    escapeMode = true;
                // wildCard must be converted to %
                else if ( c == wildCard )
                    parameter.append( '%' );
                // singleChar must be converted to ?
                else if ( c == singleChar )
                    parameter.append( '?' );
                // ' must be converted to \'
                else if ( c == '\'' )
                    parameter.append( "$'$" );
                // % must be converted to \'
                else if ( c == '%' )
                    parameter.append( "$%$" );
                // ? must be converted to \'
                // else if (c == '?') sb.append("$?$");
                // \ must (even in escapeMode) be converted to \\
                else if ( c == '\\' )
                    parameter.append( "\\\\" );
                else
                    parameter.append( c );
            }
        }
        if ( operation.isMatchCase() ) {
            query.append( parameter );
        } else {
            query.append( parameter.toString().toLowerCase() );
        }
        query.append( '\'' );
        // query.addArgument( parameter.toString() );
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param operation
     */
    protected void appendPropertyIsNullOperationAsSQL( StringBuffer query, PropertyIsNullOperation operation ) {
        appendPropertyNameAsSQL( query, operation.getPropertyName() );
        query.append( " IS NULL" );
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param operation
     */
    protected void appendPropertyIsBetweenOperationAsSQL( StringBuffer query, PropertyIsBetweenOperation operation ) {

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
     */
    protected void appendExpressionAsSQL( StringBuffer query, Expression expression, int targetSqlType ) {
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
    protected void appendLiteralAsSQL( StringBuffer query, Literal literal, int targetSqlType ) {
        switch ( targetSqlType ) {
        case java.sql.Types.DECIMAL:
        case java.sql.Types.DOUBLE:
        case java.sql.Types.FLOAT:
        case java.sql.Types.INTEGER:
        case java.sql.Types.NUMERIC:
        case java.sql.Types.REAL:
        case java.sql.Types.SMALLINT:
        case java.sql.Types.TINYINT:
            query.append( literal.getValue() );
            break;
        default:
            query.append( "'" + literal.getValue() + "'" );
            break;
        }
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param propertyName
     * @return the field mapped from the propertyname
     */
    protected MappingField getPropertyNameMapping( PropertyName propertyName ) {

        PropertyPath propertyPath = propertyName.getValue();
        LOG.logDebug( "Looking up '" + propertyPath + "' in the query table tree." );
        MappingField mappingField = null;
        PropertyNode propertyNode = this.queryTableTree.getPropertyNode( propertyPath );
        if ( propertyNode == null ) {
            String msg = "Internal error in WhereBuilder: no PropertyNode for path '" + propertyPath
                         + "' in QueryTableTree.";
            LOG.logError( msg );
            throw new RuntimeException( msg );
        } else if ( propertyNode instanceof SimplePropertyNode ) {
            SimpleContent content = ( (MappedSimplePropertyType) ( propertyNode.getProperty() ) ).getContent();
            if ( !( content instanceof MappingField ) ) {
                String msg = "Virtual properties are currently ignored in WhereBuilder#appendPropertyPathAsSQL(StatementBuffer,PropertyPath).";
                LOG.logError( msg );
                throw new RuntimeException( msg );
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
        return mappingField;
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param propertyName
     */
    protected void appendPropertyNameAsSQL( StringBuffer query, PropertyName propertyName ) {

        MappingField mappingField = getPropertyNameMapping( propertyName );
        // with ArcSDE because of versioning not applicable
        // query.append( mappingField.getTable() );
        // query.append( '.' );
        query.append( mappingField.getField() );
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param expression
     * @param targetSqlType
     */
    protected void appendArithmeticExpressionAsSQL( StringBuffer query, ArithmeticExpression expression,
                                                    int targetSqlType ) {
        query.append( '(' );
        appendExpressionAsSQL( query, expression.getFirstExpression(), targetSqlType );
        switch ( expression.getExpressionId() ) {
        case ExpressionDefines.ADD: {
            query.append( '+' );
            break;
        }
        case ExpressionDefines.SUB: {
            query.append( '-' );
            break;
        }
        case ExpressionDefines.MUL: {
            query.append( '*' );
            break;
        }
        case ExpressionDefines.DIV: {
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
     */
    protected void appendFunctionAsSQL( StringBuffer query, Function function, int targetSqlType ) {
        query.append( function.getName() );
        query.append( " (" );
        List<Expression> list = function.getArguments();
        for ( int i = 0; i < list.size(); i++ ) {
            Expression expression = list.get( i );
            appendExpressionAsSQL( query, expression, targetSqlType );
            if ( i != list.size() - 1 )
                query.append( ", " );
        }
        query.append( ")" );
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * @param query
     * @param operation
     */
    protected void appendLogicalOperationAsSQL( StringBuffer query, LogicalOperation operation ) {
        List<Operation> argumentList = operation.getArguments();
        switch ( operation.getOperatorId() ) {
        case OperationDefines.AND: {
            for ( int i = 0; i < argumentList.size(); i++ ) {
                Operation argument = argumentList.get( i );
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
                Operation argument = argumentList.get( i );
                query.append( '(' );
                appendOperationAsSQL( query, argument );
                query.append( ')' );
                if ( i != argumentList.size() - 1 )
                    query.append( " OR " );
            }
            break;
        }
        case OperationDefines.NOT: {
            Operation argument = argumentList.get( 0 );
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
     */
    protected void appendFeatureFilterAsSQL( StringBuffer query, FeatureFilter filter ) {
        ArrayList<FeatureId> list = filter.getFeatureIds();
        MappingField mapping = rootFeatureType.getGMLId().getIdFields()[0];
        String quote = "";
        switch ( mapping.getType() ) {
        case java.sql.Types.DECIMAL:
        case java.sql.Types.DOUBLE:
        case java.sql.Types.FLOAT:
        case java.sql.Types.INTEGER:
        case java.sql.Types.NUMERIC:
        case java.sql.Types.REAL:
        case java.sql.Types.SMALLINT:
        case java.sql.Types.TINYINT:
            break;
        default:
            quote = "'";
            break;
        }
        query.append( ' ' );
        query.append( mapping.getField() );
        try {
            for ( int i = 0; i < list.size(); i++ ) {
                if ( 0 == i )
                    query.append( " IN (" + quote );
                else
                    query.append( quote + "," + quote );
                String fid = list.get( i ).getValue();
                Object fidValue = org.deegree.io.datastore.FeatureId.removeFIDPrefix( fid, rootFeatureType.getGMLId() );
                query.append( fidValue.toString() );
            }
        } catch ( Exception e ) {
            LOG.logError( "Error converting feature id", e );
        }
        query.append( quote + ")" );
    }

    /**
     * Generates an SQL-fragment for the given object.
     * 
     * @param filter
     * @param layers
     * @return the complex filters as se filters.
     * 
     * @throws DatastoreException
     */
    protected SeFilter[] buildSpatialFilter( ComplexFilter filter, List<SeLayer> layers )
                            throws DatastoreException {

        SpatialOperation[] spatialOps = FilterTools.extractSpatialFilter( filter );
        if ( null == spatialOps || 0 == spatialOps.length )
            return null;

        SeFilter[] spatialFilter = new SeFilter[spatialOps.length];

        for ( int i = 0; i < spatialOps.length; i++ ) {
            try {
                MappingField mappingField = getPropertyNameMapping( spatialOps[i].getPropertyName() );
                String filterTable = mappingField.getTable();
                String filterColumn = mappingField.getField();

                SeCoordinateReference coordRef = null;
                String[] splitted = filterTable.toUpperCase().split( "\\." );
                String tmp = splitted[splitted.length - 1];
                for ( int k = 0; k < layers.size(); k++ ) {
                    SeLayer layer = layers.get( k );
                    splitted = layer.getName().toUpperCase().split( "\\." );
                    if ( splitted[splitted.length - 1].equals( tmp ) ) {
                        coordRef = layer.getCoordRef();
                        break;
                    }
                }
                if ( null == coordRef ) {
                    coordRef = new SeCoordinateReference();
                }

                int filterMethod = -1;
                boolean filterTruth = true;
                switch ( spatialOps[i].getOperatorId() ) {
                case OperationDefines.CROSSES: {
                    filterMethod = SeFilter.METHOD_LCROSS;
                    break;
                }
                case OperationDefines.EQUALS: {
                    filterMethod = SeFilter.METHOD_IDENTICAL;
                    break;
                }
                case OperationDefines.WITHIN: {
                    filterMethod = SeFilter.METHOD_SC_NO_ET;
                    break;
                }
                case OperationDefines.OVERLAPS: {
                    filterMethod = SeFilter.METHOD_ENVP;
                    break;
                }
                case OperationDefines.TOUCHES: {
                    filterMethod = SeFilter.METHOD_ET_OR_AI;
                    break;
                }
                case OperationDefines.DISJOINT: {
                    filterMethod = SeFilter.METHOD_SC_NO_ET;
                    filterTruth = false;
                    break;
                }
                case OperationDefines.INTERSECTS: {
                    filterMethod = SeFilter.METHOD_AI;
                    break;
                }
                case OperationDefines.CONTAINS: {
                    filterMethod = SeFilter.METHOD_AI_OR_ET;
                    break;
                }
                case OperationDefines.BBOX: {
                    filterMethod = SeFilter.METHOD_ENVP;
                    break;
                }
                case OperationDefines.DWITHIN:
                case OperationDefines.BEYOND:
                default: {
                    continue;
                }
                }
                SeShape filterGeom = SDEAdapter.export( spatialOps[i].getGeometry(), coordRef );
                spatialFilter[i] = new SeShapeFilter( filterTable, filterColumn, filterGeom, filterMethod, filterTruth );
            } catch ( Exception e ) {
                e.printStackTrace();
                throw new DatastoreException( "Error creating spatial filter", e );
            }
        }
        return spatialFilter;
    }
}