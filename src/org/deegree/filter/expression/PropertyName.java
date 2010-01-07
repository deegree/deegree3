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

import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchableObject;
import org.deegree.gml.GMLVersion;
import org.jaxen.JaxenException;
import org.jaxen.NamespaceContext;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class PropertyName implements Expression {

    private String xPath;

    private NamespaceContext nsContext;

    private QName simpleProp;

    private Boolean isSimple;

    /**
     * Creates a new {@link PropertyName} instance from an XPath-expression and the namespace bindings.
     * 
     * @param xPath
     *            XPath-expression, this may be the empty string, but never <code>null</code>
     * @param nsContext
     *            binding of the namespaces used in the XPath expression
     */
    public PropertyName( String xPath, NamespaceContext nsContext ) {
        this.xPath = xPath;
        this.nsContext = nsContext;
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
            this.xPath = prefix + ":" + name.getLocalPart();
        } else {
            this.xPath = name.getLocalPart();
        }
    }

    /**
     * Returns the property name value (an XPath-expression).
     * 
     * @return the XPath property name, this may be an empty string, but never <code>null</code>
     */
    public String getPropertyName() {
        return xPath;
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
            isSimple = !xPath.contains( "@" ) && !xPath.contains( "/" ) && !xPath.contains( "[" )
                       && !xPath.contains( "*" ) && !xPath.contains( "::" ) && !xPath.contains( "(" )
                       && !xPath.contains( "=" );
        }
        return isSimple;
    }

    /**
     * If the property name is simple, the element name is returned.
     * 
     * @see #isSimple()
     * @return the qualified name value, or <code>null</code> if the property name is not simple
     */
    public QName getAsQName() {
        if ( simpleProp == null && isSimple() ) {
            int colonIdx = xPath.indexOf( ":" );
            if ( colonIdx == -1 ) {
                simpleProp = new QName( xPath );
            } else {
                String prefix = xPath.substring( 0, colonIdx );
                String localPart = xPath.substring( colonIdx + 1 );
                String namespace = nsContext.translateNamespacePrefixToUri( prefix );
                simpleProp = new QName( namespace, localPart );
            }
        }
        return simpleProp;
    }

    @Override
    public Type getType() {
        return Type.PROPERTY_NAME;
    }

    @Override
    public Object[] evaluate( MatchableObject obj )
                            throws FilterEvaluationException {
        Object[] values;
        try {
            values = obj.getPropertyValues( this, GMLVersion.GML_31 );
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
        String s = indent + "-PropertyName ('" + xPath + "')\n";
        return s;
    }

    @Override
    public Expression[] getParams() {
        return new Expression[0];
    }
}
