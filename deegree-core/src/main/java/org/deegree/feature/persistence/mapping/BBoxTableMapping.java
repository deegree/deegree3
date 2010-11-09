/**
 * 
 */
package org.deegree.feature.persistence.mapping;

import org.deegree.cs.CRS;

/**
 * @author markus
 * 
 */
public class BBoxTableMapping {

    private CRS crs;

    public BBoxTableMapping( CRS crs ) {
        this.crs = crs;
    }

    public String getTable() {
        return "feature_types";
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
