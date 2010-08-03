/**
 * 
 */
package org.deegree.feature.persistence.mapping;

/**
 * @author markus
 *
 */
public class BBoxTableMapping {
    
    public String getTable () {
        return "feature_types";
    }
    
    public String getFTNameColumn () {
        return "qname";
    }

    public String getBBoxColumn () {
        return "bbox";
    }        
}
