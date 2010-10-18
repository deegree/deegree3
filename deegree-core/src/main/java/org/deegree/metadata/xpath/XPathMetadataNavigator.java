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
package org.deegree.metadata.xpath;

import org.deegree.metadata.MetadataRecord;
import org.jaxen.DefaultNavigator;
import org.jaxen.XPath;
import org.jaxen.saxpath.SAXPathException;
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
public class XPathMetadataNavigator extends DefaultNavigator {

    private static Logger LOG = LoggerFactory.getLogger( XPathMetadataNavigator.class );

    private final DocumentNode rootNode;

    public XPathMetadataNavigator( MetadataRecord root ) {

        this.rootNode = new DocumentNode( new ElementNode( root.getName(), null ) );
        LOG.debug( "" + rootNode.getRootNode() );

    }

    @Override
    public String getAttributeName( Object node ) {
        String msg = "getAttributeName(Object) called with argument (" + node + "), but method not implemented";
        throw new UnsupportedOperationException( msg );
    }

    @Override
    public String getAttributeNamespaceUri( Object node ) {
        String msg = "getAttributeNamespaceUri(Object) called with argument (" + node + "), but method not implemented";
        throw new UnsupportedOperationException( msg );
    }

    @Override
    public String getAttributeQName( Object node ) {
        String msg = "getAttributeQName(Object) called with argument (" + node + "), but method not implemented";
        throw new UnsupportedOperationException( msg );
    }

    @Override
    public String getAttributeStringValue( Object node ) {
        String msg = "getAttributeStringValue(Object) called with argument (" + node + "), but method not implemented";
        throw new UnsupportedOperationException( msg );
    }

    @Override
    public String getCommentStringValue( Object node ) {
        String msg = "getCommentStringValue(Object) called with argument (" + node + "), but method not implemented";
        throw new UnsupportedOperationException( msg );
    }

    @Override
    public String getElementName( Object node ) {
        String name = null;
        if ( isElement( node ) ) {
            ElementNode el = (ElementNode) node;
            name = el.getLocalName();
        }
        return name;
    }

    @Override
    public String getElementNamespaceUri( Object node ) {
        String ns = null;
        if ( isElement( node ) ) {
            ElementNode el = (ElementNode) node;
            ns = el.getNamespaceUri();
        }
        return ns;
    }

    @Override
    public String getElementQName( Object node ) {
        String name = null;
        if ( isElement( node ) ) {
            ElementNode el = (ElementNode) node;
            name = el.getPrefixedName();
        }
        return name;
    }

    @Override
    public String getElementStringValue( Object node ) {
        String msg = "getElementStringValue(Object) called with argument (" + node + "), but method not implemented";
        throw new UnsupportedOperationException( msg );
    }

    @Override
    public String getNamespacePrefix( Object node ) {
        String msg = "getNamespacePrefix(Object) called with argument (" + node + "), but method not implemented";
        throw new UnsupportedOperationException( msg );
    }

    @Override
    public String getNamespaceStringValue( Object node ) {
        String msg = "getNamespaceStringValue(Object) called with argument (" + node + "), but method not implemented";
        throw new UnsupportedOperationException( msg );
    }

    @Override
    public String getTextStringValue( Object node ) {
        String msg = "getTextStringValue(Object) called with argument (" + node + "), but method not implemented";
        throw new UnsupportedOperationException( msg );
    }

    @Override
    public boolean isAttribute( Object arg0 ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isComment( Object arg0 ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDocument( Object obj ) {
        return obj instanceof DocumentNode;
    }

    @Override
    public boolean isElement( Object obj ) {
        return obj instanceof ElementNode;
    }

    @Override
    public boolean isNamespace( Object arg0 ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isProcessingInstruction( Object arg0 ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isText( Object arg0 ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public XPath parseXPath( String xpathExpr )
                            throws SAXPathException {
        String msg = "parseXPath(String) called with argument (" + xpathExpr + "), but method not implemented";
        throw new UnsupportedOperationException( msg );

        // return new XPathMetadata( xpathExpr, root );
    }

}
