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
package org.deegree.geometry.standard;

import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.standard.multi.DefaultMultiGeometry;
import org.deegree.geometry.standard.multi.DefaultMultiLineString;
import org.deegree.geometry.standard.multi.DefaultMultiPoint;
import org.deegree.geometry.standard.multi.DefaultMultiPolygon;
import org.deegree.geometry.standard.points.JTSPoints;
import org.deegree.geometry.standard.primitive.DefaultLineString;
import org.deegree.geometry.standard.primitive.DefaultLinearRing;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.geometry.standard.primitive.DefaultPolygon;
import org.deegree.gml.geometry.refs.GeometryReference;
import org.deegree.gml.props.GMLStdProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.CoordinateSequence;

/**
 * Abstract base class for the default {@link Geometry} implementation.
 * <p>
 * This implementation is built around <a href="http://tsusiatsoftware.net/jts/main.html">JTS (Java Topology Suite)</a>
 * geometries which are used to evaluate topological predicates (e.g. intersects) and perform spatial analysis
 * operations (e.g union). Simple geometries (e.g. {@link LineString}s are mapped to a corresponding JTS object, for
 * complex ones (e.g. {@link Curve}s with non-linear segments), the JTS geometry only approximates the original
 * geometry. See <a href="https://wiki.deegree.org/deegreeWiki/deegree3/MappingComplexGeometries">this page</a> for a
 * discussion.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractDefaultGeometry implements Geometry {

    private static final Logger LOG = LoggerFactory.getLogger( AbstractDefaultGeometry.class );

    /**
     * Used to built JTS geometries.
     */
    protected final static com.vividsolutions.jts.geom.GeometryFactory jtsFactory = new com.vividsolutions.jts.geom.GeometryFactory();

    /** Geometry identifier. */
    protected String id;

    /** Reference to a coordinate system. */
    protected CRS crs;

    protected PrecisionModel pm;

    // contains an equivalent (or best-fit) JTS geometry object
    protected com.vividsolutions.jts.geom.Geometry jtsGeometry;

    private GMLStdProps standardProps;

    /**
     * @param id
     * @param crs
     * @param pm
     */
    public AbstractDefaultGeometry( String id, CRS crs, PrecisionModel pm ) {
        this.id = id;
        this.crs = crs;
        this.pm = pm;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId( String id ) {
        this.id = id;
    }

    @Override
    public CRS getCoordinateSystem() {
        return crs;
    }

    @Override
    public void setCoordinateSystem( CRS crs ) {
        this.crs = crs;
    }

    @Override
    public PrecisionModel getPrecision() {
        return pm;
    }

    @Override
    public void setPrecision( PrecisionModel pm ) {
        this.pm = pm;
    }

    @Override
    public boolean intersects( Geometry geometry ) {
        JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair( this, geometry );
        return jtsGeoms.first.intersects( jtsGeoms.second );
    }

    @Override
    public boolean isDisjoint( Geometry geometry ) {
        JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair( this, geometry );
        return jtsGeoms.first.disjoint( jtsGeoms.second );
    }

    @Override
    public boolean overlaps( Geometry geometry ) {
        JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair( this, geometry );
        return jtsGeoms.first.overlaps( jtsGeoms.second );
    }

    @Override
    public boolean touches( Geometry geometry ) {
        JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair( this, geometry );
        return jtsGeoms.first.touches( jtsGeoms.second );
    }

    @Override
    public boolean isWithin( Geometry geometry ) {
        JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair( this, geometry );
        return jtsGeoms.first.within( jtsGeoms.second );
    }

    @Override
    public boolean isWithinDistance( Geometry geometry, Measure distance ) {
        LOG.warn( "TODO: Respect UOM in evaluation of topological predicate." );
        JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair( this, geometry );
        return jtsGeoms.first.isWithinDistance( jtsGeoms.second, distance.getValueAsDouble() );
    }

    @Override
    public boolean isBeyond( Geometry geometry, Measure distance ) {
        return !isWithinDistance( geometry, distance );
    }

    @Override
    public boolean contains( Geometry geometry ) {
        JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair( this, geometry );
        return jtsGeoms.first.contains( jtsGeoms.second );
    }

    @Override
    public boolean crosses( Geometry geometry ) {
        JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair( this, geometry );
        return jtsGeoms.first.crosses( jtsGeoms.second );
    }

    @Override
    public boolean equals( Geometry geometry ) {
        JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair( this, geometry );
        return jtsGeoms.first.equals( jtsGeoms.second );
    }

    @Override
    public Point getCentroid() {
        return (Point) createFromJTS( getJTSGeometry().getCentroid() );
    }

    @Override
    public Measure getDistance( Geometry geometry, Unit requestedUnit ) {
        // TODO respect unit
        double dist = getJTSGeometry().distance( getAsDefaultGeometry( geometry ).getJTSGeometry() );
        return new Measure( Double.toString( dist ), null );
    }

    @Override
    public Geometry getIntersection( Geometry geometry ) {
        JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair( this, geometry );
        com.vividsolutions.jts.geom.Geometry jtsGeom = jtsGeoms.first.intersection( jtsGeoms.second );
        return createFromJTS( jtsGeom );
    }

    @Override
    public Geometry getUnion( Geometry geometry ) {
        JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair( this, geometry );
        com.vividsolutions.jts.geom.Geometry jtsGeom = jtsGeoms.first.union( jtsGeoms.second );
        return createFromJTS( jtsGeom );
    }

    @Override
    public Geometry getDifference( Geometry geometry ) {
        JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair( this, geometry );
        com.vividsolutions.jts.geom.Geometry jtsGeom = jtsGeoms.first.difference( jtsGeoms.second );
        return createFromJTS( jtsGeom );
    }

    @Override
    public Geometry getBuffer( Measure distance ) {
        // TODO get double in CRS units
        double crsDistance = distance.getValueAsDouble();
        com.vividsolutions.jts.geom.Geometry jtsGeom = getJTSGeometry().buffer( crsDistance );
        return createFromJTS( jtsGeom );
    }

    @Override
    public Geometry getConvexHull() {
        com.vividsolutions.jts.geom.Geometry jtsGeom = getJTSGeometry().convexHull();
        return createFromJTS( jtsGeom );
    }

    @Override
    public Envelope getEnvelope() {
        // TODO consider 3D geometries
        com.vividsolutions.jts.geom.Envelope jtsEnvelope = getJTSGeometry().getEnvelopeInternal();
        Point min = new DefaultPoint( null, crs, pm, new double[] { jtsEnvelope.getMinX(), jtsEnvelope.getMinY() } );
        Point max = new DefaultPoint( null, crs, pm, new double[] { jtsEnvelope.getMaxX(), jtsEnvelope.getMaxY() } );
        return new DefaultEnvelope( null, crs, pm, min, max );
    }

    /**
     * Returns an equivalent (or best-fit) JTS geometry object.
     * 
     * @return an equivalent (or best-fit) JTS geometry
     */
    public com.vividsolutions.jts.geom.Geometry getJTSGeometry() {
        if ( jtsGeometry == null ) {
            jtsGeometry = buildJTSGeometry();
        }
        return jtsGeometry;
    }

    protected com.vividsolutions.jts.geom.Geometry buildJTSGeometry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GMLStdProps getGMLProperties() {
        return standardProps;
    }

    @Override
    public void setGMLProperties( GMLStdProps standardProps ) {
        this.standardProps = standardProps;
    }

    /**
     * Helper methods for creating {@link AbstractDefaultGeometry} from JTS geometries that have been derived from this
     * geometry by JTS spatial analysis methods.
     * 
     * @param jtsGeom
     * @return geometry with precision model and CRS information that are identical to the ones of this geometry, or
     *         null if the given geometry is an empty collection
     */
    @SuppressWarnings("unchecked")
    public AbstractDefaultGeometry createFromJTS( com.vividsolutions.jts.geom.Geometry jtsGeom ) {

        AbstractDefaultGeometry geom = null;
        if ( jtsGeom instanceof com.vividsolutions.jts.geom.Point ) {
            com.vividsolutions.jts.geom.Point jtsPoint = (com.vividsolutions.jts.geom.Point) jtsGeom;
            geom = new DefaultPoint( null, crs, pm, new double[] { jtsPoint.getX(), jtsPoint.getY() } );
        } else if ( jtsGeom instanceof com.vividsolutions.jts.geom.LinearRing ) {
            com.vividsolutions.jts.geom.LinearRing jtsLinearRing = (com.vividsolutions.jts.geom.LinearRing) jtsGeom;
            geom = new DefaultLinearRing( null, crs, pm, getAsPoints( jtsLinearRing.getCoordinateSequence() ) );
        } else if ( jtsGeom instanceof com.vividsolutions.jts.geom.LineString ) {
            com.vividsolutions.jts.geom.LineString jtsLineString = (com.vividsolutions.jts.geom.LineString) jtsGeom;
            geom = new DefaultLineString( null, crs, pm, getAsPoints( jtsLineString.getCoordinateSequence() ) );
        } else if ( jtsGeom instanceof com.vividsolutions.jts.geom.Polygon ) {
            com.vividsolutions.jts.geom.Polygon jtsPolygon = (com.vividsolutions.jts.geom.Polygon) jtsGeom;
            Points exteriorPoints = getAsPoints( jtsPolygon.getExteriorRing().getCoordinateSequence() );
            LinearRing exteriorRing = new DefaultLinearRing( null, crs, pm, exteriorPoints );
            List<Ring> interiorRings = new ArrayList<Ring>( jtsPolygon.getNumInteriorRing() );
            for ( int i = 0; i < interiorRings.size(); i++ ) {
                Points interiorPoints = getAsPoints( jtsPolygon.getInteriorRingN( i ).getCoordinateSequence() );
                interiorRings.add( new DefaultLinearRing( null, crs, pm, interiorPoints ) );
            }
            geom = new DefaultPolygon( null, crs, pm, exteriorRing, interiorRings );
        } else if ( jtsGeom instanceof com.vividsolutions.jts.geom.MultiPoint ) {
            com.vividsolutions.jts.geom.MultiPoint jtsMultiPoint = (com.vividsolutions.jts.geom.MultiPoint) jtsGeom;
            if ( jtsMultiPoint.getNumGeometries() > 0 ) {
                List<Point> members = new ArrayList<Point>( jtsMultiPoint.getNumGeometries() );
                for ( int i = 0; i < jtsMultiPoint.getNumGeometries(); i++ ) {
                    members.add( (Point) createFromJTS( jtsMultiPoint.getGeometryN( i ) ) );
                }
                geom = new DefaultMultiPoint( id, crs, pm, members );
            }
        } else if ( jtsGeom instanceof com.vividsolutions.jts.geom.MultiLineString ) {
            com.vividsolutions.jts.geom.MultiLineString jtsMultiLineString = (com.vividsolutions.jts.geom.MultiLineString) jtsGeom;
            if ( jtsMultiLineString.getNumGeometries() > 0 ) {
                List<LineString> members = new ArrayList<LineString>( jtsMultiLineString.getNumGeometries() );
                for ( int i = 0; i < jtsMultiLineString.getNumGeometries(); i++ ) {
                    members.add( (LineString) createFromJTS( jtsMultiLineString.getGeometryN( i ) ) );
                }
                geom = new DefaultMultiLineString( id, crs, pm, members );
            }
        } else if ( jtsGeom instanceof com.vividsolutions.jts.geom.MultiPolygon ) {
            com.vividsolutions.jts.geom.MultiPolygon jtsMultiPolygon = (com.vividsolutions.jts.geom.MultiPolygon) jtsGeom;
            if ( jtsMultiPolygon.getNumGeometries() > 0 ) {
                List<Polygon> members = new ArrayList<Polygon>( jtsMultiPolygon.getNumGeometries() );
                for ( int i = 0; i < jtsMultiPolygon.getNumGeometries(); i++ ) {
                    members.add( (Polygon) createFromJTS( jtsMultiPolygon.getGeometryN( i ) ) );
                }
                geom = new DefaultMultiPolygon( id, crs, pm, members );
            }
        } else if ( jtsGeom instanceof com.vividsolutions.jts.geom.GeometryCollection ) {
            com.vividsolutions.jts.geom.GeometryCollection jtsGeometryCollection = (com.vividsolutions.jts.geom.GeometryCollection) jtsGeom;
            if ( jtsGeometryCollection.getNumGeometries() > 0 ) {
                List<Geometry> members = new ArrayList<Geometry>( jtsGeometryCollection.getNumGeometries() );
                for ( int i = 0; i < jtsGeometryCollection.getNumGeometries(); i++ ) {
                    members.add( createFromJTS( jtsGeometryCollection.getGeometryN( i ) ) );
                }
                geom = new DefaultMultiGeometry( id, crs, pm, members );
            }
        } else {
            throw new RuntimeException( "Internal error. Encountered unhandled JTS geometry type '"
                                        + jtsGeom.getClass().getName() + "'." );
        }
        return geom;
    }

    protected static AbstractDefaultGeometry getAsDefaultGeometry( Geometry geometry ) {
        if ( geometry instanceof AbstractDefaultGeometry ) {
            return (AbstractDefaultGeometry) geometry;
        }
        if ( geometry instanceof GeometryReference<?> ) {
            Geometry refGeometry = ( (GeometryReference<?>) geometry ).getReferencedObject();
            if ( refGeometry instanceof AbstractDefaultGeometry ) {
                return (AbstractDefaultGeometry) refGeometry;
            }
        }
        throw new RuntimeException( "Cannot convert Geometry to AbstractDefaultGeometry." );
    }

    private Points getAsPoints( CoordinateSequence seq ) {
        return new JTSPoints( seq );
    }
}