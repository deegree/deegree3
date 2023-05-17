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
package org.deegree.services.wfs.format.gml.request;

import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_SCHEMA_URL;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.xpath.TypedObjectNodeXPathEvaluator;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.GMLFeatureWriter;
import org.deegree.gml.reference.GmlXlinkOptions;
import org.deegree.protocol.wfs.getpropertyvalue.GetPropertyValue;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.i18n.Messages;
import org.deegree.services.wfs.WebFeatureService;
import org.deegree.services.wfs.format.gml.BufferableXMLStreamWriter;
import org.deegree.services.wfs.format.gml.GmlFormat;
import org.deegree.services.wfs.query.QueryAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link GetPropertyValue} requests for the {@link GmlFormat}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class GmlGetPropertyValueHandler extends AbstractGmlRequestHandler {

	private static final Logger LOG = LoggerFactory.getLogger(GmlGetPropertyValueHandler.class);

	/**
	 * Creates a new {@link GmlGetPropertyValueHandler} instance.
	 * @param gmlFormat never <code>null</code>
	 */
	public GmlGetPropertyValueHandler(GmlFormat format) {
		super(format);
	}

	public void doGetPropertyValueResult(GetPropertyValue request, HttpResponseBuffer response) throws Exception {

		LOG.debug("doGetPropertyValue: " + request);

		QueryAnalyzer analyzer = new QueryAnalyzer(Collections.singletonList(request.getQuery()), format.getMaster(),
				format.getMaster().getStoreManager(), options.isCheckAreaOfUse());
		String schemaLocation = getSchemaLocation(request.getVersion(), analyzer.getFeatureTypes());

		GMLVersion gmlVersion = options.getGmlVersion();

		int traverseXLinkDepth = 0;
		String xLinkTemplate = getObjectXlinkTemplate(request.getVersion(), gmlVersion);

		if (request.getResolveParams().getDepth() != null) {
			if ("*".equals(request.getResolveParams().getDepth())) {
				traverseXLinkDepth = -1;
			}
			else {
				try {
					traverseXLinkDepth = Integer.parseInt(request.getResolveParams().getDepth());
				}
				catch (NumberFormatException e) {
					String msg = Messages.get("WFS_TRAVERSEXLINKDEPTH_INVALID", request.getResolveParams().getDepth());
					throw new OWSException(new InvalidParameterValueException(msg));
				}
			}
		}

		// quick check if local references in the output can be ruled out
		boolean localReferencesPossible = localReferencesPossible(analyzer, traverseXLinkDepth);

		String contentType = options.getMimeType();
		XMLStreamWriter xmlStream = WebFeatureService.getXMLResponseWriter(response, contentType, schemaLocation);
		xmlStream = new BufferableXMLStreamWriter(xmlStream, xLinkTemplate);

		GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter(gmlVersion, xmlStream);
		gmlStream.setProjections(analyzer.getProjections());
		gmlStream.setOutputCrs(analyzer.getRequestedCRS());
		gmlStream.setGeometrySimplifier(options.getGeometrySimplifier());
		gmlStream.setCoordinateFormatter(options.getFormatter());
		gmlStream.setGenerateBoundedByForFeatures(options.isGenerateBoundedByForFeatures());
		Map<String, String> prefixToNs = new HashMap<String, String>(
				format.getMaster().getStoreManager().getPrefixToNs());
		prefixToNs.putAll(getFeatureTypeNsPrefixes(analyzer.getFeatureTypes()));
		gmlStream.setNamespaceBindings(prefixToNs);
		GmlXlinkOptions resolveOptions = new GmlXlinkOptions(request.getResolveParams());
		BufferableXMLStreamWriter bufferedStream = (BufferableXMLStreamWriter) xmlStream;
		WfsXlinkStrategy additionalObjects = new WfsXlinkStrategy(bufferedStream, localReferencesPossible,
				xLinkTemplate, resolveOptions);
		gmlStream.setReferenceResolveStrategy(additionalObjects);

		// retrieve and write result features
		int startIndex = 0;
		int maxResults = -1;
		// TODO evaluate if maxResults should have a default / configurable value
		if (request.getPresentationParams().getCount() != null) {
			maxResults = request.getPresentationParams().getCount().intValue();
		}
		if (request.getPresentationParams().getStartIndex() != null) {
			startIndex = request.getPresentationParams().getStartIndex().intValue();
		}

		TypedObjectNodeXPathEvaluator evaluator = new TypedObjectNodeXPathEvaluator();
		GMLFeatureWriter featureWriter = gmlStream.getFeatureWriter();

		// open "wfs:ValueCollection" element
		xmlStream.setPrefix("wfs", WFS_200_NS);
		xmlStream.writeStartElement(WFS_200_NS, "ValueCollection");
		xmlStream.writeNamespace("wfs", WFS_200_NS);
		xmlStream.writeAttribute("timeStamp", getTimestamp());

		if (options.isDisableStreaming()) {
			writeValuesCached(request, analyzer, resolveOptions, xmlStream, startIndex, maxResults, evaluator,
					featureWriter);
		}
		else {
			writeValuesStream(request, analyzer, resolveOptions, xmlStream, startIndex, maxResults, evaluator,
					featureWriter);
		}

		writeAdditionalObjects(gmlStream, additionalObjects, new QName(WFS_200_NS, "member"), request.getVersion());

		// close container element
		xmlStream.writeEndElement();
		xmlStream.flush();

		// append buffered parts of the stream
		if (bufferedStream.hasBuffered()) {
			bufferedStream.appendBufferedXML(gmlStream);
		}
	}

	private void writeValuesStream(GetPropertyValue request, QueryAnalyzer analyzer, GmlXlinkOptions resolveState,
			XMLStreamWriter xmlStream, int startIndex, int maxResults, TypedObjectNodeXPathEvaluator evaluator,
			GMLFeatureWriter featureWriter) throws XMLStreamException, FeatureStoreException, FilterEvaluationException,
			UnknownCRSException, TransformationException {

		xmlStream.writeAttribute("numberMatched", "0");
		xmlStream.writeAttribute("numberReturned", "0");
		xmlStream.writeComment(
				"NOTE: numberReturned/numberMatched attributes should be 'unknown', but this would not validate against the current version of the WFS 2.0 schema (change upcoming). See change request (CR 144): https://portal.opengeospatial.org/files?artifact_id=43925.");

		int numberReturned = 0;
		int valuesSkipped = 0;
		for (Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet()) {
			FeatureStore fs = fsToQueries.getKey();
			Query[] queries = fsToQueries.getValue().toArray(new Query[fsToQueries.getValue().size()]);
			FeatureInputStream rs = fs.query(queries);
			try {
				for (Feature member : rs) {
					if (numberReturned == maxResults) {
						break;
					}
					TypedObjectNode[] values = evaluator.eval(member, request.getValueReference());
					for (TypedObjectNode value : values) {
						if (valuesSkipped < startIndex) {
							valuesSkipped++;
						}
						else {
							xmlStream.writeStartElement(WFS_200_NS, "member");
							featureWriter.export(value, resolveState);
							xmlStream.writeEndElement();
							numberReturned++;
							if (numberReturned == maxResults) {
								break;
							}
						}
					}
				}
			}
			finally {
				rs.close();
			}
		}
	}

	private void writeValuesCached(GetPropertyValue request, QueryAnalyzer analyzer, GmlXlinkOptions resolveState,
			XMLStreamWriter xmlStream, int startIndex, int maxResults, TypedObjectNodeXPathEvaluator evaluator,
			GMLFeatureWriter featureWriter) throws XMLStreamException, FeatureStoreException, FilterEvaluationException,
			UnknownCRSException, TransformationException {

		List<TypedObjectNode> cachedValues = new LinkedList<TypedObjectNode>();
		int numberReturned = 0;
		int valuesSkipped = 0;
		for (Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet()) {
			FeatureStore fs = fsToQueries.getKey();
			Query[] queries = fsToQueries.getValue().toArray(new Query[fsToQueries.getValue().size()]);
			FeatureInputStream rs = fs.query(queries);
			try {
				for (Feature member : rs) {
					if (numberReturned == maxResults) {
						break;
					}
					TypedObjectNode[] values = evaluator.eval(member, request.getValueReference());
					for (TypedObjectNode value : values) {
						if (valuesSkipped < startIndex) {
							valuesSkipped++;
						}
						else {
							cachedValues.add(value);
							numberReturned++;
							if (numberReturned == maxResults) {
								break;
							}
						}
					}
				}
			}
			finally {
				rs.close();
			}
		}

		xmlStream.writeAttribute("numberMatched", "" + numberReturned);
		xmlStream.writeAttribute("numberReturned", "" + numberReturned);

		for (TypedObjectNode value : cachedValues) {
			xmlStream.writeStartElement(WFS_200_NS, "member");
			featureWriter.export(value, resolveState);
			xmlStream.writeEndElement();
		}
	}

	public void doGetPropertyValueHits(GetPropertyValue request, HttpResponseBuffer response)
			throws FeatureStoreException, FilterEvaluationException, IOException, OWSException, XMLStreamException {

		LOG.debug("Performing doGetPropertyValue (HITS) request: " + request);

		QueryAnalyzer analyzer = new QueryAnalyzer(Collections.singletonList(request.getQuery()), format.getMaster(),
				format.getMaster().getStoreManager(), options.isCheckAreaOfUse());
		String contentType = options.getMimeType();
		String schemaLocation = WFS_200_NS + " " + WFS_200_SCHEMA_URL;
		XMLStreamWriter xmlStream = WebFeatureService.getXMLResponseWriter(response, contentType, schemaLocation);

		TypedObjectNodeXPathEvaluator evaluator = new TypedObjectNodeXPathEvaluator();

		int numFeatures = 0;

		for (Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet()) {
			FeatureStore fs = fsToQueries.getKey();
			Query[] queries = fsToQueries.getValue().toArray(new Query[fsToQueries.getValue().size()]);
			FeatureInputStream rs = fs.query(queries);
			try {
				for (Feature member : rs) {
					TypedObjectNode[] values = evaluator.eval(member, request.getValueReference());
					numFeatures += values.length;
				}
			}
			finally {
				LOG.debug("Closing FeatureResultSet (stream)");
				rs.close();
			}
		}

		xmlStream.setPrefix("wfs", WFS_200_NS);
		xmlStream.writeStartElement(WFS_200_NS, "ValueCollection");
		xmlStream.writeNamespace("wfs", WFS_200_NS);
		xmlStream.writeAttribute("timeStamp", getTimestamp());
		xmlStream.writeAttribute("numberMatched", Integer.toString(numFeatures));
		xmlStream.writeAttribute("numberReturned", "0");
		xmlStream.writeEndElement();
		xmlStream.flush();
	}

}
