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

import java.util.List;

import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.persistence.sql.id.FIDMapping;

public class IdAssignment {

    private final String oldId;

    private final DelayedInsertRow insertRow;

    private String newId;

    private FIDMapping fidMapping;

    IdAssignment( String oldId, DelayedInsertRow insertRow ) {
        this.oldId = oldId;
        this.insertRow = insertRow;
    }
    
    void setFidMapping (FIDMapping fidMapping) {
        this.fidMapping = fidMapping;
    }

    public String getOldId() {
        return oldId;
    }

    public String getNewId() {
        if ( !isAssigned() ) {
            throw new RuntimeException( "Id has not been reassigned yet." );
        }
        return newId;
    }

    public boolean isAssigned() {
        return fidMapping != null || newId != null;
    }

    void assign( FIDMapping fidMapping ) {               
        newId = fidMapping.getPrefix();
        List<Pair<String, BaseType>> fidColumns = fidMapping.getColumns();
        newId += insertRow.get( fidColumns.get( 0 ).first );
        for ( int i = 1; i < fidColumns.size(); i++ ) {
            newId += fidMapping.getDelimiter() + insertRow.get( fidColumns.get( i ).first );
        }
    }

    public DelayedInsertRow getInsertRow() {
        return insertRow;
    }
}
