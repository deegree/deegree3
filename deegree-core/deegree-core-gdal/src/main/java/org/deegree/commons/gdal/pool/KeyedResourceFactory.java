package org.deegree.commons.gdal.pool;

public interface KeyedResourceFactory<T> {

	T create(final String key);

}
