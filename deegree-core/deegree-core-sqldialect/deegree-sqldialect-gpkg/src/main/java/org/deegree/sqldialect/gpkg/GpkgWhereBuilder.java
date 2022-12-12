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
package org.deegree.sqldialect.gpkg;

import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.sqldialect.SortCriterion;
import org.deegree.sqldialect.filter.AbstractWhereBuilder;
import org.deegree.sqldialect.filter.PropertyNameMapper;
import org.deegree.sqldialect.filter.UnmappableException;
import org.deegree.sqldialect.filter.expression.SQLExpression;
import org.deegree.sqldialect.filter.expression.SQLOperation;
import org.deegree.sqldialect.filter.expression.SQLOperationBuilder;

import java.util.List;

/**
 * {@link AbstractWhereBuilder} implementation for GeoPackage databases.
 *
 * @author <a href="mailto:migliavacca@lat-lon.de">Diego Migliavacca</a>
 * @since 3.5
 */
public class GpkgWhereBuilder extends AbstractWhereBuilder {

    /**
     * Creates a new {@link GpkgWhereBuilder} instance.
     *
     * @param dialect
     *                 SQL dialect
     * @param mapper
     *                 provides the mapping from {@link ValueReference}s to DB columns, must not be <code>null</code>
     * @param filter
     *                 Filter to use for generating the WHERE clause, can be <code>null</code>
     * @param sortCrit
     *                 criteria to use for generating the ORDER BY clause, can be <code>null</code>
     * @param defaultSortCriteria
     * @param allowPartialMappings
     *                 if false, any unmappable expression will cause an {@link UnmappableException} to be thrown
     * @throws FilterEvaluationException
     *                 if the expression contains invalid {@link ValueReference}s
     * @throws UnmappableException
     *                 if allowPartialMappings is false and an expression could not be mapped to the db
     */
    public GpkgWhereBuilder( GpkgDialect dialect, PropertyNameMapper mapper, OperatorFilter filter,
                             SortProperty[] sortCrit, List<SortCriterion> defaultSortCriteria, boolean allowPartialMappings )
                            throws FilterEvaluationException,
                            UnmappableException {
        super( dialect, mapper, filter, sortCrit, defaultSortCriteria );
        build( allowPartialMappings );
    }

    /**
     * Translates the given {@link PropertyIsLike} into an {@link SQLOperation}
     *
     * @param op comparison operator to be translated, must not be <code>null</code>
     * @return corresponding SQL expression, never <code>null</code>
     * @throws UnmappableException       if translation is not possible (usually due to unmappable property names)
     * @throws FilterEvaluationException if the expression contains invalid {@link ValueReference}s
     */
    @Override
    protected SQLOperation toProtoSQL( PropertyIsLike op )
                            throws UnmappableException, FilterEvaluationException {
        return null;
    }

    @Override
    protected SQLOperation toProtoSQL( SpatialOperator op )
                            throws UnmappableException, FilterEvaluationException {
        return null;
    }

    @Override
    protected void addExpression( SQLOperationBuilder builder, SQLExpression expr, Boolean matchCase ) {
    }
}
