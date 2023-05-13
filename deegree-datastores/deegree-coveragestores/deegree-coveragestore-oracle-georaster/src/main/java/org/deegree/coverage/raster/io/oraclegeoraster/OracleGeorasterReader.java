/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2014 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit GmbH -
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

package org.deegree.coverage.raster.io.oraclegeoraster;

import static org.deegree.commons.utils.ColorUtils.decodeWithAlpha;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import oracle.spatial.georaster.JGeoRaster;
import oracle.spatial.georaster.image.GeoRasterImage;
import oracle.sql.STRUCT;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.coverage.persistence.oraclegeoraster.jaxb.AbstractOracleGeorasterType;
import org.deegree.coverage.persistence.oraclegeoraster.jaxb.AbstractOracleGeorasterType.StorageBBox;
import org.deegree.coverage.persistence.oraclegeoraster.jaxb.OracleGeorasterConfig;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.data.container.BufferResult;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.info.InterleaveType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Oracle GeoRaster Reader
 *
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * @since 3.4
 */
public class OracleGeorasterReader implements RasterReader {

	class MetaInfo {

		public double[] mbr = null;

		public String mbrLocation;

		public int bands = -1;

	}

	private static final Logger LOG = LoggerFactory.getLogger(OracleGeorasterReader.class);

	private static final Color[] DBG_COLORS = new Color[] { Color.RED, Color.YELLOW, Color.BLUE, Color.CYAN,
			Color.ORANGE };

	private static final String ORACLE_RASTER_META_NS = "xmlns=\"http://xmlns.oracle.com/spatial/georaster\"";

	private static int DBG_COLOR_CNT = -1;

	private Object LOCK = new Object();

	private Rectangle rasterRect;

	private Envelope envelope;

	private RasterGeoReference rasterReference;

	private String dataLocationId;

	private RasterDataInfo rdi;

	private ICRS crs = null;

	private String jdbcConnId;

	private String rasterTable;

	private String rasterRDTTable;

	private String rasterColumn;

	private int rasterId;

	private int bandR = -1;

	private int bandG = -1;

	private int bandB = -1;

	// private byte[] nodata = null;

	private int level;

	private int maxLevel;

	private Workspace workspace;

	private int debug = 0;

	private Color noData;

	private MetaInfo info;

	private OracleGeorasterReader(Workspace workspace, OracleGeorasterReader base, int lvl) {
		this.workspace = workspace;
		this.crs = base.crs;
		this.jdbcConnId = base.jdbcConnId;
		this.rasterId = base.rasterId;
		this.rasterTable = base.rasterTable;
		this.rasterRDTTable = base.rasterRDTTable;
		this.rasterColumn = base.rasterColumn;

		this.bandR = base.bandR;
		this.bandG = base.bandG;
		this.bandB = base.bandB;
		this.noData = base.noData;
		this.maxLevel = base.maxLevel;
		this.level = lvl;
		this.dataLocationId = base.dataLocationId + "." + lvl;
		this.envelope = base.envelope;
		this.debug = base.debug;

		double sc = Math.pow(2.0d, lvl);
		int lvlCurWidth = (int) (((double) base.rasterRect.width) / sc);
		int lvlCurHeight = (int) (((double) base.rasterRect.height) / sc);

		rasterRect = new Rectangle(0, 0, lvlCurWidth, lvlCurHeight);
		rasterReference = RasterGeoReference.create(OriginLocation.OUTER, envelope, lvlCurWidth, lvlCurHeight);
	}

	public OracleGeorasterReader(Workspace workspace, OracleGeorasterConfig config)
			throws IOException, ResourceInitException {
		this.workspace = workspace;

		initBase(config);

		init(config.getRaster(), config.getStorageBBox(), config.getBands());
	}

	public OracleGeorasterReader(Workspace workspace, OracleGeorasterConfig config,
			AbstractOracleGeorasterType.Part part) throws IOException, ResourceInitException {
		this.workspace = workspace;

		initBase(config);

		init(part.getRaster(), part.getStorageBBox(), part.getBands());
	}

	private void initBase(OracleGeorasterConfig config) {
		// retrieve data from config
		info = new MetaInfo();
		jdbcConnId = config.getJDBCConnId();

		if (config.getDebug() != null) {
			debug = config.getDebug().intValue();
		}

		if (config.getStorageCRS() != null && config.getStorageCRS().trim().length() > 0) {
			crs = CRSManager.getCRSRef(config.getStorageCRS());
		}

	}

