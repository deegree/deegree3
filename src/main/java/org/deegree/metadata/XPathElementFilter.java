//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.metadata;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.deegree.commons.xml.XPath;
import org.jaxen.JaxenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class XPathElementFilter implements OMElement {

    private static Logger LOG = LoggerFactory.getLogger( XPathElementFilter.class );

    private final OMElement input;

    private final Set<XPath> removeElements;

    public XPathElementFilter( OMElement input, Set<XPath> removeElements ) {
        this.input = input;
        this.removeElements = removeElements;
        detach();
    }

    public OMAttribute addAttribute( OMAttribute arg0 ) {
        return input.addAttribute( arg0 );
    }

    public OMAttribute addAttribute( String arg0, String arg1, OMNamespace arg2 ) {
        return input.addAttribute( arg0, arg1, arg2 );
    }

    public void addChild( OMNode arg0 ) {
        input.addChild( arg0 );
    }

    public void build() {
        input.build();
    }

    public void buildNext() {
        input.buildNext();
    }

    public void buildWithAttachments() {
        input.buildWithAttachments();
    }

    public OMElement cloneOMElement() {
        return input.cloneOMElement();
    }

    public void close( boolean arg0 ) {
        input.close( arg0 );
    }

    public OMNamespace declareDefaultNamespace( String arg0 ) {
        return input.declareDefaultNamespace( arg0 );
    }

    public OMNamespace declareNamespace( OMNamespace arg0 ) {
        return input.declareNamespace( arg0 );
    }

    public OMNamespace declareNamespace( String arg0, String arg1 ) {
        return input.declareNamespace( arg0, arg1 );
    }

    public OMNode detach()
                            throws OMException {
        AXIOMXPath path;
        for ( XPath x : removeElements ) {
            try {
                path = new AXIOMXPath( x.getXPath() );
                path.setNamespaceContext( x.getNamespaceContext() );
                Object node = path.selectSingleNode( input );
                if ( node != null ) {
                    ( (OMElement) node ).detach();
                }

            } catch ( JaxenException e ) {

                LOG.debug( e.getMessage() );
                throw new OMException( e.getMessage() );
            }
        }
        return input.detach();
    }

    public void discard()
                            throws OMException {
        input.discard();
    }

    public OMNamespace findNamespace( String arg0, String arg1 ) {
        return input.findNamespace( arg0, arg1 );
    }

    public OMNamespace findNamespaceURI( String arg0 ) {
        return input.findNamespaceURI( arg0 );
    }

    public Iterator getAllAttributes() {
        return input.getAllAttributes();
    }

    public Iterator getAllDeclaredNamespaces()
                            throws OMException {
        return input.getAllDeclaredNamespaces();
    }

    public OMAttribute getAttribute( QName arg0 ) {
        return input.getAttribute( arg0 );
    }

    public String getAttributeValue( QName arg0 ) {
        return input.getAttributeValue( arg0 );
    }

    public OMXMLParserWrapper getBuilder() {
        return input.getBuilder();
    }

    public Iterator getChildElements() {
        return input.getChildElements();
    }

    public Iterator getChildren() {
        return input.getChildren();
    }

    public Iterator getChildrenWithLocalName( String arg0 ) {
        return input.getChildrenWithLocalName( arg0 );
    }

    public Iterator getChildrenWithName( QName arg0 ) {
        return input.getChildrenWithName( arg0 );
    }

    public Iterator getChildrenWithNamespaceURI( String arg0 ) {
        return input.getChildrenWithNamespaceURI( arg0 );
    }

    public OMNamespace getDefaultNamespace() {
        return input.getDefaultNamespace();
    }

    public OMElement getFirstChildWithName( QName arg0 )
                            throws OMException {
        return input.getFirstChildWithName( arg0 );
    }

    public OMElement getFirstElement() {
        return input.getFirstElement();
    }

    public OMNode getFirstOMChild() {
        return input.getFirstOMChild();
    }

    public int getLineNumber() {
        return input.getLineNumber();
    }

    public String getLocalName() {
        return input.getLocalName();
    }

    public OMNamespace getNamespace()
                            throws OMException {
        return input.getNamespace();
    }

    public OMNode getNextOMSibling()
                            throws OMException {
        return input.getNextOMSibling();
    }

    public OMFactory getOMFactory() {
        return input.getOMFactory();
    }

    public OMContainer getParent() {
        return input.getParent();
    }

    public OMNode getPreviousOMSibling() {
        return input.getPreviousOMSibling();
    }

    public QName getQName() {
        return input.getQName();
    }

    public String getText() {
        return input.getText();
    }

    public QName getTextAsQName() {
        return input.getTextAsQName();
    }

    public int getType() {
        return input.getType();
    }

    public XMLStreamReader getXMLStreamReader() {
        return input.getXMLStreamReader();
    }

    public XMLStreamReader getXMLStreamReaderWithoutCaching() {
        return input.getXMLStreamReaderWithoutCaching();
    }

    public void insertSiblingAfter( OMNode arg0 )
                            throws OMException {
        input.insertSiblingAfter( arg0 );
    }

    public void insertSiblingBefore( OMNode arg0 )
                            throws OMException {
        input.insertSiblingBefore( arg0 );
    }

    public boolean isComplete() {
        return input.isComplete();
    }

    public void removeAttribute( OMAttribute arg0 ) {
        input.removeAttribute( arg0 );
    }

    public QName resolveQName( String arg0 ) {
        return input.resolveQName( arg0 );
    }

    public void serialize( OutputStream arg0, OMOutputFormat arg1 )
                            throws XMLStreamException {
        input.serialize( arg0, arg1 );
    }

    public void serialize( OutputStream arg0 )
                            throws XMLStreamException {
        input.serialize( arg0 );
    }

    public void serialize( Writer arg0, OMOutputFormat arg1 )
                            throws XMLStreamException {
        input.serialize( arg0, arg1 );
    }

    public void serialize( Writer arg0 )
                            throws XMLStreamException {
        input.serialize( arg0 );
    }

    public void serialize( XMLStreamWriter arg0 )
                            throws XMLStreamException {
        input.serialize( arg0 );
    }

    public void serializeAndConsume( OutputStream arg0, OMOutputFormat arg1 )
                            throws XMLStreamException {
        input.serializeAndConsume( arg0, arg1 );
    }

    public void serializeAndConsume( OutputStream arg0 )
                            throws XMLStreamException {
        input.serializeAndConsume( arg0 );
    }

    public void serializeAndConsume( Writer arg0, OMOutputFormat arg1 )
                            throws XMLStreamException {
        input.serializeAndConsume( arg0, arg1 );
    }

    public void serializeAndConsume( Writer arg0 )
                            throws XMLStreamException {
        input.serializeAndConsume( arg0 );
    }

    public void serializeAndConsume( XMLStreamWriter arg0 )
                            throws XMLStreamException {
        input.serializeAndConsume( arg0 );
    }

    public void setBuilder( OMXMLParserWrapper arg0 ) {
        input.setBuilder( arg0 );
    }

    public void setFirstChild( OMNode arg0 ) {
        input.setFirstChild( arg0 );
    }

    public void setLineNumber( int arg0 ) {
        input.setLineNumber( arg0 );
    }

    public void setLocalName( String arg0 ) {
        input.setLocalName( arg0 );
    }

    public void setNamespace( OMNamespace arg0 ) {
        input.setNamespace( arg0 );
    }

    public void setNamespaceWithNoFindInCurrentScope( OMNamespace arg0 ) {
        input.setNamespaceWithNoFindInCurrentScope( arg0 );
    }

    public void setText( QName arg0 ) {
        input.setText( arg0 );
    }

    public void setText( String arg0 ) {
        input.setText( arg0 );
    }

    public String toString() {
        return input.toString();
    }

    public String toStringWithConsume()
                            throws XMLStreamException {
        return input.toStringWithConsume();
    }

}
