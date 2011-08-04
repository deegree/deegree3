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
package org.deegree.feature.persistence.sql.insert;

import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.jdbc.InsertRow;
import org.deegree.commons.jdbc.QTableName;

/**
 * An {@link InsertRow} that can not be inserted until the values for the foreign keys are known.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class DelayedInsertRow extends InsertRow {

    private Map<DelayedInsertRow, InsertRowReference> parentToRef = new HashMap<DelayedInsertRow, InsertRowReference>();

    DelayedInsertRow( QTableName table, String autoGenColumn ) {
        super( table, autoGenColumn );
    }

    void setTable( QTableName table ) {
        this.table = table;
    }

    void setAutoGenColumn( String autoGenColumn ) {
        this.autogenColumn = autoGenColumn;
    }

    void addParent( InsertRowReference ref ) {
        parentToRef.put( ref.getRef(), ref );
    }

    public void removeParent( DelayedInsertRow parent ) {
        
        InsertRowReference row = parentToRef.get( parent );
        
        // propagate keys
        for ( int i = 0; i < row.getJoin().getFromColumns().size(); i++ ) {
            String fromColumn = row.getJoin().getFromColumns().get( i );
            String toColumn = row.getJoin().getToColumns().get( i );
            Object key = parent.get( fromColumn );
            if ( key == null ) {
                throw new IllegalArgumentException( "Unable to resolve foreign key relation. Encountered NULL value." );
            }
            addPreparedArgument( toColumn, key );
        }
        parentToRef.remove( parent );
    }

    boolean canInsert() {
        return parentToRef.isEmpty();
    }
}
