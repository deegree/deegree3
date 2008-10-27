//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/rendering/Java2DRenderer.java $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/
package org.deegree.model.geometry.standard.primitive;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.Envelope;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.standard.AbstractDefaultGeometry;

/**
 * Default implementation of {@link Envelope}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class DefaultEnvelope extends AbstractDefaultGeometry implements Envelope {

    private Point max;
    
    private Point min;

    /**
     * Creates a new <code>DefaultEnvelope</code> instance from the given parameters.
     * 
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param min 
     * @param max 
     */    
    public DefaultEnvelope( String id, CoordinateSystem crs, Point min, Point max ) {
        super (id, crs);
        this.min = min;
        this.max = max;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.ENVELOPE;
    }

    @Override
    public double getHeight() {
        return max.getY() - min.getY();
    }

    @Override
    public Point getMax() {
        return max;
    }

    @Override
    public Point getMin() {
        return min;
    }

    @Override
    public double getWidth() {
        return max.getX() - min.getX();
    }

    @Override
    public Geometry intersection( Geometry geometry ) {
        Geometry result = null;
        switch (geometry.getGeometryType()) {
        case ENVELOPE: {
            Envelope other = (Envelope) geometry;
            throw new UnsupportedOperationException();
        }
        default : {
            throw new UnsupportedOperationException();
        }
        }
//        return result;
    }

    @Override
    public boolean intersects( Geometry geometry ) {
        throw new UnsupportedOperationException();
    }    
    
    @Override
    public Envelope merge( Envelope other ) {
        int coordinateDimension = max.getCoordinateDimension();
        double[] min = new double[coordinateDimension];
        double[] max = new double[coordinateDimension];
        for ( int i = 0; i < coordinateDimension; i++ ) {
            if ( this.min.getAsArray()[i] < other.getMin().getAsArray()[i] ) {
                min[i] = this.min.getAsArray()[i];
            } else {
                min[i] = other.getMin().getAsArray()[i];
            }
            if ( this.max.getAsArray()[i] > other.getMax().getAsArray()[i] ) {
                max[i] = this.max.getAsArray()[i];
            } else {
                max[i] = other.getMax().getAsArray()[i];
            }
        }
        Point newMin = new DefaultPoint( null, getCoordinateSystem(), min );
        Point newMax = new DefaultPoint( null, getCoordinateSystem(), min );
        return new DefaultEnvelope(null, getCoordinateSystem(), newMin, newMax);
    }
}
