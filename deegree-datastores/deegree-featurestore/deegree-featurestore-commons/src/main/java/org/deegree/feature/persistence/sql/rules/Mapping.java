//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.feature.persistence.sql.rules;

import org.deegree.feature.persistence.sql.expressions.JoinChain;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.DBField;

/**
 * A {@link Mapping} identifies a relative XPath-expression in the feature type model with a mapping / join rule in the
 * relational model.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class Mapping {

    private final PropertyName path;

    private final JoinChain joinedTable;

    private final DBField nilMapping;

    /**
     * Creates a new {@link Mapping} instance.
     * 
     * @param path
     *            relative xpath expression, must not be <code>null</code>
     * @param joinRule
     *            the table joins, can be <code>null</code> (no joins involved)
     * @param nilMapping
     *            name of (boolean) column that stores whether the element is nilled, can be <code>null</code>
     */
    protected Mapping( PropertyName path, JoinChain joinRule, DBField nilMapping ) {
        this.path = path;
        this.joinedTable = joinRule;
        this.nilMapping = nilMapping;
    }

    /**
     * Returns a relative XPath-expression that describes the path from the parent particle to the particle(s) that are
     * affected by this rule.
     * 
     * @return a relative xpath expression, never <code>null</code>
     */
    public PropertyName getPath() {
        return path;
    }

    /**
     * Returns the table joins in the relational model.
     * 
     * @return the table joins, can be <code>null</code> (no joins involved)
     */
    public JoinChain getJoinedTable() {
        return joinedTable;
    }

    /**
     * Returns the name of the boolean column that is used for storing if the element is nilled.
     * 
     * @return name of the boolean column, can be <code>null</code>
     */
    public DBField getNilMapping() {
        return nilMapping;
    }

    @Override
    public String toString() {
        return "{path=" + path + ",joinChain=" + joinedTable + "}";
    }
}