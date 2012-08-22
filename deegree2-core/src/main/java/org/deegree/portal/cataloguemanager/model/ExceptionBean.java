package org.deegree.portal.cataloguemanager.model;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public class ExceptionBean {

    private String message;
    private String source;
    
    /**
     * 
     *
     */
    public ExceptionBean() {
        this.message = "EXCEPTION:";
        this.source = "unknown";
    }
    
    /**
     * 
     * @param source
     * @param message
     */
    public ExceptionBean(String source, String message) {
        this.message = message;
        this.source = source;
    }
    
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    /**
     * @param message the message to set
     */
    public void setMessage( String message ) {
        this.message = message;
    }
    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }
    /**
     * @param source the source to set
     */
    public void setSource( String source ) {
        this.source = source;
    }
}
