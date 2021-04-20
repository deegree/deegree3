/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.layer.persistence.gdal;

import static java.awt.color.ColorSpace.CS_sRGB;
import static java.awt.image.DataBuffer.TYPE_BYTE;
import static org.deegree.cs.components.Axis.AO_EAST;
import static org.deegree.cs.components.Axis.AO_WEST;
import static org.gdal.gdalconst.gdalconstConstants.CE_None;
import static org.gdal.gdalconst.gdalconstConstants.GDT_Byte;
import static org.gdal.osr.CoordinateTransformation.CreateCoordinateTransformation;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.gdal.GdalDataset;
import org.deegree.commons.gdal.GdalDatasetPool;
import org.deegree.commons.gdal.GdalSettings;
import org.deegree.cs.CRSUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.layer.LayerData;
import org.deegree.rendering.r2d.context.RenderContext;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;
import org.slf4j.Logger;

/**
 * {@link LayerData} implementation for layers that are rendered from GDAL datasets.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
class GdalLayerData implements LayerData {

    private static final Logger LOG = getLogger( GdalLayerData.class );

    private final List<File> datasets;

    private final Envelope bbox;

    private final int width;

    private final int height;

    private final GdalSettings gdalSettings;

    GdalLayerData( List<File> datasets, Envelope bbox, int width, int height, GdalSettings gdalSettings ) {
        this.datasets = datasets;
        this.bbox = bbox;
        this.width = width;
        this.height = height;
        this.gdalSettings = gdalSettings;
    }

    @Override
    public void render( RenderContext context ) {
        ICRS nativeCrs = gdalSettings.getDatasetPool().getCrs( datasets.get( 0 ) );
        BufferedImage img = null;
        if ( bbox.getCoordinateSystem().equals( nativeCrs ) ) {
            img = extractRegionFromGdalFiles( bbox );
        } else {
            img = extractAndReprojectRegion( nativeCrs );
        }
        if ( img != null ) {
            context.paintImage( img );
        }
    }

    private BufferedImage extractRegionFromGdalFiles( Envelope bbox ) {
        List<byte[][]> regions = getIntersectingRegionsFromAllDatasets( bbox );
        if ( regions.isEmpty() ) {
            return null;
        }
        byte[][] bytes = compose( regions );
        return toImage( bytes, width, height, true );
    }

    private BufferedImage extractAndReprojectRegion( ICRS nativeCrs ) {
        SpatialReference requestSr = getSpatialReference( bbox.getCoordinateSystem() );
        SpatialReference nativeSr = getSpatialReference( nativeCrs );
        Envelope nativeBbox = transform( bbox, requestSr, nativeSr );
        List<byte[][]> nativeRegions = getIntersectingRegionsFromAllDatasets( nativeBbox );
        if ( nativeRegions.isEmpty() ) {
            return null;
        }
        Dataset nativeRegion = composeMemDataset( nativeBbox, nativeSr.ExportToWkt(), nativeRegions );
        Dataset reprojectedRegion = reproject( nativeRegion, requestSr.ExportToWkt() );
        byte[][] rawImage = readBands( reprojectedRegion );
        nativeRegion.delete();
        reprojectedRegion.delete();
        return toImage( rawImage, width, height, true );
    }

    private Dataset reproject( Dataset src, String dstCrsWkt ) {
        Driver vrtDriver = gdal.GetDriverByName( "MEM" );
        Dataset region = vrtDriver.Create( "/tmp/whatever", width, height, src.getRasterCount() );
        region.SetProjection( dstCrsWkt );
        region.SetGeoTransform( getGeoTransform( bbox, width, height ) );
        gdal.ReprojectImage( src, region );
        return region;
    }

    private Dataset composeMemDataset( Envelope nativeBbox, String nativeProjection, List<byte[][]> nativeRegions ) {
        byte[][] composedRegion = compose( nativeRegions );
        return createMemDataset( nativeBbox, nativeProjection, composedRegion );
    }

    private byte[][] compose( List<byte[][]> regions ) {
        byte[][] composedRegion = regions.get( 0 );
        for ( int i = 1; i < regions.size(); i++ ) {
            byte[][] region = regions.get( i );
            compose( composedRegion, region );
        }
        return composedRegion;
    }

    private void compose( byte[][] composedRegion, byte[][] overlay ) {
        if ( composedRegion.length == 4 && overlay.length == 4 ) {
            byte[] alpha = overlay[3];
            for ( int i = 0; i < 3; i++ ) {
                compose( composedRegion[i], overlay[i], alpha );
            }
        } else if ( composedRegion.length == 3 && overlay.length == 3 ) {
            final int length = composedRegion[0].length;
            for ( int i = 0; i < length; i++ ) {
                if ( composedRegion[0][i] == -1 && composedRegion[1][i] == -1 && composedRegion[2][i] == -1 ) {
                    composedRegion[0][i] = overlay[0][i];
                    composedRegion[1][i] = overlay[1][i];
                    composedRegion[2][i] = overlay[2][i];
                }
            }
        }
    }

    private void compose( byte[] merged, byte[] bytes, byte[] alpha ) {
        if ( alpha != null ) {
            for ( int i = 0; i < merged.length; i++ ) {
                if ( alpha[i] == -1 ) {
                    merged[i] = bytes[i];
                }
            }
        } else {
            for ( int i = 0; i < merged.length; i++ ) {
                merged[i] = bytes[i];
            }
        }
    }

    private Dataset createMemDataset( Envelope nativeBbox, String nativeProjection, byte[][] composedRegion ) {
        Driver vrtDriver = gdal.GetDriverByName( "MEM" );
        Dataset dataset = vrtDriver.Create( "/tmp/whatever", width, height, composedRegion.length );
        dataset.SetProjection( nativeProjection );
        dataset.SetGeoTransform( getGeoTransform( nativeBbox, width, height ) );
        for ( int i = 1; i <= dataset.getRasterCount(); i++ ) {
            Band band = dataset.GetRasterBand( i );
            if ( band.WriteRaster( 0, 0, width, height, width, height, GDT_Byte, composedRegion[i - 1] ) != CE_None ) {
                throw new RuntimeException( "Error writing composed raster." );
            }
        }
        return dataset;
    }

    private double[] getGeoTransform( Envelope bbox, int pixelsX, int pixelsY ) {
        if ( isXy( bbox.getCoordinateSystem() ) ) {
            double originX = bbox.getMin().get0();
            double pixelSizeX = bbox.getSpan0() / pixelsX;
            double originY = bbox.getMax().get1();
            double pixelSizeY = -bbox.getSpan1() / pixelsY;
            return new double[] { originX, pixelSizeX, 0.0, originY, 0.0, pixelSizeY };
        }
        double originX = bbox.getMin().get1();
        double pixelSizeX = bbox.getSpan1() / pixelsX;
        double originY = bbox.getMax().get0();
        double pixelSizeY = -bbox.getSpan0() / pixelsY;
        return new double[] { originX, pixelSizeX, 0.0, originY, 0.0, pixelSizeY };
    }

    private List<byte[][]> getIntersectingRegionsFromAllDatasets( Envelope bbox ) {
        List<byte[][]> regions = new ArrayList<byte[][]>( datasets.size() );
        GdalDatasetPool pool = gdalSettings.getDatasetPool();
        for ( File file : datasets ) {
            if ( bbox.intersects( gdalSettings.getDatasetPool().getEnvelope( file ) ) ) {
                GdalDataset dataset = null;
                try {
                    dataset = pool.borrow( file );
                    regions.add( dataset.extractRegionAsByteArray( bbox, width, height, true ) );
                } catch ( Exception e ) {
                    LOG.error( "Error extracting region from dataset: " + e.getMessage(), e );
                } finally {
                    if ( dataset != null ) {
                        try {
                            pool.returnDataset( dataset );
                        } catch ( Exception e ) {
                            LOG.error( "Error returning dataset to pool: " + e.getMessage() );
                        }
                    }
                }
            }
        }
        return regions;
    }

    private byte[][] readBands( Dataset dataset ) {
        int numBands = dataset.getRasterCount();
        byte[][] bands = new byte[numBands][width * height];
        for ( int i = 0; i < numBands; i++ ) {
            Band band = dataset.GetRasterBand( i + 1 );
            byte[] bandBytes = bands[i];
            band.ReadRaster( 0, 0, width, height, width, height, GDT_Byte, bandBytes, 0, 0 );
        }
        return bands;
    }

    private SpatialReference getSpatialReference( ICRS coordinateSystem ) {
        try {
            int requestEpsgCode = CRSUtils.getEpsgCode( coordinateSystem );
            return gdalSettings.getCrsAsWkt( requestEpsgCode );
        } catch ( IllegalArgumentException e ) {
            boolean isCrs84 = coordinateSystem.hasId( "crs:84", true, true );
            if ( isCrs84 )
                return gdalSettings.getCrs84();
            throw e;
        }
    }

    private Envelope transform( Envelope bbox, SpatialReference requestSr, SpatialReference nativeSr ) {
        double[][] points = getBoundarySamplePointsAndEnsureXyOrder( bbox );
        CoordinateTransformation transform = CreateCoordinateTransformation( requestSr, nativeSr );
        transform.TransformPoints( points );
        transform.delete();
        double[] minXY = new double[] { points[0][0], points[0][1] };
        double[] maxXY = new double[] { points[0][0], points[0][1] };
        for ( double[] point : points ) {
            if ( point[0] < minXY[0] ) {
                minXY[0] = point[0];
            }
            if ( point[1] < minXY[1] ) {
                minXY[1] = point[1];
            }
            if ( point[0] > maxXY[0] ) {
                maxXY[0] = point[0];
            }
            if ( point[1] > maxXY[1] ) {
                maxXY[1] = point[1];
            }
        }
        Point min = new DefaultPoint( null, null, null, minXY );
        Point max = new DefaultPoint( null, null, null, maxXY );
        return new DefaultEnvelope( min, max );
    }

    private double[][] getBoundarySamplePointsAndEnsureXyOrder( Envelope bbox ) {
        double[][] points = new double[4][2];
        if ( isXy( bbox.getCoordinateSystem() ) ) {
            points[0][0] = bbox.getMin().get0();
            points[0][1] = bbox.getMin().get1();
            points[1][0] = bbox.getMax().get0();
            points[1][1] = bbox.getMin().get1();
            points[2][0] = bbox.getMax().get0();
            points[2][1] = bbox.getMax().get1();
            points[3][0] = bbox.getMin().get0();
            points[3][1] = bbox.getMax().get1();
        } else {
            points[0][0] = bbox.getMin().get1();
            points[0][1] = bbox.getMin().get0();
            points[1][0] = bbox.getMax().get1();
            points[1][1] = bbox.getMin().get0();
            points[2][0] = bbox.getMax().get1();
            points[2][1] = bbox.getMax().get0();
            points[3][0] = bbox.getMin().get1();
            points[3][1] = bbox.getMax().get0();
        }
        return points;
    }

    private boolean isXy( final ICRS crs ) {
        if ( crs == null ) {
            return true;
        }
        final int axisOrientation = crs.getAxis()[0].getOrientation();
        return axisOrientation == AO_WEST || axisOrientation == AO_EAST;
    }

    private BufferedImage toImage( byte[][] bands, int xSize, int ySize, boolean removeTransparency ) {
        int numBands = bands.length;
        if ( removeTransparency && bands.length == 4 ) {
            byte[][] bands2 = new byte[3][];
            bands2[0] = bands[0];
            bands2[1] = bands[1];
            bands2[2] = bands[2];
            bands = bands2;
        }
        int numBytes = xSize * ySize * numBands;
        DataBuffer imgBuffer = new DataBufferByte( bands, numBytes );
        SampleModel sampleModel = new BandedSampleModel( TYPE_BYTE, xSize, ySize, bands.length );
        WritableRaster raster = Raster.createWritableRaster( sampleModel, imgBuffer, null );
        ColorSpace cs = ColorSpace.getInstance( CS_sRGB );
        ColorModel cm;
        if ( bands.length == 3 ) {
            cm = new ComponentColorModel( cs, false, false, ColorModel.OPAQUE, TYPE_BYTE );
        } else if ( bands.length == 4 ) {
            cm = new ComponentColorModel( cs, true, false, ColorModel.TRANSLUCENT, TYPE_BYTE );
        } else {
            throw new IllegalArgumentException( "Unsupported number of bands: " + bands.length );
        }
        return new BufferedImage( cm, raster, false, null );
    }

    @Override
    public FeatureCollection info() {
        throw new UnsupportedOperationException();
    }

}
