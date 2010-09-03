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
package org.deegree.protocol.wfs.getfeature;

import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.cs.CRS;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.Within;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;
import org.junit.Test;

/**
 * The <code>GetFeatureKVPAdapterTest</code> class tests the GetFeature KVP adapter.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GetFeatureKVPAdapterTest extends TestCase {

    // ----------------------V 1.0.0 ---------------------------------
    private final String V100_EXAMPLE_1 = "examples_kvp/v100/example1.kvp";

    private final String V100_EXAMPLE_2 = "examples_kvp/v100/example2.kvp";

    private final String V100_EXAMPLE_3 = "examples_kvp/v100/example3.kvp";

    private final String V100_EXAMPLE_4 = "examples_kvp/v100/example4.kvp";

    private final String V100_EXAMPLE_5 = "examples_kvp/v100/example5.kvp";

    private final String V100_EXAMPLE_6 = "examples_kvp/v100/example6.kvp";

    private final String V100_EXAMPLE_7 = "examples_kvp/v100/example7.kvp";

    private final String V100_EXAMPLE_9 = "examples_kvp/v100/example9.kvp";

    private final String V100_EXAMPLE_10 = "examples_kvp/v100/example10.kvp";

    private final String V100_EXAMPLE_11 = "examples_kvp/v100/example11.kvp";

    private final String V100_EXAMPLE_12 = "examples_kvp/v100/example12.kvp";

    // ----------------------V 1.1.0 ---------------------------------
    private final String V110_EXAMPLE_1 = "examples_kvp/v110/example1.kvp";

    private final String V110_EXAMPLE_2 = "examples_kvp/v110/example2.kvp";

    private final String V110_EXAMPLE_3 = "examples_kvp/v110/example3.kvp";

    private final String V110_EXAMPLE_4 = "examples_kvp/v110/example4.kvp";

    private final String V110_EXAMPLE_5 = "examples_kvp/v110/example5.kvp";

    private final String V110_EXAMPLE_6 = "examples_kvp/v110/example6.kvp";

    private final String V110_EXAMPLE_7 = "examples_kvp/v110/example7.kvp";

    private final String V110_EXAMPLE_8 = "examples_kvp/v110/example8.kvp";

    private final String V110_EXAMPLE_9 = "examples_kvp/v110/example9.kvp";

    private final String V110_EXAMPLE_10 = "examples_kvp/v110/example10.kvp";

    private final String V110_EXAMPLE_11 = "examples_kvp/v110/example11.kvp";

    private final String V110_EXAMPLE_12 = "examples_kvp/v110/example12.kvp";

    private final String V110_EXAMPLE_13 = "examples_kvp/v110/example13.kvp";

    private final String V110_EXAMPLE_14 = "examples_kvp/v110/example14.kvp";

    private final String V110_EXAMPLE_15 = "examples_kvp/v110/example15.kvp";

    private final String V110_EXAMPLE_16 = "examples_kvp/v110/example16.kvp";

    private final String V110_EXAMPLE_17 = "examples_kvp/v110/example17.kvp";

    private final String V110_EXAMPLE_sortby = "examples_kvp/v110/example_sortby.kvp";

    /**
     * @throws Exception
     */
    @Test
    public void test_V100_EXAMPLE_1()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V100_EXAMPLE_1 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        assertEquals( new QName( "INWATERA_1M" ), getFeature.getQueries()[0].getTypeNames()[0].getFeatureTypeName() );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_1()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_1 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );

        Query[] queries = getFeature.getQueries();
        assertEquals( ( (FilterQuery) queries[0] ).getTypeNames()[0].getFeatureTypeName(), new QName( "InWaterA_1M" ) );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V100_EXAMPLE_2()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V100_EXAMPLE_2 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FilterQuery filterQuery = (FilterQuery) getFeature.getQueries()[0];
        assertEquals( new QName( "INWATERA_1M" ), filterQuery.getTypeNames()[0].getFeatureTypeName() );

        assertEquals( "INWATERA_1M/WKB_GEOM", filterQuery.getPropertyNames()[0].getPropertyName() );
        assertEquals( "INWATERA_1M/TILE_ID", filterQuery.getPropertyNames()[1].getPropertyName() );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_2()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_2 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );

        Query[] queries = getFeature.getQueries();
        assertEquals( ( (FilterQuery) queries[0] ).getPropertyNames()[0].getPropertyName(), "InWaterA_1M/wkbGeom" );
        assertEquals( ( (FilterQuery) queries[0] ).getPropertyNames()[1].getPropertyName(), "InWaterA_1M/tileId" );
        assertEquals( ( (FilterQuery) queries[0] ).getTypeNames()[0].getFeatureTypeName(), new QName( "InWaterA_1M" ) );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V100_EXAMPLE_3()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V100_EXAMPLE_3 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FeatureIdQuery featureQuery = (FeatureIdQuery) getFeature.getQueries()[0];
        assertEquals( "INWATERA_1M.1013", featureQuery.getFeatureIds()[0] );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_3()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_3 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );

        Query[] queries = getFeature.getQueries();
        FeatureIdQuery featureId = (FeatureIdQuery) queries[0];
        assertEquals( featureId.getFeatureIds()[0], "InWaterA_1M.1013" );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V100_EXAMPLE_4()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V100_EXAMPLE_4 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );

        Query[] queries = getFeature.getQueries();
        FeatureIdQuery featureId = (FeatureIdQuery) queries[0];
        assertEquals( "INWATERA_1M", featureId.getFeatureIds()[0] );

        assertEquals( "INWATERA_1M/WKB_GEOM", featureId.getPropertyNames()[0][0].getPropertyName() );
        assertEquals( "INWATERA_1M/TILE_ID", featureId.getPropertyNames()[0][1].getPropertyName() );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_4()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_4 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        assertEquals( ( (FeatureIdQuery) getFeature.getQueries()[0] ).getPropertyNames()[0][0].getPropertyName(),
                      "InWaterA_1M/wkbGeom" );
        assertEquals( ( (FeatureIdQuery) getFeature.getQueries()[0] ).getPropertyNames()[0][1].getPropertyName(),
                      "InWaterA_1M/tileId" );
        FeatureIdQuery featureId = (FeatureIdQuery) getFeature.getQueries()[0];
        assertEquals( featureId.getFeatureIds()[0], "InWaterA_1M.1013" );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V100_EXAMPLE_5()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V100_EXAMPLE_5 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );

        Query[] queries = getFeature.getQueries();
        FeatureIdQuery featureId = (FeatureIdQuery) queries[0];
        assertEquals( "INWATERA_1M.1013", featureId.getFeatureIds()[0] );
        assertEquals( "INWATERA_1M.1014", featureId.getFeatureIds()[1] );
        assertEquals( "INWATERA_1M.1015", featureId.getFeatureIds()[2] );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_5()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_5 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );

        Query[] queries = getFeature.getQueries();
        FeatureIdQuery featureId = (FeatureIdQuery) queries[0];
        assertEquals( featureId.getFeatureIds()[0], "InWaterA_1M.1013" );
        assertEquals( featureId.getFeatureIds()[1], "InWaterA_1M.1014" );
        assertEquals( featureId.getFeatureIds()[2], "InWaterA_1M.1015" );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V100_EXAMPLE_6()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V100_EXAMPLE_6 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );

        Query[] queries = getFeature.getQueries();
        FeatureIdQuery featureId = (FeatureIdQuery) queries[0];
        assertEquals( "INWATERA_1M.1013", featureId.getFeatureIds()[0] );
        assertEquals( "INWATERA_1M.1014", featureId.getFeatureIds()[1] );
        assertEquals( "INWATERA_1M.1015", featureId.getFeatureIds()[2] );

        assertEquals( "INWATERA_1M/WKB_GEOM", featureId.getPropertyNames()[0][0].getPropertyName() );
        assertEquals( "INWATERA_1M/TILE_ID", featureId.getPropertyNames()[0][1].getPropertyName() );

        assertEquals( "INWATERA_1M/WKB_GEOM", featureId.getPropertyNames()[1][0].getPropertyName() );
        assertEquals( "INWATERA_1M/TILE_ID", featureId.getPropertyNames()[1][1].getPropertyName() );

        assertEquals( "INWATERA_1M/WKB_GEOM", featureId.getPropertyNames()[2][0].getPropertyName() );
        assertEquals( "INWATERA_1M/TILE_ID", featureId.getPropertyNames()[2][1].getPropertyName() );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_6()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_6 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FeatureIdQuery featureQuery = (FeatureIdQuery) getFeature.getQueries()[0];
        assertEquals( "InWaterA_1M/wkbGeom", featureQuery.getPropertyNames()[0][0].getPropertyName() );
        assertEquals( "InWaterA_1M/tileId", featureQuery.getPropertyNames()[0][1].getPropertyName() );
        assertEquals( "InWaterA_1M/wkbGeom", featureQuery.getPropertyNames()[1][0].getPropertyName() );
        assertEquals( "InWaterA_1M/tileId", featureQuery.getPropertyNames()[1][1].getPropertyName() );
        assertEquals( "InWaterA_1M/wkbGeom", featureQuery.getPropertyNames()[2][0].getPropertyName() );
        assertEquals( "InWaterA_1M/tileId", featureQuery.getPropertyNames()[2][1].getPropertyName() );
        FeatureIdQuery featureId = (FeatureIdQuery) getFeature.getQueries()[0];
        assertEquals( featureId.getFeatureIds()[0], "InWaterA_1M.1013" );
        assertEquals( featureId.getFeatureIds()[1], "InWaterA_1M.1014" );
        assertEquals( featureId.getFeatureIds()[2], "InWaterA_1M.1015" );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V100_EXAMPLE_7()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V100_EXAMPLE_7 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FilterQuery filterQuery = (FilterQuery) getFeature.getQueries()[0];
        OperatorFilter opFilter = (OperatorFilter) filterQuery.getFilter();
        Within within = (Within) opFilter.getOperator();
        assertEquals( "INWATERA_1M/WKB_GEOM", within.getPropName().getPropertyName() );
        Envelope env = (Envelope) within.getGeometry();
        verifyEnvelope( env, 10.0, 10.0, 20.0, 20.0 );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_7()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_7 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FilterQuery filterq = (FilterQuery) getFeature.getQueries()[0];
        assertEquals( "InWaterA_1M", filterq.getTypeNames()[0].getFeatureTypeName().getLocalPart() );
        OperatorFilter filter = (OperatorFilter) filterq.getFilter();
        assertTrue( filter.getOperator() instanceof Within );
        Within within = (Within) filter.getOperator();
        assertEquals( "InWaterA_1M/wkbGeom", within.getPropName().getPropertyName() );
        assertTrue( within.getGeometry() instanceof Envelope );
        Envelope env = (Envelope) within.getGeometry();
        verifyEnvelope( env, 10.0, 10.0, 20.0, 20.0 );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_8()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_8 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FilterQuery filterq = (FilterQuery) getFeature.getQueries()[0];
        assertEquals( "InWaterA_1M/wkbGeom", filterq.getPropertyNames()[0].getPropertyName() );
        assertEquals( "InWaterA_1M/tileId", filterq.getPropertyNames()[1].getPropertyName() );
        OperatorFilter filter = (OperatorFilter) filterq.getFilter();
        assertTrue( filter.getOperator() instanceof Within );
        Within within = (Within) filter.getOperator();
        assertEquals( "InWaterA_1M/wkbGeom", within.getPropName().getPropertyName() );
        assertTrue( within.getGeometry() instanceof Envelope );
        Envelope env = (Envelope) within.getGeometry();
        verifyEnvelope( env, 10.0, 10.0, 20.0, 20.0 );
    }

    @SuppressWarnings("boxing")
    private void verifyEnvelope( Envelope env, double d, double e, double f, double g ) {
        Point p1 = env.getMin();
        assertEquals( p1.get0(), d );
        assertEquals( p1.get1(), e );
        Point p2 = env.getMax();
        assertEquals( p2.get0(), f );
        assertEquals( p2.get1(), g );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V100_EXAMPLE_9()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V100_EXAMPLE_9 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FilterQuery filterQuery0 = (FilterQuery) getFeature.getQueries()[0];
        assertEquals( new QName( "INWATERA_1M" ), filterQuery0.getTypeNames()[0].getFeatureTypeName() );
        FilterQuery filterQuery1 = (FilterQuery) getFeature.getQueries()[1];
        assertEquals( new QName( "BUILTUPA_1M" ), filterQuery1.getTypeNames()[0].getFeatureTypeName() );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_9()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_9 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FilterQuery query0 = (FilterQuery) getFeature.getQueries()[0];
        assertEquals( new QName( "http://www.someserver.com", "InWaterA_1M" ),
                      query0.getTypeNames()[0].getFeatureTypeName() );
        FilterQuery query1 = (FilterQuery) getFeature.getQueries()[1];
        assertEquals( new QName( "http://www.someotherserver.com", "BuiltUpA_1M" ),
                      query1.getTypeNames()[0].getFeatureTypeName() );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V100_EXAMPLE_10()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V100_EXAMPLE_10 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );

        FilterQuery filterQuery = (FilterQuery) getFeature.getQueries()[0];
        assertEquals( new QName( "INWATERA_1M" ), filterQuery.getTypeNames()[0].getFeatureTypeName() );
        assertEquals( "INWATERA_1M/WKB_GEOM", filterQuery.getPropertyNames()[0].getPropertyName() );
        assertEquals( "INWATERA_1M/TILE_ID", filterQuery.getPropertyNames()[1].getPropertyName() );

        filterQuery = (FilterQuery) getFeature.getQueries()[1];
        assertEquals( new QName( "BUILTUPA_1M" ), filterQuery.getTypeNames()[0].getFeatureTypeName() );
        assertEquals( "BUILTUPA_1M/*", filterQuery.getPropertyNames()[0].getPropertyName() );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_10()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_10 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FilterQuery query0 = (FilterQuery) getFeature.getQueries()[0];
        PropertyName[] propNames0 = query0.getPropertyNames();
        assertEquals( "InWaterA_1M/wkbGeom", propNames0[0].getPropertyName() );
        assertEquals( "InWaterA_1M/tileId", propNames0[1].getPropertyName() );
        assertEquals( new QName( "InWaterA_1M" ), query0.getTypeNames()[0].getFeatureTypeName() );

        FilterQuery query1 = (FilterQuery) getFeature.getQueries()[1];
        PropertyName[] propNames1 = query1.getPropertyNames();
        assertEquals( "BuiltUpA_1M/*", propNames1[0].getPropertyName() );
        assertEquals( new QName( "BuiltUpA_1M" ), query1.getTypeNames()[0].getFeatureTypeName() );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V100_EXAMPLE_11()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V100_EXAMPLE_11 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FeatureIdQuery featureQuery = (FeatureIdQuery) getFeature.getQueries()[0];
        assertEquals( "INWATERA_1M.1013", featureQuery.getFeatureIds()[0] );
        assertEquals( "BUILTUP_1M.3456", featureQuery.getFeatureIds()[1] );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_11()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_11 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FeatureIdQuery query = (FeatureIdQuery) getFeature.getQueries()[0];
        String[] featureIds = query.getFeatureIds();
        assertEquals( "InWaterA_1M.1013", featureIds[0] );
        assertEquals( "BUILTUP_1M.3456", featureIds[1] );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V100_EXAMPLE_12()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V100_EXAMPLE_12 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FeatureIdQuery featureQuery = (FeatureIdQuery) getFeature.getQueries()[0];

        assertEquals( "INWATERA_1M.1013", featureQuery.getFeatureIds()[0] );
        assertEquals( "BUILTUPA_1M.3456", featureQuery.getFeatureIds()[1] );

        assertEquals( "INWATERA_1M/WKB_GEOM", featureQuery.getPropertyNames()[0][0].getPropertyName() );
        assertEquals( "INWATERA_1M/TILE_ID", featureQuery.getPropertyNames()[0][1].getPropertyName() );
        assertEquals( "BUILTUPA_1M/WKB_GEOM", featureQuery.getPropertyNames()[0][2].getPropertyName() );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_12()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_12 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FeatureIdQuery featureQuery = (FeatureIdQuery) getFeature.getQueries()[0];
        PropertyName[][] propNames = featureQuery.getPropertyNames();
        assertEquals( "InWaterA_1M/wkbGeom", propNames[0][0].getPropertyName() );
        assertEquals( "InWaterA_1M/tileId", propNames[0][1].getPropertyName() );
        assertEquals( "BuiltUpA_1M/wkbGeom", propNames[1][0].getPropertyName() );

        String[] featureIds = featureQuery.getFeatureIds();
        assertEquals( "InWaterA_1M.1013", featureIds[0] );
        assertEquals( "BuiltUpA_1M.3456", featureIds[1] );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_13()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_13 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FilterQuery query0 = (FilterQuery) getFeature.getQueries()[0];
        OperatorFilter filter = (OperatorFilter) query0.getFilter();
        assertTrue( filter.getOperator() instanceof Within );
        Within within = (Within) filter.getOperator();
        assertEquals( "InWaterA_1M/wkbGeom", within.getPropName().getPropertyName() );

        Envelope env = (Envelope) within.getGeometry();
        verifyEnvelope( env, 10.0, 10.0, 20.0, 20.0 );

        FilterQuery query1 = (FilterQuery) getFeature.getQueries()[1];
        filter = (OperatorFilter) query1.getFilter();
        assertTrue( filter.getOperator() instanceof Within );
        within = (Within) filter.getOperator();
        assertEquals( "BuiltUpA_1M/wkbGeom", within.getPropName().getPropertyName() );

        env = (Envelope) within.getGeometry();
        verifyEnvelope( env, 10.0, 10.0, 20.0, 20.0 );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_14()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_14 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FilterQuery filter0 = (FilterQuery) getFeature.getQueries()[0];
        PropertyName[] propNames = filter0.getPropertyNames();
        assertEquals( "InWaterA_1M/wkbGeom", propNames[0].getPropertyName() );
        assertEquals( "InWaterA_1M/tileId", propNames[1].getPropertyName() );
        assertEquals( new QName( "InWaterA_1M" ), filter0.getTypeNames()[0].getFeatureTypeName() );

        FilterQuery filter1 = (FilterQuery) getFeature.getQueries()[1];
        propNames = filter1.getPropertyNames();
        assertEquals( "BuiltUpA_1M/wkbGeom", propNames[0].getPropertyName() );
        assertEquals( new QName( "BuiltUpA_1M" ), filter1.getTypeNames()[0].getFeatureTypeName() );

        Operator op = ( (OperatorFilter) filter0.getFilter() ).getOperator();
        assertTrue( op instanceof Within );
        Within within = (Within) op;
        assertEquals( "InWaterA_1M/wkbGeom|InWaterA_1M/wkbGeom", within.getPropName().getPropertyName() );
        Envelope env = (Envelope) within.getGeometry();
        verifyEnvelope( env, 10.0, 10.0, 20.0, 20.0 );

        op = ( (OperatorFilter) filter1.getFilter() ).getOperator();
        assertTrue( op instanceof Within );
        within = (Within) op;
        assertEquals( "InWaterA_1M/wkbGeom", within.getPropName().getPropertyName() );
        env = (Envelope) within.getGeometry();
        verifyEnvelope( env, 10.0, 10.0, 20.0, 20.0 );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_15()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_15 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FeatureIdQuery featureQuery = (FeatureIdQuery) getFeature.getQueries()[0];
        PropertyName[][] propNames = featureQuery.getPropertyNames();
        assertEquals( "uk:Town/gml:name", propNames[0][0].getPropertyName() );
        assertEquals( "uk:Town/gml:directedNode", propNames[0][1].getPropertyName() );
        String[] featureIds = featureQuery.getFeatureIds();
        assertEquals( "t1", featureIds[0] );
        TypeName[] typeName = featureQuery.getTypeNames();
        assertEquals( new QName( "http://www.theuknamespace.uk", "Town" ), typeName[0].getFeatureTypeName() );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_16()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_16 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        assertEquals( "1", getFeature.getTraverseXlinkDepth() );
        assertEquals( new Integer( 1 ), getFeature.getTraverseXlinkExpiry() );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_17()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_17 );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FeatureIdQuery featureQuery = (FeatureIdQuery) getFeature.getQueries()[0];
        XLinkPropertyName[][] xlinkProps = featureQuery.getXLinkPropertyNames();
        assertEquals( "uk:Town/gml:name", xlinkProps[0][0].getPropertyName().getPropertyName() );
        assertEquals( "0", xlinkProps[0][0].getTraverseXlinkDepth() );
        assertEquals( new Integer( 0 ), xlinkProps[0][0].getTraverseXlinkExpiry() );

        assertEquals( "uk:Town/gml:directedNode", xlinkProps[0][1].getPropertyName().getPropertyName() );
        assertEquals( "2", xlinkProps[0][1].getTraverseXlinkDepth() );
        assertEquals( new Integer( 2 ), xlinkProps[0][1].getTraverseXlinkExpiry() );
    }

    /**
     * @throws Exception
     */
    @Test
    public void test_V110_EXAMPLE_sortby()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V110_EXAMPLE_sortby );
        Map<String, String> kvpMap = KVPUtils.readFileIntoMap( exampleURL );

        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpMap, null );
        FilterQuery filterQuery = (FilterQuery) getFeature.getQueries()[0];
        SortProperty[] sortby = filterQuery.getSortBy();
        assertEquals( 4, sortby.length );
        assertEquals( true, sortby[0].getSortOrder() );
        assertEquals( "ands/dsalw", sortby[0].getSortProperty().getPropertyName() );
        assertEquals( true, sortby[1].getSortOrder() );
        assertEquals( "dsad/assdsa", sortby[1].getSortProperty().getPropertyName() );
        assertEquals( true, sortby[2].getSortOrder() );
        assertEquals( "dsda/asdasda", sortby[2].getSortProperty().getPropertyName() );
        assertEquals( true, sortby[3].getSortOrder() );
        assertEquals( "erewr/sdasd/dasda", sortby[3].getSortProperty().getPropertyName() );

        assertEquals( "ALL", filterQuery.getFeatureVersion() );
        assertEquals( new Integer( 1000000 ), getFeature.getMaxFeatures() );
        assertEquals( new CRS( "EPSG:4326" ), filterQuery.getSrsName() );
    }
}
