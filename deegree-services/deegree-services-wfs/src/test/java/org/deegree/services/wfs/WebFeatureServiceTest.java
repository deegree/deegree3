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
package org.deegree.services.wfs;

import static org.deegree.protocol.wfs.WFSRequestType.DescribeFeatureType;
import static org.deegree.protocol.wfs.WFSRequestType.GetCapabilities;
import static org.deegree.protocol.wfs.WFSRequestType.GetFeature;
import static org.deegree.protocol.wfs.WFSRequestType.GetPropertyValue;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Set;

import org.deegree.protocol.wfs.WFSRequestType;
import org.deegree.services.jaxb.wfs.DeegreeWFS.SupportedRequests;
import org.deegree.services.jaxb.wfs.RequestType;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class WebFeatureServiceTest {

    private final WebFeatureService webFeatureService = new WebFeatureService( null, null, null );;

    @Test
    public void testParseEncodings() {
        SupportedRequests supportedRequests = prepareSupportedEncodings();

        Map<WFSRequestType, Set<String>> enabledEncodings = webFeatureService.parseEncodings( supportedRequests ).getEnabledEncodingsPerRequestType();

        Set<String> capabilitiesEncodings = enabledEncodings.get( GetCapabilities );
        assertThat( capabilitiesEncodings, hasItems( "xml", "soap" ) );

        Set<String> describeFeatureTypeEncodings = enabledEncodings.get( DescribeFeatureType );
        assertThat( describeFeatureTypeEncodings, hasItems( "xml" ) );

        Set<String> getFeatureEncodings = enabledEncodings.get( GetFeature );
        assertThat( getFeatureEncodings, hasItems( "xml" ) );

        Set<String> getPropertyValueEncodings = enabledEncodings.get( GetPropertyValue );
        assertThat( getPropertyValueEncodings.size(), is( 0 ) );
    }

    @Test
    public void testParseEncodingsKvpForAll() {
        SupportedRequests supportedRequests = prepareSupportedEncodingsKvpForAll();

        Map<WFSRequestType, Set<String>> enabledEncodings = webFeatureService.parseEncodings( supportedRequests ).getEnabledEncodingsPerRequestType();
        assertThat( enabledEncodings, hasEncodingForAllWfsRequestTypes( "kvp" ) );

        Set<String> capabilitiesEncodings = enabledEncodings.get( GetCapabilities );
        assertThat( capabilitiesEncodings, hasItems( "kvp", "xml", "soap" ) );

        Set<String> describeFeatureTypeEncodings = enabledEncodings.get( DescribeFeatureType );
        assertThat( describeFeatureTypeEncodings, hasItems( "kvp", "xml" ) );

        Set<String> getFeatureEncodings = enabledEncodings.get( GetFeature );
        assertThat( getFeatureEncodings, hasItems( "kvp", "xml" ) );

        Set<String> getPropertyValueEncodings = enabledEncodings.get( GetPropertyValue );
        assertThat( getPropertyValueEncodings, hasItems( "kvp" ) );
    }

    @Test
    public void testParseEncodingsWithSupportedRequestsAndEncodings() {
        SupportedRequests supportedRequests = prepareSupportedEncodingsWithSupportedRequestsAndEncodings();

        Map<WFSRequestType, Set<String>> enabledEncodings = webFeatureService.parseEncodings( supportedRequests ).getEnabledEncodingsPerRequestType();

        Set<String> capabilitiesEncodings = enabledEncodings.get( GetCapabilities );
        assertThat( capabilitiesEncodings, hasItems( "kvp", "xml", "soap" ) );

        Set<String> describeFeatureTypeEncodings = enabledEncodings.get( DescribeFeatureType );
        assertThat( describeFeatureTypeEncodings, hasItems( "kvp", "xml", "soap" ) );

        Set<String> getFeatureEncodings = enabledEncodings.get( GetFeature );
        assertThat( getFeatureEncodings, hasItems( "kvp", "xml" ) );

        Set<String> getPropertyValueEncodings = enabledEncodings.get( GetPropertyValue );
        assertThat( getPropertyValueEncodings.size(), is( 0 ) );
    }

    @Test
    public void testParseEncodingsWithSupportedRequests() {
        SupportedRequests supportedRequests = prepareSupportedEncodingsWithSupportedRequests();

        Map<WFSRequestType, Set<String>> enabledEncodings = webFeatureService.parseEncodings( supportedRequests ).getEnabledEncodingsPerRequestType();

        Set<String> capabilitiesEncodings = enabledEncodings.get( GetCapabilities );
        assertThat( capabilitiesEncodings, hasItems( "kvp", "xml", "soap" ) );

        Set<String> getFeatureEncodings = enabledEncodings.get( GetFeature );
        assertThat( getFeatureEncodings, hasItems( "kvp", "xml", "soap" ) );

        Set<String> describeFeatureTypeEncodings = enabledEncodings.get( DescribeFeatureType );
        assertThat( describeFeatureTypeEncodings.size(), is( 0 ) );

        Set<String> getPropertyValueEncodings = enabledEncodings.get( GetPropertyValue );
        assertThat( getPropertyValueEncodings.size(), is( 0 ) );
    }

    private SupportedRequests prepareSupportedEncodings() {
        SupportedRequests supportedRequests = new SupportedRequests();

        supportedRequests.setGetCapabilities( new RequestType() );
        supportedRequests.getGetCapabilities().getSupportedEncodings().add( "xml" );
        supportedRequests.getGetCapabilities().getSupportedEncodings().add( "soap" );

        supportedRequests.setDescribeFeatureType( new RequestType() );
        supportedRequests.getDescribeFeatureType().getSupportedEncodings().add( "xml" );

        supportedRequests.setGetFeature( new RequestType() );
        supportedRequests.getGetFeature().getSupportedEncodings().add( "xml" );
        return supportedRequests;
    }

    private SupportedRequests prepareSupportedEncodingsKvpForAll() {
        SupportedRequests supportedRequests = new SupportedRequests();
        supportedRequests.getSupportedEncodings().add( "kvp" );

        supportedRequests.setGetCapabilities( new RequestType() );
        supportedRequests.getGetCapabilities().getSupportedEncodings().add( "xml" );
        supportedRequests.getGetCapabilities().getSupportedEncodings().add( "soap" );

        supportedRequests.setDescribeFeatureType( new RequestType() );
        supportedRequests.getDescribeFeatureType().getSupportedEncodings().add( "xml" );

        supportedRequests.setGetFeature( new RequestType() );
        supportedRequests.getGetFeature().getSupportedEncodings().add( "xml" );
        return supportedRequests;
    }

    private SupportedRequests prepareSupportedEncodingsWithSupportedRequestsAndEncodings() {
        SupportedRequests supportedRequests = new SupportedRequests();

        supportedRequests.setGetCapabilities( new RequestType() );
        supportedRequests.getGetCapabilities().getSupportedEncodings().add( "xml" );
        supportedRequests.getGetCapabilities().getSupportedEncodings().add( "soap" );

        supportedRequests.setDescribeFeatureType( new RequestType() );

        supportedRequests.setGetFeature( new RequestType() );
        supportedRequests.getGetFeature().getSupportedEncodings().add( "xml" );
        return supportedRequests;
    }

    private SupportedRequests prepareSupportedEncodingsWithSupportedRequests() {
        SupportedRequests supportedRequests = new SupportedRequests();
        supportedRequests.setGetCapabilities( new RequestType() );
        supportedRequests.setGetFeature( new RequestType() );
        return supportedRequests;
    }

    private Matcher<Map<WFSRequestType, Set<String>>> hasEncodingForAllWfsRequestTypes( final String encoding ) {
        return new BaseMatcher<Map<WFSRequestType, Set<String>>>() {

            @Override
            public boolean matches( Object item ) {
                @SuppressWarnings("unchecked")
                Map<WFSRequestType, Set<String>> encodings = (Map<WFSRequestType, Set<String>>) item;
                WFSRequestType[] wfsRequestTypes = WFSRequestType.values();
                if ( encodings.size() != wfsRequestTypes.length )
                    return false;
                for ( WFSRequestType wfsRequestType : wfsRequestTypes ) {
                    if ( !encodings.containsKey( wfsRequestType ) )
                        return false;
                    if ( !encodings.get( wfsRequestType ).contains( encoding ) )
                        return false;
                }
                return true;
            }

            @Override
            public void describeTo( Description description ) {
                description.appendText( "Checks if the list of enabled encodings contains one entry for each featuretype" );
            }
        };
    }

}