//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2019 by:
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
package org.deegree.services.wfs.query;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.filter.projection.PropertyName;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.query.FilterQuery;
import org.deegree.services.wfs.WebFeatureService;
import org.deegree.services.wfs.WfsFeatureStoreManager;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class QueryAnalyzerTest {

    private static final String NAMESPACE_URI_CP = "http://inspire.ec.europa.eu/schemas/cp/4.0";

    private static final String NAMESPACE_URI_AU = "http://inspire.ec.europa.eu/schemas/au/4.0";

    private QName FEATURETYPE_CP = new QName( NAMESPACE_URI_CP, "CadastralZoning" );

    private QName FEATURETYPE_AU = new QName( NAMESPACE_URI_AU, "AdministrativeUnit" );

    private QName PROP_CP_BLV = new QName( NAMESPACE_URI_CP, "beginLifespanVersion" );

    private QName PROP_CP_EA = new QName( NAMESPACE_URI_CP, "estimatedAccuracy" );

    private QName PROP_AU_BLV = new QName( NAMESPACE_URI_AU, "beginLifespanVersion" );

    @Test
    public void testQueryAnalyzer()
                            throws Exception {
        List<org.deegree.protocol.wfs.query.Query> wfsQueries = createQueries();
        WebFeatureService controller = mockController();
        WfsFeatureStoreManager manager = mockManager();
        QueryAnalyzer queryAnalyzer = new QueryAnalyzer( wfsQueries, controller, manager, false );

        Map<FeatureStore, List<Query>> queries = queryAnalyzer.getQueries();
        assertThat( queries.size(), is( 1 ) );
        assertThat( queries.values().iterator().next().size(), is( 2 ) );

        Map<QName, List<ProjectionClause>> projections = queryAnalyzer.getProjections();
        assertThat( projections.size(), is( 2 ) );
        assertThat( projections.get( FEATURETYPE_CP ).size(), is( 2 ) );
        assertThat( projections.get( FEATURETYPE_AU ).size(), is( 1 ) );
    }

    private List<org.deegree.protocol.wfs.query.Query> createQueries()
                            throws UnknownCRSException {
        List<org.deegree.protocol.wfs.query.Query> queries = new ArrayList<>();
        queries.add( createQueryCadastralZoning() );
        queries.add( createQueryAdministrativeUnit() );
        return queries;
    }

    private org.deegree.protocol.wfs.query.Query createQueryCadastralZoning()
                            throws UnknownCRSException {
        TypeName[] typeNames = createTypeName( FEATURETYPE_CP );
        ProjectionClause[] projectionClauses = { propName( PROP_CP_BLV ), propName( PROP_CP_EA ) };
        return new FilterQuery( null, typeNames, null, CRSManager.lookup( "EPSG:4326" ), projectionClauses, null, null );
    }

    private org.deegree.protocol.wfs.query.Query createQueryAdministrativeUnit()
                            throws UnknownCRSException {
        TypeName[] typeNames = createTypeName( FEATURETYPE_AU );
        ProjectionClause[] projectionClauses = { propName( PROP_AU_BLV ) };
        return new FilterQuery( null, typeNames, null, CRSManager.lookup( "EPSG:4326" ), projectionClauses, null, null );
    }

    private TypeName[] createTypeName( QName ftName ) {
        TypeName typeName = new TypeName( ftName, null );
        return new TypeName[] { typeName };
    }

    private ProjectionClause propName( QName propName ) {
        ValueReference valueRef = new ValueReference( propName );
        return new PropertyName( valueRef, null, null );
    }

    private WebFeatureService mockController() {
        WebFeatureService mock = mock( WebFeatureService.class );
        return mock;
    }

    private WfsFeatureStoreManager mockManager() {
        WfsFeatureStoreManager mock = mock( WfsFeatureStoreManager.class );
        FeatureType featureTypeCp = mock( FeatureType.class );
        when( featureTypeCp.getName() ).thenReturn( FEATURETYPE_CP );
        when( featureTypeCp.getPropertyDeclaration( PROP_CP_BLV ) ).thenReturn( mock( PropertyType.class ) );
        when( featureTypeCp.getPropertyDeclaration( PROP_CP_EA ) ).thenReturn( mock( PropertyType.class ) );
        when( mock.lookupFeatureType( FEATURETYPE_CP ) ).thenReturn( featureTypeCp );
        FeatureType featureTypeAu = mock( FeatureType.class );
        when( featureTypeAu.getName() ).thenReturn( FEATURETYPE_AU );
        when( featureTypeAu.getPropertyDeclaration( PROP_AU_BLV ) ).thenReturn( mock( PropertyType.class ) );
        when( mock.lookupFeatureType( FEATURETYPE_AU ) ).thenReturn( featureTypeAu );
        when( mock.getFeatureTypeNames() ).thenReturn( new QName[] { FEATURETYPE_CP, FEATURETYPE_AU } );
        return mock;
    }
    /*
     * 
     * <wfs:GetFeature service="WFS" version="2.0.0" xmlns:wfs="http://www.opengis.net/wfs/2.0"
     * xmlns:fes="http://www.opengis.net/fes/2.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     * xmlns:gml="http://www.opengis.net/gml/3.2"
     * xsi:schemaLocation="http://www.opengis.net/wfs/2.0 http://schemas.opengis.net/wfs/2.0/wfs.xsd"
     * xmlns:de.hh.up="https://registry.gdi-de.org/id/de.hh.up"> <wfs:Query typeNames="de.hh.up:ortsteile">
     * <wfs:PropertyName>de.hh.up:bezirk_name</wfs:PropertyName> <wfs:PropertyName>de.hh.up:stadtteil</wfs:PropertyName>
     * <wfs:PropertyName>de.hh.up:ortsteilnummer</wfs:PropertyName> <wfs:PropertyName>de.hh.up:bezirk</wfs:PropertyName>
     * <wfs:PropertyName>de.hh.up:ags</wfs:PropertyName> <fes:Filter> <fes:Intersects>
     * <fes:ValueReference>de.hh.up:geom</fes:ValueReference> <gml:Point srsName="EPSG:4326">
     * <gml:pos>10.041668189056937 53.54036348655801</gml:pos> </gml:Point> </fes:Intersects> </fes:Filter> </wfs:Query>
     * <wfs:Query typeNames="de.hh.up:pk_grenzen"> <wfs:PropertyName>de.hh.up:polizeirevier</wfs:PropertyName>
     * <wfs:PropertyName>de.hh.up:pk</wfs:PropertyName> <fes:Filter> <fes:Intersects>
     * <fes:ValueReference>de.hh.up:geom</fes:ValueReference> <gml:Point srsName="EPSG:4326">
     * <gml:pos>10.041668189056937 53.54036348655801</gml:pos> </gml:Point> </fes:Intersects> </fes:Filter> </wfs:Query>
     * </wfs:GetFeature>
     */
}