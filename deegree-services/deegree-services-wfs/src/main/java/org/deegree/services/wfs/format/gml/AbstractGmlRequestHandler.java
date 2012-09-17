//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.services.wfs.format.gml;

import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.protocol.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.protocol.wfs.WFSConstants.GML32_NS;
import static org.deegree.protocol.wfs.WFSConstants.GML32_SCHEMA_URL;
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
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.i18n.Messages;
import org.deegree.services.wfs.QueryAnalyzer;
import org.deegree.services.wfs.WebFeatureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
abstract class AbstractGmlRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger( AbstractGmlRequestHandler.class );

    final static TimeZone GMT = TimeZone.getTimeZone( "GMT" );

    protected final GmlFormatOptions options;

    protected final GMLFormat format;

    AbstractGmlRequestHandler( GMLFormat format ) {
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

    protected void writeAdditionalObjects( GMLStreamWriter gmlStream, XlinkedObjectsHandler additionalObjects,
                                           int traverseXLinkDepth, QName featureMemberEl )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        int currentLevel = 1;
        Collection<GMLReference<?>> includeObjects = additionalObjects.getAdditionalRefs();

        while ( ( traverseXLinkDepth == -1 || currentLevel <= traverseXLinkDepth ) && !includeObjects.isEmpty() ) {
            additionalObjects.clear();
            for ( GMLReference<?> gmlReference : includeObjects ) {
                Feature feature = (Feature) gmlReference;
                writeMemberFeature( feature, gmlStream, gmlStream.getXMLStream(), currentLevel, featureMemberEl );
            }
            includeObjects = additionalObjects.getAdditionalRefs();
            currentLevel++;
        }
    }

    protected void writeMemberFeature( Feature member, GMLStreamWriter gmlStream, XMLStreamWriter xmlStream, int level,
                                       QName featureMemberEl )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        if ( gmlStream.isObjectExported( member.getId() ) ) {
            xmlStream.writeEmptyElement( featureMemberEl.getNamespaceURI(), featureMemberEl.getLocalPart() );
            if ( xmlStream.getPrefix( XLNNS ) == null ) {
                xmlStream.setPrefix( "xlink", XLNNS );
                xmlStream.writeNamespace( "xlink", XLNNS );
            }
            xmlStream.writeAttribute( "xlink", XLNNS, "href", "#" + member.getId() );
        } else {
            xmlStream.writeStartElement( featureMemberEl.getNamespaceURI(), featureMemberEl.getLocalPart() );
            gmlStream.getFeatureWriter().export( member, level );
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
                           + URLEncoder.encode( gmlVersion.getMimeType(), "UTF-8" )
                           + "&STOREDQUERY_ID=urn:ogc:def:query:OGC-WFS::GetFeatureById&ID={}#{}";
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

        GMLVersion gmlVersion = options.getGmlVersion();

        String schemaLocation = null;
        if ( !VERSION_200.equals( requestVersion ) ) {
            schemaLocation = options.getSchemaLocation();
        } else {
            schemaLocation = WFS_200_NS + " " + WFS_200_SCHEMA_URL + " " + GML32_NS + " " + GML32_SCHEMA_URL;
        }
        if ( options.getResponseContainerEl() == null ) {
            // use "wfs:FeatureCollection" then
            QName wfsFeatureCollection = new QName( WFS_NS, "FeatureCollection", WFS_PREFIX );
            if ( wfsFeatureCollection.equals( options.getResponseContainerEl() ) ) {
                if ( VERSION_100.equals( requestVersion ) ) {
                    if ( GML_2 == gmlVersion ) {
                        schemaLocation = WFS_NS + " " + WFS_100_BASIC_SCHEMA_URL;
                    } else {
                        schemaLocation = WebFeatureService.getSchemaLocation( requestVersion, gmlVersion,
                                                                              wfsFeatureCollection );
                    }
                } else if ( VERSION_110.equals( requestVersion ) ) {
                    if ( GML_31 == gmlVersion ) {
                        schemaLocation = WFS_NS + " " + WFS_110_SCHEMA_URL;
                    } else {
                        schemaLocation = WebFeatureService.getSchemaLocation( requestVersion, gmlVersion,
                                                                              wfsFeatureCollection );
                    }
                }
            }
        }

        if ( requestedFts == null ) {
            requestedFts = format.getMaster().getStoreManager().getFeatureTypes();
        }

        QName[] requestedFtNames = new QName[requestedFts.size()];
        int i = 0;
        for ( FeatureType requestedFt : requestedFts ) {
            requestedFtNames[i++] = requestedFt.getName();
        }

        if ( schemaLocation == null || schemaLocation.isEmpty() ) {
            schemaLocation = WebFeatureService.getSchemaLocation( requestVersion, gmlVersion, requestedFtNames );
        } else {
            schemaLocation += " " + WebFeatureService.getSchemaLocation( requestVersion, gmlVersion, requestedFtNames );
        }

        return schemaLocation;
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

    protected String getTimestamp() {
        DateTime dateTime = getCurrentDateTimeWithoutMilliseconds();
        return ISO8601Converter.formatDateTime( dateTime );
    }

    protected DateTime getCurrentDateTimeWithoutMilliseconds() {
        long msSince1970 = new Date().getTime();
        msSince1970 = msSince1970 / 1000 * 1000;
        return new DateTime( new Date( msSince1970 ), GMT );
    }

}
