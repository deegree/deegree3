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
package org.deegree.layer.persistence.remotewms;

import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.util.Map;

import org.deegree.commons.utils.Pair;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.featureinfo.parsing.FeatureInfoParser;
import org.deegree.layer.LayerData;
import org.deegree.protocol.wms.client.WMSClient;
import org.deegree.protocol.wms.ops.GetFeatureInfo;
import org.deegree.protocol.wms.ops.GetMap;
import org.deegree.rendering.r2d.context.RenderContext;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class RemoteWMSLayerData implements LayerData {

	private static final Logger LOG = getLogger(RemoteWMSLayerData.class);

	private GetMap gm;

	private final WMSClient client;

	private final Map<String, String> extraParams;

	private GetFeatureInfo gfi;

	private FeatureInfoParser featureInfoParser;

	public RemoteWMSLayerData(WMSClient client, GetMap gm, Map<String, String> extraParams) {
		this.client = client;
		this.gm = gm;
		this.extraParams = extraParams;
	}

	public RemoteWMSLayerData(WMSClient client, GetFeatureInfo gfi, Map<String, String> extraParams,
			FeatureInfoParser featureInfoParser) {
		this.client = client;
		this.gfi = gfi;
		this.extraParams = extraParams;
		this.featureInfoParser = featureInfoParser;
	}

	@Override
	public void render(RenderContext context) {
		try {
			Pair<BufferedImage, String> map = client.getMap(gm, extraParams, 30);
			if (map.first != null) {
				context.paintImage(map.first);
			}
		}
		catch (Throwable e) {
			e.printStackTrace();
			LOG.warn("Error when retrieving remote map: {}", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
		}
	}

	@Override
	public FeatureCollection info() {
		try {
			return client.doGetFeatureInfo(gfi, extraParams, featureInfoParser);
		}
		catch (Exception e) {
			LOG.warn("Error when retrieving remote feature info: {}", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
		}
		return new GenericFeatureCollection();
	}

}
