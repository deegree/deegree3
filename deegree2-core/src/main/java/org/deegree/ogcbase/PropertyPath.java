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
package org.deegree.ogcbase;

import java.util.List;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.NamespaceContext;

/**
 * Represents a subset of the XPath expression language as described in section 7.4.2 of the Web Feature Implementation
 * Specification 1.1.0 (but is used by other OGC specifications as well).
 * <p>
 * This specification does not require a WFS implementation to support the full XPath language. In order to keep the
 * implementation entry cost as low as possible, this specification mandates that a WFS implementation <b>must</b>
 * support the following subset of the XPath language:
 * <ol>
 * <li>A WFS implementation <b>must</b> support <i>abbreviated relative location</i> paths.</li>
 * <li>Relative location paths are composed of one or more <i>steps</i> separated by the path separator '/'.</li>
 * <li>The first step of a relative location path <b>may</b> correspond to the root element of the feature property
 * being referenced <b>or</b> to the root element of the feature type with the next step corresponding to the root
 * element of the feature property being referenced</li>
 * <li>Each subsequent step in the path <b>must</b> be composed of the abbreviated form of the <i>child::</i> axis
 * specifier and the name of the feature property encoded as the principal node type of <i>element</i>. The abbreviated
 * form of the <i>child::</i> axis specifier is to simply omit the specifier from the location step.</li>
 * <li>Each step in the path may optionally contain a predicate composed of the predicate delimiters '[' and ']' and a
 * number indicating which child of the context node is to be selected. This allows feature properties that may be
 * repeated to be specifically referenced.</li>
 * <li>The final step in a path may optionally be composed of the abbreviated form of the <i>attribute::</i> axis
 * specifier, '@', and the name of a feature property encoded as the principal node type of <i>attribute::</i>.</li>
 * </ol>
 * <p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 * @see PropertyPathStep
 */
public class PropertyPath implements Comparable<PropertyPath> {

    private List<PropertyPathStep> steps;

    /**
     * Creates a new instance of <code>PropertyPath</code> with the specified steps.
     * 
     * @param steps
     *            property path steps, may not be null
     */
    public PropertyPath( List<PropertyPathStep> steps ) {
        if ( steps.size() < 1 ) {
            throw new IllegalArgumentException( "PropertyPath must contain at least one step." );
        }
        this.steps = steps;
    }

    /**
     * Returns the namespace bindings for the prefices that are used by this property path.
     * 
     * @return the namespace bindings
     */
    public NamespaceContext getNamespaceContext() {
        NamespaceContext nsContext = new NamespaceContext();
        for ( PropertyPathStep step : steps ) {
            QualifiedName elementName = step.getPropertyName();
            if ( elementName.getPrefix() != null && elementName.getNamespace() != null ) {
                nsContext.addNamespace( elementName.getPrefix(), elementName.getNamespace() );
            }
        }
        return nsContext;
    }

    /**
     * Returns the number of steps.
     * 
     * @return the number of steps.
     */
    public int getSteps() {
        return this.steps.size();
    }

    /**
     * Setter method for steps
     * 
     * @param steps
     *            a list of {@link PropertyPathStep}s
     */
    public void setSteps( List<PropertyPathStep> steps ) {
        this.steps = steps;
    }

    /**
     * Returns the canonical string representation.
     * 
     * @return canonical string representation
     */
    public String getAsString() {
        StringBuffer sb = new StringBuffer( 500 );
        for ( int i = 0; i < steps.size(); i++ ) {
            sb.append( steps.get( i ).toString() );
            if ( i < steps.size() - 1 ) {
                sb.append( '/' );
            }
        }
        return sb.toString();
    }

    /**
     * Returns the <code>PropertyPathStep</code> at the given index.
     * 
     * @param i
     * @return the <code>PropertyPathStep</code> at the given index
     */
    public PropertyPathStep getStep( int i ) {
        return this.steps.get( i );
    }

    /**
     * Returns all steps of the <code>PropertyPath</code>.
     * 
     * @return all steps of the <code>PropertyPath</code>
     */
    public List<PropertyPathStep> getAllSteps() {
        return this.steps;
    }

    /**
     * Adds the given <code>PropertyPathStep</code> to the end of the path.
     * 
     * @param last
     *            <code>PropertyPathStep</code> to add
     */
    public void append( PropertyPathStep last ) {
        this.steps.add( last );
    }

    /**
     * Adds the given <code>PropertyPathStep</code> to the beginning of the path.
     * 
     * @param first
     *            <code>PropertyPathStep</code> to add
     */
    public void prepend( PropertyPathStep first ) {
        this.steps.add( 0, first );
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for ( PropertyPathStep step : steps ) {
            hashCode += step.hashCode();
        }
        return hashCode;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( !( obj instanceof PropertyPath ) ) {
            return false;
        }
        PropertyPath that = (PropertyPath) obj;
        if ( this.getSteps() != that.getSteps() ) {
            return false;
        }
        for ( int i = 0; i < this.getSteps(); i++ ) {
            if ( !this.getStep( i ).equals( that.getStep( i ) ) ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < getSteps(); i++ ) {
            sb.append( getStep( i ) );
            if ( i != getSteps() - 1 ) {
                sb.append( "/" );
            }
        }
        return sb.toString();
    }

    /**
     * Compares this object with the specified object for order.
     * <p>
     * TODO use really unique string representations (namespaces!) + cache them
     * 
     * @param that
     *            the PropertyPath to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    public int compareTo( PropertyPath that ) {
        return this.toString().compareTo( that.toString() );
    }
}
