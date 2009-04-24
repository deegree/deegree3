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

import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;

/**
 * Abstract base class for the default {@link Geometry} implementation.
 * <p>
 * This implementation is built around JTS geometries which are used to evaluate topological predicates (e.g.
 * intersects) and spatial functions (e.g union). Simple geometries (e.g. {@link LineString}s are represented directly
 * by a corresponding JTS object, for complex ones (e.g. {@link Curve}s with non-linear segments), the JTS geometry only
 * approximates the original geometry. See <a
 * href="https://wiki.deegree.org/deegreeWiki/deegree3/MappingComplexGeometries">this page</a> for a discussion.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class AbstractDefaultGeometry implements Geometry {

    /**
     * May be used to built JTS geometries.
     */
    protected final static com.vividsolutions.jts.geom.GeometryFactory jtsFactory = new com.vividsolutions.jts.geom.GeometryFactory();    

    // contains an equivalent (or best-fit) JTS geometry object
    protected com.vividsolutions.jts.geom.Geometry jtsGeometry;
        
    protected String id;

    protected CRS crs;
    
    /**
     * @param id
     * @param crs
     */
    public AbstractDefaultGeometry( String id, CRS crs ) {
        this.id = id;
        this.crs = crs;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public CRS getCoordinateSystem() {
        return crs;
    }

    @Override
    public double getPrecision() {
        // TODO return real precision
        return 0.0000001;
    }

    @Override
    public boolean intersects( Geometry geometry ) {
        return getJTSGeometry().intersects( geometry.getJTSGeometry() );
    }

    @Override
    public boolean isWithin( Geometry geometry ) {
        return getJTSGeometry().within( geometry.getJTSGeometry() );
    }

    @Override
    public boolean isWithinDistance( Geometry geometry, double distance ) {
        return getJTSGeometry().isWithinDistance( geometry.getJTSGeometry(), distance );
    }

    @Override
    public boolean isBeyond( Geometry geometry, double distance ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains( Geometry geometry ) {
        return getJTSGeometry().contains( geometry.getJTSGeometry() );
    }

    @Override
    public boolean equals( Geometry geometry ) {
        return getJTSGeometry().equals( geometry.getJTSGeometry() );
    }

    @Override
    public double distance( Geometry geometry ) {
        return getJTSGeometry().distance( geometry.getJTSGeometry() );
    }

    @Override
    public Geometry intersection( Geometry geometry ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Geometry union( Geometry geometry ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Geometry difference( Geometry geometry ) {
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
    public Envelope getEnvelope() {
        throw new UnsupportedOperationException();
    }

    @Override
    public com.vividsolutions.jts.geom.Geometry getJTSGeometry() {
        if ( jtsGeometry == null ) {
            jtsGeometry = buildJTSGeometry();
        }
        return jtsGeometry;
    }

    protected com.vividsolutions.jts.geom.Geometry buildJTSGeometry() {
        throw new UnsupportedOperationException();
    }
}
