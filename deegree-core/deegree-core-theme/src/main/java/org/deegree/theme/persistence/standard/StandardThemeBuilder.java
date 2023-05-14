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
package org.deegree.theme.persistence.standard;

import static java.util.Collections.singletonList;
import static org.deegree.theme.Themes.aggregateSpatialMetadata;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.ows.metadata.DescriptionConverter;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.geometry.metadata.SpatialMetadataConverter;
import org.deegree.layer.Layer;
import org.deegree.layer.dims.Dimension;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.LayerStoreProvider;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.theme.Theme;
import org.deegree.theme.persistence.standard.jaxb.ThemeType;
import org.deegree.theme.persistence.standard.jaxb.ThemeType.Identifier;
import org.deegree.theme.persistence.standard.jaxb.Themes;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * Builds a {@link StandardTheme} from jaxb config beans.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class StandardThemeBuilder implements ResourceBuilder<Theme> {

	private static final Logger LOG = getLogger(StandardThemeBuilder.class);

	private Themes config;

	private ResourceMetadata<Theme> metadata;

	private Workspace workspace;

	public StandardThemeBuilder(Themes config, ResourceMetadata<Theme> metadata, Workspace workspace) {
		this.config = config;
		this.metadata = metadata;
		this.workspace = workspace;
	}

	private Layer findLayer(ThemeType.Layer l, Map<String, LayerStore> stores) {
		Layer lay = null;
		if (l.getLayerStore() != null) {
			LayerStore s = stores.get(l.getLayerStore());
			if (s != null) {
				lay = s.get(l.getValue());
			}
			if (lay == null) {
				LOG.warn("Layer with identifier {} is not available from {}, trying all.", l.getValue(),
						l.getLayerStore());
			}
		}
		if (lay == null) {
			for (LayerStore s : stores.values()) {
				lay = s.get(l.getValue());
				if (lay != null) {
					break;
				}
			}
		}
		if (lay == null) {
			LOG.warn("Layer with identifier {} is not available from any layer store.", l.getValue());
		}
		return lay;
	}

	private StandardTheme buildTheme(ThemeType current, List<ThemeType.Layer> layers, List<ThemeType> themes,
			Map<String, LayerStore> stores) {
		List<Layer> lays = new ArrayList<Layer>(layers.size());

		LinkedHashMap<String, Dimension<?>> dims = new LinkedHashMap<String, Dimension<?>>();
		LinkedHashMap<String, Style> styles = new LinkedHashMap<String, Style>();
		LinkedHashMap<String, Style> legendStyles = new LinkedHashMap<String, Style>();
		List<FeatureType> types = new ArrayList<FeatureType>();

		for (ThemeType.Layer l : layers) {
			Layer lay = findLayer(l, stores);
			if (lay != null) {
				if (lay.getMetadata().getDimensions() != null) {
					dims.putAll(lay.getMetadata().getDimensions());
				}
				styles.putAll(lay.getMetadata().getStyles());
				legendStyles.putAll(lay.getMetadata().getLegendStyles());
				types.addAll(lay.getMetadata().getFeatureTypes());
				lays.add(lay);
			}
		}
		List<Theme> thms = new ArrayList<Theme>(themes.size());
		for (ThemeType tt : themes) {
			StandardTheme thm = buildTheme(tt, tt.getLayer(), tt.getTheme(), stores);
			if (thm != null) {
				thms.add(thm);
			}
		}

		if (lays.isEmpty() && themes.isEmpty()) {
			LOG.warn("Skipping theme or subtheme with id {} because it is empty (no subthemes and no layers).",
					current.getIdentifier() != null ? current.getIdentifier().getValue() : " - (no identifier)");
			return null;
		}

		SpatialMetadata smd = SpatialMetadataConverter.fromJaxb(current.getEnvelope(), current.getCRS());
		Description desc = DescriptionConverter.fromJaxb(current.getTitle(), current.getAbstract(),
				current.getKeywords());
		final Identifier identifier = current.getIdentifier();
		final String name = identifier != null ? identifier.getValue() : null;
		final LayerMetadata md = new LayerMetadata(name, desc, smd);
		if (identifier != null && !identifier.isRequestable()) {
			md.setRequestable(false);
		}
		md.setDimensions(dims);
		if (current.getLegendGraphic() != null && current.getLegendGraphic().getValue() != null
				&& !current.getLegendGraphic().getValue().isEmpty()) {
			Map<String, Style> configuredLegendStyles = new HashMap<>();
			Style style = parseConfiguredStyle(current.getLegendGraphic());
			configuredLegendStyles.put(style.getName(), style);
			md.setStyles(configuredLegendStyles);
			md.setLegendStyles(configuredLegendStyles);
		}
		else {
			md.setStyles(styles);
			md.setLegendStyles(legendStyles);
		}
		return new StandardTheme(md, thms, lays, metadata);
	}

	private Style parseConfiguredStyle(ThemeType.LegendGraphic configuredLegendGraphic) {
		Style style = new Style();
		style.setName("default");
		URL url = null;
		try {
			url = new URL(configuredLegendGraphic.getValue());
			if (url.toURI().isAbsolute()) {
				style.setLegendURL(url);
			}
			style.setPrefersGetLegendGraphicUrl(configuredLegendGraphic.isOutputGetLegendGraphicUrl());
		}
		catch (Exception e) {
			LOG.debug("LegendGraphic was not an absolute URL.");
			LOG.trace("Stack trace:", e);
		}

		if (url == null) {
			File file = metadata.getLocation().resolveToFile(configuredLegendGraphic.getValue());
			if (file.exists()) {
				style.setLegendFile(file);
			}
			else {
				LOG.warn("LegendGraphic {} could not be resolved to a legend.", configuredLegendGraphic);
			}
		}
		return style;
	}

	private Theme buildAutoTheme(Layer layer) {
		LayerMetadata md = new LayerMetadata(null, null, null);
		LayerMetadata lmd = layer.getMetadata();
		md.merge(lmd);
		md.setDimensions(new LinkedHashMap<String, Dimension<?>>(lmd.getDimensions()));
		md.setStyles(new LinkedHashMap<String, Style>(lmd.getStyles()));
		md.setLegendStyles(new LinkedHashMap<String, Style>(lmd.getLegendStyles()));
		return new StandardTheme(md, Collections.<Theme>emptyList(), singletonList(layer), metadata);
	}

	private Theme buildAutoTheme(String id, LayerStore store) {
		Description desc = new Description(id, singletonList(new LanguageString(id, null)), null, null);
		LayerMetadata md = new LayerMetadata(null, desc, new SpatialMetadata(null, Collections.<ICRS>emptyList()));
		List<Theme> themes = new ArrayList<Theme>();

		for (Layer l : store.getAll()) {
			themes.add(buildAutoTheme(l));
		}

		return new StandardTheme(md, themes, new ArrayList<Layer>(), metadata);
	}

	private Theme buildAutoTheme(Map<String, LayerStore> stores) {
		Description desc = new Description(null, Collections.singletonList(new LanguageString("root", null)), null,
				null);
		LayerMetadata md = new LayerMetadata(null, desc, new SpatialMetadata(null, Collections.<ICRS>emptyList()));
		List<Theme> themes = new ArrayList<Theme>();

		for (Entry<String, LayerStore> e : stores.entrySet()) {
			themes.add(buildAutoTheme(e.getKey(), e.getValue()));
		}

		return new StandardTheme(md, themes, new ArrayList<Layer>(), metadata);
	}

	@Override
	public Theme build() {
		List<String> storeIds = config.getLayerStoreId();
		Map<String, LayerStore> stores = new LinkedHashMap<String, LayerStore>(storeIds.size());

		for (String id : storeIds) {
			LayerStore store = workspace.getResource(LayerStoreProvider.class, id);
			if (store == null) {
				LOG.warn("Layer store with id {} is not available.", id);
				continue;
			}
			stores.put(id, store);
		}

		ThemeType root = config.getTheme();
		Theme theme;
		if (root == null) {
			theme = buildAutoTheme(stores);
		}
		else {
			theme = buildTheme(root, root.getLayer(), root.getTheme(), stores);
		}
		if (theme == null) {
			throw new ResourceInitException("Root theme contains no layers and no themes.");
		}
		aggregateSpatialMetadata(theme);
		return theme;
	}

}
