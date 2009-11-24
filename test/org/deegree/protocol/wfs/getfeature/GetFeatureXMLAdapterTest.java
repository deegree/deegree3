//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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

package org.deegree.protocol.wfs.getfeature;

import static org.deegree.filter.Operator.Type.COMPARISON;
import static org.deegree.filter.Operator.Type.LOGICAL;
import static org.deegree.filter.logical.LogicalOperator.SubType.AND;

import java.net.URL;
import java.util.Set;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.BinaryComparisonOperator;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.comparison.PropertyIsGreaterThan;
import org.deegree.filter.comparison.PropertyIsGreaterThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLessThanOrEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.spatial.Within;
import org.deegree.geometry.Envelope;
import org.junit.Test;

/**
 * Test class for the GetFeatureXMLAdapter.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetFeatureXMLAdapterTest extends TestCase {

    // ---------------------version 1.0.0------------------------------
    private final String V100_EXAMPLE1 = "examples_xml/v100/example1.xml";

    private final String V100_EXAMPLE2 = "examples_xml/v100/example2.xml";

    private final String V100_EXAMPLE3 = "examples_xml/v100/example3.xml";

    private final String V100_EXAMPLE4 = "examples_xml/v100/example4.xml";

    private final String V100_EXAMPLE5 = "examples_xml/v100/example5.xml";

    private final String V100_EXAMPLE6 = "examples_xml/v100/example6.xml";

    private final String V100_EXAMPLE7 = "examples_xml/v100/example7.xml";

    private final String V100_EXAMPLE8 = "examples_xml/v100/example8.xml";

    private final String V100_EXAMPLE9 = "examples_xml/v100/example9.xml";

    // ---------------------version 1.1.0------------------------------
    private final String V110_EXAMPLE01 = "examples_xml/v110/example01.xml";

    private final String V110_EXAMPLE02 = "examples_xml/v110/example02.xml";

    private final String V110_EXAMPLE03 = "examples_xml/v110/example03.xml";

    private final String V110_EXAMPLE04 = "examples_xml/v110/example04.xml";

    // private final String V110_EXAMPLE05 = "examples_xml/v110/example05.xml";

    // private final String V110_EXAMPLE06 = "examples_xml/v110/example06.xml";

    private final String V110_EXAMPLE09 = "examples_xml/v110/example09.xml";

    private final String V110_EXAMPLE10 = "examples_xml/v110/example10.xml";

    private final String V110_EXAMPLE11 = "examples_xml/v110/example11.xml";

    private final String V110_EXAMPLE12 = "examples_xml/v110/example12.xml";

    /**
     * @throws Exception
     */
    public void test_V100_EXAMPLE01()
                            throws Exception {
        URL exampleURL = this.getClass().getResource( V100_EXAMPLE1 );
        XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );

        GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
        getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
        GetFeature getFeature = getFeatureAdapter.parse( null );

        Query[] queries = getFeature.getQueries();
        FilterQuery filterQuery = (FilterQuery) queries[0];
        assertEquals( new QName( "http://www.someserver.com/myns", "INWATERA_1M" ),
                      filterQuery.getTypeNames()[0].getFeatureTypeName() );
        IdFilter idFilter = (IdFilter) filterQuery.getFilter();

        Set<String> matchingIds = idFilter.getMatchingIds();
        assertTrue( matchingIds.size() == 1 && matchingIds.contains( "INWATERA_1M.1234" ) );
    }

    /**
     * @throws Exception
     *             When the version of the GetFeature document is not supported for parsing (superfluous in this case,
     *             since we are testing 1.1.0 files and parsing is supported for this version)
     */
    @Test
    public void test_V110_EXAMPLE01()
                            throws Exception {

        URL exampleURL = this.getClass().getResource( V110_EXAMPLE01 );
        XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );

        GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
        getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
        GetFeature getFeature = getFeatureAdapter.parse( null );

        Query[] queries = getFeature.getQueries();
        FilterQuery filterQuery = (FilterQuery) queries[0];

        TypeName[] typeNames = filterQuery.getTypeNames();

        assertEquals( typeNames.length, 1 );
        assertEquals( typeNames[0].getFeatureTypeName(), new QName( "http://www.someserver.com/myns", "InWaterA1M" ) );

        IdFilter filter = (IdFilter) filterQuery.getFilter();
        Set<String> ids = filter.getMatchingIds();

        assertEquals( ids.size(), 1 );
        assertTrue( ids.contains( "InWaterA_1M.1234" ) );
    }

    /**
     * @throws Exception
     */
    public void test_V100_EXAMPLE2()
                            throws Exception {

        URL exampleURL = this.getClass().getResource( V100_EXAMPLE2 );
        XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );

        GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
        getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
        GetFeature getFeature = getFeatureAdapter.parse( null );

        FilterQuery filterQuery = (FilterQuery) getFeature.getQueries()[0];
        PropertyName[] propertyNames = filterQuery.getPropertyNames();

        assertEquals( "myns:WKB_GEOM", propertyNames[0].getPropertyName() );
        assertEquals( "myns:TILE_ID", propertyNames[1].getPropertyName() );
        assertEquals( "myns:FAC_ID", propertyNames[2].getPropertyName() );

        IdFilter idFilter = (IdFilter) filterQuery.getFilter();
        Set<String> matchingIds = idFilter.getMatchingIds();
        assertTrue( matchingIds.size() == 1 && matchingIds.contains( "INWATERA_1M.1013" ) );
    }

    /**
     * @throws Exception
     *             When the version of the GetFeature document is not supported for parsing (superfluous in this case,
     *             since we are testing 1.1.0 files and parsing is supported for this version)
     */
    @Test
    public void test_V110_EXAMPLE02()
                            throws Exception {

        URL exampleURL = this.getClass().getResource( V110_EXAMPLE02 );
        XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );

        GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
        getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
        GetFeature getFeature = getFeatureAdapter.parse( null );

        Query[] queries = getFeature.getQueries();
        FilterQuery filterQuery = (FilterQuery) queries[0];

        PropertyName[] propNames = filterQuery.getPropertyNames();

        assertEquals( propNames.length, 3 );
        assertEquals( propNames[0].getPropertyName(), "myns:wkbGeom" );
        assertEquals( propNames[1].getPropertyName(), "myns:tileId" );
        assertEquals( propNames[2].getPropertyName(), "myns:facId" );

        IdFilter filter = (IdFilter) filterQuery.getFilter();
        Set<String> ids = filter.getMatchingIds();

        assertEquals( ids.size(), 1 );
        assertTrue( ids.contains( "InWaterA1M.1013" ) );
    }

    /**
     * @throws Exception
     */
    public void test_V100_EXAMPLE3()
                            throws Exception {

        URL exampleURL = this.getClass().getResource( V100_EXAMPLE3 );
        XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );

        GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
        getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
        GetFeature getFeature = getFeatureAdapter.parse( null );

        FilterQuery filterQuery = (FilterQuery) getFeature.getQueries()[0];
        assertEquals( new QName( "http://www.someserver.com/myns", "INWATERA_1M" ),
                      filterQuery.getTypeNames()[0].getFeatureTypeName() );
        IdFilter idFilter = (IdFilter) filterQuery.getFilter();
        Set<String> matchingIds = idFilter.getMatchingIds();
        assertTrue( matchingIds.size() == 3 && matchingIds.contains( "INWATERA_1M.1013" )
                    && matchingIds.contains( "INWATERA_1M.1014" ) && matchingIds.contains( "INWATERA_1M.1015" ) );
    }

    /**
     * @throws Exception
     *             When the version of the GetFeature document is not supported for parsing (superfluous in this case,
     *             since we are testing 1.1.0 files and parsing is supported for this version)
     */
    @Test
    public void test_V110_EXAMPLE03()
                            throws Exception {

        URL exampleURL = this.getClass().getResource( V110_EXAMPLE03 );
        XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );

        GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
        getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
        GetFeature getFeature = getFeatureAdapter.parse( null );

        Query[] queries = getFeature.getQueries();
        FilterQuery filterQuery = (FilterQuery) queries[0];

        IdFilter filter = (IdFilter) filterQuery.getFilter();
        Set<String> ids = filter.getMatchingIds();

        assertEquals( ids.size(), 3 );
        assertTrue( ids.contains( "InWaterA1M.1013" ) );
        assertTrue( ids.contains( "InWaterA1M.1014" ) );
        assertTrue( ids.contains( "InWaterA1M.1015" ) );
    }

    /**
     * @throws Exception
     */
    public void test_V100_EXAMPLE4()
                            throws Exception {

        URL exampleURL = this.getClass().getResource( V100_EXAMPLE4 );
        XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );

        GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
        getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
        GetFeature getFeature = getFeatureAdapter.parse( null );

        FilterQuery filterQuery = (FilterQuery) getFeature.getQueries()[0];
        assertEquals( new QName( "http://www.someserver.com/myns", "INWATERA_1M" ),
                      filterQuery.getTypeNames()[0].getFeatureTypeName() );

        assertEquals( "myns:WKB_GEOM", filterQuery.getPropertyNames()[0].getPropertyName() );
        assertEquals( "myns:TILE_ID", filterQuery.getPropertyNames()[1].getPropertyName() );
        IdFilter idFilter = (IdFilter) filterQuery.getFilter();
        Set<String> ids = idFilter.getMatchingIds();

        assertTrue( ids.size() == 3 && ids.contains( "INWATERA_1M.1013" ) && ids.contains( "INWATERA_1M.1014" )
                    && ids.contains( "INWATERA_1M.1015" ) );

    }

    /**
     * @throws Exception
     *             When the version of the GetFeature document is not supported for parsing (superfluous in this case,
     *             since we are testing 1.1.0 files and parsing is supported for this version)
     */
    @Test
    public void test_V110_EXAMPLE04()
                            throws Exception {

        URL exampleURL = this.getClass().getResource( V110_EXAMPLE04 );
        XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );

        GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
        getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
        GetFeature getFeature = getFeatureAdapter.parse( null );

        Query[] queries = getFeature.getQueries();
        FilterQuery filterQuery = (FilterQuery) queries[0];

        PropertyName[] propNames = filterQuery.getPropertyNames();

        assertEquals( propNames.length, 2 );
        assertEquals( propNames[0].getPropertyName(), "myns:wkbGeom" );
        assertEquals( propNames[1].getPropertyName(), "myns:tileId" );

        IdFilter filter = (IdFilter) filterQuery.getFilter();
        Set<String> ids = filter.getMatchingIds();

        assertEquals( ids.size(), 3 );
        assertTrue( ids.contains( "InWaterA1M.1013" ) );
        assertTrue( ids.contains( "InWaterA1M.1014" ) );
        assertTrue( ids.contains( "InWaterA1M.1015" ) );
    }

    /**
     * @throws Exception
     */
    public void test_V100_EXAMPLE5()
                            throws Exception {

        URL exampleURL = this.getClass().getResource( V100_EXAMPLE5 );
        XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );

        GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
        getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
        GetFeature getFeature = getFeatureAdapter.parse( null );

        assertEquals( new Integer( 10000 ), getFeature.getMaxFeatures() );

        FilterQuery filterQuery = (FilterQuery) getFeature.getQueries()[0];
        assertEquals( new QName( "http://www.someserver.com/myns", "INWATERA_1M" ),
                      filterQuery.getTypeNames()[0].getFeatureTypeName() );
    }

    /**
     * @throws Exception
     */
    public void test_V100_EXAMPLE6()
                            throws Exception {

        URL exampleURL = this.getClass().getResource( V100_EXAMPLE6 );
        XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );

        GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
        getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
        GetFeature getFeature = getFeatureAdapter.parse( null );

        FilterQuery filterQuery = (FilterQuery) getFeature.getQueries()[0];
        assertEquals( new QName( "http://www.someserver.com/myns", "INWATERA_1M" ),
                      filterQuery.getTypeNames()[0].getFeatureTypeName() );

        filterQuery = (FilterQuery) getFeature.getQueries()[1];
        assertEquals( new QName( "http://www.someserver.com/myns", "BUILTUPA_1M" ),
                      filterQuery.getTypeNames()[0].getFeatureTypeName() );

        filterQuery = (FilterQuery) getFeature.getQueries()[2];
        assertEquals( new QName( "http://demo.cubewerx.com/yourns", "ROADL_1M" ),
                      filterQuery.getTypeNames()[0].getFeatureTypeName() );
    }

    /**
     * @throws Exception
     */
    public void test_V100_EXAMPLE7()
                            throws Exception {

        URL exampleURL = this.getClass().getResource( V100_EXAMPLE7 );
        XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );

        GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
        getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
        GetFeature getFeature = getFeatureAdapter.parse( null );

        FilterQuery filterQuery = (FilterQuery) getFeature.getQueries()[0];
        assertEquals( new QName( "http://www.someserver.com/myns", "HYDROGRAPHY" ),
                      filterQuery.getTypeNames()[0].getFeatureTypeName() );

        assertEquals( "myns:GEOTEMP", filterQuery.getPropertyNames()[0].getPropertyName() );
        assertEquals( "myns:DEPTH", filterQuery.getPropertyNames()[1].getPropertyName() );
    }

    /**
     * @throws Exception
     */
    @SuppressWarnings("boxing")
    public void test_V100_EXAMPLE8()
                            throws Exception {

        URL exampleURL = this.getClass().getResource( V100_EXAMPLE8 );
        XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );

        GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
        getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
        GetFeature getFeature = getFeatureAdapter.parse( null );

        FilterQuery filterQuery = (FilterQuery) getFeature.getQueries()[0];
        assertEquals( new QName( "http://www.someserver.com/myns", "ROADS" ),
                      filterQuery.getTypeNames()[0].getFeatureTypeName() );

        assertEquals( "myns:PATH", filterQuery.getPropertyNames()[0].getPropertyName() );
        assertEquals( "myns:LANES", filterQuery.getPropertyNames()[1].getPropertyName() );
        assertEquals( "myns:SURFACETYPE", filterQuery.getPropertyNames()[2].getPropertyName() );

        OperatorFilter opFilter = (OperatorFilter) filterQuery.getFilter();
        Within within = (Within) opFilter.getOperator();
        assertEquals( "myns:PATH", within.getPropName().getPropertyName() );
        Envelope env = (Envelope) within.getGeometry();
        assertEquals( 50.0, env.getMin().get0() );
        assertEquals( 40.0, env.getMin().get1() );
        assertEquals( 100.0, env.getMax().get0() );
        assertEquals( 60.0, env.getMax().get1() );
    }

    // /**
    // * @throws Exception
    // * When the version of the GetFeature document is not supported for parsing (superfluous in this case,
    // * since we are testing 1.1.0 files and parsing is supported for this version)
    // */
    // @Test
    // public void testEXAMPLE05()
    // throws Exception {
    //
    // URL exampleURL = this.getClass().getResource( V110_EXAMPLE05 );
    // XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );
    //
    // GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
    // getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
    // GetFeature getFeature = getFeatureAdapter.parse();
    //
    // Query[] queries = getFeature.getQueries();
    // FilterQuery filterQuery = (FilterQuery) queries[0];
    //
    // TypeName[] typeNames = filterQuery.getTypeNames();
    //
    // assertEquals( typeNames.length, 1 );
    // assertEquals( typeNames[0].getFeatureTypeName(), new QName( "http://www.someserver.com/myns", "InWaterA_1M" ) );
    // }

    // /**
    // * @throws Exception When the version of the GetFeature document is not supported for
    // * parsing (superfluous in this case, since we are testing 1.1.0 files and parsing is supported for this version)
    // */
    // @Test
    // public void testEXAMPLE06() throws Exception {
    //
    // URL exampleURL = this.getClass().getResource( V110_EXAMPLE06 );
    // XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );
    //
    // GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
    // getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
    // GetFeature getFeature = getFeatureAdapter.parse();
    //
    // Query[] queries = getFeature.getQueries();
    //        
    // assertEquals( queries.length, 3 );
    //        
    // FilterQuery filterQuery = (FilterQuery) queries[0];
    // TypeName[] typeNames = filterQuery.getTypeNames();
    // assertEquals( typeNames.length, 1 );
    // assertEquals( typeNames[0].getFeatureTypeName(),
    // new QName( "http://www.someserver.com/myns", "InWaterA_1M" ) );
    //        
    // filterQuery = (FilterQuery) queries[1];
    // typeNames = filterQuery.getTypeNames();
    // assertEquals( typeNames.length, 1 );
    // assertEquals( typeNames[0].getFeatureTypeName(),
    // new QName( "http://www.someserver.com/myns", "BuiltUpA_1M" ) );
    //        
    // filterQuery = (FilterQuery) queries[2];
    // typeNames = filterQuery.getTypeNames();
    // assertEquals( typeNames.length, 1 );
    // assertEquals( typeNames[0].getFeatureTypeName(),
    // new QName( "http://demo.cubewerx.com/yourns", "RoadL_1M" ) );
    // }

    /**
     * @throws Exception
     */
    public void test_V100_EXAMPLE9()
                            throws Exception {

        URL exampleURL = this.getClass().getResource( V100_EXAMPLE9 );
        XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );

        GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
        getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
        GetFeature getFeature = getFeatureAdapter.parse( null );

        FilterQuery filterQuery = (FilterQuery) getFeature.getQueries()[0];
        assertEquals( new QName( "Person" ), filterQuery.getTypeNames()[0].getFeatureTypeName() );

        assertEquals( "myns:Person/myns:LastName", filterQuery.getPropertyNames()[0].getPropertyName() );

        OperatorFilter opFilter = (OperatorFilter) filterQuery.getFilter();
        assertTrue( opFilter.getOperator() instanceof And );

        And rootOp = (And) opFilter.getOperator();
        assertTrue( rootOp.getParameter( 0 ) instanceof And );
        And op0 = (And) rootOp.getParameter( 0 );

        assertTrue( op0.getParameter( 0 ) instanceof PropertyIsGreaterThanOrEqualTo );
        PropertyIsGreaterThanOrEqualTo op00 = (PropertyIsGreaterThanOrEqualTo) op0.getParameter( 0 );
        assertEquals( "myns:Person/myns:Address/myns:StreetNumber",
                      ( (PropertyName) op00.getParameter1() ).getPropertyName() );
        assertEquals( "10000", ( (Literal) op00.getParameter2() ).getValue() );

        assertTrue( rootOp.getParameter( 1 ) instanceof And );
        And op1 = (And) rootOp.getParameter( 1 );

        assertTrue( op1.getParameter( 0 ) instanceof PropertyIsEqualTo );
        PropertyIsEqualTo op10 = (PropertyIsEqualTo) op1.getParameter( 0 );
        assertEquals( "myns:Person/myns:Address/myns:StreetName",
                      ( (PropertyName) op10.getParameter1() ).getPropertyName() );
        assertEquals( "Main St.", ( (Literal) op10.getParameter2() ).getValue() );

        PropertyIsEqualTo op11 = (PropertyIsEqualTo) op1.getParameter( 1 );
        assertEquals( "myns:Person/myns:Address/myns:City", ( (PropertyName) op11.getParameter1() ).getPropertyName() );
        assertEquals( "SomeTown", ( (Literal) op11.getParameter2() ).getValue() );

        PropertyIsEqualTo op12 = (PropertyIsEqualTo) op1.getParameter( 2 );
        assertEquals( "myns:Person/myns:Sex", ( (PropertyName) op12.getParameter1() ).getPropertyName() );
        assertEquals( "Female", ( (Literal) op12.getParameter2() ).getValue() );

        PropertyIsGreaterThan op13 = (PropertyIsGreaterThan) op1.getParameter( 3 );
        assertEquals( "myns:Person/myns:Salary", ( (PropertyName) op13.getParameter1() ).getPropertyName() );
        assertEquals( "35000", ( (Literal) op13.getParameter2() ).getValue() );

    }

    /**
     * @throws Exception
     *             When the version of the GetFeature document is not supported for parsing (superfluous in this case,
     *             since we are testing 1.1.0 files and parsing is supported for this version)
     */
    @Test
    public void test_V110_EXAMPLE09()
                            throws Exception {

        URL exampleURL = this.getClass().getResource( V110_EXAMPLE09 );
        XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );

        GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
        getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
        GetFeature getFeature = getFeatureAdapter.parse( null );

        Query[] queries = getFeature.getQueries();
        FilterQuery filterQuery = (FilterQuery) queries[0];
        TypeName[] typeNames = filterQuery.getTypeNames();

        assertEquals( typeNames.length, 1 );
        assertEquals( new QName( "Person" ), typeNames[0].getFeatureTypeName() );

        OperatorFilter opFilter = (OperatorFilter) filterQuery.getFilter();
        assertEquals( opFilter.getOperator().getType(), LOGICAL );

        LogicalOperator logOp = (LogicalOperator) opFilter.getOperator();

        assertEquals( logOp.getSubType(), AND );
        And andOp = (And) logOp;
        Operator op1 = andOp.getParameter( 0 );

        assertEquals( op1.getType(), LOGICAL );
        LogicalOperator logOp1 = (LogicalOperator) op1;
        assertEquals( logOp1.getSubType(), AND );
        And andOp1 = (And) logOp1;

        Operator op11 = andOp1.getParameter( 0 );
        assertEquals( op11.getType(), COMPARISON );
        BinaryComparisonOperator compOp11 = (BinaryComparisonOperator) op11;
        assertTrue( compOp11 instanceof PropertyIsGreaterThanOrEqualTo );
        assertTrue( ( (PropertyIsGreaterThanOrEqualTo) op11 ).getParameter1() instanceof PropertyName );
        assertEquals( ( (PropertyName) ( (PropertyIsGreaterThanOrEqualTo) op11 ).getParameter1() ).getPropertyName(),
                      "myns:Person/myns:mailAddress/myns:Address/myns:streetNumber" );
        assertTrue( ( (PropertyIsGreaterThanOrEqualTo) op11 ).getParameter2() instanceof Literal );
        assertEquals( ( (Literal) ( (PropertyIsGreaterThanOrEqualTo) op11 ).getParameter2() ).getValue(), "10000" );

        Operator op12 = andOp1.getParameter( 1 );
        assertEquals( op12.getType(), COMPARISON );
        BinaryComparisonOperator compOp12 = (BinaryComparisonOperator) op12;
        assertTrue( compOp12 instanceof PropertyIsLessThanOrEqualTo );
        assertTrue( ( (PropertyIsLessThanOrEqualTo) op12 ).getParameter1() instanceof PropertyName );
        assertEquals( ( (PropertyName) ( (PropertyIsLessThanOrEqualTo) op12 ).getParameter1() ).getPropertyName(),
                      "myns:Person/myns:mailAddress/myns:Address/myns:streetNumber" );
        assertTrue( ( (PropertyIsLessThanOrEqualTo) op12 ).getParameter2() instanceof Literal );
        assertEquals( ( (Literal) ( (PropertyIsLessThanOrEqualTo) op12 ).getParameter2() ).getValue(), "10999" );

        Operator op2 = andOp.getParameter( 1 );

        assertEquals( op2.getType(), LOGICAL );
        LogicalOperator logOp2 = (LogicalOperator) op2;
        assertEquals( logOp2.getSubType(), AND );
        And andOp2 = (And) logOp2;

        Operator op21 = andOp2.getParameter( 0 );
        assertEquals( op21.getType(), COMPARISON );
        BinaryComparisonOperator compOp21 = (BinaryComparisonOperator) op21;
        assertTrue( compOp21 instanceof PropertyIsEqualTo );
        assertTrue( ( (PropertyIsEqualTo) op21 ).getParameter1() instanceof PropertyName );
        assertEquals( ( (PropertyName) ( (PropertyIsEqualTo) op21 ).getParameter1() ).getPropertyName(),
                      "myns:Person/myns:mailAddress/myns:Address/myns:streetName" );
        assertTrue( ( (PropertyIsEqualTo) op21 ).getParameter2() instanceof Literal );
        assertEquals( ( (Literal) ( (PropertyIsEqualTo) op21 ).getParameter2() ).getValue(), "Main St." );

        Operator op22 = andOp2.getParameter( 1 );
        assertEquals( op22.getType(), COMPARISON );
        BinaryComparisonOperator compOp22 = (BinaryComparisonOperator) op22;
        assertTrue( compOp22 instanceof PropertyIsEqualTo );
        assertTrue( ( (PropertyIsEqualTo) op22 ).getParameter1() instanceof PropertyName );
        assertEquals( ( (PropertyName) ( (PropertyIsEqualTo) op22 ).getParameter1() ).getPropertyName(),
                      "myns:Person/myns:mailAddress/myns:Address/myns:city" );
        assertTrue( ( (PropertyIsEqualTo) op22 ).getParameter2() instanceof Literal );
        assertEquals( ( (Literal) ( (PropertyIsEqualTo) op22 ).getParameter2() ).getValue(), "SomeTown" );

        Operator op23 = andOp2.getParameter( 2 );
        assertEquals( op23.getType(), COMPARISON );
        BinaryComparisonOperator compOp23 = (BinaryComparisonOperator) op23;
        assertTrue( compOp23 instanceof PropertyIsEqualTo );
        assertTrue( ( (PropertyIsEqualTo) op23 ).getParameter1() instanceof PropertyName );
        assertEquals( ( (PropertyName) ( (PropertyIsEqualTo) op23 ).getParameter1() ).getPropertyName(),
                      "myns:Person/myns:sex" );
        assertTrue( ( (PropertyIsEqualTo) op23 ).getParameter2() instanceof Literal );
        assertEquals( ( (Literal) ( (PropertyIsEqualTo) op23 ).getParameter2() ).getValue(), "Female" );

        Operator op24 = andOp2.getParameter( 3 );
        assertEquals( op24.getType(), COMPARISON );
        BinaryComparisonOperator compOp24 = (BinaryComparisonOperator) op24;
        assertTrue( compOp24 instanceof PropertyIsGreaterThan );
        assertTrue( ( (PropertyIsGreaterThan) op24 ).getParameter1() instanceof PropertyName );
        assertEquals( ( (PropertyName) ( (PropertyIsGreaterThan) op24 ).getParameter1() ).getPropertyName(),
                      "myns:Person/myns:salary" );
        assertTrue( ( (PropertyIsGreaterThan) op24 ).getParameter2() instanceof Literal );
        assertEquals( ( (Literal) ( (PropertyIsGreaterThan) op24 ).getParameter2() ).getValue(), "35000" );

    }

    /**
     * @throws Exception
     *             When the version of the GetFeature document is not supported for parsing (superfluous in this case,
     *             since we are testing 1.1.0 files and parsing is supported for this version)
     */
    @Test
    public void test_V110_EXAMPLE10()
                            throws Exception {

        URL exampleURL = this.getClass().getResource( V110_EXAMPLE10 );
        XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );

        GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
        getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
        GetFeature getFeature = getFeatureAdapter.parse( null );

        Query[] queries = getFeature.getQueries();
        FilterQuery filterQuery = (FilterQuery) queries[0];

        PropertyName[] propNames = filterQuery.getPropertyNames();

        assertEquals( propNames.length, 2 );
        assertEquals( propNames[0].getPropertyName(), "gml:name" );
        assertEquals( propNames[1].getPropertyName(), "gml:directedNode" );

        IdFilter filter = (IdFilter) filterQuery.getFilter();
        Set<String> ids = filter.getMatchingIds();

        assertEquals( ids.size(), 1 );
        assertTrue( ids.contains( "t1" ) );
    }

    /**
     * @throws Exception
     *             When the version of the GetFeature document is not supported for parsing (superfluous in this case,
     *             since we are testing 1.1.0 files and parsing is supported for this version)
     */
    @Test
    public void test_V110_EXAMPLE11()
                            throws Exception {

        URL exampleURL = this.getClass().getResource( V110_EXAMPLE11 );
        XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );

        GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
        getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
        GetFeature getFeature = getFeatureAdapter.parse( null );

        Query[] queries = getFeature.getQueries();
        FilterQuery filterQuery = (FilterQuery) queries[0];

        PropertyName[] propNames = filterQuery.getPropertyNames();

        assertEquals( propNames.length, 2 );
        assertEquals( propNames[0].getPropertyName(), "gml:name" );
        assertEquals( propNames[1].getPropertyName(), "gml:directedNode" );

        IdFilter filter = (IdFilter) filterQuery.getFilter();
        Set<String> ids = filter.getMatchingIds();

        assertEquals( ids.size(), 1 );
        assertTrue( ids.contains( "t1" ) );
    }

    /**
     * @throws Exception
     *             When the version of the GetFeature document is not supported for parsing (superfluous in this case,
     *             since we are testing 1.1.0 files and parsing is supported for this version)
     */
    public void test_V110_EXAMPLE12()
                            throws Exception {

        URL exampleURL = this.getClass().getResource( V110_EXAMPLE12 );
        XMLAdapter xmlAdapter = new XMLAdapter( exampleURL );

        GetFeatureXMLAdapter getFeatureAdapter = new GetFeatureXMLAdapter();
        getFeatureAdapter.setRootElement( xmlAdapter.getRootElement() );
        GetFeature getFeature = getFeatureAdapter.parse( null );

        Query[] queries = getFeature.getQueries();
        FilterQuery filterQuery = (FilterQuery) queries[0];

        PropertyName[] propNames = filterQuery.getPropertyNames();

        assertEquals( propNames.length, 1 );
        assertEquals( propNames[0].getPropertyName(), "gml:name" );
        assertEquals( filterQuery.getXLinkPropertyNames().length, 1 );
        assertEquals( filterQuery.getXLinkPropertyNames()[0].getTraverseXlinkDepth(), "2" );
        assertEquals( filterQuery.getXLinkPropertyNames()[0].getTraverseXlinkExpiry(), new Integer( 2 ) );
        assertEquals( filterQuery.getXLinkPropertyNames()[0].getPropertyName().getPropertyName(), "gml:directedNode" );

        IdFilter filter = (IdFilter) filterQuery.getFilter();
        Set<String> ids = filter.getMatchingIds();

        assertEquals( ids.size(), 1 );
        assertTrue( ids.contains( "t1" ) );
    }
}
