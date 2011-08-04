//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.commons.jdbc;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.sql.ParticleConversion;
import org.deegree.commons.tom.sql.ParticleConverter;

/**
 * Encapsulates columns and values for manipulating one row in a database.
 * 
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class TransactionRow {

    protected QTableName table;

    protected final LinkedHashMap<String, String> columnToLiteral = new LinkedHashMap<String, String>();

    protected final LinkedHashMap<String, Object> columnToObject = new LinkedHashMap<String, Object>();

    /**
     * @param table
     *            table targeted by the transaction, must not be <code>null</code>
     */
    public TransactionRow( QTableName table ) {
        this.table = table;
    }

    /**
     * @return the table targeted for the transaction
     */
    public QTableName getTable() {
        return table;
    }

    /**
     * 
     * @param column
     *            the name of the column, must not be <code>null</code>
     * @param literal
     *            a string literal to add as value
     */
    public void addLiteralValue( String column, String literal ) {
        columnToLiteral.put( column.toLowerCase(), literal );
    }

    /**
     * 
     * @param column
     *            the name of the column, must not be <code>null</code>
     * @param value
     *            the value to append, can be <code>null</code>
     */
    public void addPreparedArgument( String column, Object value ) {
        addPreparedArgument( column, value, "?" );
    }

    /**
     * Use this method for example to manipulate a geometry: (#addPreparedArgument("geom", geomAsWKB,
     * "SetSRID(GeomFromWKB(?)"))
     * 
     * @param column
     *            the name of the column, must not be <code>null</code>
     * @param value
     *            the value to append, can be <code>null</code>
     * @param literal
     *            the string literal to append to the list of values, must not be <code>null</code>
     */
    public void addPreparedArgument( String column, Object value, String literal ) {
        columnToLiteral.put( column.toLowerCase(), literal );
        columnToObject.put( column.toLowerCase(), value );
    }

    public <T extends TypedObjectNode> void addPreparedArgument( String column, T particle,
                                                                 ParticleConverter<T> converter ) {
        columnToLiteral.put( column.toLowerCase(), converter.getSetSnippet( particle ) );
        columnToObject.put( column.toLowerCase(), new ParticleConversion<T>( converter, particle ) );
    }

    /**
     * @return all columns considered by this tranaction
     */
    public Collection<String> getColumns() {
        return columnToLiteral.keySet();
    }

    /**
     * @param column
     *            the name of the column, must not be <code>null</code>
     * @return the value assigned to the column with the given name, null if there is not value assigned
     */
    public Object get( String column ) {
        return columnToObject.get( column.toLowerCase() );
    }

    /**
     * @return the generated sl statement
     */
    public abstract String getSql();
}