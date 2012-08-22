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

import static org.deegree.crs.components.Unit.DEGREE;

import java.awt.Color;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.deegree.datatypes.values.Values;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.ColorUtils;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.util.MimeTypeMapper;
import org.deegree.framework.util.NetWorker;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.Marshallable;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.graphics.sld.SLDFactory;
import org.deegree.graphics.sld.StyledLayerDescriptor;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.InvalidGMLException;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wmps.operation.PrintMap;
import org.deegree.ogcwebservices.wms.InvalidCRSException;
import org.deegree.ogcwebservices.wms.InvalidFormatException;
import org.deegree.ogcwebservices.wms.InvalidSRSException;
import org.deegree.ogcwebservices.wms.configuration.AbstractDataSource;
import org.deegree.ogcwebservices.wms.configuration.RemoteWMSDataSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This interface describes the access to the parameters of a GeMap request. It is excpected that there are two kinds of
 * request. The first is the 'normal' HTTP GET request with name-value-pair enconding and the second is a HTTP POST
 * request containing a SLD.
 * <p>
 * </p>
 * Even it is possible to access the values of a HTTP GET request throught their bean accessor methods the request shall
 * be mapped to a SLD data structure that is accessible using the <tt>getSLD()</tt>.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author:wanhoff$
 * 
 * @version $Revision$, $Date:20.03.2007$
 */
public class GetMap extends WMSRequestBase {

    private static final long serialVersionUID = 887256882709344021L;

    private static final ILogger LOG = LoggerFactory.getLogger( GetMap.class );

    private Values elevation = null;

    private Values time = null;

    private Map<String, Values> sampleDimension = null;

    private List<Layer> layers = null;

    private Color bGColor = null;

    private Envelope boundingBox = null;

    private String exceptions = null;

    private String format = null;

    private String srs = null;

    private StyledLayerDescriptor sld = null;

    private URL sLD_URL = null;

    private URL wFS_URL = null;

    private boolean transparency = false;

    private int height = 0;

    private int width = 0;

    private DimensionValues dimTime;

    private DimensionValues dimElev;

    /**
     * A list to transport messages to the WMSHandler that need to be set in HTTP headers.
     */
    public LinkedList<String> warningHeaders = new LinkedList<String>();

    /**
     * creates a <tt>WTSGetViewRequest</tt> from a set of parameters and builds up the complete SLD.
     * 
     * @return an instance of <tt>GetMapRequest</tt>
     * @param version
     *            Request version.
     * @param layers
     *            list of one or more map layers. Optional if SLD parameter is present. Contains list of one rendering
     *            style per requested layer. Optional if SLD parameter is present.
     * @param elevation
     *            Elevation of layer desired.
     * @param sampleDimension
     *            Value of other dimensions as appropriate.
     * @param format
     *            Output format of map.
     * @param width
     *            Width in pixels of map picture.
     * @param height
     *            Height in pixels of map picture.
     * @param srs
     *            the requested Spatial Reference System.
     * @param boundingBox
     *            Bounding box corners (lower left, upper right) in SRS units.
     * @param transparency
     *            Background transparency of map.
     * @param bGColor
     *            Hexadecimal red-green-blue color value for the background color.
     * @param exceptions
     *            The format in which exceptions are to be reported by the WMS.
     * @param time
     *            Time value of layer desired
     * @param sld
     *            Styled Layer Descriptor
     * @param id
     *            an unique ID of the request
     * @param sldURL
     * @param vendorSpecificParameter
     *            Vendor Specific Parameter
     */
    public static GetMap create( String version, String id, Layer[] layers, Values elevation,
                                 Map<String, Values> sampleDimension, String format, int width, int height, String srs,
                                 Envelope boundingBox, boolean transparency, Color bGColor, String exceptions,
                                 Values time, URL sldURL, StyledLayerDescriptor sld,
                                 Map<String, String> vendorSpecificParameter ) {
        return new GetMap( version, id, layers, elevation, sampleDimension, format, width, height, srs, boundingBox,
                           transparency, bGColor, exceptions, time, sldURL, sld, vendorSpecificParameter );
    }

