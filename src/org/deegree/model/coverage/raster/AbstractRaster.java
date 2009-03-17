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

import org.deegree.model.coverage.AbstractCoverage;
import org.deegree.model.coverage.raster.geom.RasterEnvelope;
import org.deegree.model.geometry.Envelope;

/**
 * This class represents an abstract grid coverage.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractRaster extends AbstractCoverage {

    private RasterEnvelope rasterEnv = null;

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
     * @param rasterEnv
     *            The raster envelope of the raster.
     */
    protected AbstractRaster( Envelope envelope, RasterEnvelope rasterEnv ) {
        super( envelope );
        this.rasterEnv = rasterEnv;
    }

    /**
     * Returns columns of the raster.
     * 
     * @return width in pixel
     */
    public int getColumns() {
        return getRasterEnvelope().getSize( getEnvelope() )[0];
    }

    /**
     * Returns rows of the raster.
     * 
     * @return height in pixel
     */
    public int getRows() {
        return getRasterEnvelope().getSize( getEnvelope() )[1];
    }

    /**
     * Extends current RasterEnvelope with rasterEnv. Useful for extending the raster, e.g. adding a tile.
     * 
     * @param rasterEnv
     *            The raster envelope to add to the current.
     */
    protected void extendRasterEnvelope( RasterEnvelope rasterEnv ) {
        if ( this.rasterEnv == null ) {
            this.rasterEnv = rasterEnv;
        } else {
            this.rasterEnv = this.rasterEnv.merger( rasterEnv );
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
     * Returns a subset of the raster.
     * 
     * @param env
     *            envelope of the subset
     * @return subset of the raster
     */
    public abstract AbstractRaster getSubRaster( Envelope env );

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
        Envelope env = getGeometryFactory().createEnvelope( new double[] { x, y }, new double[] { x2, y2 },
                                                            getRasterEnvelope().getDelta(), null );
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
            epsg = getEnvelope().getCoordinateSystem().getIdentifier();
        }
        return epsg + " " + getEnvelope().getMin().getX() + " " + getEnvelope().getMin().getY() + " "
               + getEnvelope().getMax().getX() + " " + getEnvelope().getMax().getY();
    }

    /**
     * @return The raster envelope of the raster.
     */
    public RasterEnvelope getRasterEnvelope() {
        return rasterEnv;
    }

}