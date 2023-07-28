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

import static org.deegree.commons.xml.CommonNamespaces.OWS_NS;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.OperationsMetadata;
import org.deegree.commons.ows.metadata.domain.AllowedValues;
import org.deegree.commons.ows.metadata.domain.Domain;
import org.deegree.commons.ows.metadata.domain.PossibleValues;
import org.deegree.commons.ows.metadata.domain.Value;
import org.deegree.commons.ows.metadata.domain.Values;
import org.deegree.commons.ows.metadata.operation.DCP;
import org.deegree.commons.ows.metadata.operation.Operation;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;

/**
 * {@link OWSCapabilitiesAdapter} for capabilities documents that comply to the
 * <a href="http://www.opengeospatial.org/standards/common">OWS Common 1.0.0</a>
 * specification.
 * <p>
 * Known OWS Common 1.0.0-based specifications:
 * <ul>
 * <li>WFS 1.1.0</li>
 * <li>CSW 2.0.2</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class OWSCommon100CapabilitiesAdapter extends AbstractOWSCommonCapabilitiesAdapter {

	private final static GeometryFactory geomFac = new GeometryFactory();

	/**
	 * Creates a new {@link OWSCommon100CapabilitiesAdapter} instance.
	 */
	public OWSCommon100CapabilitiesAdapter() {
		super(OWS_NS);
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
		List<Domain> params = new ArrayList<Domain>(paramEls.size());
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

		xpath = new XPath("ows:Metadata", nsContext);
		List<OMElement> metadataEls = getElements(opEl, xpath);

		return new Operation(name, dcps, params, constraints, metadataEls);
	}

	@Override
	protected Domain parseDomain(OMElement domainEl) {

		// <attribute name="name" type="string" use="required">
		String name = getNodeAsString(domainEl, new XPath("@name", nsContext), null);

		// <element name="Value" type="string" maxOccurs="unbounded">
		String[] valuesArray = getNodesAsStrings(domainEl, new XPath("ows:Value", nsContext));
		List<Values> values = new ArrayList<Values>(valuesArray.length);
		for (String value : valuesArray) {
			values.add(new Value(value));
		}

		PossibleValues possibleValues = new AllowedValues(values);

		// <element ref="ows:Metadata" minOccurs="0" maxOccurs="unbounded">
		List<OMElement> metadataEls = getElements(domainEl, new XPath("ows:Metadata", nsContext));

		return new Domain(name, possibleValues, null, null, null, null, null, metadataEls);
	}

	/**
	 * @param bboxEl
	 * @return
	 */
	public Envelope parseWGS84BoundingBox(OMElement bboxEl) {

		// <element name="LowerCorner" type="ows:PositionType2D">
		double[] lowerCorner = parseDoubleList(getRequiredElement(bboxEl, new XPath("ows:LowerCorner", nsContext)));

		// <element name="UpperCorner" type="ows:PositionType2D">
		double[] upperCorner = parseDoubleList(getRequiredElement(bboxEl, new XPath("ows:UpperCorner", nsContext)));

		// <attribute name="crs" type="anyURI" use="optional"
		// fixed="urn:ogc:def:crs:OGC:2:84">
		// TODO
		String crsName = "EPSG:4326";
		CRSRef crsRef = CRSManager.getCRSRef(crsName);

		// "dimensions" attribute (optional)
		// int dimensions = getNodeAsInt( boundingBoxDataElement, new XPath(
		// "@dimensions", nsContext ), -1 );

		return geomFac.createEnvelope(lowerCorner, upperCorner, crsRef);
	}

	private double[] parseDoubleList(OMElement positionElement) throws XMLParsingException {
		String s = positionElement.getText();
		// don't use String.split(regex) here (speed)
		StringTokenizer st = new StringTokenizer(s);
		List<String> tokens = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			tokens.add(st.nextToken());
		}
		double[] doubles = new double[tokens.size()];
		for (int i = 0; i < doubles.length; i++) {
			try {
				doubles[i] = Double.parseDouble(tokens.get(i));
			}
			catch (NumberFormatException e) {
				String msg = "Value '" + tokens.get(i) + "' cannot be parsed as a double.";
				throw new XMLParsingException(this, positionElement, msg);
			}
		}
		return doubles;
	}

}
