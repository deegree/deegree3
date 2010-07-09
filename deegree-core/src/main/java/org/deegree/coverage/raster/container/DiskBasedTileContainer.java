//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.coverage.raster.container;

import static org.deegree.coverage.raster.utils.RasterFactory.loadRasterFromFile;
import static org.deegree.geometry.utils.GeometryUtils.createEnvelope;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.deegree.commons.index.QTree;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.coverage.ResolutionInfo;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterBuilder.QTreeInfo;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(debug = "logs when raster files could not be loaded", trace = "logs stack traces")
public class DiskBasedTileContainer implements TileContainer {

    private static final Logger LOG = getLogger( DiskBasedTileContainer.class );

    private QTree<File> qtree;

    private Envelope envelope;

    private RasterDataInfo rasterDataInfo;

    private RasterGeoReference rasterGeoReference;

    private ResolutionInfo resolutionInfo;

    private HashMap<File, SoftReference<AbstractRaster>> cache = new HashMap<File, SoftReference<AbstractRaster>>();

    private RasterIOOptions options;

    private boolean initialized = false;

    /**
     * @param info
     * @param files
     * @param rasters
     * @param options
     */
    public DiskBasedTileContainer( QTreeInfo info, List<File> files, List<AbstractRaster> rasters,
                                   RasterIOOptions options ) {
        qtree = new QTree<File>( createEnvelope( info.envelope ), info.numberOfObjects );
        envelope = info.envelope;
        AbstractRaster raster = rasters.iterator().next();
        rasterDataInfo = raster.getRasterDataInfo();
        rasterGeoReference = info.rasterGeoReference;
        resolutionInfo = raster.getResolutionInfo();
        this.options = options;
        Iterator<File> iter = files.iterator();
        for ( AbstractRaster r : rasters ) {
            File f = iter.next();
            qtree.insert( createEnvelope( r.getEnvelope() ), f );
            cache.put( f, new SoftReference<AbstractRaster>( r ) );
        }
        initialized = true;
    }

    /**
     * @param file
     */
    public DiskBasedTileContainer( File file ) {
        ObjectInputStream in = null;
        LOG.debug( "Reading index file for directory..." );
        try {
            in = new ObjectInputStream( new BufferedInputStream( new FileInputStream( file ) ) );
            qtree = (QTree<File>) in.readObject();
            envelope = createEnvelope( (float[]) in.readObject(), null );
            rasterDataInfo = (RasterDataInfo) in.readObject();

            rasterGeoReference = new RasterGeoReference( (OriginLocation) in.readObject(), in.readDouble(),
                                                         in.readDouble(), in.readDouble(), in.readDouble(),
                                                         in.readDouble(), in.readDouble(), (CRS) in.readObject() );

            envelope.setCoordinateSystem( rasterGeoReference.getCrs() );
            resolutionInfo = (ResolutionInfo) in.readObject();
            options = (RasterIOOptions) in.readObject();
            initialized = true;
            LOG.debug( "Done." );
        } catch ( FileNotFoundException e ) {
            LOG.debug( "Raster pyramid file '{}' could not be found.", file );
            LOG.trace( "Stack trace:", e );
        } catch ( IOException e ) {
            LOG.debug( "Raster pyramid file '{}' could not be read: '{}'", file, e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( ClassNotFoundException e ) {
            LOG.debug( "Raster pyramid file '{}' was in the wrong format.", file );
            LOG.trace( "Stack trace:", e );
        } finally {
            if ( in != null ) {
                try {
                    in.close();
                } catch ( IOException e ) {
                    LOG.debug( "Raster pyramid file '{}' could not be closed: '{}'.", file, e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public RasterDataInfo getRasterDataInfo() {
        return rasterDataInfo;
    }

    public RasterGeoReference getRasterReference() {
        return rasterGeoReference;
    }

    public ResolutionInfo getResolutionInfo() {
        return resolutionInfo;
    }

    /**
     * @return false, if loading from file failed
     */
    public boolean isInitialized() {
        return initialized;
    }

    public List<AbstractRaster> getTiles( Envelope env ) {
        List<File> files = qtree.query( createEnvelope( env ) );
        List<AbstractRaster> result = new ArrayList<AbstractRaster>( files.size() );

        for ( File f : files ) {
            SoftReference<AbstractRaster> ref = cache.get( f );
            AbstractRaster raster = ref == null ? null : ref.get();
            if ( raster != null ) {
                result.add( raster );
            } else {
                try {
                    result.add( raster = loadRasterFromFile( f, options ) );
                    cache.put( f, new SoftReference<AbstractRaster>( raster ) );
                } catch ( IOException e ) {
                    LOG.debug( "Raster file '{}' could not be loaded: '{}'.", f, e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }

        return result;
    }

    /**
     * @param file
     */
    public void export( File file ) {
        LOG.debug( "Writing index file for directory..." );
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream( new BufferedOutputStream( new FileOutputStream( file ) ) );
            out.writeObject( qtree );
            out.writeObject( createEnvelope( envelope ) );
            out.writeObject( rasterDataInfo );
            out.writeObject( rasterGeoReference.getOriginLocation() );
            out.writeDouble( rasterGeoReference.getResolutionX() );
            out.writeDouble( rasterGeoReference.getResolutionY() );
            out.writeDouble( rasterGeoReference.getRotationX() );
            out.writeDouble( rasterGeoReference.getRotationY() );
            out.writeDouble( rasterGeoReference.getOriginEasting() );
            out.writeDouble( rasterGeoReference.getOriginNorthing() );
            out.writeObject( rasterGeoReference.getCrs() );
            out.writeObject( resolutionInfo );
            out.writeObject( options );
            LOG.debug( "Done." );
        } catch ( IOException e ) {
            LOG.debug( "Raster pyramid file '{}' could not be written: '{}'.", file, e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } finally {
            if ( out != null ) {
                try {
                    out.close();
                } catch ( IOException e ) {
                    LOG.debug( "Raster pyramid file '{}' could not be closed: '{}'.", file, e.getLocalizedMessage() );
                    LOG.trace( "Stack trace:", e );
                }
            }
        }
    }

}
