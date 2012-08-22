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
import static org.deegree.security.drm.model.RightType.DELETE;
import static org.deegree.security.drm.model.RightType.INSERT;
import static org.deegree.security.drm.model.RightType.UPDATE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
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
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.deegree.ogcwebservices.wfs.operation.transaction.Delete;
import org.deegree.ogcwebservices.wfs.operation.transaction.Insert;
import org.deegree.ogcwebservices.wfs.operation.transaction.Transaction;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionOperation;
import org.deegree.ogcwebservices.wfs.operation.transaction.Update;
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
 * Validator for OGC CSW Transaction requests. It will validated values of:<br>
 * <ul>
 * <li>service version</li>
 * <li>operation</li>
 * <li>type names</li>
 * <li>metadata standard</li>
 * </ul>
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class TransactionValidator extends AbstractWFSRequestValidator {

    private static final ILogger LOG = LoggerFactory.getLogger( TransactionValidator.class );

    private final static String TYPENAME = "typeName";

    private static Map<QualifiedName, Filter> filterMap = new HashMap<QualifiedName, Filter>();

    private static FeatureType insertFT = null;

    private static FeatureType updateFT = null;

    private static FeatureType deleteFT = null;

    static {
        if ( insertFT == null ) {
            insertFT = TransactionValidator.createInsertFeatureType();
        }
        if ( updateFT == null ) {
            updateFT = TransactionValidator.createUpdateFeatureType();
        }
        if ( deleteFT == null ) {
            deleteFT = TransactionValidator.createDeleteFeatureType();
        }
    }

    /**
     *
     * @param policy
     */
    public TransactionValidator( Policy policy ) {
        super( policy );
    }

    @Override
    public void validateRequest( OGCWebServiceRequest request, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        userCoupled = false;

        Transaction wfsreq = (Transaction) request;

        List<TransactionOperation> ops = wfsreq.getOperations();
        for ( int i = 0; i < ops.size(); i++ ) {
            userCoupled = false;
            if ( ops.get( i ) instanceof Insert ) {
                Request req = policy.getRequest( "WFS", "WFS_Insert" );
                if ( !req.isAny() && !req.getPreConditions().isAny() ) {
                    Condition condition = req.getPreConditions();
                    validateOperation( condition, (Insert) ops.get( i ) );
                }
                if ( userCoupled ) {
                    validateAgainstRightsDB( (Insert) ops.get( i ), user );
                }
            } else if ( ops.get( i ) instanceof Update ) {
                Request req = policy.getRequest( "WFS", "WFS_Update" );
                if ( !req.isAny() && !req.getPreConditions().isAny() ) {
                    Condition condition = req.getPreConditions();
                    validateOperation( condition, (Update) ops.get( i ) );
                }
                if ( userCoupled ) {
                    validateAgainstRightsDB( (Update) ops.get( i ), user );
                }
                if ( req.getPostConditions() != null ) {
                    addFilter( ops.get( i ), req.getPostConditions(), user );
                }
            } else if ( ops.get( i ) instanceof Delete ) {
                Request req = policy.getRequest( "WFS", "WFS_Delete" );
                if ( !req.isAny() && !req.getPreConditions().isAny() ) {
                    Condition condition = req.getPreConditions();
                    validateOperation( condition, (Delete) ops.get( i ) );
                }
                if ( userCoupled ) {
                    validateAgainstRightsDB( (Delete) ops.get( i ), user );
                }
                if ( req.getPostConditions() != null ) {
                    addFilter( ops.get( i ), req.getPostConditions(), user );
                }
            }
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
     * adds a filter to the passed opertaion. If the condition is userCoupled the filter will be read from the DRM
     * otherwise it is read from the current WFS policy file
     *
     * @param operation
     * @param postConditions
     * @param user
     * @throws InvalidParameterValueException
     * @throws UnauthorizedException
     */
    private void addFilter( TransactionOperation operation, Condition postConditions, User user )
                            throws InvalidParameterValueException, UnauthorizedException {
        if ( postConditions.getOperationParameter( "instanceFilter" ) != null ) {
            Filter opFilter = null;
            if ( operation instanceof Update ) {
                opFilter = ( (Update) operation ).getFilter();
            } else {
                opFilter = ( (Delete) operation ).getFilter();
            }
            Filter filter = null;
            if ( postConditions.getOperationParameter( "instanceFilter" ).isUserCoupled() ) {
                // read filterMap from constraints defined in deegree DRM
                filter = readFilterFromDRM( operation, user );
            } else {
                fillFilterMap( postConditions );
                // use filterMap read from policy document
                filter = filterMap.get( operation.getAffectedFeatureTypes().get( 0 ) );
            }

            if ( opFilter instanceof ComplexFilter ) {
                // create a new Filter that is a combination of the
                // original filter and the one defined in the GetFeatures
                // PostConditions coupled by a logical 'And'
                ComplexFilter qFilter = (ComplexFilter) opFilter;
                if ( filter == null ) {
                    filter = qFilter;
                } else {
                    filter = new ComplexFilter( qFilter, (ComplexFilter) filter, OperationDefines.AND );
                }
            } else if ( opFilter instanceof FeatureFilter ) {
                // just take original filter if it is as feature filter
                // because feature filter and complex filters can not
                // be combined
                filter = opFilter;
            }
            if ( operation instanceof Update ) {
                ( (Update) operation ).setFilter( filter );
            } else {
                ( (Delete) operation ).setFilter( filter );
            }
        }
    }

    /**
     * reads a filter m
     *
     * @param operation
     * @param user
     * @return the defined filter for the given operation or <code>null</code> if no such filter was found.
     * @throws UnauthorizedException
     * @throws InvalidParameterValueException
     */
    private Filter readFilterFromDRM( TransactionOperation operation, User user )
                            throws UnauthorizedException, InvalidParameterValueException {
        Filter f = null;
        try {
            SecurityAccessManager sam = SecurityAccessManager.getInstance();
            SecurityAccess access = sam.acquireAccess( user );

            QualifiedName qn = operation.getAffectedFeatureTypes().get( 0 );
            SecuredObject secObj = access.getSecuredObjectByName( qn.getFormattedString(),
                                                                  ClientHelper.TYPE_FEATURETYPE );

            RightSet rs = user.getRights( access, secObj );
            Right right = null;
            if ( operation instanceof Update ) {
                right = rs.getRight( secObj, RightType.UPDATE_RESPONSE );
            } else {
                right = rs.getRight( secObj, RightType.DELETE_RESPONSE );
            }

            // a constraint - if available - is constructed as a OGC Filter
            // one of the filter operations may is 'PropertyIsEqualTo' and
            // defines a ProperyName == 'instanceFilter'. The Literal of this
            // operation itself is a complete and valid Filter expression.
            if ( right != null ) {
                ComplexFilter filter = (ComplexFilter) right.getConstraints();
                if ( filter != null ) {
                    // extract filter expression to be used as additional
                    // filter for a GetFeature request
                    filter = extractInstanceFilter( filter.getOperation() );
                    if ( filter != null ) {
                        f = filter;
                    }
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
        return f;
    }

    private void fillFilterMap( Condition postConditions )
                            throws InvalidParameterValueException {
        List<Element> complexValues = postConditions.getOperationParameter( "instanceFilter" ).getComplexValues();
        try {
            if ( filterMap.size() == 0 ) {
                for ( int i = 0; i < complexValues.size(); i++ ) {
                    Query q = Query.create( complexValues.get( 0 ) );
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
     *
     * @param condition
     * @param insert
     * @throws InvalidParameterValueException
     */
    private void validateOperation( Condition condition, Insert insert )
                            throws InvalidParameterValueException {

        OperationParameter op = condition.getOperationParameter( TYPENAME );

        // version is valid because no restrictions are made
        if ( op.isAny() ) {
            return;
        }

        if ( op.isUserCoupled() ) {
            userCoupled = true;
        } else {
            List<String> vals = op.getValues();
            List<QualifiedName> fts = insert.getAffectedFeatureTypes();
            for ( int i = 0; i < fts.size(); i++ ) {
                String qn = fts.get( i ).getFormattedString();
                if ( !vals.contains( qn ) ) {
                    String s = Messages.getMessage( "OWSPROXY_NOT_ALLOWED_FEATURETYPE", "insert", qn );
                    throw new InvalidParameterValueException( s );
                }
            }
        }
    }

    /**
     *
     * @param condition
     * @param delete
     * @throws InvalidParameterValueException
     */
    private void validateOperation( Condition condition, Delete delete )
                            throws InvalidParameterValueException {
        OperationParameter op = condition.getOperationParameter( TYPENAME );

        // version is valid because no restrictions are made
        if ( op.isAny() ) {
            return;
        }

        if ( op.isUserCoupled() ) {
            userCoupled = true;
        } else {
            List<String> vals = op.getValues();
            List<QualifiedName> fts = delete.getAffectedFeatureTypes();
            for ( int i = 0; i < fts.size(); i++ ) {
                String qn = fts.get( i ).getFormattedString();
                if ( !vals.contains( qn ) ) {
                    String s = Messages.getMessage( "OWSPROXY_NOT_ALLOWED_FEATURETYPE", "delete", qn );
                    throw new InvalidParameterValueException( s );
                }
            }
        }
    }

    /**
     *
     * @param condition
     * @param update
     * @throws InvalidParameterValueException
     */
    private void validateOperation( Condition condition, Update update )
                            throws InvalidParameterValueException {

        OperationParameter op = condition.getOperationParameter( TYPENAME );

        // version is valid because no restrictions are made
        if ( op.isAny() ) {
            return;
        }

        if ( op.isUserCoupled() ) {
            userCoupled = true;
        } else {
            List<String> vals = op.getValues();
            List<QualifiedName> fts = update.getAffectedFeatureTypes();
            for ( int i = 0; i < fts.size(); i++ ) {
                String qn = fts.get( i ).getFormattedString();
                if ( !vals.contains( qn ) ) {
                    String s = Messages.getMessage( "OWSPROXY_NOT_ALLOWED_FEATURETYPE", "update", qn );
                    throw new InvalidParameterValueException( s );
                }
            }
        }
    }

    /**
     * validates a Transcation.Delete request against the underlying users and rights management system
     *
     * @param delete
     * @param user
     * @throws InvalidParameterValueException
     * @throws UnauthorizedException
     */
    private void validateAgainstRightsDB( Delete delete, User user )
                            throws InvalidParameterValueException, UnauthorizedException {
        if ( user == null ) {
            throw new UnauthorizedException( Messages.getMessage( "OWSPROXY_NO_ANONYMOUS_ACCESS" ) );
        }

        List<QualifiedName> fts = delete.getAffectedFeatureTypes();
        for ( int i = 0; i < fts.size(); i++ ) {
            String name = fts.get( i ).getLocalName();
            String ns = fts.get( i ).getNamespace().toASCIIString();
            String qn = StringTools.concat( 200, '{', ns, "}:", name );

            // create a feature instance from the parameters of the GetFeature request
            // to enable comparsion with a filter encoding expression stored in the
            // assigned rights management system
            List<FeatureProperty> fps = new ArrayList<FeatureProperty>();
            QualifiedName tn = new QualifiedName( "typeName" );
            FeatureProperty fp = FeatureFactory.createFeatureProperty( tn, qn );
            fps.add( fp );
            Feature feature = FeatureFactory.createFeature( "id", deleteFT, fps );

            if ( securityConfig.getProxiedUrl() == null ) {
                handleUserCoupledRules( user, // the user who posted the request
                                        feature, // This is the Database feature
                                        qn, // the Qualified name of the users Featurerequest
                                        TYPE_FEATURETYPE, // a primary key in the db.
                                        DELETE );// We're requesting a featuretype.
            } else {
                handleUserCoupledRules( user, // the user who posted the request
                                        feature, // This is the Database feature
                                        "[" + securityConfig.getProxiedUrl() + "]:" + qn, // the Qualified name of the
                                        // users Featurerequest
                                        TYPE_FEATURETYPE, // a primary key in the db.
                                        DELETE );// We're requesting a featuretype.
            }
        }

    }

    /**
     * validates a Transcation.Update request against the underlying users and rights management system
     *
     * @param update
     * @param user
     * @throws UnauthorizedException
     * @throws InvalidParameterValueException
     */
    private void validateAgainstRightsDB( Update update, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        if ( user == null ) {
            throw new UnauthorizedException( Messages.getMessage( "OWSPROXY_NO_ANONYMOUS_ACCESS" ) );
        }

        List<QualifiedName> fts = update.getAffectedFeatureTypes();
        for ( int i = 0; i < fts.size(); i++ ) {
            String name = fts.get( i ).getLocalName();
            String ns = fts.get( i ).getNamespace().toASCIIString();
            String qn = StringTools.concat( 200, '{', ns, "}:", name );

            // create a feature instance from the parameters of the GetFeature request
            // to enable comparsion with a filter encoding expression stored in the
            // assigned rights management system
            List<FeatureProperty> fps = new ArrayList<FeatureProperty>();
            QualifiedName tn = new QualifiedName( "typeName" );
            FeatureProperty fp = FeatureFactory.createFeatureProperty( tn, qn );
            fps.add( fp );
            Feature feature = FeatureFactory.createFeature( "id", updateFT, fps );

            if ( securityConfig.getProxiedUrl() == null ) {
                handleUserCoupledRules( user, // the user who posted the request
                                        feature, // This is the Database feature
                                        qn, // the Qualified name of the users Featurerequest
                                        TYPE_FEATURETYPE, // a primary key in the db.
                                        UPDATE );// We're requesting a featuretype.
            } else {
                handleUserCoupledRules( user, // the user who posted the request
                                        feature, // This is the Database feature
                                        "[" + securityConfig.getProxiedUrl() + "]:" + qn, // the Qualified name of the
                                        // users Featurerequest
                                        TYPE_FEATURETYPE, // a primary key in the db.
                                        UPDATE );// We're requesting a featuretype.
            }
        }
    }

    /**
     * validates the passed insert operation against the deegree user/rights management system
     *
     * @param insert
     * @param user
     * @throws InvalidParameterValueException
     * @throws UnauthorizedException
     */
    private void validateAgainstRightsDB( Insert insert, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        if ( user == null ) {
            throw new UnauthorizedException( Messages.getMessage( "OWSPROXY_NO_ANONYMOUS_ACCESS" ) );
        }

        List<QualifiedName> fts = insert.getAffectedFeatureTypes();
        for ( int i = 0; i < fts.size(); i++ ) {
            String name = fts.get( i ).getLocalName();
            String ns = fts.get( i ).getNamespace().toASCIIString();
            String qn = StringTools.concat( 200, '{', ns, "}:", name );
            // create a feature instance from the parameters of the GetRecords request
            // to enable comparsion with a filter encoding expression stored in the
            // assigned rights management system
            List<FeatureProperty> fps = new ArrayList<FeatureProperty>();
            QualifiedName tn = new QualifiedName( "typeName" );
            FeatureProperty fp = FeatureFactory.createFeatureProperty( tn, qn );
            fps.add( fp );
            Feature feature = FeatureFactory.createFeature( "id", insertFT, fps );

            if ( securityConfig.getProxiedUrl() == null ) {
                handleUserCoupledRules( user, // the user who posted the request
                                        feature, // This is the Database feature
                                        qn, // the Qualified name of the users Featurerequest
                                        TYPE_FEATURETYPE, // a primary key in the db.
                                        INSERT );// We're requesting a featuretype.
            } else {
                handleUserCoupledRules( user, // the user who posted the request
                                        feature, // This is the Database feature
                                        "[" + securityConfig.getProxiedUrl() + "]:" + qn, // the Qualified name of the
                                                                                          // users Featurerequest
                                        TYPE_FEATURETYPE, // a primary key in the db.
                                        INSERT );// We're requesting a featuretype.
            }
        }

    }

    /**
     * creates a feature type that matches the parameters of a Insert operation
     *
     * @return created <tt>FeatureType</tt>
     */
    private static FeatureType createInsertFeatureType() {
        PropertyType[] ftps = new PropertyType[1];
        ftps[0] = FeatureFactory.createSimplePropertyType( new QualifiedName( "typeName" ), Types.VARCHAR, false );

        return FeatureFactory.createFeatureType( "WFS_Insert", false, ftps );
    }

    /**
     * creates a feature type that matches the parameters of a Update operation
     *
     * @return created <tt>FeatureType</tt>
     */
    private static FeatureType createUpdateFeatureType() {
        PropertyType[] ftps = new PropertyType[2];
        ftps[0] = FeatureFactory.createSimplePropertyType( new QualifiedName( "typeName" ), Types.VARCHAR, false );

        return FeatureFactory.createFeatureType( "WFS_Update", false, ftps );
    }

    /**
     * creates a feature type that matches the parameters of a Delete operation
     *
     * @return created <tt>FeatureType</tt>
     */
    private static FeatureType createDeleteFeatureType() {
        PropertyType[] ftps = new PropertyType[1];
        ftps[0] = FeatureFactory.createSimplePropertyType( new QualifiedName( "typeName" ), Types.VARCHAR, false );

        return FeatureFactory.createFeatureType( "WFS_Delete", false, ftps );
    }

}
