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
package org.deegree.graphics.sld;

import static org.deegree.framework.xml.XMLTools.escape;

import java.io.Serializable;
import java.util.ArrayList;

import org.deegree.framework.xml.Marshallable;
import org.deegree.model.feature.Feature;
import org.deegree.model.filterencoding.Expression;
import org.deegree.model.filterencoding.FilterEvaluationException;
import org.deegree.model.filterencoding.PropertyName;

/**
 * 
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class ParameterValueType implements Serializable, Marshallable {

    private static final long serialVersionUID = -2232181501775429152L;

    private ArrayList<Object> components = new ArrayList<Object>();

    /**
     * Constructs a new <tt>ParameterValueType</tt>.
     * <p>
     * 
     * @param components
     *            <tt>String</tt>s/<tt>Expression</tt> s that make up the contents of the
     *            <tt>ParameterValueType</tt>
     */
    public ParameterValueType( Object[] components ) {
        setComponents( components );
    }

    /**
     * Returns the contents (mix of <tt>String</tt>/<tt>Expression</tt> -objects) of this
     * <tt>ParameterValueType</tt>.
     * <p>
     * 
     * @return mix of <tt>String</tt>/<tt>Expression</tt> -objects
     * 
     */
    public Object[] getComponents() {
        return components.toArray( new Object[components.size()] );
    }

    /**
     * Sets the contents (mix of <tt>String</tt>/<tt>Expression</tt> -objects) of this
     * <tt>ParameterValueType</tt>.
     * <p>
     * 
     * @param components
     *            mix of <tt>String</tt> and <tt>Expression</tt> -objects
     */
    public void setComponents( Object[] components ) {
        this.components.clear();

        if ( components != null ) {
            this.components.ensureCapacity( components.length );
            for ( int i = 0; i < components.length; i++ ) {
                this.components.add( components[i] );
            }
        }
    }

    /**
     * Concatenates a component (a<tt>String</tt> or an <tt>Expression</tt> -object) to this
     * <tt>ParameterValueType</tt>.
     * <p>
     * 
     * @param component
     *            either a <tt>String</tt> or an <tt>Expression</tt> -object
     */
    public void addComponent( Object component ) {
        components.add( component );
    }

    /**
     * Removes a component (a<tt>String</tt> or an <tt>Expression</tt> -object) from this
     * <tt>ParameterValueType</tt>.
     * <p>
     * 
     * @param component
     *            either a <tt>String</tt> or an <tt>Expression</tt> -object
     */
    public void removeComponent( Object component ) {
        components.remove( components.indexOf( component ) );
    }

    /**
     * Returns the value of the ParameterValueType as an <tt>PropertyName</tt>.
     * <p>
     * 
     * @return the value of the parameter as an <tt>PropertyName</tt>
     */
    public PropertyName getValueAsPropertyName() {
        for ( Object component : components ) {
            if ( component instanceof PropertyName ) {
                return (PropertyName) component;
            }
        }
        return null;
    }

    /**
     * Returns the actual <tt>String</tt> value of this object. Expressions are evaluated according
     * to the given <tt>Feature</tt> -instance.
     * <p>
     * 
     * @param feature
     *            used for the evaluation of the underlying 'wfs:Expression'-elements
     * @return the (evaluated) String value
     * @throws FilterEvaluationException
     *             if the evaluation fails
     */
    public String evaluate( Feature feature )
                            throws FilterEvaluationException {
        StringBuffer sb = new StringBuffer();

        for ( int i = 0; i < components.size(); i++ ) {
            Object component = components.get( i );
            if ( component instanceof Expression ) {
                sb.append( ( (Expression) component ).evaluate( feature ) );
            } else if ( component != null && component instanceof String ) {
                sb.append( ( (String) component ).trim() );
            } else {
                sb.append( component );
            }
        }

        return sb.toString();
    }

    /**
     * Exports the content of the ParameterValueType as an XML formatted String.
     * 
     * @return xml representation of the ParameterValueType
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < components.size(); i++ ) {
            Object component = components.get( i );
            if ( component instanceof Expression ) {
                sb.append( ( (Expression) component ).toXML() );
            } else if ( component != null && component instanceof String ) {
                sb.append( escape( ( (String) component ).trim() ) );
            } else {
                sb.append( component );
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return exportAsXML();
    }

}
