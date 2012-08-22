//$HeadURL$
//$CVSHeader: deegree/src/org/deegree/io/datastore/sql/transaction/FeatureRow.java,v 1.13 2006/09/26 16:45:44 mschneider Exp $
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
package org.deegree.io.datastore.sql.transaction.insert;

import java.util.Iterator;

import org.deegree.ogcwebservices.wfs.operation.transaction.Insert;

/**
 * Represents a feature table row (columns + values) which has to be inserted as part of a
 * {@link Insert} operation.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class FeatureRow extends InsertRow {

    /**
     * Creates a new <code>FeatureRow</code> instance for the given feature table.
     *
     * @param table
     */
    public FeatureRow( String table ) {
        super( table );
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer( "FeatureRow, table: '" );
        sb.append( this.table );
        sb.append( "'" );
        Iterator<String> columnIter = this.columnMap.keySet().iterator();
        while ( columnIter.hasNext() ) {
            sb.append( ", " );
            String column = columnIter.next();
            InsertField field = this.columnMap.get( column );
            sb.append( field );
        }
        return sb.toString();
    }
}
