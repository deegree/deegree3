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
package org.deegree.services.wfs;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static org.deegree.gml.GMLVersion.GML_31;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.deegree.commons.utils.QNameUtils;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.filter.Filter;
import org.deegree.filter.Filters;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.spatial.BBOX;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.gml.GMLVersion;
import org.deegree.protocol.wfs.getfeature.BBoxQuery;
import org.deegree.protocol.wfs.getfeature.FeatureIdQuery;
import org.deegree.protocol.wfs.getfeature.FilterQuery;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.getfeature.XLinkPropertyName;
import org.deegree.services.controller.ows.OWSException;
import org.jaxen.NamespaceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for validating the queries contained in {@link GetFeature} requests and generating a corresponding
 * sequence of feature store queries.
 * <p>
 * Also performs some normalizing on the values of {@link PropertyName}s. TODO describe strategy
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetFeatureAnalyzer {

    private final GeometryFactory geomFac = new GeometryFactory();

    private static final Logger LOG = LoggerFactory.getLogger( GetFeatureAnalyzer.class );

    private final WFSController controller;

    private final WFService service;

    private final GMLVersion outputFormat;

    private final Set<FeatureType> requestedFts = new HashSet<FeatureType>();

    private final Map<Query, org.deegree.protocol.wfs.getfeature.Query> queryToWFSQuery = new HashMap<Query, org.deegree.protocol.wfs.getfeature.Query>();

    private final Map<FeatureStore, List<Query>> fsToQueries = new LinkedHashMap<FeatureStore, List<Query>>();

    private PropertyName[] requestedProps = null;

    private XLinkPropertyName[] xlinkProps = null;

    private CRS requestedCrs;

    private boolean allFtsPossible;

    private final boolean checkAreaOfUse;

    /**
     * Creates a new {@link GetFeatureAnalyzer}.
     * 
     * @param request
     *            get feature request to be performed, must not be <code>null</code>
     * @param service
     *            {@link WFService} to be used, must not be <code>null</code>
     * @param outputFormat
     *            output format, must not be <code>null</code>
     * @param checkInputDomain
     *            true, if geometries in query constraints should be checked against validity domain of the SRS (needed
     *            for CITE 1.1.0 compliance)
     * @throws OWSException
     *             if the request cannot be performed, e.g. because it queries feature types that are not served
     */
    public GetFeatureAnalyzer( GetFeature request, WFSController controller, WFService service,
                               GMLVersion outputFormat, boolean checkInputDomain ) throws OWSException {

        this.controller = controller;
        this.service = service;
        this.outputFormat = outputFormat;
        this.checkAreaOfUse = checkInputDomain;

        // generate validated feature store queries
        org.deegree.protocol.wfs.getfeature.Query[] wfsQueries = request.getQueries();
        Query[] queries = new Query[wfsQueries.length];
        if ( queries.length == 0 ) {
            // TODO perform the check here?
            String msg = "Either the typeName parameter must be present or the query must provide feature ids.";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "typeName" );
        }
        for ( int i = 0; i < wfsQueries.length; i++ ) {
            org.deegree.protocol.wfs.getfeature.Query wfsQuery = wfsQueries[i];
            Query query = validateQuery( wfsQuery );
            queries[i] = query;
            queryToWFSQuery.put( query, wfsQuery );

            // TODO what about queries with different SRS?
            if ( wfsQuery.getSrsName() != null ) {
                requestedCrs = wfsQuery.getSrsName();
            } else {
                requestedCrs = controller.getDefaultQueryCrs();
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

        // TODO cope with more queries than one
        if ( wfsQueries.length == 1 ) {
            if ( wfsQueries[0] instanceof FilterQuery ) {
                FilterQuery featureQuery = ( (FilterQuery) wfsQueries[0] );
                if ( featureQuery.getPropertyNames() != null ) {
                    this.requestedProps = featureQuery.getPropertyNames();
                }
                if ( featureQuery.getXLinkPropertyNames() != null && featureQuery.getXLinkPropertyNames().length > 0 ) {
                    this.xlinkProps = featureQuery.getXLinkPropertyNames();
                }
            } else if ( wfsQueries[0] instanceof BBoxQuery ) {
                BBoxQuery bboxQuery = ( (BBoxQuery) wfsQueries[0] );
                if ( bboxQuery.getPropertyNames() != null && bboxQuery.getPropertyNames().length > 0 ) {
                    // TODO cope with arrays with more than one entry
                    this.requestedProps = bboxQuery.getPropertyNames()[0];
                }
                if ( bboxQuery.getXLinkPropertyNames() != null && bboxQuery.getXLinkPropertyNames().length > 0 ) {
                    // TODO cope with arrays with more than one entry
                    this.xlinkProps = bboxQuery.getXLinkPropertyNames()[0];
                }
            } else if ( wfsQueries[0] instanceof FeatureIdQuery ) {
                FeatureIdQuery idQuery = ( (FeatureIdQuery) wfsQueries[0] );
                if ( idQuery.getPropertyNames() != null && idQuery.getPropertyNames().length > 0 ) {
                    // TODO cope with arrays with more than one entry
                    this.requestedProps = idQuery.getPropertyNames()[0];
                }
                if ( idQuery.getXLinkPropertyNames() != null && idQuery.getXLinkPropertyNames().length > 0 ) {
                    // TODO cope with arrays with more than one entry
                    this.xlinkProps = idQuery.getXLinkPropertyNames()[0];
                }
            }
        }
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
     * Returns the crs that the returned geometries should have.
     * 
     * TODO what about multiple queries with different CRS
     * 
     * @return the crs, or <code>null</code> (use native crs)
     */
    public CRS getRequestedCRS() {
        return requestedCrs;
    }

    /**
     * Returns the features properties to be included in the output.
     * 
     * TODO what about multiple queries that specify different sets of properties
     * 
     * @return features properties to be include or <code>null</code> (include all properties)
     */
    public PropertyName[] getRequestedProps() {
        return requestedProps;
    }

    /**
     * Returns the specific XLink-behaviour for features properties.
     * 
     * TODO what about multiple queries that specify different sets of properties
     * 
     * @return specific XLink-behaviour or <code>null</code> (no specific behaviour)
     */
    public XLinkPropertyName[] getXLinkProps() {
        return xlinkProps;
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
    private Query validateQuery( org.deegree.protocol.wfs.getfeature.Query wfsQuery )
                            throws OWSException {

        // requalify query typenames and keep track of them
        TypeName[] wfsTypeNames = wfsQuery.getTypeNames();
        TypeName[] typeNames = new TypeName[wfsTypeNames.length];
        FeatureStore commonFs = null;
        for ( int i = 0; i < wfsTypeNames.length; i++ ) {
            String alias = wfsTypeNames[i].getAlias();
            FeatureType ft = service.lookupFeatureType( wfsTypeNames[i].getFeatureTypeName() );
            if ( ft == null ) {
                String msg = "Feature type with name '" + wfsTypeNames[i].getFeatureTypeName()
                             + "' is not served by this WFS.";
                throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "typeName" );
            }
            FeatureStore fs = service.getStore( ft.getName() );
            if ( commonFs != null ) {
                if ( fs != commonFs ) {
                    String msg = "Requested join of feature types from different feature stores. This is not supported.";
                    throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "typeName" );
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
            if ( fQuery.getPropertyNames() != null ) {
                for ( PropertyName propName : fQuery.getPropertyNames() ) {
                    validatePropertyName( propName, typeNames );
                }
            }
            if ( fQuery.getXLinkPropertyNames() != null ) {
                for ( XLinkPropertyName xlinkPropName : fQuery.getXLinkPropertyNames() ) {
                    validatePropertyName( xlinkPropName.getPropertyName(), typeNames );
                }
            }
            if ( fQuery.getFilter() != null ) {
                for ( PropertyName pt : Filters.getPropertyNames( fQuery.getFilter() ) ) {
                    validatePropertyName( pt, typeNames );
                }
                if ( checkAreaOfUse ) {
                    for ( Geometry geom : Filters.getGeometries( fQuery.getFilter() ) ) {
                        validateGeometryConstraint( geom, wfsQuery.getSrsName() );
                    }
                }
            }
            filter = fQuery.getFilter();
        } else if ( wfsQuery instanceof BBoxQuery ) {
            BBoxQuery bboxQuery = (BBoxQuery) wfsQuery;
            PropertyName[][] propNames = bboxQuery.getPropertyNames();
            if ( propNames != null ) {
                for ( PropertyName[] propertyNames : propNames ) {
                    for ( PropertyName propertyName : propertyNames ) {
                        validatePropertyName( propertyName, typeNames );
                    }
                }
            }
            XLinkPropertyName[][] xlinkPropNames = bboxQuery.getXLinkPropertyNames();
            if ( xlinkPropNames != null ) {
                for ( XLinkPropertyName[] propertyNames : xlinkPropNames ) {
                    for ( XLinkPropertyName propertyName : propertyNames ) {
                        validatePropertyName( propertyName.getPropertyName(), typeNames );
                    }
                }
            }
            validateGeometryConstraint( ( (BBoxQuery) wfsQuery ).getBBox(), wfsQuery.getSrsName() );

            Envelope bbox = bboxQuery.getBBox();
            BBOX bboxOperator = new BBOX( bbox );
            filter = new OperatorFilter( bboxOperator );
        } else if ( wfsQuery instanceof FeatureIdQuery ) {
            FeatureIdQuery fidQuery = (FeatureIdQuery) wfsQuery;
            PropertyName[][] propNames = fidQuery.getPropertyNames();
            if ( propNames != null ) {
                for ( PropertyName[] propertyNames : propNames ) {
                    for ( PropertyName propertyName : propertyNames ) {
                        validatePropertyName( propertyName, typeNames );
                    }
                }
            }
            XLinkPropertyName[][] xlinkPropNames = fidQuery.getXLinkPropertyNames();
            if ( xlinkPropNames != null ) {
                for ( XLinkPropertyName[] propertyNames : xlinkPropNames ) {
                    for ( XLinkPropertyName propertyName : propertyNames ) {
                        validatePropertyName( propertyName.getPropertyName(), typeNames );
                    }
                }
            }
            filter = new IdFilter( fidQuery.getFeatureIds() );
        }

        if ( wfsTypeNames.length == 0 && ( filter == null || !( filter instanceof IdFilter ) ) ) {
            String msg = "Either the typeName parameter must be present or the query must provide feature ids.";
            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "typeName" );
        }

        SortProperty[] sortProps = wfsQuery.getSortBy();
        if ( sortProps != null ) {
            for ( SortProperty sortProperty : sortProps ) {
                validatePropertyName( sortProperty.getSortProperty(), typeNames );
            }
        }
        return new Query( typeNames, filter, wfsQuery.getFeatureVersion(), wfsQuery.getSrsName(), sortProps );
    }

    private void validatePropertyName( PropertyName propName, TypeName[] typeNames )
                            throws OWSException {

        // no check possible if feature type is unknown
        if ( typeNames.length > 0 ) {
            if ( propName.isSimple() ) {
                if ( !isPrefixedAndBound( propName ) ) {
                    repairSimpleUnqualified( propName, typeNames[0] );
                }

                // check that the propName is indeed valid as belonging to serviced features
                QName name = getPropertyNameAsQName( propName );
                if ( name != null ) {
                    if ( typeNames.length == 1 ) {
                        FeatureType ft = service.lookupFeatureType( typeNames[0].getFeatureTypeName() );
                        if ( ft.getPropertyDeclaration( name, outputFormat ) == null ) {
                            String msg = "Specified PropertyName '" + propName.getPropertyName() + "' (='" + name
                                         + "') does not exist for feature type '" + ft.getName() + "'.";
                            throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "PropertyName" );
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
    private boolean isPrefixedAndBound( PropertyName propName ) {
        QName name = propName.getAsQName();
        return !name.getPrefix().equals( DEFAULT_NS_PREFIX )
               && !name.getNamespaceURI().equals( XMLConstants.NULL_NS_URI );
    }

    /**
     * Repairs a {@link PropertyName} that contains the local name of a {@link FeatureType}'s property or a prefixed
     * name, but without a correct namespace binding.
     * <p>
     * This types of propertynames especially occurs in WFS 1.0.0 requests.
     * </p>
     * 
     * @param propName
     *            property name to be repaired, must be "simple", i.e. contain only of a QName
     * @param typeName
     *            feature type specification from the query, must not be <code>null</code>
     */
    private void repairSimpleUnqualified( PropertyName propName, TypeName typeName ) {

        FeatureType ft = service.lookupFeatureType( typeName.getFeatureTypeName() );

        List<QName> propNames = new ArrayList<QName>();
        // TODO which GML version
        for ( PropertyType pt : ft.getPropertyDeclarations( GML_31 ) ) {
            propNames.add( pt.getName() );
        }

        QName match = QNameUtils.findBestMatch( propName.getAsQName(), propNames );
        if ( !match.equals( propName.getAsQName() ) ) {
            LOG.warn( "Repairing unqualified PropertyName: " + QNameUtils.toString( propName.getAsQName() ) + " -> "
                      + QNameUtils.toString( match ) );
            // vague match
            String text = match.getLocalPart();
            if ( !match.getPrefix().equals( DEFAULT_NS_PREFIX ) ) {
                text = match.getPrefix() + ":" + match.getLocalPart();
            }
            org.deegree.commons.xml.NamespaceContext nsContext = new org.deegree.commons.xml.NamespaceContext();
            nsContext.addNamespace( match.getPrefix(), match.getNamespaceURI() );
            propName.set( text, nsContext );
        }
    }

    // TODO do this properly
    private QName getPropertyNameAsQName( PropertyName propName ) {
        QName name = null;
        NamespaceContext nsContext = propName.getNsContext();
        String s = propName.getPropertyName();
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
                        nsUri = XMLConstants.NULL_NS_URI;
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

    private void validateGeometryConstraint( Geometry geom, CRS queriedCrs )
                            throws OWSException {

        // check if geometry's bbox is inside the domain of its CRS
        Envelope bbox = geom.getEnvelope();
        if ( bbox.getCoordinateSystem() != null ) {
            // check if geometry's bbox is valid with respect to the CRS domain
            try {
                double[] b = bbox.getCoordinateSystem().getAreaOfUse();
                Envelope domainOfValidity = geomFac.createEnvelope( b[0], b[1], b[2], b[3], CRS.EPSG_4326 );
                domainOfValidity = transform( domainOfValidity, bbox.getCoordinateSystem() );
                if ( !bbox.isWithin( domainOfValidity ) ) {
                    String msg = "Invalid geometry constraint in filter. The envelope of the geometry is not within the domain of validity ('"
                                 + domainOfValidity + "') of its CRS ('" + bbox.getCoordinateSystem().getName() + "').";
                    throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "filter" );
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
                double[] b = queriedCrs.getAreaOfUse();
                Envelope domainOfValidity = geomFac.createEnvelope( b[0], b[1], b[2], b[3], CRS.EPSG_4326 );
                domainOfValidity = transform( domainOfValidity, queriedCrs );
                Envelope bboxTransformed = transform( bbox, queriedCrs );
                if ( !bboxTransformed.isWithin( domainOfValidity ) ) {
                    String msg = "Invalid geometry constraint in filter. The envelope of the geometry is not within the domain of validity ('"
                                 + domainOfValidity + "') of the queried CRS ('" + queriedCrs.getName() + "').";
                    throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "filter" );
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

    private Envelope transform( Envelope bbox, CRS targetCrs )
                            throws IllegalArgumentException, TransformationException, UnknownCRSException {
        if ( bbox.getEnvelope().equals( targetCrs ) ) {
            return bbox;
        }
        GeometryTransformer transformer = new GeometryTransformer( targetCrs.getWrappedCRS() );
        return (Envelope) transformer.transform( bbox );
    }
}