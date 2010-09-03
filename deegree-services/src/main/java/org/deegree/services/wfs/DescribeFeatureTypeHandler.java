//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.services.wfs;

import static org.deegree.commons.xml.CommonNamespaces.GML_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XSNS;
import static org.deegree.commons.xml.CommonNamespaces.XS_PREFIX;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.gml.feature.schema.ApplicationSchemaXSDEncoder.GML_2_DEFAULT_INCLUDE;
import static org.deegree.gml.feature.schema.ApplicationSchemaXSDEncoder.GML_30_DEFAULT_INCLUDE;
import static org.deegree.gml.feature.schema.ApplicationSchemaXSDEncoder.GML_31_DEFAULT_INCLUDE;
import static org.deegree.gml.feature.schema.ApplicationSchemaXSDEncoder.GML_32_DEFAULT_INCLUDE;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_PREFIX;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.Version;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDEncoder;
import org.deegree.protocol.wfs.WFSConstants;
import org.deegree.protocol.wfs.describefeaturetype.DescribeFeatureType;
import org.deegree.protocol.wfs.describefeaturetype.DescribeFeatureTypeKVPAdapter;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link DescribeFeatureType} requests for the {@link WFSController}.
 * 
 * @see WFSController
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
class DescribeFeatureTypeHandler {

    private static final Logger LOG = LoggerFactory.getLogger( DescribeFeatureTypeHandler.class );

    private WFService service;

    /**
     * Creates a new {@link DescribeFeatureTypeHandler} instance that uses the given service to lookup requested
     * {@link FeatureType}s.
     * 
     * @param service
     *            WFS instance used to lookup the feature types
     */
    DescribeFeatureTypeHandler( WFService service ) {
        this.service = service;
    }

    /**
     * Performs the given {@link DescribeFeatureType} request.
     * <p>
     * If the request targets feature types in multiple namespaces, a WFS 2.0.0-style wrapper document is generated. The
     * response document embeds all feature type declarations from one of the namespaces and imports the declarations of
     * the feature types from the other namespaces using a KVP-<code>DescribeFeatureType</code> request that refers back
     * to the service.
     * </p>
     * 
     * @param request
     *            request to be handled
     * @param response
     *            response that is used to write the result
     * @throws OWSException
     *             if a WFS specific exception occurs, e.g. a requested feature type is not served
     * @throws XMLStreamException
     *             if writing the XML response fails
     * @throws IOException
     *             if an IO-error occurs
     */
    @SuppressWarnings("unchecked")
    void doDescribeFeatureType( DescribeFeatureType request, HttpResponseBuffer response )
                            throws OWSException, XMLStreamException, IOException {

        LOG.debug( "doDescribeFeatureType: " + request );

        GMLVersion version = determineRequestedGMLVersion( request );
        setContentType( version, response );

        LOG.debug( "contentType:" + response.getContentType() );
        LOG.debug( "characterEncoding:" + response.getCharacterEncoding() );

        XMLStreamWriter writer = WFSController.getXMLResponseWriter( response, null );

        // check for deegree-special DescribeFeatureType-request that asks for the WFS schema in a GML
        // version that does not match the WFS schema (e.g. WFS 1.1.0, GML 2)
        if ( request.getTypeNames() != null && request.getTypeNames().length == 1
             && "FeatureCollection".equals( request.getTypeNames()[0].getLocalPart() )
             && "wfs".equals( request.getTypeNames()[0].getPrefix() ) ) {
            writeWFSSchema( writer, request.getVersion(), version );
        } else {
            Map<String, List<FeatureType>> nsToFts = determineRequestedFeatureTypes( request );
            if ( nsToFts.size() == 1 ) {
                // specific feature types from single namespace -> one schema document suffices
                Map<String, String> importMap = buildImportMap( request, nsToFts.keySet() );
                Map<String, String> prefixToNs = service.getPrefixToNs();
                String namespace = nsToFts.keySet().iterator().next();
                ApplicationSchemaXSDEncoder exporter = new ApplicationSchemaXSDEncoder( version, namespace, importMap,
                                                                                        prefixToNs );
                exporter.export( writer, nsToFts.get( nsToFts.keySet().iterator().next() ) );
            } else if ( request.getTypeNames() == null && request.getNsBindings() != null ) {
                // all feature types from a single namespace
                String namespace = request.getNsBindings().get( "" );
                Map<String, String> importMap = buildImportMap( request, Collections.singletonList( namespace ) );
                Map<String, String> prefixToNs = service.getPrefixToNs();
                ApplicationSchemaXSDEncoder exporter = new ApplicationSchemaXSDEncoder( version, namespace, importMap,
                                                                                        prefixToNs );
                // TODO remove hack
                exporter.export( writer, service.getStores()[0].getSchema() );
            } else {
                // feature types from multiple namespaces -> generate wrapper schema document from all feature stores
                Set<String> namespaces = new TreeSet<String>();
                for ( FeatureStore fs : service.getStores() ) {
                    namespaces.addAll( fs.getSchema().getNamespaces() );
                }
                writeWrapperSchema( writer, request, version, namespaces );
            }
        }
        writer.flush();
    }

