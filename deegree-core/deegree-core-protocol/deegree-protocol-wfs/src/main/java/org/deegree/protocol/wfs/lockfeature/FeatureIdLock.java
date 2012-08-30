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
package org.deegree.protocol.wfs.lockfeature;

import org.deegree.protocol.wfs.getfeature.TypeName;

/**
 * The <code>FeatureIdLock</code> class represents a lock based on a featureId. To be used with {@link LockFeature}.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureIdLock implements LockOperation {

    private final String[] featureIds;

    private final TypeName[] typeNames;

    /**
     * Creates a new {@link FeatureIdLock} instance.
     * 
     * @param featureIds
     *            a String array as feature ids, must not be null
     * @param typeNames
     *            a QName, may be null
     */
    public FeatureIdLock( String[] featureIds, TypeName[] typeNames ) {
        if ( featureIds == null ) {
            throw new IllegalArgumentException();
        }
        this.featureIds = featureIds;
        this.typeNames = typeNames;
    }

    /**
     * @return the featureIds
     */
    public String[] getFeatureIds() {
        return featureIds;
    }

    /**
     * @return the typeName
     */
    public TypeName[] getTypeNames() {
        return typeNames;
    }
}
