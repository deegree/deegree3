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
package org.deegree.services.metadata.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;

/**
 * {@link OWSMetadataProvider} implementation that is a simple bean providing the
 * metadata.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class DefaultOWSMetadataProvider implements OWSMetadataProvider {

	private final ServiceIdentification serviceIdentification;

	private final ServiceProvider serviceProvider;

	private final List<DatasetMetadata> datasetMetadata;

	private final Map<QName, List<DatasetMetadata>> datasetNameToMetadata = new HashMap<QName, List<DatasetMetadata>>();

	private final Map<String, List<OMElement>> extendedCapabilities;

	private final Map<String, String> authorities;

	private ResourceMetadata<OWSMetadataProvider> metadata;

	public DefaultOWSMetadataProvider(ServiceIdentification si, ServiceProvider sp,
			Map<String, List<OMElement>> extendedCapabilities, List<DatasetMetadata> datasetMetadata,
			Map<String, String> authorities, ResourceMetadata<OWSMetadataProvider> metadata) {
		this.serviceIdentification = si;
		this.serviceProvider = sp;
		this.extendedCapabilities = extendedCapabilities;
		this.metadata = metadata;
		if (datasetMetadata != null) {
			this.datasetMetadata = datasetMetadata;
		}
		else {
			this.datasetMetadata = Collections.emptyList();
		}
		for (DatasetMetadata dsMd : this.datasetMetadata) {
			QName dsMdName = dsMd.getQName();
			if (!this.datasetNameToMetadata.containsKey(dsMdName))
				this.datasetNameToMetadata.put(dsMdName, new ArrayList<DatasetMetadata>());
			this.datasetNameToMetadata.get(dsMdName).add(dsMd);
		}
		this.authorities = authorities;
	}

	@Override
	public void init() {
		// nothing to init
	}

	@Override
	public void destroy() {
		// nothing to release
	}

	@Override
	public ServiceIdentification getServiceIdentification() {
		return serviceIdentification;
	}

	@Override
	public ServiceProvider getServiceProvider() {
		return serviceProvider;
	}

	@Override
	public Map<String, List<OMElement>> getExtendedCapabilities() {
		return extendedCapabilities;
	}

	@Override
	public List<DatasetMetadata> getDatasetMetadata() {
		return datasetMetadata;
	}

	@Override
	public DatasetMetadata getDatasetMetadata(QName name) {
		List<DatasetMetadata> md = datasetNameToMetadata.get(name);
		if (md == null || md.isEmpty()) {
			for (Entry<QName, List<DatasetMetadata>> e : datasetNameToMetadata.entrySet()) {
				if (e.getKey().getLocalPart().equalsIgnoreCase(name.getLocalPart())) {
					List<DatasetMetadata> dsMd = e.getValue();
					if (!dsMd.isEmpty())
						return dsMd.get(0);
				}
			}
			return null;
		}
		return md.get(0);
	}

	@Override
	public List<DatasetMetadata> getAllDatasetMetadata(QName name) {
		List<DatasetMetadata> datasetMetadata = new ArrayList<DatasetMetadata>();
		List<DatasetMetadata> mds = datasetNameToMetadata.get(name);
		if (mds == null || mds.isEmpty()) {
			for (Entry<QName, List<DatasetMetadata>> e : datasetNameToMetadata.entrySet()) {
				if (e.getKey().getLocalPart().equalsIgnoreCase(name.getLocalPart())) {
					datasetMetadata.addAll(e.getValue());
				}
			}
		}
		else {
			datasetMetadata.addAll(mds);
		}
		return datasetMetadata;
	}

	@Override
	public Map<String, String> getExternalMetadataAuthorities() {
		return authorities;
	}

	@Override
	public ResourceMetadata<? extends Resource> getMetadata() {
		return metadata;
	}

}
