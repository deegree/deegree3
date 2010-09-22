//$HeadURL$
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
package org.deegree.filter.spatial;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.uom.Measure;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.PropertyName;
import org.deegree.geometry.Geometry;

/**
 * {@link SpatialOperator} that evaluates to true, iff geometries are beyond the specified distance of each other.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class Beyond extends SpatialOperator {

    private final PropertyName propName;

    private final Geometry geometry;

    private final Measure distance;

    public Beyond( PropertyName propName, Geometry geometry, Measure distance ) {
        this.propName = propName;
        this.geometry = geometry;
        this.distance = distance;
    }

    /**
     * @return the propName
     */
    public PropertyName getPropName() {
        return propName;
    }

    /**
     * @return the geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * @return the distance
     */
    public Measure getDistance() {
        return distance;
    }

    @Override
    public <T> boolean evaluate( T obj, XPathEvaluator<T> xpathEvaluator )
                            throws FilterEvaluationException {
        for ( TypedObjectNode param1Value : propName.evaluate( obj, xpathEvaluator ) ) {
            Geometry geom = checkGeometryOrNull( param1Value );
            if ( geom != null ) {
                Geometry transformedLiteral = getCompatibleGeometry( geom, geometry );
                // TODO what about the units of the distance when transforming?
                return geom.isBeyond( transformedLiteral, distance );
            }
        }
        return false;
    }

    public String toString( String indent ) {
        String s = indent + "-Beyond\n";
        s += indent + propName + "\n";
        s += indent + geometry + "\n";
        s += indent + distance;
        return s;
    }

    @Override
    public Object[] getParams() {
        return new Object[] { propName, geometry };
    }
}
