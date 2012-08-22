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
package org.deegree.model.spatialschema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.crs.CoordinateSystem;

/**
 * default implementation of the Aggregate interface
 *
 * ------------------------------------------------------------
 *
 * @version 8.6.2001
 * @author Andreas Poth href="mailto:poth@lat-lon.de"
 */
public abstract class AggregateImpl extends GeometryImpl implements Aggregate, Serializable {

    private static ILogger LOG = LoggerFactory.getLogger( AggregateImpl.class );

    /** Use serialVersionUID for interoperability. */
    private final static long serialVersionUID = 1161164609227432958L;

    protected ArrayList<Geometry> aggregate = new ArrayList<Geometry>( 500 );

    /**
     * Creates a new AggregateImpl object.
     *
     * @param crs
     */
    protected AggregateImpl( CoordinateSystem crs ) {
        super( crs );
    }

    /**
     * @return the number of Geometry within the aggregation
     */
    public int getSize() {
        return aggregate.size();
    }

    /**
     * merges this aggregation with another one
     *
     * @exception GeometryException
     *                a GeometryException will be thrown if the submitted isn't the same type as the recieving one.
     */
    public void merge( Aggregate aggregate )
                            throws GeometryException {
        if ( !this.getClass().getName().equals( aggregate.getClass().getName() ) ) {
            throw new GeometryException( "Aggregations are not of the same type!" );
        }

        for ( int i = 0; i < this.getSize(); i++ ) {
            this.add( aggregate.getObjectAt( i ) );
        }

        setValid( false );
    }

    /**
     * adds an Geometry to the aggregation
     */
    public void add( Geometry gmo ) {
        aggregate.add( gmo );

        setValid( false );
    }

    /**
     * inserts a Geometry in the aggregation. all elements with an index equal or larger index will be moved. if index
     * is larger then getSize() - 1 or smaller then 0 or gmo equals null an exception will be thrown.
     *
     * @param gmo
     *            Geometry to insert.
     * @param index
     *            position where to insert the new Geometry
     */
    public void insertObjectAt( Geometry gmo, int index )
                            throws GeometryException {
        if ( ( index < 0 ) || ( index > this.getSize() - 1 ) ) {
            throw new GeometryException( "invalid index/position: " + index + " to insert a geometry!" );
        }

        if ( gmo == null ) {
            throw new GeometryException( "gmo == null. it isn't possible to insert a value" + " that equals null!" );
        }

        aggregate.add( index, gmo );

        setValid( false );
    }

    /**
     * sets the submitted Geometry at the submitted index. the element at the position <code>index</code> will be
     * removed. if index is larger then getSize() - 1 or smaller then 0 or gmo equals null an exception will be thrown.
     *
     * @param gmo
     *            Geometry to set.
     * @param index
     *            position where to set the new Geometry
     */
    public void setObjectAt( Geometry gmo, int index )
                            throws GeometryException {
        if ( ( index < 0 ) || ( index > this.getSize() - 1 ) ) {
            throw new GeometryException( "invalid index/position: " + index + " to set a geometry!" );
        }

        if ( gmo == null ) {
            throw new GeometryException( "gmo == null. it isn't possible to set a value" + " that equals null!" );
        }

        aggregate.set( index, gmo );

        setValid( false );
    }

    /**
     * removes the submitted Geometry from the aggregation
     *
     * @return the removed Geometry
     */
    public Geometry removeObject( Geometry gmo ) {
        if ( gmo == null ) {
            return null;
        }

        int i = aggregate.indexOf( gmo );

        Geometry gmo_ = null;

        try {
            gmo_ = removeObjectAt( i );
        } catch ( GeometryException e ) {
            LOG.logError( e.getMessage(), e );
        }

        setValid( false );

        return gmo_;
    }

    /**
     * removes the Geometry at the submitted index from the aggregation. if index is larger then getSize() - 1 or
     * smaller then 0 an exception will be thrown.
     *
     * @return the removed Geometry
     */
    public Geometry removeObjectAt( int index )
                            throws GeometryException {
        if ( index < 0 ) {
            return null;
        }

        if ( index > ( this.getSize() - 1 ) ) {
            throw new GeometryException( "invalid index/position: " + index + " to remove a geometry!" );
        }

        Geometry gmo = aggregate.remove( index );

        setValid( false );

        return gmo;
    }

    /**
     * removes all Geometry from the aggregation.
     */
    public void removeAll() {
        aggregate.clear();
        envelope = null;
        setValid( false );
    }

    /**
     * returns the Geometry at the submitted index. if index is larger then getSize() - 1 or smaller then 0 an exception
     * will be thrown.
     */
    public Geometry getObjectAt( int index ) {
        return aggregate.get( index );
    }

    /**
     * returns all Geometries as array
     */
    public Geometry[] getAll() {
        Geometry[] gmos = new Geometry[this.getSize()];

        return aggregate.toArray( gmos );
    }

    public boolean isMember( Geometry gmo ) {
        return aggregate.contains( gmo );
    }

    public Iterator<Geometry> getIterator() {
        return aggregate.iterator();
    }

    @Override
    public boolean isEmpty() {
        return ( getSize() == 0 );
    }

    @Override
    public void setCoordinateSystem( CoordinateSystem crs ) {
        super.setCoordinateSystem( crs );

        if ( aggregate != null ) {
            for ( int i = 0; i < aggregate.size(); i++ ) {
                ( (GeometryImpl) getObjectAt( i ) ).setCoordinateSystem( crs );
            }
            setValid( false );
        }
    }

    /**
     * translate the point by the submitted values. the <code>dz</code>- value will be ignored.
     */
    @Override
    public void translate( double[] d ) {
        try {
            for ( int i = 0; i < getSize(); i++ ) {
                Geometry gmo = getObjectAt( i );
                gmo.translate( d );
            }
            setValid( false );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }
        setValid( false );
    }

    @Override
    public boolean equals( Object other ) {
        if ( envelope == null ) {
            calculateParam();
        }
        if ( !super.equals( other ) || !( other instanceof AggregateImpl )
             || !envelope.equals( ( (Geometry) other ).getEnvelope() )
             || ( getSize() != ( (Aggregate) other ).getSize() ) ) {
            return false;
        }

        try {
            for ( int i = 0; i < getSize(); i++ ) {
                Object o1 = getObjectAt( i );
                Object o2 = ( (Aggregate) other ).getObjectAt( i );

                if ( !o1.equals( o2 ) ) {
                    return false;
                }
            }
        } catch ( Exception ex ) {
            return false;
        }

        return true;
    }

    /**
     * The Boolean valued operation "intersects" shall return TRUE if this Geometry intersects another Geometry. Within
     * a Complex, the Primitives do not intersect one another. In general, topologically structured data uses shared
     * geometric objects to capture intersection information.
     */
    @Override
    public boolean intersects( Geometry gmo ) {
        boolean inter = false;

        try {
            for ( int i = 0; i < aggregate.size(); i++ ) {
                if ( this.getObjectAt( i ).intersects( gmo ) ) {
                    inter = true;
                    break;
                }
            }
        } catch ( Exception e ) {
            // nottin
        }

        return inter;
    }

    @Override
    public String toString() {
        String ret = null;
        ret = "aggregate = " + aggregate + "\n";
        ret += ( "envelope = " + envelope + "\n" );
        return ret;
    }
}
