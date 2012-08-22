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

package org.deegree.model.feature;

import java.io.Serializable;

import org.deegree.datatypes.QualifiedName;

/**
 *
 * the interface describes a property entry of a feature. It is made of a name and a value
 * associated to it.
 *
 * <p>
 * ---------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$ $Date$
 */

class DefaultFeatureProperty implements FeatureProperty, Serializable {

    private static final long serialVersionUID = -4265511951750096466L;

    private Object value = null;

    private QualifiedName name = null;

    /**
     * constructor for complete initializing the FeatureProperty
     *
     * @param name
     *            qualified name of the property
     * @param value
     *            the properties value
     */
    DefaultFeatureProperty( QualifiedName name, Object value ) {
        setValue( value );
        this.name = name;
    }

    /**
     * returns the qualified name of the property
     */
    public QualifiedName getName() {
        return name;
    }

    /**
     * returns the value of the property
     */
    public Object getValue() {
        return value;
    }

    /**
     * returns the value of the property; if the value is null the passed defaultValuewill be
     * returned
     *
     * @param defaultValue
     */
    public Object getValue( Object defaultValue ) {
        if ( value == null ) {
            return defaultValue;
        }
        return value;
    }

    /**
     * sets the value of the property
     */
    public void setValue( Object value ) {
        this.value = value;
    }

    @Override
    public String toString() {
        String ret = null;
        ret = "name = " + name + "\n";
        ret += "value = " + value + "\n";
        return ret;
    }

    public Feature getOwner() {
        return null;
    }

}
