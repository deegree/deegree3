//$HeadURL$
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
package org.deegree.io.datastore.sql.transaction.delete;

import org.deegree.io.datastore.schema.TableRelation;
import org.deegree.io.datastore.schema.TableRelation.FK_INFO;
import org.deegree.io.datastore.schema.content.MappingField;

/**
 * Represents a reference from a table to another table. This corresponds to a {@link TableRelation}
 * declaration in an annotated GML schema, but with unambigious direction: the source (from) table
 * contains the foreign key.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
class TableReference {

    private String fromTable;

    private String toTable;

    private MappingField[] fkColumns;

    private MappingField[] keyColumns;

    /**
     * Creates a new <code>TableReference</code> instance from the given {@link TableRelation}.
     *
     * @param relation
     */
    TableReference( TableRelation relation ) {
        if ( relation.getFKInfo() == FK_INFO.fkIsFromField ) {
            this.fromTable = relation.getFromTable();
            this.fkColumns = relation.getFromFields();
            this.toTable = relation.getToTable();
            this.keyColumns = relation.getToFields();
        } else if ( relation.getFKInfo() == FK_INFO.fkIsToField ) {
            this.fromTable = relation.getToTable();
            this.fkColumns = relation.getToFields();
            this.toTable = relation.getFromTable();
            this.keyColumns = relation.getFromFields();
        } else {
            throw new IllegalArgumentException( "Cannot create TableReference without "
                                                + "fk (foreign key) information: " + relation );
        }
    }

    /**
     * Returns the columns that build the foreign key (in the 'from' table).
     *
     * @return the columns that build the foreign key (in the 'from' table)
     */
    MappingField[] getFkColumns() {
        return this.fkColumns;
    }

    /**
     * Returns the name of the 'from' table.
     *
     * @return the name of the 'from' table
     */
    String getFromTable() {
        return this.fromTable;
    }

    /**
     * Returns the columns that build the key (in the 'to' table).
     *
     * @return the columns that build the key (in the 'to' table)
     */
    MappingField[] getKeyColumns() {
        return this.keyColumns;
    }

    /**
     * Returns the name of the 'to' table.
     *
     * @return the name of the 'to' table
     */
    String getToTable() {
        return this.toTable;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( this.fromTable );
        sb.append( " [" );
        for ( int i = 0; i < this.fkColumns.length; i++ ) {
            sb.append( fkColumns[i].getField() );
            if ( i != this.fkColumns.length - 1 ) {
                sb.append( ',' );
            }
        }
        sb.append( "] -> " );
        sb.append( this.toTable );
        sb.append( " [" );
        for ( int i = 0; i < this.fkColumns.length; i++ ) {
            sb.append( keyColumns[i].getField() );
            if ( i != this.fkColumns.length - 1 ) {
                sb.append( ',' );
            }
        }
        sb.append( "]" );
        return sb.toString();
    }
}
