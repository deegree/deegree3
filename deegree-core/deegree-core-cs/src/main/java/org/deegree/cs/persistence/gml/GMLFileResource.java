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
 http://ICompoundCRS.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.cs.persistence.gml;

import static org.deegree.cs.coordinatesystems.CRS.CRSType.COMPOUND;
import static org.deegree.cs.coordinatesystems.CRS.CRSType.GEOGRAPHIC;
import static org.deegree.cs.coordinatesystems.CRS.CRSType.PROJECTED;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.configuration.resources.XMLFileResource;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.coordinatesystems.ICompoundCRS;
import org.deegree.cs.coordinatesystems.IGeographicCRS;
import org.deegree.cs.coordinatesystems.IProjectedCRS;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.i18n.Messages;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.coordinate.ConcatenatedTransform;
import org.deegree.cs.transformations.helmert.Helmert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>GMLFileResource</code> provides easy access to a gml3.2. dictionary file,
 * which can be used together with the {@link GMLCRSStore}.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
public class GMLFileResource extends XMLFileResource implements GMLResource {

	private static final long serialVersionUID = -4389365894942107300L;

	private static Logger LOG = LoggerFactory.getLogger(GMLFileResource.class);

	private static final String PRE = CommonNamespaces.GML3_2_PREFIX + ":";

	private static final String TRANSFORM_XPATH = "/" + PRE + "Dictionary/" + PRE + "dictionaryEntry/" + PRE
			+ "Transformation";

	private static final String ID_XPATH = "//" + PRE + "dictionaryEntry/" + PRE + "*[" + PRE + "identifier='";

	private List<OMElement> transformations;

	private XMLAdapter adapter;

