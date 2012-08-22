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
package org.deegree.security.owsrequestvalidator.csw;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.FeatureFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterConstructionException;
import org.deegree.model.filterencoding.LogicalOperation;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.csw.discovery.GetRecords;
import org.deegree.ogcwebservices.csw.discovery.Query;
import org.deegree.ogcwebservices.csw.discovery.XMLFactory;
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
import org.deegree.security.owsrequestvalidator.Messages;
import org.deegree.security.owsrequestvalidator.Policy;
import org.xml.sax.SAXException;

/**
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class GetRecordsRequestValidator extends AbstractCSWRequestValidator {

    private static final ILogger LOG = LoggerFactory.getLogger( GetRecordsRequestValidator.class );

    private static final String ELEMENTSETNAME = "elementSetName";

    private static final String MAXRECORDS = "maxRecords";

    private static final String OUTPUTFORMAT = "outputFormat";

    private static final String RESULTTYPE = "resultType";

    private static final String SORTBY = "sortBy";

    private static final String TYPENAMES = "typeNames";

    private static FeatureType grFT = null;

    private static Map<String, Filter> filterMap = new HashMap<String, Filter>();

    static {
        if ( grFT == null ) {
            grFT = GetRecordsRequestValidator.createFeatureType();
        }
    }

    /**
     * @param policy
     */
    public GetRecordsRequestValidator( Policy policy ) {
        super( policy );
    }

    /**
     * @param request
     * @param user
     */
    @Override
    public void validateRequest( OGCWebServiceRequest request, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        userCoupled = false;
        Request req = policy.getRequest( "CSW", "GetRecords" );
        // request is valid because no restrictions are made
        if ( req.isAny() || req.getPreConditions().isAny() ) {
            return;
        }
        Condition condition = req.getPreConditions();

        GetRecords casreq = (GetRecords) request;

        validateVersion( condition, casreq.getVersion() );

        // validateRecordTypes( condition, tn );
        validateMaxRecords( condition, casreq.getMaxRecords() );
        validateOutputFormat( condition, casreq.getOutputFormat() );
        validateResultType( condition, casreq.getResultTypeAsString() );
        validateElementSetName( condition, casreq.getQuery().getElementSetName() );
        validateSortBy( condition, casreq.getQuery().getSortProperties() );
        List<QualifiedName> list = casreq.getQuery().getTypeNamesAsList();
        validateTypeNames( condition, list );

        if ( userCoupled ) {
            validateAgainstRightsDB( casreq, user );
        }

        if ( req.getPostConditions() != null ) {
            addFilter( casreq, req.getPostConditions(), user );
        }

    }

    /**
     * adds an additional Filter read from parameter 'instanceFilter'to the Filter of the passed
     * GetFeature request. If parameter 'instanceFilter' is userCoupled the filter will be read from
     * DRM, if it is not the filter defined within the responsible policy document will be used.
     *
     * @param casreq
     * @param postConditions
     * @param user
     * @throws InvalidParameterValueException
     * @throws UnauthorizedException
     */
    private void addFilter( GetRecords casreq, Condition postConditions, User user )
                            throws InvalidParameterValueException, UnauthorizedException {
        if ( postConditions.getOperationParameter( "instanceFilter" ) != null ) {
            Map<String, Filter> localFilterMap;
            if ( postConditions.getOperationParameter( "instanceFilter" ).isUserCoupled() ) {
                // read filterMap from constraints defined in deegree DRM
                localFilterMap = readFilterFromDRM( casreq, user );
            } else {
                fillFilterMap( postConditions );
                // use filterMap read from policy document
                localFilterMap = filterMap;
            }
            Query query = casreq.getQuery();
            Filter filter = null;
            if ( query.getContraint() == null ) {
                // if query does not define a filter just use the matching
                // one from the post conditions
                filter = localFilterMap.get( casreq.getOutputSchema() );
            } else if ( query.getContraint() instanceof ComplexFilter ) {
                // create a new Filter that is a combination of the
                // original filter and the one defined in the GetFeatures
                // PostConditions coupled by a logical 'And'
                ComplexFilter qFilter = (ComplexFilter) query.getContraint();
                filter = localFilterMap.get( casreq.getOutputSchema() );
                if ( filter == null ) {
                    filter = qFilter;
                } else {
                    filter = new ComplexFilter( qFilter, (ComplexFilter) filter, OperationDefines.AND );
                }
            } else if ( query.getContraint() instanceof FeatureFilter ) {
                // just take original filter if it is as feature filter
                // because feature filter and complex filters can not
                // be combined
                filter = query.getContraint();
            }
            // substitue query by a new one using the re-created filter
            query = new Query( query.getElementSetName(), query.getElementSetNameTypeNamesList(),
                               query.getElementSetNameVariables(), query.getElementNamesAsPropertyPaths(),
                               filter, query.getSortProperties(), query.getTypeNamesAsList(),
                               query.getDeclaredTypeNameVariables() );

            casreq.setQuery( query );
        }
        if ( LOG.getLevel() != ILogger.LOG_DEBUG ) {
            try {
                XMLFactory.export( casreq ).prettyPrint( System.out );
            } catch ( Exception e ) {
            }
        }
    }

    private void fillFilterMap( Condition postConditions ) {
//        List<Element> complexValues =
        postConditions.getOperationParameter( "instanceFilter" ).getComplexValues();
        /*
         * TODO try { if ( filterMap.size() == 0 ) { for ( int i = 0; i < complexValues.size(); i++ ) {
         * Query q = Query.create( complexValues.get( 0 ) ); Filter f = q.getFilter(); QualifiedName
         * qn = q.getTypeNames()[0]; filterMap.put( qn, f ); } } } catch ( XMLParsingException e ) {
         * LOG.logError( e.getMessage(), e ); throw new InvalidParameterValueException(
         * this.getClass().getName(), e.getMessage() ); }
         */
    }

    private Map<String, Filter> readFilterFromDRM( GetRecords casreq, User user )
                            throws UnauthorizedException, InvalidParameterValueException {

        Map<String, Filter> map = new HashMap<String, Filter>();
        try {
            SecurityAccessManager sam = SecurityAccessManager.getInstance();
            SecurityAccess access = sam.acquireAccess( user );
            String ops = casreq.getOutputSchema();
            SecuredObject secObj = access.getSecuredObjectByName( ops, ClientHelper.TYPE_METADATASCHEMA );
            RightSet rs = user.getRights( access, secObj );
            Right right = rs.getRight( secObj, RightType.GETRECORDS_RESPONSE );
            // a constraint - if available - is constructed as a OGC Filter
            // one of the filter operations may is 'PropertyIsEqualTo' and
            // defines a ProperyName == 'instanceFilter'. The Literal of this
            // operation itself is a complete and valid Filter expression.
            if ( right != null ) {
                ComplexFilter filter = (ComplexFilter) right.getConstraints();
                if ( filter != null ) {
                    List<ComplexFilter> foundFilters = new ArrayList<ComplexFilter>();
                    // extract filter expression to be used as additional
                    // filter for a GetFeature request
                    extractInstanceFilter( filter.getOperation(), foundFilters );
                    if ( foundFilters.size() == 1 ) {
                        filter = foundFilters.get( 0 );
                    } else if ( foundFilters.size() > 1 ) {
                        List<org.deegree.model.filterencoding.Operation> list = new ArrayList<org.deegree.model.filterencoding.Operation>();
                        for ( ComplexFilter cf : foundFilters ) {
                            list.add( cf.getOperation() );
                        }
                        LogicalOperation lo = new LogicalOperation( OperationDefines.OR, list );
                        filter = new ComplexFilter( lo );
                    }
                    map.put( ops, filter );
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

    /**
     * validates the passed CSW GetRecords request against a User- and Rights-Management DB.
     *
     * @param casreq
     * @param user
     */
    private void validateAgainstRightsDB( GetRecords casreq, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        if ( user == null ) {
            throw new UnauthorizedException( Messages.getString( "RequestValidator.NOACCESS" ) );
        }

        // create a feature instance from the parameters of the GetRecords request
        // to enable comparsion with a filter encoding expression stored in the
        // assigned rights management system
        List<FeatureProperty> fp = new ArrayList<FeatureProperty>();
        fp.add( FeatureFactory.createFeatureProperty( new QualifiedName( "version" ), casreq.getVersion() ) );
        fp.add( FeatureFactory.createFeatureProperty( new QualifiedName( "maxRecords" ), casreq.getMaxRecords() ) );
        fp.add( FeatureFactory.createFeatureProperty( new QualifiedName( "outputFormat" ), casreq.getOutputFormat() ) );
        fp.add( FeatureFactory.createFeatureProperty( new QualifiedName( "resultType" ), casreq.getResultTypeAsString() ) );
        SortProperty[] sp = casreq.getQuery().getSortProperties();
        if ( sp != null ) {
            for ( int i = 0; i < sp.length; i++ ) {
                fp.add( FeatureFactory.createFeatureProperty( new QualifiedName( "sortBy" ),
                                                              sp[i].getSortProperty().getAsString() ) );
            }
        }
        List<QualifiedName> tp = casreq.getQuery().getTypeNamesAsList();
        for ( int i = 0; i < tp.size(); i++ ) {
            fp.add( FeatureFactory.createFeatureProperty( new QualifiedName( "typeNames" ),
                                                          tp.get( i ).getPrefixedName() ) );
        }
        fp.add( FeatureFactory.createFeatureProperty( new QualifiedName( "elementSetName" ),
                                                      casreq.getQuery().getElementSetName() ) );

        Feature feature = FeatureFactory.createFeature( "id", grFT, fp );
        handleUserCoupledRules( user, feature, casreq.getOutputSchema(), ClientHelper.TYPE_METADATASCHEMA,
                                RightType.GETRECORDS );

    }

    /**
     * valides if the maxRecords parameter in a GetRecords request is valid against the policy
     * assigned to Validator.
     *
     * @param condition
     * @param maxRecords
     * @throws InvalidParameterValueException
     */
    private void validateMaxRecords( Condition condition, int maxRecords )
                            throws InvalidParameterValueException {
        OperationParameter op = condition.getOperationParameter( MAXRECORDS );

        // is valid because no restrictions are made
        if ( op.isAny() )
            return;

        int maxF = op.getFirstAsInt();

        if ( op.isUserCoupled() ) {
            userCoupled = true;
        } else {
            if ( maxRecords > maxF || maxRecords < 0 ) {
                String s = Messages.format( "GetRecordsRequestValidator.INVALIDMAXRECORDS", MAXRECORDS );
                throw new InvalidParameterValueException( s );
            }
        }

    }

    /**
     * valides if the elementSetName parameter in a GetRecords request is valid against the policy
     * assigned to Validator.
     *
     * @param condition
     * @param elementSetName
     * @throws InvalidParameterValueException
     */
    private void validateElementSetName( Condition condition, String elementSetName )
                            throws InvalidParameterValueException {
        OperationParameter op = condition.getOperationParameter( ELEMENTSETNAME );

        // is valid because no restrictions are made
        if ( op.isAny() )
            return;

        List<String> list = op.getValues();

        if ( op.isUserCoupled() ) {
            userCoupled = true;
        } else {
            if ( !list.contains( elementSetName ) ) {
                String s = Messages.format( "GetRecordsRequestValidator.INVALIDELEMENTSETNAME", elementSetName );
                throw new InvalidParameterValueException( s );
            }
        }

    }

    /**
     * valides if the metadataFormat parameter in a GetRecords request is valid against the policy
     * assigned to Validator.
     *
     * @param condition
     * @param outputFormat
     * @throws InvalidParameterValueException
     */
    private void validateOutputFormat( Condition condition, String outputFormat )
                            throws InvalidParameterValueException {
        OperationParameter op = condition.getOperationParameter( OUTPUTFORMAT );

        // is valid because no restrictions are made
        if ( op.isAny() )
            return;

        List<String> list = op.getValues();

        if ( op.isUserCoupled() ) {
            userCoupled = true;
        } else {
            if ( !list.contains( outputFormat ) ) {
                String s = Messages.format( "GetRecordsRequestValidator.INVALIDOUTPUTFORMAT", outputFormat );
                throw new InvalidParameterValueException( s );
            }
        }

    }

    /**
     * valides if the resultType parameter in a GetRecords request is valid against the policy
     * assigned to Validator.
     *
     * @param condition
     * @param resultType
     * @throws InvalidParameterValueException
     */
    private void validateResultType( Condition condition, String resultType )
                            throws InvalidParameterValueException {
        OperationParameter op = condition.getOperationParameter( RESULTTYPE );

        // is valid because no restrictions are made
        if ( op.isAny() )
            return;

        List<String> list = op.getValues();

        if ( op.isUserCoupled() ) {
            userCoupled = true;
        } else {
            if ( !list.contains( resultType ) ) {
                String s = Messages.format( "GetRecordsRequestValidator.INVALIDRESULTTYPE", resultType );
                throw new InvalidParameterValueException( s );
            }
        }

    }

    /**
     * valides if the sortBy parameter in a GetRecords request is valid against the policy assigned
     * to Validator.
     *
     * @param condition
     * @param sortBy
     * @throws InvalidParameterValueException
     */
    private void validateSortBy( Condition condition, SortProperty[] sortBy )
                            throws InvalidParameterValueException {
        OperationParameter op = condition.getOperationParameter( SORTBY );

        // is valid because no restrictions are made or
        // nothing to validate
        if ( op.isAny() || sortBy == null )
            return;

        List<String> list = op.getValues();

        if ( op.isUserCoupled() ) {
            userCoupled = true;
        } else {
            for ( int i = 0; i < sortBy.length; i++ ) {

                if ( !list.contains( sortBy[i].getSortProperty().getAsString() ) ) {
                    String s = Messages.format( "GetRecordsRequestValidator.INVALIDSORTBY", sortBy[i] );
                    throw new InvalidParameterValueException( s );
                }
            }
        }

    }

    /**
     * valides if the sortBy parameter in a GetRecords request is valid against the policy assigned
     * to Validator.
     *
     * @param condition
     * @param typeNames
     * @throws InvalidParameterValueException
     */
    private void validateTypeNames( Condition condition, List<QualifiedName> typeNames )
                            throws InvalidParameterValueException {
        OperationParameter op = condition.getOperationParameter( TYPENAMES );

        // is valid because no restrictions are made
        if ( op.isAny() )
            return;

        List<String> list = op.getValues();

        if ( op.isUserCoupled() ) {
            userCoupled = true;
        } else {
            for ( int i = 0; i < typeNames.size(); i++ ) {
                if ( !list.contains( typeNames.get( i ).getPrefixedName() ) ) {
                    String s = Messages.format( "GetRecordsRequestValidator.INVALIDTYPENAMES", typeNames.get( i ) );
                    throw new InvalidParameterValueException( s );
                }
            }
        }

    }

    /**
     * creates a feature type that matches the parameters of a GetRecords request
     *
     * @return created <tt>FeatureType</tt>
     */
    private static FeatureType createFeatureType() {
        PropertyType[] ftps = new PropertyType[7];
        QualifiedName qn = new QualifiedName( "version" );
        ftps[0] = FeatureFactory.createSimplePropertyType( qn, Types.VARCHAR, false );

        qn = new QualifiedName( "maxRecords" );
        ftps[1] = FeatureFactory.createSimplePropertyType( qn, Types.INTEGER, false );

        qn = new QualifiedName( "outputFormat" );
        ftps[2] = FeatureFactory.createSimplePropertyType( qn, Types.VARCHAR, false );

        qn = new QualifiedName( "resultType" );
        ftps[3] = FeatureFactory.createSimplePropertyType( qn, Types.VARCHAR, false );

        qn = new QualifiedName( "sortBy" );
        ftps[4] = FeatureFactory.createSimplePropertyType( qn, Types.VARCHAR, 0, Integer.MAX_VALUE );

        qn = new QualifiedName( "typeNames" );
        ftps[5] = FeatureFactory.createSimplePropertyType( qn, Types.VARCHAR, 0, Integer.MAX_VALUE );

        qn = new QualifiedName( "elementSetName" );
        ftps[6] = FeatureFactory.createSimplePropertyType( qn, Types.VARCHAR, false );

        return FeatureFactory.createFeatureType( "GetRecords", false, ftps );
    }

}
