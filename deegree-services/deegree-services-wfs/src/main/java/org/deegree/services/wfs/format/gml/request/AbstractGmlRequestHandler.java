//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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

import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.protocol.wfs.WFSConstants.GML32_NS;
import static org.deegree.protocol.wfs.WFSConstants.GML32_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.QUERY_ID_GET_FEATURE_BY_ID;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_100_BASIC_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_110_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_PREFIX;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.datetime.ISO8601Converter;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.GMLReference;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.reference.GmlXlinkOptions;
import org.deegree.protocol.wfs.describefeaturetype.DescribeFeatureType;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.i18n.Messages;
import org.deegree.services.wfs.format.gml.GmlFormat;
import org.deegree.services.wfs.format.gml.GmlFormatOptions;
import org.deegree.services.wfs.query.QueryAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link DescribeFeatureType} requests for the {@link GmlFormat}.
 *
 * @see GmlFormat
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 *
 * @since 3.2
 */
abstract class AbstractGmlRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger( AbstractGmlRequestHandler.class );

    private static final QName WFS_FEATURECOLLECTION_NAME = new QName( WFS_NS, "FeatureCollection", WFS_PREFIX );

    final static TimeZone GMT = TimeZone.getTimeZone( "GMT" );

    protected final GmlFormatOptions options;

    protected final GmlFormat format;

    AbstractGmlRequestHandler( GmlFormat format ) {
        this.options = format.getGmlFormatOptions();
        this.format = format;
    }

    protected GMLObject retrieveObject( String id )
                            throws OWSException {
        GMLObject o = null;
        for ( FeatureStore fs : format.getMaster().getStoreManager().getStores() ) {
            try {
                o = fs.getObjectById( id );
            } catch ( FeatureStoreException e ) {
                throw new OWSException( e.getMessage(), NO_APPLICABLE_CODE );
            }
            if ( o != null ) {
                break;
            }
        }
        if ( o == null ) {
            String msg = Messages.getMessage( "WFS_NO_SUCH_OBJECT", id );
            throw new OWSException( new InvalidParameterValueException( msg ) );
        }
        return o;
    }

    protected void writeAdditionalObjects( GMLStreamWriter gmlStream, WfsXlinkStrategy additionalObjects,
                                           QName featureMemberEl, Version requestVersion )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        Collection<GMLReference<?>> nextLevelObjects = additionalObjects.getAdditionalRefs();
        XMLStreamWriter xmlStream = gmlStream.getXMLStream();
        boolean wroteStartSection = false;
        while ( !nextLevelObjects.isEmpty() ) {
            Map<GMLReference<?>, GmlXlinkOptions> refToResolveState = additionalObjects.getResolveStates();
            additionalObjects.clear();
            for ( GMLReference<?> ref : nextLevelObjects ) {
                if ( isResolvable( ref ) && !isObjectAlreadySerialized( gmlStream, ref.getId() ) ) {
                    GmlXlinkOptions resolveState = refToResolveState.get( ref );
                    Feature feature = (Feature) ref;
                    if ( !wroteStartSection ) {
                        writeAdditionalObjectsStart( xmlStream, requestVersion );
                        wroteStartSection = true;
                    }
                    writeMemberFeature( feature, gmlStream, xmlStream, resolveState, featureMemberEl );
                }
            }
            nextLevelObjects = additionalObjects.getAdditionalRefs();
        }
        if ( wroteStartSection ) {
            writeAdditionalObjectsEnd( xmlStream, requestVersion );
        }
    }

    private boolean isObjectAlreadySerialized( final GMLStreamWriter gmlStream, final String id ) {
        return gmlStream.getReferenceResolveStrategy().isObjectExported( id );
    }

    private void writeAdditionalObjectsStart( XMLStreamWriter xmlStream, Version requestVersion )
                            throws XMLStreamException {
        if ( requestVersion.equals( VERSION_200 ) ) {
            xmlStream.writeStartElement( "wfs", "additionalObjects", WFS_200_NS );
            xmlStream.writeStartElement( "wfs", "SimpleFeatureCollection", WFS_200_NS );
        } else {
            xmlStream.writeComment( "Additional features (subfeatures of requested features)" );
        }
    }

    private void writeAdditionalObjectsEnd( XMLStreamWriter xmlStream, Version requestVersion )
                            throws XMLStreamException {
        if ( requestVersion.equals( VERSION_200 ) ) {
            xmlStream.writeEndElement();
            xmlStream.writeEndElement();
        }
    }

    private boolean isResolvable( GMLReference<?> ref ) {
        try {
            ref.getReferencedObject();
            return true;
        } catch ( ReferenceResolvingException e ) {
            return false;
        }
    }

    protected void writeMemberFeature( Feature member, GMLStreamWriter gmlStream, XMLStreamWriter xmlStream,
                                       GmlXlinkOptions resolveState, QName featureMemberEl )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        if ( gmlStream.getReferenceResolveStrategy().isObjectExported( member.getId() ) ) {
            xmlStream.writeEmptyElement( featureMemberEl.getNamespaceURI(), featureMemberEl.getLocalPart() );
            if ( xmlStream.getPrefix( XLNNS ) == null ) {
                xmlStream.setPrefix( "xlink", XLNNS );
                xmlStream.writeNamespace( "xlink", XLNNS );
            }
            xmlStream.writeAttribute( "xlink", XLNNS, "href", "#" + member.getId() );
        } else {
            xmlStream.writeStartElement( featureMemberEl.getNamespaceURI(), featureMemberEl.getLocalPart() );
            gmlStream.getFeatureWriter().export( member, resolveState );
            xmlStream.writeEndElement();
        }
    }

    /**
     * Returns an URL template for requesting individual objects (feature or geometries) from the server by the object's
     * id.
     * <p>
     * The form of the URL depends on the protocol version:
     * <ul>
     * <li>WFS 1.0.0: GetGmlObject request (actually a 1.1.0 request, as 1.0.0 doesn't have it)</li>
     * <li>WFS 1.1.0: GetGmlObject request</li>
     * <li>WFS 2.0.0: GetFeature request using stored query (urn:ogc:def:query:OGC-WFS::GetFeatureById)</li>
     * </ul>
     * </p>
     *
     * @param version
     *            WFS protocol version, must not be <code>null</code>
     * @param gmlVersion
     *            GML version, must not be <code>null</code>
     * @return URI template that contains <code>{}</code> as the placeholder for the object id
     * @throws UnsupportedOperationException
     *             if the protocol version does not support requesting individual objects by id
     */
    protected String getObjectXlinkTemplate( Version version, GMLVersion gmlVersion ) {

        String baseUrl = OGCFrontController.getHttpGetURL() + "SERVICE=WFS&VERSION=" + version + "&";
        String template = null;
        try {
            if ( VERSION_100.equals( version ) ) {
                baseUrl = OGCFrontController.getHttpGetURL() + "SERVICE=WFS&VERSION=1.1.0&";
                template = baseUrl + "REQUEST=GetGmlObject&OUTPUTFORMAT="
                           + URLEncoder.encode( gmlVersion.getMimeTypeOldStyle(), "UTF-8" )
                           + "&TRAVERSEXLINKDEPTH=0&GMLOBJECTID={}#{}";
            } else if ( VERSION_110.equals( version ) ) {
                template = baseUrl + "REQUEST=GetGmlObject&OUTPUTFORMAT="
                           + URLEncoder.encode( gmlVersion.getMimeTypeOldStyle(), "UTF-8" )
                           + "&TRAVERSEXLINKDEPTH=0&GMLOBJECTID={}#{}";
            } else if ( VERSION_200.equals( version ) ) {
                template = baseUrl + "REQUEST=GetFeature&OUTPUTFORMAT="
                           + URLEncoder.encode( gmlVersion.getMimeType(), "UTF-8" ) + "&STOREDQUERY_ID="
                           + QUERY_ID_GET_FEATURE_BY_ID + "&ID={}#{}";
            } else {
                throw new UnsupportedOperationException( Messages.getMessage( "WFS_BACKREFERENCE_UNSUPPORTED", version ) );
            }
        } catch ( UnsupportedEncodingException e ) {
            // should never happen (UTF-8 is known)
        }
        return template;
    }

    /**
     * Returns the value for the <code>xsi:schemaLocation</code> attribute in the response document.
     *
     * @param requestVersion
     *            requested WFS version, must not be <code>null</code>
     * @param requestedFts
     *            requested feature types, can be <code>null</code> (any feature type may occur in the output)
     * @return value for the <code>xsi:schemaLocation</code> attribute, never <code>null</code>
     */
    protected String getSchemaLocation( Version requestVersion, Collection<FeatureType> requestedFts ) {
        if ( VERSION_200.equals( requestVersion ) ) {
            return getSchemaLocationForWfs200( requestedFts );
        }
        if ( options.getResponseContainerEl() == null ) {
            if ( VERSION_100.equals( requestVersion ) ) {
                return getSchemaLocationForWfs100( requestedFts );
            } else if ( VERSION_110.equals( requestVersion ) ) {
                return getSchemaLocationForWfs110( requestedFts );
            }
        }
        return getCustomSchemaLocationForWfs100Or110( requestVersion, requestedFts );
    }

    private String getSchemaLocationForWfs100( Collection<FeatureType> requestedFts ) {
        GMLVersion gmlVersion = options.getGmlVersion();
        String schemaLocation = null;
        if ( GML_2 == gmlVersion ) {
            schemaLocation = WFS_NS + " " + WFS_100_BASIC_SCHEMA_URL;
        } else {
            schemaLocation = getSchemaLocation( VERSION_100, gmlVersion, WFS_FEATURECOLLECTION_NAME );
        }
        return schemaLocation + " " + getSchemaLocationPartForFeatureTypes( VERSION_100, gmlVersion, requestedFts );
    }

    private String getSchemaLocationForWfs110( Collection<FeatureType> requestedFts ) {
        GMLVersion gmlVersion = options.getGmlVersion();
        String schemaLocation = null;
        if ( GML_31 == gmlVersion ) {
            schemaLocation = WFS_NS + " " + WFS_110_SCHEMA_URL;
        } else {
            schemaLocation = getSchemaLocation( VERSION_110, gmlVersion, WFS_FEATURECOLLECTION_NAME );
        }
        return schemaLocation + " " + getSchemaLocationPartForFeatureTypes( VERSION_110, gmlVersion, requestedFts );
    }

    private String getSchemaLocationForWfs200( Collection<FeatureType> requestedFts ) {
        String schemaLocation = WFS_200_NS + " " + WFS_200_SCHEMA_URL + " " + GML32_NS + " " + GML32_SCHEMA_URL;
        GMLVersion gmlVersion = options.getGmlVersion();
        return schemaLocation + " " + getSchemaLocationPartForFeatureTypes( VERSION_200, gmlVersion, requestedFts );
    }

    private String getCustomSchemaLocationForWfs100Or110( Version requestVersion, Collection<FeatureType> requestedFts ) {
        String schemaLocation = "";
        if ( options.getSchemaLocation() != null ) {
            schemaLocation = options.getSchemaLocation();
        }
        GMLVersion gmlVersion = options.getGmlVersion();
        return schemaLocation + " " + getSchemaLocationPartForFeatureTypes( requestVersion, gmlVersion, requestedFts );
    }

    private String getSchemaLocationPartForFeatureTypes( Version requestVersion, GMLVersion gmlVersion,
                                                         Collection<FeatureType> requestedFts ) {
        QName[] requestedFtNames = new QName[0];
        if ( requestedFts != null ) {
            requestedFtNames = new QName[requestedFts.size()];
            int i = 0;
            for ( FeatureType requestedFt : requestedFts ) {
                requestedFtNames[i++] = requestedFt.getName();
            }
        }
        return getSchemaLocation( requestVersion, gmlVersion, requestedFtNames );
    }

    protected QName determineFeatureMemberElement( Version version ) {
        GMLVersion gmlVersion = options.getGmlVersion();
        QName memberElementName = null;
        if ( VERSION_200.equals( version ) ) {
            // WFS 2.0.0 response -> hard-wired wfs:FeatureCollection response, always use wfs:member
            memberElementName = new QName( WFS_200_NS, "member", "wfs" );
        } else if ( options.getResponseFeatureMemberEl() != null ) {
            // WFS 1.0.0 / 1.1.0 with a custom configured member element
            memberElementName = new QName( options.getResponseFeatureMemberEl().getNamespaceURI(),
                                           options.getResponseFeatureMemberEl().getLocalPart(),
                                           options.getResponseFeatureMemberEl().getPrefix() );
        } else if ( gmlVersion == GML_32 ) {
            // WFS 1.0.0 / 1.1.0 without custom configured member element, GML 3.2 -> wfs:featureMember
            memberElementName = new QName( WFS_NS, "member", "wfs" );
        } else {
            // WFS 1.0.0 / 1.1.0 without custom configured member element, non-GML 3.2 -> gml:featureMember
            memberElementName = new QName( gmlVersion.getNamespace(), "featureMember", "gml" );
        }
        return memberElementName;
    }

    protected boolean localReferencesPossible( QueryAnalyzer analyzer, int traverseXLinkDepth ) {
        if ( traverseXLinkDepth == 0 && analyzer.getQueries().size() == 1 ) {
            List<Query> queries = analyzer.getQueries().values().iterator().next();
            if ( queries.size() == 1 ) {
                Query query = queries.get( 0 );
                if ( query.getTypeNames().length == 1 ) {
                    TypeName typeName = query.getTypeNames()[0];
                    FeatureStore fs = analyzer.getQueries().keySet().iterator().next();
                    FeatureType ft = fs.getSchema().getFeatureType( typeName.getFeatureTypeName() );
                    for ( PropertyType pt : ft.getPropertyDeclarations() ) {
                        if ( pt instanceof FeaturePropertyType ) {
                            FeaturePropertyType fpt = (FeaturePropertyType) pt;
                            FeatureType targetFt = fpt.getValueFt();
                            if ( targetFt == null || fs.getSchema().isSubType( targetFt, ft ) ) {
                                return true;
                            }
                        }
                    }
                    LOG.debug( "Forward references can be ruled out." );
                    return false;
                }
            }
        }
        return true;
    }

    protected Map<String, String> getFeatureTypeNsPrefixes( Collection<FeatureType> fts ) {

        if ( fts == null ) {
            fts = format.getMaster().getStoreManager().getFeatureTypes();
        }

        Map<String, String> prefixToNs = new HashMap<String, String>();
        for ( FeatureType ft : fts ) {
            QName ftName = ft.getName();
            if ( ftName.getPrefix() != null ) {
                prefixToNs.put( ftName.getPrefix(), ftName.getNamespaceURI() );
            }
        }

        return prefixToNs;
    }

    public Set<String> getAppSchemaNamespaces() {
        Set<String> set = new LinkedHashSet<String>();
        for ( FeatureStore fs : format.getMaster().getStoreManager().getStores() ) {
            set.addAll( fs.getSchema().getAppNamespaces() );
        }
        set.remove( GMLNS );
        set.remove( GML3_2_NS );
        return set;
    }

    protected String getTimestamp() {
        DateTime dateTime = getCurrentDateTimeWithoutMilliseconds();
        return ISO8601Converter.formatDateTime( dateTime );
    }

    protected DateTime getCurrentDateTimeWithoutMilliseconds() {
        long msSince1970 = new Date().getTime();
        msSince1970 = msSince1970 / 1000 * 1000;
        return new DateTime( new Date( msSince1970 ), GMT );
    }

    /**
     * Returns the value for the 'xsi:schemaLocation' attribute to be included in a <code>GetGmlObject</code> or
     * <code>GetFeature</code> response.
     *
     * @param version
     *            WFS protocol version, must not be <code>null</code>
     * @param gmlVersion
     *            requested GML version, must not be <code>null</code>
     * @param fts
     *            types of features included in the response, may be empty, but must not be <code>null</code>
     * @return schemaLocation value
     */
    protected String getSchemaLocation( Version version, GMLVersion gmlVersion, QName... fts ) {

        StringBuilder baseUrl = new StringBuilder();

        baseUrl.append( OGCFrontController.getHttpGetURL() );
        baseUrl.append( "SERVICE=WFS&VERSION=" );
        baseUrl.append( version );
        baseUrl.append( "&REQUEST=DescribeFeatureType&OUTPUTFORMAT=" );

        try {
            if ( VERSION_100.equals( version ) && gmlVersion == GML_2 ) {
                baseUrl.append( "XMLSCHEMA" );
            } else if ( gmlVersion == GML_32 ) {
                baseUrl.append( URLEncoder.encode( options.getMimeType(), "UTF-8" ) );
            } else {
                baseUrl.append( URLEncoder.encode( gmlVersion.getMimeTypeOldStyle(), "UTF-8" ) );
            }

            if ( fts.length > 0 ) {

                baseUrl.append( "&TYPENAME=" );

                Map<String, String> bindings = new HashMap<String, String>();
                for ( int i = 0; i < fts.length; i++ ) {
                    QName ftName = fts[i];
                    bindings.put( ftName.getPrefix(), ftName.getNamespaceURI() );
                    baseUrl.append( URLEncoder.encode( ftName.getPrefix(), "UTF-8" ) );
                    baseUrl.append( ':' );
                    baseUrl.append( URLEncoder.encode( ftName.getLocalPart(), "UTF-8" ) );
                    if ( i != fts.length - 1 ) {
                        baseUrl.append( ',' );
                    }
                }

                if ( VERSION_110.equals( version ) ) {
                    baseUrl.append( "&NAMESPACE=xmlns(" );
                    int i = 0;
                    for ( Entry<String, String> entry : bindings.entrySet() ) {
                        baseUrl.append( URLEncoder.encode( entry.getKey(), "UTF-8" ) );
                        baseUrl.append( '=' );
                        baseUrl.append( URLEncoder.encode( entry.getValue(), "UTF-8" ) );
                        if ( i != bindings.size() - 1 ) {
                            baseUrl.append( ',' );
                        }
                        ++i;
                    }
                    baseUrl.append( ')' );
                }
                if ( VERSION_200.equals( version ) ) {
                    baseUrl.append( "&NAMESPACES=" );
                    int i = 0;
                    for ( Entry<String, String> e : bindings.entrySet() ) {
                        baseUrl.append( "xmlns(" );
                        baseUrl.append( URLEncoder.encode( e.getKey(), "UTF-8" ) );
                        baseUrl.append( ',' );
                        baseUrl.append( URLEncoder.encode( e.getValue(), "UTF-8" ) );
                        baseUrl.append( ")" );
                        if ( i != bindings.size() - 1 ) {
                            baseUrl.append( "," );
                        }
                        ++i;
                    }
                }
            }
        } catch ( UnsupportedEncodingException e ) {
            // should never happen (UTF-8 *is* known to Java)
        }

        String ns = null;
        if ( fts.length > 0 ) {
            ns = fts[0].getNamespaceURI();
        } else {
            ns = getAppSchemaNamespaces().iterator().next();
        }

        return ns + " " + baseUrl.toString();
    }

}
