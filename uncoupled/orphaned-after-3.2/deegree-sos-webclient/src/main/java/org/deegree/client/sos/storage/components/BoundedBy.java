package org.deegree.client.sos.storage.components;

import java.util.List;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;

/**
 * Helper class for Observation class containing the contents of XML element "BoundedBy".
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 *
 */
public class BoundedBy {
    
    private String type;
    
    private String text;
    
    private List<OMElement> elements;
    
    private List<OMAttribute> attributes;
    
    public BoundedBy(){
        
    }
    
    public String getType(){
        return type;
    }
    
    public String getText(){
        return text;
    }
    
    public List<OMElement> getElements(){
        return elements;
    }
    
    public List<OMAttribute> getAttributes(){
        return attributes;
    }
    
    public void setType(String that){
        type = that;
    }
    
    public void setText(String that){
        text = that;
    }
    
    public void setElements(List<OMElement> that){
        elements = that;
    }
    
    public void setAttributes(List<OMAttribute> that){
        attributes = that;
    }

}
