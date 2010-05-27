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

import java.util.Arrays;
import java.util.List;

import org.deegree.coverage.ResolutionInfo;
import org.deegree.coverage.raster.container.MemoryTileContainer;
import org.deegree.coverage.raster.container.TileContainer;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.slf4j.Logger;

/**
 * This class represents a tiled AbstractRaster.
 * 
 * A TiledRaster contains multiple non-overlapping (TODO verify this) AbstractRasters
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class TiledRaster extends AbstractRaster {

    private static final Logger LOG = getLogger( TiledRaster.class );

    private TileContainer tileContainer;

    /**
     * Creates a new TiledRaster with tiles from the given TileContainer
     * 
     * @param tileContainer
     *            wraps all tiles
     */
    public TiledRaster( TileContainer tileContainer ) {
        super();
        this.tileContainer = tileContainer;
    }

    /**
     * Returns information about the possible sample resolutions of this coverage.
     * 
     * @return information about the possible sample resolutions.
     */
    @Override
    public ResolutionInfo getResolutionInfo() {
        return tileContainer.getResolutionInfo();
    }

    /**
     * Returns the wrapper for all tiles.
     * 
     * @return The container for all tiles.
     */
    public TileContainer getTileContainer() {
        return tileContainer;
    }

    @Override
    public Envelope getEnvelope() {
        return tileContainer.getEnvelope();
    }

    @Override
    public RasterGeoReference getRasterReference() {
        return tileContainer.getRasterReference();
    }

    @Override
    public CRS getCoordinateSystem() {
        return tileContainer.getEnvelope().getCoordinateSystem();
    }

    // TODO: convert to TileContainer
    @Override
    public TiledRaster copy() {
        // TiledRaster result = new TiledRaster();
        // for ( AbstractRaster r : tiles ) {
        // result.addTile( r.copy() );
        // }
        // return result;
        throw new UnsupportedOperationException();
    }

    @Override
    public TiledRaster getSubRaster( Envelope env ) {
        return getSubRaster( env, null );
    }

    @Override
    public TiledRaster getSubRaster( Envelope env, BandType[] bands ) {
        return this.getSubRaster( env, bands, null );
    }

    /**
     * Get a subraster which has it's origin location at the given location.
     * 
     * @param env
     *            of the subraster
     * @param bands
     *            defining the bands of the sub raster
     * @param targetLocation
     *            the new origin's location definition.
     * @return a subraster of the size of the given envelope, having the given bands and the given target location.
     */
    @Override
    public TiledRaster getSubRaster( Envelope env, BandType[] bands, OriginLocation targetLocation ) {
        if ( getEnvelope().equals( env ) && ( bands == null || Arrays.equals( bands, getRasterDataInfo().bandInfo ) ) ) {
            return this;
        }
        // determine the new raster geo reference for the requested envelope.
        RasterGeoReference ref = getRasterReference().createRelocatedReference( targetLocation, env );
        // merging with the existing one, will result in getting the origin right.
        // ref = RasterGeoReference.merger( getRasterReference(), ref );

        // use the default tile container.
        MemoryTileContainer resultTC = new MemoryTileContainer( ref, env, getRasterDataInfo() );
        TiledRaster result = new TiledRaster( resultTC );
        List<AbstractRaster> tiles = getTileContainer().getTiles( env );
        if ( tiles == null || tiles.isEmpty() ) {
            // a tiledraster with no simple rasters should return the a simple raster with no data values. To do this we
            // need the raster data info. If it is present supply the result with it. If not we can only throw an
            // exception.
            if ( getRasterDataInfo() == null ) {
                throw new NullPointerException(
                                                "The given tile container does not contain any tiles and no raster data information is available. " );
            }
            return result;
        }
        for ( AbstractRaster r : tiles ) {
            Envelope rasterEnv = r.getEnvelope();
            Geometry intersection = rasterEnv.getIntersection( env );

            if ( intersection != null ) {
                // rb: don't delete the following tests, they are crucial.
                // ignore if it only touches a tile
                if ( intersection instanceof Point ) {
                    continue;
                }
                if ( intersection instanceof Curve ) {
                    continue;
                }
                Envelope subsetEnv = intersection.getEnvelope();
                resultTC.addTile( r.getSubRaster( subsetEnv, bands ) );
            }
        }

        // if ( resultTC.getRasterReference() == null ) {
        // throw new IndexOutOfBoundsException( "no intersection between TiledRaster and requested subset" );
        // }
        return result;
    }

    @Override
    public void setSubRaster( Envelope envelope, AbstractRaster source ) {
        List<AbstractRaster> interSectingTiles = getTileContainer().getTiles( envelope );
        if ( !interSectingTiles.isEmpty() ) {
            for ( AbstractRaster r : interSectingTiles ) {
                if ( r != null ) {
                    Geometry intersection = r.getEnvelope().getIntersection( envelope );
                    if ( intersection != null ) {
                        Envelope subsetEnv = intersection.getEnvelope();
                        r.setSubRaster( subsetEnv, source );
                    }
                }
            }
        }
    }

    @Override
    public void setSubRaster( double x, double y, AbstractRaster source ) {
        RasterGeoReference srcREnv = source.getRasterReference();
        RasterGeoReference dstREnv = new RasterGeoReference( srcREnv.getOriginLocation(), srcREnv.getResolutionX(),
                                                             srcREnv.getResolutionY(), x, y );
        Envelope dstEnv = dstREnv.getEnvelope( source.getColumns(), source.getRows(), source.getCoordinateSystem() );
        RasterData srcData = source.getAsSimpleRaster().getRasterData();
        SimpleRaster movedRaster = new SimpleRaster( srcData, dstEnv, dstREnv );
        setSubRaster( dstEnv, movedRaster );
    }

    @Override
    public void setSubRaster( double x, double y, int dstBand, AbstractRaster source ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSubRaster( Envelope env, int dstBand, AbstractRaster source ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleRaster getAsSimpleRaster() {

        Envelope env = getEnvelope();
        List<AbstractRaster> tiles = getTileContainer().getTiles( env );
        if ( tiles == null || tiles.isEmpty() ) {
            // a tiledraster with no simple rasters should return the a simple raster with no data values. To do this we
            // need the raster data info. If it is present supply the result with it. If not we can only throw an
            // exception.
            RasterDataInfo dataInfo = getRasterDataInfo();
            if ( dataInfo == null ) {
                throw new NullPointerException(
                                                "The given tile container does not contain any tiles and no raster data information is available. " );
            }
            SimpleRaster result = RasterFactory.createEmptyRaster( dataInfo, env, getRasterReference() );
            return result;
        }
        SimpleRaster originalSimpleRaster = tiles.get( 0 ).getAsSimpleRaster();
        SimpleRaster result = originalSimpleRaster.createCompatibleSimpleRaster( getRasterReference(), env );
        LOG.debug( "Tiled to simple -> result(w,h): " + result.getColumns() + ", " + result.getRows() );

        for ( AbstractRaster r : tiles ) {
            Geometry intersec = r.getEnvelope().getIntersection( env );
            if ( intersec != null ) {
                if ( intersec instanceof Point ) {
                    continue;
                }
                if ( intersec instanceof Curve ) {
                    continue;
                }
                Envelope subsetEnv = intersec.getEnvelope();
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug( "Adding raster intersection:{}, rasterInfo:{}, rasterref: {} ",
                               new Object[] { subsetEnv, r.getRasterDataInfo(), r.getRasterReference() } );
                }

                result.setSubRaster( subsetEnv, r );
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append( "TiledRaster: " + getEnvelope() );
        result.append( "\n\t" );
        result.append( getTileContainer().toString() );
        return result.toString();
    }

    @Override
    public RasterDataInfo getRasterDataInfo() {
        return this.tileContainer.getRasterDataInfo();
    }

}
