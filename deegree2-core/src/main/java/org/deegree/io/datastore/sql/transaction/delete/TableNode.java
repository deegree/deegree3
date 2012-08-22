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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.schema.content.MappingField;
import org.deegree.ogcwebservices.wfs.operation.transaction.Delete;

/**
 * Represents a table row that has to be deleted as part of a {@link Delete} operation.
 * <p>
 * Connected table entries (rows that refer to this row or that are referenced by this row) are also
 * stored.
 *
 * @see TableGraph
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class TableNode {

    private String table;

    private Map<String, KeyColumn> namesToColumns = new HashMap<String, KeyColumn>();

    private Set<TableNode> preNodes = new HashSet<TableNode>();

    private Set<TableNode> postNodes = new HashSet<TableNode>();

    private boolean deleteVetoPossible;

    /**
     * Creates a new <code>DeleteNode</code> instance.
     *
     * @param table
     * @param keyColumns
     */
    TableNode( String table, Collection<KeyColumn> keyColumns ) {
        this.table = table;
        for ( KeyColumn column : keyColumns ) {
            this.namesToColumns.put( column.getName(), column );
        }
    }

    /**
     * Creates a new <code>DeleteNode</code> instance for a feature instance.
     *
     * @param fid
     */
    TableNode( FeatureId fid ) {
        MappingField[] fidFields = fid.getFidDefinition().getIdFields();
        for ( int i = 0; i < fidFields.length; i++ ) {
            KeyColumn column = new KeyColumn( fidFields[i].getField(), fidFields[i].getType(), fid.getValue( i ) );
            this.namesToColumns.put( column.getName(), column );
        }
        this.table = fid.getFeatureType().getTable();
    }

    boolean isDeleteVetoPossible() {
        return this.deleteVetoPossible;
    }

    void setDeleteVetoPossible() {
        this.deleteVetoPossible = true;
    }

    /**
     * Returns the table name.
     *
     * @return the table name
     */
    String getTable() {
        return this.table;
    }

    /**
     * Returns the key columns that identify the table row.
     *
     * @return the key columns that identify the table row
     */
    Collection<KeyColumn> getKeyColumns() {
        return this.namesToColumns.values();
    }

    /**
     * Returns the value for the given key column name.
     *
     * @param columnName
     * @return the value for the given key column name
     */
    KeyColumn getKeyColumnValue( String columnName ) {
        return this.namesToColumns.get( columnName );
    }

    /**
     * Returns the set of post nodes, i.e. table rows that <i>are referenced</i> by this row.
     *
     * @return the set of post nodes
     */
    Collection<TableNode> getPostNodes() {
        return this.postNodes;
    }

    /**
     * Returns the set of pre nodes, i.e. table rows that <i>refer</i> to this row.
     *
     * @return the set of pre nodes
     */
    Collection<TableNode> getPreNodes() {
        return this.preNodes;
    }

    /**
     * Connects this node to the given target node.
     * <p>
     * NOTE: The target node is the one that stores the primary key.
     *
     * @param targetNode
     */
    void connect( TableNode targetNode ) {
        if ( !this.postNodes.contains( targetNode ) ) {
            this.postNodes.add( targetNode );
            targetNode.preNodes.add( this );
        }
    }

    @Override
    public int hashCode() {
        return this.table.hashCode();
    }

    @Override
    public boolean equals( Object obj ) {
        if ( obj == null || !( obj instanceof TableNode ) ) {
            return false;
        }
        TableNode that = (TableNode) obj;
        if ( !this.table.equals( that.table ) ) {
            return false;
        }
        if ( this.namesToColumns.size() != that.namesToColumns.size() ) {
            return false;
        }
        for ( String columnName : this.namesToColumns.keySet() ) {
            KeyColumn thisKeyValue = this.namesToColumns.get( columnName );
            KeyColumn thatKeyValue = that.namesToColumns.get( columnName );
            if ( !thisKeyValue.equals( thatKeyValue ) ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( this.table );
        for ( KeyColumn column : this.namesToColumns.values() ) {
            sb.append( "," );
            sb.append( column );
        }
        return sb.toString();
    }

    String toString( String indent, Set<TableNode> printedNodes ) {
        StringBuffer sb = new StringBuffer();
        sb.append( indent );
        sb.append( "- table: " );
        sb.append( this.table );
        for ( KeyColumn column : this.namesToColumns.values() ) {
            sb.append( ", " );
            sb.append( column );
        }
        sb.append( '\n' );

        for ( TableNode postNode : this.postNodes ) {
            sb.append( indent );
            if ( printedNodes.contains( postNode ) ) {
                sb.append( indent + " " );
                sb.append( "- table: " );
                sb.append( postNode.getTable() );
                sb.append( " (DUP)\n" );
            } else {
                printedNodes.add( postNode );
                sb.append( postNode.toString( indent + " ", printedNodes ) );
            }
        }
        return sb.toString();
    }

}

/**
 * Encapsulates a column name, it's type code and a value.
 */
class KeyColumn {

    private String name;

    private int typeCode;

    private Object value;

    KeyColumn( String name, int typeCode, Object value ) {
        this.name = name;
        this.typeCode = typeCode;
        this.value = value;
    }

    String getName() {
        return this.name;
    }

    int getTypeCode() {
        return this.typeCode;
    }

    Object getValue() {
        return this.value;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( obj == null || !( obj instanceof KeyColumn ) ) {
            return false;
        }
        KeyColumn that = (KeyColumn) obj;
        if ( !this.name.equals( that.name ) ) {
            return false;
        }
        return this.value.equals( that.value );
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( this.name );
        sb.append( '=' );
        sb.append( this.value );
        return sb.toString();
    }
}
