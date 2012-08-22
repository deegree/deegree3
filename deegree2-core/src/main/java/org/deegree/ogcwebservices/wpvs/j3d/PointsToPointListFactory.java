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

package org.deegree.ogcwebservices.wpvs.j3d;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.spatialschema.Point;

/**
 * ...
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class PointsToPointListFactory/* implements PointListFactory*/ {


    /**
     * Builds a point list from the <code>Point</code>s in the
     * FeatureCollection fc.
     *
     * @param fc a feature collection containing <code>Point</code>s. This collection cannot be null
     * and must contain a geometry property of <code>Point</code> type. No check for this is done.
     * @return a List with <code>Point</code>s
     */
	public List<Point3d> createFromFeatureCollection( FeatureCollection fc  ) {
		if ( fc == null ) {
			 throw new NullPointerException("FeatureColection cannot be null.");
        }

        List<Point3d> ptsList = new ArrayList<Point3d>( fc.size() +1 );
        for ( int i = 0; i < fc.size(); i++ ) {
            Point point = (Point)fc.getFeature(i).getDefaultGeometryPropertyValue();
            if( point != null ){
                ptsList.add( new Point3d( point.getX(), point.getY(), point.getZ() ) );
            }
        }

        return ptsList;
	}

}
