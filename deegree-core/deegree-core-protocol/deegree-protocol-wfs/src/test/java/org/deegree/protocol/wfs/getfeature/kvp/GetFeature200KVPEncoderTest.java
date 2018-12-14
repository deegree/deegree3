//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.protocol.wfs.getfeature.kvp;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.query.AdHocQuery;
import org.deegree.protocol.wfs.query.Query;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class GetFeature200KVPEncoderTest {

    @Test(expected = IllegalArgumentException.class)
    public void testExport_MultipleQueries()
                            throws Exception {
        Map<String, String> kvpUnderTest = asKvp( "wfs200/example8.kvp" );
        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpUnderTest, null );
        GetFeature200KVPEncoder.export( getFeature );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExport_MissingQuery()
                            throws Exception {
        Map<String, String> kvpUnderTest = asKvp( "wfs200/example8.kvp" );
        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpUnderTest, null );
        getFeature.getQueries().clear();
        GetFeature200KVPEncoder.export( getFeature );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExport_UnsupportedQueryClass()
                            throws Exception {
        Map<String, String> kvpUnderTest = asKvp( "wfs200/example8.kvp" );
        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpUnderTest, null );
        getFeature.getQueries().clear();
        getFeature.getQueries().add( unknownQuery() );
        GetFeature200KVPEncoder.export( getFeature );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExport_UnsupportedAdHocQueryClass()
                            throws Exception {
        Map<String, String> kvpUnderTest = asKvp( "wfs200/example8.kvp" );
        GetFeature getFeature = GetFeatureKVPAdapter.parse( kvpUnderTest, null );
        getFeature.getQueries().clear();
        getFeature.getQueries().add( unknownAdHocQuery() );
        GetFeature200KVPEncoder.export( getFeature );
    }

    private Query unknownQuery() {
        return Mockito.mock( Query.class );
    }

    private Query unknownAdHocQuery() {
        return Mockito.mock( AdHocQuery.class );
    }

    private static Map<String, String> asKvp( String name )
                            throws IOException {
        URL exampleURL = GetFeature200KVPEncoderParameterizedTest.class.getResource( name );
        return KVPUtils.readFileIntoMap( exampleURL );
    }

}