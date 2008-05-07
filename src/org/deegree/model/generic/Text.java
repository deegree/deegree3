package org.deegree.model.generic;

import org.apache.axiom.om.OMText;
import org.deegree.model.generic.schema.TextType;

/**
 * An <code>OMText</code> with schema information.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public interface Text extends Node, OMText {

    /**
     * Returns the schema information for this node.
     * 
     * @return the schema information
     */
    public TextType getSchemaInfo();
}