    /**
     * creates a getMap request for requesting a cascaded remote WMS considering the getMap request and the
     * filterconditions defined in the submitted <tt>DataSource</tt> object The request will be encapsualted within a
     * <tt>OGCWebServiceEvent</tt>.
     * 
     * @param ds
     * @param request
     * @param style
     * @param layer
     * @return GetMap request object
     */
    public static GetMap createGetMapRequest( AbstractDataSource ds, GetMap request, String style, String layer ) {

        GetMap gmr = ( (RemoteWMSDataSource) ds ).getGetMapRequest();

        String format = request.getFormat();

        if ( gmr != null && !"%default%".equals( gmr.getFormat() ) ) {
            format = gmr.getFormat();
        }

        GetMap.Layer[] lys = null;
        lys = new GetMap.Layer[1];

        if ( style != null ) {
            lys[0] = PrintMap.createLayer( layer, style );
        } else {
            lys[0] = PrintMap.createLayer( layer, "$DEFAULT" );
        }
        if ( gmr != null && gmr.getLayers() != null && !( gmr.getLayers()[0].getName().equals( "%default%" ) ) ) {
            lys = gmr.getLayers();
        }
        Color bgColor = request.getBGColor();
        if ( gmr != null && gmr.getBGColor() != null ) {
            bgColor = gmr.getBGColor();
        }
        Values time = request.getTime();
        if ( gmr != null && gmr.getTime() != null ) {
            time = gmr.getTime();
        }

        Map<String, String> vendorSpecificParameter = request.getVendorSpecificParameters();
        if ( gmr != null && gmr.getVendorSpecificParameters() != null && gmr.getVendorSpecificParameters().size() > 0 ) {
            vendorSpecificParameter.putAll( gmr.getVendorSpecificParameters() );
        }
        String version = "1.1.0";
        if ( gmr != null && gmr.getVersion() != null ) {
            version = gmr.getVersion();
        }

        Values elevation = request.getElevation();
        if ( gmr != null && gmr.getElevation() != null ) {
            elevation = gmr.getElevation();
        }
        Map<String, Values> sampleDim = null;
        if ( gmr != null && gmr.getSampleDimension() != null ) {
            sampleDim = gmr.getSampleDimension();
        }

        boolean tranparency = false;
        if ( gmr != null ) {
            tranparency = gmr.getTransparency();
        }

        // now filter out the unwanted vendor specific parameters and put in
        // the wanted additional ones
        Map<String, String> vsp = new HashMap<String, String>( 10 );
        vsp.putAll( ( (RemoteWMSDataSource) ds ).getAddedParameters() );
        for ( String name : ( (RemoteWMSDataSource) ds ).getPassedParameters() ) {
            if ( vendorSpecificParameter.containsKey( name ) ) {
                vsp.put( name, vendorSpecificParameter.get( name ).toString() );
            }
        }

        IDGenerator idg = IDGenerator.getInstance();
        gmr = GetMap.create( version, "" + idg.generateUniqueID(), lys, elevation, sampleDim, format,
                             request.getWidth(), request.getHeight(), request.getSrs(), request.getBoundingBox(),
                             tranparency, bgColor, request.getExceptions(), time, null, null, vsp );

        return gmr;
    }

