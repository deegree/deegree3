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

package org.deegree.protocol.wfs.getfeature;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.getfeature.ResultType.HITS;
import static org.deegree.protocol.wfs.getfeature.ResultType.RESULTS;

import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.filter.Filter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.xml.Filter100XMLDecoder;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.query.BBoxQuery;
import org.deegree.protocol.wfs.query.FeatureIdQuery;
import org.deegree.protocol.wfs.query.FilterQuery;
import org.deegree.protocol.wfs.query.ProjectionClause;
import org.deegree.protocol.wfs.query.Query;
import org.deegree.protocol.wfs.query.QueryKVPAdapter;
import org.deegree.protocol.wfs.query.StandardPresentationParams;
import org.deegree.protocol.wfs.query.StandardResolveParams;

/**
 * Adapter between KVP <code>GetFeature</code> requests and {@link GetFeature} objects.
 * <p>
 * Supported WFS versions:
 * <ul>
 * <li>1.0.0</li>
 * <li>1.1.0</li>
 * <li>2.0.0</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetFeatureKVPAdapter extends QueryKVPAdapter {

    /**
     * Parses a normalized KVP-map as a WFS {@link GetFeature} request.
     * 
     * @param kvpParams
     *            normalized KVP-map; keys must be uppercase, each key only has one associated value
     * @param nsMap
     *            only for 1.0.0 version; the prefix-namespace map given in the NamespaceHints in the configuration
     * @return parsed {@link GetFeature} request
     * @throws Exception
     */
    public static GetFeature parse( Map<String, String> kvpParams, Map<String, String> nsMap )
                            throws Exception {

        Version version = Version.parseVersion( KVPUtils.getRequired( kvpParams, "VERSION" ) );

        GetFeature result = null;
        if ( VERSION_100.equals( version ) ) {
            result = parse100( kvpParams, nsMap );
        } else if ( VERSION_110.equals( version ) ) {
            result = parse110( kvpParams );
        } else if ( VERSION_200.equals( version ) ) {
            result = parse200( kvpParams );
        } else {
            String msg = Messages.get( "UNSUPPORTED_VERSION", version,
                                       Version.getVersionsString( VERSION_100, VERSION_110, VERSION_200 ) );
            throw new InvalidParameterValueException( msg );
        }
        return result;
    }

    @SuppressWarnings("boxing")
    private static GetFeature parse100( Map<String, String> kvpParams, Map<String, String> nsMap )
                            throws Exception {

        NamespaceBindings nsContext = new NamespaceBindings();
        if ( nsMap != null ) {
            for ( String key : nsMap.keySet() ) {
                nsContext.addNamespace( key, nsMap.get( key ) );
            }
        }

        // optional: MAXFEATURES
        BigInteger maxFeatures = KVPUtils.getBigInt( kvpParams, "MAXFEATURES", null );

        // ??? not in 1.0.0 spec, but CITE 1.0.0 test (wfs:test1.0.0-basic-getfeature-get-3) suggests this parameter
        String outputFormat = kvpParams.get( "OUTPUTFORMAT" );

        StandardPresentationParams presentationParams = new StandardPresentationParams( null, maxFeatures, null,
                                                                                        outputFormat );

        // optional: 'PROPERTYNAME'
        String propertyStr = kvpParams.get( "PROPERTYNAME" );
        ProjectionClause[][] propertyNames = getPropertyNames( propertyStr, nsContext );

        // optional: FEATUREVERSION
        String featureVersion = kvpParams.get( "FEATUREVERSION" );

        // mandatory: TYPENAME, but optional if FEATUREID is specified
        String typeStrList = kvpParams.get( "TYPENAME" );
        TypeName[] typeNames = getTypeNames100( typeStrList );

        // optional: FEATUREID
        String featureIdStr = kvpParams.get( "FEATUREID" );
        String[] featureIds = null;
        if ( featureIdStr != null ) {
            featureIds = featureIdStr.split( "," );
        }
        // optional: BBOX
        String bboxStr = kvpParams.get( "BBOX" );

        // optional: FILTER
        String filterStr = kvpParams.get( "FILTER" );

        // optional: SRSNAME (not specified in WFS 1.0.0, deegree extension)
        String srsName = kvpParams.get( "SRSNAME" );
        ICRS srs = null;
        if ( srsName != null ) {
            srs = CRSManager.getCRSRef( srsName );
        }

        List<Query> queries = new ArrayList<Query>();

        if ( ( featureIdStr != null && bboxStr != null ) || ( featureIdStr != null && filterStr != null )
             || ( bboxStr != null && filterStr != null ) ) {
            // TODO make new exception
            throw new Exception( "The FEATUREID, BBOX and FILTER keywords are mutually exclusive!" );
        }

        if ( featureIdStr != null ) {
            queries.add( new FeatureIdQuery( null, typeNames, featureIds, featureVersion, srs, propertyNames, null ) );
        } else if ( bboxStr != null ) {
            if ( typeNames == null ) {
                // TODO make new exception
                throw new Exception( "The TYPENAME keyword is mandatory if BBOX is present!" );
            }

            String[] coordList = bboxStr.split( "," );
            ICRS bboxCrs = null;
            if ( coordList.length % 2 == 1 ) {
                bboxCrs = CRSManager.getCRSRef( coordList[coordList.length - 1] );
            }

            Envelope bbox = createEnvelope( bboxStr, bboxCrs );
            queries.add( new BBoxQuery( null, typeNames, featureVersion, srs, propertyNames, null, bbox ) );
        } else if ( filterStr != null || typeNames != null ) {
            if ( typeNames == null ) {
                // TODO make new exception
                throw new Exception( "The FILTER element requires the TYPENAME element" );
            }

            int length = typeNames.length;

            String[] filters = getFilters( filterStr );

            for ( int i = 0; i < length; i++ ) {
                Filter filter = null;
                if ( filters != null ) {

                    StringReader sr = new StringReader( filters[i] );
                    XMLAdapter adapter = new XMLAdapter( sr );
                    XMLStreamReaderWrapper streamWrapper = new XMLStreamReaderWrapper(
                                                                                       adapter.getRootElement().getXMLStreamReaderWithoutCaching(),
                                                                                       adapter.getSystemId() );
                    try {
                        streamWrapper.nextTag();
                        filter = Filter100XMLDecoder.parse( streamWrapper );
                    } catch ( XMLParsingException e ) {
                        e.printStackTrace();
                        // TODO raise exception
                    } catch ( XMLStreamException e ) {
                        e.printStackTrace();
                        // TODO raise exception
                    }
                }
                if ( propertyNames != null ) {
                    queries.add( new FilterQuery( null, new TypeName[] { typeNames[i] }, featureVersion, srs,
                                                  propertyNames[i], null, filter ) );
                } else {
                    queries.add( new FilterQuery( null, new TypeName[] { typeNames[i] }, featureVersion, srs, null,
                                                  null, filter ) );
                }
            }
        }
        return new GetFeature( VERSION_100, null, presentationParams, null, queries );
    }

    @SuppressWarnings("boxing")
    private static GetFeature parse110( Map<String, String> kvpParams )
                            throws Exception {

        // optional: 'NAMESPACE'
        Map<String, String> nsBindings = extractNamespaceBindings110( kvpParams );
        if ( nsBindings == null ) {
            nsBindings = Collections.emptyMap();
        }

        NamespaceBindings nsContext = new NamespaceBindings();
        if ( nsBindings != null ) {
            for ( String key : nsBindings.keySet() ) {
                nsContext.addNamespace( key, nsBindings.get( key ) );
            }
        }

        // optional: 'OUTPUTFORMAT'
        String outputFormat = kvpParams.get( "OUTPUTFORMAT" );

        // optional: 'RESULTTYPE'
        ResultType resultType = RESULTS;
        if ( kvpParams.get( "RESULTTYPE" ) != null && kvpParams.get( "RESULTTYPE" ).equalsIgnoreCase( "hits" ) ) {
            resultType = HITS;
        }

        // optional: SRSNAME
        String srsName = kvpParams.get( "SRSNAME" );
        ICRS srs = null;
        if ( srsName != null ) {
            srs = CRSManager.getCRSRef( srsName );
        }

        // optional: MAXFEATURES
        BigInteger maxFeatures = KVPUtils.getBigInt( kvpParams, "MAXFEATURES", null );

        StandardPresentationParams presentationParams = new StandardPresentationParams( null, maxFeatures, resultType,
                                                                                        outputFormat );

        // optional: 'PROPERTYNAME'
        String propertyStr = kvpParams.get( "PROPERTYNAME" );
        ProjectionClause[][] propertyNames = getPropertyNames( propertyStr, nsContext );

        // optional: SORTBY
        String sortbyStr = kvpParams.get( "SORTBY" );
        SortProperty[] sortBy = getSortBy( sortbyStr, nsContext );

        // optional: FEATUREVERSION
        String featureVersion = kvpParams.get( "FEATUREVERSION" );

        // mandatory: TYPENAME, but optional if FEATUREID is specified
        String typeStrList = kvpParams.get( "TYPENAME" );
        TypeName[] typeNames = getTypeNames( typeStrList, nsBindings );

        // optional: FEATUREID
        String featureIdStr = kvpParams.get( "FEATUREID" );
        String[] featureIds = null;
        if ( featureIdStr != null ) {
            featureIds = featureIdStr.split( "," );
        }
        // optional: BBOX
        String bboxStr = kvpParams.get( "BBOX" );

        // optional: FILTER
        String filterStr = kvpParams.get( "FILTER" );

        // optional: 'PROPTRAVXLINKDEPTH'
        String propTravXlinkDepth = kvpParams.get( "PROPTRAVXLINKDEPTH" );
        String[][] ptxDepthAr = null;
        if ( propTravXlinkDepth != null ) {
            ptxDepthAr = parseParamList( propTravXlinkDepth );
        }

        // optional: 'PROPTRAVXLINKEXPIRY'
        String propTravXlinkExpiry = kvpParams.get( "PROPTRAVXLINKEXPIRY" );
        Integer[][] ptxExpAr = null;
        if ( propTravXlinkExpiry != null ) {
            ptxExpAr = parseParamListAsInts( propTravXlinkDepth );
        }

        // optional: 'TRAVERSEXLINKDEPTH'
        String traverseXlinkDepth = kvpParams.get( "TRAVERSEXLINKDEPTH" );

        // optional: 'TRAVERSEXLINKEXPIRY'
        String traverseXlinkExpiryStr = kvpParams.get( "TRAVERSEXLINKEXPIRY" );
        BigInteger resolveTimeout = null;
        Integer traverseXlinkExpiry = null;
        if ( traverseXlinkExpiryStr != null ) {
            try {
                traverseXlinkExpiry = Integer.parseInt( traverseXlinkExpiryStr );
                resolveTimeout = BigInteger.valueOf( traverseXlinkExpiry * 60 );
            } catch ( NumberFormatException e ) {
                e.printStackTrace();
                throw new InvalidParameterValueException( e.getMessage(), e );
            }
        }

        StandardResolveParams resolveParams = new StandardResolveParams( null, propTravXlinkExpiry, resolveTimeout );
        propertyNames = getXLinkPropNames( propertyNames, ptxDepthAr, ptxExpAr, traverseXlinkDepth, traverseXlinkExpiry );
        List<Query> queries = new ArrayList<Query>();

        if ( ( featureIdStr != null && bboxStr != null ) || ( featureIdStr != null && filterStr != null )
             || ( bboxStr != null && filterStr != null ) ) {
            // TODO make new exception
            throw new Exception( "The FEATUREID, BBOX and FILTER keywords are mutually exclusive!" );
        }

        if ( featureIdStr != null ) {
            queries.add( new FeatureIdQuery( null, typeNames, featureIds, featureVersion, srs, propertyNames, sortBy ) );
        } else if ( bboxStr != null ) {
            if ( typeNames == null ) {
                // TODO make new exception
                throw new Exception( "The TYPENAME keyword is mandatory if BBOX is present!" );
            }

            String[] coordList = bboxStr.split( "," );

            // NOTE: Contradiction between spec and CITE tests (for omitted crsUri)
            // - WFS 1.1.0 spec, 14.3.3: coordinates should be in WGS84
            // - CITE tests, wfs:wfs-1.1.0-Basic-GetFeature-tc8.1: If no CRS reference is provided, a service-defined
            // default value must be assumed.
            ICRS bboxCrs = null;
            if ( coordList.length % 2 == 1 ) {
                bboxCrs = CRSManager.getCRSRef( coordList[coordList.length - 1] );
            }

            Envelope bbox = createEnvelope( bboxStr, bboxCrs );
            queries.add( new BBoxQuery( null, typeNames, featureVersion, srs, propertyNames, sortBy, bbox ) );
        } else if ( filterStr != null || typeNames != null ) {
            if ( typeNames == null ) {
                // TODO make new exception
                throw new Exception( "The FILTER element requires the TYPENAME element" );
            }

            int length = typeNames.length;

            String[] filters = getFilters( filterStr );

            for ( int i = 0; i < length; i++ ) {
                Filter filter = null;
                if ( filters != null ) {

                    StringReader sr = new StringReader( filters[i] );
                    XMLAdapter adapter = new XMLAdapter( sr );
                    XMLStreamReaderWrapper streamWrapper = new XMLStreamReaderWrapper(
                                                                                       adapter.getRootElement().getXMLStreamReaderWithoutCaching(),
                                                                                       adapter.getSystemId() );
                    try {
                        streamWrapper.nextTag();
                        filter = Filter110XMLDecoder.parse( streamWrapper );
                    } catch ( XMLParsingException e ) {
                        e.printStackTrace();
                        // TODO raise exception
                    } catch ( XMLStreamException e ) {
                        e.printStackTrace();
                        // TODO raise exception
                    }
                }
                if ( propertyNames != null ) {
                    queries.add( new FilterQuery( null, new TypeName[] { typeNames[i] }, featureVersion, srs,
                                                  propertyNames[i], sortBy, filter ) );
                } else {
                    queries.add( new FilterQuery( null, new TypeName[] { typeNames[i] }, featureVersion, srs, null,
                                                  sortBy, filter ) );
                }
            }
        }
        return new GetFeature( VERSION_110, null, presentationParams, resolveParams, queries );
    }

    private static GetFeature parse200( Map<String, String> kvpParams )
                            throws Exception {
        StandardPresentationParams presentationParams = parseStandardPresentationParameters200( kvpParams );
        StandardResolveParams resolveParams = parseStandardResolveParameters200( kvpParams );
        List<Query> queries = parseQueries200( kvpParams );
        return new GetFeature( VERSION_200, null, presentationParams, resolveParams, queries );
    }
}
