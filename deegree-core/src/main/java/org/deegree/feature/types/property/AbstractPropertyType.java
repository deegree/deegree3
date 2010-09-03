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

import java.util.List;

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
     * The possible substitutions (including this {@link PropertyType}), never <code>null</code> and always at least one
     * entry.
     */
    protected PropertyType[] substitutions;

    private boolean isAbstract;

    /**
     * Creates a new <code>AbstractPropertyType</code> instance.
     * 
     * @param name
     *            name of the property
     * @param minOccurs
     *            minimum number of times that this property must be present
     * @param maxOccurs
     *            maximum number of times that this property must be present, or -1 (=unbounded)
     * @param isAbstract
     *            true, if it is abstract, false otherwise
     * @param substitutions
     *            the possible concrete substitutions, can be <code>null</code>
     */
    protected AbstractPropertyType( QName name, int minOccurs, int maxOccurs, boolean isAbstract,
                                    List<PropertyType> substitutions ) {
        this.name = name;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
        this.isAbstract = isAbstract;
        if ( substitutions != null ) {
            substitutions.add( this );
            this.substitutions = substitutions.toArray( new PropertyType[substitutions.size()] );
        } else {
            this.substitutions = new PropertyType[] { this };
        }
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

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public PropertyType[] getSubstitutions() {
        return substitutions;
    }
}
