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

package org.deegree.protocol.wfs.getpropertyvalue.xml;

import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.commons.ows.exception.OWSException.VERSION_NEGOTIATION_FAILED;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ResolveParams;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.XPath;
import org.deegree.filter.expression.ValueReference;
import org.deegree.protocol.wfs.getpropertyvalue.GetPropertyValue;
import org.deegree.protocol.wfs.query.Query;
import org.deegree.protocol.wfs.query.StandardPresentationParams;
import org.deegree.protocol.wfs.query.xml.QueryXMLAdapter;

/**
 * Adapter between XML <code>GetPropertyValue</code> requests and {@link GetPropertyValue}
 * objects.
 * <p>
 * Supported WFS versions:
 * <ul>
 * <li>2.0.0</li>
 * </ul>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class GetPropertyValueXMLAdapter extends QueryXMLAdapter {

	/**
	 * Parses a <code>GetPropertyValue</code> element into a {@link GetPropertyValue}
	 * object.
	 * @return parsed {@link GetPropertyValue} request, never <code>null</code>
	 * @throws OWSException
	 */
	public GetPropertyValue parse() throws OWSException {

		Version version = Version.parseVersion(getNodeAsString(rootElement, new XPath("@version", nsContext), null));

		GetPropertyValue result = null;

		if (VERSION_200.equals(version)) {
			result = parse200();
		}
		else {
			String msg = "Version '" + version
					+ "' is not supported for GetPropertyValue requests. The only supported version is 2.0.0.";
			throw new OWSException(msg, VERSION_NEGOTIATION_FAILED, null);
		}
		return result;
	}

	/**
	 * Parses a WFS 2.0.0 <code>GetPropertyValue</code> document into a
	 * {@link GetPropertyValue} object.
	 * @return corresponding GetPropertyValue instance, never <code>null</code>
	 * @throws OWSException
	 */
	private GetPropertyValue parse200() throws OWSException {

		// <xsd:attribute name="handle" type="xsd:string"/>
		String handle = getNodeAsString(rootElement, new XPath("@handle", nsContext), null);

		// <xsd:attributeGroup ref="wfs:StandardPresentationParams"/>
		StandardPresentationParams presentationParams = parseStandardPresentationParameters200(rootElement);

		// <xsd:attributeGroup ref="wfs:StandardResolveParameters"/>
		ResolveParams resolveParams = parseStandardResolveParameters200(rootElement);

		// <xsd:attribute name="valueReference" type="xsd:string" use="required"/>
		String valueRefStr = getRequiredNodeAsString(rootElement, new XPath("@valueReference", nsContext));
		if (valueRefStr.isEmpty()) {
			throw new OWSException("The valueReference attribute was empty.", INVALID_PARAMETER_VALUE,
					"valueReference");
		}
		ValueReference valueReference = new ValueReference(valueRefStr, getNamespaceContext(rootElement));

		// <xsd:attribute name="resolvePath" type="xsd:string"/>
		ValueReference resolvePath = null;
		String resolvePathStr = getNodeAsString(rootElement, new XPath("@resolvePath", nsContext), null);
		if (resolvePathStr != null) {
			resolvePath = new ValueReference(resolvePathStr, getNamespaceContext(rootElement));
		}

		// <xsd:element ref="fes:AbstractQueryExpression" maxOccurs="unbounded"/>
		OMElement queryEl = getRequiredElement(rootElement, new XPath("*", nsContext));
		Query query = parseAbstractQuery200(queryEl);

		return new GetPropertyValue(VERSION_200, handle, presentationParams, resolveParams, valueReference, resolvePath,
				query);
	}

}
