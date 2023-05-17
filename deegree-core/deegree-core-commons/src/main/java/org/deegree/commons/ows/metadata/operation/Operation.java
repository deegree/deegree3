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
package org.deegree.commons.ows.metadata.operation;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.domain.Domain;
import org.deegree.commons.utils.Pair;

/**
 * Encapsulates the metadata on a single operation of an OGC web service (as reported in
 * the capabilities document).
 * <p>
 * Data model has been designed to capture the expressiveness of all OWS specifications
 * and versions and was verified for the following specifications:
 * <ul>
 * <li>OWS Common 1.0.0</li>
 * <li>OWS Common 1.1.0</li>
 * <li>OWS Common 2.0</li>
 * <li>WFS 1.0.0</li>
 * </ul>
 * </p>
 * <p>
 * From OWS Common 2.0: <cite>Metadata for one operation that this server
 * implements.</cite>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class Operation {

	private final String name;

	private final List<URL> getUrls = new ArrayList<URL>();

	private final List<URL> postUrls = new ArrayList<URL>();

	private final List<DCP> dcp;

	private final List<Domain> parameters;

	private final List<Domain> constraints;

	private List<OMElement> metadata;

	public Operation(String name, List<DCP> dcps, List<Domain> params, List<Domain> constraints,
			List<OMElement> metadata) {

		this.name = name;
		this.dcp = dcps;
		if (params != null) {
			this.parameters = params;
		}
		else {
			this.parameters = new ArrayList<Domain>();
		}
		if (constraints != null) {
			this.constraints = constraints;
		}
		else {
			this.constraints = new ArrayList<Domain>();
		}
		if (metadata != null) {
			this.metadata = metadata;
		}
		else {
			this.metadata = new ArrayList<OMElement>();
		}

		for (DCP dcp : dcps) {
			for (Pair<URL, List<Domain>> urls : dcp.getGetEndpoints()) {
				getUrls.add(urls.first);
			}
			for (Pair<URL, List<Domain>> urls : dcp.getPostEndpoints()) {
				postUrls.add(urls.first);
			}
		}
	}

	/**
	 * Returns the operation name.
	 * <p>
	 * From OWS Common 2.0: <cite>Name or identifier of this operation (request) (for
	 * example, GetCapabilities). The list of required and optional operations implemented
	 * shall be specified in the Implementation Specification for * this service.</cite>
	 * </p>
	 * @return the operation name, never <code>null</code>
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the supported Distributed Computing Platforms (DCPs).
	 * <p>
	 * From OWS Common 2.0: <cite>Unordered list of Distributed Computing Platforms (DCPs)
	 * supported for this operation. At present, only the HTTP DCP is defined, so this
	 * element will appear only once.</cite>
	 * </p>
	 * @return dcps, never <code>null</code>.
	 */
	public List<DCP> getDCPs() {
		return dcp;
	}

	/**
	 * Returns the endpoint {@link URL}s for this operation (HTTP-GET).
	 * @return endpoint URLs, can be empty, but never <code>null</code>
	 */
	public List<URL> getGetUrls() {
		return getUrls;
	}

	/**
	 * Returns the endpoint {@link URL}s for this operation (HTTP-POST).
	 * @return endpoint URLs, can be empty, but never <code>null</code>
	 */
	public List<URL> getPostUrls() {
		return postUrls;
	}

	/**
	 * Returns the parameter validity domains for this operation.
	 * <p>
	 * From OWS Common 2.0: <cite>Optional unordered list of parameter domains that each
	 * apply to this operation which this server implements. If one of these Parameter
	 * elements has the same "name" attribute as a Parameter element in the
	 * OperationsMetadata element, this Parameter element shall override the other one for
	 * this operation. The list of required and optional parameter domain limitations for
	 * this operation shall be specified in the Implementation Specification for this
	 * service.</cite>
	 * </p>
	 * @return operation parameter domains, never <code>null</code>.
	 */
	public List<Domain> getParameters() {
		return parameters;
	}

	/**
	 * Returns the domain validity constraints for this operation.
	 * <p>
	 * From OWS Common 2.0: <cite>Optional unordered list of valid domain constraints on
	 * non-parameter quantities that each apply to this operation. If one of these
	 * Constraint elements has the same "name" attribute as a Constraint element in the
	 * OperationsMetadata element, this Constraint element shall override the other one
	 * for this operation. The list of required and optional constraints for this
	 * operation shall be specified in the Implementation Specification for this
	 * service.</cite>
	 * </p>
	 * @return operation domain validity constraints, never <code>null</code>
	 */
	public List<Domain> getConstraints() {
		return constraints;
	}

	/**
	 * <p>
	 * From OWS Common 2.0: <cite>Optional unordered list of additional metadata about
	 * this operation and its' implementation. A list of required and optional metadata
	 * elements for this operation should be specified in the Implementation Specification
	 * for this service. (Informative: This metadata might specify the operation request
	 * parameters or provide the XML Schemas for the operation request.)</cite>
	 * </p>
	 * @return additional operation metadata, never <code>null</code>
	 */
	public List<OMElement> getMetadata() {
		return metadata;
	}

}
