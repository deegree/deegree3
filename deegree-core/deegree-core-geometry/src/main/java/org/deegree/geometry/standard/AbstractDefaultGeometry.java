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

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.uom.Unit;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKTWriter;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.refs.GeometryReference;
import org.deegree.geometry.standard.multi.DefaultMultiGeometry;
import org.deegree.geometry.standard.multi.DefaultMultiLineString;
import org.deegree.geometry.standard.multi.DefaultMultiPoint;
import org.deegree.geometry.standard.multi.DefaultMultiPolygon;
import org.deegree.geometry.standard.points.JTSPoints;
import org.deegree.geometry.standard.primitive.DefaultLineString;
import org.deegree.geometry.standard.primitive.DefaultLinearRing;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.geometry.standard.primitive.DefaultPolygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.locationtech.jts.geom.CoordinateSequence;

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
    protected final static org.locationtech.jts.geom.GeometryFactory jtsFactory = new org.locationtech.jts.geom.GeometryFactory();

    /** Geometry identifier. */
    protected String id;

    private GMLObjectType type;

    private List<Property> props;

    /** Reference to a coordinate system. */
    protected ICRS crs;

    protected PrecisionModel pm;

    // contains an equivalent (or best-fit) JTS geometry object
    protected org.locationtech.jts.geom.Geometry jtsGeometry;

    protected Envelope env;

    /**
     * @param id
     * @param crs
     * @param pm
     */
    public AbstractDefaultGeometry( String id, ICRS crs, PrecisionModel pm ) {
        this.id = id;
        this.crs = crs;
        this.pm = pm;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public GMLObjectType getType() {
        return type;
    }

    @Override
    public void setType( GMLObjectType type ) {
        this.type = type;
    }

    @Override
    public void setId( String id ) {
        this.id = id;
    }

    @Override
    public ICRS getCoordinateSystem() {
        return crs;
    }

    @Override
    public void setCoordinateSystem( ICRS crs ) {
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
        return (Point) createFromJTS( getJTSGeometry().getCentroid(), crs );
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
        ICRS crs = this.crs;
        if ( crs == null ) {
            crs = geometry.getCoordinateSystem();
        }
        org.locationtech.jts.geom.Geometry jtsGeom = jtsGeoms.first.intersection( jtsGeoms.second );
        return createFromJTS( jtsGeom, crs );
    }

    @Override
    public Geometry getUnion( Geometry geometry ) {
        JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair( this, geometry );
        org.locationtech.jts.geom.Geometry jtsGeom = jtsGeoms.first.union( jtsGeoms.second );
        return createFromJTS( jtsGeom, crs );
    }

    @Override
    public Geometry getDifference( Geometry geometry ) {
        JTSGeometryPair jtsGeoms = JTSGeometryPair.createCompatiblePair( this, geometry );
        org.locationtech.jts.geom.Geometry jtsGeom = jtsGeoms.first.difference( jtsGeoms.second );
        return createFromJTS( jtsGeom, crs );
    }

    @Override
    public Geometry getBuffer( Measure distance ) {
        // TODO get double in CoordinateSystem units
        double crsDistance = distance.getValueAsDouble();
        org.locationtech.jts.geom.Geometry jtsGeom = getJTSGeometry().buffer( crsDistance );
        return createFromJTS( jtsGeom, crs );
    }

    @Override
    public Geometry getConvexHull() {
        org.locationtech.jts.geom.Geometry jtsGeom = getJTSGeometry().convexHull();
        return createFromJTS( jtsGeom, crs );
    }

    @Override
    public Envelope getEnvelope() {
        if ( env == null ) {
            org.locationtech.jts.geom.Envelope jtsEnvelope = getJTSGeometry().getEnvelopeInternal();
            Point min = new DefaultPoint( null, crs, pm, new double[] { jtsEnvelope.getMinX(), jtsEnvelope.getMinY() } );
            Point max = new DefaultPoint( null, crs, pm, new double[] { jtsEnvelope.getMaxX(), jtsEnvelope.getMaxY() } );
            env = new DefaultEnvelope( null, crs, pm, min, max );
        }
        return env;
    }

    /**
     * Returns an equivalent (or best-fit) JTS geometry object.
     * 
     * @return an equivalent (or best-fit) JTS geometry
     */
    public org.locationtech.jts.geom.Geometry getJTSGeometry() {
        if ( jtsGeometry == null ) {
            jtsGeometry = buildJTSGeometry();
        }
        return jtsGeometry;
    }

    protected org.locationtech.jts.geom.Geometry buildJTSGeometry() {
        throw new UnsupportedOperationException( "#buildJTSGeometry() is not implemented for "
                                                 + this.getClass().getName() );
    }

    @Override
    public List<Property> getProperties() {
        if ( props == null ) {
            return emptyList();
        }
        return props;
    }

    @Override
    public List<Property> getProperties( QName propName ) {
        if ( props == null ) {
            return emptyList();
        }
        List<Property> namedProps = new ArrayList<Property>( props.size() );
        for ( Property property : props ) {
            if ( propName.equals( property.getName() ) ) {
                namedProps.add( property );
            }
        }
        return namedProps;
    }

    @Override
    public void setProperties( List<Property> props ) {
        this.props = props;
    }

    /**
     * Helper methods for creating {@link AbstractDefaultGeometry} from JTS geometries that have been derived from this
     * geometry by JTS spatial analysis methods.
     * 
     * @param jtsGeom
     * @param crs
     * @return geometry with precision model and CoordinateSystem information that are identical to the ones of this
     *         geometry, or null if the given geometry is an empty collection
     */
    @SuppressWarnings("unchecked")
    public AbstractDefaultGeometry createFromJTS( org.locationtech.jts.geom.Geometry jtsGeom, ICRS crs ) {

        AbstractDefaultGeometry geom = null;
        if ( jtsGeom.isEmpty() ) {
            return null;
        }
        if ( jtsGeom instanceof org.locationtech.jts.geom.Point ) {
            org.locationtech.jts.geom.Point jtsPoint = (org.locationtech.jts.geom.Point) jtsGeom;
            if ( Double.isNaN( jtsPoint.getCoordinate().z ) ) {
                geom = new DefaultPoint( null, crs, pm, new double[] { jtsPoint.getX(), jtsPoint.getY() } );
            } else {
                geom = new DefaultPoint( null, crs, pm, new double[] { jtsPoint.getX(), jtsPoint.getY(),
                                                                      jtsPoint.getCoordinate().z } );
            }
        } else if ( jtsGeom instanceof org.locationtech.jts.geom.LinearRing ) {
            org.locationtech.jts.geom.LinearRing jtsLinearRing = (org.locationtech.jts.geom.LinearRing) jtsGeom;
            geom = new DefaultLinearRing( null, crs, pm, getAsPoints( jtsLinearRing.getCoordinateSequence(), crs ) );
        } else if ( jtsGeom instanceof org.locationtech.jts.geom.LineString ) {
            org.locationtech.jts.geom.LineString jtsLineString = (org.locationtech.jts.geom.LineString) jtsGeom;
            geom = new DefaultLineString( null, crs, pm, getAsPoints( jtsLineString.getCoordinateSequence(), crs ) );
        } else if ( jtsGeom instanceof org.locationtech.jts.geom.Polygon ) {
            org.locationtech.jts.geom.Polygon jtsPolygon = (org.locationtech.jts.geom.Polygon) jtsGeom;
            Points exteriorPoints = getAsPoints( jtsPolygon.getExteriorRing().getCoordinateSequence(), crs );
            LinearRing exteriorRing = new DefaultLinearRing( null, crs, pm, exteriorPoints );
            List<Ring> interiorRings = new ArrayList<Ring>( jtsPolygon.getNumInteriorRing() );
            for ( int i = 0; i < jtsPolygon.getNumInteriorRing(); i++ ) {
                Points interiorPoints = getAsPoints( jtsPolygon.getInteriorRingN( i ).getCoordinateSequence(), crs );
                interiorRings.add( new DefaultLinearRing( null, crs, pm, interiorPoints ) );
            }
            geom = new DefaultPolygon( null, crs, pm, exteriorRing, interiorRings );
        } else if ( jtsGeom instanceof org.locationtech.jts.geom.MultiPoint ) {
            org.locationtech.jts.geom.MultiPoint jtsMultiPoint = (org.locationtech.jts.geom.MultiPoint) jtsGeom;
            if ( jtsMultiPoint.getNumGeometries() > 0 ) {
                List<Point> members = new ArrayList<Point>( jtsMultiPoint.getNumGeometries() );
                for ( int i = 0; i < jtsMultiPoint.getNumGeometries(); i++ ) {
                    members.add( (Point) createFromJTS( jtsMultiPoint.getGeometryN( i ), crs ) );
                }
                geom = new DefaultMultiPoint( null, crs, pm, members );
            }
        } else if ( jtsGeom instanceof org.locationtech.jts.geom.MultiLineString ) {
            org.locationtech.jts.geom.MultiLineString jtsMultiLineString = (org.locationtech.jts.geom.MultiLineString) jtsGeom;
            if ( jtsMultiLineString.getNumGeometries() > 0 ) {
                List<LineString> members = new ArrayList<LineString>( jtsMultiLineString.getNumGeometries() );
                for ( int i = 0; i < jtsMultiLineString.getNumGeometries(); i++ ) {
                    Curve curve = (Curve) createFromJTS( jtsMultiLineString.getGeometryN( i ), crs );
                    members.add( curve.getAsLineString() );
                }
                geom = new DefaultMultiLineString( null, crs, pm, members );
            }
        } else if ( jtsGeom instanceof org.locationtech.jts.geom.MultiPolygon ) {
            org.locationtech.jts.geom.MultiPolygon jtsMultiPolygon = (org.locationtech.jts.geom.MultiPolygon) jtsGeom;
            if ( jtsMultiPolygon.getNumGeometries() > 0 ) {
                List<Polygon> members = new ArrayList<Polygon>( jtsMultiPolygon.getNumGeometries() );
                for ( int i = 0; i < jtsMultiPolygon.getNumGeometries(); i++ ) {
                    members.add( (Polygon) createFromJTS( jtsMultiPolygon.getGeometryN( i ), crs ) );
                }
                geom = new DefaultMultiPolygon( null, crs, pm, members );
            }
        } else if ( jtsGeom instanceof org.locationtech.jts.geom.GeometryCollection ) {
            org.locationtech.jts.geom.GeometryCollection jtsGeometryCollection = (org.locationtech.jts.geom.GeometryCollection) jtsGeom;
            if ( jtsGeometryCollection.getNumGeometries() > 0 ) {
                List<Geometry> members = new ArrayList<Geometry>( jtsGeometryCollection.getNumGeometries() );
                for ( int i = 0; i < jtsGeometryCollection.getNumGeometries(); i++ ) {
                    members.add( createFromJTS( jtsGeometryCollection.getGeometryN( i ), crs ) );
                }
                geom = new DefaultMultiGeometry( null, crs, pm, members );
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

    private Points getAsPoints( CoordinateSequence seq, ICRS crs ) {
        return new JTSPoints( crs, seq );
    }

    @Override
    public boolean isSFSCompliant() {
        return false;
    }

    @Override
    public String toString() {
        String wkt = WKTWriter.write( this );
        if ( wkt.length() > 1000 ) {
            return wkt.substring( 0, 1000 ) + " [...]";
        }
        return wkt;
    }
}
