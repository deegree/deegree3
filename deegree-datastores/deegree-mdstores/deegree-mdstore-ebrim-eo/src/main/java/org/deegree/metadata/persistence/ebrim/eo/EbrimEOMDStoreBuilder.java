/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.metadata.persistence.ebrim.eo;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.ebrim.RegistryPackage;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.ebrim.eo.jaxb.EbrimEOMDStoreConfig;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * Responsible for building ebrim eo stores.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class EbrimEOMDStoreBuilder implements ResourceBuilder<MetadataStore<? extends MetadataRecord>> {

	private ResourceMetadata<MetadataStore<? extends MetadataRecord>> metadata;

	private Workspace workspace;

	private EbrimEOMDStoreConfig storeConfig;

	public EbrimEOMDStoreBuilder(ResourceMetadata<MetadataStore<? extends MetadataRecord>> metadata,
			Workspace workspace, EbrimEOMDStoreConfig storeConfig) {
		this.metadata = metadata;
		this.workspace = workspace;
		this.storeConfig = storeConfig;
	}

	@Override
	public MetadataStore<? extends MetadataRecord> build() {
		File queriesDir = null;
		String dir = null;
		try {
			dir = storeConfig.getAdhocQueriesDirectory();
			if (dir != null) {
				URL resolved = metadata.getLocation().resolveToUrl(dir);
				queriesDir = new File(resolved.toURI());
			}
		}
		catch (URISyntaxException e) {
			String msg = "Could not resolve path to the queries directory: " + dir;
			throw new ResourceInitException(msg, e);
		}

		String profile = null;
		RegistryPackage rp = null;
		profile = storeConfig.getExtensionPackage();
		Date lastModified = null;
		try {
			if (profile != null) {
				URL resolved = metadata.getLocation().resolveToUrl(profile);
				File f = new File(resolved.toURI());
				lastModified = new Date(f.lastModified());
				XMLInputFactory inf = XMLInputFactory.newInstance();
				XMLStreamReader reader = inf.createXMLStreamReader(resolved.openStream());
				rp = new RegistryPackage(reader);
			}
		}
		catch (MalformedURLException e) {
			String msg = "Could not resolve path to the profile: " + profile;
			throw new ResourceInitException(msg, e);
		}
		catch (XMLStreamException e) {
			String msg = "Could not resolve profile: " + profile;
			throw new ResourceInitException(msg, e);
		}
		catch (IOException e) {
			String msg = "Could not resolve profile: " + profile;
			throw new ResourceInitException(msg, e);
		}
		catch (URISyntaxException e) {
			String msg = "Could not resolve path to the profile: " + profile;
			throw new ResourceInitException(msg, e);
		}
		long queryTimeout = storeConfig.getQueryTimeout() == null ? 0 : storeConfig.getQueryTimeout().intValue();

		return new EbrimEOMDStore(storeConfig.getJDBCConnId(), queriesDir, rp, lastModified, queryTimeout, metadata,
				workspace);
	}

}
