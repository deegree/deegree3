/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.feature.stream;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Features;

/**
 * {@link FeatureInputStream} that encapsulates a sequence of {@link FeatureInputStream}s.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class CombinedFeatureInputStream implements FeatureInputStream {

	Iterator<FeatureInputStream> resultSetIter;

	FeatureInputStream currentResultSet;

	boolean lastClosed = false;

	/**
	 * Creates a new {@link CombinedFeatureInputStream} that is backed by the given
	 * {@link FeatureInputStream}.
	 * @param resultSetIter FeatureResultSet to back the result set, must not be
	 * <code>null</code>
	 */
	public CombinedFeatureInputStream(Iterator<FeatureInputStream> resultSetIter) {
		this.resultSetIter = resultSetIter;
	}

	@Override
	public void close() {
		if (currentResultSet != null) {
			currentResultSet.close();
		}
		while (resultSetIter.hasNext()) {
			resultSetIter.next().close();
		}
	}

	@Override
	public FeatureCollection toCollection() {
		return Features.toCollection(this);
	}

	@Override
	public Iterator<Feature> iterator() {
		return new Iterator<Feature>() {

			Iterator<Feature> featureIter;

			boolean nextRead = true;

			Feature next = null;

			@Override
			public boolean hasNext() {
				if (!nextRead) {
					return next != null;
				}
				if (featureIter == null) {
					if (!resultSetIter.hasNext()) {
						return false;
					}
					// only happens for the first call of #hasNext()
					currentResultSet = resultSetIter.next();
					featureIter = currentResultSet.iterator();
				}

				while (!featureIter.hasNext()) {
					if (resultSetIter.hasNext()) {
						currentResultSet.close();
						currentResultSet = resultSetIter.next();
						featureIter = currentResultSet.iterator();
					}
					else {
						// lastClosed = true;
						break;
					}
				}

				if (featureIter.hasNext()) {
					next = featureIter.next();
					nextRead = false;
				}
				else {
					next = null;
				}

				return next != null;
			}

			@Override
			public Feature next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				nextRead = true;
				return next;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
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

}