    /**
     * creates a <tt>GetMapRequest</tt> from a <tt>HashMap</tt> that contains the request parameters as key-value-pairs.
     * Keys are expected to be in upper case notation.
     * 
     * @param model
     *            <tt>HashMap</tt> containing the request parameters
     * @return an instance of <tt>GetMapRequest</tt>
     * @throws InconsistentRequestException
     * @throws XMLParsingException
     * @throws MalformedURLException
     */
    public static GetMap create( Map<String, String> model )
                            throws InconsistentRequestException, XMLParsingException, MalformedURLException {

        LOG.logDebug( "Request parameters: " + model );

        // use model.remove(..) so at the end of the method the vendor
        // specific parameters remains in the model HashMap
        model.remove( "REQUEST" );

        // Version
        String version = model.remove( "VERSION" );

        if ( version == null ) {
            version = model.remove( "WMTVER" );
        }

        if ( version == null ) {
            throw new InconsistentRequestException( "VERSION-value must be set" );
        }

        // LAYERS & STYLES & SLD (URL, XML)
        StyledLayerDescriptor sld = null;
        String sld_body = model.remove( "SLD_BODY" );
        String sld_urlstring = model.remove( "SLD" );

        // The SLD is complete in the Maprequest
        URL sLD_URL = null;

        if ( sld_body != null ) {
            try {
                sld_body = URLDecoder.decode( sld_body, CharsetUtils.getSystemCharset() );
                sld = SLDFactory.createSLD( sld_body );
            } catch ( Exception ee ) {
                throw new XMLParsingException( "Could not decode SLD_BODY: " + ee.toString() );
            }
        } else if ( sld_urlstring != null ) {
            // The SLD is as url in the Maprequest
            sLD_URL = new URL( sld_urlstring );

            try {
                sld = SLDFactory.createSLD( sLD_URL );
            } catch ( Exception ioex ) {
                ioex.printStackTrace();
                LOG.logError( ioex.getMessage(), ioex );
                throw new InconsistentRequestException( "IOException occured during the access "
                                                        + "to the SLD-URL. Wrong URL? Server down?" + ioex.getMessage() );
            }
        }

        // LAYERS & STYLES
        String layersstring = model.remove( "LAYERS" );
        if ( ( layersstring == null || layersstring.trim().length() == 0 ) && ( sld == null ) ) {
            throw new InconsistentRequestException( "At least one layer must be defined within a GetMap request" );
        }
        String stylesstring = model.remove( "STYLES" );

        // normalize styles parameter
        if ( stylesstring == null ) {
            stylesstring = "";
        }
        if ( stylesstring.startsWith( "," ) ) {
            stylesstring = "$DEFAULT" + stylesstring;
        }

        stylesstring = StringTools.replace( stylesstring, ",,", ",$DEFAULT,", true );

        if ( stylesstring.endsWith( "," ) ) {
            stylesstring = stylesstring + "$DEFAULT";
        }

        List<String> layers = StringTools.toList( layersstring, ",", false );

        List<String> styles = null;
        if ( stylesstring.length() == 0 ) {
            styles = new ArrayList<String>( layers.size() );
            for ( int i = 0; i < layers.size(); i++ ) {
                styles.add( "$DEFAULT" );
            }
        } else {
            styles = StringTools.toList( stylesstring, ",", false );
        }

        // At last, build up the Layer object
        GetMap.Layer[] ls = new GetMap.Layer[layers.size()];

        if ( styles.size() != layers.size() ) {
            throw new InconsistentRequestException( Messages.getMessage( "WMS_WRONG_STYLES" ) );
        }

        for ( int i = 0; i < layers.size(); i++ ) {
            try {
                String l = URLDecoder.decode( layers.get( i ), CharsetUtils.getSystemCharset() );
                ls[i] = GetMap.createLayer( l, styles.get( i ) );
            } catch ( UnsupportedEncodingException e2 ) {
                e2.printStackTrace();
            }
        }

        // FORMAT
        String format = model.remove( "FORMAT" );
        if ( format == null ) {
            throw new InconsistentRequestException( "FORMAT-value must be set" );
        }
        try {
            format = URLDecoder.decode( format, CharsetUtils.getSystemCharset() );
        } catch ( UnsupportedEncodingException e1 ) {
            e1.printStackTrace();
        }
        if ( !MimeTypeMapper.isKnownImageType( format ) ) {
            throw new InvalidFormatException( format + " is not a valid image/result format" );
        }

        // width
        String tmp = model.remove( "WIDTH" );
        if ( tmp == null ) {
            throw new InconsistentRequestException( "WIDTH must be set" );
        }
        int width = 0;
        try {
            width = Integer.parseInt( tmp );
        } catch ( Exception e ) {
            throw new InconsistentRequestException( "WIDTH must be a valid integer number" );
        }

        // height
        tmp = model.remove( "HEIGHT" );
        if ( tmp == null ) {
            throw new InconsistentRequestException( "HEIGHT must be set" );
        }
        int height = 0;
        try {
            height = Integer.parseInt( tmp );
        } catch ( Exception e ) {
            throw new InconsistentRequestException( "HEIGHT must be a valid integer number" );
        }

        double minx, miny, maxx, maxy;
        Envelope boundingBox = null;
        String srs;
        boolean isLongLat = false;

        boolean is130 = false;

        if ( "1.3.0".compareTo( version ) <= 0 ) {
            is130 = true;

            // SRS or rather CRS
            srs = model.remove( "CRS" );
            if ( srs == null ) {
                throw new InvalidCRSException( "CRS-value must be set" );
            }

            // check for geographic coordinate system
            try {
                CoordinateSystem crs = CRSFactory.create( srs );
                isLongLat = crs.getAxisUnits()[0].equals( DEGREE ) && srs.toLowerCase().startsWith( "epsg" );
            } catch ( UnknownCRSException e ) {
                LOG.logDebug( e.getLocalizedMessage(), e );
                throw new InvalidCRSException( srs );
            }

        } else {
            // SRS
            srs = model.remove( "SRS" );
            if ( srs == null ) {
                throw new InvalidSRSException( "SRS-value must be set" );
            }

            try {
                // check for crs validity - yes, this method is bad. Is there a better one?
                CRSFactory.create( srs );
            } catch ( UnknownCRSException e ) {
                LOG.logDebug( e.getLocalizedMessage(), e );
                if ( is130 ) {
                    throw new InvalidCRSException( Messages.getMessage( "WMS_UNKNOWN_CRS", srs ) );
                }
                throw new InvalidSRSException( Messages.getMessage( "WMS_UNKNOWN_CRS", srs ) );
            }

        }

        // BBOX
        String boxstring = model.remove( "BBOX" );
        if ( boxstring == null ) {
            throw new InconsistentRequestException( "BBOX-value must be set" );
        }

        // ESRI ArcGIS Explorer hack which sends bboxes like 45,1234,7,234,46,123,8,345
        int count = StringTools.countString( boxstring, "," );
        if ( count == 7 ) {
            String str = boxstring;
            boxstring = "";
            for ( int i = 0; i < 4; ++i ) {
                str = str.replaceFirst( ",", "." );
                if ( str.indexOf( "," ) == -1 ) {
                    boxstring += str;
                } else {
                    boxstring += str.substring( 0, str.indexOf( "," ) + 1 );
                    str = str.substring( str.indexOf( "," ) + 1 );
                }
            }
            LOG.logDebug( "Changed bbox string from ArcGIS Explorer to " + boxstring );
        }

        StringTokenizer st = new StringTokenizer( boxstring, "," );

        if ( isLongLat ) {
            // parse first y, then x
            String s = st.nextToken().replace( ' ', '+' );
            miny = Double.parseDouble( s );
            s = st.nextToken().replace( ' ', '+' );
            minx = Double.parseDouble( s );
            s = st.nextToken().replace( ' ', '+' );
            maxy = Double.parseDouble( s );
            s = st.nextToken().replace( ' ', '+' );
            maxx = Double.parseDouble( s );
        } else {
            // old method
            String s = st.nextToken().replace( ' ', '+' );
            minx = Double.parseDouble( s );
            s = st.nextToken().replace( ' ', '+' );
            miny = Double.parseDouble( s );
            s = st.nextToken().replace( ' ', '+' );
            maxx = Double.parseDouble( s );
            s = st.nextToken().replace( ' ', '+' );
            maxy = Double.parseDouble( s );
        }

        // check for consistency
        if ( minx >= maxx ) {
            throw new InvalidFormatException( "minx must be lower than maxx" );
        }

        if ( miny >= maxy ) {
            throw new InvalidFormatException( "miny must be lower than maxy" );
        }

        boundingBox = GeometryFactory.createEnvelope( minx, miny, maxx, maxy, null );

        // TRANSPARENCY
        boolean transparency = false;
        String tp = model.remove( "TRANSPARENT" );
        if ( tp != null ) {
            transparency = tp.toUpperCase().trim().equals( "TRUE" );
        }

        String mime = MimeTypeMapper.toMimeType( format );
        if ( mime.equals( "image/jpg" ) || mime.equals( "image/jpeg" ) || mime.equals( "image/bmp" )
             || mime.equals( "image/tif" ) || mime.equals( "image/tiff" ) ) {
            transparency = false;
        }

        // BGCOLOR
        tmp = model.remove( "BGCOLOR" );
        Color bgColor = Color.white;
        if ( tmp != null ) {
            bgColor = Color.decode( tmp );
        }

        // EXCEPTIONS
        String exceptions = model.remove( "EXCEPTIONS" );

        if ( exceptions == null ) {
            if ( is130 ) {
                exceptions = "XML";
            } else {
                exceptions = "application/vnd.ogc.se_xml";
            }
        }

        // WFS
        /*
         * URL wFS_URL = null; if ((String)model.get( "WFS" ) != null) { wFS_URL = new URL((String)model.remove( "WFS"
         * )); }
         */

        // ID
        String id = model.remove( "ID" );
        if ( id == null ) {
            throw new InconsistentRequestException( "ID-value must be set" );
        }

        String dim = model.remove( "TIME" );
        DimensionValues dimTime = dim == null ? null : new DimensionValues( dim );
        dim = model.remove( "ELEVATION" );
        DimensionValues dimElev = dim == null ? null : new DimensionValues( dim );

        // VendorSpecificParameter; because all defined parameters has been
        // removed
        // from the model the vendorSpecificParameters are what left
        Map<String, String> vendorSpecificParameter = new HashMap<String, String>();
        for ( Object str : model.keySet() ) {
            vendorSpecificParameter.put( str.toString(), model.get( str ).toString() );
        }

        return new GetMap( version, id, ls, format, width, height, srs, boundingBox, transparency, bgColor, exceptions,
                           sLD_URL, sld, vendorSpecificParameter, dimTime, dimElev );
    }

