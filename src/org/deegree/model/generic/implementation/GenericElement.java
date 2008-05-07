//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/

package org.deegree.model.generic.implementation;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.deegree.model.generic.Element;
import org.deegree.model.generic.schema.ElementType;
import org.deegree.model.generic.schema.NodeType;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class GenericElement extends GenericNode implements Element {

    protected GenericElement( OMElement el, NodeType schemaInfo ) {
        super( el, schemaInfo );
    }
    
    @Override
    public ElementType getSchemaInfo() {
        return (ElementType) schemaInfo;
    }

    public OMAttribute addAttribute( OMAttribute arg0 ) {
        return ((OMElement) node).addAttribute( arg0 );
    }

    public OMAttribute addAttribute( String arg0, String arg1, OMNamespace arg2 ) {
        return ((OMElement) node).addAttribute( arg0, arg1, arg2 );
    }

    public OMElement cloneOMElement() {
        // TODO Auto-generated method stub
        return null;
    }

    public OMNamespace declareDefaultNamespace( String arg0 ) {
        // TODO Auto-generated method stub
        return null;
    }

    public OMNamespace declareNamespace( OMNamespace arg0 ) {
        // TODO Auto-generated method stub
        return null;
    }

    public OMNamespace declareNamespace( String arg0, String arg1 ) {
        // TODO Auto-generated method stub
        return null;
    }

    public OMNamespace findNamespace( String arg0, String arg1 ) {
        // TODO Auto-generated method stub
        return null;
    }

    public OMNamespace findNamespaceURI( String arg0 ) {
        // TODO Auto-generated method stub
        return null;
    }

    public Iterator getAllAttributes() {
        // TODO Auto-generated method stub
        return null;
    }

    public Iterator getAllDeclaredNamespaces()
                            throws OMException {
        // TODO Auto-generated method stub
        return null;
    }

    public OMAttribute getAttribute( QName arg0 ) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getAttributeValue( QName arg0 ) {
        // TODO Auto-generated method stub
        return null;
    }

    public OMXMLParserWrapper getBuilder() {
        // TODO Auto-generated method stub
        return null;
    }

    public Iterator getChildElements() {
        // TODO Auto-generated method stub
        return null;
    }

    public OMNamespace getDefaultNamespace() {
        // TODO Auto-generated method stub
        return null;
    }

    public OMElement getFirstElement() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getLineNumber() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getLocalName() {
        // TODO Auto-generated method stub
        return null;
    }

    public OMNamespace getNamespace()
                            throws OMException {
        // TODO Auto-generated method stub
        return null;
    }

    public QName getQName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getText() {
        // TODO Auto-generated method stub
        return null;
    }

    public QName getTextAsQName() {
        // TODO Auto-generated method stub
        return null;
    }

    public XMLStreamReader getXMLStreamReader() {
        // TODO Auto-generated method stub
        return null;
    }

    public XMLStreamReader getXMLStreamReaderWithoutCaching() {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeAttribute( OMAttribute arg0 ) {
        // TODO Auto-generated method stub
        
    }

    public QName resolveQName( String arg0 ) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setBuilder( OMXMLParserWrapper arg0 ) {
        // TODO Auto-generated method stub
        
    }

    public void setFirstChild( OMNode arg0 ) {
        // TODO Auto-generated method stub
        
    }

    public void setLineNumber( int arg0 ) {
        // TODO Auto-generated method stub
        
    }

    public void setLocalName( String arg0 ) {
        // TODO Auto-generated method stub
        
    }

    public void setNamespace( OMNamespace arg0 ) {
        // TODO Auto-generated method stub
        
    }

    public void setNamespaceWithNoFindInCurrentScope( OMNamespace arg0 ) {
        // TODO Auto-generated method stub
        
    }

    public void setText( String arg0 ) {
        // TODO Auto-generated method stub
        
    }

    public void setText( QName arg0 ) {
        // TODO Auto-generated method stub
        
    }

    public String toStringWithConsume()
                            throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    public void addChild( OMNode arg0 ) {
        // TODO Auto-generated method stub
        
    }

    public void buildNext() {
        // TODO Auto-generated method stub
        
    }

    public Iterator getChildren() {
        // TODO Auto-generated method stub
        return null;
    }

    public Iterator getChildrenWithLocalName( String arg0 ) {
        // TODO Auto-generated method stub
        return null;
    }

    public Iterator getChildrenWithName( QName arg0 ) {
        // TODO Auto-generated method stub
        return null;
    }

    public Iterator getChildrenWithNamespaceURI( String arg0 ) {
        // TODO Auto-generated method stub
        return null;
    }

    public OMElement getFirstChildWithName( QName arg0 )
                            throws OMException {
        // TODO Auto-generated method stub
        return null;
    }

    public OMNode getFirstOMChild() {
        // TODO Auto-generated method stub
        return null;
    }
}
