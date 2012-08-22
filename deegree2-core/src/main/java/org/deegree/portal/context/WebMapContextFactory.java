//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn
 and
 - lat/lon GmbH

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
package org.deegree.portal.context;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.deegree.datatypes.QualifiedName;
import org.deegree.enterprise.WebUtils;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Parameter;
import org.deegree.framework.util.ParameterList;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.graphics.sld.FeatureTypeStyle;
import org.deegree.graphics.sld.SLDFactory;
import org.deegree.graphics.sld.StyledLayerDescriptor;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.metadata.iso19115.Address;
import org.deegree.model.metadata.iso19115.CitedResponsibleParty;
import org.deegree.model.metadata.iso19115.ContactInfo;
import org.deegree.model.metadata.iso19115.Phone;
import org.deegree.model.metadata.iso19115.RoleCode;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.ogcbase.BaseURL;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.ImageURL;
import org.deegree.ogcwebservices.OWSUtils;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilitiesDocument;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSCapabilities;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wms.capabilities.LegendURL;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocument;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocumentFactory;
import org.deegree.ogcwebservices.wms.operation.GetLegendGraphic;
import org.deegree.owscommon_new.HTTP;
import org.deegree.owscommon_new.Operation;
import org.deegree.owscommon_new.OperationsMetadata;
import org.deegree.security.drm.model.User;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Factory class for creating an instance of a web map Context (<tt>ViewContext</tt>). The factory is able to parse
 * deegree specific extensions (General and Layer) as well as standard web map context documents.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WebMapContextFactory {

    private static final ILogger LOG = LoggerFactory.getLogger( WebMapContextFactory.class );

    /**
     * Caching the wms capabilities. Key is the wms address (URL), value is the WMSCapabilities.
     */
    private static Map<URL, WMSCapabilities> wmsCache = new HashMap<URL, WMSCapabilities>();

    private static Map<URL, WFSCapabilities> wfsCache = new HashMap<URL, WFSCapabilities>();

    private static Map<URL, WCSCapabilities> wcsCache = new HashMap<URL, WCSCapabilities>();

    /**
     * creates an instance of a ViewContext from the web map context document read from the passed URL
     * 
     * @param url
     * @param user
     * @param sessionID
     * @return new ViewContext for web map context document from <code>url</code>
     * @throws IOException
     * @throws XMLParsingException
     * @throws ContextException
     * @throws SAXException
     * @throws UnknownCRSException
     */
    public synchronized static ViewContext createViewContext( URL url, User user, String sessionID )
                            throws IOException, XMLParsingException, ContextException, SAXException,
                            UnknownCRSException {
        // cache have to be cleared because contained capabilities may has been
        // requested with other user identification
        wmsCache.clear();

        XMLFragment xml = new XMLFragment( url );

        return createViewContext( xml, user, sessionID );
    }

    /**
     * @param xml
     * @param user
     * @param sessionID
     * @return new ViewContext for web map context document from <code>xml</code>
     * @throws IOException
     * @throws XMLParsingException
     * @throws ContextException
     * @throws UnknownCRSException
     * @throws SAXException
     */
    public synchronized static ViewContext createViewContext( XMLFragment xml, User user, String sessionID )
                            throws IOException, XMLParsingException, ContextException, UnknownCRSException,
                            SAXException {

        // general section
        Element element = XMLTools.getRequiredChildElement( "General", CommonNamespaces.CNTXTNS, xml.getRootElement() );
        General general = createGeneral( element, xml );

        // Layer (List) section
        element = XMLTools.getRequiredChildElement( "LayerList", CommonNamespaces.CNTXTNS, xml.getRootElement() );

        final LayerList layerList = createLayerList( element, user, sessionID );

        GeneralExtension ge = general.getExtension();
        MapModel mapModel = ge.getMapModel();
        if ( mapModel != null ) {
            // WMC layers assigned to MapModel layers just can be set after WMC layer
            // list has been parsed
            try {
                mapModel.walkLayerTree( new MapModelVisitor() {

                    public void visit( LayerGroup layerGroup )
                                            throws Exception {
                    }

                    public void visit( MMLayer layer )
                                            throws Exception {
                        Layer[] layers = layerList.getLayers();
                        for ( Layer layer2 : layers ) {
                            if ( layer.getIdentifier().equals( layer2.getExtension().getIdentifier() ) ) {
                                layer.setLayer( layer2 );
                                break;
                            }
                        }
                    }
                } );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        ViewContext vc = new ViewContext( general, layerList );

        return vc;
    }

    /**
     * creates an instance of a class encapsulating the general context informations
     * 
     * @param element
     *            <General>
     * @param xml
     * 
     * @return instance of <tt>General</tt>
     * 
     * @throws XMLParsingException
     * @throws UnknownCRSException
     * @throws SAXException
     * @throws IOException
     */
    private static General createGeneral( Element element, XMLFragment xml )
                            throws XMLParsingException, UnknownCRSException, IOException, SAXException {

        // <Window>
        Element elem = XMLTools.getChildElement( "Window", CommonNamespaces.CNTXTNS, element );
        Rectangle rect = createWindow( elem );

        // <BoundingBox>
        elem = XMLTools.getRequiredChildElement( "BoundingBox", CommonNamespaces.CNTXTNS, element );
        Point[] bbox = createBoundingBox( elem );

        // <Title>
        String title = XMLTools.getRequiredStringValue( "Title", CommonNamespaces.CNTXTNS, element );

        // <KeywordList>
        elem = XMLTools.getChildElement( "KeywordList", CommonNamespaces.CNTXTNS, element );
        String[] keywords = createKeywords( elem );

        // <Abstract>
        String abstract_ = XMLTools.getStringValue( "Abstract", CommonNamespaces.CNTXTNS, element, null );

        // <LogoURL>
        elem = XMLTools.getChildElement( "LogoURL", CommonNamespaces.CNTXTNS, element );
        ImageURL logoURL = createImageURL( elem );

        // <DescriptionURL>

        // elem = XMLTools.getChildElement( "DescriptionURL", CommonNamespaces.CNTXTNS, element );
        elem = XMLTools.getRequiredElement( element, CommonNamespaces.CNTXT_PREFIX + ":DescriptionURL",
                                            CommonNamespaces.getNamespaceContext() );

        BaseURL descriptionURL = createBaseURL( elem );

        // <ContactInformation>
        elem = XMLTools.getChildElement( "ContactInformation", CommonNamespaces.CNTXTNS, element );
        CitedResponsibleParty contact = createContactInformation( elem );

        // <Extension>
        elem = XMLTools.getChildElement( "Extension", CommonNamespaces.CNTXTNS, element );
        GeneralExtension extension = createGeneralExtension( elem, xml );

        General general = null;
        try {
            general = new General( title, abstract_, rect, contact, bbox, descriptionURL, logoURL, keywords, extension );
        } catch ( Exception e ) {
            throw new XMLParsingException( e.getMessage(), e );
        }

        return general;
    }

    /**
     * creates a <tt>Rectangle<tt> (Window) instance from the passed Element.
     * 
     * @param element
     *            <Window>
     * 
     * @return instance of <tt>Rectangle</tt>
     * 
     * @throws XMLParsingException
     */
    private static Rectangle createWindow( Element element )
                            throws XMLParsingException {

        Rectangle rect = null;

        if ( element != null ) {
            String tmp = XMLTools.getRequiredAttrValue( "width", null, element );
            int width = Integer.parseInt( tmp );
            tmp = XMLTools.getRequiredAttrValue( "height", null, element );

            int height = Integer.parseInt( tmp );
            rect = new Rectangle( width, height );
        }

        return rect;
    }

    /**
     * creates a <tt>Envelope </tt> from the passed Element
     * 
     * @param element
     *            <BoundingBox>
     * 
     * @return instance of <tt>Envelope</tt>
     * 
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    private static Point[] createBoundingBox( Element element )
                            throws XMLParsingException, UnknownCRSException {

        String srs = XMLTools.getRequiredAttrValue( "SRS", null, element );
        CoordinateSystem crs = CRSFactory.create( srs );
        String tmp = XMLTools.getRequiredAttrValue( "minx", null, element );
        double minx = Double.parseDouble( tmp );
        tmp = XMLTools.getRequiredAttrValue( "miny", null, element );

        double miny = Double.parseDouble( tmp );
        tmp = XMLTools.getRequiredAttrValue( "maxx", null, element );

        double maxx = Double.parseDouble( tmp );
        tmp = XMLTools.getRequiredAttrValue( "maxy", null, element );

        double maxy = Double.parseDouble( tmp );

        Point[] points = new Point[2];
        points[0] = GeometryFactory.createPoint( minx, miny, crs );
        points[1] = GeometryFactory.createPoint( maxx, maxy, crs );

        return points;
    }

    /**
     * creates an array of keywords (String) from the passed Keyword list
     * 
     * @param element
     *            <KeywordList>
     * 
     * @return array of Strings
     * 
     */
    private static String[] createKeywords( Element element ) {

        ElementList el = XMLTools.getChildElements( "Keyword", CommonNamespaces.CNTXTNS, element );
        String[] keywords = new String[el.getLength()];

        for ( int i = 0; i < keywords.length; i++ ) {
            keywords[i] = XMLTools.getStringValue( el.item( i ) );
        }

        return keywords;
    }

    /**
     * creates an instance of an ImageURL that is used for <LogoURL> and LegendURL
     * 
     * @param element
     *            <LogoURL> or <LegendURL>
     * 
     * @return instance of <tt>ImageURL</tt>
     * 
     * @throws XMLParsingException
     */
    private static ImageURL createImageURL( Element element )
                            throws XMLParsingException {

        ImageURL imageURL = null;

        if ( element != null ) {
            String tmp = XMLTools.getAttrValue( element, null, "width", null );
            int width = -1;
            if ( tmp != null ) {
                width = Integer.parseInt( tmp );
            }
            tmp = XMLTools.getAttrValue( element, null, "height", null );
            int height = -1;
            if ( tmp != null ) {
                height = Integer.parseInt( tmp );
            }
            String format = XMLTools.getAttrValue( element, null, "format", null );

            Element elem = XMLTools.getRequiredChildElement( "OnlineResource", CommonNamespaces.CNTXTNS, element );
            URL onlineResource = createOnlineResource( elem );

            imageURL = new ImageURL( width, height, format, onlineResource );
        }

        return imageURL;
    }

    /**
     * creates an instance of an URL described by a <OnlineResource> element
     * 
     * @param element
     *            <OnlineResource>
     * 
     * @return instance of <tt>URL</tt>
     * 
     * @throws XMLParsingException
     */
    private static URL createOnlineResource( Element element )
                            throws XMLParsingException {

        URL onlineResource = null;

        if ( element != null ) {
            String type = element.getAttributeNS( CommonNamespaces.XLNNS.toASCIIString(), "type" );
            if ( ( type != null ) && !"".equals( type ) && !type.equals( "simple" ) ) {
                throw new XMLParsingException( "unknown type of online resource: " + type );
            }
            String tmp = element.getAttributeNS( CommonNamespaces.XLNNS.toASCIIString(), "href" );
            if ( !isImageURL( tmp ) ) {
                tmp = OWSUtils.validateHTTPGetBaseURL( tmp );
            }

            try {
                onlineResource = new URL( tmp );
            } catch ( Exception e ) {
                e.printStackTrace();
                throw new XMLParsingException( "couldn't create online resource", e );
            }
        }

        return onlineResource;
    }

    /**
     * @param tmp
     * @return
     */
    private static boolean isImageURL( String tmp ) {
        String s = tmp.toLowerCase();
        return s.endsWith( ".gif" ) || s.endsWith( ".png" ) || s.endsWith( ".jpg" ) || s.endsWith( ".jpeg" )
               || s.endsWith( ".tif" ) || s.endsWith( ".tiff" ) || s.endsWith( ".bmp" );
    }

    /**
     * creates an instance of BaseURL that is used for <DescriptionURL>
     * 
     * @param element
     *            <DescriptionURL>
     * @return instance of <tt>BaseURL</tt>, or null, if the passed element is null
     * @throws XMLParsingException
     */
    private static BaseURL createBaseURL( Element element )
                            throws XMLParsingException {

        BaseURL baseURL = null;

        if ( element != null ) {
            String format = XMLTools.getAttrValue( element, null, "format", null );

            Element elem = XMLTools.getElement( element, CommonNamespaces.CNTXT_PREFIX + ":OnlineResource",
                                                CommonNamespaces.getNamespaceContext() );
            URL onlineResource = createOnlineResource( elem );

            baseURL = new BaseURL( format, onlineResource );
        }

        return baseURL;
    }

    /**
     * Creates a <tt>ContactInformation</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'ContactInformation'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'ContactInformation'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>ContactInformation</tt>-instance
     */
    private static CitedResponsibleParty createContactInformation( Element element )
                            throws XMLParsingException {

        CitedResponsibleParty contact = null;

        if ( element != null ) {
            // optional: <ContactPersonPrimary>
            Element contactPersonPrimaryElement = XMLTools.getChildElement( "ContactPersonPrimary",
                                                                            CommonNamespaces.CNTXTNS, element );

            String contactPerson = null;
            String contactOrganization = null;
            if ( contactPersonPrimaryElement != null ) {
                // required: <ContactPerson>
                contactPerson = XMLTools.getRequiredStringValue( "ContactPerson", CommonNamespaces.CNTXTNS,
                                                                 contactPersonPrimaryElement );

                // required: <ContactOrganization>
                contactOrganization = XMLTools.getRequiredStringValue( "ContactOrganization", CommonNamespaces.CNTXTNS,
                                                                       contactPersonPrimaryElement );
            }

            // optional: <ContactPosition>
            String contactPosition = XMLTools.getStringValue( "ContactPosition", CommonNamespaces.CNTXTNS, element,
                                                              null );

            // optional: <ContactAddress>
            Address contactAddress = null;
            Element contactAddressElement = XMLTools.getChildElement( "ContactAddress", CommonNamespaces.CNTXTNS,
                                                                      element );

            if ( contactAddressElement != null ) {
                // optional: <ContactElectronicMailAddress>
                String eMailAddress = XMLTools.getStringValue( "ContactElectronicMailAddress",
                                                               CommonNamespaces.CNTXTNS, element, null );
                contactAddress = createContactAddress( eMailAddress, contactAddressElement );
            }

            // optional: <ContactVoiceTelephone>
            String voice = XMLTools.getStringValue( "ContactVoiceTelephone", CommonNamespaces.CNTXTNS, element, null );

            // optional: <ContactFacsimileTelephone>
            String fax = XMLTools.getStringValue( "ContactFacsimileTelephone", CommonNamespaces.CNTXTNS, element, null );

            Phone phone = new Phone( new String[] { fax }, new String[] { voice } );
            ContactInfo ci = new ContactInfo( contactAddress, null, null, null, phone );
            contact = new CitedResponsibleParty( new ContactInfo[] { ci }, new String[] { contactPerson },
                                                 new String[] { contactOrganization },
                                                 new String[] { contactPosition }, new RoleCode[0] );
        }

        return contact;
    }

    /**
     * Creates a <tt>ContactAddress</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'ContactAddress'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'ContactAddress'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>ContactAddress</tt>-instance
     */
    private static Address createContactAddress( String eMail, Element element )
                            throws XMLParsingException {

        // required: <AddressType>
        /*
         * String addressType = XMLTools.getRequiredStringValue( "AddressType", CommonNamespaces.CNTXTNS, element );
         */
        // required: <Address>
        String address = XMLTools.getRequiredStringValue( "Address", CommonNamespaces.CNTXTNS, element );

        // required: <City>
        String city = XMLTools.getRequiredStringValue( "City", CommonNamespaces.CNTXTNS, element );

        // required: <StateOrProvince>
        String stateOrProvince = XMLTools.getRequiredStringValue( "StateOrProvince", CommonNamespaces.CNTXTNS, element );

        // required: <PostCode>
        String postCode = XMLTools.getRequiredStringValue( "PostCode", CommonNamespaces.CNTXTNS, element );

        // required: <Country>
        String country = XMLTools.getRequiredStringValue( "Country", CommonNamespaces.CNTXTNS, element );

        return new Address( stateOrProvince, city, country, new String[] { address }, new String[] { eMail }, postCode );
    }

    /**
     * creates an instance of a class encapsulating the deegree specific extensions of the general section of a web map
     * context document
     * 
     * @param element
     *            <Extension>
     * @param xml
     * 
     * @return instance of <tt>GeneralExtension</tt>
     * 
     * @throws XMLParsingException
     * @throws SAXException
     * @throws IOException
     */
    private static GeneralExtension createGeneralExtension( Element element, XMLFragment xml )
                            throws XMLParsingException, IOException, SAXException {

        GeneralExtension ge = null;

        if ( element != null ) {

            // retunrs the current mode of a client using a WMC
            String mode = XMLTools.getStringValue( "Mode", CommonNamespaces.DGCNTXTNS, element, "ZOOMIN" );

            // <AuthentificationSettings>
            Element elem = XMLTools.getChildElement( "AuthentificationSettings", CommonNamespaces.DGCNTXTNS, element );
            AuthentificationSettings authSettings = null;
            if ( elem != null ) {
                authSettings = createAuthentificationSettings( elem );
            }
            // <IOSetiings>
            elem = XMLTools.getChildElement( "IOSettings", CommonNamespaces.DGCNTXTNS, element );
            IOSettings ioSettings = null;
            if ( elem != null ) {
                ioSettings = createIOSettings( elem, xml );
            }
            // <Frontend>
            elem = XMLTools.getChildElement( "Frontend", CommonNamespaces.DGCNTXTNS, element );
            Frontend frontend = null;
            if ( elem != null ) {
                frontend = createFrontend( elem, xml );
            }
            // <MapParameter>
            elem = XMLTools.getRequiredChildElement( "MapParameter", CommonNamespaces.DGCNTXTNS, element );
            MapParameter mapParameter = createMapParameter( elem );

            // <LayerTree> old version
            elem = XMLTools.getChildElement( "LayerTree", CommonNamespaces.DGCNTXTNS, element );

            Node layerTreeRoot = null;
            if ( elem != null ) {
                Element nodeElem = XMLTools.getRequiredChildElement( "Node", CommonNamespaces.DGCNTXTNS, elem );
                layerTreeRoot = createNode( nodeElem, null );
            } else {
                try {
                    layerTreeRoot = new Node( 0, null, "root", false, false );
                    Node[] nodes = new Node[] { new Node( 1, layerTreeRoot, "deegree", false, false ) };
                    layerTreeRoot.setNodes( nodes );
                } catch ( ContextException e ) {
                    throw new XMLParsingException( "couldn't create layertree node", e );
                }
            }

            elem = XMLTools.getChildElement( "MapModel", CommonNamespaces.DGCNTXTNS, element );
            MapModel mapModel = null;
            if ( elem != null ) {
                MapModelDocument doc = new MapModelDocument( elem );
                mapModel = doc.parseMapModel();
            }

            String tmp = XMLTools.getStringValue( "XSLT", CommonNamespaces.DGCNTXTNS, element, "context2HTML.xsl" );
            URL xslt = xml.resolve( tmp );
            File file = new File( xslt.getFile() );
            if ( !file.exists() ) {
                // address xslt script from WEB-INF/conf/igeoportal
                file = new File( file.getParentFile().getParentFile().getParent() + File.separatorChar + file.getName() );
                xslt = file.toURL();
            }

            ge = new GeneralExtension( ioSettings, frontend, mapParameter, authSettings, mode, layerTreeRoot, mapModel,
                                       xslt );
        }

        return ge;
    }

    /**
     * creates a node in the layerTree
     */
    private static Node createNode( Element nodeElem, Node parent )
                            throws XMLParsingException {

        int id = Integer.parseInt( XMLTools.getRequiredAttrValue( "id", null, nodeElem ) );
        String title = XMLTools.getRequiredAttrValue( "title", null, nodeElem );
        String s = XMLTools.getAttrValue( nodeElem, null, "selectable", null );
        boolean selectable = "true".equals( s ) || "1".equals( s );
        s = XMLTools.getAttrValue( nodeElem, null, "collapsed", null );
        boolean collapsed = "true".equals( s ) || "1".equals( s );
        Node node = null;
        try {
            node = new Node( id, parent, title, selectable, collapsed );
        } catch ( Exception e ) {
            throw new XMLParsingException( "couldn't create layertree node", e );
        }

        // get Child nodes
        ElementList nl = XMLTools.getChildElements( "Node", CommonNamespaces.DGCNTXTNS, nodeElem );
        Node[] nodes = new Node[nl.getLength()];
        for ( int i = 0; i < nodes.length; i++ ) {
            nodes[i] = createNode( nl.item( i ), node );
        }
        // set child nodes
        node.setNodes( nodes );

        return node;
    }

    /**
     * creates an instance of a class encapsulating access descrition to an authentification service for a deegree map
     * client
     * 
     * @param element
     *            <AuthentificationSettings>
     * @return instance of <tt>AuthentificationSettings</tt>
     * @throws XMLParsingException
     */
    private static AuthentificationSettings createAuthentificationSettings( Element element )
                            throws XMLParsingException {

        Element elem = XMLTools.getRequiredChildElement( "AuthentificationService", CommonNamespaces.DGCNTXTNS, element );
        elem = XMLTools.getRequiredChildElement( "OnlineResource", CommonNamespaces.CNTXTNS, elem );

        URL url = createOnlineResource( elem );
        BaseURL bu = new BaseURL( "text/xml", url );

        return new AuthentificationSettings( bu );
    }

    /**
     * creates an instance of a class encapsulating the frontend (GUI) description of a deegree map client
     * 
     * @param element
     * 
     * @param xml
     * 
     * @return instance of <tt>Frontend</tt>
     * 
     * @throws XMLParsingException
     * @throws SAXException
     * @throws IOException
     */
    private static Frontend createFrontend( Element element, XMLFragment xml )
                            throws XMLParsingException, IOException, SAXException {

        // <Controller>
        String controller = XMLTools.getRequiredStringValue( "Controller", CommonNamespaces.DGCNTXTNS, element );
        // <Style>
        String style = XMLTools.getStringValue( "Style", CommonNamespaces.DGCNTXTNS, element, null );
        // <Buttons>
        String buttons = XMLTools.getStringValue( "Buttons", CommonNamespaces.DGCNTXTNS, element, null );
        // <CommonJS>
        Element elem = XMLTools.getChildElement( "CommonJS", CommonNamespaces.DGCNTXTNS, element );
        String[] commonJS = createCommonJS( elem );
        // <West>
        elem = XMLTools.getChildElement( "West", CommonNamespaces.DGCNTXTNS, element );
        GUIArea west = createGUIArea( elem, xml );
        // <East>
        elem = XMLTools.getChildElement( "East", CommonNamespaces.DGCNTXTNS, element );
        GUIArea east = createGUIArea( elem, xml );
        // <North>
        elem = XMLTools.getChildElement( "North", CommonNamespaces.DGCNTXTNS, element );
        GUIArea north = createGUIArea( elem, xml );
        // <South>
        elem = XMLTools.getChildElement( "South", CommonNamespaces.DGCNTXTNS, element );
        GUIArea south = createGUIArea( elem, xml );
        // <Center>
        elem = XMLTools.getChildElement( "Center", CommonNamespaces.DGCNTXTNS, element );
        GUIArea center = createGUIArea( elem, xml );
        // <Header>
        String header = XMLTools.getStringValue( "Header", CommonNamespaces.DGCNTXTNS, element, null );
        // <Footer>
        String footer = XMLTools.getStringValue( "Footer", CommonNamespaces.DGCNTXTNS, element, null );

        Frontend frontend = new JSPFrontend( controller, west, east, south, north, center, style, buttons, commonJS,
                                             header, footer );

        return frontend;
    }

    /**
     * creates a list of javascript pages (names) that contains javascript objects and methods that are used by more
     * than one module
     * 
     * @param element
     *            <CommonJS>
     * 
     * @return instance of <tt>String[]</tt>
     * 
     */
    private static String[] createCommonJS( Element element ) {
        String[] commonJS = null;
        if ( element != null ) {
            ElementList el = XMLTools.getChildElements( "Name", CommonNamespaces.DGCNTXTNS, element );
            commonJS = new String[el.getLength()];
            for ( int i = 0; i < commonJS.length; i++ ) {
                commonJS[i] = XMLTools.getStringValue( el.item( i ) );
            }
        }

        return commonJS;
    }

    /**
     * creates an instance of a class encapsulating the GUI description of one region of the GUI
     * 
     * @param element
     *            <West>; <East>; <South>; <North> or <Center>
     * 
     * @return instance of <tt>GUIArea</tt>
     * 
     * @throws XMLParsingException
     * @throws SAXException
     * @throws IOException
     */
    private static GUIArea createGUIArea( Element element, XMLFragment xml )
                            throws XMLParsingException, IOException, SAXException {

        GUIArea gui = null;
        if ( element != null ) {
            String tmp = element.getLocalName();
            int area = 0;
            if ( tmp.equals( "West" ) ) {
                area = GUIArea.WEST;
            } else if ( tmp.equals( "East" ) ) {
                area = GUIArea.EAST;
            } else if ( tmp.equals( "South" ) ) {
                area = GUIArea.SOUTH;
            } else if ( tmp.equals( "North" ) ) {
                area = GUIArea.NORTH;
            } else if ( tmp.equals( "Center" ) ) {
                area = GUIArea.CENTER;
            }

            NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();
            int w = XMLTools.getNodeAsInt( element, "./@width", nsContext, -1 );
            int h = XMLTools.getNodeAsInt( element, "./@height", nsContext, -1 );
            int l = XMLTools.getNodeAsInt( element, "./@left", nsContext, -1 );
            int t = XMLTools.getNodeAsInt( element, "./@top", nsContext, -1 );
            int r = XMLTools.getNodeAsInt( element, "./@right", nsContext, -1 );
            int b = XMLTools.getNodeAsInt( element, "./@bottom", nsContext, -1 );
            boolean ov = XMLTools.getNodeAsBoolean( element, "./@overlay", nsContext, false );
            boolean head = XMLTools.getNodeAsBoolean( element, "./@header", nsContext, false );
            boolean close = XMLTools.getNodeAsBoolean( element, "./@closable", nsContext, false );

            // hidden
            tmp = XMLTools.getAttrValue( element, null, "hidden", null );
            boolean hidden = "1".equals( tmp ) || "true".equals( tmp );
            // <Module>
            ElementList el = XMLTools.getChildElements( "Module", CommonNamespaces.DGCNTXTNS, element );
            Module[] modules = new Module[el.getLength()];
            for ( int i = 0; i < modules.length; i++ ) {
                modules[i] = createModule( el.item( i ), xml );
            }
            gui = new GUIArea( area, hidden, w, h, l, t, r, b, ov, head, close, modules );
        }

        return gui;
    }

    /**
     * creates an instance of a class encapsulating module informations
     * 
     * @param element
     * @param xml
     * 
     * @return instance of <tt>Module</tt>
     * 
     * @throws XMLParsingException
     * @throws SAXException
     * @throws IOException
     */
    private static Module createModule( Element element, XMLFragment xml )
                            throws XMLParsingException, IOException, SAXException {

        // width and height of a Module are optional
        // if not set '0' will be used instead
        NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();
        String w = XMLTools.getNodeAsString( element, "./@width", nsContext, "-1" );
        String h = XMLTools.getNodeAsString( element, "./@height", nsContext, "-1" );
        int l = XMLTools.getNodeAsInt( element, "./@left", nsContext, -1 );
        int t = XMLTools.getNodeAsInt( element, "./@top", nsContext, -1 );
        int b = XMLTools.getNodeAsInt( element, "./@bottom", nsContext, -1 );
        int r = XMLTools.getNodeAsInt( element, "./@right", nsContext, -1 );
        boolean ov = XMLTools.getNodeAsBoolean( element, "./@overlay", nsContext, false );
        boolean head = XMLTools.getNodeAsBoolean( element, "./@header", nsContext, false );
        boolean close = XMLTools.getNodeAsBoolean( element, "./@closable", nsContext, false );
        boolean collapsed = XMLTools.getNodeAsBoolean( element, "./@collapsed", nsContext, false );
        String scrollable = XMLTools.getAttrValue( element, null, "scrolling", null );

        String tmp = XMLTools.getAttrValue( element, null, "hidden", null );
        boolean hidden = tmp.equals( "1" ) || tmp.equals( "true" );

        // <ModuleConfiguration>
        Element elem = XMLTools.getElement( element, "dgcntxt:ModuleConfiguration", nsContext );
        ModuleConfiguration mc = createModuleConfiguration( elem, xml );
        if ( mc != null ) {
            XMLFragment moduleXML = new XMLFragment( mc.getOnlineResource() );
            element = moduleXML.getRootElement();
        }

        // <Name>
        String name = XMLTools.getRequiredStringValue( "Name", CommonNamespaces.DGCNTXTNS, element );
        // <Title>
        String title = XMLTools.getStringValue( "Title", CommonNamespaces.DGCNTXTNS, element, name );
        // <Content>
        String content = XMLTools.getRequiredStringValue( "Content", CommonNamespaces.DGCNTXTNS, element );

        // <ParameterList>
        elem = XMLTools.getElement( element, "dgcntxt:ParameterList", nsContext );
        ParameterList paramList = createParameterList( elem );

        String type = XMLTools.getAttrValue( element, null, "type", null );

        String[] moduleJS = createModuleJSList( element );
        return new Module( name, title, content, hidden, type, l, t, r, b, w, h, ov, head, close, collapsed,
                           scrollable, moduleJS, mc, paramList );
    }

    /**
     * creates an instance of a class encapsulating the access the configuration of Module
     * 
     * @param element
     * @param xml
     * 
     * @return instance of <tt>ModuleConfiguration</tt>
     * 
     * @throws XMLParsingException
     * @throws MalformedURLException
     */
    private static ModuleConfiguration createModuleConfiguration( Element element, XMLFragment xml )
                            throws XMLParsingException, MalformedURLException {

        ModuleConfiguration mc = null;
        if ( element != null ) {
            Element elem = XMLTools.getRequiredElement( element, "cntxt:OnlineResource",
                                                        CommonNamespaces.getNamespaceContext() );
            Attr attr = elem.getAttributeNodeNS( CommonNamespaces.XLNNS.toASCIIString(), "href" );
            String url = attr.getValue();
            URL u = xml.resolve( url );
            url = u.toExternalForm();
            if ( url.endsWith( "?" ) ) {
                url = url.substring( 0, url.length() - 1 );
            }
            attr.setNodeValue( url );
            URL onlineResource = createOnlineResource( elem );
            mc = new ModuleConfiguration( onlineResource );
        }

        return mc;
    }

    /**
     * creates an instance of a class encapsulating the layer list informations
     * 
     * @param element
     *            <LayerList>
     * 
     * @return instance of <tt>LayerList</tt>
     * 
     * @throws XMLParsingException
     */
    private static ParameterList createParameterList( Element element )
                            throws XMLParsingException {

        ParameterList parameterList = new ParameterList();
        if ( element != null ) {
            ElementList el = XMLTools.getChildElements( "Parameter", CommonNamespaces.DGCNTXTNS, element );
            for ( int i = 0; i < el.getLength(); i++ ) {
                Parameter parameter = createParameter( el.item( i ) );
                parameterList.addParameter( parameter );
            }
        }

        return parameterList;
    }

    /**
     * creates an instance of a class encapsulating a parameter that shall be passed to a module
     * 
     * @param element
     *            <Parameter>
     * 
     * @return instance of <tt>Parameter</tt>
     * 
     * @throws XMLParsingException
     */
    private static Parameter createParameter( Element element )
                            throws XMLParsingException {

        String name = XMLTools.getRequiredStringValue( "Name", CommonNamespaces.DGCNTXTNS, element );
        String value = XMLTools.getRequiredStringValue( "Value", CommonNamespaces.DGCNTXTNS, element );
        // Parameter param = new Parameter_Impl( name+":"+value, value );
        Parameter param = new Parameter( name, value );

        return param;
    }

    /**
     * creates an instance of a class encapsulating informations about controlling options for a map presented to the
     * user
     * 
     * @param element
     *            <MapParameter>
     * 
     * @return instance of <tt>MapParameter</tt>
     * 
     * @throws XMLParsingException
     */
    private static MapParameter createMapParameter( Element element )
                            throws XMLParsingException {

        // <OfferedInfoFormats>
        Element elem = XMLTools.getChildElement( "OfferedInfoFormats", CommonNamespaces.DGCNTXTNS, element );
        Format[] infoFormats = null;
        if ( elem != null ) {
            infoFormats = createOfferedInfoFormats( elem );
        }
        // <OfferedZoomFactor>
        elem = XMLTools.getChildElement( "OfferedZoomFactor", CommonNamespaces.DGCNTXTNS, element );
        MapOperationFactor[] zoomFactors = null;
        if ( elem != null ) {
            zoomFactors = createOfferedMapOperationFactors( elem );
        }
        // <OfferedPanFactor>
        elem = XMLTools.getChildElement( "OfferedPanFactor", CommonNamespaces.DGCNTXTNS, element );
        MapOperationFactor[] panFactors = null;
        if ( elem != null ) {
            panFactors = createOfferedMapOperationFactors( elem );
        }
        // <MinScale>
        String tmp = XMLTools.getStringValue( "MinScale", CommonNamespaces.DGCNTXTNS, element, "0" );
        double minScale = Double.parseDouble( tmp );
        // <MaxScale>
        tmp = XMLTools.getStringValue( "MaxScale", CommonNamespaces.DGCNTXTNS, element, "9999999999" );
        double maxScale = Double.parseDouble( tmp );

        MapParameter mp = new MapParameter( infoFormats, panFactors, zoomFactors, minScale, maxScale );

        return mp;
    }

    /**
     * Creates a list of the feature info formats offered by the client.
     * 
     * @param element
     *            <OfferedInfoFormats> element of the configuration
     * 
     * @return list of offered feature info formats
     * 
     * @throws XMLParsingException
     */
    private static Format[] createOfferedInfoFormats( Element element )
                            throws XMLParsingException {

        Format[] format = null;

        // get list of offered feature info formats
        ElementList el = XMLTools.getChildElements( "Format", CommonNamespaces.DGCNTXTNS, element );

        format = new Format[el.getLength()];

        for ( int i = 0; i < el.getLength(); i++ ) {
            String name = XMLTools.getStringValue( el.item( i ) );
            String sel = XMLTools.getAttrValue( el.item( i ), null, "selected", null );

            boolean selected = "1".equals( sel ) || "true".equals( sel );
            try {
                format[i] = new Format( name, selected );
            } catch ( ContextException e ) {
                throw new XMLParsingException( "", e );
            }
        }

        return format;
    }

    /**
     * returns a list of offered numerical map operation factors that can be used to determine zoom or pan levels
     * 
     * @param element
     *            a <tt>Element</tt> that contains <Factor> elements as children
     * 
     * @return list of <tt>MapOperationFactor</tt>s
     * 
     */
    private static MapOperationFactor[] createOfferedMapOperationFactors( Element element ) {

        // get list of offered factors
        ElementList el = XMLTools.getChildElements( "Factor", CommonNamespaces.DGCNTXTNS, element );

        MapOperationFactor[] mof = new MapOperationFactor[el.getLength()];

        for ( int i = 0; i < el.getLength(); i++ ) {
            boolean free = true;
            String tmp = XMLTools.getStringValue( el.item( i ) );
            double fac = -99;

            if ( !tmp.equals( "*" ) ) {
                free = false;
                fac = Double.parseDouble( tmp );
            }

            String sel = XMLTools.getAttrValue( el.item( i ), null, "selected", null );
            boolean selected = "1".equals( sel ) || "true".equals( sel );
            mof[i] = new MapOperationFactor( fac, selected, free );
        }

        return mof;
    }

    /**
     * creates an instance of a class encapsulating the IO setting informations
     * 
     * @param element
     * @param xml
     * @return the iosettings.
     * @throws XMLParsingException
     */
    private static IOSettings createIOSettings( Element element, XMLFragment xml )
                            throws XMLParsingException, MalformedURLException {

        // temp directory
        Element elem = XMLTools.getChildElement( "TempDirectory", CommonNamespaces.DGCNTXTNS, element );
        DirectoryAccess temp = null;
        if ( elem != null ) {
            temp = createDirectoryAccess( elem, null, xml );
        }
        // download directory
        elem = XMLTools.getChildElement( "DownloadDirectory", CommonNamespaces.DGCNTXTNS, element );
        DirectoryAccess download = null;
        if ( elem != null ) {
            download = createDirectoryAccess( elem, temp, xml );
        }
        if ( temp == null && elem == null ) {
            throw new XMLParsingException( "If <TempDirectory> isn't set, " + "downloaddirectory must be set!" );
        }
        // SLD directory
        elem = XMLTools.getChildElement( "SLDDirectory", CommonNamespaces.DGCNTXTNS, element );
        DirectoryAccess sld = null;
        if ( elem != null ) {
            sld = createDirectoryAccess( elem, temp, xml );
        }
        if ( temp == null && elem == null ) {
            throw new XMLParsingException( "If <TempDirectory> isn't set, " + "slddirectory must be set!" );
        }
        // Print directory
        elem = XMLTools.getChildElement( "PrintDirectory", CommonNamespaces.DGCNTXTNS, element );
        DirectoryAccess print = null;
        if ( elem != null ) {
            print = createDirectoryAccess( elem, temp, xml );
        }
        if ( temp == null && elem == null ) {
            throw new XMLParsingException( "If <TempDirectory> isn't set, " + "printdirectory must be set!" );
        }

        IOSettings ioSettings = new IOSettings( download, sld, print, temp );

        return ioSettings;
    }

    /**
     * @param element
     * @param tempDir
     * @return the directory access.
     * @throws XMLParsingException
     */
    private static DirectoryAccess createDirectoryAccess( Element element, DirectoryAccess tempDir, XMLFragment xml )
                            throws XMLParsingException, MalformedURLException {

        // directory name
        String name = XMLTools.getStringValue( "Name", CommonNamespaces.DGCNTXTNS, element, null );
        name = xml.resolve( name ).toExternalForm();

        URL url = null;
        Element elem = XMLTools.getChildElement( "Access", CommonNamespaces.DGCNTXTNS, element );
        if ( elem != null ) {
            elem = XMLTools.getRequiredChildElement( "OnlineResource", CommonNamespaces.CNTXTNS, elem );
            url = createOnlineResource( elem );
        }

        DirectoryAccess da = null;
        if ( name == null || url == null ) {
            da = tempDir;
        } else {
            da = new DirectoryAccess( xml.resolve( name ).toExternalForm(), url );
        }

        return da;

    }

    /**
     * creates an instance of a class encapsulating the layer list informations
     * 
     * @param element
     *            <LayerList>
     * @param user
     * @param sessionID
     * 
     * @return instance of <tt>LayerList</tt>
     * 
     * @throws XMLParsingException
     */
    private static LayerList createLayerList( Element element, User user, String sessionID )
                            throws XMLParsingException {

        ElementList el = XMLTools.getChildElements( "Layer", CommonNamespaces.CNTXTNS, element );
        Layer[] layers = new Layer[el.getLength()];
        for ( int i = 0; i < layers.length; i++ ) {
            Layer layer = createLayer( el.item( i ), user, sessionID );
            layers[i] = layer;

        }
        LayerList list = new LayerList( layers );

        return list;
    }

    /**
     * creates an instance of a class encapsulating a web map context layer's attributes
     * 
     * @param element
     *            <Layer>
     * @param user
     * @param sessionID
     * 
     * @return instance of <tt>Layer</tt>
     * 
     * @throws XMLParsingException
     */
    private static Layer createLayer( Element element, User user, String sessionID )
                            throws XMLParsingException {

        String tmp = XMLTools.getRequiredAttrValue( "queryable", null, element );
        boolean queryable = "1".equals( tmp ) || "true".equals( tmp );
        tmp = XMLTools.getRequiredAttrValue( "hidden", null, element );
        boolean hidden = "1".equals( tmp ) || "true".equals( tmp );

        // <Name>
        String name = XMLTools.getRequiredStringValue( "Name", CommonNamespaces.CNTXTNS, element );
        // <Title>
        String title = XMLTools.getRequiredStringValue( "Title", CommonNamespaces.CNTXTNS, element );
        // <Abstract>
        String abstract_ = XMLTools.getStringValue( "Abstract", CommonNamespaces.CNTXTNS, element, null );
        // <DataURL>
        Element elem = XMLTools.getChildElement( "DataURL", CommonNamespaces.CNTXTNS, element );
        BaseURL dataURL = createBaseURL( elem );
        // <MetaDataURL>
        elem = XMLTools.getChildElement( "MetadataURL", CommonNamespaces.CNTXTNS, element );
        BaseURL metadataURL = createBaseURL( elem );
        // <SRS>
        tmp = XMLTools.getStringValue( "SRS", CommonNamespaces.CNTXTNS, element, null );
        String[] srs = StringTools.toArray( tmp, ",; ", true );
        // <FormatList>
        elem = XMLTools.getChildElement( "FormatList", CommonNamespaces.CNTXTNS, element );
        FormatList formatList = createFormatList( elem );
        // <Extension>
        Element extElem = XMLTools.getChildElement( "Extension", CommonNamespaces.CNTXTNS, element );
        LayerExtension extension = createLayerExtension( extElem, user, sessionID );
        // <Server>
        elem = XMLTools.getRequiredChildElement( "Server", CommonNamespaces.CNTXTNS, element );
        Server server = createServer( elem, user, sessionID, extension.getAuthentication() );

        // try setting metadataURl again. this time from server.
        if ( metadataURL == null ) {
            metadataURL = createMetadataURL( name, server );
        }

        // TODO must be removed, if reading capabilities from remote WMS is too slow
        setScaleHint( extElem, name, extension, server, user, sessionID, extension.getAuthentication() );

        // <StyleList>
        elem = XMLTools.getChildElement( "StyleList", CommonNamespaces.CNTXTNS, element );
        StyleList styleList = createStyleList( elem, name, server );

        Layer layer = null;
        try {
            layer = new Layer( server, name, title, abstract_, srs, dataURL, metadataURL, formatList, styleList,
                               queryable, hidden, extension );
        } catch ( Exception e ) {
            throw new XMLParsingException( "couldn't create map context layer", e );
        }

        if ( layer.getServer().getCapabilities() == null ) {
            LOG.logInfo( "set status of  layer: " + name + " to not valid because capabilities could be read." );
            layer.getExtension().setValid( false );
        }
        return layer;
    }

    /**
     * @param layerName
     *            the layer name from which to take the MetadataURL information
     * @param server
     *            the WMS server from which to read the layer information (WMS capabilities document)
     * @return only the first MetadataURL from the WMS capabilities for the given layer
     * @throws XMLParsingException
     */
    private static BaseURL createMetadataURL( String layerName, Server server )
                            throws XMLParsingException {

        WMSCapabilities capa = (WMSCapabilities) server.getCapabilities();
        BaseURL metaURL = null;

        if ( capa != null ) {
            org.deegree.ogcwebservices.wms.capabilities.Layer layer = capa.getLayer( layerName );
            if ( layer != null ) {
                MetadataURL[] urls = layer.getMetadataURL();
                if ( urls != null && urls.length > 0 ) {
                    metaURL = urls[0];
                }
            } else {
                if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                    String msg = StringTools.concat( 500, "LayerName '", layerName,
                                                     "' does not exist in the WMSCapabilities of server ",
                                                     server.getOnlineResource() );
                    LOG.logDebug( msg );
                }
            }
        }
        return metaURL;
    }

    /**
     * creates an instance of a class encapsulating informations about the server (service) a layer based on
     * 
     * @param element
     *            <Server>
     * @param user
     * @param sessionID
     * @param useAuthentication
     * 
     * @return instance of <tt>Server</tt>
     * 
     * @throws XMLParsingException
     */
    private static Server createServer( Element element, User user, String sessionID, int useAuthentication )
                            throws XMLParsingException {

        String service = XMLTools.getRequiredAttrValue( "service", null, element );
        String version = XMLTools.getRequiredAttrValue( "version", null, element );
        String title = XMLTools.getRequiredAttrValue( "title", null, element );

        // <OnlineResource>
        Element elem = XMLTools.getRequiredChildElement( "OnlineResource", CommonNamespaces.CNTXTNS, element );
        URL onlineResource = createOnlineResource( elem );

        OGCCapabilities capabilities = getCapabilities( onlineResource, service, version, user, sessionID,
                                                        useAuthentication );

        Server server = null;
        try {
            server = new Server( title, version, service, onlineResource, capabilities );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLParsingException( "could not create context server", e );
        }

        return server;
    }

    /**
     * creates an instance of a class encapsulating a list of image formats a layer offers
     * 
     * @param element
     *            <FormatList>
     * 
     * @return instance of <tt>FormatList</tt>
     * 
     * @throws XMLParsingException
     */
    private static FormatList createFormatList( Element element )
                            throws XMLParsingException {

        ElementList el = XMLTools.getChildElements( "Format", CommonNamespaces.CNTXTNS, element );
        Format[] formats = new Format[el.getLength()];
        for ( int i = 0; i < formats.length; i++ ) {
            String name = XMLTools.getStringValue( el.item( i ) );
            String tmp = XMLTools.getAttrValue( el.item( i ), null, "current", null );
            boolean current = "1".equals( tmp ) || "true".equals( tmp ) || formats.length == 1;
            try {
                formats[i] = new Format( name, current );
            } catch ( Exception e ) {
                throw new XMLParsingException( "could not create context format", e );
            }
        }

        FormatList formatList = null;
        try {
            formatList = new FormatList( formats );
        } catch ( Exception e ) {
            LOG.logDebug( e.getMessage(), e );
            throw new XMLParsingException( "could not create context formatList", e );
        }

        return formatList;
    }

    /**
     * creates an instance of a class encapsulating a list of styles available for a layer
     * 
     * @param element
     *            <StyleList>
     * @param layerName
     * @param server
     * 
     * @return instance of <tt>StyleList</tt>
     * 
     * @throws XMLParsingException
     */
    private static StyleList createStyleList( Element element, String layerName, Server server )
                            throws XMLParsingException {

        ElementList el = XMLTools.getChildElements( "Style", CommonNamespaces.CNTXTNS, element );
        Style[] styles = new Style[el.getLength()];
        boolean current = false;
        for ( int i = 0; i < styles.length; i++ ) {
            styles[i] = createStyle( el.item( i ), layerName, server );
            if ( styles[i].isCurrent() ) {
                current = true;
            }
        }
        if ( !current ) {
            styles[0].setCurrent( true );
            LOG.logWarning( "not style for layer: " + layerName
                            + " has been defined as current -> first style has been marked as current" );
        }
        StyleList styleList = null;
        try {
            styleList = new StyleList( styles );
        } catch ( Exception e ) {
            LOG.logDebug( e.getMessage(), e );
            throw new XMLParsingException( "could not create context stylelist", e );
        }

        return styleList;
    }

    /**
     * creates an instance of a class encapsulating a description of a Style
     * 
     * @param element
     *            <Style>
     * 
     * @return instance of <tt>Style</tt>
     * 
     * @throws XMLParsingException
     */
    private static Style createStyle( Element element, String layerName, Server server )
                            throws XMLParsingException {

        Style style = null;

        String tmp = XMLTools.getAttrValue( element, null, "current", null );
        boolean current = "1".equals( tmp ) || "true".equals( tmp );

        Element elem = XMLTools.getChildElement( "SLD", CommonNamespaces.CNTXTNS, element );
        if ( elem != null ) {
            SLD sld = createSLD( elem );
            try {
                style = new Style( sld, current );
            } catch ( Exception e ) {
                throw new XMLParsingException( "could not create context style", e );
            }
        } else {
            String name = XMLTools.getRequiredStringValue( "Name", CommonNamespaces.CNTXTNS, element );
            String title = XMLTools.getRequiredStringValue( "Title", CommonNamespaces.CNTXTNS, element );
            String abstract_ = XMLTools.getStringValue( "Abstract", CommonNamespaces.CNTXTNS, element, null );
            // <LegendURL>
            elem = XMLTools.getChildElement( "LegendURL", CommonNamespaces.CNTXTNS, element );
            ImageURL legendURL = null;
            if ( elem != null ) {
                legendURL = createImageURL( elem );
            } else {
                try {
                    legendURL = createLegendURL( name, layerName, server );
                } catch ( Exception e ) {
                    throw new XMLParsingException( "could not create context style", e );
                }
            }
            try {
                style = new Style( name, title, abstract_, legendURL, current );
            } catch ( Exception e ) {
                LOG.logDebug( e.getMessage(), e );
                throw new XMLParsingException( "could not create context style", e );
            }
        }

        return style;
    }

    /**
     * creates a legendURL for a style/layer by evaluating the servers capabilities the layer is servered by.
     * 
     * @return the image url.
     * @throws XMLParsingException
     */
    private static ImageURL createLegendURL( String styleName, String layerName, Server server )
                            throws XMLParsingException {

        WMSCapabilities capa = (WMSCapabilities) server.getCapabilities();
        ImageURL legendURL = null;
        if ( capa != null ) {
            org.deegree.ogcwebservices.wms.capabilities.Layer layer = capa.getLayer( layerName );
            // null layer will produce NullPointerException@layer.getStyles()
            if ( layer == null ) {
                LOG.logDebug( "LayerName: " + layerName + " does not exist in the WMSCapabilities" );
            }
            // if layer is null, no legend is needed!
            if ( layer != null ) {
                org.deegree.ogcwebservices.wms.capabilities.Style[] styles = layer.getStyles();
                org.deegree.ogcwebservices.wms.capabilities.Style style = null;
                for ( int i = 0; i < styles.length; i++ ) {
                    // find responsible style definition
                    style = styles[i];
                    if ( style.getName().equals( styleName ) ) {
                        break;
                    }
                }

                LegendURL[] urls = null;

                if ( style != null ) {
                    urls = style.getLegendURL();
                }

                if ( urls != null && urls.length > 0 && urls[0] != null ) {
                    // if style has defined LegendURL(s) take the first
                    legendURL = new ImageURL( urls[0].getWidth(), urls[0].getHeight(), urls[0].getFormat(),
                                              urls[0].getOnlineResource() );
                } else {
                    // create a GetLegendGraphic request as style URL if the server
                    // supports GetLegendGraphic operation
                    OperationsMetadata om = capa.getOperationMetadata();
                    Operation operation = om.getOperation( new QualifiedName( "GetLegendGraphic" ) );
                    if ( operation != null ) {
                        HTTP http = (HTTP) operation.getDCP().get( 0 );
                        URL url = http.getGetOnlineResources().get( 0 );
                        StringBuffer sb = new StringBuffer( 500 );
                        sb.append( OWSUtils.validateHTTPGetBaseURL( url.toExternalForm() ) );
                        GetLegendGraphic glg = GetLegendGraphic.create( "12", capa.getVersion(), layerName, styleName,
                                                                        null, null, 1, null, null, "image/jpeg", 20,
                                                                        20, null, null );
                        try {
                            sb.append( glg.getRequestParameter() );
                            url = new URL( sb.toString() );
                            legendURL = new ImageURL( 20, 20, "image/jpeg", url );
                        } catch ( Exception shouldNeverHappen ) {
                            shouldNeverHappen.printStackTrace();
                        }
                    }
                }
            }
        }

        return legendURL;
    }

    /**
     * creates an instance of a class encapsulating a description of a Style based on a SLD
     * 
     * @param element
     *            <SLD>
     * 
     * @return instance of <tt>SLD</tt>
     * 
     * @throws XMLParsingException
     */
    private static SLD createSLD( Element element )
                            throws XMLParsingException {

        SLD sld = null;

        String name = XMLTools.getRequiredStringValue( "Name", CommonNamespaces.CNTXTNS, element );
        String title = XMLTools.getStringValue( "Title", CommonNamespaces.CNTXTNS, element, null );

        Element elem = XMLTools.getChildElement( "OnlineResource", CommonNamespaces.CNTXTNS, element );
        try {
            if ( elem != null ) {
                URL onlineResource = createOnlineResource( elem );
                sld = new SLD( name, title, onlineResource );
            } else {
                elem = XMLTools.getChildElement( "StyledLayerDescriptor", CommonNamespaces.SLDNS, element );
                if ( elem != null ) {
                    XMLFragment xml = new XMLFragment();
                    xml.setRootElement( elem );
                    StyledLayerDescriptor styledLayerDescriptor = SLDFactory.createSLD( xml );
                    sld = new SLD( name, title, styledLayerDescriptor );
                } else {
                    FeatureTypeStyle fts = SLDFactory.createFeatureTypeStyle( elem );
                    sld = new SLD( name, title, fts );
                }
            }
        } catch ( Exception e ) {
            LOG.logDebug( e.getMessage(), e );
            throw new XMLParsingException( "couldn't create map context SLD", e );
        }

        return sld;
    }

    /**
     * creates an instance of a class encapsulating the deegree specific extensions of a Layer.
     * 
     * Extensions are: DataService, MasterLayer, SelectedForQuery, parentNodeId, showLegendGraphic, identifier,
     * ScaleHint.
     * 
     * @param element
     *            Extension
     * @param user
     * @param sessionID
     * 
     * @return instance of <tt>LayerExtension</tt>
     * 
     * @throws XMLParsingException
     */
    private static LayerExtension createLayerExtension( Element element, User user, String sessionID )
                            throws XMLParsingException {

        LayerExtension le = null;
        if ( element != null ) {

            String tmp = XMLTools.getNodeAsString( element, "./dgcntxt:UseAuthentication/text()",
                                                   CommonNamespaces.getNamespaceContext(), "NONE" );
            int ua = LayerExtension.NONE;
            if ( "sessionID".equalsIgnoreCase( tmp ) ) {
                ua = LayerExtension.SESSIONID;
            } else if ( "user/password".equalsIgnoreCase( tmp ) ) {
                ua = LayerExtension.USERPASSWORD;
            }

            // standard edition: download WFS data
            DataService dataService = null;
            Element elem = XMLTools.getChildElement( "DataService", CommonNamespaces.DGCNTXTNS, element );
            if ( elem != null ) {
                Element el = XMLTools.getRequiredChildElement( "Server", CommonNamespaces.CNTXTNS, elem );
                Server server = createServer( el, user, sessionID, ua );
                String geoType = XMLTools.getStringValue( "GeometryType", CommonNamespaces.DGCNTXTNS, elem, null );
                String featureType = XMLTools.getStringValue( "FeatureType", CommonNamespaces.DGCNTXTNS, elem, null );
                dataService = new DataService( server, featureType, geoType );
            }

            // project specific
            boolean masterLayer = false;
            elem = XMLTools.getChildElement( "MasterLayer", CommonNamespaces.DGCNTXTNS, element );
            if ( elem != null ) {
                String s = XMLTools.getStringValue( elem );
                masterLayer = "true".equals( s ) || "1".equals( s );
            }

            // project specific
            boolean selectedForFI = false;
            elem = XMLTools.getChildElement( "SelectedForQuery", CommonNamespaces.DGCNTXTNS, element );
            if ( elem != null ) {
                String s = XMLTools.getStringValue( elem );
                selectedForFI = "true".equalsIgnoreCase( s ) || "1".equals( s );
            }

            // portlet edition: LayerTree
            int parentNodeId = 1;
            elem = XMLTools.getChildElement( "parentNodeId", CommonNamespaces.DGCNTXTNS, element );
            if ( elem != null ) {
                parentNodeId = Integer.parseInt( XMLTools.getStringValue( elem ) );
            }

            // 
            boolean showLegendGraphic = false;
            elem = XMLTools.getChildElement( "showLegendGraphic", CommonNamespaces.DGCNTXTNS, element );
            if ( elem != null ) {
                String s = XMLTools.getStringValue( elem );
                showLegendGraphic = "true".equalsIgnoreCase( s ) || "1".equals( s );
            }

            boolean tiled = false;
            elem = XMLTools.getChildElement( "tiled", CommonNamespaces.DGCNTXTNS, element );
            if ( elem != null ) {
                String s = XMLTools.getStringValue( elem );
                tiled = "true".equalsIgnoreCase( s ) || "1".equals( s );
            }

            // standard edition: LayerTree
            String identifier = null;
            elem = XMLTools.getChildElement( "identifier", CommonNamespaces.DGCNTXTNS, element );
            if ( elem != null ) {
                identifier = XMLTools.getStringValue( elem );
            }

            // standard edition: ScaleHint
            String scaleHint = null;
            elem = XMLTools.getChildElement( "ScaleHint", CommonNamespaces.DGCNTXTNS, element );
            double minScale = 0;
            double maxScale = Double.MAX_VALUE;
            if ( elem != null ) {
                minScale = Double.valueOf( XMLTools.getRequiredAttrValue( "min", null, elem ) );
                maxScale = Double.valueOf( XMLTools.getRequiredAttrValue( "max", null, elem ) );
            }

            le = new LayerExtension( dataService, masterLayer, minScale, maxScale, selectedForFI, ua, parentNodeId,
                                     showLegendGraphic, identifier, tiled );

        } else {
            le = new LayerExtension();
        }

        return le;
    }

    /**
     * Use information form extension and server elements of a layer to set a scaleHint.
     * 
     * If layer extension contains scale hints (min, max), then these values are used. Otherwise, the information is
     * taken from the WMS capabilities by using {@link #getScaleHintFromCapabilities(Server, String, User, String, int)}
     * 
     * @param extElem
     * @param name
     * @param extension
     * @param server
     * @param user
     * @param sessionID
     * @param useAuthentication
     * @throws XMLParsingException
     */
    private static void setScaleHint( Element extElem, String name, LayerExtension extension, Server server, User user,
                                      String sessionID, int useAuthentication )
                            throws XMLParsingException {

        // <deegree:ScaleHint min="0.0" max="41.0"/>
        Element elem = null;
        if ( extElem != null ) {
            elem = XMLTools.getChildElement( "ScaleHint", CommonNamespaces.DGCNTXTNS, extElem );
        }

        if ( elem == null ) {
            // reade scaleHint from WMS Capabilities
            double[] sc = getScaleHintFromCapabilities( server, name, user, sessionID, useAuthentication );
            extension.setMinScaleHint( sc[0] );
            extension.setMaxScaleHint( sc[1] );
        } else {
            // do nothing. Values have already been read from layer extension when creating the LayerExtension object.
        }
    }

    /**
     * taken from d1 and adjusted for d2.
     * 
     * TODO: check if inconsistencies arise regarding user-dependant scaleHint, because of using {@link #wmsCache}.
     * 
     * @param server
     * @param layerName
     * @param user
     * @param sessionID
     * @param useAuthentication
     * @return
     */
    private static double[] getScaleHintFromCapabilities( Server server, String layerName, User user, String sessionID,
                                                          int useAuthentication ) {

        double[] sc = new double[] { 0, Double.MAX_VALUE };
        WMSCapabilities capa = null;

        // capabilities have not been stored in cache before. getting the capabilities for the specified user
        if ( wmsCache.get( server.getOnlineResource() ) == null ) {
            capa = (WMSCapabilities) getCapabilities( server.getOnlineResource(), server.getService(),
                                                      server.getVersion(), user, sessionID, useAuthentication );
        } else {
            // get capabilities from cache.
            // May cause an inconsistency problem, if cache contains capabilities of a different but the specified user.
            // Could be handled with different caches for different users, but this would cause performance problems.
            capa = (WMSCapabilities) wmsCache.get( server.getOnlineResource() );
        }
        if ( capa != null ) {
            wmsCache.put( server.getOnlineResource(), capa );
            org.deegree.ogcwebservices.wms.capabilities.Layer lay = capa.getLayer( layerName );
            if ( lay != null ) {
                try {
                    sc[0] = lay.getScaleHint().getMin();
                    sc[1] = lay.getScaleHint().getMax();
                } catch ( Exception e ) {
                }
            }
        }
        return sc;
    }

    /**
     * returns the capabilities for the passed server. The capabilities are read directly from the server or from the
     * loacal cache.
     * 
     * @param url
     * @param service
     * @param version
     * @param user
     * @param sessionID
     * @param useAuthentication
     * @return he capabilities for the passed server. The capabilities are read directly from the server or from the
     *         loacal cache.
     */
    private static OGCCapabilities getCapabilities( URL url, String service, String version, User user,
                                                    String sessionID, int useAuthentication ) {

        OGCCapabilities capa = null;

        String href = OWSUtils.validateHTTPGetBaseURL( url.toExternalForm() );
        if ( href.toLowerCase().startsWith( "http://" ) || href.toLowerCase().startsWith( "https://" ) ) {
            if ( "OGC:WMS".equals( service ) ) {
                // is a HTTP URL so GetCapabilities request must be constructed
                if ( "1.0.0".equals( version ) ) {
                    href = StringTools.concat( 1000, href, "request=capabilities&service=WMS", "&WMTVER=", version );
                } else {
                    href = StringTools.concat( 1000, href, "request=GetCapabilities&service=WMS", "&version=", version );
                }

            } else if ( "OGC:WFS".equals( service ) ) {
                // is a HTTP URL so GetCapabilities request must be constructed
                href = StringTools.concat( 1000, href, "request=GetCapabilities&service=WFS", "&version=", version );

            } else if ( "OGC:WCS".equals( service ) ) {
                // is a HTTP URL so GetCapabilities request must be constructed
                href = StringTools.concat( 1000, href, "request=GetCapabilities&service=WCS", "&version=", version );

            }

            if ( useAuthentication == LayerExtension.SESSIONID ) {
                href = StringTools.concat( 1000, href, "&sessionID=", sessionID );
            } else if ( useAuthentication == LayerExtension.USERPASSWORD ) {
                href = StringTools.concat( 1000, href, "&user=", user.getName(), "&password=", user.getPassword() );
            }
        }

        if ( wmsCache.get( url ) == null && "OGC:WMS".equals( service ) ) {
            LOG.logDebug( "get " + service + " capabilities from GetCapabilities request" );
            capa = parseCapabilities( href, service );
            if ( capa != null ) {
                // write capabilities into local cache
                wmsCache.put( url, (WMSCapabilities) capa );
            }
        } else if ( "OGC:WMS".equals( service ) ) {
            LOG.logDebug( "get WMS capabilities from cache" );
            capa = wmsCache.get( url );
        } else if ( wfsCache.get( url ) == null && "OGC:WFS".equals( service ) ) {
            LOG.logDebug( "get " + service + " capabilities from GetCapabilities request" );
            capa = parseCapabilities( href, service );
            if ( capa != null ) {
                // write capabilities into local cache
                wfsCache.put( url, (WFSCapabilities) capa );
            }
        } else if ( "OGC:WFS".equals( service ) ) {
            LOG.logDebug( "get WFS capabilities from cache" );
            capa = wfsCache.get( url );
        } else if ( wcsCache.get( url ) == null && "OGC:WCS".equals( service ) ) {
            LOG.logDebug( "get " + service + " capabilities from GetCapabilities request" );
            capa = parseCapabilities( href, service );
            if ( capa != null ) {
                // write capabilities into local cache
                wcsCache.put( url, (WCSCapabilities) capa );
            }
        } else if ( "OGC:WFS".equals( service ) ) {
            LOG.logDebug( "get WCS capabilities from cache" );
            capa = wcsCache.get( url );
        }

        return capa;
    }

    /**
     * 
     * @param href
     * @param service
     * @return the capabilities bean.
     */
    private static OGCCapabilities parseCapabilities( String href, String service ) {

        OGCCapabilities capa = null;
        try {
            URL url = null;
            Reader reader = null;

            // consider that the reference to the capabilities may has been
            // made by a file URL to a local copy
            if ( href.toLowerCase().startsWith( "http://" ) || href.toLowerCase().startsWith( "https://" ) ) {
                HttpClient httpclient = new HttpClient();
                httpclient = WebUtils.enableProxyUsage( httpclient, new URL( href ) );
                int timeout = Integer.parseInt( Messages.getString( "WebMapContextFactory.timeout" ) );
                httpclient.getHttpConnectionManager().getParams().setSoTimeout( timeout );

                GetMethod httpget = new GetMethod( href );
                LOG.logDebug( "GetCapabilities: ", href );

                httpclient.executeMethod( httpget );
                reader = new InputStreamReader( httpget.getResponseBodyAsStream() );
            } else {
                if ( href.endsWith( "?" ) ) {
                    url = new URL( href.substring( 0, href.length() - 1 ) );
                }
                reader = new InputStreamReader( url.openStream() );
            }

            OGCCapabilitiesDocument doc = null;
            if ( "OGC:WMS".equals( service ) ) {
                doc = new WMSCapabilitiesDocument();
                doc.load( reader, XMLFragment.DEFAULT_URL );
                doc = WMSCapabilitiesDocumentFactory.getWMSCapabilitiesDocument( doc.getRootElement() );
            } else if ( "OGC:WFS".equals( service ) ) {
                doc = new WFSCapabilitiesDocument();
                doc.load( reader, XMLFragment.DEFAULT_URL );
            } else if ( "OGC:WCS".equals( service ) ) {
                doc = new WCSCapabilitiesDocument();
                doc.load( reader, XMLFragment.DEFAULT_URL );
            } else {
                throw new XMLParsingException( "not supported service type: " + service );
            }

            capa = doc.parseCapabilities();
        } catch ( Exception e ) {
            LOG.logWarning( "could not read capabilities: " + href );
            // LOG.logError( e.getMessage(), e );
            return null;
        }
        return capa;
    }

    // /**
    // * @param mapServer
    // * @param layer
    // * @return ScaleHint
    // */
    // private static double[] getScaleHintFromCapabilities( Server mapServer, String layer ) {
    //
    // double[] sc = new double[] { 0, 9999999 };
    // WMSCapabilities capa = (WMSCapabilities) mapServer.getCapabilities();
    // if ( capa != null ) {
    // org.deegree.ogcwebservices.wms.capabilities.Layer lay = capa.getLayer( layer );
    // if ( lay != null ) {
    // sc[0] = lay.getScaleHint().getMin();
    // sc[1] = lay.getScaleHint().getMax();
    // }
    // }
    // return sc;
    // }

    /**
     * creates a list (String[]) containing the name of the JavaScript files used by the moudle
     * 
     * @param element
     *            <Module>
     * 
     * @return instance of <tt>String[]</tt>
     * 
     */
    private static String[] createModuleJSList( Element element ) {
        String[] moduleJS = null;
        if ( element != null ) {
            ElementList el = XMLTools.getChildElements( "ModuleJS", CommonNamespaces.DGCNTXTNS, element );
            moduleJS = new String[el.getLength()];
            for ( int i = 0; i < el.getLength(); i++ ) {
                moduleJS[i] = ( (Text) el.item( i ).getFirstChild() ).getData();
            }
        }

        return moduleJS;
    }
}
