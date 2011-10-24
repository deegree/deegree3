//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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

 Occam Labs Schmitz & Schneider GbR
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.layer.persistence.feature;

import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.feature.persistence.FeatureStore;
import org.deegree.filter.OperatorFilter;
import org.deegree.layer.AbstractLayer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.rendering.r2d.context.RenderingInfo;
import org.deegree.style.se.unevaluated.Style;

/**
 * @author stranger
 * 
 */
public class FeatureLayer extends AbstractLayer {

    private FeatureStore featureStore;

    private OperatorFilter filter;

    private final QName featureType;

    /**
     * @param md
     */
    protected FeatureLayer( LayerMetadata md, FeatureStore featureStore, QName featureType, OperatorFilter filter,
                            Map<String, Style> styles, Map<String, Style> legendStyles ) {
        super( md );
        this.featureStore = featureStore;
        this.featureType = featureType;
        this.filter = filter;
    }

    @Override
    public void paintMap( RenderContext context, RenderingInfo info, Style style ) {
    }

}
