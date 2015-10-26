//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.services.wfs.query;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.feature.types.FeatureType;
import org.deegree.services.wfs.WebFeatureService;
import org.deegree.services.wfs.WfsFeatureStoreManager;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class StoredQueryHandlerTest {

    @Test
    public void testCollectAndSortFeatureTypesToExport_AllFeatureTypes() {
        List<FeatureType> featureTypes = featureTypes();
        StoredQueryHandler storedQueryHandler = new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>() );

        List<QName> configuredFeatureTypeNames = Collections.emptyList();
        List<QName> featureTypeNamesToExport = storedQueryHandler.collectAndSortFeatureTypesToExport( configuredFeatureTypeNames );

        assertThat( featureTypeNamesToExport.size(), is( featureTypes.size() ) );
        for ( FeatureType featureType : featureTypes ) {
            assertThat( featureTypeNamesToExport, hasItems( featureType.getName() ) );
        }
    }

    @Test
    public void testCollectAndSortFeatureTypesToExport_EmptyFeatureTypeList() {
        List<FeatureType> featureTypes = Collections.emptyList();
        StoredQueryHandler storedQueryHandler = new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>() );

        List<QName> configuredFeatureTypeNames = Collections.emptyList();
        List<QName> featureTypeNamesToExport = storedQueryHandler.collectAndSortFeatureTypesToExport( configuredFeatureTypeNames );

        assertThat( featureTypeNamesToExport.size(), is( 0 ) );
    }

    @Test
    public void testCollectAndSortFeatureTypesToExport_LimitedConfiguredFeatureTypes() {
        List<FeatureType> featureTypes = featureTypes();
        StoredQueryHandler storedQueryHandler = new StoredQueryHandler( mockWFS( featureTypes ), new ArrayList<URL>() );

        List<QName> configuredFeatureTypeNames = configuredFeatureTypeNames();
        List<QName> featureTypeNamesToExport = storedQueryHandler.collectAndSortFeatureTypesToExport( configuredFeatureTypeNames );

        assertThat( featureTypeNamesToExport.size(), is( 1 ) );

        QName featureTypeNameToExport = featureTypeNamesToExport.get( 0 );
        assertThat( featureTypeNameToExport.getLocalPart(), is( "one" ) );
        assertThat( featureTypeNameToExport.getNamespaceURI(), is( "" ) );
        assertThat( featureTypeNameToExport.getPrefix(), is( "" ) );
    }

    private List<FeatureType> featureTypes() {
        List<FeatureType> featureTypes = new ArrayList<FeatureType>();
        featureTypes.add( mockFeatureType( "one" ) );
        featureTypes.add( mockFeatureType( "two" ) );
        return featureTypes;
    }

    private List<QName> configuredFeatureTypeNames() {
        List<QName> configuredFeatureTypes = new ArrayList<QName>();
        configuredFeatureTypes.add( new QName( "one" ) );
        return configuredFeatureTypes;
    }

    private FeatureType mockFeatureType( String name ) {
        FeatureType mockedFeatureType = mock( FeatureType.class );
        QName qName = new QName( name );
        when( mockedFeatureType.getName() ).thenReturn( qName );
        return mockedFeatureType;
    }

    private WebFeatureService mockWFS( Collection<FeatureType> featureTypes ) {
        WebFeatureService mockedWfs = mock( WebFeatureService.class );
        WfsFeatureStoreManager mockedStoreManager = mockStoreManager( featureTypes );
        when( mockedWfs.getStoreManager() ).thenReturn( mockedStoreManager );
        return mockedWfs;
    }

    private WfsFeatureStoreManager mockStoreManager( Collection<FeatureType> featureTypes ) {
        WfsFeatureStoreManager mockedStoreManager = mock( WfsFeatureStoreManager.class );
        when( mockedStoreManager.getFeatureTypes() ).thenReturn( featureTypes );
        return mockedStoreManager;
    }

}