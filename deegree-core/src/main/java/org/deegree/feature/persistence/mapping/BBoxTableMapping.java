/**
 * 
 */
package org.deegree.feature.persistence.mapping;

import org.deegree.commons.jdbc.QTableName;
import org.deegree.cs.CRS;

/**
 * @author markus
 * 
 */
public class BBoxTableMapping {

    private final QTableName ftTable;

    private final CRS crs;

    public BBoxTableMapping( String ftTable, CRS crs ) {
        this.ftTable = new QTableName( ftTable );
        this.crs = crs;
    }

    public QTableName getTable() {
        return ftTable;
    }

    public CRS getCRS() {
        return crs;
    }

    public String getFTNameColumn() {
        return "qname";
    }

    public String getBBoxColumn() {
        return "bbox";
    }
}
