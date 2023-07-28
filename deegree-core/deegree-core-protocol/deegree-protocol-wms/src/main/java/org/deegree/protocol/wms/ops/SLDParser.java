/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.protocol.wms.ops;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;
import static org.deegree.gml.GMLInputFactory.createGMLStreamReader;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.layer.dims.Dimension.parseTyped;
import static org.deegree.protocol.wms.ops.GetMap.parseDimensionValues;
import static org.slf4j.LoggerFactory.getLogger;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.DynamicAppSchema;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.MatchAction;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.ResourceId;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.Or;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.gml.GMLStreamReader;
import org.deegree.layer.LayerRef;
import org.deegree.layer.persistence.inline.InlineFeatureLayer;
import org.deegree.protocol.wms.sld.StyleContainer;
import org.deegree.protocol.wms.sld.StylesContainer;
import org.deegree.style.StyleRef;
import org.deegree.style.se.parser.SymbologyParser;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;

/**
 * <code>SLDParser</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class SLDParser {

	private static final Logger LOG = getLogger(SLDParser.class);

	/**
	 * @param in
	 * @param service
	 * @param gm filters will be added to this GetMap instance
	 * @return a list of layers parsed from SLD
	 * @throws XMLStreamException
	 * @throws OWSException
	 * @throws ParseException
	 */
	@Deprecated
	public static Triple<LinkedList<LayerRef>, LinkedList<StyleRef>, LinkedList<OperatorFilter>> parse(
			XMLStreamReader in, RequestBase gm) throws XMLStreamException, OWSException, ParseException {
		StylesContainer styleInformations = parse(in);

		for (Pair<String, List<?>> dimension : styleInformations.getDimensions()) {
			gm.addDimensionValue(dimension.first, dimension.second);
		}

		LinkedList<LayerRef> layerRefs = new LinkedList<LayerRef>();
		LinkedList<StyleRef> styleRefs = new LinkedList<StyleRef>();
		LinkedList<OperatorFilter> filters = new LinkedList<OperatorFilter>();

		List<StyleContainer> styles = styleInformations.getStyles();
		for (StyleContainer styleInformation : styles) {
			layerRefs.add(styleInformation.getLayerRef());
			styleRefs.add(styleInformation.getStyleRef());
			filters.add(styleInformation.getFilter());
		}
		return new Triple<LinkedList<LayerRef>, LinkedList<StyleRef>, LinkedList<OperatorFilter>>(layerRefs, styleRefs,
				filters);
	}

	/**
	 * @param in the StyledLayerDescriptor to parse, never <code>null</code>
	 * @return the parsed SLD, never <code>null</code>
	 * @throws XMLStreamException if an error occurred during parsing
	 * @throws OWSException if an error occurred during parsing
	 * @throws ParseException if an error occurred during parsing
	 */
	public static StylesContainer parse(XMLStreamReader in) throws XMLStreamException, OWSException, ParseException {
		while (!in.isStartElement() || in.getLocalName() == null
				|| !(in.getLocalName().equals("NamedLayer") || in.getLocalName().equals("UserLayer"))) {
			in.nextTag();
		}

		StylesContainer stylesContainer = new StylesContainer();

		while (in.getLocalName().equals("NamedLayer") || in.getLocalName().equals("UserLayer")) {
			if (in.getLocalName().equals("NamedLayer")) {
				in.nextTag();

				in.require(START_ELEMENT, null, "Name");
				String layerName = in.getElementText();

				in.nextTag();

				LOG.debug("Extracted layer '{}' from SLD.", layerName);

				// skip description
				if (in.getLocalName().equals("Description")) {
					skipElement(in);
				}

				OperatorFilter operatorFilter = null;
				if (in.getLocalName().equals("LayerFeatureConstraints")) {

					operatorFilter = parseLayerFeatureConstraints(in, stylesContainer);

					in.nextTag();
				}

				if (in.getLocalName().equals("NamedStyle")) {
					in.nextTag();
					String name = in.getElementText();

					StyleContainer styleContainer = new StyleContainer(new LayerRef(layerName), new StyleRef(name),
							operatorFilter);
					stylesContainer.addStyle(styleContainer);

					in.nextTag(); // out of name
					in.nextTag(); // out of named style
				}

				if (in.getLocalName().equals("UserStyle")) {

					while (!(in.isEndElement() && in.getLocalName().equals("UserStyle"))) {
						in.nextTag();

						// TODO skipped
						if (in.getLocalName().equals("Name")) {
							in.getElementText();
						}

						// TODO skipped
						if (in.getLocalName().equals("Description")) {
							skipElement(in);
						}

						// TODO skipped
						if (in.getLocalName().equals("Title")) {
							in.getElementText();
						}

						// TODO skipped
						if (in.getLocalName().equals("Abstract")) {
							in.getElementText();
						}

						// TODO skipped
						if (in.getLocalName().equals("IsDefault")) {
							in.getElementText();
						}

						if (in.getLocalName().equals("FeatureTypeStyle") || in.getLocalName().equals("CoverageStyle")
								|| in.getLocalName().equals("OnlineResource")) {
							Style style = SymbologyParser.INSTANCE.parseFeatureTypeOrCoverageStyle(in);

							StyleContainer styleContainer = new StyleContainer(new LayerRef(layerName),
									new StyleRef(style), operatorFilter);
							stylesContainer.addStyle(styleContainer);
						}
					}

					in.nextTag();

				}

				in.nextTag();
			}
			else {
				// UserLayer
				in.nextTag();

				in.require(START_ELEMENT, null, "Name");
				// TODO skipped
				in.getElementText();

				in.nextTag();

				// TODO skipped
				if (in.getLocalName().equals("Description")) {
					skipElement(in);
				}

				FeatureCollection inlineFeatures = null;
				DynamicAppSchema inlineAppSchema = null;
				if (in.getLocalName().equals("InlineFeature")) {
					in.nextTag();
					inlineAppSchema = new DynamicAppSchema();
					GMLStreamReader reader = createGMLStreamReader(GML_31, in);
					reader.setApplicationSchema(inlineAppSchema);
					try {
						inlineFeatures = reader.readFeatureCollection();
						LOG.debug("Has read {} inline features", inlineFeatures.size());
					}
					catch (XMLParsingException | UnknownCRSException e) {
						throw new OWSException("Could not read inlined FeatureCollection", e,
								OWSException.NO_APPLICABLE_CODE);
					}

					in.nextTag();
					in.nextTag();
				}

				if (in.getLocalName().equals("RemoteOWS")) {
					throw new OWSException("Only UserLayer requests with InlineFeature are currently supported.",
							OWSException.NO_APPLICABLE_CODE);
				}

				OperatorFilter operatorFilter = null;
				if (in.getLocalName().equals("LayerFeatureConstraints")) {

					operatorFilter = parseLayerFeatureConstraints(in, stylesContainer);

					in.nextTag();
				}

				if (in.getLocalName().equals("LayerCoverageConstraints")) {
					throw new OWSException(
							"Only UserLayer requests with LayerFeatureConstraints are currently supported.",
							OWSException.NO_APPLICABLE_CODE);
				}

				if (in.getLocalName().equals("UserStyle")) {

					while (!(in.isEndElement() && in.getLocalName().equals("UserStyle"))) {
						in.nextTag();

						// TODO skipped
						if (in.getLocalName().equals("Name")) {
							in.getElementText();
						}

						// TODO skipped
						if (in.getLocalName().equals("Description")) {
							skipElement(in);
						}

						// TODO skipped
						if (in.getLocalName().equals("Title")) {
							in.getElementText();
						}

						// TODO skipped
						if (in.getLocalName().equals("Abstract")) {
							in.getElementText();
						}

						// TODO skipped
						if (in.getLocalName().equals("IsDefault")) {
							in.getElementText();
						}

						if (in.getLocalName().equals("FeatureTypeStyle") || in.getLocalName().equals("CoverageStyle")
								|| in.getLocalName().equals("OnlineResource")) {
							Style style = SymbologyParser.INSTANCE.parseFeatureTypeOrCoverageStyle(in);

							InlineFeatureLayer layer = new InlineFeatureLayer(inlineFeatures, inlineAppSchema, 10000,
									style);

							StyleContainer styleContainer = new StyleContainer(new LayerRef(layer), new StyleRef(style),
									operatorFilter);
							stylesContainer.addStyle(styleContainer);
						}
					}

					in.nextTag();

				}

				in.nextTag();
			}
		}

		return stylesContainer;
	}

	private static OperatorFilter parseLayerFeatureConstraints(XMLStreamReader in, StylesContainer stylesContainer)
			throws XMLStreamException, OWSException, ParseException {
		OperatorFilter operatorFilter = null;

		while (!(in.isEndElement() && in.getLocalName().equals("LayerFeatureConstraints"))) {
			in.nextTag();

			while (!(in.isEndElement() && in.getLocalName().equals("FeatureTypeConstraint"))) {
				in.nextTag();

				// skip feature type name, it is useless in this context (or is it?) TODO
				if (in.getLocalName().equals("FeatureTypeName")) {
					in.getElementText();
					in.nextTag();
				}

				if (in.getLocalName().equals("Filter")) {
					Filter filter = Filter110XMLDecoder.parse(in);
					if (filter instanceof OperatorFilter) {
						operatorFilter = (OperatorFilter) filter;
					}
					else if (filter instanceof IdFilter) {
						IdFilter idFilter = (IdFilter) filter;
						List<ResourceId> ids = idFilter.getSelectedIds();

						NamespaceBindings nsContext = new NamespaceBindings();
						nsContext.addNamespace("gml", GMLNS);
						ValueReference idReference = new ValueReference("@gml:id", nsContext);

						int idCount = ids.size(), i = 0;
						Operator[] operators = new Operator[idCount];
						for (ResourceId id : ids) {
							operators[i++] = new PropertyIsEqualTo(idReference,
									new Literal<PrimitiveValue>(id.getRid()), Boolean.TRUE, MatchAction.ONE);
						}

						if (idCount == 1) {
							operatorFilter = new OperatorFilter(operators[0]);
						}
						else {
							operatorFilter = new OperatorFilter(new Or(operators));
						}
					}
				}

				if (in.getLocalName().equals("Extent")) {
					in.nextTag();

					in.require(START_ELEMENT, null, "Name");
					String name = in.getElementText().toUpperCase();
					in.nextTag();
					in.require(START_ELEMENT, null, "Value");
					String value = in.getElementText();
					in.nextTag();
					in.require(END_ELEMENT, null, "Extent");

					List<?> list = parseDimensionValues(value, name.toLowerCase());
					if (name.toUpperCase().equals("TIME")) {
						stylesContainer.addDimensionValue("time", (List<?>) parseTyped(list, true));
					}
					else {
						List<?> values = (List<?>) parseTyped(list, false);
						stylesContainer.addDimensionValue(name, values);
					}

				}
			}
			in.nextTag();
		}

		return operatorFilter;
	}

	/**
	 * @param in
	 * @param layerName
	 * @param styleNames
	 * @return the filters defined for the NamedLayer, and the matching styles
	 * @throws XMLStreamException
	 */
	public static Pair<LinkedList<Filter>, LinkedList<StyleRef>> getStyles(XMLStreamReader in, String layerName,
			Map<String, String> styleNames) throws XMLStreamException {
		while (!in.isStartElement() || in.getLocalName() == null
				|| !(in.getLocalName().equals("NamedLayer") || in.getLocalName().equals("UserLayer"))) {
			in.nextTag();
		}

		LinkedList<StyleRef> styles = new LinkedList<StyleRef>();
		LinkedList<Filter> filters = new LinkedList<Filter>();

		while (in.hasNext() && (in.getLocalName().equals("NamedLayer") && !in.isEndElement())
				|| in.getLocalName().equals("UserLayer")) {
			if (in.getLocalName().equals("UserLayer")) {
				skipElement(in);
			}
			if (in.getLocalName().equals("NamedLayer")) {
				in.nextTag();

				in.require(START_ELEMENT, null, "Name");
				String name = in.getElementText();
				if (!name.equals(layerName)) {
					while (!(in.isEndElement() && in.getLocalName().equals("NamedLayer"))) {
						in.next();
					}
					in.nextTag();
					continue;
				}
				in.nextTag();

				// skip description
				if (in.getLocalName().equals("Description")) {
					skipElement(in);
				}

				if (in.getLocalName().equals("LayerFeatureConstraints")) {

					while (!(in.isEndElement() && in.getLocalName().equals("LayerFeatureConstraints"))) {
						in.nextTag();

						while (!(in.isEndElement() && in.getLocalName().equals("FeatureTypeConstraint"))) {
							in.nextTag();

							// TODO use this
							if (in.getLocalName().equals("FeatureTypeName")) {
								in.getElementText();
								in.nextTag();
							}

							if (in.getLocalName().equals("Filter")) {
								filters.add(Filter110XMLDecoder.parse(in));
							}

							if (in.getLocalName().equals("Extent")) {
								// skip extent, does not make sense to parse it here
								skipElement(in);
							}
						}
						in.nextTag();
					}

					in.nextTag();
				}

				if (in.getLocalName().equals("NamedStyle")) {
					// does not make sense to reference a named style when configuring
					// it...
					skipElement(in);
				}

				String styleName = null;

				while (in.hasNext() && in.getLocalName().equals("UserStyle")) {

					while (in.hasNext() && !(in.isEndElement() && in.getLocalName().equals("UserStyle"))) {

						in.nextTag();

						if (in.getLocalName().equals("Name")) {
							styleName = in.getElementText();
							if (!(styleNames.isEmpty() || styleNames.containsKey(styleName))) {
								continue;
							}
						}

						// TODO skipped
						if (in.getLocalName().equals("Description")) {
							skipElement(in);
						}

						// TODO skipped
						if (in.getLocalName().equals("Title")) {
							in.getElementText();
						}

						// TODO skipped
						if (in.getLocalName().equals("Abstract")) {
							in.getElementText();
						}

						// TODO skipped
						if (in.getLocalName().equals("IsDefault")) {
							in.getElementText();
						}

						if (in.getLocalName().equals("FeatureTypeStyle") || in.getLocalName().equals("CoverageStyle")
								|| in.getLocalName().equals("OnlineResource")) {
							Style style = SymbologyParser.INSTANCE.parseFeatureTypeOrCoverageStyle(in);
							if (styleNames.get(styleName) != null) {
								style.setName(styleNames.get(styleName));
							}
							styles.add(new StyleRef(style));
						}

					}
					in.nextTag();

				}

			}
		}

		return new Pair<LinkedList<Filter>, LinkedList<StyleRef>>(filters, styles);
	}

}
