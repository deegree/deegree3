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

package org.deegree.services.controller.wpvs.getview;

import java.awt.Color;
import java.util.List;

import org.deegree.commons.utils.SunInfo;

/**
 * The <code>GetViewSceneParameters</code> class wraps scene parameters of a GetView request.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 *
 */
public class GetViewSceneParameters {

    private final String elevationModel;

    private final List<String> datasets;

    private final Color bgColor;

    private final String skyImage;

    private final String date;

    private final float scale;

    private final SunInfo sunPosition;

    /**
     * @param scale
     * @param elevationModel
     * @param datasets
     * @param bgColor
     * @param skyImage
     * @param date
     * @param position
     */
    public GetViewSceneParameters( float scale, String elevationModel, List<String> datasets, Color bgColor,
                                   String skyImage, String date, SunInfo position ) {
        this.scale = scale;
        this.elevationModel = elevationModel;
        this.datasets = datasets;
        this.bgColor = bgColor;
        this.skyImage = skyImage;
        this.date = date;
        this.sunPosition = position;
    }

    /**
     * @return the elevationModel
     */
    public final String getElevationModel() {
        return elevationModel;
    }

    /**
     * @return the datasets
     */
    public final List<String> getDatasets() {
        return datasets;
    }

    /**
     * @return the bgColor
     */
    public final Color getBgColor() {
        return bgColor;
    }

    /**
     * @return the skyImage
     */
    public final String getSkyImage() {
        return skyImage;
    }

    /**
     * @return the date
     */
    public final String getDate() {
        return date;
    }

    /**
     * @return the scale
     */
    public final float getScale() {
        return scale;
    }

    /**
     * @return the sunPosition created from the date object.
     */
    public final SunInfo getSunPosition() {
        return sunPosition;
    }

}
