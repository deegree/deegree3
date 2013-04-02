//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-datastores/deegree-featurestores/deegree-featurestore-sql/src/test/java/org/deegree/feature/persistence/sql/TOPPStatesTest.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.feature.persistence.sql;

import static junit.framework.Assert.assertEquals;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.cs.persistence.CRSManager.lookup;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.protocol.wfs.transaction.action.IDGenMode.GENERATE_NEW;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.param.DefaultJDBCParams;
import org.deegree.commons.jdbc.param.JDBCParams;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.test.TestDBProperties;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.ddl.DDLCreator;
import org.deegree.feature.xpath.TypedObjectNodeXPathEvaluator;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.comparison.PropertyIsLessThanOrEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.function.FunctionManager;
import org.deegree.filter.spatial.BBOX;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Point;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.SQLDialectManager;
import org.deegree.sqldialect.filter.function.SQLFunctionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SQLFeatureStore} test for peculiar aspects of mapping AIXM.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 32701 $, $Date: 2012-01-17 22:35:34 +0100 (Di, 17. Jan 2012) $
 */
@RunWith(value = Parameterized.class)
public class SQLFeatureStoreAIXMTest {

    private static Logger LOG = LoggerFactory.getLogger( SQLFeatureStoreAIXMTest.class );

    private static final QName HELIPORT_NAME = QName.valueOf( "{http://www.aixm.aero/schema/5.1}AirportHeliport" );

    private static final QName GML_IDENTIFIER = QName.valueOf( "{http://www.opengis.net/gml/3.2}identifier" );

    private final NamespaceBindings nsContext;

    private final TestDBProperties settings;

    private DeegreeWorkspace ws;

    private SQLDialect dialect;

    private SQLFeatureStore fs;

    public SQLFeatureStoreAIXMTest( TestDBProperties settings ) {
        this.settings = settings;
        nsContext = new NamespaceBindings();
        nsContext.addNamespace( "aixm", "http://www.aixm.aero/schema/5.1" );
        nsContext.addNamespace( "gml", "http://www.opengis.net/gml/3.2" );
    }

    @Before
    public void setUp()
                            throws Throwable {

        initWorkspaceExceptFeatureStore();
        createDB();
        createTables();
        initFeatureStore();
        populateStore();
    }

    private void populateStore()
                            throws Throwable {

        URL datasetURL = SQLFeatureStoreAIXMTest.class.getResource( "aixm/data/heliports.gml" );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GML_32, datasetURL );
        gmlReader.setApplicationSchema( fs.getSchema() );
        FeatureCollection fc = gmlReader.readFeatureCollection();
        Assert.assertEquals( 2, fc.size() );
        gmlReader.close();

