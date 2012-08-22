//$HeadURL$
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
package org.deegree.ogcwebservices.wms.operation;

import java.awt.Point;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.ColorUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.graphics.sld.StyledLayerDescriptor;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wms.InvalidPointException;

/**
 * @author Katharina Lupp <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @version $Revision$ $Date$
 */
public class GetFeatureInfo extends WMSRequestBase {

    private static final long serialVersionUID = 1197866346790857492L;

    private static final ILogger LOGGER = LoggerFactory.getLogger( GetFeatureInfo.class );

    private List<String> queryLayers = null;

    private Point clickPoint = null;

    private String exceptions = null;

    private String infoFormat = null;

    private StyledLayerDescriptor sld = null;

    private GetMap getMapRequestCopy = null;

    private int featureCount = 1;

    private boolean infoFormatIsDefault = false;

    /**
     * creates a <tt>WMSFeatureInfoRequest</tt> from the request parameters.
     * 
     * @return an instance of <tt>WMSFeatureInfoRequest</tt>
     * @param version
     *            VERSION=version (R): Request version.
     * @param id
     *            the request id
     * @param queryLayers
     *            QUERY_LAYERS=layer_list (R): Comma-separated list of one or more layers to be queried.
     * @param getMapRequestCopy
     *            &lt;map_request_copy&gt; (R): Partial copy of the Map request parameters that generated the map for
     *            which information is desired.
     * @param infoFormat
     *            INFO_FORMAT=output_format (O): Return format of feature information (MIME type).
     * @param featureCount
     *            FEATURE_COUNT=number (O): Number of features about which to return information (default=1).
     * @param clickPoint
     *            X=pixel_column (R): X coordinate in pixels of feature (measured from upper left corner=0) Y=pixel_row
     *            (R): Y coordinate in pixels of feature (measured from upper left corner=0)
     * @param exceptions
     *            EXCEPTIONS=exception_format (O): The format in which exceptions are to be reported by the WMS
     *            (default=application/vnd.ogc.se_xml).
     * @param sld
     *            StyledLayerDescriptor
     * @param vendorSpecificParameter
     *            Vendor-specific parameters (O): Optional experimental parameters.
     */
    public static GetFeatureInfo create( String version, String id, String[] queryLayers, GetMap getMapRequestCopy,
                                         String infoFormat, int featureCount, java.awt.Point clickPoint,
                                         String exceptions, StyledLayerDescriptor sld,
                                         Map<String, String> vendorSpecificParameter ) {

        return new GetFeatureInfo( version, id, queryLayers, getMapRequestCopy, infoFormat, featureCount, clickPoint,
                                   exceptions, sld, vendorSpecificParameter );

    }

