package org.deegree.cs.transformations;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.EPSGCode;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.Ellipsoid;
import org.deegree.cs.components.GeodeticDatum;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.GeocentricCRS;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ProjectedCRS;
import org.deegree.cs.projections.Projection;
import org.deegree.cs.projections.azimuthal.StereographicAlternative;
import org.deegree.cs.projections.cylindric.TransverseMercator;
import org.deegree.cs.transformations.helmert.Helmert;

/**
 * 
 * An interface defining Coordinate systems which are unrelated to the configuration. With these coordinate systems the
 * {@link TransformationFactory} can be tested for functionality based on a defined (static) state.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface CRSDefines {
    /** the epsilon for meter based crs */
    public final static double METER_EPSILON = 0.15;

    /** the epsilon for degree based crs */
    public final static double DEGREE_EPSILON = 0.0000015;

    /** the epsilon for meter based crs */
    public final static Point3d EPSILON_M = new Point3d( METER_EPSILON, METER_EPSILON, 0.4 );

    /** the epsilon for degree based crs */
    public final static Point3d EPSILON_D = new Point3d( DEGREE_EPSILON, DEGREE_EPSILON, 0.4 );

    /**
     * Used axis
     */
    /** Axis for geographic crs, degree, lon/lat */
    public final static Axis[] axis_degree = new Axis[] { new Axis( Unit.DEGREE, "lon", Axis.AO_EAST ),
                                                         new Axis( Unit.DEGREE, "lat", Axis.AO_NORTH ) };

    /** Axis for geographic crs, degree, lat/lon */
    public final static Axis[] axis_lat_lon = new Axis[] { new Axis( Unit.DEGREE, "lat", Axis.AO_NORTH ),
                                                          new Axis( Unit.DEGREE, "lon", Axis.AO_EAST ) };

    /** Axis for projections, meter, x (east)/y(north) */
    public final static Axis[] axis_projection = new Axis[] { new Axis( "x", Axis.AO_EAST ),
                                                             new Axis( "y", Axis.AO_NORTH ) };

    /** Axis for projections, meter, y(north)/x (east) */
    public final static Axis[] axis_y_x = new Axis[] { new Axis( "y", Axis.AO_NORTH ), new Axis( "x", Axis.AO_EAST ) };

    /** Axis for geocentric, X(front), Y( east ), Z( north) */
    public final static Axis[] axis_geocentric = new Axis[] { new Axis( Unit.METRE, "X", Axis.AO_FRONT ),
                                                             new Axis( Unit.METRE, "Y", Axis.AO_EAST ),
                                                             new Axis( Unit.METRE, "Z", Axis.AO_NORTH ) };

    /** One sole axis for compound, z, metre, up */
    public final static Axis heightAxis = new Axis( Unit.METRE, "z", Axis.AO_UP );

    /*
     * Used ellipsoids
     */

    /**
     * (bessel) used for {@link #datum_6289} ({@link #projection_28992}), and {@link #datum_6314} (
     * {@link #geographic_4314} )
     */
    public final static Ellipsoid ellipsoid_7004 = new Ellipsoid( 6377397.155, Unit.METRE, 299.1528128,
                                                                  new EPSGCode[] { new EPSGCode( 7004 ) } );

    /** Used for {@link #geocentric_4964} */
    public final static Ellipsoid ellipsoid_7019 = new Ellipsoid( 6378137.0, Unit.METRE, 298.257222101,
                                                                  new EPSGCode[] { new EPSGCode( 7019 ) } );

    /*
     * Used datums
     */

    /** Used for {@link #geographic_4289} */
    public final static GeodeticDatum datum_6289 = new GeodeticDatum( ellipsoid_7004, null /* wgs_1672 */,
                                                                      new EPSGCode[] { new EPSGCode( 6289 ) } );

    /** Used for {@link #geographic_4258} */
    public final static GeodeticDatum datum_6258 = new GeodeticDatum( ellipsoid_7019, null /* wgs_1188 */,
                                                                      new EPSGCode[] { new EPSGCode( 6258 ) } );

    /** Used for {@link #geocentric_dummy} and {@link #geographic_4314} */
    public final static GeodeticDatum datum_6314 = new GeodeticDatum( ellipsoid_7004, null /* wgs_1777 */,
                                                                      new EPSGCode[] { new EPSGCode( 6314 ) } );

    /** Used for {@value #geocentric_4964} */
    public final static GeodeticDatum datum_6171 = new GeodeticDatum( ellipsoid_7019, null /* wgs_1188 */,
                                                                      new EPSGCode[] { new EPSGCode( 6717 ) } );

    /*
     * Used geocentric crs's
     */

    /** geocentric crs with {@link #datum_6171} and {@link #axis_geocentric} */
    public final static GeocentricCRS geocentric_4964 = new GeocentricCRS( datum_6171, axis_geocentric,
                                                                           new EPSGCode( 4964 ) );

    /** a none existing geocentric crs with {@link #datum_6314} and {@link #axis_geocentric} */
    public final static GeocentricCRS geocentric_dummy = new GeocentricCRS( datum_6314, axis_geocentric,
                                                                            new CRSCodeType( "NO_REAL_GEOCENTRIC" ) );

    /**
     * Used geographic crs's
     */

    /** Geographic crs with {@link #datum_6289} and {@link #axis_degree} */
    public final static GeographicCRS geographic_4289 = new GeographicCRS( datum_6289, axis_degree,
                                                                           new EPSGCode[] { new EPSGCode( 4289 ) } );

    /** Geographic crs with {@link #datum_6289} and {@link #axis_lat_lon} */
    public final static GeographicCRS geographic_4289_lat_lon = new GeographicCRS(
                                                                                   datum_6289,
                                                                                   axis_lat_lon,
                                                                                   new EPSGCode[] { new EPSGCode( 4289 ) } );

    /** Geographic crs with {@link #datum_6258} and {@link #axis_degree} */
    public final static GeographicCRS geographic_4258 = new GeographicCRS( datum_6258, axis_degree,
                                                                           new EPSGCode[] { new EPSGCode( 4258 ) },
                                                                           null /* names */, null/* version */,
                                                                           null/* description */,
                                                                           new String[] { "-10.67,34.5,31.55,71.05" } );

    /** Geographic crs with {@link #datum_6258} and {@link #axis_lat_lon} */
    public final static GeographicCRS geographic_4258_lat_lon = new GeographicCRS(
                                                                                   datum_6258,
                                                                                   axis_lat_lon,
                                                                                   new EPSGCode[] { new EPSGCode( 4258 ) },
                                                                                   null /* names */,
                                                                                   null/* version */,
                                                                                   null/* description */,
                                                                                   new String[] { "-10.67,34.5,31.55,71.05" } );

    /** Geographic crs with {@link #datum_6314} and {@link #axis_degree} */
    public final static GeographicCRS geographic_4314 = new GeographicCRS( datum_6314, axis_degree,
                                                                           new EPSGCode[] { new EPSGCode( 4314 ) },
                                                                           null /* names */, null/* version */,
                                                                           null/* description */,
                                                                           new String[] { "5.87,47.27,13.83,55.04" } );

    /** Geographic crs with {@link #datum_6314} and {@link #axis_lat_lon} (northing/easting) */
    public final static GeographicCRS geographic_4314_lat_lon = new GeographicCRS(
                                                                                   datum_6314,
                                                                                   axis_lat_lon,
                                                                                   new EPSGCode[] { new EPSGCode( 4314 ) },
                                                                                   null /* names */,
                                                                                   null/* version */,
                                                                                   null/* description */,
                                                                                   new String[] { "5.87,47.27,13.83,55.04" } );

    /*
     * Used to wgs
     */
    /** The parameters to go from {@link #datum_6289} -> wgs48 */
    public final static Helmert wgs_1672 = new Helmert( 565.04, 49.91, 465.84, -0.40941295127179994,
                                                        0.3608190255680464, -1.8684910003505757, 4.0772,
                                                        geographic_4289, GeographicCRS.WGS84,
                                                        new EPSGCode[] { new EPSGCode( 1672 ) } );

    /** The null parameters defined for wgs48, {@link #datum_6258} and {@link #datum_6171} */
    public final static Helmert wgs_1188 = new Helmert( GeographicCRS.WGS84, GeographicCRS.WGS84,
                                                        new CRSCodeType[] { new CRSCodeType( "1188" ) } );

    /** The parameters to go from {@link #datum_6314} -> wgs48 */
    public final static Helmert wgs_1777 = new Helmert( 598.1, 73.7, 418.2, 0.202, 0.045, -2.455, 6.7, geographic_4314,
                                                        GeographicCRS.WGS84,
                                                        new CRSCodeType[] { new CRSCodeType( "1777" ) } );

    /*
     * Used projections
     */
    /** {@link StereographicAlternative} projection with {@link GeographicCRS} {@link #geographic_4289}. */
    public final static Projection projection_28992 = new StereographicAlternative(
                                                                                    geographic_4289,
                                                                                    463000.0,
                                                                                    155000.0,
                                                                                    new Point2d(
                                                                                                 Math.toRadians( 5.38763888888889 ),
                                                                                                 Math.toRadians( 52.15616055555555 ) ),
                                                                                    Unit.METRE,
                                                                                    0.9999079,
                                                                                    new CRSIdentifiable(
                                                                                                         new EPSGCode(
                                                                                                                       19914 ) ) );

    /** {@link StereographicAlternative} projection with {@link GeographicCRS} {@link #geographic_4289_lat_lon}. */
    public final static Projection projection_28992_lat_lon = new StereographicAlternative(
                                                                                            geographic_4289_lat_lon,
                                                                                            463000.0,
                                                                                            155000.0,
                                                                                            new Point2d(
                                                                                                         Math.toRadians( 5.38763888888889 ),
                                                                                                         Math.toRadians( 52.15616055555555 ) ),
                                                                                            Unit.METRE,
                                                                                            0.9999079,
                                                                                            new CRSIdentifiable(
                                                                                                                 new EPSGCode(
                                                                                                                               19914 ) ) );

    /** {@link TransverseMercator} projection with {@link GeographicCRS} {@link #geographic_4258}. */
    public final static Projection projection_25832 = new TransverseMercator(
                                                                              true,
                                                                              geographic_4258,
                                                                              0,
                                                                              500000.0,
                                                                              new Point2d( Math.toRadians( 9 ), 0 ),
                                                                              Unit.METRE,
                                                                              0.9996,
                                                                              new CRSIdentifiable( new EPSGCode( 16032 ) ) );

    /** {@link TransverseMercator} projection with {@link GeographicCRS} {@link #geographic_4258_lat_lon}. */
    public final static Projection projection_25832_lat_lon = new TransverseMercator(
                                                                                      true,
                                                                                      geographic_4258_lat_lon,
                                                                                      0,
                                                                                      500000.0,
                                                                                      new Point2d( Math.toRadians( 9 ),
                                                                                                   0 ),
                                                                                      Unit.METRE,
                                                                                      0.9996,
                                                                                      new CRSIdentifiable(
                                                                                                           new EPSGCode(
                                                                                                                         16032 ) ) );

    /** {@link TransverseMercator} projection with {@link GeographicCRS} {@link #geographic_4314}. */
    public final static Projection projection_31467 = new TransverseMercator(
                                                                              geographic_4314,
                                                                              0,
                                                                              3500000.0,
                                                                              new Point2d( Math.toRadians( 9 ),
                                                                                           Math.toRadians( 0 ) ),
                                                                              Unit.METRE,
                                                                              new CRSIdentifiable( new EPSGCode( 16263 ) ) );

    /** {@link TransverseMercator} projection with {@link GeographicCRS} {@link #geographic_4314_lat_lon}. */
    public final static Projection projection_31467_lat_lon = new TransverseMercator(
                                                                                      geographic_4314_lat_lon,
                                                                                      0,
                                                                                      3500000.0,
                                                                                      new Point2d( Math.toRadians( 9 ),
                                                                                                   Math.toRadians( 0 ) ),
                                                                                      Unit.METRE,
                                                                                      new CRSIdentifiable(
                                                                                                           new EPSGCode(
                                                                                                                         16263 ) ) );

    /*
     * Used projected crs's
     */

    /** {@link ProjectedCRS} based on {@link #projection_28992}, which is based on {@link #geographic_4289} */
    public final static ProjectedCRS projected_28992 = new ProjectedCRS( projection_28992, axis_projection,
                                                                         new EPSGCode[] { new EPSGCode( 28992 ) } );

    /** {@link ProjectedCRS} based on {@link #projection_28992}, which is based on {@link #geographic_4289} */
    public final static ProjectedCRS projected_28992_lat_lon = new ProjectedCRS(
                                                                                 projection_28992_lat_lon,
                                                                                 axis_projection,
                                                                                 new EPSGCode[] { new EPSGCode( 28992 ) } );

    /** {@link ProjectedCRS} based on {@link #projection_28992}, which is based on {@link #geographic_4289} */
    public final static ProjectedCRS projected_28992_yx = new ProjectedCRS( projection_28992, axis_y_x,
                                                                            new EPSGCode[] { new EPSGCode( 28992 ) } );

    /** {@link ProjectedCRS} based on {@link #projection_25832}, which is based on {@link #geographic_4258} */
    public final static ProjectedCRS projected_25832 = new ProjectedCRS( projection_25832, axis_projection,
                                                                         new EPSGCode[] { new EPSGCode( 25832 ) },
                                                                         null /* names */, null/* version */,
                                                                         null/* description */,
                                                                         new String[] { "5.05,57.9,12.0,65.67" } );

    /** {@link ProjectedCRS} based on {@link #projection_25832}, which is based on {@link #geographic_4258} */
    public final static ProjectedCRS projected_25832_lat_lon = new ProjectedCRS(
                                                                                 projection_25832_lat_lon,
                                                                                 axis_projection,
                                                                                 new EPSGCode[] { new EPSGCode( 25832 ) },
                                                                                 null /* names */,
                                                                                 null/* version */,
                                                                                 null/* description */,
                                                                                 new String[] { "5.05,57.9,12.0,65.67" } );

    /** {@link ProjectedCRS} based on {@link #projection_25832}, which is based on {@link #geographic_4258} */
    public final static ProjectedCRS projected_25832_yx = new ProjectedCRS( projection_25832, axis_y_x,
                                                                            new EPSGCode[] { new EPSGCode( 25832 ) },
                                                                            null /* names */, null/* version */,
                                                                            null/* description */,
                                                                            new String[] { "5.05,57.9,12.0,65.67" } );

    /** {@link ProjectedCRS} based on {@link #projection_31467}, which is based on {@link #geographic_4314} */
    public final static ProjectedCRS projected_31467 = new ProjectedCRS( projection_31467, axis_projection,
                                                                         new EPSGCode[] { new EPSGCode( 31467 ) },
                                                                         null /* names */, null/* version */,
                                                                         null/* description */,
                                                                         new String[] { "7.5,47.27,10.5,55.06" } );

    /**
     * {@link ProjectedCRS} based on {@link #projection_31467}, which is based on {@link #geographic_4314} with lat/lon
     * axis
     */
    public final static ProjectedCRS projected_31467_lat_lon = new ProjectedCRS(
                                                                                 projection_31467_lat_lon,
                                                                                 axis_projection,
                                                                                 new EPSGCode[] { new EPSGCode( 31467 ) },
                                                                                 null /* names */,
                                                                                 null/* version */,
                                                                                 null/* description */,
                                                                                 new String[] { "7.5,47.27,10.5,55.06" } );

    /**
     * {@link ProjectedCRS} based on {@link #projection_31467} with yx coordinates, which is based on
     * {@link #geographic_4314}
     */
    public final static ProjectedCRS projected_31467_yx = new ProjectedCRS( projection_31467, axis_y_x,
                                                                            new EPSGCode[] { new EPSGCode( 31467 ) },
                                                                            null /* names */, null/* version */,
                                                                            null/* description */,
                                                                            new String[] { "7.5,47.27,10.5,55.06" } );

}
