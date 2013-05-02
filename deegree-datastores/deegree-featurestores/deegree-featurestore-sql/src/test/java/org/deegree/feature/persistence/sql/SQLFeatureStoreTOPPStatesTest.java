//$HeadURL$
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
import static org.deegree.commons.tom.primitive.BaseType.DOUBLE;
import static org.deegree.commons.tom.primitive.BaseType.STRING;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.protocol.wfs.transaction.action.IDGenMode.GENERATE_NEW;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.param.DefaultJDBCParams;
import org.deegree.commons.jdbc.param.JDBCParams;
import org.deegree.commons.jdbc.param.JDBCParamsManager;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.test.TestDBProperties;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.ddl.DDLCreator;
import org.deegree.feature.persistence.sql.mapper.AppSchemaMapper;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.function.FunctionManager;
import org.deegree.filter.spatial.BBOX;
import org.deegree.geometry.GeometryFactory;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.SQLDialectManager;
import org.deegree.sqldialect.filter.function.SQLFunctionManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic {@link SQLFeatureStore} test for table-based configurations.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@RunWith(value = Parameterized.class)
public class SQLFeatureStoreTOPPStatesTest {

    private static Logger LOG = LoggerFactory.getLogger( SQLFeatureStoreTOPPStatesTest.class );

    private static final QName TOPP_STATES = QName.valueOf( "{http://www.openplans.org/topp}states" );

    private static final QName STATE_NAME = QName.valueOf( "{http://www.openplans.org/topp}STATE_NAME" );

    private static final QName STATE_FIPS = QName.valueOf( "{http://www.openplans.org/topp}STATE_FIPS" );

    private static final QName SAMP_POP = QName.valueOf( "{http://www.openplans.org/topp}SAMP_POP" );

    private final TestDBProperties settings;

    private DeegreeWorkspace ws;

    private SQLDialect dialect;

    private SQLFeatureStore fs;

    public SQLFeatureStoreTOPPStatesTest( TestDBProperties settings ) {
        this.settings = settings;
    }

    @Before
    public void setUp()
                            throws Throwable {

        initWorkspace();
        createDB();
        createTables();

        SQLFeatureStoreProvider provider = new SQLFeatureStoreProvider();
        provider.init( ws );
        fs = provider.create( SQLFeatureStoreTOPPStatesTest.class.getResource( "topp_states/topp_states.xml" ) );
        fs.init( ws );

        populateStore();
    }

    private void populateStore()
                            throws Throwable {
        URL datasetURL = SQLFeatureStoreTOPPStatesTest.class.getResource( "topp_states/data/states.xml" );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GML_2, datasetURL );
        gmlReader.setApplicationSchema( fs.getSchema() );
        FeatureCollection fc = gmlReader.readFeatureCollection();
        Assert.assertEquals( 49, fc.size() );
        gmlReader.close();

        FeatureStoreTransaction ta = fs.acquireTransaction();
        try {
            List<String> fids = ta.performInsert( fc, GENERATE_NEW );
            Assert.assertEquals( 49, fids.size() );
            ta.commit();
        } catch ( Throwable t ) {
            ta.rollback();
            throw t;
        }
    }

    private void initWorkspace()
                            throws ResourceInitException {
        // TODO
        ws = DeegreeWorkspace.getInstance( "deegree-featurestore-sql-tests" );
        ws.initManagers();
        ws.getSubsystemManager( ConnectionManager.class ).startup( ws );
        ws.getSubsystemManager( JDBCParamsManager.class ).startup( ws );
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

        // read application schema
        URL schemaUrl = SQLFeatureStoreTOPPStatesTest.class.getResource( "topp_states/schema/states.xsd" );
        GMLAppSchemaReader decoder = new GMLAppSchemaReader( null, null, schemaUrl.toString() );
        AppSchema appSchema = decoder.extractAppSchema();

        // map application schema
        ICRS crs = CRSManager.getCRSRef( "EPSG:4326" );
        GeometryStorageParams storageParams = new GeometryStorageParams( crs, dialect.getUndefinedSrid(), DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, storageParams,
                                                      dialect.getMaxTableNameLength(), false, true );
        MappedAppSchema mappedSchema = mapper.getMappedSchema();

        // create tables
        String[] ddl = DDLCreator.newInstance( mappedSchema, dialect ).getDDL();

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
    public void testSchema() {
        Assert.assertEquals( 1, fs.getSchema().getFeatureTypes().length );
        FeatureType ft = fs.getSchema().getFeatureTypes()[0];
        Assert.assertEquals( TOPP_STATES, ft.getName() );
        Assert.assertEquals( 23, ft.getPropertyDeclarations().size() );
    }

    @Test
    public void queryByStateName()
                            throws FeatureStoreException, FilterEvaluationException {
        ValueReference propName = new ValueReference( STATE_NAME );
        Literal literal = new Literal( "Illinois" );
        PropertyIsEqualTo oper = new PropertyIsEqualTo( propName, literal, false, null );
        Filter filter = new OperatorFilter( oper );
        Query query = new Query( TOPP_STATES, filter, -1, -1, -1 );
        FeatureCollection fc = fs.query( query ).toCollection();
        Assert.assertEquals( 1, fc.size() );

        Feature f = fc.iterator().next();
        Assert.assertEquals( 23, ( f.getProperties().size() ) );

        assertEquals( "Illinois", getPropertyValue( f, STATE_NAME ).getAsText() );
        assertEquals( STRING, getPropertyValue( f, STATE_NAME ).getType().getBaseType() );
        assertEquals( "17", getPropertyValue( f, STATE_FIPS ).getAsText() );
        assertEquals( STRING, getPropertyValue( f, STATE_FIPS ).getType().getBaseType() );
        assertEquals( 1747776.0, ( (Double) getPropertyValue( f, SAMP_POP ).getValue() ), 0.001 );
        assertEquals( DOUBLE, getPropertyValue( f, SAMP_POP ).getType().getBaseType() );
    }

    @Test
    public void queryByBBOX()
                            throws FeatureStoreException, FilterEvaluationException, UnknownCRSException {

        BBOX oper = new BBOX( new GeometryFactory().createEnvelope( -75.102613, 40.212597, -72.361859, 41.512517,
                                                                    CRSManager.lookup( "EPSG:4326" ) ) );
        Filter filter = new OperatorFilter( oper );
        Query query = new Query( TOPP_STATES, filter, -1, -1, -1 );
        FeatureCollection fc = fs.query( query ).toCollection();
        Assert.assertEquals( 4, fc.size() );

        Set<String> stateNames = new HashSet<String>();
        for ( Feature f : fc ) {
            stateNames.add( getPropertyValue( f, STATE_NAME ).getAsText() );
        }

        Assert.assertTrue( stateNames.contains( "New York" ) );
        Assert.assertTrue( stateNames.contains( "Pennsylvania" ) );
        Assert.assertTrue( stateNames.contains( "Connecticut" ) );
        Assert.assertTrue( stateNames.contains( "New Jersey" ) );
    }

    private PrimitiveValue getPropertyValue( Feature f, QName propName ) {
        return (PrimitiveValue) f.getProperties( propName ).get( 0 ).getValue();
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
