//$HeadURL: svn+ssh://otonnhofer@svn.wald.intevation.org/deegree/deegree3/model/trunk/src/org/deegree/model/coverage/raster/DatastoreRaster.java $
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
package org.deegree.model.coverage.raster.implementation;

import org.deegree.model.geometry.primitive.Envelope;
import org.deegree.model.coverage.raster.AbstractRaster;
import org.deegree.model.coverage.raster.RasterEnvelope;
import org.deegree.model.coverage.raster.SimpleRaster;
import org.deegree.model.filterencoding.ComplexFilter;

/**
 * This is a first version of an {@link AbstractRaster} that builds on RasterDatastore.
 * 
 * @version $Revision: $
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: $
 * 
 */
public class DatastoreRaster extends AbstractRaster {

    RasterDatastore datastore;

    /**
     * Create a new DatastoreRaster with given RasterDatastore as data backend.
     * @param datastore 
     */
    public DatastoreRaster( RasterDatastore datastore ) {
        this.datastore = datastore;
    }

    /* (non-Javadoc)
     * @see org.deegree.model.coverage.AbstractCoverage#getEnvelope()
     */
    public Envelope getEnvelope() {
        return datastore.getEnvelope();
    }

    /* (non-Javadoc)
     * @see org.deegree.model.coverage.raster.AbstractRaster#getRasterEnvelope()
     */
    public RasterEnvelope getRasterEnvelope() {
        return datastore.getRasterEnvelope();
    }

    @Override
    public AbstractRaster copy() {
        return new DatastoreRaster( datastore );
    }

    @Override
    public SimpleRaster getAsSimpleRaster() {
        return datastore.getAbstractRaster().getAsSimpleRaster();
    }

    @Override
    public DatastoreRaster getSubset( Envelope env ) {
        return new DatastoreRaster( datastore.getSubset( env ) );
    }

    /* (non-Javadoc)
     * @see org.deegree.model.coverage.raster.AbstractRaster#getSubset(double, double, double, double)
     */
    @Override
    public DatastoreRaster getSubset( double x, double y, double x2, double y2 ) {
        Envelope env = getGeometryFactory().createEnvelope( new double[] { x, y }, new double[] { x2, y2 },
                                                            getRasterEnvelope().getDelta(), null );
        return getSubset( env );
    }

    /**
     * Get a filtered subset.  
     * @param filter The filter for the subset.
     * @return A new DatastoreRaster with data that matched the filter.
     */
    public DatastoreRaster getSubset( ComplexFilter filter ) {
        return new DatastoreRaster( datastore.getSubset( filter ) );
    }

    @Override
    public void setSubset( Envelope env, AbstractRaster source ) {
        throw new UnsupportedOperationException( DatastoreRaster.class + " are immutable" );
    }

    @Override
    public void setSubset( double x, double y, AbstractRaster source ) {
        throw new UnsupportedOperationException( DatastoreRaster.class + " are immutable" );
    }

    @Override
    public void setSubset( double x, double y, int dstBand, AbstractRaster source ) {
        throw new UnsupportedOperationException( DatastoreRaster.class + " are immutable" );
    }

    @Override
    public void setSubset( Envelope env, int dstBand, AbstractRaster source ) {
        throw new UnsupportedOperationException( DatastoreRaster.class + " are immutable" );
    }

}
