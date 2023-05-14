/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wms;

import static org.deegree.rendering.r2d.context.MapOptions.Antialias.BOTH;
import static org.deegree.rendering.r2d.context.MapOptions.Interpolation.NEARESTNEIGHBOR;
import static org.deegree.rendering.r2d.context.MapOptions.Quality.NORMAL;
import static org.slf4j.LoggerFactory.getLogger;

import org.deegree.rendering.r2d.context.MapOptions;
import org.deegree.rendering.r2d.context.MapOptions.Antialias;
import org.deegree.rendering.r2d.context.MapOptions.Interpolation;
import org.deegree.rendering.r2d.context.MapOptions.Quality;
import org.deegree.services.jaxb.wms.LayerOptionsType;
import org.deegree.services.jaxb.wms.ServiceConfigurationType;
import org.slf4j.Logger;

/**
 * Builds map service components from jaxb config beans.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class MapServiceBuilder {

	private static final Logger LOG = getLogger(MapServiceBuilder.class);

	private ServiceConfigurationType conf;

	private Antialias alias;

	private Quality quali;

	private Interpolation interpol;

	MapServiceBuilder(ServiceConfigurationType conf) {
		this.conf = conf;
		alias = Antialias.BOTH;
		quali = Quality.HIGH;
		interpol = Interpolation.NEARESTNEIGHBOUR;
	}

	MapOptions buildMapOptions() {
		int maxFeatures = 10000;
		int featureInfoRadius = 1;
		if (conf != null) {
			LayerOptionsType sf = conf.getDefaultLayerOptions();
			alias = handleDefaultValue(sf == null ? null : sf.getAntiAliasing(), Antialias.class, BOTH);
			quali = handleDefaultValue(sf == null ? null : sf.getRenderingQuality(), Quality.class, NORMAL);
			interpol = handleDefaultValue(sf == null ? null : sf.getInterpolation(), Interpolation.class,
					NEARESTNEIGHBOR);
			if (sf != null && sf.getMaxFeatures() != null) {
				maxFeatures = sf.getMaxFeatures();
				LOG.debug("Using global max features setting of {}.", maxFeatures);
			}
			else {
				LOG.debug("Using default global max features setting of {}, set it to -1 if you don't want a limit.",
						maxFeatures);
			}
			if (sf != null && sf.getFeatureInfoRadius() != null) {
				featureInfoRadius = sf.getFeatureInfoRadius();
				LOG.debug("Using global feature info radius setting of {}.", featureInfoRadius);
			}
			else {
				LOG.debug("Using default feature info radius of {}.", featureInfoRadius);
			}
			return new MapOptions.Builder().quality(quali)
				.interpolation(interpol)
				.antialias(alias)
				.maxFeatures(maxFeatures)
				.featureInfoRadius(featureInfoRadius)
				.build();
		}
		return null;
	}

	private static <T extends Enum<T>> T handleDefaultValue(String val, Class<T> enumType, T defaultValue) {
		if (val == null) {
			return defaultValue;
		}
		try {
			return Enum.valueOf(enumType, val.toUpperCase());
		}
		catch (IllegalArgumentException e) {
			LOG.warn("'{}' is not a valid value for '{}'. Using default value '{}' instead.",
					new Object[] { val, enumType.getSimpleName(), defaultValue });
			return defaultValue;
		}
	}

}
