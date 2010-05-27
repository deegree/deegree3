//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.filter.expression;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.XMLConstants.NULL_NS_URI;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchableObject;
import org.deegree.gml.GMLVersion;
import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.NamespaceContext;
import org.jaxen.expr.Expr;

/**
 * {@link Expression} that usually just encodes the name of a property of an object, but may also contains an XPath 1.0
 * expression.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class PropertyName implements Expression {

    private String text;

    private NamespaceContext nsContext;

    private Expr xpath;

    private QName simpleProp;

    private Boolean isSimple;

    /**
     * Creates a new {@link PropertyName} instance from an encoded XPath-expression and the namespace bindings.
     * 
     * @param text
     *            must be a valid XPath 1.0-expression, never <code>null</code>
     * @param nsContext
     *            binding of the namespaces used in the XPath expression
     */
    public PropertyName( String text, NamespaceContext nsContext ) {
        this.text = text;
        this.nsContext = nsContext;
    }

    // TODO check if this should stay here
    public void set( String text, NamespaceContext nsContext ) {
        this.text = text;
        this.nsContext = nsContext;
        this.xpath = null;
        this.simpleProp = null;
        this.isSimple = null;
    }

    /**
     * Creates a new {@link PropertyName} instance that select a property of a {@link MatchableObject}.
     * 
     * @param name
     *            qualified name of the property, never <code>null</code>
     */
    public PropertyName( QName name ) {
        this.nsContext = new org.deegree.commons.xml.NamespaceContext();
        if ( name.getNamespaceURI() != null ) {
            String prefix = ( name.getPrefix() != null && !"".equals( name.getPrefix() ) ) ? name.getPrefix() : "app";
            ( (org.deegree.commons.xml.NamespaceContext) nsContext ).addNamespace( prefix, name.getNamespaceURI() );
            this.text = prefix + ":" + name.getLocalPart();
        } else {
            this.text = name.getLocalPart();
        }
    }

    /**
     * Returns the <a href="http://jaxen.codehaus.org/">Jaxen</a> representation of the XPath expression, which provides
     * access to the syntax tree.
     * 
     * @return the compiled expression, or <code>null</code> if this {@link PropertyName} represents the empty string
     * @throws FilterEvaluationException
     *             if this {@link PropertyName} does not denote a valid XPath 1.0 expression
     */
    public Expr getAsXPath()
                            throws FilterEvaluationException {
        if ( xpath == null ) {
            try {
                xpath = new BaseXPath( text, null ).getRootExpr();
            } catch ( JaxenException e ) {
                String msg = "'" + text + "' does not denote a valid XPath 1.0 expression: " + e.getMessage();
                throw new FilterEvaluationException( msg );
            }
        }
        return xpath;
    }

    /**
     * Returns the property name value (an XPath-expression).
     * 
     * @return the XPath property name, this may be an empty string, but never <code>null</code>
     */
    public String getPropertyName() {
        return text;
    }

    /**
     * If the property name is simple, the element name is returned.
     * 
     * @see #isSimple()
     * @return the qualified name value, or <code>null</code> if the property name is not simple
     */
    public QName getAsQName() {
        if ( simpleProp == null && isSimple() ) {
            int colonIdx = text.indexOf( ":" );
            String localPart = null;
            String prefix = DEFAULT_NS_PREFIX;
            if ( colonIdx == -1 ) {
                localPart = text;
            } else {
                localPart = text.substring( colonIdx + 1 );
                prefix = text.substring( 0, colonIdx );
            }
            String namespace = nsContext == null ? NULL_NS_URI : nsContext.translateNamespacePrefixToUri( prefix );
            simpleProp = new QName( namespace, localPart, prefix );
        }
        return simpleProp;
    }

    /**
     * Returns the bindings for the namespaces used in the XPath expression.
     * 
     * @return the namespace bindings, never <code>null</code>
     */
    public NamespaceContext getNsContext() {
        return nsContext;
    }

    /**
     * Returns whether the property name is simple, i.e. if it only contains of a single element step.
     * 
     * @return <code>true</code>, if the property is simple, <code>false</code> otherwise
     */
    public boolean isSimple() {
        if ( isSimple == null ) {
            // TODO check against XPath spec.
            isSimple = !text.contains( "@" ) && !text.contains( "/" ) && !text.contains( "[" ) && !text.contains( "*" )
                       && !text.contains( "::" ) && !text.contains( "(" ) && !text.contains( "=" );
        }
        return isSimple;
    }

    @Override
    public Type getType() {
        return Type.PROPERTY_NAME;
    }

    @Override
    public TypedObjectNode[] evaluate( MatchableObject obj )
                            throws FilterEvaluationException {
        TypedObjectNode[] values;
        try {
            // TODO outfactor GML version
            values = obj.evalXPath( this, GMLVersion.GML_31 );
        } catch ( JaxenException e ) {
            e.printStackTrace();
            throw new FilterEvaluationException( e.getMessage() );
        }
        return values;
    }

    @Override
    public String toString() {
        return toString( "" );
    }

    @Override
    public String toString( String indent ) {
        String s = indent + "-PropertyName ('" + text + "')\n";
        return s;
    }

    @Override
    public Expression[] getParams() {
        return new Expression[0];
    }
}
