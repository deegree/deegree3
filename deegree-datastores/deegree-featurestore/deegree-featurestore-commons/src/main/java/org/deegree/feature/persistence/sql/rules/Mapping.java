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

import java.util.List;

import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.filter.expression.PropertyName;

/**
 * A {@link Mapping} describes how a particle of a feature type is mapped to a relational model (tables/columns).
 * <p>
 * The mapping is defined by identifying a relative XPath-expression in the feature model with a column mapping/join
 * rule in the relational model.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class Mapping {

    private final PropertyName path;

    private final List<TableJoin> tableChange;

    /**
     * Creates a new {@link Mapping} instance.
     * 
     * @param path
     *            relative xpath expression, must not be <code>null</code>
     * @param tableChange
     *            table joins, can be <code>null</code> (no joins involved)
     */
    protected Mapping( PropertyName path, List<TableJoin> tableChange ) {
        this.path = path;
        this.tableChange = tableChange;
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
     * Returns the table joins that have to be performed in the relational model to follow the particle path.
     * 
     * @return the table joins, can be <code>null</code> (no joins involved)
     */
    public List<TableJoin> getJoinedTable() {
        return tableChange;
    }

    @Override
    public String toString() {
        return "{path=" + path + ",joinChain=" + tableChange + "}";
    }
}