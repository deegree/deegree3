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
package org.deegree.io.datastore.sql.generic;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.io.JDBCConnection;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.content.MappingField;
import org.deegree.io.datastore.sql.StatementBuffer;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.datastore.sql.VirtualContentProvider;
import org.deegree.io.datastore.sql.wherebuilder.SpecialCharString;
import org.deegree.io.datastore.sql.wherebuilder.WhereBuilder;
import org.deegree.io.quadtree.DBQuadtree;
import org.deegree.io.quadtree.DBQuadtreeManager;
import org.deegree.io.quadtree.IndexException;
import org.deegree.io.quadtree.Quadtree;
import org.deegree.model.filterencoding.DBFunction;
import org.deegree.model.filterencoding.Expression;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterEvaluationException;
import org.deegree.model.filterencoding.Function;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyIsLikeOperation;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcbase.SortProperty;

/**
 * {@link WhereBuilder} implementation for the {@link GenericSQLDatastore}.
 * <p>
 * Uses the {@link Quadtree} to speed up BBOX queries.
 *
 * @see org.deegree.io.quadtree
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
class GenericSQLWhereBuilder extends WhereBuilder {

    private static final ILogger LOG = LoggerFactory.getLogger( GenericSQLWhereBuilder.class );

    private final static String SQL_TRUE = "1=1";

    private final static String SQL_FALSE = "1!=1";

    private JDBCConnection jdbc;

    /**
     * Creates a new instance of <code>GenericSQLWhereBuilder</code> from the given parameters.
     * 
     * @param rootFts
     *            selected feature types, more than one type means that the types are joined
     * @param aliases
     *            aliases for the feature types, may be null (must have same length as rootFts otherwise)
     * @param filter
     *            filter that restricts the matched features
     * @param sortProperties
     *            sort criteria for the result, may be null or empty
     * @param aliasGenerator
     *            used to generate unique table aliases
     * @param vcProvider
     * @param jdbc
     * @throws DatastoreException
     */
    public GenericSQLWhereBuilder( MappedFeatureType[] rootFts, String[] aliases, Filter filter,
                                   SortProperty[] sortProperties, TableAliasGenerator aliasGenerator,
                                   VirtualContentProvider vcProvider, JDBCConnection jdbc ) throws DatastoreException {
        super( rootFts, aliases, filter, sortProperties, aliasGenerator, vcProvider );
        this.jdbc = jdbc;
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     *
     * NOTE: Currently, the method uses a quirk and appends the generated argument inline, i.e. not using
     * query.addArgument(). Works around an SQL error on HSQLDB that would occur otherwise.
     *
     * @param query
     * @param operation
     * @throws FilterEvaluationException
     */
    @Override
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

        query.append( " LIKE '" );
        query.append( sqlEncoded );
        query.append( "'" );
    }    
        
    
    /**
     * Generates an SQL-fragment for the given object.
     * 
     * TODO: Implement BBOX faster using explicit B0X-constructor
     * 
     * @throws DatastoreException
     */
    @Override
    protected void appendSpatialOperationAsSQL( StatementBuffer query, SpatialOperation operation )
                            throws DatastoreException {

        switch ( operation.getOperatorId() ) {
        case OperationDefines.BBOX: {
            appendBBOXOperationAsSQL( query, operation );
            break;
        }
        case OperationDefines.DISJOINT:
        case OperationDefines.CROSSES:
        case OperationDefines.EQUALS:
        case OperationDefines.WITHIN:
        case OperationDefines.OVERLAPS:
        case OperationDefines.TOUCHES:
        case OperationDefines.CONTAINS:
        case OperationDefines.INTERSECTS:
        case OperationDefines.DWITHIN:
        case OperationDefines.BEYOND: {
            query.append( SQL_TRUE );
            break;
        }
        default: {
            String msg = Messages.getMessage( "DATASTORE_UNKNOWN_SPATIAL_OPERATOR",
                                              OperationDefines.getNameById( operation.getOperatorId() ) );
            throw new DatastoreException( msg );
        }
        }
    }

    /**
     * Appends a constraint (FEATURE_ID IN (...)) to the given {@link StatementBuffer} which is generated by using the
     * associated {@link DBQuadtree} index.
     * 
     * @param query
     * @param operation
     * @throws DatastoreException
     */
    private void appendBBOXOperationAsSQL( StatementBuffer query, SpatialOperation operation )
                            throws DatastoreException {

        Envelope env = operation.getGeometry().getEnvelope();

        DBQuadtreeManager<?> qtm = null;
        try {
            qtm = new DBQuadtreeManager<Object>( jdbc, this.rootFts[0].getTable(), "geometry", null, Integer.MIN_VALUE );
            Object type = qtm.determineQuattreeType();
            int dataType = Types.VARCHAR;
            if ( type instanceof Integer ) {
                LOG.logDebug( "The elements of the quadtree are of type Integer." );
                qtm = new DBQuadtreeManager<Integer>( jdbc, this.rootFts[0].getTable(), "geometry", null, Types.INTEGER );
                dataType = Types.INTEGER;
            } else if ( type instanceof String ) {
                LOG.logDebug( "The elements of the quadtree are of type String." );
                qtm = new DBQuadtreeManager<String>( jdbc, this.rootFts[0].getTable(), "geometry", null, Types.INTEGER );
            }

            Envelope qtEnv = qtm.getQuadtree().getRootBoundingBox();
            if ( qtEnv.intersects( env ) ) {
                // check if features within this bbox are available
                // if not -> return an empty list
                List<?> ids = qtm.getQuadtree().query( env );
                if ( ids.size() > 0 ) {

                    MappingField[] idFields = this.rootFts[0].getGMLId().getIdFields();
                    if ( idFields.length > 1 ) {
                        String msg = "GenericSQLDatastore cannot handle composite feature ids.";
                        throw new DatastoreException( msg );
                    }

                    query.append( getRootTableAlias( 0 ) + '.' + idFields[0].getField() + " IN (" );
                    for ( int i = 0; i < ids.size() - 1; i++ ) {
                        query.append( "?," );
                        if ( dataType == Types.VARCHAR ) {
                            query.addArgument( ( "" + ids.get( i ) ).trim(), Types.VARCHAR );
                        } else {
                            query.addArgument( ids.get( i ), Types.INTEGER );
                        }
                    }
                    if ( dataType == Types.VARCHAR ) {
                        query.addArgument( ( "" + ids.get( ids.size() - 1 ) ).trim(), Types.VARCHAR );
                    } else {
                        query.addArgument( ids.get( ids.size() - 1 ), Types.INTEGER );
                    }
                    query.append( "?)" );
                } else {
                    query.append( SQL_FALSE );
                }
            } else {
                query.append( SQL_FALSE );
            }
        } catch ( IndexException e ) {
            LOG.logError( e.getMessage(), e );
            throw new DatastoreException(
                                          "Could not append bbox operation as sql into the Quadtree: " + e.getMessage(),
                                          e );
        } finally {
            if ( qtm != null ) {
                qtm.release();
            }
        }
    }
}
