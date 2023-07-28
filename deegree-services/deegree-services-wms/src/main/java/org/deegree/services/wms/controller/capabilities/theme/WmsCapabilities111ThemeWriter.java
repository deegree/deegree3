/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.services.wms.controller.capabilities.theme;

import static java.lang.Double.MAX_VALUE;
import static java.lang.Double.MIN_VALUE;
import static org.deegree.commons.xml.CommonNamespaces.XLINK_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.XMLAdapter.writeElement;
import static org.deegree.services.wms.controller.capabilities.Capabilities111XMLAdapter.writeDimensions;
import static org.deegree.services.wms.controller.capabilities.WmsCapabilities111SpatialMetadataWriter.writeSrsAndEnvelope;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.ows.metadata.MetadataUrl;
import org.deegree.commons.ows.metadata.layer.Attribution;
import org.deegree.commons.ows.metadata.layer.ExternalIdentifier;
import org.deegree.commons.ows.metadata.layer.LogoUrl;
import org.deegree.commons.ows.metadata.layer.UrlWithFormat;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.rendering.r2d.legends.Legends;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.wms.controller.capabilities.Capabilities111XMLAdapter;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.theme.Theme;

/**
 * Writes WMS 1.1.1 Layer elements.
 * <p>
 * Data/Metadata is considered from the Theme/Layer tree as well as from the
 * {@link OWSMetadataProvider}.
 * </p>
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
public class WmsCapabilities111ThemeWriter {

	private final OWSMetadataProvider metadataProvider;

	private final Capabilities111XMLAdapter styleWriter;

	private final String mdUrlTemplate;

	private final DecimalFormat scaleFormat;

	/**
	 * Creates a new {@link WmsCapabilities111ThemeWriter} instance.
	 * @param metadataProvider provider for metadata on OWS datasets, can be
	 * <code>null</code>
	 * @param styleWriter writer for WMS 1.1.1 Style elements, can be <code>null</code>
	 * (styles will be skipped)
	 * @param mdUrlTemplate URL template for requesting metadata records
	 * (<code>${metadataSetId}</code> will be replaced with metadata id), can be
	 * <code>null</code>
	 */
	public WmsCapabilities111ThemeWriter(final OWSMetadataProvider metadataProvider,
			final Capabilities111XMLAdapter styleWriter, final String mdUrlTemplate) {
		this.metadataProvider = metadataProvider;
		this.styleWriter = styleWriter;
		this.mdUrlTemplate = mdUrlTemplate;
		final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		this.scaleFormat = new DecimalFormat("0.0#######", symbols);
	}

	/**
	 * Writes the given {@link Theme} as a WMS 1.1,1 Layer element.
	 * @param writer used to write the XML, must not be <code>null</code>
	 * @param theme theme to be serialized, must not be <code>null</code>
	 * @throws XMLStreamException
	 */
	public void writeTheme(final XMLStreamWriter writer, final Theme theme) throws XMLStreamException {
		final LayerMetadata layerMetadata = new LayerMetadataMerger().merge(theme);
		final DatasetMetadataFactory factory = new DatasetMetadataFactory();
		final List<DatasetMetadata> dsMd1 = getDatasetMetadataFromProvider(theme);
		final DatasetMetadata dsMd2 = factory.buildDatasetMetadata(layerMetadata, theme, mdUrlTemplate);
		if (dsMd1 != null && dsMd2 != null)
			dsMd1.add(dsMd2);
		final DatasetMetadata datasetMetadata = new DatasetMetadataMerger().merge(dsMd1);
		final DoublePair scaleDenominators = new LayerMetadataMerger().mergeScaleDenominators(theme);
		final Map<String, String> authorityNameToUrl = getExternalAuthorityNameToUrlMap(metadataProvider);
		writeTheme(writer, layerMetadata, datasetMetadata, authorityNameToUrl, scaleDenominators, theme.getThemes());
	}

	private List<DatasetMetadata> getDatasetMetadataFromProvider(final Theme theme) {
		final String datasetName = getNameFromTheme(theme);
		if (metadataProvider != null && datasetName != null) {
			return metadataProvider.getAllDatasetMetadata(new QName(datasetName));
		}
		return new ArrayList<DatasetMetadata>();
	}

	private Map<String, String> getExternalAuthorityNameToUrlMap(final OWSMetadataProvider metadataProvider) {
		if (metadataProvider != null) {
			return metadataProvider.getExternalMetadataAuthorities();
		}
		return null;
	}

	private String getNameFromTheme(final Theme theme) {
		if (theme.getLayerMetadata().getName() != null) {
			return theme.getLayerMetadata().getName();
		}
		return null;
	}

	void writeTheme(final XMLStreamWriter writer, final LayerMetadata layerMetadata,
			final DatasetMetadata datasetMetadata, final Map<String, String> authorityNameToUrl,
			final DoublePair scaleDenominators, final List<Theme> subThemes) throws XMLStreamException {
		writer.writeStartElement("Layer");
		// queryable (0 | 1) "0"
		writeQueryable(writer,
				layerMetadata.isRequestable() && layerMetadata.isQueryable() && layerMetadata.getName() != null);
		// cascaded CDATA #IMPLIED
		writeCascaded(writer, layerMetadata.getCascaded());
		// opaque (0 | 1) "0"
		writeOpaque(writer, layerMetadata.getMapOptions() != null && layerMetadata.getMapOptions().isOpaque());
		// noSubsets (0 | 1) "0"
		writeNoSubset(writer);
		// fixedWidth CDATA #IMPLIED
		writeFixedWidth(writer);
		// fixedHeight CDATA #IMPLIED
		writeFixedHeight(writer);
		// Name?
		if (layerMetadata.isRequestable()) {
			writeName(writer, layerMetadata.getName());
		}
		// Title
		writeTitle(writer, datasetMetadata.getTitles(), layerMetadata.getName());
		// Abstract?
		writeAbstract(writer, datasetMetadata.getAbstracts());
		// KeywordList?
		writeKeywordList(writer, datasetMetadata.getKeywords());
		// SRS*
		// LatLonBoundingBox?
		// BoundingBox*
		writeSrsAndBoundingBoxes(writer, layerMetadata.getSpatialMetadata());
		// Dimension*
		writeDimensions(writer, layerMetadata.getDimensions());
		// Attribution?
		writeAttribution(writer, datasetMetadata.getAttribution());
		// AuthorityURL*
		writeAuthorityUrls(writer, authorityNameToUrl);
		// Identifier*
		writeIdentifiers(writer, datasetMetadata.getExternalIds());
		// MetadataURL*
		writeMetadataUrls(writer, datasetMetadata.getMetadataUrls());
		// DataURL*
		writeDataUrls(writer, datasetMetadata.getDataUrls());
		// FeatureListURL*
		writeFeatureListUrls(writer, datasetMetadata.getFeatureListUrls());
		// Style*
		writeStyles(writer, layerMetadata.getName(), layerMetadata.getLegendStyles(), layerMetadata.getStyles());
		// ScaleHint?
		writeScaleHint(writer, scaleDenominators);
		// Layer*
		if (subThemes != null) {
			for (final Theme subTheme : subThemes) {
				writeTheme(writer, subTheme);
			}
		}
		writer.writeEndElement();
	}

	private void writeQueryable(final XMLStreamWriter writer, final boolean queryable) throws XMLStreamException {
		if (queryable) {
			writer.writeAttribute("queryable", "1");
		}
		else {
			writer.writeAttribute("queryable", "0");
		}
	}

	private void writeCascaded(final XMLStreamWriter writer, final int cascaded) throws XMLStreamException {
		if (cascaded > 0) {
			writer.writeAttribute("cascaded", cascaded + "");
		}
		else {
			writer.writeAttribute("cascaded", "0");
		}
	}

	private void writeOpaque(final XMLStreamWriter writer, final boolean opaque) throws XMLStreamException {
		if (opaque) {
			writer.writeAttribute("opaque", "1");
		}
		else {
			writer.writeAttribute("opaque", "0");
		}
	}

	private void writeNoSubset(final XMLStreamWriter writer) throws XMLStreamException {
		writer.writeAttribute("noSubsets", "0");

	}

	private void writeFixedWidth(final XMLStreamWriter writer) throws XMLStreamException {
		writer.writeAttribute("fixedWidth", "0");
	}

	private void writeFixedHeight(final XMLStreamWriter writer) throws XMLStreamException {
		writer.writeAttribute("fixedHeight", "0");
	}

	private void writeName(final XMLStreamWriter writer, final String name) throws XMLStreamException {
		if (name != null) {
			writeElement(writer, "Name", name);
		}
	}

	private void writeTitle(final XMLStreamWriter writer, final List<LanguageString> titles, final String name)
			throws XMLStreamException {
		if (titles != null && !titles.isEmpty()) {
			writeElement(writer, "Title", titles.get(0).getString());
		}
		else if (name != null) {
			writeElement(writer, "Title", name);
		}
	}

	private void writeAbstract(final XMLStreamWriter writer, final List<LanguageString> abstracts)
			throws XMLStreamException {
		if (abstracts != null && !abstracts.isEmpty()) {
			writeElement(writer, "Abstract", abstracts.get(0).getString());
		}
	}

	private void writeKeywordList(final XMLStreamWriter writer,
			final List<Pair<List<LanguageString>, CodeType>> keywordList) throws XMLStreamException {
		if (keywordList != null && !keywordList.isEmpty()) {
			writer.writeStartElement("KeywordList");
			for (final Pair<List<LanguageString>, CodeType> kws : keywordList) {
				for (final LanguageString ls : kws.first) {
					// <!ELEMENT Keyword (#PCDATA) >
					writeElement(writer, "Keyword", ls.getString());
				}
			}
			writer.writeEndElement();
		}
	}

	private void writeSrsAndBoundingBoxes(final XMLStreamWriter writer, final SpatialMetadata smd)
			throws XMLStreamException {
		if (smd != null) {
			writeSrsAndEnvelope(writer, smd.getCoordinateSystems(), smd.getEnvelope());
		}
	}

	private void writeAttribution(final XMLStreamWriter writer, final Attribution attribution)
			throws XMLStreamException {
		if (attribution != null) {
			writer.writeStartElement("Attribution");
			if (attribution.getTitle() != null) {
				writer.writeStartElement("Title");
				writer.writeCharacters(attribution.getTitle());
				writer.writeEndElement();
			}
			writeOnlineResource(writer, attribution.getUrl());
			if (attribution.getLogoUrl() != null) {
				final LogoUrl logoUrl = attribution.getLogoUrl();
				writer.writeStartElement("LogoURL");
				if (logoUrl.getWidth() != null) {
					writer.writeAttribute("width", "" + logoUrl.getWidth());
				}
				if (logoUrl.getHeight() != null) {
					writer.writeAttribute("height", "" + logoUrl.getHeight());
				}
				if (logoUrl.getFormat() != null) {
					writer.writeStartElement("Format");
					writer.writeCharacters(logoUrl.getFormat());
					writer.writeEndElement();
				}
				writeOnlineResource(writer, logoUrl.getUrl());
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
	}

	private void writeAuthorityUrls(final XMLStreamWriter writer, final Map<String, String> authorityNameToUrl)
			throws XMLStreamException {
		if (authorityNameToUrl != null) {
			for (final Entry<String, String> authorityNameAndUrl : authorityNameToUrl.entrySet()) {
				writer.writeStartElement("AuthorityURL");
				writer.writeAttribute("name", authorityNameAndUrl.getKey());
				writeOnlineResource(writer, authorityNameAndUrl.getValue());
				writer.writeEndElement();
			}
		}
	}

	private void writeIdentifiers(final XMLStreamWriter writer, final List<ExternalIdentifier> ids)
			throws XMLStreamException {
		if (ids == null) {
			return;
		}
		for (final ExternalIdentifier id : ids) {
			writer.writeStartElement("Identifier");
			if (id.getAuthorityCode() != null) {
				writer.writeAttribute("authority", id.getAuthorityCode());
			}
			writer.writeCharacters(id.getId());
			writer.writeEndElement();
		}
	}

	private void writeDataUrls(final XMLStreamWriter writer, final List<UrlWithFormat> urls) throws XMLStreamException {
		if (urls == null) {
			return;
		}
		for (final UrlWithFormat url : urls) {
			writer.writeStartElement("DataURL");
			if (url.getFormat() != null) {
				writer.writeStartElement("Format");
				writer.writeCharacters(url.getFormat());
				writer.writeEndElement();
			}
			writeOnlineResource(writer, url.getUrl());
			writer.writeEndElement();
		}
	}

	private void writeFeatureListUrls(final XMLStreamWriter writer, final List<UrlWithFormat> urls)
			throws XMLStreamException {
		if (urls == null) {
			return;
		}
		for (final UrlWithFormat url : urls) {
			writer.writeStartElement("FeatureListURL");
			if (url.getFormat() != null) {
				writer.writeStartElement("Format");
				writer.writeCharacters(url.getFormat());
				writer.writeEndElement();
			}
			writeOnlineResource(writer, url.getUrl());
			writer.writeEndElement();
		}
	}

	private void writeMetadataUrls(final XMLStreamWriter writer, final List<MetadataUrl> list)
			throws XMLStreamException {
		if (list == null) {
			return;
		}
		for (final MetadataUrl url : list) {
			writer.writeStartElement("MetadataURL");
			if (url.getType() == null) {
				writer.writeAttribute("type", "TC211");
			}
			else {
				writer.writeAttribute("type", url.getType());
			}
			if (url.getFormat() == null) {
				writeElement(writer, "Format", "application/xml");
			}
			else {
				writeElement(writer, "Format", url.getFormat());
			}
			writeOnlineResource(writer, url.getUrl());
			writer.writeEndElement();
		}
	}

	private void writeStyles(final XMLStreamWriter writer, final String name, final Map<String, Style> legends,
			final Map<String, Style> styles) throws XMLStreamException {
		if (styleWriter != null) {
			for (final Entry<String, Style> e : styles.entrySet()) {
				String styleName = e.getKey();
				if (styleName == null || styleName.isEmpty()) {
					continue;
				}
				Style style = e.getValue();
				Style ls = style;
				if (legends.get(styleName) != null) {
					ls = legends.get(styleName);
				}
				final Pair<Integer, Integer> p = new Legends().getLegendSize(ls);
				String styleTitle = style.getTitle();
				String title = styleTitle != null && !"".equals(styleTitle) ? styleTitle : styleName;
				styleWriter.writeStyle(writer, styleName, title, p, name, style);
			}
		}
	}

	private void writeScaleHint(final XMLStreamWriter writer, DoublePair hint) throws XMLStreamException {
		if (hint == null) {
			return;
		}
		if (!hint.first.isInfinite() || !hint.second.isInfinite()) {
			writer.writeStartElement("ScaleHint");
			writer.writeAttribute("min",
					scaleFormat.format(hint.first.isInfinite() ? MIN_VALUE : calculateScaleHint(hint.first)));
			writer.writeAttribute("max",
					scaleFormat.format(hint.second.isInfinite() ? MAX_VALUE : calculateScaleHint(hint.second)));
			writer.writeEndElement();
		}
	}

	private void writeOnlineResource(final XMLStreamWriter writer, final String url) throws XMLStreamException {
		if (url != null) {
			writer.writeEmptyElement("OnlineResource");
			writer.writeNamespace(XLINK_PREFIX, XLNNS);
			writer.writeAttribute(XLNNS, "type", "simple");
			writer.writeAttribute(XLNNS, "href", url);
		}
	}

	private double calculateScaleHint(double scaleDenominator) {
		double pixelSize = 0.00028;
		return Math.sqrt(Math.pow((scaleDenominator * pixelSize), 2) * 2);
	}

}
