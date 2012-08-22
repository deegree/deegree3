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

package org.deegree.model.filterencoding;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.OGCDocument;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Encapsulates the information of a PropertyName element.
 *
 * @author Markus Schneider
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class PropertyName extends Expression {

    /** the PropertyName's value (as an XPATH expression). */
    private PropertyPath propertyPath;

    /**
     * Creates a new instance of <code>PropertyName</code>.
     *
     * @param elementName
     */
    public PropertyName( QualifiedName elementName ) {
        this( PropertyPathFactory.createPropertyPath( elementName ) );
    }

    /**
     * Creates a new instance of <code>PropertyName</code>.
     *
     * @param value
     */
    public PropertyName( PropertyPath value ) {
        id = ExpressionDefines.PROPERTYNAME;
        setValue( value );
    }

    /**
     * Given a DOM-fragment, a corresponding Expression-object is built.
     *
     * @param element
     * @return the Expression object for the passed element
     * @throws FilterConstructionException
     *             if the structure of the DOM-fragment is invalid
     */
    public static Expression buildFromDOM( Element element )
                            throws FilterConstructionException {
        // check if root element's name equals 'PropertyName'
        if ( !element.getLocalName().toLowerCase().equals( "propertyname" ) ) {
            throw new FilterConstructionException( "Name of element does not equal " + "'PropertyName'!" );
        }
        PropertyPath propertyPath;
        try {
            Text node = (Text) XMLTools.getRequiredNode( element, "text()", CommonNamespaces.getNamespaceContext() );
            propertyPath = OGCDocument.parsePropertyPath( node );
        } catch ( XMLParsingException e ) {
            throw new FilterConstructionException( e.getMessage() );
        }
        return new PropertyName( propertyPath );
    }

    /**
     * Returns the PropertyName's value.
     *
     * @return the PropertyName's value.
     */
    public PropertyPath getValue() {
        return this.propertyPath;
    }

    /**
     * @param value
     * @see org.deegree.model.filterencoding.PropertyName#getValue()
     */
    public void setValue( PropertyPath value ) {
        this.propertyPath = value;
    }

    /**
     * Produces an indented XML representation of this object.
     */
    @Override
    public StringBuffer toXML() {
        StringBuffer sb = new StringBuffer( 200 );
        sb.append( "<ogc:PropertyName" );

        // TODO use methods from XMLTools
        Map<String, URI> namespaceMap = this.propertyPath.getNamespaceContext().getNamespaceMap();
        Iterator<String> prefixIter = namespaceMap.keySet().iterator();
        while ( prefixIter.hasNext() ) {
            String prefix = prefixIter.next();
            if ( !CommonNamespaces.XMLNS_PREFIX.equals( prefix ) ) {
                URI namespace = namespaceMap.get( prefix );
                sb.append( " xmlns:" );
                sb.append( prefix );
                sb.append( "=" );
                sb.append( "\"" );
                sb.append( namespace );
                sb.append( "\"" );
            }
        }
        sb.append( ">" ).append( propertyPath ).append( "</ogc:PropertyName>" );
        return sb;
    }

    /**
     * Returns the <tt>PropertyName</tt>'s value (to be used in the evaluation of a complexer
     * <tt>Expression</tt>). If the value is a geometry, an instance of <tt>Geometry</tt> is
     * returned, if it appears to be numerical, a <tt>Double</tt>, else a <tt>String</tt>.
     * <p>
     * TODO: Improve datatype handling.
     * <p>
     *
     * @param feature
     *            that determines the value of this <tt>PropertyName</tt>
     * @return the resulting value
     * @throws FilterEvaluationException
     *             if the <Feature>has no <tt>Property</tt> with a matching name
     */
    @Override
    public Object evaluate( Feature feature )
                            throws FilterEvaluationException {

        if ( feature == null ) {
            throw new FilterEvaluationException( "Trying to evaluate an expression that depends "
                                                 + "on a property without a feature!" );
        }

        FeatureProperty property = null;
        try {
            property = feature.getDefaultProperty( this.propertyPath );
        } catch ( PropertyPathResolvingException e ) {
            e.printStackTrace();
            throw new FilterEvaluationException( e.getMessage() );
        }
        FeatureType ft = feature.getFeatureType();
        if ( property == null && ft.getProperty( this.propertyPath.getStep( 0 ).getPropertyName() ) == null ) {
            throw new FilterEvaluationException( "Feature '" + feature.getFeatureType().getName()
                                                 + "' has no property identified by '" + propertyPath + "'!" );
        }

        if ( property == null || property.getValue() == null ) {
            return null;
        }
        Object object = property.getValue();
        if ( object instanceof Number || object instanceof Geometry ) {
            return object;
        }
        return object.toString();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @return <code>true</code> if this object is the same as the obj argument;
     *         <code>false</code> otherwise
     */
    @Override
    public boolean equals( Object other ) {
        if ( other == null || !( other instanceof PropertyName ) ) {
            return false;
        }
        return propertyPath.equals( ( (PropertyName) other ).getValue() );
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return this.propertyPath.getAsString();
    }
}
