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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.geometry.Envelope;

/**
 * This class represents an AbstractRaster with multiple ranges.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class MultiRangedRaster extends AbstractRaster {

    private List<AbstractRaster> multiRange = null;

    /**
     * Create an empty MultiRangedRaster.
     */
    public MultiRangedRaster() {
        super();
        multiRange = new ArrayList<AbstractRaster>();
    }

    @Override
    public MultiRangedRaster copy() {
        MultiRangedRaster result = new MultiRangedRaster();
        for ( AbstractRaster r : multiRange ) {
            result.addRaster( r.copy() );
        }
        return result;
    }

    /**
     * Adds an AbstractRaster to the MultiRangedRaster
     * 
     * @param raster
     */
    public void addRaster( AbstractRaster raster ) {
        extendEnvelope( raster.getEnvelope() );
        extendRasterReference( raster.getRasterReference() );
        multiRange.add( raster );
    }

    /**
     * Returns a single range with given index
     * 
     * @param index
     *            index of range
     * @return selected range as AbstractRaster
     */
    public AbstractRaster getRange( int index ) {
        return multiRange.get( index );
    }

    /**
     * Returns a new MultiRangedRaster with selected indices.
     * 
     * @param indices
     *            selected ranges
     * @return new MultiRangeRaster
     */
    public MultiRangedRaster getRanges( int... indices ) {
        MultiRangedRaster result = new MultiRangedRaster();
        for ( int index : indices ) {
            result.addRaster( multiRange.get( index ) );
        }
        return result;
    }

    @Override
    public MultiRangedRaster getSubRaster( Envelope env ) {
        return getSubRaster( env, null );
    }

    @Override
    public MultiRangedRaster getSubRaster( Envelope env, BandType[] bands ) {
        return getSubRaster( env, bands, null );
    }

    @Override
    public MultiRangedRaster getSubRaster( Envelope env, BandType[] bands, OriginLocation targetLocation ) {
        if ( getEnvelope().equals( env ) && ( bands == null || Arrays.equals( bands, getRasterDataInfo().bandInfo ) ) ) {
            return this;
        }
        checkBounds( env );
        MultiRangedRaster result = new MultiRangedRaster();
        for ( AbstractRaster raster : multiRange ) {
            result.addRaster( raster.getSubRaster( env, bands, targetLocation ) );
        }
        return result;
    }

    @Override
    public MultiRangedRaster getSubRaster( double x, double y, double x2, double y2 ) {
        // what about the precision model? Formerly: getRasterReference().getDelta() was used
        Envelope env = getGeometryFactory().createEnvelope( new double[] { x, y }, new double[] { x2, y2 }, null );
        return getSubRaster( env );
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
    public void setSubRaster( double x, double y, AbstractRaster source ) {
        // checkBounds(x, y, source.getColumns(), source.getRows());
        SimpleRaster src = source.getAsSimpleRaster();
        if ( src.getBands() != getNumberOfRanges() ) {
            throw new IndexOutOfBoundsException();
        }
        for ( int i = 0; i < getNumberOfRanges(); i++ ) {
            SimpleRaster raster = multiRange.get( i ).getAsSimpleRaster();
            raster.setSubRaster( x, y, src.getBand( i ).getAsSimpleRaster() );
            multiRange.set( i, raster );
        }
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
    public void setSubRaster( double x, double y, int index, AbstractRaster source ) {
        // checkBounds(x, y, source.getColumns(), source.getRows());
        if ( index >= getNumberOfRanges() ) {
            throw new IndexOutOfBoundsException();
        }
        SimpleRaster raster = multiRange.get( index ).getAsSimpleRaster();
        raster.setSubRaster( x, y, source );
        multiRange.set( index, raster );
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
    public void setSubset( double x, double y, MultiRangedRaster source ) {
        // checkBounds(x, y, source.getColumns(), source.getRows());
        if ( source.getNumberOfRanges() != getNumberOfRanges() ) {
            throw new IndexOutOfBoundsException();
        }
        for ( int i = 0; i < getNumberOfRanges(); i++ ) {
            SimpleRaster raster = multiRange.get( i ).getAsSimpleRaster();
            raster.setSubRaster( x, y, source.getRange( i ).getAsSimpleRaster() );
            multiRange.set( i, raster );
        }
    }

    @Override
    public void setSubRaster( Envelope env, AbstractRaster source ) {
        AbstractRaster subset = source.getSubRaster( env );
        double[] pos = subset.getRasterReference().getOrigin();
        setSubRaster( pos[0], pos[1], source );
    }

    @Override
    public void setSubRaster( Envelope env, int dstBand, AbstractRaster source ) {
        AbstractRaster subset = source.getSubRaster( env );
        double[] pos = subset.getRasterReference().getOrigin();
        setSubRaster( pos[0], pos[1], dstBand, source );
    }

    @Override
    public SimpleRaster getAsSimpleRaster() {
        int i = 0;
        SimpleRaster raster = multiRange.get( i ).getAsSimpleRaster();

        SimpleRaster result = raster.createCompatibleSimpleRaster( BandType.fromBufferedImageType( 0, multiRange.size(), null ) );

        result.setSubRaster( getEnvelope(), i, raster );
        for ( i = 1; i < multiRange.size(); i++ ) {
            raster = multiRange.get( i ).getAsSimpleRaster();
            result.setSubRaster( getEnvelope(), i, raster );
        }
        return result;
    }

    /**
     * Returns the number of ranges
     * 
     * @return number of ranges
     */
    public int getNumberOfRanges() {
        return multiRange.size();
    }

    @Override
    public RasterDataInfo getRasterDataInfo() {
        SimpleRaster raster = multiRange.get( 0 ).getAsSimpleRaster();
        if ( raster != null ) {
            return raster.getRasterDataInfo();
        }
        return null;
    }

}
