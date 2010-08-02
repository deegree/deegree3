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
        return "FT_BBOX";
    }
    
    public String getFTNameColumn () {
        return "ft_qname";
    }

    public String getBBoxColumn () {
        return "ft_bbox";
    }        
}
