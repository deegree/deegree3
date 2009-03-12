//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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

import org.deegree.model.coverage.raster.data.RasterData;
import org.deegree.model.coverage.raster.data.container.MemoryRasterDataContainer;
import org.deegree.model.coverage.raster.data.container.RasterDataContainer;
import org.deegree.model.coverage.raster.geom.RasterEnvelope;
import org.deegree.model.coverage.raster.geom.RasterRect;
import org.deegree.model.geometry.Envelope;

/**
 * This class represents a single raster with multiple bands.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SimpleRaster extends AbstractRaster {

    private RasterDataContainer rasterDataContainer;

    /**
     * Create a SimpleRaster with no raster data but with an envelope and raster envelope.
     * 
     * @param envelope
     *            The envelope of the new raster.
     * @param rasterEnv
     *            The raster envelope of the new raster.
     */
    protected SimpleRaster( Envelope envelope, RasterEnvelope rasterEnv ) {
        super( envelope, rasterEnv );
    }

    /**
     * Creates a new SimpleRaster with given RasterData and Envelope
     * 
     * @param raster
     *            content for the SimpleRaster
     * @param envelope
     *            The envelope of the new raster.
     * @param rasterEnv
     *            The raster envelope of the new raster.
     */
    public SimpleRaster( RasterData raster, Envelope envelope, RasterEnvelope rasterEnv ) {
        this( envelope, rasterEnv );

        this.rasterDataContainer = new MemoryRasterDataContainer( raster );
    }

    /**
     * Creates a new SimpleRaster with given RasterDataContainer and Envelope
     * 
     * @param rasterDataContainer
     *            data source for the SimpleRaster
     * @param envelope
     *            boundary of the new raster
     * @param rasterEnv
     *            RasterEnvelope for the new raster
     */
    public SimpleRaster( RasterDataContainer rasterDataContainer, Envelope envelope, RasterEnvelope rasterEnv ) {
        this( envelope, rasterEnv );
        this.rasterDataContainer = rasterDataContainer;
    }

    /**
     * Creates a SimpleRaster with same size, DataType and InterleaveType
     * 
     * @param bands
     *            number of bands
     * @return new empty SimpleRaster
     */
    public SimpleRaster createCompatibleSimpleRaster( int bands ) {
        RasterData newRaster = getRasterData().createCompatibleRasterData( bands );
        return new SimpleRaster( newRaster, getEnvelope(), getRasterEnvelope() );
    }

    /**
     * Creates a new empty SimpleRaster with same size, DataType and InterleaveType.
     * 
     * @return new empty SimpleRaster
     */
    public SimpleRaster createCompatibleSimpleRaster() {
        int height = this.getRows();
        int width = this.getColumns();
        int bands = this.getBands();
        RasterData newRaster = this.getRasterData().createCompatibleRasterData( width, height, bands );
        return new SimpleRaster( newRaster, this.getEnvelope(), this.getRasterEnvelope() );
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
    public SimpleRaster createCompatibleSimpleRaster( RasterEnvelope rEnv, Envelope env ) {
        int[] size = rEnv.getSize( env );
        int bands = this.getBands();
        RasterData newRaster = this.getRasterData().createCompatibleRasterData( size[0], size[1], bands );

        return new SimpleRaster( newRaster, env, rEnv );
    }

    @Override
    public SimpleRaster copy() {
        SimpleRaster result = this.createCompatibleSimpleRaster();
        result.setSubset( getEnvelope(), this );
        return result;
    }

    /**
     * Returns the RasterData of this SimpleRaster
     * 
     * @return The raster data of this SimpleRaster.
     */
    public RasterData getRasterData() {
        return rasterDataContainer.getRasterData();
    }

    /**
     * Returns a read-only copy of the RasterData of this SimpleRaster
     * 
     * @return The raster data of this SimpleRaster (read-only).
     */
    public RasterData getReadOnlyRasterData() {
        return rasterDataContainer.getReadOnlyRasterData();
    }

    @Override
    public SimpleRaster getSubset( Envelope envelope ) {
        // checkBounds(envelope);
        //RasterRect rasterRect = getRasterEnvelope().convertEnvelopeToRasterCRS( envelope );
        RasterEnvelope rasterEnv = getRasterEnvelope().createSubEnvelope( envelope );
        // RasterData data = getReadOnlyRasterData();
        // data.getSubset( rasterRect );
        // return new SimpleRaster( getReadOnlyRasterData().getSubset( rasterRect ), envelope, rasterEnv );
        return new SimpleRaster( getReadOnlyRasterData(), envelope, rasterEnv );
    }

    @Override
    public SimpleRaster getSubset( double x, double y, double x2, double y2 ) {
        Envelope env = getGeometryFactory().createEnvelope( new double[] { x, y }, new double[] { x2, y2 },
                                                            getRasterEnvelope().getDelta(), null );
        return getSubset( env );
    }

    /**
     * Returns a single band of the raster.
     * 
     * @param band
     *            Number of the selected band.
     * @return A copy of the selected band.
     */
    public SimpleRaster getBand( int band ) {
        return new SimpleRaster( getRasterData().getSubset( 0, 0, getColumns(), getRows(), band ), getEnvelope(),
                                 getRasterEnvelope() );
    }

    @Override
    public void setSubset( Envelope env, AbstractRaster source ) {
        RasterRect rect = getRasterEnvelope().convertEnvelopeToRasterCRS( env );
        SimpleRaster src = source.getSubset( env ).getAsSimpleRaster();
        // source.getSubset( env ) already returns a copy, no need for getReadOnlyRasterData
        getRasterData().setSubset( rect.x, rect.y, rect.width, rect.height, src.getRasterData() );
    }

    @Override
    public void setSubset( double x, double y, AbstractRaster source ) {
        // calculate position in RasterData
        int offset[] = getRasterEnvelope().convertToRasterCRS( x, y );
        RasterData sourceRD = source.getAsSimpleRaster().getReadOnlyRasterData();
        getRasterData().setSubset( offset[0], offset[1], sourceRD.getWidth(), sourceRD.getHeight(), sourceRD );
    }

    @Override
    public void setSubset( double x, double y, int dstBand, AbstractRaster source ) {
        // calculate position in RasterData
        int offset[] = getRasterEnvelope().convertToRasterCRS( x, y );
        getRasterData().setSubset( offset[0], offset[1], dstBand, 0, source.getAsSimpleRaster().getReadOnlyRasterData() );
    }

    @Override
    public void setSubset( Envelope env, int dstBand, AbstractRaster source ) {
        // calculate position in RasterData
        RasterRect rect = getRasterEnvelope().convertEnvelopeToRasterCRS( env );

        getRasterData().setSubset( rect.x, rect.y, dstBand, 0, source.getAsSimpleRaster().getReadOnlyRasterData() );
    }

    @Override
    public int getColumns() {
        return getRasterData().getWidth();
    }

    @Override
    public int getRows() {
        return getRasterData().getHeight();
    }

    /**
     * @return the number of bands in this raster.
     */
    public int getBands() {
        return getRasterData().getBands();
    }

    @Override
    public SimpleRaster getAsSimpleRaster() {
        return this;
    }

    @Override
    public String toString() {
        return "SimpleRaster: " + envelopeString();
    }
}
