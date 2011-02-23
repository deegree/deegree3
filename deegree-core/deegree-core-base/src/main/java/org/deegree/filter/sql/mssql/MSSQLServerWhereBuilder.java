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
package org.deegree.filter.sql.mssql;

import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.filter.sql.AbstractWhereBuilder;
import org.deegree.filter.sql.UnmappableException;
import org.deegree.filter.sql.expression.SQLExpression;
import org.deegree.filter.sql.expression.SQLOperation;

/**
 * {@link AbstractWhereBuilder} implementation for Microsoft SQL Server databases.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MSSQLServerWhereBuilder extends AbstractWhereBuilder {

    private final MSSQLServerMapping mapping;

    /**
     * Creates a new {@link MSSQLServerWhereBuilder} instance.
     * 
     * @param mapping
     *            provides the mapping from {@link PropertyName}s to DB columns, must not be <code>null</code>
     * @param filter
     *            filter to use for generating the WHERE clause, can be <code>null</code>
     * @param sortCrit
     *            criteria to use for generating the ORDER BY clause, can be <code>null</code>
     * @throws FilterEvaluationException
     *             if the expression contains invalid {@link PropertyName}s
     */
    public MSSQLServerWhereBuilder( MSSQLServerMapping mapping, OperatorFilter filter, SortProperty[] sortCrit )
                            throws FilterEvaluationException {
        super( filter, sortCrit );
        this.mapping = mapping;
        build();
    }

    @Override
    protected SQLOperation toProtoSQL( PropertyIsLike op )
                            throws UnmappableException, FilterEvaluationException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    protected SQLOperation toProtoSQL( SpatialOperator op )
                            throws UnmappableException, FilterEvaluationException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    protected SQLExpression toProtoSQL( PropertyName propName )
                            throws UnmappableException, FilterEvaluationException {
        // TODO
        throw new UnsupportedOperationException();
    }
}