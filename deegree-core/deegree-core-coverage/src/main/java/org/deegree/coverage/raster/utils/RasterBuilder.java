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

package org.deegree.coverage.raster.utils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.AbstractCoverage;
import org.deegree.coverage.persistence.DefaultCoverageBuilder.QTreeInfo;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.TiledRaster;
import org.deegree.coverage.raster.container.DiskBasedTileContainer;
import org.deegree.coverage.raster.container.GriddedBlobTileContainer;
import org.deegree.coverage.raster.container.IndexedMemoryTileContainer;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>RasterBuilder</code> recursively enters a given directory and creates a
 * {@link TiledRaster} from found image files.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
public class RasterBuilder {

	private final static Logger LOG = LoggerFactory.getLogger(RasterBuilder.class);

	/**
	 * Create a {@link MultiResolutionRaster} with the origin or the world coordinate of
	 * each raster file, defined by the given {@link OriginLocation}
	 * @param resolutionDirectory locating the different resolutions
	 * @param recursive if the sub directories of the resolution directories should be
	 * scanned as well
	 * @param options containing information on the loading of the raster data.
	 * @return a {@link MultiResolutionRaster} filled with {@link TiledRaster}s or
	 * <code>null</code> if the resolutionDirectory is not a directory.
	 */
	public MultiResolutionRaster buildMultiResolutionRaster(File resolutionDirectory, boolean recursive,
			RasterIOOptions options) {
		if (!resolutionDirectory.isDirectory()) {
			return null;
		}
		return buildMultiResolutionRaster(findResolutionDirs(resolutionDirectory), recursive, options);
	}

	/**
	 * Sets the no data value of the raster to the passed no data value if the no data
	 * value is not null. The no data value is converted to the byte array consistent to
	 * raster {@link DataType}.
	 * @param raster to update the no data values, never <code>null</code>
	 * @param noDataValue the no data value to set, may be <code>null</code> (nothing is
	 * updated then)
	 */
	public static void setNoDataValue(AbstractRaster raster, BigDecimal noDataValue) {
		if (noDataValue != null) {
			RasterDataInfo rasterDataInfo = raster.getRasterDataInfo();
			DataType dataType = rasterDataInfo.getDataType();
			byte[] noData = null;
			switch (dataType) {
				case DOUBLE:
					noData = ByteBuffer.allocate(dataType.getSize()).putDouble(noDataValue.doubleValue()).array();
					break;
				case FLOAT:
					noData = ByteBuffer.allocate(dataType.getSize()).putFloat(noDataValue.floatValue()).array();
					break;
				case INT:
					noData = ByteBuffer.allocate(dataType.getSize()).putInt(noDataValue.intValue()).array();
					break;
				case SHORT:
				case USHORT:
					noData = ByteBuffer.allocate(dataType.getSize()).putShort(noDataValue.shortValue()).array();
					break;
				case BYTE:
					noData = ByteBuffer.allocate(dataType.getSize()).put(noDataValue.byteValue()).array();
					break;
				case UNDEFINED:
				default:
					LOG.warn("No data values are not configurable for data type {}", dataType);
					break;
			}
			if (noData != null) {
				rasterDataInfo.setNoDataPixel(noData);
			}
		}
	}

	/**
	 * Scan the given directory for top level directories ending with a resolution.
	 * @param toplevelDir
	 * @return a list of directories which can be used for the building of
	 * {@link TiledRaster}s.
	 */
	private static List<File> findResolutionDirs(File toplevelDir) {
		List<File> result = new LinkedList<File>();
		for (File f : toplevelDir.listFiles()) {
			if (f.isDirectory()) {
				double res = RasterBuilder.getPixelResolution(null, f);
				if (!Double.isNaN(res)) {
					result.add(f);
				}
				else {
					LOG.info("Directory: " + f.getAbsolutePath()
							+ " can not be added to a Multiresolution raster, because it does not denote a resolution.");
				}
			}
		}
		return result;
	}

	/**
	 * @param resolutionDirectories locating the different resolutions
	 * @param recursive if the sub directories of the resolution directories should be
	 * scanned as well
	 * @param options containing values for the loading of the raster data.
	 * @return a {@link MultiResolutionRaster} filled with {@link TiledRaster}s
	 */
	private MultiResolutionRaster buildMultiResolutionRaster(List<File> resolutionDirectories, boolean recursive,
			RasterIOOptions options) {
		MultiResolutionRaster mrr = new MultiResolutionRaster(null);
		for (File resDir : resolutionDirectories) {
			if (resDir != null && resDir.isDirectory()) {
				AbstractRaster rasterLevel = buildTiledRaster(resDir, recursive, options);
				if (rasterLevel != null) {
					mrr.addRaster(rasterLevel);
				}
			}
		}
		return mrr;
	}

