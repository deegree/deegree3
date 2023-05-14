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
package org.deegree.protocol.wms.client;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.deegree.protocol.i18n.Messages.get;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.ows.metadata.OperationsMetadata;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.ows.metadata.domain.Domain;
import org.deegree.commons.ows.metadata.operation.DCP;
import org.deegree.commons.ows.metadata.operation.Operation;
import org.deegree.commons.struct.Tree;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.metadata.MetadataUrl;
import org.deegree.protocol.ows.capabilities.OWSCapabilitiesAdapter;
import org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public abstract class WMSCapabilitiesAdapter extends XMLAdapter implements OWSCapabilitiesAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(WMSCapabilitiesAdapter.class);

	private Tree<LayerMetadata> layerTree;

	private List<String> namedLayers;

	private Map<WMSRequestType, LinkedList<String>> operationToFormats = new HashMap<WMSRequestType, LinkedList<String>>();

	private Map<String, Envelope> layerNameToLatLonBoundingBox = new HashMap<String, Envelope>();

	private Map<String, Map<String, Envelope>> layerNameToCRSToBoundingBox = new HashMap<String, Map<String, Envelope>>();

	private Map<String, LinkedList<String>> layerToCRS = new HashMap<String, LinkedList<String>>();

	/**
	 * @param request
	 * @return the image formats defined for the request, or null, if request is not
	 * supported
	 */
	public LinkedList<String> getFormats(WMSRequestType request) {
		if (!isOperationSupported(request)) {
			return null;
		}
		return operationToFormats.get(request);
	}

	private Map<WMSRequestType, LinkedList<String>> parseFormats(OperationsMetadata operationsMetadata) {
		Map<WMSRequestType, LinkedList<String>> opToFormats = new HashMap<WMSRequestType, LinkedList<String>>();
		if (operationsMetadata == null) {
			return opToFormats;
		}
		for (Operation operation : operationsMetadata.getOperation()) {
			String operationName = operation.getName();
			XPath xp = new XPath("//" + getPrefix() + operationName + "/" + getPrefix() + "Format", nsContext);
			LinkedList<String> formats = new LinkedList<String>();
			Object res = evaluateXPath(xp, getRootElement());
			if (res instanceof List<?>) {
				for (Object o : (List<?>) res) {
					formats.add(((OMElement) o).getText());
				}
			}
			opToFormats.put(WMSRequestType.valueOf(operationName), formats);
		}
		return opToFormats;
	}

	/**
	 * Use parseOperationsMetadata().getGetUrls( request.name() ) or
	 * parseOperationsMetadata().getPostUrls( request.name() ) instead
	 * @param request
	 * @param get true means HTTP GET, false means HTTP POST
	 * @return the address, or null, if not defined or request unavailable
	 */
	public String getAddress(WMSRequestType request, boolean get) {
		if (!isOperationSupported(request)) {
			return null;
		}
		List<URL> urls;
		if (get) {
			urls = parseOperationsMetadata().getGetUrls(request.name());
		}
		else {
			urls = parseOperationsMetadata().getPostUrls(request.name());
		}
		return urls.size() > 0 ? urls.get(0).toExternalForm() : null;
	}

	/**
	 * @param srs the name of the CRS, must not be <code>null</code>
	 * @param layer the layer name, must not be <code>null</code>
	 * @return the envelope, or null, if none was found
	 */
	public Envelope getBoundingBox(String srs, String layer) {
		Map<String, Envelope> crsToBBox = layerNameToCRSToBoundingBox.get(layer);
		if (crsToBBox != null) {
			return crsToBBox.get(srs);
		}
		return null;
	}

	private Map<String, Map<String, Envelope>> parseBoundingBoxes() {
		Map<String, Map<String, Envelope>> layerToCRSToBBox = new HashMap<String, Map<String, Envelope>>(
				namedLayers.size());
		for (String layer : namedLayers) {
			layerToCRSToBBox.put(layer, parseBoundingBoxes(layer));
		}
		return layerToCRSToBBox;
	}

	private Map<String, Envelope> parseBoundingBoxes(String layer) {
		Map<String, Envelope> crsToBBox = new HashMap<String, Envelope>();

		OMElement elem = getLayerElement(layer);
		while (elem != null && elem.getLocalName().equals("Layer")) {
			List<OMElement> bboxes = getElements(elem, new XPath(getPrefix() + "BoundingBox", nsContext));
			for (OMElement bbox : bboxes) {
				String crs = getNodeAsString(bbox, new XPath("@" + getLayerCRSElementName(), nsContext), null);
				if (crs != null && !crsToBBox.containsKey(crs)) {
					try {
						double minx = Double.parseDouble(bbox.getAttributeValue(new QName("minx")));
						double miny = Double.parseDouble(bbox.getAttributeValue(new QName("miny")));
						double maxx = Double.parseDouble(bbox.getAttributeValue(new QName("maxx")));
						double maxy = Double.parseDouble(bbox.getAttributeValue(new QName("maxy")));
						crsToBBox.put(crs, new GeometryFactory().createEnvelope(minx, miny, maxx, maxy,
								CRSManager.getCRSRef(crs)));
					}
					catch (NumberFormatException nfe) {
						LOG.warn(get("WMSCLIENT.SERVER_INVALID_NUMERIC_VALUE", nfe.getLocalizedMessage()));
					}
				}
			}
			elem = (OMElement) elem.getParent();
		}
		return crsToBBox;
	}

	/**
	 * @return the names of all layers that have a name
	 */
	public List<String> getNamedLayers() {
		return namedLayers;
	}

	private List<String> parseNamedLayers() {
		return asList(getNodesAsStrings(getRootElement(),
				new XPath("//" + getPrefix() + "Layer/" + getPrefix() + "Name", nsContext)));
	}

	/**
	 * @param name the layer name, must not be <code>null</code>
	 * @return true, if the WMS advertises a layer with that name
	 */
	public boolean hasLayer(String name) {
		return getLayer(name) != null;
	}

	/**
	 * @param layer the layer name, must not be <code>null</code>
	 * @return all coordinate system names, also inherited ones
	 */
	public LinkedList<String> getCoordinateSystems(String layer) {
		return layerToCRS.get(layer);
	}

	private Map<String, LinkedList<String>> parseCoordinateSystems() {
		Map<String, LinkedList<String>> layerTorCRS = new HashMap<String, LinkedList<String>>();
		for (String layer : namedLayers) {
			LinkedList<String> crss = new LinkedList<String>();
			OMElement elem = getLayerElement(layer);
			String crsElementName = getPrefix() + getLayerCRSElementName();
			List<OMElement> es = getElements(elem, new XPath(crsElementName, nsContext));
			while ((elem = (OMElement) elem.getParent()).getLocalName().equals("Layer")) {
				es.addAll(getElements(elem, new XPath(crsElementName, nsContext)));
			}
			for (OMElement e : es) {
				if (!crss.contains(e.getText())) {
					crss.add(e.getText());
				}
			}
			layerTorCRS.put(layer, crss);
		}
		return layerTorCRS;
	}

	/**
	 * @param name the name of the layer, must not be <code>null</code>
	 * @return the {@link LayerMetadata} of the layer with the given name or null, if no
	 * layer with this name exists
	 */
	public LayerMetadata getLayer(String name) {
		return getLayer(name, layerTree);
	}

	private LayerMetadata getLayer(String name, Tree<LayerMetadata> layerTree) {
		if (name.equals(layerTree.value.getName())) {
			return layerTree.value;
		}
		for (Tree<LayerMetadata> tree : layerTree.children) {
			LayerMetadata result = getLayer(name, tree);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * @return return the elemnt name of the layer csr (without prefix)
	 */
	protected abstract String getLayerCRSElementName();

	/**
	 * @param layer the layer name, must not be <code>null</code>
	 * @return the envelope, or null, if none was found
	 */
	public Envelope getLatLonBoundingBox(String layer) {
		return layerNameToLatLonBoundingBox.get(layer);
	}

	protected abstract Envelope parseLatLonBoundingBox(OMElement elem);

	/**
	 * @param layers
	 * @return a merged envelope of all the layer's envelopes
	 */
	public Envelope getLatLonBoundingBox(List<String> layers) {
		Envelope res = null;

		for (String name : layers) {
			if (res == null) {
				res = getLatLonBoundingBox(name);
			}
			else {
				res = res.merge(getLatLonBoundingBox(name));
			}
		}

		return res;
	}

	private Map<String, Envelope> parseLatLonBoxes() {
		Map<String, Envelope> layerToLLBBox = new HashMap<String, Envelope>();
		for (String layer : namedLayers) {
			layerToLLBBox.put(layer, parseLatLonBoundingBox(getLayerElement(layer)));
		}
		return layerToLLBBox;
	}

	public Tree<LayerMetadata> getLayerTree() {
		return layerTree;
	}

	private Tree<LayerMetadata> parseLayers() {
		Tree<LayerMetadata> tree = new Tree<LayerMetadata>();
		OMElement lay = getElement(getRootElement(),
				new XPath("//" + getPrefix() + "Capability/" + getPrefix() + "Layer", nsContext));
		tree.value = extractMetadata(lay);
		buildLayerTree(tree, lay);
		return tree;
	}

	private void buildLayerTree(Tree<LayerMetadata> node, OMElement lay) {
		for (OMElement l : getElements(lay, new XPath(getPrefix() + "Layer", nsContext))) {
			Tree<LayerMetadata> child = new Tree<LayerMetadata>();
			child.value = extractMetadata(l);
			node.children.add(child);
			buildLayerTree(child, l);
		}
	}

	private LayerMetadata extractMetadata(OMElement lay) {
		String name = getNodeAsString(lay, new XPath(getPrefix() + "Name", nsContext), null);
		String title = getNodeAsString(lay, new XPath(getPrefix() + "Title", nsContext), null);
		List<Pair<String, String>> ids = parseIdentifiers(lay);
		List<Pair<String, String>> authorities = parseAuthorities(lay);
		String abstract_ = getNodeAsString(lay, new XPath(getPrefix() + "Abstract", nsContext), null);
		List<Pair<List<LanguageString>, CodeType>> keywords = null;
		OMElement kwlist = getElement(lay, new XPath(getPrefix() + "KeywordList", nsContext));
		if (kwlist != null) {
			keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>();
			Pair<List<LanguageString>, CodeType> p = new Pair<List<LanguageString>, CodeType>();
			p.first = new ArrayList<LanguageString>();
			keywords.add(p);
			String[] kws = getNodesAsStrings(kwlist, new XPath(getPrefix() + "Keyword", nsContext));
			for (String kw : kws) {
				p.first.add(new LanguageString(kw, null));
			}
		}

		Description desc = new Description(null, null, null, null);
		desc.setTitles(singletonList(new LanguageString(title, null)));
		if (abstract_ != null) {
			desc.setAbstracts(singletonList(new LanguageString(abstract_, null)));
		}
		desc.setKeywords(keywords);

		// use first envelope that we can find
		Envelope envelope = null;
		List<ICRS> crsList = new ArrayList<ICRS>();
		if (name != null) {
			envelope = getLatLonBoundingBox(name);
			for (String crs : getCoordinateSystems(name)) {
				if (envelope != null) {
					break;
				}
				envelope = getBoundingBox(crs, name);
			}
			for (String crs : getCoordinateSystems(name)) {
				crsList.add(CRSManager.getCRSRef(crs, true));
			}
		}

		SpatialMetadata smd = new SpatialMetadata(envelope, crsList);
		LayerMetadata md = new LayerMetadata(ids, authorities, name, desc, smd);

		String casc = lay.getAttributeValue(new QName("cascaded"));
		if (casc != null) {
			try {
				md.setCascaded(Integer.parseInt(casc));
			}
			catch (NumberFormatException nfe) {
				md.setCascaded(1);
			}
		}
		md.setQueryable(getNodeAsBoolean(lay, new XPath("@queryable"), false));

		md.setMetadataUrls(parseMetadataUrls(lay));

		Map<String, Style> styles = new HashMap<String, Style>();
		List<OMElement> styleEls = getElements(lay, new XPath(getPrefix() + "Style", nsContext));
		for (OMElement styleEl : styleEls) {
			String styleName = getRequiredNodeAsString(styleEl, new XPath(getPrefix() + "Name", nsContext));
			try {
				styles.put(styleName, parseStyle(styleName, styleEl));
			}
			catch (MalformedURLException e) {
				LOG.info("Could not parse style with name {} from layer {}", styleName, name);
			}
		}
		md.setStyles(styles);
		return md;
	}

	private List<Pair<String, String>> parseAuthorities(OMElement lay) {
		List<Pair<String, String>> authorities = new ArrayList<Pair<String, String>>();
		List<OMElement> authorityElements = getElements(lay, new XPath(getPrefix() + "AuthorityURL", nsContext));
		for (OMElement authorityElement : authorityElements) {
			String authority = getNodeAsString(authorityElement, new XPath("@name"), null);
			String authorityUrl = getNodeAsString(authorityElement,
					new XPath(getPrefix() + "OnlineResource/@xlink:href", nsContext), null);
			authorities.add(new Pair<String, String>(authority, authorityUrl));
		}
		return authorities;
	}

	private List<Pair<String, String>> parseIdentifiers(OMElement lay) {
		List<Pair<String, String>> identifiers = new ArrayList<Pair<String, String>>();
		List<OMElement> identiferElements = getElements(lay, new XPath(getPrefix() + "Identifier", nsContext));
		for (OMElement identifierElement : identiferElements) {
			String id = identifierElement.getText();
			if (id != null && id.length() > 0) {
				String authority = getNodeAsString(identifierElement, new XPath("@authority"), null);
				identifiers.add(new Pair<String, String>(id, authority));
			}
		}
		return identifiers;
	}

	private Style parseStyle(String styleName, OMElement styleEl) throws MalformedURLException {
		String url = getNodeAsString(styleEl,
				new XPath(getPrefix() + "LegendURL/" + getPrefix() + "OnlineResource/@xlink:href", nsContext), null);
		Style style = new Style();
		style.setName(styleName);
		if (url != null) {
			style.setLegendURL(new URL(url));
		}
		return style;
	}

	private List<MetadataUrl> parseMetadataUrls(OMElement lay) {
		List<MetadataUrl> metadataUrls = new ArrayList<MetadataUrl>();
		List<OMElement> mdUrlEls = getElements(lay, new XPath(getPrefix() + "MetadataURL", nsContext));
		for (OMElement mdUrlEl : mdUrlEls) {
			String type = getRequiredNodeAsString(mdUrlEl, new XPath("@type", nsContext));
			String format = getRequiredNodeAsString(mdUrlEl, new XPath(getPrefix() + "Format", nsContext));
			String url = getRequiredNodeAsString(mdUrlEl,
					new XPath(getPrefix() + "OnlineResource/@xlink:href", nsContext));
			try {
				metadataUrls.add(new MetadataUrl(format, type, new URL(url)));
			}
			catch (MalformedURLException e) {
				LOG.info("Could not parse MetadataUrl {}", url);
			}
		}
		return metadataUrls;
	}

	protected OMElement getLayerElement(String layer) {
		return getElement(getRootElement(),
				new XPath("//" + getPrefix() + "Layer[" + getPrefix() + "Name = '" + layer + "']", nsContext));
	}

	/**
	 * @param request
	 * @return true, if an according section was found in the capabilities
	 */
	public boolean isOperationSupported(WMSRequestType request) {
		return operationToFormats.containsKey(request);
	}

	@Override
	public List<String> parseLanguages() throws XMLParsingException {
		return null;
	}

	@Override
	public ServiceProvider parseServiceProvider() throws XMLParsingException {
		throw new UnsupportedOperationException("ServiceProvider is not parsed, yet.");
	}

	@Override
	public ServiceIdentification parseServiceIdentification() throws XMLParsingException {
		OMElement serviceIdEl = getElement(getRootElement(), new XPath(getPrefix() + "Service", nsContext));
		if (serviceIdEl == null) {
			return null;
		}

		String title = getRequiredNodeAsString(serviceIdEl, new XPath(getPrefix() + "Title", nsContext));
		List<LanguageString> titles = singletonList(new LanguageString(title, null));

		String name = getRequiredNodeAsString(serviceIdEl, new XPath(getPrefix() + "Name", nsContext));

		String _abstract = getNodeAsString(serviceIdEl, new XPath(getPrefix() + "Abstract", nsContext), null);
		List<LanguageString> abstracts = _abstract != null ? singletonList(new LanguageString(_abstract, null))
				: new ArrayList<LanguageString>();

		String[] keywordValues = getNodesAsStrings(serviceIdEl,
				new XPath(getPrefix() + "KeywordList/" + getPrefix() + "Keyword", nsContext));
		List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>(
				keywordValues.length);

		List<LanguageString> keywordLS = new ArrayList<LanguageString>();
		for (String keyword : keywordValues) {
			if (keyword != null) {
				keywordLS.add(new LanguageString(keyword, null));
			}
		}
		keywords.add(new Pair<List<LanguageString>, CodeType>(keywordLS, null));

		CodeType serviceType = new CodeType("WMS");

		Version version = getNodeAsVersion(serviceIdEl, new XPath("version", nsContext), getServiceVersion());
		List<Version> serviceTypeVersions = singletonList(version);

		String fees = getNodeAsString(serviceIdEl, new XPath(getPrefix() + "Fees", nsContext), null);

		String constraintValues = getNodeAsString(serviceIdEl, new XPath(getPrefix() + "AccessConstraints", nsContext),
				null);
		List<String> constraints = constraintValues != null ? singletonList(constraintValues) : new ArrayList<String>();

		return new ServiceIdentification(name, titles, abstracts, keywords, serviceType, serviceTypeVersions,
				new ArrayList<String>(), fees, constraints);

	}

	@Override
	public OperationsMetadata parseOperationsMetadata() throws XMLParsingException {

		OMElement requestEl = getElement(getRootElement(),
				new XPath(getPrefix() + "Capability/" + getPrefix() + "Request", nsContext));
		if (requestEl == null) {
			return null;
		}
		XPath xpath = new XPath("./*", nsContext);
		List<OMElement> opEls = getElements(requestEl, xpath);
		List<Operation> operations = new ArrayList<Operation>(opEls.size());
		if (opEls != null) {
			for (OMElement opEl : opEls) {
				Operation op = parseOperation(opEl);
				operations.add(op);
			}
		}
		return new OperationsMetadata(operations, new ArrayList<Domain>(), new ArrayList<Domain>(),
				new ArrayList<OMElement>());
	}

	private Operation parseOperation(OMElement opEl) {

		String name = opEl.getLocalName();

		XPath xpath = new XPath(getPrefix() + "DCPType", nsContext);
		List<OMElement> dcpEls = getElements(opEl, xpath);
		List<DCP> dcps = new ArrayList<DCP>(dcpEls.size());
		if (dcpEls != null) {
			for (OMElement dcpEl : dcpEls) {
				DCP dcp = parseDCP(dcpEl);
				dcps.add(dcp);
			}
		}
		return new Operation(name, dcps, new ArrayList<Domain>(), new ArrayList<Domain>(), new ArrayList<OMElement>());
	}

	private DCP parseDCP(OMElement dcpEl) {
		XPath xpath = new XPath(getPrefix() + "HTTP/" + getPrefix() + "Get", nsContext);

		List<OMElement> getEls = getElements(dcpEl, xpath);
		List<Pair<URL, List<Domain>>> getEndpoints = new ArrayList<Pair<URL, List<Domain>>>(getEls.size());
		if (getEls != null) {
			for (OMElement getEl : getEls) {
				xpath = new XPath(getPrefix() + "OnlineResource/@xlink:href", nsContext);
				URL href = getNodeAsURL(getEl, xpath, null);
				getEndpoints.add(new Pair<URL, List<Domain>>(href, new ArrayList<Domain>()));
			}
		}

		xpath = new XPath(getPrefix() + "HTTP/" + getPrefix() + "Post", nsContext);
		List<OMElement> postEls = getElements(dcpEl, xpath);
		List<Pair<URL, List<Domain>>> postEndpoints = new ArrayList<Pair<URL, List<Domain>>>(postEls.size());
		if (postEls != null) {
			for (OMElement postEl : postEls) {
				xpath = new XPath(getPrefix() + "OnlineResource/@xlink:href", nsContext);
				URL href = getNodeAsURL(postEl, xpath, null);

				postEndpoints.add(new Pair<URL, List<Domain>>(href, new ArrayList<Domain>()));
			}
		}

		return new DCP(getEndpoints, postEndpoints);
	}

	/**
	 * @param prefix of the element containging the extended capabilities, may be
	 * <code>null</code>
	 * @param localName localName of the element containing the extended capabilities,
	 * never <code>null</code>
	 * @param namespaceUri of the element containging the extended capabilities, may be
	 * <code>null</code>
	 * @return the {@link OMElement} containing the extended capabilities, may be
	 * <code>null</code> if no extended capabilities exists
	 */
	public OMElement getExtendedCapabilities(String prefix, String localName, String namespaceUri) {
		if (prefix != null)
			nsContext.addNamespace(prefix, namespaceUri);
		prefix = prefix != null ? (prefix + ":") : "";
		String xpath = getExtendedCapabilitiesRootXPath() + "/" + prefix + localName;
		return getElement(rootElement, new XPath(xpath, nsContext));
	}

	protected abstract String getExtendedCapabilitiesRootXPath();

	protected abstract Version getServiceVersion();

	protected abstract String getPrefix();

	public void parseWMSSpecificCapabilities(OperationsMetadata operationsMetadata) {
		namedLayers = parseNamedLayers();
		operationToFormats = parseFormats(operationsMetadata);
		layerToCRS = parseCoordinateSystems();
		layerNameToCRSToBoundingBox = parseBoundingBoxes();
		layerNameToLatLonBoundingBox = parseLatLonBoxes();
		layerTree = parseLayers();
	}

}