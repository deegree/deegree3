package org.deegree.model.generic.implementation;

import java.io.OutputStream;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMOutputFormat;
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

    protected OMNode node;

    protected NodeType schemaInfo;        

    protected GenericNode (OMNode node, NodeType schemaInfo) {
        this.node = node;
        this.schemaInfo = schemaInfo;
    }
    
    public abstract NodeType getSchemaInfo();

    public void build() {
       node.build();
    }

    public void buildWithAttachments() {
        node.buildWithAttachments();
    }

    public void close( boolean arg0 ) {
        node.close(arg0);
    }

    public OMNode detach()
                            throws OMException {
        return node.detach();
    }

    public void discard()
                            throws OMException {
        node.discard();
    }

    public OMNode getNextOMSibling()
                            throws OMException {
        return getNextOMSibling();
    }

    public OMFactory getOMFactory() {
        return node.getOMFactory();
    }

    public OMContainer getParent() {
        return node.getParent();
    }

    public OMNode getPreviousOMSibling() {
        return node.getPreviousOMSibling();
    }

    public int getType() {
        return node.getType();
    }

    public void insertSiblingAfter( OMNode arg0 )
                            throws OMException {
        node.insertSiblingAfter( arg0 );
    }

    public void insertSiblingBefore( OMNode arg0 )
                            throws OMException {
        node.insertSiblingBefore( arg0 );        
    }

    public boolean isComplete() {
        return node.isComplete();
    }

    public void serialize( XMLStreamWriter arg0 )
                            throws XMLStreamException {
        node.serialize( arg0 );
    }

    public void serialize( OutputStream arg0 )
                            throws XMLStreamException {
        node.serialize( arg0 );
    }

    public void serialize( Writer arg0 )
                            throws XMLStreamException {
        node.serialize( arg0 );        
    }

    public void serialize( OutputStream arg0, OMOutputFormat arg1 )
                            throws XMLStreamException {
        node.serialize( arg0, arg1 );
    }

    public void serialize( Writer arg0, OMOutputFormat arg1 )
                            throws XMLStreamException {
        node.serialize( arg0, arg1 );
    }

    public void serializeAndConsume( XMLStreamWriter arg0 )
                            throws XMLStreamException {
        // TODO Auto-generated method stub
        
    }

    public void serializeAndConsume( OutputStream arg0 )
                            throws XMLStreamException {
        node.serializeAndConsume( arg0 );
    }

    public void serializeAndConsume( Writer arg0 )
                            throws XMLStreamException {
        node.serializeAndConsume( arg0 );
    }

    public void serializeAndConsume( OutputStream arg0, OMOutputFormat arg1 )
                            throws XMLStreamException {
        node.serializeAndConsume( arg0, arg1 );
    }

    public void serializeAndConsume( Writer arg0, OMOutputFormat arg1 )
                            throws XMLStreamException {
        node.serializeAndConsume( arg0, arg1 );
    }
}
