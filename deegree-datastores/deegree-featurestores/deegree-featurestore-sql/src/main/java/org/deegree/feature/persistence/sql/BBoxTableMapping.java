/**
 * 
 */
package org.deegree.feature.persistence.sql;

import org.deegree.commons.jdbc.QTableName;
import org.deegree.cs.coordinatesystems.ICRS;

/**
 * @author markus
 * 
 */
public class BBoxTableMapping {

    private final QTableName ftTable;

    private final ICRS crs;

    public BBoxTableMapping( String ftTable, ICRS crs ) {
        this.ftTable = new QTableName( ftTable );
        this.crs = crs;
    }

    public QTableName getTable() {
        return ftTable;
    }

    public ICRS getCRS() {
        return crs;
    }

    public String getFTNameColumn() {
        return "qname";
    }

    public String getBBoxColumn() {
        return "bbox";
    }
}
