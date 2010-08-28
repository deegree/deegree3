package org.deegree.console;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

/**
 * TODO Remove this
 * 
 * Just needed, because I don't understand how to pass JSF beans from one page to another... 
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
@ManagedBean
@ApplicationScoped
public class EditorConfig {

    static XMLConfig current;

    public XMLConfig getConfig() {
        return current;
    }
}
