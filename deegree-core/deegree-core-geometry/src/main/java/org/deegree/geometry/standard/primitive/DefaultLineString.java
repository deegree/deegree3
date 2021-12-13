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
package org.deegree.geometry.standard.primitive;

import java.util.Collections;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.geometry.standard.curvesegments.DefaultLineStringSegment;

/**
 * Default implementation of {@link LineString}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultLineString extends DefaultCurve implements LineString {

    private LineStringSegment singleSegment;

    /**
     * 
     * @param id
     * @param crs
     * @param pm
     * @param controlPoints
     */
    public DefaultLineString( String id, ICRS crs, PrecisionModel pm, Points controlPoints ) {
        super( id, crs, pm, Collections.singletonList( (CurveSegment) new DefaultLineStringSegment( controlPoints ) ) );
        singleSegment = (LineStringSegment) getCurveSegments().get( 0 );
    }

    @Override
    public CurveType getCurveType() {
        return CurveType.LineString;
    }

    @Override
    public Points getControlPoints() {
        return singleSegment.getControlPoints();
    }

    @Override
    public int getCoordinateDimension() {
        return singleSegment.getCoordinateDimension();
    }

    @Override
    public LineString getAsLineString() {
        return this;
    }
    
    @Override
    public boolean isSFSCompliant() {
        return true;
    }

    @Override
    protected org.locationtech.jts.geom.LineString buildJTSGeometry() {
        return jtsFactory.createLineString( singleSegment.getControlPoints() );
    }
}