    /**
     * creates a <tt>GetMapRequest</tt> from its XML representation as defined in SLD 1.0.0 specification
     * 
     * <p>
     * This method does not yet cope with 1.3.0.
     * </p>
     * 
     * @param id
     *            an unique id of the request
     * @param doc
     *            the document tree
     * @return an instance of <tt>GetMapRequest</tt>
     * @throws XMLParsingException
     * @throws InvalidSRSException
     * @throws InconsistentRequestException
     */
    public static GetMap create( String id, Document doc )
                            throws XMLParsingException, InvalidSRSException, InconsistentRequestException {
        String PSLD = CommonNamespaces.SLD_PREFIX + ':';

        Element root = doc.getDocumentElement();
        NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();
        Element sldElem = (Element) XMLTools.getRequiredNode( root, PSLD + "StyledLayerDescriptor", nsContext );
        XMLFragment xml = new XMLFragment();
        xml.setRootElement( sldElem );

        StyledLayerDescriptor sld = SLDFactory.createSLD( xml );
        String version = root.getAttribute( "version" );

        boolean is130 = false;

        if ( "1.3.0".compareTo( version ) <= 0 ) {
            is130 = true;
        }

        Element bboxElem = (Element) XMLTools.getRequiredNode( root, PSLD + "BoundingBox", nsContext );

        Envelope bbox;
        try {
            bbox = GMLGeometryAdapter.wrapBox( bboxElem, null );
            // check for consistency
            if ( bbox.getMin().getX() >= bbox.getMax().getX() ) {
                throw new InvalidFormatException( "minx must be lower than maxx" );
            }

            if ( bbox.getMin().getY() >= bbox.getMax().getY() ) {
                throw new InvalidFormatException( "miny must be lower than maxy" );
            }
        } catch ( InvalidGMLException e ) {
            LOG.logDebug( e.getLocalizedMessage(), e );
            if ( bboxElem == null ) {
                throw new InconsistentRequestException( Messages.getMessage( "WMS_NO_BOUNDINGBOX" ) );
            }
            if ( is130 ) {
                throw new InvalidCRSException( Messages.getMessage( "WMS_UNKNOWN_CRS",
                                                                    bboxElem.getAttribute( "srsName" ) ) );
            }
            throw new InvalidSRSException( Messages.getMessage( "WMS_UNKNOWN_CRS", bboxElem.getAttribute( "srsName" ) ) );
        } catch ( UnknownCRSException e ) {
            LOG.logDebug( e.getLocalizedMessage(), e );
            if ( bboxElem == null ) {
                throw new InconsistentRequestException( Messages.getMessage( "WMS_NO_BOUNDINGBOX" ) );
            }
            if ( is130 ) {
                throw new InvalidCRSException( Messages.getMessage( "WMS_UNKNOWN_CRS",
                                                                    bboxElem.getAttribute( "srsName" ) ) );
            }
            throw new InvalidSRSException( Messages.getMessage( "WMS_UNKNOWN_CRS", bboxElem.getAttribute( "srsName" ) ) );
        }

        String srs = bbox.getCoordinateSystem().getIdentifier().toString();

        Element output = (Element) XMLTools.getRequiredNode( root, PSLD + "Output", nsContext );

        boolean transparent = XMLTools.getNodeAsBoolean( output, PSLD + "Transparent", nsContext, false );

        int width = 0;
        int height = 0;
        try {
            Element node = (Element) XMLTools.getRequiredNode( output, PSLD + "Size", nsContext );
            width = XMLTools.getRequiredNodeAsInt( node, PSLD + "Width", nsContext );
            height = XMLTools.getRequiredNodeAsInt( node, PSLD + "Height", nsContext );
        } catch ( XMLParsingException e ) {
            throw new InconsistentRequestException( Messages.getMessage( "WMS_REQUEST_SIZE" ) );
        }

        String exception = XMLTools.getNodeAsString( output, PSLD + "Exceptions", nsContext,
                                                     "application/vnd.ogc.se_xml" );
        String sbgColor = XMLTools.getNodeAsString( output, PSLD + "BGColor", nsContext, "#FFFFFF" );
        Color bgColor = Color.decode( sbgColor );

        String format = XMLTools.getRequiredNodeAsString( output, PSLD + "Format", nsContext );
        if ( format == null ) {
            throw new InconsistentRequestException( "FORMAT-value must be set" );
        }
        try {
            format = URLDecoder.decode( format, CharsetUtils.getSystemCharset() );
        } catch ( UnsupportedEncodingException e1 ) {
            e1.printStackTrace();
        }
        if ( !MimeTypeMapper.isKnownImageType( format ) ) {
            throw new InvalidFormatException( format + " is not a valid image/result format" );
        }

        GetMap req = new GetMap( version, id, null, null, null, format, width, height, srs, bbox, transparent, bgColor,
                                 exception, null, null, sld, null );

        return req;
    }

