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
package org.deegree.filter;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.filter.comparison.PropertyIsBetween;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.comparison.PropertyIsGreaterThan;
import org.deegree.filter.comparison.PropertyIsLessThan;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.expression.Add;
import org.deegree.filter.expression.Function;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.Not;
import org.deegree.filter.logical.Or;
import org.deegree.filter.spatial.BBOX;
import org.deegree.filter.spatial.Disjoint;
import org.deegree.filter.spatial.Overlaps;
import org.deegree.filter.spatial.Within;
import org.deegree.filter.xml.Filter100XMLDecoder;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.junit.Test;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class Filter100XMLAdapterTest extends TestCase {

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    @Test
    public void testFilter1()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter result = parse( "testdata/v100/testfilter1.xml" );
        OperatorFilter opFilter = (OperatorFilter) result;

        PropertyIsEqualTo prop = (PropertyIsEqualTo) opFilter.getOperator();
        assertEquals( "SomeProperty", ( (PropertyName) prop.getParameter1() ).getPropertyName() );
        assertEquals( "100", ( (Literal<PrimitiveValue>) prop.getParameter2() ).getValue().toString() );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    @Test
    public void testFilter2()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter result = parse( "testdata/v100/testfilter2.xml" );
        OperatorFilter opFilter = (OperatorFilter) result;

        PropertyIsLessThan prop = (PropertyIsLessThan) opFilter.getOperator();
        assertEquals( "DEPTH", ( (PropertyName) prop.getParameter1() ).getPropertyName() );
        assertEquals( "30", ( (Literal<PrimitiveValue>) prop.getParameter2() ).getValue().toString() );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    @Test
    public void testFilter3()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter result = parse( "testdata/v100/testfilter3.xml" );
        OperatorFilter opFilter = (OperatorFilter) result;

        Not op = (Not) opFilter.getOperator();
        Disjoint op1 = (Disjoint) op.getParameter();
        Object[] params = op1.getParams();
        assertEquals( "Geometry", ( (PropertyName) params[0] ).getPropertyName() );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    @Test
    public void testFilter4()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter result = parse( "testdata/v100/testfilter4.xml" );
        OperatorFilter opFilter = (OperatorFilter) result;

        BBOX op = (BBOX) opFilter.getOperator();
        Envelope env = op.getBoundingBox();
        Point min = env.getMin();
        Point max = env.getMax();

        assertEquals( 13.0983, min.get0() );
        assertEquals( 31.5899, min.get1() );
        assertEquals( 35.5472, max.get0() );
        assertEquals( 42.8143, max.get1() );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    @SuppressWarnings( { "unchecked", "boxing" })
    @Test
    public void testFilter5()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter result = parse( "testdata/v100/testfilter5.xml" );
        OperatorFilter opFilter = (OperatorFilter) result;

        And op = (And) opFilter.getOperator();
        PropertyIsLessThan prop = (PropertyIsLessThan) op.getParameter( 0 );
        PropertyName propName = (PropertyName) prop.getParameter1();
        assertEquals( "DEPTH", propName.getPropertyName() );
        Literal<PrimitiveValue> literal = (Literal<PrimitiveValue>) prop.getParameter2();
        assertEquals( "30", literal.getValue().toString() );

        Not not = (Not) op.getParameter( 1 );
        Disjoint disjoint = (Disjoint) not.getParameter();
        Object[] params = disjoint.getParams();
        PropertyName dPropName = (PropertyName) params[0];
        assertEquals( "Geometry", dPropName.getPropertyName() );
        Envelope box = (Envelope) params[1];

        Point min = box.getMin();
        Point max = box.getMax();
        assertEquals( 13.0983, min.get0() );
        assertEquals( 31.5899, min.get1() );
        assertEquals( 35.5472, max.get0() );
        assertEquals( 42.8143, max.get1() );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    @Test
    public void testFilter6()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter result = parse( "testdata/v100/testfilter6.xml" );
        IdFilter idFilter = (IdFilter) result;

        Set<String> matchingIds = idFilter.getMatchingIds();
        assertTrue( matchingIds.contains( "TREESA_1M.1234" ) );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    @Test
    public void testFilter7()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter result = parse( "testdata/v100/testfilter7.xml" );
        IdFilter idFilter = (IdFilter) result;

        Set<String> matchingIds = idFilter.getMatchingIds();
        assertTrue( matchingIds.contains( "TREESA_1M.1234" ) );
        assertTrue( matchingIds.contains( "TREESA_1M.5678" ) );
        assertTrue( matchingIds.contains( "TREESA_1M.9012" ) );
        assertTrue( matchingIds.contains( "INWATERA_1M.3456" ) );
        assertTrue( matchingIds.contains( "INWATERA_1M.7890" ) );
        assertTrue( matchingIds.contains( "BUILTUPA_1M.4321" ) );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testFilter8()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter result = parse( "testdata/v100/testfilter8.xml" );
        OperatorFilter opFilter = (OperatorFilter) result;

        PropertyIsEqualTo prop = (PropertyIsEqualTo) opFilter.getOperator();
        Function func = (Function) prop.getParameter1();
        assertEquals( "SIN", func.getName() );
        List<Expression> params = func.getParameters();
        PropertyName propName = (PropertyName) params.get( 0 );
        assertEquals( "DISPERSION_ANGLE", propName.getPropertyName() );

        Literal<PrimitiveValue> lit = (Literal<PrimitiveValue>) prop.getParameter2();
        assertEquals( "1", lit.getValue().toString() );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testFilter9()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter result = parse( "testdata/v100/testfilter9.xml" );
        OperatorFilter opFilter = (OperatorFilter) result;

        PropertyIsEqualTo prop = (PropertyIsEqualTo) opFilter.getOperator();
        PropertyName propName = (PropertyName) prop.getParameter1();
        assertEquals( "PROPA", propName.getPropertyName() );

        Add add = (Add) prop.getParameter2();
        PropertyName propB = (PropertyName) add.getParameter1();
        assertEquals( "PROPB", propB.getPropertyName() );

        Literal<PrimitiveValue> lit = (Literal<PrimitiveValue>) add.getParameter2();
        assertEquals( "100", lit.getValue().toString() );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testFilter10()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter result = parse( "testdata/v100/testfilter10.xml" );
        OperatorFilter opFilter = (OperatorFilter) result;

        PropertyIsBetween prop = (PropertyIsBetween) opFilter.getOperator();
        Literal<PrimitiveValue> lb = (Literal<PrimitiveValue>) prop.getLowerBoundary();
        assertEquals( "100", lb.getValue().toString() );

        Literal<PrimitiveValue> ub = (Literal<PrimitiveValue>) prop.getUpperBoundary();
        assertEquals( "200", ub.getValue().toString() );

        PropertyName propName = (PropertyName) prop.getExpression();
        assertEquals( "DEPTH", propName.getPropertyName() );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testFilter11()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter result = parse( "testdata/v100/testfilter11.xml" );
        OperatorFilter opFilter = (OperatorFilter) result;

        PropertyIsBetween prop = (PropertyIsBetween) opFilter.getOperator();
        Literal<PrimitiveValue> lb = (Literal<PrimitiveValue>) prop.getLowerBoundary();
        assertEquals( "2001-01-15T20:07:48.11", lb.getValue().toString() );

        Literal<PrimitiveValue> ub = (Literal<PrimitiveValue>) prop.getUpperBoundary();
        assertEquals( "2001-03-06T12:00:00.00", ub.getValue().toString() );

        PropertyName propName = (PropertyName) prop.getExpression();
        assertEquals( "SAMPLE_DATE", propName.getPropertyName() );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    @Test
    public void testFilter12()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter result = parse( "testdata/v100/testfilter12.xml" );
        OperatorFilter opFilter = (OperatorFilter) result;

        PropertyIsLike propIsLike = (PropertyIsLike) opFilter.getOperator();
        assertEquals( "!", propIsLike.getEscapeChar() );
        assertEquals( "#", propIsLike.getSingleChar() );
        assertEquals( "*", propIsLike.getWildCard() );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    @SuppressWarnings("boxing")
    @Test
    public void testFilter13()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter result = parse( "testdata/v100/testfilter13.xml" );
        OperatorFilter opFilter = (OperatorFilter) result;

        Overlaps overlaps = (Overlaps) opFilter.getOperator();
        Object[] params = overlaps.getParams();
        PropertyName propName = (PropertyName) params[0];
        assertEquals( "Geometry", propName.getPropertyName() );

        Polygon polygon = (Polygon) params[1];
        LinearRing linearRing = (LinearRing) polygon.getExteriorRing();
        Points points = linearRing.getControlPoints();
        assertEquals( 0.0, points.get( 0 ).get0() );
        assertEquals( 0.0, points.get( 0 ).get1() );
        assertEquals( 1.0, points.get( 1 ).get0() );
        assertEquals( 1.0, points.get( 1 ).get1() );
        assertEquals( 1.0, points.get( 2 ).get0() );
        assertEquals( 2.0, points.get( 2 ).get1() );
        assertEquals( 2.0, points.get( 3 ).get0() );
        assertEquals( 2.0, points.get( 3 ).get1() );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testFilter14()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter result = parse( "testdata/v100/testfilter14.xml" );
        OperatorFilter opFilter = (OperatorFilter) result;

        And and = (And) opFilter.getOperator();
        Or or = (Or) and.getParameter( 0 );
        PropertyIsEqualTo prop0 = (PropertyIsEqualTo) or.getParameter( 0 );
        assertEquals( "FIELD1", ( (PropertyName) prop0.getParameter1() ).getPropertyName() );
        assertEquals( "10", ( (Literal<PrimitiveValue>) prop0.getParameter2() ).getValue().toString() );

        PropertyIsEqualTo prop1 = (PropertyIsEqualTo) or.getParameter( 1 );
        assertEquals( "FIELD1", ( (PropertyName) prop1.getParameter1() ).getPropertyName() );
        assertEquals( "20", ( (Literal<PrimitiveValue>) prop1.getParameter2() ).getValue().toString() );

        PropertyIsEqualTo prop01 = (PropertyIsEqualTo) and.getParameter( 1 );
        assertEquals( "STATUS", ( (PropertyName) prop01.getParameter1() ).getPropertyName() );
        assertEquals( "VALID", ( (Literal<PrimitiveValue>) prop01.getParameter2() ).getValue().toString() );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    @SuppressWarnings("boxing")
    @Test
    public void testFilter15()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter result = parse( "testdata/v100/testfilter15.xml" );
        OperatorFilter opFilter = (OperatorFilter) result;

        And and = (And) opFilter.getOperator();
        Within within = (Within) and.getParameter( 0 );
        PropertyName propName = within.getPropName();
        assertEquals( "WKB_GEOM", propName.getPropertyName() );
        Polygon polygon = (Polygon) within.getGeometry();
        Points points = polygon.getExteriorRingCoordinates();
        // -98.5485,24.2633 100.0,100.0 100.0,100.0 100.0,100.0
        assertEquals( -98.5485, points.get( 0 ).get0() );
        assertEquals( 24.2633, points.get( 0 ).get1() );
        assertEquals( 100.0, points.get( 1 ).get0() );
        assertEquals( 100.0, points.get( 1 ).get1() );
        assertEquals( 100.0, points.get( 2 ).get0() );
        assertEquals( 100.0, points.get( 2 ).get1() );
        assertEquals( 100.0, points.get( 3 ).get0() );
        assertEquals( 100.0, points.get( 3 ).get1() );
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testFilter16()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Filter result = parse( "testdata/v100/testfilter16.xml" );
        OperatorFilter opFilter = (OperatorFilter) result;

        And and = (And) opFilter.getOperator();
        PropertyIsGreaterThan pg = (PropertyIsGreaterThan) and.getParameter( 0 );
        PropertyName pn1 = (PropertyName) pg.getParameter1();
        assertEquals( "Person/Age", pn1.getPropertyName() );
        Literal<PrimitiveValue> lit1 = (Literal<PrimitiveValue>) pg.getParameter2();
        assertEquals( "50", lit1.getValue().toString() );

        PropertyIsEqualTo pe = (PropertyIsEqualTo) and.getParameter( 1 );
        PropertyName pn2 = (PropertyName) pe.getParameter1();
        assertEquals( "Person/Address/City", pn2.getPropertyName() );
        Literal<PrimitiveValue> lit2 = (Literal<PrimitiveValue>) pe.getParameter2();
        assertEquals( "Toronto", lit2.getValue().toString() );
    }

    private Filter parse( String resourceName )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        URL url = Filter100XMLAdapterTest.class.getResource( resourceName );
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( url.toString(),
                                                                                         url.openStream() );
        xmlStream.nextTag();

        return Filter100XMLDecoder.parse( xmlStream );
    }
}