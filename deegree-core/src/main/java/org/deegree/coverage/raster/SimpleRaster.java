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
package org.deegree.coverage.raster;

import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.geometry.Envelope;

/**
 * This class represents a single raster with multiple bands.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SimpleRaster extends AbstractRaster {

    // private RasterDataContainer rasterDataContainer;

    private final Object LOCK = new Object();

    private RasterData data;

    /**
     * Create a SimpleRaster with no raster data but with an envelope and raster envelope.
     * 
     * @param envelope
     *            The envelope of the new raster.
     * @param rasterReference
     *            The raster envelope of the new raster.
     */
    protected SimpleRaster( Envelope envelope, RasterGeoReference rasterReference ) {
        super( envelope, rasterReference );
    }

    /**
     * Creates a new SimpleRaster with given RasterData and Envelope
     * 
     * @param raster
     *            content for the SimpleRaster
     * @param envelope
     *            The envelope of the new raster.
     * @param rasterReference
     *            The raster envelope of the new raster.
     */
    public SimpleRaster( RasterData raster, Envelope envelope, RasterGeoReference rasterReference ) {
        this( envelope, rasterReference );
        this.data = raster;
        // this.rasterDataContainer = new MemoryRasterDataContainer( raster );
    }

    // /**
    // * Creates a new SimpleRaster with given RasterDataContainer and Envelope
    // *
    // * @param rasterDataContainer
    // * data source for the SimpleRaster
    // * @param envelope
    // * boundary of the new raster
    // * @param rasterReference
    // * RasterReference for the new raster
    // */
    // public SimpleRaster( RasterDataContainer rasterDataContainer, Envelope envelope, RasterGeoReference
    // rasterReference ) {
    // this( envelope, rasterReference );
    //        
    // }

    /**
     * Creates a SimpleRaster with same size, DataType and InterleaveType
     * 
     * @param bands
     *            number of bands
     * @return new empty SimpleRaster
     */
    public SimpleRaster createCompatibleSimpleRaster( BandType[] bands ) {
        RasterData data = this.getRasterData();
        RasterData newRaster = data.createCompatibleWritableRasterData(
                                                                        new RasterRect( 0, 0, getColumns(), getRows() ),
                                                                        bands );
        return new SimpleRaster( newRaster, getEnvelope(), getRasterReference() );
    }

    /**
     * Creates a new empty writable SimpleRaster with same size, DataType and InterleaveType.
     * 
     * @return new empty SimpleRaster
     */
    public SimpleRaster createCompatibleSimpleRaster() {
        int height = this.getRows();
        int width = this.getColumns();
        RasterData data = this.getRasterData();
        BandType[] bands = data.getDataInfo().bandInfo;
        RasterData newRaster = data.createCompatibleWritableRasterData( new RasterRect( 0, 0, width, height ), bands );
        return new SimpleRaster( newRaster, this.getEnvelope(), this.getRasterReference() );
    }

    /**
     * Creates a new empty SimpleRaster with same DataType and InterleaveType. Size is determined by the given envelope.
     * 
     * @param rEnv
     *            The raster envelope of the new SimpleRaster.
     * @param env
     *            The boundary of the new SimpleRaster.
     * @return A new empty SimpleRaster.
     */
    public SimpleRaster createCompatibleSimpleRaster( RasterGeoReference rEnv, Envelope env ) {
        int[] size = rEnv.getSize( env );
        RasterRect rasterRect = new RasterRect( 0, 0, size[0], size[1] );
        RasterData data = this.getRasterData();
        BandType[] bands = data.getDataInfo().bandInfo;
        RasterData newRaster = data.createCompatibleWritableRasterData( rasterRect, bands );
        return new SimpleRaster( newRaster, env, rEnv );
    }

    @Override
    public SimpleRaster copy() {
        SimpleRaster result = this.createCompatibleSimpleRaster();
        result.setSubRaster( getEnvelope(), this );
        return result;
    }

    /**
     * Returns the RasterData of this SimpleRaster
     * 
     * @return The raster data of this SimpleRaster.
     */
    public RasterData getRasterData() {
        return data;
    }

    /**
     * Returns a read-only copy of the RasterData of this SimpleRaster
     * 
     * @return The raster data of this SimpleRaster (read-only).
     */
    public RasterData getReadOnlyRasterData() {
        return ( data != null ) ? data.asReadOnly() : null;
    }

    @Override
    public SimpleRaster getSubRaster( Envelope envelope ) {
        return getSubRaster( envelope, null );
    }

    @Override
    public SimpleRaster getSubRaster( Envelope envelope, BandType[] bands ) {
        return getSubRaster( envelope, bands, null );
    }

    @Override
    public SimpleRaster getSubRaster( Envelope envelope, BandType[] bands, OriginLocation targetLocation ) {
        // rb: testing for envelope equality can lead to a memory leak, because the memory can not be freed.
        RasterRect rasterRect = getRasterReference().convertEnvelopeToRasterCRS( envelope );
        RasterGeoReference rasterReference = getRasterReference().createRelocatedReference( targetLocation, envelope );
        // RasterData view = getReadOnlyRasterData().getSubset( rasterRect, bands );
        // rb: don't need to get a readonly raster data, because it will be filled with data later.
        RasterData view = getRasterData().getSubset( rasterRect, bands );
        return new SimpleRaster( view, envelope, rasterReference );
    }

    @Override
    public SimpleRaster getSubRaster( double x, double y, double x2, double y2 ) {
        // what about the precision model? Formerly: getRasterReference().getDelta() was used
        Envelope env = getGeometryFactory().createEnvelope( new double[] { x, y }, new double[] { x2, y2 }, null );
        return getSubRaster( env );
    }

    /**
     * Returns a single band of the raster.
     * 
     * @param band
     *            Number of the selected band.
     * @return A copy of the selected band.
     */
    public SimpleRaster getBand( int band ) {
        return new SimpleRaster( getRasterData().getSubset( new RasterRect( 0, 0, getColumns(), getRows() ),
                                                            new BandType[] {} ), getEnvelope(), getRasterReference() );
    }

    @Override
    public void setSubRaster( Envelope env, AbstractRaster source ) {
        RasterRect rect = getRasterReference().convertEnvelopeToRasterCRS( env );
        SimpleRaster src = source.getSubRaster( env ).getAsSimpleRaster();
        // source.getSubset( env ) already returns a copy, no need for getReadOnlyRasterData
        getRasterData().setSubset( rect.x, rect.y, rect.width, rect.height, src.getRasterData() );
    }

    @Override
    public void setSubRaster( double x, double y, AbstractRaster source ) {
        // calculate position in RasterData
        int offset[] = getRasterReference().getRasterCoordinate( x, y );
        RasterData sourceRD = source.getAsSimpleRaster().getReadOnlyRasterData();
        getRasterData().setSubset( offset[0], offset[1], sourceRD.getColumns(), sourceRD.getRows(), sourceRD );
    }

    @Override
    public void setSubRaster( double x, double y, int dstBand, AbstractRaster source ) {
        // calculate position in RasterData
        int offset[] = getRasterReference().getRasterCoordinate( x, y );
        getRasterData().setSubset( offset[0], offset[1], dstBand, 0, source.getAsSimpleRaster().getReadOnlyRasterData() );
    }

    @Override
    public void setSubRaster( Envelope env, int dstBand, AbstractRaster source ) {
        // calculate position in RasterData
        RasterRect rect = getRasterReference().convertEnvelopeToRasterCRS( env );

        getRasterData().setSubset( rect.x, rect.y, dstBand, 0, source.getAsSimpleRaster().getReadOnlyRasterData() );
    }

    @Override
    public int getColumns() {
        return getRasterData().getColumns();
    }

    @Override
    public int getRows() {
        return getRasterData().getRows();
    }

    /**
     * @return the number of bands in this raster.
     */
    public int getBands() {
        return getRasterData().getBands();
    }

    /**
     * @return the number of bands in this raster.
     */
    public BandType[] getBandTypes() {
        return getRasterData().getDataInfo().bandInfo;
    }

    @Override
    public RasterDataInfo getRasterDataInfo() {
        return getRasterData().getDataInfo();
    }

    @Override
    public SimpleRaster getAsSimpleRaster() {
        return this;
    }

    @Override
    public String toString() {
        return "SimpleRaster: " + envelopeString();
    }

    @Override
    public boolean isSimpleRaster() {
        return true;
    }

    /**
     * Cleans up all used memory buffers.
     */
    public void dispose() {
        synchronized ( LOCK ) {
            if ( data != null ) {
                data.dispose();
            }
        }

    }
}