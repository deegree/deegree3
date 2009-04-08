//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
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
package org.deegree.geometry.standard;

import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;

public abstract class AbstractDefaultGeometry implements Geometry{

    protected String id;
    
    protected CoordinateSystem crs;

    public AbstractDefaultGeometry( String id, CoordinateSystem crs ) {
        this.id = id;
        this.crs = crs;
    }

    public String getId() {
        return id;
    }    
    
    public CoordinateSystem getCoordinateSystem() {
        return crs;
    }

    @Override
    public double getPrecision() {
    	// TODO return real precision
        return 0.0000001;
    }

    @Override
    public Geometry intersection( Geometry geometry ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean intersects( Geometry geometry ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBeyond( Geometry geometry, double distance ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWithin( Geometry geometry ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWithinDistance( Geometry geometry, double distance ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Geometry union( Geometry geometry ) {
       throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains( Geometry geometry ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Geometry difference( Geometry geometry ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double distance( Geometry geometry ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals( Geometry geometry ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Geometry getBuffer( double distance ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Geometry getConvexHull() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCoordinateDimension() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Envelope getEnvelope() {
        throw new UnsupportedOperationException();
    }    
}
