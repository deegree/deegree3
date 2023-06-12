/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
 - IDgis bv -
 and
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

 IDgis bv
 Boomkamp 16
 7461 AX Rijssen
 The Netherlands
 http://idgis.nl/

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
package org.deegree.feature.stream;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureInputStream} that uses a separate thread to keep an internal queue of
 * features filled.
 *
 * @author <a href="mailto:reijer.copier@idgis.nl">Reijer Copier</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 *
 */
public class ThreadedFeatureInputStream implements FeatureInputStream {

	private static Logger LOG = LoggerFactory.getLogger(ThreadedFeatureInputStream.class);

	// TODO where to manage this?
	private static ExecutorService service = Executors.newFixedThreadPool(10);

	private final Consumer iterator;

	/**
	 * Creates a new {@link ThreadedFeatureInputStreamTest} based on the given
	 * {@link FeatureInputStream}.
	 * @param featureInputStream
	 * @param queueSize
	 */
	public ThreadedFeatureInputStream(final FeatureInputStream featureInputStream, final int queueSize) {
		final BlockingQueue<ProducerMessage> producerQueue = new ArrayBlockingQueue<ProducerMessage>(queueSize, true);
		final BlockingQueue<ConsumerMessage> consumerQueue = new ArrayBlockingQueue<ConsumerMessage>(1, true);

		iterator = new Consumer(producerQueue, consumerQueue);

		service.execute(new Producer(featureInputStream, producerQueue, consumerQueue));
	}

	@Override
	public void close() {
		iterator.close();
	}

	@Override
	public FeatureCollection toCollection() {
		return Features.toCollection(this);
	}

	@Override
	public Iterator<Feature> iterator() {
		return iterator;
	}

	@Override
	public int count() {
		int i = 0;
		for (@SuppressWarnings("unused")
		Feature f : this) {
			i++;
		}
		close();
		return i;
	}

	protected static abstract class ConsumerMessage {

		boolean isClosing() {
			return false;
		}

	}

	protected static class ConsumerClosingMessage extends ConsumerMessage {

		@Override
		boolean isClosing() {
			return true;
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof ConsumerClosingMessage;
		}

	}

	protected static abstract class ProducerMessage {

		boolean isFinished() {
			return false;
		}

		boolean isFeature() {
			return false;
		}

		boolean isException() {
			return false;
		}

		Feature getFeature() {
			throw new IllegalStateException("Not a ProducerFeatureMessage");
		}

		Throwable getException() {
			throw new IllegalStateException("Not a ProducerExceptionMessage");
		}

	}

	protected static class ProducerFeatureMessage extends ProducerMessage {

		final Feature feature;

		ProducerFeatureMessage(final Feature feature) {
			this.feature = feature;
		}

		@Override
		boolean isFeature() {
			return true;
		}

		@Override
		Feature getFeature() {
			return feature;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ProducerFeatureMessage other = (ProducerFeatureMessage) obj;
			if (feature == null) {
				if (other.feature != null)
					return false;
			}
			else if (!feature.equals(other.feature))
				return false;
			return true;
		}

	}

	protected static class ProducerExceptionMessage extends ProducerMessage {

		final Throwable exception;

		ProducerExceptionMessage(final Throwable exception) {
			this.exception = exception;
		}

		@Override
		boolean isException() {
			return true;
		}

		@Override
		Throwable getException() {
			return exception;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ProducerExceptionMessage other = (ProducerExceptionMessage) obj;
			if (exception == null) {
				if (other.exception != null)
					return false;
			}
			else if (!exception.equals(other.exception))
				return false;
			return true;
		}

	}

	protected static class ProducerFinishedMessage extends ProducerMessage {

		@Override
		boolean isFinished() {
			return true;
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof ProducerFinishedMessage;
		}

	}

	protected static class Consumer implements Iterator<Feature> {

		ProducerMessage lastMessage;

		final BlockingQueue<ProducerMessage> producerQueue;

		final BlockingQueue<ConsumerMessage> consumerQueue;

		public Consumer(final BlockingQueue<ProducerMessage> producerQueue,
				final BlockingQueue<ConsumerMessage> consumerQueue) {

			this.producerQueue = producerQueue;
			this.consumerQueue = consumerQueue;
		}

		@Override
		public boolean hasNext() {
			if (lastMessage == null) {
				try {
					LOG.debug("Initial message consumed");

					lastMessage = producerQueue.take();
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

			return !lastMessage.isFinished();
		}

		@Override
		public Feature next() {
			try {
				if (lastMessage == null) {
					LOG.debug("Initial message consumed");

					lastMessage = producerQueue.take();
				}

				ProducerMessage currentMessage = lastMessage;
				lastMessage = producerQueue.take();

				if (currentMessage.isException()) {
					LOG.debug("Exception consumed");

					throw new RuntimeException(currentMessage.getException());
				}

				if (!currentMessage.isFeature()) {
					throw new IllegalStateException("FeatureProducerMessage expected");
				}

				LOG.debug("Feature consumed");

				return currentMessage.getFeature();
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		void close() {
			if (hasNext()) {

				try {
					LOG.debug("Requesting producer to finish");

					consumerQueue.put(new ConsumerClosingMessage());
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				while (hasNext()) {
					next();
				}

				LOG.debug("Producer finished");
			}
			else {
				LOG.debug("Producer already finished");
			}
		}

	}

	protected static class Producer implements Runnable {

		private final FeatureInputStream featureInputStream;

		private final BlockingQueue<ProducerMessage> producerQueue;

		private final BlockingQueue<ConsumerMessage> consumerQueue;

		protected Producer(FeatureInputStream featureInputStream, BlockingQueue<ProducerMessage> producerQueue,
				BlockingQueue<ConsumerMessage> consumerQueue) {
			this.featureInputStream = featureInputStream;
			this.producerQueue = producerQueue;
			this.consumerQueue = consumerQueue;
		}

		@Override
		public void run() {
			try {
				LOG.debug("Producer started");

				for (Feature f : featureInputStream) {
					ConsumerMessage consumerMessage = consumerQueue.poll();
					if (consumerMessage != null && consumerMessage.isClosing()) {
						LOG.debug("Producer halted");

						break;
					}

					LOG.debug("Feature produced");
					producerQueue.put(new ProducerFeatureMessage(f));
				}
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			catch (Throwable t) {
				try {
					LOG.debug("Exception produced");
					producerQueue.put(new ProducerExceptionMessage(t));
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			finally {
				featureInputStream.close();

				try {
					producerQueue.put(new ProducerFinishedMessage());
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				LOG.debug("Producer finished");
			}
		}

	}

	/**
	 *
	 */
	public static void shutdown() {
		service.shutdown();
	}

}
