//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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
package org.deegree.model.feature.types;

import javax.xml.namespace.QName;

/**
 * Abstract base class for {@link PropertyType}s that defines common fields and methods.
 * <p>
 * Common to all {@link PropertyType}s are the following:
 * <ul>
 * <li>A (qualified) name</li>
 * <li>Minimum number of times that a property must be present in a corresponding feature instance (minOccurs)</li>
 * <li>Maximum number of times that a property must be present in a corresponding feature instance (maxOccurs)</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public abstract class AbstractPropertyType implements PropertyType {

    /** The name of the property. */
    protected QName name;

    /** The minimum number of times that this property must be present. */
    protected int minOccurs;

    /** The maximum number of times that this property must be present, or -1 (=unbounded). */
    protected int maxOccurs;

    /**
     * Creates a new <code>AbstractPropertyType</code> instance.
     * 
     * @param name
     *            name of the property
     * @param minOccurs
     *            minimum number of times that this property must be present
     * @param maxOccurs
     *            maximum number of times that this property must be present, or -1 (=unbounded)
     */
    protected AbstractPropertyType( QName name, int minOccurs, int maxOccurs ) {
        this.name = name;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
    }

    @Override
    public QName getName() {
        return name;
    }

    @Override
    public int getMinOccurs() {
        return minOccurs;
    }

    @Override
    public int getMaxOccurs() {
        return maxOccurs;
    }
}
