/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.protocol.wfs.getfeature.xml;

import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ResolveParams;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.filter.Filter;
import org.deegree.filter.expression.Function;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.projection.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.xml.Filter100XMLDecoder;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.protocol.wfs.WFSConstants;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.query.FilterQuery;
import org.deegree.protocol.wfs.query.Query;
import org.deegree.protocol.wfs.query.StandardPresentationParams;
import org.deegree.protocol.wfs.query.xml.QueryXMLAdapter;

/**
 * Adapter between XML <code>GetFeature</code> requests and {@link GetFeature} objects.
 * <p>
 * Supported WFS versions:
 * <ul>
 * <li>1.0.0</li>
 * <li>1.1.0</li>
 * <li>2.0.0</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class GetFeatureXMLAdapter extends QueryXMLAdapter {

	/**
	 * Parses a WFS <code>GetFeature</code> document into a {@link GetFeature} object. *
	 * <p>
	 * Supported WFS versions:
	 * <ul>
	 * <li>1.0.0</li>
	 * <li>1.1.0</li>
	 * <li>2.0.0</li>
	 * </ul>
	 * </p>
	 * @return parsed {@link GetFeature} request, never <code>null</code>
	 * @throws Exception
	 * @throws XMLParsingException if a syntax error occurs in the XML
	 * @throws MissingParameterException if the request version is unsupported
	 * @throws InvalidParameterValueException if a parameter contains a syntax error
	 */
	public GetFeature parse() throws Exception {

		Version version = determineVersion110Safe();

		GetFeature result = null;
		if (VERSION_100.equals(version)) {
			result = parse100();
		}
		else if (VERSION_110.equals(version)) {
			result = parse110();
		}
		else if (VERSION_200.equals(version)) {
			result = parse200();
		}
		else {
			String msg = "Version '" + version + "' is not supported. Supported versions are 1.0.0, 1.1.0 and 2.0.0.";
			throw new Exception(msg);
		}
		return result;
	}

	/**
	 * Parses a WFS 1.0.0 <code>GetFeature</code> document into a {@link GetFeature}
	 * object.
	 * @return a GetFeature instance
	 */
	public GetFeature parse100() {

		String handle = getNodeAsString(rootElement, new XPath("@handle", nsContext), null);

		StandardPresentationParams presentationParams = parseStandardPresentationParameters100(rootElement);

		List<OMElement> queryElements = getRequiredElements(rootElement, new XPath("*", nsContext));
		// check if all child elements are 'wfs:Query' elements (required for CITE)
		for (OMElement omElement : queryElements) {
			if (!new QName(WFSConstants.WFS_NS, "Query").equals(omElement.getQName())) {
				String msg = "Child element '" + omElement.getQName() + "' is not allowed.";
				throw new XMLParsingException(this, omElement, msg);
			}
		}

		List<Query> queries = new ArrayList<Query>();

		for (OMElement queryEl : queryElements) {
			List<PropertyName> propNames = new ArrayList<PropertyName>();
			List<OMElement> propertyNameElements = getElements(queryEl, new XPath("ogc:PropertyName", nsContext));

			for (OMElement propertyNameEl : propertyNameElements) {
				ValueReference propertyName = new ValueReference(propertyNameEl.getText(),
						getNamespaceContext(propertyNameEl));
				propNames.add(new PropertyName(propertyName, null, null));
			}

			Filter filter = null;
			OMElement filterEl = queryEl.getFirstChildWithName(new QName(OGCNS, "Filter"));
			if (filterEl != null) {
				try {
					// TODO remove usage of wrapper (necessary at the moment to work
					// around problems with AXIOM's
					// XMLStreamReader)
					XMLStreamReader xmlStream = new XMLStreamReaderWrapper(filterEl.getXMLStreamReaderWithoutCaching(),
							null);
					// skip START_DOCUMENT
					xmlStream.nextTag();

					filter = Filter100XMLDecoder.parse(xmlStream);
				}
				catch (XMLStreamException e) {
					e.printStackTrace();
					throw new XMLParsingException(this, filterEl, e.getMessage());
				}
			}

			String queryHandle = getNodeAsString(queryEl, new XPath("@handle", nsContext), null);

			String typeNameStr = getRequiredNodeAsString(queryEl, new XPath("@typeName", nsContext));
			TypeName[] typeNames = TypeName.valuesOf(queryEl, typeNameStr);

			String featureVersion = getNodeAsString(queryEl, new XPath("@featureVersion", nsContext), null);

			// convert some lists to arrays to conform the FilterQuery constructor
			// signature
			PropertyName[] propNamesArray = new PropertyName[propNames.size()];
			propNames.toArray(propNamesArray);

			// build Query
			Query filterQuery = new FilterQuery(queryHandle, typeNames, featureVersion, null, propNamesArray, null,
					filter);
			queries.add(filterQuery);
		}

		return new GetFeature(VERSION_100, handle, presentationParams, null, queries);
	}

	/**
	 * Parses a WFS 1.1.0 <code>GetFeature</code> document into a {@link GetFeature}
	 * object.
	 * @return a GetFeature instance
	 */
	public GetFeature parse110() {

		String handle = getNodeAsString(rootElement, new XPath("@handle", nsContext), null);

		StandardPresentationParams presentationParams = parseStandardPresentationParameters110(rootElement);
		ResolveParams resolveParams = parseStandardResolveParameters110(rootElement);

		List<OMElement> queryElements = getRequiredElements(rootElement, new XPath("*", nsContext));
		// check if all child elements are 'wfs:Query' elements (required for CITE)
		for (OMElement omElement : queryElements) {
			if (!new QName(WFSConstants.WFS_NS, "Query").equals(omElement.getQName())) {
				String msg = "Child element '" + omElement.getQName() + "' is not allowed.";
				throw new XMLParsingException(this, omElement, msg);
			}
		}

		List<Query> queries = new ArrayList<Query>();

		for (OMElement queryEl : queryElements) {
			List<PropertyName> propNames = new ArrayList<PropertyName>();
			List<OMElement> propertyNameElements = getElements(queryEl, new XPath("wfs:PropertyName", nsContext));
			for (OMElement propertyNameEl : propertyNameElements) {
				ValueReference propertyName = new ValueReference(propertyNameEl.getText(),
						getNamespaceContext(propertyNameEl));
				propNames.add(new PropertyName(propertyName, null, null));
			}

			List<OMElement> xlinkPropertyElements = getElements(queryEl, new XPath("wfs:XlinkPropertyName", nsContext));
			for (OMElement xlinkPropertyEl : xlinkPropertyElements) {
				ValueReference xlinkProperty = new ValueReference(xlinkPropertyEl.getText(),
						getNamespaceContext(xlinkPropertyEl));
				String xlinkDepth = getRequiredNodeAsString(xlinkPropertyEl,
						new XPath("@traverseXlinkDepth", nsContext));
				String xlinkExpiry = getNodeAsString(xlinkPropertyEl, new XPath("@traverseXlinkExpiry", nsContext),
						null);
				BigInteger resolveTimeout = null;
				try {
					if (xlinkExpiry != null) {
						resolveTimeout = new BigInteger(xlinkExpiry).multiply(BigInteger.valueOf(60));
					}
				}
				catch (NumberFormatException e) {
					// TODO string provided as time in minutes is not an integer
				}
				PropertyName xlinkPropName = new PropertyName(xlinkProperty,
						new ResolveParams(null, xlinkDepth, resolveTimeout), null);
				propNames.add(xlinkPropName);
			}

			List<Function> functions = new ArrayList<Function>();
			List<OMElement> functionElements = getElements(queryEl, new XPath("ogc:Function", nsContext));
			for (OMElement functionEl : functionElements) {
				try {
					XMLStreamReaderWrapper xmlStream = new XMLStreamReaderWrapper(
							functionEl.getXMLStreamReaderWithoutCaching(), getSystemId());
					// skip START_DOCUMENT
					xmlStream.nextTag();
					Function function = Filter110XMLDecoder.parseFunction(xmlStream);
					functions.add(function);
				}
				catch (XMLStreamException e) {
					throw new XMLParsingException(this, functionEl, e.getMessage());
				}
			}

			Filter filter = null;
			OMElement filterEl = queryEl.getFirstChildWithName(new QName(OGCNS, "Filter"));
			if (filterEl != null) {
				try {
					// TODO remove usage of wrapper (necessary at the moment to work
					// around problems with AXIOM's
					// XMLStreamReader)
					XMLStreamReader xmlStream = new XMLStreamReaderWrapper(filterEl.getXMLStreamReaderWithoutCaching(),
							null);
					// skip START_DOCUMENT
					xmlStream.nextTag();
					filter = Filter110XMLDecoder.parse(xmlStream);
				}
				catch (XMLStreamException e) {
					e.printStackTrace();
					throw new XMLParsingException(this, filterEl, e.getMessage());
				}
			}

			List<SortProperty> sortProps = new ArrayList<SortProperty>();
			OMElement sortByEl = getElement(queryEl, new XPath("ogc:SortBy", nsContext));
			if (sortByEl != null) {
				List<OMElement> sortPropertyElements = getRequiredElements(sortByEl,
						new XPath("ogc:SortProperty", nsContext));
				for (OMElement sortPropertyEl : sortPropertyElements) {
					OMElement propNameEl = getRequiredElement(sortPropertyEl, new XPath("ogc:PropertyName", nsContext));
					String sortOrder = getNodeAsString(sortPropertyEl, new XPath("ogc:SortOrder", nsContext), "ASC");
					SortProperty sortProp = new SortProperty(
							new ValueReference(propNameEl.getText(), getNamespaceContext(propNameEl)),
							sortOrder.equals("ASC"));
					sortProps.add(sortProp);
				}
			}

			String queryHandle = getNodeAsString(queryEl, new XPath("@handle", nsContext), null);

			String typeNameStr = getRequiredNodeAsString(queryEl, new XPath("@typeName", nsContext));
			TypeName[] typeNames = TypeName.valuesOf(queryEl, typeNameStr);

			String featureVersion = getNodeAsString(queryEl, new XPath("@featureVersion", nsContext), null);

			ICRS crs = null;
			String srsName = getNodeAsString(queryEl, new XPath("@srsName", nsContext), null);
			if (srsName != null) {
				crs = CRSManager.getCRSRef(srsName);
			}

			PropertyName[] propNamesArray = new PropertyName[propNames.size()];
			propNames.toArray(propNamesArray);

			SortProperty[] sortPropsArray = new SortProperty[sortProps.size()];
			sortProps.toArray(sortPropsArray);

			// build Query
			Query filterQuery = new FilterQuery(queryHandle, typeNames, featureVersion, crs, propNamesArray,
					sortPropsArray, filter);
			queries.add(filterQuery);
		}

		Query[] queryArray = new FilterQuery[queries.size()];
		queries.toArray(queryArray);

		return new GetFeature(VERSION_110, handle, presentationParams, resolveParams, queries);
	}

	/**
	 * Parses a WFS 2.0.0 <code>GetFeature</code> document into a {@link GetFeature}
	 * object.
	 * @return corresponding GetFeature instance, never <code>null</code>
	 * @throws OWSException
	 */
	public GetFeature parse200() throws OWSException {

		// <xsd:attribute name="handle" type="xsd:string"/>
		String handle = getNodeAsString(rootElement, new XPath("@handle", nsContext), null);

		// <xsd:attributeGroup ref="wfs:StandardPresentationParams"/>
		StandardPresentationParams presentationParams = parseStandardPresentationParameters200(rootElement);

		// <xsd:attributeGroup ref="wfs:StandardResolveParameters"/>
		ResolveParams resolveParams = parseStandardResolveParameters200(rootElement);

		// <xsd:element ref="fes:AbstractQueryExpression" maxOccurs="unbounded"/>
		List<OMElement> queryElements = getRequiredElements(rootElement, new XPath("*", nsContext));
		List<Query> queries = new ArrayList<Query>(queryElements.size());
		for (OMElement queryEl : queryElements) {
			queries.add(parseAbstractQuery200(queryEl));
		}

		return new GetFeature(VERSION_200, handle, presentationParams, resolveParams, queries);
	}

}
