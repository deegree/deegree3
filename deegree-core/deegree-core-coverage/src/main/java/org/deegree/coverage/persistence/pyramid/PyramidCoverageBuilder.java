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
package org.deegree.coverage.persistence.pyramid;

import static org.deegree.coverage.raster.io.RasterIOOptions.CRS;
import static org.deegree.coverage.raster.io.RasterIOOptions.IMAGE_INDEX;
import static org.deegree.coverage.raster.io.RasterIOOptions.OPT_FORMAT;
import static org.deegree.coverage.raster.utils.RasterBuilder.setNoDataValue;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;

import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.deegree.coverage.Coverage;
import org.deegree.coverage.persistence.pyramid.jaxb.Pyramid;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.imageio.geotiff.GeoTiffIIOMetadataAdapter;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder for pyramid coverages.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class PyramidCoverageBuilder implements ResourceBuilder<Coverage> {

	private static Logger LOG = LoggerFactory.getLogger(PyramidCoverageBuilder.class);

	private Pyramid config;

	private ResourceMetadata<Coverage> metadata;

	public PyramidCoverageBuilder(ResourceMetadata<Coverage> metadata, Pyramid config) {
		this.metadata = metadata;
		this.config = config;
	}

	@Override
	public Coverage build() {
		try {
			Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix("tiff");
			ImageReader reader = null;
			while (readers.hasNext() && !(reader instanceof TIFFImageReader)) {
				reader = readers.next();
			}

			if (reader == null) {
				throw new ResourceInitException("No TIFF reader was found for imageio.");
			}

			ICRS crs = null;
			if (config.getCRS() != null) {
				crs = CRSManager.getCRSRef(config.getCRS());
			}

			MultiResolutionRaster mrr = new MultiResolutionRaster(metadata);
			String file = config.getPyramidFile();
			ImageInputStream iis = ImageIO.createImageInputStream(metadata.getLocation().resolveToFile(file));
			reader.setInput(iis);
			int num = reader.getNumImages(true);
			if (crs == null) {
				IIOMetadata md = reader.getImageMetadata(0);
				crs = getCRS(md);
			}
			iis.close();

			if (crs == null) {
				throw new ResourceInitException("No CRS information could be read from GeoTIFF, and none was "
						+ " configured. Please configure a CRS or add one to the GeoTIFF.");
			}

			for (int i = 0; i < num; ++i) {
				RasterIOOptions opts = new RasterIOOptions();
				opts.add(IMAGE_INDEX, "" + i);
				opts.add(OPT_FORMAT, "tiff");
				opts.add(CRS, crs.getAlias());
				if (config.getOriginLocation() != null) {
					opts.add(RasterIOOptions.GEO_ORIGIN_LOCATION, config.getOriginLocation().toString().toUpperCase());
				}

				AbstractRaster raster = RasterFactory.loadRasterFromFile(metadata.getLocation().resolveToFile(file),
						opts, metadata);
				setNoDataValue(raster, config.getNodata());
				raster.setCoordinateSystem(crs);
				mrr.addRaster(raster);
			}
			mrr.setCoordinateSystem(crs);
			return mrr;
		}
		catch (Exception e) {
			throw new ResourceInitException("Could not read pyramid configuration file.", e);
		}
	}

	private static ICRS getCRS(IIOMetadata metaData) {
		GeoTiffIIOMetadataAdapter geoTIFFMetaData = new GeoTiffIIOMetadataAdapter(metaData);
		try {
			int modelType = Integer.valueOf(geoTIFFMetaData.getGeoKey(GeoTiffIIOMetadataAdapter.GTModelTypeGeoKey));
			String epsgCode = null;
			if (modelType == GeoTiffIIOMetadataAdapter.ModelTypeProjected) {
				epsgCode = geoTIFFMetaData.getGeoKey(GeoTiffIIOMetadataAdapter.ProjectedCSTypeGeoKey);
			}
			else if (modelType == GeoTiffIIOMetadataAdapter.ModelTypeGeographic) {
				epsgCode = geoTIFFMetaData.getGeoKey(GeoTiffIIOMetadataAdapter.GeographicTypeGeoKey);
			}
			if (epsgCode != null && epsgCode.length() != 0) {
				try {
					return CRSManager.lookup("EPSG:" + epsgCode);
				}
				catch (UnknownCRSException e) {
					LOG.error("No coordinate system found for EPSG:" + epsgCode);
				}
			}
		}
		catch (UnsupportedOperationException ex) {
			LOG.debug("couldn't read crs information in GeoTIFF");
		}
		return null;
	}

}
