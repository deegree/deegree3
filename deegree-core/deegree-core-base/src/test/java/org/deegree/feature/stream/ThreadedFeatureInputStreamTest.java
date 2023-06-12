/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2014 by:

 IDgis bv

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

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import org.deegree.feature.Feature;
import org.deegree.feature.stream.ThreadedFeatureInputStream.Consumer;
import org.deegree.feature.stream.ThreadedFeatureInputStream.ConsumerMessage;
import org.deegree.feature.stream.ThreadedFeatureInputStream.ConsumerClosingMessage;
import org.deegree.feature.stream.ThreadedFeatureInputStream.ProducerMessage;
import org.deegree.feature.stream.ThreadedFeatureInputStream.ProducerExceptionMessage;
import org.deegree.feature.stream.ThreadedFeatureInputStream.ProducerFeatureMessage;
import org.deegree.feature.stream.ThreadedFeatureInputStream.ProducerFinishedMessage;
import org.deegree.feature.stream.ThreadedFeatureInputStream.Producer;
import org.junit.Test;
import org.mockito.InOrder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ThreadedFeatureInputStreamTest {

	@Test
	@SuppressWarnings("unchecked")
	public void testProducer() throws Exception {
		Feature[] features = new Feature[] { mock(Feature.class), mock(Feature.class) };
		Iterator<Feature> featureIterator = Arrays.asList(features).iterator();

		FeatureInputStream featureInputStream = mock(FeatureInputStream.class);
		when(featureInputStream.iterator()).thenReturn(featureIterator);

		BlockingQueue<ProducerMessage> producerQueue = mock(BlockingQueue.class);
		BlockingQueue<ConsumerMessage> consumerQueue = mock(BlockingQueue.class);

		Producer producer = new Producer(featureInputStream, producerQueue, consumerQueue);
		producer.run();

		verify(consumerQueue, atLeastOnce()).poll();

		InOrder producerQueueOrder = inOrder(producerQueue);
		producerQueueOrder.verify(producerQueue).put(new ProducerFeatureMessage(features[0]));
		producerQueueOrder.verify(producerQueue).put(new ProducerFeatureMessage(features[1]));
		producerQueueOrder.verify(producerQueue).put(new ProducerFinishedMessage());

		verify(featureInputStream).close();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testProducerException() throws Exception {

		Feature feature = mock(Feature.class);
		Throwable exception = new RuntimeException();

		Iterator<Feature> featureIterator = mock(Iterator.class);
		when(featureIterator.hasNext()).thenReturn(true);
		when(featureIterator.next()).thenReturn(feature).thenThrow(exception);

		FeatureInputStream featureInputStream = mock(FeatureInputStream.class);
		when(featureInputStream.iterator()).thenReturn(featureIterator);

		BlockingQueue<ProducerMessage> producerQueue = mock(BlockingQueue.class);
		BlockingQueue<ConsumerMessage> consumerQueue = mock(BlockingQueue.class);

		Producer producer = new Producer(featureInputStream, producerQueue, consumerQueue);
		producer.run();

		InOrder producerQueueOrder = inOrder(producerQueue);
		producerQueueOrder.verify(producerQueue).put(new ProducerFeatureMessage(feature));
		producerQueueOrder.verify(producerQueue).put(new ProducerExceptionMessage(exception));
		producerQueueOrder.verify(producerQueue).put(new ProducerFinishedMessage());

		verify(featureIterator, times(2)).next();
		verify(featureInputStream).close();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testProducerAbort() throws Exception {
		Iterator<Feature> featureIterator = mock(Iterator.class);
		when(featureIterator.hasNext()).thenReturn(true);
		when(featureIterator.next()).thenReturn(mock(Feature.class));

		FeatureInputStream featureInputStream = mock(FeatureInputStream.class);
		when(featureInputStream.iterator()).thenReturn(featureIterator);

		BlockingQueue<ProducerMessage> producerQueue = mock(BlockingQueue.class);
		BlockingQueue<ConsumerMessage> consumerQueue = mock(BlockingQueue.class);
		when(consumerQueue.poll()).thenReturn(null, new ConsumerClosingMessage());

		Producer producer = new Producer(featureInputStream, producerQueue, consumerQueue);
		producer.run();

		verify(producerQueue).put(new ProducerFinishedMessage());
		verify(featureInputStream).close();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testConsumer() throws Exception {

		BlockingQueue<ProducerMessage> producerQueue = mock(BlockingQueue.class);
		BlockingQueue<ConsumerMessage> consumerQueue = mock(BlockingQueue.class);

		Feature[] features = new Feature[] { mock(Feature.class), mock(Feature.class) };
		when(producerQueue.take()).thenReturn(new ProducerFeatureMessage(features[0]),
				new ProducerFeatureMessage(features[1]), new ProducerFinishedMessage());

		Consumer consumer = new Consumer(producerQueue, consumerQueue);
		assertTrue(consumer.hasNext());
		assertEquals(features[0], consumer.next());
		assertTrue(consumer.hasNext());
		assertEquals(features[1], consumer.next());
		assertFalse(consumer.hasNext());

		consumer.close();

		verify(consumerQueue, never()).put(new ConsumerClosingMessage());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testConsumerAbort() throws Exception {
		BlockingQueue<ProducerMessage> producerQueue = mock(BlockingQueue.class);
		BlockingQueue<ConsumerMessage> consumerQueue = mock(BlockingQueue.class);

		Feature[] features = new Feature[] { mock(Feature.class), mock(Feature.class) };
		when(producerQueue.take()).thenReturn(new ProducerFeatureMessage(features[0]),
				new ProducerFeatureMessage(features[1]), new ProducerFinishedMessage());

		Consumer consumer = new Consumer(producerQueue, consumerQueue);
		assertTrue(consumer.hasNext());
		assertEquals(features[0], consumer.next());
		assertTrue(consumer.hasNext());

		consumer.close();
		verify(consumerQueue).put(new ConsumerClosingMessage());
		verify(producerQueue, times(3)).take();
		assertFalse(consumer.hasNext());
	}

	@Test(expected = RuntimeException.class)
	@SuppressWarnings("unchecked")
	public void testConsumerException() throws Exception {

		BlockingQueue<ProducerMessage> producerQueue = mock(BlockingQueue.class);
		BlockingQueue<ConsumerMessage> consumerQueue = mock(BlockingQueue.class);

		Feature feature = mock(Feature.class);
		when(producerQueue.take()).thenReturn(new ProducerFeatureMessage(feature),
				new ProducerExceptionMessage(new RuntimeException()), new ProducerFinishedMessage());

		Consumer consumer = new Consumer(producerQueue, consumerQueue);
		assertTrue(consumer.hasNext());
		assertEquals(feature, consumer.next());
		assertTrue(consumer.hasNext());
		consumer.next();
	}

}
