//$HeadURL: http://svn.wald.intevation.org/svn/deegree/deegree3/commons/trunk/test/org/deegree/model/crs/transformations/TransformationTest.java $
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

package org.deegree.cs.transformations;

import static java.lang.Double.NaN;

import java.util.Collections;

import javax.vecmath.Point3d;

import junit.framework.Assert;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.CoordinateTransformer;
import org.deegree.cs.coordinatesystems.CompoundCRS;
import org.deegree.cs.coordinatesystems.GeocentricCRS;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.coordinatesystems.ProjectedCRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.persistence.CRSManager;
import org.junit.Test;

/**
 * A JUnit test class for testing the accuracy of various transformations, thus testing the functionality of the
 * Transformation factory independent of any underlying configuration.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author: rbezema $
 * 
 * @version $Revision: 15508 $, $Date: 2009-01-06 12:08:22 +0100 (Tue, 06 Jan 2009) $
 * 
 */
public class TransformationAccuracyTest extends TransformationAccuracy implements CRSDefines {

    static {
        datum_6289.setToWGS84( wgs_1672 );
        datum_6258.setToWGS84( wgs_1188 );
        datum_6314.setToWGS84( wgs_1777 );
        datum_6171.setToWGS84( wgs_1188 );
    }

    /**
     * Test the forward/inverse transformation from a compound_projected crs (EPSG:28992) to another compound_projected
     * crs (EPSG:25832)
     * 
     * @throws TransformationException
     */
    @Test
    public void testCompoundToCompound()
                            throws TransformationException {
        // Source crs espg:28992
        CompoundCRS sourceCRS = new CompoundCRS(
                                                 heightAxis,
                                                 projected_28992,
                                                 20,
                                                 new CRSIdentifiable(
                                                                      new CRSCodeType[] { new CRSCodeType(
                                                                                                           projected_28992.getCode().getOriginal()
                                                                                                                                   + "_compound" ) } ) );

        // Target crs espg:25832
        CompoundCRS targetCRS = new CompoundCRS(
                                                 heightAxis,
                                                 projected_25832,
                                                 20,
                                                 new CRSIdentifiable(
                                                                      new CRSCodeType[] { new CRSCodeType(
                                                                                                           projected_25832.getCode().getOriginal()
                                                                                                                                   + "_compound" ) } ) );

        // reference created with coord tool from http://www.rdnap.nl/ (NL/Amsterdam/dam)
        Point3d sourcePoint = new Point3d( 121397.572, 487325.817, 6.029 );
        Point3d targetPoint = new Point3d( 220513.823, 5810438.891, 49 );
        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_M, EPSILON_M );
    }

    /**
     * Test the transformation from a compound_projected crs (EPSG:28992_compound) to a geographic crs (EPSG:4258)
     * coordinate system .
     * 
     * @throws TransformationException
     */
    @Test
    public void testCompoundToGeographic()
                            throws TransformationException {

        // Source crs espg:28992
        CompoundCRS sourceCRS = new CompoundCRS(
                                                 heightAxis,
                                                 projected_28992,
                                                 20,
                                                 new CRSIdentifiable(
                                                                      new CRSCodeType[] { new CRSCodeType(
                                                                                                           projected_28992.getCode().getOriginal()
                                                                                                                                   + "_compound" ) } ) );

        // Target crs espg:4258
        GeographicCRS targetCRS = geographic_4258;

        // reference created with coord tool from http://www.rdnap.nl/ denoting (NL/Groningen/lichtboei)
        Point3d sourcePoint = new Point3d( 236694.856, 583952.500, 1.307 );
        Point3d targetPoint = new Point3d( 6.610765, 53.235916, 42 );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_D, new Point3d( METER_EPSILON,
                                                                                                     0.17, 0.6 ) );
    }

    /**
     * Test the transformation from a compound_projected crs (EPSG:28992_compound) to a geographic crs (EPSG:4258)
     * coordinate system .
     * 
     * @throws TransformationException
     */
    @Test
    public void testCompoundToGeographicEqualOnEllips()
                            throws TransformationException {
        // urn:x-ogc:def:crs:EPSG:4979
        // urn:x-ogc:def:crs:EPSG:4326
        // EPSG:4326
        CompoundCRS sourceCRS = new CompoundCRS(
                                                 heightAxis,
                                                 GeographicCRS.WGS84_YX,
                                                 20,
                                                 new CRSIdentifiable(
                                                                      new CRSCodeType[] { new CRSCodeType(
                                                                                                           "urn:x-ogc:def:crs:EPSG:4979" ) } ) );

        // Target crs espg:4258
        GeographicCRS targetCRS = GeographicCRS.WGS84_YX;

        // taken from wfs cite 1.1.0 test
        Point3d sourcePoint = new Point3d( 46.074, 9.799, 600.2 );
        Point3d targetPoint = new Point3d( 46.074, 9.799, Double.NaN );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_D, EPSILON_D );
    }

    /**
     * Test the transformation from a compound_projected crs (EPSG:28992_compound) to a geographic crs (EPSG:4258)
     * coordinate system .
     * 
     * @throws TransformationException
     */
    @Test
    public void testCompoundToGeographicEqualOnEllipsNotOnAxis()
                            throws TransformationException {
        // urn:x-ogc:def:crs:EPSG:4979
        // urn:x-ogc:def:crs:EPSG:4326
        // EPSG:4326
        CompoundCRS sourceCRS = new CompoundCRS(
                                                 heightAxis,
                                                 GeographicCRS.WGS84_YX,
                                                 600.2,
                                                 new CRSIdentifiable(
                                                                      new CRSCodeType[] { new CRSCodeType(
                                                                                                           "urn:x-ogc:def:crs:EPSG:4979" ) } ) );

        // Target crs espg:4258
        GeographicCRS targetCRS = GeographicCRS.WGS84;

        // taken from wfs cite 1.1.0 test
        Point3d sourcePoint = new Point3d( 46.074, 9.799, 600.2 );
        Point3d targetPoint = new Point3d( 9.799, 46.074, Double.NaN );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_D, EPSILON_D );
    }

    /**
     * Test the forward/inverse transformation from a compound_projected crs (EPSG:31467) to a geocentric crs
     * (EPSG:4964)
     * 
     * @throws TransformationException
     */
    @Test
    public void testCompoundToGeocentric()
                            throws TransformationException {

        // source crs epsg:31467
        CompoundCRS sourceCRS = new CompoundCRS(
                                                 heightAxis,
                                                 projected_31467,
                                                 20,
                                                 new CRSIdentifiable(
                                                                      new CRSCodeType[] { new CRSCodeType(
                                                                                                           projected_31467.getCode().getOriginal()
                                                                                                                                   + "_compound" ) } ) );

        // Target crs EPSG:4964
        GeocentricCRS targetCRS = geocentric_4964;

        // do the testing
        Point3d sourcePoint = new Point3d( 3532465.57, 5301523.49, 817 );
        Point3d targetPoint = new Point3d( 4230602.192492622, 702858.4858986374, 4706428.360722791 );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_M, EPSILON_M );
    }

    /**
     * Test the forward/inverse transformation from a compound_geographic crs (EPSG:4326) to a projected crs
     * (EPSG:31467)
     * 
     * @throws TransformationException
     */
    @Test
    public void testCompoundToProjected()
                            throws TransformationException {

        // Source WGS:84_compound
        CompoundCRS sourceCRS = new CompoundCRS(
                                                 heightAxis,
                                                 GeographicCRS.WGS84,
                                                 20,
                                                 new CRSIdentifiable(
                                                                      new CRSCodeType[] { new CRSCodeType(
                                                                                                           GeographicCRS.WGS84.getCode().getOriginal()
                                                                                                                                   + "_compound" ) } ) );

        // Target EPSG:31467
        ProjectedCRS targetCRS = projected_31467;

        // kind regards to vodafone for supplying reference points.
        Point3d sourcePoint = new Point3d( 9.432778, 47.851111, 870.6 );
        Point3d targetPoint = new Point3d( 3532465.57, 5301523.49, 817 );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_M, EPSILON_D );
    }

    /**
     * Test the forward/inverse transformation from a projected crs (EPSG:28992) to another projected crs (EPSG:25832)
     * 
     * @throws TransformationException
     */
    @Test
    public void testProjectedToProjected()
                            throws TransformationException {
        // Source crs espg:28992
        ProjectedCRS sourceCRS = projected_28992;

        // Target crs espg:25832
        ProjectedCRS targetCRS = projected_25832;

        // reference created with coord tool from http://www.rdnap.nl/ (NL/hoensbroek)
        Point3d sourcePoint = new Point3d( 191968.31999475454, 326455.285005203, Double.NaN );
        Point3d targetPoint = new Point3d( 283065.845, 5646206.125, Double.NaN );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_M, EPSILON_M );
    }

    /**
     * Test the forward/inverse transformation from a projected crs (EPSG:28992) to another projected crs (EPSG:25832)
     * 
     * @throws TransformationException
     */
    @Test
    public void testProjectedToProjectedWithLatLonGeoAxis()
                            throws TransformationException {
        // Source crs espg:28992
        ProjectedCRS sourceCRS = projected_28992_lat_lon;

        // Target crs espg:25832
        ProjectedCRS targetCRS = projected_25832_lat_lon;

        // reference created with coord tool from http://www.rdnap.nl/ (NL/hoensbroek)
        Point3d sourcePoint = new Point3d( 191968.31999475454, 326455.285005203, Double.NaN );
        Point3d targetPoint = new Point3d( 283065.845, 5646206.125, Double.NaN );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_M, EPSILON_M );
    }

    /**
     * Test the forward/inverse transformation from a projected crs (EPSG:28992) to another projected crs (EPSG:25832)
     * 
     * @throws TransformationException
     */
    @Test
    public void testProjectedToProjectedWithYX()
                            throws TransformationException {
        // Source crs espg:28992
        ProjectedCRS sourceCRS = projected_28992_yx;

        // Target crs espg:25832
        ProjectedCRS targetCRS = projected_25832;

        // reference created with coord tool from http://www.rdnap.nl/ (NL/hoensbroek)
        Point3d sourcePoint = new Point3d( 326455.285005203, 191968.31999475454, Double.NaN );
        Point3d targetPoint = new Point3d( 283065.845, 5646206.125, Double.NaN );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_M, EPSILON_M );
    }

    /**
     * Test the forward/inverse transformation from a projected crs (EPSG:31467) to a geographic crs (EPSG:4258)
     * 
     * @throws TransformationException
     */
    @Test
    public void testProjectedToGeographic()
                            throws TransformationException {
        // Source crs espg:31467
        ProjectedCRS sourceCRS = projected_31467;

        // Target crs espg:4258
        GeographicCRS targetCRS = geographic_4258;

        // with kind regards to vodafone for supplying reference points
        Point3d sourcePoint = new Point3d( 3532465.57, 5301523.49, Double.NaN );
        Point3d targetPoint = new Point3d( 9.432778, 47.851111, Double.NaN );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_D, EPSILON_M );
    }

    /**
     * Test the forward/inverse transformation from a projected crs (EPSG:31467) with yx axis order to a geographic crs
     * (EPSG:4258).
     * 
     * @throws TransformationException
     */
    @Test
    public void testProjectedWithYXToGeographic()
                            throws TransformationException {
        // Source crs espg:31467
        ProjectedCRS sourceCRS = projected_31467_yx;

        // Target crs espg:4258
        GeographicCRS targetCRS = geographic_4258;

        // with kind regards to vodafone for supplying reference points
        Point3d sourcePoint = new Point3d( 5301523.49, 3532465.57, Double.NaN );
        Point3d targetPoint = new Point3d( 9.432778, 47.851111, Double.NaN );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_D, EPSILON_M );
    }

    /**
     * Test the forward/inverse transformation from a projected crs (EPSG:31467) based on a lat/lon geographic crs to a
     * geographic crs (EPSG:4258).
     * 
     * @throws TransformationException
     */
    @Test
    public void testProjectedOnGeoLatLonToGeographic()
                            throws TransformationException {
        // Source crs espg:31467
        ProjectedCRS sourceCRS = projected_31467_lat_lon;

        // Target crs espg:4258
        GeographicCRS targetCRS = geographic_4258;

        // with kind regards to vodafone for supplying reference points
        Point3d sourcePoint = new Point3d( 3532465.57, 5301523.49, Double.NaN );
        Point3d targetPoint = new Point3d( 9.432778, 47.851111, Double.NaN );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_D, EPSILON_M );
    }

    /**
     * Test the forward/inverse transformation from a projected crs (EPSG:31467) to a geographic crs (EPSG:4258) which
     * has lat/lon axis
     * 
     * @throws TransformationException
     */
    @Test
    public void testProjectedToGeographicLatLon()
                            throws TransformationException {
        // Source crs espg:31467
        ProjectedCRS sourceCRS = projected_31467;

        // Target crs espg:4258
        GeographicCRS targetCRS = geographic_4258_lat_lon;

        // with kind regards to vodafone for supplying reference points
        Point3d sourcePoint = new Point3d( 3532465.57, 5301523.49, Double.NaN );
        Point3d targetPoint = new Point3d( 47.851111, 9.432778, Double.NaN );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_D, EPSILON_M );
    }

    /**
     * Test the forward/inverse transformation from a projected crs (EPSG:28992) to a geocentric crs (EPSG:4964)
     * 
     * @throws TransformationException
     */
    @Test
    public void testProjectedToGeocentric()
                            throws TransformationException {
        ProjectedCRS sourceCRS = projected_28992;

        // Target crs EPSG:4964
        GeocentricCRS targetCRS = geocentric_4964;

        // do the testing created reference points with deegree (not a fine test!!)
        Point3d sourcePoint = new Point3d( 191968.31999475454, 326455.285005203, Double.NaN );
        Point3d targetPoint = new Point3d( 4006964.9993508584, 414997.8479008863, 4928439.8089122595 );

        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_M, EPSILON_M );
    }

    /**
     * Test the forward/inverse transformation from a geographic crs (EPSG:4314) to another geographic crs (EPSG:4258)
     * 
     * @throws TransformationException
     */
    @Test
    public void testGeographicToGeographic()
                            throws TransformationException {

        // source crs epsg:4314
        GeographicCRS sourceCRS = geographic_4314;
        // target crs epsg:4258
        GeographicCRS targetCRS = geographic_4258;

        // with kind regards to vodafone for supplying reference points.
        Point3d sourcePoint = new Point3d( 8.83319047, 54.90017335, Double.NaN );
        Point3d targetPoint = new Point3d( 8.83213115, 54.89846442, Double.NaN );

        // do the testing
        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_D, EPSILON_D );
    }

    /**
     * Test the forward/inverse transformation from a geographic crs (EPSG:4314) lat/lon to another geographic crs
     * (EPSG:4258) lon/lat
     * 
     * @throws TransformationException
     */
    @Test
    public void testGeographicLatLonToGeographic()
                            throws TransformationException {

        // source crs epsg:4314
        GeographicCRS sourceCRS = geographic_4314_lat_lon;
        // target crs epsg:4258
        GeographicCRS targetCRS = geographic_4258;

        // with kind regards to vodafone for supplying reference points.
        Point3d sourcePoint = new Point3d( 54.90017335, 8.83319047, Double.NaN );
        Point3d targetPoint = new Point3d( 8.83213115, 54.89846442, Double.NaN );

        // do the testing
        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_D, EPSILON_D );
    }

    /**
     * Test the forward/inverse transformation from a geographic crs (EPSG:4314) lat/lon to another geographic crs
     * (EPSG:4258) lat/lon
     * 
     * @throws TransformationException
     */
    @Test
    public void testGeographicLatLonToGeographicLatLon()
                            throws TransformationException {

        // source crs epsg:4314
        GeographicCRS sourceCRS = geographic_4314_lat_lon;
        // target crs epsg:4258
        GeographicCRS targetCRS = geographic_4258_lat_lon;

        // with kind regards to vodafone for supplying reference points.
        Point3d sourcePoint = new Point3d( 54.90017335, 8.83319047, Double.NaN );
        Point3d targetPoint = new Point3d( 54.89846442, 8.83213115, Double.NaN );

        // do the testing
        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_D, EPSILON_D );
    }

    /**
     * Test the forward/inverse transformation from a geographic crs (EPSG:4314) to a geocentric crs (EPSG:4964)
     * 
     * @throws TransformationException
     * 
     * @throws TransformationException
     */
    @Test
    public void testGeographicToGeocentric()
                            throws TransformationException {
        // source crs epsg:4314
        GeographicCRS sourceCRS = geographic_4314;
        // target crs epsg:4964
        GeocentricCRS targetCRS = geocentric_4964;

        // created with deegree not a fine reference
        Point3d sourcePoint = new Point3d( 8.83319047, 54.90017335, Double.NaN );
        Point3d targetPoint = new Point3d( 3632280.522352362, 564392.6943947134, 5194921.3092999635 );
        // do the testing
        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_M, EPSILON_D );
    }

    /**
     * Test the forward/inverse transformation from a geocentric (dummy based on bessel) to another geocentric crs
     * (EPSG:4964 based on etrs89)
     * 
     * @throws TransformationException
     */
    @Test
    public void testGeocentricToGeocentric()
                            throws TransformationException {
        // source crs is a dummy based on the epsg:4314 == bessel datum.
        GeocentricCRS sourceCRS = geocentric_dummy;

        // target crs epsg:4964 etrs89 based
        GeocentricCRS targetCRS = geocentric_4964;

        // created with deegree not a fine reference
        Point3d sourcePoint = new Point3d( 3631650.239831989, 564363.5250884632, 5194468.545970947 );
        Point3d targetPoint = new Point3d( 3632280.522352362, 564392.6943947134, 5194921.3092999635 );

        // do the testing
        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_M, EPSILON_D );
    }

    @Test
    public void test2DTo2Dwith3rdCoordinate()
                            throws TransformationException {
        // source crs is epsg:31467
        ProjectedCRS sourceCRS = projected_31467;

        // target crs is epsg:4326
        GeographicCRS targetCRS = GeographicCRS.WGS84;

        // created with deegree not a fine reference
        Point3d sourcePoint = new Point3d( 3532465.57, 5301523.49, 42 );
        Point3d targetPoint = new Point3d( 9.432778, 47.851111, 42 );

        // do the testing
        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_D, EPSILON_M );
    }

    @Test
    public void testSwitchXYEPSG7423()
                            throws IllegalArgumentException, TransformationException {
        ICRS sourceCRS = CRSManager.getCRSRef( "urn:ogc:def:crs:EPSG::7423_XY" );
        ICRS targetCRS = CRSManager.getCRSRef( "urn:ogc:def:crs:EPSG::7423" );

        Point3d sourcePoint = new Point3d( 9.822340, 52.404600, 50.00 );
        CoordinateTransformer transformer = new CoordinateTransformer( targetCRS );
        Point3d targetPoint = transformer.transform( sourceCRS, Collections.singletonList( sourcePoint ) ).get( 0 );
        Assert.assertEquals( sourcePoint.y, targetPoint.x, 0 );
        Assert.assertEquals( sourcePoint.x, targetPoint.y, 0 );
        Assert.assertEquals( sourcePoint.z, targetPoint.z, 0 );
    }

    @Test
    public void testEpsg4647ToEpsg4326()
                            throws IllegalArgumentException, TransformationException {
        ICRS sourceCRS = CRSManager.getCRSRef( "epsg:4647" );
        ICRS targetCRS = CRSManager.getCRSRef( "epsg:4326" );
        Point3d sourcePoint = new Point3d( 32574177.309926, 6020313.020522, NaN );
        Point3d targetPoint = new Point3d( 10.140556, 54.325278, NaN );
        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_D, EPSILON_M );
    }

    @Test
    public void testEpsg4647ToEpsg25832()
                            throws IllegalArgumentException, TransformationException {
        ICRS sourceCRS = CRSManager.getCRSRef( "epsg:4647" );
        ICRS targetCRS = CRSManager.getCRSRef( "epsg:25832" );
        Point3d sourcePoint = new Point3d( 32574177.309926, 6020313.020522, NaN );
        Point3d targetPoint = new Point3d( 574177.309926, 6020313.020522, NaN );
        doForwardAndInverse( sourceCRS, targetCRS, sourcePoint, targetPoint, EPSILON_D, EPSILON_M );
    }
}
