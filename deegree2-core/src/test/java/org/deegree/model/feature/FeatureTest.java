//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/model/feature/FeatureTest.java $
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
package org.deegree.model.feature;

import junit.framework.TestCase;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;

/**
 *
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: apoth $
 *
 * @version. $Revision: 29955 $, $Date: 2011-03-09 14:50:25 +0100 (Mi, 09 Mrz 2011) $
 */
public class FeatureTest extends TestCase {
    private static ILogger LOG = LoggerFactory.getLogger( FeatureTest.class );

    private PropertyType[] ftps1 = null;

    private PropertyType[] ftpsGeom = null;

    private FeatureProperty[] featProp = null;

    private FeatureProperty[] featPropGeom = null;

    private FeatureType feature_type = null;

    private FeatureType featureTypeGeom = null;

    private Geometry geom = null;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp()
                            throws Exception {
        super.setUp();

        // Set up PropertyType - no Geometry
        ftps1 = new PropertyType[2];
        ftps1[0] = FeatureFactory.createSimplePropertyType( new QualifiedName( "ID" ), Types.DOUBLE, false );
        ftps1[1] = FeatureFactory.createSimplePropertyType( new QualifiedName( "Name" ), Types.VARCHAR, false );

        // Set up PropertyType with a Geometry Property
        ftpsGeom = new PropertyType[3];
        ftpsGeom[0] = FeatureFactory.createSimplePropertyType( new QualifiedName( "ID" ), Types.DOUBLE, false );
        ftpsGeom[1] = FeatureFactory.createSimplePropertyType( new QualifiedName( "Name" ), Types.VARCHAR, false );
        ftpsGeom[2] = FeatureFactory.createSimplePropertyType( new QualifiedName( "GEOM" ), Types.GEOMETRY, false );

        // Set up FeatureProperty - no Geometry Properties
        featProp = new FeatureProperty[2];
        featProp[0] = FeatureFactory.createFeatureProperty( new QualifiedName( "ID" ), new Double( 100 ) );
        featProp[1] = FeatureFactory.createFeatureProperty( new QualifiedName( "Name" ), "String_100" );

        // Set up FeatureProperty with a Geometry as property
        featPropGeom = new FeatureProperty[3];
        featPropGeom[0] = FeatureFactory.createFeatureProperty( new QualifiedName( "ID" ), new Double( -999 ) );
        featPropGeom[1] = FeatureFactory.createFeatureProperty( new QualifiedName( "Name" ), "String_-999" );
        geom = createCurve();
        featPropGeom[2] = FeatureFactory.createFeatureProperty( new QualifiedName( "GEOM" ), geom );

        // Set up FeatureTypes - no geometry
        feature_type = FeatureFactory.createFeatureType( "myFeatureType", false, ftps1 );

        // Set up FeatureTypes with a geometry property
        featureTypeGeom = FeatureFactory.createFeatureType( "featureTypeGom", false, ftpsGeom );
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown()
                            throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for ShapeTest.
     *
     * @param arg0
     */
    public FeatureTest( String arg0 ) {
        super( arg0 );
    }

    /*
     * Tests the <tt>PropertyType</tt>s created with the values initiated in setUp()
     */
    public void testFeatureTypeProperty() {

        // LOG.logInfo( " --- \t PropertyType \t --- " );
        featureTypePropertyTesting( new QualifiedName( "ID" ), Types.DOUBLE, 1, ftps1[0] );
        featureTypePropertyTesting( new QualifiedName( "Name" ), Types.VARCHAR, 1, ftps1[1] );
        featureTypePropertyTesting( new QualifiedName( "GEOM" ), Types.GEOMETRY, 1, ftpsGeom[2] );
        // LOG.logInfo( " ---------------------------------------------- \n" );
    }

    /*
     * Tests wether the <tt>PropertyType</tt> object featTypeProp is completly equal to a
     * PropertyType created with the values name, type, and nullable. @param name @param type @param
     * nullabled @param featTypeProp
     */
    public void featureTypePropertyTesting( QualifiedName name, int type, int nullable, PropertyType featTypeProp ) {

        // LOG.logInfo( "\t featTypeProp.getName():"
        // + featTypeProp.getName() );
        // LOG.logInfo( "\t Expected:"
        // + name );
        assertEquals( " PropertyType names are not equal ", name, featTypeProp.getName() );
        // LOG.logInfo( "\t featTypeProp.getType(): "
        // + featTypeProp.getType() );
        // LOG.logInfo( "\t Expected: "
        // + type );
        assertEquals( " PropertyType types expected to be equal ", type, featTypeProp.getType() );
        //
        // LOG.logInfo( "\t featTypeProp.getMinOccurs(): "
        // + featTypeProp.getMinOccurs() );
        // LOG.logInfo( "\t featTypeProp.getMaxOccurs(): "
        // + featTypeProp.getMaxOccurs() );
        // LOG.logInfo( "\t Expected: "
        // + nullable );
        assertEquals( " PropertyType expected to be equal ", nullable, featTypeProp.getMinOccurs() );
    }

    /*
     * Tests the <tt>FeatureType</tt>s created with the values initiated in setUp()
     */
    public void testFeatureType() {

        // LOG.logInfo( " --- \t FeatureType \t --- " );
        featureTypeTesting( "myFeatureType", ftps1, feature_type );
        featureTypeTesting( "featureTypeGom", ftpsGeom, featureTypeGeom );
        // LOG.logInfo( " ---------------------------------------------- \n" );
    }

    /*
     * Tests wether the <tt>FeatureType</tt> object featureType is completly equal to a
     * FeatureType created with the values parents, children, featTypeName and properties. @param
     * parents @param children @param featTypeName @param properties @param featureType
     */
    public void featureTypeTesting( String featTypeName, PropertyType[] properties, FeatureType featureType ) {

        String name = featureType.getName().getPrefixedName();
        // LOG.logInfo( "\t featureType.getName(): "
        // + name );
        // LOG.logInfo( "\t Expected: "
        // + featTypeName );
        assertEquals( " FeatureType names expected to be equal ", featTypeName, name );

        PropertyType[] ftps = featureType.getProperties();
        // LOG.logInfo( "\t ftps.length: "
        // + ftps.length );
        // LOG.logInfo( "\t Expected: "
        // + properties.length );
        assertEquals( " FeatureType: properties lengths expected to be equal ", properties.length, ftps.length );
        for ( int i = 0; i < ftps.length; i++ ) {
            // LOG.logInfo( "\t property["
            // + i + "]" );
            featureTypePropertyTesting( properties[i].getName(), properties[i].getType(), properties[i].getMinOccurs(),
                                        ftps[i] );
        }
    }

    /*
     * Tests the <tt>FeatureType</tt>s created with the values initiated in setUp()
     */
    public void testFeatureProperty() {
        //
        // LOG.logInfo( " --- \t FeatureProperty \t --- " );
        //
        featurePropertyTesting( "ID", new Double( 100 ), featProp[0] );
        featurePropertyTesting( "Name", "String_object", featProp[1] );

        // featPropGeom
        featurePropertyTesting( "ID", new Double( -999 ), featPropGeom[0] );
        featurePropertyTesting( "Name", "String_-999", featPropGeom[1] );
        featurePropertyTesting( "GEOM", geom, featPropGeom[2] );
        // LOG.logInfo( " ---------------------------------------------- \n" );
    }

    /*
     * Tests wether the <tt>FeatureProperty</tt> object featProp is completly equal to a
     * FeatureProperty created with the values identifier, featPropObject, featProp. @param
     * identifier @param featPropObject @param featProp
     */
    public void featurePropertyTesting( String identifier, Object featPropObject, FeatureProperty featProp ) {

        QualifiedName name = featProp.getName();
        // LOG.logInfo( "\t featureProperty.getName(): "
        // + name );
        // LOG.logInfo( "\t Expeted: "
        // + identifier );
        assertEquals( " FeatureProperty names expected to be equal ", name.getPrefixedName(), identifier );
        Object object = featProp.getValue();
        // LOG.logInfo( "\t object value: "
        // + object.toString() );
        // LOG.logInfo( "\t Expected: "
        // + featPropObject.toString() );
        assertEquals( " FeatureProperty object values expected to be equal ", name.getPrefixedName(), identifier );
    }

    /*
     * Tests <tt>Feature</tt> with the values initiated in setUp()
     */

    public void testFeature() {
        // LOG.logInfo( "--- \t Feature \t ---" );
        String featureId = "featureId";

        Feature feature = FeatureFactory.createFeature( featureId, feature_type, featProp );
        featureTesting( featureId, feature_type, featProp, feature );

        String featureIdGeom = "featureIdGeometry";
        Feature featureGeom = FeatureFactory.createFeature( featureIdGeom, featureTypeGeom, featPropGeom );
        featureTesting( featureIdGeom, featureTypeGeom, featPropGeom, featureGeom );
        //
        // LOG.logInfo( "-------------------------------------\n" );
    }

    /*
     * Tests wether the <tt>Feature</tt> object feature is completly equal to a FeatureProperty
     * created with the values featureId, featureTypea and featureProperties. @param featureId
     * @param featureTypea @param featurePropertiesa @param feature
     */
    public void featureTesting( String featureId, FeatureType featureType, FeatureProperty[] featureProperties,
                                Feature feature ) {

        String id = feature.getId();
        // LOG.logInfo( "\t feature.getId(): "
        // + id );
        // LOG.logInfo( "\t Expected: "
        // + featureId );
        assertEquals( " Feature: Ids expected to be equal ", id, featureId );

        FeatureType new_feature_type = feature.getFeatureType();
        // LOG.logInfo( "\t feature.getFeatureType(): "
        // + new_feature_type.toString() );
        // LOG.logInfo( "\t featureType: "
        // + featureType.toString() );
        //
        String featType_name = featureType.getName().getPrefixedName();
        PropertyType[] ftps = featureType.getProperties();
        featureTypeTesting( featType_name, ftps, new_feature_type );

    }

    /*
     * Creates a FeatureCollection object without no Geometry property to test
     */
    public void testFeatureCollection() {

        // LOG.logInfo( "--- \t FeatureCollection \t ---" );
        Feature[] features = new Feature[2];
        // first feature
        FeatureProperty[][] featureProperties = new FeatureProperty[2][2];
        featureProperties[0][0] = FeatureFactory.createFeatureProperty( new QualifiedName( "ID" ), new Double( 100 ) );
        featureProperties[0][1] = FeatureFactory.createFeatureProperty( new QualifiedName( "Name" ), "Object_100" );
        String featureId1 = "id_100";
        features[0] = FeatureFactory.createFeature( featureId1, feature_type, featureProperties[0] );

        // second feature
        featureProperties[1][0] = FeatureFactory.createFeatureProperty( new QualifiedName( "ID" ), new Double( 999 ) );
        featureProperties[1][1] = FeatureFactory.createFeatureProperty( new QualifiedName( "Name" ), "Object_999" );
        String featureId2 = "id_999";
        features[1] = FeatureFactory.createFeature( featureId2, feature_type, featureProperties[0] );

        FeatureCollection FC = FeatureFactory.createFeatureCollection( "FeatureCollection", features );

        featureCollectionTesting( "FeatureCollection", features, FC );

    }

    /*
     * Creates a FeatureCollection object with one Geometry property to test
     */
    public void testFeatureCollectionGeom() {

        // LOG.logInfo( "--- \t FeatureCollection \t ---" );
        //
        Feature[] features = new Feature[2];

        // first feature
        FeatureProperty[][] featureProperties = new FeatureProperty[2][3];
        featureProperties[0][0] = FeatureFactory.createFeatureProperty( new QualifiedName( "ID" ), new Double( 111 ) );
        featureProperties[0][1] = FeatureFactory.createFeatureProperty( new QualifiedName( "Name" ), "Object_111" );
        featureProperties[0][2] = FeatureFactory.createFeatureProperty( new QualifiedName( "GEOM" ), geom );
        String featureId1 = "id_111";
        features[0] = FeatureFactory.createFeature( featureId1, featureTypeGeom, featureProperties[0] );

        // second feature
        featureProperties[1][0] = FeatureFactory.createFeatureProperty( new QualifiedName( "ID" ), new Double( 555 ) );
        featureProperties[1][1] = FeatureFactory.createFeatureProperty( new QualifiedName( "Name" ), "Object_555" );
        Geometry geom2 = createSurface();
        featureProperties[1][2] = FeatureFactory.createFeatureProperty( new QualifiedName( "GEOM" ), geom2 );
        String featureId2 = "id_555";
        features[1] = FeatureFactory.createFeature( featureId2, featureTypeGeom, featureProperties[1] );

        FeatureCollection FC = FeatureFactory.createFeatureCollection( "FeatureCollectionGeom", features );

        featureCollectionTesting( "FeatureCollectionGeom", features, FC );

    }

    /*
     * Tests wether the <tt>FeatureCollection</tt> object fc is completly equal to a
     * FeatureCollection created with the values Id, features. @param featureId @param featureTypea
     * @param featurePropertiesa @param feature
     */
    public void featureCollectionTesting( String Id, Feature[] features, FeatureCollection fc ) {

        // Check FC ids
        String fcId = fc.getId();
        // LOG.logInfo( "\t feature.getId(): "
        // + fcId );
        // LOG.logInfo( "\t Expected: "
        // + Id );
        assertEquals( "FeatureCollection: Ids expected to be equal", Id, fcId );

        // Check wether the amount of Features is the same
        Feature[] fcFeatures = fc.toArray();
        // LOG.logInfo( "\t fcFeatures.length: "
        // + fcFeatures.length );
        // LOG.logInfo( "\t Expected: "
        // + features.length );
        assertEquals( "FeatureCollection: Features lengths expected to be equal", Id, fcId );

        // Check wether the Features one by one
        for ( int i = 0; i < features.length; i++ ) {

            // Check Feature ids
            String fcFeaturesId = fcFeatures[i].getId();
            String featuresId = features[i].getId();
            // LOG.logInfo( "\t fcFeaturesId: "
            // + fcFeaturesId );
            // LOG.logInfo( "\t Expected: "
            // + featuresId );
            assertEquals( " Feature: Ids expected to be equal ", fcFeaturesId, featuresId );

            // Check FeatureTypes
            FeatureType fcFeaturesFeatType = fcFeatures[i].getFeatureType();
            FeatureType featuresFeatType = features[i].getFeatureType();
            featureTypeTesting( featuresFeatType.getName().getPrefixedName(), featuresFeatType.getProperties(),
                                fcFeaturesFeatType );

            // Check the FeatureProperties
            FeatureProperty[] featObjecProps = features[i].getProperties();
            FeatureProperty[]fcFeatObjecProps = features[i].getProperties();

            // LOG.logInfo( "\t FC features["
            // + i + "].properties lengh " + fcFeatObjecProps.length );
            // LOG.logInfo( "\t Expected "
            // + featObjecProps.length );
            //
            assertEquals( "Properties length expected to be equal ", featObjecProps.length, fcFeatObjecProps.length );

            // Check the Value of each Property
            for ( int j = 0; j < featObjecProps.length; j++ ) {

                Class class1 = featObjecProps[j].getClass();
                Class class2 = fcFeatObjecProps[j].getClass();
                // LOG.logInfo( "\t FC features["
                // + i + "] properties[" + j + "]" + "\n\t " + class2 );
                // LOG.logInfo( "\t Expected: "
                // + class1 );
                assertEquals( "FC features[" + i + "] properties[" + j + "]" + "Classes expected to be equal ", class1,
                              class2 );

                Object value1 = features[i].getProperties( featObjecProps[j].getName() )[0];
                Object value2 = fcFeatures[i].getProperties( featObjecProps[j].getName() )[0];

                assertEquals( "Expected Objects to be of the same Type", value1.getClass(), value2.getClass() );

                // LOG.logInfo( "\t FC features["
                // + i + "] properties[" + j + "]" + "\t Vaule: " + value2 );
                // LOG.logInfo( "\t Expected: "
                // + value1 );
                assertEquals( "Expected values to be equal", value1, value2 );
            }
        }
        // LOG.logInfo( " ---------------------------------------------- \n" );
    }

    /*
     * Creates a Curve Geometry Object
     */
    public Geometry createCurve()
                            throws UnknownCRSException {

        CoordinateSystem crs = CRSFactory.create( "EPSG:4326" );
        Position[] pos = new Position[4];
        for ( int i = 0; i < pos.length; i++ ) {
            pos[i] = GeometryFactory.createPosition( i, i );
        }
        Geometry geom = null;
        try {
            geom = GeometryFactory.createCurve( pos, crs );
        } catch ( Exception e ) {
            String s = "Exception GeometryFactory.createSurface failed";
            LOG.logError( "\t" + s + " failed \n" + e.getMessage(), e );
        }
        return geom;
    }

    /*
     * Creates a Surface Geometry Object
     */
    public Geometry createSurface() {

        Geometry geom = null;

        try {
            Position[] exteriorRing = new Position[5];
            exteriorRing[0] = GeometryFactory.createPosition( 100, 100 );
            exteriorRing[1] = GeometryFactory.createPosition( 99, -99 );
            exteriorRing[2] = GeometryFactory.createPosition( -88, -88 );
            exteriorRing[3] = GeometryFactory.createPosition( -77, 77 );
            exteriorRing[4] = GeometryFactory.createPosition( 100, 100 );

            Position[][] interiorRings = new Position[1][4];
            interiorRings[0][0] = GeometryFactory.createPosition( 1, 1 );
            interiorRings[0][1] = GeometryFactory.createPosition( 9, -9 );
            interiorRings[0][2] = GeometryFactory.createPosition( -9, -9 );
            interiorRings[0][3] = GeometryFactory.createPosition( 1, 1 );

            geom = GeometryFactory.createSurface( exteriorRing, interiorRings, null, null );

        } catch ( Exception e ) {
            String s = "Exception GeometryFactory.createSurface failed";
            LOG.logError( "\t" + s + " failed \n" + e.getMessage(), e );
            fail( e.getMessage() );
        }
        return geom;
    }
}
