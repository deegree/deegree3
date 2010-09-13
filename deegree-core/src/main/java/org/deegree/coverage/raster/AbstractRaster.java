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

import static org.slf4j.LoggerFactory.getLogger;

import org.deegree.coverage.AbstractCoverage;
import org.deegree.coverage.ResolutionInfo;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.coverage.raster.interpolation.RasterInterpolater;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.slf4j.Logger;

/**
 * This class represents an abstract grid coverage.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractRaster extends AbstractCoverage {
    private static final Logger LOG = getLogger( AbstractRaster.class );

    private RasterGeoReference rasterReference = null;

    private ResolutionInfo resolutionInfo;

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
        SampleResolution res = new SampleResolution( new double[] { rasterReference.getResolutionX(),
                                                                   rasterReference.getResolutionY() } );
        this.resolutionInfo = new ResolutionInfo( res );
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
        // assert ( envelope.getMin().x < envelope.getMax().x );
        // assert ( envelope.getMin().y < envelope.getMax().y );
        // assert ( getEnvelope().getMin().x < getEnvelope().getMax().x );
        // assert ( getEnvelope().getMin().y < getEnvelope().getMax().y );
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
     * @param subsetEnv
     *            envelope of the sub raster, may not be <code>null</code>
     * @return subset of the raster
     */
    public abstract AbstractRaster getSubRaster( Envelope subsetEnv );

    /**
     * Returns a subset of the raster, note this is a view on the given raster.
     * 
     * @param subsetEnv
     *            envelope of the sub raster, may not be <code>null</code>
     * @param bands
     *            to use for the given sub raster, if <code>null</code> the bands of the instance shall be used.
     * @return subset of the raster
     */
    public abstract AbstractRaster getSubRaster( Envelope subsetEnv, BandType[] bands );

    /**
     * Returns a subset of the raster, note this is a view on the given raster.
     * 
     * @param subsetEnv
     *            envelope of the sub raster
     * @param bands
     *            to use for the given sub raster, if <code>null</code> the bands of the instance shall be used.
     * @param targetOrigin
     *            the origin location of the target sub raster, if <code>null</code> the origin location of the instance
     *            shall be used.
     * @return subset of the raster
     */
    public abstract AbstractRaster getSubRaster( Envelope subsetEnv, BandType[] bands, OriginLocation targetOrigin );

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

    @Override
    public AbstractRaster getAsRaster( Envelope spatialExtent, SampleResolution resolution,
                                       InterpolationType interpolation ) {
        if ( spatialExtent == null ) {
            return null;
        }
        // Try the incoming envelope
        CRS targetCRS = spatialExtent.getCoordinateSystem();
        if ( this.getCoordinateSystem() == null ) {
            // take this crs
            targetCRS = getCoordinateSystem();
        }

        if ( targetCRS == null || targetCRS.equals( this.getCoordinateSystem() ) ) {
            if ( getResolutionInfo().getNativeResolutions().get( 0 ).equals( resolution ) ) {
                // same resolution and same crs, we return the sub raster
                return getSubRaster( spatialExtent );
            }
            RasterGeoReference nGeoRef = resolution.createGeoReference( getRasterReference().getOriginLocation(),
                                                                        spatialExtent );
            RasterRect rect = nGeoRef.convertEnvelopeToRasterCRS( spatialExtent );

            // same crs (no crs) but different resolution we must interpolate.
            RasterInterpolater interpolater = new RasterInterpolater( interpolation );
            return interpolater.interPolate( this.getSubRaster( spatialExtent ), rect.width, rect.height );
        }
        RasterRect rect = null;
        try {
            GeometryTransformer gt = new GeometryTransformer( getCoordinateSystem().getWrappedCRS() );
            Envelope inLocalCRS = gt.transform( spatialExtent ).getEnvelope();
            RasterGeoReference nGeoRef = resolution.createGeoReference( getRasterReference().getOriginLocation(),
                                                                        inLocalCRS );
            rect = nGeoRef.convertEnvelopeToRasterCRS( inLocalCRS );

        } catch ( IllegalArgumentException e1 ) {
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "(Stack) Exception occurred: " + e1.getLocalizedMessage(), e1 );
            } else {
                LOG.error( "Exception occurred: " + e1.getLocalizedMessage() );
            }
        } catch ( UnknownCRSException e1 ) {
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "(Stack) Exception occurred: " + e1.getLocalizedMessage(), e1 );
            } else {
                LOG.error( "Exception occurred: " + e1.getLocalizedMessage() );
            }
        } catch ( TransformationException e ) {
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "(Stack) Exception occurred: " + e.getLocalizedMessage(), e );
            } else {
                LOG.error( "Exception occurred: " + e.getLocalizedMessage() );
            }
        }

        if ( rect == null ) {
            LOG.warn( "Unable to determine new raster size of requested Envelope: " + spatialExtent
                      + " at resolution: " + resolution );
            return null;
        }

        // a different crs, we must transform to the requested crs.
        AbstractRaster result = null;
        try {
            RasterTransformer transformer = new RasterTransformer( targetCRS );
            result = transformer.transform( this, spatialExtent, rect.width, rect.height, interpolation );
        } catch ( TransformationException e ) {
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "(Stack) Exception occurred: " + e.getLocalizedMessage(), e );
            } else {
                LOG.error( "Exception occurred: " + e.getLocalizedMessage() );
            }
        } catch ( UnknownCRSException e ) {
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "(Stack) Exception occurred: " + e.getLocalizedMessage(), e );
            } else {
                LOG.error( "Exception occurred: " + e.getLocalizedMessage() );
            }
        }
        return result;

    }

    /**
     * Returns information about the possible sample resolutions of this coverage.
     * 
     * @return information about the possible sample resolutions.
     */
    public ResolutionInfo getResolutionInfo() {
        return this.resolutionInfo;
    }

}
