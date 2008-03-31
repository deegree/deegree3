//$HeadURL$
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
package org.deegree.model.coverage.raster;

import java.util.List;

import org.deegree.model.geometry.primitive.Envelope;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;

import org.deegree.model.legacy.GeometryConverter;

/**
 * This class represents an AbstractRaster with multiple ranges.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 * 
 */
public class DatastoreMultiRangedRaster extends AbstractRaster {

    private RasterDatastore datastore;

    public DatastoreMultiRangedRaster( RasterDatastore container ) {
        super();
        this.datastore = container;
    }

    // private DatastoreMultiRangedRaster( RasterDatastore container, ComplexFilter filter ) {
    // super();
    // this.container = container;
    // this.currentFilter = filter;
    // }

    @Override
    public DatastoreMultiRangedRaster copy() {
        throw new UnsupportedOperationException();

    }

    @Override
    public RasterEnvelope getRasterEnvelope() {
        return datastore.getRasterEnvelope();
    }

    @Override
    public Envelope getEnvelope() {
        return datastore.getEnvelope();
    }

    /**
     * Adds an AbstractRaster to the MultiRangedRaster
     * 
     * @param raster
     */
    public void addRaster( AbstractRaster raster ) {
        throw new UnsupportedOperationException( DatastoreMultiRangedRaster.class.toString() + " is immutable." );
    }

    /**
     * Create a subset with filter expression
     * 
     * @param filter
     * @return
     */
    public DatastoreMultiRangedRaster getSubset( ComplexFilter filter ) {
        return new DatastoreMultiRangedRaster( datastore.getSubset( filter ) );
    }

    @Override
    public DatastoreMultiRangedRaster getSubset( Envelope env ) {
        return new DatastoreMultiRangedRaster( datastore.getSubset( env ) );
    }

    @Override
    public DatastoreMultiRangedRaster getSubset( double x, double y, double x2, double y2 ) {
        Envelope env = getGeometryFactory().createEnvelope( new double[] { x, y }, new double[] { x2, y2 },
                                                            getRasterEnvelope().getDelta(), null );
        return getSubset( env );
    }

    /**
     * Sets the MultiRangedRaster with data from source.
     * 
     * The number of ranges and the number of bands in source must be equal.
     * 
     * @param x
     *            left boundary
     * @param y
     *            upper boundary
     * @param source
     *            data to copy
     */
    @Override
    public void setSubset( double x, double y, AbstractRaster source ) {
        throw new UnsupportedOperationException( DatastoreMultiRangedRaster.class.toString() + " is immutable." );
    }

    /**
     * Sets a range with data from source.
     * 
     * @param x
     *            left boundary
     * @param y
     *            upper boundary
     * @param index
     *            index of the destination range
     * @param source
     *            data to copy (first band will be used)
     */
    @Override
    public void setSubset( double x, double y, int index, AbstractRaster source ) {
        throw new UnsupportedOperationException( DatastoreMultiRangedRaster.class.toString() + " is immutable." );
    }

    /**
     * Sets the MultiRangedRaster with data from source.
     * 
     * The number of ranges must be equal.
     * 
     * @param x
     *            left boundary
     * @param y
     *            upper boundary
     * @param source
     *            data to copy
     */
    public void setSubset( double x, double y, DatastoreMultiRangedRaster source ) {
        throw new UnsupportedOperationException( DatastoreMultiRangedRaster.class.toString() + " is immutable." );
    }

    @Override
    public void setSubset( Envelope env, AbstractRaster source ) {
        throw new UnsupportedOperationException( DatastoreMultiRangedRaster.class.toString() + " is immutable." );
    }

    @Override
    public void setSubset( Envelope env, int dstBand, AbstractRaster source ) {
        throw new UnsupportedOperationException( DatastoreMultiRangedRaster.class.toString() + " is immutable." );
    }

    @Override
    public SimpleRaster getAsSimpleRaster() {
        return datastore.getAbstractRaster().getAsSimpleRaster();
    }

}
