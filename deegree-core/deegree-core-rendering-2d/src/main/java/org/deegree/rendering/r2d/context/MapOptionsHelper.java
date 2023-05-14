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
package org.deegree.rendering.r2d.context;

/**
 * Helper methods to work with map options.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class MapOptionsHelper {

	public static void insertMissingOptions(String layer, MapOptionsMaps options, MapOptions layerDefaults,
			MapOptions globalDefaults) {
		insertAntialias(layer, options, layerDefaults, globalDefaults);
		insertQuality(layer, options, layerDefaults, globalDefaults);
		insertInterpolation(layer, options, layerDefaults, globalDefaults);
		insertMaxFeatures(layer, options, layerDefaults, globalDefaults);
		insertRadius(layer, options, layerDefaults, globalDefaults);
	}

	private static void insertRadius(String layer, MapOptionsMaps options, MapOptions layerDefaults,
			MapOptions globalDefaults) {
		if (options.getFeatureInfoRadius(layer) == -1) {
			if (layerDefaults != null) {
				options.setFeatureInfoRadius(layer, layerDefaults.getFeatureInfoRadius());
			}
			if (options.getFeatureInfoRadius(layer) == -1) {
				options.setFeatureInfoRadius(layer, globalDefaults.getFeatureInfoRadius());
			}
		}
	}

	private static void insertMaxFeatures(String layer, MapOptionsMaps options, MapOptions layerDefaults,
			MapOptions globalDefaults) {
		if (options.getMaxFeatures(layer) == -1) {
			if (layerDefaults != null) {
				options.setMaxFeatures(layer, layerDefaults.getMaxFeatures());
			}
			if (options.getMaxFeatures(layer) == -1) {
				options.setMaxFeatures(layer, globalDefaults.getMaxFeatures());
			}
		}
	}

	private static void insertInterpolation(String layer, MapOptionsMaps options, MapOptions layerDefaults,
			MapOptions globalDefaults) {
		if (options.getInterpolation(layer) == null) {
			if (layerDefaults != null) {
				options.setInterpolation(layer, layerDefaults.getInterpolation());
			}
			if (options.getInterpolation(layer) == null) {
				options.setInterpolation(layer, globalDefaults.getInterpolation());
			}
		}
	}

	private static void insertQuality(String layer, MapOptionsMaps options, MapOptions layerDefaults,
			MapOptions globalDefaults) {
		if (options.getQuality(layer) == null) {
			if (layerDefaults != null) {
				options.setQuality(layer, layerDefaults.getQuality());
			}
			if (options.getQuality(layer) == null) {
				options.setQuality(layer, globalDefaults.getQuality());
			}
		}
	}

	private static void insertAntialias(String layer, MapOptionsMaps options, MapOptions layerDefaults,
			MapOptions globalDefaults) {
		if (options.getAntialias(layer) == null) {
			if (layerDefaults != null) {
				options.setAntialias(layer, layerDefaults.getAntialias());
			}
			if (options.getAntialias(layer) == null) {
				options.setAntialias(layer, globalDefaults.getAntialias());
			}
		}
	}

}
