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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.cs.persistence;

import static java.lang.System.currentTimeMillis;
import static org.deegree.commons.xml.stax.XMLStreamUtils.closeQuietly;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.tom.Object;
import org.deegree.commons.tom.ReferenceResolver;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSResource;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.exceptions.CRSStoreException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.i18n.Messages;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.TransformationFactory;
import org.deegree.workspace.Destroyable;
import org.deegree.workspace.Initializable;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for creating and retrieving {@link CRSStore} and {@link CRSStoreProvider}
 * instances.
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public class CRSManager implements Initializable, Destroyable {

	private static Logger LOG = LoggerFactory.getLogger(CRSManager.class);

	private static Map<String, CRSStoreProvider> nsToProvider = null;

	private static Map<String, CRSStore> idToCRSStore = Collections.synchronizedMap(new HashMap<String, CRSStore>());

	// store ids in order of requesting, workspace stores should overwrite the default
	// store!
	private static List<String> storeIds = Collections.synchronizedList(new LinkedList<String>());

	private static Map<String, TransformationFactory> idToTransF = new HashMap<String, TransformationFactory>();

	private Workspace workspace;

	private static boolean defaultInitialized = false;

	static {
		new CRSManager().initDefault();
	}

	private void initDefault() {
		synchronized (CRSManager.class) {
			if (defaultInitialized) {
				return;
			}
			LOG.info("--------------------------------------------------------------------------------");
			LOG.info("No 'crs' directory -- use default configuration.");
			LOG.info("--------------------------------------------------------------------------------");

			URL defaultConfig = CRSManager.class.getResource("default.xml");
			try {
				handleConfigFile(defaultConfig, false);
				defaultInitialized = true;
			}
			catch (Throwable t) {
				LOG.error("The default configuration could not be loaded: " + t.getMessage());
			}
		}
	}

	@Override
	public void init(Workspace workspace) {
		this.workspace = workspace;
		initDefault();
		init(new File(((DefaultWorkspace) workspace).getLocation(), "crs"));
	}

	@Override
	public void destroy(Workspace workspace) {
		LOG.info("Clear CRS store and transformation map");
		idToCRSStore.clear();
		idToTransF.clear();
		storeIds.clear();
		defaultInitialized = false;
		new CRSManager().initDefault();
	}

	/**
	 * Initializes the {@link CRSManager} by loading all crs store configurations from the
	 * given directory. If null, or directory does not exist the default will be used.
	 * @param crsDir
	 */
	public void init(File crsDir) {
		initDefault();
		if (crsDir != null && crsDir.exists()) {
			LOG.info("--------------------------------------------------------------------------------");
			LOG.info("Setting up crs stores.");
			LOG.info("--------------------------------------------------------------------------------");

			File[] crsConfigFiles = crsDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".xml");
				}
			});

			for (File crsConfigFile : crsConfigFiles) {
				try {
					boolean prefer = true;
					if ("default.xml".equals(crsConfigFile.getName())) {
						prefer = false;
						remove("default");
						LOG.info("CRS store " + crsConfigFile + " overwrites internal configuration!");
					}
					handleConfigFile(crsConfigFile.toURI().toURL(), prefer);
				}
				catch (Throwable t) {
					LOG.error("Unable to read config file '" + crsConfigFile + "'.", t);
				}
			}
			LOG.info("");
		}
		else {
			LOG.info("Could not set up CRS stores: CRS workspace directory " + crsDir + " is null or does not exist.");
		}
	}

	private void handleConfigFile(URL crsConfigFile, boolean prefer) {
		String fileName = crsConfigFile.getFile();
		int fileNameStart = fileName.lastIndexOf('/') + 1;
		// 4 is the length of ".xml"
		String crsId = fileName.substring(fileNameStart, fileName.length() - 4);
		LOG.info("Setting up crs store '" + crsId + "' from file '" + fileName + "'..." + "");
		try {
			CRSStore crss = create(crsConfigFile.toURI().toURL());
			registerAndInit(crss, crsId, prefer);
		}
		catch (Exception e) {
			LOG.error("Error creating crs store: " + e.getMessage());
			LOG.trace("Stack trace:", e);
		}
	}

	/**
	 * Returns an uninitialized {@link CRSStore} instance that's created from the
	 * specified CRSStore configuration document.
	 * @param configURL URL of the configuration document, must not be <code>null</code>
	 * @return corresponding {@link CRSStore} instance, not yet initialized, never
	 * <code>null</code>
	 * @throws CRSStoreException if the creation fails, e.g. due to a configuration error
	 */
	public synchronized CRSStore create(URL configURL) throws CRSStoreException {
		String namespace = null;
		XMLStreamReader xmlReader = null;
		InputStream urlStream = null;
		try {
			urlStream = configURL.openStream();
			xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(urlStream);
			XMLStreamUtils.nextElement(xmlReader);
			namespace = xmlReader.getNamespaceURI();
		}
		catch (Exception e) {
			String msg = Messages.get("CRSManager.CREATING_STORE_FAILED", configURL);
			LOG.error(msg);
			throw new CRSStoreException(msg);
		}
		finally {
			closeQuietly(xmlReader);
			IOUtils.closeQuietly(urlStream);
		}
		LOG.debug("Config namespace: '" + namespace + "'");
		CRSStoreProvider provider = getProviders().get(namespace);
		if (provider == null) {
			String msg = Messages.get("CRSManager.MISSING_PROVIDER", namespace, configURL);
			LOG.error(msg);
			throw new CRSStoreException(msg);
		}
		return provider.getCRSStore(configURL, workspace);
	}

	/**
	 * Returns all available {@link CRSStore} providers.
	 * @return all available providers, keys: config namespace, value: provider instance
	 */
	private synchronized Map<String, CRSStoreProvider> getProviders() {
		if (nsToProvider == null) {
			nsToProvider = new HashMap<String, CRSStoreProvider>();
			try {
				ServiceLoader<CRSStoreProvider> loaded;
				if (workspace != null) {
					loaded = ServiceLoader.load(CRSStoreProvider.class, workspace.getModuleClassLoader());
				}
				else {
					loaded = ServiceLoader.load(CRSStoreProvider.class);
				}
				for (CRSStoreProvider provider : loaded) {
					LOG.debug("CRS store provider: " + provider + ", namespace: " + provider.getConfigNamespace());
					if (nsToProvider.containsKey(provider.getConfigNamespace())) {
						LOG.error("Multiple crs store providers for config namespace: '" + provider.getConfigNamespace()
								+ "' on classpath -- omitting provider '" + provider.getClass().getName() + "'.");
						continue;
					}
					nsToProvider.put(provider.getConfigNamespace(), provider);
				}
			}
			catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}

		}
		return nsToProvider;
	}

	private static void registerAndInit(CRSStore crss, String id, boolean prefer) throws CRSStoreException {
		if (id != null) {
			if (idToCRSStore.containsKey(id)) {
				throw new CRSStoreException(Messages.getMessage("CRSManager.DUPLICATE_ID", id));
			}
			LOG.info("Registering global crs store with id '" + id + "', type: '" + crss.getClass().getName() + "'");
			idToTransF.put(id, new TransformationFactory(crss));
			idToCRSStore.put(id, crss);
			if (prefer) {
				storeIds.add(0, id);
			}
			else {
				storeIds.add(id);
			}
			crss.init();
		}
	}

	/**
	 * Returns all active {@link CRSStore}s.
	 * @return the {@link CRSStore}s instance, may be empty but never <code>null</code>
	 */
	public static Collection<CRSStore> getAll() {
		return idToCRSStore.values();
	}

	/**
	 * Returns the {@link CRSStore} instance with the specified identifier or
	 * <code>null</code> if an assigned {@link CRSStore} is missing
	 * @param id identifier of the {@link CRSStore} instance
	 * @return the corresponding {@link CRSStore} instance or the default {@link CRSStore}
	 * instance if no such instance has been created or <code>null</code> if the default
	 * one could also not be created.
	 */
	public static CRSStore get(String id) {
		return idToCRSStore.get(id);
	}

	public static Collection<String> getCrsStoreIds() {
		return idToCRSStore.keySet();
	}

	/*********************************************/
	/**
	 * This method allows to get all {@link ICRS} from all stores.
	 * @return all configured CRSs.
	 * @throws CRSConfigurationException if the implementation was confronted by an
	 * exception and could not deliver the requested crs. This exception should not be
	 * thrown if no CoordinateSystems were found, in the latter case an empty List ( a
	 * list with size == 0 ) should be returned.
	 */
	public List<ICRS> getAvailableCRSs() throws CRSConfigurationException {
		List<ICRS> result = new ArrayList<ICRS>();
		for (CRSStore store : getAll()) {
			result.addAll(store.getAvailableCRSs());
		}
		return result;
	}

	/**
	 * This methods allows to get all available identifiers and not in the
	 * coordinatesystems themselves from all stores.
	 * @return the identifiers of all configured CRSs.
	 * @throws CRSConfigurationException if the implementation was confronted by an
	 * exception and could not deliver the requested crs. This exception should not be
	 * thrown if no CoordinateSystems were found, in the latter case an empty List ( a
	 * list with size == 0 ) should be returned.
	 */
	public List<CRSCodeType[]> getAvailableCRSCodes() throws CRSConfigurationException {
		List<CRSCodeType[]> result = new ArrayList<CRSCodeType[]>();
		for (CRSStore store : getAll()) {
			result.addAll(store.getAvailableCRSCodes());
		}
		return result;

	}

	/***************************************************/

	/**
	 * Returns a {@link CRSRef} instance which is not resolved.
	 * @param uri the uri of the resource which is used to resolve the reference
	 * @return an unresolved {@link CRSRef} instance
	 */
	public static CRSRef getCRSRef(String uri) {
		return getCRSRef(uri, false);
	}

	/**
	 * Return a {@link CRSRef} instance which is not resolved.
	 * @param uri the uri of the resource which is used to resolve the reference
	 * @param forceXY flag indicating if the axis order should be as configured or xy
	 * @return an unresolved {@link CRSRef} instance
	 */
	public static CRSRef getCRSRef(String uri, final boolean forceXY) {
		return new CRSRef(new ReferenceResolver() {
			@Override
			public Object getObject(String uri, String baseURL) {
				ICRS crs = null;
				try {
					crs = lookup(uri, forceXY);
				}
				catch (UnknownCRSException e) {
					LOG.debug("Could not find CRS with uri " + uri + "; return null.");
				}
				return crs;
			}
		}, uri, null, forceXY);
	}

	/**
	 * Returns a {@link CRSRef} wrapping the given CRS. The uri of the reference is set to
	 * the id of the given {@link ICRS}
	 * @param crs the {@link ICRS} to wrap
	 * @return
	 */
	public static CRSRef getCRSRef(final ICRS crs) {
		CRSRef newRef = new CRSRef(new ReferenceResolver() {
			@Override
			public Object getObject(String uri, String baseURL) {
				return crs;
			}
		}, crs.getId(), null);
		return newRef;
	}

	/**
	 * Creates a direct {@link ICRS} instance from the given name not just a
	 * {@link CRSRef}, if no {@link ICRS} was found an {@link UnknownCRSException} will be
	 * thrown. All configured {@link CRSStore}s will be considered and the first match
	 * returned.
	 * @param name of the crs, e.g. EPSG:4326
	 * @return a direct {@link ICRS} instance corresponding to the given name not just a
	 * {@link CRSRef}, using all configured {@link CRSStore}s.
	 * @throws UnknownCRSException if a {@link ICRS} with the name is not known
	 */
	public synchronized static ICRS lookup(String name) throws UnknownCRSException {
		return lookup(name, false);
	}

	/**
	 * Creates a direct {@link ICRS} instance from the given name not just a
	 * {@link CRSRef}, if no {@link ICRS} was found an {@link UnknownCRSException} will be
	 * thrown. All configured {@link CRSStore}s will be considered and the first match
	 * returned.
	 * @param name of the crs, e.g. EPSG:4326
	 * @param forceXY true if the axis order of the coordinate system should be x/y
	 * (EAST/NORTH; WEST/SOUTH); false id the defined axis order should be used
	 * @return a direct {@link ICRS} instance corresponding to the given name not just a
	 * {@link CRSRef}, using all configured {@link CRSStore}s.
	 * @throws UnknownCRSException if a {@link ICRS} with the name is not known
	 */
	public synchronized static ICRS lookup(String name, boolean forceXY) throws UnknownCRSException {
		return lookup(null, name, forceXY);
	}

	/**
	 * Creates a direct {@link ICRS} instance from the given {@link CRSCodeType} not just
	 * a {@link CRSRef}, if no {@link ICRS} was found an {@link UnknownCRSException} will
	 * be thrown. All configured {@link CRSStore}s will be considered and the first match
	 * returned.
	 * @param codeType the {@link CodeType} of the CRS to return
	 * @return a direct {@link ICRS} instance with the given {@link CodeType} not just a
	 * {@link CRSRef}
	 * @throws UnknownCRSException if a {@link ICRS} with the name is not known
	 */
	public synchronized static ICRS lookup(CRSCodeType codeType) throws UnknownCRSException {
		return lookup(null, codeType);
	}

	/**
	 * Creates a direct {@link ICRS} instance from the given name using not just a
	 * {@link CRSRef} the given storeID, if no {@link ICRS} was found in the given store
	 * an {@link UnknownCRSException} will be thrown.
	 * @param storeId identifier of the {@link CRSStore} looking for the {@link ICRS} with
	 * the given name, may be <code>null</code> if in all {@link CRSStore}s should be
	 * searched
	 * @param name of the crs, e.g. EPSG:31466
	 * @return a direct {@link ICRS} instance not just a {@link CRSRef} corresponding to
	 * the given name from the {@link CRSStore} with the given id
	 * @throws UnknownCRSException if a {@link ICRS} with the name is not known
	 */
	public synchronized static ICRS lookup(String storeId, String name) throws UnknownCRSException {
		return lookup(storeId, name, false);
	}

	/**
	 * Creates a direct {@link ICRS} instance with the given name not just a
	 * {@link CRSRef} using the given storeId, if no {@link ICRS} was found an
	 * {@link UnknownCRSException} will be thrown.
	 * @param storeIdName identifier of the store, looking for the {@link ICRS} instance,
	 * may be <code>null</code> if in all {@link CRSStore}s should be searched
	 * @param name of the crs, e.g. EPSG:31466
	 * @param forceXY true if the axis order of the coordinate system should be x/y
	 * (EAST/NORTH; WEST/SOUTH); false id the defined axis order should be used
	 * @return a direct {@link ICRS} instance not just a {@link CRSRef} corresponding to
	 * the given name from the {@link CRSStore} with the given id
	 * @throws UnknownCRSException if a {@link ICRS} with the name is not known
	 */
	public synchronized static ICRS lookup(String storeIdName, String name, boolean forceXY)
			throws UnknownCRSException {
		CRSStore crsStore = get(storeIdName);
		if (crsStore != null) {
			return lookupStore(crsStore, name, forceXY);
		}
		else {
			for (String stId : storeIds) {
				CRSStore store = idToCRSStore.get(stId);
				try {
					ICRS crs = lookupStore(store, name, forceXY);
					if (crs != null) {
						return crs;
					}
				}
				catch (UnknownCRSException e) {
					// nothing to do
				}
			}
		}
		throw new UnknownCRSException(name);
	}

	/**
	 * Creates a direct {@link ICRS} instance from the given {@link CRSCodeType}
	 * notjustjust a {@link CRSRef} using the given storeId, if no {@link ICRS} was found
	 * an {@link UnknownCRSException} will be thrown.
	 * @param storeId identifier of the store, looking for the {@link ICRS} instance, may
	 * be <code>null</code> if in all {@link CRSStore}s should be searched
	 * @param crsCodeType the {@link CRSCodeType} of the ICRS to return
	 * @return a real {@link ICRS} not just a reference.
	 * @throws UnknownCRSException if a {@link ICRS} with the name is not known
	 */
	public synchronized static ICRS lookup(String storeId, CRSCodeType crsCodeType) throws UnknownCRSException {
		CRSStore crsStore = get(storeId);
		if (crsStore != null) {
			return lookupStore(crsStore, crsCodeType, false);
		}
		else {
			for (String sId : storeIds) {
				CRSStore store = idToCRSStore.get(sId);
				try {
					ICRS crs = lookupStore(store, crsCodeType, false);
					if (crs != null) {
						return crs;
					}
				}
				catch (UnknownCRSException e) {
					// nothing to do
				}
			}
		}
		throw new UnknownCRSException(crsCodeType.getOriginal());
	}

	/**
	 * Creates a {@link ICRS} from the given name using the given {@link CRSStore}, if no
	 * {@link ICRS} was found an {@link UnknownCRSException} will be thrown.
	 * @param crsStore {@link CRSStore} instance, looking for the {@link ICRS} instance,
	 * may not be <code>null</code>, if in all {@link CRSStore}s should be searched
	 * @param name of the crs, e.g. EPSG:31466
	 * @param forceXY true if the axis order of the coordinate system should be x/y
	 * (EAST/NORTH; WEST/SOUTH); false id the defined axis order should be used
	 * @throws UnknownCRSException if name is not known
	 * @throws IllegalArgumentException if crsStore is null
	 */
	private static ICRS lookupStore(CRSStore crsStore, String name, boolean forceXY) throws UnknownCRSException {
		if (crsStore == null) {
			throw new IllegalArgumentException(Messages.get("CRSManager.STORE_NULL"));
		}
		long sT = currentTimeMillis();
		long eT = currentTimeMillis() - sT;
		LOG.debug("Getting provider: " + crsStore + " took: " + eT + " ms.");
		ICRS realCRS = null;
		try {
			sT = currentTimeMillis();
			realCRS = crsStore.getCRSByCode(CRSCodeType.valueOf(name), forceXY);
			if (realCRS == null) {
				// TODO: try to get CRS with lower case id (bug with id handling in the
				// abstractStore cache)
				realCRS = crsStore.getCRSByCode(CRSCodeType.valueOf(name.toLowerCase()), forceXY);
			}
			eT = currentTimeMillis() - sT;
			LOG.debug("Getting crs ( " + name + " )from provider: " + crsStore + " took: " + eT + " ms.");
		}
		catch (CRSConfigurationException e) {
			String msg = Messages.get("CRSManager.BROKEN_CRS_CONFIG", name, e.getMessage());
			LOG.debug(msg, e);
			throw new RuntimeException(msg, e);
		}
		if (realCRS == null) {
			throw new UnknownCRSException(name);
		}
		LOG.debug("Successfully created the crs with id: " + name);
		return realCRS;
	}

	/**
	 * Creates a {@link ICRS} from the given name using the given {@link CRSStore}, if no
	 * {@link ICRS} was found an {@link UnknownCRSException} will be thrown.
	 * @param crsStore {@link CRSStore} instance, looking for the {@link ICRS} instance,
	 * may not be <code>null</code>, if in all {@link CRSStore}s should be searched
	 * @param crsCodeType of the crs, e.g. EPSG:31466
	 * @param forceXY true if the axis order of the coordinate system should be x/y
	 * (EAST/NORTH; WEST/SOUTH); false id the defined axis order should be used
	 * @throws UnknownCRSException if name is not known
	 * @throws IllegalArgumentException if crsStore is null
	 */
	private static ICRS lookupStore(CRSStore crsStore, CRSCodeType crsCodeType, boolean forceXY)
			throws UnknownCRSException {
		if (crsStore == null) {
			throw new IllegalArgumentException(Messages.get("CRSManager.STORE_NULL"));
		}
		ICRS realCRS = null;
		try {
			realCRS = crsStore.getCRSByCode(crsCodeType);
		}
		catch (CRSConfigurationException e) {
			LOG.error(e.getMessage(), e);
		}
		if (realCRS == null) {
			throw new UnknownCRSException(crsCodeType.getOriginal());
		}
		LOG.debug("Successfully created the crs with id: " + crsCodeType);
		return realCRS;
	}

	/**
	 * @param storeId identifier of the {@link CRSStore} instance, may be
	 * <code>null</code>, then the first transformation factory will be returned
	 * @return the {@link TransformationFactory} instance assigned to the store with the
	 * given id or the first one in the list of {@link TransformationFactory}s if no such
	 * instance has been created or <code>null</code> if no {@link TransformationFactory}
	 * instance could be found.
	 */
	public static final TransformationFactory getTransformationFactory(String storeId) {
		if (storeId == null) {
			for (TransformationFactory tf : idToTransF.values()) {
				return tf;
			}
		}
		return idToTransF.get(storeId);
	}

	/**
	 * Get a {@link Transformation} with given id, or <code>null</code> if it does not
	 * exist.
	 * @param storeId identifier of the store, looking for the {@link Transformation}, may
	 * be <code>null</code> if in all {@link CRSStore}s should be searched
	 * @param id of the {@link Transformation}
	 * @return the identified {@link Transformation} or <code>null<code> if no such
	 * transformation is found.
	 */
	public synchronized static Transformation getTransformation(String storeId, String id) {
		CRSStore crsStore = idToCRSStore.get(storeId);
		if (crsStore == null) {
			for (String sId : storeIds) {
				CRSStore store = idToCRSStore.get(sId);
				Transformation transformation = getTransformation(store, id);
				if (transformation != null) {
					return transformation;
				}
			}
		}
		else {
			return getTransformation(crsStore, id);
		}
		return null;
	}

	/**
	 * Retrieve a {@link Transformation} (chain) which transforms coordinates from the
	 * given source into the given target crs. If no such {@link Transformation} could be
	 * found or the implementation does not support inverse lookup of transformations
	 * <code>null<code> will be returned.
	 *
	 *
	@param storeId
	 *            identifier of the store, looking for the {@link Transformation}, may be <code>null</code> if
	 * in all {@link CRSStore}s should be searched
	 * @param sourceCRS start {@link ICRS} of the transformation (chain)
	 * @param targetCRS end {@link ICRS} of the transformation (chain).
	 * @return the given {@link Transformation} or <code>null<code> if no such
	 * transformation was found.
	 * @throws TransformationException
	 * @throws IllegalArgumentException
	 */
	public synchronized static Transformation getTransformation(String storeId, ICRS sourceCRS, ICRS targetCRS)
			throws IllegalArgumentException, TransformationException {
		return getTransformation(storeId, sourceCRS, targetCRS, null);
	}

	/**
	 * Retrieve a {@link Transformation} (chain) which transforms coordinates from the
	 * given source into the given target crs. If no such {@link Transformation} could be
	 * found or the implementation does not support inverse lookup of transformations
	 * <code>null<code> will be returned.
	 *
	 *
	@param storeId
	 *            identifier of the store, looking for the {@link Transformation}, may be <code>null</code> if
	 * in all {@link CRSStore}s should be searched
	 * @param sourceCRS start {@link ICRS} of the transformation (chain)
	 * @param targetCRS end {@link ICRS} of the transformation (chain).
	 * @param transformationsToBeUsed a list of transformations which must be used on the
	 * resulting transformation chain, may be <code>null</code> or empty
	 * @return the given {@link Transformation} or <code>null<code> if no such
	 * transformation was found.
	 * @throws TransformationException
	 * @throws IllegalArgumentException
	 */
	public synchronized static Transformation getTransformation(String storeId, ICRS sourceCRS, ICRS targetCRS,
			List<Transformation> transformationsToBeUsed) throws IllegalArgumentException, TransformationException {
		if (storeId != null) {
			TransformationFactory fac = getTransformationFactory(storeId);
			return fac.createFromCoordinateSystems(sourceCRS, targetCRS, transformationsToBeUsed);
		}
		else {
			for (TransformationFactory tf : idToTransF.values()) {
				Transformation trans = tf.createFromCoordinateSystems(sourceCRS, targetCRS, transformationsToBeUsed);
				if (trans != null) {
					return trans;
				}
			}
		}
		return null;
	}

	/**
	 * Get a {@link Transformation} with given id, or <code>null</code> if it does not
	 * exist.
	 * @param crsStore {@link CRSStore} instance, looking for the {@link Transformation},
	 * may not be <code>null</code>
	 * @param id of the {@link Transformation}
	 * @return the identified {@link Transformation} or <code>null<code> if no
	 * transformation is found.
	 * @throws IllegalArgumentException if crsStore is null
	 */
	private synchronized static Transformation getTransformation(CRSStore crsStore, String id) {
		if (crsStore == null) {
			throw new IllegalArgumentException(Messages.get("CRSManager.STORE_NULL"));
		}
		CRSResource t = null;
		try {
			t = crsStore.getDirectTransformation(id);
		}
		catch (Throwable e) {
			LOG.debug("Could not retrieve a transformation for id: " + id);
		}
		if (t != null) {
			return (Transformation) t;
		}
		LOG.debug("The given id: " + id + " is not of type transformation return null.");
		return null;
	}

	protected void remove(String id) {
		if (id != null) {
			storeIds.remove(id);
			idToCRSStore.remove(id);
			idToTransF.remove(id);
		}
	}

}
