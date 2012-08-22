//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.portal.standard.gazetteer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.xml.XMLException;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.ogcbase.ElementStep;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathStep;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.wfs.XMLFactory;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.xml.sax.SAXException;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
abstract class AbstractGazetteerCommand {

    private static final ILogger LOG = LoggerFactory.getLogger( AbstractGazetteerCommand.class );

    protected List<GazetteerItem> items;

    protected String gazetteerAddress;

    protected QualifiedName featureType;

    protected static Map<String, WFSCapabilities> capabilitiesMap;

    protected Map<String, String> properties;

    static {
        if ( capabilitiesMap == null ) {
            capabilitiesMap = new HashMap<String, WFSCapabilities>();
        }
    }

    /**
     * @throws IOException
     * @throws HttpException
     * @throws SAXException
     * @throws XMLException
     * 
     */
    protected void loadCapabilities()
                            throws Exception {
        InputStream is = HttpUtils.performHttpGet( gazetteerAddress, "request=GetCapabilities&service=WFS", 60000,
                                                   null, null, null ).getResponseBodyAsStream();
        WFSCapabilitiesDocument doc = new WFSCapabilitiesDocument();
        doc.load( is, gazetteerAddress );
        WFSCapabilities capa = (WFSCapabilities) doc.parseCapabilities();
        capabilitiesMap.put( gazetteerAddress, capa );
    }

    protected FeatureCollection performGetFeature( WFSCapabilities capabilities, GetFeature getFeature )
                            throws Exception {

        // find a valid URL for performing GetFeature requests
        URL wfs = null;
        org.deegree.ogcwebservices.getcapabilities.Operation[] op = capabilities.getOperationsMetadata().getOperations();
        for ( org.deegree.ogcwebservices.getcapabilities.Operation operation : op ) {
            if ( "GetFeature".equalsIgnoreCase( operation.getName() ) ) {
                DCPType[] dcp = operation.getDCPs();
                for ( DCPType dcpType : dcp ) {
                    if ( dcpType.getProtocol() instanceof HTTP ) {
                        wfs = ( (HTTP) dcpType.getProtocol() ).getPostOnlineResources()[0];
                    }
                }
            }
        }

        String gf = XMLFactory.export( getFeature ).getAsString();

        LOG.logDebug( "GetFeature request: ", gf );
        LOG.logDebug( "Sending against: ", wfs );
        InputStream is = HttpUtils.performHttpPost( wfs.toURI().toASCIIString(), gf, 60000, null, null, "text/xml",
                                                    null, null ).getResponseBodyAsStream();

        GMLFeatureCollectionDocument gml = new GMLFeatureCollectionDocument();
        gml.load( is, wfs.toURI().toASCIIString() );
        FeatureCollection fc = gml.parse();

        return fc;
    }

    protected void createItemsList( FeatureCollection fc )
                            throws PropertyPathResolvingException {
        items = new ArrayList<GazetteerItem>( fc.size() );
        Iterator<Feature> iterator = fc.iterator();
        PropertyPath gi = createPropertyPath( properties.get( "GeographicIdentifier" ) );
        PropertyPath gai = null;
        if ( properties.get( "AlternativeGeographicIdentifier" ) != null ) {
            gai = createPropertyPath( properties.get( "AlternativeGeographicIdentifier" ) );
        }
        PropertyPath disp = createPropertyPath( properties.get( "DisplayName" ) );

        while ( iterator.hasNext() ) {
            Feature feature = (Feature) iterator.next();
            String gmlID = feature.getId();
            String geoId = feature.getDefaultProperty( gi ).getValue().toString();
            String displayName = (String) feature.getDefaultProperty( disp ).getValue();
            String altGeoId = null;
            if ( gai != null ) {
                FeatureProperty fp = feature.getDefaultProperty( gai );
                if ( fp != null ) {
                    altGeoId = (String) fp.getValue();
                }
            }
            items.add( new GazetteerItem( gmlID, geoId, altGeoId, displayName ) );
        }
    }

    /**
     * @param properties
     * @return
     */
    protected PropertyPath[] getResultProperties( Map<String, String> properties ) {
        List<PropertyPath> pathes = new ArrayList<PropertyPath>();

        pathes.add( createPropertyPath( properties.get( "GeographicIdentifier" ) ) );

        String tmp = properties.get( "DisplayName" );
        if ( tmp != null && !tmp.equals( properties.get( "GeographicIdentifier" ) ) ) {
            pathes.add( createPropertyPath( tmp ) );
        }

        tmp = properties.get( "AlternativeGeographicIdentifier" );
        if ( tmp != null && !tmp.equals( properties.get( "GeographicIdentifier" ) ) ) {
            pathes.add( createPropertyPath( tmp ) );
        }

        return pathes.toArray( new PropertyPath[pathes.size()] );
    }

    protected PropertyPath createPropertyPath( String name ) {
        List<String> l1 = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        boolean opened = false;
        for ( int i = 0; i < name.length(); i++ ) {
            char c = name.charAt( i );
            if ( c == '{' ) {
                opened = true;
            }
            if ( c == '/' && !opened ) {
                l1.add( sb.toString() );
                sb.delete( 0, sb.length() );
            } else {
                sb.append( c );
            }
            if ( c == '}' ) {
                opened = false;
            }
        }
        l1.add( sb.toString() );

        String[] tmp = l1.toArray( new String[l1.size()] );
        List<PropertyPathStep> steps = new ArrayList<PropertyPathStep>();
        for ( String string : tmp ) {
            QualifiedName qn = null;
            if ( name.startsWith( "{" ) ) {
                qn = new QualifiedName( string );
            } else {
                qn = new QualifiedName( string, featureType.getNamespace() );
            }
            steps.add( new ElementStep( qn ) );
        }

        return new PropertyPath( steps );
    }

}
