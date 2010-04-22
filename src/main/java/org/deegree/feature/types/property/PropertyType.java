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
package org.deegree.feature.types.property;

import javax.xml.namespace.QName;

import org.deegree.feature.types.FeatureType;

/**
 * Declares a named property of a {@link FeatureType}.
 * 
 * @see FeatureType
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface PropertyType {

    /**
     * Returns the name of the property.
     * 
     * @return the name of the property
     */
    public QName getName();

    /**
     * Specifies the minimum number of times that this property must be present in a feature instance.
     * 
     * @return the minimum number of times that this property must be present
     */
    public int getMinOccurs();

    /**
     * Specifies the maximum number of times that this property must be present in a feature instance.
     * 
     * @return the maximum number of times that this property must be present, or -1 (=unbounded)
     */
    public int getMaxOccurs();

    /**
     * Returns whether this {@link PropertyType} declaration is abstract.
     * 
     * @return true, if is abstract, false otherwise
     */
    public boolean isAbstract();

    /**
     * Returns the possible substitutions that are defined for this {@link PropertyType}.
     * 
     * NOTE: This is only needed for a number of GML application schemas (e.g. CityGML) that define properties using
     * abstract element declarations and provide multiple concrete substitutable elements.
     * 
     * @return the possible substitutions (including this {@link PropertyType}), never <code>null</code> and always at
     *         least one entry
     */
    public PropertyType[] getSubstitutions();

    /**
     * Returns whether this {@link PropertyType} declaration allows for setting the <code>xsi:nil="true"</code>
     * attribute in a GML representation.
     * 
     * @return true, if code>xsi:nil="true"</code> is permitted, false otherwise
     */
    public boolean isNillable();
}
