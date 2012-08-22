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
package org.deegree.portal.standard.wms.control;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfo;
import org.deegree.portal.Constants;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.standard.wms.model.Table;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetFeatureInfoListener extends AbstractListener {

    private static ILogger LOG = LoggerFactory.getLogger( GetFeatureInfoListener.class );

    private XSLTDocument xslt;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    @SuppressWarnings("unchecked")
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        try {
            String tmp = getInitParameter( "XSLT" );
            if ( tmp != null ) {
                URL url = new File( event.getAbsolutePath( tmp ) ).toURL();
                xslt = new XSLTDocument( url );
            }
        } catch ( Exception e ) {
            handleException( responseHandler, e );
            return;
        }

        HttpSession session = event.getSession();
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
        Map<String, Object> parameter = event.getParameter();
        LOG.logDebug( "parameter: ", parameter );
        String type = (String) parameter.get( "type" );
        Table table = null;
        if ( type.toLowerCase().indexOf( "wms" ) > -1 ) {
            try {
                String request = createGFIRequest( responseHandler, vc, parameter, type );
                table = readDataFromWFS( parameter, request, (String) parameter.get( "url" ) );
            } catch ( Exception e ) {
                handleException( responseHandler, e );
                return;
            }
        } else if ( type.toLowerCase().indexOf( "wfs" ) > -1 ) {

        } else if ( type.toLowerCase().indexOf( "wcs" ) > -1 ) {

        }

        String charEnc = getRequest().getCharacterEncoding();
        if ( charEnc == null ) {
            charEnc = Charset.defaultCharset().displayName();
        }
        responseHandler.setContentType( "application/json; charset=" + charEnc );
        responseHandler.writeAndClose( false, table );

    }

    private String createGFIRequest( ResponseHandler responseHandler, ViewContext vc, Map<String, Object> parameter,
                                     String type )
                            throws IOException, OGCWebServiceException {
        int width = ( (Number) parameter.get( "mapWidth" ) ).intValue();
        int height = ( (Number) parameter.get( "mapHeight" ) ).intValue();
        int x = ( (Number) parameter.get( "x" ) ).intValue();
        int y = ( (Number) parameter.get( "y" ) ).intValue();
        String bbox = (String) parameter.get( "bbox" );

        Map<String, String> getFIParam = new HashMap<String, String>();
        getFIParam.put( "ID", "TMP" );
        getFIParam.put( "LAYERS", (String) parameter.get( "name" ) );
        getFIParam.put( "STYLES", "" );
        getFIParam.put( "BBOX", bbox );
        if ( "OGC:WMS 1.3.0".equalsIgnoreCase( type ) ) {
            getFIParam.put( "VERSION", "1.3.0" );
            getFIParam.put( "CRS", vc.getGeneral().getBoundingBox()[0].getCoordinateSystem().getPrefixedName() );
            getFIParam.put( "I", Integer.toString( x ) );
            getFIParam.put( "J", Integer.toString( y ) );
        } else {
            getFIParam.put( "VERSION", type.split( " " )[1] );
            getFIParam.put( "SRS", vc.getGeneral().getBoundingBox()[0].getCoordinateSystem().getPrefixedName() );
            getFIParam.put( "X", Integer.toString( x ) );
            getFIParam.put( "Y", Integer.toString( y ) );
        }
        getFIParam.put( "WIDTH", Integer.toString( width ) );
        getFIParam.put( "HEIGHT", Integer.toString( height ) );
        getFIParam.put( "FORMAT", "image/jpeg" );

        getFIParam.put( "QUERY_LAYERS", (String) parameter.get( "name" ) );
        getFIParam.put( "FEATURE_COUNT", "999" );
        if ( parameter.get( "time" ) != null ) {
            getFIParam.put( "TIME", (String) parameter.get( "time" ) );
        }
        String request = null;
        GetFeatureInfo gfi = GetFeatureInfo.create( getFIParam );
        request = gfi.getRequestParameter();

        return request;
    }

    private Table readDataFromWFS( Map<String, Object> parameter, String request, String url )
                            throws HttpException, IOException, SAXException, XMLParsingException, TransformerException {

        XMLFragment xml = new XMLFragment();
        HttpMethod method = HttpUtils.performHttpGet( url, request, timeout, null, null, null );
        xml.load( method.getResponseBodyAsStream(), url );

        if ( xslt != null ) {
            // if a XSLT for transforming get feature info results has been defined:
            // perform a transformation to ensure that feature info result is a valid
            // GML document
            xml = xslt.transform( xml );
        }

        if ( LOG.isDebug() ) {
            LOG.logDebug( "feature info performed : ", url + " " + request );
            LOG.logDebug( "feature info result : ", xml.getAsPrettyString() );
        }

        NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
        List<Element> featureMember = XMLTools.getElements( xml.getRootElement(), "//gml:featureMember", nsc );
        if ( featureMember.size() == 0 ) {
            featureMember = XMLTools.getElements( xml.getRootElement(), "//gml3_2:featureMember", nsc );
        }

        // get column header
        List<String> header = new ArrayList<String>();
        for ( int i = 0; i < featureMember.size(); i++ ) {
            // get feature; there should be exactly one
            ElementList el = XMLTools.getChildElements( featureMember.get( i ) );
            if ( el.getLength() > 0 && el.item( 0 ) != null ) {
                // get properties
                ElementList el2 = XMLTools.getChildElements( el.item( 0 ) );
                for ( int j = 0; j < el2.getLength(); j++ ) {
                    String name = el2.item( j ).getLocalName();
                    if ( !header.contains( name ) && !"boundedBy".equalsIgnoreCase( name ) ) {
                        header.add( name );
                    }
                }
            }
        }

        // remember index positions (column number) for each property (name)
        Map<String, Integer> map = new HashMap<String, Integer>();
        for ( int i = 0; i < header.size(); i++ ) {
            map.put( header.get( i ), new Integer( i ) );
        }

        // get column data
        String[][] data = new String[featureMember.size()][];
        for ( int i = 0; i < featureMember.size(); i++ ) {
            data[i] = new String[header.size()];
            // get feature; there should be exactly one
            ElementList el = XMLTools.getChildElements( featureMember.get( i ) );
            if ( el.getLength() > 0 && el.item( 0 ) != null ) {
                // get properties
                ElementList el2 = XMLTools.getChildElements( el.item( 0 ) );
                for ( int j = 0; j < el2.getLength(); j++ ) {
                    String name = el2.item( j ).getLocalName();
                    if ( !"boundedBy".equalsIgnoreCase( name ) ) {
                        int idx = map.get( name );
                        data[i][idx] = XMLTools.getStringValue( el2.item( j ) );
                    }
                }
            }
        }
        Table table = new Table();
        table.setName( (String) parameter.get( "title" ) );
        table.setColumns( header.toArray( new String[header.size()] ) );
        table.setData( data );

        return table;
    }

}