	/**
	 * Creates a coverage from the given raster location. Supported are loading from:
	 * <ul>
	 * <li>a raster file</li>
	 * <li>a raster directory containing a tiled raster</li>
	 * <li>a directory containing several sub directories named with doubles, containing
	 * different resolutions (a multiresolution raster tree).</li>
	 * </ul>
	 * @param rasterLocation may be a raster file or a raster directory containing a tiled
	 * raster or several sub directories named with doubles, containing different
	 * resolutions (a multiresolution raster tree).
	 * @param recursive if the directory should be searched recursively.
	 * @param options containing configured values for the loading of the coverage.
	 * @return an AbstractCoverage created from the given raster location. Result can be a
	 * {@link SimpleRaster} a {@link TiledRaster} or a {@link MultiResolutionRaster}.
	 * @throws IOException if the raster location could not be read.
	 */
	public AbstractCoverage buildCoverage(File rasterLocation, boolean recursive, RasterIOOptions options)
			throws IOException {
		if (rasterLocation == null) {
			throw new IOException("Raster location may not be null.");
		}
		if (!rasterLocation.exists()) {
			throw new IOException("Raster location (" + rasterLocation + ") does not exist.");
		}
		if (rasterLocation.isFile()) {
			return RasterFactory.loadRasterFromFile(rasterLocation, options);
		}
		List<File> resolutions = findResolutionDirs(rasterLocation);
		if (resolutions.isEmpty()) {
			return buildTiledRaster(rasterLocation, recursive, options);
		}
		return buildMultiResolutionRaster(resolutions, recursive, options);
	}

	/**
	 * Get the resolution from the resolution or if no value was configured try to get it
	 * from the name of the directory.
	 * @param resolution
	 * @param resolutionDir
	 * @return the resolution from the configuration if missing from the directory name,
	 * if not parse-able return NaN
	 */
	private static double getPixelResolution(Double resolution, File resolutionDir) {
		Double result = resolution;
		if (result == null || result.isNaN()) {
			File rasterDirectory = resolutionDir;
			String dirRes = FileUtils.getFilename(rasterDirectory);
			try {
				result = Double.parseDouble(dirRes);
			}
			catch (NumberFormatException e) {
				LOG.debug("No resolution found in raster datasource defintion, nor in the directory name: " + dirRes
						+ " returning 0");
				result = Double.NaN;
			}
		}
		return result;
	}

	/**
	 * Scan the given directory (recursively) for files with given extension and create a
	 * tiled raster from them. The tile raster will use an
	 * {@link IndexedMemoryTileContainer}. The options should define an
	 * {@link RasterIOOptions#OPT_FORMAT} to be used as file extension which will be case
	 * insensitive extension of the files to to scan for
	 * @param directory
	 * @param recursive if true sub directories will be scanned as well.
	 * @param options containing information on the data
	 * @return a new {@link TiledRaster} or <code>null</code> if no raster files were
	 * found at the given location, with the given extension.
	 */
	private static AbstractRaster buildTiledRaster(File directory, boolean recursive, RasterIOOptions options) {
		File indexFile = new File(directory, "deegree-pyramid.idx");
		if (!indexFile.exists() || indexFile.lastModified() < directory.lastModified()) {
			LOG.info("Scanning for files in directory: {}", directory.getAbsolutePath());
			List<File> coverageFiles = FileUtils.findFilesForExtensions(directory, recursive,
					"grid,bin,jpg,jpeg,png,tif,tiff,bmp,gif");
			TiledRaster raster = null;
			if (!coverageFiles.isEmpty()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Found following files: \n{}", coverageFiles.toString());
				}
				RasterIOOptions opts = new RasterIOOptions();
				opts.copyOf(options);

				String cacheDir = opts.get(RasterIOOptions.LOCAL_RASTER_CACHE_DIR);
				if (cacheDir == null) {
					String dir = directory.getName();
					if (directory.getParentFile() != null) {
						dir = directory.getParentFile().getName() + "_" + directory.getName();
					}
					opts.add(RasterIOOptions.LOCAL_RASTER_CACHE_DIR, dir);
				}
				if (opts.get(RasterIOOptions.CREATE_RASTER_MISSING_CACHE_DIR) == null) {
					opts.add(RasterIOOptions.CREATE_RASTER_MISSING_CACHE_DIR, "yes");
				}
				String nm = coverageFiles.get(0).getName();
				String format = nm.substring(nm.lastIndexOf('.') + 1);
				opts.add(RasterIOOptions.OPT_FORMAT, format);
				boolean readSingleBlobTile = false;
				if ("grid".equalsIgnoreCase(format) || "bin".equalsIgnoreCase(format)) {
					// the grid file structure can be defined over multiple 'bin' files,
					// which is used in e.g the WPVS.
					try {
						raster = new TiledRaster(GriddedBlobTileContainer.create(directory, opts), null);
						readSingleBlobTile = true;
					}
					catch (IOException e) {
						LOG.debug("Exception occurred: '{}'", e.getLocalizedMessage());
						LOG.trace("Stack trace:", e);
					}
				}
				if (!readSingleBlobTile) {
					if ("grid".equalsIgnoreCase(format) || "bin".equalsIgnoreCase(format)) {
						LOG.info(
								"Could not instantiate a gridded raster from a single grid file, trying to create a raster from files in directory.");
					}
					List<AbstractRaster> rasters = new ArrayList<AbstractRaster>(coverageFiles.size());
					QTreeInfo inf = buildTiledRaster(coverageFiles, rasters, opts);
					Envelope domain = inf.envelope;

					DiskBasedTileContainer container = new DiskBasedTileContainer(inf, coverageFiles, rasters, opts);
					container.export(indexFile);
					// if ( rasters.size() > 1000 ) {
					// container = new IndexedMemoryTileContainer( domain,
					// inf.rasterGeoReference, inf.numberOfObjects
					// );
					// ( (IndexedMemoryTileContainer) container ).addRasterTiles( rasters
					// );
					// } else {
					// container = new MemoryTileContainer( rasters );
					// }
					raster = new TiledRaster(container, null);
					raster.setCoordinateSystem(domain.getCoordinateSystem());
				}
			}
			else {
				LOG.warn("No raster files found in directory {}", directory.getAbsolutePath());
			}
			return raster;
		}