	private void init(AbstractOracleGeorasterType.Raster raster, StorageBBox storageBBox,
			AbstractOracleGeorasterType.Bands bands) throws ResourceInitException, IOException {
		rasterTable = emptyToNull(raster.getTable());
		rasterRDTTable = emptyToNull(raster.getRDTTable());
		rasterColumn = emptyToNull(raster.getColumn());
		rasterId = raster.getId();
		// 3.4.0
		if (raster.getRows() > 0 && raster.getColumns() > 0) {
			rasterRect = new Rectangle(0, 0, raster.getColumns(), raster.getRows());
		}
		maxLevel = raster.getMaxLevel();

		if (bands != null) {
			if (bands.getSingle() != null) {
				int val = bands.getSingle().intValue();
				bandR = val;
				bandG = val;
				bandB = val;
			}
			else if (bands.getRGB() != null) {
				bandR = bands.getRGB().getRed();
				bandG = bands.getRGB().getGreen();
				bandB = bands.getRGB().getBlue();
			}

			if (bands.getNodata() != null) {
				noData = decodeWithAlpha(bands.getNodata().replaceFirst("^0[xX]", "#"));
			}
		}

		if (storageBBox != null) {
			List<Double> ll = storageBBox.getLowerCorner();
			List<Double> ur = storageBBox.getUpperCorner();
			if (ll != null && ur != null && ll.size() > 1 && ur.size() > 1) {
				try {
					double[] dbllst = new double[4];
					dbllst[0] = ll.get(0).doubleValue();
					dbllst[1] = ll.get(1).doubleValue();
					dbllst[2] = ur.get(0).doubleValue();
					dbllst[3] = ur.get(1).doubleValue();
					this.info.mbr = dbllst;
				}
				catch (Exception ex) {
					LOG.warn("Failed loading StorageBBox from config: {}", ex.getMessage());
					LOG.trace("Exception", ex);
				}
			}
		}

		Connection con = null;
		ConnectionProvider connProvider;
		try {
			boolean needBase = (rasterTable == null || rasterColumn == null);
			if (needBase || needsMetadataLookup()) {
				connProvider = workspace.getResource(ConnectionProviderProvider.class, jdbcConnId);
				con = connProvider.getConnection();
				if (needBase)
					loadGeoRasterBaseDate(con);

				if (needsMetadataLookup())
					loadGeoRasterMetadata(con);
			}

		}
		catch (IOException ioe) {
			LOG.warn("Raster {}.{}:{} failed (error see next log entry)", this.rasterTable, this.rasterColumn,
					this.rasterId);
			throw ioe;
		}
		catch (Exception othere) {
			LOG.warn("Raster {}.{}:{} failed (error see next log entry)", this.rasterTable, this.rasterColumn,
					this.rasterId);
			throw new ResourceInitException("Failed to init GeoRaster", othere);
		}
		finally {
			JDBCUtils.close(con);
		}

		// output loaded infos
		StringBuilder id = new StringBuilder();
		id.append("oracle://").append(this.jdbcConnId).append("localhost/oraclegeorasterid");
		id.append("/").append(this.rasterTable).append("/").append(this.rasterRDTTable);
		id.append("/").append(this.rasterRDTTable).append("/").append(this.rasterColumn);
		id.append("/").append(this.rasterId);
		this.dataLocationId = id.toString();

		try {
			calculateEnvelopeAndReference();
		}
		catch (Exception ex) {
			LOG.trace("Execption", ex);
			throw new ResourceInitException(
					"GeoRaster could not calculate Spatial extend. Please correct config or db.");
		}

		if (envelope == null || rasterRect == null)
			throw new ResourceInitException(
					"GeoRaster has no Spatial and/or Size information. Please correct config or db.");
		if (maxLevel < 0) {
			maxLevel = 0;
			LOG.warn("Raster {}.{}:{} has no Pyramid, this is not recommended", rasterTable, rasterColumn, rasterId);
		}

		if (info.mbr != null && info.mbr.length > 3) {
			LOG.info("Raster {}.{}:{} Size: {}x{} Levels:  0 - {} BBOX: {} {} - {} {}", rasterTable, rasterColumn,
					rasterId, rasterRect.width, rasterRect.height, maxLevel, info.mbr[0], info.mbr[1], info.mbr[2],
					info.mbr[3]);
		}
	}

