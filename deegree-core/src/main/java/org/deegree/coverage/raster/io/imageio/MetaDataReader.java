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
package org.deegree.coverage.raster.io.imageio;

import javax.imageio.metadata.IIOMetadata;

import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.imageio.geotiff.GeoTiffIIOMetadataAdapter;
import org.deegree.cs.CRS;
import org.deegree.cs.CRSRegistry;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class MetaDataReader {

    private static final String TIFF_MD_FORMAT = "com_sun_media_imageio_plugins_tiff_image_1.0";

    private IIOMetadata metaData;

    private RasterGeoReference rasterReference = null;

    private CoordinateSystem crs = null;

    private static Logger LOG = LoggerFactory.getLogger( MetaDataReader.class );

    /**
     * @param metaData
     *            a ImageIO meta data object
     * @param definedRasterOrigLoc
     */
    public MetaDataReader( IIOMetadata metaData, OriginLocation definedRasterOrigLoc ) {
        this.metaData = metaData;
        if ( metaData != null ) {
            init( definedRasterOrigLoc );
        }
    }

    /**
     * @return the raster envelope or <code>null</code>, if the metadata contains no georeference
     */
    public RasterGeoReference getRasterReference() {
        return rasterReference;
    }

    /**
     * @return the coordinate system or <code>null</code>, if the metadata contains no crs
     */
    public CoordinateSystem getCRS() {
        return crs;
    }

    private void init( OriginLocation definedRasterOrigLoc ) {
        if ( metaData.getNativeMetadataFormatName().equals( TIFF_MD_FORMAT ) ) {
            initGeoTIFF( definedRasterOrigLoc );
        }

    }

    // read GeoTIFF metadata
    private void initGeoTIFF( OriginLocation definedRasterOrigLoc ) {
        GeoTiffIIOMetadataAdapter geoTIFFMetaData = new GeoTiffIIOMetadataAdapter( metaData );

        try {
            int modelType = Integer.valueOf( geoTIFFMetaData.getGeoKey( GeoTiffIIOMetadataAdapter.GTModelTypeGeoKey ) );
            String epsgCode = null;
            if ( modelType == GeoTiffIIOMetadataAdapter.ModelTypeProjected ) {
                epsgCode = geoTIFFMetaData.getGeoKey( GeoTiffIIOMetadataAdapter.ProjectedCSTypeGeoKey );
            } else if ( modelType == GeoTiffIIOMetadataAdapter.ModelTypeGeographic ) {
                epsgCode = geoTIFFMetaData.getGeoKey( GeoTiffIIOMetadataAdapter.GeographicTypeGeoKey );
            }
            if ( epsgCode != null && epsgCode.length() != 0 ) {
                try {
                    crs = CRSRegistry.lookup( "EPSG:" + epsgCode );
                } catch ( UnknownCRSException e ) {
                    LOG.error( "No coordinate system found for EPSG:" + epsgCode );
                }
            }
        } catch ( UnsupportedOperationException ex ) {
            LOG.debug( "couldn't read crs information in GeoTIFF" );
        }

        try {
            double[] tiePoints = geoTIFFMetaData.getModelTiePoints();
            double[] scale = geoTIFFMetaData.getModelPixelScales();
            if ( tiePoints != null && scale != null ) {

                if ( definedRasterOrigLoc != null ) {
                    rasterReference = new RasterGeoReference( definedRasterOrigLoc, scale[0], -scale[1], tiePoints[3],
                                                              tiePoints[4], new CRS( crs ) );
                } else {
                    if ( Math.abs( scale[0] - 0.5 ) < 0.001 ) { // when first pixel tie point is 0.5 -> center type
                        // rb: this might not always be right, see examples at
                        // http://www.remotesensing.org/geotiff/spec/geotiff3.html#3.2.1.
                        // search for PixelIsArea/PixelIsPoint to determine center/outer
                        rasterReference = new RasterGeoReference( RasterGeoReference.OriginLocation.CENTER, scale[0],
                                                                  -scale[1], tiePoints[3], tiePoints[4], new CRS( crs ) );
                    } else {
                        rasterReference = new RasterGeoReference( RasterGeoReference.OriginLocation.OUTER, scale[0],
                                                                  -scale[1], tiePoints[3], tiePoints[4], new CRS( crs ) );
                    }
                }
            }
        } catch ( UnsupportedOperationException ex ) {
            LOG.debug( "couldn't read georeference information in GeoTIFF" );
        }

        if ( LOG.isDebugEnabled() ) {
            for ( String format : metaData.getMetadataFormatNames() ) {
                // IIOMetadataNode elem = (IIOMetadataNode) metaData.getAsTree( format );
                LOG.debug( "metadata format: " + format );
                LOG.debug( "TBD output the xml file here." );
                // LogUtils.writeTempFile( LOG, "geotiff", ".xml", new XMLFragment( elem ).toString() );
            }
        }
    }
}
