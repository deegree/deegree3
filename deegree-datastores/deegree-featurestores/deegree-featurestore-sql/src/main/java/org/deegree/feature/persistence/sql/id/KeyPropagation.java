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
package org.deegree.feature.persistence.sql.id;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;

/**
 * Defines the propagation of a key column in a source table to a foreign key column in a target table.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
public class KeyPropagation {

    private final TableName source;

    private final TableName target;

    private final SQLIdentifier pkColumn;

    private final SQLIdentifier fkColumn;

    private final boolean cascadeOnDelete;

    /**
     * Creates a new {@link KeyPropagation} instance.
     * 
     * @param source
     *            source table, must not be <code>null</code>
     * @param pkColumn
     *            primary key column (in source table), must not be <code>null</code>
     * @param target
     *            target table, must not be <code>null</code>
     * @param fkColumn
     *            foreign key column (in target table), must not be <code>null</code>
     */
    public KeyPropagation( TableName source, SQLIdentifier pkColumn, TableName target, SQLIdentifier fkColumn ) {
        this.source = source;
        this.pkColumn = pkColumn;
        this.target = target;
        this.fkColumn = fkColumn;
        this.cascadeOnDelete = true;
    }

    public TableName getPKTable() {
        return source;
    }

    public SQLIdentifier getPKColumn() {
        return pkColumn;
    }

    public TableName getFKTable() {
        return target;
    }

    public SQLIdentifier getFKColumn() {
        return fkColumn;
    }

    /**
     * Returns whether the foreign relation is constrained by the DB, i.e. if creating a state where the fk column
     * contains a value without a match in the pk column will fail.
     * 
     * @return <code>true</code>, if the foreign key relation is constrained, otherwise <code>false</code>
     */
    public boolean isFKConstrained() {
        throw new UnsupportedOperationException( "Not implemented yet." );
    }

    /**
     * Returns whether corresponding rows in the target table are automatically deleted by the database if a row is
     * deleted in the source table.
     * 
     * @return <code>true</code>, if corresponding rows are deleted automatically, <code>false</code> otherwise
     */
    public boolean getCascadeOnDelete() {
        throw new UnsupportedOperationException( "Not implemented yet." );
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( fkColumn == null ) ? 0 : fkColumn.hashCode() );
        result = prime * result + ( ( pkColumn == null ) ? 0 : pkColumn.hashCode() );
        result = prime * result + ( ( source == null ) ? 0 : source.hashCode() );
        result = prime * result + ( ( target == null ) ? 0 : target.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( !( obj instanceof KeyPropagation ) ) {
            return false;
        }
        KeyPropagation that = (KeyPropagation) obj;
        return this.source.equals( that.source ) && this.pkColumn.equals( that.pkColumn )
               && this.target.equals( that.target ) && this.fkColumn.equals( that.fkColumn );
    }

    @Override
    public String toString() {
        return source + "." + pkColumn + " -> " + target + "." + fkColumn;
    }
}
