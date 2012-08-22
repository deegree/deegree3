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
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities100Fragment;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilitiesDocument;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.owscommon.OWSDomainType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Represents a capabilities document for an OGC WFS 1.0.0 compliant web service.
 * <p>
 * NOTE: The parsing methods produces beans that are designed to match the WFS 1.1.0 specification. Needs testing! The
 * following things are still TBD:
 * <ul>
 * <li>Respect value of wfs:Service/wfs:OnlineResource element. Should possible be stored in a {@link ServiceProvider}
 * bean.</li>
 * <li>Respect value of wfs:Capability/wfs:DescribeFeatureType/wfs:SchemaDescriptionLanguage element. Should possibly be
 * stored as an {@link OWSDomainType} bean in the corresponding {@link Operation} object.</li>
 * <li>Respect value of wfs:Capability/wfs:GetFeature/wfs:ResultFormat element. Should possibly be stored as an
 * {@link OWSDomainType} bean in the corresponding {@link Operation} object.</li>
 * <li>Respect value of wfs:Capability/wfs:GetFeatureWithLock/wfs:ResultFormat element. Should possibly be stored as an
 * {@link OWSDomainType} bean in the corresponding {@link Operation} object.</li>
 * </ul>
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFSCapabilitiesDocument_1_0_0 extends OGCCapabilitiesDocument {

    private static final long serialVersionUID = 4538469826043112486L;

    private static final String[] VALID_TYPES = { "TDC211", "FGDC" };

    private static final String[] VALID_FORMATS = { "XML", "SGML", "TXT" };

    /**
     * Creates a skeleton capabilities document that contains the mandatory elements only.
     *
     * @throws IOException
     * @throws SAXException
     */
    public void createEmptyDocument()
                            throws IOException, SAXException {
        // set up the root document.
        Document doc = XMLTools.create();
        Element root = doc.createElementNS( "http://www.opengis.net/wfs", "wfs:WFS_Capabilities" );
        doc.importNode( root, false );

        setRootElement( root );
        root.setAttribute( "version", "1.0.0" );
        root.setAttribute( "updateSequence", "0" );
    }

    /**
     * Creates a class representation of the document.
     *
     * @return class representation of the capabilities document
     */
    @Override
    public OGCCapabilities parseCapabilities()
                            throws InvalidCapabilitiesException {

        WFSCapabilities wfsCapabilities = null;
        try {

            wfsCapabilities = new WFSCapabilities( parseVersion(), parseUpdateSequence(), getService(), null,
                                                   getCapability(), getFeatureTypeList(), null, null, null,
                                                   getFilterCapabilities() );
        } catch ( XMLParsingException e ) {
            throw new InvalidCapabilitiesException( e.getMessage() + "\n" + StringTools.stackTraceToString( e ) );
        }

        return wfsCapabilities;
    }

    /**
     * Returns the class representation for the <code>Service</code> section of the document.
     *
     * @return class representation for the <code>Service</code> section
     * @throws XMLParsingException
     */
    public ServiceIdentification getService()
                            throws XMLParsingException {

        Element element = XMLTools.getRequiredElement( getRootElement(), "wfs:Service", nsContext );

        String name = XMLTools.getRequiredNodeAsString( element, "wfs:Name/text()", nsContext );
        String title = XMLTools.getRequiredNodeAsString( element, "wfs:Title/text()", nsContext );
        String abstract_ = XMLTools.getNodeAsString( element, "wfs:Abstract/text()", nsContext, null );

        String keywordsValue = XMLTools.getNodeAsString( element, "wfs:Keywords/text()", nsContext, null );
        Keywords[] keywords = null;
        if ( keywordsValue != null ) {
            keywords = new Keywords[] { new Keywords( new String[] { keywordsValue } ) };
        }

        String[] serviceTypeVersions = new String[] { "1.0.0" };

        // String onlineResource = XMLTools.getRequiredNodeAsString( element, "wfs:OnlineResource/text()", nsContext );

        // 'Fees' element (optional)
        String fees = XMLTools.getNodeAsString( element, "wfs:Fees/text()", nsContext, null );

        return new ServiceIdentification( name, null, serviceTypeVersions, title, abstract_, keywords, fees, null );
    }

    /**
     * Creates an object representation of the <code>wfs:Capability</code> section.
     *
     * @return object representation of the <code>wfs:Capability</code> section
     * @throws XMLParsingException
     */
    public OperationsMetadata getCapability()
                            throws XMLParsingException {

        Element requestElement = XMLTools.getRequiredElement( getRootElement(), "wfs:Capability/wfs:Request", nsContext );

        // wfs:GetCapabilities element
        Operation getCapabilities = null;
        Element getCapabilitiesElement = XMLTools.getElement( requestElement, "wfs:GetCapabilities", nsContext );
        if ( getCapabilitiesElement != null ) {
            List<Element> dcpTypeElements = XMLTools.getElements( getCapabilitiesElement, "wfs:DCPType", nsContext );
            DCPType[] dcpTypes = new DCPType[dcpTypeElements.size()];
            for ( int i = 0; i < dcpTypes.length; i++ ) {
                Element dcpTypeElement = dcpTypeElements.get( i );
                dcpTypes[i] = getDCPTypeType( dcpTypeElement );
            }
            getCapabilities = new Operation( "GetCapabilities", dcpTypes );
        }

        // wfs:DescribeFeatureType element
        Operation describeFeatureType = null;
        Element describeFeatureTypeElement = XMLTools.getElement( requestElement, "wfs:DescribeFeatureType", nsContext );
        if ( describeFeatureTypeElement != null ) {
            List<Element> dcpTypeElements = XMLTools.getElements( describeFeatureTypeElement, "wfs:DCPType", nsContext );
            DCPType[] dcpTypes = new DCPType[dcpTypeElements.size()];
            for ( int i = 0; i < dcpTypes.length; i++ ) {
                Element dcpTypeElement = dcpTypeElements.get( i );
                dcpTypes[i] = getDCPTypeType( dcpTypeElement );
            }

            // TODO evaluate SchemaDescriptionLanguage element

            describeFeatureType = new Operation( "DescribeFeatureType", dcpTypes );
        }

        // wfs:GetFeature element
        Operation getFeature = null;
        Element getFeatureElement = XMLTools.getElement( requestElement, "wfs:GetFeature", nsContext );
        if ( getFeatureElement != null ) {
            List<Element> dcpTypeElements = XMLTools.getElements( getFeatureElement, "wfs:DCPType", nsContext );
            DCPType[] dcpTypes = new DCPType[dcpTypeElements.size()];
            for ( int i = 0; i < dcpTypes.length; i++ ) {
                Element dcpTypeElement = dcpTypeElements.get( i );
                dcpTypes[i] = getDCPTypeType( dcpTypeElement );
            }

            // TODO evaluate ResultFormat element

            getFeature = new Operation( "GetFeature", dcpTypes );
        }

        // wfs:GetFeature element
        Operation getFeatureWithLock = null;
        Element getFeatureWithLockElement = XMLTools.getElement( requestElement, "wfs:GetFeatureWithLock", nsContext );
        if ( getFeatureWithLockElement != null ) {
            List<Element> dcpTypeElements = XMLTools.getElements( getFeatureWithLockElement, "wfs:DCPType", nsContext );
            DCPType[] dcpTypes = new DCPType[dcpTypeElements.size()];
            for ( int i = 0; i < dcpTypes.length; i++ ) {
                Element dcpTypeElement = dcpTypeElements.get( i );
                dcpTypes[i] = getDCPTypeType( dcpTypeElement );
            }

            // TODO evaluate ResultFormat element

            getFeatureWithLock = new Operation( "GetFeatureWithLock", dcpTypes );
        }

        // wfs:LockFeature element
        Operation lockFeature = null;
        Element lockFeatureElement = XMLTools.getElement( requestElement, "wfs:LockFeature", nsContext );
        if ( lockFeatureElement != null ) {
            List<Element> dcpTypeElements = XMLTools.getElements( lockFeatureElement, "wfs:DCPType", nsContext );
            DCPType[] dcpTypes = new DCPType[dcpTypeElements.size()];
            for ( int i = 0; i < dcpTypes.length; i++ ) {
                Element dcpTypeElement = dcpTypeElements.get( i );
                dcpTypes[i] = getDCPTypeType( dcpTypeElement );
            }
            lockFeature = new Operation( "LockFeature", dcpTypes );
        }

        // wfs:Transaction element
        Operation transaction = null;
        Element transactionElement = XMLTools.getElement( requestElement, "wfs:Transaction", nsContext );
        if ( transactionElement != null ) {
            List<Element> dcpTypeElements = XMLTools.getElements( transactionElement, "wfs:DCPType", nsContext );
            DCPType[] dcpTypes = new DCPType[dcpTypeElements.size()];
            for ( int i = 0; i < dcpTypes.length; i++ ) {
                Element dcpTypeElement = dcpTypeElements.get( i );
                dcpTypes[i] = getDCPTypeType( dcpTypeElement );
            }
            transaction = new Operation( "Transaction", dcpTypes );
        }

        return new WFSOperationsMetadata( getCapabilities, describeFeatureType, getFeature, getFeatureWithLock, null,
                                          lockFeature, transaction, null, null );
    }

    /**
     * Creates an object representation of the given <code>wfs:DCPTypeType</code> element.
     *
     * @return object representation of the given <code>wfs:DCPTypeType</code> element.
     * @throws XMLParsingException
     */
    private DCPType getDCPTypeType( Element element )
                            throws XMLParsingException {

        Element httpElement = XMLTools.getRequiredElement( element, "wfs:HTTP", nsContext );
        String[] gets = XMLTools.getNodesAsStrings( httpElement, "wfs:Get/@onlineResource", nsContext );
        URL[] getURLs = new URL[gets.length];
        for ( int j = 0; j < gets.length; j++ ) {
            try {
                getURLs[j] = new URL( gets[j] );
            } catch ( MalformedURLException e ) {
                throw new XMLParsingException( "OnlineResource '" + gets[j] + "' is not a valid URL." );
            }
        }
        String[] posts = XMLTools.getNodesAsStrings( httpElement, "wfs:Post/@onlineResource", nsContext );
        URL[] postURLs = new URL[posts.length];
        for ( int j = 0; j < posts.length; j++ ) {
            try {
                postURLs[j] = new URL( posts[j] );
            } catch ( MalformedURLException e ) {
                throw new XMLParsingException( "OnlineResource '" + posts[j] + "' is not a valid URL." );
            }
        }
        return new DCPType( new HTTP( getURLs, postURLs ) );
    }

    /**
     * Returns the object representation of the <code>wfs:FeatureTypeList</code>- section.
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
            if ( featureTypeElementList.size() < 1 ) {
                throw new XMLParsingException(
                                               "A wfs:FeatureTypeListType must contain at least one wfs:FeatureType-element." );
            }
            for ( int i = 0; i < featureTypeElementList.size(); i++ ) {
                WFSFeatureType wfsFT = getFeatureTypeType( featureTypeElementList.get( i ) );
                wfsFeatureTypes.add( wfsFT );
            }

            featureTypeList = new FeatureTypeList( globalOperations, wfsFeatureTypes );
        }

        return featureTypeList;
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
        String title = XMLTools.getNodeAsString( element, "wfs:Title/text()", nsContext, null );
        String abstract_ = XMLTools.getNodeAsString( element, "wfs:Abstract/text()", nsContext, null );

        String keywordsValue = XMLTools.getNodeAsString( element, "wfs:Keywords/text()", nsContext, null );
        Keywords[] keywords = null;
        if ( keywordsValue != null ) {
            keywords = new Keywords[] { new Keywords( new String[] { keywordsValue } ) };
        }

        URI defaultSrs = XMLTools.getRequiredNodeAsURI( element, "wfs:SRS", nsContext );

        org.deegree.ogcwebservices.wfs.capabilities.Operation[] operations = null;
        Element operationsTypeElement = (Element) XMLTools.getNode( element, "wfs:Operations", nsContext );
        if ( operationsTypeElement != null ) {
            operations = getOperationsType( operationsTypeElement );
        }

        List<Element> latLongBoundingBoxElements = XMLTools.getElements( element, "wfs:LatLongBoundingBox", nsContext );
        Envelope[] latLongBoundingBoxes = new Envelope[latLongBoundingBoxElements.size()];
        for ( int i = 0; i < latLongBoundingBoxes.length; i++ ) {
            latLongBoundingBoxes[i] = getLatLongBoundingBoxType( latLongBoundingBoxElements.get( i ) );
        }

        List<Element> metadataURLElementList = XMLTools.getElements( element, "wfs:MetadataURL", nsContext );
        MetadataURL[] metadataUrls = new MetadataURL[metadataURLElementList.size()];
        for ( int i = 0; i < metadataUrls.length; i++ ) {
            metadataUrls[i] = getMetadataURL( metadataURLElementList.get( i ) );
        }

        return new WFSFeatureType( name, title, abstract_, keywords, defaultSrs, null, operations, null,
                                   latLongBoundingBoxes, metadataUrls );
    }

    /**
     * Creates an <code>Envelope</code> object from the given element of type <code>wfs:LatLongBoundingBoxType</code>.
     *
     * @param element
     * @return corresponsing <code>Envelope</code> object
     * @throws XMLParsingException
     */
    private Envelope getLatLongBoundingBoxType( Element element )
                            throws XMLParsingException {
        double minX = XMLTools.getRequiredNodeAsDouble( element, "@minx", nsContext );
        double minY = XMLTools.getRequiredNodeAsDouble( element, "@miny", nsContext );
        double maxX = XMLTools.getRequiredNodeAsDouble( element, "@maxx", nsContext );
        double maxY = XMLTools.getRequiredNodeAsDouble( element, "@maxy", nsContext );
        return GeometryFactory.createEnvelope( minX, minY, maxX, maxY, null );
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
            if ( org.deegree.ogcwebservices.wfs.capabilities.Operation.GET_GML_OBJECT.equals( operationCodes[i] ) ) {
                String msg = ( "Invalid WFS capabilities document. WFS 1.0.0 does not specify operation '"
                               + org.deegree.ogcwebservices.wfs.capabilities.Operation.GET_GML_OBJECT + ".'" );
                throw new XMLParsingException( msg );
            }
            try {
                operations[i] = new org.deegree.ogcwebservices.wfs.capabilities.Operation( operationCodes[i] );
            } catch ( InvalidParameterException e ) {
                throw new XMLParsingException( e.getMessage() );
            }
        }

        return operations;
    }

    /**
     * Returns the object representation of the <code>Filter_Capabilities</code> section of the document.
     *
     * @return class representation of the <code>Filter_Capabilities</code> section
     * @throws XMLParsingException
     */
    public FilterCapabilities getFilterCapabilities()
                            throws XMLParsingException {

        FilterCapabilities filterCapabilities = null;
        Element filterCapabilitiesElement = (Element) XMLTools.getNode( getRootElement(), "ogc:Filter_Capabilities",
                                                                        nsContext );
        if ( filterCapabilitiesElement != null ) {
            filterCapabilities = new FilterCapabilities100Fragment( filterCapabilitiesElement, getSystemId() ).parseFilterCapabilities();
        }
        return filterCapabilities;
    }
}
