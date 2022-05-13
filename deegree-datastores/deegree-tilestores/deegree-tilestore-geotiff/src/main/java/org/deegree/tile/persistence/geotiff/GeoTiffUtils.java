//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.geotiff;

import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.CENTER;
import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.OUTER;
import static org.slf4j.LoggerFactory.getLogger;

import javax.imageio.metadata.IIOMetadata;

import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.io.imageio.geotiff.GeoTiffIIOMetadataAdapter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.geometry.Envelope;
import org.deegree.workspace.ResourceInitException;
import org.slf4j.Logger;

/**
 * Methods to extract envelope/crs info from imageio metadata objects (GeoTIFF tags).
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class GeoTiffUtils {

    private static final Logger LOG = getLogger( GeoTiffUtils.class );

    public static Envelope getEnvelopeAndCrs( IIOMetadata metaData, int width, int height, ICRS crs ) {
        GeoTiffIIOMetadataAdapter geoTIFFMetaData = new GeoTiffIIOMetadataAdapter( metaData );
        try {
            if ( crs == null ) {
                int modelType = Integer.valueOf( geoTIFFMetaData.getGeoKey( GeoTiffIIOMetadataAdapter.GTModelTypeGeoKey ) );
                String epsgCode = null;
                if ( modelType == GeoTiffIIOMetadataAdapter.ModelTypeProjected ) {
                    epsgCode = geoTIFFMetaData.getGeoKey( GeoTiffIIOMetadataAdapter.ProjectedCSTypeGeoKey );
                } else if ( modelType == GeoTiffIIOMetadataAdapter.ModelTypeGeographic ) {
                    epsgCode = geoTIFFMetaData.getGeoKey( GeoTiffIIOMetadataAdapter.GeographicTypeGeoKey );
                }
                if ( epsgCode != null && epsgCode.length() != 0 ) {
                    try {
                        CRSRef ref = CRSManager.getCRSRef( "EPSG:" + epsgCode );
                        ref.getReferencedObject();
                        crs = ref;
                    } catch ( ReferenceResolvingException e ) {
                        LOG.error( "No coordinate system found for EPSG:" + epsgCode );
                    }
                }
            }
            if ( crs == null ) {
                throw new ResourceInitException( "No CRS information could be read from GeoTIFF, "
                                                 + "and none was configured. Please configure a"
                                                 + " CRS or add one to the GeoTIFF." );
            }

            return getEnvelope( metaData, width, height, crs );
        } catch ( UnsupportedOperationException ex ) {
            LOG.debug( "couldn't read crs information in GeoTIFF" );
        }
        return null;
    }

    public static Envelope getEnvelope( IIOMetadata metaData, int width, int height, ICRS crs ) {
        GeoTiffIIOMetadataAdapter geoTIFFMetaData = new GeoTiffIIOMetadataAdapter( metaData );
        try {
            double[] tiePoints = geoTIFFMetaData.getModelTiePoints();
            double[] scale = geoTIFFMetaData.getModelPixelScales();
            if ( tiePoints != null && scale != null ) {

                RasterGeoReference rasterReference;
                if ( Math.abs( scale[0] - 0.5 ) < 0.001 ) { // when first pixel tie point is 0.5 -> center type
                    // rb: this might not always be right, see examples at
                    // http://www.remotesensing.org/geotiff/spec/geotiff3.html#3.2.1.
                    // search for PixelIsArea/PixelIsPoint to determine center/outer
                    rasterReference = new RasterGeoReference( CENTER, scale[0], -scale[1], tiePoints[3], tiePoints[4],
                                                              crs );
                } else {
                    rasterReference = new RasterGeoReference( OUTER, scale[0], -scale[1], tiePoints[3], tiePoints[4],
                                                              crs );
                }
                return rasterReference.getEnvelope( OUTER, width, height, crs );
            }

        } catch ( UnsupportedOperationException ex ) {
            LOG.debug( "couldn't read crs information in GeoTIFF" );
        }
        return null;
    }

}
