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
package org.deegree.layer.persistence.remotewms;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.ows.metadata.DescriptionConverter;
import org.deegree.commons.utils.DoublePair;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.geometry.metadata.SpatialMetadataConverter;
import org.deegree.gml.GMLVersion;
import org.deegree.layer.Layer;
import org.deegree.layer.config.ConfigUtils;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.metadata.XsltFile;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.base.jaxb.ScaleDenominatorsType;
import org.deegree.layer.persistence.remotewms.jaxb.LayerType;
import org.deegree.layer.persistence.remotewms.jaxb.RemoteWMSLayers;
import org.deegree.layer.persistence.remotewms.jaxb.RequestOptionsType;
import org.deegree.layer.persistence.remotewms.jaxb.StyleType;
import org.deegree.layer.persistence.remotewms.jaxb.LayerType.XSLTFile;
import org.deegree.protocol.wms.client.WMSClient;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.workspace.ResourceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds remote wms layers from jaxb beans.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class RemoteWmsLayerBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(RemoteWmsLayerBuilder.class);

	private WMSClient client;

	private RemoteWMSLayers cfg;

	private ResourceMetadata<LayerStore> metadata;

	RemoteWmsLayerBuilder(WMSClient client, RemoteWMSLayers cfg, ResourceMetadata<LayerStore> metadata) {
		this.client = client;
		this.cfg = cfg;
		this.metadata = metadata;
	}

	Map<String, Layer> buildLayerMap() {
		Map<String, LayerMetadata> configured = collectConfiguredLayers();
		if (configured.isEmpty())
			return parseAllRemoteLayers();
		return collectConfiguredRemoteLayers(configured);
	}

	private Map<String, Layer> parseAllRemoteLayers() {
		Map<String, Layer> map = new LinkedHashMap<>();
		RequestOptionsType opts = cfg.getRequestOptions();
		List<LayerMetadata> layers = client.getLayerTree().flattenDepthFirst();
		for (LayerMetadata md : layers) {
			if (md.getName() != null) {
				map.put(md.getName(), new RemoteWMSLayer(md.getName(), md, client, opts));
			}
		}
		return map;
	}

	private Map<String, Layer> collectConfiguredRemoteLayers(Map<String, LayerMetadata> configured) {
		Map<String, Layer> map = new LinkedHashMap<>();
		RequestOptionsType opts = cfg.getRequestOptions();
		List<LayerMetadata> layers = client.getLayerTree().flattenDepthFirst();
		for (LayerMetadata md : layers) {
			String name = md.getName();
			LayerMetadata confMd = configured.get(name);
			if (confMd != null) {
				confMd.merge(md);
				mergeStyleAndLegendStyles(md, confMd);
				map.put(confMd.getName(), new RemoteWMSLayer(name, confMd, client, opts));
			}
		}
		return map;
	}

	private void mergeStyleAndLegendStyles(LayerMetadata remoteServiceMd, LayerMetadata confMd) {
		Map<String, Style> configuredLegendStyles = confMd.getLegendStyles();
		Map<String, Style> remoteServiceLegendStyles = remoteServiceMd.getLegendStyles();
		Map<String, Style> remoteServiceStyles = remoteServiceMd.getStyles();
		if (!configuredLegendStyles.isEmpty()) {
			for (String styleName : configuredLegendStyles.keySet()) {
				Style configuredLegendStyle = configuredLegendStyles.get(styleName);
				Style remoteServiceStyle = remoteServiceStyles.get(styleName);
				if (remoteServiceStyle != null) {
					setLegendUrlAndFile(remoteServiceStyle, configuredLegendStyle);
				}
				Style remoteServiceLegendStyle = remoteServiceLegendStyles.get(styleName);
				if (remoteServiceLegendStyle != null) {
					setLegendUrlAndFile(remoteServiceLegendStyle, configuredLegendStyle);
				}
			}
			removeUnconfiguredStyles(configuredLegendStyles, remoteServiceLegendStyles, remoteServiceStyles);
		}
		confMd.setLegendStyles(remoteServiceLegendStyles);
		confMd.setStyles(remoteServiceStyles);
	}

	private void removeUnconfiguredStyles(Map<String, Style> configuredLegendStyles,
			Map<String, Style> remoteServiceLegendStyles, Map<String, Style> remoteServiceStyles) {
		for (String remoteServiceStyleName : remoteServiceStyles.keySet()) {
			if (!"default".equalsIgnoreCase(remoteServiceStyleName)
					&& !configuredLegendStyles.containsKey(remoteServiceStyleName)) {
				remoteServiceStyles.remove(remoteServiceStyleName);
				remoteServiceLegendStyles.remove(remoteServiceStyleName);
			}
		}
	}

	private void setLegendUrlAndFile(Style targetStyle, Style sourceStyle) {
		targetStyle.setPrefersGetLegendGraphicUrl(sourceStyle.prefersGetLegendGraphicUrl());
		if (sourceStyle.getLegendURL() != null) {
			targetStyle.setLegendURL(sourceStyle.getLegendURL());
		}
		if (sourceStyle.getLegendFile() != null) {
			targetStyle.setLegendURL(null);
			targetStyle.setLegendFile(sourceStyle.getLegendFile());
		}
	}

	private Map<String, LayerMetadata> collectConfiguredLayers() {
		Map<String, LayerMetadata> configured = new HashMap<String, LayerMetadata>();
		if (cfg.getLayer() != null) {
			for (LayerType l : cfg.getLayer()) {
				if (!client.hasLayer(l.getOriginalName())) {
					LOG.warn("Layer {} is not offered by the remote WMS.", l.getOriginalName());
					continue;
				}
				String name = l.getName();
				SpatialMetadata smd = SpatialMetadataConverter.fromJaxb(l.getEnvelope(), l.getCRS());
				Description desc = null;
				if (l.getDescription() != null) {
					desc = DescriptionConverter.fromJaxb(l.getDescription().getTitle(),
							l.getDescription().getAbstract(), l.getDescription().getKeywords());
				}

				LayerMetadata md = new LayerMetadata(name, desc, smd);
				ScaleDenominatorsType denoms = l.getScaleDenominators();
				if (denoms != null) {
					md.setScaleDenominators(new DoublePair(denoms.getMin(), denoms.getMax()));
				}
				md.setMapOptions(ConfigUtils.parseLayerOptions(l.getLayerOptions()));
				md.setXsltFile(parseXsltFile(md, l.getXSLTFile()));
				md.setLegendStyles(parseConfiguredStyles(l));
				configured.put(l.getOriginalName(), md);
			}
		}
		return configured;
	}

	private Map<String, Style> parseConfiguredStyles(LayerType l) {
		return l.getStyle().stream().map(configuredStyle -> {
			Style style = new Style();
			style.setName(configuredStyle.getOriginalName());
			StyleType.LegendGraphic g = configuredStyle.getLegendGraphic();

			URL url = null;
			try {
				url = new URL(g.getValue());
				if (url.toURI().isAbsolute()) {
					style.setLegendURL(url);
				}
				style.setPrefersGetLegendGraphicUrl(g.isOutputGetLegendGraphicUrl());
			}
			catch (Exception e) {
				LOG.debug("LegendGraphic was not an absolute URL.");
				LOG.trace("Stack trace:", e);
			}

			if (url == null) {
				File file = metadata.getLocation().resolveToFile(g.getValue());
				if (file.exists()) {
					style.setLegendFile(file);
				}
				else {
					LOG.warn("LegendGraphic {} could not be resolved to a legend.", g.getValue());
				}
			}
			return style;
		}).collect(Collectors.toMap(Style::getName, Function.identity()));
	}

	private XsltFile parseXsltFile(LayerMetadata md, XSLTFile xsltFileConfig) {
		if (xsltFileConfig != null) {
			GMLVersion gmlVersion = GMLVersion.valueOf(xsltFileConfig.getTargetGmlVersion().value());
			String xslFile = xsltFileConfig.getValue();
			URL xsltFileUrl = metadata.getLocation().resolveToUrl(xslFile);
			if (xsltFileUrl == null) {
				LOG.warn("Could not resolve xslt file url {}.", xslFile);
			}
			else {
				return new XsltFile(xsltFileUrl, gmlVersion);
			}
		}
		return null;
	}

}