		DiskBasedTileContainer container = new DiskBasedTileContainer(indexFile);
		if (!container.isInitialized()) {
			indexFile.delete();
			return buildTiledRaster(directory, recursive, options);
		}
		AbstractRaster raster = new TiledRaster(container, null);
		raster.setCoordinateSystem(container.getRasterReference().getCrs());

		return raster;
	}

	/**
	 * @param coverageFiles to read
	 * @param result will hold the resulting coverages.
	 * @param options
	 * @return the total envelope of the given coverages
	 */
	private static QTreeInfo buildTiledRaster(List<File> coverageFiles, List<AbstractRaster> result,
			RasterIOOptions options) {
		Envelope resultEnvelope = null;
		RasterGeoReference rasterReference = null;

		ICRS crs = options == null ? null : options.getCRS();
		if (crs == null) {
			LOG.warn("Configured crs is null, maybe the rasterfiles define one.");
		}
		ICRS defaultCRS = crs;
		Envelope rasterEnvelope = null;
		for (File filename : coverageFiles) {
			try {
				LOG.info("Creating raster from file: {}", filename);
				RasterIOOptions newOpts = RasterIOOptions.forFile(filename);
				newOpts.copyOf(options);
				AbstractRaster raster = RasterFactory.loadRasterFromFile(filename, newOpts);
				ICRS rasterCRS = raster.getCoordinateSystem();
				if (defaultCRS == null) {
					defaultCRS = rasterCRS;
				}
				else {
					if (rasterCRS != null) {
						if (!rasterCRS.equals(defaultCRS)) {
							LOG.warn("Configured CRS was not compatible with CRS in files, replacing it.");
							defaultCRS = rasterCRS;
						}
					}
				}
				if (rasterEnvelope == null) {
					rasterEnvelope = raster.getEnvelope();
				}
				if (defaultCRS != null && raster.getCoordinateSystem() == null) {
					raster.setCoordinateSystem(defaultCRS);
				}
				if (resultEnvelope == null) {
					resultEnvelope = raster.getEnvelope();
				}
				else {
					resultEnvelope = resultEnvelope.merge(raster.getEnvelope());
				}
				if (rasterReference == null) {
					rasterReference = raster.getRasterReference();
				}
				else {
					rasterReference = RasterGeoReference.merger(rasterReference, raster.getRasterReference());
				}
				result.add(raster);
			}
			catch (IOException e) {
				LOG.error("unable to load raster, ignoring file ({}): {}", filename, e.getMessage());
			}
		}
		int leafObjects = calcBalancedLeafObjectSize(rasterEnvelope, resultEnvelope, 4);
		return new QTreeInfo(resultEnvelope, rasterReference, leafObjects);
	}

	/**
	 * Calculate the approximate objects in a leaf node.
	 * @param rasterEnvelope
	 * @param resultEnvelope
	 */
	private static int calcBalancedLeafObjectSize(Envelope rasterEnvelope, Envelope resultEnvelope, int treeDepth) {
		double tw = resultEnvelope.getSpan0();

		double rw = rasterEnvelope.getSpan0();

		double widthScale = Math.pow(0.5, treeDepth--);

		double leafSize = tw * widthScale;
		while (leafSize < (5 * rw)) {
			widthScale = Math.pow(0.5, treeDepth--);
			leafSize = tw * widthScale;
		}

		return Math.max(3, (int) Math.ceil(leafSize / rw));
	}

}
