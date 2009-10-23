//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

package org.deegree.protocol.wfs.getfeaturewithlock;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.getfeature.ResultType.HITS;
import static org.deegree.protocol.wfs.getfeature.ResultType.RESULTS;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.types.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.CRS;
import org.deegree.filter.Filter;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.AbstractWFSRequestKVPAdapter;
import org.deegree.protocol.wfs.getfeature.BBoxQuery;
import org.deegree.protocol.wfs.getfeature.FeatureIdQuery;
import org.deegree.protocol.wfs.getfeature.FilterQuery;
import org.deegree.protocol.wfs.getfeature.GetFeatureKVPAdapter;
import org.deegree.protocol.wfs.getfeature.Query;
import org.deegree.protocol.wfs.getfeature.ResultType;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.getfeature.XLinkPropertyName;

/**
 * Adapter between KVP <code>GetFeatureWithLock</code> requests and {@link GetFeatureWithLock} objects.
 * <p>
 * See specification in WFS v1.1 implementation document, clause 14.7.3.1. Differs from {@link GetFeatureKVPAdapter}
 * only with the the EXPIRY keyword.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:schneider@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetFeatureWithLockKVPAdapter extends AbstractWFSRequestKVPAdapter {

    /**
     * Parses a normalized KVP-map as a WFS {@link GetFeatureWithLock} request.
     * <p>
     * Supported versions:
     * <ul>
     * <li>WFS 1.0.0</li>
     * <li>WFS 1.1.0</li>
     * </ul>
     * 
     * @param kvpParams
     *            normalized KVP-map; keys must be uppercase, each key only has one associated value
     * @return parsed {@link GetFeatureWithLock} request
     * @throws Exception
     */
    public static GetFeatureWithLock parse( Map<String, String> kvpParams )
                            throws Exception {

        Version version = Version.parseVersion( KVPUtils.getRequired( kvpParams, "VERSION" ) );

        GetFeatureWithLock result = null;
        // if ( VERSION_100.equals( version ) ) {
        // result = parse100( kvpParams );
        // } else
        if ( VERSION_110.equals( version ) ) {
            result = parse110( kvpParams );
            // } else if ( VERSION_200.equals( version ) ) {
            // result = parse200( kvpParams );
        } else {
            String msg = Messages.get( "UNSUPPORTED_VERSION", version, Version.getVersionsString( VERSION_110 ) );
            throw new InvalidParameterValueException( msg );
        }
        return result;
    }

    @SuppressWarnings("boxing")
    private static GetFeatureWithLock parse110( Map<String, String> kvpParams )
                            throws Exception {
        // optional: 'NAMESPACE'
        Map<String, String> nsBindings = extractNamespaceBindings( kvpParams );

        NamespaceContext nsContext = new NamespaceContext();
        if ( nsBindings != null ) {
            for ( String key : nsBindings.keySet() ) {
                nsContext.addNamespace( key, nsBindings.get( key ) );
            }
        }

        // optional: 'OUTPUTFORMAT'
        String outputFormat = KVPUtils.getDefault( kvpParams, "OUTPUTFORMAT", "text/xml; subtype=gml/3.1.1" );

        // optional: 'RESULTTYPE'
        ResultType resultType = RESULTS;
        if ( kvpParams.get( "RESULTTYPE" ) != null && kvpParams.get( "RESULTTYPE" ).equalsIgnoreCase( "hits" ) ) {
            resultType = HITS;
        }

        // optional: SRSNAME
        String srsName = kvpParams.get( "SRSNAME" );
        CRS srs = null; // TODO should it be WGS:84, or EPSG:4326 ??
        if ( srsName != null ) {
            srs = new CRS( srsName );
        }

        // optional: MAXFEATURES
        Integer maxFeatures = null;
        String maxFeatureStr = kvpParams.get( "MAXFEATURES" );
        if ( maxFeatureStr != null ) {
            maxFeatures = Integer.parseInt( maxFeatureStr );
        }

        // optional: EXPIRY -- specific for GetFeatureWithLock
        Integer expiry = null;
        String expiryStr = kvpParams.get( "EXPIRY" );
        if ( expiryStr != null ) {
            expiry = Integer.parseInt( expiryStr );
        }

        // optional: 'PROPERTYNAME'
        String propertyStr = kvpParams.get( "PROPERTYNAME" );
        PropertyName[][] propertyNames = getPropertyNames( propertyStr, nsContext );

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
        Integer traverseXlinkExpiry = null;
        if ( traverseXlinkExpiryStr != null ) {
            try {
                traverseXlinkExpiry = Integer.parseInt( traverseXlinkExpiryStr );
            } catch ( NumberFormatException e ) {
                e.printStackTrace();
                throw new InvalidParameterValueException( e.getMessage(), e );
            }
        }

        XLinkPropertyName[][] xlinkPropNames = getXLinkPropNames( propertyNames, ptxDepthAr, ptxExpAr,
                                                                  traverseXlinkDepth, traverseXlinkExpiry );

        Query[] queries = null;

        if ( ( featureIdStr != null && bboxStr != null ) || ( featureIdStr != null && filterStr != null )
             || ( bboxStr != null && filterStr != null ) ) {
            // TODO make new exception
            throw new Exception( "The FEATUREID, BBOX and FILTER keywords are mutually exclusive!" );
        }

        if ( featureIdStr != null ) {

            queries = new Query[1];
            queries[0] = new FeatureIdQuery( null, typeNames, featureIds, featureVersion, srs, propertyNames,
                                             xlinkPropNames, sortBy );
            return new GetFeatureWithLock( VERSION_110, null, resultType, outputFormat, maxFeatures,
                                           traverseXlinkDepth, traverseXlinkExpiry, queries, expiry );
        }

        if ( bboxStr != null ) {
            if ( typeNames == null ) {
                // TODO make new exception
                throw new Exception( "The TYPENAME keyword is mandatory if BBOX is present!" );
            }

            String[] coordList = bboxStr.split( "," );
            if ( coordList.length % 2 == 1 ) {
                srs = new CRS( coordList[coordList.length - 1] );
            }

            Envelope bbox = createEnvelope( bboxStr, srs );

            queries = new Query[1];
            queries[0] = new BBoxQuery( null, typeNames, featureVersion, srs, propertyNames, null, sortBy, bbox );

            return new GetFeatureWithLock( VERSION_110, null, resultType, outputFormat, maxFeatures,
                                           traverseXlinkDepth, traverseXlinkExpiry, queries, expiry );
        }

        if ( filterStr != null || typeNames != null ) {
            if ( typeNames == null ) {
                // TODO make new exception
                throw new Exception( "The FILTER element requires the TYPENAME element" );
            }

            int length = typeNames.length;

            String[] filters = getFilters( filterStr );

            queries = new Query[length];
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
                    if ( xlinkPropNames != null ) {
                        queries[i] = new FilterQuery( null, new TypeName[] { typeNames[i] }, featureVersion, srs,
                                                      propertyNames[i], xlinkPropNames[i], null, sortBy, filter );
                    } else {
                        queries[i] = new FilterQuery( null, new TypeName[] { typeNames[i] }, featureVersion, srs,
                                                      propertyNames[i], null, null, sortBy, filter );
                    }
                } else {
                    if ( xlinkPropNames != null ) {
                        queries[i] = new FilterQuery( null, new TypeName[] { typeNames[i] }, featureVersion, srs, null,
                                                      xlinkPropNames[i], null, sortBy, filter );
                    } else {
                        queries[i] = new FilterQuery( null, new TypeName[] { typeNames[i] }, featureVersion, srs, null,
                                                      null, null, sortBy, filter );
                    }
                }
            }
            return new GetFeatureWithLock( VERSION_110, null, resultType, outputFormat, maxFeatures,
                                           traverseXlinkDepth, traverseXlinkExpiry, queries, expiry );
        }
        return null;
    }

    private static String[] getFilters( String filterStr ) {
        String[] filters = null;
        if ( filterStr != null ) {
            filters = filterStr.split( "[)][(]" );
            if ( filters[0].startsWith( "(" ) ) {
                filters[0] = filters[0].substring( 1 );
            }

            String last = filters[filters.length - 1];
            if ( last.endsWith( ")" ) ) {
                filters[filters.length - 1] = last.substring( 0, last.length() - 1 );
            }
        }
        return filters;
    }

    @SuppressWarnings("boxing")
    private static Envelope createEnvelope( String bboxStr, CRS srs ) {
        String[] coordList = bboxStr.split( "," );

        int n = coordList.length / 2;
        List<Double> lowerCorner = new ArrayList<Double>();
        for ( int i = 0; i < n; i++ ) {
            lowerCorner.add( Double.parseDouble( coordList[i] ) );
        }
        List<Double> upperCorner = new ArrayList<Double>();
        for ( int i = n; i < 2 * n; i++ ) {
            upperCorner.add( Double.parseDouble( coordList[i] ) );
        }

        GeometryFactory gf = new GeometryFactory();

        return gf.createEnvelope( lowerCorner, upperCorner, srs );
    }

    private static XLinkPropertyName[][] getXLinkPropNames( PropertyName[][] propertyNames, String[][] ptxDepthAr,
                                                            Integer[][] ptxExpAr, String traverseXlinkDepth,
                                                            Integer traverseXlinkExpiry ) {
        XLinkPropertyName[][] result = null;
        if ( propertyNames != null ) {
            result = new XLinkPropertyName[propertyNames.length][];

            for ( int i = 0; i < propertyNames.length; i++ ) {
                result[i] = new XLinkPropertyName[propertyNames[i].length];
                for ( int j = 0; j < propertyNames[i].length; j++ ) {

                    if ( ptxDepthAr != null ) {
                        if ( ptxExpAr != null ) {
                            result[i][j] = new XLinkPropertyName( propertyNames[i][j], ptxDepthAr[i][j], ptxExpAr[i][j] );
                        } else {
                            result[i][j] = new XLinkPropertyName( propertyNames[i][j], ptxDepthAr[i][j],
                                                                  traverseXlinkExpiry );
                        }
                    } else {
                        if ( ptxExpAr != null ) {
                            result[i][j] = new XLinkPropertyName( propertyNames[i][j], traverseXlinkDepth,
                                                                  ptxExpAr[i][j] );
                        } else {
                            result[i][j] = new XLinkPropertyName( propertyNames[i][j], traverseXlinkDepth,
                                                                  traverseXlinkExpiry );
                        }
                    }
                }
            }
        }
        return result;
    }

    private static TypeName[] getTypeNames( String typeStrList, Map<String, String> nsBindings ) {
        TypeName[] result = null;
        if ( typeStrList != null ) {

            String[] typeList = typeStrList.split( "," );
            result = new TypeName[typeList.length];

            for ( int i = 0; i < typeList.length; i++ ) {
                String[] typeParts = typeList[i].split( ":" );
                if ( typeParts.length == 2 ) {

                    // check if it has an alias
                    int equalSign;
                    if ( ( equalSign = typeParts[1].indexOf( "=" ) ) != -1 ) {
                        result[i] = new TypeName(
                                                  new QName( nsBindings.get( typeParts[0] ), typeParts[1], typeParts[0] ),
                                                  typeParts[1].substring( equalSign + 1 ) );
                    } else {
                        result[i] = new TypeName(
                                                  new QName( nsBindings.get( typeParts[0] ), typeParts[1], typeParts[0] ),
                                                  null );
                    }
                } else {
                    result[i] = new TypeName( new QName( typeParts[0] ), null );
                }
            }
        }
        return result;
    }

    private static SortProperty[] getSortBy( String sortbyStr, NamespaceContext nsContext ) {
        SortProperty[] result = null;
        if ( sortbyStr != null ) {
            String[] sortbyComm = sortbyStr.split( "," );

            result = new SortProperty[sortbyComm.length];
            for ( int i = 0; i < sortbyComm.length; i++ ) {
                if ( sortbyComm[i].endsWith( " D" ) ) {
                    String sortbyProp = sortbyComm[i].substring( 0, sortbyComm[i].indexOf( " " ) );
                    result[i] = new SortProperty( new PropertyName( sortbyProp, nsContext ), false );

                } else {

                    if ( sortbyComm[i].endsWith( " A" ) ) {
                        String sortbyProp = sortbyComm[i].substring( 0, sortbyComm[i].indexOf( " " ) );
                        result[i] = new SortProperty( new PropertyName( sortbyProp, nsContext ), true );

                    } else {
                        result[i] = new SortProperty( new PropertyName( sortbyComm[i], nsContext ), true );
                    }
                }
            }
        }
        return result;
    }

    private static PropertyName[][] getPropertyNames( String propertyStr, NamespaceContext nsContext ) {
        PropertyName[][] result = null;
        if ( propertyStr != null ) {
            String[][] propComm = parseParamList( propertyStr );

            result = new PropertyName[propComm.length][];
            for ( int i = 0; i < propComm.length; i++ ) {
                result[i] = new PropertyName[propComm[i].length];

                for ( int j = 0; j < propComm[i].length; j++ ) {
                    result[i][j] = new PropertyName( propComm[i][j], nsContext );
                }
            }
        }
        return result;
    }

    @SuppressWarnings("boxing")
    private static Integer[][] parseParamListAsInts( String paramList ) {
        String[][] strings = parseParamList( paramList );

        Integer[][] result = new Integer[strings.length][];
        for ( int i = 0; i < strings.length; i++ ) {
            result[i] = new Integer[strings[i].length];

            for ( int j = 0; j < strings[i].length; j++ ) {
                try {
                    result[i][j] = Integer.parseInt( strings[i][j] );

                } catch ( NumberFormatException e ) {
                    e.printStackTrace();
                    throw new InvalidParameterValueException( e.getMessage(), e );
                }
            }
        }

        return result;
    }

    private static String[][] parseParamList( String paramList ) {
        String[] paramPar = paramList.split( "[)][(]" );

        if ( paramPar[0].startsWith( "(" ) ) {
            paramPar[0] = paramPar[0].substring( 1 );
        }
        String last = paramPar[paramPar.length - 1];
        if ( last.endsWith( ")" ) ) {
            paramPar[paramPar.length - 1] = last.substring( 0, last.length() - 1 );
        }

        // split on commas
        String[][] paramComm = new String[paramPar.length][];
        for ( int i = 0; i < paramPar.length; i++ ) {
            paramComm[i] = paramPar[i].split( "," );
        }

        return paramComm;
    }
}
