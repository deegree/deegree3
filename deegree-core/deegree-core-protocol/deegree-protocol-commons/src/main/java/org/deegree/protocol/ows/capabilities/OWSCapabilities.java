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
package org.deegree.protocol.ows.capabilities;

import java.util.List;

import org.deegree.commons.ows.metadata.OperationsMetadata;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.tom.ows.Version;

/**
 * Specification and version agnostic representation of the capabilities reported by an
 * OGC Web Service.
 * <p>
 * Data model has been designed to capture the expressiveness of all OWS specifications
 * and versions and was verified against the following specifications:
 * <ul>
 * <li>OWS Common 2.0</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class OWSCapabilities {

	private final Version version;

	private final String updateSequence;

	private final ServiceIdentification serviceIdentification;

	private final ServiceProvider serviceProvider;

	private final OperationsMetadata operationsMetadata;

	private final List<String> languages;

	/**
	 * Creates a new {@link OWSCapabilities} instance.
	 * @param version service specification version, may be <code>null</code>
	 * @param updateSequence service metadata document version, may be <code>null</code>
	 * @param serviceIdentification general server-specific metadata, may be
	 * <code>null</code>
	 * @param serviceProvider metadata about the organization that provides the server,
	 * may be <code>null</code>
	 * @param operationsMetadata metadata about the operations and related abilities
	 * implemented by the server, may be <code>null</code>
	 * @param languages list of languages that the server is able to fully support, may be
	 * <code>null</code>
	 */
	public OWSCapabilities(Version version, String updateSequence, ServiceIdentification serviceIdentification,
			ServiceProvider serviceProvider, OperationsMetadata operationsMetadata, List<String> languages) {
		this.version = version;
		this.updateSequence = updateSequence;
		this.serviceIdentification = serviceIdentification;
		this.serviceProvider = serviceProvider;
		this.operationsMetadata = operationsMetadata;
		this.languages = languages;
	}

	/**
	 * Returns the service specification version.
	 * @return specification version, may be <code>null</code>
	 */
	public Version getVersion() {
		return version;
	}

	/**
	 * Returns the service metadata document version.
	 * @return service metadata document version, may be <code>null</code>
	 */
	public String getUpdateSequence() {
		return updateSequence;
	}

	/**
	 * Returns the general server-specific metadata.
	 * @return general server-specific metadata, may be <code>null</code>
	 */
	public ServiceIdentification getServiceIdentification() {
		return serviceIdentification;
	}

	/**
	 * Returns the metadata about the organization that provides the server.
	 * @return metadata about the organization that provides the server, may be
	 * <code>null</code>
	 */
	public ServiceProvider getServiceProvider() {
		return serviceProvider;
	}

	/**
	 * Returns the metadata about the operations and related abilities implemented by the
	 * server.
	 * @return metadata about the operations and related abilities implemented by the
	 * server, may be <code>null</code>
	 */
	public OperationsMetadata getOperationsMetadata() {
		return operationsMetadata;
	}

	/**
	 * Returns the list of languages that the server is able to fully support.
	 * @return list of languages that the server is able to fully support, may be
	 * <code>null</code>
	 */
	public List<String> getLanguages() {
		return languages;
	}

}
