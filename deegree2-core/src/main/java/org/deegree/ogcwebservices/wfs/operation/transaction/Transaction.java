//$Header: /deegreerepository/deegree/src/org/deegree/ogcwebservices/wfs/operation/transaction/Transaction.java,v 1.11 2007/02/07 15:01:51 poth Exp $
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
package org.deegree.ogcwebservices.wfs.operation.transaction;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.KVP2Map;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathStep;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.WFService;
import org.deegree.ogcwebservices.wfs.operation.AbstractWFSRequest;
import org.w3c.dom.Element;

/**
 * Represents a <code>Transaction</code> request to a web feature service.
 * <p>
 * A <code>Transaction</code> consists of a sequence of {@link Insert}, {@link Update}, {@link Delete} and
 * {@link Native} operations.
 * <p>
 * From the WFS Specification 1.1.0 OGC 04-094 (#12, Pg.63):
 * <p>
 * A <code>Transaction</code> request is used to describe data transformation operations that are to be applied to web
 * accessible feature instances. When the transaction has been completed, a web feature service will generate an XML
 * response document indicating the completion status of the transaction.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Transaction extends AbstractWFSRequest {

    private static final long serialVersionUID = 6904739857311368390L;

    private static final ILogger LOG = LoggerFactory.getLogger( Transaction.class );

    private List<TransactionOperation> operations;

    // request version
    private String version;

    // transaction ID
    private String id;

    // LockID associated with the request
    private String lockId;

    /**
     * Specifies if ALL records should be released or if SOME records, indicating only those records which have been
     * modified will be released. The default is ALL.
     */
    private RELEASE_ACTION releaseAction = RELEASE_ACTION.ALL;

    private TransactionDocument sourceDocument;

    /** Controls how locked features are treated when a transaction request is completed. */
    public static enum RELEASE_ACTION {

        /**
         * Indicates that the locks on all feature instances locked using the associated lockId should be released when
         * the transaction completes, regardless of whether or not a particular feature instance in the locked set was
         * actually operated upon.
         */
        ALL,

        /**
         * Indicates that only the locks on feature instances modified by the transaction should be released. The other,
         * unmodified, feature instances should remain locked using the same lockId so that subsequent transactions can
         * operate on those feature instances. If an expiry period was specified, the expiry counter must be reset to
         * zero after each transaction unless all feature instances in the locked set have been operated upon.
         */
        SOME
    }

    /**
     * Creates a new <code>Transaction</code> instance.
     * 
     * @param version
     *            WFS version
     * @param id
     *            Transaction id
     * @param versionSpecificParameter
     * @param lockID
     *            Lock Id
     * @param operations
     *            List of operations to be carried out
     * @param releaseAllFeatures
     * @param sourceDocument
     */
    public Transaction( String id, String version, Map<String, String> versionSpecificParameter, String lockID,
                        List<TransactionOperation> operations, boolean releaseAllFeatures,
                        TransactionDocument sourceDocument ) {
        super( version, id, null, versionSpecificParameter );
        this.id = id;
        this.version = version;
        this.lockId = lockID;
        this.operations = operations;
        if ( !releaseAllFeatures ) {
            this.releaseAction = RELEASE_ACTION.SOME;
        }
        this.sourceDocument = sourceDocument;
    }

    /**
     * Returns the source document that was used to create this <code>Transaction</code> instance.
     * 
     * @return the source document
     */
    public TransactionDocument getSourceDocument() {
        return this.sourceDocument;
    }

    /**
     * Returns the {@link TransactionOperation}s that are contained in the transaction.
     * 
     * @return the contained operations
     */
    public List<TransactionOperation> getOperations() {
        return this.operations;
    }

    /**
     * Returns the lock identifier associated with this transaction.
     * 
     * @return the lock identifier associated with this transaction if it exists, null otherwise
     */
    public String getLockId() {
        return this.lockId;
    }

    /**
     * Returns the release action mode to be applied after the transaction finished successfully.
     * 
     * @see RELEASE_ACTION
     * @return the release action mode to be applied after the transaction finished successfully
     */
    public RELEASE_ACTION getReleaseAction() {
        return this.releaseAction;
    }

    /**
     * Returns the names of the feature types that are affected by the transaction.
     * 
     * @return the names of the affected feature types
     */
    public Set<QualifiedName> getAffectedFeatureTypes() {
        Set<QualifiedName> featureTypeSet = new HashSet<QualifiedName>();

        Iterator<TransactionOperation> iter = this.operations.iterator();
        while ( iter.hasNext() ) {
            TransactionOperation operation = iter.next();
            featureTypeSet.addAll( operation.getAffectedFeatureTypes() );
        }
        return featureTypeSet;
    }

    /**
     * Creates a <code>Transaction</code> request from a key-value-pair encoding of the parameters contained in the
     * passed variable 'request'.
     * 
     * @param id
     *            id of the request
     * @param request
     *            key-value-pair encoded GetFeature request
     * @return new created Transaction instance
     * @throws InconsistentRequestException
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    public static Transaction create( String id, String request )
                            throws InconsistentRequestException, InvalidParameterValueException,
                            MissingParameterValueException {

        Map<String, String> model = KVP2Map.toMap( request );
        model.put( "ID", id );

        return create( model );
    }

    /**
     * Creates a <code>Transaction</code> request from a key-value-pair encoding of the parameters contained in the
     * given Map.
     * 
     * @param model
     *            key-value-pair encoded Transaction request
     * @return new Transaction instance
     * @throws InconsistentRequestException
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    public static Transaction create( Map<String, String> model )
                            throws InconsistentRequestException, InvalidParameterValueException,
                            MissingParameterValueException {

        Map<String, String> versionSpecificParameter = null;

        String id = model.get( "ID" );

        String version = checkVersionParameter( model );

        checkServiceParameter( model );

        String request = model.remove( "REQUEST" );
        if ( request == null ) {
            throw new InconsistentRequestException( "Request parameter for a transaction request must be set." );
        }

        String lockID = model.remove( "LOCKID" );

        String releaseAction = model.remove( "RELEASEACTION" );
        boolean releaseAllFeatures = true;
        if ( releaseAction != null ) {
            if ( "SOME".equals( releaseAction ) ) {
                releaseAllFeatures = false;
            } else if ( "ALL".equals( releaseAction ) ) {
                releaseAllFeatures = true;
            } else {
                throw new InvalidParameterValueException( "releaseAction", releaseAction );
            }
        }

        QualifiedName[] typeNames = extractTypeNames( model );

        String featureIdParameter = model.remove( "FEATUREID" );
        if ( typeNames == null && featureIdParameter == null ) {
            throw new InconsistentRequestException( "TypeName OR FeatureId parameter must be set." );
        }

        // String[] featureIds = null;
        // if ( featureIdParameter != null ) {
        // // FEATUREID specified. Looking for featureId
        // // declaration TYPENAME contained in featureId declaration (eg.
        // // FEATUREID=InWaterA_1M.1013)
        // featureIds = StringTools.toArray( featureIdParameter, ",", false );
        // //typeNameSet = extractTypeNameFromFeatureId( featureIds, context, (HashSet) typeNameSet
        // );
        // }

        // Filters
        // Map typeFilter = buildFilterMap( model, typeNames, featureIds, context );

        // // BBOX
        // typeFilter = extractBBOXParameter( model, typeNames, typeFilter );
        //
        // if ( typeFilter == null || typeFilter.size() == 0 ) {
        // for ( int i = 0; i < typeNames.length; i++ ) {
        // typeFilter.put( typeNames[i], null );
        // }
        // }

        List<TransactionOperation> operations = extractOperations( model, null );

        return new Transaction( id, version, versionSpecificParameter, lockID, operations, releaseAllFeatures, null );
    }

    /**
     * Extracts the {@link TransactionOperation}s contained in the given kvp request.
     * 
     * @param model
     * @param typeFilter
     * @return List
     * @throws InconsistentRequestException
     */
    private static List<TransactionOperation> extractOperations( Map<String, String> model,
                                                                 Map<QualifiedName, Filter> typeFilter )
                            throws InconsistentRequestException {
        List<TransactionOperation> operation = new ArrayList<TransactionOperation>();
        String op = model.remove( "OPERATION" );
        if ( op == null ) {
            throw new InconsistentRequestException( "Operation parameter must be set" );
        }
        if ( op.equals( "Delete" ) ) {
            List<Delete> deletes = Delete.create( typeFilter );
            operation.addAll( deletes );
        } else {
            String msg = "Invalid OPERATION parameter '" + op
                         + "'. KVP Transactions only support the 'Delete' operation.";
            throw new InconsistentRequestException( msg );
        }
        return operation;
    }

    /**
     * Creates a <code>Transaction</code> instance from a document that contains the DOM representation of the request.
     * 
     * @param id
     * @param root
     *            element that contains the DOM representation of the request
     * @return transaction instance
     * @throws OGCWebServiceException
     */
    public static Transaction create( String id, Element root )
                            throws OGCWebServiceException {
        TransactionDocument doc = new TransactionDocument();
        doc.setRootElement( root );
        Transaction request;
        try {
            request = doc.parse( id );
        } catch ( XMLParsingException e ) {
            if ( e.getWrapped() != null ) {
                throw e.getWrapped();
            }
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( "Transaction", e.getMessage() );
        }
        return request;
    }

    @Override
    public String toString() {
        String ret = this.getClass().getName();
        ret += "version: " + this.version + "\n";
        ret += "id: " + this.id + "\n";
        ret += "lockID: " + this.lockId + "\n";
        ret += "operations: \n";
        for ( int i = 0; i < operations.size(); i++ ) {
            ret += ( i + ": " + operations.get( i ) + "\n " );
        }
        ret += "releaseAllFeatures: " + this.releaseAction;
        return ret;
    }

    /**
     * Adds missing namespaces in the names of requested feature types.
     * <p>
     * If the {@link QualifiedName} of a requested type has a null namespace, the first qualified feature type name of
     * the given {@link WFService} with the same local name is used instead.
     * <p>
     * Note: The method changes this request (the feature type names) and should only be called by the
     * <code>WFSHandler</code> class.
     * 
     * @param wfs
     *            {@link WFService} instance that is used for the lookup of proper (qualified) feature type names
     */
    public void guessMissingNamespaces( WFService wfs ) {

        Set<QualifiedName> featureNames = wfs.getMappedFeatureTypes().keySet();
        for ( int j = 0; j < operations.size(); j++ ) {
            TransactionOperation op = operations.get( j );
            if ( op instanceof Update ) {
                Update update = (Update) op;
                QualifiedName tn = update.getTypeName();
                if ( tn.getNamespace() == null ) {
                    QualifiedName newTn = guessTypeNameNamespace( tn, featureNames );
                    update.setTypeName( newTn );
                }

                if ( update.getReplacementProperties() != null ) {
                    Set<PropertyPath> propPaths = update.getReplacementProperties().keySet();
                    String defaultPrefix = update.getTypeName().getPrefix();
                    URI defaultNamespace = update.getTypeName().getNamespace();
                    guessMissingPropertyNamespace( propPaths, defaultPrefix, defaultNamespace, update, j );
                }

            } else if ( op instanceof Delete ) {
                Delete delete = (Delete) op;
                QualifiedName newTn = guessTypeNameNamespace( delete.getTypeName(), featureNames );
                delete.setTypeName( newTn );
            }
        }
    }

    private void guessMissingPropertyNamespace( Set<PropertyPath> propPaths, String defaultPrefix,
                                                URI defaultNamespace, Update update, int j ) {
        for ( PropertyPath propPath : propPaths ) {
            for ( int i = 0; i < propPath.getAllSteps().size(); i++ ) {
                PropertyPathStep step = propPath.getStep( i );
                QualifiedName prop = step.getPropertyName();
                if ( prop.getNamespace() == null ) {
                    // the following retrieves the old values the object involved
                    Map<PropertyPath, FeatureProperty> oldMap = update.getReplacementProperties();
                    List<PropertyPathStep> steps = propPath.getAllSteps();
                    FeatureProperty oldValue = oldMap.get( propPath );
                    oldMap.remove( propPath );
                    QualifiedName newQName = new QualifiedName( defaultPrefix, prop.getLocalName(), defaultNamespace );

                    // replaces old values in the objects involved
                    step.setPropertyName( newQName );
                    FeatureProperty newValue = FeatureFactory.createFeatureProperty( newQName, oldValue.getValue() );
                    steps.set( i, step );
                    propPath.setSteps( steps );
                    oldMap.put( propPath, newValue );
                    update.setReplacementProperties( oldMap );
                    operations.set( j, update );
                }
            }
        }
    }

    private QualifiedName guessTypeNameNamespace( QualifiedName candidate, Set<QualifiedName> featureNames ) {
        if ( candidate.getNamespace() == null ) {
            for ( QualifiedName ftName : featureNames ) {
                if ( ftName.getLocalName().equals( candidate.getLocalName() ) ) {
                    return ftName;
                }
            }
        }
        return candidate;
    }

}
