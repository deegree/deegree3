/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

package org.deegree.feature.persistence.memory;

import static org.deegree.protocol.wfs.transaction.action.IDGenMode.USE_EXISTING;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.memory.jaxb.GMLVersionType;
import org.deegree.feature.persistence.memory.jaxb.MemoryFeatureStoreConfig;
import org.deegree.feature.persistence.memory.jaxb.MemoryFeatureStoreConfig.GMLFeatureCollection;
import org.deegree.feature.persistence.memory.jaxb.MemoryFeatureStoreConfig.GMLSchema;
import org.deegree.feature.persistence.memory.jaxb.MemoryFeatureStoreConfig.NamespaceHint;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>MemoryFeatureStoreBuilder</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class MemoryFeatureStoreBuilder implements ResourceBuilder<FeatureStore> {

	private static final Logger LOG = LoggerFactory.getLogger(MemoryFeatureStoreBuilder.class);

	private MemoryFeatureStoreMetadata metadata;

	private MemoryFeatureStoreConfig config;

	private Workspace workspace;

	public MemoryFeatureStoreBuilder(MemoryFeatureStoreMetadata metadata, MemoryFeatureStoreConfig config,
			Workspace workspace) {
		this.metadata = metadata;
		this.config = config;
		this.workspace = workspace;
	}

	@Override
	public FeatureStore build() {
		MemoryFeatureStore fs;
		ICRS storageCRS = null;
		AppSchema schema = null;
		try {
			String[] schemaURLs = new String[config.getGMLSchema().size()];
			int i = 0;
			GMLVersionType gmlVersionType = null;
			for (GMLSchema jaxbSchemaURL : config.getGMLSchema()) {
				schemaURLs[i++] = metadata.getLocation()
					.resolveToFile(jaxbSchemaURL.getValue().trim())
					.toURI()
					.toURL()
					.toString();
				// TODO what about different versions at the same time?
				gmlVersionType = jaxbSchemaURL.getVersion();
			}

			GMLAppSchemaReader decoder = null;
			if (schemaURLs.length == 1 && schemaURLs[0].startsWith("file:")) {
				File file = new File(new URL(schemaURLs[0]).toURI());
				decoder = new GMLAppSchemaReader(GMLVersion.valueOf(gmlVersionType.name()),
						getHintMap(config.getNamespaceHint()), file);
			}
			else {
				decoder = new GMLAppSchemaReader(GMLVersion.valueOf(gmlVersionType.name()),
						getHintMap(config.getNamespaceHint()), schemaURLs);
			}
			schema = decoder.extractAppSchema();
			if (config.getStorageCRS() != null) {
				storageCRS = CRSManager.lookup(config.getStorageCRS());
			}
		}
		catch (Exception e) {
			String msg = Messages.getMessage("STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage());
			LOG.error(msg, e);
			throw new ResourceInitException(msg, e);
		}

		try {
			ConnectionProvider lockProvider = workspace.getResource(ConnectionProviderProvider.class, "LOCK_DB");
			fs = new MemoryFeatureStore(schema, storageCRS, metadata, lockProvider);
		}
		catch (FeatureStoreException ex) {
			throw new ResourceInitException(ex.getLocalizedMessage(), ex);
		}
		for (GMLFeatureCollection datasetFile : config.getGMLFeatureCollection()) {
			if (datasetFile != null) {
				try {
					GMLVersion version = GMLVersion.valueOf(datasetFile.getVersion().name());
					URL docURL = metadata.getLocation().resolveToFile(datasetFile.getValue().trim()).toURI().toURL();
					GMLStreamReader gmlStream = GMLInputFactory.createGMLStreamReader(version, docURL);
					gmlStream.setApplicationSchema(schema);
					LOG.info("Populating feature store with features from file '" + docURL + "'...");
					FeatureCollection fc = (FeatureCollection) gmlStream.readFeature();
					gmlStream.getIdContext().resolveLocalRefs();

					FeatureStoreTransaction ta = fs.acquireTransaction();
					int fids = ta.performInsert(fc, USE_EXISTING).size();
					LOG.info("Inserted " + fids + " features.");
					ta.commit();
				}
				catch (Exception e) {
					String msg = Messages.getMessage("STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage());
					LOG.error(msg);
					LOG.trace("Stack trace:", e);
					throw new ResourceInitException(msg, e);
				}
			}
		}
		return fs;
	}

	private static Map<String, String> getHintMap(List<NamespaceHint> hints) {
		Map<String, String> prefixToNs = new HashMap<String, String>();
		for (NamespaceHint namespaceHint : hints) {
			prefixToNs.put(namespaceHint.getPrefix(), namespaceHint.getNamespaceURI());
		}
		return prefixToNs;
	}

}
