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
package org.deegree.filter.sql.postgis;

import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.PropertyNameMapping;
import org.deegree.filter.sql.TableAliasManager;
import org.deegree.filter.sql.UnmappableException;
import org.deegree.geometry.Geometry;

/**
 * Implementations provide {@link PropertyName} to table/column mappings for the {@link PostGISWhereBuilder}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface PostGISMapping {

    /**
     * Returns the {@link PropertyNameMapping} for the given {@link PropertyName}.
     * 
     * @param propName
     *            property name, can be <code>null</code> (indicates that the default geometry property of the root
     *            object is requested)
     * @param aliasManager
     *            manager for creating and tracking table aliases, never <code>null</code>
     * @return relational mapping, may be <code>null</code> (if no mapping is possible)
     * @throws FilterEvaluationException
     *             thrown to indicate that the {@link PropertyName} is invalid
     * @throws UnmappableException
     */
    public PropertyNameMapping getMapping( PropertyName propName, TableAliasManager aliasManager )
                            throws FilterEvaluationException, UnmappableException;

    /**
     * Returns the value object for the given {@link Literal}, adapted to the SQL type that is imposed by the relational
     * mapping of the specified {@link PropertyName}.
     * 
     * @param literal
     *            literal to be converted, never <code>null</code>
     * @param propName
     *            property name, never <code>null</code>
     * @return value object for the column in the database, may be <code>null</code>
     * @throws FilterEvaluationException
     */
    public Object getPostGISValue( Literal<?> literal, PropertyName propName )
                            throws FilterEvaluationException;

    /**
     * Returns the WKB for the given {@link Geometry} in the CRS that is imposed by the relational mapping of the
     * specified {@link PropertyName}.
     * 
     * @param literal
     *            literal to be converted, never <code>null</code>
     * @param propName
     *            property name, can be <code>null</code> (indicates that the default geometry property of the root
     *            object is requested)
     * @return value object for the column in the database, may be <code>null</code>
     * @throws FilterEvaluationException
     */
    public byte[] getPostGISValue( Geometry literal, PropertyName propName )
                            throws FilterEvaluationException;
}
