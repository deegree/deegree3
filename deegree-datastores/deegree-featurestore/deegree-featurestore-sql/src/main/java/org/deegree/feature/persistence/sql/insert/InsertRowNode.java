//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/src/main/java/org/deegree/feature/persistence/postgis/PostGISFeatureStoreTransaction.java $
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
package org.deegree.feature.persistence.sql.insert;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deegree.commons.jdbc.InsertRow;
import org.deegree.commons.jdbc.QTableName;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.filter.sql.DBField;

public class InsertRowNode {

    private final InsertRow row;

    private final TableJoin parentRelation;

    private final List<InsertRowNode> relatedRows = new ArrayList<InsertRowNode>();

    public InsertRowNode( QTableName table, TableJoin parentRelation, String autogenColumn ) {
        this.row = new InsertRow( table, autogenColumn );
        this.parentRelation = parentRelation;
    }
    
    /**
     * @return the row
     */
    public InsertRow getRow() {
        return row;
    }

    /**
     * @return the parentRelation
     */
    public TableJoin getParentRelation() {
        return parentRelation;
    }

    /**
     * @return the relatedRows
     */
    public List<InsertRowNode> getRelatedRows() {
        return relatedRows;
    }

    public Map<String, Object> performInsert( Connection conn )
                            throws SQLException {

        Map<String, Object> keys = row.performInsert( conn );

        for ( InsertRowNode relatedRow : relatedRows ) {
            // propagate value of foreign key
            // TODO multi column joins
            DBField from = new DBField( relatedRow.parentRelation.getFromTable().getTable(),
                                        relatedRow.parentRelation.getFromColumns().get( 0 ) );
            Object fkValue = row.get( from.getColumn() );
            if ( fkValue == null ) {
                fkValue = keys.get( from.getColumn() );
            }
            if ( fkValue != null ) {
                // TODO multi column joins                
                DBField to = new DBField( relatedRow.parentRelation.getToTable().getTable(),
                                          relatedRow.parentRelation.getToColumns().get( 0 ) );
                relatedRow.getRow().addPreparedArgument( to.getColumn(), fkValue );
            }
            relatedRow.performInsert( conn );
        }
        return keys;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( row );
        sb.append( ";" );
        for ( InsertRowNode child : relatedRows ) {
            sb.append( "\n" );
            sb.append( child.getRow() );
            sb.append( ";" );
        }
        return sb.toString();
    }
}