    /**
     * Creates a new GetMapRequest object.
     * 
     * @param version
     * @param id
     * @param layers
     * @param elevation
     * @param sampleDimension
     * @param format
     * @param width
     * @param height
     * @param srs
     * @param boundingBox
     * @param transparency
     * @param bGColor
     * @param exceptions
     * @param time
     * @param sldURL
     * @param sld
     * @param vendorSpecificParameter
     * 
     */
    GetMap( String version, String id, Layer[] layers, Values elevation, Map<String, Values> sampleDimension,
            String format, int width, int height, String srs, Envelope boundingBox, boolean transparency,
            Color bGColor, String exceptions, Values time, URL sldURL, StyledLayerDescriptor sld,
            Map<String, String> vendorSpecificParameter ) {
        super( version, id, vendorSpecificParameter );

        if ( layers != null ) {
            this.layers = Arrays.asList( layers );
        } else {
            this.layers = new ArrayList<Layer>();
        }
        this.sld = sld;
        this.elevation = elevation;
        this.sampleDimension = sampleDimension;
        this.format = format;
        this.width = width;
        this.height = height;
        this.srs = srs;
        this.boundingBox = boundingBox;
        this.transparency = transparency;
        this.bGColor = bGColor;
        this.exceptions = exceptions;
        this.time = time;
        this.sLD_URL = sldURL;
        // setWFS_URL( wFS_URL );
    }

    /**
     * @param version
     * @param id
     * @param ls
     * @param format
     * @param width
     * @param height
     * @param srs
     * @param boundingBox
     * @param transparency
     * @param bgColor
     * @param exceptions
     * @param sld_url
     * @param sld
     * @param vendorSpecificParameter
     * @param dimTime
     * @param dimElev
     */
    public GetMap( String version, String id, Layer[] ls, String format, int width, int height, String srs,
                   Envelope boundingBox, boolean transparency, Color bgColor, String exceptions, URL sld_url,
                   StyledLayerDescriptor sld, Map<String, String> vendorSpecificParameter, DimensionValues dimTime,
                   DimensionValues dimElev ) {
        this( version, id, ls, null, null, format, width, height, srs, boundingBox, transparency, bgColor, exceptions,
              null, sld_url, sld, vendorSpecificParameter );
        this.dimTime = dimTime;
        this.dimElev = dimElev;
    }

    /**
     * The FORMAT parameter specifies the output format of the response to an operation.
     * <p>
     * </p>
     * An OGC Web CapabilitiesService may offer only a subset of the formats known for that type of operation, but the
     * server shall advertise in its Capabilities XML those formats it does support and shall accept requests for any
     * format it advertises. A CapabilitiesService Instance may optionally offer a new format not previously offered by
     * other instances, with the recognition that clients are not required to accept or process an unknown format. If a
     * request contains a Format not offered by a particular server, the server shall throw a CapabilitiesService
     * Exception (with code "InvalidFormat").
     * 
     * @return the output format
     */
    public String getFormat() {
        return format;
    }

    /**
     * sets the format
     * 
     * @param format
     *            the requested output-format
     */
    public void setFormat( String format ) {
        this.format = format;
    }

