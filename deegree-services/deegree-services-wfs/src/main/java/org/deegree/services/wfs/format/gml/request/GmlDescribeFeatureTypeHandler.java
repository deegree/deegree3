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

package org.deegree.services.wfs.format.gml.request;

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.GML_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XSNS;
import static org.deegree.commons.xml.CommonNamespaces.XS_PREFIX;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.gml.schema.GMLAppSchemaWriter.GML_2_DEFAULT_INCLUDE;
import static org.deegree.gml.schema.GMLAppSchemaWriter.GML_30_DEFAULT_INCLUDE;
import static org.deegree.gml.schema.GMLAppSchemaWriter.GML_31_DEFAULT_INCLUDE;
import static org.deegree.gml.schema.GMLAppSchemaWriter.GML_32_DEFAULT_INCLUDE;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_PREFIX;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.util.base64.Base64EncodingStringBufferOutputStream;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.URITranslator;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLAppSchemaWriter;
import org.deegree.gml.schema.GMLSchemaInfoSet;
import org.deegree.protocol.wfs.WFSConstants;
import org.deegree.protocol.wfs.describefeaturetype.DescribeFeatureType;
import org.deegree.protocol.wfs.describefeaturetype.kvp.DescribeFeatureTypeKVPAdapter;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.i18n.Messages;
import org.deegree.services.resources.ResourcesServlet;
import org.deegree.services.wfs.WebFeatureService;
import org.deegree.services.wfs.WfsFeatureStoreManager;
import org.deegree.services.wfs.format.gml.GmlFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link DescribeFeatureType} requests for the {@link GmlFormat}.
 * 
 * @see GmlFormat
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GmlDescribeFeatureTypeHandler extends AbstractGmlRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger( GmlDescribeFeatureTypeHandler.class );

    private static final String APPSCHEMAS = "appschemas";

    private static final String OGC_SCHEMA_HOST = "http://schemas.opengis.net";

    // URL of workspace appschema directory
    private String wsAppSchemaBaseURL;

    private String ogcSchemaJarBaseURL;

    /**
     * Creates a new {@link GmlDescribeFeatureTypeHandler} instance.
     * 
     * @param gmlFormat
     *            never <code>null</code>
     */
    public GmlDescribeFeatureTypeHandler( GmlFormat gmlFormat ) {
        super( gmlFormat );
        try {
            File wsBaseDir = OGCFrontController.getServiceWorkspace().getLocation();
            File appSchemaBaseDir = new File( wsBaseDir, APPSCHEMAS );
            this.wsAppSchemaBaseURL = appSchemaBaseDir.toURI().toURL().toString();
        } catch ( MalformedURLException e ) {
            throw new RuntimeException( e );
        }
        URL baseUrl = DescribeFeatureType.class.getResource( "/META-INF/SCHEMAS_OPENGIS_NET" );
        if ( baseUrl != null ) {
            this.ogcSchemaJarBaseURL = baseUrl.toString();
            LOG.debug( "GmlDescribeFeatureTypeHandler OGC Schema Jar Base URL: '" + this.ogcSchemaJarBaseURL + "'" );

        }
    }

    /**
     * Performs the given {@link DescribeFeatureType} request.
     * <p>
     * If the request targets feature types in multiple namespaces, a WFS 2.0.0-style wrapper document is generated. The
     * response document embeds all element and type declarations from one of the namespaces and imports the
     * declarations from the other namespaces using a KVP-<code>DescribeFeatureType</code> request that refers back to
     * the service.
     * </p>
     * 
     * @param request
     *            request to be handled, never <code>null</code>
     * @param response
     *            response that is used to write the result, never <code>null</code>
     * @throws OWSException
     *             if a WFS specific exception occurs, e.g. a requested feature type is not served
     * @throws XMLStreamException
     *             if writing the XML response fails
     * @throws IOException
     *             if an IO-error occurs
     */
    public void doDescribeFeatureType( DescribeFeatureType request, HttpResponseBuffer response )
                            throws OWSException, XMLStreamException, IOException {
        LOG.debug( "doDescribeFeatureType: " + request );

        String mimeType = options.getMimeType();
        GMLVersion version = options.getGmlVersion();

        LOG.debug( "contentType:" + response.getContentType() );
        LOG.debug( "characterEncoding:" + response.getCharacterEncoding() );

        XMLStreamWriter writer = WebFeatureService.getXMLResponseWriter( response, mimeType, null );

        doDescribeFeatureType( request, version, writer );

        writer.flush();
    }

    /**
     * Performs the given {@link DescribeFeatureType} request from SOAP.
     * <p>
     * If the request targets feature types in multiple namespaces, a WFS 2.0.0-style wrapper document is generated. The
     * response document embeds all element and type declarations from one of the namespaces and imports the
     * declarations from the other namespaces using a KVP-<code>DescribeFeatureType</code> request that refers back to
     * the service. Result is encoded in base64 and served in wrapping DescribeFeatureTypeResponse element.
     * </p>
     * 
     * @param request
     *            request to be handled, never <code>null</code>
     * @param response
     *            response that is used to write the result, never <code>null</code>
     * @throws OWSException
     *             if a WFS specific exception occurs, e.g. a requested feature type is not served
     * @throws XMLStreamException
     *             if writing the XML response fails
     * @throws IOException
     *             if an IO-error occurs
     */
    public void doDescribeFeatureTypeInSoap( DescribeFeatureType request, HttpResponseBuffer response )
                            throws OWSException, XMLStreamException, IOException {
        LOG.debug( "doDescribeFeatureTypeInSoap: " + request );

        String mimeType = options.getMimeType();
        GMLVersion version = options.getGmlVersion();

        LOG.debug( "contentType:" + response.getContentType() );
        LOG.debug( "characterEncoding:" + response.getCharacterEncoding() );

        XMLStreamWriter writer = WebFeatureService.getXMLResponseWriter( response, mimeType, null );

        StringBuffer base64Schema = new StringBuffer();
        Base64EncodingStringBufferOutputStream base64OutputStream = new Base64EncodingStringBufferOutputStream(
                                                                                                                base64Schema );
        final XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter schemaWriter = factory.createXMLStreamWriter( base64OutputStream );

        // open "wfs:DescribeFeatureTypeResponse" element
        if ( VERSION_200.equals( request.getVersion() ) ) {
            writer.setPrefix( WFS_PREFIX, WFS_200_NS );
            writer.writeStartElement( WFS_200_NS, "DescribeFeatureTypeResponse" );
            writer.writeNamespace( WFS_PREFIX, WFS_200_NS );
        } else {
            writer.setPrefix( WFS_PREFIX, WFS_NS );
            writer.writeStartElement( WFS_NS, "DescribeFeatureTypeResponse" );
            writer.writeNamespace( WFS_PREFIX, WFS_NS );
        }

        doDescribeFeatureType( request, version, schemaWriter );

        // write base64 padding and flush schema to buffer
        schemaWriter.flush();
        base64OutputStream.complete();

        writer.writeCharacters( base64Schema.toString() );
        writer.writeEndElement(); // DescribeFeatureTypeResponse
        writer.flush();
    }

    private void doDescribeFeatureType( DescribeFeatureType request, GMLVersion version, XMLStreamWriter writer )
                    throws XMLStreamException, OWSException, IOException {
        // check for deegree-specific DescribeFeatureType request that asks for the WFS schema in a GML
        // version that does not match the WFS schema (e.g. WFS 1.1.0, GML 2)
        if ( request.getTypeNames() != null && request.getTypeNames().length == 1
             && "FeatureCollection".equals( request.getTypeNames()[0].getLocalPart() )
             && "wfs".equals( request.getTypeNames()[0].getPrefix() ) ) {
            writeWFSSchema( writer, request.getVersion(), version );
        } else {
            Collection<String> namespaces = determineRequiredNamespaces( request );
            String targetNs = namespaces.iterator().next();
            if ( options.isExportOriginalSchema() ) {
                GMLSchemaInfoSet gmlSchema = findGmlSchema( namespaces, version );
                if ( gmlSchema != null ) {
                    exportOriginalInfoSet( writer, gmlSchema, targetNs );
                } else {
                    LOG.warn( "Could not find original schema corresponding to the requested schema, try to reencode the schema!" );
                    reencodeSchema( request, writer, targetNs, namespaces, version );
                }
            } else {
                reencodeSchema( request, writer, targetNs, namespaces, version );
            }
        }
    }

    private GMLSchemaInfoSet findGmlSchema( Collection<String> namespaces, GMLVersion version ) {
        LOG.debug( "Try to find GML schema from store supporting namespaces {}", namespaces );
        WfsFeatureStoreManager storeManager = format.getMaster().getStoreManager();
        for ( FeatureStore store : storeManager.getStores() ) {
            if ( storeSupportsAllRequestedNamespaces( store, namespaces ) ) {
                GMLSchemaInfoSet gmlSchema = store.getSchema().getGMLSchema();
                if ( gmlSchema != null && gmlSchema.getVersion() == version )
                    return gmlSchema;
            }
        }
        return null;
    }

    private boolean storeSupportsAllRequestedNamespaces( FeatureStore store, Collection<String> namespaces ) {
        Set<String> appNamespaces = store.getSchema().getAppNamespaces();
        return appNamespaces.containsAll( namespaces );
    }

    private void reencodeSchema( DescribeFeatureType request, XMLStreamWriter writer, String targetNs,
                                 Collection<String> importNs, GMLVersion version )
                            throws XMLStreamException {

        Map<String, String> importMap = buildImportMap( request, importNs );
        WfsFeatureStoreManager storeManager = format.getMaster().getStoreManager();
        Map<String, String> prefixToNs = storeManager.getPrefixToNs();
        GMLAppSchemaWriter exporter = new GMLAppSchemaWriter( version, targetNs, importMap, prefixToNs );

        List<FeatureType> fts = new ArrayList<FeatureType>();
        for ( FeatureStore fs : storeManager.getStores() ) {
            for ( FeatureType ft : fs.getSchema().getFeatureTypes( targetNs, true, true ) ) {
                fts.add( ft );
            }
        }
        exporter.export( writer, fts );
    }

    /**
     * Writes a response based on the original {@link GMLSchemaInfoSet}.
     * 
     * @param writer
     * @param infoSet
     * @param targetNs
     * @throws XMLStreamException
     */
    private void exportOriginalInfoSet( XMLStreamWriter writer, GMLSchemaInfoSet infoSet, String targetNs )
                    throws XMLStreamException, IOException {
        LOG.debug( "Exporting wrapper schema for original infoset." );
        GMLAppSchemaWriter.export( writer, infoSet, targetNs, new URITranslator() {
            @SuppressWarnings("synthetic-access")
            @Override
            public String translate( String uri ) {
                if ( uri.startsWith( wsAppSchemaBaseURL ) ) {
                    String relativePath = uri.substring( wsAppSchemaBaseURL.length() );
                    if ( options.getAppSchemaBaseURL() == null ) {
                        return ResourcesServlet.getUrl( APPSCHEMAS + "/" + relativePath );
                    }
                    return options.getAppSchemaBaseURL() + "/" + relativePath;
                }
                if ( uri.startsWith( ogcSchemaJarBaseURL ) ) {
                    return translateOGCSchemaLocation( uri );
                }
                return uri;
            }
        } );
    }

    /**
     * Helper method to translate the internal URI given into a remote OGC Schema location. This method ensures there's
     * a starting "/" in the path.
     * 
     * @param uri
     *            The URI String to translate
     * @return The "remote" OGC Schema location.
     */
    private String translateOGCSchemaLocation( String uri ) {

        StringBuilder ogcSchemaLocation = new StringBuilder( OGC_SCHEMA_HOST );
        String ogcSchemaPath = uri.substring( ogcSchemaJarBaseURL.length() );
        if ( !ogcSchemaPath.startsWith( "/" ) ) {
            ogcSchemaLocation.append( "/" );
            LOG.debug( "OGC Schema path from internal Jar did not include starting '/'. Path: '" + ogcSchemaPath
                       + "'. Prefixed with '/' to complete URL...." );
        }
        ogcSchemaLocation.append( ogcSchemaPath );

        LOG.debug( "Translated OGC Schema Jar URL to schemaLocation: '" + ogcSchemaLocation + "'" );

        return ogcSchemaLocation.toString();
        // Old behaviour
        // return "http://schemas.opengis.net" + uri.substring( ogcSchemaJarBaseURL.length() );
    }

    private void writeWFSSchema( XMLStreamWriter writer, Version version, GMLVersion gmlVersion )
                            throws XMLStreamException {

        writer.setPrefix( XS_PREFIX, XSNS );
        writer.writeStartElement( XSNS, "schema" );
        writer.writeNamespace( XS_PREFIX, XSNS );
        writer.writeAttribute( "attributeFormDefault", "unqualified" );
        writer.writeAttribute( "elementFormDefault", "qualified" );

        if ( VERSION_100.equals( version ) || VERSION_110.equals( version ) ) {
            writer.writeAttribute( "targetNamespace", WFS_NS );
            writer.writeNamespace( WFS_PREFIX, WFSConstants.WFS_NS );
        } else if ( VERSION_200.equals( version ) ) {
            writer.writeAttribute( "targetNamespace", WFS_200_NS );
            writer.writeNamespace( WFS_PREFIX, WFSConstants.WFS_200_NS );
        }

        writer.writeNamespace( GML_PREFIX, gmlVersion.getNamespace() );

        // import GML core schema
        String parentElement = null;
        String parentType = null;
        writer.writeEmptyElement( XSNS, "import" );
        writer.writeAttribute( "namespace", gmlVersion.getNamespace() );
        switch ( gmlVersion ) {
        case GML_2:
            parentElement = GML_PREFIX + ":_FeatureCollection";
            parentType = GML_PREFIX + ":AbstractFeatureCollectionType";
            writer.writeAttribute( "schemaLocation", GML_2_DEFAULT_INCLUDE );
            break;
        case GML_30:
            parentElement = GML_PREFIX + ":_FeatureCollection";
            parentType = GML_PREFIX + ":AbstractFeatureCollectionType";
            writer.writeAttribute( "schemaLocation", GML_30_DEFAULT_INCLUDE );
            break;
        case GML_31:
            parentElement = GML_PREFIX + ":_FeatureCollection";
            parentType = GML_PREFIX + ":AbstractFeatureCollectionType";
            writer.writeAttribute( "schemaLocation", GML_31_DEFAULT_INCLUDE );
            break;
        case GML_32:
            // there is no abstract FeatureCollection element in GML 3.2 anymore
            parentElement = GML_PREFIX + ":AbstractFeature";
            parentType = GML_PREFIX + ":AbstractFeatureType";
            writer.writeAttribute( "schemaLocation", GML_32_DEFAULT_INCLUDE );
            break;
        }

        // write wfs:FeatureCollection element declaration
        writer.writeStartElement( XSNS, "element" );
        writer.writeAttribute( "name", "FeatureCollection" );
        writer.writeAttribute( "type", WFS_PREFIX + ":FeatureCollectionType" );
        writer.writeAttribute( "substitutionGroup", parentElement );
        writer.writeEndElement();

        // write wfs:FeatureCollectionType declaration
        writer.writeStartElement( XSNS, "complexType" );
        writer.writeAttribute( "name", "FeatureCollectionType" );
        writer.writeStartElement( XSNS, "complexContent" );
        writer.writeStartElement( XSNS, "extension" );
        writer.writeAttribute( "base", parentType );

        if ( GML_32 == gmlVersion ) {
            // in GML 3.2, FeatureCollections are Features with properties that derive
            // gml:AbstractFeatureMemberType
            writer.writeStartElement( XSNS, "sequence" );
            writer.writeEmptyElement( XSNS, "element" );
            writer.writeAttribute( "name", "member" );
            writer.writeAttribute( "type", WFS_PREFIX + ":FeaturePropertyType" );
            writer.writeAttribute( "minOccurs", "0" );
            writer.writeAttribute( "maxOccurs", "unbounded" );
            writer.writeEndElement();
        }

        writer.writeEmptyElement( XSNS, "attribute" );
        writer.writeAttribute( "name", "lockId" );
        writer.writeAttribute( "type", "xs:string" );
        writer.writeAttribute( "use", "optional" );

        if ( VERSION_110.equals( version ) ) {
            writer.writeEmptyElement( XSNS, "attribute" );
            writer.writeAttribute( "name", "timeStamp" );
            writer.writeAttribute( "type", "xs:dateTime" );
            writer.writeAttribute( "use", "optional" );

            writer.writeEmptyElement( XSNS, "attribute" );
            writer.writeAttribute( "name", "numberOfFeatures" );
            writer.writeAttribute( "type", "xs:nonNegativeInteger" );
            writer.writeAttribute( "use", "optional" );
        }

        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();

        if ( GML_32 == gmlVersion ) {
            // write wfs:FeaturePropertyType declaration
            writer.writeStartElement( XSNS, "complexType" );
            writer.writeAttribute( "name", "FeaturePropertyType" );
            writer.writeStartElement( XSNS, "complexContent" );
            writer.writeStartElement( XSNS, "extension" );
            writer.writeAttribute( "base", GML_PREFIX + ":AbstractFeatureMemberType" );
            writer.writeStartElement( XSNS, "sequence" );
            writer.writeEmptyElement( XSNS, "element" );
            writer.writeAttribute( "ref", GML_PREFIX + ":AbstractFeature" );
            writer.writeAttribute( "minOccurs", "0" );
            writer.writeEndElement();
            writer.writeEmptyElement( XSNS, "attributeGroup" );
            writer.writeAttribute( "ref", GML_PREFIX + ":AssociationAttributeGroup" );
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
        }

        // end 'xs:schema'
        writer.writeEndElement();
    }

    private Map<String, String> buildImportMap( DescribeFeatureType request, Collection<String> namespaces ) {
        Map<String, String> nsToDescribeFtRequest = new HashMap<String, String>();
        Iterator<String> namespaceIter = namespaces.iterator();
        // skip first namespace (will be included directly in output document)
        namespaceIter.next();
        while ( namespaceIter.hasNext() ) {
            String ns = namespaceIter.next();
            String requestURL = buildDescribeFeatureTypeRequest( request, ns );
            nsToDescribeFtRequest.put( ns, requestURL );
        }
        return nsToDescribeFtRequest;
    }

    private String buildDescribeFeatureTypeRequest( DescribeFeatureType request, String namespace ) {

        Map<String, String> nsBindings = new HashMap<String, String>();
        nsBindings.put( "", namespace );
        DescribeFeatureType subRequest = new DescribeFeatureType( request.getVersion(), null,
                                                                  request.getOutputFormat(), null, nsBindings );

        String baseURL = OGCFrontController.getHttpGetURL();
        String paramPart = DescribeFeatureTypeKVPAdapter.export( subRequest, request.getVersion() );
        return baseURL + paramPart;
    }

    /**
     * Determines the application namespaces that have to be included in the generated schema response.
     * 
     * @param request
     * @return key: namespace, value: list of feature types in the namespace
     * @throws OWSException
     */
    private List<String> determineRequiredNamespaces( DescribeFeatureType request )
                            throws OWSException {

        Set<String> set;
        WfsFeatureStoreManager storeManager = format.getMaster().getStoreManager();
        if ( request.getTypeNames() == null || request.getTypeNames().length == 0 ) {
            if ( request.getNsBindings() == null ) {
                set = getAppSchemaNamespaces();
            } else {
                set = getExplicitlyRequestedNamespaces( request, storeManager );
            }
        } else {
            set = getNamespacesForRequestedFeatureTypes( request, storeManager );
        }

        // add dependent namespaces
        Set<String> add = findUnhandledNs( set );
        while ( !add.isEmpty() ) {
            set.addAll( add );
            add = findUnhandledNs( set );
        }

        set.remove( GMLNS );
        set.remove( GML3_2_NS );

        return new ArrayList<String>( set );
    }

    private Set<String> getNamespacesForRequestedFeatureTypes( DescribeFeatureType request,
                                                               WfsFeatureStoreManager storeManager )
                            throws OWSException {
        Set<String> set = new LinkedHashSet<String>();
        LOG.debug( "Adding namespaces of requested feature types." );
        for ( QName ftName : request.getTypeNames() ) {
            FeatureType ft = storeManager.lookupFeatureType( ftName );
            if ( ft == null ) {
                throw new OWSException( Messages.get( "WFS_FEATURE_TYPE_NOT_SERVED", ftName ),
                                        OWSException.INVALID_PARAMETER_VALUE, "typenames" );
            }
            set.add( ft.getName().getNamespaceURI() );
        }
        return set;
    }

    private Set<String> getExplicitlyRequestedNamespaces( DescribeFeatureType request,
                                                          WfsFeatureStoreManager storeManager ) {

        Set<String> set = new LinkedHashSet<String>();
        for ( String ns : request.getNsBindings().values() ) {
            for ( FeatureStore fs : storeManager.getStores() ) {
                AppSchema schema = fs.getSchema();
                if ( schema.getNamespaceBindings().values().contains( ns ) ) {
                    set.add( ns );
                    break;
                }
            }
        }
        return set;
    }

    private Set<String> findUnhandledNs( Set<String> set ) {
        Set<String> dependentNamespaces = new HashSet<String>();
        WfsFeatureStoreManager storeManager = format.getMaster().getStoreManager();
        for ( String ns : set ) {
            for ( FeatureStore fs : storeManager.getStores() ) {
                AppSchema schema = fs.getSchema();
                List<String> depNs = schema.getNamespacesDependencies( ns );
                for ( String n : depNs ) {
                    if ( !set.contains( n ) ) {
                        dependentNamespaces.add( n );
                    }
                }
            }
        }
        return dependentNamespaces;
    }

}