    /**
     * creates a <tt>WMSFeatureInfoRequest</tt> from a <tt>HashMap</tt> that contains the request parameters as
     * key-value-pairs. Keys are expected to be in upper case notation.
     * 
     * @param model
     *            <tt>HashMap</tt> containing the request parameters
     * @return an instance of <tt>WMSFeatureInfoRequest</tt>
     * @throws OGCWebServiceException
     */
    public static GetFeatureInfo create( Map<String, String> model )
                            throws OGCWebServiceException {

        // VERSION
        String version = model.get( "VERSION" );
        if ( version == null ) {
            version = model.get( "WMTVER" );
        }
        if ( version == null ) {
            throw new InconsistentRequestException( "VERSION-value must be set in the GetFeatureInfo request" );
        }

        boolean is130 = ( "1.3.0".compareTo( version ) <= 0 );

        // ID
        String id = model.get( "ID" );
        if ( id == null ) {
            throw new InconsistentRequestException( "ID-value must be set in the GetFeatureInfo request" );
        }

        // QUERY_LAYERS
        String layerlist = model.remove( "QUERY_LAYERS" );
        String[] queryLayers = null;

        if ( layerlist != null ) {
            StringTokenizer st = new StringTokenizer( layerlist, "," );
            queryLayers = new String[st.countTokens()];
            int i = 0;
            while ( st.hasMoreTokens() ) {
                queryLayers[i++] = st.nextToken();
            }
        } else {
            throw new InconsistentRequestException( "QUERY_LAYERS-value must be set in the GetFeatureInfo request" );
        }

        // INFO_FORMAT (mime-type)
        String infoFormat = model.remove( "INFO_FORMAT" );
        boolean infoFormatDefault = false;
        if ( infoFormat == null ) {
            infoFormat = "application/vnd.ogc.gml";
            infoFormatDefault = true;
        }

        // FEATURE_COUNT (default=1)
        String feco = model.remove( "FEATURE_COUNT" );
        int featureCount = 1;
        if ( feco != null ) {
            featureCount = Integer.parseInt( feco.trim() );
        }
        if ( featureCount < 0 ) {
            featureCount = 1;
        }

        // X, Y (measured from upper left corner=0)
        String X;
        String Y;

        if ( is130 ) {
            X = "I";
            Y = "J";
        } else {
            X = "X";
            Y = "Y";
        }

        String xstring = model.remove( X );
        String ystring = model.remove( Y );

        java.awt.Point clickPoint = null;
        if ( ( xstring != null ) && ( ystring != null ) ) {
            try {
                int x = Integer.parseInt( xstring.trim() );
                int y = Integer.parseInt( ystring.trim() );
                clickPoint = new java.awt.Point( x, y );
            } catch ( NumberFormatException nfe ) {
                LOGGER.logError( nfe.getLocalizedMessage(), nfe );
                throw new OGCWebServiceException( "GetFeatureInfo", "Invalid point parameter",
                                                  ExceptionCode.INVALID_POINT );
            }
        } else {
            throw new InconsistentRequestException( X + "- and/or " + Y
                                                    + "-value must be set in the GetFeatureInfo request" );
        }

        // EXCEPTIONS (default=application/vnd.ogc.se_xml)
        String exceptions = model.get( "EXCEPTIONS" );
        if ( exceptions == null ) {
            if ( is130 ) {
                exceptions = "XML";
            } else {
                exceptions = "application/vnd.ogc.se_xml";
            }
        }

        // <map_request_copy>
        GetMap getMapRequestCopy = null;

        try {
            getMapRequestCopy = GetMap.create( model );
        } catch ( Exception ex ) {
            throw new InconsistentRequestException(
                                                    "\nAn Exception "
                                                                            + "occured in creating the GetMap request-copy included in the "
                                                                            + "GetFeatureInfo-Operations:\n"
                                                                            + "--> Location: WMSProtocolFactory, createGetFeatureInfoRequest(int, HashMap)\n"
                                                                            + ex.getMessage() );

        }

        // check for consistency
        if ( clickPoint.x > getMapRequestCopy.getWidth() || clickPoint.y > getMapRequestCopy.getHeight() ) {
            throw new InvalidPointException( "The requested point is not valid." );
        }

        // VendorSpecificParameter; because all defined parameters has been
        // removed
        // from the model the vendorSpecificParameters are what left
        Map<String, String> vendorSpecificParameter = model;

        // StyledLayerDescriptor
        StyledLayerDescriptor sld = getMapRequestCopy.getStyledLayerDescriptor();

        GetFeatureInfo res = create( version, id, queryLayers, getMapRequestCopy, infoFormat, featureCount, clickPoint,
                                     exceptions, sld, vendorSpecificParameter );
        res.infoFormatIsDefault = infoFormatDefault;

        return res;
    }

    /**
     * Creates a new WMSFeatureInfoRequest_Impl object.
     * 
     * @param version
     * @param id
     * @param queryLayers
     * @param getMapRequestCopy
     * @param infoFormat
     * @param featureCount
     * @param clickPoint
     * @param exceptions
     * @param sld
     * @param vendorSpecificParameter
     */
    private GetFeatureInfo( String version, String id, String[] queryLayers, GetMap getMapRequestCopy,
                            String infoFormat, int featureCount, Point clickPoint, String exceptions,
                            StyledLayerDescriptor sld, Map<String, String> vendorSpecificParameter ) {
        super( version, id, vendorSpecificParameter );
        this.queryLayers = new ArrayList<String>();
        setQueryLayers( queryLayers );
        setGetMapRequestCopy( getMapRequestCopy );
        setGetMapRequestCopy( getMapRequestCopy );
        setFeatureCount( featureCount );
        setClickPoint( clickPoint );
        setExceptions( exceptions );
        setStyledLayerDescriptor( sld );
        setInfoFormat( infoFormat );
    }

    /**
     * <map request copy> is not a name/value pair like the other parameters. Instead, most of the GetMap request
     * parameters that generated the original map are repeated. Two are omitted because GetFeatureInfo provides its own
     * values: VERSION and REQUEST. The remainder of the GetMap request shall be embedded contiguously in the
     * GetFeatureInfo request.
     * 
     * @return a copy of the original request
     */
    public GetMap getGetMapRequestCopy() {
        return getMapRequestCopy;
    }

    /**
     * sets the <GetMapRequestCopy>
     * 
     * @param getMapRequestCopy
     */
    public void setGetMapRequestCopy( GetMap getMapRequestCopy ) {
        this.getMapRequestCopy = getMapRequestCopy;
    }

