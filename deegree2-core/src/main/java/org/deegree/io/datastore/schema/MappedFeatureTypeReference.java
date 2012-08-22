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
package org.deegree.io.datastore.schema;

import org.deegree.datatypes.QualifiedName;

/**
 * Represents a reference to a {@link MappedFeatureType}.
 * <p>
 * The reference may be resolved or not. If it is resolved, the referenced
 * {@link MappedFeatureType} is accessible, otherwise only the name of the type is available.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MappedFeatureTypeReference {

    private QualifiedName featureTypeName;

    private MappedFeatureType featureType;

    private boolean isResolved;

    /**
     * Creates an unresolved <code>MappedFeatureTypeReference</code>.
     *
     * @param featureTypeName
     */
    public MappedFeatureTypeReference( QualifiedName featureTypeName ) {
        this.featureTypeName = featureTypeName;
    }

    /**
     * Returns the name of the referenced {@link MappedFeatureType}.
     *
     * @return the name of the referenced feature type
     */
    public QualifiedName getName() {
        return this.featureTypeName;
    }

    /**
     * Returns true, if the reference has been resolved.
     * <p>
     * If this method returns true, {@link #getFeatureType()} will return the correct
     * {@link MappedFeatureType} instance.
     *
     * @return true, if the reference has been resolved, false otherwise
     */
    public boolean isResolved() {
        return this.isResolved;
    }

    /**
     * Returns the referenced {@link MappedFeatureType}.
     * <p>
     * This method will only return the correct {@link MappedFeatureType} instance, if
     * the reference has been resolved by a call to {@link #resolve(MappedFeatureType)}.
     *
     * @return the referenced feature type, or null if it has not been resolved
     */
    public MappedFeatureType getFeatureType() {
        return this.featureType;
    }

    /**
     * Sets the referenced {@link MappedFeatureType} instance.
     *
     * @param featureType
     * @throws RuntimeException
     *             if the reference has been resolved already
     */
    public void resolve( MappedFeatureType featureType ) {
        if ( isResolved() ) {
            throw new RuntimeException( "MappedFeatureTypeReference to feature type '"
                + featureTypeName + "' has already been resolved." );
        }
        this.featureType = featureType;
        this.isResolved = true;
    }
}
