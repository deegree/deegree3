/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH
 and
 grit graphische Informationstechnik Beratungsgesellschaft mbH

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.feature.persistence;

import org.deegree.feature.Feature;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryInspectionException;

/**
 * Enables the inspection of {@link Feature} instances to be inserted in a {@link FeatureStoreTransaction}.
 * <p>
 * NOTE: Implementations must be thread-safe.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public interface FeatureInspector {

    /**
     * Invokes the inspection of the given {@link Feature}.
     * 
     * @param f
     *            feature to be inspected, never <code>null</code>
     * @return inspected feature, may be a different or modified instance
     * @throws GeometryInspectionException
     *             if the inspector rejects the {@link Geometry}
     */
    public Feature inspect( Feature f );

    /**
     * Invokes the inspection of the given {@link Feature}.
     * 
     * @param f
     *            feature to be inspected, never <code>null</code>
     * @param transaction
     *            transaction if feature is inspected in context of a {@link FeatureStoreTransaction}, may be
     *            <code>null</null>
     * @return inspected feature, may be a different or modified instance
     * @throws GeometryInspectionException
     *             if the inspector rejects the {@link Geometry}
     */
    default public Feature inspect( Feature f, FeatureStoreTransaction transaction ) {
        // default implementation to be compatible to previous inspectors
        return inspect( f );
    }
}
