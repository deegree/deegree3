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

package org.deegree.tools.crs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.coordinatesystems.IProjectedCRS;
import org.deegree.cs.persistence.deegree.db.DBCRSStore;
import org.deegree.cs.projections.IProjection;
import org.deegree.cs.projections.azimuthal.LambertAzimuthalEqualArea;
import org.deegree.cs.projections.azimuthal.StereographicAlternative;
import org.deegree.cs.projections.azimuthal.StereographicAzimuthal;
import org.deegree.cs.projections.conic.LambertConformalConic;
import org.deegree.cs.projections.cylindric.TransverseMercator;
import org.deegree.cs.transformations.TransformationFactory.DSTransform;
import org.deegree.tools.coverage.gridifier.RasterTreeGridifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>EPSGDBSynchronizer</code> class sets the Codes for CRS Identifiables that do
 * not have such codes (actually, since these code are required for CRS Identifiables to
 * exist, they are temporary).
 *
 * The connection parameters used are :
 * <ul>
 * <li>JDBC Driver: org.postgresql.Driver</li>
 * <li>Connection protocol: jdbc:postgresql://hurricane/epsg?user=postgres</li>
 * </ul>
 *
 * Currently the codes for the projections are set. The Axis element also need codes, but
 * the axis attributes values are quite different in the EPSG database and more work on
 * how to automate this process has to be done.
 *
 * <b> In order to succeed in changing the codetypes in the CRS database, the user must
 * set the variable CRS_DB_URL with the protocol jdbc:derby:... prior to running this
 * tool. At the moment of writing the database is at d3_core/bin/META-INF/deegreeCRS/</b>
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 *
 */
@Tool("Connects to the EPSG database at //hurricane/epsg, and for all projections (other objects to be added!) that have no CRS codetype in the CRS database, the codes are fetched from the EPSG database.")
public class EPSGDBSynchronizer {

	private static Logger LOG = LoggerFactory.getLogger(EPSGDBSynchronizer.class);

	// private static final String CRS_CONFIG =
	// "org.deegree.cs.configuration.deegree.db.DatabaseCRSProvider";

	private static final String JDBC_DRIVER = "org.postgresql.Driver";

	private static final String JDBC_CONNECTION_PATH = "jdbc:postgresql://hurricane/epsg?user=postgres";

	private Connection EPSGdbConn;

	private String epsgPath;

