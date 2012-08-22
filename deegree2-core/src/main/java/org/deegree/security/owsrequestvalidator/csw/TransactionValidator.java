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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Expression;
import org.deegree.model.filterencoding.FeatureFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterConstructionException;
import org.deegree.model.filterencoding.Literal;
import org.deegree.model.filterencoding.LogicalOperation;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyIsBetweenOperation;
import org.deegree.model.filterencoding.PropertyIsCOMPOperation;
import org.deegree.model.filterencoding.PropertyIsLikeOperation;
import org.deegree.model.filterencoding.PropertyIsNullOperation;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.csw.manager.Delete;
import org.deegree.ogcwebservices.csw.manager.Insert;
import org.deegree.ogcwebservices.csw.manager.Operation;
import org.deegree.ogcwebservices.csw.manager.Transaction;
import org.deegree.ogcwebservices.csw.manager.Update;
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
import org.deegree.security.owsrequestvalidator.Messages;
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
public class TransactionValidator extends AbstractCSWRequestValidator {

    private static final ILogger LOG = LoggerFactory.getLogger( TransactionValidator.class );

    private final static String METADATAFORMAT = "metadataFormat";

    private final static String TYPENAME = "typeName";

    private static Map<QualifiedName, Filter> filterMap = new HashMap<QualifiedName, Filter>();

    private static FeatureType insertFT = null;

    private static FeatureType updateFT = null;

    private static FeatureType deleteFT = null;

    private static NamespaceContext nsc = CommonNamespaces.getNamespaceContext();

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

        Transaction cswreq = (Transaction) request;

