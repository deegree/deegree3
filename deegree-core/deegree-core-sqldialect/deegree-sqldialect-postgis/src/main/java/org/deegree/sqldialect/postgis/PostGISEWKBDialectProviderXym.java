package org.deegree.sqldialect.postgis;

import org.deegree.geometry.io.ewkb.Axis;
import org.deegree.sqldialect.SQLDialect;

public class PostGISEWKBDialectProviderXym extends PostGISEWKBDialectProvider {

	@Override
	protected SQLDialect create(String version) {
		return new PostGISEWKBDialect(version, Axis.XYM);
	}

}