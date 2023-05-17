/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.protocol.wms.featureinfo;

import static org.deegree.commons.xml.CommonNamespaces.OWS_NS;
import static org.deegree.commons.xml.CommonNamespaces.SLDNS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.getText;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipToRequiredElement;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.protocol.wms.AbstractWmsParser;
import org.deegree.protocol.wms.WMSConstants;
import org.deegree.protocol.wms.map.GetMapParser;
import org.deegree.protocol.wms.ops.GetFeatureInfo;
import org.deegree.protocol.wms.ops.GetMap;

/**
 * Adapter between XML <code>GetFeatureInfo</code> requests and {@link GetFeatureInfo}
 * objects.
 * <p>
 * Supported WMS versions:
 * <ul>
 * <li>1.3.0</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class GetFeatureInfoParser extends AbstractWmsParser {

	private static final QName QUERYLAYER_ELEMENT = new QName(OWS_NS, "QueryLayer");

	private static final QName I_ELEMENT = new QName(OWS_NS, "I");

	private static final QName J_ELEMENT = new QName(OWS_NS, "J");

	private static final QName Output_ELEMENT = new QName(OWS_NS, "Output");

	private static final QName InfoFormat_ELEMENT = new QName(OWS_NS, "InfoFormat");

	private static final QName FeatureCount_ELEMENT = new QName(OWS_NS, "FeatureCount");

	private static final QName Exceptions_ELEMENT = new QName(OWS_NS, "Exceptions");

	/**
	 * Parses a WMS <code>GetFeatureInfo</code> document into a {@link GetFeatureInfo}
	 * object.
	 *
	 * <p>
	 * Supported WMS versions:
	 * <ul>
	 * <li>1.3.0</li>
	 * </ul>
	 * </p>
	 * @return parsed {@link GetFeatureInfo} request, never <code>null</code>
	 * @throws XMLStreamException if an error occurs during parsing the xml
	 * @throws InvalidParameterException if the request version is not supported
	 * @throws OWSException if the CRS is not supported or an error occurred during
	 * parsing a value
	 */
	public GetFeatureInfo parse(XMLStreamReader getMap) throws OWSException, XMLStreamException {
		Version version = forwardToStartAndDetermineVersion(getMap);
		if (!WMSConstants.VERSION_130.equals(version))
			throw new InvalidParameterException("Version " + version + " is not supported (yet).");
		try {
			return parse130(getMap);
		}
		catch (UnknownCRSException e) {
			throw new OWSException(e.getMessage(), OWSException.NO_APPLICABLE_CODE);
		}
		catch (ParseException e) {
			throw new OWSException(e.getMessage(), OWSException.NO_APPLICABLE_CODE);
		}
	}

	private GetFeatureInfo parse130(XMLStreamReader in)
			throws UnknownCRSException, XMLStreamException, OWSException, ParseException {
		skipToRequiredElement(in, new QName(SLDNS, "GetMap"));

		GetMapParser getMapParser = new GetMapParser();
		GetMap parsedGetMap = getMapParser.parse(in);

		skipToRequiredElement(in, QUERYLAYER_ELEMENT);
		List<String> queryLayers = parseQueryLayers(in);

		skipToRequiredElement(in, I_ELEMENT);
		int i = XMLStreamUtils.getRequiredElementTextAsInteger(in, I_ELEMENT, true);

		skipToRequiredElement(in, J_ELEMENT);
		int j = XMLStreamUtils.getRequiredElementTextAsInteger(in, J_ELEMENT, true);

		skipToRequiredElement(in, Output_ELEMENT);
		Output output = parseOutput(in);
		String exceptions = parseExceptions(in);

		return createGetFeatureInfo(parsedGetMap, queryLayers, i, j, output, exceptions);
	}

	private GetFeatureInfo createGetFeatureInfo(GetMap getMap, List<String> queryLayers, int i, int j, Output output,
			String exceptions) throws OWSException {
		HashMap<String, String> parameterMap = new HashMap<String, String>();
		if (exceptions != null)
			parameterMap.put("EXCEPTIONS", exceptions);
		return new GetFeatureInfo(getMap.getLayers(), getMap.getStyles(), queryLayers, getMap.getWidth(),
				getMap.getHeight(), i, j, getMap.getBoundingBox(), getMap.getCoordinateSystem(), output.featureCount,
				output.infoFormat, parameterMap, getMap.getDimensions());
	}

	private List<String> parseQueryLayers(XMLStreamReader in) throws XMLStreamException {
		List<String> queryLayers = new ArrayList<String>();
		while (QUERYLAYER_ELEMENT.equals(in.getName())) {
			String queryLayer = getText(in, QUERYLAYER_ELEMENT, null, true);
			if (queryLayer != null)
				queryLayers.add(queryLayer);
		}
		return queryLayers;
	}

	private Output parseOutput(XMLStreamReader in) throws XMLStreamException {
		skipToRequiredElement(in, InfoFormat_ELEMENT);
		String infoFormat = getText(in, InfoFormat_ELEMENT, null, true);
		int featureCount = 1;
		if (FeatureCount_ELEMENT.equals(in.getName())) {
			featureCount = XMLStreamUtils.getElementTextAsInteger(in);
			nextElement(in);
		}
		skipElement(in);
		nextElement(in);
		return new Output(infoFormat, featureCount);
	}

	private String parseExceptions(XMLStreamReader in) throws XMLStreamException {
		if (Exceptions_ELEMENT.equals(in.getName()))
			return getText(in, Exceptions_ELEMENT, null, true);
		return null;
	}

	private class Output {

		String infoFormat;

		int featureCount;

		public Output(String infoFormat, int featureCount) {
			this.infoFormat = infoFormat;
			this.featureCount = featureCount;
		}

	}

}