        List<Operation> ops = cswreq.getOperations();
        for ( int i = 0; i < ops.size(); i++ ) {
            userCoupled = false;
            if ( ops.get( i ) instanceof Insert ) {
                Request req = policy.getRequest( "CSW", "CSW_Insert" );
                if ( req != null ) {
                    if ( !req.isAny() && !req.getPreConditions().isAny() ) {
                        Condition condition = req.getPreConditions();
                        validateOperation( condition, (Insert) ops.get( i ) );
                    }
                    if ( userCoupled ) {
                        validateAgainstRightsDB( (Insert) ops.get( i ), user );
                    }
                    if ( req.getPostConditions() != null ) {
                        evaluateFilter( ops.get( i ), req.getPostConditions(), user );
                    }
                } else {
                    throw new UnauthorizedException( "You are not allowed to Insert items from the repository." );
                }
            } else if ( ops.get( i ) instanceof Update ) {
                Request req = policy.getRequest( "CSW", "CSW_Update" );
                if ( req != null ) {
                    if ( !req.isAny() && !req.getPreConditions().isAny() ) {
                        Condition condition = req.getPreConditions();
                        validateOperation( condition, (Update) ops.get( i ) );
                    }
                    if ( userCoupled ) {
                        validateAgainstRightsDB( (Update) ops.get( i ), user );
                    }
                    if ( req.getPostConditions() != null ) {
                        evaluateFilter( ops.get( i ), req.getPostConditions(), user );
                    }
                } else {
                    throw new UnauthorizedException( "You are not allowed to update items from the repository." );
                }
            } else if ( ops.get( i ) instanceof Delete ) {
                Request req = policy.getRequest( "CSW", "CSW_Delete" );
                if ( req != null ) {
                    if ( !req.isAny() && !req.getPreConditions().isAny() ) {
                        Condition condition = req.getPreConditions();
                        validateOperation( condition, (Delete) ops.get( i ) );
                    }
                    if ( userCoupled ) {
                        validateAgainstRightsDB( (Delete) ops.get( i ), user );
                    }
                    if ( req.getPostConditions() != null ) {
                        evaluateFilter( ops.get( i ), req.getPostConditions(), user );
                    }
                } else {
                    throw new UnauthorizedException( "You are not allowed to delete items from the repository." );
                }
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
    private void evaluateFilter( Operation operation, Condition postConditions, User user )
                            throws InvalidParameterValueException, UnauthorizedException {
        if ( postConditions.getOperationParameter( "instanceFilter" ) != null ) {
            if ( postConditions.getOperationParameter( "instanceFilter" ).isAny() ) {
                return;
            }

            List<QualifiedName> qns = getMetadataTypes( operation );
            Map<QualifiedName, Filter> filter = null;
            if ( postConditions.getOperationParameter( "instanceFilter" ).isUserCoupled() ) {
                // read filterMap from constraints defined in deegree DRM
                filter = readFilterFromDRM( qns, operation, user );
            } else {
                fillFilterMap( postConditions );
                filter = filterMap;
            }

            if ( operation instanceof Update || operation instanceof Delete ) {
                handleUpdateDelete( operation, filter );
            } else {
                handleInsert( (Insert) operation, filter );
            }
        } else if ( operation instanceof Insert ) {
            // because of its pessimistic security concept deegree assumed
            // that there is a security validation if no filter is available
            // for a metadata type
            String msg = org.deegree.i18n.Messages.getMessage( "OWSPROXY_CSW_INSERT_NOT_ALLOWED" );
            throw new UnauthorizedException( msg );
        }
    }

    /**
     * validates if the content of an Insert operation matches the defined security conditions
     *
     * @param operation
     * @param filterMap
     * @throws InvalidParameterValueException
     * @throws UnauthorizedException
     */
    private void handleInsert( Insert operation, Map<QualifiedName, Filter> filterMap )
                            throws InvalidParameterValueException, UnauthorizedException {

        List<Element> records = operation.getRecords();

        for ( int i = 0; i < records.size(); i++ ) {
            Element rec = records.get( i );
            String name = rec.getLocalName();
            URI uri;
            try {
                uri = new URI( rec.getNamespaceURI() );
            } catch ( URISyntaxException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e.getMessage(), e );
            }
            QualifiedName qn = new QualifiedName( "a", name, uri );

            ComplexFilter filter = (ComplexFilter) filterMap.get( qn );
            if ( filter != null ) {
                boolean match = false;
                // just if a constraint is defined on combination of insert
                // operation and the current metadata type it can be evaluated
                LogicalOperation lo = (LogicalOperation) filter.getOperation();
                if ( lo.getOperatorId() == OperationDefines.AND ) {
                    // handle conditions connected by logical AND
                    List<org.deegree.model.filterencoding.Operation> args = lo.getArguments();
                    match = evaluateLogicalAnd( rec, args );
                } else if ( lo.getOperatorId() == OperationDefines.OR ) {
                    // handle conditions connected by logical OR
                    List<org.deegree.model.filterencoding.Operation> args = lo.getArguments();
                    match = evaluateLogicalOr( rec, args );
                } else {
                    // NOT
                }
                if ( !match ) {
                    // if loop has been left and 'match' is still false
                    // no condition has matched
                    String msg = org.deegree.i18n.Messages.getMessage( "OWSPROXY_CSW_INSERT_NOT_ALLOWED" );
                    throw new UnauthorizedException( msg );
                }
            } else {
                // because of its pessimistic security concept deegree assumed
                // that there is a security validation if no filter is available
                // for a metadata type
                String msg = org.deegree.i18n.Messages.getMessage( "OWSPROXY_CSW_INSERT_NOT_ALLOWED" );
                throw new UnauthorizedException( msg );
            }
        }

    }

    /**
     * evaluates if operations surrounded by a logical OR. If none of the contained conditions are fullfilled an
     * {@link UnauthorizedException} will be thrown
     *
     * @param rec
     * @param args
     * @return the boolean result
     * @throws InvalidParameterValueException
     */
    private boolean evaluateLogicalOr( Element rec, List<org.deegree.model.filterencoding.Operation> args )
                            throws InvalidParameterValueException {
        boolean match = false;
        for ( org.deegree.model.filterencoding.Operation op : args ) {
            try {
                match = evaluate( rec, op );
                if ( match ) {
                    // loop can be breaked if at least one condition
                    // matches
                    return true;
                }
            } catch ( XMLParsingException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e.getMessage(), e );
            }
        }
        return match;
    }

