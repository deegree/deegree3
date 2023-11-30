/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2023 by:
 - Department of Geography, University of Bonn -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 https://www.grit.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wms.controller.plugins;

import java.io.IOException;
import java.util.List;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.feature.FeatureCollection;
import org.deegree.protocol.wms.ops.GetFeatureInfo;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.controller.WMSController;

public interface GetFeatureInfoProvider {

	/**
	 * @param wmsController Controller handling the request
	 * @param service MapService providing the data
	 * @param fi Info request
	 * @param queryLayers Original layer in map request
	 * @param headers may not be null. Extra HTTP headers will be added, as required by
	 * the WMS spec.
	 * @return a FeatureCollection from the requested service
	 */
	FeatureCollection query(WMSController wmsController, MapService service, GetFeatureInfo fi,
			List<String> queryLayers, List<String> headers) throws OWSException, IOException;

}
