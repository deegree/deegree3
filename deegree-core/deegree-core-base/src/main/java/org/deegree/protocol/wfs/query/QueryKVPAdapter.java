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
package org.deegree.protocol.wfs.query;

import static org.deegree.commons.utils.kvp.KVPUtils.getBigInt;
import static org.deegree.commons.xml.CommonNamespaces.FES_20_NS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipStartDocument;
import static org.deegree.protocol.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.protocol.ows.exception.OWSException.MISSING_PARAMETER_VALUE;

import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.deegree.commons.tom.ResolveMode;
import org.deegree.commons.tom.ResolveParams;
import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.filter.Filter;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.xml.Filter200XMLDecoder;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.wfs.AbstractWFSRequestKVPAdapter;
import org.deegree.protocol.wfs.getfeature.ResultType;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryKVPAdapter extends AbstractWFSRequestKVPAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( QueryKVPAdapter.class );

    protected static StandardPresentationParams parseStandardPresentationParameters200( Map<String, String> kvpUC ) {

        // optional: STARTINDEX
        BigInteger startIndex = getBigInt( kvpUC, "STARTINDEX", null );

        // optional: COUNT
        BigInteger count = getBigInt( kvpUC, "COUNT", null );

        // optional: OUTPUTFORMAT
        String outputFormat = kvpUC.get( "OUTPUTFORMAT" );

        // optional: RESULTTYPE
        ResultType resultType = null;
        String resultTypeStr = kvpUC.get( "RESULTTYPE" );
        if ( "hits".equalsIgnoreCase( resultTypeStr ) ) {
            resultType = ResultType.HITS;
        } else if ( "results".equalsIgnoreCase( resultTypeStr ) ) {
            resultType = ResultType.RESULTS;
        }

        return new StandardPresentationParams( startIndex, count, resultType, outputFormat );
    }

    protected static ResolveParams parseStandardResolveParameters200( Map<String, String> kvpUC ) {

        // optional: RESOLVE
        ResolveMode resolve = null;
        String resolveString = kvpUC.get( "RESOLVE" );
        if ( resolveString != null ) {
            if ( resolveString.equalsIgnoreCase( "local" ) ) {
                resolve = ResolveMode.LOCAL;
            } else if ( resolveString.equalsIgnoreCase( "remote" ) ) {
                resolve = ResolveMode.REMOTE;
            } else if ( resolveString.equalsIgnoreCase( "none" ) ) {
                resolve = ResolveMode.NONE;
            } else if ( resolveString.equalsIgnoreCase( "all" ) ) {
                resolve = ResolveMode.ALL;
            } else {
                LOG.warn( "Invalid value (='{}') for resolve parameter.", resolveString );
            }
        }

        // optional: RESOLVEDEPTH
        String resolveDepth = kvpUC.get( "RESOLVEDEPTH" );

        // optional: RESOLVETIMEOUT
        BigInteger resolveTimeout = getBigInt( kvpUC, "RESOLVETIMEOUT", null );

        return new ResolveParams( resolve, resolveDepth, resolveTimeout );
    }

    protected static List<Query> parseQueries200( Map<String, String> kvpUC )
                            throws Exception {

        List<Query> queries = null;
        if ( kvpUC.containsKey( "STOREDQUERY_ID" ) ) {
            queries = parseStoredQuery200( kvpUC );
        } else {
            queries = parseAdhocQueries200( kvpUC );
        }
        return queries;
    }

    private static List<Query> parseAdhocQueries200( Map<String, String> kvpUC )
                            throws Exception {

        int numQueries = 0;

        // optional: 'NAMESPACE'
        List<NamespaceBindings> namespacesList = new ArrayList<NamespaceBindings>();
        if ( kvpUC.get( "NAMESPACE" ) != null ) {
            List<String> params = KVPUtils.splitLists( kvpUC.get( "NAMESPACE" ) );
            numQueries = params.size();
            for ( String param : params ) {
                Map<String, String> nsBindings2 = extractNamespaceBindings200( param );
                if ( nsBindings2 == null ) {
                    nsBindings2 = Collections.emptyMap();
                }
                NamespaceBindings nsContext = new NamespaceBindings();
                if ( nsBindings2 != null ) {
                    for ( String key : nsBindings2.keySet() ) {
                        nsContext.addNamespace( key, nsBindings2.get( key ) );
                    }
                }
                namespacesList.add( nsContext );
            }
        }

        // optional: ALIASES
        List<String[]> aliasesList = new ArrayList<String[]>();
        if ( kvpUC.get( "ALIASES" ) != null ) {
            List<String> params = KVPUtils.splitLists( kvpUC.get( "ALIASES" ) );
            if ( numQueries != 0 && params.size() != numQueries ) {
                String msg = "Invalid Ad hoc multi-query KVP request. List sizes of all specified query params must match.";
                throw new OWSException( msg, MISSING_PARAMETER_VALUE );
            } else {
                numQueries = params.size();
            }
            for ( String param : params ) {
                String[] a = StringUtils.split( param, "," );
                aliasesList.add( a );
            }
        }

        // mandatory: TYPENAMES (optional if RESOURCEID is present)
        List<TypeName[]> typeNamesList = new ArrayList<TypeName[]>();
        if ( kvpUC.get( "TYPENAMES" ) != null ) {
            List<String> params = KVPUtils.splitLists( kvpUC.get( "TYPENAMES" ) );
            if ( numQueries != 0 && params.size() != numQueries ) {
                String msg = "Invalid Ad hoc multi-query KVP request. List sizes of all specified query params must match.";
                throw new OWSException( msg, MISSING_PARAMETER_VALUE );
            } else {
                numQueries = params.size();
            }
            for ( int i = 0; i < numQueries; i++ ) {
                NamespaceBindings nsBindings = null;
                if ( !namespacesList.isEmpty() ) {
                    nsBindings = namespacesList.get( i );
                }
                String[] alias = null;
                if ( !aliasesList.isEmpty() ) {
                    alias = aliasesList.get( i );
                }
                String typeNameStr = params.get( i );
                String[] tokens = StringUtils.split( typeNameStr, " " );
                if ( alias != null && alias.length != tokens.length ) {
                    String msg = "Number of entries in 'ALIASES' and 'TYPENAMES' parameters does not match.";
                    throw new OWSException( msg, INVALID_PARAMETER_VALUE, "aliases" );
                }
                TypeName[] typeName = new TypeName[tokens.length];
                for ( int j = 0; j < tokens.length; j++ ) {
                    String a = alias != null ? alias[j] : null;
                    String token = tokens[j];
                    if ( token.startsWith( "schema-element(" ) && token.endsWith( ")" ) ) {
                        String prefixedName = token.substring( 15, token.length() - 1 );
                        QName qName = resolveQName( prefixedName, nsBindings );
                        typeName[i] = new TypeName( qName, a, true );
                    } else {
                        QName qName = resolveQName( token, nsBindings );
                        typeName[i] = new TypeName( qName, a, false );
                    }
                }
                typeNamesList.add( typeName );
            }
        }

        // optional: SRSNAME
        List<ICRS> srsNames = new ArrayList<ICRS>();
        if ( kvpUC.get( "SRSNAME" ) != null ) {
            List<String> params = KVPUtils.splitLists( kvpUC.get( "SRSNAME" ) );
            if ( numQueries != 0 && params.size() != numQueries ) {
                String msg = "Invalid Ad hoc multi-query KVP request. List sizes of all specified query params must match.";
                throw new OWSException( msg, MISSING_PARAMETER_VALUE );
            } else {
                numQueries = params.size();
            }
            for ( String param : params ) {
                srsNames.add( CRSManager.getCRSRef( param ) );
            }
        }

        // optional: PROPERTYNAME
        List<ProjectionClause[]> projectionClausesList = new ArrayList<ProjectionClause[]>();
        // TODO

        // optional: FILTER
        List<Filter> filterList = new ArrayList<Filter>();
        if ( kvpUC.get( "FILTER" ) != null ) {
            List<String> params = KVPUtils.splitLists( kvpUC.get( "FILTER" ) );
            if ( numQueries != 0 && params.size() != numQueries ) {
                String msg = "Invalid Ad hoc multi-query KVP request. List sizes of all specified query params must match.";
                throw new OWSException( msg, MISSING_PARAMETER_VALUE );
            } else {
                numQueries = params.size();
            }
            for ( String param : params ) {
                filterList.add( parseFilter200( param ) );
            }
        }

        // optional: FILTER_LANGUAGE
        List<String> filterLanguageList = new ArrayList<String>();
        // TODO

        // optional: RESOURCEID
        List<String[]> resourceIdList = new ArrayList<String[]>();
        // TODO

        // optional: BBOX (yes, this is not a list, see 7.9.2.3)
        Envelope bbox = null;
        // TODO

        // optional: SORTBY
        List<SortProperty[]> sortByList = new ArrayList<SortProperty[]>();
        if ( kvpUC.get( "SORTBY" ) != null ) {
            List<String> params = KVPUtils.splitLists( kvpUC.get( "SORTBY" ) );
            if ( numQueries != 0 && params.size() != numQueries ) {
                String msg = "Invalid Ad hoc multi-query KVP request. List sizes of all specified query params must match.";
                throw new OWSException( msg, MISSING_PARAMETER_VALUE );
            } else {
                numQueries = params.size();
            }
            for ( int i = 0; i < sortByList.size(); i++ ) {
                String param = params.get( i );
                // TODO
                NamespaceBindings nsContext = null;
                if ( !namespacesList.isEmpty() ) {
                    nsContext = namespacesList.get( i );
                }
                sortByList.add( getSortBy( param, nsContext ) );
            }
        }

        List<Query> queries = new ArrayList<Query>( numQueries );
        if ( !resourceIdList.isEmpty() ) {
            if ( bbox != null ) {
                String msg = "Parameters RESOURCEID and BBOX are mututally exclusive.";
                throw new OWSException( msg, INVALID_PARAMETER_VALUE );
            }
            if ( !filterList.isEmpty() ) {
                String msg = "Parameters RESOURCEID and FILTER are mututally exclusive.";
                throw new OWSException( msg, INVALID_PARAMETER_VALUE );
            }
        } else if ( !typeNamesList.isEmpty() ) {
            if ( bbox != null ) {
                for ( int i = 0; i < numQueries; i++ ) {
                    TypeName[] typeNames = typeNamesList.get( i );
                    ICRS srsName = srsNames.isEmpty() ? null : srsNames.get( i );
                    // TODO
                    ProjectionClause[][] projectionClauses = null;
                    SortProperty[] sortBy = sortByList.isEmpty() ? null : sortByList.get( i );
                    queries.add( new BBoxQuery( null, typeNames, null, srsName, projectionClauses, sortBy, bbox ) );
                }
            } else {
                for ( int i = 0; i < numQueries; i++ ) {
                    TypeName[] typeNames = typeNamesList.get( i );
                    ICRS srsName = srsNames.isEmpty() ? null : srsNames.get( i );
                    ProjectionClause[] projectionClauses = projectionClausesList.isEmpty() ? null
                                                                                          : projectionClausesList.get( i );
                    SortProperty[] sortBy = sortByList.isEmpty() ? null : sortByList.get( i );
                    Filter filter = filterList.isEmpty() ? null : filterList.get( i );
                    queries.add( new FilterQuery( null, typeNames, null, srsName, projectionClauses, sortBy, filter ) );
                }
            }
        } else {
            String msg = "One of the parameters TYPENAMES and RESOURCEID must be present in a KVP-encoded Ad hoc query.";
            throw new OWSException( msg, MISSING_PARAMETER_VALUE );
        }
        return queries;
    }

    private static QName resolveQName( String prefixedName, NamespaceBindings nsBindings ) {
        QName qName = null;
        String[] typeParts = prefixedName.split( ":" );
        if ( typeParts.length == 2 ) {
            String nsUri = nsBindings == null ? null : nsBindings.getNamespaceURI( typeParts[0] );
            qName = new QName( nsUri, typeParts[1], typeParts[0] );
        } else {
            qName = new QName( typeParts[0] );
        }
        return qName;
    }

    protected static Filter parseFilter200( String filter )
                            throws XMLStreamException, FactoryConfigurationError {

        String bindingPreamble = "<nsbindings xmlns=\"" + FES_20_NS + "\" xmlns:fes=\"" + FES_20_NS + "\">";
        String bindingEpilog = "</nsbindings>";
        StringReader sr = new StringReader( bindingPreamble + filter + bindingEpilog );
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( sr );
        skipStartDocument( xmlStream );
        nextElement( xmlStream );
        return Filter200XMLDecoder.parse( xmlStream );
    }

    private static List<Query> parseStoredQuery200( Map<String, String> kvpUC ) {
        // mandatory: STOREDQUERY_ID
        String id = KVPUtils.getRequired( kvpUC, "STOREDQUERY_ID" );

        Map<String, OMElement> paramNameToValue = new HashMap<String, OMElement>();
        for ( String key : kvpUC.keySet() ) {
            String literalValue = kvpUC.get( key );
            // TODO
            String xml = "<fes:Literal xmlns:fes=\"" + FES_20_NS + "\"><![CDATA[";
            xml += literalValue;
            xml += "]]></fes:Literal>";
            OMElement literalEl;
            try {
                literalEl = AXIOMUtil.stringToOM( xml );
                paramNameToValue.put( key, literalEl );
            } catch ( XMLStreamException e ) {
                e.printStackTrace();
            }
        }
        List<Query> queries = new ArrayList<Query>();
        queries.add( new StoredQuery( null, id, paramNameToValue ) );
        return queries;
    }

    protected static String[] getFilters( String filterStr ) {
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
    protected static Envelope createEnvelope( String bboxStr, ICRS srs ) {
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

    protected static ProjectionClause[][] getXLinkPropNames( ProjectionClause[][] propertyNames, String[][] ptxDepthAr,
                                                             Integer[][] ptxExpAr, String traverseXlinkDepth,
                                                             Integer traverseXlinkExpiry ) {
        ProjectionClause[][] result = null;
        if ( propertyNames != null ) {
            result = new ProjectionClause[propertyNames.length][];
            for ( int i = 0; i < propertyNames.length; i++ ) {
                result[i] = new ProjectionClause[propertyNames[i].length];
                for ( int j = 0; j < propertyNames[i].length; j++ ) {
                    if ( ptxDepthAr != null || ptxExpAr != null ) {
                        String resolveDepth = ptxDepthAr[i][j];
                        BigInteger resolveTimeout = ptxExpAr[i][j] == null ? null
                                                                          : BigInteger.valueOf( ptxExpAr[i][j] * 60 );
                        ResolveParams resolveParams = new ResolveParams( null, resolveDepth, resolveTimeout );
                        result[i][j] = new ProjectionClause( propertyNames[i][j].getPropertyName(), resolveParams, null );
                    } else {
                        result[i][j] = propertyNames[i][j];
                    }
                }
            }
        }
        return result;
    }

    protected static TypeName[] getTypeNames( String typeStrList, Map<String, String> nsBindings ) {
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
        } else {
            result = new TypeName[0];
        }
        return result;
    }

    protected static TypeName[] getTypeNames100( String typeStrList ) {
        TypeName[] result = null;
        if ( typeStrList != null ) {

            String[] typeList = typeStrList.split( "," );
            result = new TypeName[typeList.length];

            for ( int i = 0; i < typeList.length; i++ ) {
                String alias = null;
                String theRest = typeList[i];
                if ( typeList[i].contains( "=" ) ) {
                    alias = typeList[i].split( "=" )[0];
                    theRest = typeList[i].split( "=" )[1];
                }

                String prefix = null;
                String local = theRest;
                if ( theRest.contains( ":" ) ) {
                    prefix = theRest.split( ":" )[0];
                    local = theRest.split( ":" )[1];
                }

                QName qName = prefix == null ? new QName( local ) : new QName( XMLConstants.NULL_NS_URI, local, prefix );
                result[i] = new TypeName( qName, alias );
            }
        }
        return result;
    }

    protected static SortProperty[] getSortBy( String sortbyStr, NamespaceBindings nsContext ) {
        SortProperty[] result = null;
        if ( sortbyStr != null ) {
            String[] sortbyComm = sortbyStr.split( "," );
            result = new SortProperty[sortbyComm.length];
            for ( int i = 0; i < sortbyComm.length; i++ ) {
                if ( sortbyComm[i].endsWith( " D" ) || sortbyComm[i].endsWith( " DESC" ) ) {
                    String sortbyProp = sortbyComm[i].substring( 0, sortbyComm[i].indexOf( " " ) );
                    result[i] = new SortProperty( new ValueReference( sortbyProp, nsContext ), false );
                } else {
                    if ( sortbyComm[i].endsWith( " A" ) || sortbyComm[i].endsWith( " ASC" ) ) {
                        String sortbyProp = sortbyComm[i].substring( 0, sortbyComm[i].indexOf( " " ) );
                        result[i] = new SortProperty( new ValueReference( sortbyProp, nsContext ), true );
                    } else {
                        result[i] = new SortProperty( new ValueReference( sortbyComm[i], nsContext ), true );
                    }
                }
            }
        }
        return result;
    }

    protected static ProjectionClause[][] getPropertyNames( String propertyStr, NamespaceBindings nsContext ) {
        ProjectionClause[][] result = null;
        if ( propertyStr != null ) {
            String[][] propComm = parseParamList( propertyStr );

            result = new ProjectionClause[propComm.length][];
            for ( int i = 0; i < propComm.length; i++ ) {
                result[i] = new ProjectionClause[propComm[i].length];

                for ( int j = 0; j < propComm[i].length; j++ ) {
                    result[i][j] = new ProjectionClause( new ValueReference( propComm[i][j], nsContext ), null, null );
                }
            }
        }
        return result;
    }

    @SuppressWarnings("boxing")
    protected static Integer[][] parseParamListAsInts( String paramList ) {
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

    protected static String[][] parseParamList( String paramList ) {
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
