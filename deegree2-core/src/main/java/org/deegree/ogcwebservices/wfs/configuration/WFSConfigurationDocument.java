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
package org.deegree.ogcwebservices.wfs.configuration;

import static org.deegree.framework.xml.XMLTools.getNodeAsBoolean;
import static org.deegree.framework.xml.XMLTools.getRequiredElement;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.InvalidConfigurationException;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.Protocol;
import org.deegree.ogcwebservices.wfs.capabilities.FormatType;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.deegree.ogcwebservices.wfs.capabilities.WFSOperationsMetadata;
import org.deegree.owscommon.OWSDomainType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Represents an XML configuration document for a deegree WFS instance, i.e. it consists of all sections common to an
 * OGC WFS 1.1 capabilities document plus some deegree specific elements.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFSConfigurationDocument extends WFSCapabilitiesDocument {

    private static final long serialVersionUID = -6415476866015999971L;

    protected static final ILogger LOG = LoggerFactory.getLogger( WFSConfigurationDocument.class );

    /**
     * Creates an object representation of the document.
     *
     * @return class representation of the configuration document
     * @throws InvalidConfigurationException
     */
    public WFSConfiguration getConfiguration()
                            throws InvalidConfigurationException {

        WFSConfiguration config = null;

        try {
            WFSDeegreeParams deegreeParams = getDeegreeParams();

            // get default Urls (used when DCP element is ommitted in Operation-elements)
            OnlineResource defaultOnlineResource = deegreeParams.getDefaultOnlineResource();
            String defaultUrl = defaultOnlineResource.getLinkage().getHref().toString();
            if ( defaultUrl.endsWith( "?" ) ) {
                defaultUrl = defaultUrl.substring( 0, defaultUrl.length() - 1 );
            }
            URL defaultUrlGet = new URL( defaultUrl + '?' );
            URL defaultUrlPost = new URL( defaultUrl );

            OperationsMetadata opMetadata = getOperationsMetadata( defaultUrlGet, defaultUrlPost );
            config = new WFSConfiguration( parseVersion(), parseUpdateSequence(), getServiceIdentification(),
                                           getServiceProvider(), opMetadata, getFeatureTypeList(),
                                           getServesGMLObjectTypeList(), getSupportsGMLObjectTypeList(), null,
                                           getFilterCapabilities(), deegreeParams );
        } catch ( Exception e ) {
            throw new InvalidConfigurationException( e.getMessage() + "\n" + StringTools.stackTraceToString( e ) );
        }
        return config;
    }

    /**
     * Creates an object representation of the <code>deegreeParams</code>- section.
     *
     * @return class representation of the <code>deegreeParams</code>- section
     * @throws InvalidConfigurationException
     */
    public WFSDeegreeParams getDeegreeParams()
                            throws InvalidConfigurationException {

        WFSDeegreeParams deegreeParams = null;

        try {
            Element element = (Element) XMLTools.getRequiredNode( getRootElement(), "deegreewfs:deegreeParams",
                                                                  nsContext );
            OnlineResource defaultOnlineResource = parseOnLineResource( getRequiredElement(
                                                                                            element,
                                                                                            "deegreewfs:DefaultOnlineResource",
                                                                                            nsContext ) );
            int cacheSize = XMLTools.getNodeAsInt( element, "deegreewfs:CacheSize/text()", nsContext, 100 );
            int requestTimeLimit = XMLTools.getNodeAsInt( element, "deegreewfs:RequestTimeLimit/text()", nsContext, 10 );

            boolean checkUTMZones = getNodeAsBoolean( element, "deegreewfs:CheckUTMZones", nsContext, false );
            boolean switchAxes = getNodeAsBoolean( element, "deegreewfs:SwitchAxesForEPSG4326", nsContext, false );
            boolean printGeomIds = getNodeAsBoolean( element, "deegreewfs:PrintGeometryGmlIds", nsContext, false );

            String characterSet = XMLTools.getNodeAsString( element, "deegreewfs:Encoding/text()", nsContext,
                                                            CharsetUtils.getSystemCharset() );
            String[] dataDirectories = XMLTools.getNodesAsStrings(
                                                                   element,
                                                                   "deegreewfs:DataDirectoryList/deegreewfs:DataDirectory/text()",
                                                                   nsContext );
            if ( dataDirectories.length == 0 ) {
                LOG.logInfo( "No data directory specified. Using configuration document directory." );
                dataDirectories = new String[] { "." };
            }
            for ( int i = 0; i < dataDirectories.length; i++ ) {
                try {
                    dataDirectories[i] = resolve( dataDirectories[i] ).toURI().getPath();
                } catch ( Exception e ) {
                    String msg = "DataDirectory '" + dataDirectories[i] + "' cannot be resolved as a directory: "
                                 + e.getMessage();
                    throw new InvalidConfigurationException( msg );
                }
            }

            String lockManagerString = XMLTools.getNodeAsString( element, "deegreewfs:LockManagerDirectory/text()",
                                                                 nsContext, null );
            File lockManagerDir = null;
            if ( lockManagerString != null ) {
                try {
                    lockManagerDir = new File( this.resolve( lockManagerString ).toURI().toURL().getFile() );
                } catch ( Exception e ) {
                    String msg = "Specified value (" + lockManagerDir
                                 + ") for 'deegreewfs:LockManagerDirectory' is invalid.";
                    throw new InvalidConfigurationException( msg, e );
                }
            }
            deegreeParams = new WFSDeegreeParams( defaultOnlineResource, cacheSize, requestTimeLimit, characterSet,
                                                  dataDirectories, lockManagerDir, checkUTMZones, switchAxes,
                                                  printGeomIds );
        } catch ( XMLParsingException e ) {
            throw new InvalidConfigurationException( "Error parsing the deegreeParams "
                                                     + "section of the WFS configuration: \n" + e.getMessage()
                                                     + StringTools.stackTraceToString( e ) );
        }
        return deegreeParams;
    }

    /**
     * Creates an object representation of the <code>ows:OperationsMetadata</code> section.
     *
     * @param defaultUrlGet
     * @param defaultUrlPost
     * @return object representation of the <code>ows:OperationsMetadata</code> section
     * @throws XMLParsingException
     */
    public OperationsMetadata getOperationsMetadata( URL defaultUrlGet, URL defaultUrlPost )
                            throws XMLParsingException {

        List<Node> operationElementList = XMLTools.getNodes( getRootElement(), "ows:OperationsMetadata/ows:Operation",
                                                             nsContext );

        // build HashMap of 'ows:Operation'-elements for easier access
        Map<String, Element> operations = new HashMap<String, Element>();
        for ( int i = 0; i < operationElementList.size(); i++ ) {
            operations.put( XMLTools.getRequiredNodeAsString( operationElementList.get( i ), "@name", nsContext ),
                            (Element) operationElementList.get( i ) );
        }

        Operation getCapabilities = getOperation( OperationsMetadata.GET_CAPABILITIES_NAME, true, operations,
                                                  defaultUrlGet, defaultUrlPost );
        Operation describeFeatureType = getOperation( WFSOperationsMetadata.DESCRIBE_FEATURETYPE_NAME, true,
                                                      operations, defaultUrlGet, defaultUrlPost );
        Operation getFeature = getOperation( WFSOperationsMetadata.GET_FEATURE_NAME, false, operations, defaultUrlGet,
                                             defaultUrlPost );
        Operation getFeatureWithLock = getOperation( WFSOperationsMetadata.GET_FEATURE_WITH_LOCK_NAME, false,
                                                     operations, defaultUrlGet, defaultUrlPost );
        Operation getGMLObject = getOperation( WFSOperationsMetadata.GET_GML_OBJECT_NAME, false, operations,
                                               defaultUrlGet, defaultUrlPost );
        Operation lockFeature = getOperation( WFSOperationsMetadata.LOCK_FEATURE_NAME, false, operations,
                                              defaultUrlGet, defaultUrlPost );
        Operation transaction = getOperation( WFSOperationsMetadata.TRANSACTION_NAME, false, operations, defaultUrlGet,
                                              defaultUrlPost );

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
     * Creates an object representation of an <code>ows:Operation</code>- element.
     *
     * @param name
     * @param isMandatory
     * @param operations
     * @param defaultUrlGet
     * @param defaultUrlPost
     * @return object representation of <code>ows:Operation</code>- element
     * @throws XMLParsingException
     */
    protected Operation getOperation( String name, boolean isMandatory, Map<String, Element> operations,
                                      URL defaultUrlGet, URL defaultUrlPost )
                            throws XMLParsingException {

        Operation operation = null;
        Element operationElement = operations.get( name );
        if ( operationElement == null ) {
            if ( isMandatory ) {
                throw new XMLParsingException( "Mandatory operation '" + name + "' not defined in "
                                               + "'OperationsMetadata'-section." );
            }
        } else {
            // "ows:Parameter"-elements
            List<Element> parameterElements = XMLTools.getElements( operationElement, "ows:Parameter", nsContext );
            OWSDomainType[] parameters = new OWSDomainType[parameterElements.size()];
            for ( int i = 0; i < parameters.length; i++ ) {
                parameters[i] = getOWSDomainType( name, parameterElements.get( i ) );
            }

            DCPType[] dcps = null;
            List<Element> nl = XMLTools.getElements( operationElement, "ows:DCP", nsContext );
            if ( nl.size() > 0 ) {
                dcps = getDCPs( nl );
            } else {
                // add default URLs
                dcps = new DCPType[1];
                Protocol protocol = new HTTP( new URL[] { defaultUrlGet }, new URL[] { defaultUrlPost } );
                dcps[0] = new DCPType( protocol );
            }
            operation = new Operation( name, dcps, parameters );
        }
        return operation;
    }

    /**
     * Returns the object representation for an element of type <code>wfs:FeatureTypeType</code>.
     *
     * @param element
     * @return object representation for the element of type <code>wfs:OperationsType</code>
     * @throws XMLParsingException
     */
    @Override
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
}
