//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.record.persistence;

/**
 * Defines the mapping information from a profile specific propertyname to the database table.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class MappingInfo {

    private String table;

    private String column;

    private ColumnType type;

    /**
     * 
     * @param table
     *            specifies the table in which this property is archived
     * @param column
     *            specifies the columnname in the table
     * @param type
     *            specifies the type of the column
     */
    public MappingInfo( String table, String column, ColumnType type ) {
        this.table = table;
        this.column = column;
        this.type = type;
    }

    /**
     * 
     * Specifies the datatype for a property in the backend.
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author: thomas $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum ColumnType {
        /**
         * a string
         */
        STRING,

        /**
         * an integer
         */
        INTEGER,

        /**
         * a date
         */
        DATE,

        /**
         * an envelope
         */
        ENVELOPE,

        /**
         * a float
         */
        FLOAT
    }

    /**
     * @return the table
     */
    public String getTables() {
        return table;
    }

    /**
     * @return the column
     */
    public String getColumn() {
        return column;
    }

    /**
     * @return the type
     */
    public ColumnType getType() {
        return type;
    }

}
