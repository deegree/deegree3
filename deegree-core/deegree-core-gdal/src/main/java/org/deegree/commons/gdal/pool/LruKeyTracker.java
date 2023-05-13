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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

class LruKeyTracker {

	private final Map<String, Integer> lruMap = new LinkedHashMap<String, Integer>(16, 0.75f, true);

	private final int maxActive;

	private final AtomicInteger totalResources = new AtomicInteger(0);

	LruKeyTracker(final int maxActive) {
		this.maxActive = maxActive;
	}

	String getLeastRecentlyUsedInstance() {
		Set<Entry<String, Integer>> entries = lruMap.entrySet();
		return entries.iterator().next().getKey();
	}

	void add(final String key) {
		Integer num = lruMap.get(key);
		if (num == null) {
			num = new Integer(1);
		}
		else {
			num++;
		}
		lruMap.put(key, num);
		totalResources.incrementAndGet();
		renew(key);
	}

	void remove(final String key) {
		int num = lruMap.get(key) - 1;
		if (num == 0) {
			lruMap.remove(key);
		}
		else {
			lruMap.put(key, num);
		}
		totalResources.decrementAndGet();
		renew(key);
	}

	void renew(final String key) {
		lruMap.get(key);
	}

	boolean isEmptySlotsAvailable() {
		return totalResources.intValue() < maxActive;
	}

	@Override
	public String toString() {
		return "" + totalResources;
	}

}
