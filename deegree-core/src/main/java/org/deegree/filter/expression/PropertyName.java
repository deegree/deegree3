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

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XPathUtils;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.NamespaceContext;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.jaxen.saxpath.Axis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Expression} that contain an XPath 1.0 expression (but usually is a simple property name).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class PropertyName implements Expression {

    private static Logger LOG = LoggerFactory.getLogger( PropertyName.class );

    private String text;

    private NamespaceBindings bindings = new NamespaceBindings();

    private Expr xpath;

    private QName qName;

    /**
     * Creates a new {@link PropertyName} instance from an encoded XPath-expression and the namespace bindings.
     * 
     * @param text
     *            must be a valid XPath 1.0-expression, must not be <code>null</code>
     * @param nsContext
     *            binding of the namespaces used in the XPath expression, may be <code>null</code>
     * @throws IllegalArgumentException
     *             if text is not a valid XPath 1.0-expression (or a used namespace is not bound)
     */
    public PropertyName( String text, NamespaceContext nsContext ) throws IllegalArgumentException {
        this.text = text;
        init( nsContext );
    }    

    private void init( NamespaceContext nsContext ) {

        try {
            xpath = new BaseXPath( text, null ).getRootExpr();
            LOG.debug( "XPath: " + xpath );
        } catch ( JaxenException e ) {
            String msg = "'" + text + "' does not denote a valid XPath 1.0 expression: " + e.getMessage();
            throw new IllegalArgumentException( msg );
        }

        for ( String prefix : XPathUtils.extractPrefixes( xpath ) ) {
            String ns = nsContext == null ? null : nsContext.translateNamespacePrefixToUri( prefix );
            LOG.debug( prefix + " -> " + ns );
            bindings.addNamespace( prefix, ns );
        }

        if ( xpath instanceof LocationPath ) {
            LocationPath lpath = (LocationPath) xpath;
            if ( lpath.getSteps().size() == 1 ) {
                if ( lpath.getSteps().get( 0 ) instanceof NameStep ) {
                    NameStep step = (NameStep) lpath.getSteps().get( 0 );
                    if ( step.getAxis() == Axis.CHILD && step.getPredicates().isEmpty()
                         && !step.getLocalName().equals( "*" ) ) {
                        String prefix = step.getPrefix();
                        if ( prefix.isEmpty() ) {
                            qName = new QName( step.getLocalName() );
                        } else {
                            String ns = this.bindings.translateNamespacePrefixToUri( prefix );
                            qName = new QName( ns, step.getLocalName(), prefix );
                        }
                        LOG.debug( "QName: " + qName );
                    }
                }
            }
        }
    }

    /**
     * Creates a new {@link PropertyName} instance that selects a property.
     * 
     * @param name
     *            qualified name of the property, never <code>null</code>
     */
    public PropertyName( QName name ) {
        NamespaceBindings nsContext = new NamespaceBindings();
        if ( name.getNamespaceURI() != null ) {
            String prefix = ( name.getPrefix() != null && !"".equals( name.getPrefix() ) ) ? name.getPrefix() : "app";
            nsContext.addNamespace( prefix, name.getNamespaceURI() );
            this.text = prefix + ":" + name.getLocalPart();
        } else {
            this.text = name.getLocalPart();
        }
        init( nsContext );
    }

    // TODO check if this should stay here
    public void set( String text, NamespaceContext nsContext ) {
        this.text = text;
        init( nsContext );
    }

    /**
     * Returns the <a href="http://jaxen.codehaus.org/">Jaxen</a> representation of the XPath expression, which provides
     * access to the syntax tree.
     * 
     * @return the compiled expression, never <code>null</code>
     */
    public Expr getAsXPath()
                            throws FilterEvaluationException {
        return xpath;
    }

    /**
     * Returns the property name value (an XPath-expression).
     * 
     * @return the XPath property name, this may be an empty string, but never <code>null</code>
     */
    public String getAsText() {
        return text;
    }

    /**
     * If the property name is simple, the element name is returned.
     * 
     * @see #isSimple()
     * @return the qualified name value, or <code>null</code> if the property name is not simple
     */
    public QName getAsQName() {
        return qName;
    }

    /**
     * Returns the bindings for the namespaces used in the XPath expression.
     * 
     * @return the namespace bindings, never <code>null</code>
     */
    public NamespaceContext getNsContext() {
        return bindings;
    }

    /**
     * Returns whether the property name is simple, i.e. if it only contains of a single element step.
     * 
     * @return <code>true</code>, if the property is simple, <code>false</code> otherwise
     */
    public boolean isSimple() {
        return qName != null;
    }

    @Override
    public Type getType() {
        return Type.PROPERTY_NAME;
    }

    @Override
    public <T> TypedObjectNode[] evaluate( T obj, XPathEvaluator<T> xpathEvaluator )
                            throws FilterEvaluationException {
        return xpathEvaluator.eval( obj, this );
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