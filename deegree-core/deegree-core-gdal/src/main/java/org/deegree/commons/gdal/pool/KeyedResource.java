package org.deegree.commons.gdal.pool;

import java.io.Closeable;

public interface KeyedResource extends Closeable {

	String getKey();

}