    private void writeWFSSchema( XMLStreamWriter writer, Version version, GMLVersion gmlVersion )
                            throws XMLStreamException {

        writer.writeStartElement( XSNS, "schema" );
        writer.writeAttribute( "attributeFormDefault", "unqualified" );
        writer.writeAttribute( "elementFormDefault", "qualified" );

        if ( VERSION_100.equals( version ) || VERSION_110.equals( version ) ) {
            writer.writeAttribute( "targetNamespace", WFS_NS );
            writer.writeNamespace( WFS_PREFIX, WFSConstants.WFS_NS );
        } else if ( VERSION_200.equals( version ) ) {
            writer.writeAttribute( "targetNamespace", WFS_200_NS );
            writer.writeNamespace( WFS_PREFIX, WFSConstants.WFS_200_NS );
        }

        writer.writeNamespace( XS_PREFIX, XSNS );
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
            // there is no FeatureCollection in GML 3.2 anymore
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

    private void writeWrapperSchema( XMLStreamWriter writer, DescribeFeatureType request, GMLVersion gmlVersion,
                                     Collection<String> namespaces )
                            throws XMLStreamException {

        Iterator<String> iter = namespaces.iterator();

        writer.writeStartElement( XSNS, "schema" );
        writer.writeAttribute( "attributeFormDefault", "unqualified" );
        writer.writeAttribute( "elementFormDefault", "qualified" );
        writer.writeAttribute( "targetNamespace", iter.next() );
        writer.writeNamespace( WFS_PREFIX, WFSConstants.WFS_NS );

        writer.writeNamespace( XS_PREFIX, XSNS );
        writer.writeNamespace( GML_PREFIX, gmlVersion.getNamespace() );
        while ( iter.hasNext() ) {
            String ns = iter.next();
            String prefix = service.getTargetNsToPrefix().get( ns );
            writer.writeNamespace( prefix, ns );
        }

        // import GML core schema
        writer.writeEmptyElement( XSNS, "import" );
        writer.writeAttribute( "namespace", gmlVersion.getNamespace() );
        switch ( gmlVersion ) {
        case GML_2:
            writer.writeAttribute( "schemaLocation", GML_2_DEFAULT_INCLUDE );
            break;
        case GML_30:
            writer.writeAttribute( "schemaLocation", GML_30_DEFAULT_INCLUDE );
            break;
        case GML_31:
            writer.writeAttribute( "schemaLocation", GML_31_DEFAULT_INCLUDE );
            break;
        case GML_32:
            writer.writeAttribute( "schemaLocation", GML_32_DEFAULT_INCLUDE );
            break;
        }

        boolean first = true;
        for ( String ns : namespaces ) {
            if ( first ) {
                writer.writeEmptyElement( XSNS, "include" );
                first = false;
            } else {
                writer.writeEmptyElement( XSNS, "import" );
                writer.writeAttribute( "namespace", ns );
            }
            writer.writeAttribute( "schemaLocation", buildDescribeFeatureTypeRequest( request, ns ) );
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
     * Determine all feature types that have to be included in the generated schema response.
     * <p>
     * This includes:
     * <nl>
     * <li>All explicitly requested feature types.</li>
     * <li>All feature types that may occur as values of the requested feature types.</li>
     * </nl>
     * </p>
     * 
     * @param request
     * @return key: namespace, value: list of feature types in the namespace
     * @throws OWSException
     */
    private Map<String, List<FeatureType>> determineRequestedFeatureTypes( DescribeFeatureType request )
                            throws OWSException {

        Set<FeatureType> fts = new LinkedHashSet<FeatureType>();
        if ( request.getTypeNames() == null || request.getTypeNames().length == 0 ) {
            if ( request.getNsBindings() == null ) {
                LOG.debug( "Describing all served feature types." );
                fts.addAll( service.getFeatureTypes() );
            } else {
                String ns = request.getNsBindings().values().iterator().next();
                LOG.debug( "Describing all feature types in namespace '" + ns + "'." );
                List<FeatureType> nsFts = service.getFeatureTypes().iterator().next().getSchema().getFeatureTypes(
                                                                                                                   ns,
                                                                                                                   true,
                                                                                                                   false );
                for ( FeatureType ft : nsFts ) {
                    addToClosure( ft, fts );
                }
            }
        } else {
            for ( QName ftName : request.getTypeNames() ) {
                FeatureType ft = service.lookupFeatureType( ftName );
                if ( ft == null ) {
                    throw new OWSException( Messages.get( "WFS_FEATURE_TYPE_NOT_SERVED", ftName ),
                                            OWSException.INVALID_PARAMETER_VALUE );
                }
                addToClosure( ft, fts );
            }
        }

        // sort per namespace
        Map<String, List<FeatureType>> nsToFts = new LinkedHashMap<String, List<FeatureType>>();
        for ( FeatureType ft : fts ) {
            List<FeatureType> nsFts = nsToFts.get( ft.getName().getNamespaceURI() );
            if ( nsFts == null ) {
                nsFts = new ArrayList<FeatureType>();
                nsToFts.put( ft.getName().getNamespaceURI(), nsFts );
            }
            nsFts.add( ft );
        }
        return nsToFts;
    }

    private void addToClosure( FeatureType ft, Set<FeatureType> fts ) {
        if ( !fts.contains( ft ) ) {
            fts.add( ft );
            for ( PropertyType pt : ft.getPropertyDeclarations() ) {
                if ( pt instanceof FeaturePropertyType ) {
                    FeatureType valueFt = ( (FeaturePropertyType) pt ).getValueFt();
                    LOG.debug( "Value ft of property " + pt + ": " + valueFt );
                    if ( valueFt == null ) {
                        LOG.debug( "Unrestricted feature type reference. Adding all served feature types from application schema." );
                        for ( FeatureType ft2 : ft.getSchema().getFeatureTypes() ) {
                            addToClosure( ft2, fts );
                        }
                    } else {
                        addToClosure( valueFt, fts );
                    }
                }
            }
        }
    }

    /**
     * Determines the GML version that is requested.
     * <p>
     * Evaluation strategy:
     * <nl>
     * <li>The <code>format</code> attribute is checked:
     * <ul>
     * <li><code>XMLSCHEMA -> GML 2.1.2</code></li>
     * <li><code>text/xml; subtype=gml/X -> GML X (2.1.2, 3.1.1 or 3.2.1)</code></li>
     * </ul>
     * </li>
     * <li>If the first step fails, the GML version is derived from the request version:
     * <ul>
     * <li><code>WFS 1.0.0 -> GML 2.1.2</code></li>
     * <li><code>WFS 1.1.0 -> GML 3.1.1</code></li>
     * <li><code>WFS 2.0.0 -> GML 3.2.1</code></li>
     * </ul>
     * </li>
     * </nl>
     * </p>
     * 
     * @param request
     * @return GML version to be used
     * @throws OWSException
     */
    GMLVersion determineRequestedGMLVersion( DescribeFeatureType request )
                            throws OWSException {

        GMLVersion version = null;

        String format = request.getOutputFormat();
        if ( format != null ) {
            // format is specified
            LOG.debug( "Determining GML version based on requested format: '" + format + "'" );
            if ( format.startsWith( "text/xml" ) ) {
                LOG.debug( "Starts with 'text/xml'" );
                int subTypePos = format.indexOf( "subtype=gml/" );
                if ( subTypePos != -1 ) {
                    LOG.debug( "Contains 'subtype=gml/'" );
                    // 12 is the length of "subtype=gml/"
                    int begin = subTypePos + 12;
                    String versionString = format.substring( begin, format.length() );
                    LOG.debug( "version string: '" + versionString + "'" );
                    if ( versionString.startsWith( "3.2" ) ) {
                        version = GMLVersion.GML_32;
                    } else if ( versionString.startsWith( "3.1" ) ) {
                        version = GMLVersion.GML_31;
                    } else if ( versionString.startsWith( "3.0" ) ) {
                        version = GMLVersion.GML_30;
                    } else if ( versionString.startsWith( "2" ) ) {
                        version = GMLVersion.GML_2;
                    } else {
                        LOG.debug( "Specified GML subtype format '" + format + "' is not understood." );
                    }
                } else {
                    LOG.debug( "No GML subtype found in format '" + format + "'." );
                }
            } else if ( "XMLSCHEMA".equals( format ) ) {
                version = GMLVersion.GML_2;
            } else {
                throw new OWSException( Messages.get( "WFS_OUTPUT_FORMAT_NOT_SUPPORTED", format ),
                                        OWSException.INVALID_FORMAT );
            }
        }

        if ( version == null ) {
            LOG.debug( "Determining GML version by request version." );
            if ( request.getVersion() == VERSION_100 ) {
                version = GMLVersion.GML_2;
            } else if ( request.getVersion() == VERSION_110 ) {
                version = GMLVersion.GML_31;
            } else if ( request.getVersion() == VERSION_200 ) {
                version = GMLVersion.GML_32;
            }
        }
        return version;
    }

    /**
     * Sets the content type header for the HTTP response.
     * 
     * TODO integrate handling for custom formats
     * 
     * @param outputFormat
     *            output format to be used, must not be <code>null</code>
     * @param response
     *            http response, must not be <code>null</code>
     */
    private void setContentType( GMLVersion outputFormat, HttpServletResponse response ) {

        switch ( outputFormat ) {
        case GML_2:
            response.setContentType( "text/xml; subtype=gml/2.1.2" );
            break;
        case GML_30:
            response.setContentType( "text/xml; subtype=gml/3.0.1" );
            break;
        case GML_31:
            response.setContentType( "text/xml; subtype=gml/3.1.1" );
            break;
        case GML_32:
            response.setContentType( "text/xml; subtype=gml/3.2.1" );
            break;
        }
    }
}
