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
package org.deegree.services.wfs.query;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.commons.ows.exception.OWSException.MISSING_PARAMETER_VALUE;
import static org.deegree.services.wfs.query.StoredQueryHandler.GET_FEATURE_BY_ID;
import static org.deegree.services.wfs.query.StoredQueryHandler.GET_FEATURE_BY_TYPE;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.IOUtils;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.QNameUtils;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.cs.CRSUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.Filter;
import org.deegree.filter.Filters;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.filter.projection.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.BBOX;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.query.AdHocQuery;
import org.deegree.protocol.wfs.query.BBoxQuery;
import org.deegree.protocol.wfs.query.FeatureIdQuery;
import org.deegree.protocol.wfs.query.FilterQuery;
import org.deegree.protocol.wfs.query.StoredQuery;
import org.deegree.protocol.wfs.query.xml.QueryXMLAdapter;
import org.deegree.protocol.wfs.storedquery.QueryExpressionText;
import org.deegree.protocol.wfs.storedquery.StoredQueryDefinition;
import org.deegree.protocol.wfs.storedquery.xml.StoredQueryDefinitionXMLAdapter;
import org.deegree.services.wfs.WebFeatureService;
import org.deegree.services.wfs.WfsFeatureStoreManager;
import org.jaxen.NamespaceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for validating a sequence of queries (e.g from {@link GetFeature} requests) and generating a
 * corresponding sequence of feature store queries.
 * <p>
 * Also performs some normalizing on the values of {@link ValueReference}s. TODO describe strategy
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class QueryAnalyzer {

    private final GeometryFactory geomFac = new GeometryFactory();

    private static final Logger LOG = LoggerFactory.getLogger( QueryAnalyzer.class );

    private final WebFeatureService controller;

    private final WfsFeatureStoreManager service;

    private final Set<FeatureType> requestedFts = new HashSet<FeatureType>();

    private final Map<Query, org.deegree.protocol.wfs.query.Query> queryToWFSQuery = new HashMap<Query, org.deegree.protocol.wfs.query.Query>();

    private final Map<FeatureStore, List<Query>> fsToQueries = new LinkedHashMap<FeatureStore, List<Query>>();

    private final Map<QName, List<ProjectionClause>> projections = new HashMap<>();

    private ICRS requestedCrs;

    private boolean allFtsPossible;

    private final boolean checkAreaOfUse;

    private final int count;

    private final int startIndex;

    /**
     * Creates a new {@link QueryAnalyzer}.
     *
     * @param wfsQueries
     *            queries be performed, must not be <code>null</code>
     * @param controller
     *            {@link WebFeatureService} to be used, must not be <code>null</code>
     * @param service
     *            {@link WfsFeatureStoreManager} to be used, must not be <code>null</code>
     * @param checkInputDomain
     *            true, if geometries in query constraints should be checked against validity domain of the SRS (needed
     *            for CITE 1.1.0 compliance)
     * @throws OWSException
     *             if the request cannot be performed, e.g. because it queries feature types that are not served
     */
    public QueryAnalyzer( List<org.deegree.protocol.wfs.query.Query> wfsQueries, WebFeatureService controller,
                          WfsFeatureStoreManager service, boolean checkInputDomain ) throws OWSException {
        this(wfsQueries, controller, service, checkInputDomain, -1, 0);
    }

    /**
     * Creates a new {@link QueryAnalyzer}.
     *
     * @param wfsQueries
     *            queries be performed, must not be <code>null</code>
     * @param controller
     *            {@link WebFeatureService} to be used, must not be <code>null</code>
     * @param service
     *            {@link WfsFeatureStoreManager} to be used, must not be <code>null</code>
     * @param checkInputDomain
     *            true, if geometries in query constraints should be checked against validity domain of the SRS (needed
     *            for CITE 1.1.0 compliance)
     * @param count
     *            number of features to return, if not specified: -1
     * @param startIndex
     *            index of the first feature to return, default: 0
     * @throws OWSException
     *             if the request cannot be performed, e.g. because it queries feature types that are not served
     */
    public QueryAnalyzer( List<org.deegree.protocol.wfs.query.Query> wfsQueries, WebFeatureService controller,
                          WfsFeatureStoreManager service, boolean checkInputDomain, int count, int startIndex ) throws OWSException {

        this.controller = controller;
        this.service = service;
        this.checkAreaOfUse = checkInputDomain;
        this.count = count;
        this.startIndex = startIndex;

        // generate validated feature store queries
        if ( wfsQueries.isEmpty() ) {
            // TODO perform the check here?
            String msg = "Either the typeName parameter must be present or the query must provide feature ids.";
            throw new OWSException( msg, INVALID_PARAMETER_VALUE, "typeName" );
        }

        List<Pair<AdHocQuery, org.deegree.protocol.wfs.query.Query>> adHocQueries = convertStoredQueries( wfsQueries );

        Query[] queries = new Query[adHocQueries.size()];
        for ( int i = 0; i < adHocQueries.size(); i++ ) {
            AdHocQuery wfsQuery = adHocQueries.get( i ).first;
            Query query = validateQuery( wfsQuery );
            query.setHandleStrict( controller.isStrict() );
            queries[i] = query;

            // yes, use the original WFS query (not necessarily adHoc)
            queryToWFSQuery.put( query, adHocQueries.get( i ).second );

            // TODO what about queries with different SRS?
            if ( wfsQuery.getSrsName() != null ) {
                requestedCrs = wfsQuery.getSrsName();
            } else {
                requestedCrs = controller.getDefaultQueryCrs();
            }

            // TODO cope with more queries than one
            if ( wfsQuery.getProjectionClauses() != null ) {
                for ( TypeName typeName : wfsQuery.getTypeNames() ) {
                    QName bestMatch = findBestMatchingFeatureTypeName( typeName );
                    this.projections.put( bestMatch, Arrays.asList( wfsQuery.getProjectionClauses() ) );
                }
            }
        }

        // associate queries with feature stores
        for ( Query query : queries ) {
            if ( query.getTypeNames().length == 0 ) {
                for ( FeatureStore fs : service.getStores() ) {
                    List<Query> fsQueries = fsToQueries.get( fs );
                    if ( fsQueries == null ) {
                        fsQueries = new ArrayList<Query>();
                        fsToQueries.put( fs, fsQueries );
                    }
                    fsQueries.add( query );
                }
            } else {
                FeatureStore fs = service.getStore( query.getTypeNames()[0].getFeatureTypeName() );
                List<Query> fsQueries = fsToQueries.get( fs );
                if ( fsQueries == null ) {
                    fsQueries = new ArrayList<Query>();
                    fsToQueries.put( fs, fsQueries );
                }
                fsQueries.add( query );
            }
        }
    }

    private QName findBestMatchingFeatureTypeName( TypeName typeName ) {
        QName[] featureTypeNames = service.getFeatureTypeNames();
        if ( featureTypeNames == null )
            return typeName.getFeatureTypeName();
        List<QName> allFeatureTypeNames = Arrays.asList( featureTypeNames );
        QName bestMatch = QNameUtils.findBestMatch( typeName.getFeatureTypeName(), allFeatureTypeNames );

        if ( bestMatch != null )
            return bestMatch;
        return typeName.getFeatureTypeName();
    }

    private List<Pair<AdHocQuery, org.deegree.protocol.wfs.query.Query>> convertTemplateStoredQuery( StoredQuery query )
                            throws OWSException {
        List<Pair<AdHocQuery, org.deegree.protocol.wfs.query.Query>> list = new ArrayList<Pair<AdHocQuery, org.deegree.protocol.wfs.query.Query>>();
        StoredQueryHandler handler = controller.getStoredQueryHandler();
        URL u = handler.getStoredQueryTemplate( query.getId() );
        try {
            String templ = IOUtils.toString( u.openStream() );
            for ( Entry<String, OMElement> e : query.getParams().entrySet() ) {
                String val = e.getValue().getText();
                Pattern p = Pattern.compile( "[$][{]" + e.getKey() + "[}]", Pattern.CASE_INSENSITIVE );
                templ = p.matcher( templ ).replaceAll( val );
            }

            LOG.debug( "Stored query template after replacement: {}", templ );

            StoredQueryDefinitionXMLAdapter parser = new StoredQueryDefinitionXMLAdapter();
            parser.load( new StringReader( templ ), "http://www.deegree.org/none" );
            StoredQueryDefinition def = parser.parse();
            for ( QueryExpressionText text : def.getQueryExpressionTextEls() ) {
                for ( OMElement elem : text.getChildEls() ) {
                    org.deegree.protocol.wfs.query.Query q = new QueryXMLAdapter().parseAbstractQuery200( elem );
                    if ( q instanceof AdHocQuery ) {
                        list.add( new Pair<AdHocQuery, org.deegree.protocol.wfs.query.Query>( (AdHocQuery) q, query ) );
                    }
                }
            }
            return list;
        } catch ( IOException e ) {
            String msg = "An error occurred when trying to convert stored query with id '" + query.getId() + "': '"
                         + e.getLocalizedMessage() + "'.";
            throw new OWSException( msg, INVALID_PARAMETER_VALUE, "storedQueryId" );
        }
    }

    private List<Pair<AdHocQuery, org.deegree.protocol.wfs.query.Query>> convertStoredQueries( List<org.deegree.protocol.wfs.query.Query> wfsQueries )
                            throws OWSException {
        List<Pair<AdHocQuery, org.deegree.protocol.wfs.query.Query>> adHocQueries = new ArrayList<Pair<AdHocQuery, org.deegree.protocol.wfs.query.Query>>();
        for ( org.deegree.protocol.wfs.query.Query wfsQuery : wfsQueries ) {
            if ( wfsQuery instanceof AdHocQuery ) {
                adHocQueries.add( new Pair<AdHocQuery, org.deegree.protocol.wfs.query.Query>( (AdHocQuery) wfsQuery,
                                                                                              wfsQuery ) );
            } else {
                StoredQuery storedQuery = (StoredQuery) wfsQuery;
                if ( storedQuery.getId().equals( GET_FEATURE_BY_ID ) ) {
                    OMElement literalEl = storedQuery.getParams().get( "ID" );
                    if ( literalEl == null ) {
                        String msg = "Stored query '" + storedQuery.getId() + "' requires parameter 'ID'.";
                        throw new OWSException( msg, MISSING_PARAMETER_VALUE, "ID" );
                    }
                    LOG.debug( "GetFeatureById query" );
                    String requestedId = literalEl.getText();
                    FeatureIdQuery q = new FeatureIdQuery( null, null, null, null, null, null,
                                                           new String[] { requestedId } );
                    adHocQueries.add( new Pair<AdHocQuery, org.deegree.protocol.wfs.query.Query>( q, wfsQuery ) );
                } else if ( storedQuery.getId().equals( GET_FEATURE_BY_TYPE ) ) {
                    // TODO qualify typeName using NAMESPACES parameter for KVP requests
                    OMElement literalEl = storedQuery.getParams().get( "TYPENAME" );
                    if ( literalEl == null ) {
                        String msg = "Stored query '" + storedQuery.getId() + "' requires parameter 'TYPENAME'.";
                        throw new OWSException( msg, MISSING_PARAMETER_VALUE, "TYPENAME" );
                    }
                    String tn = literalEl.getText();
                    if ( tn.contains( ":" ) ) {
                        tn = tn.split( ":" )[1];
                    }
                    LOG.debug( "GetFeatureByType query" );
                    FilterQuery q = new FilterQuery( new QName( tn ), null, null, null );
                    adHocQueries.add( new Pair<AdHocQuery, org.deegree.protocol.wfs.query.Query>( q, wfsQuery ) );
                } else if ( controller.getStoredQueryHandler().hasStoredQuery( storedQuery.getId() ) ) {
                    List<Pair<AdHocQuery, org.deegree.protocol.wfs.query.Query>> qs = convertTemplateStoredQuery( storedQuery );
                    adHocQueries.addAll( qs );
                } else {
                    String msg = "Stored query with id '" + storedQuery.getId() + "' is not known.";
                    throw new OWSException( msg, INVALID_PARAMETER_VALUE, "storedQueryId" );
                }
            }
        }
        return adHocQueries;
    }

    /**
     * Returns all {@link FeatureType}s that may be returned in the response to the request.
     * 
     * @return list of requested feature types, or <code>null</code> if any of the feature types served by the WFS could
     *         be returned (happens only for KVP-request with feature ids and without typenames)
     */
    public Collection<FeatureType> getFeatureTypes() {
        return allFtsPossible ? null : requestedFts;
    }

    /**
     * Returns the feature store queries that have to performed for this request.
     * 
     * @return the feature store queries that have to performed, never <code>null</code>
     */
    public Map<FeatureStore, List<Query>> getQueries() {
        return fsToQueries;
    }

    /**
     * Returns the original <code>GetFeature</code> query that the given query was derived from.
     * 
     * @param query
     * @return
     */
    public org.deegree.protocol.wfs.query.Query getQuery( Query query ) {
        return queryToWFSQuery.get( query );
    }

    /**
     * Returns the crs that the returned geometries should have.
     * 
     * TODO what about multiple queries with different CRS
     * 
     * @return the crs, or <code>null</code> (use native crs)
     */
    public ICRS getRequestedCRS() {
        return requestedCrs;
    }

    /**
     * Returns the specific XLink-behaviour for features properties.
     * 
     * TODO what about multiple queries that specify different sets of properties
     * 
     * @return specific XLink-behaviour or <code>null</code> (no specific behaviour)
     */
    public Map<QName, List<ProjectionClause>> getProjections() {
        return projections;
    }

    /**
     * Builds a feature store {@link Query} from the given WFS query and checks if the feature type / property name
     * references in the given {@link Query} are resolvable against the served application schema.
     * <p>
     * Incorrectly or un-qualified feature type or property names are repaired. These often stem from WFS 1.0.0
     * KVP-requests (which doesn't have a namespace parameter) or broken clients.
     * </p>
     * 
     * @param wfsQuery
     *            query to be validated, must not be <code>null</code>
     * @return the feature store query, using only correctly fully qualified feature / property names
     * @throws OWSException
     *             if an unresolvable feature type / property name is used
     */
    private Query validateQuery( org.deegree.protocol.wfs.query.Query wfsQuery )
                            throws OWSException {

        // requalify query typenames and keep track of them
        TypeName[] wfsTypeNames = ( (AdHocQuery) wfsQuery ).getTypeNames();
        TypeName[] typeNames = new TypeName[wfsTypeNames.length];
        FeatureStore commonFs = null;
        for ( int i = 0; i < wfsTypeNames.length; i++ ) {
            String alias = wfsTypeNames[i].getAlias();
            FeatureType ft = service.lookupFeatureType( wfsTypeNames[i].getFeatureTypeName() );
            if ( ft == null ) {
                String msg = "Feature type with name '" + wfsTypeNames[i].getFeatureTypeName()
                             + "' is not served by this WFS.";
                throw new OWSException( msg, INVALID_PARAMETER_VALUE, "typeName" );
            }
            FeatureStore fs = service.getStore( ft.getName() );
            if ( commonFs != null ) {
                if ( fs != commonFs ) {
                    String msg = "Requested join of feature types from different feature stores. This is not supported.";
                    throw new OWSException( msg, INVALID_PARAMETER_VALUE, "typeName" );
                }
            } else {
                commonFs = fs;
            }
            requestedFts.add( ft );
            QName ftName = ft.getName();
            typeNames[i] = new TypeName( ftName, alias );
        }
        if ( wfsTypeNames.length == 0 ) {
            allFtsPossible = true;
        }

        // check requested / filter property names and geometries
        Filter filter = null;
        if ( wfsQuery instanceof FilterQuery ) {
            FilterQuery fQuery = ( (FilterQuery) wfsQuery );
            if ( fQuery.getProjectionClauses() != null ) {
                for ( ProjectionClause projection : fQuery.getProjectionClauses() ) {
                    if ( projection instanceof PropertyName ) {
                        validatePropertyName( ( (PropertyName) projection ).getPropertyName(), typeNames );
                    }
                }
            }
            if ( fQuery.getFilter() != null ) {
                for ( ValueReference pt : Filters.getPropertyNames( fQuery.getFilter() ) ) {
                    validatePropertyName( pt, typeNames );
                }
                if ( checkAreaOfUse ) {
                    for ( Geometry geom : Filters.getGeometries( fQuery.getFilter() ) ) {
                        validateGeometryConstraint( geom, ( (AdHocQuery) wfsQuery ).getSrsName() );
                    }
                }
            }
            filter = fQuery.getFilter();
        } else if ( wfsQuery instanceof BBoxQuery ) {
            BBoxQuery bboxQuery = (BBoxQuery) wfsQuery;
            ProjectionClause[] propNames = bboxQuery.getProjectionClauses();
            if ( propNames != null ) {
                for ( ProjectionClause propertyName : propNames ) {
                    if ( propertyName instanceof PropertyName ) {
                        validatePropertyName( ( (PropertyName) propertyName ).getPropertyName(), typeNames );
                    }
                }
            }
            if ( checkAreaOfUse ) {
                validateGeometryConstraint( ( (BBoxQuery) wfsQuery ).getBBox(), ( (AdHocQuery) wfsQuery ).getSrsName() );
            }

            Envelope bbox = bboxQuery.getBBox();
            BBOX bboxOperator = new BBOX( bbox );
            filter = new OperatorFilter( bboxOperator );
        } else if ( wfsQuery instanceof FeatureIdQuery ) {
            FeatureIdQuery fidQuery = (FeatureIdQuery) wfsQuery;
            ProjectionClause[] propNames = fidQuery.getProjectionClauses();
            if ( propNames != null ) {
                for ( ProjectionClause propertyName : propNames ) {
                    if ( propertyName instanceof PropertyName ) {
                        validatePropertyName( ( (PropertyName) propertyName ).getPropertyName(), typeNames );
                    }
                }
            }
            filter = new IdFilter( fidQuery.getFeatureIds() );
        }

        if ( wfsTypeNames.length == 0 && ( filter == null || !( filter instanceof IdFilter ) ) ) {
            String msg = "Either the typeName parameter must be present or the query must provide feature ids.";
            throw new OWSException( msg, INVALID_PARAMETER_VALUE, "typeName" );
        }

        SortProperty[] sortProps = ( (AdHocQuery) wfsQuery ).getSortBy();
        if ( sortProps != null ) {
            for ( SortProperty sortProperty : sortProps ) {
                validatePropertyName( sortProperty.getSortProperty(), typeNames );
            }
        }

        // superimpose default query CRS
        if ( filter != null ) {
            Filters.setDefaultCRS( filter, controller.getDefaultQueryCrs() );
        }

        return new Query( typeNames, filter, sortProps, count, startIndex );
    }

    private void validatePropertyName( ValueReference propName, TypeName[] typeNames )
                            throws OWSException {

        // no check possible if feature type is unknown
        if ( typeNames.length > 0 ) {
            if ( propName.getAsQName() != null ) {
                if ( !isPrefixedAndBound( propName ) ) {
                    repairSimpleUnqualified( propName, typeNames[0] );
                }

                // check that the propName is indeed valid as belonging to serviced features
                QName name = getPropertyNameAsQName( propName );
                if ( name != null ) {
                    if ( typeNames.length == 1 ) {
                        FeatureType ft = service.lookupFeatureType( typeNames[0].getFeatureTypeName() );
                        if ( ft.getPropertyDeclaration( name ) == null ) {
                            // gml:boundedBy currently requires special treatment
                            if ( !name.getLocalPart().equals( "boundedBy" ) ) {
                                String msg = "Specified PropertyName '" + propName.getAsText() + "' (='" + name
                                             + "') does not exist for feature type '" + ft.getName() + "'.";
                                throw new OWSException( msg, INVALID_PARAMETER_VALUE, "PropertyName" );
                            }
                        }
                    }
                    // TODO really skip this check for join queries?
                }
            } else {
                // TODO property name may be an XPath and use aliases...
            }
        }
    }

    /**
     * Returns whether the propName has to be considered for re-qualification.
     * 
     * @param propName
     * @return
     */
    private boolean isPrefixedAndBound( ValueReference propName ) {
        QName name = propName.getAsQName();
        return !name.getPrefix().equals( DEFAULT_NS_PREFIX )
               && !name.getNamespaceURI().equals( "" );
    }

    /**
     * Repairs a {@link ValueReference} that contains the local name of a {@link FeatureType}'s property or a prefixed
     * name, but without a correct namespace binding.
     * <p>
     * This types of propertynames especially occurs in WFS 1.0.0 requests.
     * </p>
     * 
     * @param propName
     *            property name to be repaired, must be "simple", i.e. contain only of a QName
     * @param typeName
     *            feature type specification from the query, must not be <code>null</code>
     * @throws OWSException
     *             if no match could be found
     */
    private void repairSimpleUnqualified( ValueReference propName, TypeName typeName )
                            throws OWSException {

        FeatureType ft = service.lookupFeatureType( typeName.getFeatureTypeName() );

        List<QName> propNames = new ArrayList<QName>();
        // TODO which GML version
        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            propNames.add( pt.getName() );
        }

        QName match = QNameUtils.findBestMatch( propName.getAsQName(), propNames );
        if ( match == null ) {
            String msg = "Specified PropertyName '" + propName.getAsText() + "' does not exist for feature type '"
                         + ft.getName() + "'.";
            throw new OWSException( msg, INVALID_PARAMETER_VALUE, "PropertyName" );
        }
        if ( !match.equals( propName.getAsQName() ) ) {
            LOG.debug( "Repairing unqualified PropertyName: " + QNameUtils.toString( propName.getAsQName() ) + " -> "
                       + QNameUtils.toString( match ) );
            // vague match
            String text = match.getLocalPart();
            if ( !match.getPrefix().equals( DEFAULT_NS_PREFIX ) ) {
                text = match.getPrefix() + ":" + match.getLocalPart();
            }
            NamespaceBindings nsContext = new NamespaceBindings();
            nsContext.addNamespace( match.getPrefix(), match.getNamespaceURI() );
            propName.set( text, nsContext );
        }
    }

    // TODO do this properly
    private QName getPropertyNameAsQName( ValueReference propName ) {
        QName name = null;
        NamespaceContext nsContext = propName.getNsContext();
        String s = propName.getAsText();
        int colonIdx = s.indexOf( ':' );
        if ( !s.contains( "/" ) && colonIdx != -1 ) {
            if ( Character.isLetterOrDigit( s.charAt( 0 ) ) && Character.isLetterOrDigit( s.charAt( s.length() - 1 ) ) ) {
                String prefix = s.substring( 0, colonIdx );
                String localName = s.substring( colonIdx + 1, s.length() );
                String nsUri = null;

                if ( nsContext != null ) {
                    nsUri = nsContext.translateNamespacePrefixToUri( prefix );
                } else {

                    nsUri = service.getPrefixToNs().get( prefix );
                    if ( nsUri == null ) {
                        nsUri = "";
                    }
                }
                name = new QName( nsUri, localName, prefix );
            }
        } else {
            if ( !s.contains( "/" ) && !s.isEmpty() && Character.isLetterOrDigit( s.charAt( 0 ) )
                 && Character.isLetterOrDigit( s.charAt( s.length() - 1 ) ) ) {
                name = new QName( s );
            }
        }
        return name;
    }

    private void validateGeometryConstraint( Geometry geom, ICRS queriedCrs )
                            throws OWSException {

        // check if geometry's bbox is inside the domain of its CRS
        Envelope bbox = geom.getEnvelope();
        if ( bbox.getCoordinateSystem() != null ) {
            // check if geometry's bbox is valid with respect to the CRS domain
            try {
                double[] b = bbox.getCoordinateSystem().getAreaOfUseBBox();
                Envelope domainOfValidity = geomFac.createEnvelope( b[0], b[1], b[2], b[3], CRSUtils.EPSG_4326 );
                domainOfValidity = transform( domainOfValidity, bbox.getCoordinateSystem() );
                if ( !bbox.isWithin( domainOfValidity ) ) {
                    String msg = "Invalid geometry constraint in filter. The envelope of the geometry is not within the domain of validity ('"
                                 + domainOfValidity
                                 + "') of its CRS ('"
                                 + bbox.getCoordinateSystem().getAlias()
                                 + "').";
                    throw new OWSException( msg, INVALID_PARAMETER_VALUE, "filter" );
                }
            } catch ( UnknownCRSException e ) {
                // could not validate constraint, but let's assume it's met
            } catch ( IllegalArgumentException e ) {
                // could not validate constraint, but let's assume it's met
            } catch ( TransformationException e ) {
                // could not validate constraint, but let's assume it's met
            }
        }

        // check if geometry's bbox is inside the validity domain of the queried CRS
        if ( queriedCrs != null ) {
            try {
                double[] b = queriedCrs.getAreaOfUseBBox();
                Envelope domainOfValidity = geomFac.createEnvelope( b[0], b[1], b[2], b[3], CRSUtils.EPSG_4326 );
                domainOfValidity = transform( domainOfValidity, queriedCrs );
                Envelope bboxTransformed = transform( bbox, queriedCrs );
                if ( !bboxTransformed.isWithin( domainOfValidity ) ) {
                    String msg = "Invalid geometry constraint in filter. The envelope of the geometry is not within the domain of validity ('"
                                 + domainOfValidity + "') of the queried CRS ('" + queriedCrs.getAlias() + "').";
                    throw new OWSException( msg, INVALID_PARAMETER_VALUE, "filter" );
                }
            } catch ( UnknownCRSException e ) {
                // could not validate constraint, but let's assume it's met
            } catch ( IllegalArgumentException e ) {
                // could not validate constraint, but let's assume it's met
            } catch ( TransformationException e ) {
                // could not validate constraint, but let's assume it's met
            }
        }
    }

    private Envelope transform( Envelope bbox, ICRS targetCrs )
                            throws IllegalArgumentException, TransformationException, UnknownCRSException {
        if ( targetCrs.equals( bbox.getEnvelope().getCoordinateSystem() ) ) {
            return bbox;
        }
        GeometryTransformer transformer = new GeometryTransformer( targetCrs );
        return transformer.transform( bbox );
    }
}
