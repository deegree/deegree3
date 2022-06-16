package org.deegree.sqldialect;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class SortCriterion {

    private final String columneName;

    private final boolean sortAscending;

    public SortCriterion( String columneName, boolean sortAscending ) {
        this.columneName = columneName;
        this.sortAscending = sortAscending;
    }

    public String getColumneName() {
        return columneName;
    }

    public boolean isSortAscending() {
        return sortAscending;
    }

}