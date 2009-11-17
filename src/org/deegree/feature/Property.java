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
 * A spatial or non-spatial property of a {@link Feature}.
 * <p>
 * Encapsulates a (qualified) name, type information and a value of a certain type.
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
     * there are some GML application schemas (e.g. CityGML) that define properties using abstract element declarations
     * and provide multiple concrete substitutable elements. In these cases, the name of a property is not equal to the
     * name of the property type.
     * </p>
     * 
     * @return the name of the property, never <code>null</code>
     */
    public QName getName();

    /**
     * Returns the type information for this property.
     * 
     * @return the type information, never <code>null</code>
     */
    public PropertyType<T> getType();

    /**
     * Returns the value of this property.
     * 
     * @return the value of this property, or <code>null</code> if it is nilled
     */
    public T getValue();
}
