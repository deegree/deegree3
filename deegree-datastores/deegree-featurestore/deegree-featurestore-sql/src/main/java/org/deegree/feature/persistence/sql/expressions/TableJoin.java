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
package org.deegree.feature.persistence.sql.expressions;

import java.util.List;

import org.deegree.commons.jdbc.QTableName;
import org.deegree.filter.sql.MappingExpression;

/**
 * Defines a join between two tables with optional ordering.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TableJoin implements MappingExpression {

    private final QTableName fromTable;

    private final QTableName toTable;

    private final List<String> fromColumns;

    private final List<String> toColumns;

    private final List<String> orderColumns;

    private final boolean numberedOrder;

    public TableJoin( QTableName fromTable, QTableName toTable, List<String> fromColumns, List<String> toColumns,
                      List<String> orderColumns, boolean numberedOrder ) {
        this.fromTable = fromTable;
        this.toTable = toTable;
        this.fromColumns = fromColumns;
        this.toColumns = toColumns;
        this.orderColumns = orderColumns;
        this.numberedOrder = numberedOrder;
    }

    public QTableName getFromTable() {
        return fromTable;
    }

    public QTableName getToTable() {
        return toTable;
    }

    public List<String> getFromColumns() {
        return fromColumns;
    }

    public List<String> getToColumns() {
        return toColumns;
    }

    public List<String> getOrderColumns() {
        return orderColumns;
    }

    public boolean isNumberedOrder() {
        return numberedOrder;
    }
}