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
package org.deegree.model.feature.schema;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;

/**
 * Represents a property type in a GML feature type definition.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public interface PropertyType {

    /**
     * Returns the name of the property.
     *
     * @return the name of the property
     */
    public QualifiedName getName();

    /**
     * Returns the code of the data type that the property contains.
     *
     * @see Types
     * @return the code of the data type that the property contains
     */
    public int getType();

    /**
     * Returns the minimum number of occurrences of the property within a feature. The method
     * returns 0 if the property is nillable.
     *
     * @return minimum number of occurrences of the property, 0 if the property is nillable
     */
    int getMinOccurs();

    /**
     * Returns the maximum number of occurrences of the property within a feature. The method
     * returns -1 if the number of occurences is unbounded.
     *
     * @return maximum number of occurrences of the property, -1 if the number of occurences is
     *         unbounded
     */
    int getMaxOccurs();

}
