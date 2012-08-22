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
package org.deegree.io.datastore.sql.transaction.insert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.TransactionException;
import org.deegree.ogcwebservices.wfs.operation.transaction.Insert;

/**
 * Represents a table row (columns + values) which has to be inserted as part of an {@link Insert} operation.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class InsertRow {

    private static final ILogger LOG = LoggerFactory.getLogger( InsertRow.class );

    /** Database table */
    protected String table;

    /** Maps column names to their corresponding {@link InsertField} instance. */
    protected Map<String, InsertField> columnMap = new LinkedHashMap<String, InsertField>();

    /**
     * Creates a new <code>InsertRow</code> instance for the given table.
     *
     * @param table
     */
    public InsertRow( String table ) {
        this.table = table;
    }

    /**
     * Returns the name of table.
     *
     * @return the name of table
     */
    public String getTable() {
        return this.table;
    }

    /**
     * Sets the value to be inserted in the given table column.
     * <p>
     * In complex + erroneous mappings (or feature instances), it may happen that different property values (mapped to
     * the same column) imply different values. This is checked for and in case an {@link TransactionException} is
     * thrown.
     *
     * @param column
     * @param value
     * @param sqlType
     * @param isPK
     * @return column + value to be set
     * @throws TransactionException
     *             if the value for the column clashes
     */
    public InsertField setColumn( String column, Object value, int sqlType, boolean isPK )
                            throws TransactionException {

        InsertField field = new InsertField( this, column, sqlType, value, isPK );
        InsertField presentField = columnMap.get( column );
        if ( presentField != null && ( !presentField.getValue().toString().equals( value.toString() ) ) ) {
            Object[] params = new Object[] { column, this.table, presentField.getValue().toString(), value.toString() };
            String msg = Messages.getMessage( "DATASTORE_AMBIGOUS_COLUMN_VALUES", params );
            throw new TransactionException( msg );
        }
        if ( presentField == null ) {
            this.columnMap.put( column, field );
        }
        // TODO type check
        return field;
    }

    /**
     * Sets the value to be inserted in the given geometry column.
     * <p>
     * In complex + erroneous mappings (or feature instances), it may happen that different property values (mapped to
     * the same column) imply different values. This is checked for and in case an {@link TransactionException} is
     * thrown.
     *
     * @param column
     * @param value
     * @param sqlType
     * @param isPK
     * @param internalSrsCode
     * @return column + value to be set
     * @throws TransactionException
     *             if the value for the column clashes
     */
    public InsertGeometryField setGeometryColumn( String column, Object value, int sqlType, boolean isPK,
                                                  int internalSrsCode )
                            throws TransactionException {

        InsertGeometryField field = new InsertGeometryField( this, column, sqlType, value, isPK, internalSrsCode );
        InsertField presentField = columnMap.get( column );
        if ( presentField != null && ( !presentField.getValue().toString().equals( value.toString() ) ) ) {
            Object[] params = new Object[] { column, this.table, presentField.getValue().toString(), value.toString() };
            String msg = Messages.getMessage( "DATASTORE_AMBIGOUS_COLUMN_VALUES", params );
            throw new TransactionException( msg );
        }
        if ( presentField == null ) {
            this.columnMap.put( column, field );
        }
        // TODO type check
        return field;
    }

    /**
     * Sets the value to be inserted in the given table column - the column references the given {@link InsertField} and
     * thus must have the same value as the referenced column.
     * <p>
     * In complex + erroneous mappings (or feature instances), it may happen that different property values (mapped to
     * the same column) imply different values. This is checked for and in case an {@link TransactionException} is
     * thrown.
     *
     * @param column
     * @param referencedField
     * @throws TransactionException
     *             if the value for the column clashes
     */
    public void linkColumn( String column, InsertField referencedField )
                            throws TransactionException {

        if ( referencedField.getValue() == null ) {
            LOG.logError( "linkColumn (): referencedField is null" );
            throw new TransactionException( "linkColumn (): referenced field is null!" );
        }

        InsertField presentField = this.columnMap.get( column );
        if ( presentField != null
             && ( !presentField.getValue().toString().equals( referencedField.getValue().toString() ) ) ) {
            Object[] params = new Object[] { column, this.table, presentField.getValue().toString(),
                                            referencedField.getValue().toString() };
            String msg = Messages.getMessage( "DATASTORE_AMBIGOUS_COLUMN_VALUES", params );
            throw new TransactionException( msg );
        }

        InsertField field = presentField;
        if ( presentField != null ) {
            presentField.relinkField( referencedField );
        } else {
            field = new InsertField( this, column, referencedField );
            this.columnMap.put( column, field );
        }
        referencedField.addReferencingField( field );
    }

    /**
     * Returns all {@link InsertField}s.
     *
     * @return all InsertFields
     */
    public Collection<InsertField> getColumns() {
        return this.columnMap.values();
    }

    /**
     * Returns the {@link InsertField} for the given column name.
     *
     * @param column
     *            requested column name
     * @return the InsertField for the given column name, or null if it does not exist
     */
    public InsertField getColumn( String column ) {
        return this.columnMap.get( column );
    }

    /**
     * Returns the {@link InsertField} for the primary key column.
     *
     * @return the InsertField for the primary key column
     */
    public InsertField getPKColumn() {
        InsertField pkField = null;
        for ( InsertField field : this.columnMap.values() ) {
            if ( field.isPK() ) {
                pkField = field;
                break;
            }
        }
        return pkField;
    }

    /**
     * Returns all {@link InsertField} that reference a column in this <code>InsertRow</code>. The fields may well be
     * from other tables.
     *
     * @return all InsertFields that reference a column in this InsertRow
     */
    public List<InsertField> getReferencingFields() {
        List<InsertField> referencingFields = new ArrayList<InsertField>();
        Iterator<InsertField> iter = this.columnMap.values().iterator();
        while ( iter.hasNext() ) {
            InsertField field = iter.next();
            referencingFields.addAll( field.getReferencingFields() );
        }
        return referencingFields;
    }

    /**
     * Returns all {@link InsertRow}s that reference a column in this <code>InsertRow</code>. The rows may well be from
     * other tables.
     *
     * @return all InsertRows that reference a column in this InsertRow
     */
    Collection<InsertRow> getReferencingRows() {
        Set<InsertRow> referencingRows = new HashSet<InsertRow>();
        Iterator<InsertField> iter = this.columnMap.values().iterator();
        while ( iter.hasNext() ) {
            InsertField field = iter.next();
            referencingRows.addAll( field.getReferencingRows() );
        }
        return referencingRows;
    }

    /**
     * Returns all {@link InsertField}s that are referenced by a field from this <code>InsertRow</code>. The fields may
     * well be from other tables.
     *
     * @return all InsertField that are referenced by a field from this InsertRow
     */
    List<InsertField> getReferencedFields() {
        List<InsertField> referencedFields = new ArrayList<InsertField>();
        Iterator<InsertField> iter = this.columnMap.values().iterator();
        while ( iter.hasNext() ) {
            InsertField field = iter.next();
            InsertField referencedField = field.getReferencedField();
            if ( referencedField != null ) {
                referencedFields.add( referencedField );
            }
        }
        return referencedFields;
    }

    /**
     * Returns all {@link InsertRow}s that are referenced by a field from this <code>InsertRow</code>. The rows may well
     * be from other tables.
     *
     * @return all InsertRows that are referenced by a field from this InsertRow
     */
    private Collection<InsertRow> getReferencedRows() {
        Set<InsertRow> referencedRows = new HashSet<InsertRow>();
        Iterator<InsertField> iter = this.columnMap.values().iterator();
        while ( iter.hasNext() ) {
            InsertField field = iter.next();
            InsertRow referencedRow = field.getReferencedRow();
            if ( referencedRow != null ) {
                referencedRows.add( referencedRow );
            }
        }
        return referencedRows;
    }

    /**
     * Sorts the given <code>InsertRow</code>s topologically (respecting the foreign key constraints), so they can be
     * inserted in the resulting order without causing foreign key violations.
     * <p>
     * Number of precedessors (pre): number of fields that *are referenced by* this row Number of successors (post) :
     * number of fields that *reference* this row
     *
     * @param inserts
     *            insert rows to sort
     * @return the nodes of the graph in topological order if no cycle is present, else in arbitrary order
     */
    public static List<InsertRow> getInsertOrder( List<InsertRow> inserts ) {

        List<InsertRow> result = new ArrayList<InsertRow>();

        // key: inserts, value: number of fields that are referenced by this row
        Map<InsertRow, Integer> preMap = new HashMap<InsertRow, Integer>();

        // key: inserts with no foreign key constraints
        List<InsertRow> noPre = new ArrayList<InsertRow>();

        // build map
        Iterator<InsertRow> insertIter = inserts.iterator();
        while ( insertIter.hasNext() ) {
            InsertRow insertRow = insertIter.next();
            int pre = insertRow.getReferencedFields().size();
            LOG.logDebug( "Adding row to preMap: " + insertRow );
            preMap.put( insertRow, pre );
            if ( pre == 0 ) {
                noPre.add( insertRow );
            }
        }

        while ( !noPre.isEmpty() ) {
            // select an insert row that has no open fk constraints
            InsertRow insertRow = noPre.get( 0 );
            noPre.remove( 0 );
            result.add( insertRow );

            // decrease the number of pending fk constraints for all insert rows that
            // reference the currently processed insert row
            Collection<InsertField> postList = insertRow.getReferencingFields();
            Iterator<InsertField> iter = postList.iterator();
            while ( iter.hasNext() ) {
                InsertField postField = iter.next();
                if ( preMap.get( postField.getRow() ) == null ) {
                    LOG.logDebug( "No pre info for: " + postField.getRow() );
                }
                int pre = preMap.get( postField.getRow() );
                preMap.put( postField.getRow(), --pre );
                if ( pre == 0 ) {
                    noPre.add( postField.getRow() );
                }
            }
        }

        if ( result.size() != inserts.size() ) {
            Collection<InsertRow> cycle = InsertRow.findCycle( inserts );
            Iterator<InsertRow> cycleIter = cycle.iterator();
            StringBuffer sb = new StringBuffer();
            while ( cycleIter.hasNext() ) {
                sb.append( cycleIter.next() );
                if ( cycle.iterator().hasNext() ) {
                    sb.append( " -> " );
                }
            }
            String msg = Messages.getMessage( "DATASTORE_FK_CYCLE", sb.toString() );
            LOG.logWarning( msg );
            result.clear();
            result.addAll( inserts );
        }
        return result;
    }

    /**
     * Checks if the given <code>InsertRow</code>s contain cyclic fk constraints.
     *
     * @param rows
     * @return steps of the cycle, or null if there is none
     */
    private static Collection<InsertRow> findCycle( Collection<InsertRow> rows ) {

        Iterator<InsertRow> rowsIter = rows.iterator();
        while ( rowsIter.hasNext() ) {
            InsertRow begin = rowsIter.next();
            Iterator<InsertRow> refIter = begin.getReferencedRows().iterator();
            while ( refIter.hasNext() ) {
                InsertRow referencedRow = refIter.next();
                List<InsertRow> cycle = findCycleRecursion( referencedRow, begin, new ArrayList<InsertRow>() );
                if ( cycle != null ) {
                    return cycle;
                }
            }
        }
        return null;
    }

    private static List<InsertRow> findCycleRecursion( InsertRow next, InsertRow begin, List<InsertRow> steps ) {

        if ( steps.contains( next ) ) {
            steps.add( next );
            return steps;
        }
        steps.add( next );

        Iterator<InsertRow> refIter = next.getReferencedRows().iterator();
        while ( refIter.hasNext() ) {
            InsertRow referencedRow = refIter.next();
            List<InsertRow> cycle = findCycleRecursion( referencedRow, begin, steps );
            if ( cycle != null ) {
                return cycle;
            }
        }
        steps.remove( steps.size() - 1 );
        return null;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer( "InsertRow, table: '" );
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
