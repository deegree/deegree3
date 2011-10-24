//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.layer;

import java.util.Map;

import org.deegree.geometry.Envelope;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class LayerQuery {

    private final Envelope envelope;

    private final int width, height;

    private final Map<String, String> parameters;

    private int x, y, featureCount;

    public LayerQuery( Envelope envelope, int width, int height, Map<String, String> parameters ) {
        this.envelope = envelope;
        this.width = width;
        this.height = height;
        this.parameters = parameters;
    }

    public LayerQuery( Envelope envelope, int width, int height, int x, int y, int featureCount,
                       Map<String, String> parameters ) {
        this.envelope = envelope;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.featureCount = featureCount;
        this.parameters = parameters;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getFeatureCount() {
        return featureCount;
    }

}
