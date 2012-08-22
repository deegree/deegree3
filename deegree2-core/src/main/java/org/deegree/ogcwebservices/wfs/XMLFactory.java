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
import static org.deegree.ogcbase.CommonNamespaces.OGC_PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.WFSNS;
import static org.deegree.ogcbase.CommonNamespaces.WFS_PREFIX;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLException;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.io.datastore.FeatureId;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureException;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureAdapter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.Function;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcbase.XLinkPropertyPath;
import org.deegree.ogcwebservices.getcapabilities.Contents;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.wfs.capabilities.FeatureTypeList;
import org.deegree.ogcwebservices.wfs.capabilities.FormatType;
import org.deegree.ogcwebservices.wfs.capabilities.GMLObject;
import org.deegree.ogcwebservices.wfs.capabilities.Operation;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.GetFeatureDocument;
import org.deegree.ogcwebservices.wfs.operation.Lock;
import org.deegree.ogcwebservices.wfs.operation.LockFeature;
import org.deegree.ogcwebservices.wfs.operation.LockFeatureDocument;
import org.deegree.ogcwebservices.wfs.operation.LockFeatureResponse;
import org.deegree.ogcwebservices.wfs.operation.LockFeatureResponseDocument;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.deegree.ogcwebservices.wfs.operation.GetFeature.RESULT_TYPE;
import org.deegree.ogcwebservices.wfs.operation.transaction.Delete;
import org.deegree.ogcwebservices.wfs.operation.transaction.Insert;
import org.deegree.ogcwebservices.wfs.operation.transaction.InsertResults;
import org.deegree.ogcwebservices.wfs.operation.transaction.Transaction;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionDocument;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionOperation;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionResponse;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionResponseDocument;
import org.deegree.ogcwebservices.wfs.operation.transaction.Update;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Responsible for the generation of XML representations of objects from the WFS context.
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class XMLFactory extends org.deegree.owscommon.XMLFactory {

    private static final URI WFS = CommonNamespaces.WFSNS;

    private static final URI OGCNS = CommonNamespaces.OGCNS;

    // private static final String PRE_WFS = CommonNamespaces.WFS_PREFIX + ":";

    private static final ILogger LOG = LoggerFactory.getLogger( XMLFactory.class );

    /**
     * Exports a <code>WFSCapabilities</code> instance to a <code>WFSCapabilitiesDocument</code>.
     * 
     * @param capabilities
     * @return DOM representation of the <code>WFSCapabilities</code>
     * @throws IOException
     *             if XML template could not be loaded
     */
    public static WFSCapabilitiesDocument export( WFSCapabilities capabilities )
                            throws IOException {
        return export( capabilities, true, true, true, true );
    }

    /**
     * Exports a <code>WFSCapabilities</code> instance to a <code>WFSCapabilitiesDocument</code>.
     * 
     * @param capabilities
     * @param sections
     *            names of sections to be exported, may contain 'All'
     * @return DOM representation of the <code>WFSCapabilities</code>
     * @throws IOException
     *             if XML template could not be loaded
     */
    public static WFSCapabilitiesDocument export( WFSCapabilities capabilities, String[] sections )
                            throws IOException {

        if ( sections == null || sections.length == 0 ) {
            return export( capabilities );
        }

        if ( sections.length == 1 && sections[0].equalsIgnoreCase( "all" ) ) {
            return export( capabilities );
        }

        boolean ident = false, provider = false, md = false, ftlist = false;

        HashSet<String> set = new HashSet<String>();
        for ( String s : sections ) {
            set.add( s.toLowerCase() );
        }

        LOG.logDebug( "The set of requested sections was", set );

        if ( set.contains( "serviceidentification" ) ) {
            ident = true;
        }
        if ( set.contains( "serviceprovider" ) ) {
            provider = true;
        }
        if ( set.contains( "operationsmetadata" ) ) {
            md = true;
        }
        if ( set.contains( "featuretypelist" ) ) {
            ftlist = true;
        }

        return export( capabilities, ident, provider, md, ftlist );
    }

    /**
     * @param capabilities
     * @param serviceIdentification
     * @param serviceProvider
     * @param operationsMetadata
     * @param featureTypeList
     * @return the exported capabilities document (possibly missing some sections if one of the flags was set to false)
     * @throws IOException
     */
    public static WFSCapabilitiesDocument export( WFSCapabilities capabilities, boolean serviceIdentification,
                                                  boolean serviceProvider, boolean operationsMetadata,
                                                  boolean featureTypeList )
                            throws IOException {
        WFSCapabilitiesDocument capabilitiesDocument = new WFSCapabilitiesDocument();

        try {
            capabilitiesDocument.createEmptyDocument();
            Element root = capabilitiesDocument.getRootElement();

            root.setAttribute( "updateSequence", capabilities.getUpdateSequence() );

            if ( serviceIdentification ) {
                ServiceIdentification si = capabilities.getServiceIdentification();
                if ( si != null ) {
                    appendServiceIdentification( root, si );
                }
            }

            if ( serviceProvider ) {
                ServiceProvider sp = capabilities.getServiceProvider();
                if ( sp != null ) {
                    appendServiceProvider( root, sp );
                }
            }

            if ( operationsMetadata ) {
                OperationsMetadata om = capabilities.getOperationsMetadata();
                if ( om != null ) {
                    appendOperationsMetadata( root, om );
                }
            }

            if ( featureTypeList ) {
                FeatureTypeList ftl = capabilities.getFeatureTypeList();
                if ( ftl != null ) {
                    appendFeatureTypeList( root, ftl );
                }
            }

            GMLObject[] servesGMLObjectTypes = capabilities.getServesGMLObjectTypeList();
            if ( servesGMLObjectTypes != null ) {
                appendGMLObjectTypeList( root, WFS, "ServesGMLObjectTypeList", servesGMLObjectTypes );
            }
            GMLObject[] supportsGMLObjectTypes = capabilities.getSupportsGMLObjectTypeList();
            if ( supportsGMLObjectTypes != null ) {
                appendGMLObjectTypeList( root, WFS, "SupportsGMLObjectTypeList", supportsGMLObjectTypes );
            }
            Contents contents = capabilities.getContents();
            if ( contents != null ) {
                // appendContents(root, contents);
            }

            FilterCapabilities fc = capabilities.getFilterCapabilities();
            if ( fc != null ) {
                org.deegree.model.filterencoding.XMLFactory.appendFilterCapabilities110( root, fc );
            }

        } catch ( SAXException e ) {
            LOG.logError( e.getMessage(), e );
        }
        return capabilitiesDocument;
    }

    /**
     * Appends the DOM representation of the {@link ServiceIdentification} section to the passed {@link Element}.
     * 
     * @param root
     * @param serviceIdentification
     */
    protected static void appendServiceIdentification( Element root, ServiceIdentification serviceIdentification ) {

        // 'ServiceIdentification'-element
        Element serviceIdentificationNode = XMLTools.appendElement( root, OWSNS, "ows:ServiceIdentification" );

        // the optional title element
        String tmp = serviceIdentification.getTitle();
        if ( tmp != null && !"".equals( tmp ) ) {
            XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:Title", tmp );
        }

        // the optional abstract element
        tmp = serviceIdentification.getAbstract();
        if ( tmp != null && !"".equals( tmp ) ) {
            XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:Abstract", tmp );
        }

        // the optional keywords element
        appendKeywords( serviceIdentificationNode, serviceIdentification.getKeywords(), OWSNS );

        // 'ServiceType'-element
        XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:ServiceType",
                                serviceIdentification.getServiceType().getCode() );

        // 'ServiceTypeVersion'-elements
        String[] versions = serviceIdentification.getServiceTypeVersions();
        for ( int i = 0; i < versions.length; i++ ) {
            XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:ServiceTypeVersion", versions[i] );
        }

        // 'Fees'-element
        XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:Fees", serviceIdentification.getFees() );

        // 'AccessConstraints'-element
        String[] constraints = serviceIdentification.getAccessConstraints();
        if ( constraints != null ) {
            for ( int i = 0; i < constraints.length; i++ ) {
                XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:AccessConstraints", constraints[i] );
            }
        }
    }

    /**
     * Appends a <code>ows:Keywords</code> -element for each <code>Keywords</code> object of the passed array to the
     * passed <code>Element</code>.
     * 
     * @param xmlNode
     * @param keywords
     * @param namespaceURI
     */
    protected static void appendKeywords( Element xmlNode, Keywords[] keywords, URI namespaceURI ) {
        if ( keywords != null ) {
            for ( int i = 0; i < keywords.length; i++ ) {
                Element node = XMLTools.appendElement( xmlNode, namespaceURI, "ows:Keywords" );
                appendKeywords( node, keywords[i], namespaceURI );
            }
        }
    }

    /**
     * Appends a <code>Keyword</code> -element to the passed <code>Element</code> and fills it with the available
     * keywords.
     * 
     * @param xmlNode
     * @param keywords
     * @param namespaceURI
     */
    protected static void appendKeywords( Element xmlNode, Keywords keywords, URI namespaceURI ) {
        if ( keywords != null ) {
            String[] kw = keywords.getKeywords();
            for ( int i = 0; i < kw.length; i++ ) {
                XMLTools.appendElement( xmlNode, namespaceURI, "ows:Keyword", kw[i] );
            }
            if ( keywords.getThesaurusName() != null ) {
                XMLTools.appendElement( xmlNode, namespaceURI, "ows:Type", keywords.getThesaurusName() );
            }
        }
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
        if ( getFeature.getMaxFeatures() > 0 ) {
            root.setAttribute( "maxFeatures", "" + getFeature.getMaxFeatures() );
        }
        if ( getFeature.getStartPosition() > 0 ) {
            root.setAttribute( "startPosition", "" + getFeature.getStartPosition() );
        }
        if ( getFeature.getTraverseXLinkDepth() >= 0 ) {
            root.setAttribute( "traverseXlinkDepth", "" + getFeature.getTraverseXLinkDepth() );
        }
        if ( getFeature.getTraverseXLinkExpiry() >= 0 ) {
            root.setAttribute( "traverseXlinkExpiry", "" + getFeature.getTraverseXLinkExpiry() );
        }
        Query[] queries = getFeature.getQuery();
        for ( int i = 0; i < queries.length; i++ ) {
            appendQuery( root, queries[i] );
        }
        return xml;
    }

    /**
     * Exports a {@link LockFeature} request instance to a {@link LockFeatureDocument}.
     * 
     * @param request
     *            request to be exported
     * @return XML representation of the <code>LockFeature</code> request
     * @throws IOException
     * @throws XMLParsingException
     * @throws SAXException
     */
    public static LockFeatureDocument export( LockFeature request )
                            throws IOException, XMLParsingException, SAXException {

        LockFeatureDocument doc = new LockFeatureDocument();
        doc.createEmptyDocument();

        Element root = doc.getRootElement();
        root.setAttribute( "version", request.getVersion() );
        root.setAttribute( "service", "WFS" );
        if ( request.getHandle() != null ) {
            root.setAttribute( "handle", request.getHandle() );
        }
        root.setAttribute( "expiry", "" + request.getExpiry() );
        root.setAttribute( "lockAction", "" + request.getLockAction() );

        List<Lock> locks = request.getLocks();
        for ( Lock lock : locks ) {
            appendLock( root, lock );
        }
        return doc;
    }

    /**
     * Appends the XML representation of the given {@link Lock} to the given element.
     * 
     * @param root
     * @param lock
     */
    private static void appendLock( Element root, Lock lock )
                            throws IOException, XMLParsingException {

        Element lockElement = XMLTools.appendElement( root, WFS, "Lock" );
        if ( lock.getHandle() != null ) {
            lockElement.setAttribute( "handle", lock.getHandle() );
        }
        QualifiedName typeName = lock.getTypeName();
        if ( typeName.getPrefix() != null ) {
            lockElement.setAttribute( "xmlns:" + typeName.getPrefix(), typeName.getNamespace().toASCIIString() );
        }
        lockElement.setAttribute( "typeName", typeName.getPrefixedName() );

        // copy filter into Lock element
        if ( lock.getFilter() != null ) {
            StringReader sr = new StringReader( lock.getFilter().to110XML().toString() );
            Document doc;
            try {
                doc = XMLTools.parse( sr );
            } catch ( SAXException e ) {
                throw new XMLParsingException( "Could not parse filter.", e );
            }
            Element elem = XMLTools.appendElement( lockElement, OGCNS, "ogc:Filter" );
            XMLTools.copyNode( doc.getDocumentElement(), elem );
        }
    }

    /**
     * Exports a {@link LockFeatureResponse} instance to its XML representation.
     * 
     * @param response
     *            response to be exported
     * @return XML representation of the <code>LockFeatureResponse</code>
     * @throws IOException
     * @throws SAXException
     */
    public static LockFeatureResponseDocument export( LockFeatureResponse response )
                            throws IOException, SAXException {

        LockFeatureResponseDocument doc = new LockFeatureResponseDocument();
        doc.createEmptyDocument();

        Element root = doc.getRootElement();
        XMLTools.appendElement( root, WFS, "LockId", response.getLockId() );
        String[] fids = response.getFeaturesLocked();
        if ( fids.length != 0 ) {
            Element featuresLockedElement = XMLTools.appendElement( root, WFS, "FeaturesLocked" );
            for ( String fid : fids ) {
                appendFeatureId( featuresLockedElement, fid );
            }
        }
        fids = response.getFeaturesNotLocked();
        if ( fids.length != 0 ) {
            Element featuresNotLockedElement = XMLTools.appendElement( root, WFS, "FeaturesNotLocked" );
            for ( String fid : fids ) {
                appendFeatureId( featuresNotLockedElement, fid );
            }
        }
        return doc;
    }

    /**
     * Exports a {@link Transaction} instance to its XML representation.
     * 
     * @param transaction
     *            transaction to export
     * @return XML representation of transaction
     * @throws IOException
     * @throws XMLParsingException
     */
    public static TransactionDocument export( Transaction transaction )
                            throws IOException, XMLParsingException {

        TransactionDocument xml = new TransactionDocument();
        try {
            xml.createEmptyDocument();
        } catch ( SAXException e ) {
            throw new IOException( e.getMessage() );
        }
        Element root = xml.getRootElement();
        List<TransactionOperation> ops = transaction.getOperations();
        for ( int i = 0; i < ops.size(); i++ ) {
            try {
                if ( ops.get( i ) instanceof Insert ) {
                    appendInsert( root, (Insert) ops.get( i ) );
                } else if ( ops.get( i ) instanceof Update ) {
                    appendUpdate( root, (Update) ops.get( i ) );
                } else if ( ops.get( i ) instanceof Delete ) {
                    appendDelete( root, (Delete) ops.get( i ) );
                }
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
                throw new XMLParsingException( e.getMessage() );
            }
        }
        return xml;
    }

    /**
     * Adds the XML representation of a <code>Delete</code> operation to the given element.
     * 
     * @param root
     * @param delete
     */
    private static void appendDelete( Element root, Delete delete ) {
        Element el = XMLTools.appendElement( root, WFS, "Delete" );
        if ( delete.getHandle() != null ) {
            el.setAttribute( "handle", delete.getHandle() );
        }
        el.setAttribute( "typeName", delete.getTypeName().getPrefixedName() );
        // ensure that the type's namespace is declared
        el.setAttribute( "xmlns:" + delete.getTypeName().getPrefix(),
                         delete.getTypeName().getNamespace().toASCIIString() );

        Filter filter = delete.getFilter();
        if ( filter != null ) {
            org.deegree.model.filterencoding.XMLFactory.appendFilter( el, filter );
        }
        root.appendChild( el );
    }

    /**
     * Adds the XML representation of an <code>Update</code> operation to the given element.
     * <p>
     * Respects the deegree-specific extension to the Update operation: instead of specifying properties and their
     * values, it's also possible to only specify just one feature that replaces the matched feature.
     * 
     * @param root
     * @param update
     * @throws SAXException
     * @throws IOException
     * @throws FeatureException
     * @throws GeometryException
     */
    private static void appendUpdate( Element root, Update update )
                            throws FeatureException, IOException, SAXException, GeometryException {

        Element el = XMLTools.appendElement( root, WFS, "Update" );
        if ( update.getHandle() != null ) {
            el.setAttribute( "handle", update.getHandle() );
        }
        
        el.setAttribute( "typeName", update.getTypeName().getPrefixedName() );

        // ensure that the type's namespace is declared
        el.setAttribute( "xmlns:" + update.getTypeName().getPrefix(),
                         update.getTypeName().getNamespace().toASCIIString() );

        Feature replacement = update.getFeature();
        if ( replacement != null ) {
            GMLFeatureAdapter adapter = new GMLFeatureAdapter();
            adapter.append( el, replacement );
        } else {
            Map<PropertyPath, FeatureProperty> replaces = update.getReplacementProperties();
            for ( PropertyPath propertyName : replaces.keySet() ) {
                Element propElement = XMLTools.appendElement( el, WFS, "Property" );
                Element nameElement = XMLTools.appendElement( propElement, WFS, "Name" );
                org.deegree.ogcbase.XMLFactory.appendPropertyPath( nameElement, propertyName );

                // append property value
                Object propValue = replaces.get( propertyName ).getValue();
                if ( propValue != null ) {
                    Element valueElement = XMLTools.appendElement( propElement, WFS, "Value" );
                    if ( propValue instanceof Feature ) {
                        GMLFeatureAdapter adapter = new GMLFeatureAdapter();
                        adapter.append( valueElement, (Feature) propValue );
                    } else if ( propValue instanceof Geometry ) {
                        appendGeometry( valueElement, (Geometry) propValue );
                    } else {
                        XMLTools.setNodeValue( valueElement, propValue.toString() );
                    }
                }
            }
        }

        Filter filter = update.getFilter();
        if ( filter != null ) {
            org.deegree.model.filterencoding.XMLFactory.appendFilter( el, filter );
        }
        root.appendChild( el );
    }

    /**
     * Adds the XML representation of an <code>Insert</code> operation to the given element.
     * 
     * @param root
     * @param insert
     * @throws SAXException
     * @throws IOException
     * @throws FeatureException
     */
    private static void appendInsert( Element root, Insert insert )
                            throws IOException, FeatureException, XMLException, SAXException {

        Element el = XMLTools.appendElement( root, WFS, "Insert" );
        if ( insert.getHandle() != null ) {
            el.setAttribute( "handle", insert.getHandle() );
        }
        if ( insert.getIdGen() != null ) {
            switch ( insert.getIdGen() ) {
            case USE_EXISTING:
                el.setAttribute( "idgen", "UseExisting" );
                break;
            case GENERATE_NEW:
                el.setAttribute( "idgen", "GenerateNew" );
                break;
            case REPLACE_DUPLICATE:
                el.setAttribute( "idgen", "ReplaceDuplicate" );
                break;
            }
        }

        GMLFeatureAdapter adapter = new GMLFeatureAdapter();
        adapter.append( el, insert.getFeatures() );
    }

    /**
     * Exports an instance of {@link TransactionResponse} to its XML representation.
     * 
     * @param response
     *            TransactionResponse to export
     * @return XML representation of TransactionResponse
     * @throws IOException
     */
    public static TransactionResponseDocument export( TransactionResponse response )
                            throws IOException {

        TransactionResponseDocument xml = new TransactionResponseDocument();
        try {
            xml.createEmptyDocument();
        } catch ( SAXException e ) {
            throw new IOException( e.getMessage() );
        }

        Element root = xml.getRootElement();
        appendTransactionSummary( root, response.getTotalInserted(), response.getTotalUpdated(),
                                  response.getTotalDeleted() );
        appendInsertResults( root, response.getInsertResults() );
        return xml;
    }

    /**
     * Appends a 'wfs:TransactionSummary' element to the given element.
     * 
     * @param root
     * @param totalInserted
     * @param totalUpdated
     * @param totalDeleted
     */
    private static void appendTransactionSummary( Element root, int totalInserted, int totalUpdated, int totalDeleted ) {
        Element taSummary = XMLTools.appendElement( root, WFS, "TransactionSummary" );
        XMLTools.appendElement( taSummary, WFS, "totalInserted", "" + totalInserted );
        XMLTools.appendElement( taSummary, WFS, "totalUpdated", "" + totalUpdated );
        XMLTools.appendElement( taSummary, WFS, "totalDeleted", "" + totalDeleted );
    }

    /**
     * Appends an 'wfs:InsertResults' element to the given element (only if necessary).
     * 
     * @param root
     * @param insertResults
     */
    private static void appendInsertResults( Element root, Collection<InsertResults> insertResults ) {
        Element insertResultsElement = appendElement( root, WFS, "InsertResults" );

        // append synthetic ones because of the faulty schema
        if ( insertResults.size() == 0 ) {
            Document d = root.getOwnerDocument();
            Comment comment = d.createComment( "Dummy InsertResults element for compliance with (faulty?) WFS schema" );
            root.insertBefore( comment, insertResultsElement );
            Element elem = appendElement( insertResultsElement, WFS, "Feature" );
            appendElement( elem, OGCNS, OGC_PREFIX + ":FeatureId" ).setAttribute( "fid", "bogus" );
        } else {
            Iterator<InsertResults> iter = insertResults.iterator();
            while ( iter.hasNext() ) {
                appendFeatureIds( insertResultsElement, iter.next() );
            }
        }
    }

    /**
     * Appends a 'wfs:Feature' element to the given element.
     * 
     * @param root
     * @param results
     */
    private static void appendFeatureIds( Element root, InsertResults results ) {
        Element featureElement = XMLTools.appendElement( root, WFS, "Feature" );
        String handle = results.getHandle();
        if ( handle != null ) {
            featureElement.setAttribute( "handle", handle );
        }
        Iterator<FeatureId> iter = results.getFeatureIDs().iterator();
        while ( iter.hasNext() ) {
            Element featureIdElement = XMLTools.appendElement( featureElement, OGCNS, "ogc:FeatureId" );
            featureIdElement.setAttribute( "fid", iter.next().getAsString() );
        }
    }

    /**
     * Appends the XML representation of the given {@link Query} instance to an element.
     * 
     * @param query
     */
    private static void appendQuery( Element root, Query query )
                            throws IOException, XMLParsingException {

        Element queryElem = XMLTools.appendElement( root, WFS, "Query" );
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
            if ( propertyNames[i] instanceof XLinkPropertyPath ) {
                Element propertyNameElement = appendElement( queryElem, WFSNS, WFS_PREFIX + ":XlinkPropertyName" );
                String depth = Integer.toString( ( (XLinkPropertyPath) propertyNames[i] ).getXlinkDepth() );
                propertyNameElement.setAttribute( "traverseXlinkDepth", depth );
                appendPropertyPath( propertyNameElement, propertyNames[i] );
            } else {
                Element propertyNameElement = appendElement( queryElem, WFSNS, WFS_PREFIX + ":PropertyName" );
                appendPropertyPath( propertyNameElement, propertyNames[i] );
            }
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
            StringReader sr = new StringReader( query.getFilter().to110XML().toString() );
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
     * Appends the XML representation of the <code>wfs:FeatureTypeList</code>- section to the passed
     * <code>Element</code>.
     * 
     * @param root
     * @param featureTypeList
     */
    public static void appendFeatureTypeList( Element root, FeatureTypeList featureTypeList ) {

        Element featureTypeListNode = XMLTools.appendElement( root, WFS, "FeatureTypeList", null );
        Operation[] operations = featureTypeList.getGlobalOperations();
        if ( operations != null ) {
            Element operationsNode = XMLTools.appendElement( featureTypeListNode, WFS, "Operations" );
            for ( int i = 0; i < operations.length; i++ ) {
                XMLTools.appendElement( operationsNode, WFS, "Operation", operations[i].getOperation() );
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
     * Appends the XML representation of the <code>WFSFeatureType</code> instance to the passed <code>Element</code>.
     * 
     * @param root
     * @param featureType
     */
    public static void appendWFSFeatureType( Element root, WFSFeatureType featureType ) {

        Element featureTypeNode = XMLTools.appendElement( root, WFS, "FeatureType" );

        if ( featureType.getName().getPrefix() != null ) {
            XMLTools.appendNSBinding( featureTypeNode, featureType.getName().getPrefix(),
                                      featureType.getName().getNamespace() );
        }
        XMLTools.appendElement( featureTypeNode, WFS, "Name", featureType.getName().getPrefixedName() );
        XMLTools.appendElement( featureTypeNode, WFS, "Title", featureType.getTitle() );
        String abstract_ = featureType.getAbstract();
        if ( abstract_ != null ) {
            XMLTools.appendElement( featureTypeNode, WFS, "Abstract", featureType.getAbstract() );
        }
        Keywords[] keywords = featureType.getKeywords();
        if ( keywords != null ) {
            appendOWSKeywords( featureTypeNode, keywords );
        }
        URI defaultSrs = featureType.getDefaultSRS();
        if ( defaultSrs != null ) {
            XMLTools.appendElement( featureTypeNode, WFS, "DefaultSRS", defaultSrs.toString() );
            URI[] otherSrs = featureType.getOtherSrs();
            if ( otherSrs != null ) {
                for ( int i = 0; i < otherSrs.length; i++ ) {
                    XMLTools.appendElement( featureTypeNode, WFS, "OtherSRS", otherSrs[i].toString() );
                }
            }
        } else {
            XMLTools.appendElement( featureTypeNode, WFS, "Title" );
        }
        Operation[] operations = featureType.getOperations();
        if ( operations != null ) {
            Element operationsNode = XMLTools.appendElement( featureTypeNode, WFS, "Operations" );
            for ( int i = 0; i < operations.length; i++ ) {
                XMLTools.appendElement( operationsNode, WFS, "Operation", operations[i].getOperation() );
            }
        }
        FormatType[] formats = featureType.getOutputFormats();
        if ( formats != null ) {
            appendOutputFormats( featureTypeNode, formats );
        }
        Envelope[] wgs84BoundingBoxes = featureType.getWgs84BoundingBoxes();
        for ( int i = 0; i < wgs84BoundingBoxes.length; i++ ) {
            appendWgs84BoundingBox( featureTypeNode, wgs84BoundingBoxes[i] );
        }
        if ( featureType.getMetadataUrls() != null ) {
            for ( MetadataURL metadataURL : featureType.getMetadataUrls() ) {
                appendMetadataURL( featureTypeNode, metadataURL );
            }
        }
    }

    /**
     * Appends the XML representation of the <code>wfs:ServesGMLObjectTypeList</code>- section to the passed
     * <code>Element</code> as a new element with the given qualified name.
     * 
     * @param root
     * @param elementNS
     * @param elementName
     * @param gmlObjectTypes
     */
    public static void appendGMLObjectTypeList( Element root, URI elementNS, String elementName,
                                                GMLObject[] gmlObjectTypes ) {

        Element gmlObjectTypeListNode = XMLTools.appendElement( root, elementNS, elementName );
        for ( int i = 0; i < gmlObjectTypes.length; i++ ) {
            appendGMLObjectTypeType( gmlObjectTypeListNode, gmlObjectTypes[i] );
        }
    }

    /**
     * Appends the XML representation of the given {@link GMLObject} (as a <code>wfs:GMLObjectType</code> element) to
     * the passed <code>Element</code>.
     * 
     * @param root
     * @param gmlObjectType
     */
    public static void appendGMLObjectTypeType( Element root, GMLObject gmlObjectType ) {

        Element gmlObjectTypeNode = XMLTools.appendElement( root, WFS, "GMLObjectType" );

        if ( gmlObjectType.getName().getPrefix() != null ) {
            XMLTools.appendNSBinding( gmlObjectTypeNode, gmlObjectType.getName().getPrefix(),
                                      gmlObjectType.getName().getNamespace() );
        }
        XMLTools.appendElement( gmlObjectTypeNode, WFS, "Name", gmlObjectType.getName().getPrefixedName() );
        if ( gmlObjectType.getTitle() != null ) {
            XMLTools.appendElement( gmlObjectTypeNode, WFS, "Title", gmlObjectType.getTitle() );
        }
        String abstract_ = gmlObjectType.getAbstract();
        if ( abstract_ != null ) {
            XMLTools.appendElement( gmlObjectTypeNode, WFS, "Abstract", gmlObjectType.getAbstract() );
        }
        Keywords[] keywords = gmlObjectType.getKeywords();
        if ( keywords != null ) {
            appendOWSKeywords( gmlObjectTypeNode, keywords );
        }
        FormatType[] formats = gmlObjectType.getOutputFormats();
        if ( formats != null ) {
            appendOutputFormats( gmlObjectTypeNode, formats );
        }
    }

    /**
     * Appends the XML representation of the given {@link Envelope} (as an <code>ows:WGS84BoundingBoxType</code>
     * element) to the passed <code>Element</code>.
     * 
     * @param root
     * @param envelope
     */
    public static void appendWgs84BoundingBox( Element root, Envelope envelope ) {
        Element wgs84BoundingBoxElement = XMLTools.appendElement( root, OWSNS, "ows:WGS84BoundingBox" );
        XMLTools.appendElement( wgs84BoundingBoxElement, OWSNS, "ows:LowerCorner", envelope.getMin().getX() + " "
                                                                                   + envelope.getMin().getY() );
        XMLTools.appendElement( wgs84BoundingBoxElement, OWSNS, "ows:UpperCorner", envelope.getMax().getX() + " "
                                                                                   + envelope.getMax().getY() );
    }

    private static void appendMetadataURL( Element root, MetadataURL metadataURL ) {
        Element metadataURLElement = XMLTools.appendElement( root, WFSNS, "wfs:MetadataURL",
                                                             metadataURL.getOnlineResource().toString() );
        metadataURLElement.setAttribute( "format", metadataURL.getFormat() );
        metadataURLElement.setAttribute( "type", metadataURL.getType() );
    }

    /**
     * Appends the XML representation of the given {@link FormatType}s as (as a <code>wfs:OutputFormats</code> element)
     * to the passed <code>Element</code>.
     * 
     * @param root
     * @param formats
     */
    public static void appendOutputFormats( Element root, FormatType[] formats ) {

        Element outputFormatsNode = XMLTools.appendElement( root, WFS, "OutputFormats" );
        for ( int i = 0; i < formats.length; i++ ) {
            appendElement( outputFormatsNode, WFS, "Format", formats[i].getValue() );
        }
    }
}
