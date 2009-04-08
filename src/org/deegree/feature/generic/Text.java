package org.deegree.feature.generic;

import org.deegree.feature.generic.schema.TextType;

/**
 * A <code>Text</code> node with type information.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public interface Text extends Node {

    /**
     * Returns the type information for this node.
     * 
     * @return the type information
     */
    public TextType getType();
    
    /**
     * Returns the <code>String</code> value of this node.
     * 
     * @return the <code>String</code> value
     */
    public String getValue();
}
