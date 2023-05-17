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

package org.deegree.services.wfs;

import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.commons.ows.exception.OWSException.OPERATION_NOT_SUPPORTED;
import static org.deegree.commons.xml.CommonNamespaces.FES_20_NS;
import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.commons.xml.XMLAdapter.writeElement;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_100_TRANSACTION_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_110_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;
import static org.deegree.services.wfs.WebFeatureService.getXMLResponseWriter;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.CloseableIterator;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.lock.Lock;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.FeatureType;
import org.deegree.protocol.wfs.lockfeature.LockFeature;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.wfs.query.QueryAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link LockFeature} requests for the {@link WebFeatureService}.
 *
 * @see WebFeatureService
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
class LockFeatureHandler {

	private static final Logger LOG = LoggerFactory.getLogger(LockFeatureHandler.class);

	private static final long DEFAULT_EXPIRY_IN_MILLISECONDS = 5 * 60 * 1000;

	private final WebFeatureService master;

	/**
	 * Creates a new {@link LockFeatureHandler} instance that uses the given service to
	 * lookup requested {@link FeatureType}s.
	 * @param master
	 *
	 */
	LockFeatureHandler(WebFeatureService master) {
		this.master = master;
	}

	/**
	 * Performs the given {@link LockFeature} request.
	 * @param request request to be handled, must not be <code>null</code>
	 * @param response response that is used to write the result, must not be
	 * <code>null</code>
	 * @throws OWSException if a WFS specific exception occurs, e.g. a requested feature
	 * type is not served
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	void doLockFeature(LockFeature request, HttpResponseBuffer response)
			throws OWSException, XMLStreamException, IOException {

		LOG.debug("doLockFeature: " + request);

		LockManager manager = getLockManager();
		Lock lock = acquireOrRenewLock(request, manager);
		try {
			writeLockFeatureResponse(request, response, lock);
		}
		catch (FeatureStoreException e) {
			throw new OWSException("Cannot acquire lock: " + e.getMessage(), NO_APPLICABLE_CODE);
		}
	}

	private void writeLockFeatureResponse(LockFeature request, HttpResponseBuffer response, Lock lock)
			throws XMLStreamException, IOException, FeatureStoreException, OWSException {

		Version version = request.getVersion();
		if (VERSION_100.equals(version)) {
			writeLockFeatureResponse100(response, lock);
		}
		else if (VERSION_110.equals(version)) {
			writeLockFeatureResponse110(response, lock);
		}
		else if (VERSION_200.equals(version)) {
			writeLockFeatureResponse200(response, lock);
		}
		else {
			String msg = "LockFeature for WFS version: " + request.getVersion() + " is not implemented yet.";
			throw new OWSException(msg, OPERATION_NOT_SUPPORTED);
		}
	}

	private void writeLockFeatureResponse100(HttpResponseBuffer response, Lock lock)
			throws XMLStreamException, IOException, FeatureStoreException {

		String schemaLocation = WFS_NS + " " + WFS_100_TRANSACTION_URL;
		XMLStreamWriter writer = getXMLResponseWriter(response, "text/xml", schemaLocation);

		writer.setPrefix("wfs", WFS_NS);
		writer.writeStartElement(WFS_NS, "WFS_LockFeatureResponse");
		writer.writeNamespace("wfs", WFS_NS);
		writer.writeNamespace("ogc", OGCNS);
		writeElement(writer, WFS_NS, "LockId", lock.getId());

		if (lock.getNumLocked() > 0) {
			writeFeaturesLocked100or110(lock, writer);
		}
		if (lock.getNumFailedToLock() > 0) {
			writeFeaturesNotLocked100(lock, writer);
		}

		writer.writeEndElement();
		writer.flush();
	}

	private void writeLockFeatureResponse110(HttpResponseBuffer response, Lock lock)
			throws XMLStreamException, IOException, FeatureStoreException {

		String schemaLocation = WFS_NS + " " + WFS_110_SCHEMA_URL;
		XMLStreamWriter writer = getXMLResponseWriter(response, "text/xml", schemaLocation);
		writer.setPrefix("wfs", WFS_NS);
		writer.writeStartElement(WFS_NS, "LockFeatureResponse");
		writer.writeNamespace("wfs", WFS_NS);
		writer.writeNamespace("ogc", OGCNS);
		writeElement(writer, WFS_NS, "LockId", lock.getId());

		if (lock.getNumLocked() > 0) {
			writeFeaturesLocked100or110(lock, writer);
		}

		if (lock.getNumFailedToLock() > 0) {
			writeFeaturesNotLocked110(lock, writer);
		}

		writer.writeEndElement();
		writer.flush();
	}

	private void writeLockFeatureResponse200(HttpResponseBuffer response, Lock lock)
			throws XMLStreamException, IOException, FeatureStoreException {

		String schemaLocation = WFS_200_NS + " " + WFS_200_SCHEMA_URL;
		XMLStreamWriter writer = getXMLResponseWriter(response, "text/xml", schemaLocation);
		writer.setPrefix("wfs", WFS_200_NS);
		writer.writeStartElement(WFS_200_NS, "LockFeatureResponse");
		writer.writeAttribute("lockId", lock.getId());
		writer.writeNamespace("wfs", WFS_200_NS);
		writer.writeNamespace("fes", FES_20_NS);

		if (lock.getNumLocked() > 0) {
			writeFeaturesLocked200(lock, writer);
		}

		if (lock.getNumFailedToLock() > 0) {
			writeFeaturesNotLocked200(lock, writer);
		}

		writer.writeEndElement();
		writer.flush();
	}

	private void writeFeaturesLocked100or110(Lock lock, XMLStreamWriter writer)
			throws XMLStreamException, FeatureStoreException {
		writer.writeStartElement(WFS_NS, "FeaturesLocked");
		CloseableIterator<String> fidIter = lock.getLockedFeatures();
		try {
			while (fidIter.hasNext()) {
				String fid = fidIter.next();
				writer.writeEmptyElement(OGCNS, "FeatureId");
				writer.writeAttribute("fid", fid);
			}
		}
		finally {
			fidIter.close();
		}
		writer.writeEndElement();
	}

	private void writeFeaturesLocked200(Lock lock, XMLStreamWriter writer)
			throws XMLStreamException, FeatureStoreException {
		writer.writeStartElement(WFS_200_NS, "FeaturesLocked");
		CloseableIterator<String> fidIter = lock.getLockedFeatures();
		try {
			while (fidIter.hasNext()) {
				String fid = fidIter.next();
				writer.writeEmptyElement(FES_20_NS, "ResourceId");
				writer.writeAttribute("rid", fid);
			}
		}
		finally {
			fidIter.close();
		}
		writer.writeEndElement();
	}

	private void writeFeaturesNotLocked100(Lock lock, XMLStreamWriter writer)
			throws XMLStreamException, FeatureStoreException {
		writer.writeStartElement(WFS_NS, "FeaturesNotLocked");
		CloseableIterator<String> fidIter = lock.getFailedToLockFeatures();
		try {
			while (fidIter.hasNext()) {
				String fid = fidIter.next();
				writer.writeEmptyElement(OGCNS, "FeatureId");
				writer.writeAttribute("fid", fid);
			}
		}
		finally {
			fidIter.close();
		}
		writer.writeEndElement();
	}

	private void writeFeaturesNotLocked110(Lock lock, XMLStreamWriter writer)
			throws XMLStreamException, FeatureStoreException {
		writer.writeStartElement(WFS_NS, "FeaturesNotLocked");
		CloseableIterator<String> fidIter = lock.getFailedToLockFeatures();
		try {
			while (fidIter.hasNext()) {
				String fid = fidIter.next();
				writer.writeStartElement("ogc", "FeatureId", OGCNS);
				writer.writeCharacters(fid);
				writer.writeEndElement();
			}
		}
		finally {
			fidIter.close();
		}
		writer.writeEndElement();
	}

	private void writeFeaturesNotLocked200(Lock lock, XMLStreamWriter writer)
			throws XMLStreamException, FeatureStoreException {
		writer.writeStartElement(WFS_200_NS, "FeaturesNotLocked");
		CloseableIterator<String> fidIter = lock.getFailedToLockFeatures();
		try {
			while (fidIter.hasNext()) {
				String fid = fidIter.next();
				writer.writeEmptyElement(FES_20_NS, "ResourceId");
				writer.writeAttribute("rid", fid);
			}
		}
		finally {
			fidIter.close();
		}
		writer.writeEndElement();
	}

	private Lock acquireOrRenewLock(LockFeature request, LockManager manager) throws OWSException {
		long expiryInMilliseconds = DEFAULT_EXPIRY_IN_MILLISECONDS;
		if (request.getExpiryInSeconds() != null) {
			expiryInMilliseconds = request.getExpiryInSeconds().longValue() * 1000;
		}

		String existingLockId = request.getExistingLockId();
		if (existingLockId != null) {
			return renewLock(manager, expiryInMilliseconds, existingLockId);
		}
		return acquireLock(request, manager, expiryInMilliseconds);

	}

	private Lock renewLock(LockManager manager, long expiryInMilliseconds, String existingLockId) throws OWSException {
		Lock lock = null;
		try {
			lock = manager.getLock(existingLockId);
			long acquistionDate = lock.getAcquistionDate();
			long expiryDate = acquistionDate + expiryInMilliseconds;
			lock.setExpiryDate(expiryDate);
		}
		catch (FeatureStoreException e) {
			LOG.debug(e.getMessage(), e);
			throw new OWSException("Cannot renew lock: " + e.getMessage(), NO_APPLICABLE_CODE);
		}
		return lock;
	}

	private Lock acquireLock(LockFeature request, LockManager manager, long expiryInMilliseconds) throws OWSException {

		// default: lock all
		boolean lockAll = true;
		if (request.getLockAll() != null) {
			lockAll = request.getLockAll();
		}

		List<Query> fsQueries = null;
		try {
			QueryAnalyzer queryAnalyzer = new QueryAnalyzer(request.getQueries(), master, master.getStoreManager(),
					master.getCheckAreaOfUse());
			fsQueries = queryAnalyzer.getQueries().get(master.getStoreManager().getStores()[0]);
		}
		catch (Exception e) {
			throw new OWSException("Cannot determine feature store queries for locking: " + e.getMessage(),
					NO_APPLICABLE_CODE);
		}

		Lock lock = null;
		try {
			lock = manager.acquireLock(fsQueries, lockAll, expiryInMilliseconds);
		}
		catch (OWSException e) {
			LOG.debug(e.getMessage(), e);
			throw new OWSException(e.getMessage(), "CannotLockAllFeatures");
		}
		catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			throw new OWSException("Cannot acquire lock: " + e.getMessage(), NO_APPLICABLE_CODE);
		}

		return lock;
	}

	private LockManager getLockManager() throws OWSException {
		LockManager manager = null;
		try {
			// TODO strategy for multiple LockManagers / feature stores
			manager = master.getStoreManager().getStores()[0].getLockManager();
		}
		catch (FeatureStoreException e) {
			LOG.debug(e.getMessage(), e);
			throw new OWSException("Cannot acquire lock manager: " + e.getMessage(), NO_APPLICABLE_CODE);
		}
		return manager;
	}

}