    /**
     * The required LAYERS parameter lists the map layer(s) to be returned by this GetMap request. The value of the
     * LAYERS parameter is a comma-separated list of one or more valid layer names. Allowed layer names are the
     * character data content of any <Layer><Name> element in the Capabilities XML.
     * <p>
     * </p>
     * A WMS shall render the requested layers by drawing the leftmost in the list bottommost, the next one over that,
     * and so on.
     * <p>
     * </p>
     * Each layer is associated to a style. Styles are also is encoded as a comma- seperated list within the GetMap
     * request.
     * <p>
     * </p>
     * The required STYLES parameter lists the style in which each layer is to be rendered. There is a one-to-one
     * correspondence between the values in the LAYERS parameter and the values in the STYLES parameter. Because of this
     * layer-style combinations are returned coupled within an array of Layer- objects. Each map in the list of LAYERS
     * is drawn using the corresponding style in the same position in the list of STYLES. Each style Name shall be one
     * that was defined in the <Name> element of a <Style> element that is either directly contained within, or
     * inherited by, the associated <Layer> element in Capabilities XML.
     * 
     * @return The required LAYERS
     */
    public Layer[] getLayers() {
        return layers.toArray( new Layer[layers.size()] );
    }

    /**
     * adds the &lt;Layer&gt;
     * 
     * @param layers
     */
    public void addLayers( Layer layers ) {
        this.layers.add( layers );
    }

    /**
     * sets the &lt;Layer&gt;
     * 
     * @param layers
     *            a set of layer
     */
    public void setLayers( Layer[] layers ) {
        this.layers.clear();

        if ( layers != null ) {
            for ( int i = 0; i < layers.length; i++ ) {
                this.layers.add( layers[i] );
            }
        }
    }

    /**
     * The required SRS parameter states which Spatial Reference System applies to the values in the BBOX parameter. The
     * value of the SRS parameter shall be one of the values defined in the character data section of an <SRS> element
     * defined or inherited by the requested layer. The same SRS applies to all layers in a single request.
     * <p>
     * </p>
     * If the WMS server has declared SRS=NONE for a Layer, as discussed in the Basic CapabilitiesService Elements
     * section, then the Layer does not have a well-defined spatial reference system and should not be shown in
     * conjunction with other layers. The Client shall specify SRS=NONE (case-insensitive) in the GetMap request and the
     * Server may issue a CapabilitiesService Exception otherwise.
     * 
     * @return the spatial reference system
     */
    public String getSrs() {
        return srs;
    }

    /**
     * sets the srs
     * 
     * @param srs
     *            the spatial reference system
     */
    public void setSrs( String srs ) {
        this.srs = srs;
    }

    /**
     * The required BBOX parameter allows a Client to request a particular Bounding Box. Bounding Boxes are defined in
     * the Basic CapabilitiesService Elements section. The value of the BBOX parameter in a GetMap request is a list of
     * comma-separated numbers of the form "minx,miny,maxx,maxy".
     * <p>
     * </p>
     * If the WMS server has declared that a Layer is not subsettable then the Client shall specify exactly the declared
     * Bounding Box values in the GetMap request and the Server may issue a CapabilitiesService Exception otherwise.
     * 
     * @return the bounding box
     */
    public Envelope getBoundingBox() {
        return boundingBox;
    }

    /**
     * @see #getBoundingBox()
     * @param boundingBox
     */
    public void setBoundingBox( Envelope boundingBox ) {
        this.boundingBox = boundingBox;
    }

    /**
     * WIDTH specifies the number of pixels to be used between the minimum and maximum X values (inclusive) in the BBOX
     * parameter. The returned picture, regardless of its return format, shall have exactly the specified width and
     * height in pixels. In the case where the aspect ratio of the BBOX and the ratio width/height are different, the
     * WMS shall stretch the returned map so that the resulting pixels could themselves be rendered in the aspect ratio
     * of the BBOX. In other words, it should be possible using this definition to request a map for a device whose
     * output pixels are themselves non-square, or to stretch a map into an image area of a different aspect ratio.
     * 
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @see #getWidth()
     * @param width
     */
    public void setWidth( int width ) {
        this.width = width;
    }

    /**
     * HEIGHT specifies the number of pixels between the minimum and maximum Y values. The returned picture, regardless
     * of its return format, shall have exactly the specified width and height in pixels. In the case where the aspect
     * ratio of the BBOX and the ratio width/height are different, the WMS shall stretch the returned map so that the
     * resulting pixels could themselves be rendered in the aspect ratio of the BBOX. In other words, it should be
     * possible using this definition to request a map for a device whose output pixels are themselves non-square, or to
     * stretch a map into an image area of a different aspect ratio.
     * 
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @see #getHeight()
     * @param height
     */
    public void setHeight( int height ) {
        this.height = height;
    }

    /**
     * The optional TRANSPARENT parameter specifies whether the map background is to be made transparent or not.
     * TRANSPARENT can take on two values, "TRUE" or "FALSE". The default value is FALSE if this parameter is absent
     * from the request.
     * <p>
     * </p>
     * The ability to return pictures drawn with transparent pixels allows results of different Map requests to be
     * overlaid, producing a composite map. It is strongly recommended that every WMS offer a format that provides
     * transparency for layers which could sensibly be overlaid above others.
     * 
     * @return the transparency setting
     */
    public boolean getTransparency() {
        return transparency;
    }