    /**
     * The required QUERY_LAYERS parameter states the map layer(s) from which feature information is desired to be
     * retrieved. Its value is a comma- separated list of one or more map layers that are returned as an array. This
     * parameter shall contain at least one layer name, but may contain fewer layers than the original GetMap request.
     * <p>
     * </p>
     * If any layer in this list is not contained in the Capabilities XML of the WMS, the results are undefined and the
     * WMS shall produce an exception response.
     * 
     * @return the layer names
     */
    public String[] getQueryLayers() {
        return queryLayers.toArray( new String[queryLayers.size()] );
    }

    /**
     * adds the <QueryLayers>
     * 
     * @param queryLayers
     */
    public void addQueryLayers( String queryLayers ) {
        this.queryLayers.add( queryLayers );
    }

    /**
     * sets the <QueryLayers>
     * 
     * @param queryLayers
     */
    public void setQueryLayers( String[] queryLayers ) {
        this.queryLayers.clear();

        if ( queryLayers != null ) {
            for ( int i = 0; i < queryLayers.length; i++ ) {
                this.queryLayers.add( queryLayers[i] );
            }
        }
    }

    /**
     * The optional INFO_FORMAT indicates what format to use when returning the feature information. Supported values
     * for a GetFeatureInfo request on a WMS instance are listed as MIME types in one or more <Format>elements inside
     * the <Request><FeatureInfo>element of its Capabilities XML. The entire MIME type string in <Format>is used as the
     * value of the INFO_FORMAT parameter. In an HTTP environment, the MIME type shall be set on the returned object
     * using the Content-type entity header.
     * <p>
     * </p>
     * <b>EXAMPLE: </b> <tt> The parameter INFO_FORMAT=application/vnd.ogc.gml
     * requests that the feature information be formatted in Geography Markup
     * Language (GML).</tt>
     * 
     * @return the format
     */
    public String getInfoFormat() {
        return infoFormat;
    }

    /**
     * sets the <InfoFormat>
     * 
     * @param infoFormat
     */
    public void setInfoFormat( String infoFormat ) {
        this.infoFormat = infoFormat;
    }

    /**
     * The optional FEATURE_COUNT parameter states the maximum number of features for which feature information should
     * be returned. Its value is a positive integer greater than zero. The default value is 1 if this parameter is
     * omitted.
     * 
     * @return the count
     */
    public int getFeatureCount() {
        return featureCount;
    }

    /**
     * sets the <FeatureCount>
     * 
     * @param featureCount
     */
    public void setFeatureCount( int featureCount ) {
        this.featureCount = featureCount;
    }

    /**
     * The required X and Y parameters indicate a point of interest on the map. X and Y identify a single point within
     * the borders of the WIDTH and HEIGHT parameters of the embedded GetMap request. The origin is set to (0,0)
     * centered in the pixel at the upper left corner; X increases to the right and Y increases downward. X and Y are
     * retruned as java.awt.Point class/datastructure.
     * 
     * @return the point of interest
     */
    public Point getClickPoint() {
        return clickPoint;
    }

    /**
     * sets the <ClickPoint>
     * 
     * @param clickPoint
     */
    public void setClickPoint( Point clickPoint ) {
        this.clickPoint = clickPoint;
    }

    /**
     * The optional EXCEPTIONS parameter states the manner in which errors are to be reported to the client. The default
     * value is application/vnd.ogc.se_xml if this parameter is absent from the request. At present, not other values
     * are defined for the WMS GetFeatureInfo request.
     * 
     * @return the exception format
     */
    public String getExceptions() {
        return exceptions;
    }

    /**
     * sets the <Exception>
     * 
     * @param exceptions
     */
    public void setExceptions( String exceptions ) {
        this.exceptions = exceptions;
    }

    /**
     * returns the SLD the request is made of. This implies that a 'simple' HTTP GET-Request will be transformed into a
     * valid SLD. This is mandatory within a JaGo WMS.
     * <p>
     * </p>
     * This mean even if a GetMap request is send using the HTTP GET method, an implementing class has to map the
     * request to a SLD data sructure.
     * 
     * @return the sld
     */
    public StyledLayerDescriptor getStyledLayerDescriptor() {
        return sld;
    }

    /**
     * sets the SLD the request is made of. This implies that a 'simple' HTTP GET-Request or a part of it will be
     * transformed into a valid SLD. For convenience it is asumed that the SLD names just a single layer to generate
     * display elements of.
     * 
     * @param sld
     */
    public void setStyledLayerDescriptor( StyledLayerDescriptor sld ) {
        this.sld = sld;
    }

    @Override
    public String toString() {
        try {
            return getRequestParameter();
        } catch ( OGCWebServiceException e ) {
            e.printStackTrace();
        }
        return super.toString();
    }

