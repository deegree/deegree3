/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2025 by:
 - Department of Geography, University of Bonn -
 and
 - grit GmbH -
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
package org.deegree.logging.autoconfiguration;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class StreamUtils {

	private StreamUtils() {

	}

	public static <K, V> Predicate<Map.Entry<K, V>> filterKey(Predicate<K> keyPredicate) {
		return e -> keyPredicate.test(e.getKey());
	}

	public static <K, V> Predicate<Map.Entry<K, V>> filterValue(Predicate<V> valuePredicate) {
		return e -> valuePredicate.test(e.getValue());
	}

	public static <K, V> Predicate<Map.Entry<K, V>> filterKeyValue(Predicate<K> keyPredicate,
			Predicate<V> valuePredicate) {
		return e -> keyPredicate.test(e.getKey()) && valuePredicate.test(e.getValue());
	}

	public static <A, B, AA> Function<Map.Entry<A, B>, Map.Entry<AA, B>> mapKey(Function<A, AA> keyMapper) {
		return e -> new SimpleImmutableEntry<>(keyMapper.apply(e.getKey()), e.getValue());
	}

	public static <A, B, BB> Function<Map.Entry<A, B>, Map.Entry<A, BB>> mapValue(Function<B, BB> valueMapper) {
		return e -> new SimpleImmutableEntry<>(e.getKey(), valueMapper.apply(e.getValue()));
	}

	public static <A, B, AA, BB> Function<Map.Entry<A, B>, Map.Entry<AA, BB>> mapKeyValue(Function<A, AA> keyMapper,
			Function<B, BB> valueMapper) {
		return e -> new SimpleImmutableEntry<>(keyMapper.apply(e.getKey()), valueMapper.apply(e.getValue()));
	}

}