    /**
     * evaluates if operations surrounded by a logical AND. If at least one of the contained conditions is not
     * fullfilled an {@link UnauthorizedException} will be thrown
     *
     * @param rec
     * @param args
     * @return the truth
     * @throws InvalidParameterValueException
     */
    private boolean evaluateLogicalAnd( Element rec, List<org.deegree.model.filterencoding.Operation> args )
                            throws InvalidParameterValueException {
        boolean match = false;
        for ( org.deegree.model.filterencoding.Operation op : args ) {
            try {
                match = evaluate( rec, op );
                if ( !match ) {
                    break;
                }
            } catch ( XMLParsingException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e.getMessage(), e );
            }
        }
        return match;
    }

    /**
     * evaluates if the passed record matches the passed filter operation
     *
     * @param element
     * @param op
     * @return the truth
     * @throws XMLParsingException
     * @throws InvalidParameterValueException
     */
    private boolean evaluate( Element record, org.deegree.model.filterencoding.Operation op )
                            throws XMLParsingException, InvalidParameterValueException {
        if( op == null ){
            throw new InvalidParameterValueException( "The operation cannot be null" );
        }
        boolean matches = false;
        Expression exp = null;
        if ( op.getOperatorId() == OperationDefines.PROPERTYISEQUALTO
             || op.getOperatorId() == OperationDefines.PROPERTYISGREATERTHAN
             || op.getOperatorId() == OperationDefines.PROPERTYISGREATERTHANOREQUALTO
             || op.getOperatorId() == OperationDefines.PROPERTYISLESSTHAN
             || op.getOperatorId() == OperationDefines.PROPERTYISLESSTHANOREQUALTO ) {
            matches = evaluateCOMP( record, (PropertyIsCOMPOperation) op );
        } else if ( op.getOperatorId() == OperationDefines.PROPERTYISNULL ) {
            PropertyName pn = ( (PropertyIsNullOperation) op ).getPropertyName();
            String xpath = pn.getValue().getAsString();
            nsc.addAll( pn.getValue().getNamespaceContext() );
            matches = XMLTools.getNode( record, xpath, nsc ) == null;
        } else if ( op.getOperatorId() == OperationDefines.PROPERTYISBETWEEN ) {
            PropertyName pn = ( (PropertyIsBetweenOperation) op ).getPropertyName();
            String xpath = pn.getValue().getAsString();
            nsc.addAll( pn.getValue().getNamespaceContext() );
            exp = ( (PropertyIsBetweenOperation) op ).getLowerBoundary();
            String lower = ( (Literal) exp ).getValue();
            exp = ( (PropertyIsBetweenOperation) op ).getUpperBoundary();
            String upper = ( (Literal) exp ).getValue();
            String value = XMLTools.getNodeAsString( record, xpath, nsc, null );
            matches = lower.compareTo( value ) < 0 && upper.compareTo( value ) > 0;
        } else if ( op.getOperatorId() == OperationDefines.PROPERTYISLIKE ) {
            PropertyName pn = ( (PropertyIsLikeOperation) op ).getPropertyName();
            String xpath = pn.getValue().getAsString();
            nsc.addAll( pn.getValue().getNamespaceContext() );
            String value = XMLTools.getNodeAsString( record, xpath, nsc, null );
            String literal = ( (PropertyIsLikeOperation) op ).getLiteral().getValue();
            if( literal == null ){
                throw new InvalidParameterValueException( "No literal found resulting from the xpath: " + xpath + " therefore you're not authorized." );
            }
            matches = ( (PropertyIsLikeOperation) op ).matches( literal, value );
        } else if ( op.getOperatorId() == OperationDefines.AND ) {
            List<org.deegree.model.filterencoding.Operation> ops = ( (LogicalOperation) op ).getArguments();
            return evaluateLogicalAnd( record, ops );
        } else if ( op.getOperatorId() == OperationDefines.OR ) {
            List<org.deegree.model.filterencoding.Operation> ops = ( (LogicalOperation) op ).getArguments();
            return evaluateLogicalOr( record, ops );
        }
        return matches;
    }

    private boolean evaluateCOMP( Element record, PropertyIsCOMPOperation op )
                            throws XMLParsingException {
        boolean matches = false;
        Expression exp = op.getFirstExpression();
        PropertyName pn = (PropertyName) exp;
        String xpath = pn.getValue().getAsString();
        nsc.addAll( pn.getValue().getNamespaceContext() );
        String value = XMLTools.getNodeAsString( record, xpath, nsc, null );
        exp = op.getSecondExpression();
        Literal literal = (Literal) exp;
        if ( op.getOperatorId() == OperationDefines.PROPERTYISEQUALTO ) {
            matches = literal.getValue().equals( value );
        } else if ( op.getOperatorId() == OperationDefines.PROPERTYISGREATERTHAN ) {
            matches = value != null && literal.getValue().compareTo( value ) < 0;
        } else if ( op.getOperatorId() == OperationDefines.PROPERTYISGREATERTHANOREQUALTO ) {
            matches = value != null && literal.getValue().compareTo( value ) <= 0;
        } else if ( op.getOperatorId() == OperationDefines.PROPERTYISLESSTHAN ) {
            matches = value != null && literal.getValue().compareTo( value ) > 0;
        } else if ( op.getOperatorId() == OperationDefines.PROPERTYISLESSTHANOREQUALTO ) {
            matches = value != null && literal.getValue().compareTo( value ) >= 0;
        }
        return matches;
    }

    /**
     * redefines the constraints of the passed operation if necessary
     *
     * @param operation
     * @param filter
     * @return the constrained operation
     */
    private Operation handleUpdateDelete( Operation operation, Map<QualifiedName, Filter> filter ) {
        Filter tmpFilter = null;
        Filter opFilter = null;
        if ( operation instanceof Update ) {
            opFilter = ( (Update) operation ).getConstraint();
        } else {
            opFilter = ( (Delete) operation ).getConstraint();
        }
        if ( opFilter instanceof ComplexFilter ) {
            // create a new Filter that is a combination of the
            // original filter and the one defined in the GetFeatures
            // PostConditions coupled by a logical 'And'
            ComplexFilter qFilter = (ComplexFilter) opFilter;
            if ( filter == null ) {
                // not filter defined in security managment
                tmpFilter = qFilter;
            } else {
                // merger filter of update/delete operation and filter
                // defined in security managment
                tmpFilter = filter.values().iterator().next();
                tmpFilter = new ComplexFilter( qFilter, (ComplexFilter) tmpFilter, OperationDefines.AND );
            }
        } else if ( opFilter instanceof FeatureFilter ) {
            // just take original filter if it is as feature filter
            // because feature filter and complex filters can not
            // be combined
            tmpFilter = opFilter;
        }
        if ( operation instanceof Update ) {
            ( (Update) operation ).setConstraint( tmpFilter );
        } else {
            ( (Delete) operation ).setConstraint( tmpFilter );
        }
        return operation;
    }

    /**
     * reads a filter m
     *
     * @param operation
     * @param user
     * @return the filter map
     * @throws UnauthorizedException
     * @throws InvalidParameterValueException
     */
    private Map<QualifiedName, Filter> readFilterFromDRM( List<QualifiedName> qns, Operation operation, User user )
                            throws UnauthorizedException, InvalidParameterValueException {
        Map<QualifiedName, Filter> f = new HashMap<QualifiedName, Filter>();
        try {
            SecurityAccessManager sam = SecurityAccessManager.getInstance();
            SecurityAccess access = sam.acquireAccess( user );

            for ( int i = 0; i < qns.size(); i++ ) {
                List<ComplexFilter> foundFilters = new ArrayList<ComplexFilter>();
                SecuredObject secObj = access.getSecuredObjectByName( qns.get( i ).getFormattedString(),
                                                                      ClientHelper.TYPE_METADATASCHEMA );
                RightSet rs = user.getRights( access, secObj );
                Right right = null;
                if ( operation instanceof Update ) {
                    right = rs.getRight( secObj, RightType.UPDATE_RESPONSE );
                } else if ( operation instanceof Delete ) {
                    right = rs.getRight( secObj, RightType.DELETE_RESPONSE );
                } else {
                    right = rs.getRight( secObj, RightType.INSERT_RESPONSE );
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
                        f.put( qns.get( i ), filter );
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

    /**
     * @return the list a metadata types targeted by an operation
     *
     * @param operation
     * @throws InvalidParameterValueException
     */
    private List<QualifiedName> getMetadataTypes( Operation operation )
                            throws InvalidParameterValueException {
        List<QualifiedName> qns = new ArrayList<QualifiedName>();
        if ( operation instanceof Update ) {

        } else if ( operation instanceof Delete ) {

        } else {
            // get list of all record types to be inserted
            List<Element> recs = ( (Insert) operation ).getRecords();
            for ( int i = 0; i < recs.size(); i++ ) {
                String name = recs.get( i ).getLocalName();
                URI uri;
                try {
                    uri = new URI( recs.get( i ).getNamespaceURI() );
                } catch ( URISyntaxException e ) {
                    LOG.logError( e.getMessage(), e );
                    throw new InvalidParameterValueException( e.getMessage(), e );
                }
                QualifiedName qn = new QualifiedName( "a", name, uri );
                if ( !qns.contains( qn ) ) {
                    qns.add( qn );
                }
            }
        }
        return qns;
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

        OperationParameter op = condition.getOperationParameter( METADATAFORMAT );

        // version is valid because no restrictions are made
        if ( op.isAny() ) {
            return;
        }

        List<?> vals = op.getValues();

        List<Element> records = insert.getRecords();
        for ( int i = 0; i < records.size(); i++ ) {
            String name = records.get( i ).getLocalName();
            String ns = records.get( i ).getNamespaceURI();
            String qn = StringTools.concat( 200, '{', ns, "}:", name );

            if ( !vals.contains( qn ) ) {
                if ( !op.isUserCoupled() ) {
                    String s = Messages.format( "CSWTransactionValidator.INVALIDMETADATAFORMAT", qn );
                    throw new InvalidParameterValueException( s );
                }
                userCoupled = true;
                break;
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
        if ( op.isAny() )
            return;

        URI typeName = delete.getTypeName();

        if ( typeName == null ) {
            String s = Messages.getString( "CSWTransactionValidator.INVALIDDELETETYPENAME1" );
            throw new InvalidParameterValueException( s );
        }

        List<?> vals = op.getValues();
        if ( !vals.contains( typeName.toASCIIString() ) ) {
            if ( !op.isUserCoupled() ) {
                String s = Messages.format( "CSWTransactionValidator.INVALIDDELETETYPENAME2", typeName );
                throw new InvalidParameterValueException( s );
            }
            userCoupled = true;
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

        URI typeName = update.getTypeName();
        Element record = update.getRecord();

        if ( typeName == null && record == null ) {
            String s = Messages.getString( "CSWTransactionValidator.INVALIDUPDATETYPENAME1" );
            throw new InvalidParameterValueException( s );
        }

        OperationParameter op = condition.getOperationParameter( TYPENAME );
        List<?> vals = op.getValues();

        if ( typeName != null && !vals.contains( typeName.toASCIIString() ) ) {
            // version is valid because no restrictions are made
            if ( op.isAny() ) {
                return;
            }
            if ( !op.isUserCoupled() ) {
                String s = Messages.format( "CSWTransactionValidator.INVALIDUPDATETYPENAME2", typeName );
                throw new InvalidParameterValueException( s );
            }
            userCoupled = true;
        } else {
            op = condition.getOperationParameter( METADATAFORMAT );
            // version is valid because no restrictions are made
            if ( op.isAny() ) {
                return;
            }
            vals = op.getValues();
            String name = record.getLocalName();
            String ns = record.getNamespaceURI();
            String qn = StringTools.concat( 200, '{', ns, "}:", name );
            if ( !vals.contains( qn ) ) {
                if ( !op.isUserCoupled() ) {
                    String s = Messages.format( "CSWTransactionValidator.INVALIDMETADATAFORMAT", qn );
                    throw new InvalidParameterValueException( s );
                }
                userCoupled = true;
            }
        }
    }

    /**
     * validates a Transcation.Delete request against the underlying users and rights management system
     *
     * @param delete
     * @param version
     * @param user
     * @throws InvalidParameterValueException
     * @throws UnauthorizedException
     */
    private void validateAgainstRightsDB( Delete delete, User user )
                            throws InvalidParameterValueException, UnauthorizedException {
        if ( user == null ) {
            throw new UnauthorizedException( Messages.getString( "RequestValidator.NOACCESS" ) );
        }

        // create a feature instance from the parameters of the GetRecords request
        // to enable comparsion with a filter encoding expression stored in the
        // assigned rights management system
        List<FeatureProperty> fps = new ArrayList<FeatureProperty>();

        URI typeName = delete.getTypeName();
        String tn = null;
        if ( typeName != null ) {
            tn = typeName.toASCIIString();
        }
        FeatureProperty fp = FeatureFactory.createFeatureProperty( new QualifiedName( "typeName" ), tn );
        fps.add( fp );
        Feature feature = FeatureFactory.createFeature( "id", insertFT, fps );

        handleUserCoupledRules( user, // the user who posted the request
                                feature, // This is the Database feature
                                // the name the metadataFormat to be deleted
                                "{http://www.opengis.net/cat/csw}:profil", ClientHelper.TYPE_METADATASCHEMA, // a
                                // primary
                                // key
                                // in
                                // the
                                // db.
                                RightType.DELETE );// We're requesting a featuretype.

    }

    /**
     * validates a Transcation.Update request against the underlying users and rights management system
     *
     * @param update
     * @param user
     */
    private void validateAgainstRightsDB( Update update, User user ) {
        throw new NoSuchMethodError( getClass().getName() + ".validateAgainstRightsDB not implemented yet" );
    }

    /**
     * validates the passed insert operation against the deegree user/rights management system
     *
     * @param insert
     * @param version
     * @param user
     * @throws InvalidParameterValueException
     * @throws UnauthorizedException
     */
    private void validateAgainstRightsDB( Insert insert, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        if ( user == null ) {
            throw new UnauthorizedException( Messages.getString( "RequestValidator.NOACCESS" ) );
        }

        // create a feature instance from the parameters of the GetRecords request
        // to enable comparsion with a filter encoding expression stored in the
        // assigned rights management system
        List<FeatureProperty> fps = new ArrayList<FeatureProperty>();
        FeatureProperty fp = null;
        fps.add( fp );

        Feature feature = FeatureFactory.createFeature( "id", insertFT, fps );

        List<Element> records = insert.getRecords();
        for ( int i = 0; i < records.size(); i++ ) {
            String name = records.get( i ).getLocalName();
            String ns = records.get( i ).getNamespaceURI();
            String qn = StringTools.concat( 200, '{', ns, "}:", name );

            handleUserCoupledRules( user, // the user who posted the request
                                    feature, // This is the Database feature
                                    qn, // the Qualified name of the users Featurerequest
                                    ClientHelper.TYPE_METADATASCHEMA, // a primary key in the db.
                                    RightType.INSERT );// We're requesting a featuretype.
        }

    }

    /**
     * creates a feature type that matches the parameters of a Insert operation
     *
     * @return created <tt>FeatureType</tt>
     */
    private static FeatureType createInsertFeatureType() {
        PropertyType[] ftps = new PropertyType[1];
        ftps[0] = FeatureFactory.createSimplePropertyType( new QualifiedName( "metadataFormat" ), Types.VARCHAR, false );

        return FeatureFactory.createFeatureType( "CSW_Insert", false, ftps );
    }

    /**
     * creates a feature type that matches the parameters of a Update operation
     *
     * @return created <tt>FeatureType</tt>
     */
    private static FeatureType createUpdateFeatureType() {
        PropertyType[] ftps = new PropertyType[2];
        ftps[0] = FeatureFactory.createSimplePropertyType( new QualifiedName( "metadataFormat" ), Types.VARCHAR, false );
        ftps[1] = FeatureFactory.createSimplePropertyType( new QualifiedName( "typeName" ), Types.VARCHAR, false );

        return FeatureFactory.createFeatureType( "CSW_Update", false, ftps );
    }

    /**
     * creates a feature type that matches the parameters of a Delete operation
     *
     * @return created <tt>FeatureType</tt>
     */
    private static FeatureType createDeleteFeatureType() {
        PropertyType[] ftps = new PropertyType[1];
        ftps[0] = FeatureFactory.createSimplePropertyType( new QualifiedName( "typeName" ), Types.VARCHAR, false );

        return FeatureFactory.createFeatureType( "CSW_Delete", false, ftps );
    }

}
