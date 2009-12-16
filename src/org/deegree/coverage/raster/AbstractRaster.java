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

import org.deegree.coverage.AbstractCoverage;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.geometry.Envelope;

/**
 * This class represents an abstract grid coverage.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractRaster extends AbstractCoverage {

    private RasterGeoReference rasterReference = null;

    /**
     * Instantiate an AbstractRaster with no envelope.
     */
    protected AbstractRaster() {
        super();
    }

    /**
     * Instantiate an AbstractRaster with given envelope.
     * 
     * @param envelope
     *            The envelope of the raster.
     */
    protected AbstractRaster( Envelope envelope ) {
        super( envelope );
    }

    /**
     * Instantiate an AbstractRaster with given envelope and raster envelope.
     * 
     * @param envelope
     *            The envelope of the raster.
     * @param rasterReference
     *            The raster reference of the underlying raster.
     */
    protected AbstractRaster( Envelope envelope, RasterGeoReference rasterReference ) {
        super( envelope );
        this.rasterReference = rasterReference;
    }

    /**
     * Returns columns of the raster.
     * 
     * @return width in pixel
     */
    public int getColumns() {
        return getRasterReference().getSize( getEnvelope() )[0];
    }

    /**
     * Returns rows of the raster.
     * 
     * @return height in pixel
     */
    public int getRows() {
        return getRasterReference().getSize( getEnvelope() )[1];
    }

    /**
     * Extends current RasterReference with rasterReference. Useful for extending the raster, e.g. adding a tile.
     * 
     * @param rasterReference
     *            The raster envelope to add to the current.
     */
    protected void extendRasterReference( RasterGeoReference rasterReference ) {
        if ( this.rasterReference == null ) {
            this.rasterReference = rasterReference;
        } else {
            this.rasterReference = RasterGeoReference.merger( this.rasterReference, rasterReference );
        }
    }

    /**
     * Checks if the coverage contains the <code>envelope</code>.
     * 
     * @param envelope
     *            The envelope to check for.
     */
    protected void checkBounds( Envelope envelope ) {
        // rb: following checks are unnecessary because the creation of envelopes should check it.
        // assert ( envelope.getMin().getX() < envelope.getMax().getX() );
        // assert ( envelope.getMin().getY() < envelope.getMax().getY() );
        // assert ( getEnvelope().getMin().getX() < getEnvelope().getMax().getX() );
        // assert ( getEnvelope().getMin().getY() < getEnvelope().getMax().getY() );
        if ( !getEnvelope().contains( envelope ) ) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Creates a copy of the raster with all the data.
     * 
     * @return A copy of the raster.
     */
    public abstract AbstractRaster copy();

    /**
     * Returns a subset of the raster, note this is a view on the given raster.
     * 
     * @param env
     *            envelope of the subset
     * @return subset of the raster
     */
    public abstract AbstractRaster getSubRaster( Envelope env );

    /**
     * Returns a subset of the raster, note this is a view on the given raster.
     * 
     * @param env
     *            envelope of the subset
     * @param bands
     *            to use for the given subset.
     * @return subset of the raster
     */
    public abstract AbstractRaster getSubRaster( Envelope env, BandType[] bands );

    /**
     * Returns a subset of the raster.
     * 
     * @param x
     *            left boundary
     * @param y
     *            upper boundary
     * @param x2
     *            right boundary
     * @param y2
     *            lower boundary
     * @return subset of the raster
     */
    public AbstractRaster getSubRaster( double x, double y, double x2, double y2 ) {
        // what about the precision model? Formerly: getRasterReference().getDelta() was used
        Envelope env = getGeometryFactory().createEnvelope( new double[] { x, y }, new double[] { x2, y2 }, null );
        return getSubRaster( env );
    }

    /**
     * Sets the raster with data from source. Source must overlap the raster (within the envelope).
     * 
     * @param source
     *            data to copy
     * @param env
     *            Envelope with the destination area
     */
    public abstract void setSubRaster( Envelope env, AbstractRaster source );

    /**
     * Sets the raster with data from source.
     * 
     * @param x
     *            left boundary
     * @param y
     *            upper boundary
     * @param source
     *            data to copy
     */
    public abstract void setSubRaster( double x, double y, AbstractRaster source );

    /**
     * Sets a single band with data from source.
     * 
     * Copies the first band of source into dstBand.
     * 
     * @param x
     *            left boundary
     * @param y
     *            upper boundary
     * @param dstBand
     *            selected destination band
     * @param source
     *            data to copy
     */
    public abstract void setSubRaster( double x, double y, int dstBand, AbstractRaster source );

    /**
     * Sets a single band with data from source.
     * 
     * @param env
     *            destination area
     * @param dstBand
     *            selected destination band
     * @param source
     *            data to copy
     */
    public abstract void setSubRaster( Envelope env, int dstBand, AbstractRaster source );

    /**
     * Returns the AbstractRaster as a SimpleRaster. The data gets cropped (TiledRaster) or merged (MultiRange) if
     * necessary.
     * 
     * @return The raster data as SimpleRaster
     */
    public abstract SimpleRaster getAsSimpleRaster();

    @Override
    public String toString() {
        return "AbstractRaster: " + envelopeString();
    }

    // TODO remove
    /**
     * @return string representation of the envelope
     */
    protected String envelopeString() {
        String epsg = "";
        if ( getEnvelope().getCoordinateSystem() != null ) {
            epsg = getEnvelope().getCoordinateSystem().getName(); // added .toString() since the code retrieved was a
            // CRSCodeType
        }
        return epsg + " " + getEnvelope().getMin().get0() + " " + getEnvelope().getMin().get1() + " "
               + getEnvelope().getMax().get0() + " " + getEnvelope().getMax().get1();
    }

    /**
     * @return The raster reference of this raster.
     */
    public RasterGeoReference getRasterReference() {
        return rasterReference;
    }

    /**
     * Returns available information on the raster data. This method should be called with care, it may under
     * circumstances, cause massive loading of raster data.
     * 
     * @return available information on the raster data.
     */
    public abstract RasterDataInfo getRasterDataInfo();

    /**
     * @return true if the given raster is a simple raster, false otherwise.
     */
    public boolean isSimpleRaster() {
        return false;
    }

}
