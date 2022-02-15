//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-base/src/main/java/org/deegree/sqldialect/SQLDialect.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.sqldialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.sql.PrimitiveParticleConverter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.utils.GeometryParticleConverter;
import org.deegree.sqldialect.filter.AbstractWhereBuilder;
import org.deegree.sqldialect.filter.PropertyNameMapper;
import org.deegree.sqldialect.filter.UnmappableException;

/**
 * Implementations provide the vendor-specific behavior for a spatial DBMS so it can be accessed by deegree.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31186 $, $Date: 2011-07-01 18:01:58 +0200 (Fr, 01. Jul 2011) $
 */
public interface SQLDialect {

    /**
     * Returns the maximum number of characters allowed for column names.
     * 
     * @return maximum number of characters
     */
    int getMaxColumnNameLength();

    /**
     * Returns the maximum number of characters allowed for table names.
     * 
     * @return maximum number of characters
     */
    int getMaxTableNameLength();

    /**
     * @return the default table schema to use for the database (eg. for requesting table metadata)
     */
    String getDefaultSchema();

    /**
     * @return the string concatenation operator
     */
    String stringPlus();

    /**
     * @param pattern
     * @param string
     * @return an expression that yields the string index
     */
    String stringIndex( String pattern, String string );

    /**
     * @param expr
     * @param type
     * @return expr cast to type
     */
    String cast( String expr, String type );

    /**
     * @param qTable
     * @param column
     * @param isGeographical
     * @return statement to determine the coordinate dimension, the srid and the geometry type of a given column (in
     *         this order)
     */
    String geometryMetadata( TableName qTable, String column, boolean isGeographical );

    /**
     * Returns an {@link AbstractWhereBuilder} instance for the given parameters.
     * 
     * @param mapper
     *            provides property name mappings, must not be <code>null<code>
     * @param filter
     *            filter to use for generating the WHERE clause, can be <code>null</code>
     * @param sortCrit
     *            criteria to use generating the ORDER BY clause, can be <code>null</code>
     * @param allowPartialMappings
     *            if <code>false</code>, any unmappable expression will cause an {@link UnmappableException} to be
     *            thrown
     * @return where builder, never <code>null</code>
     * @throws UnmappableException
     *             if allowPartialMappings is false and an expression could not be mapped to the db
     * @throws FilterEvaluationException
     */
    AbstractWhereBuilder getWhereBuilder( PropertyNameMapper mapper, OperatorFilter filter, SortProperty[] sortCrit,
                                          boolean allowPartialMappings )
                            throws UnmappableException, FilterEvaluationException;

    /**
     * Returns the SRID code for undefined.
     * 
     * @return SRID code, can be <code>null/code>
     */
    String getUndefinedSrid();

    /**
     * Returns an SQL snippet for SELECTing the aggregate bounding box of the given column.
     * 
     * @param colummn
     *            name of the column that stores the bounding box, never <code>null</code>
     * @return SQL snippet, never <code>null</code>
     */
    String getBBoxAggregateSnippet( String colummn );

    /**
     * Converts the value that has been SELECTed via {@link #getBBoxAggregateSnippet(String)} into an {@link Envelope}.
     * 
     * @param rs
     * @param colIdx
     * @param crs
     * @return aggregate envelope, can be <code>null</code>
     * @throws SQLException
     */
    Envelope getBBoxAggregateValue( ResultSet rs, int colIdx, ICRS crs )
                            throws SQLException;

    GeometryParticleConverter getGeometryConverter( String column, ICRS crs, String srid, boolean is2d );

    PrimitiveParticleConverter getPrimitiveConverter( String column, PrimitiveType pt );

    /**
     * Creates a new (spatially-enabled) database using the specified administrator connection.
     * 
     * @param adminConn
     *            administrator JDBC connection, must not be <code>null</code>
     * @param dbName
     *            name of the database to be created, must not be <code>null</code>
     * @throws SQLException
     */
    void createDB( Connection adminConn, String dbName )
                            throws SQLException;

    /**
     * Drops the specified database.
     * 
     * @param adminConn
     *            administrator JDBC connection, must not be <code>null</code>
     * @param dbName
     *            name of the database to be created, must not be <code>null</code>
     * @throws SQLException
     */
    void dropDB( Connection adminConn, String dbName )
                            throws SQLException;

    void createAutoColumn( StringBuffer currentStmt, List<StringBuffer> additionalSmts, SQLIdentifier column,
                                  SQLIdentifier table );

    ResultSet getTableColumnMetadata( DatabaseMetaData md, TableName table )
                            throws SQLException;

    /**
     * Returns whether a transaction context is required for cursor mode to work.
     * 
     * @return <code>true</code>, if a transaction context is required, <code>false</code> otherwise
     */
    boolean requiresTransactionForCursorMode();

    /**
     * Returns a <code>SELECT</code> statement for retrieving the next value in the specified DB sequence.
     * 
     * @param sequence
     *            name of the database sequence, must not be <code>null</code>
     * @return SQL <code>SELECT</code> statement, never <code>null</code>
     */
    String getSelectSequenceNextVal( String sequence );

    /**
     * Returns the leading escape char for the SQLDialect
     *
     * @return leading escape char
     */
    char getLeadingEscapeChar();

    /**
     * Returns the tailing escape char for the SQLDialect
     *
     * @return tailing escape char
     */
    char getTailingEscapeChar();

    /**
     * Returns whether a query can use row limiting syntax 
     */
    boolean isRowLimitingCapable();
}
