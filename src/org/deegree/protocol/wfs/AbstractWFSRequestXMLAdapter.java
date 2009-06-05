package org.deegree.protocol.wfs;

import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;

/**
 * Provides basic functionality for parsing WFS XML requests.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class AbstractWFSRequestXMLAdapter extends XMLAdapter {

    /** Namspace context with predefined bindings "wfs" and "wfs200" */
    protected static NamespaceContext nsContext;

    /** Namespace binding for WFS 1.0.0 and WFS 1.1.0 constructs */
    protected final static String WFS_PREFIX = "wfs";

    /** Namespace binding for WFS 2.0.0 constructs */
    protected final static String WFS_200_PREFIX = "wfs200";

    static {
        nsContext = new NamespaceContext( XMLAdapter.nsContext );
        nsContext.addNamespace( WFS_PREFIX, WFSConstants.WFS_NS );
        nsContext.addNamespace( WFS_200_PREFIX, WFSConstants.WFS_200_NS );
    }    
}
