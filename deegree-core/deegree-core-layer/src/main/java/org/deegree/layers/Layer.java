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

package org.deegree.layers;

import java.awt.image.renderable.RenderContext;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.utils.Pair;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.wms.WMSException.InvalidDimensionValue;
import org.deegree.protocol.wms.WMSException.MissingDimensionValue;
import org.deegree.rendering.r2d.se.unevaluated.Style;

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
     * @return the layer envelope, may be null, may be in any CRS
     */
    Envelope getEnvelope();

    /**
     * @param envelope
     */
    void setEnvelope( Envelope envelope );

    /**
     * @return the parent, null for the root layer
     */
    Layer getParent();

    /**
     * @param parent
     *            may be null
     */
    void setParent( Layer parent );

    /**
     * @return never null
     */
    List<Layer> getChildren();

    /**
     * @param children
     *            may be null
     */
    void setChildren( List<Layer> children );

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
    LinkedList<String> paintMap( RenderContext context, RenderingInfo info, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue;

    /**
     * @param info
     * @param style
     * @return a collection of matching features and a list of warning headers (currently only used for dimension
     *         warnings). Return an empty collection if feature info is not supported
     * @throws MissingDimensionValue
     * @throws InvalidDimensionValue
     */
    Pair<FeatureCollection, LinkedList<String>> getFeatures( RenderingInfo info, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue;

    /**
     * @return the feature type, may be null if feature info is not supported
     */
    FeatureType getFeatureType();

}
