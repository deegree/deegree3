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
package org.deegree.security.owsrequestvalidator.wfs;

import static org.deegree.portal.standard.security.control.ClientHelper.TYPE_FEATURETYPE;
import static org.deegree.security.drm.model.RightType.GETFEATURE;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.i18n.Messages;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.FeatureFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterConstructionException;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wfs.XMLFactory;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.deegree.portal.standard.security.control.ClientHelper;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.model.Right;
import org.deegree.security.drm.model.RightSet;
import org.deegree.security.drm.model.RightType;
import org.deegree.security.drm.model.SecuredObject;
import org.deegree.security.drm.model.User;
import org.deegree.security.owsproxy.Condition;
import org.deegree.security.owsproxy.OperationParameter;
import org.deegree.security.owsproxy.Request;
import org.deegree.security.owsrequestvalidator.Policy;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version 1.1, $Revision$, $Date$
 *
 * @since 1.1
 */
class GetFeatureRequestValidator extends AbstractWFSRequestValidator {

    private static final ILogger LOG = LoggerFactory.getLogger( GetFeatureRequestValidator.class );

    // known condition parameter
    private static final String FORMAT = "format";

    private static final String MAXFEATURES = "maxFeatures";

    private static Map<QualifiedName, Filter> filterMap = new HashMap<QualifiedName, Filter>();

    private static FeatureType gfFT = null;

    static {
        if ( gfFT == null ) {
            gfFT = GetFeatureRequestValidator.createFeatureType();
        }
    }

    /**
     * @param policy
     */
    public GetFeatureRequestValidator( Policy policy ) {
        super( policy );
    }

    /**
     * validates if the passed request is valid against the policy assigned to the validator. If the passed user is not
     * <tt>null</tt> user coupled parameters will be validated against a users and rights management system.
     */
    @Override
    public void validateRequest( OGCWebServiceRequest request, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        userCoupled = false;
        Request req = policy.getRequest( "WFS", "GetFeature" );
        // request is valid because no restrictions are made
        if ( req.isAny() || req.getPreConditions().isAny() ) {
            return;
        }
        Condition condition = req.getPreConditions();

        GetFeature wfsreq = (GetFeature) request;

        validateVersion( condition, wfsreq.getVersion() );

        Query[] queries = wfsreq.getQuery();
        String[] ft = new String[queries.length];
        for ( int i = 0; i < ft.length; i++ ) {
            ft[i] = queries[i].getTypeNames()[0].getFormattedString();
        }

        validateFeatureTypes( condition, ft );
        validateFormat( condition, wfsreq.getOutputFormat() );
        validateMaxFeatures( condition, wfsreq.getMaxFeatures() );

        if ( userCoupled ) {
            validateAgainstRightsDB( wfsreq, user );
        }

        if ( req.getPostConditions() != null ) {
            addFilter( wfsreq, req.getPostConditions(), user );
        }

    }

