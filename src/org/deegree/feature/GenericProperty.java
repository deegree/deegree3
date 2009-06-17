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
package org.deegree.feature;

import javax.xml.namespace.QName;

import org.deegree.feature.types.property.PropertyType;

/**
 * TODO add documentation here
 *
 * @param <T>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class GenericProperty<T> implements Property<T> {

    private PropertyType declaration;

    private QName name;

    private T value;

    /**
     * Creates a new {@link GenericProperty} instance.
     *
     * @param declaration
     *            type information
     * @param name
     *            name of the property (does not necessarily match the name in the type information)
     * @param value
     *            property value
     */
    public GenericProperty( PropertyType declaration, QName name, T value ) {
        this.declaration = declaration;
        this.name = name;
        this.value = value;
    }

    /**
     * Creates a new {@link GenericProperty} instance.
     *
     * @param declaration
     * @param value
     */
    public GenericProperty( PropertyType declaration, T value ) {
        this( declaration, declaration.getName(), value );
    }

    @Override
    public QName getName() {
        return name;
    }

    @Override
    public T getValue() {
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
