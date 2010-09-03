//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.EPSGCode;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.VerticalDatum;
import org.deegree.cs.configuration.CRSConfiguration;
import org.deegree.cs.configuration.deegree.xml.stax.Parser;
import org.deegree.cs.coordinatesystems.GeocentricCRS;
import org.deegree.cs.coordinatesystems.ProjectedCRS;
import org.deegree.cs.coordinatesystems.VerticalCRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.transformations.TransformationFactory.DSTransform;
import org.deegree.cs.transformations.coordinate.ConcatenatedTransform;
import org.deegree.cs.transformations.coordinate.GeocentricTransform;
import org.deegree.cs.transformations.coordinate.NotSupportedTransformation;
import org.deegree.cs.transformations.coordinate.ProjectionTransform;
import org.deegree.cs.transformations.helmert.Helmert;
import org.deegree.cs.transformations.ntv2.NTv2Transformation;
import org.deegree.cs.utilities.MappingUtils;
import org.junit.Test;

/**
 * Tests the substitution of transformations.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class TransformationSubstitutionTest implements CRSDefines {

    static {
        datum_6289.setToWGS84( wgs_1672 );
        datum_6258.setToWGS84( wgs_1188 );
        datum_6314.setToWGS84( wgs_1777 );
        datum_6171.setToWGS84( wgs_1188 );
    }

    private final VerticalDatum datum = new VerticalDatum( new CRSCodeType( "datum" ) );

    private final Axis[] axis = new Axis[] { new Axis( "up", Axis.AO_UP ) };

    private final VerticalCRS c1 = new VerticalCRS( datum, axis, new CRSIdentifiable( new CRSCodeType( "1" ) ) );

    private final VerticalCRS c2 = new VerticalCRS( datum, axis, new CRSIdentifiable( new CRSCodeType( "2" ) ) );

    private final VerticalCRS c3 = new VerticalCRS( datum, axis, new CRSIdentifiable( new CRSCodeType( "3" ) ) );

    private final VerticalCRS c4 = new VerticalCRS( datum, axis, new CRSIdentifiable( new CRSCodeType( "4" ) ) );

    private final VerticalCRS c5 = new VerticalCRS( datum, axis, new CRSIdentifiable( new CRSCodeType( "5" ) ) );

    private final VerticalCRS c6 = new VerticalCRS( datum, axis, new CRSIdentifiable( new CRSCodeType( "6" ) ) );

    private final VerticalCRS c7 = new VerticalCRS( datum, axis, new CRSIdentifiable( new CRSCodeType( "7" ) ) );

    private final VerticalCRS c8 = new VerticalCRS( datum, axis, new CRSIdentifiable( new CRSCodeType( "8" ) ) );

    private final VerticalCRS c9 = new VerticalCRS( datum, axis, new CRSIdentifiable( new CRSCodeType( "9" ) ) );

    private final VerticalCRS c10 = new VerticalCRS( datum, axis, new CRSIdentifiable( new CRSCodeType( "10" ) ) );

    private final Transformation c = new NotSupportedTransformation( c1, c2,
                                                                     new CRSIdentifiable( new CRSCodeType( "c" ) ) );

    private final Transformation e = new NotSupportedTransformation( c2, c3,
                                                                     new CRSIdentifiable( new CRSCodeType( "e" ) ) );

    private final Transformation f = new NotSupportedTransformation( c3, c4,
                                                                     new CRSIdentifiable( new CRSCodeType( "f" ) ) );

    private final Transformation h = new NotSupportedTransformation( c4, c5,
                                                                     new CRSIdentifiable( new CRSCodeType( "h" ) ) );

    private final Transformation j = new NotSupportedTransformation( c5, c6,
                                                                     new CRSIdentifiable( new CRSCodeType( "j" ) ) );

    private final Transformation k = new NotSupportedTransformation( c6, c7,
                                                                     new CRSIdentifiable( new CRSCodeType( "k" ) ) );

    private final Transformation n = new NotSupportedTransformation( c7, c8,
                                                                     new CRSIdentifiable( new CRSCodeType( "n" ) ) );

    private final Transformation p = new NotSupportedTransformation( c8, c9,
                                                                     new CRSIdentifiable( new CRSCodeType( "p" ) ) );

    private final Transformation q = new NotSupportedTransformation( c9, c10,
                                                                     new CRSIdentifiable( new CRSCodeType( "q" ) ) );

    private final Transformation d = new ConcatenatedTransform( e, f, new CRSIdentifiable( new CRSCodeType( "d" ) ) );

    private final Transformation i = new ConcatenatedTransform( j, k, new CRSIdentifiable( new CRSCodeType( "i" ) ) );

    private final Transformation b = new ConcatenatedTransform( c, d, new CRSIdentifiable( new CRSCodeType( "b" ) ) );

    private final Transformation g = new ConcatenatedTransform( h, i, new CRSIdentifiable( new CRSCodeType( "g" ) ) );

    private final Transformation a = new ConcatenatedTransform( b, g, new CRSIdentifiable( new CRSCodeType( "a" ) ) );

    private final Transformation o = new ConcatenatedTransform( p, q, new CRSIdentifiable( new CRSCodeType( "a" ) ) );

    private final Transformation m = new ConcatenatedTransform( n, o, new CRSIdentifiable( new CRSCodeType( "a" ) ) );

    private final Transformation l = new ConcatenatedTransform( a, m, new CRSIdentifiable( new CRSCodeType( "a" ) ) );

    /**
     * @return the factory to test.
     */
    private TransformationFactory getFactory() {
        return CRSConfiguration.getInstance().getTransformationFactory();
    }

    /**
     * Tests if the Substitute of the automated transformation chain is totally replaced by the given 'single'
     * transformation.
     * 
     * @throws IllegalArgumentException
     * @throws TransformationException
     */
    @Test
    public void oneFitsAll()
                            throws IllegalArgumentException, TransformationException {
        TransformationFactory factory = getFactory();
        Transformation created = factory.createFromCoordinateSystems( projected_31467, projected_28992 );
        Assert.assertNotNull( created );
        Assert.assertTrue( created instanceof ConcatenatedTransform );
        Assert.assertEquals( projected_31467, created.getSourceCRS() );
        Assert.assertEquals( projected_28992, created.getTargetCRS() );

        Transformation sub = new NotSupportedTransformation( projected_31467, projected_28992 );
        List<Transformation> transList = new ArrayList<Transformation>();
        transList.add( sub );
        Transformation replaced = factory.createFromCoordinateSystems( projected_31467, projected_28992, transList );
        Assert.assertNotNull( replaced );
        Assert.assertTrue( "Substitute was not used", replaced.equals( sub ) );
    }

    /**
     * Test if the substitute of the first (inverse) projection works.
     * 
     * @throws IllegalArgumentException
     * @throws TransformationException
     */
    @Test
    public void substituteInverseProjection()
                            throws IllegalArgumentException, TransformationException {
        TransformationFactory factory = getFactory();
        Transformation created = factory.createFromCoordinateSystems( projected_31467, projected_28992 );
        Assert.assertNotNull( created );
        Assert.assertTrue( created instanceof ConcatenatedTransform );
        Assert.assertEquals( projected_31467, created.getSourceCRS() );
        Assert.assertEquals( projected_28992, created.getTargetCRS() );

        ProjectedCRS np = new ProjectedCRS( projected_31467.getProjection(), projected_31467.getAxis(), projected_31467 );
        String name = "The substitute of the old projection.";
        np.setDefaultName( name, true );
        Transformation sub = new ProjectionTransform( np );
        // it's an inverse
        sub.inverse();

        List<Transformation> transList = new ArrayList<Transformation>();
        transList.add( sub );
        Transformation replaced = factory.createFromCoordinateSystems( projected_31467, projected_28992, transList );
        Assert.assertNotNull( replaced );
        // equals only tests on the id's.
        Assert.assertEquals( created, replaced );
        /* check if the name was not accidentally set in the static crs */
        Assert.assertNull( projected_31467.getName() );
        Assert.assertEquals( name, replaced.getSourceCRS().getName() );

    }

    /**
     * Test if the substitute of the last projection works.
     * 
     * @throws IllegalArgumentException
     * @throws TransformationException
     */
    @Test
    public void substituteProjection()
                            throws IllegalArgumentException, TransformationException {
        TransformationFactory factory = getFactory();
        Transformation created = factory.createFromCoordinateSystems( projected_31467, projected_28992 );
        Assert.assertNotNull( created );
        Assert.assertTrue( created instanceof ConcatenatedTransform );
        Assert.assertEquals( projected_31467, created.getSourceCRS() );
        Assert.assertEquals( projected_28992, created.getTargetCRS() );

        ProjectedCRS np = new ProjectedCRS( projected_28992.getProjection(), projected_28992.getAxis(), projected_28992 );
        String name = "The substitute of the old projection.";
        np.setDefaultName( name, true );
        Transformation sub = new ProjectionTransform( np );

        List<Transformation> transList = new ArrayList<Transformation>();
        transList.add( sub );
        Transformation replaced = factory.createFromCoordinateSystems( projected_31467, projected_28992, transList );
        Assert.assertNotNull( replaced );
        // equals only tests on the id's.
        Assert.assertEquals( created, replaced );
        /* check if the name was not accidentally set in the static crs */
        Assert.assertNull( projected_28992.getName() );
        Assert.assertEquals( name, replaced.getTargetCRS().getName() );
    }

    /**
     * Test if the substitute of usage of the helmert transformation chain can be replaced by a
     * {@link NTv2Transformation}.
     * 
     * @throws IllegalArgumentException
     * @throws TransformationException
     */
    @Test
    public void substituteHelmertWithNTv2()
                            throws IllegalArgumentException, TransformationException {
        TransformationFactory factory = getFactory();

        Transformation created = factory.createFromCoordinateSystems( projected_31467, projected_25832 );
        Assert.assertNotNull( created );
        Assert.assertTrue( created instanceof ConcatenatedTransform );
        Assert.assertEquals( projected_31467, created.getSourceCRS() );
        Assert.assertEquals( projected_25832, created.getTargetCRS() );

        URL beta2007 = Parser.class.getResource( "config/ntv2/beta2007.gsb" );
        Transformation sub = new NTv2Transformation(
                                                     geographic_4314,
                                                     geographic_4258,
                                                     new CRSIdentifiable(
                                                                          new CRSCodeType(
                                                                                           "urn:ogc:def:coordinateOperation:EPSG::15948" ) ),
                                                     beta2007 );

        List<Transformation> transList = new ArrayList<Transformation>();
        transList.add( sub );
        Transformation replaced = factory.createFromCoordinateSystems( projected_31467, projected_25832, transList );
        Assert.assertNotNull( replaced );
        Assert.assertTrue( replaced instanceof ConcatenatedTransform );
        // equals only tests on the id's.
        Assert.assertNotSame( created, replaced );

        Assert.assertTrue( ( (ConcatenatedTransform) replaced ).getSecondTransform() instanceof ConcatenatedTransform );
        ConcatenatedTransform second = (ConcatenatedTransform) ( (ConcatenatedTransform) replaced ).getSecondTransform();

        Assert.assertEquals( sub, second.getFirstTransform() );
    }

    /**
     * Test if the substitute of usage of the helmert transformation chain can be replaced by a
     * {@link NTv2Transformation}.
     * 
     * @throws IllegalArgumentException
     * @throws TransformationException
     */
    @Test
    public void substituteNTv2WithHelmert()
                            throws IllegalArgumentException, TransformationException {

        TransformationFactory factory = getFactory();
        factory.setPreferredTransformation( DSTransform.NTv2 );

        Transformation created = factory.createFromCoordinateSystems( projected_31467, projected_25832 );
        Assert.assertNotNull( created );
        Assert.assertTrue( created instanceof ConcatenatedTransform );
        Assert.assertEquals( projected_31467, created.getSourceCRS() );
        Assert.assertEquals( projected_25832, created.getTargetCRS() );

        GeocentricTransform geo1 = new GeocentricTransform( geographic_4314, new GeocentricCRS( datum_6314,
                                                                                                axis_geocentric,
                                                                                                new EPSGCode( 14314 ) ) );
        ConcatenatedTransform ct = new ConcatenatedTransform( geo1, wgs_1777 );

        Helmert h_inv = new Helmert( geographic_4258, new EPSGCode[] { new EPSGCode( 1111111 ) } );
        h_inv.inverse();
        ConcatenatedTransform ct2 = new ConcatenatedTransform( ct, h_inv );
        GeocentricTransform inv_geo = new GeocentricTransform( geographic_4258,
                                                               new GeocentricCRS( datum_6258, axis_geocentric,
                                                                                  new EPSGCode( 14258 ) ) );
        inv_geo.inverse();
        ConcatenatedTransform sub = new ConcatenatedTransform( ct2, inv_geo );

        List<Transformation> transList = new ArrayList<Transformation>();
        transList.add( sub );

        ConcatenatedTransform replaced = (ConcatenatedTransform) factory.createFromCoordinateSystems( projected_31467,
                                                                                                      projected_25832,
                                                                                                      transList );
        Assert.assertNotNull( replaced );
        // equals only tests on the id's.
        Assert.assertNotSame( created, replaced );

        Assert.assertTrue( replaced.getFirstTransform() instanceof ConcatenatedTransform );

        ConcatenatedTransform f = (ConcatenatedTransform) replaced.getFirstTransform();
        Assert.assertTrue( f.getFirstTransform() instanceof ConcatenatedTransform );

        ConcatenatedTransform ff = (ConcatenatedTransform) f.getFirstTransform();
        Assert.assertTrue( ff.getFirstTransform() instanceof ConcatenatedTransform );
        ConcatenatedTransform fff = (ConcatenatedTransform) ff.getFirstTransform();

        Assert.assertEquals( geo1, fff.getSecondTransform() );
        Assert.assertEquals( wgs_1777, ff.getSecondTransform() );

        Assert.assertEquals( h_inv, f.getSecondTransform() );

        Assert.assertTrue( ( replaced ).getSecondTransform() instanceof ConcatenatedTransform );
        ConcatenatedTransform s = (ConcatenatedTransform) replaced.getSecondTransform();
        Assert.assertEquals( inv_geo, s.getFirstTransform() );
        factory.setPreferredTransformation( DSTransform.HELMERT );
    }

    /**
     * Test if the last three transformations of the 'a' transformation are found while replacing the first 3.
     * 
     * @throws IllegalArgumentException
     * @throws TransformationException
     */
    @Test
    public void substituteHypotheticalRightConcat()
                            throws IllegalArgumentException, TransformationException {

        Transformation tbu = new NotSupportedTransformation( c1, c4, new CRSIdentifiable( new CRSCodeType( "tbu" ) ) );
        List<Transformation> tbus = new ArrayList<Transformation>();
        tbus.add( tbu );
        Transformation result = MappingUtils.updateFromDefinedTransformations( tbus, a );

        Assert.assertNotNull( result );
        Assert.assertTrue( result instanceof ConcatenatedTransform );
        ConcatenatedTransform test = (ConcatenatedTransform) result;
        Assert.assertEquals( test.getFirstTransform(), tbu );

        Assert.assertTrue( test.getSecondTransform() instanceof ConcatenatedTransform );

        test = (ConcatenatedTransform) test.getSecondTransform();
        Assert.assertEquals( test.getFirstTransform(), h );

        Assert.assertTrue( "Second should be concatenated, but was: " + test.getSecondTransform(),
                           test.getSecondTransform() instanceof ConcatenatedTransform );
        test = (ConcatenatedTransform) test.getSecondTransform();

        Assert.assertEquals( test.getFirstTransform(), j );
        Assert.assertEquals( test.getSecondTransform(), k );
    }

    /**
     * Test if the first three transformations of the 'a' transformation are found while replacing the last 3.
     * 
     * @throws IllegalArgumentException
     * @throws TransformationException
     */
    @Test
    public void substituteHypotheticalLeftConcat()
                            throws IllegalArgumentException, TransformationException {

        Transformation tbu = new NotSupportedTransformation( c4, c7, new CRSIdentifiable( new CRSCodeType( "tbu" ) ) );
        List<Transformation> tbus = new ArrayList<Transformation>();
        tbus.add( tbu );
        Transformation result = MappingUtils.updateFromDefinedTransformations( tbus, a );

        Assert.assertNotNull( result );
        Assert.assertTrue( result instanceof ConcatenatedTransform );
        ConcatenatedTransform test = (ConcatenatedTransform) result;
        Assert.assertEquals( test.getFirstTransform(), c );

        Assert.assertTrue( "Second should be concatenated, but was: " + test.getSecondTransform(),
                           test.getSecondTransform() instanceof ConcatenatedTransform );

        test = (ConcatenatedTransform) test.getSecondTransform();
        Assert.assertEquals( test.getFirstTransform(), e );

        Assert.assertTrue( "Second should be concatenated, but was: " + test.getSecondTransform(),
                           test.getSecondTransform() instanceof ConcatenatedTransform );
        test = (ConcatenatedTransform) test.getSecondTransform();

        Assert.assertEquals( test.getFirstTransform(), f );
        Assert.assertEquals( test.getSecondTransform(), tbu );
    }

    /**
     * Test if the first three transformations of the 'a' transformation are found while replacing the last 3.
     * 
     * @throws IllegalArgumentException
     * @throws TransformationException
     */
    @Test
    public void substituteHypotheticalMiddleConcat()
                            throws IllegalArgumentException, TransformationException {

        Transformation tbu = new NotSupportedTransformation( c4, c7, new CRSIdentifiable( new CRSCodeType( "tbu" ) ) );
        List<Transformation> tbus = new ArrayList<Transformation>();
        tbus.add( tbu );
        Transformation result = MappingUtils.updateFromDefinedTransformations( tbus, l );

        Assert.assertNotNull( result );
        Assert.assertTrue( result instanceof ConcatenatedTransform );
        ConcatenatedTransform test = (ConcatenatedTransform) result;
        Assert.assertEquals( test.getFirstTransform(), c );

        Assert.assertTrue( "Second should be concatenated, but was: " + test.getSecondTransform(),
                           test.getSecondTransform() instanceof ConcatenatedTransform );

        test = (ConcatenatedTransform) test.getSecondTransform();
        Assert.assertEquals( test.getFirstTransform(), e );

        Assert.assertTrue( "Second should be concatenated, but was: " + test.getSecondTransform(),
                           test.getSecondTransform() instanceof ConcatenatedTransform );
        test = (ConcatenatedTransform) test.getSecondTransform();

        Assert.assertEquals( test.getFirstTransform(), f );

        Assert.assertTrue( "Second should be concatenated, but was: " + test.getSecondTransform(),
                           test.getSecondTransform() instanceof ConcatenatedTransform );
        test = (ConcatenatedTransform) test.getSecondTransform();

        Assert.assertEquals( test.getFirstTransform(), tbu );

        Assert.assertTrue( "Second should be concatenated, but was: " + test.getSecondTransform(),
                           test.getSecondTransform() instanceof ConcatenatedTransform );
        test = (ConcatenatedTransform) test.getSecondTransform();
        Assert.assertEquals( test.getFirstTransform(), n );

        Assert.assertTrue( "Second should be concatenated, but was: " + test.getSecondTransform(),
                           test.getSecondTransform() instanceof ConcatenatedTransform );
        test = (ConcatenatedTransform) test.getSecondTransform();
        Assert.assertEquals( test.getFirstTransform(), p );
        Assert.assertEquals( test.getSecondTransform(), q );

    }
}
