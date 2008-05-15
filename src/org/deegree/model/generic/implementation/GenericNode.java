package org.deegree.model.generic.implementation;

import org.deegree.model.generic.Node;
import org.deegree.model.generic.schema.NodeType;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class GenericNode implements Node {

    protected NodeType type;        

    protected GenericNode (NodeType type) {
        this.type = type;
    }
    
    public abstract NodeType getType();
}
