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

package org.deegree.services.wms.model.layers;

import java.awt.Graphics2D;
import java.util.LinkedList;

import org.deegree.commons.utils.Pair;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.FeatureType;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.services.jaxb.wms.AbstractLayerType;
import org.deegree.services.wms.controller.ops.GetFeatureInfo;
import org.deegree.services.wms.controller.ops.GetMap;

/**
 * <code>EmptyLayer</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class EmptyLayer extends Layer {

    /**
     * @param name
     * @param title
     * @param parent
     */
    public EmptyLayer( String name, String title, Layer parent ) {
        super( name, title, parent );
    }

    /**
     * @param layer
     * @param parent
     */
    public EmptyLayer( AbstractLayerType layer, Layer parent ) {
        super( layer, parent );
    }

    @Override
    public LinkedList<String> paintMap( Graphics2D g, GetMap gm, Style style ) {
        return new LinkedList<String>();
    }

    @Override
    public Pair<FeatureCollection, LinkedList<String>> getFeatures( GetFeatureInfo fi, Style style ) {
        return new Pair<FeatureCollection, LinkedList<String>>( null, new LinkedList<String>() );
    }

    @Override
    public FeatureType getFeatureType() {
        return null;
    }

    @Override
    public String toString() {
        return getName();
    }

}
