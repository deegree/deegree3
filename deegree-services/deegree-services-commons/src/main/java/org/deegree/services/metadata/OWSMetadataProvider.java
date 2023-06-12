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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.metadata;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.services.OWS;
import org.deegree.workspace.Resource;

/**
 * Implementations provide metadata that {@link OWS} instances can use in their
 * <code>GetCapabilities</code> responses.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 */
public interface OWSMetadataProvider extends Resource {

	/**
	 * Returns the {@link ServiceIdentification} metadata.
	 * @return service identification bean, possibly <code>null</code>
	 */
	ServiceIdentification getServiceIdentification();

	/**
	 * Returns the {@link ServiceProvider} metadata.
	 * @return service provider bean, possibly <code>null</code>
	 */
	ServiceProvider getServiceProvider();

	/**
	 * Returns the <code>ExtendedCapabilities</code> sections.
	 * @return mapping from protocol version string (may be "default") to a list of
	 * extended capabilities (represented as DOM tree), possibly <code>null</code>
	 */
	Map<String, List<OMElement>> getExtendedCapabilities();

	/**
	 * Returns {@link DatasetMetadata} for all datasets.
	 * @return list of dataset metadata, possibly <code>null</code>
	 */
	List<DatasetMetadata> getDatasetMetadata();

	/**
	 * @return a mapping from metadata authority name to authority url
	 */
	Map<String, String> getExternalMetadataAuthorities();

	/**
	 * Returns data metadata for the specified dataset.
	 * @param name for layers, a qname with only a local name is used, for feature types
	 * its qname
	 * @return metadata, possibly <code>null</code> (no metadata available)
	 */
	DatasetMetadata getDatasetMetadata(QName name);

	/**
	 * Returns a list of data metadata for the specified dataset.
	 * @param name for layers, a qname with only a local name is used, for feature types
	 * its qname
	 * @return metadata, may be empty but never <code>null</code> (no metadata available)
	 */
	List<DatasetMetadata> getAllDatasetMetadata(QName name);

}