    /**
     * The optional BGCOLOR parameter specifies the color to be used as the background of the map. The general format of
     * BGCOLOR is a hexadecimal encoding of an RGB value where two hexadecimal characters are used for each of Red,
     * Green, and Blue color values. The values can range between 00 and FF for each (0 and 255, base 10). The format is
     * 0xRRGGBB; either upper or lower case characters are allowed for RR, GG, and BB values. The "0x" prefix shall have
     * a lower case 'x'. The default value is 0xFFFFFF (corresponding to the color white) if this parameter is absent
     * from the request.
     * 
     * @return the background color
     */
    public Color getBGColor() {
        return bGColor;
    }

    /**
     * The optional EXCEPTIONS parameter states the manner in which errors are to be reported to the client. The default
     * value is application/vnd.ogc.se_xml if this parameter is absent from the request.
     * <p>
     * </p>
     * A Web Map CapabilitiesService shall offer one or more of the following exception reporting formats by listing
     * them in separate <Format> elements inside the <Exceptions> element of its Capabilities XML. The entire MIME type
     * string in <Format> is used as the value of the EXCEPTIONS parameter. The first of these formats is required to be
     * offered by every WMS; the others are optional.
     * 
     * @return the exceptions parameter
     */
    public String getExceptions() {
        return exceptions;
    }

    /**
     * This specification is based on [ISO 8601:1988(E)]; it extends ISO 8601 in the following ways:
     * <UL>
     * <li>It defines a syntax for expressing the start, end and periodicity of a data collection.
     * <li>It defines terms to represent the 7 days of the week.
     * <li>It allows years before 0001 AD.
     * <li>It allows times in the distant geologic past (thousands, millions or billions of years before present).
     * </UL>
     * 
     * @return the time setting
     */
    public Values getTime() {
        return time;
    }

    /**
     * Some geospatial information may be available at multiple elevations. An OWS may announce available elevations in
     * its Capabilities XML, and some operations include a parameter for requesting a particular elevation. A single
     * elevation value is an integer or real number whose units are declared by naming an EPSG datum. When providing
     * elevation information, Servers should declare a default value in Capabilities XML unless there is compelling
     * reason to behave otherwise, and Servers shall respond with the default value if one has been declared and the
     * Client request does not include a value.
     * 
     * @return the elevation
     */
    public Values getElevation() {
        return elevation;
    }

    /**
     * Some geospatial information may be available at other dimensions (for example, satellite images in different
     * wavelength bands). The dimensions other than the four space-time dimensions are referred to as
     * "sample dimensions". An OWS may announce available sample dimensions in its Capabilities XML, and some operations
     * include a mechanism for including dimensional parameters. Each sample dimension has a Name and one or more valid
     * values.
     * 
     * @return the map
     */
    public Map<String, Values> getSampleDimension() {
        return sampleDimension;
    }

    /**
     * @return the URL of Styled Layer Descriptor (as defined in SLD Specification). This parameter is optional. If no
     *         sld URL is defined <tt>null</tt> will be returned.
     */
    public URL getSLD_URL() {
        return sLD_URL;
    }

    /**
     * @return the URL of Web Feature CapabilitiesService providing features to be symbolized using SLD. This parameter
     *         is optional. If no WFS URL is defined <tt>null</tt> will be returned.
     */
    public URL getWFS_URL() {
        return wFS_URL;
    }

    /**
     * @return the SLD the request is made of. This implies that a 'simple' HTTP GET-Request will be transformed into a
     *         valid SLD. This is mandatory within a JaGo WMS.
     *         <p>
     *         </p>
     *         This mean even if a GetMap request is send using the HTTP GET method, an implementing class has to map
     *         the request to a SLD data structure.
     */
    public StyledLayerDescriptor getStyledLayerDescriptor() {
        return sld;
    }

    /**
     * @return the parameter of a HTTP GET request.
     * 
     */
    @Override
    public String getRequestParameter()
                            throws OGCWebServiceException {
        return getRequestParameter( true );
    }

