/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.layer.config;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.deegree.commons.utils.Pair;
import org.deegree.layer.dims.Dimension;
import org.deegree.layer.persistence.base.jaxb.DimensionType;
import org.deegree.layer.persistence.base.jaxb.LayerOptionsType;
import org.deegree.layer.persistence.base.jaxb.StyleRefType;
import org.deegree.layer.persistence.base.jaxb.StyleRefType.Style.LegendGraphic;
import org.deegree.layer.persistence.base.jaxb.StyleRefType.Style.LegendStyle;
import org.deegree.rendering.r2d.context.MapOptions;
import org.deegree.rendering.r2d.context.MapOptions.Antialias;
import org.deegree.rendering.r2d.context.MapOptions.Interpolation;
import org.deegree.rendering.r2d.context.MapOptions.Quality;
import org.deegree.style.persistence.StyleStore;
import org.deegree.style.persistence.StyleStoreProvider;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.slf4j.Logger;

/**
 * Some methods to work with the jaxb beans from the base layer schema.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public final class ConfigUtils {

	private static final Logger LOG = getLogger(ConfigUtils.class);

	private ConfigUtils() {
	}

	public static Pair<Map<String, Style>, Map<String, Style>> parseStyles(Workspace workspace, String layerName,
			List<StyleRefType> styles) {
		// hail java 7 to finally be able to do some really complicated type inference
		Map<String, Style> styleMap = new LinkedHashMap<String, Style>();
		Map<String, Style> legendStyleMap = new LinkedHashMap<String, Style>();

		Style defaultStyle = null, defaultLegendStyle = null;

		for (StyleRefType srt : styles) {
			String id = srt.getStyleStoreId();
			StyleStore store = workspace.getResource(StyleStoreProvider.class, id);
			if (srt.getStyle() == null || srt.getStyle().isEmpty()) {
				if (store.getAll(layerName) != null) {
					for (Style s : store.getAll(layerName)) {
						if (defaultStyle == null) {
							defaultStyle = s;
							defaultLegendStyle = s;
						}
						styleMap.put(s.getName(), s);
						legendStyleMap.put(s.getName(), s);
					}
				}
				continue;
			}
			Pair<Style, Style> p = useSelectedStyles(workspace, store, srt, id, styleMap, legendStyleMap, defaultStyle,
					defaultLegendStyle);
			defaultStyle = p.first;
			defaultLegendStyle = p.second;
		}
		checkDefaultStyle(defaultStyle, styleMap, defaultLegendStyle, legendStyleMap);
		return new Pair<Map<String, Style>, Map<String, Style>>(styleMap, legendStyleMap);
	}

	public static List<ResourceIdentifier<StyleStore>> getStyleDeps(List<StyleRefType> styles) {
		List<ResourceIdentifier<StyleStore>> list = new ArrayList<ResourceIdentifier<StyleStore>>();
		for (StyleRefType srt : styles) {
			String id = srt.getStyleStoreId();
			list.add(new DefaultResourceIdentifier<StyleStore>(StyleStoreProvider.class, id));
		}
		return list;
	}

	private static Pair<Style, Style> useSelectedStyles(Workspace workspace, StyleStore store, StyleRefType srt,
			String id, Map<String, Style> styleMap, Map<String, Style> legendStyleMap, Style defaultStyle,
			Style defaultLegendStyle) {
		for (org.deegree.layer.persistence.base.jaxb.StyleRefType.Style s : srt.getStyle()) {
			boolean isDefault = false;
			String name = s.getStyleName();
			String title = s.getStyleTitle();
			String nameRef = s.getStyleNameRef();
			String layerRef = s.getLayerNameRef();
			Style st = store.getStyle(layerRef, nameRef);
			if (st == null) {
				LOG.warn("The combination of layer {} and style {} from store {} is not available.",
						new Object[] { layerRef, nameRef, id });
				continue;
			}
			if (defaultStyle == null) {
				isDefault = true;
				defaultStyle = st;
			}
			st = st.copy();
			st.setName(name);
			st.setTitle(title);
			styleMap.put(name, st);
			if (isDefault && !styleMap.containsKey("default")) {
				styleMap.put("default", st);
			}
			if (hasLegendGraphic(s) || hasLegendStyle(s)) {
				if (hasLegendGraphic(s)) {
					LegendGraphic g = s.getLegendGraphic();

					URL url = null;
					try {
						url = new URL(g.getValue());
						if (url.toURI().isAbsolute()) {
							st.setLegendURL(url);
						}
						st.setPrefersGetLegendGraphicUrl(g.isOutputGetLegendGraphicUrl());
					}
					catch (Exception e) {
						LOG.debug("LegendGraphic was not an absolute URL.");
						LOG.trace("Stack trace:", e);
					}

					if (url == null) {
						File file = store.getMetadata().getLocation().resolveToFile(g.getValue());
						if (file.exists()) {
							st.setLegendFile(file);
						}
						else {
							LOG.warn("LegendGraphic {} could not be resolved to a legend.", g.getValue());
						}
					}
				}
				else if (hasLegendStyle(s)) {
					LegendStyle ls = s.getLegendStyle();
					st = store.getStyle(ls.getLayerNameRef(), ls.getStyleNameRef());
					st = st.copy();
					st.setName(name);
				}
				legendStyleMap.put(name, st);
				if (defaultLegendStyle == null) {
					defaultLegendStyle = st;
				}
			}
		}
		return new Pair<Style, Style>(defaultStyle, defaultLegendStyle);
	}

	private static void checkDefaultStyle(Style defaultStyle, Map<String, Style> styleMap, Style defaultLegendStyle,
			Map<String, Style> legendStyleMap) {
		if (defaultStyle != null && !styleMap.containsKey("default")) {
			styleMap.put("default", defaultStyle);
		}
		if (defaultLegendStyle != null && !legendStyleMap.containsKey("default")) {
			legendStyleMap.put("default", defaultLegendStyle);
		}
		if (defaultStyle != null && !legendStyleMap.containsKey("default")) {
			legendStyleMap.put("default", defaultStyle);
		}
		if (!styleMap.containsKey("default")) {
			styleMap.put("default", new Style());
		}
		if (!legendStyleMap.containsKey("default")) {
			legendStyleMap.put("default", new Style());
		}
	}

	/**
	 * @param cfg
	 * @return null, if cfg is null
	 */
	public static MapOptions parseLayerOptions(LayerOptionsType cfg) {
		if (cfg == null) {
			return null;
		}
		Antialias alias = null;
		Quality quali = null;
		Interpolation interpol = null;
		int maxFeats = -1;
		int rad = -1;
		boolean opaque = false;
		Integer decimalPlaces = null;
		try {
			alias = Antialias.valueOf(cfg.getAntiAliasing());
		}
		catch (Throwable e) {
			// ignore
		}
		try {
			quali = Quality.valueOf(cfg.getRenderingQuality());
		}
		catch (Throwable e) {
			// ignore
		}
		try {
			interpol = Interpolation.valueOf(cfg.getInterpolation());
		}
		catch (Throwable e) {
			// ignore
		}
		if (cfg.getMaxFeatures() != null) {
			maxFeats = cfg.getMaxFeatures();
		}
		if (cfg.getFeatureInfo() != null) {
			if (cfg.getFeatureInfo().isEnabled()) {
				rad = Math.max(0, cfg.getFeatureInfo().getPixelRadius().intValue());
				decimalPlaces = Optional.ofNullable(cfg.getFeatureInfo().getDecimalPlaces())
					.map(BigInteger::intValue)
					.orElse(null);
			}
			else {
				rad = 0;
			}
		}
		else if (cfg.getFeatureInfoRadius() != null) {
			rad = Math.max(0, cfg.getFeatureInfoRadius());
		}
		if (cfg.isOpaque() != null) {
			opaque = cfg.isOpaque();
		}
		return new MapOptions.Builder().quality(quali)
			.interpolation(interpol)
			.antialias(alias)
			.maxFeatures(maxFeats)
			.featureInfoRadius(rad)
			.featureInfoDecimalPlaces(decimalPlaces)
			.build();
	}

	public static Map<String, Dimension<?>> parseDimensions(String layerName, List<DimensionType> dimensions) {
		return DimensionConfigBuilder.parseDimensions(layerName, dimensions);
	}

	private static boolean hasLegendGraphic(org.deegree.layer.persistence.base.jaxb.StyleRefType.Style s) {
		return s.getLegendGraphic() != null;
	}

	private static boolean hasLegendStyle(org.deegree.layer.persistence.base.jaxb.StyleRefType.Style s) {
		return s.getLegendStyle() != null;
	}

}
