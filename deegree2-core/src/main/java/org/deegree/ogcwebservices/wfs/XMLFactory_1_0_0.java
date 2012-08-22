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
package org.deegree.ogcwebservices.wfs;

import static org.deegree.framework.xml.XMLTools.appendElement;
import static org.deegree.ogcbase.CommonNamespaces.OGCNS;
import static org.deegree.ogcbase.CommonNamespaces.OGC_PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.WFS_PREFIX;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.xlink.SimpleLink;
import org.deegree.enterprise.DeegreeParams;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.FeatureId;
import org.deegree.model.filterencoding.Function;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.metadata.iso19115.Linkage;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Position;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.Protocol;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.wfs.capabilities.FeatureTypeList;
import org.deegree.ogcwebservices.wfs.capabilities.Operation;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.deegree.ogcwebservices.wfs.capabilities.WFSOperationsMetadata;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfiguration;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.GetFeatureDocument;
import org.deegree.ogcwebservices.wfs.operation.LockFeatureResponse;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.deegree.ogcwebservices.wfs.operation.GetFeature.RESULT_TYPE;
import org.deegree.ogcwebservices.wfs.operation.transaction.Insert;
import org.deegree.ogcwebservices.wfs.operation.transaction.InsertResults;
import org.deegree.ogcwebservices.wfs.operation.transaction.Transaction;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionOperation;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionResponse;
import org.deegree.ogcwebservices.wfs.operation.transaction.Update;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Responsible for the generation of XML representations of objects from the WFS context that comply to WFS
 * specification 1.0.0.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class XMLFactory_1_0_0 {

    private static final URI WFSNS = CommonNamespaces.WFSNS;

    // private static final String PRE_WFS = CommonNamespaces.WFS_PREFIX + ":";

    private static final ILogger LOG = LoggerFactory.getLogger( XMLFactory_1_0_0.class );

    private static XMLFactory_1_0_0 factory = null;

    /**
     * @return a cached instance of this XMLFactory.
     */
    public static synchronized XMLFactory_1_0_0 getInstance() {
        if ( factory == null ) {
            factory = new XMLFactory_1_0_0();
        }
        return factory;
    }

    /**
     * Exports a <code>WFSCapabilities</code> instance to a <code>WFSCapabilitiesDocument</code> with version 1_0.
     *
     * @param config
     * @return DOM representation of the <code>WFSCapabilities</code>
     */
    public WFSCapabilitiesDocument export( WFSConfiguration config ) {

        WFSCapabilitiesDocument capabilitiesDocument = new WFSCapabilitiesDocument();

        capabilitiesDocument.createEmptyDocument( "1.0.0" );
        Element root = capabilitiesDocument.getRootElement();

        // Find the default online resource
        DeegreeParams deegreeParams = config.getDeegreeParams();
        String defaultOnlineResource = "http://localhost:8080/deegree/services";
        if ( deegreeParams != null ) {
            OnlineResource or = deegreeParams.getDefaultOnlineResource();
            if ( or != null ) {
                Linkage link = or.getLinkage();
                if ( link != null ) {
                    URL uri = link.getHref();
                    if ( uri != null ) {
                        String tmp = uri.toExternalForm();
                        if ( !"".equals( tmp.toString() ) ) {
                            defaultOnlineResource = tmp;
                        }
                    }
                }
            }
        }

        ServiceIdentification serviceIdentification = config.getServiceIdentification();
        ServiceProvider serviceProvider = config.getServiceProvider();
        OperationsMetadata operationsMetadata = config.getOperationsMetadata();
        FeatureTypeList featureTypeList = config.getFeatureTypeList();

        if ( serviceIdentification != null ) {
            String onlineResource = null;
            if ( serviceProvider != null ) {
                SimpleLink resource = serviceProvider.getProviderSite();
                URI online = resource.getHref();
                onlineResource = online.toASCIIString();
            } else {
                onlineResource = defaultOnlineResource;
            }
            appendService( root, serviceIdentification, onlineResource );
        }

        // Add the capabilities element
        if ( operationsMetadata != null ) {
            appendCapability( root, (WFSOperationsMetadata) operationsMetadata, defaultOnlineResource );
        }

        if ( featureTypeList != null ) {
            appendFeatureTypeList( root, featureTypeList );
        }

        FilterCapabilities filterCapabilities = config.getFilterCapabilities();
        if ( filterCapabilities != null ) {
            org.deegree.model.filterencoding.XMLFactory.appendFilterCapabilities100( root, filterCapabilities );
        }
        return capabilitiesDocument;
    }

    /**
     * Appends the DOM representation of the {@link ServiceIdentification} section to the passed {@link Element}.
     *
     * @param root
     *            to which to append the service section.
     * @param serviceIdentification
     *            bean of the service identification element.
     * @param onlineResource
     *            to be used as a default onlineResource.
     */
    private void appendService( Element root, ServiceIdentification serviceIdentification, String onlineResource ) {

        // 'Service'-element
        Element service = XMLTools.appendElement( root, WFSNS, "Service" );

        // 'Name'-element
        String tmpValue = serviceIdentification.getName();
        tmpValue = checkForEmptyValue( tmpValue, "Name", "WFS" );
        XMLTools.appendElement( service, WFSNS, "Name", tmpValue );

        tmpValue = serviceIdentification.getTitle();
        tmpValue = checkForEmptyValue( tmpValue, "Title", "A Web Feature Service" );
        XMLTools.appendElement( service, WFSNS, "Title", tmpValue );

        tmpValue = serviceIdentification.getAbstract();
        if ( tmpValue != null && "".equals( tmpValue.trim() ) ) {
            XMLTools.appendElement( service, WFSNS, "Abstract", tmpValue );
        }

        Keywords[] keywords = serviceIdentification.getKeywords();
        appendKeyWords( service, keywords );

        XMLTools.appendElement( service, WFSNS, "OnlineResource", onlineResource );

        tmpValue = serviceIdentification.getFees();
        if ( tmpValue == null || "".equals( tmpValue.trim() ) ) {
            tmpValue = "NONE";
        }
        XMLTools.appendElement( service, WFSNS, "Fees", tmpValue );

        String[] constraints = serviceIdentification.getAccessConstraints();
        StringBuffer sb = new StringBuffer();
        if ( constraints == null || constraints.length > 0 ) {
            sb.append( "NONE" );
        } else {
            for ( int i = 0; i < constraints.length; ++i ) {
                String constraint = constraints[i];
                if ( constraint != null && "".equals( constraint.trim() ) ) {
                    sb.append( constraint );
                    if ( ( i + 1 ) < constraints.length ) {
                        sb.append( " " );
                    }
                }
            }
            if ( sb.length() == 0 ) {
                sb.append( "NONE" );
            }
        }
        XMLTools.appendElement( service, WFSNS, "AccessConstraints", sb.toString() );

    }

    /**
     * Appends the wfs:Capability element to the root element
     *
     * @param root
     * @param operationsMetadata
     */
    private void appendCapability( Element root, WFSOperationsMetadata operationsMetadata, String defaultOnlineResource ) {
        Element capability = XMLTools.appendElement( root, WFSNS, "Capability" );
        Element request = XMLTools.appendElement( capability, WFSNS, "Request" );
        org.deegree.ogcwebservices.getcapabilities.Operation[] ops = operationsMetadata.getOperations();
        if ( ops != null && ops.length > 0 ) {
            for ( org.deegree.ogcwebservices.getcapabilities.Operation op : ops ) {
                String name = op.getName();
                if ( !( name == null || "".equals( name.trim() ) || "GetGMLObject".equals( name.trim() ) ) ) {
                    name = name.trim();

                    Element operation = XMLTools.appendElement( request, WFSNS, name );
                    if ( "DescribeFeatureType".equalsIgnoreCase( name ) ) {
                        Element sdl = XMLTools.appendElement( operation, WFSNS, "SchemaDescriptionLanguage" );
                        XMLTools.appendElement( sdl, WFSNS, "XMLSCHEMA" );
                    } else if ( "GetFeature".equalsIgnoreCase( name ) || "GetFeatureWithLock".equalsIgnoreCase( name ) ) {
                        Element resultFormat = XMLTools.appendElement( operation, WFSNS, "ResultFormat" );
                        XMLTools.appendElement( resultFormat, WFSNS, "GML2" );
                    }
                    DCPType[] dcpTypes = op.getDCPs();
                    if ( dcpTypes != null && dcpTypes.length > 0 ) {
                        for ( DCPType dcpType : dcpTypes ) {
                            appendDCPType( operation, dcpType, defaultOnlineResource );
                        }
                    } else {
                        appendDCPType( operation, null, defaultOnlineResource );
                    }
                }
            }
        }

    }

    /**
     * Appends the XML representation of the <code>wfs:FeatureTypeList</code>- section to the passed
     * <code>Element</code>.
     *
     * @param root
     * @param featureTypeList
     */
    private void appendFeatureTypeList( Element root, FeatureTypeList featureTypeList ) {
        Element featureTypeListNode = XMLTools.appendElement( root, WFSNS, "FeatureTypeList" );
        Operation[] operations = featureTypeList.getGlobalOperations();
        if ( operations != null ) {
            Element operationsNode = XMLTools.appendElement( featureTypeListNode, WFSNS, "Operations" );
            for ( int i = 0; i < operations.length; i++ ) {
                XMLTools.appendElement( operationsNode, WFSNS, operations[i].getOperation() );
            }
        }
        WFSFeatureType[] featureTypes = featureTypeList.getFeatureTypes();
        if ( featureTypes != null ) {
            for ( int i = 0; i < featureTypes.length; i++ ) {
                appendWFSFeatureType( featureTypeListNode, featureTypes[i] );
            }
        }
    }

    /**
     * Appends the DCPType in the WFS namespace... pre-ows stuff.
     *
     * @param operation
     *            to add the dcptype to.
     * @param type
     *            a bean containing necessary information if <code>null</code> a http-get/post dcp with the
     *            defaultonline resource will be inserted.
     * @param defaultOnlineResource
     *            if no dcpType is given or no URL were inserted in the config, this will be inserted.
     */
    private void appendDCPType( Element operation, DCPType type, String defaultOnlineResource ) {
        Element dcpType = XMLTools.appendElement( operation, WFSNS, "DCPType" );
        Element http = XMLTools.appendElement( dcpType, WFSNS, "HTTP" );
        boolean appendDefaultProtocol = true;
        if ( type != null ) {
            Protocol pr = type.getProtocol();
            if ( pr != null ) {
                if ( pr instanceof HTTP ) {
                    HTTP prot = (HTTP) pr;
                    URL[] getters = prot.getGetOnlineResources();
                    URL[] posters = prot.getPostOnlineResources();
                    if ( ( getters != null && getters.length > 0 ) ) {
                        for ( URL get : getters ) {
                            appendGetURL( http, get.toExternalForm() );
                        }
                    } else {
                        appendGetURL( http, defaultOnlineResource );
                    }
                    if ( posters != null && posters.length > 0 ) {
                        for ( URL post : posters ) {
                            appendPostURL( http, post.toExternalForm() );
                        }
                    } else {
                        appendPostURL( http, defaultOnlineResource );
                    }
                    appendDefaultProtocol = false;
                }
            }
        }
        if ( appendDefaultProtocol ) {
            appendGetURL( http, defaultOnlineResource );
            appendPostURL( http, defaultOnlineResource );
        }
    }

    private void appendGetURL( Element http, String resourceURL ) {
        Element get = XMLTools.appendElement( http, WFSNS, "Get" );
        get.setAttribute( "onlineResource", resourceURL );
    }

    private void appendPostURL( Element http, String resourceURL ) {
        Element post = XMLTools.appendElement( http, WFSNS, "Post" );
        post.setAttribute( "onlineResource", resourceURL );
    }

    private void appendKeyWords( Element root, Keywords[] keywords ) {
        if ( keywords != null && keywords.length > 0 ) {
            StringBuffer sb = new StringBuffer();
            for ( int k = 0; k < keywords.length; ++k ) {
                String[] words = keywords[k].getKeywords();
                if ( words != null && words.length > 0 ) {
                    for ( int i = 0; i < words.length; ++i ) {
                        sb.append( words[i] );
                        if ( ( i + 1 ) < words.length ) {
                            sb.append( " " );
                        }
                    }
                }
                if ( ( k + 1 ) < keywords.length ) {
                    sb.append( " " );
                }
            }
            XMLTools.appendElement( root, WFSNS, "Keywords", sb.toString() );
        }
    }

    /**
     * Appends the XML representation of the <code>WFSFeatureType</code> instance to the passed <code>Element</code>.
     *
     * @param root
     * @param featureType
     */
    private void appendWFSFeatureType( Element root, WFSFeatureType featureType ) {

        Element featureTypeNode = XMLTools.appendElement( root, WFSNS, "FeatureType" );

        if ( featureType.getName().getPrefix() != null ) {
            XMLTools.appendNSBinding( featureTypeNode, featureType.getName().getPrefix(),
                                      featureType.getName().getNamespace() );
        }
        XMLTools.appendElement( featureTypeNode, WFSNS, "Name", featureType.getName().getPrefixedName() );
        XMLTools.appendElement( featureTypeNode, WFSNS, "Title", featureType.getTitle() );
        String tmpValue = featureType.getAbstract();
        if ( tmpValue != null && !"".equals( tmpValue.trim() ) ) {
            XMLTools.appendElement( featureTypeNode, WFSNS, "Abstract", tmpValue );
        }
        Keywords[] keywords = featureType.getKeywords();
        appendKeyWords( featureTypeNode, keywords );

        URI defaultSrs = featureType.getDefaultSRS();
        if ( defaultSrs != null ) {
            XMLTools.appendElement( featureTypeNode, WFSNS, "SRS", defaultSrs.toASCIIString() );
        } else if ( featureType.getOtherSrs() != null && featureType.getOtherSrs().length > 0 ) {
            for ( URI srs : featureType.getOtherSrs() ) {
                if ( srs != null ) {
                    XMLTools.appendElement( featureTypeNode, WFSNS, "SRS", srs.toASCIIString() );
                    break;
                }
            }
        } else {
            // defaulting to EPSG:4326 is this correct????
            LOG.logDebug( "Found no default- or other-SRS, setting to EPGS:4326, is this correct?" );
            XMLTools.appendElement( featureTypeNode, WFSNS, "SRS", "EPSG:4326" );
        }
        Operation[] operations = featureType.getOperations();
        if ( operations != null && operations.length > 0 ) {
            Element opEl = XMLTools.appendElement( featureTypeNode, WFSNS, "Operations" );
            for ( Operation op : operations ) {
                if ( op != null && !"".equals( op.getOperation().trim() )
                     && !"GetGMLObject".equalsIgnoreCase( op.getOperation().trim() ) ) {
                    XMLTools.appendElement( opEl, WFSNS, op.getOperation().trim() );

                }
            }
        }

        Envelope[] wgs84BoundingBoxes = featureType.getWgs84BoundingBoxes();
        for ( Envelope bbox : wgs84BoundingBoxes ) {
            if ( bbox != null && bbox.getMin().getCoordinateDimension() == 2 ) {
                Element latlon = XMLTools.appendElement( featureTypeNode, WFSNS, "LatLongBoundingBox" );
                Position min = bbox.getMin();
                Position max = bbox.getMax();
                latlon.setAttribute( "minx", Double.toString( min.getX() ) );
                latlon.setAttribute( "miny", Double.toString( min.getY() ) );
                latlon.setAttribute( "maxx", Double.toString( max.getX() ) );
                latlon.setAttribute( "maxy", Double.toString( max.getY() ) );
            }
        }
        MetadataURL[] mdURLs = featureType.getMetadataUrls();
        if ( mdURLs != null ) {
            for ( MetadataURL mdURL : mdURLs ) {
                if ( mdURL != null && mdURL.getOnlineResource() != null ) {
                    // first check if the format and type are acceptable.
                    String format = mdURL.getFormat();
                    boolean formatOK = true;
                    if ( format != null ) {
                        if ( "text/xml".equals( format ) ) {
                            format = "XML";
                        } else if ( "text/sgml".equals( format ) ) {
                            format = "SGML";
                        } else if ( "text/plain".equals( format ) ) {
                            format = "TXT";
                        } else {
                            formatOK = false;
                        }
                    }

                    String type = mdURL.getType();
                    boolean typeOK = true;
                    if ( type != null ) {
                        if ( !( "TC211".equals( type ) || "FGDC".equals( type ) ) ) {
                            typeOK = false;
                        }
                    }
                    if ( formatOK && typeOK ) {
                        Element metadata = XMLTools.appendElement( featureTypeNode, WFSNS, "MetadataURL",
                                                                   mdURL.getOnlineResource().toExternalForm() );
                        metadata.setAttribute( "type", type );
                        metadata.setAttribute( "format", format );
                    }
                }
            }
        }
    }

    private String checkForEmptyValue( String value, String elementName, String defaultValue ) {
        if ( value == null || "".equals( value.trim() ) ) {
            LOG.logError( Messages.getMessage( "WFS_MISSING_REQUIRED_ELEMENT", elementName ) );
            value = defaultValue;
        }
        return value;
    }

    /**
     * @param response
     * @return a WFS 1.0.0 style response
     */
    public static XMLFragment export( TransactionResponse response ) {

        XMLFragment doc = new XMLFragment( new QualifiedName( WFS_PREFIX, "WFS_TransactionResponse", WFSNS ) );

        Element root = doc.getRootElement();
        root.setAttribute( "version", "1.0.0" );

        if ( response.getInsertResults().size() > 0 ) {
            Element e = appendElement( root, WFSNS, WFS_PREFIX + ":InsertResult" );
            // this is obviously not always correct, but it works around limitations in the test scripts...
            e.setAttribute( "handle", response.getInsertResults().get( 0 ).getHandle() );
            for ( InsertResults res : response.getInsertResults() ) {
                for ( FeatureId fid : res.getFeatureIDs() ) {
                    appendElement( e, OGCNS, OGC_PREFIX + ":FeatureId" ).setAttribute( "fid", fid.getAsString() );
                }
            }
        }

        Element e = appendElement( root, WFSNS, WFS_PREFIX + ":TransactionResult" );
        List<Exception> exceptions = response.getExceptions();
        String status = exceptions == null || exceptions.size() == 0 ? "SUCCESS" : null;

        if ( exceptions != null && exceptions.size() > 0
             && ( response.getTotalDeleted() > 0 || response.getTotalInserted() > 0 || response.getTotalUpdated() > 0 ) ) {
            status = "PARTIAL";
        }
        if ( exceptions != null && exceptions.size() > 0
             && ( response.getTotalDeleted() + response.getTotalInserted() + response.getTotalUpdated() == 0 ) ) {
            status = "FAILED";
        }

        if ( response.getTotalDeleted() + response.getTotalInserted() + response.getTotalUpdated() == 0 ) {
            Transaction t = (Transaction) response.getRequest();
            boolean delOnly = true;
            for ( TransactionOperation op : t.getOperations() ) {
                if ( op instanceof Update || op instanceof Insert ) {
                    delOnly = false;
                }
            }
            status = delOnly ? "SUCCESS" : "FAILED";
        }

        e = appendElement( e, WFSNS, WFS_PREFIX + ":Status" );
        appendElement( e, WFSNS, WFS_PREFIX + ":" + ( status == null ? "FAILED" : status ) );

        return doc;
    }

    /**
     * @param response
     * @return a WFS 1.0.0 style lock feature response
     */
    public static XMLFragment export( LockFeatureResponse response ) {
        XMLFragment doc = new XMLFragment( new QualifiedName( WFS_PREFIX, "WFS_LockFeatureResponse", WFSNS ) );

        Element root = doc.getRootElement();

        appendElement( root, WFSNS, WFS_PREFIX + ":LockId", response.getLockId() );
        if ( response.getFeaturesLocked() != null && response.getFeaturesLocked().length > 0 ) {
            Element e = appendElement( root, WFSNS, WFS_PREFIX + ":FeaturesLocked" );
            for ( String fid : response.getFeaturesLocked() ) {
                appendElement( e, OGCNS, OGC_PREFIX + ":FeatureId" ).setAttribute( "fid", fid );
            }
        }
        if ( response.getFeaturesNotLocked() != null && response.getFeaturesNotLocked().length > 0 ) {
            Element e = appendElement( root, WFSNS, WFS_PREFIX + ":FeaturesNotLocked" );
            for ( String fid : response.getFeaturesNotLocked() ) {
                appendElement( e, OGCNS, OGC_PREFIX + ":FeatureId" ).setAttribute( "fid", fid );
            }
        }

        return doc;
    }

    /**
     * Exports a <code>GetFeature</code> instance to a <code>GetFeatureDocument</code>.
     *
     * @param getFeature
     *            request to be exported
     * @return XML representation of the <code>GetFeature</code> request
     * @throws IOException
     * @throws XMLParsingException
     */
    public static GetFeatureDocument export( GetFeature getFeature )
                            throws IOException, XMLParsingException {

        GetFeatureDocument xml = new GetFeatureDocument();
        try {
            xml.load( XMLFactory.class.getResource( "GetFeatureTemplate.xml" ) );
        } catch ( SAXException e ) {
            throw new XMLParsingException( "could not parse GetFeatureTemplate.xml", e );
        }
        Element root = xml.getRootElement();
        root.setAttribute( "outputFormat", getFeature.getOutputFormat() );
        root.setAttribute( "service", "WFS" );
        root.setAttribute( "version", getFeature.getVersion() );
        if ( getFeature.getHandle() != null ) {
            root.setAttribute( "handle", getFeature.getHandle() );
        }
        if ( getFeature.getResultType() == RESULT_TYPE.HITS ) {
            root.setAttribute( "resultType", "hits" );
        } else {
            root.setAttribute( "resultType", "results" );
        }
        root.setAttribute( "maxFeatures", "" + getFeature.getMaxFeatures() );
        if ( getFeature.getStartPosition() > 0 ) {
            root.setAttribute( "startPosition", "" + getFeature.getStartPosition() );
        }
        if ( getFeature.getTraverseXLinkDepth() > 0 ) {
            root.setAttribute( "traverseXLinkDepth", "" + getFeature.getTraverseXLinkDepth() );
        }
        if ( getFeature.getTraverseXLinkExpiry() > 0 ) {
            root.setAttribute( "traverseXLinkExpiry", "" + getFeature.getTraverseXLinkExpiry() );
        }
        Query[] queries = getFeature.getQuery();
        for ( int i = 0; i < queries.length; i++ ) {
            appendQuery( root, queries[i] );
        }
        return xml;
    }

    /**
     * Appends the XML representation of the given {@link Query} instance to an element.
     *
     * @param query
     */
    private static void appendQuery( Element root, Query query )
                            throws IOException, XMLParsingException {

        Element queryElem = XMLTools.appendElement( root, WFSNS, "Query" );
        if ( query.getHandle() != null ) {
            queryElem.setAttribute( "handle", query.getHandle() );
        }
        if ( query.getFeatureVersion() != null ) {
            queryElem.setAttribute( "featureVersion", query.getFeatureVersion() );
        }
        QualifiedName[] qn = query.getTypeNames();
        String[] na = new String[qn.length];
        for ( int i = 0; i < na.length; i++ ) {
            na[i] = qn[i].getPrefixedName();
            if ( qn[i].getNamespace() != null ) {
                queryElem.setAttribute( "xmlns:" + qn[i].getPrefix(), qn[i].getNamespace().toASCIIString() );
            }
        }
        String tn = StringTools.arrayToString( na, ',' );
        queryElem.setAttribute( "typeName", tn );

        if ( query.getSrsName() != null ) {
            queryElem.setAttribute( "srsName", query.getSrsName() );
        }

        String[] aliases = query.getAliases();
        if ( aliases != null && aliases.length != 0 ) {
            StringBuffer aliasesList = new StringBuffer( aliases[0] );
            for ( int i = 1; i < aliases.length; i++ ) {
                aliasesList.append( ' ' );
                aliasesList.append( aliases[i] );
            }
            queryElem.setAttribute( "aliases", aliasesList.toString() );
        }

        PropertyPath[] propertyNames = query.getPropertyNames();
        for ( int i = 0; i < propertyNames.length; i++ ) {
            Element propertyNameElement = appendElement( queryElem, OGCNS, OGC_PREFIX + ":PropertyName" );
            appendPropertyPath( propertyNameElement, propertyNames[i] );
        }
        Function[] fn = query.getFunctions();
        // copy function definitions into query node
        if ( fn != null ) {
            for ( int i = 0; i < fn.length; i++ ) {
                StringReader sr = new StringReader( fn[i].toXML().toString() );
                Document doc;
                try {
                    doc = XMLTools.parse( sr );
                } catch ( SAXException e ) {
                    throw new XMLParsingException( "could not parse filter function", e );
                }
                XMLTools.copyNode( doc.getDocumentElement(), queryElem );
            }
        }
        // copy filter into query node
        if ( query.getFilter() != null ) {
            StringReader sr = new StringReader( query.getFilter().to100XML().toString() );
            Document doc;
            try {
                doc = XMLTools.parse( sr );
            } catch ( SAXException e ) {
                throw new XMLParsingException( "could not parse filter", e );
            }
            Element elem = XMLTools.appendElement( queryElem, OGCNS, "ogc:Filter" );
            XMLTools.copyNode( doc.getDocumentElement(), elem );
        }

        SortProperty[] sp = query.getSortProperties();
        if ( sp != null ) {
            Element sortBy = XMLTools.appendElement( queryElem, OGCNS, "ogc:SortBy" );
            for ( int i = 0; i < sp.length; i++ ) {
                Element sortProp = XMLTools.appendElement( sortBy, OGCNS, "ogc:SortProperty" );
                XMLTools.appendElement( sortProp, OGCNS, "ogc:PropertyName", sp[i].getSortProperty().getAsString() );
                if ( !sp[i].getSortOrder() ) {
                    XMLTools.appendElement( sortProp, OGCNS, "ogc:SortOrder", "DESC" );
                }
            }
        }
    }

    /**
     * Appends the <code>DOM</code> representation of the given <code>PropertyPath</code> as a new text node to the
     * given element (including necessary namespace bindings).
     *
     * @param element
     *            Element node where the PropertyPath is appended to
     * @param propertyPath
     */
    protected static void appendPropertyPath( Element element, PropertyPath propertyPath ) {
        StringBuffer sb = new StringBuffer();
        sb.append( propertyPath );

        Text textNode = element.getOwnerDocument().createTextNode( sb.toString() );
        element.appendChild( textNode );
        XMLTools.appendNSBindings( element, propertyPath.getNamespaceContext() );
    }
}