    /**
     * returns the parameter of a HTTP GET request.
     * 
     */
    @Override
    public String getRequestParameter()
                            throws OGCWebServiceException {
        // indicates if the request parameters are decoded as SLD. deegree won't
        // perform SLD requests through HTTP GET
        if ( ( getMapRequestCopy.getBoundingBox() == null ) || ( queryLayers.size() == 0 ) ) {
            throw new OGCWebServiceException( "Operations can't be expressed as HTTP GET request " );
        }

        StringBuffer sb = new StringBuffer( "service=WMS" );

        if ( getVersion().compareTo( "1.0.0" ) <= 0 ) {
            sb.append( "&VERSION=" + getVersion() + "&REQUEST=feature_info" );
            sb.append( "&TRANSPARENT=" + getMapRequestCopy.getTransparency() );
        } else {
            sb.append( "&VERSION=" + getVersion() + "&REQUEST=GetFeatureInfo" );
            sb.append( "&TRANSPARENCY=" + getMapRequestCopy.getTransparency() );
        }

        sb.append( "&WIDTH=" + getMapRequestCopy.getWidth() );
        sb.append( "&HEIGHT=" + getMapRequestCopy.getHeight() );
        sb.append( "&FORMAT=" + getMapRequestCopy.getFormat() );
        sb.append( "&EXCEPTIONS=" + getExceptions() );
        sb.append( "&BGCOLOR=" );
        sb.append( ColorUtils.toHexCode( "0x", getMapRequestCopy.getBGColor() ) );
        if ( "1.3.0".compareTo( getVersion() ) <= 0 ) {
            sb.append( "&CRS=" + getMapRequestCopy.getSrs() );
            sb.append( "&BBOX=" ).append( getMapRequestCopy.getBoundingBox().getMin().getY() );
            sb.append( ',' ).append( getMapRequestCopy.getBoundingBox().getMin().getX() );
            sb.append( ',' ).append( getMapRequestCopy.getBoundingBox().getMax().getY() );
            sb.append( ',' ).append( getMapRequestCopy.getBoundingBox().getMax().getX() );
        } else {
            sb.append( "&SRS=" + getMapRequestCopy.getSrs() );
            sb.append( "&BBOX=" ).append( getMapRequestCopy.getBoundingBox().getMin().getX() );
            sb.append( ',' ).append( getMapRequestCopy.getBoundingBox().getMin().getY() );
            sb.append( ',' ).append( getMapRequestCopy.getBoundingBox().getMax().getX() );
            sb.append( ',' ).append( getMapRequestCopy.getBoundingBox().getMax().getY() );
        }

        GetMap.Layer[] layers = getMapRequestCopy.getLayers();
        String l = "";
        String s = "";

        for ( int i = 0; i < layers.length; i++ ) {
            l += ( layers[i].getName() + "," );
            s += ( layers[i].getStyleName() + "," );
        }

        l = l.substring( 0, l.length() - 1 );
        s = s.substring( 0, s.length() - 1 );
        sb.append( "&LAYERS=" + l );

        // replace $DEFAULT with "", which is what WMSses expect
        s = StringTools.replace( s, "$DEFAULT", "", true );

        sb.append( "&STYLES=" + s );

        // TODO
        // append time, elevation and sample dimension

        String[] qlayers = getQueryLayers();
        String ql = "";

        for ( int i = 0; i < qlayers.length; i++ ) {
            ql += ( qlayers[i] + "," );
        }

        ql = ql.substring( 0, ql.length() - 1 );
        sb.append( "&QUERY_LAYERS=" + ql );
        sb.append( "&FEATURE_COUNT=" + getFeatureCount() );
        sb.append( "&INFO_FORMAT=" + getInfoFormat() );
        if ( "1.3.0".compareTo( getVersion() ) <= 0 ) {
            sb.append( "&I=" + clickPoint.x );
            sb.append( "&J=" + clickPoint.y );
        } else {
            sb.append( "&X=" + clickPoint.x );
            sb.append( "&Y=" + clickPoint.y );
        }
        
        DimensionValues values = getMapRequestCopy.getDimTime();
        if ( values != null ) {
            sb.append( "&time=" + values.getOriginalValue() );
        }
        
        if ( getVendorSpecificParameters() != null ) {
            Iterator<String> iterator = getVendorSpecificParameters().keySet().iterator();
            while ( iterator.hasNext() ) {
                String key = iterator.next();
                String value = getVendorSpecificParameters().get( key );
                try {
                    value = URLEncoder.encode( value, CharsetUtils.getSystemCharset() );
                } catch ( UnsupportedEncodingException e ) {
                    // system encoding should be supported...
                }
                sb.append( '&' ).append( key ).append( '=' ).append( value );
            }
        }


        return sb.toString();
    }

    /**
     * @return whether the info format is the default setting
     */
    public boolean isInfoFormatDefault() {
        return infoFormatIsDefault;
    }

}
