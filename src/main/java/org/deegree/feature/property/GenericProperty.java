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
package org.deegree.feature.property;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GenericProperty implements Property {

    private PropertyType declaration;

    private QName name;

    private TypedObjectNode value;

    private boolean nilled;

    /**
     * Creates a new {@link GenericProperty} instance.
     * 
     * @param declaration
     *            type information
     * @param value
     *            property value, can be <code>null</code>
     */
    public GenericProperty( PropertyType declaration, TypedObjectNode value ) {
        this( declaration, null, value );
    }

    /**
     * Creates a new {@link GenericProperty} instance.
     * 
     * @param declaration
     *            type information
     * @param name
     *            name of the property (does not necessarily match the name in the type information)
     * @param value
     *            property value, can be <code>null</code>
     */
    public GenericProperty( PropertyType declaration, QName name, TypedObjectNode value ) {
        this.declaration = declaration;
        this.name = name;
        if ( name == null ) {
            this.name = declaration.getName();
        }
        this.value = value;

        if ( declaration instanceof SimplePropertyType ) {
            if ( value != null && !( value instanceof PrimitiveValue ) ) {
                // TODO do more fine grained type checks
                String msg = "Invalid simple property (PrimitiveType="
                             + ( (SimplePropertyType) declaration ).getPrimitiveType().name() + "): required class="
                             + ( (SimplePropertyType) declaration ).getPrimitiveType().getValueClass()
                             + ", but given '" + value.getClass() + ".";
                throw new IllegalArgumentException( msg );
            }
        }
    }

    /**
     * Creates a new {@link GenericProperty} instance.
     * 
     * @param declaration
     *            type information
     * @param name
     *            name of the property (does not necessarily match the name in the type information)
     * @param value
     *            property value, can be <code>null</code>
     * @param isNilled
     *            true, if the property is explicitly nilled, false otherwise
     */
    public GenericProperty( PropertyType declaration, QName name, TypedObjectNode value, boolean isNilled ) {
        this (declaration, name, value);
        this.nilled = isNilled;
    }
    
    @Override
    public QName getName() {
        return name;
    }

    @Override
    public boolean isNilled() {
        return nilled;
    }

    @Override
    public TypedObjectNode getValue() {
        return value;
    }

    @Override
    public PropertyType getType() {
        return declaration;
    }

    @Override
    public String toString() {
        return value == null ? "null" : value.toString();
    }
}
