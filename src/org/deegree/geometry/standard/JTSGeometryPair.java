package org.deegree.geometry.standard;

import org.deegree.commons.utils.Pair;

import com.vividsolutions.jts.geom.Geometry;

public class JTSGeometryPair extends Pair<Geometry, Geometry> {

    public JTSGeometryPair( Geometry first, Geometry second ) {
        super( first, second );
    }

    public static JTSGeometryPair createCompatiblePair( AbstractDefaultGeometry geom1,
                                                        org.deegree.geometry.Geometry geom2 ) {
        return new JTSGeometryPair( geom1.getJTSGeometry(), ( (AbstractDefaultGeometry) geom2 ).getJTSGeometry() );
    }

}
