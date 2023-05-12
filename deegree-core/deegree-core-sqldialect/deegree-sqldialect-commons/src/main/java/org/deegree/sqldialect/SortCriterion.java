package org.deegree.sqldialect;

import org.deegree.commons.jdbc.TableName;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class SortCriterion {

    private final String columName;

    private final TableName tableName;

    private final boolean sortAscending;

    public SortCriterion( String columName, TableName tableName, boolean sortAscending ) {
        this.columName = columName;
        this.sortAscending = sortAscending;
        this.tableName = tableName;
    }

    public String getColumName() {
        return columName;
    }

    public TableName getTableName() {
        return tableName;
    }

    public boolean isSortAscending() {
        return sortAscending;
    }
}