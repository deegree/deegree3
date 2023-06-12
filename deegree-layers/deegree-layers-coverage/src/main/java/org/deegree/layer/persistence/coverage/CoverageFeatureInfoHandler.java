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
package org.deegree.layer.persistence.coverage;

import static java.lang.Integer.MAX_VALUE;
import static org.deegree.coverage.raster.utils.CoverageTransform.transform;
import static org.slf4j.LoggerFactory.getLogger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.geom.Grid;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeature;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;
import org.deegree.layer.dims.Dimension;
import org.slf4j.Logger;

/**
 * Responsible for creating coverage feature info responses.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class CoverageFeatureInfoHandler {

	private static final Logger LOG = getLogger(CoverageFeatureInfoHandler.class);

	private static final QName VALUE_PROP = new QName("http://www.deegree.org/app", "value", "app");

	private AbstractRaster raster;

	private Envelope bbox;

	private FeatureType featureType;

	private InterpolationType interpol;

	private CoverageDimensionHandler dimensionHandler;

	private Integer decimalPlaces;

	CoverageFeatureInfoHandler(AbstractRaster raster, Envelope bbox, FeatureType featureType,
			InterpolationType interpol, CoverageDimensionHandler dimensionHandler, Integer decimalPlaces) {
		this.raster = raster;
		this.bbox = bbox;
		this.featureType = featureType;
		this.interpol = interpol;
		this.dimensionHandler = dimensionHandler;
		this.decimalPlaces = decimalPlaces;
	}

	FeatureCollection handleFeatureInfoPoint(int x, int y, int width, int height) {
		try {
			Point bboxCenter = bbox.getCentroid();

			double[] dpos = raster.getRasterReference()
				.getRasterCoordinateUnrounded(bboxCenter.get0(), bboxCenter.get1());
			int[] pos = new int[] { (int) dpos[0], (int) dpos[1] };

			Envelope pixelEnv = raster.getRasterReference()
				.getEnvelope(OriginLocation.OUTER, new RasterRect(pos[0], pos[1], 1, 1), raster.getCoordinateSystem());

			GenericFeatureCollection col = new GenericFeatureCollection();
			if (!bbox.intersects(pixelEnv)) {
				LOG.debug("FeatureInfo point does not intersects with info box");
				return col;
			}
			else if (x < 0 || y < 0 || x >= width || y >= height) {
				LOG.debug("FeatureInfo is not aligned with the pixel box");
				return col;
			}

			// TRICKY It is necessary to perform a raster transformation first to avoid
			// addressing issues with
			// getXxxSample.
			// Background is that getXxxSample accesses the memory/file-buffer address
			// directly, bypassing the
			// tiling, which leads to address problems with large files.
			SimpleRaster infoRaster = transform(raster, bbox, Grid.fromSize(width, height, MAX_VALUE, bbox),
					interpol.toString())
				.getAsSimpleRaster();

			RasterData data = infoRaster.getAsSimpleRaster().getRasterData();
			List<Property> props = new LinkedList<Property>();
			DataType dataType = data.getDataType();
			switch (dataType) {
				case SHORT:
				case USHORT: {
					addValueToProps(props, new BigDecimal(0xffff & data.getShortSample(x, y, 0)));
					break;
				}
				case BYTE: {
					// TODO unknown why this always yields 0 values for eg. satellite
					// images/RGB/ARGB
					for (int i = 0; i < data.getBands(); ++i) {
						addValueToProps(props, new BigDecimal(0xff & data.getByteSample(x, y, i)));
					}
					break;
				}
				case DOUBLE:
				case INT:
				case UNDEFINED:
					LOG.warn("The raster is of type '{}', this is handled as float currently.", dataType);
				case FLOAT:
					addValueToProps(props, new BigDecimal(data.getFloatSample(x, y, 0)));
					break;
			}
			Feature f = new GenericFeature(featureType, null, props, null);
			if (pixelEnv != null) {
				f.setEnvelope(pixelEnv);
			}
			col.add(f);
			return col;
		}
		catch (Throwable e) {
			LOG.trace("Stack trace:", e);
			LOG.error("Unable to create raster feature info: {}", e.getLocalizedMessage());
		}
		return null;
	}

	FeatureCollection handleFeatureInfo() {
		try {
			SimpleRaster res = transform(raster, bbox, Grid.fromSize(1, 1, MAX_VALUE, bbox), interpol.toString())
				.getAsSimpleRaster();

			RasterData data = res.getRasterData();
			GenericFeatureCollection col = new GenericFeatureCollection();
			List<Property> props = new LinkedList<Property>();
			DataType dataType = data.getDataType();
			switch (dataType) {
				case SHORT:
				case USHORT: {
					addValueToProps(props, new BigDecimal(0xffff & data.getShortSample(0, 0, 0)));
					break;
				}
				case BYTE: {
					// TODO unknown why this always yields 0 values for eg. satellite
					// images/RGB/ARGB
					for (int i = 0; i < data.getBands(); ++i) {
						addValueToProps(props, new BigDecimal(0xff & data.getByteSample(0, 0, i)));
					}
					break;
				}
				case DOUBLE:
				case INT:
				case UNDEFINED:
					LOG.warn("The raster is of type '{}', this is handled as float currently.", dataType);
				case FLOAT:
					addValueToProps(props, new BigDecimal(data.getFloatSample(0, 0, 0)));
					break;
			}
			Feature f = new GenericFeature(featureType, null, props, null);
			col.add(f);
			return col;
		}
		catch (Throwable e) {
			LOG.trace("Stack trace:", e);
			LOG.error("Unable to create raster feature info: {}", e.getLocalizedMessage());
		}
		return null;
	}

	private PropertyType findValueProperty() {
		List<PropertyType> propertyDeclarations = featureType.getPropertyDeclarations();
		for (PropertyType propertyType : propertyDeclarations) {
			if (VALUE_PROP.equals(propertyType.getName())) {
				return propertyType;
			}
		}
		LOG.warn("Could not find property with name 'value', use the first property.");
		return propertyDeclarations.get(0);
	}

	private Map<QName, PrimitiveValue> createAttributeList() {
		Map<QName, PrimitiveValue> attrs = new HashMap<QName, PrimitiveValue>();
		if (dimensionHandler != null && dimensionHandler.getDimension() != null) {
			Dimension<?> dimension = dimensionHandler.getDimension();
			String uom = createUom(dimension);
			if (uom != null)
				attrs.put(new QName("uom"), new PrimitiveValue(uom));
		}
		return attrs;
	}

	private String createUom(Dimension<?> dimension) {
		String unitSymbol = dimension.getUnitSymbol();
		if (unitSymbol != null && unitSymbol.length() > 0)
			return unitSymbol;
		return dimension.getUnits();
	}

	private void addValueToProps(List<Property> props, BigDecimal result) {
		PrimitiveValue val = new PrimitiveValue(roundValue(result), new PrimitiveType(BaseType.DECIMAL));
		props.add(new GenericProperty(findValueProperty(), val));
	}

	private BigDecimal roundValue(BigDecimal value) {
		return decimalPlaces != null ? value.setScale(decimalPlaces, RoundingMode.HALF_UP) : value;
	}

}