	/**
	 * @param provider
	 * @param properties
	 */
	public GMLFileResource(GMLCRSStore provider, URL gmlFile) {
		super(provider, gmlFile, "Dictionary", CommonNamespaces.GML3_2_NS);
		try {
			transformations = getElements(getRootElement(), new XPath(TRANSFORM_XPATH, nsContext));
		}
		catch (XMLParsingException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
		adapter = new XMLAdapter();
	}

	public Helmert getWGS84Transformation(IGeographicCRS sourceCRS) {
		if (sourceCRS == null) {
			return null;
		}

		Helmert result = null;
		Transformation parsedTransformation = getTransformation(sourceCRS, null);
		if (parsedTransformation instanceof Helmert) {
			result = (Helmert) parsedTransformation;
		}
		else {
			ICRS target = parsedTransformation.getTargetCRS();
			IGeographicCRS geoCRS = getGeographicCRS(target);
			if (geoCRS != null) {
				result = getWGS84Transformation(geoCRS);
			}
		}

		if (result == null) {
			LOG.info("No helmert transformation found for the given crs: " + sourceCRS.getCodeAndName());
		}
		return result;
	}

	/**
	 * Retrieves the underlying geographic crs from the given coordinate system.
	 * @param crs to get the {@link GeographicCRS} from.
	 * @return the {@link GeographicCRS} or <code>null</code> if the crs has no underlying
	 * {@link GeographicCRS}.
	 */
	private IGeographicCRS getGeographicCRS(ICRS crs) {
		IGeographicCRS result = null;
		if (crs.getType() == COMPOUND) {
			if (((ICompoundCRS) crs).getUnderlyingCRS().getType() == PROJECTED) {
				result = ((IProjectedCRS) ((ICompoundCRS) crs).getUnderlyingCRS()).getGeographicCRS();
			}
			else if (((ICompoundCRS) crs).getUnderlyingCRS().getType() == GEOGRAPHIC) {
				result = (GeographicCRS) crs;
			}
			else {
				LOG.warn("Wgs84 Transformation lookup is currently only supported for GeographicCRS-chains.");
			}
		}
		else if (crs.getType() == PROJECTED) {
			result = ((IProjectedCRS) crs).getGeographicCRS();
		}
		else if (crs.getType() == GEOGRAPHIC) {
			result = (GeographicCRS) crs;
		}
		else {
			LOG.warn("Wgs84 Transformation lookup is currently only supported for GeographicCRS-chains.");
		}
		return result;
	}

	public Transformation getTransformation(ICRS sourceCRS, ICRS targetCRS) {
		if (sourceCRS == null) {
			return null;
		}
		List<String> sourceIDs = Arrays.asList(sourceCRS.getOrignalCodeStrings());
		List<String> targetIDs = null;
		if (targetCRS != null) {
			targetIDs = Arrays.asList(targetCRS.getOrignalCodeStrings());
		}
		else {
			targetIDs = new ArrayList<String>();
		}

		if (transformations.isEmpty()) {
			LOG.debug("Apparently no transformations were found for the given CoordinateSystem: "
					+ sourceCRS.getCode().getOriginal());
			return null;
		}
		Transformation result = null;
		for (int i = 0; i < transformations.size() && result == null; ++i) {
			OMElement transElem = transformations.get(i);
			if (transElem != null) {
				try {
					String transformSourceID = getSourceTransformID(transElem);
					if (sourceIDs.contains(transformSourceID)) {
						result = parseTransformation(transElem, targetIDs, targetCRS);
					}
				}
				catch (XMLParsingException e) {
					LOG.debug("Transformation with id: " + transElem.getLocalName() + " could not be used because:  "
							+ e.getMessage());
				}
			}
		}
		return result;
	}

	/**
	 * Parses the transformation from the given element and checks if a transformation
	 * chain needs to be build.
	 * @param transformationElement to parse
	 * @param targetIDs
	 * @param targetCRS
	 * @return
	 * @throws XMLParsingException
	 */
	private Transformation parseTransformation(OMElement transformationElement, List<String> targetIDs, ICRS targetCRS)
			throws XMLParsingException {
		// ITransformation result = getProvider().parseTransformation(
		// transformationElement );
		Transformation result = null;
		if (getProvider() instanceof GMLCRSStore) {
			result = ((GMLCRSStore) getProvider()).parseTransformation(transformationElement);
		}
		if (targetCRS == null) {
			// Trying to find a helmert transformation
			LOG.debug("Resolving a possible transformation.");
			if (result != null && !(result instanceof Helmert)) {
				result = getTransformation(result.getTargetCRS(), null);
			}
		}
		else {
			String transformTargetID = getTargetTransformID(transformationElement);
			if (!targetIDs.contains(transformTargetID)) {
				LOG.debug("Found a transformation with gml:id: "
						+ transformationElement.getAttributeValue(new QName(CommonNamespaces.GML3_2_NS, "id"))
						+ ", but the target does not match the source crs, trying to build transformation chain.");
				Transformation second = getTransformation(result.getTargetCRS(), targetCRS);
				if (second != null) {
					result = new ConcatenatedTransform(result, second);
				}
				else {
					LOG.debug("The transformation with gml:id: "
							+ transformationElement.getAttributeValue(new QName(CommonNamespaces.GML3_2_NS, "id"))
							+ " is not the start of transformation chain, discarding it. ");
					result = null;
				}
			}
		}
		return result;
	}

	/**
	 * @param transElem
	 * @return
	 * @throws XMLParsingException
	 */
	private String getTargetTransformID(OMElement transElem) throws XMLParsingException {
		OMElement targetCRSProp = adapter.getRequiredElement(transElem, new XPath(PRE + "targetCRS", nsContext));
		return getIdFromElemOrXlink(targetCRSProp);
	}

	private String getSourceTransformID(OMElement transElem) throws XMLParsingException {
		OMElement sourceCRSProp = adapter.getRequiredElement(transElem, new XPath(PRE + "sourceCRS", nsContext));
		return getIdFromElemOrXlink(sourceCRSProp);
	}

	/**
	 * Get the id from an element of a href.
	 * @param sourceCRSProp
	 * @return
	 * @throws XMLParsingException
	 */
	private String getIdFromElemOrXlink(OMElement sourceCRSProp) throws XMLParsingException {
		String result = sourceCRSProp.getAttributeValue(new QName(CommonNamespaces.XLNNS, "href"));
		if (result == null || "".equals(result)) {
			result = adapter.getRequiredNodeAsString(sourceCRSProp, new XPath("*[1]/" + PRE + "identifier", nsContext));
		}
		return result;

	}

	public OMElement getURIAsType(String uri) throws IOException {
		OMElement result = null;
		try {
			XPath xpath = new XPath(ID_XPATH + uri + "']", nsContext);
			OMElement root = getRootElement();
			result = adapter.getElement(root, xpath);
		}
		catch (XMLParsingException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.deegree.crs.configuration.gml.GMLResource#getAvailableCRSIds()
	 */
	public List<CRSCodeType[]> getAvailableCRSIds() {
		List<OMElement> crsIDs = new LinkedList<OMElement>();
		try {
			XPath xpath = new XPath("//" + PRE + "dictionaryEntry/*[" + PRE + "ProjectedCRS|" + PRE + "CompoundCRS|"
					+ PRE + "GeodeticCRS]", nsContext);
			crsIDs.addAll(adapter.getElements(getRootElement(), xpath));
		}
		catch (XMLParsingException e) {
			throw new CRSConfigurationException(Messages.getMessage("CRS_CONFIG_GET_ALL_ELEMENT_IDS", e.getMessage()),
					e);
		}

		List<CRSCodeType[]> result = new ArrayList<CRSCodeType[]>();
		for (OMElement crs : crsIDs) {
			if (crs != null) {
				String[] ids = adapter.getNodesAsStrings(crs, new XPath(PRE + "identifier", nsContext));
				if (ids != null) {
					CRSCodeType[] r = new CRSCodeType[ids.length];
					for (int i = 0; i < ids.length; ++i) {
						if (ids[i] != null) {
							r[i] = new CRSCodeType(ids[i]);
						}
					}
					result.add(r);
				}
			}
		}
		return result;
	}

	@Override
	public List<ICRS> getAvailableCRSs() {
		List<CRSCodeType[]> availableCRSIds = getAvailableCRSIds();
		List<ICRS> result = new ArrayList<ICRS>(availableCRSIds.size());
		for (CRSCodeType[] s : availableCRSIds) {
			ICRS crs = getProvider().getCRSByCode(s[0]);
			if (crs != null) {
				result.add(crs);
			}
		}
		return result;
	}

}
