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
package org.deegree.io.datastore.schema.content;

/**
 * Encapsulates a field of the backend (e.g. an SQL table column).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MappingField implements SimpleContent {

    private String table;

    private String field;

    private int type;

    private boolean auto;

    /**
     * Creates a new instance of <code>MappingField</code> from the given parameters.
     *
     * @param table
     * @param field
     * @param type
     *            type code
     * @param auto
     *
     * @see java.sql.Types
     */
    public MappingField( String table, String field, int type, boolean auto ) {
        this.table = table;
        this.field = field;
        this.type = type;
        this.auto = auto;
    }

    /**
     * Creates a new instance of <code>MappingField</code> from the given parameters with no automatic generation of
     * values.
     *
     * @param table
     * @param field
     * @param type
     *
     * @see java.sql.Types
     */
    public MappingField( String table, String field, int type ) {
        this.table = table;
        this.field = field;
        this.type = type;
    }

    /**
     * Returns true, because a db field may be updated.
     *
     * @return true, because a db field may be updated
     */
    public boolean isUpdateable() {
        return true;
    }

    /**
     * Returns true, because a db field is (in general) suitable as a sort criterion.
     *
     * @return true, because a db field is (in general) suitable as a sort criterion
     */
    public boolean isSortable() {
        return true;
    }

    /**
     * Returns the name of the backend's (e.g. database) table.
     *
     * @return the table name
     */
    public String getTable() {
        return this.table;
    }

    /**
     * Sets the table to the given table name. This is currently needed, as <code>MappedGMLSchema</code> must be able to
     * resolve unspecified (null) table names.
     *
     * @param table
     *            table name to set
     */
    public void setTable( String table ) {
        this.table = table;
    }

    /**
     * Returns the name of the backend's (e.g. database) field.
     *
     * @return the field name
     */
    public String getField() {
        return this.field;
    }

    /**
     * Returns the SQL type code of the field.
     *
     * @return the SQL type code
     * @see java.sql.Types
     */
    public int getType() {
        return this.type;
    }

    /**
     * Returns whether the backend generates the value automatically on insert.
     *
     * @return true, if a value for this field is generated automatically, false otherwise
     */
    public boolean isAuto() {
        return this.auto;
    }

    /**
     * Returns <code>true</code> if the field has a numerical type.
     *
     * @see java.sql.Types
     *
     * @return <code>true</code> if the field has a numerical type, false otherwise
     */
    public boolean isNumeric() {
        switch ( getType() ) {
        case java.sql.Types.BIT:
        case java.sql.Types.BIGINT:
        case java.sql.Types.INTEGER:
        case java.sql.Types.FLOAT:
        case java.sql.Types.DOUBLE:
        case java.sql.Types.DECIMAL:
        case java.sql.Types.NUMERIC:
        case java.sql.Types.REAL:
        case java.sql.Types.SMALLINT:
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return this.table + "." + this.field;
    }

    @Override
    public boolean equals( Object other ) {
        if ( other == this ) {
            return true;
        }
        if ( other instanceof MappingField ) {
            MappingField f = (MappingField) other;
            return f.table.equals( table ) && f.field.equals( field ) && f.type == type;
        }
        return super.equals( other );
    }

}