        FeatureStoreTransaction ta = fs.acquireTransaction();
        try {
            List<String> fids = ta.performInsert( fc, GENERATE_NEW );
            Assert.assertEquals( 2, fids.size() );
            ta.commit();
        } catch ( Throwable t ) {
            ta.rollback();
            throw t;
        }
    }

    private void initWorkspaceExceptFeatureStore()
                            throws ResourceInitException {
        // TODO
        ws = DeegreeWorkspace.getInstance( "deegree-featurestore-sql-tests" );
        ws.initManagers();
        ws.getSubsystemManager( ConnectionManager.class ).startup( ws );
        ws.getSubsystemManager( SQLDialectManager.class ).startup( ws );
        ws.getSubsystemManager( FeatureStoreManager.class ).startup( ws );
        ws.getSubsystemManager( FunctionManager.class ).startup( ws );
        ws.getSubsystemManager( SQLFunctionManager.class ).startup( ws );

        ConnectionManager mgr = ws.getSubsystemManager( ConnectionManager.class );
        JDBCParams params = new DefaultJDBCParams( settings.getAdminUrl(), settings.getAdminUser(),
                                                   settings.getAdminPass(), false );
        mgr.addPool( "admin", params, ws );
        params = new DefaultJDBCParams( settings.getUrl(), settings.getUser(), settings.getPass(), false );
        mgr.addPool( "deegree-test", params, ws );

        dialect = ws.getSubsystemManager( SQLDialectManager.class ).create( "admin" );
    }

    private void initFeatureStore()
                            throws ResourceInitException {
        SQLFeatureStoreProvider provider = new SQLFeatureStoreProvider();
        provider.init( ws );
        fs = provider.create( SQLFeatureStoreAIXMTest.class.getResource( "aixm/datasources/feature/aixm.xml" ) );
        fs.init( ws );
    }

    private void createDB()
                            throws SQLException {
        ConnectionManager mgr = ws.getSubsystemManager( ConnectionManager.class );
        Connection adminConn = mgr.get( "admin" );
        try {
            dialect.createDB( adminConn, settings.getDbName() );
        } finally {
            adminConn.close();
        }
    }

    private void createTables()
                            throws Exception {

        SQLFeatureStoreProvider provider = new SQLFeatureStoreProvider();
        provider.init( ws );
        SQLFeatureStore fs = provider.create( SQLFeatureStoreAIXMTest.class.getResource( "aixm/datasources/feature/aixm.xml" ) );
        fs.init( ws );

        // create tables
        String[] ddl = DDLCreator.newInstance( fs.getSchema(), dialect ).getDDL();

        ConnectionManager mgr = ws.getSubsystemManager( ConnectionManager.class );
        Connection conn = mgr.get( "deegree-test" );
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            for ( String sql : ddl ) {
                stmt.execute( sql );
            }
        } finally {
            stmt.close();
            conn.close();
        }
        fs.destroy();
    }

    @After
    public void tearDown()
                            throws Exception {
        ConnectionManager mgr = ws.getSubsystemManager( ConnectionManager.class );
        Connection adminConn = mgr.get( "admin" );
        mgr.deactivate( "deegree-test" );
        try {
            dialect.dropDB( adminConn, settings.getDbName() );
        } finally {
            adminConn.close();
        }
        ws.destroyAll();
        fs.destroy();
    }

    @Test
    public void testElevatedPointReconstruction()
                            throws FeatureStoreException, FilterEvaluationException, UnknownCRSException {

        ValueReference propName = new ValueReference( GML_IDENTIFIER );
        Literal literal = new Literal( "dd062d88-3e64-4a5d-bebd-89476db9ebea" );
        PropertyIsEqualTo oper = new PropertyIsEqualTo( propName, literal, false, null );
        Filter filter = new OperatorFilter( oper );
        Query query = new Query( HELIPORT_NAME, filter, -1, -1, -1 );
        FeatureCollection fc = fs.query( query ).toCollection();
        Feature f = fc.iterator().next();
        Point geom = (Point) getGeometry( "aixm:timeSlice/aixm:AirportHeliportTimeSlice/aixm:ARP/aixm:ElevatedPoint", f );

        String aixmNs = "http://www.aixm.aero/schema/5.1";

        double DELTA = 0.00000001;
        assertEquals( -32.035, geom.get0(), DELTA );
        assertEquals( 52.288888888888884, geom.get1(), DELTA );
        assertEquals( 2, geom.getCoordinateDimension() );
        assertEquals( CRSManager.lookup( "urn:ogc:def:crs:EPSG:4326" ), geom.getCoordinateSystem() );

        List<Property> props = geom.getProperties();
        assertEquals( 11, props.size() );
        int i = 0;
        // assertEquals( new QName( GML3_2_NS, "metaDataProperty" ), props.get( 0 ).getName() );
        assertEquals( new QName( GML3_2_NS, "description" ), props.get( i++ ).getName() );
        assertEquals( new QName( GML3_2_NS, "descriptionReference" ), props.get( i++ ).getName() );
        assertEquals( new QName( GML3_2_NS, "identifier" ), props.get( i++ ).getName() );
        assertEquals( new QName( GML3_2_NS, "name" ), props.get( i++ ).getName() );
        assertEquals( new QName( GML3_2_NS, "name" ), props.get( i++ ).getName() );
        assertEquals( new QName( aixmNs, "horizontalAccuracy" ), props.get( i++ ).getName() );
        assertEquals( new QName( aixmNs, "annotation" ), props.get( i++ ).getName() );
        assertEquals( new QName( aixmNs, "elevation" ), props.get( i++ ).getName() );
        assertEquals( new QName( aixmNs, "geoidUndulation" ), props.get( i++ ).getName() );
        assertEquals( new QName( aixmNs, "verticalDatum" ), props.get( i++ ).getName() );
        assertEquals( new QName( aixmNs, "verticalAccuracy" ), props.get( i++ ).getName() );

        // assertEquals( "Example for metadata: Ce point ne pas une GML point, c'est une AIXM point.",
        // getPrimitive( "gml:metaDataProperty/gml:GenericMetaData/text()", geom ).getAsText() );
        assertEquals( "This is just for testing the parsing of standard GML properties.",
                      getPrimitive( "gml:description/text()", geom ).getValue() );
        assertEquals( "XYZ", getPrimitive( "gml:identifier/text()", geom ).getValue() );
        assertEquals( "urn:blabla:bla", getPrimitive( "gml:identifier/@codeSpace", geom ).getValue() );
        assertEquals( "Point P1", getPrimitive( "gml:name[1]/text()", geom ).getValue() );
        assertEquals( "P1", getPrimitive( "gml:name[2]/text()", geom ).getValue() );
        assertEquals( new BigDecimal( 1 ), getPrimitive( "aixm:horizontalAccuracy/text()", geom ).getValue() );
        assertEquals( "M", getPrimitive( "aixm:horizontalAccuracy/@uom", geom ).getValue() );
        assertEquals( "18.0", getPrimitive( "aixm:elevation/text()", geom ).getValue() );
        assertEquals( "M", getPrimitive( "aixm:elevation/@uom", geom ).getValue() );
        assertEquals( new BigDecimal( 3.22 ).doubleValue(),
                      ( (BigDecimal) getPrimitive( "aixm:geoidUndulation/text()", geom ).getValue() ).doubleValue(),
                      DELTA );
        assertEquals( "M", getPrimitive( "aixm:geoidUndulation/@uom", geom ).getValue() );
        assertEquals( "NAVD88", getPrimitive( "aixm:verticalDatum/text()", geom ).getValue() );
        assertEquals( new BigDecimal( 2 ), getPrimitive( "aixm:verticalAccuracy/text()", geom ).getValue() );
        assertEquals( "M", getPrimitive( "aixm:verticalAccuracy/@uom", geom ).getValue() );
    }

    private Geometry getGeometry( String xpath, GMLObject object )
                            throws FilterEvaluationException {
        TypedObjectNodeXPathEvaluator evaluator = new TypedObjectNodeXPathEvaluator();
        NamespaceBindings nsContext = new NamespaceBindings();
        nsContext.addNamespace( "gml", GML3_2_NS );
        nsContext.addNamespace( "aixm", "http://www.aixm.aero/schema/5.1" );
        ValueReference path = new ValueReference( xpath, nsContext );
        TypedObjectNode[] eval = evaluator.eval( object, path );
        assertEquals( 1, eval.length );
        assertTrue( eval[0] instanceof Geometry );
        return (Geometry) eval[0];
    }

    private PrimitiveValue getPrimitive( String xpath, GMLObject object )
                            throws FilterEvaluationException {
        TypedObjectNodeXPathEvaluator evaluator = new TypedObjectNodeXPathEvaluator();
        NamespaceBindings nsContext = new NamespaceBindings();
        nsContext.addNamespace( "gml", GML3_2_NS );
        nsContext.addNamespace( "aixm", "http://www.aixm.aero/schema/5.1" );
        ValueReference path = new ValueReference( xpath, nsContext );
        TypedObjectNode[] eval = evaluator.eval( object, path );
        assertEquals( 1, eval.length );
        assertTrue( eval[0] instanceof PrimitiveValue );
        return (PrimitiveValue) eval[0];
    }

    @Test
    public void queryAllHeliports()
                            throws FeatureStoreException, FilterEvaluationException {

        Query query = new Query( HELIPORT_NAME, null, -1, -1, -1 );
        FeatureCollection fc = fs.query( query ).toCollection();
        assertEquals( 2, fc.size() );
    }

    @Test
    public void queryHeliportByGmlIdentifier()
                            throws FeatureStoreException, FilterEvaluationException {

        ValueReference propName = new ValueReference( GML_IDENTIFIER );
        Literal literal = new Literal( "1b54b2d6-a5ff-4e57-94c2-f4047a381c64" );
        PropertyIsEqualTo oper = new PropertyIsEqualTo( propName, literal, false, null );
        Filter filter = new OperatorFilter( oper );
        Query query = new Query( HELIPORT_NAME, filter, -1, -1, -1 );
        FeatureCollection fc = fs.query( query ).toCollection();
        assertEquals( 1, fc.size() );
    }

    @Test
    public void queryHeliportByElevatedPointElevation()
                            throws FeatureStoreException, FilterEvaluationException {

        ValueReference propName = new ValueReference(
                                                      "aixm:timeSlice/aixm:AirportHeliportTimeSlice/aixm:ARP/aixm:ElevatedPoint/aixm:elevation",
                                                      nsContext );
        Literal literal = new Literal( "18.0" );
        PropertyIsLessThanOrEqualTo oper = new PropertyIsLessThanOrEqualTo( propName, literal, false, null );
        Filter filter = new OperatorFilter( oper );
        Query query = new Query( HELIPORT_NAME, filter, -1, -1, -1 );
        FeatureCollection fc = fs.query( query ).toCollection();
        Assert.assertEquals( 1, fc.size() );
    }

    @Test
    public void queryHeliportByBboxPathToGeometry()
                            throws FeatureStoreException, FilterEvaluationException, UnknownCRSException {

        ValueReference propName = new ValueReference(
                                                      "aixm:timeSlice/aixm:AirportHeliportTimeSlice/aixm:ARP/aixm:ElevatedPoint",
                                                      nsContext );
        Envelope env = new GeometryFactory().createEnvelope( -32.036, 52.288, -32.034, 52.289,
                                                             lookup( "urn:ogc:def:crs:EPSG:4326" ) );
        BBOX oper = new BBOX( propName, env );
        Filter filter = new OperatorFilter( oper );
        Query query = new Query( HELIPORT_NAME, filter, -1, -1, -1 );
        FeatureCollection fc = fs.query( query ).toCollection();
        Assert.assertEquals( 1, fc.size() );
    }

    @Test
    public void queryHeliportByBboxPathToGeometryProperty()
                            throws FeatureStoreException, FilterEvaluationException, UnknownCRSException {

        ValueReference propName = new ValueReference( "aixm:timeSlice/aixm:AirportHeliportTimeSlice/aixm:ARP",
                                                      nsContext );
        Envelope env = new GeometryFactory().createEnvelope( -32.036, 52.288, -32.034, 52.289,
                                                             lookup( "urn:ogc:def:crs:EPSG:4326" ) );
        BBOX oper = new BBOX( propName, env );
        Filter filter = new OperatorFilter( oper );
        Query query = new Query( HELIPORT_NAME, filter, -1, -1, -1 );
        FeatureCollection fc = fs.query( query ).toCollection();
        Assert.assertEquals( 1, fc.size() );
    }

    @Test
    public void queryHeliportByBboxNoPropertyName()
                            throws FeatureStoreException, FilterEvaluationException, UnknownCRSException {

        Envelope env = new GeometryFactory().createEnvelope( -32.036, 52.288, -32.034, 52.289,
                                                             lookup( "urn:ogc:def:crs:EPSG:4326" ) );
        BBOX oper = new BBOX( env );
        Filter filter = new OperatorFilter( oper );
        Query query = new Query( HELIPORT_NAME, filter, -1, -1, -1 );
        FeatureCollection fc = fs.query( query ).toCollection();
        Assert.assertEquals( 1, fc.size() );
    }

    @Parameters
    public static Collection<TestDBProperties[]> data()
                            throws IllegalArgumentException, IOException {
        List<TestDBProperties[]> settings = new ArrayList<TestDBProperties[]>();
        try {
            for ( TestDBProperties testDBSettings : TestDBProperties.getAll() ) {
                settings.add( new TestDBProperties[] { testDBSettings } );
            }
        } catch ( Throwable t ) {
            LOG.error( "Access to test databases not configured properly: " + t.getMessage() );
        }
        return settings;
    }
}
