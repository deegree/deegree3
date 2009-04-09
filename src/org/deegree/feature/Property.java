//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.feature;

import javax.xml.namespace.QName;

import org.deegree.feature.types.property.PropertyType;

/**
 * A spatial or non-spatial property of a {@link Feature}.
 * <p>
 * Basically, it has a (qualified) name and a value of a certain type.
 * 
 * @see Feature
 * 
 * @param <T>
 *            The class of the <code>Property</code>'s value.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface Property<T> {

    /**
     * Returns the name of the property.
     * <p>
     * In a canonical GML representation, this corresponds to the property's element name in the declaration. However,
     * there are some GML application schemas (e.g. CityGML) that define the property using an abstract element and
     * provide multiple concrete substitutable elements. In these cases, the name of a property is not equal to the name
     * of the property type.
     * </p>
     * 
     * @return the name of the property
     */
    public QName getName();

    /**
     * Returns the type information for this property.
     * 
     * @return the type information
     */
    public PropertyType getType();

    /**
     * Returns the value of this property.
     * 
     * @return the value of this property
     */
    public T getValue();
}
