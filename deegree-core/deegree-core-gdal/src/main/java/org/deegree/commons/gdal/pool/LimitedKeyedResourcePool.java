/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2014 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.commons.gdal.pool;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;

/**
 * A "keyed" resource pool implementation that pools {@link KeyedResource} object
 * instances and guarantees a maximum limit fpr open resources.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
public class LimitedKeyedResourcePool<T extends KeyedResource> implements Closeable {

	private static final Logger LOG = getLogger(LimitedKeyedResourcePool.class);

	private final KeyedResourceFactory<T> factory;

	private final int maxResources;

	private final Map<String, BlockingQueue<T>> keyToIdleQueue = new HashMap<String, BlockingQueue<T>>();

	private final LruKeyTracker keyTracker;

	private final Lock needResource = new ReentrantLock();

	/**
	 * Creates a new {@link LimitedKeyedResourcePool} instance.
	 * @param factory factory for creating new KeyedResource instances, must not be
	 * <code>null</code>
	 * @param maxResources maximum number of open KeyedResource instances
	 */
	public LimitedKeyedResourcePool(final KeyedResourceFactory<T> factory, final int maxResources) {
		this.factory = factory;
		this.maxResources = maxResources;
		keyTracker = new LruKeyTracker(maxResources);
	}

	public T borrow(final String key) throws InterruptedException, IOException {

		LOG.debug("Borrowing resource, key: " + key + ". Total resource count: " + keyTracker);
		needResource.lock();
		T resource = checkForIdleResource(key);
		if (resource != null) {
			recycleResource(resource);
		}
		else if (keyTracker.isEmptySlotsAvailable()) {
			resource = addResource(key);
		}
		else {
			resource = takeLeastRecentlyUsedResource();
			if (resource.getKey().equals(key)) {
				recycleResource(resource);
			}
			else {
				resource = trashResourceAndAddNew(key, resource);
			}
		}
		LOG.debug("Borrowed resource, key: " + key + ". Total resource count: " + keyTracker);
		return resource;
	}

	private T checkForIdleResource(final String key) {
		BlockingQueue<T> queue = getQueue(key);
		return queue.poll();
	}

	private void recycleResource(final T resource) {
		LOG.debug("Got recycled resource, key: " + resource.getKey());
		keyTracker.renew(resource.getKey());
		needResource.unlock();
	}

	private T addResource(final String key) {
		LOG.debug("Got empty resource slot");
		keyTracker.add(key);
		needResource.unlock();
		LOG.debug("Creating resource, key: " + key);
		return factory.create(key);
	}

	private T trashResourceAndAddNew(final String key, final T resource) throws IOException {
		LOG.debug("Got resource to trash, key: " + resource.getKey());
		keyTracker.remove(resource.getKey());
		keyTracker.add(key);
		needResource.unlock();
		LOG.debug("Destroying resource, key: " + resource.getKey());
		resource.close();
		LOG.debug("Creating resource, key: " + key);
		return factory.create(key);
	}

	private T takeLeastRecentlyUsedResource() throws InterruptedException {
		BlockingQueue<T> queue = getQueue(keyTracker.getLeastRecentlyUsedInstance());
		return queue.take();
	}

	public void returnObject(final T resource) {
		LOG.debug("Returning resource, key: " + resource.getKey() + ". Total resource count: " + keyTracker);
		final String key = resource.getKey();
		final BlockingQueue<T> queue = getQueue(key);
		queue.add(resource);
	}

	@Override
	public void close() {

	}

	private synchronized BlockingQueue<T> getQueue(final String key) {
		BlockingQueue<T> queue = keyToIdleQueue.get(key);
		if (queue == null) {
			queue = new LinkedBlockingQueue<T>(maxResources);
			keyToIdleQueue.put(key, queue);
		}
		return queue;
	}

}
