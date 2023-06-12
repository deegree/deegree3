/*-
 * #%L
 * deegree-cli-utility
 * %%
 * Copyright (C) 2022 grit graphische Informationstechnik Beratungsgesellschaft mbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.deegree.tools.featurestoresql.loader;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import javax.xml.namespace.QName;

import org.deegree.feature.Feature;
import org.springframework.batch.core.ItemWriteListener;

/**
 * Collects statistics for each type loaded
 *
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public class FeatureStatistics {

	private Map<QName, AtomicLong> counter = new TreeMap<>(
			Comparator.comparing(QName::getNamespaceURI).thenComparing(QName::getLocalPart));

	public void reset() {
		counter.clear();
	}

	/**
	 * @param name type name to increment
	 */
	public long increment(QName name) {
		AtomicLong cnt = counter.get(name);
		if (cnt == null) {
			cnt = new AtomicLong();
			counter.put(name, cnt);
		}
		return cnt.incrementAndGet();
	}

	/**
	 * @param out Consumer function that receives the output
	 */
	public void summary(Consumer<String> out) {
		final long max = counter.values()
			.stream() //
			.map(AtomicLong::get) //
			.max(Long::compareTo)//
			.orElse(1L);
		final int len = String.valueOf(max).length();
		final String format = "%" + len + "d %s";
		counter.forEach((k, v) -> out.accept(String.format(format, v.get(), k.toString())));
	}

}
