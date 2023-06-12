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

package org.deegree.tools.coverage.gridifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.RasterDataFactory;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.InterleaveType;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.data.nio.PixelInterleavedRasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.grid.GridMetaInfoFile;
import org.deegree.coverage.tools.RasterOptionsParser;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.tools.coverage.gridifier.index.MultiLevelMemoryTileGridIndex;
import org.deegree.tools.coverage.gridifier.index.MultiLevelRasterTileIndex;
import org.deegree.tools.coverage.gridifier.index.MultiResolutionTileGrid;
import org.deegree.tools.coverage.gridifier.index.TileFile;

/**
 * Command line tool for converting a raster tree into a grid of regular, non-overlapping
 * raster cells.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
@Tool("Converts a deegree 2 raster tree into a grid of regular, non-overlapping raster cells encoded as raw RGB blobs, suitable for the WPVS.")
public class RasterTreeGridifier {

	final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RasterTreeGridifier.class);

	// private static final String OPT_INPUT_DIR = "inputdir";

	// For a postgis index
	private static final String OPT_INDEX_JDBC_URL = "jdbc_url";

	private static final String OPT_INDEX_JDBC_RAK_TABLE = "rak_table";

	private static final String OPT_INDEX_JDBC_PYR_TABLE = "pyr_table";

	// for a file based index.

	private static final String OPT_OUTPUT_DIR = "outputdir";

	private static final String OPT_OUTPUT_TILE_HEIGHT = "tile_height";

	private static final String OPT_OUTPUT_TILE_WIDTH = "tile_width";

	private static final String OPT_DOMAIN_MIN_X = "min_x";

	private static final String OPT_DOMAIN_MIN_Y = "min_y";

	private static final String OPT_DOMAIN_MAX_X = "max_x";

	private static final String OPT_DOMAIN_MAX_Y = "max_y";

	private static final String OPT_MAX_BLOB_SIZE = "blob_size";

	final String rtbBaseDir;

	final float minX;

	final float minY;

	final float maxX;

	final float maxY;

	final MultiLevelRasterTileIndex tileIndex;

	private final String outputDir;

	final int tileHeight;

	final int tileWidth;

	final long maxBlobSize;

	private final RasterLevel[] levels;

	private int domainWidth;

	private int domainHeight;

	final static org.deegree.geometry.GeometryFactory geomFactory = new GeometryFactory();

	File currentOutputDir;

	private long dataSize;

	int bytesPerTile;

	private int numWorkerThreads = 4;

	OriginLocation originLocation;

	private RasterTreeGridifier(String rtbBaseDir, float minX, float minY, float maxX, float maxY, String jdbcUrl,
			String rakTableName, String pyrTableName, String outputDir, int tileHeight, int tileWidth, long maxBlobSize,
			OriginLocation location) throws SQLException {
		this(rtbBaseDir, minX, minY, maxX, maxY, outputDir, tileHeight, tileWidth, maxBlobSize,
				new MultiLevelMemoryTileGridIndex(jdbcUrl, rakTableName, pyrTableName, minX, minY, maxX, maxY,
						location),
				location);
	}

	private RasterTreeGridifier(String rtbBaseDir, float minX, float minY, float maxX, float maxY, String outputDir,
			int tileHeight, int tileWidth, long maxBlobSize, boolean recursive, RasterIOOptions options) {
		this(rtbBaseDir, minX, minY, maxX, maxY, outputDir, tileHeight, tileWidth, maxBlobSize,
				new MultiResolutionTileGrid(new File(rtbBaseDir), recursive, options),
				options.getRasterOriginLocation());

	}

	/**
	 * @param rtbBaseDir
	 * @param minX
	 * @param minY
	 * @param maxX
	 * @param maxY
	 * @param outputDir
	 * @param tileHeight
	 * @param tileWidth
	 * @param maxBlobSize
	 */
	private RasterTreeGridifier(String rtbBaseDir, float minX, float minY, float maxX, float maxY, String outputDir,
			int tileHeight, int tileWidth, long maxBlobSize, MultiLevelRasterTileIndex tileIndex,
			OriginLocation originLocation) {
		this.rtbBaseDir = rtbBaseDir;
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;

		this.outputDir = outputDir;
		this.tileHeight = tileHeight;
		this.tileWidth = tileWidth;
		this.maxBlobSize = maxBlobSize;

		domainWidth = (int) (maxX - minX);
		domainHeight = (int) (maxY - minY);
		LOG.info("\nInitializing RasterTreeGridifier\n");
		LOG.info("- domain width: " + domainWidth);
		LOG.info("- domain height: " + domainHeight);

		this.tileIndex = tileIndex;

		this.levels = tileIndex.getRasterLevels();
		for (RasterLevel level : levels) {
			LOG.info("Raster level: " + level);
		}
		this.originLocation = originLocation;

	}

	private void generateCells(RasterLevel level) throws IOException, InterruptedException {

		double metersPerPixel = level.getNativeScale();
		double cellWidth = tileWidth * metersPerPixel;
		double cellHeight = tileHeight * metersPerPixel;

		int columns = (int) Math.ceil(domainWidth / cellWidth);
		int rows = (int) Math.ceil(domainWidth / cellHeight);

		bytesPerTile = tileWidth * tileHeight * 3;
		dataSize = (long) rows * (long) columns * tileWidth * tileHeight * 3L;
		int numberOfBlobs = (int) Math.ceil((double) dataSize / (double) maxBlobSize);

		// prepare output directory
		currentOutputDir = new File(outputDir, "" + metersPerPixel);
		if (!currentOutputDir.exists()) {
			if (!currentOutputDir.mkdir()) {
				LOG.warn("Could not create directory {}.", currentOutputDir);
			}
		}

		LOG.info("\nGridifying level: " + level.getLevel() + "\n");
		LOG.info("- meters per pixel: " + metersPerPixel);
		LOG.info("- cell width (world units): " + cellWidth);
		LOG.info("- cell height (world units): " + cellHeight);
		LOG.info("- number of columns: " + columns);
		LOG.info("- number of rows: " + rows);
		LOG.info("- output directory: " + currentOutputDir);
		LOG.info("- total amount of data: " + dataSize);
		LOG.info("- number of blobs: " + numberOfBlobs);

		Envelope env = geomFactory.createEnvelope(minX, minY, minX + columns * cellWidth, minY + rows * cellHeight,
				null);
		RasterGeoReference renv = RasterGeoReference.create(originLocation, env, columns * tileWidth,
				rows * tileHeight);

		writeMetaInfoFile(new File(currentOutputDir, GridMetaInfoFile.METAINFO_FILE_NAME), renv, columns, rows);

		// start writer daemon thread
		BlobWriterThread writer = new BlobWriterThread(rows * columns);
		Thread writerThread = new Thread(writer);
		writerThread.start();

		// generate and store cell data in separate worker threads
		ExecutorService exec = Executors.newFixedThreadPool(numWorkerThreads);

		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				double cellMinX = minX + column * cellWidth;
				double cellMinY = minY + (rows - row - 1) * cellHeight;
				double cellMaxX = cellMinX + cellWidth;
				double cellMaxY = cellMinY + cellHeight;
				int cellId = row * columns + column;
				Worker worker = new Worker(cellId, cellMinX, cellMinY, cellMaxX, cellMaxY, metersPerPixel, writer);
				exec.execute(worker);
			}
		}
		exec.shutdown();

		while (writerThread.isAlive()) {
			Thread.sleep(1000);
		}
	}

	private void writeMetaInfoFile(File file, RasterGeoReference env, int columns, int rows) throws IOException {

		System.out.print("\n Writing meta info file (raster envelope, etc)...");

		PrintWriter writer = new PrintWriter(new FileWriter(file));

		// begins with standard world file entries
		writer.println(env.getResolutionX());
		writer.println(env.getRotationY());
		writer.println(env.getRotationX());
		writer.println(env.getResolutionY());
		double[] origin = env.getOrigin();
		writer.println(origin[0]);
		writer.println(origin[1]);

		// now infos on grid
		writer.println(rows);
		writer.println(columns);
		writer.println(tileWidth);
		writer.println(tileHeight);

		writer.close();

		LOG.info("done.");
	}

	/**
	 * @param args
	 * @throws SQLException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws SQLException, IOException, InterruptedException {
		Options options = initOptions();

		RasterTreeGridifier gridifier;

		// for the moment, using the CLI API there is no way to respond to a help
		// argument; see
		// https://issues.apache.org/jira/browse/CLI-179
		if (args.length == 0 || (args.length > 0 && (args[0].contains("help") || args[0].contains("?")))) {
			printHelp(options);
		}

		try {
			CommandLine line = new PosixParser().parse(options, args);
			// new PosixParser().parse( options, args );

			String rtbBaseDir = line.getOptionValue(RasterOptionsParser.OPT_RASTER_LOCATION);
			float minX = Float.parseFloat(line.getOptionValue(OPT_DOMAIN_MIN_X));
			float minY = Float.parseFloat(line.getOptionValue(OPT_DOMAIN_MIN_Y));
			float maxX = Float.parseFloat(line.getOptionValue(OPT_DOMAIN_MAX_X));
			float maxY = Float.parseFloat(line.getOptionValue(OPT_DOMAIN_MAX_Y));
			String outputDir = line.getOptionValue(OPT_OUTPUT_DIR);
			int tileHeight = Integer.parseInt(line.getOptionValue(OPT_OUTPUT_TILE_HEIGHT));
			int tileWidth = Integer.parseInt(line.getOptionValue(OPT_OUTPUT_TILE_WIDTH));

			long maxBlobSize = blobSize(line.getOptionValue(OPT_MAX_BLOB_SIZE));

			RasterIOOptions ioOptions = RasterOptionsParser.parseRasterIOOptions(line);
			boolean recursive = line.hasOption(RasterOptionsParser.OPT_RECURSIVE);

			String jdbcUrl = line.getOptionValue(OPT_INDEX_JDBC_URL);
			if (jdbcUrl != null) {

				String rakTableName = line.getOptionValue(OPT_INDEX_JDBC_RAK_TABLE);
				if (rakTableName == null || "".equals(rakTableName)) {
					System.err.println("ERROR: Invalid command line: " + OPT_INDEX_JDBC_RAK_TABLE
							+ " must be a valid table name.");
					return;
				}

				String pyrTableName = line.getOptionValue(OPT_INDEX_JDBC_PYR_TABLE);

				if (pyrTableName == null || "".equals(pyrTableName)) {
					System.err.println("ERROR: Invalid command line: " + OPT_INDEX_JDBC_PYR_TABLE
							+ " must be a valid table name.");
					return;
				}

				gridifier = new RasterTreeGridifier(rtbBaseDir, minX, minY, maxX, maxY, jdbcUrl, rakTableName,
						pyrTableName, outputDir, tileHeight, tileWidth, maxBlobSize,
						ioOptions.getRasterOriginLocation());
			}
			else {

				// String extension = line.getOptionValue( OPT_FILE_EXTENSIONS );
				// if ( extension == null || "".equals( extension ) ) {
				// System.err.println( "ERROR: Invalid command line: " +
				// OPT_FILE_EXTENSIONS
				// + " must be a valid file extension, e.g something like 'jpg' or
				// 'tiff'." );
				// return;
				// }
				//
				// String crs = line.getOptionValue( OPT_CRS );

				// if ( crs == null || "".equals( crs ) ) {
				// System.err.println( "ERROR: Invalid command line: " + OPT_CRS
				// + " must be a valid crs name, e.g. epsg:26912" );
				// return;
				// }
				// RasterIOOptions ioOptions = new RasterIOOptions();
				// ioOptions.add( RasterIOOptions.CRS, crs );
				// ioOptions.add( RasterIOOptions.GEO_ORIGIN_LOCATION, location.name() );

				gridifier = new RasterTreeGridifier(rtbBaseDir, minX, minY, maxX, maxY, outputDir, tileHeight,
						tileWidth, maxBlobSize, recursive, ioOptions);

			}

			for (int i = 0; i < gridifier.levels.length; ++i) {
				gridifier.generateCells(gridifier.levels[i]);
			}

		}
		catch (ParseException exp) {
			System.err.println("ERROR: Invalid command line: " + exp.getMessage());
		}
	}

	private static long blobSize(String blobsize) {
		String unit = blobsize.substring(blobsize.length() - 1);
		if (Pattern.matches("\\d", unit)) {
			return Long.parseLong(blobsize);
		}
		unit = unit.toLowerCase();
		long val = Long.parseLong(blobsize.substring(0, blobsize.length() - 1));
		if ("k".equals(unit)) {
			val *= 1024;
		}
		else if ("m".equals(unit)) {
			val *= (1024 * 1024);
		}
		else if ("g".equals(unit)) {
			val *= (1024 * 1024 * 1024);

		}
		else {
			throw new IllegalArgumentException(
					"Unknown blobsize unit: " + unit + " (try k(ilo), m(ega), g(iga) byte).");
		}

		return val;
	}

	private static Options initOptions() {

		Options opts = new Options();

		RasterOptionsParser.addRasterIOLineOptions(opts);

		// Option opt = new Option( OPT_INPUT_DIR, true, "base dir of input raster tree"
		// );
		// opt.setRequired( true );
		// opts.addOption( opt );

		Option opt = new Option(OPT_INDEX_JDBC_URL, true, "JDBC url of database with index tables");
		// opt.setRequired( true );
		opts.addOption(opt);

		opt = new Option(OPT_INDEX_JDBC_PYR_TABLE, true, "name of index table");
		// opt.setRequired( true );
		opts.addOption(opt);

		opt = new Option(OPT_INDEX_JDBC_RAK_TABLE, true, "name of level table");
		// opt.setRequired( true );
		opts.addOption(opt);

		// opt = new Option( OPT_CRS, true, "crs of the original files" );
		// opts.addOption( opt );

		// opt = new Option( "e", OPT_FILE_EXTENSIONS, true, "exension of the original
		// files e.g. 'jpg' or 'tiff'" );
		// opts.addOption( opt );

		opt = new Option(OPT_OUTPUT_DIR, true, "output dir for gridded tiles");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option(OPT_OUTPUT_TILE_HEIGHT, true, "height of generated (output) raster tiles (pixels)");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option(OPT_OUTPUT_TILE_WIDTH, true, "width of generated (output) raster tiles (pixels)");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option(OPT_DOMAIN_MIN_X, true, "minimum x coordinate of gridded area");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option(OPT_DOMAIN_MIN_Y, true, "minimum y coordinate of gridded area");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option(OPT_DOMAIN_MAX_X, true, "maximum x coordinate of gridded area");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option(OPT_DOMAIN_MAX_Y, true, "maximum y coordinate of gridded area");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option(OPT_MAX_BLOB_SIZE, true, "maximum size (bytes) of a single blob file");
		opt.setRequired(true);
		opts.addOption(opt);

		return opts;
	}

	private static void printHelp(Options options) {
		CommandUtils.printHelp(options, RasterTreeGridifier.class.getSimpleName(), null, "file/dir [file/dir(s)]");
	}

	Map<Integer, AbstractRaster> tileIdToRaster = Collections.synchronizedMap(new TileCache(650));

	private class Worker implements Runnable {

		private double cellMinX;

		private double cellMinY;

		private double cellMaxX;

		private double cellMaxY;

		private double metersPerPixel;

		private BlobWriterThread writer;

		private int cellId;

		Worker(int cellId, double cellMinX, double cellMinY, double cellMaxX, double cellMaxY, double metersPerPixel,
				BlobWriterThread writer) {
			this.cellId = cellId;
			this.cellMinX = cellMinX;
			this.cellMinY = cellMinY;
			this.cellMaxX = cellMaxX;
			this.cellMaxY = cellMaxY;
			this.metersPerPixel = metersPerPixel;
			this.writer = writer;
		}

		public void run() {

			Envelope cellWorldEnvelope = geomFactory.createEnvelope(cellMinX, cellMinY, cellMaxX, cellMaxY, null);
			RasterGeoReference cellRasterReference = RasterGeoReference.create(originLocation, cellWorldEnvelope,
					tileWidth, tileHeight);
			// rb: TODO should the following raster data be added to the cache.
			ByteBufferRasterData cellRasterData = RasterDataFactory.createRasterData(tileWidth, tileHeight,
					BandType.RGB, DataType.BYTE, InterleaveType.PIXEL, false);
			SimpleRaster cellRaster = new SimpleRaster(cellRasterData, cellWorldEnvelope, cellRasterReference, null);

			double croppedMinX = minX < cellMinX ? cellMinX : minX;
			double croppedMinY = minY < cellMinY ? cellMinY : minY;
			double croppedMaxX = maxX > cellMaxX ? cellMaxX : maxX;
			double croppedMaxY = maxY > cellMaxY ? cellMaxY : maxY;

			Set<TileFile> tileFiles = tileIndex.getTiles(
					geomFactory.createEnvelope(croppedMinX, croppedMinY, croppedMaxX, croppedMaxY, null),
					metersPerPixel);
			for (TileFile tileFile : tileFiles) {
				AbstractRaster raster = tileIdToRaster.get(tileFile.getId());
				if (raster == null) {
					try {
						raster = tileFile.loadRaster(rtbBaseDir);
					}
					catch (IOException e) {
						e.printStackTrace();
					}
					tileIdToRaster.put(tileFile.getId(), raster);
				}
				setSubsetWithAlphaHack(cellRaster, raster);
			}
			writer.add(cellId, cellRasterData.getByteBuffer());
		}

		private void setSubsetWithAlphaHack(SimpleRaster target, AbstractRaster source) {
			if (target != null && source != null) {
				// rb: todo the intersection of two envelopes should be an envelope, but
				// the cast will be wrong.
				Envelope tEnv = target.getEnvelope();
				Envelope sEnv = source.getEnvelope();
				if (tEnv != null && sEnv != null) {
					Geometry geom = tEnv.getIntersection(sEnv);
					if (geom != null) {
						Envelope intersectEnv = geom.getEnvelope();
						if (intersectEnv != null) {
							RasterRect rect = target.getRasterReference().convertEnvelopeToRasterCRS(intersectEnv);
							SimpleRaster src = source.getSubRaster(intersectEnv).getAsSimpleRaster();

							PixelInterleavedRasterData targetData = (PixelInterleavedRasterData) target.getRasterData();
							setSubset(targetData, rect.x, rect.y, rect.width, rect.height, src.getRasterData());
						}
					}
				}
			}
			else {
				LOG.debug("Ignoring rasters because of null reference.");
			}
		}

		private void setSubset(PixelInterleavedRasterData targetData, int x0, int y0, int width, int height,
				RasterData sourceRaster) {

			// clamp to maximum possible size
			int subWidth = min(targetData.getColumns() - x0, width, sourceRaster.getColumns());
			int subHeight = min(targetData.getRows() - y0, height, sourceRaster.getRows());

			byte[] tmp = new byte[targetData.getDataInfo().getDataSize()];
			for (int y = 0; y < subHeight; y++) {
				for (int x = 0; x < subWidth; x++) {

					byte[] color = new byte[3];
					byte[] c = new byte[1];
					c = sourceRaster.getSample(x, y, 0, c);
					color[0] = c[0];
					c = sourceRaster.getSample(x, y, 1, c);
					color[1] = c[0];
					c = sourceRaster.getSample(x, y, 2, c);
					color[2] = c[0];

					if (!shouldBeTransparent(color)) {
						targetData.setSample(x0 + x, y0 + y, 0, sourceRaster.getSample(x, y, 0, tmp));
						targetData.setSample(x0 + x, y0 + y, 1, sourceRaster.getSample(x, y, 1, tmp));
						targetData.setSample(x0 + x, y0 + y, 2, sourceRaster.getSample(x, y, 2, tmp));
					}
				}
			}
		}

		private int min(int... sizes) {
			int result = Math.min(sizes[0], sizes[1]);
			int i = 2;
			while (i < sizes.length) {
				result = Math.min(result, sizes[i]);
				i++;
			}
			return result;
		}

		private boolean shouldBeTransparent(byte[] argb) {
			return (argb[0] == -1 && argb[1] == 0 && argb[2] == -1) || argb[0] == 3;
		}

	}

	/**
	 *
	 * The <code>TileCache</code> implements a simple caching mechanism
	 *
	 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
	 *
	 */
	private static class TileCache extends LinkedHashMap<Integer, AbstractRaster> {

		/**
		 *
		 */
		private static final long serialVersionUID = 1640216638286535720L;

		private int maxEntries;

		TileCache(int maxEntries) {
			super(100, 0.1f, false);
			this.maxEntries = maxEntries;
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<Integer, AbstractRaster> eldest) {
			return size() > maxEntries;
		}

	}

	private class BlobWriterThread implements Runnable {

		private static final int MAX_FIFO_SIZE = 10;

		private SortedMap<Integer, ByteBuffer> fifo = Collections
			.synchronizedSortedMap(new TreeMap<Integer, ByteBuffer>());

		private Object queueDataAvailable = new Object();

		private int numCells;

		private int currentBlobNo;

		private int waitingForCell;

		private long bytesInCurrentBlob;

		private FileChannel currentBlobChannel;

		BlobWriterThread(int numCells) {
			this.numCells = numCells;
		}

		void add(int cellId, ByteBuffer buffer) {

			while (fifo.size() > MAX_FIFO_SIZE && cellId != waitingForCell) {
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			synchronized (queueDataAvailable) {
				fifo.put(cellId, buffer);
				queueDataAvailable.notify();
			}

		}

		@Override
		public void run() {

			LOG.info("Starting writer daemon thread...");

			long begin = System.currentTimeMillis();
			int cellId = 0;

			try {
				currentBlobNo = 0;
				File blob = new File(currentOutputDir, "blob_" + currentBlobNo + ".bin");
				currentBlobChannel = new FileOutputStream(blob).getChannel();
				bytesInCurrentBlob = 0;

				while (numCells >= cellId + 1) {
					synchronized (queueDataAvailable) {
						waitingForCell = cellId;
						while (!fifo.containsKey(cellId)) {
							queueDataAvailable.wait();
						}
						waitingForCell = -1;
					}

					ByteBuffer buffer = fifo.remove(cellId++);
					storeCell(buffer);
					if (cellId % 100 == 0) {
						long elapsed = System.currentTimeMillis() - begin;
						double rate = cellId / (elapsed / 1000d);
						LOG.info("Tile generation rate: " + rate + " tiles / second");
						LOG.info("cached tiles: " + tileIdToRaster.size());
						LOG.info("total mem: " + Runtime.getRuntime().totalMemory());
						LOG.info("free mem: " + Runtime.getRuntime().freeMemory());
						// call garbage collector explicitly
						System.gc();
					}
				}

				LOG.info("Writer daemon thread is exiting.");
				currentBlobChannel.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		private void storeCell(ByteBuffer buffer) throws IOException {

			if (maxBlobSize - bytesInCurrentBlob < bytesPerTile) {
				currentBlobChannel.close();
				currentBlobNo++;
				File blob = new File(currentOutputDir, "blob_" + currentBlobNo + ".bin");
				currentBlobChannel = new FileOutputStream(blob).getChannel();
				bytesInCurrentBlob = 0;
				LOG.info("beginning with new blob: '" + blob + "'");
			}

			buffer.rewind();
			currentBlobChannel.write(buffer);
			bytesInCurrentBlob += bytesPerTile;
		}

	}

}
