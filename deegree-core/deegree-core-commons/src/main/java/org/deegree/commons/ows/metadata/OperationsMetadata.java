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
package org.deegree.commons.ows.metadata;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.domain.Domain;
import org.deegree.commons.ows.metadata.operation.Operation;

/**
 * Encapsulates metadata on operations provided by an OGC web service (as reported in the
 * capabilities document).
 * <p>
 * Data model has been designed to capture the expressiveness of all OWS specifications
 * and versions and was verified against the following specifications:
 * <ul>
 * <li>OWS Common 2.0</li>
 * </ul>
 * </p>
 * <p>
 * From OWS Common 2.0: <cite>Metadata about the operations and related abilities
 * specified by this service and implemented by this server, including the URLs for
 * operation requests. The basic contents of this section shall be the same for all OWS
 * types, but individual services can add elements and/or change the optionality of
 * optional elements.</cite>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class OperationsMetadata {

	private final Map<String, Operation> operationNameToMD = new LinkedHashMap<String, Operation>();

	private final List<Domain> parameters;

	private final List<Domain> constraints;

	private final List<OMElement> extendedCapabilities;

	public OperationsMetadata(List<Operation> operations, List<Domain> parameters, List<Domain> constraints,
			List<OMElement> extendedCapabilities) {
		for (Operation operation : operations) {
			operationNameToMD.put(operation.getName(), operation);
		}
		if (parameters != null) {
			this.parameters = parameters;
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
		if (extendedCapabilities != null) {
			this.extendedCapabilities = extendedCapabilities;
		}
		else {
			this.extendedCapabilities = new ArrayList<OMElement>();
		}
	}

	/**
	 * Returns the metadata for all operations.
	 * <p>
	 * From OWS Common 2.0: <cite>Metadata for unordered list of all the (requests for)
	 * operations that this server interface implements. The list of required and optional
	 * operations implemented shall be specified in the Implementation Specification for
	 * this service.</cite>
	 * </p>
	 * @return operation metadata, may be empty, but never <code>null</code>
	 */
	public List<Operation> getOperation() {
		return new ArrayList<Operation>(operationNameToMD.values());
	}

	/**
	 * Returns the metadata for the specified operation name.
	 * @param operationName name of the operation, can be <code>null</code>
	 * @return operation metadata or <code>null</code> if no metadata for operation
	 * available
	 */
	public Operation getOperation(String operationName) {
		return operationNameToMD.get(operationName);
	}

	/**
	 * Returns the endpoint {@link URL}s for the specified operation and method HTTP-GET.
	 * @return endpoint URLs, can be empty, but never <code>null</code>
	 */
	public List<URL> getGetUrls(String operationName) {
		Operation operation = getOperation(operationName);
		if (operation != null) {
			return operation.getGetUrls();
		}
		return null;
	}

	/**
	 * Returns the endpoint {@link URL}s for the specified operation and method HTTP-POST.
	 * @return endpoint URLs, can be empty, but never <code>null</code>
	 */
	public List<URL> getPostUrls(String operationName) {
		Operation operation = getOperation(operationName);
		if (operation != null) {
			return operation.getPostUrls();
		}
		return null;
	}

	/**
	 * Returns the global parameter validity domains that apply to all operations.
	 * <p>
	 * From OWS Common 2.0: <cite>Optional unordered list of parameter valid domains that
	 * each apply to one or more operations which this server interface implements. The
	 * list of required and optional parameter domain limitations shall be specified in
	 * the Implementation Specification for this service.</cite>
	 * </p>
	 * @return global parameter validity domains, may be empty, but never
	 * <code>null</code>
	 */
	public List<Domain> getParameters() {
		return parameters;
	}

	/**
	 * Returns the global domain validity constraints that apply to all operations.
	 * <p>
	 * From OWS Common 2.0: <cite>Optional unordered list of valid domain constraints on
	 * non-parameter quantities that each apply to this server. The list of required and
	 * optional constraints shall be specified in the Implementation Specification for
	 * this service.</cite>
	 * </p>
	 * @return global domain validity constraints, may be empty, but never
	 * <code>null</code>
	 */
	public List<Domain> getConstraints() {
		return constraints;
	}

	/**
	 * Returns the extended capabilities.
	 * <p>
	 * From OWS Common 2.0: <cite>Individual software vendors and servers can use this
	 * element to provide metadata about any additional server abilities.</cite>
	 * </p>
	 *
	 * return extended capabilities, may be empty, but never <code>null</code>
	 */
	public List<OMElement> getExtendedCapabilities() {
		return extendedCapabilities;
	}

}
