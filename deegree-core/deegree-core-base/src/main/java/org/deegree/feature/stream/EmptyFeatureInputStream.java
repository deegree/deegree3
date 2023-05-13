package org.deegree.feature.stream;

import java.util.Collections;
import java.util.Iterator;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;

/**
 * Creates an empty {@link FeatureInputStream}.
 *
 * @author <a href="mailto:stenger@lat-lon.de">Dirk Stenger</a>
 * @version 3.4
 * @since 3.4
 */
public class EmptyFeatureInputStream implements FeatureInputStream {

	@Override
	public void close() {
	}

	@Override
	public FeatureCollection toCollection() {
		return new GenericFeatureCollection();
	}

	@Override
	public int count() {
		return 0;
	}

	@Override
	public Iterator<Feature> iterator() {
		// TODO Use proper Java 1.7 API if JDK 6 is dropped
		// return Collections.emptyIterator();
		return Collections.<Feature>emptyList().iterator();
	}

}