    /**
     * adds an additional Filter read from parameter 'instanceFilter'to the Filter of the passed GetFeature request. If
     * parameter 'instanceFilter' is userCoupled the filter will be read from DRM, if it is not the filter defined
     * within the responsible policy document will be used.
     *
     * @param wfsreq
     * @param postConditions
     * @param user
     * @throws InvalidParameterValueException
     * @throws UnauthorizedException
     */
    private void addFilter( GetFeature wfsreq, Condition postConditions, User user )
                            throws InvalidParameterValueException, UnauthorizedException {
        if ( LOG.isDebug() ) {
            LOG.logDebug( "Feature type", wfsreq.getQuery()[0].getTypeNames()[0] );
        }
        if ( postConditions.getOperationParameter( "instanceFilter" ) != null
             && !postConditions.getOperationParameter( "instanceFilter" ).isAny() ) {
            Map<QualifiedName, Filter> localFilterMap;
            if ( postConditions.getOperationParameter( "instanceFilter" ).isUserCoupled() ) {
                // read filterMap from constraints defined in deegree DRM
                localFilterMap = readFilterFromDRM( wfsreq, user );
                LOG.logDebug( "Filter map from DRM", localFilterMap );
            } else {
                fillFilterMap( postConditions );
                // use filterMap read from policy document
                localFilterMap = filterMap;
            }
            Query[] queries = wfsreq.getQuery();
            for ( int i = 0; i < queries.length; i++ ) {
                Filter filter = null;
                if ( queries[i].getFilter() == null ) {
                    // if query does not define a filter just use the matching
                    // one from the post conditions
                    filter = localFilterMap.get( queries[i].getTypeNames()[0] );
                } else if ( queries[i].getFilter() instanceof ComplexFilter ) {
                    // create a new Filter that is a combination of the
                    // original filter and the one defined in the GetFeatures
                    // PostConditions coupled by a logical 'And'
                    ComplexFilter qFilter = (ComplexFilter) queries[i].getFilter();
                    filter = localFilterMap.get( queries[i].getTypeNames()[0] );
                    if ( filter == null ) {
                        filter = qFilter;
                    } else {
                        filter = new ComplexFilter( qFilter, (ComplexFilter) filter, OperationDefines.AND );
                    }
                } else if ( queries[i].getFilter() instanceof FeatureFilter ) {
                    // just take original filter if it is as feature filter
                    // because feature filter and complex filters can not
                    // be combined
                    filter = queries[i].getFilter();
                }

                if ( LOG.isDebug() ) {
                    LOG.logDebug( "Filter", filter == null ? " is null" : filter.to110XML() );
                }

                // substitue query by a new one using the re-created filter
                queries[i] = Query.create( queries[i].getPropertyNames(), queries[i].getFunctions(),
                                           queries[i].getSortProperties(), queries[i].getHandle(),
                                           queries[i].getFeatureVersion(), queries[i].getTypeNames(),
                                           queries[i].getAliases(), queries[i].getSrsName(), filter,
                                           queries[i].getMaxFeatures(), queries[i].getStartPosition(),
                                           queries[i].getResultType() );
            }
            wfsreq.setQueries( queries );
        }
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            try {
                XMLFactory.export( wfsreq ).prettyPrint( System.out );
            } catch ( Exception e ) {
                // nottin
            }
        }
    }

    /**
     *
     * @param wfsreq
     * @param user
     * @return a map with the filters
     * @throws UnauthorizedException
     * @throws InvalidParameterValueException
     */
    private Map<QualifiedName, Filter> readFilterFromDRM( GetFeature wfsreq, User user )
                            throws UnauthorizedException, InvalidParameterValueException {

        Map<QualifiedName, Filter> map = new HashMap<QualifiedName, Filter>();
        try {
            SecurityAccessManager sam = SecurityAccessManager.getInstance();
            SecurityAccess access = sam.acquireAccess( user );
            Query[] queries = wfsreq.getQuery();
            for ( int i = 0; i < queries.length; i++ ) {
                QualifiedName qn = queries[i].getTypeNames()[0];
                SecuredObject secObj = access.getSecuredObjectByName( qn.getFormattedString(),
                                                                      ClientHelper.TYPE_FEATURETYPE );

                RightSet rs = user.getRights( access, secObj );
                Right right = rs.getRight( secObj, RightType.GETFEATURE_RESPONSE );
                // a constraint - if available - is constructed as a OGC Filter
                // one of the filter operations may is 'PropertyIsEqualTo' and
                // defines a ProperyName == 'instanceFilter'. The Literal of this
                // operation itself is a complete and valid Filter expression.

                if ( right != null ) {
                    ComplexFilter filter = (ComplexFilter) right.getConstraints();
                    if ( filter != null ) {
                        // extract filter expression to be used as additional
                        // filter for a GetFeature request
                        LOG.logDebug( "Filter before extraction", filter.toXML() );
                        filter = extractInstanceFilter( filter.getOperation() );
                        if ( filter != null ) {
                            map.put( qn, filter );
                            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                                LOG.logDebug( "instance filter for right GETFEATURE_RESPONSE", filter.toXML() );
                            }
                        } else {
                            LOG.logDebug( "no instance filter defined for right GETFEATURE_RESPONSE and feature type: "
                                          + qn );
                        }
                    } else {
                        LOG.logDebug( "no constraint defined for right GETFEATURE_RESPONSE and feature type: " + qn );
                    }
                } else {
                    LOG.logDebug( "right GETFEATURE_RESPONSE not defined for current user and feature type: " + qn );
                }
            }
        } catch ( GeneralSecurityException e ) {
            LOG.logError( e.getMessage(), e );
            throw new UnauthorizedException( e.getMessage(), e );
        } catch ( FilterConstructionException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        } catch ( SAXException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        }

        return map;
    }

    private void fillFilterMap( Condition postConditions )
                            throws InvalidParameterValueException {
        List<Element> complexValues = postConditions.getOperationParameter( "instanceFilter" ).getComplexValues();
        try {
            if ( filterMap.size() == 0 ) {
                for ( int i = 0; i < complexValues.size(); i++ ) {
                    Query q = Query.create( complexValues.get( i ) );
                    Filter f = q.getFilter();
                    QualifiedName qn = q.getTypeNames()[0];
                    filterMap.put( qn, f );
                }
            }
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( this.getClass().getName(), e.getMessage() );
        }
    }

    /**
     * valides if the format you in a GetFeature request is valid against the policy assigned to Validator. If the
     * passed user is not <tt>null</tt> and the format parameter is user coupled the format will be validated against a
     * users and rights management system.
     *
     * @param condition
     * @param format
     * @throws InvalidParameterValueException
     */
    private void validateFormat( Condition condition, String format )
                            throws InvalidParameterValueException {
        OperationParameter op = condition.getOperationParameter( FORMAT );

        // version is valid because no restrictions are made
        if ( op.isAny() )
            return;

        List<String> validLayers = op.getValues();
        if ( op.isUserCoupled() ) {
            userCoupled = true;
        } else {
            if ( !validLayers.contains( format ) ) {
                String s = Messages.getMessage( "OWSPROXY_DESCRIBEFEATURETYPE_FORMAT", format );
                throw new InvalidParameterValueException( s );
            }
        }

    }

    /**
     * valides if the format you in a GetFeature request is valid against the policy assigned to Validator. If the
     * passed user is not <tt>null</tt> and the maxFeatures parameter is user coupled the maxFeatures will be validated
     * against a users and rights management system.
     *
     * @param condition
     * @param maxFeatures
     * @throws InvalidParameterValueException
     */
    private void validateMaxFeatures( Condition condition, int maxFeatures )
                            throws InvalidParameterValueException {
        OperationParameter op = condition.getOperationParameter( MAXFEATURES );

        // version is valid because no restrictions are made
        if ( op.isAny() )
            return;

        int maxF = Integer.parseInt( op.getValues().get( 0 ) );

        if ( op.isUserCoupled() ) {
            userCoupled = true;
        } else {
            if ( maxFeatures > maxF || maxFeatures < 0 ) {
                String s = Messages.getMessage( "OWSPROXY_GETFEATURE_MAXFEATURE", maxFeatures );
                throw new InvalidParameterValueException( s );
            }
        }

    }

    /**
     * validates the passed WMS GetMap request against a User- and Rights-Management DB.
     *
     * @param wfsreq
     * @param user
     * @throws InvalidParameterValueException
     */
    private void validateAgainstRightsDB( GetFeature wfsreq, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        if ( user == null ) {
            throw new UnauthorizedException( "no access to anonymous user" );
        }

        // create feature that describes the map request
        FeatureProperty[] fps = new FeatureProperty[3];
        fps[0] = FeatureFactory.createFeatureProperty( new QualifiedName( "version" ), wfsreq.getVersion() );
        Integer mxf = new Integer( wfsreq.getMaxFeatures() );
        // The database can handle "features as a key", this feature is build from the request's
        // features
        fps[1] = FeatureFactory.createFeatureProperty( new QualifiedName( "maxfeatures" ), mxf );
        fps[2] = FeatureFactory.createFeatureProperty( new QualifiedName( "outputformat" ), wfsreq.getOutputFormat() );

        Feature feature = FeatureFactory.createFeature( "id", gfFT, fps );
        Query[] queries = wfsreq.getQuery();
        for ( int i = 0; i < queries.length; i++ ) {
            StringBuffer sb = new StringBuffer( 200 );
            sb.append( '{' ).append( queries[i].getTypeNames()[0].getNamespace().toASCIIString() );
            sb.append( "}:" ).append( queries[i].getTypeNames()[0].getLocalName() );
            if ( securityConfig.getProxiedUrl() == null ) {
                handleUserCoupledRules( user, // the user who posted the request
                                        feature, // This is the Database feature
                                        sb.toString(), // the Qualified name of the users Featurerequest
                                        TYPE_FEATURETYPE, // a primary key in the db.
                                        GETFEATURE );// We're requesting a featuretype.
            } else {
                handleUserCoupledRules( user, // the user who posted the request
                                        feature, // This is the Database feature
                                        "[" + securityConfig.getProxiedUrl() + "]:" + sb, // the Qualified name of the
                                        // users Featurerequest
                                        TYPE_FEATURETYPE, // a primary key in the db.
                                        GETFEATURE );// We're requesting a featuretype.
            }
        }

    }

    /**
     * creates a feature type that matches the parameters of a GetLagendGraphic request
     *
     * @return created <tt>FeatureType</tt>
     */
    private static FeatureType createFeatureType() {
        PropertyType[] ftps = new PropertyType[3];
        ftps[0] = FeatureFactory.createSimplePropertyType( new QualifiedName( "version" ), Types.VARCHAR, false );
        ftps[1] = FeatureFactory.createSimplePropertyType( new QualifiedName( "maxfeatures" ), Types.INTEGER, false );
        ftps[2] = FeatureFactory.createSimplePropertyType( new QualifiedName( "outputformat" ), Types.VARCHAR, false );

        return FeatureFactory.createFeatureType( "GetFeature", false, ftps );
    }
}