	private boolean needsMetadataLookup() {
		return (rasterRDTTable == null || maxLevel < 0 || info == null || info.mbr == null || rasterRect == null);
	}

	private String emptyToNull(String inp) {
		if (inp != null && inp.trim().length() == 0)
			return null;

		return inp;
	}

	private void calculateEnvelopeAndReference() {
		boolean isOuter = info.mbrLocation == null || "upperleft".equalsIgnoreCase(info.mbrLocation);
		if (info.mbr == null || rasterRect == null) {
			return;
		}

		MathContext mc = MathContext.DECIMAL128;
		BigDecimal min0 = BigDecimal.valueOf(info.mbr[0]).min(BigDecimal.valueOf(info.mbr[2]));
		BigDecimal min1 = BigDecimal.valueOf(info.mbr[1]).min(BigDecimal.valueOf(info.mbr[3]));
		BigDecimal max0 = BigDecimal.valueOf(info.mbr[0]).max(BigDecimal.valueOf(info.mbr[2]));
		BigDecimal max1 = BigDecimal.valueOf(info.mbr[1]).max(BigDecimal.valueOf(info.mbr[3]));

		BigDecimal span0 = max0.subtract(min0, mc);
		BigDecimal span1 = max1.subtract(min1, mc);

		if (!isOuter) {
			BigDecimal two = new BigDecimal(2);
			BigDecimal half0 = span0.divide(new BigDecimal(rasterRect.width), mc).divide(two, mc).abs(mc);
			BigDecimal half1 = span1.divide(new BigDecimal(rasterRect.height), mc).divide(two, mc).abs(mc);

			min0 = min0.add(half0, mc);
			min1 = min1.subtract(half1, mc);
			max0 = max0.add(half0, mc);
			max1 = max1.subtract(half1, mc);
		}

		envelope = (new GeometryFactory()).createEnvelope(min0.doubleValue(), min1.doubleValue(), max0.doubleValue(),
				max1.doubleValue(), crs);

		rasterReference = RasterGeoReference.create(OriginLocation.OUTER, envelope, rasterRect.width,
				rasterRect.height);
	}

	public AbstractRaster getRaster() {
		RasterDataInfo rdi = getRasterDataInfo();
		// RasterIOOptions options = new RasterIOOptions();
		// options.setNoData(nodata);
		// return RasterFactory.createEmptyRaster( rdi, envelope, rasterReference, this,
		// false, options );
		return RasterFactory.createEmptyRaster(rdi, envelope, rasterReference, this, false, null);
	}

	private void loadGeoRasterBaseDate(Connection con) throws IOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(
					"SELECT TABLE_NAME, COLUMN_NAME, RDT_TABLE_NAME FROM USER_SDO_GEOR_SYSDATA WHERE RASTER_ID = ?");
			ps.setInt(1, this.rasterId);

			rs = ps.executeQuery();
			if (!rs.next())
				throw new SQLException("No GeoRaster object with rasterid = " + this.rasterId
						+ " registered in USER_SDO_GEOR_SYSDATA");

			if (this.rasterTable == null)
				this.rasterTable = rs.getString(1);

			if (this.rasterColumn == null)
				this.rasterColumn = rs.getString(2);

