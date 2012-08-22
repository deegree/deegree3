// $HeadURL$
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
package org.deegree.ogcwebservices.wfs.capabilities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.Code;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities110Fragment;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.owscommon.OWSCommonCapabilitiesDocument;
import org.deegree.owscommon.OWSDomainType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Represents a capabilities document for an OGC WFS 1.1.0 compliant web service.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFSCapabilitiesDocument_1_1_0 extends OWSCommonCapabilitiesDocument {

    private static final long serialVersionUID = 6664839532969382269L;

    private static ILogger LOG = LoggerFactory.getLogger( WFSCapabilitiesDocument_1_1_0.class );

    /**
     * The "FeatureTypeList" string.
     */
    public final static String FEATURE_TYPE_LIST_NAME = "FeatureTypeList";

    /**
     * The "ServesGMLObjectTypeList" string.
     */
    public final static String SERVES_GML_OBJECT_TYPE_LIST_NAME = "ServesGMLObjectTypeList";

    /**
     * The "SupportsGMLObjectTypeList" string.
     */
    public final static String SUPPORTS_GML_OBJECT_TYPE_LIST_NAME = "SupportsGMLObjectTypeList";

    /**
     * The "FilterCapabilities" string.
     */
    public final static String FILTER_CAPABILITIES_NAME = "FilterCapabilities";

    protected static final URI WFSNS = CommonNamespaces.WFSNS;

    private static final String PRE_OWS = CommonNamespaces.OWS_PREFIX + ":";

    protected static final URI OGCNS = CommonNamespaces.OGCNS;

    protected static final URI DEEGREEWFSNS = CommonNamespaces.DEEGREEWFS;

    private static final String XML_TEMPLATE = "WFSCapabilitiesTemplate.xml";

    private static final String[] VALID_TYPES = { "TC211", "FGDC", "19115", "19139" };

    private static final String[] VALID_FORMATS = { "text/xml", "text/html", "text/sgml", "text/plain" };

    /**
     * Creates a skeleton capabilities document that contains the mandatory elements only.
     *
     * @throws IOException
     * @throws SAXException
     */
    public void createEmptyDocument()
                            throws IOException, SAXException {
        URL url = WFSCapabilitiesDocument_1_1_0.class.getResource( XML_TEMPLATE );
        if ( url == null ) {
            throw new IOException( "The resource '" + XML_TEMPLATE + " could not be found." );
        }
        load( url );
    }

    /**
     * Creates an emptyDocument with given version, and an updateSequence of "0" without reading the skeleton document.
     *
     * @param version
     *            if
     *
     */
    public void createEmptyDocument( String version ) {
        // set up the root document.
        Document doc = XMLTools.create();
        Element root = doc.createElementNS( "http://www.opengis.net/wfs", "WFS_Capabilities" );
        doc.importNode( root, false );

        setRootElement( root );
        root.setAttribute( "version", version );
        root.setAttribute( "updateSequence", "0" );
    }

    /**
     * Creates a class representation of the document.
     *
     * @return class representation of the configuration document
     */
    @Override
    public OGCCapabilities parseCapabilities()
                            throws InvalidCapabilitiesException {

        WFSCapabilities wfsCapabilities = null;
        try {

            wfsCapabilities = new WFSCapabilities( parseVersion(), parseUpdateSequence(), getServiceIdentification(),
                                                   getServiceProvider(), getOperationsMetadata(), getFeatureTypeList(),
                                                   getServesGMLObjectTypeList(), getSupportsGMLObjectTypeList(), null,
                                                   getFilterCapabilities() );
        } catch ( XMLParsingException e ) {
            throw new InvalidCapabilitiesException( e.getMessage() + "\n" + StringTools.stackTraceToString( e ) );
        }

        return wfsCapabilities;
    }

    /**
     * Returns the class representation for the <code>ServiceIdentification</code> section of the document.
     * <p>
     * NOTE: this method is overridden, because the WFS 1.1.0 requires the OWS 1.0.0 version of the element
     *
     * @return class representation for the <code>ServiceIdentification</code> section
     * @throws XMLParsingException
     */
    @Override
    public ServiceIdentification getServiceIdentification()
                            throws XMLParsingException {

        // Element element = XMLTools.getRequiredChildElement( "ServiceIdentification", OWSNS,
        // getRootElement() );
        Element element = XMLTools.getRequiredElement( getRootElement(), PRE_OWS + "ServiceIdentification", nsContext );

        // 'ServiceType' element (mandatory)
        // Element serviceTypeElement = XMLTools.getRequiredChildElement( "ServiceType", OWSNS,
        // element );
        Element serviceTypeElement = XMLTools.getRequiredElement( element, PRE_OWS + "ServiceType", nsContext );
        Code serviceType = null;
        try {
            String codeSpace = XMLTools.getAttrValue( serviceTypeElement, OWSNS, "codeSpace", null );
            URI uri = codeSpace != null ? new URI( codeSpace ) : null;
            serviceType = new Code( XMLTools.getStringValue( serviceTypeElement ), uri );
        } catch ( URISyntaxException e ) {
            throw new XMLParsingException( "Given value '"
                                           + XMLTools.getAttrValue( serviceTypeElement, OWSNS, "codeSpace", null )
                                           + "' in attribute 'codeSpace' of element 'ServiceType' " + "(namespace: '"
                                           + OWSNS + "') is not a valid URI." );
        }

        // 'ServiceTypeVersion' elements (mandatory)
        String[] serviceTypeVersions = XMLTools.getRequiredNodeAsStrings( element, "ows:ServiceTypeVersion", nsContext,
                                                                          ",;" );
        if ( serviceTypeVersions.length == 0 ) {
            String msg = "No version specified in 'ows:ServiceTypeVersion' element.";
            throw new XMLParsingException( msg );
        }

        // 'Fees' element (optional)
        String fees = XMLTools.getStringValue( "Fees", OWSNS, element, null );

        // 'AccessConstraints' elements (optional)
        String accessConstraints[] = XMLTools.getNodesAsStrings( element, "ows:AccessConstraints", nsContext );

        String title = XMLTools.getNodeAsString( element, "ows:Title", nsContext, null );        
        // 'ows:Name' -- nonstandard, added to support the WFS 1.0.0 Service/Name element
        String name = XMLTools.getNodeAsString( element, "ows:Name", nsContext, title );

        String abs = XMLTools.getNodeAsString( element, "ows:Abstract", nsContext, null );

        Keywords[] kws = getKeywords( XMLTools.getElements( element, "ows:Keywords", nsContext ) );

        ServiceIdentification serviceIdentification = new ServiceIdentification( name, serviceType, serviceTypeVersions,
                                                                                 title, abs, kws, fees,
                                                                                 accessConstraints );
        return serviceIdentification;
    }

    /**
     * Creates an object representation of the <code>ows:OperationsMetadata</code> section.
     *
     * @return object representation of the <code>ows:OperationsMetadata</code> section
     * @throws XMLParsingException
     */
    public OperationsMetadata getOperationsMetadata()
                            throws XMLParsingException {

        List<Node> operationElementList = XMLTools.getNodes( getRootElement(), "ows:OperationsMetadata/ows:Operation",
                                                             nsContext );

        // build HashMap of 'ows:Operation'-elements for easier access
        Map<String, Node> operations = new HashMap<String, Node>();
        for ( int i = 0; i < operationElementList.size(); i++ ) {
            operations.put( XMLTools.getRequiredNodeAsString( operationElementList.get( i ), "@name", nsContext ),
                            operationElementList.get( i ) );
        }

        Operation getCapabilities = getOperation( OperationsMetadata.GET_CAPABILITIES_NAME, true, operations );
        Operation describeFeatureType = getOperation( WFSOperationsMetadata.DESCRIBE_FEATURETYPE_NAME, true, operations );
        Operation getFeature = getOperation( WFSOperationsMetadata.GET_FEATURE_NAME, false, operations );
        Operation getFeatureWithLock = getOperation( WFSOperationsMetadata.GET_FEATURE_WITH_LOCK_NAME, false,
                                                     operations );
        Operation getGMLObject = getOperation( WFSOperationsMetadata.GET_GML_OBJECT_NAME, false, operations );
        Operation lockFeature = getOperation( WFSOperationsMetadata.LOCK_FEATURE_NAME, false, operations );
        Operation transaction = getOperation( WFSOperationsMetadata.TRANSACTION_NAME, false, operations );

        List<Element> parameterElementList = XMLTools.getElements( getRootElement(),
                                                                   "ows:OperationsMetadata/ows:Parameter", nsContext );
        OWSDomainType[] parameters = new OWSDomainType[parameterElementList.size()];
        for ( int i = 0; i < parameters.length; i++ ) {
            parameters[i] = getOWSDomainType( null, parameterElementList.get( i ) );
        }

        List<Element> constraintElementList = XMLTools.getElements( getRootElement(),
                                                                    "ows:OperationsMetadata/ows:Constraint", nsContext );
        OWSDomainType[] constraints = new OWSDomainType[constraintElementList.size()];
        for ( int i = 0; i < constraints.length; i++ ) {
            constraints[i] = getOWSDomainType( null, constraintElementList.get( i ) );
        }
        WFSOperationsMetadata metadata = new WFSOperationsMetadata( getCapabilities, describeFeatureType, getFeature,
                                                                    getFeatureWithLock, getGMLObject, lockFeature,
                                                                    transaction, parameters, constraints );

        return metadata;
    }

    /**
     * Returns the object representation for the <code>wfs:FeatureTypeList</code>- section.
     *
     * @return object representation of the <code>wfs:FeatureTypeList</code> section, may be empty (if missing)
     * @throws XMLParsingException
     */
    public FeatureTypeList getFeatureTypeList()
                            throws XMLParsingException {

        List<WFSFeatureType> wfsFeatureTypes = new ArrayList<WFSFeatureType>();

        FeatureTypeList featureTypeList = new FeatureTypeList(
                                                               new org.deegree.ogcwebservices.wfs.capabilities.Operation[0],
                                                               wfsFeatureTypes );

        Element element = (Element) XMLTools.getNode( getRootElement(), "wfs:FeatureTypeList", nsContext );
        if ( element != null ) {
            org.deegree.ogcwebservices.wfs.capabilities.Operation[] globalOperations = null;
            Element operationsTypeElement = (Element) XMLTools.getNode( element, "wfs:Operations", nsContext );
            if ( operationsTypeElement != null ) {
                globalOperations = getOperationsType( operationsTypeElement );
            }
            List<Element> featureTypeElementList = XMLTools.getElements( element, "wfs:FeatureType", nsContext );
            // TODO Check this.
            // if ( featureTypeElementList.getLength() < 1 ) {
            // throw new XMLParsingException(
            // "A wfs:FeatureTypeListType must contain at least one wfs:FeatureType-element." );
            // }
            for ( int i = 0; i < featureTypeElementList.size(); i++ ) {
                WFSFeatureType wfsFT = getFeatureTypeType( featureTypeElementList.get( i ) );
                wfsFeatureTypes.add( wfsFT );
            }

            featureTypeList = new FeatureTypeList( globalOperations, wfsFeatureTypes );
        }

        return featureTypeList;
    }

    /**
     * Returns the object representation for the <code>wfs:ServesGMLObjectTypeList</code>- section.
     *
     * @return object representation of the <code>wfs:ServesGMLObjectTypeList</code> section, null if the section does
     *         not exist
     * @throws XMLParsingException
     */
    public GMLObject[] getServesGMLObjectTypeList()
                            throws XMLParsingException {

        GMLObject[] gmlObjectTypes = null;
        Element element = (Element) XMLTools.getNode( getRootElement(), "wfs:ServesGMLObjectTypeList", nsContext );
        if ( element != null ) {
            List<Node> nodeList = XMLTools.getRequiredNodes( element, "wfs:GMLObjectType", nsContext );
            gmlObjectTypes = new GMLObject[nodeList.size()];
            for ( int i = 0; i < gmlObjectTypes.length; i++ ) {
                gmlObjectTypes[i] = getGMLObjectType( (Element) nodeList.get( i ) );
            }
        }

        return gmlObjectTypes;
    }

    /**
     * Returns the object representation for the <code>wfs:SupportsGMLObjectTypeList</code>- section.
     *
     * @return object representation of the <code>wfs:SupportsGMLObjectTypeList</code> section, null if the section
     *         does not exist
     * @throws XMLParsingException
     */
    public GMLObject[] getSupportsGMLObjectTypeList()
                            throws XMLParsingException {

        GMLObject[] gmlObjectTypes = null;
        Element element = (Element) XMLTools.getNode( getRootElement(), "wfs:SupportsGMLObjectTypeList", nsContext );
        if ( element != null ) {
            List<Node> nodeList = XMLTools.getRequiredNodes( element, "wfs:GMLObjectType", nsContext );
            gmlObjectTypes = new GMLObject[nodeList.size()];
            for ( int i = 0; i < gmlObjectTypes.length; i++ ) {
                gmlObjectTypes[i] = getGMLObjectType( (Element) nodeList.get( i ) );
            }
        }

        return gmlObjectTypes;
    }

    /**
     * Returns the object representation for an element of type <code>wfs:GMLObjectType</code>.
     *
     * @param element
     * @return object representation of the element of type <code>wfs:GMLObjectType</code>
     * @throws XMLParsingException
     */
    public GMLObject getGMLObjectType( Element element )
                            throws XMLParsingException {
        QualifiedName name = parseQualifiedName( XMLTools.getRequiredNode( element, "wfs:Name/text()", nsContext ) );
        String title = XMLTools.getNodeAsString( element, "wfs:Title/text()", nsContext, null );
        String abstract_ = XMLTools.getNodeAsString( element, "wfs:Abstract/text()", nsContext, null );
        Keywords[] keywords = getKeywords( XMLTools.getNodes( element, "ows:Keywords", nsContext ) );
        List<Element> formatElementList = XMLTools.getElements( element, "wfs:OutputFormats/wfs:Format", nsContext );
        FormatType[] outputFormats = new FormatType[formatElementList.size()];
        for ( int i = 0; i < outputFormats.length; i++ ) {
            outputFormats[i] = getFormatType( formatElementList.get( i ) );
        }
        return new GMLObject( name, title, abstract_, keywords, outputFormats );
    }

    /**
     * Returns the object representation for an element of type <code>wfs:FeatureTypeType</code>.
     *
     * @param element
     * @return object representation for the element of type <code>wfs:OperationsType</code>
     * @throws XMLParsingException
     */
    public WFSFeatureType getFeatureTypeType( Element element )
                            throws XMLParsingException {

        QualifiedName name = parseQualifiedName( XMLTools.getRequiredNode( element, "wfs:Name/text()", nsContext ) );
        String title = XMLTools.getRequiredNodeAsString( element, "wfs:Title/text()", nsContext );
        String abstract_ = XMLTools.getNodeAsString( element, "wfs:Abstract/text()", nsContext, null );
        Keywords[] keywords = getKeywords( XMLTools.getNodes( element, "ows:Keywords", nsContext ) );

        URI defaultSrs = null;
        URI[] otherSrs = null;
        Node noSrsElement = XMLTools.getNode( element, "wfs:NoSRS", nsContext );
        if ( noSrsElement == null ) {
            defaultSrs = XMLTools.getNodeAsURI( element, "wfs:DefaultSRS/text()", nsContext, null );
            if ( defaultSrs == null ) {
                String msg = "A 'wfs:FeatureType' element must always contain a 'wfs:NoSRS' "
                             + "element  or a 'wfs:DefaultSRS' element";
                throw new XMLParsingException( msg );
            }
            otherSrs = XMLTools.getNodesAsURIs( element, "wfs:OtherSRS/text()", nsContext );
        }

        org.deegree.ogcwebservices.wfs.capabilities.Operation[] operations = null;
        Element operationsTypeElement = (Element) XMLTools.getNode( element, "wfs:Operations", nsContext );
        if ( operationsTypeElement != null ) {
            operations = getOperationsType( operationsTypeElement );
        }
        List<Element> formatElementList = XMLTools.getElements( element, "wfs:OutputFormats/wfs:Format", nsContext );
        FormatType[] formats = new FormatType[formatElementList.size()];
        for ( int i = 0; i < formats.length; i++ ) {
            formats[i] = getFormatType( formatElementList.get( i ) );
        }
        List<Element> wgs84BoundingBoxElements = XMLTools.getElements( element, "ows:WGS84BoundingBox", nsContext );
        if ( wgs84BoundingBoxElements.size() < 1 ) {
            throw new XMLParsingException( "A 'wfs:FeatureTypeType' must contain at least one "
                                           + "'ows:WGS84BoundingBox'-element." );
        }
        Envelope[] wgs84BoundingBoxes = new Envelope[wgs84BoundingBoxElements.size()];
        for ( int i = 0; i < wgs84BoundingBoxes.length; i++ ) {
            wgs84BoundingBoxes[i] = getWGS84BoundingBoxType( wgs84BoundingBoxElements.get( i ) );
        }
        List<Element> metadataURLElementList = XMLTools.getElements( element, "wfs:MetadataURL", nsContext );
        MetadataURL[] metadataUrls = new MetadataURL[metadataURLElementList.size()];
        for ( int i = 0; i < metadataUrls.length; i++ ) {
            metadataUrls[i] = getMetadataURL( metadataURLElementList.get( i ) );
        }
        WFSFeatureType featureType = new WFSFeatureType( name, title, abstract_, keywords, defaultSrs, otherSrs,
                                                         operations, formats, wgs84BoundingBoxes, metadataUrls );

        return featureType;
    }

    /**
     * Returns the object representation for an <code>wfs:OutputFormat</code> -element.
     *
     * @param element
     * @return object representation for the element
     * @throws XMLParsingException
     */
    public FormatType getFormatType( Element element )
                            throws XMLParsingException {

        String[] tmp = new String[3];
        URI[] uris = new URI[3];
        tmp[0] = XMLTools.getNodeAsString( element, "@deegreewfs:inFilter", nsContext, null );
        tmp[1] = XMLTools.getNodeAsString( element, "@deegreewfs:outFilter", nsContext, null );
        tmp[2] = XMLTools.getNodeAsString( element, "@deegreewfs:schemaLocation", nsContext, null );
        for ( int i = 0; i < tmp.length; i++ ) {
            try {
                if ( tmp[i] != null && !"".equals( tmp[i].trim() ) ) {
                    if ( !( tmp[i].toLowerCase().startsWith( "file:/" ) ) ) {
                        tmp[i] = this.resolve( tmp[i] ).toExternalForm();
                        LOG.logDebug( "Found format "
                                      + ( ( i == 0 ) ? "inFilter" : ( ( i == 1 ) ? "outFilter" : "schemaLocation" ) )
                                      + " at location: " + tmp[i] );
                    }
                    uris[i] = new URI( tmp[i] );
                }
            } catch ( MalformedURLException e ) {
                throw new XMLParsingException( "Could not resolve relative path:" + tmp[i] );
            } catch ( URISyntaxException e ) {
                throw new XMLParsingException( "Not a valid URI:" + tmp[i] );
            }
        }

        String value = XMLTools.getRequiredNodeAsString( element, "text()", nsContext );

        return new FormatType( uris[0], uris[1], uris[2], value );
    }

    /**
     * Returns the object representation for an element node of type <code>wfs:MetadataURLType</code>.
     *
     * TODO: Schema says base type is String, not URL!
     *
     * @param element
     * @return object representation for the element of type <code>wfs:MetadataURLType</code>
     * @throws XMLParsingException
     */
    public MetadataURL getMetadataURL( Element element )
                            throws XMLParsingException {

        String type = XMLTools.getRequiredNodeAsString( element, "@type", nsContext, VALID_TYPES );
        String format = XMLTools.getRequiredNodeAsString( element, "@format", nsContext, VALID_FORMATS );
        String url = XMLTools.getRequiredNodeAsString( element, "text()", nsContext );
        URL onlineResource;
        try {
            onlineResource = new URL( url );
        } catch ( MalformedURLException e ) {
            throw new XMLParsingException( "A wfs:MetadataURLType must contain a valid URL: " + e.getMessage() );
        }

        return new MetadataURL( type, format, onlineResource );
    }

    /**
     * Returns the object representation for an element node of type <code>wfs:OperationsType</code>.
     *
     * @param element
     * @return object representation for the element of type <code>wfs:OperationsType</code>
     * @throws XMLParsingException
     */
    public org.deegree.ogcwebservices.wfs.capabilities.Operation[] getOperationsType( Element element )
                            throws XMLParsingException {

        String[] operationCodes = XMLTools.getNodesAsStrings( element, "wfs:Operation/text()", nsContext );
        org.deegree.ogcwebservices.wfs.capabilities.Operation[] operations = new org.deegree.ogcwebservices.wfs.capabilities.Operation[operationCodes.length];
        for ( int i = 0; i < operations.length; i++ ) {
            try {
                operations[i] = new org.deegree.ogcwebservices.wfs.capabilities.Operation( operationCodes[i] );
            } catch ( InvalidParameterException e ) {
                throw new XMLParsingException( e.getMessage() );
            }
        }

        return operations;
    }

    /**
     * Returns the object representation for the <code>Filter_Capabilities</code> section of the document.
     *
     * @return class representation for the <code>Filter_Capabilities</code> section
     * @throws XMLParsingException
     */
    public FilterCapabilities getFilterCapabilities()
                            throws XMLParsingException {

        FilterCapabilities filterCapabilities = null;
        Element filterCapabilitiesElement = (Element) XMLTools.getNode( getRootElement(), "ogc:Filter_Capabilities",
                                                                        nsContext );
        if ( filterCapabilitiesElement != null ) {
            filterCapabilities = new FilterCapabilities110Fragment( filterCapabilitiesElement, getSystemId() ).parseFilterCapabilities();
        }
        return filterCapabilities;
    }
}
