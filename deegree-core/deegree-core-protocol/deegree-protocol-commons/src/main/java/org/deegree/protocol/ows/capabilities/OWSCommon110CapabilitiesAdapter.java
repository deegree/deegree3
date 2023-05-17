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
package org.deegree.protocol.ows.capabilities;

import static org.deegree.commons.xml.CommonNamespaces.OWS_11_NS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.requireStartElement;
import static org.deegree.protocol.wps.WPSConstants.WPS_100_NS;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.OperationsMetadata;
import org.deegree.commons.ows.metadata.domain.Domain;
import org.deegree.commons.ows.metadata.domain.PossibleValues;
import org.deegree.commons.ows.metadata.operation.DCP;
import org.deegree.commons.ows.metadata.operation.Operation;
import org.deegree.commons.tom.ows.StringOrRef;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;

/**
 * {@link OWSCapabilitiesAdapter} for capabilities documents that comply to the
 * <a href="http://www.opengeospatial.org/standards/common">OWS Common 1.1.0</a>
 * specification.
 * <p>
 * Known OWS Common 1.1.0-based specifications:
 * <ul>
 * <li>WFS 2.0.0</li>
 * <li>WMTS 1.0.0</li>
 * <li>WPS 1.0.0</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class OWSCommon110CapabilitiesAdapter extends AbstractOWSCommonCapabilitiesAdapter {

	/**
	 * Creates a new {@link OWSCommon110CapabilitiesAdapter} instance.
	 */
	public OWSCommon110CapabilitiesAdapter() {
		super(OWS_11_NS);
		nsContext.addNamespace("wps", WPS_100_NS);
	}

	@Override
	public OperationsMetadata parseOperationsMetadata() {

		OMElement opMetadataEl = getElement(getRootElement(), new XPath("ows:OperationsMetadata", nsContext));
		if (opMetadataEl == null) {
			return null;
		}

		XPath xpath = new XPath("ows:Operation", nsContext);
		List<OMElement> opEls = getElements(opMetadataEl, xpath);
		List<Operation> operations = new ArrayList<Operation>(opEls.size());
		if (opEls != null) {
			for (OMElement opEl : opEls) {
				Operation op = parseOperation(opEl);
				operations.add(op);
			}
		}

		xpath = new XPath("ows:Parameter", nsContext);
		List<OMElement> paramEls = getElements(opMetadataEl, xpath);
		List<Domain> params = new ArrayList<Domain>(opEls.size());
		if (paramEls != null) {
			for (OMElement paramEl : paramEls) {
				Domain parameter = parseDomain(paramEl);
				params.add(parameter);
			}
		}

		xpath = new XPath("ows:Constraint", nsContext);
		List<OMElement> constaintEls = getElements(opMetadataEl, xpath);
		List<Domain> constraints = new ArrayList<Domain>(constaintEls.size());
		if (constaintEls != null) {
			for (OMElement constaintEl : constaintEls) {
				Domain constraint = parseDomain(constaintEl);
				constraints.add(constraint);
			}
		}

		List<OMElement> extendedCaps = new ArrayList<OMElement>();
		xpath = new XPath("ows:ExtendedCapabilities", nsContext);
		OMElement extededCapab = getElement(opMetadataEl, xpath);
		if (extededCapab != null) {
			extendedCaps.add(extededCapab);
		}

		return new OperationsMetadata(operations, params, constraints, extendedCaps);
	}

	/**
	 * @param opEl context {@link OMElement}
	 * @return an {@link Operation} instance, never <code>null</code>
	 */
	private Operation parseOperation(OMElement opEl) {

		XPath xpath = new XPath("@name", nsContext);
		String name = getNodeAsString(opEl, xpath, null);

		xpath = new XPath("ows:DCP", nsContext);
		List<OMElement> dcpEls = getElements(opEl, xpath);
		List<DCP> dcps = new ArrayList<DCP>(dcpEls.size());
		if (dcpEls != null) {
			for (OMElement dcpEl : dcpEls) {
				DCP dcp = parseDCP(dcpEl);
				dcps.add(dcp);
			}
		}

		xpath = new XPath("ows:Parameter", nsContext);
		List<OMElement> paramEls = getElements(opEl, xpath);
		List<Domain> params = new ArrayList<Domain>(paramEls.size());
		if (paramEls != null) {
			for (OMElement paramEl : paramEls) {
				Domain parameter = parseDomain(paramEl);
				params.add(parameter);
			}
		}

		xpath = new XPath("ows:Constraint", nsContext);
		List<OMElement> constaintEls = getElements(opEl, xpath);
		List<Domain> constraints = new ArrayList<Domain>(constaintEls.size());
		if (constaintEls != null) {
			for (OMElement constaintEl : constaintEls) {
				Domain constraint = parseDomain(constaintEl);
				constraints.add(constraint);
			}
		}

		List<OMElement> metadataEls = getElements(opEl, new XPath("ows:Metadata", nsContext));

		return new Operation(name, dcps, params, constraints, metadataEls);
	}

	/**
	 * Returns the URL for the specified operation and HTTP method.
	 * @param operation name of the operation, must not be <code>null</code>
	 * @param post if set to true, the URL for POST requests will be returned, otherwise
	 * the URL for GET requests will be returned
	 * @return the operation URL (trailing question marks are stripped), can be
	 * <code>null</code> (if the operation/method is not announced by the service)
	 * @throws MalformedURLException if the announced URL is malformed
	 */
	public URL getOperationURL(String operation, boolean post) throws MalformedURLException {

		String xpathStr = "ows:OperationsMetadata/ows:Operation[@name='" + operation + "']/ows:DCP/ows:HTTP/ows:"
				+ (post ? "Post" : "Get") + "/@xlink:href";
		URL url = null;
		String href = getNodeAsString(getRootElement(), new XPath(xpathStr, nsContext), null);
		if (href != null) {
			if (href.endsWith("?")) {
				href = href.substring(0, href.length() - 1);
			}
			url = new URL(href);
		}
		return url;
	}

	/**
	 * @param domainEl context {@link OMElement}
	 * @return an {@link Operation} instance, never <code>null</code>
	 */
	protected Domain parseDomain(OMElement domainEl) {

		// <attribute name="name" type="string" use="required">
		String name = getNodeAsString(domainEl, new XPath("@name", nsContext), null);

		// <group ref="ows:PossibleValues"/>
		OMElement possibleValuesEl = domainEl.getFirstElement();
		if (possibleValuesEl == null) {
			throw new XMLParsingException(this, domainEl, "Element from 'ows:PossibleValues' group is missing.");
		}
		PossibleValues possibleValues = parsePossibleValues(possibleValuesEl);

		// <element ref="ows:DefaultValue" minOccurs="0">
		String defaultValue = getNodeAsString(domainEl, new XPath("ows:DefaultValue", nsContext), null);

		// <element ref="ows:Meaning" minOccurs="0">
		StringOrRef meaning = null;
		String meaningName = getNodeAsString(domainEl, new XPath("ows:Meaning", nsContext), null);
		String meaningRef = getNodeAsString(domainEl, new XPath("ows:Meaning/@reference", nsContext), null);
		if (meaningName != null || meaningRef != null) {
			meaning = new StringOrRef(meaningName, meaningRef);
		}

		// <element ref="ows:DataType" minOccurs="0">
		StringOrRef dataType = null;
		String datatypeName = getNodeAsString(domainEl, new XPath("ows:DataType", nsContext), null);
		String datatypeRef = getNodeAsString(domainEl, new XPath("ows:DataType/@reference", nsContext), null);
		if (datatypeName != null || datatypeRef != null) {
			dataType = new StringOrRef(datatypeName, datatypeRef);
		}

		// <group ref="ows:ValuesUnit" minOccurs="0">
		StringOrRef valuesUnitUom = null;
		String valuesUnitUomName = getNodeAsString(domainEl, new XPath("ows:UOM", nsContext), null);
		String valuesUnitUomRef = getNodeAsString(domainEl, new XPath("ows:UOM/@ows:reference", nsContext), null);
		if (valuesUnitUomName != null || valuesUnitUomRef != null) {
			valuesUnitUom = new StringOrRef(valuesUnitUomName, valuesUnitUomRef);
		}

		StringOrRef valuesUnitRefSys = null;
		String valuesUnitRefSysName = getNodeAsString(domainEl, new XPath("ows:ReferenceSystem", nsContext), null);
		String valuesUnitRefSysRef = getNodeAsString(domainEl,
				new XPath("ows:ReferenceSystem/@ows:reference", nsContext), null);
		if (valuesUnitRefSysName != null || valuesUnitRefSysRef != null) {
			valuesUnitRefSys = new StringOrRef(valuesUnitRefSysName, valuesUnitRefSysRef);
		}

		// <element ref="ows:Metadata" minOccurs="0" maxOccurs="unbounded">
		List<OMElement> metadataEls = getElements(domainEl, new XPath("ows:Metadata", nsContext));

		return new Domain(name, possibleValues, defaultValue, meaning, dataType, valuesUnitUom, valuesUnitRefSys,
				metadataEls);
	}

	/**
	 * Consumes an <code>ows:PositionType</code> element from the given XML stream.
	 * <ul>
	 * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (of
	 * type <code>ows:PositionType</code>)</li>
	 * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code>
	 * event (of type <code>ows:PositionType</code>)</li>
	 * </ul>
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event (of type
	 * <code>ows:PositionType</code>), points at the corresponding
	 * <code>END_ELEMENT</code> event (of type <code>ows:PositionType</code>) afterwards
	 * @return corresponding coordinates, never <code>null</code>
	 * @throws XMLParsingException if the element can not be parsed as an
	 * "ows:PositionType" element
	 * @throws XMLStreamException
	 */
	public double[] parsePositionType(XMLStreamReader xmlStream) throws XMLStreamException, NumberFormatException {
		String text = xmlStream.getElementText();
		String[] tokens = text.split("\\s+");
		double[] coords = new double[tokens.length];
		for (int i = 0; i < tokens.length; i++) {
			coords[i] = Double.parseDouble(tokens[i]);
		}
		return coords;
	}

	/**
	 * Consumes an <code>ows:BoundingBoxType</code> element from the given XML stream.
	 * <ul>
	 * <li>Precondition: cursor must point at the <code>START_ELEMENT</code> event (of
	 * type <code>ows:BoundingBoxType</code>)</li>
	 * <li>Postcondition: cursor points at the corresponding <code>END_ELEMENT</code>
	 * event (of type <code>ows:BoundingBoxType</code>)</li>
	 * </ul>
	 * @param xmlStream cursor must point at the <code>START_ELEMENT</code> event (of type
	 * <code>ows:BoundingBoxType</code>), points at the corresponding
	 * <code>END_ELEMENT</code> event (of type <code>ows:BoundingBoxType</code>)
	 * afterwards
	 * @return corresponding coordinates, never <code>null</code>
	 * @throws NoSuchElementException
	 * @throws XMLParsingException if the element can not be parsed as an
	 * "ows:BoundingBoxType" element
	 * @throws XMLStreamException
	 */
	public Envelope parseBoundingBoxType(XMLStreamReader xmlStream, ICRS defaultCrs)
			throws NoSuchElementException, XMLStreamException {

		String crsString = xmlStream.getAttributeValue(null, "crs");
		ICRS crs = defaultCrs;
		if (crsString != null) {
			crs = CRSManager.getCRSRef(crsString);
		}
		nextElement(xmlStream);

		// <element name="LowerCorner" type="ows:PositionType">
		requireStartElement(xmlStream, new QName(OWS_11_NS, "LowerCorner"));
		double[] lowerCorner = parsePositionType(xmlStream);
		nextElement(xmlStream);

		// <element name="UpperCorner" type="ows:PositionType">
		requireStartElement(xmlStream, new QName(OWS_11_NS, "UpperCorner"));
		double[] upperCorner = parsePositionType(xmlStream);
		nextElement(xmlStream);

		Point min = new DefaultPoint(null, crs, null, lowerCorner);
		Point max = new DefaultPoint(null, crs, null, upperCorner);
		return new DefaultEnvelope(null, crs, null, min, max);
	}

}