			if (this.rasterRDTTable == null)
				this.rasterRDTTable = rs.getString(3);

		}
		catch (Exception ex) {
			throw new IOException(ex.getMessage());
		}
		finally {
			JDBCUtils.close(rs);
			JDBCUtils.close(ps);
		}
	}

	private void loadGeoRasterMetadata(Connection con) throws IOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT");
			sb.append(" SDO_GEOM.SDO_MIN_MBR_ORDINATE( x.").append(rasterColumn).append(".spatialExtent ,1 ),"); // minX
			sb.append(" SDO_GEOM.SDO_MIN_MBR_ORDINATE( x.").append(rasterColumn).append(".spatialExtent ,2 ),"); // minY
			sb.append(" SDO_GEOM.SDO_MAX_MBR_ORDINATE( x.").append(rasterColumn).append(".spatialExtent ,1 ),"); // maxX
			sb.append(" SDO_GEOM.SDO_MAX_MBR_ORDINATE( x.").append(rasterColumn).append(".spatialExtent ,2 ),"); // maxY
			sb.append(" EXTRACTVALUE( x.")
				.append(rasterColumn)
				.append(".metadata, '/georasterMetadata/rasterInfo/dimensionSize[@type=\"ROW\"]/size', '");
			sb.append(ORACLE_RASTER_META_NS).append("'),"); // rowSize
			sb.append(" EXTRACTVALUE( x.")
				.append(rasterColumn)
				.append(".metadata, '/georasterMetadata/rasterInfo/dimensionSize[@type=\"COLUMN\"]/size', '");
			sb.append(ORACLE_RASTER_META_NS).append("'),");// colSize
			sb.append(" EXTRACTVALUE( x.")
				.append(rasterColumn)
				.append(".metadata, '/georasterMetadata/rasterInfo/dimensionSize[@type=\"BAND\"]/size', '");
			sb.append(ORACLE_RASTER_META_NS).append("'),");// bandSize
			sb.append(" EXTRACTVALUE( x.")
				.append(rasterColumn)
				.append(".metadata, '/georasterMetadata/spatialReferenceInfo/modelCoordinateLocation', '");
			sb.append(ORACLE_RASTER_META_NS).append("'),");// modelCoordinateLocation
			sb.append(" EXTRACTVALUE( x.")
				.append(rasterColumn)
				.append(".metadata, '/georasterMetadata/rasterInfo/pyramid/maxLevel', '");
			sb.append(ORACLE_RASTER_META_NS).append("'),");// maxLevel
			sb.append(" x.").append(rasterColumn).append(".rasterDataTable,"); // rdtTable
			// optional stuff
			sb.append(" EXTRACTVALUE( x.")
				.append(rasterColumn)
				.append(".metadata, '/georasterMetadata/rasterInfo/ULTCoordinate/row', '");
			sb.append(ORACLE_RASTER_META_NS).append("'),");// ultRow
			sb.append(" EXTRACTVALUE( x.")
				.append(rasterColumn)
				.append(".metadata, '/georasterMetadata/rasterInfo/ULTCoordinate/column', '");
			sb.append(ORACLE_RASTER_META_NS).append("')");// ultCol

			sb.append(" FROM ").append(rasterTable);
			sb.append(" x WHERE x.").append(rasterColumn).append(".rasterid = ?");
			if (this.rasterRDTTable != null) {
				sb.append(" AND UPPER(x.").append(rasterColumn).append(".rasterdatatable) = ?");
			}
			ps = con.prepareStatement(sb.toString());

			ps.setInt(1, this.rasterId);
			if (this.rasterRDTTable != null) {
				ps.setString(2, this.rasterRDTTable.toUpperCase());
			}

			rs = ps.executeQuery();
			if (!rs.next())
				throw new SQLException("No GeoRaster object exists at rasterid = " + this.rasterId + " (RDT = "
						+ this.rasterRDTTable + ")");

			if (this.info == null)
				this.info = new MetaInfo();

			boolean mbrOk = true;
			double[] mbrSql = new double[4];
			for (int i = 0; i < 4; i++) {
				mbrSql[i] = rs.getDouble(1 + i);
				if (rs.wasNull())
					mbrOk = false;
			}
			if (this.info.mbr == null && mbrOk)
				this.info.mbr = mbrSql;

			boolean lastNull, rowNull, colNull;
			int lastInt, rows, cols;

			rows = rs.getInt(5);
			rowNull = rs.wasNull();
			cols = rs.getInt(6);
			colNull = rs.wasNull();
			if (!rowNull && !colNull && rasterRect == null)
				rasterRect = new Rectangle(0, 0, cols, rows);

			lastInt = rs.getInt(7);
			lastNull = rs.wasNull();
			if (this.info.bands < 0 && !lastNull)
				this.info.bands = lastInt;

			this.info.mbrLocation = rs.getString(8);

			lastInt = rs.getInt(9);
			lastNull = rs.wasNull();
			if (this.maxLevel < 0 && !lastNull) {
				this.maxLevel = lastInt;
			}

			if (rasterRDTTable == null)
				rasterRDTTable = emptyToNull(rs.getString(10));

			int ultRow, ultCol;
			ultRow = rs.getInt(11);
			ultCol = rs.getInt(12);
			if (ultRow != 0 || ultCol != 0) {
				LOG.info("Raster {}.{}:{} ULTCoordinate: row {} col {} currently ignored", this.rasterTable,
						this.rasterColumn, this.rasterId, ultRow, ultCol);
			}
		}
		catch (Exception ex) {
			throw new IOException(ex.getMessage());
		}
		finally {
			JDBCUtils.close(rs);
			JDBCUtils.close(ps);
		}
	}

	private JGeoRaster getGeoraster(Connection con) throws IOException {
		JGeoRaster res = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ").append(this.rasterColumn);
			sb.append(" FROM ").append(this.rasterTable);
			sb.append(" a WHERE a.").append(this.rasterColumn);
			sb.append(".rasterid = ? AND UPPER(a.").append(this.rasterColumn);
			sb.append(".rasterdatatable) = ?");
			ps = con.prepareStatement(sb.toString());
			ps.setInt(1, this.rasterId);
			ps.setString(2, this.rasterRDTTable.toUpperCase());

			rs = ps.executeQuery();

			if (!rs.next())
				throw new SQLException(
						"No GeoRaster object exists at rasterid = " + this.rasterId + ", RDT = " + this.rasterRDTTable);

			STRUCT struct = (STRUCT) rs.getObject(1);
			res = new JGeoRaster(struct);
		}
		catch (Exception ex) {
			throw new IOException(ex.getMessage());
		}
		finally {
			JDBCUtils.close(rs);
			JDBCUtils.close(ps);
		}

		return res;
	}

	@Override
	public BufferResult read(RasterRect rect, ByteBuffer result) throws IOException {
		Rectangle intersect = rasterRect.intersection(new Rectangle(rect.x, rect.y, rect.width, rect.height));

		if (LOG.isDebugEnabled()) {
			LOG.debug("Reading Rasterdata from {}", this.dataLocationId);
			LOG.debug(" Inters X = {}", intersect.x);
			LOG.debug(" Inters Y = {}", intersect.y);
			LOG.debug(" Inters W = {}", intersect.width);
			LOG.debug(" Inters H = {}", intersect.height);
		}

		if (intersect.width == 0 || intersect.height == 0)
			return null;

		if (LOG.isDebugEnabled()) {
			LOG.debug(" Raster X = {}", rasterRect.x);
			LOG.debug(" Raster Y = {}", rasterRect.y);
			LOG.debug(" Raster W = {}", rasterRect.width);
			LOG.debug(" Raster H = {}", rasterRect.height);

			LOG.debug(" Rect   X = {}", rect.x);
			LOG.debug(" Rect   Y = {}", rect.y);
			LOG.debug(" Rect   W = {}", rect.width);
			LOG.debug(" Rect   H = {}", rect.height);

			LOG.debug(" Level    = {} ( max = {} )", level, maxLevel);
		}

		Connection con = null;
		BufferResult res = null;
		RenderedImage img = null;

		ConnectionProvider connProvider;
		try {
			connProvider = workspace.getResource(ConnectionProviderProvider.class, jdbcConnId);
			con = connProvider.getConnection();

			JGeoRaster jGeoRaster = getGeoraster(con);
			GeoRasterImage geoRasterImg = jGeoRaster.getGeoRasterImageObject();

			if (this.bandR > 0 && this.bandG > 0 && this.bandG > 0) {
				geoRasterImg.setRed(this.bandR);
				geoRasterImg.setGreen(this.bandG);
				geoRasterImg.setBlue(this.bandB);
			}

			long[] outWindow = new long[4];
			img = geoRasterImg.getRasterImage(level, (long) intersect.y, (long) intersect.x,
					(long) (intersect.y + intersect.height - 1), (long) (intersect.x + intersect.width - 1), outWindow);

			BufferedImage bimg;
			Graphics2D bg = null;

			if (img == null) {
				//
				// TRICKY Look into pyramid level below or above if current raster block
				// is corrupted in database
				//
				LOG.warn("*** Loading Raster from {}:{}/{}.{}={} for level {} failed in the range of {}-{} / {}-{}",
						new Object[] { this.jdbcConnId, this.rasterTable, this.rasterRDTTable, this.rasterColumn,
								this.rasterId, level, intersect.y, intersect.x, (intersect.y + intersect.height - 1),
								(intersect.x + intersect.width - 1) });

				int newlevel;
				long nx, ny, nw, nh;
				if (level <= 0) {
					newlevel = 1;
					nx = intersect.x / 2;
					ny = intersect.y / 2;
					nw = intersect.width / 2;
					nh = intersect.height / 2;
				}
				else {
					newlevel = level - 1;
					nx = intersect.x * 2;
					ny = intersect.y * 2;
					nw = intersect.width * 2;
					nh = intersect.height * 2;
				}

				img = geoRasterImg.getRasterImage(newlevel, ny, nx, ny + nh - 1, nx + nw - 1, outWindow);

				bimg = getBufferedImage(intersect.width, intersect.height);
				bg = bimg.createGraphics();

				bg.drawImage((Image) img, 0, 0, intersect.width, intersect.height, null);
			}
			else {
				LOG.debug(" Image oW = {}, {}, {}, {}", outWindow);

				bimg = getBufferedImage(img.getWidth(), img.getHeight());
				bg = bimg.createGraphics();
				bg.drawImage((Image) img, 0, 0, null);
			}

			if (bg != null && this.debug > 0) {
				LOG.warn("Rendering additional debug graphics on top of raster data ({})", this.debug);
				if (this.debug == 1) {
					bg.setColor(DBG_NEXT_COLOR());
					// bg.setStroke( new java.awt.BasicStroke( 3 ) );
					// bg.drawRect( 1, 1, bimg.getWidth() - 3, bimg.getHeight() - 3 );
					bg.setStroke(new java.awt.BasicStroke(1));
					bg.drawRect(0, 0, bimg.getWidth() - 1, bimg.getHeight() - 1);
				}
				else if (this.debug == 2) {
					bg.setColor(DBG_NEXT_COLOR());
					bg.setStroke(new java.awt.BasicStroke(1));
					bg.drawRect(0, 0, 0, 0);
					bg.drawRect(bimg.getWidth() - 1, bimg.getHeight() - 1, 0, 0);
					bg.drawRect(bimg.getWidth() - 1, 0, 0, 0);
					bg.drawRect(0, bimg.getHeight() - 1, 0, 0);
				}
			}

			bg.dispose();
			img = bimg;

			if (LOG.isDebugEnabled()) {
				LOG.debug(" Image  W = ", img.getWidth());
				LOG.debug(" Image  H = ", img.getHeight());
			}

			ByteBufferRasterData rd = RasterFactory.rasterDataFromImage(img, null, result);
			res = new BufferResult(rd.getView(), result);
		}
		catch (Exception ex) {
			// ignore
			LOG.info("Exception on Loading", ex);
		}
		finally {
			JDBCUtils.close(con);
		}

		return res;
	}

	private static synchronized Color DBG_NEXT_COLOR() {
		DBG_COLOR_CNT++;
		if (DBG_COLOR_CNT >= DBG_COLORS.length)
			DBG_COLOR_CNT = 0;

		return DBG_COLORS[DBG_COLOR_CNT];
	}

	@Override
	public AbstractRaster load(File filename, RasterIOOptions options) throws IOException {
		throw new IOException("Reading GeoRaster from File is not supported");
	}

	@Override
	public AbstractRaster load(InputStream stream, RasterIOOptions options) throws IOException {
		throw new IOException("Reading GeoRaster from InputStream is not supported");
	}

	@Override
	public boolean canLoad(File filename) {
		return false;
	}

	@Override
	public Set<String> getSupportedFormats() {
		return Collections.emptySet();
	}

	@Override
	public boolean shouldCreateCacheFile() {
		return false;
	}

	@Override
	public File file() {
		// no file
		return null;
	}

	@Override
	public int getHeight() {
		return rasterRect.height;
	}

	@Override
	public int getWidth() {
		return rasterRect.width;
	}

	@Override
	public RasterGeoReference getGeoReference() {
		return rasterReference;
	}

	private BufferedImage getBufferedImage(int w, int h) {
		return new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
	}

	@Override
	public RasterDataInfo getRasterDataInfo() {

		synchronized (LOCK) {
			if (rdi == null) {
				//
				// Fixed RDI
				//
				rdi = new RasterDataInfo(new BandType[] { BandType.ALPHA, BandType.BLUE, BandType.GREEN, BandType.RED },
						DataType.BYTE, InterleaveType.PIXEL);
			}
		}

		return rdi;
	}

	@Override
	public boolean canReadTiles() {
		return true;
	}

	@Override
	public String getDataLocationId() {
		return dataLocationId;
	}

	@Override
	public void dispose() {

	}

	public boolean isMultiResulution() {
		return maxLevel > 0;
	}

	/**
	 * Return full Pyramid as array of AbstractRaster elements
	 * @return
	 */
	public AbstractRaster[] getPyramidRaster() {
		if (level != 0)
			return null;

		AbstractRaster[] res = new AbstractRaster[maxLevel];
		for (int lvl = 1; lvl <= maxLevel; lvl++) {
			res[lvl - 1] = new OracleGeorasterReader(workspace, this, lvl).getRaster();
		}
		return res;
	}

}
