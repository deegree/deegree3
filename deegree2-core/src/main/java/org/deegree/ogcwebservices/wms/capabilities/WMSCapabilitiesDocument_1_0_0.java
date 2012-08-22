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
package org.deegree.ogcwebservices.wms.capabilities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.deegree.datatypes.Code;
import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.metadata.iso19115.Constraints;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.metadata.iso19115.Linkage;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.owscommon_new.DCP;
import org.deegree.owscommon_new.DomainType;
import org.deegree.owscommon_new.HTTP;
import org.deegree.owscommon_new.Operation;
import org.deegree.owscommon_new.OperationsMetadata;
import org.deegree.owscommon_new.Parameter;
import org.deegree.owscommon_new.ServiceIdentification;
import org.deegree.owscommon_new.ServiceProvider;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <code>WMSCapabilitiesDocument</code> is the parser class for WMS capabilities documents that
 * uses the new OWS common classes to encapsulate the data.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class WMSCapabilitiesDocument_1_0_0 extends WMSCapabilitiesDocument {

    private static final long serialVersionUID = 4689978960047737035L;

    private static final String XML_TEMPLATE = "WMSCapabilitiesTemplate.xml";

    private static final ILogger LOG = LoggerFactory.getLogger( WMSCapabilitiesDocument_1_0_0.class );

    /**
     * Creates a skeleton capabilities document that contains the mandatory elements only.
     *
     * @throws IOException
     * @throws SAXException
     */
    @Override
    public void createEmptyDocument()
                            throws IOException, SAXException {

        URL url = WMSCapabilitiesDocument_1_0_0.class.getResource( XML_TEMPLATE );
        if ( url == null ) {
            throw new IOException( "The resource '" + XML_TEMPLATE + " could not be found." );
        }
        load( url );
    }

    /**
     *
     * @param elem
     * @return array of supported exception formats
     * @throws XMLParsingException
     */
    @Override
    protected List<String> parseExceptionFormats( Element elem )
                            throws XMLParsingException {
        List<Node> nodes = XMLTools.getRequiredNodes( elem, "Format", nsContext );
        String[] formats = new String[nodes.size()];
        for ( int i = 0; i < formats.length; i++ ) {
            formats[i] = nodes.get( i ).getLocalName();
        }
        return Arrays.asList( formats );
    }

    /**
     * Creates a class representation of the document.
     *
     * @return class representation of the configuration document
     * @throws InvalidCapabilitiesException
     */
    @Override
    public OGCCapabilities parseCapabilities()
                            throws InvalidCapabilitiesException {
        ServiceIdentification serviceIdentification = null;
        ServiceProvider serviceProvider = null;
        OperationsMetadata metadata = null;
        Layer layer = null;
        List<String> exceptions;

        String updateSeq = parseUpdateSequence();
        try {
            serviceIdentification = parseServiceIdentification();
            metadata = parseOperationsMetadata();

            Element exceptionElement = XMLTools.getRequiredElement( getRootElement(), "Capability/Exception", nsContext );
            exceptions = parseExceptionFormats( exceptionElement );

            Element layerElem = XMLTools.getRequiredElement( getRootElement(), "./Capability/Layer", nsContext );
            layer = parseLayers( layerElem, null, null );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidCapabilitiesException( e.getMessage() );
        } catch ( UnknownCRSException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidCapabilitiesException( e.getMessage() );
        }

        return new WMSCapabilities_1_0_0( updateSeq, serviceIdentification, serviceProvider, metadata, layer,
                                          exceptions );

    }

    /**
     * returns the services indentification read from the WMS capabilities service section
     *
     * @throws XMLParsingException
     */
    @Override
    protected ServiceIdentification parseServiceIdentification()
                            throws XMLParsingException {
        String name = XMLTools.getNodeAsString( getRootElement(), "./Service/Name", nsContext, null );
        String title = XMLTools.getNodeAsString( getRootElement(), "./Service/Title", nsContext, name );
        String serviceAbstract = XMLTools.getNodeAsString( getRootElement(), "./Service/Abstract", nsContext, null );

        String tmp = XMLTools.getNodeAsString( getRootElement(), "./Service/Keywords", nsContext, "" );
        String[] kw = StringTools.toArray( tmp, " ", false );

        Keywords[] keywordArray = new Keywords[] { new Keywords( kw ) };
        List<Keywords> keywords = Arrays.asList( keywordArray );

        String fees = XMLTools.getNodeAsString( getRootElement(), "./Service/Fees", nsContext, null );

        List<Constraints> accessConstraints = new ArrayList<Constraints>();

        String constraints = XMLTools.getNodeAsString( getRootElement(), "./Service/AccessConstraints", nsContext, null );

        if ( constraints != null ) {
            List<String> limits = new ArrayList<String>();
            limits.add( constraints );
            accessConstraints.add( new Constraints( fees, null, null, null, limits, null, null, null ) );
        }

        List<String> versions = new ArrayList<String>();
        versions.add( "1.0.0" );

        return new ServiceIdentification( new Code( "OGC:WMS" ), versions, title, null,
                                          new Date( System.currentTimeMillis() ), title, serviceAbstract, keywords,
                                          accessConstraints );

    }

    /**
     * returns the services capabilitiy read from the WMS capabilities file
     *
     * @return the operations metadata
     * @throws XMLParsingException
     */
    @Override
    protected OperationsMetadata parseOperationsMetadata()
                            throws XMLParsingException {

        Node opNode = XMLTools.getRequiredNode( getRootElement(), "./Capability/Request/Capabilities", nsContext );

        Operation getCapa = parseOperation( opNode );

        opNode = XMLTools.getRequiredNode( getRootElement(), "./Capability/Request/Map", nsContext );

        Operation getMap = parseOperation( opNode );

        Operation getFI = null;
        opNode = XMLTools.getNode( getRootElement(), "./Capability/Request/FeatureInfo", nsContext );
        if ( opNode != null ) {
            getFI = parseOperation( opNode );
        }

        List<Operation> operations = new ArrayList<Operation>();
        if ( getCapa != null )
            operations.add( getCapa );
        if ( getMap != null )
            operations.add( getMap );
        if ( getFI != null )
            operations.add( getFI );

        return new OperationsMetadata( null, null, operations, null );

    }

    /**
     * Creates an <tt>Operation</tt>-instance according to the contents of the DOM-subtree
     * starting at the given <tt>Node</tt>.
     * <p>
     * Notice: operation to be parsed must be operations in sense of WMS 1.0.0 - 1.3.0 and not as
     * defined in OWSCommons. But the method will return an OWSCommon Operation which encapsulates
     * parsed WMS operation
     * <p>
     *
     * @param node
     *            the <tt>Element</tt> that describes an <tt>Operation</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>Operation</tt>-instance
     */
    @Override
    protected Operation parseOperation( Node node )
                            throws XMLParsingException {
        // use node name as name of the Operation to be defined
        String name = node.getNodeName();
        if ( name.equals( "Capabilities" ) ) {
            name = "GetCapabilities";
        } else if ( name.equals( "Map" ) ) {
            name = "GetMap";
        } else if ( name.equals( "FeatureInfo" ) ) {
            name = "GetFeatureInfo";
        }

        List<Node> nodes = XMLTools.getRequiredNodes( node, "./Format", nsContext );

        List<TypedLiteral> values = new ArrayList<TypedLiteral>();

        URI stringURI = null;
        try {
            stringURI = new URI( null, "String", null );
        } catch ( URISyntaxException e ) {
            // cannot happen, why do I have to catch this?
        }

        for ( Node str : nodes )
            values.add( new TypedLiteral( str.getLocalName(), stringURI ) );

        DomainType owsDomainType = new DomainType( false, true, null, 0, new QualifiedName( "Format" ), values, null,
                                                   null, false, null, false, null, null, null, null );
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add( owsDomainType );

        List<Element> nl = XMLTools.getRequiredElements( node, "./DCPType", nsContext );
        List<DCP> dcps = new ArrayList<DCP>();

        for ( Element element : nl ) {
            dcps.add( parseDCP( element ) );
        }

        return new Operation( new QualifiedName( name ), dcps, parameters, null, null, null );
    }

    /**
     * Parses a DCPType element. Does not override the method defined in the base class any more.
     *
     * @param element
     * @return created <code>DCPType</code>
     * @throws XMLParsingException
     * @see org.deegree.ogcwebservices.getcapabilities.OGCStandardCapabilities
     */
    @Override
    protected DCP parseDCP( Element element )
                            throws XMLParsingException {

        List<HTTP.Type> types = new ArrayList<HTTP.Type>();
        List<OnlineResource> links = new ArrayList<OnlineResource>();

        Element elem = XMLTools.getRequiredElement( element, "HTTP", nsContext );
        String s = null;
        try {
            List<Node> nl = XMLTools.getNodes( elem, "Get", nsContext );
            for ( int i = 0; i < nl.size(); i++ ) {
                s = XMLTools.getNodeAsString( nl.get( i ), "./@onlineResource", nsContext, null );
                types.add( HTTP.Type.Get );
                links.add( new OnlineResource( new Linkage( new URL( s ) ) ) );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLParsingException( Messages.getMessage( "WMS_DCPGET", s ) );
        }
        try {
            List<Node> nl = XMLTools.getNodes( elem, "Post", nsContext );

            for ( int i = 0; i < nl.size(); i++ ) {
                s = XMLTools.getNodeAsString( nl.get( i ), "./@onlineResource", nsContext, null );
                types.add( HTTP.Type.Post );
                links.add( new OnlineResource( new Linkage( new URL( s ) ) ) );
            }

        } catch ( MalformedURLException e ) {
            throw new XMLParsingException( Messages.getMessage( "WMS_DCPPOST", s ) );
        }
        return new HTTP( links, null, types );

    }

    /**
     * returns the layers offered by the WMS
     *
     * @return the layer
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    @Override
    protected Layer parseLayers( Element layerElem, Layer parent, ScaleHint scaleHint )
                            throws XMLParsingException, UnknownCRSException {

        boolean queryable = XMLTools.getNodeAsBoolean( layerElem, "./@queryable", nsContext, false );

        int cascaded = 0;
        boolean opaque = false;
        boolean noSubsets = false;
        int fixedWidth = 0;
        int fixedHeight = 0;
        String name = XMLTools.getNodeAsString( layerElem, "./Name", nsContext, null );
        String title = XMLTools.getRequiredNodeAsString( layerElem, "./Title", nsContext );
        String layerAbstract = XMLTools.getNodeAsString( layerElem, "./Abstract", nsContext, null );
        String[] keywords = XMLTools.getNodesAsStrings( layerElem, "./Keywords", nsContext );
        String[] srs = XMLTools.getNodesAsStrings( layerElem, "./SRS", nsContext );

        List<Element> nl = XMLTools.getElements( layerElem, "./BoundingBox", nsContext );
        // TODO
        // substitue with Envelope
        LayerBoundingBox[] bboxes = null;
        if ( nl.size() == 0 && parent != null ) {
            // inherit BoundingBoxes from parent layer
            bboxes = parent.getBoundingBoxes();
        } else {
            bboxes = parseLayerBoundingBoxes( nl );
        }

        Element llBox = XMLTools.getElement( layerElem, "./LatLonBoundingBox", nsContext );
        Envelope llBoundingBox = null;

        if ( llBox == null && parent != null ) {
            // inherit LatLonBoundingBox parent layer
            llBoundingBox = parent.getLatLonBoundingBox();
        } else if ( llBox != null ) {
            llBoundingBox = parseLatLonBoundingBox( llBox );
        } else {
            llBoundingBox = GeometryFactory.createEnvelope( -180, -90, 180, 90, CRSFactory.create( "EPSG:4326" ) );
        }

        DataURL[] dataURLs = parseDataURL( layerElem );

        Style[] styles = parseStyles( layerElem );

        scaleHint = parseScaleHint( layerElem, scaleHint );

        Layer layer = new Layer( queryable, cascaded, opaque, noSubsets, fixedWidth, fixedHeight, name, title,
                                 layerAbstract, llBoundingBox, null, scaleHint, keywords, srs, bboxes, null, null,
                                 null, null, null, dataURLs, null, styles, null, null, parent );

        // get Child layers
        nl = XMLTools.getElements( layerElem, "./Layer", nsContext );
        Layer[] layers = new Layer[nl.size()];
        for ( int i = 0; i < layers.length; i++ ) {
            layers[i] = parseLayers( nl.get( i ), layer, scaleHint );
        }

        // set child layers
        layer.setLayer( layers );

        return layer;
    }

    /**
     *
     * @param layerElem
     * @return the URLs
     * @throws XMLParsingException
     */
    @Override
    protected DataURL[] parseDataURL( Element layerElem )
                            throws XMLParsingException {

        List<Node> nl = XMLTools.getNodes( layerElem, "./DataURL", nsContext );
        DataURL[] dataURL = new DataURL[nl.size()];
        for ( int i = 0; i < dataURL.length; i++ ) {
            URL url;
            try {
                url = new URL( XMLTools.getStringValue( nl.get( i ) ) );
            } catch ( MalformedURLException e ) {
                throw new XMLParsingException( XMLTools.getStringValue( nl.get( i ) ) + " is not an URL" );
            }
            dataURL[i] = new DataURL( null, url );

        }

        return dataURL;
    }

    /**
     *
     * @param layerElem
     * @return the styles
     * @throws XMLParsingException
     */
    @Override
    protected Style[] parseStyles( Element layerElem )
                            throws XMLParsingException {

        List<Node> nl = XMLTools.getNodes( layerElem, "./Style", nsContext );
        Style[] styles = new Style[nl.size()];
        for ( int i = 0; i < styles.length; i++ ) {
            String name = XMLTools.getRequiredNodeAsString( nl.get( i ), "./Name", nsContext );

            if ( name == null ) {
                throw new XMLParsingException( Messages.getMessage( "WMS_STYLENAME" ) );
            }
            String title = XMLTools.getNodeAsString( nl.get( i ), "./Title", nsContext, null );
            if ( title == null ) {
                throw new XMLParsingException( Messages.getMessage( "WMS_STYLETITLE" ) );
            }
            String styleAbstract = XMLTools.getNodeAsString( nl.get( i ), "./Abstract", nsContext, null );
            StyleURL styleURL = parseStyleURL( nl.get( i ) );

            styles[i] = new Style( name, title, styleAbstract, null, null, styleURL, null );
        }

        return styles;
    }

    /**
     *
     * @param node
     * @return the URL
     * @throws XMLParsingException
     */
    @Override
    protected StyleURL parseStyleURL( Node node )
                            throws XMLParsingException {

        StyleURL styleURL = null;
        Node styleNode = XMLTools.getNode( node, "./StyleURL", nsContext );

        if ( styleNode != null ) {
            URL url;
            try {
                url = new URL( XMLTools.getStringValue( styleNode ) );
            } catch ( MalformedURLException e ) {
                throw new XMLParsingException( XMLTools.getStringValue( styleNode ) + " is not an URL" );
            }
            styleURL = new StyleURL( null, url );
        }

        return styleURL;
    }

    /**
     *
     * @param layerElem
     * @param scaleHint
     *            the default scale hint
     * @return the scale hint
     * @throws XMLParsingException
     */
    @Override
    protected ScaleHint parseScaleHint( Element layerElem, ScaleHint scaleHint )
                            throws XMLParsingException {

        Node scNode = XMLTools.getNode( layerElem, "./ScaleHint", nsContext );
        if ( scNode != null ) {
            double mn = XMLTools.getNodeAsDouble( scNode, "./@min", nsContext, 0 );
            double mx = XMLTools.getNodeAsDouble( scNode, "./@max", nsContext, Double.MAX_VALUE );
            scaleHint = new ScaleHint( mn, mx );
        }

        if ( scaleHint == null ) {
            // set default value to avoid NullPointerException
            // when accessing a layers scalehint
            scaleHint = new ScaleHint( 0, Double.MAX_VALUE );
        }

        return scaleHint;
    }

    /**
     *
     * @param nl
     * @return the bboxes
     * @throws XMLParsingException
     */
    @Override
    protected LayerBoundingBox[] parseLayerBoundingBoxes( List<Element> nl )
                            throws XMLParsingException {

        LayerBoundingBox[] llBoxes = new LayerBoundingBox[nl.size()];
        for ( int i = 0; i < llBoxes.length; i++ ) {
            double minx = XMLTools.getRequiredNodeAsDouble( nl.get( i ), "./@minx", nsContext );
            double maxx = XMLTools.getRequiredNodeAsDouble( nl.get( i ), "./@maxx", nsContext );
            double miny = XMLTools.getRequiredNodeAsDouble( nl.get( i ), "./@miny", nsContext );
            double maxy = XMLTools.getRequiredNodeAsDouble( nl.get( i ), "./@maxy", nsContext );
            String srs = XMLTools.getRequiredNodeAsString( nl.get( i ), "./@SRS", nsContext );
            Position min = GeometryFactory.createPosition( minx, miny );
            Position max = GeometryFactory.createPosition( maxx, maxy );
            llBoxes[i] = new LayerBoundingBox( min, max, srs, -1, -1 );
        }

        return llBoxes;
    }

    /**
     *
     * @param llBox
     * @return the envelope
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    @Override
    protected Envelope parseLatLonBoundingBox( Element llBox )
                            throws XMLParsingException, UnknownCRSException {

        double minx = XMLTools.getRequiredNodeAsDouble( llBox, "./@minx", nsContext );
        double maxx = XMLTools.getRequiredNodeAsDouble( llBox, "./@maxx", nsContext );
        double miny = XMLTools.getRequiredNodeAsDouble( llBox, "./@miny", nsContext );
        double maxy = XMLTools.getRequiredNodeAsDouble( llBox, "./@maxy", nsContext );
        CoordinateSystem crs = CRSFactory.create( "EPSG:4326" );

        Envelope env = GeometryFactory.createEnvelope( minx, miny, maxx, maxy, crs );

        return env;
    }

}
