//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.jdbc.QTableName;
import org.deegree.commons.utils.PostRelation;
import org.deegree.commons.utils.SortUtils;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of database rows to be inserted (and their dependencies) during a {@link FeatureStoreTransaction}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class InsertRowManager {

    private static Logger LOG = LoggerFactory.getLogger( InsertRowManager.class );

    private final List<InsertRow> rows = new ArrayList<InsertRow>();

    private final Map<InsertRow, List<ForeignKey>> rowToPost = new HashMap<InsertRow, List<ForeignKey>>();

    private final Map<InsertRow, List<ForeignKey>> rowToPre = new HashMap<InsertRow, List<ForeignKey>>();

    public InsertRow newRow( QTableName table, String autoGenColumn ) {
        InsertRow row = new InsertRow( table, autoGenColumn );
        rows.add( row );
        return row;
    }

    public void addForeignKeyRelation( InsertRow primary, String primaryColumn, InsertRow foreign, String foreignColumn ) {
        ForeignKey fk = new ForeignKey( primary, primaryColumn, foreign, foreignColumn );
        addSafely( primary, fk, rowToPost );
        addSafely( foreign, fk, rowToPre );
    }

    private void addSafely( InsertRow row, ForeignKey fk, Map<InsertRow, List<ForeignKey>> map ) {
        List<ForeignKey> list = map.get( row );
        if ( list == null ) {
            list = new ArrayList<ForeignKey>();
            map.put( row, list );
        }
        list.add( fk );
    }

    public void performInserts( Connection conn )
                            throws SQLException {

        SortUtils.sortTopologically( rows, new PostRelation<InsertRow>() {
            @Override
            public List<InsertRow> getPost( InsertRow vertex ) {
                List<ForeignKey> fks = rowToPost.get( vertex );
                if ( fks == null ) {
                    return Collections.emptyList();
                }
                List<InsertRow> post = new ArrayList<InsertRow>( fks.size() );
                for ( ForeignKey fk : fks ) {
                    post.add( fk.getPost() );
                }
                return post;
            }
        } );

        for ( InsertRow row : rows ) {
            List<ForeignKey> pre = rowToPre.get( row );
            if ( pre != null ) {
                for ( ForeignKey keyPropagation : pre ) {
                    InsertRow fromRow = keyPropagation.getPre();
                    String fromColumn = keyPropagation.getPreColumn();
                    String toColumn = keyPropagation.getPostColumn();
                    Object key = fromRow.get( fromColumn );
                    LOG.warn( "Propagating: " + fromRow.getTable() + "." + fromColumn + " -> " + row.getTable() + "."
                              + toColumn );
                    row.addPreparedArgument( toColumn, key );
                }
            }
            LOG.warn( "Inserting: " + row);
            row.performInsert( conn );
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for ( InsertRow row : rows ) {
            sb.append( row );
            sb.append( "\n" );
        }
        return sb.toString();
    }
}