	protected void connectToEPSGdatabase() {
		try {
			Class.forName(JDBC_DRIVER);
			EPSGdbConn = DriverManager.getConnection(epsgPath);
		}
		catch (ClassNotFoundException e) {
			LOG.error(e.getMessage(), e);
		}
		catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	protected void getCodesForProjections(DBCRSStore dbSTore) {
		try {
			// select the EPSG code of all ProjectedCRS's, and their projection projection
			// codes and type
			PreparedStatement ps = EPSGdbConn.prepareStatement("SELECT coord_ref_sys_code, " + "coord_op_method_code, "
					+ "coord_op_code " + "FROM epsg_coordinatereferencesystem, " + "epsg_coordoperation "
					+ "WHERE coord_ref_sys_kind = 'projected' AND " + "projection_conv_code = coord_op_code");
			ResultSet largeRs = ps.executeQuery();
			while (largeRs.next()) {
				int projectedCRSCode = largeRs.getInt(1);
				int projectionTypeCode = largeRs.getInt(2);
				int projectionCode = largeRs.getInt(3);

				ICRS crs = dbSTore.getCRSByCode(new CRSCodeType(String.valueOf(projectedCRSCode), "EPSG"));

				if (crs != null && (crs instanceof IProjectedCRS)) {
					// if the projections are of the known types ( Transverse Mercator,
					// Lambert Azimuthal EA, Lambert
					// Conic Conformal, Stereographic alternative or Stereografic
					// azimuthal )
					if (projectionTypeCode == 9807 || projectionTypeCode == 9820 || projectionTypeCode == 9801
							|| projectionTypeCode == 9809 || projectionTypeCode == 9810) {
						IProjectedCRS projectedCRS = (IProjectedCRS) crs;
						IProjection projection = projectedCRS.getProjection();

						ps = EPSGdbConn.prepareStatement(
								"SELECT parameter_value, parameter_code FROM epsg_coordoperationparamvalue "
										+ "WHERE coord_op_code = " + projectionCode);
						ResultSet rs = ps.executeQuery();

						// initialize the specific projection attributes
						double latNatOrigin = Double.NaN;
						double longNatOrigin = Double.NaN;
						double scale = Double.NaN;
						double falseEasting = Double.NaN;
						double falseNorthing = Double.NaN;

						// get the attributes values according to their codes
						while (rs.next()) {
							if (rs.getInt(2) == 8801)
								latNatOrigin = rs.getDouble(1);
							else if (rs.getInt(2) == 8802)
								longNatOrigin = rs.getDouble(1);
							else if (rs.getInt(2) == 8805)
								scale = rs.getDouble(1);
							else if (rs.getInt(2) == 8806)
								falseEasting = rs.getDouble(1);
							else if (rs.getInt(2) == 8807)
								falseNorthing = rs.getDouble(1);
						}

						//
						// TRANSVERSE MERCATOR
						//
						if (projectionTypeCode == 9807 && (projection instanceof TransverseMercator)) {
							TransverseMercator tmProjection = (TransverseMercator) projection;
							// compare the CRS database projection attributes with the
							// attributes from the EPSG database
							if (java.lang.Math
								.abs(tmProjection.getNaturalOrigin().y - Math.toRadians(latNatOrigin)) < 1e-7
									&& java.lang.Math
										.abs(tmProjection.getNaturalOrigin().x - Math.toRadians(longNatOrigin)) < 1e-7
									&& tmProjection.getScale() == scale
									&& tmProjection.getFalseEasting() == falseEasting
									&& tmProjection.getFalseNorthing() == falseNorthing) {
								LOG.info(
										"The two Transverse Mercator projections attributes match. Updating the projection with Code: "
												+ projectionCode);

								// do the UPDATE
								// int pInternalID = dbProvider.getInternalID(
								// tmProjection );
								// dbProvider.setCode( pInternalID, String.valueOf(
								// projectionCode ) );
							}
						}

						//
						// LAMBERT AZIMUTHAL EQUAL AREA
						//
						if (projectionTypeCode == 9820 && (projection instanceof LambertAzimuthalEqualArea)) {
							LambertAzimuthalEqualArea laeaProjection = (LambertAzimuthalEqualArea) projection;
							// compare the CRS database projection attributes with the
							// attributes from the EPSG database
							if (java.lang.Math
								.abs(laeaProjection.getNaturalOrigin().y - Math.toRadians(latNatOrigin)) < 1e-7
									&& java.lang.Math
										.abs(laeaProjection.getNaturalOrigin().x - Math.toRadians(longNatOrigin)) < 1e-7
									&& laeaProjection.getFalseEasting() == falseEasting && // no
																							// scale
																							// comparison
																							// since
																							// no
									// scale attribute
									laeaProjection.getFalseNorthing() == falseNorthing) {
								LOG.info(
										"The two Lambert Azimuthal projections attributes match. Updating the projection with Code: "
												+ projectionCode);

								// do the UPDATE
								// int pInternalID = dbProvider.getInternalID(
								// laeaProjection );
								// dbProvider.setCode( pInternalID, String.valueOf(
								// projectionCode ) );
							}
						}

						//
						// LAMBERT CONIC CONFORMAL
						//
						if (projectionTypeCode == 9801 && (projection instanceof LambertConformalConic)) {
							LambertConformalConic lccProjection = (LambertConformalConic) projection;
							// compare the CRS database projection attributes with the
							// attributes from the EPSG database
							if (java.lang.Math
								.abs(lccProjection.getNaturalOrigin().y - Math.toRadians(latNatOrigin)) < 1e-7
									&& java.lang.Math
										.abs(lccProjection.getNaturalOrigin().x - Math.toRadians(longNatOrigin)) < 1e-7
									&& lccProjection.getScale() == scale
									&& lccProjection.getFalseEasting() == falseEasting
									&& lccProjection.getFalseNorthing() == falseNorthing) {
								LOG.info(
										"The two Lambert Conic Conformal projections attributes match. Updating the projection with the Code "
												+ projectionCode);

								// do the UPDATE
								// int pInternalID = dbProvider.getInternalID(
								// lccProjection );
								// dbProvider.setCode( pInternalID, String.valueOf(
								// projectionCode ) );
							}
						}

						//
						// STEREOGRAPHIC
						//
						if (projectionTypeCode == 9809 || projectionTypeCode == 9810) {
							if (projection instanceof StereographicAlternative) {
								StereographicAlternative salProjection = (StereographicAlternative) projection;
								// compare the CRS database projection attributes with the
								// attributes from the EPSG
								// database
								if (java.lang.Math
									.abs(salProjection.getNaturalOrigin().y - Math.toRadians(latNatOrigin)) < 0.00001
										&& java.lang.Math.abs(salProjection.getNaturalOrigin().x
												- Math.toRadians(longNatOrigin)) < 0.00001
										&& salProjection.getScale() == scale
										&& salProjection.getFalseEasting() == falseEasting
										&& salProjection.getFalseNorthing() == falseNorthing) {
									LOG.info(
											"The two StereographicAlternative projections attributes match. Updating the projection with the Code: "
													+ projectionCode);

									// do the UPDATE
									// int pInternalID = dbProvider.getInternalID(
									// salProjection );
									// dbProvider.setCode( pInternalID, String.valueOf(
									// projectionCode ) );
								}
							}
							else if (projection instanceof StereographicAzimuthal) {
								StereographicAzimuthal sazProjection = (StereographicAzimuthal) projection;
								// compare the CRS database projection attributes with the
								// attributes from the EPSG
								// database
								if (java.lang.Math
									.abs(sazProjection.getNaturalOrigin().y - Math.toRadians(latNatOrigin)) < 0.00001
										&& java.lang.Math.abs(sazProjection.getNaturalOrigin().x
												- Math.toRadians(longNatOrigin)) < 0.00001
										&& sazProjection.getScale() == scale
										&& sazProjection.getFalseEasting() == falseEasting
										&& sazProjection.getFalseNorthing() == falseNorthing) {
									LOG.info(
											"The two StereographicAzimuthal projections attributes match. Updating the projectio with the Code: "
													+ projectionCode);

									// do the UPDATE
									// int pInternalID = dbProvider.getInternalID(
									// sazProjection );
									// dbProvider.setCode( pInternalID, String.valueOf(
									// projectionCode ) );
								}
							}
						}
					}
				}
			}
		}
		catch (SQLException e) {
			LOG.error(e.getMessage());
		}
	}

	// ---------- Still to be worked upon, since the EPSG data for Axis is pretty
	// different in form. Need to make a
	// semantic correspondence in order to automate this.
	// protected void getCodesForAxes( DatabaseCRSProvider dbProvider ) throws
	// SQLException {
	// PreparedStatement ps = EPSGdbConn.prepareStatement(
	// "SELECT coord_sys_code, " +
	// "coord_axis_orientation, " +
	// "coord_axis_name, unit_of_meas_name " +
	// "FROM epsg_coordinateaxisname, epsg_coordinateaxis, epsg_unitofmeasure " +
	// "WHERE epsg_unitofmeasure.uom_code = epsg_coordinateaxis.uom_code " +
	// "AND epsg_coordinateaxis.coord_axis_name_code =
	// epsg_coordinateaxisname.coord_axis_name_code ");
	// ResultSet rs = ps.executeQuery();
	//
	// while ( rs.next() ) {
	// EPSGCode code = new EPSGCode( rs.getInt( 1 ) );
	//
	// String axisOrientation = rs.getString( 2 );
	// // do some adjusting of the string to our in-house names
	// if ( axisOrientation.toLowerCase().contains( "north" ) )
	// axisOrientation = "north";
	// else if ( axisOrientation.toLowerCase().contains( "east" ) )
	// axisOrientation = "east";
	// else if ( axisOrientation.toLowerCase().contains( "up" ) )
	// axisOrientation = "up";
	//
	// String axisName = rs.getString( 3 );
	// // do some adjusting of the string to our in-house names
	// if ( axisName.toLowerCase().contains( "longitude" ) )
	// axisName = "longitude";
	// else if ( axisName.toLowerCase().contains( "latitude" ) )
	// axisName = "latitude";
	// else if ( axisName.toLowerCase().contains( "height" ) )
	// axisName = "height";
	// else if ( axisName.toLowerCase().contains( "x" ) )
	// axisName = "x";
	// else if ( axisName.toLowerCase().contains( "y" ) )
	// axisName = "y";
	//
	// Unit uom = Unit.createUnitFromString( rs.getString( 4 ) );
	//
	// dbProvider.changeAxisCode( axisName, axisOrientation, uom, code );
	// }
	// }

	public EPSGDBSynchronizer() {
		// nothing necessary yet
	}

	protected void closeEPSGConnection() throws SQLException {
		EPSGdbConn.close();
	}

	static private Options initOptions() {
		Options options = new Options();

		Option opt = new Option("epsgPath", true, "path to epsg database");
		options.addOption(opt);

		CommandUtils.addDefaultOptions(options);

		return options;
	}

	private static void printHelp(Options options) {
		CommandUtils.printHelp(options, RasterTreeGridifier.class.getSimpleName(), null, "file/dir [file/dir(s)]");
	}

	static public void main(String[] args) {
		EPSGDBSynchronizer sync = new EPSGDBSynchronizer();

		Options options = initOptions();

		try {
			CommandLine line = new PosixParser().parse(options, args);

			if (line.hasOption("help")) {
				printHelp(options);
			}

			sync.epsgPath = options.getOption("epsgPath").getValue();

			if (sync.epsgPath == null || sync.epsgPath.trim().length() == 0) {
				sync.epsgPath = JDBC_CONNECTION_PATH;
				LOG.info("Using the default JDBC_CONNECTION_PATH: " + JDBC_CONNECTION_PATH);
			}

			sync.connectToEPSGdatabase();

			// get the CRS database provider
			DBCRSStore dbStore = new DBCRSStore(DSTransform.HELMERT);

			sync.getCodesForProjections(dbStore);

			// synchronizer.getCodesForAxes( dbProvider );

		}
		catch (ParseException e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		}

	}

}
