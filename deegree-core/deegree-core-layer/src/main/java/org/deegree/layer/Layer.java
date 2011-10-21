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

package org.deegree.layer;

import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.FeatureType;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.protocol.oldwms.WMSException.InvalidDimensionValue;
import org.deegree.protocol.oldwms.WMSException.MissingDimensionValue;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.rendering.r2d.context.RenderingInfo;
import org.deegree.style.se.unevaluated.Style;

/**
 * <code>Layer</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface Layer {

    /**
     * @return the layer metadata
     */
    LayerMetadata getMetadata();

    /**
     * Method to paint on a graphics object.
     * 
     * @param context
     * @param info
     * @param style
     * @return a list of warning headers (currently only used for dimension warnings)
     * @throws MissingDimensionValue
     * @throws InvalidDimensionValue
     */
    void paintMap( RenderContext context, RenderingInfo info, Style style );

    /**
     * @param info
     * @param style
     * @return a collection of matching features and a list of warning headers (currently only used for dimension
     *         warnings). Return an empty collection if feature info is not supported
     * @throws MissingDimensionValue
     * @throws InvalidDimensionValue
     */
    FeatureCollection getFeatures( RenderingInfo info, Style style );

    /**
     * @return the feature type, may be null if feature info is not supported
     */
    FeatureType getFeatureType();

}