    /**
     * @param allowSwapAxsisOrder
     *            if <code>true</code> axis order of bounding box will be swapped if a WMS 1.3 GetMap request shall be
     *            created
     * @return the parameter of a HTTP GET request.
     * 
     */
    public String getRequestParameter( boolean allowSwapAxsisOrder )
                            throws OGCWebServiceException {

        // indicates if the request parameters are decoded as SLD. deegree won't
        // perform SLD requests through HTTP GET
        if ( boundingBox == null ) {
            throw new OGCWebServiceException( "Operations can't be expressed as HTTP GET request " );
        }

        StringBuffer sb = new StringBuffer();

        if ( getVersion().compareTo( "1.0.0" ) <= 0 ) {
            sb.append( "VERSION=" ).append( getVersion() ).append( "&REQUEST=map" );
            String f = StringTools.replace( getFormat(), "image/", "", false );
            sb.append( "&FORMAT=" );
            try {
                sb.append( URLEncoder.encode( f, CharsetUtils.getSystemCharset() ) );
            } catch ( UnsupportedEncodingException e ) {
                // system encoding should be supported...
            }
        } else {
            sb.append( "VERSION=" ).append( getVersion() ).append( "&REQUEST=GetMap" );
            sb.append( "&FORMAT=" );
            try {
                sb.append( URLEncoder.encode( getFormat(), CharsetUtils.getSystemCharset() ) );
            } catch ( UnsupportedEncodingException e ) {
                // system encoding should be supported...
            }
        }

        sb.append( "&TRANSPARENT=" ).append( Boolean.toString( getTransparency() ).toUpperCase() );
        sb.append( "&WIDTH=" ).append( getWidth() );
        sb.append( "&HEIGHT=" ).append( getHeight() );
        sb.append( "&EXCEPTIONS=" ).append( getExceptions() );
        sb.append( "&BGCOLOR=" ).append( ColorUtils.toHexCode( "0x", bGColor ) );

        if ( "1.3.0".compareTo( getVersion() ) <= 0 && allowSwapAxsisOrder ) {
            sb.append( "&BBOX=" ).append( boundingBox.getMin().getY() );
            sb.append( ',' ).append( boundingBox.getMin().getX() );
            sb.append( ',' ).append( boundingBox.getMax().getY() );
            sb.append( ',' ).append( boundingBox.getMax().getX() );
        } else {
            sb.append( "&BBOX=" ).append( boundingBox.getMin().getX() );
            sb.append( ',' ).append( boundingBox.getMin().getY() );
            sb.append( ',' ).append( boundingBox.getMax().getX() );
            sb.append( ',' ).append( boundingBox.getMax().getY() );
        }

        Layer[] layers = getLayers();
        StringBuffer l = new StringBuffer( 500 );
        StringBuffer s = new StringBuffer( 500 );

        if ( sLD_URL == null ) {
            for ( int i = 0; i < layers.length; i++ ) {
                try {
                    l.append( URLEncoder.encode( layers[i].getName(), CharsetUtils.getSystemCharset() ) );
                    l.append( ',' );
                    if ( !layers[i].getStyleName().equals( "$DEFAULT" ) ) {
                        s.append( URLEncoder.encode( layers[i].getStyleName(), CharsetUtils.getSystemCharset() ) );
                    }
                    s.append( ',' );
                } catch ( Exception e ) {
                    throw new OGCWebServiceException( e.toString() );
                }
            }

            if ( l.length() != 0 ) {
                sb.append( "&LAYERS=" ).append( l.substring( 0, l.length() - 1 ) );
            }

            if ( s.length() != 0 ) {
                sb.append( "&STYLES=" ).append( s.substring( 0, s.length() - 1 ) );
            }
        } else if ( sLD_URL != null ) {
            sb.append( "&SLD=" ).append( NetWorker.url2String( sLD_URL ) );
        } else if ( sld != null ) {
            String tmp = ( (Marshallable) sld ).exportAsXML();
            try {
                tmp = URLEncoder.encode( tmp, CharsetUtils.getSystemCharset() );
            } catch ( Exception e ) {
                throw new OGCWebServiceException( e.toString() );
            }
            sb.append( "&SLD_BODY=" ).append( tmp );
        }

        if ( "1.3.0".compareTo( getVersion() ) <= 0 ) {
            sb.append( "&CRS=" ).append( getSrs() );
        } else {
            sb.append( "&SRS=" ).append( getSrs() );
        }

        // TODO
        // append elevation and sampleDimensions
        
        if ( dimTime != null ) {
            sb.append( "&time=" + dimTime.getOriginalValue() );
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

    @Override
    public String toString() {
        String s = "An unknown " + this.getClass().getName() + " request";
        try {
            s = getRequestParameter();
        } catch ( OGCWebServiceException e ) {
            // in that case, we just don't have the parameters...
        }
        return s;
    }

    /**
     * creates a Layer object beacuse of the inner class construct.
     * 
     * @param name
     *            the name of the layer
     * @param style
     *            the corresponding style of the layer
     * @return Layer a layer object constaining name and style
     */
    public static Layer createLayer( String name, String style ) {
        return new Layer( name, style );
    }

    /**
     * A Layer object. It contains the name of the layer and the corresponding style.
     * 
     * @version $Revision$
     * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
     */
    public static class Layer implements Serializable {

        private static final long serialVersionUID = -98575941104285931L;

        private String name = null;

        private String styleName = null;

        /**
         * constructor initializing the class with the <Layer>
         * 
         * @param name
         * @param styleName
         */
        public Layer( String name, String styleName ) {
            this.name = name;
            this.styleName = styleName;
        }

        /**
         * @return the <Name>
         */
        public String getName() {
            return name;
        }

        /**
         * @return the <StyleName>
         */
        public String getStyleName() {
            return styleName;
        }

    }

    /**
     * @return the dimTime
     */
    public DimensionValues getDimTime() {
        return dimTime;
    }

    /**
     * @param dimTime
     *            the dimTime to set
     */
    public void setDimTime( DimensionValues dimTime ) {
        this.dimTime = dimTime;
    }

    /**
     * @return the dimElev
     */
    public DimensionValues getDimElev() {
        return dimElev;
    }

    /**
     * @param dimElev
     *            the dimElev to set
     */
    public void setDimElev( DimensionValues dimElev ) {
        this.dimElev = dimElev;
    }

}
