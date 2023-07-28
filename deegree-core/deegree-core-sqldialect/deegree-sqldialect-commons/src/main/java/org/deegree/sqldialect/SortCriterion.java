package org.deegree.sqldialect;

import org.deegree.commons.jdbc.TableName;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class SortCriterion {

	private final String columnName;

	private final TableName tableName;

	private final boolean sortAscending;

	public SortCriterion(String columnName, TableName tableName, boolean sortAscending) {
		this.columnName = columnName;
		this.sortAscending = sortAscending;
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public TableName getTableName() {
		return tableName;
	}

	public boolean isSortAscending() {
		return sortAscending;
	}

}