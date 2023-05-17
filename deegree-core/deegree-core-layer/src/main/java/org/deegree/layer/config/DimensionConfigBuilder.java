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
package org.deegree.layer.config;

import static org.deegree.layer.dims.Dimension.parseTyped;
import static org.slf4j.LoggerFactory.getLogger;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.deegree.layer.dims.Dimension;
import org.deegree.layer.dims.DimensionsLexer;
import org.deegree.layer.dims.DimensionsParser;
import org.deegree.layer.persistence.base.jaxb.DimensionType;
import org.slf4j.Logger;

/**
 * Builds dimensions map from jaxb configuration.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class DimensionConfigBuilder {

	private static final Logger LOG = getLogger(DimensionConfigBuilder.class);

	public static Map<String, Dimension<?>> parseDimensions(String layerName, List<DimensionType> dimensions) {
		Map<String, Dimension<?>> map = new LinkedHashMap<String, Dimension<?>>();
		for (DimensionType type : dimensions) {
			DimensionsLexer lexer = new DimensionsLexer(new ANTLRStringStream(type.getExtent()));
			DimensionsParser parser = new DimensionsParser(new CommonTokenStream(lexer));
			DimensionsParser defaultParser = null;
			if (type.getDefaultValue() != null) {
				lexer = new DimensionsLexer(new ANTLRStringStream(type.getDefaultValue()));
				defaultParser = new DimensionsParser(new CommonTokenStream(lexer));
			}

			List<?> list;
			List<?> defaultList;

			try {
				try {
					parser.dimensionvalues();
				}
				catch (RecognitionException e) {
					throw new Exception(parser.error);
				}

				list = parser.values;

				if (defaultParser != null) {
					try {
						defaultParser.dimensionvalues();
					}
					catch (RecognitionException e) {
						throw new Exception(defaultParser.error);
					}
					defaultList = defaultParser.values;
				}
				else {
					defaultList = parser.values;
				}
			}
			catch (Exception e) {
				LOG.warn(
						"The dimension '{}' has not been added for layer '{}' because the error"
								+ " '{}' occurred while parsing the extent/default values.",
						new Object[] { type.getName(), layerName, e.getLocalizedMessage() });
				continue;
			}

			if (type.isIsTime()) {
				handleTime(type, map, defaultList, list, layerName);
			}
			else if (type.isIsElevation()) {
				handleElevation(type, map, defaultList, list, layerName);
			}
			else {
				handleOther(type, map, defaultList, list, layerName);
			}
		}
		return map;
	}

	private static void handleTime(DimensionType type, Map<String, Dimension<?>> map, List<?> defaultList, List<?> list,
			String layerName) {
		try {
			boolean current = (type.isCurrent() != null) && type.isCurrent();
			boolean nearest = (type.isNearestValue() != null) && type.isNearestValue();
			boolean multiple = (type.isMultipleValues() != null) && type.isMultipleValues();
			map.put("time", new Dimension<Object>("time", (List<?>) parseTyped(defaultList, true), current, nearest,
					multiple, "ISO8601", null, type.getSource(), (List<?>) parseTyped(list, true)));
		}
		catch (ParseException e) {
			LOG.warn(
					"The TIME dimension has not been added for layer {} because the error"
							+ " '{}' occurred while parsing the extent/default values.",
					layerName, e.getLocalizedMessage());
		}
	}

	private static void handleElevation(DimensionType type, Map<String, Dimension<?>> map, List<?> defaultList,
			List<?> list, String layerName) {
		try {
			boolean nearest = (type.isNearestValue() != null) && type.isNearestValue();
			boolean multiple = (type.isMultipleValues() != null) && type.isMultipleValues();
			map.put("elevation",
					new Dimension<Object>("elevation", (List<?>) parseTyped(defaultList, false), false, nearest,
							multiple, type.getUnits(), type.getUnitSymbol() == null ? "m" : type.getUnitSymbol(),
							type.getSource(), (List<?>) parseTyped(list, false)));
		}
		catch (ParseException e) {
			// does not happen, as we're not parsing with time == true
		}
	}

	private static void handleOther(DimensionType type, Map<String, Dimension<?>> map, List<?> defaultList,
			List<?> list, String layerName) {
		try {
			boolean nearest = (type.isNearestValue() != null) && type.isNearestValue();
			boolean multiple = (type.isMultipleValues() != null) && type.isMultipleValues();
			Dimension<Object> dim;
			dim = new Dimension<Object>(type.getName(), (List<?>) parseTyped(defaultList, false), false, nearest,
					multiple, type.getUnits(), type.getUnitSymbol(), type.getSource(),
					(List<?>) parseTyped(list, false));
			map.put(type.getName(), dim);
		}
		catch (ParseException e) {
			// does not happen, as we're not parsing with time == true
		}
	}

}
